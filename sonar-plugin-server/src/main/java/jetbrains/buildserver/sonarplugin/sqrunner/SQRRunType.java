package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildserver.sonarplugin.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Andrey Titov on 4/2/14.
 *
 * RunType definition for SonarQube Runner
 */
public class SQRRunType extends RunType {
    private static final String EDIT_SQRRUN_PARAMS_JSP_PATH = "editSQRRunParams.jsp";
    private static final String VIEW_SQRRUN_PARAMS_JSP_PATH = "viewSQRRunParams.jsp";
    private static final String RUNNER_DISPLAY_NAME = "SonarQube Runner";
    private static final String RUNNER_DESCRIPTION = "Runner for executing SonarQube analysis";

    public static final String DEFAULT_PROJECT_NAME = "%system.teamcity.projectName%";
    public static final String DEFAULT_PROJECT_KEY = "%teamcity.project.id%";
    public static final String DEFAULT_PROJECT_VERSION = "%build.number%";
    public static final String DEFAULT_SOURCE_PATH = "src";

    public SQRRunType(@NotNull final RunTypeRegistry runTypeRegistry) {
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return Constants.RUNNER_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return RUNNER_DISPLAY_NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return RUNNER_DESCRIPTION;
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new PropertiesProcessor() {
            public Collection<InvalidProperty> process(Map<String, String> properties) {
                List<InvalidProperty> invalidProperties = new LinkedList<InvalidProperty>();

                final String serverId = properties.get(Constants.SONAR_SERVER_ID);
                if (serverId == null) {
                    invalidProperties.add(new InvalidProperty(Constants.SQS_CHOOSER, "Choose a SonarQube Server to send information to"));
                }

                return invalidProperties;
            }
        };
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return EDIT_SQRRUN_PARAMS_JSP_PATH;
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return VIEW_SQRRUN_PARAMS_JSP_PATH;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SONAR_PROJECT_NAME, DEFAULT_PROJECT_NAME);
        map.put(Constants.SONAR_PROJECT_KEY, DEFAULT_PROJECT_KEY);
        map.put(Constants.SONAR_PROJECT_VERSION, DEFAULT_PROJECT_VERSION);
        map.put(Constants.SONAR_PROJECT_SOURCES, DEFAULT_SOURCE_PATH);
        return map;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull final Map<String, String> parameters) {
        return "";
    }
}
