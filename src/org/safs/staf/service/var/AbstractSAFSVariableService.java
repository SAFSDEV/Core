package org.safs.staf.service.var;

import org.safs.Log;
import org.safs.SAFSStringTokenizer;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.map.AbstractSAFSAppMapService;
import org.safs.tools.expression.SafsExpression;
import org.safs.tools.stringutils.StringUtilities;
import org.safs.tools.vars.SimpleVarsInterface;

import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSVariablesService class is an external STAF service run by the JSTAF Service Proxy.<br>
 * This acts as a SAFS-aware wrapper for the STAF Variables service.<br>
 * <p>
 * In a true SAFS environment, Variables (DDVariables) are nearly always
 * tightly integrated with the centralized AppMap service.  This is because we allow
 * the retrieval of Variable values to "look thru" to AppMap "constants".  And we allow the
 * AppMap to provide dynamic values via Variables for items that are otherwise normally static.
 * <p>
 * Thus, in a single request for a variable or AppMap value, the search can ping back and forth
 * between Variable and AppMap services until the request is satisfied or we have exhausted
 * the search chain.
 * <p>
 * Another important issue is that SAFS variables use a different syntax for identifying
 * variables:
 * <p>
 * <ul><li>STAF Variable Syntax: {MyVariable}
 *     <li>SAFS Variable Syntax: ^MyVariable
 * </ul>
 * <p>
 * Lastly(?), the RESOLVE command will have to process the EXPRESSIONS supported by SAFS.
 * <p>
 * Oh, additionally, SAFS variable names are not case-sensitive.
 * <p>
 * <b>The SAFSVariablesService service provides the following commands:</b>
 * <p>
 * <table>
 * <tr><td width="40%">
 *         SAFSMAPS           <td>Get/Set the associated AppMap service name
 * <tr><td>HANDLEID           <td>Return the handle used by this service
 * <tr><td>SET                <td>Set a variable value
 * <tr><td>GET                <td>Get a variable value
 * <tr><td>LIST               <td>Retrieves the list of all variables/values
 * <tr><td>RESET              <td>Reset and clear all storage--delete everything.
 * <tr><td>RESOLVE            <td>Resolve variables and expressions in a string.
 * <tr><td>DELETE             <td>Delete a variable
 * <tr><td>COUNT              <td>Return the count of variables currently stored
 * <tr><td>HELP               <td>Get HELP on command syntax.
 * </table>
 * <h2>1.0 Service Registration</h2>
 * <p>
 * Each instance of the service must be registered via the STAF Service service.
 * <p>
 * Example showing comandline registration:
 * <p><pre>
 * STAF LOCAL SERVICE ADD SERVICE &lt;servicename> LIBRARY JSTAF /
 *            EXECUTE org/safs/staf/service/SAFSVariablesService [PARMS &lt;Parameters>]
 *
 * SERVICE ADD SERVICE safsvars LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSVariablesService /
 *                              PARMS SAFSMAPS "altMapService"
 * </pre>
 * <p>
 * By default, the service expects a default "SAFSMAPS" SAFSAppMapService to handle AppMap calls.
 * <p>
 * <b>1.1</b> Valid Parameters when registering the service:
 * <p>
 * <b>1.1.1 SAFSMAPS</b> &lt;servicename><br>
 * The SAFSMAPS parameter specifies the name of the service that will provide
 * SAFS AppMap services to this Variables service.  If not provided, the Variables service
 * expects a "SAFSMAPS" service to handle requests.
 * <p>
 * EX: &lt;PARMS> SAFSMAPS "altMapService"
 * <p>
 * <h2>2.0 Commands</h2>
 * <p>
 * <h3>2.1 SAFSMAPS</h3>
 * <p>
 * Get or Set the name of the SAFSMAPS service handling AppMap requests for variable
 * resolution.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * SAFSMAPS &lt;no parameter> - returns the current SAFSMAPS setting.<br>
 * SAFSMAPS &lt;servicename> - sets an alternate AppMap service.
 * <p>
 * <b>2.1.1 &lt;servicename></b> is the name of an alternate SAFSAppMapService to
 * associate for variables resolution.  By default, thes "SAFSMAPS" service is used.
 * <p>
 * <h3>2.2 HANDLEID </h3>
 * <p>
 * Returns the HANDLE associated with the SAFSVariableService.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HANDLEID
 * <p>
 * <h3>2.3 SET </h3>
 * <p>
 * Sets the value of a SAFS variable.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * SET &lt;name> VALUE &lt;value>
 * <p>
 * <b>2.3.1 &lt;name></b> the name of the variable to "set"
 * <p>
 * <b>2.3.2 &lt;value></b> the value to assign the variable
 * <p>
 * <h3>2.4 GET </h3>
 * <p>
 * Returns the value of a SAFS variable.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * GET &lt;name>
 * <p>
 * <b>2.4.1 &lt;&lt;name></b> the name of the variable to resolve
 * <p>
 * <h3>2.5 DELETE </h3>
 * <p>
 * Delete a variable from storage.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * DELETE &lt;name>
 * <p>
 * <b>2.5.1 &lt;name></b> the name of the variable to delete
 * <p>
 * <h3>2.6 LIST </h3>
 * <p>
 * The LIST command returns the name and value of each variable known to the service.<br>
 * This could include STAF VAR variables not necessarily seen in SAFSVARS.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * LIST [V2]
 * <p>
 * <b>2.6.1 V2 - an optional flag used to return the list in original STAF V2 format instead of 
 * newer STAF V3 marshaled data format.  The V3 format is returned by default when using STAF V3.
 * <p>
 * <h3>2.7 COUNT </h3>
 * <p>
 * The COUNT command returns the count of variables currently stored.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * COUNT
 * <p>
 * <h3>2.8 RESET </h3>
 * <p>
 * When implemented, this will reset (DELETE) all stored variables
 * <p>
 * <b>Syntax:</b>
 * <p>
 * RESET
 * <p>
 * <h3>2.9 RESOLVE </h3>
 * <p>
 * This will resolve variable values and SAFS expressions in a
 * multi-field, delimited string (a test record).  Returns the input strings in a 
 * RC:STRING format.  The RC is a numeric STAF return code -- hopefully 0 (Ok). Followed 
 * by a colon(:), then the actual processed test record.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * RESOLVE &lt;string> [SEPARATOR &lt;char>] [NOEXPRESSIONS]
 * <p>
 * <b>2.9.1 &lt;string></b> the string (input record) to process for variables and expressions.
 * Note, if the CARET (^) identifying a DDVariable cannot be sent intact the service will 
 * accept the string "_DDV_" in place of each intended CARET character and convert it internally before 
 * processing.
 * <p>
 * <b>2.9.2 SEPARATOR &lt;char></b> an optional single character to use as the field
 * delimiter when processing the input string or record as separate fields, left to right.
 * <p>
 * <b>2.9.3 NOEXPRESSIONS</b> an optional value to indicate how the input record will 
 * have the variables and expressions processed.  By default, Expressions are assumed ON.  
 * With NOEXPRESSIONS, the input record is handled in the older Substitute/Extract method: 
 * only the setting and getting of individual variable values occurs.
 * <p>
 * <h3>2.10 HELP </h3>
 * <p>
 * The HELP command returns this syntax information for service requests.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HELP
 * <p>
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 * @author Carl Nagle JUL 03, 2003 Moved initialization into init, out of constructor.
 * @author Carl Nagle SEP 26, 2007 Fixed App Map Resolve problem with explicit ApplicationConstants section
 * @author Carl Nagle MAR 19, 2009 Fixed problem with quoting of already quoted strings.
 * @author JunwuMa    MAY 12, 2009 Adding support for STAF3.
 *                                 Renamed SAFSVariableService (old class only for STAF2) with AbstractSAFSVariableService and keep common operations in it. 
 *                                 Two different versions of the service extend this class for supporting STAF2 and STAF3.  
 * @author CANAGL     JUN 04, 2009 Synchronizing for Thread Safety.
 * @author CANAGL     FEB 04, 2010 Attempt to catch and ignore individual field expression processing errors.
 * @author SBJLWA     APR 05, 2012 Add option "MAPVARLOOP" for command "GET", see Testhelp078350
 * @author CANAGL     JUL 22, 2013 Allow SAFSVARS LIST to return V2 format.
 * 
 * @see SAFSVariableService SAFSVariableService3 SAFSAppMapService SAFSAppMapService3 
 *********************************************************************************************/
public abstract class AbstractSAFSVariableService implements SimpleVarsInterface {


	public int  SVS_SERVICE_REQUEST_ARGS_MAX    = 8;  //?most OPEN should see
	public int  SVS_SERVICE_INIT_ARGS_MAX       = 5;  //?most INIT should see

	public static final String SVS_SERVICE_PROCESS_NAME = "SAFSVariableService";

	public static final String SVS_SERVICE_REQUEST_SAFSMAPS = "SAFSMAPS";
	public static final String SVS_SERVICE_REQUEST_HANDLEID = "HANDLEID";
	public static final String SVS_SERVICE_REQUEST_COUNT    = "COUNT";
	public static final String SVS_SERVICE_REQUEST_GET      = "GET";
	public static final String SVS_SERVICE_REQUEST_SET      = "SET";
	public static final String SVS_SERVICE_REQUEST_VALUE    = "VALUE";
	public static final String SVS_SERVICE_REQUEST_LIST     = "LIST";
	public static final String SVS_SERVICE_REQUEST_RESET    = "RESET";
	public static final String SVS_SERVICE_REQUEST_RESOLVE  = "RESOLVE";
	public static final String SVS_SERVICE_REQUEST_SEPARATOR= "SEPARATOR";
	public static final String SVS_SERVICE_REQUEST_NOEXPRESSIONS  = "NOEXPRESSIONS";
	public static final String SVS_SERVICE_REQUEST_DELETE   = "DELETE";
	public static final String SVS_SERVICE_REQUEST_HELP     = "HELP";
	public static final String SVS_SERVICE_REQUEST_V2       = "V2";
	
	/**
	 * Used for {@link #SVS_SERVICE_REQUEST_GET}<br>
	 * Used internally to stop the loop between map service and variable service.<br>
	 * "MAPVARLOOP" can have parameter, a delimited string, the items have been processed<br>
	 * in map service. The delimiter is {@value AbstractSAFSAppMapService#SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP}<br>
	 * 
	 * Example:<br>
	 * MAPVARLOOP item1_SEP:item2_SEP:item3
	 * 
	 * @see #getValue(String, boolean, String)
	 */
	public static final String SVS_SERVICE_PARM_MAP_VAR_LOOP      = "MAPVARLOOP";

	public static final String SVS_CARET					= "^";
	public static final String SVS_FALSE_CARET              = "_DDV_";
	
	protected STAFCommandParser parser = new STAFCommandParser(SVS_SERVICE_REQUEST_ARGS_MAX);

	protected String servicemaps  = new String(SVS_SERVICE_REQUEST_SAFSMAPS);

	protected String  servicename            = new String();
	protected String  serviceparms           = new String();
	protected boolean service_maps_available = false;

	protected HandleInterface client; // should be passed in from its derived class

	protected static String empty = new String();
	protected static String c = ":";  // colon
	protected static String s = " ";  // space
	protected static String r = "\n"; // newline
	protected static String eq = "="; // equals
	protected static String q = "\""; // quote
	protected static String dq = "\"\""; // double quotes

	protected SafsExpression exp = new SafsExpression(this);

	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public AbstractSAFSVariableService () {
	}

	/**********************************************************************
	 * 	our HELP text
	 **********************************************************************/
	protected String getHELPInfo(){
		return  r+
		        "SAFSVariablesService HELP" +r+
		        r+
		        "HANDLEID" +r+
		        "SAFSMAPS [<safsAppMapService>]" +r+
		        "GET <varname>" +r+
		        "SET <varname> VALUE <value>" +r+
		        "LIST [V2]" +r+
		        "COUNT" +r+
		        "RESET" +r+
		        "DELETE <varname>" +r+
		        "RESOLVE <string> [SEPARATOR <char>] [NOEXPRESSIONS]" +r+
		        "HELP"+r+r;
	}
	
	// common init phase for STAFServiceInterfaceLevel1 and STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
	protected int doInit(HandleInterface client, String name, String params){
		
		this.client = client;
		servicename  = name;
		
		if (params == null) params = new String();
		serviceparms = params;

		// DON'T KNOW WHAT HAPPENS IF WE FAILED TO REGISTER CLIENT ABOVE
		// ARE WE REGISTERED?  OR NOT?

		parser.addOption( SVS_SERVICE_REQUEST_SAFSMAPS , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SVS_SERVICE_REQUEST_HANDLEID , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_GET      , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_REQUEST_SET      , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_REQUEST_VALUE    , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SVS_SERVICE_REQUEST_LIST     , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SVS_SERVICE_REQUEST_RESOLVE  , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_REQUEST_SEPARATOR, 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_REQUEST_NOEXPRESSIONS, 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_DELETE   , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_REQUEST_HELP     , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_COUNT    , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_RESET    , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_PARM_MAP_VAR_LOOP, 1, STAFCommandParser.VALUEALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_V2       , 1, STAFCommandParser.VALUENOTALLOWED );
		

		// each request should have only 1 of these
		parser.addOptionGroup ( SVS_SERVICE_REQUEST_SET  +s+ SVS_SERVICE_REQUEST_HELP     +s+
		                        SVS_SERVICE_REQUEST_GET  +s+ SVS_SERVICE_REQUEST_DELETE   +s+
		                        SVS_SERVICE_REQUEST_LIST +s+ SVS_SERVICE_REQUEST_SAFSMAPS +s+
		                        SVS_SERVICE_REQUEST_COUNT   +s+ SVS_SERVICE_REQUEST_RESET +s+
		                        SVS_SERVICE_REQUEST_RESOLVE +s+ SVS_SERVICE_REQUEST_HANDLEID,
		                        1, 1);

		parser.addOptionNeed (SVS_SERVICE_REQUEST_SET  , SVS_SERVICE_REQUEST_VALUE);
		parser.addOptionNeed (SVS_SERVICE_REQUEST_VALUE, SVS_SERVICE_REQUEST_SET  );
		
		parser.addOptionNeed (SVS_SERVICE_REQUEST_SEPARATOR    , SVS_SERVICE_REQUEST_RESOLVE  );
		parser.addOptionNeed (SVS_SERVICE_REQUEST_NOEXPRESSIONS, SVS_SERVICE_REQUEST_RESOLVE  );

		STAFCommandParser registrar = new STAFCommandParser(SVS_SERVICE_INIT_ARGS_MAX);

		registrar.addOption( SVS_SERVICE_REQUEST_SAFSMAPS , 1,
		                     STAFCommandParser.VALUEREQUIRED );

		STAFCommandParseResult parsedData = registrar.parse(params);
		if(parsedData.rc != STAFResult.Ok) return parsedData.rc;

		if (parsedData.optionTimes(SVS_SERVICE_REQUEST_SAFSMAPS) > 0)
			servicemaps = parsedData.optionValue(SVS_SERVICE_REQUEST_SAFSMAPS);

		return STAFResult.Ok;
	}
	

	// common acceptRequest phase for STAFServiceInterfaceLevel1 and STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle service request from STAF
	 **********************************************************************/
	protected STAFResult doAcceptRequest(String request) {

		String value1 = null;
		String value2 = null;

		STAFCommandParseResult parsedData = parser.parse(request);
		STAFResult locresult = new STAFResult(parsedData.rc, new String());

		if (locresult.rc != STAFResult.Ok){
			locresult.result = new String(String.valueOf(parsedData.rc) +c+
			                    parsedData.errorBuffer);
			return locresult;
		}

		// ===============================================================
		if ( parsedData.optionTimes(SVS_SERVICE_REQUEST_HELP) > 0){
			locresult.result = getHELPInfo();
			return locresult;

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_SAFSMAPS) > 0){

			value1 = parsedData.optionValue(SVS_SERVICE_REQUEST_SAFSMAPS);
			if (! value1.equals(empty)) servicemaps = value1;
			locresult.result = servicemaps;
			return locresult;

		// ===============================================================
		}else if ( parsedData.optionTimes(SVS_SERVICE_REQUEST_HANDLEID) > 0){
			locresult.result = String.valueOf(client.getHandle()).trim();
			return locresult;

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_LIST) > 0){
			return handleList(parsedData);	
			
		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_COUNT) > 0){
			return handleCount();

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_SET) > 0) {

			value1 = parsedData.optionValue(SVS_SERVICE_REQUEST_SET).toLowerCase();
			value2  = parsedData.optionValue(SVS_SERVICE_REQUEST_VALUE);
			synchronized(tempresult){
				setValue(value1, value2);
				locresult.rc = tempresult.rc;
				locresult.result = tempresult.result;
			}
			return locresult;

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_GET) > 0) {

			value1= parsedData.optionValue(SVS_SERVICE_REQUEST_GET);
			
			//If the 'var get value request' comes from the Map Service, we know it is a loop.
			boolean mapVarLoop = parsedData.optionTimes(SVS_SERVICE_PARM_MAP_VAR_LOOP) > 0;
			//processedItems contains the items have been processed in Map Service
			String processedItems = "";
			if(mapVarLoop){
				processedItems = parsedData.optionValue(SVS_SERVICE_PARM_MAP_VAR_LOOP);
			}
			//I don't think we can synchronize here because a call to getValue
			//can result in a call to get an ApplicationConstant out of an App map
			//that can result in another call to VARS getValue from the App Map service 
			//if the app map value is a call to a _DDV or {^resolved} variable.
			//In that scenario, I believe synchronizing on tempresult here would 
			//cause a deadlock when the second call to getValue is made.
			getValue(value1.toLowerCase(), mapVarLoop, processedItems); //internally synchronized...mostly
			locresult.rc = tempresult.rc;
			locresult.result = tempresult.result;
			return locresult;			

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_RESET) > 0){
			return handleReset();

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_RESOLVE) > 0){
			value1 = parsedData.optionValue(SVS_SERVICE_REQUEST_RESOLVE);
			value1 = StringUtilities.findAndReplace(value1, SVS_FALSE_CARET, SVS_CARET);
			if( parsedData.optionTimes(SVS_SERVICE_REQUEST_SEPARATOR) > 0){	
				value2 = parsedData.optionValue(SVS_SERVICE_REQUEST_SEPARATOR);
			}
			boolean expressions = !(parsedData.optionTimes(SVS_SERVICE_REQUEST_NOEXPRESSIONS) > 0);

			try{
				if((value2==null)||(value2.length()==0)){
					String expResult = null;
					try{
						exp.setExpression(value1);
						if (expressions) { expResult = exp.evalExpression();}
						else             { expResult = exp.evalVariables() ;}
						expResult = smartQuoteField(expResult);
						locresult.result = "0:" + expResult;
					}catch(Exception x){
						Log.debug("IGNORING SAFSVARS RESOLVE ERROR for text:"+ value1, x);
						locresult.result = "0:" + value1;
					}
				}
				else{
					String expression = "";
					String field = "";
					value2 = value2.substring(0,1);
					SAFSStringTokenizer toker = new SAFSStringTokenizer(value1, value2);
					while(toker.hasMoreTokens()){
						field = StringUtilities.TWhitespace(toker.nextToken());
						if (! field.equals(dq)){
							if (field.length()> 0){
									try{ 
										exp.setExpression(field);									
										if (expressions) {field = exp.evalExpression();}
										else             {field = exp.evalVariables() ;}
										field = smartQuoteField(field);
									}catch(Exception x){
										Log.debug("IGNORING SAFSVARS RESOLVE ERROR for text:"+ field, x);
									}
							}else{
								field = smartQuoteField(field);
							}
						}
						expression += field;
						if (toker.hasMoreTokens()) expression += value2;
					}
					locresult.result = "0:"+ expression;
				}
			}
			catch(Exception x){
				Log.debug("SAFSVARS RESOLVE Exception:", x);
				locresult.rc = STAFResult.InvalidResolveString;
				locresult.result = "15:"+ value1;
			}

			return locresult;

		// ===============================================================
		}else if( parsedData.optionTimes(SVS_SERVICE_REQUEST_DELETE) > 0){

			value1= parsedData.optionValue(SVS_SERVICE_REQUEST_DELETE);
			value2= stafDeleteCommand(value1.toLowerCase());
			return deleteStoredVariable(value2);
		// ===============================================================
		}else{
			locresult.rc = STAFResult.InvalidRequestString;
			locresult.result = request;
		}

		return locresult;
	}
	
	/**
	 * Made to be overridden by subclasses, if any.
	 * @param deleteCmd
	 * @return STAFResult
	 */
	protected STAFResult deleteStoredVariable(String deleteCmd){
		return client.submit2("local", "var", deleteCmd);
	}
	
	/**
	 * Made to be overridden by subclasses, if any.
	 * @param getCmd
	 * @return STAFResult
	 */
	protected STAFResult getStoredValue(String getCmd){
		return client.submit2("local", "var", getCmd);		
	}
	
	/**
	 * Made to be overridden by subclasses, if any.
	 * @param setCmd
	 * @return STAFResult
	 */
	protected STAFResult setStoredValue(String setCmd){
		return client.submit2("local", "var", setCmd);		
	}
	
	/**
	 * tempresult is internally synchronized since the method can be recursively called
	 * @param varname			String, the variable name to resolve
	 * @param stopMapVarLoop	boolean, if it is an infinite loop between variable and map service<br>
	 *                          true, we call Map Service with option "MAPVARLOOP" to prevent infinite loop.<br>
	 * @param processedItems	String, the items have been processed in Map Service. Delimited by {@value AbstractSAFSAppMapService#SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP}}<br>
	 *                          only stopMapVarLoop is true, this parameter will be used.<br>
	 *                          This parameter will be appended to "MAPVARLOOP", and passed back to Map Service.<br>
	 * @see SimpleVarsInterface#getValue(String)
	 */
	private String getValue(String varname, boolean stopMapVarLoop, String processedItems) {
		
		boolean trymap = true;
		
		if ((varname == null)||(varname.length() == 0)){
			synchronized(tempresult){
		    	tempresult.result = "";
		    	tempresult.rc = STAFResult.Ok;
		    	return tempresult.result;
			}
		}
		// TODO: DEBUG ONLY
		//Log.info("SAFSVARS: getValue processing: "+ varname);
		
		// catch infinite recursion problem from AppMap lookups
		// a _DDV prefix means to NOT try the AppMap again for this iteration
		if (varname.startsWith(AbstractSAFSAppMapService.SAM_DDV_PREFIX.toLowerCase())){
			Log.info("SAFSVARS: AppMap recursion safequard detected...bypassing AppMap lookups.");
			trymap = false;
			varname = varname.substring(AbstractSAFSAppMapService.SAM_DDV_PREFIX_LEN);
		}
		String lentagged = c+ String.valueOf(varname.length()).trim() +c+ varname.toLowerCase();

		String getCmd = stafGetCommmd(lentagged);
		
		STAFResult aresult = getStoredValue(getCmd);
		
		if ((trymap)&&(aresult.rc == STAFResult.VariableDoesNotExist)){

			Log.debug("SAFSVARS: Try to get value for '"+varname+"' from defaultsection of map service.");
			String command = "GETITEM SECTION "+ q + AbstractSAFSAppMapService.SAM_SERVICE_REQUEST_DEFAULTMAPSECTION + q + s + "ITEM "+ varname;
			
			if(stopMapVarLoop){
				//pass back processedItems to Map Service, at that side processedItems is used to stop infinite-loop
				command += " "+AbstractSAFSAppMapService.SAM_SERVICE_PARM_MAP_VAR_LOOP+ " "+ q + processedItems + q;
			}
			Log.debug("SAFSVARS: Map service command='"+command+"'.");
			
			// TODO: if STAFHandle is not an EmbeddedHandle then it will not seek embedded services via EmbeddedHandles class.
			// Thus, it will not see SAFSMAPS if it is running Embedded.
			STAFResult mapresult = client.submit2("local", servicemaps,command);

		    if(mapresult.rc == STAFResult.Ok) {
		    	aresult.rc = mapresult.rc;
		    	aresult.result = mapresult.result;
		    }
		}

		//only return empty string if we tried all options.
		// ! trymap should return DoesNotExist.
		synchronized(tempresult){
			if(trymap){
				if ((aresult.rc == STAFResult.VariableDoesNotExist)||(aresult.rc == STAFResult.DoesNotExist))
				{
			    	tempresult.result = "";
			    	tempresult.rc = STAFResult.Ok;		    
				}else{
					tempresult.rc = aresult.rc;
					tempresult.result = aresult.result;
				}
			}else{
				tempresult.rc = aresult.rc;
				tempresult.result = aresult.result;
			}
			return tempresult.result;
		}
	}
	
	/**
	 * 
	 * @see SimpleVarsInterface#getValue(String)
	 * @see #getValue(String, boolean)
	 */
	public final String getValue(String varname) {
		//Keep the previous behaviors
		return getValue(varname, false, null);
	}

	/**
	 * tempresult must be externally synchronized by caller!
	 * @see SimpleVarsInterface#setValue(String, String)
	 */
	public String setValue(String varname, String varvalue) {

		String command = varname.toLowerCase() + eq + varvalue;
		command = c+ String.valueOf(command.length()).trim() +c+ command;
		
		String setCmd = stafSetCommmd(command);
		tempresult = setStoredValue(setCmd);

		return varvalue;
	}
	
	// returns the STAF command of getting a variable. It should be implemented in its concrete derived class
	protected String stafGetCommmd(String varname) { throw new UnsupportedOperationException(); } 
	
	// returns the STAF command of setting a variable. It should be implemented in its concrete derived class
	protected String stafSetCommmd(String assignExp) { throw new UnsupportedOperationException(); } 

	// returns the STAF command of deleting a variable. It should be implemented in its concrete derived class
	protected String stafDeleteCommand(String varname) { throw new UnsupportedOperationException(); } 
	
	// returns the STAF command of listing a variable. It should be implemented in its concrete derived class
	protected String stafListCommand() { throw new UnsupportedOperationException(); }
	
	// method for command COUNT for SAFSVARS, shall be implemented in its concrete derived class 
	protected STAFResult handleCount() {throw new UnsupportedOperationException(); }
	// method for command RESET for SAFSVARS, shall be implemented in its concrete derived class 
	protected STAFResult handleReset() {throw new UnsupportedOperationException(); }
	// method for command LIST for SAFSVARS, shall be implemented in its concrete derived class 
	protected STAFResult handleList(STAFCommandParseResult parsedData) {throw new UnsupportedOperationException(); }

	/**
	 * Only quote the field if it is not already quoted.
	 * @param field to quote, if necessary
	 * @return quoted field, or field unmodified.
	 */
	protected String smartQuoteField(String field){
		try{
			if (! field.equals(dq)){		
				if((field.length()>0)&&
				   (field.startsWith(q))&&
				   (field. endsWith(q))){
					//don't quote it
				}else{
					field = q +field+ q;
				}
			}
		}catch(Exception x){;}
		return field;
	}
	
	
	/**
	 * Shared by various methods and actually causes thread-safety issues if not 
	 * properly synchronized when multiple processes are using the active service!
	 * CANAGL 2009.06.04
	 */
	protected STAFResult tempresult = new STAFResult();
}

