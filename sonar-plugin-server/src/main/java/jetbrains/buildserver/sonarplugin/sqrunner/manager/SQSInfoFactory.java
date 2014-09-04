package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by Andrey Titov on 7/10/14.
 */
public class SQSInfoFactory {
    public static SQSInfo createServerInfo(final @Nullable String id,
                                           final @NotNull String name,
                                           final @NotNull String url,
                                           final @NotNull String login,
                                           final @NotNull String password,
                                           final @Nullable String dbUrl,
                                           final @Nullable String dbUsername,
                                           final @Nullable String dbPassword) {
        return new XMLBasedSQSInfo(id == null ? UUID.randomUUID().toString() : id, name, url, login, password, dbUrl, dbUsername, dbPassword);
    }
}
