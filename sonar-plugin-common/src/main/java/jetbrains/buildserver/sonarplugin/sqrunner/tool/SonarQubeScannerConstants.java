package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import org.jetbrains.annotations.NotNull;

public final class SonarQubeScannerConstants {
    @NotNull public static final String SONAR_QUBE_SCANNER_TOOL_TYPE_ID = "sonar-qube-scanner";
    @NotNull public static final String SONAR_QUBE_SCANNER_VERSION_PARAMETER = "teamcity.tool.sonarquberunner";

    public SonarQubeScannerConstants() {
        // jsp compatibility
    }

    @NotNull
    public String getToolTypeId() {
        return SONAR_QUBE_SCANNER_TOOL_TYPE_ID;
    }

    @NotNull
    public String getSonarQubeScannerVersionParameter() {
        return SONAR_QUBE_SCANNER_VERSION_PARAMETER;
    }
}
