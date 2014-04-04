package jetbrains.buildserver.sonarplugin;

/**
 * Created by linfar on 4/2/14.
 */
public final class Constants {
    public static final String RUNNER_TYPE = "sonar-runner";
    public static final String PLUGIN_NAME = "sonar-plugin";
    public static final String SONAR_SERVER_URL_FILENAME = "sonar_server.txt";
    public static final String SONAR_SERVER_URL_ARTIF_LOCATION = ".teamcity/sonar/";
    public static final String SONAR_SERVER_URL_ARTIF_LOCATION_FULL = SONAR_SERVER_URL_ARTIF_LOCATION + SONAR_SERVER_URL_FILENAME;

    private Constants() {
    }
}
