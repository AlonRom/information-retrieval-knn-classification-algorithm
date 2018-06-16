package Lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.IndexReader;

public class CustomAnalyzer extends Analyzer {

    IndexReader reader;
    CharArraySet stopWordSet;



    public CustomAnalyzer(CharArraySet stopWordSet){

        this.stopWordSet = stopWordSet;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        if (stopWordSet!=null)
            filter = new StopFilter(filter, stopWordSet);
        filter = new PorterStemFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
