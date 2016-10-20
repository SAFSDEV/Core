/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer History:
 * OCT 19, 2016	(sbjlwa) Provided ability to set the console window's state.
 * 
 */
package org.safs.selenium.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.safs.StringUtils;
import org.safs.tools.consoles.JavaJVMConsole;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

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
 * <b>-state MAX|MIN|NORMAL</b>, optional, the console windows will be maximized, minimized or as it is.<br>
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
 * 
 * @author canagl
 */
public class SeleniumServerRunner extends JavaJVMConsole{

	protected static boolean isRMIServer = false;
	/** The default value is null, so the console start without RMI server. */
	protected static String rmiServerClassname = null;

	/** "Selenium Server", The title will be shown. */
	public static final String TITLE = "Selenium Server";
	
	/** '-outputToConsole' specifies if the 'execution message' will also be printed to standard out/err. */
	public static final String PARAM_OUTPUTCONSOLE = "-outputToConsole";
	/** '-jar' specifies the jar file to put dynamically in the classpath.<br>
	 * -jar fullpath/to/selenium-server-standalone*.jar<br> */
	public static final String PARAM_JAR = "-jar";
	/** '-safs.rmi.server' specifies the name of RMI server class to start with.<br>
	 * '-safs.rmi.server', without value the default RMI {@link DriverConstant#DEFAULT_RMISERVER_CLASSNAME} will be used.<br>
	 * '-safs.rmi.server full.pacakge.RMIServer', 'full.pacakge.RMIServer' will start with this console.<br>
	 *  */
	public static final String PARAM_RMI = "-"+ DriverConstant.PROPERTY_RMISERVER;
	
	/**
	 * Normally 'execution message' will be printed to JavaJVMConsole panel.<br>
	 * If outputToConsole is true, the 'execution message' will also be printed to standard out/err.<br>
	 */
	private static boolean _paramOutputToConsole = false;

	/** Will be set to the value of parameter "-state stateValue", the default is "Normal". */
	private static String _paramState = STATE_NORMAL;
	
	private SeleniumServerRunner(){
		super();
	}
	private SeleniumServerRunner(boolean outputToConsole){
		super(outputToConsole);
	}
	private SeleniumServerRunner(boolean outputToConsole, String state){
		super(outputToConsole, state);
	}
	
	public void init(){
		super.init();
		setTitle(TITLE);
	}

	/**
	 * Check the VM properties.<br>
	 * This should be called before analyzing "program parameters", which has higher priority.<br>
	 * @see #processArgs(String[])
	 */
	private static void checkSystemProperty(){
		String value = null;

		value = System.getProperty(DriverConstant.PROPERTY_RMISERVER);
		if(value!=null){
			if(!value.trim().isEmpty()){
				rmiServerClassname = value;
			}else{
				rmiServerClassname = DriverConstant.DEFAULT_RMISERVER_CLASSNAME;
			}
		}
		
		value = System.getProperty(SeleniumConfigConstant.PROPERTY_CONSOLE_STATE);
		if(StringUtils.isValid(value)){
			_paramState = value;
		}
		
	}
	
    protected static String[] processArgs(String[] args) throws IOException{
    	ArrayList<String> list = new ArrayList<String>();
    	String arg = null;

    	checkSystemProperty();
    	
    	for(int i=0; i< args.length;i++){
    		arg = args[i];
    		if(PARAM_JAR.equalsIgnoreCase(arg)){
    			if(++i < args.length){
    				arg = args[i];
    				addFile(arg);
    			}
    		}else if(PARAM_RMI.equalsIgnoreCase(arg)){
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
        			rmiServerClassname = DriverConstant.DEFAULT_RMISERVER_CLASSNAME;
    		}else if(PARAM_OUTPUTCONSOLE.equalsIgnoreCase(arg)){
    			_paramOutputToConsole = true;
    		}else if(PARAM_STATE.equalsIgnoreCase(arg)){
    			if(i+1<args.length){
    				_paramState = args[++i];
    			}

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
	 * <b>-state MAX|MIN|NORMAL</b>, optional, the console windows will be maximized, minimized or as it is.<br>
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
			SeleniumServerRunner console = new SeleniumServerRunner(_paramOutputToConsole, _paramState);
			console.init();
			launchRMIServer();
			Class<?> aclass = Class.forName("org.openqa.grid.selenium.GridLauncher");
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
