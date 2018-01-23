package jetbrains.buildserver.sonarplugin;

public interface SonarQubeKeysProvider {
    String getProjectHome();
    String getHostUrl();
    String getJdbcUrl();
    String getJdbcUsername();
    String getJdbcPassword();
    String getProjectKey();
    String getProjectName();
    String getProjectVersion();
    String getSources();
    String getTests();
    String getBinaries();
    String getJavaBinaries();
    String getModules();
    String getPassword();
    String getLogin();
}
