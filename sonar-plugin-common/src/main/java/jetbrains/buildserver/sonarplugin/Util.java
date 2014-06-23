package jetbrains.buildserver.sonarplugin;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by linfar on 4/7/14.
 */
public final class Util {
    private Util() {
    }

    public static void close(Closeable fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * <div>Checks whether directory exists. Tries to create it if the directory doesn't exist. </div>
     * @param dir directory to check
     * @return true when directory exists or was created
     */
    public static boolean assureDirExistence(final @NotNull File dir) {
        return !dir.getParentFile().exists() && dir.getParentFile().mkdirs();
    }
}
