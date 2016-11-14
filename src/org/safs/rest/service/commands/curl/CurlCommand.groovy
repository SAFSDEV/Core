// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands.curl

import static org.safs.rest.service.commands.CommandInvoker.PRESERVE_SPECIAL_CHARACTERS_QUOTE
import static org.safs.rest.service.commands.curl.Response.EMPTY_BODY
import static org.springframework.http.HttpHeaders.ACCEPT as ACCEPT_HEADER
import static org.springframework.http.HttpHeaders.AUTHORIZATION as AUTHORIZATION_HEADER
import static org.springframework.http.HttpHeaders.CONTENT_TYPE as CONTENT_TYPE_HEADER
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
import org.safs.rest.service.models.providers.authentication.TokenProviderEntrypoints

import org.springframework.http.HttpMethod



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
     * DATA_BINARY_OPTION tells curl to treat the request body exactly as
     * it is specified. Use this option instead of the DATA_OPTION.
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
     * Name of the curl command line location option.
     *
     * The curl <code>--location</code> option follows a 30x redirect for a GET request; for a POST request
     * with status 301, 302, or 303, curl turns the POST into a GET before retrying the request (see the curl
     * man page for more information about what to do in this situation.
     */
    public static final LOCATION_OPTION = '--location'

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
    public static final FORM_FIELDS_ARGUMENT_NAME = 'formFields'


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
     */
    String accept = null

    /**
     * contentType contains the String media type value for the HTTP Content-Type: header
     */
    String contentType = null

    /**
     *  The final modifier tells Groovy to only generate a getter method for
     *  propertyProvider. No setter method will be generated, but an instance
     *  of a SafsRestPropertyProvider can be injected into this object via
     *  a named argument on the constructor. If no SafsRestPropertyProvider has
     *  been injected via the constructor, then one will be created.
     */
    final SafsRestPropertyProvider propertyProvider = null



// TODO brfaul Nov 17, 2015: Consider refactoring this constructor so that there is no
// longer a need for it by implementing all the necessary setter methods and
// just letting "normal" Groovy bean constructor processing occur.
    CurlCommand(Map arguments) {
        executable = CURL_EXECUTABLE

        def propertyProviderArgument = loadArgument arguments, PROPERTY_PROVIDER_ARGUMENT_NAME

        if (propertyProviderArgument && propertyProviderArgument instanceof SafsRestPropertyProvider) {
            propertyProvider = propertyProviderArgument
        } else if (!propertyProvider) {
            propertyProvider = new SafsRestPropertyProvider()
        }

        initializeRequestParameters arguments
    }


    private loadArgument(Map arguments, CharSequence argumentName) {
        def argumentValue = ''

        def argumentToLoad = arguments?."${argumentName}"

        if (argumentToLoad || argumentToLoad == 0 ) {
            argumentValue = argumentToLoad

            if (argumentValue instanceof String) {
                argumentValue = argumentValue.trim()
            }
        }

        argumentValue
    }


    private void initializeRequestParameters(Map arguments) {
        def entrypointParameter = loadArgument arguments, ENTRYPOINT_ARGUMENT_NAME
        setEntrypoint entrypointParameter

        def acceptParameter = loadArgument arguments, ACCEPT_ARGUMENT_NAME
        setAccept acceptParameter

        def contentTypeParameter = loadArgument arguments, CONTENT_TYPE_ARGUMENT_NAME
        setContentType contentTypeParameter

        def requestBodyParameter = loadArgument arguments, REQUEST_BODY_ARGUMENT_NAME
        setRequestBody requestBodyParameter

        def userOptions = loadArgument arguments, OPTIONS_ARGUMENT_NAME
        def userOptionList = [ userOptions ].flatten()
        initOptions userOptionList

        def maxTimeParameter = loadArgument arguments, MAX_TIME_ARGUMENT_NAME
        initMaxTime maxTimeParameter

        def formFieldsParameter = loadArgument arguments, FORM_FIELDS_ARGUMENT_NAME
        initFormFields formFieldsParameter

        def httpMethodParameter = loadArgument arguments, HTTP_METHOD_ARGUMENT_NAME
        setHttpMethod httpMethodParameter
    }


    private initOptions(userOptions) {
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

            URL entrypointUrl = new URL(normalizedEntrypoint)

            def entrypointParameters = [
                host: entrypointUrl.host,
                port: entrypointUrl.port
            ]
            TokenProviderEntrypoints tokenEntrypoints = new TokenProviderEntrypoints(entrypointParameters)

            if (entrypoint != tokenEntrypoints.authTokenResource) {
                defaultOptions << initializeAuthorizationHeaderOption(userOptions)
            }
        }

        defaultOptions << initializeDefaultAcceptHeaderOption(userOptions)

        defaultOptions << initializeDefaultContentTypeHeaderOption(userOptions)

        defaultOptions << initializeLocationOption(userOptions)

        defaultOptions.flatten()
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


    private List initializeDefaultAcceptHeaderOption(List userOptions) {
        List defaultAcceptHeaderOption = []

        if (hasAcceptHeader(userOptions) == false) {
            if (accept) {
                defaultAcceptHeaderOption << makeHeaderOption(ACCEPT_HEADER, accept)
            }
        }

        defaultAcceptHeaderOption
    }


    private CharSequence makeHeaderOption(String header, String mediaType) {
        assert header
        assert mediaType

        /${HEADER_OPTION}${header}${HEADER_FIELD_SEPARATOR}${mediaType}/
    }


    private List initializeDefaultContentTypeHeaderOption(userOptions) {
        List defaultContentTypeHeaderOption = []

        def needsContentTypeHeader = isContentTypeHeaderNeeded userOptions

        if (needsContentTypeHeader) {
            if (!contentType) {
                // Because a content type header needs to be generated, and no good
                // default value exists, then throw an exception if the user has
                // not supplied the content type header option nor the contentType
                // property.
                throw new IllegalStateException(EXPECTED_CONTENT_TYPE_MESSAGE)
            }

            defaultContentTypeHeaderOption << makeHeaderOption(CONTENT_TYPE_HEADER, contentType)
        }

        defaultContentTypeHeaderOption
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
        contentType || ((hasContentTypeHeader(userOptions) == false) && requestBody)
    }


    private def initializeLocationOption(userOptions) {
        def defaultLocationOption = ''

        if (isOptionMissing(userOptions, LOCATION_OPTION)) {
            defaultLocationOption = LOCATION_OPTION
        }

        defaultLocationOption
    }


    private boolean hasOption(option) {
        hasOption this.options, option
    }


    private boolean isOptionMissing(List allOptions, CharSequence singleOption) {
        hasOption(allOptions, singleOption) == false
    }


    private boolean hasOption(List userOptions, CharSequence singleOption) {
        def foundOption = userOptions.find { option ->
            option.toLowerCase().contains singleOption.toLowerCase()
        }

        foundOption
    }


    private boolean hasAuthorizationHeader(List userOptions) {
        hasOption userOptions, AUTHORIZATION_HEADER
    }


    private boolean hasContentTypeHeader(List userOptions) {
        hasOption userOptions, CONTENT_TYPE_HEADER
    }


    private boolean hasAcceptHeader(List userOptions) {
        hasOption userOptions, ACCEPT_HEADER
    }


// TODO brfaul Sep 29, 2015: Consider making this setter private to that
// the object is immutable; alternatively, consider @Immutable on the class.
    /**
     * <p><strong>NOTE:</strong></p>
     * <p>
     *     Users of the CurlCommand class should provide the entrypoint value
     *     as an argument to the constructor (via
     *     {@link #ENTRYPOINT_ARGUMENT_NAME}) instead of calling this method directly.
     * </p>
     * <p>
     *     A future release of SAFSREST might convert this method to private scope.
     * </p>
     *
     * @param entrypointValue a URL to use with this instance of a CurlCommand
     */
    void setEntrypoint(entrypointValue = '') {
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


// TODO brfaul Sep 29, 2015: Consider making this setter private to that
// the object is immutable; alternatively, consider @Immutable on the class.
    /**
     * Set the value of the {@link #requestBody} property to the value
     * supplied in the parameter. If the supplied requestBodyValue contains
     * embedded single quotes, then the requestBody will be
     * {@link #normalizeRequestBody(String) normalized}.
     *
     * <p><strong>NOTE:</strong></p>
     * <p>
     *     Users of the CurlCommand class should provide the requestBody value
     *     as an argument to the constructor (via
     *     {@link #REQUEST_BODY_ARGUMENT_NAME}) instead of calling this method
     *     directly.
     * </p>
     * <p>
     *     A future release of SAFSREST might convert this method to private scope.
     * </p>
     *
     * @param requestBodyValue a String representing the value to be supplied
     * as part of a request. This value normally represents a JSON object
     * or collection. The default value for requestBodyValue is
     * {@link Response#EMPTY_BODY}.
     */
    void setRequestBody(requestBodyValue = EMPTY_BODY) {
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
        this.contentType?.endsWith('xml')
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


// TODO brfaul Sep 29, 2015: Consider making this setter private to that
// the object is immutable; alternatively, consider @Immutable on the class.
    /**
     * <p><strong>NOTE:</strong></p>
     * <p>
     *     Users of the CurlCommand class should provide the HTTP method value
     *     as an argument to the constructor (via
     *     {@link #HTTP_METHOD_ARGUMENT_NAME}) instead of calling this method
     *     directly.
     * </p>
     * <p>
     *     A future release of SAFSREST might convert this method to private scope.
     * </p>
     *
     * @param httpMethod the String representation of an HTTP Method
     * @see HttpMethod
     */
    void setHttpMethod(String httpMethod) {
        HttpMethod method = GET

        if (httpMethod) {
            method = "${httpMethod.toUpperCase()}" as HttpMethod
        }

        setHttpMethod method
    }


// TODO brfaul Sep 29, 2015: Consider making this setter private to that
// the object is immutable; alternatively, consider @Immutable on the class.
    /**
     * <p><strong>NOTE:</strong></p>
     * <p>
     *     Users of the CurlCommand class should provide the HTTP method value
     *     as an argument to the constructor (via
     *     {@link #HTTP_METHOD_ARGUMENT_NAME}) instead of calling this method
     *     directly.
     * </p>
     * <p>
     *     A future release of SAFSREST might convert this method to private scope.
     * </p>
     *
     * @param httpMethod a Spring {@link HttpMethod} enumeration representing
     * a HTTP method
     * @see HttpMethod
     */
    void setHttpMethod(HttpMethod httpMethod) {
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


    private void setAccept(acceptParameter) {
        if (acceptParameter) {
            this.accept = acceptParameter as String
        }
    }


    private void setContentType(contentTypeParameter) {
        if (contentTypeParameter) {
            this.contentType = contentTypeParameter as String
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
}
