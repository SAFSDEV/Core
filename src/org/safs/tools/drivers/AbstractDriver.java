/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import java.io.File;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.CoreInterface;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.vars.VarsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.status.*;
import org.safs.tools.stacks.StacksInterface;

/**
 * The root, abstract implementation of our tool-independent Driver.
 */
public abstract class AbstractDriver implements DriverInterface{

	/**
	 * Name of this Driver: Default = "SAFS/TIDriver" 
	 */	 
	protected String driverName = "SAFS/TIDriver";
	
	// Configuration Information	
	protected ConfigureInterface        configInfo = null;	

	// Driver Interface Information	
	protected InputInterface           input       = null;
	protected MapsInterface            maps        = null;
	protected VarsInterface            vars        = null;
	protected LogsInterface            logs        = null;
	protected CoreInterface            core        = null;
	protected CountersInterface        counts      = null;	
	protected DebugInterface           debug       = new DebugInfo();
	/** {@link org.safs.tools.engines.TIDDriverCommands} */
	protected EngineInterface          tidcommands = null;
	/** {@link org.safs.tools.engines.TIDComponent} */
	protected EngineInterface          tidcomponent = null;
	/** {@link org.safs.tools.engines.SAFSDRIVERCOMMANDS} */
	protected EngineInterface          ipcommands  = null;
	/** {@link org.safs.tools.status.StatusCounter} */
	protected StatusInterface         statuscounts = new StatusCounter();
	protected int            millisBetweenRecords  = 0;
	
	/** 
	 * CYCLE shared flow control info used by all Driver/InputProcessors. 
	 * This may be overridden by local flowcontrol info defined for each separate 
	 * InputProcessor if local flowcontrol mode is ever enabled.
	 */
	protected FlowControlInterface     cycleflowcontrol = new FlowControlInfo();

	/** 
	 * SUITE shared flow control info used by all Driver/InputProcessors. 
	 * This may be overridden by local flowcontrol info defined for each separate 
	 * InputProcessor if local flowcontrol mode is ever enabled.
	 */
	protected FlowControlInterface     suiteflowcontrol = new FlowControlInfo();

	/** 
	 * STEP shared flow control info used by all Driver/InputProcessors. 
	 * This may be overridden by local flowcontrol info defined for each separate 
	 * InputProcessor if local flowcontrol mode is ever enabled.
	 */
	protected FlowControlInterface     stepflowcontrol = new FlowControlInfo();

	/** 
	 * If true then local flow control blocks can be defined for each separate 
	 * test table (InputProcessor).  When true, shared flow control blockIDs 
	 * are disabled in favor of per table flow control.
	 */
	protected boolean  perTableFlowControl = false;
	

	// Directory Information
	
	protected String driverRootDir     = null;
	protected String projectRootDir    = null;

	protected String datapoolSource = DriverConstant.DEFAULT_PROJECT_DATAPOOL;
	protected String verifySource   = DriverConstant.DEFAULT_PROJECT_DATAPOOL;
	protected String benchSource    = DriverConstant.DEFAULT_PROJECT_BENCH;;
	protected String difSource      = DriverConstant.DEFAULT_PROJECT_DIF;
	protected String logsSource     = DriverConstant.DEFAULT_PROJECT_LOGS;
	protected String testSource     = DriverConstant.DEFAULT_PROJECT_TEST;
	

	// Test Information	
	protected String testName  = null;
	protected String testLevel = null;

	protected String cycleSuffix    = DriverConstant.DEFAULT_CYCLE_TESTNAME_SUFFIX;
	protected String cycleSeparator = DriverConstant.DEFAULT_FIELD_SEPARATOR;

	protected String suiteSuffix    = DriverConstant.DEFAULT_SUITE_TESTNAME_SUFFIX;
	protected String suiteSeparator = DriverConstant.DEFAULT_FIELD_SEPARATOR;

	protected String stepSuffix     = DriverConstant.DEFAULT_STEP_TESTNAME_SUFFIX;
	protected String stepSeparator  = DriverConstant.DEFAULT_FIELD_SEPARATOR;

	// Log Information
	protected String logLevel     = DriverConstant.DEFAULT_LOGLEVEL;

	protected String cycleLogName   = null;
	protected long   cycleLogMode   = DriverConstant.DEFAULT_LOGMODE;
	protected String cycleLinkedFac = null;

	protected String suiteLogName   = null;
	protected long   suiteLogMode   = DriverConstant.DEFAULT_LOGMODE;
	protected String suiteLinkedFac = null;

	protected String stepLogName    = null;
	protected long   stepLogMode    = DriverConstant.DEFAULT_LOGMODE;
	protected String stepLinkedFac  = null;

	protected boolean expressionsOn = false;
	protected boolean exitSuite     = false;
	protected boolean exitCycle     = false;

	/**
	 * @see DriverInterface#getMillisBetweenRecords()
	 */
	public int getMillisBetweenRecords() { return millisBetweenRecords;}

	/**
	 * @see DriverInterface#setMillisBetweenRecords(int)
	 */
	public void setMillisBetweenRecords(int millisBetween) { millisBetweenRecords = millisBetween;}
	
	
	/**
	 * @see DriverInterface#isPerTableFlowControl()
	 */
	public boolean isPerTableFlowControl() { return perTableFlowControl; }

	/**
	 * @see DriverInterface#setPerTableFlowControl(boolean)
	 */
	public void setPerTableFlowControl(boolean enabled) {perTableFlowControl = enabled; }

	/**
	 * @see DriverInterface#isExitSuite()
	 */
	public boolean isExitSuite() { return exitSuite; }

	/**
	 * @see DriverInterface#setExitSuite(boolean)
	 */
	public void setExitSuite(boolean enabled) {exitSuite = enabled; }

	/**
	 * @see DriverInterface#isExitCycle()
	 */
	public boolean isExitCycle() { return exitCycle; }

	/**
	 * @see DriverInterface#setExitCycle(boolean)
	 */
	public void setExitCycle(boolean enabled) {exitCycle = enabled; }

	/**
	 * @see DriverInterface#getConfigureInterface()
	 */
	public ConfigureInterface getConfigureInterface() { return configInfo; }

	/**
	 * @see DriverInterface#getInputInterface()
	 */
	public InputInterface getInputInterface() { return input; }

	/**
	 * @see DriverInterface#getMapsInterface()
	 */
	public MapsInterface getMapsInterface() { return maps; }

	/**
	 * @see DriverInterface#getVarsInterface()
	 */
	public VarsInterface getVarsInterface() { return vars; }

	/**
	 * @see DriverInterface#getLogsInterface()
	 */
	public LogsInterface getLogsInterface() { return logs; }

	/**
	 * Attempts to return an interface to the core framework through one of the existing interfaces.
	 * @see DriverInterface#getCoreInterface()
	 */
	public CoreInterface getCoreInterface() { 
		if(core != null) return core;
		if(core == null && (input instanceof GenericToolsInterface))
			try{core = ((GenericToolsInterface)input).getCoreInterface();}catch(Exception ignore){}
		if(core == null && (maps instanceof GenericToolsInterface))
			try{core = ((GenericToolsInterface)maps).getCoreInterface();}catch(Exception ignore){}
		if(core == null && (vars instanceof GenericToolsInterface))
			try{core = ((GenericToolsInterface)vars).getCoreInterface();}catch(Exception ignore){}
		if(core == null && (logs instanceof GenericToolsInterface))
			try{core = ((GenericToolsInterface)logs).getCoreInterface();}catch(Exception ignore){}
		return core;
	}

	/**
	 * @see DriverInterface#getCountersInterface()
	 */
	public CountersInterface getCountersInterface() { return counts; }

	/**
	 * @see DriverInterface#getDebugInterface()
	 */
	public DebugInterface getDebugInterface(){ return debug; }
	
	/**
	 * @see DriverInterface#getFlowControlInterface(String)
	 */
	public FlowControlInterface getFlowControlInterface(String testlevel){ 
		try{
			if(testlevel.equalsIgnoreCase("CYCLE")) return cycleflowcontrol;
			if(testlevel.equalsIgnoreCase("SUITE")) return suiteflowcontrol;
			if(testlevel.equalsIgnoreCase("STEP")) return stepflowcontrol;
		}catch(Exception ex){}
		return new FlowControlInfo();
	}	

	/**
	 * @see DriverInterface#getStatusInterface()
	 */
	public StatusInterface getStatusInterface(){ return statuscounts;	}	

	/**
	 * @see DriverInterface#addStatusCounts(StatusInterface)
	 */
	public StatusInterface addStatusCounts(StatusInterface incstatus){ 
		try{ ((StatusCounterInterface)statuscounts).addStatus(incstatus);}
		catch(Exception ex){;}
		return statuscounts;	}	

	/**
	 * @see DriverInterface#getDriverName()
	 */
	public String getDriverName() { return driverName; }

	/**
	 * @see DriverInterface#getTIDDriverCommands()
	 */
	public EngineInterface getTIDDriverCommands() { return tidcommands; }

	/**
	 * @see DriverInterface#getTIDGUIlessComponentSupport()
	 */
	public EngineInterface getTIDGUIlessComponentSupport() { return tidcomponent; }
	
	/**
	 * @see DriverInterface#getIPDriverCommands()
	 */
	public EngineInterface getIPDriverCommands() { return ipcommands; }

	public String getDriverRootDir() { return driverRootDir; }

	public String getProjectRootDir() { return projectRootDir; }

	public long setProjectRootDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setProjectRootDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			projectRootDir = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_PROJECTDIRECTORY, projectRootDir);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setProjectRootDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getRootVerifyDir(){
		return verifySource;		
	}
	
	public long setRootVerifyDir(String absolute_path) { 
		String _bench = getBenchDir();
		String _dif = getDifDir();
		String _test = getTestDir();
		long status = StatusCodes.GENERAL_SCRIPT_FAILURE;		
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setRootVerifyDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			String _path = dir.getAbsolutePath()+File.separatorChar;
			status = setBenchDir(_path +"Bench"+ File.separatorChar);
			if (status != StatusCodes.NO_SCRIPT_FAILURE){
				//revert all changes back
				setBenchDir(_bench);
				return status;
			}
			status = setTestDir(_path +"Test"+ File.separatorChar);
			if (status != StatusCodes.NO_SCRIPT_FAILURE){
				//revert all changes back
				setBenchDir(_bench);
				setTestDir(_test);
				return status;
			}
			status = setDifDir(_path +"Dif"+ File.separatorChar);
			if (status != StatusCodes.NO_SCRIPT_FAILURE){
				//revert all changes back
				setBenchDir(_bench);
				setTestDir(_test);
				setDifDir(_dif);
				return status;
			}
			verifySource = _path;
			vars.setValue(STAFHelper.SAFS_VAR_ROOTVERIFYDIRECTORY, verifySource);						
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setProjectRootDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getDatapoolDir() { return datapoolSource; }

	public long setDatapoolDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setDatapoolDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			datapoolSource = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY, datapoolSource);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setDatapoolDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getBenchDir() { return benchSource; }
	
	public long setBenchDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setBenchDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			benchSource = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_BENCHDIRECTORY, benchSource);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setBenchDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getDifDir() { return difSource; }

	public long setDifDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setDifDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			difSource = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_DIFDIRECTORY, difSource);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setDifDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getLogsDir() { return logsSource; }

	public long setLogsDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setLogsDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			logsSource = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_LOGSDIRECTORY, logsSource);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setLogsDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}

	public String getTestDir() { return testSource; }

	public long setTestDir(String absolute_path) { 
		try{
			File dir = new CaseInsensitiveFile(absolute_path).toFile();
			if ((!dir.isDirectory())||(!dir.isAbsolute())){
				Log.debug("<Driver>.setTestDir File failure using '"+ absolute_path +"'"); 		
				return StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
			testSource = dir.getAbsolutePath()+ File.separatorChar;
			vars.setValue(STAFHelper.SAFS_VAR_TESTDIRECTORY, testSource);			
			return StatusCodes.NO_SCRIPT_FAILURE;
		}catch(Exception x){ 
			Log.debug("<Driver>.setTestDir Exception using '"+ absolute_path +"'", x); 		
			return StatusCodes.GENERAL_SCRIPT_FAILURE;
		}
	}


	/**
	 * @see DriverInterface#getTestName()
	 */
	public String getTestName() { return testName; }

	/**
	 * @see DriverInterface#getTestLevel()
	 */
	public String getTestLevel() { return testLevel; }

	/**
	 * @see DriverInterface#getCycleSuffix()
	 */
	public String getCycleSuffix() { return cycleSuffix; }

	/**
	 * @see DriverInterface#getCycleSeparator()
	 */
	public String getCycleSeparator() { return cycleSeparator; }

	/**
	 * @see DriverInterface#getSuiteSuffix()
	 */
	public String getSuiteSuffix() { return suiteSuffix; }

	/**
	 * @see DriverInterface#getSuiteSeparator()
	 */
	public String getSuiteSeparator() { return suiteSeparator; }

	/**
	 * @see DriverInterface#getStepSuffix()
	 */
	public String getStepSuffix() { return stepSuffix; }

	/**
	 * @see DriverInterface#getStepSeparator()
	 */
	public String getStepSeparator() { return stepSeparator; }

	/**
	 * @see DriverInterface#getLogLevel()
	 */
	public String getLogLevel() { return logLevel; }

	/**
	 * @see DriverInterface#getCycleLogName()
	 */
	public String getCycleLogName() { return cycleLogName; }

	/**
	 * @see DriverInterface#getCycleLogMode()
	 */
	public long getCycleLogMode() { return cycleLogMode; }

	/**
	 * @see DriverInterface#getSuiteLogName()
	 */
	public String getSuiteLogName() { return suiteLogName; }

	/**
	 * @see DriverInterface#getSuiteLogMode()
	 */
	public long getSuiteLogMode() { return suiteLogMode; }

	/**
	 * @see DriverInterface#getStepLogName()
	 */
	public String getStepLogName() { return stepLogName; }

	/**
	 * @see DriverInterface#getStepLogMode()
	 */
	public long getStepLogMode() { return stepLogMode; }

	
	/**
	 * @see DriverInterface#isExpressionsEnabled()
	 */
	public boolean isExpressionsEnabled(){ return expressionsOn; }

	/**
	 * @see DriverInterface#setExpressionsEnabled(boolean)
	 */
	public void setExpressionsEnabled(boolean enabled)
	{ expressionsOn = enabled; }

	/**********************************************************************************
	 * This is the one that actually opens and loops through our tests records!
	 * Typically, DriverInterface subclasses like SAFSDRIVER will be used to provide 
	 * the concrete implementation.
	 * @see SAFSDRIVER#processTest()
	 */
	protected abstract StatusInterface processTest();
}

