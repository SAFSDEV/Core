package org.safs.logging;

/**
 * This class is the base for all SAFS logging related exceptions.
 * 
 * @version 1.0, 09/11/2003
 * @author Yuesong Wang, Original Release
 * @author Carl Nagle  , SEP 12, 2003  Made subclass of SAFSException
 * 
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
public class LogException extends org.safs.SAFSException 
{
	public LogException(String s)
	{
		super(s);
	}
}