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

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.Util;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Andrey Titov on 6/2/14.
 * <p>
 * SonarQube Server parameters provider. Resolves SQS parameters by it's ID before build is started.
 */
public class SQSPropertiesProvider implements BuildStartContextProcessor {
    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final SQSManager mySqsManager;

    public SQSPropertiesProvider(@NotNull final ProjectManager projectManager, @NotNull final SQSManager sqsManager) {
        myProjectManager = projectManager;
        mySqsManager = sqsManager;
    }

    public void updateParameters(@NotNull final BuildStartContext context) {
        for (SRunnerContext runnerContext : context.getRunnerContexts()) {
            if (!Constants.RUNNER_TYPE.equals(runnerContext.getType()) && !SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_ID.equals(runnerContext.getType())) {
                continue;
            }

            final SQSInfo server = getSqsInfo(context, runnerContext);
            if (server == null) {
                continue;
            }

            doUpdateParameters(runnerContext, server);
        }
    }

    private SQSInfo getSqsInfo(@NotNull final BuildStartContext context, @NotNull final SRunnerContext runnerContext) {
        final String serverId = runnerContext.getParameters().get(Constants.SONAR_SERVER_ID);
        if (serverId == null) {
            return null;
        }

        final SProject project = myProjectManager.findProjectById(context.getBuild().getProjectId());
        if (project == null) {
            return null;
        }

        return mySqsManager.getServer(project, serverId);
    }

    private void doUpdateParameters(@NotNull final SRunnerContext runnerContext, @NotNull final SQSInfo server) {
        addIfNotNull(runnerContext, Constants.SONAR_HOST_URL, server.getUrl());
        addIfNotNull(runnerContext, Constants.SONAR_LOGIN, server.getLogin());
        addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_URL, server.getJDBCUrl());
        addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_USERNAME, server.getJDBCUsername());
        if (!Util.isEmpty(server.getPassword())) {
            runnerContext.addRunnerParameter(Constants.SONAR_PASSWORD, server.getPassword());
        }
        if (!Util.isEmpty(server.getJDBCPassword())) {
            runnerContext.addRunnerParameter(Constants.SONAR_SERVER_JDBC_PASSWORD, server.getJDBCPassword());
        }
        if (Util.isEmpty(runnerContext.getParameters().get(SonarQubeScannerConstants.SONAR_QUBE_SCANNER_VERSION_PARAMETER))) {
            runnerContext.addRunnerParameter(SonarQubeScannerConstants.SONAR_QUBE_SCANNER_VERSION_PARAMETER, "%teamcity.tool." + SonarQubeScannerConstants.SONAR_QUBE_SCANNER_TOOL_TYPE_ID + ".DEFAULT%");
        }
    }

    private static void addIfNotNull(@NotNull final SRunnerContext runnerContext,
                                     @NotNull final String key,
                                     @Nullable final String value) {
        if (!Util.isEmpty(value)) {
            runnerContext.addRunnerParameter(key, value);
        }
    }
}
