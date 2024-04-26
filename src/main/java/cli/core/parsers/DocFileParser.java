package cli.core.parsers;

import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.*;


import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;

import static cli.config.GlobalLogger.log;


public class DocFileParser implements IParser {
    @Override
    @SneakyThrows
    public void readContent(File file, Consumer<String> consumer) {
        try (FileInputStream fis = new FileInputStream(file); XWPFDocument document = new XWPFDocument(fis)) {
            // read all the text content from the file
            // the sources of text should include paragraphs, tables, comments, etc.
            readContent(document, consumer);


        } catch (IOException e) {
            log.severe(Arrays.toString(e.getStackTrace()));
        }
    }

    private void readContent(XWPFDocument document, Consumer<String> consumer) {
        // Read paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            consumer.accept(paragraph.getText());
        }

        try {
            // Read tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        consumer.accept(cell.getText());
                    }
                }
            }

            // Read other parts of the document (headers, footers, etc.)
            // Example: Headers
            for (XWPFHeader header : document.getHeaderList()) {
                consumer.accept(header.getText());
            }

            // Example: Footers
            for (XWPFFooter footer : document.getFooterList()) {
                consumer.accept(footer.getText());
            }

            // Read comments (if any)
            if(document.getComments() != null) {
                for (XWPFComment comment : document.getComments()) {
                    consumer.accept(comment.getText());
                }
            }
        } catch (Exception e) {
            Arrays.stream(e.getStackTrace()).forEach(st -> log.severe(st.toString()));
            log.info("SKIPPING OPTIONAL CONTENT IN DOC FILE");
        }
    }
}
