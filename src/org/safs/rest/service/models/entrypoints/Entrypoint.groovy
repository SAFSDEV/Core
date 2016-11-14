// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.entrypoints


/**
 * Provides the basic wrapper for a REST entrypoint.
 *
 * The properties of this wrapper have default values. To override the default values, provide
 * named constructor parameters.
 *
 * For instance, use the following constructor invocation to set the service
 * to <code>foobar</code>, the host to <code>myTestHostName.sas.com</test>,
 * and the port to <code>7980</code>.
 *
 * <code>
 *     def entrypointParameters = [
 *         service: 'foobar',
 *         host: 'myTestHostName.sas.com',
 *         port: 7980
 *     ]
 *
 *     def myEntrypoint = new Entrypoint(entrypointParameters)
 * </code>
 *
 * @author ***REMOVED***
 * @author ***REMOVED***
 *
 */
class Entrypoint {
    public static final CharSequence DEFAULT_SERVICE_NAME = 'defaultRestServiceName'
    public static final CharSequence DEFAULT_HOST = 'localhost'
    public static final int DEFAULT_PORT = 80

    public static final CharSequence API_META = 'apiMeta'
    public static final CharSequence API_META_RESOURCE =  makeUriFromResource API_META

    /**
     * Character used to separate the entire query string from the rest of a URL.
     */
    public static final QUERY_STRING_SEPARATOR = '?'

    /**
     * Character used to separate each query parameter from the next when more
     * than one exists in the URL.
     */
    public static final QUERY_PARAMETER_DELIMITER = '&'


    // NOTE: Override the default values for the following properties via
    // named constructor parameters

    /**
     * service is the name of the REST service under test. The service name is
     * usually the root context of the Web application.
     *
     * For instance, the URL for the CAS Management service Web application
     * would be http://localhost/casManagement, so the service property would
     * be <code>casManagement</code>.
     */
    String service = DEFAULT_SERVICE_NAME

    String host = DEFAULT_HOST
    def port = DEFAULT_PORT


    /**
     * Named parameter constructors of subclasses should call this method
     * to initialize common entrypoint properties, such as service, host,
     * and port.
     *
     * @param entrypointProperties is a map containing named properties
     * with values to set for an Entrypoint
     */
    void initializeProperties(Map entrypointProperties) {
        entrypointProperties.each { property ->
            this."${property.key}" = property.value
        }
    }


    /**
     * Returns the protocol and authority components of the entrypoint URL.
     *
     * For example, in the serviceUrl http://myhost.sas.com:7980/casmgmt/servers,
     * the returned value will be http://myhost.sas.com:7980. This value can
     * be useful in HATEOAS tests following link href components where the href
     * is a relative URL (e.g. /casmgmt/servers).
     *
     * @return the protocol and authority components of the entrypoint URL
     */
    CharSequence getRootUrl() {
        "http://${host}:${port}"
    }


    /**
     * Concatenates the host, port, and service to form a URL string
     *
     * @return String representation of the URL to the service
     */
    String getServiceUrl() {
        "${rootUrl}${serviceResource}"
    }


    /**
     * Returns the service property with a leading "/" character.
     *
     * @return the service property with a leading "/" character.
     */
    CharSequence getServiceResource() {
        makeUriFromResource service
    }


    /**
     * Concatenates the value of the API_META_RESOURCE to the end of the
     * serviceUrl.
     *
     * @return String representation of the URL to the API_META_RESOURCE
     * for a given service
     *
     * @see #getServiceUrl
     */
    CharSequence getApiMeta() {
        "${serviceUrl}${API_META_RESOURCE}"
    }


    /**
     * Returns a URL query parameter of the form <code>key=value</code>. If
     * the key does not exist or there is a key and the value is either a null
     * or an empty string, then an empty string will be returned. For instance,
     *
     *     <code>
     *         def queryParameter = makeQueryParameter 'serverId', 'cas'
     *         assert queryParameter == 'serverId=cas'
     *
     *         queryParameter = makeQueryParameter 'someKeyValue', ''
     *         assert queryParameter == ''
     *     </code>
     *
     * @param key a name for the query parameter (e.g. <code>serverId</code>)
     * @param value some value to associate with the query parameter (e.g. <code>cas</code>)
     *
     * @return a URL query parameter of the form <code>key=value</code> or empty string.
     */
    String makeQueryParameter(key, value) {
        String queryParameter = ''

        if (isValidQueryParameterKey(key)) {
            if (isValidQueryParameterValue(value)) {
                queryParameter = "${key}=${value}"
            }
        }

        queryParameter
    }


    private boolean isValidQueryParameterKey(key) {
        isValidQueryParameterElement key
    }


    private boolean isValidQueryParameterElement(element) {
        boolean isNotNull = element != null && element != 'null'

        boolean isNotEmpty = element.toString().trim() != ''

        isNotNull && isNotEmpty
    }


    private boolean isValidQueryParameterValue(value) {
        isValidQueryParameterElement value
    }


    /**
     * Creates a query string for use in a URL. Uses the specified key/value
     * pairs from the <code>queryParameters</code> map. If any key in the Map
     * is an empty string or the literal 'null', then this method throws an
     * {@link IllegalArgumentException}
     *
     * @param queryParameters a Map containing one or more entries that
     * represent key/value pairs to be used as query parameters in a query
     * string for a URL
     *
     * @return a String that represents the query string of a URL with the
     * specified parameters.
     */
    String makeQueryString(Map queryParameters) {
        String queryString = ''

        if (queryParameters) {
            boolean needsSeparator = true

            queryParameters.each { key, value ->
                queryString += makeQueryStringParameter key, value, needsSeparator

                if (queryString && needsSeparator) {
                    needsSeparator = false
                }
            }
        }

        queryString
    }

    /**
     * Makes a query parameter for use in a query string. Returns a string
     * starting with '?' or '&' followed by the values 'key=value'.
     *
     * <p>
     * <strong>NOTE:</strong> This method has been given a scope of
     * <code>protected</code> so subclasses of {@link Entrypoint} can call
     * the public method {@link #makeQueryString} without encountering
     * {@link MissingMethodException} for private methods called within a
     * closure. Subclasses should <strong>NOT</strong> call this method directly;
     * instead, they should call {@link #makeQueryString}. See
     * <a href="https://myhost.sas.com/browse/SAFSREST-428">SAFSREST-428</a> for more
     * information about this issue.
     * </p>
     *
     * @param key the query parameter name
     * @param value the value associated with the query parameter
     * @param needsSeparator true indicates that the returned string starts
     * with '?'; false indicates that the returned string starts with '&'
     * @return a String representing one query parameter of an overall query
     * string used as a URL; the String starts with either '?' or '&' depending
     * on the value of the needsSeparator parameter.
     */
    protected makeQueryStringParameter(key, value, needsSeparator) {
        String queryString = ''

        if (isValidQueryParameterKey(key)) {
            def queryParameter = makeQueryParameter key, value

            if (queryParameter) {
                if (needsSeparator) {
                    queryString += QUERY_STRING_SEPARATOR
                } else {
                    queryString += QUERY_PARAMETER_DELIMITER
                }

                queryString += queryParameter
            }
        } else {
            def invalidKeyMessage =
                    "While attempting to make a query string, an invalid key (${key.inspect()}) has been found."

            throw new IllegalArgumentException(invalidKeyMessage)
        }

        queryString
    }


    /**
     * Make a URI from the given resource. If the resource begins with a <code>/</code> character
     * or is null, then the resource itself gets returned; otherwise, a leading <code>/</code>
     * character gets added to resource and the resulting URI becomes the return value.
     *
     * @param resource a resource identifier that is (usually) not a complete URI
     * @return a URI representation of the resource or null
     */
    static String makeUriFromResource(String resource) {
        String uri = resource

        if (uri?.startsWith('/') == false) {
            uri = "/${resource}"
        }

        uri
    }


    /**
     * Make a URL from the given link using the rootUrl (i.e. protocol://host:port/;
     * example: http://exampleHost.sas.com:80) for the service and the uri
     * attribute from the link
     *
     * @param link an object with an uri property from which to make a URL
     * @return a String representation of a URL that can be used to follow a
     * link.
     */
    String makeUrlFromLink(link) {
        def resource = link?.uri
        def linkUri = ''

        if (resource) {
            linkUri = makeUriFromResource resource
        }

        "${rootUrl}${linkUri}"
    }

}
