/**********************************************************************************************
 * Copyright 2007 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 *********************************************************************************************/
package org.safs.text;

/*******************************************************************************************
 * History:
 *
 * <br>	JUL 25, 2013	(Lei Wang)    Remove dependency on org.safs.Log so that this class can be generally used.
 * <br>	OCT 18, 2013	(Lei Wang)    Add method detectFileEncoding().
 * <br>	OCT 18, 2013	(Lei Wang)    Add some enums to serve as SeleniumPlus's Files parameter.
 *                                  Move and refactor codes from DCDriverFileCommands to treat file attribute.
 * <br>	DEC 09, 2014	(Lei Wang)    Add equals() to FileAttribute;
 *                                  Modify FileAttributeFilter: distinguish Windows and other OS for attribute 'archive'.
 * <br>	DEC 09, 2014	(Carl Nagle)    Refactored deduceFile routines into here from GenericEngine and Processor.
 * <br>	FEB 04, 2015	(Lei Wang)    Modify getBufferedFileReader(): deduce the file encoding if no encoding is provided.
 * <br>	APR 06, 2016	(Carl Nagle)    Support FileAttribute.ALLFILES Type to specify all non-VOLUMELABEL files at a location.
 *
 *********************************************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.android.auto.lib.DefaultConsoleTool;
import org.safs.android.auto.lib.Process2;
import org.safs.text.FileUtilities.FileAttribute.Type;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.RuntimeDataInterface;
import org.safs.tools.stringutils.StringUtilities;

/*******************************************************************************************
 * Functions to process files for specific text.  This includes processing of both default
 * System character encodings and UTF-8 encodings.
 * @author Carl Nagle
 *
 *********************************************************************************************/
public class FileUtilities
{

	public final static int FILE_TYPE_TEST 	  = 0;
	public final static int FILE_TYPE_BENCH   = 1;
	public final static int FILE_TYPE_PROJECT = 2;
	public final static int FILE_TYPE_DIFF 	  = 3;

	public static final String VAR_SAFSBENCHDIRECTORY    = "safsbenchdirectory";
	public static final String VAR_SAFSDATAPOOLDIRECTORY = "safsdatapooldirectory";
	public static final String VAR_SAFSDIFDIRECTORY      = "safsdifdirectory";
	public static final String VAR_SAFSLOGSDIRECTORY     = "safslogsdirectory";
	public static final String VAR_SAFSPROJECTDIRECTORY  = "safsprojectdirectory";
	public static final String VAR_SAFSTESTDIRECTORY     = "safstestdirectory";

	/** "/" */
	public static String FILE_SEP_UNIX = "/";

	/** "\\" */
	public static String FILE_SEP_WIN = "\\";

	private static int DEFAULT_BUFFER_SIZE = 1024 * 10;

	private static final String EOL = System.getProperty("line.separator");

	private static final String FILE_UTILITIES_BY_THIRDPARTY_CLASS_NAME = "org.safs.text.FileUtilitiesByThirdParty";
	private static Class<?> FileUtilitiesbYThirdPartyClass = null;
	static{
		try {
			FileUtilitiesbYThirdPartyClass = Class.forName(FILE_UTILITIES_BY_THIRDPARTY_CLASS_NAME);
		} catch (Exception e) {
			IndependantLog.warn(FileUtilities.class.getName()+": executing static block, met "+e.getClass().getSimpleName()+":"+e.getMessage());
		}
	}

	/**
	 * Provides a BufferedWriter for a FileOutputStream with encoding.
	 * @param filename case-insensitive absolute filename path.
	 * @param encoding The encoding to be used to create a OutputStreamWriter;
	 *                 If null, a default encoding indicated by "file.encoding" will be used.
	 *
	 * @return BufferedWriter open with a OutputStreamWriter to a FileOutputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist and cannot be created, cannot be opened for write operations, etc.
	 * @see CaseInsensitiveFile
	 * @see OutputStreamWriter
	 * @see FileOutputStream
	 */
	public static BufferedWriter getBufferedFileWriter(String filename, String encoding)throws FileNotFoundException{
		BufferedWriter bw = null;
		String debugmsg = FileUtilities.class.getName()+".getBufferedFileWriter(): ";
		String defaultFileEncoding = System.getProperty("file.encoding");
		//IndependantLog.debug(debugmsg+"System default Encoding is "+defaultFileEncoding);
		File afile = new CaseInsensitiveFile(filename).toFile();
		if(!afile.exists()){
			try{
				if(!afile.getParentFile().exists()){
					IndependantLog.info(debugmsg+" attempting to create missing directory structure in the provided path.");
					afile.getParentFile().mkdirs();
				}
			}catch(Exception np){}
		}
		if(encoding==null || "".equals(encoding.trim())){
			//IndependantLog.debug(debugmsg+" Use System Encoding "+defaultFileEncoding);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(afile)), DEFAULT_BUFFER_SIZE);
		}else{
			try{
				//IndependantLog.debug(debugmsg+" Use Encoding "+encoding);
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(afile), Charset.forName(encoding)), DEFAULT_BUFFER_SIZE);
			}catch(Exception e){
				IndependantLog.warn(debugmsg+" Exception="+e.getMessage());
				IndependantLog.debug(debugmsg+"Can't create writer with encoding '"+encoding+"'; The default encoding '"+defaultFileEncoding+"' will be used.");
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(afile)), DEFAULT_BUFFER_SIZE);
			}
		}

	    return bw;
	}

	/**
	 * Provides a BufferedWriter for a FileOutputStream opened with a UTF-8 OutputStreamWriter.
	 * @param filename case-insensitive absolute filename path.
	 * @return BufferedWriter open with a UTF-8 OutputStreamWriter to a FileOutputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist and cannot be created, cannot be opened for write operations, etc.
	 * @see CaseInsensitiveFile
	 * @see OutputStreamWriter
	 * @see FileOutputStream
	 */
	public static BufferedWriter getUTF8BufferedFileWriter(String filename)throws FileNotFoundException{
	    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(filename).toFile()), Charset.forName("UTF-8")), DEFAULT_BUFFER_SIZE);
	}
	/**
	 * Provides a BufferedWriter for a FileOutputStream opened with a UTF-8 OutputStreamWriter.
	 * @param filename case-insensitive absolute filename path.
	 * @param append   if true, append contents to the end of file
	 * @return BufferedWriter open with a UTF-8 OutputStreamWriter to a FileOutputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist and cannot be created, cannot be opened for write operations, etc.
	 * @see CaseInsensitiveFile
	 * @see OutputStreamWriter
	 * @see FileOutputStream
	 */
	public static BufferedWriter getUTF8BufferedFileWriter(String filename, boolean append)throws FileNotFoundException{
	    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(filename).toFile(),append), Charset.forName("UTF-8")), DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Opens a FileInputStream and checks for UTF-8 0xEF 0xBB 0xBF marker bytes and closes the file.
	 * {@link http://en.wikipedia.org/wiki/Byte_Order_Mark#Representations_of_byte_order_marks_by_encoding}
	 * @param filename case-insensitive absolute filename path.
	 * @return true if the file starts with the UTF-8 marker bytes. false otherwise--including if there
	 * are not 3 bytes of data available to be read from the file.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist, cannot be opened for read operations, etc.
	 * @throws IOException if there is an IO problem when attempting to open or read from the file.
	 * @see org.safs.tools.CaseInsensitiveFile
	 * @see java.io.FileInputStream
	 * @see FileOutputStream
	 */
	public static boolean isFileUTF8(String filename)throws FileNotFoundException, IOException{
		FileInputStream reader = new FileInputStream(new CaseInsensitiveFile(filename).toFile());
		if (reader.available()< 3) return false;
		int _byte1 = reader.read();
		int _byte2 = reader.read();
		int _byte3 = reader.read();
		reader.close();
		boolean rc = true;
		// check for UTF-8 marker bytes
		if(_byte1!=0xEF) rc=false;
		if(_byte2!=0xBB) rc=false;
		if(_byte3!=0xBF) rc=false;
		
		return rc;
	}

	/**
	 * The Method object of method detectFileEncoding() of class {@link org.safs.text.FileUtilitiesByThirdParty}.
	 */
	private static Method detectFileEncodingMethod = null;
	
	private static Method detectStringEncodingMethod = null;
	/**
	 * Detects file's encoding.<br>
	 * This depends on class {@link org.safs.text.FileUtilitiesByThirdParty}, which requires jar
	 * <a href="http://code.google.com/p/juniversalchardet/">juniversalchardet</a><br>
	 * @param filename case-insensitive absolute filename path.
	 * @return String, the file encoding; null, if some Exceptions occur.
	 */
	public static String detectFileEncoding(String filename){
		String encoding = null;
		try {
			if(detectFileEncodingMethod==null) detectFileEncodingMethod = FileUtilitiesbYThirdPartyClass.getMethod("detectFileEncoding", String.class);
			encoding = (String) detectFileEncodingMethod.invoke(null, filename);
		} catch (Exception e) {
			IndependantLog.warn("Cannot get file encoding for "+filename+", met "+e.getClass().getSimpleName()+":"+e.getMessage());
			Throwable cause = e.getCause();
			IndependantLog.warn("caused by "+cause.getClass().getSimpleName()+":"+cause.getMessage());
		}
		
		return encoding;
	}

	/**
	 * Detects String encoding.<br>
	 * This depends on class {@link org.safs.text.FileUtilitiesByThirdParty}, which requires jar
	 * <a href="http://code.google.com/p/juniversalchardet/">juniversalchardet</a><br>
	 * @param str - input as string.
	 * @return String, the file encoding; null, if some Exceptions occur.
	 */
	public static String detectStringEncoding(String str){
		String encoding = null;
		try {
			if(detectStringEncodingMethod==null) detectStringEncodingMethod = FileUtilitiesbYThirdPartyClass.getMethod("detectStringEncoding", String.class);
			encoding = (String) detectStringEncodingMethod.invoke(null, str);
		} catch (Exception e) {
			IndependantLog.warn("Cannot get file encoding for "+str+", met "+e.getClass().getSimpleName()+":"+e.getMessage());
			Throwable cause = e.getCause();
			IndependantLog.warn("caused by "+cause.getClass().getSimpleName()+":"+cause.getMessage());
		}
		
		return encoding;
	}
	
	
	/**
	 * Attempt to replace all filename path separators ( "\" or "/" ) with the correct ones for
	 * the current environment.
	 * @param filename that may or may not have separators embedded.
	 * @return new String with correct File.separators -- if any. Returns filename unmodified
	 * if it is null or contains no separators.
	 */
	public static String normalizeFileSeparators(String filename){
		try{
			if (FILE_SEP_UNIX.equals(File.separator))
				filename = StringUtilities.findAndReplace(filename, FILE_SEP_WIN, File.separator);
			else
				filename = StringUtilities.findAndReplace(filename, FILE_SEP_UNIX, File.separator);
		}catch(NullPointerException np){}
		return filename;
	}

	/**
	 * Provides a BufferedReader for a FileInputStream with encoding.
	 * @param filename case-insensitive absolute filename path.
	 * @param encoding The encoding to be used to create a InputStreamReader;
	 *                 If null, a default encoding indicated by "file.encoding" will be used.
	 *
	 * @return BufferedReader open with a InputStreamReader to a FileInputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist, cannot be opened for read operations, etc.
	 * @see CaseInsensitiveFile
	 * @see InputStreamReader
	 * @see FileInputStream
	 */
	public static BufferedReader getBufferedFileReader(String filename, String encoding)throws FileNotFoundException{
		BufferedReader br = null;
		String debugmsg = FileUtilities.class.getName()+".getBufferedFileReader(): ";
		String defaultFileEncoding = System.getProperty("file.encoding");
		//IndependantLog.debug(debugmsg+" System Encoding "+defaultFileEncoding);
		//Try to deduce the encoding from the file itself
		File file = new CaseInsensitiveFile(filename).toFile();
		if(encoding==null || "".equals(encoding.trim())){ encoding = detectFileEncoding(file.getAbsolutePath());}
		if(encoding==null || "".equals(encoding.trim()) || encoding.equals(defaultFileEncoding)){
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)), DEFAULT_BUFFER_SIZE);
		}else{
			try{
				//IndependantLog.debug(debugmsg+" Use Encoding "+encoding);
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(encoding)), DEFAULT_BUFFER_SIZE);
			}catch(Exception e){
				IndependantLog.warn(debugmsg+" Exception="+e.getMessage());
				IndependantLog.debug(debugmsg+"Can't create reader with encoding '"+encoding+"'; The default encoding '"+defaultFileEncoding+"' will be used.");
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)), DEFAULT_BUFFER_SIZE);
			}
		}

	    return br;
	}

	/**
	 * Provides a BufferedReader for a FileInputStream opened with a UTF-8 InputStreamReader.
	 * @param filename case-insensitive absolute filename path.
	 * @return BufferedReader open with a UTF-8 InputStreamReader to a FileInputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist, cannot be opened for read operations, etc.
	 * @see CaseInsensitiveFile
	 * @see InputStreamReader
	 * @see FileInputStream
	 */
	public static BufferedReader getUTF8BufferedFileReader(String filename)throws FileNotFoundException{
	    return new BufferedReader(new InputStreamReader(new FileInputStream(new CaseInsensitiveFile(filename).toFile()), Charset.forName("UTF-8")), DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Provides a BufferedWriter for a FileOutputStream opened with an OutputStreamWriter using the
	 * default System codepage\charset.
	 * @param filename case-insensitive absolute filename path.
	 * @return BufferedWriter open with a default OutputStreamWriter to a FileOutputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist and cannot be created, cannot be opened for write operations, etc.
	 * @see CaseInsensitiveFile
	 * @see OutputStreamWriter
	 * @see FileOutputStream
	 */
	public static BufferedWriter getSystemBufferedFileWriter(String filename)throws FileNotFoundException{
	    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(filename).toFile())), DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Provides a BufferedWriter for a FileOutputStream opened with an OutputStreamWriter using the
	 * default System codepage\charset.
	 * @param filename case-insensitive absolute filename path.
	 * @param append   if true, append contents to the end of file
	 * @return BufferedWriter open with a default OutputStreamWriter to a FileOutputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist and cannot be created, cannot be opened for write operations, etc.
	 * @see CaseInsensitiveFile
	 * @see OutputStreamWriter
	 * @see FileOutputStream
	 */
	public static BufferedWriter getSystemBufferedFileWriter(String filename, boolean append)throws FileNotFoundException{
	    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(filename).toFile(),append)), DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Provides a BufferedReader for a FileInputStream opened with an InputStreamReader using the
     * default System codepage\charset.
     * @param filename case-insensitive absolute filename path.
	 * @return BufferedReader open with a default InputStreamReader to a FileInputStream and
	 *         the default buffer size.
	 * @throws FileNotFoundException if filename exists but points to a directory instead of a regular file,
	 *         does not exist, cannot be opened for read operations, etc.
	 * @see CaseInsensitiveFile
	 * @see InputStreamReader
	 * @see FileInputStream
	 */
	public static BufferedReader getSystemBufferedFileReader(String filename)throws FileNotFoundException{
	    return new BufferedReader(new InputStreamReader(new FileInputStream(new CaseInsensitiveFile(filename).toFile())), DEFAULT_BUFFER_SIZE);
	}


	/**
	 * Copy one file (BufferedReader) to another file (BufferedWriter).  Typically this is done
	 * to convert from one character encoding to another.
	 * The reader and writer will be closed on exit even if an IOException is thrown.
	 *
	 * @param reader BufferedReader to copy from.
	 * @param writer BufferedWriter to copy to.
	 * @throws IOException if an error occurs reading or writing.
	 */
	public static void copyFileToFile(BufferedReader reader, BufferedWriter writer)throws IOException {
		int code = 0;
		try{
			while(code != -1){
				code = reader.read();
				if(code != -1) writer.write(code);
			}
		}
		//close reader and writer even when exceptions are being thrown.
		finally{
			if(reader != null) {
				try{reader.close();}catch(IOException x){;}
			}
			if(writer != null){
				try{writer.flush();}catch(IOException x){;}
				try{writer.close();}catch(IOException x){;}
			}
		}
	}

	/**
	 * Copy a UTF-8 format file to a file in the default system codepage.
	 * @param utf8File full absolute path of readable UTF-8 file to copy to default system codepage.
	 * @param systemFile full absolute path to valid output file to be created during copy.
	 * @throws FileNotFoundException if filenames exist but points to a directory instead of regular files,
	 *         input file does not exist or output file cannot be created, or files cannot be opened for
	 *         read or write operations, as appropriate.
	 * @see #copyFileToFile(BufferedReader, BufferedWriter)
	 */
	public static void copyUTF8ToSystemFile(String utf8File, String systemFile)throws FileNotFoundException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try{
			reader = getUTF8BufferedFileReader(utf8File);
			writer = getSystemBufferedFileWriter(systemFile);
			copyFileToFile(reader, writer); //closes reader and writer
		}
		// only occurs if making the reader and writer was successful but IO was unsuccessful
		catch(IOException x){
			IndependantLog.debug("FileProcessor IOException:", x);
		}
	}

	/**
	 * Copy a default system codepage file to a file in UTF-8 format.
	 * @param systemFile full absolute path to default system file to be copied to UTF-8.
	 * @param utf8File full absolute path of file to be created during copy.
	 * @throws FileNotFoundException if filenames exist but point to a directory instead of regular files,
	 *         input file does not exist or output file cannot be created, or files cannot be opened for
	 *         read or write operations, as appropriate.
	 * @see #copyFileToFile(BufferedReader, BufferedWriter)
	 */
	public static void copySystemToUTF8File(String systemFile, String utf8File)throws FileNotFoundException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try{
			reader = getSystemBufferedFileReader(systemFile);
			writer = getUTF8BufferedFileWriter(utf8File);
			copyFileToFile(reader, writer); //closes reader and writer
		}
		// only occurs if making the reader and writer was successful but IO was unsuccessful
		catch(IOException x){
			IndependantLog.debug("FileProcessor IOException:", x);
		}
	}


	/**
	 * Output the toString() values in a Collection as separate lines in a new file.
	 * Each line is ended with the newline String provided.
	 * The output character encoding is determined by the BufferedWriter provided.
	 * <p>
	 * The writer is flushed and closed here, upon completion.
	 * <p>
	 * @param writer BufferedWriter determines the output character encoding.
	 * @param list Collection of objects\lines to write
	 * @param newline String to append to each item in the list.
	 * @throws IOException thrown if an IO error occurs during writing.
	 */
	public static void writeCollectionToFile(BufferedWriter writer, Collection list, String newline)throws IOException{
	    try {
	      for(Iterator i= list.iterator(); i.hasNext(); ) {
	        writer.write(i.next().toString() + newline);
	      }
	    }
	    //if either writer or list is null just exit without incident
	    catch(NullPointerException x){
	    	IndependantLog.warn("FileUtilities.writeCollectionToFile "+x.getClass().getSimpleName()+", "+x.getMessage());
	    }

	    finally {
      	  IndependantLog.warn("FileUtilities.writeCollectionToFile finished with Collection.");
	      if (writer != null)  {
	      	try{ writer.flush();}catch(Exception x){}
	      	try{ writer.close();}catch(Exception x){}
	      }
	    }
	}

	  /** <br><em>Purpose:</em> write Properties contents to BufferedWriter in a SAFS Properties file format:
	   * <pre>
	   * :PROPERTY:propertyName
	   * property value possibly spanning
	   * multiple lines.
	   * :PROPERTY:propertyName
	   * etc...
	   * </pre>
	   * @param                     reader
	   * @return                    Properties
	   **/
	  public static void writePropertiesFile(BufferedWriter writer, Map<String,String> props)throws IOException{
			String test = null;
			final String CRNL = "\r\n";
			final String NL = "\n";
			try{
				Set<String> keys = props.keySet();
				for(String key:keys){
					try{
						test = props.get(key);
						test = test.replace(CRNL, NL);
						writer.write(StringUtils.PROPERTY_START+ key +NL);
						writer.write(test+ NL);
					}catch(ClassCastException cc){}
				}
			}catch(NullPointerException np){
		    	IndependantLog.warn("FileUtilities.writeProperties "+np.getClass().getSimpleName()+", "+np.getMessage());
			}finally{
				try{writer.flush();}catch(Exception x){}
				try{writer.close();}catch(Exception x){}
			}
	  }

	/**
	 * Output a String to a new file.  The String is expected to contain all formatting,
	 * tabs, and newlines, etc.. and will be written unmodified.
	 *
	 * The output character encoding is determined by the BufferedWriter provided.
	 *
	 * @param writer BufferedWriter determines the output character encoding.
	 * @param data String text to write.
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeStringToSystemFile(String, String)
	 * @see #writeStringToUTF8File(String, String)
	 */
	public static void writeStringToFile(BufferedWriter writer, String data)throws IOException{
	    try {
	        writer.write(data);
	    }
	    //if either writer or data is null just exit without incident
	    catch(NullPointerException x){ ; }

	    finally {
	      if (writer != null)  {
	      	writer.flush();
	      	writer.close();
	      }
	    }
	}

	/**
	 * Output the toString() values in a Collection as separate lines in a new file.
	 * Each line is ended with the System line.separator property.
	 * This method outputs the text in the default character encoding of the System.
	 *
	 * @param filename absolute system path to the file to be created\overwritten.
	 * @param data String text to write.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeStringToFile(BufferedWriter, String)
	 */
	public static void writeStringToSystemFile(String filename, String data)throws FileNotFoundException, IOException {
		writeStringToFile(getSystemBufferedFileWriter(filename), data);
	}

	/**
	 * Output the String in a new file.
	 * This method outputs the text in the UTF-8 character encoding.
	 *
	 * @param filename absolute system path to the file to be created\overwritten.
	 * @param data String text to write.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeStringToFile(BufferedWriter, String)
	 */
	public static void writeStringToUTF8File(String filename, String data)throws FileNotFoundException, IOException {
		writeStringToFile(getUTF8BufferedFileWriter(filename), data);
	}

	/**
	 * Output the String in a new file.
	 * This method outputs the text in the character encoding given by parameter.
	 *
	 * @param filename absolute system path to the file to be created\overwritten.
	 * @param encoding the encoding to be used to write a file.
	 * @param data String text to write.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeStringToFile(BufferedWriter, String)
	 */
	public static void writeStringToFile(String filename, String encoding, String data)throws FileNotFoundException, IOException {
		writeStringToFile(getBufferedFileWriter(filename,encoding), data);
	}

	/**
	 * Output the toString() values in a Collection as separate lines in a new file.
	 * Each line is ended with the System line.separator property appropriate for the
	 * current operating system.  The output character encoding is determined by
	 * the BufferedWriter provided.
	 *
	 * @param writer BufferedWriter determines the output character encoding.
	 * @param list Collection of objects\lines to write
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeCollectionToFile(BufferedWriter, Collection, String)
	 * @see #writeCollectionToFile(BufferedWriter, Collection, String)
	 */
	public static void writeCollectionToFile(BufferedWriter writer, Collection list)throws IOException{
		String newline = System.getProperty("line.separator");
		writeCollectionToFile(writer, list, newline);
	}

	/**
	 * Output the toString() values in a Collection as separate lines in a new file.
	 * Each line is ended with the System line.separator property.
	 * This method outputs the text in the default character encoding of the System.
	 *
	 * @param filename absolute system path to the file to be created\overwritten.
	 * @param list
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeCollectionToFile(BufferedWriter, Collection)
	 */
	public static void writeCollectionToSystemFile(String filename, Collection list)throws FileNotFoundException, IOException {
		writeCollectionToFile(getSystemBufferedFileWriter(filename), list);
	}

	/**
	 * Output the toString() values in a Collection as separate lines in a new file.
	 * Each line is ended with the System line.separator property.
	 * This method outputs the text in the UTF-8 character encoding.
	 *
	 * @param filename absolute system path to the file to be created\overwritten.
	 * @param list
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs during writing.
	 * @see #writeCollectionToFile(BufferedWriter, Collection)
	 */
	public static void writeCollectionToUTF8File(String filename, Collection list)throws FileNotFoundException, IOException {
		writeCollectionToFile(getUTF8BufferedFileWriter(filename), list);
	}

	/**
	 * Read file contents into a String.  The String is expected to contain all formatting,
	 * tabs, and newlines, etc.. and will be returned unmodifed.
	 *
	 * The character decoding is determined by the BufferedReader provided.
	 *
	 * @param reader BufferedReader determines the character decoding.
	 * @return String contents of file
	 * @throws IOException thrown if an IO error occurs.
	 * @see #readStringFromSystemFile(String)
	 * @see #readStringFromUTF8File(String)
	 */
	public static String readStringFromFile(BufferedReader reader)throws IOException{
		StringBuffer buf = new StringBuffer();
	    try {
	    	int count = 0;
	    	char[] chars = new char[1024];
	        while(count >=0){
	        	count = reader.read(chars);
	        	if(count > 0){
	        		buf.append(chars, 0, count);
	        	}
	        }
	    }
	    //if reader is null just exit without incident
	    catch(NullPointerException x){ ; }

	    finally {
	      if (reader != null)  {
	      	reader.close();
	      }
	    }
	    return buf.toString();
	}

	/**
	 * Read file lines into a String[].  The String[] is expected to contain all formatting,
	 * but \n and \r should be removed via the readLine calls.
	 *
	 * The character encoding is detected from the file, if possible.
	 * If not, the System encoding is assumed.
	 *
	 * @param filepath
	 * @return String[] contents of file
	 * @throws IOException thrown if an IO error occurs.
	 * @see #readStringFromFile(BuffereReader)
	 * @see #readStringFromSystemFile(String)
	 * @see #readStringFromUTF8File(String)
	 */
	public static String[] readLinesFromFile(String filepath)throws IOException{
		ArrayList buf = new ArrayList();
    	BufferedReader reader = null;
	    try {
	    	String line = null;
	    	String encoding = detectFileEncoding(filepath);
	    	if(encoding == null){
	    		boolean utf8 = isFileUTF8(filepath);
	    		reader = utf8 ?
					     getUTF8BufferedFileReader(filepath):
					     getSystemBufferedFileReader(filepath);
	    	}else{
	    		reader = getBufferedFileReader(filepath, encoding);
	    	}
	        do{
	        	line = reader.readLine();
	        	buf.add(line.toString());
	        }while(line != null);
	    }
	    //if reader is null just exit without incident
	    catch(NullPointerException x){ ; }
	    finally {
	      if (reader != null)  {
	      	reader.close();
	      }
	    }
	    return (String[]) buf.toArray(new String[0]);
	}

	private static boolean endsWith(String eval, String[] endings, boolean isCaseSensitive){
		if(endings == null|| endings.length == 0) return false;
		String lcval = isCaseSensitive ? eval:eval.toLowerCase();
		String lcend = null;
		for(String ends:endings){
			lcend = isCaseSensitive ? ends:ends.toLowerCase();
			if(lcval.endsWith(lcend)) return true;
		}
		return false;
	}

	/**
	 * Recursively replaceDirectoryFileSubstrings in all matching files in the directory and all
	 * subdirectories.
	 * @param directory
	 * @param fileEndings
	 * @param findString
	 * @param replaceString
	 * @param isCaseSensitive
	 * @throws IOException
	 * @see #replaceDirectoryFilesSubstrings(String, String[], String, String, boolean)
	 */
	public static void replaceAllSubdirectoryFilesSubstrings(String directory, String[] fileEndings, String findString, String replaceString, boolean isCaseSensitive) throws IOException{
	    File dir = new CaseInsensitiveFile(directory).toFile();
	    if(!dir.isDirectory()) throw new IOException(directory +" is not a valid Directory!");
	    replaceDirectoryFilesSubstrings(dir.getAbsolutePath(), fileEndings, findString, replaceString, isCaseSensitive);
	    File[] files = dir.listFiles();
	    String filename;
	    for(File afile: files){
	    	filename = afile.getAbsolutePath();
	    	if(afile.isDirectory())
	    		replaceAllSubdirectoryFilesSubstrings(afile.getAbsolutePath(), fileEndings, findString, replaceString, isCaseSensitive);
	    }
	}

	/**
	 * Examine the contents of each file in the specified directory whose filename matches any one of the provided fileEndings.
	 * Every instance of the findString in these files will be replaced with the replaceString.
	 * The file will be decoded in whatever encoding it contains via readLinesFromFile.
	 * The replacement file will be written to whatever encoding was detected, or to UTF8 if
	 * the encoding was not properly detected.
	 * When the file is rewritten, the platform-specific EOL is used.
	 * @param directory
	 * @param fileEndings
	 * @param findString
	 * @param replaceString
	 * @param isCaseSensitive
	 * @see #readLinesFromFile(String)
	 * @see #detectFileEncoding(String)
	 * @see #writeStringToFile(String, String, String)
	 * @see #writeStringToUTF8File(String, String)
	 */
	public static void replaceDirectoryFilesSubstrings(String directory, String[] fileEndings, String findString, String replaceString, boolean isCaseSensitive) throws IOException{
	    String line = null;
	    int index = 0;
	    StringBuffer buffer = null;
	    String strfile = null;
	    File dir = new CaseInsensitiveFile(directory).toFile();
	    if(!dir.isDirectory()) throw new IOException(directory +" is not a valid Directory!");
	    File[] files = dir.listFiles();
	    String encoding = null;
	    boolean changed = false;
	    for(File afile: files){
	    	changed = false;
	    	if(afile.isDirectory()) continue;
	    	strfile = afile.getAbsolutePath();
	    	// what are unix or mac extensions we care about?
	    	encoding = detectFileEncoding(strfile);
	    	if(endsWith(strfile, fileEndings, isCaseSensitive)){
	    		String[] lines = readLinesFromFile(strfile);
	    		boolean linechanged = false;
	    		for(int i=0;i < lines.length;i++){
	    			line = lines[i];
	    			linechanged = false;
	    			index = isCaseSensitive ? line.indexOf(findString):line.toLowerCase().indexOf(findString.toLowerCase());
	    			if(index == 0){
	    				changed = true;
	    				linechanged = true;
	    				lines[i] = replaceString;
	    			}else if (index > 0){
	    				changed = true;
	    				linechanged=true;
	    				lines[i] = line.substring(0, index) + replaceString;
	    			}
    				if(linechanged && (line.length() > index + findString.length())){
    					lines[i] += line.substring(index + findString.length());
    				}
	    		}
	    		if(changed){
	    			IndependantLog.debug("Modifying File substrings in "+ afile.getAbsolutePath());
		    		buffer = new StringBuffer();
		    		for(String aline:lines){
		    			buffer.append(aline + EOL);
		    		}
		    		if(encoding != null){
		    			FileUtilities.writeStringToFile(afile.getAbsolutePath(), encoding, buffer.toString());
		    		}else{
		    			FileUtilities.writeStringToUTF8File(afile.getAbsolutePath(),buffer.toString());
		    		}
	    		}
	    	}
	    }
	}

	/**
	 * Read file contents into a String.  The String is expected to contain all formatting,
	 * tabs, and newlines, etc.. and will be returned unmodifed.
	 *
	 * The default System character decoding is assumed.
	 *
	 * @param filename
	 * @return String contents of file.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs.
	 * @see #getSystemBufferedFileReader(String)
	 */
	public static String readStringFromSystemFile(String filename)throws FileNotFoundException, IOException {
		return readStringFromFile(getSystemBufferedFileReader(filename));
	}

	/**
	 * Read file contents into a String.  The String is expected to contain all formatting,
	 * tabs, and newlines, etc.. and will be returned unmodified.
	 *
	 * UTF8 character decoding is assumed.
	 *
	 * @param filename
	 * @return String contents of file.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs.
	 * @see #getUTF8BufferedFileReader(String)
	 */
	public static String readStringFromUTF8File(String filename)throws FileNotFoundException, IOException {
		return readStringFromFile(getUTF8BufferedFileReader(filename));
	}

	/**
	 * Read file contents into a String.  The String is expected to contain all formatting,
	 * tabs, and newlines, etc.. and will be returned unmodified.
	 *
	 * Character decoding is given by parameter.
	 *
	 * @param filename
	 * @param encoding The encoding to be used to read a file
	 * @return String contents of file.
	 * @throws FileNotFoundException
	 * @throws IOException thrown if an IO error occurs.
	 * @see #getUTF8BufferedFileReader(String)
	 */
	public static String readStringFromEncodingFile(String filename, String encoding)throws FileNotFoundException, IOException {
		return readStringFromFile(getBufferedFileReader(filename,encoding));
	}

	/**
	 * Open a new Properties file located at the provided URL and add its entries
	 * to those in the target Properties file.  If the target Properties file is
	 * null then simply return the new Properties file.
	 * If the URL is null or an IOException is encountered while opening it then
	 * we will simply return the target Properties file unmodified (which might be
	 * null.)
	 * @param target Properties file to be appended, or null.
	 * @param url to Properties file to append.
	 * @return Properties file or null.
	 */
	public static Properties appendProperties(Properties target, URL url){
		Properties p2 = new Properties();
		try{
			InputStream in = url.openStream();  // poss. IOException
			p2.load(in);
			in.close();
			in = null;
			if(p2.size()>0) {
				target.putAll(p2); // poss. NullPointerException
			}
		}catch(IOException io){
			IndependantLog.info("GCD IOException:"+ io.getMessage());
			return target;
		}catch(NullPointerException np){;}
		return (target==null)? p2:target;
	}

	public static boolean isWindowPlatform(){
		if(getPlatformName().indexOf("window")>-1)
			return true;
		return false;
	}
	public static String getPlatformName(){
		return System.getProperty("os.name").toLowerCase();
	}

	/**
	 * Return the lower-case file extension portion of the provided logical file.
	 * Ex: ...\Filename.ext will return "ext".
	 *
	 * @param file
	 * @return the file extension, or null if one cannot be deduced.
	 */
	public static String getExtension(File file) {
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

	/**
	 * Calls unzipJAR with noMETATDir as false.
	 * @param zipFileName, String, the 'zip file' to be uncompressed.
	 * @param root, File, the directory to store the uncompressed files.
	 * @param verbose, boolean, if the uncompressed message will be printed to console.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @see #unzipJAR(String, File, boolean, boolean)
	 */
	public static void unzipFile(String zipFileName, File root, boolean verbose) throws IOException, FileNotFoundException{
		unzipJAR(zipFileName, root, verbose, false);
	}

	/**
	 * @param zipFileName, String, the 'zip' or 'jar' file to be uncompressed.
	 * @param root, File, the directory to store the uncompressed files.
	 * @param verbose, boolean, if the uncompressed message will be printed to console.
	 * @param noMETADir, true to bypass the META-INF directory and MANIFEST.MF file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void unzipJAR(String zipFileName, File root, boolean verbose, boolean noMETADir) throws IOException, FileNotFoundException{
		ZipFile zip = new ZipFile(zipFileName);
		Enumeration<?> files = zip.entries();
		final String METAINF  = "META-INF";
		final String MANIFEST = "MANIFEST.MF";

		// cycle through to make any required subdirectories
		while(files.hasMoreElements()){

			ZipEntry zipfile = (ZipEntry) files.nextElement();
			File file;
			File directory;

			String filename  = zipfile.getName();
			long   time      = zipfile.getTime();

			if(noMETADir){
				if(filename.toUpperCase().contains(METAINF+"/")) {
					//System.out.println("Skipping JAR Entry "+ filename);
					if(verbose) IndependantLog.info("Skipping file "+filename+" ...");
					continue;
				}
			}
			// see if the current file in the ZipFile is needed to be copied to the target directory.
			// If a same file exists in the target directory already,it will be decided by its last modified time.
			// No need to replace the existing file if it is newer than its update!
			String replacedfilename = root + File.separator + filename;
			File replacedfile = new CaseInsensitiveFile(root + File.separator + filename).toFile();
			try {
				if (replacedfile.exists()) {
					long modifiedtime = replacedfile.lastModified();
					if (modifiedtime >= time){ // no need to replace the existing file
						if(!replacedfile.isDirectory()) IndependantLog.info(replacedfilename + " does not have a new version, will not overwrite it!");
						continue;
					}
				}
			}catch (Exception x){;}

			if(verbose) IndependantLog.info("Extracting file "+filename+" ...");
			if( zipfile.isDirectory()){
				directory = new CaseInsensitiveFile(root, filename).toFile();
				directory.mkdirs();
				continue;
			}
			else{
				file = new CaseInsensitiveFile(root, filename).toFile();
				try{
					directory = file.getParentFile();
					if (! directory.exists()){
						directory.mkdirs();
				    }
				}
				catch(NullPointerException np){;}
				catch(Exception x){
				    throw new FileNotFoundException(
				    "Specified install path could not be created.");
				}
			}

			copyFile(zip.getInputStream(zipfile), new FileOutputStream(file));

			file.setLastModified(time);
		}

		zip.close();
	}

	/**
	 * @param in, InputStream, The InputStream of the source file to be copied.
	 * @param out, OutputStream, The OutputStream of the destination file.
	 * @throws IOException
	 */
	public static void copyFile(InputStream in, OutputStream out) throws IOException{
		BufferedOutputStream bout = new BufferedOutputStream(out);
		BufferedInputStream  bin  = new BufferedInputStream (in);

		int read = -1;
		byte[] bytes = new byte[1024];
		try {
			while((read = bin.read(bytes)) >0){
				bout.write(bytes, 0, read);
				bout.flush();
			}
		} catch (IOException e) {
			throw e;
		}finally{
			try { bin.close(); } catch (IOException e) { }
			try { bout.close(); } catch (IOException e) { }
		}
	}

	/**
	 * Remove directory recursively by JAVA API
	 * @param directory, String, the directory to be recursively deleted.
	 * @param verbose, boolean, if the error message will be printed to console.
	 * @return
	 */
	public static int deleteDirectoryRecursively(String directory, boolean verbose){
		//If directory does not exist, just return 0.
		File aFile = new CaseInsensitiveFile(directory).toFile();
		if( !aFile.exists()){
			IndependantLog.warn("Warning: File '"+aFile.getAbsolutePath()+"' doesn't exist! Can't delete.");
			return 0;
		}

		if(aFile.isFile()){
			//Just delete it.
			if(verbose) IndependantLog.debug("Deleting file "+ aFile.getName());
			if(!aFile.delete()) IndependantLog.warn("Warning: Fail to delete '"+aFile.getAbsolutePath()+"'");
		}else if(aFile.isDirectory()){
			//If the directory is not empty, delete all its children firstly
			File[] children = aFile.listFiles();
			if(children!=null){
				for(int i=0;i<children.length;i++){
					deleteDirectoryRecursively(children[i].getAbsolutePath(), verbose);
				}
			}
			//Then delete the empty directory
			if(verbose) IndependantLog.debug("Deleting directory "+ aFile.getName());
			if(!aFile.delete()) IndependantLog.warn("Warning: Fail to delete '"+aFile.getAbsolutePath()+"'");
		}

		return 0;
	}

	/**
	 * Careful what you wish for...
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public static int copyDirectoryRecursively(File srcDir, File destDir) throws IOException, IllegalArgumentException {
		if (srcDir.isDirectory()) {
		    //if directory not exists, create it
		    if (!destDir.exists()) {
		      destDir.mkdirs();
		    }
		    //list all the directory contents
		    File files[] = srcDir.listFiles();
		    for (File srcFile : files) {
		        //construct the src and dest file structure
		        File destFile = new File(destDir, srcFile.getName());
		        //recursive copy
		        if(srcFile.isDirectory()) {
		    	    copyDirectoryRecursively(srcFile,destFile);
		    	    continue;
		        }else{
				    //if file, then copy it
				    //Use bytes stream to support all file types
				    InputStream in = new FileInputStream(srcFile);
				    OutputStream out = new FileOutputStream(destFile);
				    byte[] buffer = new byte[1024];
				    int length;
				    //copy the file content in bytes
				    while ((length = in.read(buffer)) > 0){
				      out.write(buffer, 0, length);
				    }
				    out.flush();
				    in.close();
				    out.close();
				    System.out.println("File copied from " + srcFile.getPath() + " to " + destFile.getPath());
		        }
		    }
		}else{
		    System.out.println("CopyDirectoryRecursively ONLY acces Directory arguments.");
		    throw new IllegalArgumentException("CopyDirectoryRecursively ONLY acces Directory arguments.");
		}
		return 0;
	}

	/**
	 * Attempt to copy a source File to a destination File location.
	 * The destination File location will be created if it does not already exist.
	 * @param source -- cannot be null and must be a file, not a directory.
	 * @param dest -- cannot be null. Will be created if it does not already exist.
	 * @throws IOException if an error occurs during the attempt.
	 */
	public static void copyFileToFile(File source, File dest) throws IOException{
		if(source == null || !source.isFile()) throw new IOException("Source file must exist and cannot be null.");
		if(dest == null) throw new IOException("Destination file cannot be null.");
		if(!dest.exists()) dest.createNewFile();
		FileChannel src = null;
		FileChannel dst = null;
		try {
	        src = new FileInputStream(source).getChannel();
	        dst = new FileOutputStream(dest).getChannel();
	        dst.transferFrom(src, 0, src.size());
	    }
	    finally {
	        if(src != null) {
	            try{ src.close();}catch(Exception x){}
	        }
	        if(dst != null) {
	            try{dst.close();}catch(Exception x){}
	        }
	    }
	}

	public static enum FilterMode{
		TOLERANCE("TOLERANCE");

		public final String name;
		FilterMode(String name){
			this.name = name;
		}
	}
	
	public static enum PatternFilterMode{
		WILDCARD("WILDCARD"),
		REGEXP("REGEXP");

		public final String name;
		PatternFilterMode(String name){
			this.name = name;
		}
	}
	public static enum ImageFilterMode{
		COORD("COORD");

		public final String name;
		ImageFilterMode(String name){
			this.name = name;
		}
	}

	public static enum Mode{
		INPUT("Input"),
		OUTPUT("Output"),
		APPEND("Append");

		public final String name;
		Mode(String name){
			this.name = name;
		}
	}

	public static enum Access{
		R("Read"),
		W("Write");
//		RW("Read Write");//Not supported yet

		public final String name;
		Access(String name){
			this.name = name;
		}
	}

	public static enum DateType{
		CREATED("Created"),
		LASTMODIFIED("LastModified"),
		LASTACCESSED("LastAccessed");

		public final String name;
		DateType(String name){
			this.name = name;
		}
	}

	public static enum Placement{
		IMMIDIATE(";"),
		NEWLINE(""),
		TABULATION(",");

		public final String name;
		Placement(String name){
			this.name = name;
		}
	}

	/** "NoVerify"*/
	public static final String PARAM_NO_VERIYF 				= "NoVerify";
	/** "CaseSensitive"*/
	public static final String PARAM_CASE_SENSITIVE 		= "CaseSensitive";
	/** "CaseInsensitive"*/
	public static final String PARAM_CASE_INSENSITIVE 		= "CaseInsensitive";
	/** -1, means all characters will be written.*/
	public static final int PARAM_ALL_CHARACTERS 			= -1;

	/**
	 * To model the file attribute.
      * <br> APR 06, 2016	(Carl Nagle)    Support FileAttribute.ALLFILES Type to specify all non-VOLUMELABEL files at a location.
	 */
	public static class FileAttribute{
		/**
		 * An enumeration to specify the file's attributes.<br>
		 * They can be used one by one, or an bit-combination of them.<br>
		 */
		public static enum Type{
			NORMALFILE((byte)0),//0 is not good for bit-operation, for history reason, we keep it.
			READONLYFILE((byte)1),
			HIDDENFILE((byte)2),
			SYSTEMFILE((byte)4),
			VOLUMELABEL((byte)8),
			DIRECTORY((byte)16),
			ARCHIVEFILE((byte)32),
			ALLFILES((byte)55); //everything but VOLUMELABEL. If VOLUMELABEL present, no others count.

			private final byte value;
			Type(byte value){this.value=value;}
			public byte getValue(){ return value;}
		}

		/**
		 * <pre>
		 * Represents the file's attributes. It can be one of Type or any combination of them.
		 * {@link Type#NORMALFILE}
		 * {@link Type#READONLYFILE}
		 * {@link Type#HIDDENFILE}
		 * {@link Type#SYSTEMFILE}
		 * {@link Type#VOLUMELABEL}
		 * {@link Type#DIRECTORY}
		 * {@link Type#ARCHIVEFILE}
		 * </pre>
		 */
		private int attributes = Type.NORMALFILE.value;
		/**Used to filter no-attribute bits*/
		private static int allAttributes = Type.READONLYFILE.value
				                    |Type.HIDDENFILE.value
				                    |Type.SYSTEMFILE.value
				                    |Type.VOLUMELABEL.value
				                    |Type.DIRECTORY.value
				                    |Type.ARCHIVEFILE.value;

		public static FileAttribute instance(){
			return new FileAttribute();
		}
		public static FileAttribute instance(Type type){
			return new FileAttribute(type);
		}
		public static FileAttribute instance(int attributes){
			return new FileAttribute(attributes);
		}

		public FileAttribute(){}

		public FileAttribute(Type type){
			this.attributes = type.value;
		}
		public FileAttribute(int attributes){
			this.attributes = filterNonAttributeBits(attributes);
		}

		public FileAttribute add(Type type){
			this.attributes |= type.value;
			return this;
		}

		public FileAttribute add(int attributes){
			this.attributes |= filterNonAttributeBits(attributes);
			return this;
		}

		public int getValue(){
			return attributes;
		}

		public String getStringValue(){
			return Integer.toString(attributes);
		}

		public boolean containsReadOnly(){return contains(attributes, Type.READONLYFILE.value);}
		public boolean containsHidden(){return contains(attributes, Type.HIDDENFILE.value);}
		public boolean containsSystem(){return contains(attributes, Type.SYSTEMFILE.value);}
		public boolean containsVolumeLabel(){return contains(attributes, Type.VOLUMELABEL.value);}
		public boolean containsDirectory(){return contains(attributes, Type.DIRECTORY.value);}
		public boolean containsArchive(){return contains(attributes, Type.ARCHIVEFILE.value);}
		public boolean isNormalFile(){return attributes==Type.NORMALFILE.value; }

		/**
		 * A string represents this attribute.<br>
		 * It can contain "R", "H", "S", "A", "D", "V", "N".<br>
		 * For example: "R S H" means ReadOnly, System, Hidden.
		 */
		public String toString(){
			StringBuffer sb = new StringBuffer();

			if(containsReadOnly()) sb.append(ATTRIBUTE_READONLY+" ");
			if(containsHidden()) sb.append(ATTRIBUTE_HIDDEN+" ");
			if(containsSystem()) sb.append(ATTRIBUTE_SYSTEM+" ");
			if(containsArchive()) sb.append(ATTRIBUTE_ARCHIVE+" ");
			if(containsDirectory()) sb.append("D ");
			if(containsVolumeLabel()) sb.append("V ");
			if(isNormalFile()) sb.append("N ");

			return sb.toString();
		}

		/**Remove the bits which don't count.*/
		private static int filterNonAttributeBits(int attributes){
			return (attributes&allAttributes);
		}

		/**
		 * @param actualAttributes int
		 * @param expectedAttributes int, one of {@link FileAttribute.Type} or their combination.
		 * @return boolean true if fileAttributeCode bit-contains filetype
		 */
		public static boolean contains(int actualAttributes, int expectedAttributes){
//			IndependantLog.info(StringUtils.debugmsg(false)+" actualAttributes: "+actualAttributes+"; expectedAttributes: "+expectedAttributes);
			return (actualAttributes&expectedAttributes)==expectedAttributes;
		}

		public static boolean containsReadOnly(int attributes){return contains(attributes, Type.READONLYFILE.value);}
		public static boolean containsHidden(int attributes){return contains(attributes, Type.HIDDENFILE.value);}
		public static boolean containsSystem(int attributes){return contains(attributes, Type.SYSTEMFILE.value);}
		public static boolean containsVolumeLabel(int attributes){return contains(attributes, Type.VOLUMELABEL.value);}
		public static boolean containsDirectory(int attributes){return contains(attributes, Type.DIRECTORY.value);}
		public static boolean containsArchive(int attributes){return contains(attributes, Type.ARCHIVEFILE.value);}

		public static boolean isNormalFile(int attributes){return attributes==Type.NORMALFILE.value; }

		public boolean equals(Object attr){
			if(attr==null) return false;
			if(attr==this) return true;
			if(!(attr instanceof FileAttribute)) return false;

			FileAttribute fa = (FileAttribute) attr;
			if(fa.attributes==this.attributes) return true;
			if(filterNonAttributeBits(fa.attributes)==filterNonAttributeBits(this.attributes)) return true;

			return false;
		}
	}

	/**
	 * A list of suffix for archive file in link http://en.wikipedia.org/wiki/List_of_archive_formats
	 */
	public static final String[] ARCHIVEFILESUFFIX		= {".jar",".tar",".zip",".rar",".a",".ar",".gz",".tgz"};

	/**
	 * Return true, if the file has a suffix as one of {@link #ARCHIVEFILESUFFIX}.<br>
	 */
	public static boolean isArchive(File file) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg+" Testing file attribes 'Archive'.");
		try{
			if(!file.isDirectory()) return isArchive(file.getName());
			return false;
		}catch(Exception e){
			throw new SAFSException("Fail to detect archive attribute. due to "+StringUtils.debugmsg(e));
		}
	}
	/**
	 * Return true, if the file has a suffix as one of {@link #ARCHIVEFILESUFFIX}.<br>
	 */
	public static boolean isArchive(String filename) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		try{
			IndependantLog.info(debugmsg+" filename is "+filename);
			if(filename==null || filename.trim().isEmpty()) return false;
			filename = filename.toLowerCase();
			for(int i=0;i<ARCHIVEFILESUFFIX.length;i++){
				if(filename.endsWith(ARCHIVEFILESUFFIX[i])){
					return true;
				}
			}
			return false;
		}catch(Exception e){
			throw new SAFSException("Fail to detect archive attribute. due to "+StringUtils.debugmsg(e));
		}
	}
	/**
	 * If file can be read and can NOT be wrote, then return true.
	 */
	public static boolean isReadOnly(File file) throws SAFSException{
		try{
			return file.canRead()&&!file.canWrite();
		}catch(Exception e){
			throw new SAFSException(StringUtils.debugmsg(e));
		}
	}
	/**"attrib"*/
	public static final String DOS_COMMAND_ATTRIB	= "attrib";
	/**'R'*/
	public static final char ATTRIBUTE_READONLY 	= 'R';
	/**'S'*/
	public static final char ATTRIBUTE_SYSTEM 		= 'S';
	/**'H'*/
	public static final char ATTRIBUTE_HIDDEN 		= 'H';
	/**'A'*/
	public static final char ATTRIBUTE_ARCHIVE		= 'A';
	/**An array containing "R" "S" "H" "A"*/
	public static final String[] ATTRIBUTES_ARRAY	= {
		String.valueOf(ATTRIBUTE_READONLY),
		String.valueOf(ATTRIBUTE_SYSTEM),
		String.valueOf(ATTRIBUTE_HIDDEN),
		String.valueOf(ATTRIBUTE_ARCHIVE)
	};

	/**
	 * @param attribute FileAttribute, the attribute to set
	 * @param file File, the file for which the attributes will be set
	 * @return boolean, true if the setting is successful.
	 * @throws SAFSException
	 */
	public static boolean setFileAttribute(FileAttribute attribute, File file) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);

		//Windows OS: uses DOS attrib; other OS use portable java
		if (Console.isWindowsOS()) {
			IndependantLog.info(debugmsg+"Windows OS, using DOS 'attrib' command");

			try {
				DefaultConsoleTool console = DefaultConsoleTool.instance();
				Process2 p = null;
				//First, clear the file's attribute, DOS "attrib -R -S -H -A filename"
				List<String> arguments = new ArrayList<String>();
				for(String attr: ATTRIBUTES_ARRAY) arguments.add(StringUtils.MINUS+attr);
				arguments.add(file.getCanonicalPath());
				p = console.exec(DOS_COMMAND_ATTRIB, arguments);
				p.waitForSuccess();
				IndependantLog.info(debugmsg+" exec command: "+console.getLastCommand());

				//Second, set the file's attribute, DOS "attrib +R +S +H +A filename"
				arguments.clear();
				if(attribute.containsReadOnly()) arguments.add(StringUtils.PLUS+ATTRIBUTE_READONLY);
				if(attribute.containsSystem()) arguments.add(StringUtils.PLUS+ATTRIBUTE_SYSTEM);
				if(attribute.containsHidden()) arguments.add(StringUtils.PLUS+ATTRIBUTE_HIDDEN);
				if(attribute.containsArchive()) arguments.add(StringUtils.PLUS+ATTRIBUTE_ARCHIVE);
				arguments.add(file.getCanonicalPath());
				p = console.exec(DOS_COMMAND_ATTRIB, arguments);
				p.waitForSuccess();
				IndependantLog.info(debugmsg+" exec command: "+console.getLastCommand());

				return true;
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Met "+StringUtils.debugmsg(e));
				throw new SAFSException(StringUtils.debugmsg(e), SAFSException.CODE_ERROR_SET_FILE_ATTR);
			}
		}else{
			IndependantLog.info(debugmsg+"Using '"+Console.getOsFamilyName()+"' OS, use java to set attributes.");
			// not a windows pc platform
			// only attribute available is File.setReadOnly()
			// using a security manager might be more robust, but you need
			// a policy implemented which can not be assumed or programatically determined
			// within safs
			String errorKey = null;
			String alterMsg = null;
			String fileProtection = "File Protection";
			if(attribute.containsReadOnly()){
				if(file.setReadOnly()) return true;

				errorKey = FAILStrings.COULD_NOT_SET;
				alterMsg = "Could not set '"+fileProtection+"' to '"+ attribute.getStringValue() +"'";
			}else{
				errorKey = FAILStrings.SUPPORT_NOT_FOUND;
				alterMsg = "Support for '"+fileProtection+"':"+ attribute.getStringValue() +" not found.";
			}

			String error = FAILStrings.convert(errorKey, alterMsg,
					GENStrings.text(GENStrings.FILE_PROTECTION, fileProtection), attribute.getStringValue());
			throw new SAFSException(error, SAFSException.CODE_ERROR_SET_FILE_ATTR);
		}
	}

	/**
	   * Create a unique test/actual filename matching the benchFilename but with a UUID String embedded.
	   * @param benchFilename Example: "benchmark.txt"
	   * @return "benchmarkUUIDMARKER.txt"
	   */
	  public static String deduceMatchingUUIDFilename(String benchFilename){
			String ext = "";
			String tempname = new File(benchFilename).getName();
			int dot = tempname.lastIndexOf('.');
			try{
				ext = tempname.substring(dot+1);
				tempname = tempname.substring(0, dot);
			}catch(Exception ignore){ /* keep tempname as it is */}
			String uid = String.valueOf(System.nanoTime()).replace("-", "");
			String testFilename = tempname + uid;
			if(ext.length() > 0) testFilename += "." + ext;
		    return testFilename;
	  }

	  /**
	   * Deduce the absolute full path to a project-relative, test-relative, bench-relative, diff-relative filename.
	   * @param filename, String, usually a relative path filename.
	   * <p>
	   * There is a caveat for test/bench/diff-relative filenames.  If there is any File.separator
	   * present in the relative path then the path is considered relative to whatever is considered the
	   * Datapool directory unless it does not exist, or is already an absolute file path.
	   * <p>
	   * If it does not exist as the Datapool directory, or a subdirectory of the Datapool directory, then the path is
	   * made relative to the Project directory.  A project-relative path is always relative to the actual project root
	   * directory unless it is given as an absolute file path.
	   * <p>
	   * If the provided filename is an absolute path, and contains a root path that includes the Bench directory,
	   * and the filepath sought is of type Test or Diff, then the filename/filepath will be converted to a comparable
	   * relative path off the Test or Diff directories as appropriate.  This is done to avoid unintentional overwriting
	   * of benchmark files by test/actual and diff files.
	   * <p>
	   * @param type, int, the type of the file: test-relative, bench-relative, project-relative.
	   * @param RuntimeDataInterface to access runtime data (directories). Like a subclass of GenericEngine, or Processor.
	   * @return File, the absolute full path test/bench/diff file.
	   * @throws SAFSException
	   * @see {@link #FILE_TYPE_TEST}
	   * @see {@link #FILE_TYPE_BENCH}
	   * @see {@link #FILE_TYPE_DIFF}
	   * @see {@link #FILE_TYPE_PROJECT}
	   */
	  public static File deduceFile(String filename, int type /** 0->test file; 1->bench file; 2->project file; 3->diff file */, RuntimeDataInterface data) throws SAFSException{
		  File fn = null;
		  if (filename==null || filename.length()==0) {
			  throw new SAFSException("Required filename is not provided!");
		  }
		  File benchfile = new File(data.getVariable(VAR_SAFSBENCHDIRECTORY));
		  File testfile = new File(data.getVariable(VAR_SAFSTESTDIRECTORY));
		  File difffile = new File(data.getVariable(VAR_SAFSDIFDIRECTORY));
		  String benchpath = null;
		  try{ benchpath = benchfile.getCanonicalPath();}catch(IOException ignore){}

		  filename = FileUtilities.normalizeFileSeparators(filename);
		  fn = new CaseInsensitiveFile(filename).toFile();
		  if(fn==null) throw new SAFSException("Filename resolved to null!");

		  String filepath = filename;
		  try{ filepath = fn.getCanonicalPath();}catch(IOException ignore){}

		  // must not let a test or diff file overwrite an absolute bench file
		  if( fn.isAbsolute() && filepath.startsWith(benchpath)){
			  if(type==FILE_TYPE_DIFF || type==FILE_TYPE_TEST){
				  IndependantLog.info("Processor correcting Bench Absolute Path to a comparable Test/Diff relative path to prevent Bench overwrite!");
				  filename = type==FILE_TYPE_TEST ?
						     testfile.getAbsolutePath():
						     difffile.getAbsolutePath();
				  filename += filepath.substring(benchpath.length());
				  fn = new File(filename);
				  if(!fn.getParentFile().exists()) fn.getParentFile().mkdirs();
			  }
		  }

		  if (!fn.isAbsolute()) {
			  String pdir = null;
			  try {
				  if(type==FILE_TYPE_TEST || type==FILE_TYPE_BENCH || type==FILE_TYPE_DIFF){
					  if (filename.indexOf(File.separator) > -1) {
						  try{ pdir = data.getVariable(VAR_SAFSDATAPOOLDIRECTORY);}catch(Exception e){}
						  //if the datapool directory does not exist, use the 'project directory'
						  if ((pdir == null) || (pdir.equals(""))) pdir = data.getVariable(VAR_SAFSPROJECTDIRECTORY);
						  else{
							  CaseInsensitiveFile datapoolDirFile = new CaseInsensitiveFile(pdir);
							  if(!datapoolDirFile.exists() || !datapoolDirFile.isDirectory()){
								  pdir = data.getVariable(VAR_SAFSPROJECTDIRECTORY);
							  }else{
								  CaseInsensitiveFile tmpFile = new CaseInsensitiveFile(filename, pdir);
								  File parentDir = tmpFile.getParentFile();
								  //if combined-file's parent-directory does not exist, use the 'project directory'
								  if(!parentDir.exists()|| !parentDir.isDirectory()){
									  pdir = data.getVariable(VAR_SAFSPROJECTDIRECTORY);
								  }
							  }
						  }
					  } else {
						  if(type==FILE_TYPE_TEST ){
							  pdir = data.getVariable(VAR_SAFSTESTDIRECTORY);
						  }else if(type==FILE_TYPE_BENCH){
							  pdir = data.getVariable(VAR_SAFSBENCHDIRECTORY);
						  }else if(type==FILE_TYPE_DIFF){
							  pdir = data.getVariable(VAR_SAFSDIFDIRECTORY);
						  }
					  }
				  }else if(type==FILE_TYPE_PROJECT){
					  pdir = data.getVariable(VAR_SAFSPROJECTDIRECTORY);
				  }
			  } catch (Exception x) {}
			  if ((pdir == null) || (pdir.equals(""))) {
				  String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, "Could not get one or more variable values.");
				  if(type==FILE_TYPE_TEST){
					  error += " "+ VAR_SAFSDATAPOOLDIRECTORY + ", "+ VAR_SAFSTESTDIRECTORY;
				  }else if(type==FILE_TYPE_BENCH){
					  error += " "+ VAR_SAFSDATAPOOLDIRECTORY + ", "+ VAR_SAFSBENCHDIRECTORY;
				  }else if(type==FILE_TYPE_DIFF){
					  error += " "+ VAR_SAFSDATAPOOLDIRECTORY + ", "+ VAR_SAFSDIFDIRECTORY;
				  }else if(type==FILE_TYPE_PROJECT){
					  error += " "+ VAR_SAFSPROJECTDIRECTORY;
				  }
				  throw new SAFSException(error);
			  }
			  fn = new CaseInsensitiveFile(pdir, filename).toFile();
			  if(! fn.getParentFile().exists()) fn.getParentFile().mkdirs();
		  }
		  IndependantLog.debug("filename '"+filename+"' resolves to: "+ fn.getAbsolutePath());

		  return fn;
	  }

	  /**
	   * Deduce the absolute full path test-relative file.
	   * @param filename, String, the test/actual file name.  If there are any File.separators in the 
	   * relative path then the path is actually considered relative to the Datapool 
	   * directory unless it does not exist, or is already an absolute file path.
	   * <p>
	   * If a relative directory path does not exist relative to the Datapool directory then 
	   * the final path will be relative to the Project directory.
	   * <p>
	   * If it is an absolute path, and contains a root path that includes the Bench directory, then the 
	   * file will be converted to a comparable relative path off the Test directory.
	   * <p>
	   * @param RuntimeDataInterface to access runtime data (directories). Like a subclass of GenericEngine, or Processor.
	   * @return File, the absolute full path test file.
	   * @throws SAFSException
	   * @see {@link #deduceFile(String, int)}
	   */
	  public static File deduceTestFile(String filename, RuntimeDataInterface data) throws SAFSException{
		  return deduceFile(filename, FILE_TYPE_TEST, data);
	  }
	  
	  /**
	   * Deduce the absolute full path Diff-relative file.
	   * @param filename, String, the diff file name.  If there are any File.separators in the 
	   * relative path then the path is actually considered relative to the Datapool 
	   * directory unless it does not exist, or is already an absolute file path.
	   * <p>
	   * If a relative directory path does not exist relative to the Datapool directory then 
	   * the final path will be relative to the Project directory.
	   * <p>
	   * If it is an absolute path, and contains a root path that includes the Bench directory, then the 
	   * file will be converted to a comparable relative path off the Diff directory.
	   * <p>
	   * @param RuntimeDataInterface to access runtime data (directories). Like a subclass of GenericEngine, or Processor.
	   * @return File, the absolute full path diff file.
	   * @throws SAFSException
	   * @see {@link #deduceFile(String, int)}
	   */
	  public static File deduceDiffFile(String filename, RuntimeDataInterface data) throws SAFSException{
		  return deduceFile(filename, FILE_TYPE_DIFF, data);
	  }
	  
	  /**
	   * Deduce the absolute full path bench-relative file.
	   * @param filename, String, the test file name.  If there are any File.separators in the 
	   * relative path then the path is actually considered relative to the Datapool 
	   * directory unless it does not exist, or is already an absolute file path.
	   * If a relative directory path does not exist relative to the Datapool directory then 
	   * the final path will be relative to the Project directory.
	   * @param RuntimeDataInterface to access runtime data (directories). Like a subclass of GenericEngine, or Processor.
	   * @return File, the absolute full path bench file.
	   * @throws SAFSException
	   * @see {@link #deduceFile(String, int)}
	   */
	  public static File deduceBenchFile(String filename, RuntimeDataInterface data) throws SAFSException{
		  return deduceFile(filename, FILE_TYPE_BENCH, data);
	  }
	  
	  /**
	   * Deduce the absolute full path to a project-relative file.
	   * @param filename, String, the test file name.  The path is ALWAYS considered relative 
	   * to the project root directory regardless of the absence or presence of File.separators 
	   * unless the file is already an absolute path.
	   * @param RuntimeDataInterface to access runtime data (directories). Like a subclass of GenericEngine, or Processor.
	   * @return File, the absolute full path bench file.
	   * @throws SAFSException 
	   * @see {@link #deduceFile(String, int)}
	   */
	  public static File deduceProjectFile(String filename, RuntimeDataInterface data) throws SAFSException{
		  return deduceFile(filename, FILE_TYPE_PROJECT, data);
	  }
	  
	
	/**
	 * @param file File, the file to extract the attribute information.
	 * @return FileAttribute, the file's attributes
	 * @throws SAFSException
	 */
	public static FileAttribute getFileAttribute(File file) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		FileAttribute attribute = FileAttribute.instance(Type.NORMALFILE);

		//Windows OS uses non portable DOS "attrib", other OS use portable java
		try{
			if(Console.isWindowsOS()){
				IndependantLog.info(debugmsg+"Windows OS, using DOS '"+DOS_COMMAND_ATTRIB+"' command");
				DefaultConsoleTool console = DefaultConsoleTool.instance();
				Process2 p2 = console.exec(DOS_COMMAND_ATTRIB, file.getCanonicalPath());
//				IndependantLog.info(debugmsg+" exec command: "+console.getLastCommand());
				p2.waitForSuccess();
				InputStream stdout = p2.getStdout();
				byte[] cbuf = new byte[128];
				int count = 0;

				if((count=stdout.read(cbuf))>0){
					IndependantLog.info(debugmsg+" got attribut "+new String(cbuf,0,count));
					if (cbuf[0] == ATTRIBUTE_ARCHIVE) attribute.add(Type.ARCHIVEFILE);
					if (cbuf[3] == ATTRIBUTE_SYSTEM) attribute.add(Type.SYSTEMFILE);
					if (cbuf[4] == ATTRIBUTE_HIDDEN) attribute.add(Type.HIDDENFILE);
					if (cbuf[5] == ATTRIBUTE_READONLY) attribute.add(Type.READONLYFILE);
				}
			}else{
				IndependantLog.info(debugmsg+"Using '"+Console.getOsFamilyName()+"' OS, use java to determine attribute.");

				//if(f.canRead() && f.canWrite());//normal in DOS lingo, default is normal
				if(isReadOnly(file)) attribute.add(Type.READONLYFILE);
				if(file.isHidden()) attribute.add(Type.HIDDENFILE);
				if(isArchive(file)) attribute.add(Type.ARCHIVEFILE);
			}

			if(file.isDirectory()) attribute.add(Type.DIRECTORY);

		}catch(Exception e){
			IndependantLog.error(debugmsg+"Met "+StringUtils.debugmsg(e));
			throw new SAFSException(StringUtils.debugmsg(e));
		}

		return attribute;
	}

	/**
	 * <pre>
	 * Note:
	 *  To form a new instance of class FileAttributeFilter,please use
	 *  one of the following file attribute's code or their combination:
	 *  {@link FileAttribute.Type#NORMALFILE}
	 *  {@link FileAttribute.Type#READONLYFILE}
	 *  {@link FileAttribute.Type#HIDDENFILE}
	 *  {@link FileAttribute.Type#SYSTEMFILE}
	 *  {@link FileAttribute.Type#DIRECTORY}
	 *  {@link FileAttribute.Type#ARCHIVEFILE}
	 *
	 *  For example:
	 *  //this filter will help to get all normal files, all hidden files and all archive files.
	 *  FileAttributeFilter filter = new FileAttributeFilter(new FileAttribute(Type.HIDDENFILE).add(Type.ARCHIVEFILE));
	 *
	 *  If OS is other than Windows:
	 *  1. SYSTEMFILE will not have effect.
	 *  2. ARCHIVEFILE will only affect files which end with suffix listed in array ARCHIVEFILESUFFIX;
	 *     Array ARCHIVEFILESUFFIX contains ".jar",".tar",".zip",".rar",".a",".ar",".gz",".tgz".
	 * </pre>
	 */

	public static class FileAttributeFilter implements FileFilter{
		FileAttribute expectedAttributes = FileAttribute.instance();

		public FileAttributeFilter(int attributes) throws SAFSException{
			this.expectedAttributes = new FileAttribute(attributes);
		}

		public FileAttributeFilter(FileAttribute attribute) throws SAFSException{
			this.expectedAttributes = attribute;
		}

		public boolean accept(File pathname) {
			String debugmsg = StringUtils.debugmsg(false);

			boolean isreadonly	= false;
			boolean issystem	= false;
			boolean ishidden	= false;
			boolean isdirectory	= false;
			boolean isarchive 	= false;

			//Get the actual attribute for a file
			try{
				FileAttribute actualAttribute = getFileAttribute(pathname);
				isreadonly = actualAttribute.containsReadOnly();
				issystem = actualAttribute.containsSystem();
				ishidden = actualAttribute.containsHidden();
				isdirectory = actualAttribute.containsDirectory();
				isarchive = actualAttribute.containsArchive();
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(e));
			}

			IndependantLog.info(debugmsg+" expectedAttributes="+expectedAttributes.getValue());
			//This is NORMALFILE
			if(!(isreadonly||ishidden||isdirectory||isarchive||issystem)){
				return true;
			}

			if(expectedAttributes.containsReadOnly() && isreadonly){
				return true;
			}
			if(expectedAttributes.containsHidden() && ishidden){
				return true;
			}
			if(expectedAttributes.containsDirectory() && isdirectory){
				return true;
			}
			if(expectedAttributes.containsArchive() && isarchive){
				return true;
			}
			if(expectedAttributes.containsSystem() && issystem){
				return true;
			}

			return false;
		}
	}
}

