

package jetbrains.buildserver.sonarplugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by Andrey Titov on 4/8/14.
 *
 * Accessor to simplify SQR parameters managing
 */
public class SQRParametersAccessor {
    private static final Logger LOG = Logger.getInstance(SQRParametersAccessor.class);
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
        String value = myParameters.get(Constants.SONAR_HOST_URL);
        if (value != null && !value.startsWith("http")) {
            value = "http://" + value;
        }
        return value;
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

    public String getToken() {
        return myParameters.get(Constants.SONAR_TOKEN);
    }

    public String getPassword() {
        return myParameters.get(Constants.SONAR_PASSWORD);
    }

    public String getProjectHome() {
        return myParameters.get(Constants.PROJECT_HOME);
    }

    public String getToolVersion() {
        String path = myParameters.get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
        try {
            if (path != null) {
                String parts[] = myParameters.get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER).split("/");
                String versionPart = parts[parts.length - 1].split("\\.", 2)[1];
                return versionPart;
            }
        } catch (Throwable e) {
            LOG.debug("Can't parse string version from tool: {}");
        }
        return "unknown";
    }
}