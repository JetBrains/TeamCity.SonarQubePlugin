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
