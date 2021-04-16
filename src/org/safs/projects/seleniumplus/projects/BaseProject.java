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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAY 25, 2018 (Lei Wang) Corrected the comments of some constant.
 * MAY 10, 2018	(Lei Wang) Made some public static fields final because they are constants.
 *                       Removed some public static fields which should be local variables inside a method; Only kept 'srcDir', which
 *                       is used in class com.sas.seleniumplus.eclipse.EclipseCallbacks. TODO 'srcDir' should be removed too.
 *                       Added some constants for 'cycle' 'suite' test level.
 * MAY 18, 2018	(Lei Wang) Assigned DriverConstant.DEFAULT_CONFIGURE_FILENAME_TEST_INI to constant 'TESTINI_FILE'.
 *                       DriverConstant.DEFAULT_CONFIGURE_FILENAME_TEST_INI is used by Runner as default .ini configuration file.
 *                       TESTINI_FILE is used as the default .ini configuration file during generating project.
 *                       To keep the consistency, I made them equal.
 * SEP 27, 2018	(Lei Wang) Modified addToProjectStructure(): Add spring configuration file to this test project.
 * NOV 23, 2018	(Lei Wang) Added constant SRC_SRC_DIRS.
 */
package org.safs.projects.seleniumplus.projects;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.safs.Constants;
import org.safs.projects.common.projects.callbacks.Callbacks;
import org.safs.projects.common.projects.pojo.POJOContainer;
import org.safs.projects.common.projects.pojo.POJOFile;
import org.safs.projects.common.projects.pojo.POJOFolder;
import org.safs.projects.common.projects.pojo.POJOPath;
import org.safs.projects.common.projects.pojo.POJOProject;
import org.safs.projects.seleniumplus.popupmenu.FileTemplates;
import org.safs.tools.drivers.DriverConstant;

public class BaseProject {
	/** holds path to SeleniumPlus install directory -- once validated. */
	public static String SELENIUM_PLUS;
	public static String srcDir;

	/** "SELENIUM_PLUS" the system environment variable name holding the path where SeleniumPlus has been installed */
	public static final String SELENIUM_PLUS_ENV = org.safs.Constants.ENV_SELENIUM_PLUS;

	/** "Tests" */
	public static final String SRC_TEST_DIR = "Tests";
	/** "src" */
	public static final String SRC_SRC_DIR = "src";

	/**
	 * Possible source folder names, currently it has 2 items:
	 * <ul>
	 * <li>"Tests"
	 * <li>"src"
	 * </ul>
	 */
	public static final String[] SRC_SRC_DIRS = {SRC_TEST_DIR, SRC_SRC_DIR};

	/** "testcycle" */
	public static final String SRC_TESTCYCLE_SUBDIR = "testcycle";
	/** "testsuite" */
	public static final String SRC_TESTSUITE_SUBDIR = "testsuite";
	/** "testcase" */
	public static final String SRC_TESTCASE_SUBDIR = "testcase";

	/** "testcases" */
	public static final String SRC_TESTCASES_SUBDIR = "testcases";
	/** "testruns" */
	public static final String SRC_TESTRUNS_SUBDIR = "testruns";

	/** "/org/safs/projects/seleniumplus/projects/samples" */
	public static final String SAMPLES_RESOURCE_PATH = "/org/safs/projects/seleniumplus/projects/samples";

	/** "Cycle" */
	public static final String TESTCYCLE_FILE = "Cycle";

	/** "Suite" */
	public static final String TESTSUITE_FILE = "Suite";

	/** "Cases1" */
	public static final String TESTCASE_FILE = "Cases1";

	private static final String SUFFIX_TXT = ".txt";

	/** "TestCase1" */
	public static final String TESTCASECLASS_FILE = "TestCase1";
	/** "/org/safs/projects/seleniumplus/projects/samples/TestCase1.java" */
	public static final String TESTCASECLASS_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestCase1.java";
	/** "/org/safs/projects/seleniumplus/projects/samples/TestCase1.java.txt" */
	public static final String TESTCASECLASS_TXT_RESOURCE = TESTCASECLASS_RESOURCE+SUFFIX_TXT;
	/** "TestRun1" */
	public static final String TESTRUNCLASS_FILE = "TestRun1";
	/** "/org/safs/projects/seleniumplus/projects/samples/TestRun1.java" */
	public static final String TESTRUNCLASS_RESOURCE = SAMPLES_RESOURCE_PATH + "/TestRun1.java";
	/** "/org/safs/projects/seleniumplus/projects/samples/TestRun1.java.txt" */
	public static final String TESTRUNCLASS_TXT_RESOURCE = TESTRUNCLASS_RESOURCE+SUFFIX_TXT;

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
	public static final String TESTINI_FILE = DriverConstant.DEFAULT_CONFIGURE_FILENAME_TEST_INI;
	/** runAutomation.bat */
	public static final String RUNAUTOMATION_WIN_FILE = "runAutomation.bat";
	/** /org/safs/projects/seleniumplus/projects/samples/runautomation.bat */
	public static final String RUNAUTOMATION_WIN_RESOURCE = SAMPLES_RESOURCE_PATH + "/runautomation.bat";

	/** App.map */
	public static final String APPMAP_FILE = "App.map";
	/** /org/safs/projects/seleniumplus/projects/samples/App.map */
	public static final String APPMAP_RESOURCE = SAMPLES_RESOURCE_PATH + "/App.map";
	/** App_en.map */
	public static final String APPMAP_EN_FILE = "App_en.map";
	/** /org/safs/projects/seleniumplus/projects/samples/App_en.map */
	public static final String APPMAP_EN_RESOURCE = SAMPLES_RESOURCE_PATH + "/App_en.map";
	/** AppMap.order */
	public static final String APPMAP_ORDER_FILE = "AppMap.order";
	/** /org/safs/projects/seleniumplus/projects/samples/AppMap.order */
	public static final String APPMAP_ORDER_RESOURCE = SAMPLES_RESOURCE_PATH + "/AppMap.order";

	private static JarFile jarFile;

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

	public static void main(String[] args){
		init(new File("c:\\temp"));
		createProject(PROJECTNAME_SAMPLE, null, "sas", PROJECTTYPE_SAMPLE, null);
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

		String testrunDir = null;
		String testcaseDir = null;

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
			testcaseDir = srcDir + File.separator + projectName.toLowerCase() +File.separator+ SRC_TESTCASES_SUBDIR;
			testrunDir =  srcDir + File.separator+ projectName.toLowerCase() +File.separator+ SRC_TESTRUNS_SUBDIR;

		/*} else if (projectType.equalsIgnoreCase(PROJECTTYPE_ADVANCE)){

			srcDir = SRC_SRC_DIR;
			testcaseDir = srcDir + File.separator+"com"+File.separator + companyName.toLowerCase() + File.separator+ projectName.toLowerCase()+ File.separator+ SRC_TESTS_SUBDIR;
			testrunDir = srcDir + File.separator+"com"+File.separator + companyName.toLowerCase() + File.separator+ projectName.toLowerCase()+ File.separator+ SRC_SUITES_SUBDIR;
		*/
		} else {
			// internal error
			throw new RuntimeException("Unsupported Project type '"+projectType+"'!");
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

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		POJOFolder srcPkg = newProject.getFolder(paths[0]);
		if (srcPkg.exists()){
			/** Create spring configuration file */
			POJOFile file = srcPkg.getFile(Constants.SPRING_CONFIG_CUSTOM_FILE);
			//test's root package is the lower case of the project name
			InputStream stream = FileTemplates.springConfig(newProject.getName().toLowerCase());
			file.create(stream, true, null);
			if (stream != null) stream.close();

			/** Create log4j configuration file */
			file = srcPkg.getFile(Constants.LOG4J2_CONFIG_FILE);
			stream = FileTemplates.log4j2Config();
			file.create(stream, true, null);
			if (stream != null) stream.close();
		}

		/** Create sample test class */
		String testClass = TESTCASECLASS_FILE;
		POJOFolder testPkg = newProject.getFolder(paths[1]);

		POJOFile testclass = null;
		String fullQualifiedTestClassName = null;

		if (testPkg.exists()){
			testclass = testPkg.getFile(testClass + ".java");
			InputStream testclassstream = null;
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){
				testclassstream = getResourceAsStream(loader, TESTCASECLASS_TXT_RESOURCE);
			//TODO: support non-SAMPLE case.
//			} else {
//				testclassstream = FileTemplates.testClass(newProject.getName(),newPackage,mapPkg, testClass);
			}

			testclass.create(testclassstream, true, null);
			if (testclassstream != null) testclassstream.close();
			if (jarFile != null) jarFile.close();

			//generate the test package and full-qualified test class name.
			String testPackage = null;
			String srcPath = null;
			String packagePath = null;
			int index = -1;
			try{
				srcPath = srcPkg.getPath();
				packagePath = testPkg.getPath();
				index = packagePath.indexOf(srcPath);
				if(index >-1){
					testPackage = packagePath.substring(index+srcPath.length());
				}
			}catch(Exception e){}

			try{
				if(testPackage==null){
					srcPath = paths[0];
					packagePath = paths[1];
					index = packagePath.indexOf(srcPath);
					if(index >-1){
						testPackage = packagePath.substring(index+srcPath.length());
					}else{
						testPackage = paths[1];
					}
				}
			}catch(Exception e){}

			if(testPackage!=null){
				if(testPackage.startsWith(File.separator)) testPackage = testPackage.substring(1);
				testPackage = testPackage.replace(File.separator, ".");
				fullQualifiedTestClassName = testPackage+"."+testClass;
			}
		}

		/** Create run tests */
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
			if (jarFile != null) jarFile.close();
		}

		/** Map and Map order files */
		POJOFolder mapFolder = newProject.getFolder(DATAPOOL_DIR);

		if (mapFolder.exists()) {

			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){

				POJOFile appMap = mapFolder.getFile(newProject.getName()+APPMAP_FILE);
				//InputStream mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App.map");
				InputStream mapstream = getResourceAsStream(loader, APPMAP_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();
				if (jarFile != null) jarFile.close();

				appMap = mapFolder.getFile(newProject.getName()+APPMAP_EN_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App_zh.map");
				mapstream = getResourceAsStream(loader, APPMAP_EN_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();
				if (jarFile != null) jarFile.close();

				appMap = mapFolder.getFile(APPMAP_ORDER_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/AppMap.order");
				mapstream = getResourceAsStream(loader, APPMAP_ORDER_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();
				if (jarFile != null) jarFile.close();

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

		/** create test.ini file */
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
			if(testclass!=null && fullQualifiedTestClassName!=null){
				batstream = FileTemplates.runAutomationBatch(fullQualifiedTestClassName);
			}else{
				batstream = getResourceAsStream(loader, RUNAUTOMATION_WIN_RESOURCE);
			}
		}
		if (batstream != null) {
			batfile.create(batstream, true, null);
			batstream.close();
			if (jarFile != null) jarFile.close();
		}
	}

	private static InputStream getResourceAsStream(ClassLoader loader, String resourcePath) {
		InputStream stream = null;
		try {
			stream = loader.getResourceAsStream(resourcePath);
			if (stream == null) {
				URI uri = BaseProject.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				if (uri.getPath().endsWith(".jar")) {
					/*
					 * This is the case where PowerMock is being used.
					 * For some reason the MockClassloader has problems finding resources.
					 * So, read the entry directly from the jar.
					 */
					File file = new File(uri);
					jarFile = new JarFile(file);
					ZipEntry entry = jarFile.getEntry(resourcePath.substring(1));
					if (entry != null) {
						stream = jarFile.getInputStream(entry);
					}
				} else {
					/*
					 * The file must be outside of a jar.  It is not clear why the classloader in
					 * Eclipse is not finding resources in the bin directory.  So, look for the
					 * resources under it now.
					 */
					File binDir = new File(uri);
					File file = new File(binDir, resourcePath);
					if (file.isFile()) {
						stream = new FileInputStream(file);
					}
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
