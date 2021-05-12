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

import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.slf4j.helpers.MessageFormatter;

/**
 * SAFS Logger Adapter writing only to the SAFS Test Log only.
 * <p>
 * This Logger will be acquired if requesting the "TEST" logname from the SAFSLoggerFactory.
 * <p>
 * This Logger DOES require post-acquisition initialization.
 * <p>
 * @author Carl Nagle
 * @see SAFSLoggerFactory#getLogger(String)
 * @see SAFSLoggerFactory#initializeTestLoggerAdapter(org.slf4j.Logger, org.safs.tools.drivers.AbstractDriver)
 */
@SuppressWarnings("serial")
public class TestLoggerAdapter extends VoidLoggerAdapter {

	static STAFHelper staf = SingletonSTAFHelper.getHelper();
	static LogUtilities log = new LogUtilities(staf);
	static String facname;

	public String getFacName(){
		return log.getLastNamedLogFacility()== null? facname : log.getLastNamedLogFacility();
	}

	/**
	 * Usually set internally during initialization.
	 * @param safslog -- The name of the SAFS Log to be used.
	 * This will be set during post-acquisition initialization from SAFSLoggerFactory.
	 * @see SAFSLoggerFactory#initializeTestLoggerAdapter(org.slf4j.Logger, org.safs.tools.drivers.AbstractDriver)
	 */
	public void setLogFacName(String facname){
		TestLoggerAdapter.facname = facname;
	}

	@Override
	public void debug(String msg) {
		if(isDebugEnabled()){
			log.logMessage(getFacName(), msg, AbstractLogFacility.DEBUG_MESSAGE);
		}
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
		debug(msg +"\n"+ this.getThrownString(arg1));
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		debug(format, new Object[]{arg1, arg2});
	}

	@Override
	public void error(String msg) {
		if(isErrorEnabled()){
			log.logMessage(getFacName(), msg, AbstractLogFacility.FAILED_MESSAGE);
		}
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
		error(msg +"\n"+ this.getThrownString(arg1));
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		error(format, new Object[]{arg1, arg2});
	}

	@Override
	public void info(String msg) {
		if(isInfoEnabled()){
			log.logMessage(getFacName(), msg, AbstractLogFacility.GENERIC_MESSAGE);
		}
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
		info(msg +"\n"+ this.getThrownString(arg1));
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		info(format, new Object[]{arg1, arg2});
	}

	@Override
	public void trace(String msg) {
		if(isTraceEnabled()){
			log.logMessage(getFacName(), msg, AbstractLogFacility.DEBUG_MESSAGE);
		}
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
		trace(msg +"\n"+ this.getThrownString(arg1));
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		trace(format, new Object[]{arg1, arg2});
	}

	@Override
	public void warn(String msg) {
		if(isWarnEnabled()){
			log.logMessage(getFacName(), msg, AbstractLogFacility.WARNING_MESSAGE);
		}
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
		warn(msg+"\n"+ getThrownString(arg1));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		warn(format, new Object[]{arg1,arg2});
	}
}
