package Lucene;

import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.KNearestNeighborClassifier;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    static BytesRefHash termDictionary;
    static HashMap<Integer,Float>[] trainTfIdfVectorArray;
    static HashMap<Integer,Float>[] testTfIdfVectorArray;
    static KNearestNeighborClassifier classifier1;


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



		trainDocList = TextFileReader.getListFromCsv(trainFilePath,1);
        LuceneIndexing indexer = new LuceneIndexing(trainDocList,Constants.TRAIN_DOCS_INDEX_PATH);
        System.out.println("Starting Indexing training set...");
        indexer.IndexDocList();
		System.out.println("Index Ended");
		ClassificationKNN classi = new ClassificationKNN(Constants.TRAIN_DOCS_INDEX_PATH,_numberOfNeighbors);
		classifier1= classi.getClassifier();
	}

	public static void test(String testFilePath, int numberOfNeighbors){
		testDocList = TextFileReader.getListFromCsv(testFilePath,1);
		List<ClassificationResult<BytesRef>> resultList=null;
		Integer[] testClassifiction = new Integer[testDocList.size()];
		for (ClassificationDocument doc : testDocList){
			try {
				long startTime = System.nanoTime();
				resultList = classifier1.getClasses(doc.getContent(), 1);
				testClassifiction[doc.getDocId()] = new Integer(resultList.get(0).getAssignedClass().utf8ToString());
				long endTime = System.nanoTime();
				long result1 = endTime-startTime;
				System.out.println("Ended Classify " + doc.getDocId() + " in " + result1);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}

		ClassificationEvaluate evaluate = new ClassificationEvaluate(testClassifiction,testDocList,Constants.NUMBER_OF_CATEGORIES);
		evaluate.evaluate();
		double microF1 = evaluate.getMicroF1();
		double macroF1 = evaluate.getMacroF1();
		System.out.println("for " + numberOfNeighbors + " the micro F1 is " + microF1 + "  and macro F1 is " + macroF1);
		evaluate.printResultsToFile(_outputFilePath,testFilePath);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		System.out.println(formatter.format(date));




	}
}