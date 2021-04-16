/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * APR 18, 2018		Lei Wang	Implemented methods of Versionable interface.
 */
package org.safs.tools.drivers;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.safs.DCTestRecordHelper;
import org.safs.StatusCodes;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.engines.AutoItComponent;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.engines.TIDDriverCommands;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.status.StatusInterface;
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

	protected UniqueStringCounterInfo counterInfo = null;

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
	@Override
	public int getMillisBetweenRecords() { return driver.getMillisBetweenRecords();}

	/**
	 * @see DriverInterface#setMillisBetweenRecords(int)
	 */
	@Override
	public void setMillisBetweenRecords(int millisBetweenRecords) { driver.setMillisBetweenRecords(millisBetweenRecords);}

	/**
	 * @see DriverInterface#isExitSuite()
	 */
	@Override
	public boolean isExitSuite() { return driver.isExitSuite(); }

	/**
	 * @see DriverInterface#setExitSuite(boolean)
	 */
	@Override
	public void setExitSuite(boolean enabled) { driver.setExitSuite(enabled); }

	/**
	 * @see DriverInterface#isExitCycle()
	 */
	@Override
	public boolean isExitCycle() { return driver.isExitCycle(); }

	/**
	 * @see DriverInterface#setExitCycle(boolean)
	 */
	@Override
	public void setExitCycle(boolean enabled) { driver.setExitCycle(enabled); }

	/**
	 * @see DriverInterface#setPerTableFlowControl()
	 */
	@Override
	public void setPerTableFlowControl (boolean enabled){ driver.setPerTableFlowControl(enabled);}

	/**
	 * @see DriverInterface#isPerTableFlowControl()
	 */
	@Override
	public boolean isPerTableFlowControl (){ return driver.isPerTableFlowControl();}

	/**
	 * @see DriverInterface#getConfigureInterface()
	 */
	@Override
	public ConfigureInterface getConfigureInterface() {return driver.getConfigureInterface();}

	/**
	 * @see DriverInterface#getInputInterface()
	 */
	@Override
	public InputInterface getInputInterface() {	return driver.getInputInterface();	}

	/**
	 * @see DriverInterface#getMapsInterface()
	 */
	@Override
	public MapsInterface getMapsInterface() { return driver.getMapsInterface();	}

	/**
	 * @see DriverInterface#getVarsInterface()
	 */
	@Override
	public VarsInterface getVarsInterface() { return driver.getVarsInterface();	}

	/**
	 * @see DriverInterface#getLogsInterface()
	 */
	@Override
	public LogsInterface getLogsInterface() { return driver.getLogsInterface();	}

	/**
	 * @see DriverInterface#getCountersInterface()
	 */
	@Override
	public CountersInterface getCountersInterface() { return driver.getCountersInterface();	}

	/**
	 * @see DriverInterface#getDebugInterface()
	 */
	@Override
	public DebugInterface getDebugInterface() { return driver.getDebugInterface();	}

	/**
	 * We provide our own local FlowControlInterface object if per/table flow control is enabled.
	 * Otherwise, we provide the Driver's default FlowControlInterface object.
	 * @see DriverInterface#getFlowControlInterface()
	 */
	@Override
	public FlowControlInterface getFlowControlInterface(String testlevel) {
		if (isPerTableFlowControl()){
	    	return localFlowControlInterface;}
	    else{
			return driver.getFlowControlInterface(testlevel);}
	}

	/**
	 * @see DriverInterface#getStatusInterface()
	 */
	@Override
	public StatusInterface getStatusInterface(){ return statusCounter;	}

	/**
	 * @see DriverInterface#addStatusCounts(StatusInterface)
	 */
	@Override
	public StatusInterface addStatusCounts(StatusInterface incstatus){
		statusCounter.addStatus(incstatus);
		return statusCounter;	}


	/**
	 * @see DriverInterface#getEngines()
	 */
	@Override
	public ListIterator getEngines() { return driver.getEngines();}


	/**
	 * @see DriverInterface#hasEnginePreferences()
	 */
	@Override
	public boolean hasEnginePreferences() { return driver.hasEnginePreferences();}

	/**
	 * @see DriverInterface#getEnginePreferences()
	 */
	@Override
	public ListIterator getEnginePreferences() { return driver.getEnginePreferences();}

	/**
	 * @see DriverInterface#startEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	@Override
	public void startEnginePreference(String key) throws IllegalArgumentException
	{ driver.startEnginePreference(key);}

	/**
	 * @see DriverInterface#endEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	@Override
	public void endEnginePreference(String key) throws IllegalArgumentException
	{ driver.endEnginePreference(key);}

	/**
	 * @see DriverInterface#clearEnginePreferences()
	 */
	@Override
	public void clearEnginePreferences(){ driver.clearEnginePreferences();}

	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	@Override
	public boolean isPreferredEngine(String key) throws IllegalArgumentException
	{ return driver.isPreferredEngine(key);}

	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	@Override
	public boolean isPreferredEngine(EngineInterface engine) throws IllegalArgumentException
	{ return driver.isPreferredEngine(engine);}

	/**
	 * @see DriverInterface#getPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	@Override
	public EngineInterface getPreferredEngine(String key) throws IllegalArgumentException
	{ return driver.getPreferredEngine(key);}

	/**
	 * Retrieves our stored TestRecordData.
	 */
	public TestRecordData getTestRecordData() { return testRecordData;}

	/**
	 * @see DriverInterface#getDriverName()
	 */
	@Override
	public String getDriverName() { return driver.getDriverName();	}

	/**
	 * We override the Driver's TIDDriverCommands object and provide our own instance
	 * since the InputProcessor instance needs to override some values the TIDDriverCommands
	 * object may query.  Such as retrieving local FlowControlInterface objects, etc..
	 * @see DriverInterface#getTIDDriverCommands()
	 */
	@Override
	public EngineInterface getTIDDriverCommands() {
		if (tidDriverCommands == null) tidDriverCommands = new TIDDriverCommands(this);
		return tidDriverCommands;	}

	/**
	 * We provide AutoIt Component Function support.
	 *
	 * @see DriverInterface#getAutoItComponentSupport()
	 */
	@Override
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
	@Override
	public EngineInterface getTIDGUIlessComponentSupport() {
		if (tidComponentCommands == null) tidComponentCommands = new TIDComponent(this);
		return tidComponentCommands;	}

	/**
	 * @see DriverInterface#getIPDriverCommands()
	 */
	@Override
	public EngineInterface getIPDriverCommands() { return driver.getIPDriverCommands();	}

	/**
	 * @see DriverInterface#getDriverRootDir()
	 */
	@Override
	public String getDriverRootDir() { return driver.getDriverRootDir(); }

	/**
	 * @see DriverInterface#getProjectRootDir()
	 */
	@Override
	public String getProjectRootDir() { return driver.getProjectRootDir();	}

	/**
	 * @see DriverInterface#getDatapoolDir()
	 */
	@Override
	public String getDatapoolDir() { return driver.getDatapoolDir(); }

	/**
	 * @see DriverInterface#getBenchDir()
	 */
	@Override
	public String getBenchDir() { return driver.getBenchDir(); }

	/**
	 * @see DriverInterface#getDifDir()
	 */
	@Override
	public String getDifDir() { return driver.getDifDir();	}

	/**
	 * @see DriverInterface#getLogsDir()
	 */
	@Override
	public String getLogsDir() { return driver.getLogsDir(); }

	/**
	 * @see DriverInterface#getTestDir()
	 */
	@Override
	public String getTestDir() { return driver.getTestDir(); }

	/**
	 * @see DriverInterface#getTestName()
	 */
	@Override
	public String getTestName() { return driver.getTestName();	}

	/**
	 * @see DriverInterface#getTestLevel()
	 */
	@Override
	public String getTestLevel() { return driver.getTestLevel(); }

	/**
	 * @see DriverInterface#getCycleSuffix()
	 */
	@Override
	public String getCycleSuffix() { return driver.getCycleSuffix(); }

	/**
	 * @see DriverInterface#getCycleSeparator()
	 */
	@Override
	public String getCycleSeparator() { return driver.getCycleSeparator(); }

	/**
	 * @see DriverInterface#getSuiteSuffix()
	 */
	@Override
	public String getSuiteSuffix() { return driver.getSuiteSuffix(); }

	/**
	 * @see DriverInterface#getSuiteSeparator()
	 */
	@Override
	public String getSuiteSeparator() { return driver.getSuiteSeparator(); }

	/**
	 * @see DriverInterface#getStepSuffix()
	 */
	@Override
	public String getStepSuffix() { return driver.getStepSuffix(); }

	/**
	 * @see DriverInterface#getStepSeparator()
	 */
	@Override
	public String getStepSeparator() { return driver.getStepSeparator(); }

	/**
	 * @see DriverInterface#getLogLevel()
	 */
	@Override
	public String getLogLevel() { return driver.getLogLevel(); }

	/**
	 * @see DriverInterface#getCycleLogName()
	 */
	@Override
	public String getCycleLogName() { return driver.getCycleLogName(); }

	/**
	 * @see DriverInterface#getCycleLogMode()
	 */
	@Override
	public long getCycleLogMode() { return driver.getCycleLogMode(); }

	/**
	 * @see DriverInterface#getSuiteLogName()
	 */
	@Override
	public String getSuiteLogName() { return driver.getSuiteLogName(); }

	/**
	 * @see DriverInterface#getSuiteLogMode()
	 */
	@Override
	public long getSuiteLogMode() { return driver.getSuiteLogMode(); }

	/**
	 * @see DriverInterface#getStepLogName()
	 */
	@Override
	public String getStepLogName() { return driver.getStepLogName(); }

	/**
	 * @see DriverInterface#getStepLogMode()
	 */
	@Override
	public long getStepLogMode() { return driver.getStepLogMode(); }


	/**
	 * @see DriverInterface#isExpressionsEnabled()
	 */
	@Override
	public boolean isExpressionsEnabled(){ return driver.isExpressionsEnabled(); }

	/**
	 * @see DriverInterface#setExpressionsEnabled(boolean)
	 */
	@Override
	public void setExpressionsEnabled(boolean enabled)
	{ driver.setExpressionsEnabled(enabled); }

	/**
	 * @see DriverInterface#setProjectRootDir(String)
	 */
	@Override
	public long setProjectRootDir(String absolute_path){
		return driver.setProjectRootDir(absolute_path);
	}

	/**
	 * @see DriverInterface#setDatapoolDir(String)
	 */
	@Override
	public long setDatapoolDir(String absolute_path){
		return driver.setDatapoolDir(absolute_path);
	}

	/**
	 * @see DriverInterface#setBenchDir(String)
	 */
	@Override
	public long setBenchDir(String absolute_path){
		return driver.setBenchDir(absolute_path);
	}

	/**
	 * @see DriverInterface#setTestDir(String)
	 */
	@Override
	public long setTestDir(String absolute_path){
		return driver.setTestDir(absolute_path);
	}

	/**
	 * @see DriverInterface#setDifDir(String)
	 */
	@Override
	public long setDifDir(String absolute_path){
		return driver.setDifDir(absolute_path);
	}

	/**
	 * @see DriverInterface#setLogsDir(String)
	 */
	@Override
	public long setLogsDir(String absolute_path){
		return driver.setLogsDir(absolute_path);
	}

	/**
	 * @see DriverInterface#getRootVerifyDir()
	 */
	@Override
	public String getRootVerifyDir(){
		return driver.getRootVerifyDir();
	}

	/**
	 * @see DriverInterface#setRootVerifyDir(String)
	 */
	@Override
	public long setRootVerifyDir(String absolute_path){
		return driver.setRootVerifyDir(absolute_path);
	}
	/**
	 * Increment General (not Test) record counts.
	 * @param status
	 * @see StatusCodes
	 */
	@Override
	public void incrementGeneralStatus(int status){
		switch(status){
			case StatusCodes.OK:
				statusCounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				statusCounter.incrementIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
				statusCounter.incrementGeneralFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
				statusCounter.incrementGeneralWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				statusCounter.incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			default:
				statusCounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}

	/**
	 * Increment Test Record counts.
	 * @param status
	 * @see StatusCodes
	 */
	@Override
	public void incrementTestStatus(int status){
		switch(status){
			case StatusCodes.OK:
				statusCounter.incrementTestPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				statusCounter.incrementTestIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
			case StatusCodes.NO_RECORD_TYPE_FIELD:
			case StatusCodes.UNRECOGNIZED_RECORD_TYPE:
				statusCounter.incrementTestFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
				statusCounter.incrementTestWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				statusCounter.incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			default:
				statusCounter.incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}

	/** The version of this driver */
	protected String productName = "Abstract Input Processor";
	/** The version of this driver */
	protected String version = "1.0";
	/** The description of this driver */
	protected String description = "Abstract Input Processor";

	@Override
	public void setProductName(String productName) {
		this.productName = productName;
	}

	@Override
	public void setVersion(String version){
		this.version = version;
	}
	@Override
	public void setDescription(String desc){
		this.description = desc;
	}

	@Override
	public String getProductName(){
		return productName;
	}
	@Override
	public String getVersion(){
		return version;
	}
	@Override
	public String getDescription(){
		return description;
	}

	protected List<EngineInterface> embeddedEngines = new ArrayList<EngineInterface>();
	@Override
	public List<EngineInterface> getEmbeddedEngines(){
		return embeddedEngines;
	}
	@Override
	public void addEmbeddedEngine(EngineInterface embeddedEngine){
		embeddedEngines.add(embeddedEngine);
	}
}
