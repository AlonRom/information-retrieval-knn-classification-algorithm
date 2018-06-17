package Lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LuceneIndexing 
{
    List<ClassificationDocument> _docList;
    IndexWriter _docIndexWriter;

    public LuceneIndexing(List<ClassificationDocument> docList)
    {
        _docList = docList;
        _docIndexWriter = null;
    }

    public void IndexDocList()
    {
        IndexWriter docIndexWriterEmptyStopWords = createIndexWriter(null,false);
        IndexDocListWithIndexWriter(docIndexWriterEmptyStopWords);
        CharArraySet stopWords = GetMostFrequentWords(docIndexWriterEmptyStopWords,Constants.STOP_WORDS_COUNT);
        _docIndexWriter = createIndexWriter(stopWords,true);
        IndexDocListWithIndexWriter(_docIndexWriter);
    }

    private CharArraySet GetMostFrequentWords(IndexWriter index, int numberOfStopWords)
    {
        try
        {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);

            //get high frequent terms
            TermStats[] states = HighFreqTerms.getHighFreqTerms(reader, Constants.STOP_WORDS_COUNT, Constants.CONTENT,
                    new HighFreqTerms.TotalTermFreqComparator());
            List<TermStats> stopWordsCollection = Arrays.asList(states);
            //fill list of stop words
            System.out.print("Stop Words: ");
            for (TermStats term : states)
            {
                System.out.print(term.termtext.utf8ToString() + " ");
            }
            System.out.println();
            //return a char array set in order to initialize other analyzers with stop words consideration
            return new CharArraySet(stopWordsCollection, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private IndexWriter createIndexWriter(CharArraySet StopWord,boolean usePorterFilter)
    {
        try 
        {
            CustomAnalyzer analyzer = new CustomAnalyzer(StopWord, usePorterFilter);
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
            IndexWriterConfig docsFileConfig = new IndexWriterConfig(analyzer);
            docsFileConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            //create a writer for finding the stop words
            IndexWriter indexWriter = new IndexWriter(docsFileIndexdirectory, docsFileConfig);
            return indexWriter;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void IndexDocListWithIndexWriter(IndexWriter writer)
    {
        for (ClassificationDocument classDoc:_docList)
        {
            Document document = new Document();
            //Add content to document
            document.add(new TextField(Constants.CONTENT,new StringReader(classDoc.getContent())));
            //Add title to document
            document.add(new TextField(Constants.TITLE,new StringReader(classDoc.getTitle())));
            try 
            {
                if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) 
                {
                    //new index, so we just add the document (no old document can be there):
                    writer.addDocument(document);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
