package org.safs.staf.service.logging;

import java.io.Writer;

import org.safs.Log;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.MessageTypeInfo;

import com.ibm.staf.STAFResult;

/**
 * This class encapsulates a standard SAFS xml file log implemented using STAF.
 * Log messages are written in a temporary STAF log while the log is running. 
 * Upon closing, the content of of the STAF log is formatted to the SAFS xml log 
 * standard and exported to the destination log file.
 * <p>
 * Note that logging function of this class is for local use only.
 * 
 * @since	MAY 19, 2009	(LW)	This class contains most code of org.safs.staf.service.logging.STAFXmlLogItem,
 * 									and the original STAFXmlLogItem is removed.
 */
public abstract class AbstractSTAFXmlLogItem extends STAFFileLogItem 
{
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
	public void init()
	{
		debugLog.debugPrintln("STAFXmlLogItem.init()");

		super.init();
		stafLogLog("<LOG_OPENED date='" + dateTime(0) + "' time='" + 
			dateTime(1) + "'/>");
		stafLogLog("<LOG_VERSION major='" + SAFSLOG_MAJOR_VER + "' minor='" + 
			SAFSLOG_MINOR_VER + "' />");
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
		
		stafLogLog("<LOG_CLOSED date='" + dateTime(0) + "' time='" + 
			dateTime(1) + "'/>");
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
			message = "<LOG_MESSAGE type='" + info.xmlPrefix + "' date='" + 
				nowDate + "' time='" + nowTime + "' >\n" +
				"    <MESSAGE_TEXT><![CDATA[" + msg + "]]></MESSAGE_TEXT>\n";
			if (desc != null && desc.length() > 0)
				message += "    <MESSAGE_DETAILS><![CDATA[" + desc + 
					"]]></MESSAGE_DETAILS>\n";
			message += "</LOG_MESSAGE>";
		}
		else if (msgType == LocalSLSLogFacility.STATUS_REPORT_START) 
		{
			// status report start message
			message = "<STATUS_REPORT name='" + msg + "' date='" + nowDate + 
				"' time='" + nowTime +"' >\n" +	
				"     <STATUS_START_TEXT><![CDATA[" + msg + 
				"]]></STATUS_START_TEXT>\n";
			if (desc != null && desc.length() > 0)
                message += "     <STATUS_START_DETAILS><![CDATA[" + desc + 
					"]]></STATUS_START_DETAILS>";
		}
		else if (msgType == LocalSLSLogFacility.STATUS_REPORT_END) 
		{
			// status report end message
			message = "     <STATUS_END_TEXT><![CDATA[" + msg + 
				"]]></STATUS_END_TEXT>\n";
			if (desc != null && desc.length() > 0)
                message += "     <STATUS_END_DETAILS><![CDATA[" + desc + 
					"]]></STATUS_END_DETAILS>\n";
            message += "</STATUS_REPORT>";
		}
		else if (msgType > LocalSLSLogFacility.STATUS_REPORT_START) 
		{
			message = 
				"     <STATUS_ITEM type='" + info.xmlPrefix + "'>\n" +
				"          <STATUS_ITEM_TEXT><![CDATA[" + msg + 
				"]]></STATUS_ITEM_TEXT>\n";
			if (desc != null && desc.length() > 0)
				message += "          <STATUS_ITEM_DETAILS><![CDATA[" + desc +
					"]]></STATUS_ITEM_DETAILS>\n";
			message += "     </STATUS_ITEM>";
		}
		return message;
	}

	/**
	 * Override superclass prepend to provide a valid XML header if capXML is true.
	 * This routine does nothing if capXML is false.
	 * @param out PrintWriter
	 */
	protected void prependFinalLog(Writer out){
		Log.info("XML Log Item PREPEND processing CAPXML:"+ capXML);
		if (capXML){
			try{
				out.write("<?xml version=\"1.0\" ?>\n");				
				out.write("<SAFS_LOG>\n");
				out.flush();
			}catch(Exception x){;}
		}
	}

	/**
	 * Override superclass prepend to provide a valid XML header if capXML is true.
	 * This routine does nothing if capXML is false.
	 * @param out PrintWriter
	 */
	protected void appendFinalLog(Writer out){
		Log.info("XML Log Item APPEND processing CAPXML:"+ capXML);
		if (capXML){
			try{
				out.write("</SAFS_LOG>\n");
				out.flush();
			}catch(Exception x){;}
		}
	}
}