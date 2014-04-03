package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRBuildServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SonarQubeRunnerBuildRunner mySonarQubeRunnerBuildRunner;

    public SQRBuildServiceFactory(@NotNull final SonarQubeRunnerBuildRunner sonarQubeRunnerBuildRunner) {
        mySonarQubeRunnerBuildRunner = sonarQubeRunnerBuildRunner;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SQRBuildService();
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySonarQubeRunnerBuildRunner;
    }
}
