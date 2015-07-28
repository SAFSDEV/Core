/**
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidTools extends ConsoleTool{
	private static AndroidTools tool = null;
	
	/** "ANDROID_HOME" */
	public static final String ANDROID_HOME_ENV_VAR = "ANDROID_HOME";
	/** "ANDROID_SDK" */
	public static final String ANDROID_SDK_ENV_VAR = "ANDROID_SDK";
	
	/** "android-home" as in VM argument "-Dandroid-home=..." */
	public static final String ANDROID_HOME_SYS_PROP = "android-home";

	private AndroidTools(){super();}
	
	/**
	 * TODO Do we need to keep this tool as a singleton? 
	 * If different users call {@link #setToolHome(String)} to set different tool-path, problem!!!<br>
	 * But on one machine, it is rarely that user has different path for one tool. Different version?<br>
	 * @return
	 */
	public static synchronized AndroidTools get(){
		if(tool==null){
			tool = new AndroidTools();
		}
		return tool;
	}
	
	/** "tools" subdirectory in Android SDK */
	public static final String ANDROID_SDK_TOOLS_DIR = "tools";
	/** "platform-tools" subdirectory in Android SDK */
	public static final String ANDROID_SDK_PLATFORM_TOOLS_DIR = "platform-tools";
	/** "build-tools" subdirectory in Android SDK */
	public static final String ANDROID_SDK_BUILD_TOOLS_DIR = "build-tools";
	/** "platform" subdirectory in Android SDK */
	public static final String ANDROID_SDK_PLATFORM_DIR = "platforms";
	
	/** called internally to set the List of tools directories to search for tools. 
	 * @see #ANDROID_SDK_OLD_TOOLS_DIR
	 * @see AndroidTools#ANDROID_SDK_NEW_TOOLS_DIR */
	protected void modifyBinDirectories() {
		binDirectories.clear();
		binDirectories.add(ANDROID_SDK_TOOLS_DIR);
		binDirectories.add(ANDROID_SDK_PLATFORM_TOOLS_DIR);
		binDirectories.add(ANDROID_SDK_BUILD_TOOLS_DIR);
	}

	/**
	 * called internally to get the list (1) VM argument name to search for the
	 * Android SDK home directory.
	 * @see #ANDROID_HOME_SYS_PROP
	 */
	protected List<String> getToolHomeProperties(){
		List<String> properties = new ArrayList<String>();
		properties.add(ANDROID_HOME_SYS_PROP);
		return properties;
	}
	
	/**
	 * called internally to get the list of environment variable names to search for 
	 * the Android SDK home directory.
	 * @see #ANDROID_HOME_ENV_VAR
	 * @see #ANDROID_SDK_ENV_VAR
	 */
	protected List<String> getToolHomeEnvs(){
		List<String> envs = new ArrayList<String>();
		envs.add(ANDROID_HOME_ENV_VAR);
		envs.add(ANDROID_SDK_ENV_VAR);
		return envs;	
	}
	
	/**
	 * If the directory is {@value #ANDROID_SDK_BUILD_TOOLS_DIR}, needs to 
	 * search in the sub-directories. Because Android will store the executables
	 * in the version-sub-folder something like '17.0.0'
	 */
	protected boolean needSearchRecursively(String directoryName){
		return ANDROID_SDK_BUILD_TOOLS_DIR.equals(directoryName);
	}
	
	/**
	 * If you are sure about the tool's home, you can set it.<br>
	 * Because if you set it, {@link #getToolHome()} will not try to get the tool's home<br>
	 * from "JVM properties" or "System environment variables"<br>
	 * This method is deprecated, please use {@link #setToolHome(String)} instead.<br>
	 * 
	 * @param toolHome
	 * @see #getToolHome()
	 * @see #setToolHome(String)
	 * @deprecated
	 */
	public void setAndroidHome(String androidHome){
		setToolHome(androidHome);
	}
	
	/** "aapt.exe" */
	public static final String ANDROID_SDK_AAPT_TOOL_WIN = "aapt.exe";
	/** "aapt" */
	public static final String ANDROID_SDK_AAPT_TOOL_UNX = "aapt";

	public Process2 aapt(String... args) throws IOException{
		if(isWindowsOS()){
			return exec(ANDROID_SDK_AAPT_TOOL_WIN, args);			
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_AAPT_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start aapt on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 aapt(List<String> args) throws IOException {
		return aapt(args.toArray(new String [0]));
	}
	
	/** "aidl.exe" */
	public static final String ANDROID_SDK_AIDL_TOOL_WIN = "aidl.exe";
	/** "aidl" */
	public static final String ANDROID_SDK_AIDL_TOOL_UNX = "aidl";

	public Process2 aidl(String... args) throws IOException {
		if(isWindowsOS()){
			return exec(ANDROID_SDK_AIDL_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_AIDL_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start aidl on " + tool.getOsFamilyName());
		}
	}

	public Process2 aidl(List<String> args) throws IOException {
		return aidl(args.toArray(new String [0]));
	}
	
	/** "apkbuilder.bat" */
	public static final String ANDROID_SDK_APKBUILDER_TOOL_WIN = "apkbuilder.bat";
	/** "apkbuilder" */
	public static final String ANDROID_SDK_APKBUILDER_TOOL_UNX = "apkbuilder";

	public Process2 apkBuilder(String... args) throws IOException {
		if(isWindowsOS()){
			return batch(ANDROID_SDK_APKBUILDER_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_APKBUILDER_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start apkbuilder on " + tool.getOsFamilyName());
		}
	}
	public Process2 apkBuilder(List<String> args) throws IOException {
		return apkBuilder(args.toArray(new String [0]));
	}
	
	/** "adb.exe" */
	public static final String ANDROID_SDK_ADB_TOOL_WIN = "adb.exe";
	/** "adb" */
	public static final String ANDROID_SDK_ADB_TOOL_UNX = "adb";

	public Process2 adb(String... args) throws IOException {
		if(isWindowsOS()){
			return exec(ANDROID_SDK_ADB_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_ADB_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start adb on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 adb(List<String> args) throws IOException {
		return adb(args.toArray(new String [0]));
	}
	
	/** "ddms.bat" */
	public static final String ANDROID_SDK_DDMS_TOOL_WIN = "ddms.bat";
	/** "ddms" */
	public static final String ANDROID_SDK_DDMS_TOOL_UNX = "ddms";

	public Process2 ddms(String... args) throws IOException {
		if(isWindowsOS()){
			return batch(ANDROID_SDK_DDMS_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_DDMS_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start ddms on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 ddms(List<String> args) throws IOException {
		return ddms(args.toArray(new String [0]));
	}
	
	/** "dmtracedump.exe" */
	public static final String ANDROID_SDK_DMTRACEDUMP_TOOL_WIN = "dmtracedump.exe";
	/** "dmtracedump" */
	public static final String ANDROID_SDK_DMTRACEDUMP_TOOL_UNX = "dmtracedump";

	public Process2 dmtracedump(String... args) throws IOException {
		if(isWindowsOS()){
			return exec(ANDROID_SDK_DMTRACEDUMP_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_DMTRACEDUMP_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start dmtracedump on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 dmtracedump(List<String> args) throws IOException {
		return dmtracedump(args.toArray(new String [0]));
	}
	
	/** "dx.bat" */
	public static final String ANDROID_SDK_DX_TOOL_WIN = "dx.bat";
	/** "dx" */
	public static final String ANDROID_SDK_DX_TOOL_UNX = "dx";

	public Process2 dx(String... args) throws IOException {
		if(isWindowsOS()){
			return batch(ANDROID_SDK_DX_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_DX_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start dx on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 dx(List<String> args) throws IOException {
		return dx(args.toArray(new String [0]));
	}
	
	/** "emulator.exe" */
	public static final String ANDROID_SDK_EMULATOR_TOOL_WIN = "emulator.exe";
	/** "emulator" */
	public static final String ANDROID_SDK_EMULATOR_TOOL_UNX = "emulator";

	public Process2 emulator(String... args) throws IOException {
		Process2 proc = null;
		if(isWindowsOS()){
			proc = exec(ANDROID_SDK_EMULATOR_TOOL_WIN, args);
			// allow 2nd (hidden) emulator-arm.exe process time to get started
			try{Thread.sleep(5000);}catch(Exception x){}
			return proc;		
		}else if(isUnixOS()){
			proc = exec(ANDROID_SDK_EMULATOR_TOOL_UNX, args);
			try{Thread.sleep(5000);}catch(Exception x){}
			return proc;
		}else{
			throw new UnsupportedOperationException("Don't know how to start emulator on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 emulator(List<String> args) throws IOException {
		return emulator(args.toArray(new String [0]));
	}
	
	/** "mksdcard.exe" */
	public static final String ANDROID_SDK_MKSDCARD_TOOL_WIN = "mksdcard.exe";
	/** "mksdcard" */
	public static final String ANDROID_SDK_MKSDCARD_TOOL_UNX = "mksdcard";

	public Process2 mksdcard(String... args) throws IOException {
		if(isWindowsOS()){
			return exec(ANDROID_SDK_MKSDCARD_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_MKSDCARD_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start mksdcard on " + tool.getOsFamilyName());
		}
	}
	public Process2 mksdcard(List<String> args) throws IOException {
		return mksdcard(args.toArray(new String [0]));
	}
	
	/** "sqlite3.exe" */
	public static final String ANDROID_SDK_SQLITE3_TOOL_WIN = "sqlite3.exe";
	/** "sqlite3" */
	public static final String ANDROID_SDK_SQLITE3_TOOL_UNX = "sqlite3";

	public Process2 sqlite3(String... args) throws IOException {
		if(isWindowsOS()){
			return exec(ANDROID_SDK_SQLITE3_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_SQLITE3_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start sqlite3 on " + tool.getOsFamilyName());
		}
	}	
	
	public Process2 sqlite3(List<String> args) throws IOException {
		return sqlite3(args.toArray(new String [0]));
	}
	
	/** "traceview.bat" */
	public static final String ANDROID_SDK_TRACEVIEW_TOOL_WIN = "traceview.bat";
	/** "traceview" */
	public static final String ANDROID_SDK_TRACEVIEW_TOOL_UNX = "traceview";

	public Process2 traceview(String... args) throws IOException {
		if(isWindowsOS()){
			return batch(ANDROID_SDK_TRACEVIEW_TOOL_WIN, args);
		}else if(isUnixOS()){
			return exec(ANDROID_SDK_TRACEVIEW_TOOL_UNX, args);
		}else{
			throw new UnsupportedOperationException("Don't know how to start traceview on " + tool.getOsFamilyName());
		}
	}
	
	public Process2 traceview(List<String> args) throws IOException {
		return traceview(args.toArray(new String [0]));
	}
	
	public List<Integer> getInstalledSKDLevel(){
		List<Integer> levels = new ArrayList<Integer>();
		String toolHome = getToolHome();
		String subdirPrefix = "android-";

		File directory = new File(toolHome, ANDROID_SDK_PLATFORM_DIR);
		if(directory.exists() && directory.isDirectory()){
			int level = 0;
			String childName = null;
			File[] children = directory.listFiles();
			for(File child: children){
				childName = child.getName();
				if(child.isDirectory() && childName.startsWith(subdirPrefix)){
					try{
						level = Integer.parseInt(childName.substring(subdirPrefix.length()));
						levels.add(level);
					}catch(NumberFormatException ignore){}
				}
			}
		}
		
		return levels;
	}
}
