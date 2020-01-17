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
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import jetbrains.buildserver.sonarplugin.SQRParametersUtil;
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
                                        @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher) {
        mySQMSBuildStartRunner = sqmsBuildStartRunner;
        myOSType = osType;
        myMonoLocator = monoLocator;
        mySqmsBuildFinishServiceFactory = sqmsBuildFinishServiceFactory;
        mySonarQubeMSBuildScannerLocator = new SonarQubeMSBuildScannerLocatorImpl();

        dispatcher.addListener(new AgentLifeCycleAdapter() {
            @Override
            public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
                if (runner.getRunType().equals(mySQMSBuildStartRunner.getType())) {

                    mySqmsBuildFinishServiceFactory.setUpFinishStep(new SonarQubeMSBuildScannerLocator() {
                        @Nullable
                        @Override
                        public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException {
                            return mySonarQubeMSBuildScannerLocator.getExecutablePath(runner);
                        }
                    }, runner.getWorkingDirectory(), new SQRParametersAccessor(SQRParametersUtil.mergeAuthParameters(runner.getBuild().getSharedConfigParameters(), runner.getRunnerParameters())));
                }
            }
        });
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
                new SQMSBuildExecutableFactory(mySonarQubeMSBuildScannerLocator));
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
