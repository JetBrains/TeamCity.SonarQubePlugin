package jetbrains.buildserver.sonarplugin.sqrunner.manager.factories;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSManagerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by linfar on 03.10.16.
 */
public class SQSManagerFactory {
    public SQSManager createSQSManager(@NotNull final ProjectSettingsManager projectSettingsManager) {
        return new SQSManagerImpl(projectSettingsManager);
    }
}
