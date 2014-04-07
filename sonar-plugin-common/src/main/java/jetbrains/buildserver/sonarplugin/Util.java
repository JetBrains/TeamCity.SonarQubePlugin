package jetbrains.buildserver.sonarplugin;

import java.io.Closeable;
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
}
