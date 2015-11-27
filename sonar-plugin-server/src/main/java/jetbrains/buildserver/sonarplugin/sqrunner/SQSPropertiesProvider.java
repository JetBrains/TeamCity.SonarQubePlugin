package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.Util;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager.ProjectAccessor.recurse;

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
            if (!Constants.RUNNER_TYPE.equals(runnerContext.getType())) {
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

        return mySqsManager.findServer(recurse(project), serverId);
    }

    private void doUpdateParameters(@NotNull final SRunnerContext runnerContext, @NotNull final SQSInfo server) {
        addIfNotNull(runnerContext, Constants.SONAR_HOST_URL, server.getUrl());
        addIfNotNull(runnerContext, Constants.SONAR_LOGIN, server.getLogin());
        addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_URL, server.getJDBCUrl());
        addIfNotNull(runnerContext, Constants.SONAR_SERVER_JDBC_USERNAME, server.getJDBCUsername());
        if (!Util.isEmpty(server.getPassword())) {
            runnerContext.addRunnerParameter(Constants.SONAR_PASSWORD, "%" + getSecuredPropertyName(server, Constants.SONAR_PASSWORD) + "%");
        }
        if (!Util.isEmpty(server.getJDBCPassword())) {
            runnerContext.addRunnerParameter(Constants.SONAR_SERVER_JDBC_PASSWORD, "%" + getSecuredPropertyName(server, Constants.SONAR_SERVER_JDBC_PASSWORD) + "%");
        }
    }

    @NotNull
    private String getSecuredPropertyName(@NotNull final SQSInfo server, @NotNull final String key) {
        return Constants.SECURE_TEAMCITY_PASSWORD_PREFIX + server.getId() + "." + key;
    }

    private static void addIfNotNull(@NotNull final SRunnerContext runnerContext,
                                     @NotNull final String key,
                                     @Nullable final String value) {
        if (!Util.isEmpty(value)) {
            runnerContext.addRunnerParameter(key, value);
        }
    }
}
