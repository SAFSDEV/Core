/*
* Copyright 2012 Sauce Labs
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.sebuilder.interpreter;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;

/**
 * A Selenium locator. Extended to allow new methods of object location via more Types
 * @author Original author zarkonnen<br>
 * @author Carl Nagle Adding default constructor for effective extension
 */
public class Locator {
	public Type type;
	public String value;

	public Locator(){}
	
	public Locator(Type type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public Locator(String type, String value) {
		this.type = Type.ofName(type);
		this.value = value;
	}

	public Locator(Locator l) {
		type = l.type;
		value = l.value;
	}
	
	public WebElement find(TestRun ctx) {
		return type.find(value, ctx);
	}
	
	public List<WebElement> findElements(TestRun ctx) {
		return type.findElements(value, ctx);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("type", type.toString());
		o.put("value", value);
		return o;
	}
	
	@Override
	public String toString() {
		try { return toJSON().toString(); } catch (JSONException e) { throw new RuntimeException(e); }
	}
	
	public enum Type {
		ID {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver.findElementById(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver.findElementsById(value);
			}
		},
		NAME {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver.findElementByName(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver.findElementsByName(value);
			}
		},
		LINK_TEXT {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver.findElementByLinkText(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver.findElementsByLinkText(value);
			}
		},
		CSS_SELECTOR {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver.findElementByCssSelector(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver.findElementsByCssSelector(value);
			}
		},
		XPATH {
			@Override
			public WebElement find(String value, TestRun ctx) {
				return ctx.driver.findElementByXPath(value);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver.findElementsByXPath(value);
			}
		};
				
		public abstract WebElement find(String value, TestRun ctx);
		public abstract List<WebElement> findElements(String value, TestRun ctx);
		
		@Override
		public String toString() {
			return name().toLowerCase().replace("_", " ");
		}
		
		public static Type ofName(String name) {
			return Type.valueOf(name.toUpperCase().replace(" ", "_"));
		}
	}
}
