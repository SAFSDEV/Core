// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.commands

import static org.safs.rest.service.models.providers.SystemPropertyProvider.OS_LINUX

import static org.safs.rest.service.commands.curl.CurlCommand.ASCII_DOUBLE_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.DATA_BINARY_OPTION
import static org.safs.rest.service.commands.curl.CurlCommand.READ_STDIN_OPTION
import static org.safs.rest.service.commands.curl.CurlCommand.JSON_ESCAPED_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.JSON_SINGLE_QUOTE
import static org.safs.rest.service.commands.curl.CurlCommand.WRAPPER_CHARACTERS

import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.safs.rest.service.models.providers.SystemInformationProvider
import org.safs.rest.service.models.providers.SystemPropertyProvider

import org.codehaus.groovy.runtime.ProcessGroovyMethods



/**
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
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
        List executionCommandList = systemCommand.commandList

        def consoleCommandString = executionCommandList.join ' '

        if (showCommand) {
            // Note: The command is intentionally logged at the error level because want this
            //       message to always display
            log.error consoleCommandString
        }

        def retMap = makeNormalizedCommandList executionCommandList
        def normalizedCommandList = retMap.list
        def canUseList = retMap.canUseList

        def commandActuallyUsed = canUseList ? normalizedCommandList : normalizedCommandList.join(' ')

        String requestBody = null
        def windowsCommandList = modifyCommandForWindows(normalizedCommandList, requestBody)
        def windowsCommandString = windowsCommandList.join ' '
        
//        if (showCommand) {
//            // Note: The command is intentionally logged at the error level because want this
//            //       message to always display
//            log.error "Windows CMD: $windowsCommandString"
//        }

        if (useScript) {
            /*
             * useScript is set to true during some testing to make sure the
             * bash command (consoleCommandString) or the CMD command
             * can actually be used.  The command string is written to a script, and
             * the command needed to execute the script is returned.
             */
            commandActuallyUsed = writeShellScript(isLinux() ? consoleCommandString : windowsCommandString)
        } else {
            commandActuallyUsed = prepareCommandListForJavaExecution(commandActuallyUsed)
        }
        
        Process process = ProcessGroovyMethods.execute commandActuallyUsed

        log.debug PROCESS_LOG_MESSAGE, process


        StringBuffer stdOut = new StringBuffer()
        StringBuffer stdErr = new StringBuffer()

        ProcessGroovyMethods.waitForProcessOutput process, stdOut, stdErr

        CommandResults results =
            new CommandResults(output: stdOut, error: stdErr, exitValue: process.exitValue())

         log.debug  results.commandResultsString

        results
    }



    CommandResults pipeTo(ExecutableCommand producer, ExecutableCommand consumer) {
        List producerCommandList = producer.commandList
        def consoleProducerCommandString = producerCommandList.join ' '

        def retMap = makeNormalizedCommandList producerCommandList
        def normalizedProducerCommandList = retMap.list
        def producerCanUseList = retMap.canUseList

        def producerCommandActuallyUsed = producerCanUseList ? normalizedProducerCommandList : normalizedProducerCommandList.join(' ')

        List consumerCommandList = consumer.commandList
        def consoleConsumerCommandString = consumerCommandList.join ' '

        retMap = makeNormalizedCommandList consumerCommandList
        List normalizedConsumerCommandList = retMap.list
        def consumerCanUseList = retMap.canUseList

        def consumerCommandActuallyUsed = consumerCanUseList ? normalizedConsumerCommandList : normalizedConsumerCommandList.join(' ')
        
        // last parameter of the echo is the request body.
        String requestBody = normalizedProducerCommandList[-1]
        def windowsCommandList = modifyCommandForWindows(normalizedConsumerCommandList, requestBody)
        def windowsCommandString = windowsCommandList.join ' '
        
        
        def consoleCommandString = "$consoleProducerCommandString | $consoleConsumerCommandString" as String
        if (showCommand) {
            // Note: The command is intentionally logged at the error level because want this
            //       message to always display
            log.error consoleCommandString
//            log.error "Windows CMD: $windowsCommandString"
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
            consumerCommandActuallyUsed = windowsCommandList
            
            // Set the echo (the producer) to null so it won't be used.
            producerCommandActuallyUsed = null
        }

        if (showCommand) {
            if (producerCommandActuallyUsed) {
                log.debug '*** actual (native OS) command to be executed: {} | {}', producerCommandActuallyUsed, consumerCommandActuallyUsed
            } else {
                log.debug '*** actual (native OS) command to be executed: {}', consumerCommandActuallyUsed
            }
        }

        if (useScript) {
            /*
             * useScript is set to true during some testing to make sure the
             * bash command (consoleCommandString) or the CMD command
             * can actually be used.  On Linux, the consoleCommandString is written to a file, and
             * the command needed to execute the script is returned.
             */
            consumerCommandActuallyUsed = writeShellScript(isLinux() ? consoleCommandString : windowsCommandString)
        } else {
            if (execCurlFromJVM) { 
                consumerCommandActuallyUsed = prepareCommandListForJavaExecution(consumerCommandActuallyUsed)
            } else {
                return null
            }
        }
        
        Process producerProcess = producerCommandActuallyUsed ? ProcessGroovyMethods.execute(producerCommandActuallyUsed) : null
        Process consumerProcess = ProcessGroovyMethods.execute consumerCommandActuallyUsed

        Process process = producerProcess ?
                          ProcessGroovyMethods.pipeTo(producerProcess, consumerProcess) :
                          consumerProcess

        log.debug PROCESS_LOG_MESSAGE, process

        StringBuffer stdOut = new StringBuffer()
        StringBuffer stdErr = new StringBuffer()

        ProcessGroovyMethods.waitForProcessOutput process, stdOut, stdErr

        CommandResults results =
            new CommandResults(output: stdOut, error: stdErr, exitValue: process.exitValue())

        log.debug results.commandResultsString

        results
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
