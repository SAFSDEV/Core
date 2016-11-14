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
 * Implementation of {@link HttpServerTestingAdapter} for Apache HttpClient5.
 *
 * @since 5.0
 */
public class HttpClient5TestingAdapter extends HttpServerTestingAdapter {

    /*
     * The following is not expected to be changed to true, but it is to highlight
     * where the execute method can call the requestHandler's assertNothingThrown()
     * method if desired.  Since this adapter's execute method does not check
     * the response, there is no need to call it.
     */
    private boolean callAssertNothingThrown;

    public HttpClient5TestingAdapter() {
        adapter = new HttpClient5Adapter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final String defaultURI, final Map<String, Object> request,
            final HttpServerTestingRequestHandler requestHandler,
            final Map<String, Object> responseExpectations) throws HttpServerTestingFrameworkException {

        try {
            // Call the adapter's execute method to actually make the HTTP request.
            final Map<String, Object> response = adapter.execute(defaultURI, request);

            /*
             * Adapters may call assertNothingThrown() if they would like.  This would be to
             * make sure the following code is not executed in the event there was something
             * thrown in the request handler.
             *
             * Otherwise, the framework will call it when this method returns.  So, it is
             * optional.
             */
            if (callAssertNothingThrown) {
                if (requestHandler == null) {
                    throw new HttpServerTestingFrameworkException("requestHandler cannot be null");
                }
                requestHandler.assertNothingThrown();
            }

            return response;
        } catch (HttpServerTestingFrameworkException e) {
            throw e;
        } catch (Exception ex) {
            throw new HttpServerTestingFrameworkException(ex);
        }
    }
}
