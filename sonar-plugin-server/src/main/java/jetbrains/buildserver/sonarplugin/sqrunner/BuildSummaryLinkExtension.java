package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.Util;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by Andrey Titov on 4/4/14.
 * <p>
 * Link to the SonarQube Server on the Build Summary table
 * </p>
 */
public class BuildSummaryLinkExtension extends SimplePageExtension {
    public static final long ABSOLUTE_FILESIZE_THRESHOLD = 1000L;
    @NotNull
    private final SBuildServer myServer;

    public BuildSummaryLinkExtension(final @NotNull WebControllerManager manager,
                                     final @NotNull PluginDescriptor pluginDescriptor,
                                     final @NotNull SBuildServer server) {
        super(manager, PlaceId.BUILD_SUMMARY, pluginDescriptor.getPluginName(), "buildSummary.jsp");
        myServer = server;
        register();
    }

    @Override
    public void fillModel(final @NotNull Map<String, Object> model,
                          final @NotNull HttpServletRequest request) {
        final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
        if (build != null) {
            final BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY).getArtifact(Constants.SONAR_SERVER_URL_ARTIF_LOCATION_FULL);
            if (artifact != null) {
                if (artifact.getSize() > ABSOLUTE_FILESIZE_THRESHOLD) {
                    model.put("sonar_bigUrlFile", artifact.getSize());
                } else {
                    InputStream inputStream = null;
                    try {
                        inputStream = artifact.getInputStream();
                        String url = readUrlFromStream(inputStream);
                        model.put("sonar_url", url);
                    } catch (IOException e) {
                        model.put("sonar_IOException", e);
                    } finally {
                        Util.close(inputStream);
                    }
                }
            } else {
                model.put("sonar_noArtifact", Boolean.TRUE);
            }
        }
        super.fillModel(model, request);
    }

    @NotNull
    protected String readUrlFromStream(@NotNull final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.readLine();
    }

    @Override
    public boolean isAvailable(@NotNull final HttpServletRequest request) {
        final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
        if (build == null) {
            return false;
        }
        final BuildArtifactHolder artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY).findArtifact(Constants.SONAR_SERVER_URL_ARTIF_LOCATION_FULL);
        return artifact.isAvailable();
    }
}
