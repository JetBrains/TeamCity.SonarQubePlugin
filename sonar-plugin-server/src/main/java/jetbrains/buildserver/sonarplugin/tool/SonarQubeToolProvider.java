

package jetbrains.buildserver.sonarplugin.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SonarQubeToolProvider extends ServerToolProviderAdapter {

    private static final Logger LOG = Logger.getInstance(SonarQubeToolProvider.class.getName());

    public static final String VERSION_GROUP_NAME = "version";
    public static final String BUNDLED_SUBPATH = "bundled";

    @NotNull private final Pattern myPackedSonarQubeScannerRootZipPattern;
    @NotNull private final Pattern myPackedSonarQubeScannerRootDirPattern;
    @NotNull private final String myDefaultBundledVersionString;

    @NotNull private final Pattern myUploadedPackagePattern;

    @NotNull private final SimpleZipToolProvider mySimpleZipToolProvider;

    public SonarQubeToolProvider(@NotNull final SimpleZipToolProvider simpleZipToolProvider) {
        mySimpleZipToolProvider = simpleZipToolProvider;
        myDefaultBundledVersionString = mySimpleZipToolProvider.getDefaultBundledVersion();
        myPackedSonarQubeScannerRootZipPattern = Pattern.compile(mySimpleZipToolProvider.getPackedZipPattern());
        myPackedSonarQubeScannerRootDirPattern = Pattern.compile(mySimpleZipToolProvider.getPackedDirPattern());
        myUploadedPackagePattern = Pattern.compile(mySimpleZipToolProvider.getVersionPattern());
    }

    @NotNull
    @Override
    public ToolType getType() {
        return mySimpleZipToolProvider.getToolType();
    }

    @NotNull
    @Override
    public Collection<InstalledToolVersion> getBundledToolVersions() {
        final Path bundledTools = mySimpleZipToolProvider.getBundledVersionsRoot();
        LOG.warn(" - getBundledToolVersions in " + bundledTools);

        final String error = checkDirectory(bundledTools, "Cannot get bundled " + mySimpleZipToolProvider.getName() + " version");
        if (error != null) {
            LOG.warn(error);
            return Collections.emptyList();
        }

        final List<InstalledToolVersion> res = new ArrayList<>();
        try (final DirectoryStream<Path> contents = Files.newDirectoryStream(bundledTools)) {
            for (Path path : contents) {
                LOG.warn(" - getBundledToolVersions found package " + path);
                final String errorFiles = checkFile(path, "Cannot parse SonarQube Scanner package");
                if (errorFiles != null) {
                    LOG.warn(errorFiles);
                } else {
                    final String fileName = path.getFileName().toString();

                    final Matcher matcher = myPackedSonarQubeScannerRootZipPattern.matcher(fileName);

                    final GetPackageVersionResult result = mySimpleZipToolProvider.tryParsePackedPackage(path, matcher);
                    if (result.getToolVersion() != null) {
                        res.add(new SimpleInstalledToolVersion(result.getToolVersion(), null, null, path.toFile()));
                    }
                }
            }
        } catch (IOException e) {
            LOG.warnAndDebugDetails("Cannot get bundled " + mySimpleZipToolProvider.getName() + " version due to an error", e);
        }

        return res;
    }

    @NotNull
    @Override
    public GetPackageVersionResult tryGetPackageVersion(@NotNull final File toolPackage) {
        return tryGetPackageVersion(toolPackage.toPath());
    }

    @NotNull
    public GetPackageVersionResult tryGetPackageVersion(@NotNull final Path toolPackage) {
        final Matcher matcher = myUploadedPackagePattern.matcher(toolPackage.getFileName().toString());

        if (matcher.matches()) {
            try {
                return mySimpleZipToolProvider.parseVersion(toolPackage, matcher.group(VERSION_GROUP_NAME));
            } catch (Exception ex) {
                LOG.warn("Cannot read tool package in '" + toolPackage + "'");
            }
        }

        return mySimpleZipToolProvider.describeBrokenPackage();
    }

    @Override
    public void unpackToolPackage(@NotNull final File toolPackage, @NotNull final File targetDirectory) throws ToolException {
        unpackToolPackage(toolPackage.toPath(), targetDirectory.toPath());
    }

    public void unpackToolPackage(@NotNull final Path toolPath, @NotNull final Path targetPath) throws ToolException {
        try {
            Files.createDirectory(targetPath);
        } catch (IOException ignore) {
        }

        {
            final String error = checkDirectory(targetPath, "Cannot unpack " + toolPath + " to " + targetPath);
            if (error != null) {
                LOG.warn(error);
                throw new ToolException(error);
            }
        }

        final Matcher dirMatcher = myPackedSonarQubeScannerRootDirPattern.matcher(targetPath.getFileName().toString());
        if (!dirMatcher.matches()) {
            throw new ToolException("Unexpected target directory name: should match '" + myPackedSonarQubeScannerRootDirPattern.pattern() + "' while got " + targetPath.getFileName().toString());
        }

        mySimpleZipToolProvider.layoutContents(toolPath, targetPath);
    }

    @Nullable
    @Override
    public String getDefaultBundledVersionId() {
        return myDefaultBundledVersionString;
    }

    @Nullable
    private String checkDirectory(@NotNull final Path bundledTool, final String description) {
        String error = checkCommon(bundledTool, description);
        if (error != null) return error;

        if (!Files.isDirectory(bundledTool)) {
            return description + ": '" + bundledTool + "' is not a directory";
        }
        return null;
    }

    @Nullable
    private String checkFile(@NotNull final Path bundledTool, final String description) {
        String error = checkCommon(bundledTool, description);
        if (error != null) return error;

        if (!Files.isRegularFile(bundledTool)) {
            return description + ": '" + bundledTool + "' is not a file";
        }
        return null;
    }

    @Nullable
    private String checkCommon(@NotNull final Path bundledTool, final String description) {
        String error = null;
        if (!Files.exists(bundledTool)) {
            error = description + ": '" + bundledTool + "' expected to exist";
        }
        if (!Files.isReadable(bundledTool)) {
            error = description + ": cannot read '" + bundledTool + "'";
        }
        return error;
    }
}