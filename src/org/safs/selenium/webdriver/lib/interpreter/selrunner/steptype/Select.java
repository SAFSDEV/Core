/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SetElementSelected;

public class Select implements StepType, SRunnerType {

	public static final String ITEM_PARAM = "item";
	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		step.stringParams.put(ITEM_PARAM, params[2]);
	}

	static final String REGEXP_PREFIX = "regexp:";
	
	@Override
	public boolean run(TestRun ctx) {
		
		List<WebElement> es = ctx.locator("locator").findElements(ctx);
		if(es == null || es.isEmpty()) return false;
		
		String param = ctx.string(ITEM_PARAM);
		if(param == null || param.length()==0) return false;
		
		String att = null;
		String val = null;
		
		int eq = param.indexOf("=");
		if(eq < 1 || eq==param.length()) return false;
		
		att = param.substring(0,eq).trim();
		val = param.substring(eq+1).trim();
		if(att.length()==0 || val.length()==0) return false;

		boolean isRegExp = false;
		if(val.startsWith(REGEXP_PREFIX)){
			isRegExp = true;
			val = val.substring(REGEXP_PREFIX.length());				
		}
		
		WebElement e = null;
		String curval = null;
		for(int i=0; i<es.size();i++){
			e = es.get(i);
			curval = e.getAttribute(att);
			if(curval == null) continue;
			if(isRegExp){
				if(val.matches(curval)) return performClick(ctx, e);
			}else{
				if(val.equals(curval)) return performClick(ctx, e);
			}
		}
		return false;
	}
	
	protected boolean performClick(TestRun ctx, WebElement e){
		e.click();
		return true;
	}
}
