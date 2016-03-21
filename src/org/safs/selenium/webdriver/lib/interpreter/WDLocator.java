/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.Locator.Type;

/**
 * @author canagl
 * <br>JUL 28, 2015 SCNTAX Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDLocator extends Locator {

	public WDType wdtype;
	private static String frameInfo = "";

	/**
	 * @param l
	 */
	public WDLocator(Locator l) {
		super(l);
		wdtype = WDType.ofName(l.type.name());
	}

	/**
	 * @param type
	 * @param value
	 */
	public WDLocator(Type type, String value) {
		super(type, value);
		wdtype = WDType.ofName(type.name());
	}
	
	/**
	 * @param type
	 * @param value
	 */
	public WDLocator(String type, String value) {
		try{ this.type = Type.ofName(type);}
		catch(Exception ignore){}
		wdtype = WDType.ofName(type);
		this.value = value;
	}

	
	public static void setFrameInfo(String _frameInfo) {
		frameInfo = _frameInfo;
	}
	
	public static String getFrameInfo() {
		return frameInfo;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("type", wdtype.toString());
		o.put("value", value);
		return o;
	}
	
	@Override
	public WebElement find(TestRun ctx) {
		return wdtype.find(value, ctx);
	}
	
	/**
	 * @param ctx
	 * @return true if element was NOT found in 2 second timeout period.
	 */	
	public boolean findElementNotPresent(TestRun ctx) {
		try{WebDriverGUIUtilities._LASTINSTANCE.setWDTimeout(1);}catch(Exception x){
			ctx.log().debug("WDLocator.findElementNotPresent unable to change WebDriver timeouts!");
		}
		boolean b = false;
		try{ b = wdtype.findElementNotPresent(value, ctx);}catch(Exception x){
			ctx.log().debug("WDLocator.findElementNotPresent ignoring "+
		                    x.getClass().getSimpleName()+", "+x.getMessage());
		}
		try{WebDriverGUIUtilities._LASTINSTANCE.resetWDTimeout();}catch(Exception x){
			ctx.log().debug("WDLocator.findElementNotPresent unable to reset WebDriver timeouts!");
		}
		return b;		
	}
	
	@Override
	public List<WebElement> findElements(TestRun ctx) {
		return wdtype.findElements(value, ctx);
	}
	
	public enum WDType {
		MAP {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String rs = new String(value);
				if(ctx instanceof WDTestRun){
					rs = ((WDTestRun)ctx).replaceVariableReferences(value);
				}
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				String rs = new String(value);
				if(ctx instanceof WDTestRun){
					rs = ((WDTestRun)ctx).replaceVariableReferences(value);
				}
				if(null != frameInfo)
					return SearchObject.getObjects(ctx.driver(), frameInfo + rs);
				return SearchObject.getObjects(ctx.driver(),rs);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = new String(value);
				if(ctx instanceof WDTestRun){
					rs = ((WDTestRun)ctx).replaceVariableReferences(value);
				}
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + rs) == null;

				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		ID {
			@Override
			public WebElement find(String value, TestRun ctx) {
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + "id="+ value);
				return SearchObject.getObject(ctx.driver(), "id="+ value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsById(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = frameInfo == null ? "" : frameInfo ;
				rs += "id="+value;
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		NAME {
			@Override
			public WebElement find(String value, TestRun ctx) {
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + "name="+ value);
				return SearchObject.getObject(ctx.driver(), "name="+ value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByName(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = frameInfo == null ? "" : frameInfo ;
				rs += "name="+value;
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		LINK {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver().findElementByLinkText(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByLinkText(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				return ctx.driver().findElementByLinkText(value)==null;
			}
		},
		LINK_TEXT {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver().findElementByLinkText(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByLinkText(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				return ctx.driver().findElementByLinkText(value)==null;
			}
		},
		CSS_SELECTOR {
			@Override
			public WebElement find(String value, TestRun ctx) {
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + "css="+ value);
				return SearchObject.getObject(ctx.driver(), "css="+ value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByCssSelector(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = frameInfo == null ? "" : frameInfo ;
				rs += "css="+value;
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		XPATH {
			@Override
			public WebElement find(String value, TestRun ctx) {
				if(null != frameInfo)
					return SearchObject.getObject(ctx.driver(), frameInfo + "xpath="+ value);
				return SearchObject.getObject(ctx.driver(), "xpath="+ value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByXPath(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = frameInfo == null ? "" : frameInfo ;
				rs += "xpath="+value;
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		};
				
		public abstract WebElement find(String value, TestRun ctx);
		public abstract List<WebElement> findElements(String value, TestRun ctx);
		public abstract boolean findElementNotPresent(String value, TestRun ctx);
		
		@Override
		public String toString() {
			return name().toLowerCase().replace("_", " ");
		}
		
		public static WDType ofName(String name) {
			return WDType.valueOf(name.toUpperCase().replace(" ", "_"));
		}
	}	
}
