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
 * JUL 11, 2018    (Lei Wang) Modified isXXXEnabled(): check accpetDebugLog and accpetTestLog
 */
package org.safs.logging.slf4j;

import org.safs.IndependantLog;
import org.slf4j.helpers.MessageFormatter;

/**
 * SAFS Logger Adapter writing to BOTH the SAFS Test Log and the SAFS Debug Log--if available.
 * <p>
 * This Logger will be acquired if requesting anything other than a "TEST" or "DEBUG" logname.
 * <p>
 * This Logger DOES require post-acquisition initialization.
 * <p>
 * Logger info, warn, and error messages will go to the SAFS Test Log.<br>
 * Logger debug and trace messages will go to the SAFS Debug Log--if available.
 * <p>
 * @author Carl Nagle
 * @see SAFSLoggerFactory#getLogger(String)
 * @see SAFSLoggerFactory#initializeTestLoggerAdapter(org.slf4j.Logger, org.safs.tools.drivers.AbstractDriver)
 */
@SuppressWarnings("serial")
public class CombinedLoggerAdapter extends TestLoggerAdapter {

	//Check accpetDebugLog for 'debug' and 'trace' message, as they go into SAFS Debug log.
	@Override
	public boolean isDebugEnabled() {
		return SAFSLoggerFactory.accpetDebugLog && debugEnabled;
	}

	@Override
	public boolean isTraceEnabled() {
		return SAFSLoggerFactory.accpetDebugLog && traceEnabled;
	}

	//Check accpetTestLog for 'info', 'warn' and 'error' message, as they go into SAFS Test log.
	@Override
	public boolean isErrorEnabled() {
		return SAFSLoggerFactory.accpetTestLog && errorEnabled;
	}

	@Override
	public boolean isInfoEnabled() {
		return SAFSLoggerFactory.accpetTestLog && infoEnabled;
	}

	@Override
	public boolean isWarnEnabled() {
		return SAFSLoggerFactory.accpetTestLog && warnEnabled;
	}

	@Override
	public void debug(String msg) {
		if(isDebugEnabled()) IndependantLog.debug(msg);
	}

	@Override
	public void debug(String format, Object arg) {
		debug(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void debug(String format, Object... args) {
		debug(MessageFormatter.arrayFormat(format, args).getMessage());
	}

	@Override
	public void debug(String msg, Throwable arg1) {
		debug(msg +"\n"+ getThrownString(arg1));
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		debug(format, new Object[]{arg1, arg2});
	}

	@Override
	public void trace(String msg) {
		if(isTraceEnabled()) IndependantLog.debug(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		trace(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void trace(String format, Object... args) {
		trace(MessageFormatter.arrayFormat(format, args).getMessage());
	}

	@Override
	public void trace(String msg, Throwable arg1) {
		trace(msg +"\n"+ getThrownString(arg1));
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		trace(format, new Object[]{arg1, arg2});
	}

}
