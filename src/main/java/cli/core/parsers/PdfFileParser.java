package cli.core.parsers;

import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class PdfFileParser implements IParser {
    PDFTextStripper pdfTextStripper;

    public PdfFileParser() {
        this.pdfTextStripper = new PDFTextStripper();
    }

    @SneakyThrows
    @Override
    public void readContent(File file, Consumer<String> consumer) {
        // Load the PDF document
        try(PDDocument document = Loader.loadPDF(file)) {
            readContent(document, consumer);
        }
    }

    private void readContent(PDDocument document, Consumer<String> consumer) throws IOException {
        // Extract text line by line
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            pdfTextStripper.setStartPage(page);
            pdfTextStripper.setEndPage(page);
            String pageText = pdfTextStripper.getText(document).replaceAll("\\r?\\n", " ");

            consumer.accept(pageText);
        }
    }
}
