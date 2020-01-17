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
    private static final String EDIT_SQRRUN_PARAMS_JSP_PATH = "scanner/editSQRRunParams.jsp";
    private static final String VIEW_SQRRUN_PARAMS_JSP_PATH = "scanner/viewSQRRunParams.jsp";
    private static final String RUNNER_DISPLAY_NAME = "SonarQube Runner";
    private static final String RUNNER_DESCRIPTION = "Runner for executing SonarQube analysis";
    private static final String DEFAULT_TOOL_VERSION = "%teamcity.tool." + SonarQubeScannerConstants.SONAR_QUBE_SCANNER_TOOL_TYPE_ID + ".DEFAULT%";

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
        map.put(SonarQubeScannerConstants.SONAR_QUBE_SCANNER_VERSION_PARAMETER, DEFAULT_TOOL_VERSION);
        return map;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull final Map<String, String> parameters) {
        return "";
    }
}
