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

package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import jetbrains.buildServer.tools.ToolTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SonarQubeScannerToolType extends ToolTypeAdapter {
    @Nullable
    @Override
    public String getValidPackageDescription() {
        return "Specify path to the SonarQube Scanner jar, eg: sonar-runner.2.4.jar, sonar-scanner-cli-3.0.3.jar";
    }

    @NotNull
    @Override
    public String getType() {
        return SonarQubeScannerConstants.SONAR_QUBE_SCANNER_TOOL_TYPE_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SonarQube Scanner";
    }

    @NotNull
    @Override
    public String getShortDisplayName() {
        return "SonarQube Scanner";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Tool to run SonarQube analyzis during the build";
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
