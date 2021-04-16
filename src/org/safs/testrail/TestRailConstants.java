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
 * @date 2018-11-16    (Lei Wang) Initial release.
 */
package org.safs.testrail;

/**
 * @author Lei Wang
 *
 */
public abstract class TestRailConstants{

	/** "TestRail" string, The name of the tracking system. */
	public static final String TRACKING_SYSTEM_NAME						= "TestRail";

	/**
	 * "status_id" int, The ID of the test status. The built-in system statuses have the following IDs:<br>
	 * (got by running command http://docs.gurock.com/testrail-api2/reference-statuses)<br>
	 *
	 * <ol>
	 * <li>"id":1,"label":"Passed"
	 * <li>"id":2,"label":"Blocked"
	 * <li>"id":3,"label":"Not Started" (not allowed when adding a result)
	 * <li>"id":4,"label":"Retest"
	 * <li>"id":5,"label":"Failed"
	 * <li>"id":6,"label":"Defer"
	 * <li>"id":7,"label":"Error"
	 * <li>"id":8,"label":"Known Defect"
	 * <li>"id":9,"label":"In Progress"
	 * <li>"id":10,"label":"Passed with Issues"
	 * <li>"id":11,"label":"Not Applicable"
	 * <li>"id":12,"label":"Not Tested"
	 * </ol>
	 */
	public static final String TESTCASE_RESULT_STATUS_ID		= "status_id";

	public static final int TESTCASE_RESULT_STATUSID_INVALID				= -1;
	public static final int TESTCASE_RESULT_TESTRUNID_INVALID				= -1;
	public static final int TESTCASE_RESULT_TESTCASEID_INVALID				= -1;

	/** 1 int, "Passed" */
	public static final String TESTCASE_RESULT_STATUS_PASSED			= "Passed";
	/** 2 int, "Blocked" */
	public static final String TESTCASE_RESULT_STATUS_BLOCKED			= "Blocked";
	/** 3 int, "Not Started" */
	public static final String TESTCASE_RESULT_STATUS_NOT_STARTED		= "Not Started";
	/** 4 int, "Retest" */
	public static final String TESTCASE_RESULT_STATUS_RESET				= "Retest";
	/** 5 int, "Failed" */
	public static final String TESTCASE_RESULT_STATUS_FAILED			= "Failed";
	/** 6 int, "Defer" */
	public static final String TESTCASE_RESULT_STATUS_DEFER				= "Defer";
	/** 7 int, "Error" */
	public static final String TESTCASE_RESULT_STATUS_ERROR				= "Error";
	/** 8 int, "Known Defect" */
	public static final String TESTCASE_RESULT_STATUS_KNOWN_DEFECT		= "Known Defect";
	/** 9 int, "In Progress" */
	public static final String TESTCASE_RESULT_STATUS_INPROGRESS		= "In Progress";
	/** 10 int, "Passed with Issues" */
	public static final String TESTCASE_RESULT_STATUS_PASSED_WITH_ISSUES = "Passed with Issues";
	/** 11 int, "Not Applicable" */
	public static final String TESTCASE_RESULT_STATUS_NOT_APPLICABLE	= "Not Applicable";
	/** 12 int, "Not Tested" */
	public static final String TESTCASE_RESULT_STATUS_NOT_TESTED		= "Not Tested";

	/** "Not Valid" */
	public static final String TESTCASE_RESULT_STATUS_INVALID			= "Not Valid";

	/** 1 int, "Passed" */
	public static final int TESTCASE_RESULT_STATUSID_PASSED				= 1;
	/** 2 int, "Blocked" */
	public static final int TESTCASE_RESULT_STATUSID_BLOCKED			= 2;
	/** 3 int, "Not Started" */
	public static final int TESTCASE_RESULT_STATUSID_NOT_STARTED		= 3;
	/** 4 int, "Retest" */
	public static final int TESTCASE_RESULT_STATUSID_RESET				= 4;
	/** 5 int, "Failed" */
	public static final int TESTCASE_RESULT_STATUSID_FAILED				= 5;
	/** 6 int, "Defer" */
	public static final int TESTCASE_RESULT_STATUSID_DEFER				= 6;
	/** 7 int, "Error" */
	public static final int TESTCASE_RESULT_STATUSID_ERROR				= 7;
	/** 8 int, "Known Defect" */
	public static final int TESTCASE_RESULT_STATUSID_KNOWN_DEFECT		= 8;
	/** 9 int, "In Progress" */
	public static final int TESTCASE_RESULT_STATUSID_INPROGRESS			= 9;
	/** 10 int, "Passed with Issues" */
	public static final int TESTCASE_RESULT_STATUSID_PASSED_WITH_ISSUES	= 10;
	/** 11 int, "Not Applicable" */
	public static final int TESTCASE_RESULT_STATUSID_NOT_APPLICABLE		= 11;
	/** 12 int, "Not Tested" */
	public static final int TESTCASE_RESULT_STATUSID_NOT_TESTED			= 12;

	public static final String getTestCaseStatus(int statusID){
		switch(statusID){
		case TESTCASE_RESULT_STATUSID_PASSED: return TESTCASE_RESULT_STATUS_PASSED;
		case TESTCASE_RESULT_STATUSID_BLOCKED: return TESTCASE_RESULT_STATUS_BLOCKED;
		case TESTCASE_RESULT_STATUSID_NOT_STARTED: return TESTCASE_RESULT_STATUS_NOT_STARTED;
		case TESTCASE_RESULT_STATUSID_RESET: return TESTCASE_RESULT_STATUS_RESET;
		case TESTCASE_RESULT_STATUSID_FAILED: return TESTCASE_RESULT_STATUS_FAILED;
		case TESTCASE_RESULT_STATUSID_DEFER: return TESTCASE_RESULT_STATUS_DEFER;
		case TESTCASE_RESULT_STATUSID_ERROR: return TESTCASE_RESULT_STATUS_ERROR;
		case TESTCASE_RESULT_STATUSID_KNOWN_DEFECT: return TESTCASE_RESULT_STATUS_KNOWN_DEFECT;
		case TESTCASE_RESULT_STATUSID_INPROGRESS: return TESTCASE_RESULT_STATUS_INPROGRESS;
		case TESTCASE_RESULT_STATUSID_PASSED_WITH_ISSUES: return TESTCASE_RESULT_STATUS_PASSED_WITH_ISSUES;
		case TESTCASE_RESULT_STATUSID_NOT_APPLICABLE: return TESTCASE_RESULT_STATUS_NOT_APPLICABLE;
		case TESTCASE_RESULT_STATUSID_NOT_TESTED: return TESTCASE_RESULT_STATUS_NOT_TESTED;
		}
		//if no match, return the "Not Valid" string
		return TESTCASE_RESULT_STATUS_INVALID;
	}

	/** "comment" string, The comment/description for the test result. */
	public static final String TESTCASE_RESULT_COMMENT			= "comment";

	/** "version" string, The version or build you tested against. */
	public static final String TESTCASE_RESULT_VERSION			= "version";

	/** "elapsed" string(time span), The time it took to execute the test, e.g. "30s" or "1m 45s". */
	public static final String TESTCASE_RESULT_ELAPSED			= "elapsed";

	/** "defects" string, A comma-separated list of defects to link to the test result. */
	public static final String TESTCASE_RESULT_DEFECTS			= "defects";

	/** "assignedto_id" int, The ID of a user the test should be assigned to. */
	public static final String TESTCASE_RESULT_ASSIGNEDTO_ID	= "assignedto_id";

	/** "results" array of result, The ID of a user the test should be assigned to. */
	public static final String TESTCASE_RESULT_RESULTS			= "results";

	/** "case_id" int, The ID of the test case. */
	public static final String TESTCASE_RESULT_CASE_ID			= "case_id";


}
