/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
