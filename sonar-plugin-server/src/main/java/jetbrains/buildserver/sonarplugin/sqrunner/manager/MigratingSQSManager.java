package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectsettings.SQSManagerImpl;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectfeatures.SQSManagerProjectFeatures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by linfar on 03.10.16.
 */
public class MigratingSQSManager implements SQSManager {
    @NotNull private final SQSManagerImpl mySQSManagerImpl;
    @NotNull private final SQSManagerProjectFeatures mySQSManagerProjectFeatures;

    public MigratingSQSManager(@NotNull final SQSManagerImpl sqsManagerImpl, @NotNull final SQSManagerProjectFeatures sqsManagerProjectFeatures) {
        mySQSManagerImpl = sqsManagerImpl;
        mySQSManagerProjectFeatures = sqsManagerProjectFeatures;
    }

    @NotNull
    @Override
    public List<SQSInfo> getAvailableServers(@NotNull SProject project) {
        final Set<SQSInfo> res = new HashSet<>(mySQSManagerProjectFeatures.getAvailableServers(project));
        res.addAll(mySQSManagerImpl.getAvailableServers(project));
        return new ArrayList<>(res);
    }

    @NotNull
    @Override
    public List<SQSInfo> getOwnAvailableServers(@NotNull SProject project) {
        final Set<SQSInfo> res = new HashSet<>(mySQSManagerProjectFeatures.getOwnAvailableServers(project));
        res.addAll(mySQSManagerImpl.getOwnAvailableServers(project));
        return new ArrayList<>(res);
    }

    @Nullable
    @Override
    public SQSInfo getServer(@NotNull SProject project, @NotNull String serverId) {
        SQSInfo server = mySQSManagerProjectFeatures.getServer(project, serverId);
        if (server != null) return server;
        return mySQSManagerImpl.getServer(project, serverId);
    }

    @Nullable
    @Override
    public SQSInfo getOwnServer(@NotNull SProject project, @NotNull String serverId) {
        SQSInfo server = mySQSManagerProjectFeatures.getOwnServer(project, serverId);
        if (server != null) return server;
        return mySQSManagerImpl.getOwnServer(project, serverId);
    }

    @Override
    public void editServer(@NotNull SProject project, @NotNull String serverId, @NotNull SQSInfo sqsInfo) throws IOException {
        if (mySQSManagerProjectFeatures.getServer(project, serverId) != null) {
            mySQSManagerProjectFeatures.editServer(project, serverId, sqsInfo);
        } else {
            mySQSManagerProjectFeatures.addServer(project, sqsInfo);
        }
        mySQSManagerImpl.removeIfExists(project, serverId);
    }

    @Override
    public void addServer(@NotNull SProject project, @NotNull SQSInfo sqsInfo) throws IOException {
        mySQSManagerProjectFeatures.addServer(project, sqsInfo);
    }

    @Override
    public SQSInfo removeIfExists(@NotNull SProject project, @NotNull String serverId) throws CannotDeleteData {
        final SQSInfo sqsInfo = mySQSManagerImpl.removeIfExists(project, serverId);
        final SQSInfo oldSqsInfo = mySQSManagerProjectFeatures.removeIfExists(project, serverId);
        return sqsInfo != null ? sqsInfo : oldSqsInfo;
    }
}
