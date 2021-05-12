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

/**
 * JAN 12, 2017 (Lei Wang) Create a LogUtilities with the a log facility, so that it can be used as a SAFS Log.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.safs.STAFHelper;

/**
 * Extends LogUtilities to provide the apache.commons.logging.Log Interface.
 * This dependency "might" not always be present at runtime.
 * @author Carl Nagle
 */
public class ApacheLogUtilities extends LogUtilities implements org.apache.commons.logging.Log{

	/* don't know that this will be used for anything. */
	private String apachelogname = null;

	/** super() */
	public ApacheLogUtilities() {
		super();
	}

	/** Satisfies the org.apache.commons.logging.Log Interface requirements.
	 * However, we do NOT initialize any type of log via this Constructor.
	 * The logname is currently NOT used in our implementation. We defer to the existing
	 * instances of LogFacility being run by SAFSLOGS through the LogUtilities superclass.
	 * <p>
	 * super()*/
	public ApacheLogUtilities(String logname) {
		super();
		this.apachelogname = logname;
	}

	/**
	 * Create a LogUtilities with the a log facility, so that it can be used as a SAFS Log.
	 * <p>
	 * this(logname)*/
	public ApacheLogUtilities(String logname, String logFac) {
		this(logname);
		this.lastNamedLogFacility = logFac;
	}

	/** super(helper) */
	public ApacheLogUtilities(STAFHelper helper) {
		super(helper);
	}

	/** super(helper, copyLogClass) */
	public ApacheLogUtilities(STAFHelper helper, boolean copyLogClass) {
		super(helper, copyLogClass);
	}

	/**
	 * @param t Throwable
	 * @return String representation of Throwable.printStackTrace
	 */
	private String getThrownString(Throwable t){
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
	public void debug(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.DEBUG_MESSAGE );
	}

	@Override
	public void debug(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.DEBUG_MESSAGE );
	}

	@Override
	public void error(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.FAILED_MESSAGE );
	}

	@Override
	public void error(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.FAILED_MESSAGE );
	}

	@Override
	public void fatal(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.FAILED_MESSAGE );

	}

	@Override
	public void fatal(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.FAILED_MESSAGE );
	}

	@Override
	public void info(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.GENERIC_MESSAGE );

	}

	@Override
	public void info(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.GENERIC_MESSAGE );
	}

	@Override
	/** true */
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	/** true */
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	/** false */
	public boolean isFatalEnabled() {
		return false;
	}

	@Override
	/** true */
	public boolean isInfoEnabled() {
		return true;
	}


	@Override
	/** false */
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	/** true */
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void trace(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.CUSTOM_MESSAGE );
	}

	@Override
	public void trace(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.CUSTOM_MESSAGE );
	}

	@Override
	public void warn(Object message) {
		String m = message == null ? " ":message.toString();
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.WARNING_MESSAGE );
	}

	@Override
	public void warn(Object message, Throwable t) {
		String m = message == null ? " ":message.toString();
	    m+=",\n"+getThrownString(t);
		logMessage(lastNamedLogFacility, m, AbstractLogFacility.WARNING_MESSAGE );
	}
}
