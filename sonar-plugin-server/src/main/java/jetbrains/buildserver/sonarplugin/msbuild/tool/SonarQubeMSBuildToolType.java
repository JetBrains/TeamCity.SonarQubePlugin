package jetbrains.buildserver.sonarplugin.msbuild.tool;

import jetbrains.buildServer.tools.ToolTypeAdapter;
import org.jetbrains.annotations.NotNull;

public class SonarQubeMSBuildToolType extends ToolTypeAdapter {
    @NotNull
    @Override
    public String getType() {
        return SQMSConstants.SONAR_QUBE_MSBUILD_TOOL_TYPE_ID;
    }
}
