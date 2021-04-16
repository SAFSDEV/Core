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

import org.safs.logging.LogException;

import com.ibm.staf.STAFResult;

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
