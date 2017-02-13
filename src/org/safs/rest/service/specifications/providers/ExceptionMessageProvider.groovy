// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

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
 * @author ***REMOVED***
 * @author ***REMOVED***
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
