package jetbrains.buildserver.sonarplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestTools {
    public static Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("keystoreTemp");
    }

    public static File writeTo(String prefix, String content) {
        return writeTo(prefix, content, ".tmp", null);
    }

    public static File writeTo(String prefix, String content, String suffix) {
        return writeTo(prefix, content, suffix, null);
    }

    public static File writeTo(String prefix, String content, String suffix, File baseFolder) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(prefix, suffix, baseFolder);
            FileWriter fw = new FileWriter(tempFile);
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

}
