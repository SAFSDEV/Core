/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android;

import org.safs.StatusCodes;
import org.safs.tools.drivers.DriverConstant;

/**
 * Object used to house the status results and loggable messages from a Droid engine execution.
 * 
 * @author Carl Nagle
 */
public class DStatus {
	
	public static final int STAT_WARNING = StatusCodes.SCRIPT_WARNING;                      // -2
	public static final int STAT_OK = StatusCodes.OK;                                       // -1
	public static final int STAT_FAILURE = StatusCodes.GENERAL_SCRIPT_FAILURE;              // 0
	public static final int STAT_IO_ERROR = StatusCodes.INVALID_FILE_IO;                    // 2
	public static final int STAT_NOT_EXECUTED = StatusCodes.SCRIPT_NOT_EXECUTED;            // 4
	
	public static final int STAT_WINDOW_NOT_FOUND = 5;  // engine-specific
	public static final int STAT_COMP_NOT_FOUND   = 6;  // engine-specific
	
	public static final int STAT_EXIT_TABLE = StatusCodes.EXIT_TABLE_COMMAND;               // 8
	public static final int STAT_IGNORE_RC = StatusCodes.IGNORE_RETURN_CODE;                // 16
	public static final int STAT_NO_RECORDTYPE = StatusCodes.NO_RECORD_TYPE_FIELD;          // 32
	public static final int STAT_UNKNOWN_RECORDTYPE = StatusCodes.UNRECOGNIZED_RECORD_TYPE; // 64
	public static final int STAT_WRONG_FIELDS_COUNT = StatusCodes.WRONG_NUM_FIELDS;         // 128
	public static final int STAT_BRANCH = StatusCodes.BRANCH_TO_BLOCKID;                    // 256

	
	/** initializes to SCRIPT_NOT_EXECUTED */
	public int rc = STAT_NOT_EXECUTED;
	/** initializes to null */
	public String comment = null;
	/** initializes to null */
	public String detail = null;
	
	public DStatus(){}
	
	/**
	 * Convenience constructor to populate multiple fields in a single call. 
	 */
	public DStatus(int rc, String comment, String detail){
		this.rc = rc;
		this.comment = comment;
		this.detail = detail;
	}
}
