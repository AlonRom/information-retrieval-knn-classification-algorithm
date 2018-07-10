package Lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DocumentClassificationExperiment 
{
	static String _inputFilePath;
	static List<String> _inputParameters;
	static String _trainFilePath;
	static String _testFilePath;
	static String _outputFilePath;
	static int _numberOfNeighbors;
    static List<ClassificationDocument> trainDocList;
    static List<ClassificationDocument> testDocList;
    static HashMap<Integer,Integer> termDictionary;
    static HashMap<Integer,Float>[] trainTfIdfVectorArray;
    static HashMap<Integer,Float>[] testTfIdfVectorArray;


	public static void main(String[] args) throws IOException
	{
		if(args.length < 1)
		{
			System.out.println("Parameters file argument is missing!");
			System.out.println("Exiting Retrieval Experiment...");
			System.exit(1);
		}

		//get parameters file data
		try
		{
			System.out.println("Parameters file path:..." + args[0]);
			_inputFilePath = args[0];
			_inputParameters = TextFileReader.ReadFileParametres(_inputFilePath);		
			_trainFilePath = _inputParameters.get(0);
			_testFilePath = _inputParameters.get(1);
			_outputFilePath = _inputParameters.get(2);
			_numberOfNeighbors = TextFileReader.TryParseInt(_inputParameters.get(3));
		}
		catch (ArrayIndexOutOfBoundsException e) 
		{
			 System.out.println("Failed parsing parameters in '" + _inputFilePath + "' file!");
			 System.out.println("Exiting Retrieval Experiment...");
			 System.exit(1);
		}

		catch(IndexOutOfBoundsException e)
		{
			System.out.println("One or more paramters are missing in '" + _inputFilePath + "' file!");
			System.out.println("Exiting Retrieval Experiment...");
			System.exit(1);
		}
		catch(NumberFormatException e)
		{
			System.out.println("K parameter is not a number in '" + _inputFilePath + "' file!");
			System.out.println("Exiting Retrieval Experiment...");
			System.exit(1);
		}		
		catch (IOException e) 
		{
			 System.out.println("Failed reading '" + _inputFilePath + "' file!");
			 System.out.println("Exiting Retrieval Experiment...");
			 System.exit(1);
		}

		train(_trainFilePath);
		test(_testFilePath,_numberOfNeighbors);
	}

	public static void train(String trainFilePath)
	{
		trainDocList = TextFileReader.getListFromCsv(trainFilePath,5);
        LuceneIndexing indexer = new LuceneIndexing(trainDocList,Constants.TRAIN_DOCS_INDEX_PATH);
        System.out.println("Starting Indexing training set...");
        indexer.IndexDocList();
		System.out.println("Index Ended");
		System.out.println("Matching Each Term an Integer");
		termDictionary = indexer.getTermDicitionary();
		System.out.println("Building Train TFIDF Vectors");
		trainTfIdfVectorArray = indexer.TfIDFVector();
		System.out.println("Done!");
	}

	public static void test(String testFilePath, int numberOfNeighbors){
		testDocList = TextFileReader.getListFromCsv(testFilePath,1);
		LuceneIndexing indexer = new LuceneIndexing(testDocList,Constants.TEST_DOCS_INDEX_PATH);
		System.out.println("Starting Indexing test set...");
		indexer.IndexDocList();
		System.out.println("Index Ended");
		System.out.println("Building Test TFIDF Vectors");
		testTfIdfVectorArray = indexer.testTfIDFVector(Constants.TRAIN_DOCS_INDEX_PATH,Constants.TEST_DOCS_INDEX_PATH,termDictionary);
		System.out.println("Done!");
		termDictionary = null;
		KnnClassificationL2Distance classifier = new KnnClassificationL2Distance(trainTfIdfVectorArray,testTfIdfVectorArray,
				trainDocList,_numberOfNeighbors,Constants.NUMBER_OF_CATEGORIES);
		Integer[] testClassifiction = classifier.getDocsClassification();
		int sum=0;
		for (int i=0;i<testClassifiction.length;i++){
			if (testClassifiction[i]==testDocList.get(i).getClassID()){
				sum++;
			}
		}
		System.out.println(sum/testClassifiction.length);




    }
}