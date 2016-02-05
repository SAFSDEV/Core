/** 
 * Some original concepts provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 * FEB 05, 2016    (Lei Wang) Add methods to kill processes by Windows command "wmic".
 * 
 */
package org.safs.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.net.NetUtilities;
import org.safs.tools.consoles.GenericProcessCapture;

/**
 * Various utilities for monitoring or otherwise interrogating native system processes.
 * This "Generic" version does all logging to System.out
 * <p>
 * Subclasses should override the debug(String) method to log to alternative mechanisms 
 * and should override the 
 * <p>
 * This class contains no extended SAFS dependencies and can be readily packaged and distributed 
 * for non-SAFS installations.
 * @author Carl Nagle 2012.03.27 Original Release 
 */
public class GenericProcessMonitor {

	static final String winproclist = "tasklist.exe /v";
	static final String winprockill = "taskkill.exe /f ";
	static final String winpidoption = "/pid ";
	static final String winimgoption = "/im ";
	static final String winwmic = "wmic ";
	static final String winwmic_nodeoption = "/node:";
	static final String winwmic_cmdprocess = "process";
	
	static final String unxproclist = "ps -f";
	static final String unxprockillpid = "kill -9 ";
	static final String unxprockillimg = "killall ";
	
	/** The value of System property "os.name"*/
	public static final String OS_NAME 	= System.getProperty("os.name").toLowerCase(Locale.US);
	/** The value of System property "path.separator"*/
	public static final String PATH_SEP = System.getProperty("path.separator");
	/** The value of System property "line.separator"*/
	public static final String EOL		= System.getProperty("line.separator");
	/** The value of System property "file.separator"*/	
	public static final String FILE_SEP = System.getProperty("file.separator");
	
	/** The operating system family name, deduced from os name {@link #OS_NAME}*/
	public static String osFamilyName = null;
	static final String OS_FAMILY_SYS_PROP = "os-family";
	static final String OS_FAMILY_WINDOWS = "windows";
	static final String OS_FAMILY_UNIX    = "unix";
	static final String OS_FAMILY_MAC     = "mac";
	
	static{
		osFamilyName = System.getProperty(OS_FAMILY_SYS_PROP);
		try{ if (osFamilyName == null) osFamilyName = deduceOSFamily(); }
		catch(IllegalStateException e){ System.err.println(e.getClass().getSimpleName()+":"+e.getMessage());}
	}
	
	/**
	 * Deduce the 'OS family name' from the 'OS name'.<br>
	 * 'windows' for all versions of Windows System.<br>
	 * 'unix' for all versions of Unix, Linux System.<br>
	 * 'mac' for all versions of Mac System.<br>
	 * @return "windows", "unix" or "mac"
	 * @throws IllegalStateException if neither can be deduced.
	 */
	protected static String deduceOSFamily(){
		if (OS_NAME.indexOf(OS_FAMILY_WINDOWS) > -1) {
			return OS_FAMILY_WINDOWS;
			
		} else if (PATH_SEP.equals(":") && 
				   OS_NAME.indexOf("openvms") == -1 &&
				   (OS_NAME.indexOf(OS_FAMILY_MAC) == -1 || OS_NAME.endsWith("x"))) {
			return OS_FAMILY_UNIX;
			
		} else if ((OS_NAME.indexOf(OS_FAMILY_MAC) > -1)) {
			return OS_FAMILY_MAC;
			
		}else {
			throw new IllegalStateException(
					"Can't infer your OS family.  Please set the " + OS_FAMILY_SYS_PROP
							+ " system property to one of 'windows', 'unix'.");
		}
	}
	
	/**
	 * Writes to System.out .
	 * Subclasses should override to log to alternate sinks.
	 * @param message
	 */
	protected static void debug(String message){
		System.out.println(message);
	}
	
	/**
	 * Subclasses may wish to override to return a different subclass of GenericProcessCapture.
	 * @param aproc
	 * @return 
	 */
	protected static GenericProcessCapture getProcessCapture(Process aproc){
		return new GenericProcessCapture(aproc);
	}
	
	/**
	 * Return true or false that a given process is running on the system. 
	 * On Windows we are using the {@value GenericProcessMonitor.wincmd} output for comparison.
	 * On Unix we are using {@value GenericProcessMonitor.unxcmd} output for comparison.
	 * @param procid CMD name, or IMAGE, or PID to seek.  
	 * @return true if the procid is running/listed.
	 * @throws IOException if any error occurs in getting the processes for evaluation.
	 */
	public static boolean isProcessRunning(String procid)throws IOException{

		GenericProcessCapture console = null;
		boolean success = false;
		String cmd = null;
		try{
			cmd = isWindowsOS() ? winproclist : unxproclist;
			Process proc = Runtime.getRuntime().exec(cmd);
			console = getProcessCapture(proc);
			Thread athread = new Thread(console);
			athread.start();
			proc.waitFor();
			success = (proc.exitValue()==0);
		}catch(Exception x){
			// something else was wrong with the underlying process
			debug(cmd +", "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		}
		if(success){
			success = false;
			boolean isNumeric = false;
			int pid = 0;
			int pidindex = 0;
			String pidtest = null;
			try{ 
				pid = Integer.parseInt(procid);
				isNumeric = true;
			}catch(NumberFormatException n){ }
			String line = null;
			Enumeration reader = console.getData().elements();
			while((!success) && (reader.hasMoreElements())){
				line = (String)reader.nextElement();
				if(isNumeric){
					pidindex = line.indexOf(procid);
					if(pidindex > 0){
						try{
							// grab a character before and after and make sure it is not alpha
							pidtest = line.substring(pidindex -1, procid.length()+pidindex+1).trim();
							success = ( pid == Integer.parseInt(pidtest));
						}catch(Exception x){/*ignore*/}
					}
				}else{
					success = line.contains(procid);
				}
			}			
		}
		return success;
	}

	/**
	 * To generate <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa394054(v=vs.85).aspx">WQL</a> search condition for "wmic".<br>
	 * The condition is to search according a key's string value, we use "=" or "like" to compare.<br>
	 * <b>Note:</b><br>
	 * 1. It could be concatenated by operator "and", "or", "not" etc. refer to <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa394606(v=vs.85).aspx">WQL Keywords</a><br>
	 * 2. A group of conditions should be wrapped by parenthesis, such as ((condition1 or condition2) and condition3).<br>
	 * 3. Finally the group of conditions should be wrapped by double-quote, such as "((condition1 or condition2) and condition3)", before feed the where clause.<br>
	 * 
	 * @param key			String, the key to match, for example, for command 'process', the key could be CommandLine, Name, ProcessId etc.
	 * @param value			String, the value to match
	 * @param partialMatch	boolean, if the match is partial.
	 * @param caseSensitive	boolean, if the match is case sensitive.<br>
	 *                               NOT USED YET. It seems that command 'wmic' handles condition case-insensitively.<br>
	 * @return String, the search condition.
	 */
	public static String wqlCondition(String key, String value, boolean partialMatch, boolean caseSensitive/* not used, all case-insensitive*/){
		String debugmsg = StringUtils.debugmsg(false);
		String condition = "";
		
		if(!StringUtils.isValid(key)){
			IndependantLog.error(debugmsg+" the key is not valid.");
			return condition;
		}
		if(!StringUtils.isValid(value)){
			IndependantLog.error(debugmsg+" the value is not valid.");
			return condition;
		}
		
		value = wqlNormalizeValue(value);
		
		if(partialMatch){
			condition = key+" like '%"+value+"%' ";
		}else{
			condition = key+" = '"+value+"' ";
		}
		
		IndependantLog.debug(debugmsg+" got condition: "+condition);
		
		return condition.toString();
	}
	
	/**
	 * Escape character back-slash(\), double-quote(") and single-quote(').<br>
	 * @param value	String, the value to match for a certain key.
	 * @return String the normalized value.
	 */
	private static String wqlNormalizeValue(String value){
		if(!StringUtils.isValid(value)){
			IndependantLog.error(StringUtils.debugmsg(false)+" the value "+value+" is not valid.");
			return value;
		}
		//escape character back-slash(\), double-quote(") and single-quote('). 
		if(value.contains(StringUtils.BACK_SLASH)) value = value.replaceAll("\\\\", "\\\\\\\\");// replace \ by \\
		if(value.contains(StringUtils.QUOTE)) value = value.replaceAll(StringUtils.QUOTE, "\\'");// replace ' by \'
		if(value.contains(StringUtils.DOUBLE_QUOTE)) value = value.replaceAll(StringUtils.DOUBLE_QUOTE, "\\\"");// replace " by \"

		return value;
	}
	
	public static class WQLSearchCondition{
		//The condition used in command wmic's where clause, such as
		//commandline like '%d:\\seleniumplus\\extra\\chromedriver.exe%' and name = 'chromedriver.exe' 
		private String condition = null;
		
		public WQLSearchCondition(String condition){
			this.condition = condition;
		}
		
		public String toString(){			
			if(!StringUtils.isValid(condition)){
				IndependantLog.error(StringUtils.debugmsg(false)+" the value "+condition+" is not valid.");
				return condition;
			}
			
			//Wrap the WQL condition with double-quote
			String normalizedCondition = condition.trim();

			if(!(normalizedCondition.startsWith(StringUtils.DOUBLE_QUOTE) && normalizedCondition.endsWith(StringUtils.DOUBLE_QUOTE))){
				normalizedCondition = StringUtils.quote(normalizedCondition);
			}
			
			return normalizedCondition;
		}
	}
	
	public static class ProcessInfo{
		String id = null;
		int wmiTerminateRC = Integer.MIN_VALUE;
		
		public String getId() {
			return id;
		}
		public int getWmiTerminateRC() {
			return wmiTerminateRC;
		}

	}
	
	/**
	 * Attempt to forcefully kill processes on a certain host according to a WQL search condition.<br>
	 * On Windows we are using wmic.exe. It is only supported on Windows.<br>
	 * 
	 * @param host	String, the host name where to kill processes
	 * @param condition	WQLSearchCondition, the WQL search condition to find processes
	 * @return	List<ProcessInfo>, a list of killed processes
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> shutdownProcess(String host, WQLSearchCondition condition) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		GenericProcessCapture console = null;
		boolean success = false;
		String cmd = null;
		
		if(!isWindowsOS()) throw new SAFSException(osFamilyName+" has NOT been supported yet.");
		
		try{
			cmd = winwmic;
			//If this host is a remote machine, then add the "/node" option
			if(!(host==null || host.trim().isEmpty() || NetUtilities.isLocalHost(host))){
				cmd += " "+winwmic_nodeoption+host+" ";
			}
			cmd += " "+winwmic_cmdprocess+" ";
			cmd += " where "+condition+" call terminate";

			IndependantLog.debug(debugmsg+" executing command: "+cmd);
			
			Process proc = Runtime.getRuntime().exec(cmd);
			console = getProcessCapture(proc);
			Thread athread = new Thread(console);
			athread.start();
			athread.join();
			success = (console.getExitValue()==0);
		}catch(Exception x){
			// something else was wrong with the underlying process
			IndependantLog.error(debugmsg+" executing "+cmd +", met "+ StringUtils.debugmsg(x));
		}
		
		List<ProcessInfo> processList = new ArrayList<ProcessInfo>();
		
		//Analyze the output to store the killed processes information into a list
		if(success){
			int pidindex = 0;

			String line = null;
			Enumeration<String> reader = console.getData().elements();
			ProcessInfo processInfo = new ProcessInfo();
			boolean found = false;
			while((reader.hasMoreElements())){
				line = reader.nextElement();
				pidindex = line.indexOf("Win32_Process.Handle=");
				if(pidindex > 0){
					found = true;
					try{
						processInfo.id = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")).trim();
						processList.add(processInfo);
					}catch(Exception x){/*ignore*/}
				}
				if(found){
					if(line.indexOf("ReturnValue")>-1){
						try{
							String rc = line.substring(line.indexOf("=")+1, line.indexOf(";")).trim();
							processInfo.wmiTerminateRC = Integer.parseInt(rc);
						}catch(Exception e){}
						//reinitialized for next process
						found = false;
						processInfo = new ProcessInfo();
					}
				}
			}
		}
		
		return processList;
	}
	
	/** 
	 * Attempt to forcefully kill a given process by name or PID. 
	 * On Windows we are using taskkill.exe.  
	 * On Unix we are using kill -9 for PID, or killall for process names.
	 * Of course, this must be used with care!
	 * @param procid CMD name(nix ps-f) or IMAGE(win qprocess.exe) or PID to kill.  
	 * @return true if the shutdown attempt returned with success, false otherwise.
	 * @throws IOException if no shutdown attempt was ever able to execute.
	 */
	public static boolean shutdownProcess(String procid)throws IOException{

		boolean isNumeric = false;
		try{ isNumeric = Integer.parseInt(procid) > 0;}
		catch(NumberFormatException n){ }
		
		String wincmd = winprockill;
		wincmd += isNumeric ? winpidoption : winimgoption;
		wincmd += procid;		
		String unxcmd = isNumeric ? unxprockillpid + procid:unxprockillimg + procid;

		GenericProcessCapture console = null;
		String cmd = null;
		boolean success = false;
		boolean run = false;
		try{
			cmd = isWindowsOS() ? wincmd : unxcmd;
			Process proc = Runtime.getRuntime().exec(cmd);
			console = getProcessCapture(proc);
			Thread athread = new Thread(console);
			athread.start();
			proc.waitFor();
			success = (proc.exitValue()==0);
			run = true;
		}catch(Exception x){
			// something else was wrong with the underlying process
			IndependantLog.error(cmd +", "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		}
		if (!run) throw new IOException("ShutdownProcess command did not execute properly using : "+ cmd);
		return success;
	 }
	
	public static boolean isWindowsOS(){
		return osFamilyName.equals(OS_FAMILY_WINDOWS);
	}
}
