package org.safs.tools.engines;

import org.safs.SAFSException;
import org.safs.TestRecordHelper;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.consoles.ProcessConsole;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.staf.STAFProcessHelpers;
import com.ibm.staf.STAFResult;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * A wrapper to the IBM Rational Functional Tester SAFS engine--the "RobotJ" engine.
 * This engine can only be used if you have a valid, licensed installation of 
 * IBM Rational Functional Tester (a.k.a. XDE Tester and--before that--RobotJ.)<br>
 * We often refer to IBM Rational Functional Tester as "RFT".
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any 
 * command-line options to configure RFT.  All configuration information must 
 * be provided in config files.  By default, these are SAFSTID.INI files.  See 
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile" target="_blank">SAFSTID Config Fields</a>} 
 * for more information.
 * <p>
 * The RFT supported config file items:
 * <p>
 * <ul><pre>
 * <b>[SAFS_ROBOTJ]</b>
 * AUTOLAUNCH=FALSE            Defaults to FALSE because config info must be valid.
 * DATASTORE=&lt;DatastoreJ>      Root directory of RobotJ Project/Datastore. Location of RobotJ scripts.
 * 
 * ;optional args follow
 * ;--------------------
 * ;PLAYBACK=TestScript        RobotJ script to use as SAFS Hook. (HOOK, HOOKSCRIPT)
 * ;STAFID="SAFS/ROBOTJ"       Normally never used.
 * ;JVM=JVMpath                Full path to desired Java executable if "java" is not sufficient.
 * ;JVMARGS=JVM Args           Ex: "-Xms512m -Xmx512m", will be used unmodified.
 * ;CLASSPATH=altClasspath     Generally overrides system classpath.
 * ;INSTALLDIR=&lt;wswpluginPath> [-V7]Directory in Rational where rational_ft.jar resides.
 * ;                           Do not use INSTALLDIR if using JARPATH and PROJECTPATH.
 * ;JARPATH=&lt;FunctionalTester\bin> [V7+]Full file path to rational_ft.jar resides.
 * ;PROJECTPATH=&lt;CLASSPATH>    [V7+]CLASSPATH syntax to all needed SAFS\STAF JARS.
 * ;                           Do not use JARPATH and PROJECTPATH if using INSTALLDIR.
 * ;XBOOTCLASSPATH=&lt;CLASSPATH>    [V8+]CLASSPATH needed by RFT when launching with -jar rational_ft.jar.
 * ;PROJECT=&lt;Project.rsp>      Full path to Rational .rsp file for the Rational Project.
 * ;BUILD=&lt;Build #>            Build # associated with this Rational Project TestManager.
 * ;LOGFOLDER=Default          The name of the root logfolder in the Rational Project.
 * ;LOG=TestScript             The name of the TestManager log to use.
 * ;USERID=&lt;userid>            Login info for the Rational Project.
 * ;PASSWORD=                  Login info for the Rational Project.
 * ;TIMEOUT=45                 AutoLaunch timeout value in seconds. Default 45 seconds.
 * ;OPTIONS=                   Script -args to be appended, if any. Normally never used.
 * 
 * SECSWaitForWindow=30        Timeout in seconds to look for Windows
 * SECSWaitForComponent=30     Timeout in seconds to look for Window Components
 * CommandLineBreakpoint=TRUE  Set option to TRUE or FALSE
 * TESTDOMAINS=Java,Html,Win,Net,Swt,Flex   Limit the number of domains to search
 * ClearProxiesAlways=True     Always Clear reference caches. Force component search for every Action.
 * RFSMOnly=Yes				   Run RFSM Search Mode exclusively(recognition string doesn't need :RFSM: prefix). 
 * RFSMCache=Yes			   Cache RFSM Search Mode components object(Child object). Main object(main window object) caches by default.
 * </pre></ul>
 * <p>
 * Note, the majority of these options are necessary to satisfy the command-line options 
 * for RFT ONLY if the project has been associated with a IBM Rational Test Manager project.  
 * Consult the RFT command-line options documentation for more details and valid settings.
 * <p>
 * As of RFT V7, if there is no Test Manager association and logging, then we can usually only 
 * need the AUTOLAUNCH and DATASTORE settings in the INI file.
 * <p>
 * Uses the following logic to deduce some settings:
 * <p><dl>
 * <dt><b>INSTALLDIR and JARPATH</b>
 * <p>
 * <dd>The use of INSTALLDIR and JARPATH are mutually exclusive.  
 * JARPATH launches RFT using the -JAR rational_ft.jar option and REQUIRES the use of 
 * PROJECTPATH.  However, if neither INSTALLDIR nor JARPATH are provided then we attempt 
 * to deduce the JARPATH mechanism of launching RFT and build JARPATH and PROJECTPATH 
 * dynamically.
 * <p>
 * <dt><b>AUTOLAUNCH</b>
 * <p>
 * <dd>True if set to "TRUE", "YES", "ON", or "1".  Not case-sensitive.
 * <p>
 * <dt><b>JVM</b>
 * <p>
 * <dd>If not provided by config file we try to locate the java executable of 
 * "./jdk/jre/bin/java.exe" from one of these settings, if found:
 * <p><ol>
 * <li>System.getenv Variable name: IBM_RATIONAL_RFT_ECLIPSE_DIR
 * <li>System getProperty Variable name: IBM_RATIONAL_RFT_ECLIPSE_DIR
 * <li>System getProperty Variable name: IBM.RATIONAL.RFT.ECLIPSE.DIR
 * </ol>
 * <p>
 * Some 8.x versions of RFT (perhaps all?) still contain their own Java 1.6 JDK.  
 * For runtime execution with SAFS we now use Java 1.7. at bundles in directory SAFS\jre.  
 * <p>
 * If RFT will not launch 
 * successfully using our default launch algorithms pointing to what we think is 
 * the right JDK/JRE, the user may have to provide this JVM parameter to point to a 
 * JDK/JRE that will work for their case.
 * <p>
 * <dt><b>JVMARGS</b>
 * <p>
 * <dd>Any JVM options to pass to the Java JVM as part of its invocation.
 * <p><ul>Ex: JVMARGS="-Xms512m -Xmx1024m"</ul> 
 * <p>
 * <dt><b>JARPATH</b>
 * <p>
 * <dd>If not provided by config file we try to locate the rational_ft.jar 
 * from one of these settings, if found:
 * <p><ol>
 * <li>System.getenv Variable name: IBM_RATIONAL_RFT_INSTALL_DIR
 * <li>System getProperty Variable name: IBM_RATIONAL_RFT_INSTALL_DIR
 * <li>System getProperty Variable name: IBM.RATIONAL.RFT.INSTALL.DIR
 * </ol>
 * <p>
 * <dt><b>PROJECTPATH</b>
 * <p>
 * <dd>Used with JARPATH.  If not provided by config file we try 
 * to build this using Environment variables to validate the existence of STAF and 
 * SAFS JAR files dynamically:
 * <p><ol>
 * <li>System.getenv Variable name: SAFSDIR
 * <li>System.getenv Variable name: STAFDIR
 * <p>
 * <li>Required JAR file: STAFDIR/bin/jstaf.jar
 * <p>
 * <li>Required JAR file: SAFSDIR/lib/safs.jar
 * <li>Required JAR file: SAFSDIR/lib/jakarta-regexp-1.3.jar
 * <li>Required JAR file: SAFSDIR/lib/safsrational_ft.jar
 * <li>Required JAR file: SAFSDIR/lib/safsrational_ft_enabler.jar
 * <li>Required JAR file: SAFSDIR/lib/jai_core.jar
 * <li>Required JAR file: SAFSDIR/lib/jai_codec.jar
 * <li>Required JAR file: SAFSDIR/lib/jai_imageio.jar
 * <li>Required JAR file: SAFSDIR/lib/jna.zip
 * <li>Required JAR file: SAFSDIR/lib/safscust.jar
 * <li>Optional JAR file: SAFSDIR/lib/safsdebug.jar
 * 
 * </ol>
 * <p>
 * <dt><b>XBOOTCLASSPATH</b>
 * <p>
 * <dd>Used to add required JAR libs to RFT JVM when -jar rational_ft is used to launch RFT.    
 * The normal launch mechanism already provides a default XBOOTCLASSPATH.  The user only uses this option 
 * if they need to add more JARS to those used by default.  If the user provides the setting, they 
 * must include all required JARS in the path since the setting will override the one provided by default.
 * <p><ul>Example:
 * <p>XBOOTCLASSPATH="c:\safs\lib\jai_core.jar;c:\safs\lib\jai_codec.jar;c:\safs\lib\jai_imageio.jar;others..." 
 * </dl>
 * <p>
 * Other settings are not deduced and must be provided as necessary for your configuration.
 * <p>
 * This routine appends an -args parameters to the TestScriptHelper hook in the form of:
 * 
 *    -args safs.config.paths=&lt;paths to chained config files>
 *    
 * TestScriptHelper takes this value and stores it as a JVM System.property, "safs.config.paths".
 *     
 * @author Carl Nagle DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author Carl Nagle JAN 14, 2008 Refactored for RFT V7 AUTOLAUNCH.
 * @author Carl Nagle AUG 07, 2008 Show support for new options SecsWaitForWindow, SecsWaitForComponent,
 *                             CommandLineBreakpoint, and TestDomains.
 * @author Carl Nagle APR 22, 2009 Doc update for ClearProxiesAlways.
 * @author Carl Nagle AUG 14, 2009 Support for JVM Args during AutoLaunch.
 * @author JunwuMa FEB 26 2010 Added XBOOTCLASSPATH for JVM searching JAI/JAI Imageio during AutoLaunch.  
 * @author JunwuMa APR 02 2010 Removed clibwrapper_jiio.jar from XBOOTCLASSPATH.
 * @author Carl Nagle JAN 20, 2016 Fixes for launching RFT with SAFS JRE.
 */
public class SAFSROBOTJ extends GenericEngine {

	/** 
	 * "SAFS/RobotJ" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME = "SAFS/RobotJ";

	/** 
	 * "com.rational.test.ft.rational_ft" -- The Rational class that is RobotJ/XDE Tester. 
	 */
	static final String HOOK_CLASS  = "com.rational.test.ft.rational_ft";

	/** 
	 * "TestScript" -- The default script run by the engine. 
	 */
	static final String HOOK_SCRIPT  = "TestScript";
	
	static final String HOOK_OPTION  = "HOOK";
	static final String HOOKSCRIPT_OPTION  = "HOOKSCRIPT";
	static final String PLAYBACK_OPTION  = "PLAYBACK";
	static final String JVMARGS_OPTION  = "JVMARGS";
	static final String XBOOTCLASSPATH_OPTION  = "XBOOTCLASSPATH";

	/**
	 * Constructor for SAFSROBOTJ.  Call launchInterface with an appropriate DriverInterface 
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSROBOTJ() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSROBOTJ.
	 */
	public SAFSROBOTJ(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/* return necessary jars delimited with ';' for RJ JVM as Bootclasspath
	 */
	private String getExtJarsForBootcp(){
		// jars needed for supporting more image formats. TIF and PNM are required by OCR. 
		String extjars[] = {
				"jai_core.jar", "jai_codec.jar", "jai_imageio.jar" /*,"clibwrapper_jiio.jar"*/		
		};
    	String safsdir = System.getenv("SAFSDIR");
    	if(safsdir==null){
			Log.error(ENGINE_NAME +" PROJECTPATH SAFSDIR could not be deduced.");
			return null;
    	}
    	String bootclasspath = "";
		for (int i = 0; i<extjars.length; i++) {
			CaseInsensitiveFile afile = new CaseInsensitiveFile(safsdir +"/lib/" +extjars[i]);	
		   	if(! afile.isFile()){
				Log.error(ENGINE_NAME +" PROJECTPATH to JAI_CORE.JAR " + extjars[i] + "could not be validated.");
				return null;
		   	}
		   	bootclasspath += afile.getAbsolutePath() + ";";
		}
		return bootclasspath;
	}
	/**
	 * Extracts or deduces configuration information and launches RFT in a new process.
	 * 
	 * @author Carl Nagle AUG 26, 2005 Added support for PLAYBACK and HOOKSCRIPT along with HOOK
	 * @author Carl Nagle JAN 14, 2008 Added support RFT V7 and deducing settings.
	 * @author Carl Nagle JAN 20, 2016 Added support for SAFSDIR embedded JRE usage.
	 * @see GenericEngine#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);
		try{ 
			// see if we are already running
			// launch it if our config says AUTOLAUNCH=TRUE and it is not running
			// otherwise don't AUTOLAUNCH it.
			if( ! isToolRunning()){
	
				Log.info(ENGINE_NAME +" is not running. Evaluating AUTOLAUNCH...");
				
				//check to see if AUTOLAUNCH exists in ConfigureInterface
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
           				                              "AUTOLAUNCH");

				if (setting==null) setting = "";
	
				// launch it if we dare!
				if ((setting.equalsIgnoreCase("TRUE"))||
				    (setting.equalsIgnoreCase("YES")) ||
				    (setting.equalsIgnoreCase("ON"))  ||
				    (setting.equalsIgnoreCase("1"))){

					Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

					String array = "";
					
					String tempstr = null;

					// JVM	
				    String jvm = "java";				    
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, "JVM");
				    if (tempstr != null) {
				    	CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr);
				    	if(! afile.isFile()){
							Log.error(ENGINE_NAME +" Option 'JVM' Java executable path is invalid: "+ tempstr);
							return;
				    	}
				    	jvm=makeQuotedPath(tempstr, true);
				    //try to derive from SAFS
				    }
				    if(tempstr == null){
			    		tempstr = System.getenv("SAFSDIR");
			    		if(tempstr != null){
					    	CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr +"/jre/bin/java.exe");
					    	if(afile.isFile()){
								Log.info(ENGINE_NAME +" SAFSDIR embedded JRE detected at "+ afile.getCanonicalPath());
								jvm=makeQuotedPath(afile.getCanonicalPath(), true);
					    	}else{
								Log.warn(ENGINE_NAME +" SAFSDIR embedded JRE was not detected at "+ afile.getCanonicalPath());
								tempstr = null;
					    	}
			    		}
				    }
				    if(tempstr == null){			    		
			    		tempstr = System.getenv("IBM_RATIONAL_RFT_ECLIPSE_DIR");
			    		if (tempstr == null) tempstr = System.getProperty("IBM_RATIONAL_RFT_ECLIPSE_DIR");
			    		if (tempstr == null) tempstr = System.getProperty("IBM.RATIONAL.RFT.ECLIPSE.DIR");
			    		if (tempstr != null){
			    			CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr +"/jdk/jre/bin/java.exe");
					    	if(! afile.isFile()){
								Log.generic(ENGINE_NAME +" Java JVM path could not be found in deduction path: "+ tempstr);
					    	}else{
					    		jvm = makeQuotedPath(afile.getAbsolutePath(), true);
					    	}					    	
			    		}
				    }
			    	Log.generic(ENGINE_NAME +" using 'JVM' path: "+ jvm);
				    array = jvm +" ";
				    
					String ft_bin = null;
				    String jarpath = null;
				    String projectpath = null;
				    String classpath = null;
				    String installdir = null;				    

					// JVMARGS -- append unmodified, if present
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, JVMARGS_OPTION);
				    if (tempstr != null) { 
				    	array += tempstr +" ";
				    }

			    	// XBOOTCLASSPATH 
				    // separate RJ JVM needs some jars to be in CLASSPATH, append them to end of bootstrap classpath.
				    // When java.exe calls rational_ft.jar using option -jar, the JVM only takes rational_ft.jar as its classpath. 
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, XBOOTCLASSPATH_OPTION);
				    if(tempstr != null){
			    		array += "-Xbootclasspath/a:" + makeQuotedString(tempstr) + " ";	
				    }else{
				    	tempstr = getExtJarsForBootcp();
				    	if (tempstr != null) {
				    		array += "-Xbootclasspath/a:" + makeQuotedString(tempstr) + " ";	
				    	}
				    }
				    
					// INSTALLDIR 
				    installdir = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, "INSTALLDIR");
				    if (installdir != null) { 
				    	String qpath = makeQuotedPath(installdir, false);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid INSTALLDIR information!");
							return;
				    	}
						// CLASSPATH  to precede INSTALLDIR if present	
					    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ,  "CLASSPATH");
					    if (tempstr != null) {
					    	array += "-cp "+ tempstr +" ";
					    }					    
				    	installdir = "-Drational_ft.install.dir="+ qpath;
				    	array += installdir +" ";

						// RATIONAL FT_CLASS only valid for INSTALLDIR setting
						array += HOOK_CLASS +" ";

				    }else{  // check JARPATH & PROJECTPATH or DEDUCE INFORMATION
				    	
				    	// JARPATH
					    tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, "JARPATH");
					    if (tempstr == null) tempstr = System.getenv("IBM_RATIONAL_RFT_INSTALL_DIR");
				    	if (tempstr == null) tempstr = System.getProperty("IBM_RATIONAL_RFT_INSTALL_DIR");
				    	if (tempstr == null) tempstr = System.getProperty("IBM.RATIONAL.RFT.INSTALL.DIR");
				    	if (tempstr != null){
				    		CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr +"/rational_ft.jar");
						   	if(afile.isFile()){
						   		jarpath = "-jar "+ makeQuotedPath(afile.getAbsolutePath(), true);
								array += jarpath +" ";
						   	}else{
								Log.error(ENGINE_NAME +" JARPATH could not be validated.");
								return;
						   	}
				    	}
				    	
				    	// PROJECTPATH
					    tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, "PROJECTPATH");
					    if (tempstr == null) {
					    	String stafdir = null;
					    	String safsdir = null;
					    	
					    	stafdir = System.getenv("STAFDIR");
					    	if(stafdir==null){
								Log.error(ENGINE_NAME +" PROJECTPATH STAFDIR could not be deduced.");
								return;
					    	}
					    	safsdir = System.getenv("SAFSDIR");
					    	if(safsdir==null){
								Log.error(ENGINE_NAME +" PROJECTPATH SAFSDIR could not be deduced.");
								return;
					    	}
					    	
					    	// validate paths to JSTAF.JAR and SAFS JAR files
					    	CaseInsensitiveFile afile = new CaseInsensitiveFile(stafdir +"/bin/jstaf.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JSTAF.JAR could not be validated.");
								return;
						   	}
					    	projectpath = afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/safs.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to SAFS.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/jakarta-regexp-1.3.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JAKARTA-REGEXP-1.3.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/safsrational_ft.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to SAFSRATIONAL_FT.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/safsrational_ft_enabler.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to SAFSRATIONAL_FT_ENABLER.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/jai_core.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JAI_CORE.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/jai_codec.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JAI_CODEC.JAR could not be validated.");
								return;
						   	}
						   	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/jai_imageio.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JAI_IMAGEIO.JAR could not be validated.");
								return;
						   	}
						   	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/jna.zip");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to JNA.ZIP could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/win32-x86.zip");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to WIN32-X86.ZIP could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/safscust.jar");
						   	if(! afile.isFile()){
								Log.error(ENGINE_NAME +" PROJECTPATH to SAFSCUST.JAR could not be validated.");
								return;
						   	}
					    	projectpath += afile.getAbsolutePath() +";";
					    	afile = new CaseInsensitiveFile(safsdir +"/lib/safsdebug.jar");
						   	if(afile.isFile()){
						    	projectpath += afile.getAbsolutePath() +";";
						   	}else{
								Log.warn(ENGINE_NAME +" PROJECTPATH to SAFSDEBUG.JAR could not be validated.");
						   	}
						   	
						   	afile = new CaseInsensitiveFile(safsdir +"/lib/safsrational_ft_custom.jar");
						   	if(afile.isFile()){
						   		projectpath += afile.getAbsolutePath() +";";
						   		Log.info( ENGINE_NAME +" SAFS found safsrational_ft_custom.jar file to include in ProjectPath.");
						   	}
					    }else{
					    	projectpath = tempstr;
					    }
				    	if (projectpath != null){
					   		projectpath = "-projectpath "+ makeQuotedString(projectpath);
							array += projectpath +" ";
						}else{
							Log.error(ENGINE_NAME +" PROJECTPATH could not be deduced.");
							return;						
				    	}
				    }

					// DATASTORE
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "DATASTORE");
				    if (tempstr != null) { 
				    	String qpath = makeQuotedPath(tempstr, false);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid DATASTORE information!");
							return;
				    	}
				    	array += "-datastore "+ qpath +" ";
				    }else{
						Log.error(ENGINE_NAME +" missing DATASTORE information!");
						return;
				    }

					// HOOK SCRIPT
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		       PLAYBACK_OPTION);
				    if (tempstr == null)  
				    	tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		       HOOK_OPTION);
				    if (tempstr == null)  
				    	tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		       HOOKSCRIPT_OPTION);
				    
				    if (tempstr == null) tempstr = HOOK_SCRIPT;
			    	{
			    		String qpath = makeQuotedString(tempstr);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid HOOK information!");
							return;
				    	}
				    	array += "-playback "+ qpath +" ";
			    	}

					// USERID
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "USERID");
				    if (tempstr != null) {
				    	array += "-user "+ tempstr +" ";
				    }
				    
					// PASSWORD
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "PASSWORD");
				    if (tempstr != null) {
				    	array += "-password "+ tempstr +" ";
				    }

					// PROJECT
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "PROJECT");
				    if (tempstr != null) { 
				    	String qpath = makeQuotedPath(tempstr, true);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid PROJECT information!");
							return;
				    	}
				    	array += "-project "+ qpath +" ";
				    }

					// BUILD
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "BUILD");
				    if (tempstr != null) { 
				    	String qpath = makeQuotedString(tempstr);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid BUILD information!");
							return;
				    	}
				    	array += "-build "+ qpath +" ";
				    }

					// LOGFOLDER
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "LOGFOLDER");
				    if (tempstr != null) { 
				    	String qpath = makeQuotedString(tempstr);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid LOGFOLDER information!");
							return;
				    	}
				    	array += "-logfolder "+ qpath +" ";
				    }

					// LOG
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "LOG");
				    if (tempstr != null) { 
				    	String qpath = makeQuotedString(tempstr);
				    	if (qpath==null){
							Log.error(ENGINE_NAME +" invalid LOG information!");
							return;
				    	}
				    	array +="-log "+ qpath +" ";
				    }

					// OPTIONS
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ, 
				         		      "OPTIONS");
				    String ARGS = "-args ";
				    if (tempstr != null) {
				    	if(tempstr.trim().startsWith(ARGS)){
					    	array += tempstr +" ";				    		
				    	}else{
					    	array += ARGS + tempstr +" ";				    		
				    	}
				    }else{
				    	array += ARGS;
				    }
				    
				    //append safs.config.paths
				    String cpaths = config.getConfigurePaths();
				    cpaths = "safs.config.paths="+ cpaths;
				    if (cpaths.indexOf(" ")>0) cpaths = "\""+ cpaths +"\"";
				    array += cpaths;

					Log.info(ENGINE_NAME +" preparing to execute external process...");
					Log.info(array);

				    // launch SAFSROBOTJ
					Runtime runtime = Runtime.getRuntime();
					boolean done = false;
					boolean fixtried = false;
					int timeout = 45; // Default timeout
					try{
						String t = config.getNamedValue(DriverConstant.SECTION_SAFS_ROBOTJ,
			         		              "TIMEOUT");
						if((t !=null)&&(t.length()>0)) 
							timeout = Integer.parseInt(t);
					}catch(NumberFormatException nf){
						Log.info(ENGINE_NAME +" ignoring invalid config info for TIMEOUT.");
					}
					
					while(!done){
						process = runtime.exec(array);						
						console = new ProcessConsole(process);
						Thread athread = new Thread(console);
						athread.start();
						
						running = false;
						for(int loop=0;((loop < timeout)&&(! running));loop++){
							running = isToolRunning();
							if(! running)
							   try{Thread.sleep(1000);}catch(InterruptedException ix){}					
						}
						
						if(! running){
							Log.error("Unable to detect running "+ ENGINE_NAME +
							          " within timeout period!");
							if (!fixtried && console.getExceptionsCount()> 0){
								Log.info("Checking for "+ ENGINE_NAME +" Exceptions we may be able to handle...");
								java.util.Vector exceptions = console.getExceptions();
								Enumeration items = exceptions.elements();
								console.shutdown();
								process.destroy();          
								String item = null;
								done = true; //preset exit of loop
								while(!fixtried && items.hasMoreElements()){
									item = (String) items.nextElement();
									if ((item.toLowerCase().indexOf("suiteservicemanager$suiteservicenotfoundexception")> 0)||
										(item.toLowerCase().indexOf("com.rational.test.ft.services.logexception")> 0))
								    {
										Log.info(ENGINE_NAME +" attempting to disable TestManager Logging...");
										fixtried = true;
										if ( disableTestManagerLogging()){
											Log.info(ENGINE_NAME +" attempting to launch without Test Manager Logging...");
											done = false;
										}else{
											Log.info(ENGINE_NAME +" unsuccessful disabling Test Manager Logging.");											
										}
									}
								}
								
								if(done) {
									Log.info(ENGINE_NAME +" unable to recover from exceptions or errors.");
									return;
								}
								
							}else{
								Log.info(ENGINE_NAME +" unable to recover from failed launch.");
								console.shutdown();
								process.destroy();          
								return;
							}
						}
						else{
							done = true;
							weStartedService = true;
							Log.info(ENGINE_NAME + " detected.");
						}
					}
				}
				// not supposed to autolaunch
				else{
					Log.generic(ENGINE_NAME +" AUTOLAUNCH is *not* enabled.");
					// ?we will hope the user is getting it online before we have to use it?
				}
			}
		}catch(Exception x){
			Log.error(
			ENGINE_NAME +" requires a valid DriverInterface object for initialization!  "+
			x.getMessage());
		}
	}		

	/**
	 * Attempt to disable Test Manager Logging in RFT in the options.rftdval file.
	 * @return true if successful, false otherwise
	 */
	private boolean disableTestManagerLogging() {
		String userdir = System.getProperty("user.home");
		//Log.info(ENGINE_NAME +" user home directory is: "+ userdir);
		String rftdir = userdir + "\\Application Data\\IBM\\RFT";
		//Log.info(ENGINE_NAME +" seeking OPTIONS file in directory: "+ rftdir);
		String optionfile = rftdir + "\\options.rftdval";
		Log.info(ENGINE_NAME +" seeking RFT OPTIONS file : "+ optionfile);
		File options = new CaseInsensitiveFile(optionfile).toFile();
		if (! options.exists()) return false;
		optionfile = options.getAbsolutePath();
		String optionbak = optionfile+".safsbackup";
		File backup = new File(optionbak);
		String eol = System.getProperty("line.separator", "\r\n");
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try{
			//persist backup only once
			if(!backup.exists()){
				writer = new BufferedWriter(new FileWriter(optionbak));
				reader = new BufferedReader(new FileReader(optionfile));
				while(reader.ready()){
					writer.write(reader.read());
				}
				reader.close();
				writer.flush();
				writer.close();
			}
			// now overwrite original file
			writer = new BufferedWriter(new FileWriter(optionfile));
			reader = new BufferedReader(new FileReader(optionbak));
			String line = null;
			boolean set = false;
			while(reader.ready()){
				line = reader.readLine();
				if(!set && line.indexOf("/OptionDefaults")>0){
					writer.write("\t<Name>rt.log_format</Name>\n");
					writer.write("\t<Default>text</Default>\n");
				}
				if(!set && line.indexOf("rt.log_format")>0){
					writer.write("\t<Name>rt.log_format</Name>\n");
					writer.write("\t<Default>text</Default>\n");
					set = true;
					line = reader.readLine();//ignore setting
					continue;
				}
				writer.write(line +"\n");
			}
			reader.close();
			writer.flush();
			writer.close();
			return true;
		}catch(Exception x){
			Log.error(ENGINE_NAME +" Exception:\n",x);
			try{reader.close();}catch(Exception x2){;}
			try{writer.close();}catch(Exception x2){;}
		}
		return false;
	}

	// this may be more correctly refactored into the GenericEngine superclass.
	/** Override superclass to catch unsuccessful initialization scenarios. */
	public long processRecord(TestRecordHelper testRecordData) {
		if (running) return super.processRecord(testRecordData);
		running = isToolRunning();
		if (running) return super.processRecord(testRecordData);
		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}	

}

