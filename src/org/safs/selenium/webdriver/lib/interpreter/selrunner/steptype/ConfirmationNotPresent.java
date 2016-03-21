package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.NoAlertPresentException;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public class ConfirmationNotPresent extends ConfirmationPresent {

	@Override
	public String get(TestRun ctx) {
		try{ 
			ctx.driver().switchTo().alert();
			return "false";
		}
		catch(NoAlertPresentException ignnore){}
		return "true";
	}
}
