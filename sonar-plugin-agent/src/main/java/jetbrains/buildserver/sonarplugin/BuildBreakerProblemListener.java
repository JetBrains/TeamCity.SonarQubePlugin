package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Andrey Titov on 6/26/14.
 *
 * Listener for Build Breaker plugin. Catches and reports to TC build problems reported from BB plugin.
 */
public class BuildBreakerProblemListener extends AgentLifeCycleAdapter {
    public static final String BUILD_BREAKER_MESSAGE = "[BUILD BREAKER] ";

    private boolean mySonarIsWorking = false;
    @NotNull
    private final Set<String> myBuildProblems = new HashSet<String>();
    @Nullable
    private BuildProgressLogger myBuildLogger;

    public BuildBreakerProblemListener(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
        agentDispatcher.addListener(this);
    }

    @Override
    public void buildStarted(@NotNull final AgentRunningBuild runningBuild) {
        myBuildLogger = runningBuild.getBuildLogger();
    }

    @Override
    public void beforeBuildFinish(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
        myBuildLogger = null;
    }

    @Override
    public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        mySonarIsWorking = Util.isSonarRunner(runner.getRunType());
    }

    @Override
    public void messageLogged(@NotNull final BuildMessage1 buildMessage) {
        if (mySonarIsWorking && buildMessage.getValue() instanceof String) {
            final String message = (String) buildMessage.getValue();
            final int idx = message.indexOf(BUILD_BREAKER_MESSAGE);
            if (idx > 0) {
                final String cause = message.substring(idx + BUILD_BREAKER_MESSAGE.length(), message.length());
                myBuildProblems.add(cause);
            }
        }
    }

    @Override
    public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
        if (mySonarIsWorking) {
            for (final String cause : myBuildProblems) {
                logError(new SonarBuildBreakerMessage(cause).asString());
            }
            myBuildProblems.clear();
        }
        mySonarIsWorking = false;
    }

    private void logError(@NotNull final String message) {
        if (myBuildLogger != null) {
            myBuildLogger.error(message);
        }
    }

    private static class SonarBuildBreakerMessage extends MessageWithAttributes {
        public SonarBuildBreakerMessage(@NotNull final String description) {
            super(ServiceMessageTypes.BUILD_PORBLEM, attributesMap(description));
        }

        private static Map<String, String> attributesMap(@NotNull final String description) {
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("type", "sonar-build-breaker");
            attributes.put("identity", Integer.toString(("sonar-build-breaker" + description).hashCode()));
            attributes.put("description", "Sonar BuildBreaker: " + description);
            return attributes;
        }
    }
}
