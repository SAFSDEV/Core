/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * Developer History:
 * OCT 19, 2016	(Lei Wang) Provided ability to set the console window's state.
 * JUL 31, 2017	(Lei Wang) Properly load GridLauncher. If Selenium is version 3, then
 *                       handle "VM parameters" passed in as program parameter
 *                       split -timeout=20 and -browserTimeout=60, and pass them separately as parameter
 * AUG 08, 2017	(Lei Wang) Added static method isSelenium3X(): load Selenium GridLauncher, initialize field 'isSelenium3X'.
 *
 */
package org.safs.selenium.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.safs.IndependantLog;
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
 * @author Carl Nagle
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

	/** "-D" */
	public static final String PARAM_PREFIX_FOR_VM	 	= "-D";

	/** "-timeout" */
	public static final String PARAM_TIMEOUT 			= "-timeout";
	/** "-browserTimeout" */
	public static final String PARAM_BROWSER_TIMEOUT 	= "-browserTimeout";
	/** "=" */
	public static final String TIMEOUT_ASSIGN_SYMBOL 	= "=";

	/** -timeout=20 */
	public static final String PARAM_TIMEOUT_DEFAULT			= PARAM_TIMEOUT+TIMEOUT_ASSIGN_SYMBOL+SeleniumConfigConstant.DEFAULT_TIMEOUT;
	/** -browserTimeout=60 */
	public static final String PARAM_BROWSER_TIMEOUT_DEFAULT 	= PARAM_BROWSER_TIMEOUT+TIMEOUT_ASSIGN_SYMBOL+SeleniumConfigConstant.DEFAULT_BROWSER_TIMEOUT;

	/**
	 * If the GridLauncher has been loaded from the Selenium 3.X
	 */
	private static boolean isSelenium3X 		= false;
	/**
	 * The loaded GridLauncher class, it can be null if not loaded.
	 */
	private static Class<?> gridLauncherClass = null;

	/** "org.openqa.grid.selenium.GridLauncher" */
	public static final String GRID_LAUNCHER_CLASS_SELENIUM2 = "org.openqa.grid.selenium.GridLauncher";
	/** "org.openqa.grid.selenium.GridLauncherV3" */
	public static final String GRID_LAUNCHER_CLASS_SELENIUM3 = "org.openqa.grid.selenium.GridLauncherV3";

	public static synchronized boolean isSelenium3X(){
		String debugmsg = "SeleniumServerRunner.isSelenium3X(): ";
		//Firstly load the GridLuancher
		if(gridLauncherClass==null){
			try{
				gridLauncherClass = Class.forName(GRID_LAUNCHER_CLASS_SELENIUM3);
				isSelenium3X = true;
			}catch(ClassNotFoundException e){
				IndependantLog.warn(debugmsg+" Met "+e.toString());
			}
		}

		if(gridLauncherClass==null){
			try {
				gridLauncherClass = Class.forName(GRID_LAUNCHER_CLASS_SELENIUM2);
				isSelenium3X = false;
			} catch (ClassNotFoundException e) {
				gridLauncherClass = null;
				String errorMsg = "Could NOT load Selenium Grid Launcher!";
				IndependantLog.error(debugmsg+errorMsg+" Met "+e.toString());
				throw new TypeNotPresentException(errorMsg, e);
			}
		}

		return isSelenium3X;
	}

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

	@Override
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
    			if(isSelenium3X){
    				if(arg.startsWith(PARAM_TIMEOUT) ||
    					arg.startsWith(PARAM_BROWSER_TIMEOUT)){
    					//Started from Selenium 3.X, it does not accept "-timeout=N", but it accepts "-timeout N" without the "="
    					String[] keyAndValue = arg.split(TIMEOUT_ASSIGN_SYMBOL);
    					try{
    						list.add(keyAndValue[0]);
    						list.add(keyAndValue[1]);
    					}catch(Exception e){
    						IndependantLog.warn("Failed to add parameter '"+arg+"', due to "+e.toString());
    					}
    				}else if(arg.startsWith(PARAM_PREFIX_FOR_VM)){
    					//Started from Selenium 3.X, it does not accept "-Dvm.parameter=value" as program parameter
    					//We copied code from org.openqa.selenium.server.SeleniumServer (Selenium 2.52) and handle the VM parameter here.
    					setSystemProperty(arg);
    				}else{
    					list.add(arg);
    				}
    			}else{
    				list.add(arg);
    			}
    		}
    	}
    	return list.toArray(new String[]{});
    }

    //Copied from org.openqa.selenium.server.SeleniumServer (Selenium 2.52)
    //To handle -Dvm.parameter=value
    private static void setSystemProperty(String arg) {
    	if (arg.indexOf('=') == -1) {
    		System.err.println("poorly formatted Java property setting (I expect to see '=') " + arg);
    		System.exit(1);
    	}
    	String property = arg.replaceFirst("-D", "").replaceFirst("=.*", "");
    	String value = arg.replaceFirst("[^=]*=", "");
    	IndependantLog.debug("SeleniumServerRunner.setSystemProperty(): Setting system property " + property + " to " + value);
    	System.setProperty(property, value);
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

		//Firstly load the GridLuancher
		try{
			isSelenium3X();
		}catch(TypeNotPresentException e){
			return;
		}

		try{
			passArgs = processArgs(args);
			SeleniumServerRunner console = new SeleniumServerRunner(_paramOutputToConsole, _paramState);
			console.init();
			launchRMIServer();
			Method main = gridLauncherClass.getMethod("main", String[].class);
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
