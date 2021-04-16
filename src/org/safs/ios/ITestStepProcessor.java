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
package org.safs.ios;

import java.util.Collection;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.TestStepProcessor;

public class ITestStepProcessor extends TestStepProcessor {
	
	// Window OBT and Child IBT mixed recognition detected
	boolean isMixedUse = false;

	public ITestStepProcessor() {
		super();
	}

	/**
	 * calls super.interpretFields and then forces the retrieval of WIN and COMP recognition strings.
	 * The intention is to allows us to create the TestRecordData.js for subsequent import.
	 * @see org.safs.TestStepProcessor#interpretFields()
	 */
	@Override
	protected Collection interpretFields() throws SAFSException {
		// TODO Auto-generated method stub
		params = super.interpretFields();
		isMixedUse = testRecordData.isMixedRsUsed(); //sets winGUIID and compGUIID fields, too!
		return params;
	}

	/**
	 * Does nothing but return true at this time.
	 * @see org.safs.TestStepProcessor#setActiveWindow()
	 */
	@Override
	protected boolean setActiveWindow() throws SAFSException {
		// TODO Auto-generated method stub
		Log.debug("ITSP.setActiveWindow called but IGNORED at this time...");
		return true;
	}

	/**
	 * Does nothing but return 0 at this time.
	 * @return 0 if wait was successful.
	 * @see org.safs.TestStepProcessor#waitForObject(boolean)
	 */
	@Override
	protected int waitForObject(boolean isWindow) throws SAFSException {
		Log.debug("ITSP.waitForObject called but IGNORED at this time...");
		return 0;
	}	
}
