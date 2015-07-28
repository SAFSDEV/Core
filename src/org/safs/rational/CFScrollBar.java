/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.Point;
import java.awt.Rectangle;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.text.FAILStrings;

import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.object.interfaces.ScrollTestObject;

/**
 * <br><em>Purpose:</em> CFScrollBar, process a ScrollBar component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   OCT 10, 2003
 *
 *   <br>   OCT 10, 2003    (DBauman) 	Original Release
 *   <br>   NOV 08, 2003    (CANAGL) 	Removed GetText SAFSException
 *   <br>	AUG 12, 2008	(LeiWang)	Modify to make it work for .NET application.
 *   <br>   APR 28, 2009	(JunwuMa)   Updated localProcess() to support java.awt.Scrollbar, trying to click on 
 *                                      the scrollbar at its relative point if click(Subitem) throws an exception. 
 **/
public class CFScrollBar extends CFComponent {

  public static final String ONEDOWN                        = "OneDown";
  public static final String ONELEFT                        = "OneLeft";
  public static final String ONERIGHT                       = "OneRight";
  public static final String ONEUP                          = "OneUp";
  public static final String PAGEDOWN                       = "PageDown";
  public static final String PAGELEFT                       = "PageLeft";
  public static final String PAGERIGHT                      = "PageRight";
  public static final String PAGEUP                         = "PageUp";

  /** Since the RobotJ api does not provide a 'relative' page
   ** mechanism, then it is necessary to click the up/down/right/left button
   ** a number of times to simulate a page movement.  All the do provide
   ** is an 'absolute' positiononing mechanism, which won't work for us
   ** since we are stateless.
   **/
  public static final int PAGECLICKS                        = 7;

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFScrollBar () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>OneDown
   ** <li>OneLeft
   ** <li>OneRight
   ** <li>OneUp
   ** <li>PageDown
   ** <li>PageLeft
   ** <li>PageRight
   ** <li>PageUp 
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
	String debugmsg = getClass().getName()+".localProcess() ";
	
    try {    	
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(), 
                     getClass().getName()+".process, searching specific tests...",
                     DEBUG_MESSAGE);
      if (action != null) {
        Log.info(".....CFScrollBar.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(ONEDOWN) ||
            action.equalsIgnoreCase(ONELEFT) ||
            action.equalsIgnoreCase(ONERIGHT) ||
            action.equalsIgnoreCase(ONEUP) ||
            action.equalsIgnoreCase(PAGEDOWN) ||
            action.equalsIgnoreCase(PAGELEFT) ||
            action.equalsIgnoreCase(PAGERIGHT) ||
            action.equalsIgnoreCase(PAGEUP)) {
          int num=1;
          if (params.size() >= 1) {
            String param = (String) params.iterator().next();
            Log.info("..... param: "+param);
            try {
              Integer ni = new Integer(param);
              num = ni.intValue();
              if (num < 1) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                               getClass().getName()+": number should be >= 1: "+param,
                               FAILED_MESSAGE);
                return;
              }
            } catch (NumberFormatException nfe) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                             getClass().getName()+": bad number: "+param,
                             FAILED_MESSAGE);
              return;
            }
          }
          GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
          //For System.Windows.Forms.ScrollBar ( VScrollBar and HScrollBar)
          //GuiSubitemTestObject.click(RationalTestScriptConstants.SCROLL_DOWNBUTTON) can be performed,
          //but no visibel effect on the scrollbar.
          //So we use function GuiTestObject.click(Point) to perform a click on the scrollButton; 
          //We need to find the relative coordination of the four scrollButton within the scrollBar.
          //I hope this is a tempraire solution and RFT will support click(RationalTestScriptConstants.SCROLL_DOWNBUTTON)
          //In fact, click(Point) is more general and can work for any application.
          
          Rectangle scrollBarRectangle = guiObj.getScreenRectangle();
          Log.info(debugmsg+" Rectangle of ScrollBar is "+scrollBarRectangle);
          boolean isDotnetApp = isDotnetDomain(guiObj);
          Point relativePoint = null;
          Point leftOrUpButtonPoint = new Point(2,2);
          Point rightOrDownButtonPoint = new Point(scrollBarRectangle.width-2,scrollBarRectangle.height-2);
          Log.info(debugmsg+" leftOrUpButtonPoint is "+leftOrUpButtonPoint+"; rightOrDownButtonPoint is "+rightOrDownButtonPoint);
          Subitem subitem = null;
          
          if (action.equalsIgnoreCase(ONEDOWN)) {
        	  relativePoint = rightOrDownButtonPoint;
        	  subitem = Script.SCROLL_DOWNBUTTON;
          } else if (action.equalsIgnoreCase(ONELEFT)) {
        	  relativePoint = leftOrUpButtonPoint;
        	  subitem = Script.SCROLL_LEFTBUTTON;
          } else if (action.equalsIgnoreCase(ONERIGHT)) {
        	  relativePoint = rightOrDownButtonPoint;
        	  subitem = Script.SCROLL_RIGHTBUTTON;
          } else if (action.equalsIgnoreCase(ONEUP)) {
        	  relativePoint = leftOrUpButtonPoint;
        	  subitem = Script.SCROLL_UPBUTTON;
          } else if (action.equalsIgnoreCase(PAGEDOWN)) {
        	  relativePoint = rightOrDownButtonPoint;
        	  subitem = Script.SCROLL_DOWNBUTTON;
        	  num = num * PAGECLICKS;
          } else if (action.equalsIgnoreCase(PAGELEFT)) {
        	  relativePoint = leftOrUpButtonPoint;
        	  subitem = Script.SCROLL_LEFTBUTTON;
        	  num = num * PAGECLICKS;
          } else if (action.equalsIgnoreCase(PAGERIGHT)) {
        	  relativePoint = rightOrDownButtonPoint;
        	  subitem = Script.SCROLL_RIGHTBUTTON;
        	  num = num * PAGECLICKS;
          } else if (action.equalsIgnoreCase(PAGEUP)) {
        	  relativePoint = leftOrUpButtonPoint;
        	  subitem = Script.SCROLL_UPBUTTON;
        	  num = num * PAGECLICKS;
          }else{
        	  String msg = failedText.convert(FAILStrings.SUPPORT_NOT_FOUND, "Support for"+action+"not found!", action);
        	  Log.debug(debugmsg+" relativePoint or subitem is null. "+msg);
        	  componentFailureMessage(msg);
        	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        	  return; 
          }

	      for(int i=0; i<num; i++){
	    	  if(isDotnetApp){
          			guiObj.click(relativePoint);
	    	  }else{
          			try{
          				guiObj.click(subitem);
          			}catch(Exception e){
          				// try to click on the guiObj at relativePoint
          				guiObj.click(relativePoint);        			
          			}
	    	  }
          } // end of while
          
          // set status to ok
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(TXT_SUCCESS_3, 
                                            action+" complete on "+compName+" in "+windowName, 
                                            windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        }
      }
    } catch (WrappedException we) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "Comp: "+compName+", action: "+action+
                     ", Cannot find component, perhaps it is not visible or does not exist",
                     WARNING_MESSAGE);
    } catch (java.lang.NullPointerException npe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "Comp: "+compName+", Unsupported action: "+action,
                     FAILED_MESSAGE);
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
    } 
  }
}
