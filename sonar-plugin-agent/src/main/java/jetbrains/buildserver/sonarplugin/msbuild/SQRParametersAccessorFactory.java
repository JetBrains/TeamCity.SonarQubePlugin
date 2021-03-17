package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import org.jetbrains.annotations.NotNull;

public interface SQRParametersAccessorFactory {
    SQRParametersAccessor createAccessor(@NotNull final BuildRunnerContext runnerContext);
}
