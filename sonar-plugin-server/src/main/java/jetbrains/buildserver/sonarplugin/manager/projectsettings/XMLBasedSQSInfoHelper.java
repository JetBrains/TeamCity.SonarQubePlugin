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

package jetbrains.buildserver.sonarplugin.manager.projectsettings;

import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by Andrey Titov on 7/10/14.
 */
public class XMLBasedSQSInfoHelper {
    private XMLBasedSQSInfoHelper() {
    }

    public static SQSInfo createServerInfo(@Nullable final String id,
                                           @Nullable final String name,
                                           @Nullable final String url,
                                           @Nullable final String login,
                                           @Nullable final String password,
                                           @Nullable final String dbUrl,
                                           @Nullable final String dbUsername,
                                           @Nullable final String dbPassword) {
        return new XMLBasedSQSInfo(id == null ? UUID.randomUUID().toString() : id, name, url, login, password, dbUrl, dbUsername, dbPassword);
    }

    public static SQSInfo createServerInfo(@Nullable final String id) {
        return new XMLBasedSQSInfo(id == null ? UUID.randomUUID().toString() : id, null, null, null, null, null, null, null);
    }
}
