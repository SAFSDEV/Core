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
 * @date 2018-06-13    Initial release
 * @date 2020-07-22    Add field 'returnObj', user can set it so that our "spring aspect"
 *                     can return it for method annotated by {@link TestCycle}, {@link TestSuite} or {@link TestCase}.
 */
package org.safs;

import java.util.ArrayList;
import java.util.List;

import org.safs.model.annotations.AspectTestLevel;
import org.safs.model.annotations.TestCase;
import org.safs.model.annotations.TestCycle;
import org.safs.model.annotations.TestSuite;

/**
 * This is expected to throw out in a method annotated by {@link TestCycle}, {@link TestSuite} or {@link TestCase} to indicate the failures/errors of a certain test level.<br>
 * This class is abstract, please use sub-class {@link SAFSTestLevelFailure} or {@link SAFSTestLevelError}.<br>
 *
 * <p>
 * This Exception will be caught by {@link AspectTestLevel}, but we don't throw it out anymore,
 * instead we return a <b>null</b> for the original method. If the original method doesn't require a return value, it is OK;
 * but if the original method needs a return value, there will be a AopInvocationException. At this situation user can set
 * the field {@link #returnObj} and it will be used for original method as return value. Below is an example:<br>
 *
 * <pre>
 * <font color="red">
 * //With this annotation, this method will be intercepted by our "spring aspect" {@link AspectTestLevel}.
 * {@literal @}TestCase</font>
 * //<font color="red">an integer value is required as return value</font>
 * public <font color="red">int</font> testAPIForHtml(String browser) throws Throwable{
 *   int fail = 0;
 *   String mapID = "BogusHtml.map";
 *   String counterID = StringUtils.getMethodName(0, true);
 *
 *   SAFSTestLevelError testLevelError = new SAFSTestLevelError();
 *
 *   if(Misc.SetApplicationMap(mapID)){
 *
 *   }else{
 *     String errorMsg = "Fail to load map '"+mapID+"', cannot test in browser '"+browser+"'!";
 *     Logging.LogTestFailure(counterID+errorMsg);
 *     testLevelError.addError("SetApplicationMap Failed.", errorMsg, "MAPERR");
 *   }
 *
 *   if(fail > 0){
 *     Logging.LogTestFailure(counterID + " reports "+ fail +" UNEXPECTED test failures!");
 *     <font color="red">//Set the field 'returnObj' so that {@link AspectTestLevel} can return it for this method as "return value".
 *     <b>testLevelError.setReturnObj(fail);</b></font>
 *     throw testLevelError;
 *   }else{
 *     Logging.LogTestSuccess(counterID + " did not report any UNEXPECTED test failures!");
 *   }
 *   return fail;
 * }
 * </pre>
 *
 * @author Lei Wang
 *
 */
@SuppressWarnings("serial")
public abstract class SAFSTestLevelException extends RuntimeException {
	/**
	 * Hold the errors/failures thrown out with this exception.
	 */
	protected List<TestLevelMessage> messages = new ArrayList<TestLevelMessage>();

	/**
	 * Hold the object to return for annotation handling. When handling the annotation,
	 * we use the "spring aspect" {@link AspectTestLevel} to add some extra operations
	 * around an annotated method, sometimes when the original method throws an exception
	 * {@link SAFSTestLevelError}/{@SAFSTestLevelFailure}, we catch them in "spring aspect"
	 * {@link AspectTestLevel} but we don't throw it out again, instead we return a <b>null</b>
	 * for the original method. If the original method doesn't require a return value, it is
	 * OK; but if the original method needs a return value, there will be a AopInvocationException.
	 * At this situation, we can set this field when we create the exception in the original method,
	 * and our "spring aspect" {@link AspectTestLevel} will return this field for original method.
	 */
	protected Object returnObj = null;

	public SAFSTestLevelException (){
		super();
	}

	public SAFSTestLevelException (String message, String details, String type){
		this();
		addMessage(message, details, type, StringUtils.getStackTraceElement(1));
	}

	protected void addMessage(String message, String details, String type, StackTraceElement errorTrace){
		messages.add(new TestLevelMessage(message, details, type, errorTrace));
	}

	protected List<TestLevelMessage> getMessages() {
		return messages;
	}

	public Object getReturnObj() {
		return returnObj;
	}

	public void setReturnObj(Object returnObj) {
		this.returnObj = returnObj;
	}

	public static class TestLevelMessage{
		private String message = null;
		private String details = null;
		private String type = null;
		/** Hold the point where the SAFSTestLevelException is thrown out. */
		private StackTraceElement errorTrace = null;
//		private Map<String, String> customProperties = null;

		/**
		 * @param message
		 * @param details
		 * @param type
		 */
		public TestLevelMessage(String message, String details, String type, StackTraceElement errorTrace) {
			super();
			this.message = message;
			this.details = details;
			this.type = type;
			this.errorTrace = errorTrace;
		}

		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getDetails() {
			return details;
		}
		public void setDetails(String details) {
			this.details = details;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public StackTraceElement getErrorTrace() {
			return errorTrace;
		}
		public void setErrorTrace(StackTraceElement errorTrace) {
			this.errorTrace = errorTrace;
		}
	}
}

