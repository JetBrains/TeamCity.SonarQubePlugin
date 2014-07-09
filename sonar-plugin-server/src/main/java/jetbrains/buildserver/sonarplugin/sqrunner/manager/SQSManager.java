package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Created by linfar on 7/9/14.
 */
public interface SQSManager {
    List<SQSInfo> getAvailableServers(@NotNull ProjectAccessor accessor);

    @Nullable
    SQSInfo findServer(@NotNull ProjectAccessor accessor, @NotNull String serverId);

    void editServer(@NotNull SProject project,
                    @NotNull String serverId,
                    @NotNull SQSInfo modifiedSerever) throws IOException;

    void addServer(@NotNull SQSInfo newServer,
                   @NotNull SProject toProject) throws IOException;

    boolean removeIfExists(@NotNull SProject currentProject,
                           @NotNull String id) throws CannotDeleteData;

    public static class CannotWriteData extends IOException {
        public CannotWriteData(final @NotNull String message) {
            super(message);
        }
    }

    public static class ServerInfoExists extends IOException {
    }

    public static class ServerIdMissing extends RuntimeException {
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

        public abstract SProject next();
    }
}
