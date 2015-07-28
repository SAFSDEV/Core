/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.safs.DDGUIUtilities;
import org.safs.Domains;
import org.safs.EngineCommandProcessor;
import org.safs.GuiObjectVector;
import org.safs.Log;
import org.safs.Processor;
import org.safs.RobotJHook;
import org.safs.SAFSException;
import org.safs.SAFSRuntimeException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.rational.logging.RLogUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.rational.test.ft.UserStoppedScriptError;


/**
 * Description   : RRobotJ Hook Script
 * @author dbauman
 * @since   JUL 15, 2003
 *
 *   <br>   JUN 15, 2003    dbauman: subclass is now in org.safs.rational
 *          AUG 15, 2008    JunwuMa: Modify initializeUtilities() using REngineCommandProcessor 
 *                                   instead of EngineCommandProcessor. 
 *   <br>   MAR 26, 2009    (Carl Nagle) Added support for clearProxiesAlways.
 *   <br>   MAR 08, 2011 (DharmeshPatel) Added RFSM support for search mode.
 */

public class RRobotJHook extends RobotJHook {


  private Script script;
  private boolean clearProxiesAlways = false;

  public RRobotJHook (){super();}
  public RRobotJHook (Script script){ super(); this.setScript(script);}
  public RRobotJHook (Script script, RLogUtilities logUtils){
    super();
    this.setLogUtil(logUtils); // must be first so messages can be logged.
    this.setScript(script);
  }

  public void setScript(Script script) {
    log.logMessage(null, 
                        "Script: "+script.getClass().getName()+", "+script,
    					DEBUG_MESSAGE);
    this.script = script;
  }

  /**
   * Overrides the superclass to add an EngineCommandProcessor to our 
   * ProcessRequest object after normal initialization.
   * @see RobotJHook#initializeUtilities()
   */
  protected void initializeUtilities(){
  	super.initializeUtilities();
    EngineCommandProcessor ecommands = new REngineCommandProcessor();
    RGuiObjectVector evector = new RGuiObjectVector("Window", "Child","");
    evector.setScript(script);
    evector.setProcessMode(GuiObjectVector.MODE_EXTERNAL_PROCESSING);    
    ecommands.setGuiObjectVector(evector);
    ecommands.setLogUtilities(log);
    processor.setEngineCommandProcessor(ecommands);
    
    initConfigPaths();
    initRuntimeParams();
  }
  
  protected void initConfigPaths(){
		String path = System.getProperty("safs.config.paths");
		String [] paths = new String[0];
		if(path != null){
			paths = path.split(File.pathSeparator);
		} else {
			String msg = "SAFS -Dsafs.config.paths is not available for this instance.";
			Log.info(msg);
			return;
		}
		
		ConfigureInterface config = null;
		for(int i = 0; i < paths.length; i++){
			File f = new CaseInsensitiveFile(paths[i]).toFile();
			if(f.exists()){
				if(config == null){
					config = new ConfigureFile(f);
				} else {
					config.addConfigureInterface(new ConfigureFile(f));
				}
			}			
		}
		((RTestRecordData)data).setConfig(config);	  
  }
  
  protected void initRuntimeParams(){
	  ConfigureInterface config = null;
      String p = null;
	  try{
		  config = ((RTestRecordData)data).getConfig();
	  }catch(Exception x){;}
	  if(config == null){
		  Log.info("SAFS/RobotJ access to configuration file(s) is not available.");
		  return;
	  }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.SAFS_SECSWAITFORWINDOW_ITEM);
          Processor.setSecsWaitForWindow(Integer.parseInt(p));
          Log.info("Config setting: secsWaitForWindow = "+ p);
          System.out.println("Overriding config setting: secsWaitForWindow = "+ p);
      }catch(Exception x){
    	  Log.debug("Ignoring secsWaitForWindow Exception: "+x.getClass().getSimpleName());
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.SAFS_SECSWAITFORCOMPONENT_ITEM);
          Processor.setSecsWaitForComponent(Integer.parseInt(p));
          Log.info("Config setting: secsWaitForComponent = "+ p);
          System.out.println("Overriding config setting: secsWaitForComponent = "+ p);
      }catch(Exception x){
    	  Log.debug("Ignoring secsWaitForComponent Exception: "+x.getClass().getSimpleName());
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.SAFS_COMMANDLINEBREAKPOINT_ITEM);
    	  if(p.length()>0){
    		  Processor.setCommandLineBreakpoint(StringUtilities.convertBool(p));
              Log.info("Config setting: commandLineBreakpoint = "+ p);
              System.out.println("Overriding config setting: commandLineBreakpoint = "+ p);
    	  }
      }catch(Exception x){
    	  Log.debug("Ignoring commandLineBreakpoint Exception: "+x.getClass().getSimpleName());
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.SAFS_TESTDOMAINS_ITEM);
    	  if(p.length()>0){
	          Processor.setTestDomains(p);
	          Domains.enableDomains(p);
              System.out.println("Overriding config setting: testDomains = "+ p);
    	  }
      }catch(Exception x){
    	  Log.debug("Ignoring testDomains Exception: "+x.getClass().getSimpleName());
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.SAFS_CLEARPROXIESALWAYS_ITEM);
    	  if(p.length()>0){
              clearProxiesAlways = StringUtilities.convertBool(p);
	          Processor.setClearProxiesAlways(clearProxiesAlways);
              System.out.println("Overriding config setting: clearProxiesAlways = "+ p);
    	  }else{
              clearProxiesAlways = Processor.getClearProxiesAlways();
    	  }
      }catch(Exception x){
          clearProxiesAlways = Processor.getClearProxiesAlways();
    	  Log.debug("Ignoring clearProxiesAlways Exception: "+x.getClass().getSimpleName());
      }
      try{ processor.getEngineCommandProcessor().distributeConfigInformation();}
      catch(Exception x){
    	  Log.debug("Ignoring processor.distributeConfigInformation Exception: "+x.getClass().getSimpleName(),x);
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.RFT_FIND_SEARCH_MODE_ALGORITHM);
    	  if(p.length()>0){
    		  Processor.setRFSMOnly(StringUtilities.convertBool(p));
              Log.info("Config setting: RFSMOnly = "+ p);
              System.out.println("Overriding config setting: RFSMOnly = "+ p);
    	  }
      }catch(Exception x){
    	  Log.debug("Ignoring RFSMOnly Exception: "+x.getClass().getSimpleName());
      }
      try{
    	  p = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, Processor.RFT_FIND_SEARCH_MODE_CACHE);
    	  if(p.length()>0){
    		  Processor.setRFSMCache(StringUtilities.convertBool(p));
              Log.info("Config setting: RFSMCache = "+ p);
              System.out.println("Overriding config setting: RFSMCache = "+ p);
    	  }
      }catch(Exception x){
    	  Log.debug("Ignoring RFSMCache Exception: "+x.getClass().getSimpleName());
      }
  }
  
  public void start () {
	boolean record_running = false;
    if (script == null) {
      String scriptMsg="Script is null, please do a RobotJHook.setScript(this) in your test script";
      log.logMessage(null, scriptMsg, FAILED_MESSAGE);
      throw new RuntimeException(scriptMsg);
    }
    //super.start();
    boolean shutdown = false;
    String  testrecord = null;                
    int rc = 99;
    try {
      helper.resetHookEvents(ROBOTJ_PROCESS_NAME);
      initializeUtilities();
      do{
      	record_running = false;
        helper.postEvent(STAFHelper.ROBOTJ_EVENT_READY);

        try {
          testrecord = helper.getNextTestEvent();
          if (testrecord.equalsIgnoreCase(SHUTDOWN_RECORD)) {
            shutdown = true;
          }
        } catch (SAFSException safsex) {
          log.logMessage(null, "safsex:"+safsex, WARNING_MESSAGE);
          shutdown = true;
        }
        //System.out.println("Processing message: "+ testrecord);

        if (!shutdown) {
          record_running = true;
          data.reinit(); // reset the data to starting point
          try{
            data.setInstanceName(STAFHelper.SAFS_HOOK_TRD);
            data.populateDataFromVar();
            try{ 
            	processor.doRequest();
                if(clearProxiesAlways) {
                	Log.info(ROBOTJ_PROCESS_NAME +" clearProxiesAlways initiating UnregisterAll for remote proxy references...");
                	script.localUnregisterAll();
                	script.getGuiUtilities().clearAllAppMapCaches();
                }
            }
            catch (SAFSRuntimeException ex) {
                long response = evaluateRuntimeException (ex);
                if (response == REQUEST_USER_STOPPED_SCRIPT_REQUEST) {
                	Log.info(ROBOTJ_PROCESS_NAME +" processing user-initiated engine shutdown...");
                	shutdown = true;
                	data.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
                	data.setStatusInfo(SHUTDOWN_RECORD);
                }
            } 
            data.sendbackResponse();          
          } 
          catch (SAFSException ex) {
            Log.error("*** ERROR *** "+ ex.getMessage(),ex);
          }
        }

        helper.setTestResults();
        helper.resetEvent(STAFHelper.ROBOTJ_EVENT_RUNNING);    
        System.gc();
      } while(!shutdown);                

      helper.resetHookEvents(ROBOTJ_PROCESS_NAME);
      helper.postEvent(STAFHelper.ROBOTJ_EVENT_SHUTDOWN);
    } 
    catch(SAFSException e){
      Log.error("Error talking with STAF subsystem; "+e.getMessage(), e);
    } 
    catch(UserStoppedScriptError e){
    	Log.info(ROBOTJ_PROCESS_NAME +" processing user-initiated test abort shutdown...");
    	data.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
    	data.setStatusInfo(SHUTDOWN_RECORD);
        try{ 
            if (helper != null){
            	if(record_running) {
            		data.sendbackResponse();         
    	            helper.setTestResults();
            	}
	            helper.resetHookEvents(ROBOTJ_PROCESS_NAME);
	            helper.postEvent(STAFHelper.ROBOTJ_EVENT_SHUTDOWN);
	            helper.unRegister();
            }
        }catch(SAFSException ex){
            Log.error("*** ERROR *** during User Abort Test processing: "+ ex.getMessage(),ex);
        }
    	
    } catch(Error e){
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	PrintStream err = new PrintStream(stream);
    	e.printStackTrace();
        e.printStackTrace(err);       
        Log.error("Error talking with STAF subsystem; "+e.getMessage()+"\n"+ err.toString());
    } finally {
      try {
        if (helper != null) helper.unRegister();
      } catch(SAFSException e){ }
      //System.out.println("RobotJ hook script shutting down.");
      // using the Clipboard class sometimes gets us stuck here, so doing this:
      
      //System.exit(0); //(Carl Nagle) FEB 28, 2009
    }
}

  /**
   * Overrides superclass.
   * Evaluate if the runtime hook should shutdown, proceed, or some other action 
   * following the receipt of a RuntimeException.
   */
  protected long evaluateRuntimeException(Throwable ex){
  	  Throwable x = ex.getCause();
  	  if (x instanceof com.rational.test.ft.UserStoppedScriptError)
  	  	  return REQUEST_USER_STOPPED_SCRIPT_REQUEST;
  	  	  
  	  return REQUEST_PROCEED_TESTING;
  }
  
  
  protected DDGUIUtilities getUtilitiesFactory() {	  
    return new RDDGUIUtilities(helper, script, log);
  }

  public RDDGUIUtilities getRGuiUtilities() {
	    return (RDDGUIUtilities) utils;
  }

  protected TestRecordHelper getTestRecordDataFactory() {
    return new RTestRecordData();
  }

  /**
   * @return the current populated RTestRecordData
   */
  public RTestRecordData getRTestRecordData() {
	    return (RTestRecordData) data;
	  }

  protected TestRecordHelper getTestRecordDataFactory(DDGUIUtilities utils) {
    return new RTestRecordData(helper, script, utils);
  }

}
