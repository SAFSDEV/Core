// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.models.providers


/**
 * This class provides a central location to retrieve SAFSREST properties.
 * The SAFSREST properties can be true system properties, environment variables,
 * or system properties specified by the -D option when invoking the JVM.
 *
 * @author ***REMOVED***
 * @since September 8, 2015
 */
class SystemPropertyProvider {
    public static final String OS_PROPERTY_NAME = 'os.name'
    public static final String OS_LINUX = 'linux'
    public static final String OS_WINDOWS = 'windows'


    String getOperatingSystem() {
        String operatingSystem = OS_LINUX

        def systemOsName = System.properties[OS_PROPERTY_NAME]?.trim()?.toLowerCase()

        if (systemOsName.startsWith(OS_WINDOWS)) {
            operatingSystem = OS_WINDOWS
        }

        operatingSystem
    }


    boolean isLinux() {
        operatingSystem == OS_LINUX
    }
    
    public boolean isWindows() {
        operatingSystem == OS_WINDOWS
    }
}
