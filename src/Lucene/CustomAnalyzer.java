package Lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.IndexReader;

public class CustomAnalyzer extends Analyzer 
{
    IndexReader _reader;
    CharArraySet _stopWordSet;
    boolean _usePorterFilter;

    public CustomAnalyzer(CharArraySet stopWordSet)
    {
        _stopWordSet = stopWordSet;
        _usePorterFilter = true;
    }

    public CustomAnalyzer(CharArraySet stopWordSet,boolean usePorterFilter)
    {
        _stopWordSet = stopWordSet;
        _usePorterFilter = usePorterFilter;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) 
    {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        if (_stopWordSet!=null)
            filter = new StopFilter(filter, _stopWordSet);
        if (_usePorterFilter)
            filter = new PorterStemFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
