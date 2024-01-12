

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