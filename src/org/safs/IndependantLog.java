package org.safs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
	private static Class logClass = null;
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

	private static Class getLogClass(){
		try {
			if(logClass!=null) return logClass;
			logClass = Class.forName(SAFS_LOG_CLASS_NAME);
		} catch (Throwable ignore) {}
		return logClass;
	}
	
	private static Method debug = null;
	public static void debug(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(debug==null) debug = logClass.getMethod("debug", Object.class);
					debug.invoke(null, message);
				}
			} catch (Throwable e) {
				System.out.println(message);
			}
		}
	}
	private static Method info = null;
	public static void info(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(info==null) info = logClass.getMethod("info", Object.class);
					info.invoke(null, message);
				}
			} catch (Throwable e) {
				System.out.println(message);
			}
		}
	}
	private static Method index = null;
	public static void index(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(index==null) index = logClass.getMethod("index", Object.class);
					index.invoke(null, message);
				}
			} catch (Throwable e) {
				System.out.println(message);
			}
		}
	}
	private static Method generic = null;
	public static void generic(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(generic==null) generic = logClass.getMethod("generic", Object.class);
					generic.invoke(null, message);
				}
			} catch (Throwable e) {
				System.out.println(message);
			}
		}
	}
	private static Method pass = null;
	public static void pass(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(pass==null) pass = logClass.getMethod("pass", Object.class);
					pass.invoke(null, message);
				}
			} catch (Throwable e) {
				System.out.println(message);
			}
		}
	}
	private static Method warn = null;
	public static void warn(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(warn==null) warn = logClass.getMethod("warn", Object.class);
					warn.invoke(null, message);
				}
			} catch (Throwable e) {
				System.err.println(message);
			}
		}
	}
	private static Method error = null;
	public static void error(String message){
		if(!suspended){
			try {
				if(debugListener!=null){
					debugListener.onReceiveDebug(message);
				}else{
					if(error==null) error = logClass.getMethod("error", Object.class);
					error.invoke(null, message);
				}
			} catch (Throwable e) {
				System.err.println(message);
			}
		}
	}
	
	private static Method error2 = null;
	public static void error(String message, Throwable th){
		if(!suspended){
			try {
				if(debugListener!=null){
					String logMessage = message + "\n"+ getStackTrace(th);
					debugListener.onReceiveDebug(logMessage);
				}else{
					if(error2==null) error2 = logClass.getMethod("error", Object.class, Throwable.class);
					error2.invoke(null, message, th);
				}
			} catch (Throwable e) {
				String logMessage = message + "\n"+ getStackTrace(th);
				System.err.println(logMessage);
			}
		}
	}
	private static Method info2 = null;
	public static void info(String message, Throwable th){
		if(!suspended){
			try {
				if(debugListener!=null){
					String logMessage = message + "\n"+ getStackTrace(th);
					debugListener.onReceiveDebug(logMessage);
				}else{
					if(info2==null) info2 = logClass.getMethod("info", Object.class, Throwable.class);
					info2.invoke(null, message, th);
				}
			} catch (Throwable e) {
				String logMessage = message + "\n"+ getStackTrace(th);
				System.out.println(logMessage);
			}
		}
	}
	private static Method debug2 = null;
	public static void debug(String message, Throwable th){
		if(!suspended){
			try {
				if(debugListener!=null){
					String logMessage = message + "\n"+ getStackTrace(th);
					debugListener.onReceiveDebug(logMessage);
				}else{
					if(debug2==null) debug2 = logClass.getMethod("debug", Object.class, Throwable.class);
					debug2.invoke(null, message, th);
				}
			} catch (Throwable e) {
				String logMessage = message + "\n"+ getStackTrace(th);
				System.out.println(logMessage);
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
			rc = x.getClass().getName()+", "+ x.getMessage()+"\n";
			StackTraceElement[] se = x.getStackTrace();
			for(StackTraceElement s:se){ rc += s.toString()+"\n"; }
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
		public String getListenerName() {
			return StringUtils.getClassName(1, true);
		}
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
