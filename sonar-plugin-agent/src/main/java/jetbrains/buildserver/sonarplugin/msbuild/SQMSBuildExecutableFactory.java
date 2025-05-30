

package jetbrains.buildserver.sonarplugin.msbuild;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.ExecutableFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQMSBuildExecutableFactory implements ExecutableFactory {
    private static final Logger LOG = Logger.getInstance(SQMSBuildExecutableFactory.class);

    @NotNull private final SonarQubeMSBuildScannerLocator mySonarQubeMSBuildScannerLocator;
    private final MonoLocator myMonoLocator;

    @SuppressWarnings("WeakerAccess")
    public SQMSBuildExecutableFactory(@NotNull final SonarQubeMSBuildScannerLocator sonarQubeMSBuildScannerLocator, MonoLocator myMonoLocator) {
        mySonarQubeMSBuildScannerLocator = sonarQubeMSBuildScannerLocator;
        this.myMonoLocator = myMonoLocator;
    }

    @NotNull
    @Override
    public Executable create(@NotNull final BuildRunnerContext runnerContext) throws RunBuildException {
        final String msBuildScannerRoot = mySonarQubeMSBuildScannerLocator.getExecutablePath(runnerContext);

        if (msBuildScannerRoot == null) {
            throw new RunBuildException("No SonarScanner for MSBuild selected");
        }
        File executableFile = findExecutable(msBuildScannerRoot);
        if (executableFile == null) {
            throw new RunBuildException("No SonarScanner for MSBuild executable found");
        }

        return new Executable(executableFile.getAbsolutePath(), Collections.<String>emptyList());
    }

    @Nullable
    private File findExecutable(String msBuildScannerRoot) throws RunBuildException {
        List<String> orderedCommands = new ArrayList<String>() {{
            add("SonarScanner.MSBuild.exe");
            add("SonarQube.Scanner.MSBuild.exe");
            add("MSBuild.SonarQube.Runner.exe");
        }};

        for (String command : orderedCommands) {
            File executableFile = new File(msBuildScannerRoot, command);
            String reason = checkExecutable(executableFile);
            if (reason == null) { // no issues
                return executableFile;
            } else {
                LOG.debug(reason);
            }
        }
        return null;
    }

    private String checkExecutable(final File executable) throws RunBuildException {
        if (!executable.exists()) {
            return "Incorrect SonarScanner for MSBuild installation: " + executable.getAbsolutePath() + " not found";
        }
        if (!executable.isFile()) {
            return "Incorrect SonarScanner for MSBuild installation: " + executable.getAbsolutePath() + " is not a file";
        }
        if (!myMonoLocator.isMono() && !executable.canExecute()) { // executability not need for running in mono
            return "Incorrect SonarScanner for MSBuild installation: cannot execute " + executable.getAbsolutePath();
        }
        return null;
    }
}