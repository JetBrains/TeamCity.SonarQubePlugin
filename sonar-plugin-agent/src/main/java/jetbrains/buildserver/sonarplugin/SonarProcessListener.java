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
                String path = serviceMessage.getAttributes().get("path");
                if (path == null) {
                    path = serviceMessage.getAttributes().get("file");
                }
                if (path != null) {
                    final String dir;
                    final File file = new File(path);
                    if (file.exists() && file.isDirectory()) {
                        dir = path;
                    } else {
                        final int endIndex = path.lastIndexOf(File.separatorChar);
                        dir = endIndex >= 0 ? path.substring(0, endIndex) : path;
                    }
                    myCollectedReports.add(dir);
                }
            }
        }
    }
}
