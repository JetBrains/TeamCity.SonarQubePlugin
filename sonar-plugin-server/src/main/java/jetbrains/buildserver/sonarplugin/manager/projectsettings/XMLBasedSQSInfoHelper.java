package jetbrains.buildserver.sonarplugin.manager.projectsettings;

import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by Andrey Titov on 7/10/14.
 */
public class XMLBasedSQSInfoHelper {
    private XMLBasedSQSInfoHelper() {
    }

    public static SQSInfo createServerInfo(@Nullable final String id,
                                           @Nullable final String name,
                                           @Nullable final String url,
                                           @Nullable final String login,
                                           @Nullable final String password,
                                           @Nullable final String dbUrl,
                                           @Nullable final String dbUsername,
                                           @Nullable final String dbPassword) {
        return new XMLBasedSQSInfo(id == null ? UUID.randomUUID().toString() : id, name, url, login, password, dbUrl, dbUsername, dbPassword);
    }

    public static SQSInfo createServerInfo(@Nullable final String id) {
        return new XMLBasedSQSInfo(id == null ? UUID.randomUUID().toString() : id, null, null, null, null, null, null, null);
    }
}
