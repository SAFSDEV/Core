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

package org.safs.rest.service.commands.curl.error


import org.safs.rest.service.commands.CommandResults



/**
 * Provides specific details about a curl command that fails to execute successfully.
 *
 * @author Bruce.Faulkner
 * @since 0.7.3
 */
class CurlErrorDetailsProvider {
    /**
     * Curl exit code for success. All curl exit codes > 0 indicate an error.
     */
    public static final int SUCCESS = 0

    /**
     * The curl error message should be the last line of the
     * {@link CommandResults} error property.
     */
    public static final int CURL_ERROR_MESSAGE_LINE_INDEX = -1


    /**
     * The results from executing a curl command. A value must be provided via
     * a named argument constructor.
     */
    CommandResults curlResults



    /**
     * Returns true if the curl exit code associated with this provider is any
     * value other than SUCCESS (0)
     *
     * @return true if the curl exit code associated with this provider is any
     * value other than SUCCESS (0); false otherwise
     */
    boolean isError() {
        curlResults?.exitValue != SUCCESS
    }


    /**
     * Produces a detailed message about the reason curl exited with an exit
     * code > 0. This message can be logged to provide a SAFSREST test author
     * with additional information about why curl exited.
     *
     * @return String a detailed message about the reason curl exited with an
     * exit code > 0; empty String otherwise.
     */
    String getDetails() {
        String detailMessage = ''

        if (isError()) {
            // The | character serves as a delimiter used with the here document
            // and the stripMargin() method to allow the here document to be
            // formatted properly both here in the source and in any resulting
            // log message output.
            detailMessage = """\
                    |    ERROR: While attempting to execute a curl command, the command exited with the following details:
                    |        * exit code   : '${curlResults.exitValue}'
                    |        * exit message: ${curlErrorMessage.inspect()}
                    |""".stripMargin('|')
        }

        detailMessage
    }


    /**
     * Returns the actual curl error message created by a failing curl command.
     * Finds the error message by parsing the error property of the
     * {@link CommandResults} object wrapped by this provider. The curl error
     * message should be the last line of the {@link CommandResults} error
     * property.
     *
     * @return a String with the curl error message
     */
    String getCurlErrorMessage() {
        String curlErrorMessage = ''

        def errorLines = curlResults?.error.readLines()

        if (errorLines?.size() > 0) {
            curlErrorMessage = errorLines[CURL_ERROR_MESSAGE_LINE_INDEX]
        }

        curlErrorMessage
    }
}
