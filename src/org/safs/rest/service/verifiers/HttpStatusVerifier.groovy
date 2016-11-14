// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.verifiers

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import org.safs.rest.service.commands.curl.Response

import org.springframework.http.HttpStatus



/**
 * Provides convenience methods to check the status (integer) and httpStatus (enum) of a Response object
 * against an expected Spring HttpStatus code.
 *
 * @author ***REMOVED***
 * @since May 6, 2015
 *
 */
class HttpStatusVerifier {

    boolean isExpected(Response response, HttpStatus expectedStatus) {
        def actualStatus = response?.status

        def statusValuesEqual = (actualStatus == response?.httpStatus.value)
        def statusValid = (actualStatus == expectedStatus.value)

        def actualHttpStatus = response?.httpStatus
        def httpStatusValid = (actualHttpStatus == expectedStatus)

        statusValuesEqual && statusValid && httpStatusValid
    }


    boolean isOk(Response response) {
        isExpected response, OK
    }


    boolean isCreated(Response response) {
        isExpected response, CREATED
    }


    boolean isNotFound(Response response) {
        isExpected response, NOT_FOUND
    }


    boolean isBadRequest(Response response) {
        isExpected response, BAD_REQUEST
    }


    boolean isNoContent(Response response) {
        isExpected response, NO_CONTENT
    }

}
