package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.projectfeatures.SQSInfoImpl;
import jetbrains.buildserver.sonarplugin.manager.projectfeatures.SQSManagerProjectFeatures;
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
    private final SQSManagerProjectFeatures mySQSManagerProjectFeatures;


    public SonarQubeSecureParametersProcessor(@NotNull SQSManagerProjectFeatures sqsManagerProjectFeatures,
                                              @NotNull ProjectManager myProjectManager,
                                              @NotNull EventDispatcher<BuildServerListener> dispatcher) {
        this.myProjectManager = myProjectManager;
        this.mySQSManagerProjectFeatures = sqsManagerProjectFeatures;
        dispatcher.addListener(this);
    }

    @Override
    public void serverStartup() {
        myProjectManager.getProjects().forEach(project -> {
            if (project.isReadOnly()) {
                return;
            }
            List<SQSInfo> servers = mySQSManagerProjectFeatures.getOwnAvailableServers(project);
            if (servers.isEmpty()) return;
            for (SQSInfo server : servers) {
                Map<String, String> parameters = server.getParameters();
                if (parameters.containsKey(PASSWORD) || parameters.containsKey(TOKEN) || parameters.containsKey(JDBC_PASSWORD)) {
                    Map<String, String> newParameters = new HashMap<>(parameters);
                    changeParameter(parameters, newParameters, PASSWORD);
                    changeParameter(parameters, newParameters, TOKEN);
                    changeParameter(parameters, newParameters, JDBC_PASSWORD);
                    mySQSManagerProjectFeatures.editServer(project, new SQSInfoImpl(newParameters));
                }
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
