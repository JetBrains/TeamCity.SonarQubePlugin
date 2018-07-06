package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import jetbrains.buildserver.sonarplugin.util.ExecutionChain;
import jetbrains.buildserver.sonarplugin.util.SimpleExecute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class SQMSBuildStartServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SQMSBuildStartRunner mySQMSBuildStartRunner;
    @NotNull private final OSType myOSType;
    @NotNull private final MonoLocator myMonoLocator;
    @NotNull
    private final SQMSBuildFinishServiceFactory mySqmsBuildFinishServiceFactory;
    private final SonarQubeMSBuildScannerLocator mySonarQubeMSBuildScannerLocator;

    public SQMSBuildStartServiceFactory(@NotNull final SQMSBuildStartRunner sqmsBuildStartRunner,
                                        @NotNull final OSType osType,
                                        @NotNull final MonoLocator monoLocator,
                                        @NotNull final SQMSBuildFinishServiceFactory sqmsBuildFinishServiceFactory,
                                        @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                                        @NotNull final PluginDescriptor pluginDescriptor) {
        mySQMSBuildStartRunner = sqmsBuildStartRunner;
        myOSType = osType;
        myMonoLocator = monoLocator;
        mySqmsBuildFinishServiceFactory = sqmsBuildFinishServiceFactory;
        mySonarQubeMSBuildScannerLocator = new SonarQubeMSBuildScannerLocatorImpl(pluginDescriptor);

        dispatcher.addListener(new AgentLifeCycleAdapter() {
            @Override
            public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
                if (runner.getRunType().equals(mySQMSBuildStartRunner.getType())) {
                    mySqmsBuildFinishServiceFactory.setMSBuildScannerLocator(new SonarQubeMSBuildScannerLocator() {
                        @Nullable
                        public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException {
                            return mySonarQubeMSBuildScannerLocator.getExecutablePath(runner);
                        }
                    });
                }
            }
        });
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SimpleExecute(
                new ExecutionChain(
                        Arrays.asList(
                                new SonarQubeArgumentsWrapper(new SQScannerArgsComposer(myOSType)),
                                new MonoWrapper(myOSType, myMonoLocator),
                                new BeginExecutionWrapper())),
                new SQMSBuildExecutableFactory(mySonarQubeMSBuildScannerLocator));
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySQMSBuildStartRunner;
    }

    private static class BeginExecutionWrapper implements Execution {
        @NotNull
        public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
            final ArrayList<String> arguments = new ArrayList<String>(old.myArguments);
            arguments.add("begin");
            return new Executable(old.myExecutable, arguments);
        }
    }
}
