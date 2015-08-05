package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.controllers.PublicKeyUtil;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfoFactory;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
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

    public static final String SERVERINFO_ID = "serverinfo.id";
    public static final String SERVERINFO_NAME = "serverinfo.name";
    public static final String SONAR_URL = "sonar.host.url";
    public static final String SONAR_LOGIN = "sonar.login";
    public static final String SONAR_PASSWORD = "sonar.password";
    public static final String SONAR_PASSWORD_PRESERVE = "sonar.password_preserve";
    public static final String SONAR_JDBC_URL = "sonar.jdbc.url";
    public static final String SONAR_JDBC_USERNAME = "sonar.jdbc.username";
    public static final String SONAR_JDBC_PASSWORD = "sonar.jdbc.password";
    public static final String SONAR_JDBC_PASSWORD_PRESERVE = "sonar.jdbc.password_preserve";

    public static final String ADD_SQS_ACTION = "addSqs";
    public static final String REMOVE_SQS_ACTION = "removeSqs";
    public static final String EDIT_SQS_ACTION = "editSqs";
    public static final String SQS_ACTION = "action";
    @NotNull
    private final SQSManager mySqsManager;
    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final SecurityContext securityContext;

    public ManageSQSActionController(@NotNull final WebControllerManager controllerManager,
                                     @NotNull final SQSManager sqsManager,
                                     @NotNull final ProjectManager projectManager,
                                     @NotNull final SecurityContext securityContext) {
        super(controllerManager);
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

    private static String getAction(final @NotNull HttpServletRequest request) {
        return request.getParameter(SQS_ACTION);
    }

    public void process(final @NotNull HttpServletRequest request,
                        final @NotNull HttpServletResponse response,
                        final @Nullable Element ajaxResponse) {
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
            if (ADD_SQS_ACTION.equals(action)) {
                addServerInfo(request, project, ajaxResponse);
            } else if (REMOVE_SQS_ACTION.equals(action)) {
                removeServerInfo(request, project, ajaxResponse);
            } else if (EDIT_SQS_ACTION.equals(action)) {
                editServerInfo(request, project, ajaxResponse);
            }
        } catch (IOException e) {
            ajaxResponse.setAttribute("error", "Exception occurred: " + e.getMessage());
        }
    }

    private void editServerInfo(final @NotNull HttpServletRequest request,
                                final @NotNull SProject project,
                                final @NotNull Element ajaxResponse) {
        if (validate(request, ajaxResponse)) {
            final String serverInfoId = getServerInfoId(request);
            if (serverInfoId == null) {
                ajaxResponse.setAttribute("error", "ID is not set");
            } else {
                final SQSInfo old = mySqsManager.findServer(SQSManager.ProjectAccessor.recurse(project), serverInfoId);
                if (old != null) {
                    final String pass = Boolean.parseBoolean(request.getParameter(SONAR_PASSWORD_PRESERVE)) ? old.getPassword() : decryptIfNeeded(request.getParameter(SONAR_PASSWORD));
                    final String jdbcPass = Boolean.parseBoolean(request.getParameter(SONAR_JDBC_PASSWORD_PRESERVE)) ? old.getJDBCPassword() : decryptIfNeeded(request.getParameter(SONAR_JDBC_PASSWORD));
                    final SQSInfo info = SQSInfoFactory.createServerInfo(serverInfoId,
                            request.getParameter(SERVERINFO_NAME),
                            request.getParameter(SONAR_URL),
                            request.getParameter(SONAR_LOGIN),
                            pass,
                            request.getParameter(SONAR_JDBC_URL),
                            request.getParameter(SONAR_JDBC_USERNAME),
                            jdbcPass);
                    try {
                        mySqsManager.editServer(project, serverInfoId, info);
                        ajaxResponse.setAttribute("status", "OK");
                    } catch (IOException e) {
                        ajaxResponse.setAttribute("error", "Cannot add server: " + e.getMessage());
                    }
                }
            }
        }
    }

    private String decryptIfNeeded(final @Nullable String value) {
        return value != null ? RSACipher.decryptWebRequestData(value) : null;
    }

    private void removeServerInfo(final @NotNull HttpServletRequest request,
                                  final @NotNull SProject project,
                                  final @NotNull Element ajaxResponse) throws IOException {
        final String serverinfoId = getServerInfoId(request);
        if (serverinfoId == null) {
            ajaxResponse.setAttribute("error", "ID is not set");
        } else {
            try {
                final boolean wasRemoved = mySqsManager.removeIfExists(project, serverinfoId);
                if (wasRemoved) {
                    ajaxResponse.setAttribute("status", serverinfoId + " was removed");
                } else {
                    ajaxResponse.setAttribute("error", serverinfoId + " wasn't removed");
                }
            } catch (SQSManager.CannotDeleteData cannotDeleteData) {
                ajaxResponse.setAttribute("error", "Cannot delete data - " + cannotDeleteData.getMessage());
            }
        }
    }

    private void addServerInfo(final @NotNull HttpServletRequest request,
                               final @NotNull SProject project,
                               final @NotNull Element ajaxResponse) throws IOException {
        if (validate(request, ajaxResponse)) {
            final SQSInfo serverInfo = SQSInfoFactory.createServerInfo(null,
                    request.getParameter(SERVERINFO_NAME),
                    request.getParameter(SONAR_URL),
                    request.getParameter(SONAR_LOGIN),
                    request.getParameter(SONAR_PASSWORD),
                    request.getParameter(SONAR_JDBC_URL),
                    request.getParameter(SONAR_JDBC_USERNAME),
                    request.getParameter(SONAR_JDBC_PASSWORD));
            try {
                mySqsManager.addServer(project, serverInfo);
                ajaxResponse.setAttribute("status", "OK");
            } catch (SQSManager.ServerInfoExists e) {
                ajaxResponse.setAttribute("error", "Server with such name already exists");
            } catch (IOException e) {
                ajaxResponse.setAttribute("error", "Cannot add server: " + e.getMessage());
            }
        }
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
    private SProject getProject(final @NotNull HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }

    private static String getServerInfoId(final @NotNull HttpServletRequest request) {
        return request.getParameter(SERVERINFO_ID);
    }

}
