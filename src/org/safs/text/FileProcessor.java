/**********************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 *********************************************************************************************/
package org.safs.text;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.safs.*;
import org.safs.tools.CaseInsensitiveFile;

/*******************************************************************************************
 * Functions to process files for specific text.
 *********************************************************************************************/
public class FileProcessor
{
    //private static int DEFAULT_BUFFER_SIZE = 1024 * 10;
	protected int status = 0;

	/** 
	 * Attempts to open the file and process each line in the file 
	 * with the StringProcessor.getEmbeddedSubstring method.  The substrings to be extracted 
	 * are those in between but not including the expressions that define the start and end 
	 * of the target substring.
	 * <p>
	 * Example string: "abcdefghi"
	 * <p>
	 * regexStart="abc"<br>
	 * regexStop ="ghi"<br>
	 * <p>
	 * returned substring="def"
	 * <p>
	 * It returns a String[] of all the matching substrings.
	 * <p>
	 * Limitations: this method only returns the first match in a line. Subsequent matches
	 * appearing in the same line are ignored.
	 * 
	 * @param file case-insensitive full absolute path to the file to process.  The file is 
	 *        assumed to be in the default System codepage format.  However, if the file contains 
	 *        valid UTF-8 marker bytes (239, 187, 191) then it will be opened as a UTF-8 file.
	 * 
	 * @param regexStart regular expression that identifies the characters immediately before 
	 *        the substring to be extracted.
	 * 
	 * @param regexStop regular expression that identifies the characters immediately following 
	 *        the substring to be extracted.
	 * 
	 * @return String[] of each matching substring, one per line max.
	 * 
	 * @see StringProcessor#getEmbeddedSubstring(String, String, String)
	 * @see FileUtilities#isFileUTF8(String) 
	 **/
	public String[] getEmbeddedSubstrings(String file, String regexStart, String regexStop)
	{

		String line;
		StringBuffer theResult = new StringBuffer();
		int count = 0; //set the string index to 0
		Vector matches = new Vector();
        BufferedReader inFile = null;
        StringProcessor proc = null;
        
		try {
			proc = new StringProcessor();
			
			// do not believe this has worked for DBCS users.
			// converting back to default System codepage usage
			// InputStreamReader x = new InputStreamReader(new FileInputStream(new CaseInsensitiveFile(file).toFile()), Charset.forName("UTF-8"));
			// inFile = new BufferedReader(x);

			if (FileUtilities.isFileUTF8(file)) {
				inFile = FileUtilities.getUTF8BufferedFileReader(file);
			}else{
				inFile = FileUtilities.getSystemBufferedFileReader(file);
			}
			
			line = inFile.readLine();

			while (line != null) {					
				String value = proc.getEmbeddedSubstring(line, regexStart, regexStop);
				if (value.length()>0) matches.add(value);
				line = inFile.readLine();
			}
			status = 0;
		}
		catch (FileNotFoundException e) {
			status = 17; // RC 17 - File Open Error
			System.err.println(e);
		}
		catch (IOException e) {
			status = 18; // RC 18 - File Read Error
			System.err.println(e);
		}
		catch (Exception e) {
			status = 6; // RC 6 - Unknown Error
			System.err.println(e);
		}
		finally{
			if (inFile != null){
				try{inFile.close();}catch(Exception e){}
				inFile = null;
			}
		}
		String[] strings = new String[matches.size()];
		if (! matches.isEmpty()) {
			for (int i = 0; i < matches.size(); i++) 
				strings[i]=(String)matches.get(i);			
		}
		return strings;
	}

    /** 
     * returns the status code of the last function called.
     * The status codes are STAF return codes. 
     * <p>
     * The following codes are implemented:<br>
     * 0  - Success<br>
     * 17 - File Open Error<br>
     * 18 - File Read Error<br>
     * 6  - Unknown Error<br>
     */
	public int returnStatus() {
		return status;
	}

	/**
	 * Read the contents of a text file line-by-line.  Lines are retained in the returned 
	 * text String with a single '\n' character.
	 * 
	 * @param file case-insensitive full absolute path to the file to process.  The file is 
	 *        assumed to be in the default System codepage format.  However, if the file contains 
	 *        valid UTF-8 marker bytes (239, 187, 191) then it will be opened as a UTF-8 file.
	 * 
	 * @return String contents of the file.
	 * @see FileUtilities#isFileUTF8(String) 
	 **/
	public String getTextFileContents(String file)
	{

		String line;
		StringBuffer theResult = new StringBuffer();
        BufferedReader inFile = null;
        StringProcessor proc = null;
        
		try {
			proc = new StringProcessor();

			// do not believe this has worked for DBCS users.
			// converting back to default System codepage usage
			// InputStreamReader x = new InputStreamReader(new FileInputStream(new CaseInsensitiveFile(file).toFile()), Charset.forName("UTF-8"));
			// inFile = new BufferedReader(x);
			
			if (FileUtilities.isFileUTF8(file)) {
				inFile = FileUtilities.getUTF8BufferedFileReader(file);
			}else{
				inFile = FileUtilities.getSystemBufferedFileReader(file);
			}

			line =inFile.readLine() ;
			while (line != null) {
				theResult.append(line + '\n') ;
				line = inFile.readLine();
			}
			status = 0;
		}
		catch (FileNotFoundException e) {
			status = 17; // RC 17 - File Open Error
			System.err.println(e);
		}
		catch (IOException e) {
			status = 18; // RC 18 - File Read Error
			System.err.println(e);
		}
		catch (Exception e) {
			status = 6; // RC 6 - Unknown Error
			System.err.println(e);
		}
		finally{
			if (inFile != null){
				try{inFile.close();}catch(Exception e){}
				inFile = null;
			}
		}
		return theResult.toString();
	}
	
	/** 
	 * Opens the file for writing, write the text, and close the file.
	 * 
	 * @param filename case-insensitive full absolute path to the file to process.  The file is 
	 *        assumed to be in the default System codepage format.
	 *
	 * @param text String to write to the output file.  The text will be written in the default 
	 *        System codepage format.
	 *  
	 * @return 0 on success; 1 on failure.
	 **/
	public int writeStringToFile(String filename, String text) {

	    FileOutputStream out ;
    	PrintStream p ;
	    try
    	{
            out = new FileOutputStream(new CaseInsensitiveFile(filename).toFile()) ;
            p = new PrintStream( out );
            p.print(text);
            p.close();
	    }
    	catch (Exception e)
	    {
	    	status = 1 ;
            return status ;
    	}
    	status = 0 ;
		return status ;
	}	
}

