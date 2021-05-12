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
package org.safs.rest.service;

import static org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter.PASSWORD;
import static org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter.USERID;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolVersion;

public class RESTImpl {

	public Response request(String serviceId, String requestMethod, String relativeURI, String headers,
			Object body) throws Exception {

		Service service = Services.getService(serviceId);
		ProtocolVersion protVersion = service.getProtocolVersionObject();
		String defaultURI = service.getBaseURL();

		// This is only going to be returned in the Response for informational
		// purposes...
		Request safsRequest = new Request();
		safsRequest.set_method(requestMethod);
		safsRequest.set_message_body(body);
		safsRequest.set_headers(headers);
		
		// do not prepend defaultURI if given URI is fullpath. ex: login redirection
		if(relativeURI.startsWith("http")) 
			safsRequest.set_uri(relativeURI);
		else if(defaultURI.endsWith("/") && relativeURI.startsWith("/"))
			safsRequest.set_uri(defaultURI + relativeURI.substring(1));
		else if(defaultURI.endsWith("/") || relativeURI.startsWith("/"))
			safsRequest.set_uri(defaultURI + relativeURI);
		else
			safsRequest.set_uri(defaultURI + "/" + relativeURI);
		
		safsRequest.set_protocol_version(protVersion.toString());

		Response safsResponse = new Response();
		safsResponse.set_request(safsRequest);

		Map<String,String> headersMap = Headers.getHeadersMapFromMultiLineString(headers);

		//TODO: Use HttpServerTestingFramework constants when they are added
		Map<String,Object> request = new HashMap<String,Object>();
		request.put("method", requestMethod);
		request.put("path", relativeURI);
		request.put("headers", headersMap);
		request.put("body", body);
		request.put("contentType", headersMap.get(Headers.CONTENT_TYPE));
		request.put("protocolVersion", protVersion);
		request.put(USERID, service.getUserId());
		request.put(PASSWORD, service.getPassword());

		headersMap.remove(Headers.CONTENT_TYPE);

		long defaultTimeout = Timeouts.getDefaultTimeouts().getMillisToTimeout();
		request.put("timeout", defaultTimeout);

		HttpClientPOJOAdapter clientAdapter = (HttpClientPOJOAdapter) service.getClientAdapter();

		Map<String,Object> response = clientAdapter.execute(defaultURI, request);

		int status = (int) response.get("status");

		safsResponse.set_status_code(status);

		@SuppressWarnings("unchecked")
		Map<String,String> respHeaders = (Map<String,String>) response.get("headers");

		safsResponse.set_headers(respHeaders);

		safsResponse.set_entity_body(response.get("body"));
		safsResponse.set_content_type((String) response.get("contentType"));

		return safsResponse;
	}

	/**
	 * Parse a URI and put the parameters in a query map.  Also, return the
	 * path with the parameters taken off.
	 */
	public static Map<String,Object> parseUri(String uriString) throws URISyntaxException {
		URI uri = new URI(uriString);
		Map<String, String> query = new HashMap<String,String>();
		List<NameValuePair> nvplist = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

		for (NameValuePair nvp : nvplist) {
			query.put(nvp.getName(), nvp.getValue());
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("query", query);
		ret.put("path", uri.getPath());
		return ret;
	}

}
