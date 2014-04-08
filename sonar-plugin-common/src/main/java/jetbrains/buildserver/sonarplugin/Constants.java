package jetbrains.buildserver.sonarplugin;

/**
 * Created by linfar on 4/2/14.
 */
public final class Constants {
    public static final String RUNNER_TYPE = "sonar-plugin";
    public static final String PLUGIN_NAME = "sonar-plugin";
    public static final String SONAR_SERVER_URL_FILENAME = "sonar_server.txt";
    public static final String SONAR_SERVER_URL_ARTIF_LOCATION = ".teamcity/sonar/";
    public static final String SONAR_SERVER_URL_ARTIF_LOCATION_FULL = SONAR_SERVER_URL_ARTIF_LOCATION + SONAR_SERVER_URL_FILENAME;
    public static final String SONAR_PROJECT_MODULES = "sonarProjectModules";
    public static final String SONAR_PROJECT_SOURCES = "sonarProjectSources";
    public static final String SONAR_PROJECT_VERSION = "sonarProjectVersion";
    public static final String SONAR_PROJECT_KEY = "sonarProjectKey";
    public static final String SONAR_PROJECT_NAME = "sonarProjectName";

    private Constants() {
    }
}
