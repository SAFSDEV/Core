package org.safs.projects.seleniumplus.projects;

public class BaseProject {
	/** holds path to SeleniumPlus install directory -- once validated. */
	public static String SELENIUM_PLUS;

	/** "SELENIUM_PLUS" the system environment variable name holding the path where SeleniumPlus has been installed */
	public static final String SELENIUM_PLUS_ENV = org.safs.Constants.ENV_SELENIUM_PLUS;

	/** "Tests" */
	public static final String SRC_TEST_DIR = "Tests";
	/** "src" */
	public static final String SRC_SRC_DIR = "src";

	/** "testcases" */
	public static final String SRC_TESTCASES_SUBDIR = "testcases";
	/** "testruns" */
	public static final String SRC_TESTRUNS_SUBDIR = "testruns";

	/** "TestCase1" */
	public static final String TESTCASECLASS_FILE = "TestCase1";
	/** "/samples/TestCase1.java" */
	public static final String TESTCASECLASS_RESOURCE = "/samples/TestCase1.java";
	/** "TestRun1" */
	public static final String TESTRUNCLASS_FILE = "TestRun1";
	/** "/samples/TestRun1.java" */
	public static final String TESTRUNCLASS_RESOURCE = "/samples/TestRun1.java";

	public static String srcDir;
	public static String testcaseDir;
	public static String testrunDir;

	/** "Maps" */
	public static final String DATAPOOL_DIR = "Maps";
	/** "Benchmarks" */
	public static final String BENCH_DIR = "Benchmarks";
	/** "Diffs" */
	public static final String DIF_DIR = "Diffs";
	/** "Actuals" */
	public static final String TEST_DIR = "Actuals";
	/** "Logs" */
	public static final String LOGS_DIR = "Logs";

	/** "SampleProject" */
	public static final String PROJECTTYPE_SAMPLE   = "SampleProject";

	/** "SAMPLE" */
	public static final String PROJECTNAME_SAMPLE   = "SAMPLE";
	/** Map */
	public static final String MAPCLASS_FILE = "Map";
	/** test.ini */
	public static final String TESTINI_FILE = "test.ini";
	/** runAutomation.bat */
	public static final String RUNAUTOMATION_WIN_FILE = "runAutomation.bat";
	/** /samples/runautomation.bat */
	public static final String RUNAUTOMATION_WIN_RESOURCE = "/samples/runautomation.bat";

	/** App.map */
	public static final String APPMAP_FILE = "App.map";
	/** /samples/App.map */
	public static final String APPMAP_RESOURCE = "/samples/App.map";
	/** App_en.map */
	public static final String APPMAP_EN_FILE = "App_en.map";
	/** /samples/App_en.map */
	public static final String APPMAP_EN_RESOURCE = "/samples/App_en.map";
	/** AppMap.order */
	public static final String APPMAP_ORDER_FILE = "AppMap.order";
	/** /samples/AppMap.order */
	public static final String APPMAP_ORDER_RESOURCE = "/samples/AppMap.order";

}
