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

package org.safs.rest.service.models.providers


/**
 * This class provides a central location to retrieve SAFSREST properties.
 * The SAFSREST properties can be true system properties, environment variables,
 * or system properties specified by the -D option when invoking the JVM.
 *
 * @author Bruce.Faulkner
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
