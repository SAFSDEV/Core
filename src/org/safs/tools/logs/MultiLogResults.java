package org.safs.tools.logs;

import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.safs.tools.CaseInsensitiveFile;

/**
 * Processes a user-specified directory for SAFS XML Log files.  
 * Creates a new single XML List file consolidating the file hrefs for all SAFS XML Log files in the directory. 
 * Then, optionally, the class creates a summary XML file and/or a summary JAXP Document Node containing:
 * <ul>
 * <p>
 * <li>For each XML file that proves to be a SAFS XML Log the class will: 
 * <p>
 * <ol>
 * <li>Record the log file href information uniquely identifying the log
 * <li>Record the date/time the log was opened
 * <li>Record the final status report information for that log
 * <li>Maintain a cumulative summary of all status report information for all files
 * <li>Record the date/time the log was closed
 * </ol>
 * <p>
 * <li>Record the cumulative summary of all status report information from all files
 * </ul>
 * <p>
 * JVM Arguments:<br>
 * <ul>
 * <p>
 * <li><b-dir</b> "C:\\SAFS\\Project\\Datapool\\Logs"<br>
 * Full path to the directory containing the SAFS XML log(s) to consolidate and/or summarize.<br>
 * This must be specified unless the -log argument contains a full path to a single log to be processed.<br>
 * Same as JVM Option: <b>>-Dsafs.multilog.dir</b>="&lt;path/to/xmllogs/directory>"<br>
 * Ex: -Dsafs.multilog.dir="C:\\SAFS\\Project\\Datapool\\Logs"
 * <p>
 * <li><b>-out</b> "C:\\SAFS\\Project\\Reports"<br>
 * Full path to the directory where any generated files will be written.<br>  
 * By default all generated files will be written to the same directory where input files are found.<br>
 * Same as JVM Option: <b>-Dsafs.multilog.out</b>="&lt;path/to/output/directory>"<br>
 * Ex: -Dsafs.multilog.out="C:\\SAFS\\Project\\Reports"
 * <p>
 * <li><b>-list</b> "MultiLogResultsList.xml"<br>
 * Full or relative path to an existing XML Logs List file to process for summarization.  
 * This allows the user to pre-specify a set of log files to process instead of letting the 
 * program process ALL log files present in a logs directory.  A relative path is relative 
 * to the specified -dir logs directory.<br>
 * Same as JVM Option: <b>-Dsafs.multilog.list</b>="&lt;path/to/safsxmllogslist.xml>"<br>
 * Ex: -Dsafs.multilog.list="MultiLogResultsList.xml"<br>
 * Ex: -Dsafs.multilog.list="C:\\SAFS\\Project\\Datapool\\Logs\\MultiLogResultsList.xml"
 * <p>
 * <li><b>-xsl</b> "XSLMultiLogResults.xsl"<br>
 * Full or relative path to an existing XSL file to use for summarization. A relative file 
 * specification assumes the XSL file is in the same directory as the XML List file to be processed.<br>
 * Same as JVM Option: <b>-Dsafs.multilog.xsl</b>="&lt;path/to/safsxmllogsxsl.xsl>"<br>
 * Ex: -Dsafs.multilog.xsl="C:\\SAFS\\Project\\Datapool\\Logs\\XSLMultiLogResults.xsl"<br>
 * Ex: -Dsafs.multilog.xsl="XSLMultiLogResults.xsl"
 * <p>
 * <li><b>-log</b> "TestComposerLog.xml"<br>
 * Full or relative path to a single SAFS XML log to summarize.<br>
 * Same as JVM Option: <b>-Dsafs.multilog.log</b>="&lt;path/to/single/safsxmllog.xml>"<br>
 * Ex: -Dsafs.multilog.log="TestComposerLog.xml"<br>
 * Ex: -Dsafs.multilog.log="C:\\SAFS\\Project\\Datapool\\Logs\\TestComposerLog.xml"
 * <p>
 * <li><b>-nosummary</b><br>
 * If present, only the initial XML list file containing the file hrefs for all SAFS XML log files will be created. 
 * All subsequent processing will be aborted.<br>
 * Same as JVM Option: <b>-Dsafs.multilog.nosummary</b>
 * <p>
 * <li><b>-summary</b> "EMinerSummary.xml"<br>
 * Full or relative path for the summary XML file to be created.  
 * If a relative path is provided then we will:
 * <p><ul>
 * <li>look first to write to the specified log -out directory,  
 * <li>else, look to write to the specified logs -dir directory,
 * <li>else, attempt to write to the directory where a single -log was processed,
 * <li>else, an error in arguments has occurred.
 * </ul>
 * <p>  
 * Same as JVM Option: <b>-Dsafs.multilog.summary</b>="&lt;path/to/xml/summary.xml>"<br>
 * Ex: -Dsafs.multilog.summary="EMinerSummary.xml"<br>
 * Ex: -Dsafs.multilog.summary="C:\\SAFS\\Project\\Reports\\EMinerSummary.xml"
 * <p>
 * This class requires a "registered" JAXP XSLT Processor to be in the runtime CLASSPATH.<br>
 * A registered JAXP XSLT Processor generally contains TransformerFactory services information in the 
 * META-INF\services directory of its JAR file library. This class was tested using the SAXON 9.4 
 * JAR files be in the runtime CLASSPATH:
 * <p>
 * <ul>
 * <b>
 * <li>saxon9he.jar
 * <li>saxon9-unpack.jar
 * </b>
 * </ul>
 * <p>
 * Other XSLT Processors conforming to the JAXP standards and XSLT 2.0 specification should also work.
 * <p>
 * The processor must support XSLT 2.0 or higher.  Specifically, that result tree fragments can 
 * be internally converted to and be treated as valid node-sets.  MSXML, the MSXSL.EXE program, and 
 * older (sometimes default) versions of XALAN do not support such tree fragment conversions when 
 * they strictly adhere to XSLT 1.0 specifications.
 * <p> 
 * @author canagl
 * <br>FEB 20, 2013 CANAGL Added support for the default XSL file to use.
 */
public class MultiLogResults {

	
	/** Create an XML summary file from one or more SAFS XML log files. */
	public boolean DO_SUMMARY = true;
	
	/** "MultiLogResultsList.xml" */
	public static final String DEFAULT_LIST_FILENAME = "MultiLogResultsList.xml";
	
	/** "MultiLogResultsSummary.xml" */
	public static final String DEFAULT_SUMMARY_FILENAME = "MultiLogResultsSummary.xml";

	/** "XSLMultiLogsListSummary.xsl" */
	public static final String DEFAULT_XSL_FILENAME = "XSLMultiLogsListSummary.xsl";

	/** "safs.multilog.dir" */
	public static final String PROP_KEY_SAFS_MULTILOG_DIR       ="safs.multilog.dir";
	/** "safs.multilog.out" */
	public static final String PROP_KEY_SAFS_MULTILOG_OUT       ="safs.multilog.out";
	/** "safs.multilog.xsl" */
	public static final String PROP_KEY_SAFS_MULTILOG_XSL       ="safs.multilog.xsl";
	/** "safs.multilog.list" */
	public static final String PROP_KEY_SAFS_MULTILOG_LIST      ="safs.multilog.list";
	/** "safs.multilog.log" */
	public static final String PROP_KEY_SAFS_MULTILOG_LOG       ="safs.multilog.log";
	/** "safs.multilog.nosummary" */
	public static final String PROP_KEY_SAFS_MULTILOG_NOSUMMARY ="safs.multilog.nosummary";
	/** "safs.multilog.summary" */
	public static final String PROP_KEY_SAFS_MULTILOG_SUMMARY   ="safs.multilog.summary";
	
	/** "-dir" */
	public static final String ARG_DIR       ="-dir";
	/** "-out" */
	public static final String ARG_OUT       ="-out";
	/** "-log" */
	public static final String ARG_LOG       ="-log";
	/** "-xsl" */
	public static final String ARG_XSL       ="-xsl";
	/** "-list" */
	public static final String ARG_LIST      ="-list";
	/** "-nosummary" */
	public static final String ARG_NOSUMMARY ="-nosummary";
	/** "-summary" */
	public static final String ARG_SUMMARY   ="-summary";
	
	protected boolean absoluteLogs    = false;
	protected boolean absoluteList    = false;
	protected boolean absoluteLog     = false;
	protected boolean absoluteXSL     = false;
	protected boolean absoluteSummary = false;
	protected boolean absoluteOut     = false;
	protected boolean isValidated     = false;
	protected boolean isListInput     = false;
	
	protected File DIR_LOGS     = null;
	protected File DIR_OUT      = null;
	protected File FILE_LIST    = null;
	protected File FILE_LOG     = null;
	protected File FILE_XSL     = new File(DEFAULT_XSL_FILENAME);
	protected File FILE_SUMMARY = null;

	/** ".xml" */
	protected static final String XML_SUFFIX_LC = ".xml";
	
	/** ".xsl" */
	protected static final String XSL_SUFFIX_LC = ".xsl";
	
	/** "UTF-8" */
	protected static final String UTF8_CHARSET = "UTF-8";
	
	/** Currently logs to System.out.  Other options may be supported in the future. */
	public static void log(String message){
		System.out.println(message);
	}
	
	/** 
	 * @return true if all set parameters have been validated successfully and the instance is 
	 * set to process a pre-existing XML File list instead of creating one dynamically. */
	public boolean hasListInput(){ return isValidated && isListInput; }
	
	/**
	 * @return true if all set parameters have been validated successfully--which also means an 
	 * XSL document has been identified as existing.
	 */
	public boolean hasXSLInput(){ return isValidated; }
	
	/**
	 * @return true if all the set parameters have been validated successfully.
	 */
	public boolean hasValidated(){ return isValidated ; }

	/** 
	 * @return true if all set parameters have been validated successfully and the instance is 
	 * set to process a single pre-existing SAFS XML Log File instead of consolidating multiple 
	 * SAFS XML log files. */
	public boolean hasLogInput() { return isValidated && absoluteLog; }
	
	/**
	 * clears ALL flags associated with validated settings including the isValidated flag.
	 */
	public void resetValidation(){
		isValidated = false;
		isListInput = false;
		absoluteLogs = false;
		absoluteOut = false;
		absoluteLog = false;
		absoluteXSL = false;
		absoluteList = false;
		absoluteSummary = false;
	}
	
	/** convenience routine to retrieve a System property value or null--
	 * catching any thrown "not found" exceptions. */
	protected static String getSystemProperty(String pname){
		try{
			return System.getProperty(pname);
		}catch(Throwable t){}
		return null;
	}
	
	/** (Re)Set any arguments passed in as JVM -D Options. 
	 * A call to this routine will also resetValidation()
	 **/
	public void processJVMOptions(){
		resetValidation();
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_DIR) != null) DIR_LOGS     = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_DIR ));		
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_OUT) != null) DIR_OUT      = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_OUT ));
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_LOG) != null) FILE_LOG     = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_LOG ));
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_XSL) != null) FILE_XSL     = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_XSL ));
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_LIST)!= null) FILE_LIST    = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_LIST));
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_NOSUMMARY) != null) DO_SUMMARY = false;
		if(getSystemProperty( PROP_KEY_SAFS_MULTILOG_SUMMARY) != null) FILE_SUMMARY = new File(getSystemProperty( PROP_KEY_SAFS_MULTILOG_SUMMARY));
	}

	/** String[] args will override any processJVMOptions previously set-- 
	 * assuming JVM Options are processed BEFORE these args.    
	 * A call to this routine will also resetValidation(). 
	 * @param args -- the String[] command-line args received or otherwise provided. */
	public void processArgs(String[] args){
		resetValidation();
		if(args == null) args = new String[0];
		String arg = null;
		for(int i=0;i < args.length;i++){
			try{
				arg = args[i];
				if(arg.length() == 0) continue;
				if(ARG_DIR.equalsIgnoreCase(arg))        { DIR_LOGS     = new File(args[++i]); continue; }
				if(ARG_OUT.equalsIgnoreCase(arg))        { DIR_OUT      = new File(args[++i]); continue; }
				if(ARG_LOG.equalsIgnoreCase(arg))        { FILE_LOG     = new File(args[++i]); continue; }
				if(ARG_XSL.equalsIgnoreCase(arg))        { FILE_XSL     = new File(args[++i]); continue; }
				if(ARG_LIST.equalsIgnoreCase(arg))       { FILE_LIST    = new File(args[++i]); continue; }
				if(ARG_NOSUMMARY.equalsIgnoreCase(arg))  { DO_SUMMARY   = false;               continue; }
				if(ARG_SUMMARY.equalsIgnoreCase(arg))    { FILE_SUMMARY = new File(args[++i]); continue; }
			}catch(Exception x){}
		}		
	}

	/**
	 * Call this routine to validate the current instance configuration settings.  This would normally 
	 * be called after we have processJVMOptions and processArgs to verify the current 
	 * setup should work.  That is, that specified directories and files provided in the arguments do 
	 * indeed exist, as appropriate.
	 * <p><pre>
		LOGS (-dir) must be specified unless an absolute LOG _OR_ an absolute LIST is specified.
		It is an error to specify both a LOG and a LIST.
		LOGS (-dir) will be assigned the LOG parent directory when an absolute LOG is specified.
		LOGS (-dir) will be assigned the LIST parent directory when an absolute LIST is specified and LOGS is not specified.
		OUT is optional always.  
		OUT will be assigned the LOGS directory if OUT is not specified.
		OUT will be assigned the SUMMARY parent directory when an absolute SUMMARY is specified and 
		    OUT is not specified.
		SUMMARY is optional always.  SUMMARY will be assigned the default name if not specified.
		LIST is optional always. LIST will be assigned the default name if not specified and we are 
		    NOT processing a single LOG.
     * </pre>
     * <p>
	 * @return true if all settings validated successfully.  Throws IllegalArgumentException otherwise.
	 * @throws IllegalArgumentException if a problem is found with the current configuration.
	 */
	public boolean validate() throws IllegalArgumentException{		
		resetValidation();		
		if(FILE_LOG != null && FILE_LIST != null) throw new IllegalArgumentException(
			" a -log file and a -list file cannot both be provided as arguments.  They are mutually exclusive.");
		
		if(FILE_LIST != null){
			if(FILE_LIST.isAbsolute()){
				File list = new CaseInsensitiveFile(FILE_LIST.getAbsolutePath()).toFile();
				if(list.isFile()){
					if(!list.getName().toLowerCase().endsWith(XML_SUFFIX_LC)) throw new IllegalArgumentException(
						"the specified -list file does NOT have the required .xml file suffix.");
					FILE_LIST = list;
					isListInput = true;
					absoluteList = true;
					if(DIR_LOGS == null){
						DIR_LOGS = FILE_LIST.getParentFile();
						absoluteLogs = true;
					}
				}else throw new IllegalArgumentException(
					"the specified -list file "+ list.getAbsolutePath() +" does not exist");
			}else{
				if(DIR_LOGS == null || !DIR_LOGS.isAbsolute()) throw new IllegalArgumentException(
					"the -dir directory must be specified as a full absolute path for relative -list file paths.");
				File dir = new CaseInsensitiveFile(DIR_LOGS.getAbsolutePath()).toFile();
				if(dir.isDirectory()){
					DIR_LOGS = dir;
					absoluteLogs = true;
				}else throw new IllegalArgumentException(
					"the specified -dir directory '"+ DIR_LOGS.getAbsolutePath()+"' is NOT a valid directory.");
				File list = new CaseInsensitiveFile(DIR_LOGS, FILE_LIST.getPath()).toFile();
				if(list.isFile()){
					if(!list.getName().toLowerCase().endsWith(XML_SUFFIX_LC)) throw new IllegalArgumentException(
							"the specified -list file does NOT have the required .xml file suffix.");
					FILE_LIST = list;
					isListInput = true;
					absoluteList = true;
				}else throw new IllegalArgumentException(
					"the specified -list file '"+ FILE_LIST.getPath() +"' does not exist in '"+ DIR_LOGS.getAbsolutePath() +"'");
			}
		}
		
		if(FILE_LOG != null){
			if(FILE_LOG.isAbsolute()){
				File log = new CaseInsensitiveFile(FILE_LOG.getAbsolutePath()).toFile();
				if(log.isFile()){
					if(!log.getName().toLowerCase().endsWith(XML_SUFFIX_LC)) throw new IllegalArgumentException(
							"the specified -log file does NOT have the required .xml file suffix.");
					FILE_LOG = log;
					absoluteLog = true;
					DIR_LOGS = FILE_LOG.getParentFile();
					absoluteLogs = true;
				}else throw new IllegalArgumentException(
					"specified -log file "+ log.getAbsolutePath() +" does not exist");
			}else{
				if(DIR_LOGS == null || !DIR_LOGS.isAbsolute()) throw new IllegalArgumentException(
					"the -dir directory must be specified as a full absolute path for relative -log file paths.");
				File dir = new CaseInsensitiveFile(DIR_LOGS.getAbsolutePath()).toFile();
				if(dir.isDirectory()){
					DIR_LOGS = dir;
					absoluteLogs = true;
				}else throw new IllegalArgumentException(
					"the specified -dir directory '"+ DIR_LOGS.getAbsolutePath()+"' is NOT a valid directory.");
				File log = new CaseInsensitiveFile(DIR_LOGS, FILE_LOG.getPath()).toFile();
				if(log.isFile()){
					if(!log.getName().toLowerCase().endsWith(XML_SUFFIX_LC)) throw new IllegalArgumentException(
							"the specified -log file does NOT have the required .xml file suffix.");
					FILE_LOG = log;
					absoluteLog = true;
				}else throw new IllegalArgumentException(
					"the specified -log file '"+ FILE_LOG.getPath() +"' does not exist in '"+ DIR_LOGS.getAbsolutePath() +"'");
			}
		}
		
		if(! absoluteLogs){
			if(DIR_LOGS == null || !DIR_LOGS.isAbsolute()) throw new IllegalArgumentException(
				"the -dir directory must be specified as a full absolute path.");
			File dir = new CaseInsensitiveFile(DIR_LOGS.getAbsolutePath()).toFile();
			if(dir.isDirectory()){
				DIR_LOGS = dir;
				absoluteLogs = true;
			}else throw new IllegalArgumentException(
				"the specified -dir directory '"+ DIR_LOGS.getAbsolutePath()+"' is NOT a valid directory.");			
		}
		
		if(DIR_OUT != null){
			if(! DIR_OUT.isAbsolute()) throw new IllegalArgumentException(
				"the specified -out directory must be specified as a full absolute path.");
			File dir = new CaseInsensitiveFile(DIR_OUT.getAbsolutePath()).toFile();
			if(! dir.isDirectory()) throw new IllegalArgumentException(
				"the specified -out directory '"+ DIR_OUT.getAbsolutePath() +"' is NOT a valid directory.");
			DIR_OUT = dir;
			absoluteOut = true;
		}
		
		if(FILE_SUMMARY!=null){
			if(!FILE_SUMMARY.getName().toLowerCase().endsWith(XML_SUFFIX_LC)) throw new IllegalArgumentException(
					"the specified -summary file does NOT have the required .xml file suffix.");
			if(FILE_SUMMARY.isAbsolute()){
				File dir = new CaseInsensitiveFile(FILE_SUMMARY.getParent()).toFile();
				File file = null;
				if(!dir.isDirectory())throw new IllegalArgumentException(
					"the specified -summary file output directory '"+ dir.getAbsolutePath() +"' does NOT exist.");
				file = new CaseInsensitiveFile(FILE_SUMMARY.getAbsolutePath()).toFile();
				FILE_SUMMARY = file;
				absoluteSummary = true;
				if(DIR_OUT==null){
					DIR_OUT = dir;
					absoluteOut = true;
				}
			}else{
				if(DIR_OUT==null){
					File file = new CaseInsensitiveFile(DIR_LOGS, FILE_SUMMARY.getPath()).toFile();
					FILE_SUMMARY = file;
					absoluteSummary = true;
					DIR_OUT = file.getParentFile();
					absoluteOut = true;
				}else{
					File file = new CaseInsensitiveFile(DIR_OUT, FILE_SUMMARY.getPath()).toFile();
					FILE_SUMMARY = file;
					absoluteSummary = true;
				}
			}
			try{ 
				if(FILE_SUMMARY.isFile()) {
				   FILE_SUMMARY.delete(); 
				}
			}catch(SecurityException e){ throw new IllegalArgumentException(e);	}
		}
		
		if(DIR_OUT==null){
			DIR_OUT = new CaseInsensitiveFile(DIR_LOGS.getAbsolutePath()).toFile();
			absoluteOut = true;
		}
		
		//provide the default SUMMARY file if none was provided
		if(FILE_SUMMARY == null){
			FILE_SUMMARY = new CaseInsensitiveFile(DIR_OUT, DEFAULT_SUMMARY_FILENAME).toFile();
			if(FILE_SUMMARY.isFile()){ 
				try{
					FILE_SUMMARY.delete(); 
				}catch(SecurityException e){
					throw new IllegalArgumentException(e);
				}
			}
			absoluteSummary = true;
		}
		
		// provide the default output LIST file if none was provided for input.
		if(FILE_LIST == null){
			FILE_LIST = new CaseInsensitiveFile(DIR_OUT, DEFAULT_LIST_FILENAME).toFile();
			if(FILE_LIST.isFile()){ 
				try{
					FILE_LIST.delete(); 
				}catch(SecurityException e){
					throw new IllegalArgumentException(e);
				}
			}
			isListInput = false;
			absoluteList = true;
		}
		
		if(DO_SUMMARY){
			if(FILE_XSL == null) throw new IllegalArgumentException(
				"the -xsl file must be specified for a log summarization to be performed.");
			if(FILE_XSL.isAbsolute()){
				FILE_XSL = new CaseInsensitiveFile(FILE_XSL.getAbsolutePath()).toFile();
			}else{
				// relative file should be in same directory as the XML List file
				FILE_XSL = new CaseInsensitiveFile(FILE_LIST.getParentFile(), FILE_XSL.getPath()).toFile();
			}
			if(! FILE_XSL.isFile()) throw new IllegalArgumentException(
				"the -xsl file '"+ FILE_XSL.getAbsolutePath()+"' is missing or is not a valid file path.");				
			absoluteXSL = true;			
		}
		isValidated = true;
		return isValidated;
	}
	
	/** 
	 * Create an XML List File listing all SAFS XML log files in the inputLogsDir. 
	 * @param inputLogsDir -- fullpath directory to process for SAFS XML log files.  If this is null then 
	 * we will use whatever arguments have previously been provided.  If non-null, a call to re-validate 
	 * all arguments will occur.
	 * @param outputListFile -- fullpath XML list file to create.  If this is null then we will 
	 * use whatever arguments have previously been provided.  If non-null, a call to re-validate all 
	 * arguments will occur.
	 * @return File object to created XML List File, or null if there were no logs found or processed.
	 * @throws IllegalArgumentException if inputLogsDir is invalid, does not exist, or if the outputListFile 
	 * cannot be created.
	 **/
	public File createXMLLogsList(String inputLogsDir)throws IllegalArgumentException {
		if(inputLogsDir != null){
			DIR_LOGS = new File(inputLogsDir);
			validate();
		}
		try{
			if(FILE_LIST.isFile()) FILE_LIST.delete();
			if(! FILE_LIST.createNewFile()) throw new IllegalArgumentException("Could not create "+ FILE_LIST.getAbsolutePath());						
		}catch(SecurityException e){ throw new IllegalArgumentException(e);			
		}catch(IOException e){ throw new IllegalArgumentException(e); }
		
		File[] files = DIR_LOGS.listFiles();
		if(files.length == 0) return null;
		
		// start writing the file 
		BufferedWriter writer = null;
		boolean hasOne = false;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_LIST), UTF8_CHARSET));
			writer.write("<?xml version='1.0' encoding='UTF-8' ?>\n");
			writer.write("<filelist>\n");
			File f;
			String n;
			for(int i=0;i< files.length;i++){
				f = files[i];
				if(f.isDirectory()) continue;
				n = f.getName();
				if(! n.toLowerCase().endsWith(XML_SUFFIX_LC)) continue;
				if(n.equals(FILE_LIST.getName())) continue;
				writer.write("   <file href='"+ n +"' />\n");
				log("Adding '"+ n +"' to XML Logs List.");
				hasOne = true;
			}
			writer.write("</filelist>\n");
			writer.flush();
		}catch(Exception e){
			throw new IllegalArgumentException(e); 
		}finally{
			try{writer.close();}catch(Exception x){}
			writer = null;
		}		
		return hasOne ? FILE_LIST.getAbsoluteFile(): null;
	}
	
	/** 
	 * Create an XML List File listing a single SAFS XML log file. 
	 * @param inputLogDir -- full path to directory containing XML log file.  If this is null then 
	 * we will use whatever arguments have previously been provided.  If non-null, a call to re-validate
	 * all arguments will occur.
	 * @param inputLogFile -- full or relative path to single SAFS XML log file.  If this is null then 
	 * we will use whatever arguments have previously been provided.  If non-null, a call to re-validate 
	 * all arguments will occur.
	 * @param outputListFile -- full path to the XML list file to create.  If this is null then we will 
	 * use whatever arguments have previously been provided.  If non-null, a call to re-validate all 
	 * arguments will occur.
	 * @return File object to created XML List File, or null if there was no log found or processed.
	 * @throws IllegalArgumentException if inputLogFile is invalid, does not exist, or if the outputListFile 
	 * cannot be created.
	 **/
	public File createSingleXMLLogList(String inputLogDir, String inputLogFile)throws IllegalArgumentException {
		boolean revalidate = !isValidated;
		if(inputLogDir != null){
			DIR_LOGS = new File(inputLogDir);
			revalidate = true;
		}
		if(inputLogFile != null){
			FILE_LOG = new File(inputLogFile);
			revalidate = true;
		}
		if(revalidate) validate();
		try{
			if(FILE_LIST.isFile()) FILE_LIST.delete();
			if(! FILE_LIST.createNewFile()) throw new IllegalArgumentException("Could not create "+ FILE_LIST.getAbsolutePath());						
		}catch(SecurityException e){ throw new IllegalArgumentException(e);			
		}catch(IOException e){ throw new IllegalArgumentException(e); }
		
		// start writing the file 
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_LIST), UTF8_CHARSET));
			writer.write("<?xml version='1.0' encoding='UTF-8' ?>\n");
			writer.write("<filelist>\n");
			writer.write("   <file href='"+ FILE_LOG.getName() +"' />\n");
			log("Adding '"+ FILE_LOG.getName() +"' to XML Logs List.");
			writer.write("</filelist>\n");
			writer.flush();
		}catch(Exception e){
			throw new IllegalArgumentException(e); 
		}finally{
			try{writer.close();}catch(Exception x){}
			writer = null;
		}		
		return FILE_LIST.isFile() ? FILE_LIST.getAbsoluteFile(): null;
	}
	
	/**
	 * Create an XML Summary File from the transformation of an XML List file and XSL transformation file.
	 * @param inputXMLFile -- fullpath to valid XML List File to be used in transformation.  
	 * @param inputXSLFile -- fullpath to valid XSL File to be used in transformation.
	 * @param outputXMLFile -- fullpath to the desired output XML File to be created or overwritten.  
	 * The directory for this file must already exist.
	 * @return File object pointing to the created XML Summary File, or null if the file was not created 
	 * or otherwise does not exist.
	 * @throws IllegalArgumentException if any of the input parameters are invalid or otherwise not usable.
	 * @throws Exception --various exceptions that might be thrown from the JAXP XML transformation classes.
	 */
	public File createXMLLogsSummary(String inputXMLFile, String inputXSLFile, String outputXMLFile) throws IllegalArgumentException, Exception{
		File f = new CaseInsensitiveFile(inputXMLFile).toFile();
		if(!f.isFile()) throw new IllegalArgumentException("inputXMLFile is invalid.");
		f = new CaseInsensitiveFile(inputXSLFile).toFile();
		if(!f.isFile()) throw new IllegalArgumentException("inputXSLFile is invalid.");
		f = new CaseInsensitiveFile(outputXMLFile).toFile();
		if(!f.isAbsolute()) throw new IllegalArgumentException("outputXMLFile is invalid--must be an absolute path.");
		if(!f.getParentFile().isDirectory()) throw new IllegalArgumentException("outputXMLFile must point to a valid directory.");
		f = null;
	    TransformerFactory factory = TransformerFactory.newInstance();
	    DOMSource stylesheet = new DOMSource(buildDoc(inputXSLFile));
	    StreamSource xmlDoc = new StreamSource(inputXMLFile);
	    StreamResult result = new StreamResult(new FileOutputStream(outputXMLFile));	    
	    Transformer transFormer = factory.newTransformer(stylesheet);
	    transFormer.transform(xmlDoc, result);
		return FILE_SUMMARY.isFile() ? FILE_SUMMARY.getAbsoluteFile(): null;
	}
	
	/**
	 * Create an JAXP XML Document Node from the transformation of an XML List file and XSL transformation file.
	 * @param inputXMLFile -- fullpath to valid XML List File to be used in transformation.  
	 * @param inputXSLFile -- fullpath to valid XSL File to be used in transformation.
	 * @return The top-level XML Document as a DOM document Node if the transform was successful.
	 * Can be null if no Node was set, or if the transformation was not completed.  
	 * @throws IllegalArgumentException if any of the input parameters are invalid or otherwise not usable.
	 * @throws Exception -- various exceptions that might be thrown from the JAXP XML transformation classes.
	 */
	public Node createXMLLogsSummaryNodes(String inputXMLFile, String inputXSLFile) throws Exception{
		File f = new CaseInsensitiveFile(inputXMLFile).toFile();
		if(!f.isFile()) throw new IllegalArgumentException("inputXMLFile is invalid.");
		f = new CaseInsensitiveFile(inputXSLFile).toFile();
		if(!f.isFile()) throw new IllegalArgumentException("inputXSLFile is invalid.");
		f = null;
	    TransformerFactory factory = TransformerFactory.newInstance();
	    DOMSource stylesheet = new DOMSource(buildDoc(inputXSLFile));
	    StreamSource xmlDoc = new StreamSource(inputXMLFile);
	    DOMResult dom = new DOMResult();	    
	    Transformer transFormer = factory.newTransformer(stylesheet);
	    transFormer.transform(xmlDoc, dom);
		return dom.getNode();
	}
	
	/**
	 * Used internally.
	 * @param document -- fullpath to valid (XSL) file to be used in transformation.  
	 * @return JAXP DOM Document object loaded and parsed.
	 * @throws Exception -- various exceptions that might be thrown from the JAXP XML transformation classes.
	 */
	protected static Document buildDoc(String document) throws Exception{
		File f = new CaseInsensitiveFile(document).toFile();
		if(!f.isFile()) throw new IllegalArgumentException("document file is invalid.");
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document theDocument = db.parse(f);
		return theDocument;
    }

	/**
	 * Normally only called AFTER a successful execution.
	 * @return true if the instance is still "validated" and the file exists.  
	 */
	public boolean hasXMLListFile(){
		return isValidated && FILE_LIST.isFile();
	}
	
	/**
	 * Normally only called AFTER a successful execution.
	 * @return File reference to the input or created XML Log List file, but only if the instance 
	 * is still "validated" and the file exists.  Otherwise, throws an IllegalStateException.
	 * @throws IllegalStateException if the current configuration is not "validated" or the 
	 * requested file does not exist.
	 */
	public File getXMLListFile() throws IllegalStateException{
		if (! (isValidated && FILE_LIST.isFile())) throw new IllegalStateException(
			"Current configuration is not valid, or the XML List File does not exist!");
		return FILE_LIST.getAbsoluteFile();
	}
	
	/**
	 * @return true if the instance is still "validated" and the file exists.  
	 */
	public boolean hasXSLFile(){
		return isValidated && FILE_XSL.isFile();
	}
	
	/**
	 * @return File reference to the XSL file, but only if the instance 
	 * is still "validated" and the file exists.  Otherwise, throws an IllegalStateException.
	 * @throws IllegalStateException if the current configuration is not "validated" or the 
	 * requested file does not exist.
	 */
	public File getXSLFile() throws IllegalStateException{
		if (! (isValidated && FILE_XSL.isFile())) throw new IllegalStateException(
			"Current configuration is not valid, or the XSL File does not exist!");
		return FILE_XSL.getAbsoluteFile();
	}
	
	/**
	 * Normally only called AFTER a successful execution.
	 * @return true if the instance is still "validated" and the file exists.  
	 */
	public boolean hasXMLSummaryFile(){
		return isValidated && FILE_SUMMARY.isFile();
	}
	
	/**
	 * Normally only called AFTER a successful execution.
	 * @return File reference to the created XML Logs Summary file, but only if the instance 
	 * is still "validated" and the file exists.  Otherwise, returns null.
	 * @throws IllegalStateException if the current configuration is not "validated" or the 
	 * requested file does not exist.
	 */
	public File getXMLSummaryFile() throws IllegalStateException{
		if (! (isValidated && FILE_SUMMARY.isFile())) throw new IllegalStateException(
				"Current configuration is not valid, or the XML Summary File does not exist!");
			return FILE_SUMMARY.getAbsoluteFile();
	}

	/**
	 * Perform the end-to-end execution of creating an XML List File and XML Summary File 
	 * from the current configuration of the instance.
	 * <p>
	 * The routine will first validate() the current configuration, then create the XML List File 
	 * as necessary from a single XML log file, or all XML log files as required by the current 
	 * configuration.  This portion of the process is bypassed if the current configuration specifies 
	 * to use a pre-existing XML List File.
	 * <p>
	 * With the XML List File ready, the routine will then create an XML Summary File consolidating 
	 * all test results specified in the XML List File.  This portion of the process is bypassed if 
	 * the current configuration has the -nosummary setting set true.
	 * @throws IllegalArgumentException as may be thrown during configuration validation.
	 * @throws Exception as may be thrown by the JAXP XML transformation process. 
	 */
	public void execute()throws IllegalArgumentException{
	    try{
	    	validate();
		    if(! hasListInput()){
			    if(hasLogInput()){
			    	createSingleXMLLogList(null, null);
			    }else{
			    	createXMLLogsList(null);
			    }
		    }
    		if(hasXMLListFile()){
	    	    if(DO_SUMMARY){
	    	    	createXMLLogsSummary(FILE_LIST.getAbsolutePath(), 
	    	    			                     FILE_XSL.getAbsolutePath(),
	    	    			                     FILE_SUMMARY.getAbsolutePath());
	    	    }else{
	    	    	log("Bypassing Logs Summary creation.");
	    	    }
    		}else{
    			log("ERROR: Cannot create XML Summary file.  Invalid configuration or missing XML List file.");
    			throw new IllegalArgumentException("No valid XML List File available to process into a Summary File.");
    		}
	    }catch(IllegalArgumentException e){
	    	throw e;
	    }catch(Exception e){
	    	log("ERROR: "+ e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * primarily a testing/debug command-line entry point.
	 * Default command-line execution performs:
	 * <p>
	 * <ul><pre>
		MultiLogResults process = new MultiLogResults();
		process.processJVMOptions();
		process.processArgs(args);
		process.execute();
	 * </pre>
	 * </ul>
	 * @param args -- command-line args.
	 */
	public static void main(String[] args) {
		MultiLogResults process = new MultiLogResults();
		process.processJVMOptions();
		process.processArgs(args);
		process.execute();
	}
}
