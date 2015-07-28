/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
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
