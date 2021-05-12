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
package org.safs.rest.service.models.providers.http


/**
 * Represents the valid options for specifying the protocol and verification
 * options to use in the specifications.
 *
 * <p>
 *     The following options can be used:
 *     <ul>
 *         <li>
 *             <strong>NONE</strong>: Use the HTTP protocol.
 *         </li>
 *         <li>
 *             <strong>NOCERT</strong>: Use the HTTPS protocol BUT do NOT
 *             verify certificates returned by the server.
 *         </li>
 *         <li>
 *             <strong>CERT</strong>: Use the HTTPS protocol AND
 *             verify certificates returned by the server.
 *         </li>
 *     </ul>
 *
 * @author Bruce.Faulkner
 * @since 0.8.6
 */
enum SecureOptions {
    NONE(NONE_VALUE),
    NOCERT(NOCERT_VALUE),
    CERT(CERT_VALUE)

    public static final String NONE_VALUE = 'none'
    public static final String NOCERT_VALUE = 'nocert'
    public static final String CERT_VALUE = 'cert'


    String value = NONE_VALUE

    SecureOptions(String value) {
        this.value = value.toLowerCase()
    }
}
