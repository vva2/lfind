package core.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class ReversedTokenFilter extends TokenFilter {
    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
    private boolean emitOriginalToken = true;

    public ReversedTokenFilter(TokenFilter input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (emitOriginalToken && input.incrementToken()) {
            emitOriginalToken = false;
            return true; // Emit original token first
        }

        if (!emitOriginalToken) {
            String currentToken = charTermAttribute.toString();
            String reversedToken = new StringBuilder(currentToken).reverse().toString();

            if (!currentToken.equals(reversedToken)) {
                // Clear the attributes for the current token
                clearAttributes();
                // Set the reversed token in the charTermAttribute
                charTermAttribute.setEmpty().append(reversedToken);
                // Set position increment to 0 to indicate a synonym
                posIncAttribute.setPositionIncrement(0);
                emitOriginalToken = true; // Next call will emit the original token
                return true; // Emit reversed token
            }
        }

        // If the reversed token is the same as the original, or there are no more tokens, return false
        return false;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        emitOriginalToken = true; // Reset the flag for the next run
    }
}
