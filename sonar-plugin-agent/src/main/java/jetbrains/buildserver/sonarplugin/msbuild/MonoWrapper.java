

package jetbrains.buildserver.sonarplugin.msbuild;

import java.util.Map;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MonoWrapper implements Execution {
    @NotNull
    private final MonoLocator myMonoLocator;

    public MonoWrapper(@NotNull final MonoLocator monoLocator) {
        myMonoLocator = monoLocator;
    }

    @NotNull
    @Override
    public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext, @NotNull Map<String, String> environmentVariables) {
        if (myMonoLocator.isMono()) {
            final List<String> newArgs = new ArrayList<String>(old.myArguments.size() + 1);
            newArgs.add(old.myExecutable);
            newArgs.addAll(old.myArguments);
            return new Executable(myMonoLocator.getMono(), newArgs);
        } else {
            return old;
        }
    }
}