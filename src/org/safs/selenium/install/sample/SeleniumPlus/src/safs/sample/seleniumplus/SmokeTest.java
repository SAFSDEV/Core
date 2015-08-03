/**
 * 
 */
package safs.sample.seleniumplus;

import org.safs.selenium.webdriver.SeleniumPlus;

/**
 * @author Carl Nagle
 *
 */
public class SmokeTest extends SeleniumPlus {
	
	public SmokeTest () { super(); }

	@Override
	public void runTest() throws Throwable {
		Runner.logGENERIC("I'm starting my SeleniumPlus Test.", "(Isn't this exciting!)");
		
		String value = Misc.GetAppMapValue("MainWin", "AnyComp1");
		String expectedValue = "<recognition string for Chinese>";
		if(expectedValue.equals(value)){
			Logging.LogTestSuccess("Misc.GetAppMapValue succeed.");
		}
		
		Runner.command("Pause", "5");
		Runner.logPASSED("I've finished my SeleniumPlus Test.", "(WOW! Wasn't that exciting!)");
	}

}
