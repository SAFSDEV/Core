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

package org.safs.rest.service.specifications.providers

import groovy.util.logging.Slf4j


/**
 * Supplies the ability to print exception details.
 * <p>
 * <b>NOTE:</b> No SAFSREST-based code currently uses ExceptionMessageProvider.
 * However, this class has <b>NOT</b> been deprecated because it has been used
 * in the past and will likely be used again in the future.
 * </p>
 *
 * @author Bruce.Faulkner
 * @author Barry.Myers
 */
@Slf4j
class ExceptionMessageProvider {

    /**
     * Prints the details of an exception.
     *
     * <p>
     *     <b>NOTE:</b> This method currently prints the details to the system console. However,
     *     future changes will result in this method using Groovy logging to print the details.
     * </p>
     *
     * @param ex an exception
     */
    void printExceptionDetails(Exception ex) {
        String exceptionDetails = makeExceptionDetails ex

        log.error exceptionDetails
    }


    /**
     * Creates a string representation of the details of an exception.
     *
     * @param ex an exception
     * @return String representation of the exception details
     */
    String makeExceptionDetails(Exception ex) {
        def outputHeaderMessage = "exception message follows:"
        def exceptionMessage = ex?.message

        def details = [
            outputHeaderMessage,
            '-' * outputHeaderMessage.size(),
            exceptionMessage,
            '\n'
        ]

        details.join '\n'
    }

}
