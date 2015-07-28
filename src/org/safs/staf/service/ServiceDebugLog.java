package org.safs.staf.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;

import com.ibm.staf.STAFResult;

public class ServiceDebugLog {
	// for debugging
	public static final String DEBUG_LOG_LOGGING 			= "LoggingService";
	public static final String DEBUG_LOG_VARIABLE 			= "VariableService";
	public static final String DEBUG_LOG_MAPS				= "MapsService";
	public static final String DEBUG_LOG_INPUT	 			= "InputService";
	public static final String DEBUG_LOG_CUSTOM_LOGGING 	= "CustomLoggingService";
	
	private boolean DEBUG = true;
	private String debugFileName = "";
	private String serviceName = "";
	private PrintWriter debugLog;
	
	public ServiceDebugLog(){
		//log file shall be put in the root of SAFS; it can work on both Unix and Windows
		debugFileName = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR)+ File.separator + "_SAFSService.debug.txt";
	}

	public ServiceDebugLog(String serviceName,boolean debug){
		this.serviceName = serviceName;
		//log file shall be put in the root of SAFS; it can work on both Unix and Windows
		this.debugFileName = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR)+ File.separator +"_SAFS"+serviceName+".debug.txt";
		this.DEBUG = debug;
	}
	
	/**
	 * Initializes debug log.
	 * Do not forget to call debugTerm().
	 */
	public  void debugInit()
	{
		if(!DEBUG) return;
		try {
			debugLog = new PrintWriter(new FileWriter(new CaseInsensitiveFile(debugFileName).toFile(), false));
			debugPrintln("DEBUG STARTING: " + (new Date()));
		} catch (IOException e) {
			e.printStackTrace();
			debugLog = null;
			DEBUG = false;
		}
	}

	/**
	 * Prints a line to the debug log if debugging is enabled.
	 * <p>
	 * @param msg the message to print.
	 */
	public  void debugPrintln(String msg)
	{
		if(DEBUG && debugLog!=null){
			debugLog.println(addServiceNameToMessage(msg));
			debugLog.flush();
		}
	}

	/**
	 * Prints a message and STAFResult to the debug log if debugging is enabled.
	 * <p>
	 * @param msg the message to print.
	 * @param t   the STAFResult to print.
	 */
	public  void debugPrintln(String msg, STAFResult r)
	{
		if(DEBUG && debugLog!=null){
			debugLog.println(addServiceNameToMessage(msg) + "[RC " + r.rc + "] " + r.result);
			debugLog.flush();
		}
	}

	/**
	 * Prints a message and Exception to the debug log if debugging is enabled.
	 * <p>
	 * @param msg the message to print.
	 * @param e   the Exception to print.
	 */
	public  void debugPrintln(String msg, Exception e)
	{
		if(DEBUG && debugLog!=null){
			debugLog.println(addServiceNameToMessage(msg));
			e.printStackTrace(debugLog);
			debugLog.flush();
		}
	}

	public String addServiceNameToMessage(String message){
		return this.serviceName+": "+message;
	}
	
	/**
	 * Closing debug log. 
	 * This should always be called finally.
	 */
	public  void debugTerm()
	{
		if(DEBUG && debugLog!=null){
			debugPrintln("DEBUG ENDING: " + (new Date()));
			debugLog.close();
		}
	}
}
