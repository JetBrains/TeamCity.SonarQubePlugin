package jetbrains.buildserver.sonarplugin.sqrunner.manager.projectfeatures;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.BaseSQSInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by linfar on 03.10.16.
 */
public class SQSManagerProjectFeatures implements SQSManager {
    private static final String PROJECT_FEATURE_TYPE = "";

    @NotNull
    @Override
    public synchronized List<SQSInfo> getAvailableServers(@NotNull SProject project) {
        return getAvailableServersStream(project).collect(Collectors.toList());
    }

    private Stream<? extends SQSInfo> getAvailableServersStream(@NotNull SProject project) {
        return project.getAvailableFeaturesOfType(PROJECT_FEATURE_TYPE).stream().map(SQSInfoImpl::new);
    }

    @NotNull
    @Override
    public synchronized List<SQSInfo> getOwnAvailableServers(@NotNull SProject project) {
        return getOwnServersStream(project).collect(Collectors.toList());
    }

    private Stream<? extends SQSInfo> getOwnServersStream(@NotNull SProject project) {
        return project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream().map(SQSInfoImpl::new);
    }

    @Nullable
    @Override
    public synchronized SQSInfo getServer(@NotNull SProject project, @NotNull String serverId) {
        return getAvailableServersStream(project).filter(sqs -> serverId.equals(sqs.getId())).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public synchronized SQSInfo getOwnServer(@NotNull SProject project, @NotNull String serverId) {
        return getOwnServersStream(project).filter(sqs -> serverId.equals(sqs.getId())).findFirst().orElse(null);
    }

    @Override
    public synchronized void editServer(@NotNull SProject project, @NotNull String serverId, @NotNull SQSInfo modifiedServer) throws IOException {
        final Optional<SProjectFeatureDescriptor> found = findByServerId(project, serverId);
        if (found.isPresent()) {
            final SProjectFeatureDescriptor featureDescriptor = found.get();
            project.updateFeature(featureDescriptor.getId(), PROJECT_FEATURE_TYPE, toMap(modifiedServer));
        }
    }

    @Override
    public synchronized void addServer(@NotNull SProject toProject, @NotNull SQSInfo newServer) throws IOException {
        toProject.addFeature(PROJECT_FEATURE_TYPE, toMap(newServer));
    }

    @Override
    public synchronized SQSInfo removeIfExists(@NotNull SProject project, @NotNull String serverId) throws CannotDeleteData {
        final Optional<SProjectFeatureDescriptor> found = findByServerId(project, serverId);
        if (found.isPresent() && project.removeFeature(found.get().getId()) != null) {
            return new SQSInfoImpl(found.get());
        }
        return null;
    }

    private Optional<SProjectFeatureDescriptor> findByServerId(@NotNull SProject project, @NotNull String serverId) {
        return project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream().filter(f -> {
            final String id = f.getParameters().get(BaseSQSInfo.ID);
            return id != null && serverId.equals(id);
        }).findFirst();
    }

    @NotNull
    private Map<String, String> toMap(@NotNull final SQSInfo sqsInfo) {
        final Map<String, String> res = new HashMap<>();
        res.putAll(sqsInfo.getParameters());
        for (String key : BaseSQSInfo.ENCRYPTED_FIELDS) {
            final String value = sqsInfo.getParameters().get(key);
            if (value != null) {
                res.put(key, EncryptUtil.scramble(value));
            }
        }
        return res;
    }
}