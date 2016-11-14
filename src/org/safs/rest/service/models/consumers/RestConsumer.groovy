// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.consumers

import static org.safs.rest.service.commands.curl.CurlCommand.PROPERTY_PROVIDER_ARGUMENT_NAME
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.HEAD
import static org.springframework.http.HttpMethod.PATCH
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.MediaType.TEXT_HTML_VALUE
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import static org.springframework.http.MediaType.TEXT_XML_VALUE

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import org.safs.rest.service.commands.curl.CurlCommand
import org.safs.rest.service.commands.curl.CurlInvoker
import org.safs.rest.service.commands.curl.Response
import org.safs.rest.service.models.providers.SafsRestPropertyProvider
import org.safs.rest.service.verifiers.HttpStatusVerifier
import org.safs.rest.service.verifiers.ResponseVerifier

import org.springframework.http.HttpMethod



/**
 * The class models the client interface that consumes the REST API contract
 * supplied by a REST service.
 *
 * Use this class to interact with the REST API contract in simulating what a
 * third-party application developer might do when consuming REST services.
 *
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
 *
 */
@Slf4j
class RestConsumer {
    public static final ITEMS_COLLECTION_KEY = 'items'

    public static final TEXT_MEDIA_TYPES = [
            TEXT_HTML_VALUE,
            TEXT_PLAIN_VALUE,
            TEXT_XML_VALUE
    ]


    /**
     * curlInvoker MUST be injected via a constructor named argument
     */
    CurlInvoker curlInvoker

    /**
     * safsrestProperties MUST be injected via a constructor named argument.
     */
    @Delegate
    SafsRestPropertyProvider safsrestProperties


    @Delegate
    static HttpStatusVerifier httpStatusVerifier = new HttpStatusVerifier()

    @Delegate
    static ResponseVerifier responseVerifier = new ResponseVerifier(statusVerifier: httpStatusVerifier)


    JsonSlurper slurper = new JsonSlurper()



    /**
     * Returns the JSON representation of the specified JSON element.
     *
     * @param responseBody a String representation of JSON; will be processed
     * by JsonSlurper
     * @param elementName a String representing the name of an element within
     * a JSON object
     * @return a JSON representation for the specified element; null if the
     * responseBody cannot be parsed as JSON or if an element with the given
     * name cannot be found in the body.
     */
    def loadElementFromJson(String responseBody, String elementName) {
        def jsonResponse = parseBodyText responseBody

        def foundElement = null

        if (jsonResponse != responseBody) {
            foundElement = jsonResponse?."${elementName}"
        }

        foundElement
    }


    /**
     * Returns the JSON representation of the specified JSON element.
     *
     * @param response a Response object with a body
     * @param elementName a String representing the name of an element within
     * a JSON object
     * @return a JSON representation for the specified element
     * @see #loadElementFromJson(org.safs.rest.service.commands.curl.Response, java.lang.String)
     */
    def loadElementFromJson(Response response, String elementName) {
        assert response

        loadElementFromJson response.body, elementName
    }


    /**
     * Convenience method to return the collection associated with the items
     * key contained in a String representation of JSON.
     *
     * @param responseBody a String representation of JSON; will be processed
     * by JsonSlurper
     *
     * @return a collection of items
     *
     * @since 0.5.1
     */
    def loadItemsCollection(String responseBody) {
        loadElementFromJson responseBody, ITEMS_COLLECTION_KEY
    }


    /**
     * Convenience method to return the collection associated with the items
     * key contained in a String representation of JSON.
     *
     * @param response a Response object with a body
     *
     * @return a collection of items
     *
     * @since 0.5.1
     * @see #loadItemsCollection(String responseBody)
     */
    def loadItemsCollection(Response response) {
        assert response

        loadItemsCollection response.body
    }


    /**
     * Convenience method for sending a request using the GET HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response received from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response get(Map parameters) {
        sendRequest parameters
    }


    /**
     * Convenience method for sending a request using the GET HTTP method
     * and receiving a response.
     *
     * @param entrypointUrl CharSequence representing an entrypoint URL
     * that will be passed to {@link #get(Map)} as an entry in the Map.
     *
     * @return a Response received from sending a request
     *
     * @see #get(Map)
     * @see Response
     */
    Response get(CharSequence entrypointUrl) {
        get entrypoint: entrypointUrl
    }


    /**
     * Convenience method for sending a request using the HEAD HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response received from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response head(Map parameters) {
        initHttpMethodParameter parameters, HEAD

        sendRequest parameters
    }


    /**
     * Convenience method for sending a request using the HEAD HTTP method
     * and receiving a response.
     *
     * @param entrypointUrl CharSequence representing an entrypoint URL
     * that will be passed to {@link #head(Map)} as an entry in the Map.
     *
     * @return a Response received from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response head(CharSequence entrypointUrl) {
        head entrypoint: entrypointUrl
    }


    /**
     * Convenience method for sending a request using the POST HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response post(Map parameters) {
        initHttpMethodParameter parameters, POST

        sendRequest parameters
    }


    /**
     * Convenience method for sending a request using the DELETE HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response delete(Map parameters) {
        initHttpMethodParameter parameters, DELETE

        sendRequest parameters
    }


    /**
     * Convenience method for executing a CurlCommand using the DELETE HTTP
     * method.
     *
     * @param entrypointUrl CharSequence representing an entrypoint URL
     * that will be passed to {@link #delete(Map)} as an entry in
     * the Map.
     *
     * @return a Response from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response delete(CharSequence entrypointUrl) {
        delete entrypoint: entrypointUrl
    }


    /**
     * Convenience method for sending a request using the PUT HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response put(Map parameters) {
        initHttpMethodParameter parameters, PUT

        sendRequest parameters
    }


    /**
     * Convenience method for sending a request using the PUT HTTP method
     * and receiving a response.
     *
     * @param entrypointUrl CharSequence representing an entrypoint URL
     * that will be passed to {@link #put(Map)} as an entry in
     * the Map.
     *
     * @return a Response from sending a request
     *
     * @see #put(Map)
     * @see Response
     */
    Response put(CharSequence entrypointUrl) {
        put entrypoint: entrypointUrl
    }


    /**
     * Convenience method for sending a request using the PATCH HTTP method
     * and receiving a response.
     *
     * @param parameters Map of parameters that specify information about
     * the request.
     *
     * @return a Response from sending a request
     *
     * @see CurlCommand
     * @see Response
     */
    Response patch(Map parameters) {
        initHttpMethodParameter parameters, PATCH

        sendRequest parameters
    }


    /**
     * Convenience method for parsing a Response body received as a
     * result of sending a request. An attempt will be made to parse the body
     * into JSON. If the body is JSON, then a data structure of lists and maps
     * representing the JSON will be returned. If the body cannot be parsed
     * as JSON, then the raw text value of the body will be returned.
     *
     * @param Response object (with an optional body) to be parsed
     *
     * @return a parsed response into a data structure of lists and/or maps IF
     * the response body contains valid JSON; otherwise, returns the raw
     * text representation of the response body.
     *
     * @see Response
     */
    def parseBody(Response response) {
        def rawBody = response?.body  // need this in case of an empty response body

        parseBodyText rawBody
    }


    private parseBodyText(String rawBody) {
        def parsedBody = rawBody

        if (rawBody) {
            try {
                parsedBody = slurper.parseText rawBody
            } catch (Exception exception) {
                // slurper.parseText throws an Exception if the rawBody cannot
                // be parsed as JSON. Since parsedBody has been initialized to
                // rawBody above, then just log a debug message.
                String exceptionMessage =
                        'RestConsumer.parseBody(), an exception: {} occurred when trying to parse rawBody: {}'
                log.debug exceptionMessage, exception, rawBody
            }
        }

        parsedBody
    }


    private initHttpMethodParameter(Map parameters, HttpMethod method) {
        if (parameters?.keySet()?.contains(method) == false) {
            parameters.httpMethod = method
        }
    }


    private Response sendRequest(Map parameters) {
        initPropertyProviderParameter parameters

        CurlCommand curlCommand = new CurlCommand(parameters)

        curlInvoker.execute curlCommand
    }


    private initPropertyProviderParameter(Map parameters) {
        if (parameters.keySet().contains(PROPERTY_PROVIDER_ARGUMENT_NAME) == false) {
            parameters["${PROPERTY_PROVIDER_ARGUMENT_NAME}"] = safsrestProperties
        }
    }

}
