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

package jetbrains.buildserver.sonarplugin.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * SonarQube Server definition.
 */
public interface SQSInfo {
    @Nullable
    String getUrl();

     @Nullable
    String getLogin();

     @Nullable
    String getPassword();

    @Nullable
    String getJDBCUrl();

    @Nullable
    String getJDBCUsername();

    @Nullable
    String getJDBCPassword();

    @NotNull
    String getId();

    @Nullable
    String getName();

    @Nullable
    String getDescription();

    @NotNull
    Map<String, String> getParameters();
}
