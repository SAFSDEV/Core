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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2017-10-17    (Lei Wang) Initial release.
 * OCT 26, 2017 (Lei Wang) Ignore the constant fields when persisting.
 */
package org.safs.auth;

import org.safs.persist.PersistableDefault;

/**
 * @author Lei Wang
 */
public class Parameter extends PersistableDefault{

	/** 'user' the key represents the username parameter. */
	public static final String KEY_USER = "user";
	/** 'password' the key represents the password parameter. */
	public static final String KEY_PASSWORD = "password";

	/**
	 * the parameter key, it can be {@link #KEY_PASSWORD} or {@link #KEY_USER}.<br>
	 * In our program, it will tell us what kind of parameter it is.<br>
	 *
	 *  @see #KEY_PASSWORD
	 *  @see #KEY_USER
	 */
	private String key = null;
	/** the parameter name, used to pass to server */
	private String name = null;
	/** the parameter value, used to pass to server */
	private String value = null;

	public Parameter(){
		//Ignore the constant fields whose modifiers are of final static, such as KEY_USER, KEY_PASSWORD.
		super(MODIFIER_CONSTANT, true/* match all field's modifiers */);
	}
	public Parameter(String key, String name, String value){
		this();
		this.key = key;
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
