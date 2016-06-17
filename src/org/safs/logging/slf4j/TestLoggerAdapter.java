/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.logging.slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * SAFS Logger Adapter writing only to the SAFS Test Log only.
 * <p>
 * This Logger will be acquired if requesting the "TEST" logname from the SAFSLoggerFactory.
 * <p>
 * This Logger DOES require post-acquisition initialization.
 * <p>
 * @author canagl
 * @see SAFSLoggerFactory#getLogger(String)
 * @see SAFSLoggerFactory#initializeTestLoggerAdapter(org.slf4j.Logger, org.safs.tools.drivers.AbstractDriver)
 */
public class TestLoggerAdapter extends MarkerIgnoringBase {

	static STAFHelper staf = SingletonSTAFHelper.getHelper();	
	static LogUtilities log = new LogUtilities(staf);
	static String facname;
	
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
		log.logMessage(getFacName(), msg, AbstractLogFacility.DEBUG_MESSAGE);		
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
		log.logMessage(getFacName(), msg, AbstractLogFacility.FAILED_MESSAGE);		
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
		log.logMessage(getFacName(), msg, AbstractLogFacility.GENERIC_MESSAGE);		
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
	public void trace(String msg) {
		debug(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		debug(format, arg);		
	}

	@Override
	public void trace(String format, Object... arg1) {
		debug(format, arg1);		
	}

	@Override
	public void trace(String msg, Throwable arg1) {
		debug(msg,arg1);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		debug(format, arg1, arg2);
	}

	@Override
	public void warn(String msg) {
		log.logMessage(getFacName(), msg, AbstractLogFacility.WARNING_MESSAGE);
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
