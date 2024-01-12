

package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.util.OSType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andrey Titov on 4/3/14.
 *
 * Factory for SQRBuildService
 */
public class SQRBuildServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SonarQubeRunnerBuildRunner mySonarQubeRunnerBuildRunner;
    @NotNull private SonarProcessListener mySonarProcessListener;
    @NotNull private final OSType myOsType;
    @NotNull
    private final BuildAgentConfiguration myBuildAgentConfiguration;

    public SQRBuildServiceFactory(@NotNull final SonarQubeRunnerBuildRunner sonarQubeRunnerBuildRunner,
                                  @NotNull final SonarProcessListener sonarProcessListener,
                                  @NotNull final OSType osType,
                                  @NotNull final BuildAgentConfiguration buildAgentConfiguration) {
        mySonarQubeRunnerBuildRunner = sonarQubeRunnerBuildRunner;
        mySonarProcessListener = sonarProcessListener;
        myOsType = osType;
        myBuildAgentConfiguration = buildAgentConfiguration;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SQRBuildService(mySonarProcessListener, myOsType, myBuildAgentConfiguration);
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySonarQubeRunnerBuildRunner;
    }
}