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

package org.safs.rest.service.commands

import groovy.transform.ToString
import groovy.util.logging.Slf4j



/**
 *
 * @author Bruce.Faulkner
 * @author Barry.Myers
 */
@Slf4j
@ToString(includeNames=true)
class CommandResults {
    static final OUTPUT_TITLE = 'Output'
    static final ERROR_TITLE = 'Error'
    static final EXIT_VALUE_TITLE = 'Exit value'

    String output = ''
    String error = ''
    int exitValue = 0


    String getCommandResultsString() {
        StringBuilder builder = new StringBuilder()

        builder << '\n' * 2
        def resultsMessage = "${this.getClass().name}:"
        builder << "${resultsMessage}\n"

        def lineSeparator = '-' * resultsMessage.size()
        builder << lineSeparator
        builder << '\n'


        builder << prettyResultsString(OUTPUT_TITLE, output)
        builder << '\n'
        builder << prettyResultsString(ERROR_TITLE, error)
        builder << '\n'
        builder << "${EXIT_VALUE_TITLE}: ${exitValue}\n"

        builder << lineSeparator
        builder << '*** DONE EXECUTING ***\n'

        builder.toString()
    }


    String prettyResultsString(title, results) {
        StringBuilder builder = new StringBuilder()

        builder << "\t${title}:\n"
        builder << '\t\t<<\n'
        builder << "\t\t\t${results}"
        if (builder[-1] != '\n') {
            builder << '\n'
        }
        builder << '\t\t>>'

        builder.toString()
    }
}
