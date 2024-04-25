package core.parsers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;


import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;


@Slf4j
public class DocFileParser implements IParser {
    @Override
    @SneakyThrows
    public void readContent(File file, Consumer<String> consumer) {
        try (FileInputStream fis = new FileInputStream(file); XWPFDocument document = new XWPFDocument(fis)) {
            // read all the text content from the file
            // the sources of text should include paragraphs, tables, comments, etc.
            readContent(document, consumer);


        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
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
            log.error(Arrays.toString(e.getStackTrace()));
            log.info("SKIPPING OPTIONAL CONTENT IN DOC FILE");
        }
    }
}
