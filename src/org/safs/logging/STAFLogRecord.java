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
