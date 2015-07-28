package org.safs.staf.service.logging;

import java.util.*;


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