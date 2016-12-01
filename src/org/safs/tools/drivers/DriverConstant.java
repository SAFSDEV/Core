/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;
/**
 * History:
 * APR 07, 2015    (SBJLWA) Add static class SeleniumConfigConstant.
 * SEP 24, 2015    (SBJLWA) Add static class MailConstant.
 * OCT 09, 2016    (SBJLWA) Modified 2 wrongly-named constant, corrected a typo and added constants about DndReleaseDelay.
 */
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import org.safs.JavaConstant;
import org.safs.StatusCodes;
import org.safs.logging.AbstractLogFacility;

/**
 * @see JavaConstant
 */
public class DriverConstant extends JavaConstant{

	/** Disable construction. **/
	protected DriverConstant (){}

	/** "C". Driver Command Record Type.**/
	public static final String RECTYPE_C = "C";

	/** "CW". WARNOK Driver Command Record Type.**/
	public static final String RECTYPE_CW = "CW";

	/** "CF". FAILOK Driver Command Record Type.**/
	public static final String RECTYPE_CF = "CF";

	/** "E". Engine Command Record Type.**/
	public static final String RECTYPE_E = "E";

	/** "T". Test Record Type.**/
	public static final String RECTYPE_T = "T";

	/** "TW". WARNOK Test Record Type.**/
	public static final String RECTYPE_TW = "TW";

	/** "TF". FAILOK Test Record Type.**/
	public static final String RECTYPE_TF = "TF";

	/** "B". BlockID Record Type.**/
	public static final String RECTYPE_B = "B";

	/** "BP". BreakPoint Record Type.**/
	public static final String RECTYPE_BP = "BP";

	/** "S". Skipped Record Type.**/
	public static final String RECTYPE_S = "S";

	/** "-2". A warning occurred.**/
    public static final int STATUS_SCRIPT_WARNING           = StatusCodes.SCRIPT_WARNING;      //for scripts AND test tables

	/** "-1". No known error or warning occurred.**/
    public static final int STATUS_NO_SCRIPT_FAILURE        = StatusCodes.NO_SCRIPT_FAILURE;      //for scripts AND test tables

	/** "0". An error occurred.**/
    public static final int STATUS_GENERAL_SCRIPT_FAILURE   = StatusCodes.GENERAL_SCRIPT_FAILURE;       //for scripts AND test tables

	/** "2". An IO (file/input) error occurred.**/
    public static final int STATUS_INVALID_FILE_IO          = StatusCodes.INVALID_FILE_IO;

	/** "4". No processing has occurred.
	 * This generally means an engine or driver has not accepted responsibility 
	 * for the record.  Noone has tried to execute it yet.
	 */
    public static final int STATUS_SCRIPT_NOT_EXECUTED      = StatusCodes.SCRIPT_NOT_EXECUTED;       //for scripts AND test tables

	/** "8". An  EXIT TABLE Driver Command flags premature termination.**/
    public static final int STATUS_EXIT_TABLE_COMMAND       = StatusCodes.EXIT_TABLE_COMMAND;

	/** "16". IGNORE this status code. 
	 * Another process has handled 'stuff'.  
	 * Generally, a Driver Command will have set this if it already handled all 
	 * necessary activities like incrementing counters, etc...
	 */
    public static final int STATUS_IGNORE_RETURN_CODE       = StatusCodes.IGNORE_RETURN_CODE;      //drivers ignore this one

    /** "256". BRANCH TO BLOCKID. 
	 * Used by Driver Command to denote that branching is expected.
	 * added 11.15.2005 (bolawl) RJL
	 */
    public static final int STATUS_BRANCH_TO_BLOCKID       = StatusCodes.BRANCH_TO_BLOCKID; 
    
	/** "5". User-Defined TEST FAILURE pre-logged. 
	 * The driver should increment the appropriate counters.
	 */
    public static final int STATUS_TESTFAILURE_LOGGED       = 5;      

	/** "6". User-Defined TEST SUCCESS pre-logged. 
	 * The driver should increment the appropriate counters.
	 */
    public static final int STATUS_TESTSUCCESS_LOGGED       = 6;      

	/** "7". User-Defined TEST WARNING pre-logged. 
	 * The driver should increment the appropriate counters.
	 */
    public static final int STATUS_TESTWARNING_LOGGED       = 7;      

	/** "safstid.ini" <br>
	 * Default Configuration filename used by TID Driver.**/
	public static final String DEFAULT_CONFIGURE_FILENAME = "safstid.ini";
	
	/** "test.ini" <br>
	 * Default Configuration filename used by org.safs.model.tools.AbstractRunner,
	 * it serves as the default configuration file for any driver launched from within AbstractRunner. **/
	public static final String DEFAULT_CONFIGURE_FILENAME_TEST_INI = "test.ini";

	/** "org.safs.tools.drivers.ConfigureFileLocator" <br>
	 * Default ConfigureLocatorInterface used by TID Driver.**/
	public static final String DEFAULT_CONFIGURE_LOCATOR = "org.safs.tools.drivers.ConfigureFileLocator";

	/** "org.safs.tools.input.SAFSINPUT" <br>
	 * Default InputInterface used by TID Driver.**/
	public static final String DEFAULT_INPUT_INTERFACE = "org.safs.tools.input.SAFSINPUT";
	
	/** "org.safs.tools.input.SAFSMAPS" <br>
	 * Default MapsInterface used by TID Driver.**/
	public static final String DEFAULT_MAPS_INTERFACE = "org.safs.tools.input.SAFSMAPS";
	
	/** "org.safs.tools.vars.SAFSVARS" <br>
	 * Default VarsInterface used by TID Driver.**/
	public static final String DEFAULT_VARS_INTERFACE = "org.safs.tools.vars.SAFSVARS";
	
	/** "org.safs.tools.logs.SAFSLOGS" <br>
	 * Default LogsInterface used by TID Driver.**/
	public static final String DEFAULT_LOGS_INTERFACE = "org.safs.tools.logs.SAFSLOGS";

	/** "org.safs.tools.stacks.SAFSSTACKS" <br>
	 * Default StacksInterface used by TID Driver.**/
	public static final String DEFAULT_STACKS_INTERFACE = "org.safs.tools.stacks.SAFSSTACKS";
	
	/** "org.safs.tools.status.SAFSSTATUS" <br>
	 * Default StatusInterface used by TID Driver.**/
	public static final String DEFAULT_STATUS_INTERFACE = "org.safs.tools.status.SAFSSTATUS";
	
	/** "org.safs.tools.counters.SAFSCOUNTERS" <br>
	 * Default CountersInterface used by TID Driver.**/
	public static final String DEFAULT_COUNTERS_INTERFACE = "org.safs.tools.counters.SAFSCOUNTERS";
	
	/** "safs.config.locator"<br>
	 * System Property identifying an alternate class to locate driver 
	 * configuration information. The class is required to implement the 
	 * org.safs.tools.drivers.ConfigureLocatorInterface<br>
	 * JVM command line: -Dsafs.config.locator=classname <br>
	 * The default locator is listed above.<br>
	 * @see ConfigureLocatorInterface **/
	public static final String PROPERTY_SAFS_CONFIG_LOCATOR ="safs.config.locator";
	
	/** "," <br>
	 * Default field separator used on input sources.**/
	public static final String DEFAULT_FIELD_SEPARATOR = ",";
	
	/** ".CDD" <br>
	 * Default CYCLE testname suffix.**/
	public static final String DEFAULT_CYCLE_TESTNAME_SUFFIX = ".CDD";
	
	/** ".STD" <br>
	 * Default SUITE testname suffix.**/
	public static final String DEFAULT_SUITE_TESTNAME_SUFFIX = ".STD";
	
	/** ".SDD" <br>
	 * Default STEP testname suffix.**/
	public static final String DEFAULT_STEP_TESTNAME_SUFFIX = ".SDD";

	/** "0", AbstractLogFacility.LOGMODE_DISABLED;
	 * Logging Disabled logmode.**/
	public static final long DRIVER_LOGGING_DISABLED = AbstractLogFacility.LOGMODE_DISABLED;

	/** "1", AbstractLogFacility.LOGMODE_TOOL;
	 * TOOLLOG logmode enabled.  This can be OR'd with other logmodes.<br>
	 * The tool log is whatever proprietary logging mechanism is available from the underlying 
	 * execution engine(s).  For example, with Rational this would be the TestManager log.  
	 * Of course, only those messages issued by the particular engine will appear in that tools 
	 * proprietary log.  But the SAFSLOGS logs will contain messages issued from ALL engines.**/
	public static final long DRIVER_TOOLLOG_ENABLED = AbstractLogFacility.LOGMODE_TOOL;

	/** "TOOLLOG", Text alternative to DRIVER_TOOLLOG_ENABLED numeric value when allowed. */
	public static final String DRIVER_TOOLLOG_ENABLED_STRING = "TOOLLOG";

	/** "32", AbstractLogFacility.LOGMODE_SAFS_TEXT;
	 * TEXTLOG logmode enabled.  This can be OR'd with other logmodes.**/
	public static final long DRIVER_TEXTLOG_ENABLED = AbstractLogFacility.LOGMODE_SAFS_TEXT;

	/** "TEXTLOG", Text alternative to DRIVER_TEXTLOG_ENABLED numeric value when allowed. */
	public static final String DRIVER_TEXTLOG_ENABLED_STRING = "TEXTLOG";


	/** "8", AbstractLogFacility.LOGMODE_CONSOLE;
	 * CONSOLELOG logmode enabled.  This can be OR'd with other logmodes.<br>
	 * The console log is whatever proprietary log console is available from the underlying 
	 * execution engine.  For example, with Rational this would be the Console written to 
	 * with SQAConsoleWrite.<br>
	 * Of course, only those messages issued by the particular engine will appear in that tools 
	 * proprietary console.  But the SAFSLOGS logs will contain messages issued from ALL engines.**/
	public static final long DRIVER_CONSOLELOG_ENABLED = AbstractLogFacility.LOGMODE_CONSOLE;

	/** "CONSOLELOG", Text alternative to DRIVER_CONSOLELOG_ENABLED numeric value when allowed. */
	public static final String DRIVER_CONSOLELOG_ENABLED_STRING = "CONSOLELOG";


	/** "64", AbstractLogFacility.LOGMODE_SAFS_XML;
	 * XMLLOG logmode enabled.  This can be OR'd with other logmodes.**/
	public static final long DRIVER_XMLLOG_ENABLED = AbstractLogFacility.LOGMODE_SAFS_XML;

	/** "XMLLOG", Text alternative to DRIVER_XMLLOG_ENABLED numeric value when allowed. */
	public static final String DRIVER_XMLLOG_ENABLED_STRING = "XMLLOG";


	/** "127", AbstractLogFacility.LOGMODE_MAX;
	 * MAXLOGMODE enabled.  This enables all logmodes.<br>
	 * Note, this constant value can change in future releases as additional logmodes are 
	 * created.  The developer should not count on this constant value being what it is today.<br<
	 * The user should be cautious in using MAX_LOGMODE as it is very resource intensive.  
	 * Also, using MAX_LOGMODE means that new logmodes will automatically be enabled once they 
	 * become available.  And that may be an undesirable, performance-hindering side-affect.<br>
	 * Note LOGMODE values of 32, 64, and 128 are RESERVED.**/
	public static final long DRIVER_MAX_LOGMODE_ENABLED = AbstractLogFacility.LOGMODE_MAX;

	/** "ALL", Text alternative to DRIVER_MAX_LOGMODE_ENABLED numeric value when allowed. */
	public static final String DRIVER_MAX_LOGMODE_ENABLED_STRING = "ALL";


	/** "SAFSLOG"
	 * Default SAFS LogName.**/
	public static final String DEFAULT_LOGNAME = "SAFSLOG";

	/** "ERROR"
	 * Log Error test messages only.**/
	public static final String DRIVER_LOGLEVEL_ERROR = "ERROR";

	/** "WARN"
	 * Log Error and Warning test messages only.**/
	public static final String DRIVER_LOGLEVEL_WARN = "WARN";

	/** "PASS"
	 * Log all test messages except GENERIC test messages.**/
	public static final String DRIVER_LOGLEVEL_PASS = "PASS";

	/** "GENERIC"
	 * Log all test messages.**/
	public static final String DRIVER_LOGLEVEL_GENERIC = "GENERIC";

	/** "INDEX"
	 * Log all test messages and debugmode GUI Index messages only.**/
	public static final String DRIVER_LOGLEVEL_INDEX = "INDEX";

	/** "INFO"
	 * Log all test messages, debug GUI Index, and debug Info messages only.**/
	public static final String DRIVER_LOGLEVEL_INFO = "INFO";

	/** "DEBUG"
	 * Log all test and debug mode messages.**/
	public static final String DRIVER_LOGLEVEL_DEBUG = "DEBUG";

	/** "GENERIC"
	 * Default Log Level for logs.**/
	public static final String DEFAULT_LOGLEVEL= DRIVER_LOGLEVEL_GENERIC;

	/** "&lt;SAFS_NULL>"
	 * Used thru STAF and other areas to exchange NULL, MISSING, or DOES NOT EXIST values.**/
	public static final String SAFS_NULL = "<SAFS_NULL>";


	/** "10" <br>
	 * Default SAFS LogMode.
	 * Enables both the TEXT and CONSOLE logs.**/
	public static final long DEFAULT_LOGMODE = (DRIVER_TEXTLOG_ENABLED | DRIVER_CONSOLELOG_ENABLED) ;


	/** "CYCLE" <br>
	 * CYCLE test level constant.**/
	public static final String DRIVER_CYCLE_TESTLEVEL = "CYCLE";
	
	/** "SUITE" <br>
	 * SUITE test level constant.**/
	public static final String DRIVER_SUITE_TESTLEVEL = "SUITE";

	/** "STEP" <br>
	 * STEP test level constant.**/
	public static final String DRIVER_STEP_TESTLEVEL = "STEP";

	/** "datapool"<br>
	 * Default project datapool subdirectory.<br>
	 * This is where test tables and app maps are stored by default. 
	 * Generally, all inputs except benchmarks are placed here.<br>
	 * Default resolves to .\safsproject\datapool\ **/
	public static final String DEFAULT_PROJECT_DATAPOOL = "datapool";

	/** "bench"<br>
	 * Default project benchmarks subdirectory.<br>
	 * Default resolves to .\safsproject\datapool\bench\ **/
	public static final String DEFAULT_PROJECT_BENCH = "bench";

	/** "logs"<br>
	 * Default project logs subdirectory.<br>
	 * Default resolves to .\safsproject\datapool\logs\ **/
	public static final String DEFAULT_PROJECT_LOGS = "logs";

	/** "dif"<br>
	 * Default project differences subdirectory.<br>
	 * Default resolves to .\safsproject\datapool\dif\ **/
	public static final String DEFAULT_PROJECT_DIF = "dif";
	
	/** "test"<br>
	 * Default project test/actuals subdirectory.<br>
	 * Default resolves to .\safsproject\datapool\test\ **/
	public static final String DEFAULT_PROJECT_TEST = "test";
	
	/** "safs.modified.root"<br>
	 * System property for a root directory Property that was dynamically altered at runtime. <br>
	 **/
	public static final String PROPERTY_SAFS_MODIFIED_ROOT = "safs.modified.root";

	/** "safs.driver.root"<br>
	 * System property for the root driver directory. <br>
	 * If this property is not provided at startup, the driver must resort to 
	 * searching the entire CLASSPATH looking for a suitable "safstid.ini" 
	 * configuration file.<br>
	 * JVM command line: -Dsafs.driver.root=fullpath **/
	public static final String PROPERTY_SAFS_DRIVER_ROOT = "safs.driver.root";

	/** "safs.project.extract"<br>
	 * System property for the root project parent directory for JAR file extraction. <br>
	 * This is absolute fullpath directory the project from a JAR file will get extracted to.<br>
	 * JVM command line: -Dsafs.project.extract=fullpath **/
	public static final String PROPERTY_SAFS_PROJECT_EXTRACT = "safs.project.extract";

	/** "safs.project.root"<br>
	 * System property for the root project directory. <br>
	 * If this property is not provided at startup, the driver must hope to 
	 * find a suitable setting in the provided or sought configuration file(s).<br>
	 * JVM command line: -Dsafs.project.root=fullpath **/
	public static final String PROPERTY_SAFS_PROJECT_ROOT = "safs.project.root";

	/** "safs.modified.config"<br>
	 * System Property for a config file Property that was dynamically altered at runtime. <br> 
     **/
	public static final String PROPERTY_SAFS_MODIFIED_CONFIG ="safs.modified.config";
	
	/** "safs.driver.config"<br>
	 * System Property identifying the path to an alternate driver 
	 * configuration file (other than safstid.ini in the driver root location.<br>
	 * JVM command line: -Dsafs.driver.config=string|path **/
	public static final String PROPERTY_SAFS_DRIVER_CONFIG ="safs.driver.config";
	
	/** "safs.project.config"<br>
	 * System Property identifying the path to an alternate project 
	 * configuration file (other than safstid.ini in the default 
	 * project configuration location.<br>
	 * JVM command line: -Dsafs.project.config=string|path **/
	public static final String PROPERTY_SAFS_PROJECT_CONFIG ="safs.project.config";

	/** "safs.config.paths"<br>
	 * System property identifying chained configuration file paths to be used by 
	 * running engines or other Java processes.  Each chained file path is separated by 
	 * the normal File.pathSeparator for the current platform.<br>
	 * Mac Ex: /SAFS/Project/TIDTest.ini:/SAFS/Project/safstid.ini:/SAFS/safstid.ini<br>
	 * Win Ex: C:\SAFS\Project\TIDTest.ini;C:\SAFS\Project\safstid.ini;C:\SAFS\safstid.ini
	 */
	public static final String PROPERTY_SAFS_CONFIG_PATHS = "safs.config.paths";
	
    /** "safs.driver.autolaunch" <br>
	 * JVM command line: -Dsafs.driver.autolaunch=TRUE|FALSE **/
	public static final String PROPERTY_SAFS_DRIVER_AUTOLAUNCH ="safs.driver.autolaunch";

    /** "safs.test.name" <br>
	 * JVM command line: -Dsafs.test.name=string|path **/
	public static final String PROPERTY_SAFS_TEST_NAME ="safs.test.name";

    /** "safs.test.level" <br>
	 * JVM command line: -Dsafs.test.level=CYCLE|SUITE|STEP **/
	public static final String PROPERTY_SAFS_TEST_LEVEL ="safs.test.level";

    /** "safs.test.millisbetweenrecords" <br>
	 * JVM command line: -Dsafs.test.millisbetweenrecords=0-N **/
	public static final String PROPERTY_SAFS_TEST_MILLISBETWEENRECORDS ="safs.test.millisbetweenrecords";

    /** "safs.test.secswaitforwindow" <br>
	 * JVM command line: -Dsafs.test.secswaitforwindow=0-N **/
	public static final String PROPERTY_SAFS_TEST_SECSWAITFORWINDOW ="safs.test.secswaitforwindow";

    /** "safs.test.secswaitforcomponent" <br>
	 * JVM command line: -Dsafs.test.secswaitforcomponent=0-N **/
	public static final String PROPERTY_SAFS_TEST_SECSWAITFORCOMPONENT ="safs.test.secswaitforcomponent";
	
	/** the default value of the number lock, it is the current number lock status. */
	public static final boolean DEFAULT_NUMLOCK_STATUS = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
	/** "safs.test.numLockon" <br>
	 * JVM command line: -Dsafs.test.numlockon=TRUE|FALSE **/
	public static final String PROPERTY_SAFS_TEST_NUMLOCKON ="safs.test.numlockon";
	/** "numLockOn" option under section
	 * [SAFS_TEST]
	 * numLockOn=TRUE|FALSE */
	public static final String KEY_SAFS_TEST_NUMLOCKON ="numLockOn";
	
	/** 'Ignore' it is the default value for handling the unexpected alert dialog */
	public static final String DEFAULT_UNEXPECTED_ALERT_BEHAVIOUR = "ignore";
	/** "safs.test.unexpected_alert_behaviour" <br>
	 * JVM command line: -Dsafs.test.unexpected_alert_behaviour=accept|dismiss|ignore */
	public static final String PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR = "safs.test.unexpected_alert_behaviour";
	/** "UnexpectedAlertBehaviour" option under section
	 * [SAFS_TEST]
	 * UnexpectedAlertBehaviour=accept|dismiss|ignore */
	public static final String KEY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR ="UnexpectedAlertBehaviour";

	/** '800' milliseconds, it is the default value for delay before releasing mouse when 'drag and drop' */
	public static final int DEFAULT_SAFS_TEST_DND_RELEASE_DELAY  = 800;
	/** "safs.test.dnd.release.delay" the delay in millisecond before releasing mouse<br>
	 * JVM command line: -Dsafs.test.dnd.release.delay=N*/
	public static final String PROPERTY_SAFS_TEST_DND_RELEASE_DELAY = "safs.test.dnd.release.delay";
	/** "DndReleaseDelay" option (the delay in millisecond before releasing mouse) under section
	 * [SAFS_TEST]
	 * DndReleaseDelay=N */
	public static final String KEY_SAFS_TEST_DND_RELEASE_DELAY ="DndReleaseDelay";
	
    /** "safs.log.level" <br>
	 * JVM command line: -Dsafs.log.level=ERROR|WARN|PASS|GENERIC|INDEX|INFO|DEBUG **/
	public static final String PROPERTY_SAFS_LOG_LEVEL ="safs.log.level";

    /** "safs.log.overwrite" <br>
	 * JVM command line: -Dsafs.log.overwrite=TRUE|YES|1 (anything else is FALSE) **/
	public static final String PROPERTY_SAFS_LOG_OVERWRITE ="safs.log.overwrite";

    /** "safs.log.capxml" <br>
	 * JVM command line: -Dsafs.log.capxml=TRUE|YES|1 (anything else is FALSE) **/
	public static final String PROPERTY_SAFS_LOG_CAPXML ="safs.log.capxml";

    /** "safs.log.truncate" <br>
	 * JVM command line: -Dsafs.log.truncate=numchars|ON (default: OFF)**/
	public static final String PROPERTY_SAFS_LOG_TRUNCATE ="safs.log.truncate";

    /** "safs.test.defaultmap" <br>
	 * JVM command line: -Dsafs.test.defaultmap=mapid **/
	public static final String PROPERTY_SAFS_TEST_DEFAULTMAP ="safs.test.defaultmap";

    /** "safs.cycle.separator" <br>
	 * JVM command line: -Dsafs.cycle.separator=string (1 char) **/
	public static final String PROPERTY_SAFS_CYCLE_SEPARATOR ="safs.cycle.separator";

    /** "safs.suite.separator" <br>
	 * JVM command line: -Dsafs.suite.separator=string (1 char) **/
	public static final String PROPERTY_SAFS_SUITE_SEPARATOR ="safs.suite.separator";

    /** "safs.step.separator" <br>
	 * JVM command line: -Dsafs.step.separator=string (1 char) **/
	public static final String PROPERTY_SAFS_STEP_SEPARATOR ="safs.step.separator";


    /** "safs.cycle.logname" <br>
	 * JVM command line: -Dsafs.cycle.logname=logid|name **/
	public static final String PROPERTY_SAFS_CYCLE_LOGNAME ="safs.cycle.logname";

    /** "safs.suite.logname" <br>
	 * JVM command line: -Dsafs.suite.logname=logid|name **/
	public static final String PROPERTY_SAFS_SUITE_LOGNAME ="safs.suite.logname";

    /** "safs.step.logname" <br>
	 * JVM command line: -Dsafs.step.logname=logid|name **/
	public static final String PROPERTY_SAFS_STEP_LOGNAME ="safs.step.logname";

    /** "safs.cycle.logmode" <br>
	 * JVM command line: -Dsafs.cycle.logmode=(numeric) **/
	public static final String PROPERTY_SAFS_CYCLE_LOGMODE ="safs.cycle.logmode";

    /** "safs.suite.logmode" <br>
	 * JVM command line: -Dsafs.suite.logmode=(numeric) **/
	public static final String PROPERTY_SAFS_SUITE_LOGMODE ="safs.suite.logmode";

    /** "safs.step.logmode" <br>
	 * JVM command line: -Dsafs.step.logmode=(numeric) **/
	public static final String PROPERTY_SAFS_STEP_LOGMODE ="safs.step.logmode";

	/** 'safs.rmi.server'<br>
	 * JVM command line: -Dsafs.rmi.server=org.safs.selenium.rmi.server.SeleniumServer */
	public static final String PROPERTY_RMISERVER = "safs.rmi.server";
	
	/** 'org.safs.selenium.rmi.server.SeleniumServer' */
	public static final String DEFAULT_RMISERVER_CLASSNAME = "org.safs.selenium.rmi.server.SeleniumServer";

    /** "ProjectRoot" <br>
	 * Predefined "item" in the SAFS_PROJECT section of a SAFS Configuration source. **/
	public static final String ITEM_PROJECT_ROOT = "ProjectRoot";
    /** "DriverRoot" <br>
	 * Predefined "item" in the SAFS_DRIVER section of a SAFS Configuration source. **/
	public static final String ITEM_DRIVER_ROOT = "DriverRoot";
	
    /** "STAF" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_STAF ="STAF";

    /** "SAFS_ABBOT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_ABBOT ="SAFS_ABBOT";

    /** "SAFS_DRIVER" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DRIVER ="SAFS_DRIVER";

    /** "SAFS_PROJECT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_PROJECT ="SAFS_PROJECT";

    /** "SAFS_DIRECTORIES" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DIRECTORIES ="SAFS_DIRECTORIES";

    /** "SAFS_TEST" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_TEST ="SAFS_TEST";

    /** "SAFS_ENGINES" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_ENGINES ="SAFS_ENGINES";

    /** "SAFS_DRIVERCOMMANDS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DRIVERCOMMANDS ="SAFS_DRIVERCOMMANDS";

    /** "SAFS_IOS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_IOS ="SAFS_IOS";

    /** "SAFS_DROID" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DROID ="SAFS_DROID";

    /** "SAFS_QTP" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_QTP ="SAFS_QTP";

    /** "SAFS_TC" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_TC ="SAFS_TC";

    /** "SAFS_ROBOTJ" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_ROBOTJ ="SAFS_ROBOTJ";

    /** "SAFS_ROBOT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_ROBOT ="SAFS_ROBOT";

	/** "SAFS_SELENIUM" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_SELENIUM ="SAFS_SELENIUM";

	/**
	 * Define some constant strings used under section {@link DriverConstant#SECTION_SAFS_DRIVERCOMMANDS} for Mail.
	 * 
	 * @see org.safs.tools.mail.Mailer
	 */
	public static final class MailConstant{
		
		/** "OUT_MAILSERVER" define the mail server for sending mails. */
		public static final String OUT_MAILSERVER 			="OUT_MAILSERVER";
		/** "OUT_MAILSERVERPORT" define the port of mail server for sending mails. */
		public static final String OUT_MAILSERVERPORT 		="OUT_MAILSERVERPORT";
		/** "OUT_MAILSERVERPROTOCOL" define the protocol used by mail server for sending mails.
		 * The value could be SMTP, SMTPS etc. */
		public static final String OUT_MAILSERVERPROTOCOL 	="OUT_MAILSERVERPROTOCOL";
		/** "OUT_MAILUSER" define the user for authentication on mail server for sending mails. */
		public static final String OUT_MAILUSER 			="OUT_MAILUSER";
		/** "OUT_MAILPASS" define the password for authentication on mail server for sending mails. */
		public static final String OUT_MAILPASS 			="OUT_MAILPASS";
		
		/** "IN_MAILSERVER" define the mail server for receiving mails. */
		public static final String IN_MAILSERVER 			="IN_MAILSERVER";
		/** "IN_MAILSERVERPORT" define the port of mail server for receiving mails. */
		public static final String IN_MAILSERVERPORT 		="IN_MAILSERVERPORT";
		/** "IN_MAILSERVERPROTOCOL" define the protocol used by mail server for receiving mails. 
		 * The value could be IMAP, POP3 etc. */
		public static final String IN_MAILSERVERPROTOCOL 	="IN_MAILSERVERPROTOCOL";
		/** "IN_MAILUSER" define the user for authentication on mail server for receiving mails. */
		public static final String IN_MAILUSER 				="IN_MAILUSER";
		/** "IN_MAILPASS" define the password for authentication on mail server for receiving mails. */
		public static final String IN_MAILPASS 				="IN_MAILPASS";
		
	}
	/**
	 * 
	 * Define some constant strings used under section {@link DriverConstant#SECTION_SAFS_SELENIUM} for SELENIUM.
	 */
	public static final class SeleniumConfigConstant{
		
		/** "SELENIUMHOST" define the host name where the SELENIUM "standalone server"/"grid hub" will run
		 * @see #SELENIUMPORT
		 * */
		public static final String SELENIUMHOST ="SELENIUMHOST";
		/**'localhost' the default host where the SELENIUM server will run*/
		public static final String DEFAULT_SELENIUM_HOST = LOCAL_HOST;
		/** '127.0.0.1' the default host where the SELENIUM server will run*/
		public static final String DEFAULT_SELENIUM_HOST_IP = LOCAL_HOST_IP;
				
		/** "SELENIUMPORT" define the port number on which the SELENIUM "standalone server"/"grid hub" will run
		 * @see #SELENIUMHOST
		 * */
		public static final String SELENIUMPORT ="SELENIUMPORT";
		/** 4444 */
		public static final int DEFAULT_SELENIUM_PORT_INT = 4444;
		/**'4444' the default port number on which the SELENIUM "standalone server"/"grid hub" will run */
		public static final String DEFAULT_SELENIUM_PORT = String.valueOf(DEFAULT_SELENIUM_PORT_INT);
		
		/** 5555 */
		public static final int DEFAULT_SELENIUM_NODE_PORT_INT = 5555;
		/**'5555' the default port number on which the SELENIUM node will run */
		public static final String DEFAULT_SELENIUM_NODE_PORT = String.valueOf(DEFAULT_SELENIUM_NODE_PORT_INT);
		
		/** "SELENIUMNODE" define the selenium nodes to run. The value is something like "node1.machine.name:port:nodeconfig;node2.machine.name:port:nodeconfig".
		 * If this is not provided, then {@link #SELENIUMHOST} will be considered as "standalone server"
		 * If this is provided, then {@link #SELENIUMHOST} will be considered as "grid hub" on which all nodes will register.
		 * @see #SELENIUMHOST
		 * */
		public static final String SELENIUMNODE ="SELENIUMNODE";
		
		/** 
		 * "SELENIUMSERVER_JVM_Xmx" define the maximum memory to use for SELENIUM server<br>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * @see #SELENIUMSERVER_JVM_Xms
		 * */
		public static final String SELENIUMSERVER_JVM_Xmx ="SELENIUMSERVER_JVM_Xmx";
		/** "2g" default maximum memory for SELENIUM server*/
		public static String DEFAULT_JVM_MEMORY_MAXIMUM = "2g";
		
		/** "SELENIUMSERVER_JVM_Xms" define the minimum memory to use for SELENIUM server
		 * @see #SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String SELENIUMSERVER_JVM_Xms ="SELENIUMSERVER_JVM_Xms";
		/** "512m" default minimum memory for SELENIUM server*/
		public static String DEFAULT_JVM_MEMORY_MINIMUM = "512m";
		
		/** 
		 * "SELENIUMSERVER_JVM_OPTIONS" define the JVM Options for SELENIUM server.
		 * {@link #SELENIUMSERVER_JVM_Xms} and {@link #SELENIUMSERVER_JVM_Xmx} are finer, if they exist then
		 * they will be used instead of the options specified in {@link #SELENIUMSERVER_JVM_OPTIONS}.<br>
		 * For example, the we have specified option as following, then 4g and 512m will be used to start the selenium server.
		 * <ul>
		 * <li>SELENIUMSERVER_JVM_OPTIONS="-Xms256m -Xmx1g"
		 * <li>SELENIUMSERVER_JVM_Xmx=4g
		 * <li>SELENIUMSERVER_JVM_Xms=512m
		 * </ul>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * @see #SELENIUMSERVER_JVM_Xms
		 * @see #SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String SELENIUMSERVER_JVM_OPTIONS ="SELENIUMSERVER_JVM_OPTIONS";
		/**
		 * "SELENIUMSERVER_JVM" defines the JVM to start SELENIUM server.<br>
		 * The value should be an absolute path the the java.exe executable, for example as following<br>
		 * SELENIUMSERVER_JVM="D:\jdk\jdk1.7.0_45_64bit\bin\java.exe"
		 */
		public static final String SELENIUMSERVER_JVM ="SELENIUMSERVER_JVM";
		
		/** 
		 * "CONSOLE_STATE" defines the state of the JVM console for SELENIUM server<br>
		 * The possible value could be: MAX|MIN|NORMAL|MAXIMIZE|MINIMIZE<br>
		 * @see #SELENIUMSERVER_JVM_Xms
		 * */
		public static final String ITEM_CONSOLE_STATE ="CONSOLE_STATE";
	    /** "safs.selenium.console.state" defines the state of the JVM console for SELENIUM server<br>
		 * JVM command line: -Dsafs.selenium.console.state=MAX|MIN|NORMAL|MAXIMIZE|MINIMIZE **/
		public static final String PROPERTY_CONSOLE_STATE ="safs.selenium.console.state";
		
	}
	
	/** "GATEWAYHOST" define the HTTP PROXY host name to connect Internet
	 * @see #GATEWAYPORT
	 * */
	public static final String GATEWAYHOST ="GATEWAYHOST";
	
	/** "GATEWAYPORT" define the HTTP PROXY port number to connect Internet
	 * @see #GATEWAYHOST
	 * */
	public static final String GATEWAYPORT ="GATEWAYPORT";
	
	/** "PROXY_BYPASS_ADDRESS" define the HTTP PROXY "bypass address" when connecting INTRANET
	 * @see #GATEWAYHOST
	 * @see #GATEWAYPORT
	 * */
	public static final String PROXY_BYPASS_ADDRESS ="PROXY_BYPASS_ADDRESS";
	
    /** "SAFS_WINRUNNER" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_WINRUNNER ="SAFS_WINRUNNER";

    /** "SAFS_LOGS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_LOGS ="SAFS_LOGS";

    /** "SAFS_VARS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_VARS ="SAFS_VARS";

    /** "SAFS_MAPS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_MAPS ="SAFS_MAPS";

    /** "SAFS_INPUT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_INPUT ="SAFS_INPUT";

    /** "SAFS_COUNTERS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_COUNTERS ="SAFS_COUNTERS";

    /** "SAFS_STACKS" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_STACKS ="SAFS_STACKS";

    /** "SAFS_DIFFER" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DIFFER ="SAFS_DIFFER";

    /** "SAFS_DIFFVIEWER" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_DIFFVIEWER ="SAFS_DIFFVIEWER";
	
	/** "SAFS_JREX" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_JREX ="SAFS_JREX";
	
	/** "SAFS_OCR" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_OCR ="SAFS_OCR";

	/** "SAFS_IBT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_IBT ="SAFS_IBT";
	
	/** "SAFSDIR" <br>
	 * System Environment variable for SAFS Installation Directory **/
	public static final String SYSTEM_PROPERTY_SAFS_DIR = "SAFSDIR";

	/** "STAFDIR" <br>
	 * System Environment variable for STAF Installation Directory **/
	public static final String SYSTEM_PROPERTY_STAF_DIR = "STAFDIR";
	
	/** DOMAIN_SEPARATOR*/
	public static final String DOMAIN_SEPARATOR = ";";
	
	/** "Android Client" */
	public static final String ANDROID_CLIENT_DISPLAY = "Android Client";	
	/** "Android" */
	public static final String ANDROID_CLIENT_TEXT = "Android";

	/** "Java Client" */
	public static final String JAVA_CLIENT_DISPLAY = "Java Client";	
	/** "Java" */
	public static final String JAVA_CLIENT_TEXT = "Java";

	/** "HTML Client" */
	public static final String HTML_CLIENT_DISPLAY = "HTML Client";	
	/** "HTML" */
	public static final String HTML_CLIENT_TEXT = "Html";

	/** ".Net Client" */
	public static final String NET_CLIENT_DISPLAY = ".Net Client";	
	/** "Net" */
	public static final String NET_CLIENT_TEXT = "Net";

	/** "RCP Client" */
	public static final String RCP_CLIENT_DISPLAY = "RCP Client";	
	/** "Win" */
	public static final String RCP_CLIENT_TEXT = "Win";

	/** "Flex Client" */
	public static final String FLEX_CLIENT_DISPLAY = "Flex Client";	
	/** "Html" */
	public static final String FLEX_CLIENT_TEXT = "Flex";

	/** "Win Client" */
	public static final String WIN_CLIENT_DISPLAY = "Win Client";	
	/** "Win" */
	public static final String WIN_CLIENT_TEXT = "Win";

	/** "Swt Client" */
	public static final String SWT_CLIENT_DISPLAY = "Swt Client";	
	/** "Swt" */
	public static final String SWT_CLIENT_TEXT = "Swt";

	/** "SELENIUM_PLUS" */
	public static final String SYSTEM_PROPERTY_SELENIUMPLUS_DIR = "SELENIUM_PLUS";
	
	/** "MORE_ENGINES" */
	public static final String MORE_ENGINES = "MORE_ENGINES";

	/** 'showMonitor' */
	public static final String SHOW_MONITOR 			    = "showMonitor";	  
}

