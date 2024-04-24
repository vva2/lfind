package core.searchers;

import java.nio.file.Path;

public class PipeStreamSearcher implements ISearcher {
    public PipeStreamSearcher(Path indexDir) {

    }

    @Override
    public String[] getMatches(String query) {
        return new String[0];
    }

    @Override
    public void close() {

    }
}