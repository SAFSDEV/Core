package org.safs.seleniumplus.projects

import org.safs.seleniumplus.projects.BaseProject

import static org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@SuppressWarnings('MethodName') // prevent CodeNarc from complaining about String method names
class BaseProjectTest {
	def seleniumPlusTestUtil = new SeleniumPlusTestUtil()
	
	@Test
	void "Test SAMPLE project"() {
		seleniumPlusTestUtil.buildProjectAndRunTest(
				projectName:  SampleProjectNewWizard.PROJECT_NAME,
				projectType:  BaseProject.PROJECTTYPE_SAMPLE,
				testClass:    'sample.testcases.TestCase1',
		) { project ->
			/*
			 * Make sure the screenshot was taken.
			 */
			def projectDir = project.projectDir
			def actualDir = new File(projectDir, "Actuals")
			assertTrue(actualDir.exists())
			def files = actualDir.listFiles() as List
			
			assertFalse("The screenshot was not taken.", files.isEmpty())
		}
	}
}
