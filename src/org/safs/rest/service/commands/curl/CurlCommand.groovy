// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.
package org.safs.rest.service.commands.curl

import static org.safs.rest.service.commands.CommandInvoker.PRESERVE_SPECIAL_CHARACTERS_QUOTE
import static org.safs.rest.service.commands.curl.Response.EMPTY_BODY
import static org.safs.rest.service.models.entrypoints.Entrypoint.SECURE_HTTP_PROTOCOL
import static org.safs.rest.service.models.providers.authentication.TokenProvider.PASSWORD_KEY
import static org.safs.rest.service.models.providers.http.SecureOptions.NONE

import static org.springframework.http.HttpHeaders.ACCEPT as ACCEPT_HEADER
import static org.springframework.http.HttpHeaders.AUTHORIZATION as AUTHORIZATION_HEADER
import static org.springframework.http.HttpHeaders.CONTENT_TYPE as CONTENT_TYPE_HEADER
import static org.springframework.http.HttpHeaders.IF_MATCH
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH
import static org.springframework.http.HttpHeaders.IF_RANGE
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.HEAD
import static org.springframework.http.HttpMethod.PATCH
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpMethod.PUT

import java.text.MessageFormat

import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.xml.XmlUtil

import org.safs.rest.service.commands.ExecutableCommand
import org.safs.rest.service.models.providers.SafsRestPropertyProvider
import org.safs.rest.service.models.providers.SystemPropertyProvider
import org.safs.rest.service.models.providers.authentication.TokenProviderEntrypoints

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType



/**
 * CurlCommand models the operating system curl command line tool to provide
 * a simple mechanism for sending requests to, and receiving responses from,
 * a REST API entrypoint URL.
 *
 * <p>
 *     <strong>IMPORTANT NOTES:</strong>
 * </p>
 * <ul>
 *     <li>
 *         Test authors should <strong>NOT</strong> instantiate CurlCommand
 *         directly. Instead, use the convenience methods supplied by
 *         {@link org.safs.rest.service.models.consumers.RestConsumer}.
 *     </li>
 *     <li>
 *         The convenience methods in RestConsumer model specific HTTP methods
 *         (verbs) with appropriate parameters. Currently, RestConsumer
 *         manages all interaction between {@link CurlCommand} and
 *         {@link CurlInvoker#execute(CurlCommand)} on behalf of a test
 *         author, simplifying and improving the readability and
 *         maintainability of tests.
 *     </li>
 * </ul>
 * <p>
 * For more information on the cURL command line tool, see the
 * <a href="https://curl.haxx.se/">online cURL documentation</a>.
 * </p>
 *
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
 *
 * @since 0.0.1
 */
@ToString(includeNames=true)
class CurlCommand extends ExecutableCommand {
    public static final EXPECTED_CONTENT_TYPE_MESSAGE = '''\
        ERROR: The contentType property has NOT been set on a CurlCommand.
        Because the requestBody property has been set, the contentType property must also be set.
        Set the contentType property for this request via the parameters supplied to either a
        RestConsumer HTTP method (e.g. post, put, delete) or to the CurlCommand constructor.'''.stripIndent()

    public static final CURL_EXECUTABLE = 'curl'

    public static final HEADER_OPTION = '-H'
    public static final HEADER_FIELD_SEPARATOR = ':'

    public static final AUTHORIZATION_BEARER_HEADER_PREFIX = "${AUTHORIZATION_HEADER}:bearer"
    // The positional parameter 0 will allow for substitution of the actual authentication token value.
    public static final AUTHORIZATION_BEARER_HEADER = /${AUTHORIZATION_BEARER_HEADER_PREFIX}{0}/
    public static final AUTHORIZATION_BEARER_HEADER_OPTION= /${HEADER_OPTION}${AUTHORIZATION_BEARER_HEADER}/

    /**
     * @see #DATA_BINARY_OPTION
     */
    public static final DATA_OPTION = '--data'

    /**
     * Displayed instead of the user's password in the generated
     * console string.
     *
     * {@see #getConsoleString}
     */
    public static final PASSWORD_MASK = '*' * 12

    /**
     * DATA_BINARY_OPTION tells curl to treat the request body exactly as
     * it is specified. Use this option instead of the DATA_OPTION for
     * most requests with a body.
     *
     * <b>NOTE:</b> Users of {@link CurlCommand} do not need to set this option directly if they supply
     * a value for the {@link #requestBody} property. {@link CurlCommand} generates the proper options
     * when the {@link #requestBody} property has been set.
     */
    public static final CharSequence DATA_BINARY_OPTION = '--data-binary'
    public static final CharSequence READ_STDIN_OPTION = '@-'

    public static final CharSequence REQUEST_OPTION = '--request'

    public static final CharSequence DELETE_OPTION = "${REQUEST_OPTION} ${DELETE}"
    public static final CharSequence GET_OPTION = "${REQUEST_OPTION} ${GET}"
    public static final CharSequence HEAD_OPTION = '--head'
    public static final CharSequence PATCH_OPTION = "${REQUEST_OPTION} ${PATCH}"
    public static final CharSequence POST_OPTION = "${REQUEST_OPTION} ${POST}"
    public static final CharSequence PUT_OPTION = "${REQUEST_OPTION} ${PUT}"

    public static final INCLUDE_RESPONSE_HEADERS_OPTION = '--include'

    public static final FORM_OPTION = '--form'
    public static final FILE_DATA_PREFIX = '@'

    /**
     * Name of the curl command line <code>location</code> option.
     *
     * The curl <code>--location</code> option follows a 30x redirect for a GET request; for a POST request
     * with status 301, 302, or 303, curl turns the POST into a GET before retrying the request (see the curl
     * man page for more information about what to do in this situation.
     */
    public static final LOCATION_OPTION = '--location'

    /**
     * Name of the curl command line <code>insecure</code> option, which instructs curl NOT
     * to perform validation of HTTPS certificates received from a server.
     */
    public static final INSECURE_OPTION = '--insecure'

    /*
     * Argument names for each of the properties that can be passed to the constructor in the
     * arguments Map parameter.
     */
    public static final OPTIONS_ARGUMENT_NAME = 'options'
    public static final ENTRYPOINT_ARGUMENT_NAME = 'entrypoint'
    public static final HTTP_METHOD_ARGUMENT_NAME = 'httpMethod'
    public static final REQUEST_BODY_ARGUMENT_NAME = 'requestBody'
    public static final PROPERTY_PROVIDER_ARGUMENT_NAME = 'propertyProvider'
    public static final ACCEPT_ARGUMENT_NAME = 'accept'
    public static final CONTENT_TYPE_ARGUMENT_NAME = 'contentType'
    public static final HTTP_HEADERS_ARGUMENT_NAME = 'httpHeaders'
    public static final FORM_FIELDS_ARGUMENT_NAME = 'formFields'
    public static final AUTO_REDIRECT_ARGUMENT_NAME = 'autoRedirect'


    /**
     * The ASCII representation of a single quotation mark (apostrophe)
     * @see #UTF8_SINGLE_QUOTE
     */
    public static final String ASCII_SINGLE_QUOTE = "'"

    /**
     * The Unicode representation for an embedded single quotation mark (apostrophe). See
     * <a href="http://marcgrabanski.com/groovy-escape-quote-string/">http://marcgrabanski.com/groovy-escape-quote-string/</a>
     * for a full explanation of why the single quote must be replaced with the Unicode value in JSON.
     * @see #ASCII_SINGLE_QUOTE
     */
    public static final String UTF8_SINGLE_QUOTE = "\\\\u0027"

    /**
     * The XML entity for a single quote (apostrophe).
     */
    public static final String XML_APOSTROPHE_ENTITY = XmlUtil.escapeXml ASCII_SINGLE_QUOTE

    /**
     * The ASCII representation of a double quotation mark
     * @see #ASCII_SINGLE_QUOTE
     */
    public static final String ASCII_DOUBLE_QUOTE = '"'

    /**
     * The ASCII representation of a single quotation mark (apostrophe)
     * @see #JSON_ESCAPED_QUOTE
     * @see #ASCII_SINGLE_QUOTE
     */
    public static final String JSON_SINGLE_QUOTE = ASCII_SINGLE_QUOTE

    /**
     * The Unicode representation for an embedded single quotation mark (apostrophe). See
     * <a href="http://marcgrabanski.com/groovy-escape-quote-string/">http://marcgrabanski.com/groovy-escape-quote-string/</a>
     * for a full explanation of why the single quote must be replaced with the Unicode value in JSON.
     * @see #JSON_SINGLE_QUOTE
     * @see #UTF8_SINGLE_QUOTE
     */
    public static final String JSON_ESCAPED_QUOTE = UTF8_SINGLE_QUOTE


    /**
     * Name of the curl command line maximum time option
     */
    public static final CharSequence MAX_TIME_OPTION = '--max-time'

    /**
     * Name of the SAFSREST maximum time argument. This argument allows test
     * authors to set the maximum time allowed to wait for a response to
     * a request (in seconds).
     */
    public static final CharSequence MAX_TIME_ARGUMENT_NAME = 'maxTime'

    /**
     * Default maximum number of seconds the SAFSREST CurlCommand waits for a
     * curl request to receive a response.
     *
     * Currently, this constant has a value of 30 (seconds).
     */
    public static final int DEFAULT_MAX_TIME = 30


    public static final WRAPPER_CHARACTERS = [
            ASCII_SINGLE_QUOTE,
            ASCII_DOUBLE_QUOTE
    ]

    private static final int FIRST = 0
    private static final int LAST = -1


    private boolean isRequestBodyJson = false

    private static final Set HEADERS_USING_ETAGS = [IF_MATCH, IF_NONE_MATCH, IF_RANGE]


    /**
     * A textual representation of the HTTP entrypoint to be used with this
     * CurlCommand. The entrypoint should contain the full URL where a request
     * will be sent, including any path or query parameters specified
     * appropriately.
     */
    String entrypoint = ''

    /**
     * The HTTP verb used for sending the request.
     *
     * @see HttpMethod
     */
    HttpMethod httpMethod

    /**
     * This property should contain a textual representation of a body when
     * the request includes one. This value defaults to {@link Response#EMPTY_BODY}.
     */
    String requestBody = EMPTY_BODY
	String rawRequestBody = requestBody
	
    /**
     * accept contains the String media type value for the HTTP Accept: header
     *
     * @deprecated Use {@link #httpHeaders} instead.
     */
    @Deprecated
    String accept = null

    /**
     * contentType contains the String media type value for the HTTP Content-Type: header
     *
     * @deprecated Use {@link #httpHeaders} instead.
     */
    @Deprecated
    String contentType = null


    // TODO brfaul 10 November 2016: Need to add Groovydoc comment. Also, the
    // values specified for the Accept and Content-Type headers in httpHeaders
    // will supersede the values specified for the accept and contentType properties.
    HttpHeaders httpHeaders = null

    /**
     * By default, CurlCommand generates the --location curl option to
     * automatically follow redirects for a request. Set autoRedirect to false
     * to prevent CurlCommand from generating this option.
     */
    boolean autoRedirect = true

    /**
     *  The final modifier tells Groovy to only generate a getter method for
     *  propertyProvider. No setter method will be generated, but an instance
     *  of a SafsRestPropertyProvider can be injected into this object via
     *  a named argument on the constructor. If no SafsRestPropertyProvider has
     *  been injected via the constructor, then one will be created.
     */
    final SafsRestPropertyProvider propertyProvider = null


    private static final SystemPropertyProvider SYSTEM_PROPERTIES = new SystemPropertyProvider()
    private static final boolean WINDOWS_OS = !SYSTEM_PROPERTIES.linux



// TODO brfaul Nov 17, 2015: Consider refactoring this constructor so that there is no
// longer a need for it by implementing all the necessary setter methods and
// just letting "normal" Groovy bean constructor processing occur.
    CurlCommand(Map arguments) {
        executable = CURL_EXECUTABLE

        def propertyProviderArgument = loadArgument arguments, PROPERTY_PROVIDER_ARGUMENT_NAME

        if (propertyProviderArgument && propertyProviderArgument instanceof SafsRestPropertyProvider) {
            propertyProvider = propertyProviderArgument
        } else {
            propertyProvider = new SafsRestPropertyProvider()
        }

        initializeRequestParameters arguments
    }


    private loadArgument(Map arguments, CharSequence argumentName) {
        def argumentValue = ''

        def argumentToLoad = arguments?."${argumentName}"

        if (argumentToLoad || argumentToLoad == 0 || isBooleanArgument(argumentToLoad)) {
            argumentValue = argumentToLoad

            if (argumentValue instanceof String) {
                argumentValue = argumentValue.trim()
            }
        }

        argumentValue
    }


    private boolean isBooleanArgument(argument) {
        boolean isBooleanArgument = false

        // To support arguments with a boolean value of false, convert
        // the argument to a String so the empty property can be examined
        // to determine the actual value of the argument. Without this
        // conversion (and the subsequent check below), the Groovy truth
        // value will be evaluated, which causes the incorrect value to
        // be set when the actual value is false.
        if (argument instanceof Boolean) {
            String argumentString = argument as String

            isBooleanArgument = (argumentString?.empty == false)
        }

        isBooleanArgument
    }


    private void initializeRequestParameters(Map arguments) {
        def entrypointParameter = loadArgument arguments, ENTRYPOINT_ARGUMENT_NAME
        setEntrypoint entrypointParameter

        def acceptParameter = loadArgument arguments, ACCEPT_ARGUMENT_NAME
        setAccept acceptParameter

        def contentTypeParameter = loadArgument arguments, CONTENT_TYPE_ARGUMENT_NAME
        setContentType contentTypeParameter

// TODO brfaul 15 November 2016: Must call setHttpHeaders after calling setAccept
// and setContentType as current (temporary for SAFSREST 0.8.3) implementation of
// setHttpHeaders will only look for the accept and content type headers and then
// set those properties. A future version of SAFSREST (> 0.9.0) will remove the
// accept and contentType properties and provide full support for generating
// the proper curl options for any HTTP headers.
        def httpHeadersParameter = loadArgument arguments, HTTP_HEADERS_ARGUMENT_NAME
        setHttpHeaders httpHeadersParameter

        def requestBodyParameter = loadArgument arguments, REQUEST_BODY_ARGUMENT_NAME
        setRequestBody requestBodyParameter

        def autoRedirectParameter = loadArgument arguments, AUTO_REDIRECT_ARGUMENT_NAME
        setAutoRedirect autoRedirectParameter

        def userOptions = loadArgument arguments, OPTIONS_ARGUMENT_NAME
        def userOptionList = [ userOptions ].flatten()
        initOptions userOptionList

        def maxTimeParameter = loadArgument arguments, MAX_TIME_ARGUMENT_NAME
        initMaxTime maxTimeParameter

        def formFieldsParameter = loadArgument arguments, FORM_FIELDS_ARGUMENT_NAME
        initFormFields formFieldsParameter

        def httpMethodParameter = loadArgument arguments, HTTP_METHOD_ARGUMENT_NAME
        setHttpMethod httpMethodParameter

        initInsecureOption()
    }


    private void initOptions(userOptions) {
        def defaultOptions = initializeDefaultOptions userOptions

        def allOptions = [
            defaultOptions
        ]

        def okToAddUserOptions = hasUserOptionListToAdd userOptions

        if (okToAddUserOptions) {
            userOptions = userOptions.collect { option ->
                if (option.startsWith(HEADER_OPTION) && option.contains(' ')) {
                    option = /'$option'/
                }
                option
            }
            allOptions << userOptions
        }

        this.options = allOptions.flatten()

        // Set allOptions before ensuring that a HTTP Content-Type header
        // has been specified IF the command requires that header.
        requireContentTypeHeader allOptions
    }


    private boolean hasUserOptionListToAdd(List userOptions) {
        boolean valid = false

        if (userOptions && userOptions.size() > 0) {
            valid = true

            if ((userOptions.size() == 1) && userOptions[FIRST].trim() == '') {
                valid = false
            }
        }

        valid
    }


    private List initializeDefaultOptions(List userOptions) {
        List defaultOptions = responseInfoOption.flatten()

        if (entrypoint) {
            CharSequence normalizedEntrypoint = normalizeString entrypoint

            TokenProviderEntrypoints tokenEntrypoints = new TokenProviderEntrypoints(rootUrl: normalizedEntrypoint)

            if (entrypoint != tokenEntrypoints.authTokenResource) {
                defaultOptions << initializeAuthorizationHeaderOption(userOptions)
            }
        }

        defaultOptions << initializeAllHeaderOptions()
        defaultOptions << initializeLocationOption(userOptions)

        defaultOptions.flatten()
    }


    private List initializeAllHeaderOptions() {
        List headerOptions = []

        httpHeaders?.each { headerName, headerValue ->
            String headerValueString = convertHeaderValueToString headerValue

            headerOptions << makeHeaderOption(headerName, headerValueString)
        }

// TODO brfaul 13 December 2016: Temporarily add the Accept header if httpHeaders
// does not contain a value but the accept property has been specified. When the
// deprecated accept property has been removed, this code can be removed as well.
        if (!httpHeaders?.getAccept() && accept) {
            headerOptions << makeHeaderOption(ACCEPT_HEADER, accept)
        }

// TODO brfaul 13 December 2016: Temporarily add the Content-Type header if httpHeaders
// does not contain a value but the contentType property has been specified. When the
// deprecated contentType property has been removed, this code can be removed as well.
        if (!httpHeaders?.getContentType() && contentType) {
            headerOptions << makeHeaderOption(CONTENT_TYPE_HEADER, contentType)
        }

        headerOptions
    }


    /**
     * Converts a header value to a String.
     *
     * The Spring HttpHeaders object is an instance of a MultiValueMap,
     * so the value will likely always be a List. To be conservative,
     * check to see if the value is an instance of a Collection. If so,
     * strip the brackets from the string representation.
     *
     * @param headerValue is an object representing a header value from an
     * instance of {@link HttpHeaders}
     * @return the String representation of the object WITHOUT any collection
     * notation
     */
    private String convertHeaderValueToString(headerValue) {
        String headerValueString = headerValue as String

        if (headerValue instanceof Collection) {
            // Strip the list brackets notation from the String representation
            headerValueString = headerValueString[1..-2]
        }

        headerValueString
    }


    /**
     * Normalize the candidate String by stripping a leading or trailing single
     * quotation mark, if either are present.
     *
     * For instance, a single quotation mark must be stripped so that a URL
     * object can be created without causing a MalformedURLException.
     *
     * @param candidate a string to be normalized
     * @return the candidate string with any leading or trailing
     * single quotation mark stripped.
     */
    private String normalizeString(String candidate) {
        String normalizedString = candidate

        if (normalizedString) {
            if (normalizedString[FIRST] == PRESERVE_SPECIAL_CHARACTERS_QUOTE) {
                // Strip the leading quotation mark
                normalizedString = normalizedString[1..LAST]

                if (normalizedString[LAST] == PRESERVE_SPECIAL_CHARACTERS_QUOTE) {
                    // Strip the trailing single quotation mark
                    normalizedString = normalizedString[FIRST..-2]
                }
            }
        }

        normalizedString
    }


    private List initializeAuthorizationHeaderOption(List userOptions) {
        List authorizationHeaderOption = []

        if (hasAuthorizationHeader(userOptions) == false && propertyProvider?.authToken) {
            String authorizationHeader =
                MessageFormat.format "${AUTHORIZATION_BEARER_HEADER}", propertyProvider.authToken

            authorizationHeaderOption << /${HEADER_OPTION}${authorizationHeader}/
        }

        authorizationHeaderOption
    }


    private CharSequence makeHeaderOption(String header, String headerValue) {
        assert header
        assert headerValue

        CharSequence headerOption = /${HEADER_OPTION}${header}${HEADER_FIELD_SEPARATOR}${headerValue}/

// TODO brfaul 13 December 2016: For now, just wrap with single quotes the
// values of any headers where an eTag can be specified.
// TODO brfaul 14 December 2016: Consider whether all
// header options should be wrapped for simplicity's sake.
// TODO brfaul 14 December 2016: Also, consider whether some attempt should be
// made to "guess" when a headerValue is an ETag, perhaps by checking for the
// presence of leading and trailing ASCII_DOUBLE_QUOTE characters? Research
// needs to be done to determine whether there is a reliable way to recognize
// an ETag. Of course, if the simpler route to wrap all header options gets
// implemented, then there's no need to try to recognize ETag values.
        if (header in HEADERS_USING_ETAGS) {
            headerOption = "${ASCII_SINGLE_QUOTE}${headerOption}${ASCII_SINGLE_QUOTE}"
        }

        headerOption
    }


    private void requireContentTypeHeader(userOptions) {
        boolean needsContentTypeHeader = isContentTypeHeaderNeeded userOptions

        if (needsContentTypeHeader && !hasContentTypeHeader(userOptions)) {
            // Because a content type header needs to be generated, and no good
            // default value exists, then throw an exception if the user has
            // not supplied the content type header option nor the contentType
            // property.
            throw new IllegalStateException(EXPECTED_CONTENT_TYPE_MESSAGE)
        }
    }


    /**
     * Determine if an HTTP Content-Type header needs to be added for this request.
     * The Content-Type header should only be present when the user has not supplied
     * it and when there is a requestBody (i.e. for POST, PUT, DELETE, etc.)
     *
     * @param userOptions the List of current user options for this CurlCommand
     * @return true if the user did not supply a Content-Type header AND the
     * requestBody on this CurlCommand is not empty; false otherwise.
     */
    private boolean isContentTypeHeaderNeeded(List userOptions) {
        !hasContentTypeHeader(userOptions) && requestBody
    }


    private boolean hasContentTypeHeader(userOptions) {
        def hasContentTypeInOptions = hasContentTypeHeaderInOptions userOptions
        httpHeaders?.getContentType() || contentType || hasContentTypeInOptions
    }


    private def initializeLocationOption(userOptions) {
        def defaultLocationOption = ''

        if (autoRedirect) {
            defaultLocationOption = LOCATION_OPTION
        }

        defaultLocationOption
    }


    private boolean hasOption(option) {
        hasOption this.options, option
    }


    private boolean hasOption(List userOptions, CharSequence singleOption) {
        def foundOption = userOptions.find { option ->
            if (option instanceof List) {
                hasOption option, singleOption
            } else {
                String optionString = option as String
                optionString?.toLowerCase().contains singleOption.toLowerCase()
            }
        }

        foundOption as boolean
    }


    private boolean hasAuthorizationHeader(List userOptions) {
        hasOption userOptions, AUTHORIZATION_HEADER
    }


    private boolean hasContentTypeHeaderInOptions(List userOptions) {
        hasOption userOptions, CONTENT_TYPE_HEADER
    }


    /**
     * @param entrypointValue an URL to use with this instance of a CurlCommand
     */
    private void setEntrypoint(entrypointValue = '') {
        def quotedEntrypointValue = quoteEntrypointWhenHasSpecialCharacters entrypointValue

        this.entrypoint = quotedEntrypointValue

        // Set the superclass value as well.
        data = this.entrypoint
    }


    /**
     * When the entrypointValue contains special characters [such as ?, (, )], the entrypointValue
     * must be enclosed in single quotation marks [e.g. ' '] so that the console output can be copied
     * and pasted into a terminal window and executed without modification.
     *
     * If the entrypointValue does NOT contain special characters, then it does not need to be wrapped
     * by single quotation marks, so this method simply returns the entrypoint value in that case.
     *
     * @param entrypointValue a CharSequence representing an URL with possible special characters
     * @return "'${entrypointValue}'" when entrypointValue contains special characters; entrypointValue
     * otherwise
     */
    private quoteEntrypointWhenHasSpecialCharacters(entrypointValue) {
        def quotedEntrypointValue = entrypointValue

        if (quotedEntrypointValue) {
            if (hasSpecialCharacter(quotedEntrypointValue)) {
                if (entrypointValue[FIRST] != PRESERVE_SPECIAL_CHARACTERS_QUOTE) {
                    quotedEntrypointValue = "${PRESERVE_SPECIAL_CHARACTERS_QUOTE}${quotedEntrypointValue}"
                }
                if (quotedEntrypointValue[LAST] != PRESERVE_SPECIAL_CHARACTERS_QUOTE) {
                    quotedEntrypointValue = "${quotedEntrypointValue}${PRESERVE_SPECIAL_CHARACTERS_QUOTE}"
                }
            }
        }

        quotedEntrypointValue
    }


    private boolean hasSpecialCharacter(entrypointValue) {
        def specialCharacters = [
                '&', '(', ')'
        ]

        def hasSpecialCharacter = entrypointValue.find { character ->
            character in specialCharacters
        }

        hasSpecialCharacter as boolean
    }


    /**
     * Set the value of the {@link #requestBody} property to the value
     * supplied in the parameter. If the supplied requestBodyValue contains
     * embedded single quotes, then the requestBody will be
     * {@link #normalizeRequestBody(String) normalized}.
     *
     * @param requestBodyValue a String representing the value to be supplied
     * as part of a request. This value normally represents a JSON object
     * or collection. The default value for requestBodyValue is
     * {@link Response#EMPTY_BODY}.
     */
    private void setRequestBody(requestBodyValue = EMPTY_BODY) {
        this.requestBody = requestBodyValue
		this.rawRequestBody = this.requestBody
		
        setRequestBodyJson()

        formatRequestBody()
    }


    /**
     * Set the {@link #isRequestBodyJson} property to accurately reflect
     * whether the value of requestBody property represents JSON or not.
     */
    private void setRequestBodyJson() {
        if (this.requestBody) {
            def body = this.requestBody

            if (body[FIRST] == ASCII_SINGLE_QUOTE) {
                body = body[1..LAST]
            }

            if (body[LAST] == ASCII_SINGLE_QUOTE) {
                body = body[FIRST..-2]
            }

            JsonSlurper slurper = new JsonSlurper()
            // Attempt to slurp the requestBody to determine whether or not it is
            // JSON. If the requestBody cannot be parsed, a JsonException will be
            // thrown. If the requestBody is null, then IllegalArgumentException
            // will be thrown. In either case, the requestBody is not JSON
            try {
                slurper.parseText body

                this.isRequestBodyJson = true

            } catch (JsonException | IllegalArgumentException e) {
                this.isRequestBodyJson = false
            }
        }
    }


    /**
     * Formats the requestBody property (if needed) with appropriate wrapper
     * quotation marks depending on whether the body content is a JSON string,
     * a XML string,  or a plain string and whether or not the requestBody
     * needs to be formatted because of embedded special characters or
     * quotation marks.
     */
    private void formatRequestBody() {
        if (this.requestBody) {
            if (isRequestBodyJson) {
                formatJsonRequestBody()
            } else {
                formatOtherRequestBody()
            }
        }
    }


    /**
     * Formats the requestBody property (if needed) with appropriate wrapper
     * quotation marks when the body is JSON
     */
    private void formatJsonRequestBody() {
        def hasEmbeddedSingleQuote = hasJsonBodyWithEmbeddedSingleQuote this.requestBody

        if (hasEmbeddedSingleQuote) {
            // The request body is JSON and has embedded single quotes,
            // so format the body appropriately.
            this.requestBody = normalizeRequestBody this.requestBody
        }
    }


    /**
     * Formats the requestBody property (if needed) with appropriate wrapper
     * quotation marks when the body is not JSON (i.e. XML or plain text).
     */
    private void formatOtherRequestBody() {
        if (isXmlContentType()) {
            // When the content type is XML, replace all the single
            // quotes with the XML apostrophe entity for use in the
            // bash and OS native execution environments.
            this.requestBody = this.requestBody.replace ASCII_SINGLE_QUOTE, XML_APOSTROPHE_ENTITY

        } else if (needsWrapperQuote(this.requestBody)) {
            // The request body is NOT JSON, NOT XML, and needs
            // wrapper quotes.
            this.requestBody = /"${this.requestBody}"/
        }
    }


    /**
     * Returns true if the contentType property (Content-Type header) of this
     * CurlCommand is a XML content type.
     *
     * @return true if the contentType is XML
     */
    private boolean isXmlContentType() {
        String headerContentType = httpHeaders?.getContentType()

        this.contentType?.endsWith('xml') || headerContentType?.endsWith('xml')
    }


    /**
     * Determines whether a text representation of a non-JSON body needs to be wrapped
     * in quotation marks or not.
     *
     * @param body the text representation of a non-JSON request body
     * @return true if the text representation needs to be wrapped; false otherwise
     */
    private boolean needsWrapperQuote(CharSequence body) {
        boolean mustBeWrapped = false

        if (body?.size() > 0) {
            def firstCharacter = body[FIRST]
            def lastCharacter = body[LAST]

            boolean isFirstCharacterWrapper = isWrapperCharacter firstCharacter
            boolean isLastCharacterWrapper = isWrapperCharacter lastCharacter
            boolean hasOtherSingleQuotation = containsSingleQuotation body[1..-2]

            mustBeWrapped = !isFirstCharacterWrapper || !isLastCharacterWrapper || hasOtherSingleQuotation
        }

        mustBeWrapped
    }


    /**
     * Determines whether the specified character is in the list of wrapper
     * characters
     *
     * @param character the character to be tested against the list of wrapper
     * characters
     * @return true if the wrapper character list contains the specified
     * character; false otherwise.
     */
    private boolean isWrapperCharacter(character) {
        WRAPPER_CHARACTERS.contains character
    }


    /**
     * Determines whether the specified text contains a single quote (') mark.
     *
     * @param text the text to search for a single quote mark
     * @return true if a single quote is in the text, false otherwise
     */
    private boolean containsSingleQuotation(CharSequence text) {
        def index = text.findIndexOf { character ->
            character == ASCII_SINGLE_QUOTE
        }

        index >= 0
    }


    /**
     * Checks to see if the specified JSON request body (as a String) contains
     * any embedded single quotes. For purposes of this comparison, if the
     * requestBodyValue starts with a single quote or ends with a single quote,
     * those are stripped off to determine if the string contains any embedded
     * quotes.
     *
     * @param requestBodyValue a String representing some JSON that might have
     * embedded single quotes
     * @return true if at least one single quote is found between the first and
     * last characters of the string; false otherwise
     */
    private boolean hasJsonBodyWithEmbeddedSingleQuote(String requestBodyValue) {
        def normalizedRequestBody = normalizeString requestBodyValue

        def hasEmbeddedSingleQuote = normalizedRequestBody.findAll { jsonCharacter ->
            jsonCharacter == JSON_SINGLE_QUOTE
        }

        hasEmbeddedSingleQuote as boolean
    }


    /**
     * Converts a String representation of a JSON request body into a normal
     * form as follows:
     * <ul>
     *     <li>
     *         Any embedded ASCII single quotation marks (') will be converted
     *         into the Unicode equivalent representation (\u0027).
     *     </li>
     *     <li>
     *         An ASCII initial leading (position 0 in the string) single
     *         quote remains as an ASCII single quote.
     *     </li>
     *     <li>
     *         An ASCII initial trailing (position -1 in the string) single
     *         quote remains as an ASCII single quote.
     *     </li>
     * </ul>
     *
     * <ol>
     *     <li>
     *         Example with no embedded quotes
     *         <ul>
     *             <li>
     *                 <tt>{ "where" : "team = Duke"}</tt> becomes
     *             </li>
     *             <li>
     *                 <tt>{ "where": "team = Duke"}</tt>
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         Example with embedded quotes
     *         <ul>
     *             <li>
     *                 <tt>{ "where" : "team = 'Duke Blue Devils'"}</tt> becomes
     *             </li>
     *             <li>
     *                 <tt>{ "where": "team = \u0027Duke Blue Devils\u0027"}</tt>
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         Example with embedded, leading, and trailing quotes
     *         <p>
     *             <strong>NOTE:</strong> Valid JSON does not begin with a single
     *              quotation mark, but if a test author has enclosed the string
     *              in single quotes for purposes of passing it as a curl option,
     *              then honor what has been passed.
     *         </p>
     *         <ul>
     *             <li>
     *                 <tt>'{ "where" : "team = 'Duke Blue Devils'"}'</tt> becomes
     *             </li>
     *             <li>
     *                 <tt>'{ "where": "team = \u0027Duke Blue Devils\u0027"}'</tt>
     *             </li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param requestBodyValue a String representing a JSON object or collection
     * @return the normalized String as described in the rules and examples above
     */
    private String normalizeRequestBody(String requestBodyValue) {
        String normalizedBody = requestBodyValue

        // The normalizeString method strips the leading and trailing single
        // quotation marks if any are present.
        String normalizedContent = normalizeString normalizedBody

        normalizedBody = normalizedContent.replaceAll JSON_SINGLE_QUOTE, JSON_ESCAPED_QUOTE

        // Add the leading and trailing single quotation marks back to the normalized
        // body if any are present.
        normalizedBody = addPreserveSpecialCharactersQuote requestBodyValue, normalizedBody

        normalizedBody
    }


    /**
     * Restore the leading and trailing single quotation marks to the normalizedRequestBody
     * if the originalRequestBody contained them
     *
     * @param originalRequestBody a String that has NOT been normalized and represents a
     * JSON object or collection
     * @param normalizedRequestBody a String that has been normalized
     * @return the normalizedRequestBody with leading and/or trailing single quotation
     * marks added as needed.
     */
    private String addPreserveSpecialCharactersQuote(originalRequestBody, normalizedRequestBody) {
        String normalizedBody = normalizedRequestBody

        def wrapperQuotation = PRESERVE_SPECIAL_CHARACTERS_QUOTE
        if (!isRequestBodyJson) {
             wrapperQuotation = ASCII_DOUBLE_QUOTE
        }

        if (originalRequestBody) {
            if (originalRequestBody[FIRST] == wrapperQuotation) {
                normalizedBody = "${wrapperQuotation}${normalizedBody}"
            }

            if (originalRequestBody[LAST] == wrapperQuotation) {
                normalizedBody <<= wrapperQuotation
            }
        }

        normalizedBody
    }


    /**
     * Sets the HTTP method name from a String.
     *
     * @param httpMethod the String representation of an HTTP Method
     * @see HttpMethod
     */
    private void setHttpMethod(String httpMethod) {
        HttpMethod method = GET

        if (httpMethod) {
            method = "${httpMethod.toUpperCase()}" as HttpMethod
        }

        setHttpMethod method
    }


    /**
     * Sets the HTTP method name from a Spring {@link HttpMethod} enumeration.
     *
     * @param httpMethod a Spring {@link HttpMethod} enumeration representing
     * a HTTP method
     * @see HttpMethod
     */
    private void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod

        initPropertiesForHttpMethod()
    }


    private initPropertiesForHttpMethod() {
        switch (httpMethod) {
            case HEAD:
                initHttpMethodOption HEAD_OPTION
                break

            case POST:
                initPostOptions()
                break

            case DELETE:
                initDeleteOptions()
                break

            case PUT:
                initPutOptions()
                break

            case PATCH:
                initPatchOptions()
                break

            case GET:
            default:
                // Case included for completeness as there's no current need to
                // do anything for GET since curl defaults to the GET method
                // when neither -X nor --request are specified on the command
                // line.
                break
        }
    }


    private initPostOptions() {
        initHttpMethodOption POST_OPTION
    }


    private initHttpMethodOption(option) {
        if (hasOption(option) == false) {
            this.options << option
        }

        initDataBinaryOption()
    }


    private initDataBinaryOption() {
        if (hasOption(DATA_BINARY_OPTION) == false) {
            if (requestBody) {
                this.options << DATA_BINARY_OPTION
                this.options << READ_STDIN_OPTION
            }
        }
    }


    private initDeleteOptions() {
        initHttpMethodOption DELETE_OPTION
    }


    private initPutOptions() {
        initHttpMethodOption PUT_OPTION
    }


    private initPatchOptions() {
        initHttpMethodOption PATCH_OPTION
    }


    private List getResponseInfoOption() {
        [
            INCLUDE_RESPONSE_HEADERS_OPTION
        ]
    }


    private void initMaxTime(maxTimeParameter) {
        if (hasOption(MAX_TIME_OPTION) == false) {
            int maxTimeValue = propertyProvider.maxTime

            if (propertyProvider.isInteger(maxTimeParameter)) {
                maxTimeValue = maxTimeParameter as int
                if (maxTimeValue < 0) {
                    maxTimeValue = DEFAULT_MAX_TIME
                }
            }
            def maxTimeOption = "${MAX_TIME_OPTION} ${maxTimeValue}"
            this.options << maxTimeOption
        }
    }


    @Deprecated
    private void setAccept(acceptParameter) {
        if (acceptParameter) {
            this.accept = acceptParameter as String
        }
    }


    @Deprecated
    private void setContentType(contentTypeParameter) {
        if (contentTypeParameter) {
            this.contentType = contentTypeParameter as String
        }
    }


    private void setHttpHeaders(httpHeadersParameter) {
        if (httpHeadersParameter) {
            httpHeaders = httpHeadersParameter

// TODO brfaul 16 November 2016: Temporarily set the accept and contentType properties from the httpHeaders
// to minimize changes for SAFSREST 0.8.3. Post-0.8.3, these properties will be removed and the values will
// be provided by httpHeaders when generating the CurlCommand. This method call can then be safely removed.
            temporarySetIndividualHeaderProperties()
        }
    }


// TODO brfaul 16 November 2016: Temporarily set the accept and contentType properties from the httpHeaders
// to minimize changes for SAFSREST 0.8.3. Post-0.8.3, these properties will be removed and the values will
// be provided by httpHeaders when generating the CurlCommand. This method can then be safely removed.
    private void temporarySetIndividualHeaderProperties() {
        List acceptHeaders = httpHeaders.getAccept()
        if (acceptHeaders?.empty == false) {
            accept = null
        }

        MediaType contentType = httpHeaders.getContentType()
        if (contentType) {
            this.contentType = null
        }
    }


    private void initFormFields(formFieldsParameter) {
        if (hasOption(FORM_OPTION) == false) {
            // Convert each form field into the correct curl command line option
            List curlFormOptions = formFieldsParameter?.collect { String fieldName, fieldValue ->
                makeCurlFormOption fieldName, fieldValue
            }

            addFormOptions curlFormOptions
        }
    }


    static String makeCurlFormOption(String fieldName, fieldValue) {
        "${FORM_OPTION} ${fieldName}=${fieldValue}"
    }


    private void addFormOptions(List formOptions) {
        if (formOptions) {
            this.options << formOptions
            // Because formOptions is a List and this.options is a
            // List, reassign this.options to a flattened version of
            // itself so that the resulting options list will only be one
            // level deep.
            this.options = this.options.flatten()
        }
    }


    private void setAutoRedirect(autoRedirectParameter) {
        String autoRedirectValue = autoRedirectParameter as String

        if (autoRedirectValue?.empty == false) {
            autoRedirect = autoRedirectParameter
        }
    }


    private void initInsecureOption() {
        boolean isSecureProtocol = (propertyProvider.protocol == SECURE_HTTP_PROTOCOL)

        if (isSecureProtocol || propertyProvider.secureOptions != NONE) {
            this.options << INSECURE_OPTION
        }
    }



// TODO brfaul 02 March 2017: Temporarily comment out the loadCommandList
// method since it is not used yet. This code will be uncommented as the
// refactoring of ExecutableCommand, CurlCommand, CommandInvoker, and
// CurlInvoker progresses.
//    @Override
//    protected List loadCommandList() {
//        def commandListToExecute = []
//
//        commandListToExecute << "${executable}"
//
//        // In the case of CurlCommand, the dataProvider contains the entrypoint
//        // URL, so write that into the list ahead of the options to prevent
//        // certain cryptic curl failures.
//        dataProvider commandListToExecute
//
//        if (options.empty == false) {
//            commandListToExecute << options
//        }
//
//        commandListToExecute.flatten()
//    }


    /**
     * Creates a String representation, suitable for display in the console,
     * of the command list for this CurlCommand. If the command list contains
     * a {@link #DATA_OPTION} specifying a password query parameter, then this
     * method will cause a redacted password value to be generated for display
     * purposes only.
     *
     * <p>
     *     In most cases (the exception being a redacted password), this
     *     generated representation of the CurlCommand can be copied from the
     *     console and run from a bash shell.
     * </p>
     *
     * @return a String representing the displayable version of the command
     * list for this CurlCommand
     */
    @Override
    String getConsoleString() {
        List consoleCommandList = commandList.clone()

        redactPasswordIfNecessary consoleCommandList

        String consoleText = consoleCommandList.join COMMAND_LIST_SEPARATOR

        if (WINDOWS_OS) {
            consoleText = consoleText.replaceAll(/\\"/, '"')
            consoleText = consoleText.replaceAll(/\\'/, "\'")
        }

        consoleText = consoleText.replace('\\\\u', '\\u')

        consoleText
    }


    private void redactPasswordIfNecessary(List consoleCommandList) {
        int passwordIndex = consoleCommandList.findIndexOf { String command ->
            command.contains "${DATA_OPTION} ${PASSWORD_KEY}="
        }

        if (passwordIndex >= 0) {
            String passwordCommand = consoleCommandList[passwordIndex]

            consoleCommandList[passwordIndex] = makeRedactedPassword passwordCommand
        }
    }


    private String makeRedactedPassword(String passwordCommand) {
        String redactedPassword = passwordCommand

        List passwordElements = passwordCommand.split '='

        if (passwordElements.size() == 2) {
            redactedPassword = "${passwordElements[0]}=${PASSWORD_MASK}" as String
        }

        redactedPassword
    }


//    @Override
//    String getCommandString() {
//        String commandText = super.getCommandString()
//        commandText = commandText.replace('\\\\u', '\\u')
//        "${commandText}"
//    }

}
