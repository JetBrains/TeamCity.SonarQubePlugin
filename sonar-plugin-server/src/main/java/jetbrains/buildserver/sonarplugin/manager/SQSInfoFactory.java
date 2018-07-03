package jetbrains.buildserver.sonarplugin.manager;

import jetbrains.buildserver.sonarplugin.manager.projectfeatures.SQSInfoImpl;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by linfar on 04.10.16.
 */
public class SQSInfoFactory {
    public SQSInfo create(@Nullable final String id,
                          @Nullable final String name,
                          @Nullable final String url,
                          @Nullable final String login,
                          @Nullable final String password,
                          @Nullable final String jdbcUrl,
                          @Nullable final String jdbcUsername,
                          @Nullable final String jdbcPassword) {
        return new SQSInfoImpl(id == null ? UUID.randomUUID().toString() : id, name, url, login, password, jdbcUrl, jdbcUsername, jdbcPassword);
    }
}
