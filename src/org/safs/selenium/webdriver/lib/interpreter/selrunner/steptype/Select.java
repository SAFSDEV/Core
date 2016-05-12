/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator.WDType;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.text.Comparator;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SetElementSelected;

public class Select implements StepType, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		step.stringParams.put(WDScriptFactory.ITEM_PARAM, params[2]);
	}

	static final String REGEXP_PREFIX = "regexp:";
	
	@Override
	public boolean run(TestRun ctx) {
		
		// select locator
		List<WebElement> es = ctx.locator(WDScriptFactory.LOCATOR_PARAM).findElements(ctx);
		if(es == null || es.isEmpty()) {
			ctx.log().error("Select did not find any matching SELECT WebElements to evaluate.");
			return false;
		}else{
			ctx.log().info("Select found "+ es.size()+" matching SELECT WebElements to evaluate.");			
		}
		
		String param = ctx.string(WDScriptFactory.ITEM_PARAM);
		if(param == null || param.length()==0) {
			ctx.log().error("Select was provided no OPTION to match.");
			return false;
		}
		
		String att = null;
		String val = null;
		
		int eq = param.indexOf("=");
		if(eq == -1){
			att = "label";//documented default
		    val = param;
		}else if(eq < 1 || eq==param.length()) {
			ctx.log().error("Select OPTION type=value format was invalid.");
			return false;
		}else{		
			att = param.substring(0,eq).trim();
			val = param.substring(eq+1).trim();
		}
		
		if(att.length()==0 || val.length()==0) {
			ctx.log().error("Select OPTION Locator type and/or value were empty.");
			return false;
		}

		WDType wdtype = WDType.ofName(att);
		if(wdtype == null){
			ctx.log().error("Select OPTION Locator '"+ param +"' is not supported.");
			return false;
		}

		WebElement select = null;
		WebElement option = null;
		String curval = null;
		for(int i=0; i<es.size();i++){
			ctx.log().info("Select processing SELECT Element "+ i +" for child OPTION "+ param);
			select = es.get(i); // next SELECT
			List<WebElement> ws = select.findElements(By.tagName("option"));
			if(ws==null||ws.size()==0){
				ctx.log().info("Select found no child OPTIONs for SELECT Element "+ i);
				continue;
			}
			try{
				option = wdtype.find(val, ws, ctx);
				if(option instanceof WebElement) {
					return performSelect(select, option, ctx);
				}
			}catch(Exception x){
				ctx.log().debug("Select matching ignoring "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			}
		}
		ctx.log().error("Select did not find any OPTION matching '"+ param +"'.");
		return false;
	}
	
	protected boolean performSelect(WebElement select, WebElement option, TestRun ctx){
		try{
			org.openqa.selenium.support.ui.Select list = new org.openqa.selenium.support.ui.Select(select);
			if(list.isMultiple()) list.deselectAll();
			option.click();
			return true;
		}catch(Exception x){
			ctx.log().error("Select Option.click "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}
	}
	
	public enum WDType {
		ID {
			@Override
			public WebElement find(String value,  List<WebElement> options, TestRun ctx) {
				for(int i=0;i<options.size();i++){
					WebElement e = options.get(i);
					if(value.equals(e.getAttribute(WDScriptFactory.ID_LOCATORTYPE))) return e;
				}
				return null;
			}
		},
		LABEL {
			@Override
			public WebElement find(String value,  List<WebElement> options, TestRun ctx) {
				String text = null;
				WebElement e = null;
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				boolean isGlob = SRUtilities.isGlobMatchPattern(value)&& SRUtilities.containsGlobMatchWildcards(real);
				boolean isRegexp = SRUtilities.isRegexpMatchPattern(value);
				boolean isRegexpi = SRUtilities.isRegexpiMatchPattern(value);
				boolean isExact = SRUtilities.isExactMatchPattern(value) ||
						          (! isGlob && !isRegexp && !isRegexpi);
				ctx.log().info("Select has match pattern '"+ value +"' for text matching '"+real+"'");
				ctx.log().info("Select has "+ options.size() + " OPTIONs.");
				for(int i=0;i<options.size();i++){
					text = null;
					e = options.get(i);
					text = WDLibrary.getText(e);
					ctx.log().info("Select OPTION "+ i +" text: '"+ text +"'");
					if(text == null|| text.length()==0) continue;
					if(isExact){
						if(Comparator.isExactMatch(text,  real) ||
						   Comparator.isExactMatch(text.trim(), real)){
						       ctx.log().info("Select exact match on text: '"+ text +"'");
						       return e;
						}
					}else if(isRegexp){
						if( Comparator.isRegexpMatch(text, real) ||
							Comparator.isRegexpMatch(text.trim(), real)||
							(text.trim().length()==0 && " ".matches(real))){
							    ctx.log().info("Select regexp match on text: '"+ text +"'");
							    return e;
						}
					}else if(isRegexpi){
						if( Comparator.isRegexpiMatch(text, real)||
							Comparator.isRegexpiMatch(text.trim(), real)||
							(text.trim().length()==0 && " ".matches(real))){
						        ctx.log().info("Select regexpi match on text: '"+ text +"'");
						        return e;
						}
					}else if(isGlob){
						if( Comparator.isGlobMatch(text, real)||
							Comparator.isGlobMatch(text.trim(), real)){
						        ctx.log().info("Select glob match on text: '"+ text +"'");
						        return e;
						}
					}
				}
				ctx.log().info("Select did not find an OPTION match for text: '"+ real +"'");
				return null;
			}
		},
		VALUE {
			@Override
			public WebElement find(String value, List<WebElement> options, TestRun ctx) {
				for(int i=0;i<options.size();i++){
					WebElement e = options.get(i);
					if(value.equals(e.getAttribute(WDScriptFactory.VALUE_LOCATORTYPE))) return e;					
				}
				return null;
			}
		},
		INDEX {
			@Override
			public WebElement find(String value,  List<WebElement> options, TestRun ctx) {
				try{
					return options.get(Integer.parseInt(value));
				}catch(Exception x){}
				return null;
			}
		};
				
		public abstract WebElement find(String value, List<WebElement> options, TestRun ctx);
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
		
		public static WDType ofName(String name) {
			return WDType.valueOf(name.toUpperCase());
		}
	}	
}
