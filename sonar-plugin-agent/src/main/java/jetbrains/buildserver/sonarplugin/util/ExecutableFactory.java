package jetbrains.buildserver.sonarplugin.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

public interface ExecutableFactory {
    @NotNull
    Executable create(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException;
}
