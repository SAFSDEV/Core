package sample.testcases;

import org.safs.selenium.webdriver.SeleniumPlus;

import sample.Map;

public class TestCase1 extends SeleniumPlus{

	private static String browserID;

	/**
	 * Start WebBrowser, FireFox is default browser
	 * URL: User must specify "http://.." protocol in URL.
	 * BrowserID: Unique applicationid/browserid.
	 * Browser : Default FF or specify one
	 * Timeout : Timeout in seconds.
	 * @throws Exception if the StartBrowser command did not work
	 */
	public static void startBrowser() throws Throwable{
		String url = GetVariableValue(Map.GoogleURL);
		browserID = GetVariableValue(Map.GoogleBrowser);
		if (! StartWebBrowser(url, browserID))
			AbortTest("StartWebBrowser Unsuccessful. Cannot Proceed.");
		//StartWebBrowser("http://www.google.com", browserID,SelectBrowser.BROWSER_NAME_FIREFOX,"20");
		//StartWebBrowser("http://www.google.com",browserID,SelectBrowser.BROWSER_NAME_IE);
		//StartWebBrowser("http://www.google.com",browserID,SelectBrowser.BROWSER_NAME_CHROME);
	}


	/**
	 *  Take picture
	 *  See Map.LogIn.SignIn for RS
	 */
	public static void takeScreenshot(){
		String filename = GetVariableValue(Map.SignInScreenshot);
		GetGUIImage(Map.LogIn.OneGoogle, filename);
	}



	/**
	 * Log in
	 * For Map.LogIn.UserName, see App.map file
	 */
	public static void LogIn() throws Throwable{
		// Click call with literal coords is NOT as maintainable as the one at bottom
		if(! Click(Map.Google.SignIn,"5,5"))
			AbortTest("Google Not Found.  Test Aborting");
		
		EditBox.SetTextValue(Map.LogIn.UserName, GetVariableValue(Map.GoogleUser));
		Click(Map.LogIn.UserNameNext,Map.TopLeft);
		
		EditBox.SetTextValue(Map.LogIn.Passwd, GetVariableValue(Map.GooglePassword));
		// This is the preferred type of Click call using coords
		Click(Map.LogIn.SignIn, Map.TopLeft);
	}


	/**
	 * Stop the WebBrowser
	 */
	public static void stopBrowser(){
		StopWebBrowser(browserID);
	}

	/**
	 * User can test cases locally here
	 *
	 * To run test
	 * Right click on TestCase1.java
	 * Run As Selenium+1 Test.
	 *
	 * Always comment testcases once tested
	 *
	 */

	@Override
	public void runTest() throws Throwable {

		try{
		    startBrowser();
		    
		    //Turn on the "highlight" during debug so that the component will be circled with a red rectangle
		    SeleniumPlus.Highlight(true);
		    LogIn();		    
		    //Turn off the "highlight" for better performance after debug.
		    SeleniumPlus.Highlight(false);
		    
		    takeScreenshot();
		    
		}
		catch(Throwable x){
		    Logging.LogTestFailure(x.getMessage());
		}
		stopBrowser();

	}
}