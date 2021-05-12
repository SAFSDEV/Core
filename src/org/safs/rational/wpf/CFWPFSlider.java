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
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.CFComponent;
import org.safs.rational.Script;

import com.rational.test.ft.object.interfaces.WPF.WpfTrackbarTestObject;

public class CFWPFSlider extends CFComponent {
	protected void localProcess() {
		//Do the special work before process in the super class CFComponent
		Log.info(getClass().getName()+ ".process, searching specific tests...");
		try {
			if (action != null) {
				Log.info("....." + getClass().getName() + ".process; ACTION: "
						+ action + "; win: " + windowName + "; comp: " + compName);
				if (action.equalsIgnoreCase(VSCROLLTO) || action.equalsIgnoreCase(HSCROLLTO)) {
					performScorll();
				}
			}
		} catch (SAFSException ex) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), "SAFSException: "
					+ ex.getClass().getName() + ", msg: " + ex.getMessage(), FAILED_MESSAGE);
		}
	}

	protected void performScorll() throws SAFSException {
		String debugMsg = getClass().getName() + ".performScorll() ";
		int position = 0;
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
			this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		} else {
			try {
				position = (int) Float.parseFloat(params.iterator().next().toString().trim());
			} catch (NumberFormatException nfe) {
				Log.error(debugMsg + nfe.getMessage());
				this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				this.componentFailureMessage(nfe.getMessage());
				return;
			}

			if (obj1 instanceof WpfTrackbarTestObject) {
				try {
					WpfTrackbarTestObject scrollObject = new WpfTrackbarTestObject(obj1.getObjectReference());
					if (action.equalsIgnoreCase(VSCROLLTO)) {
						scrollObject.drag(Script.THUMB, Script.atDPosition(position));
					} else if (action.equalsIgnoreCase(HSCROLLTO)) {
						scrollObject.drag(Script.THUMB, Script.atDPosition(position));
					}
				} catch (Exception e) {
					String msg = e.getClass().getName() + ": " + e.getMessage();
					Log.debug(debugMsg + msg);
					componentFailureMessage(msg);
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					return;
				}
			}

			this.componentSuccessMessage("");
			this.testRecordData.setStatusCode(StatusCodes.OK);
		}
	}
}
