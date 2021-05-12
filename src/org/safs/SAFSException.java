/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs;

/**
 * <br><em>Purpose:</em> our user defined application exception used with this package.
 * <p>
 * @author  Doug Bauman
 * @since   JUN 09, 2003
 *
 *   <br>   JUN 09, 2003    (DBauman) Original Release
 **/
@SuppressWarnings("serial")
public class SAFSException extends Exception {

	public static final String NAME_SeleniumException 							= "com.thoughtworks.selenium.SeleniumException";
	public static final String NAME_FT_WindowActivateFailedException 			= "com.rational.test.ft.WindowActivateFailedException";
	public static final String NAME_FT_PropertyNotFoundException				= "com.rational.test.ft.PropertyNotFoundException";
	public static final String NAME_FT_ObjectNotFoundException					= "com.rational.test.ft.ObjectNotFoundException";
	public static final String NAME_FT_TargetGoneException						= "com.rational.test.ft.TargetGoneException";
	public static final String NAME_FT_UnsupportedActionException				= "com.rational.test.ft.UnsupportedActionException";

	public static final String CODE_UNKONWN_ERROR 								= SAFSException.class.getSimpleName()+":CODE_UNKONWN_ERROR";
	public static final String CODE_CONTAINER_ISFULL							= SAFSException.class.getSimpleName()+":CODE_CONTAINER_ISFULL";
	public static final String CODE_CONTAINER_ISEMPTY							= SAFSException.class.getSimpleName()+":CODE_CONTAINER_ISEMPTY";
	public static final String CODE_TIMEOUT_REACHED								= SAFSException.class.getSimpleName()+":CODE_TIMEOUT_REACHED";
	public static final String CODE_ACTION_NOT_SUPPORTED						= SAFSException.class.getSimpleName()+":CODE_ACTION_NOT_SUPPORTED";
	public static final String CODE_CONTENT_ISNULL								= SAFSException.class.getSimpleName()+":CODE_CONTENT_ISNULL";
	public static final String CODE_ERROR_SET_FILE_ATTR							= SAFSException.class.getSimpleName()+":CODE_ERROR_SET_FILE_ATTR";

	public static final String CODE_WindowActivateFailedException 			= SAFSException.class.getSimpleName()+":CODE_WindowActivateFailedException";
	public static final String CODE_PropertyNotFoundException				= SAFSException.class.getSimpleName()+":CODE_PropertyNotFoundException";
	public static final String CODE_ObjectNotFoundException					= SAFSException.class.getSimpleName()+":CODE_ObjectNotFoundException";
	public static final String CODE_TargetGoneException						= SAFSException.class.getSimpleName()+":CODE_TargetGoneException";
	public static final String CODE_UnsupportedActionException				= SAFSException.class.getSimpleName()+":CODE_UnsupportedActionException";

	protected String code = CODE_UNKONWN_ERROR;

	/** <br><em>Purpose:</em> constructor
	 * @param                     msg, String, the string to pass along to our 'super'
	 **/
	public SAFSException (String msg) {
		super(msg);
	}
	/**
	 *
	 * @param detailMessage String, the string to pass along to 'super class' Exception
	 * @param code String, the ID to identify an instance of  SAFSException
	 */
	public SAFSException(String detailMessage, String code) {
		this(detailMessage);
		this.code = code;
	}

	/** <br><em>Purpose:</em> constructor, this one takes a 'this' reference from the caller
	 * @param                     obj, Object, nominally the caller ('this') so that we
	 * can add their getClass().getName() to the msg; if null then not used.
	 * @param                     msg, String, the string to pass along to our 'super'
	 **/
	public SAFSException (Object obj, String msg) {
		super((obj==null?"":obj.getClass().getName()+": ")+msg);
	}
	/** <br><em>Purpose:</em> constructor, this one takes a 'this' reference from the caller
	 * @param                     obj, Object, nominally the caller ('this') so that we
	 * can add their getClass().getName() to the msg; if null then not used.
	 * @param                     methodName, String, the name of the method to make part of msg.
	 * @param                     msg, String, the string to pass along to our 'super'
	 **/
	public SAFSException (Object obj, String methodName, String msg) {
		super((obj==null?"":obj.getClass().getName()+(methodName==null?": ":"."))+
				(methodName==null?"":methodName+": ") +
				msg);
	}

	/**
	 * Use a Throwable to create a new SAFSException
	 * @param th
	 */
	public SAFSException (Throwable th) {
		super("Embedded Throwable: "+th.getClass().getSimpleName()+":"+th.getMessage(), th);
	}

	public SAFSException(String message, Throwable th) {
		super(message, th);
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
