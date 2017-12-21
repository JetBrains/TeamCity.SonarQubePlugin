package jetbrains.buildserver.sonarplugin.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * SonarQube Server definition.
 */
public interface SQSInfo {
    @Nullable
    String getUrl();

     @Nullable
    String getLogin();

     @Nullable
    String getPassword();

    @Nullable
    String getJDBCUrl();

    @Nullable
    String getJDBCUsername();

    @Nullable
    String getJDBCPassword();

    @NotNull
    String getId();

    @Nullable
    String getName();

    @Nullable
    String getDescription();

    @NotNull
    Map<String, String> getParameters();
}
