
 TeamCity SonarQube plugin

 This plugin allows confguring and running SonarQube analysis in the TeamCity server.

 1. Downloading binaries 
 =====

 The latest build of the plugin is available on public TeamCity server and could be downloaded from: 
 http://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCitySonarQubePlugin_Build/.lastPinned/sonar-plugin.zip

 2. Building sources
 =====

 Run the following command in the root of checked out repository: 
 mvn clean package

 3. Installing
 =====

 Install the plugin as described in: http://confluence.jetbrains.com/display/TCD8/Installing+Additional+Plugins

 4. License
 =====

 Apache Lisence 2.0

 5. Key features
 =====

 a. Build step to run SonarQube Runner analysis

 The most used properties could be configured from the TeamCity UI in a comfortable way. Maven test results and JaCoCo code coverage results will be sent to the SonarQube Server automatically.

 b. Configuring SonarQube Server locations to send information from the SonarQube Runner to

 Any number of SonarQube Server connections could be defined for a project. A connection could be used in any Build Configuration under the project including those from subprojects.

 c. Build Breaker plugin integration

 TeamCity will add build problems according to the results of the Build Breaker. TeamCity will look for the Build Breaker messages in the build log and parse them to produce TeamCity Build Problems.
