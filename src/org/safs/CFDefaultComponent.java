/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
 *   <br>   SEP 16, 2003    (CANAGL) Implemented use of new SAFSLOGS logging.
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
