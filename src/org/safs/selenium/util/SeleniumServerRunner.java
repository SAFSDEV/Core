/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.safs.tools.consoles.JavaJVMConsole;

/**
 * This class is used to provide a special Java console for the Selenium Server (standalone, hub, node) process.
 * This is essential for the following cases (server will be launched on local machine):
 * <ul>
 * <li>Launching a standalone Selenium Server.
 * <li>Launching a standalone Selenium Server supporting SeleniumPlus RMI.
 * <li>Launching a hub (in grid mode) Selenium Server.
 * <li>Launching a hub (in grid mode) Selenium Server supporting SeleniumPlus RMI.
 * <li>Launching a node (in grid mode) Selenium Server.
 * <li>Launching a node (in grid mode) Selenium Server supporting SeleniumPlus RMI.
 * </ul>
 * 
 * <b>Required</b> parameters:<br>
 * <b>-jar fullpath/to/selenium-server-standalone-&lt;version>.jar</b><br>
 * <b>-timeout=N</b> Controls how long (in seconds) the client is allowed to be gone before the session is reclaimed<br>
 * <b>-browserTimeout=N</b> Controls how long (in seconds) the browser is allowed to hang<br>
 * <p>
 * <b>Optional</b> parameters:<br>
 * <b>-port N</b>, optional, the port number for Selenium Server. If not provided, the default port will be used.
 *                   For "standalone" and "hub" the default port number is 4444; While for "node", it is 5555.
 *                   <br>
 * <b>-role TheServerRole</b>, optional, if not provided, a standalone server will be launched.<br>
 *                                         TheServerRole could be <b>"hub"</b>, and selenium server will be launched
 *                                         as a hub (in grid mode) for other node to connect.<br>
 *                                         TheServerRole could be <b>"node"</b>, and selenium server will be launched
 *                                         as a node (in grid mode) to connect a hub. <b>**Note**</b> Hub's information must also 
 *                                         be provided. Ex: <b>-role node -hub http://hub.machine:port/grid/register</b><br>
 *                                         <br>
 * <b>-outputToConsole</b>, optional, if provided, the console message will also be printed to standard out/err.<br>
 * <p>
 * 
 * Other JVM params--including those needed by Selenium Server ( -Dwebdriver...)--must also be 
 * provided and will have already been applied to this JVM process.
 * <p>
 * Consequently, a typical command-line invocation of this Java program would look like:
 * <p><pre>
 * &lt;pathTo>/bin/java 
 * 
 *     -Xms51m -Xmx1g (or -Xmx2g)
 *     -cp &lt;pathTo>/seleniumplus.jar;&lt;pathTo>/JSTAFEmbedded.jar;&lt;pathTo>selenium-server-standalone-&lt;version>.jar
 *     
 *     -Dwebdriver.log.file="webdriver.console" 
 *     -Dwebdriver.firefox.logfile="firefox.console" 
 *     -Dwebdriver.safari.logfile="safari.console" 
 *     -Dwebdriver.ie.logfile="ie.console" 
 *     -Dwebdriver.opera.logfile="opera.console" 
 *     -Dwebdriver.chrome.logfile="chrome.console" 
 *     -Dwebdriver.chrome.driver="&lt;pathTo/chromedriver.exe" 
 *     -Dwebdriver.ie.driver="&lt;pathTo/IEDriverServer.exe"
 *     
 *     org.safs.selenium.util.SeleniumServerRunner 
 *     
 *     -jar &lt;pathTo>/selenium-server-standalone-&lt;version>.jar;
 *     -timeout=20
 *     -browserTimeout=60
 * </pre>
 * When being used as the SeleniumPlus RMI bootstrap mechanism on a remote standalone/node Selenium Server, 
 * the following additional command-line arguments are necessary:
 * <p><pre>
 *     -Djava.rmi.server.hostname=&lt;full hostname or ip address>
 *     
 *       The correct hostname is imperative as later versions of Java seem to resolve the 
 *       our hostname to "localhost" or "127.0.0.1" which will cause connection failures on 
 *       remote systems trying to communicate with this RMI Server.)
 *
 *     One of the following:
 *     
 *     -safs.rmi.server  (no arg: default SeleniumPlus RMI Server)
 *     -safs.rmi.server &lt;full.package.classname>   (alternative/custom RMI Server)  
 *     -Dsafs.rmi.server &lt;full.package.classname>  (alternative/custom RMI Server)  
 *</pre>
 * @see org.safs.selenium.rmi.server.SeleniumRMIServer
 * @see org.safs.selenium.rmi.server.SeleniumServer
 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
 * @see org.safs.selenium.rmi.agent.SeleniumAgent
 */
public class SeleniumServerRunner extends JavaJVMConsole{

	protected static boolean isRMIServer = false;
	protected static String rmiServerClassname = null;

	/** "Selenium Server", The title will be shown. */
	public static final String TITLE = "Selenium Server";
	/** 'safs.rmi.server' */
	public static final String PROPERTY_RMISERVER = "safs.rmi.server";
	
	/** '-outputToConsole' */
	public static final String PARAM_OUTPUTCONSOLE = "-outputToConsole";
	/**
	 * Normally 'execution message' will be printed to JavaJVMConsole panel.<br>
	 * If outputToConsole is true, the 'execution message' will also be printed to standard out/err.<br>
	 */
	private static boolean outputToConsole = false;
	
	/** 'org.safs.selenium.rmi.server.SeleniumServer' */
	public static final String DEFAULT_RMISERVER_CLASSNAME = org.safs.selenium.rmi.server.SeleniumServer.class.getName();
	
	private SeleniumServerRunner(){
		super();
		setTitle(TITLE);
	}
	private SeleniumServerRunner(boolean outputToConsole){
		super(outputToConsole);
		setTitle(TITLE);
	}

    protected static String[] processArgs(String[] args) throws IOException{
    	ArrayList<String> list = new ArrayList<String>();
    	String arg = null;
    	final String jar = "-jar";
    	final String rmiflag = "-"+ PROPERTY_RMISERVER;
    	
    	if(System.getProperty(PROPERTY_RMISERVER) != null){
    		arg = System.getProperty(PROPERTY_RMISERVER);
    		if(arg.length()> 0){
    			rmiServerClassname = new String(arg);
    		}else{
    			rmiServerClassname = DEFAULT_RMISERVER_CLASSNAME;
    		}
    	}
    	for(int i=0; i< args.length;i++){
    		arg = args[i];
    		if(jar.equalsIgnoreCase(arg)){
    			if(++i < args.length){
    				arg = args[i];
    				addFile(arg);
    			}
    		}else if(rmiflag.equalsIgnoreCase(arg)){
    			// 2nd part of rmiserver arg is OPTIONAL
    			int i2 = i + 1;
    			if( i2 < args.length){
    				arg = args[i2];
    				// only use it if NOT a new different argument type
    	    		if(!arg.startsWith("-")){
    	    			i = i2;
    	    			rmiServerClassname = new String(arg);
    	    		}
    			}
    			if(rmiServerClassname == null)
        			rmiServerClassname = DEFAULT_RMISERVER_CLASSNAME;
    		}else if(PARAM_OUTPUTCONSOLE.equalsIgnoreCase(arg)){
    			outputToConsole = true;
    		}else{
    			list.add(arg);
    		}
    	}
    	return list.toArray(new String[]{});
    }
    
    /**
     * Start an RMI Server class specified on the command-line, if any.
     * If none was specified, then nothing happens.
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    protected static void launchRMIServer() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
    	if(!(rmiServerClassname == null)){
    		Class.forName(rmiServerClassname).newInstance();
    	}    	
    }

	/**
	 * Is expected to be able to run in its own standalone Java process.
	 * @param args expects:
	 * <p>
	 * <b>-jar fullpath/to/selenium-server-standalone*.jar</b><br>
	 * <b>-timeout=N</b> Controls how long (in seconds) the client is allowed to be gone before the session is reclaimed<br>
	 * <b>-browserTimeout=N</b> Controls how long (in seconds) the browser is allowed to hang<br>
	 * <b>-port N</b>, optional, the port number for Selenium Server. If not provided, the default port will be used.
	 *                   For "standalone" and "hub" the default port number is 4444; While for "node", it is 5555.
	 *                   <br>
	 * <b>-role TheServerRole</b>, optional, if not provided, a standalone server will be launched.<br>
	 *                                         TheServerRole could be <b>"hub"</b>, and selenium server will be launched
	 *                                         as a hub (in grid mode) for other node to connect.<br>
	 *                                         TheServerRole could be <b>"node"</b>, and selenium server will be launched
	 *                                         as a node (in grid mode) to connect a hub. <b>**Note**</b> Hub's information must also 
	 *                                         be provided. Ex: <b>-role node -hub http://hub.machine:port/grid/register</b><br>
	 *                                         <br>
	 * <b>-outputToConsole</b>, optional, if provided, the console message will also be printed to standard out/err.<br>
	 * <p>
	 * Other JVM params--including those needed by standalone Selenium Server ( -Dwebdriver...)--must also be 
	 * provided and will have already been applied to this JVM process.
	 * <p>
	 * When being used as the SeleniumPlus RMI bootstrap mechanism on a remote standalone/node Selenium Server, 
	 * the following additional command-line arguments are necessary:
	 * <p><pre>
	 *     -Djava.rmi.server.hostname=&lt;full hostname or ip address>
	 *     
	 *       The correct hostname is imperative as later versions of Java seem to resolve the 
	 *       our hostname to "localhost" or "127.0.0.1" which will cause connection failures on 
	 *       remote systems trying to communicate with this RMI Server.)
	 *
	 *     One of the following:
	 *     
	 *     -safs.rmi.server  (no arg: default SeleniumPlus RMI Server)
	 *     -safs.rmi.server &lt;full.package.classname>   (alternative/custom RMI Server)  
	 *     -Dsafs.rmi.server &lt;full.package.classname>  (alternative/custom RMI Server)  
	 *</pre>
	 */
	public static void main(String[] args) {
		String[] passArgs = new String[0];
		try{
			passArgs = processArgs(args);
			SeleniumServerRunner console = new SeleniumServerRunner(outputToConsole);
			launchRMIServer();
			Class aclass = Class.forName("org.openqa.grid.selenium.GridLauncher");
			Method main = aclass.getMethod("main", String[].class);
			main.invoke(null, new Object[]{passArgs});
		}catch(IOException io){
			io.printStackTrace();
		}catch(ClassNotFoundException cnf){
			cnf.printStackTrace();
		} catch (SecurityException s) {
			s.printStackTrace();
		} catch (NoSuchMethodException nsm) {
			nsm.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}				
	}
}
