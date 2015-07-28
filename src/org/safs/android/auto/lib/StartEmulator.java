/**
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.lib;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.safs.tools.consoles.GenericProcessCapture;

/**
 * Default usage:<br>
 * <pre>
 * new StartEmulator().run(args);
 * 
 * The above starts a new emulator and waits up to the default {@value #bootCompletionTimeoutInSeconds} seconds detecting boot completion.
 * 
 * Common Alternatives:<br>
 * 
 * StartEmulator.setBootCompletionTimeout(150);
 * StartEmulator emu = new StartEmulator();
 * emu.setOnlyIfRunning(true);
 * emu.setDoReaperThread(false);
 * emu.setDoSocketThread(false);
 * emu.setDoOpenSocket(false);
 * emu.setDoCloseSocket(false);
 * emu.run(emulatorArgs);
 * </pre>
 * @author canagl
 *
 */
public class StartEmulator {

	private final static Pattern FINISHED_BOOTING = Pattern.compile("(.*done scanning volume internal.*)|"+
            "(.*Filesystem check completed.*)|"+
            "(.*/PowerManagerService\\(.*\\): bootCompleted.*)");
	
	private String serialNumber = null;
	private Process2 emulator = null;
	
	/** Set this System property "org.safs.android.start-emulator.destroy" to "true" to destroy any 
	 * emulator we launched. */
	public static final String EMULATOR_DESTROY_PROPERTY   = "org.safs.android.start-emulator.destroy";
	
	/** System property "org.safs.android.start-emulator.destroyed" is set to "true" when we have 
	 * detected and attempted an emulator destroy request. */
	public static final String EMULATOR_DESTROYED_PROPERTY = "org.safs.android.start-emulator.destroyed";
	
	public static final String EMULATOR_WIN_EXT = ".exe";
			
	public static final String[] EMULATORS = new String[]{
		"emulator",
		"emulator-arm",
		"emulator-mips",
		"emulator-x86",
		"emulator64-arm",
		"emulator64-mips",
		"emulator64-x86"
	};
	
	private boolean doReaperThread = true;
	private boolean doSocketThread = true;
	private boolean doCloseSocket = true;
	private boolean doOpenSocket = true;
	private boolean doOnlyIfNotRunning = false;
	private Appendable chainOut = null;
	
	/*
	 *  Default: 300 seconds (5 minutes).
	 *  Change to shorten or lengthen the timeout when waiting for boot completion.
	 *  Values less than 0 mean wait indefinitely.
	 */
	private static long bootCompletionTimeoutInSeconds = 300;
	/*
	 * Default: 10 seconds.
	 * Delay the issuance of boot completion detection to allow it to settle.
	 */
	private static long bootCompletionDetectedDelayInSeconds = 10;
	
	/**
	 * This main entry point simply creates a new StartEmulator instance and passes 
	 * repackaged arguments to the run(onlyIfNotRunning, args) method.
	 * @param args<br>
	 *   "--only-if-not-running" -- self explanatory.<br>
	 *   "noreaper" -- do not run reaper thread to force emulator termination.<br>
	 *   "nosocket" -- do not run socket thread to accept socket connections.<br>
	 *   "noopensocket" -- do not the socket at all.<br>
	 *   "noclosesocket" -- do not close the socket if opened.<br>
	 *   "noboottimeout" -- do not set a timeout for boot completion.<br>
	 *   "boottimeout &lt;seconds> -- set boot completion timeout in seconds. Default is {@value #bootCompletionTimeoutInSeconds}<br>
	 *   all other args will be passed to the emulator instance.<br>
	 * @throws IOException
	 * @throws InterruptedException
	 * @see #run(boolean, String...)
	 */
	public static void main(String [] args) throws IOException, InterruptedException {
		boolean onlyIfNotRunning = false;
		String anarg = null;
		StartEmulator em = new StartEmulator();		
		Vector realargs = new Vector();
		if (args.length > 0){
			for(int a=0;a<args.length;a++){
				anarg = args[a];
				if("--only-if-not-running".equalsIgnoreCase(anarg)) {
					em.setOnlyIfNotRunning(true);
				}else if("noreaper".equalsIgnoreCase(anarg)){
					em.setDoReaperThread(false);
				}else if("nosocket".equalsIgnoreCase(anarg)){
					em.setDoSocketThread(false);
				}else if("noopensocket".equalsIgnoreCase(anarg)){
					em.setDoOpenSocket(false);
				}else if("noclosesocket".equalsIgnoreCase(anarg)){
					em.setDoCloseSocket(false);
				}else if("noboottimeout".equalsIgnoreCase(anarg)){
					StartEmulator.setBootCompletionTimeout(0);
				}else if("boottimeout".equalsIgnoreCase(anarg)){
					try{
						StartEmulator.setBootCompletionTimeout(Long.parseLong(args[++a]));
					}catch(Exception x){}
				}else{
					realargs.add(anarg);
				}
			}
			args = (String[]) realargs.toArray(new String[0]);
		}
		em.run(args);
		em.getEmulatorProcess().setPersist(true);
	}
	
	/**
	 * Set the number of seconds we watch for boot completion before timing out the watch loop.
	 * Default: {@value #bootCompletionTimeoutInSeconds} seconds.  
	 * Set 0 seconds for no timeout--wait indefinitely.  
	 * @see #watchLogUntilBooted()
	 */
	public static void setBootCompletionTimeout(long seconds){
		if (seconds >= 0) bootCompletionTimeoutInSeconds = seconds;
	}
	
	/**
	 * Set the number of seconds we delay issuing boot completion detected to allow it to settle.
	 * Default: {@value #bootCompletionDetectedDelayInSeconds} seconds.  
	 * Set 0 seconds for no delay.  
	 * @see #watchLogUntilBooted()
	 */
	public static void setBootCompletionDetectedDelay(long seconds){
		if (seconds >= 0) bootCompletionDetectedDelayInSeconds = seconds;
	}
	
	/** Set/Clear the flag to only launch an emulator on run() IF there is not one already running. 
	 * This is set FALSE by default. */
	public void setOnlyIfNotRunning(boolean ifNotRunning) { doOnlyIfNotRunning = ifNotRunning;}

	/** Set/Clear the flag to run a monitor "reaper" thread to kill the emulator on command. 
	 * This is set TRUE by default. 
	 * <p>
	 * The reaper thread monitors Java System.getProperty("org.safs.android.start-emulator.destroy").
	 * If this gets set to "true" then the emulator Process created/maintained here will 
	 * be destroyed.
	 */
	public void setDoReaperThread(boolean doReaper){doReaperThread = doReaper;}

	/** Set/Clear the flag to run a Sockets.accept() timeout thread which attempts to connect with 
	 * and report the TCP Port the emulator is starting on. 
	 * This is set TRUE by default. <br>
	 * (I have not seen a scenario where this actually works--connecting and reporting the emulator 
	 * serial number to a receiver.)
	 */
	public void setDoSocketThread(boolean doSocket){doSocketThread = doSocket;}

	/** Set/Clear the flag to even attempt a Socket Server connection and temporarily bind to a TCP port 
	 * to report the emulator serial number. 
	 * This is set TRUE by default. <br>
	 * (I have not seen a scenario where binding to the Socket has successfully connected to anything 
	 * and it may sometimes prevent abd from "seeing" the emulator--but this is just a guess.)
	 */
	public void setDoOpenSocket(boolean doSocket){doOpenSocket = doSocket;}

	/** Set/Clear the flag to Close the Socket Server connection after the Socket Thread has completed 
	 * with the attempts to report the emulator serial number to some remote receiver. 
	 * This is set TRUE by default. <br>
	 * (I have not seen a scenario where binding to the Socket has successfully connected to anything 
	 * and it may sometimes prevent abd from "seeing" the emulator--but this is just a guess.)
	 */
	public void setDoCloseSocket(boolean doSocket){doCloseSocket = doSocket;}

	/** Setan Appendable sink to receive stdOut after we are finished using the stdOut Reader to monitor 
	 * the emulator for bootstrap completion.  If no (optional) Appendable sink is provided, the stdOut is routed to 
	 * the dev/NULL sink once we are finished with it.
	 */
	public void setChainedStdOut(Appendable newOut){ chainOut = newOut;}

	/**
	 * Retrieve the Process2 object owning/wrapping the emulator process.
	 * Note, at least on Windows, the emulator process spawns multiple other processes and the emulator 
	 * that ultimately becomes visible is running in an entirely different process.  
	 * <p>
	 * Thus, doing things like process.destroy() does NOT necessarily mean the emulator will actually 
	 * be affected.  Although, it may free up resources no longer needed (and possibly interfering with) 
	 * the actual emulator and abd.
	 * @return Process2 wrapper
	 */
	public Process2 getEmulatorProcess(){ return emulator;}
	
	public void SysOut(String out){
		if(chainOut != null){
			try{ chainOut.append(out+"\n"); }catch(Exception e){ System.out.println(out); }
		}else{
			System.out.println(out);
		}
	}
	
	/**
	 * Simply calls {@link #run(boolean, String...)} using the preset value for onlyIfNotRunning.
	 * @param args emulator program args, not StartEmulator args.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run(String... args) throws IOException, InterruptedException{
		run(doOnlyIfNotRunning, args);
	}
	
	/**
	 * The primary routine used to start an emulator.  The routine will exit without starting 
	 * an emulator if onlyIfNotRunning=true and it detects a running emulator via adb "devices".
	 * <p>
	 * if doOpenSocket is true(default) it will also launch and wait for up to 30 seconds for a 
	 * remote receiver to connect to the emulator remote console and receive the emulator serial number.
	 * When that time is up, the connection is closed and the port is released (in theory).
	 * <p>
	 * Whatever emulator args are provided this routine also adds the following args:
	 * <p>
	 * <ul>-logcat *:v -report-console tcp:&lt;SocketServer port></ul>
	 * <p>
	 * This routine also will block until it has detected the emulator has completed the boot process.
	 * <p>
	 * if doReaperThread is true(default) the routine will also spawn a separate thread to monitor the 
	 * System property {@link #EMULATOR_DESTROY_PROPERTY} for emulator destroy requests.  However, because 
	 * the actual running emulator is one or more processes removed from this process this request usually 
	 * does not result in the emulator being shutdown.
	 * 
	 * @param onlyIfNotRunning
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run(boolean onlyIfNotRunning, String... args) throws IOException, InterruptedException {
		AndroidTools tools = AndroidTools.get();
		
		if (onlyIfNotRunning) {
			StringBuffer devices = new StringBuffer();
			tools.adb("devices").connectStdout(devices).discardStderr().waitForSuccess();
			if (devices.toString().contains("emulator-")) return;
		}
		
		ServerSocket serverSocket = null;
		Thread socketThread = null;
		int port = 5554;
		boolean exhausted = false;
		boolean bound = false;
		try {
			//serverSocket = new ServerSocket();
			//port = serverSocket.getLocalPort();

			//start at 5554 and work up +2, until exhausting options
			if(doOpenSocket){
				while(!bound && !exhausted){
					try{
						serverSocket = new ServerSocket(port);
						//port = 0;
						//serverSocket = new ServerSocket();
						//serverSocket.bind(null);
						//port = serverSocket.getLocalPort();
						bound = serverSocket.isBound();
					}catch(IOException x){ //assuming "already be in use"
						SysOut("Local port "+ port +" may already be in use...");
						exhausted = ((port += 2) > 5584);
					}
				}
			}

			List<String> emulatorArgs = new ArrayList<String>(asList(args));
			emulatorArgs.addAll(asList("-logcat", "*:v", "-report-console", "tcp:" + port));
			//emulatorArgs.addAll(asList("-report-console", "tcp:" + port));
			emulator = tools.emulator(emulatorArgs).connectStderr(System.err);

			if(doOpenSocket && doSocketThread) {
				SysOut("Starting ServerSocket Socket Thread...");	
				socketThread = startSocketThread(serverSocket);
			}else{
				SysOut("Bypassing ServerSocket Socket Thread...");	
			}
			
			if(bootCompletionTimeoutInSeconds > 0){
				SysOut("Watching emulator for boot completion within "+ String.valueOf(bootCompletionTimeoutInSeconds) +" seconds.");	
			}else{
				SysOut("Watching emulator for boot completion indefinitely!");	
			}
			watchLogUntilBooted();
	
			if(socketThread != null) {
				SysOut("Joining ServerSocket Socket Thread until ThreadDeath...");	
				socketThread.join();
			}
			if(doReaperThread){
				SysOut("Emulator: "+ serialNumber +", Starting Reaper Thread...");			
				startReaperThread();
			}else{
				SysOut("Emulator: "+ serialNumber +", Bypassing Reaper Thread...");			
			}
		} finally {
			if(socketThread != null) {
				SysOut("FINALLY Joining ServerSocket Socket Thread until ThreadDeath...");	
				try{socketThread.join();}catch(Exception x){}
			}
			if (doCloseSocket && serverSocket != null) {
				SysOut("Closing ServerSocket...");	
				try{ serverSocket.close();}catch(Exception x){}
			}
		}
	}

	/*
	 * Attempt to accept a Socket connection on our ServerSocket for up to 30 seconds.
	 * If a connection is made, get the emulator serial number to store for reference.
	 * I have never seen this work.  Unless this is something we are supposed to assign 
	 * to the emulator--which we've never done.
	 */
	private Thread startSocketThread(final ServerSocket serverSocket) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				Socket conn = null;
				InputStream in = null;

				if(serverSocket != null){
					try {				
						SysOut("Accepting Socket Connection for up to 30 seconds...");
						serverSocket.setSoTimeout(30 * 1000);
						conn = serverSocket.accept();
						
						SysOut("Socket Connection established.");
						in = conn.getInputStream();
						StringBuilder serialNumber = new StringBuilder("emulator-");
	
						int c = 0;
						while ((c = in.read()) != -1) {
							serialNumber.appendCodePoint(c);
						}
						StartEmulator.this.serialNumber = serialNumber.toString();
						SysOut("Detected emulator serialno: "+ serialNumber.toString());					
					} catch (IOException e) {
						SysOut("SOCKET THREAD HANDLING IOEXCEPTION: ");
						e.printStackTrace();
					} finally {
						try {
							if (in != null) in.close();
						} catch (IOException e) {}
	
						try {
							if (conn != null) conn.close();
						} catch (IOException e) {}
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();

		return thread;
	}

	/*
	 * connect emulator stdout to chainOut if chainOut exists.
	 * Otherwise, tell the emulator to discard stdout.
	 */
	private void _chainEmulatorStdOut(){
		if(chainOut != null) {
			emulator.connectStdout(chainOut);
		}else{
			emulator.discardStdout();
		}
	}
	
	/*
	 * Attempts to run "adb shell dumpsys window policy" in order to detect the 
	 * presence of the main launcher window.
	 */
	private boolean launcherWindowDetected(int secsTimeout) throws IOException{
    	Process2 process = AndroidTools.get().adb("shell", "dumpsys", "window", "policy");
    	GenericProcessCapture monitor = new GenericProcessCapture(process.getProcess(),null, true, false);
    	String line = null;
    	boolean detected = false;
    	final String LAUNCHER_CLASS = "com.android.launcher/com.android.launcher";
    	try{ 
    		process = process.waitFor(secsTimeout);
    		Vector data = monitor.getData();
			if(!data.isEmpty()){
	    		for(Object item:data){
	    			line = item.toString();
		    		if(line.contains(LAUNCHER_CLASS)) {
		    			detected = true;
		    			break;
		    		}
	    		}
			}
    	}
    	catch(Exception x){ /* ignore: all bad */}
		monitor = null;
		process.destroy();
		process = null;
    	if(! detected) SysOut("Boot detection has not found Launcher Activity yet...");
		return detected;
	}
	
	/*
	 * Watches the emulator stdOut for an indication the emulator boot process 
	 * should be complete.  Will wait/watch for boot completion up to the value of 
	 * bootCompletionTimeoutInSeconds.   
	 * @throws IOException
	 * @see #bootCompletionTimeoutInSeconds
	 * @see #bootCompletionDetectedDelayInSeconds
	 */
	private void watchLogUntilBooted() throws IOException, AndroidRuntimeException {
		_chainEmulatorStdOut();
		long timeout = Long.MAX_VALUE;
		boolean detected = false;
		if (bootCompletionTimeoutInSeconds > 0)
			timeout = System.currentTimeMillis()+(bootCompletionTimeoutInSeconds * 1000);
		SysOut("Boot detection checking Window for Launcher Activity...");
		while (! (System.currentTimeMillis() > timeout)) {
			if(launcherWindowDetected(10)){
				SysOut("Emulator boot completion detected!");
				if(StartEmulator.bootCompletionDetectedDelayInSeconds > 0)
					try{Thread.sleep(StartEmulator.bootCompletionDetectedDelayInSeconds *1000);}catch(Exception x){}
				return;
			}
			if(System.currentTimeMillis() > timeout) break;
			try{Thread.sleep(5000);}catch(Exception x){}			
		}
		SysOut("*** TIMEOUT *** reached for emulator boot process completion.");
		throw new AndroidRuntimeException("Failed to detect emulator boot completion in timeout period.");
	}
	
//	private void watchLogUntilBooted() throws IOException, AndroidRuntimeException {
//		_chainEmulatorStdOut();
//		long timeout = Long.MAX_VALUE;
//		boolean detected = false;
//		int online ;
//		int offline;
//		List devices;
//		if (bootCompletionTimeoutInSeconds > 0)
//			timeout = System.currentTimeMillis()+(bootCompletionTimeoutInSeconds * 1000);
//		while (! (System.currentTimeMillis() > timeout)) {
//			devices = DUtilities.getAttachedDevices();
//			offline = 0;
//			online = 0;
//			if(devices.size()>0){
//				for(Object device:devices){
//					if(device.toString().toLowerCase().trim().endsWith("device")){
//						online++;
//					}else{
//						offline++;
//					}
//				}
//				if(online > 0 && offline == 0){
//					SysOut("Emulator boot completion detected!");
//					if(StartEmulator.bootCompletionDetectedDelayInSeconds > 0)
//						try{Thread.sleep(StartEmulator.bootCompletionDetectedDelayInSeconds *1000);}catch(Exception x){}
//					return;
//				}
//			}
//			if(System.currentTimeMillis() > timeout) break;
//			//try{Thread.sleep(2000);}catch(Exception x){}			
//		}
//		SysOut("*** TIMEOUT *** reached for emulator boot process completion.");
//		throw new AndroidRuntimeException("Failed to detect emulator boot completion in timeout period.");
//	}
	
	/*
	 * Thread that monitors System.getProperty(EMULATOR_DESTROY_PROPERTY).
	 * If this gets set to "true" then the emulator Process created/maintained here will 
	 * be destroyed and EMULATOR_DESTROYED_PROPERTY will be set to "true".
	 */
	private void startReaperThread() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				SysOut("Monitoring Emulator shutdown requests...");
				while (! "true".equalsIgnoreCase(System.getProperty(EMULATOR_DESTROYED_PROPERTY))) {
					if ("true".equals(System.getProperty(EMULATOR_DESTROY_PROPERTY))) {
						SysOut("Received Emulator shutdown requests...");
						emulator.destroy();
						System.setProperty(EMULATOR_DESTROY_PROPERTY, "false");
						System.setProperty(EMULATOR_DESTROYED_PROPERTY, "true");
						break;
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				SysOut("Emulator Reaper Thread loop exiting as if DESTROYED.");
			}			
		});
		thread.setDaemon(true);
		thread.start();
	}	
}