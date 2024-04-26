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

@CommandLine.Command(name = "lfind")
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

//    @CommandLine.Option(names = {"-m"}, description = "In memory search")
//    boolean inMemory;

    File directory;

    @CommandLine.Option(names = {"-m", "--mimetypes"}, description = "Mime-Types to include (comma-separated, e.g. pdf,doc,text)", split = ",")
    String[] mimeTypes;

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
        log.info("help-mode: " + helpMode);
        log.info("verbose-mode: " + verbose);
        log.info("query: " + (queries == null? null: Arrays.asList(queries)));
        log.info("");
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
        checkVerbosity();

        // TODO remove this
        printArgs();

        ISearcher searcher = buildSearcher();
        processQueries(searcher);
        searcher.close();
    }

    private void checkVerbosity() {
        if(!verbose)
            GlobalLogger.turnOffLogging();
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
                searcher = new FileContentSearcher(indexDir, directory, mimeTypes);
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
            String[] matches;
            try {
                matches = luceneQuery? searcher.getLuceneQueryMatches(query) :  searcher.getMatches(query);
            } catch (Exception e) {
                matches = null;

                Arrays.stream(e.getStackTrace()).forEach(st -> log.info(st.toString()));
            }

            PrettyPrint.printMatches(query, matches);
        }
    }

    private SearchMode getSearchMode() {
        return hasPipedInput()? SearchMode.PIPED_INPUT: contentMode? SearchMode.FILE_CONTENT: SearchMode.FILE_METADATA;
    }

    private static void turnOffLogging() {
        // Get the logger context
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//
//        // Set the root logger level to OFF
//        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
//        rootLogger.setLevel(Level.OFF);
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
