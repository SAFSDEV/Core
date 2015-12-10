/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer History:
 * <br> Carl Nagle  MAY 29, 2015  Add support for setMillisBetweenKeystrokes
 * <br> Lei Wang  SEP 18, 2015  Add support for setWaitReaction
 * <br> Lei Wang  DEC 10, 2015  Add support for clipboard related methods.
 */
package org.safs.selenium.rmi.server;

import java.awt.AWTException;
import java.awt.datatransfer.DataFlavor;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.ObjID;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.rmi.engine.RemoteRoot;
import org.safs.robot.Robot;
import org.safs.selenium.rmi.agent.SeleniumRMIAgent;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.stringutils.StringUtilities;

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
 * @see org.safs.selenium.util.SeleniumServerRunner
 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
 * @see org.safs.selenium.rmi.agent.SeleniumAgent
 */
public class SeleniumServer extends RemoteRoot implements SeleniumRMIServer{
	
	public static final String CMD_CLICK 				= "CLICK";
	public static final String CMD_CLICK_WITH_KEY 		= "CLICK-KEY";
	public static final String CMD_KEYPRESS 			= "KEY-PRESS";
	public static final String CMD_KEYRELEASE 			= "KEY-RELEASE";
	public static final String CMD_MOUSEWHEEL 			= "MOUSE-WHEEL";
	public static final String CMD_TYPEKEYS 			= "TYPE-KEYS";
	public static final String CMD_TYPECHARS 			= "TYPE-CHARS";
	public static final String CMD_SET_KEY_DELAY 		= "SET-KEY-DELAY";
	public static final String CMD_SET_WAIT_REACTION 	= "SET-WAIT-REACTION";
	public static final String CMD_CLIPBOARD_CLEAR 		= "CLIPBOARD_CLEAR";
	public static final String CMD_CLIPBOARD_SET 		= "CLIPBOARD_SET";
	public static final String CMD_CLIPBOARD_GET 		= "CLIPBOARD_GET";
	
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
	 * @throws ServerException 
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
	 * @throws ServerException if fail
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
	 * @throws ServerException if fail
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
	 * @throws ServerException if fail
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
	 * {@link Robot#setWaitReaction(boolean)} and {@link Robot#setWaitReaction(boolean, int, int, int)}.
	 * @param params String[]
	 * <pre>
	 * params[0] wait boolean, if wait or not.
	 * params[1] tokenLength int, the length of a token. Only if the string is longer than this 
	 *                            then we wait the reaction after input-keys a certain time 
	 *                            indicated by the parameter dealyForToken.
	 * params[2] dealyForToken int, The delay in millisecond to wait the reaction after input-keys 
	 *                              for the string as long as a token.
	 * params[3] dealy int, The constant delay in millisecond to wait the reaction after input-keys.
	 * </pre>
	 * @return An empty String on success.
	 * @throws ServerException on failure.
	 * @see Robot#setWaitReaction(boolean)
	 * @see Robot#setWaitReaction(boolean, int, int, int)
	 */
	protected Object setWaitReaction(String[] params) throws ServerException{		
		try{			
			IndependantLog.info(remoteType +" performing Robot.setWaitReaction with parameters="+Arrays.toString(params));
			if(params.length==1){
				Robot.setWaitReaction(StringUtilities.convertBool(params[0]));
			}else if(params.length==4){
				Robot.setWaitReaction(StringUtilities.convertBool(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]));
			}else{
				throw new SAFSException(" the parameter number is not correct.");
			}
		}catch(Exception any){
			String msg = remoteType +" Robot.setWaitReaction "+any.getClass().getName()+", "+ any.getMessage();
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
	 * Clear the clipboard.
	 * @return A Boolean(true) on success.
	 * @throws ServerException 
	 * @see Robot#clearClipboard()
	 */
	protected Boolean clearClipboard() throws ServerException{
		try{			
			IndependantLog.info(remoteType +" performing Robot.clearClipboard.");
			Robot.clearClipboard();
			return new Boolean(true);
		}catch(Exception any){
			String msg = remoteType +" Robot.clearClipboard "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
	}
	
	/**
	 * Set string content to clipboard.
	 * @param params Object...
	 * <pre>
	 * field[0] String content string to set to clipboard.
	 * </pre>
	 * @return A Boolean(true) on success.
	 * @throws ServerException 
	 * @see {@link Robot#setClipboard(String)}
	 */
	protected Boolean setClipboard(Object... params) throws ServerException{
		IndependantLog.info(remoteType +" performing Robot.setClipboard.");
		if(params==null || params.length<1 || !(params[0] instanceof String)){
			throw new ServerException(remoteType+" Parameter is not valid. Please provide a String as parameter.");
		}
		
		try{			
			Robot.setClipboard(params[0].toString());
			return new Boolean(true);
		}catch(Exception any){
			String msg = remoteType +" Robot.setClipboard "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
	}
	
	/**
	 * Get clipboard's content.
	 * @param params Object...
	 * <pre>
	 * field[0] DataFlavor the type of the content to get from clipboard
	 * </pre>
	 * @return Object, the content of the clipboard
	 * @throws ServerException 
	 * @see {@link Robot#getClipboard(DataFlavor)}
	 */
	protected Object getClipboard(Object... params) throws ServerException{
		IndependantLog.info(remoteType +" performing Robot.getClipboard.");
		if(params==null || params.length<1 || !(params[0] instanceof DataFlavor)){
			throw new ServerException(remoteType+" Parameter is not valid. Please provide a DataFlavor as parameter.");
		}
		
		try{			
			return Robot.getClipboard((DataFlavor)params[0]);
		}catch(Exception any){
			String msg = remoteType +" Robot.getClipboard "+any.getClass().getName()+", "+ any.getMessage();
			IndependantLog.warn(msg);
			throw new ServerException(msg, any);
		}
	}

	/**
	 * @param commandAndParameters -- usually a String.
	 * <p>
	 * String commandAndParameters interpreted as:
	 * <pre>
	 * char[0] field separator for all subsequent fields
	 * field[1] -- command
	 * field[2-N] -- command parameters
	 * </pre>
	 * @return determined by individual commands. Usually a String. 
	 * @throws ServerException if an error occurs or the requested command is not supported.
	 */
	@Override
	public Object runCommand(Object commandAndParameters) throws RemoteException, Exception {
		if(commandAndParameters==null) throw new ServerException(remoteType +" The passed in command is null!");
		
		System.out.println("SeleniumRMIServer received "+ commandAndParameters.toString());
		IndependantLog.info(remoteType +"runCommand received "+ commandAndParameters.toString());
		String action = null;
		if(commandAndParameters instanceof String){
			String[] params = null;
			try{
				String cmd = (String)commandAndParameters;
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
			if(CMD_SET_WAIT_REACTION.equalsIgnoreCase(action)) return setWaitReaction(params);
			
			IndependantLog.warn(remoteType +" does not support command "+ action);
			
		}else{
			IndependantLog.warn(remoteType +" the command's type '"+commandAndParameters.getClass().getSimpleName()+"' is not supported");
			
		}
		
		throw new ServerException(remoteType +" Unsupported commmand type or parameters.");
	}
	
	/**
	 * @param command -- usually a String representing the action's name 
	 * @param parameters Object..., serializable parameters
	 * 
	 * @return determined by individual commands. 
	 * @throws ServerException if an error occurs or the requested command is not supported.
	 */
	@Override
	public Object execute(Object command, Object... parameters) throws RemoteException, Exception {
		if(command==null) throw new ServerException(remoteType +" The passed in command is null!");
		
		System.out.println("SeleniumRMIServer received "+ command.toString());
		IndependantLog.info(remoteType +"runCommand received "+ command.toString());
		
		if(command instanceof String){
			String action = command.toString();
			
			if(CMD_CLIPBOARD_CLEAR.equalsIgnoreCase(action)) return clearClipboard();
			if(CMD_CLIPBOARD_SET.equalsIgnoreCase(action)) return setClipboard(parameters);
			if(CMD_CLIPBOARD_GET.equalsIgnoreCase(action)) return getClipboard(parameters);
			
			IndependantLog.warn(remoteType +" does not support command "+ action);
			
		}else{
			IndependantLog.warn(remoteType +" the command's type '"+command.getClass().getSimpleName()+"' is not supported");
		}
		
		throw new ServerException(remoteType +" Unsupported commmand type or parameters.");
	}
}
