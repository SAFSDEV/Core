// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands

import groovy.transform.ToString
import groovy.util.logging.Slf4j



/**
 * @author Bruce.Faulkner
 * @author Barry.Myers
 */
@Slf4j
@ToString(includeNames=true)
class ExecutableCommand {
    public static final EXECUTABLE_MISSING_MESSAGE =
            "An executable must be set on ExecutableCommand. The value of the executable property ="
    public static final EXECUTABLE_COMMAND_DETAILS_MESSAGE =
            'ExecutableCommand details: {}'

    public static final String EMPTY_STRING = ''
    public static final String BLANK_STRING = ' '
    public static final String COMMAND_LIST_SEPARATOR = BLANK_STRING


    /**
     * The value provided for the executable must either be found on the path or must include an OS-appropriate
     * path to the executable along with the name of the executable.
     */
    String executable = null

    /**
     * Any command line options for the command. These options usually begin with - or --.
     */
    List options = []

    /**
     * The target of the command to be executed; usually the non-options portion of the command.
     *
     * <p>
     *      For instance, the command <code>which --all curl</code> has the following parts:
     *      <ul>
     *          <li>executable: <code>'which'</code></li>
     *          <li>options: <code>[ '--all' ]</code></li>
     *          <li>data: <code>'curl'</code></li>
     *      </ul>
     * </p>
     *
     */
    String data = EMPTY_STRING



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

        List commandListToExecute = loadCommandList()

        log.debug 'commandList: {}', commandListToExecute

        commandListToExecute
    }


    private List loadCommandList() {
        List commandListToExecute = []

        if (executable) {
            commandListToExecute << "${executable}"

            if (options.empty == false) {
                commandListToExecute << options
            }

            addFormattedData commandListToExecute

        } else {
            def message =
                    "${EXECUTABLE_MISSING_MESSAGE} ${executable}"

            throw new IllegalStateException(message)
        }

        commandListToExecute.flatten()
    }


    private void addFormattedData(List commandListToexecute) {
        formattedDataProvider commandListToexecute
    }


    /**
     * Returns a {@link Closure} that provides the data in the necessary
     * format for use by {@link #loadCommandList()}.
     *
     * <p>
     *     Subclasses of ExecutableCommand should override this method to
     *     provide data in other formats.
     * </p>
     *
     * @return {@link Closure} providing data property formatted as necessary
     */
    protected Closure getFormattedDataProvider() {
        { List commandListToExecute ->
            if (data.trim() != EMPTY_STRING) {
                commandListToExecute << "${data}"
            }
        }
    }


    /**
     * Creates a String representation, suitable for display in the console,
     * of the command list for this ExecutableCommand. This representation can
     * be copied from the console and run from a bash shell.
     *
     * @return a String representing the command list
     */
    String getConsoleString() {
        commandList.join COMMAND_LIST_SEPARATOR
    }
}
