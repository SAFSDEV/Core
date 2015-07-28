package org.safs.tools.drivers;
import com.ibm.staf.STAFResult;

import java.util.*;
import java.io.*;

import org.safs.GetText;
import org.safs.Log;
import org.safs.logging.*;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.TestRecordData;
import org.safs.staf.STAFProcessHelpers;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.sem.EmbeddedSemService;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.CoreInterface;
import org.safs.tools.logs.*;
import org.safs.tools.stringutils.StringUtilities;
/**
 * Provides a generic, incomplete implementation of a ConfigurableToolsInterface 
 * that requires or expects a DriverInterface as the object used to access 
 * configuration information.  
 * <p>
 * It provides the generic tasks of registering with 
 * STAF using the Driver's name as the process name needed by STAF.  It does this 
 * through the STAFProcessHelpers class so that all associated tools in the 
 * process can use the same process name.
 * <p>
 * Any attempt to launchInterface through this class will also trigger the verification 
 * that STAF is running.  If it is not, the instance will attempt to launch STAF 
 * automatically.  Configuration information can be provided via the ConfigureInterface 
 * as shown below.  Default values will be used if not provided.
 * <p><pre>
 * [STAF]
 * PATH=Full path to/STAFProc     (or bat, script, etc.)
 * CONFIG=Full path to/yourCustom.CFG
 * EMBEDSEM=TRUE
 * </pre>
 * <p>
 * The provided finalizer will unRegister the instance with the STAFProcessHelpers 
 * class to retain proper reference counts in STAFProcessHelpers.
 * 
 * @author CANAGL JAN 27, 2005 Made STAFProc constant compatible with Linux/Unix
 * @author CANAGL DEC 14, 2005 Refactored to support STAF shutdown if weStartedSTAF
 * @author CANAGL AUG 11, 2008 Doubled the time to wait for STAFServiceLoader to come online.
 * @author LEWANG AUG 23, 2010 Add a field LogUtilities: If we can't initialize an engine with a driver,
 *                             we need to set this field to keep working the log Message.
 *                             Add method logMessage(): It will try driver's LogsInterface to log Message;
 *                             If the driver is null, it will try the LogUtilities to log Message.
 * @author CANAGL NOV 13, 2014 Fixed logMessage to try LogUtilities before Driver to get Console logging.
 */
public abstract class DriverConfiguredSTAFInterfaceClass
	implements ConfigurableToolsInterface {

	/** 
	 * "SAFS_DCSIC_PING".
	 * Used when we PING STAF to make sure it is running.
	 */
	public static final String DRIVER_TEMP_PROCESS = "SAFS_DCSIC_PING";
	
	/** 
	 * "STAFProc".
	 * Default executable path to STAF.  This should be multi-platform compatible.
	 */
	public static final String STAFPROC_EXE = "STAFProc";
	
	/** 
	 * The DriverInterface used for all configuration information. 
	 */
	protected DriverInterface    driver      = null;
	
	/**
	 * This LogUtilities is set for logging message if the DriverInterface can't
	 * be initialized.
	 */
	protected LogUtilities log = null;
	
	/** 
	 * The stored DriverName. Primarily saved to unRegister with STAF even if the 
	 * Driver has been destroyed.
	 */
	protected String             processName = null;

	/**************************************************************
	 * Storage for STAF service\client name
	 */
	protected String servicename = "";
	
	
	/** 
	 * The ConfigurInterface extracted from the Driver for easy access.
	 */
	protected ConfigureInterface config      = null;
	
	/** 
	 * The STAFHelper retrieved from STAFProcessHelpers 
	 * during the call to launchInterface. 
	 */
	protected STAFHelper         staf        = null;

	/**
	 * true if the client was running or successfully launched.
	 */
	protected boolean running = false;		
	
	/** 
	 * Flag to record whether the associated STAF service was started by this 
	 * instance (true); or if it was found already running (false).
	 */
	protected boolean weStartedService   = false;

	/** 
	 * Flag to record whether this JVM (DRIVER) started STAF.
	 * If we started STAF, we then may be allowed to shut it down.
	 */
	protected static boolean weStartedSTAF   = false;

	/**
	 * The "where" of our associated STAF service for this instance.
	 * By default, the are on the "local" machine.
	 */
	protected String machine = STAFHelper.LOCAL_MACHINE;
			
	/**
	 * Tracking how many launchInterface calls processed.
	 */
	protected long launchCount = 0;

	/**
	 * Track how many things must be shutdown before we can attempt to shutdown STAF.
	 * We will only attempt to shutdown STAF if weStartedSTAF= true
	 */
	protected long shutdownCount = 0;
	
	/**
	 * Constructor for DriverConfiguredInterfaceClass
	 */
	public DriverConfiguredSTAFInterfaceClass() {
		super();
	}
    
    
    /** "SAFSTextResourceBundle" **/
    public static final String SAFS_RESBUN_NAME = "SAFSTextResourceBundle";
    /** empty. Forces use of default SAFSTextResourceBundle. **/
    protected static final String genericStr = "";//"generic";
    /** empty. Forces use of default SAFSTextResourceBundle. **/
    protected static final String passedStr  = "";//"passed";
    /** "failed". Forces use of failedSAFSTextResourceBundle. **/
    protected static final String failedStr  = "failed";
    /** "failed". Forces use of failedSAFSTextResourceBundle. **/
    protected static final String warningStr = "failed";//"warning";
    /** empty. Forces use of default SAFSTextResourceBundle. **/
    protected static final String otherStr   = "";//"other";
    /** empty. Forces use of default SAFSTextResourceBundle. **/
    protected static final String customStr  = "";//"custom";
    /** empty. Forces use of default SAFSTextResourceBundle. **/
    protected static final String debugStr   = "";//"debug";

    /** GetText instance handling GENERIC text resources. **/
    protected static GetText genericText = new GetText(genericStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling PASSED text resources. **/
    protected static GetText passedText  = new GetText(passedStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling FAILED text resources. **/
    protected static GetText failedText  = new GetText(failedStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling WARNING text resources. **/
    protected static GetText warningText = new GetText(warningStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling ?OTHER? text resources? **/
    protected static GetText otherText   = new GetText(otherStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling CUSTOM text resources. **/
    protected static GetText customText  = new GetText(customStr+SAFS_RESBUN_NAME, Locale.getDefault());
    /** GetText instance handling DEBUG text resources. **/
    protected static GetText debugText   = new GetText(debugStr+SAFS_RESBUN_NAME, Locale.getDefault());
  
    protected void waitForServiceStartCompletion(int seconds) throws IllegalArgumentException{
		int timeout = seconds;
		int loop    = 0;
		boolean running = false;
		
		for(;((loop < timeout)&&(! running));loop++){
			running = staf.isServiceAvailable(servicename);
			if(! running)
			   try{Thread.sleep(1000);}catch(InterruptedException ix){}					
		}
		
		if(! running){
			throw new IllegalArgumentException(
			"Unable to detect "+ servicename +" service in timeout period.");
		}
		else{
			weStartedService = true;
		}
    }
    
    /**
     * If the driver can't be set, the log must be set for logging messages.
     * This method is used for setup the LogUtilities.
     * 
     * In the org.safs.rational.DCDriverCommand (a Processor), we create a new engine
     * org.safs.tools.engines.TIDDriverCommand, but we don't have the driver to initialize
     * a new engine, so we just set the LogUtilizes to keep the log working.
     * 
     * @param log
     */
    public void setLogUtilities(LogUtilities log){
    	this.log = log;
    	log.setCopyLogClass(true);
    }
    
    /**
     * This method is used to setup the LogUtilities and OPTIONALLY setup to copyLogClass.
     * 
     * @param log
     * @param copyLogClass set to true to copy everything to the debug log (Log.class)
     */
    public void setLogUtilities(LogUtilities log, boolean copyLogClass){
    	this.log = log;
    	log.setCopyLogClass(copyLogClass);
    }
    
    /**
     * This method will use try to use the LogUtilities to log message first.
     * If not present, the LogsInterface of a driver to log message will be tried.
     * @param info
     */
    public void logMessage(UniqueStringMessageInfo info){
    	// Try the LogUtilities first to make sure we also attempt tool and console logging.
    	if(log!=null){
       		Log.debug("DCSIC LogUtilities: "+ info.getLogMessage());
    		String rs = log.logMessage(info.getStringID(),info.getLogMessage(),info.getLogMessageDescription(),info.getLogMessageType());
    		if(rs != null){
    			//check for a normal return scenario
        		if( (!rs.startsWith(LogUtilities.LOG_ERROR)) &&
        		    (rs.contains(AbstractSAFSLoggingService.SLS_STATES_CONSOLELOG_PREFIX)))
        			return;
    		}
    	}
    	if(driver!=null){
       		Log.debug("DCSIC LogInterface: "+ info.getLogMessage());
    		driver.getLogsInterface().logMessage(info);
    		return;
    	}
   		Log.debug("DriverInterface and LogUtilities are not fully initialized.");
    }
    /** 
     * Log a standard command FAILED_MESSAGE with detail. Detail can be null.<br/>
     * "your problem failed in table filename at line linenumber."<br/>
     * "detail here"
     * <p>
     * Expects testRecordData to already have filename and lineNumber.**/
    protected void standardErrorMessage(TestRecordData testRecordData, 
                                        String         problem, 
                                        String         detail) {
        String tag = "standard_err";
        String sfile = testRecordData.getFilename();
        String sline = String.valueOf(testRecordData.getLineNumber());

        String message =
            failedText.convert(tag, problem +" failure in table "+ sfile +" at line "+ sline +".",
                         problem, sfile, sline);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.FAILED_MESSAGE);
        logMessage(info);
    }
  

    /** 
     * Log an extended GENERIC_MESSAGE including filename, linenumber, and detail. Detail can be null<br/>
     * "note in table filename at line linenumber."<br/>
     * "detail here"
     * <p>
     * Expects testRecordData to already have filename and lineNumber.**/
    protected void extendedGenericMessage(TestRecordData testRecordData, 
                                        String           note, 
                                        String           detail) {
        String tag = "extended_info";
        String sfile = testRecordData.getFilename();
        String sline = String.valueOf(testRecordData.getLineNumber());

        String message =
            genericText.convert(tag, note +" in table "+ sfile +" at line "+ sline +".",
                         note, sfile, sline);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.GENERIC_MESSAGE);
        logMessage(info);
    }
  

    /** 
     * Log a simple PASSED_MESSAGE with detail. Detail can be null.<br/>
     * "note successful."<br/>
     * "detail here"
     * <p>
     * Expects testRecordData to already have filename and lineNumber.**/
    protected void simpleSuccessMessage(TestRecordData testRecordData, 
                                        String           note, 
                                        String           detail) {
        String tag = "success1";
        String message =
            genericText.convert(tag, note +" successful.", note);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.PASSED_MESSAGE);
        logMessage(info);
    }

    /** 
     * Log a simple GENERIC_MESSAGE with detail. Detail can be null.<br/>
     * "note successful."<br/>
     * "detail here"<br/>
     * Detail should already be localized, if present.
     **/
    protected void simpleGenericSuccessMessage(TestRecordData testRecordData, 
                                        String           note, 
                                        String           detail) {
        String tag = GENStrings.SUCCESS_1;
        String message =
            genericText.convert(tag, note +" successful.", note);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.GENERIC_MESSAGE);
        logMessage(info);
    }

    /** 
     * Log a simple GENERIC_MESSAGE with optional detail.<br/>
     * "your note"<br/>
     * "your detail"<br/>
     * note and detail should already be localized and will be sent unmodified.<br/>
     **/
    protected void simpleGenericMessage(TestRecordData testRecordData, 
                                        String note, String detail) {
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   note,  
                                                                   detail, 
                                                                   AbstractLogFacility.GENERIC_MESSAGE);
        logMessage(info);
    }
    
    public CoreInterface getCoreInterface() throws IllegalStateException {
    	try{
    		if (staf.isInitialized()) return staf;
    	}catch(Exception ignore){ }
    	throw new IllegalStateException("Interface to STAF may be null or has not yet been initialized.");
    }

    /** 
     * Log a simple WARNING_MESSAGE with standard detail.<br/>
     * "your note"<br/>
     * "[command] warning in table [filename] at line [linenumber]."
     * 
     * note should already be localized.<br/>
     * The routine will create the standard warning details.
     * Expects testRecordData to already have command, filename, and lineNumber.**/
    protected void simpleGenericWarningMessage(TestRecordData testRecordData, 
                                        String           note) {
    	String tag = FAILStrings.STANDARD_WARNING;
    	String sfile = testRecordData.getFilename();
        String sline = String.valueOf(testRecordData.getLineNumber());
        String command = testRecordData.getCommand();
        String detail =
            failedText.convert(tag, command +" warning in table "+ sfile +" at line "+ sline +".",
                         command, sfile, sline);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   note,  
                                                                   detail, 
                                                                   AbstractLogFacility.WARNING_MESSAGE);
        logMessage(info);
    }

    /** 
     * Log a simple PASSED_MESSAGE with detail. Detail can be null.<br/>
     * "item action successful."<br/>
     * "detail here"
     * <p>
     * Expects testRecordData to already have filename and lineNumber.**/
    protected void simpleSuccessMessage(TestRecordData testRecordData, 
                                        String           item, 
                                        String           action, 
                                        String           detail) {
        String tag = "success2";
        String message =
            genericText.convert(tag, item + action +" successful.", item, action);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.PASSED_MESSAGE);
        logMessage(info);
    }

    /** 
     * Log a simple PASSED_MESSAGE with detail. Detail can be null.<br/>
     * "note successful using item."<br/>
     * "detail here"
     * <p>
     * Expects testRecordData to already have filename and lineNumber.**/
    protected void simpleSuccessUsingMessage(TestRecordData testRecordData, 
                                        String           note, 
                                        String           item, 
                                        String           detail) {
        String tag = "success2a";
        String message =
            genericText.convert(tag, note +" successful using "+ item, note, item);
        UniqueStringMessageInfo info = new UniqueStringMessageInfo(testRecordData.getFac(), 
                                                                   message,  
                                                                   detail, 
                                                                   AbstractLogFacility.PASSED_MESSAGE);
        logMessage(info);
    }

	/**
	 * Get our STAFProcessHelper.  Launch STAF if it is not running.
	 * Will give us our STAFHelper object once everything is running.
	 * Tries to create a connection to STAF.  If not running, we will attempt to 
	 * launch STAF with a simple execution of STAFProc.exe and set the static 
	 * weStartedSTAF flag to TRUE.  
	 * <p>
	 * If necessary, or desirable, 
	 * the configuration source can specify the full path to an executable, and the 
	 * path to any alternate configuration file desired.  STAFProc will 
	 * use its default configuration file if no other is specified.
	 * <p><pre>
	 * [STAF]
	 * PATH=Full path to/STAFProc.EXE (or shell, bat, script, etc.)
	 * CONFIG=Full path to/yourCustom.CFG
	 * </pre>
	 * @throws IllegalArgumentException if we are unable to return TRUE.
	 */
	public void getSTAFHelper(){

		IllegalArgumentException iax = new IllegalArgumentException(
		    "\n"+ processName +" was unable to register with STAFProcessHelpers.\n"+
		    "Or, we were unable to launch STAF itself.\n");

		try{
			if(!STAFHelper.no_staf_handles){
				String temp = config.getNamedValue(DriverConstant.SECTION_STAF, "NOSTAF");
				if(temp != null) STAFHelper.no_staf_handles = StringUtilities.convertBool(temp);
			}
			staf = STAFProcessHelpers.registerHelper(processName);
			staf.configEmbeddedServices(config); // STAF could have already been started for Debug Log
		}
		catch(SAFSSTAFRegistrationException se){

			System.out.println("Retrieving STAFProc information caused by "+ se.getMessage());
			//se.printStackTrace(System.out);
			Log.info("Retrieving STAFProc information caused by "+se.getMessage());
									
			// launch STAF if not running
			String stafpath = config.getNamedValue(DriverConstant.SECTION_STAF, "PATH");
			if (stafpath==null) stafpath = STAFPROC_EXE;

			String cfgpath  = config.getNamedValue(DriverConstant.SECTION_STAF, "CONFIG");
			
			String[] cmdarray = null;
			
			if (cfgpath==null){
				cmdarray = new String[1];
				cmdarray[0]=stafpath;					
			}
			else{
				cmdarray = new String[2];
				cmdarray[0] = stafpath;
				cmdarray[1] = cfgpath;
			}
			
			try{
				System.out.println("Attempting to start "+ stafpath +" "+ cfgpath);
				Log.info("Attempting to start "+ stafpath +" "+ cfgpath);
				Process proc = Runtime.getRuntime().exec( cmdarray );

				weStartedSTAF = true;
				
				System.out.println("Attempting to register with STAF...");
				//loop til registration works
				int timeout = 60;
				int loop    = 0;
				for(;loop < timeout;loop++){
					try{
						staf = STAFProcessHelpers.registerHelper(processName);
					    break; }
					catch(SAFSSTAFRegistrationException sx){;}
					try{ Thread.sleep(1000);}catch(InterruptedException ix){}
				}
				if (loop==60)
				    throw iax;

				staf.configEmbeddedServices(config);
				
				// wait for service loader to initialize
				System.out.println("Waiting for STAF/ServiceLoader...");
				Log.info("Waiting for STAF/ServiceLoader...");
				boolean ready = false;
				for(loop=0;((loop < (timeout))&&(!ready));loop++){
					if(staf.getSTAFVersion()>2)
						ready = staf.isToolAvailable(staf.STAF_SERVICELOADER_V3);
					else
						ready = staf.isToolAvailable(staf.STAF_SERVICELOADER_V2);
					if(!ready)
					    try{ Thread.sleep(1000);}catch(InterruptedException ix){}
				}
			}
			catch(IOException iox){
				System.err.println(iox.getMessage());
				throw iax;
			}
			catch(SecurityException secx){
				System.err.println(secx.getMessage());
				throw iax;
			}
		}
	}
	
	
	/**
	 * Generic extraction of Driver, Config, and STAFHelper objects.
	 * Each concrete subclass must override to provide their own specific 
	 * interface initialization, but they must call this routine before 
	 * proceeding with their own launch activities using:
	 * <p>
	 * super.launchInterface(configInfo);
	 * <p>
	 * popuplates the driver, processName, config, and staf fields with 
	 * valid objects.  These items are properly unregistered/destroyed in the finalize() 
	 * method.
	 * <p>
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
		
		launchCount++;
		driver      = (DriverInterface) configInfo;
		processName = driver.getDriverName();
		config      = driver.getConfigureInterface();
		getSTAFHelper(); // throws IllegalArgumentException if there is a problem.		
		Log.info(processName +" STAF handle: "+ staf.getHandleNumber());
		shutdownCount++;
		Log.info(processName +" shutdownCount="+ shutdownCount +" for "+ servicename +" launchInterface");
	}

	/**
	 * See if SAFS has an embedded JVM with it.
	 * @return the File object pointing to a valid JVM\bin directory.
	 * @throws FileNotFoundException if we could not locate an embedded JVM Path.
	 */
	public File getEmbeddedJVMBinPath() throws FileNotFoundException{
		String pre = "DCSIC.getEmbeddedJVMBinPath ";
		char s = File.separatorChar;
		try{
			String rootdir = driver.getDriverRootDir();
			File jvm = new CaseInsensitiveFile(rootdir, "jre"+ s +"bin").toFile();
			if(jvm.isDirectory()) return jvm;
		}catch(NullPointerException x){
			throw new FileNotFoundException(pre + x.getClass().getSimpleName()+" "+ x.getMessage());
		}
		throw new FileNotFoundException(pre + " SAFS 'jre"+ s +"bin' subdirectory not found."); 
	}
	
	/**
	 * Configure SAFS Services to use the embedded 32-bit JVM if:
	 * <p><ul>
	 * <li>the shared JSTAF JVM is not already running.
	 * <li>existing configuration OPTION does not explicitly set a different JVM.
	 * <li>SAFS has an embedded JRE (\SAFS\jre\bin\java) exists.
	 * </ul>
	 * @param options already retrieved from config files
	 * @return options with new JVM= param added, or options unmodified.
	 */
	public String configureJSTAFServiceEmbeddedJVMOption(String options){
		final String JSTAF = "jstaf"; 
		try{
			if( ! staf.isServiceLibraryRunning(JSTAF)){
				try{ 
					String lcoptions = options.toLowerCase();
					if(lcoptions.contains(" jvm=")) return options;
					if(lcoptions.contains(" jvmname=")){
						// TODO: determine if that Named JVM is already running?
						// If so, return options;
					}
				}catch(NullPointerException ignore){}
				String mod = "JVM="+getEmbeddedJVMBinPath().getAbsolutePath()+ File.separatorChar + "java";
				if(mod.contains(" ")) mod = staf.lentagValue(mod);
				mod = "OPTION "+ mod;
				return (options==null) ? mod : options +" "+ mod;
			}
		}
		catch(FileNotFoundException ignore){}
		return options;
	}
	
	/**
	 * Provides a default shutdown implementation for services.
	 * Subclasses that are not services should override this method to 
	 * provide the correct shutdown behavior.
	 * @param aname Generally the servicename or client name used in STAF.
	 * Default values passed in are usually the same as the servicename field.
	 */
	protected void shutdownService(String aname){
		// shut it down
		Log.info("Trying to shutdown service: " + aname) ;
		Log.info("Call staf.removeService on " + machine + " to remove SERVICE " + servicename) ;
		staf.removeService(machine, aname);	
		running = false;	
	}
	
	/**
	 * Each concrete subclass may override to provide their own specific 
	 * interface shutdown.  
	 * <p>
	 * This implementation checks to see if 
	 * weStartedService=true.  If so, it will attempt to shutdown the 
	 * servicename through STAF.
	 * <p>
	 * This implementation will also see if weStartedSTAF=true.  If so, then 
	 * it will attempt to decrement shutdownCount.  If shutdownCount reaches 
	 * 0 then it will attempt to shutdown STAF.
	 * @see GenericToolsInterface#shutdown()
	 */
	public void shutdown() {

		if (weStartedService){
			shutdownService(servicename);
			weStartedService = false;
		}
				
		if (weStartedSTAF){
			if(shutdownCount > 0) {
				if (--shutdownCount == 0){
					if (staf != null){
						//weStartedSTAF = false;
						
						staf.shutdownEmbeddedServices();												
						staf.shutDown(machine);
						staf = null;
					}
				}
			}
		}		
	}

	/**
	 * @see GenericToolsInterface#isToolRunning()
	 */
	public boolean isToolRunning() {
		running = staf.isServiceAvailable(servicename);
		return running;
	}


	/** 
	 * finalize if we are ever destroyed without the JVM shutting down.
	 * Most importantly, this unRegisters us through STAFProcessHelpers and reduces 
	 * the STAFHelpers instance count appropriately.
	 */
	protected void finalize() throws Throwable {

		super.finalize();

		try{ if (staf!=null) STAFProcessHelpers.unRegisterHelper(processName);}
		catch(Exception tx){;}			
		
		config=null;
		driver=null;
		staf=null;
	}
}