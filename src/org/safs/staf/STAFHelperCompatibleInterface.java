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
package org.safs.staf;

import java.util.Collection;
import java.util.List;

import org.safs.SAFSException;
import org.safs.STAFHelper;

/**
 * <pre>
 * This interface contains some methods need by {@link #STAFHelper}, but can't
 * be defined there due to the compatibility of STAF.
 * From STAF3, there are some new classes that don't exist for STAF2. If we use
 * them in STAFHelper, the ClassNotFoundException will be thrown with STAF2 environment.
 * 
 * </pre>
 * 
 * @author Lei Wang
 * 
 */
public interface STAFHelperCompatibleInterface {

	public static final String STAF2_CLASS_NAME = "org.safs.staf.STAFHelperCompatible2";
	public static final String STAF3_CLASS_NAME = "org.safs.staf.STAFHelperCompatible3";
	/**
	 * This MUST be called to set the STAFHelper before calling other methods.
	 * @param staf
	 */
	public void setSTAFHelper(STAFHelper staf);
	
	/**
	 * According to the service name, we try to get all events related to it.<br>
     * @param servicename	String, the service we want to get events for
     * @return List of events related to the service, each list-item contains
     *         the event's name, state, number of waiters. The format is not
     *         the same for STAF2 and STAF3
     *         
	 * @throws SAFSException
	 */
	public List<String> getServiceEvents(String machine, String servicename) throws SAFSException;

    /** 
     * Get collection of running Engine names.
     * We parse the STAF command: staf local SEM list EVENT<br>
     * We deduce the number of unique &lt;engine>Ready or &lt;engine>Running events.
     * We return the collection of unique engine names.
     * @return Collection (Vector) of String engine names( ex: SAFS/RobotJ, SAFS/SELENIUM, etc..)
     * @throws SAFSException
     **/
	public Collection getRunningEngineNames (String machine) throws SAFSException;
}
