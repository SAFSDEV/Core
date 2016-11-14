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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.bootstrap.io.HttpServer;
import org.apache.hc.core5.http.bootstrap.io.ServerBootstrap;
import org.apache.hc.core5.http.config.SocketConfig;

/**
 * <p>This testing framework starts an in-process {@link HttpServer} which will use an
 * {@link HttpServerTestingRequestHandler} to check HTTP requests that are sent
 * to it.  Before the request is sent, the handler is told what request to expect.
 * If the received request does not match the request expectations, an exception
 * is thrown.</p>
 *
 * <p>The handler is also told what response to return.  This testing framework will
 * then check the response it receives with what it desired.  If they do not
 * match, an exception is thrown.</p>
 *
 * <p>This has been designed to work with any HTTP client.  So, for instance, Groovy's
 * HttpBuilder or RESTClient which uses Apache HttpClient can also be tested with this
 * testing framework.  A different {@link HttpServerTestingAdapter} is used with
 * different HTTP clients.  If testing Apache HttpClient5, the {@link HttpClient5TestingAdapter}
 * is used.  Since use of this testing framework with other projects is desired,
 * the testframework package has been placed outside the test directory.  Care has
 * been taken to make sure no testing dependency such as JUnit or EasyMock is used
 * in the framework.</p>
 *
 * <p>The {@link HttpClient5TestingAdapter} that is used is either passed into the
 * constructor or set with setAdapter().</p>
 *
 * <p>By default, this framework will go through a series of tests that will exercise
 * all HTTP methods.  If the default tests are not desired, then the deleteTests()
 * method can be called.  Then, custom tests can be added with the addTest() methods.
 * Of course additional tests can be added with the addTest() method without first
 * calling deleteTests().  In that case, the default tests and the additional tests
 * will all run.</p>
 *
 * <p>Since this framework has been designed to be used with any HTTP client, the test
 * is specified with POJO's such as Map, List, and primitives.  The test is a Map with
 * two keys - request and response.  See {@link HttpClientPOJOAdapter} for details
 * on the format of the request and response.</p>
 *
 * <p>Once any additional tests have been added, the runTests() method is called to
 * actually do the testing.</p>
 *
 * @since 5.0
 *
 */
public class HttpServerTestingFramework {
    /**
     * Use the ALL_METHODS list to conveniently cycle through all HTTP methods.
     */
    public static final List<String> ALL_METHODS = Arrays.asList("HEAD", "GET", "DELETE", "POST", "PUT", "PATCH");

    /**
     * If an {@link HttpClient5TestingAdapter} is unable to return a response in
     * the format this testing framework is needing, then it will need to check the
     * item in the response (such as body, status, headers, or contentType) itself and set
     * the returned value of the item as ALREADY_CHECKED.
     */
    public static final Object ALREADY_CHECKED = new Object();

    /**
     * If a test does not specify a path, this one is used.
     */
    public static final String DEFAULT_REQUEST_PATH = "a/path";

    /**
     * If a test does not specify a body, this one is used.
     */
    public static final String DEFAULT_REQUEST_BODY = "{\"location\":\"home\"}";

    /**
     * If a test does not specify a request contentType, this one is used.
     */
    public static final String DEFAULT_REQUEST_CONTENT_TYPE = "application/json";

    /**
     * If a test does not specify query parameters, these are used.
     */
    public static final Map<String, String> DEFAULT_REQUEST_QUERY;

    /**
     * If a test does not specify a request headers, these are used.
     */
    public static final Map<String, String> DEFAULT_REQUEST_HEADERS;

    /**
     * If a test does not specify a protocol version, this one is used.
     */
    public static final ProtocolVersion DEFAULT_REQUEST_PROTOCOL_VERSION = HttpVersion.HTTP_1_1;

    /**
     * If a test does not specify an expected response status, this one is used.
     */
    public static final int DEFAULT_RESPONSE_STATUS = 200;

    /**
     * If a test does not specify an expected response body, this one is used.
     */
    public static final String DEFAULT_RESPONSE_BODY = "{\"location\":\"work\"}";

    /**
     * If a test does not specify an expected response contentType, this one is used.
     */
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json";

    /**
     * If a test does not specify expected response headers, these are used.
     */
    public static final Map<String, String> DEFAULT_RESPONSE_HEADERS;

    static {
        final Map<String, String> request = new HashMap<String, String>();
        request.put("p1", "this");
        request.put("p2", "that");
        DEFAULT_REQUEST_QUERY = Collections.unmodifiableMap(request);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "stuff");
        headers.put("header2", "more stuff");
        DEFAULT_REQUEST_HEADERS = Collections.unmodifiableMap(headers);

        headers = new HashMap<String, String>();
        headers.put("header3", "header_three");
        headers.put("header4", "header_four");
        DEFAULT_RESPONSE_HEADERS = Collections.unmodifiableMap(headers);
    }

    private HttpServerTestingAdapter adapter;
    private HttpServerTestingRequestHandler requestHandler = new HttpServerTestingRequestHandler();
    private List<HttpServerTest> tests = new ArrayList<HttpServerTest>();

    private HttpServer server;
    private int port;

    public HttpServerTestingFramework() throws HttpServerTestingFrameworkException {
        this(null);
    }

    public HttpServerTestingFramework(final HttpServerTestingAdapter adapter) throws HttpServerTestingFrameworkException {
        this.adapter = adapter;

        /*
         * By default, a set of tests that will exercise each HTTP method are pre-loaded.
         */
        for (String method : ALL_METHODS) {
            final List<Integer> statusList = Arrays.asList(200, 201);
            for (Integer status : statusList) {
                final Map<String, Object> request = new HashMap<String, Object>();
                request.put("method", method);

                final Map<String, Object> response = new HashMap<String, Object>();
                response.put("status", status);

                final Map<String, Object> test = new HashMap<String, Object>();
                test.put("request", request);
                test.put("response", response);

                addTest(test);
            }
        }
    }

    /**
     * This is not likely to be used except during the testing of this class.
     * It is used to inject a mocked request handler.
     *
     * @param requestHandler
     */
    public void setRequestHandler(final HttpServerTestingRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    /**
     * Run the tests that have been previously added.  First, an in-process {@link HttpServer} is
     * started.  Then, all the tests are completed by passing each test to the adapter
     * which will make the HTTP request.
     *
     * @throws HttpServerTestingFrameworkException if there is a test failure or unexpected problem.
     */
    public void runTests() throws HttpServerTestingFrameworkException {
        if (adapter == null) {
            throw new HttpServerTestingFrameworkException("adapter should not be null");
        }

        startServer();

        try {
            for (HttpServerTest test : tests) {
                try {
                    callAdapter(test);
                } catch (Throwable t) {
                    processThrowable(t, test);
                }
            }
        } finally {
            stopServer();
        }
    }

    private void processThrowable(final Throwable t, final HttpServerTest test) throws HttpServerTestingFrameworkException {
        HttpServerTestingFrameworkException e;
        if (t instanceof HttpServerTestingFrameworkException) {
            e = (HttpServerTestingFrameworkException) t;
        } else {
            e = new HttpServerTestingFrameworkException(t);
        }
        e.setAdapter(adapter);
        e.setTest(test);
        throw e;
    }

    private void startServer() throws HttpServerTestingFrameworkException {
        /*
         * Start an in-process server and handle all HTTP requests
         * with the requestHandler.
         */
        final SocketConfig socketConfig = SocketConfig.custom()
                                          .setSoTimeout(15000)
                                          .build();

        final ServerBootstrap serverBootstrap = ServerBootstrap.bootstrap()
                                          .setSocketConfig(socketConfig)
                                          .registerHandler("/*", requestHandler);

        server = serverBootstrap.create();
        try {
            server.start();
        } catch (IOException e) {
            throw new HttpServerTestingFrameworkException(e);
        }

        port = server.getLocalPort();
    }

    private void stopServer() {
        if (server != null) {
            server.shutdown(0, TimeUnit.SECONDS);
            server = null;
        }
    }

    private void callAdapter(final HttpServerTest test) throws HttpServerTestingFrameworkException {
        Map<String, Object> request = test.initRequest();

        /*
         * If the adapter does not support the particular request, skip the test.
         */
        if (! adapter.isRequestSupported(request)) {
            return;
        }

        /*
         * Allow the adapter to modify the request before the request expectations
         * are given to the requestHandler.  Typically, adapters should not have
         * to modify the request.
         */
        request = adapter.modifyRequest(request);

        // Tell the request handler what to expect in the request.
        requestHandler.setRequestExpectations(request);

        Map<String, Object> responseExpectations = test.initResponseExpectations();
        /*
         * Allow the adapter to modify the response expectations before the handler
         * is told what to return.  Typically, adapters should not have to modify
         * the response expectations.
         */
        responseExpectations = adapter.modifyResponseExpectations(request, responseExpectations);

        // Tell the request handler what response to return.
        requestHandler.setDesiredResponse(responseExpectations);

        /*
         * Use the adapter to make the HTTP call.  Make sure the responseExpectations are not changed
         * since they have already been sent to the request handler and they will later be used
         * to check the response.
         */
        final String defaultURI = getDefaultURI();
        final Map<String, Object> response = adapter.execute(
                                                defaultURI,
                                                request,
                                                requestHandler,
                                                Collections.unmodifiableMap(responseExpectations));
        /*
         * The adapter is welcome to call assertNothingThrown() earlier, but we will
         * do it here to make sure it is done.  If the handler threw any exception
         * while checking the request it received, it will be re-thrown here.
         */
        requestHandler.assertNothingThrown();

        assertResponseMatchesExpectation(request.get("method"), response, responseExpectations);
    }

    @SuppressWarnings("unchecked")
    private void assertResponseMatchesExpectation(final Object method, final Map<String, Object> actualResponse,
                                                  final Map<String, Object> expectedResponse)
                                                  throws HttpServerTestingFrameworkException {
        if (actualResponse == null) {
            throw new HttpServerTestingFrameworkException("response should not be null");
        }
        /*
         * Now check the items in the response unless the adapter says they
         * already checked something.
         */
        if (actualResponse.get("status") != HttpServerTestingFramework.ALREADY_CHECKED) {
            assertStatusMatchesExpectation(actualResponse.get("status"), expectedResponse.get("status"));
        }
        if (! method.equals("HEAD")) {
            if (actualResponse.get("body") != HttpServerTestingFramework.ALREADY_CHECKED) {
                assertBodyMatchesExpectation(actualResponse.get("body"), expectedResponse.get("body"));
            }
            if (actualResponse.get("contentType") != HttpServerTestingFramework.ALREADY_CHECKED) {
                assertContentTypeMatchesExpectation(actualResponse.get("contentType"), expectedResponse.get("contentType"));
            }
        }
        if (actualResponse.get("headers") != HttpServerTestingFramework.ALREADY_CHECKED) {
            assertHeadersMatchExpectation((Map<String, String>) actualResponse.get("headers"),
                                          (Map<String, String>) expectedResponse.get("headers"));
        }
    }

    private void assertStatusMatchesExpectation(final Object actualStatus, final Object expectedStatus)
            throws HttpServerTestingFrameworkException {
        if (actualStatus == null) {
            throw new HttpServerTestingFrameworkException("Returned status is null.");
        }
        if ((expectedStatus != null) && (! actualStatus.equals(expectedStatus))) {
            throw new HttpServerTestingFrameworkException("Expected status not found. expected="
                                                  + expectedStatus + "; actual=" + actualStatus);
        }
    }

    private void assertBodyMatchesExpectation(final Object actualBody, final Object expectedBody)
        throws HttpServerTestingFrameworkException {
        if (actualBody == null) {
            throw new HttpServerTestingFrameworkException("Returned body is null.");
        }
        if ((expectedBody != null) && (! actualBody.equals(expectedBody))) {
            throw new HttpServerTestingFrameworkException("Expected body not found. expected="
                                    + expectedBody + "; actual=" + actualBody);
        }
    }

    private void assertContentTypeMatchesExpectation(final Object actualContentType, final Object expectedContentType)
        throws HttpServerTestingFrameworkException {
        if (expectedContentType != null) {
            if (actualContentType == null) {
                throw new HttpServerTestingFrameworkException("Returned contentType is null.");
            }
            if (! actualContentType.equals(expectedContentType)) {
                throw new HttpServerTestingFrameworkException("Expected content type not found.  expected="
                                    + expectedContentType + "; actual=" + actualContentType);
            }
        }
    }

    private void assertHeadersMatchExpectation(final Map<String, String> actualHeaders,
                                               final Map<String, String>  expectedHeaders)
            throws HttpServerTestingFrameworkException {
        if (expectedHeaders == null) {
            return;
        }
        for (Map.Entry<String, String> expectedHeader : ((Map<String, String>) expectedHeaders).entrySet()) {
            final String expectedHeaderName = expectedHeader.getKey();
            if (! actualHeaders.containsKey(expectedHeaderName)) {
                throw new HttpServerTestingFrameworkException("Expected header not found: name=" + expectedHeaderName);
            }
            if (! actualHeaders.get(expectedHeaderName).equals(expectedHeaders.get(expectedHeaderName))) {
                throw new HttpServerTestingFrameworkException("Header value not expected: name=" + expectedHeaderName
                        + "; expected=" + expectedHeaders.get(expectedHeaderName)
                        + "; actual=" + actualHeaders.get(expectedHeaderName));
            }
        }
    }

    private String getDefaultURI() {
        return "http://localhost:" + port  + "/";
    }

    /**
     * Sets the {@link HttpServerTestingAdapter}.
     *
     * @param adapter
     */
    public void setAdapter(final HttpServerTestingAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Deletes all tests.
     */
    public void deleteTests() {
        tests = new ArrayList<HttpServerTest>();
    }

    /**
     * Call to add a test with defaults.
     *
     * @throws HttpServerTestingFrameworkException
     */
    public void addTest() throws HttpServerTestingFrameworkException {
        addTest(null);
    }

    /**
     * Call to add a test.  The test is a map with a "request" and a "response" key.
     * See {@link HttpClientPOJOAdapter} for details on the format of the request and response.
     *
     * @param test Map with a "request" and a "response" key.
     * @throws HttpServerTestingFrameworkException
     */
    @SuppressWarnings("unchecked")
    public void addTest(final Map<String, Object> test) throws HttpServerTestingFrameworkException {
        final Map<String, Object> testCopy = (Map<String, Object>) deepcopy(test);

        tests.add(new HttpServerTest(testCopy));
    }

    /**
     * Used to make a "deep" copy of an object.  This testing framework makes deep copies
     * of tests that are added as well as requestExpectations Maps and response Maps.
     *
     * @param orig a serializable object.
     * @return a deep copy of the orig object.
     * @throws HttpServerTestingFrameworkException
     */
    public static Object deepcopy(final Object orig) throws HttpServerTestingFrameworkException {
        try {
            // this is from http://stackoverflow.com/questions/13155127/deep-copy-map-in-groovy
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.flush();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bin);
            return ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new HttpServerTestingFrameworkException(e);
        }
    }
}
