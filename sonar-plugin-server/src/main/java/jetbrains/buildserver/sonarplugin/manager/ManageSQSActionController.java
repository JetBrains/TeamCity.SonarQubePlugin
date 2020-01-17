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

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.controllers.PublicKeyUtil;
import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSInfoFactory;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Andrery Titov on 4/10/14.
 *
 * Ajax controller for SonarQube Server management
 */
public class ManageSQSActionController extends BaseAjaxActionController implements ControllerAction {
    private static final String SERVERINFO_ID = "serverinfo.id";
    private static final String SERVERINFO_NAME = "serverinfo.name";
    private static final String SONAR_URL = "sonar.host.url";
    private static final String SONAR_LOGIN = "sonar.login";
    private static final String SONAR_PASSWORD = "sonar.password";
    private static final String SONAR_PASSWORD_PRESERVE = "sonar.password_preserve";
    private static final String SONAR_JDBC_URL = "sonar.jdbc.url";
    private static final String SONAR_JDBC_USERNAME = "sonar.jdbc.username";
    private static final String SONAR_JDBC_PASSWORD = "sonar.jdbc.password";
    private static final String SONAR_JDBC_PASSWORD_PRESERVE = "sonar.jdbc.password_preserve";

    private static final String ADD_SQS_ACTION = "addSqs";
    private static final String REMOVE_SQS_ACTION = "removeSqs";
    private static final String EDIT_SQS_ACTION = "editSqs";
    private static final String SQS_ACTION = "action";
    @NotNull
    private final SQSManager mySqsManager;
    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final SecurityContext securityContext;
    @NotNull
    private final SQSInfoFactory mySQSInfoFactory;
    @NotNull
    private final ConfigActionFactory myConfigActionFactory;

    public ManageSQSActionController(@NotNull final WebControllerManager controllerManager,
                                     @NotNull final SQSManager sqsManager,
                                     @NotNull final ProjectManager projectManager,
                                     @NotNull final SecurityContext securityContext,
                                     @NotNull final SQSInfoFactory sqsInfoFactory,
                                     @NotNull final ConfigActionFactory configActionFactory) {
        super(controllerManager);
        mySQSInfoFactory = sqsInfoFactory;
        myConfigActionFactory = configActionFactory;
        controllerManager.registerController("/admin/manageSonarServers.html", this);
        registerAction(this);

        mySqsManager = sqsManager;
        myProjectManager = projectManager;
        this.securityContext = securityContext;
    }

    public boolean canProcess(@NotNull final HttpServletRequest request) {
        final String action = getAction(request);
        return ADD_SQS_ACTION.equals(action) ||
               EDIT_SQS_ACTION.equals(action) ||
               REMOVE_SQS_ACTION.equals(action);
    }

    private static String getAction(@NotNull final HttpServletRequest request) {
        return request.getParameter(SQS_ACTION);
    }

    public void process(@NotNull final HttpServletRequest request,
                        @NotNull final HttpServletResponse response,
                        @Nullable final Element ajaxResponse) {
        final SProject project = getProject(request);
        if (ajaxResponse == null || project == null) {
            return;
        }

        // Security test (user without management permission could access this controller)
        if (!AuthUtil.hasPermissionToManageProject(securityContext.getAuthorityHolder(), project.getProjectId())){
            ajaxResponse.setAttribute("error", "User has not the management permission");
            return;
        }

        final String action = getAction(request);
        try {
            ConfigAction configAction = null;
            if (ADD_SQS_ACTION.equals(action)) {
                final SQSInfo sqsInfo = addServerInfo(request, project, ajaxResponse);
                if (sqsInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "SonarQube Server '" + sqsInfo.getName() + "' was added");
                }
            } else if (REMOVE_SQS_ACTION.equals(action)) {
                final SQSInfo sqsInfo = removeServerInfo(request, project, ajaxResponse);
                if (sqsInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "SonarQube Server '" + sqsInfo.getName() + "' was removed");
                }
            } else if (EDIT_SQS_ACTION.equals(action)) {
                final SQSInfo sqsInfo = editServerInfo(request, project, ajaxResponse);
                if (sqsInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "parameters of SonarQube Server '" + sqsInfo.getName() + "' were changed");
                }
            }
            if (configAction != null) {
                project.persist(configAction);
            }
        } catch (IOException e) {
            ajaxResponse.setAttribute("error", "Exception occurred: " + e.getMessage());
        }
    }

    private SQSInfo editServerInfo(@NotNull final HttpServletRequest request,
                                @NotNull final SProject project,
                                @NotNull final Element ajaxResponse) {
        if (!validate(request, ajaxResponse)) {
            return null;
        }

        final String serverInfoId = getServerInfoId(request);
        if (serverInfoId == null) {
            ajaxResponse.setAttribute("error", "ID is not set");
            return null;
        }

        final SQSInfo old = mySqsManager.getServer(project, serverInfoId);
        if (old == null) {
            return null;
        }

        final String pass = getPassword(request, old);
        final String jdbcPass = getJDBCPassword(request, old);
        final SQSInfo info = createServerInfo(request, serverInfoId, pass, jdbcPass);
        final SQSManager.SQSActionResult result = mySqsManager.editServer(project, info);
        if (!result.isError()) {
            ajaxResponse.setAttribute("status", "OK");
        } else {
            ajaxResponse.setAttribute("error", result.getReason());
        }
        return result.getAfterAction();
    }

    @Nullable
    private String getJDBCPassword(@NotNull final HttpServletRequest request, @NotNull final SQSInfo old) {
        return Boolean.parseBoolean(request.getParameter(SONAR_JDBC_PASSWORD_PRESERVE)) ? old.getJDBCPassword() : decryptIfNeeded(request.getParameter(SONAR_JDBC_PASSWORD));
    }

    @Nullable
    private String getPassword(@NotNull HttpServletRequest request, @NotNull final SQSInfo old) {
        return Boolean.parseBoolean(request.getParameter(SONAR_PASSWORD_PRESERVE)) ? old.getPassword() : decryptIfNeeded(request.getParameter(SONAR_PASSWORD));
    }

    @NotNull
    private SQSInfo createServerInfo(@NotNull HttpServletRequest request, String serverInfoId, String pass, String jdbcPass) {
        return mySQSInfoFactory.create(serverInfoId,
                StringUtil.nullIfEmpty(request.getParameter(SERVERINFO_NAME)),
                StringUtil.nullIfEmpty(request.getParameter(SONAR_URL)),
                StringUtil.nullIfEmpty(request.getParameter(SONAR_LOGIN)),
                StringUtil.nullIfEmpty(pass),
                StringUtil.nullIfEmpty(request.getParameter(SONAR_JDBC_URL)),
                StringUtil.nullIfEmpty(request.getParameter(SONAR_JDBC_USERNAME)),
                StringUtil.nullIfEmpty(jdbcPass));
    }

    private String decryptIfNeeded(@Nullable final String value) {
        return value != null ? RSACipher.decryptWebRequestData(value) : null;
    }

    private SQSInfo removeServerInfo(@NotNull final HttpServletRequest request,
                                  @NotNull final SProject project,
                                  @NotNull final Element ajaxResponse) throws IOException {
        final String serverinfoId = getServerInfoId(request);
        if (serverinfoId == null) {
            ajaxResponse.setAttribute("error", "ID is not set");
        } else {
            final SQSManager.SQSActionResult result = mySqsManager.removeServer(project, serverinfoId);
            if (!result.isError()) {
                ajaxResponse.setAttribute("status", result.getReason());
                return result.getBeforeAction();
            } else {
                ajaxResponse.setAttribute("error", result.getReason());
            }
    }
        return null;
    }

    private SQSInfo addServerInfo(@NotNull final HttpServletRequest request,
                                  @NotNull final SProject project,
                                  @NotNull final Element ajaxResponse) throws IOException {
        if (validate(request, ajaxResponse)) {
            final SQSInfo serverInfo = createServerInfo(request, null, decryptIfNeeded(request.getParameter(SONAR_PASSWORD)), decryptIfNeeded(request.getParameter(SONAR_JDBC_PASSWORD)));
            final SQSManager.SQSActionResult result = mySqsManager.addServer(project, serverInfo);
            if (!result.isError()) {
                ajaxResponse.setAttribute("status", "OK");
            } else {
                ajaxResponse.setAttribute("error", result.getReason());
            }
            return serverInfo;
        }
        return null;
    }

    private boolean validate(HttpServletRequest request, Element ajaxResponse) {
        if (request.getParameter("publicKey") != null && PublicKeyUtil.isPublicKeyExpired(request)) {
            PublicKeyUtil.writePublicKeyExpiredError(ajaxResponse);
            return false;
        }
        if (request.getParameter(SERVERINFO_NAME) == null) {
            ajaxResponse.setAttribute("error", "Server name should be set");
            return false;
        }
        if (request.getParameter(SONAR_URL) == null) {
            ajaxResponse.setAttribute("error", "Server url should be explicitly set");
            return false;
        }
        return true;
    }

    @Nullable
    private SProject getProject(@NotNull final HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }

    private static String getServerInfoId(@NotNull final HttpServletRequest request) {
        return request.getParameter(SERVERINFO_ID);
    }

}
