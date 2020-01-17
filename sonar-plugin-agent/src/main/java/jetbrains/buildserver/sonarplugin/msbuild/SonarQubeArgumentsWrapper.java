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
