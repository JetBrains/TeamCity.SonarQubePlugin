package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface SonarQubeMSBuildScannerLocator {
    @Nullable
    String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException;
}
