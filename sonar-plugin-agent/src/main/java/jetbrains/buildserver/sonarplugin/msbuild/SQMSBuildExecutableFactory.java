package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.ExecutableFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;

public class SQMSBuildExecutableFactory implements ExecutableFactory {
    @NotNull private final SonarQubeMSBuildScannerLocator mySonarQubeMSBuildScannerLocator;

    @SuppressWarnings("WeakerAccess")
    public SQMSBuildExecutableFactory(@NotNull final SonarQubeMSBuildScannerLocator sonarQubeMSBuildScannerLocator) {
        mySonarQubeMSBuildScannerLocator = sonarQubeMSBuildScannerLocator;
    }

    @NotNull
    @Override
    public Executable create(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException {
        final String msBuildScannerRoot = mySonarQubeMSBuildScannerLocator.getExecutablePath(runnerContext);

        if (msBuildScannerRoot == null) {
            throw new RunBuildException("No SonarScanner for MSBuild selected");
        }

        final File executableFile = new File(msBuildScannerRoot, "MSBuild.SonarQube.Runner.exe");

        checkExecutable(executableFile);

        return new Executable(executableFile.getAbsolutePath(), Collections.<String>emptyList());
    }

    private void checkExecutable(final File executable) throws RunBuildException {
        if (!executable.exists()) {
            throw new RunBuildException("Incorrect SonarScanner for MSBuild installation: " + executable.getAbsolutePath() + " not found");
        }
        if (!executable.isFile()) {
            throw new RunBuildException("Incorrect SonarScanner for MSBuild installation: " + executable.getAbsolutePath() + " is not a file");
        }
        if (!executable.canExecute()) {
            throw new RunBuildException("Incorrect SonarScanner for MSBuild installation: cannot execute " + executable.getAbsolutePath() + "");
        }
    }
}
