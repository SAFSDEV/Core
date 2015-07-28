/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.consoles;

import java.io.*;
import java.lang.Process;
import java.util.Vector;

import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.GenericProcessMonitor;

/**
 * Attempts to capture out and err streams from a process for use by other processes.
 * The process data is available via a Vector retrieved from getData().
 * <p>
 * By default, this subclass of {@link GenericProcessConsole} disables the out and err 
 * streams from appearing in the debug/console output.  Users can change this behavior 
 * by overriding the debug() method and also by turning on 
 * the output of either or both of the streams via {@link #setShowOutStream(boolean)} and 
 * {@link #setShowErrStream(boolean)}. 
 * <p>
 * Normal usage might be something like below:
 * <pre> 
 *	Process process = runtime.exec(procstr);
 *	GenericProcessCapture console = new GenericProcessCapture(process);
 *	Thread athread = new Thread(console);
 *	athread.start();
 *  //we can wait until process is finished
 *	try{ athread.join();}catch(InterruptedException x){;}
 *	console.shutdown();//precaution
 *	Vector data = console.getData();
 * </pre> 
 * Alternatively, we can autostart the capture thread. We don't have to wait for 
 * it to end if we don't want to.  The autostarted thread can be acquired, though:
 * <pre> 
 *	Process process = runtime.exec(procstr);
 *	GenericProcessCapture console = new GenericProcessCapture(process, null, true);
 *  //we can wait until process is finished if we want
 *	try{ console.thread.join();}catch(InterruptedException x){;}
 *	Vector data = console.getData();
 *	console.shutdown();//precautionary force shutdown
 * </pre> 
 * Data for the Error and Output streams is captured into the single Vector storage.
 * The Error stream data is prefixed with the ERR_PREFIX, while the Output data is 
 * prefixed with the OUT_PREFIX.
 * <p>
 * This class contains no extended SAFS dependencies and can be readily packaged and distributed 
 * for non-SAFS installations.
 * @author canagl
 */
public class GenericProcessCapture extends GenericProcessConsole{

	protected java.util.Vector data = new java.util.Vector();
	
	/**
	 * If non-null, we will check to see if this CMD(Unix), IMAGE(Win), or PID is running. 
	 * If the process is still running, we will not attempt to end our reading of the IO 
	 * streams. */
	public String monitor = null;
	
	/**
	 * Returns true if the process we are capturing has exited.
	 * We may still be capturing additional data if a secondary process is being 
	 * monitored, however.
	 */
	public boolean exited = false;
	
	/**
	 * Returns the exitValue returned from the exited process.
	 * The value is only valid if exited = true; 
	 */
	protected int exitValue = -99;
	
	/**
	 * The (running) thread used to autostart the process capture, if applicable.
	 * Will normally be null if this thread was not autostarted.
	 */
	public Thread thread = null;
	
	/**
	 * Default Constructor for GenericProcessCapture.
	 * This sets up a default console that does not monitor any secondary processes 
	 * and does NOT autostart its own capture thread.  This instance also will not route 
	 * either the System.out or System.err to the debug() sink. 
	 * @param process to capture IO in, out, and err streams.
	 * @see #thread
	 * @see #setShowOutStream(boolean)
	 * @see #setShowErrStream(boolean)
	 */
	public GenericProcessCapture(Process process) {
		this(process, null, false);
	}

	/**
	 * Get the process exitValue.  
	 * Use isExited() first to avoid the IllegalStateException, if desired.
	 * @return exitValue or IllegalStateException if process has not exited.
	 * @throws IllegalStateException if process is still running.
	 */
	public int getExitValue()throws IllegalStateException {
		if(!exited) throw new IllegalStateException("Process still running...");
		return exitValue;
	}
	
	/**
	 * Alternative constructor allowing the IO thread to remain open as long as 
	 * a secondary process/pid remains running (if specified).  This instance also will not route 
	 * either the System.out or System.err to the debug() sink.
	 * @param process -- to capture IO in, out, and err streams.
	 * @param monitor -- optional secondary process name or pid to monitor for continued 
	 * IO output.
	 * @param autostart -- true if we should automatically start the separate capture thread.
	 * @see #thread
	 * @see #setShowOutStream(boolean)
	 * @see #setShowErrStream(boolean)
	 */
	public GenericProcessCapture(Process process, String monitor, boolean autostart) {
		this(process, monitor, autostart, false);
	}

	
	/**
	 * Alternative constructor allowing the IO thread to remain open as long as 
	 * a secondary process/pid remains running (if specified).  This version also allows 
	 * the caller to change the default behavior for routing the System.out and System.err 
	 * streams to the debug() sink.
	 * @param process -- to capture IO in, out, and err streams.
	 * @param monitor -- optional secondary process name or pid to monitor for continued 
	 * IO output.
	 * @param autostart -- true if we should automatically start the separate capture thread.
	 * @param debug2console set true to have the out and err streams copied to the debug() sink.
	 */
	public GenericProcessCapture(Process process, String monitor, boolean autostart, boolean debug2console) {
		super(process, debug2console);
		if (monitor != null && monitor.length()>0) this.monitor = monitor;
		try{
			if(showOutStream)debug("GenericProcessCapture "+ process +" initializing...");
			if(autostart){
				thread = new Thread(this);
				thread.start();
			}
		}
		catch(Exception x){
			debug("GenericProcessCapture initialization error for "+ process+", "+ x.getMessage());
		}
	}

	
	/**
	 * Return a snapshot(copy) of the String lines of data from the streams.
	 * The output stream data is prefixed with OUT_PREFIX.   
	 * The error stream data is prefixed with ERR_PREFIX.
	 * @return Vector storing a snapshot of the data read from Process out and err streams.
	 */
	public java.util.Vector getData(){
		return (Vector) data.clone();
	}

	/**
	 * @return the count of lines in combined out and err streams, or 0.
	 */
	public int getDataLineCount(){
		return data.size();
	}
	
	public void run(){
		boolean outdata = true;
		boolean errdata = true;
		String linedata = null;
		if(showOutStream) debug("GenericProcessCapture activated for "+ process);
		try{
			do{
				outdata = out.ready();
				if (outdata) {
					linedata = "";
					linedata = out.readLine();
					if(showOutStream) debug(linedata);
					if (linedata.length()>0){
						data.add(OUT_PREFIX + linedata);
						if (linedata.toLowerCase().startsWith("unhandled exception")){
								exceptions.add(OUT_PREFIX + linedata);
							}
					}
				}
				
				errdata = err.ready();
				if (errdata) {
					linedata = "";
					linedata = err.readLine();
					if(showErrStream) debug(linedata);
					if (linedata.length()>0){
						data.add(ERR_PREFIX +linedata);
						if (linedata.toLowerCase().startsWith("unhandled exception")){
								exceptions.add(ERR_PREFIX + linedata);
							}
					}
				}
				//problem getting all stream output on an exited process!
				if ((!outdata)&&(!errdata)){
					try{
						exitValue = process.exitValue();
						exited = true;
						if(showOutStream) debug("GenericProcessCapture "+ process +" exited with code: "+exitValue);
						shutdown = (monitor == null)||(monitor.length()==0);
						if(!shutdown){ // we must be monitoring a process
							try{
								shutdown = GenericProcessMonitor.isProcessRunning(monitor);
							}
							catch(Exception x){
								debug("GenericProcessCapture process monitoring for '"+ monitor +"': "+ 
										  x.getClass().getSimpleName()+": "+ x.getMessage());
							}
						}
					}catch(IllegalThreadStateException x){exited = false;}
					if(!shutdown) Thread.sleep(100);
				}
			}while(! shutdown);
			if(showOutStream) debug("GenericProcessCapture shutdown for "+ process);
		}
		catch(Exception x){
			debug("GenericProcessCapture execution error for "+process +", "+ x.getMessage());
		}
	}
}

