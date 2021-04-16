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
 * MAY 19, 2009	(Lei Wang) This class contains most code of org.safs.staf.service.logging.STAFXmlLogItem,
 * 									and the original STAFXmlLogItem is removed.
 * APR 28, 2018	(Lei Wang) Used constant instead 'hard-coded string' to generate XML Log file.
 * JUN 05, 2018	(Lei Wang) Added method getInitMessages(), getCloseMessages().
 *                        Modified method init() and close(): write init/close messages properly by superclass method.
 */
package org.safs.staf.service.logging;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.safs.Constants.SAFS_XML_LogConstants;
import org.safs.Log;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.MessageTypeInfo;
import org.safs.net.NetUtilities;

import com.ibm.staf.STAFResult;

/**
 * This class encapsulates a standard SAFS xml file log implemented using STAF.
 * Log messages are written in a temporary STAF log while the log is running.
 * Upon closing, the content of of the STAF log is formatted to the SAFS xml log
 * standard and exported to the destination log file.
 * <p>
 * Note that logging function of this class is for local use only.
 *
 */
public abstract class AbstractSTAFXmlLogItem extends STAFFileLogItem
{
	protected static final String TAG_LOG_OPENED 		= SAFS_XML_LogConstants.TAG_LOG_OPENED;
	protected static final String TAG_LOG_VERSION 		= SAFS_XML_LogConstants.TAG_LOG_VERSION;
	protected static final String TAG_LOG_CLOSED 		= SAFS_XML_LogConstants.TAG_LOG_CLOSED;
	protected static final String TAG_SAFS_LOG 			= SAFS_XML_LogConstants.TAG_SAFS_LOG;

	protected static final String TAG_LOG_MESSAGE 		= SAFS_XML_LogConstants.TAG_LOG_MESSAGE;
	protected static final String TAG_MESSAGE_TEXT		= SAFS_XML_LogConstants.TAG_MESSAGE_TEXT;
	protected static final String TAG_MESSAGE_DETAILS	= SAFS_XML_LogConstants.TAG_MESSAGE_DETAILS;

	protected static final String TAG_STATUS_REPORT 		= SAFS_XML_LogConstants.TAG_STATUS_REPORT;
	protected static final String TAG_STATUS_ITEM	 		= SAFS_XML_LogConstants.TAG_STATUS_ITEM;
	protected static final String TAG_STATUS_ITEM_TEXT		= SAFS_XML_LogConstants.TAG_STATUS_ITEM_TEXT;
	protected static final String TAG_STATUS_ITEM_DETAILS	= SAFS_XML_LogConstants.TAG_STATUS_ITEM_DETAILS;
	protected static final String TAG_STATUS_START_TEXT		= SAFS_XML_LogConstants.TAG_STATUS_START_TEXT;
	protected static final String TAG_STATUS_START_DETAILS	= SAFS_XML_LogConstants.TAG_STATUS_START_DETAILS;
	protected static final String TAG_STATUS_END_TEXT		= SAFS_XML_LogConstants.TAG_STATUS_END_TEXT;
	protected static final String TAG_STATUS_END_DETAILS	= SAFS_XML_LogConstants.TAG_STATUS_END_DETAILS;

	protected static final String PROPERTY_TYPE = SAFS_XML_LogConstants.PROPERTY_TYPE;
	protected static final String PROPERTY_DATE = SAFS_XML_LogConstants.PROPERTY_DATE;
	protected static final String PROPERTY_TIME = SAFS_XML_LogConstants.PROPERTY_TIME;
	protected static final String PROPERTY_USER = SAFS_XML_LogConstants.PROPERTY_USER;
	protected static final String PROPERTY_MACHINE = SAFS_XML_LogConstants.PROPERTY_MACHINE;
	protected static final String PROPERTY_IP = SAFS_XML_LogConstants.PROPERTY_IP;

	protected static final String PROPERTY_NAME = SAFS_XML_LogConstants.PROPERTY_NAME;

	protected static final String PROPERTY_MAJOR = SAFS_XML_LogConstants.PROPERTY_MAJOR;
	protected static final String PROPERTY_MINOR = SAFS_XML_LogConstants.PROPERTY_MINOR;

	/**
	 * TRUE if the XML log should be capped at closure.
	 * Capping the log provides final XML header and footer text to make the XML valid.
	 */
	protected boolean capXML = false;

	/**
	 * Creates a disabled <code>AbstractSTAFXmlLogItem</code> with default log level
	 * (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_XML</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFXmlLogItem(String name, String parent, String file)
	{
		super(name, AbstractLogFacility.LOGMODE_SAFS_XML, false, parent, file);
	}

	/**
	 * Creates a disabled <code>AbstractSTAFXmlLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_XML</code>.
	 * <p>
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFXmlLogItem(String parent, String file)
	{
		super(AbstractLogFacility.LOGMODE_SAFS_XML, parent, file);
	}

	/**
	 * Creates a disabled <code>AbstractSTAFXmlLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>), and empty parent
	 * directory.
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_XML</code>.
	 * <p>
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFXmlLogItem(String file)
	{
		this(null, file);
	}

	/**
	 * Initializes this log and writes the log open message.
	 * <p>
	 */
	@Override
	public void init()
	{
		debugLog.debugPrintln("STAFXmlLogItem.init()");
		super.init();
	}

	@Override
	protected List<String> getInitMessages(){
		List<String> messages = new ArrayList<String>();
		messages.add("<"+TAG_LOG_OPENED+" "+PROPERTY_DATE+"='" + dateTime(0) + "' "+PROPERTY_TIME+"='" + dateTime(1) + "' "
				+ PROPERTY_NAME+"='"+getTestName()+"' "
				+ PROPERTY_USER+"='"+System.getProperty("user.name")+"' "+PROPERTY_MACHINE+"='"+NetUtilities.getLocalHostName()+"' "
				+ PROPERTY_IP+ "='"+NetUtilities.getLocalHostIP()+"' />");
		messages.add("<"+TAG_LOG_VERSION+" "+PROPERTY_MAJOR+"='" + SAFSLOG_MAJOR_VER + "' "+PROPERTY_MINOR+"='" + SAFSLOG_MINOR_VER + "' />");
		return messages;
	}

	@Override
	protected List<String> getCloseMessages(){
		List<String> messages = new ArrayList<String>();
		messages.add("<"+TAG_LOG_CLOSED+" "+PROPERTY_DATE+"='" + dateTime(0) + "' "+PROPERTY_TIME+"='" + dateTime(1) + "'/>");
		return messages;
	}

	/**
	 * Set true if this log should be "capped" (made valid XML) upon closure.
	 *
	 * @param capbool
	 */
	public void setCapXML(boolean capbool){
		capXML = capbool;
		Log.info("XML Log Item setting CAPXML:"+ capXML);
	}

	/**
	 * Returns the current setting of whether the XML log will be capped on closure.
	 *
	 * @return true if the log is set to be XML capped on closure.
	 */
	public boolean getCapXML(){ return capXML;}

	/**
	 * Logs a message in standard SAFS xml log format if enabled and open.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	@Override
	public void logMessage(String msg,	String desc, int msgType)
	{
		if (!enabled || closed || msg == null || msg.length() <= 0) return;

		MessageTypeInfo info = MessageTypeInfo.get(msgType);
		if (info == null)
			info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
		stafLogLog(formatMessage(msg, desc, info), info.stafLevel);
	}

	/**
	 * Logs a message in standard SAFS text log format if enabled and open.
	 * <p>
	 * @param msg		the message to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	public void logMessage(String msg, int msgType)
	{
		logMessage(msg, null, msgType);
	}

	/**
	 * Logs a message in standard SAFS text log format if enabled and open.
	 * <p>
	 * Message type defaults to <code>GENERIC_MESSAGE</code>.
	 * <p>
	 * @param msg		the message to log.
	 */
	public void logMessage(String msg)
	{
		logMessage(msg, null, LocalSLSLogFacility.GENERIC_MESSAGE);
	}

	/**
	 * Close this log if enabled and open.
	 * <p>
	 * @throws	STAFLogException
	 * 			if this method failed for any reason.
	 */
	@Override
	public void close() throws STAFLogException
	{
		// if not enabled or already closed, do nothing
		if (!enabled || closed) return;

		if (fileExists())
		{
			 throw new STAFLogException(
				getAbsolutePath() + " already exists.",
				new STAFResult(STAFResult.AlreadyExists, getAbsolutePath()));
		}

		writeCloseMessages();
		finalizeLogFile();
		closed = true;
	}

	/**
	 * Prepares the message to standard SAFS xml format.
	 * <p>
	 * @param msg	the message to log.
	 * @param desc	additional description to log.
	 * @param info	the constant info for the message type.
	 * <p>
	 * @return		the standard-SAFS-xml-formatted message.
	 */
	protected String formatMessage(String msg, String desc, MessageTypeInfo info)
	{
		int msgType = info.type;
		String nowDate = dateTime(0);
		String nowTime = dateTime(1);
		String message = "";

		if (msgType < LocalSLSLogFacility.STATUS_REPORT_START ||
			msgType > LocalSLSLogFacility.STATUS_REPORT_END)
		{
			// non status report messages
			message = "<"+TAG_LOG_MESSAGE+" "+PROPERTY_TYPE+"='" + info.xmlPrefix + "' "+PROPERTY_DATE+"='" +
				nowDate + "' "+PROPERTY_TIME+"='" + nowTime + "' >\n" +
				"    <"+TAG_MESSAGE_TEXT+"><![CDATA[" + msg + "]]></"+TAG_MESSAGE_TEXT+">\n";
			if (desc != null && desc.length() > 0)
				message += "    <"+TAG_MESSAGE_DETAILS+"><![CDATA[" + desc +
					"]]></"+TAG_MESSAGE_DETAILS+">\n";
			message += "</"+TAG_LOG_MESSAGE+">";
		}
		else if (msgType == LocalSLSLogFacility.STATUS_REPORT_START)
		{
			// status report start message
			message = "<"+TAG_STATUS_REPORT+" "+PROPERTY_NAME+"='" + msg + "' "+PROPERTY_DATE+"='" + nowDate +
				"' "+PROPERTY_TIME+"='" + nowTime +"' >\n" +
				"     <"+TAG_STATUS_START_TEXT+"><![CDATA[" + msg +
				"]]></"+TAG_STATUS_START_TEXT+">\n";
			if (desc != null && desc.length() > 0)
                message += "     <"+TAG_STATUS_START_DETAILS+"><![CDATA[" + desc +
					"]]></"+TAG_STATUS_START_DETAILS+">";
		}
		else if (msgType == LocalSLSLogFacility.STATUS_REPORT_END)
		{
			// status report end message
			message = "     <"+TAG_STATUS_END_TEXT+"><![CDATA[" + msg +
				"]]></"+TAG_STATUS_END_TEXT+">\n";
			if (desc != null && desc.length() > 0)
                message += "     <"+TAG_STATUS_END_DETAILS+"><![CDATA[" + desc +
					"]]></"+TAG_STATUS_END_DETAILS+">\n";
            message += "</"+TAG_STATUS_REPORT+">";
		}
		else if (msgType > LocalSLSLogFacility.STATUS_REPORT_START)
		{
			message =
				"     <"+TAG_STATUS_ITEM+" "+PROPERTY_TYPE+"='" + info.xmlPrefix + "'>\n" +
				"          <"+TAG_STATUS_ITEM_TEXT+"><![CDATA[" + msg +
				"]]></"+TAG_STATUS_ITEM_TEXT+">\n";
			if (desc != null && desc.length() > 0)
				message += "          <"+TAG_STATUS_ITEM_DETAILS+"><![CDATA[" + desc +
					"]]></"+TAG_STATUS_ITEM_DETAILS+">\n";
			message += "     </"+TAG_STATUS_ITEM+">";
		}
		return message;
	}

	/**
	 * Override superclass prepend to provide a valid XML header if capXML is true.
	 * This routine does nothing if capXML is false.
	 * @param out PrintWriter
	 */
	@Override
	protected void prependFinalLog(Writer out){
		Log.info("XML Log Item PREPEND processing CAPXML:"+ capXML);
		if (capXML){
			try{
				out.write("<?xml version=\"1.0\" ?>\n");
				out.write("<"+TAG_SAFS_LOG+">\n");
				out.flush();
			}catch(Exception x){;}
		}
	}

	/**
	 * Override superclass prepend to provide a valid XML header if capXML is true.
	 * This routine does nothing if capXML is false.
	 * @param out PrintWriter
	 */
	@Override
	protected void appendFinalLog(Writer out){
		Log.info("XML Log Item APPEND processing CAPXML:"+ capXML);
		if (capXML){
			try{
				out.write("</"+TAG_SAFS_LOG+">\n");
				out.flush();
			}catch(Exception x){;}
		}
	}
}
