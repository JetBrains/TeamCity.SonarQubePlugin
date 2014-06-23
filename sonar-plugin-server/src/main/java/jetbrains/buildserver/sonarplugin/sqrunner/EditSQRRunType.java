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

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager.recurse;

/**
 * Created by linfar on 5/29/14.
 */
public class EditSQRRunType implements EditRunTypeControllerExtension {
    @NotNull
    private final SQSManager mySqsManager;

    public EditSQRRunType(final @NotNull SBuildServer server,
                          final @NotNull SQSManager mySqsManager) {
        this.mySqsManager = mySqsManager;
        server.registerExtension(EditRunTypeControllerExtension.class, Constants.RUNNER_TYPE, this);
    }

    public void fillModel(@NotNull HttpServletRequest request, @NotNull BuildTypeForm form, @NotNull Map model) {
        SProject project = form.getProject();
        final List<SQSInfo> availableServers = mySqsManager.getAvailableServers(recurse(project));
        model.put("servers", availableServers);
    }

    public void updateState(@NotNull HttpServletRequest request, @NotNull BuildTypeForm form) {
    }

    @Nullable
    public StatefulObject getState(@NotNull HttpServletRequest request, @NotNull BuildTypeForm form) {
        return null;
    }

    @NotNull
    public ActionErrors validate(@NotNull HttpServletRequest request, @NotNull BuildTypeForm form) {
        return new ActionErrors();
    }

    public void updateBuildType(@NotNull HttpServletRequest request, @NotNull BuildTypeForm form, @NotNull BuildTypeSettings buildTypeSettings, @NotNull ActionErrors errors) {
    }
}
