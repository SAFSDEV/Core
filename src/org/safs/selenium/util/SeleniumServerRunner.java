/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.safs.tools.consoles.JavaJVMConsole;

/**
 * This class is used to provide a special Java console for the standalone Selenium Server process.
 * This is essential for at least two critical cases:
 * <ul>
 * <li>Launching a standalone Selenium Server on localhost from inside Eclipse.
 * <li>Launching a remote standalone Selenium Server supporting SeleniumPlus RMI.
 * </ul>
 * minimum args expected:<br>
 * <p>
 * -jar fullpath/to/selenium-server-standalone-&lt;version>.jar<br>
 * -timeout=N<br>
 * -browserTimeout=N
 * <p>
 * other optional args:<br>
 * <p>
 * "-port N" the port number for SeleniumServer. If not provided, the default port 4444 will be used<br>
 * -outputToConsole if provided, the console messages will also be printed to standard out/err.<br>
 * <p>
 * 
 * Other JVM params--including those needed by standalone Selenium Server ( -Dwebdriver...)--must also be 
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
 * When being used as the SeleniumPlus RMI bootstrap mechanism on a remote standalone Selenium Server, 
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
		setTitle("Selenium Server");
	}
	private SeleniumServerRunner(boolean outputToConsole){
		super(outputToConsole);
		setTitle("Selenium Server");
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
	 * -jar fullpath/to/selenium-server-standalone*.jar<br>
	 * -timeout=N<br>
	 * -browserTimeout=N<br>
	 * "-port N" the port number for SeleniumServer. If not provided, the default port 4444 will be used<br>
	 * -outputToConsole if provided, the console message will also be printed to standard out/err.<br>
	 * <p>
	 * Other JVM params--including those needed by standalone Selenium Server ( -Dwebdriver...)--must also be 
	 * provided and will have already been applied to this JVM process.
	 * <p>
	 * When being used as the SeleniumPlus RMI bootstrap mechanism on a remote standalone Selenium Server, 
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
		SeleniumServerRunner console;
		try{
			passArgs = processArgs(args);
			console = new SeleniumServerRunner(outputToConsole);
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
