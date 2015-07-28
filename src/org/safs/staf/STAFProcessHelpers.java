/** Copyright (C) SAS Institute. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.staf;

import org.safs.*;

import java.io.IOException;
import java.util.*;


/**
 * Retrieves the only allowed instance of a STAFHelper for a given process name.
 * This prevents attempts of registering multiple processes with the same name.
 *
 * @author  Carl Nagle
 * @since   NOV 09, 2003
 * <br>     NOV 09, 2003  (CANAGL) Original Release
 * <br>     APR 26, 2011  (LeiWang) Modify method unRegisterALLHelpers()
 *                                  If we clear the holders before calling unRegisterHelper(processname),
 *                                  unRegisterHelper(processname) may not work.
 * <br>     MAY 17, 2012  (CANAGL) Enhance launchSTAFProc to make more than 1 attempt.                                 
 **/
public class STAFProcessHelpers {

  /** stored helpers **/
  private static Hashtable helpers = new Hashtable(5);

  /** stored reference counts **/
  private static Hashtable holders = new Hashtable(5);

  protected static GetText errorText = new GetText("failedSAFSTextResourceBundle", 
                                                    Locale.getDefault());

  protected static final String NULL_ERROR = "staf_reg_null";
  
  /** 
   * create/Register/Retrieve a STAFHelper with the given process name.
   * The returned STAFHelper will already be initialized.
   * <p> 
   * If the STAFHelper was not already registered then a STAFHelper of the 
   * provided subclass will be instantiated.
   * 
   * @param process_name The name of the process to register with STAF.
   * @param clazz Allows us to specify a specific STAFHelper subclass to instantiate, if needed.
   * 
   * @return STAFHelper instance
   * @exception SAFSStafRegistrationException may be thrown if the process was registered 
   * by some other means outside of the control of this class.
   **/
  public static STAFHelper registerHelperClass (String process_name, Class clazz) 
                                           throws SAFSSTAFRegistrationException{
    
    if ((process_name == null)||(process_name.length() == 0)) 
       throw new SAFSSTAFRegistrationException ( errorText.convert(NULL_ERROR,
                                                "STAFProcessHelper : Bad Process Name!",
                                                "STAFProcessHelper"));

    if (helpers.containsKey(process_name)) {
    	Integer count = (Integer) holders.get(process_name);
    	holders.put(process_name, new Integer(count.intValue() +1));
    	return (STAFHelper) helpers.get(process_name);
    }
    else{
    	holders.put(process_name, new Integer(1));
    	STAFHelper help = null;
    	try{ 
    		help = (STAFHelper)clazz.newInstance();
    		help.initialize(process_name);
    	}catch(Exception x){
    		throw new SAFSSTAFRegistrationException(errorText.convert(NULL_ERROR,
                                                "STAFProcessHelper : "+ x.getClass().getSimpleName()+": "+x.getMessage(),
                                                x.getClass().getSimpleName()+": "+x.getMessage()));
    	}
    	helpers.put(process_name, help);
    	return help; 
    }
  }

  /** 
   * Create/Register/Retrieve a STAFHelper with the given process name.
   * The returned STAFHelper will already be initialized unless an exception is thrown.
   * Will instantiate a STAFHelper.class instance if not already registered.
   * 
   * @param process_name The name of the process to register with STAF.
   * 
   * @return STAFHelper instance 
   * @exception SAFSStafRegistrationException may be thrown if the process was registered 
   * by some other means outside of the control of this class.
   **/
  public static STAFHelper registerHelper(String process_name) throws SAFSSTAFRegistrationException{
	  return registerHelperClass(process_name, STAFHelper.class);
  }
  
  /** 
   * unRegister a STAFHelper with the given process name.  This will only unregister the 
   * STAFHelper if it is the last known reference to the object.  
   * <p>
   * The user can also simply wait until the end of everything and call the 
   * unRegisterALLHelpers method.  This will unRegister all known STAFHelpers for all 
   * processes ever registered via this class.
   * 
   * @param process_name The name of the process to unRegister with STAF.
   * 
   * @exception SAFSStafRegistrationException may be thrown if some problem occurs.
   **/
  public static void unRegisterHelper (String process_name) 
                                       throws SAFSSTAFRegistrationException{
    
    if ((process_name == null)||(process_name.length() == 0)) return;

    if (holders.containsKey(process_name)) {

    	Integer reference = (Integer) holders.get(process_name);
    	int count = reference.intValue() -1;    	
    	
    	if (count < 1) {
    		STAFHelper helper = (STAFHelper) helpers.remove(process_name);
    		holders.remove(process_name);
    		try { helper.unRegister();}
    		catch(SAFSException e){ throw new SAFSSTAFRegistrationException( e.toString());}
    	}
    	else{
	    	holders.put(process_name, new Integer(count));
    	}
    }
  }


  /**
   * Unregisters ALL STAFHelpers for ALL processes ever registered via this class.
   * Only recommended at the end of absolutely everything in SAFS testing.
   **/
  public static void unRegisterALLHelpers() {
  	
  	if (helpers.isEmpty()) return;

  	//Reset counter to 1 for each process
  	Enumeration counters = holders.keys();
  	while(counters.hasMoreElements()){
  		holders.put(counters.nextElement(), new Integer(1));
  	}
  	
    Enumeration helper = helpers.keys();    
    while(helper.hasMoreElements()){
      try{ unRegisterHelper((String)helper.nextElement()); }
      catch(Exception e){;}
    }
    
    holders.clear();
    helpers.clear();
  }
  
  /**
   * Attempt to launch STAF.
   * Instantiates and registers STAFHelper.class instance by default.
   * Normally you would only do this if other operations detected it was not running.
   * @param processName to automatically register with STAF and detect ServiceLoader.
   */
  public static STAFHelper launchSTAFProc(String processName)throws SAFSSTAFRegistrationException{
	  return launchSTAFProcClass(processName, STAFHelper.class);
  }

  /**
   * Attempt to launch STAF.
   * Normally you would only do this if other operations detected it was not running.
   * @param processName to automatically register with STAF and detect ServiceLoader.
   * @param a STAFHelper subclass Class to instantiate.
   * @see #registerHelperClass(String, Class)
   */
  public static STAFHelper launchSTAFProcClass(String processName, Class clazz) throws SAFSSTAFRegistrationException{
		String[] cmdarray = {"STAFProc"};					
		STAFHelper staf = null;
		try{
			Process proc = null;
			int attempts = 0;
			int checks = 0;
			int procrc = -1;
			while (proc == null && attempts++ < 2){
				proc = Runtime.getRuntime().exec( cmdarray );
				while (proc != null && checks++ < 3){
					try{Thread.sleep(1000);}catch(Exception ignore){}
					try{
						procrc = proc.exitValue();
						proc.destroy();
						proc = null;
						checks = 0;
					}catch(IllegalThreadStateException running){}
				}
			}
			if(proc == null) throw new SAFSSTAFRegistrationException("Unrecoverable Error attempting to launch STAF!");
			
			//loop til registration works
			int timeout = 60;
			int loop    = 0;
			for(;loop < timeout;loop++){
				try{
					staf = STAFProcessHelpers.registerHelperClass(processName, clazz);
				    break; }
				catch(SAFSSTAFRegistrationException sx){;}
				try{ Thread.sleep(1000);}catch(InterruptedException ix){}
			}
			if (loop==60)
			    throw new SAFSSTAFRegistrationException("Unable to launch STAF itself.");
			    
			// wait for service loader to initialize
			Log.info("Waiting for STAF/ServiceLoader...");
			boolean ready = false;
			for(loop=0;((loop < (timeout/2))&&(!ready));loop++){
				if(staf.getSTAFVersion()>2)
					ready = staf.isToolAvailable(STAFHelper.STAF_SERVICELOADER_V3);
				else
					ready = staf.isToolAvailable(STAFHelper.STAF_SERVICELOADER_V2);
				if(!ready)
				    try{ Thread.sleep(1000);}catch(InterruptedException ix){}
			}
			return staf;
		}
		catch(IOException iox){
			System.err.println(iox.getMessage());
			SAFSSTAFRegistrationException x = new SAFSSTAFRegistrationException("Unable to launch STAF");
			x.initCause(iox);
			throw x;
		}
		catch(SecurityException secx){
			System.err.println(secx.getMessage());
			SAFSSTAFRegistrationException x = new SAFSSTAFRegistrationException("Unable to launch STAF");
			x.initCause(secx);
			throw x;
		}
  }

  /**
   * Attempt to shutdown the SAFS Debug Log, if any.
   */
  public static void shutdownDebugLog() throws SAFSException{
		String[] cmdarray = {"STAF", "local", "queue", "queue", "name", "SAFS/TESTLOG", "message", "SHUTDOWN"};					
		try{
			Process proc = null;
			int attempts = 0;
			int checks = 0;
			int procrc = -1;
			while (proc == null && attempts++ < 2){
				proc = Runtime.getRuntime().exec( cmdarray );
				while (proc != null && checks++ < 3){
					try{Thread.sleep(1000);}catch(Exception ignore){}
					try{
						procrc = proc.exitValue();
						proc.destroy();
						proc = null;
						checks = 0;
					}catch(IllegalThreadStateException running){}
				}
			}
			if(proc == null) throw new SAFSException("Unrecoverable Error attempting to shutdown the Debug Log, if any!");
			
		}
		catch(IOException iox){
			System.err.println(iox.getMessage());
			SAFSException x = new SAFSException("Unable to shutdown the Debug Log, if any.");
			x.initCause(iox);
			throw x;
		}
		catch(SecurityException secx){
			System.err.println(secx.getMessage());
			SAFSException x = new SAFSException("Unable to shutdown Debug Log, if any.");
			x.initCause(secx);
			throw x;
		}		
  }

  /**
   * Attempt to shutdown STAF.
   * Normally you would only do this if all running engines, services, and queues would 
   * not prevent a successful shutdown.  For example, you should NOT attempt to shutdown 
   * STAF if there is a running SAFS Engine or independent SAFS Debug Log process running.  
   * Those should be shutdown first.
   * <p>
   * However, calling this routine will make an attempt to shutdown any running 
   * SAFS Debug Log.
   * @see #shutdownDebugLog() 
   */
  public static void shutdownSTAFProc() throws SAFSException{
		String[] cmdarray = {"STAF", "local", "shutdown", "shutdown"};					
		try{
			try{ shutdownDebugLog(); }catch(SAFSException ignore){}
			// do we need a slight delay after the debug log shutdown?
			Process proc = null;
			int attempts = 0;
			int checks = 0;
			int procrc = -1;
			while (proc == null && attempts++ < 2){
				proc = Runtime.getRuntime().exec( cmdarray );
				while (proc != null && checks++ < 3){
					try{Thread.sleep(1000);}catch(Exception ignore){}
					try{
						procrc = proc.exitValue();
						proc.destroy();
						proc = null;
						checks = 0;
					}catch(IllegalThreadStateException running){}
				}
			}
			if(proc == null) throw new SAFSException("Unrecoverable Error attempting to shutdown STAF!");
			
		}
		catch(IOException iox){
			System.err.println(iox.getMessage());
			SAFSException x = new SAFSException("Unable to shutdown STAF");
			x.initCause(iox);
			throw x;
		}
		catch(SecurityException secx){
			System.err.println(secx.getMessage());
			SAFSException x = new SAFSException("Unable to shutdown STAF");
			x.initCause(secx);
			throw x;
		}
  }
  
  /** prevent instantiation **/
  private STAFProcessHelpers() {;}

  /**
   * @return true if we have ANY initialized STAFHelpers stored.
   */
  public static boolean hasSTAFHelpers() {	
	return ! helpers.isEmpty();
  }

  /**
   * Retrieve the Process Names of any registered STAFHelpers--regardless of Class.
   * @return Enumeration of registered Process Names, if any.  Thus, the Enumeration 
   * could have 0 entries.
   */
  public static Enumeration getSTAFHelperProcessNames()throws NullPointerException{
	  return helpers.keys();
  }
}
