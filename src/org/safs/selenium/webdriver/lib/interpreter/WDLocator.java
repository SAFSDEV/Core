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
 * History:
 * DEC 19, 2017 Lei Wang Added method prependFrameInfo() and modified WDType to use prependFrameInfo() for adding frame info to a recognition string.
 *                      Added method resetFrameInfo().
 * JAN 08, 2018 Lei Wang Added method toPrettyString(): avoid the NullPointerException.
 */
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.text.Comparator;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestRun;

/**
 * @author Carl Nagle
 * <br>JUL 28, 2015 Tao Xie Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDLocator extends Locator {

	/** 'type' the key to keep Locator's type field value in a JSON string. */
	public static final String NAME_TYPE 	= "type";
	/** 'value' the key to keep Locator's type field value in a JSON string. */
	public static final String NAME_VALUE 	= "value";

	public static final String DEFAULT_FRAMEINFO = "";
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

	/**
	 * Reset the field 'frameInfo' to {@link #DEFAULT_FRAMEINFO}.<br>
	 * If we have called 'SwitchToFrame' or 'SelectFrame', the field 'frameInfo' will be set so that we can find the element in the correct frame.<br>
	 * Then if we close this session, and start a new session, this field 'frameInfo' should be reset to empty string.<br>
	 * This should be called at the begin (or the end) of a session; in the command 'open', 'get' or 'close' etc.<br>
	 */
	public static void resetFrameInfo(){
		frameInfo = DEFAULT_FRAMEINFO;
	}

	public static void setFrameInfo(String _frameInfo) {
		frameInfo = _frameInfo;
	}

	public static String getFrameInfo() {
		return frameInfo;
	}

	/**
	 * Prepend the {@link #frameInfo} to a Recognition String.
	 * @param rs String, the recognition string
	 * @return String, the recognition string with {@link #frameInfo} if it exists.
	 */
	private static String prependFrameInfo(String rs){
		if(rs==null || rs.trim().isEmpty()) return rs;
		if(frameInfo==null || frameInfo.trim().isEmpty()) return rs;
		if(frameInfo.trim().endsWith(WDLibrary.childSeparator)){
			return frameInfo+rs;
		}else{
			return frameInfo+WDLibrary.childSeparator+rs;
		}
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("type", wdtype.toString());
		o.put("value", value);
		return o;
	}

	@Override
	public String toPrettyString() {
		try{
			//In super class, it executes the following code
			//type.name().toLowerCase() + ":" + value;
			//the field type might be null for class WDLocator
			return super.toPrettyString();
		}catch(Exception e){
			try{
				return wdtype.name().toLowerCase() + ":" + value;
			}catch(Exception e1){
				return wdtype+": "+value;
			}
		}
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
				if(rs.startsWith("//")) rs = "xpath="+ rs;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				String rs = new String(value);
				if(ctx instanceof WDTestRun){
					rs = ((WDTestRun)ctx).replaceVariableReferences(value);
				}
				if(rs.startsWith("//")) rs = "xpath="+ rs;
				rs = prependFrameInfo(rs);
				return SearchObject.getObjects(ctx.driver(),rs);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = new String(value);
				if(ctx instanceof WDTestRun){
					rs = ((WDTestRun)ctx).replaceVariableReferences(value);
				}
				if(rs.startsWith("//")) rs = "xpath="+ rs;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		ID {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String rs = "id="+ value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsById(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = "id="+value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		NAME {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String rs = "name="+ value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByName(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = "name="+value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		LINK {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObject(ctx.driver(), xpath);
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObject(ctx.driver(), xpath);
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObject(ctx.driver(), xpath);
				    }
				    // glob match (yuk!)
				    else {
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   return list.get(i);
				    	   }
				       }
				       return null;
				    }
				}
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObjects(ctx.driver(), xpath);
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObjects(ctx.driver(), xpath);
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObjects(ctx.driver(), xpath);
				    }
				    // glob match (yuk!)
				    else {
				       ArrayList<WebElement> matches = new ArrayList<WebElement>();
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   matches.add(list.get(i));
				    	   }
				       }
				       return matches;
				    }
				}
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObject(ctx.driver(), xpath)==null;
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObject(ctx.driver(), xpath)==null;
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObject(ctx.driver(), xpath)==null;
				    }
				    // glob match (yuk!)
				    else {
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   return false;
				    	   }
				       }
				       return true;
				    }
				}
			}
		},
		LINK_TEXT {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObject(ctx.driver(), xpath);
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObject(ctx.driver(), xpath);
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObject(ctx.driver(), xpath);
				    }
				    // glob match (yuk!)
				    else {
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   return list.get(i);
				    	   }
				       }
				       return null;
				    }
				}
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObjects(ctx.driver(), xpath);
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObjects(ctx.driver(), xpath);
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObjects(ctx.driver(), xpath);
				    }
				    // glob match (yuk!)
				    else {
				       ArrayList<WebElement> matches = new ArrayList<WebElement>();
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   matches.add(list.get(i));
				    	   }
				       }
				       return matches;
				    }
				}
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String real = SRUtilities.stripStringMatchPatternPrefix(value);
				String xpath = "xpath=.//a";
				xpath = prependFrameInfo(xpath);

				// exact match
				if( (SRUtilities.isGlobMatchPattern(value) &&
					!SRUtilities.containsGlobMatchWildcards(real)) ||
					 SRUtilities.isExactMatchPattern(value)){
					xpath += "[normalize-space(text())='"+ real +"']";
					return SearchObject.getObject(ctx.driver(), xpath)==null;
				}else{
					// regexp match
				    if(SRUtilities.isRegexpMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"')]";
						return SearchObject.getObject(ctx.driver(), xpath)==null;
				    }
				    // regexpi (case-insensitive)
				    else if(SRUtilities.isRegexpiMatchPattern(value)){
						xpath += "[matches(normalize-space(text()),'"+ real +"','i')]";
						return SearchObject.getObject(ctx.driver(), xpath)==null;
				    }
				    // glob match (yuk!)
				    else {
				       List<WebElement> list =ctx.getDriver().findElementsByTagName("a");
				       for(int i=0;i<list.size();i++){
				    	   String text = WDLibrary.getText(list.get(i));
				    	   if(Comparator.isGlobMatch(text, real)){
				    		   return false;
				    	   }
				       }
				       return true;
				    }
				}
			}
		},
		CSS_SELECTOR {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String rs = "css="+ value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByCssSelector(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = "css="+ value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs) == null;
			}
		},
		XPATH {
			@Override
			public WebElement find(String value, TestRun ctx) {
				String rs = "xpath="+ value;
				rs = prependFrameInfo(rs);
				return SearchObject.getObject(ctx.driver(), rs);
			}
			@Override
			public List<WebElement> findElements(String value, TestRun ctx) {
				return ctx.driver().findElementsByXPath(value);
			}
			@Override
			public boolean findElementNotPresent(String value, TestRun ctx) {
				String rs = "xpath="+ value;
				rs = prependFrameInfo(rs);
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
