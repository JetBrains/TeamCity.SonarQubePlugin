package jetbrains.buildserver.sonarplugin;

public class JavaSonarQubeKeysProvider implements SonarQubeKeysProvider {
    @Override
    public String getProjectHome() {
        return "-Dproject.home=";
    }

    @Override
    public String getHostUrl() {
        return "-Dsonar.host.url=";
    }

    @Override
    public String getJdbcUrl() {
        return "-Dsonar.jdbc.url=";
    }

    @Override
    public String getJdbcUsername() {
        return "-Dsonar.jdbc.username=";
    }

    @Override
    public String getJdbcPassword() {
        return "-Dsonar.jdbc.password=";
    }

    @Override
    public String getProjectKey() {
        return "-Dsonar.projectKey=";
    }

    @Override
    public String getProjectName() {
        return "-Dsonar.projectName=";
    }

    @Override
    public String getProjectVersion() {
        return "-Dsonar.projectVersion=";
    }

    @Override
    public String getSources() {
        return "-Dsonar.sources=";
    }

    @Override
    public String getTests() {
        return "-Dsonar.tests=";
    }

    @Override
    public String getBinaries() {
        return "-Dsonar.binaries=";
    }

    @Override
    public String getJavaBinaries() {
        return "-Dsonar.java.binaries=";
    }

    @Override
    public String getModules() {
        return "-Dsonar.modules=";
    }

    @Override
    public String getPassword() {
        return "-Dsonar.password=";
    }

    @Override
    public String getLogin() {
        return "-Dsonar.login=";
    }
}
