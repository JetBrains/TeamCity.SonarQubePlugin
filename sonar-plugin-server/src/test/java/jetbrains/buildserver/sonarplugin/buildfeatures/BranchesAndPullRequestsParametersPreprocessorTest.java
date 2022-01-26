/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
package jetbrains.buildserver.sonarplugin.buildfeatures;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.version.ServerVersionInfo;

@Test
public class BranchesAndPullRequestsParametersPreprocessorTest {

    private final static String SQS_SYSENV = BranchesAndPullRequestsParametersPreprocessor.SQS_SYSENV;

    public void testNoFeature() {
        Map<String, String> buildParams = new HashMap<>();
        executeProcessor(buildParams, false);
        Assert.assertFalse(buildParams.containsKey(SQS_SYSENV));
    }

    public void testAlreadyDefinedOrMultipleCall() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.url", "https://github.company.com/orga-test/repo-test");
        buildParams.put("teamcity.build.branch.is_default", "false");
        buildParams.put("teamcity.pullRequest.number", "1");
        buildParams.put("teamcity.pullRequest.target.branch", "master");
        buildParams.put("teamcity.pullRequest.title", "Update README.md");

        final String someJson = "{\"status\",\"already defined !\"}";
        buildParams.put(SQS_SYSENV, someJson);

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV), someJson);
    }

    public void testGitHubPrUrlHttp() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.url", "https://github.company.com/orga-test/repo-test");
        buildParams.put("teamcity.build.branch.is_default", "false");
        buildParams.put("teamcity.pullRequest.number", "1");
        buildParams.put("teamcity.pullRequest.target.branch", "master");
        buildParams.put("teamcity.pullRequest.title", "Update README.md");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV),
                "{\"sonar.pullrequest.key\":\"1\",\"sonar.pullrequest.branch\":\"Update README.md\",\"sonar.pullrequest.base\":\"master\",\"sonar.pullrequest.provider\":\"github\",\"sonar.pullrequest.github.repository\":\"orga-test/repo-test\"}");
    }

    @Test
    public void testGitHubPrUrlHttpGitSuffix() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.url", "https://github.company.com/orga-test/repo-test.git");
        buildParams.put("teamcity.build.branch.is_default", "false");
        buildParams.put("teamcity.pullRequest.number", "42");
        buildParams.put("teamcity.pullRequest.target.branch", "master");
        buildParams.put("teamcity.pullRequest.title", "Update README.md");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV),
                "{\"sonar.pullrequest.key\":\"42\",\"sonar.pullrequest.branch\":\"Update README.md\",\"sonar.pullrequest.base\":\"master\",\"sonar.pullrequest.provider\":\"github\",\"sonar.pullrequest.github.repository\":\"orga-test/repo-test\"}");
    }

    @Test
    public void testGitHubPrUrlSsh() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.url", "git@github.company.com:orga-test/repo-test.git");
        buildParams.put("teamcity.build.branch.is_default", "false");
        buildParams.put("teamcity.pullRequest.number", "1");
        buildParams.put("teamcity.pullRequest.target.branch", "master");
        buildParams.put("teamcity.pullRequest.title", "Update README.md");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV),
                "{\"sonar.pullrequest.key\":\"1\",\"sonar.pullrequest.branch\":\"Update README.md\",\"sonar.pullrequest.base\":\"master\",\"sonar.pullrequest.provider\":\"github\",\"sonar.pullrequest.github.repository\":\"orga-test/repo-test\"}");
    }

    @Test
    public void testGitHubBranch() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch", "someBranch");
        buildParams.put("teamcity.build.branch.is_default", "false");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV), "{\"sonar.branch.name\":\"someBranch\",\"sonar.branch.target\":\"master\"}");
    }

    @Test
    public void testTeamCityVersions() throws Exception {
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.3 (build 42)")));

        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo(null)));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2020.1.4 (build 42)")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2020")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.3 (build 42)")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.2.1 (build 71758)")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.2 (build 42)")));
        Assert.assertTrue(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.2")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1.5 (build 66605)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1.4 (build 66526)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1.3 (build 66439)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1.2 (build 66342)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1.1 (build 66192)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2019.1")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.2.4 (build 61678)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.2.3 (build 61544)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.2.2 (build 61245)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.2.1 (build 61078)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.2")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1.5 (build 58744)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1.4 (build 58724)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1.3 (build 58658)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1.2 (build 58537)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1.1 (build 58406)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2018.1 (build 58245)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.2.4 (build 51228)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.2.3 (build 51047)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.2.2 (build 50909)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.2.1 (build 50732)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.2 (build 50574)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1.5 (build 47175)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1.4 (build 47070)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1.3 (build 46961)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1.2 (build 46812)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1.1 (build 46654)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("2017.1 (build 46533)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0.5 (build 42677)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0.4 (build 42538)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0.3 (build 42434)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0.2 (build 42234)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0.1 (build 42078)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("10.0 (build 42002)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.7 (build 37573)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.6 (build 37459)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.5 (build 37377)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.4 (build 37293)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.3 (build 37176)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.2 (build 37168)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1.1 (build 36973)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.1 (build 36973)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0.5 (build 32523)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0.4 (build 32407)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0.3 (build 32334)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0.2 (build 32195)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0.1 (build 32116)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("9.0 (build 32060)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1.5 (build 30240)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1.4 (build 30168)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1.3 (build 30101)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1.2 (build 29993)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1.1 (build 29939)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.1 (build 29879)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.6 (build 27767)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.5 (build 27692)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.4 (build 27540)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.3 (build 27540)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.2 (build 27482)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0.1 (build 27435)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("8.0 (build 27402)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1.5 (build 24400)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1.4 (build 24331)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1.3 (build 24266)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1.2 (build 24170)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1.1 (build 24074)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.1 (build 23907)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.0.4 (build 21474)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.0.3 (build 21424)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.0.2 (build 21349)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.0.1 (build 21326)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("7.0 (build 21241)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.6 (build 18130)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.5 (build 18087)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.4 (build 18046)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.3 (build 17985)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.2 (build 17935)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5.1 (build 17834)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.5 (build 17795)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.0.3 (build 15925)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.0.2 (build 15857)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.0.1 (build 15816)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("6.0 (build 15772)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1.5 (build 13602)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1.4 (build 13550)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1.3 (build 13506)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1.2 (build 13430)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1.1 (build 13398)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.1 (build 13360)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.0.3 (build 10821)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.0.2 (build 10784)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.0.1 (build 10715)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("5.0 (build 10669)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.6 (build 9140)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.5 (build 9103)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.4 (build 9071)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.3 (build 9035)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.2 (build 9029)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5.1 (build 8975)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.5 (build 8944)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.0.2 (build 8222)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.0.1 (build 8171)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("4.0 (build 8080)")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("3.1.2")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("3.1.1")));
        Assert.assertFalse(BranchesAndPullRequestsParametersPreprocessor.isTeamCityMinimalVersion(mockServerVersionInfo("3.1")));
    }

    @Test
    public void testTeamCityVersionOK() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch", "someBranch");
        buildParams.put("teamcity.build.branch.is_default", "false");

        executeProcessor(buildParams, true, "2019.2.1 (build 71758)");

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV), "{\"sonar.branch.name\":\"someBranch\",\"sonar.branch.target\":\"master\"}");
    }

    @Test
    public void testTeamCityVersionKO() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch", "someBranch");
        buildParams.put("teamcity.build.branch.is_default", "false");

        executeProcessor(buildParams, true, "2019.1.5 (build 66605)");

        Assert.assertFalse(buildParams.containsKey(SQS_SYSENV));
    }

    @Test
    public void testGitHubBranchParamPREmpty() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch", "someBranch");
        buildParams.put("teamcity.build.branch.is_default", "false");
        buildParams.put("teamcity.pullRequest.number", "");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV), "{\"sonar.branch.name\":\"someBranch\",\"sonar.branch.target\":\"master\"}");
    }

    @Test
    public void testGitHubBranchLong() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("vcsroot.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch", "feature/someBranch");
        buildParams.put("teamcity.build.branch.is_default", "false");

        executeProcessor(buildParams, true);

        Assert.assertTrue(buildParams.containsKey(SQS_SYSENV));
        Assert.assertEquals(buildParams.get(SQS_SYSENV), "{\"sonar.branch.name\":\"feature/someBranch\",\"sonar.branch.target\":\"master\"}");
    }

    @Test
    public void testGitHubMaster() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("teamcity.build.branch", "master");
        buildParams.put("teamcity.build.branch.is_default", "true");

        executeProcessor(buildParams, true);

        Assert.assertFalse(buildParams.containsKey(SQS_SYSENV));
    }

    @Test
    public void testGitHubMasterLongRef() throws Exception {
        Map<String, String> buildParams = new HashMap<>();
        buildParams.put("teamcity.build.branch", "refs/heads/master");
        buildParams.put("teamcity.build.branch.is_default", "true");

        executeProcessor(buildParams, true);

        Assert.assertFalse(buildParams.containsKey(SQS_SYSENV));
    }

    @Test
    public void testGitHubNoProps() throws Exception {
        Map<String, String> buildParams = new HashMap<>();

        executeProcessor(buildParams, true);

        Assert.assertFalse(buildParams.containsKey(SQS_SYSENV));
    }

    private static Map<String, String> executeProcessor(Map<String, String> buildParams, boolean featureEnabled) {
        return executeProcessor(buildParams, featureEnabled, null);
    }

    private static Map<String, String> executeProcessor(Map<String, String> buildParams, boolean featureEnabled, String teamCityVersion) {
        final Map<String, String> runParameters = new HashMap<>();

        final SRunningBuild build = mock(SRunningBuild.class);
        when(build.getBuildLog()).thenReturn(mock(BuildLog.class));
        if (featureEnabled) {
            final SBuildFeatureDescriptor mockbf = mock(SBuildFeatureDescriptor.class);
            when(mockbf.getType()).thenReturn(BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_TYPE);

            when(build.getBuildFeaturesOfType(BranchesAndPullRequestsBuildFeature.BUILD_FEATURE_TYPE)).thenReturn(Collections.singleton(mockbf));
        }

        BranchesAndPullRequestsParametersPreprocessor processor = spy(BranchesAndPullRequestsParametersPreprocessor.class);
        when(processor.getServerVersionInfo()).thenReturn(mockServerVersionInfo(teamCityVersion));
        processor.fixRunBuildParameters(build, runParameters, buildParams);
        return buildParams;
    }

    /**
     * Simple TeamCity version parser (only for unit test)
     * 
     * @param version String version like '2019.1.5 (build 66605)'
     * @return {@link ServerVersionInfo}
     */
    private static ServerVersionInfo mockServerVersionInfo(String version) {
        if (StringUtils.isEmpty(version)) {
            return null;
        }
        int major = 0;
        int minor = 0;
        String build = "";
        String[] v = version.split("\\.");
        for (int i = 0; i < v.length; i++) {
            String value = v[i];
            if (value.contains("build")) {
                build = value.replaceFirst(".* \\(build ", "").replaceFirst("\\)", "");
                value = value.split(" ")[0];
            }
            if (i == 0) {
                major = Integer.valueOf(value);
            } else if (i == 1) {
                minor = Integer.valueOf(value);
            }
        }
        return new ServerVersionInfo("", "", "", build, new Date(), major, minor);
    }

}
