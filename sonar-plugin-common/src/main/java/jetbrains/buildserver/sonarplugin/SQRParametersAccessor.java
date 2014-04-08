package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by linfar on 4/8/14.
 */
public class SQRParametersAccessor {
    @NotNull
    private final Map<String, String> myParameters;



    public SQRParametersAccessor(final @NotNull Map<String, String> parameters) {
        myParameters = parameters;
    }

    public String getProjectName() {
        final String name = myParameters.get(Constants.SONAR_PROJECT_NAME);
        if (name != null) {
            return name;
        } else {
            return getProjectKey();
        }
    }

    public String getProjectKey() {
        return myParameters.get(Constants.SONAR_PROJECT_KEY);
    }

    public String getProjectVersion() {
        return myParameters.get(Constants.SONAR_PROJECT_VERSION);
    }

    public String getProjectSources() {
        return myParameters.get(Constants.SONAR_PROJECT_SOURCES);
    }

    public String getProjectModules() {
        return myParameters.get(Constants.SONAR_PROJECT_MODULES);
    }

    public String getAdditionalParameters() {
        return myParameters.get(Constants.SONAR_ADDITIONAL_PARAMETERS);
    }
}
