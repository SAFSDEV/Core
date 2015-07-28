package org.safs.staf.service.logging;

import java.util.*;

import org.safs.logging.*;

/**
 * This helper class parses the result buffer of every LOGMESSAGE request to the
 * SAFS logging service for states of the log facility in question. Use the
 * static {@link #parseStates parseStates} method to parse the result buffer to
 * a <code>SLSLogFacilityStates</code>:
 * <p>
 * <pre>
 * STAFResult result;
 * // submits LOGMESSAGE request to logging servcie...
 * SLSLogFacilityStates states = SLSLogFacilityStates.parseStates(result.result);
 * </pre>
 * 
 * @since	MAY 19 2009		(LW)	Use AbstractSAFSLoggingService instead of SAFSLoggingService to get contstants.
 */
public class SLSLogFacilityStates 
{
	/**
	 * toollog state of the log facility as parsed from the result buffer.
	 */
	public boolean tool;
	/**
	 * consolelog state of the log facility as parsed from the result buffer.
	 */
	public boolean console;
	/**
	 * log level of the log facility as parsed from the result buffer.
	 */
	public int level;

	/**
	 * Creates a <code>SLSLogFacilityStates</code>.
	 * <p>
	 * This constructor is declared private to prevent object of this class from
	 * being created explicitly. The user should use the static 
	 * <code>parseStates</code> method to get a correctly populated instance of 
	 * this class.
	 * <p>
	 * @param too	the toollog state.
	 * @param con	the consolelog state.
	 * @param lvl	the log level.
	 */
	private SLSLogFacilityStates(boolean too, boolean con, int lvl)
	{
		tool = too;
		console = con;
		level = lvl;
	}

	/**
	 * Parses the result buffer of LOGMESSAGE request.
	 * <p>
	 * @param states	the result buffer of LOGMESSAGE request to parse.
	 * @return			a <code>SLSLogFacilityStates</code> populated with the
	 * 					parsed values.
	 */
	public static SLSLogFacilityStates parseStates(String states)
	{
		if (states == null) throw new IllegalArgumentException();
		boolean too = true;
		boolean con = true;
		int lvl = AbstractLogFacility.LOGLEVEL_DEBUG;

		StringTokenizer st = new StringTokenizer(states, "\n");
		while (st.hasMoreTokens()) 
		{
			String s = st.nextToken();
			if (s.startsWith(AbstractSAFSLoggingService.SLS_STATES_TOOLLOG_PREFIX)) 
			{
				too = Boolean.valueOf(s.substring(
					AbstractSAFSLoggingService.SLS_STATES_TOOLLOG_PREFIX.length())).booleanValue();
			}
			if (s.startsWith(AbstractSAFSLoggingService.SLS_STATES_CONSOLELOG_PREFIX)) 
			{
				con = Boolean.valueOf(s.substring(
					AbstractSAFSLoggingService.SLS_STATES_CONSOLELOG_PREFIX.length())).booleanValue();
			}
			else if (s.startsWith(
				AbstractSAFSLoggingService.SLS_STATES_LOGLEVEL_PREFIX)) 
			{
				lvl = Integer.parseInt(s.substring(
					AbstractSAFSLoggingService.SLS_STATES_LOGLEVEL_PREFIX.length()));
			}
		}
		return new SLSLogFacilityStates(too, con, lvl);
	}
}