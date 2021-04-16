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
package org.safs.tools.drivers;
/**
 * History:
 * APR 07, 2015    (Lei Wang) Add static class SeleniumConfigConstant.
 * SEP 24, 2015    (Lei Wang) Add static class MailConstant.
 * OCT 09, 2016    (Lei Wang) Modified 2 wrongly-named constant, corrected a typo and added constants about DndReleaseDelay.
 * MAR 07, 2017    (Lei Wang) Made SeleniumConfigConstant subclass of SeleniumConstants.
 * MAR 30, 2017    (Lei Wang) Added DataServiceConstant.
 * APR 16, 2018    (Lei Wang) Add constants of 'selenium server launch script' to SeleniumConfigConstant.
 * MAY 18, 2018    (Lei Wang) Added method getServiceURL() to DataServiceConstant.
 * JUL 11, 2018    (Lei Wang) Add SAFSLogsConstant.
 * OCT 09, 2018    (Lei Wang) Modified DataServiceConstant: changed the default data service url to "http://safsdev:8880/safsdata".
 * OCT 09, 2018    (Lei Wang) Add SafsROBOTJ.
 */
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.rmi.registry.Registry;

import org.safs.JavaConstant;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.logging.AbstractLogFacility;

/**
 * @see JavaConstant
 */
public abstract class DriverConstant extends JavaConstant{

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

	/** "SCRIPT_T". SCRIPT STEP Record Type.**/
	public static final String RECTYPE_SCRIPT_T = "SCRIPT_T";

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
	 * added 11.15.2005 (Bob Lawler) RJL
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
	 * JVM command line: -Dsafs.test.unexpected_alert_behaviour=accept|dismiss|ignore|off */
	public static final String PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR = "safs.test.unexpected_alert_behaviour";
	/** "UnexpectedAlertBehaviour" option under section
	 * [SAFS_TEST]
	 * UnexpectedAlertBehaviour=accept|dismiss|ignore|off */
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

	/** "safs.test.product.name" the name of the product the test runs against<br>
	 * JVM command line: -Dsafs.test.product.name=Product to test */
	public static final String PROPERTY_SAFS_TEST_ORD_PRODUCT_NAME = "safs.test.product.name";
	/** "ProductName" option (the name of the product the test runs against) under section
	 * [SAFS_TEST]
	 * ProductName=Product to test */
	public static final String KEY_SAFS_TEST_ORD_PRODUCT_NAME ="ProductName";

	/** "safs.test.product.platform" the platform on which the test runs<br>
	 * JVM command line: -Dsafs.test.product.platform=OS platform */
	public static final String PROPERTY_SAFS_TEST_ORD_PLATFORM = "safs.test.product.platform";
	/** "Platform" option (the platform on which the test runs) under section
	 * [SAFS_TEST]
	 * Platform=OS platform */
	public static final String KEY_SAFS_TEST_ORD_PLATFORM ="Platform";

	/** "safs.test.product.track" the track of the product the test runs against<br>
	 * JVM command line: -Dsafs.test.product.track=track name */
	public static final String PROPERTY_SAFS_TEST_ORD_TRACK = "safs.test.product.track";
	/** "Track" option (the track of the product the test runs against) under section
	 * [SAFS_TEST]
	 * Track=track name */
	public static final String KEY_SAFS_TEST_ORD_TRACK ="Track";

	/** "safs.test.product.branch" the branch of the product the test runs against<br>
	 * JVM command line: -Dsafs.test.product.branch=branch name */
	public static final String PROPERTY_SAFS_TEST_ORD_BRANCH = "safs.test.product.branch";
	/** "Branch" option (the branch of the product the test runs against) under section
	 * [SAFS_TEST]
	 * Branch=branch name */
	public static final String KEY_SAFS_TEST_ORD_BRANCH ="Branch";

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
	public static final String SECTION_SAFS_DIRECTORIES = SafsDirectories.SECTION_NAME;

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
	public static final String SECTION_SAFS_ROBOTJ = SafsROBOTJ.SECTION_NAME;

    /** "SAFS_ROBOT" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_ROBOT ="SAFS_ROBOT";

	/** "SAFS_SELENIUM" <br>
	 * Predefined "section" of a Driver Configuration source. **/
	public static final String SECTION_SAFS_SELENIUM ="SAFS_SELENIUM";

	public abstract static class SafsROBOTJ{
		/** "SAFS_ROBOTJ" <br>
		 * Predefined "section" of a SAFS RFT Rational Function Tester. **/
		public static final String SECTION_NAME ="SAFS_ROBOTJ";

		/**'true' by default, SAFS-RFT will automatically search top windows to enable for testing. */
		public static final boolean DEFAULT_DYNAMIC_ENABLE_TOP_WINS = true;

		/**
		 * "DynamicEnableTopWindows" defines if the Dynamic Enabler will automatically search the top windows to enable.<br>
		 * If this is enabled, it may cost more time.<br>
		 * Examples:<br>
		 * [SAFS_ROBOTJ]<br>
		 * DynamicEnableTopWindows=<b>True|False</b><br>
		 */
		public static final String ITEM_DYNAMIC_ENABLE_TOP_WINS ="DynamicEnableTopWindows";

	}

	public abstract static class SafsDirectories{
		/** "SAFS_DIRECTORIES" <br>
		 * Predefined "section" of a SAFS Test Directories. **/
		public static final String SECTION_NAME ="SAFS_DIRECTORIES";

		/**'Actuals' the default folder where the test results will go. */
		public static final String DEFAULT_TESTDIR = "Actuals";
		/**'Diffs' the default folder where the test diff results will go. */
		public static final String DEFAULT_DIFFDIR = "Diffs";
		/**'Benchmarks' the default folder where the test benchmarks are stored. */
		public static final String DEFAULT_BENCHDIR = "Benchmarks";
		/**'Logs' the default folder where the test logs will go. */
		public static final String DEFAULT_LOGDIR = "Logs";
		/**'Maps' the default folder where the test data (something likes Map, Map-Order files) is stored. */
		public static final String DEFAULT_DATADIR = "Maps";

		/**
		 * "TESTDIR" defines the name of folder where the test results will go.<br>
		 * Examples:<br>
		 * [SAFS_DIRECTORIES]<br>
		 * TESTDIR=<b>Actuals</b><br>
		 * */
		public static final String ITEM_TESTDIR ="TESTDIR";
		/**
		 * "DIFFDIR" defines the name of folder where the test differences will go.<br>
		 * Examples:<br>
		 * [SAFS_DIRECTORIES]<br>
		 * DIFFDIR=<b>Diffs</b><br>
		 * */
		public static final String ITEM_DIFFDIR ="DIFFDIR";
		/**
		 * "BENCHDIR" defines the name of folder where the test benchmarks are stored.<br>
		 * Examples:<br>
		 * [SAFS_DIRECTORIES]<br>
		 * BENCHDIR=Benchmarks<b></b><br>
		 * */
		public static final String ITEM_BENCHDIR ="BENCHDIR";
		/**
		 * "LOGDIR" defines the name of folder where the test logs will go.<br>
		 * Examples:<br>
		 * [SAFS_DIRECTORIES]<br>
		 * LOGDIR=Logs<b></b><br>
		 * */
		public static final String ITEM_LOGDIR ="LOGDIR";
		/**
		 * "DATADIR" defines the name of folder where the test data (something likes Map, Map-Order files) is stored.<br>
		 * Examples:<br>
		 * [SAFS_DIRECTORIES]<br>
		 * DATADIR=Maps<b></b><br>
		 * */
		public static final String ITEM_DATADIR ="DATADIR";

	}

	/*
	 * History:
	 * APR 02, 2018 (Lei Wang) Added constants related to protocol.
	 * OCT 08, 2018 (Lei Wang) Added constants/methods related to test-step intercept.
	 */
	/**
	 * Defines the constants used for SAFS Data Service.
	 *
	 */
	public abstract static class DataServiceConstant{
		/** "SAFS_DATA_SERVICE" <br>
		 * Predefined "section" of a Driver Configuration source. **/
		public static final String SECTION_NAME ="SAFS_DATA_SERVICE";

		/** "SAFS_DATA_SERVICE" <br>
		 * Default service ID. Usually it will be appended with a timestamp to create a unique ID,
		 * which is used to start a REST session.
		 */
		public static final String DEFAULT_ID = SECTION_NAME;

		/** 'safs.data.' */
		private static final String PREFIX = "safs.data.";

		/** 8880 */
		public static final int DEFAULT_PORT_INT = 8880;

		/**'safsdev' the default host on which the SAFS Data service runs. */
		public static final String DEFAULT_HOST = "safsdev";
		/**'8880' the default port number on which the SAFS Data service runs. */
		public static final String DEFAULT_PORT = String.valueOf(DEFAULT_PORT_INT);
		/**'safsdata' the default relative URI on which the SAFS Data service runs. */
		public static final String DEFAULT_BASE_NAME = "safsdata";
		/**'http://' the default protocol with which the SAFS Data service runs. */
		public static final String DEFAULT_PROTOCOL = "http://";
		/**'http://safsdev:8880/safsdata' the default server URL on which the SAFS Data service runs. */
		public static final String DEFAULT_SERVER_URL = DEFAULT_PROTOCOL+DEFAULT_HOST+":"+DEFAULT_PORT+"/"+DEFAULT_BASE_NAME+"";

		/**
		 * "protocol" defines the protocol with which the safs data service runs. It can be "http://" or "https://" etc.<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * protocol=<b>http://</b><br>
		 * */
		public static final String ITEM_PROTOCOL ="protocol";
		/** "safs.data.protocol" defines the protocol with which the safs data service runs<br>
		 * JVM command line: -Dsafs.data.protocol=http://<br>
		 * **/
		public static final String PROPERTY_PROTOCOL = PREFIX+ITEM_PROTOCOL;

		/**
		 * "host" defines the name of host where the safs data service runs<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * host=<b>serverMachineName</b><br>
		 * */
		public static final String ITEM_HOST ="host";
		/** "safs.data.host" defines the name of host where the safs data service runs<br>
		 * JVM command line: -Dsafs.data.host=serverMachineName<br>
		 * **/
		public static final String PROPERTY_HOST = PREFIX+ITEM_HOST;

		/**
		 * "port" defines the port number where the safs data service runs<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * port=<b>8080</b><br>
		 * */
		public static final String ITEM_PORT ="port";
		/** "safs.data.port" defines the port number where the safs data service runs<br>
		 * JVM command line: -Dsafs.data.port=portNumber <br>
		 * Examples:<br>
		 * -Dsafs.data.port=<b>8080</b><br>
		 * **/
		public static final String PROPERTY_PORT = PREFIX+ITEM_PORT;

		/**
		 * "base.name" defines the relative URI where the safs data service runs<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * base.name=<b>safsdata</b><br>
		 * */
		public static final String ITEM_BASE_NAME ="base.name";
	    /** "safs.data.base.name" defines the relative URI where the safs data service runs<br>
		 * JVM command line: -Dsafs.data.base.name=relativeURI<br>
		 * Examples:<br>
		 * -Dsafs.data.base.name=<b>safsdata</b><br>
		 * **/
		public static final String PROPERTY_BASE_NAME = PREFIX+ITEM_BASE_NAME;

		/**
		 * "server.url" defines the whole URL of safs data service.<br>
		 * If this is defined, then the 'protocol', 'host', 'port' and 'base.name' will not take effect anymore.<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * server.url=<b>http://host:8080/safsdata</b><br>
		 * */
		public static final String ITEM_SERVER_URL ="server.url";
	    /** "safs.data.server.url" defines the whole URL of safs data service<br>
	     * If this is defined, then the 'safs.data.protocol', 'safs.data.host', 'safs.data.port' and 'safs.data.base.name' will not take effect anymore.<br>
		 * JVM command line: -Dsafs.data.server.url=http://host:port/safsdata <br>
		 * As always, "the property setting" has higher priority than configuration file setting.<br>
		 * Examples:<br>
		 * -Dsafs.data.server.url=<b>http://host:8080/safsdata</b><br>
		 * **/
		public static final String PROPERTY_SERVER_URL = PREFIX+ITEM_SERVER_URL;

		/**
		 * Try to get 'data service URL' from<br>
		 * <ol>
		 * <li>system property {@link #PROPERTY_SERVER_URL}
		 * <li>item {@link #ITEM_SERVER_URL} of section {@link #SECTION_NAME} from configuration.
		 * </ol>
		 * If not found, then try to get the 'protocol', 'host', 'port' and 'baseName' by
		 * <ol>
		 * <li>{@link #PROPERTY_PROTOCOL}, {@link #PROPERTY_HOST}, {@link #PROPERTY_PORT} and {@link #PROPERTY_BASE_NAME} from system property
		 * <li>{@link #ITEM_PROTOCOL}, {@link #ITEM_SERVER_URL}, {@link #ITEM_HOST}, {@link #ITEM_PORT} from configuration
		 * </ol>
		 * and concatenate them into a URL as protocol+host+":"+port+"/"+baseName
		 * <p>
		 *
		 * @param configInfo ConfigureInterface, the configuration to find {@link #ITEM_SERVER_URL}, {@link #ITEM_HOST}, {@link #ITEM_PORT} etc.
		 * @return String, the SAFS data service URL.
		 */
		public static final String getServiceURL(ConfigureInterface configInfo){
			String serverURL = StringUtils.getSystemProperty(PROPERTY_SERVER_URL, configInfo, SECTION_NAME, ITEM_SERVER_URL);
			if(!StringUtils.isValid(serverURL)){
				String protocol = StringUtils.getSystemProperty(PROPERTY_PROTOCOL, configInfo, SECTION_NAME, ITEM_PROTOCOL, DEFAULT_PROTOCOL);
				String host = StringUtils.getSystemProperty(PROPERTY_HOST, configInfo, SECTION_NAME, ITEM_HOST, DEFAULT_HOST);
				String port = StringUtils.getSystemProperty(PROPERTY_PORT, configInfo, SECTION_NAME, ITEM_PORT, DEFAULT_PORT);
				String baseName = StringUtils.getSystemProperty(PROPERTY_BASE_NAME, configInfo, SECTION_NAME, ITEM_BASE_NAME, DEFAULT_BASE_NAME);

				serverURL = protocol+host+":"+port+"/"+baseName;
			}

			return serverURL;
		}

		/** "false" */
		public static final String DEFAULT_INTERCEPT_STEP ="false";
		/**
		 * "intercept.step" defines if the test step will be intercepted.<br>
		 * If this is set to true, then each test step will be intercepted. The default is false.<br>
		 * Examples:<br>
		 * [SAFS_DATA_SERVICE]<br>
		 * intercept.step=<b>true|false</b><br>
		 * */
		public static final String ITEM_INTERCEPT_STEP ="intercept.step";
	    /** "safs.data.intercept.step" defines if the test step will be intercepted.<br>
	     * If this is set to true, then each test step will be intercepted. The default is false.<br>
		 * JVM command line: -Dsafs.data.intercept.step=<b>true|false</b><br>
		 * As always, "the property setting" has higher priority than configuration file setting.<br>
		 * Examples:<br>
		 * -Dsafs.data.intercept.step=<b>true|false</b><br>
		 * **/
		public static final String PROPERTY_INTERCEPT_STEP = PREFIX+ITEM_INTERCEPT_STEP;

		public static final void readInterceptions(ConfigureInterface configInfo){
			String bool = StringUtils.getSystemProperty(PROPERTY_INTERCEPT_STEP, configInfo, SECTION_NAME, ITEM_INTERCEPT_STEP, DEFAULT_INTERCEPT_STEP);
			interceptStep = Boolean.parseBoolean(bool);
		}

		private static boolean interceptStep = false;
		/**
		 * This should be called after calling {@link #readInterceptions(ConfigureInterface)}.
		 *
		 * @return boolean, indicates if the test step will be intercepted.
		 */
		public static boolean isInterceptStep() {
			return interceptStep;
		}

	}

	/**
	 * Define some constant strings used under section {@link DriverConstant#SECTION_SAFS_DRIVERCOMMANDS} for Mail.
	 *
	 * @see org.safs.tools.mail.Mailer
	 */
	public abstract static class MailConstant{

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
	public abstract static class SeleniumConfigConstant extends SeleniumConstants{

		/** 'safs.selenium.' */
		private static final String ENGINE_PREFIX = "safs.selenium.";


		/**
		 * "<b>server.launch.script</b>" is the absolute path of the script for launching the selenium server.
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * If this item is specified, many other items, such as {@link #SELENIUMHOST}, {@link #SELENIUMPORT} and {@link #SELENIUMNODE} etc. should also be specified consistently.<br>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * server.launch.script=<b>C:\SeleniumPlus\extra\RemoteServer.bat</b><br>
		 */
		public static final String ITEM_SERVER_LAUNCH_SCRIPT		 			= "server.launch.script";
		/**
		 * "<b>safs.selenium.server.launch.script</b>" is the absolute path of the script for launching the selenium server.<br>
		 * If this item is specified, many other properties, such as 'selenium.host', 'selenium.port' and 'selenium.node' etc., should also be specified consistently.<br>
		 * </p>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.server.launch.script=<b>C:\SeleniumPlus\extra\RemoteServer.bat</b><br>
		 */
		public static final String PROPERTY_SERVER_LAUNCH_SCRIPT 				= ENGINE_PREFIX+ITEM_SERVER_LAUNCH_SCRIPT;

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
		 * "SELENIUMSERVER_JVM_Xmx" define the maximum memory to use for the SELENIUM server<br>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br>
		 * SELENIUMSERVER_JVM_Xmx=2g<br>
		 *
		 * @see #SELENIUMSERVER_JVM_Xms
		 * */
		public static final String SELENIUMSERVER_JVM_Xmx ="SELENIUMSERVER_JVM_Xmx";
		/**
		 * "safs.selenium.server.jvm.xmx" define the maximum memory to use for the SELENIUM server<br>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #PROPERTY_SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * Examples:<br>
		 * -Dsafs.selenium.server.jvm.xmx=2g<br>
		 *
		 * @see #PROPERTY_SELENIUMSERVER_JVM
		 * @see #PROPERTY_SELENIUMSERVER_JVM_Xms
		 * */
		public static final String PROPERTY_SELENIUMSERVER_JVM_Xmx = ENGINE_PREFIX+"server.jvm.xmx";
		/** "2g" default maximum memory for the SELENIUM server*/
		public static String DEFAULT_JVM_MEMORY_MAXIMUM = "2g";

		/** "SELENIUMSERVER_JVM_Xms" define the minimum memory to use for the SELENIUM server
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br>
		 * SELENIUMSERVER_JVM_Xmx=512m<br>
		 *
		 * @see #SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String SELENIUMSERVER_JVM_Xms ="SELENIUMSERVER_JVM_Xms";
		/** "safs.selenium.server.jvm.xms" define the minimum memory to use for the SELENIUM server
		 * Examples:<br>
		 * -Dsafs.selenium.server.jvm.xms=512m<br>
		 *
		 * @see #PROPERTY_SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String PROPERTY_SELENIUMSERVER_JVM_Xms = ENGINE_PREFIX+"server.jvm.xms";
		/** "512m" default minimum memory for the SELENIUM server*/
		public static String DEFAULT_JVM_MEMORY_MINIMUM = "512m";

		/**
		 * "SELENIUMSERVER_JVM_OPTIONS" defines the JVM Options for the SELENIUM server.
		 * {@link #SELENIUMSERVER_JVM_Xms} and {@link #SELENIUMSERVER_JVM_Xmx} are finer, if they exist then
		 * they will be used instead of the options specified in {@link #SELENIUMSERVER_JVM_OPTIONS}.<br>
		 * For example, if we specify option as following, then 4g and 512m will be used to start the selenium server.<br>
		 * [SAFS_SELENIUM]<br>
		 * SELENIUMSERVER_JVM_OPTIONS="-Xms256m -Xmx1g"<br>
		 * SELENIUMSERVER_JVM_Xmx=4g<br>
		 * SELENIUMSERVER_JVM_Xms=512m<br>
		 * <br>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * @see #SELENIUMSERVER_JVM_Xms
		 * @see #SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String SELENIUMSERVER_JVM_OPTIONS ="SELENIUMSERVER_JVM_OPTIONS";
		/**
		 * "safs.selenium.server.jvm.options" defines the JVM Options for the SELENIUM server.
		 * {@link #PROPERTY_SELENIUMSERVER_JVM_Xms} and {@link #PROPERTY_SELENIUMSERVER_JVM_Xmx} are finer, if they exist then
		 * they will be used instead of the options specified in {@link #PROPERTY_SELENIUMSERVER_JVM_OPTIONS}.<br>
		 * For example, the we have specified properties as following, then 4g and 512m will be used to start the selenium server.<br>
		 * <ul>
		 * <li>-Dsafs.selenium.server.jvm.options="-Xms256m -Xmx1g"
		 * <li>-Dsafs.selenium.server.jvm.xmx=4g
		 * <li>-Dsafs.selenium.server.jvm.xms=512m
		 * </ul>
		 * To use a very high memory, 4g for example, you may need to specify the {@link #PROPERTY_SELENIUMSERVER_JVM} to use 64 bits Java.<br>
		 * @see #PROPERTY_SELENIUMSERVER_JVM_Xms
		 * @see #PROPERTY_SELENIUMSERVER_JVM_Xmx
		 * */
		public static final String PROPERTY_SELENIUMSERVER_JVM_OPTIONS = ENGINE_PREFIX+"server.jvm.options";
		/**
		 * "SELENIUMSERVER_JVM" defines the JVM to start the SELENIUM server.<br>
		 * The value should be an absolute path of the java.exe executable<br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br>
		 * SELENIUMSERVER_JVM="D:\jdk\jdk1.7.0_45_64bit\bin\java.exe"
		 */
		public static final String SELENIUMSERVER_JVM ="SELENIUMSERVER_JVM";
		/**
		 * "safs.selenium.server.jvm" defines the JVM to start the SELENIUM server.<br>
		 * The value should be an absolute path of the java.exe executable, for example as following<br>
		 * -Dsafs.selenium.server.jvm="D:\jdk\jdk1.7.0_45_64bit\bin\java.exe"
		 */
		public static final String PROPERTY_SELENIUMSERVER_JVM = ENGINE_PREFIX+"server.jvm";

		/**
		 * "CONSOLE_STATE" defines the state of the JVM console for the SELENIUM server<br>
		 * The value can be: MAX|MIN|NORMAL|MAXIMIZE|MINIMIZE<br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br>
		 * CONSOLE_STATE=<b>MAX</b> Selenium Server console will be maximized.<br>
		 * */
		public static final String ITEM_CONSOLE_STATE ="CONSOLE_STATE";
	    /** "safs.selenium.console.state" defines the state of the JVM console for the SELENIUM server<br>
		 * JVM command line: -Dsafs.selenium.console.state=MAX|MIN|NORMAL|MAXIMIZE|MINIMIZE <br>
		 * Examples:<br>
		 * -Dsafs.selenium.console.state=<b>MAX</b> Selenium Server console will be maximized.<br>
		 * **/
		public static final String PROPERTY_CONSOLE_STATE = ENGINE_PREFIX+"console.state";

		/**
		 * "<b>WEB_DRIVERS</b>" defines a set of drivers to start with the selenium-server.<br>
		 * The value can be a combination (separated by a colon :) of<br>
		 * <ul>
		 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
		 * <li>BrowserConstants.BROWSER_NAME_XXX might be supported.
		 * </ul>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br>
		 * WEB_DRIVERS=<b>explorer</b> Only IEDriver will start with the selenium-server.<br>
		 * WEB_DRIVERS=<b>explorer:chrome:MicrosoftEdge</b> IEDriver, ChromeDriver and EdgeDriver will start with the selenium-server.<br>
		 */
		public static final String ITEM_WEB_DRIVERS ="WEB_DRIVERS";
		/**
		 * "<b>safs.selenium.web.drivers</b>" defines a set of drivers to start with the selenium-server.<br>
		 * The value can be a combination (separated by a colon :) of<br>
		 * <ul>
		 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
		 * <li>BrowserConstants.BROWSER_NAME_XXX might be supported.
		 * </ul>
		 *
		 * Examples:<br>
		 * -Dsafs.selenium.web.drivers=<b>explorer</b> Only IEDriver will start with the selenium-server.<br>
		 * -Dsafs.selenium.web.drivers=<b>explorer:chrome:MicrosoftEdge</b> IEDriver, ChromeDriver and EdgeDriver will start with the selenium-server.<br>
		 */
		public static final String PROPERTY_WEB_DRIVERS = ENGINE_PREFIX+"web.drivers";

		//Find explanation of timeout and browser-timeout from https://seleniumhq.github.io/docs/remote.html
		/**
		 * <b>0</b> seconds, means that the client is NEVER allowed to be gone before the session is reclaimed<br>
		 * @see #ITEM_TIMEOUT
		 * @see #PROPERTY_TIMEOUT
		 */
		public static final int DEFAULT_TIMEOUT 			= 0;
		/**
		 * <b>0</b> seconds, means that the browser is allowed to hang forever.<br>
		 * @see #ITEM_BROWSER_TIMEOUT
		 * @see #PROPERTY_BROWSER_TIMEOUT
		 */
		public static final int DEFAULT_BROWSER_TIMEOUT 	= 0;

		/**
		 * "<b>timeout</b>" defines the timeout in seconds before the hub automatically releases a node that hasn't received any requests for more than the specified number of seconds.<br>
		 * The timeout value is an integer.<br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 *
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * timeout=<b>200</b> The node can stay idle for 200 seconds before being released by the hub.<br>
		 */
		public static final String ITEM_TIMEOUT = "timeout";
		/**
		 * "<b>safs.selenium.timeout</b>" defines the timeout in seconds before the selenium hub automatically releases a node that hasn't received any requests for more than the specified number of seconds.<br>
		 * The timeout value is an integer.<br>
		 *
		 * Examples:<br>
		 * -Dsafs.selenium.timeout=<b>200</b> The node can stay idle for 200 seconds before being released by the hub.<br>
		 */
		public static final String PROPERTY_TIMEOUT = ENGINE_PREFIX+ITEM_TIMEOUT;

		/**
		 * <b>false</b> means that we don't wait the component's ready before trying to handle it.<br>
		 * @see #ITEM_WAIT_READY
		 * @see #PROPERTY_WAIT_READY
		 */
		public static final boolean DEFAULT_WAIT_READY 			= false;

		/**
		 * "<b>wait.ready</b>" defines if the component will not be handled until it is ready.<br>
		 * It is a boolean value, the default value is false.<br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 *
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * wait.ready=<b>true</b> The component will not be handled until it is ready.<br>
		 */
		public static final String ITEM_WAIT_READY = "wait.ready";
		/**
		 * "<b>safs.selenium.wait.ready</b>" defines if the component will not be handled until it is ready.<br>
		 * It is a boolean value, the default value is false.<br>
		 *
		 * Examples:<br>
		 * -Dsafs.selenium.wait.ready=<b>true</b> The component will not be handled until it is ready.<br>
		 */
		public static final String PROPERTY_WAIT_READY = ENGINE_PREFIX+ITEM_WAIT_READY;

		/**
		 * "<b>browser.timeout</b>" defines the timeout in seconds a node is willing to hang inside the browser.<br>
		 * The timeout value is an integer.<br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 *
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * browser.timeout=<b>200</b> The node is willing to hang 200 seconds.<br>
		 */
		public static final String ITEM_BROWSER_TIMEOUT = "browser.timeout";
		/**
		 * "<b>safs.selenium.browser.timeout</b>" defines the timeout in seconds a selenium node is willing to hang inside the browser.<br>
		 * The timeout value is an integer.<br>
		 *
		 * Examples:<br>
		 * -Dsafs.selenium.browser.timeout=<b>200</b> The node is willing to hang 200 seconds.<br>
		 */
		public static final String PROPERTY_BROWSER_TIMEOUT = ENGINE_PREFIX+ITEM_BROWSER_TIMEOUT;

		/**
		 * "<b>connection.test.command</b>" defines the <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
		 * used to test the connection between WebDriver and BrowserDriver.<br>
		 * selenium-driver-command is a string understood by a remote server using the JSON wire protocol.<br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * connection.test.command=<b>getAlertText</b><br>
		 * connection.test.command=<b>getCurrentWindowSize</b><br>
		 */
		public static final String ITEM_CONNECTION_TEST_COMMAND 			= "connection.test.command";
		/**
		 * "<b>safs.selenium.connection.test.command</b>" defines the <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
		 * used to test the connection between WebDriver and BrowserDriver.<br>
		 * selenium-driver-command is a string understood by a remote server using the JSON wire protocol.<br>
		 * Examples:<br>
		 * -Dsafs.selenium.connection.test.command=<b>getAlertText</b><br>
		 * -Dsafs.selenium.connection.test.command=<b>getCurrentWindowSize</b><br>
		 */
		public static final String PROPERTY_CONNECTION_TEST_COMMAND 		= ENGINE_PREFIX+ITEM_CONNECTION_TEST_COMMAND;

		/**
		 * "<b>connection.test.max.duration</b>" defines the maximum duration (in <b>milliseconds</b>) that user can accept when executing
		 * <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
		 * defined by {@link #PROPERTY_CONNECTION_TEST_COMMAND} or {@link #ITEM_CONNECTION_TEST_COMMAND}.<br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * connection.test.max.duration=<b>3000</b><br>
		 */
		public static final String ITEM_CONNECTION_TEST_MAX_DURATION 		= "connection.test.max.duration";
		/**
		 * "<b>safs.selenium.connection.test.max.duration</b>" defines the maximum duration (in <b>milliseconds</b>) that user can accept when executing
		 * <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
		 * defined by {@link #PROPERTY_CONNECTION_TEST_COMMAND} or {@link #ITEM_CONNECTION_TEST_COMMAND}.<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.connection.test.max.duration=<b>3000</b><br>
		 */
		public static final String PROPERTY_CONNECTION_TEST_MAX_DURATION 	= ENGINE_PREFIX+ITEM_CONNECTION_TEST_MAX_DURATION;
		/**
		 * <b>5000</b> defines the default maximum duration (in <b>milliseconds</b>) that user can accept when executing
		 * <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
		 * defined by {@link #PROPERTY_CONNECTION_TEST_COMMAND} or {@link #ITEM_CONNECTION_TEST_COMMAND}.<br>
		 */
		public static final int DEFAULT_CONNECTION_TEST_MAX_DURATION 		= 5000;

		/**
		 * "<b>connection.test.max.try</b>" defines the maximum times to try to get a good connection between WebDriver and BrowserDriver<br>
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * connection.test.max.try=<b>10</b><br>
		 */
		public static final String ITEM_CONNECTION_TEST_MAX_TRY 			= "connection.test.max.try";
		/**
		 * "<b>safs.selenium.connection.test.max.try</b>" defines the maximum times to try to get a good connection between WebDriver and BrowserDriver<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.connection.test.max.try=<b>10</b><br>
		 */
		public static final String PROPERTY_CONNECTION_TEST_MAX_TRY 		= ENGINE_PREFIX+ITEM_CONNECTION_TEST_MAX_TRY;
		/**
		 * <b>2</b> defines the maximum times to try to get a good connection.<br>
		 */
		public static final int DEFAULT_CONNECTION_TEST_MAX_TRY 			= 2;

		/**
		 * "<b>delay.get.content</b>" defines the delay (milliseconds) waiting for the refresh of a webelement before getting its content.
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * delay.get.content=<b>2000</b><br>
		 */
		public static final String ITEM_DELAY_WAIT_REFRESH		 			= "delay.get.content";
		/**
		 * "<b>safs.selenium.delay.get.content</b>" defines the delay (milliseconds) waiting for the refresh of a webelement before getting its content.<br>
		 * <p>
		 * Sometimes the Component's content will get refreshed (the html tag will be redrawn on page), we need to wait before it is ready.
		 * Otherwise, the content is stale and StaleElementReferenceException will be thrown out if we try to operate the content.
		 * </p>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.delay.get.content=<b>2000</b><br>
		 */
		public static final String PROPERTY_DELAY_GET_CONTENT 				= ENGINE_PREFIX+ITEM_DELAY_WAIT_REFRESH;
		/**
		 * <b>0</b> milliseconds delay waiting for the refresh of a webelement before getting its content.
		 */
		public static final int DEFAULT_DELAY_GET_CONTENT 			= 0;

		/**
		 * "<b>bypass.frame.reset</b>" By default, search algorithms begin every search by switching back to the topmost root HTML document (frame).<br>
		 * Set <b>bypass.frame.reset</b> to true if you wish to disable this frames reset at the beginning of every component search.<br>
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * bypass.frame.reset=<b>true | false</b><br>
		 *
		 * <br>
		 * <b>NOTE:</b> If this is set to true, user MUST handle the frame-switch by himself and should NOT define any "frame information" in the Recognition String in MAP file.
		 * <pre>
		 *    WebDriver().switchTo().defaultContent();
		 *    WebDriver().switchTo().frame("my_iframe");
		 *    //SeleniumPlus will search components on frame "my_iframe".
		 * </pre>
		 *
		 */
		public static final String ITEM_BYPASS_FRAME_RESET		 			= "bypass.frame.reset";
		/**
		 * "<b>safs.selenium.bypass.frame.reset</b>" By default, search algorithms begin every search by switching back to the topmost root HTML document (frame).<br>
		 * Set <b>safs.selenium.bypass.frame.reset</b> to true if you wish to disable this frames reset at the beginning of every component search.<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.bypass.frame.reset=<b>true | false</b><br>
		 *
		 * <br>
		 * <b>NOTE:</b> If this is set to true, user MUST handle the frame-switch by himself and should NOT define any "frame information" in the Recognition String in MAP file.
		 * <pre>
		 *    WebDriver().switchTo().defaultContent();
		 *    WebDriver().switchTo().frame("my_iframe");
		 *    //SeleniumPlus will search components on frame "my_iframe".
		 * </pre>
		 */
		public static final String PROPERTY_BYPASS_FRAME_RESET 				= ENGINE_PREFIX+ITEM_BYPASS_FRAME_RESET;
		/**
		 * <b>false</b> Do not bypass the frame reset, by default, search algorithms begin every search by switching back to the topmost root HTML document (frame).
		 */
		public static final boolean DEFAULT_BYPASS_FRAME_RESET 			= false;

		/**
		 * "<b>bypass.robot.action</b>" By default, our library will handle click actions by Robot firstly, if failed then handle it by selenium.<br>
		 * Set <b>bypass.robot.action</b> to true if you wish to skip the Robot.<br>
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * bypass.robot.action=<b>true | false</b><br>
		 *
		 */
		public static final String ITEM_BYPASS_ROBOT_ACTION		 			= "bypass.robot.action";
		/**
		 * "<b>safs.selenium.bypass.robot.action</b>" By default, our library will handle click actions by Robot firstly, if failed then handle it by selenium.<br>
		 * Set <b>safs.selenium.bypass.robot.action</b> to true if you wish to skip the Robot.<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.bypass.robot.action=<b>true | false</b><br>
		 *
		 */
		public static final String PROPERTY_BYPASS_ROBOT_ACTION 				= ENGINE_PREFIX+ITEM_BYPASS_ROBOT_ACTION;
		/**
		 * <b>false</b> Do not bypass the robot.action, by default our library will handle click actions by Robot firstly, if failed then handle it by selenium.
		 */
		public static final boolean DEFAULT_BYPASS_ROBOT_ACTION 			= false;

		/**
		 * "<b>rmi.port.forward</b>" With docker, by default, our library will connect the RMI server running on docker container by container's IP and port.<br>
		 *                           But we can map the container's port to local-machine's port so that our library will connect the RMI server (running on docker container) by "localhost" and the mapped port.<br>
		 *
		 * Set <b>rmi.port.forward</b> to true if you want our library to connect the RMI server (running on docker container) by "localhost" and the mapped port.<br>
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * rmi.port.forward=<b>true | false</b><br>
		 *
		 */
		public static final String ITEM_RMI_PORT_FORWARD		 			= "rmi.port.forward";
		/**
		 * "<b>safs.selenium.rmi.port.forward</b>" With docker, by default, our library will connect the RMI server running on docker container by container's IP and port.<br>
		 *                           But we can map the container's port to local-machine's port so that our library will connect the RMI server (running on docker container) by "localhost" and the mapped port.<br>
		 *
		 * Set <b>safs.selenium.rmi.port.forward</b> to true if you want our library to connect the RMI server (running on docker container) by "localhost" and the mapped port.<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.rmi.port.forward=<b>true | false</b><br>
		 *
		 */
		public static final String PROPERTY_RMI_PORT_FORWARD 			= ENGINE_PREFIX+ITEM_RMI_PORT_FORWARD;
		/**
		 * <b>false</b> Do not use 'rmi port forward', our library will connect the RMI server running on docker container by container's IP and port.<br>
		 */
		public static final boolean DEFAULT_RMI_PORT_FORWARD 			= false;

		/**
		 * "<b>registry.port</b>" By default, our library will use {@link #DEFAULT_REGISTRY_PORT 1099} as the port to get the registry for looking up the RMI server.<br>
		 * <br>
		 * Set <b>registry.port</b> to another port if the registry is created with different port.<br>
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_SELENIUM}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_SELENIUM]<br/>
		 * registry.port=<b>1100</b><br>
		 *
		 */
		public static final String ITEM_REGISTRY_PORT		 			= "registry.port";
		/**
		 * "<b>safs.selenium.registry.port</b>" By default, our library will use {@link #DEFAULT_REGISTRY_PORT 1099} as the port to get the registry for looking up the RMI server.<br>
		 * Set <b>safs.selenium.registry.port</b> to another port if the registry is created with different port.<br>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.selenium.registry.port=<b>1100</b><br>
		 *
		 */
		public static final String PROPERTY_REGISTRY_PORT 				= ENGINE_PREFIX+ITEM_REGISTRY_PORT;

		/** the default port {@link Registry#REGISTRY_PORT 1099} used by the registry for looking up the RMI server */
		public static final int DEFAULT_REGISTRY_PORT				 	= Registry.REGISTRY_PORT;
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

	/**
	 *
	 * Define properties and items in section {@link DriverConstant#SECTION_SAFS_LOGS}.
	 */
	public abstract static class SAFSLogsConstant{

		/** 'safs.log.' */
		private static final String PREFIX = "safs.log.";

		/**
		 * "<b>accept.slf4j.debug</b>" boolean (default is {@link #DEFAULT_ACCPET_SLF4J_DEBUG}), if the debug message from slf4j will be output to SAFS debug log file.
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_LOGS}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_LOGS]<br/>
		 * accept.slf4j.debug=<b>false</b><br>
		 */
		public static final String ITEM_ACCPET_SLF4J_DEBUG		 			= "accept.slf4j.debug";
		/** false */
		public static final String DEFAULT_ACCPET_SLF4J_DEBUG		 			= Boolean.toString(false);
		/**
		 * "<b>safs.log.accept.slf4j.debug</b>" boolean (default is {@link #DEFAULT_ACCPET_SLF4J_DEBUG}), if the debug message from slf4j will be output to SAFS debug log file.
		 * </p>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.log.accept.slf4j.debug=<b>false</b><br>
		 */
		public static final String PROPERTY_ACCPET_SLF4J_DEBUG 				= PREFIX+ITEM_ACCPET_SLF4J_DEBUG;

		/**
		 * "<b>accept.slf4j.test</b>" boolean (default is {@link #DEFAULT_ACCPET_SLF4J_TEST}), if the test message from slf4j will be output to SAFS test log file.
		 * <br>
		 * It is an item under section {@link DriverConstant#SECTION_SAFS_LOGS}<br/>
		 * <br>
		 * Examples:<br>
		 * [SAFS_LOGS]<br/>
		 * accept.slf4j.test=<b>false</b><br>
		 */
		public static final String ITEM_ACCPET_SLF4J_TEST		 			= "accept.slf4j.test";
		/** false */
		public static final String DEFAULT_ACCPET_SLF4J_TEST		 			= Boolean.toString(false);
		/**
		 * "<b>safs.log.accept.slf4j.test</b>" boolean (default is {@link #DEFAULT_ACCPET_SLF4J_TEST}), if the test message from slf4j will be output to SAFS test log file.
		 * </p>
		 * <br>
		 * Examples:<br>
		 * -Dsafs.log.accept.slf4j.test=<b>false</b><br>
		 */
		public static final String PROPERTY_ACCPET_SLF4J_TEST 				= PREFIX+ITEM_ACCPET_SLF4J_TEST;
	}
}

