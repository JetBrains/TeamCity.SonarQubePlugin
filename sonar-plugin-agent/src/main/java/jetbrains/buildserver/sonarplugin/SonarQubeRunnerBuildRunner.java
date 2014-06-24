package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andrey Titov on 4/2/14.
 *
 * Build runner for SonarQube Runner - a CMD utility collecting data for SonarQube
 */
public class SonarQubeRunnerBuildRunner implements AgentBuildRunnerInfo {

    @NotNull
    public String getType() {
        return Constants.RUNNER_TYPE;
    }

    public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
        return true;
    }
}
