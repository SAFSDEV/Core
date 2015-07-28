package org.safs.tools.drivers;

import org.safs.tools.UniqueStringID;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringFileInfo;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.vars.VarsInterface;
import org.safs.tools.status.StatusInterface;

/**
 * SAFSDRIVER is the default, completed implementation of our abstract DefaultDriver.
 * <p>
 * This class is intended to be executed as a standalone Java application--typically, 
 * in its own JVM.  The main() entry point will instance a new SAFSDRIVER and 
 * immediately invoke run().
 * <p>
 * Command-line Options and Configuration File Options are linked below.
 * <p>
 * The default name of configuration files is "SAFSTID.INI".  There is a hierarchy of
 * configuration files that will be sought based on command-line parameters provided.  
 * This hierarchy is summarized in the Configuration File Options doc linked below and 
 * detailed below::
 * <p>
 * <ol>
 * <li><b>command-line specified PROJECT config file</b><br>
 * a project or test-specific config file, other than SAFSTID.INI, containing config 
 * information intended to override settings in the SAFSTID.INI file located in the 
 * PROJECT ROOT directory.
 * <p>
 * <li><b>default PROJECT config file</b><br>
 * SAFSTID.INI located in the PROJECT ROOT directory.  The PROJECT ROOT would normally 
 * be provided as a command-line parameter.  It may instead be provided in the previous 
 * specified PROJECT config file.  This file will normally contain settings specific 
 * to an entire project and these override any similar settings in the DRIVER config 
 * files.
 * <p>
 * <li><b>command-line specified DRIVER config file</b><br>
 * Rarely used. A config file, other than SAFSTID.INI, intended to override settings 
 * in any SAFSTID.INI file located in the DRIVER ROOT directory.
 * <p>
 * <li><b>default DRIVER config file</b><br>
 * SAFSTID.INI located in the DRIVER ROOT directory.  The DRIVER ROOT would normally 
 * be provided in a previous config file or as a command-line parameter.  This config 
 * file will contain the bulk of the configuration information that is specific to 
 * the driver and independent of any project or test being executed.
 * </ol>
 * <p>
 * In general, you want to provide the bare minimum of command-line parameters and 
 * place all remaining info in one or more configuration files.  The total of all 
 * command-line parameters and config file information must enable the driver to 
 * locate valid driver and project root directories, project subdirectories, and all 
 * other items necessary to run a specified test.  See the DefaultDriver.run() link 
 * below for all the neat things the driver will do prior to launching the test!
 * <p>
 * An example invocation, providing the bare minimum command-line parameters:
 * <p>
 * <ul>
 *     java -Dsafs.project.config=c:\SAFSProject\TIDTest.INI org.safs.tools.drivers.SAFSDRIVER
 * </ul>
 * This then expects TIDTest.INI to contain the information concerning which test 
 * to run, and where the PROJECT ROOT and maybe the DRIVER ROOT directories are located.  
 * The remaining configuration information can reside in SAFSTID.INI files located in 
 * these directories.
 * <p>
 * Sample TIDTest.INI in c:\SAFSProject specific to one test:
 * <p>
 * <ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="C:\safsproject"
 * 
 * [SAFS_TEST]
 * 
 * TestName="TIDTestCycle"
 * TestLevel="Cycle"
 * CycleSeparator=","
 * 
 * CycleLogName="TIDTestCycle"
 * CycleLogMode="41"
 *      (or)
 * CycleLogMode="TOOLLOG CONSOLELOG TEXTLOG"
 * 
 * </pre>
 * </ul>
 * <p>
 * Sample SAFSTID.INI in c:\SAFSProject used by all tests:
 * <p>
 * <ul><pre>
 * [SAFS_DRIVER]
 * DriverRoot="C:\safs"
 * 
 * [SAFS_MAPS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_INPUT]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_VARS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_LOGS]
 * AUTOLAUNCH=TRUE
 * </pre>
 * </ul>
 * <p>
 * And that is enough for the TID to run the TIDTestCycle.CDD test.  Assuming, 
 * that test exists in c:\SAFSProject\Datapool\ as expected.
 * <p>
 * Of course, more of the configuration parameters necessary for desired engines 
 * will have to be in those configuration files once the engines actually become 
 * available.
 * <p>
 * @see DefaultDriver#run()
 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#driveroptions">Command-Line Options</A>
 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">Configuration File Options</A>
 */
public class SAFSDRIVER extends DefaultDriver {

	/**
	 * Default constructor using the default Driver name. 
	 * The constructor does nothing more than initialize internals and superclasses.
	 * Nothing else happens until {@link DefaultDriver#run()} is invoked.
	 */
	public SAFSDRIVER (){
		super();
		org.safs.Log.setLogProcessName(this.driverName);
	}
	
	/**
	 * Constructor allowing an alternate Driver name. 
	 * The constructor does nothing more than initialize internals and superclasses.
	 * Nothing else happens until {@link DefaultDriver#run()} is invoked.
	 * @see #driverName
	 */
	public SAFSDRIVER (String drivername){
		super();
		this.driverName = drivername;
		org.safs.Log.setLogProcessName(this.driverName);
	}
	

	/**********************************************************************************
	 * This is the one that actually opens and loops through our tests records!
	 * We use String-based Interface objects for inter-API communication.
	 * 
	 * @see AbstractDriver#processTest()
	 * @see org.safs.tools.UniqueStringID
	 * @see org.safs.tools.input.UniqueStringFileInfo
	 */
	protected StatusInterface processTest(){
	
		UniqueStringFileInfo sourceInfo = new UniqueStringFileInfo(
		                                      testName,
											  testName, 
											  getTestLevelSeparator(testLevel),
											  testLevel);

		UniqueStringID testid = new UniqueStringID(getLogID(testLevel));
		
		InputProcessor driver = new InputProcessor(this, sourceInfo, testid);
		
		StatusInterface statusinfo = driver.processTest();
		
		return statusinfo;
	}

	/**
	 * Entry point for standalone Java execution.
	 * Instances a new SAFSDRIVER object and immediately executes run().
	 * @see DefaultDriver#run()
	 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#driveroptions">Command-Line Options</A> 
	 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">Configuration File Options</A> 
	 */
	public static void main(String[] args) {

		SAFSDRIVER driver = new SAFSDRIVER();
		driver.run();
		System.runFinalization();
		System.exit(0);
	}
}

