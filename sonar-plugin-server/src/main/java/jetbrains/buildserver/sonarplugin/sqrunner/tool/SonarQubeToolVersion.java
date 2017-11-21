package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import jetbrains.buildServer.tools.SimpleToolVersion;
import jetbrains.buildServer.tools.ToolType;
import org.jetbrains.annotations.NotNull;

public class SonarQubeToolVersion extends SimpleToolVersion {
    public SonarQubeToolVersion(@NotNull ToolType toolType, @NotNull String versionString, @NotNull String toolId) {
        super(toolType, versionString, toolId);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return getType().getShortDisplayName() + " " + getVersion();
    }
}
