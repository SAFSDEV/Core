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
package org.safs.selenium;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.jvmagent.SAFSActionErrorRuntimeException;

public class CFCheckBox extends CFComponent {
	//CheckBoxFunctions Actions
	public static final String CHECK					= "Check";
	public static final String UNCHECK					= "Uncheck";
	
	public CFCheckBox() {
		super();
	}
	
	protected void localProcess(){
		if (action != null) {
			Log.info(".....CFCheckBox.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
			if(action.equalsIgnoreCase(CHECK)){
					selenium.windowFocus();
					selenium.check(sHelper.getCompTestObject().getLocator());
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			} else if(action.equalsIgnoreCase(UNCHECK)){
					selenium.windowFocus();
					selenium.uncheck(sHelper.getCompTestObject().getLocator());
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}
			
			
			if(testRecordData.getStatusCode() == StatusCodes.NO_SCRIPT_FAILURE){
				String msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
						windowName, compName, action);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				return;
				// just in case. (normally any failure should have issued an Exception)
			}
		}
	}

}
