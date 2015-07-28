/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
		this("Embedded Throwalbe: "+th.getClass().getSimpleName()+":"+th.getMessage());
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
