package jetbrains.buildserver.sonarplugin.msbuild.tool;

import jetbrains.buildServer.tools.ToolTypeAdapter;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SonarQubeMSBuildToolType extends ToolTypeAdapter {
    @NotNull
    @Override
    public String getType() {
        return SQMSConstants.SONAR_QUBE_MSBUILD_TOOL_TYPE_ID;
    }

    @Nullable
    @Override
    public String getValidPackageDescription() {
        return "Specify path to the SonarQube MSBuild Scanner zip package, eg: sonar-scanner-msbuild.4.0.2.892.zip";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SonarQube MSBuild Scanner";
    }

    @NotNull
    @Override
    public String getShortDisplayName() {
        return "SonarQube MSBuild Scanner";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Tool to run SonarQube analyzis during the build with MSBuild";
    }

    @Override
    public boolean isSupportDownload() {
        return false;
    }

    @Override
    public boolean isSupportUpload() {
        return true;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isServerOnly() {
        return false;
    }

    @Override
    public boolean isCountUsages() {
        return false;
    }
}
