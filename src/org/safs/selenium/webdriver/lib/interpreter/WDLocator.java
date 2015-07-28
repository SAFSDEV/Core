/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.SearchObject;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.Locator.Type;

/**
 * @author Carl Nagle
 * <br>JUL 28, 2015 Tao Xie Added SwitchToFrame and SwitchToFrameIndex support
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
	
	public static void setFrameInfo(String _frameInfo) {
		frameInfo = _frameInfo;
	}
	
	public static String getFrameInfo() {
		return frameInfo;
	}
	
	/**
	 * @param type
	 * @param value
	 */
	public WDLocator(String type, String value) {
		super(type, value);
		wdtype = WDType.ofName(type);
	}

	public WebElement find(TestRun ctx) {
		return WDType.ofName(wdtype.name()).find(value, ctx);
	}
	
	public List<WebElement> findElements(TestRun ctx) {
		return WDType.ofName(wdtype.name()).findElements(value, ctx);
	}
	
	public enum WDType {
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
		};
				
		public abstract WebElement find(String value, TestRun ctx);
		public abstract List<WebElement> findElements(String value, TestRun ctx);
		
		@Override
		public String toString() {
			return name().toLowerCase().replace("_", " ");
		}
		
		public static WDType ofName(String name) {
			return WDType.valueOf(name.toUpperCase().replace(" ", "_"));
		}
	}	
}
