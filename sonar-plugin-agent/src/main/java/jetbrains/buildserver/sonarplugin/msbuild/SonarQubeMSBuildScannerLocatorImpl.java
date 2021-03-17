package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SonarQubeMSBuildScannerLocatorImpl implements SonarQubeMSBuildScannerLocator {

    @Override
    @Nullable
    public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) {
        final String explicitPath = runnerContext.getConfigParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
        return explicitPath != null ? explicitPath : runnerContext.getRunnerParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
    }
}
