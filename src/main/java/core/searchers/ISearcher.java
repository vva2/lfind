package core.searchers;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public interface ISearcher {
    String[] getMatches(final String query);
    void close();

    String[] getLuceneQueryMatches(String query);

    default String[] processSearchResults(TopDocs topDocs, IndexSearcher searcher) throws IOException {
        // Convert TopDocs to an array of matching file names
        String[] matches = new String[topDocs.scoreDocs.length];
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            int docId = topDocs.scoreDocs[i].doc;
            Document doc = searcher.storedFields().document(docId);

            matches[i] = formatMatch(doc);
        }
        return matches;
    }

    default String makePathClickable(String path) {
        return "\"" + path + "\"";
    }

    String formatMatch(Document document);
}
