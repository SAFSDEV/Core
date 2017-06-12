package sample.testruns;

import org.safs.selenium.webdriver.SeleniumPlus;
import sample.testcases.TestCase1;

public class TestRun1 extends SeleniumPlus{

	/**
	 * Define testcase order here
	 * 
	 */
	
	@Override
	public void runTest() throws Throwable {
		
		/*
		 * Start Browser calls from TestCase1 file
		 * and it execute first 
		 */
		try{
			TestCase1.startBrowser();
			TestCase1.LogIn();
			TestCase1.takeScreenshot();
		}
		catch(Exception x){ Logging.LogTestFailure(x.getMessage()); }
		
		TestCase1.stopBrowser();		
		
		// add other testcases here
		
		
	}
}
