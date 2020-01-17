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

package jetbrains.buildserver.sonarplugin.msbuild.tool;

import jetbrains.buildServer.tools.ToolTypeAdapter;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SonarQubeMSBuildToolType extends ToolTypeAdapter {
    @NotNull
    @Override
    public String getType() {
        return SQMSConstants.SONAR_QUBE_MSBUILD_TOOL_TYPE_ID;
    }

    @Nullable
    @Override
    public String getValidPackageDescription() {
        return "Specify path to the SonarScanner for MSBuild zip package, eg: sonar-scanner-msbuild.4.0.2.892.zip";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SonarScanner for MSBuild";
    }

    @NotNull
    @Override
    public String getShortDisplayName() {
        return "SonarScanner for MSBuild";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Tool to run SonarQube analysis during the build with MSBuild";
    }

    @Override
    public boolean isSupportDownload() {
        return false;
    }

    @Override
    public boolean isSupportUpload() {
        return true;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isServerOnly() {
        return false;
    }

    @Override
    public boolean isCountUsages() {
        return false;
    }
}
