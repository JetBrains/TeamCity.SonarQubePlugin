package jetbrains.buildserver.sonarplugin.manager.projectsettings;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    public synchronized SQSActionResult editServer(@NotNull final SProject project,
                                                   @NotNull final SQSInfo sqsInfo){
        final SQSProjectSettings settings = getSettings(project);
        final SQSInfo old = settings.getInfo(sqsInfo.getId());
        settings.setInfo(sqsInfo.getId(), sqsInfo);
        return new SQSActionResult(old, sqsInfo, "SonarQube Server '" + sqsInfo.getName() + "' updated");
    }

    @NotNull
    public synchronized SQSActionResult addServer(@NotNull final SProject project,
                                                  @NotNull final SQSInfo sqsInfo) {
        final SQSProjectSettings settings = getSettings(project);
        final SQSInfo old = settings.getInfo(sqsInfo.getId());
        if (old != null) {
            return new SQSActionResult(old, null, "Cannot add: SonarQube Server with id " + sqsInfo.getId() + " already exists");
        }
        settings.setInfo(sqsInfo.getId(), sqsInfo);
        return new SQSActionResult(null, sqsInfo, "SonarQube Server '" + sqsInfo.getName() + " added");
    }

    @NotNull
    public SQSActionResult removeServer(@NotNull final SProject project,
                                        @NotNull final String serverId) {
        final SQSProjectSettings settings = getSettings(project);
        final SQSInfo old = settings.getInfo(serverId);
        if (old != null) {
            settings.remove(serverId);
            return new SQSActionResult(old, null, "SonarQube Server '" + old.getName() + "' removed");
        }
        return new SQSActionResult(null, null, "Cannot remove: SonarQube Server with id '" + serverId + "' doesn't exist", true);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "plugin-settings";
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
