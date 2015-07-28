/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.*;

import org.safs.*;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

/**
 * <br><em>Purpose:</em> CFHtmlLink, process html link components (like Html.A).
 * See the file ObjectTypesMap.dat for cross reference as to which map to us.
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   Sep 26, 2003
 *
 *   <br>   Sep 26, 2003    (DBauman) Original Release
 **/
public class CFHtmlLink extends CFComponent {

  public static final String CLICKLINKBEGINNING           = "ClickLinkBeginning";
  public static final String CLICKLINKCONTAINING          = "ClickLinkContaining";

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFHtmlLink () {
    super();
  }

  /** <br><em>Purpose:</em> overrides parent: in our local version we do not get 'obj1',
   ** that is done directly in the localProcess method because it may need to search for
   ** more than one object because it doesn't know the object name just yet...
   **/
  protected void getHelpers() throws SAFSException{
    getHelpersWorker();
    script = ((RTestRecordData)testRecordData).getScript();
    // DON'T do this unless the action will go back to the parent
    if (!action.equalsIgnoreCase(CLICKLINKBEGINNING) &&
        !action.equalsIgnoreCase(CLICKLINKCONTAINING)) {
      obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName);
    }
  }
  
  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li> ClickLinkBeginning
   ** <li> ClickLinkContaining
   ** </ul><br>
   ** <br>NOTE: the 'activate' keywords didn't seem to work on the regression test
   ** because the use the guiObject.click instead of guiObject.setState method.
   ** The latter seems to work better
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
	// for now, bypass everything since CFHTMLDocument handles these 2 keywords
  	if (true) return;
    try {
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+".process, searching specific tests...",
                     DEBUG_MESSAGE);

      if (action != null) {
        Log.info("....."+getClass().getName()+".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CLICKLINKBEGINNING) ||
            action.equalsIgnoreCase(CLICKLINKCONTAINING)) {
          if (params.size() < 1) {
            paramsFailedMsg(windowName, compName);
          } else {
            Iterator iterator = params.iterator();
            String name = (String) iterator.next();
            // WE NEED TO SEARCH FOR MULTIPLE OBJECTS...
            // Using overloaded version of getTestObject which takes a list to 'gather'
            // Note: ignore cached objects
            java.util.List gather= new java.util.LinkedList();
            obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true,
                                                          gather);
            Log.info(" ..... name: "+name);
            Log.info(" ..... gather: "+gather);
            String match = null;
            for(Iterator j= gather.iterator(); j.hasNext(); ) {
              String n = (String) j.next();
              if (action.equalsIgnoreCase(CLICKLINKBEGINNING)) {
                if (n.toLowerCase().indexOf(name.toLowerCase())==0) { //found it
                  match = n;
                  break;
                }
              } else if (action.equalsIgnoreCase(CLICKLINKCONTAINING)) {
                if (n.toLowerCase().indexOf(name.toLowerCase())>=0) { //found it
                  match = n;
                  break;
                }
              }
            }
            Log.info(" ..... match: "+match);
            if (match == null) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                             "failed to match: "+name+ ", in gathered names: "+gather,
                             FAILED_MESSAGE);
              return;
            }
            // do this again, the first time just 'gather'ed the names
            // this time, another overloaded version needs the following:
            // compName: (same as before, it ends with Name=* to get all the components
            // name: the matching name
            obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true,
                                                          match);
            
            //listAllProperties(guiObj, "obj");
            String tc = (String)obj1.getProperty(".class");
            Log.debug(".class: "+tc);
            String text = (String)obj1.getProperty(".text");
            Log.debug(".text: "+text);

            // ready to do the click
            GuiTestObject guiObj = new GuiTestObject(obj1);
            guiObj.click();
            // set status to ok
            log.logMessage(testRecordData.getFac()," "+action+" ok", GENERIC_MESSAGE);
            testRecordData.setStatusCode(StatusCodes.OK);
          }
        }

        //all for now
      }
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "ObjectNotFoundException: "+onfe.getMessage(),
                     FAILED_MESSAGE);
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getClass().getName()+", msg: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }
}
