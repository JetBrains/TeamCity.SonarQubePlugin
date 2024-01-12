

package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import org.jetbrains.annotations.NotNull;

public final class SonarQubeScannerConstants {
    // Used in editSQRRunParams.jsp
    @NotNull public static final String SONAR_QUBE_SCANNER_TOOL_TYPE_ID = "sonar-qube-scanner";
    @NotNull public static final String SONAR_QUBE_SCANNER_TOOL_TYPE_ID_ALT = "sonar-scanner-cli";
    @NotNull public static final String SONAR_QUBE_SCANNER_VERSION_PARAMETER = "teamcity.tool.sonarquberunner";
}