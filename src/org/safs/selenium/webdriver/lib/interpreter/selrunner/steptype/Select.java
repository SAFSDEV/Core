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
/**
 * 2017-12-27 (Lei Wang) Modified WDType.LABEL and WDType.VALUE: Replace the char &nbsp; (160) by normal empty char 32 and try to compare again.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.text.Comparator;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

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
				String tempText = null;
				WebElement e = null;
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				boolean isGlob = SRUtilities.isGlobMatchPattern(value)&& SRUtilities.containsGlobMatchWildcards(real);
				boolean isRegexp = SRUtilities.isRegexpMatchPattern(value);
				boolean isRegexpi = SRUtilities.isRegexpiMatchPattern(value);
				boolean isExact = SRUtilities.isExactMatchPattern(value) ||
						          (! isGlob && !isRegexp && !isRegexpi);
				ctx.log().info("Select has match pattern '"+ value +"' for text matching '"+real+"'");
				ctx.log().info("Select has "+ options.size() + " OPTIONs.");

				int lastIndex = -1;
				boolean canTryAgain = false;

				for(int i=0;i<options.size();i++){

					if(lastIndex==i){
						//that means it is the second time we try, we cannot try it again.
						canTryAgain = false;
					}else{//else, we will get the next item
						lastIndex = i;
						text = null;
						e = options.get(i);
						text = WDLibrary.getText(e);
						//This is the first time we compare the text, we still have another chance.
						canTryAgain = true;
					}

					if(text == null|| text.length()==0) continue;

					ctx.log().info("Select OPTION "+ i +" text: '"+ text +"'");
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

					if(canTryAgain){
						//replace the char &nbsp; (160) by normal empty char 32.
						tempText = StringUtils.replaceChar(text, (char)160/* &nbsp; */, ' ');
						if(!tempText.equals(text)){
							text = tempText;
							//decrement the index, and we will try again
							i--;
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
				String text = null;
				WebElement result = null;

				for(int i=0;i<options.size();i++){
					WebElement e = options.get(i);
					text = e.getAttribute(WDScriptFactory.VALUE_LOCATORTYPE);
					if(value.equals(text)){
						return e;
					}
					//replace the char &nbsp; (160) by normal empty char 32.
					text = StringUtils.replaceChar(text, (char)160/* &nbsp; */, ' ');
					if(value.equals(text)) return e;
				}
				return result;
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
