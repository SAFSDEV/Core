/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;

import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.drivers.InputProcessor;
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
	
	public static boolean running = false; 

	/** An available reference to the full-featured JSAFSDriver, if available.
	 *  @see org.safs.tools.drivers.JSAFSDriver */
	private static JSAFSDriver jsafs = null;
	
	/** An available reference to the a running InputProcessor, if available 
	 *  @see org.safs.tools.drivers.InputProcessor */
	private static InputProcessor processor = null;
		
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
	
	@Override
	protected JSAFSDriver jsafs(){ return jsafs;}
	@Override
	protected InputProcessor processor() { return processor; }
	
	public static void setIDriver(DriverInterface iDriver){
		if(iDriver == null){ // clear out references during shutdown
			jsafs = null;
			processor = null;
			running = false;			
		}else if(iDriver instanceof JSAFSDriver){
			if(jsafs == null) jsafs = (JSAFSDriver)iDriver;
		}else if(iDriver instanceof InputProcessor){
			if(processor == null) processor = (InputProcessor)iDriver;
			running = true;
		}
	}	
	
	public static DriverInterface getIDriver(){
		if(jsafs instanceof JSAFSDriver) return jsafs;
		if(processor instanceof InputProcessor) return processor;
		return null;
	}
	
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
			if((jsafs == null) && (processor == null))
			    jsafs = new JSAFSDriver("JSAFS");
			
			if(jsafs instanceof JSAFSDriver){
				if(jsafs.getCoreInterface()== null){
					jsafs.run();
					jsafs.systemExitOnShutdown = false;
					jsafs.removeShutdownHook();
					try{Thread.sleep(2000);}catch(Exception x){}
					preloadAppMapExpressions();
					preloadAppMaps();
				}
			}
			running = true;
		}
	}
	
	/** 
	 * This is an 'AfterAll' JVM ShutdownHook--if it is needed.
	 */
	public final Thread SHUTDOWN_HOOK = new Thread(){
		public void run(){
			System.out.println("SAFS Model Driver AfterAll hook has been invoked.");
			if(allow_shutdown) { 
				try{ shutdown(); }
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
			if(jsafs instanceof JSAFSDriver) jsafs.shutdown();	
			running = false;
		}
	}
	
	@Override
	public void shutdown() throws Exception{
		shutdownJSAFS();
	}
	
	@Override
	public void run() throws Exception{
		beforeAll();
	}
}
