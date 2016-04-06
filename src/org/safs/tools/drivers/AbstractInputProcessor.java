/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import java.util.ListIterator;
import java.util.Locale;
import org.safs.GetText;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.DCTestRecordHelper;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.engines.AutoItComponent;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.engines.TIDDriverCommands;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.vars.VarsInterface;

/**
 * An abstract implementation of the DriverInterface to be subclassed by 
 * other concrete InputProcessors.
 */
public abstract class AbstractInputProcessor implements DriverInterface {

    /**
     * By default, SKIPPED records (S) do get processed by resolveExpressions.
     * Set to false to bypass expression resolution for SKIPPED records.
     */
    public static boolean RESOLVE_SKIPPED_RECORDS = true;  

    /**
     * Allow preferred engines to override internal process commands.
     * By default, some processors use internal command processors before they resort to 
     * other external engines.  Setting this boolean to TRUE allows a "preferred" engine 
     * to override the implementation of an internal command.
     */
    public static boolean PREFERRED_ENGINES_OVERRIDE = false;
    
	/** The parent DriverInterface app running the test. **/
	protected DriverInterface  driver        = null;

	/** Pass/Fail info for a single instance of an Input processor. **/
	protected StatusCounter    statusCounter = new StatusCounter();
	
	/** Stores input record information for the driver and some engines. **/
	protected TestRecordHelper testRecordData = new DCTestRecordHelper();
	
	/**This allows us to provide per test table error recovery blockIDs 
	 * instead of the globally applied blockID values stored in the Driver. */
	protected FlowControlInterface localFlowControlInterface = new FlowControlInfo();

	/**This allows us to provide per table error recovery blockIDs 
	 * instead of the globally applied blockID values stored in the Driver. */
	protected TIDDriverCommands tidDriverCommands = null;

	/**This allows us to provide local support for non-GUI CF test records. */
	protected TIDComponent tidComponentCommands = null;

	/**This allows us to provide local support AutoIt CF test records. */
	protected AutoItComponent autoitComponentCommands = null;
	
	/**
	 * Constructor for AbstractInputProcessor
	 */
	public AbstractInputProcessor(DriverInterface driver) {
		super();
		this.driver = driver;
	}
	
	/**
	 * @see DriverInterface#getMillisBetweenRecords()
	 */
	public int getMillisBetweenRecords() { return driver.getMillisBetweenRecords();}

	/**
	 * @see DriverInterface#setMillisBetweenRecords(int)
	 */
	public void setMillisBetweenRecords(int millisBetweenRecords) { driver.setMillisBetweenRecords(millisBetweenRecords);}
	
	/**
	 * @see DriverInterface#getNumLockOn()
	 */
	public boolean getNumLockOn() { return driver.getNumLockOn();}
	
	/**
	 * @see DriverInterface#setNumLockOn()
	 */
	public void setNumLockOn(boolean numLockOnValue) { driver.setNumLockOn(numLockOnValue); }
	
	/**
	 * @see DriverInterface#isExitSuite()
	 */
	public boolean isExitSuite() { return driver.isExitSuite(); }

	/**
	 * @see DriverInterface#setExitSuite(boolean)
	 */
	public void setExitSuite(boolean enabled) { driver.setExitSuite(enabled); }

	/**
	 * @see DriverInterface#isExitCycle()
	 */
	public boolean isExitCycle() { return driver.isExitCycle(); }

	/**
	 * @see DriverInterface#setExitCycle(boolean)
	 */
	public void setExitCycle(boolean enabled) { driver.setExitCycle(enabled); }

	/**
	 * @see DriverInterface#setPerTableFlowControl()
	 */
	public void setPerTableFlowControl (boolean enabled){ driver.setPerTableFlowControl(enabled);}
	
	/**
	 * @see DriverInterface#isPerTableFlowControl()
	 */
	public boolean isPerTableFlowControl (){ return driver.isPerTableFlowControl();}
	
	/**
	 * @see DriverInterface#getConfigureInterface()
	 */
	public ConfigureInterface getConfigureInterface() {return driver.getConfigureInterface();}

	/**
	 * @see DriverInterface#getInputInterface()
	 */
	public InputInterface getInputInterface() {	return driver.getInputInterface();	}

	/**
	 * @see DriverInterface#getMapsInterface()
	 */
	public MapsInterface getMapsInterface() { return driver.getMapsInterface();	}

	/**
	 * @see DriverInterface#getVarsInterface()
	 */
	public VarsInterface getVarsInterface() { return driver.getVarsInterface();	}

	/**
	 * @see DriverInterface#getLogsInterface()
	 */
	public LogsInterface getLogsInterface() { return driver.getLogsInterface();	}

	/**
	 * @see DriverInterface#getCountersInterface()
	 */
	public CountersInterface getCountersInterface() { return driver.getCountersInterface();	}

	/**
	 * @see DriverInterface#getDebugInterface()
	 */
	public DebugInterface getDebugInterface() { return driver.getDebugInterface();	}

	/**
	 * We provide our own local FlowControlInterface object if per/table flow control is enabled.
	 * Otherwise, we provide the Driver's default FlowControlInterface object.
	 * @see DriverInterface#getFlowControlInterface()
	 */
	public FlowControlInterface getFlowControlInterface(String testlevel) { 
		if (isPerTableFlowControl()){
	    	return localFlowControlInterface;}
	    else{
			return driver.getFlowControlInterface(testlevel);}	    
	}

	/**
	 * @see DriverInterface#getStatusInterface()
	 */
	public StatusInterface getStatusInterface(){ return statusCounter;	}	

	/**
	 * @see DriverInterface#addStatusCounts(StatusInterface)
	 */
	public StatusInterface addStatusCounts(StatusInterface incstatus){ 
		statusCounter.addStatus(incstatus);
		return statusCounter;	}	

	
	/**
	 * @see DriverInterface#getEngines()
	 */
	public ListIterator getEngines() { return driver.getEngines();}


	/**
	 * @see DriverInterface#hasEnginePreferences()
	 */
	public boolean hasEnginePreferences() { return driver.hasEnginePreferences();}
	
	/**
	 * @see DriverInterface#getEnginePreferences()
	 */
	public ListIterator getEnginePreferences() { return driver.getEnginePreferences();}

	/**
	 * @see DriverInterface#startEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void startEnginePreference(String key) throws IllegalArgumentException
	{ driver.startEnginePreference(key);}

	/**
	 * @see DriverInterface#endEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void endEnginePreference(String key) throws IllegalArgumentException
	{ driver.endEnginePreference(key);}

	/**
	 * @see DriverInterface#clearEnginePreferences()
	 */
	public void clearEnginePreferences(){ driver.clearEnginePreferences();}

	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public boolean isPreferredEngine(String key) throws IllegalArgumentException
	{ return driver.isPreferredEngine(key);}
	
	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public boolean isPreferredEngine(EngineInterface engine) throws IllegalArgumentException
	{ return driver.isPreferredEngine(engine);}
	
	/**
	 * @see DriverInterface#getPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public EngineInterface getPreferredEngine(String key) throws IllegalArgumentException
	{ return driver.getPreferredEngine(key);}

	/**
	 * Retrieves our stored TestRecordData.
	 */
	public TestRecordData getTestRecordData() { return testRecordData;}

	/**
	 * @see DriverInterface#getDriverName()
	 */
	public String getDriverName() { return driver.getDriverName();	}

	/**
	 * We override the Driver's TIDDriverCommands object and provide our own instance 
	 * since the InputProcessor instance needs to override some values the TIDDriverCommands 
	 * object may query.  Such as retrieving local FlowControlInterface objects, etc..
	 * @see DriverInterface#getTIDDriverCommands()
	 */
	public EngineInterface getTIDDriverCommands() { 
		if (tidDriverCommands == null) tidDriverCommands = new TIDDriverCommands(this);
		return tidDriverCommands;	}

	/**
	 * We provide AutoIt Component Function support.  
	 * 
	 * @see DriverInterface#getAutoItComponentSupport()
	 */
	public EngineInterface getAutoItComponentSupport() { 
		if (autoitComponentCommands == null) autoitComponentCommands = new AutoItComponent(this);
		return autoitComponentCommands;	}

	/**
	 * We provide generic support for certain Component Function commands that don't actually 
	 * operate on any GUI objects.  These are generally those CF commands that allow the record 
	 * to specify "anything" "at all" for Window and Component references.
	 * 
	 * @see DriverInterface#getTIDGUIlessComponentSupport()
	 */
	public EngineInterface getTIDGUIlessComponentSupport() { 
		if (tidComponentCommands == null) tidComponentCommands = new TIDComponent(this);
		return tidComponentCommands;	}

	/**
	 * @see DriverInterface#getIPDriverCommands()
	 */
	public EngineInterface getIPDriverCommands() { return driver.getIPDriverCommands();	}

	/**
	 * @see DriverInterface#getDriverRootDir()
	 */
	public String getDriverRootDir() { return driver.getDriverRootDir(); }

	/**
	 * @see DriverInterface#getProjectRootDir()
	 */
	public String getProjectRootDir() { return driver.getProjectRootDir();	}

	/**
	 * @see DriverInterface#getDatapoolDir()
	 */
	public String getDatapoolDir() { return driver.getDatapoolDir(); }

	/**
	 * @see DriverInterface#getBenchDir()
	 */
	public String getBenchDir() { return driver.getBenchDir(); }

	/**
	 * @see DriverInterface#getDifDir()
	 */
	public String getDifDir() { return driver.getDifDir();	}

	/**
	 * @see DriverInterface#getLogsDir()
	 */
	public String getLogsDir() { return driver.getLogsDir(); }

	/**
	 * @see DriverInterface#getTestDir()
	 */
	public String getTestDir() { return driver.getTestDir(); }

	/**
	 * @see DriverInterface#getTestName()
	 */
	public String getTestName() { return driver.getTestName();	}

	/**
	 * @see DriverInterface#getTestLevel()
	 */
	public String getTestLevel() { return driver.getTestLevel(); }

	/**
	 * @see DriverInterface#getCycleSuffix()
	 */
	public String getCycleSuffix() { return driver.getCycleSuffix(); }

	/**
	 * @see DriverInterface#getCycleSeparator()
	 */
	public String getCycleSeparator() { return driver.getCycleSeparator(); }

	/**
	 * @see DriverInterface#getSuiteSuffix()
	 */
	public String getSuiteSuffix() { return driver.getSuiteSuffix(); }

	/**
	 * @see DriverInterface#getSuiteSeparator()
	 */
	public String getSuiteSeparator() { return driver.getSuiteSeparator(); }

	/**
	 * @see DriverInterface#getStepSuffix()
	 */
	public String getStepSuffix() { return driver.getStepSuffix(); }

	/**
	 * @see DriverInterface#getStepSeparator()
	 */
	public String getStepSeparator() { return driver.getStepSeparator(); }

	/**
	 * @see DriverInterface#getLogLevel()
	 */
	public String getLogLevel() { return driver.getLogLevel(); }

	/**
	 * @see DriverInterface#getCycleLogName()
	 */
	public String getCycleLogName() { return driver.getCycleLogName(); }

	/**
	 * @see DriverInterface#getCycleLogMode()
	 */
	public long getCycleLogMode() { return driver.getCycleLogMode(); }

	/**
	 * @see DriverInterface#getSuiteLogName()
	 */
	public String getSuiteLogName() { return driver.getSuiteLogName(); }

	/**
	 * @see DriverInterface#getSuiteLogMode()
	 */
	public long getSuiteLogMode() { return driver.getSuiteLogMode(); }

	/**
	 * @see DriverInterface#getStepLogName()
	 */
	public String getStepLogName() { return driver.getStepLogName(); }

	/**
	 * @see DriverInterface#getStepLogMode()
	 */
	public long getStepLogMode() { return driver.getStepLogMode(); }


	/**
	 * @see DriverInterface#isExpressionsEnabled()
	 */
	public boolean isExpressionsEnabled(){ return driver.isExpressionsEnabled(); }

	/**
	 * @see DriverInterface#setExpressionsEnabled(boolean)
	 */
	public void setExpressionsEnabled(boolean enabled)
	{ driver.setExpressionsEnabled(enabled); }

	/**
	 * @see DriverInterface#setProjectRootDir(String)
	 */
	public long setProjectRootDir(String absolute_path){
		return driver.setProjectRootDir(absolute_path);
	}
			
	/**
	 * @see DriverInterface#setDatapoolDir(String)
	 */
	public long setDatapoolDir(String absolute_path){
		return driver.setDatapoolDir(absolute_path);
	}
	
	/**
	 * @see DriverInterface#setBenchDir(String)
	 */
	public long setBenchDir(String absolute_path){
		return driver.setBenchDir(absolute_path);
	}
		
	/**
	 * @see DriverInterface#setTestDir(String)
	 */
	public long setTestDir(String absolute_path){
		return driver.setTestDir(absolute_path);
	}
	
	/**
	 * @see DriverInterface#setDifDir(String)
	 */
	public long setDifDir(String absolute_path){
		return driver.setDifDir(absolute_path);
	}
	
	/**
	 * @see DriverInterface#setLogsDir(String)
	 */
	public long setLogsDir(String absolute_path){
		return driver.setLogsDir(absolute_path);
	}

	/**
	 * @see DriverInterface#getRootVerifyDir()
	 */
	public String getRootVerifyDir(){
		return driver.getRootVerifyDir();
	}
	
	/**
	 * @see DriverInterface#setRootVerifyDir(String)
	 */
	public long setRootVerifyDir(String absolute_path){
		return driver.setRootVerifyDir(absolute_path);		
	}
}
