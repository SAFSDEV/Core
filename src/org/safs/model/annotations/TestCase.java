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
package org.safs.model.annotations;

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-21    (Lei Wang) Initial release.
 * @date 2018-06-11    (Lei Wang) Added skippedMessage.
 * @date 2018-12-14    (Lei Wang) Added testRailTestCaseId, testRailStatus and testRailComment.
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.safs.testrail.TestRailConstants;

/**
 * The annotation to mark a method representing test level as 'TestCase'.
 * @author Lei Wang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCase {
	/** Indicate if this test level will be skipped */
	boolean skipped() default false;
	/** Indicate why this test is skipped, it is only valid when skipped is set to true. */
	String skippedMessage() default "";

	//This test run ID will be provided in the .ini configuration file
	//int testRailTestRunId() default TestRailConstants.TESTCASE_RESULT_TESTRUNID_INVALID;

	int testRailTestCaseId() default TestRailConstants.TESTCASE_RESULT_TESTCASEID_INVALID;

	String testRailStatus() default TestRailConstants.TESTCASE_RESULT_STATUS_INVALID;

	String testRailComment() default "";

}
