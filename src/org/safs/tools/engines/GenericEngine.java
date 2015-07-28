package org.safs.tools.engines;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.safs.JavaHook;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.RuntimeDataInterface;
import org.safs.tools.consoles.GenericProcessConsole;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.logs.UniqueStringMessageInfo;


/**
 * @author CANAGL DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author CANAGL JUL 28, 2009 Enhanced shutdown operations for stability.
 *         LeiWg  AUG 23, 2010 Modify method logMessage(): call logMessage of its super class.
 *         SBJLWA APR 27, 2012 Modify method shutdownService(): reset all the semaphores of hook.
 *         SBJLWA MAY 03, 2012 Modify method shutdownService(): wait for "shutdown" semaphore and reset it.
 *                             Add method resetShutdownSemaphore(): check the condition before resetting "shutdown" semaphore.
 *                             Modify method launchInterface(): Try to reset "shutdown" semaphore during of launch engine.
 * <br>   NOV 25, 2014    (SBJLWA) Copy method deduceFile() from org.safs.Processor: so that the same logic will be used
 *                                 to deduce test/bench file name.                          
 *         
 */
public class GenericEngine extends DriverConfiguredSTAFInterfaceClass implements EngineInterface, RuntimeDataInterface {

	/** The Java Process running the engine. **/
	protected Process process = null;
	
	/** The Console handling IO streams for the engine process. **/
	protected GenericProcessConsole console = null;
	
	/**
	 * Stores the data on the input record we are to process.
	 */
	protected TestRecordHelper  testRecordData = null;

	/**
	 * Constructor for GenericEngine
	 */
	public GenericEngine() {
		super();
		servicename = "GENERIC_ENGINE";
	}


	/***************************************************************************
	 * Convenience routine for building the appropriate MessageInfo and logging
	 * a message to our active log.  Consult the AbstractLogFacility for valid 
	 * msgtype values.
	 * 
	 * @see org.safs.logging.AbstractLogFacility 
	 */
	protected void logMessage(String msg, String msgdescription, int msgtype){
		UniqueStringMessageInfo msgInfo = new UniqueStringMessageInfo(
											  testRecordData.getFac(),
											  msg, msgdescription, msgtype);
											  
		logMessage(msgInfo);
	}
	

	/**
	 * @see EngineInterface#getEngineName()
	 */
	public String getEngineName() {
		return servicename;
	}

	/**
	 * Initiate the event-driven protocol to send the test record to the engine.
	 * All official engines conform to this protocol.  So this function rarely 
	 * needs to be overridden..
	 * <p>
	 * @see EngineInterface#processRecord()
	 */
	public long processRecord(TestRecordHelper testRecordData) {
		this.testRecordData = testRecordData;
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		try{
			if (running) rc = staf.postNextHookTestEvent(getEngineName(), staf.SAFS_HOOK_TRD, testRecordData);
			if ((rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
				(testRecordData.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
				running = false;
			return rc;
		}
		catch(SAFSException se){
			Log.error(se.getMessage());
		}		
		return rc;
	}

	/**
	 * Typically expects a DriverInterface object for initialization.
	 * Subclasses should indicate if an alternative type of object is required.
	 * 
	 * This method will try to reset the "shutdown" semaphore remained by last hook shutdown.
	 * 
	 * @see DriverConfiguredSTAFInterfaceClass#launchInterface(Object)
	 * @see org.safs.tools.drivers.DriverInterface
	 */
	public void launchInterface(Object configInfo) {
		super.launchInterface(configInfo);
		//Before the driver can call processRecord(), we try to reset the "shutdown"
		//semaphore remained from last hook's shutdown
		if(!isToolRunning()) resetShutdownSemaphore();
	}

	/**
	 * Verifies a STAF tool matching our engine name is running.
	 * @see GenericToolsInterface#isToolRunning()
	 */
	public boolean isToolRunning() {
		try{
			running = staf.isToolAvailable(getEngineName());
		}
		catch(Exception x){ 
			running = false;
		}
		return running;
	}

	/**
	 * Wraps the privided text in double-quotes *if* the text contains a space.
	 * @return provided text item unmodified if null or no spaces found.  New string 
	 *         wrapped in quotes if space(s) found.
	 */
	protected String makeQuotedString(String text){		
		try{
			if (text.indexOf(' ') >= 0) return "\""+text+"\"";
		}catch(NullPointerException npx){;}
		return text;
	}


	/**
	 * Wraps valid file path in double-quotes if necessary.
	 * @param path to file or directory. Can be relative to the project root directory.
	 * @param isFile true if the path should be a file, instead of a directory.
     * @return valid path quoted ONLY if it contains a space and was found 
     *         to be a valid file or dir in the file system.  Otherwise,
     *         we return null.
	 */
	protected String makeQuotedPath(String path, boolean isFile){		
		String newpath = StringUtils.getTrimmedUnquotedStr(path);
		File file = new CaseInsensitiveFile(newpath).toFile();

		if (! file.exists()) {
			String project = driver.getProjectRootDir();
			file = new CaseInsensitiveFile(project + File.separator + newpath).toFile();
			if (! file.exists()) return null;
		}
		if (isFile)
		    return (file.isFile())? makeQuotedString(file.getPath()):null;
		
	    return (file.isDirectory())? makeQuotedString(file.getPath()):null;		
	}


	/** 
	 * Sets TRD statuscode to the provided status and returns the same. 
	 * This is a convenience routine to do both in a single call.
	 * <p>
	 * <ul>return setTRDStatus(trd, status);</ul>
	 */
	protected long setTRDStatus(TestRecordHelper trd, long status){
		trd.setStatusCode((int)status);
		return status;
	}
		

	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
	}

	/**
	 * Normally called on failure to execute an IBT command.
	 * Gets the current IBT screen snapshot and saves it to file in the Datapool\Test directory.
	 * Saves it with testRecordData filename and linenumber info with a timestamp.
	 * Note: the output file format is normally JPG.  However, if ImageUtils.debug is set to true 
	 * then the image format will be BMP.
	 * @param the testRecordData containing the test FileID and test LineNumber for unique image naming.
	 * @return full filepath to saved snapshot
	 */
	protected String saveTestRecordScreenToTestDirectory(TestRecordData testRecordData){
		String testshot = "";
      SimpleDateFormat time = new SimpleDateFormat("hh.mm.ss.SSS");
		try{ 
			testshot = staf.getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
			if ((testshot==null)||(testshot.length()==0)) return "";
			File file = new CaseInsensitiveFile(testshot).toFile();
			if(! (file.exists()&&(file.isDirectory())))
				throw new java.io.FileNotFoundException(file.getPath());
			testshot += File.separator + testRecordData.getFileID()+
			           ".Line."+ testRecordData.getLineNumber()    +
			           "_Time_"+ time.format(new Date())+ 
			           (ImageUtils.debug ? ".bmp":".jpg");
			
			file = new CaseInsensitiveFile(testshot).toFile();
			testshot = file.getAbsolutePath();
			ImageUtils.saveImageToFile(ImageUtils.getScreenImage(), file);
		}
		catch(Exception x){
			Log.debug(this.getEngineName()+" CANNOT SAVE SCREENSHOT TO TEST DIRECTORY: "+ x.getClass().getSimpleName());
		}
		return testshot;
	}
			
	/**
	 * Shutdown the Engine.  
	 * <p>
	 * All official engines conform to the "SHUTDOWN_HOOK" TestRecordData method 
	 * to initiate shutdown.  So, this function rarely needs to be overridden.
	 * <p>
	 * Following the confirmation that the service or engine is no longer 'running', 
	 * this implementation will call {@link #postShutdownServiceDelay()} which is intended  
	 * to allow subclasses to delay final shutdown of the process by an arbitrary length 
	 * of time while internal shutdown activities are completed.
	 * <p>
	 * This implementation then resets "running" to false.
	 * <p>
	 * Unregistering staf and nulling superclass objects occurs in the finalize() 
	 * method of the superclass.
	 * <p>
	 * @param aname is not used directly in this implementation.
	 * @see DriverConfiguredSTAFInterfaceClass#shutdownService(String)
	 */
	protected void shutdownService(String aname){
		if (console!=null) {
			if (testRecordData == null) testRecordData = new TestRecordHelper();
			testRecordData.setShutdownData();
			try{
				processRecord(testRecordData);
				long timeout = System.currentTimeMillis()+ 8000;
				long now = 0;
				boolean loop = true;
				while(loop){
					loop = isToolRunning();
					now = System.currentTimeMillis();
					if(loop) {
						loop = now < timeout;
						try{Thread.sleep(100);}catch(Exception x){}
					}
				}
			}
			catch(Exception x){;}
		}
		
		postShutdownServiceDelay();
		
		//Try to reset the "shutdown" semaphore
		waitHookShutdownAndReset(5);
		
		running = false;
		try{console.shutdown();}
		catch(Exception x){Log.debug("Console shutdown failure "+x.getClass().getSimpleName());}
		try{process.destroy();}
		catch(Exception x){Log.debug("Process destroy failure "+x.getClass().getSimpleName());}
		console = null;
		process = null;
	}
	
	/** 
	 * Called internally by {@link #shutdownService(String)}.   
	 * <p>
	 * This method is intended to wait for the "shutdown" semaphore post by hook when it stops,
	 * then try to reset the "shutdown" semaphore.
	 * If we don't reset the "shutdown" semaphore, the driver may see this
	 * semaphore and consider that the hook is shutdown and return to try other engine/hook.
	 * 
	 * This method can only try its best to reset "shutdown" semaphore, but this is not guaranteed.
	 * 
	 * <p>
	 * @param timeoutInSeconds, int, the timeout in seconds to wait for the "shutdown" semaphore
	 */
	protected void waitHookShutdownAndReset(int timeoutInSeconds){
		String debugmsg = getClass().getName()+".waitHookShutdownToReset(): ";
		
		//We just to reset all semaphores, especially the semaphore HOOKNAMEShutdown
		//The risk is that if "service name" is the same as the corresponding "hook name"
		try {
			
			//we should wait for the "shutdown" semaphore to make sure that hook has post it before
			//we reset it.
			boolean shutdown = false;
			String shutdownEvent = STAFHelper.getEventShutdownString(servicename);
			
	        Log.debug("Waiting for EVENTS "+ shutdownEvent);
	        int attempt = 0;
	        int maxTries = timeoutInSeconds*10;
	        while(!shutdown){
	        	shutdown = staf.waitEventMillis(shutdownEvent, 80);
	        	if(!shutdown) try{
	        		Thread.sleep(20);
	        		shutdown = ++attempt > maxTries;
	        		if(shutdown){
	        			Log.debug(debugmsg+" detected a response TIMEOUT shutdown.");
	        			Log.debug(debugmsg+" !!! we RISK to reset the 'shutdown' semaphore before hook post it !!!");
	        			//Sleep extra 2 seconds to wait for the 'shutdown' semaphore
	        			Thread.sleep(2000);
	        		}
	        	}catch(Exception x){}
	        }
	        
	        if(!resetShutdownSemaphore()){
	        	Log.debug(debugmsg+"Fail to reset all semaphores for hook '"+servicename+"'");
	        }
	        
		} catch (SAFSException e) {
			Log.debug(debugmsg+"Fail to wait 'shutdown' semaphore for hook '"+servicename+"'; Exception="+e.getMessage());
		}
	}
	
	/**  
	 * <p>
	 * This method is intended to reset the "shutdown" semaphore post hook when it stops.
	 * 
	 * Before we reset the "shutdown" semaphore, we should test the condition for resetting. The
	 * condition is that only "shutdown" is "posted", the others are "reset".
	 * <p>
	 */
	protected boolean resetShutdownSemaphore(){
		String debugmsg = getClass().getName()+".resetShutdownSemaphore(): ";
		try{
			Log.debug(debugmsg+"Try to Reset all semaphores for hook '"+servicename+"'");
			staf.resetHookEvents(servicename);
			
			//We check that only "shutdown" semaphore's state is "posted", other semaphores's state are "reset"
			if(staf.isOnlyShutdownPosted(servicename)){
				Log.debug(debugmsg+"We reset '"+STAFHelper.SAFS_EVENT_SHUTDOWN+"' for Service '"+servicename+"' ");
				staf.resetHookEvents(servicename);
			}else{
				Log.debug(debugmsg+"We should not reset '"+STAFHelper.SAFS_EVENT_SHUTDOWN+"' for Service '"+servicename+"' ");
				Log.debug(debugmsg+"Service '"+servicename+"' events state are: ");
				List<String> events = staf.getServiceEvents(servicename);
				for(String event: events){
					Log.debug(debugmsg+event);
				}
			}
			
			return true;
		} catch (SAFSException e) {
			Log.debug(debugmsg+"Fail to reset all semaphores for hook '"+servicename+"'; Exception="+e.getMessage());
			return false;
		}
	}
	
	/**
	 * Called internally by {@link #shutdownService(String)}.   
	 * <p>
	 * This method is intended to allow subclasses to delay final shutdown of the process by 
	 * an arbitrary length of time while internal shutdown activities are completed.
	 * <p>
	 * This default implementation provides no delay and immediately returns.
	 */
	protected void postShutdownServiceDelay(){ return;}
	
	public boolean setVariable(String var, String val) throws SAFSException {
		if(staf==null){
			Log.error("STAFHelper is null, can't set variable!");
			return false;
		}
		return staf.setVariable(var, val);
	}

	public String getVariable(String var) throws SAFSException {
		if(staf==null){
			Log.error("STAFHelper is null, can't get variable!");
			throw new SAFSException("STAFHelper is null, can't get variable!");
		}
		return staf.getVariable(var);
	}

	/**
	 * Deduce the absolute full path test file name.
	 * @param filename, String, the test file name.
	 * @return String, the absolute full path test file name.
	 * @throws SAFSException
	 * @see {@link #deduceFile(String, int)}
	 */
	protected File deduceTestFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_TEST, this);
	}
	/**
	 * Deduce the absolute full path bench file name.
	 * @param filename, String, the bench file name.
	 * @return String, the absolute full path bench file name.
	 * @throws SAFSException
	 * @see {@link #deduceFile(String, int)}
	 */
	protected File deduceBenchFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_BENCH, this);
	}
	/**
	 * Deduce the absolute full path to a project-relative file.
	 * @param filename, String, the test file name.  The path is ALWAYS considered relative 
	 * to the project root directory regardless of the absence or presence of File.separators 
	 * unless the file is already an absolute path.
	 * @return File, the absolute full path bench file.
	 * @throws SAFSException 
	 * @see {@link #deduceFile(String, int)}
	 */
	protected File deduceProjectFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_PROJECT, this);
	}


	@Override
	public String getAppMapItem(String appMapId, String sectionName,
			String itemName) throws SAFSException {
		return getCoreInterface().getAppMapItem(appMapId, sectionName, itemName);
	}
}

