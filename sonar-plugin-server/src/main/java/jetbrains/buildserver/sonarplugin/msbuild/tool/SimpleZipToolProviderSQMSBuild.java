package jetbrains.buildserver.sonarplugin.msbuild.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeToolVersion;
import jetbrains.buildserver.sonarplugin.tool.SimpleZipToolProvider;
import jetbrains.buildserver.sonarplugin.tool.SonarQubeToolProvider;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;

public class SimpleZipToolProviderSQMSBuild implements SimpleZipToolProvider {
    private static final Logger LOG = Logger.getInstance(SimpleZipToolProviderSQMSBuild.class.getName());

    private static final String DEFAULT_BUNDLED_VERSION = "3.0.3.778";
    private static final String VERSION_PATTERN = "(?<" + SonarQubeToolProvider.VERSION_GROUP_NAME + ">(\\d+.)*\\d+)";
    private static final String ZIP_EXTENSION = "\\.zip";

    @NotNull
    private final ToolType myToolType;
    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private final String myPackedSonarQubeScannerRootZipPattern;
    @NotNull private final String myPackedSonarQubeScannerRootDirPattern;

    public SimpleZipToolProviderSQMSBuild(@NotNull final PluginDescriptor pluginDescriptor,
                                          @NotNull final SonarQubeMSBuildToolType sonarQubeScannerToolType) {
        myPluginDescriptor = pluginDescriptor;
        myToolType = sonarQubeScannerToolType;
        myPackedSonarQubeScannerRootZipPattern = myToolType.getType() + "\\." + VERSION_PATTERN + ZIP_EXTENSION;
        myPackedSonarQubeScannerRootDirPattern = myToolType.getType() + "\\." + VERSION_PATTERN;
    }

    @NotNull
    @Override
    public Path getBundledVersionsRoot() {
        return myPluginDescriptor.getPluginRoot().toPath().resolve(SonarQubeToolProvider.BUNDLED_SUBPATH);
    }

    @NotNull
    @Override
    public String getName() {
        return "SonarQube MSBuild Scanner";
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
        try (final FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + toolPackage.toAbsolutePath()), Collections.emptyMap())) {
            final Path executable = fs.getPath("/SonarQube.Scanner.MSBuild.exe");
            if (Files.exists(executable) && Files.isRegularFile(executable)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(getToolType(), version, getToolType().getType() + "." + version));
            } else {
                return GetPackageVersionResult.error("Doesn't seem like SonarQube MSBuild Scanner: cannot find 'MSBuild.SonarQube.Runner.exe'");
            }
        }
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
}
