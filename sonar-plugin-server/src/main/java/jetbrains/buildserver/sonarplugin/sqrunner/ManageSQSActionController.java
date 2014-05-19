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
 * Created by linfar on 4/10/14.
 */
public class ManageSQSActionController extends BaseAjaxActionController implements ControllerAction {

    public static final String ADD_SQS_ACTION = "addSqs";
    public static final String REMOVE_SQS_ACTION = "removeSqs";
    public static final String SQS_ACTION = "sqsAction";
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
        return ADD_SQS_ACTION.equals(getAction(request)) ||
               REMOVE_SQS_ACTION.equals(getAction(request));
    }

    private static String getAction(HttpServletRequest request) {
        return request.getParameter("action");
    }

    public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element ajaxResponse) {
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
            } else if ("editSqs".equals(action)) {
                editServerInfo(request, response, project);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editServerInfo(HttpServletRequest request, HttpServletResponse response, SProject project) {
    }

    private void removeServerInfo(final @NotNull HttpServletRequest request, final @NotNull SProject project, final @NotNull Element ajaxResponse) throws IOException {
        try {
            final String serverinfoId = request.getParameter(SQSInfo.SERVERINFO_ID);
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

    private void addServerInfo(HttpServletRequest request, SProject project, Element ajaxResponse) throws IOException {
        final SQSInfo.ValidationError[] validationResult = SQSInfo.validate(request.getParameterMap());
        if (validationResult != null) {
            ajaxResponse.setAttribute("error", Integer.toString(validationResult.length));
        } else {
            final SQSInfo info = SQSInfo.from(request.getParameterMap());
            try {
                mySqsManager.addServer(info, project);
            } catch (IOException e) {
                ajaxResponse.setAttribute("error", "Cannot add server: " + e.getMessage());
            }
        }
    }

    @Nullable
    private SProject getProject(HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }
}
