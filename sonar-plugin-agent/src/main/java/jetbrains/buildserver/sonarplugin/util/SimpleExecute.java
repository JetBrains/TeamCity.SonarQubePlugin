

package jetbrains.buildserver.sonarplugin.util;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleExecute extends CommandLineBuildService  {
    @NotNull
    private final Execution myChain;
    @NotNull
    private final ExecutableFactory myExecutableFactory;
    @Nullable
    private final String myAbsolutePath;

    public SimpleExecute(@NotNull final ExecutionChain chain,
                         @NotNull final ExecutableFactory executableFactory,
                         @NotNull final String workingDirectory) {

        myChain = chain;
        myExecutableFactory = executableFactory;
        myAbsolutePath = workingDirectory;
    }
    public SimpleExecute(@NotNull final ExecutionChain chain,
                         @NotNull final ExecutableFactory executableFactory) {

        myChain = chain;
        myExecutableFactory = executableFactory;
        myAbsolutePath = null;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        Map<String, String> environmentVariables = new HashMap<>(getRunnerContext().getBuildParameters().getEnvironmentVariables());
        final Executable executable = myChain.modify(myExecutableFactory.create(getRunnerContext()), getRunnerContext(), environmentVariables);

        return new SimpleProgramCommandLine(
          environmentVariables,
          myAbsolutePath != null ? myAbsolutePath : getRunnerContext().getWorkingDirectory().getAbsolutePath(),
          executable.myExecutable,
          executable.myArguments);
    }
}