package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class SonarQubeMSBuildScannerLocatorImpl implements SonarQubeMSBuildScannerLocator {
    private static final String BUNDLED_SQMS_DIR = "sonarscanner_for_msbuild";
    private static final String EXE_PATH = "MSBuild.SonarQube.Runner.exe";

    @NotNull private final PluginDescriptor myPluginDescriptor;

    public SonarQubeMSBuildScannerLocatorImpl(@NotNull final PluginDescriptor pluginDescriptor) {
        myPluginDescriptor = pluginDescriptor;
    }

    @Nullable
    public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) {
        final String explicitPath = runnerContext.getConfigParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
        return getExecutablePath(explicitPath != null ? explicitPath : getBundledPath());
    }

    private String getExecutablePath(final String homeDir) {
        return new File(homeDir, EXE_PATH).getAbsolutePath();
    }

    private String getBundledPath() {
        return new File(myPluginDescriptor.getPluginRoot(), BUNDLED_SQMS_DIR).getAbsolutePath();
    }
}
