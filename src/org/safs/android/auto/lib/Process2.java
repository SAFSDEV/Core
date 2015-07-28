/**
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.lib;

import java.io.BufferedReader;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;


/** Extends Process by wrapping one. */
public class Process2 {

	/** 
	 * Appendable sink for Appendable streams.
	 * @see #discardStdout()
	 * @see #discardStderr()
	 */
	private final static Appendable DEV_NULL = new Appendable() {

		public Appendable append(CharSequence csq) throws IOException { return this; }
		public Appendable append(char c) throws IOException { return this; }
		public Appendable append(CharSequence csq, int start, int end) throws IOException { return this; }
		
	};
	
	/**
	 * List of all Process2 instances created.
	 * Used to potentially destroy (cleanup) all processes launched by a JVM instance.
	 */
	private final static Set<Process2> processes = new HashSet<Process2>();
	
	private static boolean _jvmShutdown = false;
	
	/**
	 * Set to true to stop the contained process from being destroyed as part of a JVM shutdown.
	 * Default is false--allow the process to be destroyed at JVM shutdown.*/
	private boolean persist = false;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				_jvmShutdown = true;
				for (Process2 process : processes) {
					try {
						if(! process.persist){
//							System.out.println("Process2: JVM Shutdown...");
							process.destroy();
						}
					} catch (Exception ignored) {}
				}
			}
		});
	}
	
	private final Process process;

	/**
	 * Default constructor allowing process cleanup on JVM Shutdown.
	 * This is the same as instancing Process2(process, false)
	 * @param process object to wrap
	 */
	public Process2(Process process) {
		this(process, false);
	}
	
	/**
	 * Alternate constructor to allow the prevention of cleanup on JVM Shutdown.
	 * Use this if you need to allow the underlying process to persist after shutdown.
	 * @param process object to wrap
	 * @param no_shutdown_hook -- true to persist the process beyond JVM shutdown.
	 * @see #persist
	 */
	public Process2(Process process, boolean no_shutdown_hook){
		persist = no_shutdown_hook;
		if(! persist) processes.add(this);
		this.process = process;
	}
	
	/**
	 * Change the persist status of this process.
	 * If persist is being set "true", then we will attempt to remove this process 
	 * from the cache of processes that will be destroyed upon JVM Shutdown.
	 * If persist is being set "false", then we will attempt to add this process to 
	 * the cache of processes that will be destroyed upon JVM Shutdown.
	 * @param doPersist
	 */
	public void setPersist(boolean doPersist){
		persist = doPersist;
		if(persist) {
			if(!_jvmShutdown) removeJVMShutdownReference();
		}else{
			if(! processes.contains(this)){
				if(!_jvmShutdown) processes.add(this);
			}
		}
	}
	
	/**
	 * destroy() the underlying process IF the flag to persist beyond shutdown is not set.
	 * If the flag is not set we will remove this process from the JVM shutdown hook thread 
	 * and destroy it immediately.
	 * @see #persist
	 */
	public void destroy() {
		if(!persist) {
			removeJVMShutdownReference();
			process.destroy();
		}
	}

	/**
	 * Remove this process from the JVM Shutdown Hook thread.
	 * This will only succeed if we are not already in JVM shutdown mode.
	 */
	public void removeJVMShutdownReference(){
		if(!_jvmShutdown){
			try{processes.remove(this);}catch(Exception x){}
		}
	}
	/**
	 * Returns the exit value for the subprocess.
	 * @return the exit value of the subprocess represented by this Process object. 
	 * By convention, the value 0 indicates normal termination.
	 * @throws IllegalThreadStateException - if the subprocess represented by this Process object has not yet terminated.
	 */
	public int exitValue() {
		return process.exitValue();
	}
	
	/**
	 * Wait/Block for the exitValue/completion from the wrapped Process.  
	 * @return this
	 * @throws InterruptedException if thrown by the underlying process.
	 * @see java.lang.Process#waitFor()
	 */
	public Process2 waitFor() throws InterruptedException {
		process.waitFor();
		return this;
	}
	
	/**
	 * Wait/Block for the exitValue/completion from the wrapped Process up to 
	 * the provided secsTimeout period.
	 * @param secsTimeout -- number of seconds to wait before issuing IllegalThreadStateException.  
	 * @return this
	 * @throws InterruptedException if thrown by the underlying process.
	 * @throws IllegalThreadStateException if the timeout has been reached without the 
	 * process having exited.
	 */
	public Process2 waitFor(int secsTimeout) throws InterruptedException {
		long timeout = System.currentTimeMillis() + (1000 * secsTimeout);
		boolean done = false;
		while(!done){
			try{
				exitValue();
				return this;
			}catch(IllegalThreadStateException x){}
			done = timeout < System.currentTimeMillis();
			if(!done) try{ Thread.sleep(1000);}catch(Exception x){}
		}
		throw new IllegalThreadStateException("Process has not completed within specified timeout period.");
	}
	
	/**
	 * Wait for exitValue (0) from the wrapped Process.  If the exitValue is not 0 
	 * then the routine will throw a RuntimeException.
	 * @return this
	 * @throws InterruptedException if thrown by the underlying process.
	 * @throws RuntimeException if the exitValue is NOT 0 (success).
	 * @see #waitFor()
	 * @see java.lang.Process#waitFor()
	 */
	public Process2 waitForSuccess() throws InterruptedException {
		int exitValue = waitFor().exitValue();
		if (exitValue != 0) throw new RuntimeException("Tool return " + exitValue);
		return this;
	}

	/**
	 * Wait for exitValue (0) from the wrapped Process within the specified timeout period.  
	 * If the exitValue is not 0 then the routine will throw a RuntimeException.
	 * @param secsTimeout -- number of seconds to wait before issuing an IllegalThreadStateException. 
	 * @return this
	 * @throws InterruptedException if thrown by the underlying process.
	 * @throws RuntimeException if the exitValue is NOT 0 (success).
	 * @throws IllegalThreadStateException if the timeout is reached without process termination.
	 * @see #waitFor(int)
	 */
	public Process2 waitForSuccess(int secsTimeout) throws InterruptedException {
		int exitValue = waitFor(secsTimeout).exitValue();
		if (exitValue != 0) throw new RuntimeException("Tool return " + exitValue);
		return this;
	}

	private Writer wrap(OutputStream sink) {
		return new OutputStreamWriter(sink);
	}
	
	private Reader wrap(InputStream sink) {
		return new InputStreamReader(sink);
	}
	
	public Process getProcess(){
		return process;
	}
	
	public OutputStream getStdin() {
		return process.getOutputStream();
	}

	public Writer getStdinWriter() {
		return wrap(process.getOutputStream());
	}
	
	public InputStream getStdout() {
		return process.getInputStream();
	}
	
	public BufferedReader getStdoutReader() {
		return new BufferedReader(wrap(process.getInputStream()));
	}
	
	public InputStream getStderr() {
		return process.getErrorStream();
	}
	
	public BufferedReader getStderrReader() {
		return new BufferedReader(wrap(process.getErrorStream()));
	}
	
	/**
	 * Connect an Appendable sink to a Readable source and immediately begin 
	 * processing the source contents into the sink.
	 * <p>
	 * Important: A separate thread is launched to read the source and write to the 
	 * sink to prevent blocking.  However, the thread only lasts for as long as there 
	 * are characters to be read.  A long-running process that does not consistently 
	 * send characters can allow this thread to run to completion and exit before the 
	 * source is truly done sending output.
	 * <p>
	 * Set persist to true to prevent the IO reading from stopping when it otherwise 
	 * would stop.  Reset persist to false to allow the thread to terminate.
	 * @param source
	 * @param sink
	 * @see #persist
	 */
	private void connect(final Readable source, final Appendable sink) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				CharBuffer cb = CharBuffer.wrap(new char [256]);
				try {
					int numchars = source.read(cb);
					while(numchars != -1 || persist){
						if(numchars != -1){
							cb.flip();
							sink.append(cb);
							cb.clear();
							if (sink instanceof Flushable) {
								((Flushable)sink).flush();
							}
						}else{
							// don't hog the cpu if there is nothing to read yet
							try{Thread.sleep(200);}catch(Exception x){}
						}
						numchars = source.read(cb);
					}
				} catch (IOException e) { /* prolly broken pipe, just die */ 
					setPersist(false);
				}
			}
		});
		thread.setDaemon(!persist);
		thread.start();
	}
	
	public Process2 connectStdin(Readable source) {
		connect(source, wrap(getStdin()));
		return this;
	}
		
	public Process2 connectStdin(InputStream source) {
		return connectStdin(wrap(source));
	}
	
	public Process2 connectStdout(Appendable sink) {
		connect(wrap(getStdout()), sink);
		return this;
	}
	
	public Process2 connectStderr(Appendable sink) {
		connect(wrap(getStderr()), sink);
		return this;
	}

	/**
	 * Connects Stdout to the internal DEV_NULL sink.
	 * @return this (from connectStdout)
	 * @see #connectStdout(Appendable)
	 */
	public Process2 discardStdout() {
		return connectStdout(DEV_NULL);
	}
	
	/**
	 * Connects Stderr to the internal DEV_NULL sink.
	 * @return this (from connectStdout)
	 * @see #connectStderr(Appendable)
	 */
	public Process2 discardStderr() {
		return connectStderr(DEV_NULL);
	}
	
	/**
	 * Forward Process Stdin to System.in  
	 * Also invokes forwardOutput
	 * 
	 * @return this (from forwardOutput())
	 * @see #forwardOutput()
	 */
	public Process2 forwardIO() {
		connectStdin(System.in);
		return forwardOutput();
	}
	
	/**
	 * Forward Process StdOut to System.out and forward StdErr to System.err.
	 * Uses connectStdout(System.out); connectStderr(System.err);
	 * 
	 * @return this
	 */
	public Process2 forwardOutput() {
		connectStdout(System.out);
		connectStderr(System.err);
		return this;
	}
	
}
