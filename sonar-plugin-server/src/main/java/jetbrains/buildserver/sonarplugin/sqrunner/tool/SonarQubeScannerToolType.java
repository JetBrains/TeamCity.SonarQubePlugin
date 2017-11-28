package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import jetbrains.buildServer.tools.ToolTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SonarQubeScannerToolType extends ToolTypeAdapter {
    @Nullable
    @Override
    public String getValidPackageDescription() {
        return "Specify path to the SonarQube Scanner jar, eg: sonar-runner.2.4.jar, sonar-scanner-cli-3.0.3.jar";
    }

    @NotNull
    @Override
    public String getType() {
        return SonarQubeScannerConstants.SONAR_QUBE_SCANNER_TOOL_TYPE_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SonarQube Scanner";
    }

    @NotNull
    @Override
    public String getShortDisplayName() {
        return "SonarQube Scanner";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Tool to run SonarQube analyzis during the build";
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
