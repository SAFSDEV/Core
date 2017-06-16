package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.NoAlertPresentException;
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
