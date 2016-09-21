/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 * 
 * JUN 23,2015     (Lei Wang) Modified autorun(): to get autorun.classname from arguments.
 *                                              Use StringUtils.getCallerClassName() to replace the deprecated sun.reflect.Reflection.getCallerClass().
 * SEP 21,2016     (Lei Wang) Modified command(): increment the "general counter" instead of "test counter".
 */
import java.util.Hashtable;
import java.util.Vector;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.Component;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSConfiguredClassStore;
import org.safs.model.annotations.Utilities;
import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.drivers.EmbeddedHookDriver;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * This Runner is an access point to a minimalist EmbeddedHookDriver Driver API.
 * See the EmbeddedHookDriver reference for a list of known available subclasses.
 * @author Carl Nagle
 * @see EmbeddedHookDriver
 */
public class EmbeddedHookDriverRunner implements JSAFSConfiguredClassStore{

	private static EmbeddedHookDriverDriver driver;
	private static Vector callers = new Vector();
    private static Hashtable<String, Object> instances = new Hashtable<String,Object>(); 
	public static void debug(String message){
		if(STAFProcessHelpers.hasSTAFHelpers()) Log.debug(message);
		else System.out.println(message);
	}	
	
	/* hidden constructor */
	private EmbeddedHookDriverRunner(){}
	
	/**
	 * Create the Runner that instantiates the particular EmbeddedHookDriver subclass 
	 * pass in to the Constructor. 
	 */
	public EmbeddedHookDriverRunner(Class clazz){
		super();
		if(driver == null){
			try{				
				driver = new EmbeddedHookDriverDriver(clazz);
			}catch(Exception x){
				x.printStackTrace();
				throw new Error("Cannot instantiate required Drivers!");
			}
		}
	}

	/** retrieve access to the minimalist Driver API, if needed. */
	public EmbeddedHookDriverDriver driver(){ return driver;}

	/** retrieve access to the wrapped EmbeddedHookDriver API, if needed. */
	public EmbeddedHookDriver hookDriver(){ return driver.driver;}

	/** retrieve access to the full JSAFSDriver API, if needed. */
	public JSAFSDriver jsafs(){return hookDriver().jsafs();}
	
	/**
	 * When using JSAFS to automatically instantiate, configure, and execute tests 
	 * across many classes and packages the user can retrieve those otherwise 
	 * unavailable class object instances here. This can be useful if the class instance 
	 * will capture test execution information or data that you want to examine.
	 * @param classname -- the full package name of the class to retrieve.
	 * ex: my.test.package.MyTest 
	 * @return the object instance that was instantiated and used, or null if we have 
	 * no instance of the specified class.
	 */
	public Object getConfiguredClassInstance(String classname){
		return classname== null ? null:instances.get(classname);
	}
	/** normally only used internally to store objects as we instantiate them.*/
	public void addConfiguredClassInstance(String classname, Object object){
		instances.put(classname, object);
	}
	
	/**
	 * Initiates the embedded drivers and engine to start running if it is not already running.
	 * The user should call either this method or the autorun to ensure the drivers are initialized 
	 * before trying to use them.
	 * @see #autorun(String[]) 
	 */
	public void run(){
		driver.run();
	}
	
	/**
	 * This is the critical method users could call to commence the automatic 
	 * instantiation, configuration, and execution of JSAFSTest methods.
	 * <p>
	 * Minimalist example:
	 * <p><ul><pre>
	 *     public static void main(String[] args)throws Throwable{
	 *         MyTestApp app = new MyTestApp();
	 *         
	 *         new Runner().autorun(args);
	 *         ...
	 *     }
	 * </pre></ul>
	 * <p>
	 * Automatic configuration and usage is not required.  The user can control 
	 * test configuration and execution within their custom code if they want.
	 * 
	 * @param args passed in from command-line Java-- the primordial main(String[] args)<br>
	 *             Usually, it is the class who calls Runner.autorun() will be considered as<br>
	 *             the class that is requesting the automatic configuration and execution, but we can also<br>
	 *             change it by providing parameter as "-autorunclass autorun.full.classname".
	 * @throws Throwable
	 * @see org.safs.model.annotations.JSAFSTest
	 */
	public void autorun(String[] args) throws Throwable{
		run();
		boolean found = false;
		String classname = "bogus";
		String debugmsg = StringUtils.debugmsg(false);
		
		//Try to get the autorun.full.classname from the arguments
		for(int i=0;i<args.length;i++){
			if(!found && args[i].equalsIgnoreCase(SeleniumPlus.ARG_AUTORUN_CLASS)){
				if(i+1<args.length){
					found = true;
					classname = args[++i];
				}
			}
		}

		//LeiWang, get the caller's classname, replace deprecated sun.reflect.Reflection.getCallerClass()
		if(!found){
			try{
				classname = StringUtils.getCallerClassName(true);
				found = StringUtils.isValid(classname);
			}catch(Exception e){
				debug(debugmsg+"Fail to get caller class, due to exception "+StringUtils.debugmsg(e));
			}
		}
		
//		//LeiWang comment it out for the TODO task
//		for(int i=0;!found && i<4;i++){
//			// TODO API getCallerClass Disappearing in Java 7/Java 8?
//			// http://www.infoq.com/news/2013/07/Oracle-Removes-getCallerClass
//			classname= sun.reflect.Reflection.getCallerClass(i).getName();
//			debug(debugmsg+" trace["+ i +"]:"+ classname);
//			if(!classname.equals(sun.reflect.Reflection.class.getName()) &&
//			   !classname.equals(Thread.class.getName()) &&
//			   !classname.equals(EmbeddedHookDriverRunner.class.getName())){
//				found = true;
//			}
//		}
		
		if(found && !callers.contains(classname)){
			debug(debugmsg+"processing calling class "+classname);
			callers.add(classname);
			Class c = Class.forName(classname);
			// we might not require the AutoConfigureJSAFS Annotation.
			// If they called us, they want us to do it.
			if(c.isAnnotationPresent(AutoConfigureJSAFS.class)){
			    Utilities.autoConfigure(classname, this);
			}
		}else{
			if(!found) debug(debugmsg+"calling class WAS NOT detected.");
			else       debug(debugmsg+"class "+ classname +" was previously processed.");
		}
	}	
	
	public TestRecordHelper action(Component comp, String command, String... params) throws Throwable{
		String parent = comp.getParentName();
		String child = comp.getName();
		return parent == null ?
			   action(command, child, child, params) :
			   action(command, parent, child, params);			   
	}
	
	public TestRecordHelper action(String command, String window, String component, String... params) throws Throwable{
		TestRecordHelper rc = driver.runComponentFunction(command, component, window, params);
		driver.iDriver().incrementTestStatus(rc.getStatusCode());
		if(rc.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			jsafs().logMessage(component +" "+command.toUpperCase() +" did NOT execute!", 
				    "Support for this action may not be available in this runtime environment.", 
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
		return rc;
	}
	
	public TestRecordHelper command(String command, String... params) throws Throwable{
		TestRecordHelper rc = driver.runDriverCommand(command, params);
		driver.iDriver().incrementGeneralStatus(rc.getStatusCode());
		if(rc.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			jsafs().logMessage(command.toUpperCase() +" did NOT execute!", 
				    "Support for this command may not be available in this runtime environment.", 
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
		return rc;
	}
	
	public void logGENERIC(String message, String detail){
		jsafs().logGENERIC(message, detail);
	}
	public void logPASSED(String message, String detail){
		jsafs().logPASSED(message, detail);
	}
	
	public void logFAILED(String message, String detail){
		jsafs().logFAILED(message, detail);
	}
	
	/**
	 * The user should make sure the embedded Driver is shutdown to close any external assets 
	 * after execution is complete.
	 * @see EmbeddedHookDriverDriver#shutdownDriver()
	 */
	public static void shutdown(){
		try{ driver.shutdownDriver(); }
		catch(Exception x){
			debug("Hook Shutdown ignoring "+ x.getClass().getSimpleName()+" "+ x.getMessage());
		}
	}
}
