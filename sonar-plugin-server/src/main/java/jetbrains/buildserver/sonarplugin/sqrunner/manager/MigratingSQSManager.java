package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectfeatures.SQSManagerProjectFeatures;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectsettings.SQSManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by linfar on 03.10.16.
 *
 * A SonarQube Server manager migrating SQSInfos from plugin-settings.xml to project features
 */
public class MigratingSQSManager implements SQSManager {
    @NotNull private final SQSManagerImpl mySQSManagerImpl;
    @NotNull private final SQSManagerProjectFeatures mySQSManagerProjectFeatures;
    @NotNull private final ConfigActionFactory myConfigActionFactory;

    public MigratingSQSManager(@NotNull final SQSManagerImpl sqsManagerImpl,
                               @NotNull final SQSManagerProjectFeatures sqsManagerProjectFeatures,
                               @NotNull final ConfigActionFactory configActionFactory) {
        mySQSManagerImpl = sqsManagerImpl;
        mySQSManagerProjectFeatures = sqsManagerProjectFeatures;
        myConfigActionFactory = configActionFactory;
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

    @NotNull
    @Override
    public SQSActionResult editServer(@NotNull SProject project, @NotNull SQSInfo sqsInfo) {
        SQSInfo init = mySQSManagerProjectFeatures.getServer(project, sqsInfo.getId());
        if (init == null) {
            init = mySQSManagerImpl.getServer(project, sqsInfo.getId());
            if (init != null) {
                migrate(project, sqsInfo);
                return new SQSActionResult(init, sqsInfo, "SonarQube Server '" + sqsInfo.getName() + "' updated and moved to project features");
            } else {
                return new SQSActionResult(null, null, "Cannot edit: SonarQube Server with id '" + sqsInfo.getId() + "' was not found", true);
            }
        } else {
            return mySQSManagerProjectFeatures.editServer(project, sqsInfo);
        }
    }

    @NotNull
    @Override
    public SQSActionResult addServer(@NotNull SProject project, @NotNull SQSInfo sqsInfo) {
        return mySQSManagerProjectFeatures.addServer(project, sqsInfo);
    }

    @NotNull
    @Override
    public SQSActionResult removeServer(@NotNull SProject project, @NotNull String serverId) {
        final SQSActionResult sqsInfo = mySQSManagerImpl.removeServer(project, serverId);
        final SQSActionResult oldSqsInfo = mySQSManagerProjectFeatures.removeServer(project, serverId);
        return !sqsInfo.isError() ? sqsInfo : oldSqsInfo;
    }

    private void migrate(@NotNull final SProject project, @NotNull final SQSInfo... sqsInfos) {
        for (SQSInfo sqsInfo : sqsInfos) {
            mySQSManagerProjectFeatures.addServer(project, sqsInfo);
            mySQSManagerImpl.removeServer(project, sqsInfo.getId());
        }
        if (sqsInfos.length > 0) {
            if (sqsInfos.length > 1) {
                project.persist(myConfigActionFactory.createAction(project, sqsInfos.length + " SonarQube Servers moved from plugin-settings to project features"));
            } else {
                project.persist(myConfigActionFactory.createAction(project, "SonarQube Server '" + sqsInfos[0].getName() + "' moved from plugin-settings to project features"));
            }
        }
    }
}
