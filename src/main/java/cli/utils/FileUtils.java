package cli.utils;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    @SneakyThrows
    public static Path createTempDirectory() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path fixedPath = Paths.get(tempDir, "lfind-index");

        if (!Files.exists(fixedPath)) {
            Files.createDirectory(fixedPath);
        }

        return fixedPath;
    }
}
