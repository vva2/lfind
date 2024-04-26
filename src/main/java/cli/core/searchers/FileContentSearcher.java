package cli.core.searchers;

import cli.core.analyzers.CustomWhiteSpaceAnalyzer;
import cli.core.enums.MimeType;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static cli.config.GlobalLogger.log;


public class FileContentSearcher implements ISearcher {
    private static class Fields {
        public static String FILE_NAME = "fileName";
        public static String ABS_PATH = "absPath";
        public static String CONTENT = "content";
        public static String MIME_TYPE = "mimeType";
    }

    Directory index;
    IndexWriter writer;
    File rootDir;
    IndexSearcher searcher;
    Analyzer analyzer;
    int nTopDocs;
    Tika tika;
    Set<MimeType> allowedMimeTypes;

    public FileContentSearcher(Path indexDir, File rootDir, String[] mimeTypes) {
        this.rootDir = rootDir;
        this.analyzer = new CustomWhiteSpaceAnalyzer();
        this.nTopDocs = Integer.MAX_VALUE;
        this.tika = new Tika();


        buildMimeTypeFilter(mimeTypes);
        buildIndex(indexDir);
        openSearcher();
    }

    private void buildMimeTypeFilter(String[] mimeTypes) {
        if(mimeTypes == null || mimeTypes.length == 0)
            return;

        this.allowedMimeTypes = new TreeSet<>();

        for (String mimeType : mimeTypes) {
            MimeType type = MimeType.parse(mimeType);

            allowedMimeTypes.add(type);
        }
    }

    private boolean isMimeTypeAllowed(MimeType mimeType) {
        return allowedMimeTypes == null || allowedMimeTypes.contains(mimeType);
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
        // recursively read files and folder metadata and add to index
        Files.walk(rootDir.toPath())
                .filter(Files::isRegularFile)
                .forEach(this::indexFile);

        // Commit and close the index writer
        commitAndClose();
    }

    private void commitAndClose() throws IOException {
        writer.commit();
        writer.close();
    }

    private void indexFile(Path filePath) {
        final File file = filePath.toFile();
        MimeType mimeType;

        try {
            final String tikaMime = tika.detect(file);
            mimeType = MimeType.parse(tikaMime);

            log.info("file: " + file.getAbsolutePath() + " | mimeType: " + mimeType + " | " + tikaMime);

            if(!isMimeTypeAllowed(mimeType))
                return;

        } catch (IOException e) {
            // skip processing this file
            return;
        }

        final String absolutePath = file.getAbsolutePath();
        final String name = file.getName();

        log.info("indexing file: " + absolutePath);

        // Create a Lucene document for the file
        Document document = new Document();

        try {
            mimeType.getParser().readContent(file, text -> {
                document.add(new TextField(Fields.CONTENT, text + " ", Field.Store.NO));
            });
        } catch (Exception e) {
            log.severe("ERROR occured while indexing file: " + absolutePath);
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
            return;
        }

        document.add(new StoredField(Fields.ABS_PATH, absolutePath));
        document.add(new StoredField(Fields.MIME_TYPE, mimeType.name()));
        document.add(new StoredField(Fields.FILE_NAME, name));

        // Add the document to the Lucene index
        try {
            log.info("writing document...");
            writer.addDocument(document);
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
                    new WildcardQuery(new Term(Fields.CONTENT, searchTerm)), BooleanClause.Occur.MUST
            );
        }

        if(tokens.length > 2) {
            // Create a PhraseQuery to ensure the tokens are contiguous and in order
            PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();

            // Add terms to the PhraseQuery (both original and reversed)
            for (int i = 1; i < tokens.length - 1; i++) {
                // Exact match for middle tokens
                phraseBuilder.add(new Term(Fields.CONTENT, tokens[i]), i);
            }

            // Set the maximum number of other words permitted between words in query phrase
            // If you want the words to be contiguous, set it to 0
            phraseBuilder.setSlop(0);

            booleanBuilder.add(phraseBuilder.build(), BooleanClause.Occur.MUST);
        }

        if(tokens.length > 1) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(Fields.CONTENT, tokens[tokens.length - 1]));
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
        QueryParser parser = new QueryParser(Fields.CONTENT, this.analyzer);

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
        return String.format("%-3s | %-40s | %s", document.get(Fields.MIME_TYPE), document.get(Fields.FILE_NAME), makePathClickable(document.get(Fields.ABS_PATH)));
    }
}
