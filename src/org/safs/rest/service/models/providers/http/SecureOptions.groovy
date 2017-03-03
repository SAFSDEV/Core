// Copyright (c) 2017 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.
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
 * @author Bruce.Faulkner@sas.com
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
