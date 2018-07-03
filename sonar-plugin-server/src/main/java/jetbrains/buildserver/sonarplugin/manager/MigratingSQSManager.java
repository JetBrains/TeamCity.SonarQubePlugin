package jetbrains.buildserver.sonarplugin.manager;

import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.SProject;
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
    @NotNull
    private final List<SQSManager> mySQSManagers;
    @NotNull
    private final SQSManager myEditManager;
    @NotNull private final ConfigActionFactory myConfigActionFactory;

    public MigratingSQSManager(@NotNull final List<SQSManager> sqsManagers,
                               @NotNull final SQSManager editManager,
                               @NotNull final ConfigActionFactory configActionFactory) {
        mySQSManagers = sqsManagers;
        myConfigActionFactory = configActionFactory;
        myEditManager = editManager;
    }

    @NotNull
    @Override
    public List<SQSInfo> getAvailableServers(@NotNull SProject project) {
        final Set<SQSInfo> res = new HashSet<>();
        for (SQSManager sqsManager: mySQSManagers) {
            res.addAll(sqsManager.getAvailableServers(project));
        }
        return new ArrayList<>(res);
    }

    @NotNull
    @Override
    public List<SQSInfo> getOwnAvailableServers(@NotNull SProject project) {
        final Set<SQSInfo> res = new HashSet<>();
        for (SQSManager sqsManager: mySQSManagers) {
            res.addAll(sqsManager.getOwnAvailableServers(project));
        }
        return new ArrayList<>(res);
    }

    @Nullable
    @Override
    public SQSInfo getServer(@NotNull SProject project, @NotNull String serverId) {
        for (SQSManager sqsManager: mySQSManagers) {
            SQSInfo server = sqsManager.getServer(project, serverId);
            if (server != null) return server;
        }
        return null;
    }

    @Nullable
    @Override
    public SQSInfo getOwnServer(@NotNull SProject project, @NotNull String serverId) {
        for (SQSManager sqsManager: mySQSManagers) {
            SQSInfo server = sqsManager.getOwnServer(project, serverId);
            if (server != null) return server;
        }
        return null;
    }

    @NotNull
    @Override
    public SQSActionResult editServer(@NotNull SProject project, @NotNull SQSInfo sqsInfo) {
        if (myEditManager.getServer(project, sqsInfo.getId()) == null) {
            for (SQSManager sqsManager: mySQSManagers) {
                if (sqsManager == myEditManager) continue;

                SQSInfo init = sqsManager.getServer(project, sqsInfo.getId());
                if (init != null) {
                    migrate(sqsManager, project, sqsInfo);
                    return new SQSActionResult(init, sqsInfo, "SonarQube Server '" + sqsInfo.getName() + "' updated and moved to project features");
                } else {
                    return new SQSActionResult(null, null, "Cannot edit: SonarQube Server with id '" + sqsInfo.getId() + "' was not found", true);
                }
            }
        }

        return myEditManager.editServer(project, sqsInfo);
    }

    @NotNull
    @Override
    public SQSActionResult addServer(@NotNull SProject project, @NotNull SQSInfo sqsInfo) {
        return myEditManager.addServer(project, sqsInfo);
    }

    @NotNull
    @Override
    public SQSActionResult removeServer(@NotNull SProject project, @NotNull String serverId) {
        SQSActionResult oldSqsInfo = null;
        for (SQSManager sqsManager: mySQSManagers) {
            if (sqsManager != myEditManager) {
                final SQSActionResult sqsActionResult = sqsManager.removeServer(project, serverId);
                if (oldSqsInfo == null || !oldSqsInfo.isError()) {
                    oldSqsInfo = sqsActionResult;
                }
            }
        }

        final SQSActionResult sqsInfo = myEditManager.removeServer(project, serverId);
        return oldSqsInfo == null || !oldSqsInfo.isError() ? sqsInfo : oldSqsInfo;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Composite manager";
    }

    private void migrate(final SQSManager sqsManager, @NotNull final SProject project, @NotNull final SQSInfo... sqsInfos) {
        for (SQSInfo sqsInfo : sqsInfos) {
            myEditManager.addServer(project, sqsInfo);
            sqsManager.removeServer(project, sqsInfo.getId());
        }
        if (sqsInfos.length > 0) {
            if (sqsInfos.length > 1) {
                project.persist(myConfigActionFactory.createAction(project, sqsInfos.length + " SonarQube Servers moved from " + sqsManager.getDescription() + " to " + myEditManager.getDescription()));
            } else {
                project.persist(myConfigActionFactory.createAction(project, "SonarQube Server '" + sqsInfos[0].getName() + "' moved from " + sqsManager.getDescription() + " to " + myEditManager.getDescription()));
            }
        }
    }
}
