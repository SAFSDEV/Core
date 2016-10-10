/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer Logs:
 * SEP 21, 2016		Lei Wang	Modified incrementXXXStatus(): check non-standard status code, such as DriverConstant.STATUS_XXX_LOGGED.
 * SEP 22, 2016		Lei Wang	Modified incrementGeneralStatus(): if status is like DriverConstant.STATUS_TESTXXX_LOGGED, increment "test" counter.
 */
package org.safs.tools.drivers;

import java.io.File;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.CoreInterface;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.engines.AutoItComponent;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.logs.UniqueStringMessageInfo;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.status.StatusCounterInterface;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.vars.VarsInterface;

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
	/** {@link org.safs.tools.engines.AutoItComponent} */
	protected AutoItComponent autoitcomponent = null;	
	/** {@link org.safs.tools.engines.TIDDriverCommands} */
	protected EngineInterface          tidcommands = null;
	/** {@link org.safs.tools.engines.TIDComponent} */
	protected EngineInterface          tidcomponent = null;
	/** {@link org.safs.tools.engines.SAFSDRIVERCOMMANDS} */
	protected EngineInterface          ipcommands  = null;
	/** {@link org.safs.tools.status.StatusCounter} */
	protected StatusInterface         statuscounts = new StatusCounter();
	public UniqueStringCounterInfo counterInfo = null;
	
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
	
	// Configuration Information	
	protected ConfigureLocatorInterface locator = null;

	// Directory Information	
	protected String driverRootDir     = null;
	protected String projectRootDir    = null;

	// Directory Information	
	protected String driverConfigPath  = DriverConstant.DEFAULT_CONFIGURE_FILENAME;
	protected String projectConfigPath = DriverConstant.DEFAULT_CONFIGURE_FILENAME;

	
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
	 * Increment General (not Test, means not T, TW, TF) record counts.<br>
	 * There is an exceptional case, the status is like DriverConstant.STATUS_TESTXXX_LOGGED, then<br>
	 * increment the "test"(NOT "general") counter, keep consistent with InputProcessor.<br>
	 * @param status
	 * @see StatusCodes
	 */
	public void incrementGeneralStatus(int status){
		if(counterInfo == null) counterInfo = new UniqueStringCounterInfo(getTestName(), getTestLevel());
		switch(status){		
			case StatusCodes.OK:
				((StatusCounter) statuscounts).incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				((StatusCounter) statuscounts).incrementIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
				((StatusCounter) statuscounts).incrementGeneralFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
				((StatusCounter) statuscounts).incrementGeneralWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				((StatusCounter) statuscounts).incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			//STATUS_TESTXXX_LOGGED should be treated differently, increment the "test"(not "general") counter
			//to keep consistent with InputProcessor
			case DriverConstant.STATUS_TESTSUCCESS_LOGGED:
				((StatusCounter) statuscounts).incrementTestPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
				break;
			case DriverConstant.STATUS_TESTFAILURE_LOGGED:
				((StatusCounter) statuscounts).incrementTestFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_FAILURE);
				break;
			case DriverConstant.STATUS_TESTWARNING_LOGGED:
				((StatusCounter) statuscounts).incrementTestWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_WARNING);
				break;
				
			default:
				((StatusCounter) statuscounts).incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}

	/**
	 * Increment Test Record (for T, TW, TF) counts.
	 * @param status
	 * @see StatusCodes
	 */
	public void incrementTestStatus(int status){
		if(counterInfo == null) counterInfo = new UniqueStringCounterInfo(getTestName(), getTestLevel());
		switch(status){		
			case StatusCodes.OK:
			case DriverConstant.STATUS_TESTSUCCESS_LOGGED:
				((StatusCounter) statuscounts).incrementTestPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_PASS);
				break;
			case StatusCodes.INVALID_FILE_IO:
				((StatusCounter) statuscounts).incrementTestIOFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_IO_FAILURE);
				break;
			case StatusCodes.GENERAL_SCRIPT_FAILURE:
			case StatusCodes.WRONG_NUM_FIELDS:
			case StatusCodes.NO_RECORD_TYPE_FIELD:
			case StatusCodes.UNRECOGNIZED_RECORD_TYPE:
			case DriverConstant.STATUS_TESTFAILURE_LOGGED:
				((StatusCounter) statuscounts).incrementTestFailures();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_FAILURE);
				break;
			case StatusCodes.SCRIPT_WARNING:
			case DriverConstant.STATUS_TESTWARNING_LOGGED:
				((StatusCounter) statuscounts).incrementTestWarnings();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_TEST_WARNING);
				break;
			case StatusCodes.SCRIPT_NOT_EXECUTED:
				((StatusCounter) statuscounts).incrementSkippedRecords();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_SKIPPED_RECORD);
				break;
			default:
				((StatusCounter) statuscounts).incrementGeneralPasses();
				getCountersInterface().incrementAllCounters(counterInfo, CountersInterface.STATUS_GENERAL_PASS);
		}
	}


	/**
	 * @see DriverInterface#getDriverName()
	 */
	public String getDriverName() { return driverName; }

	/**
	 * @see DriverInterface#getTIDDriverCommands()
	 */
	public EngineInterface getTIDDriverCommands() { return tidcommands; }

	/**
	 * @see DriverInterface#getAutoItComponentSupport()
	 */
	public EngineInterface getAutoItComponentSupport() { 
		if (autoitcomponent == null) autoitcomponent = new AutoItComponent(this);
		return autoitcomponent;	}

	
	/**
	 * @see DriverInterface#getTIDGUIlessComponentSupport()
	 */
	public EngineInterface getTIDGUIlessComponentSupport() { return tidcomponent; }
	
	/**
	 * @see DriverInterface#getIPDriverCommands()
	 */
	public EngineInterface getIPDriverCommands() { return ipcommands; }


	/** Capture REQUIRED parameter values.
	 * @return System property value or an empty string if not found.**/	
	protected String getParameterValue(String param){

		return (System.getProperty(param, ""));
	}
	
	/**
	 * Clear System Properties for:
	 * <p><ul>
	 * <li>safs.modified.config
	 * <li>safs.modified.root
	 * </ul>
	 */
	protected static void resetModifiedProperties(){
		System.clearProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG);
		System.clearProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT);
	}	

	protected static boolean isModifiedConfig(){
		String val = System.getProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG);
		return (val != null && val.length() > 0); 
	}
	
	protected static boolean isModifiedRoot(){
		String val = System.getProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT);
		return (val != null && val.length() > 0); 
	}

	/**
	 * Checks to see if the path is (or startswith) an embedded variable.<br>
	 * Embedded variables are in the form %VARIABLE_NAME% and will be sought as System Properties, 
	 * or System Environment variables.
	 * @param rootDir
	 * @return
	 */
	protected String processEmbeddedVariable(String rootDir){
		final String P = "%";
		if(rootDir == null || rootDir.length() < 3) return rootDir;
		if(rootDir.indexOf(P)==rootDir.lastIndexOf(P)) return rootDir;
		int s = rootDir.indexOf(P);
		int e = rootDir.indexOf(P, s+1);
		String varname = rootDir.substring(s+1, e);
		String varvalue = System.getProperty(varname);
		String result = new String(rootDir);
		if(varvalue == null) try{ varvalue = System.getenv(varname);}catch(Exception ignore){}
		if(varvalue != null && varvalue.length() > 0){
			try{ 
				result = varvalue;
				if( (e+1) < rootDir.length()) result += rootDir.substring(e+1);
			}catch(Exception ignore){}
		}
		return result;
	}
	
	
	public String getDriverRootDir() { return driverRootDir; }

	public String getProjectRootDir() { return projectRootDir; }

	/** 
	 * Locate a ConfigureLocatorInterface given the locatorInfo, presumably 
	 * provided from command-line options.
	 * <p>
	 * @exception IllegalArgumentException if appropriate locator class cannot be 
	 * instantiated.**/
	protected ConfigureLocatorInterface getConfigureLocator(String locatorInfo){
		
		ConfigureLocatorInterface locator = null;
		Class locatorClass = null;
		try{		
			locatorClass = Class.forName(locatorInfo);
			locator = (ConfigureLocatorInterface) locatorClass.newInstance();
			return locator;
		}
		catch(ClassNotFoundException cnfe){
			throw new IllegalArgumentException(
			"ClassNotFoundException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
		catch(InstantiationException ie){
			throw new IllegalArgumentException(
			"InstantiationException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
		catch(IllegalAccessException iae){
			throw new IllegalArgumentException(
			"IllegalAccessException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
	}

	/** Initialize or append a ConfigureInterface to existing ones in the search order.**/
	protected void addConfigureInterfaceSource(ConfigureInterface source){
		if (configInfo==null) { configInfo = source; }
		else { if (source!=null) configInfo.addConfigureInterface(source);}		
	}

	
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

	/** 
	 * Verify the existence of Project subdirectories.
	 * The Project root directory must have already been validated with getRootDirectoryInfo.<br>
	 * Note, when attempting to locate relative project paths, the "datapool" directory is 
	 * determined relative to the root project directory.  All other (default) subdirectories are 
	 * sought as relative to the "datapool" directory.  Thus, the "datapool" directory must be 
	 * verified and set prior to any of its subdirectories.
	 * <p>
	 * @param configItem is usually null if the default directory structure is in use.  It 
	 * can contain an alternate directory specification, usually provided from a configuration 
	 * file.  When provided as a relative path, it will be relative to the Project directory, not 
	 * the datapool directory.
	 * @param subdir is the default subdirectory name to use/verify if configItem is null, or 
	 * does not resolve to a valid directory.
	 * @throws NullPointerException if a directory cannot be resolved.**/
	protected String getProjectDirectoryInfo(String configItem, String subdir, boolean datapoolRelative){

		CaseInsensitiveFile fprj = null;
		CaseInsensitiveFile fdat = null;
		CaseInsensitiveFile fdir = null;
		
		fprj = new CaseInsensitiveFile(projectRootDir);
		fdat = new CaseInsensitiveFile(datapoolSource);
		
		// often null if defaults are in use.
		String configPath = null;
		if(DriverConstant.DEFAULT_PROJECT_DATAPOOL.equalsIgnoreCase(subdir)) {
			configPath =(configItem == null) ? fprj.getAbsolutePath() + File.separator + subdir : configItem;
			fdir = new CaseInsensitiveFile(configPath);
			if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fprj, configPath);
			if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fprj, subdir);
			if (! fdir.isDirectory()) {
				configPath = (configItem == null) ? fdat.getAbsolutePath() + File.separator + subdir : configItem;
				fdir = new CaseInsensitiveFile(configPath);
				if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fdat, configPath);
				if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fdat, subdir);
				if (! fdir.isDirectory()) throw new NullPointerException(subdir);
			}
		}
		else { // Bench, Dif, Logs, Test directories
			configPath =(configItem == null) ? fdat.getAbsolutePath() + File.separator + subdir : configItem; 
			fdir = new CaseInsensitiveFile(configPath);
			if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fdat, configPath);
			if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fdat, subdir);
			if (! fdir.isDirectory()) {
				configPath = (configItem == null) ? fprj.getAbsolutePath() + File.separator + subdir : configItem;
				fdir = new CaseInsensitiveFile(configPath);
				if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fprj, configPath);
				if (! fdir.isDirectory()) fdir = new CaseInsensitiveFile( fprj, subdir);				
				if (! fdir.isDirectory()){		
					if(datapoolRelative){
						configPath = (configItem == null) ? 
								     fdat.getAbsolutePath() + File.separator + subdir : 
								     configItem;						
					}
					fdir = new CaseInsensitiveFile(configPath);
					if(fdir.isAbsolute()){
						try{ fdir.mkdirs();}catch(Exception x){
							throw new NullPointerException(x.getClass().getName()+", "+x.getMessage());
						}
						if(! fdir.isDirectory()) throw new NullPointerException(fdir.getAbsolutePath());
					}else{						
						fdir = datapoolRelative ? 
							   new CaseInsensitiveFile(fdat, configPath) : 
							   new CaseInsensitiveFile(fprj, configPath);
						try{ fdir.mkdirs();}catch(Exception x){
							throw new NullPointerException(x.getClass().getName()+", "+x.getMessage());
						}
						if(! fdir.isDirectory()) throw new NullPointerException(fdir.getAbsolutePath());
					}
				}
			}
		}		
		return fdir.getAbsolutePath();		
	}

	/** 
	 * Verify the validity of REQUIRED parameters.
	 * Verify valid driver root.<br>
	 * Verify valid project root.<br>
	 * Verify valid configure locator class.<br>
	 * Verify existence of at least 1 configuration source.<br>
	 * Verify existence of all required Project directories.<br>
	 * Verify existence of test name parameter.<br>
	 * Verify/Create existence of test level.<br>
	 * Verify existence of appropriate test level field separator.<br>
	 * @param datapoolRelative -- if forced to create project subdirectories, create them relative to the Datapool directory.
	 * Otherwise, they will be considered relative to the Project directory.
	 * @throws IllegalArgumentException if insufficient configuration information is 
	 * available from command-line parameters or configuration files.
	 * **/
	protected void validateRootConfigureParameters(boolean datapoolRelative){
		
		// instance a ConfigureLocatorInterface
		locator = getConfigureLocator( System.getProperty
	                                 ( DriverConstant.PROPERTY_SAFS_CONFIG_LOCATOR, 
	                                   DriverConstant.DEFAULT_CONFIGURE_LOCATOR));
		
		// check for existence of driver config
		driverConfigPath = getParameterValue(DriverConstant.PROPERTY_SAFS_DRIVER_CONFIG);
		
		// check for existence of project config
		projectConfigPath = getParameterValue(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG);
		
		// verify required existence of driver root
		driverRootDir = getParameterValue(DriverConstant.PROPERTY_SAFS_DRIVER_ROOT);

		// verify required existence of project root
		projectRootDir = getParameterValue(DriverConstant.PROPERTY_SAFS_PROJECT_ROOT);
		
		resetModifiedProperties();
		
		// check for valid project.alttid
		ConfigureInterface projectConfig3= locator.locateConfigureInterface(
		                                                         "",projectConfigPath);
		Log.info("Project.alttid="+projectConfig3);

		if(isModifiedConfig()) projectConfigPath = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG);			
		if(isModifiedRoot()) projectRootDir = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT);			
		resetModifiedProperties();
		
		// Project.alttid is always first to search
		addConfigureInterfaceSource(projectConfig3);

	    // if we have no projectroot, see if we can get it from config
		if ((projectRootDir.length()==0)&&(configInfo != null)) {
			projectRootDir = configInfo.getNamedValue(
										DriverConstant.SECTION_SAFS_PROJECT, 
		                                "ProjectRoot");
			if (projectRootDir==null) projectRootDir = "";
		}
		
	    // see if we have a projectroot.safstid
		ConfigureInterface projectConfig = locator.locateConfigureInterface(
		                                                         projectRootDir,"");
		Log.info("ProjectRoot.safstid="+projectConfig);

		if(isModifiedConfig()) projectConfigPath = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG);			
		if(isModifiedRoot()) projectRootDir = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT);			
		resetModifiedProperties();
		
		// cannot add to configInfo yet because projectroot.safstid is 
		// subordinate to projectroot.alttid, but it must be found first.
		
		if ((projectRootDir.length()==0)&&(projectConfig != null)) {
			projectRootDir = projectConfig.getNamedValue(
										DriverConstant.SECTION_SAFS_PROJECT, 
		                                "ProjectRoot");
			if (projectRootDir==null) projectRootDir = "";
		}

		// check for embedded %VARIABLE_NAME% at beginning of path
		projectRootDir = processEmbeddedVariable(projectRootDir);
		
	    // see if we have a projectroot.alttid		
		ConfigureInterface projectConfig2= locator.locateConfigureInterface(
		                                                         projectRootDir,
		                                                         projectConfigPath);
		Log.info("ProjectRoot.alttid="+projectConfig2);

		if(isModifiedConfig()) projectConfigPath = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG);			
		if(isModifiedRoot()) projectRootDir = getParameterValue(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT);			
		resetModifiedProperties();
		
		// Projectroot.alttid is always 2nd to search
		// Projectroot.safstid is always 3rd to search
		addConfigureInterfaceSource(projectConfig2);
		addConfigureInterfaceSource(projectConfig);

	    // if we have no projectroot, see if we can get it from config
		if ((projectRootDir.length()==0)&&(configInfo != null)) {
			projectRootDir = configInfo.getNamedValue(
										DriverConstant.SECTION_SAFS_PROJECT, 
		                                "ProjectRoot");
			if (projectRootDir==null) projectRootDir = "";
		}

		// check for embedded %VARIABLE_NAME% at beginning of path
		projectRootDir = processEmbeddedVariable(projectRootDir);
				
		// check for valid driver info
		ConfigureInterface driverConfig3 = locator.locateConfigureInterface(
		                                                        "",driverConfigPath);
		Log.info("Driver.alttid="+driverConfig3);

		// Driver.alttid is always 4th to search
		addConfigureInterfaceSource(driverConfig3);

	    // if we have no driverroot, see if we can get it from config
		if ((driverRootDir.length()==0)&&(configInfo != null)) {
			driverRootDir = configInfo.getNamedValue(
										DriverConstant.SECTION_SAFS_DRIVER, 
		                                "DriverRoot");
			if (driverRootDir==null) driverRootDir = "";
		}
		
		// check for embedded %VARIABLE_NAME% at beginning of path
		driverRootDir = processEmbeddedVariable(driverRootDir);	

		ConfigureInterface driverConfig = locator.locateConfigureInterface(
		                                                        driverRootDir,"");
		Log.info("DriverRoot.safstid="+driverConfig);

		// cannot add to configInfo yet because driverroot.safstid is 
		// subordinate to driverroot.alttid, but it must be found first.
		
		if ((driverRootDir.length()==0)&&(driverConfig != null)) {
			driverRootDir = driverConfig.getNamedValue(
										DriverConstant.SECTION_SAFS_DRIVER, 
		                                "DriverRoot");
			if (driverRootDir==null) driverRootDir = "";
		}
		
		// check for embedded %VARIABLE_NAME% at beginning of path
		driverRootDir = processEmbeddedVariable(driverRootDir);

		ConfigureInterface driverConfig2 = locator.locateConfigureInterface(
		                                                        driverRootDir,
		                                                        driverConfigPath);
		Log.info("DriverRoot.alttid="+driverConfig2);

		// Driverroot.alttid is always 5th to search
		// Driverroot.safstid is always last to search
		addConfigureInterfaceSource(driverConfig2);
		addConfigureInterfaceSource(driverConfig);

	    // if we have no projectroot, see if we can get it from config
		if ((projectRootDir.length()==0)&&(configInfo != null)) {
			projectRootDir = configInfo.getNamedValue(
										DriverConstant.SECTION_SAFS_PROJECT, 
		                                "ProjectRoot");
			if (projectRootDir==null) projectRootDir = "";
		}
		
		// check for embedded %VARIABLE_NAME% at beginning of path
		projectRootDir = processEmbeddedVariable(projectRootDir);

		// if we have no driverroot, see if we can get it from config
		if ((driverRootDir.length()==0)&&(configInfo != null)) {
			driverRootDir = configInfo.getNamedValue(
										DriverConstant.SECTION_SAFS_DRIVER, 
		                                "DriverRoot");
			if (driverRootDir==null) driverRootDir = "";
		}
		
		// check for embedded %VARIABLE_NAME% at beginning of path
		driverRootDir = processEmbeddedVariable(driverRootDir);		
		
		// throw us out if no config file has been found
		if (configInfo == null)
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
		    "driverRootDir="+driverRootDir+"\n"+
		    "driverConfigPath="+driverConfigPath+"\n"+
		    "projectRootDir="+projectRootDir+"\n"+
		    "projectConfigPath="+projectConfigPath+"\n");
		
		//validate the ROOT paths directly, or through the ConfigureInterface(s)
		try{
			projectRootDir = getRootDirectoryInfo (projectRootDir,
			                                      DriverConstant.SECTION_SAFS_PROJECT, 
			                                      "ProjectRoot");

			// check for embedded %VARIABLE_NAME% at beginning of path
			projectRootDir = processEmbeddedVariable(projectRootDir);

			Log.info("Project RootDir: "+ projectRootDir);
		} catch(NullPointerException npe){
			
			try{ 
				System.out.println("Attempting to deduce unknown Project Root Directory...");
				String[] paths = projectConfig3.getConfigurePaths().split(File.pathSeparator);
				boolean finished = false;
				String apath = null;
				String aname = null;
				File aparent = null;
				String datadir = configInfo.getNamedValue (DriverConstant.SECTION_SAFS_DIRECTORIES, "DataDir");
				if (datadir == null) datadir = DriverConstant.DEFAULT_PROJECT_DATAPOOL;						
				for(int p=0;p<paths.length && !finished;p++){
					apath = paths[p];
					System.out.println("Evaluating config path "+ apath);
					aparent = new CaseInsensitiveFile(apath).getParentFile();
					aname = aparent.getAbsolutePath();
					System.out.println("Evaluating config file path "+ aname);
					if(aname.endsWith(".")){
						aparent = aparent.getParentFile();
						aname = aparent.getAbsolutePath();
						System.out.println("Evaluating parent config file path "+ aname);
					}
					File[] childs = aparent.listFiles();
					for(File child:childs){
						if(child.getName().equalsIgnoreCase(datadir)){
							projectRootDir = aparent.getAbsolutePath();
							finished= true;
							break;
						}
					}
				}				
				System.out.println("Salvaged Project RootDir: "+ projectRootDir);
			}catch(Exception x){
			    throw new IllegalArgumentException("\n"+
			    "Insufficient configuration information available:\n"+
				"Cannot resolve valid Project RootDir: " + projectRootDir + "\n");
			}
		}
		try{
			driverRootDir = getRootDirectoryInfo (driverRootDir, 
			                                      DriverConstant.SECTION_SAFS_DRIVER, 
			                                      "DriverRoot");
			
			// check for embedded %VARIABLE_NAME% at beginning of path
			driverRootDir = processEmbeddedVariable(driverRootDir);

			Log.info("Driver  RootDir: "+ driverRootDir);
			
		} catch(NullPointerException npe){			
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Driver  RootDir: "+ driverRootDir + "\n");
		}
		try{
			datapoolSource = getProjectDirectoryInfo (configInfo.getNamedValue (
			                           DriverConstant.SECTION_SAFS_DIRECTORIES, "DataDir"),
			                           DriverConstant.DEFAULT_PROJECT_DATAPOOL, false);
			Log.info(" Datapool Store: "+ datapoolSource);
		} catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Datapool Store: "+ datapoolSource + "\n");
		}
		try{			                                           
			benchSource = getProjectDirectoryInfo (configInfo.getNamedValue (
			                           DriverConstant.SECTION_SAFS_DIRECTORIES, "BenchDir"),
			                           DriverConstant.DEFAULT_PROJECT_BENCH, 
			                           datapoolRelative);
			Log.info("    Bench Store: "+ benchSource);
		} catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Bench Store: "+ benchSource + "\n");
		}
		try{
			                                           
			difSource = getProjectDirectoryInfo (configInfo.getNamedValue (
			                           DriverConstant.SECTION_SAFS_DIRECTORIES, "DiffDir"),
			                           DriverConstant.DEFAULT_PROJECT_DIF,
			                           datapoolRelative);
			Log.info("      Dif Store: "+ difSource);
			                                           
		} catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Dif Store: "+ difSource + "\n");
		}
		try{
			logsSource = getProjectDirectoryInfo (configInfo.getNamedValue (
			                           DriverConstant.SECTION_SAFS_DIRECTORIES, "LogDir"),
			                           DriverConstant.DEFAULT_PROJECT_LOGS,
			                           datapoolRelative);
			Log.info("     Logs Store: "+ logsSource);
		} catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Logs Store: "+ logsSource + "\n");
		}
		try{
			                                           
			testSource = getProjectDirectoryInfo (configInfo.getNamedValue (
			                           DriverConstant.SECTION_SAFS_DIRECTORIES, "TestDir"),
			                           DriverConstant.DEFAULT_PROJECT_TEST, 
			                           datapoolRelative);
			Log.info("   Test/Actuals: "+ testSource);
		} catch(NullPointerException npe){
		    throw new IllegalArgumentException("\n"+
		    "Insufficient configuration information available:\n"+
			"Cannot resolve valid Test/Actuals: "+ testSource + "\n");
		}

	}
	
	
	/** 
	 * Verify the existence of a "root" directory, like the Driver root or Project root.
	 * First tries based on stored command-line parameter.  If that is not found, then we 
	 * attempt to lookup valid path information in configuration files.
	 * @return the string path to a directory verified to exist.
	 * @throws NullPointerException if no valid directory can be found.*/
	protected String getRootDirectoryInfo(String store, String configSection, String configItem){

		CaseInsensitiveFile fDir = new CaseInsensitiveFile(store);
		if (! fDir.isDirectory()){
			//may return null if not found
			String rootDir = configInfo.getNamedValue(configSection, configItem);
			fDir = new CaseInsensitiveFile(rootDir);
			if (! fDir.isDirectory()) throw new NullPointerException();
		}
		return fDir.getAbsolutePath();		
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
	public void logMessage(String msg, String msgdescription, int msgtype){
		String logname = cycleLogName != null ? cycleLogName :
			             suiteLogName != null ? suiteLogName :
			             stepLogName;
		UniqueStringMessageInfo msgInfo = new UniqueStringMessageInfo(
											  logname,
											  msg, msgdescription, msgtype);
											  
		getLogsInterface().logMessage(msgInfo);
	}
}

