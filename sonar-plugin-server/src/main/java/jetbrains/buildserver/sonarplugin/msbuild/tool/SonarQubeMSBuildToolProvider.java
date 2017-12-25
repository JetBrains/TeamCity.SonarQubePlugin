package jetbrains.buildserver.sonarplugin.msbuild.tool;

import jetbrains.buildServer.tools.ServerToolProviderAdapter;
import jetbrains.buildServer.tools.ToolType;
import org.jetbrains.annotations.NotNull;

public class SonarQubeMSBuildToolProvider extends ServerToolProviderAdapter {

    private static final SonarQubeMSBuildToolType TOOL_TYPE = new SonarQubeMSBuildToolType();

    @NotNull
    @Override
    public ToolType getType() {
        return TOOL_TYPE;
    }
}
