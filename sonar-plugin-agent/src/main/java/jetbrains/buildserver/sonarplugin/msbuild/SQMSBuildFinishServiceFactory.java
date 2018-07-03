package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import jetbrains.buildserver.sonarplugin.util.ExecutionChain;
import jetbrains.buildserver.sonarplugin.util.SimpleExecute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class SQMSBuildFinishServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SQMSBuildFinishRunner mySQMSBuildFinishRunner;
    @NotNull private final OSType myOSType;
    @NotNull private final MonoLocator myMonoLocator;
    @NotNull
    private final CurrentBuildTracker myCurrentBuildTracker;
    @Nullable
    private volatile SonarQubeMSBuildScannerLocator myMSBuildScannerLocator;

    public SQMSBuildFinishServiceFactory(@NotNull final SQMSBuildFinishRunner sqmsBuildStartRunner,
                                         @NotNull final OSType osType,
                                         @NotNull final MonoLocator monoLocator,
                                         @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher, @NotNull final CurrentBuildTracker currentBuildTracker) {
        mySQMSBuildFinishRunner = sqmsBuildStartRunner;
        myOSType = osType;
        myMonoLocator = monoLocator;
        myCurrentBuildTracker = currentBuildTracker;
        dispatcher.addListener(new AgentLifeCycleAdapter() {
            @Override
            public void buildStarted(@NotNull final AgentRunningBuild runningBuild) {
                myMSBuildScannerLocator = null;
            }

            @Override
            public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
                if (runner.getRunType().equals(mySQMSBuildFinishRunner.getType())) {
                    myMSBuildScannerLocator = null;
                }
            }
        });
    }

    @NotNull
    public CommandLineBuildService createService() {
        SonarQubeMSBuildScannerLocator msBuildScannerLocator = myMSBuildScannerLocator;
        if (msBuildScannerLocator == null) {
            return new CommandLineBuildService() {
                @NotNull
                @Override
                public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
                    throw new RunBuildException("SonarQube MSBuild Scanner: begin analysis runner was not triggered yet");
                }
            };
        }
        return new SimpleExecute(
                new ExecutionChain(Arrays.asList(new MonoWrapper(myOSType, myMonoLocator), new EndExecution())),
                new SQMSBuildExecutableFactory(msBuildScannerLocator));
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySQMSBuildFinishRunner;
    }

    public void setMSBuildScannerLocator(final SonarQubeMSBuildScannerLocator sonarQubeMSBuildScannerLocator) {
        myMSBuildScannerLocator = sonarQubeMSBuildScannerLocator;
    }

    private static class EndExecution implements Execution {
        @NotNull
        public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
            final ArrayList<String> arguments = new ArrayList<String>(old.myArguments);
            arguments.add("end");
            return new Executable(old.myExecutable, arguments);
        }
    }
}
