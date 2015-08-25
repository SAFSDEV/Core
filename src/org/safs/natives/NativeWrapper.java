/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.natives;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.android.auto.lib.Process2;
import org.safs.natives.win32.Kernel32;
import org.safs.natives.win32.Kernel32.FileTime;
import org.safs.natives.win32.Psapi;
import org.safs.natives.win32.Shell32;
import org.safs.natives.win32.User32;
import org.safs.natives.win32.User32.WNDENUMPROC;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.stringutils.StringUtilities;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 * This class is used to encapsulate platform independent calls to native operating systems through JNA,
 * or other native platform technologies.
 * <p>
 * JNA is the Java Native Access library supplied via <a href="http://jna.dev.java.net" target="_blank">JNA Home</a> 
 * <p>
 * JNA provides Java programs easy access to native shared libraries on multiple operating systems without 
 * writing anything but Java code JNI or native code is required. This functionality is comparable to 
 * Windows' Platform/Invoke and Python's ctypes. Access is dynamic at runtime without code generation.
 * <p>
 * SAFS is now delivered with the core JNA.ZIP(JAR).  Other JNA support libraries may be added as needed. 
 * @author Carl Nagle
 * @author Carl Nagle  Jun 03, 2009  Added GetProcessUIResourceCount<br>
 *         Carl Nagle  Aug 07, 2009  Added GetProcessFileName <br>
 *         Carl Nagle  SEP 14, 2009  Refactored with WIN classes. <br>
 *         Carl Nagle  DEC 15, 2009  Added GetRegistryKeyValue and  DoesRegistryKeyExists routines.<br> 
 *         JunwuMa JUL 23, 2010  Updated to get GetRegistryKeyValue support Win7.<br>
 *         (Lei Wang)NOV 18, 2011  Add method getFileTime() and convertFileTimeToJavaTime()<br>
 *         Carl Nagle  OCT 29, 2013  Added SetRegistryKeyValue and SetSystemEnvironmentVariable routines.<br>
 *         DHARMESH4  FEB 19, 2014  Added runAsyncExec call.<br>  
 *         DHARMESH4  AUG 17, 2015  Added setForgroundWindow call<br>  
 * @since 2009.02.03
 */
public class NativeWrapper {
	
	/** -99 */
	public static final int NO_RESULT = -99;
	/** "Vector" */
	public static final String VECTOR_KEY = "Vector";
	/** "Result" */
	public static final String RESULT_KEY = "Result";
	
	public static final String REG_SZ        = "REG_SZ";
	public static final String REG_EXPAND_SZ = "REG_EXPAND_SZ";
	public static final String REG_MULTI_SZ  = "REG_MULTI_SZ";
	public static final String REG_DWORD     = "REG_DWORD";
	public static final String REG_QWORD     = "REG_QWORD";
	public static final String REG_BINARY    = "REG_BINARY";
	public static final String REG_NONE      = "REG_NONE";

	
	/**
	 * True if the requested "registry key" already exists, false otherwise.<br>
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will return false.
	 * <p>
	 * @param key For Windows this is a String. Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param valuename For Windows this is a String. Ex:"SpyHeapSize"
	 * @return true if the key and value exists, false if it does not.
	 */
	public static boolean DoesRegistryKeyExist(Object key, Object valuename){
		
		if(Platform.isWindows()){
			String rawkey = (String) key;
			String strkey = new String(rawkey);
			String strval = null;
			if (valuename != null) strval = (String) valuename;
			boolean tresult = false;
			try{ tresult = _winKeyExistsWRegEXE(strkey, strval);}
			catch(IOException x){
				IndependantLog.info("NativeWrapper.DoesRegistryKeyExist trying alternatives...");
			}
			if (tresult) return true;
			//alternatives go here
			return false;
		}
		IndependantLog.info("NativerWrapper.DoesRegistryKeyExist is not supported on this platform.");
		return false;
	}

	/**
	 * Runs a short process and waits for it to complete.
	 * Provides a Hashtable that will contain the exit code and system out and err data:
	 * <p>
	 * <ul>
	 *     "Result" key returns and Integer Object containing the value of the exit code.<br>
	 *     "Vector" key contains a jav.util.Vector object of the console output Strings from the process.<br>
	 *     <ul>
	 *         System.out console output lines are prefixed with "OUT: "<br>
	 *         System.err console output lines are prefixed with "ERR: "<br>
	 *     </ul>
	 * </ul>
	 * <p>
	 * @param proc Ex: "myProg" -- the caller must do any necessary quoting if the proc path contains spaces that 
	 * would interfere with finding and executing the process.
	 * <p>
	 * @param args Ex: "-u"  or String[]{"-u", "arg2", "arg3"}.  The caller must do any necessary quoting of individual 
	 * arguments if spaces in the arguments will interfere with proper interpretation of the arguments by the process.
	 * <p>
	 * @return Hashtable containing process output "Vector" as a Vector and "Result" as an Integer.
	 * The value of the Integer will be dependent on what the process returns, if anything. 
	 * The Integer will contain NO_RESULT (-99) if the program was not successfully processed. 
	 * The Vector may be empty if no data was received from the process.
	 * @throws IOException if the process was not executed on the system.
	 */
	public static Hashtable runShortProcessAndWait(String proc, String... args) throws IOException{
		String procstr = proc;
		if(args != null && args.length > 0){
			for(String arg: args){
				if(arg == null || arg.length() == 0) continue;
				procstr += " "+ arg;
			}
		}
		int procresult = NO_RESULT;
		Hashtable result = new Hashtable();
		result.put(VECTOR_KEY, new Vector());
		result.put(RESULT_KEY, new Integer(NO_RESULT));
		try{
			IndependantLog.info("NativeWrapper attempting to execute: "+ procstr);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(procstr);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.start();
			try{ athread.join(10000);//should take at most a few seconds
			}catch(InterruptedException x){
				IndependantLog.info("NativeWrapper handling InterruptedException on "+ process);
			}
			console.shutdown();//just in case
			try{ 
				procresult = process.exitValue();
				result.put(RESULT_KEY, new Integer(procresult));
			}
			catch(IllegalThreadStateException x){;}
			result.put(VECTOR_KEY, console.getData());
			return result;
		}catch(IOException x){
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return result;		
	}
	
	/**
	 * Runs an asynch batch process and immediately returns.
	 * @param workdir The fullpath to a valid directory to be used as the batch working directory.<br>
	 * Ex: "C:\\STAF"
	 * <p>
	 * @param batchAndargs the program and arguments array.  The first item in the array is the batch program to execute. 
	 * <p>
	 * @return true if successfully launched
	 * @throws IOException if the process path was not valid or was not executed on the system.
	 * @see Console
	 */
	public static boolean runAsynchBatchProcess(String workdir, String... batchAndargs) throws IOException{
		File f = new File(workdir);
		if(!f.isDirectory())throw new IOException("runAsynchProcess workdir is not a valid directory.");
		Console c; 
		try{c = Console.get().getClass().newInstance();}
		catch(Exception x){
			IOException io = new IOException("runAsynchProcess cannot instantiate Console class to run asynchronous Process2.");
			io.initCause(x);
			throw io;
		}
		Process2 p = c.batch(f, batchAndargs);
		p.removeJVMShutdownReference();
		try{ Thread.sleep(500); }catch(Exception x){}
		return true;
	}
	
	/**
	 * Runs an asynch exec and immediately returns.
	 * @param Full path of the file.
	 * <p>
	 * @return true if successfully launched
	 * @throws IOException if the process path was not valid or was not executed on the system. 
	 * 
	 */
	public static boolean runAsynchExec(String fullpath) throws IOException{
		
		File afile = new File(fullpath);
		
		try {
			Desktop.getDesktop().open(afile);
		} catch (Exception e) {
			IOException io = new IOException("runAsynchExec process failed.");
			io.initCause(e);
			throw io;
		}
		
		return true;
	}
	/**
	 * Vector containing output if "registry key" exists, may be empty.<br>
	 * This Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strval Ex:"SpyHeapSize"
	 * @return Hashtable containing process output "Vector" as a Vector and "Result" as an Integer.
	 * The Integer will normally be 0 if all went fine, or 1 if a reg.exe error occurred--like the 
	 * key was not found or does not exist. The Integer will contain NO_RESULT if reg.exe was not 
	 * successfully processed. The Vector may be empty if no data was received from the process.
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static Hashtable _winRegExeQueryResults(String strkey, String strval) throws IOException{
		String procstr = "reg.exe query ";
		int procresult = NO_RESULT;
		Hashtable result = new Hashtable();
		result.put(VECTOR_KEY, new Vector());
		result.put(RESULT_KEY, new Integer(NO_RESULT));
		try{
			//quote a key containing spaces
			if(strkey.contains(" ")) strkey = "\""+ strkey +"\"";
			procstr += strkey;
			if (strval != null){
				if(strval.contains(" ")) strval = "\""+ strval +"\"";
				procstr +=" /v "+ strval;	
			}
			IndependantLog.info("NativeWrapper attempting to execute: "+ procstr);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(procstr);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.start();
			try{ athread.join(10000);//should take at most a few seconds
			}catch(InterruptedException x){
				IndependantLog.info("NativeWrapper handling InterruptedException on "+ process);
			}
			console.shutdown();//just in case
			try{ 
				procresult = process.exitValue();
				result.put(RESULT_KEY, new Integer(procresult));
			}
			catch(IllegalThreadStateException x){;}
			result.put(VECTOR_KEY, console.getData());
			return result;
		}catch(IOException x){
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return result;		
	}
	
	/**
	 * Vector containing output of the SETX operation, may be empty.<br>
	 * This Windows version uses SETX which is supplied with Vista.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * Note: the newly set Environment Variable value is NOT available to the currently 
	 * running JVM through System.getEnv().  This is because the JVM does not refresh its 
	 * Environment variable space after launch.  Use GetSystemEnvironmentVariable to get 
	 * the latest "refreshed" value of any System Environment Variable.
	 * <p>
	 * @param strkey Name of Environment Variable to Set
	 * @param strval Value to set. 
	 * @return Hashtable containing process output "Vector" as a Vector and "Result" as an Integer.
	 * The Integer will normally be 0 if all went fine, or 1 if a setx.exe error occurred. 
	 * The Integer will contain NO_RESULT if setx.exe was not successfully processed. 
	 * The Vector may be empty if no data was received from the process.
	 * @throws IOException if setx.exe is not found on the system.
	 */
	private static Hashtable _winSetEnvironmentResults(String strkey, String strval) throws IOException{
		String procstr = "setx.exe /M ";
		int procresult = NO_RESULT;
		Hashtable result = new Hashtable();
		result.put(VECTOR_KEY, new Vector());
		result.put(RESULT_KEY, new Integer(NO_RESULT));
		try{
			//quote a key containing spaces
			if(strkey.contains(" ")) strkey = strkey.replace(" ", "_");
			procstr += strkey;
			if (strval != null){
				if(strval.contains(" ")) {
					strval = "\""+ strval;
					// defect in setx? when string end with \" it treats it as a special char
					if(strval.endsWith("\\")){
						strval += "\\\"";
					}else{
						strval += "\"";
					}
				}
			}else{
				strval = "\"\"";
			}
			
			procstr +=" "+ strval;	
			IndependantLog.info("NativeWrapper attempting to execute: "+ procstr);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(procstr);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.start();
			try{ athread.join(10000);//should take at most a few seconds
			}catch(InterruptedException x){
				IndependantLog.info("NativeWrapper handling InterruptedException on "+ process);
			}
			console.shutdown();//just in case
			try{ 
				procresult = process.exitValue();
				result.put(RESULT_KEY, new Integer(procresult));
			}
			catch(IllegalThreadStateException x){;}
			result.put(VECTOR_KEY, console.getData());
			return result;
		}catch(IOException x){
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return result;		
	}
	

	/**
	 * Vector containing output of the REG ADD operation, may be empty.<br>
	 * This Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strname (Optional) Ex: The Name of a value to put under the key.
	 * @param strtype (Optional) Ex: REG_SZ or REG_MULTI_SZ, etc. (REG_SZ is the default)
	 * @param strval[] (Optional) One or more values to place in the strname value. Multiple 
	 * values should only be used for REG_MULTI_SZ types. 
	 * @return Hashtable containing process output "Vector" as a Vector and "Result" as an Integer.
	 * The Integer will normally be 0 if all went fine, or 1 if a reg.exe error occurred. 
	 * The Integer will contain NO_RESULT if reg.exe was not successfully processed. 
	 * The Vector may be empty if no data was received from the process.
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static Hashtable _winRegExeAddResults(String strkey, String strname, String strtype, String... strval) throws IOException{
		String dbinfo = "NativeWrapper.winRegExeAddResults: ";
		String sep = "\0";
		String procstr = "reg.exe add ";
		int procresult = NO_RESULT;
		Hashtable result = new Hashtable();
		result.put(VECTOR_KEY, new Vector());
		result.put(RESULT_KEY, new Integer(NO_RESULT));
		try{
			//quote a key containing spaces
			if(strkey.contains(" ")) strkey = "\""+ strkey +"\"";
			procstr += strkey;
			if (strname != null ){
				if(strname.length() > 0){
					if(strname.contains(" ")) strname = "\""+ strname +"\"";
					procstr +=" /v "+ strname;
				}else{
					IndependantLog.info(dbinfo +"using value name empty (Default) for key.");
					procstr +=" /ve";
				}
			}
			if (strtype != null && 
			    (
			     strtype.equals(REG_SZ) ||
			     strtype.equals(REG_MULTI_SZ) ||
			     strtype.equals(REG_EXPAND_SZ) ||
			     strtype.equals(REG_DWORD) ||
			     strtype.equals(REG_QWORD) ||
			     strtype.equals(REG_BINARY) ||
			     strtype.equals(REG_NONE)
			    )
			   ){
				procstr +=" /t "+ strtype;
				if(strtype.equals(REG_MULTI_SZ)) procstr +=" /s "+ sep;				
			}
			if (strval != null && strval.length > 0){
				IndependantLog.info(dbinfo +"attempting to process "+ strval.length +" strval parameter(s).");
				String vals = "";
				String val;
				for(int i=0;i<strval.length;i++){
					try{
						val = strval[i];
						if(val.contains(" ")) val = "\""+ val +"\"";
						vals += val;
					}catch(NullPointerException np){
						IndependantLog.info(dbinfo +"ignoring NULL strval parameter value.");
					}
					if(i<strval.length -1) vals += sep;
				}
				if (vals.length()>0){
					procstr += " /d "+ vals;					
				}
			}else{
				IndependantLog.info(dbinfo +"optional strval parameter(s) are NOT available.");
			}
			procstr += " /f";
			IndependantLog.info(dbinfo +"attempting to execute: "+ procstr);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(procstr);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.setDaemon(true);
			athread.start();
			int li = 0;//loop count
			int lm = 10;// max loop
			while(athread.isAlive()&& (li++ < lm)){
				try{Thread.sleep(1000);}catch(InterruptedException ix){ }
			}
			if(!athread.isAlive()) {
				console.shutdown();//just in case
			}else{
				IndependantLog.info("WARNING: "+ dbinfo+"process console was NOT shutdown due to long-running process.");
			}
			try{ 
				procresult = process.exitValue();
				result.put(RESULT_KEY, new Integer(procresult));
			}
			catch(IllegalThreadStateException x){
				IndependantLog.info("WARNING: "+ dbinfo+"process has NOT completed in timeout period!\n"+procstr);
				IndependantLog.info("WARNING: "+ dbinfo+"data returned from process may be incomplete!");
		    }
			result.put(VECTOR_KEY, console.getData());
			return result;
		}catch(IOException x){
			throw x;
		}catch(Exception x){
			IndependantLog.info(dbinfo+"UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return result;		
	}
	
	/**
	 * Vector containing output of the REG DELETE operation, may be empty.<br>
	 * This Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\System\CurrentControlSet\Control\Session Manager\Environment"
	 * @param strname Ex: "SAFSDIR".
	 * @return Hashtable containing process output "Vector" as a Vector and "Result" as an Integer.
	 * The Integer will normally be 0 if all went fine, or 1 if a reg.exe error occurred. 
	 * The Integer will contain NO_RESULT if reg.exe was not successfully processed. 
	 * The Vector may be empty if no data was received from the process.
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static Hashtable _winRegExeDeleteResults(String strkey, String strname) throws IOException{
		String sep = "\0";
		String procstr = "reg.exe delete ";
		int procresult = NO_RESULT;
		Hashtable result = new Hashtable();
		result.put(VECTOR_KEY, new Vector());
		result.put(RESULT_KEY, new Integer(NO_RESULT));
		if(strkey==null || strname == null) {
			IndependantLog.info("NativeWrapper _winRegExeDelete cannot accept null parameter values.");
			return result;
		}
		try{
			//quote a key containing spaces
			if(strkey.contains(" ")) strkey = "\""+ strkey +"\"";
			procstr += strkey;
			if(strname.contains(" ")) strname = "\""+ strname +"\"";
			procstr +=" /v "+ strname +" /f";	
			IndependantLog.info("NativeWrapper attempting to execute: "+ procstr);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(procstr);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.start();
			try{ athread.join(10000);//should take at most a few seconds
			}catch(InterruptedException x){
				IndependantLog.info("NativeWrapper handling InterruptedException on "+ process);
			}
			console.shutdown();//just in case
			try{ 
				procresult = process.exitValue();
				result.put(RESULT_KEY, new Integer(procresult));
			}
			catch(IllegalThreadStateException x){;}
			result.put(VECTOR_KEY, console.getData());
			return result;
		}catch(IOException x){
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return result;		
	}
	
	/**
	 * True if the requested "registry key" already exists, false otherwise.<br>
	 * This Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strval Ex:"SpyHeapSize"
	 * @return true if the key and value exists, false if it does not.
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static boolean _winKeyExistsWRegEXE(String strkey, String strval) throws IOException{
		try{
			Hashtable result = _winRegExeQueryResults(strkey, strval);
			int procresult = ((Integer)result.get(RESULT_KEY)).intValue();
			Vector data = (Vector) result.get(VECTOR_KEY);
			//now check the data, if any
			int lines = data.size();
			IndependantLog.info("NativeWrapper found "+ lines +" lines of process output...");
			if (lines == 0) return false;//no lines means no success
			String line = null;
			if(procresult == 1){
				IndependantLog.info("NativeWrapper processing error...");
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.debug("NativerWrapper REG.EXE: "+ line);
						}
					}
				}
				return false;
			}
			if(procresult == 0) return true;
			IndependantLog.info("NativerWrapper REG.EXE could not determine if key exists...");
		}catch(IOException x){
			IndependantLog.info("NativerWrapper aborting. Required REG.EXE may not be present:"+ x.getMessage());
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return false;		
	}
	
	/**
	 * Retrieves the value of the requested "registry key".<br>
	 * This Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * 
	 * @param strval Ex:"SpyHeapSize". A null means get ALL values under the key.  These would 
	 * be returned as bracketed key=val pairs one right after another.  If strval is not null, 
	 * then we will return only the value for the one (first) key in the reg.exe response.
	 * 
	 * @return String value of the key, or null.  An empty string may be possible if no 
	 * values are retrieved for a valid key.  If strval is null, then all the values will 
	 * be returned bracketed in the format: [key=val][key2=val2][key3=val3] etc...
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static String _winKeyValueWRegEXE(String strkey, String strval) throws IOException{
		try{
			Hashtable result = _winRegExeQueryResults(strkey, strval);
			int procresult = ((Integer)result.get(RESULT_KEY)).intValue();
			Vector data = (Vector) result.get(VECTOR_KEY);
			//now check the data, if any
			int lines = data.size();
			IndependantLog.info("NativeWrapper found "+ lines +" lines of process output...");
			if (lines == 0) return null;//no lines means no success
			String line = null;
			// send any error info to the debug log, if possible
			// possibly 3 lines of standard output followed by a blank line then 1 line of ERR
			if(procresult == 1){
				IndependantLog.info("NativeWrapper processing error...");
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.debug("NativerWrapper REG.EXE: "+ line);
						}
					}
				}
				return null;
			}
			if(procresult == 0){
				IndependantLog.info("NativeWrapper processing success...");
				String[] fields = null;
				String key = null;
				String val = null;
				String returnval = "";
				//values seem to start on line 4, but don't count on it
				//(strval==null) a key with no values has no data on or after line 4
				//(strval==null) a key with many stored values has a separate line 
				//               for each key value on and after line 4
				//(strval valid) a single line at line 4 with the value
				//values are stored as TAB delimited fields: keyname, type, value
				for(int i=0 ; i<lines ; i++){
					line = (String)data.elementAt(i);
					IndependantLog.info("GetValue processing: "+ line);
					val = "";
					if(line != null){
						if(line.startsWith(ProcessCapture.OUT_PREFIX)) {
							try{
								line = line.substring(ProcessCapture.OUT_PREFIX.length());
								//string format in WinXP: "subkey\tREG_*\tvalue"
								//string format in Win7:  "subkey    REG_*    value"
								//so use below dataTypeReg as delimiter to split the string to support Winxp and Win7
								//two fields returned, 1)subkey  2)value
								String dataTypeReg = "[ \t]+REG_[A-Z_]+[ \t]+";
								fields = line.split(dataTypeReg);
								if(fields.length > 1){
									key = fields[0].trim();
									if(fields.length==2) val = fields[1].trim();
									if(strval != null) return val;
									returnval +="["+key+"="+val+"]";
								} 
							}catch(Exception x){/* ignore */}
						}
					}
				}
				return returnval;
			}
			IndependantLog.info("NativerWrapper REG.EXE could not determine if key exists...");
		}catch(IOException x){
			IndependantLog.info("NativerWrapper aborting. Required REG.EXE may not be present:"+ x.getMessage());
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		return null;		
	}

	
	/**
	 * Add/Set the value of the specified "registry key".<br>
	 * This Windows version uses Reg.EXE which is typically supplied by Windows since WindowsXP.  
	 * If this Reg.EXE is not present on the System PATH then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strname (Optional) Ex: The Name of a value to put under the key.
	 * @param strtype (Optional) Ex: REG_SZ or REG_MULTI_SZ, etc. (REG_SZ is the default)
	 * @param strval[] (Optional) One or more values to place in the strname value. Multiple 
	 * values should only be used for REG_MULTI_SZ types. 
	 * @return true if the operation completed successfully.  false otherwise.
	 * @throws IOException if reg.exe is not found on the system.
	 */
	private static boolean _winSetKeyValueWRegEXE(String strkey, String strname, String strtype, String... strval) throws IOException{
		String dbinfo = "NativeWrapper.winSetKeyValueWRegEXE: ";
		try{
			Hashtable result = _winRegExeAddResults(strkey, strname, strtype, strval);
			int procresult = ((Integer)result.get(RESULT_KEY)).intValue();
			Vector data = (Vector) result.get(VECTOR_KEY);
			//now check the data, if any
			int lines = data.size();
			IndependantLog.info(dbinfo+"found "+ lines +" lines of process output...");
			if (lines == 0) return false;//no lines means no success

			String line = null;
			// send any error info to the debug log, if possible
			// possibly 3 lines of standard output followed by a blank line then 1 line of ERR
			if(procresult != 0){
				IndependantLog.info(dbinfo+"processing error...");
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.debug(dbinfo+"REG.EXE: "+ line);
						}
					}
				}
				return false;
			}

			if(procresult == 0){
				String msg = "";
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						msg += line + "\n";
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.info(dbinfo+"assuming error: "+ line);
							return false;
						}
					}
				}
				IndependantLog.info(dbinfo+"assuming success: "+ msg);
				return true;
			}
		}catch(IOException x){
			IndependantLog.info(dbinfo+"aborting. Required REG.EXE may not be present:"+ x.getMessage());
			throw x;
		}catch(Exception x){
			IndependantLog.info(dbinfo+"UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		IndependantLog.info(dbinfo+"REG.EXE does not appear to be successful.");
		return false;		
	}

	
	/**
	 * Add/Set the value of the specified System Environment Variable.<br>
	 * This Windows version uses SETX.EXE which is typically supplied by Windows since Vista.  
	 * If this SETX.EXE is not present on the System PATH then this function will throw an IOException.
	 * <p>
	 * @param strkey Ex: "MY_VAR"
	 * @param strval Ex:"My variable value"
	 * @return true if the operation completed successfully.  false otherwise.
	 * @throws IOException if setx.exe is not found on the system.
	 */
	private static boolean _winSetEnvironmentEXE(String strkey, String strval) throws IOException{
		try{
			Hashtable result = _winSetEnvironmentResults(strkey,strval);
			int procresult = ((Integer)result.get(RESULT_KEY)).intValue();
			Vector data = (Vector) result.get(VECTOR_KEY);
			//now check the data, if any
			int lines = data.size();
			IndependantLog.info("NativeWrapper found "+ lines +" lines of process output...");
			if (lines == 0) return false;//no lines means no success

			String line = null;
			// send any error info to the debug log, if possible
			// possibly 3 lines of standard output followed by a blank line then 1 line of ERR
			if(procresult != 0){
				IndependantLog.info("NativeWrapper processing error...");
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.debug("NativerWrapper SetX: "+ line);
						}
					}
				}
				return false;
			}

			if(procresult == 0){
				String msg = "";
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						msg += line + "\n";
						if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
							IndependantLog.info("NativeWrapper assuming error: "+ line);
							return false;
						}
					}
				}
				IndependantLog.info("NativeWrapper assuming success: "+ msg);
				return true;
			}
		}catch(IOException x){
			IndependantLog.info("NativerWrapper aborting. Required SETX.EXE may not be present:"+ x.getMessage());
			throw x;
		}catch(Exception x){
			IndependantLog.info("NativerWrapper UNHANDLED "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		IndependantLog.info("NativerWrapper SETX.EXE does not appear to be successful.");
		return false;		
	}
	
	/**
	 * Set/Create a registry key, and/or key value.  
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will always return false.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strname (Optional) Ex: The Name of a value to put under the key.
	 * @param strtype (Optional) Ex: REG_SZ or REG_MULTI_SZ, etc. (REG_SZ is the default)
	 * @param strval[] (Optional) One or more values to place in the strname value. Multiple 
	 * values should only be used for REG_MULTI_SZ types.  
	 * @return true if the operation completed successfully.  false otherwise.
	 */
	public static boolean SetRegistryKeyValue(Object key, Object valuename, String strtype, Object... vals){
		
		if(Platform.isWindows()){
			if(key == null) {
				IndependantLog.info("NativeWrapper.SetRegistryKeyValue MUST not be null.  Exiting without success.");
				return false;
			}
			String rawkey = (String) key;
			String strkey = new String(rawkey);
			String rawname = null;
			if (valuename != null) rawname = (String) valuename;
			String strname = rawname == null ? null:new String(rawname);
			String[] arrval = null;
			if(vals != null){
				arrval = new String[vals.length];
				for(int i=0;i<vals.length;i++){
					arrval[i] = (String) vals[i];
				}
			}
			boolean tresult = false;
			try{ tresult = _winSetKeyValueWRegEXE(strkey, strname, strtype, arrval);}
			catch(IOException x){
				IndependantLog.info("NativeWrapper.SetRegistryKeyValue "+ x.getMessage());
			}
			//alternatives go here--like WSH
			return tresult;
		}
		IndependantLog.info("NativerWrapper.SetRegistryKeyValue is not supported on this platform.");
		return false;
	}
	
	/**
	 * Remove a registry key, and/or key value.  
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will always return false.
	 * <p>
	 * @param strkey Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param strname (Optional) Ex: The Name of a value to put under the key.
	 * @return true if the operation completed successfully.  false otherwise.
	 */
	public static boolean RemoveRegistryKeyValue(Object key, Object valuename){
		
		if(Platform.isWindows()){
			if(key == null || valuename == null) {
				IndependantLog.info("NativeWrapper.RemoveRegistryKeyValue MUST not be null.  Exiting without success.");
				return false;
			}
			String rawkey = (String) key;
			String strkey = new String(rawkey);
			String rawname = null;
			if (valuename != null) rawname = (String) valuename;
			String strname = rawname == null ? null:new String(rawname);
			boolean tresult = false;
			try{ 
				Hashtable rc = _winRegExeDeleteResults(strkey, strname);
				int procresult = ((Integer)rc.get(RESULT_KEY)).intValue();
				Vector data = (Vector) rc.get(VECTOR_KEY);
				//now check the data, if any
				int lines = data.size();
				IndependantLog.info("NativeWrapper.RemoveRegistryKeyValue found "+ lines +" lines of process output...");
				if (lines == 0) return false;//no lines means no success
				String line = null;
				if(procresult == 1){
					IndependantLog.info("NativeWrapper.RemoveRegistryKeyValue processing error...");
					for(int i=0;i<lines;i++){
						line = (String)data.elementAt(i);
						if(line != null){
							if(line.startsWith(ProcessCapture.ERR_PREFIX)) {
								IndependantLog.debug("NativerWrapper.RemoveRegistryKeyValue REG.EXE: "+ line);
							}
						}
					}
					return false;
				}
				if(procresult == 0) return true;
			}
			catch(IOException x){
				IndependantLog.info("NativeWrapper.RemoveRegistryKeyValue "+ x.getMessage());
			}
			//alternatives go here--like WSH
			return tresult;
		}
		IndependantLog.info("NativerWrapper.RemoveRegistryKeyValue is not supported on this platform.");
		return false;
	}
	
	/**
	 * Set/Create a System Environment Variable.  
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses SetX.EXE Vista and later.  If this EXE is 
	 * not present on the Windows system then will try to set the value through 
	 * the Registry.
	 * <p>
	 * Because the system combines LOCAL variables and USER variables on any get, when we 
	 * go to SET we do NOT want to have USER variable values mixed in.  They will be duplicated 
	 * if they are mixed in.  So, on any SET, we must try to make sure we remove USER variables 
	 * from the value we are setting.
	 * <p>
	 * @param strkey Ex:"MY_VAR_NAME"
	 * @param strval Ex:"My variable value." 
	 * @return true if the operation completed successfully.  false otherwise.
	 */
	public static boolean SetSystemEnvironmentVariable(Object key, Object value){
		
		if(Platform.isWindows()){
			if(key == null) {
				IndependantLog.info("NativerWrapper.SetSystemEnvironmentVariable VarName MUST not be null.  Exiting without success.");
				return false;
			}
			String rawkey = (String) key;
			String strkey = new String(rawkey);
			String rawvalue = null;
			String strvalue = null;
			if (value != null) {
				rawvalue = (String) value;
			}
			strvalue = rawvalue == null ? null:new String(rawvalue);
			try{ 
				return _winSetEnvironmentEXE(strkey, strvalue);}
			catch(IOException x){
				IndependantLog.info("NativerWrapper.SetSystemEnvironmentVariable trying registry...");
				return SetRegistryKeyValue("HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment", strkey, null, strvalue);
			}
		}
		IndependantLog.info("NativerWrapper.SetSystemEnvironmentVariable is not supported on this platform.");
		return false;
	}
	
	/**
	 * Delete/Remove a System Environment Variable.  
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses REG.EXE Vista and later.  If this EXE is 
	 * not present on the Windows system then we will exit with failure.
	 * <p>
	 * @param strkey Ex:"SAFSDIR"
	 * @return true if the operation completed successfully.  false otherwise.
	 */
	public static boolean RemoveSystemEnvironmentVariable(Object key){
		
		if(Platform.isWindows()){
			if(key == null) {
				IndependantLog.info("NativerWrapper.RemoveSystemEnvironmentVariable VarName MUST not be null.  Exiting without success.");
				return false;
			}
			String rawkey = (String) key;
			try{ 
				Hashtable result = _winRegExeDeleteResults("HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment", rawkey);
				int procresult = ((Integer)result.get(RESULT_KEY)).intValue();
				Vector data = (Vector) result.get(VECTOR_KEY);
				//now check the data, if any
				int lines = data.size();
				IndependantLog.info("NativeWrapper found "+ lines +" lines of process output...");
				if (lines == 0) return false;//no lines means no success?	
				String line = null;
				// send any error info to the debug log, if possible
				for(int i=0;i<lines;i++){
					line = (String)data.elementAt(i);
					if(line != null){
						IndependantLog.debug("NativerWrapper REG DELETE: "+ line);
					}
				}
				return procresult == 0;
			}catch(IOException x){
				IndependantLog.info("NativeWrapper.RemoveSystemEnvironmentVariable "+ x.getMessage());
				return false;
			}
		}
		IndependantLog.info("NativerWrapper.RemoveSystemEnvironmentVariable is not supported on this platform.");
		return false;
	}
	
	/**
	 * Get a System Environment Variable.  
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses ECHO.
	 * <p>
	 * Note: variables set or changed after JVM start are NOT available to the currently 
	 * running JVM through System.getEnv().  This is because the JVM does not refresh its 
	 * Environment variable space after launch.  Use GetSystemEnvironmentVariable to get 
	 * the latest "refreshed" value of any System Environment Variable.
	 * <p>
	 * @param strkey Ex:"MY_VAR_NAME"
	 * @return String value of the variable, or null if it does not exist.
	 */
	public static String GetSystemEnvironmentVariable(Object key){
		
		if(Platform.isWindows()){
			if(key == null) {
				IndependantLog.info("NativerWrapper.GetSystemEnvironmentVariable VarName MUST not be null.  Exiting without success.");
				return null;
			}
			String rawkey = (String) key;
			String strkey = new String(rawkey);
			return (String)GetRegistryKeyValue("HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment", strkey);
		}
		IndependantLog.info("NativerWrapper.GetSystemEnvironmentVariable is not supported on this platform.");
		return null;
	}
	/**
	 * Return the value of the specified key.  Null if the value does not exist.<br>
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is 
	 * not present on the Windows system then this function will always return false.
	 * <p>
	 * @param key For Windows this is a String. Ex:"HKLM\Software\Rational Software\Rational Test\8\Options"
	 * @param valuename For Windows this is a String. Ex:"SpyHeapSize"
	 * @return Windows returns a String or null. Ex:"0x00200000".  An empty string may be possible if no 
	 * values are retrieved for a valid key.  If valuename is null, then all the key values will 
	 * be returned bracketed in the format: [key=val][key2=val2][key3=val3] etc... 
	 */
	public static Object GetRegistryKeyValue(Object key, Object valuename){
		
		if(Platform.isWindows()){
				String rawkey = (String) key;
				String strkey = new String(rawkey);
				String strval = null;
				if (valuename != null) strval = (String) valuename;
				String tresult = null;
				try{ tresult = _winKeyValueWRegEXE(strkey, strval);}
				catch(IOException x){
					IndependantLog.info("NativeWrapper.GetRegistryKeyValue trying alternatives...");
				}
				//alternatives go here--like WSH
				return tresult;
		}
		IndependantLog.info("NativerWrapper.GetRegistryKeyValue is not supported on this platform.");
		return null;
	}
	
	/**
	 * Retrieves last error via GetLastError and also attempts to debug log the error code 
	 * and any system message for the error code. 
	 * @return last error code encountered or 0
	 */
	protected static int _processLastError(){
		int error = 0;
		if(Platform.isWindows()){
			error = Kernel32.INSTANCE.GetLastError();
			if(error==0){
				IndependantLog.debug("GetLastError did not provide any error information. Error: 0.");
			}else{
				int size = 0;
				int nSize = 4096;
				// LOWER BYTE = 0 =           NO OUTPUT LINE WIDTH RESTRICTIONS
				int dwFlags = 0x00001000 | // FORMAT_MESSAGE_FROM_SYSTEM
				              0x00000200 ; // FORMAT_MESSAGE_IGNORE_INSERTS
				Memory lpBuffer = new Memory(nSize);
				if(lpBuffer.isValid()) lpBuffer.clear();
				size = Kernel32.INSTANCE.FormatMessageA(dwFlags, null, error, 0, lpBuffer, nSize, null);
				if(size <= 0){
					IndependantLog.debug("FormatErrorMessage did not retrieve a useful message for error: "+ error);
				}else{
					IndependantLog.debug("Error: "+ error +": "+ lpBuffer.getString(0));
				}
				lpBuffer = null;
			}
		}
		return error;
	}
	
	/**
	 * Platform independent entry-point to receive the ID or HANDLE of the current foreground window.
	 * The GetForegroundWindow function returns a "handle" to the foreground window--the window with 
	 * which the user is currently working.
	 * <p>For windows
	 * NOTE: WIN32: we will return a Long representing the Handle (HWND), or null on error.
	 * NOTE: Support for other Platforms will be added as needed.
	 * @return
	 * @see org.safs.natives.win32.User32#GetForegroundWindow()
	 */
	public static Object GetForegroundWindow(){
		Object rc = null;

		if(Platform.isWindows()){
			NativeLong nl = null;
			try{ 
				IndependantLog.info("NativeWrapper WIN32 GetForegroundWindow.");
				nl = User32.INSTANCE.GetForegroundWindow();
				rc = new Long(nl.longValue());
			}catch(Exception x){
				IndependantLog.debug("NativeWrapper for WIN32 GetForegroundWindow IGNORING Exception: "+ x.getClass().getSimpleName());
			}catch(Error x){
				IndependantLog.debug("NativeWrapper for WIN32 GetForegroundWindow IGNORING ERROR: "+ x.getClass().getSimpleName());
			}
		}
		return rc;
	}
	
	/**
	 * Set ForeGroundWindow active by title. If there are same title match then first window make active.
	 * Regular expression is allowed for windows title.
	 * @param title - Title of the window; Regular expression allowed.
	 * @return boolean
	 * @see org.safs.natives.win32.User32#ShowWindow(NativeLong, int)
	 */
	public static boolean SetForegroundWindow(String regex_title){
		
		if(Platform.isWindows()){ 
		
			byte[] windowText = new byte[512];
			
			try {
				
				Object[] hWnd = EnumWindows();
				if (hWnd != null) {
					for (int i = 0; i < hWnd.length; i++) {
						NativeLong nHwnd = new NativeLong(((Long)hWnd[i]).longValue());
						User32.INSTANCE.GetWindowTextA(nHwnd, windowText, 512);
						String wText = Native.toString(windowText);
						Pattern pattern = Pattern.compile(regex_title);
						boolean match = pattern.matcher(wText).find(); 
						if (match) {
							User32.INSTANCE.ShowWindow(nHwnd, User32.SW_SHOW);
							User32.INSTANCE.SetForegroundWindow(nHwnd);
							return true;
						}
					}
				}
			}catch(Exception x){
				IndependantLog.debug("NativeWrapper for WIN32 SetForegroundWindow IGNORING Exception: "+ x.getClass().getSimpleName());
			}catch(Error x){
				IndependantLog.debug("NativeWrapper for WIN32 SetForegroundWindow IGNORING ERROR: "+ x.getClass().getSimpleName());
			}
		}
		return false;
	}	
	
	/**
	 * Platform independent entry-point to receive the ID or HANDLE of the Desktop window.
	 * The GetDesktopWindow function returns a "handle" to the window on which  
	 * all other windows are painted.
	 * <p>For windows
	 * NOTE: WIN32: we will return a Long representing the Handle (HWND), or null on error.
	 * NOTE: Support for other Platforms will be added as needed.
	 * @return
	 * @see org.safs.natives.win32.User32#GetDesktopWindow()
	 */
	public static Object GetDesktopWindow(){
		Object rc = null;
		
		if(Platform.isWindows()){
			NativeLong nl = null;
			try{ 
				IndependantLog.info("NativeWrapper WIN32 GetDesktopWindow.");
				nl = User32.INSTANCE.GetDesktopWindow();
				rc = new Long(nl.longValue());
			}catch(Exception x){
				IndependantLog.debug("NativeWrapper for WIN32 GetDesktopWindow IGNORING Exception: "+ x.getClass().getSimpleName());
			}catch(Error x){
				IndependantLog.debug("NativeWrapper for WIN32 GetDesktopWindow IGNORING ERROR: "+ x.getClass().getSimpleName());
			}
		}
		return rc;
	}

	/**
	 * Platform independent entry-point to receive the ID or HANDLE of all top-level Windows.
	 * The EnumWindows function returns an array of "handles" to these top-level windows.
	 * <p>For windows
	 * NOTE: WIN32: we will return an array of Longs representing the Handles (HWND), or null on error.
	 * NOTE: Support for other Platforms will be added as needed.
	 * @return Object[] of Longs or Object[0].
	 * @see org.safs.natives.win32.User32#EnumWindows(WNDENUMPROC,Pointer)
	 */
	public static Object[] EnumWindows(){
		Object[] rc = new Object[0];
		if(Platform.isWindows()){
			try{ 
				IndependantLog.info("NativeWrapper WIN32 EnumWindows.");
				WNDENUMPROC callback = new WNDENUMPROC(){
					long handle = 0;
					public boolean callback(NativeLong hWnd, Pointer userData){						
						try{ handle = hWnd.longValue(); }
						catch(Throwable ix){
							IndependantLog.debug("EnumWindows callback error: "+ix.getClass().getSimpleName()+":"+ix.getMessage());
							handle=0;
						}
						if(handle==0){
							IndependantLog.debug("EnumWindows received last window handle.");
							return false;
						}else{
							Long newh = new Long(handle);
							if(!handles.contains(newh)) handles.add(newh);
							return true;
						}
					}
				};
				boolean success = User32.INSTANCE.EnumWindows(callback, null);
				if(!success) {
					IndependantLog.debug("NativeWrapper WIN32 EnumWindows reported UNSUCCESSFUL call to User32.INSTANCE.EnumWindows.");
					int error = _processLastError();
					// may do something with certain errors here.
					return rc;
				}
				rc = callback.handles.toArray();
			}catch(Exception x){
				System.out.println("Exception received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 EnumWindows IGNORING Exception: "+ x.getClass().getSimpleName(),x);
			}catch(Error x){
				System.out.println("ERROR received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 EnumWindows IGNORING Error: "+ x.getClass().getSimpleName());
			}
		}
		return rc;
	}
	
	/**
	 * Platform independent entry-point to receive the ID or HANDLE of all child Windows of a parent.
	 * The EnumChildWindows function returns an array of "handles" to these child windows.
	 * <p>For windows
	 * NOTE: WIN32: we will return an array of Longs representing the Handles (HWND), or null on error.
	 * NOTE: Support for other Platforms will be added as needed.
	 * @param handle to the parent. For WIN32, this is a Long.
	 * @return Object[] of Longs or Object[0].
	 * @see org.safs.natives.win32.User32#EnumWindows(WNDENUMPROC,Pointer)
	 */
	public static Object[] EnumChildWindows(Object parent){
		Object[] rc = new Object[0];
		if(Platform.isWindows()){
			try{ 
				NativeLong lparent = new NativeLong(((Long)parent).longValue());
				IndependantLog.info("NativeWrapper WIN32 EnumChildWindows.");
				WNDENUMPROC callback = new WNDENUMPROC(){
					long handle = 0;
					public boolean callback(NativeLong hWnd, Pointer userData){						
						try{ handle = hWnd.longValue(); }
						catch(Throwable ix){
							IndependantLog.debug("EnumChildWindows callback error: "+ix.getClass().getSimpleName()+":"+ix.getMessage());
							handle=0;
						}
						if(handle==0){
							IndependantLog.debug("EnumChildWindows received last child window handle.");
							return false;
						}else{
							Long newh = new Long(handle);
							if(!handles.contains(newh)) handles.add(newh);
							return true;
						}
					}
				};
				boolean success = User32.INSTANCE.EnumChildWindows(lparent, callback, null);
				if(!success) {
					IndependantLog.debug("NativeWrapper WIN32 EnumChildWindows reported UNSUCCESSFUL call to User32.INSTANCE.EnumChildWindows.");
					int error = _processLastError();
					// may do something with certain errors here.
					return rc;
				}
				rc = callback.handles.toArray();
			}catch(Exception x){
				System.out.println("Exception received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 EnumChildWindows IGNORING Exception: "+ x.getClass().getSimpleName(),x);
			}catch(Error x){
				System.out.println("Error received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 EnumChildWindows IGNORING Error: "+ x.getClass().getSimpleName());
			}
		}
		return rc;
	}

	/**
	 * The GetWindowThreadProcessId function retrieves the identifier of the thread that 
	 * created the specified window and, optionally, the identifier of the process that 
	 * created the window. 
	 * @param parent - handle to the parent. For WIN32, this is a Long.
	 * @return Object[2] [0]=ThreadID, [1]=ProcessID. For WIN32 these are both Integers.  
	 * Array values must be <> 0 to be considered valid. 
	 */
	public static Object[] GetWindowThreadProcessId(Object parent){
		Object[] rc = new Object[2];
		rc[0]= new Integer(0);
		rc[1]= new Integer(0);
		if(Platform.isWindows()){
			NativeLong lparent = new NativeLong(((Long)parent).longValue());
			Memory pidOut = new Memory(4);
			pidOut.clear();
			int thrid = 0;
			try{
				thrid = User32.INSTANCE.GetWindowThreadProcessId(lparent, pidOut);
				rc[0]=new Integer(thrid);
				rc[1]=new Integer(pidOut.getInt(0));
			}catch(Exception x){
				System.out.println("Exception received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 GetWindowThreadProcessId IGNORING Exception: "+ x.getClass().getSimpleName(),x);
			}catch(Error x){
				System.out.println("Error received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 GetWindowThreadProcessId IGNORING Error: "+ x.getClass().getSimpleName());
			}
		}
		return rc;
	}
	
	/**
	 * Retrieve the count of UI elements used by the specified process.
	 * If the process does not have any UI elements, then we should get zero.
	 * NOTE: Support for non-WIN32 platforms will be added when able.
	 * NOTE: Will not work on "protected processes" on Windows Vista.
	 * @param processID -- the id of the process to query.  For WIN32 this is an Integer.
	 * @return Object -- the number of UI elements used by the process, or 0. For WIN32 this is an 
	 * Integer.
	 * @see org.safs.natives.win32.Kernel32#GetGuiResources(Pointer, int)
	 */
	public static Object GetProcessUIResourceCount(Object processID){
		Object rc = new Integer(0);
		if(Platform.isWindows()){
			Pointer pHandle = null;
			try{
				int id = ((Integer)processID).intValue();
				// 0x0400 is PROCESS_QUERY_INFORMATION -- valid for pre-Vista OS
				// not valid for Vista and beyond?
				pHandle = Kernel32.INSTANCE.OpenProcess(0x0400, false, id);
				if(pHandle==null){
					IndependantLog.debug("NativeWrapper for WIN32 GetProcessUIResourceCount IGNORING OpenProcess handle=NULL");
					return rc;
				}
				int resources = User32.INSTANCE.GetGuiResources(pHandle, 1);
				if(resources==0){ //zero can be valid, or can be possible error.
					int errcode = Kernel32.INSTANCE.GetLastError();
					IndependantLog.debug("NativeWrapper reporting User32.GetGuiResources LastError ="+ errcode);
				}
				rc = new Integer(resources);
			}catch(Exception x){
				System.out.println("Exception received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 GetProcessUIResourceCount IGNORING Exception: "+ x.getClass().getSimpleName(),x);
			}catch(Error x){
				System.out.println("Error received: "+ x.getClass().getSimpleName());
				x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 GetProcessUIResourceCount IGNORING Error: "+ x.getClass().getSimpleName());
			}
			if(pHandle != null){
				try{Kernel32.INSTANCE.CloseHandle(pHandle);}catch(Throwable x){}
			}
			pHandle = null;
		}
		return rc;
	}
	
	
	/**
	 * Retrieve the name of the executable file for the specified process.
	 * The returned value should not include path information to the executable filename.
	 * NOTE: Support for non-WIN32 platforms will be added when able.
	 * @param processID -- the id of the process to query.  For WIN32 this is an Integer.
	 * @return Object -- the name of the process or NULL. For WIN32 this is a String. 
	 * @see org.safs.natives.win32.Kernel32#GetProcessImageFileNameA(Pointer, Pointer, int)
	 * @see org.safs.natives.win32.Psapi#GetProcessImageFileNameA(Pointer, Pointer, int)
	 */
	public static Object GetProcessFileName(Object processID){
		if (processID == null){
			IndependantLog.debug("NativeWrapper GetProcessFileName cannot process a NULL processID.");
			return null;
		}
		if(Platform.isWindows()){
			Pointer pHandle = null;
			try{
				int id = ((Integer)processID).intValue();
				// 0x0400 is PROCESS_QUERY_INFORMATION -- valid for pre-Vista OS
				// may not be valid for Vista and beyond?
				pHandle = Kernel32.INSTANCE.OpenProcess(0x0400, false, id);
				if(pHandle == null){
					IndependantLog.debug("NativeWrapper.getProcessFileName error, received a NULL process handle for process id: "+ id);
					return null;
				}
				int size = 0;
				int plen = 4096;
				Memory lpFileName = new Memory(plen);
				if(lpFileName.isValid()){
					lpFileName.clear();
				}else{
					IndependantLog.debug("NativeWrapper.getProcessFileName could not allocate memory for filename buffer!");
					Kernel32.INSTANCE.CloseHandle(pHandle);
					return null;
				}
				try{
					size = Psapi.INSTANCE.GetProcessImageFileNameA(pHandle, lpFileName, plen);
				}catch(Throwable t){
					IndependantLog.debug("NativeWrapper.getProcessFileName trying Kernel32 after Psapi "+ t.getClass().getSimpleName()+":"+t.getMessage());
					try{ size = Kernel32.INSTANCE.GetProcessImageFileNameA(pHandle, lpFileName, plen);}
					catch(Throwable t2){
						IndependantLog.debug("NativeWrapper.getProcessFileName failing with Kernel32 "+ t2.getClass().getSimpleName()+":"+t2.getMessage());
					}
				}
				if(pHandle != null) Kernel32.INSTANCE.CloseHandle(pHandle);
				pHandle = null;
				if(size>0){
					String rc = new String((String)lpFileName.getString(0));
					//IndependantLog.info("NativeWrapper.getProcessFileName returning String len: "+ rc.length()+ ", "+ rc);
					int i = rc.lastIndexOf(File.separatorChar);
					if(i > -1) rc = rc.substring(i+1);
					return rc;
				}
			}catch(Throwable x){
				//System.out.println("Exception received: "+ x.getClass().getSimpleName());
				//x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 GetProcessFileName IGNORING "+ x.getClass().getSimpleName()+": "+ x.getMessage());
				try{if (pHandle != null) Kernel32.INSTANCE.CloseHandle(pHandle);}
				catch(Throwable x2){}
			}
			if(pHandle != null) try{Kernel32.INSTANCE.CloseHandle(pHandle);}catch(Throwable x){}
			pHandle = null;
		}
		IndependantLog.debug("NativeWrapper GetProcessFileName failed to identify a process name for processID "+processID.toString());
		return null;
	}

	/**
	 * Attempt to launch a web URL via the system's default web browser.
	 * <p>
	 * On Windows this uses ShellExecute's "open" operation to open the URL document using 
	 * the default app.
	 * @param url -- User should include the 'protocol' portion of the url (Ex: 'http://')
	 * @return Object -- For WIN32 this is typically an Integer(42).  NULL on execution failures.
	 * @see org.safs.natives.win32.Shell32#ShellExecuteA(NativeLong, String, String, String, String, long) 
	 */
	public static Object LaunchURLInDefaultWebBrowser(String url){
		if(Platform.isWindows()){
			Integer result = null;			
			try{
				NativeLong hwnd = new NativeLong(0);
				result = Shell32.INSTANCE.ShellExecuteA(hwnd, "open", url, null, null, 1);
				if(result == null){
					IndependantLog.debug("NativeWrapper.LaunchURLInDefaultWebBrowser error, received a NULL result for url: "+ url);
					return null;
				}
				// initial tests indicate '42' is the normal return code on success.
				IndependantLog.info("NativeWrapper.LaunchURLInDefaultWebBrowser result for '"+ url +"': "+ String.valueOf(result.intValue()));
				return result;
			}catch(Throwable x){
				//System.out.println("Exception received: "+ x.getClass().getSimpleName());
				//x.printStackTrace();
				IndependantLog.debug("NativeWrapper for WIN32 LaunchURLInDefaultWebBrowser IGNORING "+ x.getClass().getSimpleName()+": "+ x.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * <pre>
	 * This method will get the file's created time, access time, and write time.
	 * Dor in parameter filename: it MUST be an absolute file path.
	 * For out parameters: createTime, accessTime and writeTime, if you don't want
	 * some of them, you can just pass a null value; If you want some of them, you
	 * MUST pass an instance of java.util.Date as value.
	 * </pre>
	 * 
	 * @param filename		In		The absolute file path
	 * @param createTime	Out		The java.util.Date object contains the file created time
	 * @param accessTime	Out		The java.util.Date object contains the file last accessed time
	 * @param writeTime		Out		The java.util.Date object contains the file last modified time
	 * 
	 * @return	A boolean to indicate if this method get the file time successfully
	 */
	public static boolean getFileTime(String filename, Date createTime, Date accessTime, Date writeTime){
		String debugmsg = NativeWrapper.class.getName()+".getFileTime(): ";
		boolean rc = false;
		
		if(Platform.isWindows()){
			Kernel32 k32 = Kernel32.INSTANCE;
			FileTime _createTime = new FileTime();
			FileTime _accessTime = new FileTime();
			FileTime _writeTime = new FileTime();
			
			IndependantLog.warn(debugmsg+" filename is "+filename);
			
			//First open the file, and get the handle
			Pointer handle = k32.CreateFileA(filename, Kernel32.GENERIC_NO_ACCESS, Kernel32.FILE_SHARE_R_W_D, null, Kernel32.OPEN_EXISTING, Kernel32.FILE_FLAG_BACKUP_SEMANTICS, null);
			if(k32.GetLastError()==Kernel32.ERROR_FILE_NOT_FOUND){
				IndependantLog.warn(debugmsg+" Kernel.CreateFileA can't find file "+filename+" ; Try Kernel.CreateFileW ...");
				handle = k32.CreateFileW(filename, Kernel32.GENERIC_NO_ACCESS, Kernel32.FILE_SHARE_R_W_D, null, Kernel32.OPEN_EXISTING, Kernel32.FILE_FLAG_BACKUP_SEMANTICS, null);
			}
			
			if(k32.GetLastError()== Kernel32.SUCCESS_EXECUTE){
				IndependantLog.debug(debugmsg+" Got file handle. Try to get file times ...");
				//Get the file times
				if(k32.GetFileTime(handle, _createTime, _accessTime, _writeTime)){
					IndependantLog.debug(debugmsg+" Got file times.");
					//_createTime, _accessTime, _writeTime is in format of a structure
					//we need to convert it to java long time in millisecond
					if(createTime!=null) createTime.setTime(convertFileTimeToJavaTime(_createTime));
					if(accessTime!=null) accessTime.setTime(convertFileTimeToJavaTime(_accessTime));
					if(writeTime!=null) writeTime.setTime(convertFileTimeToJavaTime(_writeTime));
					rc = true;
				}else{
					IndependantLog.error(debugmsg+" Can't get file times, meet error "+k32.GetLastError());
				}
			}else{
				IndependantLog.error(debugmsg+" Can't get file handle, meet error "+k32.GetLastError());
			}
			
			//Close the file handle
			k32.CloseHandle(handle);
		}else{
			IndependantLog.warn(StringUtils.debugmsg(false)+" Platform type "+Platform.getOSType()+" OS: "+Console.getOsFamilyName()+" is not supported!");
		}
		
		return rc;
	}
	
	/**
	 * <pre>
	 * <b>This method will convert a FileTime to JavaTime</b>
	 * FileTime contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
	 * JavaTime represents a point in time that is time milliseconds after January 1, 1970 00:00:00 GMT.
	 * 1, 000, 000 nanoseconds = 1 millisecond
	 * </pre>
	 * 
	 * @param filetime	A FileTime
	 * @return			A long value contain the java time in millisecond
	 * @see FileTime
	 * @see StringUtilities#TimeBaseDifferenceFromFileTimeToJavaTimeInMillisecond
	 */
	private static long convertFileTimeToJavaTime(FileTime filetime){
		long high = filetime.dwHighDateTime;
		long low = filetime.dwLowDateTime;
		long time = ((high << 32) & 0xFFFFFFFF00000000L) | (low & 0x00000000FFFFFFFFL);
		
		//convert time from '100 nanoseconds' to millisecond
		time = time/10000;
		
		//convert from filetime to javatime
		time = time - StringUtilities.TimeBaseDifferenceFromFileTimeToJavaTimeInMillisecond;
		
		return time;
	}

	/** command 'ping'*/
	public static final String COMMAND_PING 		= "ping";
	/** command 'ipconfig'*/
	public static final String COMMAND_IPCONFIG 	= "ipconfig";
	/** command 'ifconfig'*/
	public static final String COMMAND_IFCONFIG 	= "ifconfig";
	/** command 'hostname'*/
	public static final String COMMAND_HOSTNMAE 	= "hostname";
	/** parameter '-f' for command 'hostname', to get full qualified domain name*/
	public static final String PARAM_HOSTNMAE_f 	= "-f";
	
	/** directory 'sbin'*/
	public static final String DIRECTORY_SBIN 	= "sbin";
	
	/**
	 * Return the ipconfig's result as a List of String. This is for Windows.
	 * @param parameters String[], the parameters for command ping.
	 * @return List<String> a list containing the result of execution of command 'ipconfig'
	 */
	public static List<String> ipconfig(String... parameters){
		return execute(COMMAND_IPCONFIG, parameters);
	}
	/**
	 * Return the ifconfig's result as a List of String. This is for Mac or Unix/Linux.
	 * @param parameters String[], the parameters for command ping.
	 * @return List<String> a list containing the result of execution of command 'ifconfig'
	 */
	public static List<String> ifconfig(String... parameters){
		List<String> result = execute(COMMAND_IFCONFIG, parameters);
		if(result==null || result.isEmpty() || (result.size()==1&&result.get(0).contains("command not found"))){
			//On Linux, "ifconfig" cannot be found, we need to use "/sbin/ifconfig"
			String cmdIfconfig = File.separator+DIRECTORY_SBIN+File.separator+COMMAND_IFCONFIG;
			IndependantLog.debug("Executing "+cmdIfconfig+" with "+Arrays.toString(parameters));
			result = execute(cmdIfconfig, parameters);
		}
		IndependantLog.debug("Got result\n"+result);
		return result;
	}
	
	/**
	 * Return the ping's result as a List of String.
	 * @param hostname String, the name of the host to ping
	 * @param parameters String[], the parameters for command ping.
	 * @return List<String> a list containing the result of execution of command 'ping'
	 */
	public static List<String> ping(String hostname, String... parameters){
		List<String> params = new ArrayList<String>();
		if(parameters!=null && parameters.length>0){
			for(String param:parameters) params.add(param);
		}
		params.add(hostname);
		return execute(COMMAND_PING, params.toArray(new String[0]));
	}
	
	/**
	 * Return the result of execution of a command as a List of String.<br>
	 * <b>Note: The command MUST end with limit result, otherwise this method will block.</b>
	 * @param parameters String[], the parameters for command.
	 * @return List<String> a list containing the result of execution of command
	 */
	public static List<String> execute(String command, String... parameters){
		String debugmsg = StringUtils.debugmsg(false);
		
		ProcessCapture console = null;
		List<String> result = new ArrayList<String>();
		try {
			Process process = null;
			//append parameters
			if(parameters!=null && parameters.length>0){
				for(String param:parameters) command += " "+param+" ";
			}
			process = Runtime.getRuntime().exec(command);
			console = new ProcessCapture(process, null, true, false);
			//Wait for the console to finish the job, may block if it doesn't end.
			try{ console.thread.join();}catch(InterruptedException x){;}
			List<String> data = console.getData();
			for(String aLine:data){
				//ProcessCapture will add prefix to the console message, we need to remove it.
				if(aLine.startsWith(ProcessCapture.OUT_PREFIX)) aLine = aLine.substring(ProcessCapture.OUT_PREFIX.length());
				else if(aLine.startsWith(ProcessCapture.ERR_PREFIX)) aLine = aLine.substring(ProcessCapture.ERR_PREFIX.length());
				result.add(aLine);
			}
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}finally{
			if(console!=null) console.shutdown();
		}
		
		return result;
	}
	
	//Ping response on Windows
	//Pinging hostname [172.27.16.63] with 32 bytes of data:
	private static final String PING_WIN_PINGING = "Pinging";
	//Reply from 172.27.16.63: bytes=32 time=1ms TTL=128
	private static final String PING_WIN_REPLY_FROM_BEGIN = "Reply from";
	private static final String PING_WIN_REPLY_FROM_END_BYTES = ": bytes";
	//Reply from ::1: time<1ms
	private static final String PING_WIN_REPLY_FROM_END_TIME = ": time";
	
	//Ping response on Mac
	//PING hostname (172.27.16.63) with 56 data bytes
	private static final String PING_MAC_PINGING = "PING";
	//64 bytes from 172.27.16.63: imcp_seq=0 ttl=128 time=0.5.03 ms
	private static final String PING_MAC_REPLY_FROM_BEGIN = "bytes from";
	private static final String PING_MAC_REPLY_FROM_END_ICMP_SEQ = ": imcp_seq";
	
	/**
	 * Get the host's IP Address by command {@link NativeWrapper#COMMAND_PING}.
	 * @see #getHostIPByName(String)
	 */
	public static String getHostIPByPing(String hostname){
		String debugmsg = StringUtils.debugmsg(false);
		String hostIP = null;
		
		if(StringUtils.isValid(hostname)){
			//Use command "ping" to get the IP address
			try {
				String count = "";
				if(Console.isWindowsOS()){
					count = " -n 4 ";
				}else if(Console.isUnixOS() || Console.isMacOS()){
					count = " -c 4 ";
				}
				
				List<String> data = ping(hostname, count); ;
				int beginIndex, endIndex;
				if(data!=null && data.size()>0){
					for(String message:data){
						//Ping request could not find host ho. Please check the name and try again.
						if(message.contains(PING_WIN_PINGING)){
							beginIndex = message.indexOf("[");
							endIndex = message.indexOf("]");
							if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
								hostIP = message.substring(beginIndex+1, endIndex);
								break;
							}
						}else if(message.contains(PING_MAC_PINGING)){
							beginIndex = message.indexOf("(");
							endIndex = message.indexOf(")");
							if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
								hostIP = message.substring(beginIndex+1, endIndex);
								break;
							}
						}else if(message.contains(PING_WIN_REPLY_FROM_BEGIN)){
							beginIndex = message.indexOf(PING_WIN_REPLY_FROM_BEGIN);
							endIndex = message.indexOf(PING_WIN_REPLY_FROM_END_BYTES);
							if(endIndex<0) endIndex = message.indexOf(PING_WIN_REPLY_FROM_END_TIME);
							
							if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
								hostIP = message.substring(beginIndex+PING_WIN_REPLY_FROM_BEGIN.length(), endIndex);
								break;
							}
						}else if(message.contains(PING_MAC_REPLY_FROM_BEGIN)){
							beginIndex = message.indexOf(PING_MAC_REPLY_FROM_BEGIN);
							endIndex = message.indexOf(PING_MAC_REPLY_FROM_END_ICMP_SEQ);
							
							if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
								hostIP = message.substring(beginIndex+PING_MAC_REPLY_FROM_BEGIN.length(), endIndex);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
			}
			
			if(hostIP!=null) hostIP = hostIP.trim();
		}
		
		return hostIP;
	}
	
	//The prefix of response of command 'ipconfig'
	//IPv4 Address. . . . . . . . . . . : 192.168.17.89
	private static final String IPCONFIG_IPV4_ADDRESS = "IPv4 Address";
	//inet 192.168.2.2 netmask 0xfffff800 broadcast 192.168.2.255
	//inet addr:192.168.2.2  Bcast:192.168.2.255  Mask:255.255.255.0
	private static final String IFCONFIG_INET = "inet";
	//inet6 fe80::21d:92ff:fede:499b/64 prefixlen 64 scropid 0x4
	//inet6 addr: fe80::21d:92ff:fede:499b/64 Scope:Link
	private static final String IFCONFIG_INET6 = "inet6";
	private static final String IFCONFIG_ADDR = "addr:";
	//lo        Link encap:Local Loopback
	//lo0:  flags=8049<UP,LOOPBACK,RUNNING,MULTICAT> mtu 16384
	private static final String IFCONFIG_LO = "lo";
	private static final String IFCONFIG_REGEX_LO = "^"+IFCONFIG_LO+"\\d*:*.*$";
	//en0:  flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
	private static final String IFCONFIG_EN = "en";
	/**Regex to match string starting with en, en0, en1, en0:, en1: etc.*/
	private static final String IFCONFIG_REGEX_EN = "^"+IFCONFIG_EN+"\\d*:*.*$";
	//eth0      Link encap:Ethernet  HWaddr 00:50:56:8D:53:75, 
	private static final String IFCONFIG_ETH = "eth";
	/**Regex to match staring starting with eth, eth0, eth1, eth0:, eth1: etc.*/
	private static final String IFCONFIG_REGEX_ETH = "^"+IFCONFIG_ETH+"\\d:*.*$";
	
	/**
	 * Execute {@link #COMMAND_IPCONFIG} or {@link #COMMAND_IFCONFIG} to parse the local IP address.
	 * @return String, IP address of localhost.
	 */
	public static String getLocalHostIPByConfig(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostIP = null;
		try {
			List<String> data = null;
			
			if(Console.isWindowsOS()){
				data = NativeWrapper.ipconfig();
				//IPv4 Address. . . . . . . . . . . : 192.168.17.89
			}else if(Console.isMacOS() || Console.isUnixOS()){
				data = NativeWrapper.ifconfig();
				//For mac
				//data = NativeWrapper.ifconfig(" en0 ");
				//inet6 fe80::21d:92ff:fede:499b/64 prefixlen 64 scropid 0x4				
				//inet 192.168.2.2 netmask 0xfffff800 broadcast 192.168.2.255
				//For Unix
				//data = NativeWrapper.ifconfig(" eth0 ");
				//inet6 addr: fe80::21d:92ff:fede:499b/64 Scope:Link
				//inet addr:192.168.2.2  Bcast:192.168.2.255  Mask:255.255.255.0
			}else{
				IndependantLog.warn(debugmsg+" not implemented for OS '"+Console.getOsFamilyName()+"'");
			}
			
			int lastColonIndex = -1;
			String trimmedMsg = null;
			/**If the response start with en or eth, which means the following inet or inet6 address is ethernet or broadcasting, not loopback*/
			boolean isEthernet = false;
			if(data!=null && data.size()>0){
				for(String message:data){
					try{
						trimmedMsg = message.trim();
						IndependantLog.debug("processing '"+trimmedMsg+"' with isEthernet="+isEthernet);
						if(trimmedMsg.startsWith(IPCONFIG_IPV4_ADDRESS)){
							//IPv4 Address. . . . . . . . . . . : 192.168.17.89
							lastColonIndex = trimmedMsg.lastIndexOf(StringUtils.COLON);
							if(lastColonIndex>-1) hostIP = trimmedMsg.substring(lastColonIndex+StringUtils.COLON.length());
							break;
						}else if(isEthernet && trimmedMsg.startsWith(IFCONFIG_INET) && !trimmedMsg.startsWith(IFCONFIG_INET6)){
							//we don't want loopback address
							//lo        Link encap:Local Loopback
							//          inet addr:127.0.0.1  Mask:255.0.0.0
							//          inet6 addr: ::1/128 Scope:Host
							
							//eth0      Link encap:Ethernet  HWaddr 00:50:56:8D:53:75
							//en0:  flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
							//inet 192.168.2.2 netmask 0xfffff800 broadcast 192.168.2.255
							//inet addr:192.168.2.2  Bcast:192.168.2.255  Mask:255.255.255.0
							trimmedMsg = trimmedMsg.substring(IFCONFIG_INET.length()).trim();
							if(trimmedMsg.startsWith(IFCONFIG_ADDR)) trimmedMsg = trimmedMsg.substring(IFCONFIG_ADDR.length());
							hostIP = trimmedMsg.split("\\s")[0];
							break;
						}else if(trimmedMsg.matches(IFCONFIG_REGEX_EN) || trimmedMsg.matches(IFCONFIG_REGEX_ETH)){
							IndependantLog.debug("set isEthernet to true.");
							isEthernet = true;
						}else if(trimmedMsg.startsWith(IFCONFIG_REGEX_LO)){
							//The following responses are for LOOPBACK
							IndependantLog.debug("set isEthernet to false, it is loopback informations.");
							isEthernet = false;
						}
					} catch (Exception e) {
						IndependantLog.warn(debugmsg+" during parse, met "+StringUtils.debugmsg(e));
					}
				}
			}
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		
		if(hostIP!=null) hostIP = hostIP.trim();
		
		return hostIP;
	}
	
	/**
	 * @return String, hostname of localhost
	 */
	public static String getLocalHostName(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostname = null;
		try {
			List<String> data = null;
			if(Console.isWindowsOS()){
				data = NativeWrapper.execute(COMMAND_HOSTNMAE);
			}else if(Console.isMacOS() || Console.isUnixOS()){
				data = NativeWrapper.execute(COMMAND_HOSTNMAE, PARAM_HOSTNMAE_f);
			}else{
				data = NativeWrapper.execute(COMMAND_HOSTNMAE);
				if(data==null || data.isEmpty()){
					IndependantLog.warn(debugmsg+COMMAND_HOSTNMAE+" is not supported yet for OS '"+Console.getOsFamilyName()+"'");
				}
			}
			if(data!=null) hostname = data.get(0);
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		return hostname;
	}
	
	/**
	 * test method {@link #getFileTime(String, Date, Date, Date)}
	 * @param args String[], arguments array from which to get
	 */
	private static void test_getHostIp(String[] args){
		System.out.println("-------------------------   test_getHostIp   -------------------------");
		
		String hostname = null;
		
		for(int i=0;i<args.length;i++){
			if(ARG_HOSTNAME.equalsIgnoreCase(args[i])){
				if(i+1<args.length){
					hostname = args[++i];
					break;
				}
			}
		}
		String ip=null;
		if(StringUtils.isValid(hostname)){
			ip = getHostIPByPing(hostname);
			System.out.println("The IP for host '"+hostname+"' is "+ip);
		}else{
			System.err.println("hostname '"+hostname+"' is not valid.");
		}
		
		System.out.println("The Local host IP is "+getLocalHostIPByConfig());
		System.out.println("The Local host Name is "+getLocalHostName());
		
		System.out.println("-----------------------------------------------------------------");
	}
	
	/**
	 * test method {@link #getFileTime(String, Date, Date, Date)}
	 * @param args String[], arguments array from which to get file name
	 */
	private static void test_getFileTime(String[] args){
		System.out.println("-------------------------   test_getFileTime   -------------------------");
		java.sql.Timestamp ct = new java.sql.Timestamp(0);
		java.sql.Timestamp wt = new java.sql.Timestamp(0);
		java.sql.Timestamp at = new java.sql.Timestamp(0);
		
		String file = "C:\\Windows\\";

		for(int i=0;i<args.length;i++){
			if(ARG_FILE_FOR_GETTIME.equalsIgnoreCase(args[i])){
				if(i+1<args.length){
					file = args[++i];
					break;
				}
			}
		}
		
		System.out.println("GetFileTime for File '"+file+"'");
		if (getFileTime (file, ct, at, wt)){
			System.out.println("	Created Time: " + ct);
			System.out.println("	Access Time: " + at);
			System.out.println("	Modified Time: " + wt);
		}else{
			System.out.println("GetFileTime Fail");
		}
		System.out.println("-----------------------------------------------------------------");
	}

	/**"-fileforgettime" followed by a full path file name, for testing {@link #getFileTime(String, Date, Date, Date)} */
	public static final String ARG_FILE_FOR_GETTIME = "-fileforgettime";
	/**"-hostname" followed by a hostname, for testing {@link #getHostIPByPing(String)} */
	public static final String ARG_HOSTNAME = "-hostname";
	
	/**
	 * Test some implementations of this class.<br>
	 * Simple regression tests with output to System.out
	 * <p>
	 * java org.safs.natives.NativeWrapper > outputFile.txt
	 * <p>
	 * To test {@link #getFileTime(String, Date, Date, Date)}, call as following:<br>
	 * {@code java java org.safs.natives.NativeWrapper -fileforgettime fullPathFileName > outputFile.txt}<br>
	 * @param args String[], <br>
	 *             If {@link #ARG_DEBUG} is present, then show debug message on console<br>
	 *             if {@link #ARG_FILE_FOR_GETTIME} followed by a full path file name such as "c:\temp\myfile.txt", then {@link #test_getFileTime(String[])}<br>
	 *             if {@link IndependantLog#ARG_DEBUG} followed by host name such as machine.domain.com, then {@link #test_getHostIp(String[])}<br>
	 */
	public static void main(String[] args){
		
		
		IndependantLog.parseArguments(args);
		IndependantLog.debug("Test NativeWrapper on OS '"+Console.getOsFamilyName()+"', Platform type "+Platform.getOSType());
		
		SetRegistryKeyValue("HKEY_CLASSES_ROOT\\Wow6432Node\\AppID\\{AD514E88-9335-4CE6-80A6-10CF68CCD0AA}", "", null);
  		System.out.println("GetRegistryKeyValue should be [<NO NAME>=]");
 
		String key = "HKLM\\Software";
		String keyvalue = (String) GetRegistryKeyValue(key, null);
		System.out.println(key +":"+ keyvalue);
		
		//reg.exe does not output <no name> if no other key value exists
		System.out.println("GetRegistryKeyValue should be empty");
		key = "HKLM\\Software\\Secure";
		keyvalue = (String) GetRegistryKeyValue(key, null);
		System.out.println(key +":"+ keyvalue);

		System.out.println("GetRegistryKeyValue should be [multiple entries] if present");
		key = "HKLM\\Software\\Rational Software\\Rational Test\\8\\Options";
		keyvalue = (String) GetRegistryKeyValue(key, null);
		System.out.println(key +":"+ keyvalue);

		System.out.println("GetRegistryKeyValue should be valid path");
		key = "HKLM\\Software\\Rational Software\\Rational Test\\8";
		keyvalue = (String) GetRegistryKeyValue(key, "Eclipse Directory");
		System.out.println(key +":"+ keyvalue);

		System.out.println("GetRegistryKeyValue should be null");
		key = "HKLM\\Software\\Rational Software\\Rational Test\\8";
		keyvalue = (String) GetRegistryKeyValue(key, "Bogus Directory");
		System.out.println(key +":"+ keyvalue);

		System.out.println("DoesRegistryKeyExist should be true");
		key = "HKLM\\Software";
		boolean keyexists = DoesRegistryKeyExist(key, null);
		System.out.println(key +":"+ keyexists);
		
		System.out.println("DoesRegistryKeyExist should be false");
		key = "HKLM\\BogusSoftware";
		keyexists = DoesRegistryKeyExist(key, null);
		System.out.println(key +":"+ keyexists);

		System.out.println("DoesRegistryKeyExist should be true");
		key = "HKEY_LOCAL_MACHINE\\Software";
		keyexists = DoesRegistryKeyExist(key, null);
		System.out.println(key +":"+ keyexists);
		
		System.out.println("DoesRegistryKeyExist should be true");
		key = "HKLM\\Software\\Rational Software\\Rational Test\\8";
		keyexists = DoesRegistryKeyExist(key, null);
		System.out.println(key +"\\"+":"+ keyexists);
		
		System.out.println("DoesRegistryKeyExist should be true");
		key = "HKLM\\Software\\Rational Software\\Rational Test\\8";
		keyexists = DoesRegistryKeyExist(key, "Eclipse Directory");
		System.out.println(key +"\\"+ "Eclipse Directory"+ ":"+ keyexists);
		
		System.out.println("DoesRegistryKeyExist should be false");
		key = "HKEY_LOCAL_MACHINE\\Software\\Rational Software\\Rational Test\\8";
		keyexists = DoesRegistryKeyExist(key, "Bogus Directory");
		System.out.println(key +"\\"+ "Bogus Directory"+ ":"+ keyexists);
		
		System.out.println("Testing GetForegroundWindow().  should not be NULL.");
		Long foreground = (Long)GetForegroundWindow();
		System.out.println("GetForgroundWindow()=="+ foreground);
		Object[] ids = GetWindowThreadProcessId(foreground);
		System.out.println("Foreground ThreadID: "+ ids[0]);
		System.out.println("Foreground ProcesID: "+ ids[1]);
		String procname = (String) GetProcessFileName(ids[1]);
		System.out.println("Foreground ProcessName: "+ procname);
		System.out.println("");
		System.out.println("Testing EnumWindows().  should not be NULL or 0-length.");
		Object[] windows = EnumWindows();
		if(!(windows == null)){
			for(int i=0;i< windows.length;i++){
				System.out.print(i+"=="+ windows[i].toString()+", ");
			}
		}else{
			System.out.println("EnumWindows() FAILED with NULL return.");
		}
		System.out.println("\r\n");
		System.out.println("Testing GetDesktopWindow().  should not be NULL.");
		Long desktop = (Long)GetDesktopWindow();
		System.out.println("GetDesktopWindow()=="+ desktop);
		System.out.println("");
		System.out.println("Testing EnumChildWindows().  should not be NULL or 0-length.");
		windows = EnumChildWindows(desktop);
		if(!(windows == null)){
			for(int i=0;i< windows.length;i++){
				System.out.print(i+"=="+ windows[i].toString()+", ");
			}
		}else{
			System.out.println("EnumChildWindows() FAILED with NULL return.");
		}
		System.out.println("\r\n");		
		System.out.println("Testing _processLastError(). Expect 1400: Invalid Window Handle.");
		ids = GetWindowThreadProcessId(new Long(10001));
		int error = _processLastError();
		System.out.println("   _processLastError() returned: "+ error);
		System.out.println("\r\n");
		
		test_getFileTime(args);

		test_getHostIp(args);
		
		
	}
}