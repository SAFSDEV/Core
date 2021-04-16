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
 * JUL 11, 2018 (Lei Wang) Moved common codes from DebugLoggerAdapter and TestLoggerAdapter.
 *                        Provided void implementation for method debug()
 *
 */
package org.safs.logging.slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * <p>
 * This Logger will NOT write message to anywhere, it is a void logger.
 * <p>
 * @see SAFSLoggerFactory#getLogger(String)
 */
@SuppressWarnings("serial")
public class VoidLoggerAdapter extends MarkerIgnoringBase {

	protected static boolean debugEnabled = true;
	protected static boolean warnEnabled = true;
	protected static boolean traceEnabled = false;
	protected static boolean fatalEnabled = true;
    protected static boolean infoEnabled = true;
    protected static boolean errorEnabled = true;

	/**
	 * @param t Throwable
	 * @return String representation of Throwable.printStackTrace
	 */
    protected String getThrownString(Throwable t){
	    OutputStream os = new ByteArrayOutputStream();
	    String m = "";
	    try {
		    t.printStackTrace(new PrintStream(os));
	    	m = os.toString();
	    	os.close();
	    } catch (IOException io){}
	    return m;
	}

	@Override
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override
	public boolean isErrorEnabled() {
		return errorEnabled;
	}

	@Override
	public boolean isInfoEnabled() {
		return infoEnabled;
	}

	@Override
	public boolean isTraceEnabled() {
		return traceEnabled;
	}

	@Override
	public boolean isWarnEnabled() {
		return warnEnabled;
	}

	@Override
	public void debug(String arg0) {
	}

	@Override
	public void debug(String arg0, Object arg1) {
	}

	@Override
	public void debug(String arg0, Object... arg1) {
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
	}

	@Override
	public void error(String arg0) {
	}

	@Override
	public void error(String arg0, Object arg1) {
	}

	@Override
	public void error(String arg0, Object... arg1) {
	}

	@Override
	public void error(String arg0, Throwable arg1) {
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
	}

	@Override
	public void info(String arg0) {
	}

	@Override
	public void info(String arg0, Object arg1) {
	}

	@Override
	public void info(String arg0, Object... arg1) {
	}

	@Override
	public void info(String arg0, Throwable arg1) {
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
	}

	@Override
	public void trace(String arg0) {
	}

	@Override
	public void trace(String arg0, Object arg1) {
	}

	@Override
	public void trace(String arg0, Object... arg1) {
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
	}

	@Override
	public void warn(String arg0) {
	}

	@Override
	public void warn(String arg0, Object arg1) {
	}

	@Override
	public void warn(String arg0, Object... arg1) {
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
	}
}
