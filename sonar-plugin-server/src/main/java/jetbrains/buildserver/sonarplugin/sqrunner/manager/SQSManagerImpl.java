package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * SonarQube Server Manager based on ProjectSettingsManager
 */
public class SQSManagerImpl implements SQSManager, ProjectSettingsFactory {
    public static final String SQS_MANAGER_KEY = "sonar-qube";

    @NotNull
    private final ProjectSettingsManager mySettingsManager;

    public SQSManagerImpl(@NotNull final ProjectSettingsManager settingsManager) {
        mySettingsManager = settingsManager;
        mySettingsManager.registerSettingsFactory(SQS_MANAGER_KEY, this);
    }

    @NotNull
    public List<SQSInfo> getAvailableServers(@NotNull final ProjectAccessor accessor) {
        SProject project;
        List<SQSInfo> res = new LinkedList<SQSInfo>();
        while ((project = accessor.next()) != null) {
            res.addAll(getSettings(project).getAll());
        }
        return res;
    }

    @Nullable
    public synchronized SQSInfo findServer(@NotNull final ProjectAccessor accessor, @NotNull final String serverId) {
        SProject project;
        while ((project = accessor.next()) != null) {
            final SQSInfo info = getSettings(project).getInfo(serverId);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    public synchronized void editServer(@NotNull final SProject project,
                                        @NotNull final String serverId,
                                        @NotNull final SQSInfo modifiedServer) throws IOException {
        getSettings(project).setInfo(serverId, modifiedServer);
        project.persist();
    }

    public synchronized void addServer(@NotNull final SProject project,
                                       @NotNull final SQSInfo serverInfo) throws IOException {
        getSettings(project).setInfo(serverInfo.getId(), serverInfo);
        project.persist();
    }

    public boolean removeIfExists(@NotNull final SProject project,
                                  @NotNull final String id) throws CannotDeleteData {
        if (getSettings(project).remove(id)) {
            project.persist();
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    private SQSProjectSettings getSettings(@NotNull final SProject project) {
        final ProjectSettings settings = mySettingsManager.getSettings(project.getProjectId(), SQS_MANAGER_KEY);
        if (!(settings instanceof SQSProjectSettings)) {
            // TODO log error
            return new SQSProjectSettings();
        } else {
            return (SQSProjectSettings)settings;
        }
    }

    @NotNull
    public ProjectSettings createProjectSettings(String s) {
        return new SQSProjectSettings();
    }

}
