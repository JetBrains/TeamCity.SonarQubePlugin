package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.PropertiesProcessorProvider;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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

    @NotNull private final PropertiesProcessorProvider myPropertiesProcessorProvider;

    public SQRRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                      @NotNull final PropertiesProcessorProvider propertiesProcessorProvider) {
        myPropertiesProcessorProvider = propertiesProcessorProvider;
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
        return myPropertiesProcessorProvider.getRunnerPropertiesProcessor();
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
        map.put(Constants.SONAR_PROJECT_NAME, Constants.DEFAULT_PROJECT_NAME);
        map.put(Constants.SONAR_PROJECT_KEY, Constants.DEFAULT_PROJECT_KEY);
        map.put(Constants.SONAR_PROJECT_VERSION, Constants.DEFAULT_PROJECT_VERSION);
        map.put(Constants.SONAR_PROJECT_SOURCES, Constants.DEFAULT_SOURCE_PATH);
        return map;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull final Map<String, String> parameters) {
        return "";
    }
}
