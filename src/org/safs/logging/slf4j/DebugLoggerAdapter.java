package org.safs.logging.slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.safs.IndependantLog;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class DebugLoggerAdapter extends MarkerIgnoringBase {

//	static final STAFHelper staf = SingletonSTAFHelper.getHelper();
//	static final LogUtilities log = new LogUtilities(staf);
	static final IndependantLog log = new IndependantLog();
	
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
	public void debug(String msg) {
		log.debug(msg);		
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
		log.debug(msg, arg1);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		debug(format, new Object[]{arg1, arg2});		
	}

	@Override
	public void error(String msg) {
		log.error(msg);		
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
		log.error(msg, arg1);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		error(format, new Object[]{arg1, arg2});		
	}

	@Override
	public void info(String msg) {
		log.info(msg);		
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
		log.info(msg, arg1);
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
		log.warn(msg);
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
		log.warn(msg +"\n"+ getThrownString(arg1));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		warn(format, new Object[]{arg1,arg2});
	}
}
