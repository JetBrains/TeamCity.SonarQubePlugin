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

import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildLog.MessageAttrs;
import jetbrains.buildServer.version.ServerVersionHolder;
import jetbrains.buildServer.version.ServerVersionInfo;

/**
 * Provides SONARQUBE_SCANNER_PARAMS environment variable on build if feature activated
 */
public class BranchesAndPullRequestsParametersPreprocessor implements ParametersPreprocessor {

    static final String SQS_SYSENV = "env.SONARQUBE_SCANNER_PARAMS";

    @Override
    public void fixRunBuildParameters(SRunningBuild build, Map<String, String> runParameters, Map<String, String> buildParams) {

        if (build.getBuildFeaturesOfType(BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_TYPE).isEmpty() || buildParams.containsKey(SQS_SYSENV)) {
            // Currently only GitHub is supported => if feature defined, it is for GitHub
            // ParametersPreprocessor is called for each steps (or could be defined manually). So if sysenv already defined, skip process.
            return;
        }

        if (!isTeamCityMinimalVersion(getServerVersionInfo())) {
            build.getBuildLog().message(
                    String.format("Build feature '%s' requiers TeamCity 2019.2 or above", BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_NAME),
                    Status.ERROR, MessageAttrs.attrs());
            return;
        }

        String branchIsDefault = buildParams.get("teamcity.build.branch.is_default");
        if (StringUtils.isEmpty(branchIsDefault) || Boolean.TRUE.equals(Boolean.valueOf(branchIsDefault))) {
            // No information or default branch, nothing to provide
            return;
        }

        final String type;
        final JsonObject json = new JsonObject();
        final String prNumber = buildParams.get("teamcity.pullRequest.number");
        if (StringUtils.isEmpty(prNumber)) {
            // Branch
            type = "branch";
            final String vcsBranch = buildParams.get("vcsroot.branch");
            json.addProperty("sonar.branch.name", buildParams.get("teamcity.build.branch"));
            json.addProperty("sonar.branch.target", vcsBranch.substring(vcsBranch.indexOf("refs/heads/") + 11));
        } else {
            // Pull Request
            type = "pull-request";
            String repo = buildParams.get("vcsroot.url");
            repo = repo.replaceFirst("^git@.*:", "");
            repo = repo.replaceFirst("^https?://[^/]*/", "");
            repo = repo.replaceFirst("\\.git$", "");

            json.addProperty("sonar.pullrequest.key", prNumber);
            json.addProperty("sonar.pullrequest.branch", buildParams.get("teamcity.pullRequest.title"));
            json.addProperty("sonar.pullrequest.base", buildParams.get("teamcity.pullRequest.target.branch"));
            json.addProperty("sonar.pullrequest.provider", "github");
            json.addProperty("sonar.pullrequest.github.repository", repo);
        }

        final String jsonString = json.toString();
        build.getBuildLog().message(String.format("SonarQube plugin detects %s, '%s' set with '%s'", type, SQS_SYSENV, jsonString), Status.NORMAL,
                MessageAttrs.attrs());
        buildParams.put(SQS_SYSENV, jsonString);
    }

    protected ServerVersionInfo getServerVersionInfo() {
        return ServerVersionHolder.getVersion();
    }

    /**
     * Check TeamCity minimal version
     * 
     * @param version Current version
     * @return true if version is 2019.2 or above
     */
    static boolean isTeamCityMinimalVersion(ServerVersionInfo version) {
        if (version == null) {
            // If absent, version considered as OK
            return true;
        }
        return version.getDisplayVersionMajor() > 2019 || (version.getDisplayVersionMajor() == 2019 && version.getDisplayVersionMinor() >= 2);
    }

}