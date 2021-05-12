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
package org.safs;

import java.util.*;

/**
 * <br><em>Purpose:</em> CFDefaultComponent, process a default generic component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 13, 2003
 *
 *   <br>   JUN 13, 2003    (DBauman) Original Release
 *   <br>   SEP 16, 2003    (Carl Nagle) Implemented use of new SAFSLOGS logging.
 **/
public class CFDefaultComponent extends ComponentFunction {

  public static final String CLICK 						= "Click";

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFDefaultComponent () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is the default generic version which does nothing.
   ** This is just for testing/demonstration and should not be used.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>click
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
  	
      // assume this for now..
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      // get the action
      if (action != null) {
        Log.info(".....CFDefaultComponent.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CLICK)) {
          // ready to do the click
          Log.info("...READY TO DO THE CLICK, BUT NOT DOING ANYTHING SINCE WE ARE A TEST!");
          //GuiTestObject guiObj = new GuiTestObject((SpyMappedTestObject)obj);
          //guiObj.click();
          
          // set status to ok
          testRecordData.setStatusCode(StatusCodes.OK);
        }
      }
      log.logMessage(testRecordData.getFac(), getClass().getName()+".process: "+testRecordData, 
                     DEBUG_MESSAGE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+".process: params:"+params, 
                     DEBUG_MESSAGE);
  }
}
