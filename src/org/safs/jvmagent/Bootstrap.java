/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

/**
 * This class represents our Assistive Technologies Enabler for Java JVMs.
 * <p>
 * The Bootstrap class will instantiate our {@link AgentClassLoader} which will then be 
 * responsible for loading all classes needed by our Java Enabler.
 * <p>
 * As in all assistive technology bootstrap loaders, this class must be entered 
 * in the comma delimited list of assistive_technologies per 
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/guide/access/properties.html" target=_blank>Accessibility Properties</a>} 
 * standards.  Most simply, adding the appropriate entry to the file:
 * <p><ul>    
 *     (java.home)/lib/accessibility.properties
 * </ul>
 * <p>
 * This file is expected to be located or created in the (java.home)/lib directory of each and 
 * every Java JRE\SDK that is expected to use these Java Enablers. 
 * <p>
 * The entry in the file will look like this:
 * <p><ul>
 *     assistive_technologies=org.safs.jvmagent.Bootstrap
 * </ul>
 * <p>
 * There are also some rumors that an 'accessibility.properties' file may be placed in the 
 * "users home directory" for those cases when the user does not have write access to the 
 * Java JRE directories.  The Java system property "java.ext.dir" may also allow the user to 
 * place the required safsjvmagent.jar and properties files in directories writable by the 
 * user, but this has not been tested or verified.  Consult Java support resources provided 
 * by Sun and others if you are faced with these types of issues.
 * <p>
 * As mentioned, multiple assistive technology entries are separated by commas. 
 * <p>
 * This Bootstrap class and a minimum set of associated classes are expected to reside in 
 * the (java.home)/lib/ext directory of each and every Java JRE\SDK that is expected to use these 
 * Java enablers.
 * <p>
 * Copy the following files from SAFS/lib to the (java.home)/lib/ext directories:
 * <p><ul>
 * <li>safsjvmagent.jar
 * <li>safsjvmagent.properties
 * </ul>
 * <p>
 * The 'safsjvmagent.properties' file is expected to contain a 'safs.jvmagent.classpath' setting 
 * containing the search paths to SAFS, STAF and other Jar file dependencies.  Review and edit 
 * this file as necessary to provide accurate classpath information. An example is below.<br>
 * Note the reversed file separator character used even on Windows:
 * <p><ul>
 *     safs.jvmagent.classpath=c:/safs/lib/jaccess.jar;c:/safs/lib/safs.jar;c:/safs/lib/jakarta-regexp-1.3.jar;c:/staf/bin/JSTAF.jar
 * </ul>
 * <p>
 * Additional agents or alternative agents can be specified in another 
 * comma-separated list property as the example below shows:
 * <p><ul>
 *     safs.jvmagent.classes=org.safs.abbot.jvmagent.JVMAgent,altpackage.AnotherCustomAgent
 * </ul>
 * <p>
 * If this property is not present, the DEFAULT_AGENT ({@link org.safs.rmi.engine.AgentImpl}) alone will be launched.
 * <p>
 * Each additional agent will be launched and should spawn its own separate threads 
 * as needed.  
 * <p>
 * Future multi-platform installers should automate the proper generation of this 
 * 'safsjvmagent.properties' file and detecting and enabling Java JVMs.
 * <p>
 * The 'safsjvmagent.jar' file contains a default 'safsjvmagent.properties' 
 * file.  But this may only work on Windows systems using the default installation 
 * options.  If desired, changes made to the local 'safsjvmagent.properties' can 
 * be merged or updated into the JAR file.  Any such JAR updates should be copied 
 * into all enabled JVMs that might be used by the application(s) to be tested.
 * However, it is easier to simply provide a separate, editable properties file 
 * in each JDK\lib\ext directory.
 * <p>
 * @see <A href="http://java.sun.com/j2se/1.4.2/docs/guide/access/index.html" target="_sundoc">Java Accessibility</A>
 * @see AgentClassLoader
 * @see org.safs.rmi.engine.AgentImpl
 * @see org.safs.abbot.jvmagent.JVMAgent
 * @author Carl Nagle 2005.01.06 Original Release
 * @author Carl Nagle 2005.01.18 Enabled standalone 'safsjvmagent.properties' files
 * @author Carl Nagle 2005.01.20 Added 'safs.jvmagent.classes' property to allow multiple/custom Agents
 * @author Carl Nagle 2005.06.01 Added documentation.
 * @author Carl Nagle 2005.08.04 Corrected documentation for 'accessibility.properties'
 */
public class Bootstrap {

	/** 
	 * 'org.safs.rmi.engine.AgentImpl' 
	 * The default Agent instanced by the Bootstrap AgentClassLoader.  Additional 
	 * or alternative agent classes can be specified with the safs.jvmagent.classes 
	 * property value in the safsjvmagent.properties file.
	 */
	public static final String DEFAULT_AGENT = "org.safs.rmi.engine.AgentImpl";

	/** 
	 * 'safsjvmagent.properties' 
	 * This is the properties file containing values used by this class.  It is 
	 * expected to reside in the same directory as the JAR file containing this 
	 * class.  This is normally the JDK\lib\ext directory for Java extensions.
	 */
	public static final String DEFAULT_AGENT_PROPERTIES = "safsjvmagent.properties";

	/** 
	 * 'safs.jvmagent.classpath' 
	 * This is the property name in the safsjvmagent.properties file which contains 
	 * the classpath the AgentClassLoader is expected to use when loading classes.
	 */
	public static final String AGENT_CLASSPATH = "safs.jvmagent.classpath";

	/** 
	 * 'safs.jvmagent.classes' 
	 * This is the property name in the safsjvmagent.properties file which can contain 
	 * a comma-separated list of Agent-type classes for this class to launch.
	 * */
	public static final String AGENT_CLASSES = "safs.jvmagent.classes";

	private String classPath            = null;	
	private AgentClassLoader sysLoader  = null;
	private ClassLoader      extLoader  = null;
	private Properties       properties = null;
	
	/**
	 * Default Assistive Technologies constructor.
	 * The Java JVM Extension Mechanism will automatically create an instance of 
	 * this class.  The constructor will load up necessary values from the 
	 * safsjvmagent.properties file and then instance the AgentClassLoader.
	 * The AgentClassLoader will then be tasked to instance every type of Agent class
	 * listed in the properties file.  
	 * If no classes are listed then the DEFAULT_AGENT will be instanced.
	 */
	public Bootstrap() {
		super();
	    logInfo("SAFS Testing Agent attempting to load...");

		extLoader = getClass().getClassLoader();
		
		try{
			CodeSource jar = getClass().getProtectionDomain().getCodeSource();
			String jarpath = jar.getLocation().getFile();
			if (Platform.isWindows()) jarpath = jarpath.replaceAll("%20", " ");
			
			String lcjarpath = jarpath.toLowerCase();
			String proppath = null;
			String rootfile = null;
			String logpath = null;
			
			if ((lcjarpath.endsWith(".jar"))||
			    (lcjarpath.endsWith(".zip"))){
			    
			    // remove ".jar" from filepath
			    rootfile = jarpath.substring(0, jarpath.length()-4);
			    proppath = rootfile +".properties";
			    logpath = rootfile  +".log";

				openLog(logpath);
				logInfo("SAFS Testing Agent JAR path: ("+ jarpath.length() +") "+ jarpath);
				logInfo("SAFS Testing Agent PROP path: ("+ proppath.length() +") "+ proppath);

				properties = new Properties();
				properties.load((new URL("file:"+ proppath)).openStream());
			}else{
				logInfo("SAFS Testing Agent JAR path is not a valid JAR or ZIP...");
				throw new NullPointerException();
			}
		}
		catch(Throwable th){
			logInfo(
			    "SAFS Testing Agent resorting to JAR embedded properties file...\n" +
				"SAFS Testing Agents may not be available for this JVM.");
			properties = new Properties();
			try{ properties.load(extLoader.getResourceAsStream(DEFAULT_AGENT_PROPERTIES));}
			catch(IOException io){
				logInfo(
					"SAFS Testing Agent could not load required '"+ DEFAULT_AGENT_PROPERTIES +"' file.\n" +
					"SAFS Testing Agents will not be available for this JVM.");
				closeLog();
				return;}
			catch(NullPointerException np){
				logInfo(
					"SAFS Testing Agent did not find required '"+ DEFAULT_AGENT_PROPERTIES +"' file.\n" +
					"SAFS Testing Agents will not be available for this JVM.");
				closeLog();
				return;}			
		}
		
		classPath  = properties.getProperty(AGENT_CLASSPATH);
		if (classPath == null) {
			logInfo(
				"SAFS Testing Agent defaulting to java.class.path \n" +
				"SAFS Testing Agents may not be available for this JVM.");
			classPath = System.getProperty("java.class.path");
		}

		sysLoader = new AgentClassLoader(classPath, extLoader);
		
		String nextAgent = null;
		Class cagent = null;
		Object oagent = null;
		
		String agentClasses = properties.getProperty(AGENT_CLASSES);
		if ((agentClasses == null)||(agentClasses.length()==0)) 
		    agentClasses = DEFAULT_AGENT;
		
		StringTokenizer toker = new StringTokenizer(agentClasses, ",");
		
		while(toker.hasMoreTokens()){
			nextAgent = toker.nextToken().trim();
			try{			
				cagent = sysLoader.loadClass(nextAgent);
			    oagent = cagent.newInstance();
			    logInfo("SAFS Testing Agent '"+nextAgent+"' has successfully been loaded.");
			}
			catch(Throwable cnf){
			    logInfo(
			    	"SAFS Testing Agent '"+nextAgent+"' class loading or initialization error.\n"+
					"Reported Error:"+ cnf.getClass().getName() +"\n"+ 
					cnf.getMessage() +"\n"+
			    	"Using CLASSPATH:\n"+ classPath +"\n" +
					"SAFS Testing Agent '"+nextAgent+"' may not be available for this JVM.");
			}
		}
		closeLog();
	}
	
	BufferedWriter log = null;
	
	void openLog(String path){
		try{ log = new BufferedWriter(new FileWriter(path)); }
		catch(Exception x){
			System.err.println("Unable to open SAFS Bootstrap log:"+ path);
			System.err.println("Error: "+ x.getMessage());
		}
	}
	
	void logInfo(String message){
		//System.out.println(message);
		try{
			log.write(message);
			log.newLine();
			log.flush();
		}catch(Exception x){
			System.out.println(message);
		}
	}
	
	void closeLog(){
		try{ 
			log.flush();
			log.close();
		}catch(Exception x){;}
	}	

	protected void finalize(){
		closeLog();
	}	
}