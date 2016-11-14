// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands

import groovy.transform.ToString
import groovy.util.logging.Slf4j



/**
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
 */
@Slf4j
@ToString(includeNames=true)
class ExecutableCommand {
    public static final EXECUTABLE_MISSING_MESSAGE =
            "An executable must be set on ExecutableCommand. The value of the executable property ="
    public static final EXECUTABLE_COMMAND_DETAILS_MESSAGE =
            'ExecutableCommand details: {}'
    /**
     * The value provided for the executable must either be found on the path or must include an OS-appropriate
     * path to the executable along with the name of the executable.
     */
    String executable = null

    List options = []
    String data = ''



    /**
     * Returns a list of command elements to be used when the command gets
     * executed. The list consists of the executable, the options, and the data
     * in their String representation.
     * @see {ProcessGroovyMethods.execute(List)}
     *
     * @return a list of command elements
     */
    List getCommandList() {
        log.debug EXECUTABLE_COMMAND_DETAILS_MESSAGE, this.inspect()

        def commandListToExecute = loadCommandList()

        log.debug 'commandList: {}', commandListToExecute

        commandListToExecute
    }


    private List loadCommandList() {
        def commandListToExecute = []

        if (executable) {
            commandListToExecute << "${executable}"

            if (options.empty == false) {
                commandListToExecute << options
            }

            if (data.trim() != '') {
                commandListToExecute << "${data}"
            }

        } else {
            def message =
                "${EXECUTABLE_MISSING_MESSAGE} ${executable}"

            throw new IllegalStateException(message)
        }

        commandListToExecute.flatten()
    }

}
