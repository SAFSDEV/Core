/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs;

/**
 * Constants and utilities used by a JavaSocketsHook and a remote SocketProtocol client.
 * @author Carl Nagle
 */
public class JavaSocketsUtils extends org.safs.sockets.Message{

	/** "logtype" -- Property name key for Properties returned in resultsProps messages. 
	 * This property holds a String representation for the "type" of message to log.
	 * <p> 
	 * Valid logtypes are found in {@link org.safs.logging.AbstractLogFacility}.  The most 
	 * common of these are already encoded below in the LOGTYPE_ fields.
	 * */
	public static final String KEY_LOGTYPE    = "logtype";

	/** "1024" FAILED log message. */
	public static final String LOGTYPE_FAILED_MESSAGE = "1024";
	
	/** "1025"  FAILED_OK log message. */
	public static final String LOGTYPE_FAILED_OK_MESSAGE = "1025";
	
	/** "0" GENERIC log message. */
	public static final String LOGTYPE_GENERIC_MESSAGE = "0";
	
	/** "2048" PASSED/OK log message. */
	public static final String LOGTYPE_PASSED_MESSAGE = "2048";
	
	/** "4096" WARNING log message. */
	public static final String LOGTYPE_WARNING_MESSAGE = "4096";
	
	/** "4097" WARNING OK log message. */
	public static final String LOGTYPE_WARNING_OK_MESSAGE = "4097";
	
	/** "logcomment" -- Property name key for Properties returned in resultsProps messages. 
	 * This property holds the log comment to be used "as-is".  The comment is NOT the name of 
	 * a string resource stored in SAFS GENERIC or FAILED resource bundles. */
	public static final String KEY_LOGCOMMENT = "logcomment";

	/** "logdetail" -- Property name key for Properties returned in resultsProps messages. 
	 * This property, if present, holds the log detail to be used "as-is".  
	 * The detail is NOT the name of a string resource stored in SAFS GENERIC or FAILED 
	 * resource bundles. */
	public static final String KEY_LOGDETAIL  = "logdetail";
	
	/** "logcomment_generic" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds the name of the key to use when converting a String resource 
	 * message in the Generic Strings Resource Bundle for a log comment.
	 * @see org.safs.text.GENStrings#convert(String, String, java.util.Collection) */
	public static final String KEY_LOGCOMMENT_GENERIC = "logcomment_generic";

	/** "logcomment_failed" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds the name of the key to use when converting a String resource 
	 * message in the Failed Strings Resource Bundle for a log comment.
	 * @see org.safs.text.FAILStrings#convert(String, String, java.util.Collection)*/
	public static final String KEY_LOGCOMMENT_FAILED  = "logcomment_failed";

	/** "logcomment_params" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds comma-delimited parameter values used when converting a 
	 * String resource message from either the Generic Strings bundle or the Failed Strings bundle. 
	 * @see org.safs.text.GENStrings#convert(String, String, java.util.Collection)
	 * @see org.safs.text.FAILStrings#convert(String, String, java.util.Collection) */
	public static final String KEY_LOGCOMMENT_PARAMS  = "logcomment_params";
	
	/** "logdetail_generic" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds the name of the key to use when converting a String resource 
	 * message in the Generic Strings Resource Bundle for a log detail.
	 * @see org.safs.text.GENStrings#convert(String, String, java.util.Collection) */
	public static final String KEY_LOGDETAIL_GENERIC  = "logdetail_generic";

	/** "logdetail_failed" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds the name of the key to use when converting a String resource 
	 * message in the Failed Strings Resource Bundle for a log detail.
	 * @see org.safs.text.FAILStrings#convert(String, String, java.util.Collection)*/
	public static final String KEY_LOGDETAIL_FAILED   = "logdetail_failed";

	/** "logdetail_params" -- Property name key for Properties returned in resultsProps messages.
	 * This property, if present, holds comma-delimited parameter values used when converting a 
	 * String resource message from either the Generic Strings bundle or the Failed Strings bundle 
	 * for a log detail. 
	 * @see org.safs.text.GENStrings#convert(String, String, java.util.Collection)
	 * @see org.safs.text.FAILStrings#convert(String, String, java.util.Collection) */
	public static final String KEY_LOGDETAIL_PARAMS   = "logdetail_params";
	
}
