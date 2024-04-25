package core.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

public class CustomWhiteSpaceAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();

        TokenFilter filter = new LowerCaseFilter(tokenizer); // Normalize tokens to lowercase
//        filter = new ReversedTokenFilter(filter); // Apply custom synonym filter

        return new TokenStreamComponents(tokenizer, filter);
    }


}
