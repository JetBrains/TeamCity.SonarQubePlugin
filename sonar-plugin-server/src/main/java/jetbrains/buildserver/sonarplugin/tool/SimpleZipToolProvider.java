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

package jetbrains.buildserver.sonarplugin.tool;

import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.ToolType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;

public interface SimpleZipToolProvider {
    @NotNull
    Path getBundledVersionsRoot();

    @NotNull
    String getName();

    @NotNull
    String getPackedZipPattern();

    @NotNull
    String getPackedDirPattern();

    @NotNull
    ToolType getToolType();

    @NotNull
    String getVersionPattern();

    @NotNull
    GetPackageVersionResult parseVersion(final Path toolPackage, final String version) throws Exception;

    @NotNull
    GetPackageVersionResult tryParsePackedPackage(@NotNull final Path path, @NotNull final Matcher matcher);

    @NotNull
    String getDefaultBundledVersion();

    @NotNull
    GetPackageVersionResult describeBrokenPackage();

    void validatePackedTool(@NotNull final Path toolPackage) throws ToolException;

    void layoutContents(@NotNull Path toolPath, @NotNull Path targetPath) throws ToolException;
}
