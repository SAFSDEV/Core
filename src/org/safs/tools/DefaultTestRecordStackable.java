/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年6月12日    (Lei Wang) Initial release.
 */
package org.safs.tools;

import java.util.Stack;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.model.AbstractCommand;

/**
 * @author Lei Wang
 *
 */
public class DefaultTestRecordStackable implements ITestRecordStackable{
	/** 
	 * The Stack to hold the 'test records' being processed. Such as
	 * 
	 *   |                |
	 *   |  'LogMessage'  |
	 *   |  'CallJUnit'   |
	 *   |________________|
	 *   
	 * <p>
	 * The field {@link #testRecordHelper} is a class field, it will be overwritten for a new keyword execution.
	 * And we use ONE instance of {@link #JSAFSDriver(String)} to execute keyword.
	 * It is OK for execution of sequential keyword such as 'LogMessage' 'Expressions', there is no overlap.
	 * BUT for reentrant keyword as 'CallJUnit', inside JUnit test the keyword 'LogMessage' (even 'CallJUnit')
	 * may be attempted, and the field {@link #testRecordHelper} (for 'CallJUnit') will be overwritten by that of
	 * 'LogMessage', after execution of 'LogMessage', we come back to the execution of 'CallJUnit', but 
	 * the {@link #testRecordHelper} has been changed, we need to get it back. We use a FILO (Stack) to store 
	 * the {@link #testRecordHelper}, and try to retrieve the correct one from it.
	 * </p>
	 * @see #pushTestRecord()
	 * @see #popTestRecord()
	 * @see #processCommand(AbstractCommand, String)
	 * @see #processCommandDirect(AbstractCommand, String)
	 */
	protected Stack<TestRecordData> testRecordStack = new Stack<TestRecordData>();

	/**
	 * <p>
	 * Push the current 'test record' into the Stack before the execution of a keyword.
	 * This should be called after the 'test record' is properly set.
	 * </p>
	 * 
	 * @param trd TestRecordData, the test record to push into a stack
	 * @see #processCommand(AbstractCommand, String)
	 * @see #processCommandDirect(AbstractCommand, String)
	 * @see #popTestRecord()
	 */
	public void pushTestRecord(TestRecordData trd){
		//Push the test-record into the stack
		IndependantLog.debug(StringUtils.debugmsg(false)+" push test record "+testRecordToString(trd)+" into stack.");
		testRecordStack.push(trd);
	}
	
	/**
	 * Retrieve the Test-Record from the the Stack after the execution of a keyword.<br>
	 * <p>
	 * After execution of a keyword, pop the test record from Stack. If it is the same as 
	 * current {@link #testRecordHelper}, then ignore it; otherwise reset the current
	 * {@link #testRecordHelper} by it.
	 * </p>
	 * 
	 * @see #processCommand(AbstractCommand, String)
	 * @see #processCommandDirect(AbstractCommand, String)
	 * @see #pushTestRecord()
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
