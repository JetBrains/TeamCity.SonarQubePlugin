package jetbrains.buildserver.sonarplugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildserver.sonarplugin.manager.MigratingSQSManager;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.manager.projectfeatures.SQSInfoImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SonarQubeSecureParametersProcessor extends BuildServerAdapter {
    private final static String PASSWORD = "password";
    private final static String TOKEN = "token";
    private final static String JDBC_PASSWORD = "jdbcPassword";

    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final MigratingSQSManager myMigratingSQSManager;
    @NotNull
    private final ExecutorServices myExecutorServices;
    @NotNull
    private final ConfigActionFactory myConfigActionFactory;
    @NotNull
    private final static Logger LOG = Logger.getInstance(SonarQubeSecureParametersProcessor.class);


    public SonarQubeSecureParametersProcessor(@NotNull MigratingSQSManager migratingSQSManager,
                                              @NotNull ProjectManager myProjectManager,
                                              @NotNull EventDispatcher<BuildServerListener> dispatcher,
                                              @NotNull ExecutorServices executorServices,
                                              @NotNull ConfigActionFactory myConfigActionFactory) {
        this.myProjectManager = myProjectManager;
        this.myMigratingSQSManager = migratingSQSManager;
        this.myExecutorServices = executorServices;
        this.myConfigActionFactory = myConfigActionFactory;
        dispatcher.addListener(this);
    }

    @Override
    public void serverStartup() {
        myExecutorServices.getLowPriorityExecutorService().submit(() -> {
            try {
                myProjectManager.getProjects().forEach(project -> {
                    if (project.isReadOnly()) {
                        return;
                    }
                    List<SQSInfo> servers = myMigratingSQSManager.getOwnAvailableServers(project);
                    if (servers.isEmpty()) return;
                    for (SQSInfo server : servers) {
                        Map<String, String> parameters = server.getParameters();
                        if (parameters.containsKey(PASSWORD) || parameters.containsKey(TOKEN) || parameters.containsKey(JDBC_PASSWORD)) {
                            Map<String, String> newParameters = new HashMap<>(parameters);
                            changeParameter(parameters, newParameters, PASSWORD);
                            changeParameter(parameters, newParameters, TOKEN);
                            changeParameter(parameters, newParameters, JDBC_PASSWORD);
                            SQSManager.SQSActionResult result = myMigratingSQSManager.editServer(project, new SQSInfoImpl(newParameters));
                            if (!result.isError()) {
                                ConfigAction configAction = myConfigActionFactory.createAction(project, "parameters of SonarQube Server '" + server.getName() + "' were changed to secured version");
                                project.persist(configAction);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                LOG.warnAndDebugDetails("An error occurred during changing parameters in SonarQube runner plugin", e);
            }
        });

    }

    private void changeParameter(Map<String, String> parameters, Map<String, String> newParameters, String parameterName) {
        if (parameters.containsKey(parameterName)) {
            newParameters.remove(parameterName);
            newParameters.put("secure:" + parameterName, parameters.get(parameterName));
        }
    }
}
