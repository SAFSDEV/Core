package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public class Confirmation implements Getter, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		try{ 
			if(params[1].length() > 0){
				step.stringParams.put("text", params[1]);
			}
			if(params[2].length() > 0){
				step.stringParams.put("variable", params[2]);
			}
		}catch(Throwable ignore){}
	}

	@Override
	public String get(TestRun ctx) {
		try{ return ctx.driver().switchTo().alert().getText(); }
		catch(Throwable ignnore){}
		return null;
	}

	@Override
	public String cmpParamName() {
		return "text";
	}

}
