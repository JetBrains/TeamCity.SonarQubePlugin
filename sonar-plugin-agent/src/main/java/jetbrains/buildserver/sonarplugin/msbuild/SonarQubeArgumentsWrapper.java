package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.SQArgsComposer;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SonarQubeArgumentsWrapper implements Execution {
    @NotNull private final SQArgsComposer mySQArgsComposer;

    public SonarQubeArgumentsWrapper(@NotNull final SQArgsComposer sqArgsComposer) {
        mySQArgsComposer = sqArgsComposer;
    }

    @NotNull
    @Override
    public Executable modify(@NotNull final Executable old,
                             @NotNull final BuildRunnerContext runnerContext) {
        final Map<String, String> allParameters = new HashMap<String, String>(runnerContext.getRunnerParameters());
        allParameters.putAll(runnerContext.getBuild().getSharedConfigParameters());
        final SQRParametersAccessor accessor = new SQRParametersAccessor(allParameters);

        final List<String> args = mySQArgsComposer.composeArgs(accessor, new DotNetSonarQubeKeysProvider());
        final List<String> res = new ArrayList<String>(old.myArguments);
        res.addAll(args);

        return new Executable(old.myExecutable, res);
    }
}
