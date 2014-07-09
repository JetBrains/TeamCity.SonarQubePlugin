package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by linfar on 7/9/14.
 */
public interface SQSInfo {
    @Nullable
    String getUrl();

    @Nullable
    String getJDBCUrl();

    @Nullable
    String getJDBCUsername();

    @Nullable
    String getJDBCPassword();

    @Nullable
    String getId();

    public static class ValidationError {
        public final String myError;
        public final String myKey;

        public ValidationError(@NotNull final String myError, @NotNull final String myKey) {
            this.myError = myError;
            this.myKey = myKey;
        }
    }
}
