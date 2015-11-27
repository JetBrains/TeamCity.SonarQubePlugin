package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.StatefulObject;
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm;
import jetbrains.buildServer.controllers.admin.projects.EditRunTypeControllerExtension;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.Util;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager.ProjectAccessor.recurse;

/**
 * Created by Andrey Titov on 5/29/14.
 *
 * Controller for the Edit SonarQube Runner page. Adds SonarQube Servers available for the current project.
 */
public class EditSQRRunType implements EditRunTypeControllerExtension {
    @NotNull
    private final SQSManager mySqsManager;

    public EditSQRRunType(@NotNull final SBuildServer server,
                          @NotNull final SQSManager mySqsManager) {
        this.mySqsManager = mySqsManager;
        server.registerExtension(EditRunTypeControllerExtension.class, Constants.RUNNER_TYPE, this);
    }

    @SuppressWarnings("unchecked")
    public void fillModel(@NotNull final HttpServletRequest request,
                          @NotNull final BuildTypeForm form,
                          @NotNull final Map model) {
        SProject project = form.getProject();
        final List<SQSInfo> availableServers = mySqsManager.getAvailableServers(recurse(project));
        model.put("servers", availableServers);
        final String sonarServer = getSonarServer(form);

        if (Util.isEmpty(sonarServer)) {
            model.put("showSelectServer", Boolean.TRUE);
        } else if (mySqsManager.findServer(recurse(form.getProject()), sonarServer) == null) {
            model.put("showUnknownServer", Boolean.TRUE);
        }
    }

    public void updateState(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {
        // do nothing
    }

    @Nullable
    public StatefulObject getState(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {
        return null;
    }

    @NotNull
    public ActionErrors validate(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {
        final ActionErrors errors = new ActionErrors();
        final String sonarServer = getSonarServer(form);
        if (Util.isEmpty(sonarServer)) {
            errors.addError("sonarServer", "SonarQube server should be set");
        } else {
            final SQSInfo server = mySqsManager.findServer(recurse(form.getProject()), sonarServer);
            if (server == null) {
                errors.addError("sonarServer", "This SonarQube server doesn't exist");
            }
        }
        return errors;
    }

    public void updateBuildType(@NotNull final HttpServletRequest request,
                                @NotNull final BuildTypeForm form,
                                @NotNull final BuildTypeSettings buildTypeSettings,
                                @NotNull final ActionErrors errors) {
        // do nothing
    }

    private String getSonarServer(@NotNull BuildTypeForm form) {
        return form.getBuildRunnerBean().getPropertiesBean().getProperties().get("sonarServer");
    }
}
