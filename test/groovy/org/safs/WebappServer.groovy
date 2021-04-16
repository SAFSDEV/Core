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
package org.safs

import static org.junit.Assert.*

import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.*

import groovy.servlet.TemplateServlet


public class WebappServer {

	private static final LOCALHOST = "127.0.0.1"
	private static final ant = new AntBuilder()

	def server

	/**
	 * Starts a web server embedded in this JVM and calls the closure.
	 * The web server will be stopped when the closure exits (successfully or not).
	 *
	 * The map should have a contextName such as "DJTEInfo".
	 * It should also have a webappDir which is where the webapp content is.
	 */
	public withRunningEmbeddedServer(map, closure)
	{
		def freePorts = findFreePorts()

		startEmbeddedWebServer(map.contextName, map.webappDir, freePorts)
		def data = null
		try
		{
			data = [
						hostname:LOCALHOST,
						webappPort:freePorts.webapp,
					]

			closure.call(data)
		}
		finally
		{
			stopEmbeddedWebServer()
		}

	}

	private startEmbeddedWebServer(contextName, webappDir, freePorts)
	{
		ant.echo(message:"Starting embedded Jetty webapp server on ${freePorts.webapp}")
		server = new Server(freePorts.webapp)
		def context = new Context(server, "/$contextName", Context.SESSIONS);
		// the web server looks for files at the resourceBase
		context.resourceBase = webappDir
		/*
		 * Tell the web server to use the TemplateServlet for all urls.
		 * The Template servlet processes GSPs.
		 */
		context.addServlet(TemplateServlet, "/")
		server.start()

	}

	private stopEmbeddedWebServer(stopPort)
	{
		server.stop()
	}

	/**
	 * Find two port numbers that can be used to start a webapp server (one port for
	 * http connections; one port for a stop command).
	 */
	public findFreePorts()
	{
		def freePorts = [webapp:null, stop:null]

		def webappSocket = null
		def stopSocket = null

		try
		{
			webappSocket = new ServerSocket(0, 1, null)
			stopSocket = new ServerSocket(0, 1, null)

			freePorts.webapp = webappSocket.getLocalPort()

			freePorts.stop = stopSocket.getLocalPort()
		}
		finally
		{
			closeSocket(webappSocket)
			closeSocket(stopSocket)
		}
		return freePorts
	}

	private closeSocket(socket)
	{
		try
		{
			if (socket)
			{
				socket.close()
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err)
		}
	}

}
