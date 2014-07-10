package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * SonarQube Server data manager
 */
public interface SQSManager {
    @NotNull
    public List<SQSInfo> getAvailableServers(@NotNull ProjectAccessor accessor);

    @Nullable
    public SQSInfo findServer(@NotNull ProjectAccessor accessor, @NotNull String serverId);

    public void editServer(final @NotNull SProject project,
                           final @NotNull String serverId,
                           final @NotNull SQSInfo modifiedServer) throws IOException;

    public void addServer(final @NotNull SProject toProject, final @NotNull SQSInfo newServer) throws IOException;

    public boolean removeIfExists(@NotNull SProject currentProject,
                           @NotNull String id) throws CannotDeleteData;

    public static class ServerInfoExists extends IOException {
    }

    public static class CannotDeleteData extends IOException {
        public CannotDeleteData(final @NotNull String message) {
            super(message);
        }
    }

    public static abstract class ProjectAccessor {
        @Nullable
        protected SProject myProject;

        public ProjectAccessor(final @Nullable SProject firstProject) {
            myProject = firstProject;
        }

        public static ProjectAccessor recurse(@NotNull final SProject project) {
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

        public static ProjectAccessor single(@NotNull final SProject project) {
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
