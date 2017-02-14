// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.providers.authentication

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import groovy.util.logging.Slf4j

import org.safs.rest.service.commands.curl.Response
import org.safs.rest.service.models.consumers.RestConsumer



/**
 *
 * @author ***REMOVED***
 * @author ***REMOVED***
 */
@Slf4j
class TokenProvider {
    public static final CharSequence FAKE_AUTH_TOKEN =
            'This-FAKE-auth-token-should-ONLY-appear-in-TESTS-that-do-not-need-a-real-auth-token'
    public static final CharSequence AUTH_TOKEN_ERROR = '\n\nERROR: Unable to get an authentication token. ' +
            'The server may not be available or userId and password specified may not have access.\n\n'

    public static final CharSequence TRUSTED_USER_NAME = ''
    public static final CharSequence TRUSTED_USER_PASSWORD = ''



    TokenProviderEntrypoints entrypoints

    /** Must be injected via named argument
     * *
     */
    RestConsumer consumer = null



    /**
     *
     * @param arguments
     *   arguments.consumer
     */
    TokenProvider(Map arguments) {
        consumer = arguments?.consumer

        if (consumer) {
            initializeEntrypoints()
        }
    }


    void initializeEntrypoints() {
        if (!entrypoints && consumer) {
            def entrypointParameters = [
                protocol: consumer.protocol,
                host: consumer.host,
                port: consumer.port,
                tokenProviderServiceName: consumer.tokenProviderServiceName,
                tokenProviderAuthTokenResource: consumer.tokenProviderAuthTokenResource
            ]

            entrypoints = new TokenProviderEntrypoints(entrypointParameters)
        }
    }


    /**
     * Retrieve a valid authentication token for the specified credentials.
     * Neither this method nor the TokenProvider itself store the credentials,
     * so they must be passed each time when retrieving an authentication
     * token.
     *
     * @param userName
     * @param password
     * @return String with a valid authentication token or null
     */
    String makeAuthenticationToken(String userName, String password, String trustedUser, String trustedPassword) {
        String authenticationToken = FAKE_AUTH_TOKEN

        if (consumer) {
            def contentType = APPLICATION_FORM_URLENCODED_VALUE

            // Use separate --data options for curl request query parameters
            // instead of building a query string manually. Curl takes care
            // of building the query string when multiple --data options have
            // been supplied because it ANDs them together and uses the query
            // parameter separator (&). By letting curl do the work, this code
            // avoids problems with the & character being interpreted as a
            // special character in various operating environments.
            def authenticationRequestOptions = [
                    '--data grant_type=password',
                    "--data username=${userName}",
                    "--data password=${password}",
            ]

            if (trustedPassword == null) {
                trustedPassword = TRUSTED_USER_PASSWORD
            }

            def optionList = [
                    "-u${trustedUser}:${trustedPassword}",
                    authenticationRequestOptions
            ].flatten()

            def authTokenRequestParameters = [
                    entrypoint: entrypoints.authTokenResource,
                    options: optionList,
                    contentType: contentType,
            ]

            Response response = consumer.get authTokenRequestParameters

            boolean hasValidToken = false

            if (consumer.isOk(response)) {
                def authJson = consumer.parseBody response
                authenticationToken = authJson?.access_token
                hasValidToken = authenticationToken as boolean
            }

            // Display an error message and throw an exception if a valid authentication token cannot be acquired.
            if (hasValidToken == false) {
                 log.error AUTH_TOKEN_ERROR

                throw new IllegalStateException(AUTH_TOKEN_ERROR)
            }
        }

        authenticationToken
    }
}
