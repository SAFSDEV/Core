/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.consoles;

import java.io.*;
import java.lang.Process;

//import org.safs.Log;
import org.safs.tools.CaseInsensitiveFile;

/**
 * Continuously monitors the wrapped process out and err streams routing them to the local 
 * process System.out and System.err via {@link #debug(String)} so they will not block.
 * <p>
 * By default, all System.out and System.err will go thru the debug() method. 
 * Users can change this behavior by overriding the debug() method and also by turning off 
 * the output of either or both of the streams via {@link #setShowOutStream(boolean)} and 
 * {@link #setShowErrStream(boolean)}. 
 * <p>
 * The class also attempts to see "unhandled exceptions" and store them in the exceptions Vector.
 * <p>
 * Normal usage might be something like below:
 * <pre> 
 *	Process process = runtime.exec(procstr);
 *	GenericProcessConsole console = new GenericProcessConsole(process);
 *	Thread athread = new Thread(console);
 *	athread.start();
 *  //we can wait until process is finished
 *	try{ athread.join();}catch(InterruptedException x){;}
 *	console.shutdown();//precaution
 * </pre>
 * This class contains no extended SAFS dependencies and can be readily packaged and distributed 
 * for non-SAFS installations.
 */
public class GenericProcessConsole implements Runnable{

	protected Process process;
	protected BufferedReader  out;
	protected BufferedReader  err;	
	protected BufferedWriter  in;
	protected java.util.Vector exceptions = new java.util.Vector();
	boolean shutdown = false;
	/** 
	 * set false to stop a copy of output stream data going to the debug sink.
	 * Default setting is true. */
	protected boolean showOutStream = true;
	/** set false to stop a copy of error stream data going to the debug sink.
	 * Default setting is true. */
	protected boolean showErrStream = true;
	
	/** "OUT: "*/
    public static final String OUT_PREFIX = "OUT: ";
	/** "ERR: "*/
    public static final String ERR_PREFIX = "ERR: ";
	
	/**
	 * Default (and only) public constructor for GenericProcessConsole.
	 * The class instance does NOT start its own separate runnable Thread.
	 */
	public GenericProcessConsole(Process process) {
		super();
		_initialize(process);
	}

	protected void _initialize(Process process){
		this.process = process;
		try{
			if(showOutStream) debug("GenericProcessConsole initializing...");
			out = new BufferedReader(new InputStreamReader(process.getInputStream()));
			err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			in  = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			if(showOutStream) debug("GenericProcessConsole initialization complete.");
		}
		catch(Exception x){
			debug("GenericProcessConsole initialization error:"+ x.getMessage());
		}
	}
	
	public GenericProcessConsole(Process process, boolean debug2Console) {
		super();
		setShowOutStream(debug2Console);
		setShowErrStream(debug2Console);
		_initialize(process);
	}

	/**
	 * Writes to System.out .
	 * Subclasses should override to log to alternate sinks.
	 * @param message
	 */
	protected void debug(String message){
		System.out.println(message);
	}
	
	/** set true to have the output stream copied to the active debug sink */
	public void setShowOutStream(boolean showOut){ showOutStream = showOut;}
	/** set true to have the error stream copied to the active debug sink */
	public void setShowErrStream(boolean showErr){ showErrStream = showErr;}
	
	/**
	 * Call to set the shutdown flag to true to stop the running console thread.
	 */
	public void shutdown(){ shutdown = true;}
	
	/**
	 * @return Vector storing any Exceptions read from Process out and err streams.
	 */
	public java.util.Vector getExceptions(){
		return new java.util.Vector(exceptions);
	}

	/**
	 * @return the count of Exceptions in out or err streams, or 0.
	 */
	public int getExceptionsCount(){
		return exceptions.size();
	}
	
	/**
	 * Continuously monitors the process out and err streams routing them to the local 
	 * process System.out and System.err.  Also attempts to see "unhandled exceptions" and 
	 * store them in the exceptions Vector.  Will run indefinitely until the process 
	 * exits on its own or the shutdown method is called.
	 * @see #getExceptions()
	 * @see #shutdown()
	 */
	public void run(){
		boolean outdata = true;
		boolean errdata = true;
		String linedata = null;
		int exitValue = -1;
		if(showOutStream) debug("GenericProcessConsole activated for process "+ process);
		try{
			do{
				outdata = out.ready();
				if (outdata) {
					linedata = "";
					linedata = out.readLine();
					if(showOutStream) debug(linedata);
					if ((linedata.length()>0) && 
						(linedata.toLowerCase().startsWith("unhandled exception"))){
						exceptions.add(OUT_PREFIX + linedata);
					}
				}
				
				errdata = err.ready();
				if (errdata) {
					linedata = "";
					linedata = err.readLine();
					if(showErrStream) debug(linedata);
					if ((linedata.length()>0) && 
						(linedata.toLowerCase().startsWith("unhandled exception"))){
						exceptions.add(ERR_PREFIX + linedata);
					}
				}
				try{
					exitValue = process.exitValue();
					shutdown = true;
				}catch(IllegalThreadStateException x){
					// process not yet finished
				}
				if ((!outdata)&&(!errdata)&&(!shutdown)) Thread.sleep(250);
			}while(! shutdown);
			if(showOutStream) debug("GenericProcessConsole shutdown for process "+ process);
		}
		catch(Exception x){
			debug("GenericProcessConsole thread loop error:"+ x.getMessage());
		}
	}
}

