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
package org.safs.tools.engines;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.safs.Log;
import org.safs.TestRecordHelper;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.ProcessConsole;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.stringutils.StringUtilities;

/**
 * A wrapper to Apple's iOS Developer UIAutomation SAFS engine--the "IOS" engine.
 * This engine can only be used if you have a valid install of the iOS/XCode Developer
 * SDK tools on an Apple Mac(or equivalent) computer.<p>This is intended for testing of
 * iPhone and iPad iOS applications.
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any
 * command-line options to configure the Engine.  All configuration information must
 * be provided in config files.  By default, these are SAFSTID.INI files.  See
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * The IOS supported config file items are listed below.  Remember, this engine executes on a Mac,
 * so all file paths are in Unix/Mac format:
 * <p>
 * <ul><pre>
 * <b>[SAFS_IOS]</b>
 * AUTOLAUNCH=FALSE                     (Defaults to FALSE because config info must be valid.)
 * Project=/Library/SAFS/samples/UICatalog/  (IOS Automation Project Location)
 * Template="UICatalogInstruments.tracetemplate"   (Instruments tracetemplate for Application)
 * ;HOOK=org.safs.ios.JavaHook          (Java SAFS Engine Class)
 * ;TIMEOUT=30                          (Alternate AutoLaunch timeout value in seconds)
 * ;STAFID="SAFS/IOS"                   (Normally never used.)
 * ;JVM=JVMpath                         (Full path to desired Java executable if "java" is not sufficient.)
 * ;JVMARGS=JVM Args                    (Ex: "-Xms512m -Xmx512m", will be used unmodified.)
 * ;CLASSPATH=altClasspath              (Generally overrides system classpath.)
 * ;XBOOTCLASSPATH=&lt;CLASSPATH>       (CLASSPATH needed for rare cases. Normally never used.)
 * ;ConvertSAFSInputKeysSyntax=ON|OFF   (ON to use SAFS InputKeys syntax in Test Complete)
 * </pre>
 * <p>
 * We do use a ProcessConsole to keep the Process in, out, and err streams from filling up.
 * <p>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Runtime.html#exec(java.lang.String)">Runtime.exec</a>}<br>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Process.html">Runtime.exec Process</a>}<br>
 *
 * <br/>@author Carl Nagle JUN 30, 2011 Original Release
 * @see org.safs.tools.consoles.ProcessConsole
 */
public class SAFSIOS extends GenericEngine {

	/**
	 * "SAFS/IOS" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME = "SAFS/IOS";
	/** '1.0' */ //iOS Developer UIAutomation
	public static final String ENGINE_VERSION = "1.0";
	/** 'The engine using IOS UIAutomation to test IOS mobile UI.' */
	public static final String ENGINE_DESCRIPTION = "The engine using IOS UIAutomation to test IOS mobile UI.";

	/**
	 * "org.safs.ios.JavaHook"
	 */
	static final String HOOK_CLASS       = "org.safs.ios.IJavaHook";

	static final String AUTOLAUNCH_OPTION      = "AUTOLAUNCH";
	static final String HOOK_OPTION            = "HOOK";
	static final String PROJECT_OPTION         = "PROJECT";
	static final String TEMPLATE_OPTION        = "TEMPLATE";
	static final String JVM_OPTION             = "JVM";
	static final String JVMARGS_OPTION         = "JVMARGS";
	static final String XBOOTCLASSPATH_OPTION  = "XBOOTCLASSPATH";
	static final String TIMEOUT_OPTION         = "TIMEOUT";
	static final int DEFAULT_INIT_TIMEOUT      = 30;

	/**
	 * Constructor for IOS.  Call launchInterface with an appropriate DriverInterface
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSIOS() {
		super();
		servicename = ENGINE_NAME;
		productName = ENGINE_NAME;
		version = ENGINE_VERSION;
		description = ENGINE_DESCRIPTION;
	}

	/**
	 * PREFERRED Constructor for SAFSIOS.
	 */
	public SAFSIOS(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches IOS initialization in a new process.
	 *
	 * @see GenericEngine#launchInterface(Object)
	 */
	@Override
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);

		try{
			// see if we are already running
			// launch it if our config says AUTOLAUNCH=TRUE and it is not running
			// otherwise don't AUTOLAUNCH it.
			if( ! isToolRunning()){

				Log.info(ENGINE_NAME +" is not running. Evaluating AUTOLAUNCH...");

				//check to see if AUTOLAUNCH exists in ConfigureInterface
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS,
           				                              AUTOLAUNCH_OPTION);

				if (setting==null) setting = "";

				// exit with message if AUTOLAUNCH is not enabled.
				/************************************************************/
				if (! StringUtilities.convertBool(setting)){
					Log.generic(ENGINE_NAME +" AUTOLAUNCH is *not* enabled.");
					return;
				}/***********************************************************/

				Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

				String array = "";
				String tempstr = null;

				// JVM
			    String jvm = "java";
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS, JVM_OPTION);
			    if (tempstr != null) {
			    	CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr);
			    	if(! afile.isFile()){
						Log.error(ENGINE_NAME +" Option 'JVM' Java executable path is invalid: "+ tempstr);
						return;
			    	}
			    	jvm=makeQuotedPath(tempstr, true);
			    //try to derive
			    }
		    	Log.generic(ENGINE_NAME +" using 'JVM' path: "+ jvm);
			    array = jvm +" ";

				// JVMARGS -- append unmodified, if present
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS, JVMARGS_OPTION);
			    if (tempstr != null) {
			    	array += tempstr +" ";
			    }

		    	// XBOOTCLASSPATH
			    // separate RJ JVM needs some jars to be in CLASSPATH, append them to end of bootstrap classpath.
			    // When java.exe calls rational_ft.jar using option -jar, the JVM only takes rational_ft.jar as its classpath.
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS, XBOOTCLASSPATH_OPTION);
			    if(tempstr != null){
		    		array += "-Xbootclasspath/a:" + makeQuotedString(tempstr) + " ";
			    }

			    //-Dsafs.config.paths=
			    String cpaths = config.getConfigurePaths();
			    cpaths = DriverConstant.PROPERTY_SAFS_CONFIG_PATHS +"="+ cpaths;
			    //wrap in quotes if embedded space exists
			    if (cpaths.indexOf(" ")>0) cpaths = "\""+ cpaths +"\"";
			    array += "-D"+ cpaths +" ";

				// HOOK script
				String hook = HOOK_CLASS;
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS, HOOK_OPTION);
			    if (tempstr != null){
			    	hook = tempstr;
			    }
			    array += hook;

			    Log.info(ENGINE_NAME +" preparing to execute external process...");
				Log.info(" ");
				Log.info(array);
				Log.info(" ");
			    Log.info("Using runtime environment variables...");
				Log.info(" ");

			    // launch IOS Java Hook
				Runtime runtime = Runtime.getRuntime();
				Map envp = System.getenv();
				Set keys = envp.keySet();
				Iterator chain = keys.iterator();
				String[] envpArray = new String[keys.size()];
				String key = null;
				for(int i=0;i<keys.size();i++){
					key = chain.next().toString();
					envpArray[i] = key+"="+ envp.get(key).toString();
					Log.info("    "+envpArray[i]);
				}
				Log.info(" ");
				process = runtime.exec(array, envpArray);
				//process = runtime.exec(array);
				console = new ProcessConsole(process);
				Thread athread = new Thread(console);
				athread.start();

				int timeout = DEFAULT_INIT_TIMEOUT;
				//try optional config file timeout
				try{
					String t = config.getNamedValue(DriverConstant.SECTION_SAFS_IOS, TIMEOUT_OPTION);
					if((t !=null)&&(t.length()>0)) timeout = Integer.parseInt(t);
				}catch(NumberFormatException nf){
					Log.info(ENGINE_NAME +" ignoring invalid config info for TIMEOUT.");
				}

				int loop    = 0;
				running = false;

				for(;((loop < timeout)&&(! running));loop++){
					running = isToolRunning();
					if(! running)
					   try{Thread.sleep(1000);}catch(InterruptedException ix){}
				}
				if(! running){
					Log.error("Unable to detect running "+ ENGINE_NAME +
					          " within timeout period!");
					console.shutdown();
					process.destroy();
					return;
				}
				else{
					weStartedService = true;
					Log.info(ENGINE_NAME + " detected.");
				}
		    }
		}catch(Exception x){
			Log.error(
			ENGINE_NAME +" requires a valid DriverInterface object for initialization!  "+
			x.getMessage());
		}
	}

	// this may be more correctly refactored into the GenericEngine superclass.
	/** Override superclass to catch unsuccessful initialization scenarios. */
	@Override
	public long processRecord(TestRecordHelper testRecordData) {
		if (running) return super.processRecord(testRecordData);
		running = isToolRunning();
		if (running) return super.processRecord(testRecordData);
		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}
}

