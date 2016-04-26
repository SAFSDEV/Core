/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import java.io.File;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Vector;

import org.safs.JavaHook;
import org.safs.Log;
import org.safs.Processor;
import org.safs.StringUtils;
import org.safs.image.ImageUtils;
import org.safs.text.CaseInsensitiveHashtable;
import org.safs.text.FAILStrings;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.MainClass;
import org.safs.tools.consoles.SAFSMonitorFrame;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.engines.AutoItComponent;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.engines.SAFSDRIVERCOMMANDS;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.engines.TIDDriverCommands;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.logs.UniqueStringLogInfo;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stacks.StacksInterface;
import org.safs.tools.status.SAFSSTATUS;
import org.safs.tools.stringutils.StringUtilities;
import org.safs.tools.vars.VarsInterface;

/**
 * The root, yet abstract, implementation of our tool-independent Driver.
 * The final concrete implementation must implement {@link AbstractDriver#processTest()}.
 * <p>
 * We use String-based Interface objects for inter-API communication.
 * <p>
 * The driver monitors SAFSVARS variable 'SAFS_DRIVER_CONTROL'.
 * If this variable is set to 'SHUTDOWN_HOOK' then the driver will initiate a shutdown.
 * <p>
 * The driver will provide a Frame monitor allowing the user to initiate such a shutdown.
 * 
 * <br>(Carl Nagle) DEC 10, 2008 Fixed LogMode problems affecting XML Logging
 * <br>(LeiWang)APR 20, 2010 Modify method initializePresetVariables(): Read the OCR settings from INI file, 
 *                                                                  then set them to STAF Variables.
 * <br>(LeiWang)JUL 13, 2010 Modify method initializePresetVariables(): Read the IBT settings from INI file,
 *                                                                  then set to ImageUtils
 * <br>(JunwuMa)SEP 27, 2010 Added initialization for step retry working with SAFSMonitorFrame.
 * <br>	Oct 26, 2010	(Carl Nagle) 	Refactored to support separate misc config initialization
 * <br>	Jul 08, 2011	(Carl Nagle) 	Add support for PREFERRED_ENGINES_OVERRIDE
 * <br>	SEP 12, 2013	(Carl Nagle) 	Add Daemon threads for SAFSMonitor shutdown.
 * 
 * 
 * @see org.safs.tools.UniqueStringID
 * @see org.safs.tools.logs.UniqueStringLogInfo
 */
public abstract class DefaultDriver extends AbstractDriver {

    protected SAFSMonitorFrame          safsmonitor = null;	
	protected SAFSSTATUS                status     = null;
	protected StacksInterface           cycleStack = null;
	protected StacksInterface           suiteStack = null;
	protected StacksInterface           stepStack  = null;

	/**
	 * If true, the SAFS Monitor window can be 
	 * used to control test execution--allowing the tester to interactively PAUSE the test, STEP through 
	 * portions of the test, or force a premature SHUTDOWN (Abort) of the test--including any running SAFS 
	 * Engines.
	 * <p>
	 * To ignore the default SAFS Monitor integration, the developer can set this value to 'false'.<br>
	 * The default setting is 'true'.
	 */
	public boolean useSAFSMonitor = true;
	
	/** Stores ALL instanced EngineInterface objects in instanced order. */
	protected Vector                    engines = new Vector(5,1);
	/** Stores only active 'preferred' engine class names in preferred order. */
	protected Vector                    enginePreference = new Vector(5,1);
	/** 
	 * Stores ALL instanced EngineInterface objects in classname=engine format.
	 * Note the classname is NOT case-sensitive.
	 * @see CaseInsensitiveHashtable 
	 */
	protected CaseInsensitiveHashtable  engineObjects = new CaseInsensitiveHashtable(5);

	protected UniqueStringLogInfo cycleLog = null;	
	protected UniqueStringLogInfo suiteLog = null;	
	protected UniqueStringLogInfo stepLog = null;
	
	public boolean isUseSAFSMonitor() {
		return useSAFSMonitor;
	}

	public void setUseSAFSMonitor(boolean useSAFSMonitor) {
		this.useSAFSMonitor = useSAFSMonitor;
	}

	/**
	 * @see DriverInterface#getEngines()
	 */
	public ListIterator getEngines() { 
			return engines.listIterator();
	}

	/**
	 * @see DriverInterface#getEnginePreferences()
	 */
	public ListIterator getEnginePreferences() { return enginePreference.listIterator();}

	/**
	 * used internally to find the index of an engine marked as "preferred"
	 */
	protected int getPreferredEngineIndex(String key){
		try{
			String uckey = key.toUpperCase();
			int engindex = 0;
			int subindex = -1;
			String eng = null;
			ListIterator list = enginePreference.listIterator();
			while (list.hasNext()){
				eng = (String)list.next();
				subindex = eng.indexOf(uckey);
				if (subindex >= 0) return engindex;
				engindex++;
			}
		}catch(NullPointerException npx){;}
		return -1;				
	}

	/**
	 * used internally or by subclasses to find the validity of an engine name.
	 * For example, if the key substring provided is "SAFSROBOTJ", 
	 * then the full key of "ORG.SAFS.TOOLS.ENGINES.SAFSROBOTJ" will be returned 
	 * if it is a running engine.
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	protected String getFullEngineClass(String key) throws IllegalArgumentException{
		try{
			String uckey = key.toUpperCase();
			if (engineObjects.containsKey(uckey)) return uckey;
			int subindex = -1;
			String eng = null;
			Enumeration list = engineObjects.keys();
			while (list.hasMoreElements()){
				eng = (String)list.nextElement();				
				subindex = eng.indexOf(uckey);
				if (subindex >= 0) return eng;
			}
		}catch(NullPointerException npx){;}
		String text = "DefaultDriver.startEnginePreference(KEY)";
		String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
		throw new IllegalArgumentException(err);
	}
	
	/**
	 * Retrieve a running {@link EngineInterface} engineObject by stored classname.
	 * @param key full classname or simple classname of engine to retrieve.
	 * For example, "SAFSROBOTJ" will successfully get "org.safs.tools.engines.SAFSROBOTJ" 
	 * if it is running.
	 * @see #getFullEngineClass(String)
	 * @see DriverInterface#getPreferredEngine(String)
	 * @see #engineObjects
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public EngineInterface getPreferredEngine(String key) throws IllegalArgumentException{
		String uckey = getFullEngineClass(key);
		return (EngineInterface)engineObjects.get(uckey);
	}	
	
	/**
	 * @see DriverInterface#hasEnginePreferences()
	 */
	public boolean hasEnginePreferences() { return !(enginePreference.isEmpty());}
	
	/**
	 * @see DriverInterface#startEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void startEnginePreference(String key) throws IllegalArgumentException{
		String uckey = null;
		try{
			// check if preference already exists.  move to first if so.
			uckey = key.toUpperCase();
			int index = getPreferredEngineIndex(key);
			if (index >= 0){
				Object eng = enginePreference.elementAt(index);
				enginePreference.removeElementAt(index);
				enginePreference.insertElementAt(eng, 0);			
				return;
			}
			// see if the key matches known engine classes
			String fulleng = getFullEngineClass(uckey);
			enginePreference.insertElementAt(fulleng, 0);
		}catch(NullPointerException npx){
			String text = "DefaultDriver.startEnginePreference(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
	    }
	}

	/**
	 * @see DriverInterface#endEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void endEnginePreference(String key) throws IllegalArgumentException{
		int index = getPreferredEngineIndex(key);
		if (index < 0) {
			String text = "DefaultDriver.endEnginePreference(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
		enginePreference.removeElementAt(index);
	}

	/**
	 * @see DriverInterface#clearEnginePreferences()
	 */
	public void clearEnginePreferences(){ enginePreference.clear();}

	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is invalid or unknown
	 */
	public boolean isPreferredEngine(String key) throws IllegalArgumentException{
		if (enginePreference.isEmpty()) return false;
		try{
			String uckey = key.toUpperCase();
			// try exact match
			if (enginePreference.contains(uckey)) return true;
			// try partial match
			return(getPreferredEngineIndex(uckey) > -1);			
		}catch(NullPointerException npx){
			String text = "DefaultDriver.isPreferredEngine(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
	}
	
	/**
	 * @see DriverInterface#isPreferredEngine(EngineInterface)
	 * @throw IllegalArgumentException if engine is null
	 */
	public boolean isPreferredEngine(EngineInterface engine) throws IllegalArgumentException{
		if (enginePreference.isEmpty()) return false;
		try{
			ListIterator list = enginePreference.listIterator();
			while(list.hasNext()){
				EngineInterface peng = getPreferredEngine((String)list.next());
				if (peng == engine) return true;
			}
			return false;
		}catch(NullPointerException npx){
			String text = "DefaultDriver.isPreferredEngine(ENGINE)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
	}
	

	/** 
	 * Locate an EngineInterface given the engine priority string as defined in the 
	 * documented Configuration File standard for SAFS_ENGINES. (Ex: "First", "Second", etc.)
	 * <p>
	 * @return an EngineInterface or null if not found or instantiated.**/
	protected EngineInterface getEngineInterface(String itemName){
		
		EngineInterface engine = null;
		Class engineClass = null;
		String engineClassname = configInfo.getNamedValue(
    		                     DriverConstant.SECTION_SAFS_ENGINES, itemName);

		if (engineClassname==null) return null;
		engineClassname = StringUtilities.TWhitespace(engineClassname);
		engineClassname = StringUtilities.removeDoubleQuotes(engineClassname);
		int len = engineClassname.length();
		if (len == 0) return null;
		
		try{		
			engineClass = Class.forName(engineClassname);
			engine = (EngineInterface) engineClass.newInstance();
			engineObjects.put(engineClassname.toUpperCase(), engine);
		}
		catch(ClassNotFoundException cnfe){;}
		catch(InstantiationException ie){;}
		catch(IllegalAccessException iae){;}
		return engine;
	}

	
	/** Initialize or insert a ConfigureInterface at the start of the search order.**/
	protected void insertConfigureInterfaceSource(ConfigureInterface source){
		if (configInfo==null) { configInfo = source; }
		else { if (source!=null) configInfo.insertConfigureInterface(source);}		
	}
	
	
	/** 
	 * Verify the existence of sufficient test information.
	 * We need a test name, test level, and field separator.
	 * If no separator is found we will default to a comma (,) separator.*/
	protected void validateTestParameters(){		
		try{
			Log.info("Retrieving testName...");

			// verify we have a testname
			testName = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_NAME);
			if (testName.length()==0)
		   		testName = configInfo.getNamedValue(
		   		                      DriverConstant.SECTION_SAFS_TEST, "TestName");
		    if (testName.length()==0) throw new NullPointerException();

			Log.info("Checking for alternate testName suffixes...");

			// check for alternate testname suffixes
			String value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "CycleSuffix");
			if(!(value==null)) cycleSuffix = value;

			value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "SuiteSuffix");
			if(!(value==null)) suiteSuffix = value;
			
			value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "StepSuffix");
			if(!(value==null)) stepSuffix = value;
			
			Log.info("Checking for valid test level...");

		    //verify we have a test level command-line parameter
			testLevel = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_LEVEL);
			if (testLevel.length()==0){
				
				// check for a configuration file setting
		   		testLevel = configInfo.getNamedValue(
		   		                      DriverConstant.SECTION_SAFS_TEST, "TestLevel");
		    	if ((testLevel==null)||(testLevel.length()==0)) {
		    		
		    		// check for implied test level from testname suffix
		    		String testNameUC = testName.toUpperCase();
		    		if (testNameUC.endsWith(cycleSuffix.toUpperCase())){
		    			testLevel = DriverConstant.DRIVER_CYCLE_TESTLEVEL;
		    		}else if (testNameUC.endsWith(suiteSuffix.toUpperCase())){
		    			testLevel = DriverConstant.DRIVER_SUITE_TESTLEVEL;
		    		}else if (testNameUC.endsWith(stepSuffix.toUpperCase())){
		    			testLevel = DriverConstant.DRIVER_STEP_TESTLEVEL;
		    		}
		    	}
		    }
		    // verify the test level setting is valid
		    if (testLevel.length()==0) throw new NullPointerException();
		    
		    if ((! testLevel.equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL))&&
		        (! testLevel.equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL))&&
		        (! testLevel.equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL))){
		        throw new NullPointerException();
		    }
		    
			Log.info("Checking for alternate test record separators...");

		    // check for alternate separator info
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_CYCLE_SEPARATOR);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "CycleSeparator");
			if(!(value==null)) cycleSeparator = value;

			value = getParameterValue(DriverConstant.PROPERTY_SAFS_SUITE_SEPARATOR);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "SuiteSeparator");
			if(!(value==null)) suiteSeparator = value;
			
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_STEP_SEPARATOR);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "StepSeparator");
			if(!(value==null)) stepSeparator = value;

		    //check for alternate millisBetweenRecords
			Log.info("Checking for alternate Driver Delay Command-Line setting '-Dsafs.test.millisbetweenrecords'...");
			String millisBetween = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_MILLISBETWEENRECORDS);
			if (millisBetween.length()==0){
				Log.info("Checking for alternate Driver Delay 'SAFS_TEST':'millisbetweenrecords'...");				
				// check for a configuration file setting
				millisBetween = configInfo.getNamedValue(
		   		                      DriverConstant.SECTION_SAFS_TEST, "millisBetweenRecords");
				if(millisBetween == null) millisBetween = "";
		    }
			millisBetween = StringUtils.getTrimmedUnquotedStr(millisBetween);
			if (millisBetween.length()> 0) {
	    		try{
	    			int millis = Integer.parseInt(millisBetween);
	    			millis = millis < 1 ? 0:millis;
	    			setMillisBetweenRecords(millis);
	    		}catch(Exception x){
	    			Log.info("Driver IGNORING invalid millisBetweenRecords setting "+ millisBetween);
	    		}
	    	}
			Log.info("Driver Delay 'millisBetweenRecords' set to "+ getMillisBetweenRecords());

		    //check for alternate secsWaitForWindow
			Log.info("Checking for Command-Line setting '-Dsafs.test.secswaitforwindow'...");
			String secsWindow = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_SECSWAITFORWINDOW);
			if (secsWindow.length()==0){
				Log.info("Checking for alternate Driver Delay 'SAFS_TEST':'secsWaitForWindow'...");				
				// check for a configuration file setting
				secsWindow = configInfo.getNamedValue(
		   		                      DriverConstant.SECTION_SAFS_TEST, "secsWaitForWindow");
				if(secsWindow == null) secsWindow = "";
		    }
			secsWindow = StringUtils.getTrimmedUnquotedStr(secsWindow);
			if (secsWindow.length()> 0) {
	    		try{
	    			int millis = Integer.parseInt(secsWindow);
	    			millis = millis < 1 ? 0:millis;
	    			Processor.setSecsWaitForWindow(millis);
	    		}catch(Exception x){
	    			Log.info("Driver IGNORING invalid secsWaitForWindow setting "+ secsWindow);
	    		}
	    	}
			Log.info("Driver Delay 'secsWaitForWindow' set to "+ Processor.getSecsWaitForWindow());

		    //check for alternate secsWaitForComponent
			Log.info("Checking for Command-Line setting '-Dsafs.test.secswaitforcomponent'...");
			String secsComp = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_SECSWAITFORCOMPONENT);
			if (secsComp.length()==0){
				Log.info("Checking for alternate Driver Delay 'SAFS_TEST':'secsWaitForComponent'...");				
				// check for a configuration file setting
				secsComp = configInfo.getNamedValue(
		   		                      DriverConstant.SECTION_SAFS_TEST, "secsWaitForComponent");
				if(secsComp == null) secsComp = "";
		    }
			secsComp = StringUtils.getTrimmedUnquotedStr(secsComp);
			if (secsComp.length()> 0) {
	    		try{
	    			int millis = Integer.parseInt(secsComp);
	    			millis = millis < 1 ? 0:millis;
	    			Processor.setSecsWaitForComponent(millis);
	    		}catch(Exception x){
	    			Log.info("Driver IGNORING invalid secsWaitForComponent setting "+ secsComp);
	    		}
	    	}
			Log.info("Driver Delay 'secsWaitForComponent' set to "+ Processor.getSecsWaitForComponent());
			
			// check for settings of "NumLock" on/off
			Log.info("Checking for Command-Line setting '-Dsafs.test.numlockon'...");
			String numLockSetting = getParameterValue(DriverConstant.PROPERTY_SAFS_TEST_NUMLOCKON);
			if (numLockSetting.length() == 0) {
				Log.info("Checking for alternative NumLock Setting 'SAFS_TEST':'numLockOn'...");
				// check for a configuration file setting
				numLockSetting = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_TEST, DriverConstant.SECTION_SAFS_TEST_NUMLOCKON);
				if(numLockSetting == null) numLockSetting = "";
			}
			numLockSetting = StringUtils.getTrimmedUnquotedStr(numLockSetting);
			if(numLockSetting.length()>0) setNumLockOn(StringUtilities.convertBool(numLockSetting));
			Log.info("'NumLockOn' status set to "+ getNumLockOn());
			
			// check for settings of 'DismissUnexpectedAlerts'
			Log.info("Checking for Command-Line setting '-Dsafs.test.dismiss_unexpected_alerts'");
			String dismissUnexpectedAlertsSetting = getParameterValue(DriverConstant.PROERTY_SAFS_TEST_DISMISSUNEXPECTEDALERTS);
			if(dismissUnexpectedAlertsSetting.length() == 0){
				Log.info("Checking for alternative 'DismissUnexpectedAlerts' setting 'SAFS_TEST':'DismissUnexpectedAlerts'...");
				// check for a configuration file setting
				dismissUnexpectedAlertsSetting = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_TEST, DriverConstant.SECTION_SAFS_TEST_DISMISSUNEXPECTEDALERTS);
				if (dismissUnexpectedAlertsSetting == null) dismissUnexpectedAlertsSetting = "";
			}
			dismissUnexpectedAlertsSetting = StringUtils.getTrimmedUnquotedStr(dismissUnexpectedAlertsSetting);
			if(dismissUnexpectedAlertsSetting.length()>0) setDismissUnexpectedAlerts(StringUtilities.convertBool(dismissUnexpectedAlertsSetting));				
			Log.info("'DismissUnexpectedAlerts' status set to " + getDismissUnexpectedAlerts());			
			
			Log.info("Test to execute: '"+ testName +"' using "+ testLevel.toUpperCase() +" DRIVER." );
		}
		catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient or invalid TEST information:\n"+
			"Cannot resolve test name, test level, or record separator.\n");}
	}
	
	/** parse a configuration file logmode entry for valid text or numeric values.*/
	protected long parseLogMode(String value) {
		long mode = 0;
		
		try{
			String ucvalue = value.toUpperCase();			

			int index = ucvalue.indexOf(DriverConstant.DRIVER_MAX_LOGMODE_ENABLED_STRING);
			if (index >=0) return DriverConstant.DRIVER_MAX_LOGMODE_ENABLED;
			
			index = ucvalue.indexOf(DriverConstant.DRIVER_TEXTLOG_ENABLED_STRING);
			if (index >=0) mode = DriverConstant.DRIVER_TEXTLOG_ENABLED;
			
			index = ucvalue.indexOf(DriverConstant.DRIVER_TOOLLOG_ENABLED_STRING);
			if (index >=0) mode += DriverConstant.DRIVER_TOOLLOG_ENABLED;
			
			index = ucvalue.indexOf(DriverConstant.DRIVER_XMLLOG_ENABLED_STRING);
			if (index >=0) mode += DriverConstant.DRIVER_XMLLOG_ENABLED;
			
			index = ucvalue.indexOf(DriverConstant.DRIVER_CONSOLELOG_ENABLED_STRING);
			if (index >=0) mode += DriverConstant.DRIVER_CONSOLELOG_ENABLED;
			
			if (mode==0) mode = Long.parseLong(value);
			return mode;
		}
		catch(NullPointerException npe){
			throw new NumberFormatException("No LOGMODE value specified!");
		}
		catch(NumberFormatException nfe){
			throw new NumberFormatException("LOGMODE provided: "+ value);
		}
	}
	
	/** 
	 * Verify the existence of sufficient test information.
	 * We need a test name, test level, and field separator.
	 * If no separator is found we will default to a comma (,) separator.
	 * (Carl Nagle) DEC 10, 2008 Fixed LogMode problems affecting XML Logging 
	 **/
	protected void validateLogParameters(){		
		try{
			Log.info("Checking for log names...");

		    // check for logname information
			String value = getParameterValue(DriverConstant.PROPERTY_SAFS_CYCLE_LOGNAME);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "CycleLogName");
			if(!(value==null)) cycleLogName = value;

			value = getParameterValue(DriverConstant.PROPERTY_SAFS_SUITE_LOGNAME);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "SuiteLogName");
			if(!(value==null)) suiteLogName = value;
			
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_STEP_LOGNAME);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "StepLogName");
			if(!(value==null)) stepLogName = value;

			// validate the logname settings
			if ((cycleLogName==null)&&(suiteLogName==null)&&(stepLogName==null)){
				String pkgname = MainClass.deduceMainClass();
				cycleLogName=suiteLogName=stepLogName=pkgname.substring(pkgname.lastIndexOf(".") + 1,pkgname.length());
			}else{
				if (cycleLogName==null){
					
					if (suiteLogName==null){
						cycleLogName=suiteLogName=stepLogName;
					}
					else{
						cycleLogName=suiteLogName;
						if(stepLogName==null) stepLogName=suiteLogName;
					}
				}
				else{
					if(suiteLogName==null) suiteLogName=cycleLogName;
					if(stepLogName==null)  stepLogName=cycleLogName;
				}
			}
			
			Log.info("Checking for log modes...");
			
		    // check for logmode information
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_CYCLE_LOGMODE);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "CycleLogMode");
			boolean isSetCycleMode = !(value==null);			
			if(isSetCycleMode) cycleLogMode = parseLogMode(value);

			value = getParameterValue(DriverConstant.PROPERTY_SAFS_SUITE_LOGMODE);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "SuiteLogMode");
			boolean isSetSuiteMode = !(value==null);			
			if(isSetSuiteMode) suiteLogMode = parseLogMode(value);
			
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_STEP_LOGMODE);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "StepLogMode");
			boolean isSetStepMode = !(value==null);			
			if(isSetStepMode) stepLogMode = parseLogMode(value);

			// validate the logmode settings
			if (!(isSetCycleMode)&& !(isSetSuiteMode)&& !(isSetStepMode)){
				cycleLogMode=suiteLogMode=stepLogMode=DriverConstant.DEFAULT_LOGMODE;
			}else{
				if (!isSetCycleMode){
					
					if (!isSetSuiteMode){
						cycleLogMode=suiteLogMode=stepLogMode;
					}
					else{
						cycleLogMode=suiteLogMode;
						if(!isSetStepMode) stepLogMode=suiteLogMode;
					}
				}
				else{
					if(!isSetSuiteMode) suiteLogMode=cycleLogMode;
					if(!isSetStepMode)  stepLogMode=cycleLogMode;
				}
			}
			
			Log.info("Checking for log level...");

		    // check for loglevel information
			value = getParameterValue(DriverConstant.PROPERTY_SAFS_LOG_LEVEL);
			if (value.length()==0) value = configInfo.getNamedValue(
		   		           DriverConstant.SECTION_SAFS_TEST, "LogLevel");
			if(!(value==null)) logLevel = value;

			// validate settings
			String loglevelUC = logLevel.toUpperCase();
			if ((! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_ERROR))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_WARN))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_PASS))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_GENERIC))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_INDEX))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_INFO))&&
			    (! loglevelUC.equals(DriverConstant.DRIVER_LOGLEVEL_DEBUG))){
			    throw new NullPointerException();
			}

			Log.info("Log Level='"+ logLevel +"'");

			if ((cycleLogName.equalsIgnoreCase(suiteLogName))&&
				(suiteLogName.equalsIgnoreCase(stepLogName))){
				Log.info("All logging into '"+ cycleLogName + "'; LOGMODE="+ cycleLogMode);
			}
			else{
				Log.info("Separate logs defined for...");
				Log.info("CYCLE LOGNAME='"+ cycleLogName +"'; LOGMODE="+ cycleLogMode);
				Log.info("SUITE LOGNAME='"+ suiteLogName +"'; LOGMODE="+ suiteLogMode);
				Log.info("STEP  LOGNAME='"+ stepLogName +"'; LOGMODE="+ stepLogMode);
			}
		}
		catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient or invalid LOG information:\n"+
			"Cannot properly resolve lognames, logLevel, or logmodes.\n");}
		
		catch(NumberFormatException nfe){
		    throw new IllegalArgumentException("\n"+
		    "Invalid LOGMODE information provided for test log.\n"+
			nfe.getMessage() +"\n");}
	}


	protected GenericToolsInterface getGenericInterface (String configSection, String defaultInterface)
	/**  **/
	                                                    throws ClassNotFoundException, IllegalAccessException,
	                                                           InstantiationException
	{
		String iName = configInfo.getNamedValue(configSection, "Item");
		if (iName==null) iName = defaultInterface;
		iName=StringUtilities.TWhitespace(iName);
		iName=StringUtilities.removeDoubleQuotes(iName);
		return ((GenericToolsInterface) (Class.forName(iName).newInstance()));
	}
	

	/** Initialize all the Driver/Engine interfaces as specified by the config files or 
	 * driver defaults (Status is a SAFSSTATUS object directly instanced).
	 * This driver passes itself (a DriverInterface) to all Configurable tools it 
	 * instances.
	 * (Carl Nagle) 2013.09.23 Watch the order of initialization to be reverse of shutdown. **/
	protected void initializeRuntimeInterface(){

		System.out.println("Instantiating Driver Services interfaces...");
		Log.info("Instantiating Driver Interface...");
		try{
			// first one in, if initializing STAF, must be last one to shutdown.
			input = (InputInterface) getGenericInterface(DriverConstant.SECTION_SAFS_INPUT,
			                                             DriverConstant.DEFAULT_INPUT_INTERFACE);

			maps = (MapsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_MAPS,
			                                             DriverConstant.DEFAULT_MAPS_INTERFACE);

			vars = (VarsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_VARS,
			                                             DriverConstant.DEFAULT_VARS_INTERFACE);

			logs = (LogsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_LOGS,
			                                             DriverConstant.DEFAULT_LOGS_INTERFACE);

			counts = (CountersInterface) getGenericInterface(DriverConstant.SECTION_SAFS_COUNTERS,
			                                             DriverConstant.DEFAULT_COUNTERS_INTERFACE);

			cycleStack = (StacksInterface) getGenericInterface(DriverConstant.SECTION_SAFS_STACKS,
			                                             DriverConstant.DEFAULT_STACKS_INTERFACE);

			suiteStack = (StacksInterface) getGenericInterface(DriverConstant.SECTION_SAFS_STACKS,
			                                             DriverConstant.DEFAULT_STACKS_INTERFACE);

			stepStack = (StacksInterface) getGenericInterface(DriverConstant.SECTION_SAFS_STACKS,
			                                             DriverConstant.DEFAULT_STACKS_INTERFACE);

			status = new SAFSSTATUS();
						                                             
			System.out.println("Driver Services initializing...");
			Log.info("Driver Interface initializing...");
			((ConfigurableToolsInterface)input).launchInterface(this);			
			((ConfigurableToolsInterface)maps).launchInterface(this);			
			((ConfigurableToolsInterface)vars).launchInterface(this);			
			((ConfigurableToolsInterface)logs).launchInterface(this);			
			((ConfigurableToolsInterface)counts).launchInterface(this);						
			((ConfigurableToolsInterface)status).launchInterface(this);	
			
			core = getCoreInterface();
			
			String show = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, DriverConstant.SHOW_MONITOR);
			if(show !=null && show.length()> 0){
				setUseSAFSMonitor(StringUtilities.convertBool(show));
			}					
		}
		catch(ClassNotFoundException cnfe){
		    throw new IllegalArgumentException("\n"+
		    "Unable to locate one or more runtime interface specifications.\n"+
		    cnfe.getMessage() +"\n");}		

		catch(IllegalAccessException iae){
		    throw new IllegalArgumentException("\n"+
		    "Unable to use one or more runtime interface specifications.\n"+
		    iae.getMessage() +"\n");}		

		catch(InstantiationException ie){
		    throw new IllegalArgumentException("\n"+
		    "Unable to create one or more runtime interface specifications.\n"+
		    ie.getMessage() +"\n");}		

		catch(ClassCastException cce){
		    throw new IllegalArgumentException("\n"+
		    "Unable to create one or more runtime interface specifications.\n"+
		    cce.getMessage() +"\n");}		
	}
	
	/** 
	 * Instantiate and initialize any EngineInterface classes (up to 10)listed in the 
	 * SAFS_ENGINE section of the Configuration Source.
	 * <p>
	 * This driver passes itself--a DriverInterface object--to each instanced engine.
	 ***/
	protected void initializeRuntimeEngines(){
		
		if (tidcommands == null) tidcommands = new TIDDriverCommands(this);
		if (autoitcomponent == null) autoitcomponent = new AutoItComponent(this);
		if (tidcomponent == null) tidcomponent = new TIDComponent(this);
		if (ipcommands  == null)  ipcommands = new SAFSDRIVERCOMMANDS(this);
		
		String[] items = {"First", "Second", "Third", "Fourth", "Fifth", 
			              "Sixth", "Seventh", "Eighth", "Ninth", "Tenth"};
		for(int i=0;i<items.length;i++){
			EngineInterface engine = getEngineInterface(items[i]);
			if(! (engine==null)){
				engine.launchInterface(this);
				engines.addElement(engine);
			}
		}		
	}

	/** 
	 * Initialize any preset SAFS variables such as known project directories, etc.
	 * <p>
	 * Known preset variables are:<br/>
	 * <ul>
	 *   <li>"SAFSPROJECTDIRECTORY" -- Root project directory: "C:\MyProject"
	 *   <li>"SAFSDATAPOOLDIRECTORY" -- Project Datapool directory: "C:\MyProject\Datapool"
	 *   <li>"SAFSBENCHDIRECTORY" -- Project Bench directory: "C:\MyProject\Datapool\Bench"
	 *   <li>"SAFSTESTDIRECTORY" -- Project Test\Actual directory: "C:\MyProject\Datapool\Test"
	 *   <li>"SAFSDIFDIRECTORY" -- Project Dif directory: "C:\MyProject\Datapool\Dif"
	 *   <li>"SAFSLOGSDIRECTORY" -- Project Logs directory: "C:\MyProject\Datapool\Logs"
	 *   <li>"SAFSSYSTEMUSERID" -- UserID of the person\account logged onto the system.
	 *   <li>"SAFS_DRIVER_CONTROL" -- "RUNNING".
	 *   <li>"SAFS_DRIVER_CONTROL_POF" -- "OFF".
	 *   <li>"STAF_OCR_ENGINE_VAR_NAME" -- from INI file SAFS_OCR:OCRName
	 *   <li>"STAF_OCR_LANGUAGE_ID_VAR_NAME" -- from INI file SAFS_OCR:LanguageID
	 * </ul>
	 ***/
	protected void initializePresetVariables(){
		if (vars == null) return;
		try{
			vars.setValue("safsprojectdirectory", projectRootDir + File.separatorChar);
			vars.setValue("safsdatapooldirectory", datapoolSource + File.separatorChar);
			vars.setValue("safsbenchdirectory", benchSource + File.separatorChar);
			vars.setValue("safstestdirectory", testSource + File.separatorChar);
			vars.setValue("safsdifdirectory", difSource + File.separatorChar);
			vars.setValue("safslogsdirectory", logsSource + File.separatorChar);	
			vars.setValue("safssystemuserid", System.getProperty("user.name"));
			vars.setValue(DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
			vars.setValue(DRIVER_CONTROL_POF_VAR,JavaHook.PAUSE_SWITCH_OFF);  // set PAUSE_ON_FAILURE off as default
			vars.setValue(DRIVER_CONTROL_POW_VAR,JavaHook.PAUSE_SWITCH_OFF);  // set PAUSE_ON_WARNING off as default

			//set OCR related variables
			String ocrName = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_OCR, "OCRName");
			String languageID = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_OCR, "LanguageID");
			if(ocrName!=null) vars.setValue(OCREngine.STAF_OCR_ENGINE_VAR_NAME, ocrName);
			if(languageID!=null) vars.setValue(OCREngine.STAF_OCR_LANGUAGE_ID_VAR_NAME, languageID);			
		}
		catch(Exception x){;}
	}

	/**
	 * Initialize other miscellaneous config info from INI files.
	 * For example, SAFS_IBT  INI values or other class configuration 
	 * options made available through the INI files.
	 * <p>
	 * Currently processed values:
	 * <p>
	 * <ul>From [SAFS_IBT]
	 *   <li>"UseMultiThreadedSearch"
	 *   <li>"ThreadNumber"
	 *   <li>"UsePerImageModifiers"
	 * </ul>
	 * <ul>From [SAFS_DRIVER]
	 *   <li>"ResolveSkippedRecords"
	 *   <li>"PreferredEnginesOverride"
	 *   <li>"BringMonitorToFrontOnPause"
	 *   <li>"TurnOnPOF"
	 * </ul>
	 * 
	 */
	protected void initializeMiscConfigInfo(){
		//set IBT related variables
		String useMultiThread = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_IBT, "UseMultiThreadSearch");
		if(useMultiThread!=null) {
			ImageUtils.USE_MULTIPLE_THREADS = StringUtilities.convertBool(useMultiThread);
			Log.info("SAFS_IBT:UseMultiThreadSearch set to: "+ ImageUtils.USE_MULTIPLE_THREADS);
		}
		String threadNumber = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_IBT, "ThreadNumber");
		if(threadNumber!=null) {
			ImageUtils.DIVIDE_PIECES = Integer.parseInt(threadNumber);
			Log.info("SAFS_IBT:ThreadNumber set to: "+ ImageUtils.DIVIDE_PIECES);
		}
		String modifiers = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_IBT, "UsePerImageModifiers");
		if(modifiers!=null) {
			ImageUtils.USE_PER_IMAGE_MODIFIERS = StringUtilities.convertBool(modifiers);		
			Log.info("SAFS_IBT:UsePerImageModifiers set to: "+ ImageUtils.USE_PER_IMAGE_MODIFIERS);
		}
		//set ResolveSkippedRecords of section SAFS_DRIVER
		String resolveSkippedRecords = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "ResolveSkippedRecords");
		if(resolveSkippedRecords!=null) {
			AbstractInputProcessor.RESOLVE_SKIPPED_RECORDS = StringUtilities.convertBool(resolveSkippedRecords);		
			Log.info("SAFS_DRIVER:ResolveSkippedRecords set to: "+ AbstractInputProcessor.RESOLVE_SKIPPED_RECORDS);
		}
		//set PreferredEnginesOverride of section SAFS_DRIVER
		String preferredOverride = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "PreferredEnginesOverride");
		if(preferredOverride!=null) {
			AbstractInputProcessor.PREFERRED_ENGINES_OVERRIDE = StringUtilities.convertBool(preferredOverride);		
			Log.info("SAFS_DRIVER:PreferredEnginesOverride set to: "+ AbstractInputProcessor.PREFERRED_ENGINES_OVERRIDE);
		}
		//set BringMonitorToFrontOnPause of section SAFS_DRIVER
		String bringMonitorToFrontOnPause = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "BringMonitorToFrontOnPause");
		if(bringMonitorToFrontOnPause!=null){
			SAFSMonitorFrame.shownOnFrontWhenPause = StringUtilities.convertBool(bringMonitorToFrontOnPause);
			Log.info("SAFS_DRIVER:BringMonitorToFrontOnPause set to: "+ SAFSMonitorFrame.shownOnFrontWhenPause);
		}
		//set TurnOnPOF of section SAFS_DRIVER
		String setOnPOF = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "TurnOnPOF");
		if(setOnPOF!=null && safsmonitor != null){
			boolean turnon = StringUtilities.convertBool(setOnPOF);
			safsmonitor.setSwitchOfPause(turnon, DriverInterface.DRIVER_CONTROL_POF_VAR);
			Log.info("SAFS_DRIVER:TurnOnPOF set to: "+ turnon);
		}
		//set TurnOnPOW of section SAFS_DRIVER
		String setOnPOW = configInfo.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "TurnOnPOW");
		if(setOnPOW!=null && safsmonitor != null){
			boolean turnon = StringUtilities.convertBool(setOnPOW);
			safsmonitor.setSwitchOfPause(turnon, DriverInterface.DRIVER_CONTROL_POW_VAR);
			Log.info("SAFS_DRIVER:TurnOnPOW set to: "+ turnon);
		}
	}
	
	/** 
	 * Initialize a log and set the loglevel via the LogsInterface.
	 * The logName serves as the unique ID.  Text logs and xml logs are named 
	 * with ".txt" and ".xml" suffixes on the logName.**/	
	protected UniqueStringLogInfo initLog(String logname, long logmodes, String loglevel){
		UniqueStringLogInfo logInfo = new UniqueStringLogInfo(logname, 
		                                                      logname +".txt", 
		                                                      logname +".xml",
		                                                      loglevel,
		                                                      logmodes,
		                                                      null);
	    logs.initLog(logInfo);
	    return logInfo;
	}
	
	/** Instantiate unique logs. **/
	protected void openTestLogs(){
		int logcount = 0;
		if (cycleLogName != null) {
			logcount++;
			cycleLog = initLog(cycleLogName, cycleLogMode, logLevel);}

		if ((suiteLogName != null) &&
		    (!(suiteLogName.equalsIgnoreCase(cycleLogName)))){
			logcount++;
			suiteLog = initLog(suiteLogName, suiteLogMode, logLevel);}

		if ((stepLogName != null) &&
		    (!(stepLogName.equalsIgnoreCase(cycleLogName))) &&
		    (!(stepLogName.equalsIgnoreCase(suiteLogName)))){
			logcount++;
			stepLog = initLog(stepLogName, stepLogMode, logLevel);}
			
		if (logcount==0)
		    throw new IllegalArgumentException(
			"Error: No valid LOG definitions could be initialized!");
	}
	
	
	/** Close logs initialized with openTestLogs.**/
	protected void closeTestLogs(){
		if (cycleLog != null) logs.closeLog(cycleLog);
		if (suiteLog != null) logs.closeLog(suiteLog);
		if (stepLog  != null) logs.closeLog(stepLog);
		try{Thread.sleep(100);}catch(Throwable t){}
		cycleLog=null;
		suiteLog=null;
		stepLog=null;
	}
	
	/** shutdown any engines started with initializeRuntimeEngines() **/
	protected void shutdownRuntimeEngines(){
		
		Enumeration enumerator = engines.elements();
		while(enumerator.hasMoreElements()) {
			EngineInterface engine = (EngineInterface) enumerator.nextElement();
			engine.shutdown();
			try{Thread.sleep(100);}catch(Throwable t){}			
		}
		engines.removeAllElements();
		enginePreference.clear();
		engineObjects.clear();

		if(tidcommands !=null) tidcommands.shutdown();
		if(tidcomponent !=null) tidcomponent.shutdown();
		if(autoitcomponent !=null) autoitcomponent.shutdown();
		if(ipcommands != null) ipcommands.shutdown();
	}

	/** shutdown interfaces started with initializeRuntimeInterfaces() 
	 * (Carl Nagle) 2013.09.23 Change the order of shutdown to be reverse of initialization. **/	
	protected void shutdownRuntimeInterface(){

		try{((GenericToolsInterface)cycleStack).shutdown();}catch(NullPointerException ignore){}
		try{((GenericToolsInterface)suiteStack).shutdown();}catch(NullPointerException ignore){}
		try{((GenericToolsInterface)stepStack).shutdown(); }catch(NullPointerException ignore){}
		try{status.shutdown();                             }catch(NullPointerException ignore){}
		try{((GenericToolsInterface)vars).shutdown();      }catch(NullPointerException ignore){}
		try{Thread.sleep(100);}catch(Throwable t){}
		try{((GenericToolsInterface)maps).shutdown();      }catch(NullPointerException ignore){}
		try{Thread.sleep(100);}catch(Throwable t){}
		try{((GenericToolsInterface)counts).shutdown();    }catch(NullPointerException ignore){}
		try{Thread.sleep(100);}catch(Throwable t){}
		try{((GenericToolsInterface)logs).shutdown();      }catch(NullPointerException ignore){}
		try{Thread.sleep(100);}catch(Throwable t){}
		// last one to shutdown with STAF, should be the first one initialized.
		try{((GenericToolsInterface)input).shutdown();     }catch(NullPointerException ignore){}
		try{Thread.sleep(100);}catch(Throwable t){}

		input      = null;
		maps       = null;
		vars       = null;
		logs       = null;
		counts     = null;
		status     = null;
		cycleStack = null;
		suiteStack = null;
		stepStack  = null;
	}
	
	/** 
	 * Returns the separator based on the current testLevel.
	 * Returns the DriverConstant.DEFAULT_FIELD_SEPARATOR if no other can be determined.*/
	protected String getTestLevelSeparator(String testlevel){
		
		String separator = DriverConstant.DEFAULT_FIELD_SEPARATOR;
		
		if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL)){
			if (cycleSeparator != null) separator = cycleSeparator;}
		else if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL)){
			if (suiteSeparator != null) separator = suiteSeparator;}
		else if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL)){
			if (stepSeparator != null) separator = stepSeparator;}
			
		return separator;
	}

	/** 
	 * Returns the logid based on the current testLevel.*/
	protected String getLogID(String testlevel){		
		if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL)){
			return cycleLogName;}
		else if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL)){
			return suiteLogName;}
		return stepLogName;
	}
	
	/**
	 * Only launches if there is already not one running from another process.
	 */
	protected void launchSAFSMonitor(){
		if(! core.isToolAvailable(SAFSMonitorFrame.STAF_PROCESS_ID) &&
			 useSAFSMonitor){
		    System.out.println("Launching SAFS Monitor...");
		    safsmonitor = new SAFSMonitorFrame();
		    safsmonitor.setDriver(this);
		}
	}
	
	/** 
	 * Bootstrap a newly instanced driver.
	 * The routine calls all the other routines to prepare the the test, execute the 
	 * test, and shutdown the test.  This routine must be overridden by subclasses if 
	 * they wish to change default start-to-finish execution flow.
	 * <p>
	 * The model for overall driver operation is that any command-line arguments or 
	 * configuration file arguments that prevent normal execution will generate an 
	 * IllegalArgumentException.  Those IllegalArgumentExceptions are caught here and 
	 * sent to stderr output.  We then return immediately from this function.
	 * <p>
	 * <ul>Calls:
	 * <li>{@link #validateRootConfigureParameters(boolean)}
	 * <li>{@link #validateTestParameters()}
	 * <li>{@link #validateLogParameters()}
	 * <li>{@link #initializeRuntimeInterface()}
	 * <li>{@link #launchSAFSMonitor()}
	 * <li>{@link #initializePresetVariables()}
	 * <li>{@link #initializeMiscConfigInfo()}
	 * <li>{@link #initializeRuntimeEngines()}
	 * <li>{@link #openTestLogs()}
	 * <p>
	 * <li>{@link #processTest()}
	 * <p>
	 * <li>{@link #closeTestLogs()}
	 * <li>{@link #shutdownRuntimeEngines()}
	 * <li>{@link #shutdownRuntimeInterface()}	
	 * <li>Shutdown {@link SAFSMonitorFrame#dispose()}
	 * </ul>
	 * @see SAFSDRIVER#main(String[])
	 **/
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
		    
		    statuscounts = processTest();
		}
		catch(IllegalArgumentException iae){ System.err.println("Driver "+ iae.getClass().getSimpleName()+": "+ iae.getMessage());	}
		    
		catch(Exception catchall){
			System.err.println("\n****  Unexpected CatchAll Exception handler  ****");
			System.err.println(catchall.getMessage());
			catchall.printStackTrace();
		}
		    
		try{
		    closeTestLogs();					// include any CAPPING of XML logs
		}catch(Throwable t){
			System.err.println("Driver CloseTestLogs "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		try{
		    shutdownRuntimeEngines();			// maybe, maybe not.
		}catch(Throwable t){
			System.err.println("Driver shutdownRuntimeEngines "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		try{ 
			if (safsmonitor != null) {
				safsmonitor.setVisible(false);
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
			System.err.println("Driver SAFS Monitor "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		
		try{
		    shutdownRuntimeInterface(); 		// maybe, maybe not.
		}    
		catch(Throwable t){
			System.err.println("Driver shutdownRuntimeInterfaces "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			t.printStackTrace();
		}
		// Carl Nagle moved here so we don't null too soon and cause NullPointerException at safsmonitor.dispose()
		// Commenting out to see if we can get away without nulling at all.
		// safsmonitor = null;
	}	
}

