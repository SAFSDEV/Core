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
package org.safs.projects.seleniumplus.projects

/**
 * 2018-05-23 (Lei Wang) Modified withSeleniumPlusEnv(): start the 'Selenium Server' by script C:\SeleniumPlus\extra\RemoteServer.bat
 * 2018-05-30 (Lei Wang) Modified withSeleniumPlusEnv(): roll back code and let the framework to start the 'Selenium Server' automatically.
 * 2019-02-15 (Lei Wang) Modified stopSeleniumServer(): Catch all Exceptions: after upgrading to selenium-3.14, selenium will throw out FileNotFoundException when calling 'http://localhost:4444/selenium-server/driver/?cmd=shutDownSeleniumServer'.
 */

import static org.junit.Assert.*
import static org.safs.Constants.ENV_SELENIUM_PLUS

import org.safs.LogFileUtil
import org.safs.TestUtil
import org.safs.WebappServer
import org.safs.projects.seleniumplus.builders.AppMapBuilder
import org.safs.selenium.webdriver.lib.WDLibrary

import groovy.io.FileType

public class SeleniumPlusTestUtil {
	private static final FAILED = "**FAILED**"

	public static final String CORE_TEST_JVM_DEBUG_PORT = "org.safs.test.forked.jvm.debug.port"

	def testUtil = new TestUtil()
	private static final CONTEXT_NAME = "test"

	/**
	 * With the Eclipse framework mocked, initialize SeleniumPlus; create
	 * a project with projectName and projectType; generate Map.java, compile
	 * the classes, and run the testClass.
	 *
	 * @param map containing projectName, projectType, and testClass.
	 */
	public void buildProjectAndRunTest(map, closure) {
		boolean generateMapJava = map.generateMapJava != null ? map.generateMapJava : true
		def afterProjectCreationClosure = map.afterProjectCreationClosure
		def expectedFailureClosure = map.expectedFailureClosure
		def browser = map.browser

		def projectName = map.projectName
		def projectType = map.projectType
		def testClass = map.testClass

		withSeleniumPlusEnv(map) {

			def project = createProject(projectName, projectType)
			if (browser) {
				replaceBrowser(project, browser)
			}
			if (afterProjectCreationClosure) {
				afterProjectCreationClosure(project)
			}

			if (generateMapJava) {
				generateMapJavaFile(project)
			}
			compileClasses(project)
			runTest(project, testClass, expectedFailureClosure, map.seleniumPlusEnvDir)
			if (closure) {
				closure(project)
			}
		}
	}

	private replaceBrowser(project, browser) {
		def projectDir = project.getProjectDir()
		def testIniFile = new File(projectDir, "test.ini")
		def text = testIniFile.text
		def dotall = '(?s)'  // makes "." match newlines as well
		def multiline = '(?m)' // expect string to have multiple lines
		def matcher = text =~ ("$dotall$multiline(.*^BROWSER=)\\S*(.*)" as String)
		assert matcher.matches()
		text = matcher.group(1) + browser + matcher.group(2)
		testIniFile.write(text)
	}

	/**
	 * Initialize SeleniumPlus and a mock of the Eclipse framework before
	 * calling the input closure that runs a test.  After the test closure
	 * returns, the Selenium server is stopped.
	 *
	 * @param closure called with a reference to the Eclipse mock to run the test.
	 */
	public void withSeleniumPlusEnv(map, closure) {
		// set the closure's delegate to this class so methods of this class can
		// be invoked without prefixing the class instance reference.
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this);

		def seleniumPlusInitClosure = { tempDir ->
			def workspaceDir = tempDir

			BaseProject.init(workspaceDir)

			// initialize SeleniumPlus
			BaseProject.SELENIUM_PLUS = System.getenv(ENV_SELENIUM_PLUS)

			try {
				/*
				 * Since tests have failed in the past with
				 * "WDLibrary.startBrowser(): Caused By: org.openqa.selenium.WebDriverException,
				 * The process has not exited yet therefore no result is available",
				 * call the following method to kill any web drivers that may be running on the test server.
				 */
				try{
					WDLibrary.stopSeleniumServer(null);
				}catch(Exception){ /*ignore it*/}

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
		if (map.tempDir) {
			seleniumPlusInitClosure(map.tempDir)
		} else {
			testUtil.withTempDir(seleniumPlusInitClosure)
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
		assertNotNull("Project is null - check console output", project)
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

	private void runTest(project, testClass, expectedFailureClosure=null, seleniumPlusEnvDir=null) {
		def projectDir = project.projectDir
		def binDir = project.binDir

		def ant = new AntBuilder()
		def logFile = new File(projectDir, "log.txt")
		def logFileUtil = new LogFileUtil()
		def javaClassPath = System.getProperty("java.class.path")
		def forkedJVMDebugPort = System.getProperty(CORE_TEST_JVM_DEBUG_PORT, "")
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
				if (seleniumPlusEnvDir) {
					env(key:'SELENIUM_PLUS', file:seleniumPlusEnvDir)
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
			if (line.contains(FAILED)) {
				if (expectedFailureClosure) {
					expectedFailureClosure(line)
				} else {
					assert !line.contains(FAILED)
				}
			}
		}
	}

	public runGroovyBridgeTest(map, closure) {
		def afterProjectCreationClosure = { project ->
			def srcDir = new File("${project.getProjectDir()}${project.getSrcDir()}")

			srcDir.eachFileRecurse(FileType.FILES) { file ->
				if (file.name.endsWith(".java")) {
					file.delete()
				}
			}

			def groovyFile = new File(srcDir, "sample/testcases/TestCase1.groovy")
			groovyFile.write """
package sample.testcases

public class TestCase1 {
	public void someTestMethod() {
		test {
			try {
				${map.testText}
			} catch (Throwable t) {
				logging.LogTestFailure(t.getMessage())
			}
		}
	}

	private void test(closure) {
		def bridge = new org.safs.selenium.webdriver.GroovyBridge()
		bridge.setClosure(closure)
		bridge.main([] as String[])
	}

	public static void main(args) {
		// this will most likely be done by JUnit
		def testCase = new TestCase1()
		testCase.someTestMethod()
	}
}
"""
		}

		def tempMap = map.clone()

		def newProperties = [
			generateMapJava: false,
			afterProjectCreationClosure:afterProjectCreationClosure,
		]
		tempMap.putAll(newProperties)

		buildProjectAndRunTest(tempMap, closure)
	}

	private stopSeleniumServer() {
		try {
			new URL("http://localhost:4444/selenium-server/driver/?cmd=shutDownSeleniumServer").text
			Thread.currentThread().sleep(2000)
		} catch (java.net.ConnectException e) {
			// the server is not running - OK.
		}catch (Exception e) {
			//catch what ever exception, we will stop the server in the finally clause.
		}finally{
			try{
				WDLibrary.stopSeleniumServer(null);
			}catch(Exception){ /*ignore it*/}
		}
	}

	/**
	 * Starts an embedded web server with a web app that mimics a small part of the Google home
	 * page.
	 *
	 * Then, execute a GroovyBridge test.
	 *
	 * 2018-05-25 (Lei Wang) Modified the mock page content to fit our application SAPDemo
	 *
	 * @param map
	 * @param closure
	 * @return
	 */
	public runGroovyBridgeTestWithWebServer(map, closure) {
		def webappServer = new WebappServer()

		testUtil.withTempDir { tempDir ->
			//create a working area for the webapp
			def webappDir = new File(tempDir, CONTEXT_NAME)

			def promptFile = new File(webappDir, "prompt")
			promptFile.parentFile.mkdirs()

			/*
			 * Write a GSP that will be executed by the web server when the
			 * test/prompt URL is read.
			 * If the Authorization header is not present,
			 * then the response will request a Basic Authentication.
			 * When the Basic Authentication dialog is filled in,
			 * the browser will read the URL again with the Authorization
			 * header set.
			 * At this point, a very minimal page that mimics a small part
			 * of the Google home page is returned.
			 */
			promptFile.write """
<%
if (request.getHeader("Authorization")) {
	println "<title>Google</title> <div id='viewport' class='sapUiBody'> <a href='nowhere'>Mort.Calc</a></div>"
} else {
	response.setHeader("WWW-Authenticate", 'Basic realm="SomeService"')
	response.sendError(401, "")
}
%>
"""
			/*
			 * Start a webserver that will listen for requests to http://localhost:port/$CONTEXT_NAME
			 * and will serve up content from webappDir.
			 */
			webappServer.withRunningEmbeddedServer(
					contextName:CONTEXT_NAME,
					webappDir:webappDir,
					) { serverData ->

				def rootUrl = "http://${serverData.hostname}:${serverData.webappPort}/$CONTEXT_NAME"
				def promptUrl = "$rootUrl/prompt"

				def testText = map.testTextClosure(promptUrl)

				def tempMap = map.clone()
				tempMap.putAll([
					tempDir  :   tempDir,
					testText :   testText,
				])

				runGroovyBridgeTest(tempMap, closure)
			}
		}
	}
}
