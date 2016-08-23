package org.safs.rest.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.testframework.HttpClient5Adapter;
import org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter;
import org.apache.hc.client5.http.utils.URLEncodedUtils;
import org.apache.hc.core5.http.NameValuePair;

public class RESTImpl {
	
	private HttpClientPOJOAdapter implAdapter = new HttpClient5Adapter();

	public Response request(String serviceId, String requestMethod, String relativeURI, String headers,
			Object body) throws Exception {
		
		Request safsRequest = new Request();
		safsRequest.set_request_method(requestMethod);
		safsRequest.set_message_body(body);
		safsRequest.set_headers(headers);
		safsRequest.set_request_uri(relativeURI);
		//TODO: set_http_version
		
		Response safsResponse = new Response();		
		safsResponse.set_request(safsRequest);

		Service service = Services.getService(serviceId);
		
		String defaultURI = service.getBaseURL();
		Map<String,String> headersMap = Headers.getHeadersMapFromMultiLineString(headers);
		
		Map<String,Object> request = new HashMap<String,Object>();
		request.put("method", requestMethod);
		request.put("path", relativeURI);
		request.put("headers", headersMap);
		request.put("body", body);
		//TODO: make constants
		request.put("contentType", headersMap.get("Content-Type"));
		//request.put("protocolVersion", "1.1");//TODO

		headersMap.remove("Content-Type");
		
		long defaultTimeout = Timeouts.getDefaultTimeouts().getMillisToTimeout();
		request.put("timeout", defaultTimeout);
		
		Map<String,Object> response = implAdapter.execute(defaultURI, request);
		
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
		List<NameValuePair> nvplist = URLEncodedUtils.parse(uri, "UTF-8" /*StandardCharsets.UTF_8*/);
		
		for (NameValuePair nvp : nvplist) {
			query.put(nvp.getName(), nvp.getValue());
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("query", query);
		ret.put("path", uri.getPath());
		return ret;
	}
	
}
