package org.safs.logging;

import java.util.*;

import com.ibm.staf.*;

/**
 * This class parses STAF LOG record and separates it into individual fields.
 * <p>
 */
public class STAFLogRecord 
{
	/**
	 * The date part of the time stamp field.
	 */
	public String date = null;
	/**
	 * The time part of the time stamp field.
	 */
	public String time = null;
	/**
	 * The machine field of the record.
	 */
	public String machine = null;
	/**
	 * The handle field of the record.
	 */
	public int handle = 0;
	/**
	 * The name field of the record.
	 */
	public String processName = null;
	/**
	 * The level field of the record.
	 */
	public String level = null;
	/**
	 * The actual log message.
	 */
	public String message = "";

	/**
	 * Creates a <code>STAFLogRecord</code> from a record string.
	 * <p>
	 * @param record	the full log record as returned by STAF.
	 * @param sep		the field separator character.
	 */
	public STAFLogRecord(String record, char sep)
	{
		StringTokenizer st = new StringTokenizer(record, String.valueOf(sep));
		if (st.hasMoreTokens())
		{
			String dateTime = st.nextToken();
			int pos = dateTime.indexOf("-");
			date = dateTime.substring(0, pos);
			time = dateTime.substring(pos + 1);
		}
		if (st.hasMoreTokens()) machine = st.nextToken();
		if (st.hasMoreTokens()) handle = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens()) processName = st.nextToken();
		if (st.hasMoreTokens()) level = st.nextToken();
		if (st.hasMoreTokens()) message = st.nextToken();
	}

	/**
	 * Constructs a <code>STAFLogRecord</code>.
	 * <p>
	 * '|' is used as the field separator.
	 * <p>
	 * @param record	the full log record as returned by STAF.
	 */
	public STAFLogRecord(String record)
	{
		this(record, '|');
	}

	/**
	 * Parses a log record and returns the message field.
	 * <p>
	 * This method is more efficient than the constructor.
	 * <p>
	 * @param record	the full log record as returned by STAF.
	 * @param sep		the field separator character.
	 */
	public static String parseMessage(String record, char sep)
	{
		StringTokenizer st = new StringTokenizer(record, String.valueOf(sep));
		int count = 0;
		while (st.hasMoreTokens() && count < 5)
		{
			st.nextToken();
			count++;
		}
		// return null if not enough fields (wrong record format)
		if (count < 5) return null;
		String msg = (st.hasMoreTokens())? st.nextToken() : "";
		return msg;
	}

	/**
	 * Parses a log record and returns the message field.
	 * <p>
	 * '|' is used as the field separator. This method is more efficient than 
	 * the constructor.
	 * <p>
	 * @param record	the full log record as returned by STAF.
	 */
	public static String parseMessage(String record)
	{
		return parseMessage(record, '|');
	}
}