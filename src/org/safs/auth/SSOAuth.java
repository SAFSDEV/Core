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
 * DEC 21, 2016 (Lei Wang) Initial release.
 * OCT 16, 2017 (Lei Wang) Removed field SimpleAuth and added 2 fields 'parameters' and 'requiredCookies'.
 * OCT 24, 2017 (Lei Wang) Used a list of Parameter instead of a string to represent parameters used for authentication.
 */
package org.safs.auth;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.safs.Printable;
import org.safs.persist.PersistableDefault;

/**
 * @author Lei Wang
 *
 */
public class SSOAuth extends PersistableDefault implements Auth{

	/**
	 * The URL providing "Authentication Service", such as "https://sso.server.com/opensso/UI/Login"<br>
	 * Refer to document <a href="https://docs.oracle.com/cd/E19681-01/820-3885/6nfcuutfg/index.html">Accessing the Authentication Service User Interface with a Login URL</a><br>
	 * It can be appended with the {@link #parameters} to form a full URL to make authentication, such as {@link #ssoAuthenticationURL}?{@link #parameters}.
	 * These parameters should be separated by character '&', such as 'realm=xxx&IDToken1=user&IDToken2=password'.<br>
	 * @see #parameters
	 */
	private String ssoAuthenticationURL = null;

	/**
	 * "&" the separator to separate the {@link #parameters} passed to 'Authentication Service Login URL'.<br>
	 * @see #parameters
	 */
	private static final String SEPARATOR_AMPERSAND = "&";
	/**
	 * "=" the separator to separate the parameter's name and value, such as name=value.<br>
	 * @see #parameters
	 */
	private static final String SEPARATOR_ASSIGN = "=";
	/**
	 * According to document <a href="https://docs.oracle.com/cd/E19681-01/820-3885/6nfcuutfg/index.html">Accessing the Authentication Service User Interface with a Login URL</a>, we
	 * can pass some parameters to the "Authentication Service", such as 'realm=xxx', 'IDToken1=user' and 'IDToken2=password'.<br>
	 * <b>How to know what parameters/values are needed?</b><br>
	 * 1. We need to consult the document <a href="https://docs.oracle.com/cd/E19681-01/820-3885/6nfcuutfg/index.html">Accessing the Authentication Service User Interface with a Login URL</a> .<br>
	 * 2. We also need to consult the administrator of a specific 'Authentication Service'.<br><br>
	 *
	 * It can be appended to {@link #ssoAuthenticationURL}, such as {@link #ssoAuthenticationURL}?{@link #parameters}, to form a full URL to make authentication.
	 * These parameters should be separated by character '&', such as 'realm=xxx&IDToken1=user&IDToken2=password'.<br>
	 * @see #ssoAuthenticationURL
	 */
	private  List<Parameter> parameters = null;

	/**
	 * ";" the separator to separate the cookies' name.
	 * @see #requiredCookies
	 */
	private static final String SEPARATOR_COOKIES = ";";
	/**
	 * The name of cookies returned from the SSO Server after authentication. They are separated by {@link #SEPARATOR_COOKIES}, such as "iPlanetDirectoryPro;amlbcookie"<br>
	 * The cookie <a href="https://idmdude.com/2014/02/26/understanding-the-iplanetdirectorypro-cookie/">'iPlanetDirectoryPro' and 'amlbcookie'</a> are required by SSO Authentication,<br>
	 * Their name may be reconfigured to other string, refer to <a href="http://www.zyxware.com/articles/5540/solved-how-to-update-cookie-name-in-openam">Update cookie name in OpenAM</a><br>
	 */
	private String requiredCookies = null;
	/**
	 * At least 2 cookies 'iPlanetDirectoryPro' and 'amlbcookie'  are required.
	 * @see #requiredCookies
	 */
	private static final int MINIMUM_REQUIRED_COOKIES = 2;

	public SSOAuth(){
		//We will ignore the constant fields
		super(Modifier.FINAL & Modifier.STATIC);
	}

	/**
	 * @return the ssoAuthenticationURL
	 */
	public String getSsoAuthenticationURL() {
		return ssoAuthenticationURL;
	}
	/**
	 * @param ssoAuthenticationURL the ssoAuthenticationURL to set
	 */
	public void setSsoAuthenticationURL(String ssoAuthenticationURL) {
		this.ssoAuthenticationURL = ssoAuthenticationURL;
	}

	/**
	 * @return List<String>, the requiredCookies
	 */
	public List<String> getRequiredCookies() {
		String[] result = null;
		if(requiredCookies!=null){
			result =requiredCookies.split(SEPARATOR_COOKIES);
		}
		//at least 2 cookies 'iPlanetDirectoryPro' and 'amlbcookie'  are required.
		if(result==null || result.length<MINIMUM_REQUIRED_COOKIES){
			throw new IllegalArgumentException(" NOT enough cookie names are provided: It seems that at least 2 cookies 'iPlanetDirectoryPro' and 'amlbcookie' are required!");
		}
		return Arrays.asList(result);
	}
	/**
	 * @param requiredCookies the requiredCookies to set
	 */
	public void setRequiredCookies(String requiredCookies) {
		this.requiredCookies = requiredCookies;
	}

	/**
	 * @return String, the URL parameters in format "param1=value1&param2=value2&param3=value3"
	 */
	public String getURLParameters() {
		StringBuilder urlParameters = new StringBuilder();
		if(parameters!=null){
			for(Parameter p: parameters){
				urlParameters.append(p.getName()+SEPARATOR_ASSIGN+p.getValue()+SEPARATOR_AMPERSAND);
			}
			//remove the last character '&'
			if(urlParameters.length()>0){
				urlParameters.setLength(urlParameters.length()-1);
			}
		}

		return urlParameters.toString();
	}

	/**
	 * @return String, the possible parameter username's value. It can be null if not exist.
	 */
	public String getUserName() {
		for(Parameter p:parameters){
			if(Parameter.KEY_USER.equalsIgnoreCase(p.getKey())){
				return p.getValue();
			}
		}
		return null;
	}

	/**
	 * @return String, the possible parameter password's value. It can be null if not exist.
	 */
	public String getPassword() {
		for(Parameter p:parameters){
			if(Parameter.KEY_PASSWORD.equalsIgnoreCase(p.getKey())){
				return p.getValue();
			}
		}
		return null;
	}

	/**
	 * @return the parameters
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	protected void adjustTabulation(Printable printable){
		printable.setTabulation(getTabulation()+1);
	}
}
