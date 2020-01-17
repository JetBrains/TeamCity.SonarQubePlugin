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

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SonarQubeMSBuildScannerLocatorImpl implements SonarQubeMSBuildScannerLocator {

    @Override
    @Nullable
    public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) {
        final String explicitPath = runnerContext.getConfigParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
        return explicitPath != null ? explicitPath : runnerContext.getRunnerParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
    }
}
