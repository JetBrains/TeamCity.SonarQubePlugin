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

package jetbrains.buildserver.sonarplugin;

import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Andrey Titov on 4/7/14.
 *
 * Utility methods used across the plugin
 */
public final class Util {
    private Util() {
    }

    /**
     * <p>Closes closable resource ignoring exception. Does nothing when null is passed</p>
     * @param fw Resource to close.
     */
    public static void close(@Nullable final Closeable fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    public static boolean isSonarRunner(@NotNull final String runType) {
        return Constants.RUNNER_TYPE.equals(runType) || SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_FINISH_ID.equals(runType);
    }

    /**
     * @param str String to check
     * @return true if str is null or empty (zero length or spaces only)
     */
    public static boolean isEmpty(@Nullable final String str) {
        return str == null || str.trim().isEmpty();
    }
}
