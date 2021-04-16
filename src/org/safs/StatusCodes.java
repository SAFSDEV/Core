/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs;

/**
 * <br><em>Purpose:</em> holds the status codes (static final int fields)
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 * <br>     JUN 04, 2003    (DBauman) Original Release
 * <br> 	NOV 15, 2005    (Bob Lawler)  Added BRANCH_TO_BLOCKID (RJL).
 * <br>		MAR 30, 2012    (Carl Nagle) Changed Interface to class and added static getStatusString method.
 **/
public class StatusCodes {
  public static final int NO_SCRIPT_FAILURE        = -1;      //for scripts AND test tables
  public static final int SCRIPT_WARNING           = -2;      //for scripts AND test tables
  public static final int GENERAL_SCRIPT_FAILURE   = 0;       //for scripts AND test tables
  public static final int INVALID_FILE_IO          = 2;
  public static final int SCRIPT_NOT_EXECUTED      = 4;       //for scripts AND test tables
  public static final int EXIT_TABLE_COMMAND       = 8;
  public static final int IGNORE_RETURN_CODE       = 16;      //drivers ignore this one

  public static final int OK = NO_SCRIPT_FAILURE;
  public static final int NO_RECORD_TYPE_FIELD     = 32;
  public static final int UNRECOGNIZED_RECORD_TYPE = 64;
  public static final int WRONG_NUM_FIELDS         = 128;
  //public static final int ACTION_NOT_FOUND         = 256;
  public static final int BRANCH_TO_BLOCKID = 256;

  public static final String STR_OK = "OK";
  public static final String STR_NO_SCRIPT_FAILURE = "NO_SCRIPT_FAILURE";
  public static final String STR_SCRIPT_WARNING = "SCRIPT_WARNING";
  public static final String STR_GENERAL_SCRIPT_FAILURE = "GENERAL_SCRIPT_FAILURE";
  public static final String STR_INVALID_FILE_IO = "INVALID_FILE_IO";
  public static final String STR_SCRIPT_NOT_EXECUTED = "SCRIPT_NOT_EXECUTED";
  public static final String STR_EXIT_TABLE_COMMAND = "EXIT_TABLE_COMMAND";
  public static final String STR_IGNORE_RETURN_CODE = "IGNORE_RETURN_CODE";
  public static final String STR_NO_RECORD_TYPE_FIELD = "NO_RECORD_TYPE_FIELD";
  public static final String STR_UNRECOGNIZED_RECORD_TYPE = "UNRECOGNIZED_RECORD_TYPE";
  public static final String STR_WRONG_NUM_FIELDS = "WRONG_NUM_FIELDS";
  public static final String STR_BRANCH_TO_BLOCKID = "BRANCH_TO_BLOCKID";
  
  /**
   * Return the String human-readable representation of a valid statuscode constant.
   * Ex: if the provided status code is 4, the routine returns "SCRIPT_NOT_EXECUTED".
   * @param statuscode to convert to readable string.
   * @return readable status string, or null if the statuscode was not known.
   */
  public static String getStatusString(int statuscode){
	  switch(statuscode){
	  case NO_SCRIPT_FAILURE: return STR_NO_SCRIPT_FAILURE;
	  case SCRIPT_WARNING: return STR_SCRIPT_WARNING;
	  case GENERAL_SCRIPT_FAILURE: return STR_GENERAL_SCRIPT_FAILURE;
	  case INVALID_FILE_IO: return STR_INVALID_FILE_IO;
	  case SCRIPT_NOT_EXECUTED: return STR_SCRIPT_NOT_EXECUTED;
	  case EXIT_TABLE_COMMAND: return STR_EXIT_TABLE_COMMAND;
	  case IGNORE_RETURN_CODE: return STR_IGNORE_RETURN_CODE;
	  case NO_RECORD_TYPE_FIELD: return STR_NO_RECORD_TYPE_FIELD;
	  case UNRECOGNIZED_RECORD_TYPE: return STR_UNRECOGNIZED_RECORD_TYPE;
	  case WRONG_NUM_FIELDS: return STR_WRONG_NUM_FIELDS;
	  case BRANCH_TO_BLOCKID: return STR_BRANCH_TO_BLOCKID;
	  default: return null;
	  }
  }
}
