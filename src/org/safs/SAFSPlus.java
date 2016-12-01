/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 07, 2016    (SBJLWA) Initial release: Moved codes from SeleniumPlus to here.
 * DEC 01, 2016    (SBJLWA) Added method Compare(): to compare with a regex.
 */
package org.safs;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.safs.Constants.BrowserConstants;
import org.safs.image.ImageUtils.SubArea;
import org.safs.model.annotations.Utilities;
import org.safs.model.commands.CheckBoxFunctions;
import org.safs.model.commands.ComboBoxFunctions;
import org.safs.model.commands.DDDriverRestCommands;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.DDDriverCounterCommands;
import org.safs.model.commands.DDDriverFileCommands;
import org.safs.model.commands.DDDriverFlowCommands;
import org.safs.model.commands.DDDriverLogCommands;
import org.safs.model.commands.DDDriverStringCommands;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.model.commands.JavaMenuFunctions;
import org.safs.model.commands.ListViewFunctions;
import org.safs.model.commands.ScrollBarFunctions;
import org.safs.model.commands.TIDRestFunctions;
import org.safs.model.commands.TabControlFunctions;
import org.safs.model.commands.TreeViewFunctions;
import org.safs.model.commands.WindowFunctions;
import org.safs.model.components.GenericObject;
import org.safs.model.tools.AbstractRunner;
import org.safs.model.tools.DefaultRunner;
//To keep backward compatibility, continue using SeleniumPlusException instead of SAFSException
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.map.AbstractSAFSAppMapService;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.FileUtilities.Access;
import org.safs.text.FileUtilities.DateType;
import org.safs.text.FileUtilities.FileAttribute;
import org.safs.text.FileUtilities.ImageFilterMode;
import org.safs.text.FileUtilities.Mode;
import org.safs.text.FileUtilities.PatternFilterMode;
import org.safs.text.FileUtilities.Placement;
import org.safs.text.GENStrings;
import org.safs.tools.MainClass;
import org.safs.tools.counters.CountStatusInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.JSAFSDriver;
import org.safs.tools.logs.UniqueStringLogLevelInfo;
import org.safs.tools.stringutils.StringUtilities;

/**
 * <pre>
 * This class will provide a general JAVA API to access the SAFS keywords.
 * <Font color="red">NOTE 1: Auto-evaluated expression.</Font>
 * SAFSPlus has the ability to process string as an expression.
 * For example, "36+9", "25*6" will be calculated as "45", "150" automatically; "^" is considered
 * as the leading char of a variable, hence "^var" will be considered as a variable "var",
 * if variable "var" exists, then "^var" will be replaced by its value, otherwise replaced by empty string "".
 * 
 * This ability is very useful for user, but sometimes it will cause UN-EXPECTED result during calling
 * SAFSPlus's API. For example, user wants to input a string "this is a combined-word" to an EditBox,
 * the EditBox will receive "this is a 0", which is not an expected result; user wants to select all
 * text of an EditBox, he uses "Ctrl+a" by calling SAFSPlus.TypeKeys("^a") and he finds that doesn't
 * work (the reason is that "^a" is parsed to "", because "^a" is considered as variable).
 * 
 * To avoid the problem caused by arithmetic char "+ - * /", we can call API Misc.Expressions(false) to
 * turn off the parse of an expression.
 * {@code
 * Misc.Expressions(false);
 * SAFSPlus.TypeChars("this is a combined-word");
 * ComboBox.CaptureItemsToFile(combobox, "ComboBoxData.txt", "UTF-8");
 * }
 * To avoid the problem caused by caret ^ or by arithmetic char "+ - * /", we can double-quote the parameter
 * {@code
 * SAFSPlus.TypeKeys("\"^p\""));
 * SAFSPlus.TypeKeys(quote("^p")));//quote is a static method provided by SAFSPlus
 * SAFSPlus.TypeChars(quote("this is a combined-word"));
 * }
 * 
 * <Font color="red">NOTE 2: File path deducing.</Font>
 * In SAFSPlus, there are some APIs like CaptureXXXToFile, VerifyXXXToFile, they require file-path as parameter.
 * As our doc is not very clear, user may confuse with the file-path parameter. Let's make it clear:
 *   There are 2 types of file, the test-file and bench-file. User can provide absolute or relative file-path for them.
 *   If it is absolute, there is not confusion. 
 *   If it is relative, we will combine it with a base-directory to form an absolute file. The base-directory depends
 *   on the type of file (test or bench):
 *     if it is test-file, the base-directory will be the test-directory, <ProjectDir>/Actuals/
 *     if it is bench-file, the base-directory will be the bench-directory, <ProjectDir>/Benchmarks/
 *     After the combination, the combined-file-name will be tested, if it is not valid, the project-directory will
 *     be used as base-directory.
 * 
 * 
 * <Font color="red">NOTE 3: <a href="http://safsdev.sourceforge.net/sqabasic2000/UsingDDVariables.htm">DDVariable</a></Font>
 * To use DDVariable ability, PLEASE remember to turn on the Expression by Misc.Expressions(true);
 * The DDVariable is a variable reference, it can be expressed by a leading symbol ^ and the "variable name".
 * For example:
 * ^user.name
 * ^user.password
 * 
 * DDVariable can be used along with an assignment or by itself, example as following:
 * {@code
 * Misc.Expressions(true);
 * //set value "UserA" to variable "user.name", set "Password1" to variable "user.password"
 * Misc.SetVariableValues("^user.name=UserA","^user.password=Password1");
 * //input the value of variable "user.name"
 * Component.InputCharacters(Map.AUT.UserInput, "^user.name");
 * //input the value of variable "user.password"
 * Component.InputCharacters(Map.AUT.PassWord, "^user.password");
 * }
 * 
 * <Font color="red">NOTE 4: a known issue about clicking on wrong item</Font>
 * Please to TURN OFF the browser's status bar.
 * 
 * </pre>
 * For more info on command-line options, see {@link #main(String[])}.
 * 
 * @author CANAGL
 */
public abstract class SAFSPlus {
	/**
	 * The Runner object providing access to the underlying Engines, like RFT, TestComplete, Selenium etc.
	 * This is the main object subclasses would use to execute SAFS actions and commands 
	 * and to gain references to more complex services like the running JSAFSDriver.
	 * Subclass may provide a different Runner than this one.
	 */
	protected static AbstractRunner Runner = new DefaultRunner();
	/**
	 * Keep the reference to the default Runner.
	 * 
	 * @see #resetRunner()
	 */
	protected static AbstractRunner DefaultRunner = Runner;
	
	/** "-autorun" <br>
	 * command-line argument to enable Dependency Injection, AutoConfig, and AutoExecution.<br>
	 * @see AbstractRunner#autorun(String[]) */
	public static final String ARG_AUTORUN = "-autorun";
	
	/** "-junit:" <br>
	 * command-line argument to invoke a JUnit class instead of a SAFSPlus subclass.<br>
	 * Example: -junit:com.sas.spock.tests.SpockExperiment
	 */
	public static final String ARG_JUNIT = "-junit:";
	
	/**
	 * "-autorunclass", the parameter to indicate the class name to run test automatically.<br>
	 * Only when parameter {@link #ARG_AUTORUN} is present, this parameter will take effect.<br>
	 * Example, "-autorunclass autorun.full.classname".<br>
	 */
	public static final String ARG_AUTORUN_CLASS = "-autorunclass";
	
	/** "-safsvar:"<br>
	 * command-line argument syntax: -safsvar:name=value */
	public static final String ARG_SAFSVAR = "-safsvar:";
	
	public static final String RELATIVE_TO_SCREEN = ComponentFunction.RELATIVE_TO_SCREEN;
	public static final String RELATIVE_TO_PARENT = ComponentFunction.RELATIVE_TO_PARENT;
	
	/** 
	 * Holds the TestRecordHelper object containing detailed information and results for 
	 * the last action or command executed.  Can be null if the last action or command executed 
	 * threw an Error or Exception bypassing its execution.
	 */
	public static TestRecordHelper prevResults = null;
	
	/**
	 * If true at runtime we will execute Runner.autorun() instead of runTest().
	 * @see SAFSPlus#runTest()
	 * @see AbstractRunner#autorun(String[])
	 */
	protected static boolean _autorun = false;
	/**
	 * If true at runtime means user has provided the "automatic run test class", we will not deduce it.
	 * @see SAFSPlus#runTest()
	 * @see AbstractRunner#autorun(String[])
	 * @see #autorun(String[])
	 */
	protected static boolean _autorunClassProvided = false;
	
	protected static boolean _isSPC = false;
	protected static String _junit = null; //classname(s) to execute from -junit: command-line option
	
	public SAFSPlus() {
		super();
		resetRunner();
	}
	public static AbstractRunner getRunner(){
		return Runner;
	}
	/**
	 * To get the SAFSPlus work correctly for the sub-class (Ex. SAFSPlus), 
	 * we MUST set its Runner to the sub-class's Runner (Ex. EmbeddedHookDriverRunner).
	 * @param Runner
	 */
	protected void setRunner(AbstractRunner Runner){
		SAFSPlus.Runner = Runner;
	}
	/**
	 * Reset the {@link #Runner} to {@link #DefaultRunner}.<br>
	 * @see #DefaultRunner
	 */
	private void resetRunner(){
		SAFSPlus.Runner = DefaultRunner;
	}
	
	/**
	 * INTERNAL USE ONLY.<p>
	 * Start writing tests in this required method in your subclass. 
	 * This must be an "instance" method because subclasses cannot Override static superclass methods. */
	public abstract void runTest() throws Throwable;
	
	/**
	 * Convenience routine to set the value of a SAFS Variable stored in SAFSVARS.<br>
	 * The act of logging success or failure will change prevResults.
	 * @param variableName -- Name of variable to set.
	 * @param variableValue -- value to store in variableName.
	 * @return true if successfully executed, false otherwise.<p>
	 * @see #prevResults
	 * @see Misc#SetVariableValues(String, String...)
	 * @see Misc#SetVariableValueEx(String, String)
	 */
	public static boolean SetVariableValue(String variableName, String variableValue){
		try{ 
			if(variableName == null) throw new SeleniumPlusException("SetVariableValue variableName cannot be null!");
			if(variableName.length()==0) throw new SeleniumPlusException("SetVariableValue variableName cannot be empty!");			
			if(variableValue == null) variableValue = "";
			Runner.jsafs().setVariable(variableName, variableValue);
			String msg = GENStrings.convert(GENStrings.SUCCESS_1, "SetVariableValue successful.", "SetVariableValue");
			String detail = GENStrings.convert(GENStrings.SOMETHING_SET,
					                        "'"+ variableName +"' set to '"+ variableValue +"'",
					                        variableName, variableValue);
			Logging.LogMessage(msg+" "+detail);
			return true;
		}
		catch(Throwable t){
			String msg = FAILStrings.convert(FAILStrings.NO_SUCCESS_3,
					"SetVariableValue "+ variableName +" was not successful using '"+ variableValue +"'",
					"SetVariableValue", variableName, variableValue);
			Logging.LogTestFailure(msg, t.getMessage());
			return false;
		}
	}
	
	/**
	 * Convenience routine to retrieve the value of a SAFS Variable stored in SAFSVARS.
	 * <br>This will exploit the <a href="http://safsdev.github.io/sqabasic2000/CreateAppMap.htm#ddv_lookup" target="_blank">SAFSMAPS look-thru</a> 
	 * and <a href="http://safsdev.github.io/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining" target="_blank">app map chaining</a> mechanism.  
	 * <br>That is, any variable that does NOT exist in SAFSVARS will be sought as an 
	 * ApplicationConstant in the SAFSMAPS service.
	 * <p>
	 * See <a href="http://safsdev.github.io/sqabasic2000/TestDesignGuidelines.htm" target="_blank">Test Design Guidelines for Localization</a>.
	 * @param variableName
	 * @return String value, or an empty String.  Null if an Exception or Error was encountered.<p>
	 * Does not change prevResults.
	 * @see #prevResults
	 */
	public static String GetVariableValue(String variableName){
		try{ 
			return Runner.jsafs().getVariable(variableName);
		}
		catch(Throwable t){
			// add failure info here
			return null;
		}
	}
		
	/**
	 * Abort running test flow.
	 * Prints a detailed abort message to the log and throws a RuntimeException to abort the test run.
	 * @param reason will be prepended to the detailed abort information.<p>  
	 * @example	 
	 * <pre>
	 * {@code
	 * AbortTest("reason for abort");	
	 * }
	 * </pre>	 
	 * Clears prevResults TestRecordHelper to null.
	 * @see #prevResults 
	 */
	public static void AbortTest(String reason) throws Throwable{
		prevResults = null;
		String thisMethod = "AbortTest";
		String getStackTraceMethod = "getStackTrace";
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		StackTraceElement cause = null;
		for(StackTraceElement item:trace){
			if(! item.getMethodName().equals(thisMethod) &&
			   ! item.getMethodName().equals(getStackTraceMethod)){
				cause = item;
				break;
			}
		}
		String detail = "Failure in "+ cause.getClassName()+" in Method "+ cause.getMethodName() +" at Line "+ cause.getLineNumber();
		Runner.logFAILED(reason, detail);
		throw new java.lang.RuntimeException(reason+" "+detail);
	}
	
	//======================================================  embedded_wrapper_classes_begin =================================================//
	
	/**
	 * A set of assertions methods for tests.  Only failed assertions are recorded.  
	 */
	public static class Assert{
		/** Protected Constructor for static class. */
		protected Assert(){}

		private static boolean _abortOnFailure = false;
		
		/** 
		 * @return String message localized text as:
		 * <p>
		 * content_not_matches   :the content of '%1%' does not match the content of '%2%'
		 */
		private static String getMatchFailureMessage(String command, String actual, String expected){
			return command +": "+ GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY, 
					"the content of '"+ actual +"' does not match the content of '"+ expected +"'.", 
					actual, expected);
		}
		
		/** 
		 * @return String message localized text as:
		 * <p>
		 * the content of '[actual]' matches the content of '[expected]'
		 */
		private static String getNotMatchFailureMessage(String command, String actual, String expected){
			return command +": "+ GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY, 
					"the content of '"+ actual +"' matches the content of '"+ expected +"'.", 
					actual, expected);
		}
		
		/** 
		 * Default = false. 
		 * Set to true to cause any Assert test failure to signal a test Abort.
		 */
		public static void setAbortOnFailure(boolean abortOnFailure){
			String status = abortOnFailure ? "ON":"OFF";
			debug("SAFSPlus ASSERT 'Abort On Failure' has been turned "+ status);
			_abortOnFailure = abortOnFailure;
		}
		
		/** 
		 * @return the current state of the Abort On Failure flag.
		 */
		public static boolean getAbortOnFailure(){
			return _abortOnFailure;
		}
		
		/** 
		 * @return String with failure localized failure text as:
		 * <p>
		 * [command] failure in table [caller] at line [caller linenumber].
		 */
		private static String getFailureDetails(String command){
			String test = null;
			String line = null;
			StackTraceElement calling = null;
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for(int i=2;i<stack.length;i++){
				calling = stack[i];
				//System.out.println("evaluating getCallerDetails calling class ["+ i +"]:"+ calling.getClassName());
				if(!calling.getClassName().equals(Assert.class.getName())) break;
			}
			test = calling.getClassName()+"#"+calling.getMethodName()+"()";
			line = String.valueOf(calling.getLineNumber());
			return FAILStrings.convert(FAILStrings.STANDARD_ERROR, 
					command+" failure in table '"+ test +"' at line "+ line +".",
					command, test, line);
		}
		
		static void incrementCounters(boolean wasSuccessful){
			long status = wasSuccessful ? CountersInterface.STATUS_TEST_PASS : CountersInterface.STATUS_TEST_FAILURE;
			Runner.driver().iDriver().getCountersInterface().incrementAllCounters(new UniqueStringCounterInfo("STEP", "STEP"), status);
		}
		
		/** 
		 * Assert two doubles or floats are equal using an absolute positive delta of 0.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Equals(double actual, double expected){
			return Equals(actual, expected, 0);
		}

		/** 
		 * Assert two doubles or floats are equal within an absolute positive delta. 
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails.
		 */
		public static boolean Equals(double actual, double expected, double delta){
			return Equals(actual, expected, delta, null);
		}

		/** 
		 * Assert two doubles or floats are equal within an absolute positive delta. 
		 * The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails.
		 */
		public static boolean Equals(double actual, double expected, double delta, String customMessage){
			double diff = Math.abs(expected - actual);
			boolean success = (expected == actual);
			if (!success) success = (diff <= delta);
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.Equals";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, String.valueOf(actual), String.valueOf(expected)), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}

		/** Assert two longs or ints are equal. 
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails.
		 */
		public static boolean Equals(long actual, long expected){
			return Equals(actual, expected, null);
		}

		/** 
		 * Assert two longs or ints are equal. 
		 * The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails.
		 */
		public static boolean Equals(long actual, long expected, String customMessage){
			boolean success = (expected == actual);
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.Equals";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, String.valueOf(actual), String.valueOf(expected)), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}

		/** Assert two Objects are equal. 
		 *  @return true if both Objects are null, or actual.equals(expected).
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Equals(Object actual, Object expected){
			return Equals(actual, expected, null);
		}

		/** Assert two Objects are equal.
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if both Objects are null, or actual.equals(expected).
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Equals(Object actual, Object expected, String customMessage){
			boolean success = (expected == null && actual == null);
			if(!success) try{ success=actual.equals(expected);}catch(Exception ignore){}
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.Equals";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, "ACTUAL Object", "EXPECTED Object"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}

		/** Assert two doubles or floats are NOT equal by an absolute positive delta of 0. 
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(double actual, double expected){
			return NotEqual(actual, expected, 0);
		}

		/** Assert two doubles or floats are NOT equal by an absolute positive delta.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(double actual, double expected, double delta){
			return NotEqual(actual, expected, delta, null);
		}

		/** 
		 * Assert two doubles or floats are NOT equal by an absolute positive delta. 
		 * The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(double actual, double expected, double delta, String customMessage){
			boolean success = Math.abs(expected - actual) > delta;
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.NotEqual";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getNotMatchFailureMessage(command, String.valueOf(actual), String.valueOf(expected)), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}

		/** Assert two longs or ints are NOT equal. 
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(long actual, long expected){
			return NotEqual(actual, expected, null);
		}

		/** 
		 * Assert two longs or ints are NOT equal. 
		 * The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(long actual, long expected, String customMessage){
			boolean success = (expected != actual);
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.NotEqual";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getNotMatchFailureMessage(command, String.valueOf(actual), String.valueOf(expected)), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}

		/** Assert two Objects are NOT equal. 
		 *  @return true if at least one of the Objects is not null, and actual.equals(expected) is false, 
		 *  or throws an Exception.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(Object actual, Object expected){
			return NotEqual(actual, expected, null);
		}

		/** Assert two Objects are NOT equal.
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if at least one of the Objects is not null, and actual.equals(expected) is false, 
		 *  or throws an Exception.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotEqual(Object actual, Object expected, String customMessage){
			boolean success = !(expected == null && actual == null);
			if(success) try{ success = !(actual.equals(expected));}catch(Exception x){ success = true;}
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.NotEqual";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getNotMatchFailureMessage(command, "ACTUAL Object", "EXPECTED Object"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}
	
		/** Assert an Objects is NOT null. 
		 *  @return true if the object is NOT null.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotNull(Object actual){
			return NotNull(actual, null);
		}

		/** Assert an Object is NOT null.
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if the object is NOT null.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotNull(Object actual, String customMessage){
			boolean success = !(actual == null);
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.NotNull";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getNotMatchFailureMessage(command, "null", "NotNull"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}
	
		/** Assert an Objects IS null. 
		 *  @return true if the object IS null.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Null(Object actual){
			return Null(actual, null);
		}

		/** Assert an Object IS null.
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if the object IS null.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Null(Object actual, String customMessage){
			boolean success = (actual == null);
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.Null";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, actual.getClass().getName(), "null"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}
	
		/** Assert an Object is the same object as the expected Object. 
		 *  @return true if both objects are null, or actual == expected.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Same(Object actual, Object expected){
			return Same(actual, expected, null);
		}

		/** Assert an Object is the same object as the expected Object. 
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if both objects are null, or actual == expected.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean Same(Object actual, Object expected, String customMessage){
			boolean success = (actual == null && expected == null);
			if(!success)try{ success = (actual == expected);}catch(Exception x){success = false;}
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.Same";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, "ACTUAL Object", "EXPECTED Object"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}
	
		/** 
		 * Assert an Object is NOT the same object as the expected Object. 
		 * @return true if at least one of the objects is not null, and actual != expected.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotSame(Object actual, Object expected){
			return NotSame(actual, expected, null);
		}

		/** Assert an Object is NOT the same object as the expected Object. 
		 *  The user can provide a default custom failure message instead of using the default.
		 *  @return true if at least one of the objects is not null, and actual != expected.
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean NotSame(Object actual, Object expected, String customMessage){
			boolean success = !(actual == null && expected == null);
			if(success)try{ success = !(actual == expected);}catch(Exception x){success = true;}
			Counters.IncrementCounts(success);
			if(!success){
				String command = "Assert.NotSame";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getNotMatchFailureMessage(command, "ACTUAL Object", "EXPECTED Object"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return success;
		}
	
		/** Assert a boolean is true.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean True(boolean actual){
			return True(actual, null);
		}

		/** Assert a boolean is true.
		 *  The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean True(boolean actual, String customMessage){
			Counters.IncrementCounts(actual);
			if(!actual){
				String command = "Assert.True";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, "false", "true"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return actual;
		}
	
		/** Assert a boolean is false.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean False(boolean actual){
			return False(actual, null);
		}

		/** Assert a boolean is false.
		 *  The user can provide a default custom failure message instead of using the default.
		 * @return true on success, false on failure
		 * @throws RuntimeException if abortOnFailure is true and the assertion fails. 
		 */
		public static boolean False(boolean actual, String customMessage){
			Counters.IncrementCounts(!actual);
			if(actual){
				String command = "Assert.False";
				if(customMessage != null) Logging.LogTestFailure(customMessage);
				else{
					Logging.LogTestFailure(getMatchFailureMessage(command, "true", "false"), 
							               getFailureDetails(command));
				}
				if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
			}
			return !actual;
		}
	
		/** Issue a generic test failure. 
		 * @throws RuntimeException if abortOnFailure is true. 
		 */
		public static void fail(){
			fail(null);
		}

		/** Issue a failure.
		 *  The user can provide a default custom failure message instead of using the default.
		 * @throws RuntimeException if abortOnFailure is true. 
		 */
		public static void fail(String customMessage){
			String command = "Assert.fail";
			if(customMessage != null) Logging.LogTestFailure(customMessage);
			else{
				Logging.LogTestFailure(getFailureDetails(command));
			}
			if(getAbortOnFailure()) throw new java.lang.RuntimeException(command+" 'Abort On Failure'!");
		}
	}

	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/DDDriverCommandsIndex.htm">Driver keywords</a>, like StartWebBrowser, UseWebBrowser, SetPosition etc.<br>
	 */
	public static class DriverCommand{

	    /***********  
        Copy the clipboard contents to a DDVariable.     
        This command can only copy text contents of the clipboard.
		@param varName  The name of the DDvariable variable to hold the clipboard text. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Misc.AssignClipboardVariable(VAR_CLIPBOARD); 
		* String contents = GetVariableValue(VAR_CLIPBOARD);
		* }
		* </pre>
		*/
		public static boolean AssignClipboardVariable(String varName){
			return command(DDDriverCommands.ASSIGNCLIPBOARDVARIABLE_KEYWORD, varName);
		}
		/**
		 * Invoke one or more JUnit tests using the provided Class name(s).
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverFlowCommandsReference.htm#detail_CallJUnit">Detailed Reference</a><p>
		 * @param clazzes String, the JUnit class names separated by semi-colon, colon, comma, or space.
		 * @return true if successfully executed with a successful result, false if the execution fails.<br>
		 *         The JUnit test result will be stored in "status info" of #prevResults, call {@link org.safs.TestRecordHelper#getStatusInfo()} to get it.<br>
		 * 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * 
		 * @example
		 * <pre>
		 * {@code
		 *  //Execute a JUnit Test "com.sas.spock.tests.SpockExperiment"
		 *  boolean success = Misc.CallJUnit("com.sas.spock.tests.SpockExperiment");
		 *  
		 *  //Execute 2 JUnit Tests "com.sas.spock.tests.SpockExperiment and myapp.tests.AnOtherTest"
		 *  boolean success = Misc.CallJUnit("com.sas.spock.tests.SpockExperiment;myapp.tests.AnOtherTest");
		 * 
		 *  //JUnitTestClasses is centrally stored in the App Map as "com.sas.spock.tests.SpockExperiment;myapp.tests.AnOtherTest"
		 *  boolean success = Misc.CallScript(Map.JUnitTestClasses()); 
		 * }
		 * </pre>
		 */
		public static boolean CallJUnit(String clazzes){
			return command(DDDriverFlowCommands.CALLJUNIT_KEYWORD, clazzes);
		}
		
	    /***********  
        Execute a SeBuilder JSON script in the currently running WebDriver.
		@param path -- full absolute path or project-relative path to an existing SeBuilder 
		JSON script to execute. 
        @return true if successfully executed with a successful result, 
        false if the script reports failure or an error occurred during execution.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		*  //checkbox.json path is centrally stored in the App Map.
		*  boolean success = Misc.CallScript(Map.CheckBoxJSONScript());
		*  
		*  //Literal String examples (not recommended):
		*  
		*  //checkbox.json is in a Scripts sub-directory for the Project.
		*  boolean success = Misc.CallScript("Scripts\checkbox.json");
		* 
		*  //checkbox.json is provided using a full absolute path.
		*  boolean success = Misc.CallScript("C:\Automation\SharedStorage\Selenium\checkbox.json"); 
		* }
		* </pre>
		*/
		public static boolean CallScript(String path){
			return command(DDDriverFlowCommands.CALLSCRIPT_KEYWORD, path);
		}
		
	    /*********** 
	    Execute a command on an external/local system or application.
        This command presently supports only the STAF protocol.
        Command syntax, parameters, and values will be dependent upon the protocol specified and 
        the command issued to the remote (or even local) system.
        @param protocol -- only "STAF" currently supported. Defaults to "STAF" if null.
        @param system -- the external/local system name or id to send the command to.
        @param service -- the service or process on the system targetted.
        @param command -- the command to be issued to the the service or process.
        @param resultVar -- the DDVariable name to receive the results of the call. 
		@param params -- additional parameters (String or String[]), if any, to be passed with the command. 
        <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsCallRemote.html">Detailed Reference</a><p>
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Misc.CallRemote("STAF", "local", "service", "list", STAF_RESULT); 
		* String resultStr = GetVariableValue(STAF_RESULT);
		* }
		* </pre>
		 */
		public static boolean CallRemote(String protocol, String system, String service, String command, String resultVar, String... params){
			String[] args = {protocol, system, service, command, resultVar};
			ArrayList<String> l = new ArrayList<String>();
			for(String arg: args){ l.add(arg);}
			for(String arg: params){ l.add(arg);}
			return command(DDDriverCommands.CALLREMOTE_KEYWORD, l.toArray(new String[0]));
		}
		
	    /***********  
        Enable and Disable enhanced expressions.
        When enabled, records are pre-processed for advanced expressions (math and string) conversions with DDVariables.
        When disabled, records are not pre-processed at all. 
		@param on boolean, true to enable the enhanced expressions; false to disable.
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* Misc.Expressions(true);
		* Logging.LogMessage("Expression On", "58+45");//Log result is 103
		* Misc.Expressions(false);//the evaluation of expression will be off until Expressions(true) is called.
		* Logging.LogMessage("Expression Off", "58+45");//Log result is '58+45'
		* }
		* </pre>
		* 
		* @example
		* <pre>
		* {@code
		* Misc.Expressions(true);
		* Logging.LogMessage("assign a variable var", "^var=58+45");//only when first letter is ^, the string between ^ and = will be consider as variable name
		* String value = SAFSPlus.GetVariableValue("var");
		* Logging.LogMessage("get value for variable var", value);
		* Logging.LogMessage("the value of variable var is", "^var");//only when first letter is ^, the string between ^ and = will be consider as variable name
		* }
		* </pre>
		* 
		* @example
		* <pre>
		* {@code
		* Component combobox = new Component(new Component("Window"), "ComboBox");
		* Misc.Expressions(true);
		* ComboBox.CaptureItemsToFile(combobox, URL+"ComboBoxData.txt", "UTF-8");//Fail to store as UTF8 file, "UTF-8" is processed as an arithmetic expression
		* Misc.Expressions(false);
		* ComboBox.CaptureItemsToFile(combobox, "ComboBoxData.txt", "UTF-8");//Save as UTF8 file
		* }
		* </pre>
		*/
		public static boolean Expressions(boolean on){
			return command(DDDriverCommands.EXPRESSIONS_KEYWORD, String.valueOf(on));
		}
		
	    /***********  
        Capture the location of the mouse pointer relative to the screen and save the x and y components into variables. 
		@param variableX String, The name of the DDVariable to store the X component of the mouse position.
		@param variableY String, The name of the DDVariable to store the Y component of the mouse position.
        @return Point2D.Double, the position of the Mouse.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* Point2D.Double mousePosition = Misc.CaptureMousePositionOnScreen("positionX", "positionY");
		* Logging.LogMessage("Mouse position: ", "["+mousePosition.x+","+mousePosition.y+"]");
		* }
		* </pre>
		*/
		public static Point2D.Double CaptureMousePositionOnScreen(String variableX, String variableY) throws SeleniumPlusException{
			try{
				String x = variableX;
				String y = variableY;
				
				if(x==null || x.isEmpty()) x = StringUtils.generateUniqueName("x");
				if(y==null || y.isEmpty()) y = StringUtils.generateUniqueName("y");
				
				if(command(DDDriverCommands.CAPTUREMOUSEPOSITIONONSCREEN_KEYWORD, x, y)){
					return new Point2D.Double(Double.parseDouble(GetVariableValue(x)), Double.parseDouble(GetVariableValue(y)));
				}
			}catch(Throwable th){
				IndependantLog.error(StringUtils.debugmsg(Misc.class, "CaptureMousePositionOnScreen"), th);
			}
			throw new SeleniumPlusException(DDDriverCommands.CAPTUREMOUSEPOSITIONONSCREEN_KEYWORD+" failed.");
		}
		
	    /***********  
        Clear storage of all DDVariables. 
        @return  true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		*/
		public static boolean ClearAllVariables(){
			return command(DDDriverCommands.CLEARALLVARIABLES_KEYWORD);
		}
		
		/***********  
        Clear the internal application map cache.
        <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_ClearAppMapCache">Detailed Reference</a><p>
        @return  true if successful, false otherwise.<p>
		* @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		*/
		public static boolean ClearAppMapCache(){
			return command(DDDriverCommands.CLEARAPPMAPCACHE_KEYWORD);
		}
		
		/***********  
        Clear storage of all SAFS variables containing a specific prefix.
		<p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_ClearArrayVariables">Detailed Reference</a><p>
		@param prefix String, The variable name prefix to use for the delete.
        @return  true if successful, false otherwise.<p>
		* @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Misc.ClearArrayVariables("TCAFS.UID.");//All variables beginning with "TCAFS.UID." will be deleted.
		* }
		* </pre>
		*/
		public static boolean ClearArrayVariables(String prefix){
			return command(DDDriverCommands.CLEARARRAYVARIABLES_KEYWORD, prefix);
		}
		
		/***********  
        Clear the contents of the Window's clipboard.
        @return  true if successful, false otherwise.<p>
		* @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		*/
		public static boolean ClearClipboard(){
			return command(DDDriverCommands.CLEARCLIPBOARD_KEYWORD);
		}
		
	    /*********** 
	    Close a named application process launched with LaunchApplication.
        <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_CloseApplication">Detailed Reference</a><p>
        @param ApplicationID String, A text ID to reference the application to close.
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Misc.CloseApplication("NotePad");
		* }
		* </pre>
		*/
		public static boolean CloseApplication(String ApplicationID){
			return command(DDDriverCommands.CLOSEAPPLICATION_KEYWORD, ApplicationID);
		}
		
		/**
		 * By map ID, Close a opened Application Map in <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a>.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_CloseApplicationMap">Detailed Reference</a><p>
		 * @param mapID String, the ID of the map to be closed. The ID is usually the map file name.<br>
		 *                      It is the same ID that you provide when calling {@link Misc#SetApplicationMap(String)}.<br>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.CloseApplicationMap("App.map");
		 * }
		 * </pre>
		 */
		public static boolean CloseApplicationMap(String mapID)  throws SeleniumPlusException{
			if(mapID==null){
				throw new SeleniumPlusException("SetApplicationMap mapID cannot be null!");
			}
			if(command(DDDriverCommands.CLOSEAPPLICATIONMAP_KEYWORD, mapID)){
				String defaultMapID2 = Runner.jsafs().getMapsInterface().getDefaultMap().getUniqueID().toString();
				IndependantLog.debug("Current default map ID = "+defaultMapID2);
				return true;
			}else{
				return false;
			}
		}
		
		/**
		 * Copy the value of a (dynamic) DDVariable to another.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_CopyVariableValueEx">Detailed Reference</a><p>
		 * @param sourceVar String, The name of the variable to copy.
		 * @param destVar String, The name of the variable to receive the copy.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "myvar";
		 * String dest = "mydest";
		 * String value = "myvar_value";
		 * if(SetVariableValue(var, value) && Misc.CopyVariableValueEx(var, dest))
		 *   if(!GetVariableValue(dest).equals(value)) System.err.println("Error copy value.");
		 * }
		 * </pre>
		 */
		public static boolean CopyVariableValueEx(String sourceVar, String destVar){
			return command(DDDriverCommands.COPYVARIABLEVALUEEX_KEYWORD, sourceVar, destVar);
		}
		/**
		 * Pause test-case flow in milliseconds. If you want to pause in seconds, use {@link Pause(int)}.
		 * @param milliseconds int, the milliseconds to pause
		 * @return true if successful, false otherwise.<p>
		 * @see #Pause(int)
		 */
		public static boolean Delay(int milliseconds){
			return command(DDDriverCommands.DELAY_KEYWORD, String.valueOf(milliseconds));
		}
		
		/*********** 
	    Identify and Launch a specified application
        <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_LaunchApplication">Detailed Reference</a><p>
        @param ApplicationID String, A text ID to reference the application 
        @param ExecutablePath String, The path, filename, and parameters for the executable OR an ApplicationConstant.
	    @param params -- <br>
	    params[0] -- WorkDir, A Working Directory for the application<br>
	    params[1] -- CMDLineParam, Separate command line parameters for the application<br>
	    params[2] -- CMDLineParam, Filename to the Application Map for the application<br>
        @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * boolean success = Misc.LaunchApplication("NotePad", "notepad.exe c:\\sample.txt");
		 * }
		 * </pre>
		 */
		public static boolean LaunchApplication(String ApplicationID, String ExecutablePath, String ... params){
			return command(DDDriverCommands.LAUNCHAPPLICATION_KEYWORD, combineParams(params, ApplicationID, ExecutablePath));
		}
		/**
		 * Assign the value of the specified Registry Key to a variable.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_GetRegistryKeyValue">Detailed Reference</a><p>
		 * @param key String, The registry Key to seek
		 * @param keyValue String, The value name under the parent key to seek
		 * @param result String, The name of the variable to receive the value of the registry key.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment";
		 * String keyValue = "CurrentVersion";
		 * String result = "result";
		 * if(Misc.GetRegistryKeyValue(key, keyValue, result))
		 *   System.out.println(key+":"+keyValue+" = "+GetVariableValue(result));
		 * }
		 * </pre>
		 */
		public static boolean GetRegistryKeyValue(String key, String keyValue, String result){
			return command(DDDriverCommands.GETREGISTRYKEYVALUE_KEYWORD, key, keyValue, result);
		}
		/**
		 * Send a "GET" HTTP Request by AJAX, and save the response to a variable.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_GetURL">Detailed Reference</a><p>
		 * @param url String, The URL to request.
		 * @param variable String, The name of the variable holding the content of response<br>
		 * 		  Some suffixes can be appended to the variable name to form new variable holding other values, suffixes list as below:<br>
		 *   <ul>
		 *     <li>.readyState contains 'AJAX Ready State Code', such as "0", "1", "2", "3" and "4".</li>
		 *     <li>.headers contains the response headers</li>
		 *     <li>.status contains the HTTP status code, such as "200", "404" etc.</li>
		 *     <li>.statusText contains the HTTP status text, such as "OK", "Not Found" etc.</li>
		 *     <li>.xml contains the XML response if it exists.</li>
		 *   </ul>
		 * @param optionals
		 * <ul>
		 * <li><b>optionals[0] timeout</b> int, "Timeout" in seconds. The default is 120 seconds.
		 * <li><b>optionals[1] headerName</b> String, the name of the request header
		 * <li><b>optionals[2] headerValue</b> String, the value for the request header
		 * </ul>
		 * <b>optionals</b> parameters '<b>headerName</b>' and '<b>headerValue</b>' must appear in pair, and they can present more than 1 time, 
		 * which means multiple pair of ('headerName', 'headerValue') can be provided. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String url = "http://rest.api.url";
		 * String variable = "response";
		 * //Get content of url and wait for response with timeout 60 seconds, save result to variable.
		 * if(Misc.GetURL(url, variable, "60")){
		 *   System.out.println("content of url '"+url+"' has been saved to variable '"+variable+"'.");
		 *   System.out.println("content: \n"+GetVariableValue(variable));
		 *   System.out.println("ready state: "+GetVariableValue(variable+".readyState"));
		 *   System.out.println("status code: "+GetVariableValue(variable+".status"));
		 *   System.out.println("status text: "+GetVariableValue(variable+".statusText"));
		 *   System.out.println("response headers: \n"+GetVariableValue(variable+".headers"));
		 *   System.out.println("XML content: "+GetVariableValue(variable+".xml"));
		 * }
		 * //Get content of url with headers ("Accept", "text/*") and ("Accept-Charset", "UTF-8") 
		 * //and wait for response with timeout 60 seconds, save result to variable.
		 * if(Misc.GetURL(url, variable, "60", "Accept", "text/*", "Accept-Charset", "UTF-8" )){
		 *   System.out.println("content of url '"+url+"' has been saved to variable '"+variable+"'.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean GetURL(String url, String variable, String...optionals){
			return command(DDDriverCommands.GETURL_KEYWORD, combineParams(optionals, url, variable));
		}
		/**
		 * Get the value of the specified Registry Key to a variable.
		 * @param key String, The registry Key to seek
		 * @param keyValue String, The value name under the parent key to seek
		 * @return String, the registry key's value. null if not found or execution fails.
		 * 
		 * @see #prevResults
		 * @see #GetRegistryKeyValue(String, String, String)
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment";
		 * String keyValue = "CurrentVersion";
		 * String result = Misc.GetRegistryKeyValue(key, keyValue);
		 * }
		 * </pre>
		 */
		public static String GetRegistryKeyValue(String key, String keyValue){
			String result = "GetRegistryKeyValue_VAR";
			if(GetRegistryKeyValue(key, keyValue, result)){
				return GetVariableValue(result);
			}else{
				return null;
			}
		}
		/**
		 * Get the string value of the system date in the format MM-DD-YYYY
		 * @return String, the string value of the system date. null if execution fails.
		 * 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * System.out.println("SystemDate "+Misc.GetSystemDate());
		 * }
		 * </pre>
		 */
		public static String GetSystemDate(){
			String result = "GetSystemDate_VAR";
			if(command(DDDriverCommands.GETSYSTEMDATE_KEYWORD, result)){
				return GetVariableValue(result);				
			}else{
				return null;
			}
		}
		/**
		 * Get the string value of the system date-time in the format MM-DD-YYYY HH:MM:SS
		 * @param military boolean, if true the hour string will be 24-hours format; otherwise is 12 AM/PM.
		 * @return String, the string value of the system date-time. null if execution fails.
		 * 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * System.out.println("SystemDateTime "+Misc.GetSystemDateTime(true));
		 * System.out.println("SystemDateTime "+Misc.GetSystemDateTime(false));
		 * }
		 * </pre>
		 */
		public static String GetSystemDateTime(boolean military){
			String result = "GetSystemDate_VAR";
			if(command(DDDriverCommands.GETSYSTEMDATETIME_KEYWORD, result, String.valueOf(military))){
				return GetVariableValue(result);				
			}else{
				return null;
			}
		}
		/**
		 * Get the string value of the system time in the format HH:MM:SS
		 * @param military boolean, if true the hour string will be 24-hours format; otherwise is 12 AM/PM.
		 * @return String, the string value of the system time. null if execution fails.
		 * 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * System.out.println("SystemTime "+Misc.GetSystemTime(true));
		 * System.out.println("SystemTime "+Misc.GetSystemTime(false));
		 * }
		 * </pre>
		 */
		public static String GetSystemTime(boolean military){
			String result = "GetSystemDate_VAR";
			if(command(DDDriverCommands.GETSYSTEMTIME_KEYWORD, result, String.valueOf(military))){
				return GetVariableValue(result);				
			}else{
				return null;
			}
		}
		/**
		 * Highlight object
		 * @param OnOff -- true or false for object highlight 
		 * @return true on success
		 */
		public static boolean Highlight(boolean OnOff){
			return command(DDDriverCommands.HIGHLIGHT_KEYWORD, String.valueOf(OnOff));		
		}
		
		/**
		 * Pause test-case flow in seconds. If you want to pause in millisecond, use {@link Misc#Delay(int)}.
		 * @param seconds int, the seconds to pause
		 * @return true if successfully executed, false otherwise.<p>
		 * @example	 
		 * <pre>
		 * {@code
		 * Pause(20);	
		 * }
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 * @see Misc#Delay(int)
		 */
		public static boolean Pause(int seconds){
			return command(DDDriverCommands.PAUSE_KEYWORD, String.valueOf(seconds));
		}
		/**
		 * Set the general pause time between records (SAFSPlus API calls), in milliseconds.
		 * @param milliseconds int, pause time between records, in milliseconds.
		 * @return boolean if successful, false otherwise
		 */
		public static boolean SetMillisBetweenRecords(int milliseconds){
			return command(DDDriverCommands.SETMILLISBETWEENRECORDS_KEYWORD, String.valueOf(milliseconds));
		}
		/**
		 * Start WebBrowser
		 * See <a href="http://safsdev.github.io/sqabasic2000/DDDriverCommandsReference.htm#detail_StartWebBrowser">Detailed Reference</a>	
		 * @param URL String,
		 * @param BrowserID String, Unique application/browser ID.
		 * @param params optional, currently <b>ONLY supported for Selenium WebDriver</b> Engine.
		 * <p>
		 * <ul>
		 * <b>params[0] browser name</b> String, (default is {@link BrowserConstants#DEFAULT_BROWSER}), it can be one of:
		 * <p>
		 * 		<ul>
		 *          <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 *          <li>{@link BrowserConstants#BROWSER_NAME_FIREFOX}
		 *          <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * 		</ul>
		 * <p>
		 * <b>params[1] timeout</b> int, in seconds. Implicit timeout for search elements<br>	
		 * <b>params[2] isRemote</b> boolean, (no longer used -- everything is now "remote")<br>
		 * <br>
		 * Following parameters indicate the <b>extra parameters</b>, they <b>MUST</b> be given by <b>PAIR(key, value)</b>
		 * <p>
		 * The key can be one of:
		 * <p><ul>
		 * {@link BrowserConstants#getExtraParameterKeys()}<br>
		 * </ul><p>
		 * params[3] extra parameter key1<br>
		 * params[4] extra parameter value for key1<br>
		 * <br>
		 * params[5] extra parameter key2<br>
		 * params[6] extra parameter value for key2<br>
		 * <br>
		 * params[7] extra parameter key3<br>
		 * params[8] extra parameter value for key3<br>
		 * ...
		 * </ul>
		 * @return true on success
		 * @example
		 * <pre>
		 * StartWebBrowser("http://www.google.com", "GoogleMain");
		 * StartWebBrowser("http://www.google.com", "GoogleMain", BrowserConstans.BROWSER_NAME_CHROME);
		 * StartWebBrowser("http://www.google.com", "GoogleMain", BrowserConstans.BROWSER_NAME_IE, "10");
		 * 
		 * <b>
		 * The following gives some examples to start web browser with "custom profile" and "preferences".
		 * For the detail explanation of starting browser with "custom profile" and/or "preferences", please visit the section "<font color="red">Start Browser</font>" at <a href="http://safsdev.github.io/selenium/doc/SAFSPlus-Welcome.html">Selenium Welcome Document</a>. 
		 * </b>
		 * 
		 * //Start firefox browser with custom profile "myprofile" ( <a href="https://support.mozilla.org/en-US/kb/profile-manager-create-and-remove-firefox-profiles">Create custom profile</a>)
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_FIREFOX, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        BrowserConstans.KEY_FIREFOX_PROFILE, 
		 *                                                        "myprofile"
		 *                                                        });
		 *  
		 * //Start firefox browser with some preference to set. 
		 * String absolutePreferenceFile = "c:\\firefoxPref.json.dat";//A json file containing chrome preferences, like { "intl.accept_languages":"zh-cn", "accessibility.accesskeycausesactivation":false, "browser.download.folderList":2 }
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_FIREFOX, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_FIREFOX_PROFILE_PREFERENCE), 
		 *                                                        quote(absolutePreferenceFile)
		 *                                                        });
		 *                                                        
		 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the last-used user. 
		 * String datapool = "C:\\Users\\some-user\\AppData\\Local\\Google\\Chrome\\User Data";
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_USER_DATA_DIR), 
		 *                                                        datapool
		 *                                                        });
		 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the default user. 
		 * String datapool = "C:\\Users\\some-user\\AppData\\Local\\Google\\Chrome\\User Data";
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_USER_DATA_DIR), 
		 *                                                        datapool,
		 *                                                        quote(BrowserConstans.KEY_CHROME_PROFILE_DIR),
		 *                                                        "Default"
		 *                                                        });
		 *                                                        
		 * //Start chrome browser with custom data, and using the last-used user.                                                      
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_USER_DATA_DIR), 
		 *                                                        "c:\\chrome_custom_data"//<a href="http://www.chromium.org/developers/creating-and-using-profiles">Create custom data pool</a>
		 *                                                        });
		 * //Start chrome browser with custom data, and using the 1th user.                                                       
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_USER_DATA_DIR), 
		 *                                                        "c:\\chrome_custom_data",//<a href="http://www.chromium.org/developers/creating-and-using-profiles">Create custom data pool</a>
		 *                                                        quote(BrowserConstans.KEY_CHROME_PROFILE_DIR),
		 *                                                        "Profile 1"
		 *                                                        });
		 * //Start chrome browser with some options to be turned off. 
		 * String optionsToExclude = "disable-component-update";//comma separated options to exclude, like "disable-component-update, ignore-certificate-errors", be careful, there are NO 2 hyphens before options.                                                      
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_EXCLUDE_OPTIONS), 
		 *                                                        quote(optionsToExclude)
		 *                                                        });
		 * //Start chrome browser with some chrome-command-line-options/preferences to set. 
		 * String absolutePreferenceFile = "c:\\chromePref.json.dat";//A json file containing chrome command-line-options/preferences, like { "lang":"zh-cn", "start-maximized":"",  "<b>seplus.chrome.preference.json.key</b>":{ "intl.accept_languages":"zh-CN-pseudo", "intl.charset_default"  :"utf-8"} }
		 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
		 *                                                        BrowserConstans.BROWSER_NAME_CHROME, 
		 *                                                        "10", 
		 *                                                        "true", 
		 *                                                        quote(BrowserConstans.KEY_CHROME_PREFERENCE), 
		 *                                                        quote(absolutePreferenceFile)
		 *                                                        });
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 * @see #StopWebBrowser(String)
		 * @see #SwitchWebBrowser(String)
		 */
		public static boolean StartWebBrowser(String URL,String BrowserID, String... params){
			try{
				String[] allParams = combineParams(params, URL, BrowserID);
				return command(DDDriverCommands.STARTWEBBROWSER_KEYWORD, allParams);
			}catch(Throwable t){
				prevResults = null;
				return false;
			}
		}
		
		/**
		 * Stop WebBrowser by ID.
		 * During test, multiple browsers can be opened by {@link #StartWebBrowser(String, BrowserID, String...)}<br>
		 * If user wants to stop one of these opened browser, use can call this method.<br>
		 * This method requires a parameter 'ID', which is given by user when he calls {@link #StartWebBrowser(String, BrowserID, String...)}<br>
		 * @param BrowserID String, the BrowserID served as key to get the WebDriver from cache.<br>
		 * @return - true on success<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * String browserID = "GoogleMain";
		 * StartWebBrowser("http://www.google.com", browserID);
		 * //do some testing, then
		 * StopWebBrowser(browserID);	  
		 * }
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 * @see #StartWebBrowser(String, String, String...)
		 */
		public static boolean StopWebBrowser(String BrowserID){
			return command(DDDriverCommands.STOPWEBBROWSER_KEYWORD, BrowserID);
		}
		
		/**
		 * Send email via a mail server, which should be configured by user in the .INI file.<br>
		 * This method will not verify the validation of the recipients address.<br>
		 * 
		 * @param from - Sender's email address.
		 * @param to - Receiver's email. Multiple receivers allowed by semicolon(";");
		 * @param subject - Subject line of the email.
		 * @param message - Email message, html default format. 
		 * @param optionals - Attachment file. Multiple files allowed by semicolon(";").
		 * @return boolean - True on success.
		 * @example
		 * <pre>
		 * {@code
		 * 
		 * Prerequisite:
		 * in test.ini setup following
		 *   [SAFS_DRIVERCOMMANDS]
		 *   OUT_MAILSERVER="mail server"
		 *   OUT_MAILSERVERPORT=25|465|587
		 *   OUT_MAILSERVERPROTOCOL=SMTP|SMTPS|TLS
		 *   OUT_MAILUSER=user.name@mail.com
		 *   OUT_MAILPASS=*******
		 *   
		 *   [SAFS_DRIVER]
		 *   SMTP="mail server" (deprecated, replaced by OUT_MAILSERVER)
		 *   PORT=25            (deprecated, replaced by OUT_MAILSERVERPORT)
		 * 
		 * Misc.SendMail("Sender@email.com", "Recipient1@email.com;@Recipient2@email.com", "Subject line", "Email message", 
		 * "Attachment 1 filename; Attachment 2 filename");
		 *  
		 * CountStatusInterface info = Counters.GetCounterStatus("TestCase1") or Counters.StartCounter("test1")
    	 * String message = "Total Records: " + info.getTotalRecords() + "\nTotal Pass: " + 
    	 * info.getTestPasses() +"\nTotal Fail: " + info.getTestFailures();
		 * Misc.SendMail("sender@email.com","to1@email.com", "Test results", message,"Logs\\TestCase1.txt");
		 * }
		 * </pre>
		 */
		public static boolean SendMail(String from, String to, String subject, String message, String... attachment){
			return command(DDDriverCommands.SENDEMAIL_KEYWORD, combineParams(attachment, from, to, subject, message));
		}
		
		/**
		 * Send a "GET" HTTP Request by AJAX, and save the response to a file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SaveURLToFile">Detailed Reference</a><p>
		 * @param url String, The URL to request.
		 * @param file String, The file to save the response from the server.
		 *                     This can be a full path, a relative path, or a file name. 
		 *                     For relative path, it is appended to the project's path to build the full path of the file. 
		 *                     For file name, the file is saved under the project's test directory (it is Datapool\Test in SAFS or Actuals in SE+). 
		 *                     In any case the parent folder of the file must exist. <br>
		 * @param optionals
		 * <ul>
		 * <li><b>optionals[0] timeout</b> int, "Timeout" in seconds. The default is 120 seconds.
		 * <li><b>optionals[1] headerName</b> String, the name of the request header
		 * <li><b>optionals[2] headerValue</b> String, the value for the request header
		 * </ul>
		 * <b>optionals</b> parameters '<b>headerName</b>' and '<b>headerValue</b>' must appear in pair, and they can present more than 1 time, 
		 * which means multiple pair of ('headerName', 'headerValue') can be provided. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String url = "http://rest.api.url";
		 * String file = "response.html";
		 * //Get content of url and wait for response with default timeout 120 seconds, save result to file.
		 * if(Misc.SaveURLToFile(url, file)){
		 *   System.out.println("content of url '"+url+"' has been saved to file '"+file+"'.");
		 * }
		 * //Get content of url with headers ("Accept", "text/*") and ("Accept-Charset", "UTF-8") 
		 * //and wait for response with timeout 60 seconds, save result to file.
		 * if(Misc.SaveURLToFile(url, variable, "60", "Accept", "text/*", "Accept-Charset", "UTF-8" )){
		 *   System.out.println("content of url '"+url+"' has been saved to file '"+file+"'.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean SaveURLToFile(String url, String file, String...optionals){
			return command(DDDriverCommands.SAVEURLTOFILE_KEYWORD, combineParams(optionals, url, file));
		}
		/**
		 * Load/Set/Change an Application Map and put it on top of <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a>.<br>
		 * No action or command relying on the contents of a particular Application Map will work properly if<br>
		 * the Map has not been previously loaded by this command or similar test initialization processes--<br>
		 * like an AppMap.order initialization file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetApplicationMap">Detailed Reference</a><p>
		 * @param mapID String, the ID of the map to open.<br>
		 *                      It is normally the map file name under the project datapool directory (the "Maps" folder).<br>
		 *                      It can also be the map file name under the project directory.<br>
		 *                      It can also be the absolute map file name under whatever directory.<br>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetApplicationMap("App.map");
		 * Misc.SetApplicationMap("d:\\mytest\\App.map");
		 * }
		 * </pre>
		 */
		public static boolean SetApplicationMap(String mapID) throws SeleniumPlusException{
			if(mapID==null){
				throw new SeleniumPlusException("SetApplicationMap mapID cannot be null!");
			}
			if(command(DDDriverCommands.SETAPPLICATIONMAP_KEYWORD, mapID)){
				String defaultMapID2 = Runner.jsafs().getMapsInterface().getDefaultMap().getUniqueID().toString();
				IndependantLog.debug("Current default map ID = "+defaultMapID2);
				return true;
			}else{
				return false;
			}
		}
		/**
		 * Save the clipboard contents to a text file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SaveClipboardToFile">Detailed Reference</a><p>
		 * @param file String, The file name to store the clipboard's content
		 * <pre>
		 *      Absolute file path.
		 *      Relative file path, 
		 *        if it contains file separator, it is relative to datapool or project root.
		 *        otherwise it is relative to the project test folder.
		 * </pre>
		 * @param optionals --<ul>
		 * optionals[0] String, "Encoding" used to write a file.  The default is "UTF-8".<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SaveClipboardToFile(D:\test\clipboard.txt);
		 * Misc.SaveClipboardToFile(clipboard.txt);//Save to file under project test folder
		 * }
		 * </pre>
		 */
		public static boolean SaveClipboardToFile(String file, String...optionals){
			return command(DDDriverCommands.SAVECLIPBOARDTOFILE_KEYWORD, combineParams(optionals, file));
		}
		
		/**
		 * Move mouse wheel forward or backward. The 'mouse wheel scroll' will happen on the focused object, users<br>
		 * needs to click the the object on which he wished the 'mouse wheel scroll' happens.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_ScrollWheel">Detailed Reference</a><p>
		 * @param wheelAmount int, The amount of wheel to scroll
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.ScrollWheel(25);
		 * Misc.ScrollWheel(-15);
		 * }
		 * </pre>
		 */
		public static boolean ScrollWheel(int wheelAmount){
			return command(DDDriverCommands.SCROLLWHEEL_KEYWORD, String.valueOf(wheelAmount));
		}
		
		//TODO Allow user to modify the directory, maybe too dangerous.
		//SetBenchDirectory
		//SetDifDirectory
		//SetProjectDirectory
		//SetRootVerifyDirectory
		//SetTestDirectory
		
		/**
		 * Set a string content to the clipboard.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetClipboard">Detailed Reference</a><p>
		 * @param content String, The content to set to clipboard.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String clipboardValue = "THIS IS SOMETHING TO BE SET TO CLIPBOARD!";
		 * Misc.SetClipboard(clipboardValue);
		 * }
		 * </pre>
		 */
		public static boolean SetClipboard(String content){
			return command(DDDriverCommands.SETCLIPBOARD_KEYWORD, content);
		}
		/**
		 * Give a Window or Component input focus.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetContext">Detailed Reference</a><p>
		 * @param component org.safs.model.Component, The component to gain focus.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetContext(Map.SAPDemoPage.SAPDemoPage);
		 * Misc.SetContext(Map.SAPDemoPage.Basc_Password);
		 * }
		 * </pre>
		 */
		//TODO Not exposed yet, does not work, browser cannot be in front.
		static boolean SetContext(org.safs.model.Component component) throws SeleniumPlusException{
			if(component==null) throw new SeleniumPlusException("Componet is null");
			String winName = component.getParentName()==null?component.getName():component.getParentName();
			String comName = component.getName();
			return command(DDDriverCommands.SETCONTEXT_KEYWORD, winName, comName);
		}
		/**
		 * Give a Window or Component input focus.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetFocus">Detailed Reference</a><p>
		 * @param component org.safs.model.Component, The component to gain focus.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetFocus(Map.SAPDemoPage.SAPDemoPage);
		 * Misc.SetFocus(Map.SAPDemoPage.Basc_TextArea);
		 * }
		 * </pre>
		 */
		//TODO Not exposed yet, does not work, browser cannot be in front.
		static boolean SetFocus(org.safs.model.Component component) throws SeleniumPlusException{
			if(component==null) throw new SeleniumPlusException("Componet is null");
			String winName = component.getParentName()==null?component.getName():component.getParentName();
			String comName = component.getName();
			return command(DDDriverCommands.SETFOCUS_KEYWORD, winName, comName);
		}
		/**
		 * Enable/Disable verbose debug log during using <a href="http://safsdev.sourceforge.net/sqabasic2000/SAFSImageBasedRecognition.htm">IBT</a>.<br>
		 * Normally, it is off. It is ONLY turned on when developer needs more information.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetImageDebug">Detailed Reference</a><p>
		 * @param on boolean, true to turn on; false to turn off.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetImageDebug(true);
		 * }
		 * </pre>
		 */
		public static boolean SetImageDebug(boolean on){
			return command(DDDriverCommands.SETIMAGEDEBUG_KEYWORD, String.valueOf(on));
		}
		/**
		 * Enable/Disable <a href="http://safsdev.sourceforge.net/sqabasic2000/SAFSImageBasedRecognition.htm">IBT</a> Fuzzy Matching.<br>
		 * Normally, it is off.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetImageFuzzyMatching">Detailed Reference</a><p>
		 * @param on boolean, true to turn on; false to turn off.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetImageFuzzyMatching(true);
		 * }
		 * </pre>
		 */
		public static boolean SetImageFuzzyMatching(boolean on){
			return command(DDDriverCommands.SETIMAGEFUZZYMATCHING_KEYWORD, String.valueOf(on));
		}
		/**
		 * Enable/Disable <a href="http://safsdev.sourceforge.net/sqabasic2000/SAFSImageBasedRecognition.htm">IBT</a> MultiThread Matching.<br>
		 * Normally, it is off.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetMultipleThreadSearch">Detailed Reference</a><p>
		 * @param on boolean, true to turn on; false to turn off.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.SetMultipleThreadSearch(true);
		 * }
		 * </pre>
		 */
		public static boolean SetMultipleThreadSearch(boolean on){
			return command(DDDriverCommands.SETMULTIPLETHREADSEARCH_KEYWORD, String.valueOf(on));
		}
		/**
		 * Set the value of a variable/DDVariable; for DDVariable, refer to {@link SAFSPlus} Note3.<br>
		 * The different between this method and SAFSPlus#SetVariableValue(String, String) is that this method<br>
		 * can take a variable or DDVariable as the first parameter, the later one can only take variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetVariableValueEx">Detailed Reference</a><p>
		 * @param variable String, The name of variable or DDVariable that evaluates to a valid variable name.<br>
		 * @param value String, The content to set to variable.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * @see SAFSPlus#SetVariableValue(String, String)
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.Expressions(true);
		 * String user_name_var = "user.name";//variable
		 * String user_name = "NewUserName";//value
		 * //Create a reference "ref_user_name_var" to variable "user.name"
		 * String refToUsernameVar = "ref_user_name_var";//reference to variable "user.name"
		 * Misc.SetVariableValueEx(refToUsernameVar, user_name_var);
		 * //Use the reference to set value to variable "user.name"
		 * Misc.SetVariableValueEx("^"+refToUsernameVar, user_name);
		 * if(!username.equals(GetVariableValue(user_name_var))) System.err.println("Fail SetVariableValues");
		 * }
		 * </pre>
		 */
		public static boolean SetVariableValueEx(String variable, String value){
			String action = DDDriverCommands.SETVARIABLEVALUEEX_KEYWORD;
			
			try{ 
				if(variable == null) throw new SeleniumPlusException("parameter 'variable' cannot be null!");
				return command(action, _resolveDDVariables(variable)[0], value);
			}
			catch(Throwable t){
				String msg = FAILStrings.convert(FAILStrings.NO_SUCCESS_3,
						action+" '"+variable+"' was not successful using '"+ value +"'",
						action, variable, value);
				Logging.LogTestFailure(msg, t.getMessage());
				return false;
			}
			
		}
		/**
		 * Set the value of one or more DDVariables; for DDVariable, refer to {@link SAFSPlus} Note3.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_SetVariableValues">Detailed Reference</a><p>
		 * @param varEqualVal String, The DDVariable along with its value.
		 * @param optionals
		 * <ul>
		 * optionals[0] String, "varEqualVal1".<br>
		 * optionals[1] String, "varEqualVal2".<br>
		 * optionals[2] String, "varEqualVal3".<br>
		 * ...<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * @see SAFSPlus#SetVariableValue(String, String)
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.Expressions(true);
		 * //set value "UserA" to variable "user.name", set "Password1" to "user.password"
		 * Misc.SetVariableValues("^user.name=UserA","^user.password=Password1");
		 * //input the value of variable "user.name"
		 * Component.InputCharacters(Map.AUT.UserInput, "^user.name");
		 * //input the value of variable "user.password"
		 * Component.InputCharacters(Map.AUT.PassWord, "^user.password");
		 * }
		 * </pre>
		 */
		public static boolean SetVariableValues(String varEqualVal, String...optionals){
			String action = DDDriverCommands.SETVARIABLEVALUES_KEYWORD;
			String parameters = Arrays.toString(combineParams(optionals, varEqualVal));

			try{ 
				if(varEqualVal == null) throw new SeleniumPlusException("parameter 'varEqualVal' cannot be null!");
				return command(action, _resolveDDVariables(varEqualVal, optionals));
			}
			catch(Throwable t){
				String msg = FAILStrings.convert(FAILStrings.NO_SUCCESS_2,
						action+" was not successful using '"+ parameters.toString() +"'",
						action, parameters);
				Logging.LogTestFailure(msg, t.getMessage());
				return false;
			}
		}
		
		/**
		 * Take screenshot and save it to a test file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_TakeScreenShot">Detailed Reference</a><p>
		 * @param testFile String, The test file name to save the screenshot.
		 * <pre>
		 *      Absolute file path.
		 *      Relative file path, 
		 *        if it contains file separator, it is relative to datapool or project root.
		 *        otherwise it is relative to the project test folder.
		 * </pre>
		 * @param subarea SubArea, indicating partial image of the screen to capture
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * //Take a whole screen image
		 * Misc.TakeScreenShot("D:\test\screenshot.png");
		 * //Take a sub image of screen
		 * SubArea subarea = new SubArea(0,0,"20%","30%");
		 * Misc.TakeScreenShot("sub_screenshot.png", subarea);
		 * }
		 * </pre>
		 */
		public static boolean TakeScreenShot(String testFile, SubArea subarea){
			return command(DDDriverCommands.TAKESCREENSHOT_KEYWORD, testFile, "No"/*Rotatable*/, subarea.toString());
		}
		/**
		 * Take screenshot and save it to a test file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_TakeScreenShot">Detailed Reference</a><p>
		 * @param testFile String, The test file name to save the screenshot.
		 * <pre>
		 *      Absolute file path.
		 *      Relative file path, 
		 *        if it contains file separator, it is relative to datapool or project root.
		 *        otherwise it is relative to the project test folder.
		 * </pre>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.TakeScreenShot("D:\test\screenshot.png");
		 * Misc.TakeScreenShot("screenshot.png");//Save to project test folder
		 * }
		 * </pre>
		 */
		public static boolean TakeScreenShot(String testFile){
			return command(DDDriverCommands.TAKESCREENSHOT_KEYWORD, testFile);
		}
		/**
		 * Switch WebBrowser by ID. 
		 * During test, multiple browsers can be opened by {@link #StartWebBrowser(String, int, String...)}<br>
		 * If user wants to switch between these opened browser, use can call this method.<br>
		 * This method requires a parameter 'ID', which is given by user when he calls {@link #StartWebBrowser(String, int, String...)}<br>
		 * See <a href="http://safsdev.github.io/sqabasic2000/DDDriverCommandsReference.htm#detail_UseWebBrowser">Detailed Reference</a><br>	
		 * This is currently <b>ONLY supported for Selenium WebDriver Engine.</b>
		 * @param ID String, the ID served as key to get the WebDriver from cache.<br>
		 * @return true on success 
		 * @example	 
		 * <pre>
		 * {@code
		 * UseWebBrowser("GoogleNewWindow");
		 * }
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 * @see #StartWebBrowser(String, String, String...)
		 */
		public static boolean UseWebBrowser(String ID){
			return command(DDDriverCommands.USEWEBBROWSER_KEYWORD, ID);
		}
		/**
		 * Verify the current contents of the Windows clipboard with a benchmark file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_VerifyClipboardToFile">Detailed Reference</a><p>
		 * @param benchFile String, The bench file name to compare with the clipboard's content.<br>
		 * <pre>
		 *      Absolute file path.
		 *      Relative file path, 
		 *        if it contains file separator, it is relative to datapool or project root.
		 *        otherwise it is relative to the project bench folder.
		 * </pre>
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] encoding</b> String, "Encoding" used to read the bench file. The default is "UTF-8".<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * Misc.VerifyClipboardToFile("D:\bench\clipboard.txt");
		 * Misc.VerifyClipboardToFile("clipboard.txt");//Compare with the file under project bench directory
		 * }
		 * </pre>
		 */
		public static boolean VerifyClipboardToFile(String benchFile, String...optionals){
			return command(DDDriverCommands.VERIFYCLIPBOARDTOFILE_KEYWORD, combineParams(optionals, benchFile));
		}
		/**
		 * Send a "GET" HTTP Request by AJAX, and Verify URL Content with a string content.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_VerifyURLContent">Detailed Reference</a><p>
		 * @param url String, The URL to request.
		 * @param content String, The content to verify with.<br>
		 * @param optionals
		 * <ul>
		 * <li><b>optionals[0] timeout</b> int, "Timeout" in seconds. The default is 120 seconds.
		 * <li><b>optionals[1] headerName</b> String, the name of the request header
		 * <li><b>optionals[2] headerValue</b> String, the value for the request header
		 * </ul>
		 * <b>optionals</b> parameters '<b>headerName</b>' and '<b>headerValue</b>' must appear in pair, and they can present more than 1 time, 
		 * which means multiple pair of ('headerName', 'headerValue') can be provided. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String url = "http://rest.api.url";
		 * String content = "the content returned by url";//for verification
		 * //Get content of url and wait for response with timeout 60 seconds, and verify with content.
		 * if(Misc.VerifyURLContent(url, content, "60")){
		 *   System.out.println("content of url '"+url+"' matches with content '"+content+"'.");
		 * }
		 * //Get content of url with headers ("Accept", "text/*") and ("Accept-Charset", "UTF-8") 
		 * //and wait for response with default timeout 120 seconds, then verify with content.
		 * if(Misc.VerifyURLContent(url, content, "", "Accept", "text/*", "Accept-Charset", "UTF-8" )){
		 *   System.out.println("content of url '"+url+"' matches '"+content+"'.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean VerifyURLContent(String url, String content, String...optionals){
			return command(DDDriverCommands.VERIFYURLCONTENT_KEYWORD, combineParams(optionals, url, content));
		}
		/**
		 * Send a "GET" HTTP Request by AJAX, and Verify URL Content with a file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_VerifyURLToFile">Detailed Reference</a><p>
		 * @param url String, The URL to request.
		 * @param file String, The name of the bench file holding the content to be compared with the response sent back from the server.
		 *                     The bench file can be a full path, a relative path, or a file name. 
		 *                     For relative path, it is appended to the project's path to build the full path of the file. 
		 *                     For file name, the file is supposed under the project's bench directory (it is Datapool\Bench in SAFS or Benchmarks in SE+). 
		 *                     In any case the parent folder of the file must exist.
		 * @param optionals
		 * <ul>
		 * <li><b>optionals[0] timeout</b> int, "Timeout" in seconds. The default is 120 seconds.
		 * <li><b>optionals[1] headerName</b> String, the name of the request header
		 * <li><b>optionals[2] headerValue</b> String, the value for the request header
		 * </ul>
		 * <b>optionals</b> parameters '<b>headerName</b>' and '<b>headerValue</b>' must appear in pair, and they can present more than 1 time, 
		 * which means multiple pair of ('headerName', 'headerValue') can be provided. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String url = "http://rest.api.url";
		 * String file = "content.html";//for verification
		 * //Get content of url and wait for response with timeout 60 seconds, and verify with a file.
		 * if(Misc.VerifyURLToFile(url, file, "60")){
		 *   System.out.println("content of url '"+url+"' matches with file '"+file+"'.");
		 * }
		 * //Get content of url with headers ("Accept", "text/*") and ("Accept-Charset", "UTF-8") 
		 * //and wait for response with default timeout 120 seconds, then verify with a file.
		 * if(Misc.VerifyURLToFile(url, file, "", "Accept", "text/*", "Accept-Charset", "UTF-8" )){
		 *   System.out.println("content of url '"+url+"' matches with file '"+file+"'.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean VerifyURLToFile(String url, String file, String...optionals){
			return command(DDDriverCommands.VERIFYURLTOFILE_KEYWORD, combineParams(optionals, url, file));
		}
		
		/**
		 * Wait for object in seconds
		 * @param comp -- Component (from generated Map.java)
		 * @param time - time in second
		 * @return
		 * @example	 
		 * <pre>
		 * {@code
		 * WaitForGUI(Map.Google.SignIn,10);
		 * }
		 * </pre>
		 */
		public static boolean WaitForGUI(org.safs.model.Component comp, long time){		
			
			String window = comp.getParentName();
			String component = comp.getName();
			if (window == null) window = component;
			String sTime = Integer.toString((int)time);		
			String[] params = {window,component,sTime};
			
			return command(DDDriverCommands.WAITFORGUI_KEYWORD,params);
		}
		/**
		 * Wait for a Window or Component to become valid.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_WaitForGUI">Detailed Reference</a><p>
		 * @param component Component, The component to wait.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds.
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * //wait for at the most 15 seconds until Basc_Button becomes valid.
		 * Misc.WaitForGUI(Map.SAPDemoPage.Basc_Button);
		 * //wait for at the most 2 seconds until Basc_Button becomes valid.
		 * Misc.WaitForGUI(Map.SAPDemoPage.Basc_Button, "2");
		 * }
		 * </pre>
		 */
		public static boolean WaitForGUI(org.safs.model.Component component, String...optionals){
			String winName = component.getParentName();
			String compName = component.getName();
			if(winName==null) winName=compName;

			return command(DDDriverCommands.WAITFORGUI_KEYWORD, combineParams(optionals, winName, compName));
		}
		/**
		 * Goto the named block in the current table if a GUI component exists.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverFlowCommandsReference.htm#detail_OnGUIExistsGotoBlockID">Detailed Reference</a><p>
		 * <b>Note: SAFSPlus user does not call this API directly.</b> If it is called, the return code could be used to tell if the component exist or not.<br>
		 * @param component Component, The component to check
		 * @param blockid String, the BLOCKID to branch
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds.
		 * </ul>
		 * @return String, the BLOCKID if it does branch<br>
		 *                 void string "" if it does not branch.<br>
		 *                 null if there is something wrong.<br>
		 * @see Misc#IsComponentExists(org.safs.model.Component, String...)
		 */
		public static String OnGUIExistsGotoBlockID(org.safs.model.Component component, String blockid, String...optionals){
			String winName = component.getParentName();
			String compName = component.getName();
			if(winName==null) winName=compName;
			
			if(command(DriverCommands.ONGUIEXISTSGOTOBLOCKID_KEYWORD, combineParams(optionals, blockid, winName, compName))){
				return prevResults.getStatusInfo();
			}else{
				IndependantLog.error(StringUtils.debugmsg(false)+" failed.");
				return null;
			}
		}

		/**
		 * Goto the named block in the current table if a GUI component does not exist.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverFlowCommandsReference.htm#detail_OnGUINotExistGotoBlockID">Detailed Reference</a><p>
		 * <b>Note: SAFSPlus user does not call this API directly.</b> If it is called, the return code could be used to tell if the component exist or not.<br>
		 * @param component Component, The component to check
		 * @param blockid String, the BLOCKID to branch
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds.
		 * </ul>
		 * @return String, the BLOCKID if it does branch<br>
		 *                 void string "" if it does not branch.<br>
		 *                 null if there is something wrong.<br>
		 */
		public static String OnGUINotExistGotoBlockID(org.safs.model.Component component, String blockid, String...optionals){
			String winName = component.getParentName();
			String compName = component.getName();
			if(winName==null) winName=compName;
			
			if(command(DriverCommands.ONGUINOTEXISTGOTOBLOCKID_KEYWORD, combineParams(optionals, blockid, winName, compName))){
				return prevResults.getStatusInfo();
			}else{
				IndependantLog.error(StringUtils.debugmsg(false)+" failed.");
				return null;
			}
		}
		
		/**
		 * Wait for a Window or Component to become invalid.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_WaitForGUIGone">Detailed Reference</a><p>
		 * @param component Component, The component to wait its gone.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds.
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * //wait for at the most 15 seconds until Basc_Button becomes invalid.
		 * Misc.WaitForGUIGone(Map.SAPDemoPage.Basc_Button);
		 * //wait for at the most 2 seconds until Basc_Button becomes invalid.
		 * Misc.WaitForGUIGone(Map.SAPDemoPage.Basc_Button, "2");
		 * }
		 * </pre>
		 */
		public static boolean WaitForGUIGone(org.safs.model.Component component, String...optionals){
			String winName = component.getParentName();
			String compName = component.getName();
			if(winName==null) winName=compName;
			
			return command(DDDriverCommands.WAITFORGUIGONE_KEYWORD, combineParams(optionals, winName, compName));
		}		

		/**
		 * Wait for a specific Window or Component property value to match an expected value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_WaitForPropertyValue">Detailed Reference</a><p>
		 * @param component Component, The component to wait for its property matching.
		 * @param propertyName String, Case-sensitive name of object property to watch.
		 * @param expectedValue String, Case-sensitive value of the object property to match.
		 * @param optionals String
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds. <br>
		 * <b>optionals[1] caseInsensitive</b> boolean, match an expected value case insensitively. Default is false. "FALSE" will cause the comparison of the property value and the expected value to ignore case. 
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example 
		 * 1. Wait for the property value matching with expected value.
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValue(Map.windowName.componentName, "propertyValue", "expectedValue");
		 * }
		 * </pre>
		 * 
		 * 2. Wait for the property value matching with expected value with 20 seconds. 
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValue(Map.windowName.componentName, "propertyValue", "expectedValue", "20");
		 * }
		 * </pre>
		 *  
		 * 3. Wait for the property value matching with expected value with 20 seconds and compare case sensitively. 
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValue(Map.windowName.componentName, "propertyValue", "expectedValue", "true");
		 * }
		 * </pre>
		 * 
		 * @author SCNTAX(Tao.xie)
		 */
		public static boolean WaitForPropertyValue(org.safs.model.Component component, String propertyName, String expectedValue, String... optionals) {
			String winName = component.getParentName();
			String compName = component.getName();
			
			if(winName==null) 
				winName=compName;
			
			String[] allParams = combineParams(optionals, winName, compName, propertyName, expectedValue);
			
			return command(DDDriverCommands.WAITFORPROPERTYVALUE_KEYWORD, allParams);
		}
			
		/**
		 * Wait for a specific Window or Component property value to change from a known value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_WaitForPropertyValueGone">Detailed Reference</a><p>
		 * @param component Component, The component to wait for its matching property gone.
		 * @param propertyName String, Case-sensitive name of object property to watch.
		 * @param expectedValue String, Case-sensitive value of the object property to match.
		 * @param optionals String
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds. <br>
		 * <b>optionals[1] caseInsensitive</b> boolean, match an expected value case insensitively. Default is false. "FALSE" will cause the comparison of the property value and the expected value to ignore case. 
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example 
		 * 1. Wait for the property value gone with expected value.
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValueGone(Map.windowName.componentName, "propertyValue", "expectedValue");
		 * }
		 * </pre>
		 * 
		 * 2. Wait for the property value gone with expected value with 20 seconds. 
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValueGone(Map.windowName.componentName, "propertyValue", "expectedValue", "20");
		 * }
		 * </pre>
		 *  
		 * 3. Wait for the property value gone with expected value with 20 seconds and compare case sensitively. 
		 * <pre>
		 * {@code
		 * Misc.WaitForPropertyValueGone(Map.windowName.componentName, "propertyValue", "expectedValue", "true");
		 * }
		 * </pre>
		 * 
		 * @author SCNTAX(Tao.Xie)
		 */
		public static boolean WaitForPropertyValueGone(org.safs.model.Component component, String propertyName, String expectedValue, String... optionals) {
			String winName = component.getParentName();
			String compName = component.getName();
			
			if(winName==null) 
				winName=compName;
			
			String[] allParams = combineParams(optionals, winName, compName, propertyName, expectedValue);
			
			return command(DDDriverCommands.WAITFORPROPERTYVALUEGONE_KEYWORD, allParams);
		}
		
		/**
		 * Wait for a Registry Key to become valid.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_WaitForRegistryKeyExists">Detailed Reference</a><p>
		 * @param key String, The Registry Key name to seek.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] value</b> String, The value name under the parent key to seek.
		 * <b>optionals[1] timeout</b> int, "Timeout" in seconds.
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment";
		 * String value = "CurrentVersion";
		 * String result = null;
		 * if(Misc.WaitForRegistryKeyExists(key, value)){
		 *   result = Misc.GetRegistryKeyValue(key, value);
		 * }
		 * }
		 * </pre>
		 */
		public static boolean WaitForRegistryKeyExists(String key, String...optionals){
			return command(DDDriverCommands.WAITFORREGISTRYKEYEXISTS_KEYWORD, combineParams(optionals, key));
		}
		/**
		 * Wait for a specific Registry KeyValue to match an expected value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverCommandsReference.htm#detail_WaitForRegistryKeyValue">Detailed Reference</a><p>
		 * @param key String, The Registry Key name to seek.
		 * @param value String, The value name under the parent key to seek.
		 * @param expectedValue String,Case-sensitive value to match with the key value.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, "Timeout" in seconds.
		 * <b>optionals[1] caseInsensitive</b> boolean, if true comparison will ignore case.
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment";
		 * String value = "CurrentVersion";
		 * String expectedValue = "1.7";
		 * //Wait 'HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment:CurrentVersion' to be "1.7"
		 * //Program will continue if the value is not detected as "1.7" within timeout 2 seconds.
		 * if(Misc.WaitForRegistryKeyValue(key, value, expectedValue, "2")){
		 *   System.out.println(key+":"+value+" equals to "+expectedValue);
		 * }
		 * }
		 * </pre>
		 */
		public static boolean WaitForRegistryKeyValue(String key, String value, String expectedValue, String...optionals){
			return command(DDDriverCommands.WAITFORREGISTRYKEYVALUE_KEYWORD, combineParams(optionals, key, value, expectedValue));
		}
		
		//TODO WaitForWebPage ???
	}

	/**
	 * Class for miscellaneous Driver Commands.<br>
	 * This is a sub-class of {@link DriverCommand} and it provides more convenient wrapper APIs.
	 * 
	 * @see DriverCommand
	 */
	public static class Misc extends DriverCommand{
		/**
		 * According to the component, get its "recognition string" defined in App Map file.<br>
		 * This component can be defined in one of the App Map file of a <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a>.<br>
		 * @param component org.safs.model.Component, The component to search.
		 * @param optionals
		 * <ul>
		 * <li>optionals[0] variable-name String, SAFSVARS variable name to receive and store the mapped value.
		 * <li>optionals[1] logMessage boolean, if true then write the log message.
		 * </ul>
		 * @return String, the "recognition string" of the component.
		 * @example
		 * <pre>
		 * In map file, we have defined the followings
		 * [Login]
		 * UserID="xpath=foo"
		 * 
		 * In our test code, we want to get the string value defined in map for item "UserID" under section "Login"
		 * {@code
		 * String userID = Misc.GetAppMapValue(Map.Login.UserID); //userID will be assigned as "xpath=foo"
		 * String userID = Misc.GetAppMapValue(Map.Login.UserID, "result"); //get Map.Login.UserID and assign to variable "result" 
		 * String userID = Misc.GetAppMapValue(Map.Login.UserID, "", "false"); //get Map.Login.UserID without logging message
		 * }
		 * </pre>	
		 * @throws SeleniumPlusException if the parameter is null or the command was not executed successfully.
		 */
		public static String GetAppMapValue(org.safs.model.Component component, String... optionals) throws SeleniumPlusException{
			if(component==null) throw new SeleniumPlusException("component is null, can not find its mapped value.");

			String section = component.getParentName()==null? component.getName(): component.getParentName();
			String item = component.getName();
			return _GetAppMapValue(null, section, item, optionals);
		}
		/**
		 * Get the constant value defined in <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a>
		 * @param contstant String, an constant name under the "ApplicationConstants"
		 * @return String, the mapped constant value.
		 * @example
		 * <pre>
		 * In base.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in base"
		 * Constant_B="Constant_B in base"
		 * [Login]
		 * UserID="UserID in base"
		 * 
		 * In specific.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in specific"
		 * [Login]
		 * ChineseGUI="ChineseGUI"
		 * 
		 * {@code
		 * String baseMap = "base.map";
		 * String specificMap = "specific.map";
		 * Misc.SetApplicationMap(baseMap);
		 * Misc.SetApplicationMap(specificMap);
		 * String value = null;
		 * value = Misc.GetAppMapValue("Constant_A");//return "Constant_A in specific"
		 * value = Misc.GetAppMapValue("Constant_B");//return "Constant_B in base"
		 * Misc.CloseApplicationMap(specificMap);
		 * value = Misc.GetAppMapValue("Constant_A");//return "Constant_A in base"
		 * value = Misc.GetAppMapValue("Constant_B");//return "Constant_B in base"
		 * }
		 * </pre>	
		 * @throws SeleniumPlusException if the parameter is null or the command was not executed successfully.
		 */
		public static String GetAppMapValue(String contstant) throws SeleniumPlusException{
			return _GetAppMapValue(null, null, contstant);
		}
		/**
		 * According to pair (section,item), get the value defined in <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a>
		 * @param section String, a section name. If provide null, program will use default-section "ApplicationConstants".
		 * @param item String, an item name under the section
		 * @return String, the mapped value.
		 * @example
		 * <pre>
		 * In base.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in base"
		 * Constant_B="Constant_B in base"
		 * [Login]
		 * UserID="UserID in base"
		 * 
		 * In specific.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in specific"
		 * [Login]
		 * ChineseGUI="ChineseGUI"
		 * 
		 * {@code
		 * String baseMap = "base.map";
		 * String specificMap = "specific.map";
		 * Misc.SetApplicationMap(baseMap);
		 * Misc.SetApplicationMap(specificMap);
		 * String value = null;
		 * value = Misc.GetAppMapValue("Login", "UserID");//return "UserID in base"
		 * value = Misc.GetAppMapValue("Login", "ChineseGUI");//return "ChineseGUI"
		 * value = Misc.GetAppMapValue((String)null, "Constant_A");//return "Constant_A in specific"
		 * value = Misc.GetAppMapValue((String)null, "Constant_B");//return "Constant_B in base"
		 * Misc.CloseApplicationMap(specificMap);
		 * value = Misc.GetAppMapValue("Login", "UserID");//return "UserID in base"
		 * value = Misc.GetAppMapValue("Login", "ChineseGUI");//return ""
		 * value = Misc.GetAppMapValue((String)null, "Constant_A");//return "Constant_A in base"
		 * value = Misc.GetAppMapValue((String)null, "Constant_B");//return "Constant_B in base"
		 * }
		 * </pre>	
		 * @throws SeleniumPlusException if the parameter is null or the command was not executed successfully.
		 */
		public static String GetAppMapValue(String section, String item) throws SeleniumPlusException{
			return _GetAppMapValue(null, section, item);
		}
		/**
		 * According to a section name and a item name, get the value defined in App Map file (defined by mapID).<br>
		 * <font color="red">Note: if the mapID is provided, program will ONLY search that map, NOT in the map chain!</font>
		 * @param mapID String, the ID to decide which map will be used. If provided null, program will search in the <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining">App Map chain</a><br>
		 *                      It is normally the map file name under the project datapool directory (the "Maps" folder).
		 * @param section String, a section name. If provide null, program will use default-section "ApplicationConstants".
		 * @param item String, an item name under the section
		 * @param optionals
		 * <ul>
		 * <li>optionals[0] variable-name String, SAFSVARS variable name to receive and store the mapped value.
		 * <li>optionals[1] logMessage boolean, if true then write the log message.
		 * </ul>
		 * @return String, the mapped value.
		 * @example
		 * <pre>
		 * In base.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in base"
		 * Constant_B="Constant_B in base"
		 * [Login]
		 * UserID="UserID in base"
		 * 
		 * In specific.map file, we have defined the followings
		 * [ApplicationConstants]
		 * Constant_A="Constant_A in specific"
		 * [Login]
		 * ChineseGUI="ChineseGUI"
		 * 
		 * {@code
		 * String baseMap = "base.map";
		 * String specificMap = "specific.map";
		 * Misc.SetApplicationMap(baseMap);
		 * Misc.SetApplicationMap(specificMap);
		 * String value = null;
		 * value = Misc.GetAppMapValue(null, "Login", "UserID");//return "UserID in base", search in map-chain
		 * value = Misc.GetAppMapValue(specificMap, "Login", "UserID");//return "", not found, search in specific.map
		 * value = Misc.GetAppMapValue(null, null, "Constant_A");//return "Constant_A in specific", search in map-chain
		 * value = Misc.GetAppMapValue(baseMap, null, "Constant_A");//return "Constant_A in base", search in base.map
		 * value = Misc.GetAppMapValue(specificMap, null, "Constant_A");//return "Constant_A in specific", search in specific.map
		 * value = Misc.GetAppMapValue(null, null, "Constant_B");//return "Constant_B in base", search in map-chain
		 * value = Misc.GetAppMapValue(specificMap, null, "Constant_B");//return "", not found, search in specific.map
		 * value = Misc.GetAppMapValue(null, "Login", "UserID", "UserIDVar");//return "UserID in base", search in map-chain, store in SAFSVARS "UserIDVar" variable.
		 * }
		 * </pre>
		 * @throws SeleniumPlusException if the command was not executed successfully.
		 */
		//Not exposed yet, the other GetAppMapValue is enough? Do we permit user to get map value from
		//a certain Map, which will NOT get value from the "map chain".
		private static String _GetAppMapValue(String mapID, String section, String item, String... optionals) throws SeleniumPlusException{
			String message = (section==null?"ApplicationConstant":section)+":"+item+" in "+(mapID==null?"Map Chain":mapID);
			
			try{
				String value = Runner.jsafs().getMappedValue(mapID, section, item);
				if(value==null) throw new SeleniumPlusException("The mapped value is null!");
				boolean logMessage = true;
				if(optionals != null){
					if(optionals.length>0) Runner.jsafs().setVariable(optionals[0], value);
					if(optionals.length>1) logMessage = StringUtilities.convertBool(optionals[1]);
				}
				if(logMessage) Logging.LogTestSuccess("GetAppMapValue Success. " + message, "value is "+value);
				return value;
			}catch(Exception e){
				Logging.LogTestFailure("GetAppMapValue Fail. " + message, StringUtils.debugmsg(e));
				throw new SeleniumPlusException("Can not find value for "+message);
			}
		}
		/**
		 * Get the general pause time between records (SAFSPlus API calls), in milliseconds.
		 * @return int, pause time between records, in milliseconds.
		 */
		public static int GetMillisBetweenRecords(){
			return Runner.jsafs().getMillisBetweenRecords();
		}
		/**
		 * Resolve a string as an expression.<br>
		 * If {@link #isExpressionsOn()} is true, the string will be resolved as "math" and "DDVariable"<br>
		 * If {@link #isExpressionsOn()} is false, the string will be resolved as "DDVariable"<br>
		 * <b>Note:</b> In SE+, the expression will be handled as "math" and "DDVariable", or not handled at all, just<br>
		 * as described in {@link #Expressions(boolean)}, but sometimes we want to resolve an expression as<br>
		 * "DDVariable" but not "math", then we can turn off expression by {@link #Expressions(boolean)} and call this method.<br>
		 * @param expression String, the string to be resolved.
		 * @return String, the resolved string. Or null if some error occurs.
		 * 
		 * @see #isExpressionsOn()
		 * @see #Expressions(boolean)
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String expression = "^var=3+5";
		 * Misc.Expressions(true);
		 * Misc.ResolveExpression(expression);//string "8" will be assigned to variable "var", and be returned as result.
		 * 
		 * Misc.Expressions(false);
		 * Misc.ResolveExpression(expression);//string "3+5" will be assigned to variable "var", and be returned as result.
		 * }
		 * </pre>
		 */
		public static String ResolveExpression(String expression){
			String action = "ResolveExpression";

			try{
				JSAFSDriver jsafs = Runner.jsafs();
				String result = jsafs.resolveExpression(expression);
				String msg = GENStrings.convert(GENStrings.SUCCESS_3B,
						                        action+" '"+result+"' successful using '"+expression+"'",
						                        action, result, expression);
				Logging.LogMessage(msg);
				return result;
			}
			catch(Throwable t){
				String msg = FAILStrings.convert(FAILStrings.NO_SUCCESS_2,
						action+" was not successful using '"+ expression +"'",
						action, expression);
				Logging.LogTestFailure(msg, t.getMessage());
				return null;
			}
		}
		private static final String FAKE_BLOCKID = "FAKE_BLOCKID";
		/**
		 * Test if a Window or Component exists within a timeout.
		 * @param component Component, The component to check
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] timeout</b> int, timeout in seconds. Default is 15 seconds.
		 * </ul>
		 * @return true if exist, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * //check if Basc_Button becomes valid within 15 seconds.
		 * Misc.IsComponentExists(Map.SAPDemoPage.Basc_Button);
		 * //check if Basc_Button becomes valid within 2 seconds.
		 * Misc.IsComponentExists(Map.SAPDemoPage.Basc_Button, "2");
		 * }
		 * </pre>
		 */
		public static boolean IsComponentExists(org.safs.model.Component component, String...optionals){
			String blockid = OnGUIExistsGotoBlockID(component, FAKE_BLOCKID, optionals);
			return StringUtils.isValid(blockid);
		}
		/**
		 * Return if the expression evaluation is turned on.
		 * Before using this method, Runner.run() must be invoked.
		 * @return boolean, true if the expression evaluation is turned on.
		 * @throws SeleniumPlusException
		 * @example
		 * <pre>
		 * {@code
		 * boolean originalExpressionOn = Misc.isExpressionsOn();
		 * Misc.Expressions(false);
		 * //Misc.Expressions(true);
		 * //User do something with expression on/off
		 * ...
		 * Misc.Expressions(originalExpressionOn);
		 * }
		 * </pre>
		 */
		public static boolean isExpressionsOn() throws SeleniumPlusException{
			try{
				return Runner.jsafs().isExpressionsEnabled();
			}catch(Throwable th){
				throw new SeleniumPlusException("Met "+StringUtils.debugmsg(th));
			}
		}
	}

	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsIndex.htm">DriverCounter keywords</a>, like StartTestSuite, StartCounter, LogCounterInfo etc.<br>
	 * It also provides:<br>
	 * some convenient APIs, like {@link Counters#PrintTestCaseSummary(String)}, {@link Counters#PrintTestSuiteSummary(String)} etc. by using combination of DriverCounter keywords.<br>
	 * CountStatusInterface instance ( {@link Counters#GetCounterStatus(String)} ) through the underlying JSAFS<br>
	 * 
	 */
	public static class Counters{
		private static boolean counterCommand(String command, String counterId, String details){
			String[] args = (details == null)? new String[1]: new String[2];
			args[0] = counterId;
			if(details != null) args[1] = details;
			return command(command, args);
		}
		/**
		 * Stop capturing test activity counts for a specific application feature or test-case if it is 
		 * still active, then print a summary report of all tests counted, passed, failed, and skipped, etc...
		 * @param tcname The name of the test-case to start using a Counter on.  The name must match a counter 
		 * that was previously started.
		 * @return false only if a failure of some kind was reported in attempting to stop the counter 
		 * or print the summary report into the log.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @see #prevResults
		 * @see #StartTestCase(String)
		 */
		public static boolean PrintTestCaseSummary(String tcname){
			try{ 
				Runner.command(DDDriverCounterCommands.STOPTESTCASE_KEYWORD, tcname);
				Runner.logGENERIC(" ", null);
				prevResults = Runner.command(DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, tcname);
	  		    Runner.logGENERIC(" ", null);
			}
			catch(Throwable t){
				// add failure info here
				prevResults = null;
				return false;}
			return true;
		}
		
		/**
		 * Stop capturing test activity counts for the overall suite of tests if it is 
		 * still active, then print a summary report of all counted, passed, failed, and skipped tests etc...
		 * @param suitename The name of the suite to stop (if still running) and process.  
		 * The name must match a counter that was previously started.
		 * @return false only if a failure of some kind was reported in attempting to stop the counter 
		 * or print the summary report into the log.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @see #prevResults
		 */
		public static boolean PrintTestSuiteSummary(String suitename){
			try{ 
				Runner.command(DDDriverCounterCommands.STOPSUITE_KEYWORD, suitename);
				Runner.logGENERIC(" ", null);
				prevResults = Runner.command(DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, suitename);
	  		    Runner.logGENERIC(" ", null);
			}
			catch(Throwable t){
				// add failure info here
				prevResults = null;
				return false;}
			return true;
		}
		
		/**
		 * Increment test counters as appropriate.
		 * @param passed true increments test successes, false increments test failures.
		 */
		public static void IncrementCounts(boolean passed){
			long status = passed ? CountersInterface.STATUS_TEST_PASS : CountersInterface.STATUS_TEST_FAILURE;
			Runner.jsafs().getCountersInterface().incrementAllCounters(new UniqueStringCounterInfo("STEP", "STEP"), status);
		}

		/**
		 * Retrieve all the information stored for a particular Counter. 
		 * @param counterId - the unique String ID of the counter to retrieve.
     	 * @return CounterStatusInterface for the specified counter, or null if it does not exist.
		 */
		public static CountStatusInterface GetCounterStatus(String counterId){
			return Runner.jsafs().getCounterStatus(counterId);
		}
		
		/***********  
        (Re)Start/Create an active tester-defined counter.
		<a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_StartCounter">Detailed Reference</a>
        @param counterId -- the unique Id of the counter.
        @param details -- optional -- any additional (String) details or description of the counter to be logged. 
        @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * boolean success = Counters.StartCounter(FEATURE_TEST_COUNTER, "Tracking all test records for this feature");
		 * ....(do some testing)
		 * success = Counters.StopCounter(FEATURE_TEST_COUNTER, "Testing for this feature is finished.");
		 * }
		 * </pre>
		 */     
		public static boolean StartCounter(String counterId, String... details){
			try{return counterCommand(DDDriverCounterCommands.STARTCOUNTER_KEYWORD, counterId, details[0]); }
			catch(ArrayIndexOutOfBoundsException x){
				return counterCommand(DDDriverCounterCommands.STARTCOUNTER_KEYWORD, counterId, null);
			}
		}
		/**
		 * Start capturing test activity counts for a specific application feature or test-case.
		 * @param tcname The name of the test-case to start using a Counter on.
		 * @return false only if a failure of some kind was reported in attempting to start the counter.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean StartTestCase(String tcname){
			try{
				Runner.logGENERIC(" ", null);
				prevResults = Runner.command(DDDriverCounterCommands.STARTTESTCASE_KEYWORD, tcname);}
			catch(Throwable t){
				// add failure info here
				prevResults = null;
				return false;}
			return true;
		}
		/**
		 * Start capturing test activity counts for the named test suite.
		 * @param suitename The name of the suite counter to start.
		 * @return false only if a failure of some kind was reported in attempting to start the counter.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean StartTestSuite(String suitename){
			try{
				Runner.logGENERIC(" ", null);
				prevResults = Runner.command(DDDriverCounterCommands.STARTSUITE_KEYWORD, suitename);}
			catch(Throwable t){
				// add failure info here
				prevResults = null;
				return false;}
			return true;
		}
	    /***********  
        Stop/Create a suspended tester-defined counter.
		<a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_StopCounter">Detailed Reference</a>
        @param counterId -- the unique Id of the counter.
        @param details -- optional -- any additional (String) details or description of the counter to be logged. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Counters.StartCounter(FEATURE_TEST_COUNTER, "Tracking all test records for this feature");
		* ....(do some testing)
		* success = Counters.StopCounter(FEATURE_TEST_COUNTER, "Testing for this feature is finished.");
		* }
		* </pre>
        */     
		public static boolean StopCounter(String counterId, String... details){
			try{return counterCommand(DDDriverCounterCommands.STOPCOUNTER_KEYWORD, counterId, details[0]); }
			catch(ArrayIndexOutOfBoundsException x){
				return counterCommand(DDDriverCounterCommands.STOPCOUNTER_KEYWORD, counterId, null);
			}
		}
		/**
		 * Stop capturing test activity counts for a specific application feature or test-case.
		 * @param tcname The name of the test-case to stop.  The name must match a counter that was 
		 * previously started.
		 * @return false only if a failure of some kind was reported in attempting to stop the counter.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @see #prevResults
		 * @see #StartTestCase(String)
		 * @see #PrintTestCaseSummary(String)
		 */
		public static boolean StopTestCase(String tcname){
			try{ 
				prevResults = Runner.command(DDDriverCounterCommands.STOPTESTCASE_KEYWORD, tcname);
				Runner.logGENERIC(" ", null);
			}
			catch(Throwable t){
				// add failure info here
				prevResults = null;
				return false;}
			return true;
		}
	    /***********  
        Suspend counting on all active counters.
		<a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_SuspendStatusCounts">Detailed Reference</a>
        @param reason -- optional -- any additional (String) details or reason for the suspension to be logged. 
        @return true on success, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode()
		* @see org.safs.TestRecordHelper#getStatusInfo()
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Counters.StartCounter(FUNCTIONAL_TEST_COUNTER, "Tracking all test records for Functional Testing");
		* ....(do some testing)
		* success = Counters.SuspendCounts("Scenario preparation commencing.");
		* ....(do some things you don't want logged or corrupting counters.)
		* success = Counters.ResumeCounts("Scenario preparation complete."); 
		* }
		* </pre>
        */     
		public static boolean SuspendCounts(String... reason){
			return command(DDDriverCounterCommands.SUSPENDSTATUSCOUNTS_KEYWORD, reason);
		}
	    /***********  
        Resume counting on all active counters.  Counters previously stopped with StopCounter 
        will remain stopped.
		<a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_ResumeStatusCounts">Detailed Reference</a>
        @param reason -- optional -- any additional (String) details to be logged. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Counters.StartCounter(FUNCTIONAL_TEST_COUNTER, "Tracking all test records for Functional Testing");
		* ....(do some testing)
		* success = Counters.SuspendCounts("Scenario preparation commencing.");
		* ....(do some things you don't want logged or corrupting counters.)
		* success = Counters.ResumeCounts("Scenario preparation complete."); 
		* }
		* </pre>
        */     
		public static boolean ResumeCounts(String... reason){
			return command(DDDriverCounterCommands.RESUMESTATUSCOUNTS_KEYWORD, reason);
		}
	    /***********  
        Log the counter info (counts) for a specific counter.
        <a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_LogCounterInfo">Detailed Reference</a>
        @param counterId -- the unique Id of the counter.
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode()
		* @see org.safs.TestRecordHelper#getStatusInfo()
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Counters.StartCounter(FUNCTIONAL_TEST_COUNTER, "Tracking all test records for Functional Testing");
		* ....(do your testing, then log a snapshot/final statistics)
		* success = Counters.LogCounterInfo(FUNCTIONAL_TEST_COUNTER); 
		* }
		* </pre>
        */
		public static boolean LogCounterInfo(String counterId){
			return command(DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, counterId);
		}
	    /***********  
        Store the counter info (counts) for a specific counter into a DDVariable array.
        <a href="http://safsdev.github.io/sqabasic2000/DDDriverCounterCommandsReference.htm#detail_StoreCounterInfo">Detailed Reference</a>
        @param counterId -- the unique Id of the counter.
        @param varPrefix -- the unique root name for a list of DDVariables to hold the counter info.
        @return true if successfully executed, false otherwise.<p>
        @see SAFSPlus#GetVariableValue(String)
        @see #prevResults
		@see org.safs.TestRecordHelper#getStatusCode()
		@see org.safs.TestRecordHelper#getStatusInfo()
		<p>        
        @example
        <pre>
        {@code
        boolean success = Counters.StartCounter(FUNCTIONAL_TEST_COUNTER, "Tracking all functional test records.");
        ...(do some testing, then check counter status) 
        success = Counters.StoreCounterInfo(FUNCTIONAL_TEST_COUNTER, VAR_TEST);
        if(success){
            int passes   = Integer.parseInt(GetVariableValue(VAR_TEST+".test_passes"));
            int failures = Integer.parseInt(GetVariableValue(VAR_TEST+".test_failures"));
            int tests    = Integer.parseInt(GetVariableValue(VAR_TEST+".test_records"));
        }
        </pre>        
        */
		public static boolean StoreCounterInfo(String counterId, String varPrefix){				
			return command(DDDriverCounterCommands.STORECOUNTERINFO_KEYWORD, new String[]{counterId, varPrefix});
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/DDDriverLogCommandsIndex.htm">Logging keywords</a>, like LogMessage, LogTestWarning etc.<br>
	 */
	public static class Logging{
		private static boolean logCommand(String command, String message, String details){
			int i = details == null ? 1: 2;
			String[] args = new String[i];
			if(message!=null && message.length()>2 && !message.startsWith("\"")) 
				message = quote(message);
			args[0] = message;
			if (details != null) {
				if(details.length() > 2 && !details.startsWith("\"")) 
					details = quote(details);
				args[1] = details;
			}
			return command(command, args);
		}
		/**
		 * Set the Log facilities LOG_LEVEL to the specified value.
		 * @param level - "INFO", "WARN", "ERROR".
		 * @return true if no known errors occurred. 
		 */
		private static boolean _setLogLevel(String level){
			String facname = Runner.jsafs().getCycleLogName();
			if(facname == null) facname = Runner.jsafs().getSuiteLogName();
			if(facname == null) facname = Runner.jsafs().getStepLogName();
			if(facname == null) return false;
			Runner.jsafs().getLogsInterface().setLogLevel(new UniqueStringLogLevelInfo(facname, level));
			return true;
		}
		/**
		 * Set the Log facilities LOG_LEVEL to log WARNINGS and FAILURES only.
		 * @return true if no known errors occurred. 
		 */
		public static boolean SetLogWarningsAndFailuresMode(){
			return _setLogLevel(AbstractSAFSLoggingService.SLS_SERVICE_PARM_WARN);
		}
		/**
		 * Set the Log facilities LOG_LEVEL to log FAILURES only.
		 * @return true if no known errors occurred. 
		 */
		public static boolean SetLogFailuresOnlyMode(){
			return _setLogLevel(AbstractSAFSLoggingService.SLS_SERVICE_PARM_ERROR);
		}
		/**
		 * Set the Log facilities LOG_LEVEL to log all INFO, WARNINGS, and FAILURES.
		 * @return true if no known errors occurred. 
		 */
		public static boolean SetLogAllInfoMode(){
			return _setLogLevel(AbstractSAFSLoggingService.SLS_SERVICE_PARM_INFO);
		}

	    /***********  
        Log a special "FAILED OK" message indicating one or more subsequent failures are EXPECTED.
        @param message -- the message indicating one or more subsequent failures should be expected.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogFailureOK("The following 2 tests should fail due to bad parameters."); 
		* boolean success = Logging.LogFailureOK("The Login should fail.", "The userID and password are bogus.");
		* }
		* </pre>
        */     
		public static boolean LogFailureOK(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGFAILUREOK_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGFAILUREOK_KEYWORD, message, null);
			}
		}
		
	    /***********  
        Log a special "WARN  OK" message indicating one or more subsequent warnings are EXPECTED.
        @param message -- the message indicating one or more subsequent warnings should be expected.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogWarningOK("The following 2 tests will produce expected warnings."); 
		* boolean success = Logging.LogWarningOK("The following test warning is expected.", "The optional item is not correct.");
		* }
		* </pre>
        */     
		public static boolean LogWarningOK(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGWARNINGOK_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGWARNINGOK_KEYWORD, message, null);
			}
		}
		
	    /***********  
        Log a test FAILURE message.
        Also increments TEST FAILURE counts on any active counters.
        @param message -- the simple failure message to log.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogTestFailure("The Login Window was NOT found."); 
		* boolean success = Logging.LogTestFailure("The Login Window was NOT found.", "The Login Window MUST be issued at this time.");
		* }
		* </pre>
        */     
		public static boolean LogTestFailure(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGTESTFAILURE_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGTESTFAILURE_KEYWORD, message, null);
			}
		}
	    /***********  
        Log a test SUCCESS message.
        Also increments TEST PASS counts on any active counters.
        @param message -- the simple success message to log.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogTestSuccess("The Login Window was found."); 
		* boolean success = Logging.LogTestSuccess("The Login Window was found.", "The Login Window MUST be issued at this time.");
		* }
		* </pre>
        */     
		public static boolean LogTestSuccess(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGTESTSUCCESS_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGTESTSUCCESS_KEYWORD, message, null);
			}
		}
	    /***********  
        Log a test WARNING message.
        Also increments TEST WARNING counts on any active counters.
        @param message -- the simple warning message to log.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogTestWarning("The Login Window was not found."); 
		* boolean success = Logging.LogTestWarning("The Login Window was not found.", "The Login Window might not be issued if already logged in.");
		* }
		* </pre>
		*/     
		public static boolean LogTestWarning(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGTESTWARNING_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGTESTWARNING_KEYWORD, message, null);
			}
		}
	    /***********  
        Log a generic (non-test) message.
        @param message -- the generic message to log.
        @param details -- optional -- any additional (String) details or supporting information. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.LogMessage("An interesting thing happened just now!"); 
		* success = Logging.LogMessage("It was so interesting!", "The image we captured is bigger than we expected.");
		* }
		* </pre>
        */     
		public static boolean LogMessage(String message, String... details){
			try{ return logCommand(DDDriverLogCommands.LOGMESSAGE_KEYWORD, message, details[0]);}
			catch(ArrayIndexOutOfBoundsException x) {
				return logCommand(DDDriverLogCommands.LOGMESSAGE_KEYWORD, message, null);
			}
		}
	    /***********  
        Suspend all logging output. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.SuspendLogging();
		* ....(do some stuff that you don't want logged or counted.)
		* success = Logging.ResumeLogging();
		* }
		* </pre>
        */     
		public static boolean SuspendLogging(){
			return command(DDDriverLogCommands.SUSPENDLOGGING_KEYWORD, new String[0]);
		}
	    /***********  
        Resume all logging output. 
        @return true if successful, false otherwise.<p>
        * @see #prevResults
		* @see org.safs.TestRecordHelper#getStatusCode
		* @see org.safs.TestRecordHelper#getStatusInfo
		* <p>
		* @example
		* <pre>
		* {@code
		* boolean success = Logging.SuspendLogging();
		* ....(do some stuff that you don't want logged or counted.)
		* success = Logging.ResumeLogging();
		* }
		* </pre>
        */     
		public static boolean ResumeLogging(){
			return command(DDDriverLogCommands.RESUMELOGGING_KEYWORD, new String[0]);
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/DDDriverFileCommandsIndex.htm">File keywords</a>, like OpenFile, ReadFileLine etc.<br>
	 * <pre>
	 * If you meet some errors when calling these API, please try to run 
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Files.xxx();
	 * </pre>
	 */
	public static class Files{
		
		/**
		 * Close an opened file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_CloseFile">Detailed Reference</a><p>
		 * @param fileNo String, the file number of an opened file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * if(Files.CloseFile(fileNo))
		 *   System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * </pre>
		 */
		public static boolean CloseFile(String fileNo){
			return command(DDDriverFileCommands.CLOSEFILE_KEYWORD, fileNo);
		}
		/**
		 * Copy the content of source file to target file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_CopyFile">Detailed Reference</a><p>
		 * @param source String, the name of source file.
		 * @param dest String, the name of destination file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory";
		 * String file = directory+File.separator+"new test file3.txt";
		 * String dest = directory+File.separator+"copy of file3.txt";
		 * Files.CopyFile(file, dest);
		 * }
		 * </pre>
		 */
		public static boolean CopyFile(String source, String dest){
			return command(DDDriverFileCommands.COPYFILE_KEYWORD, source, dest);
		}
		/**
		 * Copy multiple files/sub-directories, based on matching the provided pattern, from one directory to another.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_CopyMatchingFiles">Detailed Reference</a><p>
		 * @param fromDirectory String, the name of directory from where to find matching files.
		 * @param toDirectory String, the name of directory to where to copy the matching files.
		 * @param pattern String, the pattern used to find matching files.
		 * @param mode PatternFilterMode, the mode of the searching pattern ("regex", "wildcast").
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory";
		 * String toDirectory = "C:\\Users\\sbjlwa\\TestDriverCommand\\Copy Directory";
		 * Files.CopyMatchingFiles(directory, toDirectory, "[a-z ]*f.*", PatternFilterMode.REGEXP);
		 * Files.CopyMatchingFiles(directory, toDirectory, "UTF*.*", PatternFilterMode.WILDCARD);
		 * }
		 * </pre>
		 */
		public static boolean CopyMatchingFiles(String fromDirectory, String toDirectory, String pattern, PatternFilterMode mode){
			return command(DDDriverFileCommands.COPYMATCHINGFILES_KEYWORD, fromDirectory, toDirectory, pattern, mode.name);
		}
		/**
		 * Create a new directory.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_CreateDirectory">Detailed Reference</a><p>
		 * @param directory String, the name of directory to create. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory";
		 * if(Files.CreateDirectory(directory))
		 *   System.out.println(directory+" is created.");
		 * }
		 * </pre>
		 */
		public static boolean CreateDirectory(String directory){
			return command(DDDriverFileCommands.CREATEDIRECTORY_KEYWORD, directory);
		}
		/**
		 * Create and open a new file with the filename, mode and access provided.<br>
		 * <font color="red">Note: After calling this method, DO NOT forget to Close: Files.CloseFile(fileNo);</font>
		 * <br>
		 * <font color="red">Note: For parameter Mode and Access, only a few combinations are permitted, as following:</font>
		 * <br> {@link Access#W}, {@link Mode#OUTPUT} 
		 * <br> {@link Access#W}, {@link Mode#APPEND}
		 * <br> {@link Access#R}, {@link Mode#INPUT}
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_CreateFile">Detailed Reference</a><p>
		 * @param file  String, the name of file to create. 
		 * @param mode Mode, the mode (input, output, append) used to create file
		 * @param access Access, the access (write, read) used to create file
		 * @param fileNoVar String, the variable where stored the file number of created file.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] fileNo</b> String, The file number used to create a file.<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory";
		 * String file = directory+File.separator+"new test file3.txt";
		 * if(Files.CreateFile(file, Mode.OUTPUT, Access.W, result)){
		 *   System.out.println(file+" is created and opened with file number '"+GetVariableValue(result)+"'");
		 *   Files.CloseFile(GetVariableValue(result));
		 * }
		 * }
		 * </pre>
		 */
		public static boolean CreateFile(String file, Mode mode, Access access, String fileNoVar, String... optionals/*fileNo*/){
			return command(DDDriverFileCommands.CREATEFILE_KEYWORD, combineParams(optionals, file, mode.name, access.name, fileNoVar));
		}
		/**
		 * Delete the directory itself, ONLY EMPTY directory can be deleted.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_DeleteDirectory">Detailed Reference</a><p>
		 * @param directory  String, the name of directory to delete. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\Empty Folder";
		 * if(Files.CreateDirectory(directory)){
		 *   if(Files.DeleteDirectory(directory)){
		 *     System.out.println("directory '"+directory+"' has been deleted.");
		 *   }
		 * }
		 * }
		 * </pre>
		 */
		public static boolean DeleteDirectory(String directory){
			return command(DDDriverFileCommands.DELETEDIRECTORY_KEYWORD, directory);
		}
		/**
		 * Delete recursively the contents (files and sub-directories), the directory itself is kept.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_DeleteDirectoryContents">Detailed Reference</a><p>
		 * @param directory  String, the name of directory to delete. 
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\To Delete Folder";
		 * if(Files.CreateDirectory(directory)){
		 *   String newFile = directory+File.separator+"\\test.file";
		 *   Files.CreateFile(newFile, Mode.OUTPUT, Access.W, result);
		 *   Files.CloseFile(GetVariableValue(result));
		 *   if(Files.DeleteDirectory(directory)){
		 *     System.err.println("directory '"+directory+"' is not empty, but it is deleted! NOT possible.");
		 *   }else{
		 *     System.out.println("directory '"+directory+"' is not empty, can not be deleted. OK.");
		 *   }
		 *   if(Files.DeleteDirectoryContents(directory)){
		 *     System.out.println("directory '"+directory+"' its content has been delete.");
		 *   }
		 *   if(Files.DeleteDirectory(directory)){//directory is empty, can be deleted.
		 *     System.out.println("directory '"+directory+"' is deleted.");
		 *   }
		 * }
		 * }
		 * </pre>
		 */
		public static boolean DeleteDirectoryContents(String directoryName){
			return command(DDDriverFileCommands.DELETEDIRECTORYCONTENTS_KEYWORD, directoryName);
		}
		/**
		 * Delete recursively the contents (files and sub-directories) of a provided directory.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_DeleteDirectoryContents">Detailed Reference</a><p>
		 * @param directory  String, the name of directory to delete. 
		 * @param delectDirectory boolean, true delete also the directory itself; otherwise the directory is kept.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\To Delete Folder";
		 * if(Files.CreateDirectory(directory)){
		 *   String newFile = directory+File.separator+"\\test.file";
		 *   Files.CreateFile(newFile, Mode.OUTPUT, Access.W, result);
		 *   Files.CloseFile(GetVariableValue(result));
		 *   if(Files.DeleteDirectoryContents(directory, true)){
		 *     System.out.println("directory '"+directory+"' has been completely delete.");
		 *   }
		 * }
		 * }
		 * </pre>
		 */
		public static boolean DeleteDirectoryContents(String directory, boolean delectDirectory){
			return command(DDDriverFileCommands.DELETEDIRECTORYCONTENTS_KEYWORD, directory, String.valueOf(delectDirectory));
		}
		/**
		 * Delete file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_DeleteFile">Detailed Reference</a><p>
		 * @param file  String, the name of file to delete. 
		 * @param verifyExistence boolean, true verify the existence of the file before deleting.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * if(Files.DeleteFile(file, true))
		 *   System.out.println("file '"+file+"' has been deleted.");
		 * }
		 * </pre>
		 */
		public static boolean DeleteFile(String file, boolean verifyExistence){
			if(verifyExistence){
				return command(DDDriverFileCommands.DELETEFILE_KEYWORD, file);
			}else{
				return command(DDDriverFileCommands.DELETEFILE_KEYWORD, file, FileUtilities.PARAM_NO_VERIYF);
			}
		}
		/**
		 * Filter out specific parts of an image by coordinates and save to an image file.<br>
		 * The filtered area will be covered by black color.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterImage">Detailed Reference</a><p>
		 * @param source  String, the name of source image file. 
		 * @param dest String, the name of the destination image file.
		 * @param subareas List<SubArea>, the subareas used to filter image
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String image = "C:\\Users\\sbjlwa\\TestDriverCommand\\keyword.png";
		 * String subImage = "C:\\Users\\sbjlwa\\TestDriverCommand\\subimage.png";
		 * List<SubArea> subareas = new ArrayList<SubArea>();
		 * subareas.add(new SubArea(0,0,"20%","30%"));
		 * subareas.add(new SubArea("50%", "50%", "60%", "70%"));
		 * subareas.add(new SubArea("80%", "80%", "90%", "90%"));
		 * Files.FilterImage(image, subImage, subareas);//filter 3 areas
		 * }
		 * </pre>
		 */
		public static boolean FilterImage(String source, String dest, List<SubArea> subareas){
			StringBuffer sb = new StringBuffer();
			for(SubArea subarea: subareas) sb.append(subarea.toString()+StringUtils.SPACE);
			return FilterImage(source, dest, ImageFilterMode.COORD, sb.toString().trim());
		}
		/**
		 * Filter out specific parts of an image by coordinates and save to an image file.<br>
		 * The filtered area will be covered by black color.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterImage">Detailed Reference</a><p>
		 * @param source  String, the name of source image file. 
		 * @param dest String, the name of the destination image file.
		 * @param subareas String, the subareas used to filter image, <br>
		 *                         "one subarea" contains 4 values representing TopLeft and BottomRight<br>
		 *                         each value can be number or a percentage-number, separated by , or ;<br>
		 *                         number means absolute coordinate, percentage-number means relative width/height.<br>
		 *                         multiple areas can be provided, separated by space " ", to filter multiple areas.<br>
		 *                         Examples:<br>
		 *                         <b>0;0;35;65</b> 		a set of absolute coordinates<br>
		 *                         <b>0,0,35%,60%</b> 	a set of absolute/relative coordinates<br>
		 *                         <b>0;0;35;65 10%,10%,35%,60%</b> 		2 sets of absolute coordinates<br>
		 *                       
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String image = "C:\\Users\\sbjlwa\\TestDriverCommand\\keyword.png";
		 * String subImage = "C:\\Users\\sbjlwa\\TestDriverCommand\\subimage.png";
		 * String coords = "0,0,70%,50%";//DO NOT put any space between coordinate
		 * Files.FilterImage(image, subImage, coords);//filter 1 area
		 * subImage = "C:\\Users\\sbjlwa\\TestDriverCommand\\subimage2.png";
		 * coords = "0,0,70%,50% 80%;80%;100%;100%";
		 * Files.FilterImage(image, subImage, coords);//filter 2 areas
		 * }
		 * </pre>
		 */
		public static boolean FilterImage(String source, String dest, String subareas){
			return FilterImage(source, dest, ImageFilterMode.COORD, subareas);
		}
		/**
		 * Filter out specific parts of an image and save to an image file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterImage">Detailed Reference</a><p>
		 * @param source  String, the name of source image file. 
		 * @param dest String, the name of the destination image file.
		 * @param mode ImageFilterMode, the mode of filter. For example, if it is {@link ImageFilterMode#COORD}, the filter means the coordinates.
		 * @param filter String, the filter used to filter image.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * }
		 * </pre>
		 */
		static boolean FilterImage(String source, String dest, ImageFilterMode mode, String filter){
			return command(DDDriverFileCommands.FILTERIMAGE_KEYWORD, source, dest, mode.name, filter);
		}
		/**
		 * Filter a text file based on the given parameters.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterTextFile">Detailed Reference</a><p>
		 * @param file  String, the name of file to filter. 
		 * @param regexPattern String, the regular expression used to find matching string
		 * @param replace String, the token used to replace the matching string
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] encoding</b> String, The file encoding<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String regexPattern = "saturday|sat|sunday|sun";
		 * String replace = "weekend";
		 * Files.FilterTextFile(file, regexPattern, replace);
		 * }
		 * </pre>
		 */
		public static boolean FilterTextFile(String file, String regexPattern, String replace, String... optionals/**encoding*/){
			return FilterTextFile(file, PatternFilterMode.REGEXP, regexPattern, replace, true, optionals);
		}
		/**
		 * Filter a text file based on the given parameters.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterTextFile">Detailed Reference</a><p>
		 * @param file  String, the name of file to filter. 
		 * @param regexPattern String, the regular expression used to find matching string
		 * @param replace String, the token used to replace the matching string
		 * @param caseSensitive boolean, if matching is sensitive.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] encoding</b> String, The file encoding<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String regexPattern = "saturday|sat|sunday|sun";
		 * String replace = "weekend";
		 * Files.FilterTextFile(file, regexPattern, replace, false);
		 * }
		 * </pre>
		 */
		public static boolean FilterTextFile(String file, String regexPattern, String replace, boolean caseSensitive, String... optionals/**encoding*/){
			return FilterTextFile(file, PatternFilterMode.REGEXP, regexPattern, replace, caseSensitive, optionals);
		}
		/**
		 * Filter a text file based on the given parameters.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_FilterTextFile">Detailed Reference</a><p>
		 * @param file  String, the name of file to filter. 
		 * @param mode PatternFilterMode, the pattern mode ("regex" or "wildcast")
		 * @param pattern String, the pattern used to find matching string
		 * @param replace String, the token used to replace the matching string
		 * @param caseSensitive boolean, if matching is sensitive.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] encoding</b> String, The file encoding<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * }
		 * </pre>
		 */
		static boolean FilterTextFile(String file, PatternFilterMode mode, String pattern, String replace, boolean caseSensitive, String... optionls/*encoding*/){
			if(caseSensitive){
				return command(DDDriverFileCommands.FILTERTEXTFILE_KEYWORD, combineParams(optionls, file, mode.name, pattern, replace, FileUtilities.PARAM_CASE_SENSITIVE));
			}else{
				return command(DDDriverFileCommands.FILTERTEXTFILE_KEYWORD, combineParams(optionls, file, mode.name, pattern, replace, FileUtilities.PARAM_CASE_INSENSITIVE));
			}
		}
		/**
		 * Get the file's LastModified date time and save it to a variable, the time is in 12 AM PM format.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFileDateTime">Detailed Reference</a><p>
		 * @param file String, The file to get attribute.
		 * @param resultVariable String, The variable to save file date time.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\new test file3.txt";
		 * if(Files.GetFileDateTime(file, result))
		 *   System.out.println("file '"+file+"', LastModified time is "+GetVariableValue(result));
		 * }
		 * </pre>
		 */		
		public static boolean GetFileDateTime(String name, String resultVariable){
			return GetFileDateTime(name, resultVariable, false, DateType.LASTMODIFIED);
		}
		/**
		 * Get the file date time and save it to a variable.<br>
		 * <b>Note: For Operating System other than Windows, only "lastModified" time is supported.</b>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFileDateTime">Detailed Reference</a><p>
		 * @param file String, The file to get attribute. 
		 * @param resultVariable String, The variable to save file date time.
		 * @param isMilitaryFormat boolean, if true, time is in military format (24-hours), otherwise is 12-hours AM PM format.
		 * @param dateType DateType, The date type ("created", "lastModified", "lastAccessed")
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\new test file3.txt";
		 * if(Files.GetFileDateTime(file, result, true, DateType.LASTACCESSED))
		 *   System.out.println("file '"+file+"', LastAccessed military time is "+GetVariableValue(result));
		 * if(Files.GetFileDateTime(file, result, true, DateType.CREATED))
		 *   System.out.println("file '"+file+"', Created military time is "+GetVariableValue(result));
		 * if(Files.GetFileDateTime(file, result, false, DateType.LASTMODIFIED))
		 *   System.out.println("file '"+file+"', LastModified time is "+GetVariableValue(result));
		 * }
		 * </pre>
		 */
		public static boolean GetFileDateTime(String file, String resultVariable, boolean isMilitaryFormat, DateType dateType){
			return command(DDDriverFileCommands.GETFILEDATETIME_KEYWORD, file, resultVariable, Boolean.toString(isMilitaryFormat), dateType.name);
		}
		/**
		 * Save the file attributes for the file name to the variable provided.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFileProtections">Detailed Reference</a><p>
		 * @param file String, The file to get attribute. 
		 * @param resultVariable String, The variable to save file attribute.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * file = "C:\\Users\\sbjlwa\\ntuser.pol";
		 * Files.GetFileProtections(file, result);
		 * int attributes = Integer.parseInt(GetVariableValue(result));
		 * FileAttribute attribute = FileAttribute.instance(attributes);
		 * System.out.println("File Protection: "+attribute.toString());
		 * }
		 * </pre>
		 */
		public static boolean GetFileProtections(String file, String resultVariable){
			return command(DDDriverFileCommands.GETFILEPROTECTIONS_KEYWORD, file, resultVariable);
		}
		/**
		 * Search the directory for files according to file attribute and write the found filenames into an output file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFiles">Detailed Reference</a><p>
		 * @param directory String, the name of directory where to search file. 
		 * @param outputFile String, the name of file to store the found files name.
		 * @param attribute FileAttribute, the file attribute served as search condition.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\";
		 * String directoryToCheck = "C:\\Users\\sbjlwa";
		 * Files.GetFiles(directoryToCheck, directory+"normalList2.txt");
		 * Files.GetFiles(directoryToCheck, directory+"normalList.txt", FileAttribute.instance());
		 * Files.GetFiles(directoryToCheck, directory+"archiveList.txt", new FileAttribute(Type.ARCHIVEFILE));
		 * Files.GetFiles(directoryToCheck, directory+"hiddenList.txt", new FileAttribute(Type.HIDDENFILE));
		 * Files.GetFiles(directoryToCheck, directory+"archiveAndHiddenList.txt", new FileAttribute(Type.HIDDENFILE).add(Type.ARCHIVEFILE));
		 * }
		 * </pre>
		 */		
		public static boolean GetFiles(String directory, String outputFile, FileAttribute attribute){			
			return command(DDDriverFileCommands.GETFILES_KEYWORD, directory, outputFile, attribute.getStringValue());
		}
		/**
		 * Search the directory for normal files and write the found filenames into an output file.<br>
		 * It is equivalent to call {@link #GetFiles(String, String, FileAttribute.instance())}<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFiles">Detailed Reference</a><p>
		 * @param directory String, the name of directory where to search file. 
		 * @param outputFile String, the name of file to store the found files name.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String directory = "C:\\Users\\sbjlwa\\TestDriverCommand\\";
		 * String directoryToCheck = "C:\\Users\\sbjlwa";
		 * Files.GetFiles(directoryToCheck, directory+"normalList2.txt");
		 * }
		 * </pre>
		 */		
		public static boolean GetFiles(String directory, String outputFile){			
			return command(DDDriverFileCommands.GETFILES_KEYWORD, directory, outputFile);
		}
		
		/**
		 * Determine the file size.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetFileSize">Detailed Reference</a><p>
		 * @param file String, the name of file to get size. 
		 * @param resultVariable String, The variable to store the size value.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * file = "C:\\Users\\sbjlwa\\ntuser.pol";
		 * if(Files.GetFileSize(file, result))
		 *   System.out.println("file '"+file+"', size is "+GetVariableValue(result));
		 * }
		 * </pre>
		 */
		public static boolean GetFileSize(String file, String resultVariable){
			return command(DDDriverFileCommands.GETFILESIZE_KEYWORD, file, resultVariable);
		}
		/**
		 * Get value from INI file. INI file has format as following:<br>
		 * [Section1]<br>
		 * Item1="value_A"<br>
		 * Item2="value_B"<br>
		 * Item3="value_C"<br>
		 * <br>
		 * [Section2]<br>
		 * Item1="value_D"<br>
		 * Item2="value_E"<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetINIFileValue">Detailed Reference</a><p>
		 * @param section String, the name of section in INI file. 
		 * @param item String, the name of item in INI file.
		 * @param resultVariable String, The variable to store the item value.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "D:\\test.ini";//Suppose it contains the content as above example
		 * String result = "result";
		 * 
		 * String section = "Section1";
		 * String item = "Item1";
		 * if(Files.GetINIFileValue(file, section, item, result))
		 *   System.out.println("ini file '"+file+"' '"+section+":"+item+"'="+GetVariableValue(result));//"value_A"
		 *   
		 * section = "Section2";
		 * if(Files.GetINIFileValue(file, section, item, result))
		 *   System.out.println("ini file '"+file+"' '"+section+":"+item+"'="+GetVariableValue(result));//"value_D"
		 * }
		 * </pre>
		 */
		public static boolean GetINIFileValue(String file, String section, String item, String resultVariable){
			return command(DDDriverFileCommands.GETINIFILEVALUE_KEYWORD, file, section, item, resultVariable);
		}

		/**
		 * Count the number of occurrence of a token in a file and store that number in a variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetStringCountInFile">Detailed Reference</a><p>
		 * @param file  String, the file in which the token will be searched. 
		 * @param token String, the token to search
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] variable</b> String, The variable to hold the result. The default variable is "GetStringCountInFile".<br>
		 * <b>optionals[1] caseSensitive</b> boolean, If the comparison is case sensitive. The default is true.<br>
		 * <b>optionals[2] encoding</b> String, the encoding used to open file. The default is system encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String file = "fileName.txt";
		 * String token = "aBc";
		 * if(Files.GetStringCountInFile(file, token, var))
		 *   //number of occurrence of "aBc" in file "fileName.txt"
		 *   System.out.println("GetStringCountInFile success: result is "+GetVariableValue(var));
		 * if(Files.GetStringCountInFile(file, token, var, "false"))
		 *   //number of occurrence of "aBc"/"abc" in file "fileName.txt"
		 *   System.out.println("GetStringCountInFile success: result is "+GetVariableValue(var));
		 * }
		 * </pre>
		 */
		public static boolean GetStringCountInFile(String file, String token, String... optionals){
			return command(DDDriverFileCommands.GETSTRINGCOUNTINFILE_KEYWORD, combineParams(optionals, file, token));
		}
		/**
		 * Extract dynamic substrings from a file using regular expressions.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetSubstringsInFile">Detailed Reference</a><p>
		 * @param file String, the name of file to find string. 
		 * @param regexStart String, The starting regular expression. Should not be empty.
		 * @param regexStop String, The stopping regular expression. Should not be empty.
		 * @param resultVarRoot String, The variable root name to contain the found string and count.<br>
		 * For example, if it is "Method"<br>
		 * "Method1" will be the first found string.<br>
		 * "Method2" will be the second found string.<br>
		 * "MethodCount" will be the total number of found strings.<br>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * //List all methods name appeared in the file
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String regexStart = "\\.";
		 * String regexStop = "\\(";
		 * String result = "Method";
		 * String counter = result+"Count";
		 * if(Files.GetSubstringsInFile(file, regexStart, regexStop, result)){
		 *   int count = Integer.parseInt(GetVariableValue(counter));
		 *   for(int i=0;i&lt;count;i++)
		 *     System.out.println(GetVariableValue(result+(i+1)));
		 * }
		 * }
		 * </pre>
		 */	
		public static boolean GetSubstringsInFile(String file, String regexStart, String regexStop, String resultVarRoot){
			return command(DDDriverFileCommands.GETSUBSTRINGSINFILE_KEYWORD, file, regexStart, regexStop, resultVarRoot);
		}
		/**
		 * Incorporate OCR technology to detect the text in an image file and save the text to a variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_GetTextFromImage">Detailed Reference</a><p>
		 * @param imageFileName String, the image file containing text to detect. 
		 * @param resultVar String, the variable to store the detected text.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] ocrID</b> String, The ID of OCR, it can be "TOCR" or "GOCR"<br>
		 * <b>optionals[1] landID</b> String, The ID of language, it can be "en", "cn", "fr" etc.<br>
		 * <b>optionals[2] scaleRation</b> float, the scale ratio for resizing the original image<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String image = "C:\\Users\\sbjlwa\\TestDriverCommand\\keyword.png";
		 * if(Files.GetTextFromImage(image, result, OCREngine.OCR_T_ENGINE_KEY, Locale.ENGLISH.getLanguage(), "3.0"))
		 *   pass("image text (translated by TOCR) is '"+GetVariableValue(result)+"'");
		 * if(Files.GetTextFromImage(image, result, OCREngine.OCR_G_ENGINE_KEY, Locale.ENGLISH.getLanguage(), "2.0"))
		 *   pass("image text (translated by GOCR) is '"+GetVariableValue(result)+"'");
		 * }
		 * </pre>
		 */		
		public static boolean GetTextFromImage(String imageFileName, String resultVar, String... optionals){
			return command(DDDriverFileCommands.GETTEXTFROMIMAGE_KEYWORD, combineParams(optionals, imageFileName, resultVar));
		}
		/**
		 * If the specified directory exists, then execute the following driver command.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_IfExistDir">Detailed Reference</a><p>
		 * @param directory String, the name of directory to test the existence.
		 * @param fileCommandAndParams
		 * <br>fileCommandAndParams[0] driverComand String, The file driver-command to execute.
		 * <br>fileCommandAndParams[1] param1 String, The first parameter of driver-command.
		 * <br>fileCommandAndParams[2] param1 String, The second parameter of driver-command.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String toDirectory = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory";
		 * String newdir = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\NewDirectory";
		 * String fileDriverCommand = "CreateDirectory";
		 * if(Files.IfExistDir(directory, fileDriverCommand, newdir))
		 *   pass("directory '"+directory+"' DOES exist, and '"+fileDriverCommand+"' has executed.");
		 * }
		 * </pre>
		 */			
		public static boolean IfExistDir(String directory, String... fileCommandAndParams){
			return command(DDDriverFileCommands.IFEXISTDIR_KEYWORD, combineParams(fileCommandAndParams, directory));
		}
		/**
		 * If the specified file exists, then execute the following driver command.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_IfExistFile">Detailed Reference</a><p>
		 * @param file String, the name of file to test the existence.
		 * @param fileCommandAndParams
		 * <br>fileCommandAndParams[0] driverComand String, The file driver-command to execute.
		 * <br>fileCommandAndParams[1] param1 String, The first parameter of driver-command.
		 * <br>fileCommandAndParams[2] param1 String, The second parameter of driver-command.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\new test file3.txt";
		 * String destination = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\second copy of file3.txt";
		 * String fileDriverCommand = "CopyFile";
		 * if(Files.IfExistFile(file, fileDriverCommand, file, destination))
		 *   pass("file '"+file+"' DOES exist and it has been copied to '"+destination+"'");
		 * }
		 * </pre>
		 */	
		public static boolean IfExistFile(String file, String... fileCommandAndParams){
			return command(DDDriverFileCommands.IFEXISTFILE_KEYWORD, combineParams(fileCommandAndParams, file));
		}
		/**
		 * Test if the opened file is at the end and store the result to a variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_IsEndOfFile">Detailed Reference</a><p>
		 * @param fileNo String, the file number of the opened file.
		 * @param resultVar String, the variable to store the result.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\new test file3.txt";
		 * String fileNo = null;
		 * if(Files.OpenFile(file, Mode.INPUT, Access.R, result)){
		 *   fileNo = GetVariableValue(result);
		 *   String line = null;
		 *   while(true){
		 *     Files.IsEndOfFile(fileNo, result);
		 *     if(Boolean.parseBoolean(GetVariableValue(result))) break;
		 *     Files.ReadFileLine(fileNo, result);
		 *     line = GetVariableValue(result);//do some thing with the line.
		 *   }
		 *   if(Files.CloseFile(fileNo))
		 *     System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean IsEndOfFile(String fileNo, String resultVar){
			return command(DDDriverFileCommands.ISENDOFFILE_KEYWORD, fileNo, resultVar);
		}
		/**
		 * Open an file with the filename, mode and access provided.<br>
		 * <font color="red">Note: After calling this method, DO NOT forget to Close: Files.CloseFile(fileNo);</font>
		 * <br>
		 * <font color="red">Note: For parameter Mode and Access, only a few combinations are permitted, as following:</font>
		 * <br> {@link Access#W}, {@link Mode#OUTPUT} 
		 * <br> {@link Access#W}, {@link Mode#APPEND}
		 * <br> {@link Access#R}, {@link Mode#INPUT}
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_OpenFile">Detailed Reference</a><p>
		 * @param file String, the name of file to open. 
		 * @param mode Mode, the mode (input, output, append) used to open file
		 * @param access Access, the access (write, read) used to open file
		 * @param fileNoVar String, the variable where stored the file number of opened file.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] fileNo</b> String, The file number used to open a file.<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\new test file3.txt";
		 * if(Files.OpenFile(file, Mode.INPUT, Access.R, result))
		 *   System.out.println(file+" has been opened with file number '"+GetVariableValue(result)+"' for input.");
		 * }
		 * </pre>
		 */		
		public static boolean OpenFile(String file, Mode mode, Access access, String fileNoVar, String... fileNo){
			return command(DDDriverFileCommands.OPENFILE_KEYWORD, combineParams(fileNo, file, mode.name, access.name, fileNoVar));
		}
		/**
		 * Open an UTF-8 file with the filename, mode and access provided.<br>
		 * <font color="red">Note: After calling this method, DO NOT forget to Close: Files.CloseFile(fileNo);</font>
		 * <br>
		 * <font color="red">Note: For parameter Mode and Access, only a few combinations are permitted, as following:</font>
		 * <br> {@link Access#W}, {@link Mode#OUTPUT} 
		 * <br> {@link Access#W}, {@link Mode#APPEND}
		 * <br> {@link Access#R}, {@link Mode#INPUT}
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_OpenUTF8File">Detailed Reference</a><p>
		 * @param file String, the name of file to open. 
		 * @param mode Mode, the mode (input, output, append) used to open file
		 * @param access Access, the access (write, read) used to open file
		 * @param fileNoVar String, the variable where stored the file number of opened file.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] fileNo</b> String, The file number used to open a file.<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\UTF8 FILE.txt";
		 * if(Files.OpenUTF8File(file, Mode.OUTPUT, Access.W, result))
		 *   System.out.println(file+" is opened with file number '"+GetVariableValue(result)+"' for output UTF8 strings.");
		 * }
		 * </pre>
		 */	
		public static boolean OpenUTF8File(String file, Mode mode, Access access, String fileNoVar, String... optionals){
			return command(DDDriverFileCommands.OPENUTF8FILE_KEYWORD, combineParams(optionals, file, mode.name, access.name, fileNoVar));
		}
		/**
		 * Write a string directly to a opened file defined by file number.<br>
		 * It is equivalent to call {@link #PrintToFile(String, String, Placement.NEWLINE)}<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_PrintToFile">Detailed Reference</a><p>
		 * @param fileNo String, The file number of the file to write.
		 * @param content Strung, The string to write.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String fileNo = null;//hold the file number for an opened file.
		 * String content = "Print Output Placement Parameter determines where the next output to the same file should begin.";
		 * String separator = "=================================================";
		 * if(Files.CreateFile(file, Mode.OUTPUT, Access.W, result)){
		 *   fileNo = GetVariableValue(result);
		 *   System.out.println(file+" is created and opened with file number '"+fileNo+"' for output.");
		 *   
		 *   if(Files.PrintToFile(fileNo, separator)){
		 *     System.out.println("'"+separator+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, content)){
		 *     System.out.println("'"+content+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.CloseFile(fileNo)) System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean PrintToFile(String fileNo, String content){
			return command(DDDriverFileCommands.PRINTTOFILE_KEYWORD, fileNo, content);
		}
		/**
		 * Write a string to a opened file defined by file number.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_PrintToFile">Detailed Reference</a><p>
		 * @param fileNo String, The file number of the file to write.
		 * @param content Strung, The string to write.
		 * @param placement Placement, where to write the content. It can be one of
		 * <ul> 
		 * <li>{@link Placement#NEWLINE}		Write the string to a new line
		 * <li>{@link Placement#TABULATION}		Write the string to the next print area
		 * <li>{@link Placement#IMMIDIATE}		Write the string directly
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String fileNo = null;//hold the file number for an opened file.
		 * String content = "Print Output Placement Parameter determines where the next output to the same file should begin.";
		 * String separator = "=================================================";
		 * if(Files.CreateFile(file, Mode.OUTPUT, Access.W, result)){
		 *   fileNo = GetVariableValue(result);
		 *   System.out.println(file+" is created and opened with file number '"+fileNo+"' for output.");
		 *   
		 *   if(Files.PrintToFile(fileNo, separator, Placement.NEWLINE)){
		 *     System.out.println("'"+separator+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, content, Placement.NEWLINE)){
		 *     System.out.println("'"+content+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, separator)){
		 *     System.out.println("'"+separator+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, content, Placement.IMMIDIATE)){
		 *     System.out.println("'"+content+"' has been written to file '"+fileNo+"' in same line immediately.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, separator)){
		 *     System.out.println("'"+separator+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, content, Placement.TABULATION)){
		 *     System.out.println("'"+content+"' has been written to file '"+fileNo+"' in same line at the next print zone.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, separator)){
		 *     System.out.println("'"+separator+"' has been written to file '"+fileNo+"' in a new line.");
		 *   }
		 *   if(Files.PrintToFile(fileNo, content)){
		 *     System.out.println("'"+content+"' has been written to file '"+fileNo+"' in new line.");
		 *   }
		 *   if(Files.CloseFile(fileNo)) System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean PrintToFile(String fileNo, String content, Placement placement){
			return command(DDDriverFileCommands.PRINTTOFILE_KEYWORD, fileNo, content, placement.name);
		}
		/**
		 * Read the number of characters from the file defined by file number and assign them to a variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_ReadFileChars">Detailed Reference</a><p>
		 * @param fileNo String, The file number of the file to be read from.
		 * @param charsToRead int, The number of characters to be read.
		 * @param resultVar String, the variable to store the characters read from the file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String fileNo = null;//hold the file number for an opened file.
		 * String content = null;
		 * if(Files.OpenFile(file, Mode.INPUT, Access.R, result)){
		 *   fileNo = GetVariableValue(result);
		 *   System.out.println(file+" has been opened with file number '"+fileNo+"' for input.");
		 *   
		 *   int charsToRead = 15;
		 *   if(Files.ReadFileChars(fileNo, charsToRead, result)){
		 *     System.out.println(charsToRead+" characters: '"+GetVariableValue(result)+"' have been read from file '"+fileNo+"'");
		 *   }
		 *   if(Files.ReadFileLine(fileNo, result)){
		 *     System.out.println(" line '"+GetVariableValue(result)+"' have been read from file '"+fileNo+"'");
		 *   }
		 *   if(Files.CloseFile(fileNo))
		 *   System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean ReadFileChars(String fileNo, int charsToRead, String resultVar){
			return command(DDDriverFileCommands.READFILECHARS_KEYWORD, fileNo, String.valueOf(charsToRead), resultVar);
		}
		/**
		 * Read a line from the file defined by file number and assign it to a variable.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_ReadFileLine">Detailed Reference</a><p>
		 * @param fileNo String, The file number of the file to be read from. 
		 * @param resultVar String, the variable to store the line read from the file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * refer to {@link #ReadFileChars(String, int, String)}
		 */
		public static boolean ReadFileLine(String fileNo, String resultVar){
			return command(DDDriverFileCommands.READFILELINE_KEYWORD, fileNo, resultVar);
		}
		/**
		 * Rename the file from the old file name to the new filename.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_RenameFile">Detailed Reference</a><p>
		 * @param oldName String, the filename to be renamed. 
		 * @param newName String, the new name.
		 * @param verifyExistence boolean, true verify the existence of the file before renaming.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String newfile = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\renamed newFile.txt";
		 * Files.RenameFile(file, newfile, true);
		 * }
		 * </pre>
		 */
		public static boolean RenameFile(String oldName, String newName, boolean verifyExistence){
			if(verifyExistence){
				return command(DDDriverFileCommands.RENAMEFILE_KEYWORD, oldName, newName);
			}else{
				return command(DDDriverFileCommands.RENAMEFILE_KEYWORD, oldName, newName, FileUtilities.PARAM_NO_VERIYF);
			}
		}
		
		/**
		 * Incorporate OCR technology to detect the text in an image file and save the text to a text file.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_SaveTextFromImage">Detailed Reference</a><p>
		 * @param imageFileName String, the image file containing text to detect. 
		 * @param resultFile String, the file to store the detected text.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] ocrID</b> String, The ID of OCR, it can be "TOCR" or "GOCR"<br>
		 * <b>optionals[1] landID</b> String, The ID of language, it can be "en", "cn", "fr" etc.<br>
		 * <b>optionals[2] scaleRation</b> float, the scale ratio for resizing the original image<br>
		 * </ul>
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String image = "C:\\Users\\sbjlwa\\TestDriverCommand\\keyword.png";
		 * String textFile = "C:\\Users\\sbjlwa\\TestDriverCommand\\image_T.txt";
		 * Files.SaveTextFromImage(image, textFile, OCREngine.OCR_T_ENGINE_KEY, Locale.ENGLISH.getLanguage(), "2.0");
		 * textFile = "C:\\Users\\sbjlwa\\TestDriverCommand\\image_G.txt";
		 * Files.SaveTextFromImage(image, textFile, OCREngine.OCR_G_ENGINE_KEY, Locale.ENGLISH.getLanguage(), "2.5");
		 * }
		 * </pre>
		 */
		public static boolean SaveTextFromImage(String imageFileName, String resultFile, String... optionals){
			return command(DDDriverFileCommands.SAVETEXTFROMIMAGE_KEYWORD, combineParams(optionals, imageFileName, resultFile));
		}
		
		/**
		 * Change the file attribute to the value of the new file protection provided.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_SetFileProtections">Detailed Reference</a><p>
		 * @param file String, The file to modify attribute. 
		 * @param attribute FileAttribute, The attribute to set to file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String file = "C:\\Users\\sbjlwa\\New Text Document.txt";
		 * FileAttribute attribute = FileAttribute.instance(FileAttribute.Type.SYSTEMFILE);
		 * attribute.add(FileAttribute.Type.READONLYFILE);
		 * attribute.add(FileAttribute.Type.ARCHIVEFILE);
		 * Files.SetFileProtections(file, attribute);
		 * }
		 * </pre>
		 */
		public static boolean SetFileProtections(String file, FileAttribute attribute){
			return command(DDDriverFileCommands.SETFILEPROTECTIONS_KEYWORD, file, attribute.getStringValue());
		}
		/**
		 * Write the specified number of characters to a file already opened for writing.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverFileCommandsReference.htm#detail_WriteFileChars">Detailed Reference</a><p>
		 * @param fileNo String, The file number/identifier of the file to be written to. 
		 * @param charsToWrite int, The number of characters to write to the file; A negative number means all chars.
		 * @param content String, The content to write to file.
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String result = "result";
		 * String file = "C:\\Users\\sbjlwa\\TestDriverCommand\\New Directory\\newFile.txt";
		 * String fileNo = null;//hold the file number for an opened file.
		 * String content = "Print Output Placement Parameter determines where the next output to the same file should begin.";
		 * int charsToWrite = 10;
		 * if(Files.CreateFile(file, Mode.OUTPUT, Access.W, result)){
		 *   fileNo = GetVariableValue(result);
		 *   System.out.println(file+" is created and opened with file number '"+fileNo+"' for output.");
		 *   
		 *   if(Files.WriteFileChars(fileNo, charsToWrite, content))
		 *     System.out.println(charsToWrite+" characters of string '"+content+"' has been written to file '"+fileNo+"'");
		 *
		 *   if(Files.CloseFile(fileNo)) System.out.println("file '"+fileNo+"' has been closed.");
		 * }
		 * }
		 * </pre>
		 */
		public static boolean WriteFileChars(String fileNo, int charsToWrite, String content){
			return command(DDDriverFileCommands.WRITEFILECHARS_KEYWORD, fileNo, String.valueOf(charsToWrite), content);
		}
		
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/DDDriverStringCommandsIndex.htm">String keywords</a>, like Compare, GetMultiDelimitedField etc.<br>
	 * <pre>
	 * If you meet some errors when calling these API, please try to run 
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Strings.xxx();
	 * </pre>
	 */
	public static class Strings{
		/**
		 * For each char in string: if ((char .gt. 31) and (char .lt. 127)) keep it,
		 * otherwise turn it into a space.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_CleanString">Detailed Reference</a><p>
		 * @param source  String, (could come from a ^variable). 
		 * @param resultVar String, the variable to hold the result of the operation
		 * @return true if successful, false otherwise.<p>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode
		 * @see org.safs.TestRecordHelper#getStatusInfo
		 * <p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String controlA = "\u0001";
		 * String content = "ab"+controlA+"cd\n";
		 * if(Strings.CleanString(content, var))
		 *   System.out.println("CleanString '"+content+"' success: result is "+GetVariableValue(var));//expected result 'abcd '
		 * }
		 * </pre>
		 */
		public static boolean CleanString(String source, String resultVar){
			return command(DDDriverStringCommands.CLEANSTRING_KEYWORD, quote(source), resultVar);
		}
		/**
		 * Compares two strings.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Compare">Detailed Reference</a><p>
		 * @param source String, string to compare
		 * @param destination String, string to compare
		 * @param resultVar String, the variable to hold the result of the operation
		 * @return true if successful, false otherwise.<p>
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "hello";
		 * String destination = "Hello";
		 * if(Strings.Compare(source, destination, var))
		 *   System.out.println("Compare success: result is "+GetVariableValue(var));//expected result 'false'
		 * }
		 * </pre>
		 */
		public static boolean Compare(String source, String destination, String resultVar){
			return command(DDDriverStringCommands.COMPARE_KEYWORD, quote(source), quote(destination), resultVar);
		}
		/**
		 * Compares a string with a normal string or a regular expression.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Compare">Detailed Reference</a><p>
		 * @param source String, string to compare
		 * @param destination String, normal string to compare or the regex string to match
		 * @param resultVar String, the variable to hold the result of the operation
		 * @param regexMatch boolean, if the destination string is a regex string to match.
		 * @return true if successful, false otherwise.<p>
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "hello";
		 * String destination = "^[h|H]ello?$";
		 * if(Strings.Compare(source, destination, var))
		 *   System.out.println("Compare success: result is "+GetVariableValue(var));//expected result 'true'
		 * 
		 * source = "hell";
		 * if(Strings.Compare(source, destination, var))
		 *   System.out.println("Compare success: result is "+GetVariableValue(var));//expected result 'true'
		 * 
		 * //without leading ^ and ending $, the regex will match the substring of source string.
		 * destination = "[h|H]ello?";
		 * source = "Hi, hello Matt.";//sub-string "hello" will match the regex
		 * if(Strings.Compare(source, destination, var))
		 *   System.out.println("Compare success: result is "+GetVariableValue(var));//expected result 'true'
		 * }
		 * </pre>
		 */
		public static boolean Compare(String source, String destination, String resultVar, boolean regexMatch){
			return command(DDDriverStringCommands.COMPARE_KEYWORD, quote(source), quote(destination), resultVar, String.valueOf(regexMatch));
		}
		/**
		 * Concatenate String1 with String2 and returns concatenated string.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Concatenate">Detailed Reference</a><p>
		 * @param str1 String, string to concatenate
		 * @param str2 String, string to concatenate
		 * @param resultVar String, the variable to hold the result of the operation
		 * @return true if successful, false otherwise.<p>
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String str1 = "hello";
		 * String str2 = " world";
		 * if(Strings.Concatenate(str1, str2, var))
		 *   System.out.println("Concatenate success: result is "+GetVariableValue(var));//expected result 'hello world'
		 * }
		 * </pre>
		 */
		public static boolean Concatenate(String str1, String str2, String resultVar){
			return command(DDDriverStringCommands.CONCATENATE_KEYWORD, quote(str1), quote(str2), resultVar);
		}

		/**
		 * Get a field out of a string using specified delimiter(s).
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetField">Detailed Reference</a><p>
		 * @param source String, The input string which contains the field to be returned
		 * @param index int, 0-based index
		 * @param delimiters String, one or more single characters used as delimiters 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a|bc|d";
		 * int index = 1;
		 * String delimiter = "|";
		 * if(Strings.GetField(source, index, delimiter, var))
		 *   System.out.println("GetField success: result is "+GetVariableValue(var));//expected result'bc'
		 * }
		 * </pre>
		 */
		public static boolean GetField(String source, int index, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETFIELD_KEYWORD, quote(source), String.valueOf(index), quote(delimiters), resultVar);
		}

		/**
		 * Finds the count of all fields within the string found from startindex to the end of the string. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetFieldCount">Detailed Reference</a><p>
		 * @param source String, The input string to parse and count fields
		 * @param index int, 0-based index, start-index for parsing the string
		 * @param delimiters String, one or more single characters used as delimiters 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a|bc|d";
		 * int index = 0;
		 * String delimiter = "|";
		 * if(Strings.GetFieldCount(source, index, delimiter, var))
		 *   System.out.println("GetFieldCount success: result is "+GetVariableValue(var));//expected result '3'
		 * }
		 * </pre>
		 */
		public static boolean GetFieldCount(String source, int index, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETFIELDCOUNT_KEYWORD, quote(source), String.valueOf(index), quote(delimiters), resultVar);
		}

		/**
		 * Given an Input of fixed-width fields, return the nth(FieldID) Field in the record. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetFixedWidthField">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param fieldID int, 0-based field to retrieve
		 * @param fixedWidth int, the fixed width allotted for each field in the record 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abcdef";
		 * int fieldID = 1;
		 * int fixedWidth = 2;
		 * if(Strings.GetFixedWidthField(source, fieldID, fixedWidth, var))
		 *   System.out.println("GetFixedWidthField success: result is "+GetVariableValue(var));//expected result 'cd'
		 * }
		 * </pre>
		 */
		public static boolean GetFixedWidthField(String source, int fieldID, int fixedWidth, String resultVar){
			return command(DDDriverStringCommands.GETFIXEDWIDTHFIELD_KEYWORD, quote(source), 
					String.valueOf(fieldID), String.valueOf(fixedWidth), resultVar);
		}

		/**
		 * Given a sourceString of delimited fields, return the nth(FieldID) Field in the record from startIndex. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetMultiDelimitedField">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param fieldID int, 1-based field to retrieve
		 * @param startIndex int, 1-based start position for search in sourceString
		 * @param delimiters String, one or more single characters used as delimiters
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a->b->c->d->e->f";
		 * int fieldID = 2;
		 * int startIndex = 5;
		 * String delimiters = "->";
		 * if(Strings.GetMultiDelimitedField(source, fieldID, startIndex, delimiters, var))
		 *   System.out.println("GetMultiDelimitedField success: result is "+GetVariableValue(var));//expected result 'c'
		 * }
		 * </pre>
		 */
		public static boolean GetMultiDelimitedField(String source, int fieldID, int startIndex, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETMULTIDELIMITEDFIELD_KEYWORD, quote(source), 
					String.valueOf(fieldID), String.valueOf(startIndex), quote(delimiters), resultVar);
		}

		/**
		 * Finds the count of all fields within the input string found from start-index to the end of the inputRecord.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetMultiDelimitedFieldCount">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param startIndex int, 1-based start position for search in sourceString
		 * @param delimiters String, 1-based start position for search in sourceString
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a->b->c->d->e->f";
		 * int startIndex = 1;
		 * String delimiters = "->";
		 * if(Strings.GetMultiDelimitedFieldCount(source, startIndex, delimiters, var))
		 *   System.out.println("GetMultiDelimitedFieldCount success: result is "+GetVariableValue(var));//expected result '6'
		 * }
		 * </pre>
		 */
		public static boolean GetMultiDelimitedFieldCount(String source, int startIndex, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETMULTIDELIMITEDFIELDCOUNT_KEYWORD, quote(source), 
					String.valueOf(startIndex), quote(delimiters), resultVar);
		}

		/**
		 * Finds the index of the first character matching one of the provided delimiter characters. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetNextDelimiterIndex">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param startIndex int, 0-based start-index to begin parsing the string. 
		 * @param delimiters String, each character is treated as a separate delimiter 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a/|/";
		 * int startIndex = 0;
		 * String delimiters = "/|";
		 * if(Strings.GetNextDelimiterIndex(source, startIndex, delimiters, var))
		 *   System.out.println("GetNextDelimiterIndex success: result is "+GetVariableValue(var));//expected result '1'
		 * }
		 * </pre>
		 */
		public static boolean GetNextDelimiterIndex(String source, int startIndex, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETNEXTDELIMITERINDEX_KEYWORD, quote(source), 
					String.valueOf(startIndex), quote(delimiters), resultVar);
		}
		
		/**
		 * Returns the requested field contained in the input string using the passed in regular expression as the delimiter(s).
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetREDelimitedField">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param index int, 1-based index of the field to return from the input string. 
		 * @param regexDelimiters String, regular expression used as the delimiter(s). 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a|b&cd-efghi";
		 * int index = 3;
		 * String delimiters = "[\|&\-g]";
		 * if(Strings.GetREDelimitedField(source, index, delimiters, var))
		 *   System.out.println("GetREDelimitedField success: result is "+GetVariableValue(var));//expected result 'cd'
		 * }
		 * </pre>
		 */
		public static boolean GetREDelimitedField(String source, int index, String regexDelimiters, String resultVar){
			return command(DDDriverStringCommands.GETREDELIMITEDFIELD_KEYWORD, quote(source), 
					String.valueOf(index), quote(regexDelimiters), resultVar);
		}
		
		/**
		 * Get the number of fields contained in the input string using the passed in regular expression as the delimiter(s).  
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetREDelimitedFieldCount">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param startIndex int, 0-based index of where to start the analysis from.
		 * @param regexDelimiters String, regular expression used as the delimiter(s). 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a|b&cd-efghi";
		 * int startIndex = 0;
		 * String delimiters = "[\|&\-g]";
		 * if(Strings.GetREDelimitedFieldCount(source, startIndex, delimiters, var))
		 *   System.out.println("GetREDelimitedFieldCount success: result is "+GetVariableValue(var));//expected result '5'
		 * }
		 * </pre>
		 */
		public static boolean GetREDelimitedFieldCount(String source, int startIndex, String regexDelimiters, String resultVar){
			return command(DDDriverStringCommands.GETREDELIMITEDFIELDCOUNT_KEYWORD, quote(source), 
					String.valueOf(startIndex), quote(regexDelimiters), resultVar);
		}
		
		/**
		 * Extract dynamic substring from a string using regular expressions.   
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetSubstringsInString">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param regexStart String, The starting regular expression. Should not be empty. 
		 * @param regexStop String, The stopping regular expression. Should not be empty. 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "XaaaB";
		 * String start = "X";
		 * String end = "B";
		 * if(Strings.GetSubstringsInString(source, start, end, var))
		 *   System.out.println("GetSubstringsInString success: result is "+GetVariableValue(var));//expected result 'aaa'
		 * }
		 * </pre>
		 */
		public static boolean GetSubstringsInString(String source, String regexStart, String regexStop, String resultVar){
			return command(DDDriverStringCommands.GETSUBSTRINGINSTRING_KEYWORD, quote(source), 
					       quote(regexStart), quote(regexStop), resultVar);
		}
		
		/**
		 * Get a system environment variable value.  
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetSystemEnviron">Detailed Reference</a><p>
		 * @param systemVariable String, The system variable name.
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String systemVariable = "OS";
		 * if(Strings.GetSystemEnviron(systemVariable, var))
		 *   System.out.println("GetSystemEnviron success: result is "+GetVariableValue(var));//expected result, something like 'Windows_NT'
		 * }
		 * </pre>
		 */
		public static boolean GetSystemEnviron(String systemVariable, String resultVar){
			return command(DDDriverStringCommands.GETSYSTEMENVIRON_KEYWORD, quote(systemVariable), resultVar);
		}
		
		/**
		 * Get the USERID of the currently logged on user as stored in System Environment variables.  
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetSystemUser">Detailed Reference</a><p>
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * if(Strings.GetSystemUser(var))
		 *   System.out.println("GetSystemUser success: result is "+GetVariableValue(var));//expected result 'logged-in user'
		 * }
		 * </pre>
		 */
		public static boolean GetSystemUser(String resultVar){
			return command(DDDriverStringCommands.GETSYSTEMUSER_KEYWORD, resultVar);
		}

		/**
		 * Get a trimmed field out of a string using specified delimiter(s). 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_GetTrimmedField">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param index int, 0-based index of which field to grab .
		 * @param delimiters String, each character is treated as a separate delimiter. 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "a|bc|d";
		 * int index = 1;
		 * String delimiters = "|";
		 * if(Strings.GetTrimmedField(source, index, delimiters, var))
		 *   System.out.println("GetTrimmedField success: result is "+GetVariableValue(var));//expected result 'bc'
		 * }
		 * </pre>
		 */
		public static boolean GetTrimmedField(String source, int index, String delimiters, String resultVar){
			return command(DDDriverStringCommands.GETTRIMMEDFIELD_KEYWORD, quote(source), String.valueOf(index), quote(delimiters), resultVar);
		}
		
		/**
		 * Returns the position of the first occurrence of one string within another string. -1 if not found at all.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Index">Detailed Reference</a><p>
		 * @param startIndex int, 0-based starting offset of the sourceString to search.
		 * @param source String, The input string to parse
		 * @param findString String, the string to find. 
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * int index = 0;
		 * String findString = "bc";
		 * if(Strings.Index(index, source, findString, var))
		 *   System.out.println("Index success: result is "+GetVariableValue(var));//expected result '1'
		 * }
		 * </pre>
		 */
		public static boolean Index(int startIndex, String source, String findString, String resultVar){
			return command(DDDriverStringCommands.INDEX_KEYWORD, String.valueOf(startIndex), quote(source), quote(findString), resultVar);
		}
		
		/**
		 * Returns a string of a specified number of characters copied from the beginning of another string. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Left">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param length String, number of chars to copy
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * int length = 1;
		 * if(Strings.Left(source, length, var))
		 *   System.out.println("Left success: result is "+GetVariableValue(var));//expected result 'a'
		 * }
		 * </pre>
		 */
		public static boolean Left(String source, int length, String resultVar){
			return command(DDDriverStringCommands.LEFT_KEYWORD, quote(source), String.valueOf(length), resultVar);
		}
		
		/**
		 *  A new string trimmed of leading tabs and spaces. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_LeftTrim">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "   abc   ";
		 * if(Strings.LeftTrim(source, var))
		 *   System.out.println("LeftTrim success: result is "+GetVariableValue(var));//expected result 'abc   '
		 * }
		 * </pre>
		 */
		public static boolean LeftTrim(String source, String resultVar){
			return command(DDDriverStringCommands.LEFTTRIM_KEYWORD, quote(source), resultVar);
		}

		/**
		 * Returns the length of a string or variable. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Length">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * if(Strings.Length(source, var))
		 *   System.out.println("Length success: result is "+GetVariableValue(var));//expected result '3'
		 * }
		 * </pre>
		 */
		public static boolean Length(String source, String resultVar){
			return command(DDDriverStringCommands.LENGTH_KEYWORD, quote(source), resultVar);
		}
		
		/**
		 * Replace 'find' substring with 'replace' substring.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Replace">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param find String, The string to find
		 * @param replace String, The string used to replace
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * String find = "bc";
		 * String replace = "123";
		 * if(Strings.Replace(source, find, replace, var))
		 *   System.out.println("Replace success: result is "+GetVariableValue(var));//expected result 'a123'
		 * }
		 * </pre>
		 */
		public static boolean Replace(String source, String find, String replace, String resultVar){
			return command(DDDriverStringCommands.REPLACE_KEYWORD, quote(source), quote(find), quote(replace), resultVar);
		}

		/**
		 * Returns a string of a specified number of characters copied from the end of another string.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Right">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param length String, number of chars to copy
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * int length = 2;
		 * if(Strings.Right(source, length, var))
		 *   System.out.println("Right success: result is "+GetVariableValue(var));//expected result 'bc'
		 * }
		 * </pre>
		 */
		public static boolean Right(String source, int length, String resultVar){
			return command(DDDriverStringCommands.RIGHT_KEYWORD, quote(source), String.valueOf(length), resultVar);
		}
		
		/**
		 * A new string trimmed of ending tabs and spaces. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_RightTrim">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "   abc   ";
		 * if(Strings.RightTrim(source, var))
		 *   System.out.println("RightTrim success: result is "+GetVariableValue(var));//expected result '   abc'
		 * }
		 * </pre>
		 */
		public static boolean RightTrim(String source, String resultVar){
			return command(DDDriverStringCommands.RIGHTTRIM_KEYWORD, quote(source), resultVar);
		}
		
		/**
		 * The substring to retrieve starts at the specified start character index and ends after <br>
		 * the specified number of characters have been copied. If the number of characters to copy <br>
		 * is not provided, then we will return all characters after the start index. <br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_SubString">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param start int, 0-based offset character position 
		 * @param length int, number of chars to copy
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * int start = 1;
		 * int length = 1;
		 * if(Strings.SubString(source, start, length, var))
		 *   System.out.println("SubString success: result is "+GetVariableValue(var));//expected result 'b'
		 * }
		 * </pre>
		 */
		public static boolean SubString(String source, int start, int length, String resultVar){
			return command(DDDriverStringCommands.SUBSTRING_KEYWORD, quote(source), String.valueOf(start), String.valueOf(length), resultVar);
		}

		/**
		 * Returns a copy of a string, with all letters converted to lowercase.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_ToLowerCase">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "ABC";
		 * if(Strings.ToLowerCase(source, var))
		 *   System.out.println("ToLowerCase success: result is "+GetVariableValue(var));//expected result 'abc'
		 * }
		 * </pre>
		 */
		public static boolean ToLowerCase(String source, String resultVar){
			return command(DDDriverStringCommands.TOLOWERCASE_KEYWORD, quote(source), resultVar);
		}
		
		/**
		 * Returns a copy of a string, with all letters converted to uppercase.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_ToUpperCase">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "abc";
		 * if(Strings.ToUpperCase(source, var))
		 *   System.out.println("ToUpperCase success: result is "+GetVariableValue(var));//expected result 'ABC'
		 * }
		 * </pre>
		 */
		public static boolean ToUpperCase(String source, String resultVar){
			return command(DDDriverStringCommands.TOUPPERCASE_KEYWORD, quote(source), resultVar);
		}
		
		/**
		 * Returns a new string trimmed of leading and trailing tabs and spaces.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumDDDriverStringCommandsReference.htm#detail_Trim">Detailed Reference</a><p>
		 * @param source String, The input string to parse
		 * @param resultVar String, the variable to hold the result of the operation 
		 * @return boolean true if successful, false otherwise.<p>
		 * @example
		 * <pre>
		 * {@code
		 * String var = "result";
		 * String source = "   abc 	  ";
		 * if(Strings.Trim(source, var))
		 *   System.out.println("Trim success: result is "+GetVariableValue(var));//expected result 'abc'
		 * }
		 * </pre>
		 */
		public static boolean Trim(String source, String resultVar){
			return command(DDDriverStringCommands.TRIM_KEYWORD, source, resultVar);
		}
		
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io//sqabasic2000/WindowFunctionsIndex.htm">Window keywords</a>, like Maximize, Minimize, SetPosition etc.<br>
	 */
	public static class Window{
		/**
		 * Maximize current WebBrowser.
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.Maximize(Map.Google.Google);	  
		 * }
		 * </pre>	 
		 */
		public static boolean Maximize(org.safs.model.Component window){
			return action(window, WindowFunctions.MAXIMIZE_KEYWORD);
		}
		
		/**
		 * Minimize current WebBrowser.
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.Minimize(Map.Google.Google);	  
		 * }
		 * </pre>	 
		 */
		public static boolean Minimize(org.safs.model.Component window){
			return action(window, WindowFunctions.MINIMIZE_KEYWORD);
		}
		/**
		 * Restore current WebBrowser.
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.Restore(Map.Google.Google);	  
		 * }
		 * </pre>	 
		 */
		public static boolean Restore(org.safs.model.Component window){
			return action(window, WindowFunctions.RESTORE_KEYWORD);
		}
		
		/**
		 * Close the specified WebBrowser window.
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.CloseWindow(Map.Google.Google);	  
		 * }
		 * </pre>	 
		 */
		public static boolean CloseWindow(org.safs.model.Component window){
			
			return action(window, WindowFunctions.CLOSEWINDOW_KEYWORD);
		}
		
		/**
		 * Set focus to the window
		 * @param window - windows 
		 * @return true on success
		 * @example
		 * <pre>
		 * {@code
		 * 	Window.SetFocus(Map.Google.Google);		 
		 * }
		 * </pre>
		 */
		public static boolean SetFocus(org.safs.model.Component window){
			return action(window, WindowFunctions.SETFOCUS_KEYWORD);
		}
		
		/**
		 * Set position and resize current WebBrowser window
		 * @param x int, window's x position in pixels 
		 * @param y int, window's y position in pixels
		 * @param width int, window's width in pixels
		 * @param height int, window's height in pixels
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.SetPosition(Map.Google.Google, 0,0,1280,900);	  
		 * Window.SetPosition(Map.Google.Google, 0,0,800,600);	  
		 * }
		 * </pre>	 
		 */
		public static boolean SetPosition(org.safs.model.Component window, int x, int y, int width, int height){
			StringBuffer parameter = new StringBuffer();
			String separator = StringUtils.generatePositionSepeartor(Runner.jsafs().getStepSeparator());
			
			parameter.append(Integer.toString(x)+separator);
			parameter.append(Integer.toString(y)+separator);
			parameter.append(Integer.toString(width)+separator);
			parameter.append(Integer.toString(height));
			
			return action(window, WindowFunctions.SETPOSITION_KEYWORD, parameter.toString());
		}
		
		/**
		 * Set status of current WebBrowser window<br>
		 * See <a href="http://safsdev.github.io/sqabasic2000/WindowFunctionsReference.htm#detail_SetPosition">Detailed Reference</a>
		 * @param status String, window's status, it can be one of<br>
		 * <ul>
		 * <li>{@link org.safs.ComponentFunction.Window#MAXIMIZED}
		 * <li>{@link org.safs.ComponentFunction.Window#MINIMIZED}
		 * <li>{@link org.safs.ComponentFunction.Window#NORMAL}
		 * <li>The status string "x,y,width,height;Status=Normal". For example, "0,0,640,480;Status=NORMAL" or "0,0,640,480;Status=MAXIMIZED"
		 * <li>The map item represent the status string. For example, we define the status string 'preset' in map file as below:
		 *     <pre>
		 *     [Notepad]
		 *     Notepad=":AUTOIT:title=Untitled - Notepad"
		 *     preset="0,0,640,480;Status=NORMAL"
		 *     </pre>
		 *     The we could call {@code Window.SetPosition(Map.Notepad.Notepad, "preset");}
		 * </li>
		 * </ul>
		 * @return true on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Window.SetPosition(Map.Google.Google, "Maximized");//Maximize the window
		 * Window.SetPosition(Map.Google.Google, "Minimized");//Minimize the window
		 * Window.SetPosition(Map.Google.Google, "Normal");//Restore the window
		 * }
		 * </pre>	 
		 */
		public static boolean SetPosition(org.safs.model.Component window, String status){
			StringBuffer parameter = new StringBuffer();
			
			if(org.safs.ComponentFunction.Window.MAXIMIZED.equalsIgnoreCase(status) ||
			   org.safs.ComponentFunction.Window.MINIMIZED.equalsIgnoreCase(status) ||
			   org.safs.ComponentFunction.Window.NORMAL.equalsIgnoreCase(status)){
				
				String separator = StringUtils.generatePositionSepeartor(Runner.jsafs().getStepSeparator());
				parameter.append(Integer.toString(0)+separator);
				parameter.append(Integer.toString(0)+separator);
				parameter.append(Integer.toString(200)+separator);
				parameter.append(Integer.toString(200)+separator);
				parameter.append(status);
			}else{
				parameter.append(status);
			}
			
			return action(window, WindowFunctions.SETPOSITION_KEYWORD, parameter.toString());
		}	
	}
	
	/**
	 * Wrapper class providing APIs to handle 
	 * <a href="http://safsdev.github.io/sqabasic2000/TIDRestFunctionsIndex.htm">TIDRestFunctions Reference</a> and 
	 * <a href="http://safsdev.github.io/sqabasic2000/DDDriverRestCommandsIndex.htm">DriverRestCommands Reference</a>, like RestGetBinary, RestStoreResponse etc.<br>
	 */
	public static class Rest{

		//REST actions
		public static boolean EndServiceSession(org.safs.model.Component comp,String... params){
			return action(comp, TIDRestFunctions.RESTENDSERVICESESSION_KEYWORD, params);
		}
		public static boolean GetBinary(org.safs.model.Component comp,String... params){
			return action(comp, TIDRestFunctions.RESTGETBINARY_KEYWORD, params);
		}
		public static boolean Request(org.safs.model.Component comp,String... params){
			return action(comp, TIDRestFunctions.RESTREQUEST_KEYWORD, params);
		}
		public static boolean StartServiceSession(org.safs.model.Component comp,String... params){
			return action(comp, TIDRestFunctions.RESTSTARTSERVICESESSION_KEYWORD, params);
		}
		
		//REST Driver commands
		public static boolean DeleteResponse(String... params){
			return command(DDDriverRestCommands.RESTDELETERESPONSE_KEYWORD, params);
		}
		public static boolean DeleteResponseStore(String... params){
			return command(DDDriverRestCommands.RESTDELETERESPONSESTORE_KEYWORD, params);
		}
		public static boolean StoreResponse(String... params){
			return command(DDDriverRestCommands.RESTSTORERESPONSE_KEYWORD, params);
		}
		public static boolean CleanResponseMap(String... params){
			return command(DDDriverRestCommands.RESTCLEANRESPONSEMAP_KEYWORD, params);
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle 
	 * <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsIndex.htm">GenericMasterFunctions Reference</a> and 
	 * <a href="http://safsdev.github.io/sqabasic2000/GenericObjectFunctionsIndex.htm">GenericObjectFunctions Reference</a>, like VerifyProperty, IsPropertyExist etc.<br>
	 */
	public static class Component{
		/**
		 * Assign object property to a variable. The property could be attribute or CSS property.
		 * @param comp -- Component (from App Map) to get property.
		 * @param property -- attribute or CSS property.
		 * @param variable -- the name of the variable to receive the proeprty value.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVar";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * String label = SAFSPlus.GetVariableValue(labelVar);
		 * }
		 * </pre>
		 * @return true on success.
		 */
		public static boolean AssignPropertyVariable(org.safs.model.Component comp,String property, String variable){
			return action(comp, GenericMasterFunctions.ASSIGNPROPERTYVARIABLE_KEYWORD, property, variable);
		}
		/**
		 * Copy the current contents of a component's data to a file.
		 * @param comp Component, (from App Map) to retrieve data.
		 * @param fileName String, The file containing the component's data.
		 * @param params optional<ul>
		 * <b>params[0] encoding</b> String, The file encoding<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.CaptureObjectDataToFile(Map.SampleApp.ListView, "listview.dat");
		 * boolean success = Component.CaptureObjectDataToFile(Map.SampleApp.ListView, "listview.dat", "utf-8");
		 * boolean success = Component.CaptureObjectDataToFile(Map.SampleApp.ListView, "d:\testproj\test\listview.dat");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CaptureObjectDataToFile(org.safs.model.Component comp, String fileName, String... params){
			return action(comp, GenericMasterFunctions.CAPTUREOBJECTDATATOFILE_KEYWORD, combineParams(params, fileName));
		}
		/**
		 * Copy all of the value properties a test object to a file. 
		 * @param comp Component (from App Map) to retrieve all properties.
		 * @param file String, to store all properties
		 * @param fileEncoding String, the encoding of the file. optional, default is the system-encoding.
		 * @return True on success, a file will be created into Actuals dir
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.CapturePropertiesToFile(Map.Google.SignIn, "singin.properties");
		 * Component.CapturePropertiesToFile(Map.Google.SignIn, "singin.properties", "UTF-8");
		 * }
		 * </pre>	 
		 */		
		public static boolean CapturePropertiesToFile(org.safs.model.Component comp, String file, String... fileEncoding){
			return action(comp, GenericMasterFunctions.CAPTUREPROPERTIESTOFILE_KEYWORD, combineParams(fileEncoding, file));
		}
		/**
		 * Copy a propertie's value of a test object to to a file.
		 * @param property String, the property name to get value
		 * @param comp Component (from App Map) to retrieve property's value
		 * @param fileEncoding String, the encoding of the file. optional, default is the system-encoding.
		 * @return boolean, True on success and a file will be created into Actuals dir
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.CapturePropertyToFile(Map.Google.SignIn, "display", "singin.dispaly.dat");
		 * Component.CapturePropertyToFile(Map.Google.SignIn, "color", "singin.color.dat", "UTF-8");
		 * }
		 * </pre>	 
		 */		
		public static boolean CapturePropertyToFile(org.safs.model.Component comp, String property, String file, String... fileEncoding){
			return action(comp, GenericMasterFunctions.CAPTUREPROPERTYTOFILE_KEYWORD, combineParams(fileEncoding, property, file));
		}
		/**
		 * Some components like Tree, Menu may have a cache containing their content or time-consuming<br>
		 * resource, which will speed up the test. But the cache may contain obsolete objects, and they will<br>
		 * affect the test, to use component's latest data, user needs call this method to clear the cache<br>
		 * @param comp -- Component (from App Map) from which to clear the cache.  
		 * @return -- True if the component's cache has been cleared.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Tree.ClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * Component.ClearCache(Map.Google.Tree);
		 * boolean success = Tree.CaptureTreeDataToFile(Map.Google.Tree, "D:\data\tree.dat");
		 * }
		 * </pre>	 
		 */		
		public static boolean ClearCache(org.safs.model.Component comp){
			return action(comp, GenericMasterFunctions.CLEARCACHE_KEYWORD);
		}

		/**
		 * Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = Click(Map.Google.Apps);//Click at the center
		 * 2) boolean success = Click(Map.Google.Apps,"20,20");//Click at the coordinate (20,20)
		 * 3) boolean success = Click(Map.Google.Apps,"20%,30%"); // Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = Click(Map.Google.Apps,"AppMapSubkey");//Click at the coordinate defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = Click(Map.Google.Apps,"20,20", "false");//Click at the coordinate (20,20), and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * "AppMapSubkey" is expected to be an AppMap entry in an "Apps" section in the App Map.
		 * See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean Click(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.CLICK_KEYWORD, replaceSeparator(params));
		}
		
		/**
		 * A left mouse drag is performed from one object to another object based on the offsets values. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/GenericObjectFunctionsReference.htm#detail_DragTo">Detailed Reference</a>
		 * @param from Component, the component (from App Map) relative to which to calculate start coordinates to drag
		 * @param to Component, the component (from App Map) relative to which to calculate end coordinates to drag
		 * @param params optional<ul>
		 * <b>params[0] offsets</b> String, indicating the offset relative to component in percentage or in pixel, 
		 *                                  like "20%,10%, %50, %60", "30, 55, 70, 80", or even "20%,10%, 70, 80".
		 *                                  If not provided, then "50%, 50%, 50%, 50%" will be used as default value, 
		 *                                  which means the drag point is the center of the component.<br>
		 * <b>params[1] fromSubItem</b> String, as text. e.g tree node or list item or any sub main component's item.<br>
		 * <b>params[2] toSubItem</b> String, as text. e.g tree node or list item or any sub main component item.<br>
		 * <b>params[3] pause</b> int, time in milliseconds to hold the mouse button at the "to component" area before releasing.
		 *                             The default value is {@link DriverConstant#DEFAULT_SAFS_TEST_DND_RELEASE_DELAY} .<br>

		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = DragTo(Map.Google.Apps, Map.Google.Area);//Left-Drag from center of component Map.Google.Apps to center of component Map.Google.Area
		 * boolean success = DragTo(Map.Google.Apps, Map.Google.Area, "20%,10%, %50, %60");//Left-Drag from (20%,10%) of component Map.Google.Apps to (%50, %60) of component Map.Google.Area
		 * boolean success = DragTo(Map.Google.Apps, Map.Google.Area, "", "", "", "2000");//Left-Drag from center of component Map.Google.Apps to center of component Map.Google.Area, and hold the mouse button for 2 seconds at the component Map.Google.Area
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean DragTo(org.safs.model.Component from, org.safs.model.Component to, String... optionals){
			String parentNameOfDestination = to.getParentName()==null? to.getName():to.getParentName();
			return action(from, GenericObjectFunctions.DRAGTO_KEYWORD, combineParams(replaceSeparator(optionals), parentNameOfDestination, to.getName()));
		}

		/**
		 * Execute a simple piece of javascript on component synchronously.
		 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_ExecuteScript">Detailed Reference</a><p>
		 * If the script will return a string value, call SAFSPlus.prevResults.getStatusInfo() to get it. <br>
		 * Object result is NOT supported yet.<br>
		 * This is currently <b>ONLY supported for Selenium WebDriver Engine.</b>
		 * @param comp org.safs.model.Component, (from generated Map.java).<br>
		 *                                       In your script you reference this DOM Element as '<b>arguments[0]</b>'.<br>
		 * @param script String, the javascript to execute.<br>
		 * @param scriptParams optional, Script arguments must be a number, a boolean, a String, DOM Element, or a List of any combination of the above.
		 *                               An exception will be thrown if the arguments do not meet these criteria.
		 *                               The arguments will be made available to the JavaScript via the "arguments" variable.
		 * <ul>
		 * scriptParams[0] : Passed to the script as '<b>arguments[1]</b>', if used.<br>
		 * scriptParams[1] : Passed to the script as '<b>arguments[2]</b>', if used.<br>
		 * ... more script's parameter<br>
		 * </ul>
		 * @return true if no errors were encountered. 
		 * @example
		 * <pre>
		 * {@code
		 * SAFSPlus.ExecuteScript(
		 *     Map.Google.SignIn,                       // The DOM Element passed as 'arguments[0]' to the script. 
		 *     "arguments[0].innerHTML=arguments[1];",  // Script to set the DOM Elements innerHTML value.
		 *     "my text value");                        // The value passed as 'arguments[1]' to set to innerHTML.
		 * 
		 * SAFSPlus.ExecuteScript(
		 *     Map.Google.SignIn,                       // The DOM Element passed as 'arguments[0]' to the script. 
		 *     "return arguments[0].innerHTML;");       // A script to return the DOM Elements innerHTML.
		 * 
		 *  // scriptResult should get the innerHTML value returned.
		 * String scriptResult = SAFSPlus.prevResults.getStatusInfo();
		 * }
		 * </pre>	
		 * @see #executeScript(String, Object...)
		 * @see #executeAsyncScript(String, Object...)
		 */	
		public static boolean ExecuteScript(org.safs.model.Component comp, String script, String... scriptParams){
			return action(comp,GenericMasterFunctions.EXECUTESCRIPT_KEYWORD, combineParams(scriptParams, script));
		}
		/**
		 * Incorporate OCR technology to detect the text on a GUI component and save the text to a variable.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GetTextFromGUI">Detailed Reference</a>
		 * <br>
		 * This requires that SAFS-OCR has been installed.
		 * @param comp Component (from App Map) from which to retrieve text.
		 * @param variable String, The name of the variable to receive detected text.
		 * @param params optional<ul>
		 * <b>params[0] SubArea</b> String, indicating partial image of the component to capture, 
		 *                    it can be app map subkey referring a subarea or the subarea itself like "5,10, %50, %60"<br>
		 * <b>params[1] OCRId</b> String, indicating the OCR used to recognize text. TOCR or GORC<br>
		 * <b>params[2] LangId</b> String, representing the language in use for selected OCR to recognize text. "en", "cn" etc.<br>
		 * <b>params[3] ScaleRatio</b> float, indicating the scale ratio for resizing the original image. "1.5" <br>
		 * </ul>
		 * @return boolean, True on success and the text will be saved to a variable
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.GetTextFromGUI(Map.Google.SignIn, "signinText");
		 * String value = SAFSPlus.GetVariableValue("signinText");
		 * Component.GetTextFromGUI(Map.Google.SignIn, "signinText", "", "GOCR");
		 * Component.GetTextFromGUI(Map.Google.SignIn, "signinText", "5,10, %50, %60", "TOCR", "en", "1.5");
		 * Component.GetTextFromGUI(Map.Google.SignIn, quote("signin-partial"), "0, 0, 70%, 100%", "GOCR");
		 * //"subarea" is defined in map file
		 * //[SignIn]
		 * //subarea=="0,0,50%,50%"
		 * Component.GetTextFromGUI(Map.Google.SignIn,"SignInPartialText", "subarea");
		 * //or
		 * //"subarea" is defined in map file
		 * //[ApplicationConstants]
		 * //subarea=="0,0,50%,50%"
		 * Component.GetTextFromGUI(Map.Google.SignIn, "SignInPartialText", Map.subarea, "TOCR");
		 * }
		 * </pre>
		 */
		public static boolean GetTextFromGUI(org.safs.model.Component comp, String variable, String... params){
			return action(comp, GenericMasterFunctions.GETTEXTFROMGUI_KEYWORD, combineParams(replaceSeparator(params), variable));
		}
		/**
		 * Take a screenshot of windows or component.<br>
		 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GetGUIImage">Detailed Reference</a><p>
		 * @param comp Component, the component to get its image.
		 * @param fileName String, the file name to store image. Suggest to save as .png image.<br>
		 *                         It can be relative or absolute. If it is relative, the file will be stored to TestDirectory "Actuals".<br>
		 *                         Supported file extensions are ".jpeg", ".tif", ".gif", ".png", ".pnm", ".bmp" etc.<br>
		 *                         If the file extension is not supported, then suffix ".bmp" will be appended to filename.<br>
		 * @param params optional
		 * <ul>
		 * <b>params[1] SubArea</b> String, (x1,y1,x2,y2) indicating partial image of the component to capture, such as "0,0,50%,50%", <br>
		 *                                  it can be app map subkey under component name.<br>
		 * <b>params[2] FilteredAreas</b> String, (x1,y1,x2,y2 x1,y1,x2,y2) a set of areas to filter the current GUI image.<br>
		 *                          it has a prefix "<font color='red'>Filter</font>", and followed by a set of subareas. Such as "<font color='red'>Filter</font>=0,0,5,5 50%,50%,15,15"<br>
		 *                          Multiple areas are separated by a space character. The filtered area is covered by black.<br>
		 * </ul>
		 * @return boolean, true on success; false otherwise
		 * @example	 
		 * <pre>
		 * {@code
		 * GetGUIImage(Map.Google.SignIn,"SignIn");//will be saved at <testProject>\Actuals\SignIn.bmp
		 * GetGUIImage(Map.Google.SignIn,"c:/temp/SignIn.gif");
		 * 
		 * //Following example will store part of the SingIn image, 
		 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", "0,0,50%,50%");
		 * //"subarea" is defined in map file
		 * //[SignIn]
		 * //subarea="0,0,50%,50%"
		 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", "subarea");
		 * //or
		 * //"subarea" is defined in map file
		 * //[ApplicationConstants]
		 * //subarea="0,0,50%,50%"
		 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", Map.subarea);
		 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", Map.subarea());
		 * 
		 * //Filter the SingIn image and save it
		 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", quote("Filter=0,0,10,10 60,60,10,10"));
		 * //"filterAreas" is defined in map file
		 * //[SignIn]
		 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
		 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", "filterAreas");
		 * //"filterAreas" is defined in map file
		 * //[ApplicationConstants]
		 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
		 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", Map.filterAreas);
		 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", Map.filterAreas());
		 * }
		 * </pre>	
		 * 
		 */
		public static boolean GetGUIImage(org.safs.model.Component comp, String fileName, String... params){
			return action(comp,GenericMasterFunctions.GETGUIIMAGE_KEYWORD, combineParams(replaceSeparator(params), fileName));	
		}

		/**
		 * Verify the visual existence of a particular window and/or component.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GUIDoesExist">Detailed Reference</a>
		 * <p>
		 * @param comp -- Component (from App Map).  
		 * @return boolean, true if GUI exists; false if GUI does not exist or execution fail.
		 * @example
		 * <pre>
		 * {@code
		 * Component.GUIDoesExist(Map.AUT.EditBox);
		 * }
		 * </pre>
		 */
		public static boolean GUIDoesExist(org.safs.model.Component comp){
			return action(comp, GenericMasterFunctions.GUIDOESEXIST_KEYWORD);
		}
		/**
		 * Verify the visual non-existence of a particular window and/or component.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GUIDoesNotExist">Detailed Reference</a>
		 * <p>
		 * @param comp -- Component (from App Map).  
		 * @return boolean, true if GUI does not exist; false if GUI does exist or execution fail.
		 * @example
		 * <pre>
		 * {@code
		 * Component.GUIDoesNotExist(Map.AUT.EditBox);
		 * }
		 * </pre>
		 */
		public static boolean GUIDoesNotExist(org.safs.model.Component comp){
			return action(comp, GenericMasterFunctions.GUIDOESNOTEXIST_KEYWORD);
		}
		
		/**
		 * Hover the mouse over a component.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_HoverMouse">Detailed Reference</a>
		 * @param comp org.safs.model.Component, the component to hover.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] coordination</b> String, The offset from center of component, 
		 *                                       such as "200;400", or a mapKey defined under "ComponentName" or "ApplicationConstants" in map file.<br>
		 * <b>optionals[1] hoverTime</b> int, milliseconds to hover<br>
		 * </ul>
		 * @return true if hover succeeds, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.HoverMouse(Map.AUT.EditBox);//hover at the center of EditBox for 2 seconds
		 * boolean success = Component.HoverMouse(Map.AUT.EditBox, "50, 30", "1000");//hover at (50,30) of EditBox for 1 second
		 * boolean success = Component.HoverMouse(Map.AUT.EditBox, "locKey", "3000");//locKey="500, 300" defined in map file under "EditBox" or "ApplicationConstants"
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean HoverMouse(org.safs.model.Component comp, String... optionals){
			return action(comp, GenericMasterFunctions.HOVERMOUSE_KEYWORD, replaceSeparator(optionals));
		}
		
		/**
		 * Sends keystrokes to the specified Component.
		 * <p>
		 * This supports special key characters like:
		 * <p><pre>
		 *     {Enter} = ENTER Key
		 *     {Tab} = TAB Key
		 *     ^ = CONTROL Key with another key ( "^s" = CONTROL + s )
		 *     % = ALT Key with another key ( "%F" = ALT + F )
		 *     + = SHIFT Key with another key ( "+{Enter}" = SHIFT + ENTER )  
		 * </pre>
		 * We are generally providing this support through our generic <a href="http://safsdev.github.io/doc/org/safs/tools/input/CreateUnicodeMap.html">InputKeys Support</a>.
		 * <p>
		 * @param comp -- Component (from App Map).  
		 * @param textvalue -- to send via input to the Component.
		 * @return
		 * @see org.safs.selenium.webdriver.lib.Component#inputKeys(String)
		 * @see SAFSPlus#quote(String)
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.InputKeys(Map.AUT.EditBox, quote("^a"));//"Ctrl+a" Select all text of this EditBox
		 * }
		 * </pre>
		 */
		public static boolean InputKeys(org.safs.model.Component comp, String textvalue){
			return action(comp, GenericMasterFunctions.INPUTKEYS_KEYWORD, textvalue);
		}
		
		/**
		 * Sends AWT Robot keystrokes to whatever currently has keyboard focus.  
		 * This is intended to work for both local and remote Selenium Servers (when Remote RMI is properly enabled).
		 * <p>
		 * This supports special key characters like:
		 * <p><pre>
		 *     {Enter} = ENTER Key
		 *     {Tab} = TAB Key
		 *     ^ = CONTROL Key with another key ( "^s" = CONTROL + s )
		 *     % = ALT Key with another key ( "%F" = ALT + F )
		 *     + = SHIFT Key with another key ( "+{Enter}" = SHIFT + ENTER )  
		 * </pre>
		 * We are generally providing special key support through our generic <a href="http://safsdev.github.io/doc/org/safs/tools/input/CreateUnicodeMap.html">InputKeys Support</a>.
		 * <p>
		 * @param textvalue -- to send via Robot to the current keyboard focus.
		 * @return
		 * @see #TypeChars(String)
		 * @see SAFSPlus#quote(String)
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.TypeKeys(quote("^a"));//"Ctrl+a" Select all text of this EditBox
		 * }
		 * </pre>
		 */
		public static boolean TypeKeys(String textvalue){
			String[] parms = textvalue == null ? new String[0] : new String[]{textvalue};
			return action(new GenericObject("CurrentWindow", "CurrentWindow"), GenericMasterFunctions.TYPEKEYS_KEYWORD, parms);
//			return actionGUILess(GenericMasterFunctions.TYPEKEYS_KEYWORD, keystrokes);
		}
		
		/**
		 * Sends key characters to the specified Component.
		 * <p>
		 * @param comp -- Component (from App Map).  
		 * @param textvalue -- to send via input to the Component.
		 * @return
		 * @see org.safs.selenium.webdriver.lib.Component#inputKeys(String)
		 * @see SAFSPlus#quote(String)
		 * @example
		 * <pre>
		 * {@code
		 * Component.InputCharacters(Map.AUT.EditBox, "Test Value");
		 * Component.InputCharacters(Map.AUT.EditBox, quote("UTF-8"));
		 * Component.InputCharacters(Map.AUT.EditBox, quote("^NotVariable"));
		 * }
		 * </pre>
		 */
		public static boolean InputCharacters(org.safs.model.Component comp, String textvalue){
			return action(comp, GenericMasterFunctions.INPUTCHARACTERS_KEYWORD, textvalue);
		}
		
		/**
		 * Sends key characters to the current keyboard focus via AWT Robot.  
		 * This is intended to work for both local and remote Selenium Servers (when Remote RMI is properly enabled).
		 * <p>
		 * @param textvalue -- to send via input by AWT Robot.
		 * @return
		 * @see #TypeKeys(String)
		 * @see SAFSPlus#quote(String)
		 * @example
		 * <pre>
		 * {@code
		 * Component.TypeChars("Test Value");
		 * Component.TypeChars(quote("UTF-8"));
		 * Component.TypeChars(quote("^NotVariable"));
		 * }
		 * </pre>
		 */
		public static boolean TypeChars(String textvalue){
			String[] parms = textvalue == null ? new String[0] : new String[]{textvalue};
			return action(new GenericObject("CurrentWindow", "CurrentWindow"), GenericMasterFunctions.TYPECHARS_KEYWORD, parms);
//			return actionGUILess(GenericMasterFunctions.TYPECHARS_KEYWORD, textvalue);
		}
			
		/**
		 * Sends secret-text (such as password) to the current focused Component.<br>
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeEncryption">Detailed Reference</a>
		 * <p>
		 * @param encryptedDataFile String, the file containing 'encrypted data' to send to the current focused Component.
		 * @param privateKeyFile String, the file containing 'private key' to decrypt the 'encrypted data'
		 * @return true on success
		 * @see org.safs.robot.Robot#inputChars(String)
		 * @example	 
		 * <pre>
		 * {@code
		 * //the publickey and privatekey are generated by org.safs.RSA
		 * //C:\safs\passwords\encrypted.pass contained the encrypted-data (by public key)
		 * SAFSPlus.TypeEncryption("C:\safs\passwords\encrypted.pass", "D:\secretPath\private.key" );
		 * }
		 * </pre>
		 */
		public static boolean TypeEncryption(String encryptedDataFile, String privateKeyFile){
			return actionGUILess(GenericMasterFunctions.TYPEENCRYPTION_KEYWORD, encryptedDataFile, privateKeyFile);
		}
		
		/**
		 * Verify if object's property exists or not. 
		 * @param comp -- Component (from App Map) to get property's existence.
		 * @param property -- attribute or CSS property.
		 * @param variable -- the name of the variable to receive the proeprty's existence.
		 * @example	 
		 * <pre>
		 * {@code
		 * String PropertyExistVariable = "PropertyExistVariable";
		 * Component.IsPropertyExist(Map.AUT.Lable,"textContent", PropertyExistVariable);
		 * String existence = SAFSPlus.GetVariableValue(PropertyExistVariable);
		 * }
		 * </pre>
		 * @return true on success, "true" or "false" will be saved to a variable according to the existence of property.
		 */
		public static boolean IsPropertyExist(org.safs.model.Component comp,String property, String variable){
			return action(comp, GenericMasterFunctions.ISPROPERTYEXIST_KEYWORD, property, variable);
		}
		
		/**
		 * Verify if object's property exists or not. 
		 * @param comp -- Component (from App Map) to get property's existence.
		 * @param property -- attribute or CSS property.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean existence = Component.IsPropertyExist(Map.AUT.Lable,"textContent");
		 * }
		 * </pre>
		 * @return boolean, true if property exists. false otherwise.
		 * @throws SeleniumPlusException if the execution fails.
		 */		
		public static boolean IsPropertyExist(org.safs.model.Component comp,String property) throws SeleniumPlusException{
			String variable = "PropertyExistVariable"+System.currentTimeMillis();
			String keyword = GenericMasterFunctions.ISPROPERTYEXIST_KEYWORD;
			if(action(comp, keyword, property, variable)){
				String existence = GetVariableValue(variable);
				return StringUtilities.convertBool(existence);
			}else{
				throw new SeleniumPlusException("Fail to execute keyword '"+keyword+"'");
			}
		}
		
		/**
		 * Store the location and dimensions of a component.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_LocateScreenImage">Detailed Reference</a>
		 * @param variable String, The root name of the collection of variables to receive the location and dimensions.
		 *                         <br>variable=x y w h [space delimited values]
		 *                         <br>variable.x=x
		 *                         <br>variable.y=y
		 *                         <br>variable.w=w
		 *                         <br>variable.h=h
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] relativeTo</b> String, "screen" or "parent". Default is "screen".
		 * </ul>
		 * @return true if the location and dimensions are stored successfully to variable, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String variable = "editboxRect";
		 * boolean success = Component.LocateScreenImage(Map.AUT.EditBox, variable);//EditBox's screen location and dimension will be store to variable "editboxRect"
		 * String rect = GetVariableValue(variable);
		 * String x = GetVariableValue(variable+".x");
		 * String y = GetVariableValue(variable+".y");
		 * String w = GetVariableValue(variable+".w");
		 * String h = GetVariableValue(variable+".h");
		 * boolean success = Component.LocateScreenImage(Map.AUT.EditBox, variable, RELATIVE_TO_PARENT);//EditBox's relative location and dimension will be store to variable "editboxRect"
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean LocateScreenImage(org.safs.model.Component comp, String variable, String... optionals){
			return action(comp, GenericMasterFunctions.LOCATESCREENIMAGE_KEYWORD, combineParams(optionals, variable));
		}
		
		/**
		 * Incorporate OCR technology to detect the text on a GUI component and save the text to a file.
		 * See <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsReference.htm#detail_SaveTextFromGUI">Detailed Reference</a>
		 * <br>
		 * This requires that SAFS-OCR has been installed.
		 * @param comp Component (from App Map) from which to retrieve text.
		 * @param outputFile String, The name of the file to store detected text.
		 * @param params optional
		 * <ul>
		 * <b>params[0] SubArea</b> String, indicating partial image of the component to capture,
		 *                    it can be app map subkey referring a subarea or the subarea itself like "5,10, %50, %60"<br>
		 * <b>params[1] OCRId</b> String, indicating the OCR used to recognize text. TOCR or GORC<br>
		 * <b>params[2] LangId</b> String, representing the language in use for selected OCR to recognize text. "en", "cn" etc.<br>
		 * <b>params[3] ScaleRatio</b> float, indicating the scale ratio for resizing the original image. "1.5" <br>
		 * </ul>
		 * @return boolean, True on success and the text will be saved to a file.
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.SaveTextFromGUI(Map.Google.SignIn, "signin.txt");
		 * Component.SaveTextFromGUI(Map.Google.SignIn, "D:\data\test\signin.txt");
		 * Component.SaveTextFromGUI(Map.Google.SignIn, "signin.txt", "", "GOCR");
		 * Component.SaveTextFromGUI(Map.Google.SignIn, "signin.txt", "5,10, %50, %60", "TOCR", "en", "1.5");
		 * Component.SaveTextFromGUI(Map.Google.SignIn, quote("signin-partial.txt"), "0, 0, 70%, 100%", "GOCR");
		 * //"subarea" is defined in map file
		 * //[SignIn]
		 * //subarea=="0,0,50%,50%"
		 * Component.SaveTextFromGUI(Map.Google.SignIn,"SignInPartialText.txt", "subarea");
		 * //or
		 * //"subarea" is defined in map file
		 * //[ApplicationConstants]
		 * //subarea=="0,0,50%,50%"
		 * Component.SaveTextFromGUI(Map.Google.SignIn, "D:\data\test\SignInPartialText.txt", Map.subarea, "TOCR");
		 * }
		 * </pre>
		 */
		public static boolean SaveTextFromGUI(org.safs.model.Component comp, String outputFile, String... params){
			return action(comp, GenericMasterFunctions.SAVETEXTFROMGUI_KEYWORD, combineParams(replaceSeparator(params), outputFile));
		}
		
		/**
		 * Verify computed CSS style against bench mark. If there is no benchmark file found
		 * then the test will fail. User responsibility to copy bench mark to the project
		 * benchmark folder. 
		 * @param comp -- Component (from App Map) to verify computed CSS style.  
		 * @param benchfile -- benchmark json file name from the project benchmark dir or User 
		 * specify full path.
		 * @return -- True on success or A file will be created into Actuals dir on failure.
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.VerifyComputedStyle(Map.Google.SignIn,"benmarkfile.json");
		 * Component.VerifyComputedStyle(Map.Google.SignIn,"c:\\temp\\file.json");	  
		 * }
		 * </pre>	 
		 */		
		public static boolean VerifyComputedStyle(org.safs.model.Component comp,String benchfile){
			return action(comp, GenericMasterFunctions.VERIFYCOMPUTEDSTYLE_KEYWORD, benchfile);
		}
		
		/**
		 * Save component's computed CSS style to a test file.
		 * @param comp -- Component (from App Map) to verify computed CSS style.  
		 * @param testfile -- test json file name from the project test dir or User specify full path.
		 * @return -- True on success
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.GetComputedStyle(Map.Google.SignIn,"testfile.json");
		 * Component.GetComputedStyle(Map.Google.SignIn,"c:\\temp\\file.json");	  
		 * }
		 * </pre>	 
		 */		
		public static boolean GetComputedStyle(org.safs.model.Component comp, String testfile){
			return action(comp, GenericMasterFunctions.GETCOMPUTEDSTYLE_KEYWORD, testfile);
		}
		
		/**
		 * Verify the screen shot of a GUI component with a benchmark image file.
		 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyGUIImageToFile">Detailed Reference</a><p>
		 * @param comp Component, the component to get its image.
		 * @param benchFile String, the benchmark file name. Suggest to compare with .png image.<br>
		 *                         It can be relative or absolute. If it is relative, the file will be found at BenchDirectory "Benchmarks".<br>
		 *                         Supported file extensions are ".jpeg", ".tif", ".gif", ".png", ".pnm", ".bmp" etc.<br>
		 *                         If the file extension is not supported, then suffix ".bmp" will be appended to filename.<br>
		 * @param params optional
		 * <ul>
		 * <b>params[1] SubArea</b> String, indicating partial image of the component to capture, such as "0,0,50%,50%", <br>
		 *                                  it can be app map subkey under component name.<br>
		 * <b>params[2] PercentageTolerance</b> int, the percentage of bits need to be matched. it is between 0 and 100.<br>
		 *                                100 means only all bits of images match, the images will be considered matched.<br>
		 *                                0 means even no bits match, the images will be considered matched.<br>
		 * <b>params[3] UUIDFlag</b> boolean, set to quote("UUID=False") to prevent runtime Test/Actual filenames appended with Universally Unique IDs<br>
		 *                     This essentially allows the runtime Test/Actual filename to be the same as the Benchmark.<br>
		 * <b>params[4] FilteredAreas</b> String, a set of areas to filter the current GUI image and the bench image before comparing.<br>
		 *                          it has a prefix "<font color='red'>Filter</font>", and followed by a set of subareas. Such as "<font color='red'>Filter</font>=0,0,5,5 50%,50%,15,15"<br>
		 *                          Multiple areas are separated by a space character. The filtered area is covered by black.
		 * </ul>
		 * @return boolean, true if verification success; false otherwise
		 * @example	 
		 * <pre>
		 * {@code
		 * VerifyGUIImageToFile(Map.Google.SignIn,"SignIn");//will be compared with file <testProject>\Benchmarks\SignIn.bmp
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif");
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", quote("UUID=False"));// Simple output filename, no UUID.
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "95");//if 95% bits match, the verification will pass.
		 *
		 * //Following example will verify part of the SingIn image,
		 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", quote("0,0,50%,50%"));
		 * //"subarea" is defined in map file
		 * //[SignIn]
		 * //subarea="0,0,50%,50%"
		 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", "subarea");
		 * //or
		 * //"subarea" is defined in map file
		 * //[ApplicationConstants]
		 * //subarea="0,0,50%,50%"
		 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", Map.subarea);
		 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", Map.subarea());
		 * 
		 * //Filter the SingIn image and the bench image at certain areas and compare them
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", quote("Filter=0,0,10,10 60,60,10,10"));
		 * //"filterAreas" is defined in map file
		 * //[SignIn]
		 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", "filterAreas");
		 * //"filterAreas" is defined in map file
		 * //[ApplicationConstants]
		 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", Map.filterAreas);
		 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", Map.filterAreas());
		 * }
		 * </pre>	
		 */
		public static boolean VerifyGUIImageToFile(org.safs.model.Component comp, String benchFile, String... params){		
			return action(comp,GenericMasterFunctions.VERIFYGUIIMAGETOFILE_KEYWORD, combineParams(replaceSeparator(params), benchFile));	
		}
		
		/**
		 * Verify object property. The property could be attribute or CSS property.
		 * @param comp -- Component (from App Map) to verify property.
		 * @param property -- attribute or CSS property.
		 * @param value -- property value to be verified.
		 * @return true on success.
		 */
		public static boolean VerifyProperty(org.safs.model.Component comp,String property, String value){
			String[] param = {property,value};
			return action(comp, GenericMasterFunctions.VERIFYPROPERTY_KEYWORD,param);
		}

		/**
		 * Verify that the value of an object property contains a string.<br> 
		 * The property could be attribute or CSS property.<br>
		 * @param comp Component, (from App Map) to verify property.
		 * @param property String, attribute or CSS property.
		 * @param containedValue String, property value to be verified.
		 * @return true on success.
		 */
		public static boolean VerifyPropertyContains(org.safs.model.Component comp, String property, 
				String containedValue){
			String[] param = {property,containedValue};
			return action(comp, GenericMasterFunctions.VERIFYPROPERTYCONTAINS_KEYWORD,param);
		}
		
		/**
		 * Verify that the value of an object property contains a string.<br> 
		 * The property could be attribute or CSS property.<br>
		 * @param comp Component, (from App Map) to verify property.
		 * @param property String, attribute or CSS property.
		 * @param containedValue String, property value to be verified.
		 * @param caseSensitive boolean, if the comparison is case-sensitive or not.
		 * @return true on success.
		 * @example	 
		 * <pre>
		 * {@code
		 * Component.VerifyPropertyContains(Map.Google.SignIn, "font-family", "Helvetica");
		 * Component.VerifyPropertyContains(Map.Google.SignIn, "font-family", "HELVETICA", false);
		 * }
		 * </pre>
		 */
		public static boolean VerifyPropertyContains(org.safs.model.Component comp, String property, 
				String containedValue, boolean caseSensitive){
			String[] param = {property,containedValue, Boolean.toString(caseSensitive)};
			return action(comp, GenericMasterFunctions.VERIFYPROPERTYCONTAINS_KEYWORD,param);
		}
		
		/**
		 * Verify the value of a single object property with a benchmark file.
		 * @param comp Component, (from App Map) to verify property.
		 * @param property String, attribute or CSS property to verify.
		 * @param benchFile String, The file containing the value of the property to compare.
		 * @param params --<ul>
		 * params[0] -- optional -- The file encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.VerifyPropertyToFile(Map.Google.SignIn, "font-family", "bench.font.family.dat");
		 * boolean success = Component.VerifyPropertyToFile(Map.Google.SignIn, "font-family", "bench.font.family.dat", "utf-8");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyPropertyToFile(org.safs.model.Component comp, String property,
				String benchFile, String... params){
			return action(comp, GenericMasterFunctions.VERIFYPROPERTYTOFILE_KEYWORD, combineParams(params, property, benchFile));
		}
		
		/**
		 * Compare/Verify all of the value properties of a test object with a benchmark file.
		 * @param comp Component, (from App Map) to verify property.
		 * @param benchFile String, The file containing the values of the properties to compare.
		 * @param params --<ul>
		 * params[0] -- optional -- The file encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.VerifyPropertiesToFile(Map.Google.SignIn, "signin.properties");
		 * boolean success = Component.VerifyPropertiesToFile(Map.Google.SignIn, "signin.properties", "utf-8");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyPropertiesToFile(org.safs.model.Component comp, String benchFile, String... params){
			return action(comp, GenericMasterFunctions.VERIFYPROPERTIESTOFILE_KEYWORD, combineParams(params, benchFile));
		}

		/**
		 * Compare/Verify the value properties of a test object with a benchmark file.<br>
		 * Only those property names and values in the benchmark file will be compared.
		 * @param comp Component, (from App Map) to verify property.
		 * @param benchFile String, The file containing the value of the properties to compare.
		 * @param params --<ul>
		 * params[0] -- optional -- The file encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.VerifyPropertiesSubsetToFile(Map.Google.SignIn, "signinSubset.properties");
		 * boolean success = Component.VerifyPropertiesSubsetToFile(Map.Google.SignIn, "signinSubset.properties", "utf-8");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyPropertiesSubsetToFile(org.safs.model.Component comp, String benchFile, String... params){
			return action(comp, GenericMasterFunctions.VERIFYPROPERTIESSUBSETTOFILE_KEYWORD, combineParams(params, benchFile));
		}

		/**
		 * Verify the current contents of a component's data with a benchmark file.
		 * @param comp Component, (from App Map) to retrieve data.
		 * @param benchFile String, The benchmark file containing the date to compare.
		 * @param params optional--<ul>
		 * params[0] -- The file encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Component.VerifyObjectDataToFile(Map.SampleApp.ListView, "listview.dat");
		 * boolean success = Component.VerifyObjectDataToFile(Map.SampleApp.ListView, "listview.dat", "utf-8");
		 * boolean success = Component.VerifyObjectDataToFile(Map.SampleApp.ListView, "d:\testproj\bench\listview.dat");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyObjectDataToFile(org.safs.model.Component comp, String benchFile, String... params){
			return action(comp, GenericMasterFunctions.VERIFYOBJECTDATATOFILE_KEYWORD, combineParams(params, benchFile));
		}
		
		/**
		 * Make the component visible on the page.
		 * @param comp Component, (from App Map) to be visible on page.
		 * @param params optional<ul>
		 * <b>optionals[0] verify</b> boolean, verify that the component is shown on page. The default value is false.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * //Try to make Map.SampleApp.EditBox becomes visible on page
		 * boolean success = Component.ShowOnPage(Map.SampleApp.EditBox);
		 * //Try to make Map.SampleApp.EditBox becomes visible on page and verify that it is shown.
		 * boolean success = Component.ShowOnPage(Map.SampleApp.EditBox, "true");
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ShowOnPage(org.safs.model.Component comp, String... params){
			return action(comp, GenericMasterFunctions.SHOWONPAGE_KEYWORD, params);
		}

		/**
		 * Hover the mouse over a specified screen location.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_HoverScreenLocation">Detailed Reference</a>
		 * @param coordination String, The screen location, such as "200;400", or a mapKey defined under "ApplicationConstants" in map file
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] hoverTime</b> int, milliseconds to hover
		 * </ul>
		 * @return true if hover succeeds, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = SAFSPlus.HoverScreenLocation("500, 300", "20");
		 * boolean success = SAFSPlus.HoverScreenLocation("locKey", "20");//locKey="500, 300" defined in map file under "ApplicationConstants"
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean HoverScreenLocation(String coordination, String... optionals){
			return actionGUILess(GenericMasterFunctions.HOVERSCREENLOCATION_KEYWORD, combineParams(optionals, replaceSeparator(coordination)));
		}
		/**
		 * Verify the current contents of a binary (image) file with a benchmark file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyBinaryFileToFile">Detailed Reference</a>
		 * @param benchFile String, File used as the comparison benchmark.
		 * @param actualFile String, File used as the comparison file under test.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] FilterMode</b> String, one of FileUtilities.FilterMode. FilterMode.TOLERANCE is valid only when the binary files are images.<br>
		 * <b>optionals[1] FilterOptions</b> int, if the FilterMode is FilterMode.TOLERANCE, a number between 0 and 100, 
		 *                                        the percentage of bits need to be the same.
		 *                                        100 means only 100% match, 2 images will be considered matched;
		 *                                        0 means even no bits match, 2 images will be considered matched.<br>
		 *                                   other type, if the FilterMode is FilterMode.XXX<br>
		 * </ul>
		 * @return true if the 2 files contain the same content, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = SAFSPlus.VerifyBinaryFileToFile("signIn.png", "signIn.png");
		 * boolean success = SAFSPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png");
		 * boolean success = SAFSPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png", FilterMode.TOLERANCE.name, "90");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyBinaryFileToFile(String benchFile, String actualFile, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYBINARYFILETOFILE_KEYWORD, combineParams(optionals, benchFile, actualFile));
		}
		/**
		 * Verify the current contents of a text file with a benchmark file (same as VerifyTextFileToFile).
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyFileToFile">Detailed Reference</a>
		 * @param benchFile String, File used as the comparison benchmark.
		 * @param actualFile String, File used as the comparison file under test.
		 * @param optionals -- NOT used yet
		 * @return true if the 2 files contain the same content, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = SAFSPlus.VerifyFileToFile("benchFile.txt", "actualFile.txt");
		 * boolean success = SAFSPlus.VerifyFileToFile("c:\bench\benchFile.txt", "d:\test\actualFile.txt");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyFileToFile(String benchFile, String actualFile, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYFILETOFILE_KEYWORD, combineParams(optionals, benchFile, actualFile));
		}
		/**
		 * Verify the current contents of a text file with a benchmark file (same as VerifyFileToFile). 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyTextFileToFile">Detailed Reference</a>
		 * @param benchFile String, File used as the comparison benchmark.
		 * @param actualFile String, File used as the comparison file under test.
		 * @param optionals -- NOT used yet
		 * @return true if the 2 files contain the same content, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = SAFSPlus.VerifyTextFileToFile("benchFile.txt", "actualFile.txt");
		 * boolean success = SAFSPlus.VerifyTextFileToFile("c:\bench\benchFile.txt", "d:\test\actualFile.txt");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyTextFileToFile(String benchFile, String actualFile, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYTEXTFILETOFILE_KEYWORD, combineParams(optionals, benchFile, actualFile));
		}
		/**
		 * Verify that a string value contains a substring.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContains">Detailed Reference</a>
		 * @param wholeString String, the string value to verify.
		 * @param substring String, the substring
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if a string value does contain a substring, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * String label = SAFSPlus.GetVariableValue(labelVar);
		 * boolean success = SAFSPlus.VerifyValueContains(label, "labelContent");
		 * //or
		 * boolean success = SAFSPlus.VerifyValueContains("^"+labelVar, "labelContent");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValueContains(String wholeString, String substring, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUECONTAINS_KEYWORD, combineParams(optionals, wholeString, substring));
		}
		/**
		 * Verify that a string value contains a substring, ignoring case.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContainsIgnoreCase">Detailed Reference</a>
		 * @param wholeString String, the string value to verify.
		 * @param substring String, the substring
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if a string value does contain a substring ignoring case, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * boolean success = SAFSPlus.VerifyValueContainsIgnoreCase("^"+labelVar, "subcontent");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValueContainsIgnoreCase(String wholeString, String substring, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUECONTAINSIGNORECASE_KEYWORD, combineParams(optionals, wholeString, substring));
		}
		/**
		 * Verify that a string value does NOT contain a substring.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueDoesNotContain">Detailed Reference</a>
		 * @param wholeString String, the string value to verify.
		 * @param substring String, the substring
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if a string value does NOT contain a substring, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * boolean success = SAFSPlus.VerifyValueDoesNotContain("^"+labelVar, "substr");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValueDoesNotContain(String wholeString, String substring, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUEDOESNOTCONTAIN_KEYWORD, combineParams(optionals, wholeString, substring));
		}
		/**
		 * Verify that two string values are identical.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValues">Detailed Reference</a>
		 * @param value1 String, the first value to compare.
		 * @param value2 String, the second value to compare.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if the two values do equal, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * String label = SAFSPlus.GetVariableValue(labelVar);
		 * boolean success = SAFSPlus.VerifyValues(label, "labelContent");
		 * //or
		 * boolean success = SAFSPlus.VerifyValues("^"+labelVar, "labelContent");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValues(String value1, String value2, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUES_KEYWORD, combineParams(optionals, value1, value2));
		}
		/**
		 * Verify that two string values are identical, ignoring case.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesIgnoreCase">Detailed Reference</a>
		 * @param value1 String, the first value to compare.
		 * @param value2 String, the second value to compare.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if the two values do equal, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * boolean success = SAFSPlus.VerifyValuesIgnoreCase("^"+labelVar, "labelcontent");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValuesIgnoreCase(String value1, String value2, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUESIGNORECASE_KEYWORD, combineParams(optionals, value1, value2));
		}
		/**
		 * Verify that two string values are NOT identical.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesNotEqual">Detailed Reference</a>
		 * @param value1 String, the first value to compare.
		 * @param value2 String, the second value to compare.
		 * @param optionals
		 * <ul>
		 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
		 * </ul>
		 * @return true if the two values do NOT equal, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * String labelVar = "labelVariable";
		 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
		 * boolean success = SAFSPlus.VerifyValuesNotEqual("^"+labelVar, "labelContent");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyValuesNotEqual(String value1, String value2, String... optionals){
			return actionGUILess(GenericMasterFunctions.VERIFYVALUESNOTEQUAL_KEYWORD, combineParams(optionals, value1, value2));
		}
		
		/**
		 * Control-Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlClick">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = CtrlClick(Map.Google.Apps);//Control-Click at the center
		 * 2) boolean success = CtrlClick(Map.Google.Apps,"20,20");//Control-Click at the coordinate (20,20)
		 * 3) boolean success = CtrlClick(Map.Google.Apps,"20%,30%"); // Control-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = CtrlClick(Map.Google.Apps,"AppMapSubkey");//Control-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = CtrlClick(Map.Google.Apps,"20,20", "false");//Control-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CtrlClick(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.CTRLCLICK_KEYWORD, replaceSeparator(params));
		}
		
		/**
		 * Control-Right-Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlRightClick">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = CtrlRightClick(Map.Google.Apps);//Control-Right-Click at the center
		 * 2) boolean success = CtrlRightClick(Map.Google.Apps,"20,20");//Control-Right-Click at the coordinate (20,20)
		 * 3) boolean success = CtrlRightClick(Map.Google.Apps,"20%,30%"); // Control-Right-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = CtrlRightClick(Map.Google.Apps,"AppMapSubkey");//Control-Right-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = CtrlRightClick(Map.Google.Apps,"20,20", "false");//Control-Right-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CtrlRightClick(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.CTRLRIGHTCLICK_KEYWORD, replaceSeparator(params));
		}
		
		/**
		 * Double-Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_DoubleClick">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+,</b> params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = DoubleClick(Map.Google.Apps);//Double-Click at the center
		 * 2) boolean success = DoubleClick(Map.Google.Apps,"20,20");//Double-Click at the coordinate (20,20)
		 * 3) boolean success = DoubleClick(Map.Google.Apps,"20%,30%"); // Double-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = DoubleClick(Map.Google.Apps,"AppMapSubkey");//Double-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = DoubleClick(Map.Google.Apps,"20,20", "false");//Double-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean DoubleClick(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.DOUBLECLICK_KEYWORD, replaceSeparator(params));
		}	

		/**
		 * Right-Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_RightClick">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = RightClick(Map.Google.Apps);//Right-Click at the center
		 * 2) boolean success = RightClick(Map.Google.Apps,"20,20");//Right-Click at the coordinate (20,20)
		 * 3) boolean success = RightClick(Map.Google.Apps,"20%,30%"); // Right-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = RightClick(Map.Google.Apps,"AppMapSubkey");//Right-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = RightClick(Map.Google.Apps,"20,20", "false");//Right-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightClick(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.RIGHTCLICK_KEYWORD, replaceSeparator(params));
		}

		/**
		 * A left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_LeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = LeftDrag(Map.Google.Apps,"3,10,12,20");//Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = LeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = LeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean LeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.LEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A Shift left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_ShiftLeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ShiftLeftDrag(Map.Google.Apps,"3,10,12,20");//Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = ShiftLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = ShiftLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ShiftLeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.SHIFTLEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A Ctrl Shift left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlShiftLeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CtrlShiftLeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.CTRLSHIFTLEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A Ctrl left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlLeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = CtrlLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CtrlLeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.CTRLLEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A Alt left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_AltLeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = AltLeftDrag(Map.Google.Apps,"3,10,12,20");//Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = AltLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = AltLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean AltLeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.ALTLEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A Ctrl Alt left mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlAltLeftDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CtrlAltLeftDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.CTRLALTLEFTDRAG_KEYWORD, coordinates);
		}
		/**
		 * A right mouse drag is performed on the object based on the stored coordinates relative to this object. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_RightDrag">Detailed Reference</a>
		 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
		 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = RightDrag(Map.Google.Apps,"3,10,12,20");//Right-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = RightDrag(Map.Google.Apps,"Coords=3,10,12,20");//Right-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
		 * boolean success = RightDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
		 * //one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightDrag(org.safs.model.Component comp, String coordinates){
			return action(comp, GenericObjectFunctions.RIGHTDRAG_KEYWORD, coordinates);
		}
		
		/**
		 * Shift-Click on any visible component. 
		 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_ShiftClick">Detailed Reference</a>
		 * @param comp -- Component (from App Map) to Click
		 * @param params optional
		 * <ul>
		 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
		 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
		 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
		 *                                        if not provided, the default value is true.
		 * </ul>
		 * @return true if successfully executed, false otherwise.<p>
		 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
		 * @example	 
		 * <pre>
		 * {@code
		 * 1) boolean success = ShiftClick(Map.Google.Apps);//Shift-Click at the center
		 * 2) boolean success = ShiftClick(Map.Google.Apps,"20,20");//Shift-Click at the coordination (20,20)
		 * 3) boolean success = ShiftClick(Map.Google.Apps,"20%,30%"); // Shift-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
		 * 4) boolean success = ShiftClick(Map.Google.Apps,"AppMapSubkey");//Shift-Click at the coordination defined by entry "AppMapSubkey" in App Map.
		 * 5) boolean success = ShiftClick(Map.Google.Apps,"20,20", "false");//Shift-Click at the coordination (20,20) and web-element will not be scrolled into view automatically
		 *  // one of the above and then,
		 * int rc = prevResults.getStatusCode();      // if useful
		 * String info = prevResults.getStatusInfo(); // if useful
		 * }
		 * 
		 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
		 * 
		 * </pre>	 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ShiftClick(org.safs.model.Component comp, String... params){
			return action(comp, GenericObjectFunctions.SHIFTCLICK_KEYWORD, replaceSeparator(params));
		}
		
	}

	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBox keywords</a>, like Select, ShowList, SetTextValue etc.<br>
	 */
	public static class ComboBox extends Component{
		/**
		 * Select an item in Combo Box. Typically a verification that the item was selected is attempted.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_Select">Detailed Reference</a>
		 * @param combobox Component (from App Map) to Select item from.
		 * @param itemtext -- The combo box option to select.<br>
		 * @param extraParams optional
		 * <ul>
		 * <b>extraParams[0] forceRefresh</b> String, determine if force refreshing after selection. It is usually used when the 'id' of ComboBox is dynamic.<br>
		 * 									 true,  force refreshing <br>
		 * 									 false, not force refreshing <br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.Select(Map.Google.Combobox1, "ItemText");
		 * boolean success = ComboBox.Select(Map.Google.Combobox1, "ItemText", "true"); // force refreshing when dealing dynamic 'id' ComboBox			 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean Select(org.safs.model.Component combobox, String itemtext, String... extraParams){
			return action(combobox, ComboBoxFunctions.SELECT_KEYWORD, combineParams(extraParams, itemtext));
		}
		
		/**
		 * Select an item in Combo Box without verification of the selected item.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_SelectUnverified">Detailed Reference</a>
		 * @param combobox Component (from App Map) to Select item from.
		 * @param itemtext -- The combo box option to select.<br>
		 * @param extraParams optional
		 * <ul>
		 * <b>extraParams[0] forceRefresh</b> String, determine if force refreshing after selection. It is usually used when the 'id' of ComboBox is dynamic.<br>
		 * 									 true,  force refreshing <br>
		 * 									 false, not force refreshing <br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.SelectUnverified(Map.Google.Combobox1, "ItemText");		 
		 * boolean success = ComboBox.SelectUnverified(Map.Google.Combobox1, "ItemText", "true"); // force refreshing when dealing dynamic 'id' ComboBox
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverified(org.safs.model.Component combobox, String itemtext, String... extraParams){
			return action(combobox, ComboBoxFunctions.SELECTUNVERIFIED_KEYWORD, combineParams(extraParams, itemtext));
		}
		
		/**
		 * Select a text item in Combo Box using a partial substring match.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_SelectPartialMatch">Detailed Reference</a>
		 * @param combobox Component (from App Map) to Select item from.
		 * @param itemtext -- The combo box option to select, given as a substring of the option. Case Sensitive.<br>
		 * @param extraParams optional
		 * <ul>
		 * <b>extraParams[0] forceRefresh</b> String, determine if force refreshing after selection. It is usually used when the 'id' of ComboBox is dynamic.<br>
		 * 									 true,  force refreshing <br>
		 * 									 false, not force refreshing <br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.SelectPartialMatch(Map.Google.Combobox1, "substring");
		 * boolean success = ComboBox.SelectPartialMatch(Map.Google.Combobox1, "substring", "true"); // force refreshing when dealing dynamic 'id' ComboBox	 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectPartialMatch(org.safs.model.Component combobox, String itemtext, String... extraParams){
			return action(combobox, ComboBoxFunctions.SELECTPARTIALMATCH_KEYWORD, combineParams(extraParams, itemtext));
		}
		
		/**
		 * Select an item in Combo Box using a partial substring match, no verification of the 'selected item' will be attempted.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_SelectUnverifiedPartialMatch">Detailed Reference</a>
		 * @param combobox Component (from App Map) to Select item from.
		 * @param itemtext -- The combo box option to select, given as a substring of the option. Case Sensitive.<br>
		 * @param extraParams optional
		 * <ul>
		 * <b>extraParams[0] forceRefresh</b> String, determine if force refreshing after selection. It is usually used when the 'id' of ComboBox is dynamic.<br>
		 * 									 true,  force refreshing <br>
		 * 									 false, not force refreshing <br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.SelectUnverifiedPartialMatch(Map.Google.Combobox1, "PartialItemText");		 
		 * boolean success = ComboBox.SelectUnverifiedPartialMatch(Map.Google.Combobox1, "PartialItemText", "true"); // force refreshing when dealing dynamic 'id' ComboBox
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedPartialMatch(org.safs.model.Component combobox, String itemtext, String... extraParams){
			return action(combobox, ComboBoxFunctions.SELECTUNVERIFIEDPARTIALMATCH_KEYWORD, combineParams(extraParams, itemtext));
		}
		
		/**
		 * Select an item in Combo Box by index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_SelectIndex">Detailed Reference</a>
		 * @param combobox Component (from App Map) to Select item from.
		 * @param index -- the 1-based item index to select.<br>
		 * @param extraParams optional
		 * <ul>
		 * <b>extraParams[0] forceRefresh</b> String, determine if force refreshing after selection. It is usually used when the 'id' of ComboBox is dynamic.<br>
		 * 									 true,  force refreshing <br>
		 * 									 false, not force refreshing <br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.SelectIndex(Map.Google.Combobox1, 3);		 
		 * boolean success = ComboBox.SelectIndex(Map.Google.Combobox1, 3, "true"); // force refreshing when dealing dynamic 'id' ComboBox 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectIndex(org.safs.model.Component combobox, int index, String... extraParams){
			return action(combobox, ComboBoxFunctions.SELECTINDEX_KEYWORD, combineParams(extraParams, String.valueOf(index)));
		}
		
		/**
		 * Verify specific item in Combo Box is selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_VerifySelected">Detailed Reference</a>
		 * @param combobox Component (from App Map) to verify.
		 * @param item -- text item expected to be selected.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.VerifySelected(Map.Google.Combobox1,"SelectItem");		 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifySelected(org.safs.model.Component combobox, String item){
			return action(combobox, ComboBoxFunctions.VERIFYSELECTED_KEYWORD, item);
		}
		
		/**
		 * Hide the combo box list.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_HideList">Detailed Reference</a>
		 * @param combobox Component (from App Map).
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.HideList(Map.Google.Combobox1);
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean HideList(org.safs.model.Component combobox){
			return action(combobox, ComboBoxFunctions.HIDELIST_KEYWORD);
		}
		
		/**
		 * Show the combo box list.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_ShowList">Detailed Reference</a>
		 * @param combobox Component (from App Map).
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.ShowList(Map.Google.Combobox1);
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ShowList(org.safs.model.Component combobox){
			return action(combobox, ComboBoxFunctions.SHOWLIST_KEYWORD);
		}
		
		/**
		 * Capture all items in Combo Box to a file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ComboBoxFunctionsReference.htm#detail_CaptureItemsToFile">Detailed Reference</a>
		 * @param combobox (from App Map) to get the content.
		 * @param filename String, The filename to save the combo box's data
		 * @param params optional
		 * <ul>
		 * <b>params[0] encoding</b> String, The file encoding.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ComboBox.CaptureItemsToFile(Map.Google.Combobox1,"filename");
		 * boolean success = ComboBox.CaptureItemsToFile(Map.Google.Combobox1,"c:\\filename.txt","UTF-8");		 
		 * }
		 * <p>
		 * File will be created into Actuals project dir or user define location.
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CaptureItemsToFile(org.safs.model.Component combobox, String filename, String... params){
			return action(combobox, ComboBoxFunctions.CAPTUREITEMSTOFILE_KEYWORD, combineParams(params, filename));
		}
		
		/**
		 * Set text value in Combo box.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/SAFSReference.php?lib=ComboBoxFunctions&cmd=SetTextValue">Detailed Reference</a>
		 * 
		 * @param combobox org.safs.model.Component, 	the Combo box Component get from App map file.
		 * @param value String, 					 	the value of content, which is entered into Combo box.
		 * 
		 * @return true,	if successful.
		 * 		   false,	otherwise.
		 * 
		 * @example
		 * <pre>
		 * {@code
		 *         boolean success = ComboBox.SetTextValue(Map.Google.Combobox1,"Some Text");
		 *         boolean success = ComboBox.SetTextValue(Map.Google.Combobox1,"Some Text with special keys +(abcd)");		 
		 * } 
		 * </pre>
		 * 
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetTextValue(org.safs.model.Component combobox, String value){
			return action(combobox, ComboBoxFunctions.SETTEXTVALUE_KEYWORD, value);
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/ScrollBarFunctionsIndex.htm">ScrollBar keywords</a>, like OneDown, PageDown, PageUp etc.<br>
	 */
	public static class ScrollBar  extends Component{
		/**
		 * Attempts to perform a ScrollLineDown on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_OneDown">Detailed Reference</a>
		 * @param scrollbar Component (from App Map), a vertical scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] steps</b> int, The steps to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.OneDown(Map.Window.ScrollBar);//move 1 step down
		 * boolean success = ScrollBar.OneDown(Map.Window.ScrollBar, "5");//move 5 steps down
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean OneDown(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.ONEDOWN_KEYWORD, params);
		}
		/**
		 * Attempts to perform a ScrollLineUp on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_OneUp">Detailed Reference</a>
		 * @param scrollbar Component (from App Map), a vertical scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] steps</b> int, The steps to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.OneUp(Map.Window.ScrollBar);//move 1 step up
		 * boolean success = ScrollBar.OneUp(Map.Window.ScrollBar, "5");//move 5 steps up
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean OneUp(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.ONEUP_KEYWORD, params);
		}
		/**
		 * Attempts to perform a ScrollLeft on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_OneLeft">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a horizontal scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] steps</b> int, The steps to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.OneLeft(Map.Window.ScrollBar);//move 1 step Left
		 * boolean success = ScrollBar.OneLeft(Map.Window.ScrollBar, "5");//move 5 steps Left
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean OneLeft(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.ONELeft_KEYWORD, params);
		}
		
		/**
		 * Attempts to perform a ScrollRight on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_OneRight">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a horizontal scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] steps</b> int, The steps to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.OneRight(Map.Window.ScrollBar);//move 1 step Right
		 * boolean success = ScrollBar.OneRight(Map.Window.ScrollBar, "5");//move 5 steps Right
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean OneRight(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.ONERIGHT_KEYWORD, params);
		}
		
		/**
		 * Attempts to perform a ScrollPageDown on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_PageDown">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a vertical scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] pages</b> int, The pages to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.PageDown(Map.Window.ScrollBar);//move 1 page down
		 * boolean success = ScrollBar.PageDown(Map.Window.ScrollBar, "5");//move 5 pages down
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean PageDown(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.PAGEDOWN_KEYWORD, params);
		}
		/**
		 * Attempts to perform a ScrollPageUp on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_PageUp">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a vertical scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] pages</b> int, The pages to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.PageUp(Map.Window.ScrollBar);//move 1 page up
		 * boolean success = ScrollBar.PageUp(Map.Window.ScrollBar, "5");//move 5 pages up
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean PageUp(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.PAGEUP_KEYWORD, params);
		}
		/**
		 * Attempts to perform a ScrollPageLeft on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_PageLeft">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a horizontal scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] pages</b> int, The pages to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.PageLeft(Map.Window.ScrollBar);//move 1 page Left
		 * boolean success = ScrollBar.PageLeft(Map.Window.ScrollBar, "5");//move 5 pages Left
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean PageLeft(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.PAGELEFT_KEYWORD, params);
		}
		
		/**
		 * Attempts to perform a ScrollPageRight on a scrollbar.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ScrollBarFunctionsReference.htm#detail_PageRight">Detailed Reference</a><p>
		 * @param scrollbar Component (from App Map), a horizontal scrollbar.
		 * @param params optional
		 * <ul>
		 * <b>params[0] pages</b> int, The pages to scroll, must be positive.<br>
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ScrollBar.PageRight(Map.Window.ScrollBar);//move 1 page Right
		 * boolean success = ScrollBar.PageRight(Map.Window.ScrollBar, "5");//move 5 pages Right
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean PageRight(org.safs.model.Component scrollbar, String...params){
			return action(scrollbar, ScrollBarFunctions.PAGERIGHT_KEYWORD, params);
		}
		
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/CheckBoxFunctionsIndex.htm">CheckBox keywords</a>, like Check, UnCheck.<br>
	 */
	public static class CheckBox extends Component{
		/**
		 * Check a check-box. Typically a verification that the checkbox was checked is attempted.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/CheckBoxFunctionsReference.htm#detail_Check">Detailed Reference</a><p>
		 * @param checkbox Component (from App Map).
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = CheckBox.Check(Map.Google.Checkbox1);		 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean Check(org.safs.model.Component checkbox){
			return action(checkbox, CheckBoxFunctions.CHECK_KEYWORD);
		}
		
		/**
		 * UnCheck a check-box. Typically a verification that the checkbox was un-checked is attempted.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/CheckBoxFunctionsReference.htm#detail_UnCheck">Detailed Reference</a><p>
		 * @param checkbox Component (from App Map).
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = CheckBox.UnCheck(Map.Google.Checkbox1);			 
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean UnCheck(org.safs.model.Component checkbox){
			return action(checkbox, CheckBoxFunctions.UNCHECK_KEYWORD);
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/EditBoxFunctionsIndex.htm">EditBox keywords</a>, like SetTextValue, SetTextCharacters etc.<br>
	 */
	public static class EditBox extends Component{

		/**
		 * Set the text of edit box with verifying. The text only be treated as
		 * plain text, without special keywords dealing.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/EditBoxFunctionsReference.htm#detail_SetTextCharacters">Detailed Reference</a><p>
		 * @param editbox org.safs.model.Component, the component(from App Map) editbox being set the content.
		 * @param value text String, value of setting content.
		 * @return true if successful, false otherwise.
		 * @example 
		 * Set "textvalue" into Map.Google.Combobox1 with verification.
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetTextCharacters(Map.Google.Combobox1, "textvalue");
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetTextCharacters(org.safs.model.Component editbox, String value) {
			return action(editbox, EditBoxFunctions.SETTEXTCHARACTERS_KEYWORD, value);
		}
		
		/**
		 * Set the text of edit box without verifying. The text only be treated as
		 * plain text, without special keywords dealing.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/EditBoxFunctionsReference.htm#detail_SetUnverifiedTextCharacters">Detailed Reference</a><p>
		 * @param editbox org.safs.model.Component, the component(from App Map) editbox being set the content.
		 * @param value text String, value of setting content.
		 * @return true if successful, false otherwise.
		 * @example 
		 * Set "textvalue" into Map.Google.Combobox1 without verification.
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetUnverifiedTextCharacters(Map.Google.Combobox1, "textvalue");
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetUnverifiedTextCharacters(org.safs.model.Component editbox, String value) {
			return action(editbox,  EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD, value);
		}
		
		/**
		 * Enter text value to EditBox with verifying. The special key will be dealt. Moreover, if there's special key, verification will NOT happen.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/EditBoxFunctionsReference.htm#detail_SetTextValue">Detailed Reference</a><p>
		 * @param editbox org.safs.model.Component, the component(from App Map) editbox being set the content. 
		 * @param value text String, value of setting content.
		 * @return true if successful, false otherwise.
		 * @example
		 * 1. Set "textvalue" into Map.Google.Combobox1 with verification.
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetTextValue(Map.Google.Combobox1, "textvalue");		
		 * }
		 * </pre>
		 * 2. Set special key "^(v)", which means "Ctrl + v", into Map.Google.Combobox1 with verification.
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetTextValue(Map.Google.Combobox1, "^(v)");
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetTextValue(org.safs.model.Component editbox, String value){
			return action(editbox, EditBoxFunctions.SETTEXTVALUE_KEYWORD, value);
		}
		
		/**		 
		 * Enter text value to EditBox without verifying. The special key will be dealt. Moreover, if there's special key, verification will NOT happen.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/EditBoxFunctionsReference.htm#detail_SetUnverifiedTextValue ">Detailed Reference</a><p>
		 * @param editbox org.safs.model.Component, the component(from App Map) editbox being set the content. 
		 * @param value text String, value of setting content.
		 * @return true if successful, false otherwise.
		 * @example
		 * 1. Set "textvalue" into Map.Google.Combobox1 without verification..
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetUnverifiedTextValue(Map.Google.Combobox1, "textvalue");
		 * }
		 * </pre>
		 * 2. Set special key "^(v)", which means "Ctrl + v", into Map.Google.Combobox1 without verification..
		 * <pre>
		 * {@code
		 * boolean success = EditBox.SetUnverifiedTextValue(Map.Google.Combobox1, "^(v)");
		 * }
		 * </pre>
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetUnverifiedTextValue(org.safs.model.Component editbox, String value) {
			return action(editbox,  EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD, value);
		}
	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/TreeViewFunctionsIndex.htm">Tree keywords</a>, like ClickTextNode, ExpandTextNode etc.<br>
	 * <pre>
	 * By default, all parameters will be processed as an expression (math and string). As the parameter
	 * tree-path may contain separator "->", for example "Root->Child1->GrandChild", it will be evaluated 
	 * and 0 will be returned as parameter, this is not expected by user. To avoid the evaluation of
	 * expression, PLEASE CALL
	 * 
	 * {@code
	 * Misc.Expressions(false);
	 * }
	 * </pre>
	 */
	public static class Tree extends Component{
		
		/**
		 * Copy the current contents of a tree or a branch to a file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_CaptureTreeDataToFile">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param filename String, The filename to store the tree's content<br>
		 * @param params Optional
		 * <ul>
		 * <b>params[0] treeBranchName</b>, String, The full name of the tree branch to capture.<br>
		 * <b>params[1] indentChar</b>, String, The character(s) to use in the output file to indent the tree nodes from the parent tree branches.<br> 
		 * <b>params[2] encoding</b>, String, Specify a character encoding to be used when saving data to a file.<br> 
		 * </ul>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.CaptureTreeDataToFile(Map.Google.Tree, "D:\data\tree.dat");
		 * boolean success = Tree.CaptureTreeDataToFile(Map.Google.Tree, "D:\data\grandchild.dat", "Root->Child1->GrandChild");
		 * boolean success = Tree.CaptureTreeDataToFile(Map.Google.Tree, "D:\data\grandchild2.dat", "Root->Child1->GrandChild", "-", "UTF-8");
		 * }
		 * </pre>
		 */
		public static boolean CaptureTreeDataToFile(org.safs.model.Component tree, String filename, String... params){
			return action(tree, TreeViewFunctions.CAPTURETREEDATATOFILE_KEYWORD, combineParams(params, filename));
		}
		
		/**
		 * Click a node according to a partial match of its path value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click, the string may be part of node text, it is case-sensitive.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickPartial(Map.Google.Tree, "Roo->Chi->randChi");
		 * }
		 * </pre>
		 */
		public static boolean ClickPartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.CLICKPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Click a node according to a partial match of its path value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click, the string may be part of node text, it is case-sensitive.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickPartial(Map.Google.Tree, "Roo->Chi->randChi", 3);
		 * }
		 * </pre>
		 */
		public static boolean ClickPartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.CLICKPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Click a node according to its path and verified the node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean ClickTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.CLICKTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Click the Nth node according to its path and verified the node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean ClickTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.CLICKTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}

		/**
		 * Click a node according to its path, but will not verify the node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean ClickUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.CLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Click the Nth node according to its path, but will not verify the node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean ClickUnverifiedTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.CLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Collapse a node according to a partial match of its path value, and verify this node has been collapsed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_CollapsePartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to collapse, the string may be part of node text, it is case-sensitive.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.CollapsePartial(Map.Google.Tree, "Roo->Chi->randChi");
		 * }
		 * </pre>
		 */
		public static boolean CollapsePartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.COLLAPSEPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Collapse a node according to a partial match of its path value, and verify this node has been collapsed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_CollapsePartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to collapse, the string may be part of node text, it is case-sensitive.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.CollapsePartial(Map.Google.Tree, "Roo->Chi->randChi", 3);
		 * }
		 * </pre>
		 */
		public static boolean CollapsePartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.COLLAPSEPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Collapse a node according to its path, and verify this node has been collapsed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Collapse">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Collapse(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean Collapse(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.COLLAPSE_KEYWORD, treepath);
		}
		/**
		 * Collapse the Nth node according to its path, and verify this node has been collapsed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Collapse">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Collapse(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean Collapse(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.COLLAPSE_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Collapse a node according to its path, but will NOT verify this node has been collapsed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_CollapseUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.CollapseUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean CollapseUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.COLLAPSEUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		
		/**
		 * Click a node according to its path, at the same time the key 'CTRL' is pressed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_CtrlClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.CtrlClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean CtrlClickUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.CTRLCLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}

		/**
		 * Double click a node according to a partial match of its path value, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickPartial(Map.Google.Tree, "Root->Chil->GrandChi");
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickPartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.DOUBLECLICKPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Double click the Nth node according to a partial match of its path value, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickPartial(Map.Google.Tree, "Roo->Child->GrandChi", 3);
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickPartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.DOUBLECLICKPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Double click a node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.DOUBLECLICKTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Double Click the Nth node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.DOUBLECLICKTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Double click a node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.DOUBLECLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Double Click the Nth node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_DoubleClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.DoubleClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean DoubleClickUnverifiedTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.DOUBLECLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		
		/**
		 * Expand a node according to its path, and verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Expand">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Expand(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean Expand(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.EXPAND_KEYWORD, treepath);
		}
		/**
		 * Expand the Nth node according to its path, and verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Expand">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Expand(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean Expand(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.EXPAND_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Expand a node according to a partial match of its path value, and verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ExpandPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ExpandPartial(Map.Google.Tree, "Roo->Chil->andChild");
		 * }
		 * </pre>
		 */
		public static boolean ExpandPartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.EXPANDPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Expand the Nth node according to a partial match of its path value, and verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ExpandPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ExpandPartial(Map.Google.Tree, "Roo->Chil->GrandCh", 3);
		 * }
		 * </pre>
		 */
		public static boolean ExpandPartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.EXPANDPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Expand a node according to its path, but will NOT verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ExpandUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ExpandUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean ExpandUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.EXPANDUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Expand the Nth node according to its path, but will NOT verify this node has been expanded.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ExpandUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to expand.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ExpandUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean ExpandUnverifiedTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.EXPANDUNVERIFIEDTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Right click a node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean RightClickTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.RIGHTCLICKTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Right click the Nth node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean RightClickTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.RIGHTCLICKTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Right click a node according to a partial match of its path value, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickPartial(Map.Google.Tree, "Root->Chil->andChi");
		 * }
		 * </pre>
		 */
		public static boolean RightClickPartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.RIGHTCLICKPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Right click the Nth node according to a partial match of its path value, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickPartial(Map.Google.Tree, "Roo->Chil->GrandChi", 3);
		 * }
		 * </pre>
		 */
		public static boolean RightClickPartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.RIGHTCLICKPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Right click a node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean RightClickUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.RIGHTCLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Right click the Nth node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_RightClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.RightClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean RightClickUnverifiedTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.RIGHTCLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Select a node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Select">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Select(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean Select(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.SELECT_KEYWORD, treepath);
		}
		/**
		 * Select the Nth node according to its path, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_Select">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.Select(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean Select(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.SELECT_KEYWORD, treepath, String.valueOf(matchIndex));
		}		
		/**
		 * Select a node according to a partial match of its path value, and verify this node has been selected.
         * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SelectPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SelectPartial(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean SelectPartial(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.SELECTPARTIAL_KEYWORD, treepath);
		}
		/**
		 * Select the Nth node according to a partial match of its path value, and verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SelectPartial">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SelectPartial(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean SelectPartial(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.SELECTPARTIAL_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		/**
		 * Select a node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SelectUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SelectUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean SelectUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.SELECTUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		/**
		 * Select the Nth node according to its path, but will NOT verify this node has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SelectUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to select.<br>
		 * @param matchIndex int, index of the Nth duplicate item to match.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SelectUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild", 3);
		 * }
		 * </pre>
		 */
		public static boolean SelectUnverifiedTextNode(org.safs.model.Component tree, String treepath, int matchIndex){
			return action(tree, TreeViewFunctions.SELECTUNVERIFIEDTEXTNODE_KEYWORD, treepath, String.valueOf(matchIndex));
		}
		
		/**
		 * Verify the existence of node according to its path, and set true to a variable if node exists, false if not.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SetTreeContainsNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @param variable String, the variable to store the existence of the node<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SetTreeContainsNode(Map.Google.Tree, "Root->Child1->GrandChild", "nodeIsFound");
		 * }
		 * </pre>
		 */
		public static boolean SetTreeContainsNode(org.safs.model.Component tree, String treepath, String variable){
			return action(tree, TreeViewFunctions.SETTREECONTAINSNODE_KEYWORD, treepath, variable);
		}
		/**
		 * Verify the existence of node according to a partial match of its path value, and set true to a variable if node exists, false if not.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SetTreeContainsPartialMatch">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @param variable String, the variable to store the existence of the node<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.SetTreeContainsPartialMatch(Map.Google.Tree, "Root->Chil->GrandChi", "nodeIsFound");
		 * }
		 * </pre>
		 */
		public static boolean SetTreeContainsPartialMatch(org.safs.model.Component tree, String treepath, String variable){
			return action(tree, TreeViewFunctions.SETTREECONTAINSPARTIALMATCH_KEYWORD, treepath, variable);
		}
		
		/**
		 * Click a node according to its path, at the same time the key 'SHIFT' is pressed.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ShiftClickUnverifiedTextNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to click.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.ShiftClickUnverifiedTextNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean ShiftClickUnverifiedTextNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.SHIFTCLICKUNVERIFIEDTEXTNODE_KEYWORD, treepath);
		}
		
		/**
		 * Verify the selection of a node according to its path, node should be unselected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_VerifyNodeUnselected">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.VerifyNodeUnselected(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean VerifyNodeUnselected(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.VERIFYNODEUNSELECTED_KEYWORD, treepath);
		}		
		/**
		 * Verify the selection of a node according to its path, node should be selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_VerifySelectedNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.VerifySelectedNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean VerifySelectedNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.VERIFYSELECTEDNODE_KEYWORD, treepath);
		}		
		/**
		 * Verify the existence of a node according to its path.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_VerifyTreeContainsNode">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.VerifyTreeContainsNode(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean VerifyTreeContainsNode(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.VERIFYTREECONTAINSNODE_KEYWORD, treepath);
		}		
		/**
		 * Verify the selection of a node according to a partial match of its path value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_VerifyTreeContainsPartialMatch">Detailed Reference</a><p>
		 * @param tree Component (from App Map).
		 * @param treepath String, The tree path to verify.<br>
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * boolean success = Tree.VerifyTreeContainsPartialMatch(Map.Google.Tree, "Root->Child1->GrandChild");
		 * }
		 * </pre>
		 */
		public static boolean VerifyTreeContainsPartialMatch(org.safs.model.Component tree, String treepath){
			return action(tree, TreeViewFunctions.VERIFYTREECONTAINSPARTIALMATCH_KEYWORD, treepath);
		}
		
		/**
		 * Select tree text node.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_SelectTextNode">Detailed Reference</a><p>
		 * @param Tree Component (from App Map) to get the content. 
		 * @param Tree text node, separated by "->". ex: main node->child node.
		 * @return
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * Tree.SelectTextNode(Map.Google.Tree1","node1->node2->node3");		
		 * }
		 * </pre>
		 */
		public static boolean SelectTextNode(org.safs.model.Component tree, String node){
			return action(tree, TreeViewFunctions.SELECTTEXTNODE_KEYWORD, quotePath(node));
		}
		
		/**
		 * Expand tree text node.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TreeViewFunctionsReference.htm#detail_ExpandTextNode">Detailed Reference</a><p>
		 * @param Tree Component (from App Map) to get the content. 
		 * @param Tree text node, separated by "->". ex: main node->child node.
		 * @return
		 * @example	 
		 * <pre>
		 * {@code
		 * Misc.Expressions(false);
		 * Tree.ExpandTextNode(Map.Google.Tree1","node1->node2->node3");		
		 * }
		 * </pre>
		 */
		public static boolean ExpandTextNode(org.safs.model.Component tree, String node){
			return action(tree, TreeViewFunctions.EXPANDPARTIALTEXTNODE_KEYWORD, quotePath(node));
		}

	}
	
	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/TabControlFunctionsIndex.htm">TabControl keywords</a>, like ClickTab, SelectTabIndex etc.<br>
	 */
	public static class TabControl extends Component{
		
		/**
		 * Select a tab value in Tab Control and verify the value has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_ClickTab">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param value String, text value
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.ClickTab(Map.SAPDemo.TabStrip,"Mort.Calc");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ClickTab(org.safs.model.Component tabcontrol, String value){
			return action(tabcontrol, TabControlFunctions.CLICKTAB_KEYWORD, value);
		}
		/**
		 * Select a tab value in Tab Control, the value will be matched partially.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_ClickTabContains">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param value String, partial text value to match
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.ClickTabContains(Map.SAPDemo.TabStrip,"Calc");//For tab 'Mort.Calc'		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ClickTabContains(org.safs.model.Component tabcontrol, String value){
			return action(tabcontrol, TabControlFunctions.CLICKTABCONTAINS_KEYWORD, value);
		}
		/**
		 * Select a tab value in Tab Control and verify the value has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_MakeSelection">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param value String, text value
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.MakeSelection(Map.SAPDemo.TabStrip,"Mort.Calc");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean MakeSelection(org.safs.model.Component tabcontrol, String value){
			return action(tabcontrol, TabControlFunctions.MAKESELECTION_KEYWORD, value);
		}
		/**
		 * Select a tab value in Tab Control and verify the value has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_SelectTab">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param value String, text value
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.SelectTab(Map.SAPDemo.TabStrip,"Mort.Calc");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTab(org.safs.model.Component tabcontrol, String value){
			return action(tabcontrol, TabControlFunctions.SELECTTAB_KEYWORD, value);
		}
		/**
		 * Select a tab by index in Tab Control and verify the index has been selected.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_SelectTabIndex">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param index int, the index to select, it is 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.SelectTabIndex(Map.SAPDemo.TabStrip,1);//Select the first tab	
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTabIndex(org.safs.model.Component tabcontrol, int index){
			return action(tabcontrol, TabControlFunctions.SELECTTABINDEX_KEYWORD, String.valueOf(index));
		}
		/**
		 * Select a tab value in Tab Control without verification.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/TabControlFunctionsReference.htm#detail_UnverifiedClickTab">Detailed Reference</a><p>
		 * @param tabcontrol Component (from App Map) to select a tab from. 
		 * @param value String, text value
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = TabControl.UnverifiedClickTab(Map.SAPDemo.TabStrip,"Mort.Calc");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean UnverifiedClickTab(org.safs.model.Component tabcontrol, String value){
			return action(tabcontrol, TabControlFunctions.UNVERIFIEDCLICKTAB_KEYWORD, value);
		}

	}

	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/ListViewFunctionsIndex.htm">ListView keywords</a>, like ClickIndex, VerifyListContains etc.<br>
	 */
	public static class ListView extends Component{
		
		//Don't make this public for now
		//Get list-item's id according to index
		static String GetItemID(org.safs.model.Component listview, int index){
			if(action(listview, "GetItemID", String.valueOf(index))){
				return prevResults.getStatusInfo();
			}else{
				return null;
			}
		}
		
		/**
		 * Double click an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateIndex">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.ActivateIndex(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateIndex(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.ACTIVATEINDEX_KEYWORD, String.valueOf(index));
		}
		/**
		 * Double click an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateIndexItem">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.ActivateIndexItem(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateIndexItem(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.ACTIVATEINDEXITEM_KEYWORD, String.valueOf(index));
		}
		/**
		 * Double click an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ClickIndex">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.ClickIndex(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ClickIndex(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.CLICKINDEX_KEYWORD, String.valueOf(index));
		}
		/**
		 * Double click an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ClickIndexItem">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.ClickIndexItem(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ClickIndexItem(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.CLICKINDEXITEM_KEYWORD, String.valueOf(index));
		}
		/**
		 * Select (single click) an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectIndex">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.SelectIndex(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectIndex(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.SELECTINDEX_KEYWORD, String.valueOf(index));
		}
		/**
		 * Select (single click) an item value in ListView according to an index.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectIndexItem">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.SelectIndexItem(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectIndexItem(org.safs.model.Component listview, int index){
			return action(listview, ListViewFunctions.SELECTINDEXITEM_KEYWORD, String.valueOf(index));
		}
		/**
		 * Select (single click) an item value in ListView according to an index at specific coordinates.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectIndexItemCoords">Detailed Reference</a><p>
		 * @param listview Component (from App Map) to select an item from. 
		 * @param index int, the index to select, 1-based.
		 * @param coords String, the coordinate relative to the top-left corner of the item.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = ListView.SelectIndexItemCoords(Map.SAPDemo.ListView,2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectIndexItemCoords(org.safs.model.Component listview, int index, String coords){
			return action(listview, ListViewFunctions.SELECTINDEXITEMCOORDS_KEYWORD, String.valueOf(index), replaceSeparator(coords)[0]);
		}
		
		/**
		 * Double click an item value in ListView according to a partial text.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivatePartialMatch">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param partialText String, the case-sensitive substring of text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item containing string 'zona'
		 * boolean success = ListView.ActivatePartialMatch(Map.SAPDemo.ListView,"zona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivatePartialMatch(org.safs.model.Component listview, String partialText, int matchIndex){
			return action(listview, ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD, partialText, String.valueOf(matchIndex));
		}
		/**
		 * Double click an item value in ListView according to a partial text. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivatePartialMatch">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param partialText String, the case-sensitive substring of text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item containing string 'zona'
		 * boolean success = ListView.ActivatePartialMatch(Map.SAPDemo.ListView,"zona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivatePartialMatch(org.safs.model.Component listview, String partialText){
			return action(listview, ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD, partialText);
		}
		/**
		 * Select (single click) an item value in ListView according to a partial text.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectPartialMatch">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param partialText String, the case-sensitive substring of text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item containing string 'zona'
		 * boolean success = ListView.SelectPartialMatch(Map.SAPDemo.ListView,"zona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectPartialMatch(org.safs.model.Component listview, String partialText, int matchIndex){
			return action(listview, ListViewFunctions.SELECTPARTIALMATCH_KEYWORD, partialText, String.valueOf(matchIndex));
		}
		/**
		 * Select (single click) an item value in ListView according to a partial text. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectPartialMatch">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param partialText String, the case-sensitive substring of text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item containing string 'zona'
		 * boolean success = ListView.SelectPartialMatch(Map.SAPDemo.ListView,"zona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectPartialMatch(org.safs.model.Component listview, String partialText){
			return action(listview, ListViewFunctions.SELECTPARTIALMATCH_KEYWORD, partialText);
		}
		
		/**
		 * Double click an item value in ListView according to a full text.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona'
		 * boolean success = ListView.ActivateTextItem(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateTextItem(org.safs.model.Component listview, String text, int matchIndex){
			return action(listview, ListViewFunctions.ACTIVATETEXTITEM_KEYWORD, text, String.valueOf(matchIndex));
		}
		/**
		 * Double click an item value in ListView according to a full text. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona'
		 * boolean success = ListView.ActivateTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.ACTIVATETEXTITEM_KEYWORD, text);
		}
		/**
		 * Double click a text item at specific Coords.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //double-click the 2th item whose text string is 'Arizona' at the coordinate (10,10)
		 * boolean success = ListView.ActivateTextItemCoords(Map.SAPDemo.ListView,"Arizona", "10;10", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateTextItemCoords(org.safs.model.Component listview, String text, String coords, int matchIndex){
			return action(listview, ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0], String.valueOf(matchIndex));
		}
		/**
		 * Double click a text item at specific Coords.If there are more than one matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //double-click the first item whose text string is 'Arizona' at the coordinate (10,10)
		 * boolean success = ListView.ActivateTextItemCoords(Map.SAPDemo.ListView,"Arizona", "10;10");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateTextItemCoords(org.safs.model.Component listview, String text, String coords){
			return action(listview, ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0]);
		}
		/**
		 * Select (single click) an item value in ListView according to a full text.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona'
		 * boolean success = ListView.SelectTextItem(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTextItem(org.safs.model.Component listview, String text, int matchIndex){
			return action(listview, ListViewFunctions.SELECTTEXTITEM_KEYWORD, text, String.valueOf(matchIndex));
		}
		/**
		 * Select (single click) an item value in ListView according to a full text. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona'
		 * boolean success = ListView.SelectTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.SELECTTEXTITEM_KEYWORD, text);
		}
		/**
		 * Select (single click) an item value in ListView according to a full text at specific coordinate.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona' by clicking at (5,5) of the item.
		 * boolean success = ListView.SelectTextItemCoords(Map.SAPDemo.ListView,"Arizona", "5;5", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTextItemCoords(org.safs.model.Component listview, String text, String coords, int matchIndex){
			return action(listview, ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0], String.valueOf(matchIndex));
		}
		/**
		 * Select (single click) an item value in ListView according to a full text at specific coordinate.<br> 
		 * If there are more than one matched item, select the first one.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona' by clicking at (5,5) of the item.
		 * boolean success = ListView.SelectTextItemCoords(Map.SAPDemo.ListView,"Arizona", "5;5");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectTextItemCoords(org.safs.model.Component listview, String text, String coords){
			return action(listview, ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0]);
		}
		
		/**
		 * Double click an item value in ListView according to a full text without verification.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateUnverifiedTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona', will not verify "Arizona" is selected
		 * boolean success = ListView.ActivateUnverifiedTextItem(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateUnverifiedTextItem(org.safs.model.Component listview, String text, int matchIndex){
			return action(listview, ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD, text, String.valueOf(matchIndex));
		}
		/**
		 * Double click an item value in ListView according to a full text without verification. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateUnverifiedTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona', will not verify "Arizona" is selected
		 * boolean success = ListView.ActivateUnverifiedTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateUnverifiedTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD, text);
		}
		/**
		 * Double click an item at specific coordinate in ListView according to a full text without verification.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateUnverifiedTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //double-click the 2th item whose text string is 'Arizona' at the coordinate (10,10)
		 * boolean success = ListView.ActivateUnverifiedTextItemCoords(Map.SAPDemo.ListView,"Arizona", "10;10", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateUnverifiedTextItemCoords(org.safs.model.Component listview, String text, String coords, int matchIndex){
			return action(listview, ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0], String.valueOf(matchIndex));
		}
		/**
		 * Double click an item at specific coordinate in ListView according to a full text without verification.<br>
		 * If there are more than one matched item, select the first one.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ActivateUnverifiedTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //double-click the first item whose text string is 'Arizona' at the coordinate (10,10)
		 * boolean success = ListView.ActivateUnverifiedTextItemCoords(Map.SAPDemo.ListView,"Arizona", "10;10");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ActivateUnverifiedTextItemCoords(org.safs.model.Component listview, String text, String coords){
			return action(listview, ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0]);
		}
		
		/**
		 * Select (single click) an item value in ListView according to a full text without verification.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectUnverifiedTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona', will not verify "Arizona" is selected
		 * boolean success = ListView.SelectUnverifiedTextItem(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedTextItem(org.safs.model.Component listview, String text, int matchIndex){
			return action(listview, ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD, text, String.valueOf(matchIndex));
		}
		/**
		 * Select (single click) an item value in ListView according to a full text without verification. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectUnverifiedTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona', will not verify "Arizona" is selected
		 * boolean success = ListView.SelectUnverifiedTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD, text);
		}
		/**
		 * Select (single click) an item value in ListView according to a full text at specific coordinate without verification.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectUnverifiedTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the 2th item whose text string is 'Arizona' by clicking at (5,5) of the item.
		 * boolean success = ListView.SelectUnverifiedTextItemCoords(Map.SAPDemo.ListView,"Arizona", "5;5", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedTextItemCoords(org.safs.model.Component listview, String text, String coords, int matchIndex){
			return action(listview, ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0], String.valueOf(matchIndex));
		}
		/**
		 * Select (single click) an item value in ListView according to a full text at specific coordinate without verification.<br> 
		 * If there are more than one matched item, select the first one.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectUnverifiedTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to select
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select the first item whose text string is 'Arizona' by clicking at (5,5) of the item.
		 * boolean success = ListView.SelectUnverifiedTextItemCoords(Map.SAPDemo.ListView,"Arizona", "5;5");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedTextItemCoords(org.safs.model.Component listview, String text, String coords){
			return action(listview, ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0]);
		}
		
		/**
		 * Get all text value of items in ListView, and save them to a file.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_CaptureItemsToFile">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param file String, the name of file to save list items. It can be absolute or relative to Test directory.
		 * @param encoding String, the encoding used for output file
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //save text value of all listview items to a file "C:\\Temp\\listContents.txt", the file will be encoded as "UTF-8".
		 * boolean success = ListView.CaptureItemsToFile(Map.SAPDemo.ListView,"C:\\Temp\\listContents.txt", "UTF-8");
		 * //will be save to <TestProjectDir>/Actuals/listContents.txt		
		 * boolean success = ListView.CaptureItemsToFile(Map.SAPDemo.ListView,"listContents.txt", "UTF-8");
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CaptureItemsToFile(org.safs.model.Component listview, String file, String encoding){
			return action(listview, ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD, file, encoding);
		}
		/**
		 * Get all text value of items in ListView, and save them to a file by system encoding.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_CaptureItemsToFile">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param file String, the name of file to save list items. It can be absolute or relative to Test directory.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //save text value of all listview items to a file "C:\\Temp\\listContents.txt", the file will be encoded as system-encoding.
		 * boolean success = ListView.CaptureItemsToFile(Map.SAPDemo.ListView,"C:\\Temp\\listContents.txt");
		 * //will be save to <TestProjectDir>/Actuals/listContents.txt
		 * boolean success = ListView.CaptureItemsToFile(Map.SAPDemo.ListView,"listContents.txt");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean CaptureItemsToFile(org.safs.model.Component listview, String file){
			return action(listview, ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD, file);
		}
		
		/**
		 * Verify that an item is not selected in ListView.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_VerifyItemUnselected">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to verify an item is not selected.
		 * @param text String, the case-sensitive text item to verify
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //verify "Arizona" is not selected
		 * boolean success = ListView.VerifyItemUnselected(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyItemUnselected(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.VERIFYITEMUNSELECTED_KEYWORD, text);
		}
		
		/**
		 * Verify that an item is contained in ListView.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_VerifyListContains">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to verify an item is contained.
		 * @param text String, the case-sensitive text item to verify
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //verify "Arizona" is in listview
		 * boolean success = ListView.VerifyListContains(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyListContains(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.VERIFYLISTCONTAINS_KEYWORD, text);
		}
		
		/**
		 * Verify that an item is selected in ListView.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_VerifySelectedItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to verify an item is selected.
		 * @param text String, the case-sensitive text item to verify
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //verify "Arizona" is selected
		 * boolean success = ListView.VerifySelectedItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifySelectedItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.VERIFYSELECTEDITEM_KEYWORD, text);
		}
		/**
		 * Shift+Click and Verify a selection according to its text value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_ExtendSelectionToTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to select an item.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select "Arizona"
		 * boolean success = ListView.SelectTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * //extend-select "Florida", which will select all items between "Arizona" and "Florida"
		 * boolean success = ListView.ExtendSelectionToTextItem(Map.SAPDemo.ListView,"Florida");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean ExtendSelectionToTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.EXTENDSELECTIONTOTEXTITEM_KEYWORD, text);
		}
		/**
		 * RightClick (single click) an item value in ListView according to a full text.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_RightClickTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //RightClick the 2th item whose text string is 'Arizona'
		 * boolean success = ListView.RightClickTextItem(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightClickTextItem(org.safs.model.Component listview, String text, int matchIndex){
			return action(listview, ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD, text, String.valueOf(matchIndex));
		}
		/**
		 * RightClick (single click) an item value in ListView according to a full text. If there are more than one
		 * matched item, select the first one.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_RightClickTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //RightClick the first item whose text string is 'Arizona'
		 * boolean success = ListView.RightClickTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightClickTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD, text);
		}
		/**
		 * RightClick (single click) an item value in ListView according to a full text at specific coordinate.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_RightClickTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @param matchIndex int, allows to match item N in a list containing duplicate entries, 1-based.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //RightClick the 2th item whose text string is 'Arizona'
		 * boolean success = ListView.RightClickTextItemCoords(Map.SAPDemo.ListView,"Arizona", 2);		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightClickTextItemCoords(org.safs.model.Component listview, String text, String coords, int matchIndex){
			return action(listview, ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0], String.valueOf(matchIndex));
		}
		/**
		 * RightClick (single click) an item value in ListView according to a full text at specific coordinate.<br> 
		 * If there are more than one matched item, select the first one.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_RightClickTextItemCoords">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) to select an item from.
		 * @param text String, the case-sensitive text item to click
		 * @param coords String, the coordinate relative to the top-left corner of the item
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //RightClick the first item whose text string is 'Arizona'
		 * boolean success = ListView.RightClickTextItemCoords(Map.SAPDemo.ListView,"Arizona");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean RightClickTextItemCoords(org.safs.model.Component listview, String text, String coords){
			return action(listview, ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD, text, replaceSeparator(coords)[0]);
		}
		/**
		 * Control+Click on an item according to a partial text match.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectAnotherPartialMatch">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to select an item.
		 * @param partialText String, the case-sensitive substring of text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select "Arizona"
		 * boolean success = ListView.SelectTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * //select item containing "Flori", which will select item "Florida" while keeping "Arizona" selected
		 * boolean success = ListView.SelectAnotherPartialMatch(Map.SAPDemo.ListView,"Flori");	
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectAnotherPartialMatch(org.safs.model.Component listview, String partialText){
			return action(listview, ListViewFunctions.SELECTANOTHERPARTIALMATCH_KEYWORD, partialText);
		}
		/**
		 * Control+Click on an item by its text value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SelectAnotherTextItem">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to select an item.
		 * @param text String, the case-sensitive text item to select
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //select "Arizona"
		 * boolean success = ListView.SelectTextItem(Map.SAPDemo.ListView,"Arizona");		
		 * //select item containing "Florida", which will select item "Florida" while keeping "Arizona" selected
		 * boolean success = ListView.SelectAnotherTextItem(Map.SAPDemo.ListView,"Florida");			
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectAnotherTextItem(org.safs.model.Component listview, String text){
			return action(listview, ListViewFunctions.SELECTANOTHERTEXTITEM_KEYWORD, text);
		}
		/**
		 * Set a variable with the result of checking that a listview contains the provided item. 
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/ListViewFunctionsReference.htm#detail_SetListContains">Detailed Reference</a><p>
		 * @param listview Component, (from App Map) where to verify an item.
		 * @param text String, the case-sensitive text item to verify the existence
		 * @param variable String, the variable name to store the existence of an item. true or false.
		 * @return true if successful, false otherwise.
		 * @example
		 * <pre>
		 * {@code
		 * //verify if the item "Arizona" exists in the listview, 
		 * //the variable "existence" will be set to 'true' if 'Arizona' exists; 'false' otherwise.
		 * boolean success = ListView.SetListContains(Map.SAPDemo.ListView, "Arizona", "existence");
		 * String result = GetVariableValue("existence");
		 * System.out.println("The existence of item 'Arizona' is "+result);					
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SetListContains(org.safs.model.Component listview, String text, String variable){
			return action(listview, ListViewFunctions.SETLISTCONTAINS_KEYWORD, text, variable);
		}
	}

	/**
	 * Wrapper class providing APIs to handle <a href="http://safsdev.github.io/sqabasic2000/JavaMenuFunctionsIndex.htm">MenuBar/Menu keywords</a>, like SelectMenuItem, VerifyMenuItemContains etc.<br>
	 */	
	public static class Menu extends Component{
		/**
		 * Select a menuItem according to its text value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_SelectMenuItem">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to select an item from.
		 * @param path String, the path of the item to select
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Menu.SelectMenuItem(Map.SAPDemo.MenuBar,"Root->child");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectMenuItem(org.safs.model.Component menu, String path){
			return action(menu, JavaMenuFunctions.SELECTMENUITEM_KEYWORD, path);
		}

		/**
		 * Select a menuItem according to its text value, without verification of menuItem's existance.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_SelectUnverifiedMenuItem">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to select an item from.
		 * @param path String, the path of the item to select
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Menu.SelectUnverifiedMenuItem(Map.SAPDemo.MenuBar,"Root->child");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectUnverifiedMenuItem(org.safs.model.Component menu, String path){
			return action(menu, JavaMenuFunctions.SELECTUNVERIFIEDMENUITEM_KEYWORD, path);
		}

		/**
		 * Select a menuItem according to its text value, it will select the Nth matched item.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_SelectMenuItem">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to select an item from.
		 * @param path String, the path of the item to select.
		 * @param indexPath String, the index path for Nth matched item of each level.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * //select the 3th item matching "child" under the 2th item matching "Root"
		 * boolean success = Menu.SelectMenuItem(Map.SAPDemo.MenuBar,"Root->child", "2->3");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectMenuItem(org.safs.model.Component menu, String path, String indexPath){
			return action(menu, JavaMenuFunctions.SELECTMENUITEM_KEYWORD, path, indexPath);
		}
		/**
		 * Select a menuItem according to a partial match of its text value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_SelectMenuItemContains">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to select an item from.
		 * @param path String, the path of the item to select
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Menu.SelectMenuItemContains(Map.SAPDemo.MenuBar,"Root->child");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectMenuItemContains(org.safs.model.Component menu, String path){
			return action(menu, JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD, path);
		}
		
		/**
		 * Select a menuItem according to a partial match of its text value, it will select the Nth matched item.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_SelectMenuItemContains">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to select an item from.
		 * @param path String, the path of the item to select.
		 * @param indexPath String, the index path for Nth matched item of each level.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * //select the 3th item matching "child" under the 2th item matching "Root"
		 * boolean success = Menu.SelectMenuItemContains(Map.SAPDemo.MenuBar,"Root->child", "2->3");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean SelectMenuItemContains(org.safs.model.Component menu, String path, String indexPath){
			return action(menu, JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD, path, indexPath);
		}
		/**
		 * Verify the existence of a menuItem according to its text value.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_VerifyMenuItem">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to verify an item from.
		 * @param path String, the path of the item to verify
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Menu.VerifyMenuItem(Map.SAPDemo.MenuBar,"Root->child");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyMenuItem(org.safs.model.Component menu, String path){
			return action(menu, JavaMenuFunctions.VERIFYMENUITEM_KEYWORD, path);
		}
		
		/**
		 * Verify the existence of a menuItem according to its text value, it will verify the Nth matched item.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_VerifyMenuItem">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to verify an item from.
		 * @param path String, the path of the item to verify.
		 * @param expectedStatus String, the status of the item to verify.
		 * @param indexPath String, the index path for Nth matched item of each level.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * //verify the first matched "Root->child" is enabled
		 * boolean success = Menu.VerifyMenuItem(Map.SAPDemo.MenuBar,"Root->child", "Enabled", "");		
		 * //verify the 3th matched "child" under 2th matched "Root" exists
		 * boolean success = Menu.VerifyMenuItem(Map.SAPDemo.MenuBar,"Root->child", "", "2->3");		
		 * //verify the 3th matched "child" under 2th matched "Root" is enabled and it has 4 children.
		 * boolean success = Menu.VerifyMenuItem(Map.SAPDemo.MenuBar,"Root->child", "Enabled Menu With 4 MenuItems", "2->3");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyMenuItem(org.safs.model.Component menu, String path, 
				                             String expectedStatus, String indexPath){
			return action(menu, JavaMenuFunctions.VERIFYMENUITEM_KEYWORD, path, expectedStatus, indexPath);
		}
		/**
		 * Verify the existence of a menuItem found by partial text match.
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_VerifyMenuItemContains">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to verify an item from.
		 * @param path String, the path of the item to verify
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * boolean success = Menu.VerifyMenuItemContains(Map.SAPDemo.MenuBar,"Roo->ild");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyMenuItemContains(org.safs.model.Component menu, String path){
			return action(menu, JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD, path);
		}
		/**
		 * Verify the existence of a menuItem found by partial text match, it will verify the Nth matched item.<br>
		 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/JavaMenuFunctionsReference.htm#detail_VerifyMenuItemContains">Detailed Reference</a><p>
		 * @param menu Component (from App Map) to verify an item from.
		 * @param path String, the path of the item to verify.
		 * @param expectedStatus String, the status of the item to verify.
		 * @param indexPath String, the index path for Nth matched item of each level.
		 * @return true if successful, false otherwise.
		 * @example	 
		 * <pre>
		 * {@code
		 * //verify the first matched "Root->child" is enabled
		 * boolean success = Menu.VerifyMenuItemContains(Map.SAPDemo.MenuBar,"Root->child", "Enabled", "");		
		 * //verify the 3th matched "child" under 2th matched "Root" exists
		 * boolean success = Menu.VerifyMenuItemContains(Map.SAPDemo.MenuBar,"Root->child", "", "2->3");		
		 * //verify the 3th matched "child" under 2th matched "Root" is enabled and it has 4 children.
		 * boolean success = Menu.VerifyMenuItemContains(Map.SAPDemo.MenuBar,"Root->child", "Enabled Menu With 4 MenuItems", "2->3");		
		 * }
		 * </pre>	
		 * @see #prevResults
		 * @see org.safs.TestRecordHelper#getStatusCode()
		 * @see org.safs.TestRecordHelper#getStatusInfo()
		 */
		public static boolean VerifyMenuItemContains(org.safs.model.Component menu, String path, 
				String expectedStatus, String indexPath){
			return action(menu, JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD, path, expectedStatus, indexPath);
		}
	}
	//======================================================  embedded_wrapper_class_end  =================================================//
	
	/**
	 * Add double-quote around a string value. For "combine-word", the result is "\"combine-word\"";<br>
	 * The purpose is to avoid the string parameter to be processed by SAFS.<br>
	 * @param parameter String, the string to be double-quoted.
	 * @return String
	 */
	public static String quote(String parameter){
		if(parameter==null) return null;
		return "\"" + parameter +"\"";
	}
	
	protected static String quotePath(String path){
		if(path==null) return null;
		if (path.contains("->")) return quote(path);
		return path;
	}
	/**
	 * Sometimes the parameter (like coordination) will contain separator, but if this one is<br>
	 * the same as 'test-step-separator', then that parameter will not be correctly parsed,<br>
	 * we need to replace it by a different one.<br>
	 * To replace the possible conflicted separator in parameters.<br> 
	 */
	protected static String[] replaceSeparator(String... params){
		String stepSep = Runner.jsafs().getStepSeparator();
		return StringUtils.replaceSeparator(stepSep, params);
	}
	/**
	 * Combine the required parameters and optional parameters and return them as an array.<br>
	 * 
	 * @param extraParams String[], the optional parameters; can be null, if there is no optional parameters.
	 * @param preParams String ..., the required parameters
	 * @return String[], an array of parameters
	 */
	protected static String[] combineParams(String[] extraParams, String... preParams){
		List<String> params = new ArrayList<String>();
		
		//Add the required parameters firstly
		for(String p:preParams) {
			if(p==null) p = "(null)";
			params.add(p);
		}
		
		//Then add the optional parameters
		if(extraParams!=null) {
			for(String p:extraParams) {
				if(p == null) p = "(null)";
				params.add(p);
			}
		}
		
		return params.toArray(new String[0]);
	}
	
	/**
	 * Resolve parameters as DDVariable ONLY when {@link Misc#isExpressionsOn()} is false.<br>
	 * If {@link Misc#isExpressionsOn()} is true, we simply call {@link #combineParams(String[], String...)}<br>
	 * to put parameters into an array, later org.safs.model.tools.AbstractDriver will evaluate them.<br>
	 * @param parameter String, the first parameter
	 * @param optionals String[], the optional parameters
	 * @return String[], an array of parameters (DDVariable-resolved)
	 * @throws SeleniumPlusException
	 * @see {@link Misc#Expressions(boolean)}
	 * @see {@link Misc#isExpressionsOn()}
	 * @see #combineParams(String[], String...)
	 * @see Misc#SetVariableValueEx(String, String)
	 * @see Misc#SetVariableValues(String, String...)
	 */
	protected static String[] _resolveDDVariables(String parameter, String... optionals) throws SeleniumPlusException{
		List<String> parameters = new ArrayList<String>();
		
		try{
			JSAFSDriver jsafs = Runner.jsafs();
			if(!jsafs.isExpressionsEnabled()){
				parameters.add(jsafs.resolveExpression(parameter));
				for(int i=0;i<optionals.length;i++){
					parameters.add(jsafs.resolveExpression(optionals[i]));
				}
				return parameters.toArray(new String[0]);
			}else{
				return combineParams(optionals, parameter);
			}
		}catch(Throwable t){
			throw new SeleniumPlusException("", t);
		}
	}
	
	/**
	 * Check the status code of the TestRecord.
	 * @param command	String, the command that was executed.
	 * @param testRecord	TestRecordHelper, the result of the execution.
	 * @return boolean	true if the status code of execution is {@link StatusCodes#NO_SCRIPT_FAILURE}
	 */
	protected static boolean testStatusCode(String command, TestRecordHelper testRecord){
		String debumsg = StringUtils.debugmsg(SAFSPlus.class, "testStatusCode");
		if(testRecord==null) return false;
		
		int rc = testRecord.getStatusCode();
		if(rc==StatusCodes.NO_SCRIPT_FAILURE) return true;
		if(rc==StatusCodes.BRANCH_TO_BLOCKID) return true;
		if(rc==StatusCodes.SCRIPT_NOT_EXECUTED){
			IndependantLog.warn(debumsg+command+" is not executed!"+testRecord.getStatusInfo());
		}else if(rc==StatusCodes.SCRIPT_WARNING){
			IndependantLog.warn(debumsg+command+" has been executed with warning! "+testRecord.getStatusInfo());
		}
		return false;
	}
	
	/**
	 * Execute a 'component action' without a component.
	 * @param command	String,	the name of the 'component action' to execute.
	 * @param params	String[], the parameters for the 'component action'.
	 * @return	boolean	true if the 'component action' has been successfully executed.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @throws SAFSRuntimeException if detecting a user-initiated shutdown/abort request
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 * @see #action(org.safs.model.Component, String, String...)
	 */
	protected static boolean actionGUILess(String command, String... params) throws SAFSRuntimeException{
		org.safs.model.Component comp = new org.safs.model.Component(AbstractSAFSAppMapService.DEFAULT_SECTION_NAME);
		return action(comp, command, params);
	}
	/**
	 * Execute a 'component action' on the component.
	 * @param component Component, the component to execution some action on.
	 * @param command	String,	the name of the 'component action' to execute.
	 * @param params	String[], the parameters for the 'component action'.
	 * @return	boolean	true if the 'component action' has been successfully executed.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @throws SAFSRuntimeException if detecting a user-initiated shutdown/abort request
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	protected static boolean action(org.safs.model.Component component, String command, String... params) throws SAFSRuntimeException{
		try{
			prevResults = Runner.action(component, command, params);
			return testStatusCode(command, prevResults);
		}
		catch(Throwable t){
			prevResults = null;
			try{if(t.getMessage().toLowerCase().contains(JavaHook.SHUTDOWN_RECORD.toLowerCase())) throw new SAFSRuntimeException(t.getMessage(),t);}
			catch(NullPointerException ignore){}
			String debumsg = StringUtils.debugmsg(SAFSPlus.class, "action", command+" failed with Exception ", t);
			IndependantLog.error(debumsg);
			return false;
		}
	}
	
	/**
	 * Execute a driver command.
	 * @param command	String,	the name of the driver command to execute.
	 * @param params	String[], the parameters for the driver command.
	 * @return	boolean	true if the driver command was successfully executed.<p>  
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @throws SAFSRuntimeException if detecting a user-initiated shutdown/abort request
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	protected static boolean command(String command, String... params) throws SAFSRuntimeException{
		try{
			prevResults = Runner.command(command, params);
			return testStatusCode(command, prevResults);
		}catch(Throwable t){
			try{if(t.getMessage().toLowerCase().contains(JavaHook.SHUTDOWN_RECORD.toLowerCase())) throw new SAFSRuntimeException(t.getMessage(),t);}
			catch(NullPointerException ignore){}
			prevResults = null;
			String debumsg = StringUtils.debugmsg(SAFSPlus.class, "command", command+" failed with Exception ", t);
			IndependantLog.error(debumsg);
			return false;
		}
	}
	
	/**
	 * This is the method to start the automatic test. User may override this method, for example, to<br>
	 * provide the "auto.run.classname" as following.<br>
	 * <pre>
	 * protected void autorun(String[] args) throws Throwable{
	 *   String[] adjustedArgs = combineParams(args, SAFSPlus.ARG_AUTORUN_CLASS, "your.auto.run.classname");
	 *   super.autorun(adjustedArgs);
	 * }
	 * </pre>
	 * @param args passed in from command-line Java-- the primordial main(String[] args)<br>
	 *             Usually, it is the class who calls Runner.autorun() will be considered as<br>
	 *             the class that is requesting the automatic configuration and execution, but we can also<br>
	 *             change it by providing parameter as "-autorunclass autorun.full.classname".
	 * @throws Throwable
	 */
	protected void autorun(String[] args) throws Throwable{
		Runner.autorun(args);
	}
	
	protected static void _processArgs(String[] args){
		for(String arg:args){
			if(arg.equalsIgnoreCase(ARG_AUTORUN))            _autorun = true;
			else if(arg.equalsIgnoreCase(ARG_AUTORUN_CLASS)) _autorunClassProvided = true;
			else if(arg.startsWith(ARG_SAFSVAR)){
				String msg = null;
				try{
					String varg = arg.substring(arg.indexOf(":")+1);//string before the first :
					String vname = varg.substring(0, varg.indexOf("="));//string before the first =
					String vvalu = varg.substring(varg.indexOf("=")+1);//string after the first =
					msg = "Command-Line variable '"+ varg +"' ";
					if(SetVariableValue(vname, vvalu)){
						msg +="SET successfully.";
						System.out.println(msg);
						IndependantLog.info(msg);
					}else{
						msg +="was NOT SET!";
						System.out.println(msg);
						IndependantLog.info(msg);
					}
				}catch(Throwable t){
					msg = "SAFSPlus command-line argument \""+ arg +"\" malformed and ignored.";
					System.out.println(msg);
					IndependantLog.info(msg);
				}
			}else if(arg.startsWith(ARG_JUNIT)){
				String msg = null;
				try{
					String varg = arg.substring(arg.indexOf(":")+1);//string before the first :
					msg = "Command-Line -junit '"+ varg +"' detected.";
					if(varg.length() > 0) _junit = varg;
					else throw new IllegalArgumentException("-junit:class argument malformed.");
					System.out.println(msg);
					IndependantLog.info(msg);
				}catch(Throwable t){
					msg = "SAFSPlus command-line argument \""+ arg +"\" malformed and ignored.";
					System.out.println(msg);
					IndependantLog.info(msg);
				}
			}
		}		
	}
	
	/** 
	 * {@link #exitCode} is used to exit JVM from the method main(). The default value is 0. <br>
	 * If could be set by the subclass, for example, with the number of UNEXPECTED failures after all test.<br> 
	 */
	protected static int exitCode = 0;
	protected static boolean allowExit = false;
	public static void setExitCode(int rcCode){ exitCode = rcCode; }
	public static int getExitCode(){ return exitCode; }
	public static void setAllowExit(boolean allow){ allowExit = allow; }
	public static boolean getAllowExit() {return allowExit; }
	
	public static void debug(String message){
		AbstractRunner.debug(message);
	}
	public static void error(String message){
		AbstractRunner.error(message);
	}
	
	/**
	 * Internal framework use only.
	 * Main inherited by subclasses is required.
	 * Subclasses should not override this main method.  
	 * <p>
	 * Any subclass specific initialization should be done in the default no-arg constructor 
	 * for the subclass.  That Constructor will be instantiated and invoked automatically by 
	 * this main startup method.
	 * <p>
	 * By default will seek an AppMap.order file.  However, the user can specify an alternate 
	 * AppMap order file by using the following JVM argument:
	 * <p>
	 * <ul>Examples:
	 * <p>
	 * <li>-Dtestdesigner.appmap.order=AppMap_en.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_ja.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_win.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_mac.order
	 * <li>etc...
	 * </ul>
	 * <p>By default, a Debug Log is usually enabled and named in the test configuration (INI) file.   
	 * The user can specify or override the name of this debug log file by using the following JVM argument:
	 * <p>
	 * <ul>
	 * -Dtestdesigner.debuglogname=mydebuglog.txt
	 * </ul>
	 * <p>
	 * @param args -- 
	 * <p>
	 * -safsvar:name=value
	 * <p><ul>
	 * (Any number of these can be provided to preset variables or override App Map Constants.
	 *  Note the entire argument must be enclosed in quotes if there are spaces in it.)
	 * <pre>
	 * -safsvar:platform=win8 -safsvar:browserType=firefox "-safsvar:spacedpath=C:\Project With Spaces\Special Directory"</pre>
	 * </ul>
	 * -autorun -- Perform Dependency Injection, AutoConfig, and AutoExecution if "-autorun" is provided.
	 * <p>
	 * -autorunclass -- followed by the class that is requesting the automatic configuration and execution, only take effect when parameter '-autorun' is present.
	 * <ul>-autorunclass autorun.full.classname</ul>
	 * <p>
	 * -junit:classname -- perform a JUnit test instead of executing runTest() in the SAFSPlus subclass.<br>
	 * The normal SAFSPlus bootstrap process and initialization is performed prior to executing the JUnit test.
	 * <ul>-junit:com.sas.spock.tests.SpockExperiment</ul>
	 * <p>
	 * @see org.safs.model.annotations.AutoConfigureJSAFS
	 * @see org.safs.model.annotations.JSAFSBefore
	 * @see org.safs.model.annotations.JSAFSAfter
	 * @see org.safs.model.annotations.JSAFSTest
	 * @see org.safs.model.annotations.InjectJSAFS
	 */
	public static void main(String[] args) {
		String theClass = MainClass.getMainClass();
		if(theClass == null) theClass = MainClass.deduceMainClass();
		debug("Executing Java Main Class: "+ theClass);
		SAFSPlus test = null;
		ClassCastException cce = null;
		try{ 
			try{
				test = (SAFSPlus) Class.forName(theClass).newInstance();
			}catch(ClassCastException ccx){
				cce = ccx;
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				int i=0;
				do{
					StackTraceElement e = stack[i++];
					theClass = e.getClassName();
					debug("SAFSPlus test search evaluating "+ theClass);
					Class<?> c = Class.forName(theClass);
					Class<?> sc = c.getSuperclass();
					if(sc.getName().equals(SAFSPlus.class.getName())){
						MainClass.setMainClass(theClass);
						debug("Executing SAFSPlus subclass: "+ theClass);
						test = (SAFSPlus) c.newInstance();
					}
				}while(test == null && i < stack.length);				
			}
			Runner.run();
			if(args.length > 0) _processArgs(args);

			// enable the ability to run 3rd party scripts like Spock/Groovy
			if(test == null && _junit == null) 
				throw (cce != null) ? cce :
				new ClassCastException(theClass + " is not a subclass of SAFSPlus"+
				                                  " and no alternative -junit arg was provided.");
			
			if(!_isSPC) {
				ArrayList<String> altPackages = new ArrayList<String>();
				String modclass = "."+theClass;
				if(modclass.contains(".TestRuns.")) altPackages.add("TestCases");
				if(modclass.contains(".TestCases.")) altPackages.add("TestRuns");
				if(modclass.contains(".testruns.")) altPackages.add("testcases");
				if(modclass.contains(".testcases.")) altPackages.add("testruns");
				if(modclass.contains(".tests.")) altPackages.add("suites");
				if(modclass.contains(".suites.")) altPackages.add("tests");
				Utilities.injectRuntimeDataAwareClasses(test, altPackages, Runner.jsafs().getCoreInterface());
				Counters.StartTestSuite(test.getClass().getSimpleName());
			}
			if(_autorun){
				String[] adjustedArgs = args;
				if(!_autorunClassProvided) adjustedArgs = combineParams(args, ARG_AUTORUN_CLASS, theClass);
				test.autorun(adjustedArgs);
			}else if(_junit==null){
				test.runTest();
			}else {
				String badpath = "-junit command-parameter did not provide a valid classname!";
				if(_junit.length() == 0){
					error(badpath+" No Class.");
				}else{
					try{
						debug("SAFSPlus JUnit '"+ _junit +"' will now execute.");
						Runner.command(DDDriverFlowCommands.CALLJUNIT_KEYWORD, _junit);
					}catch(SeleniumPlusException | ClassNotFoundException e){
						Logging.LogTestFailure("SAFSPlus Execute JUnit Failed.", StringUtils.debugmsg(e));
					}
				}
			}
		}catch(Throwable x){
			x.printStackTrace(); 
		}
		if(!_isSPC) Counters.PrintTestSuiteSummary(test.getClass().getSimpleName());
		try {
			Runner.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(getAllowExit()) System.exit(exitCode);
	}
	
}
