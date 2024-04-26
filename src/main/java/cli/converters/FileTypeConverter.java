package cli.converters;

import picocli.CommandLine;


public class FileTypeConverter implements CommandLine.ITypeConverter<String[]> {
    @Override
    public String[] convert(String value) {
        return value
                .replaceAll("\\s+", "")         // remove all spaces
                .toLowerCase()                                  // lower case
                .split(",");                              // split by commas
    }
}