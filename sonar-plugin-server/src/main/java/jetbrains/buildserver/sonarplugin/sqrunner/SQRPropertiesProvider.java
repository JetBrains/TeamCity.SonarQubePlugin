package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by linfar on 6/2/14.
 */
public class SQRPropertiesProvider implements BuildStartContextProcessor {
    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final SQSManager mySqsManager;

    public SQRPropertiesProvider(@NotNull ProjectManager projectManager, @NotNull SQSManager sqsManager) {
        myProjectManager = projectManager;
        mySqsManager = sqsManager;
    }

    public void updateParameters(@NotNull BuildStartContext context) {
        for (SRunnerContext runnerContext : context.getRunnerContexts()) {
            if (Constants.RUNNER_TYPE.equals(runnerContext.getType())) {
                final String serverId = runnerContext.getParameters().get(Constants.SONAR_SERVER_ID);
                if (serverId != null) {
                    final SProject project = myProjectManager.findProjectById(context.getBuild().getProjectId());
                    if (project != null) {
                        final SQSInfo server = mySqsManager.findServer(project, serverId);
                        if (server != null) {
                            addIfNotNull(runnerContext, Constants.SONAR_HOST_URL, server.getUrl());
                            addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_URL, server.getJDBCUrl());
                            addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_USERNAME, server.getJDBCUsername());
                            addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_PASSWORD, server.getJDBCPassword());
                        }
                    }
                }
            }
        }
    }

    private static void addIfNotNull(final @NotNull SRunnerContext runnerContext,
                                     final @NotNull String key,
                                     final @Nullable String value) {
        if (value != null) {
            runnerContext.addRunnerParameter(key, value);
        }
    }
}
