/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import jetbrains.buildserver.sonarplugin.manager.SQSManager;
import jetbrains.buildserver.sonarplugin.manager.projectsettings.SQSManagerImpl;
import jetbrains.buildserver.sonarplugin.manager.projectsettings.SQSProjectSettings;
import jetbrains.buildserver.sonarplugin.manager.projectsettings.XMLBasedSQSInfo;
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
        final SQSManager sqsManager = getSQSManager(mySettingsManager);

        then(sqsManager.getOwnAvailableServers(myProject))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myServerId), "Should be one " + myServerId));
        then(sqsManager.getOwnAvailableServers(myRoot))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "Should be one " + myRootServerId));

        then(sqsManager.getOwnServer(myProject, myServerId)).isNotNull().isSameAs(myServerInfo);
        then(sqsManager.getOwnServer(myProject, "nonExisting")).isNull();
        then(sqsManager.getOwnServer(myProject, myRootServerId)).isNull();

        then(sqsManager.getOwnServer(myRoot, myServerId)).isNull();
        then(sqsManager.getOwnServer(myRoot, "nonExisting")).isNull();
        then(sqsManager.getOwnServer(myRoot, myRootServerId)).isNotNull().isSameAs(myRootServerInfo);
    }

    @NotNull
    public static SQSManagerImpl getSQSManager(ProjectSettingsManager settingsManager) {
        return new SQSManagerImpl(settingsManager);
    }

    public void test_recurse() {
        final SQSManager sqsManager = getSQSManager(mySettingsManager);

        then(sqsManager.getAvailableServers(myProject))
                .hasSize(2)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myServerId), "Should be one " + myServerId))
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "Should be one " + myServerId));
        then(sqsManager.getAvailableServers(myRoot))
                .hasSize(1)
                .areExactly(1, new Condition<>(sqsInfo -> sqsInfo.getId().equals(myRootServerId), "containing %s", myRootServerId));

        then(sqsManager.getServer(myProject, myServerId)).isNotNull().isSameAs(myServerInfo);
        then(sqsManager.getServer(myProject, "nonExisting")).isNull();
        then(sqsManager.getServer(myProject, myRootServerId)).isNotNull().isSameAs(myRootServerInfo);

        then(sqsManager.getServer(myRoot, myServerId)).isNull();
        then(sqsManager.getServer(myRoot, "nonExisting")).isNull();
        then(sqsManager.getServer(myRoot, myRootServerId)).isNotNull().isSameAs(myRootServerInfo);
    }

    @NotNull
    public static SQSInfo mockSQSInfo(@NotNull final ProjectSettingsManager settingsManager,
                                      @NotNull final String serverId,
                                      @NotNull final String inProject) {
        final SQSProjectSettings t = new SQSProjectSettings();
        final SQSInfo modifiedServer = new XMLBasedSQSInfo(serverId, null, null, null, null, null, null, null);
        t.setInfo(serverId, modifiedServer);
        when(settingsManager.getSettings(inProject, SQSManagerImpl.SQS_MANAGER_KEY)).thenReturn(t);
        return modifiedServer;
    }
}