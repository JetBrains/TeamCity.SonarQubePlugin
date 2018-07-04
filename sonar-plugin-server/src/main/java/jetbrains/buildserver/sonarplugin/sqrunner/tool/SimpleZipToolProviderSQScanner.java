package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.tool.SimpleZipToolProvider;
import jetbrains.buildserver.sonarplugin.tool.SonarQubeToolProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleZipToolProviderSQScanner implements SimpleZipToolProvider {
    private static final Logger LOG = Logger.getInstance(SimpleZipToolProviderSQScanner.class.getName());

    private static final String TYPE_GROUP_NAME = "type";

    private static final String SONAR_QUBE_SCANNER_TYPE = "scanner";
    private static final String SONAR_QUBE_RUNNER_TYPE = "runner";
    private static final String DEFAULT_BUNDLED_VERSION = "2.4";
    private static final String SONAR_QUBE_SCANNER_TYPE_SUFFIX = "-(?<" + TYPE_GROUP_NAME + ">" + SONAR_QUBE_RUNNER_TYPE + "|"+ SONAR_QUBE_SCANNER_TYPE + ")";
    private static final String VERSION_PATTERN = "(?<" + SonarQubeToolProvider.VERSION_GROUP_NAME + ">(\\d+.)*\\d+)";
    private static final String ZIP_EXTENSION = "\\.zip";
    private static final String JAR_EXTENSION = "\\.jar";

    @NotNull
    private final ToolType myToolType;
    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private final String myPackedSonarQubeScannerRootZipPattern;
    @NotNull private final String myPackedSonarQubeScannerRootDirPattern;

    public SimpleZipToolProviderSQScanner(@NotNull final PluginDescriptor pluginDescriptor, @NotNull final SonarQubeScannerToolType sonarQubeScannerToolType) {
        myPluginDescriptor = pluginDescriptor;
        myToolType = sonarQubeScannerToolType;
        myPackedSonarQubeScannerRootZipPattern = myToolType.getType() + "\\." + VERSION_PATTERN + SONAR_QUBE_SCANNER_TYPE_SUFFIX + ZIP_EXTENSION;
        myPackedSonarQubeScannerRootDirPattern = myToolType.getType() + "\\." + VERSION_PATTERN + SONAR_QUBE_SCANNER_TYPE_SUFFIX;
    }

    @NotNull
    @Override
    public Path getBundledVersionsRoot() {
        return myPluginDescriptor.getPluginRoot().toPath().resolve(SonarQubeToolProvider.BUNDLED_SUBPATH);
    }

    @NotNull
    @Override
    public String getName() {
        return "SonarQube Scanner";
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
        return ".*?[.-]" + VERSION_PATTERN + JAR_EXTENSION;
    }

    @NotNull
    @Override
    public GetPackageVersionResult parseVersion(@NotNull final Path toolPackage, final String version) throws Exception {
        try (final FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + toolPackage.toAbsolutePath()), Collections.emptyMap())) {
            final Path sonarScannerMain = fs.getPath("/org/sonarsource/scanner/cli/Main.class");
            if (Files.exists(sonarScannerMain)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(getToolType(), version, getToolType().getType() + "." + version + "-" + SONAR_QUBE_SCANNER_TYPE));
            } else {
                final Path sonarRunnerMain = fs.getPath("/org/sonar/runner/Main.class");
                if (Files.exists(sonarRunnerMain)) {
                    return GetPackageVersionResult.version(new SonarQubeToolVersion(getToolType(), version, getToolType().getType() + "." + version + "-" + SONAR_QUBE_RUNNER_TYPE));
                } else {
                    return GetPackageVersionResult.error("Doesn't seem like SonarQube Scanner or SonarQube Runner: cannot find main class neither in 'org.sonarsource.scanner.cli' package neither in 'org.sonar.runner' packege");
                }
            }
        }
    }

    @NotNull
    @Override
    public GetPackageVersionResult tryParsePackedPackage(@NotNull final Path path, @NotNull final Matcher matcher) {
        if (matcher.matches()) {
            final String version = matcher.group(SonarQubeToolProvider.VERSION_GROUP_NAME);

            if (matcher.group(TYPE_GROUP_NAME).equals(SONAR_QUBE_SCANNER_TYPE)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, version, myToolType.getType() + "." + version + "-" + SONAR_QUBE_SCANNER_TYPE));
            } else {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(myToolType, version, myToolType.getType() + "." + version + "-" + SONAR_QUBE_RUNNER_TYPE));
            }
        } else {
            LOG.warn("Unexpected package " + path.getFileName().toString() + ", only " + myToolType.getType() + " and " + SONAR_QUBE_SCANNER_TYPE_SUFFIX + " with " + SonarQubeToolProvider.VERSION_GROUP_NAME + " suffix are allowed.");
            return GetPackageVersionResult.error("Unexpected package " + path.getFileName().toString() + ", only " + myToolType.getType() + " and " + SONAR_QUBE_SCANNER_TYPE_SUFFIX + " are allowed.");
        }
    }

    @NotNull
    @Override
    public String getDefaultBundledVersion() {
        return myToolType.getType() + "." + DEFAULT_BUNDLED_VERSION + "-" + SONAR_QUBE_RUNNER_TYPE;
    }

    @NotNull
    @Override
    public GetPackageVersionResult describeBrokenPackage() {
        return GetPackageVersionResult.error("Should be single jar with. The name should contain version, eg: sonar-runner.2.4.jar or sonar-scanner-cli-3.0.3.jar");
    }

    @Override
    public void validatePackedTool(@NotNull final Path toolPackage) throws ToolException {
        final Matcher matcher = Pattern.compile(myPackedSonarQubeScannerRootZipPattern).matcher(toolPackage.getFileName().toString());
        if (!matcher.matches()) {
            LOG.warn("Cannot unpack " + toolPackage + ": should be single jar file with version suffix: 'sonar-qube-scanner.3.0.3.778-scanner.jar'.");
            throw new ToolException("Cannot unpack " + toolPackage + ": should be single jar file with version suffix. Eg: 'sonar-qube-scanner.3.0.3.778-scanner.jar'.");
        }
    }

    @Override
    public void layoutContents(@NotNull final Path toolPath, @NotNull final Path targetPath) throws ToolException {
        final Path libDirectory;
        try {
            libDirectory = Files.createDirectories(targetPath.resolve("lib"));
        } catch (IOException e) {
            throw new ToolException("Cannot create directory for unpacked tool: '" + targetPath.resolve("lib") + "'", e);
        }
        final Path targetJarLocation = libDirectory.resolve(toolPath.getFileName());
        try {
            Files.copy(toolPath, targetJarLocation);
        } catch (IOException e) {
            throw new ToolException("Cannot copy jar to " + targetJarLocation);
        }
    }
}
