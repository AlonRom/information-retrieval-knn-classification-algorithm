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
import static org.apache.lucene.search.DocIdSetIterator.NO_MORE_DOCS;

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

    public HashMap<Integer,Float>[] TfIDFVector()
    {
        try{
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            TermsEnum termEnum = MultiFields.getTerms(reader, Constants.CONTENT).iterator();
            BytesRef bytesRef;
            int termID=0;
            int NumberDocWithTerm;
            Float idf,wtf;
            HashMap<Integer,Float>[] tfIDFVector=new HashMap[reader.numDocs()];
            while ((bytesRef = termEnum.next()) != null){
                System.out.println("Term"+termID);
                if (termEnum.seekExact(bytesRef))
                {
                    NumberDocWithTerm = reader.docFreq(new Term(Constants.CONTENT, bytesRef));
                    idf = (float) Math.log10(reader.maxDoc() / NumberDocWithTerm);
                    PostingsEnum post= termEnum.postings(null);
                    int docID;
                    while((docID = post.nextDoc()) != NO_MORE_DOCS){
                        wtf = (float)(1 + Math.log10(post.freq()));
                        if (tfIDFVector[docID] == null){
                            HashMap<Integer,Float> map = new HashMap<>();
                            map.put(termID,wtf*idf);
                            tfIDFVector[docID] = map;
                        }
                        else {
                            HashMap <Integer,Float> map = tfIDFVector[docID];
                            map.put(termID,wtf*idf);
                            tfIDFVector[docID] = map;
                        }
                    }
                }
                termID++;
            }
            return tfIDFVector;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

//    public Integer[] getClassIDArr(){
//        try {
//            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(Constants.DOCS_FILE_INDEX_PATH));
//            //open index reader
//            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
//            Terms vector;
//            TermsEnum termEnum;
//            BytesRef bytesRef;
//            Integer arr[] = new Integer[reader.numDocs()];
//            for (int docID=0;docID<reader.numDocs();docID++) {
//                vector = reader.getTermVector(docID, Constants.CLASSID);
//                if (vector == null) {
//                    continue;
//                }
//                termEnum = vector.iterator();
//                while ((bytesRef = termEnum.next()) != null) {
//                    if (termEnum.seekExact(bytesRef)) {
//                        arr[docID] = new Integer(bytesRef.utf8ToString());
//                        if (arr[docID]==25)
//                            System.out.println(docID);
//                    }
//                }
//            }
//            return arr;
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }



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
            VecTextField field = new VecTextField(Constants.CONTENT, classDoc.getContent(),Field.Store.YES);
            document.add(field);

            //Add title to document
            field = new VecTextField(Constants.TITLE, classDoc.getTitle(),Field.Store.YES);
            document.add(field);

            //Add ClassId to document
            field = new VecTextField(Constants.CLASSID, classDoc.getClassID().toString(),Field.Store.YES);
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
