

[![official JetBrains project](https://jb.gg/badges/official.svg)](https://github.com/JetBrains#jetbrains-on-github) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Build Status](https://teamcity.jetbrains.com/app/rest/builds/buildType:TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Develop/statusIcon.png)](https://teamcity.jetbrains.com/buildConfiguration/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Develop)


 TeamCity SonarQube plugin
 ===========================

 This plugin allows configuring and running SonarQube analysis  on the TeamCity server.

## 1. Downloading binaries
 
 The latest build of the plugin is available on the public TeamCity server and can be downloaded from the public TeamCity server:
  * [for TeamCity 2020.1+](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build20201x/.lastPinned/sonar-plugin.zip)
  * [for TeamCity 2017.1+](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build20171x/.lastPinned/sonar-plugin.zip)
  * [for TeamCity 10](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build100x/.lastPinned/sonar-plugin.zip)
  * [for TeamCity 9](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build90x91x/.lastPinned/sonar-plugin.zip)

 ## 2. Building sources

 From the TeamCity build targeted version bundle, copy into `repository/lib` the JAR file(s) from `webapps/ROOT/WEB-INF/lib/`:
 `mvn install:install-file -DgroupId=org.jetbrains.teamcity -Dpackaging=jar -DartifactId=server-tools -Dversion=2019.2 -Dfile=./.idea_artifacts/web_deployment_debug/WEB-INF/lib/server-tools.jar`

 - `server-tools.jar`
 - `common-tools.jar` (if exist, useless since TeamCity v2019.x)

 And run the following command in the root of the checked out repository:
 
    mvn clean package

 ## 3. Installing
 
 Install the plugin as described in the [TeamCity documentation](https://www.jetbrains.com/help/teamcity/installing-additional-plugins.html).


## 4. Key features

 * **Build step to run SonarQube Runner analysis**

 The most used properties can be configured via the TeamCity UI in a convenient way. Maven test results and JaCoCo code coverage results will be sent to the SonarQube Server automatically.

 * **Connections to SonarQube Server locations to send information from the SonarQube Runner**

 Any number of SonarQube Server connections can be defined for a project. A connection can be used in any Build Configuration under the project including those from subprojects.

 The 'Edit project' permission is required to manage SonarQube servers (add/edit/remove, and view account).

 * **SonarQube Branches & Pull Requests Build Feature**
 
 [Branches](https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/branch-analysis/introduction/) and [Pull-Requests](https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/pull-request-analysis/introduction/) analysis parameters automatically provided on build from VCS build information, using `SONARQUBE_SCANNER_PARAMS` environment variable. It requires TeamCity v2019.2 and SonarQube [Developer Edition or above](https://www.sonarsource.com/plans-and-pricing/).

 * **Build Breaker plugin integration**

 TeamCity will add build problems according to the results of the Build Breaker. TeamCity will look for the Build Breaker messages in the build log and parse them to produce TeamCity Build Problems.

 
 
 