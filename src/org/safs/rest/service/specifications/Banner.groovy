// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.
package org.safs.rest.service.specifications

import static org.safs.rest.service.models.providers.SafsRestPropertyProvider.SAFSREST_PASSWORD_KEY
import static org.safs.rest.service.models.providers.SafsRestPropertyProvider.SAFSREST_USERNAME_KEY
import static org.safs.rest.service.models.providers.SafsRestPropertyProvider.isValidPort

import org.safs.rest.service.models.consumers.RestConsumer
import org.safs.rest.service.models.providers.SystemInformationProvider
import org.safs.rest.service.models.version.CurlVersion
import org.safs.rest.service.models.version.VersionInterface


/**
*
* @author Bruce.Faulkner@sas.com
* @since August 12, 2015
*/
class Banner {
    public static final SAFSREST_VERSION_CLASS_NAME =
        'org.safs.rest.service.models.version.SafsrestVersion'

    // Header generated from:
    //      http://patorjk.com/software/taag/#p=display&h=0&v=0&f=Big&t=SAFSREST
    public static final HEADER = $/\
        # _____    ______    _____   _______    _____              _______ 
        #|  __ \  |  ____|  / ____| |__   __|  / ____|     /\     |__   __|
        #| |__) | | |__    | (___      | |    | |         /  \       | |   
        #|  _  /  |  __|    \___ \     | |    | |        / /\ \      | |   
        #| | \ \  | |____   ____) |    | |    | |____   / ____ \     | |   
        #|_|  \_\ |______| |_____/     |_|     \_____| /_/    \_\    |_|   /$.stripMargin('#')


    public static final LONGEST_HEADER_LINE = HEADER.readLines().max { line ->
        line.size()
    }

    public static final EYECATCHER_LINE = EYECATCHER * LONGEST_HEADER_LINE.size()

    public static final TOOLKIT_NAME =
        'Rest Entrypoint Service Testing Consumer Automation Toolkit'

    public static final COPYRIGHT_NOTICE = '(c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.'

    public static final BLANK_LINE = ''
    public static final EYECATCHER = '='
    public static final SINGLE_INDENTATION = '\t'
    public static final DOUBLE_INDENTATION = SINGLE_INDENTATION * 2
    public static final String TRIPLE_INDENTATION = SINGLE_INDENTATION * 3

    public static final COPYRIGHT_SECTION = "${SINGLE_INDENTATION}Copyright:"
    public static final ENVIRONMENT_SECTION = "${SINGLE_INDENTATION}Environment:"
    public static final STANDARD_PROPERTIES_SECTION = "${SINGLE_INDENTATION}Standard properties:"
    public static final CUSTOM_PROPERTIES_SECTION = "${SINGLE_INDENTATION}Custom properties:"

    public static final NO_PORT_SPECIFIED_MESSAGE = 'DEFAULT (No port specified in root URL.)'

    public static final READY_LINE = 'READY TO TEST!'

    /**
     *  The consumer (an instance of RestConsumer) must be injected via a
     *  named argument when constructing the Banner.
     */
    RestConsumer consumer


    String toString() {
        def banner = ''

        def safsrestVersionMessage = "${SINGLE_INDENTATION}SAFSREST version: ${safsrestVersion}"

        def bannerItems = [
            BLANK_LINE,
            HEADER,
            BLANK_LINE,
            BLANK_LINE,
            TOOLKIT_NAME,
            EYECATCHER_LINE,
            safsrestVersionMessage,
            BLANK_LINE,
        ]

        addCopyrightDetails bannerItems
        addEnvironmentDetails bannerItems
        addStandardProperties bannerItems
        addCustomProperties bannerItems
        addFooter bannerItems

        banner = bannerItems.flatten().join '\n'

        banner
    }


    String getSafsrestVersion() {
        VersionInterface safsrestVersion = loadSafsrestVersion()

        safsrestVersion.currentVersion
    }


    /**
     * Because the SafsrestVersion class gets generated automatically by the
     * Gradle build, the class must be dynamically loaded at run-time since
     * it will not be available at compile-time.
     *
     * @return a dynamically-loaded instance of a VersionInterface with the SAFSREST version
     * information
     */
    private VersionInterface loadSafsrestVersion() {
        def classLoader = this.getClass().classLoader

        Class safsrestVersionClass = classLoader.loadClass SAFSREST_VERSION_CLASS_NAME
        VersionInterface safsrestVersionInstance = safsrestVersionClass.newInstance()

        safsrestVersionInstance
    }


    private addCopyrightDetails(List bannerItems) {
        String copyrightMessage = "${DOUBLE_INDENTATION}${COPYRIGHT_NOTICE}"

        def environmentDetails = [
                COPYRIGHT_SECTION,
                copyrightMessage,
                BLANK_LINE,
        ]

        bannerItems << environmentDetails
    }


    private addEnvironmentDetails(List bannerItems) {
        def systemInformation = new SystemInformationProvider().systemInformation.trim()
        def systemInformationMessage = "${DOUBLE_INDENTATION}System: ${systemInformation}"

        def groovyVersionMessage = "${DOUBLE_INDENTATION}Groovy: ${GroovySystem.version}"
        def javaVersionMessage = "${DOUBLE_INDENTATION}Java: ${System.properties.'java.version'}"

        def curlVersion = new CurlVersion().currentVersion
        def curlVersionMessage = "${DOUBLE_INDENTATION}Curl: ${curlVersion}"

        def environmentDetails = [
            ENVIRONMENT_SECTION,
            systemInformationMessage,
            groovyVersionMessage,
            javaVersionMessage,
            curlVersionMessage,
            BLANK_LINE,
        ]

        bannerItems << environmentDetails
    }


    private void addStandardProperties(List bannerItems) {
        def rootUrlMessage = "${DOUBLE_INDENTATION}root URL (under test): ${consumer?.rootUrl}"
        def protocolMessage = "${TRIPLE_INDENTATION}protocol (under test): ${consumer?.protocol}"
        def hostMessage = "${TRIPLE_INDENTATION}host (under test): ${consumer?.host}"

        def portText = NO_PORT_SPECIFIED_MESSAGE
        def port = consumer?.port
        if (isValidPort(port)) {
            portText = port
        }
        def portMessage = "${TRIPLE_INDENTATION}port (under test): ${portText}"

        def userNameMessage = "${DOUBLE_INDENTATION}userName: ${consumer?.userName}"
        def showStandardStreamsMessage =
            "${DOUBLE_INDENTATION}showStandardStreams: ${consumer?.showStandardStreams}"
        boolean hasAuthToken = (consumer?.authToken as boolean)
        def authTokenMessage = "${DOUBLE_INDENTATION}authToken set? ${hasAuthToken}"
        def maxTimeMessage = "${DOUBLE_INDENTATION}maxTime: ${consumer.maxTime}"

        def standardProperties = [
                STANDARD_PROPERTIES_SECTION,
                rootUrlMessage,
                protocolMessage,
                hostMessage,
                portMessage,
                userNameMessage,
                showStandardStreamsMessage,
                authTokenMessage,
                maxTimeMessage,
                BLANK_LINE
        ]

        bannerItems << standardProperties
    }


    private void addCustomProperties(List bannerItems) {
        def customProperties = consumer?.safsrestProperties?.customProperties

        if (customProperties?.size() > 0) {
            bannerItems << CUSTOM_PROPERTIES_SECTION

            customProperties.sort().each { safsrestProperty ->
                if (safsrestProperty.key != SAFSREST_USERNAME_KEY &&
                    safsrestProperty.key != SAFSREST_PASSWORD_KEY) {

                    bannerItems << "${DOUBLE_INDENTATION}${safsrestProperty}"
                }
            }

            bannerItems << BLANK_LINE
        }
    }


    private void addFooter(List bannerItems) {
        bannerItems << READY_LINE
        bannerItems << EYECATCHER_LINE
        bannerItems << BLANK_LINE
    }
}
