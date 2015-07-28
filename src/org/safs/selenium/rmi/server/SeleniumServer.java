/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.rmi.server;

import java.awt.AWTException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.ObjID;
import java.util.Enumeration;
import java.util.Hashtable;

import org.safs.IndependantLog;
import org.safs.rmi.engine.Agent;
import org.safs.rmi.engine.RemoteRoot;
import org.safs.robot.Robot;
import org.safs.selenium.rmi.agent.SeleniumAgent;
import org.safs.selenium.rmi.agent.SeleniumRMIAgent;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * Default SAFS SeleniumPlus RMI Server normally embedded inside a remote standalone Selenium Server JVM.
 * <p>
 * Because this is an RMI implementation, an additional Java rmic build process is necessary 
 * prior to creating the JAR file containing all classes.  The Java rmic program creates the 
 * Skeletons and Stubs needed by Java RMI.
 * <p>
 * Execute Java rmic from the root directory of the Java project:
 * <p>
 *    rmic -d . org.safs.selenium.rmi.server.SeleniumServer
 * <p>
 * Note: For consistent operation the <code>java.rmi.server.hostname</code> needs to be set in code or 
 * on the command line before launching an RMI Server:
 * <p><ul>
 * <li>In Code:
 * <p>
 * <ul><code>System.setProperty("java.rmi.server.hostname", "&lt;rmi server ip&gt;");</code></ul>
 * <p>
 * <ul><code>System.setProperty("java.rmi.server.hostname", "hostname.company.internal.net");</code></ul>
 * <p>
 * <li>Command line:
 * <p>
 * <ul><code>-Djava.rmi.server.hostname=&lt;rmi server ip&gt;</code></ul>
 * <p>
 * <ul><code>-Djava.rmi.server.hostname=hostname.company.internal.net</code></ul>
 * </ul>
 * @author Carl Nagle MAR 03, 2015 Original Release
 * <br> Carl Nagle  MAY 29, 2015  Add support for setMillisBetweenKeystrokes
 * @see org.safs.selenium.util.SeleniumServerRunner
 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
 * @see org.safs.selenium.rmi.agent.SeleniumAgent
 */
public class SeleniumServer extends RemoteRoot implements SeleniumRMIServer{
	
	public static final String CMD_CLICK 			= "CLICK";
	public static final String CMD_CLICK_WITH_KEY 	= "CLICK-KEY";
	public static final String CMD_KEYPRESS 		= "KEY-PRESS";
	public static final String CMD_KEYRELEASE 		= "KEY-RELEASE";
	public static final String CMD_MOUSEWHEEL 		= "MOUSE-WHEEL";
	public static final String CMD_TYPEKEYS 		= "TYPE-KEYS";
	public static final String CMD_TYPECHARS 		= "TYPE-CHARS";
	public static final String CMD_SET_KEY_DELAY 	= "SET-KEY-DELAY";
	
	private Hashtable agents = new Hashtable();
	
	/**
	 * Name of Server object in rmi Naming registry.
	 * Subclasses should override the value.  Defaults to DEFAULT_RMI_SERVER
	 */
	protected String serverName = DEFAULT_RMI_SERVER;
	
	
	/**
	 * 
	 */
	public SeleniumServer() throws RemoteException{
		super();
		System.setProperty(SERVER_SYSTEM_PROPERTY, DEFAULT_RMI_SERVER);
		remoteType = DEFAULT_RMI_SERVER;
		rebindLocalRMIRegistry(serverName, this);
	}

	@Override
	public void register(SeleniumRMIAgent anAgent) throws RemoteException {
		agents.put(anAgent.getAgentID(), anAgent);		
		String agentname = anAgent.getAgentName();
		IndependantLog.info(agentname +" registered as agent "+ agents.size() +":ID="+ anAgent.getAgentID()); 
	}

	@Override
	public void unRegister(SeleniumRMIAgent anAgent) throws RemoteException {
		class UnregThread extends Thread {
			private ObjID theID;
			public UnregThread(ObjID objID){ 
				super(); 
				theID = objID;}
			public void run(){
				agents.remove(theID);		
				IndependantLog.info(remoteType +" removed agent ID="+ theID +". "+ agents.size() +" agents remain.");
				pingAgents();
			}
		}
		new UnregThread(anAgent.getAgentID()).run();
	}

	/**
	 * Simple routine to ping each registered agent to see if it is actually still there.
	 * The routine will unregister an agent when the attempt to contact it throws an exception.
	 * This can happen when a remote Agent's JVM does not call registered shutdown hooks and allow 
	 * the Agent to gracefully unregister with the RMI server.
	 */
	protected void pingAgents(){
		Enumeration keys = agents.keys();

		// PING all known agents to see if they are still alive
		while(keys.hasMoreElements()){
			SeleniumRMIAgent agent = null;
			ObjID objID = null;
			try{
				objID = (ObjID) keys.nextElement();
				agent = (SeleniumRMIAgent) agents.get(objID);
				agent.ping();
				IndependantLog.info(remoteType +":Agent ID="+ objID +" OK");
			}
			catch(java.rmi.RemoteException x){
				agents.remove(objID);
				IndependantLog.info(remoteType +" removing Disconnected SeleniumAgent ID="+ objID +". "+ agents.size() +" agents remain.");
				//x.printStackTrace();
			}
			catch(Exception x){
				x.printStackTrace();
				agents.remove(objID);
				IndependantLog.info(remoteType +" removing Missing SeleniumAgent ID="+ objID +". "+ agents.size() +" agents remain.");
				//x.printStackTrace();				
			}
		}
	}	

	/**
	 * Robot.click(x, y, mouseButton, nclicks);
	 * <p>
	 * @param params String[]
	 * <pre>
	 * field[0] int x            (default 0)
	 * field[1] int y            (default 0)
	 * field[2] int mouseButton  (default MOUSE_BUTTON_LEFT)
	 * field[3] int numClicks    (default 1)
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerException 
	 * @see Robot#click(int, int, int, int)
	 */
	protected Object click(String[] params) throws ServerException{	
		int x = 0;int y = 0;
		int button = WDLibrary.MOUSE_BUTTON_LEFT; 
		int n = 1;		
		try{
			x = Integer.parseInt(params[0]);
			y = Integer.parseInt(params[1]);
			button = Integer.parseInt(params[2]);
			n = Integer.parseInt(params[3]);
		}catch(Exception ignore){}
		
		try{ 
			IndependantLog.info(remoteType +" performing Robot.click.");
			Robot.click(x, y, button, n); 
		}
		catch(AWTException awtx){
			String msg = remoteType +" Robot.click "+awtx.getClass().getName()+", "+ awtx.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, awtx);
		}
		return new String(); 
	}
	
	/**
	 * Robot.clickWithKeyPress(x, y, mouseButton, keyCode, nclicks);
	 * <p>
	 * @param params String[]
	 * <pre>
	 * field[0] int x            (default 0)
	 * field[1] int y            (default 0)
	 * field[2] int mouseButton  (default MOUSE_BUTTON_LEFT )
	 * field[3] int keyCode      (default int 0 -- unknown)
	 * field[4] int numClicks    (default 1)
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerRuntimeException 
	 * @see Robot#clickWithKeyPress(int, int, int, int, int)
	 */
	protected Object clickWithKeyPress(String[] params) throws ServerException{	
		int x = 0;int y = 0;
		int button = WDLibrary.MOUSE_BUTTON_LEFT;
		int keyCode = 0;
		int n = 1;		
		try{
			x = Integer.parseInt(params[0]);
			y = Integer.parseInt(params[1]);
			button = Integer.parseInt(params[2]);
			keyCode = Integer.parseInt(params[4]);
			n = Integer.parseInt(params[4]);
		}catch(Exception ignore){}
		
		try{ 
			IndependantLog.info(remoteType +" performing Robot.clickWithKeyPress.");
			Robot.clickWithKeyPress(x, y, button, keyCode, n); 
		}
		catch(AWTException awtx){
			String msg = remoteType +" Robot.clickWithKeyPress "+awtx.getClass().getName()+", "+ awtx.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, awtx);
		}
		return new String(); 
	}

	/**
	 * Robot.keyPress(keyCode);
	 * <p>
	 * @param params String[]
	 * <pre>
	 * field[0] int keyCode
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerRuntimeException if fail
	 * @see Robot#keyPress(int)
	 */
	protected Object keyPress(String[] params) throws ServerException{		
		try{			
			Robot.keyPress(Integer.parseInt(params[0]));
		}catch(Exception any){
			String msg = remoteType +" Robot.keyPress "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}
	/**
	 * Robot.keyRelease(keyCode);
	 * <p>
	 * @param params String[]
	 * <pre>
	 * field[0] int keyCode
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerRuntimeException if fail
	 * @see Robot#keyRelease(int)
	 */
	protected Object keyRelease(String[] params) throws ServerException{		
		try{			
			Robot.keyRelease(Integer.parseInt(params[0]));
		}catch(Exception any){
			String msg = remoteType +" Robot.keyRelease "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}
	/**
	 * Robot.mouseWheel(keyCode);
	 * <p>
	 * @param params String[]
	 * <pre>
	 * field[0] int, the wheel amount to scroll.
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerRuntimeException if fail
	 * @see Robot#mouseWheel(int)
	 */
	protected Object mouseWheel(String[] params) throws ServerException{		
		try{			
			Robot.mouseWheel(Integer.parseInt(params[0]));
		}catch(Exception any){
			String msg = remoteType +" Robot.mouseWheel "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}
	
	/**
	 * Robot.setMillisBetweenKeystrokes(string);
	 * @param params String[]
	 * <pre>
	 * field[0] int milliseconds between keystrokes delay.
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerException on failure.
	 * @see Robot#setMillisBetweenKeystrokes(int)
	 */
	protected Object setMillisBetweenKeystrokes(String[] params) throws ServerException{		
		try{			
			IndependantLog.info(remoteType +" performing Robot.setMillisBetweenKeystrokes.");
			Robot.setMillisBetweenKeystrokes(Integer.parseInt(params[0]));
		}catch(Exception any){
			String msg = remoteType +" Robot.setMillisBetweenKeystrokes "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}
	
	/**
	 * Robot.inputKeys(string);
	 * @param params String[]
	 * <pre>
	 * field[0] String input string.
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerException on failure.
	 * @see Robot#inputKeys(String)
	 */
	protected Object typeKeys(String[] params) throws ServerException{		
		try{			
			IndependantLog.info(remoteType +" performing Robot.inputkeys.");
			Robot.inputKeys(params[0]);
		}catch(Exception any){
			String msg = remoteType +" Robot.typeKeys "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}
	
	/**
	 * Robot.inputChars(string);
	 * @param params String[]
	 * <pre>
	 * field[0] String input string.
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerException 
	 * @see Robot#inputChars(String)
	 */
	protected Object typeChars(String[] params) throws ServerException{
		try{			
			IndependantLog.info(remoteType +" performing Robot.inputkeys.");
			Robot.inputKeys(params[0]);
		}catch(Exception any){
			String msg = remoteType +" Robot.inputKeys "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
		return new String();
	}

	/**
	 * @param command -- usually a String.
	 * <p>
	 * String commands interpretted as:
	 * <pre>
	 * char[0] field separator for all subsequent fields
	 * field[1] -- command
	 * field[2-N] -- command parameters
	 * @return determined by individual commands. Usually a String. 
	 * @throws ServerException if an error occurs or the requested command is not supported.
	 */
	@Override
	public Object runCommand(Object command) throws RemoteException, Exception {
		System.out.println("SeleniumRMIServer received "+ command.toString());
		IndependantLog.info(remoteType +"runCommand received "+ command.toString());
		String action = null;
		if(command instanceof String){
			String[] params = null;
			try{
				String cmd = (String)command;
				String sep = cmd.substring(0,1);
				cmd = cmd.substring(1);
				String[] fields = cmd.split(sep);
				action = fields[0];
				params = new String[fields.length-1];
				System.arraycopy(fields, 1, params, 0, fields.length-1);
			}catch(Exception x){
				String msg = remoteType +" runCommand "+x.getClass().getName()+", "+ x.getMessage();
				IndependantLog.warn(msg);
				throw new ServerException(msg, x);
			}
			if(CMD_CLICK.equalsIgnoreCase(action)) return click(params);
			if(CMD_CLICK_WITH_KEY.equalsIgnoreCase(action)) return clickWithKeyPress(params);
			if(CMD_TYPEKEYS.equalsIgnoreCase(action)) return typeKeys(params);
			if(CMD_TYPECHARS.equalsIgnoreCase(action)) return typeChars(params);
			if(CMD_KEYPRESS.equalsIgnoreCase(action)) return keyPress(params);
			if(CMD_KEYRELEASE.equalsIgnoreCase(action)) return keyRelease(params);
			if(CMD_MOUSEWHEEL.equalsIgnoreCase(action)) return mouseWheel(params);
			if(CMD_SET_KEY_DELAY.equalsIgnoreCase(action)) return setMillisBetweenKeystrokes(params);
		}
		IndependantLog.warn(remoteType +" does not support command "+ action);
		throw new ServerException(remoteType +" Unsupported commmand type or parameters.");
	}
}
