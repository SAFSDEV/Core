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
 * This is expected to throw out in a method annotated by {@link TestCycle}, {@link TestSuite} or {@link TestCase} to indicate the failures of a certain test level.<br>
 * Indicates that the test failed. A failure is a test which the code has explicitly failed by using the mechanisms for that purpose. e.g., via an assertEquals.<br>
 * And it will get SAFS to log a message of type {@link AbstractLogFacility#TESTLEVEL_FAILED}, example as below:<br>
 *
 * <pre>
 * &lt;LOG_MESSAGE type='TESTLEVEL_FAILED' date='06-19-2018' time='10:15:41' &gt;
 *   &lt;MESSAGE_TEXT&gt;&lt;![CDATA[{"messages":[{"message":"benchFile.txt doesn\u0027t match actualFile.txt.","details":"detailed failure message.","type":"DIFF","errorTrace":{"declaringClass":"a.testcase.Cases1","methodName":"case3","fileName":"Cases1.java","lineNumber":58}}],"stackTrace":[],"suppressedExceptions":[]}]]&gt;&lt;/MESSAGE_TEXT&gt;
 * &lt;/LOG_MESSAGE&gt;
 * </pre>
 *
 * Below is an example to throw this exception with a single error in test case.
 * <pre>
 * {@literal @}TestCase
 * public void case3() throws SAFSTestLevelFailure{
 *   if(!SeleniumPlus.VerifyFileToFile("benchFile.txt", "actualFile.txt")){
 *     throw new SAFSTestLevelFailure("benchFile.txt doesn't match actualFile.txt.","detailed failure message.", "DIFF");
 *   }
 * }
 * </pre>
 *
 * Below is an example to throw this exception with multiple errors in test case.
 * <pre>
 * {@literal @}TestCase
 * public void case4() throws SAFSTestLevelFailure{
 *   SAFSTestLevelFailure testLevelException = new SAFSTestLevelFailure();
 *
 *   org.safs.model.Component nonExistGui = new org.safs.model.Component("FANTACY_GUI");
 *
 *   if(!Misc.IsComponentExists(nonExistGui)){
 *     testLevelException.addFailure("IsComponentExists failed.", nonExistGui.getName()+" doesn't exist!", "NON_EXIST");
 *   }
 *
 *   if(!SeleniumPlus.WaitForGUI(nonExistGui, 3)){
 *     testLevelException.addFailure("WaitForGUI failed.", nonExistGui.getName()+" doesn't exist!", "TIMEOUT");
 *   }
 *
 *   throw testLevelException;
 * }
 * </pre>
 *
 * @author Lei Wang
 *
 */
@SuppressWarnings("serial")
public class SAFSTestLevelFailure extends SAFSTestLevelException {

	public SAFSTestLevelFailure (){
		super();
	}

	public SAFSTestLevelFailure (String message, String details, String type){
		this();
		addMessage(message, details, type, StringUtils.getStackTraceElement(1));
	}

	//DO NOT call this method inside this class, the StackTrace will be wrong,
	//Please call the protected void addMessage(String message, String details, String type, StackTraceElement errorTrace).
	public void addFailure(String message, String details, String type){
		addMessage(message, details, type, StringUtils.getStackTraceElement(1));
	}

	public List<TestLevelMessage> getFailures() {
		return super.getMessages();
	}

}

