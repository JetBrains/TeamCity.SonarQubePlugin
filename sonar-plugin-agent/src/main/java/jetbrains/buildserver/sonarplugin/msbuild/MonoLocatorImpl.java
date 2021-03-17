package jetbrains.buildserver.sonarplugin.msbuild;

import org.jetbrains.annotations.NotNull;

public class MonoLocatorImpl implements MonoLocator {
    @NotNull
    @Override
    public String getMono() {
        return "mono";
    }
}
