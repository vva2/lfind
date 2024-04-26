package cli.core.parsers;

import lombok.SneakyThrows;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.function.Consumer;


public class TextFileParser implements IParser {
    @Override
    @SneakyThrows
    public void readContent(File file, Consumer<String> consumer) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // Read lines from the file until the end of the file (null is returned)
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
        }
    }
}
