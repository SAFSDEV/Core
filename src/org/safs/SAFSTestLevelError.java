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
 */
package org.safs;

import java.util.List;

import org.safs.logging.AbstractLogFacility;
import org.safs.model.annotations.TestCase;
import org.safs.model.annotations.TestCycle;
import org.safs.model.annotations.TestSuite;

/**
 * This is expected to throw out in a method annotated by {@link TestCycle}, {@link TestSuite} or {@link TestCase} to indicate the error of a certain test level.<br>
 * Indicates that the test errored.  An errored test is one that had an unanticipated problem. e.g., an unchecked throwable; or a problem with the implementation of the test.<br>
 * And it will get SAFS to log a message of type {@link AbstractLogFacility#TESTLEVEL_ERRORED}, example as below:<br>
 *
 * <pre>
 * &lt;LOG_MESSAGE type='TESTLEVEL_ERRORED' date='06-19-2018' time='10:32:42' &gt;
 *   &lt;MESSAGE_TEXT&gt;&lt;![CDATA[{"propagate":false,"messages":[{"type":"NullPointerException","errorTrace":{"declaringClass":"a.testcase.Cases1","methodName":"case5","fileName":"Cases1.java","lineNumber":88}}],"stackTrace":[],"suppressedExceptions":[]}]]&gt;&lt;/MESSAGE_TEXT&gt;
 * &lt;/LOG_MESSAGE&gt;
 * </pre>
 *
 * Below is an example to throw this exception with a single error in test case.
 * <pre>
 * {@literal @}TestCase
 * public void case3() throws SAFSTestLevelException{
 *   String name = null;
 *   try{
 *     name.length();
 *   }catch(NullPointerException e){
 *     throw new SAFSTestLevelError(e.getMessage(), null, e.getClass().getSimpleName());
 *   }
 * }
 * </pre>
 *
 *
 * @author Lei Wang
 *
 */
@SuppressWarnings("serial")
public class SAFSTestLevelError extends SAFSTestLevelException {
	/**
	 * If this exception will be propagated to the higher test level.<br>
	 * If it is true, then this exception will be thrown out.<br>
	 * If it is false, then this exception will be caught inside framework.<br>
	 * In both case, a message of type {@link AbstractLogFacility#TESTLEVEL_ERRORED} will be written into SAFS Log.<br>
	 * By default, it is false.
	 */
	private boolean propagate = false;

	public SAFSTestLevelError (){
		super();
	}

	public SAFSTestLevelError (boolean propagate){
		this();
		this.propagate = propagate;
	}
	public SAFSTestLevelError (String message, String details, String type){
		this();
		addMessage(message, details, type, StringUtils.getStackTraceElement(1));
	}

	public SAFSTestLevelError (String message, String details, String type, boolean propagate){
		this(message, details, type);
		this.propagate = propagate;
	}

	public boolean isPropagate() {
		return propagate;
	}

	//DO NOT call this method inside this class, the StackTrace will be wrong,
	//Please call the protected void addMessage(String message, String details, String type, StackTraceElement errorTrace).
	public void addError(String message, String details, String type){
		addMessage(message, details, type, StringUtils.getStackTraceElement(1));
	}

	public List<TestLevelMessage> getErrors() {
		return super.getMessages();
	}

}

