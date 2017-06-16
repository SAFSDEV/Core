/**
 * JUN 16, 2017 (SBJLWA) Modified processParams(): it should accept only one parameter as Alert.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public class Confirmation implements Getter, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		//Confirmation is almost the same as an Alert, except it has one more button 'cancel'.
		//It should have the same parameter as Alert.
		//storeConfirmation, variable
		//verifyConfirmation, "confirmation message"
//		try{
//			if(params[1].length() > 0){
//				step.stringParams.put("text", params[1]);
//			}
//			if(params[2].length() > 0){
//				step.stringParams.put("variable", params[2]);
//			}
//		}catch(Throwable ignore){}

		Utils.setParam(step, this, params[1]);
	}

	@Override
	public String get(TestRun ctx) {
		try{
			Alert alert = ctx.driver().switchTo().alert();
			String text = alert.getText();
			ctx.log().info("Step Confirmation.getText() received: "+ text);
			return text;
		}catch(NoAlertPresentException anp){
			ctx.log().debug("Step Confirmation found no active Alert Dialog.");
			throw (RuntimeException) new RuntimeException("Step Confirmation found no active Alert Dialog.",anp).fillInStackTrace();
		}
	}

	@Override
	public String cmpParamName() {
		return "text";
	}

}
