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
 * History:<br>
 *
 *  APR 19, 2019    (Lei Wang) Modified method getHostNameAndPort(): Use "GET" instead of "POST" to make HTTP request.
 *  MAY 19, 2020    (Lei Wang) Modified method getHostNameAndPort(): Use our own API to execute "GET" request: selenium doesn't include the Apache http-core classes anymore.
 *
 */
package org.safs.selenium.util;

import java.net.URL;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.SessionId;
import org.safs.IndependantLog;
import org.safs.net.HttpRequest;
import org.safs.net.IHttpRequest.HttpCommand;
import org.safs.net.IHttpRequest.Key;

public class GridInfoExtractor{

	public static String[] getHostNameAndPort(String hostName, int port,
	                                           SessionId session) {
		String[] hostAndPort = new String[2];
		String errorMsg = "Failed to acquire remote webdriver node and port info. Root cause: ";

		try {
			String sessionURL = "http://" + hostName + ":" + port + "/grid/api/testsession?session=" + session;
			HttpRequest request = new HttpRequest();
			Map<String, Object> results = request.execute(HttpCommand.GET, sessionURL, true, null, "");
			String response = (String) results.get(Key.RESPONSE_TEXT.value());

			IndependantLog.debug("GridInfoExtractor.getHostNameAndPort(): get session info\n"+response);

			JSONObject object = new JSONObject(response);
			URL myURL = new URL(object.getString("proxyId"));
			if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
				hostAndPort[0] = myURL.getHost();
				hostAndPort[1] = Integer.toString(myURL.getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(errorMsg, e);
		}
		return hostAndPort;
	}

}
