package jetbrains.buildserver.sonarplugin.msbuild;

import jetbrains.buildserver.sonarplugin.SonarQubeKeysProvider;

public class DotNetSonarQubeKeysProvider implements SonarQubeKeysProvider {
    @Override
    public String getProjectHome() {
        return "/d:project.home=";
    }

    @Override
    public String getHostUrl() {
        return "/d:sonar.host.url=";
    }

    @Override
    public String getJdbcUrl() {
        return "/d:sonar.jdbc.url=";
    }

    @Override
    public String getJdbcUsername() {
        return "/d:sonar.jdbc.username=";
    }

    @Override
    public String getJdbcPassword() {
        return "/d:sonar.jdbc.password=";
    }

    @Override
    public String getProjectKey() {
        return "/k:";
    }

    @Override
    public String getProjectName() {
        return "/n:";
    }

    @Override
    public String getProjectVersion() {
        return "/v:";
    }

    @Override
    public String getSources() {
        return "/d:sonar.sources=";
    }

    @Override
    public String getTests() {
        return "/d:sonar.tests=";
    }

    @Override
    public String getBinaries() {
        return "/d:sonar.binaries=";
    }

    @Override
    public String getJavaBinaries() {
        return "/d:sonar.java.binaries=";
    }

    @Override
    public String getModules() {
        return "/d:sonar.modules=";
    }

    @Override
    public String getPassword() {
        return "/d:sonar.password=";
    }

    @Override
    public String getLogin() {
        return "/d:sonar.login=";
    }
}
