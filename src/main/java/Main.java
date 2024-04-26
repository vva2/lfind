import cli.LFind;
import picocli.CommandLine;
import cli.core.searchers.FileContentSearcher;
import cli.core.searchers.FileMetaSearcher;

import static cli.config.GlobalLogger.log;

public class Main {
    public static void main(String[] args) {
        LFind lfind = new LFind();

        try {
            CommandLine cli = new CommandLine(lfind);
            cli.parseArgs(args);

            lfind.run();

        } catch(Exception e) {
            System.out.println("Failed to run. Exiting.");

            log.severe(e.getMessage());
        }
    }
}
