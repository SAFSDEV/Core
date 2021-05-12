/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
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
			String msg = "Step Confirmation found no active Alert Dialog.";
			ctx.log().debug(msg);
			throw (RuntimeException) new RuntimeException(msg, anp).fillInStackTrace();
		}
	}

	public void accept(TestRun ctx){
		try{
			Alert alert = ctx.driver().switchTo().alert();
			alert.accept();
		}catch(NoAlertPresentException anp){
			String msg = "Step Confirmation.accept(): found no active Alert Dialog.";
			ctx.log().debug(msg);
			throw (RuntimeException) new RuntimeException(msg, anp).fillInStackTrace();
		}
	}

	public void dismiss(TestRun ctx){
		try{
			Alert alert = ctx.driver().switchTo().alert();
			alert.dismiss();
		}catch(NoAlertPresentException anp){
			String msg = "Step Confirmation.dismiss(): found no active Alert Dialog.";
			ctx.log().debug(msg);
			throw (RuntimeException) new RuntimeException(msg, anp).fillInStackTrace();
		}
	}

	@Override
	public String cmpParamName() {
		return "text";
	}

}
