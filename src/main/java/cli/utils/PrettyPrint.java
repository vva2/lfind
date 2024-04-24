package cli.utils;


public class PrettyPrint {
    public static void printMatches(final String query, String[] matches) {
        // Print the query
        System.out.println("Query: " + query);

        // Check if there are matches to print
        if (matches == null || matches.length == 0) {
            System.out.println("No matches found.");
        } else {
            // Header
            System.out.println("Matches:");
            System.out.printf("%-5s %-30s%n", "Rank", "Match");

            // Print each match with its rank and relevance
            int rank = 1;
            for (String match : matches) {
                // Assuming the match string contains the relevance score at the end
                System.out.printf("%-5d %s%n", rank++, match);
            }
        }

        // Print a separator
        System.out.println("--------------------------------------------------");
    }
}
