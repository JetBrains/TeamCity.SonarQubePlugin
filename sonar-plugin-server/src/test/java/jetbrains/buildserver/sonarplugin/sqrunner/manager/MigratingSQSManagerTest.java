package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import jetbrains.buildserver.sonarplugin.manager.MigratingSQSManager;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.manager.projectfeatures.SQSManagerProjectFeatures;
import jetbrains.buildserver.sonarplugin.manager.projectsettings.SQSManagerImpl;
import org.assertj.core.api.BDDAssertions;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by linfar on 03.10.16.
 *
 * Test for MigratingSQSManager
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
        final TestUtil.Projects projects = TestUtil.createProjects(myRootProjectId, myProjectId);
        myRoot = projects.myRoot;
        myProject = projects.myProject;
    }

    public void test() throws IOException {
        ProjectSettingsManager settingsManager = mock(ProjectSettingsManager.class);

        String serverId = "serverId";
        SQSInfo serverInfo = SQSManagerTest.mockSQSInfo(settingsManager, serverId, myProjectId);

        String rootServerId = "rootServerId";
        SQSInfo rootServerInfo = SQSManagerTest.mockSQSInfo(settingsManager, rootServerId, myRootProjectId);

        SQSManagerImpl SQSManager = new SQSManagerImpl(settingsManager);

        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);

        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(SQSManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

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
        when(sqsManager.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(any, null, ""));

        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        when(sqsManagerProjectFeatures.addServer(myProject, any)).thenReturn(new SQSManager.SQSActionResult(null, any, ""));

        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

        migratingSQSManager.addServer(myProject, any);
        verify(sqsManagerProjectFeatures, times(1)).addServer(myProject, any);
        verify(sqsManager, never()).addServer(any(), any());
    }

    public void should_remove_from_old_on_edit() throws IOException {
        final BaseSQSInfo any = new BaseSQSInfo("any");

        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        when(sqsManagerProjectFeatures.getServer(myProject, "any")).thenReturn(null);
        when(sqsManagerProjectFeatures.editServer(myProject, any)).thenReturn(new SQSManager.SQSActionResult(any, any, ""));

        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        when(sqsManager.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(any, null, ""));
        when(sqsManager.getServer(myProject, "any")).thenReturn(any);

        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

        migratingSQSManager.editServer(myProject, any);
        verify(sqsManagerProjectFeatures, times(1)).addServer(myProject, any);
        verify(sqsManager, times(1)).removeServer(myProject, "any");
    }

    public void should_add_to_new_when_edit() throws IOException {
        final BaseSQSInfo any = new BaseSQSInfo("any");

        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        when(sqsManager.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(any, null, ""));
        when(sqsManager.getServer(myProject, "any")).thenReturn(any);

        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        when(sqsManagerProjectFeatures.getServer(myProject, "any")).thenReturn(null);

        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));
        migratingSQSManager.editServer(myProject, any);

        verify(sqsManagerProjectFeatures, times(1)).addServer(myProject, any);
        verify(sqsManager, times(1)).removeServer(myProject, "any");
    }

    public void should_remove_from_both() throws IOException {
        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

        final BaseSQSInfo any = new BaseSQSInfo("any");

        when(sqsManagerProjectFeatures.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(any, null, ""));
        when(sqsManager.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(any, null, ""));

        migratingSQSManager.removeServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).removeServer(myProject, "any");
        verify(sqsManager, times(1)).removeServer(myProject, "any");

        reset(sqsManagerProjectFeatures, sqsManager);
        when(sqsManagerProjectFeatures.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(null, null, "", true));
        when(sqsManager.removeServer(any(), any())).thenReturn(new SQSManager.SQSActionResult(null, null, "", true));

        migratingSQSManager.removeServer(myProject, "any");
        verify(sqsManagerProjectFeatures, times(1)).removeServer(myProject, "any");
        verify(sqsManager, times(1)).removeServer(myProject, "any");
    }

    public void should_get_from_both() throws IOException {
        final SQSManagerImpl sqsManager = mock(SQSManagerImpl.class);
        final SQSManagerProjectFeatures sqsManagerProjectFeatures = mock(SQSManagerProjectFeatures.class);
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

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
        final MigratingSQSManager migratingSQSManager = new MigratingSQSManager(sqsManager, sqsManagerProjectFeatures, mock(ConfigActionFactory.class));

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