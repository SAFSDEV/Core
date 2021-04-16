/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

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
 * @author Bruce.Faulkner
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
