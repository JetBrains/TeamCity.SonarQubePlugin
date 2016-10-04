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
    List<SQSInfo> getAvailableServers(@NotNull final SProject project);

    @NotNull
    List<SQSInfo> getOwnAvailableServers(@NotNull final SProject project);

    @Nullable
    SQSInfo getServer(@NotNull final SProject project, @NotNull String serverId);

    @Nullable
    SQSInfo getOwnServer(@NotNull final SProject project, @NotNull String serverId);

    void editServer(@NotNull final SProject project,
                    @NotNull final String serverId,
                    @NotNull final SQSInfo sqsInfo) throws IOException;

    void addServer(@NotNull final SProject project, @NotNull final SQSInfo sqsInfo) throws IOException;

    SQSInfo removeIfExists(@NotNull SProject project,
                           @NotNull String serverId) throws CannotDeleteData;

    class ServerInfoExists extends IOException {
    }

    class CannotDeleteData extends IOException {
        public CannotDeleteData(@NotNull final String message) {
            super(message);
        }
    }
}
