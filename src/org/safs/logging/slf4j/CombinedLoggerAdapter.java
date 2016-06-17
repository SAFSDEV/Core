/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
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
 * @author canagl
 * @see SAFSLoggerFactory#getLogger(String)
 * @see SAFSLoggerFactory#initializeTestLoggerAdapter(org.slf4j.Logger, org.safs.tools.drivers.AbstractDriver)
 */
public class CombinedLoggerAdapter extends TestLoggerAdapter {

	static final IndependantLog dlog = new IndependantLog();
	
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
