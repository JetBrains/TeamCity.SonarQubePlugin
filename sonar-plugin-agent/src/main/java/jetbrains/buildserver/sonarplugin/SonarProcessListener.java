package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by linfar on 4/4/14.
 */
public class SonarProcessListener extends AgentLifeCycleAdapter {
    private static final String ANALYSIS_SUCCESSFUL = "ANALYSIS SUCCESSFUL, you can browse ";
    public static final String SONAR_SERVER_URL_FILENAME = "sonar_server.txt";
    public static final String SONAR_SERVER_URL_ARTIF_LOCATION = ".teamcity/sonar/";

    @NotNull
    private final ArtifactsWatcher myWatcher;

    public SonarProcessListener(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher,
                                @NotNull final ArtifactsWatcher watcher) {
        myWatcher = watcher;
        agentDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        // TODO: check Sonar is enabled
    }

    @Override
    public void messageLogged(@NotNull AgentRunningBuild build, @NotNull BuildMessage1 buildMessage) {
        final String message = buildMessage.getValue().toString();
        final int start = message.indexOf(ANALYSIS_SUCCESSFUL);
        if (start >= 0) {
            final String URL = message.substring(start + ANALYSIS_SUCCESSFUL.length());
            FileWriter fw = null;
            try {
                final File output = new File(build.getBuildTempDirectory(), SONAR_SERVER_URL_FILENAME);
                fw = new FileWriter(output);
                fw.write(URL);
                myWatcher.addNewArtifactsPath(output.getAbsolutePath() + "=>" + SONAR_SERVER_URL_ARTIF_LOCATION);
            } catch (IOException e) {
                build.getBuildLogger().message("Cannot save Sonar URL \"" + URL + "\" to file \"" + "\": " + e.getMessage());
            } finally {
                close(fw);
            }
        }
    }

    private static void close(Closeable fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException ignore) {
            }
        }
    }
}
