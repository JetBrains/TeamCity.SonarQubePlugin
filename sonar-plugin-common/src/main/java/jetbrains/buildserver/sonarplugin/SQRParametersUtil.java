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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SQRParametersUtil {

    private static final List<String> AUTH_PARAMETER_KEYS = Arrays.asList(Constants.SONAR_LOGIN, Constants.SONAR_PASSWORD);

    private SQRParametersUtil() {
    }

    public static Map<String, String> mergeParameters(@NotNull final Map<String, String> sharedParameters,
                                                      @NotNull final Map<String, String> runnerParameters) {
        final Map<String, String> mergedParameters = new HashMap<String, String>();
        addDefaultParameters(mergedParameters);
        mergedParameters.putAll(runnerParameters);
        mergedParameters.putAll(sharedParameters);
        return mergedParameters;
    }

    private static void addDefaultParameters(final Map<String, String> mergedParameters) {
        mergedParameters.put(Constants.PROJECT_HOME, ".");
    }

    public static Map<String, String> mergeAuthParameters(@NotNull final Map<String, String> sharedParameters,
                                                          @NotNull final Map<String, String> runnerParameters) {
        Map<String, String> merged = mergeParameters(sharedParameters, runnerParameters);

        merged.keySet().retainAll(AUTH_PARAMETER_KEYS);

        return merged;
    }
}
