package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

public class BuildSummaryLinkController extends BaseController {
    private static final long ABSOLUTE_FILESIZE_THRESHOLD = 1000L;
    private static final String INCLUDE_URL = "/buildSummaryLink.html";
    @NotNull private final PluginDescriptor myPluginDescriptor;
    @NotNull private final SBuildServer myServer;

    private class BuildSummaryLinkExtension extends SimplePageExtension {
        public BuildSummaryLinkExtension(@NotNull PagePlaces places, @NotNull PlaceId placeId) {
            super(places, placeId, myPluginDescriptor.getPluginName(), INCLUDE_URL);
            addCssFile("buildSummary.css");
            register();
        }

        @Override
        public boolean isAvailable(@NotNull final HttpServletRequest request) {
            final SBuild build = retrieveBuild(request);
            if (build == null) {
                return false;
            }
            final BuildArtifactHolder artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY).findArtifact(Constants.SONAR_SERVER_URL_ARTIF_LOCATION_FULL);
            return artifact.isAvailable();
        }
    }

    public BuildSummaryLinkController(
            @NotNull WebControllerManager controllerManager,
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull PagePlaces places,
            @NotNull SBuildServer server
    ) {
        myPluginDescriptor = pluginDescriptor;
        myServer = server;

        new BuildSummaryLinkExtension(places, new PlaceId("SAKURA_BUILD_OVERVIEW"));
        new BuildSummaryLinkExtension(places, PlaceId.BUILD_SUMMARY);

        controllerManager.registerController(INCLUDE_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        final ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("buildSummary.jsp"));
        Map<String, Object> model = mv.getModel();
        model.put("is_sakura_ui", WebUtil.sakuraUIOpened(request));

        final SBuild build = retrieveBuild(request);
        if (build != null) {
            final BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY).getArtifact(Constants.SONAR_SERVER_URL_ARTIF_LOCATION_FULL);
            if (artifact == null) {
                model.put("sonar_noArtifact", Boolean.TRUE);
            } else {
                if (artifact.getSize() > ABSOLUTE_FILESIZE_THRESHOLD) {
                    model.put("sonar_bigUrlFile", artifact.getSize());
                } else {
                    try {
                        model.put("sonar_url", readUrl(artifact));
                    } catch (IOException e) {
                        model.put("sonar_IOException", e);
                    }
                }
            }
        }
        return mv;
    }

    @Nullable
    private SBuild retrieveBuild(@NotNull HttpServletRequest request) {
        SBuild build = null;
        if (WebUtil.sakuraUIOpened(request)) {
            PluginUIContext pluginUIContext = PluginUIContext.getFromRequest(request);
            Long buildId = pluginUIContext.getBuildId();
            if (buildId != null) {
                build = myServer.findBuildInstanceById(buildId);
            }
        } else {
            build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
        }
        return build;
    }

    @NotNull
    private static String readUrl(BuildArtifact artifact) throws IOException {
        String url;
        InputStream inputStream = null;
        try {
            inputStream = artifact.getInputStream();
            url = readUrlFromStream(inputStream);
        } finally {
            Util.close(inputStream);
        }
        return url;
    }

    @NotNull
    protected static String readUrlFromStream(@NotNull final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.readLine();
    }
}