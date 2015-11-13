/** 
 * Some original concepts provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

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
 * @author canagl 2012.03.27 Original Release 
 */
public class GenericProcessMonitor {

	static final String winproclist = "tasklist.exe /v";
	static final String winprockill = "taskkill.exe /f ";
	static final String winpidoption = "/pid ";
	static final String winimgoption = "/im ";

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
			cmd = osFamilyName.equals(OS_FAMILY_WINDOWS) ? winproclist : unxproclist;
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
			cmd = osFamilyName.equals(OS_FAMILY_WINDOWS) ? wincmd : unxcmd;
			Process proc = Runtime.getRuntime().exec(cmd);
			console = getProcessCapture(proc);
			Thread athread = new Thread(console);
			athread.start();
			proc.waitFor();
			success = (proc.exitValue()==0);
			run = true;
		}catch(Exception x){
			// something else was wrong with the underlying process
			debug(cmd +", "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		}
		if (!run) throw new IOException("ShutdownProcess command did not execute properly using : "+ cmd);
		return success;
	 }
}
