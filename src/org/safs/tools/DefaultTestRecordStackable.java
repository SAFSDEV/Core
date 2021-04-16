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
 * MAY 12, 2016    (Lei Wang) Initial release.
 */
package org.safs.tools;

import java.util.Stack;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.TestRecordData;

/**
 * @author Lei Wang
 *
 */
public class DefaultTestRecordStackable implements ITestRecordStackable{
	
	/** 
	 * The default value is 'false', so that some debug message will not show in debug log.
	 * If we want more debug information about 'test record stack', we can set this field to true.
	 */
	public static boolean debug = false;
	
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
         * Log a debug log message ONLY if debug log output is enabled.
         * @param msg to be logged
         * @see #debug
         */
        public static void debug(String msg){
            if(debug) IndependantLog.debug(msg);
        }
        
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
		debug(StringUtils.debugmsg(false)+" push test record "+StringUtils.toStringWithAddress(trd)+" into stack.");
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
			debug(debugmsg+"Current test record stack: "+StringUtils.getStackInfo(testRecordStack));
			history = testRecordStack.pop();
		}
		return history;
	}

}
