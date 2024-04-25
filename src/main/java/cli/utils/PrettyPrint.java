package cli.utils;


public class PrettyPrint {
    public static void printMatches(final String query, String[] matches) {
        // Print the query
        System.out.printf("Query: '%s'\n", query);

        // Check if there are matches to print
        if (matches == null) {
            System.out.println("Error Occured.");
        }
        else if (matches.length == 0) {
            System.out.println("No matches found.");
        } else {
            // Header
            System.out.printf("%d Match%s Found:\n", matches.length, matches.length == 1? "": "es");
            int rank = 1;

            // Print each match with its rank and relevance
            for (String match : matches) {
                // Assuming the match string contains the relevance score at the end
                System.out.printf("%-6s %s%n", "[" + rank++ + "]", match);
            }
        }

        // Print a separator
        System.out.println("---");
    }
}
