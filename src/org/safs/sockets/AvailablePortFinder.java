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
package org.safs.sockets;

/**
 *  Source from apache <a href="http://mina.apache.org/">Mina</a> project.
 *
 */

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Finds currently available server ports.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * @see <a href="http://www.iana.org/assignments/port-numbers">IANA.org</a>
 */
public class AvailablePortFinder {
    /**
     * The minimum number of server port number.
     */
    public static final int MIN_PORT_NUMBER = 1;

    /**
     * The maximum number of server port number.
     */
    public static final int MAX_PORT_NUMBER = 49151;

    /**
     * Creates a new instance.
     */
    private AvailablePortFinder() {
        // Do nothing
    }

    /**
     * Returns the {@link Set} of currently available port numbers
     * ({@link Integer}).  This method is identical to
     * <code>getAvailablePorts(MIN_PORT_NUMBER, MAX_PORT_NUMBER)</code>.
     *
     * WARNING: this can take a very long time.
     */
    public static Set<Integer> getAvailablePorts() {
        return getAvailablePorts(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
    }

    /**
     * Gets the next available port starting at the lowest port number.
     *
     * @throws NoSuchElementException if there are no ports available
     */
    public static int getNextAvailable() {
        return getNextAvailable(MIN_PORT_NUMBER);
    }

    /**
     * Gets the next available port starting at a port.
     *
     * @param fromPort the port to scan for availability
     * @throws NoSuchElementException if there are no ports available
     */
    public static int getNextAvailable(int fromPort) {
        if (fromPort < MIN_PORT_NUMBER || fromPort > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: "
                    + fromPort);
        }

        for (int i = fromPort; i <= MAX_PORT_NUMBER; i++) {
            if (available(i)) {
                return i;
            }
        }

        throw new NoSuchElementException("Could not find an available port "
                + "above " + fromPort);
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    /**
     * Returns the {@link Set} of currently avaliable port numbers ({@link Integer})
     * between the specified port range.
     *
     * @throws IllegalArgumentException if port range is not between
     * {@link #MIN_PORT_NUMBER} and {@link #MAX_PORT_NUMBER} or
     * <code>fromPort</code> if greater than <code>toPort</code>.
     */
    public static Set<Integer> getAvailablePorts(int fromPort, int toPort) {
        if (fromPort < MIN_PORT_NUMBER || toPort > MAX_PORT_NUMBER
                || fromPort > toPort) {
            throw new IllegalArgumentException("Invalid port range: "
                    + fromPort + " ~ " + toPort);
        }

        Set<Integer> result = new TreeSet<Integer>();

        for (int i = fromPort; i <= toPort; i++) {
            ServerSocket s = null;

            try {
                s = new ServerSocket(i);
                result.add(new Integer(i));
            } catch (IOException e) {
                // Do nothing
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        /* should not be thrown */
                    }
                }
            }
        }

        return result;
    }
}

