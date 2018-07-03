package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildRunnerSettings;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.parameters.ProcessingResult;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants;
import jetbrains.buildserver.sonarplugin.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQMSBuildFinishServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull private final SQMSBuildFinishRunner mySQMSBuildFinishRunner;
    @NotNull private final OSType myOSType;
    @NotNull private final MonoLocator myMonoLocator;

    public SQMSBuildFinishServiceFactory(@NotNull final SQMSBuildFinishRunner sqmsBuildStartRunner,
                                         @NotNull final OSType osType, @NotNull final MonoLocator monoLocator) {
        mySQMSBuildFinishRunner = sqmsBuildStartRunner;
        myOSType = osType;
        myMonoLocator = monoLocator;
    }

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new SimpleExecute(
                new ExecutionChain(Arrays.asList(new MonoWrapper(myOSType, myMonoLocator), new EndExecution())),
                new SQMSBuildExecutableFactory(new MySonarQubeMSBuildScannerLocator()));
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return mySQMSBuildFinishRunner;
    }

    private static class EndExecution implements Execution {
        @NotNull
        @Override
        public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
            final ArrayList<String> arguments = new ArrayList<String>(old.myArguments);
            arguments.add("end");
            return new Executable(old.myExecutable, arguments);
        }
    }

    private static class MySonarQubeMSBuildScannerLocator implements SonarQubeMSBuildScannerLocator {
        @Nullable
        @Override
        public String getExecutablePath(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException {
            for (BuildRunnerSettings runnerSettings : runnerContext.getBuild().getBuildRunners()) {
                if (runnerSettings.getRunType().equals(SQMSConstants.SONAR_QUBE_MSBUILD_RUN_TYPE_ID)) {

                    final String s = runnerSettings.getConfigParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
                    if (s != null) {
                        final ProcessingResult explicitPath = runnerContext.getParametersResolver().resolve(s);

                        if (explicitPath.isFullyResolved()) return explicitPath.getResult();
                    }

                    final String s1 = runnerSettings.getRunnerParameters().get(SQMSConstants.SONAR_QUBE_MSBUILD_VERSION_PARAMETER);
                    if (s1 != null) {
                        final ProcessingResult resolve = runnerContext.getParametersResolver().resolve(s1);
                        if (resolve.isFullyResolved()) return resolve.getResult();
                    }
                }
            }

            throw new RunBuildException("Cannot find SonarQube MSBuild Scanner start task");
        }
    }
}
