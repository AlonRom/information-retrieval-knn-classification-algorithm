package Lucene;

import java.io.IOException;
import java.util.List;

public class DocumentClassificationExperiment 
{
	static String _inputFilePath;
	static List<String> _inputParameters;
	static String _trainFilePath;
	static String _testFilePath;
	static String _outputFilePath;
	static int _numberOfNeighbors;

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
	}
}