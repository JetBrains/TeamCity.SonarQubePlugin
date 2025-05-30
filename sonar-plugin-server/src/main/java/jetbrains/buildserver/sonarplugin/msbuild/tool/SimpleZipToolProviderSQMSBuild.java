

package jetbrains.buildserver.sonarplugin.msbuild.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeToolVersion;
import jetbrains.buildserver.sonarplugin.tool.SimpleZipToolProvider;
import jetbrains.buildserver.sonarplugin.tool.SonarQubeToolProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SimpleZipToolProviderSQMSBuild implements SimpleZipToolProvider {
    private static final Logger LOG = Logger.getInstance(SimpleZipToolProviderSQMSBuild.class.getName());

    private static final String DEFAULT_BUNDLED_VERSION = "3.0.3.778";
    private static final String SONAR_FILE_NAME_PARTS_SEPARATOR = "[\\.-]";
    private static final String VERSION_PATTERN = "(?<" + SonarQubeToolProvider.VERSION_GROUP_NAME + ">(\\d[\\d\\\\.]*)(-[\\w\\.]+)?(-[\\w\\.]+)?)";
    private static final String ZIP_EXTENSION = "\\.zip";
    static final String SONAR_QUBE_SCANNER_MSBUILD_EXE = "SonarQube.Scanner.MSBuild.exe";
    static final String SONAR_QUBE_SCANNER_MSBUILD_ALT_EXE = "SonarScanner.MSBuild.exe";
    static final String BIN = "bin";
    static final String TEAMCITY_PLUGIN_XML = "teamcity-plugin.xml";
    static final String SONAR_SCANNER_PREFIX = "sonar-scanner";

    @NotNull
    private final ToolType myToolType;
    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private final String myPackedSonarQubeScannerRootZipPattern;
    @NotNull private final String myPackedSonarQubeScannerRootDirPattern;

    public SimpleZipToolProviderSQMSBuild(@NotNull final PluginDescriptor pluginDescriptor,
                                          @NotNull final SonarQubeMSBuildToolType sonarQubeScannerToolType) {
        myPluginDescriptor = pluginDescriptor;
        myToolType = sonarQubeScannerToolType;
        myPackedSonarQubeScannerRootZipPattern = myToolType.getType() + SONAR_FILE_NAME_PARTS_SEPARATOR + VERSION_PATTERN + ZIP_EXTENSION;
        myPackedSonarQubeScannerRootDirPattern = myToolType.getType() + SONAR_FILE_NAME_PARTS_SEPARATOR + VERSION_PATTERN;
    }

    @NotNull
    @Override
    public Path getBundledVersionsRoot() {
        return myPluginDescriptor.getPluginRoot().toPath().resolve(SonarQubeToolProvider.BUNDLED_SUBPATH);
    }

    @NotNull
    @Override
    public String getName() {
        return "SonarScanner for MSBuild";
    }

    @NotNull
    @Override
    public String getPackedZipPattern() {
        return myPackedSonarQubeScannerRootZipPattern;
    }

    @NotNull
    @Override
    public String getPackedDirPattern() {
        return myPackedSonarQubeScannerRootDirPattern;
    }

    @Override
    @NotNull
    public ToolType getToolType() {
        return myToolType;
    }

    @NotNull
    @Override
    public String getVersionPattern() {
        return ".*?[.-]" + VERSION_PATTERN + ZIP_EXTENSION;
    }

    @NotNull
    @Override
    public GetPackageVersionResult parseVersion(final Path toolPackage, final String version) throws Exception {
        try (final FileSystem fs = FileSystems.newFileSystem(toolPackage, (ClassLoader)null)) {
            if (existsAndExecutable(fs, "/" + SONAR_QUBE_SCANNER_MSBUILD_EXE) || existsAndExecutable(fs, "/" + SONAR_QUBE_SCANNER_MSBUILD_ALT_EXE)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(getToolType(), version, getToolType().getType() + "." + version));
            } else {
                return GetPackageVersionResult.error("Doesn't seem like SonarScanner for MSBuild: cannot find '" + SONAR_QUBE_SCANNER_MSBUILD_EXE + "'");
            }
        }
    }

    private boolean existsAndExecutable(FileSystem fs, String path) {
        final Path executable = fs.getPath(path);
        return Files.exists(executable) && Files.isRegularFile(executable);
    }

    @NotNull
    @Override
    public GetPackageVersionResult tryParsePackedPackage(@NotNull final Path path, @NotNull final Matcher matcher) {
        if (matcher.matches()) {
            final String version = matcher.group(SonarQubeToolProvider.VERSION_GROUP_NAME);

            return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, version, myToolType.getType() + "." + version));
        } else {
            LOG.warn("Unexpected package " + path.getFileName().toString() + ", only sonar-scanner-msbuid with " + SonarQubeToolProvider.VERSION_GROUP_NAME + " suffix is allowed.");
            return GetPackageVersionResult.error("Unexpected package " + path.getFileName().toString() + ", only sonar-scanner-msbuid with " + SonarQubeToolProvider.VERSION_GROUP_NAME + " suffix is allowed.");
        }
    }

    @NotNull
    @Override
    public String getDefaultBundledVersion() {
        return myToolType.getType() + "." + DEFAULT_BUNDLED_VERSION;
    }

    @NotNull
    @Override
    public GetPackageVersionResult describeBrokenPackage() {
        return GetPackageVersionResult.error("Should be single zip, name should contain version, eg: sonar-scanner-msbuild.4.0.2.892.zip");
    }

    @Override
    public void validatePackedTool(@NotNull final Path toolPackage) throws ToolException {
        final Matcher matcher = Pattern.compile(myPackedSonarQubeScannerRootZipPattern).matcher(toolPackage.getFileName().toString());
        if (!matcher.matches()) {
            LOG.warn("Cannot unpack " + toolPackage + ": should be single zip file with version suffix: 'sonar-scanner-msbuild-4.0.2.892.zip'.");
            throw new ToolException("Cannot unpack " + toolPackage + ": should be single zip file with version suffix: 'sonar-scanner-msbuild-4.0.2.892.zip'.");
        }
    }

    private static Path resolve(@NotNull final Path targetPath, @NotNull final Path path) {
        final String[] paths = StreamSupport.stream(path.spliterator(), false).map(Path::toString).toArray(String[]::new);
        return targetPath.getFileSystem().getPath(targetPath.toString(), paths);
    }

    @Override
    public void layoutContents(@NotNull final Path toolPath, @NotNull final Path targetPath) throws ToolException {
        try (final FileSystem fs = FileSystems.newFileSystem(toolPath, (ClassLoader)null)) {
            Files.walkFileTree(fs.getPath("/"), new FileVisitor<Path>() {
                @NotNull private Path currentPath = targetPath;

                @Override
                public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
                    final Path targetDir = resolve(targetPath, fs.getPath("/").relativize(path));

                    try {
                        Files.createDirectory(targetDir);
                    } catch (FileAlreadyExistsException ignore) {
                    }

                    currentPath = targetDir;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
                    Path resolve = currentPath.resolve(path.getFileName().toString());
                    if (!Files.exists(resolve) && resolve.startsWith(currentPath)) {
                        Files.copy(path, resolve);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path path, final IOException e) {
                    Loggers.SERVER.warn(e);

                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path path, final IOException e) {
                    currentPath = currentPath.getParent();

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new ToolException("Error", e);
        }

        List<Path> sonarScanners = null;
        try (Stream<Path> children = Files.list(targetPath)) {
            sonarScanners = children.filter(p -> p.getFileName().toString().startsWith(SONAR_SCANNER_PREFIX)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sonarScanners == null) {
            sonarScanners = Collections.emptyList();
        }
        List<Path> executablePaths = new ArrayList<>();
        for (Path sonarScanner: sonarScanners) {
            try (Stream<Path> bin = Files.list(sonarScanner.resolve(BIN))) {
                bin.filter(Files::isRegularFile).forEach(executablePaths::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (Stream<Path> children = Files.list(targetPath)) {
            children.filter(p -> p.getFileName().toString().endsWith(".exe")).forEach(executablePaths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Path descriptor = targetPath.resolve(TEAMCITY_PLUGIN_XML);
        try {
            List<String> iterable = new ArrayList<>(Arrays.asList(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<teamcity-agent-plugin xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                    "                       xsi:noNamespaceSchemaLocation=\"urn:shemas-jetbrains-com:teamcity-agent-plugin-v1-xml\">",
                    "  <tool-deployment>",
                    "    <layout>",
                    "      <executable-files>"));
            for (Path path: executablePaths) {
                iterable.add("          <include name='" + targetPath.relativize(path).toString() + "'/>");
            }
            iterable.addAll(Arrays.asList(
                    "      </executable-files>",
                    "    </layout>",
                    "  </tool-deployment>",
                    "</teamcity-agent-plugin>"));
            Files.write(descriptor, iterable);
        } catch (IOException e) {
            throw new ToolException("Cannot write teamcity-plugin.xml", e);
        }
    }
}