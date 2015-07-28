/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntTool extends ConsoleTool{
	private static AntTool tool = null;

	//public static final String ANT_TOOL_HOME 		= "D:\\ant182";
	/** "ant-home" as in VM arg "-Dant-home=..." */
	public static final String ANT_HOME_PROP 	= "ant-home";
	/** "ANT_HOME" */
	public static final String ANT_HOME_ENV 	= "ANT_HOME";
	
	private AntTool(){
		super();
	}

	/**
	 * TODO Do we need to keep this tool as a singleton? 
	 * If different users call {@link #setToolHome(String)} to set different tool-path, problem!!!<br>
	 * But on one machine, it is rarely that user has different path for one tool. Different version?<br>
	 * @return
	 */
	public static synchronized AntTool instance(){
		if(tool==null){
			tool = new AntTool();
		}
		return tool;
	}
	
	/** "bin" subdirectory Ant home */
	public static final String ANT_TOOLS_DIR = "bin";

	/** called internally to set the List (1) of tools directories to search for the Ant tool. 
	 * @see #ANT_TOOLS_DIR */
	protected void modifyBinDirectories() {
		binDirectories.clear();
		binDirectories.add(ANT_TOOLS_DIR);
	}

	/**
	 * called internally to get the list (1) VM argument name to search for the
	 * Ant tool home directory.
	 * @see #ANT_HOME_PROP
	 */
	protected List<String> getToolHomeProperties(){
		List<String> properties = new ArrayList<String>();
		properties.add(ANT_HOME_PROP);
		return properties;
	}
	
	/**
	 * called internally to get the list (1) of environment variable names to search for 
	 * the Ant tool home directory.
	 * @see #ANT_HOME_ENV
	 */
	protected List<String> getToolHomeEnvs(){
		List<String> envs = new ArrayList<String>();
		envs.add(ANT_HOME_ENV);
		return envs;	
	}
	
	/** "ant.bat" */
	public static final String ANT_TOOL_WIN = "ant.bat";
	/** "ant" */
	public static final String ANT_TOOL_UNX = "ant";
	
	public Process2 ant(File workingDirectory, String... args) throws IOException{
		String executable = ANT_TOOL_WIN;
		
		if (isWindowsOS()) {
			executable = ANT_TOOL_WIN;
		} else if (isUnixOS()) {
			executable = ANT_TOOL_UNX;
		} else {
			throw new UnsupportedOperationException("Don't know how to start ant on " + getOsFamilyName());
		}
		
		List<String> batchAndArgs = new ArrayList<String>();
		batchAndArgs.add(executable);
		batchAndArgs.addAll(Arrays.asList(args));

		return tool.batch(workingDirectory, batchAndArgs);
	}
	

}
