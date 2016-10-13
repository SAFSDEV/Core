/**
 * Developer Logs:
 * OCT 10, 2016		(SBJLWA)	Modified dateTime(): the hour will be always in 2-digit format.
 */
package org.safs.logging;

import java.util.*;
import java.text.*;

/**
 * This class is the abstract representation of a single log in a log facility.
 * <p>
 * The type of the log (i.e. tool-specific, plain text, xml etc.) is identified
 * by the public <code>mode</code> field. Valid values are the 
 * <code>LOGMODE</code> constants defined by <code>AbstractLogFacility</code>.
 * Attributes common to all log types, such as name, log level, enabled state,
 * and closed state etc, are encapsulated in this class.
 * <p>
 * This class declares two abstract methods: 
 * <code>{@link #logMessage logMessage}</code> and
 * <code>{@link #close close}</code>. They should be implmented by subclasses
 * for specific log types that know how to provide those functionality. A 
 * concrete log facility could include instances of appropriate subclasses of
 * <code>LogItem</code> and delegate its logging functions to them.
 * 
 * @see AbstractLogFacility
 */
public abstract class LogItem
{
	/**
	 * The name of this log item.
	 */
	public String name;
	/**
	 * The type of this log item (one of the <code>LOGMODE</code> constants 
	 * defined by <code>AbstractLogFacility</code>).
	 */
	public long mode;
	/**
	 * The log level of this log item (one of the <code>LOGLEVEL</code>
	 * constants defined by <code>AbstractLogFacility</code>).
	 */
	public int level;
	/**
	 * The enabled state of this log.
	 */
	public boolean enabled;
	protected boolean closed = false;

	/**
	 * Creates a <code>LogItem</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param level		the log level for this log.
	 * @param enabled	<code>true</code> to enable this log; <code>false</code>
	 * 					to disable.
	 */
	public LogItem(String name, long mode, int level, boolean enabled)
	{
		this.name = (name == null)? "" : name;
		this.mode = mode;
		this.level = level;
		this.enabled = enabled;
	}

	/**
	 * Creates a log and sets its log level to <code>LOGLEVEL_INFO</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param enabled	<code>true</code> to enable this log; <code>false</code>
	 * 					to disable.
	 */
	public LogItem(String name, long mode, boolean enabled)
	{
		this(name, mode, AbstractLogFacility.LOGLEVEL_INFO, enabled);
	}

	/**
	 * Creates a disabled log and sets its log level to 
	 * <code>LOGLEVEL_INFO</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 */
	public LogItem(String name, long mode)
	{
		this(name, mode, AbstractLogFacility.LOGLEVEL_INFO, false);
	}

	/**
	 * Tests if this log is closed.
	 * <p>
	 * @return	<code>true</code> if this log is closed; <code>false</code>
	 * 			if it is open.
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Returns the string representation of the current date or time.
	 * <p>
	 * @param dateOrTime	0 to return date; 1 to return time
	 */
	public static String dateTime(int dateOrTime)
	{
		String pattern = (dateOrTime == 0)? "MM-dd-yyyy" : "HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(new Date());
	}

	/**
	 * Logs a message to this log.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	public abstract void logMessage(String msg,	String desc, int msgType);

	/**
	 * Closes this log.
	 * <p>
	 * @throws	LogException
	 * 			if this log failed to close for any reason.
	 */
	public abstract void close() throws LogException;

}