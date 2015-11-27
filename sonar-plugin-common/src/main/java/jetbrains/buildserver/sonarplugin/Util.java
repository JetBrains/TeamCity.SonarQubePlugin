package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Andrey Titov on 4/7/14.
 *
 * Utility methods used across the plugin
 */
public final class Util {
    private Util() {
    }

    /**
     * <p>Closes closable resource ignoring exception. Does nothing when null is passed</p>
     * @param fw Resource to close.
     */
    public static void close(@Nullable final Closeable fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    public static boolean isSonarRunner(@NotNull final String runType) {
        return Constants.RUNNER_TYPE.equals(runType);
    }

    /**
     * @param str String to check
     * @return true if str is null or empty (zero length or spaces only)
     */
    public static boolean isEmpty(@Nullable final String str) {
        return str == null || str.trim().isEmpty();
    }
}
