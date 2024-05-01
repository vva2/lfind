package cli.core.searchers;

import cli.core.analyzers.CustomWhiteSpaceAnalyzer;
import cli.core.enums.FileType;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.function.Predicate;

import static cli.config.GlobalLogger.log;


public class FileMetaSearcher implements ISearcher {
    private static final int FILE_COMMIT_THRESHOLD = 50;

    private static class Fields {
        final static String FILE_NAME = "fileName";
        final static String FILE_TYPE = "fileType";
        final static String ABS_PATH = "absPath";
    }

    Directory index;
    IndexWriter writer;
    File rootDir;
    IndexSearcher searcher;
    Analyzer analyzer;
    int nTopDocs;
    int nFilesProcessed = 0;

    public FileMetaSearcher() {}

    public FileMetaSearcher(Path indexDir, File rootDir) {
        log.info("Initializing file metadata searcher...");

        this.rootDir = rootDir;
        this.analyzer = new CustomWhiteSpaceAnalyzer();
        this.nTopDocs = Integer.MAX_VALUE;

        buildIndex(indexDir);
        openSearcher();
    }

    @SneakyThrows
    private void openSearcher() {
        log.info("Opening searcher...");

        this.searcher = new IndexSearcher(DirectoryReader.open(this.index));

        log.info("Searcher opened.");
    }

    @SneakyThrows
    private void initializeIndexWriter() {
        log.info("Initializing index...");

        IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
        // Create a new index in the directory, removing any
        // previously indexed documents:
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        this.writer = new IndexWriter(this.index, config);

        log.info("Index initialized");
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
                .filter(isFileOrDirectory())
                .forEach(this::indexFile);

        // Commit and close the index writer
        commitAndClose();
    }

    // Custom predicate to filter both files and directories
    private static Predicate<Path> isFileOrDirectory() {
        return path -> {
            try {
                log.info("checking path: " + path.toAbsolutePath());

                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

                return attributes.isRegularFile() || attributes.isDirectory();
            } catch (IOException e) {
                log.severe("Error predicating on file: " + path.toAbsolutePath());
                Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));

                // Handle IOException if necessary
                return false; // Return false in case of an error
            }
        };
    }

    private void commitAndClose() throws IOException {
        writer.commit();
        writer.close();
    }

    private void indexFile(Path filePath) {
        // Add the document to the Lucene index
        try {
            final File file = filePath.toFile();
            final String absolutePath = file.getAbsolutePath();
            final FileType fileType = file.isFile()? FileType.FILE: FileType.DIR;
            final String name = file.getName();

            // Create a Lucene document for the file
            Document document = new Document();
            document.add(new TextField(Fields.FILE_NAME, name, Field.Store.YES)); // Index file name

            // Optionally, index other metadata such as file path
            document.add(new StoredField(Fields.ABS_PATH, absolutePath));

            // store fileType
            document.add(new StoredField(Fields.FILE_TYPE, fileType.name()));

            writer.addDocument(document);
            nFilesProcessed++;


            if(nFilesProcessed%FILE_COMMIT_THRESHOLD == 0) {
                writer.commit();

                log.info("commiting writer. files processed: " + nFilesProcessed);
            }
        } catch (Exception e) {
            log.severe("ERROR in file: " + filePath.toAbsolutePath() + ". Skipping file");
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
            return;
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
                    new WildcardQuery(new Term(Fields.FILE_NAME, searchTerm)), BooleanClause.Occur.MUST
            );
        }

        if(tokens.length > 2) {
            // Create a PhraseQuery to ensure the tokens are contiguous and in order
            PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();

            // Add terms to the PhraseQuery (both original and reversed)
            for (int i = 1; i < tokens.length - 1; i++) {
                // Exact match for middle tokens
                phraseBuilder.add(new Term(Fields.FILE_NAME, tokens[i]), i);
            }

            // Set the maximum number of other words permitted between words in query phrase
            // If you want the words to be contiguous, set it to 0
            phraseBuilder.setSlop(0);

            booleanBuilder.add(phraseBuilder.build(), BooleanClause.Occur.MUST);
        }

        if(tokens.length > 1) {
            PrefixQuery prefixQuery = new PrefixQuery(new Term(Fields.FILE_NAME, tokens[tokens.length - 1]));
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
        QueryParser parser = new QueryParser(Fields.FILE_NAME, this.analyzer);

        // lowercase as wildcard parsing is case-sensitive
        query = query.toLowerCase();

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
        return String.format("%-5s| %-30s| %s", document.get(Fields.FILE_TYPE), document.get(Fields.FILE_NAME), makePathClickable(document.get(Fields.ABS_PATH)));
    }
}