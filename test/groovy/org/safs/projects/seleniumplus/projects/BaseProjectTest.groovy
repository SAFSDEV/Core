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
 * Developer Logs:
 * <pre>
 *
 * This class will ran as 'JUnit Test'.<br>
 * If we want to debug it with the 'SAFS Core Project':
 * 1. We start the JUnit Test with following VM parameters: ("org.safs.test.forked.jvm.debug.port" is defined by SeleniumPlusTestUtil.CORE_TEST_JVM_DEBUG_PORT)
 *    -Dorg.safs.test.forked.jvm.debug.port=8888
 *    -Dtest.timeout=3600000
 * 2. In Eclipse, add "Remote Java Application" for project "SAFS Core" with the port number defined above by "org.safs.test.forked.jvm.debug.port"
 *    Launch this "Remote Java Application" in Debug mode, then your breakpoints will get hit.
 *    Note that each test method (annotated by @Test) that forks a JVM will wait for a debugger to connect.
 *    So, you have to be aware of which test runs first and which one you want to debug.
 *    Regardless, you will need to connect to all of them one-by-one to get them to run.
 *    For those you do not want to debug, you can immediately disconnect, but you will need to connect to them to get the test running.
 * </pre>
 *
 * 2018-05-24 (Lei Wang) Use our internal application SAPDemo to test. Google changes from time to time, not stable.
 */
import static org.junit.Assert.*

import org.junit.Test

@SuppressWarnings('MethodName') // prevent CodeNarc from complaining about String method names
class BaseProjectTest {
	def seleniumPlusTestUtil = new SeleniumPlusTestUtil()

	@Test
	void "Test Basic Authentication with username:password in URL"() {
		def urlInUse
		def browser = 'chrome'
		def brokenBrowsers = ['explorer']

		def testTextClosure = { url ->
			url = url - "http://"
			url = "http://username:password@$url"
			urlInUse = url

			return """
		withBrowser("$url", "browserID") {
			if (! component.GUIDoesExist(map.SAPDemoPage.TabMortCalc)) {
				AbortTest("Tab Mort.Calc Not Found.  Test Aborting")
			}

		}
"""
		}

		def expectedMessageFound = false
		def expectedFailureClosure = null
		if (brokenBrowsers.contains(browser)) {
			expectedFailureClosure = { line ->
				if (line.contains("Failed to navigate to $urlInUse")) {
					expectedMessageFound = true
				}
			}
		}

		seleniumPlusTestUtil.runGroovyBridgeTestWithWebServer(
			projectName:  SampleProjectNewWizard.PROJECT_NAME,
			projectType:  BaseProject.PROJECTTYPE_SAMPLE,
			testClass:    'sample.testcases.TestCase1',
			testTextClosure:     testTextClosure,
			expectedFailureClosure: expectedFailureClosure,
			browser:      browser,
		) { project ->
			if (brokenBrowsers.contains(browser)) {
				assert expectedMessageFound
			}
		}
	}

	@Test
	void "Test GroovyBridge withBrowser"() {

		def testText = """
				withBrowser(map.URLSAPDemo, map.BrowserID) {
					if (WebDriver()?.getWindowHandles()?.size() != 1) {
						AbortTest("browser was not opened")
					}
				}
				def handles = WebDriver()?.getWindowHandles()
				if (handles?.size() > 0) {
					AbortTest("browser was not closed")
				}
"""

		def count = 0
		def throwable = null
		while (throwable == null && (count < 2)) {
			try {
				seleniumPlusTestUtil.runGroovyBridgeTest(
					projectName:  SampleProjectNewWizard.PROJECT_NAME,
					projectType:  BaseProject.PROJECTTYPE_SAMPLE,
					testClass:    'sample.testcases.TestCase1',
					testText:     testText,
				) { project ->

				}
			} catch (Throwable t) {
				throwable = t
			}
			count++
		}
		if (throwable != null) {
			throw new RuntimeException("Test failed on count: $count", throwable)
		}
	}

	@Test
	void "Test GroovyBridge logging"() {

		def testText = """
					AbortTest("Test Aborting XYZ")
"""

		def expectedMessageFound = false
		def expectedFailureClosure = { line ->
			assert line.contains("XYZ")
			expectedMessageFound = true
		}

		seleniumPlusTestUtil.runGroovyBridgeTest(
			projectName:  SampleProjectNewWizard.PROJECT_NAME,
			projectType:  BaseProject.PROJECTTYPE_SAMPLE,
			testClass:    'sample.testcases.TestCase1',
			testText:     testText,
			expectedFailureClosure: expectedFailureClosure,

		) { project ->
			assert expectedMessageFound
		}
	}

	@Test
	void "Test GroovyBridge with SAMPLE project"() {

		def testText = """
		withBrowser(map.URLSAPDemo, map.BrowserID) {
			withHighlight {
				if (! Component.Click(map.SAPDemoPage.TabMortCalc, map.TopLeft)) {
					AbortTest("Failed to switch to tab Mortgage. Test Aborted!")
				}
				if (! EditBox.SetTextValue(map.SAPDemoPage.InputTerm, "50")) {
					AbortTest("Could not set term input box.  Test Aborting")
				}
				if (! EditBox.SetTextValue(map.SAPDemoPage.InputPrincipal, "50000")) {
					AbortTest("Could click principal input box.  Test Aborting")
				}
				if (! EditBox.SetTextValue(map.SAPDemoPage.InputRate, "0.04")) {
					AbortTest("Could click rate input box.  Test Aborting")
				}

                //map.TopLeft() represents the coordinates relative to to click
				if (! Component.Click(map.SAPDemoPage.ButtonSubmit, map.TopLeft)) {
					AbortTest("Failed to click Submit Button. Test Aborted!")
				}

		        //"value" is the property (for this input box) holding the component's text value
		        //other properties such as 'text', 'innerText' etc. can be tried to verify
                //VerifyProperty with 3 parameters is unsupported this time??? Lei comment the following call
		        //Component.VerifyProperty(map.SAPDemoPage.InputMonthlyPayment, "value", "\\\$84")

                def filename = GetVariableValue(map.TabMortCalcScreenshot)
				Component.GetGUIImage(map.SAPDemoPage.SAPDemoPage, filename)
			}
		}
"""

		seleniumPlusTestUtil.runGroovyBridgeTest(
			projectName:  SampleProjectNewWizard.PROJECT_NAME,
			projectType:  BaseProject.PROJECTTYPE_SAMPLE,
			testClass:    'sample.testcases.TestCase1',
			testText:     testText,

		) { project ->
			checkForScreenshot(project)
		}
	}

	@Test
	void "Test GroovyBridge: call withHighlight first"() {

		def testText = """
		withHighlight {
			withBrowser(map.URLSAPDemo, map.BrowserID) {

				//"take screenshot":
				def filename = GetVariableValue(map.SAPDemoScreenshot)
				component.GetGUIImage(map.SAPDemoPage.SAPDemoPage, filename)
			}
		}
"""

		seleniumPlusTestUtil.runGroovyBridgeTest(
			projectName:  SampleProjectNewWizard.PROJECT_NAME,
			projectType:  BaseProject.PROJECTTYPE_SAMPLE,
			testClass:    'sample.testcases.TestCase1',
			testText:     testText,

		) { project ->
			checkForScreenshot(project)
		}
	}

	@Test
	void "Test SAMPLE project"() {
		seleniumPlusTestUtil.buildProjectAndRunTest(
				projectName:  SampleProjectNewWizard.PROJECT_NAME,
				projectType:  BaseProject.PROJECTTYPE_SAMPLE,
				testClass:    'sample.testcases.TestCase1',
		) { project ->
			checkForScreenshot(project)
		}
	}

	private void checkForScreenshot(project) {
		def projectDir = project.projectDir
		def actualDir = new File(projectDir, "Actuals")
		assertTrue(actualDir.exists())
		def files = actualDir.listFiles() as List

		assertFalse("The screenshot was not taken.", files.isEmpty())
	}
}
