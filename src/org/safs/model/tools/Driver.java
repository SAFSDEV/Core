/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.model.AbstractCommand;
import org.safs.model.ComponentFunction;
import org.safs.model.DriverCommand;
import org.safs.model.annotations.JSAFSBefore;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.commands.DDDriverCommands;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * A JSAFSDriver wrapper attempting to provide a minimalist (simple) interface--hiding a 
 * good bit of JSAFS complexity.
 * <p>
 * @author Carl Nagle
 * @since OCT 15, 2013
 * @see org.safs.tools.drivers.JSAFSDriver
 */
public class Driver extends AbstractDriver {
	
	static boolean running = false; 

	/** An available reference to the full-featured JSAFSDriver 
	 *  @see org.safs.tools.drivers.JSAFSDriver */
	public static final JSAFSDriver jsafs = new JSAFSDriver("JSAFS");
	
	/** 
	 * Set to false to prevent JSAFS from shutting down on a call to shutdownJSAFS(). 
	 * Default is true. JSAFS shutting down will NOT force a System exit on shutdown.
	 * @see #shutdownJSAFS() 
	 **/	
	public static boolean allow_shutdown = true;
	
	public Driver(){
		super();
		_instance = this;
	}
	
	protected JSAFSDriver jsafs(){ return jsafs;}
	
	/**
	 * Must be called at least once to initialize the internal JSAFSDriver.
	 * This will be called automatically when using AutoConfigureJSAFS and Runner.autorun 
	 * appropriately.
	 * <p>  
	 * It can be called automatically by annotations (dependency injection), or be called 
	 * from an overriding test controller like a JUnit runner, if appropriate.
	 * @see org.safs.model.annotations.AutoConfigureJSAFS
	 * @see org.safs.model.tools.Runner#autorun(String[])
	 */
	public void beforeAll(){
		if(!running){
			jsafs.run();
			jsafs.systemExitOnShutdown = false;
			jsafs.removeShutdownHook();
			running = true;
			try{Thread.sleep(2000);}catch(Exception x){}
			preloadAppMapExpressions();
			preloadAppMaps();
		}
	}
	
	/** 
	 * This is an 'AfterAll' JVM ShutdownHook--if it is needed.
	 */
	public static final Thread SHUTDOWN_HOOK = new Thread(){
		public void run(){
			System.out.println("SAFS Model Driver AfterAll hook has been invoked.");
			if(allow_shutdown) { 
				try{ shutdownJSAFS(); }
				catch(Throwable t){
					System.out.println(t.getMessage());
				}
			}
		}
	};

	/**
	 * Used to invoke the JSAFSDriver shutdown function.
	 * The routine will only invoke the function if allow_shutdown = true.
	 * @throws Exception
	 * @see #allow_shutdown
	 * @see org.safs.tools.drivers.JSAFSDriver#shutdown()
	 */
	public static void shutdownJSAFS() throws Exception {
		if(allow_shutdown) {
			jsafs.shutdown();	
			running = false;
		}
	}
}
