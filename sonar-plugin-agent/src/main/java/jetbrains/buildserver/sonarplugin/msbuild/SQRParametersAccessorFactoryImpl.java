package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.SQRParametersAccessor;
import jetbrains.buildserver.sonarplugin.SQRParametersUtil;
import org.jetbrains.annotations.NotNull;

public class SQRParametersAccessorFactoryImpl implements SQRParametersAccessorFactory {
    public SQRParametersAccessor createAccessor(@NotNull final BuildRunnerContext runnerContext) {
        return new SQRParametersAccessor(SQRParametersUtil.mergeParameters(runnerContext.getBuild().getSharedConfigParameters(), runnerContext.getRunnerParameters()));
    }
}
