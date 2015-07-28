/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.util;

import java.util.Map;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.tools.stringutils.StringUtilities;

/**
 * 
 * History:<br>
 * 
 *  <br>   NOV 27, 2014    (SBJLWA) Initial release.
 */
public class JSException extends SeleniumPlusException{
	
	private static final long serialVersionUID = 6051073569943746825L;
	
	public static final String ERROR_CODE_NOT_SET 	= String.valueOf(JavaScriptFunctions.ERROR_CODE_NOT_SET);
	public static final String ERROR_CODE_RESERVED 	= String.valueOf(JavaScriptFunctions.ERROR_CODE_RESERVED);
	public static final String ERROR_CODE_EXCEPTION 	= String.valueOf(JavaScriptFunctions.ERROR_CODE_EXCEPTION);
	
	protected Object jsError = null;
	
	public JSException(String detailMessage) {
		super(detailMessage);
	}
	public JSException(String detailMessage, int code) {
		super(detailMessage, String.valueOf(code));
	}
	public JSException(Object jsError, String detailMessage, int code) {
		super(detailMessage, String.valueOf(code));
		this.jsError = jsError;
	}
	
	public static JSException instance(Object jsError, int code) {		
		return new JSException(jsError, getJsErrorMessage(jsError), code);
	}
	
	public JSException(String detailMessage, String code) {
		super(detailMessage, code);
	}
	public JSException(String detailMessage, String code, String info) {
		super(detailMessage, code, info);
	}
	
	public JSException(Object obj, String methodName, String msg) {
		super(obj, methodName, msg);
	}

	public JSException(Object obj, String msg) {
		super(obj, msg);
	}

	public JSException(Throwable th) {
		super(th);
	}
	public JSException(String message, Throwable th) {
		super(message, th);
	}
	
	public Object getJsError(){ return jsError;}
	public static String getJsErrorMessage(Object jsError){
		String debugmsg = StringUtils.debugmsg(false);
		String message = null;
		
		try {
			if(jsError instanceof String) message = ((String) jsError);
			else if(jsError instanceof Map){
				Map<?, ?> errorMap = (Map<?, ?>) jsError;
				//For FireFox, "message" contains the "error message".
				try{ message = StringUtilities.getString(errorMap, "message");}catch(Exception ignore){}
				//For IE, "description" contains the "error message".
				try{ if(message==null) message = StringUtilities.getString(errorMap, "description");}catch(Exception ignore){}
			}else{
				IndependantLog.warn(debugmsg+" Need new code to get message from JS Error Object "+jsError.getClass().getName());
				message = jsError.toString();
			}
		}catch(Throwable th){
			IndependantLog.error(debugmsg+" Fail.", th);
			message = "Unknown Error.";
		}
		return message;
	}
}
