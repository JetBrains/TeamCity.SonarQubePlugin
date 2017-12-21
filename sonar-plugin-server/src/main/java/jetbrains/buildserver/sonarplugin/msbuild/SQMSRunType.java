package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.PropertiesProcessorProvider;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_ID;

public class SQMSRunType extends RunType {
    @NotNull private static final String DISPLAY_NAME = "SonarQube MSBuild Scanner";
    @NotNull private static final String DESCRIPTION = "Runner for executing SonarQube analysis for MSBuild";
    @NotNull private static final String EDIT_JSP = "editSQRRunParams.jsp";
    @NotNull private static final String VIEW_JSP = "viewSQRRunParams.jsp";

    @NotNull private final PropertiesProcessorProvider myPropertiesProcessorProvider;

    public SQMSRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                       @NotNull final PropertiesProcessorProvider propertiesProcessorProvider) {
        myPropertiesProcessorProvider = propertiesProcessorProvider;
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return SONAR_QUBE_MSBUILD_RUN_TYPE_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return myPropertiesProcessorProvider.getRunnerPropertiesProcessor();
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return EDIT_JSP;
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return VIEW_JSP;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SONAR_PROJECT_NAME, Constants.DEFAULT_PROJECT_NAME);
        map.put(Constants.SONAR_PROJECT_KEY, Constants.DEFAULT_PROJECT_KEY);
        map.put(Constants.SONAR_PROJECT_VERSION, Constants.DEFAULT_PROJECT_VERSION);
        map.put(Constants.SONAR_PROJECT_SOURCES, Constants.DEFAULT_SOURCE_PATH);
        map.put(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER, "%teamcity.tool." + SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_ID + ".DEFAULT%");
        return map;
    }
}
