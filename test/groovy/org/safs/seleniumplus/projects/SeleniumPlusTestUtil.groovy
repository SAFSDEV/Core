package org.safs.seleniumplus.projects

import static org.safs.Constants.ENV_SELENIUM_PLUS

import org.safs.seleniumplus.builders.AppMapBuilder
import org.safs.seleniumplus.projects.callbacks.CreateProjectCallback
import org.safs.seleniumplus.projects.callbacks.CreateFolderCallback
import org.safs.seleniumplus.projects.pojo.POJOFolder
import org.safs.seleniumplus.projects.callbacks.GetFolderCallback
import org.safs.seleniumplus.projects.pojo.POJOProject

public class SeleniumPlusTestUtil {
	def testUtil = new TestUtil()

	/**
	 * With the Eclipse framework mocked, initialize SeleniumPlus; create
	 * a project with projectName and projectType; generate Map.java, compile
	 * the classes, and run the testClass.
	 * 
	 * @param map containing projectName, projectType, and testClass.
	 */
	public void buildProjectAndRunTest(map, closure) {
		def projectName = map.projectName
		def projectType = map.projectType
		def testClass = map.testClass

		withSeleniumPlusEnv {
			
			def project = createProject(projectName, projectType)
			generateMapJavaFile(project)
			compileClasses(project)
			runTest(project, testClass)
			if (closure) {
				closure(project)
			}
		}
	}
	
	/**
	 * Initialize SeleniumPlus and a mock of the Eclipse framework before
	 * calling the input closure that runs a test.  After the test closure
	 * returns, the Selenium server is stopped.
	 * 
	 * @param closure called with a reference to the Eclipse mock to run the test.
	 */
	public void withSeleniumPlusEnv(closure) {
		// set the closure's delegate to this class so methods of this class can
		// be invoked without prefixing the class instance reference.
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this);

		testUtil.withTempDir() { tempDir ->
			def workspaceDir = tempDir
			
			BaseProject.init(workspaceDir)
			
			// initialize SeleniumPlus
			BaseProject.SELENIUM_PLUS = System.getenv(ENV_SELENIUM_PLUS)
			
			try {
				// call the closure that runs the test
				closure()
						
			} finally {
				/*
				 * Since the selenium server has a working directory in the 
				 * workspace (at the project root), it has to be stopped
				 * or the temporary directory cannot be deleted.
				 */
				stopSeleniumServer()
			}
		}
	}
	
	public createProject(projectName, projectType) {
		def location = null
		def companyName = "sas"
		def callbacks = null
		
		def project = BaseProject.createProject(
			projectName,
			location,
			companyName,
			projectType,
			callbacks,
		)
		project
	}
	
	public void generateMapJavaFile(project) {
		// generate Map.java
		def builder = new AppMapBuilder(project)
//		builder.setBuildConfig(project.getBuildConfig(""))
		
		def args = [:]
		builder.build(args)
	}

	public void compileClasses(project) {
		def projectDir = project.projectDir
		def binDir = project.binDir
		
		// Compile the test classes to bin with the other tests
		def srcDir = new File(projectDir, "Tests")
		testUtil.compile(srcDir:srcDir, destDir:binDir)
	}
	
	public void runTest(project, testClass) {
		def projectDir = project.projectDir
		def binDir = project.binDir

		def ant = new AntBuilder()
		def logFile = new File(projectDir, "log.txt")
		def logFileUtil = new LogFileUtil()
		def javaClassPath = System.getProperty("java.class.path")
		def forkedJVMDebugPort = 0
		def forkedJVMDebugSuspend = true
		def result = logFileUtil.withLogFile(logFile) {
			ant.java(
				classname:testClass,
				failonerror:true,
				fork:true,
				dir:projectDir,
				output:logFile,
				) {
				classpath {
					pathelement(location: binDir)
					pathelement(path:javaClassPath)
				}
				if (forkedJVMDebugPort) {
					jvmarg(value:'-Xdebug')
					def suspend = forkedJVMDebugSuspend ? 'y' : 'n'
					jvmarg(value:"-Xrunjdwp:transport=dt_socket,address=${forkedJVMDebugPort},server=y,suspend=$suspend")
				}
			}
		}
		if (result.throwable) throw result.throwable
		
		logFile.eachLine { line ->
			assert !line.contains("**FAILED**")
		}
	}
	
	private stopSeleniumServer() {
		try {
			new URL("http://localhost:4444/selenium-server/driver/?cmd=shutDownSeleniumServer").text
			Thread.currentThread().sleep(2000)
		} catch (java.net.ConnectException e) {
			// the server is not running - OK.
		}
	}
}
