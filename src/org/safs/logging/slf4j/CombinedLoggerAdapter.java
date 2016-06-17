/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.logging.slf4j;

import org.safs.IndependantLog;
import org.safs.logging.AbstractLogFacility;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Carl Nagle
 */
public class CombinedLoggerAdapter extends TestLoggerAdapter {

	static final IndependantLog dlog = new IndependantLog();
	
	/**
	 * 
	 */
	public CombinedLoggerAdapter() {
		super();
	}

	@Override
	public void debug(String msg) {
		dlog.debug(msg);		
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
}
