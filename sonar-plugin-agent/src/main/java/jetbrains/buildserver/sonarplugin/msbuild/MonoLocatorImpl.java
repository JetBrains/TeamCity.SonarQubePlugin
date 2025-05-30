

package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildServer.util.OSType;
import org.jetbrains.annotations.NotNull;

public class MonoLocatorImpl implements MonoLocator {
    private final OSType os;

    public MonoLocatorImpl(OSType os) {
        this.os = os;
    }

    @NotNull
    @Override
    public String getMono() {
        return "mono";
    }

    @Override
    public boolean isMono() {
        return os != OSType.WINDOWS;
    }
}