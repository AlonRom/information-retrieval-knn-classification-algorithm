package Lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;

import static Lucene.Constants.CONTENT;
import static org.apache.lucene.document.TextField.TYPE_STORED;

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
        //IndexWriter docIndexWriterEmptyStopWords = createIndexWriter(null,false,Constants.DOCS_FILE_INDEX_PATH);
        //IndexDocListWithIndexWriter(docIndexWriterEmptyStopWords);
        //CharArraySet stopWords = GetMostFrequentWords(docIndexWriterEmptyStopWords,Constants.STOP_WORDS_COUNT);
        CharArraySet stopWords = new StandardAnalyzer().getStopwordSet();
        _docIndexWriter = createIndexWriter(stopWords,true,Constants.DOCS_FILE_INDEX_PATH);
        IndexDocListWithIndexWriter(_docIndexWriter);
    }

    public List<HashMap<BytesRef,Float>> TfIDFVector(){
        try {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            List<HashMap<BytesRef,Float>> tfIdfVectorList = new ArrayList<>(reader.maxDoc());
            HashMap<BytesRef,Float> idfMap = new HashMap<>();
            float tf,wtf,tfIdf;
            int NumberDocWithTerm;
            TermsEnum termEnum;
            String term;
            BytesRef bytesRef;
            HashMap<BytesRef,Float> tfIdfMap;
            Terms vector;
            PostingsEnum postingEnum;
            float idf;
            for (int docID=0; docID< reader.maxDoc(); docID++) {
                vector = reader.getTermVector(docID, Constants.CONTENT);
                //There is empty line somewhere in the database, therefore there is docID with no terms.
                if (vector==null){
                    tfIdfVectorList.add(docID,null);
                    continue;
                }
                termEnum = vector.iterator();
                tfIdfMap = new HashMap<>();

                while ((bytesRef = termEnum.next()) != null)
                {
                    if (termEnum.seekExact(bytesRef)) {
                        //term = bytesRef.utf8ToString();

                        //calculate the IDF of term
                        if (idfMap.containsKey(bytesRef)){
                            idf = idfMap.get(bytesRef);
                        }
                        else {
                            NumberDocWithTerm = reader.docFreq(new Term(Constants.CONTENT, bytesRef));
                            idf = (float) Math.log10(reader.maxDoc() / NumberDocWithTerm);
                            // Store IDF value if it is popular within a lot of documents. To feature use.
                            if (NumberDocWithTerm>10000)
                                idfMap.put(bytesRef,new Float(idf));
                       }

                       //Calculate the weighted TF of a term
                       wtf = (float)(1 + Math.log10(termEnum.totalTermFreq()));
                        tfIdf = wtf * idf;
                        tfIdfMap.put(bytesRef,new Float(tfIdf));
                    }
                }
                //Add the tfIDF vector to the list
                tfIdfVectorList.add(docID,tfIdfMap);
                System.out.println(docID);
            }
            return tfIdfVectorList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private CharArraySet GetMostFrequentWords(IndexWriter index, int numberOfStopWords)
    {
        try
        {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);

            //get high frequent terms
            TermStats[] states = HighFreqTerms.getHighFreqTerms(reader, Constants.STOP_WORDS_COUNT, CONTENT,
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

    private IndexWriter createIndexWriter(CharArraySet StopWord,boolean usePorterFilter, String path)
    {
        try 
        {
            CustomAnalyzer analyzer = new CustomAnalyzer(StopWord, usePorterFilter);
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(path));
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
            VecTextField field = new VecTextField(Constants.CONTENT, classDoc.getContent().replaceAll("[^A-Za-z]",""),Field.Store.YES);
            document.add(field);

            //Add title to document
            field = new VecTextField(Constants.TITLE, classDoc.getTitle(),Field.Store.YES);
            document.add(field);
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
