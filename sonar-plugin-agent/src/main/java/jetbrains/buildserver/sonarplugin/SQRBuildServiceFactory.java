package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by linfar on 4/3/14.
 */
public class SQRBuildServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SonarQubeRunnerBuildRunner mySonarQubeRunnerBuildRunner;
    @NotNull private final PluginDescriptor myPluginDescriptor;

    public SQRBuildServiceFactory(@NotNull final SonarQubeRunnerBuildRunner sonarQubeRunnerBuildRunner,
                                  @NotNull final PluginDescriptor pluginDescriptor) {
        mySonarQubeRunnerBuildRunner = sonarQubeRunnerBuildRunner;
        myPluginDescriptor = pluginDescriptor;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SQRBuildService(myPluginDescriptor);
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySonarQubeRunnerBuildRunner;
    }
}
