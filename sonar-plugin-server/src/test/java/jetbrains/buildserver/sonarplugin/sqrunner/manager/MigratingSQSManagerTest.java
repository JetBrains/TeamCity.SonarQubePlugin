package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectfeatures.SQSManagerProjectFeatures;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.projectsettings.SQSManagerImpl;
import org.assertj.core.api.BDDAssertions;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by linfar on 03.10.16.
 */
@Test
public class MigratingSQSManagerTest {
    private SProject myRoot;
    private SProject myProject;
    @NotNull
    private final String myProjectId = "projectId";
    @NotNull
    private final String myRootProjectId = "_Root";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        myRoot = mock(SProject.class);
        when(myRoot.getProjectId()).thenReturn(myRootProjectId);
        when(myRoot.getParentProject()).thenReturn(null);

        myProject = mock(SProject.class);
        when(myProject.getProjectId()).thenReturn(myProjectId);
        when(myProject.getParentProject()).thenReturn(myRoot);
    }

    public void test() throws IOException {
        ProjectSettingsManager settingsManager = mock(ProjectSettingsManager.class);

        String serverId = "serverId";
        SQSInfo serverInfo = SQSManagerTest.mockSQSInfo(settingsManager, serverId, myProjectId);

        String rootServerId = "rootServerId";
        SQSInfo rootServerInfo = SQSManagerTest.mockSQSInfo(settingsManager, rootServerId, myRootProjectId);

        SQSManagerImpl SQSManager = new SQSManagerImpl(settingsManager);

        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);

        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(SQSManager, sqsManagerProjectFeatures);

        BDDAssertions.then(migratingSQSManager.getOwnServer(myRoot, serverId)).isNull();
        BDDAssertions.then(migratingSQSManager.getOwnServer(myRoot, rootServerId)).isNotNull().isSameAs(rootServerInfo);

        BDDAssertions.then(migratingSQSManager.getOwnServer(myProject, serverId)).isNotNull().isSameAs(serverInfo);
        BDDAssertions.then(migratingSQSManager.getOwnServer(myProject, rootServerId)).isNull();

        final BaseSQSInfo replacement = new BaseSQSInfo("newServer");
        when(sqsManagerProjectFeatures.getOwnServer(myProject, serverId)).thenReturn(replacement);
        BDDAssertions.then(migratingSQSManager.getOwnServer(myProject, serverId)).isNotNull().isSameAs(replacement);
    }

    public void should_not_add_to_old() throws IOException {
        final BaseSQSInfo any = new BaseSQSInfo("any");

        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        migratingSQSManager.addServer(myProject, any);
        verify(sqsManagerProjectFeatures, times(1)).addServer(myProject, any);
        verify(sqsManager, never()).addServer(any(), any());
    }

    public void should_remove_from_old_on_edit() throws IOException {
        final BaseSQSInfo any = new BaseSQSInfo("any");

        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        when(sqsManagerProjectFeatures.getServer(myProject, "any")).thenReturn(any);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        migratingSQSManager.editServer(myProject, "any", any);
        verify(sqsManagerProjectFeatures, times(1)).editServer(myProject, "any", any);
        verify(sqsManager, times(1)).removeIfExists(myProject, "any");
    }

    public void should_add_to_new_when_edit() throws IOException {
        final BaseSQSInfo any = new BaseSQSInfo("any");

        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        when(sqsManagerProjectFeatures.getServer(myProject, "any")).thenReturn(null);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        migratingSQSManager.editServer(myProject, "any", any);
        verify(sqsManagerProjectFeatures, times(1)).addServer(myProject, any);
        verify(sqsManager, times(1)).removeIfExists(myProject, "any");
    }

    public void should_remove_from_both() throws IOException {
        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        final BaseSQSInfo any = new BaseSQSInfo("any");

        when(sqsManagerProjectFeatures.removeIfExists(any(), any())).thenReturn(any);
        when(sqsManager.removeIfExists(any(), any())).thenReturn(any);

        migratingSQSManager.removeIfExists(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).removeIfExists(myProject, "any");
        verify(sqsManager, times(1)).removeIfExists(myProject, "any");

        reset(sqsManagerProjectFeatures, sqsManager);
        when(sqsManagerProjectFeatures.removeIfExists(any(), any())).thenReturn(null);
        when(sqsManager.removeIfExists(any(), any())).thenReturn(null);

        migratingSQSManager.removeIfExists(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).removeIfExists(myProject, "any");
        verify(sqsManager, times(1)).removeIfExists(myProject, "any");
    }

    public void should_get_from_both() throws IOException {
        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        migratingSQSManager.getAvailableServers(myProject);
        verify(sqsManagerProjectFeatures, times(1)).getAvailableServers(myProject);
        verify(sqsManager, times(1)).getAvailableServers(myProject);

        migratingSQSManager.getOwnAvailableServers(myProject);
        verify(sqsManagerProjectFeatures, times(1)).getOwnAvailableServers(myProject);
        verify(sqsManager, times(1)).getOwnAvailableServers(myProject);

        migratingSQSManager.getServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).getServer(myProject, "any");
        verify(sqsManager, times(1)).getServer(myProject, "any");

        migratingSQSManager.getOwnServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).getOwnServer(myProject, "any");
        verify(sqsManager, times(1)).getOwnServer(myProject, "any");
    }

    public void should_get_from_new_when_available() {
        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures);

        final BaseSQSInfo any = new BaseSQSInfo("any");
        when(sqsManagerProjectFeatures.getServer(myProject, "any")).thenReturn(any);
        when(sqsManagerProjectFeatures.getOwnServer(myProject, "any")).thenReturn(any);

        migratingSQSManager.getServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).getServer(myProject, "any");
        verify(sqsManager, never()).getServer(any(), any());

        migratingSQSManager.getOwnServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).getOwnServer(myProject, "any");
        verify(sqsManager, never()).getOwnServer(any(), any());
    }
}