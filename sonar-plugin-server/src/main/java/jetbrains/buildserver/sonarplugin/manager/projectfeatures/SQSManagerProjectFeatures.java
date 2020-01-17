/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildserver.sonarplugin.manager.projectfeatures;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by linfar on 03.10.16.
 *
 * Project features based SQSManager. Doesn't store data itself converting it from project features on demand instead.
 */
public class SQSManagerProjectFeatures implements SQSManager {
    protected static final String PROJECT_FEATURE_TYPE = "sonar-qube";

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
        return project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream().map(SQSInfoImpl::new).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public synchronized SQSInfo getServer(@NotNull SProject project, @NotNull String serverId) {
        final Optional<SProjectFeatureDescriptor> optional = project.getAvailableFeaturesOfType(PROJECT_FEATURE_TYPE).stream().filter(f -> {
            final String id = f.getParameters().get(BaseSQSInfo.ID);
            return id != null && serverId.equals(id);
        }).findFirst();
        if (optional.isPresent()) {
            return new SQSInfoImpl(optional.get());
        }
        return null;
    }

    @Nullable
    @Override
    public synchronized SQSInfo getOwnServer(@NotNull SProject project, @NotNull String serverId) {
        final Optional<SProjectFeatureDescriptor> optional = findByServerId(project, serverId);
        if (optional.isPresent()) {
            return new SQSInfoImpl(optional.get());
        }
        return null;
    }

    @NotNull
    @Override
    public synchronized SQSActionResult editServer(@NotNull SProject project, @NotNull SQSInfo modifiedServer) {
        final Optional<SProjectFeatureDescriptor> found = findByServerId(project, modifiedServer.getId());
        if (found.isPresent()) {
            final SProjectFeatureDescriptor featureDescriptor = found.get();
            project.updateFeature(featureDescriptor.getId(), PROJECT_FEATURE_TYPE, toMap(modifiedServer));
            return new SQSActionResult(new SQSInfoImpl(found.get()), modifiedServer, "SonarQube Server '" + modifiedServer.getName() + "' updated");
        } else {
            return addServer(project, modifiedServer);
        }
    }

    @NotNull
    @Override
    public synchronized SQSActionResult addServer(@NotNull SProject toProject, @NotNull SQSInfo newServer) {
        if (getServer(toProject, newServer.getId()) != null) return new SQSActionResult(null, null, "Cannot add: SonarQube Server with id '" + newServer.getId() + "' already exists", true);
        doAddServer(toProject, newServer);
        return new SQSActionResult(null, newServer, "SonarQube Server '" + newServer.getName() + " added");
    }

    @NotNull
    @Override
    public synchronized SQSActionResult removeServer(@NotNull SProject project, @NotNull String serverId) {
        final Optional<SProjectFeatureDescriptor> found = findByServerId(project, serverId);
        if (found.isPresent() && project.removeFeature(found.get().getId()) != null) {
            final SQSInfoImpl old = new SQSInfoImpl(found.get());
            return new SQSActionResult(old, null, "SonarQube Server '" + old.getName() + "' removed");
        }
        return new SQSActionResult(null, null, "Cannot remove: SonarQube Server with id '" + serverId + "' doesn't exist");
    }

    @NotNull
    @Override
    public String getDescription() {
        return "'sonar-qube' project features";
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

    private void doAddServer(@NotNull SProject toProject, @NotNull SQSInfo newServer) {
        toProject.addFeature(PROJECT_FEATURE_TYPE, toMap(newServer));
    }
}