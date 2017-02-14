// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

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

        def entrypointParameters = [
			protocol: tokenProvEntrypointParameters.protocol,
			host: tokenProvEntrypointParameters.host,
            port: tokenProvEntrypointParameters.port,
        ]
        initializeProperties entrypointParameters
    }


    String getAuthTokenResource() {
        "${serviceUrl}/${authTokenResource}"
    }
}
