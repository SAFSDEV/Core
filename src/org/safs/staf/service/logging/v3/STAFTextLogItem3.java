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
package org.safs.staf.service.logging.v3;

import java.util.HashMap;

import org.safs.staf.service.logging.AbstractSTAFTextLogItem;

import com.ibm.staf.STAFMarshallingContext;
import com.ibm.staf.STAFResult;

/**
 * This class is used to realize STAF-version related method.
 * It will be instantiated in class SAFSLoggingService and class SLSLogFacility
 * 
 * @since	MAY 19 2009		(LW)	Realize the method getSTAFLogDirectory()
 *
 * @see SAFSLoggingService3
 * @see org.safs.staf.service.logging.SLSLogFacility
 */

public class STAFTextLogItem3 extends AbstractSTAFTextLogItem {

	/**
	 * Creates a disabled <code>STAFTextLogItem3</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>), and empty parent
	 * directory.
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem3(String file) {
		super(file);
	}

	/**
	 * Creates a disabled <code>STAFTextLogItem3</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem3(String parent, String file) {
		super(parent, file);
	}
	
	/**
	 * Creates a disabled <code>STAFTextLogItem3</code> with default log level 
	 * (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_XML</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem3(String name, String parent, String file) {
		super(name, parent, file);
	}

	/**
	 * Returns the DIRECTORY setting of the STAF LOG service.
	 * <P>
	 * @return	the Directory setting as returned by LIST SETTINGS request to
	 * 			STAF LOG service.
	 */
	protected String getSTAFLogDirectory() {
		// submit LIST SETTINGS request to STAF LOG service to retrieve its
		// settings. the second line of the result buffer contains directory.
		STAFResult result = stafLogRequest("list settings");
		String directory = "";
		//In staf version 3, the result is a marshalled HashMap
		//If the result is marshalled data, need to unmarshall it.
		if (STAFMarshallingContext.isMarshalledData(result.result)) {
			STAFMarshallingContext mc = STAFMarshallingContext
					.unmarshall(result.result);
			HashMap hs = (HashMap) mc.getRootObject();
			directory = (String) hs.get("directory");
		}
		
		return directory;
	}

}
