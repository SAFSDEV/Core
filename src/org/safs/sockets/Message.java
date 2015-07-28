/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * The SocketProtocol Messages used between the SAFS Messaging Service.
 * 
 * @author Carl Nagle, SAS Institute, Inc.
 * @see org.safs.sockets.SocketProtocol
 */
public class Message {

	public static final int STATUS_REMOTERESULT_UNKNOWN = -99;
	public static final int STATUS_REMOTERESULT_WARN    = -3;
	public static final int STATUS_REMOTE_NOT_EXECUTED  = -2;
	public static final int STATUS_REMOTERESULT_FAIL    = -1;
	public static final int STATUS_REMOTERESULT_OK      = 0;

	public static final String STATUS_REMOTERESULT_UNKNOWN_STRING = String.valueOf(STATUS_REMOTERESULT_UNKNOWN);
	public static final String STATUS_REMOTE_NOT_EXECUTED_STRING  = String.valueOf(STATUS_REMOTE_NOT_EXECUTED);
	public static final String STATUS_REMOTERESULT_FAIL_STRING    = String.valueOf(STATUS_REMOTERESULT_FAIL);
	public static final String STATUS_REMOTERESULT_WARN_STRING    = String.valueOf(STATUS_REMOTERESULT_WARN);
	public static final String STATUS_REMOTERESULT_OK_STRING      = String.valueOf(STATUS_REMOTERESULT_OK);

	/** ":" */
	public static String msg_sep = ":";

	/** "debug" */
	public static final String msg_debug = "debug";
	
	/** "exception" */
	public static final String msg_exception = "exception";

	/** "connected" */
	public static final String msg_connected = "connected";

	/** "ready" */
	public static final String msg_ready = "ready";

	/** "dispatchprops" */
	public static final String msg_dispatchprops = "dispatchprops";
	
	/** "dispatchfile" */
	public static final String msg_dispatchfile = "dispatchfile";
	
	/** "message" */
	public static final String msg_message = "message";
	
	/** "running" */
	public static final String msg_running = "running";

	/** "result" */
	public static final String msg_result = "result";

	/** "resultprops" */
	public static final String msg_resultprops = "resultprops";

	/** "remoteshutdown" -- a normal shutdown. */
	public static final String msg_remoteshutdown = "remoteshutdown";

	/** "shutdown" -- usually reporting an unexpected shutdown. */
	public static final String msg_shutdown = "shutdown";

	public static final int shutdown_cause_normal     = 0;
	public static final int shutdown_cause_service    = 1;
	public static final int shutdown_cause_device     = 2;
	public static final int shutdown_cause_controller = 3;
	
	/** "isremoteresult" Property key for the validation property with String value "true" or "false". 
	 * "true" indicates the results information in the Properties instance should be considered valid 
	 * results issued from the remote client. */
	public static final String KEY_ISREMOTERESULT = "isremoteresult";
	
	/** "remoteresultcode" Property key for the String value of the int statuscode. */
	public static final String KEY_REMOTERESULTCODE = "remoteresultcode";
	
	/** "remoteresultinfo" Property key for the String value of additional statusinfo, if any. */
	public static final String KEY_REMOTERESULTINFO = "remoteresultinfo";	
	
	/** "<_NULL_>" A non-null value representing a real null value. **/
	public static final String NULL_VALUE = "<_NULL_>";
	
	/**
	 * Create a String from a Throwable suitable for debug output that provides 
	 * comparable information to x.printStackTrace();
	 * @param x
	 * @return String ready for output to debug(String) or other sink.
	 */
	public static String getStackTrace(Throwable x){
		String rc = x.getClass().getName()+", "+ x.getMessage()+"\n";
		StackTraceElement[] se = x.getStackTrace();
		for(StackTraceElement s:se){ rc += s.toString()+"\n"; }
		return rc;
	}	
}
