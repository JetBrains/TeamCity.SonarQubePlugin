/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildserver.sonarplugin.buildfeatures;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

/**
 * Enable the support of SonarQube Branches (https://docs.sonarqube.org/latest/branches/overview/) and Pull-Requests
 * (https://docs.sonarqube.org/latest/analysis/pull-request/), by providing SONARQUBE_SCANNER_PARAMS environment variable
 */
public class BranchesAndPullRequestsBuildFeature extends BuildFeature {

    protected static final String BUILD_FEATURE_TYPE = "JetBrains.SonarQube.BranchesAndPullRequests.Support";
    protected static final String BUILD_FEATURE_NAME = "SonarQube Branches & Pull-Requests support";

    @NotNull
    private final PluginDescriptor myDescriptor;

    public BranchesAndPullRequestsBuildFeature(@NotNull final PluginDescriptor descriptor) {
        myDescriptor = descriptor;
    }

    @Override
    public String getType() {
        return BUILD_FEATURE_TYPE;
    }

    @Override
    public String getDisplayName() {
        return BUILD_FEATURE_NAME;
    }

    @Override
    public String getEditParametersUrl() {
        return myDescriptor.getPluginResourcesPath("editPrAndBranchSupportBuildFeature.jsp");
    }

    @Override
    public String describeParameters(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        result.append("Provider: ").append(params.get("provider"));
        return result.toString();
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @Override
    public boolean isRequiresAgent() {
        // SONARQUBE_SCANNER_PARAMS environment variable is filled before build start
        return false;
    }
}