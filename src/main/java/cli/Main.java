package cli;

import picocli.CommandLine;

import static cli.config.GlobalLogger.log;

public class Main {
    public static void main(String[] args) {
        LFind lfind = new LFind();

        try {
            CommandLine cli = new CommandLine(lfind);
            cli.execute(args);
        } catch(Exception e) {
            System.out.println("Failed to run. Exiting.");

            log.severe(e.getMessage());
        }
    }
}
