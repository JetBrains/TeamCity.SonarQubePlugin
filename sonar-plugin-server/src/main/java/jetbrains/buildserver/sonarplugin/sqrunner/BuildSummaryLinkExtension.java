package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.Constants;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by linfar on 4/4/14.
 *
 * Link to the SonarQube Server on the Build Summary table
 */
public class BuildSummaryLinkExtension extends SimplePageExtension {
    @NotNull
    private final SBuildServer myServer;

    public BuildSummaryLinkExtension(final @NotNull WebControllerManager manager,
                                     final @NotNull PluginDescriptor pluginDescriptor,
                                     @NotNull SBuildServer server) {
        super(manager, PlaceId.BUILD_SUMMARY, pluginDescriptor.getPluginName(), "buildSummary.jsp");
        myServer = server;
        register();
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
        if (build == null) {
            return false;
        }
        final BuildArtifactHolder artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY).findArtifact(Constants.SONAR_SERVER_URL_ARTIF_LOCATION_FULL);
        return artifact.isAvailable();
    }
}
