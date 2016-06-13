/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAY 12, 2016    (SBJLWA) Initial release.
 */
package org.safs.tools;

import java.util.Stack;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.TestRecordData;

/**
 * @author sbjlwa
 *
 */
public class DefaultTestRecordStackable implements ITestRecordStackable{
	/** 
	 * The Stack to hold the 'test records' being processed. Such as
	 * <pre>
	 *   |  'LogMessage'  |
	 *   |__'CallJUnit'___|
	 * </pre> 
	 *
	 * @see #pushTestRecord(TestRecordData)
	 * @see #popTestRecord()
	 */
	protected Stack<TestRecordData> testRecordStack = new Stack<TestRecordData>();

	/**
	 * <p>
	 * Push the current 'test record' into the Stack before the execution of a keyword.
	 * This should be called after the 'test record' is properly set.
	 * </p>
	 * 
	 * @param trd TestRecordData, the test record to push into a stack
	 * @see #popTestRecord()
	 */
	public void pushTestRecord(TestRecordData trd){
		//Push the test-record into the stack
		IndependantLog.debug(StringUtils.debugmsg(false)+" push test record "+testRecordToString(trd)+" into stack.");
		testRecordStack.push(trd);
	}
	
	/**
	 * Retrieve the Test-Record from the top of Stack after the execution of a keyword.<br>
	 * <p>
	 * After execution of a keyword, pop the test record from Stack and return is as the result.
	 * In the sub class, we could choose to replace the class field 'Test Record' by that popped
	 * from the stack.
	 * </p>
	 * 
	 * @see #pushTestRecord(TestRecordData)
	 * @return TestRecordData, the 'Test Record' on top of the stack
	 */
	public TestRecordData popTestRecord(){
		TestRecordData history = null;
		String debugmsg = StringUtils.debugmsg(false);
		
		if(testRecordStack.empty()){
			IndependantLog.error(debugmsg+" the test-record stack is empty! Cannot reset.");
		}else{
			IndependantLog.debug(debugmsg+"Current test record stack: "+getStackInfo(testRecordStack));
			history = testRecordStack.pop();
		}
		return history;
	}
	
	public static String getMemoryAddress(Object object){
		if(object==null) return null;
		return "@"+Integer.toHexString(object.hashCode());
	}
	public static String testRecordToString(TestRecordData trd){
		if(trd==null) return null;
		return getMemoryAddress(trd) +":"+trd;
	}
	
	public static String getStackInfo(Stack<TestRecordData> stack){
		int size = stack.size();
		TestRecordData trd = null;
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<size;i++){
			trd = stack.elementAt(i);
			sb.append(testRecordToString(trd)+"\n");
		}
		
		return sb.toString();
	}
}
