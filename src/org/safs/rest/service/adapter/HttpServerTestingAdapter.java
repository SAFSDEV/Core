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

import java.util.Map;

/**
*
* <p>This adapter assists the testing of an HTTP client.  This adapter in turn uses an
* {@link HttpClientPOJOAdapter} to actually use the HTTP client to make the request.
* See {@link HttpClientPOJOAdapter} to see the format of the request and the returned
* response.  The format of the returned response is also the format of the parameter
* called responseExpectations.</p>
*
* <p>This adapter will generally call the {@link HttpClientPOJOAdapter} methods of the same
* name when these methods are called:</p>
*
* <pre>
*
* isRequestSupported
* modifyRequest
* execute
*
* </pre>
*
* <p>See these method's documentation in {@link HttpClientPOJOAdapter} for details.</p>
*
* <p>The value that this adapter adds is with the modifyResponseExpectations method.  Each
* test will specify the response that is expected.  The HttpClient5 adapter is able
* to use these unmodified expectations, but if a different HTTP client (such as Groovy's
* RESTClient which uses HttpClient) for some reason needs to modify the expectations,
* it would be done in the modifyResponseExpectations method.</p>
*
* @since 5.0
*/
public abstract class HttpServerTestingAdapter {
    /**
     * This adapter will perform the HTTP request and return the response in the
     * expected format.
     */
    protected HttpClientPOJOAdapter adapter;

    /**
     * See the documentation for the same method in {@link HttpClientPOJOAdapter}.  This
     * method will typically call it.  However, this method also has access to the
     * test's response expectations if that is needed for some reason.  Furthermore,
     * this method also has access to the {@link HttpServerTestingRequestHandler} so
     * it can optionally call assertNothingThrown() before checking the response
     * further.  It is optional because the test framework will call it later.
     *
     * @param defaultURI           See execute method of {@link HttpClientPOJOAdapter}.
     * @param request              See execute method of {@link HttpClientPOJOAdapter}.
     * @param requestHandler       The request handler that checks the received HTTP request
     *                             with the request that was intended.  If there is a
     *                             mismatch of expectations, then the requestHandler will
     *                             throw an exception.  If this execute method does not want
     *                             to make further checks of the response in the case
     *                             the responseHandler threw, then the assertNothingThrown()
     *                             method should be called before doing further checks.
     * @param responseExpectations The response expectations of the test.
     * @return See return of the execute method of {@link HttpClientPOJOAdapter}.
     * @throws HttpServerTestingFrameworkException in the case of a problem.
     */
    public abstract Map<String, Object> execute(String defaultURI,
                                                Map<String, Object> request,
                                                HttpServerTestingRequestHandler requestHandler,
                                                Map<String, Object> responseExpectations)
                                                    throws HttpServerTestingFrameworkException;

    /**
     * See the documentation for the same method in {@link HttpClientPOJOAdapter}.
     *
     * @param request
     * @return
     */
    public boolean isRequestSupported(final Map<String, Object> request) {
        return (adapter == null) ? true : adapter.checkRequestSupport(request) == null;
    };

    /**
     * See the documentation for the same method in {@link HttpClientPOJOAdapter}.
     *
     * @param request
     * @return
     */
    public Map<String, Object> modifyRequest(final Map<String, Object> request) {
       return (adapter == null) ? request : adapter.modifyRequest(request);
    };

    /**
     * Generally a test's response expectations should not need to be modified, but
     * if a particular HTTP client (such as Groovy's RESTClient which uses HttpClient)
     * needs to modify the response expectations, it should do so here.  After this
     * method returns, the {@link HttpServerTestingRequestHandler} is sent the
     * expectations so the request handler will return a response that matches the
     * expectations.  When the HTTP response is obtained, the received response
     * is matched against the expectations.
     *
     * @param request for the format, see the documentation for {@link HttpClientPOJOAdapter}.
     * @param responseExpectations for the format, see the documentation for {@link HttpClientPOJOAdapter}.
     * @return the same or modified response expectations.
     */
    public Map<String, Object> modifyResponseExpectations(final Map<String, Object> request,
                                                          final Map<String, Object> responseExpectations) {
        return responseExpectations;
    }

    /**
     * Getter for the {@link HttpClientPOJOAdapter} that is actually used to make the
     * HTTP request.
     *
     * @return the {@link HttpClientPOJOAdapter}.
     */
    public HttpClientPOJOAdapter getHttpClientPOJOAdapter() {
        return adapter;
    }

}
