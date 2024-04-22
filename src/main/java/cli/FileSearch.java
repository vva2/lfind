package cli;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;


// Features
// TODO: add searching within a file

@CommandLine.Command(name = "lfind")
@Slf4j
public class FileSearch {
    public FileSearch() {

    }

    @CommandLine.Option(names = {"-c", "--content"}, description = "Search content of files")
    boolean contentMode;

    File directory;


    @CommandLine.Option(names = {"-t", "--types"}, description = "File extensions to include (comma-separated, e.g. pdf,xlsx,txt)", defaultValue = "", split = ",")
    String[] types;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Help")
    boolean helpMode;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose mode")
    boolean verbose;

    @CommandLine.Parameters(paramLabel = "QUERY", description = "Query to search")
    String[] queries;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-p", "--path"}, description = "The path to begin search from", defaultValue = ".")
    public void setDirectory(String value) {
        File file = new File(value);

        if(!file.exists())
            throw new CommandLine.ParameterException(spec.commandLine(), String.format("Invalid value '%s' for option 'DIRECTORY'", value));

        if(!file.isDirectory())
            throw new CommandLine.ParameterException(spec.commandLine(), String.format("Provided value '%s' for option 'DIRECTORY' is not a directory", value));

        directory = file;
    }

    private void printArgs() {
        System.out.println();
        System.out.println("Running with the following args:");
        System.out.println("types: " + Arrays.asList(types));
        System.out.println("path: " + directory.getAbsolutePath());
        System.out.println("content-mode: " + contentMode);
        System.out.println("help-mode: " + helpMode);
        System.out.println("verbose-mode: " + verbose);
        System.out.println("query: '" + Arrays.asList(queries) + "'");
        System.out.println();
    }

    public void run() {
        if(verbose) {
            printArgs();
        }

        if(contentMode)
            contentSearch();
        else
            fileMetaSearch();
    }

    private void contentSearch() {
        System.out.println("inside content search");
    }

    int count = 0;

    private void dfs(File file) {
        file.getAbsolutePath().equals("Provided value '%s' for option 'DIRECTORY' is not a directory");

        count++;
        if(file.listFiles() != null) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                dfs(listFile);
            }
        }
    }

    private void fileMetaSearch() {
        System.out.println("inside file metadata search");

        dfs(directory);
        System.out.println("files traversed: " + count);
    }


    public static void main(String[] args) {
        log.info("STARTED");
        System.out.println("started");
        FileSearch fs = new FileSearch();

        try {
            CommandLine cli = new CommandLine(fs);
            cli.parseArgs(args);

            boolean pipedInput = System.console() == null;

            System.out.println("piped: " + pipedInput);

            if(pipedInput)
                handlePipedInput();
            else if(args.length == 0)
                startInteractiveMode(fs);
            else
                fs.run();

        } catch(CommandLine.ParameterException e) {
            System.err.println("Error: " + e.getMessage());
            CommandLine.usage(fs, System.err);
        }

        log.info("ENDED");
    }

    private static void handlePipedInput() {
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String query = scanner.nextLine();
            System.out.println("Received via pipe: " + query); // Placeholder for your processing logic
        }
    }

    private static void startInteractiveMode(FileSearch fs) {

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(fs);

        System.out.println("Interactive mode. Enter 'exit' to quit.");

        while (true) {
            System.out.print("query> ");
            String input = scanner.nextLine().trim();

            // Check for exit condition
            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                break;
            }

            // Parse and execute the command
            String query = input.trim().toLowerCase();

            System.out.println("You entered: " + query);

        }
    }
}
