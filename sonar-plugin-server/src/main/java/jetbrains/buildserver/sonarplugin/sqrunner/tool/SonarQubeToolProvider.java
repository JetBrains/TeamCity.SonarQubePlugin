package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.tools.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SonarQubeToolProvider extends ServerToolProviderAdapter {

    private static final Logger LOG = Logger.getInstance(SonarQubeToolProvider.class.getName());

    private static final String BUNDLED_SUBPATH = "bundled";

    private static final String SONAR_QUBE_RUNNER_PREFIX = "sonar-qube-runner";
    private static final String SONAR_QUBE_SCANNER_PREFIX = "sonar-qube-scanner";
    private static final String VERSION_PATTERN = "((\\d+.)*\\d+)";
    private static final Pattern SONAR_QUBE_SCANNER_ROOT_DIR_PATTERN = Pattern.compile("(" + SONAR_QUBE_RUNNER_PREFIX + "|" + SONAR_QUBE_SCANNER_PREFIX + ")-" + VERSION_PATTERN);

    @NotNull private final PluginDescriptor myPluginDescriptor;

    @NotNull private static final ToolTypeAdapter SQS_TOOL_TYPE = new ToolTypeAdapter() {
        @NotNull
        @Override
        public String getType() {
            return SonarQubeScannerConstants.SONAR_QUBE_SCANNER_TOOL_TYPE_ID;
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "SonarQube Scanner";
        }

        @NotNull
        @Override
        public String getShortDisplayName() {
            return "SonarQube Scanner";
        }

        @NotNull
        @Override
        public String getDescription() {
            return "Tool to run SonarQube analyzis during the build";
        }

        @Override
        public boolean isSupportDownload() {
            return false;
        }

        @Override
        public boolean isSupportUpload() {
            return false;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        @Override
        public boolean isServerOnly() {
            return false;
        }

        @Override
        public boolean isCountUsages() {
            return false;
        }
    };

    public SonarQubeToolProvider(@NotNull PluginDescriptor pluginDescriptor) {
        myPluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public ToolType getType() {
        return SQS_TOOL_TYPE;
    }

    @NotNull
    @Override
    public Collection<InstalledToolVersion> getBundledToolVersions() {
        final Path bundledTools = getBundledVersionsRoot();
        LOG.warn(" - getBundledToolVersions in " + bundledTools);

        if (!checkDirectory(bundledTools, "Cannot get bundled SonarQube Scanner version")) return Collections.emptyList();

        final List<InstalledToolVersion> res = new ArrayList<>();
        try (final DirectoryStream<Path> contents = Files.newDirectoryStream(bundledTools)) {
            for (Path path : contents) {
                LOG.warn(" - getBundledToolVersions found package " + path);
                final GetPackageVersionResult result = tryParsePackage(path);
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
    public GetPackageVersionResult tryGetPackageVersion(@NotNull File toolPackage) {
        return tryParsePackage(toolPackage.toPath());
    }

    @NotNull
    private GetPackageVersionResult tryParsePackage(@NotNull final Path root) {
        if (!checkDirectory(root, "Cannot parse SonarQube Scanner package")) return GetPackageVersionResult.error("Cannot parse SonarQube Scanner package");

        final String fileName = root.getFileName().toString();

        final Matcher matcher = SONAR_QUBE_SCANNER_ROOT_DIR_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            final MatchResult matchResult = matcher.toMatchResult();
            final String version = matchResult.group(2);

            if (matchResult.group(1).equals(SONAR_QUBE_RUNNER_PREFIX)) {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(SQS_TOOL_TYPE, version, SONAR_QUBE_RUNNER_PREFIX + "." + version));
            } else {
                return GetPackageVersionResult.version(new SonarQubeToolVersion(SQS_TOOL_TYPE, version, SONAR_QUBE_SCANNER_PREFIX + "." + version));
            }
        } else {
            LOG.warn("Unexpected package " + fileName + ", only " + SONAR_QUBE_SCANNER_PREFIX + " and " + SONAR_QUBE_RUNNER_PREFIX + " with version suffix are allowed.");
            return GetPackageVersionResult.error("Unexpected package " + fileName + ", only " + SONAR_QUBE_SCANNER_PREFIX + " and " + SONAR_QUBE_RUNNER_PREFIX + " are allowed.");
        }
    }

    @Override
    public void unpackToolPackage(@NotNull final File toolPackage, @NotNull final File targetDirectory) throws ToolException {
        final Path toolPath = toolPackage.toPath();
        if (!checkDirectory(toolPath, "Cannot unpack " + toolPackage)) {
            throw new ToolException("Cannot unpack " + toolPackage);
        }

        final Path targetPath = targetDirectory.toPath();
        if (!checkDirectory(targetPath, "Cannot unpack " + toolPackage + " to " + targetDirectory)) {
            throw new ToolException("Cannot unpack " + toolPackage + " to " + targetDirectory);
        }


    }

    @Nullable
    @Override
    public String getDefaultBundledVersionId() {
        return SONAR_QUBE_RUNNER_PREFIX + "-2.4";
    }

    private boolean checkDirectory(@NotNull final Path bundledTools, final String description) {
        if (!Files.exists(bundledTools)) {
            LOG.warn(description + ": '" + bundledTools + "' expected to exist");
            return false;
        }
        if (!Files.isReadable(bundledTools)) {
            LOG.warn(description + ": cannot read '" + bundledTools + "'");
            return false;
        }
        if (!Files.isDirectory(bundledTools)) {
            LOG.warn(description + ": '" + bundledTools + "' is not a directory");
            return false;
        }
        return true;
    }

    @NotNull
    private Path getBundledVersionsRoot() {
        return myPluginDescriptor.getPluginRoot().toPath().resolve(BUNDLED_SUBPATH);
    }
}
