

package jetbrains.buildserver.sonarplugin.util;

import java.util.Map;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

public interface Execution {
    @NotNull
    Executable modify(@NotNull Executable old, final BuildRunnerContext runnerContext, @NotNull Map<String, String> environmentVariables);
}