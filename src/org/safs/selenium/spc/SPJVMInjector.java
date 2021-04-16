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
package org.safs.selenium.spc;

import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.tools.MainClass;

/**
 * Provides the ability for any Java application running in a compatible JVM to launch and use SeleniumPlus.
 * <p>
 * When properly invoked via the inject() Method an instance of SeleniumPlus is launched in a separate daemon Thread.  
 * That instance can be used directly along with all the traditional classes and methods available via SeleniumPlus.
 * <p>
 * When the application is expected to shutdown--or whenever the SeleniumPlus instance is no longer needed--
 * the caller should invoke the shutdown() method to allow the SeleniumPlus daemon Thread to gracefully shutdown.
 * <p>
 * Configuring your Java application to use SeleniumPlus:
 * <ul>
 * <li>Tell SeleniumPlus where to find its test configuration file:<br>
 *     This is done via a JVM VM argument, String[] args, or Java System Property:
 *     <ol>  
 *         <li>JVM VM Argument: -Dsafs.project.config=&lt;pathTo>/test.ini<br>
 *         <ul>(INI file can have any name desired.)<br>
 *         JVM Argument is passed in when launching your Java application.</ul>
 *         <li>System Property: key: safs.project.config, value: &lt;pathTo>/test.ini<br>
 *         <ul>(INI file can have any path/name desired.)<br>
 *         Set by JVM VM Argument or explicitly by user Java code: System.setProperty(key, value).</ul>
 *         <li>SPJVMInjector.inject(args);<br>
 *         <ul>Any and all command-line args supported by SeleniumPlus can be passed in.</ul>
 *     </ol>
 * <li>In that SeleniumPlus configuration file, specify the SeleniumPlus project info:<br>
 *     <p>
 *     <ol><li>Specify the DriverRoot directory where default config information already exists.
 *         <li>Specify the ProjectRoot directory for the SeleniumPlus project to be used.<br>
 *         This is the project whose directories, App Maps, Benchmarks, and Log files will be used.
 *         <li>Specify any other test configuration info desired or needed by SeleniumPlus.
 *     </ol>
 *     <p>
 *     Example: "MyInjector.INI"
 *     <p>
 *     <code><pre>
 *     [SAFS_DRIVER]
 *     DriverRoot="%SELENIUM_PLUS%\extra\automation"
 *     
 *     [SAFS_PROJECT]
 *     ProjectRoot="C:\Automation\MySePlusProject"
 *     
 *     [SAFS_TEST]
 *     TestName="MyInjectorTest"
 *     CycleLogMode="TEXTLOG CONSOLELOG XMLLOG"
 *     
 *     [STAF]
 *     EmbedDebug="MyInjectorDebug.log"
 *     </pre></code>
 * <p>
 * <li>Include 3 SeleniumPlus JAR dependencies in your Java Project or CLASSPATH:<br>
 * <p>
 *     <ol>
 *     <li>%SELENIUM_PLUS%\libs\seleniumplus.jar
 *     <li>%SELENIUM_PLUS%\libs\JSTAFEmbedded.jar
 *     <li>%SELENIUM_PLUS%\libs\selenium-server-standalone-3.xx.jar<br>
 *     (Specify the selenium-standalone-server installed.)
 *     </ol>
 * </ul>
 * <p>
 * Having configured your Java project/application and INI files, a sample invocation of your app using SeleniumPlus might look like:
 * <p><ul><code><pre>
 * set CLASSPATH=&lt;YourJarFiles>;&lt;SeleniumPlusJarFiles>
 * java -cp %CLASSPATH% -Dsafs.project.config=C:/Automation/MySePlusProject/MyInjector.INI MyJavaApplicationClass
 * </pre></code></ul>
 * <p>
 * And the code inside your Java app to instantiate and then use SeleniumPlus:
 * <p><ul><code><pre>
 * &lt; your application code >
 * 
 * SPJVMInjector seleniumplus = SPJVMInjector.inject(new String[0]);
 * 
 * &lt; do more stuff with or without SeleniumPlus >
 * 
 * seleniumplus.StartWebBrowser("https://www.google.com", "Google", "chrome");
 * 
 * &lt; do more stuff until it's time to shutdown SeleniumPlus >
 * 
 * seleniumplus.shutdown();
 * 
 * &lt; allow your application to exit >
 * </pre></code></ul>
 * <p>
 * FEB 11, 2019		(Carl Nagle) Initial Release
 * @see #inject(String[])
 * @see #shutdown()
 */
public class SPJVMInjector extends SeleniumPlus{

	/** 
	 * Stores the singleton running instance to be returned to callers.
	 * @see #inject(String[])
	 **/
    private static SPJVMInjector _injector = null;    
    
    /**
     * Provide access to WebDriverGUIUtilities used by the running instance.
     * @see #getUtils()
     */
	WebDriverGUIUtilities utils = null;

	static final String BROWSER_ID_ROOT = "SPINJECTOR";
	static final String PRODUCT_NAME = "SeleniumPlus Injector";
	static final String PRODUCT_VERSION = "1.0";
	static final String PRODUCT_DESCRIPTION = "A tool helps inject SeleniumPlus usage into any application.";

	/**
	 * "running" flag polled by the SeleniumPlus instance.
	 * When this flag is reset to false, the SeleniumPlus instance will shutdown.
	 * @see #shutdown()
	 * @see #runTest()
	 */
	private static boolean _injector_running = false;

	/**
	 * Used internally.
	 * Must be public to support SAFSPlus clazz.newInstance()
	 */
	public SPJVMInjector(){
		super();
		Runner.iDriver().setProductName(PRODUCT_NAME);
		Runner.iDriver().setVersion(PRODUCT_VERSION);
		Runner.iDriver().setDescription(PRODUCT_DESCRIPTION);
		_isSPC = true;
		_isInjected = true;
		_injectDataAwareness = true;
	}

	/**
	 * Used internally.
	 * Must be public to support SAFSPlus clazz.newInstance()
	 * @param utils
	 */
	public SPJVMInjector(WebDriverGUIUtilities utils){
		this();
		this.utils = utils;
	}

	/**
	 * Only viable AFTER the call to inject() has produced a running instance of SeleniumPlus.
	 * @return WebDriverGUIUtilities used by the running SeleniumPlus instance.
	 */
	public WebDriverGUIUtilities getUtils(){ return utils; }

	/**
	 * Used internally.  
	 * As a specialized remote driver of SeleniumPlus, this method has been designed to:
	 * <ul>
	 * <li>Set the "running" flag to true.<br>
	 * A call to the shutdown() method will reset this flag to false.
	 * <p>
	 * <li>Store the SeleniumPlus runtime instance locally for the inject() method to return to the caller.
	 * <p>
	 * <li>Install a JVM Shutdown Hook.<br> 
	 * This allows us a chance to perform SeleniumPlus shutdown, if the shutdown() method is not called by the user.
	 * <p>
	 * <li>Periodically poll the "running" flag to see if we should shutdown.
	 * <p>
	 * <li>Reset the SeleniumPlus runtime instance following the shutdown.
	 * </ul>
	 * @see #inject(String[])
	 * @see #shutdown()
	 */
	@Override
	public void runTest() throws Throwable {
		
		_injector_running = true;
		
		utils = (WebDriverGUIUtilities) Runner.hookDriver().getGUIUtilities();

		// provide the instantiated instance internally for the inject method return value;
		_injector = this;

		try{Thread.sleep(2000);}catch(Exception x){}

		Runtime runtime = Runtime.getRuntime();
		
		// allow us to shutdown SeleniumPlus when the JVM is shutting down.
		runtime.addShutdownHook(new Thread(){
			public void run(){
				//stopSelenium();
				shutdown();
			}
		});
		
		//
		// what do we watch in order to not exit until later?
		// if we are not a daemon thread, then we will not loop.
		//
		while(_injector_running){
			try{Thread.sleep(500);}catch(Exception x){}
		}
		_injector = null;
		utils = null;
	}

	/**
	 * Primary user entry point.<br>
	 * Use this to inject SeleniumPlus into your running JVM.<br>
	 * SeleniumPlus will be launched in a separate daemon Thread and the instance will be returned to the caller.<br>
	 * @param args String[] as might be desired to initialize SeleniumPlus.
	 * @return SPJVMInjector -- a SeleniumPlus instance allowing use of SeleniumPlus in any Java Application.
	 * @see #shutdown()
	 */
	public static SPJVMInjector inject(String[] args){
		
		if(_injector == null){
			debug("SPJVMInjector.inject main thread with CLASSPATH "+ System.getProperty("java.class.path"));
			int count = 0;
			Thread trunner = new Thread(){
				public void run(){
					MainClass.setMainClass(SPJVMInjector.class.getName());
					SeleniumPlus.main(args);
				}
			};
			trunner.setDaemon(true);
			trunner.setContextClassLoader(ClassLoader.getSystemClassLoader());
			trunner.start();
			
			// delay the return until we have a running Se+
			while (_injector == null && count++ < 19){
				try{Thread.sleep(1500);}catch(Exception x){}
			}
		}
		return _injector;
	}

	/**
	 * Clears the "running" flag signaling the separate SeleniumPlus Thread to shutdown.<br>
	 * The routine provides a short delay before returning giving SeleniumPlus some time to shutdown.
	 */
	public void shutdown(){
		_injector_running = false;
		try{Thread.sleep(1200);}catch(Exception x){}		
	}
}
