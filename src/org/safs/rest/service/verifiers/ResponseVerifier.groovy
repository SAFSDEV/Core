// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.verifiers

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

import org.safs.rest.service.commands.curl.Response

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType



/**
 * ResponseVerifier contains methods that can be used to verify various items in a Response object.
 *
 * Use the methods that check for the status of the response when you want to display a failure message if
 * the check fails. An injected instance of HttpStatusVerifier provides methods to actually check the
 * status of the Response.
 *
 * @author Bruce.Faulkner
 * @author Barry.Myers
 * @See HttpStatusVerifier
 */
class ResponseVerifier {
    /**
     * EXPECT_BODY can be used as a parameter to methods that optionally check
     * for a response body when the response should contain a body.
     */
    public static final boolean EXPECT_BODY = true

    /**
     * EXPECT_NO_BODY can be used as a parameter to methods that optionally check
     * for a response body when the response should contain a body.
     */
    public static final boolean EXPECT_NO_BODY = false

    /**
     * MUST inject an instance of HttpStatusVerifier via a constructor named argument
     */
    HttpStatusVerifier statusVerifier



    void expectStatus(Response response, HttpStatus status) {
        def failureMessage = """\n\
            Expected status ${status.name()} (${status.value()}), but instead received:
            status: ${response.status},
            httpStatus: ${response.httpStatus},
            body (follows on next line):
            ${response.body.inspect()}""".stripIndent().trim()

        assert statusVerifier.isExpected(response, status), failureMessage
    }


    /**
     * Expect that the status of the specified response is HttpStatus.OK (200)
     *
     * @param response the {@link Response} object for which status will be verified
     * @see HttpStatus
     */
    void expectStatusOk(Response response) {
        expectStatus response, OK
    }


    /**
     * Expect that the status of the specified response is HttpStatus.CREATED (201)
     *
     * @param response the {@link Response} object for which status will be verified
     * @see HttpStatus
     */
    void expectStatusCreated(Response response) {
        expectStatus response, CREATED
    }


    /**
     * Expect that the status of the specified response is HttpStatus.NO_CONTENT (204)
     *
     * @param response the {@link Response} object for which status will be verified
     * @see HttpStatus
     */
    void expectStatusNoContent(Response response) {
        expectStatus response, NO_CONTENT
    }


    /**
     * Expect that the specified response has a content type property matching
     * the provided media type.
     *
     * @param response the {@link Response} object to verify the value of the
     * contentType property; must be non-null.
     * @param expectedContentType the text representation of the expected media type
     * for the contentType property of the response
     */
    void expectContentType(Response response, CharSequence expectedContentType) {
        assert response

        def failureMessage = """\n\
            Expected Content-Type ${expectedContentType}, but instead received:
            contentType: ${response.contentType},
            status: ${response.status},
            httpStatus: ${response.httpStatus},
            body (follows on next line):
            ${response.body}""".stripIndent().trim()

        assert response.contentType == expectedContentType, failureMessage
    }


    /**
     * Convenience method for {@link #expectContentType(Response, CharSequence)}
     * with an expected content type of {@link MediaType#APPLICATION_JSON_VALUE}
     *
     * @param response the {@link Response} object expected to have a contentType property
     * with the value {@link MediaType#APPLICATION_JSON_VALUE}
     */
    void expectContentTypeApplicationJson(Response response) {
        expectContentType response, APPLICATION_JSON_VALUE
    }


    /**
     * Expect that the specified response exists and has the specified HttpStatus.
     *
     * @param response the {@link Response} object to verify
     * @param status the {@link HttpStatus} the response should have
     */
    void expectResponse(Response response, HttpStatus status) {
        assert response
        expectStatus response, status
    }


    /**
     * Expect that the specified response is CREATED. In other words, the response
     * should exist, it should have the status of HttpStatus.CREATED (201), and
     * the response should have a body.
     *
     * @param response the {@link Response} object to verify
     */
    void expectResponseCreated(Response response) {
        expectResponseCreated response, EXPECT_BODY
    }


    /**
     * Expect that the specified response is CREATED. In other words, the response
     * should exist, it should have the status of HttpStatus.CREATED (201), and
     * if expectBody is true, then the response should have a body.
     *
     * @param response the {@link Response} object to verify
     * @param expectBody true if the caller wants the existence of a response
     * body to be verified
     */
    void expectResponseCreated(Response response, boolean expectBody) {
        expectResponse response, CREATED

        verifyResponseBody response, expectBody
    }


    private void verifyResponseBody(Response response, boolean expectBody) {
        if (expectBody) {
            assert response.body
        }
    }


    /**
     * Expect that the specified response is Ok. In other words, the response
     * should exist, it should have the status of HttpStatus.OK (200), and
     * the response should have a body.
     *
     * @param response the {@link Response} object to verify
     */
    void expectResponseOk(Response response) {
        expectResponseOk response, EXPECT_BODY
    }


    /**
     * Expect that the specified response is Ok. In other words, the response
     * should exist, it should have the status of HttpStatus.OK (200), and
     * if expectBody is true, then the response should have a body.
     *
     * @param response the {@link Response} object to verify
     * @param expectBody true if the caller wants the existence of a response
     * body to be verified
     */
    void expectResponseOk(Response response, boolean expectBody) {
        expectResponse response, OK

        verifyResponseBody response, expectBody
    }


    /**
     * Expect that the specified response contains No Content. In other words,
     * the response should exist and it should have the status of
     * HttpStatus.NO_CONTENT (204) and the response body should be empty.
     *
     * @param response the {@link Response} object to verify
     */
    void expectResponseNoContent(Response response) {
        expectResponse response, NO_CONTENT
        assert !response.body
    }


    /**
     * Expect that the specified response is not found. In other words,
     * the response should exist and it should have the status of
     * HttpStatus.NOT_FOUND (404).
     *
     * @param response the {@link Response} object to verify
     */
    void expectResponseNotFound(Response response) {
        expectResponse response, NOT_FOUND
    }


    /**
     * Expect that the specified response is FORBIDDEN. In other words, the response
     * should exist, it should have the status of HttpStatus.FORBIDDEN (403), and
     * if expectBody is true, then the response should have a body.
     *
     * @param response the {@link Response} object to verify
     * @param expectBody true if the caller wants the existence of a response
     * body to be verified
     */
    void expectResponseForbidden(Response response, boolean expectBody) {
        expectResponse response, FORBIDDEN

        verifyResponseBody response, expectBody
    }
}
