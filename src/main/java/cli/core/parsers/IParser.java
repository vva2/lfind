package cli.core.parsers;


import java.io.File;
import java.util.function.Consumer;

public interface IParser {
    void readContent(File file, Consumer<String> consumer);
}
