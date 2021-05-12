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
package org.safs.tools;

import java.util.Collection;

import org.safs.SAFSException;
import org.safs.TestRecordData;

/**
 * @author Carl Nagle
 */
public interface CoreInterface extends RuntimeDataInterface{

	 public String getLogName() throws SAFSException;
	 public String getMachine();
	 public String getProcessName();
	 
	 public Collection getAppMapNames() throws SAFSException;
	 
	 public void getSAFSTestRecordData(String hookid, TestRecordData data) throws SAFSException;
	 public void setSAFSTestRecordData(String hookid, TestRecordData data) throws SAFSException;
	 
	 public boolean isInitialized();
	 
	 public boolean isSAFSINPUTAvailable();
	 public boolean isSAFSLOGSAvailable();
	 public boolean isSAFSVARSAvailable();
	 public boolean isSAFSMAPSAvailable();
	 
	 public boolean isServiceAvailable(String servicename);
	 
	  /**
	   * Queries the Core communications protocol to see if a particular process name is
	   * currently registered and running.
	   * <p>
	   * For example, the SAFSVARS service process name is "SAFSVariableService".
	   * <p>
	   * For the STAF implementation of the core protocol: 
	   * the routine will do a QUERY ALL on the HANDLE service and then evaluate 
	   * if the requested tool appears anywhere in the returned list of named processes. 
	   * <p>
	   * Future core services might be something alternative to STAF.
	   * <p>
	   * Note, because we do a substring search, we can match on just the most 
	   * significant portion of the process name.
	   * <p>
	   * @param toolname -- The name of the process of interest.  Check your documentation
	   *        to find the name normally registered by the tool of interest.  
	   *        This can be just a substring of the full tool name.  It is NOT to be case-sensitive.
	   *
	   * @return  true  or false
	   *
	   * @author  Carl Nagle
	   * @since   Apr 1, 2014
	   *<br> History:
	   *<br>
	   *<br>      Apr 01, 2014    (Carl Nagle) Original Release
	   */
	 public boolean isToolAvailable(String toolname);	 
}
