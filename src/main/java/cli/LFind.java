package cli;


import cli.enums.SearchMode;
import cli.utils.FileUtils;
import cli.utils.PrettyPrint;
import cli.config.GlobalLogger;
import cli.core.searchers.FileContentSearcher;
import cli.core.searchers.FileMetaSearcher;
import cli.core.searchers.ISearcher;
import cli.core.searchers.PipeStreamSearcher;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

import static cli.config.GlobalLogger.log;


// Features
// TODO: add searching within a file

@CommandLine.Command(name = "lfind", mixinStandardHelpOptions = true)
public class LFind implements Runnable {
    @Setter
    boolean isPipedInput;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public LFind() {
        this.isPipedInput = false;
    }

    @CommandLine.Option(names = {"-c", "--content"}, description = "Search content of files")
    boolean contentMode;

//    @CommandLine.Option(names = {"-m"}, description = "In memory search")
//    boolean inMemory;

    File directory;

    @CommandLine.Option(names = {"-m", "--mimetypes"}, description = "Mime-Types to include (comma-separated, e.g. pdf,doc,text)", split = ",")
    String[] mimeTypes;

//    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Help")
//    boolean helpMode;

    @Getter
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose mode")
    boolean verbose;

    @CommandLine.Option(names = {"-e", "--expression"}, description = "Lucene query expression")
    boolean luceneQuery;

    @CommandLine.Parameters(paramLabel = "QUERY", description = "Query to search")
    String[] queries;


    @CommandLine.Option(names = {"-p", "--path"}, description = "The path to begin search from", defaultValue = ".")
    public void setDirectory(String value) {
        if (value.equals(".")) {
            value = System.getProperty("user.dir");
        }

        File file = new File(value);

        if(!file.exists())
            throw new CommandLine.ParameterException(spec.commandLine(), String.format("Invalid value '%s' for option 'DIRECTORY'", value));

        if(!file.isDirectory())
            throw new CommandLine.ParameterException(spec.commandLine(), String.format("Provided value '%s' for option 'DIRECTORY' is not a directory", value));

        directory = file;
    }

    private void printArgs() {
        log.info("");
        log.info("Running with the following args:");
        log.info("types: " + (mimeTypes == null? null: Arrays.asList(mimeTypes)));
        log.info("path: " + directory.getAbsolutePath());
        log.info("content-mode: " + contentMode);
//        log.info("help-mode: " + helpMode);
        log.info("verbose-mode: " + verbose);
        log.info("query: " + (queries == null? null: Arrays.asList(queries)));
        log.info("");
    }

    private void argsValidityCheck() {
        // checks if the combination of args are valid
        // can be replaced by using groups
        // throws exception if the combination is invalid

        if(isInteractive() && getSearchMode().equals(SearchMode.PIPED_INPUT))
            throw new CommandLine.ParameterException(spec.commandLine(), String.format("Piped input search in interactive mode is not supported"));
    }

    private boolean hasPipedInput() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isInteractive() {
        return queries == null;
    }

    @Override
    public void run() {
        argsValidityCheck();
        checkVerbosity();

        // TODO remove this
        printArgs();

        Path indexDir = FileUtils.createTempDirectory();
        ISearcher searcher = buildSearcher(indexDir);
        log.info("Searcher initialized");

        if(isInteractive())
            runInteractiveMode(searcher);
        else
            processQueries(searcher);

        searcher.close();
        cleanUp(indexDir);
    }

    private void checkVerbosity() {
        if(!verbose)
            GlobalLogger.turnOffLogging();
    }

    private ISearcher buildSearcher(Path indexDir) {
        log.info("INDEX DIRECTORY: " + indexDir);

        // determine type of input
        SearchMode searchMode = getSearchMode();
        ISearcher searcher;

        log.info("Search Mode: " + searchMode);

        switch (searchMode) {
            case FILE_METADATA:
                // file metadata
                searcher = new FileMetaSearcher(indexDir, directory);
                break;
            case FILE_CONTENT:
                // file content
                searcher = new FileContentSearcher(indexDir, directory, mimeTypes);
                break;
            default:
                // piped input
                searcher = new PipeStreamSearcher(indexDir);
        }

        return searcher;
    }

    private void processQueries(ISearcher searcher) {
        log.info("Processing queries...");
        // TODO
        // check for interactive mode
        for (String query : queries) {
            processQuery(searcher, query);
        }
    }

    private void processQuery(ISearcher searcher, String query) {
        String[] matches;
        try {
            matches = luceneQuery? searcher.getLuceneQueryMatches(query) :  searcher.getMatches(query);
        } catch (Exception e) {
            matches = null;

            Arrays.stream(e.getStackTrace()).forEach(st -> log.info(st.toString()));
        }

        PrettyPrint.printMatches(query, matches);
    }

    private SearchMode getSearchMode() {
        return hasPipedInput()? SearchMode.PIPED_INPUT: contentMode? SearchMode.FILE_CONTENT: SearchMode.FILE_METADATA;
    }

    private void cleanUp(Path indexDir) {
        log.info("Cleaning up index");
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(indexDir.toFile());
            log.info("index removed successfully");
        } catch (IOException e) {
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
            log.severe("failed to delete index");
        }
    }

    private void runInteractiveMode(ISearcher searcher) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Interactive mode. Enter '\\q' to quit.");

            while (true) {
                System.out.print("query> ");
                String input = scanner.nextLine().trim();

                // Check for exit condition
                if ("\\q".equalsIgnoreCase(input)) {
                    log.info("Exiting interactive mode");
                    break;
                } else if("\\h".equalsIgnoreCase(input)) {
                    log.info("Interactive: help mode");
                    System.out.println("Enter a query to search | \\q for quit | \\h for help");
                    continue;
                }

                // Parse and execute the command
                String query = input.trim();

                processQuery(searcher, query);
            }
        } catch (Exception e) {
            System.out.println("Error Occured. Exiting...");
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
        }
    }
}
