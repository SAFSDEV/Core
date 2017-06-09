package org.safs.projects.seleniumplus.projects;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import org.safs.projects.common.projects.callbacks.Callbacks;
import org.safs.projects.common.projects.pojo.POJOContainer;
import org.safs.projects.common.projects.pojo.POJOFile;
import org.safs.projects.common.projects.pojo.POJOFolder;
import org.safs.projects.common.projects.pojo.POJOPath;
import org.safs.projects.common.projects.pojo.POJOProject;
import org.safs.projects.seleniumplus.popupmenu.FileTemplates;

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

	public static final String SAMPLES_RESOURCE_PATH = "/org/safs/projects/seleniumplus/projects/samples";

	/** "TestCase1" */
	public static final String TESTCASECLASS_FILE = "TestCase1";
	/** "/samples/TestCase1.java" */
	public static final String TESTCASECLASS_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestCase1.java";
	/** "/samples/TestCase1.java.txt" */
	public static final String TESTCASECLASS_TXT_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestCase1.java.txt";
	/** "TestRun1" */
	public static final String TESTRUNCLASS_FILE = "TestRun1";
	/** "/samples/TestRun1.java" */
	public static final String TESTRUNCLASS_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestRun1.java";
	/** "/samples/TestRun1.java.txt" */
	public static final String TESTRUNCLASS_TXT_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestRun1.java.txt";

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
	public static final String RUNAUTOMATION_WIN_RESOURCE = SAMPLES_RESOURCE_PATH + "/runautomation.bat";

	/** App.map */
	public static final String APPMAP_FILE = "App.map";
	/** /samples/App.map */
	public static final String APPMAP_RESOURCE = SAMPLES_RESOURCE_PATH + "/App.map";
	/** App_en.map */
	public static final String APPMAP_EN_FILE = "App_en.map";
	/** /samples/App_en.map */
	public static final String APPMAP_EN_RESOURCE = SAMPLES_RESOURCE_PATH + "/App_en.map";
	/** AppMap.order */
	public static final String APPMAP_ORDER_FILE = "AppMap.order";
	/** /samples/AppMap.order */
	public static final String APPMAP_ORDER_RESOURCE = SAMPLES_RESOURCE_PATH + "/AppMap.order";

	/**
	 * This method should only be called outside of the SeleniumPlus Eclipse environment.
	 * An example of this is during testing where the Eclipse framework is mocked.
	 * It also applies to projects that do not use Eclipse at all.
	 *
	 * @param workspaceDir the parent of each project's root directory.
	 */
	public static void init(File workspaceDir) {
		POJOProject.init(workspaceDir);
	}

	/**
	 * For this marvelous project we need to: - create the default Eclipse
	 * project - add the custom project nature - create the folder structure
	 *
	 * @param projectName
	 * @param location
	 * @param companyName
	 * @param projectType {@value #PROJECTTYPE_SAMPLE}, {@value #PROJECTTYPE_SELENIUM}, {@value #PROJECTTYPE_ADVANCE}
	 * @return
	 */
	public static Object createProject(String projectName, URI location, String companyName, String projectType, Callbacks callbacks) {
		assert projectName != null;
		assert companyName != null;
		assert projectName.trim().length() > 0;

		/*
		 * This method was copied from the SeleniumPlus-Plugin project's BaseProject.
		 * Parts unrelated to the SAMPLE project have been commented out.
		 * Most importantly, the callback functionality has been added.
		 * The idea is that outside the SeleniumPlus Eclipse environment, the callbacks will be null,
		 * and the implementation will use POJO classes.
		 * Within the SeleniumPlus Eclipse environment, the callbacks will be non-null,
		 * and they will be called to get Eclipse to perform the operations.
		 * These operations are things like creating projects, folders, paths, etc.
		 */
		// TODO: support the PROJECTTYPE_SELENIUM and PRJECTTYPE_ADVANCE.
		if (/*projectType.equalsIgnoreCase(PROJECTTYPE_SELENIUM) ||*/
			projectType.equalsIgnoreCase(PROJECTTYPE_SAMPLE)){

			srcDir = SRC_TEST_DIR;
			testcaseDir = srcDir + "/"+ projectName.toLowerCase() +"/"+ SRC_TESTCASES_SUBDIR;
			testrunDir =  srcDir + "/"+ projectName.toLowerCase() +"/"+ SRC_TESTRUNS_SUBDIR;

		/*} else if (projectType.equalsIgnoreCase(PROJECTTYPE_ADVANCE)){

			srcDir = SRC_SRC_DIR;
			testcaseDir = srcDir + "/com/" + companyName.toLowerCase() + "/"+ projectName.toLowerCase()+ "/"+ SRC_TESTS_SUBDIR;
			testrunDir = srcDir + "/com/" + companyName.toLowerCase() + "/"+ projectName.toLowerCase()+ "/"+ SRC_SUITES_SUBDIR;
		*/
		} else {
			// internal error
			throw new RuntimeException("Unsupported");
		}

		POJOProject project =
				callbacks != null ?
				callbacks.createProjectCallback.createProject() :
				new POJOProject(projectName);

		try {

			String[] paths = {
					srcDir,
					testcaseDir,
					testrunDir,
					DATAPOOL_DIR,
					TEST_DIR,
					BENCH_DIR,
					DIF_DIR,
					LOGS_DIR
			};

			addToProjectStructure(project, paths, callbacks);

		} catch (Exception e) {
			e.printStackTrace();
			project = null;
		}

		return project;
	}

	/**
	 * Create a folder structure with a parent root, overlay, and a few child
	 * folders.
	 *
	 * @param newProject
	 * @param paths
	 * @throws CoreException
	 */
	private static void addToProjectStructure(POJOProject newProject,
			String[] paths, Callbacks callbacks) throws Exception {

		for (String path : paths) {
			if (callbacks == null) {
				File dir = new File(newProject.getProjectDir(), path);
				dir.mkdirs();
			} else {
				callbacks.createFolderCallback.createFolder(newProject, path);
			}
		}


		/**
		 * Create sample test class
		*/
		String testClass = TESTCASECLASS_FILE;
		POJOFolder testPkg = newProject.getFolder(paths[1]);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		if (testPkg.exists()){
			POJOFile testclass = testPkg.getFile(testClass + ".java");
			InputStream testclassstream = null;
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){
				testclassstream = getResourceAsStream(loader, TESTCASECLASS_TXT_RESOURCE);
			//TODO: support non-SAMPLE case.
//			} else {
//				testclassstream = FileTemplates.testClass(newProject.getName(),newPackage,mapPkg, testClass);
			}

			testclass.create(testclassstream, true, null);
			if (testclassstream != null) testclassstream.close();
		}


		/**
		 * Create run tests
		 */
		String testRunClass = TESTRUNCLASS_FILE;
		testPkg = newProject.getFolder(paths[2]);


		if (testPkg.exists()){
			POJOFile testruns = testPkg.getFile(testRunClass + ".java");
			InputStream testrunstream = null;
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){
				testrunstream = getResourceAsStream(loader, TESTRUNCLASS_TXT_RESOURCE);
//			} else {
//				testrunstream = FileTemplates.testRunClass(newProject.getName(),newPackage,mapPkg, testRunClass);
			}

			testruns.create(testrunstream, true, null);
			if (testrunstream != null) testrunstream.close();
		}



		/**
		 * Map and Map order files
		 */
		POJOFolder mapFolder = newProject.getFolder(DATAPOOL_DIR);

		if (mapFolder.exists()) {

			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){

				POJOFile appMap = mapFolder.getFile(newProject.getName()+APPMAP_FILE);
				//InputStream mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App.map");
				InputStream mapstream = getResourceAsStream(loader, APPMAP_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();

				appMap = mapFolder.getFile(newProject.getName()+APPMAP_EN_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App_zh.map");
				mapstream = getResourceAsStream(loader, APPMAP_EN_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();

				appMap = mapFolder.getFile(APPMAP_ORDER_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/AppMap.order");
				mapstream = getResourceAsStream(loader, APPMAP_ORDER_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();

//			} else {

//				IFile appMap = mapFolder.getFile(newProject.getName()+APPMAP_FILE);
//				InputStream mapstream = FileTemplates.appMap();
//				appMap.create(mapstream, true, null);
//				mapstream.close();
//
//				appMap = mapFolder.getFile(newProject.getName()+APPMAP_EN_FILE);
//				mapstream = FileTemplates.appMap();
//				appMap.create(mapstream, true, null);
//				mapstream.close();
//
//				appMap = mapFolder.getFile(APPMAP_ORDER_FILE);
//				mapstream = FileTemplates.appMapOrder(newProject.getName());
//				appMap.create(mapstream, true, null);
//				mapstream.close();
			}
		}

		/**
		 * create test.ini file
		 */
		POJOContainer container = mapFolder.getParent();
		POJOFile iniFile = container.getFile(
				callbacks == null ?
				new POJOPath(TESTINI_FILE) :
				callbacks.createPathCallback.createPath(TESTINI_FILE)
		);
		InputStream inistream = FileTemplates.testINI(SELENIUM_PLUS,newProject.getName());
		iniFile.create(inistream, true, null);
		inistream.close();

		/**
		 * create commandline bat file
		 */
		// TODO WIN and NIX versions of scripts
		boolean isWin = true;
		POJOFile batfile =  null;
		InputStream batstream = null;

		if(isWin){
			batfile = container.getFile(
					callbacks == null ?
					new POJOPath(RUNAUTOMATION_WIN_FILE) :
					callbacks.createPathCallback.createPath(RUNAUTOMATION_WIN_FILE)
			);
			batstream = getResourceAsStream(loader, RUNAUTOMATION_WIN_RESOURCE);
		}
		if (batstream != null) {
			batfile.create(batstream, true, null);
			batstream.close();
		}

	}

	private static InputStream getResourceAsStream(ClassLoader loader, String resourcePath) {
		InputStream stream = null;
		try {
			stream = loader.getResourceAsStream(resourcePath);
			if (stream == null) {
				/*
				 * The file must be outside of a jar.  It is not clear why the classloader in
				 * Eclipse is not finding resources in the bin directory.  So, look for the
				 * resources under it now.
				 */
				URI uri = BaseProject.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				File binDir = new File(uri);
				File file = new File(binDir, resourcePath);
				if (file.isFile()) {
					stream = new FileInputStream(file);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (stream == null) {
			throw new RuntimeException("Resource " + resourcePath + " not found on classpath");
		}
		return stream;
	}
}
