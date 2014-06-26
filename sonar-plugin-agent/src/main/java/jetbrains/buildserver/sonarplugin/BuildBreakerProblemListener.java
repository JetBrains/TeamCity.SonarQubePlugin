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

    public BuildBreakerProblemListener(final @NotNull EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
        agentDispatcher.addListener(this);
    }

    @Override
    public void buildStarted(final @NotNull AgentRunningBuild runningBuild) {
        myBuildLogger = runningBuild.getBuildLogger();
    }

    @Override
    public void buildFinished(final @NotNull AgentRunningBuild build, final @NotNull BuildFinishedStatus buildStatus) {
        myBuildLogger = null;
    }

    @Override
    public void beforeRunnerStart(final @NotNull BuildRunnerContext runner) {
        mySonarIsWorking = Util.isSonarRunner(runner.getRunType());
    }

    @Override
    public void messageLogged(final @NotNull BuildMessage1 buildMessage) {
        if (mySonarIsWorking) {
            if (buildMessage.getValue() instanceof String) {
                final String message = (String)buildMessage.getValue();
                final int idx = message.indexOf(BUILD_BREAKER_MESSAGE);
                if (idx > 0) {
                    final String cause = message.substring(idx + BUILD_BREAKER_MESSAGE.length(), message.length());
                    myBuildProblems.add(cause);
                }
            }
        }
    }

    @Override
    public void runnerFinished(final @NotNull BuildRunnerContext runner, final @NotNull BuildFinishedStatus status) {
        if (mySonarIsWorking) {
            for (final String cause : myBuildProblems) {
                logError(new SonarBuildBreakerMessage(cause).asString());
            }
            myBuildProblems.clear();
        }
        mySonarIsWorking = false;
    }

    private void logError(final @NotNull String message) {
        if (myBuildLogger != null) {
            myBuildLogger.error(message);
        }
    }

    private static class SonarBuildBreakerMessage extends MessageWithAttributes {
        public SonarBuildBreakerMessage(final @NotNull String description) {
            super(ServiceMessageTypes.BUILD_PORBLEM, attributesMap(description));
        }

        private static Map<String, String> attributesMap(final @NotNull String description) {
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("type", "sonar-build-breaker");
            attributes.put("identity", Integer.toString(("sonar-build-breaker" + description).hashCode()));
            attributes.put("description", "Sonar BuildBreaker: " + description);
            return attributes;
        }
    }
}
