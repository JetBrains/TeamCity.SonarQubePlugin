package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andrey Titov on 4/3/14.
 *
 * Factory for SQRBuildService
 */
public class SQRBuildServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SonarQubeRunnerBuildRunner mySonarQubeRunnerBuildRunner;
    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private SonarProcessListener mySonarProcessListener;

    public SQRBuildServiceFactory(@NotNull final SonarQubeRunnerBuildRunner sonarQubeRunnerBuildRunner,
                                  @NotNull final PluginDescriptor pluginDescriptor,
                                  @NotNull final SonarProcessListener sonarProcessListener) {
        mySonarQubeRunnerBuildRunner = sonarQubeRunnerBuildRunner;
        myPluginDescriptor = pluginDescriptor;
        mySonarProcessListener = sonarProcessListener;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SQRBuildService(myPluginDescriptor, mySonarProcessListener);
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySonarQubeRunnerBuildRunner;
    }
}
