package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andrey Titov on 30.09.16.
 */
@Test
public class SQSManagerImplTest {
    public void test_simple_own() {
        final ProjectSettingsManager settingsManager = mock(ProjectSettingsManager.class);
        final String projectId = "projectId";

        final SQSProjectSettings t = new SQSProjectSettings();
        final String serverId = "serverId";
        final XMLBasedSQSInfo modifiedServer = new XMLBasedSQSInfo(serverId, null, null, null, null, null, null, null);
        t.setInfo(serverId, modifiedServer);

        when(settingsManager.getSettings(projectId, SQSManagerImpl.SQS_MANAGER_KEY)).thenReturn(t);

        final SProject project = mock(SProject.class);
        when(project.getProjectId()).thenReturn(projectId);

        final SQSManagerImpl sqsManager = new SQSManagerImpl(settingsManager);

        final List<SQSInfo> availableServers = sqsManager.getAvailableServers(SQSManager.ProjectAccessor.single(project));
        then(availableServers).hasSize(1);
        then(availableServers.get(0).getId()).isSameAs(serverId);

        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(project), serverId)).isNotNull().isSameAs(modifiedServer);
        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(project), "nonExisting")).isNull();
    }
}