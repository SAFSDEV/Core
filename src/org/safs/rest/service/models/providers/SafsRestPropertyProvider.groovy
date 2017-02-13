// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.providers

import static org.safs.rest.service.commands.curl.CurlCommand.DEFAULT_MAX_TIME
import static org.safs.rest.service.models.entrypoints.Entrypoint.DEFAULT_HOST
import static org.safs.rest.service.models.entrypoints.Entrypoint.DEFAULT_PORT
import static org.safs.rest.service.models.providers.authentication.TokenProvider.FAKE_AUTH_TOKEN
import static org.safs.rest.service.models.providers.authentication.TokenProvider.TRUSTED_USER_NAME
import static org.safs.rest.service.models.providers.authentication.TokenProvider.TRUSTED_USER_PASSWORD

import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.safs.rest.service.commands.curl.CurlCommand
import org.safs.rest.service.commands.curl.CurlInvoker
import org.safs.rest.service.models.consumers.RestConsumer
import org.safs.rest.service.models.providers.authentication.TokenProvider



/**
 *
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
 */
@Slf4j
@ToString(includeNames = true, includeFields = true)
class SafsRestPropertyProvider {
    // NOTE: Use the public modifier on all these static final fields to force
    // them to be treated as constants rather than properties with generated
    // getFOO_BAR() methods.

    public static final String USE_USERNAME_AND_PASSWORD_MESSAGE =
        "WARNING: Use both ${SAFSREST_USERNAME_KEY} and ${SAFSREST_PASSWORD_KEY} instead of ${SAFSREST_AUTH_TOKEN_KEY}."
    public static final String MISSING_AUTH_TOKEN_MESSAGE = """\
        ERROR: Neither the combination of ${SAFSREST_USERNAME_KEY} and ${SAFSREST_PASSWORD_KEY}
        \tnor ${SAFSREST_AUTH_TOKEN_KEY} have been specified.\n\n""".stripIndent()

    public static final String SAFSREST_PROPERTY_KEY_PREFIX = 'safsrest'

    // Keys for standard safsrest* properties
    public static final String SAFSREST_SERVICE_HOST_PROPERTY_KEY = 'safsrestServiceHost'
    public static final String SAFSREST_SERVICE_PORT_PROPERTY_KEY = 'safsrestServicePort'
    public static final String SAFSREST_SHOW_STANDARD_STREAMS_KEY = 'safsrestShowStandardStreams'
    public static final String SAFSREST_USE_LIVE_CURL_KEY = 'safsrestUseLiveCurl'
    public static final String SAFSREST_AUTH_TOKEN_KEY = 'safsrestAuthToken'
    public static final String SAFSREST_USERNAME_KEY = 'safsrestUserName'
    public static final String SAFSREST_PASSWORD_KEY = 'safsrestPassword'
    public static final String SAFSREST_TRUSTED_USER_KEY  = 'safsrestTrustedUser'
    public static final String SAFSREST_TRUSTED_PASSWORD_KEY = 'safsrestTrustedPassword'
    public static final String SAFSREST_TOKEN_PROVIDER_SERVICE_NAME_KEY = 'safsrestTokenProviderServiceName'
    public static final String SAFSREST_TOKEN_PROVIDER_AUTH_TOKEN_RESOURCE_KEY = 'safsrestTokenProviderAuthTokenResource'
    public static final String SAFSREST_MAX_TIME_KEY = 'safsrestMaxTime'
    public static final String SAFSREST_CAS_SERVER_HOST_KEY = 'safsrestCasServerHost'
    public static final String SAFSREST_CAS_SERVER_PORT_KEY = 'safsrestCasServerPort'
    public static final String SAFSREST_CAS_SERVER_ID_KEY = 'safsrestCasServerId'

    public static final STANDARD_PROPERTY_KEYS = [
            SAFSREST_SERVICE_HOST_PROPERTY_KEY,
            SAFSREST_SERVICE_PORT_PROPERTY_KEY,
            SAFSREST_USERNAME_KEY,
            SAFSREST_SHOW_STANDARD_STREAMS_KEY,
            SAFSREST_USE_LIVE_CURL_KEY,
            SAFSREST_AUTH_TOKEN_KEY,
            SAFSREST_MAX_TIME_KEY,
            SAFSREST_CAS_SERVER_HOST_KEY,
            SAFSREST_CAS_SERVER_PORT_KEY,
            SAFSREST_CAS_SERVER_ID_KEY,
    ]

    public static final int MAX_PORT = 65535
    public static final VALID_PORT_RANGE = 0..MAX_PORT

    public static final CharSequence DEFAULT_USER_NAME = ''

    public static final CharSequence DEFAULT_CAS_SERVER_HOST = ''
    public static final int DEFAULT_CAS_SERVER_PORT = -1
    public static final CharSequence DEFAULT_CAS_SERVER_ID = ''


    // The fields below represent the standard properties
    // exposed by SafsRestPropertyProvider. These properties begin with
    // host and end with customProperties.

    String host = DEFAULT_HOST
    int port = DEFAULT_PORT as int
    String userName = DEFAULT_USER_NAME
    String trustedUser = DEFAULT_USER_NAME
    String tokenProviderServiceName
    String tokenProviderAuthTokenResource
// TODO brfaul Sep 24, 2015: Consider changing showStandardStreams to verbose?
    boolean showStandardStreams
    boolean useLiveCurl = true
    String authToken
    String casServerHost = DEFAULT_CAS_SERVER_HOST
    int casServerPort = DEFAULT_CAS_SERVER_PORT
    String casServerId = DEFAULT_CAS_SERVER_ID


    /**
     * maxTime is the maximum number of seconds to wait for a response
     * to a request.
     */
    int maxTime = DEFAULT_MAX_TIME

    /**
     * customProperties contains a map of all system properties beginning with
     * the prefix 'safsrest' for the active specification being executed, except
     * for those properties listed in STANDARD_PROPERTY_KEYS.
     * @see #STANDARD_PROPERTY_KEYS
     */
    Map customProperties = [:]



    SafsRestPropertyProvider() {
        loadAllSafsRestProperties()
        loadStandardProperties()
        cleanCustomProperties()
    }


    /**
     * Load all the system properties beginning with the prefix 'safsrest' into
     * the custom properties list.
     */
    void loadAllSafsRestProperties() {
        customProperties = System.properties.findAll { key, value ->
            key.startsWith SAFSREST_PROPERTY_KEY_PREFIX
        }
    }


    /**
     * Set the values for the standard, well-known properties that should be
     * available as part of SAFSREST.
     */
    private void loadStandardProperties() {
        setHost customProperties."${SAFSREST_SERVICE_HOST_PROPERTY_KEY}"
        setPort customProperties."${SAFSREST_SERVICE_PORT_PROPERTY_KEY}"

        setUserName customProperties."${SAFSREST_USERNAME_KEY}"
        setTrustedUser customProperties."${SAFSREST_TRUSTED_USER_KEY}"
        setTokenProviderServiceName customProperties."${SAFSREST_TOKEN_PROVIDER_SERVICE_NAME_KEY}"
        setTokenProviderAuthTokenResource customProperties."${SAFSREST_TOKEN_PROVIDER_AUTH_TOKEN_RESOURCE_KEY}"

        setMaxTime customProperties."${SAFSREST_MAX_TIME_KEY}"

        loadCasServerProperties()

        showStandardStreams =
            customProperties."${SAFSREST_SHOW_STANDARD_STREAMS_KEY}"?.toBoolean()

        boolean useLiveCurlKey = customProperties."${SAFSREST_USE_LIVE_CURL_KEY}"
        if (useLiveCurlKey) {
            useLiveCurl = customProperties."${SAFSREST_USE_LIVE_CURL_KEY}".toBoolean()
        }

        loadAuthToken()
    }


    private void loadCasServerProperties() {
        casServerHost = customProperties."${SAFSREST_CAS_SERVER_HOST_KEY}"

        setCasServerPort customProperties."${SAFSREST_CAS_SERVER_PORT_KEY}"

        casServerId = customProperties."${SAFSREST_CAS_SERVER_ID_KEY}"
    }


    /**
     * Attempts to retrieve a valid authentication token to send as a HTTP
     * header via a CurlCommand using the following rules:
     *
     * <ol>
     *     <li>
     *         If the safsrestUserName and safsrestPassword properties have been set,
     *         then use those values to get an authentication token via a Web app URL.
     *     </li>
     *     <li>
     *         If only one (or neither) of those properties has been set, then
     *         use the safsrestAuthToken property if it has been set.
     *     </li>
     * </ol>
     */
    private void loadAuthToken() {
        def password = customProperties."${SAFSREST_PASSWORD_KEY}"

        if (userName && password) {
            authToken = _acquireAuthToken()
        } else {
            authToken = customProperties."${SAFSREST_AUTH_TOKEN_KEY}"

            if (authToken) {
                log.warn USE_USERNAME_AND_PASSWORD_MESSAGE
            } else {
                log.debug MISSING_AUTH_TOKEN_MESSAGE
            }
        }
    }

    /**
     * Retrieves an authentication token for use in making requests.
     *
     * <ul>
     *     <li>
     *         In the SAFSREST unit testing environment, this method returns a fake authentication token.
     *     </li>
     *     <li>
     *         In an consumer-driven testing environment, this method attempts to retrieve an actual valid
     *         authentication token via the {@link TokenProvider} and returns that value.
     *     </li>
     * </ul>
     * @return an actual valid authentication token if one can be acquired; a fake authentication token otherwise.
     */
    private def _acquireAuthToken() {
        def password = customProperties."${SAFSREST_PASSWORD_KEY}"
        def trustedPassword = customProperties."${SAFSREST_TRUSTED_PASSWORD_KEY}"
        if (trustedPassword == null) {
            trustedPassword = TRUSTED_USER_PASSWORD
        }
        acquireAuthToken(password, trustedPassword)
    }
    
    public def acquireAuthToken(password, trustedPassword) {

        def acquiredToken = FAKE_AUTH_TOKEN

        CurlInvoker curlInvoker = new CurlInvoker()
        RestConsumer consumer = new RestConsumer(curlInvoker: curlInvoker, safsrestProperties: this)

        if (useLiveCurl) {
            TokenProvider tokenProvider = new TokenProvider(consumer: consumer)
            acquiredToken = tokenProvider.makeAuthenticationToken userName, password, trustedUser, trustedPassword
        }

        authToken = acquiredToken
        authToken
    }


    /**
     * Cleans the custom property list by removing the standard properties
     * and the safsrestPassword property from the list.
     */
    private void cleanCustomProperties() {
        STANDARD_PROPERTY_KEYS.each { key ->
            removeFromPropertiesWhenPresent key
        }

        cleanPassword()
    }


    private void cleanPassword() {
        removeFromPropertiesWhenPresent SAFSREST_PASSWORD_KEY
        removeFromPropertiesWhenPresent SAFSREST_TRUSTED_PASSWORD_KEY
    }


    private void removeFromPropertiesWhenPresent(key) {
        if (customProperties.containsKey(key)) {
            customProperties.remove key
        }
    }


    /**
     * Set the host property (safsrestServiceHost) to the value supplied, as
     * long as the parameter is true under Groovy truth rules.
     *
     * @param hostName is the String name of the host where the service
     * under test runs
     */
    void setHost(String hostName) {
        if (hostName) {
            host = hostName
        }
    }


    /**
     * Convenience method for setting the port property (i.e. the
     * safsrestServicePort system property). The port property must be a
     * valid port.
     * @see #isValidPort
     *
     * @param portValue is the integer port number where the service under test
     * runs.
     */
    void setPort(portValue) {
        if (isValidPort(portValue)) {
            port = portValue as int
        }
    }


    /**
     * Set the userNameValue property (from safsrestUserName) to the value supplied,
     * as long as the parameter is non-null.
     *
     * @param userNameValue is the String name of the user for which the tests run
     */
    void setUserName(String userNameValue) {
        if (userNameValue != null) {
            userName = userNameValue
        }
    }

    /**
     * Set the trustedUserValue property (from safsrestTrustedUser) to the value supplied,
     * as long as the parameter is non-null.
     *
     * @param trustedUserValue is the String name of the trusted user used to get authentication tokens.
     */
    void setTrustedUser(String trustedUserValue) {
        if (trustedUserValue != null) {
            trustedUser = trustedUserValue
        } else {
            trustedUser = TRUSTED_USER_NAME
        }
    }

    
    /**
     * Convenience method for setting the max time property (i.e. the
     * safsrestMaxTime system property). The max time property must be a
     * valid number.
     * @see #isInteger
     *
     * @param maxTimeValue is the integer number in seconds that sets
     * the maximum time for a request to wait.  If the maxTimeValue is negative
     * then we will set it to the default {@link CurlCommand#DEFAULT_MAX_TIME}
     */
    void setMaxTime(maxTimeValue) {
        if (isInteger(maxTimeValue)) {
            maxTime = maxTimeValue as int
            if (maxTime < 0) {
                maxTime = DEFAULT_MAX_TIME
            }
        }
     }


    /**
     * Convenience method for setting the CAS server port property (i.e. the
     * safsrestCasServerPort system property). The port property must be a
     * valid port.
     * @see #isValidPort
     *
     * @param portValue is the integer port number where the service under test
     * runs.
     */
    void setCasServerPort(portValue) {
        if (isValidPort(portValue)) {
            casServerPort = portValue as int
        }
    }


    /**
     * Checks to make sure the specified value is a valid TCP/IP port. The
     * portToValidate can be a number or a string that must be an integer
     * between 0 and 65535.
     *
     * @param portToValidate the value to check
     * @return true if the port is valid; false otherwise.
     */
    boolean isValidPort(portToValidate) {
        boolean validPort = false
        if (isInteger(portToValidate)) {
            int portNumber = intValue portToValidate
            if (portNumber in VALID_PORT_RANGE) {
                validPort = true
            }
        }
        validPort

    }


    /**
    * Checks to make sure the specified value is a valid integer.
    *
    * @param integerToValidate the value to check
    * @return true if the number is valid; false otherwise.
    */
    boolean isInteger(integerToValidate) {
        boolean validInteger = false

        if (integerToValidate != null) {

            try {
                intValue integerToValidate
                validInteger = true
            } catch (NumberFormatException e) {
                // NumberFormatException can occur when numberToValidate is
                // a String that cannot be turned into a number, so force
                // validNumber to false
                validInteger = false
            }
        }
        validInteger

    }


    /**
    * Returns the int value of the parameter that is passed in.
    *
    * @param integerToValidate the value to check
    * @return an int value of the parameter passed in if the parameter is valid
    * @throws NumberFormatException when integerToValidate is invalid
    */
    int intValue(integerToValidate) {
        (integerToValidate.toString() as CharSequence).toInteger().value
    }
}
