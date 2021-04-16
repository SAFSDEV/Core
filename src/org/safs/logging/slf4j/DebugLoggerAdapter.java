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
 * JUL 11, 2018 (Lei Wang) Moved common codes to VoidLoggerAdapter.
 *                        Modified debug(), error() etc.: checked isXXXEnabled() before writing message.
 */
package org.safs.logging.slf4j;

import org.safs.IndependantLog;
import org.slf4j.helpers.MessageFormatter;

/**
 * SAFS Logger Adapter writing only to the SAFS Debug Log--if it is running/available.
 * <p>
 * This Logger will be acquired if requesting the "DEBUG" logname from the SAFSLoggerFactory.
 * <p>
 * This Logger does NOT require any post-acquisition initialization.
 * <p>
 * @author Carl Nagle
 * @see SAFSLoggerFactory#getLogger(String)
 */
@SuppressWarnings("serial")
public class DebugLoggerAdapter extends VoidLoggerAdapter {

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
		if(isDebugEnabled()) IndependantLog.debug(msg, arg1);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		debug(format, new Object[]{arg1, arg2});
	}

	@Override
	public void error(String msg) {
		if(isErrorEnabled()) IndependantLog.error(msg);
	}

	@Override
	public void error(String format, Object arg) {
		error(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void error(String format, Object... args) {
		error(MessageFormatter.arrayFormat(format, args).getMessage());
	}

	@Override
	public void error(String msg, Throwable arg1) {
		if(isErrorEnabled())  IndependantLog.error(msg, arg1);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		error(format, new Object[]{arg1, arg2});
	}

	@Override
	public void info(String msg) {
		if(isInfoEnabled())  IndependantLog.info(msg);
	}

	@Override
	public void info(String format, Object arg) {
		info(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void info(String format, Object... args) {
		info(MessageFormatter.arrayFormat(format, args).getMessage());
	}

	@Override
	public void info(String msg, Throwable arg1) {
		if(isInfoEnabled())  IndependantLog.info(msg, arg1);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		info(format, new Object[]{arg1, arg2});
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
	public void trace(String format, Object... arg1) {
		trace(MessageFormatter.format(format, arg1).getMessage());
	}

	@Override
	public void trace(String msg, Throwable arg1) {
		if(isTraceEnabled()) IndependantLog.debug(msg, arg1);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		trace(format, new Object[]{arg1, arg2});
	}

	@Override
	public void warn(String msg) {
		if(isWarnEnabled()) IndependantLog.warn(msg);
	}

	@Override
	public void warn(String format, Object arg) {
		warn(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void warn(String format, Object... arg1) {
		warn(MessageFormatter.arrayFormat(format, arg1).getMessage());
	}

	@Override
	public void warn(String msg, Throwable arg1) {
		if(isWarnEnabled()) IndependantLog.warn(msg +"\n"+ getThrownString(arg1));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		warn(format, new Object[]{arg1,arg2});
	}
}
