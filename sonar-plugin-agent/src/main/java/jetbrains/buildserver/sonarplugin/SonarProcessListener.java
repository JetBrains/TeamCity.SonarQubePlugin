package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.AbstractTextMessageProcessor;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesProcessor;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andrey Titov on 4/4/14.
 *
 * Experimental. Attempt to find test results by reading the build log for test report analysis.
 */
public class SonarProcessListener extends AgentLifeCycleAdapter {
    private static final String ANALYSIS_SUCCESSFUL = "ANALYSIS SUCCESSFUL, you can browse ";
    private final Set<String> myCollectedReports = new HashSet<String>();

    @NotNull
    private final ArtifactsWatcher myWatcher;
    @NotNull
    private final AbstractTextMessageProcessor myMessageProcessor = new ImportDataMessageProcessor();
    private boolean mySonarIsWorking = false;

    public SonarProcessListener(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher,
                                @NotNull final ArtifactsWatcher watcher) {
        myWatcher = watcher;
        agentDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        mySonarIsWorking = Util.isSonarRunner(runner.getRunType());
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        mySonarIsWorking = false;
    }

    @Override
    public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        myCollectedReports.clear();
    }

    @Override
    public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
        myCollectedReports.clear();
    }

    @Override
    public void messageLogged(@NotNull AgentRunningBuild build, @NotNull BuildMessage1 buildMessage) {
        processMessage(build, buildMessage.getValue().toString(), buildMessage);
    }

    protected void processMessage(AgentRunningBuild build, String message, BuildMessage1 buildMessage) {
        if (mySonarIsWorking) {
            final int start = message.indexOf(ANALYSIS_SUCCESSFUL);
            if (start >= 0) {
                final String url = message.substring(start + ANALYSIS_SUCCESSFUL.length());
                // TODO: save URL to a parameter instead to be able to specify URL strictly in configuration
                FileWriter fw = null;
                try {
                    final File output = new File(build.getBuildTempDirectory(), Constants.SONAR_SERVER_URL_FILENAME);
                    fw = new FileWriter(output);
                    fw.write(url);
                    myWatcher.addNewArtifactsPath(output.getAbsolutePath() + "=>" + Constants.SONAR_SERVER_URL_ARTIF_LOCATION);
                } catch (IOException e) {
                    build.getBuildLogger().message("Cannot save Sonar URL \"" + url + "\" to file \"" + "\": " + e.getMessage());
                } finally {
                    Util.close(fw);
                }
            }
        } else {
            ServiceMessagesProcessor.processTextMessage(buildMessage, myMessageProcessor);
        }
    }

    public Set<String> getCollectedReports() {
        return new HashSet<String>(myCollectedReports);
    }

    private class ImportDataMessageProcessor extends AbstractTextMessageProcessor {
        public void processServiceMessage(@NotNull ServiceMessage serviceMessage, @NotNull BuildMessage1 buildMessage1) {
            if ("importData".equals(serviceMessage.getMessageName())) {
                final String path = serviceMessage.getAttributes().get("path");
                if (path != null) {
                    final String dir;
                    final File file = new File(path);
                    if (file.exists() && file.isDirectory()) {
                        dir = path;
                    } else {
                        dir = path.substring(0, path.lastIndexOf(File.separatorChar));
                    }
                    myCollectedReports.add(dir);
                }
            }
        }
    }
}
