/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildserver.sonarplugin.manager;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrey Titov on 4/4/14.
 *
 * Controller for SonarQube Server management page
 */
public class ServerManagementProjectTab extends EditProjectTab {

    @NotNull
    private final SQSManager mySqsManager;
    @NotNull
    private final SecurityContext securityContext;

    private static final String TAB_TITLE = "SonarQube Servers";

    public ServerManagementProjectTab(@NotNull final PagePlaces pagePlaces,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final SQSManager sqsManager,
                                      @NotNull final SecurityContext securityContext) {
        super(pagePlaces, pluginDescriptor.getPluginName(), "manageSonarServers.jsp", TAB_TITLE);
        mySqsManager = sqsManager;
        this.securityContext = securityContext;
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
        final List<SQSInfo> availableServers = mySqsManager.getOwnAvailableServers(currentProject);
        if (availableServers.isEmpty()) {
            return TAB_TITLE;
        }
        return TAB_TITLE + " (" + availableServers.size() + ")";
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        Map<SProject, List<SQSInfo>> infoMap = getServersMap(currentProject);
        model.put("availableServersMap", infoMap);
        model.put("projectId", currentProject.getExternalId());
        model.put("userHasPermissionManagement", AuthUtil.hasPermissionToManageProject(securityContext.getAuthorityHolder(), currentProject.getProjectId()));
    }

    private Map<SProject, List<SQSInfo>> getServersMap(@NotNull final SProject currentProject) {
        SProject project = currentProject;
        Map<SProject, List<SQSInfo>> infoMap = new HashMap<SProject, List<SQSInfo>>();
        while (project != null) {
            if (infoMap.containsKey(project)) {
                break;
            }
            final List<SQSInfo> availableServers = mySqsManager.getOwnAvailableServers(project);
            if (!availableServers.isEmpty()) {
                infoMap.put(project, availableServers);
            }
            project = project.getParentProject();
        }
        return infoMap;
    }
}
