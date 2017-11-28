package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SonarQubeToolProvider extends ServerToolProviderAdapter {

    private static final Logger LOG = Logger.getInstance(SonarQubeToolProvider.class.getName());

    private static final String BUNDLED_SUBPATH = "bundled";

    private static final String VERSION_GROUP_NAME = "version";
    private static final String VERSION_PATTERN = "(?<" + VERSION_GROUP_NAME + ">(\\d+.)*\\d+)";
    private static final String SONAR_QUBE_SCANNER_TYPE = "scanner";
    private static final String SONAR_QUBE_RUNNER_TYPE = "runner";
    private static final String TYPE_GROUP_NAME = "type";
    private static final String SONAR_QUBE_SCANNER_TYPE_SUFFIX = "-(?<" + TYPE_GROUP_NAME + ">" + SONAR_QUBE_RUNNER_TYPE + "|"+ SONAR_QUBE_SCANNER_TYPE + ")";
    private static final String ZIP_EXTENSION = "\\.zip";
    private static final String JAR_EXTENSION = "\\.jar";

    @NotNull private final Pattern myPackedSonarQubeScannerRootZipPattern;
    @NotNull private final Pattern myPackedSonarQubeScannerRootDirPattern;
    @NotNull private final String myDefaultBundledVersionString;

    private static final String DEFAULT_BUNDLED_VERSION = "2.4";
    private static final Pattern SONAR_QUBE_SCANNER_JAR_PATTERN = Pattern.compile(".*?[.-]" + VERSION_PATTERN + JAR_EXTENSION);

    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private final ToolType myToolType;

    public SonarQubeToolProvider(@NotNull final PluginDescriptor pluginDescriptor, @NotNull final SonarQubeScannerToolType sonarQubeScannerToolType) {
        myPluginDescriptor = pluginDescriptor;
        myToolType = sonarQubeScannerToolType;
        myDefaultBundledVersionString = myToolType.getType() + "." + DEFAULT_BUNDLED_VERSION + "-" + SONAR_QUBE_RUNNER_TYPE;
        myPackedSonarQubeScannerRootZipPattern = Pattern.compile(myToolType.getType() + "\\." + VERSION_PATTERN + SONAR_QUBE_SCANNER_TYPE_SUFFIX + ZIP_EXTENSION);
        myPackedSonarQubeScannerRootDirPattern = Pattern.compile(myToolType.getType() + "\\." + VERSION_PATTERN + SONAR_QUBE_SCANNER_TYPE_SUFFIX);
    }

    @NotNull
    @Override
    public ToolType getType() {
        return myToolType;
    }

    @NotNull
    @Override
    public Collection<InstalledToolVersion> getBundledToolVersions() {
        final Path bundledTools = getBundledVersionsRoot();
        LOG.warn(" - getBundledToolVersions in " + bundledTools);

        final String error = checkDirectory(bundledTools, "Cannot get bundled SonarQube Scanner version");
        if (error != null) {
            LOG.warn(error);
            return Collections.emptyList();
        }

        final List<InstalledToolVersion> res = new ArrayList<>();
        try (final DirectoryStream<Path> contents = Files.newDirectoryStream(bundledTools)) {
            for (Path path : contents) {
                LOG.warn(" - getBundledToolVersions found package " + path);
                final GetPackageVersionResult result = tryParsePackedPackage(path);
                if (result.getToolVersion() != null) {
                    res.add(new SimpleInstalledToolVersion(result.getToolVersion(), null, null, path.toFile()));
                }
            }
        } catch (IOException e) {
            LOG.warnAndDebugDetails("Cannot get bundled SonarQube Scanner version due to an error", e);
        }

        return res;
    }

    @NotNull
    @Override
    public Collection<? extends ToolVersion> getAvailableToolVersions() {
        return super.getAvailableToolVersions();
    }

    @NotNull
    @Override
    public GetPackageVersionResult tryGetPackageVersion(@NotNull final File toolPackage) {
        final Matcher matcher = SONAR_QUBE_SCANNER_JAR_PATTERN.matcher(toolPackage.getName());

        if (matcher.matches()) {
            try (final FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + toolPackage.getAbsolutePath()), Collections.emptyMap())) {
                final Path sonarScannerMain = fs.getPath("/org/sonarsource/scanner/cli/Main.class");
                final String version = matcher.group("version");
                if (Files.exists(sonarScannerMain)) {
                    return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, matcher.group("version"), myToolType.getType() + "." + matcher.group("version") + "-" + SONAR_QUBE_SCANNER_TYPE));
                } else {
                    final Path sonarRunnerMain = fs.getPath("/org/sonar/runner/Main.class");
                    if (Files.exists(sonarRunnerMain)) {
                        return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, matcher.group("version"), myToolType.getType() + "." + version + "-" + SONAR_QUBE_RUNNER_TYPE));
                    } else {
                        return GetPackageVersionResult.error("Doesn't seem like SonarQube Scanner or SonarQube Runner: cannot find main class neither in 'org.sonarsource.scanner.cli' package neither in 'org.sonar.runner' packege");
                    }
                }
            } catch (IOException e) {
                LOG.warnAndDebugDetails("Cannot read zip archive in '" + toolPackage + "'", e);
                return GetPackageVersionResult.error("Should be single jar with. The name should contain version, eg: sonar-runner.2.4.jar or sonar-scanner-cli-3.0.3.jar");
            }
        }

        return GetPackageVersionResult.error("Should be single jar with. The name should contain version, eg: sonar-runner.2.4.jar or sonar-scanner-cli-3.0.3.jar");
    }

    @NotNull
    private GetPackageVersionResult tryParsePackedPackage(@NotNull final Path root) {
        final String error = checkFile(root, "Cannot parse SonarQube Scanner package");
        if (error != null) {
            LOG.warn(error);
            return GetPackageVersionResult.error(error);
        }

        final String fileName = root.getFileName().toString();

        final Matcher matcher = myPackedSonarQubeScannerRootZipPattern.matcher(fileName);
        if (matcher.matches()) {
            final String version = matcher.group(VERSION_GROUP_NAME);

            if (matcher.group(TYPE_GROUP_NAME).equals(SONAR_QUBE_SCANNER_TYPE)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, version, myToolType.getType() + "." + version + "-" + SONAR_QUBE_SCANNER_TYPE));
            } else {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, version, myToolType.getType() + "." + version + "-" + SONAR_QUBE_RUNNER_TYPE));
            }
        } else {
            LOG.warn("Unexpected package " + fileName + ", only " + myToolType.getType() + " and " + SONAR_QUBE_SCANNER_TYPE_SUFFIX + " with " + VERSION_GROUP_NAME + " suffix are allowed.");
            return GetPackageVersionResult.error("Unexpected package " + fileName + ", only " + myToolType.getType() + " and " + SONAR_QUBE_SCANNER_TYPE_SUFFIX + " are allowed.");
        }
    }

    @Override
    public void unpackToolPackage(@NotNull final File toolPackage, @NotNull final File targetDirectory) throws ToolException {
        final Path toolPath = toolPackage.toPath();
        {
            final String error = checkFile(toolPath, "Cannot unpack " + toolPackage);
            if (error != null) {
                LOG.warn(error);
                throw new ToolException(error);
            }
        }

        final Matcher matcher = SONAR_QUBE_SCANNER_JAR_PATTERN.matcher(toolPath.getFileName().toString());
        if (!matcher.matches()) {
            LOG.warn("Cannot unpack " + toolPackage + ": should be single jar file with version suffix: 'sonar-qube-scanner.3.0.3.778-scanner.jar'.");
            throw new ToolException("Cannot unpack " + toolPackage + ": should be single jar file with version suffix. Eg: 'sonar-qube-scanner.3.0.3.778-scanner.jar'.");
        }

        final Path targetPath = targetDirectory.toPath();
        {
            final String error = checkDirectory(targetPath, "Cannot unpack " + toolPackage + " to " + targetDirectory);
            if (error != null) {
                LOG.warn(error);
                throw new ToolException(error);
            }
        }

        final Matcher dirMatcher = myPackedSonarQubeScannerRootDirPattern.matcher(targetDirectory.getName());
        if (!dirMatcher.matches()) {
            throw new ToolException("Unexpected target directory name: should be 'sonar-qube-scanner.{version}.{type}' while got " + targetDirectory.getName());
        }

        final String type = dirMatcher.group(TYPE_GROUP_NAME);
        final String version = dirMatcher.group(VERSION_GROUP_NAME);

        final Path libDirectory;
        try {
            libDirectory = Files.createDirectories(targetPath.resolve("lib"));
        } catch (IOException e) {
            throw new ToolException("Cannot create directory for unpacked tool: '" + targetPath.resolve(type + "." + version) + "'", e);
        }
        final Path targetJarLocation = libDirectory.resolve(toolPath.getFileName());
        try {
            Files.copy(toolPath, targetJarLocation);
        } catch (IOException e) {
            throw new ToolException("Cannot copy jar to " + targetJarLocation);
        }
    }

    @Nullable
    @Override
    public String getDefaultBundledVersionId() {
        return myDefaultBundledVersionString;
    }

    @Nullable
    private String checkDirectory(@NotNull final Path bundledTools, final String description) {
        if (!Files.exists(bundledTools)) {
            return description + ": '" + bundledTools + "' expected to exist";
        }
        if (!Files.isReadable(bundledTools)) {
            return description + ": cannot read '" + bundledTools + "'";
        }
        if (!Files.isDirectory(bundledTools)) {
            return description + ": '" + bundledTools + "' is not a directory";
        }
        return null;
    }

    @Nullable
    private String checkFile(@NotNull final Path bundledTool, final String description) {
        if (!Files.exists(bundledTool)) {
            return description + ": '" + bundledTool + "' expected to exist";
        }
        if (!Files.isReadable(bundledTool)) {
            return description + ": cannot read '" + bundledTool + "'";
        }
        if (!Files.isRegularFile(bundledTool)) {
            return description + ": '" + bundledTool + "' is not a file";
        }
        return null;
    }

    @NotNull
    private Path getBundledVersionsRoot() {
        return myPluginDescriptor.getPluginRoot().toPath().resolve(BUNDLED_SUBPATH);
    }
}
