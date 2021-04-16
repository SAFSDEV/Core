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
package org.safs.rest.service.models.providers.authentication

import org.safs.rest.service.models.entrypoints.Entrypoint


class TokenProviderEntrypoints extends Entrypoint {
    static final SERVICE_NAME = ''
    static final AUTH_TOKEN_RESOURCE = 'oauth/token'

	private authTokenResource

    /**
     * Creates an entrypoint reference for retrieving an authorization
     * access token.
     *
     * Implements a named-parameter constructor so a default value can be set
     * for the service property even if the parameters do not include the
     * service. By implementing this constructor, the standard Groovy
     * named-parameter constructor can still be used, but no positional
     * constructors will be valid.
     *
     * @param arguments is a map of named arguments including the service
     * (name), host, and port
     */
    TokenProviderEntrypoints(Map tokenProvEntrypointParameters) {
        service = (tokenProvEntrypointParameters.tokenProviderServiceName != null) ?
                   tokenProvEntrypointParameters.tokenProviderServiceName :
                   SERVICE_NAME

        authTokenResource = (tokenProvEntrypointParameters.tokenProviderAuthTokenResource != null) ?
                             tokenProvEntrypointParameters.tokenProviderAuthTokenResource :
                             AUTH_TOKEN_RESOURCE

        def entrypointParameters = [:]
        entrypointParameters.putAll(tokenProvEntrypointParameters)
        entrypointParameters.remove("tokenProviderServiceName")
        entrypointParameters.remove("tokenProviderAuthTokenResource")

        initializeProperties entrypointParameters
    }


    String getAuthTokenResource() {
        "${serviceUrl}/${authTokenResource}"
    }
}
