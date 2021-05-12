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

import static org.safs.rest.service.commands.curl.CurlCommand.ASCII_DOUBLE_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.DATA_BINARY_OPTION
import static org.safs.rest.service.commands.curl.CurlCommand.JSON_ESCAPED_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.JSON_SINGLE_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.READ_STDIN_OPTION
import static org.safs.rest.service.commands.curl.CurlCommand.WRAPPER_CHARACTERS

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.safs.rest.service.models.providers.SystemInformationProvider
import org.safs.rest.service.models.providers.SystemPropertyProvider

import groovy.transform.ToString
import groovy.util.logging.Slf4j



/**
 * @author Bruce.Faulkner
 * @author Barry.Myers
 */
@Slf4j
@ToString(includeNames=true)
class CommandInvoker {
    public static final PRESERVE_SPECIAL_CHARACTERS_QUOTE = "'"

    public static final PROCESS_LOG_MESSAGE = 'process: {}'

    /**
     * Set the showCommand value to true to log the command
     */
    boolean showCommand = true

    /**
     * When set to true, instead of using the Java VM execution facilities, write a bash
     * or CMD script and execute it.  This is for testing purposes to make sure
     * the bash or CMD command actually works.
     */
    boolean useScript = false

    /**
     * When curl is run from the JVM native process, multiple spaces in the request body
     * are converted to one space.  So, by default, curl will not be run from the JVM
     * directly.
     */
    boolean execCurlFromJVM = false

    /**
     * May be injected via a constructor named argument
     */
    SystemPropertyProvider systemProperties

	/**
	 * May be injected via a constructor named argument
	 */
	SystemInformationProvider systemInformationProvider

    CommandResults execute(ExecutableCommand systemCommand) {
        def consoleCommandString = systemCommand.consoleString

        if (showCommand) {
            // Note: The command is intentionally logged at the error level because want this
            //       message to always display
            log.error consoleCommandString
        }

        def executableCommand = makeCommand(systemCommand).command

        String requestBody = null
        if (useScript) {
            /*
             * useScript is set to true during some testing to make sure the
             * bash command (consoleCommandString) or the CMD command
             * can actually be used.  The command string is written to a script, and
             * the command needed to execute the script is returned.
             */
            if (isLinux()) {
                executableCommand = writeShellScript(consoleCommandString)
            } else {
                throw new RuntimeException("not supported yet")
			}
        } else {
            executableCommand = prepareCommandListForJavaExecution(executableCommand)
        }

        log.debug "actual executableCommand: <<${executableCommand}>>"

        Process process = ProcessGroovyMethods.execute executableCommand

        log.debug PROCESS_LOG_MESSAGE, process

        makeCommandResults process
    }


    private Map makeCommand(command) {
        List executionCommandList = command.commandList

        def retMap = makeNormalizedCommandList executionCommandList
        def normalizedCommandList = retMap.list
        def canUseList = retMap.canUseList

        def retCommand = canUseList ? normalizedCommandList : normalizedCommandList.join(' ')

        [
            command : retCommand,
            commandList : normalizedCommandList,
        ]
    }


    private CommandResults makeCommandResults(Process process) {
        StringBuffer stdOut = new StringBuffer()
        StringBuffer stdErr = new StringBuffer()

        process.waitForProcessOutput stdOut, stdErr

        CommandResults results =
                new CommandResults(output: stdOut, error: stdErr, exitValue: process.exitValue())

        log.debug  results.commandResultsString

        results
    }


    CommandResults pipeTo(ExecutableCommand producer, ExecutableCommand consumer) {
        def consoleProducerCommandString = producer.consoleString
        def producerCommandMap = makeCommand producer
		def producerCommand = producerCommandMap.command
		def producerCommandList = producerCommandMap.commandList

        def consoleConsumerCommandString = consumer.consoleString
        def consumerCommandMap = makeCommand consumer
		def consumerCommand = consumerCommandMap.command
		def consumerCommandList = consumerCommandMap.commandList


        // last parameter of the echo is the request body.
        String requestBody = producerCommandList[-1]
        def windowsCommandList = modifyCommandForWindows(consumerCommandList, requestBody)
		def windowsCommandString = windowsCommandList.join ' '

        def consoleCommandString = "$consoleProducerCommandString | $consoleConsumerCommandString" as String
        if (showCommand) {
            // Note: The command is intentionally logged at the error level because want this
            //       message to always display
            log.error consoleCommandString

            log.debug '*** actual (native OS) command to be executed: {} | {}', producerCommand, consumerCommand
        }

        if (! isLinux() && ! isLinuxLike()) {
            /*
             * Windows non-cygwin environment.  The echo that is available in a CMD script is not an
             * executable, so it can't be used (message: Cannot run program "echo").
             * So, replace the @- parameter with the actual data and don't use the echo.
             *
             * However, we don't want to change the consoleProducerCommandString because we
             * still want the bash command to be printed.
             */

            /*
             * Use the list instead of the string.  This keeps us from needing to use
             * quotes.
             */
            consumerCommand = windowsCommandList

            // Set the echo (the producer) to null so it won't be used.
            producerCommand = null
        }
        if (showCommand) {
            if (producerCommand) {
                log.debug '*** actual (native OS) command to be executed: {} | {}', producerCommand, consumerCommand
            } else {
                log.debug '*** actual (native OS) command to be executed: {}', consumerCommand
            }
        }

        if (useScript) {
            /*
             * useScript is set to true during some testing to make sure the
             * bash command (consoleCommandString) or the CMD command
             * can actually be used.  On Linux, the consoleCommandString is written to a file, and
             * the command needed to execute the script is returned.
             */
            consumerCommand = writeShellScript(isLinux() ? consoleCommandString : windowsCommandString)
        } else {
            if (execCurlFromJVM) {
                consumerCommand = prepareCommandListForJavaExecution(consumerCommand)
            } else {
                return null
            }
        }

        Process producerProcess = producerCommand ? producerCommand.execute() : null
        Process consumerProcess = consumerCommand.execute()

        Process process = producerProcess ?
                          producerProcess | consumerProcess :
                          consumerProcess

        log.debug PROCESS_LOG_MESSAGE, process

        makeCommandResults process
    }


    /**
     * Normalizes a list of commands that represent an OS-specific invocation with parameters into
     * a list without shell-related quotation marks, as appropriate.
     *
     * The return value of this method depends on the underlying operating system. For Windows,
     * the method returns the list unmodified. For Linux, the method examines each element in the
     * list. If an individual element in the list starts with or ends with single quotation mark,
     * which preserves special characters in the bash shell on Linux, then strip the single quotation
     * marks from the element so that the full command list can be executed properly by a native process,
     * which has no operating system shell, that a JVM Process object starts.
     *
     * @param commandList a list of the individual elements making up an operating system command to
     * be invoked.
     * @return on Windows, the unmodified list; on Linux, a list where no elements have leading or
     * trailing single quotation marks
     */
    Map makeNormalizedCommandList(List commandList) {
        def normalizedCommandList = commandList

        def canUseList = false

        if (isLinux()) {
            canUseList = true
            // If the command starts with or ends with single quotation mark,
            // which preserves special characters in the bash shell, then
            // strip the single quotation marks from the command string so that
            // the full command can be executed properly by the native process
            // (with no shell) started by a JVM Process object.
            normalizedCommandList = commandList.collect { String command ->
                if (command.startsWith(PRESERVE_SPECIAL_CHARACTERS_QUOTE)) {
                    command = command[1..-1]
                }

                if (command.endsWith(PRESERVE_SPECIAL_CHARACTERS_QUOTE)) {
                    command = command[0..-2]
                }

                command
            }
        } else {
            /*
             * Do largely the same as with linux for the non-cygwin Windows case.
             * Single quotes around the endpoint mess up non-cygwin curl.
             * It interprets the protocol as "'http" instead of just "http".
             *
             * Note: care must be taken here to call isLinuxLike() only if there
             * are quotes.  Otherwise, isLinuxLike() may create a SystemInformationProvider
             * which will cause another CommandInvoker to be created to process
             * a "which which" command (to see if "which" is on the PATH).  This will
             * cause us to reach this point again.  Since there are no quotes around
             * "which", isLinuxLike() will not be called again and there will be
             * no circular logic or stack overflow.
             */
            normalizedCommandList = commandList.collect { String command ->
                if (command.startsWith(PRESERVE_SPECIAL_CHARACTERS_QUOTE) &&
                    command.endsWith(PRESERVE_SPECIAL_CHARACTERS_QUOTE)) {
                    if (isLinuxLike()) {
                        // passing a list to the JVM Process with single quotes will not work
                        canUseList = false
                    } else {
                        command = command[1..-2]
                        canUseList = true
                    }
                }
                command
            }
        }

        [
            list:normalizedCommandList,
            canUseList:canUseList,
        ]
    }

    /**
     * In the non-cygwin Windows case, the Java execution facilities will
     * handle the quoting, so we need to remove the quoting that was done
     * to create the bash command that is logged.
     *
     * @param requestBody
     * @return the request body with quoting removed
     */
    private undoQuoting(requestBody) {
        // the single quotes in the data should not be escaped
        requestBody = requestBody.replaceAll(JSON_ESCAPED_QUOTE, JSON_SINGLE_QUOTE)

        while (wrappedWithQuotes(requestBody)) {
            // remove first and last
            requestBody = requestBody[1..-2]
        }
        requestBody
    }

    private wrappedWithQuotes(str) {
        if (str.size() < 2) return false
        WRAPPER_CHARACTERS.any { quote ->
            str.startsWith(quote) && str.endsWith(quote)
        }
    }

    /**
     * The command list will be sent to the Java execution facilities to create
     * a Process.  Several changes to the parameters need to be done:
     *
     * - The quoting that was done to create a valid bash command
     * needs to be removed.
     * - The @- needs to be replaced with the request body data.  Any double-quotes in
     * the data is escaped with a backslash.
     * - The --data-binary option is changed to --data-raw.  It just seems to
     * be the better option to use.
     * - Any parameter that has spaces in it such as --max-time 30 needs each
     * token to be put in the list separately.
     *
     * @param command the command to be modified
     * @param requestBody the request body
     * @return a command that is modified for the Windows non-cygwin case.
     */
    private modifyCommandForWindows(command, requestBody) {
        if (requestBody != null) {
            requestBody = undoQuoting(requestBody)
        }

        command = command.collect { parm ->
            if (parm == DATA_BINARY_OPTION) {
                // it seems better to use this option instead.
                parm = '--data-raw'
            }
            // replace @- with body data
            if (parm == READ_STDIN_OPTION) {
                parm = requestBody
            }

                // Note: it is important that the ^ comes first since you are also inserting ^'s here.
                // Also, do this before putting backslashes before quotes.
//                ['^', '\\', '&', '|', '>', '<'].each { c ->
//                    parm = parm.replace(c, "^" + c)
//                }
//                // escape any quotes in the data
            if (parm.contains(ASCII_DOUBLE_QUOTE)) {
                // escape with a backslash.
                parm = parm.replace(ASCII_DOUBLE_QUOTE, '\\' + ASCII_DOUBLE_QUOTE)
            }
//                if (parm.contains('%')) {
//                    parm = parm.replace('%', '%%')
//                }


            parm
        }

        command
    }

    private prepareCommandListForJavaExecution(command) {
        if (! (command instanceof List)) {
            return command
        }
        command = command.collect { parm ->
            if (parm.startsWith("--") && parm.contains(" ")) {
                /*
                 * Since we are passing a list to the Java execution facilities, we need
                 * to change parameters like "--max-time 30" so each token is a
                 * separate value in the list.  This is why the flatten() is done
                 * at the end of this method.
                 */
                parm = parm.split(' ')
            }
            parm
        }
        command = command.flatten()
        command
    }

    private boolean isLinux() {
        if (!systemProperties) {
            systemProperties = new SystemPropertyProvider()
        }

        systemProperties.linux
    }

    private isLinuxLike() {
        if (! execCurlFromJVM) {
            return false // we don't want to use cygwin curl
        }
        if (systemInformationProvider == null) {
            systemInformationProvider = new SystemInformationProvider()
        }
        systemInformationProvider.isLinuxLike()
    }

    private String writeShellScript(consoleCommandString) {
        def scriptFile = File.createTempFile("safsrestScript", isLinux() ? ".sh" : ".cmd")
        scriptFile.deleteOnExit()

        scriptFile.write(consoleCommandString)

        isLinux() ? "bash ${scriptFile.absolutePath}" : "cmd /C ${scriptFile.absolutePath}"
    }
}
