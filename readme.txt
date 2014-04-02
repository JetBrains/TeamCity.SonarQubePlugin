
 TeamCity plugin

 This is an empty project to develop TeamCity plugin.

 1. Implement
 This project contains 3 modules: '<artifactId>-server', '<artifactId>-agent' and '<artifactId>-common'. They will contain code for server and agent parts of your plugin and a common part, available for both (agent and server). When implementing components for server and agent parts, do not forget to update spring context files under 'main/resources/META-INF'. Otherwise your compoment may be not loaded. See TeamCity documentation for details on plugin development.

 2. Build
 Issue 'mvn package' command from the root project to build your plugin. Resulting package <artifactId>.zip will be placed in 'target' directory. 
 
 3. Install
 To install the plugin, put zip archive to 'plugins' dir under TeamCity data directory. If you only changed agent-side code of your plugin, the upgrade will be perfomed 'on the fly' (agents will upgrade when idle). If common or server-side code has changed, restart the server.
