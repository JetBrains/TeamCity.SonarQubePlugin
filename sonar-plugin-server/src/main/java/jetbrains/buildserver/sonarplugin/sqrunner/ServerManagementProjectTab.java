package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager.single;

/**
 * Created by Andrey Titov on 4/4/14.
 *
 * Controller for SonarQube Server management page
 */
public class ServerManagementProjectTab extends EditProjectTab {

    @NotNull
    private final SQSManager mySqsManager;

    public ServerManagementProjectTab(@NotNull final PagePlaces pagePlaces,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final SQSManager sqsManager) {
        super(pagePlaces, pluginDescriptor.getPluginName(), "manageSonarServers.jsp", "Sonar Server");
        mySqsManager = sqsManager;
        addCssFile(pluginDescriptor.getPluginResourcesPath("manageSonarServers.css"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("manageSonarServers.js"));
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        HashMap<SProject, List<SQSInfo>> infoMap = getServersMap(currentProject);
        model.put("availableServersMap", infoMap);
        model.put("projectId", currentProject.getExternalId());
    }

    private HashMap<SProject, List<SQSInfo>> getServersMap(@NotNull final SProject currentProject) {
        SProject project = currentProject;
        HashMap<SProject, List<SQSInfo>> infoMap = new HashMap<SProject, List<SQSInfo>>();
        while (project != null) {
            if (infoMap.containsKey(project)) {
                break;
            }
            infoMap.put(project, mySqsManager.getAvailableServers(single(project)));
            project = project.getParentProject();
        }
        return infoMap;
    }
}
