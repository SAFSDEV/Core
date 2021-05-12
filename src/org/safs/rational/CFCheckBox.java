/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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

import org.safs.rational.CFComponent;
import org.safs.StatusCodes;
import org.safs.Log;

import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.PropertyNotFoundException;

/**
 * <br><em>Purpose:</em> CFCheckBox, process a CheckBox component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 17, 2003
 *
 *   <br>   JUN 17, 2003    (DBauman) Original Release
 *   <br>   JAN 08, 2009    (JunwuMa) Add Flex support for Check and UnCheck.    
 *   <br>   JUN 23, 2010    (JunwuMa) Updated to support the classes that own property 'IsChecked' like 
 *                                    System.Windows.Controls.Primitives.ToggleButton in WPF.
 **/
public class CFCheckBox extends CFComponent {

  public static final String CHECK						= "Check";
  public static final String UNCHECK					= "UnCheck";

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFCheckBox () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>check
   ** <li>uncheck
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
	String debugMsg = getClass().getName() + ".localProcess() ";
    try {    	
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(), 
                     getClass().getName()+".process, searching specific tests...",
                     DEBUG_MESSAGE);

      if (action != null) {
        Log.info(".....CFCheckBox.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CHECK) ||
            action.equalsIgnoreCase(UNCHECK)) {
          ToggleGUITestObject guiObj = new ToggleGUITestObject(obj1);
          if (action.equalsIgnoreCase(CHECK)) {
        	guiObj.clickToState(RationalTestScript.SELECTED);
          } else if (action.equalsIgnoreCase(UNCHECK)) {
            guiObj.clickToState(RationalTestScript.NOT_SELECTED);
          }
          // set status to ok
          log.logMessage(testRecordData.getFac()," "+action+" ok", PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        }
        //all for now
      }
    } catch (com.rational.test.ft.SubitemNotFoundException snfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SubitemNotFoundException: "+snfe.getMessage(),
                     FAILED_MESSAGE);
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "ObjectNotFoundException: "+onfe.getMessage(),
                     FAILED_MESSAGE);
    } catch (com.rational.test.ft.MethodNotFoundException mnfe) { 
    	// MethodNotFoundException thrown when code above is working on a FlexCheckBox
    	Log.debug(debugMsg + mnfe.getMessage());
    	try {
    		Log.info(debugMsg + "trying to treat the target object as FlexCheckBox");
        	// get the current status
    		String selected;
      	  	boolean isChecked;
      	  	try {
        		selected = (String) obj1.getProperty("selected");
          	  	isChecked = Boolean.parseBoolean(selected);
      	  	}catch (PropertyNotFoundException ex){
        		Log.debug(debugMsg + "no property 'selected'; PropertyNotFoundException thrown");
        		Log.debug(debugMsg + "trying property 'IsChecked' owned by the classes like System.Windows.Controls.Primitives.ToggleButton in WPF");
        		selected = obj1.getProperty("IsChecked").toString();
          	  	isChecked = Boolean.parseBoolean(selected);
      	  	}
      	  	
      	  	if (action.equalsIgnoreCase(CHECK) && !isChecked) 
      	  		((GuiTestObject)obj1).click();
      	  	else if (action.equalsIgnoreCase(UNCHECK) && isChecked)
      	  		((GuiTestObject)obj1).click();
            // set status to ok
   	        String altText = windowName + ":" + compName + " " + action + " successful.";
   	        testRecordData.setStatusCode(StatusCodes.OK);
   	        log.logMessage(testRecordData.getFac(),
   		                 genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action),
   		                 PASSED_MESSAGE);                
        } catch (Exception ex){
        	Log.debug(debugMsg + ex.getMessage());
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                           "Exception: " + ex.getMessage(),
                           FAILED_MESSAGE);        	
        }
    }
  }
}
