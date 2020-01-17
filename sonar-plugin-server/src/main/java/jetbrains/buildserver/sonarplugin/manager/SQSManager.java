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

import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Andrey Titov on 7/9/14.
 * <p>
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

    @NotNull
    SQSActionResult editServer(@NotNull final SProject project, @NotNull final SQSInfo sqsInfo);

    @NotNull
    SQSActionResult addServer(@NotNull final SProject project, @NotNull final SQSInfo sqsInfo);

    @NotNull
    SQSActionResult removeServer(@NotNull final SProject project, @NotNull final String serverId);

    @NotNull
    String getDescription();

    class SQSActionResult {
        final SQSInfo myBeforeAction;
        final SQSInfo myAfterAction;
        final String myReason;
        final boolean myIsError;

        public SQSActionResult(@Nullable final SQSInfo beforeAction, @Nullable final SQSInfo afterAction, @NotNull final String reason) {
            this(beforeAction, afterAction, reason, false);
        }

        public SQSActionResult(@Nullable final SQSInfo beforeAction, @Nullable final SQSInfo afterAction, @NotNull final String reason, final boolean isError) {
            myBeforeAction = beforeAction;
            myAfterAction = afterAction;
            myReason = reason;
            myIsError = isError;
        }

        public SQSInfo getBeforeAction() {
            return myBeforeAction;
        }

        public SQSInfo getAfterAction() {
            return myAfterAction;
        }

        public String getReason() {
            return myReason;
        }

        public boolean isError() {
            return myIsError;
        }
    }
}
