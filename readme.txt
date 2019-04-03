
 TeamCity SonarQube plugin

 This plugin allows configuring and running SonarQube analysis in the TeamCity server.

 1. Downloading binaries
 =====

 The latest build of the plugin is available on the public TeamCity server and can be downloaded from:
  for TeamCity 2017.1 and later: https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build20171x/.lastPinned/sonar-plugin.zip
  for TeamCity 10:  https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build100x/.lastPinned/sonar-plugin.zip
  for prior versions: https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build/.lastPinned/sonar-plugin.zip

 2. Building sources
 =====

 Run the following command in the root of the checked out repository:
 mvn clean package

 3. Installing
 =====

 Install the plugin as described in: https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins

 4. License
 =====

 Apache Lisence 2.0

 5. Key features
 =====

 a. Build step to run SonarQube Runner analysis

 The most used properties can be configured from the TeamCity UI in a comfortable way. Maven test results and JaCoCo code coverage results will be sent to the SonarQube Server automatically.

 b. Configuring SonarQube Server locations to send information from the SonarQube Runner to

 Any number of SonarQube Server connections can be defined for a project. A connection can be used in any Build Configuration under the project including those from subprojects.

 The 'Edit project' permission is required to manage SonarQube servers (add/edit/remove, and view account).

 c. Build Breaker plugin integration

 TeamCity will add build problems according to the results of the Build Breaker. TeamCity will look for the Build Breaker messages in the build log and parse them to produce TeamCity Build Problems.
