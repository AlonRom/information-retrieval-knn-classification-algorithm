package Lucene;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileReader 
{ 
	public static List<String> ReadFileParametres(String inputFile) throws IOException 
	{
		List<String> lines = null;
		List<String> parameters = new ArrayList<>();
		try 
		{
			lines = Files.readAllLines(Paths.get(inputFile));
			
			//get parameters values
	        for (String line : lines) 
	        {
	        	if(line != null && !line.isEmpty()) 
	        	{
		        	parameters.add(line.split("=")[1]);	  
	        	}   
	        }
	        return parameters;
		} 
		catch (ArrayIndexOutOfBoundsException e) 
		{
			e.printStackTrace();
			throw e;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			throw e;
		}		
	}
	
	public static int TryParseInt(String strValue) 
	{  
	     try 
	     {  
	         int intValue = Integer.parseInt(strValue.trim());  
	         return intValue;  
	     } 
	     catch (NumberFormatException e) 
	     {  
	    	 e.printStackTrace();
    		 throw e;
	     }  
	}

	public static List<ClassificationDocument> getListFromCsv(String csvPath)
	{
		BufferedReader br = null;
		String line = null;
		String csvSplitBy = ",";
		
		List<ClassificationDocument> docList = new ArrayList<ClassificationDocument>();
		try {

			br = new BufferedReader(new FileReader(csvPath));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] str = line.split(csvSplitBy);
				Integer docId = new Integer(str[0]);
				Integer classId = new Integer(str[1]);
				String title = str[2];
				String content = str[3];
				//Create the ClassificationDocument
				ClassificationDocument doc = new ClassificationDocument(docId,title,content,classId);
				//Add to list
				docList.add(doc);
			}

			return docList;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static int SplitDocuments(String inputFile, String outPath) throws Exception
	{
		try
		{
			String content = null;	
		    content = new String(Files.readAllBytes(Paths.get(inputFile)));	
			String[] parts = content.split("\\*TEXT");
			String outPathID=null;
			new File(outPath).mkdirs();
			for (String part : parts)
			{			
				if (part.equals(""))
					continue;
	
				//get doc Id
				Matcher matcher = Pattern.compile("\\d+").matcher(part);
				matcher.find();
				Integer docId = Integer.valueOf(matcher.group());
				outPathID=outPath.concat(Integer.toString(docId));
				outPathID=outPathID.concat(Constants.PARSED_DOCS_FILE_TYPE);
	
				//save the rest of the content to file
				try 
				{
					File file = new File(outPathID);
					file.createNewFile();
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outPathID));
									
					//retrieve terms for a document
					part = GetTextTerms(part);
					fileWriter.write(part);
					fileWriter.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw e;
				}
			}
			return parts.length-1;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}	
	}
	
	public static Map<Integer, String> ReadFileQueries(String inputFile, List<String> stopWords) throws Exception 
	{
		try 
		{
			String content = null;
			Map<Integer, String> queries = new HashMap<Integer, String>();	
		    content = new String(Files.readAllBytes(Paths.get(inputFile)));			
			String[] parts = content.split("\\*");
			for (String part : parts) 
			{	
				if (part.equals(""))
					continue;
				//get query Id
				Integer queryId = GetNumberFromString(part);
				
				//retrieve terms for a query
				String queryTerms = GetTextTerms(part);
				
				if(queryId > 0)
				{
				    System.out.println("Query Id " + queryId + " original query: " + part); 
					System.out.println("Query Id " + queryId + " after parsing query: " + queryTerms); 
					queries.put(queryId, queryTerms);
				}
			}
			return queries;		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}	
	}

	private static Integer GetNumberFromString(String str) 
	{
		Matcher matcher = Pattern.compile("\\d+").matcher(str);
		matcher.find();
		return Integer.valueOf(matcher.group());
	}
	
	private static String GetTextTerms(String str) 
	{
		List<String> listOfTerms = new ArrayList<String>(); 
		//split string in order to take the query lines 
		String splitStr[] =  str.split("[\\r\\n]+");
		
		if(splitStr.length > 1)
		{		
			//the query can have more then 1 line so we need to iterate each line 
			for(int i=1; i < splitStr.length; i++)
			{
				//removes spaces 
				String[] wordsInLine = splitStr[i].split("\\s+");
				//take each word in the line and add it only if it's not a stop word
				for (String word : wordsInLine)
				{
				   listOfTerms.add(word);	
				}	
			}	
		}
		
		return String.join(" ", listOfTerms);
	}
}
