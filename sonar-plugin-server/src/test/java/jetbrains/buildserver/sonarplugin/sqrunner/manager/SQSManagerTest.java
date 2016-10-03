package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import org.assertj.core.api.Condition;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andrey Titov on 30.09.16.
 */
@Test
public class SQSManagerTest {
    private SProject myRoot;
    private SProject myProject;
    private SQSInfo myRootServerInfo;
    private SQSInfo myServerInfo;
    private ProjectSettingsManager mySettingsManager;
    @NotNull
    private final String myProjectId = "projectId";
    @NotNull
    private final String myRootProjectId = "_Root";
    @NotNull
    private final String myRootServerId = "rootServerId";
    @NotNull
    private final String myServerId = "serverId";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        myRoot = mock(SProject.class);
        when(myRoot.getProjectId()).thenReturn(myRootProjectId);
        when(myRoot.getParentProject()).thenReturn(null);

        myProject = mock(SProject.class);
        when(myProject.getProjectId()).thenReturn(myProjectId);
        when(myProject.getParentProject()).thenReturn(myRoot);

        mySettingsManager = mock(ProjectSettingsManager.class);

        myServerInfo = mockSQSInfo(mySettingsManager, myServerId, myProjectId);

        myRootServerInfo = mockSQSInfo(mySettingsManager, myRootServerId, myRootProjectId);
    }

    public void test_own() {
        final SQSManager sqsManager = new SQSManagerImpl(mySettingsManager);

        then(sqsManager.getAvailableServers(SQSManager.ProjectAccessor.single(myProject)))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myServerId), "Should be one " + myServerId));
        then(sqsManager.getAvailableServers(SQSManager.ProjectAccessor.single(myRoot)))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "Should be one " + myRootServerId));

        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myProject), myServerId)).isNotNull().isSameAs(myServerInfo);
        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myProject), "nonExisting")).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myProject), myRootServerId)).isNull();

        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myRoot), myServerId)).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myRoot), "nonExisting")).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.single(myRoot), myRootServerId)).isNotNull().isSameAs(myRootServerInfo);
    }

    public void test_recurse() {
        final SQSManager sqsManager = new SQSManagerImpl(mySettingsManager);

        then(sqsManager.getAvailableServers(SQSManager.ProjectAccessor.recurse(myProject)))
                .hasSize(2)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myServerId), "Should be one " + myServerId))
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "Should be one " + myServerId));
        then(sqsManager.getAvailableServers(SQSManager.ProjectAccessor.single(myRoot)))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "containing %s", myRootServerId));

        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myProject), myServerId)).isNotNull().isSameAs(myServerInfo);
        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myProject), "nonExisting")).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myProject), myRootServerId)).isNotNull().isSameAs(myRootServerInfo);

        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myRoot), myServerId)).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myRoot), "nonExisting")).isNull();
        then(sqsManager.findServer(SQSManager.ProjectAccessor.recurse(myRoot), myRootServerId)).isNotNull().isSameAs(myRootServerInfo);
    }

    @NotNull
    private static SQSInfo mockSQSInfo(@NotNull final ProjectSettingsManager settingsManager,
                                       @NotNull final String serverId,
                                       @NotNull final String inProject) {
        final SQSProjectSettings t = new SQSProjectSettings();
        final SQSInfo modifiedServer = new XMLBasedSQSInfo(serverId, null, null, null, null, null, null, null);
        t.setInfo(serverId, modifiedServer);
        when(settingsManager.getSettings(inProject, SQSManagerImpl.SQS_MANAGER_KEY)).thenReturn(t);
        return modifiedServer;
    }
}