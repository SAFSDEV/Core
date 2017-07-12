// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands.curl

import static org.safs.rest.service.commands.curl.CurlResponseInfo.DEFAULT_CONTENT_TYPE_VERSION
import static org.safs.rest.service.commands.curl.CurlResponseInfo.HTTP_PROTOCOL_VALUE
import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

import java.nio.charset.Charset

import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.safs.rest.service.commands.CommandResults

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus



/**
 * Model a response received from a request made to a REST URL
 *
 * @author Bruce.Faulkner
 *
 * @see org.safs.rest.service.commands.CommandResults
 *
 */
@Slf4j
@ToString(includeNames=true)
class Response {
    public static final String EMPTY_BODY = ''

    /**
     *  The raw integer HTTP status value returned from executing the curl command
     */
    int status = I_AM_A_TEAPOT.value()

    /**
     * The Spring HttpStatus enumeration for the status code
     */
    HttpStatus httpStatus = I_AM_A_TEAPOT

    /**
     * The value of the content-type header returned as part of the response
     * @deprecated Use {@link #httpHeaders} instead.
     */
    @Deprecated
    String contentType = APPLICATION_JSON_VALUE

    /**
     * The value of the version for the Content-Type header returned as part
     * of the response.
     * @deprecated Use {@link #httpHeaders} instead.
     */
    @Deprecated
    String contentTypeVersion = DEFAULT_CONTENT_TYPE_VERSION

    /**
     * The value of the charset returned as part of the content-type header of
     * the response
     */
    Charset charset = UTF_8

    /**
     * The String body of the response returned from executing the curl command
     */
    String body = EMPTY_BODY

    // TODO: look into using the new httpHeaders instead.
    Map<String, String> headers
    
    /**
     * An instance of a CommandResults object must be provided to the Response
     * constructor via the named argument <tt>commandResults</tt>.
     */
    @Delegate CommandResults commandResults

// TODO Bruce Faulkner 09 December 2016: Consider making httpHeaders a delegate to
// avoid having test authors have to call the get/set methods through it.
// Make sure this is CAREFUL CONSIDERATION as given that HttpHeaders is a Map,
// the convenience by making the instance a delegate MIGHT be far outweighed
// by potential issues.
    /**
     * The HTTP headers contained in this response. Only those HTTP headers
     * supported by {@link HttpHeaders} will be available through this
     * property.
     *
     * @see HttpHeaders
     */
    HttpHeaders httpHeaders = new HttpHeaders()



    Response(args) {
        commandResults = args.commandResults

        log.debug 'Response constructor, commandResults: {}', commandResults
        log.debug 'Response constructor, commandResults?.exitValue: {}', commandResults?.exitValue

        if (commandResults?.exitValue == 0) {
            init()
        }

    }

    private void init() {
        CurlResponseInfo curlResponseInfo = parseOutput()

        initProperties curlResponseInfo
    }


    private CurlResponseInfo parseOutput() {
        // Read commandResults.output as lines
        def lines = commandResults.output.readLines()

        def normalizedLines = normalize lines

        CurlResponseInfo curlResponseInfo = loadCurlResponseInfo normalizedLines

        if (curlResponseInfo.httpStatus != I_AM_A_TEAPOT) {
            loadBody normalizedLines
        }

        curlResponseInfo
    }


    private def normalize(List lines) {
        List normalizedLines = null

        if (lines) {
            def httpStatusLineIndex = lines.findLastIndexOf { line ->
                line.startsWith HTTP_PROTOCOL_VALUE
            }

            if (httpStatusLineIndex >= 0) {
                normalizedLines = lines[httpStatusLineIndex..-1]
            }
        }

        normalizedLines
    }


    private void loadBody(lines) {
        def blankLineIndex = lines?.findIndexOf { line ->
            line.trim() == ''
        }

        def headerLines

        if (blankLineIndex >= 0) {
            headerLines = lines[1..<blankLineIndex]
            def bodyCandidateLines = lines[blankLineIndex..-1]

            def nonBlankLines = bodyCandidateLines.findAll { line ->
                line
            }

            def trimmedLines = nonBlankLines.collect { line ->
                line.trim()
            }

            body = trimmedLines?.join '\n'
        } else {
            headerLines = lines[1..-1]
        }
        def trimmedHeaderLines = headerLines.collect { line ->
            line.trim()
        }
        def header = trimmedHeaderLines?.join '\n'
        headers = CurlInvoker.getHeadersMapFromMultiLineString(header)
        
    }


    private CurlResponseInfo loadCurlResponseInfo(List lines) {
        new CurlResponseInfo(lines)
    }


    private void initProperties(CurlResponseInfo responseInfo) {
        // Use CurlResponseInfo information produced by curl --include to set
        // contentType, status, and httpStatus

        status = responseInfo.status
        httpStatus = responseInfo.httpStatus

        // this.contentType is a String, while responseInfo.contentType is a
        // MediaType, so build the String representation without including the
        // charset.
        contentType = "${responseInfo.contentType.type}/${responseInfo.contentType.subtype}"
        contentTypeVersion = responseInfo.contentTypeVersion

        charset = responseInfo.charset

        httpHeaders = responseInfo.httpHeaders
    }
}
