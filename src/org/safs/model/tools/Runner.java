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
package org.safs.model.tools;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.Component;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSConfiguredClassStore;
import org.safs.model.annotations.Utilities;
import org.safs.model.commands.ComboBoxFunctions;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * This Runner is an access point to a minimalist JSAFS Driver API.
 * @author Carl Nagle
 */
public class Runner implements JSAFSConfiguredClassStore{

	private static Driver driver;
	private static Vector callers = new Vector();
    private static Hashtable<String, Object> instances = new Hashtable<String,Object>();
	private static void debug(String message){
		System.out.println(message);
		//Log.debug(message);
	}
	private static Runner _instance;

	/**
	 * Gain access to the instance interface to a Runner.
	 * The user can also call {@link Runner#getRunnerInstance()} instead.
	 * @see #getRunnerInstance()
	 */
	public Runner(){
		super();
		if (_instance == null)_instance = this;
	}

	/**
	 * Will invoke the Driver shutdown mechanism--which includes the shutdown of JSAFS.
	 * @throws Exception
	 * @see Driver#shutdownJSAFS()
	 */
	public static void shutdown() throws Exception{
		if(driver != null){
			driver().allow_shutdown = true;
			driver().shutdownJSAFS();
		}
	}

	/** Get hold of the already running instance of the Runner.
	  * If one is not already instanced, it will be created. */
	public static Runner getRunnerInstance(){
		if(_instance == null) new Runner();
		return _instance;
	}
	/**
	 * retrieve access to the minimalist Driver API, if needed.
	 * If the Driver has NOT been instantiated, calling this routine will cause
	 * the Driver to instantiate and run the Driver beforeAll method.
	 * @see Driver#beforeAll()
	 */
	public static Driver driver(){
		if(driver == null){
			driver = new Driver();
			driver.beforeAll();
		}
		return driver;
	}
	/**
	 * Primarily for backward compatibility.
	 * retrieve access to the full JSAFS Driver API, if needed and available.
	 * @return JSAFSDriver.  Can be null if the runtime system is NOT using a JSAFSDriver.
	 * @see #iDriver()
	 **/
	public static JSAFSDriver jsafs(){
		try{ return driver().jsafs(); }
		catch(Exception np){ return null; }
	}

	/**
	 * retrieve access to the the active DriverInterface, if needed and available.
	 * @return DriverInterface.  Can be a JSAFSDriver or other DriverInterface.
	 **/
	public static DriverInterface iDriver(){
		return driver().iDriver();
	}

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
	@Override
	public Object getConfiguredClassInstance(String classname){
		return classname== null ? null:instances.get(classname);
	}
	/** normally only used internally to store objects as we instantiate them.*/
	@Override
	public void addConfiguredClassInstance(String classname, Object object){
		instances.put(classname, object);
	}

	/**
	 * Log a GENERIC message to the current test log.
	 * This is an informative message, not a PASSED(OK) or FAILED message.
	 * This shows no special flag or marker in the test log.
	 * @param message
	 * @param detail
	 * @see JSAFSDriver#logGENERIC(String, String)
	 */
	public static void logGENERIC(String message, String detail){
	    iDriver().logMessage(message, detail, AbstractLogFacility.GENERIC_MESSAGE);
	    iDriver().incrementGeneralStatus(StatusCodes.NO_SCRIPT_FAILURE);
	}

	/**
	 * Log a PASSED message to the current test log.
	 * This is a message intended to indicate that an important test, step, or feature
	 * has PASSED.  This usually shows a visible PASSED or OK flag in the test log.
	 * @param message
	 * @param detail
	 * @see JSAFSDriver#logPASSED(String, String)
	 */
	public static void logPASSED(String message, String detail){
	    iDriver().logMessage(message, detail,AbstractLogFacility.PASSED_MESSAGE);
	    iDriver().incrementTestStatus(StatusCodes.NO_SCRIPT_FAILURE);
	}

	/**
	 * Log a FAILED message to the current test log.
	 * This is a message intended to indicate that an important test, step, or feature
	 * has FAILED.  This usually shows a visible failure flag in the test log.
	 * @param message
	 * @param detail
	 * @see JSAFSDriver#logFAILED(String, String)
	 */
	public static void logFAILED(String message, String detail){
	    iDriver().logMessage(message, detail, AbstractLogFacility.FAILED_MESSAGE);
	    iDriver().incrementTestStatus(StatusCodes.GENERAL_SCRIPT_FAILURE);
	}

	/**
	 * This is the critical method users would call to commence the automatic
	 * instantiation, configuration, and execution of JSAFSTest methods.
	 * <p>
	 * Minimalist example:
	 * <p><ul><pre>
	 *     public static void main(String[] args)throws Throwable{
	 *         MyTestApp app = new MyTestApp();
	 *
	 *         new Runner().autorun(args);
	 *         ...
	 *         Runner.shutdown();
	 *     }
	 * </pre></ul>
	 * <p>
	 * Automatic configuration and usage is not required.  The user can control
	 * test configuration and execution within their custom code if they want.
	 *
	 * @param args passed in from command-line Java-- the primordial main(String[] args)
	 * @throws Throwable
	 * @see org.safs.model.annotations.JSAFSTest
	 */
	public void autorun(String[] args) throws Throwable{
		boolean found = false;
		String reflectStr;
		String classname = "bogus";
		for(int i=0;!found && i<4;i++){
			reflectStr= sun.reflect.Reflection.getCallerClass(i).getName();
			debug("Runner.autoConfigure trace["+ i +"]:"+ reflectStr);
			if(!reflectStr.equals(sun.reflect.Reflection.class.getName()) &&
			   !reflectStr.equals(Thread.class.getName()) &&
			   !reflectStr.equals(Runner.class.getName())){
				found = true;
				classname = reflectStr;
			}
		}
		if(found && !callers.contains(classname)){
			debug("Runner.autorun processing calling class "+classname);
			callers.add(classname);
			Class c = Class.forName(classname);
			// we might not require the AutoConfigureJSAFS Annotation.
			// If they called us, they want us to do it.
			if(c.isAnnotationPresent(AutoConfigureJSAFS.class)){
			    Utilities.autoConfigure(classname, _instance);
			}
		}else{
			if(!found) debug("Runner.autorun calling class WAS NOT detected.");
			else       debug("Runner.autorun class "+ classname +" was previously processed.");
		}
	}

	/**
	 * Run any SAFS ComponentFunction not already in a convenience wrapper routine.
	 * <p>
	 * Ex:
	 * <p>
	 * result = Runner.action("GetGUIImage", "ChildX", "MainWin", "ChildXImage.png");
	 * <p>
	 * @param keyword
	 * @param child
	 * @param parent
	 * @param params
	 * @return TestR
	 * @throws Throwable
	 */
	public static TestRecordHelper action(String keyword, String child, String parent, String... params) throws Throwable{
		TestRecordHelper rc = driver().runComponentFunction(keyword, child, parent, params );
		driver().iDriver().incrementTestStatus(rc.getStatusCode());
		if(rc.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			iDriver().logMessage(child +" "+keyword.toUpperCase() +" did NOT execute!",
				    "Support for this action may not be available in this runtime environment.",
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
		return rc;
	}

	/** <pre>
	 * Run any SAFS ComponentFunction not already in a convenience wrapper routine.
	 * <p>
	 * result = Runner.action(AppMap.MainWin.ChildComp, "GetGUIImage", "ChildXImage.png");
	 * <p>
     * If any param value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
     * </pre>
	 * @param keyword
	 * @param child
	 * @param parent
	 * @param params
	 * @return TestR
	 * @throws Throwable
	 */
	public static TestRecordHelper action(Component comp, String keyword, String... params) throws Throwable{
		String parent = comp.getParentName()==null ? comp.getName():comp.getParentName();
		return action(keyword, comp.getName(), parent, params );
	}

	/** <pre>
	 * Run any SAFS DriverCommand not already in a convenience wrapper routine.
	 * <p>
	 * result = Runner.command("Pause", "10");
	 * <p>
     * If any param value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
     * </pre>
	 * @param keyword
	 * @param child
	 * @param parent
	 * @param params
	 * @return TestR
	 * @throws Throwable
	 */
	public static TestRecordHelper command(String keyword, String... params) throws Throwable{
		TestRecordHelper rc = driver().runDriverCommand(keyword, params );
		iDriver().incrementGeneralStatus(rc.getStatusCode());
		if(rc.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			iDriver().logMessage(keyword.toUpperCase() +" did NOT execute!",
				    "Support for this command may not be available in this runtime environment.",
				    AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		}
	    return rc;
	}

	/**
	 * Sets one or more SAFS variable values for use by the SAFS system.
	 * The setting is based on SAFS processing expressions that contain assignments.
	 * <p>
     * Ex:
     * <p><pre>
     * results = SetVariableValues("^safsVarName = Value");
     * results = SetVariableValues("^safsVarName = "+ javaVarReference);
     * results = SetVariableValues("^safsVarName = ^otherSafsVar & "+ javaVarReference);
     * results = SetVariableValues("^safsVarName = ^safsVarNumber + "+ javaVarNumber);
     * results = SetVariableValues("^safsVarName = \"a + b = c\"");
	 * </pre>
	 * @param expressions
     * If an expression contains any spaces, special characters, or expression operators
     * that should be considered as literal text then double-quotes should be embedded to surround the
     * literal text portions of the expression.<br>
	 * @return
	 * @throws Throwable
	 * @see #SetVariableValue(String, String)
	 * @see #GetVariableValue(String)
	 */
	public static TestRecordHelper SetVariableValues(String... expressions) throws Throwable{
		return command(DDDriverCommands.SETVARIABLEVALUES_KEYWORD, expressions);
	}

	/**
	 * Pause the test the specified number of seconds.  This gives the application and the system
	 * time to accomplish needed tasks.
	 * @param seconds
	 * The number of seconds to PAUSE the test before automatically resuming.
	 * @return
	 * @throws Throwable
	 */
	public static void Pause(int seconds) throws Throwable{
		command(DDDriverCommands.PAUSE_KEYWORD, String.valueOf(seconds));
	}

	/**
	 * Provide the Application Map for the test to use. <br>
	 * This command must be used prior to ANY other command or script trying to reference the AppMap contents.<br>
	 * @param mapName - The name of the text-based runtime AppMap to load (not the Java Map class that might be used in code).
	 * @throws Throwable
	 */
	public static void SetApplicationMap(String mapName) throws Throwable{
		command(DDDriverCommands.SETAPPLICATIONMAP_KEYWORD, mapName);
	}

	/**
	 * Identify and Launch a specified application.<br>
	 *
	 * @param appID -- id for this app to use in CloseApplication.
	 * @param executable -- The path and filename to the executable OR an ApplicationConstant.<br>
	 * This can and should include the full command line syntax with application specific command line parameters
	 * unless the application is unable to successfully handle this invocation.<br>
	 * This parameter may instead contain a reference to an ApplicationConstant from the currently active Application Map.
	 * The value of the retrieved constant will be used as the executable path.
	 * @param optionals -- if used must be specified in proper order.<br>
	 * Use "" empty strings to skip parameters you don't want to use:<br>
	 * <b>workdir</b> - working directory for the application (if required)<br>
	 * <b>cmdlineparams</b> - one string of separate command line parameters for the application (if required)<br>
	 * <b>appmap</b> - filename of the application map associated with the application (if required)<br>
	 * @see #CloseApplication(String)
	 * @throws Throwable
	 */
	public static void LaunchApplication(String appID, String executable, String...optionals) throws Throwable{
		if (optionals==null) optionals = new String[0];
		ArrayList<String> ps = new ArrayList<String>();
		ps.add(appID);
		ps.add(executable);
		for(String item:optionals){
			ps.add(item);
		}
		command(DDDriverCommands.LAUNCHAPPLICATION_KEYWORD, ps.toArray(new String[ps.size()]));
	}

	/**
	 * Close an application that was launched with LaunchApplication.
	 * @param appID
	 * @see #LaunchApplication(String, String, String...)
	 * @throws Throwable
	 */
	public static void CloseApplication(String appID) throws Throwable{
		command(DDDriverCommands.CLOSEAPPLICATION_KEYWORD, appID);
	}

	/**
	 * Sets a single SAFS variable value for use by the SAFS system.
	 * The varName and varValue are used as is--no special SAFS expression processing.
	 * <p>
	 * @param varName
	 * @param varValue
	 * @throws Throwable
	 * @see #GetVariableValue(String)
	 * @see #SetVariableValues(String...)
	 */
	public static void SetVariableValue(String varName, String varValue) throws Throwable{
		iDriver().getCoreInterface().setVariable(varName, varValue);
		logGENERIC("Variable "+ varName +" set to '"+ varValue +"'", null);
	}

	/**
	 * Get a single SAFS variable value from the SAFS system.
	 * <p>
	 * @param varName
	 * @throws Throwable
	 * @see #SetVariableValue(String, String)
	 * @see #SetVariableValues(String...)
	 */
	public static String GetVariableValue(String varName) throws Throwable {
		String val = iDriver().getCoreInterface().getVariable(varName);
		logGENERIC("Variable "+ varName +" retrieved as '"+ val+"'", null);
		return val;
	}

	/**
	 * Verify that two string values are equal.
	 * This is provided here to automatically record in the test log the pass/fail results of the evaluation.
	 *
     * @param  value1 -- case-sensitive value to compare.
     * @param  value2 -- case-sensitive value to compare.
	 * @return
	 */
	public static TestRecordHelper VerifyValues(String value1, String value2) throws Throwable{
		return action("VerifyValues", "AnyComp", "AnyWin", new String[]{value1, value2});
	}

	/**
	 * Verify that two string values are NOT equal.
	 * This is provided here to automatically record in the test log the pass/fail results of the evaluation.
	 *
     * @param  value1 -- case-sensitive value to compare.
     * @param  value2 -- case-sensitive value to compare.
	 * @return
	 */
	public static TestRecordHelper VerifyValuesNotEqual(String value1, String value2) throws Throwable{
		return action("VerifyValuesNotEqual", "AnyComp", "AnyWin", new String[]{value1, value2});
	}

	/**
	 * Verify the value contains the substring.
	 * This is provided here to automatically record in the test log the pass/fail results of the evaluation.
	 *
     * @param  value -- case-sensitive value to compare.
     * @param  substring -- case-sensitive substring to seek.
	 * @return
	 */
	public static TestRecordHelper VerifyValueContains(String value, String substring) throws Throwable{
		return action("VerifyValueContains", "AnyComp", "AnyWin", new String[]{value, substring});
	}

	/**
	 * Verify the value does NOT contain the substring.
	 * This is provided here to automatically record in the test log the pass/fail results of the evaluation.
	 *
     * @param  value -- case-sensitive value to compare.
     * @param  substring -- case-sensitive substring to seek.
	 * @return
	 */
	public static TestRecordHelper VerifyValueDoesNotContain(String value, String substring) throws Throwable{
		return action("VerifyValueDoesNotContain", "AnyComp", "AnyWin", new String[]{value, substring});
	}

	/** <pre>
	 * Sends keystrokes to the specified component.
	 *
     * Some Special Characters:
     *
     *               {Enter}= ENTER key
     *               {Tab} = TAB key
     *               ^ = CONTROL Key with another key ( "^S" = CONTROL + s)
     *               %= ALT  Key with another key ("%F" = ALT + F)
     *               + = SHIFT key with another key ("+{Enter}" = SHIFT + ENTER)
     * </pre>
     * @param  Component to receive keystrokes.
     * @param  keys String of keystrokes to send.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""<br>
     * Note: <a href="/doc/org/safs/tools/input/CreateUnicodeMap.html">InputKeys Special Characters</a> used across most engines.
	 * @return
	 */
	public static TestRecordHelper InputKeys(Component comp, String keys) throws Throwable{
		return action(comp, GenericMasterFunctions.INPUTKEYS_KEYWORD, keys);
	}

	/** <pre>
	 * Sends keystrokes to the specified component.
	 * No handling of special characters is performed with the keystrokes.
     * </pre>
     * @param  Component to receive keystrokes.
     * @param  keys String of keystrokes to send.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""<br>
	 * @return
	 */
	public static TestRecordHelper InputCharacters(Component comp, String keys) throws Throwable{
		return action(comp, GenericMasterFunctions.INPUTCHARACTERS_KEYWORD, keys);
	}

	/**
	 * Verify the value of a property on the component.
	 *
	 * @param  comp -- Component to check for the property value.
     * @param  propertyName -- case-sensitive name of the property.
     * @param  expectedValue -- value expected to be found.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
     * @param isCaseSensitive -- whether the expectedValue comparison is case-sensitive, or not.
	 * @return
	 */
	public static TestRecordHelper VerifyProperty(Component comp, String propertyName, String expectedValue, boolean isCaseSensitive ) throws Throwable{
		return action(comp, GenericMasterFunctions.VERIFYPROPERTY_KEYWORD, new String[]{propertyName, expectedValue, String.valueOf(isCaseSensitive)});
	}

	/**
	 * Verify the value of a property on the component contains a specific substring (partial match).
	 *
	 * @param  comp -- Component to check for the property value.
     * @param  propertyName -- case-sensitive name of the property.
     * @param  substringValue -- substring value expected to be found.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
     * @param isCaseSensitive -- whether the substringValue comparison is case-sensitive, or not.
	 * @return
	 */
	public static TestRecordHelper VerifyPropertyContains(Component comp, String propertyName, String substringValue, boolean isCaseSensitive ) throws Throwable{
		return action(comp, GenericMasterFunctions.VERIFYPROPERTYCONTAINS_KEYWORD, new String[]{propertyName, substringValue, String.valueOf(isCaseSensitive)});
	}

	/**
	 * Set the value of a supported EditBox or TextField.
	 *
	 * @param  textfield -- Editable Component to change the value on.
     * @param  text -- text to set.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
	 * @return
	 */
	public static TestRecordHelper SetTextValue(Component textfield, String... text ) throws Throwable{
		return action(textfield, EditBoxFunctions.SETTEXTVALUE_KEYWORD, text);
	}


	/** <pre>
	 * Set the value of a supported EditBox or TextField.
	 * No verification is performed on the value after it has been set.
	 *</pre>
	 * @param  textfield -- Editable Component to change the value on.
     * @param  text -- text to set.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
	 * @return
	 */
	public static TestRecordHelper SetUnverifiedTextCharacters(Component textfield, String... text ) throws Throwable{
		return action(textfield, EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD, text);
	}


	/** <pre>
	 * Set the value of a supported EditBox or TextField.
	 * No verification is performed on the value after it has been set.
	 *</pre>
	 * @param  list -- ComboBox or List Component to change the value on.
     * @param  text -- text to set.
     * If the value contains any spaces, special characters, or expression operators
     * then double-quotes should be embedded to surround the string to avoid expression processing.<br>
     * Ex: "\"a + b = c \""
	 * @return
	 */
	public static TestRecordHelper Select(Component list, String... text ) throws Throwable{
		return action(list, ComboBoxFunctions.SELECT_KEYWORD, text);
	}


    /*********** <pre>
    A single click on an object.

    By default, clicks on the center of the component.
    We can also click on any part of an object, or any point relative to an object
    based on a provided x,y coordinate or other component-specific parameters.

    The object to be clicked is first given context and then a click is
    generated at the coordinates.  Thus, a subitem or object can be
    referenced by name even though it is only recognized via coordinates.

    The coordinate lookup is done in the App Map using the child component name AND
    the first item in the optional String params.  If not passing any
    optional params then the params should be null-- (String[])null.

    Typical SAFS Data Table records:

    (1) t MainWindow MainWindow  Click
    (2) t MainWindow MainWindow  Click AnObject
    (3) t MainWindow FolderTree  Click Node1
    (4) t MainWindow MainWindow  Click "50 200"
    (5) t MainWindow MainWindow  Click "Coords=50 200"

    Matching Runner invocations:

    (1) Runner.Click(AppMap.MainWindow.MainWindow, null);
    (2) Runner.Click(AppMap.MainWindow.MainWindow, "AnObject");
    (3) Runner.Click(AppMap.MainWindow.FolderTree, "Node1");
    (4) Runner.Click(AppMap.MainWindow.MainWindow, "50;200";
    (5) Runner.Click(AppMap.MainWindow.MainWinow, "Coords=50;200";


    #2 above will expect the AppMap to contain an AnObject="3,10" entry in the MainWindow
    section of the currently active AppMap to click at x=3, y=10 in the MainWindow.

    #3 above will contain a FolderTree entry in the MainWindow section with
    normal recognition information for FolderTree.  There will also be a separate FolderTree
    section in the AppMap in which there will be an entry like Node1="15 30".
    This will tell the automation to locate the FolderTree Generic object and click at the
    coordinates specified by the AppMap reference.

    #4 and #5 above show using literal text instead of an AppMap entry to specify
    where to click relative to the item.  Note the use of the "Coords=" prefix
    on the value is optional.

    Engines should also attempt to support coordinates separated by alternate separators.
    The most common separators that should be supported would be:

    "," (comma) Example: "50,200"
    ";" (semi-colon) Example: "50;200"
    " " (space) Example: "50 200"

    Note: the TID supports this command using <a href="/sqabasic2000/SAFSImageBasedRecognition.htm" target="_blank" title="SAFS Image-Based Testing Overview" alt="SAFS Image-Based Testing Overview">Image-Based Testing</a> techniques.

    For IOS: Any optional coordinates MUST be specified as an integer number between
    0-100.  0 represents the extreme left (or top), while 100 represents the extreme
    right (or bottom). IOS does not use absolute coordinates, but relative coordinates
    representing a percentage of the element width or height.

    </pre>

	@param Component  Optional:NO
	A Component reference to the child component to be clicked.  The Component reference should have the
	embedded Component reference to its parent.
	@param params  Optional:YES
	Should be null:(String[])null if not passing any parameters
	@param appMapSubkey  Optional:YES
	Name of the AppMap subkey for lookup or the literal text to use for the click.

	**********/
	public static TestRecordHelper Click(Component comp, String... params) throws Throwable{
		return action(comp, GenericObjectFunctions.CLICK_KEYWORD, params );
	}

    /*********** <pre>
    A single RightClick on an object.

    By default, clicks on the center of the component.
    We can also click on any part of an object, or any point relative to an object
    based on a provided x,y coordinate or other component-specific parameters.

    The object to be clicked is first given context and then a click is
    generated at the coordinates.  Thus, a subitem or object can be
    referenced by name even though it is only recognized via coordinates.

    The coordinate lookup is done in the App Map using the child component name AND
    the first item in the optional String params.  If not passing any
    optional params then the params should be null-- (String[])null.

    Typical SAFS Data Table records:

    (1) t MainWindow MainWindow  RightClick
    (2) t MainWindow MainWindow  RightClick AnObject
    (3) t MainWindow FolderTree  RightClick Node1
    (4) t MainWindow MainWindow  RightClick "50 200"
    (5) t MainWindow MainWindow  RightClick "Coords=50 200"

    Matching Runner invocations:
    (1) Runner.RightClick(AppMap.MainWindow.MainWindow, null);
    (2) Runner.RightClick(AppMap.MainWindow.MainWindow, "AnObject");
    (3) Runner.RightClick(AppMap.MainWindow.FolderTree, "Node1");
    (4) Runner.RightClick(AppMap.MainWindow.MainWindow, "50;200";
    (5) Runner.RightClick(AppMap.MainWindow.MainWinow, "Coords=50;200";


    #2 above will expect the AppMap to contain an AnObject="3,10" entry in the MainWindow
    section of the currently active AppMap to click at x=3, y=10 in the MainWindow.

    #3 above will contain a FolderTree entry in the MainWindow section with
    normal recognition information for FolderTree.  There will also be a separate FolderTree
    section in the AppMap in which there will be an entry like Node1="15 30".
    This will tell the automation to locate the FolderTree Generic object and click at the
    coordinates specified by the AppMap reference.

    #4 and #5 above show using literal text instead of an AppMap entry to specify
    where to click relative to the item.  Note the use of the "Coords=" prefix
    on the value is optional.

    Engines should also attempt to support coordinates separated by alternate separators.
    The most common separators that should be supported would be:

    "," (comma) Example: "50,200"
    ";" (semi-colon) Example: "50;200"
    " " (space) Example: "50 200"

    Note: the TID supports this command using <a href="/sqabasic2000/SAFSImageBasedRecognition.htm" target="_blank" title="SAFS Image-Based Testing Overview" alt="SAFS Image-Based Testing Overview">Image-Based Testing</a> techniques.

    For IOS: Any optional coordinates MUST be specified as an integer number between
    0-100.  0 represents the extreme left (or top), while 100 represents the extreme
    right (or bottom). IOS does not use absolute coordinates, but relative coordinates
    representing a percentage of the element width or height.

    </pre>

	@param Component  Optional:NO
	A Component reference to the child component to be clicked.  The Component reference should have the
	embedded Component reference to its parent.
	@param String[]  Optional:YES
	Should be null:(String[])null if not passing any parameters
	@param appMapSubkey  Optional:YES
	Name of the AppMap subkey for lookup or the literal text to use for the click.

	**********/
	public static TestRecordHelper RightClick(Component comp, String... params) throws Throwable{
		return action(comp, GenericObjectFunctions.RIGHTCLICK_KEYWORD, params );
	}

    /*********** <pre>
    A CTRL-click on an object.

    By default, clicks on the center of the component.
    We can also click on any part of an object, or any point relative to an object
    based on a provided x,y coordinate or other component-specific parameters.

    The object to be clicked is first given context and then a click is
    generated at the coordinates.  Thus, a subitem or object can be
    referenced by name even though it is only recognized via coordinates.

    The coordinate lookup is done in the App Map using the child component name AND
    the first item in the optional String params.  If not passing any
    optional params then the params should be null-- (String[])null.

    Typical SAFS Data Table records:

    (1) t MainWindow MainWindow  CtrlClick
    (2) t MainWindow MainWindow  CtrlClick AnObject
    (3) t MainWindow FolderTree  CtrlClick Node1
    (4) t MainWindow MainWindow  CtrlClick "50 200"
    (5) t MainWindow MainWindow  Click "Coords=50 200"

    Matching Runner invocations:
    (1) Runner.ControlClick(AppMap.MainWindow.MainWindow, null);
    (2) Runner.ControlClick(AppMap.MainWindow.MainWindow, "AnObject");
    (3) Runner.ControlClick(AppMap.MainWindow.FolderTree, "Node1");
    (4) Runner.ControlClick(AppMap.MainWindow.MainWindow, "50;200";
    (5) Runner.ControlClick(AppMap.MainWindow.MainWinow, "Coords=50;200";


    #2 above will expect the AppMap to contain an AnObject="3,10" entry in the MainWindow
    section of the currently active AppMap to click at x=3, y=10 in the MainWindow.

    #3 above will contain a FolderTree entry in the MainWindow section with
    normal recognition information for FolderTree.  There will also be a separate FolderTree
    section in the AppMap in which there will be an entry like Node1="15 30".
    This will tell the automation to locate the FolderTree Generic object and click at the
    coordinates specified by the AppMap reference.

    #4 and #5 above show using literal text instead of an AppMap entry to specify
    where to click relative to the item.  Note the use of the "Coords=" prefix
    on the value is optional.

    Engines should also attempt to support coordinates separated by alternate separators.
    The most common separators that should be supported would be:

    "," (comma) Example: "50,200"
    ";" (semi-colon) Example: "50;200"
    " " (space) Example: "50 200"

    Note: the TID supports this command using <a href="/sqabasic2000/SAFSImageBasedRecognition.htm" target="_blank" title="SAFS Image-Based Testing Overview" alt="SAFS Image-Based Testing Overview">Image-Based Testing</a> techniques.

    For IOS: Any optional coordinates MUST be specified as an integer number between
    0-100.  0 represents the extreme left (or top), while 100 represents the extreme
    right (or bottom). IOS does not use absolute coordinates, but relative coordinates
    representing a percentage of the element width or height.

    </pre>

	@param Component  Optional:NO
	A Component reference to the child component to be clicked.  The Component reference should have the
	embedded Component reference to its parent.
	@param String[]  Optional:YES
	Should be null:(String[])null if not passing any parameters
	@param appMapSubkey  Optional:YES
	Name of the AppMap subkey for lookup or the literal text to use for the click.

    **********/
	public static TestRecordHelper ControlClick(Component comp, String... params) throws Throwable{
		return action(comp, GenericObjectFunctions.CTRLCLICK_KEYWORD, params);
	}

    /*********** <pre>
    A double click on an object.

    By default, clicks on the center of the component.
    We can also click on any part of an object, or any point relative to an object
    based on a provided x,y coordinate or other component-specific parameters.

    The object to be clicked is first given context and then a click is
    generated at the coordinates.  Thus, a subitem or object can be
    referenced by name even though it is only recognized via coordinates.

    The coordinate lookup is done in the App Map using the child component name AND
    the first item in the optional String params.  If not passing any
    optional params then the params should be null-- (String[])null.

    Typical SAFS Data Table records:

    (1) t MainWindow MainWindow  DoubleClick
    (2) t MainWindow MainWindow  DoubleClick AnObject
    (3) t MainWindow FolderTree  DoubleClick Node1
    (4) t MainWindow MainWindow  DoubleClick "50 200"
    (5) t MainWindow MainWindow  DoubleClick "Coords=50 200"

    Matching Runner invocations:

    (1) Runner.DoubleClick(AppMap.MainWindow.MainWindow, null);
    (2) Runner.DoubleClick(AppMap.MainWindow.MainWindow, "AnObject");
    (3) Runner.DoubleClick(AppMap.MainWindow.FolderTree, "Node1");
    (4) Runner.DoubleClick(AppMap.MainWindow.MainWindow, "50;200";
    (5) Runner.DoubleClick(AppMap.MainWindow.MainWinow, "Coords=50;200";


    #2 above will expect the AppMap to contain an AnObject="3,10" entry in the MainWindow
    section of the currently active AppMap to click at x=3, y=10 in the MainWindow.

    #3 above will contain a FolderTree entry in the MainWindow section with
    normal recognition information for FolderTree.  There will also be a separate FolderTree
    section in the AppMap in which there will be an entry like Node1="15 30".
    This will tell the automation to locate the FolderTree Generic object and click at the
    coordinates specified by the AppMap reference.

    #4 and #5 above show using literal text instead of an AppMap entry to specify
    where to click relative to the item.  Note the use of the "Coords=" prefix
    on the value is optional.

    Engines should also attempt to support coordinates separated by alternate separators.
    The most common separators that should be supported would be:

    "," (comma) Example: "50,200"
    ";" (semi-colon) Example: "50;200"
    " " (space) Example: "50 200"

    Note: the TID supports this command using <a href="/sqabasic2000/SAFSImageBasedRecognition.htm" target="_blank" title="SAFS Image-Based Testing Overview" alt="SAFS Image-Based Testing Overview">Image-Based Testing</a> techniques.

    For IOS: Any optional coordinates MUST be specified as an integer number between
    0-100.  0 represents the extreme left (or top), while 100 represents the extreme
    right (or bottom). IOS does not use absolute coordinates, but relative coordinates
    representing a percentage of the element width or height.

    </pre>

	@param Component  Optional:NO
	A Component reference to the child component to be clicked.  The Component reference should have the
	embedded Component reference to its parent.
	@param String[]  Optional:YES
	Should be null:(String[])null if not passing any parameters
	@param appMapSubkey  Optional:YES
	Name of the AppMap subkey for lookup or the literal text to use for the click.

    **********/
	public static TestRecordHelper DoubleClick(Component comp, String... params) throws Throwable{
		return action(comp, GenericObjectFunctions.DOUBLECLICK_KEYWORD, params);
	}
}
