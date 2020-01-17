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

package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
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

    public SQRBuildServiceFactory(@NotNull final SonarQubeRunnerBuildRunner sonarQubeRunnerBuildRunner,
                                  @NotNull final SonarProcessListener sonarProcessListener,
                                  @NotNull final OSType osType) {
        mySonarQubeRunnerBuildRunner = sonarQubeRunnerBuildRunner;
        mySonarProcessListener = sonarProcessListener;
        myOsType = osType;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new SQRBuildService(mySonarProcessListener, myOsType);
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySonarQubeRunnerBuildRunner;
    }
}
