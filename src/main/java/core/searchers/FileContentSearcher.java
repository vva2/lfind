package core.searchers;

import org.apache.lucene.index.IndexWriter;

import java.nio.file.Path;

public class FileContentSearcher implements ISearcher {
    IndexWriter indexWriter;

    public FileContentSearcher(Path indexDir) {

    }

    @Override
    public String[] getMatches(String query) {


        return null;
    }

    @Override
    public void close() {

    }
}
