package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.SQArgsComposer;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SonarQubeArgumentsWrapper implements Execution {
    @NotNull private final SQArgsComposer mySQArgsComposer;
    @NotNull
    private final SQRParametersAccessorFactory mySQRParametersAccessorFactory;

    public SonarQubeArgumentsWrapper(@NotNull final SQArgsComposer sqArgsComposer) {
        this(sqArgsComposer, new SQRParametersAccessorFactoryImpl());
    }

    public SonarQubeArgumentsWrapper(@NotNull final SQArgsComposer sqArgsComposer,
                                     @NotNull final SQRParametersAccessorFactory sqrParametersAccessorFactory) {
        mySQArgsComposer = sqArgsComposer;
        mySQRParametersAccessorFactory = sqrParametersAccessorFactory;
    }

    @NotNull
    @Override
    public Executable modify(@NotNull final Executable old,
                             @NotNull final BuildRunnerContext runnerContext) {
        final SQRParametersAccessor accessor = mySQRParametersAccessorFactory.createAccessor(runnerContext);

        final List<String> args = mySQArgsComposer.composeArgs(accessor, new DotNetSonarQubeKeysProvider());
        final List<String> res = new ArrayList<String>(old.myArguments);
        res.addAll(args);

        return new Executable(old.myExecutable, res);
    }
}
