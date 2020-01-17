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

import org.jetbrains.annotations.NotNull;

public final class SonarQubeScannerConstants {
    @NotNull public static final String SONAR_QUBE_SCANNER_TOOL_TYPE_ID = "sonar-qube-scanner";
    @NotNull public static final String SONAR_QUBE_SCANNER_VERSION_PARAMETER = "teamcity.tool.sonarquberunner";

    public SonarQubeScannerConstants() {
        // jsp compatibility
    }

    @NotNull
    public String getToolTypeId() {
        return SONAR_QUBE_SCANNER_TOOL_TYPE_ID;
    }

    @NotNull
    public String getSonarQubeScannerVersionParameter() {
        return SONAR_QUBE_SCANNER_VERSION_PARAMETER;
    }
}
