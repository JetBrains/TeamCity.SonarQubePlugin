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
    List<SQSInfo> getAvailableServers(@NotNull ProjectAccessor accessor);

    @Nullable
    SQSInfo findServer(@NotNull ProjectAccessor accessor, @NotNull String serverId);

    void editServer(@NotNull final SProject project,
                    @NotNull final String serverId,
                    @NotNull final SQSInfo modifiedServer) throws IOException;

    void addServer(@NotNull final SProject toProject, @NotNull final SQSInfo newServer) throws IOException;

    boolean removeIfExists(@NotNull SProject currentProject,
                           @NotNull String id) throws CannotDeleteData;

    class ServerInfoExists extends IOException {
    }

    class CannotDeleteData extends IOException {
        public CannotDeleteData(@NotNull final String message) {
            super(message);
        }
    }

    abstract class ProjectAccessor {
        @Nullable
        protected SProject myProject;

        public ProjectAccessor(@Nullable final SProject firstProject) {
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
