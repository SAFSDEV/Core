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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hc.client5.http.utils.URLEncodedUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.entity.ContentType;
import org.apache.hc.core5.http.entity.EntityUtils;
import org.apache.hc.core5.http.entity.StringEntity;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * <p>This request handler is used with an in-process instance of HttpServer during testing.
 * The handler is told what to expect in the request.  If the request does not match
 * the expectations, the handler will throw an exception which is then caught and
 * saved in the "thrown" member.  The testing framework will later call assertNothingThrown().
 * If something was thrown earlier by the handler, an exception will be thrown by the method.</p>
 *
 * <p>The handler is also told what response to return.</p>
 *
 * <p>See {@link HttpClientPOJOAdapter} for details on the format of the request and response.</p>
 *
 * @since 5.0
 *
 */
public class HttpServerTestingRequestHandler implements HttpRequestHandler {
    protected Throwable thrown;
    protected Map<String, Object> requestExpectations;
    protected Map<String, Object> desiredResponse;

    /**
     * Sets the request expectations.
     *
     * @param requestExpectations the expected values of the request.
     * @throws HttpServerTestingFrameworkException
     */
    @SuppressWarnings("unchecked")
    public void setRequestExpectations(final Map<String, Object> requestExpectations) throws HttpServerTestingFrameworkException {
        this.requestExpectations = (Map<String, Object>) HttpServerTestingFramework.deepcopy(requestExpectations);
    }

    /**
     * Sets the desired response.  The handler will return a response that matches this.
     *
     * @param desiredResponse the desired response.
     * @throws HttpServerTestingFrameworkException
     */
    @SuppressWarnings("unchecked")
    public void setDesiredResponse(final Map<String, Object> desiredResponse) throws HttpServerTestingFrameworkException {
        this.desiredResponse = (Map<String, Object>) HttpServerTestingFramework.deepcopy(desiredResponse);
    }

    /**
     * After the handler returns the response, any exception or failed assertion will be
     * in the member called "thrown".  A testing framework can later call this method
     * which will rethrow the exception that was thrown before.
     *
     * @throws HttpServerTestingFrameworkException
     */
    public void assertNothingThrown() throws HttpServerTestingFrameworkException {
        if (thrown != null) {
            final HttpServerTestingFrameworkException e = (thrown instanceof HttpServerTestingFrameworkException ?
                                                          (HttpServerTestingFrameworkException) thrown :
                                                          new HttpServerTestingFrameworkException(thrown));
            thrown = null;
            throw e;
        }
    }

    /**
     * <p>Checks the HTTP request against the requestExpectations that it was previously given.
     * If there is a mismatch, an exception will be saved in the "thrown" member.</p>
     *
     * <p>Also, a response will be returned that matches the desiredResponse.</p>
     */
    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {

        try {
            /*
             * Check the method against the method in the requestExpectations.
             */
            final String actualMethod = request.getRequestLine().getMethod();
            final String expectedMethod = (String) requestExpectations.get("method");
            if (! actualMethod.equals(expectedMethod)) {
                throw new HttpServerTestingFrameworkException("Method not expected. " +
                    " expected=" + expectedMethod + "; actual=" + actualMethod);
            }

            /*
             * Set the status to the status that is in the desiredResponse.
             */
            final Object desiredStatus = desiredResponse.get("status");
            if (desiredStatus != null) {
                response.setStatusCode((int) desiredStatus);
            }

            /*
             * Check the query parameters against the parameters in requestExpectations.
             */
            @SuppressWarnings("unchecked")
            final Map<String, String> expectedQuery = (Map<String, String>) requestExpectations.get("query");
            if (expectedQuery != null) {
                final URI uri = new URI(request.getRequestLine().getUri());
                final List<NameValuePair> actualParams = URLEncodedUtils.parse(uri, "UTF-8");
                final Map<String, String> actualParamsMap = new HashMap<String, String>();
                for (NameValuePair actualParam : actualParams) {
                    actualParamsMap.put(actualParam.getName(), actualParam.getValue());
                }
                for (Map.Entry<String, String> expectedParam : expectedQuery.entrySet()) {
                    final String key = expectedParam.getKey();
                    if (! actualParamsMap.containsKey(key)) {
                        throw new HttpServerTestingFrameworkException("Expected parameter not found: " + key);
                    }
                    final String actualParamValue = actualParamsMap.get(key);
                    final String expectedParamValue = expectedParam.getValue();
                    if (! actualParamValue.equals(expectedParamValue)) {
                        throw new HttpServerTestingFrameworkException("Expected parameter value not found. " +
                            " Parameter=" + key + "; expected=" + expectedParamValue + "; actual=" + actualParamValue);
                    }
                }
            }

            /*
             * Check the headers against the headers in requestExpectations.
             */
            @SuppressWarnings("unchecked")
            final Map<String, String> expectedHeaders = (Map<String, String>) requestExpectations.get("headers");
            if (expectedHeaders != null) {
                final Map<String, String> actualHeadersMap = new HashMap<String, String>();
                final Header[] actualHeaders = request.getAllHeaders();
                for (Header header : actualHeaders) {
                    actualHeadersMap.put(header.getName(), header.getValue());
                }
                for (Entry<String, String> expectedHeader : expectedHeaders.entrySet()) {
                    final String key = expectedHeader.getKey();
                    if (! actualHeadersMap.containsKey(key)) {
                        throw new HttpServerTestingFrameworkException("Expected header not found: " + key);
                    }
                    final String actualHeaderValue = actualHeadersMap.get(key);
                    final String expectedHeaderValue = expectedHeader.getValue();
                    if (! actualHeaderValue.equals(expectedHeaderValue)) {
                        throw new HttpServerTestingFrameworkException("Expected header value not found. " +
                                " Name=" + key + "; expected=" + expectedHeaderValue + "; actual=" + actualHeaderValue);
                    }
                }
            }

            /*
             * Check the body.
             */
            final String expectedBody = (String) requestExpectations.get("body");
            if (expectedBody != null) {
                final HttpEntity entity = request.getEntity();
                final String data = EntityUtils.toString(entity);
                if (! data.equals(expectedBody)) {
                    throw new HttpServerTestingFrameworkException("Expected body not found. " +
                            " Body=" + data + "; expected=" + expectedBody);
                }
            }

            /*
             * Check the contentType of the request.
             */
            final String requestContentType = (String) requestExpectations.get("contentType");
            if (requestContentType != null) {
                final HttpEntity entity = request.getEntity();
                final String contentType = entity.getContentType();
                final String expectedContentType = (String) requestExpectations.get("contentType");
                if (! contentType.equals(expectedContentType)) {
                    throw new HttpServerTestingFrameworkException("Expected request content type not found. " +
                            " Content Type=" + contentType + "; expected=" + expectedContentType);
                }
            }

            /*
             * Check the protocolVersion.
             */
            if (requestExpectations.containsKey("protocolVersion")) {
                final ProtocolVersion protocolVersion = request.getRequestLine().getProtocolVersion();
                final ProtocolVersion expectedProtocolVersion = (ProtocolVersion) requestExpectations.get("protocolVersion");
                if (! protocolVersion.equals(expectedProtocolVersion)) {
                    throw new HttpServerTestingFrameworkException("Expected request protocol version not found. " +
                            " Protocol Version=" + protocolVersion + "; expected=" + expectedProtocolVersion);
                }
            }

            /*
             * Return the body in desiredResponse using the contentType in desiredResponse.
             */
            final String desiredBody = (String) desiredResponse.get("body");
            if (desiredBody != null) {
                final String desiredContentType = (String) desiredResponse.get("contentType");
                final StringEntity entity = desiredContentType != null ?
                                new StringEntity(desiredBody, ContentType.parse(desiredContentType)) :
                                new StringEntity(desiredBody);
                response.setEntity(entity);
            }

            /*
             * Return the headers in desiredResponse.
             */
            @SuppressWarnings("unchecked")
            final Map<String, String> desiredHeaders = (Map<String, String>) desiredResponse.get("headers");
            if (desiredHeaders != null) {
                for (Entry<String, String> entry : desiredHeaders.entrySet()) {
                    response.setHeader(entry.getKey(), entry.getValue());
                }
            }

        } catch (Throwable t) {
            /*
             * Save the throwable to be later retrieved by a call to assertNothingThrown().
             */
            thrown = t;
        }
    }
}
