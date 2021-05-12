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
package org.safs.staf.service.logging;

import java.util.StringTokenizer;


/**
 * This helper class parses the result buffer of LIST SETTINGS request to the
 * SAFS logging service for settings of the service. Use the static
 * {@link #parseSettings parseSettings} method to parse the result buffer to a
 * <code>SLSSettings</code>:
 * <p>
 * <pre>
 * STAFResult result;
 * // submits LIST SETTINGS request to logging servcie...
 * SLSSettings setttings = SLSSettings.parseSettings(result.result);
 * </pre>
 *
 * @since	MAY 19 2009		(LW)	Use AbstractSAFSLoggingService instead of SAFSLoggingService to get contstants.
 */
public class SLSSettings
{
	/**
	 * The mode of the service as parsed from the result buffer.
	 */
	public String mode = null;
	/**
	 * The remote machine as parsed from the result buffer.
	 */
	public String remoteMachine = null;
	/**
	 * The name of the remote service as parsed from the result buffer.
	 */
	public String remoteService = null;
	/**
	 * The default log directory of the service as parsed from the result
	 * buffer.
	 */
	public String defaultDir = null;

	/**
	 * Creates a <code>SLSSettings</code>.
	 * <p>
	 * This constructor is declared private to prevent object of this class from
	 * being created explicitly. The user should use the static
	 * <code>parseSettings</code> method to get a correctly populated instance
	 * of this class.
	 * <p>
	 * @param m		the mode.
	 * @param rm	the remote machine.
	 * @param rs	the name of the remote service.
	 * @param dd	the default log directory.
	 */
	private SLSSettings(String m, String rm, String rs, String dd)
	{
		mode = m;
		remoteMachine = rm;
		remoteService = rs;
		defaultDir = dd;
	}

	/**
	 * Parses the result buffer of LIST SETTINGS request.
	 * <p>
	 * @param settings	the result buffer of LIST SETTINGS request to parse.
	 * @return			a <code>SLSSettings</code> populated with the parsed
	 * 					values.
	 */
	public static SLSSettings parseSettings(String settings)
	{
		if (settings == null) throw new IllegalArgumentException();
		String m = null;
		String rm = null;
		String rs = null;
		String dd = null;

		StringTokenizer st = new StringTokenizer(settings, "\n");
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (s.startsWith(AbstractSAFSLoggingService.SLS_SETTINGS_MODE_PREFIX))
			{
				m = s.substring(
					AbstractSAFSLoggingService.SLS_SETTINGS_MODE_PREFIX.length());
			}
			else if (s.startsWith(
				AbstractSAFSLoggingService.SLS_SETTINGS_REMOTE_MACHINE_PREFIX))
			{
				rm = s.substring(
					AbstractSAFSLoggingService.SLS_SETTINGS_REMOTE_MACHINE_PREFIX.length());
				if (rm == "null") rm = null;
			}
			else if (s.startsWith(
				AbstractSAFSLoggingService.SLS_SETTINGS_REMOTE_SERVICE_PREFIX))
			{
				rs = s.substring(
					AbstractSAFSLoggingService.SLS_SETTINGS_REMOTE_SERVICE_PREFIX.length());
				if (rs == "null") rs = null;
			}
			else if (s.startsWith(
				AbstractSAFSLoggingService.SLS_SETTINGS_DEFAULT_DIR_PREFIX))
			{
				dd = s.substring(
					AbstractSAFSLoggingService.SLS_SETTINGS_DEFAULT_DIR_PREFIX.length());
			}
		}
		return new SLSSettings(m, rm, rs, dd);
	}
}
