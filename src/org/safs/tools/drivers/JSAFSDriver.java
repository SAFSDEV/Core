/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;
/**
 * History:<br>
 * 
 * <br>	Oct 26, 2010	(CANAGL) 	Refactored to support separate misc config initialization
 * <br>	Jul 25, 2011	(LeiWang) 	Refactored to support SAFSMonitor
 * <br>	Nov 10, 2014	(LeiWang) 	Modify processExpression(): if Expression is off, do not call resolveExpressions().
 * <br>	Nov 14, 2014	(CANAGL) 	Added static getFirstNonSAFSStackTraceElement support.  
 * <br>                             TestRecordData now initialized with caller filename Class#method and linenumber.
 * <br>	Dec 17, 2014	(SBJLWA) 	Move codes from runXXXDirect() to processCommandDirect(): delay 'millisBetweenRecords' after execution.
 * <br>	JUN 11, 2015	(SBJLWA) 	Added method resolveExpression(): resolve expression as SAFS Variable Service, "DDVariable" will be always evaluated.
 * <br>	JUN 11, 2016	(SBJLWA) 	Added pushTestRecord(), popTestRecord() and modified processCommand(), processCommandDirect(): handle the 
 *                                  test-record overwritten problem when executing CallJUnit.
 *                                    
 */
import java.util.ListIterator;

import org.safs.IndependantLog;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.AbstractCommand;
import org.safs.model.ComponentFunction;
import org.safs.model.DriverCommand;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.DriverCommands;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.GENStrings;
import org.safs.tools.DefaultTestRecordStackable;
import org.safs.tools.ITestRecordStackable;
import org.safs.tools.UniqueStringID;
import org.safs.tools.consoles.SAFSMonitorFrame;
import org.safs.tools.counters.CountStatusInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.UniqueStringItemInfo;
import org.safs.tools.input.UniqueStringMapInfo;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.logs.UniqueStringMessageInfo;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.status.StatusInterface;

/**
 * A class that provides easy access to SAFS functionality for non-SAFS programs and frameworks.
 * <p>
 * JSAFSDriver jsafs = new JSAFSDriver("JSAFS");
 * <br>jsafs.run();
 * <br>//do whatever you want with and without SAFS...
 * <br>jsafs.processCommand(AbstractCommand, String);
 * <br>jsafs.shutdown();
 * <p>
 * <a href="http://safsdev.sourceforge.net/sqabasic2000/UsingJSAFS.htm#advancedruntime" target="_blank" alt="Using JSAFS Doc">Using JSAFS</a>.
 */
public class JSAFSDriver extends DefaultDriver implements ITestRecordStackable{

	public static final String ADVANCED_RUNTIME_ID = "JSAFS";
	public static final String ADVANCED_RUNTIME_TABLE = "Advanced Runtime";
	public static final String ADVANCED_RUNTIME_LEVEL = "STEP";
	public static final String ADVANCED_RUNTIME_PACKAGEROOT = "org.safs";
	
	public StatusCounter statuscounter = null;
	public TestRecordHelper testRecordHelper = null;

	/**
	 * The CounterInfo to send to incrementAllCounters.
	 */
	public UniqueStringCounterInfo counterInfo = null;
	
	protected ITestRecordStackable testrecordStackable = new DefaultTestRecordStackable();

	/**
	 * When SAFS Monitor usage is enabled (useSAFSMonitor=true), and the JSAFSDriver has detected the test 
	 * or a user has initiated a JSAFSDriver shutdown through this mechanism, the default implementation of  
	 * the JSAFSDriver {@link #processCommand(AbstractCommand, String)} is to initiate System finalization 
	 * followed by a System exit--a complete shutdown of the JVM running the tests.
	 * <p>
	 * If the developer does NOT want a such a complete shutdown to occur upon a JSAFSDriver shutdown, then 
	 * this field should be set to 'false'.<br>
	 * The default setting is 'true'.
	 * <p>
	 * @see #useSAFSMonitor
	 * @see #processCommand(AbstractCommand, String)   
	 */
	public boolean systemExitOnShutdown = true;
	
	/**
	 * If automaticResolve is true, each test record will be resolved automatically.
	 * By default, this will be set to false;
	 * 
	 * @see #processExpression(String, String)
	 * @see #runComponentFunction(ComponentFunction, String)
	 * @see #runDriverCommand(DriverCommand, String)
	 */
	private boolean automaticResolve = AUTO_RESOLVE_TESTRECORD;
	
	/**
	 * "," The default separator that will be used when issuing SAFS commands.
	 */
	public String SEPARATOR = ",";
	public static boolean AUTO_RESOLVE_TESTRECORD = false;
	
	public JSAFSDriver(String drivername) {
		this.driverName = drivername;
		automaticResolve = AUTO_RESOLVE_TESTRECORD;
	}

	public boolean removeShutdownHook(){
		boolean state = false;
		try{
			STAFHelper staf = STAFProcessHelpers.registerHelper(driverName);
			state = staf.removeShutdownHook();
			STAFProcessHelpers.unRegisterHelper(driverName);
		}catch(Throwable ignore){
			// we don't care.  Most likely problem is STAF is NOT installed.
			// we have plenty of other places that will catch that when it is 
			// important.  Not catching this causes intentional non-STAF execution 
			// fail when it should not.
		}
		return state;
	}
	
	public boolean reinstateShutdownHook(){
		boolean state = false;
		try{
			STAFHelper staf = STAFProcessHelpers.registerHelper(driverName);
			staf.reinstateShutdownHook();
			STAFProcessHelpers.unRegisterHelper(driverName);
			state = true;
		}catch(Exception ignore){ }
		return state;
	}
	
	public boolean getAutomaticResolve(){
		return automaticResolve;
	}
	public void setAutomaticResolve(boolean automaticResolve){
		this.automaticResolve = automaticResolve;
	}
	
	/**
	 * Routine to log different message types to the active SAFS log.
	 *  
	 * @param message String message
	 * @param description String (optional) for more detailed info.  Can be null.
	 * @param type int message type constant from AbstractLogFacility.
	 * <p>  
	 * Some Message Types:<br/>
	 * <ul>
	 * <li>{@link AbstractLogFacility#GENERIC_MESSAGE}
	 * <li>{@link AbstractLogFacility#PASSED_MESSAGE}
	 * <li>{@link AbstractLogFacility#FAILED_MESSAGE}
	 * <li>{@link AbstractLogFacility#FAILED_OK_MESSAGE}
	 * <li>{@link AbstractLogFacility#WARNING_MESSAGE}
	 * <li>{@link AbstractLogFacility#WARNING_OK_MESSAGE}
	 * </ul>
	 * @see #logGENERIC(String, String)
	 * @see #logPASSED(String, String)
	 * @see #logFAILED(String, String)
	 * @see #logWARNING(String, String)
	 * @see LogsInterface#logMessage(org.safs.tools.logs.UniqueMessageInterface) 
	 */
	public void logMessage(String message, String description, int type){
		logs.logMessage(new UniqueStringMessageInfo(cycleLog.getStringID(), message, description, type));
	}

	/**
	 * Convenience routine to log GENERIC message to the active SAFS log.
	 * <br>The routine uses the AbstractLogFacility.GENERIC_MESSAGE type.
	 *  
	 * @param message String message
	 * @param description String (optional) for more detailed info.  Can be null.
	 * 
	 *@see #logMessage(String, String, int) 
	 */
	public void logGENERIC(String message, String description){
		logMessage(message, description, AbstractLogFacility.GENERIC_MESSAGE);
	}
	
	/**
	 * Convenience routine to log PASSED message to the active SAFS log.
	 * <br>The routine uses the AbstractLogFacility.PASSED_MESSAGE type.
	 *  
	 * @param message String message
	 * @param description String (optional) for more detailed info.  Can be null.
	 * 
	 *@see #logMessage(String, String, int) 
	 */
	public void logPASSED(String message, String description){
		logMessage(message, description, AbstractLogFacility.PASSED_MESSAGE);
	}
	
	/**
	 * Convenience routine to log FAILED message to the active SAFS log.
	 * <br>The routine uses the AbstractLogFacility.FAILED_MESSAGE type.
	 *  
	 * @param message String message
	 * @param description String (optional) for more detailed info.  Can be null.
	 * 
	 *@see #logMessage(String, String, int) 
	 */
	public void logFAILED(String message, String description){
		logMessage(message, description, AbstractLogFacility.FAILED_MESSAGE);
	}
	
	/**
	 * Convenience routine to log WARNING message to the active SAFS log.
	 * <br>The routine uses the AbstractLogFacility.WARNING_MESSAGE type.
	 *  
	 * @param message String message
	 * @param description String (optional) for more detailed info.  Can be null.
	 * 
	 *@see #logMessage(String, String, int) 
	 */
	public void logWARNING(String message, String description){
		logMessage(message, description, AbstractLogFacility.WARNING_MESSAGE);
	}
	
	/**
	 * Convenience routine to retrieve the value of a SAFS Variable stored in SAFSVARS.
	 * <br>This will exploit the <a href="http://safsdev.sourceforge.net/sqabasic2000/CreateAppMap.htm#ddv_lookup" target="_blank">SAFSMAPS look-thru</a> 
	 * and <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining" target="_blank">app map chaining</a> mechanism.  
	 * <br>That is, any variable that does NOT exist in SAFSVARS will be sought as an 
	 * ApplicationConstant in the SAFSMAPS service.
	 * <p>
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm" target="_blank">Test Design Guidelines for Localization</a>.
	 * @param varname
	 * @return String value, or an empty String.
	 * @see #getVarsInterface()
	 */
	public String getVariable(String varname){		
		return getVarsInterface().getValue(varname);
	}
	
	/**
	 * Convenience routine to set the value of a SAFS Variable stored in SAFSVARS.
	 * 
	 * @param varname -- Name of variable to set.
	 * @param value to store for varname.
	 * @see #getVarsInterface()
	 */
	public void setVariable(String varname, String value){
		getVarsInterface().setValue(varname, value);
	}

	/**
	 * Convenience routine to open an App Map.
	 * This can be used instead of using the Driver Command "SetApplicationMap".  
	 * Note: opening an App Map and app map chaining works in LIFO order.  
	 * The last App Map opened becomes the "default" App Map.
	 * 
	 * @param mapname -- Name of app map to open and add to any app map chain.  The 
	 * exact same mapname (not case-sensitive) must be used to closeAppMap.
	 * @throws IllegalArgumentException if mapname is null or cannot be resolved to 
	 * a valid App Map filepath by the driver.
	 * @see #closeAppMap(String)
	 * @see org.safs.tools.input.SAFSMAPS#openMap(org.safs.tools.input.UniqueMapInterface)
	 */
	public void openMap(String mapname) throws IllegalArgumentException{
        
		if(mapname==null || mapname.length()==0) throw new IllegalArgumentException("mapname");
        
		UniqueStringMapInfo mapinfo = new UniqueStringMapInfo(mapname, mapname);
		String mappath = (String)mapinfo.getMapPath(this);

		if (mappath==null) throw new IllegalArgumentException("mapname does not resolve to a valid filepath");

		maps.openMap(mapinfo);
	}
	
	/**
	 * Convenience routine to close an App Map.
	 * This can be used instead of using the Driver Command "CloseApplicationMap".  
	 * Note: Closing an app map removes it from any App Map chain.  If the closed 
	 * App Map was the "default" app map then the last App Map still existing in the 
	 * App Map chain becomes the "default" App Map.  
	 * 
	 * @param mapname -- Name of app map to close and remove from any app map chain.  
	 * The name used must be the same name used in the openMap call or the SetApplicationMap 
	 * Driver Command.  The mapname, however, is not case-sensitive. 
	 * @throws IllegalArgumentException if mapname is null or cannot be resolved to 
	 * a valid App Map filepath by the driver.
	 * @see #openAppMap(String)
	 * @see org.safs.tools.input.SAFSMAPS#closeMap(org.safs.tools.UniqueIDInterface)
	 */
	public void closeMap(String mapname) throws IllegalArgumentException{
        
		if(mapname==null || mapname.length()==0) throw new IllegalArgumentException("mapname");
        
		UniqueStringMapInfo mapinfo = new UniqueStringMapInfo(mapname, mapname);
		String mappath = (String)mapinfo.getMapPath(this);

		if (mappath==null) throw new IllegalArgumentException("mapname does not resolve to a valid filepath");

		maps.closeMap(mapinfo);
	}
	
	/**
	 * Convenience routine to retrieve the value resulting from a <a href="http://safsdev.sourceforge.net/sqabasic2000/StringUtilities.htm#processexpression" target="_blank">standard SAFS Expression</a>.  
	 * <br>The routine provides support identical to SAFS InputRecord expression processing.  
	 * However, it assumes (and requires) that only a single value/expression/field 
	 * is being processed. 
	 * 
	 * @param expression -- a standard SAFS expression to process.
	 * @return String value after the expression has been processed. 
	 * This can result in an empty String or null if the expression is null.   
	 * May return the expression unmodified if we cannot resolve an unused 
	 * character to pass to SAFS as an unused separator for the expression. 
	 * 
	 * @see #getVarsInterface()
	 */
	public String processExpression(String expression){
		if(!isExpressionsEnabled()) return expression;

		String result = resolveExpression(expression, StringUtils.deduceUnusedSeparatorString(expression));
		//TODO LeiWang: should we always remove the wrapping double-quote? If the original expression is double-quoted, then we should not remove them.
		return StringUtils.removeWrappingDoubleQuotes(result);
	}
	/**
	 * This method is used to resolve the expressions within a test record, 
	 * something like ^var=Text or ^varPrefix & ^varSuffix will be processed
	 * by the VariableService, some works like assignment of variable will
	 * be done during that period.
	 * 
	 * One thing to take care: after the resolveExpression, each field will be
	 * quoted by ", for example, 
	 * "C, AppMapResolve, OFF" --> ""C", "AppMapResolve", "OFF""
	 * we should call method getTrimmedUnquotedInputRecordToken() of TestRecordHelper
	 * to get each field.
	 * 
	 * @param testRecord  a normal test record string
	 * @param separator   the separator used in the test record
	 * @return            the resolved test record string
	 */
	protected String processExpression(String testRecord, String separator){
		if(!isExpressionsEnabled()) return testRecord;

		String result = resolveExpression(testRecord, separator);
//		log_self(StringUtils.debugmsg(false)+" original: "+testRecord+" resolved:"+result);
		//expression processing often wraps the result in quotes
		
		//TODO LeiWang: Currently, for a test-record like:  C, AppMapResolve, OFF
		//this method will return:                C", "AppMapResolve", "OFF
		//This is NOT the result we want,
		//maybe we should remove double-quote for each field.
		//maybe we should keep these quotes
		return StringUtils.removeWrappingDoubleQuotes(result);
	}
	
	/**
	 * Resolve an expression. "DDVariable" will be always evaluated.<br>
	 * <b>Note:</b> Only when {@link #isExpressionsEnabled()} is true, the expression will be resolved as "math string".<br>
	 *              No mater what {@link #isExpressionsEnabled()} is, the expression will be resolved as "DDVariable"<br>
	 *              For example, String result = resolveExpression("^var=3+5");<br>
	 *              If {@link #isExpressionsEnabled()} is false, the result is "3+5", and variable "var" contains string "3+5"<br>
	 *              If {@link #isExpressionsEnabled()} is true, the result is "8", and variable "var" contains string "8"<br>
	 * <b>Note:</b> Be careful, the expression will be TRIMMED and then evaluated.<br>
	 *              For example, String result = resolveExpression(" 3+5 ");<br>
	 *              If {@link #isExpressionsEnabled()} is false, the result is string "3+5".<br>
	 *              If {@link #isExpressionsEnabled()} is true, the result is string "8".<br>
	 * @param expression String, the expression to be resolved.
	 * @return String, the resolved expressions.
	 */
	public String resolveExpression(String expression){		
		//If the original expression is not wrapped in double-quote, 
		//resolveExpression will warp the result in double-quote, so we need to remove them
		boolean isQuoted = StringUtils.isQuoted(expression);
		String result = resolveExpression(expression, StringUtils.deduceUnusedSeparatorString(expression));
		return isQuoted? result:StringUtils.removeWrappingDoubleQuotes(result);
	}

	/**
	 * Resolve a set of expressions delimited by separator. "DDVariable" will be always evaluated.<br>
	 * <b>Note:</b> Only when {@link #isExpressionsEnabled()} is true, each expression will be resolved as "math string".<br>
	 *              No mater what {@link #isExpressionsEnabled()} is, each expression will be resolved as "DDVariable"<br>
	 *              For example, String result = resolveExpression("^var=3+5", ",");<br>
	 *              If {@link #isExpressionsEnabled()} is false, the result is ""3+5"" (double-quoted string), and variable "var" contains string "3+5" (normal string)<br>
	 *              If {@link #isExpressionsEnabled()} is true, the result is ""8"" (double-quoted string), and variable "var" contains string "8" (normal string)<br>
	 * <b>Note:</b> Be careful, each expression will be TRIMMED and then evaluated, the evaluated value will be WRAPPED in DOUBLE-QUOTE to return.<br>
	 *              For example, String result = resolveExpression(" 3+5  , >  ,9-3  ", ",");<br>
	 *              If {@link #isExpressionsEnabled()} is false, the result is string ""3+5",">","9-3"", each field has been wrapped in double quote.<br>
	 *              If {@link #isExpressionsEnabled()} is true, the result is string ""8",">","6"", each field has been wrapped in double quote.<br>
	 * @param expressions String, a set of expression delimited by separator, such as "expression1, expression2, expression3"
	 * @param separator String, the separator used to delimit expressions
	 * @return String, the resolved expressions. Each field will be double quoted!
	 */
	protected String resolveExpression(String expressions, String separator){
		if(expressions==null){
			IndependantLog.error(StringUtils.debugmsg(false)+" the expressions are null!");
			return null;
		}
		if(separator==null){
			IndependantLog.error(StringUtils.debugmsg(false)+" separator is null, cannot resolve expressions: "+expressions);
			return expressions;
		}
		//resolveExpressions will split expression by separator, and resolve each of them, and probable wrap them by double-quote
		return getVarsInterface().resolveExpressions(expressions, separator);
	}
	
	/**
	 * Convenience routine to lookup the mapped value stored in the <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining" target="_blank">App Map chain</a>. 
	 * <br>This should support <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapResolve" target="_blank" alt="Dynamic Recognition Strings Doc">dynamic recognition strings</a>.  
	 * <p> 
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm" target="_blank">Test Design Guidelines for Localization</a>.
	 * 
	 * @param mapid -- Name\ID of the Map to use for the lookup. Use null for Default Map.
	 * @param section -- Name of the Section in the Map for the lookup. Use null for Default Section [ApplicationConstants].
	 * @param item -- Name of the Item to lookup in the define Map Section.
	 * @return String value, or an empty String.
	 * @see #getMapsInterface()
	 */
	public String getMappedValue(String mapid, String section, String item){
		String realmap = (mapid==null) ? getMapsInterface().getDefaultMap().getUniqueID().toString():mapid;
		String realsection = (section==null) ? getMapsInterface().getDefaultMapSection().getSectionName():section;
		return getMapsInterface().getMapItem(new UniqueStringItemInfo(realmap, realsection, item));
	}
	
	/**
	 * Convienience (re)initializer for a TestRecordHelper.
	 * <br>Most users don't need to call this if using methods like 
	 * {@link #runDriverCommand(DriverCommand)} and {@link #runComponentFunction(ComponentFunction)}.
	 * <p>
	 * Instantiates and\or reinits a {@link TestRecordHelper} and initializes key properties:
	 * <p><ul>
	 * <li>setFac=cycleLog ID (required)
	 * <li>setFileID="JSAFS" (arbitrary ID since we are not processing a test table)
	 * <li>setFilename="Advanced Runtime" (arbitrary since we are not processing a test table)
	 * <li>setTestLevel="STEP"
	 * <li>setSeparator=SEPARATOR (current value of SEPARATOR)
	 * <li>setStatusCode={@link DriverConstant#STATUS_SCRIPT_NOT_EXECUTED}
	 * <li>setStatusInfo="" (empty)
	 * </ul>
	 * @param store - A TestRecordHelper to reinitialize or null to get a new one initialized.
	 * @return TestRecordHelper
	 * @see TestRecordHelper#reinit();
	 */
	public TestRecordHelper initTestRecordData(TestRecordHelper store){
		TestRecordHelper trd = (store == null) ? new TestRecordHelper(): store;
		StackTraceElement caller = getFirstNonSAFSStackTraceElement();
		String fileID = caller == null ? ADVANCED_RUNTIME_ID : caller.getClassName();
		String fileName = caller == null ? ADVANCED_RUNTIME_TABLE : caller.getClassName()+StringUtils.NUMBER+ caller.getMethodName()+"()";
		int fileLine = caller == null ? 0 : caller.getLineNumber();
		if (store!=null){trd.reinit();}
		trd.setFac(cycleLog.getStringID());
		trd.setFileID(fileID); // can probably be anything
		trd.setFilename(fileName); // can probably be anything
		trd.setLineNumber(fileLine);
		trd.setTestLevel(ADVANCED_RUNTIME_LEVEL);// CYCLE, SUITE, STEP, maybe even nothing!		
		trd.setSeparator(SEPARATOR);
		trd.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
		trd.setStatusInfo("");
		return trd;
	}

	/**
	 * Try to set the test parameters in the super class method.<br>
	 * Then set the separator {@link #SEPARATOR} according to current test level.<br>
	 * @see org.safs.tools.drivers.DefaultDriver#validateTestParameters()
	 */
	protected void validateTestParameters(){
		try{
			//The test parameter information is optional, so if they don't exist, it is ok.
			super.validateTestParameters();
		}catch(IllegalArgumentException e){
			Log.debug("JSAFSDriver: No Test Parameters.", e);
		}
		if(testLevel!=null) SEPARATOR = getTestLevelSeparator(testLevel);
	}
	
	/**
	 * <em>Note:</em>   Before calling method processDriverCommand() and processComponentFunction()<br>
	 *                  you MUST call this method to initialize the global variable testRecordHelper.
	 *                  
	 * @param command   An instance of AbstractCommand which is abstract, so it is indeed<br>
	 *                  an instance of its child class, DriverCommand or ComponentFunction
	 * @param separator The separator used to create test-record from AbstractCommand.
	 * @param record    If it is null, a test-record generated from AbstractCommand <br>
	 *                  will be set as the input-record to TestRecordHelper.<br>
	 *                  Otherwise, if it is not null, it will be set as the input-record to TestRecordHelper.
	 */
	protected void setGlobalTestRecordHelper(AbstractCommand command, String separator, String record){
		testRecordHelper = this.initTestRecordData(null);
		testRecordHelper.setSeparator(separator);
		testRecordHelper.setRecordType(command.getTestRecordID());
		
		String testRecord = null;
		if(record==null){
			testRecord = command.exportTestRecord(separator);
		}else{
			testRecord = record;
		}
		testRecord = automaticResolve? processExpression(testRecord,separator):testRecord;
		testRecordHelper.setInputRecord(testRecord);
		
		testRecordHelper.setCommand(command.getCommandName());
		
		try{ 
			String defaultMap = (String) maps.getDefaultMap().getUniqueID();
			testRecordHelper.setAppMapName(defaultMap);
		}
		catch(ClassCastException ccx){testRecordHelper.setAppMapName("null");}
		catch(NullPointerException npx){testRecordHelper.setAppMapName("null");}
	}
	
	/**
	 * <em>Note:</em>  Before calling this method, <br>
	 *                 you must call {@link #setGlobalTestRecordHelper(AbstractCommand, String, String)}<br>
	 *                 
	 * Execute the desired DriverCommand using the specified field separator for the record.
	 * <br>This implementation tries following engines in this order:
	 * <p><ul>
	 *  <br>{@link #getTIDDriverCommands()}
	 *  <br>{@link #routeToPreferredEngines(TestRecordHelper)}
	 *  <br>{@link #routeToEngines(TestRecordHelper, boolean)}
	 *  
	 * @return  A long value to represent the status of execution. <br>
	 *          It is a constant defined in DriverConstant, such as STATUS_SCRIPT_NOT_EXECUTED<br>
	 */
	protected long processDriverCommand(){
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		
		//Try internal support for Driver Commands
		rc = tidcommands.processRecord(testRecordHelper);
		
		// might add code to check for NOT_EXECUTED and try another Engine
		// try preferred engines next
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  routeToPreferredEngines(testRecordHelper);

		// try any remaining engines if not executed
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  routeToEngines(testRecordHelper, false);

		// try ipcommands (SAFSDRIVERCOMMANDS) if nothing else has worked.
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  ipcommands.processRecord(testRecordHelper);
		
		return rc;
	}

	/**
	 * <em>Note:</em>  Before calling this method, <br>
	 *                 you must call {@link #setGlobalTestRecordHelper(AbstractCommand, String, String)}<br>
	 *                 
	 * Execute the desired ComponentFunction using the specified field separator for the record.
	 * <br>This implementation tries various possible engines based on the standard usage of:
	 * <ul>
	 * <br>{@link AbstractDriver#getTIDGUIlessComponentSupport()}
	 * <br>{@link #routeToPreferredEngines(TestRecordHelper)}
	 * <br>{@link #routeToEngines(TestRecordHelper, boolean)}
	 *  
	 * @return  A long value to represent the status of execution. <br>
	 *          It is a constant defined in DriverConstant, such as STATUS_SCRIPT_NOT_EXECUTED<br>
	 */
	protected long processComponentFunction(){
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;		
		
		// try Autoit CF support
		try{ rc = getAutoItComponentSupport().processRecord(testRecordHelper);}
	    catch(NullPointerException x){;}
		
		// try internal CF support
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
				(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc = getTIDGUIlessComponentSupport().processRecord(testRecordHelper);	    
		
	    // try preferred engines next
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  routeToPreferredEngines(testRecordHelper);

		// try any remaining engines if not executed
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! testRecordHelper.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  routeToEngines(testRecordHelper, false);
		
		return rc;
	}

	/**
	 * Retrieve a CounterStatusInterface object containing the current counts for the specified Counter.
	 * @param counterId -- the Id of the Counter from which to get status counts.
	 * @return CounterStatusInterface Object or null if no such Counter is known.
	 */
	public CountStatusInterface getCounterStatus(String counterId){
		try{ return getCountersInterface().getStatus(new UniqueStringID(counterId)); }
		catch(Exception ignore){}
		return null;
	}
	
	/**
	 * Increment General (not Test) record counts.
	 * @param status
	 * @see StatusCodes
	 */
	public void incrementGeneralStatus(int status){
		switch(status){		
			case StatusCodes.OK:
				statuscounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				statuscounter.incrementIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
				statuscounter.incrementGeneralFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
				statuscounter.incrementGeneralWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				statuscounter.incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			default:
				statuscounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}

	/**
	 * Increment Test Record counts.
	 * @param status
	 * @see StatusCodes
	 */
	public void incrementTestStatus(int status){
		switch(status){		
			case StatusCodes.OK:
				statuscounter.incrementTestPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				statuscounter.incrementTestIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
			case StatusCodes.NO_RECORD_TYPE_FIELD:
			case StatusCodes.UNRECOGNIZED_RECORD_TYPE:
				statuscounter.incrementTestFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
				statuscounter.incrementTestWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				statuscounter.incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			default:
				statuscounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}

	/**
	 * Programmatically conform to user-initiated PAUSE, RUN, STEP, STEP RETRY input from SAFS Monitor.
	 * <p>
	 * This method is used to check for and comply with user input to SAFS Monitor, but  
	 * cannot provide PAUSE_ON_FAILURE support.
	 * <p>
	 * The PAUSE and other features are only checked if {@link #useSAFSMonitor} is 'true'.
	 * <p>
	 * A given status will perform the following functions:
	 * <ul>
	 * STOP (SHUTDOWN_HOOK) - Throw the SAFSException.<br>
	 * PAUSE - sleep 1 second and loop to check again (indefinitely).<br>
	 * RUNNING - Do nothing. Immediately return with RUNNING status.<br>
	 * STEP - While PAUSED, immediately return with STEPPING status.<br>
	 * STEPPING - Enter PAUSE mode when this method is called again.<br>
	 * STEP_RETRY - While PAUSED, immediately return with STEPPING_RETRY status.<br>
	 * STEPPING_RETRY - Enter PAUSE mode when this method is called again.<br>
	 * </ul>
	 * 
	 * @return the current status of the SAFS_DRIVER_CONTROL variable.
	 * @throws SAFSException if SHUTDOWN_HOOK was detected. This routine only throws the Exception. 
	 * It does not attempt a JVM shutdown.
	 */
	public String checkSAFSMonitorStatus() throws SAFSException{
		String driverStatus = getVariable(DRIVER_CONTROL_VAR);
		// CANAGL: do not change the method signature '.checkSAFSMonitorStatus()' text below!
		String debugmsg = getClass().getSimpleName()+ ".checkSAFSMonitorStatus() ";
		
		while (useSAFSMonitor && !driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION)){
		
			if( driverStatus.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
				Log.info(debugmsg+" processing ENGINE SHUTDOWN REQUEST...");
				logMessage(GENStrings.text("user_abort", "User-initiated shutdown requested.  Stopping SAFS assets..."),
						   null, AbstractLogFacility.WARNING_MESSAGE);
				// CANAGL: do not change the   'SHUTDOWN_HOOK'  from JavaHook.SHUTDOWN_RECORD below!
				throw new SAFSException(debugmsg +"user-initiated "+ JavaHook.SHUTDOWN_RECORD+" detected!");
			}
			
			// PAUSE
			if (driverStatus.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION)){
				//check every 1 second				
				//System.gc();			
				try{ Thread.sleep(1000);}catch(Exception x){;}

			// STEP	
			}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_EXECUTION)){
				setVariable(DRIVER_CONTROL_VAR, JavaHook.STEPPING_EXECUTION);
				return JavaHook.STEPPING_EXECUTION;
				
			// STEPPING
			}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_EXECUTION)){
				setVariable(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
			
			// STEP_RETRY_EXECUTION
			}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
				setVariable(DRIVER_CONTROL_VAR, JavaHook.STEPPING_RETRY_EXECUTION);
				return JavaHook.STEPPING_RETRY_EXECUTION;

			// STEPPING_RETRY_EXECUTION
			}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_RETRY_EXECUTION)){
				setVariable(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
			}else{
				Log.info(debugmsg+" unknown or invalid SAFS_DRIVER_CONTROL status. ReSet to RUNNING!");
				setVariable(DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
				return JavaHook.RUNNING_EXECUTION;
			}
			driverStatus = getVariable(DRIVER_CONTROL_VAR);
		}
		return driverStatus;
	}
	
	/**
	 * This method can process DriverCommand or ComponentFunction just as runDriverCommand() 
	 * or runComponentFunction(), further more it includes the functionality of SAFSMonitor 
	 * so users can interctively PAUSE, STEP, DEBUG their script and SHUTDOWN the engine hook.
	 * <p>
	 * This method will automatically be called from runDriverCommand or runComponentFunction 
	 * if {@link #useSAFSMonitor}=true -- which it is by default. If the developer wants to 
	 * disable this feature the developer should set useSAFSMonitor to 'false'.
	 * <p>
	 * If this method detects a user initiated shutdown request via SAFS Monitor, or if the 
	 * running test initiates the same type of shutdown request, this method will initiate a 
	 * full JVM finalization and System.exit if {@link #systemExitOnShutdown}=true -- which 
	 * it is by default. If the developer wants to disable the automatic JVM finalization and 
	 * shutdown feature then the developer should set systemExitOnShutdown to 'false'.
	 * 
	 * @param command   An instance of AbstractCommand which is abstract, so it is<br>
	 *                  an instance of its child class, DriverCommand or ComponentFunction
	 * @param separator The separator used to create test-record from AbstractCommand.
	 * @return TestRecordHelper with result information. 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 * @see #useSAFSMonitor
	 * @see #systemExitOnShutdown
	 * @see #runDriverCommand(DriverCommand)
	 * @see #runComponentFunction(ComponentFunction)
	 */
	public TestRecordHelper processCommand(AbstractCommand command, String separator){
		String debugmsg = getClass().getName()+".processCommand() ";
		String driverStatus = null;
		String testRecord = null;
		String statusInfo = null;
		boolean shutdownHook = false;
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;

mainloop: while (true){
	        //testRecord is null when we first enter this loop.
	        //testRecord will contain the value of STAF variable "SAFS/Hook/inputrecord" when we 
	        //retry the same step.
			setGlobalTestRecordHelper(command, separator, testRecord);
			pushTestRecord(testRecordHelper);
						
			if(command instanceof DriverCommand){
				rc = processDriverCommand();
			}else if(command instanceof ComponentFunction){
				rc = processComponentFunction();
			}else{
				Log.debug(debugmsg+"Command is an instanceof "+command.getClass().getName());
			}

			statusInfo = testRecordHelper.getStatusInfo();
			
			if( rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED &&
				statusInfo.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD) ){
				Log.info(debugmsg+" processing ENGINE SHUTDOWN REQUEST...");
				logMessage(GENStrings.text("user_abort", "User-initiated shutdown requested.  Stopping SAFS assets..."),
						   null, AbstractLogFacility.WARNING_MESSAGE);
				shutdownHook = true;	
				break mainloop;
			}
	
			//======= PAUSE_ON_FAILURE_EXECUTION if Failure or Waning occurs ===
			if(pauseExecution(rc)) {
				//set pause for SAFSMonitorFrame to watch/edit
				Log.debug(debugmsg+" Execution fails, will pause.");
				setVariable(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
			}
			
			delayBetweenRecords();
			
			driverStatus = getVariable(DRIVER_CONTROL_VAR);
			
holdloop:	while(! driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION)){
				// PAUSE
				if (driverStatus.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION)){
					//check every 350 millis
					try{ Thread.sleep(350);}catch(Exception x){;}
				// STEPPING
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_EXECUTION)){
					setVariable(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
				// STEP	
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_EXECUTION)){
					setVariable(DRIVER_CONTROL_VAR, JavaHook.STEPPING_EXECUTION);
					break holdloop;
				// SHUTDOWN
				}else if (driverStatus.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
					Log.info(debugmsg+" processing USER SHUTDOWN REQUEST...");
					logMessage(GENStrings.text("user_abort",
							"User-initiated shutdown requested.  Stopping JSAFS assets..."),
							null, AbstractLogFacility.WARNING_MESSAGE);
					shutdownHook = true;
					break mainloop;
				// STEP_RETRY_EXECUTION
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
					setVariable(DRIVER_CONTROL_VAR, JavaHook.STEPPING_RETRY_EXECUTION);
					break holdloop;
				// STEPPING_RETRY_EXECUTION
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_RETRY_EXECUTION)){
					setVariable(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
				}else{
					Log.info(debugmsg+" unknown or invalid SAFS_DRIVER_CONTROL status. ReSet to RUNNING!");
					setVariable(DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
					break holdloop;
				}
				driverStatus = getVariable(DRIVER_CONTROL_VAR);
			}// end of holdloop:
			
			System.gc();			
			
			popTestRecord();

			if (driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
				// update testrecord for retry
				testRecord =  getVariable(testRecordHelper.getInstanceName() + STAFHelper.SAFS_VAR_INPUTRECORD);
				
				// before retry a component command, run ClearAppMapCache to eliminate cached Object on engine side
				// In case a component with wrong R-Strings still can be found by its cached Object.
				if(command instanceof ComponentFunction){
					//Run driver command ClearAppMapCache, which is to clear the internal cache on engine side
					runDriverCommandDirect(DriverCommands.clearAppMapCache(),SEPARATOR);
				}
			}else{
				Log.debug(debugmsg+" JSAFS Status, SAFS_DRIVER_CONTROL="+driverStatus);
				break mainloop;
			}
		}//end of mainloop
	
		if(shutdownHook){
			//Stop SAFSLogs, engines, services
			shutdown();
			Log.debug(debugmsg+" JSAFS Driver shutdown.");
			
			if(systemExitOnShutdown){
				//Exit the JVM
				System.runFinalization();
				System.exit(0);
			}
		}
		
		return testRecordHelper;
	}
	
	/**
	 * According to the execution's return_code and the global_variable to decide if we need to pause the execution.<br>
	 * @param result, long, the execution's return code
	 * @return boolean, if to pause the execution.
	 */
	private boolean pauseExecution(long result){
		if(result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE)
			return JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_POF_VAR));
		else if(result == DriverConstant.STATUS_SCRIPT_WARNING)
			return JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_POW_VAR));
		else
			return false;
	}
	
	/**
	 * Execute the desired AbstractCommand using the default field separator for the record.
	 * <br>Calls {@link #processCommand(AbstractCommand, String)}.
	 * @param command AbstractCommand command to execute 
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	public TestRecordHelper processCommand(AbstractCommand command){
		return processCommand(command, SEPARATOR);
	}
	
	/**
	 * Execute the desired AbstractCommand using the specified field separator for the record.
	 * This execution will not be controlled by SAFSMonitor<br>
	 * This method should be used internally, not by JSAFS user.<br>
	 * </ul>
	 * @param command AbstractCommand command to execute 
	 * @param separator for test record fields
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	protected TestRecordHelper processCommandDirect(AbstractCommand command, String separator){
		String debugmsg = StringUtils.debugmsg(false);
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		
		//prepare the "test record"
		setGlobalTestRecordHelper(command, separator,null);
		pushTestRecord(testRecordHelper);

		//execute the "test record"
		if(command instanceof DriverCommand){
			rc = processDriverCommand();
		}else if(command instanceof ComponentFunction){
			rc = processComponentFunction();
		}else{
			Log.debug(debugmsg+"Command is an instanceof '"+command.getClass().getName()+"', cannot be handled.");
		}
		
		//delay after exectuion
		delayBetweenRecords();
		Log.debug(debugmsg+"the returned code="+rc);
		
		popTestRecord();

		return testRecordHelper;
	}
	
	/**
	 * Execute the desired DriverCommand using the specified field separator for the record.<br>
	 * This execution will not be controlled by SAFSMonitor<br>
	 * This method should be used internally, not by JSAFS user.<br>
	 * <p> 
	 * @param command 
	 * @param separator
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 * @deprecated call {@link #processCommandDirect(AbstractCommand, String)} instead
	 */
	protected TestRecordHelper runDriverCommandDirect(DriverCommand command, String separator){
		return processCommandDirect(command, separator);
	}
	
	/**
	 * Execute the desired DriverCommand using the specified field separator for the record.<br>
	 * <br>This implementation tries following engines in this order:
	 * <p><ul>
	 *  <br>{@link #getTIDDriverCommands()}
	 *  <br>{@link #routeToPreferredEngines(TestRecordHelper)}
	 *  <br>{@link #routeToEngines(TestRecordHelper, boolean)}
	 * </ul>
	 * <p> 
	 * @param command 
	 * @param separator
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	public TestRecordHelper runDriverCommand(DriverCommand command, String separator){
		Log.info("routing DriverCommand '"+command.getCommandName()+"' with SAFS Monitor support = "+ useSAFSMonitor);
		if(useSAFSMonitor){
			return processCommand(command,separator);
		}else{
			return processCommandDirect(command, separator);
		}
	}

	
	/**
	 * Execute the desired DriverCommand using the default field separator for the record.
	 * <br>Calls {@link #runDriverCommand(DriverCommand, String)}
	 * @param command 
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	public TestRecordHelper runDriverCommand(DriverCommand command){
	    return runDriverCommand(command, SEPARATOR);
	}

	/**
	 * Execute the desired ComponentFunction using the specified field separator for the record.
	 * This execution will not be controlled by SAFSMonitor<br>
	 * This method should be used internally, not by JSAFS user.<br>
	 * </ul>
	 * @param command ComponentFunction command to execute 
	 * @param separator for test record fields
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 * @deprecated call {@link #processCommandDirect(AbstractCommand, String)} instead
	 */
	protected TestRecordHelper runComponentFunctionDirect(ComponentFunction command, String separator){
		return processCommandDirect(command, separator);
	}
	
	/**
	 * Execute the desired ComponentFunction using the specified field separator for the record.
	 * <br>This implementation tries various possible engines based on the standard usage of:
	 * <ul>
	 * <br>{@link AbstractDriver#getTIDGUIlessComponentSupport()}
	 * <br>{@link #routeToPreferredEngines(TestRecordHelper)}
	 * <br>{@link #routeToEngines(TestRecordHelper, boolean)}
	 * </ul>
	 * @param command ComponentFunction command to execute 
	 * @param separator for test record fields
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	public TestRecordHelper runComponentFunction(ComponentFunction command, String separator){
		Log.info("routing ComponentFunction '"+command.getCommandName()+"' with SAFS Monitor support = "+ useSAFSMonitor);
		if(useSAFSMonitor){
			return processCommand(command,separator);
		}else{
			return processCommandDirect(command, separator);
		}
	}
	
	/**
	 * Try to delay a certain time {@link #getMillisBetweenRecords()} between the executions.<br>
	 * Normally, it is called after the execution of a command.<br>
	 * @see #processCommand(AbstractCommand, String)
	 * @see #processCommandDirect(AbstractCommand, String)
	 */
	public void delayBetweenRecords(){
		// Delay Between Records (Commands)
		if(getMillisBetweenRecords() > 0){
			try{ Thread.sleep(getMillisBetweenRecords());}catch(Exception x){;}
		}
	}
	
	/**
	 * Execute the desired ComponentFunction using the default field separator for the record.
	 * <br>Calls {@link #runComponentFunction(ComponentFunction, String)}.
	 * @param command ComponentFunction command to execute 
	 * @return TestRecordHelper with result information in 
	 * {@link TestRecordHelper#getStatusCode()} and 
	 * {@link TestRecordHelper#getStatusInfo()}
	 */
	public TestRecordHelper runComponentFunction(ComponentFunction command){
		return runComponentFunction(command, SEPARATOR);
	}
	
	
	/***************************************************************************
	 * Route the input record to preferred engines only in the order of preference.
	 * <br>Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 */
	protected long routeToPreferredEngines(TestRecordHelper trd){
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;	
		ListIterator list = null;
		
		// try preferred engines first in the order they are preferred
		if (hasEnginePreferences()){
			list = this.getEnginePreferences();
			while((list.hasNext())&&(result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
				try{
					EngineInterface theEngine = getPreferredEngine((String)list.next());
					result = theEngine.processRecord(trd);
				}catch(IllegalArgumentException iax){
					// this should not happen!
					System.out.println(iax.getMessage());
					Log.error(iax.getMessage());
				}
			}
		}
		return result;
	}

	/***************************************************************************
	 * Route the input record to one or more engines, or all engines.
	 * <br>Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 * <p>
	 * If 'sendAll' is TRUE, it means the record needs to be processed by EVERY 
	 * engine, regardless of the response from any one engine.  For example, 
	 * if the driver must tell each engine to clear a cache, or something.
	 * <p>
	 * if 'sendAll' is FALSE, we will not route to any engine that is 'preferred' 
	 * because all 'preferred' engines should have already been tried.
	 * 
	 * @see #routeToPreferredEngines(TestRecordHelper)
	 **************************************************************************/
	protected long routeToEngines( TestRecordHelper trd, boolean sendAll) {
		
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;			
		ListIterator list = this.getEngines();
		
		while((list.hasNext())&&
		      ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)||
		       (sendAll))){
			EngineInterface theEngine = (EngineInterface) list.next();
			try{
				// don't call it if we already called it as a 'preferred engine
				if((!sendAll)&&(isPreferredEngine(theEngine))) continue;
				result = theEngine.processRecord(trd);
			}catch(IllegalArgumentException iax){
				// this should not happen!
				System.out.println(iax.getMessage());
				Log.error(iax.getMessage());
			}
		}
		
		if (sendAll) result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
		return result;
	}
	
	/**
	 * Called to initialize the JSAFSDriver and make it ready for use.
	 * <br>This overridden implementation does not invoke {@link #processTest()}.
	 * Thus, the driver is ready to use for ad-hoc calls to SAFS.
	 * The driver must be {@link #shutdown()} when testing is completed. 
	 * <p>
	 * Invokes:<br/>
	 * {@link #validateRootConfigureParameters(boolean)}<br/>
	 * {@link #validateLogParameters()}<br/>
	 * {@link #initializeRuntimeInterface()}<br/>
	 * {@link #launchSAFSMonitor()}<br/>
	 * {@link #initializePresetVariables()}<br/>
	 * {@link #initializeMiscConfigInfo()}<br/>
	 * {@link #initializeRuntimeEngines()}<br/>
	 * {@link #openTestLogs()}<br/>
	 * initialize {@link #statuscounter}<br/>
	 */
	public void run(){
		try{
		    System.out.println("Validating Root Configure Parameters...");
		    validateRootConfigureParameters(true);
		    System.out.println("Validating Test Parameters...");
		    validateTestParameters();
		    System.out.println("Validating Log Parameters...");
		    validateLogParameters();			
		    System.out.println("Initializing Runtime Interfaces...");
		    initializeRuntimeInterface();
		    launchSAFSMonitor();
		    initializePresetVariables();
		    initializeMiscConfigInfo();
		    initializeRuntimeEngines();
		    openTestLogs();
			statuscounter = statuscounts instanceof StatusCounter ? (StatusCounter)statuscounts: new StatusCounter();
			counterInfo = counterInfo == null ? 
					new UniqueStringCounterInfo(cycleLog.getStringID(), DriverConstant.DRIVER_CYCLE_TESTLEVEL):
					counterInfo;
			testRecordHelper = initTestRecordData(null);
		    
		}
		catch(IllegalArgumentException iae){ System.err.println("JSAFS "+ iae.getClass().getSimpleName()+": "+ iae.getMessage());	}
		    
		catch(Exception catchall){
			System.err.println("\n****  Unexpected CatchAll Exception handler  ****");
			System.err.println(catchall.getMessage());
			catchall.printStackTrace();
		}
		    
	}

	/**
	 * Used to shutdown the the driver and, potentially, all of SAFS once testing is complete.
	 * <p>
	 * Invokes:<br/>
	 * {@link #closeTestLogs()}<br/>
	 * {@link #shutdownRuntimeEngines()}<br/>
	 * {@link #shutdownRuntimeInterface()}<br/>
	 * {@link SAFSMonitorFrame#dispose()}<br/>
	 * 
	 */
	public void shutdown(){
		try{
		    closeTestLogs();					// include any CAPPING of XML logs
		}catch(Throwable t){
			System.err.println("Ignoring JSAFS CloseTestLogs "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		try{
		    shutdownRuntimeEngines();			// maybe, maybe not.
		}catch(Throwable t){
			System.err.println("Ignoring JSAFS shutdownRuntimeEngines "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		try{
		    shutdownRuntimeInterface(); 		// maybe, maybe not.
		}    
		catch(Throwable t){
			System.err.println("Ignoring JSAFS shutdownRuntimeInterfaces "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		try{ 
			if (safsmonitor != null) {
				Thread mThread = new Thread(){
					public void run(){
						safsmonitor.dispose();
					}
				};
				mThread.setDaemon(true);
				mThread.start();
			}
		}
		catch(Throwable t){
			System.err.println("Ignoring JSAFS SAFS Monitor "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
	}

	/**
	 * Required abstract method is never really called since we have overridden {@link #run()}
	 * @return {@link #statuscounter}
	 */
	protected StatusInterface processTest() {
		return statuscounter;
	}

	/**
	 * Examine the calling Thread's Stack Trace and return the first StackTraceElement that is NOT from
	 * the org.safs... package hierarchy.
	 * @return the first StackTraceElement that is NOT from somewhere in the org.safs package hierarchy, or null if 
	 * we cannot deduce one.
	 */
	public static StackTraceElement getFirstNonSAFSStackTraceElement(){
		StackTraceElement element = null;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(int i=1; i < stack.length; i++){
			element = stack[i];
			if(!element.getClassName().startsWith(ADVANCED_RUNTIME_PACKAGEROOT)) return element;
		}
		return null;
	}
	
	
	private static void log_self(String message){
		Log.info(message);System.out.println(message);}
	
	/**
	 * Self-Test some JSAFSDriver functionality.
	 * <p>
	 * java -Dsafs.project.config="C:\SAFS\Project\JSAFSExample.ini" _<br>
	 *      -Dmain_map="NLSBridgeTest.MAP" _<br>
	 *      -Dlocalized="NLSBridgeTest_en.MAP" or -Dlocalized="NLSBridgeTest_ja.MAP" _<br>
	 *      org.safs.tools.drivers.JSAFSDriver
	 * <p>
	 * @param args
	 */
	public static void main(String[] args){
		log_self("Commencing JSAFS Self-Test...");
		JSAFSDriver jsafs = new JSAFSDriver("JSAFS");
		String sep = ","; //default SAFS field separator
		jsafs.run();
		
		log_self("JSAFS Self-Test initializing App Map Chaining and Expression Processing...");
		TestRecordHelper trd = jsafs.runDriverCommand(DDDriverCommands.appMapChaining("ON"), sep);
		trd = jsafs.runDriverCommand(DDDriverCommands.appMapResolve("ON"), sep);
		trd = jsafs.runDriverCommand(DDDriverCommands.expressions("ON"), sep);

		log_self("JSAFS Self-Test evaluating -D App Map Properties passed in for NLS support...");
		String mainmap = System.getProperty("main_map", "NLSBridgeTest.MAP");
		String localmap = System.getProperty("localized", "NLSBridgeTest_en.MAP");
		
		log_self("JSAFS Self-Test main_map  is: "+ mainmap);
		log_self("JSAFS Self-Test local_map is: "+ localmap);
		
		log_self("JSAFS Self-Test setting App Map chaining order...");
		trd = jsafs.runDriverCommand(DriverCommands.setApplicationMap(mainmap), sep);
		trd = jsafs.runDriverCommand(DriverCommands.setApplicationMap(localmap), sep);
		
		log_self("JSAFS Self-Test verifying variable to app map look-thru...");
		String appconst = jsafs.getVariable("MainWin");
		log_self("JSAFS Self-Test Variable MainWin resolved to: "+ appconst);

		log_self("JSAFS Self-Test verifying app map to variable resolve...");
		String resolve = jsafs.getMappedValue(null, "MainWin", "MainWin");
		log_self("JSAFS Self-Test MainWin:MainWin resolved to: "+ resolve);
		resolve = jsafs.getMappedValue(null, "MainWin", "Comp");
		log_self("JSAFS Self-Test MainWin:Comp resolved to localized: "+ resolve);
		
		log_self("JSAFS Self-Test verifying variable override of ApplicationConstant...");
		jsafs.setVariable("MainWin", "*** ApplicationConstant MainWin has been overridden by Variable ***");
		resolve = jsafs.getMappedValue(null, "MainWin", "MainWin");
		log_self("JSAFS Self-Test MainWin:MainWin now resolves to: "+ resolve);

		log_self("JSAFS Self-Test verifying override of ApplicationConstant can be cancelled...");
		jsafs.getVarsInterface().deleteVariable("MainWin");
		resolve = jsafs.getMappedValue(null, "MainWin", "MainWin");
		log_self("JSAFS Self-Test MainWin:MainWin now resolves to: "+ resolve);
		
		log_self("JSAFS Self-Test verifying openMap and closeMap API");
		String mapfullpath = jsafs.getDriverRootDir()+"\\Project\\Datapool\\TIDTest.MAP";
		log_self("JSAFS Self-Test TIDTest.MAP resolves to: "+ mapfullpath);		

		resolve = null;		
		jsafs.openMap("TIDTest.MAP");
		appconst = jsafs.getVariable("does_not_contain");
		log_self("JSAFS Self-Test Constant 'does_not_contain' resolved to: "+ appconst);
		appconst = jsafs.getVariable("MainWin");
		log_self("JSAFS Self-Test app map look-thru for 'MainWin' resolved to: "+ appconst);
		resolve = jsafs.getMappedValue(null, "SAFSMon", "SAFSMon");
		log_self("JSAFS Self-Test SAFSMon:SAFSMon resolves to: "+ resolve);
		resolve = jsafs.getMappedValue(null, "MainWin", "MainWin");
		log_self("JSAFS Self-Test MainWin:MainWin now resolves to: "+ resolve);

		resolve = null;
		jsafs.closeMap("TIDTest.MAP");
		resolve = jsafs.getMappedValue(null, "SAFSMon", "SAFSMon");
		log_self("JSAFS Self-Test SAFSMon:SAFSMon from closed map now resolves to: "+ resolve);
		resolve = jsafs.getMappedValue(null, "MainWin", "MainWin");
		log_self("JSAFS Self-Test MainWin:MainWin now resolves to: "+ resolve);
		appconst = jsafs.getVariable("MainWin");
		log_self("JSAFS Self-Test app map look-thru for 'MainWin' resolved to: "+ appconst);
				
		log_self("JSAFS Self-Test verifying processExpression...");
		String expression = jsafs.processExpression("^val=(2+3)*(2+3)");
		log_self("JSAFS Self-Test expression ^val=(2+3)*(2+3) resolved to: "+ expression);
		appconst = jsafs.getVariable("val");
		log_self("JSAFS Self-Test ^val was stored as: "+ appconst);
		expression = jsafs.processExpression("(2+3)*(2+3)");
		log_self("JSAFS Self-Test expression (2+3)*(2+3) resolved to: "+ expression);
		expression = jsafs.processExpression("\"(2+3)*(2+3)\"");
		log_self("JSAFS Self-Test expression \"(2+3)*(2+3)\" resolved to: "+ expression);

		log_self("\nJSAFS Self-Test SAFS Monitor usage (Go ahead...try it!):\n");
		log_self("Current SAFSMonitor status:"+ jsafs.getVariable(DRIVER_CONTROL_VAR));
		try{Thread.sleep(4000);}catch(InterruptedException x){}
		try{
			log_self("Current SAFSMonitor status:"+ jsafs.checkSAFSMonitorStatus());
			try{Thread.sleep(1000);}catch(InterruptedException x){}
			log_self("Current SAFSMonitor status:"+ jsafs.checkSAFSMonitorStatus());
			try{Thread.sleep(1000);}catch(InterruptedException x){}
			log_self("Current SAFSMonitor status:"+ jsafs.checkSAFSMonitorStatus());
			try{Thread.sleep(1000);}catch(InterruptedException x){}
			log_self("Current SAFSMonitor status:"+ jsafs.checkSAFSMonitorStatus());
			try{Thread.sleep(1000);}catch(InterruptedException x){}
		}catch(SAFSException x){
			log_self("SAFSMonitor User-Initiated STOP!");
		}
		jsafs.shutdown();
		log_self("Ended JSAFS Self-Test...");
	}

	public void pushTestRecord(TestRecordData trd) {
		testrecordStackable.pushTestRecord(trd);		
	}

	public TestRecordData popTestRecord() {
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg+"Current test record: "+DefaultTestRecordStackable.testRecordToString(testRecordHelper));
		
		TestRecordData history = testrecordStackable.popTestRecord();
		
		if(!testRecordHelper.equals(history)){
			IndependantLog.debug(debugmsg+"Reset current test record to: "+history);
			//The cast should be safe, as we push TestRecordHelper into the stack.
			testRecordHelper = (TestRecordHelper) history;
		}
		
		return history;
	}
}
