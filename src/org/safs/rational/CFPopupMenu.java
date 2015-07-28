/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.Iterator;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;

import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;

/**
 * <br><em>Purpose:</em> CFPopupMenu, process a PopupMenu component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   AUG 20, 2003
 *
 *   <br>   AUG 20, 2003    (DBauman) Original Release
 *   <br>   MAR 10, 2004    (Carl Nagle)  Fixed problems consistently finding items.
 **/
public class CFPopupMenu extends CFMenuBar {
  public static final String SELECTINDEX                     = "SelectIndex";
	
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFPopupMenu () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFMenuBar.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>selectIndex
   ** </ul><br>
   ** The following actions are handled in CFMenuBar:
   ** <br><ul>
   ** <li>selectPopupMenuItem
   ** <li>verifyPopupMenuItem
   ** <li>verifyPopupMenuPartialMatch
   ** <li>SelectUnverifiedPopupMenuItem
   ** <li>VerifyPopupMenu
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+".process, searching specific tests...",
          DEBUG_MESSAGE);
      
      if (action != null) {
      	//TODO Find a bug:
      	//If we perform "SelectIndex" on a submenu of popup-menu,
      	//the submenu is a JMenuItem, so will come to CFMenuBar directly, not here.
        if(action.equalsIgnoreCase(SELECTINDEX)){
        	selectIndex();	
        }else{
        	Log.debug(getClass().getName()+".localProcess(): can not treate action "+action+". Try its super class.");
      		super.localProcess();
        }
      }
    } catch (com.rational.test.ft.SubitemNotFoundException snfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"SubitemNotFoundException: "+snfe.getMessage(),
          FAILED_MESSAGE);
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"ObjectNotFoundException: "+onfe.getMessage(),
          FAILED_MESSAGE);
    } catch (Exception unk) {
      unk.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          unk.getClass().getName() + ": "+ unk.getMessage(),
          FAILED_MESSAGE);
    }
  }

	public void selectIndex() throws SAFSException {
        Log.info(".....CFPopupMenu.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);

        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        Iterator piter = params.iterator();

        String altText = ", "+windowName+":"+compName+" "+action;
        String param = (String) piter.next();
        Log.info("..... param: "+param);

        int index = 0;
        try {
          Integer pi = new Integer(param);
          index = pi.intValue();
          try{ guiObj.click(Script.localAtIndex(index));}
          catch(TargetGoneException tg){
        	Log.info("...selectIndex IGNORING TargetGoneException likely resulting from intended Window closure...");  
          }
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4, param+altText, param,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } catch (NullPointerException npe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
              getClass().getName()+": for a popupmenu, apparently, robotj does not permit clicking atIndex: "+param+", use SelectPopupMenuItem keyword instead.",
              FAILED_MESSAGE);
        } catch (NumberFormatException nfe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
              getClass().getName()+": bad index: "+param,
              FAILED_MESSAGE);
        }
	}
}
