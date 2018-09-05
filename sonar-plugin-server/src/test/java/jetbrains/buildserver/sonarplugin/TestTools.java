package jetbrains.buildserver.sonarplugin;

import com.intellij.util.ThrowableConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestTools {
    private TestTools() {
    }

    public static Path prepareZip(@NotNull final Path parent,
                                  @NotNull final String name,
                                  @NotNull final ThrowableConsumer<FileSystem, IOException> consumer) throws IOException {
        Path zip = parent.resolve(name);

        Files.createDirectories(parent);
        Files.deleteIfExists(zip);
        Files.createFile(zip);
        // write empty zip file
        Files.write(zip, new byte[]{0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        try (final FileSystem zipFs = FileSystems.newFileSystem(zip, null)) {
            consumer.consume(zipFs);
        }
        return zip;
    }
}
