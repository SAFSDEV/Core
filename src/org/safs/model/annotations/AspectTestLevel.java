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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-21    (Lei Wang) Initial release.
 * @date 2018-04-20    (Lei Wang) Use SAFSPlus.getRunner() instead of org.safs.model.tools.Runner to execute command.
 *                                org.safs.model.tools.Runner will start a new JSAFSDriver including a monitor
 *                                and it keeps running after the test. We don't want that.
 * @date 2018-06-11    (Lei Wang) Handled the SAFSTestLevelException thrown during the test.
 *                                Checked the test level hierarchy (Cycle > Suite > Case > Step).
 * @date 2018-06-13    (Lei Wang) Modified aroundTestLevel(): Write the whole SAFSTestLevelException into SAFS Log as a TESTLEVEL_FAILED message.
 * @date 2018-09-25    (Lei Wang) Handle the annotation TestStep: mark the begin and stop of a test step.
 * @date 2018-10-08    (Lei Wang) Modified aroundStep(): check the configuration setting to see if we can intercept test step.
 *                                Modified isConsideredAsStepKeyword(): if the keyword is null, then return false.
 *                                Modified aroundTestLevel(): catch un-expected exceptions and add debug log messages.
 * @date 2018-10-08    (Lei Wang) Modified aroundTestLevel(): When handling 'test-step', we check the argument's type instead of argument's value to avoid the null value problem.
 * @date 2018-12-14    (Lei Wang) Modified aroundTestLevel(): handle 'TestRail' properties in the TestCase annotation.
 * @date 2018-12-17    (Lei Wang) Modified aroundTestLevel(): Pass 'TestRail' as the tracking-system parameter for 'StartTestcase'/'StopTestcase'.
 * @date 2020-07-22    (Lei Wang) Modified aroundTestLevel(): get the 'return object' from the SAFSTestLevelException and return it for original method.
 */
package org.safs.model.annotations;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Stack;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPlus;
import org.safs.SAFSTestLevelError;
import org.safs.SAFSTestLevelFailure;
import org.safs.Utils;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.commands.DDDriverCounterCommands;
import org.safs.testrail.TestRailConstants;
import org.safs.tools.drivers.DriverConstant.DataServiceConstant;
import org.springframework.stereotype.Component;

/**
 * @author Lei Wang
 */
@Aspect
@Component
public class AspectTestLevel{
	/** "origin" the key used to store the StackTraceElement where SAFSTestLevelException is originally thrown out. */
	public static final String STACKTRACE_ORIGIN 			= "origin";
	/** "current" the key used to store the StackTraceElement where (of current test level) SAFSTestLevelException is thrown out. */
	public static final String STACKTRACE_CURRENT_TESTLEVEL = "current";

	/**
	 * The test level should be hierarchical, Cycle > Suite > Case > Step.
	 * This stack contains the ancestor test level. It can be used to check the hierarchy.
	 */
	private Stack<TestLevel> testLevels = new Stack<TestLevel>();

	private class TestLevel{
		private Annotation annotation;
		private String method;

		private boolean skipped = false;
		private String skippedMessage = "";

		String testRailStatus = TestRailConstants.TESTCASE_RESULT_STATUS_INVALID;

		int testRailTestCaseId = TestRailConstants.TESTCASE_RESULT_TESTCASEID_INVALID;

		String testRailComment = null;

		public TestLevel(Annotation annotation, String method){
			this.annotation = annotation;
			this.method = method;

			if(annotation instanceof TestCycle){
				TestCycle cycle = ((TestCycle) annotation);
				skipped = cycle.skipped();
				skippedMessage = cycle.skippedMessage();
			}else if(annotation instanceof TestSuite){
				TestSuite suite = ((TestSuite) annotation);
				skipped = suite.skipped();
				skippedMessage = suite.skippedMessage();
			}else if(annotation instanceof TestCase){
				TestCase testCase = ((TestCase) annotation);
				skipped = testCase.skipped();
				skippedMessage = testCase.skippedMessage();

				testRailStatus = testCase.testRailStatus();
				testRailTestCaseId = testCase.testRailTestCaseId();
				testRailComment = testCase.testRailComment();

			}else if(annotation instanceof TestStep){
				TestStep step = ((TestStep) annotation);
				skipped = step.skipped();
				skippedMessage = step.skippedMessage();
			}

		}

		public boolean isTestRailTest(){
			boolean isTestRailTest = false;

			isTestRailTest = (testRailTestCaseId!=TestRailConstants.TESTCASE_RESULT_TESTCASEID_INVALID);

			return isTestRailTest;
		}
		public boolean isTestRailStatusSet(){
			return !TestRailConstants.TESTCASE_RESULT_STATUS_INVALID.equalsIgnoreCase(testRailStatus);
		}
		public void setTestRailStatus(String testRailStatus){
			this.testRailStatus = testRailStatus;
		}

		public boolean isCycle(){
			return annotation instanceof TestCycle;
		}
		public boolean isSuite(){
			return annotation instanceof TestSuite;
		}
		public boolean isCase(){
			return annotation instanceof TestCase;
		}
		public boolean isStep(){
			return annotation instanceof TestStep;
		}

		@Override
		public String toString(){
			return method + (annotation==null? "":": "+annotation.toString());
		}
	}

	@Around("@annotation(TestCycle)")
	public Object aroundCycle(ProceedingJoinPoint joinPoint) throws Throwable{
		return aroundTestLevel(joinPoint, ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(TestCycle.class));
	}

	@Around("@annotation(TestSuite)")
	public Object aroundSuite(ProceedingJoinPoint joinPoint) throws Throwable{
		return aroundTestLevel(joinPoint, ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(TestSuite.class));
	}

	@Around("@annotation(TestCase)")
	public Object aroundCase(ProceedingJoinPoint joinPoint) throws Throwable{
		return aroundTestLevel(joinPoint, ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(TestCase.class));
	}

	@Around("@annotation(TestStep)")
	public Object aroundStep(ProceedingJoinPoint joinPoint) throws Throwable{
		if(DataServiceConstant.isInterceptStep()){
			return aroundTestLevel(joinPoint, ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(TestStep.class));
		}else{
			return joinPoint.proceed();
		}
	}

	/**
	 * There are some keywords used for parse the XML Log file, and they are not considered as real step keyword.
	 */
	private static final String[] IGNORED_STEP_KEYWORDS = {
			DDDriverCounterCommands.DELETECOUNTER_KEYWORD, DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, DDDriverCounterCommands.RESETCOUNTER_KEYWORD,
			DDDriverCounterCommands.RESUMESTATUSCOUNTS_KEYWORD, DDDriverCounterCommands.SETCOUNTERMODE_KEYWORD, DDDriverCounterCommands.STARTCOUNTER_KEYWORD,
			DDDriverCounterCommands.STARTCYCLE_KEYWORD, DDDriverCounterCommands.STARTPROCEDURE_KEYWORD, DDDriverCounterCommands.STARTREQUIREMENT_KEYWORD,
			DDDriverCounterCommands.STARTSUITE_KEYWORD, DDDriverCounterCommands.STARTTESTCASE_KEYWORD, DDDriverCounterCommands.STOPCOUNTER_KEYWORD,
			DDDriverCounterCommands.STOPCYCLE_KEYWORD, DDDriverCounterCommands.STOPPROCEDURE_KEYWORD, DDDriverCounterCommands.STOPREQUIREMENT_KEYWORD,
			DDDriverCounterCommands.STOPSUITE_KEYWORD, DDDriverCounterCommands.STOPTESTCASE_KEYWORD, DDDriverCounterCommands.STORECOUNTERINFO_KEYWORD,
			DDDriverCounterCommands.SUSPENDSTATUSCOUNTS_KEYWORD};

	/**
	 *
	 * @param keyword String, the potential step keyword.
	 * @return boolean if this keyword will be considered as a step keyword.
	 */
	private boolean isConsideredAsStepKeyword(String keyword){
		if(keyword==null) return false;
		for(String ignoredKeyword: IGNORED_STEP_KEYWORDS){
			if(ignoredKeyword.equalsIgnoreCase(keyword)) return false;
		}
		return true;
	}

	/**
	 * According to the annotation, we will<br>
	 * 1. call StartCycle/StartSuite/StartCase before the real test invocation.<br>
	 * 2. call StoreCounterInfo after the real test invocation.<br>
	 * 3. call LogCounterInfo after the real test invocation. <br>
	 * 4. call StopCycle/StopSuite/StopCase after the real test invocation.<br>
	 * <br>
	 * If user specifies 'testRailTestCaseId' in the TestCase annotation, we will<br>
	 * consider that user want to push test result into 'TestRail' tracking system.<br>
	 * We will pass optional parameters 'Tracking System', 'Test Status' and 'Test Comments'<br>
	 * to LogCounterInfo, so that we can process the Test Log later to push result to 'Tracking System'.<br>
	 *
	 */
	private Object aroundTestLevel(ProceedingJoinPoint joinPoint, Annotation annotation/* annotate test method of certain level*/) throws Throwable{
		Signature signature = joinPoint.getSignature();
		String classname = signature.getDeclaringTypeName();
		String method = signature.getName();
		String name = classname+"."+method;
		String startCommand = null;
		String stopCommand = null;
		String stepKeyword = null;

		String debugmsg = "ATL.aroundTestLevel(): ";
		TestLevel testLevel = new TestLevel(annotation, name);
		TestLevel parentLevel = null;
		IndependantLog.debug(debugmsg+"TestLevel: " +testLevel);
		String testID = "";

		if(!testLevels.isEmpty()){
			parentLevel = testLevels.peek();
			IndependantLog.debug(debugmsg+"Parent TestLevel: " +testLevel);
		}

		boolean isConsideredAsStepKeyword = true;
		String errmsg = null;
		//Set start/end command according to test level.
		//Check the test level hierarchy
		if(testLevel.isCycle()){
			startCommand = DDDriverCounterCommands.STARTCYCLE_KEYWORD;
			stopCommand = DDDriverCounterCommands.STOPCYCLE_KEYWORD;
			if(parentLevel == null){
				testLevels.push(testLevel);
			}else{
				errmsg = "TestCycle '"+testLevel+"' can NOT be the child of '"+parentLevel+"'!";
			}
		}else if(testLevel.isSuite()){
			startCommand = DDDriverCounterCommands.STARTSUITE_KEYWORD;
			stopCommand = DDDriverCounterCommands.STOPSUITE_KEYWORD;
			if(parentLevel == null || parentLevel.isCycle()){
				testLevels.push(testLevel);
			}else{
				errmsg = "TestSuite '"+testLevel+"' can NOT be the child of '"+parentLevel+"'!";
			}
		}else if(testLevel.isCase()){
			startCommand = DDDriverCounterCommands.STARTTESTCASE_KEYWORD;
			stopCommand = DDDriverCounterCommands.STOPTESTCASE_KEYWORD;
			if(parentLevel == null || parentLevel.isSuite()){
				testLevels.push(testLevel);
			}else{
				errmsg = "TestCase '"+testLevel+"' can NOT be the child of '"+parentLevel+"'!";
			}
		}else if(testLevel.isStep()){
			//We need the real SAFS keyword name, it is passed as parameters
			Object[] arguments = joinPoint.getArgs();

			IndependantLog.debug(debugmsg+"intercepting TestStep: "+Arrays.toString(arguments));
			if("action".equalsIgnoreCase(method)){//org.safs.model.tools.AbstractRunner.action()
				//There are 2 kinds of action methods, we need to parse the parameters' type
				//action(Component comp, String command, String... params)
				//action(String command, String window, String component, String... params)

				if(signature instanceof MethodSignature){
					Class<?>[] parameterTypes = ((MethodSignature) signature).getParameterTypes();
					if(parameterTypes[0].equals(org.safs.model.Component.class)){
						stepKeyword = arguments[1].toString();
					}else if(parameterTypes[0].equals(String.class)){
						stepKeyword = arguments[0].toString();
					}
				}

				if(stepKeyword==null){
					//System.out.println(signature.toLongString());
					//public transient org.safs.TestRecordHelper org.safs.model.tools.AbstractRunner.action(org.safs.model.Component,java.lang.String,java.lang.String[])
					String signatures = signature.toLongString();
					String parameterTypeString = signatures.substring(signatures.indexOf("(")+"(".length());
					parameterTypeString = parameterTypeString.substring(0, parameterTypeString.indexOf(")"));
					String[] parameterTypes = parameterTypeString.split(",");
					if(parameterTypes[0].equals(org.safs.model.Component.class.getName())){
						stepKeyword = arguments[1].toString();
					}else if(parameterTypes[0].equals(String.class.getName())){
						stepKeyword = arguments[0].toString();
					}
				}

				if(stepKeyword==null){
					IndependantLog.error(debugmsg+"Test Step: failed to parse the parameters "+Arrays.toString(arguments));
				}
			}else if("command".equalsIgnoreCase(method)){//org.safs.model.tools.AbstractRunner.command()
				stepKeyword = arguments[0].toString();
			}else{
				IndependantLog.warn(debugmsg+"We forget to handle TestStep annotated method '"+method+"'.");
			}
			isConsideredAsStepKeyword = isConsideredAsStepKeyword(stepKeyword);
			if(parentLevel == null || parentLevel.isCase()){
				testLevels.push(testLevel);
			}else{
				errmsg = "TestStep '"+testLevel+"' can NOT be the child of '"+parentLevel+"'!";
			}
		}else{
			errmsg = "'"+name+"' is not recognized as a TestLevel method.";
		}

		if(errmsg!=null){
			IndependantLog.error(debugmsg+errmsg);
			throw new SAFSException(errmsg);
		}

		try{
			//'Start Cycle/Suite/Case'
			if(!testLevel.isStep()){

				//Handle the pre-set TestRail attributes for the annotation TestCase
				//C, StartTestcase, TestCaseId, name, tracking-system
				if(testLevel.isTestRailTest()){
					testID = String.valueOf(testLevel.testRailTestCaseId);
					SAFSPlus.getRunner().command(startCommand, testID, name, TestRailConstants.TRACKING_SYSTEM_NAME);
				}else{
					//We don't have a special test ID, we use the method name instead.
					testID = name;
					SAFSPlus.getRunner().command(startCommand, testID, name);
				}

			}else{
				if(isConsideredAsStepKeyword) SAFSPlus.getRunner().logMessage(stepKeyword, null, AbstractLogFacility.START_STEP);
			}

			Object result = null;
			if(testLevel.skipped){
				SAFSPlus.getRunner().logMessage(testLevel.skippedMessage, name, AbstractLogFacility.TESTLEVEL_SKIPPED);
				if(testLevel.isTestRailTest()) testLevel.setTestRailStatus(TestRailConstants.TESTCASE_RESULT_STATUS_DEFER);
			}else{
				try{
					result = joinPoint.proceed();
				}catch(SAFSTestLevelError e){
					//We need to call e.getStackTrace() to update the exception 'e' with latest stack traces.
//					StackTraceElement[] traces = e.getStackTrace();

//					//traceOrigin is the original point where the SAFSTestLevelException is generated
//					StackTraceElement traceOrigin = traces[0];
//					//Find myself in the stack, traceOnCurrentTestlevel is the point (of current test level) where the SAFSTestLevelException is thrown out
//					StackTraceElement traceOnCurrentTestlevel = null;
//					for(StackTraceElement trace: traces){
//						//If the classname and the method name both match, then it is me.
//						if(trace.getClassName().equals(classname) && trace.getMethodName().equals(method)){
//							traceOnCurrentTestlevel = trace;
//							break;
//						}
//					}
//					if(traceOnCurrentTestlevel==null){
//						traceOnCurrentTestlevel = new StackTraceElement(classname, method, classname, -1);
//					}
//
//					Map<String, StackTraceElement> traceMap = new HashMap<String, StackTraceElement>();
//					traceMap.put(STACKTRACE_ORIGIN, traceOrigin);
//					traceMap.put(STACKTRACE_CURRENT_TESTLEVEL, traceOnCurrentTestlevel);
//
//					SAFSPlus.getRunner().logMessage(Utils.toJsonForSpring(e), Utils.toJsonForSpring(traceMap), AbstractLogFacility.TESTLEVEL_FAILED);

					//origin = Utils.getJsonValue(Utils.toJsonForSpring(traceMap), STACKTRACE_ORIGIN, StackTraceElement.class);
					//me = Utils.getJsonValue(Utils.toJsonForSpring(traceMap), STACKTRACE_CURRENT_TESTLEVEL, StackTraceElement.class);

					SAFSPlus.getRunner().logMessage(Utils.toJsonForSpring(e), null, AbstractLogFacility.TESTLEVEL_ERRORED);

					if(testLevel.isTestRailTest()) testLevel.setTestRailStatus(TestRailConstants.TESTCASE_RESULT_STATUS_ERROR);

					if(e.isPropagate())
						throw e;
					else{
						result = e.getReturnObj();//to avoid the conversion exception
					}

				}catch(SAFSTestLevelFailure e){
					//We catch the SAFSTestLevelFailure to log 'TESTLEVEL_FAILED' message, we never throw out it again.
					SAFSPlus.getRunner().logMessage(Utils.toJsonForSpring(e), null, AbstractLogFacility.TESTLEVEL_FAILED);
					if(testLevel.isTestRailTest()) testLevel.setTestRailStatus(TestRailConstants.TESTCASE_RESULT_STATUS_FAILED);
					result = e.getReturnObj();//to avoid the conversion exception
				}
			}

			if(result==null){
				IndependantLog.warn("The reutrned value is null, it may cause a convesion error if the original method require a returned value.");
			}
			return result;

		}catch(Exception e){
			IndependantLog.error(debugmsg+"Failed to intercept a certain test level "+e.getClass().getSimpleName()+":"+e.getMessage());
			throw e;
		}finally{
			try{
				if(!testLevel.isStep()){
					SAFSPlus.getRunner().command(DDDriverCounterCommands.STORECOUNTERINFO_KEYWORD, testID, name);
					//C, LogCounterInfo, TestCaseID, name, TrackingSystem, Comments, Status
					//We must execute the 'Stop Cycle/Suite/Case' at the last so that the 'LogCounterInfo' will be included
					//between 'Start Cycle/Suite/Case' and 'Stop Cycle/Suite/Case' and it will be easier to transform the Log XML file.
					if(testLevel.isTestRailTest()){
						SAFSPlus.getRunner().command(DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, testID, name, TestRailConstants.TRACKING_SYSTEM_NAME, testLevel.testRailComment, testLevel.testRailStatus);
						SAFSPlus.getRunner().command(stopCommand, testID, name, TestRailConstants.TRACKING_SYSTEM_NAME);
					}else{
						SAFSPlus.getRunner().command(DDDriverCounterCommands.LOGCOUNTERINFO_KEYWORD, testID, name);
						SAFSPlus.getRunner().command(stopCommand, testID, name);
					}
				}else{
					if(isConsideredAsStepKeyword) SAFSPlus.getRunner().logMessage(stepKeyword, null, AbstractLogFacility.END_STEP);
				}
			}catch(Exception e){
				IndependantLog.error(debugmsg+"Failed to mark the stop of a certain test level "+e.getClass().getSimpleName()+":"+e.getMessage());
			}
			//Pop out the TestLevel object
			if(!testLevels.isEmpty()) testLevels.pop();

		}

	}
}
