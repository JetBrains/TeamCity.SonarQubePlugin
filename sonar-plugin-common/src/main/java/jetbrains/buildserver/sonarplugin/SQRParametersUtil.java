

package jetbrains.buildserver.sonarplugin;

import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class SQRParametersUtil {

    private static final List<String> AUTH_PARAMETER_KEYS = Arrays.asList(Constants.SONAR_LOGIN, Constants.SONAR_PASSWORD, Constants.SONAR_TOKEN);

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

    public static Map<String, String> mergeAuthAndToolPathParameters(@NotNull final Map<String, String> sharedParameters,
                                                          @NotNull final Map<String, String> runnerParameters) {
        Map<String, String> merged = mergeParameters(sharedParameters, runnerParameters);
        List<String> keysToKeep = new ArrayList<>(AUTH_PARAMETER_KEYS);
        keysToKeep.add(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
        merged.keySet().retainAll(keysToKeep);
        return merged;
    }
}