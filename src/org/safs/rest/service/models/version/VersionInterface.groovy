// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.version

/**
 * This interface provides a tangible name to use in SAFSREST source code
 * since the actual Version class will be dynamically generated by the
 * Gradle build.
 *
 * While Groovy does not strictly require an interface in this instance,
 * providing the interface will help with the clarity of the code for handling
 * the version.
 *
 * @author ***REMOVED***
 * @since August 12, 2015
 *
 */
interface VersionInterface {

    /**
     * Return the current version of SAFSREST.
     * @return String representation of the current verion of SAFSREST in the
     * form <code>0.0.7-SNAPSHOT</code>.
     */
    String getCurrentVersion()
}
