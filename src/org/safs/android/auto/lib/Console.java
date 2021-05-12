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
/*************************************************************************************************************************
 * History:
 * Lei Wang JAN 22, 2019 Added code to detect OS architecture (32 bit or 64 bit).
 *
 */
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.safs.IndependantLog;
import org.safs.tools.consoles.ProcessCapture;

public abstract class Console {
	/** The value of property "os.name"*/
	public static final String OS_NAME 	= System.getProperty("os.name").toLowerCase(Locale.US);
	/** The value of property "path.separator"*/
	public static final String PATH_SEP = System.getProperty("path.separator");
	/** The value of property "line.separator"*/
	public static final String EOL		= System.getProperty("line.separator");
	/** The value of property "file.separator"*/
	public static final String FILE_SEP = System.getProperty("file.separator");

	public static final String OS_64_BIT 	= "64 bit";
	public static final String OS_32_BIT 	= "32 bit";

	/**A Hash Map containing pair <OSFamilyName, ConsoleObject> */
	private static ConcurrentHashMap<String, Console> consoles = new ConcurrentHashMap<String, Console>();
	/** The operating system family name, deduced from os name {@link #OS_NAME}*/
	private static String osFamilyName = null;
	/** To tell us if the OS is 32 bit or 64 bit */
	private static String osArchitecture = OS_32_BIT;

	private static String path = null;
	private static String classpath = null;

	static final String OS_FAMILY_SYS_PROP 	= "os-family";
	static final String ENV_PATH 			= "PATH";
	static final String ENV_CLASSPATH 		= "CLASSPATH";

	private String lastCommand = null;

	static{
		osFamilyName = System.getProperty(OS_FAMILY_SYS_PROP);
		try{ if (osFamilyName == null) osFamilyName = deduceOSFamily(); }
		catch(IllegalStateException e){ System.err.println(e.getClass().getSimpleName()+":"+e.getMessage());}

		String command = "wmic os get osarchitecture";
		if(isWindowsOS()){
			//wmic os get osarchitecture
			command = "wmic os get osarchitecture";
		}else{
			//Linux/Unix or Mac
			command = "uname -m";
		}
		Process process;
		try {
			process = Runtime.getRuntime().exec(command);
			ProcessCapture console = new ProcessCapture(process);
			Thread athread = new Thread(console);
			athread.start();
			//we can wait until process is finished
			try{ athread.join();}catch(InterruptedException x){;}
			console.shutdown();//precaution
			int exitcode = console.getExitValue();
			if(exitcode!=0){
				Vector data = console.getExceptions();
				IndependantLog.debug("Failed to execute command '"+command+"'\n"+"exitcode="+exitcode+"\nErrors:\n"+data.toString());
			}else{
				Vector data = console.getData();
				for(Object line: data){
					if(line.toString().contains("x86_64") || line.toString().contains("64-bit")){
						//"x86_64" Linux
						//"64-bit" Windows 10
						osArchitecture = OS_64_BIT;
						break;
					}
				}
			}

		} catch (IOException e) {
			IndependantLog.error("Met "+e.toString());
		}

		try{ path = System.getenv(ENV_PATH); }catch(Exception e){ }
		try{ classpath = System.getenv(ENV_CLASSPATH); }catch(Exception e){ }
	}

	public static Console get() {
		return forOsFamily(osFamilyName);
	}

	public static Console forOsFamily(String osFamily) {
		if(osFamily==null) throw new UnsupportedOperationException("OS is null, Can't start console tools.");

		Console instance = consoles.get(osFamily);

		if (instance == null) {
			Console newInstance = null;
			if (osFamily.equals(WindowsConsole.OS_FAMILY_NAME)) {
				newInstance = new WindowsConsole();
			} else if (osFamily.equals(UnixConsole.OS_FAMILY_NAME)) {
				newInstance = new UnixConsole();
			} else if (osFamily.equals(MacConsole.OS_FAMILY_NAME)) {
				newInstance = new MacConsole();
			} else {
				throw new UnsupportedOperationException("Don't know how to start console tools on " + osFamily);
			}

			instance = consoles.putIfAbsent(osFamily, newInstance);
			if (instance == null) instance = newInstance;
		}

		return instance;
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
		if (OS_NAME.indexOf(WindowsConsole.OS_FAMILY_NAME) > -1) {
			return WindowsConsole.OS_FAMILY_NAME;

		} else if (PATH_SEP.equals(":") &&
				   OS_NAME.indexOf("openvms") == -1 &&
				   (OS_NAME.indexOf("mac") == -1 || OS_NAME.endsWith("x"))) {
			return UnixConsole.OS_FAMILY_NAME;

		} else if ((OS_NAME.indexOf("mac") > -1)) {
			return MacConsole.OS_FAMILY_NAME;

		}else {
			throw new IllegalStateException(
					"Can't infer your OS family.  Please set the " + OS_FAMILY_SYS_PROP
							+ " system property to one of 'windows', 'unix'.");
		}
	}

	/**
	 * @return String, the operating system family name, deduced from os name {@link #OS_NAME}<br>
	 */
	public static String getOsFamilyName() {
		return osFamilyName;
	}

	public static String getOsArchitecture() {
		return osArchitecture;
	}

	public static boolean is64BitOS(){
		return OS_64_BIT.equalsIgnoreCase(osArchitecture);
	}

	public static boolean isWindowsOS(){
		return WindowsConsole.OS_FAMILY_NAME.equals(getOsFamilyName());
	}
	public static boolean isUnixOS(){
		return UnixConsole.OS_FAMILY_NAME.equals(getOsFamilyName());
	}
	public static boolean isMacOS(){
		return MacConsole.OS_FAMILY_NAME.equals(getOsFamilyName());
	}

	/**
	 * This method suppose you have verified the binary can be found on you system.<br>
	 * The console command will be executed in working directory of the current Java process,<br>
	 * usually the directory named by the system property <code>user.dir</code><br>
	 *
	 * @param binaryAndArgs	List of strings, including binary and arguments, it is console command.
	 * @return	Process2, a process which is in charge of running the console command.
	 * @throws IOException
	 */
	public Process2 start(List<String> binaryAndArgs) throws IOException {
		return start((File)null, binaryAndArgs);
	}

	/**
	 * This method suppose you have verified the binary can be found on you system.<br>
	 *
	 * @param workingDirectory	The working directory where the console command will be executed.
	 * @param binaryAndArgs		List of strings, including binary and arguments, it is console command.
	 * @return	Process2, a process which is in charge of running the console command.
	 * @throws IOException
	 */
	public Process2 start(File workingDirectory, List<String> binaryAndArgs) throws IOException {
		StringBuffer sb = new StringBuffer();
		ProcessBuilder pb = new ProcessBuilder();
		pb.command().addAll(binaryAndArgs);
		if(workingDirectory!=null && workingDirectory.isDirectory()){
			pb.directory(workingDirectory);
			sb.append(workingDirectory.getCanonicalPath()+"$");
		}

		for(String item: binaryAndArgs) sb.append(item+" ");
		lastCommand = sb.toString();
		IndependantLog.debug(lastCommand);

		return new Process2(pb.start());
	}

	public String getLastCommand(){return lastCommand;}

	public Process2 start(String binary, List<String> args) throws IOException {
		return start(null, binary, args);
	}

	public Process2 start(File workingDirectory, String binary, List<String> args) throws IOException {
		List<String> binaryAndArgs = new ArrayList<String>();
		binaryAndArgs.add(binary);
		for(String arg: args) binaryAndArgs.add(arg);
		return start(workingDirectory, binaryAndArgs);
	}

	/**
	 * subclass should override this method to provide its own implementation.
	 * @param workingDirectory
	 * @param batchAndArgs
	 * @return
	 * @throws IOException
	 */
	public Process2 batch(File workingDirectory, List<String> batchAndArgs) throws IOException{
		return start(workingDirectory, batchAndArgs);
	}

	public Process2 batch(File workingDirectory, String... batchAndArgs) throws IOException {
		return batch(workingDirectory, Console.asList(batchAndArgs));
	}

	public static List<String> asList(String... strings){
		ArrayList<String> stringArray = new ArrayList<String>();
		for(int i=0;i<strings.length;i++){
			stringArray.add(strings[i]);
		}
		return stringArray;
	}

	/**
	 * Delete a directory recursively by console command provided by Operating System.<br>
	 * @param directoryFullPath, String, the full path of the directory to delete
	 * @return boolean, true if the directory has been deleted successfully.
	 */
	public boolean deleteDirectory(String directoryFullPath){
		Process2 p = null;
		try {
			String command = getRecursiveDeleteCommand();
			if(command==null || command.isEmpty()) throw new NullPointerException("Recursive Delete Command is null.");
			p = batch(null, command, directoryFullPath).forwardIO().waitForSuccess();
		} catch (Exception ignore) {
			System.err.println(ignore.getClass().getSimpleName()+":"+ignore.getMessage());
			return false;
		}finally{
			if(p!=null) p.destroy();
		}
		return true;
	}

	public static String getPath(){
		if(path==null) try{ path = System.getenv(ENV_PATH); }catch(Exception e){ }
		return path;
	}
	public static String getClassPath(){
		if(classpath==null) try{ classpath = System.getenv(ENV_CLASSPATH); }catch(Exception e){ }
		return classpath;
	}

	/**
	 * @return String, the console command to delete a directory recursively.<br>
	 * 	               For example, "rmdir /S/Q" for Windows System.<br>
	 * @see #deleteDirectory(String)
	 */
	public abstract String getRecursiveDeleteCommand();

	protected String quote(String unquoted) {
		return "\"" + unquoted + "\"";
	}

	public static void main(String[] args){
		String[] kkk = {"a","b"};

		System.out.println("OS Family: "+osFamilyName);
		System.out.println("OS Architecture: "+osArchitecture);

		//Arrays.asList(): the returned list doesn't permit adding new element!!!
		List<String> list = Arrays.asList(kkk);
		list.set(0, "mma");

		System.out.println(list);
		try{
			list.add(0, "newEle");
			System.out.println(list);
		}catch(Exception e){
			System.err.println("can't insert...");
		}

		list = Console.asList(kkk);
		list.set(0, "mma");

		System.out.println(list);
		try{
			list.add(0, "newEle");
			System.out.println(list);
		}catch(Exception e){
			System.err.println("can't insert...");
		}

		Properties p = System.getProperties();
		Enumeration keys = p.keys();
		String key = null;
		while(keys.hasMoreElements()){
			key = (String) keys.nextElement();
			System.out.println(key+"="+p.getProperty(key));
		}
		System.out.println("PATH="+System.getenv("PATH"));

	}
}
