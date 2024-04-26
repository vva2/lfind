package cli.core.searchers;


import cli.core.analyzers.CustomWhiteSpaceAnalyzer;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;

import static cli.config.GlobalLogger.log;


public class PipeStreamSearcher implements ISearcher {
    // commit every 500 lines
    private static final int LINE_COMMIT_THRESHOLD = 500;

    private static class Fields {
        public static final String LINE = "LINE";
    }

    Directory index;
    IndexWriter writer;
    IndexSearcher searcher;
    Analyzer analyzer;
    int nTopDocs;
    int nLinesProcessed = 0;

    public PipeStreamSearcher(Path indexDir) {
        this.analyzer = new CustomWhiteSpaceAnalyzer();
        this.nTopDocs = Integer.MAX_VALUE;   // need 100% recall as often the input length is small

        buildIndex(indexDir);
        openSearcher();
    }

    @SneakyThrows
    private void openSearcher() {
        this.searcher = new IndexSearcher(DirectoryReader.open(this.index));
    }

    @SneakyThrows
    private void initializeIndexWriter() {
        IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
        // Create a new index in the directory, removing any
        // previously indexed documents:
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        this.writer = new IndexWriter(this.index, config);
    }

    @SneakyThrows
    private void buildIndex(Path indexDir) {
        this.index = FSDirectory.open(indexDir);
        initializeIndexWriter();
        addDocsToIndex();
    }

    private void addDocsToIndex() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                indexLine(line);
            }

            log.info("lines written successfully");

        } catch (IOException e) {
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
            throw new RuntimeException(e);
        }

        // Commit and close the index writer
        commitAndClose();
    }

    private void commitAndClose() throws IOException {
        writer.commit();
        writer.close();
    }

    private void indexLine(String line) {
        // Create a Lucene document for the file
        Document document = new Document();
        document.add(new TextField(Fields.LINE, line, Field.Store.YES)); // Index file name

        // Add the document to the Lucene index
        try {
            writer.addDocument(document);

            nLinesProcessed++;

            if(nLinesProcessed% LINE_COMMIT_THRESHOLD == 0) {
                writer.commit();

                log.info("commiting writer. lines processed: " + nLinesProcessed);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    public String[] getMatches(final String query) {
        // Split the query into tokens using whitespace
        String[] tokens = query.toLowerCase().split("\\s+");

        BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();

        if (tokens.length > 0) {
            final String searchTerm = tokens.length == 1? "*" + tokens[0] + "*": "*" + tokens[0];

            booleanBuilder.add(
                    new WildcardQuery(new Term(Fields.LINE, searchTerm)), BooleanClause.Occur.MUST
            );
        }

        if(tokens.length > 2) {
            // Create a PhraseQuery to ensure the tokens are contiguous and in order
            PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();

            // Add terms to the PhraseQuery (both original and reversed)
            for (int i = 1; i < tokens.length - 1; i++) {
                // Exact match for middle tokens
                phraseBuilder.add(new Term(Fields.LINE, tokens[i]), i);
            }

            // Set the maximum number of other words permitted between words in query phrase
            // If you want the words to be contiguous, set it to 0
            phraseBuilder.setSlop(0);

            booleanBuilder.add(phraseBuilder.build(), BooleanClause.Occur.MUST);
        }

        if(tokens.length > 1) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(Fields.LINE, tokens[tokens.length - 1]));
            booleanBuilder.add(prefixQuery, BooleanClause.Occur.MUST);
        }

        BooleanQuery booleanQuery = booleanBuilder.build();

        return getMatches(booleanQuery);
    }

    @SneakyThrows
    @Override
    public void close() {
        this.index.close();
    }

    @SneakyThrows
    @Override
    public String[] getLuceneQueryMatches(String query) {
        // Create a QueryParser for the specified field and analyzer
        QueryParser parser = new QueryParser(Fields.LINE, this.analyzer);

        // Parse the user query string to obtain a Lucene Query object
        Query luceneQuery = parser.parse(query);

        return getMatches(luceneQuery);
    }

    private String[] getMatches(Query query) throws IOException {
        TopDocs topDocs = searcher.search(query, this.nTopDocs); // Adjust the number of results as needed

        // Process the search results
        return processSearchResults(topDocs, searcher);
    }

    @Override
    public String formatMatch(Document document) {
        return String.format("%s", document.get(Fields.LINE));
    }
}