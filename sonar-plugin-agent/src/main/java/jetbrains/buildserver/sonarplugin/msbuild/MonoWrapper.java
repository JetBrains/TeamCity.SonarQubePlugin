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

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.OSType;
import jetbrains.buildserver.sonarplugin.util.Executable;
import jetbrains.buildserver.sonarplugin.util.Execution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MonoWrapper implements Execution {
    @NotNull
    private final OSType myOSType;
    @NotNull
    private final MonoLocator myMonoLocator;

    public MonoWrapper(@NotNull final OSType osType,
                       @NotNull final MonoLocator monoLocator) {
        myOSType = osType;
        myMonoLocator = monoLocator;
    }

    @NotNull
    @Override
    public Executable modify(@NotNull final Executable old, final BuildRunnerContext runnerContext) {
        if (myOSType != OSType.WINDOWS) {
            final List<String> newArgs = new ArrayList<String>(old.myArguments.size() + 1);
            newArgs.add(old.myExecutable);
            newArgs.addAll(old.myArguments);
            return new Executable(myMonoLocator.getMono(), newArgs);
        } else {
            return old;
        }
    }
}
