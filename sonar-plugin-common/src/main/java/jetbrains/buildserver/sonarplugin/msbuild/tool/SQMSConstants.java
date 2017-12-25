package jetbrains.buildserver.sonarplugin.msbuild.tool;

import org.jetbrains.annotations.NotNull;

public final class SQMSConstants {
    @NotNull public static final String SONAR_QUBE_MSBUILD_TOOL_TYPE_ID = "sonar-scanner-msbuild";
    @NotNull public static final String SONAR_QUBE_MSBUILD_VERSION_PARAMETER = "teamcity.tool.sonarqubemsbuild";

    @NotNull public static final String SONAR_QUBE_MSBUILD_RUN_TYPE_ID = "sonar-qube-msbuild";
    @NotNull public static final String SONAR_QUBE_MSBUILD_RUN_TYPE_FINISH_ID = "sonar-qube-msbuild-finish";

    public SQMSConstants() {
        // jsp compatibility
    }

    @NotNull
    public String getToolTypeId() {
        return SONAR_QUBE_MSBUILD_TOOL_TYPE_ID;
    }

    @NotNull
    public String getSonarQubeScannerVersionParameter() {
        return SONAR_QUBE_MSBUILD_VERSION_PARAMETER;
    }
}
