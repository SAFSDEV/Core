// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.version

import static org.safs.rest.service.commands.curl.CurlCommand.CURL_EXECUTABLE

import org.safs.rest.service.commands.CommandInvoker
import org.safs.rest.service.commands.CommandResults
import org.safs.rest.service.commands.ExecutableCommand


/**
 *
 * @author ***REMOVED***
 * @since August 12, 2015
 */
class CurlVersion implements VersionInterface {
    public static final NO_CURL_VERSION = "NONE"
    
    static final OUTPUT_CURL_EXECUTABLE_LINE_INDEX = 0
    static final CURL_EXECUTABLE_INDEX = 0
    static final CURL_VERSION_INDEX = 1

    static final MAJOR_INDEX = 0
    static final MINOR_INDEX = 1
    static final PATCH_INDEX = 2

    static final VERSION_OPTION = '--version'

    private final String currentVersion = null
    private final List curlVersionFields = []


    CurlVersion() {
        currentVersion = loadCurlVersion()

        curlVersionFields = currentVersion.tokenize '.'
    }


    private String loadCurlVersion() {
        def commandParameters = [
            executable: CURL_EXECUTABLE,
            options: [ VERSION_OPTION ]
        ]

        def curlCommand = new ExecutableCommand(commandParameters)

        def curlVersion
        try {
            def results = new CommandInvoker(showCommand: false).execute curlCommand

            curlVersion = parseResultsForVersion results
        } catch (IOException e) {
            // curl is not likely on the PATH.  Return NO_CURL_VERSION
            curlVersion = NO_CURL_VERSION
        }
        curlVersion
    }


    private String parseResultsForVersion(CommandResults results) {
        assert results

        def curlVersion = '-1'

        def output = results.output
        assert output

        def outputLines = output.split '\n'

        assert outputLines.size() == 3

        def curlExecutableLine = outputLines[OUTPUT_CURL_EXECUTABLE_LINE_INDEX]
        assert curlExecutableLine.startsWith(CURL_EXECUTABLE)

        def curlExecutableLineFields = curlExecutableLine.split()
        assert curlExecutableLineFields[CURL_EXECUTABLE_INDEX] == CURL_EXECUTABLE

        curlVersion = curlExecutableLineFields[CURL_VERSION_INDEX]

        curlVersion
    }



    /**
     * currentVersion is a read-only property representing the current
     * version of the curl executable found first in the path.
     *
     * @return String representing the current curl version, derived from
     * curl -- version (e.g. 7.43.0)
     */
    String getCurrentVersion() {
        currentVersion
    }


    /**
     *
     * @return the major portion of the curl version (prior to the first `.`)
     */
    Integer getMajor() {
        if (currentVersion == NO_CURL_VERSION) {
            return null
        }
        Integer.parseInt curlVersionFields[MAJOR_INDEX]
    }


    /**
     *
     * @return the minor portion of the curl version (after the first `.`
     * and before the next `.`)
     */
    Integer getMinor() {
        if (currentVersion == NO_CURL_VERSION) {
            return null
        }
        Integer.parseInt curlVersionFields[MINOR_INDEX]
    }


    /**
     *
     * @return the patch portion of the curl version (after the last `.`)
     */
    Integer getPatch() {
        if (currentVersion == NO_CURL_VERSION) {
            return null
        }
        Integer.parseInt curlVersionFields[PATCH_INDEX]
    }


}
