package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.PropertiesBasedSQSInfo;
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

    public static final String ADD_SQS_ACTION = "addSqs";
    public static final String REMOVE_SQS_ACTION = "removeSqs";
    public static final String EDIT_SQS_ACTION = "editSqs";
    public static final String SQS_ACTION = "action";
    @NotNull
    private final SQSManager mySqsManager;
    @NotNull
    private final ProjectManager myProjectManager;

    public ManageSQSActionController(@NotNull final WebControllerManager controllerManager,
                                     @NotNull final SQSManager sqsManager,
                                     @NotNull final ProjectManager projectManager) {
        super(controllerManager);
        controllerManager.registerController("/admin/manageSonarServers.html", this);
        registerAction(this);

        mySqsManager = sqsManager;
        myProjectManager = projectManager;
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
        if (ajaxResponse == null) {
            return;
        }

        final SProject project = getProject(request);
        if (project == null) {
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
        try {
            mySqsManager.editServer(project, getServerInfoId(request), PropertiesBasedSQSInfo.from(request.getParameterMap()));
        } catch (IOException e) {
            ajaxResponse.setAttribute("error", "Cannot add server: " + e.getMessage());
        }
    }

    private void removeServerInfo(final @NotNull HttpServletRequest request,
                                  final @NotNull SProject project,
                                  final @NotNull Element ajaxResponse) throws IOException {
        try {
            final String serverinfoId = getServerInfoId(request);
            final boolean wasRemoved = mySqsManager.removeIfExists(project, serverinfoId);
            if (wasRemoved) {
                ajaxResponse.setAttribute("serverRemoved", serverinfoId + " was removed");
            } else {
                ajaxResponse.setAttribute("error", serverinfoId + " wasn't removed");
            }
        } catch (SQSManager.CannotDeleteData cannotDeleteData) {
            ajaxResponse.setAttribute("error", "Cannot delete data - " + cannotDeleteData.getMessage());
        }
    }

    private void addServerInfo(final @NotNull HttpServletRequest request,
                               final @NotNull SProject project,
                               final @NotNull Element ajaxResponse) throws IOException {
        final PropertiesBasedSQSInfo.ValidationError[] validationResult = PropertiesBasedSQSInfo.validate(request.getParameterMap());
        if (validationResult.length > 0) {
            ajaxResponse.setAttribute("error", Integer.toString(validationResult.length));
        } else {
            final PropertiesBasedSQSInfo info = PropertiesBasedSQSInfo.from(request.getParameterMap());
            try {
                mySqsManager.addServer(project, info);
            } catch (SQSManager.ServerInfoExists e) {
                ajaxResponse.setAttribute("error", "Server with such name already exists");
            } catch (IOException e) {
                ajaxResponse.setAttribute("error", "Cannot add server: " + e.getMessage());
            }
        }
    }

    @Nullable
    private SProject getProject(final @NotNull HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }

    private static String getServerInfoId(final @NotNull HttpServletRequest request) {
        return request.getParameter(PropertiesBasedSQSInfo.SERVERINFO_ID);
    }

}
