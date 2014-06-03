package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by linfar on 4/4/14.
 */
public class ServerManagementProjectTab extends EditProjectTab {

    @NotNull
    private final SQSManager mySqsManager;

    public ServerManagementProjectTab(@NotNull final PagePlaces pagePlaces, @NotNull final PluginDescriptor pluginDescriptor, @NotNull final SQSManager sqsManager) {
        super(pagePlaces, pluginDescriptor.getPluginName(), "manageSonarServers.jsp", "Sonar Server");
        mySqsManager = sqsManager;
        addCssFile(pluginDescriptor.getPluginResourcesPath("manageSonarServers.css"));
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        model.put("availableServers", mySqsManager.getAvailableServers(currentProject));
        model.put("projectId", currentProject.getExternalId());
    }
}
