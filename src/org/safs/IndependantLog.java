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
 * JUL 05, 2018 (Lei Wang) Use Map to manage the Methods got from class org.safs.Log
 * JUL 27, 2018 (Lei Wang) Modified method log(): invoke method with correct arguments.
 */
package org.safs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.safs.Constants.LogConstants;
import org.safs.sockets.DebugListener;

/**
 * <p>
 * Use reflection for org.safs.Log to do the Log work.
 * By this way, we don't need to import org.safs.Log which will depend on
 * STAF. In some environments, there is no STAF Library. If we import
 * org.safs.Log, there is a compilation error or ClassNotFoundException.
 * <p>
 * For example, on Android device, we don't have STAF library.
 * But our Droid-Engine needs some classes extending the
 * existing SAFS class who uses org.safs.Log
 * We can replace Log.XXX() by IndependantLog.XXX() in the SAFS class.
 *
 */
public class IndependantLog {
	private static final String SAFS_LOG_CLASS_NAME = "org.safs.Log";
	private static Class<?> logClass = null;
	private static boolean suspended = false;

	/**
	 * If you set this listener, org.safs.Log will not try to log message.
	 */
	private static DebugListener debugListener = null;

	static{
		getLogClass();
	}

	public static void suspendLogging(){
		suspended = true;
	}
	public static void resumeLogging(){
		suspended = false;
	}
	public static DebugListener getDebugListener() {
		return debugListener;
	}

	public static void setDebugListener(DebugListener debugListener) {
		IndependantLog.debugListener = debugListener;
	}

	public static void debug(String message){ log(message, LogConstants.DEBUG); }
	public static void info(String message){ log(message, LogConstants.INFO); }
	public static void index(String message){ log(message, LogConstants.INDEX); }
	public static void generic(String message){	log(message, LogConstants.GENERIC);}
	public static void pass(String message){ log(message, LogConstants.PASS);}
	public static void warn(String message){ log(message, LogConstants.WARN);}
	public static void error(String message){ log(message, LogConstants.ERROR); }
	public static void error(String message, Throwable th){	log(message, LogConstants.ERROR, th); }
	public static void info(String message, Throwable th){	log(message, LogConstants.INFO, th); }
	public static void debug(String message, Throwable th){	log(message, LogConstants.DEBUG, th); }

	private static Class<?> getLogClass(){
		try {
			if(logClass!=null) return logClass;
			logClass = Class.forName(SAFS_LOG_CLASS_NAME);
		} catch (Throwable ignore) {}
		return logClass;
	}

	/**
	 * The cache to store Method of Log class.<br>
	 * The map's key is the Method name, such as "debug", "warn", "debugThrowable", "warnThrowable" etc.<br>
	 * The map's value is the Method.<br>
	 */
	private static Map<String, Method> loggers = new HashMap<String, Method>();

	/**
	 * @param message String, the message to write into debug log.
	 * @param level int, the log level, it can be one of
	 * <ul>
	 * <li>{@link LogConstants#DEBUG}
	 * <li>{@link LogConstants#INFO}
	 * <li>{@link LogConstants#INDEX}
	 * <li>{@link LogConstants#GENERIC}
	 * <li>{@link LogConstants#PASS}
	 * <li>{@link LogConstants#WARN}
	 * <li>{@link LogConstants#ERROR}
	 * </ul>
	 */
	private static void log(String message, int level){
		log(message, level, null);
	}

	/**
	 * @param message String, the message to write into debug log.
	 * @param level int, the log level, it can be one of
	 * <ul>
	 * <li>{@link LogConstants#DEBUG}
	 * <li>{@link LogConstants#INFO}
	 * <li>{@link LogConstants#INDEX}
	 * <li>{@link LogConstants#GENERIC}
	 * <li>{@link LogConstants#PASS}
	 * <li>{@link LogConstants#WARN}
	 * <li>{@link LogConstants#ERROR}
	 * </ul>
	 * @param th Throwable, to write into log
	 */
	private static void log(String message, int level, Throwable th){
		if(!suspended){
			//level is also the method name of Log class
			String method = LogConstants.getLogLevelName(level).toLowerCase();
			String key = method;
			if(th!=null){
				key += "Throwable";
			}

			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message + getStackTrace(th));
					return;
				}

				Method logger = loggers.get(key);
				if(logClass!=null && logger==null){
					if(th==null){
						logger = logClass.getMethod(method, Object.class);
					}else{
						logger = logClass.getMethod(method, Object.class, Throwable.class);
					}
					loggers.put(key, logger);
				}

				if(logger!=null){
					if(th==null){
						logger.invoke(null, message);
					}else{
						logger.invoke(null, message, th);
					}
					return;
				}else{
					//Comment this out, it is annoying if we don't have the org.safs.Log on the classpath,
					//for example safsinstall.jar doesn't include org.safs.Log
					//System.err.println("IndependantLog Error: could not get logger method for '"+key+"'.");
				}
			} catch (Throwable e) {
				System.err.println("IndependantLog Error:");
				e.printStackTrace();
			}

			if(LogConstants.ERROR == level){
				System.err.println(message + getStackTrace(th));
			}else{
				System.out.println(message + getStackTrace(th));
			}
		}
	}

	/**
	 * Create a String from a Throwable suitable for debug output that provides
	 * comparable information to x.printStackTrace();
	 * @param x
	 * @return String ready for output to debug(String) or other sink.
	 */
	public static String getStackTrace(Throwable x){
		String rc = "";
		try{
			if(x!=null){
				rc = "\n"+x.getClass().getName()+", "+ x.getMessage()+"\n";
				StackTraceElement[] se = x.getStackTrace();
				for(StackTraceElement s:se){ rc += s.toString()+"\n"; }
			}
		}catch(Throwable ignore){}
		return rc;
	}

	/**"-debug", if it is present, then show debug message on console */
	public static final String ARG_DEBUG = "-debug";
	/**
	 * Parse an array of arguments, if {@link #ARG_DEBUG} is present, then call {@link #toConsole()} so that the debug message will be output to console.
	 * @param args String[], an array of argument to parse
	 */
	public static void parseArguments(String[] args){
		if(args!=null){
			for(String arg:args){
				if(arg.trim().equalsIgnoreCase(ARG_DEBUG)){
					toConsole();
					break;
				}
			}
		}
	}

	/** Set {@link ConsoleDebugListener} to {@link IndependantLog}, so that IndependantLog will write debug, info, warn etc. message to console.*/
	public static void toConsole(){
		if(IndependantLog.getDebugListener() instanceof ConsoleDebugListener) return;
		IndependantLog.setDebugListener(generateConsoleDebugListener());
	}
	/** an implementation of interface {@link DebugListener}, output debug message to console. */
	public static class ConsoleDebugListener implements DebugListener{
		@Override
		public String getListenerName() {
			return StringUtils.getClassName(1, true);
		}
		@Override
		public void onReceiveDebug(String message) {
			System.out.println("DEBUG--> "+message);
		}
	}
	/** create a new instance of class {@link ConsoleDebugListener} */
	public static DebugListener generateConsoleDebugListener(){
		return new ConsoleDebugListener();
	}

	/**
	 * Used to test methods in this class.
	 * @param args
	 */
	public static void main(String[] args){
		try {
			//Enable org.safs.Log
			logClass.getField("ENABLED").setBoolean(null, true);
			//Set the log level to Log.DEBUG
			int logLevelDebug = logClass.getField("DEBUG").getInt(null);
			Field levelField = logClass.getDeclaredField("level");
			levelField.setAccessible(true);
			levelField.setInt(null, logLevelDebug);

			debug("Test invoke of relfect Log.");
			info("Test invoke of relfect Log.");
			error("Test invoke of relfect Log.");
			generic("Test invoke of relfect Log.");
			warn("Test invoke of relfect Log.");
			index("Test invoke of relfect Log.");
			pass("Test invoke of relfect Log.");
			debug("Test invoke of relfect Log.", new Exception("My exception"));
			error("Test invoke of relfect Log.", new Exception("My exception"));
			info("Test invoke of relfect Log.", new Exception("My exception"));

			//Following code is used to give the original result by org.safs.Log
			//we can use that result to verify 'reflection Log result'
//			Log.ENABLED = true;
//			Log.level = Log.DEBUG;
//
//			Log.debug("Test invoke of relfect Log.");
//			Log.info("Test invoke of relfect Log.");
//			Log.error("Test invoke of relfect Log.");
//			Log.generic("Test invoke of relfect Log.");
//			Log.warn("Test invoke of relfect Log.");
//			Log.index("Test invoke of relfect Log.");
//			Log.pass("Test invoke of relfect Log.");
//			Log.debug("Test invoke of relfect Log.", new Exception("My exception"));
//			Log.error("Test invoke of relfect Log.", new Exception("My exception"));
//			Log.info("Test invoke of relfect Log.", new Exception("My exception"));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
