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

package org.safs.rest.service.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.utils.URLEncodedUtils;
import org.apache.hc.core5.http.NameValuePair;

/**
 * <p>This class is not expected to be used directly by the user, but its job is to
 * supply helpful defaults for tests.</p>
 *
 * <p>A test is made up of an HTTP request that the HTTP client will send as well
 * as a response that is expected.</p>
 *
 * <p>See {@link HttpClientPOJOAdapter} for details on the request and response.</p>
 *
 * <p>Generally, if the request does not specify a method, it is assumed to be a GET.
 * There are also defaults for headers, query parameters, body, contentType, etc.</p>
 *
 * @since 5.0
 */
public class HttpServerTest {
    private Map<String, Object> request = new HashMap<String, Object>();
    private Map<String, Object> response = new HashMap<String, Object>();

    /**
     * Constructs a test with default values.
     */
    public HttpServerTest() {
        this(null);
    }

    /**
     * Constructs a test with values that are passed in as well as defaults
     * for values that are not passed in.
     *
     * @param test Contains a "request" and an expected "response".
     *             See {@link HttpClientPOJOAdapter} for details.
     */
    @SuppressWarnings("unchecked")
    public HttpServerTest(final Map<String, Object> test) {
        if (test != null) {
            if (test.containsKey("request")) {
                request = (Map<String, Object>) test.get("request");
            }
            if (test.containsKey("response")) {
                response = (Map<String, Object>) test.get("response");
            }
        }
    }

    /**
     * Returns a request with defaults for any parameter that is not specified.
     *
     * @return a "request" map.
     * @throws HttpServerTestingFrameworkException a problem such as an invalid URL
     */
    public Map<String, Object> initRequest() throws HttpServerTestingFrameworkException {
        // initialize to some helpful defaults
        final Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("path", HttpServerTestingFramework.DEFAULT_REQUEST_PATH);
        ret.put("body", HttpServerTestingFramework.DEFAULT_REQUEST_BODY);
        ret.put("contentType", HttpServerTestingFramework.DEFAULT_REQUEST_CONTENT_TYPE);
        ret.put("query", new HashMap<String, String>(HttpServerTestingFramework.DEFAULT_REQUEST_QUERY));
        ret.put("headers", new HashMap<String, String>(HttpServerTestingFramework.DEFAULT_REQUEST_HEADERS));
        ret.put("protocolVersion", HttpServerTestingFramework.DEFAULT_REQUEST_PROTOCOL_VERSION);

        // GET is the default method.
        if (! request.containsKey("method")) {
            request.put("method", "GET");
        }
        ret.putAll(request);

        moveAnyParametersInPathToQuery(ret);

        return ret;
    }

    private void moveAnyParametersInPathToQuery(final Map<String, Object> request) throws HttpServerTestingFrameworkException {
        try {
            final String path = (String) request.get("path");
            if (path != null) {
                final URI uri = path.startsWith("/") ? new URI("http://localhost:8080" + path) :
                                                 new URI("http://localhost:8080/");
                final List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
                @SuppressWarnings("unchecked")
                final Map<String, Object> queryMap = (Map<String, Object>) request.get("query");
                for (NameValuePair param : params) {
                    queryMap.put(param.getName(), param.getValue());
                }
                if (! params.isEmpty()) {
                    request.put("path", uri.getPath());
                }
            }
        } catch (URISyntaxException e) {
            throw new HttpServerTestingFrameworkException(e);
        }
    }

    /**
     * Returns an expected response with defaults for any parameter that is not specified.
     *
     * @return the "response" map.
     */
    public Map<String, Object> initResponseExpectations() {
        // 200 is the default status.
        if (! response.containsKey("status")) {
            response.put("status", 200);
        }

        final Map<String, Object> responseExpectations = new HashMap<String, Object>();
        // initialize to some helpful defaults
        responseExpectations.put("body", HttpServerTestingFramework.DEFAULT_RESPONSE_BODY);
        responseExpectations.put("contentType", HttpServerTestingFramework.DEFAULT_RESPONSE_CONTENT_TYPE);
        responseExpectations.put("headers", new HashMap<String, String>(HttpServerTestingFramework.DEFAULT_RESPONSE_HEADERS));

        // Now override any defaults with what is requested.
        responseExpectations.putAll(response);

        return responseExpectations;
    }

    @Override
    public String toString() {
        return "request: " + request + "\nresponse: " + response;
    }
}
