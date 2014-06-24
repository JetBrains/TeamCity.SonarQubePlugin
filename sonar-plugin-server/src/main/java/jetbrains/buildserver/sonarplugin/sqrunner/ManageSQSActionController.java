package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
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
            e.printStackTrace();
        }
    }

    private void editServerInfo(final @NotNull HttpServletRequest request,
                                final @NotNull SProject project,
                                final @Nullable Element ajaxResponse) {
        try {
            mySqsManager.editServer(project, getServerInfoId(request), SQSInfo.from(request.getParameterMap()));
        } catch (IOException e) {
            addError(ajaxResponse, "error", "Cannot add server: " + e.getMessage());
        }
    }

    private void removeServerInfo(final @NotNull HttpServletRequest request,
                                  final @NotNull SProject project,
                                  final @Nullable Element ajaxResponse) throws IOException {
        try {
            final String serverinfoId = getServerInfoId(request);
            final boolean wasRemoved = mySqsManager.removeIfExists(project, serverinfoId);
            if (wasRemoved) {
                addError(ajaxResponse, "serverRemoved", serverinfoId + " was removed");
            } else {
                addError(ajaxResponse, "error", serverinfoId + " wasn't removed");
            }
        } catch (SQSManager.CannotDeleteData cannotDeleteData) {
            addError(ajaxResponse, "error", "Cannot delete data - " + cannotDeleteData.getMessage());
        }
    }

    private void addServerInfo(final @NotNull HttpServletRequest request,
                               final @NotNull SProject project,
                               final @Nullable Element ajaxResponse) throws IOException {
        final SQSInfo.ValidationError[] validationResult = SQSInfo.validate(request.getParameterMap());
        if (validationResult != null && validationResult.length > 0) {
            addError(ajaxResponse, "error", Integer.toString(validationResult.length));
        } else {
            final SQSInfo info = SQSInfo.from(request.getParameterMap());
            try {
                mySqsManager.addServer(info, project);
            } catch (IOException e) {
                addError(ajaxResponse, "error", "Cannot add server: " + e.getMessage());
            }
        }
    }

    @Nullable
    private SProject getProject(final @NotNull HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }

    private static String getServerInfoId(final @NotNull HttpServletRequest request) {
        return request.getParameter(SQSInfo.SERVERINFO_ID);
    }

    private static void addError(final @Nullable Element ajaxResponse,
                                 final @NotNull String key,
                                 final @NotNull String error) {
        if (ajaxResponse != null) {
            ajaxResponse.setAttribute(key, error);
        }
    }
}
