package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by Andrey Titov on 4/8/14.
 *
 * Accessor to simplify SQR parameters managing
 */
public class SQRParametersAccessor {
    @NotNull
    private final Map<String, String> myParameters;

    public SQRParametersAccessor(@NotNull final Map<String, String> parameters) {
        myParameters = parameters;
    }

    public String getProjectName() {
        return myParameters.get(Constants.SONAR_PROJECT_NAME);
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

    public String getProjectTests() {
        return myParameters.get(Constants.SONAR_PROJECT_TESTS);
    }

    public String getProjectBinaries() {
        return myParameters.get(Constants.SONAR_PROJECT_BINARIES);
    }

    public String getProjectModules() {
        return myParameters.get(Constants.SONAR_PROJECT_MODULES);
    }

    public String getHostUrl() {
        return myParameters.get(Constants.SONAR_HOST_URL);
    }

    public String getJDBCUrl() {
        return myParameters.get(Constants.SONAR_SERVER_JDBC_URL);
    }

    public String getJDBCUsername() {
        return myParameters.get(Constants.SONAR_SERVER_JDBC_USERNAME);
    }

    public String getJDBCPassword() {
        return myParameters.get(Constants.SONAR_SERVER_JDBC_PASSWORD);
    }

    public String getAdditionalParameters() {
        return myParameters.get(Constants.SONAR_ADDITIONAL_PARAMETERS);
    }

    public String getLogin() {
        return myParameters.get(Constants.SONAR_LOGIN);
    }

    public String getPassword() {
        return myParameters.get(Constants.SONAR_PASSWORD);
    }
}
