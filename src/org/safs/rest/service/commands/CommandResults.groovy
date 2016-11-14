// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands

import groovy.transform.ToString
import groovy.util.logging.Slf4j



/**
 *
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
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
