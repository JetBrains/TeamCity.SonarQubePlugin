package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;

public class SQMSBuildStartRunner implements AgentBuildRunnerInfo {
    @NotNull
    @Override
    public String getType() {
        return SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_ID;
    }

    @Override
    public boolean canRun(@NotNull final BuildAgentConfiguration buildAgentConfiguration) {
        return true;
    }
}
