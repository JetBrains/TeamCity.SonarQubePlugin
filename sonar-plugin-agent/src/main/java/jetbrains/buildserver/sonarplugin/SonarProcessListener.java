package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.BlockData;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by linfar on 4/4/14.
 */
public class SonarProcessListener extends AgentLifeCycleAdapter {
    private static final String ANALYSIS_SUCCESSFUL = "ANALYSIS SUCCESSFUL, you can browse ";
    private final Set<String> myCollectedReports = new HashSet<String>();

    @NotNull
    private final ArtifactsWatcher myWatcher;
    private boolean isReports;

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
    public void buildFinished(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        myCollectedReports.clear();
        isReports = false;
    }

    @Override
    public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
        myCollectedReports.clear();
        isReports = false;
    }

    @Override
    public void messageLogged(@NotNull AgentRunningBuild build, @NotNull BuildMessage1 buildMessage) {
        final String message = buildMessage.getValue().toString();
        final String blockName = getBlockName(buildMessage);
        final String typeId = buildMessage.getTypeId();

        processMessage(build, message, blockName, typeId);
    }

    protected void processMessage(AgentRunningBuild build, String message, String blockName, String typeId) {
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
        } else {
            if (!isReports) {
                if (blockName != null) {
                    isReports = blockName.equals("Successfully parsed");
                }
            } else {
                if (typeId.equals(DefaultMessagesInfo.MSG_BLOCK_END)) {
                    isReports = false;
                } else {
                    if (!message.matches("\\d+ report")) {
                        final File file = new File(build.getCheckoutDirectory(), message);

                        if (file.exists() && file.canRead()) {
                            if (file.isDirectory()) {
                                myCollectedReports.add(message);
                            } else {
                                myCollectedReports.add(file.getParentFile().getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private static String getBlockName(@Nullable final BuildMessage1 buildMessage) {
        return buildMessage != null && buildMessage.getValue() instanceof BlockData ?
                ((BlockData) buildMessage.getValue()).blockName : null;
    }

    public Set<String> getCollectedReports() {
        return new HashSet<String>(myCollectedReports);
    }
}
