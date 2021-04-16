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
package org.safs.rational;

import java.awt.Rectangle;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.image.ImageUtils;
import org.safs.text.FAILStrings;
import org.safs.tools.engines.TIDComponent;

import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
/**
 * 
 * <br><em>Purpose:</em> 	Process a mixed mode (OBT and IBT) test record.
 * <br><em>Note:</em>		Mixed mode means that: we use OBT-FORMAT-RS for top window and IBT-FORMAT-RS for
 * 							component. 
 * 							RFT engine will find the top window. In this class, we make a new Recognition
 * 							String in IBT-FORMAT for top window, then we transfer the test record to 
 * 							org.safs.tools.engines.TIDComponent
 * <br><em>Lifetime:</em> 	instantiated by TestStepProcessor
 * <p>
 * @author  Lei	Wang
 * @since   Apr 2, 2010
 *
 */

public class CFTIDComponent extends ComponentFunction {
	/**
	 *  In this class, We will firstly get the rectangle of the top window,
 	 *	then we instantiate a new TIDComponent, and deliver the work to it.
	 */
	protected TIDComponent tidComponent;
	protected Script script;
	protected TestObject windowObject;

	public CFTIDComponent() {
		super();
		tidComponent = new TIDComponent();
	}

	protected void getHelpers() throws SAFSException {
		getHelpersWorker();
		script = ((RTestRecordData) testRecordData).getScript();
		windowObject = ((RDDGUIUtilities) utils).getTestObject(mapname, windowName,windowName);
	}

	public void process() {
		String debugmsg = getClass().getName()+".process(): ";
	    try{
	    	Log.info(debugmsg+" transfer test record to TIDComponent.");
	    	getHelpers();
	    }catch (SAFSException ex) {
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      log.logMessage(testRecordData.getFac(),
	                     "SAFSException: "+ex.getClass().getName()+", msg: "+ex.getMessage(),
	                     FAILED_MESSAGE);
	      return;
	    }
	    
    	Rectangle windowRect = getComponentRectangle();
    	if(windowRect!=null){
    		//Create a IBT-FORMAT Recognition String for top window
    		String windowRS = ImageUtils.MOD_IMAGE_RECT+ImageUtils.MOD_EQ+
    						  windowRect.x+ImageUtils.MOD_COMMA+
    						  windowRect.y+ImageUtils.MOD_COMMA+
    						  windowRect.width+ImageUtils.MOD_COMMA+
    						  windowRect.height;
    		//Set the IBT-FORMAT-Recognition String to test record for top window
    		testRecordData.setWindowGuiId(windowRS);
    		//Transfer the test record to TIDComponent, and let it to do the work
    		tidComponent.processIndependently(testRecordData);
    	}
	}
	
	 /**
	 * <br>
	 * <em>Purpose:</em> This will Return the clipped-screen rectangle for the associated TestObject.
	 **/
	protected Rectangle getComponentRectangle(){
		GuiTestObject guiObj = new GuiTestObject(windowObject);
		Rectangle compRect = guiObj.getClippedScreenRectangle();
		String who = windowName + ":" + windowName;
		if (compRect == null) {
			this.issueErrorPerformingActionOnX(who, FAILStrings.convert(
					FAILStrings.NOT_FOUND_ON_SCREEN, who
							+ " was not found on screen", who));
			return null;
		}
		return compRect;
	}
}
