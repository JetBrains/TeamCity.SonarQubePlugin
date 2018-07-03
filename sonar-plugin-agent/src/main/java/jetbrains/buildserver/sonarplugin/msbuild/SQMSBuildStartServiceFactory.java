package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import jetbrains.buildserver.sonarplugin.util.ExecutionChain;
import jetbrains.buildserver.sonarplugin.util.SimpleExecute;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class SQMSBuildStartServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SQMSBuildStartRunner mySQMSBuildStartRunner;
    @NotNull private final OSType myOSType;
    @NotNull private final MonoLocator myMonoLocator;

    public SQMSBuildStartServiceFactory(@NotNull final SQMSBuildStartRunner sqmsBuildStartRunner,
                                        @NotNull final OSType osType,
                                        @NotNull final MonoLocator monoLocator) {
        mySQMSBuildStartRunner = sqmsBuildStartRunner;
        myOSType = osType;
        myMonoLocator = monoLocator;
    }

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new SimpleExecute(
                new ExecutionChain(
                        Arrays.asList(
                                new SonarQubeArgumentsWrapper(new SQScannerArgsComposer(myOSType)),
                                new MonoWrapper(myOSType, myMonoLocator),
                                new BeginExecutionWrapper())),
                new SQMSBuildExecutableFactory(new SonarQubeMSBuildScannerLocatorImpl()));
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySQMSBuildStartRunner;
    }

    private static class BeginExecutionWrapper implements Execution {
        @NotNull
        @Override
        public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
            final ArrayList<String> arguments = new ArrayList<String>(old.myArguments);
            arguments.add("begin");
            return new Executable(old.myExecutable, arguments);
        }
    }
}
