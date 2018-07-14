package Lucene;


import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
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
        CharArraySet stopWords = new StandardAnalyzer().getStopwordSet();
        _docIndexWriter = createIndexWriter(stopWords,true,_path);
        IndexDocListWithIndexWriter(_docIndexWriter);
    }

    //not in use because we change to Lucene's KNN Classifier
    public BytesRefHash getTermDicitionary(){
        try {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            TermsEnum termEnum = MultiFields.getTerms(reader, Constants.CONTENT).iterator();
            BytesRef bytesRef;
            int termID=0;
            BytesRefHash dicitionary = new BytesRefHash();
            while ((bytesRef = termEnum.next()) != null) {

                if (termEnum.seekExact(bytesRef)) {
                    if (dicitionary.find(bytesRef)==-1) {
                        dicitionary.add(bytesRef);
                        termID++;
                    }
                }
            }
            System.out.println(termID);
            return dicitionary;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //not in use because we change to Lucene's KNN Classifier
    public HashMap<Integer,Float>[] TfIDFVector(BytesRefHash termDictionary){
        try {
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(_path));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            TermsEnum termEnum = MultiFields.getTerms(reader, Constants.CONTENT).iterator();
            BytesRef bytesRef;
            int termID = 0;
            int NumberDocWithTerm;
            Float idf, wtf;
            HashMap<Integer,Float>[] tfIDFVector = new HashMap[reader.numDocs()];
            while ((bytesRef = termEnum.next()) != null) {
                System.out.println("Term" + termID);
                if (termEnum.seekExact(bytesRef)) {
                    NumberDocWithTerm = reader.docFreq(new Term(Constants.CONTENT, bytesRef));
                    idf = (float) Math.log10(reader.maxDoc() / NumberDocWithTerm);
                    PostingsEnum post = termEnum.postings(null);
                    int docID;
                    while ((docID = post.nextDoc()) != NO_MORE_DOCS) {
                        wtf = (float) (1 + Math.log10(post.freq()));
                        addTermToVector(tfIDFVector, wtf, idf, docID, termDictionary.find(bytesRef),termDictionary.size());
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




    //not in use because we change to Lucene's KNN Classifier
    public HashMap<Integer, Float>[] testTfIDFVector(String trainDocPath, String testDocPath, BytesRefHash dicitionary)
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
                    if (dicitionary.find(bytesRef)!=-1){
                        PostingsEnum post = termEnum.postings(null);
                        int docID;
                        Term term = new Term(Constants.CONTENT,bytesRef);
                        NumberDocWithTerm = trainReader.docFreq(term);
                        idf = (float) Math.log10(trainReader.maxDoc() / NumberDocWithTerm);
                        while((docID = post.nextDoc()) != NO_MORE_DOCS){
                            wtf = (float)(1 + Math.log10(post.freq()));
                            addTermToVector(tfIDFVector,wtf,idf,docID,dicitionary.find(bytesRef),dicitionary.size());
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

    private void addTermToVector(HashMap<Integer,Float>[] vector,Float wtf, Float idf, int docID, int termID, int size){
        if (vector[docID] == null){
            HashMap<Integer,Float> vec = new HashMap<>();
            vec.put(termID,wtf*idf);
            vector[docID] = vec;
        }
        else {
           HashMap<Integer,Float> vec = vector[docID];
            vec.put(termID,wtf*idf);
            vector[docID] = vec;
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



           field = new VecTextField(Constants.CLASSID,classDoc.getClassID().toString(),Field.Store.YES);
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
