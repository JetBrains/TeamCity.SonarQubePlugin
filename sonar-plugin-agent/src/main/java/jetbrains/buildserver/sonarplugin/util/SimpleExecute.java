package jetbrains.buildserver.sonarplugin.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import org.jetbrains.annotations.NotNull;

public class SimpleExecute extends CommandLineBuildService  {
    @NotNull
    private final Execution myChain;
    @NotNull
    private final ExecutableFactory myExecutableFactory;

    public SimpleExecute(@NotNull final ExecutionChain chain,
                         @NotNull final ExecutableFactory executableFactory) {

        myChain = chain;
        myExecutableFactory = executableFactory;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Executable executable = myChain.modify(myExecutableFactory.create(getRunnerContext()), getRunnerContext());

        return new SimpleProgramCommandLine(
                getRunnerContext().getBuildParameters().getEnvironmentVariables(),
                getRunnerContext().getWorkingDirectory().getAbsolutePath(),
                executable.myExecutable,
                executable.myArguments);
    }
}
