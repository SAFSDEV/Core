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
 * History:
 *
 * FEB 05, 2016    (LeiWamg) Added methods to kill processes by Windows command "wmic".
 * MAY 28, 2018    (LeiWamg) Added method getProcess().
 * MAR 28, 2020    (LeiWamg) Supported shutdownProcess() and isProcessRunning() for Linux.
 *
 */
package org.safs.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.net.NetUtilities;
import org.safs.sockets.DebugListener;
import org.safs.tools.consoles.GenericProcessCapture;
import org.safs.tools.consoles.GenericProcessConsole;

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
		String debugmsg = StringUtils.debugmsg(false);

		GenericProcessCapture console = null;
		boolean success = false;
		int exitcode = -1;
		String cmd = null;
		try{
			Process proc = null;
			if(Console.isWindowsOS()){
				cmd = winproclist;
				proc = Runtime.getRuntime().exec(cmd);

			}else if(Console.isUnixOS()){
				//"bash" "-c" "shell_command"
				List<String> cmdList = new ArrayList<String>();
				//TODO, 'bash' or 'sh' 'csh' 'ksh'?
				cmdList.add("bash");
				cmdList.add("-c");
				cmdList.add(unxproclist);
				String[] shellCmd = cmdList.toArray(new String[0]);
				cmd = Arrays.toString(shellCmd);
				proc = Runtime.getRuntime().exec(shellCmd);
			}else{
				IndependantLog.error(debugmsg+Console.getOsFamilyName() +" has NOT been supported yet.");
			}

			IndependantLog.debug(debugmsg+" executing command: "+cmd);

			console = getProcessCapture(proc);
			Thread athread = new Thread(console);
			athread.start();
			athread.join();
			exitcode = console.getExitValue();
			success = (exitcode==0);

		}catch(Exception x){
			// something else was wrong with the underlying process
			IndependantLog.debug(debugmsg+ cmd +", "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		}
		if(success){
			IndependantLog.debug(debugmsg+" success.");
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
				if(success){
					IndependantLog.debug(debugmsg+" found process "+line);
				}
			}
		}else{
			throw new IOException("Failed to run command '"+cmd+"', the exit code is "+exitcode);
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
			condition = key+"='"+value+"' ";
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

	public static class WQLSearchCondition extends SearchCondition{
		//The condition used in command wmic's where clause, such as
		//commandline like '%d:\\seleniumplus\\extra\\chromedriver.exe%' and name = 'chromedriver.exe'
		public WQLSearchCondition(String condition){
			super(condition);
		}

		@Override
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

	public static class UnixProcessSearchCondition extends SearchCondition{
		//the field 'condition' is a string command to get a list of matched processes, such as below:
		//"ps -f -C command | tail -n +2 | grep -i commandline | grep -v notCommandline | awk '{print $2}'"

		/*
		//we use unix grep command to search process
		//-f                   full-format, including command lines
		//-C <command>         command name
		searchCondition = " ps -f -C "+processName;
		//the first line is "UID        PID  PPID  C STIME TTY          TIME CMD"
		//We use the 'tail' command to remove the first heading line so that only process list is left
		searchCondition += " | tail -n +2 ";
		//-i, --ignore-case         ignore case distinctions
		searchCondition += " | grep -i "+commandline;
		//-v, --invert-match        select non-matching lines
		if (notCommandline != null) {
			searchCondition += " | grep -v "+notCommandline;
		}
		//Finally, use the 'awk' to get the process id list
		searchCondition += " | awk '{print $2}' ";
		*/

		public UnixProcessSearchCondition(String condition){
			super(condition);
		}
	}

	public static class SearchCondition{
		protected String condition = null;

		public SearchCondition(String condition){
			this.condition = condition;
		}

		@Override
		public String toString(){
			if(!StringUtils.isValid(condition)){
				IndependantLog.error(StringUtils.debugmsg(false)+" the value "+condition+" is not valid.");
			}
			return condition;
		}
	}

	public static class ProcessInfo{
		private String id = null;
		private int wmiTerminateRC = Integer.MIN_VALUE;
		private Map<String, String> fields = new HashMap<String, String>();

		public ProcessInfo(){}
		public ProcessInfo(Map<String, String> fields){
			this();
			this.fields = fields;
		}

		public String getId() {
			return id;
		}
		public int getWmiTerminateRC() {
			return wmiTerminateRC;
		}

		public String getField(String field){
			return fields.get(field.toLowerCase());
		}
		public String addField(String field, String value){
			return fields.put(field.toLowerCase(), value);
		}

		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();

			if(id!=null){
				sb.append("id="+id+"\n");
			}
			if(wmiTerminateRC!=Integer.MIN_VALUE){
				sb.append("wmiTerminateRC="+wmiTerminateRC+"\n");
			}
			if(fields!=null){
				for(String key:fields.keySet()){
					sb.append(key+"="+fields.get(key)+"\n");
				}
			}
			sb.append("\n");

			return sb.toString();
		}

	}

	/**
	 * Attempt to forcefully kill processes on a certain host according to a search condition.<br>
	 * On Windows we are using wmic.exe. It is only supported on Windows.<br>
	 * On Linux we use the 'ps', 'grep', 'awk' etc. to find matched processes, and use 'kill' to kill process.<br>
	 *
	 * @param host	String, the host name where to kill processes. This is only supprted on Windows.
	 * @param condition	SearchCondition, the search condition to find processes
	 * @return	List<ProcessInfo>, a list of killed processes
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> shutdownProcess(String host, SearchCondition condition) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		GenericProcessCapture console = null;
		boolean success = false;
		String cmd = null;

		if(!(Console.isWindowsOS() || Console.isUnixOS()) ) throw new SAFSException(osFamilyName+" has NOT been supported yet.");

		try{
			Process proc = null;

			if(Console.isWindowsOS()){
				cmd = winwmic;
				//If this host is a remote machine, then add the "/node" option
				if(!(host==null || host.trim().isEmpty() || NetUtilities.isLocalHost(host))){
					cmd += " "+winwmic_nodeoption+host+" ";
				}
				cmd += " "+winwmic_cmdprocess+" ";
				cmd += " where "+condition+" call terminate";
				IndependantLog.debug(debugmsg+" executing command: "+cmd);
				proc = Runtime.getRuntime().exec(cmd);

			}else if(Console.isUnixOS()){
				//"bash" "-c" "shell_command"
				//pids=$(ps -f -C java | tail -n +2  | grep -i selenium-server-standalone | grep -v JUnitTestRunner | awk '{print $2}'); if [ ! -z $pids ]; then kill -9 $pids; fi; echo $pids;
				List<String> cmdList = new ArrayList<String>();
				//TODO, 'bash' or 'sh' 'csh' 'ksh'?
				cmdList.add("bash");
				cmdList.add("-c");
				//assign the matched process id to a variable
				//the variable 'matched_pids' will contain matched PIDs, separated by space
				String bashcmd = "matched_pids=$("+condition+"); ";
				//if the matched process id is not empty, then kill them
				bashcmd += "if [ ! -z \"$matched_pids\" ]; then kill -9 $matched_pids; fi; ";
				//finally print the matched process id to the standard out
				bashcmd += "echo $matched_pids;";
				cmdList.add(bashcmd);
				String[] commands = cmdList.toArray(new String[0]);
				cmd = Arrays.toString(commands);
				IndependantLog.debug(debugmsg+" executing command: "+cmd);
				proc = Runtime.getRuntime().exec(commands);
			}else{
				IndependantLog.error(debugmsg+Console.getOsFamilyName() +" has NOT been supported yet.");
			}

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
			IndependantLog.info(debugmsg+" success!");
			if(Console.isWindowsOS()){
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
			}else if(Console.isUnixOS()){
				String line = null;
				Enumeration<String> reader = console.getData().elements();
				String matchedIds = null;
				if((reader.hasMoreElements())){
					line = reader.nextElement();
					matchedIds = line.substring(GenericProcessCapture.OUT_PREFIX.length()).trim();
				}

				if(matchedIds!=null){
					ProcessInfo processInfo = null;
					String[] matchedIdArray = matchedIds.split(" ");

					for(int i=0; i<matchedIdArray.length;i++){
						processInfo = new ProcessInfo();
						processInfo.id = matchedIdArray[i];
						processList.add(processInfo);
					}
				}

			}else{
				IndependantLog.warn(Console.getOsFamilyName() +" has NOT been supported yet.");
			}

			if(!processList.isEmpty()){
				IndependantLog.debug(debugmsg+" KILLED processes: "+Arrays.toString(processList.toArray(new ProcessInfo[0])));
			}
		}else{
			IndependantLog.info(debugmsg+" failed!");
		}

		return processList;
	}

	/**
	 * Attempt to get processes on a certain host according to a WQL search condition.<br>
	 * On Windows we are using wmic.exe. It is only supported on Windows.<br>
	 *
	 * @param host	String, the host name where to get processes
	 * @param condition	WQLSearchCondition, the WQL search condition to find processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return	List<ProcessInfo>, a list of processes met the condition provided in parameter.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getProcess(String host, SearchCondition condition, List<String> fields) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		GenericProcessCapture console = null;
		boolean success = false;
		String cmd = null;

		if(!isWindowsOS()) throw new SAFSException(osFamilyName+" has NOT been supported yet.");

		//In ProcessInfo, the field is case-insensitive
		List<String> lowerCaseFields = new ArrayList<String>();
		for(String field:fields){
			lowerCaseFields.add(field.toLowerCase());
		}

		try{
			//TODO on Linux, we should use "bash -c command" to execute shell command
			cmd = winwmic;
			//If this host is a remote machine, then add the "/node" option
			if(!(host==null || host.trim().isEmpty() || NetUtilities.isLocalHost(host))){
				cmd += " "+winwmic_nodeoption+host+" ";
			}
			cmd += " "+winwmic_cmdprocess+" ";
			cmd += " where "+condition+" ";

			//Sort the fields
			String getClause = lowerCaseFields.size()>0? " get ":"";
			Collections.sort(lowerCaseFields);
			for(String field:lowerCaseFields){
				getClause += " "+field+",";
			}
			getClause = getClause.substring(0, getClause.length()-1)+" /value ";// the option "/value" can break the a line into lines of "key=value"
			cmd += getClause;

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

		//Analyze the output to store the processes information into a list

//		OUT: CommandLine="c:\seleniumplus\Java64\jre\bin\java.exe"  -Xms512m -Xmx2g -Dwebdriver.chrome.driver="c:\seleniumplus\extra\chromedriver.exe" -Dwebdriver.ie.driver="c:\seleniumplus\extra\IEDriverServer.exe" -Dwebdriver.gecko.driver="c:\seleniumplus\extra\geckodriver_64.exe" -jar "c:\seleniumplus\libs\selenium-server-standalone-3.4.0.jar" -timeout 0 -browserTimeout 0
//		OUT: Name=java.exe
//		OUT: CommandLine=C:\SeleniumPlus\Java\bin\javaw.exe -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:59589 -Dfile.encoding=UTF-8 -classpath "C:\Repository\cvshome\gitlab\SAFSDEV\Core\bin;C:\Repository\cvshome\gitlab\SAFSDEV\SAFS-Android-Remote-Control\bin;C:\Repository\cvshome\git\SAFSBuild\safsjars\safsinstall.jar;C:\Repository\cvshome\git\SAFSBuild\safsjars\safsautoandroid.jar;C:\Repository\cvshome\git\SAFSBuild\safsjars\safssockets.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jna-4.2.2.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jna-platform-4.2.2.jar;C:\Repository\cvshome\git\SAFSBuild\libs\org.json.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\cuke\cucumber-core-1.1.3.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\cuke\cucumber-java-1.1.3.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\cuke\gherkin-2.12.0.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\cuke\sas-gat-cukes.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\android\ddmlib.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\selenium\SeInterpreter.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\staf\JSTAF3.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\staf\JSTAF2.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jai_imageio.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jai_core.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jai_codec.jar;C:\Repository\cvshome\git\SAFSBuild\libs\dom4j-2.0.0-ALPHA-2.jar;C:\Repository\cvshome\git\SAFSBuild\libs\nekohtml.jar;C:\Repository\cvshome\git\SAFSBuild\libs\juniversalchardet-1.0.3.jar;C:\Repository\cvshome\git\SAFSBuild\libs\javax.mail.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\customization\com.ibm.terminal.tester.ft.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\org.eclipse.tptp.platform.models_4.6.205.v201209141700.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.util_8.3.0.v20130102_2345.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.corecomponents_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.autbase_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.playback_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.rational.test.ft.wswplugin_8.3.0.v20130102_2345.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.rational.test.ft.wpf_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.rational.test.ft.siebel_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.rational.test.ft.sap_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.test.terminal.testobjects_8.2.1.v20110715_1630.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.sdk_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.html.sapwebportal.testobjects_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.domain_8.3.0.v20130319_1934.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.domain.testobjects_8.3.0.v20121022_1402.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.domain.html.dojo.testobjects_8.3.0.v20130226_1842.jar;C:\Repository\cvshome\git\SAFSBuild\libs\rft\com.ibm.rational.test.ft.clientbase_8.3.0.v20130117_1954.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\autoit\AutoItX4Java.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\autoit\jacob.jar;C:\Repository\cvshome\git\SAFSBuild\libs\slf4j-api-1.7.21.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\httpclient5\httpclient5-5.0-alpha2-SNAPSHOT.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\httpclient5\httpcore5-5.0-alpha2.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\httpclient5\httpcore5-testing-5.0-alpha2.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\groovy\groovy-all-2.4.7.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\saxon\Saxon-HE-9.7.0-8.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\test\lib\jetty-6.1.14.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\test\lib\jetty-util-6.1.14.jar;C:\Users\Lei Wang\.groovy\greclipse\global_dsld_support;C:\Eclipse\eclipse-SDK-4.6.3-win32\eclipse\plugins\org.codehaus.groovy_2.4.11.xx-201706162032-e46\plugin_dsld_support\;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\spring-web\spring-web-4.3.4.RELEASE.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\spring-core\spring-core-4.3.4.RELEASE.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\log4j\log4j-api-2.8.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\log4j\log4j-core-2.8.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\test\lib\ant-launcher.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\test\lib\ant.jar;C:\Repository\cvshome\git\SAFSBuild\libs\jaxen-1.1.1.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\selenium\selenium-server-standalone-3.4.0.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\selenium\selenium-server-safs-2.52.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\hibernate\hibernate-jpa-2.1-api-1.0.0.Final.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\commons\commons-beanutils-1.9.3.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\aspectj\aspectjweaver.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\spring\spring-beans.jar;C:\Repository\cvshome\gitlab\SAFSDEV\Core\dependencies\spring\spring-context.jar" org.safs.selenium.webdriver.lib.WDLibrary
//		OUT: Name=javaw.exe

		List<ProcessInfo> processList = new ArrayList<ProcessInfo>();
		String line = null;
		ProcessInfo processInfo = null;
		String firstField = lowerCaseFields.get(0);
		int outPrefixIndex = -1;
		int index = -1;
		if(success){
			Enumeration<String> reader = console.getData().elements();
			while((reader.hasMoreElements())){
				line = reader.nextElement();
				outPrefixIndex = line.indexOf(GenericProcessConsole.OUT_PREFIX);
				if(outPrefixIndex>-1){
					line = line.substring(outPrefixIndex+GenericProcessConsole.OUT_PREFIX.length());
				}
				line = line.trim();
				if(line.toLowerCase().startsWith(firstField+StringUtils.EQUAL)){//"CommandLine="
					processInfo = new ProcessInfo(new HashMap<String, String>());
					processList.add(processInfo);
				}
				index = line.indexOf(StringUtils.EQUAL);//"key=value"
				if(index>-1 && processInfo!=null){
					processInfo.addField(line.substring(0, index), line.substring(index+StringUtils.EQUAL.length()));
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
			//TODO on Linux, we should use "bash -c command" to execute shell command
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

	public static void main(String[] args) throws IOException{
		IndependantLog.setDebugListener(new DebugListener(){

			@Override
			public String getListenerName() {
				return null;
			}

			@Override
			public void onReceiveDebug(String message) {
				System.out.println(message);
			}

		});
		if(args.length>0){
			System.out.println("process "+args[0]+" is running "+isProcessRunning(args[0]));
		}
	}
}
