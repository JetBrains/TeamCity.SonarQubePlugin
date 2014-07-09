package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.PropertiesBasedSQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.FileBasedSQSManagerImpl.single;

/**
 * Created by Andrey Titov on 4/4/14.
 *
 * Controller for SonarQube Server management page
 */
public class ServerManagementProjectTab extends EditProjectTab {

    @NotNull
    private final SQSManager mySqsManager;

    private static final String TAB_TITLE = "SonarQube Servers";

    public ServerManagementProjectTab(@NotNull final PagePlaces pagePlaces,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final SQSManager sqsManager) {
        super(pagePlaces, pluginDescriptor.getPluginName(), "manageSonarServers.jsp", TAB_TITLE);
        mySqsManager = sqsManager;
        addCssFile(pluginDescriptor.getPluginResourcesPath("manageSonarServers.css"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("manageSonarServers.js"));
    }

    @NotNull
    @Override
    public String getTabTitle(@NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return TAB_TITLE;
        }
        final List<PropertiesBasedSQSInfo> availableServers = mySqsManager.getAvailableServers(single(currentProject));
        if (availableServers.isEmpty()) {
            return TAB_TITLE;
        } else {
            return TAB_TITLE + " (" + availableServers.size() + ")";
        }
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        Map<SProject, List<PropertiesBasedSQSInfo>> infoMap = getServersMap(currentProject);
        model.put("availableServersMap", infoMap);
        model.put("projectId", currentProject.getExternalId());
    }

    private Map<SProject, List<PropertiesBasedSQSInfo>> getServersMap(@NotNull final SProject currentProject) {
        SProject project = currentProject;
        Map<SProject, List<PropertiesBasedSQSInfo>> infoMap = new HashMap<SProject, List<PropertiesBasedSQSInfo>>();
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
