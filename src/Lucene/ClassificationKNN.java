package Lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.classification.KNearestNeighborClassifier;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.Reader;
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

            KNearestNeighborClassifier classifier = new KNearestNeighborClassifier(reader,sim,ana,null,k,1,1,"dor",Constants.CONTENT,Constants.TITLE);
            return classifier;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return null;
        }


    }

}
