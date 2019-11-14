

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Build Status](https://teamcity.jetbrains.com/app/rest/builds/buildType:TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Develop/statusIcon.png)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Develop)


 TeamCity SonarQube plugin
 ===========================

 This plugin allows configuring and running SonarQube analysis  on the TeamCity server.

## 1. Downloading binaries
 
 The latest build of the plugin is available on the public TeamCity server and can be downloaded from the public TeamCity server:
  * [for TeamCity 2017.1+](
http://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build20171x/.lastPinned/sonar-plugin.zip)
  * [for TeamCity 10]( http://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build100x/.lastPinned/sonar-plugin.zip)
  * [for prior versions](http://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build/.lastPinned/sonar-plugin.zip)

 ## 2. Building sources

 From the TeamCity build targeted version bundle, copy into `repository/lib` the JAR file(s) from `webapps/ROOT/WEB-INF/lib/`:
 
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

 * **Build Breaker plugin integration**

 TeamCity will add build problems according to the results of the Build Breaker. TeamCity will look for the Build Breaker messages in the build log and parse them to produce TeamCity Build Problems.
