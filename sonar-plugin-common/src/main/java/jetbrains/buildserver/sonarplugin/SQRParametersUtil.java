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
