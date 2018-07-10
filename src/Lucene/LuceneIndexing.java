package Lucene;

import com.sun.org.apache.xerces.internal.util.SymbolTable;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.nio.file.Paths;
import java.util.*;

import static Lucene.Constants.CONTENT;
import static org.apache.lucene.search.DocIdSetIterator.NO_MORE_DOCS;

public class LuceneIndexing 
{
    List<ClassificationDocument> _docList;
    IndexWriter _docIndexWriter;
    String _path;

    public LuceneIndexing(List<ClassificationDocument> docList, String path)
    {
        _docList = docList;
        _docIndexWriter = null;
        _path = path;
    }

    public void IndexDocList()
    {
        //IndexWriter docIndexWriterEmptyStopWords = createIndexWriter(null,false,Constants.TRAIN_DOCS_INDEX_PATH);
        //IndexDocListWithIndexWriter(docIndexWriterEmptyStopWords);
        //CharArraySet stopWords = GetMostFrequentWords(docIndexWriterEmptyStopWords,Constants.STOP_WORDS_COUNT);
        CharArraySet stopWords = new StandardAnalyzer().getStopwordSet();
        _docIndexWriter = createIndexWriter(stopWords,true,_path);
        IndexDocListWithIndexWriter(_docIndexWriter);
    }

    public HashMap<Integer,Integer> getTermDicitionary(){
        try {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            TermsEnum termEnum = MultiFields.getTerms(reader, Constants.CONTENT).iterator();
            BytesRef bytesRef;
            int termID=0;
            HashMap<Integer,Integer> dicitionary = new HashMap<>();
            while ((bytesRef = termEnum.next()) != null) {

                if (termEnum.seekExact(bytesRef)) {
                    dicitionary.put(bytesRef.hashCode(),termID);
                    termID++;
                }
            }
            return dicitionary;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public SparseVector[] TfIDFVector(HashMap<Integer,Integer> termDictionary){
        try {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            TermsEnum termEnum = MultiFields.getTerms(reader, Constants.CONTENT).iterator();
            BytesRef bytesRef;
            int termID = 0;
            int NumberDocWithTerm;
            Float idf, wtf;
            SparseVector[] tfIDFVector=new SparseVector[reader.numDocs()];
            while ((bytesRef = termEnum.next()) != null) {
                System.out.println("Term" + termID);
                if (termEnum.seekExact(bytesRef)) {
                    NumberDocWithTerm = reader.docFreq(new Term(Constants.CONTENT, bytesRef));
                    idf = (float) Math.log10(reader.maxDoc() / NumberDocWithTerm);
                    PostingsEnum post = termEnum.postings(null);
                    int docID;
                    while ((docID = post.nextDoc()) != NO_MORE_DOCS) {
                        wtf = (float) (1 + Math.log10(post.freq()));
                        addTermToVector(tfIDFVector, wtf, idf, docID, termID,termDictionary.size());
                    }
                }
            }
            return tfIDFVector;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public HashMap<Integer,Float>[] TfIDFVector()
    {
        try{
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
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
                        addTermToVector(tfIDFVector,wtf,idf,docID,termID);
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


    public HashMap<Integer,Float>[] testTfIDFVector(String trainDocPath, String testDocPath, HashMap<Integer,Integer> dicitionary)
    {
        try{
            //open test reader
            Directory testIndexdirectory = FSDirectory.open(Paths.get(testDocPath));
            IndexReader testReader = DirectoryReader.open(testIndexdirectory);

            //open train reader
            Directory trainIndexdirectory = FSDirectory.open(Paths.get(testDocPath));
            IndexReader trainReader = DirectoryReader.open(trainIndexdirectory);

            TermsEnum termEnum = MultiFields.getTerms(testReader, Constants.CONTENT).iterator();
            HashMap<Integer,Float>[] tfIDFVector=new HashMap[testReader.numDocs()];
            BytesRef bytesRef;
            Float idf,wtf;
            int NumberDocWithTerm;

            while ((bytesRef = termEnum.next()) != null) {
                if (termEnum.seekExact(bytesRef)) {
                    if (dicitionary.containsKey(bytesRef.hashCode())){
                        PostingsEnum post = termEnum.postings(null);
                        int docID;
                        Term term = new Term(Constants.CONTENT,bytesRef);
                        NumberDocWithTerm = trainReader.docFreq(term);
                        idf = (float) Math.log10(trainReader.maxDoc() / NumberDocWithTerm);
                        while((docID = post.nextDoc()) != NO_MORE_DOCS){
                            wtf = (float)(1 + Math.log10(post.freq()));
                            addTermToVector(tfIDFVector,wtf,idf,docID,dicitionary.get(bytesRef.hashCode()));
                        }

                    }

                }
            }
            return tfIDFVector;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void addTermToVector(SparseVector[] vector,Float wtf, Float idf, int docID, int termID, int size){
        if (vector[docID] == null){
            SparseVector vec = new SparseVector(size);
            vec.put(termID,wtf*idf);
            vector[docID] = vec;
        }
        else {
           SparseVector vec = vector[docID];
            vec.put(termID,wtf*idf);
            vector[docID] = vec;
        }

    }

    private void addTermToVector(HashMap<Integer,Float>[] vector,Float wtf, Float idf, int docID, int termID){
        if (vector[docID] == null){
            HashMap<Integer,Float> map = new HashMap<>();
            map.put(termID,wtf*idf);
            vector[docID] = map;
        }
        else {
            HashMap <Integer,Float> map = vector[docID];
            map.put(termID,wtf*idf);
            vector[docID] = map;
        }

    }



    private CharArraySet GetMostFrequentWords(IndexWriter index, int numberOfStopWords)
    {
        try
        {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
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
