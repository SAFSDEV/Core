package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.NoAlertPresentException;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public class ConfirmationPresent implements Getter, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		try{
			if(params[1].length() > 0){
				step.stringParams.put("variable", params[1]);
			}
		}catch(Throwable ignore){}
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
