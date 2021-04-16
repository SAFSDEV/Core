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
package org.safs.staf.service.logging;
/**
 * JUN 05, 2018	(Lei Wang) Added method getInitMessages(), getCloseMessages().
 *                        Modified method init() and close(): write init/close messages properly by superclass method.
 */
import java.util.ArrayList;
import java.util.List;

import org.safs.logging.AbstractLogFacility;
import org.safs.logging.MessageTypeInfo;
import org.safs.net.NetUtilities;

import com.ibm.staf.STAFResult;

/**
 * This class encapsulates a standard SAFS text file log implemented using STAF.
 * Log messages are written to a temporary STAF log while the log is running.
 * Upon closing, the content of of the STAF log is formatted to the SAFS text
 * log standard and exported to the destination log file.
 * <p>
 * Note that logging function of this class is for local use only.
 *
 * @since	MAY 19, 2009	(LW)	This class contains most code of org.safs.staf.service.logging.STAFTextLogItem,
 * 									and the original STAFTextLogItem is removed.
 */
public abstract class AbstractSTAFTextLogItem extends STAFFileLogItem
{

	/**
	 * Creates a disabled <code>AbstractSTAFTextLogItem</code> with default log level
	 * (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFTextLogItem(String name, String parent, String file)
	{
		super(name, AbstractLogFacility.LOGMODE_SAFS_TEXT, false, parent, file);
	}

	/**
	 * Creates a disabled <code>AbstractSTAFTextLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFTextLogItem(String parent, String file)
	{
		super(AbstractLogFacility.LOGMODE_SAFS_TEXT, parent, file);
	}

	/**
	 * Creates a disabled <code>AbstractSTAFTextLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>), and empty parent
	 * directory.
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param file		the file spec of this log.
	 */
	public AbstractSTAFTextLogItem(String file)
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
		debugLog.debugPrintln("AbstractSTAFTextLogItem.init()");
		super.init();
	}

	/**
	 * Logs a message in standard SAFS text log format if enabled and open.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	@Override
	public void logMessage(String msg,	String desc, int msgType)
	{
		if (!enabled || closed || msg == null || msg.length() <= 0) return;

		// format and log the message.
		MessageTypeInfo info = MessageTypeInfo.get(msgType);
		if (info == null)
			info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
		stafLogLog(info.textPrefix + msg, info.stafLevel);

		// additional description, if specified, is logged as GENERIC_MESSAGE
		if (desc != null && desc.length() > 0)
		{
			info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
			stafLogLog(info.textPrefix + desc);
		}
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

	@Override
	protected List<String> getInitMessages(){
		List<String> messages = new ArrayList<String>();
		messages.add("Version " + SAFSLOG_MAJOR_VER + "." + SAFSLOG_MINOR_VER);
		messages.add("Log OPENED <"+getTestName()+"> by '"+System.getProperty("user.name")+"' on '"+NetUtilities.getLocalHostName()+"'('"+NetUtilities.getLocalHostIP()+"') at " + dateTime(0) + " " + dateTime(1));
		return messages;
	}

	@Override
	protected List<String> getCloseMessages(){
		List<String> messages = new ArrayList<String>();
		messages.add("Log CLOSED " + dateTime(0) + " " + dateTime(1));
		return messages;
	}
}
