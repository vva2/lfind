package cli;

import cli.enums.SearchMode;
import cli.utils.FileUtils;
import cli.utils.PrettyPrint;
import core.searchers.FileContentSearcher;
import core.searchers.FileMetaSearcher;
import core.searchers.ISearcher;
import core.searchers.PipeStreamSearcher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;


// Features
// TODO: add searching within a file

@CommandLine.Command(name = "lfind")
@Slf4j
public class LFind {
    @Setter
    boolean isPipedInput;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public LFind() {
        this.isPipedInput = false;
    }

    @CommandLine.Option(names = {"-c", "--content"}, description = "Search content of files")
    boolean contentMode;

    @CommandLine.Option(names = {"-m"}, description = "In memory search")
    boolean inMemory;

    File directory;

    @CommandLine.Option(names = {"-t", "--types"}, description = "File extensions to include (comma-separated, e.g. pdf,xlsx,txt)", defaultValue = "", split = ",")
    String[] types;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Help")
    boolean helpMode;

    @Getter
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose mode")
    boolean verbose;

    @CommandLine.Option(names = {"-e", "--expression"}, description = "Lucene query expression")
    boolean luceneQuery;

    @CommandLine.Parameters(paramLabel = "QUERY", description = "Query to search")
    String[] queries;


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

    private void argsValidityCheck() {
        // checks if the combination of args are valid
        // can be replaced by using groups
        // throws exception if the combination is invalid
    }

    private boolean hasPipedInput() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        argsValidityCheck();

        if(verbose) {
            printArgs();
        }

        ISearcher searcher = buildSearcher();
        processQueries(searcher);
        searcher.close();
    }

    private ISearcher buildSearcher() {
        Path indexDir = FileUtils.createTempDirectory();

        // determine type of input
        SearchMode searchMode = getSearchMode();
        ISearcher searcher;

        switch (searchMode) {
            case FILE_METADATA:
                // file metadata
                searcher = new FileMetaSearcher(indexDir, directory);
                break;
            case FILE_CONTENT:
                // file content
                searcher = new FileContentSearcher(indexDir);
                break;
            default:
                // piped input
                searcher = new PipeStreamSearcher(indexDir);
        }

        return searcher;
    }

    private void processQueries(ISearcher searcher) {
        // TODO
        // check for interactive mode
        for (String query : queries) {
            final String[] matches = searcher.getMatches(query);
            PrettyPrint.printMatches(query, matches);
        }
    }

    SearchMode getSearchMode() {
        return hasPipedInput()? SearchMode.PIPED_INPUT: contentMode? SearchMode.FILE_CONTENT: SearchMode.FILE_METADATA;
    }


    public static void main(String[] args) {
        log.info("STARTED");
        LFind lfind = new LFind();

        try {
            CommandLine cli = new CommandLine(lfind);
            cli.parseArgs(args);

            lfind.run();

        } catch(Exception e) {
            System.out.println("Failed to run. Exiting.");

            if(lfind.isVerbose()) {
                e.printStackTrace();
            }
        }

        log.info("ENDED");
    }

    private static void startInteractiveMode(LFind fs) {

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
