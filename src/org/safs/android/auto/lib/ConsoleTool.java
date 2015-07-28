/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.safs.IndependantLog;

public abstract class ConsoleTool {
	
	protected Console console = null;
	/**
	 * contains possible absolute path directory.<br>
	 * contains possible sub-directories from tool-home where the tool stays<br>
	 * For example, bin, tools, platform-tools etc.<br>
	 */
	protected final List<String> binDirectories = new ArrayList<String>();
	
	/**
	 * The absolute path of the tool's home
	 */
	private String toolHome = null;

	public ConsoleTool(){
		console = Console.get();
		modifyBinDirectories();
	}
	
	public List<String> getBinDirectories() {
		return binDirectories;
	}
	
	/**
	 * This method will modify the {@link #binDirectories} according to tool itself.<br>
	 * @see binDirectories
	 */
	protected abstract void modifyBinDirectories();
	
	public static String getOsFamilyName(){
		return Console.getOsFamilyName();
	}
	
	public static boolean isWindowsOS(){
		return Console.isWindowsOS();
	}
	public static boolean isUnixOS(){
		return Console.isUnixOS();
	}
	public static boolean isMacOS(){
		return Console.isMacOS();
	}
	
	/**
	 * Try to find the location of the executable tools in the sdk's home path.
	 * It will try to look the tool in some sub directories, such as bin, tools
	 * If still not found, throw the IllegalStateException.
	 * 
	 * @param tool name like "adb.exe", "batch.bat" or "script.sh" etc...
	 * @return String absolute path to the tool or throws the exception
	 * @throws IllegalStateException if tool cannot be found.
	 */
	protected String locateTool(String tool) {
		String path = null;
		String toolHome = getToolHome();

		File directory = null;
		for(String bin: binDirectories){
			directory = new File(toolHome, bin);
			if(directory==null || !directory.isDirectory()){
				directory = new File(bin);//try bin as absolute path
				if(directory==null || !directory.isDirectory()) continue;
			}
			path = searchFile(directory, tool, needSearchRecursively(bin));
			if(path!=null){
				//we found the tool, just get out of the loop
				break;
			}
		}

		if (path == null) {
			String errmsg = null;
			if(toolHome!=null){
				errmsg = "Can't find command '" + tool + "' inside the sdk at " + toolHome;
				throw new IllegalStateException(errmsg);
			}else{
				IndependantLog.warn("Can't find command '" + tool + "'. Try it as DOS internal command.");
				path = tool;
			}
		}
		
		return path;
	}
	/**
	 * Try to find a file matching 'filename' within directory.
	 * 
	 * @param directory		File, the directory in which to search file
	 * @param filename		String, the filename to match
	 * @param recusive		boolean, if we need to search files in sub-directories
	 * @return	String, the absolute filename or null if not found.
	 * @see #locateTool(String)
	 */
	protected String searchFile(File directory, String filename, boolean recusive){
		String path = null;
		
		for (File file : directory.listFiles()) {
			if(recusive && file.isDirectory()){
				path = searchFile(file, filename, recusive);
				if(path!=null) return path;
			}
			if (!file.getName().equalsIgnoreCase(filename)) continue;
			if(!file.canExecute()) continue;
			path = file.getAbsolutePath();
			break;
		}	
		
		return path;
	}
	
	/**
	 * @param directoryName String, the directory to be searched
	 * @return	boolean, true if we need to search in the sub-directories
	 * @see #locateTool(String)
	 */
	protected boolean needSearchRecursively(String directoryName){
		return false;
	}
	
	/**
	 * Only you are sure about the tool's home, you can set it.<br>
	 * Because if you set it, {@link #getToolHome()} will not try to get the tool's home<br>
	 * from "JVM properties" or "System environment variables"<br>
	 * This method will verify if the toolHome exists and is a directory. If not, it will<br>
	 * throw out IllegalStateException. 
	 * 
	 * @param toolHome
	 * @throws IllegalStateException
	 * 
	 * @see #getToolHome()
	 */
	public void setToolHome(String toolHome) {
		if(toolHome==null){
			throw new IllegalStateException("toolHome is null, can't set it.");
		}
		File toolHomeFile = new File(toolHome);
		if (!toolHomeFile.exists()) {
			throw new IllegalStateException("" + toolHome + " doesn't exist.");
		}

		if (!toolHomeFile.isDirectory()) {
			throw new IllegalStateException("" + toolHome + " isn't a directory.");			
		}
		
		this.toolHome = toolHome;
	}
	
	/**
	 * This method will firstly try to return the field {@link #toolHome}<br>
	 * If it is null, it will try to get the toolHome from properties {@link #getToolHomeProperties()}<br>
	 * If still not found, it will try to get the toolHome from environments {@link #getToolHomeEnvs()}<br>
	 * If still not found, it will throw out IllegalStateException.<br>
	 * If the toolHome if found, it will be set to field {@link #toolHome}.<br>
	 * 
	 * @return	The tool home where (or sub-directory) the tools' executable can be found.
	 * @throws IllegalStateException
	 * 
	 * @see #setToolHome(String)
	 */
	public String getToolHome() {
		String toolHome = this.toolHome;
		
		if(toolHome!=null){
			return toolHome;
		}else{
			for(String property: getToolHomeProperties()){
				toolHome = System.getProperty(property);
				if(toolHome!=null) break;
			}
		}
		
		if (toolHome == null){
			for(String env: getToolHomeEnvs()){
				toolHome = System.getenv(env);
				if(toolHome!=null) break;
			}
		}
				
		if (toolHome == null) {
			String errorMsg = "Can't find the tool sdk home.  ";
			if(getToolHomeProperties().size()>0){
				errorMsg += "Set the " + getListString(getToolHomeProperties()) + " system property to your sdk root.";
			}
			if(getToolHomeEnvs().size()>0){
				errorMsg += "Set the " + getListString(getToolHomeEnvs()) + " environment variables to your sdk root.";
			}
			throw new IllegalStateException(errorMsg);
		}
		
		setToolHome(toolHome);
		
		return toolHome;
	}
	
	/**
	 * Subclass should override this method to provide its own system properties.<br>
	 * @return	A list of 'system property name', where the 'tool home' may be stored.
	 */
	protected List<String> getToolHomeProperties(){
		return new ArrayList<String>();
	}
	
	/**
	 * Subclass should override this method to provide its own environment variables.<br>
	 * @return	A list of 'environment variable name', where the 'tool home' may be stored.
	 */
	protected List<String> getToolHomeEnvs(){
		return new ArrayList<String>();		
	}
	
	/**
	 * Convert a list of string to a comma-separated string
	 * @param list	List of String
	 * @return	a comma-separated string
	 */
	private String getListString(List<String> list){
		String string = "";
		for(String value: list){
			string += value+", ";
		}
		//remove the last comma
		int index = string.lastIndexOf(",");
		if(index>-1){
			string = string.substring(0, index);
		}
		return string;
	}
	
	public String getLastCommand(){return console.getLastCommand();}
	
	/**
	 * Execute an executable. For .bat batch, please call batch() command instead.<br>
	 * @param tool, String, the executable (.exe)
	 * @param args, String, the parameters of the executable.
	 * @return
	 * @throws IOException
	 */
	public Process2 exec(String tool, String... args) throws IOException {	
		return exec(tool, Console.asList(args));
	}
	public Process2 exec(String tool, List<String> args) throws IOException {
		return console.start(locateTool(tool), args);
	}
	public Process2 exec(File workingDirectory, String... binaryAndArgs) throws IOException {
		return exec(workingDirectory, Console.asList(binaryAndArgs));
	}
	public Process2 exec(File workingDirectory, List<String> binaryAndArgs) throws IOException {
		if(binaryAndArgs.size()>0){
			String tool = locateTool(binaryAndArgs.get(0));
			binaryAndArgs.set(0, tool);
		}
		return console.start(workingDirectory, binaryAndArgs);
	}
	
	/**
	 * Execute a batch file. For .exe executable, please call exec() command instead.<br>
	 * @param tool, String, the batch file (.bat)
	 * @param args, String, the parameters for the batch file
	 * @return
	 * @throws IOException
	 */
	public Process2 batch(String tool, String... args) throws IOException {
		List<String> batchAndArgs = Console.asList(args);
		batchAndArgs.add(0, tool);
		return batch(null, batchAndArgs);
	}
	public Process2 batch(File workingDirectory, String... batchAndArgs) throws IOException{
		return batch(workingDirectory, Console.asList(batchAndArgs));
	}
	public Process2 batch(File workingDirectory, List<String> batchAndArgs) throws IOException{
		if(batchAndArgs.size()>0){
			String tool = locateTool(batchAndArgs.get(0));
			batchAndArgs.set(0, tool);
		}
		return console.batch(workingDirectory, batchAndArgs);
	}
	
}
