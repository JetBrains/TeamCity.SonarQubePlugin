package jetbrains.buildserver.sonarplugin.sqrunner.manager.projectsettings;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
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
    @Override
    public List<SQSInfo> getAvailableServers(@NotNull final SProject project) {
        return getAvailableServers(ProjectAccessor.recurse(project));
    }

    @NotNull
    @Override
    public List<SQSInfo> getOwnAvailableServers(@NotNull final SProject project) {
        return getAvailableServers(ProjectAccessor.single(project));
    }

    @Nullable
    @Override
    public SQSInfo getServer(@NotNull final SProject project, @NotNull String serverId) {
        return findServer(ProjectAccessor.recurse(project), serverId);
    }

    @Nullable
    @Override
    public SQSInfo getOwnServer(@NotNull final SProject project, @NotNull String serverId) {
        return findServer(ProjectAccessor.single(project), serverId);
    }

    @Nullable
    private synchronized SQSInfo findServer(@NotNull final ProjectAccessor accessor, @NotNull final String serverId) {
        SProject project;
        while ((project = accessor.next()) != null) {
            final SQSInfo info = getSettings(project).getInfo(serverId);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    @NotNull
    private List<SQSInfo> getAvailableServers(@NotNull final ProjectAccessor accessor) {
        SProject project;
        List<SQSInfo> res = new LinkedList<>();
        while ((project = accessor.next()) != null) {
            res.addAll(getSettings(project).getAll());
        }
        return res;
    }

    public synchronized void editServer(@NotNull final SProject project,
                                        @NotNull final String serverId,
                                        @NotNull final SQSInfo sqsInfo) throws IOException {
        getSettings(project).setInfo(serverId, sqsInfo);
    }

    public synchronized void addServer(@NotNull final SProject project,
                                       @NotNull final SQSInfo sqsInfo) throws IOException {
        getSettings(project).setInfo(sqsInfo.getId(), sqsInfo);
    }

    public SQSInfo removeIfExists(@NotNull final SProject project,
                                  @NotNull final String serverId) throws CannotDeleteData {
        final SQSProjectSettings settings = getSettings(project);
        final SQSInfo info = settings.getInfo(serverId);
        if (info != null) {
            settings.remove(serverId);
            return info;
        }
        return null;
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


    private static abstract class ProjectAccessor {
        @Nullable
        SProject myProject;

        ProjectAccessor(@Nullable final SProject firstProject) {
            myProject = firstProject;
        }

        static ProjectAccessor recurse(@NotNull final SProject project) {
            return new ProjectAccessor(project) {
                public SProject next() {
                    if (myProject == null) {
                        return null;
                    }
                    SProject t = myProject;
                    myProject = myProject.getParentProject();
                    return t;
                }
            };
        }

        static ProjectAccessor single(@NotNull final SProject project) {
            return new ProjectAccessor(project) {
                @Override
                public SProject next() {
                    SProject t = myProject;
                    myProject = null;
                    return t;
                }
            };
        }

        public abstract SProject next();
    }
}
