package Lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.IndexReader;

public class CustomAnalyzer extends Analyzer {

    IndexReader reader;
    CharArraySet stopWordSet;
    boolean usePorterFilter;



    public CustomAnalyzer(CharArraySet stopWordSet){

        this.stopWordSet = stopWordSet;
        this.usePorterFilter = true;
    }

    public CustomAnalyzer(CharArraySet stopWordSet,boolean usePorterFilter){

        this.stopWordSet = stopWordSet;
        this.usePorterFilter = usePorterFilter;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        if (stopWordSet!=null)
            filter = new StopFilter(filter, stopWordSet);
        if (usePorterFilter)
            filter = new PorterStemFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
