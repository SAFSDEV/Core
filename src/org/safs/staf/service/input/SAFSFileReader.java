package org.safs.staf.service.input;

import com.ibm.staf.*;
import com.ibm.staf.service.*;
import java.io.*;
import java.util.*;

import org.safs.Log;
import org.safs.tools.*;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSFileReader class is our base class for external STAF File reading services.<br>
 * This service creates and maintains collections of SAFSFile objects. These represent open files.
 * <p>
 * The intention is to provide shared file services for opening files and sharing the file
 * contents across the system.  For example, one process can open the file, and then any number
 * of other processes can read the contents.
 * <p>
 * The SAFSFileReader provides basic implementations for the following commands:<br>
 * <ul>
 * <li>OPEN - open a text file for read operations
 * <li>READL - retrieve the next available line in the file
 * <li>QUERY - retrieve status information from the file
 * <li>CLOSE - close and release resources on the text file
 * <li>LIST - list open information for the service instance
 * <li>HELP - returns syntax information
 * </ul>
 * <h2>1.0 SAFSFileReader Service Registration</h2>
 * <p>
 * Each instance of the service must be registered via the STAF Service service.
 * <p>
 * Example showing comandline registration:
 * <p><pre>
 * STAF LOCAL SERVICE ADD SERVICE &lt;servicename> LIBRARY JSTAF /
 *            EXECUTE org/safs/staf/service/SAFSFileReader [PARMS &lt;Parameters>]
 *
 * Other examples:
 *
 * SERVICE ADD SERVICE FileReader  LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSFileReader
 *
 * SERVICE ADD SERVICE StepReader  LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSFileReader PARMS DIR c:\repo\Datapool EXT ".sdd"
 *
 * SERVICE ADD SERVICE BenchReader LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSFileReader PARMS DIR "c:\new repo\Datapool\Bench"
 *
 * </pre>
 * <p>
 * <b>1.1</b> Valid Parameters when registering a SAFSFileReader service:
 * <p>
 * <b>1.1.1 DIR &lt;default directory></b><br>
 * If provided, the DIR parameter specifies a default directory to use if the OPEN
 * request provides relative path information or no path information at all.  File
 * searches will not use environment PATH information.  The OPEN request expects a
 * full filename path, or a path relative to this DIR option.
 * <p>
 * EX: PARMS DIR "c:\testrepo\Datapool"
 * <p>
 * If the DIR parameter is not provided, then OPEN requests will not attempt
 * relative path searches.  The filename provided to the OPEN request must then be
 * an exact full filepath match or the OPEN request will fail.
 * <p>
 * <b>1.1.2 EXT &lt;default file extension></b><br>
 * If provided, the EXT parameter specifies a default file extension (suffix) to try if
 * the OPEN request does not find the file as provided.  You must include any period
 * (.) if it is to be part of any appended suffix.
 * <p>
 * EX: PARMS EXT .txt
 * <p>
 * If the EXT parameter is not provided, then OPEN requests will not attempt the additional
 * file search with the appended extension if the provided filename was not found.  The
 * filename provided to the OPEN request must then be an exact filename.ext match or
 * the OPEN request will fail.
 * <p>
 * <h2>2.0 SAFSFileReader Commands</h2>
 * <p>
 * Note, there must be a uniquely qualified match for NAME/ID combinations listed below.  
 * If the same Process has opened two different files with the same ID from 2 different 
 * Handles, then you will have to use HANDLE, instead of NAME.
 * <p>
 * <h3>2.1 OPEN </h3>
 * <p>
 * The OPEN command attempts to open a file for read operations.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * OPEN ID &lt; FileID > FILE &lt; Filename > /<br>
 * <p>
 * <b>2.1.1</b> ID is a unique name for this file handling instance.<br>
 * <p>
 * <b>2.1.2</b> FILE is the filename of the file to open.<br>
 * If a default Directory was specified when the service was launched, then the filename
 * can be relative to that directory.  Otherwise, the full filepath must be specified.<br>
 * If a default Extension (Ext) was specified when the service was launched, then the
 * filename can be specified without the extension.<br>
 * <p>
 * <h3>2.2 READL </h3>
 * <p>
 * The READL command simply returns the next line of text from the file.<br>
 * No special processing or handling happens on the text.  If we have reached 
 * the end of file, the request returns ":EOF:".
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > READL
 * <p>
 * <b>2.2.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.2.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.2.3</b> ID is the unique fileID that was provided when the file was OPENed.
 * <p>
 * <h3>2.3 CLOSE </h3>
 * <p>
 * The CLOSE command closes and releases resources for the file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > CLOSE
 * <p>
 * <b>2.3.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.3 QUERY </h3>
 * <p>
 * The QUERY command returns requested information about an open file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > QUERY
 * &lt STATUS | FILENAME | FULLPATH | LASTERROR >
 * <p>
 * <b>2.3.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <b>2.3.4</b> STATUS returns one of "OPEN", "CLOSED", or "EOF".  If the "CLOSED"
 * status is returned, that generally means some type of critical failure has occurred.
 * A file that is at EOF is still open.  However, this basic reader does not support 
 * moving the file pointer backwards.  So, essentially, the object is not useful at 
 * this point.
 * <p>
 * <b>2.3.5</b> FILENAME is the short filename of the file without any path information.  
 * Due to the OPEN parameters DIR and EXT, the filename of the file may be different than 
 * any relative path information that was provided to the OPEN command.
 * <p>
 * <b>2.3.6</b> FULLPATH is the full path to the file.  Due to the OPEN parameters DIR and
 * EXT, the full path to the file may be different than any relative path information that
 * was provided to the OPEN command.
 * <p>
 * <b>2.3.7</b> LASTERROR is not yet implemented.
 * <p>
 * <h3>2.4 LIST </h3>
 * <p>
 * The LIST command returns info on each open file for the service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] LIST
 * <p>
 * <b>2.4.1</b> HANDLE specifies the handle of the process to query.
 * You can instead provide the NAME of the process to query. If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.4.2</b> NAME specifies the name of the process to query.
 * You can instead provide the HANDLE of the process to query.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <h3>2.5 HELP </h3>
 * <p>
 * The HELP command returns syntax information for the SAFSFileReader service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HELP
 * <p>
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 *********************************************************************************************/
public class SAFSFileReader implements STAFServiceInterfaceLevel1 {

	// OVERRIDE THESE WITH YOUR SAFSFILE TYPE AND DEFAULTS
	/****
	 * The text substring identifying the file type in each file LIST response.
	 * Subclasses are expected to override this value for differing file types.
	 ****/
	protected           String SFR_SERVICE_SAFSFILE_LISTINFO    = "SAFSFile";


	/****
	 * The fully qualified name of the localizable ResourceBundle for this class.
	 * Subclasses are expected to provide their own additional ResourceBundle handling 
	 * file and subroutine when appropriate.
	 ****/
	public static final String SFR_SERVICE_SAFSFILE_BUNDLE_NAME = "org.safs.staf.service.SAFSFileReaderResourceBundle";

	
	/****
	 * The maximum number of options/parameters we expect a service initialization request to receive.
	 * Subclasses are expected to override this value when appropriate.
	 ****/
	protected             int  SFR_SERVICE_INIT_PARMS_MAX      = 2;	// DIR & EXT

	
	/****
	 * The maximum number of options/parameters we expect a running service to receive in a request.
	 * Subclasses are expected to override this value when appropriate.
	 ****/
	protected             int  SFR_SERVICE_REQUEST_ARGS_MAX    = 10;  //most OPEN should see
	//=====================================================================================

	// LOAD YOUR OWN RESOURCEBUNDLE FOR LOCALIZABLE TEXT
	// ===================================================================================
	protected static ResourceBundle safsfilereader_resources = null;
	
	static {
		try{ 
			safsfilereader_resources = ResourceBundle.getBundle(SFR_SERVICE_SAFSFILE_BUNDLE_NAME, Locale.getDefault(),ClassLoader.getSystemClassLoader());
			Log.info("SAFSFileReader loading "+ SFR_SERVICE_SAFSFILE_BUNDLE_NAME);
		}
		catch(MissingResourceException mr){ 
			Log.info("SAFSFileReader retrying load of "+ SFR_SERVICE_SAFSFILE_BUNDLE_NAME);
			try{
				safsfilereader_resources = ResourceBundle.getBundle(SFR_SERVICE_SAFSFILE_BUNDLE_NAME, Locale.getDefault(),Thread.currentThread().getContextClassLoader()); 
				Log.info("SAFSFileReader loading "+ SFR_SERVICE_SAFSFILE_BUNDLE_NAME);
			}
			catch(Exception e){ 
				System.err.println( e.getMessage()); 
				Log.info("SAFSFileReader retry failed to load "+ SFR_SERVICE_SAFSFILE_BUNDLE_NAME, e);
			}
	    }
		catch(Exception e){ 
			System.err.println( e.getMessage()); 
			Log.info("SAFSFileReader failed to load "+ SFR_SERVICE_SAFSFILE_BUNDLE_NAME, e);
		}
	}
	
	public static final String SFR_RBKEY_NOT_IMPLEMENTED = "not_implemented";	
	public static final String SFR_RBKEY_OPEN_FILES      = "open_files";	
	public static final String SFR_RBKEY_UCPATH          = "PATH";
	public static final String SFR_RBKEY_SYNC_ERROR      = "storage_sync_error";
	public static final String SFR_RBKEY_REQUIRED        = "required";
	public static final String SFR_RBKEY_ERROR_NOT_INTEGER = "not_integer";
	// ===================================================================================
	
	public static final String SFR_SERVICE_OPTION_DIR          = "DIR";
	public static final String SFR_SERVICE_OPTION_EXT          = "EXT";

	public static final String SFR_SERVICE_REQUEST_OPEN        = "OPEN";
	public static final String SFR_SERVICE_REQUEST_READL       = "READL";
	public static final String SFR_SERVICE_REQUEST_QUERY       = "QUERY";
	public static final String SFR_SERVICE_REQUEST_CLOSE       = "CLOSE";
	public static final String SFR_SERVICE_REQUEST_LIST        = "LIST";
	public static final String SFR_SERVICE_REQUEST_HELP        = "HELP";

	public static final String SFR_SERVICE_PARM_HANDLE         = "HANDLE";
	public static final String SFR_SERVICE_PARM_NAME           = "NAME";
	public static final String SFR_SERVICE_PARM_ID             = "ID";
	public static final String SFR_SERVICE_PARM_FILE           = "FILE";

	// parms for QUERY
	public static final String SFR_SERVICE_PARM_STATUS         = "STATUS";
	public static final String SFR_SERVICE_PARM_FILENAME       = "FILENAME";
	public static final String SFR_SERVICE_PARM_FULLPATH       = "FULLPATH";
	public static final String SFR_SERVICE_PARM_LASTERROR      = "LASTERROR";

	public static final String SFR_SERVICE_FILE_STATE_OPEN     = "OPEN";
	public static final String SFR_SERVICE_FILE_STATE_CLOSED   = "CLOSED";
	public static final String SFR_SERVICE_FILE_STATE_EOF      = "EOF";
	public static final String SFR_SERVICE_FILE_STATE_ERROR    = "ERROR";

 	protected STAFCommandParser parser = null;

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

	private Hashtable processes = new Hashtable(4);  //maps process names to handles (Vector)
	private Hashtable handles   = new Hashtable(4);  //maps handles to fileids (Hashtable)


	/*******************************************************************************************
	 * The constructor used by the SAFSFileReader.
	 * 
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super();
	 * <p>
	 * This constructor will initialize the STAFCommandParser that will be used for all requests 
	 * sent to the service.
	 ******************************************************************************************/
	public SAFSFileReader (){
		
		// ALL SUBCLASSES MUST CALL THE SUPERCLASS CONSTRUCTOR		
		// super();
		
 		if (parser == null) {
 			parser = new STAFCommandParser(SFR_SERVICE_REQUEST_ARGS_MAX);

 			addBaseCommandOptions         ( parser );
 			buildBaseCommandList          ( parser );
 			addBaseCommandOptionGroups    ( parser );
			addBaseCommandOptionNeeds     ( parser );
			buildBaseQueryCommandList     ( parser );
 		}
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
		catch(Exception e){ System.err.println( e.getMessage());}
		return resource;
	}


	/**************************************************************
	 * Add your own command options in the STAFCommandParser.
	 * The format of the call is the OPTION string, the maximum times 
	 * it can appear in a single request, and whether it is standalone, or whether an 
	 * associated option value is required, or optional.
	 * <p>
	 * &nbsp; &nbsp; aparser.addOption(A_COMMAND_CONSTANT, 1, STAFCommandParser.VALUENOTALLOWED);<br>
	 * &nbsp; &nbsp; aparser.addOption(A_COMMAND_CONSTANT, 2, STAFCommandParser.VALUEALLOWED);<br>
	 * &nbsp; &nbsp; aparser.addOption(A_COMMAND_OPTION,   1, STAFCommandParser.VALUEREQUIRED);<br>
	 * &nbsp; &nbsp; aparser.addOption(A_COMMAND_OPTION,   9, STAFCommandParser.VALUEALLOWED);<br>
	 * &nbsp; &nbsp; super.addCommandOptions( aparser );
	 * <p>
	 * @param aparser the instance of the parser that will handle requests.
	 **************************************************************/
	protected void addCommandOptions ( STAFCommandParser aparser ) {
		// ADD YOUR SUBCLASS COMMAND OPTIONS HERE
	}


	/**************************************************************
	 * Add your primary command options to the list of mutually exclusive commands.  
	 * You must feed these up the superclass chain to enable all superclass 
	 * commands, too.
	 * <p>
	 * &nbsp; &nbsp; requestoptions += s+ COMMAND1 +s+ COMMAND2 +s+ COMMAND3 +s;<br>
	 * &nbsp; &nbsp; return super.buildCommandList( requestoptions );<br>
	 * <p>
	 * @param requestoptions the space-delimited list of commands
	 **************************************************************/
	protected String buildCommandList(String requestoptions){
		// ADD YOUR SUBCLASS REQUEST OPTION GROUP 
		return requestoptions;
	}


	/**************************************************************
	 * Add command option groups as needed.  These are often command keywords 
	 * that are optional--grouped as one, or the other, but only a certain number (or 1)
	 * of the options is valid for any given command.
	 * <p>
	 * &nbsp; &nbsp; options = THIS_ONE +s+ THAT_ONE +s+ ANOTHER_ONE;<br>
	 * &nbsp; &nbsp; aparser.addOptionGroups( options, 0,1 );<br>
	 * &nbsp; &nbsp; super.addCommandOptionGroups( aparser );<br>
	 * <p>
	 * The example shows 3 options, only one may be specified at a time, none are required.
	 * <p>
	 * @param aparser the parser instance that will process incoming requests.
	 **************************************************************/
	protected void addCommandOptionGroups(STAFCommandParser aparser){
		// ADD YOUR SUBCLASS PARAMETER OPTION GROUPS HERE
	}


	/**************************************************************
	 * Add command option "requirements.  This is where you specify which options 
	 * or items MUST appear together.  For example, an OPEN command requires 
	 * that the user specifies which FILE to open along with the ID 
	 * to give the new file.
	 * <p>
	 * &nbsp; &nbsp; aparser.addOptionNeeds( OPEN_COMMAND, FILE_OPTION );<br>
	 * &nbsp; &nbsp; aparser.addOptionNeeds( OPEN_COMMAND, ID_OPTION );<br>
	 * &nbsp; &nbsp; super.addCommandOptionNeeds( aparser );<br>
	 * <p>
	 * The example shows that the OPEN command needs a FILE specified, 
	 * and an ID specified.
	 * <p>
	 * @param aparser the parser instance that will process incoming requests.
	 **************************************************************/
	protected void addCommandOptionNeeds(STAFCommandParser aparser){
		// ADD YOUR SUBCLASS REQUEST OPTION NEEDS HERE
	}


	/**************************************************************
	 * Like primary commands, the QUERY command has a list of mutually 
	 * exclusive options.  You must feed these up the chain so that 
	 * all superclass QUERY options are made available, too.
	 * <p>
	 * &nbsp; &nbsp; queryoptions += s+ STATUS +s+ FILENAME +s+ FULLPATH +s;<br>
	 * &nbsp; &nbsp; return super.buildQueryCommandList( queryoptions );<br>
	 * <p>
	 * @param queryoptions the space-delimited list of options
	 **************************************************************/
	protected String buildQueryCommandList(String queryoptions){
		// ADD YOUR SUBCLASS PARAMETER OPTION GROUPS HERE
		return queryoptions;
	}


	/**************************************************************
	 * Add your initialization options for your service.  
	 * These options are like the command options, but these are handled by a different 
	 * parser.  Define what the valid options are for creating the 
	 * service.
	 * <p>
	 * The format of the call is the OPTION string, the maximum times 
	 * it can appear in a single request, and whether it is standalone, or whether an 
	 * associated option value is required, or optional.
	 * <p>
	 * &nbsp; &nbsp; aparser.addOption(A_SERCICE_OPTION, 3, STAFCommandParser.VALUENOTALLOWED);<br>
	 * &nbsp; &nbsp; aparser.addOption(A_SERVICE_OPTION, 1, STAFCommandParser.VALUEREQUIRED);<br>
	 * &nbsp; &nbsp; super.addServiceInitOptions( registrar );
	 * <p>
	 * @param registrar the parser handling the service init request.
	 **************************************************************/
	protected void addServiceInitOptions ( STAFCommandParser registrar ){	
		// ADD YOUR SUBCLASS SERVICE STARTUP OPTIONS HERE
	}


	/**************************************************************
	 * Use to validate the Service initialization string after a request 
	 * for initialization has beenr received.
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
	 * Provide STATUS information for the QUERY STATUS command.
	 * The subclass can also call the superclass for more information,
	 * but this is not required.  This root class has already added 
	 * info to the string prior to the call into the subclass.
	 * <p>
	 * &nbsp; &nbsp; return super.getSTATUSInfo( textfile, info );<br>
	 * <p>
	 * @param textfile the instance of the SAFSFile object to play with.
	 * <p>
	 * @param info the status string that will be displayed for the 
	 *             QUERY STATUS command.
	 * <p>
	 * @return the text to be displayed by the QUERY STATUS command.
	 **************************************************************/
	protected String  getSTATUSInfo (SAFSFile textfile, String info){
		// OVERRIDE THIS IN YOUR SUBCLASS
		return (info == null) ? (new String()): info;
	}


	/**************************************************************
	 * Handle any added QUERY options here.
	 * This root class has already added info to the string prior 
	 * to the call into the subclass.  You may still forward the 
	 * request to any superclass "just in case".
	 * <p>
	 * &nbsp; &nbsp; return super.getQUERYInfo( textfile, parsedData, info );<br>
	 * <p>
	 * @param textfile the instance of the SAFSFile object to play with.
	 * <p>
	 * @param parsedData the parsed request to interrogate for QUERY option 
	 *        parameter values.
	 * <p>
	 * @param info the status string that will be displayed for the 
	 *             QUERY STATUS command.
	 * <p>
	 * @return the text to be displayed by the QUERY STATUS command.
	 **************************************************************/
	protected String  getQUERYInfo  (SAFSFile               textfile, 
	                                 STAFCommandParseResult parsedData,
	                                 String                 info       ){
		return (info == null) ? (new String()): info;
	}
									  

	/**************************************************************
	 * Override this function and return your version of SFR_SERVICE_SAFSFILE_LISTINFO.
	 * You only have to call the superclass if you are not overriding the 
	 * value for some reason.
	 * <p>
	 * @return the text substring to be inserted with the LIST response 
	 *         for each file listed.
	 **************************************************************/
	protected String  getLISTInfo  (){ 	return SFR_SERVICE_SAFSFILE_LISTINFO; }
									  

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
	protected SAFSFile openFile (String machine, String process, int handle,
	                             String fileid , File file, STAFCommandParseResult parsedData){

		return new SAFSFile ( machine, process, handle, fileid, file);
	}

	
	/**************************************************************
	 * A subclass must override this function to handle requests not 
	 * handled by the superclass.  The subclass must also forward any 
	 * unhandled requests to the superclass function.
	 * <p>
	 * &nbsp; &nbsp; return super.processRequest(result, machine, process, handle, parsedData);<br>
	 * <p>
	 * @param result the object containing the return code and response 
	 *               string for the request.
	 * <p>
	 * @param machine the machine initiating the request.
	 * <p>
	 * @param process the name of the Process initiating the request.
	 * <p>
	 * @param handle the Handle of the Process initiating the request.
	 * <p>
	 * @param parsedData the parsed result containing the command and the 
	 *                   user-supplied options provided.
	 * <p>
	 * @return the result object after processing the request.
	 **************************************************************/
	protected STAFResult processRequest (STAFResult result, String machine, String process,
	                           		     int handle, STAFCommandParseResult parseData) {

		result.rc     = STAFResult.InvalidRequestString;
		result.result = new String();
		return result;
	}


	/**************************************************************
	 * perform final termination cleanup operations for your service.
	 * The service is being shutdown by STAF.  You must also forward 
	 * this shutdown request to your superclass.
	 * <p>
	 * &nbsp; &nbsp; return super.shutdown();<br>
	 * <p>
	 **************************************************************/
	protected int shutdown ( ){
		// ADD YOUR SUBCLASS SERVICE SHUTDOWN CODE HERE
		return STAFResult.Ok;
	}


	// ROOT ROUTINE THAT INITIATES THE BUILDING OF THE 'HELP' RESPONSE
	protected final String buildHELPInfo(){

		String helpinfo =
		
		       r+
		       "OPEN  ID <fileID> FILE <filename> "              +r+
		                                                          r+
		       "[HANDLE <handle> | NAME <process>] ID <fileID> /"+r+
		       "      QUERY <STATUS | FILENAME | FULLPATH | LASTERROR>"     +r+
		                                                          r+
		       "[HANDLE <handle> | NAME <process>] ID <fileID> /"+r+
		       "      READL | CLOSE"                             +r+
		                                                          r+
		       "[HANDLE <handle> | NAME <process>] LIST"         +r+
		                                                          r+
		       "HELP  (you are here)"                            +r;
		       
		String info = getHELPInfo(new String());
		
		if(info != null) helpinfo = info + helpinfo;
		
		return helpinfo;
	}


	// ROOT ROUTINE THAT INITIATES THE ADDING OF COMMAND OPTIONS
	protected final void addBaseCommandOptions(STAFCommandParser aparser){
		
		aparser.addOption( SFR_SERVICE_REQUEST_OPEN   , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_READL  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_QUERY  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_CLOSE  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_LIST   , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_HELP   , 1, STAFCommandParser.VALUENOTALLOWED );

		aparser.addOption( SFR_SERVICE_PARM_HANDLE    , 1, STAFCommandParser.VALUEREQUIRED   );
		aparser.addOption( SFR_SERVICE_PARM_NAME      , 1, STAFCommandParser.VALUEREQUIRED   );
		aparser.addOption( SFR_SERVICE_PARM_ID        , 1, STAFCommandParser.VALUEREQUIRED   );
		aparser.addOption( SFR_SERVICE_PARM_FILE      , 1, STAFCommandParser.VALUEREQUIRED   );

		aparser.addOption( SFR_SERVICE_PARM_STATUS    , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_FILENAME  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_FULLPATH  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_LASTERROR , 1, STAFCommandParser.VALUENOTALLOWED );
		
		addCommandOptions ( aparser );
	}
	

	// ROOT ROUTINE THAT INITIATES ADDING THE SERVICE INIT OPTIONS
	protected final void   addBaseServiceInitOptions  (STAFCommandParser registrar) {

		registrar.addOption( SFR_SERVICE_OPTION_DIR, 1, STAFCommandParser.VALUEREQUIRED );
		registrar.addOption( SFR_SERVICE_OPTION_EXT, 1, STAFCommandParser.VALUEREQUIRED );
		
		addServiceInitOptions ( registrar );
	}
	

	// ROOT ROUTINE THAT INITIATES VALIDATING THE SERVICE INIT REQUEST
	protected  final int validateBaseServiceParseResult (STAFCommandParseResult parsedData){ 

		dir = parsedData.optionValue(SFR_SERVICE_OPTION_DIR);
		ext = parsedData.optionValue(SFR_SERVICE_OPTION_EXT);

		if (ext.length() > 0 ) file_extension_available = true;
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
				return STAFResult.AccessDenied;
			}
		}
		return validateServiceParseResult ( parsedData );
	}


	// ROOT ROUTINE THAT INITIATES THE BUILDING THE LIST OF EXCLUSIVE COMMANDS
	protected final void buildBaseCommandList( STAFCommandParser aparser) {

		// EACH SUBLCLASS MUST START THE BUILD OF requestoptions		
		String requestoptions = SFR_SERVICE_REQUEST_OPEN  +s+
			                    SFR_SERVICE_REQUEST_READL +s+
			                    SFR_SERVICE_REQUEST_QUERY +s+
				                SFR_SERVICE_REQUEST_CLOSE +s+
				                SFR_SERVICE_REQUEST_LIST  +s+
				                SFR_SERVICE_REQUEST_HELP  +s;

		String options = buildCommandList(new String());
		if(options != null) requestoptions += s+ options;
		
	    aparser.addOptionGroup(requestoptions, 1, 1);
	}


	// ROOT ROUTINE THAT INITIATES ADDING COMMAND OPTION GROUPS
	protected final void addBaseCommandOptionGroups(STAFCommandParser aparser){

		// HANDLE and NAME are mutually exclusive
		aparser.addOptionGroup(SFR_SERVICE_PARM_HANDLE +s+ SFR_SERVICE_PARM_NAME, 0, 1);
		addCommandOptionGroups( aparser );
	}


	// ROOT ROUTINE THAT INITIATES BUILDING THE QUERY OPTIONS LIST
	protected final void buildBaseQueryCommandList(STAFCommandParser aparser){

		// QUERY parameters mutually exclusive
		String queryoptions = SFR_SERVICE_PARM_STATUS      +s+
		                      SFR_SERVICE_PARM_FILENAME    +s+
		                      SFR_SERVICE_PARM_FULLPATH    +s+
		                      SFR_SERVICE_PARM_LASTERROR   +s;
		
		String options = buildQueryCommandList( new String() );
		if (options != null) queryoptions += s+ options;
		
		aparser.addOptionGroup( queryoptions, 0, 1);
	}


	// ROOT ROUTINE THAT INITIATES ADDING COMMAND OPTION REQUIREMENTS
	protected final void addBaseCommandOptionNeeds(STAFCommandParser aparser){

		// REQUIRED parameters for each type of REQUEST
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_OPEN , SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_OPEN , SFR_SERVICE_PARM_FILE );

		aparser.addOptionNeed( SFR_SERVICE_REQUEST_READL, SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_QUERY, SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_CLOSE, SFR_SERVICE_PARM_ID );

		// none needed for LIST
		// none needed for HELP

		// QUERY parameters exclusive to QUERY
		aparser.addOptionNeed( SFR_SERVICE_PARM_STATUS      , SFR_SERVICE_REQUEST_QUERY );
		aparser.addOptionNeed( SFR_SERVICE_PARM_FILENAME    , SFR_SERVICE_REQUEST_QUERY );
		aparser.addOptionNeed( SFR_SERVICE_PARM_FULLPATH    , SFR_SERVICE_REQUEST_QUERY );
		aparser.addOptionNeed( SFR_SERVICE_PARM_LASTERROR   , SFR_SERVICE_REQUEST_QUERY );
		
		addCommandOptionNeeds( aparser );
	}


	// p/o STAFServiceInterfaceLevel1
	public final int init (String name, String params){

		servicename   = name;
		serviceparams = params;

		//if ((params == null)||(params.length() == 0)) return STAFResult.InvalidRequestString;
		if (params == null) params = new String();

		STAFCommandParser registrar = new STAFCommandParser(SFR_SERVICE_INIT_PARMS_MAX);

		addBaseServiceInitOptions(registrar);

		return validateBaseServiceParseResult ( registrar.parse(params) );
	}


	// p/o STAFServiceInterfaceLevel1
	public final STAFResult acceptRequest(String machine, String process, int handle, String request) {

		String response = null;

		STAFCommandParseResult parsedData = parser.parse(request);
		STAFResult result = new STAFResult(parsedData.rc, new String());

		if (parsedData.rc == 0) {

			// gonna do it here cause it is used everywhere (except HELP)
			String fileid = parsedData.optionValue(SFR_SERVICE_PARM_ID); // may be empty

			// ===============================================================
			if ( parsedData.optionTimes(SFR_SERVICE_REQUEST_HELP) > 0){
				result.result = buildHELPInfo();

			// ===============================================================
			}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_QUERY) > 0){

				SAFSFile textfile = getParsedDataTextFile( result, handle, fileid, parsedData );
				if (result.rc != 0)  return result;

				if (parsedData.optionTimes(SFR_SERVICE_PARM_FILENAME)    > 0) {
					result.result = String.valueOf(textfile.getFilename());

				}else if (parsedData.optionTimes(SFR_SERVICE_PARM_FULLPATH)    > 0) {
					result.result = String.valueOf(textfile.getFullpath());

				}else if (parsedData.optionTimes(SFR_SERVICE_PARM_STATUS)      > 0) {

					if(textfile.isClosed()){
						result.result = SFR_SERVICE_FILE_STATE_CLOSED;

					}else if (textfile.isEOF()) {
						result.result = SFR_SERVICE_FILE_STATE_EOF;

					}else{
						result.result = SFR_SERVICE_FILE_STATE_OPEN;
					}

					result.result += getSTATUSInfo(textfile, new String());

				}else if (parsedData.optionTimes(SFR_SERVICE_PARM_LASTERROR)   > 0) {

					result.result = "LASTERROR:"+ text(SFR_RBKEY_NOT_IMPLEMENTED);

				}else{
					result.result = getQUERYInfo(textfile, parsedData, new String());
				}

			// ===============================================================
			}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_LIST) > 0){
				/*
				if (processes.isEmpty()){
					result.rc     = STAFResult.DoesNotExist;
					result.result = "0 files";
					return result;}
				*/
				result.result = r+ servicename +" LIST:"+r+r;

				int count = 0;
				// loop through processes so things are sort of sorted a little
				for(Enumeration p = processes.elements(); p.hasMoreElements(); ){
					Vector openhandles = (Vector) p.nextElement();
					for (Enumeration h = openhandles.elements(); h.hasMoreElements(); ){
						Integer ohandle = (Integer) h.nextElement();
						Hashtable openfiles = (Hashtable) handles.get(ohandle);
						for(Enumeration f = openfiles.elements(); f.hasMoreElements(); ) {
							SAFSFile textfile = (SAFSFile) f.nextElement();

							count++;
							result.result += textfile.getMachine()                 +c+
							                  textfile.getProcess()                +c+
							                  String.valueOf(textfile.getHandle()) +c+
							                  textfile.getFileID ()                +c+
							                  getLISTInfo()                        +c+
							                  textfile.getFullpath();
						}
					}
				}

				result.result += r+ servicename +c+ String.valueOf(count) +s+ text(SFR_RBKEY_OPEN_FILES) +s+r;
				return result;

			// ===============================================================
			}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_READL) > 0){

				SAFSFile textfile = getParsedDataTextFile( result, handle, fileid, parsedData );
				if (result.rc != 0)  return result;

				result.result = textfile.readLine();
				if(textfile.isClosed()){
					result.rc = STAFResult.FileReadError;
					result.result = textfile.getFullpath();
				}

				if (result.result == null) result.result = c+ SFR_SERVICE_FILE_STATE_EOF +c;
				return result;

			// ===============================================================
			}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_CLOSE) > 0){

				SAFSFile textfile = getParsedDataTextFile( result, handle, fileid, parsedData );
				if (result.rc != 0)  return result;

				textfile.close();

				result.result = new String();
				removeParsedDataTextFile(result, process, handle, fileid, parsedData);
				if (result.rc == 0) result.result = "CLOSE:"+ fileid;
				return result;

			// ===============================================================
			}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_OPEN) > 0){

				String filename = parsedData.optionValue(SFR_SERVICE_PARM_FILE);
				String temp = filename;

				// try unmodified filename first
				File file = new CaseInsensitiveFile(temp).toFile();

				try{
					// if fails try adding any default extension
					if ((!file.isFile())&&(ext.length() > 0)){

						temp += ext;
						file = null;
						file = new CaseInsensitiveFile(temp).toFile();
					}

					// if fails try adding any default directory path
					if ((!file.isFile())&&(dir.length() > 0)){

						if (filename.startsWith(File.separator)){
							temp = dir + filename.substring(2);
						}else{
							temp = dir + filename;
						}

						file = null;
						file = new CaseInsensitiveFile(temp).toFile();
					}

					// last chance, add any extension along with default directory path
					if ((!file.isFile())&&(dir.length() > 0)&&(ext.length() > 0)){

						temp += ext;
						file = null;
						file = new CaseInsensitiveFile(temp).toFile();
					}

					if(!file.isFile()){

						result.rc = STAFResult.DoesNotExist;
						result.result = filename;
						return result;
					}
				}catch(SecurityException e) {
					result.rc = STAFResult.AccessDenied;
					result.result = filename;
					return result;
				}

				result.result = "OPEN:"+ fileid +c+ process +c+ String.valueOf(handle).trim() +c;

				result.result += getOPENInfo(parsedData, new String());

				result.result += text(SFR_RBKEY_UCPATH) +c+ file.getPath();

				SAFSFile textfile = openFile ( machine, process, handle, fileid, file, parsedData);

				Hashtable openfiles = null;
				Vector openhandles  = null;

				Integer ohandle = new Integer(handle);

				// see if this handle has opened other files
				// if the handle is already stored, then the process to handle
				// mapping is already done, too.
				if (handles.containsKey(ohandle)){
					openfiles = (Hashtable) handles.get(ohandle);
					openfiles.put(fileid, textfile);

				// if not, start a new file storage for handle
				}else{
					openfiles = new Hashtable(4);
					openfiles.put(fileid, textfile);
					handles.put(ohandle, openfiles);

					//new handle -- must deal with process mapping too
					if (processes.containsKey(process)){
						openhandles = (Vector) processes.get(process);
						if (!openhandles.contains(ohandle)) openhandles.addElement(ohandle);

					// first time this process has opened a file
					}else{
						openhandles = new Vector(4,3);
						openhandles.addElement(ohandle);
						processes.put(process, openhandles);
					}
				}

				result.rc = 0;
				//result.result = new String();

			// ===============================================================
			}else{
				processRequest(result, machine, process, handle, parsedData);
			}

		}else{
			result.result = new String(String.valueOf(parsedData.rc) +c+ parsedData.errorBuffer);
		}

		return result;
	}

	// must ONLY be called after a successful call to getParsedDataTextFile
	// because we rely on that error detection to have already occurred.
	protected final void removeParsedDataTextFile( STAFResult result,
	                                       String myprocess, int handle, String fileid,
	                                       STAFCommandParseResult parsedData ){

		Integer ohandle = getParsedDataFileIDHandle( result, fileid, parsedData );
		if (ohandle == null) ohandle = new Integer(handle);

		Hashtable openfiles = (Hashtable) handles.get(ohandle);

		openfiles.remove(fileid);

		if (openfiles.size()==0){

			handles.remove(ohandle);

			// we also have to remove the handle from our process list
			String process = null;

			if( parsedData.optionTimes(SFR_SERVICE_PARM_NAME) == 0){

				process = myprocess;
			}
			else{
				process = parsedData.optionValue(SFR_SERVICE_PARM_NAME);
			}

			if(! processes.containsKey(process)){
				result.rc = STAFResult.InvalidServiceResult;
				result.result = "SAFSFileReader:"+ text(SFR_RBKEY_SYNC_ERROR);
				return;
			}

			openfiles = null;

			Vector openhandles = (Vector) processes.get(process);

			if(! openhandles.contains(ohandle)){
				result.rc = STAFResult.InvalidServiceResult;
				result.result = "SAFSFileReader:"+ text(SFR_RBKEY_SYNC_ERROR);
				return;
			}

			openhandles.removeElement(ohandle);

			if(openhandles.size() == 0){

				processes.remove(process);
			}
			openhandles = null;
		}

		openfiles = null;
	}


	protected final SAFSFile getParsedDataTextFile( STAFResult result, int handle, String fileid,
	                                        STAFCommandParseResult parsedData ){

		if (handles.isEmpty()){
			result.rc     = STAFResult.DoesNotExist;
			result.result = "0 "+ text(SFR_RBKEY_OPEN_FILES);
			return null;}

		Integer ohandle = getParsedDataFileIDHandle( result, fileid, parsedData );
		if (result.rc != 0)  return null;

		if (ohandle == null) ohandle = new Integer(handle);

		if (! handles.containsKey(ohandle)){
			result.rc     = STAFResult.DoesNotExist;
			result.result = "HANDLE "+ handle +c+ "0 "+ text(SFR_RBKEY_OPEN_FILES);
			return null;}

		Hashtable openfiles = (Hashtable) handles.get(ohandle);

		if (! openfiles.containsKey(fileid)){
			result.rc     = STAFResult.DoesNotExist;
			result.result = fileid;
			return null;}

		return (SAFSFile) openfiles.get(fileid);
	}


	// looking for the HANLDE or NAME parameters to be used instead of the
	// requesting threads handle.
	private final Integer getParsedDataFileIDHandle( STAFResult result, String fileid,
	                                           STAFCommandParseResult parsedData ){

		Integer ohandle = null;

		if (parsedData.optionTimes(SFR_SERVICE_PARM_NAME) > 0) {

			ohandle = getProcessFileIDHandle( result,
			                                  parsedData.optionValue(SFR_SERVICE_PARM_NAME),
			                                  fileid);
		}
		else if (parsedData.optionTimes(SFR_SERVICE_PARM_HANDLE) > 0) {

			try{
				ohandle = Integer.valueOf(parsedData.optionValue(SFR_SERVICE_PARM_HANDLE));

			}catch(NumberFormatException e){
				ohandle = null;
				result.rc = STAFResult.InvalidRequestString;
				result.result = "HANDLE:"+ text(SFR_RBKEY_ERROR_NOT_INTEGER) ;
			}
		}

		return ohandle;
	}


	// must verify we don't have duplicate fileid in same process (different handles)
	// if we do, we must exit with error and a message to use the specific handle
	// instead of the process name
	private final Integer getProcessFileIDHandle(STAFResult result, String process, String fileid) {

		//we must loop through each process handle to make sure there is no duplicate
		if (! processes.containsKey(process)){
			result.rc = STAFResult.Ok;
			result.result = process +":0 "+ text(SFR_RBKEY_OPEN_FILES);
			return null;
		}

		// get all handles with open files
		Vector openhandles = (Vector) processes.get(process);

		Hashtable openfiles = null;
		Integer ohandle     = null;
		Integer match       = null;


		int count = 0;

		for( Enumeration enumerator = openhandles.elements(); (enumerator.hasMoreElements() && count < 2);){
			ohandle = (Integer) enumerator.nextElement();

			if(! handles.containsKey(ohandle)){
				result.rc = STAFResult.InvalidServiceResult;
				result.result = "SAFSFileReader:"+ text(SFR_RBKEY_SYNC_ERROR);
				return null;
			}

			openfiles = (Hashtable) handles.get(ohandle);

			if (openfiles.containsKey(fileid)){

				if (++count == 1) {
					match = ohandle;
				}else{
					match = null;
					result.rc = STAFResult.InvalidValue;
					result.result = "HANDLE:"+  text(SFR_RBKEY_REQUIRED);
				}
			}
		}

		return match; // which might be null
	}


	// p/o Interface STAFServiceInterfaceLevel1
	public final int term(){ 

		int rc = shutdown();

		if (!handles.isEmpty()){

			for (Enumeration e = handles.elements(); e.hasMoreElements();){
				Hashtable openfiles = (Hashtable) e.nextElement();
				for(Enumeration e2 = openfiles.elements(); e2.hasMoreElements();){
					SAFSFile textfile = (SAFSFile) e2.nextElement();
					textfile.close();
				}
			}

			handles.clear();
			processes.clear();
		}

		handles = null;
		processes = null;

		return rc;				
	}
}

