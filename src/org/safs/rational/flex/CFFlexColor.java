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
package org.safs.rational.flex;

import org.safs.rational.CFComponent;
import com.rational.test.ft.object.interfaces.flex.FlexColorPickerTestObject;
import org.safs.*;


/**
 * <br><em>Purpose:</em> CFFlexColor, process a FLEX ColorPicker component 
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  JunwuMa
 * @since   APR 24, 2009
 *   
 **/
public class CFFlexColor extends CFComponent {
	public static final String SETCOLOR = "SetColor";	

	public CFFlexColor() {
	    super();
	}
	protected void localProcess() {
		// then we have to process for specific items not covered by our super
		Log.info(getClass().getName()+".process, searching specific tests...");
		if (action != null) {
			Log.info("....."+getClass().getName()+".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
		    if (action.equalsIgnoreCase(SETCOLOR)){
		        String param = (String) params.iterator().next();
				Log.info("...param: " + param);
		    	try {
		    		FlexColorPickerTestObject colorObj = new FlexColorPickerTestObject(obj1.getObjectReference());
		    		colorObj.open();
		    		colorObj.change(param);
				    // set status to ok
				    String altText = windowName+":"+compName+" "+action +" successful using "+ param;
				    log.logMessage(testRecordData.getFac(),
				                     passedText.convert("success3a", altText, 
				                                        windowName, compName, action, param),
				                     PASSED_MESSAGE);
				    testRecordData.setStatusCode(StatusCodes.OK);		    		
		    	} catch (Exception ex) {
		              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		              String detail = failedText.convert("failure2", "Unable to perform "+ action +" on "+ compName, action, compName);
		              componentFailureMessage(detail+": "+ex.getMessage());		    		
		    	}
		    }
		} 
	}
}
