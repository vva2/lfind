package core.searchers;

public interface ISearcher {
    String[] getMatches(final String query);
    void close();
}
