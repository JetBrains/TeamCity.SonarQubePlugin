package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.StatefulObject;
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm;
import jetbrains.buildServer.controllers.admin.projects.EditRunTypeControllerExtension;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.FileBasedSQSManagerImpl.recurse;

/**
 * Created by Andrey Titov on 5/29/14.
 *
 * Controller for the Edit SonarQube Runner page. Adds SonarQube Servers available for the current project.
 */
public class EditSQRRunType implements EditRunTypeControllerExtension {
    @NotNull
    private final SQSManager mySqsManager;

    public EditSQRRunType(final @NotNull SBuildServer server,
                          final @NotNull SQSManager mySqsManager) {
        this.mySqsManager = mySqsManager;
        server.registerExtension(EditRunTypeControllerExtension.class, Constants.RUNNER_TYPE, this);
    }

    @SuppressWarnings("unchecked")
    public void fillModel(final @NotNull HttpServletRequest request,
                          final @NotNull BuildTypeForm form,
                          final @NotNull Map model) {
        SProject project = form.getProject();
        final List<SQSInfo> availableServers = mySqsManager.getAvailableServers(recurse(project));
        model.put("servers", availableServers);
    }

    public void updateState(final @NotNull HttpServletRequest request, final @NotNull BuildTypeForm form) {
        // do nothing
    }

    @Nullable
    public StatefulObject getState(final @NotNull HttpServletRequest request, final @NotNull BuildTypeForm form) {
        return null;
    }

    final @NotNull
    public ActionErrors validate(final @NotNull HttpServletRequest request, final @NotNull BuildTypeForm form) {
        return new ActionErrors();
    }

    public void updateBuildType(final @NotNull HttpServletRequest request,
                                final @NotNull BuildTypeForm form,
                                final @NotNull BuildTypeSettings buildTypeSettings,
                                final @NotNull ActionErrors errors) {
        // do nothing
    }
}
