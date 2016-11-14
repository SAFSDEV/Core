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

import org.apache.hc.core5.annotation.Immutable;

/**
 * <p>Signals a problem or an assertion failure while using the {@link HttpServerTestingFramework}.</p>
 *
 * <p>Optionally, an adapter and a test can be added to the exception.  If this is done,
 * the adapter name and the test information is appended to the exception message to help
 * determine what test is having a problem.</p>
 *
 * @since 5.0
 */
@Immutable
public class HttpServerTestingFrameworkException extends Exception {
    private HttpServerTestingAdapter adapter;

    private HttpServerTest test;

    /**
     *
     */
    private static final long serialVersionUID = -1010516169283589675L;

    /**
     * Creates a WebServerTestingFrameworkException with the specified detail message.
     */
    public HttpServerTestingFrameworkException(final String message) {
        super(message);
    }

    public HttpServerTestingFrameworkException(final Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (adapter != null) {
            final HttpClientPOJOAdapter pojoAdapter = adapter.getHttpClientPOJOAdapter();
            final String httpClient = pojoAdapter == null ? null : pojoAdapter.getHTTPClientName();
            if (httpClient != null) {
                if (message == null) {
                    message = "null";
                }
                message += "\nHTTP Client=" + httpClient;
            }
        }
        if (test != null) {
            if (message == null) {
                message = "null";
            }
            message += "\ntest:\n" + test;
        }
        return message;
    }

    public void setAdapter(final HttpServerTestingAdapter adapter) {
        this.adapter = adapter;
    }

    public void setTest(final HttpServerTest test) {
        this.test = test;
    }
}
