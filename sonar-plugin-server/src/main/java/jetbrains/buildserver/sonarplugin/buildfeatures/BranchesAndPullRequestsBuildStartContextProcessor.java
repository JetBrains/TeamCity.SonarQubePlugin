

package jetbrains.buildserver.sonarplugin.buildfeatures;

import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;

import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.serverSide.buildLog.MessageAttrs;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import jetbrains.buildServer.version.ServerVersionHolder;
import jetbrains.buildServer.version.ServerVersionInfo;

/**
 * Provides SONARQUBE_SCANNER_PARAMS environment variable on build if feature activated
 */
public class BranchesAndPullRequestsBuildStartContextProcessor implements BuildStartContextProcessor, PositionAware {

    static final String SQS_SYSENV = Constants.ENV_PREFIX + "SONARQUBE_SCANNER_PARAMS";

    @Override
    public PositionConstraint getConstraint() {
        // Technically "PositionConstraint.after("jetbrains.buildServer.pullRequests.impl.PullRequestParametersProcessor")"
        // last avoid any class name change
        return PositionConstraint.last();
    }

    @Override
    public String getOrderId() {
        return "SonarQubeBranchesPullRequestsSupport";
    }

    @Override
    public void updateParameters(BuildStartContext context) {

        if (context.getBuild().getBuildFeaturesOfType(BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_TYPE).isEmpty()
                || context.getSharedParameters().containsKey(SQS_SYSENV)) {
            // Currently only GitHub is supported => if feature defined, it is for GitHub
            // If sysenv already defined (outside this processor), skip process.
            return;
        }

        if (!isTeamCityMinimalVersion(getServerVersionInfo())) {
            context.getBuild().getBuildLog().message(
                    String.format("Build feature '%s' requires TeamCity 2019.2 or above", BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_NAME),
                    Status.ERROR, MessageAttrs.attrs());
            return;
        }

        String branchIsDefault = context.getSharedParameters().get("teamcity.build.branch.is_default");
        if (StringUtils.isEmpty(branchIsDefault) || Boolean.TRUE.equals(Boolean.valueOf(branchIsDefault))) {
            // No information or default branch, nothing to provide
            return;
        }

        final String type;
        final JsonObject json = new JsonObject();
        final String prNumber = context.getSharedParameters().get("teamcity.pullRequest.number");
        if (StringUtils.isEmpty(prNumber)) {
            // Branch
            type = "branch";
            final String vcsBranch = context.getSharedParameters().get("vcsroot.branch");
            json.addProperty("sonar.branch.name", context.getSharedParameters().get("teamcity.build.branch"));
            json.addProperty("sonar.branch.target", vcsBranch.substring(vcsBranch.indexOf("refs/heads/") + 11));
        } else {
            // Pull Request
            type = "pull-request";
            String repo = context.getSharedParameters().get("vcsroot.url");
            repo = repo.replaceFirst("^git@.*:", "");
            repo = repo.replaceFirst("^https?://[^/]*/", "");
            repo = repo.replaceFirst("\\.git$", "");

            json.addProperty("sonar.pullrequest.key", prNumber);
            json.addProperty("sonar.pullrequest.branch", context.getSharedParameters().get("teamcity.pullRequest.title"));
            json.addProperty("sonar.pullrequest.base", context.getSharedParameters().get("teamcity.pullRequest.target.branch"));
            json.addProperty("sonar.pullrequest.provider", "github");
            json.addProperty("sonar.pullrequest.github.repository", repo);
        }

        final String jsonString = json.toString();
        context.getBuild().getBuildLog().message(String.format("SonarQube plugin detects %s, '%s' set with '%s'", type, SQS_SYSENV, jsonString),
                Status.NORMAL, MessageAttrs.attrs());
        context.addSharedParameter(SQS_SYSENV, jsonString);
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