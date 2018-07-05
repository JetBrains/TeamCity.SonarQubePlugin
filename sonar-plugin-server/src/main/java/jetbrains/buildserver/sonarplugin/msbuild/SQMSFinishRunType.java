package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.PropertiesProcessorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

import static jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_FINISH_ID;

public class SQMSFinishRunType extends RunType {
    @NotNull
    private static final String DISPLAY_NAME = "SonarScanner for MSBuild: finish analysis";
    @NotNull
    private static final String DESCRIPTION = "Finishes SonarQube analysis and sends data to the server selected in begin analysis stage";
    @NotNull
    private static final String EDIT_JSP = "msbuild/editFinishSQMSRunParams.jsp";
    @NotNull
    private static final String VIEW_JSP = "msbuild/viewFinishSQMSRunParams.jsp";

    @NotNull
    private final PropertiesProcessorProvider myPropertiesProcessorProvider;
    @NotNull
    private final PluginDescriptor myPluginDescriptor;

    public SQMSFinishRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                             @NotNull final PropertiesProcessorProvider propertiesProcessorProvider,
                             @NotNull final PluginDescriptor pluginDescriptor) {
        myPropertiesProcessorProvider = propertiesProcessorProvider;
        myPluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return SONAR_QUBE_MSBUILD_RUN_TYPE_FINISH_ID;
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
        return map -> Collections.emptyList();
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath(EDIT_JSP);
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath(VIEW_JSP);
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return Collections.emptyMap();
    }
}

