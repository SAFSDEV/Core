/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.client5.http.testframework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.impl.sync.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.RequestBuilder;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.safs.IndependantLog;

// TODO: can this be moved beside HttpClient?  If so, throw a better exception than HttpServerTestingFrameworkException.
/**
 * Implementation of {@link HttpClientPOJOAdapter} for Apache HttpClient5.
 *
 * @since 5.0
 */
public class HttpClient5Adapter extends HttpClientPOJOAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final String defaultURI, final Map<String, Object> request) throws Exception {
        // check the request for missing items.
        if (defaultURI == null) {
            throw new Exception("defaultURL cannot be null");
        }
        if (request == null) {
            throw new Exception("request cannot be null");
        }
        if (! request.containsKey("path")) {
            throw new Exception("Request path should be set.");
        }
        if (! request.containsKey("method")) {
            throw new Exception("Request method should be set.");
        }

        // Append the path to the defaultURI.
        String tempDefaultURI = defaultURI;
        if (! defaultURI.endsWith("/")) {
            tempDefaultURI += "/";
        }
        final String uri = tempDefaultURI + request.get("path");

        /*
         * Use reflection to call a static method on RequestBuilder that is the
         * lowercase of the HTTP method.
         */
        final String methodName = request.get("method").toString().toLowerCase();

        final Method method = RequestBuilder.class.getMethod(methodName);
        RequestBuilder builder = ((RequestBuilder) method.invoke(null, new Object[] {}));

        builder = builder.setUri(uri);

        if (request.containsKey("protocolVersion")) {
            builder = builder.setVersion((ProtocolVersion) request.get("protocolVersion"));
        }

        // timeout
        if (request.containsKey("timeout")) {
            final long timeout = (long) request.get("timeout");
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            requestConfigBuilder.setSocketTimeout((int) timeout);
            builder.setConfig(requestConfigBuilder.build());
        }

        // call addParameter for each parameter in the query.
        @SuppressWarnings("unchecked")
        final Map<String, String> queryMap = (Map<String, String>) request.get("query");
        if (queryMap != null) {
            for (Entry<String, String> parm : queryMap.entrySet()) {
                builder = builder.addParameter(parm.getKey(), parm.getValue());
            }
        }

        // call addHeader for each header in headers.
        @SuppressWarnings("unchecked")
        final Map<String, String> headersMap = (Map<String, String>) request.get("headers");
        if (headersMap != null) {
            for (Entry<String, String> header : headersMap.entrySet()) {
                builder = builder.addHeader(header.getKey(), header.getValue());
            }
        }

        // call setEntity if a body is specified.
        final String requestBody = (String) request.get("body");
        if (requestBody != null) {
            final String requestContentType = (String) request.get("contentType");
            final StringEntity entity = requestContentType != null ?
                                          new StringEntity(requestBody, ContentType.parse(requestContentType)) :
                                          new StringEntity(requestBody);
            builder = builder.setEntity(entity);
        }

         // Now execute the request.
        HttpRoutePlanner proxy = getProxyPlanner();
        CloseableHttpClient httpclient = null;
        if(proxy==null){
        	httpclient = HttpClients.createDefault();
        }else{
        	httpclient = HttpClients.custom().setRoutePlanner(proxy).build();
        }
        
        final CloseableHttpResponse response = httpclient.execute(builder.build());

        // Prepare the response.  It will contain status, body, headers, and contentType.
        final HttpEntity entity = response.getEntity();
        final String body = entity == null ? null : EntityUtils.toString(entity);
        final String contentType = entity == null ? null : entity.getContentType();

        final Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("status", response.getCode());

        // convert the headers to a Map
        final Map<String, Object> headerMap = new HashMap<String, Object>();
        for (Header header : response.getAllHeaders()) {
            headerMap.put(header.getName(), header.getValue());
        }
        ret.put("headers", headerMap);
        ret.put("body", body);
        ret.put("contentType", contentType);

        return ret ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientName() {
        return "HttpClient5";
    }

    
    private HttpRoutePlanner getProxyPlanner(){
    	if(proxyServerURL==null){
    		return null;
    	}
    	
    	IndependantLog.debug("getProxyPlanner(): parsing proxy URL "+proxyServerURL);
    	
    	//break proxyServerURL into "serverName", "port", "username", "password" etc.
    	//http://user:password@server:port
    	String HTTP = "http://";
    	String AT = "@";
    	String COLON = ":";
    	int httpIndex = proxyServerURL.indexOf(HTTP);
    	int atIndex = proxyServerURL.indexOf(AT);
    	
    	String serverPort = null;
    	String proxyServer = null;
    	int proxyPort = 80;
    	
    	if(atIndex>-1){
    		serverPort = proxyServerURL.substring(atIndex+AT.length());
    	}else{
    		if(httpIndex>-1){
    			serverPort = proxyServerURL.substring(httpIndex+HTTP.length());
    		}else{
    			serverPort = proxyServerURL;
    		}
    	}
    	
    	if(serverPort!=null){
    		String[] serverPortArray = serverPort.split(COLON);
    		try{
    			proxyServer = serverPortArray[0];
    		}catch(Exception e){
    			IndependantLog.warn("Failed to get proxy server due to "+e.getMessage());
    		}
    		try{
    			proxyPort = Integer.parseInt(serverPortArray[1]);
    		}catch(Exception e){
    			IndependantLog.warn("Failed to get proxy port due to "+e.getMessage());
    		}    		
    	}
    	
    	if(proxyServer==null){
    		return null;
    	}
    	HttpHost proxy = new HttpHost(proxyServer, proxyPort);
    	return new DefaultProxyRoutePlanner(proxy);    		
    }
}
