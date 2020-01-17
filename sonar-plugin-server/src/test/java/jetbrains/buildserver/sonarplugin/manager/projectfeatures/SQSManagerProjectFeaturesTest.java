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

package jetbrains.buildserver.sonarplugin.manager.projectfeatures;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import jetbrains.buildserver.sonarplugin.sqrunner.manager.TestUtil;
import org.assertj.core.api.Condition;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by linfar on 05.10.16.
 */
@Test
public class SQSManagerProjectFeaturesTest {
    private SProject myRoot;
    private SProject myProject;
    private SQSManagerProjectFeatures mySqsManagerProjectFeatures;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        final TestUtil.Projects projects = TestUtil.createProjects("_Root", "projectId");
        myRoot = projects.myRoot;
        myProject = projects.myProject;

        mySqsManagerProjectFeatures = new SQSManagerProjectFeatures();
    }

    public void test_empty() {
        then(mySqsManagerProjectFeatures.getAvailableServers(myProject)).isEmpty();
        then(mySqsManagerProjectFeatures.getAvailableServers(myRoot)).isEmpty();
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myProject)).isEmpty();
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myRoot)).isEmpty();
    }

    public void test_single_child() {
        final List<SProjectFeatureDescriptor> features = Collections.singletonList(mockProjectFeature("id", BaseSQSInfo.NAME, "InChild"));
        when(myProject.getOwnFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(features);
        when(myProject.getOwnFeatures()).thenReturn(features);
        when(myProject.getAvailableFeatures()).thenReturn(features);
        when(myProject.getAvailableFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(features);

        then(mySqsManagerProjectFeatures.getAvailableServers(myProject)).hasSize(1);
        then(mySqsManagerProjectFeatures.getAvailableServers(myRoot)).isEmpty();
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myProject)).hasSize(1);
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myRoot)).isEmpty();

        then(mySqsManagerProjectFeatures.getServer(myProject, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InChild".equals(sqsInfo.getName()), "having InChild name"));
        then(mySqsManagerProjectFeatures.getOwnServer(myProject, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InChild".equals(sqsInfo.getName()), "having InChild name"));
    }

    public void test_single_parent() {
        final List<SProjectFeatureDescriptor> features = Collections.singletonList(mockProjectFeature("id", BaseSQSInfo.NAME, "InRoot"));
        when(myRoot.getOwnFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(features);
        when(myRoot.getOwnFeatures()).thenReturn(features);
        when(myRoot.getAvailableFeatures()).thenReturn(features);
        when(myRoot.getAvailableFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(features);
        when(myProject.getAvailableFeatures()).thenReturn(features);
        when(myProject.getAvailableFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(features);

        then(mySqsManagerProjectFeatures.getAvailableServers(myProject)).hasSize(1);
        then(mySqsManagerProjectFeatures.getAvailableServers(myRoot)).hasSize(1);
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myProject)).hasSize(0);
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myRoot)).hasSize(1);

        then(mySqsManagerProjectFeatures.getServer(myProject, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InRoot".equals(sqsInfo.getName()), "having InRoot name"));
        then(mySqsManagerProjectFeatures.getOwnServer(myRoot, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InRoot".equals(sqsInfo.getName()), "having InRoot name"));
        then(mySqsManagerProjectFeatures.getServer(myRoot, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InRoot".equals(sqsInfo.getName()), "having InRoot name"));
    }

    public void test_same_id_in_parent() {
        final SProjectFeatureDescriptor feature1 = mockProjectFeature("id", BaseSQSInfo.NAME, "InRoot");
        final List<SProjectFeatureDescriptor> parentFeatures = Collections.singletonList(feature1);
        final SProjectFeatureDescriptor feature2 = mockProjectFeature("id", BaseSQSInfo.NAME, "InChild");
        final List<SProjectFeatureDescriptor> childFeatures = Collections.singletonList(feature2);

        final List<SProjectFeatureDescriptor> together = new ArrayList<>(childFeatures);
        together.addAll(parentFeatures);

        when(myRoot.getOwnFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(parentFeatures);
        when(myRoot.getOwnFeatures()).thenReturn(parentFeatures);
        when(myRoot.getAvailableFeatures()).thenReturn(parentFeatures);
        when(myRoot.getAvailableFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(parentFeatures);
        when(myProject.getOwnFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(childFeatures);
        when(myProject.getOwnFeatures()).thenReturn(childFeatures);
        when(myProject.getAvailableFeatures()).thenReturn(together);
        when(myProject.getAvailableFeaturesOfType(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE)).thenReturn(together);

        then(mySqsManagerProjectFeatures.getAvailableServers(myProject)).hasSize(2);
        then(mySqsManagerProjectFeatures.getAvailableServers(myRoot)).hasSize(1);
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myProject)).hasSize(1);
        then(mySqsManagerProjectFeatures.getOwnAvailableServers(myRoot)).hasSize(1);

        then(mySqsManagerProjectFeatures.getOwnServer(myProject, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InChild".equals(sqsInfo.getName()), "having InChild name"));
        then(mySqsManagerProjectFeatures.getServer(myProject, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InChild".equals(sqsInfo.getName()), "having InChild name"));
        then(mySqsManagerProjectFeatures.getOwnServer(myRoot, "id")).isNotNull().is(new Condition<>(sqsInfo -> "InRoot".equals(sqsInfo.getName()), "having InRoot name"));
    }

    @NotNull
    private static SProjectFeatureDescriptor mockProjectFeature(@NotNull final String id, @NotNull final String... args) {
        final Map<String, String> map = new HashMap<>(1 + args.length / 2);
        map.put(BaseSQSInfo.ID, id);
        for (int i = 0; i < args.length; i+=2) {
            map.put(args[i], args[i+1]);
        }

        final SProjectFeatureDescriptor mock = mock(SProjectFeatureDescriptor.class);
        when(mock.getType()).thenReturn(SQSManagerProjectFeatures.PROJECT_FEATURE_TYPE);
        when(mock.getParameters()).thenReturn(map);
        return mock;
    }
}