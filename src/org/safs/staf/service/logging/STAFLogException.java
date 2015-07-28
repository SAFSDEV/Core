package org.safs.staf.service.logging;

import com.ibm.staf.*;

import org.safs.logging.*;

/**
 * This exception is thrown when a STAF-related logging error happens. This is
 * mainly used by STAF-specific implementation of log facility, such as 
 * <code>SLSLogFacility</code>, to conform to (<code>close</code>) method 
 * declaration of <code>AbstractLogFacility</code>. It has a public 
 * <code>STAFResult</code> field to store STAF result.
 */
public class STAFLogException extends LogException
{
	/**
	 * <code>STAFResult</code> containing STAF related information for this 
	 * exception.
	 */
	public STAFResult result;

	public STAFLogException(String s, STAFResult r)
	{
		super(s);
		result = r;
	}
}