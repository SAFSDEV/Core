/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer History:
 * <br> Carl Nagle  MAY 29, 2015  Add support for remoteSetKeyDelay
 * <br> Lei Wang  SEP 18, 2015  Add support for setWaitReaction
 * <br> Lei Wang  DEC 10, 2015  Add support for clipboard related methods.
 */
package org.safs.selenium.rmi.agent;

import java.awt.datatransfer.DataFlavor;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;

import org.safs.IndependantLog;
import org.safs.rmi.engine.RemoteRoot;
import org.safs.selenium.rmi.server.SeleniumRMIServer;
import org.safs.selenium.rmi.server.SeleniumServer;

/**
 * Default Selenium RMI Agent intended to communicate with a remote standalone Selenium Server JVM.
 * <p>
 * Because this is an RMI implementation, an additional Java rmic build process is necessary 
 * prior to creating the JAR file containing all classes.  The Java rmic program creates the 
 * Skeletons and Stubs needed by Java RMI.
 * <p>
 * Execute Java rmic from the root directory of the Java project:
 * <p>
 *    rmic -d . org.safs.selenium.rmi.agent.SeleniumAgent
 * <p>
 * Note: For consistent operation the <code>safs.server.hostname</code> needs to be set in code or 
 * on the command line before launching an Agent connecting to a remote Selenium RMI Server:
 * <p><ul>
 * <li>In Code:
 * <p>
 * <ul><code>System.setProperty("safs.server.hostname", "&lt;rmi server ip&gt;");</code></ul>
 * <p>
 * <ul><code>System.setProperty("safs.server.hostname", "hostname.company.internal.net");</code></ul>
 * <p>
 * <li>Command line:
 * <p>
 * <ul><code>-Dsafs.server.hostname=&lt;rmi server ip&gt;</code></ul>
 * <p>
 * <ul><code>-Dsafs.server.hostname=hostname.company.internal.net</code></ul>
 * </ul>
 * @author Carl Nagle MAR 03, 2015 Original Release
 * @see org.safs.selenium.util.SeleniumServerRunner
 * @see org.safs.selenium.rmi.server.SeleniumRMIServer
 * @see org.safs.selenium.rmi.server.SeleniumServer
 */
public class SeleniumAgent extends RemoteRoot implements SeleniumRMIAgent{

	public static final String DEFAULT_COMMAND_SEPARATOR = ";";
	/** The remote SAFS RMI SeleniumServer reference. */
	public SeleniumRMIServer server = null;  // may require a sync object
	protected boolean shutdown = false;  	 // may require a sync object
	protected ServerMonitor monitor = null;
	protected ObjID objID = null;		     // set by the ServerMonitor
	
	/** If the user sets the serverHost then we won't look for System Properties.
	 * @see #setServerHost(String) */
	protected boolean hostOverride = false;
	
	/** Subclasses will override to seek out different RMI Server Objects. */
	protected String serverName = SeleniumRMIServer.DEFAULT_RMI_SERVER;

	/** Each Agent may have a different RMI Server (Mac, Unix, Windows, Android, etc..) 
	 *  This setting "overrides/hides" the static serverHost of the RemoteRoot superclass. */
	protected String serverHost = RemoteRoot.DEFAULT_RMI_SERVER_HOST;

	/** The separator to separate the command and parameters to form a string to be processed on
	 * remote RMI server. Default separator is {@link #DEFAULT_COMMAND_SEPARATOR}*/
	private String separator = DEFAULT_COMMAND_SEPARATOR;
	
	/**
	 * @throws RemoteException
	 */
	public SeleniumAgent() throws RemoteException {
		super();
		remoteType = DEFAULT_RMI_AGENT;
	}

	/**
	 * Provide a runtime override of the target selenium server hostname.<br>
	 * This overrides System Properties settings for 'safs.server.hostname'.<br>
	 * MUST be called before initialize().
	 * @param hostname
	 * @see RemoteRoot#serverHost
	 */
	public void setServerHost(String hostname){
		if(hostname != null && hostname.length() > 0){
			hostOverride = true;
			serverHost = hostname;
		}
	}
	/** 
	 * MUST be called by instances after object construction.<br>
	 * If dynamically setting the desire selenium server host you must set that prior to initialization.
	 * @see #setServerHost(String)
	 */
	public void initialize(){
		startServerMonitor();
	}
	
	private void startServerMonitor(){
		monitor = new ServerMonitor();
		monitor.setName("ServerMonitor");
		monitor.setDaemon(true);
		monitor.start();		
	}
	
	/**
	 * Does nothing but verify the integrity of the RMI connection and log entry.
	 * The call normally comes from the RMI Server.
	 * @see org.safs.rmi.engine.Agent#ping()
	 */
	@Override
	public void ping() throws RemoteException{
		IndependantLog.info(remoteType +".ping");
	}
	
	/**
	 * @see org.safs.rmi.engine.Agent#getAgentID()
	 */
	@Override
	public ObjID getAgentID() throws RemoteException {
		return objID;
	}

	/**
	 * @see org.safs.rmi.engine.Agent#getAgentName()
	 */
	@Override
	public String getAgentName() throws RemoteException {
		return remoteType;
	}

	/** Used locally to cease all agent operations with any server and commence a complete RMI 
	 * disconnect.  This is necessary to allow the RMI threads to allow the JVM to exit.
	 */
	public void disconnect(){
		shutdown = true;
		try{server.unRegister(this);}catch(Exception x){}
		try{unexportObject(this, true);}catch(Exception x){}
	}
	
	/**
	 * Called from the remote RMI Server.
	 * Removes any RMI server reference we might already have and restarts a new 
	 * ServerMonitor to watch for a new RMI server object.
	 * @see org.safs.rmi.engine.Agent#shutdown()
	 */
	@Override
	public void shutdown() throws RemoteException {
		IndependantLog.info(remoteType+".shutdown");
		server = null;
		
		// with an active server, monitor should have already died.
		if(! monitor.isAlive()) {
			startServerMonitor();
		}
	}

	@Override
	public Object runCommand(Object command) throws RemoteException, Exception {
		return "SeleniumAgent received "+ command.toString();
	}

	/**
	 * Set the separator to separate command and parameters. 
	 * @param separator
	 */
	public void setSeparator(String separator){
		this.separator = separator;
	}
	
	/**
	 * @param screen_x
	 * @param screen_y
	 * @param mouseButtonNumber
	 * @param nclicks
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteClick(int screen_x, int screen_y, int mouseButtonNumber, int nclicks) throws ServerException, Exception{
		//Robot.click(location.x, location.y, mouseButtonNumber, 1);
		String s = separator;		
		server.runCommand(s+ SeleniumServer.CMD_CLICK +s+ 
		                     String.valueOf(screen_x) +s+
		                     String.valueOf(screen_y) +s+
		                     String.valueOf(mouseButtonNumber) +s+
		                     String.valueOf(nclicks));		
	}
	
	/**
	 * @param screen_x
	 * @param screen_y
	 * @param mouseButtonNumber
	 * @param keyCode
	 * @param nclicks
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteClickWithKeyPress(int screen_x, int screen_y, int mouseButtonNumber, int keyCode, int nclicks)
	                                    throws ServerException, Exception{
		// Robot.clickWithKeyPress(location.x, location.y, mouseButtonNumber, toJavaKeyCode(specialKey), 1);
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_CLICK_WITH_KEY +s+ 
		                     String.valueOf(screen_x) +s+
		                     String.valueOf(screen_y) +s+
		                     String.valueOf(mouseButtonNumber) +s+
		                     String.valueOf(keyCode) +s+
		                     String.valueOf(nclicks));		
	}
	
	/**
	 * @param keycode int, the key to press
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteKeyPress(int keycode) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_KEYPRESS +s+ keycode); 
	}
	/**
	 * @param keycode int, the key to release
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteKeyRelease(int keycode) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_KEYRELEASE +s+ keycode); 
	}
	/**
	 * @param wheelAmt int, the mouse wheel to scroll
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteMouseWheel(int wheelAmt) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_MOUSEWHEEL +s+ wheelAmt); 
	}
	
	/**
	 * @param keys
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteTypeKeys(String keys) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_TYPEKEYS +s+ keys); 
	}
	
	/**
	 * @param millisDelay between keystrokes
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteSetKeyDelay(int millisDelay) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_SET_KEY_DELAY +s+ String.valueOf(millisDelay)); 
	}
	
	/**
	 * Set if wait for reaction to "input keys/chars" for remote server.
	 * @param wait boolean if wait or not.
	 * @throws ServerException if the command did not execute successfully
	 * @see org.safs.robot.Robot#setWaitReaction(boolean)
	 **/
	public void remoteWaitReaction(boolean wait) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_SET_WAIT_REACTION +s+ String.valueOf(wait)); 
	}
	/**
	 * Set if wait for reaction to "input keys/chars" for remote server.
	 * @param wait boolean, if wait or not.
	 * @param tokenLength int, the length of a token. Only if the string is longer than this 
	 *                         then we wait the reaction after input-keys a certain time 
	 *                         indicated by the parameter dealyForToken.
	 * @param dealyForToken int, The delay in millisecond to wait the reaction after input-keys 
	 *                           for the string as long as a token.
	 * @param dealy int, The constant delay in millisecond to wait the reaction after input-keys.
	 * @throws ServerException if the command did not execute successfully
	 * @see org.safs.robot.Robot#setWaitReaction(boolean, int, int, int)
	 **/
	public void remoteWaitReaction(boolean wait, int tokenLength, int dealyForToken, int dealy) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_SET_WAIT_REACTION +
				          s+ String.valueOf(wait) +
				          s+ String.valueOf(tokenLength) +
				          s+ String.valueOf(dealyForToken) +
				          s+ String.valueOf(dealy)); 
	}
	
	/**
	 * @param String char
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception
	 */
	public void remoteTypeChars(String keys) throws ServerException, Exception{
		String s = separator;
		server.runCommand(s+ SeleniumServer.CMD_TYPECHARS +s+ keys); 
	}
	
	/**
	 * Clear the clipboard on the machine where the RMI server is running.
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception for other problems
	 */
	public void clearClipboard() throws ServerException, Exception{
		server.execute(SeleniumServer.CMD_CLIPBOARD_CLEAR, (Object)null); 
	}
	/**
	 * Set content to the clipboard on the machine where the RMI server is running.
	 * @param content String, the string content to set to clipboard
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception for other problems
	 */
	public void setClipboard(String content) throws ServerException, Exception{
		server.execute(SeleniumServer.CMD_CLIPBOARD_SET, content); 
	}
	
	/**
	 * Get content of the clipboard on the machine where the RMI server is running.
	 * @param dataFlavor DataFlavor, the data flavor for the content in clipboard
	 * @return Object, the content of the clipboard
	 * @throws ServerException if the command did not execute successfully
	 * @throws Exception for other problems
	 */
	public Object getClipboard(DataFlavor dataFlavor) throws ServerException, Exception{
		return server.execute(SeleniumServer.CMD_CLIPBOARD_GET, dataFlavor); 
	}
	
	/**
	 * Polls the server host for a Java Registry every few seconds until one with the proper 
	 * server object is found.  This is started at JVM bootup and remains running until 
	 * satisfied.  The thread will die once we have successfully registered with a server object.
	 * The Thread will also terminate if it detects the JVM is running the RMI Server 
	 * by polling the System Property 'safs.server.running'.
	 */
	protected class ServerMonitor extends Thread {
		Registry registry = null;
		public void run(){
			while(!shutdown && (server==null)){
				try{
					if(System.getProperty(SeleniumRMIServer.SERVER_SYSTEM_PROPERTY) instanceof Object) {
						IndependantLog.info("JAVA JVM appears to contain the SAFS RMI Server: ");
						shutdown = true;
						objID = null;
						break;
					}
				}catch(Exception re){
					IndependantLog.info("SeleniumAgent ignoring "+ re.getClass().getName()+", "+re.getMessage());
				}
				try{
					if(!hostOverride){
						String host = System.getProperty(SeleniumRMIAgent.SERVER_HOSTNAME_SYSTEM_PROPERTY);
						if(host != null && host.length() > 0) serverHost = new String(host);
					}
					IndependantLog.info("Seeking SAFS RMI Server at "+ serverHost);
					registry = LocateRegistry.getRegistry(serverHost, 1099);
					server = (SeleniumRMIServer)(registry.lookup(serverName));
					IndependantLog.info("SAFS RMI registry lookup provided SeleniumServer class "+ server.getClass().getName());
					IndependantLog.info("Monitor registering "+remoteType);	
					objID = new ObjID();				
					server.register((SeleniumRMIAgent)SeleniumAgent.this);
				}
				catch(Exception re){ 
					IndependantLog.info("SeleniumServer at "+ serverHost +" not found: "+ re.getClass().getName()+", "+re.getMessage());
					//re.printStackTrace();
					try{ sleep(3000);}
					catch(InterruptedException ie){;}
				}
			}
		}
	}
	
	/** For test purposes only.
	 * @param args String[]
	 * <ul>
	 * <li>args[0] command
	 * <li>args[1] parameter
	 * <li>args[2] parameter
	 * </ul>
	 * Use -Dsafs.server.hostname on command-line to specify the RMI host to contact.
	 */
	public static void main(String[] args) throws RemoteException{
		SeleniumAgent agent = new SeleniumAgent();
		agent.initialize();
		try{Thread.sleep(2000);}catch(Exception x){}
		try{
			String commandStr = "";
			for(String arg:args) commandStr += agent.separator+arg;
			
			System.out.println("Executing "+commandStr);
			System.out.println("Return "+agent.server.runCommand(commandStr));
		}catch(Exception x){
			System.out.println("SeleniumAgent ERROR invoking SeleniumServer.runCommand:");
			x.printStackTrace();
		}
		agent.disconnect();
	}
}
