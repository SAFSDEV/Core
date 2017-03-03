// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.providers

import java.text.MessageFormat

import org.safs.rest.service.commands.CommandInvoker
import org.safs.rest.service.commands.CommandResults
import org.safs.rest.service.commands.ExecutableCommand



/**
 *
 * @author Bruce.Faulkner@sas.com
 * @author Barry.Myers@sas.com
 */
class SystemInformationProvider {
    public static final String UNAME_EXECUTABLE = 'uname'
    public static final String ALL_OPTION = '--all'

    public static final String SYSTEMINFO_EXECUTABLE = 'systeminfo.exe'
    
    public static final String WHICH_EXECUTABLE = 'which'

    public static final String EXECUTABLE_NOT_FOUND_MESSAGE = [
        "WARNING: SAFSREST could not find the **{0}** executable.",
        'Please run SAFSREST tests in a Cygwin (on Windows) or a true Linux environment.',
        "Make sure that **{0}** is on the system path."
    ].join(' ')

    public static final STANDARD_UNAME_PARAMETERS = [
        executable: UNAME_EXECUTABLE,
        options: [ ALL_OPTION ]
    ]

    public final ExecutableCommand unameCommand = new ExecutableCommand(STANDARD_UNAME_PARAMETERS)
    public final ExecutableCommand systeminfoCommand = new ExecutableCommand(executable:SYSTEMINFO_EXECUTABLE)
    public final CommandInvoker commandInvoker = new CommandInvoker(showCommand: false, systemInformationProvider:this)

    private izLinuxLike = null
    
    String getSystemInformation() {
        String systemInformation = MessageFormat.format EXECUTABLE_NOT_FOUND_MESSAGE, WHICH_EXECUTABLE
        boolean isWhichFound = hasWhich()

        if (isWhichFound) {
            boolean isUnameFound = hasUname()

            if (isUnameFound) {
                CommandResults results = commandInvoker.execute unameCommand

                if (results.exitValue == 0) {
                    systemInformation = results.output
                } else {
                    systemInformation = results.error
                }
            } else {
                systemInformation = MessageFormat.format EXECUTABLE_NOT_FOUND_MESSAGE, UNAME_EXECUTABLE
            }
        } else {
            // Assume non-cygwin Windows and try systeminfo command.
            try {
                CommandResults results = commandInvoker.execute systeminfoCommand
                if (results.exitValue == 0) {
                    systemInformation = parseSystemInfoOutput(results.output)
                }
            } catch (IOException e) {
                // systeminfo.exe is not likely on the PATH - ignore
            }
        }
        
        systemInformation
    }

    boolean isLinuxLike() {
        if (izLinuxLike == null) {
            izLinuxLike = hasWhich()
        }
        izLinuxLike
    }

    private boolean hasUname() {
        boolean unameFound = false

        if (hasWhich()) {
            ExecutableCommand whichCommand = new ExecutableCommand(executable: 'which', data: UNAME_EXECUTABLE)
            CommandInvoker invoker = new CommandInvoker(showCommand: false, systemInformationProvider:this)

            CommandResults results = invoker.execute whichCommand

            unameFound = (results.exitValue == 0)
        }

        unameFound
    }


    private boolean hasWhich() {
        boolean isWhichFound = false

        ExecutableCommand whichCommand = new ExecutableCommand(executable: 'which', data: 'which')
        CommandInvoker invoker = new CommandInvoker(showCommand: false, systemInformationProvider:this)

        CommandResults results = [:]
        try {
            results = invoker.execute whichCommand
            isWhichFound = (results.exitValue == 0)
        } catch (e) {
            isWhichFound = false
        }
    }

    /**
     * systeminfo outputs rows with items and colons:
     * 
     * Host Name:  myhost
     * OS Name:    Microsoft Windows 7 Enterprise
     * OS Version: 6.1.7601 Service Pack 1 Build 7601
     * etc.
     * 
     * This output is parsed a string is returned with this format:
     * 
     * "${OS Name} ${Host Name} ${OS Version} ${System Type} Windows\n"
     * 
     */
    private String parseSystemInfoOutput(String output) {
        def pattern = java.util.regex.Pattern.compile("^([^:]+):\\s*(.*)\$")
        def map = [:]
        output.eachLine { line ->
            def matcher = pattern.matcher(line)
            if (matcher.matches()) {
                def key = matcher.group(1)
                def value = matcher.group(2)
                map.put(key, value)
            }
        }
        // return uname-like output
        "${map.'OS Name'} ${map.'Host Name'} ${map.'OS Version'} ${map.'System Type'} Windows\n"
    }
}
