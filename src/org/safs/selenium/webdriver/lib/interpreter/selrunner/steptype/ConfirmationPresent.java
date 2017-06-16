package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.NoAlertPresentException;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public class ConfirmationPresent implements Getter, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		if(params.length>1) Utils.setParam(step, this, params[1]);
	}

	@Override
	public String get(TestRun ctx) {
		try{
			ctx.driver().switchTo().alert();
			return "true";
		}
		catch(NoAlertPresentException ignnore){}
		return "false";
	}

	@Override
	public String cmpParamName() {
		return null;
	}

}
