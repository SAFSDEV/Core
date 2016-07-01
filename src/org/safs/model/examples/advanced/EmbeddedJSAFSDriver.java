/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.examples.advanced;

import org.safs.model.Component;
import org.safs.model.commands.ComponentFunctions;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.model.components.EditBox;
import org.safs.model.components.PushButton;
import org.safs.model.components.Window;
import org.safs.model.tools.Driver;
import org.safs.tools.drivers.DefaultDriver;
import org.safs.tools.drivers.JSAFSDriver;
import org.safs.Log;
import org.safs.TestRecordHelper;

/**
 * This class attempts to demonstrate how ANY Java application can be made to exploit 
 * <a href="http://safsdev.sourceforge.net/sqabasic2000/UsingJSAFS.htm" target="_blank">Driver--the Java API for SAFS testing</a>.
 * <p>
 * The class does NOT subclass any existing SAFS class like {@link DefaultDriver}.
 * <p>
 * Instead, the class instantiates a reference to a {@link JSAFSDriver} that handles all 
 * the interfacing with SAFS for the developer.  The JSAFSDriver will expect a Java System 
 * property to point to the INI file it would use for initialization.  Like:
 * <p>
 * -Dsafs.project.config="pathTo\Driver.INI"
 * <p>
 * In this example, the JSAFSDriver is initialized in {@link #main(String[])} when this 
 * EmbeddedJSAFSDriver class is executed in the JVM.
 * <p>
 * This example also show how one might reuse the same Java classes for NLS testing by passing 
 * in parameters for the primary and localized App Maps (Resources) to be used for testing.
 * <p>
 * Example -D System Properties set during command-line invocation:
 * <p>
 * -Dmain_map="MyMainApp.map" -Dlocal_map="MyLocalApp_en.map"  or<br>
 * -Dmain_map="MyMainApp.map" -Dlocal_map="MyLocalApp_zh_CN.map"
 * <p>
 *  
 * @author canagl
 */
public class EmbeddedJSAFSDriver {


	/** 
	 * "main_map"   
	 * <p>
	 * In our example, this should be another -D System Property passed in at invocation.  
	 * <p>
	 * Example:<p>
	 * -Dmain_map="MyMainApp.map"
	 * <p>
	 * This tells the test which primary (non-localizable) App Map should be used for the test.
	 */
	public static final String MAIN_MAP = "main_map";
	/** 
	 * "local_map"   
	 * <p>
	 * In our example, this should be another -D System Property passed in at invocation.  
	 * <p>
	 * Example:<p>
	 * -Dlocal_map="MyMainApp_en.map"  or<br>
	 * -Dlocal_map="MyMainApp_zh_CN.map"
	 * <p>
	 * This tells the test which localized resource should be used for the test.
	 */
	public static final String LOCAL_MAP = "local_map";

	/** "LoginWindow" 
	 * App Map window name for Login Window.
	 * To be used when the String name of the window is needed. */
	public static final String LoginWindow = "LoginWindow";
	/** "LoginUserID"
	 * App Map component name for Login Window UserID EditBox. 
	 * To be used when the String name of the component is needed. */
	public static final String LoginUserID = "LoginUserID";
	/** "LoginPassword"
	 * App Map component name for Login Window Password EditBox 
	 * To be used when the String name of the component is needed. */
	public static final String LoginPassword = "LoginPassword";
	/** "LoginSubmit"
	 * App Map component name for Login Window Submit Button 
	 * To be used when the String name of the component is needed. */
	public static final String LoginSubmit = "LoginSubmit";
	/** "MainWindow"
	 * App Map window name for main application window (after successful Login) 
	 * To be used when the String name of the window is needed. */
	public static final String MainWindow = "MainWindow";

	// below shows another way of referencing GUI components with Driver API Model
	
	/** Optional {@link Window} reference to our Login Window 
	 * To be used when a {@link Component} reference might be desired instead of a String name. */
	public static final Window LoginWindowUI = new Window(LoginWindow);
	/** Optional {@link EditBox} reference to our Login UserID field 
	 * To be used when a {@link Component} reference might be desired instead of a String name. */
	public static final EditBox Login_UserID = new EditBox(LoginWindowUI, LoginUserID);
	/** Optional {@link EditBox} reference to our Login Password field 
	 * To be used when a {@link Component} reference might be desired instead of a String name. */
	public static final EditBox Login_Password = new EditBox(LoginWindowUI, LoginPassword);	
	/** Optional {@link PushButton} reference to our Login Submit button 
	 * To be used when a {@link Component} reference might be desired instead of a String name. */
	public static final PushButton Login_Submit = new PushButton(LoginWindowUI, LoginSubmit);
	
	/** Optional {@link Window} reference to our Main Window. 
	 * To be used when a {@link Component} reference might be desired instead of a String name. */
	public static final Window MainWindowUI = new Window(MainWindow);
	
	/**
	 * A readily accessible reference to the instantiated JSAFSDriver used by the test.  
	 * new JSAFSDriver("Driver");
	 */
	public JSAFSDriver jsafs = new JSAFSDriver("Driver");
	
	public EmbeddedJSAFSDriver(){
		Driver.setIDriver(jsafs);
	}
	
	/**
	 * Possible main entry point for the test class.
	 * The JVM should be instanced with required -D Property settings used by this example:
	 * <p> 
	 * java -Dsafs.project.config="pathTo\Driver.INI" -Dmain_map="MyMainApp.map" -Dlocal_map="MyLocalApp_en.map" EmbeddedJSAFSDriver
	 * <p>
	 * This example performs the following:
	 * <p>
	 * <ul>
	 * EmbeddedJSAFSDriver driver = new EmbeddedJSAFSDriver();
	 * driver.jsafs.run();
	 * driver.initializeTests();
	 * driver.executeTests();
	 * driver.cleanupTests();
	 * driver.jsafs.shutdown();
	 * </ul>
	 * @param args
	 */
	public static void main(String[] args) {
		
		EmbeddedJSAFSDriver driver = new EmbeddedJSAFSDriver();
		driver.jsafs.run();		
		driver.initializeTest();			
		driver.executeTests();		
		driver.cleanupTests();
		driver.jsafs.shutdown();	
	}

	/**
	 * Any initialization of the test environment and assets.  
	 * IN this example, this is called immediately after the JSAFSDriver initializes all 
	 * of the SAFS framework and services.
	 */
	private void initializeTest(){

		String sep = ",";

		//do any non-SAFS initialization that might be necessary....
		
		//then:
		//these few lines show a way to invoke Driver Commands if you already know what 
		//Java Class implements them.
		TestRecordHelper trd = jsafs.runDriverCommand(DDDriverCommands.appMapChaining("ON"), sep);
		trd = jsafs.runDriverCommand(DDDriverCommands.appMapResolve("ON"), sep);
		trd = jsafs.runDriverCommand(DDDriverCommands.expressions("ON"), sep);
		
		//find out which Main and Localized App Maps were specified on the command-line
		String mainmap = System.getProperty(MAIN_MAP);
		String localmap = System.getProperty(LOCAL_MAP);
		
		//these next few lines show an alternative when you don't know which Java Class 
		//implements specific Driver Commands, but boy is that list LONG!
		trd = jsafs.runDriverCommand(DriverCommands.setApplicationMap(mainmap), sep);
		trd = jsafs.runDriverCommand(DriverCommands.setApplicationMap(localmap), sep);
		
		//do any last bits of initialization that might be necessary...		
	}

	private void executeTests() {
		// TODO Auto-generated method stub
	/*	LaunchSwingApp();
		CalculateMortgage("360", "150000", "4.65");
		VerifyMortgagePayment("^payment_579");
		CloseSwingApp();
	*/
		jsafs.runComponentFunction(JSAFSMap.LoginWindow.Submit.click());
	}
	/**
	 * Perform whatever cleanup may be required by your test environment.  
	 * In this example this is the final call before JSAFSDriver and all of SAFS is shutdown.
	 */
	private void cleanupTests() {
		Driver.setIDriver(null);
	}
}
