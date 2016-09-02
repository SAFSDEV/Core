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

import java.util.Map;

//TODO:  can this class be moved beside HttpClient? (in org.apache.hc.client5.http.sync)
public abstract class HttpClientPOJOAdapter {
    public abstract Map<String, Object> execute(String defaultURI, Map<String, Object> request) throws Exception;

    public Map<String, Object> modifyRequest(final Map<String, Object> request) {
        return request;
    };

    public String checkRequestSupport(final Map<String, Object> request) {
        // return null if everything is supported.  Otherwise, return a reason.
        // If this method is overridden, then the execute should probably call assertRequestSupported()
        // at the beginning.
        return null;
    }

    public void assertRequestSupported(final Map<String, Object> request) throws Exception {
        final String reason = checkRequestSupport(request);
        if (reason != null) {
            throw new Exception(reason);
        }
    }
}
