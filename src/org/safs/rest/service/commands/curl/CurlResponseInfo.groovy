// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands.curl

import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.*

import java.nio.charset.Charset

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


/**
 *
 * @author ***REMOVED***
 *
 * @since May 11, 2015
 */
class CurlResponseInfo {
    public static final DEFAULT_CONTENT_TYPE_VERSION = ''
    public static final HTTP_MESSAGE_FIELD_SEPARATOR = ':'

    /**
     * The response information that the CurlCommand produces should begin
     * with the HTTP_PROTOCOL_VALUE.
     */
    public static final CharSequence HTTP_PROTOCOL_VALUE = 'HTTP'

    /**
     * The responseInfoLines property must be supplied via the constructor.
     * They represent the output from the curl --include option that shows
     * the HTTP status and all of the response headers used to build a
     * CurlResponseInfo instance.
     */
    List responseInfoLines =  []

    // These properties will be set by parsing the responseInfoLines, but
    // initialize them to default values to ensure a usable object
    MediaType contentType = APPLICATION_JSON
    String contentTypeVersion = DEFAULT_CONTENT_TYPE_VERSION
    Charset charset = UTF_8
    int status = I_AM_A_TEAPOT.value()
    HttpStatus httpStatus = I_AM_A_TEAPOT


    CurlResponseInfo(List responseInfoLines) {
        this.responseInfoLines = responseInfoLines

        loadProperties()
    }


    private void loadProperties() {
        if (responseInfoLines) {
            loadContentTypeProperties()
            loadStatusProperties()
        }
    }


    /**
     * The Content-Type header format follows a pattern similar to this example.
     * See the appropriate RFCs (2616 as well as the group of RFCs that
     * superseded 2616) for more information.
     *
     *   Content-Type: application/vnd.sas.collection;version=2;charset=UTF-8
     */
    private void loadContentTypeProperties() {
        def contentTypeLine = loadContentTypeLine()

        if (contentTypeLine) {
            def contentTypeFieldValue = loadContentTypeFieldValue contentTypeLine

            parseContentTypeString contentTypeFieldValue
        }
    }


    private def loadContentTypeLine() {
        responseInfoLines.find { String line ->
            line.startsWith CONTENT_TYPE
        }
    }


    private String loadContentTypeFieldValue(String contentTypeLine) {
        // Tokenize the line into the field name and field value based on
        // the : separator.
        def contentTypeLineTokens =
            contentTypeLine.tokenize HTTP_MESSAGE_FIELD_SEPARATOR
        assert contentTypeLineTokens.size() > 1

        // The field value should be everything to the right of
        // the : separator.
        def contentTypeFieldValue = contentTypeLineTokens[1].trim()

        contentTypeFieldValue
    }


    private void parseContentTypeString(String contentTypeString) {
        contentType = MediaType.parseMediaType contentTypeString

        def contentTypeVersionParameter = contentType.parameters?.version

        if (contentTypeVersionParameter) {
            contentTypeVersion = contentTypeVersionParameter
        }

        if (contentType.charSet) {
            charset = contentType.charSet
        }
    }


    /**
     * Gets the status properties from the curl --include output.
     *
     * The first line of the output should have the following format:
     *     protocol/version status statusDescription
     * For instance,
     *     HTTP/1.1 200 OK
     *     HTTP/1.1 404 Not Found
     */
    private void loadStatusProperties() {
        List statusEntries = responseInfoLines.findAll { String line ->
            line.startsWith 'HTTP'
        }

        String statusLine = null

        if (statusEntries) {
            statusLine = statusEntries[-1]
        }

        parseStatusLine statusLine
    }


    /**
     * This method process the status line returned with the response to
     * set the status (integer) and httpStatus (enumeration) properties of
     * this CurlResponseInfo.
     *
     * The statusLine should have the following format:
     *     statusCode statusDescription
     *
     * For instance,
     *     200 OK
     *     404 Not Found
     *
     * This method ignores the status description because the description
     * can be retrieved from the httpStatus property.
     *
     * @param statusLine A line with the HTTP status code and status
     * description, separated by whitespace.
     */
    private void parseStatusLine(String statusLine) {
        def tokenList = statusLine?.tokenize()

        if (tokenList?.size() > 1) {
            def statusValueString = tokenList[1]

            status = statusValueString.toInteger()
            httpStatus = HttpStatus.valueOf status
        }
    }

}
