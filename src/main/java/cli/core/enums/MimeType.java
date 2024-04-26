package cli.core.enums;

import cli.core.parsers.PdfFileParser;
import cli.core.parsers.DocFileParser;
import cli.core.parsers.IParser;
import cli.core.parsers.TextFileParser;


public enum MimeType {
    PDF,
    DOC,
    TEXT,
    OTHER;

    IParser parser;

    public static MimeType parse(final String type) {
        switch(type) {
            case "pdf":
            case "application/pdf":
                return PDF;
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "doc":
                return DOC;
            case "text":
            case "text/plain":
            case "text/x-java-source":
            case "application/xml":
            case "text/x-web-markdown":
                return TEXT;
        }

        return OTHER;
    }

    public IParser getParser() {
        if(parser == null) {
            switch(this) {
                case PDF: return (parser = new PdfFileParser());
                case DOC: return (parser = new DocFileParser());
                case TEXT: return (parser = new TextFileParser());
            }
        }

        return parser;
    }
}
