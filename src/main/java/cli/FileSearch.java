package cli;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;


// Features
// TODO: can add searching within a file

@Slf4j
public class FileSearch {
    @CommandLine.Option(names = {"-c", "--content"}, description = "Search content of files")
    boolean contentMode;

    @CommandLine.Parameters(paramLabel = "DIRECTORY", description = "The directory to begin search", defaultValue = ".")
    File directory;

    @CommandLine.Option(names = {"-t", "--types"}, description = "File extensions to include (pipe-separated, e.g. pdf,xlsx,txt)", defaultValue = "*")
    String typeFilter;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "In help mode")
    boolean helpMode;

    public int run() {
        if(contentMode)
            return contentSearch();
        return fileMetaSearch();
    }

    private int contentSearch() {
        return -1;
    }

    private int fileMetaSearch() {
        
    }



    public static void main(String[] args) {
        FileSearch fs = new FileSearch();

        new CommandLine(fs).parseArgs(args);


    }
}
