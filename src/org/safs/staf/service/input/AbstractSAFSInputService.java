package org.safs.staf.service.input;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.safs.Log;
import org.safs.SAFSNullPointerException;
import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.StringUtils;
import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.ServiceDebugLog;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This AbstractSAFSInputService class is an external STAF service run by the JSTAF Service Proxy.<br>
 * The intention is to provide global services for reading the content of text files in a 
 * manner suitable for a SAFS Drivers(like SAFSDRIVER) and Engines. For example, two different 
 * processes (or machines?) can share the file read operations as necessary. However, there 
 * will certainly be an array of other uses.
 * <p>
 * It is important to note that in a normal SAFS environment, Input services may make use of 
 * SAFS Variable services (SAFSVARS).  This is because some commands may need to evaluate 
 * variable values or resolve expressions.
 * 
 * This class is defined as an abstract class, for different version of STAF,
 * you may extends this class and implements the staf-version-related interface
 * (STAFServiceInterfaceLevel30 or STAFServiceInterfaceLevel3).
 * We have implemented the class for version 2 and 3: SAFSInputService and SAFSInputService3
 * 
 * <p>
 * <b>The AbstractSAFSInputService service provides the following commands:</b>
 * <p>
 * <table>
 * <tr><td width="40%">
 *         OPEN               <td>Open a file for use
 * <tr><td>NEXT               <td>Get the next line from the file
 * <tr><td>GOTO               <td>Goto a defined position in the file
 * <tr><td>BEGIN              <td>Reset a file reader back to the first line
 * <tr><td>CLOSE              <td>Close and release resources on a file
 * <tr><td>QUERY              <td>Get information on a file
 * <tr><td>LIST               <td>List information for all open files
 * <tr><td>RESET              <td>Close and release all files and resources
 * <tr><td>HELP               <td>Display this help information
 * </table>
 * <p>
 * <h2>1.0 Service Registration</h2>
 * <p>
 * Each instance of the service must be registered via the STAF Service service.
 * <p>
 * Example showing comandline registration:
 * <p>
 * <pre>
 * STAF LOCAL SERVICE ADD SERVICE SAFSINPUT LIBRARY JSTAF /
 *            EXECUTE c:/safs/lib/SAFSINPUT.JAR [PARMS &lt;Parameters>]
 * 
 * STAF LOCAL SERVICE ADD SERVICE SAFSINPUT LIBRARY JSTAF /
 *            EXECUTE c:/safs/lib/SAFSINPUT.JAR 
 * 
 * STAF LOCAL SERVICE ADD SERVICE SAFSINPUT LIBRARY JSTAF /
 *            EXECUTE c:/safs/lib/SAFSINPUT.JAR PARMS DIR "c:/repo/Datapool" 
 *
 * </pre>
 * <p>
 * By default, the service expects a "SAFSVARS" SAFSVariableService to handle Variable calls.  
 * A future release may add a parameter allowing a different variable service handler.
 * <p>
 * <b>1.1</b> Valid Parameters when registering the service:
 * <p>
 * <b>1.1.1 DIR</b> &lt;default directory><br>
 * If provided, the DIR parameter specifies a default directory to use if the OPEN
 * request provides relative path information or no path information at all.  File
 * searches do not use system PATH information.  The OPEN request expects a
 * full filename path, or a path relative to this DIR option.
 * <p>
 * EX: &lt;PARMS> DIR "c:/testproject/Datapool"
 * <p>
 * <h2>2.0 Commands</h2>
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.1 OPEN </h3>
 * <p>
 * The OPEN command attempts to open a file for read operations.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * OPEN &lt;FileID> FILE &lt;Filename>
 * <p>
 * <b>2.1.1 FileID</b> is a unique ID for this file reader instance.<br>
 * <p>
 * <b>2.1.2 FILE</b> is the filename of the file to load.<br>
 * If a default Directory was specified when the service was launched, then the filename
 * can be relative to that directory.
 * <p>
 * Example: staf local safsinput open myfile file TIDTest.CDD<br>
 * Example: staf local safsinput open myfile file c:/testproject/datapool/Bench/TIDTest.txt<br>
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.2 NEXT </h3>
 * <p>
 * The NEXT command reads the next line from the file matching FileID.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * NEXT &lt;FileID> 
 * <p>
 * <b>2.2.1 FileID</b> is the unique ID for the file reader instance.<br>
 * <p>
 * Example: staf local safsinput next myfile
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.3 GOTO </h3>
 * <p>
 * The GOTO command moves the file reader pointer to a defined place in the file.<br>
 * Currently, the "defined place" must take the form of a SAFS BlockID record.  Future 
 * releases may allow you to specify specific line numbers.
 * <p>
 * A SAFS BlockID record is a line with two fields separated by a delimiter as shown below:
 * <p>
 * <pre>
 *    B, MyBlockID
 * </pre>
 * <p>
 * The first field contains "B" which specifies a BlockID record.  The second field specifies 
 * the name or label of the BlockID record.  This label is the value identifying this "defined 
 * place" in the file.
 * <p>
 * This command will use the variables service to evaluate SAFS variables and process 
 * SAFS expressions in field #1 and/or field #2 in order to accurately locate the target 
 * BlockID.
 * <p>
 * The command will return STAFResult.OK and a (linenum:record) result string if successful; or an error code 
 * and error description upon failure.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * GOTO &lt;FileID> LOCATE &lt;BlockID> SEPARATOR &lt;SepChar>
 * <p>
 * <b>2.3.1 FileID</b> is the unique ID for the file reader instance.<br>
 * <p>
 * <b>2.3.2 BlockID</b> is the target block ID label to locate.<br>
 * <p>
 * <b>2.3.3 SepChar</b> is the character that delimits the two fields.<br>
 * <p>
 * Example: staf local safsinput goto myfile locate MyBlockID separator ","
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.4 BEGIN </h3>
 * <p>
 * The BEGIN command resets the file reader matching the FileID back to the beginning of the file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * BEGIN &lt;FileID> 
 * <p>
 * <b>2.4.1 FileID</b> is the unique ID for the file reader instance.<br>
 * <p>
 * Example: staf local safsinput begin myfile
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.5 CLOSE </h3>
 * <p>
 * The CLOSE command closes the file matching FileID and releases its resources.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * CLOSE &lt;FileID> 
 * <p>
 * <b>2.5.1 FileID</b> is the unique ID for the file reader instance.<br>
 * <p>
 * Example: staf local safsinput close myfile
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.6 QUERY </h3>
 * <p>
 * The QUERY command returns information about the file matching FileID.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * QUERY &lt;FileID> [ STATUS | FILENAME | FULLPATH ]
 * <p>
 * <b>2.6.1 FileID</b> is the unique ID for the file reader instance.<br>
 * <p>
 * <b>2.6.2 STATUS</b> request status info on this file.<br>
 * <p>
 * <b>2.6.3 FILENAME</b> request the simple filename of this file.<br>
 * <p>
 * <b>2.6.4 FULLPATH</b> request the full path and filename of this file.<br>
 * <p>
 * Example: staf local safsinput query myfile STATUS FULLPATH
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.7 LIST </h3>
 * <p>
 * The LIST command returns information on ALL files.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * LIST 
 * <p>
 * Example: staf local safsinput list
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.8 RESET </h3>
 * <p>
 * The RESET command closes all files and resets all resources.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * RESET 
 * <p>
 * Example: staf local safsinput reset
 * <p><!-- ----------------------------------------------------------------------- -->
 * <h3>2.9 HELP </h3>
 * <p>
 * The HELP command returns a quick reference of supported commands and syntax.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HELP 
 * <p>
 * Example: staf local safsinput list
 * <p>
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 * @see SAFSInputService
 * @see SAFSInputService3
 * 
 * @author Carl Nagle JUL 16, 2004 Inital Release of most functionality
 * @author Carl Nagle JAN 25, 2005 Fix to accept case-sensitive filenames
 ************************************************************************************/

public abstract class AbstractSAFSInputService {
	 // OVERRIDE THESE WITH YOUR SAFSFILE TYPE AND DEFAULTS
	/****
	 * The text substring identifying the file type in each file LIST response.
	 * Subclasses are expected to override this value for differing file types.
	 ****/
	protected           String SI_SERVICE_SAFSFILE_LISTINFO    = "SAFSTextFile";

	/****
	 * "SAFSVARS" -- default service used to resolve variable expressions for GOTO
	 ****/
	public String SI_DEFAULT_RESOLVE_SERVICE = "SAFSVARS";

	/****
	 * Stores the name of the actual service used to resolve variable expressions.
	 * This is normally "SAFSVARS".
	 ****/
	protected  String servicevars = SI_DEFAULT_RESOLVE_SERVICE;

	/****
	 * The fully qualified name of the localizable ResourceBundle for this class.
	 * Subclasses are expected to provide their own additional ResourceBundle handling 
	 * file and subroutine when appropriate.
	 ****/
	public static final String SI_SERVICE_SAFSFILE_BUNDLE_NAME = "org.safs.staf.service.SAFSFileReaderResourceBundle";

	
	/****
	 * The maximum number of options/parameters we expect a service initialization request to receive.
	 * Subclasses are expected to override this value when appropriate.
	 ****/
	protected             int  SI_SERVICE_INIT_PARMS_MAX      = 2;	// DIR & EXT

	
	/****
	 * The maximum number of options/parameters we expect a running service to receive in a request.
	 * Subclasses are expected to override this value when appropriate.
	 ****/
	protected             int  SI_SERVICE_REQUEST_ARGS_MAX    = 10;  //most OPEN should see
	//=====================================================================================

	// LOAD YOUR OWN RESOURCEBUNDLE FOR LOCALIZABLE TEXT
	// ===================================================================================
	protected static ResourceBundle safsfilereader_resources = null;
	
	static {
		try{ 
			safsfilereader_resources = ResourceBundle.getBundle(SI_SERVICE_SAFSFILE_BUNDLE_NAME, Locale.getDefault(),ClassLoader.getSystemClassLoader());
			Log.info("SAFSInputService loading "+ SI_SERVICE_SAFSFILE_BUNDLE_NAME);
		}
		catch(MissingResourceException mr){
			Log.info("SAFSInputService retrying load of "+ SI_SERVICE_SAFSFILE_BUNDLE_NAME);
			try{
				safsfilereader_resources = ResourceBundle.getBundle(SI_SERVICE_SAFSFILE_BUNDLE_NAME, Locale.getDefault(),Thread.currentThread().getContextClassLoader());
				Log.info("SAFSInputService loading "+ SI_SERVICE_SAFSFILE_BUNDLE_NAME);
			}
			catch(Exception e){ 
				Log.info("SAFSInputService retry to load "+ SI_SERVICE_SAFSFILE_BUNDLE_NAME, e);
				System.err.println( e.getMessage()); }
		}
		catch(Exception e){ 
			Log.info("SAFSInputService to load "+ SI_SERVICE_SAFSFILE_BUNDLE_NAME, e);
			System.err.println( e.getMessage()); }
	}
	
	public static final String SI_RBKEY_NOT_IMPLEMENTED 	= "not_implemented";	
	public static final String SI_RBKEY_OPEN_FILES          = "open_files";	
	public static final String SI_RBKEY_UCPATH          	= "PATH";
	public static final String SI_RBKEY_SYNC_ERROR      	= "storage_sync_error";
	public static final String SI_RBKEY_REQUIRED        	= "required";
	public static final String SI_RBKEY_ERROR_NOT_INTEGER 	= "not_integer";
	// ===================================================================================
	
	public static final String SI_SERVICE_OPTION_DIR         	= "DIR";
	public static final String SI_SERVICE_OPTION_EXT         	= "EXT";

	public static final String SI_SERVICE_REQUEST_OPEN          = "OPEN";
	public static final String SI_SERVICE_REQUEST_NEXTLINE      = "NEXT";
	public static final String SI_SERVICE_REQUEST_GOTO          = "GOTO";
	public static final String SI_SERVICE_REQUEST_CLOSE         = "CLOSE";
	public static final String SI_SERVICE_REQUEST_HELP          = "HELP";
	public static final String SI_SERVICE_REQUEST_QUERY         = "QUERY";
	public static final String SI_SERVICE_REQUEST_BEGIN         = "BEGIN";
	public static final String SI_SERVICE_REQUEST_RESET         = "RESET";
	public static final String SI_SERVICE_REQUEST_LIST          = "LIST";

	public static final String SI_SERVICE_PARM_FILE             = "FILE";
	public static final String SI_SERVICE_PARM_STATUS           = "STATUS";
	public static final String SI_SERVICE_PARM_FILENAME         = "FILENAME";
	public static final String SI_SERVICE_PARM_FULLPATH         = "FULLPATH";
	public static final String SI_SERVICE_PARM_LASTERROR        = "LASTERROR";
	public static final String SI_SERVICE_PARM_LOCATE           = "LOCATE";
	public static final String SI_SERVICE_PARM_SEPARATOR        = "SEPARATOR";

	public static final String SI_SERVICE_FILE_STATE_OPEN       = "OPEN";
	public static final String SI_SERVICE_FILE_STATE_CLOSED     = "CLOSED";
	public static final String SI_SERVICE_FILE_STATE_EOF        = "EOF";
	public static final String SI_SERVICE_FILE_STATE_ERROR      = "ERROR";

 	protected STAFCommandParser parser = null;

	protected STAFCommandParser routeParser = null;
 	protected STAFCommandParser openParser  = null;
 	protected STAFCommandParser closeParser = null;
 	protected STAFCommandParser beginParser = null;
 	protected STAFCommandParser nextParser  = null;
 	protected STAFCommandParser resetParser = null;
 	protected STAFCommandParser gotoParser  = null;
 	

	protected HandleInterface fHandle;
	
//	private int handle;
	protected String servicename   = new String();
	protected String serviceparams = new String();

	protected boolean relative_path_allowed    = false;
	protected boolean file_extension_available = false;

	protected String dir = new String();
	protected String ext = new String();

	/**
	 * a single colon
	 **/
	protected static String c = ":";  // colon
	/**
	 * a single space
	 **/
	protected static String s = " ";  // space
	/**
	 * a single newline
	 **/
	protected static String r = "\n"; // newline

	//private Hashtable processes = new Hashtable(6);  //maps process names to handles (Vector)
	private Hashtable handles   = new Hashtable(6);  //maps handles to fileids (Hashtable)
	
	// After debugging the service, we should set the 2th parameter to false.
	protected ServiceDebugLog debugLog = new ServiceDebugLog(ServiceDebugLog.DEBUG_LOG_INPUT, false);


    public AbstractSAFSInputService() {}
	
    /**
     * Subclasses may override to allow for future subclasses of STAFHandle.
     * @param handleId
     * @throws STAFException
     */
    protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering STAFHandle handleId: "+ handleId);
		fHandle = new STAFHandleInterface(handleId);
    }
    
	public STAFResult init(InfoInterface.InitInfo info) {
		String debugmsg = getClass().getName() + ".init ():";
		debugLog.debugInit();
		debugLog.debugPrintln(debugmsg + " Begin.");

		try {
			servicename = info.name;
			serviceparams = info.parms;
			registerHandle("STAF/Service/" + info.name);
		} catch (STAFException e) {
			debugLog.debugTerm();
			return new STAFResult(STAFResult.STAFRegistrationError);
		}

		// handle initialization parameters:
		int rc = validateBaseServiceParseResult(info);
		if (rc != STAFResult.Ok) {
			debugLog.debugTerm();
			try {
				fHandle.unRegister();
			} catch (STAFException e) {
			}
			return new STAFResult(rc);
		}

		createParser();

		debugLog.debugPrintln(debugmsg + " End.");
		return new STAFResult(STAFResult.Ok);
	}
	
	public STAFResult acceptRequest(InfoInterface.RequestInfo info) {
		String debugmsg = getClass().getName() + ".acceptRequest():";
		debugLog.debugPrintln(debugmsg + " parsing request: "+ info.request);
		STAFCommandParseResult parsedData = parser.parse(info.request);
		if (parsedData.rc != STAFResult.Ok) {
			return new STAFResult(STAFResult.InvalidRequestString,
					parsedData.errorBuffer);
		}

		if (parsedData.optionTimes(SI_SERVICE_REQUEST_HELP) > 0) {
			return handleHelp(info);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_RESET) > 0) {
			return handleReset(info);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_NEXTLINE) > 0) {
			return handleNext(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_CLOSE) > 0) {
			return handleClose(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_OPEN) > 0) {
			return handleOpen(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_BEGIN) > 0) {
			return handleBegin(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_GOTO) > 0) {
			return handleGoto(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_QUERY) > 0) {
			return handleQuery(info, parsedData);
		} else if (parsedData.optionTimes(SI_SERVICE_REQUEST_LIST) > 0) {
			return handleList(info);
		} else {
			return processRequest(info, parsedData);
		}
	}
	  
	// p/o Interface STAFServiceInterfaceLevel1
	protected STAFResult terminate(){
		String debugmsg = getClass().getName()+".terminate(): ";
		debugLog.debugPrintln(debugmsg+" Begin.");
		
		if (!handles.isEmpty()){
			for (Enumeration e = handles.elements(); e.hasMoreElements();){
				SAFSFile textfile = (SAFSFile) e.nextElement();
				textfile.close();
			}
			handles.clear();
		}
		handles = null;
		
		debugLog.debugPrintln(debugmsg+" End.");
		debugLog.debugTerm();

		try {
			fHandle.unRegister();
		} catch (STAFException ex) {
			return new STAFResult(STAFResult.STAFRegistrationError);
		}

		return new STAFResult(STAFResult.Ok);				
	}

    // ROOT ROUTINE THAT INITIATES VALIDATING THE SERVICE INIT REQUEST
	protected  final int validateBaseServiceParseResult (InfoInterface.InitInfo info){ 
		//Create the command parser
		STAFCommandParser registrar = new STAFCommandParser(SI_SERVICE_INIT_PARMS_MAX);
		registrar.addOption(SI_SERVICE_OPTION_DIR, 1,STAFCommandParser.VALUEREQUIRED);
		registrar.addOption(SI_SERVICE_OPTION_EXT, 1,STAFCommandParser.VALUEREQUIRED);
		//Parse the initial parameters
		STAFCommandParseResult parsedData = registrar.parse(info.parms);

		dir = parsedData.optionValue(SI_SERVICE_OPTION_DIR);
		Log.info("SAFSInputService DIR parameter received: "+ dir);
	    ext = parsedData.optionValue(SI_SERVICE_OPTION_EXT);
		Log.info("SAFSInputService EXT parameter received: "+ ext);

		file_extension_available = (ext.length() > 0 );
		
		if (dir.length() > 0 ){

			File f = new CaseInsensitiveFile(dir).toFile();

			try{
				if (f.isDirectory()){
		
					relative_path_allowed = true;
					if (!dir.endsWith(File.separator)){ dir += File.separator;}
				}
				else{

					//shouldn't have to reset--we're aborted by STAF
					dir = new String();
					return STAFResult.DoesNotExist;
				}
			}catch(SecurityException e) {

				//shouldn't have to reset--we're aborted by STAF
				dir = new String();
				relative_path_allowed = false;
				Log.debug("SAFSInputService Access Denied Exception: "+ e.getClass().getSimpleName()+": "+e.getMessage());
				return STAFResult.AccessDenied;
				
			}catch(Exception x){
				dir = new String();
				relative_path_allowed = false;
				Log.debug("SAFSInputService IGNORING Exception: "+ x.getClass().getSimpleName()+": "+x.getMessage());
				return STAFResult.ServiceConfigurationError;
			}
		}
		return validateServiceParseResult ( parsedData );
	}


	protected void createParser() {
		parser = new STAFCommandParser(0, false);
		parser.addOption(SI_SERVICE_REQUEST_OPEN, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_CLOSE, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_NEXTLINE, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_GOTO, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_BEGIN, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_QUERY, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_REQUEST_RESET, 1,STAFCommandParser.VALUENOTALLOWED);
		parser.addOption(SI_SERVICE_REQUEST_LIST, 1,STAFCommandParser.VALUENOTALLOWED);
		parser.addOption(SI_SERVICE_REQUEST_HELP, 1,STAFCommandParser.VALUENOTALLOWED);

		parser.addOption(SI_SERVICE_PARM_FILE, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_PARM_LOCATE, 1,STAFCommandParser.VALUEREQUIRED);
		parser.addOption(SI_SERVICE_PARM_SEPARATOR, 1,STAFCommandParser.VALUEREQUIRED);

		parser.addOption(SI_SERVICE_PARM_STATUS, 1,STAFCommandParser.VALUENOTALLOWED);
		parser.addOption(SI_SERVICE_PARM_FILENAME, 1,STAFCommandParser.VALUENOTALLOWED);
		parser.addOption(SI_SERVICE_PARM_FULLPATH, 1,STAFCommandParser.VALUENOTALLOWED);
		parser.addOption(SI_SERVICE_PARM_LASTERROR, 1,STAFCommandParser.VALUENOTALLOWED);

		// command must contain 1 and only 1 of these requests.
		parser.addOptionGroup(SI_SERVICE_REQUEST_OPEN + s
				+ SI_SERVICE_REQUEST_NEXTLINE + s + SI_SERVICE_REQUEST_GOTO + s
				+ SI_SERVICE_REQUEST_CLOSE + s + SI_SERVICE_REQUEST_HELP + s
				+ SI_SERVICE_REQUEST_BEGIN + s + SI_SERVICE_REQUEST_QUERY + s
				+ SI_SERVICE_REQUEST_RESET + s + SI_SERVICE_REQUEST_LIST, 1, 1);

		// maximum of 1 QUERY params *if* QUERY is query is request.
		parser.addOptionGroup(SI_SERVICE_PARM_STATUS + s
				+ SI_SERVICE_PARM_FILENAME + s + SI_SERVICE_PARM_FULLPATH + s
				+ SI_SERVICE_PARM_LASTERROR, 0, 1);

		// these params ONLY valid for QUERY request.
		parser.addOptionNeed(SI_SERVICE_PARM_STATUS, SI_SERVICE_REQUEST_QUERY);
		parser.addOptionNeed(SI_SERVICE_PARM_FILENAME,SI_SERVICE_REQUEST_QUERY);
		parser.addOptionNeed(SI_SERVICE_PARM_FULLPATH,SI_SERVICE_REQUEST_QUERY);
		parser.addOptionNeed(SI_SERVICE_PARM_LASTERROR,SI_SERVICE_REQUEST_QUERY);

		// OPEN request requires FILE param.
		parser.addOptionNeed(SI_SERVICE_REQUEST_OPEN, SI_SERVICE_PARM_FILE);

		// GOTO request requires LOCATE param.
		parser.addOptionNeed(SI_SERVICE_REQUEST_GOTO, SI_SERVICE_PARM_LOCATE);
		parser.addOptionNeed(SI_SERVICE_REQUEST_GOTO,SI_SERVICE_PARM_SEPARATOR);
	}

    // ROOT ROUTINE THAT INITIATES THE BUILDING OF THE 'HELP' RESPONSE
	protected final String buildHELPInfo(){

		String helpinfo =		                                            r+
		       "OPEN <fileID> FILE <filename> "                            +r+
		                                                                    r+
		       "QUERY <fileID> [STATUS | FILENAME | FULLPATH ]"            +r+
		                                                                    r+
		       "NEXT <fileID>"                                             +r+
		                                                                    r+
		       "CLOSE <fileID>"                                            +r+
		                                                                    r+
		       "GOTO <fileID> LOCATE <locationid> SEPARATOR <fieldsep>"    +r+
		                                                                    r+
		       "BEGIN <fileID>"                                            +r+
		                                                                    r+
		       "LIST"                                                      +r+
		                                                                    r+
		       "RESET"                                                     +r+
		                                                                    r+
		       "HELP"                                                      +r;
		       
		String info = getHELPInfo(new String());
		
		if(info != null) helpinfo = info + helpinfo;
		
		return helpinfo;
	}
	
	protected STAFResult handleHelp(InfoInterface.RequestInfo info) {
		return new STAFResult(STAFResult.Ok, buildHELPInfo());
	}

	protected STAFResult handleReset(InfoInterface.RequestInfo info) {
		if (!handles.isEmpty()) {
			for (Enumeration e = handles.elements(); e.hasMoreElements();) {
				SAFSFile textfile = (SAFSFile) e.nextElement();
				textfile.close();
			}
			handles.clear();
		}

		return new STAFResult(STAFResult.Ok, SI_SERVICE_REQUEST_RESET + c+ "ALL FILE IDS AND HANDLES ARE REMOVED");
	}

	protected STAFResult handleNext(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);

		String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_NEXTLINE).toLowerCase();
		SAFSTextFile textfile = getParsedDataTextFile(result, fileid);
		if (result.rc != STAFResult.Ok)
			return result;

		result.result = textfile.next();
		if (result.result == null)
			result.result = c + SI_SERVICE_FILE_STATE_EOF + c;
		return result;
	}
	  
	protected STAFResult handleClose(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);
		
	 	String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_CLOSE).toLowerCase();
		SAFSFile textfile = getParsedDataTextFile( result, fileid );
		if (result.rc != STAFResult.Ok)  return result;

		textfile.close();
		handles.remove(fileid);

		result.result = new String();
		if (result.rc == STAFResult.Ok) result.result = "CLOSE:"+ fileid +c+ textfile.getFullpath();
		return result;
	}
	
	protected STAFResult handleOpen(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);
		
		String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_OPEN).toLowerCase();
		SAFSTextFile textfile = (SAFSTextFile) handles.get(fileid);

		// cannot dupe same fileid
		if (textfile != null) {
			result.rc = STAFResult.AlreadyExists;
			result.result = fileid +":Already Open:"+textfile.getFullpath();
			return result;
		}

		String filename = parsedData.optionValue(SI_SERVICE_PARM_FILE);
		String temp = filename;

		// try unmodified filename first
		File file = new CaseInsensitiveFile(temp).toFile();
				
		try{
			Log.info("SAFSINPUT:open trying "+ temp);
			// if fails try adding any default extension
			if ((!file.isFile())&&(file_extension_available)){

				temp += ext;
				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

			// if fails try adding any default directory path
			if ((!file.isFile())&&(relative_path_allowed)){
			
				Log.info("SAFSINPUT:open trying "+ temp);
				if (filename.startsWith(File.separator)){
					temp = dir + filename.substring(2);
				}else{
					temp = dir + filename;
				}

				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

			// last chance, add any extension along with default directory path
			if ((!file.isFile())&&(relative_path_allowed)&&(file_extension_available)){

				Log.info("SAFSINPUT:open trying "+ temp);
				temp += ext;
				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

			if(!file.isFile()){

				Log.info("SAFSINPUT:open failed.");
				result.rc = STAFResult.DoesNotExist;
				result.result = filename;
				return result;
			}
		}catch(SecurityException e) {
			Log.info("SAFSINPUT:open denied.");
			result.rc = STAFResult.AccessDenied;
			result.result = filename;
			return result;
		}

		Log.info("SAFSINPUT:opening "+ fileid +"="+ file.getPath());
		textfile = openFile ( info.machine, info.handleName, info.handle, fileid, file, parsedData);
		handles.put(fileid, textfile);

		result.result = "OPEN:"+ fileid +c+ file.getPath();
		
		return result;
	}
	
	protected STAFResult handleBegin(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);
		
	 	String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_BEGIN).toLowerCase();
		SAFSTextFile textfile = getParsedDataTextFile( result, fileid );
		if (result.rc != 0)  return result;

		textfile.begin();
		if(textfile.isClosed()){
			result.rc = STAFResult.FileOpenError;
			result.result = fileid +c+ textfile.getFullpath();
		}else{
			result.result = SI_SERVICE_REQUEST_BEGIN +c+ "1";
		}
		return result;
	}
	
	protected STAFResult handleGoto(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);
		
	 	String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_GOTO).toLowerCase();
		SAFSTextFile textfile = getParsedDataTextFile( result, fileid );
		if (result.rc != 0)  return result;
		
		String location = parsedData.optionValue(SI_SERVICE_PARM_LOCATE);
		String fieldsep = parsedData.optionValue(SI_SERVICE_PARM_SEPARATOR);

		locateBlockID(result, textfile, location, fieldsep);
		return result;
	}
	
	protected STAFResult handleQuery(InfoInterface.RequestInfo info,STAFCommandParseResult parsedData) {
		STAFResult result = new STAFResult(STAFResult.Ok);
		
	 	String fileid = parsedData.optionValue(SI_SERVICE_REQUEST_QUERY).toLowerCase();
		SAFSTextFile textfile = getParsedDataTextFile( result, fileid );
		if (result.rc != STAFResult.Ok)  return result;
		
		if(parsedData.optionTimes(SI_SERVICE_PARM_STATUS)>0){
			StringBuffer sb = new StringBuffer();
			sb.append("Machine: \t"+textfile.getMachine()+"\n");
			sb.append("FileID: \t"+textfile.getFileID()+"\n");
			sb.append("FullPath: \t"+textfile.getFullpath()+"\n");
			sb.append("CurrentLine: \t"+textfile.getLineNumber()+"\n");
//			sb.append("Mark: \t\t"+textfile.getMark()+"\n");
			result.result = sb.toString();
		}else if(parsedData.optionTimes(SI_SERVICE_PARM_FILENAME)>0){
			result.result = textfile.getFilename();
		}else if(parsedData.optionTimes(SI_SERVICE_PARM_FULLPATH)>0){
			result.result = textfile.getFullpath();			
		}else if(parsedData.optionTimes(SI_SERVICE_PARM_LASTERROR)>0){
			//Not implemented
		}else{
			result.result = "UNKNOWN OPTION. "+" Option should be STATUS or FILENAME or FULLPATH ";
		}

		return result;
	}
	
	protected STAFResult handleList(InfoInterface.RequestInfo info) {
		if(handles.isEmpty()){
			return new STAFResult(STAFResult.Ok, SI_SERVICE_REQUEST_LIST + c+ " NO FILE EXIST.");
		}else{
			StringBuffer sb = new StringBuffer();
			SAFSFile textfile = null;
			for (Enumeration e = handles.elements(); e.hasMoreElements();) {
				textfile = (SAFSFile) e.nextElement();
				sb.append(textfile.getFileID()+"\t: \t"+textfile.getFullpath()+"\n");
			}
			return new STAFResult(STAFResult.Ok, sb.toString());
		}
	}
    /**************************************************************
	 * Use to validate the Service initialization string after a request 
	 * for initialization has been received.
	 * <p>
	 * This is where you check to see which options exist in the request, 
	 * and act upon them.  Make sure you also forward the validation 
	 * request up the food chain so the superclasses can validate their 
	 * service init options, too.
	 * <p>
	 * &nbsp; &nbsp; return super.validateServiceParseResult( parsedData );<br>
	 * <p>
	 * @param parsedData the result object after parsing was performed
	 **************************************************************/
	protected int validateServiceParseResult ( STAFCommandParseResult parsedData ){
		// ADD YOUR SUBCLASS SERVICE INIT VALIDATION HERE
		return STAFResult.Ok;
	}
	
    /**************************************************************
	 * A subclass must override this function to handle requests not 
	 * handled by the superclass.  The subclass must also forward any 
	 * unhandled requests to the superclass function.
	 * <p>
	 * &nbsp; &nbsp; return super.processRequest(info, parsedData);<br>
	 * <p>
	 * @param info		InfoInterface.RequestInfo, it contains: <br>
	 * machine the machine initiating the request.<br>
	 * process the name of the Process initiating the request.<br>
	 * handle the Handle of the Process initiating the request.<br>
	 * 
	 * @param parsedData the parsed result containing the command and the 
	 *                   user-supplied options provided.
	 * <p>
	 * @return the result object after processing the request.
	 **************************************************************/
	protected STAFResult processRequest (InfoInterface.RequestInfo info, STAFCommandParseResult parseData) {
		String debugmsg = getClass().getName() + ".processRequest(): ";
		debugLog.debugPrintln(debugmsg + " unhandled request: "+ info.request);
		STAFResult result = new STAFResult(STAFResult.InvalidRequestString,"Unknown Request: " + info.request);
		return result;
	}
	
	
	
	
	

    /*******************************************************************************************
	 * Retrieves potentially localized text from the resource bundle
	 * 
	 * Localized subclasses should provide a similar function or override this function. If the 
	 * subclass function fails to locate the message in the subclass's ResourceBundle, then the 
	 * subclass should call the superclass function to look there.
	 * <p>
	 * &nbsp; &nbsp; text = super.text(resourceKey);
	 * <p>
	 * @param resourceKey the key name of the message to retrieve.
	 * <p>
	 * @return the localized text or a localization error string
	 ******************************************************************************************/
	public String text(String resourcekey){
		String resource = new String("NO LOCALIZED TEXT");
		try{ resource = safsfilereader_resources.getString(resourcekey);}
		catch(Exception e){
			Log.debug("SAFSInputService ignoring Exception: "+ ext.getClass().getSimpleName()+": "+ e.getMessage());
			System.err.println( e.getMessage());
		}
		return resource;
	}

    /**************************************************************
	 * Handle any added OPEN options here.
	 * This root class has already added info to the string prior 
	 * to the call into the subclass.  You may still forward the 
	 * request to any superclass "just in case".
	 * <p>
	 * &nbsp; &nbsp; return super.getOPENInfo( parsedData, info );<br>
	 * <p>
	 * @param parsedData the parsed request to interrogate for OPEN option 
	 *        parameter values.
	 * <p>
	 * @param info the status string that will be displayed for the 
	 *             OPEN command.
	 * <p>
	 * @return any text to be added to the OPEN response.
	 **************************************************************/
	protected String  getOPENInfo   (STAFCommandParseResult parsedData, String info){
		return (info == null) ? (new String()): info;
	}

    /**************************************************************
	 * Add your HELP text response here.
	 * The root class will handle its own HELP substring.  You will still forward the 
	 * request to any superclass to have their HELP options displayed, too.
	 * <p>
	 * &nbsp; &nbsp; info += MY_LONG_INVOLVED_FORMATTED_HELP_TEXT;<br>
	 * &nbsp; &nbsp; return super.getHELPInfo( info );<br>
	 * <p>
	 * @param info the response string that will be displayed for the 
	 *             HELP command.
	 * <p>
	 * @return any text to be prepend the superclass response.
	 **************************************************************/
	protected String getHELPInfo (String info ){
		// ADD YOUR SUBCLASS HELP COMMAND TEXT HERE
		return (info == null) ? (new String()): info;
	}
	
	
    /**************************************************************
	 * A subclass must override this function to instantiate its own file type.
	 * The file type, obviously, must be a extension of SAFSFile.
	 * <p>
	 * @param machine the machine initiating the request to open the file.
	 * <p>
	 * @param process the name of the Process initiating the request to open the file.
	 * <p>
	 * @param handle the Handle of the Process initiating the request to open the file.
	 * <p>
	 * @param fileid the unique fileid to be assigned the file.  fileids should be 
	 *               uniquely identifiable among all Handles for a given Process.
	 * <p>
	 * @param file the File object used to open the file.
	 * <p>
	 * @param parsedData the parsed result containing the OPEN command and any 
	 *                   of the user-supplied options that may be needed when 
	 *                   opening the file or setting options in the file handler.
	 * <p>
	 * @return the instance of the SAFSFile object that has been created.
	 **************************************************************/
	protected SAFSTextFile openFile (String machine, String process, int handle,
	                             String fileid , File file, STAFCommandParseResult parsedData){

		return new SAFSTextFile ( machine, process, handle, fileid, file, 
		                          false,		// skipblanklines
		                          false, 		// nolinenumbers
	                 			  false,		// trimleading
	                 			  false, 		// trimtrailing
	                 			  false, 		// trimwhitespace
	                 			  null);		// Vector commentids
	}

	protected String processExpression(String expression){
		//fHandle = STAFHandle
		try{
			long len = expression.length();
			String message = "RESOLVE "+ ":"+ String.valueOf(len).trim() +":"+ expression;
			
			// TODO: if STAFHandle is not an EmbeddedHandle then it will not seek embedded services via EmbeddedHandles class.
			// Thus, it will not see SAFSVARS if it is running Embedded.
			STAFResult varresult = fHandle.submit2("local", servicevars, message);
			if (varresult.rc == STAFResult.Ok) return varresult.result;		
		}
		catch(Exception x){;}
		return expression;
	}

	
	//returns True if a record matches the given block ID
	// inputBlockID  the BlockID we are trying to match
	// inputRecord   the test record to test. this should be the raw record before
	//               variable/expression substitution
	// delim         the delimiter of the inputRecord
	protected boolean isTargetBlock (String inputBlockID, String inputRecord, String fieldsep){

		String rtype = null;
		String blockID = null;
		
	    //Get and process the RECORD TYPE (this field could be a variable/expression)
	    //we don't want to process the whole record unless its record type is "B".
	    //otherwise, we might accidentally perform variable assignments that might
	    //appear on the record we are testing.
	    try{
	    	rtype = StringUtils.getInputToken(inputRecord, 0, fieldsep).trim();

		    if (! rtype.equalsIgnoreCase(DriverConstant.RECTYPE_B))
		        rtype = processExpression (rtype);	
		        
		    //only deal with BLOCKID record 
		    if (! rtype.equalsIgnoreCase(DriverConstant.RECTYPE_B)) return false;
		
		    blockID = StringUtils.getInputToken(inputRecord, 1, fieldsep).trim();
			
			//Log.debug("isTargetBlock check:\""+ rtype +"\"  \""+ blockID +"\"");
			
		    if (blockID.equalsIgnoreCase(inputBlockID)) return true;
		
		    blockID = processExpression (blockID);
		    if (blockID.equalsIgnoreCase(inputBlockID)) return true;	    
		}
	    catch(SAFSNullPointerException npx) {;}
	    catch(StringIndexOutOfBoundsException six) {;}
		return false;
	}

	// attempt to locate the blockId in the current table.
	// if found the file pointer will be set such that the blockID is the next line to execute.
	// if not found, the file pointer will be such that the next line in the table will execute.
	// the routine will log all failures and increment GeneralFailure if the block is not found.
	protected void locateBlockID (STAFResult result, SAFSTextFile file, String inputBlockID, String fieldsep){

	    String inputRecord = null;
	    String rtype;
		String fileID = file.getFileID();
		
		// don't process a closed file
		if (file.isClosed()) {
			result.rc = STAFResult.FileReadError;
			result.result = fileID +" is CLOSED.";
			return;
		}

		// store/mark where the file pointer is on entry
        long entryline  = file.getLineNumber();
        file.mark();
        
        boolean blockFound = false;
        long currpos = -1;

		// inputBlockID may be an expression, so resolve it
		inputBlockID = processExpression(inputBlockID) ;

		// look from here to EOF
        while(! file.isEOF()){
            
            // Get next record and trim any leading spaces    
            currpos = file.getLineNumber();
            inputRecord = file.readLine();
			if (inputRecord == null) break;
            if (isTargetBlock(inputBlockID, inputRecord, fieldsep)){
                blockFound = true;
                break;
            }            
        }

		// if not found, try again from the beginning of the file        
        if (! blockFound) {

			file.begin();	

			// look from beginning to entryline
	        while(! file.isEOF()){
	            
	            //Get next record and trim any leading spaces    
	            currpos = file.getLineNumber();
	            if (currpos == entryline) break;
	            inputRecord = file.readLine();
				if (inputRecord == null) break;
	            if (isTargetBlock(inputBlockID, inputRecord, fieldsep)){
	                blockFound = true;
	                break;
	            }            
	        }        
        }

        if (blockFound) {
        	result.rc = STAFResult.Ok;
        	result.result = (String.valueOf(currpos +1)).trim() +":"+inputRecord;
        	file.setMark(currpos);
        }
        else{
        	result.rc = STAFResult.InvalidValue;
        	result.result = "Block "+ inputBlockID +" not found in "+ fileID;
        }

        file.reset();
	}

	protected final SAFSTextFile getParsedDataTextFile( STAFResult result, String fileid){

		if (handles.isEmpty()){
			result.rc     = STAFResult.DoesNotExist;
			result.result = "0 "+ text(SI_RBKEY_OPEN_FILES);
			return null;}

		if (! handles.containsKey(fileid)){
			result.rc     = STAFResult.DoesNotExist;
			result.result = "FILEID:"+fileid+c+"Never Opened";
			return null;}

		return (SAFSTextFile) handles.get(fileid);
	}
}
