package org.safs.text;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.*;

//import org.safs.Log;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This FileReader class is intended as a generic file instance class for reading text files.<br>
 * <p>
 * This class represents the most basic reader functionality.  It will simply read a line 
 * of text for each call to readLine().  It uses a java.io.BufferedReader as the underlying 
 * IO mechanism.  The file to be read is expected to be ANSI or a supported Charset encoding.
 * <p>
 * @author Carl Nagle, SAS Institute
 * @version 1.0, 03/24/2004
 * @version 11/03/2006 (Carl Nagle) modified to accept mixed-mode UTF-8 files. 
 * @version 03/21/2007 (Carl Nagle) modified to strip utf-8 leader bytes when needed.
 * @version 07/16/2010 (Carl Nagle) modified to support other Charset encodings.
 * @version 12/16/2014 (Carl Nagle) Fix NullPointerException on empty files in readLine.
 * 
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 ******************************************************************************************/
public class FileLineReader {

	public static int DEFAULT_BUFFER_SIZE = 1024 * 10;
	
	protected String fullpath = new String();
	protected String filename = new String();
	protected InputStream stream = null;
	protected File file = null;	
	protected BufferedReader reader = null;
	protected boolean eof = false;
	protected boolean firstline = true;
	protected String charset = "UTF-8";//default
	
	protected String linetext = null; // current (last read0 line of text

	
	/*******************************************************************************************
	 * This constructor will create an inoperable (Closed) file object.  No use whatsoever. :)
	 ******************************************************************************************/
	public FileLineReader (){;}

	/*******************************************************************************************
	 * The constructor used by the FileReader if the input is a File.
	 * 
	 * All subclasses using File MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(file);
	 * <p>
	 * @param file A File object that references the file to be opened.
	 ******************************************************************************************/
	public FileLineReader (File file){
		setFile(file);
	}

	/*******************************************************************************************
	 * The constructor used by the FileReader if the input is a File.
	 * 
	 * All subclasses using File MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(file);
	 * <p>
	 * @param file A File object that references the file to be opened.
	 * @param encoding.  a valid Charset encoding like "UTF-16", etc...
	 ******************************************************************************************/
	public FileLineReader (File file, String encoding){
		charset = encoding;
		setFile(file);
	}

	/*******************************************************************************************
	 * The constructor used by the FileReader if the input is an InputStream.  For example, 
	 * resources or files stored in JARs loaded via ClassLoaders.  Files can be loaded from 
	 * the file system if the directory is in the System CLASSPATH where getSystemResourceAsStream 
	 * can find it.
	 * 
	 * All subclasses using InputStreams MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(stream);
	 * <p>
	 * @param stream an InputStream object that references the data to be opened.
	 * @see ClassLoader#getSystemResourceAsStream(java.lang.String)
	 ******************************************************************************************/
	public FileLineReader (InputStream stream){
		setStream(stream);
	}

	/*******************************************************************************************
	 * The constructor used by the FileReader if the input is an InputStream.  For example, 
	 * resources or files stored in JARs loaded via ClassLoaders.  Files can be loaded from 
	 * the file system if the directory is in the System CLASSPATH where getSystemResourceAsStream 
	 * can find it.
	 * 
	 * All subclasses using InputStreams MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(stream);
	 * <p>
	 * @param stream an InputStream object that references the data to be opened.
	 * @param encoding.  a valid Charset encoding like "UTF-16", etc...
	 * @see ClassLoader#getSystemResourceAsStream(java.lang.String)
	 ******************************************************************************************/
	public FileLineReader (InputStream stream, String encoding){
		charset = encoding;
		setStream(stream);
	}

	private static Object logger;
	private static Method debug;
	private static Class logclass;
	private static boolean logtried = false;
	private static void log(String message){
		if(! logtried){
			logtried = true;
			try{
				logclass = Class.forName("org.safs.Log");
				logger = logclass.newInstance();
				debug = logclass.getDeclaredMethod("debug", Object.class);
			}catch(Throwable ignore){}
		}
		if(logger != null && debug != null){
			try{ debug.invoke(logger, message);}catch(Throwable ignore){}
		}
	}
	
	/**
	 * Allows us to set (and change) the file that will be read.
	 * Setting the file also opens the file for reading (assuming it is a valid file).
	 */
	protected void setFile(File newfile){
		if (newfile != null){
			this.file = newfile;
			open();
			if(! isClosed()) {
				filename = file.getName();
				fullpath = file.getPath();
			}
		}
	}

	/**
	 * Allows us to set (and change) the data that will be read.
	 * Setting the stream also "open()s" it for reading through the BufferedReader.
	 */
	protected void setStream(InputStream stream){
		if (stream != null){
			this.stream = stream;
			open();
			if(! isClosed()) {
				log("FileLineReader InputStream instanceof:"+ stream.getClass().getName());
				filename = stream.toString();
				fullpath = stream.toString();
			}
		}else{
			log("FileLineReader InputStream is NULL and will not function!");
		}
	}
	
	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the short format of the filename--no path information. An empty string if this 
	 *         object failed to open the file.
	 ******************************************************************************************/
	public  String  getFilename (){ return filename.toString();}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the full filename path of the file.  An empty string if this object failed to 
	 *         open.
	 ******************************************************************************************/
	public  String  getFullpath (){ return fullpath.toString();}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return true if the BufferedReader was not successfully opened or has been closed (nulled).
	 ******************************************************************************************/
	public boolean  isClosed    (){ return (reader==null);	}	

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return true if we have reached the end of the file.  Reaching end of file does not 
	 *         Close the file.  However, this file cannot be reset.  So reaching end of file 
	 *         pretty much means this object has reached the end of its usefulness.
	 ******************************************************************************************/
	public boolean  isEOF       (){ return eof;}
	

	/*******************************************************************************************
	 * This routine is the most basic readLine function.  It simply gets the next line in the 
	 * file.  It will remove any leading ignorable Unicode FORMAT character from the first line 
	 * of a file.  This is generally required when reading UTF-8 files with the leading FORMAT 
	 * marker (BOM).  
	 * <p>
	 * Subclasses that intend to maintain any kind of pointers MUST override this method in order 
	 * to maintain their pointer counts but should still call this method with super.readLine() 
	 * to retain the Unicode character removal on the retrieved text.
	 * <p>
	 * It is still desirable to maintain the basic functionality of returning the text 
	 * value unmodified in each subclass intending to return lines of text.  A subclass wishing 
	 * to embellish or otherwise process the line of text before returning it should do so in 
	 * a different method specific to that subclass.
	 * <p>
	 * @return the next line in the file.  If the file is closed or has reached the end of file, 
	 *         then a NULL value is returned and EOF is set true.
	 ******************************************************************************************/
	public String readLine (){ 

		if((isClosed())||(isEOF())) return null;
		
		try{
			linetext = reader.readLine();
			if (firstline){
				firstline = false;
				// strip UTF-8 identifier if present
				if( (!(linetext==null)) && linetext.length()> 0){
					char char0 = linetext.charAt(0);
					if ((Character.getType(char0)==Character.FORMAT)&&
					    (Character.isIdentifierIgnorable(char0))){
						try{ linetext = linetext.substring(1);}
						catch(IndexOutOfBoundsException ix){
							linetext = "";
						}
					}
				}
			}
			
			// if EOF
			eof = (linetext==null);
			
		}catch(IOException e){ 
			close();
		}
		return linetext;
	}


	/*******************************************************************************************
	 * Resets important fields during Close and other operations.
	 * Subclasses should override this function to add any additional pointer or value reset 
	 * operations.  Those subclasses should still call the superclass method with:
	 * <p>
	 * &nbsp; &nbsp; super.resetpointers();
	 * <p>
	 ******************************************************************************************/
	protected void resetpointers(){
		linetext = null;
		eof = false;
		firstline = true;
		try{
			if (stream instanceof InputStream) stream.reset();
		}
		catch(Exception e){
			log("FileLineReader InputStream may not support Reset:"+e.getClass().getSimpleName()+":"+e.getMessage());
		}
	}
	
	/**
	 * Called internally by open().
	 * Sets BufferedReader to a valid reader or null; 
	 */
	protected void openFile(){
		if( (! isClosed()) || (file == null)) return;
		if(! file.isFile()){
			log("FileLineReader attempt to open '"+ file.getAbsolutePath() +"' failed."); 
			close(); 
			return; 
		}		
		try{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(charset)), DEFAULT_BUFFER_SIZE);
			resetpointers();
		}catch(IOException e){
			close();
		}	
	}
	
	/**
	 * Called internally by open().
	 * Sets BufferedReader to a valid reader or null; 
	 */
	protected void openStream(){
		if( (! isClosed()) || (stream == null)) return;
		try{
			if((stream.available()< 1)){
				log("FileLineReader attempt to open InputStream '"+ fullpath +"' failed."); 
				close(); 
				return;
			} 
			reader = new BufferedReader(new InputStreamReader(stream, Charset.forName(charset)), DEFAULT_BUFFER_SIZE);
			resetpointers();
		}catch(IOException e){
			close();
		}	
	}

	/*******************************************************************************************
	 * Subclasses should not need to override this function unless some very interesting things 
	 * need to be done before or after the file is opened.  However, since the file is opened 
	 * during object construction, all that interesting stuff should be handled there, not here.
	 * <p>
	 ******************************************************************************************/
	public void open(){
	
		if( (! isClosed()) || ((file == null)&&(stream == null))) return;
		if ( file instanceof File) {openFile();}
		else if (stream instanceof InputStream){ openStream();} 
	}

	/**
	 * Shutdown (close) the file reader if it exists.  The reader will be nulled on exit.
	 */
	protected void closeReader(){
		if (reader != null){
			try{
				reader.close();
			}catch(Exception e){;}
			finally{
				reader = null;
			}
		}		
	}
				
	/*******************************************************************************************
	 * A call to this routine is essentially telling the object that its useful life is over.
	 * This is the equivalent of "terminate".  You are soon to be NULLED and garbage collected.  
	 * The open BufferedReader is closed by calling closeReader.  The remaining API 
	 * will respond accordingly signalling that the file isClosed.
	 * <p>
	 * Subclasses should not need to override this function unless there is additional cleanup 
	 * needed prior to getting whacked.  If the method is overridden, the subclass should still 
	 * call this routine with:
	 * <p>
	 * &nbsp; &nbsp; super.close();
	 * <p>
	 * @see #closeReader()
	 * @see #resetpointers()
	 ******************************************************************************************/
	public void close(){ 
		closeReader();
		resetpointers();
	}
}