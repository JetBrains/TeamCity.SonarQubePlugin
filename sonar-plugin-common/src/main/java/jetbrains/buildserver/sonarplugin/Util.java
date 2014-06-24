package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
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
            }
        }
    }

    /**
     * <p>Checks whether directory exists. Tries to create it if the directory doesn't exist.</p>
     * @param dir directory to check
     * @return true when directory exists or was created
     */
    public static boolean assureDirExistence(final @NotNull File dir) {
        return !dir.getParentFile().exists() && dir.getParentFile().mkdirs();
    }
}
