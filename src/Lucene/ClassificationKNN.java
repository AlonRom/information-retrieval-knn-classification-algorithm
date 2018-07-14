package Lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.classification.KNearestNeighborClassifier;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Paths;


public class ClassificationKNN {

    private int k;
    private String readerPath;

    public ClassificationKNN(String readerPath, int k){
        this.readerPath = readerPath;
        this.k = k;
    }

    public KNearestNeighborClassifier getClassifier(){

        try{
            Directory docsFileIndexdirectory = FSDirectory.open(Paths.get(readerPath));
            //open index reader
            IndexReader reader = DirectoryReader.open(docsFileIndexdirectory);
            CustomAnalyzer ana = new CustomAnalyzer(new StandardAnalyzer().getStopwordSet());
            ClassicSimilarity sim = new ClassicSimilarity();
            // Create the KNN classifier
            KNearestNeighborClassifier classifier = new KNearestNeighborClassifier(reader,sim,ana,null,k,Constants.MIN_DOCS_FREQ,Constants.MIN_TERM_FREQ,Constants.CLASSID,Constants.CONTENT,Constants.TITLE);
            return classifier;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return null;
        }


    }

}
