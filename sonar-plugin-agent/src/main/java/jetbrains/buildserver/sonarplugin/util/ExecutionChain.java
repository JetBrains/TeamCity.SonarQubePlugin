package jetbrains.buildserver.sonarplugin.util;

import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ExecutionChain implements Execution {
    @NotNull
    private final Collection<Execution> myChain;

    public ExecutionChain(@NotNull final Collection<Execution> chain) {
        myChain = chain;
    }

    @NotNull
    @Override
    public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
        Executable current = old;
        for (Execution execution : myChain) {
            current = execution.modify(current, runnerContext);
        }
        return current;
    }
}
