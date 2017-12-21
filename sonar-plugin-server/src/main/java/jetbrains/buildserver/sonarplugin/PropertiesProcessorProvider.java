package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.serverSide.PropertiesProcessor;

public interface PropertiesProcessorProvider {
    public PropertiesProcessor getRunnerPropertiesProcessor();
}
