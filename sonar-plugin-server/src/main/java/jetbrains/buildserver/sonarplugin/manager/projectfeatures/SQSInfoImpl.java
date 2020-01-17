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

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linfar on 03.10.16.
 */
public class SQSInfoImpl extends BaseSQSInfo {
    public SQSInfoImpl(@Nullable final Map<String, String> properties) {
        super(properties);
    }

    public SQSInfoImpl(@NotNull final SProjectFeatureDescriptor fd) {
        super(projectFeatureToMap(fd));
    }

    public SQSInfoImpl(@NotNull String id, @Nullable String name, @Nullable String url, @Nullable String login, @Nullable String password, @Nullable String jdbcUrl, @Nullable String jdbcUsername, @Nullable String jdbcPassword) {
        super(id, name, url, login, password, jdbcUrl, jdbcUsername, jdbcPassword);
    }

    public SQSInfoImpl(@NotNull String id) {
        super(id);
    }

    private static Map<String, String> projectFeatureToMap(@NotNull final SProjectFeatureDescriptor projectFeatureDescriptor) {
        final Map<String, String> res = new HashMap<>(projectFeatureDescriptor.getParameters());
        for (String f : ENCRYPTED_FIELDS) {
            final String val = res.get(f);
            if (!StringUtil.isEmpty(val)) {
                res.put(f, EncryptUtil.unscramble(val));
            }
        }
        return res;
    }
}
