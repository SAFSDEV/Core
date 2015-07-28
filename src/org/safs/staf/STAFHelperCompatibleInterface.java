/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
 * @author SBJLWA
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
