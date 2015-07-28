/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import com.rational.test.ft.RationalTestException;
import com.rational.test.ft.script.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.interfaces.*;

import org.safs.*;
import org.safs.rational.logging.*;

/**
 * Extends RationalTestScript, and primarily makes many of its protected methods 
 * public so that we can use them.  
 * <p>  
 * Custom scripts invoked via the CallScript Driver Command should get a reference 
 * to the topmost executing script which will be a subclass of this 
 * org.safs.rational.Script class.
 * <p>
 * Below is the sample code for such a custom script:
 * <p><pre>
 * (existing imports)
 * import org.safs.*;
 * import org.safs.rational.*;
 * import org.safs.rational.logging.RLogUtilities;
 * ...
 * public void testMain(Object[] args) 
 * {
 * 	   Log.info("Custom Script beginning execution...");
 * 	   try{
 * 			// get reference to SAFS\RFT Hook
 * 			Script safs = (Script) getScriptCaller(); 
 * 			RLogUtilities safslog = safs.getLogUtilities();
 * 			RRobotJHook hook = safs.getRobotJHook();
 * 			RTestRecordData testdata = hook.getRTestRecordData();
 * 			STAFHelper staf = hook.getHelper();
 *			
 *			// do real stuff like getting values from SAFSVARS 			
 * 			String value = staf.getVariable("varname");
 *
 *			// sample of writing to the running test log 
 * 			String logname = testdata.getFac();
 * 			safslog.logMessage(logname, "CustomScript received varname value:"+ value);
 * 			safslog.logMessage(logname, "CustomScript changing the varname value to: somevalue");
 * 
 *			// do real stuff like setting values in SAFSVARS 			
 * 		    staf.setVariable("varname", "somevalue");
 * 		}catch(Exception x){
 * 		    Log.error("An error occurred in my custom script:"+ x.getMessage(),x);	
 * 		}	
 * }
 *...
 * Then play with SAFS stuff as much as necessary.
 * </pre>
 * @author  Doug Bauman
 * @since   JUN 24, 2003
 *
 *   <br>   JUN 24, 2003    (DBauman) Original Release
 *   <br>   FEB 11, 2008    (CANAGL) Updated for CallScript usage.
 *   <br>   JUN 24, 2009    (CANAGL) Updated doc for custom script usage
 **/
public abstract class Script extends RationalTestScript {

	public static int count = 0;
	public static int info = 0;

	public static List localAtPath(String path) {
		return atPath(path);
	}

	public static Text localAtText(String name) {
		return atText(name);
	}
	public static java.awt.Point localAtPoint(int row, int col) {
		return atPoint(row, col);
	}
	public static Cell localAtCell(Column c, Row r) {
		return atCell(c, r);
	}
	public static Row localAtRow(int i) {
		return atRow(i);
	}
	public static Column localAtColumn(int i) {
		return atColumn(i);
	}
	public static Row localAtRow(String key, Object val) {
		return atRow(key, val);
	}
	public static Row localAtRow(
		String key1,
		Object val1,
		String key2,
		Object val2) {
		return atRow(key1, val1, key2, val2);
	}
	public static Row localAtRow(
		String key1,
		Object val1,
		String key2,
		Object val2,
		String key3,
		Object val3) {
		return atRow(key1, val1, key2, val2, key3, val3);
	}
	public static Column localAtColumn(String key) {
		return atColumn(key);
	}
	public static Column localAtColumn(String key, Object val) {
		return atColumn(key, val);
	}
	public static Column localAtColumn(
		String key1,
		Object val1,
		String key2,
		Object val2) {
		return atColumn(key1, val1, key2, val2);
	}
	public static Column localAtColumn(
		String key1,
		Object val1,
		String key2,
		Object val2,
		String key3,
		Object val3) {
		return atColumn(key1, val1, key2, val2, key3, val3);
	}

    public static Index localAtIndex(int index) {return atIndex(index);}

	public IFtVerificationPoint localVp(String name) {
		return vp(name);
	}
	public void localCallScript(String script) {
		callScript(script);
	}

	public void localUnregister(Object[] testobjects){ 
		try{ unregister(testobjects);}
		catch(RationalTestException re){
			Log.info("Script IGNORING "+ re.getClass().getName()+" during UNREGISTER some...");
		}catch(RuntimeException re){
			Log.info("Script IGNORING Runtime "+ re.getClass().getName()+" during UNREGISTER some...");
		}
	}
	public void localUnregisterAll(){ 
		Log.info("UNREGISTERING ALL OBJECTS");
		try {unregisterAll();}
		catch(RationalTestException re){
			Log.info("Script IGNORING "+ re.getClass().getName()+" during UNREGISTER ALL...");
		}catch(RuntimeException re){
			Log.info("Script IGNORING Runtime "+ re.getClass().getName()+" during UNREGISTER ALL...");
		}
	}

	/** <br><em>Purpose:</em> Gets the value of an environment variable. 
	 * @param                     name, String
	 * @return                    String
	 **/
	public  String localGetenv(String name) {
	  return getenv(name);
	}

	public void localLogInfo(String info){
		super.logInfo(info);
	}

	public void localLogWarning(String warning){
		super.logWarning(warning);
	}

	public void localLogError(String error){
		super.logError(error);
	}

	public void localLogTestResult(String message, boolean passed, String description){
		super.logTestResult(message, passed,description);
	}
	
	public void localLogTestResult(String message, boolean passed){
		super.logTestResult(message, passed);
	}

	/** <br><em>Purpose:</em> Provides the basic command-line execution functionality. The supplied command line is executed in host-specific fashion with the working directory set to the specified location. If the working directory is null, the script's working directory is used
	 * @param                     command, String, the command line to execute
	 * @param                     workingDirectory, String, The directory that the command should use as its execution context
	 * @return                    ProcessTestObject
	 **/
	public ProcessTestObject localRun(java.lang.String command,
					  java.lang.String workingDirectory){
		return super.run(command, workingDirectory);
    }
	
	/**
	 ** Starts a Java application with the specified options. The JVM and classpath are split out because they tend to be host specific.
	 ** @param main - The full Java class name of the class to be run. Package and class names must be included.
	 ** @param classpath - Specifies the classpath that is used to run the main class. If this argument is null, the system classpath environment variable is used.
	 ** @param workingDirectory - The directory that the command uses as its execution context
	 ** @param jvm - The JVM that is used to execute the main class
	 ** @param jvmOptions - JVM-specific options
	 **/
	public ProcessTestObject localRunJava(java.lang.String main,
					 java.lang.String classpath,
					 java.lang.String workingDirectory,
					 java.lang.String jvm,
					 java.lang.String jvmOptions){
		return super.runJava(main, classpath, workingDirectory, jvm, jvmOptions);
	}
	
	protected RRobotJHook _hook = null;
	protected RLogUtilities _logUtils = null;
	
	/**
	 * Used internally and made available to scripts invoked by the CallScript 
	 * Driver Commands.
	 * <p>  
	 * Scripts should get a reference to the topmost executing 
	 * script which should be a subclass of this org.safs.rational.Script class.
	 * @return org.safs.rational.RRobotJHook
	 */
	public RRobotJHook getRobotJHook(){
		return _hook;
	}
	/**
	 * Used internally and made available to scripts invoked by the CallScript 
	 * Driver Commands
	 * <p>  
	 * Scripts should get a reference to the topmost executing 
	 * script which should be a subclass of this org.safs.rational.Script class.
	 * @return org.safs.STAFHelper
	 */
	public STAFHelper getSTAFHelper(){
		return _hook.getHelper();
	}
	/**
	 * Used internally and made available to scripts invoked by the CallScript 
	 * Driver Commands.
	 * <p>  
	 * Scripts should get a reference to the topmost executing 
	 * script which should be a subclass of this org.safs.rational.Script class.
	 * @return org.safs.rational.RLogUtilities
	 */
	public RLogUtilities getLogUtilities(){
		return _logUtils;
	}
	/**
	 * Used internally and made available to scripts invoked by the CallScript 
	 * Driver Commands.
	 * <p>  
	 * Scripts should get a reference to the topmost executing 
	 * script which should be a subclass of this org.safs.rational.Script class.
	 * @return org.safs.rational.RDDGUIUtilities
	 */
	public RDDGUIUtilities getGuiUtilities(){
		return (RDDGUIUtilities)_hook.getUtilitiesFactory();
	}
	/**
	 * Used internally and made available to scripts invoked by the CallScript 
	 * Driver Commands.
	 * <p>  
	 * Scripts should get a reference to the topmost executing 
	 * script which should be a subclass of this org.safs.rational.Script class.
	 * @return new (empty) org.safs.rational.RTestRecordData
	 */
	public RTestRecordData getTestRecordData(){
		return (RTestRecordData) _hook.getTestRecordDataFactory(getGuiUtilities());
	}
	
	
    public void testMain (Object[] args) 
    {
        try {
            _hook = new RRobotJHook();
      		_logUtils = new RLogUtilities(getSTAFHelper(), this);
      		_logUtils.setCopyLogClass(true);
      		_hook.setLogUtil(_logUtils);
      		_hook.setScript(this);
      		_hook.start();
    	} catch (Exception e) {
      		e.printStackTrace();
  	  	}
  	}	

	/**
	 * Process any -args passed into subclasses storing each as a System.property.
	 * @param args -- array of Strings formatted as name=value pairs. If an arg does not  
	 *                contain "=" then it will be stored as a name=name System.property.
	 */
	protected void processArgs(Object[] args){
		if((args == null)||(args.length == 0)) return;
		String arg;
		String key;
		String val;
		for(int i=0;i<args.length;i++){
			arg = args[i].toString();
			int eq = arg.indexOf("=");
			if(eq == -1) {
				System.setProperty(arg, arg);
			}else{
				try {
					key = arg.substring(0,eq).trim();				
					val = arg.substring(eq+1).trim();
					System.setProperty(key, val);
				}catch(Exception x){
					Log.debug("Ignoring "+ x.getClass().getSimpleName() +" in TestScriptHelper.processArgs for arg: '"+ arg +"'");
				}
			}
		}
	}

	/**
	 * Locate and return the verification point Dummy_contents object in the SUT.
	 */
	protected IFtVerificationPoint Dummy_contentsVP() 
	{
		return vp("Dummy_contents");
	}
	protected IFtVerificationPoint Dummy_contentsVP(TestObject anchor)
	{
		return vp("Dummy_contents", anchor);
	}
}
