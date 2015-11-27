package jetbrains.buildserver.sonarplugin.sqrunner;

import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.parameters.ParameterFactory;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import jetbrains.buildserver.sonarplugin.Constants;
import jetbrains.buildserver.sonarplugin.Util;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager.ProjectAccessor.recurse;

/**
 * Created by Andrey Titov on 10/30/14.
 *
 * <p>
 *   Provides Password Parameters ("sonar.password" and "sonar.jdbc.password") for the build.
 *
 *   The class uses closed API: ParameterFactory and PasswordsProvider
 *
 *   Currently only one SonarQube Runner is allowed for the build.
 * </p>
 */
public class SQRPasswordProvider implements PasswordsProvider {
    private static final String PASSWORD_PARAMETER_TYPE = "password";

     @NotNull private final SQSManager mySqsManager;
     @NotNull private final ParameterFactory myFactory;

    public SQRPasswordProvider(@NotNull final SQSManager sqsManager,
                               @NotNull final ParameterFactory factory) {
        mySqsManager = sqsManager;
        myFactory = factory;
    }

    @NotNull
    public Collection<Parameter> getPasswordParameters(@NotNull final SBuild build) {
        final List<SQSInfo> servers = findSQSInfos(build.getBuildType());
        final List<Parameter> list = new ArrayList<Parameter>();
        for (final SQSInfo server : servers) {
            addParameterIfNeeded(list, server.getId(), server.getPassword(), Constants.SONAR_PASSWORD);
            addParameterIfNeeded(list, server.getId(), server.getJDBCPassword(), Constants.SONAR_SERVER_JDBC_PASSWORD);
        }
        return list;
    }

    /**
     * <p>
     *     Adds a parameter to the parameter list if it's value is not empty
     * </p>
     * @param list List to add parameter to.
     * @param id Server ID
     * @param parameterValue Parameter value. The parameter will not be added if the value is null or empty.
     * @param parameterName Parameter name. NB: resulting Parameter will have name in form "secure:teamcity.password.&lt;parameterName&gt;".
     */
    private void addParameterIfNeeded(@NotNull final List<Parameter> list,
                                      @NotNull final String id,
                                      @Nullable final String parameterValue,
                                      @NotNull final String parameterName) {
        if (!Util.isEmpty(parameterValue)) {
            list.add(myFactory.createTypedParameter(id + "." + parameterName, parameterValue, PASSWORD_PARAMETER_TYPE));
        }
    }

    /**
     * @param buildType Build Configuration
     * @return SQSInfo in the Build Configuration or null
     */
    @NotNull
    private List<SQSInfo> findSQSInfos(@Nullable final SBuildType buildType) {
        if (buildType == null) {
            return Collections.emptyList();
        }
        final List<SQSInfo> res = new LinkedList<SQSInfo>();
        for (final SBuildRunnerDescriptor r : buildType.getBuildRunners()) {
            final String serverId = r.getParameters().get(Constants.SONAR_SERVER_ID);
            if (!Util.isEmpty(serverId)) {
                final SQSInfo server = mySqsManager.findServer(recurse(buildType.getProject()), serverId);
                if (server != null) {
                    res.add(server);
                }
            }
        }
        return res;
    }
}
