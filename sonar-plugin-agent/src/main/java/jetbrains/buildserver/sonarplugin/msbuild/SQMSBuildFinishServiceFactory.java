/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import jetbrains.buildserver.sonarplugin.util.ExecutionChain;
import jetbrains.buildserver.sonarplugin.util.SimpleExecute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
    @Nullable
    private volatile File myWorkingDirectory;
    @Nullable
    private volatile SQRParametersAccessor mySqrParametersAccessor;

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
                myWorkingDirectory = null;
                mySqrParametersAccessor = null;
            }

            @Override
            public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
                if (runner.getRunType().equals(mySQMSBuildFinishRunner.getType())) {
                    myMSBuildScannerLocator = null;
                    myWorkingDirectory = null;
                    mySqrParametersAccessor = null;
                }
            }
        });
    }

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        final SonarQubeMSBuildScannerLocator msBuildScannerLocator = myMSBuildScannerLocator;
        final File workingDirectory = myWorkingDirectory;
        final SQRParametersAccessor sqrParametersAccessor = mySqrParametersAccessor;
        if (msBuildScannerLocator == null || workingDirectory == null || sqrParametersAccessor == null) {
            return new CommandLineBuildService() {
                @NotNull
                @Override
                public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
                    throw new RunBuildException("SonarScanner for MSBuild: begin analysis runner was not triggered yet");
                }
            };
        }

        return new SimpleExecute(
                new ExecutionChain(Arrays.asList(
                        new SonarQubeArgumentsWrapper(new SQScannerArgsComposer(myOSType), new SQRParametersAccessorFactory() {
                            public SQRParametersAccessor createAccessor(@NotNull final BuildRunnerContext runnerContext) {
                                return sqrParametersAccessor;
                            }
                        }),
                        new MonoWrapper(myOSType, myMonoLocator),
                        new EndExecution())),
                new SQMSBuildExecutableFactory(msBuildScannerLocator), workingDirectory.getAbsolutePath());
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySQMSBuildFinishRunner;
    }

    public void setUpFinishStep(@NotNull final SonarQubeMSBuildScannerLocator sonarQubeMSBuildScannerLocator,
                                @NotNull final File workingDirectory,
                                @NotNull final SQRParametersAccessor sqrParametersAccessor) {
        myMSBuildScannerLocator = sonarQubeMSBuildScannerLocator;
        myWorkingDirectory = workingDirectory;
        mySqrParametersAccessor = sqrParametersAccessor;
    }

    private static class EndExecution implements Execution {
        @NotNull
        @Override
        public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
            final ArrayList<String> arguments = new ArrayList<String>(old.myArguments);
            arguments.add("end");
            return new Executable(old.myExecutable, arguments);
        }
    }
}
