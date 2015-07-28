/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
