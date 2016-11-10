/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 02, 2016    (SBJLWA) Most codes were moved from org.safs.model.tools.EmbeddedHookDriverRunner
 */
package org.safs.model.tools;

import java.util.Hashtable;
import java.util.Vector;

import org.safs.IndependantLog;
import org.safs.JavaHook;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.Component;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSConfiguredClassStore;
import org.safs.model.annotations.Utilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.drivers.InputProcessor;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * This Runner is an access point to a minimalist AbstractDriver API.
 * See the AbstractDriver reference for a list of known available subclasses.
 */
public abstract class AbstractRunner implements JSAFSConfiguredClassStore{
	/**
	 * "-autorunclass", the parameter to indicate the class name to run test automatically.<br>
	 * Example, "-autorunclass autorun.full.classname".<br>
	 */
	public static final String ARG_AUTORUN_CLASS = "-autorunclass";

	protected static Vector<Object> callers = new Vector<Object>();
	protected static Hashtable<String, Object> instances = new Hashtable<String,Object>(); 
	
	protected boolean running = false;
	
	/**
	 * The model driver embedded with JSAFSDriver/InputProcessor, which will handle the keyword at the back-end.
	 */
	protected AbstractDriver driver = null;
	
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
	 * The model driver embedded with JSAFSDriver/InputProcessor, which will handle the keyword at the back-end.
	 */
	public abstract AbstractDriver getDriver();
	
	/**
	 * @return AbstractDriver, the AbstractDriver instance.
	 */
	public AbstractDriver driver(){
		return getDriver();
	}
	/**
	 * @return JavaHook, the "embedded Hook" owned by some Runner/Driver.
	 */
	public abstract JavaHook hookDriver();
	
	public void setDriver(AbstractDriver driver){
		if(this.driver!=null){
			debug("The current Driver is not null, we should not replace it.");
		}else{
			this.driver = driver;			
		}
	}
	
	/**
	 * The indicator tells if the runner/embedded-driver is running or not.
	 */
	protected boolean isRunning(){
		return running;
	}
	
	/**
	 * Initiates the embedded drivers and engines to start running if it is not already running.
	 * The user MUST call this method to ensure the drivers/engines are initialized before using them.
	 */
	public void run() throws Exception{
		if(!isRunning()){
			beforeRun();
			getDriver().run();
			running = true;
			//and do something else ...
		}
	}
	
	/**
	 * Prepare something before running.
	 * @see #run()
	 */
	protected void beforeRun(){
		if(!isRunning()){
			//Set the project's default configuration file for any driver that is going to be launched.
			if(System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG)==null){
				System.setProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG, DriverConstant.DEFAULT_CONFIGURE_FILENAME_TEST_INI);
			}
		}
	}
	
	/**
	 * Shutdown the driver and all related resources such as hooks, engines, services and logs etc.
	 * @throws Exception
	 */
	//EmbeddedHookDriverRunner.shutdown() is static
	//I would like to name this method as "shutdown", but the sub-class EmbeddedHookDriverRunner
	//has a static method shutdown()
	public void terminate() throws Exception{
		if(isRunning()){
			getDriver().shutdown();
			running = false;
			//some other clean up ...
		}
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
		//Firstly, we are going to initiate the embedded driver and engines etc. 
		run();
		
		boolean found = false;
		String classname = "bogus";
		String debugmsg = StringUtils.debugmsg(false);
		
		//Try to get the autorun.full.classname from the arguments
		for(int i=0;i<args.length;i++){
			if(!found && args[i].equalsIgnoreCase(ARG_AUTORUN_CLASS)){
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
		
		if(found){
			if(!callers.contains(classname)){
				debug(debugmsg+"processing calling class "+classname);
				callers.add(classname);
				Class c = Class.forName(classname);
				// we might not require the AutoConfigureJSAFS Annotation.
				// If they called us, they want us to do it.
				if(c.isAnnotationPresent(AutoConfigureJSAFS.class)){
					Utilities.autoConfigure(classname, this);
				}
			}else{
				debug(debugmsg+"class "+ classname +" was previously processed.");
			}
		}else{
			debug(debugmsg+"calling class WAS NOT detected.");
		}
	}
	
	public DriverInterface iDriver(){
		try{ return getDriver().iDriver(); }
		catch(Exception np){
			IndependantLog.error(StringUtils.debugmsg(false)+StringUtils.debugmsg(np));
			return null;
		}
	}
	
	public JSAFSDriver jsafs(){
		try{ return getDriver().jsafs(); }
		catch(Exception np){
			IndependantLog.error(StringUtils.debugmsg(false)+StringUtils.debugmsg(np));
			return null;
		}
	}
	public InputProcessor processor(){
		try{ return getDriver().processor(); }
		catch(Exception np){
			IndependantLog.error(StringUtils.debugmsg(false)+StringUtils.debugmsg(np));
			return null;
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
		TestRecordHelper testrecord = getDriver().runComponentFunction(command, component, window, params);
		iDriver().incrementTestStatus(testrecord.getStatusCode());
		if(testrecord.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			iDriver().logMessage(component +" "+command.toUpperCase() +" did NOT execute!", 
				    "Support for this action may not be available in this runtime environment.", 
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
		return testrecord;
	}
	
	public TestRecordHelper command(String command, String... params) throws Throwable{
		TestRecordHelper testrecord = getDriver().runDriverCommand(command, params);
		iDriver().incrementGeneralStatus(testrecord.getStatusCode());
		if(testrecord.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			iDriver().logMessage(command.toUpperCase() +" did NOT execute!", 
				    "Support for this command may not be available in this runtime environment.", 
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
		return testrecord;
	}
	
	public void logGENERIC(String message, String detail){
		JSAFSDriver jsafs = jsafs();
		if(jsafs!=null) jsafs.logGENERIC(message, detail);
		else{
			debug("embedded JSAFSDriver is null, try embedded InputProcessor to log message.");
			InputProcessor processor = processor();
			if(processor!=null){
				processor.logMessage(message, detail, AbstractLogFacility.GENERIC_MESSAGE);
			}else{
				error(StringUtils.debugmsg(false)+" embedded InputProcessor is null, Cannot log message!");
			}
		}
	}
	public void logPASSED(String message, String detail){
		JSAFSDriver jsafs = jsafs();
		if(jsafs!=null) jsafs.logPASSED(message, detail);
		else{
			debug("embedded JSAFSDriver is null, try embedded InputProcessor to log message.");
			InputProcessor processor = processor();
			if(processor!=null){
				processor.logMessage(message, detail, AbstractLogFacility.PASSED_MESSAGE);
			}else{
				error(StringUtils.debugmsg(false)+" embedded InputProcessor is null, Cannot log message!");
			}
		}
	}
	
	public void logFAILED(String message, String detail){
		JSAFSDriver jsafs = jsafs();
		if(jsafs!=null) jsafs.logFAILED(message, detail);
		else{
			debug("embedded JSAFSDriver is null, try embedded InputProcessor to log message.");
			InputProcessor processor = processor();
			if(processor!=null){
				processor.logMessage(message, detail, AbstractLogFacility.FAILED_MESSAGE);
			}else{
				error(StringUtils.debugmsg(false)+" embedded InputProcessor is null, Cannot log message!");
			}
		}
	}

	public static void debug(String message){
		if(STAFProcessHelpers.hasSTAFHelpers()) IndependantLog.debug(message);
		else System.out.println(message);
	}
	public static void error(String message){
		if(STAFProcessHelpers.hasSTAFHelpers()) IndependantLog.error(message);
		else System.err.println(message);
	}
}
