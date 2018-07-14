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

	public static List<ClassificationDocument> getListFromCsv(String csvPath, int multiple)
	{
		BufferedReader br = null;
		String line = null;
		String csvSplitBy = ",";
		int firstDoc=0;
		
		List<ClassificationDocument> docList = new ArrayList<ClassificationDocument>();
		try 
		{
			br = new BufferedReader(new FileReader(csvPath));
			int i=0;
			boolean first = true;
			while ((line = br.readLine()) != null) 
			{
				i++;
				String[] str = line.split(csvSplitBy, Constants.NUMBER_OF_FILEDS_IN_CSV);
				if (first){
					firstDoc = TryParseInt(str[0]);
					first = false;
				}
				// use comma as separator

				Integer docId = (TryParseInt(str[0])-firstDoc);
				Integer classId = TryParseInt(str[1]);
				String title = str[2];
				String content = str[3].replaceAll("[^A-Za-z ]", "");

				//Create the ClassificationDocument
				ClassificationDocument doc = new ClassificationDocument(docId, title, content, classId);
				//Add to list
				docList.add(doc);

			}
			br.close();
			return docList;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
	}


	

}
