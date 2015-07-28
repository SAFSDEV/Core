/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexListTestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.FlexScrollDetails;
import com.rational.test.ft.script.FlexScrollDirections;
import com.rational.test.ft.script.MouseModifiers;
import com.rational.test.ft.vp.ITestData;
import com.rational.test.ft.vp.ITestDataElement;
import com.rational.test.ft.vp.ITestDataElementList;
import com.rational.test.ft.vp.ITestDataList;

/**
 * <br><em>Purpose:</em> CFList, process a List component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUL 09, 2003
 *
 *   <br>   JUL 09, 2003    (DBauman) Original Release
 * 	 <br>	JAN 14, 2004	(BNat)	  fix for the ActivatePartialMatch keyword.
 * 	 <br>	JUN 29, 2004    (BNat)	  ClickTextItem new keyword added.
 * 	 <br>	NOV 09, 2004    (CANAGL)  SelectAnother and VerifyUnSelected added.
 *   <br>   JUN 03, 2005    (Jeremy_J_Smith)  VerifySelected use of String as well as TestObject.
 *   <br>   AUG 18, 2005    (Jeremy_J_Smith)  RightClickTextItem added.
 *   <br>   Nov 22, 2005    (RFiroz)          ExtendSelectionToTextItem added.
 *   <br>   MAR 25, 2008    (JunwuMa) Added   VerifyListContains,VerifyListContainsPartialMatch,VerifyListDoesNotContain and VerifyListContainsPartialMatch.
 *   <br>   JUN 27, 2008	(LeiWang) Add mehtod getListContents() to support the .NET application
 *   <br>   JUL 11, 2008	(LeiWang) Modify mehtod clickTextItem(),verifySelected() to support the .NET application.
 *   								  Modify method selectIndex() for an index bug.
 *   <br>	JUL 22, 2008	(LeiWang) Modify method verifySelected(). Original version only take one selected item to compare.
 *   								  Now we will compare with all selected items. Work for awt.List, swing.JList, Forms.ListBox, Forms.ListView and their subclass.  
 *									  Modify method verifyItem(), .NET List has "Text" property instead of "text"
 *   <br>   SEP 03, 2008    (JunwuMa)  Added SetListContains.
 *   <br>	SEP 11, 2008	(LeiWang) Add method clickTextItem2() to replace clickTextItem(). The old one try to find the rectangle
 *   								  of the item within the list, this will call application specific method to do that; The new one
 *   								  use RFT API and let RFT to handle locating the item in the list.
 *   								  Impacted keywords: ClickTextItem, RightClickTextItem, RightClickUnverifiedTextItem
 *   								  Modified method verifySelected(), if the list contains nothing, a null object is returned, I 
 *   								  add some code to handle it.
 *   <br>   DEC 22, 2008    (JunwuMa) Modify getListContents() supporting SAS.SharedUI.TreeListView, which is derived from 
 *                                    System.Windows.Forms.ListView, also is a DotNetTable and a DotNetTree. This class combines features of ListBox, Tree and Table. 
 *                                    Take the first column of this object as the ListBox treated as usual. Fix S0553625. 
 *                                    Modify getListContents() and treat SAS.Shared.SASFormatInfo (list item) specifically. Fix S0554094.
 *   <br>	FEB 24, 2009	(LeiWang) For supporting Flex list. Modify method activatePartialMatch(),clickTextItem2(),getListContents(),
 *   																	modifiedClickTextItem(),selectTextItem(),verifyItem(),verifySelected()
 *   															Add method getListItem(),searchForListItem(),performDoubleClick(),scrollToIndex(),scrollToText()
 * 	 <br>	MAR 13, 2009    (CANAGL)  Renamed getListContents to be captureObjectData and refactored moving 
 *                                    SAS-specific class information into rational.custom.CFList.
 *   <br>   MAY 20, 2009    (CANAGL)  Removed extra stripping of quotes done to param. These quotes should already be 
 *                                    stripped in TestStepProcessor.interpretFields
 *   <br>	OCT 15, 2010	(LeiWang) Add keyword: ClickColumnIndex and ClickColumnLabel
 *   <br>	JUL 07, 2011	(LeiWang) Modify method captureObjectData(): get list data by verification point.
 **/
public class CFList extends CFComponent {

  public static final String ACTIVATETEXTITEM					= "ActivateTextItem";
  public static final String ACTIVATEPARTIALMATCH         		= "ActivatePartialMatch";
  public static final String ACTIVATEUNVERIFIEDPARTIALMATCH 	= "ActivateUnverifiedPartialMatch";
  public static final String ACTIVATEUNVERIFIEDTEXTITEM   		= "ActivateUnverifiedTextItem";
  public static final String CAPTUREITEMSTOFILE         		= "CaptureItemsToFile";
  public static final String EXTENDSELECTIONTOTEXTITEM  		= "ExtendSelectionToTextItem";
  public static final String SELECT                     		= "Select";
  public static final String SELECTANOTHERTEXTITEM      		= "SelectAnotherTextItem";
  public static final String SELECTANOTHERUNVERIFIEDTEXTITEM 	= "SelectAnotherUnverifiedTextItem";
  public static final String SELECTTEXTITEM             		= "SelectTextItem";
  public static final String CLICKTEXTITEM              		= "ClickTextItem";
  public static final String RIGHTCLICKTEXTITEM         		= "RightClickTextItem";
  public static final String RIGHTCLICKUNVERIFIEDTEXTITEM     	= "RightClickUnverifiedTextItem";
  public static final String SELECTINDEX                		= "SelectIndex";
  public static final String SELECTPARTIALMATCH         		= "SelectPartialMatch";
  public static final String SELECTUNVERIFIEDPARTIALMATCH 		= "SelectUnverifiedPartialMatch";
  public static final String SELECTUNVERIFIED           		= "SelectUnverified";
  public static final String SELECTUNVERIFIEDTEXTITEM   		= "SelectUnverifiedTextItem";
  public static final String SETTEXTVALUE               		= "SetTextValue";
  public static final String SETLISTCONTAINS            		= "SetListContains";
  public static final String VERIFYMENUPARTIALMATCH     		= "VerifyMenuPartialMatch";
  public static final String VERIFYMENUITEM             		= "VerifyMenuItem";
  public static final String VERIFYPARTIALMATCH         		= "VerifyPartialMatch";
  public static final String VERIFYITEM                 		= "VerifyItem";
  public static final String VERIFYSELECTED             		= "VerifySelected";
  public static final String VERIFYSELECTEDITEM         		= "VerifySelectedItem";
  public static final String VERIFYSELECTEDPARTIALMATCH 		= "VerifySelectedPartialMatch";
  public static final String VERIFYITEMUNSELECTED       		= "VerifyItemUnSelected";
  public static final String VERIFYLISTCONTAINS 				= "VerifyListContains";
  public static final String VERIFYLISTCONTAINSPARTIALMATCH		= "VerifyListContainsPartialMatch";
  public static final String VERIFYLISTDOESNOTCONTAIN 			= "VerifyListDoesNotContain";
  public static final String VERIFYLISTDOESNOTCONTAINPARTIALMATCH	= "VerifyListDoesNotContainPartialMatch";
  
  public static final String CLICKCOLUMNINDEX					= "ClickColumnIndex";
  public static final String CLICKCOLUMNLABEL					= "ClickColumnLabel";
  
  public static final String NETLISTBOXCLASSNAME				= "System.Windows.Forms.ListBox";
  public static final String NETLISTVIEWCLASSNAME				= "System.Windows.Forms.ListView";
  public static final String LISTDATA_RFT_VP_PROPERTY			= "list";
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFList () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>activatetextitem
   ** <li>activatepartialmatch
   ** <li>activateunverifiedpartialmatch
   ** <li>activateunverifiedtextitem
   ** <li>select
   ** <li>selectTextItem
   ** <li>clickTextItem 
   ** <li>selectIndex
   ** <li>selectPartialMatch
   ** <li>verifymenupartialmatch
   ** <li>verifymenuitem
   ** <li>verifypartialmatch - based on an index
   ** <li>verifyitem - based on an index
   ** <li>verifySelected
   ** <li>verifySelectedItem
   ** <li>VERIFYSELECTEDPARTIALMATCH
   ** <li>selectUnverifiedPartialMatch
   ** <li>selectUnverified
   ** <li>selectUnverifiedTextItem
   ** <li>setTextValue
   ** </ul><br>
   ** <br>NOTE: the 'activate' keywords didn't seem to work on the regression test
   ** because the use the guiObject.click instead of guiObject.setState method.
   ** The latter seems to work better
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   * <br>(CANAGL) 2009.05.20 Removed extra stripping of quotes done to param. These quotes 
   * should already be stripped in TestStepProcessor.interpretFields
   **/
  protected void localProcess() {
    try {
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+".process, searching specific tests...",
                     DEBUG_MESSAGE);

      if (action != null) {
        Log.info(".....CFList.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(ACTIVATETEXTITEM) ||
            action.equalsIgnoreCase(ACTIVATEPARTIALMATCH) ||
            action.equalsIgnoreCase(ACTIVATEUNVERIFIEDPARTIALMATCH) ||
            action.equalsIgnoreCase(ACTIVATEUNVERIFIEDTEXTITEM) ||
            action.equalsIgnoreCase(EXTENDSELECTIONTOTEXTITEM) ||
            action.equalsIgnoreCase(SELECT) ||
            action.equalsIgnoreCase(SELECTANOTHERTEXTITEM) ||
            action.equalsIgnoreCase(SELECTANOTHERUNVERIFIEDTEXTITEM) ||
            action.equalsIgnoreCase(SELECTTEXTITEM) ||
            action.equalsIgnoreCase(CLICKTEXTITEM) ||
            action.equalsIgnoreCase(RIGHTCLICKTEXTITEM) ||
            action.equalsIgnoreCase(RIGHTCLICKUNVERIFIEDTEXTITEM) ||
            action.equalsIgnoreCase(SELECTINDEX) ||
            action.equalsIgnoreCase(SELECTPARTIALMATCH) ||
            action.equalsIgnoreCase(VERIFYMENUITEM) ||
            action.equalsIgnoreCase(VERIFYMENUPARTIALMATCH) ||
            action.equalsIgnoreCase(VERIFYITEM) ||
            action.equalsIgnoreCase(VERIFYITEMUNSELECTED) ||
            action.equalsIgnoreCase(VERIFYSELECTED) ||
            action.equalsIgnoreCase(VERIFYSELECTEDITEM) ||
            action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH) ||
            action.equalsIgnoreCase(VERIFYPARTIALMATCH) ||
            action.equalsIgnoreCase(SELECTUNVERIFIEDPARTIALMATCH) ||
            action.equalsIgnoreCase(SELECTUNVERIFIED) ||
            action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM) ||
            action.equalsIgnoreCase(SETLISTCONTAINS) ||
            action.equalsIgnoreCase(VERIFYLISTCONTAINS) ||
            action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH) ||
            action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAIN) ||
        	action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH) ||
        	action.equalsIgnoreCase(CLICKCOLUMNINDEX) ||
        	action.equalsIgnoreCase(CLICKCOLUMNLABEL)) {
          if (params.size() < 1) {
            paramsFailedMsg(windowName, compName);
          } else {
            String param = null;
            try {
              // ready to do the select
              GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
              Iterator piter = params.iterator();
              param = (String) piter.next();
              Log.info("..... param: "+param);
              if (action.equalsIgnoreCase(SELECTINDEX)) {
                selectIndex(guiObj, param);
              }else if (action.equalsIgnoreCase(EXTENDSELECTIONTOTEXTITEM)){
                modifiedClickTextItem(Script.SHIFT_LEFT, guiObj, param);
              }else if (action.equalsIgnoreCase(SELECT) ||
                         action.equalsIgnoreCase(SELECTTEXTITEM)) {
                selectPartialMatch(guiObj, param, true);
              } else if (action.equalsIgnoreCase(SELECTANOTHERTEXTITEM)||
                         action.equalsIgnoreCase(SELECTANOTHERUNVERIFIEDTEXTITEM)) {
                modifiedClickTextItem(Script.CTRL_LEFT, guiObj, param);
              } else if (action.equalsIgnoreCase(CLICKTEXTITEM)) {
                clickTextItem2(guiObj, param, true, false,true);
              } else if (action.equalsIgnoreCase(RIGHTCLICKTEXTITEM)) {
                clickTextItem2(guiObj, param, true, true,true);
              }else if(action.equalsIgnoreCase(RIGHTCLICKUNVERIFIEDTEXTITEM)){ 
            	  clickTextItem2(guiObj, param, true, true, false);
              }else if (action.equalsIgnoreCase(ACTIVATETEXTITEM)) {
                activatePartialMatch(guiObj, param, true);
              } else if (action.equalsIgnoreCase(SELECTPARTIALMATCH) ||
                         action.equalsIgnoreCase(SELECTUNVERIFIEDPARTIALMATCH)) {
                selectPartialMatch(guiObj, param, false);
              } else if (action.equalsIgnoreCase(ACTIVATEPARTIALMATCH) ||
                         action.equalsIgnoreCase(ACTIVATEUNVERIFIEDPARTIALMATCH)) {
                activatePartialMatch(guiObj, param, false);
              } else if (action.equalsIgnoreCase(VERIFYMENUITEM) ||
                         action.equalsIgnoreCase(VERIFYMENUPARTIALMATCH)) {
                verifyMenuItem(guiObj, param);
              } else if (action.equalsIgnoreCase(VERIFYSELECTED) ||
                         action.equalsIgnoreCase(VERIFYSELECTEDITEM)) {
                verifySelected(guiObj, param, false);
              } else if (action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH)) {
                verifySelected(guiObj, param, true);
              } else if (action.equalsIgnoreCase(VERIFYITEMUNSELECTED)) {
                verifyUnSelected(guiObj, param, false);
              } else if (action.equalsIgnoreCase(VERIFYITEM) ||
                         action.equalsIgnoreCase(VERIFYPARTIALMATCH)) {
                verifyItem(guiObj, param, piter);
              } else if (action.equalsIgnoreCase(SELECTUNVERIFIED) ||
                         action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)) {
                modifiedClickTextItem(Script.LEFT, guiObj, param);
              } else if (action.equalsIgnoreCase(ACTIVATEUNVERIFIEDTEXTITEM)) {
                modifiedDoubleClickTextItem(Script.LEFT, guiObj, param);
              } else if (action.equalsIgnoreCase(VERIFYLISTCONTAINS)) {
                  VerifyListContains(guiObj, param, piter, true);
              } else if (action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH)) {
                  VerifyListContains(guiObj, param, piter, false);
              } else if (action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAIN)) {
                  VerifyListNotContain(guiObj, param, piter, true);
              } else if (action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH)) {
            	  VerifyListNotContain(guiObj, param, piter, false);
              } else if (action.equalsIgnoreCase(SETLISTCONTAINS)) {
            	  SetListContains(guiObj, param, piter);
              } else if (action.equalsIgnoreCase(CLICKCOLUMNLABEL) ||
            		  action.equalsIgnoreCase(CLICKCOLUMNINDEX)){
            	  clickColumn(guiObj, param);
              }else {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              }
            } catch (SubitemNotFoundException ex) {
              testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
            }
          }
        } else if (action.equalsIgnoreCase(CAPTUREITEMSTOFILE)) {
          captureItemsToFile();
        } else if (action.equalsIgnoreCase(SETTEXTVALUE)) {
          if (params.size() < 1) {
            paramsFailedMsg(windowName, compName);
          } else {
            // ready to do the set text value
            GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
            String val = (String) params.iterator().next();
            Log.info("..... val: "+val);
            // I have my doubts this is the right property to "set" in all cases.
            guiObj.setProperty("text", val);
            // set status to ok
            String altText = val+", "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_4, altText, val,
                                              windowName, compName, action),
                           PASSED_MESSAGE);
            testRecordData.setStatusCode(StatusCodes.OK);
          }
        }

        //all for now
      }
    }
    catch (WrappedException we) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "WrappedException: possibly the component is not visible, or possibly this"+
                     " action is really not supported; window: "+ windowName +"; comp: "+compName+
                     "; msg: "+we.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.UnsupportedActionException uae) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "UnsupportedAction: possibly the component is not visible, or possibly this"+
                     " action is really not supported; window: "+ windowName +"; comp: "+compName+
                     "; msg: "+uae.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "ObjectNotFound: the window or component are not visible or do not exist;"+
                     " window: "+ windowName +"; comp: "+compName+
                     "; msg: "+onfe.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.TargetGoneException tge) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "TargetGone: the window or component are unexpectedly gone;"+
                     " window: "+ windowName +"; comp: "+compName+
                     "; msg: "+tge.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (NullPointerException npe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "Item not found: "+
                     " window: "+ windowName +"; comp: "+compName+", action: "+action+", params: "+
                     params+"; msg: "+npe.toString(),
                     FAILED_MESSAGE);
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getClass().getName()+", msg: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }
  
    /**
   * try both click and setState(Action.select() means to select an item by text value
   */
  private boolean selectTextItem(GuiSubitemTestObject guiObj, String itemtext){
	  String debugmsg = getClass().getName()+".selectTextItem(): ";
	  
	  try{
		  //For Flex list, we must scroll to make the item visible on the screen
		  if (scrollToText(guiObj, itemtext)) {
			  guiObj.click(Script.localAtText(itemtext));
		  }else{
			  Log.debug(debugmsg + " can not scroll to item: "+itemtext);
			  return false;
		  }
	  }catch (Exception e){
	      try{ 
	          guiObj.setState(Action.select(), Script.localAtText(itemtext)); }
	      catch(Exception x){
	          return false;
	      }
      }
	  return true;	   
  }
  
  /**
   * try both click and setState(Action.select() means to select an item by index
   */
  protected boolean selectItemAtIndex(GuiSubitemTestObject guiObj, int index){
      try{
          guiObj.click(Script.localAtIndex(index));
      }catch (Exception e) {
          try{ 
      	      guiObj.setState(Action.select(), Script.localAtIndex(index));}
      	   catch(Exception y){
              return false;
  	      }
      }
      return true;
  }
  
  
  /** <br><em>Purpose:</em> selectIndex
   * <br>	17 JUL 2008 	(LeiWang)	Modify index setting. Because SAFS consider list's index start from 1;
   * 									while RFT consider list's index start from 0. We must minus 1 from the
   * 									index param and pass it to RFT.
   **/
  protected void selectIndex (GuiSubitemTestObject guiObj, String param) throws SAFSException {
    int index = 0;
    try {
    	index = Integer.parseInt(param);
    } catch (NumberFormatException nfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+": invalid index format: "+param,
                     FAILED_MESSAGE);
      return;
    }
    if (index < 1 ) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": bad index (less than 1): "+param,
                       FAILED_MESSAGE);
        return;
    }
    
    if (selectItemAtIndex(guiObj, (index-1))){
	    log.logMessage(testRecordData.getFac(),
	                   " "+action+" ok at index : "+index,
	                   PASSED_MESSAGE);
	    testRecordData.setStatusCode(StatusCodes.OK);
    }
    else{
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                     getClass().getName()+": item index may be out of range: "+index,
                     FAILED_MESSAGE);
    }                
    return;
  }
  
  /** <br><em>Purpose:</em> selectPartialMatch
   ** @param exact, boolean, if true, then exact match, else partial match
   **/
  protected void selectPartialMatch (GuiSubitemTestObject guiObj, String param, boolean exact) {
  	
  	  Log.info ("* * * Inside selectPartialMatch * * *");
      //implement the partial match...
      List list = null;
    
      // NOTE: HTML Comboboxes are no longer handled here.
      try{
      	list = this.captureObjectData(guiObj);
      }catch(Exception e){
      	// try other engines
    	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
    	return;
      }
      
      Log.info("list: "+list);
      
      // do the work of matching...
      ListIterator iter = list.listIterator();
      int j = -1;
      if (exact) {
        j = StringUtils.findExactMatchIndex(iter, param);
      } else {
        j = StringUtils.findMatchIndex(iter, param);
      }
      if (j>=0) {
        String match = (String) iter.previous();
        Log.info("match: "+j+", "+match);

		if(selectTextItem(guiObj, match)){
	        String altText = param+", "+match+", "+windowName+":"+compName+" "+action;
	        log.logMessage(testRecordData.getFac(),
	                       passedText.convert(PRE_TXT_SUCCESS_5, altText, param, match,
	                                          windowName, compName, action),
	                       PASSED_MESSAGE);
	        testRecordData.setStatusCode(StatusCodes.OK);
		}
		else{
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                           getClass().getName()+": matched parameter may not be valid: "+match,
                           FAILED_MESSAGE);
	    }
      } else {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": no match on: " + param,
                       FAILED_MESSAGE);
      }
  }


  /** <br><em>Purpose:</em> clickTextItem
   * Generates a coordinate-based click at the deduced location of the list item.
   ** @param exact boolean; if true, then exact match, else partial match
   ** @param rightClick boolean; Right click or Left click? 
   * @deprecated	Use clickTextItem2 instead. 
   **/
  protected void clickTextItem (GuiSubitemTestObject guiObj, String param, 
      boolean exact, boolean rightClick) {
	  String debugmsg = getClass().getName()+".clickTextItem(): ";
  	
  	  Log.info ("* * * Inside clickTextItem * * *");
      //implement the partial match...
      List list = null;
      try{
      	list = this.captureObjectData(guiObj);
      }catch(Exception e){
    	// try other engines
    	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);  
    	return;
      }
      
      Log.info("list: "+list);
      // do the work of matching...
      ListIterator iter = list.listIterator();
      int j = -1;
      if (exact) {
        j = StringUtils.findExactMatchIndex(iter, param);
      } else {
        j = StringUtils.findMatchIndex(iter, param);
      }
      
      // if matched then get the Point location of the match
      // what if the matched item is NOT scrolled into view, eh?
      if (j>=0) {
          String match = (String) iter.previous();
          Log.info("match (index): "+j+", "+match);
		  Object[] coords = new Object[1];
		  coords[0] = new Integer(j);

		  java.awt.Point p = null;
		  String className = guiObj.getObjectClassName();
    	  try{
    		  if(isJavaDomain(guiObj)){
    			  //java.awt.List does not support method indexToLocation()
    			  p = (java.awt.Point) guiObj.invoke("indexToLocation", "(I)Ljava/awt/Point;", coords);
    		  }else if(isDotnetDomain(guiObj)){
    			  //When invoke the method GetItemRectangle()
    			  //RFT will convert the result from class System.Drawing.Rectangle to java.awt.Rectangle
    			  String methodName = null;
    			  TestObject clazz = DotNetUtil.getClazz(guiObj);
    			  
    			  if(DotNetUtil.isSubclassOf(clazz, NETLISTBOXCLASSNAME)){
    				  methodName = "GetItemRectangle";
    			  }else if(DotNetUtil.isSubclassOf(clazz, NETLISTVIEWCLASSNAME)){
    				  methodName = "GetItemRect";
    			  }else{
    				  Log.debug(debugmsg+" DotNet Domain: Not suport for class:"+className);
        	          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        	          log.logMessage(testRecordData.getFac(),
        	                       getClass().getName()+": do not support for class : " + className,
        	                       FAILED_MESSAGE);
        	          return;
    			  }
    			  
    			  if(methodName != null){
	    			  Rectangle rectangle = (Rectangle) guiObj.invoke(methodName,"(I)LSystem.Object;",coords);
	    			  Log.info(debugmsg+rectangle.x+" ,"+rectangle.y);
	    			  p= rectangle.getLocation();
    			  }
    		  }else{
    			  Log.info(debugmsg+" Not suport for class: "+className);
    	          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    	          log.logMessage(testRecordData.getFac(),
    	                       getClass().getName()+": do not support for class : " + className,
    	                       FAILED_MESSAGE);
    	          return;
    		  }
    	  }catch(Exception x){
	          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	          log.logMessage(testRecordData.getFac(),
	                       getClass().getName()+": cannot determine valid location for: " + param,
	                       FAILED_MESSAGE);
	          return;
    	  }
		  Log.debug("* * * Point after invoking indexToLocation API: " + p);

		  //Adjusting the point... so that, click occurs with in the rectangle...
		  p.x = p.x + 20;
		  p.y = p.y + 5;
		  if (rightClick) 
		  	guiObj.click(Script.RIGHT, p);
          else
          	guiObj.click(p);

          String altText = param+", "+match+", "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_5, altText, param, match,
                                          windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
      } else {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": no match on: " + param,
                       FAILED_MESSAGE);
      }
  }

  /**
   * <br>Note:					This method can replace clickTextItem().
   * @param guiObj				TestObject represents a List GUI object.
   * @param param				The item to be clicked in the list.
   * @param exact				If it is true, an exact match when do verification.
   * @param rightClick			If it is true, perform a mouse right click.
   * @param verified			If it is true, perfrom a verification, that is to verify if parameter itemToClick
   * 							can be found in the List GUI object.
   * @throws SAFSException		Will be thrown out if the list contents can not be exacted from the list object.
   */
  protected void clickTextItem2(GuiSubitemTestObject guiObj, 
		  						String itemToClick,
		  						boolean exact,
		  						boolean rightClick,
		  						boolean verified) throws SAFSException{
	  String debugmsg = getClass().getName()+".clickTextItem2() ";
	  List list = null;
	  
	  Log.info(debugmsg+"click at text: "+itemToClick+" verified: "+verified+" exact: "+exact+" rightClick: "+rightClick);
	  
	  if(verified){
		  list = captureObjectData(guiObj);
		  Log.info(debugmsg+" list contents: "+list);
		  
	      ListIterator iter = list.listIterator();
	      int j = -1;
	      if (exact) {
	    	  j = StringUtils.findExactMatchIndex(iter, itemToClick);
	      } else {
	    	  j = StringUtils.findMatchIndex(iter, itemToClick);
	      }
	      
	      if (j>=0) {
	    	  //Get the exact item's text to be clicked in the list
	    	  itemToClick = (String) iter.previous();
	          Log.info(" find match (index): "+j+", "+itemToClick);
	          //For flex list, need to scroll to make the item visible on screen
	          if(!scrollToIndex(guiObj,j)){
	        	Log.debug(debugmsg+" can not scroll to item "+itemToClick);
	        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	        	log.logMessage(testRecordData.getFac(),getClass().getName()+": can not scroll to : " + itemToClick,FAILED_MESSAGE);
	        	return;
	          }
	      }else {
	          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	          log.logMessage(testRecordData.getFac(),
	                       getClass().getName()+": no match on: " + itemToClick,
	                       FAILED_MESSAGE);
	          return;
	      }
	  }
	  
	  if(rightClick){
		  guiObj.click(Script.RIGHT, Script.atText(itemToClick));
	  }else{
		  guiObj.click(Script.atText(itemToClick));
	  }
	  
      String altText = itemToClick+", "+windowName+":"+compName+" "+action;
      log.logMessage(testRecordData.getFac(),
                     passedText.convert(PRE_TXT_SUCCESS_4, altText, itemToClick,
                                      windowName, compName, action),
                     PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
  }

  /** <br><em>Purpose:</em> activatePartialMatch
   **/
  protected void activatePartialMatch (GuiSubitemTestObject guiObj, String param, boolean exact) throws SAFSException{
//	  String debugmsg = getClass().getName()+".activatePartialMatch(): ";
	  
	  String matchedText = performDoubleClick(guiObj,param,exact,null);
      
      String altText = param + ", " + matchedText + ", " + windowName + ":"+ compName + " " + action;
      String message =  passedText.convert(PRE_TXT_SUCCESS_5, altText, param, matchedText, windowName,compName, action);
      log.logMessage(testRecordData.getFac(), message, PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /** <br><em>Purpose:</em> verifyMenuItem
   **/
  protected void verifyMenuItem (GuiSubitemTestObject guiObj, String param) {
      //implement the partial match...
      java.util.List list = null;
      try{
      	list = this.captureObjectData(guiObj);
      }catch(Exception e){
    	// try other engines
    	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
    	return;
      }
      Log.info("list: "+list);
      // do the work of matching...
      // this is matching for *A* match, in other words, does the item exist in the menu
      ListIterator iter = list.listIterator();
      int j = -1;
      if (action.equalsIgnoreCase(VERIFYMENUITEM)) {
        j = StringUtils.findExactMatchIndex(iter, param);
      } else {
        j = StringUtils.findMatchIndex(iter, param);
      }
      if (j>=0) {
        String match = (String) iter.previous();
        Log.info("match: "+j+", "+match);
        String altText = param+", "+match+", "+windowName+":"+compName+" "+action;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_5, altText, param, match,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } else {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": no match on: " + param,
                       FAILED_MESSAGE);
      }
  }


  /** <br><em>Purpose:</em> verifySelected
   ** @param partialmatch, if true, then partial match on selected
   **/
  protected void verifySelected (GuiSubitemTestObject guiObj, String param,
                                 boolean partialmatch) {
        verifySelected(guiObj, param, partialmatch, true);
  }
        
  /** <br><em>Purpose:</em> verifyUnSelected
   ** @param partialmatch, if true, then partial match on unselected
   **/
  protected void verifyUnSelected (GuiSubitemTestObject guiObj, String param,
                                   boolean partialmatch) {
        verifySelected(guiObj, param, partialmatch, false);
  }
  
  /** <br><em>Purpose:</em> verifySelected
   ** @param partialmatch if true, then partial match on selected
   ** @param desireSelected if true, test for selected; otherwise, unselected
   **/  
  protected void verifySelected (GuiSubitemTestObject guiObj, String param,
                                 boolean partialmatch, boolean desireSelected) {
        
	String debugmsg = getClass().getName()+".verifySelected(): ";
	int statusCode = StatusCodes.SCRIPT_NOT_EXECUTED;
    int logMsgCode = FAILED_MESSAGE;
    String logMsg = null;
    
    String className = guiObj.getObjectClassName();
    Log.info(debugmsg+"className="+className);
        
    Log.info("..... param: " + param);
    // ready to do the verify
    Integer[] selectedIndices = null;
    try{
    	
    	if(isJavaDomain(guiObj)){
    		Object object = null;
    		try{
    			//For class javax.swing.JList and its subclass
    			object = guiObj.invoke("getSelectedIndices");
    		}catch(MethodNotFoundException e){
    			//For class java.awt.List and its subclass
    			object =  guiObj.invoke("getSelectedIndexes");
    		}
    		int arrayLength = object==null? 0:Array.getLength(object);
    		if(arrayLength>0){
    			selectedIndices = new Integer[arrayLength];
    			for(int i=0;i<arrayLength;i++){
    				selectedIndices[i] = new Integer(Array.getInt(object, i));
    			}
    		}
    	}else if(isDotnetDomain(guiObj)){
    		try{
    			//For both System.Windows.Fors.ListBox and System.Windows.Fors.ListView
	    		TestObject indicesCollection = (TestObject) guiObj.getProperty("SelectedIndices");
	    		int count = indicesCollection==null? 0:((Integer)indicesCollection.getProperty("Count")).intValue();
	    		if(count >0){
	    			selectedIndices = new Integer[count];
	    		}
	    		Object[] args = new Object[1];
	    		for(int i=0;i<count;i++){
	    			args[0] = new Integer(i);
	    			selectedIndices[i] = (Integer) indicesCollection.invoke("get_Item","(I)LSystem.Object;",args);
	    		}
    		}catch(PropertyNotFoundException e){
    			Log.debug(debugmsg+" For .NET object Property not found"+e.getMessage());
    		}
    	}else if(isHtmlDomain(guiObj)){
    		Object object = guiObj.getProperty(".selectedIndex");
    		if(object!=null){
    			Integer index = (Integer) object;
    			if(index.intValue()!=-1){
    				selectedIndices = new Integer[1];
    				selectedIndices[0] = index;		
    			}
    		}
    	}else if(isFlexDomain(guiObj)){
    		try{
    			Object indicesCollection = (Object) guiObj.getProperty("selectedIndices");
    			Log.debug(debugmsg+indicesCollection.getClass().getName()+" : "+indicesCollection);
    			if(indicesCollection instanceof String){
    				String[] indices = ((String) indicesCollection).split(";");
    				selectedIndices = new Integer[indices.length];
    				for (int i = 0; i < indices.length; i++) {
    					selectedIndices[i] = Integer.parseInt(indices[i]);
    				}
    			}else{
    				Log.debug(debugmsg+" Need new implementation for flex domain. ");
    			}		
    		}catch(PropertyNotFoundException e){
    			Log.debug(debugmsg+" For Flex object Property 'selectedIndices' not found"+e.getMessage());
    		}
    	}else{
    		Log.info(debugmsg+className+" can not be handled in current implementation.");
            testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
            // try another engine
            return;
    	}
    }catch(Exception x){
        Log.info(debugmsg+" Exception occured: "+x.getMessage());
        testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        // try another engine
        return;
    }

    if(selectedIndices!=null){
    	int i = 0;
    	for(;i<selectedIndices.length;i++){
	        // something is selected
	        String text = "";
	        boolean match = false;
	        
	        try {
	            text = getListItem(guiObj, selectedIndices[i].intValue());
	        } catch(Exception x) {
	            if (desireSelected) {
	                // failure if testing for selected
	                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	                log.logMessage(testRecordData.getFac(),
	                               getClass().getName()
	                               + ": selected item in object may be invalid: " 
	                               + param, FAILED_MESSAGE);                     
	            } else {
	                // warning if testing for unselected
	                testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
	                log.logMessage(testRecordData.getFac(),
	                               getClass().getName()
	                               + ": selected item in object may be invalid: " 
	                               + param, WARNING_MESSAGE);        
	            }
	            return;
	        }
	        
            if (partialmatch) { 
            	try{ match = text.indexOf(param) > -1; }catch(Exception x){;}
            }
            else { match = param.equals(text); } 
	      
	        if(match){
	    		if(desireSelected){
		            //verification success
		            statusCode = StatusCodes.OK;
		            logMsgCode = PASSED_MESSAGE;
		            logMsg = passedText.convert(TXT_SUCCESS_4, action, windowName,compName, action, param);
	    		}else{
	    			//verification fail
		            statusCode = StatusCodes.GENERAL_SCRIPT_FAILURE;
		            logMsgCode = FAILED_MESSAGE;
		            logMsg = getClass().getName() +": selected item '" + text + "' unexpectedly matched: " + param;
	    		}
	        	break;
	        }
    	}//End for loop
    	
		//We can not find a matched item
    	if(i==selectedIndices.length){
    		if(desireSelected){
	            // verification fail
	            statusCode = StatusCodes.GENERAL_SCRIPT_FAILURE;
	            logMsgCode = FAILED_MESSAGE;
	            logMsg = getClass().getName() + ": selected items did not match: " + param;
    		}else{
    			//verification success    			
	            statusCode = StatusCodes.OK;
	            logMsgCode = PASSED_MESSAGE;
	            logMsg = passedText.convert(TXT_SUCCESS_4, action, windowName,compName, action, param);
    		}
    	}
    } else {
        // nothing is selected
        if (desireSelected) {                   
            statusCode = StatusCodes.GENERAL_SCRIPT_FAILURE;
            logMsgCode = FAILED_MESSAGE;
            logMsg = getClass().getName()
                + ": there may not be any item selected when seeking: " 
                + param;
        } else {
            statusCode = StatusCodes.OK;
            logMsgCode = PASSED_MESSAGE;
            logMsg = passedText.convert(TXT_SUCCESS_4, action, windowName, 
                                        compName, action, param);
        }
    }

    // report results
    log.logMessage(testRecordData.getFac(), logMsg, logMsgCode);
    testRecordData.setStatusCode(statusCode);

  }

  /** <br><em>Purpose:</em> verifyItem
   **/
  protected void verifyItem (GuiSubitemTestObject guiObj, String param, Iterator piter) {
  	Integer index = null;
    try {
      index = new Integer(param);
      index = new Integer(index.intValue() - 1);
    }
    catch(Exception x){
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+": index format error: " + x.getMessage(),
                     FAILED_MESSAGE);
      return;
    }
    try{
      param = (String) piter.next();
      String val = getListItem(guiObj,index.intValue());
      Log.info("val: "+val);
      // do the work of matching...
      if (action.equalsIgnoreCase(VERIFYITEM)) {
        if (val.equals(param)) {
          String altText = param+", "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4, altText, param,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         getClass().getName()+": no match on: " + param + ", index:"+index+", actual value is: "+val,
                         FAILED_MESSAGE);
        }
      } else {
        if (val.toLowerCase().indexOf(param.toLowerCase())>=0) {
          String altText = param+", "+val+", "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_5, altText, param, val,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         getClass().getName()+": no match on: " + param + ", index:"+index+", actual value is: "+val,
                         FAILED_MESSAGE);
        }
      }
    } catch (NoSuchElementException nsee) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+": not enough parameters",
                     FAILED_MESSAGE);
    } catch (SAFSException se) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+": " + se.getMessage(),
                     FAILED_MESSAGE);
    }
  }
 
	/**
	 * @param guiObj		A test object represents a list
	 * @param index			An index of the item in a list
	 * @return				String, the item's text at index of a list
	 * @throws SAFSException
	 */
	protected String getListItem(GuiTestObject guiObj, int index) throws SAFSException{
		String debugmsg = getClass().getName() + ".getListItem(): ";
		String item = "";

		String className = guiObj.getObjectClassName();
		Log.info(debugmsg + "className=" + className);

		try {
			try{
				item = utils.getListItem(guiObj, index, "text");
			}catch(PropertyNotFoundException e1){
				Log.debug(debugmsg+" property 'text' does not exitst. Try 'Text'.");
				try{
					item = utils.getListItem(guiObj, index, "Text");	
				}catch(PropertyNotFoundException e2){
					Log.debug(debugmsg+" property 'Text' does not exitst. Try 'value'.");
					try{
						item = utils.getListItem(guiObj, index, "value");	
					}catch(PropertyNotFoundException e3){
						Log.debug(debugmsg+" property 'value' does not exitst. Maybe add one of Properties: "+guiObj.getProperties());
					}
				}
			}
		} catch (Exception ex) {
			Log.info(debugmsg + ex.toString()+ " could NOT extract item "+ index+" for "+ guiObj.getObjectClassName());
			throw new SAFSException(ex.toString() + " RJ:CFList could NOT extract item "+index);
		}
		
		return item;
	}
  
  /** <br><em>Purpose:</em> click with various mouse buttons active
   **/
  protected void modifiedClickTextItem (MouseModifiers mbuttons, GuiSubitemTestObject guiObj, String param) throws SAFSException {
    String debugmsg = getClass().getName()+".modifiedClickTextItem(): ";
    
	try{
    	if(scrollToText(guiObj,param)){
    		guiObj.click(mbuttons, Script.localAtText(param));
    	}else{
    		Log.debug(debugmsg+ param+ " does not exist in list");
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                           getClass().getName()+": action error on: " + param,
                           FAILED_MESSAGE);
            return;		
    	}
    }catch(Exception x){
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": action error on: " + param,
                       FAILED_MESSAGE);
        return;
    }
    String altText = param+", "+windowName+":"+compName+" "+action;
    log.logMessage(testRecordData.getFac(),
                   passedText.convert(PRE_TXT_SUCCESS_4, altText, param,
                                      windowName, compName, action),
                   PASSED_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  
  /** <br><em>Purpose:</em> activateUnverifiedTextItem
   **/
  protected void modifiedDoubleClickTextItem (MouseModifiers mbuttons, GuiSubitemTestObject guiObj, String param) throws SAFSException {
    try{
    	guiObj.doubleClick(mbuttons,Script.localAtText(param));
    }catch(Exception x){
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": action error on: " + param,
                       FAILED_MESSAGE);
        return;
    }
    String altText = param+", "+windowName+":"+compName+" "+action;
    log.logMessage(testRecordData.getFac(),
                   passedText.convert(PRE_TXT_SUCCESS_4, altText, param,
                                      windowName, compName, action),
                   PASSED_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /** <br><em>Purpose:</em> captureItemsToFile
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JList, CaptureItemsToFile, AFileName.txt
   ** <br> Capture all items of JList to file AFileName.txt
   ** <br> 
   **/
  protected void captureItemsToFile () throws SAFSException {
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      try {
        log.logMessage(testRecordData.getFac(),"...params: "+params, DEBUG_MESSAGE);
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator piter = params.iterator();
        String filename =  (String) piter.next();
        Log.info("...filename: "+filename);
        String fileEncoding = null;
        if(piter.hasNext()){
        	fileEncoding =  (String) piter.next();
        	//If user put a blank string as encoding,
        	//we should consider that user does NOT provide a encoding, reset encoding to null.
        	fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
        }        
        Log.info("...filename: "+filename+";  fileEncoding: "+fileEncoding);
        
        java.util.List list = null;
        try{
          	list = this.captureObjectData(guiObj);
        }catch(Exception e){
        	// try other engines
        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	return;
        }
        Log.info("list: "+list);
        try {
          File file = new CaseInsensitiveFile(filename).toFile();
          if (!file.isAbsolute()) {
            String testdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
            if (testdir != null) {
              file = new CaseInsensitiveFile(testdir, filename).toFile();
              filename = file.getAbsolutePath();
            }
          }
          Log.info("Writing to file: "+filename);
	      //If a file encoding is given or we need to keep the encoding consistent
	      if (fileEncoding != null || keepEncodingConsistent) {
	    	  StringUtils.writeEncodingfile(filename, list, fileEncoding);
	      } else {
	    	  // Keep compatible with old version
	    	  StringUtils.writefile(filename, list);
	      }
        } catch (java.io.IOException e) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         getClass().getName()+": io exception: "+e.getMessage(),
                         FAILED_MESSAGE);
          return;
        }
        // set status to ok
        String altText = filename+", "+windowName+":"+compName+" "+action;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_4, altText, filename,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": item not found "+ 
                       "; msg: "+ex.getMessage(),
                       FAILED_MESSAGE);
      } catch (SAFSException se) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": " + se.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }
  
  /** <br><em>Purpose:</em> repeatly access each list-item of guiObj to see if its substring matches target string param.
   *                        Used by keyword SetListContains.
   * @param guiObj  stands for JList, Html.SELECT or .NET list
   * @param param   target string  
   * @param piter   Iterator in which first item is case-sensitivity. 
   * @throws SAFSException
   */
  protected void SetListContains(GuiSubitemTestObject guiObj, String param, Iterator piter) throws SAFSException{
      // get the varible to be assigned
      String varIsFound = null;
	  if(piter.hasNext())
		  varIsFound = (String) piter.next();
	  else {
	      paramsFailedMsg(windowName, compName);
	      return;
	  }
	  // see if the list contains the text
      List list = this.captureObjectData(guiObj);
    
	  Log.info(getClass().getName()+".SetListContains starting");
      Log.info("start matching...");
      ListIterator iter = list.listIterator();
      // see if it is case-sensitive Match
      int idx = StringUtils.findExactMatchIndex(iter, param);
      String strMatch = (idx>=0)?"TRUE":"FALSE"; 
      
      Log.info("...List contained as expected? :" + strMatch);
      if (!setVariable(varIsFound, strMatch)) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
        		  		 failedText.text("could_not_set_vars","Could not set variable "+varIsFound),
                         FAILED_MESSAGE);
      } else {
          testRecordData.setStatusCode(StatusCodes.OK);   	  
    	  log.logMessage(testRecordData.getFac(),
                  passedText.convert("varAssigned2",
                		  			 "value "+strMatch+" was assigned to "+varIsFound,
                                     strMatch,
                                     varIsFound),
                  PASSED_MESSAGE);    	  
      }
  }
 
  /** <br><em>Purpose:</em> respond to VERIFYLISTCONTAINS and VERIFYLISCONTAINSPARTIALMATCH. 
   * @param guiObj  stands for JList or Html.SELECT
   * @param param   target string  
   * @param piter   Iterator in which first item is case-sensitivity. 
   * @param exact   true means exactly matching the target string; false means partially matching.  
   */
  protected void VerifyListContains(GuiSubitemTestObject guiObj, String param, Iterator piter, boolean exact) {
      try {
          if (IsListContain(guiObj,param,piter,exact)) {
              Log.info("...List contained as expected.");
              testRecordData.setStatusCode(StatusCodes.OK);
              this.componentSuccessMessage(genericText.convert(GENStrings.CONTAINS_KEY,
                      								param+" contained",
                      								"The List",
                      								param));              
              
          } else {
              Log.info("...List not contained as unexpected.");
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              this.componentExecutedFailureMessage(genericText.convert(GENStrings.NOT_CONTAIN_KEY,
                      								param+" not contained", 
                      								"The List",
                      								param));              
          }
      } 
      catch (SAFSException se) {
    	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    	  this.componentFailureMessage("Script not executed!" + se.getMessage());
      }     
  }
      
  /** <br><em>Purpose:</em> respond to VERIFYLISDOESNOTCONTAINMATCH and VERIFYLISDOESNOTCONTAINPARTIALMATCH 
   * @param guiObj  stands for JList or Html.SELECT
   * @param param   target string  
   * @param piter   Iterator in which first item is case-sensitivity. 
   * @param exact   true means exactly matching the target string; false means partially matching. 
   */
  protected void VerifyListNotContain(GuiSubitemTestObject guiObj, String param, Iterator piter, boolean exact) {
      try {
          if (IsListContain(guiObj,param,piter,exact)) {
              Log.info("...List contained as unexpected.");
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              this.componentExecutedFailureMessage(genericText.convert(GENStrings.CONTAINS_KEY,
                      								param+" contained",
                      								"The List",
                      								param));
          } else {
              Log.info("...List not contained as expected.");
              testRecordData.setStatusCode(StatusCodes.OK);   
              this.componentSuccessMessage(genericText.convert(GENStrings.NOT_CONTAIN_KEY,
											param+" not contained",                      
                                           "The List",
                                           param));
          }
      } 
      catch (SAFSException se) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    	  this.componentFailureMessage("Script not executed!" + se.getMessage());
      }
  }
  /** <br><em>Purpose:</em> repeatly access each list-item of guiObj to see if its substring matches target string param.
   *                        Called by VerifyListContains and VerifyListNotContain.
   * @param guiObj  stands for JList or Html.SELECT
   * @param param   target string  
   * @param piter   Iterator in which first item is case-sensitivity. 
   * @param exact   true means exactly matching the target string; false means partially matching.
   * @return  true if finding target string; otherwise false 
   * @throws SAFSException
   */
  private boolean IsListContain(GuiSubitemTestObject guiObj, String param, Iterator piter, boolean exact) throws SAFSException {
      //implement the partial match...
      List list = this.captureObjectData(guiObj);

      // see if this is a case-sensitive check
      boolean isCaseSens = false;
	  if(piter.hasNext()){
		  String strcase = (String) piter.next();
		  Log.info("extracted potential USECASE value of: "+ strcase);
		  isCaseSens = ((strcase.equalsIgnoreCase("CASE-SENSITIVE"))||
		    	    (strcase.equalsIgnoreCase("CASESENSITIVE"))||
		    	    (strcase.equalsIgnoreCase("TRUE")));
	  }
    
	  Log.info("list: "+list);
	    
	  // Change param to UpperCase if not case-sensitive
	  param = !isCaseSens?param.toUpperCase():param;
            
      // Change each element in list to UpperCase if not case-sensitive
      if (!isCaseSens)
          for (ListIterator i = list.listIterator(); i.hasNext(); ) { 
              String tmpStr = i.next().toString().toUpperCase();
              i.set(tmpStr);
          }
     
      Log.info("start matching...");
      ListIterator iter = list.listIterator();
      int idx = -1;
      // see if it Partial Match
      if (exact) 
          idx = StringUtils.findExactMatchIndex(iter, param);
      else
          idx = StringUtils.findMatchIndex(iter, param);
      
      return (idx>=0);
  }
  
  /**
   * <em>Note:</em>			Need to modify this method if we want to support new application type, such as Flex application
   * @param guiObj			This is the reference to the List object (java or .net or other list)
   * @return				A list contains all items of a listbox
   * @throws SAFSException
   */
  protected List captureObjectData(TestObject guiObj) throws SAFSException{
    String debugmsg = getClass().getName()+".captureObjectData(): ";
  	java.util.List list = null;
    
    String className = guiObj.getObjectClassName();
    Log.info(debugmsg+"className="+className);
    
    //Treate the java object
    if(isJavaDomain(guiObj)){
	    try {
			list = utils.extractListItems(guiObj, "itemCount", "text");
		}catch (Exception x){
			Log.info(debugmsg+" could NOT extract list of items for java application!");
			throw new SAFSException("RJ:CFList could NOT extract list of items");
		}
	//Treate HTML application
    }else if(isHtmlDomain(guiObj)){
	    try {
	    	list = utils.extractListItems(guiObj, ".length", ".text");
		}catch (Exception x){
			Log.info(debugmsg+" could NOT extract list of items for HTML application!");
			throw new SAFSException("RJ:CFList could NOT extract list of items");
		}
	//Treate the .NET object	
    }else if(isDotnetDomain(guiObj)){
    	//property "Items","Count" and method "getItem(int)" exist in 
    	//System.Windows.Forms.ListBox; System.Windows.Forms.ListView
    	//NOT sure about if these properties exist in the other Class of type List
    	list = new ArrayList();
    	try{
    		TestObject items = (TestObject) guiObj.getProperty("Items");
    		Log.debug(debugmsg+" .Net property items="+items);
    		int count = ((Integer)items.getProperty("Count")).intValue();
    		Log.debug(debugmsg+" count="+count);
    		
    		Object[] index = new Object[1];
    		Object value = null;
    		String strvalue = null;
    		for(int i=0;i<count;i++){
    			index[0] = new Integer(i);
    			value = (items.invoke("get_Item","(I)LSystem.Object;",index));
    			//For ListBox, the item is a string; But for ListView, the item is a ListViewItem, need to get it's text.
				strvalue = convertObjectValueToString(value);
			    list.add(strvalue);
    		}
    	}catch(Exception e){
    		Log.debug(debugmsg+" Exception: "+e.getMessage()+". Can't get list data through .net property 'Items'");
    		list = getListItems(guiObj, LISTDATA_RFT_VP_PROPERTY);
    	}
    }        
    return list;
  }
  
  /**
   * 
   * @param guiObj       TestObject representing the ListBox object
   * @param vpProperty   String representing the property name of list test data provided<br>
   *                     by RFT when it creates verification point.<br>
   *                     This property name will be different for different applications<br>
   *                     For example: "list", "allitems" etc.
   * @return             A list of itmes contained in the ListBox
   */
  protected List getListItems(TestObject guiObj, String vpProperty) throws SAFSException{
    String debugmsg = getClass().getName()+".getListItems(): ";
    List list = new ArrayList();
	  
	Log.debug(debugmsg+" Try RFT Verification Point property '"+vpProperty+"' to get list items");
	try{ 
    	//Try to get the data through the verification point of RFT
   		Hashtable dataTypes = guiObj.getTestDataTypes();
   		Log.debug(debugmsg+" Test Object's verification point data types are "+dataTypes);
   		//For list data, the type name maybe 'list' or 'allitems'
   		ITestData testData = guiObj.getTestData(vpProperty);
   		Log.debug(debugmsg+" list data Class is "+testData.getClass().getName());
   		if(testData instanceof ITestDataList){
   			ITestDataList listdata = (ITestDataList) testData;
   			ITestDataElementList elements = listdata.getElements();
   			for(int i=0;i<elements.getLength();i++){
   				ITestDataElement element = (ITestDataElement) elements.getElement(i);
   				list.add(convertObjectValueToString(element.getElement()));
   			}
   		}else{
   			Log.warn(debugmsg+" can't get list data by RFT Verification point.");
   			throw new Exception("can't get list data by RFT Verification point.");
   		}
	}catch(Exception e1){
		Log.debug(debugmsg+"Exception: "+e1.getMessage());
		throw new SAFSException("RJ:CFList could NOT extract list of items from "+ guiObj.getObjectClassName());    			
	}
	
	return list;
  }
  
  /**
   * <em>Purpose:</em> Search item in list
   * @param guiObj			A test object represents a list
   * @param param			An item text
   * @param exact			If true, the item will be matched exactly; otherwise, it will be matched partially.
   * @return				an array that contains the index and searchedItem
   * @throws SAFSException
   */
	@SuppressWarnings("unchecked")
	protected String[] searchForListItem(GuiSubitemTestObject guiObj,String param, boolean exact) throws SAFSException{
		String debugmsg = getClass().getName()+".searchForListItem(): ";
		
		String[] indexItem = new String[2];
		List list =  captureObjectData(guiObj);
		Log.info("list: " + list);
		// do the work of matching...
		ListIterator iter = list.listIterator();
		int j = -1;
		if (exact) {
			j = StringUtils.findExactMatchIndex(iter, param);
		} else {
			j = StringUtils.findMatchIndex(iter, param);
		}
		
		if (j >= 0) {
			indexItem[0] = String.valueOf(j);
			indexItem[1] = (String) iter.previous();
		} else {
			Log.debug(debugmsg+" Can not find item "+param+" in list.");
			throw new SAFSException("Can not find item "+param+" in list");
		}
		
		return indexItem;
	}
	
	/**
	 * <em>Purpose:</em> 	Perform a double click on the item in a list
	 * <em>Note:</em>		Should be called by function without verification, as this method will check the existence of item
	 * @param guiObj		A test object represents a list
	 * @param param			An item text
	 * @param exact			If true, the item will be matched exactly; otherwise, it will be matched partially.
	 * @param mbuttons		A mouse modifier like left, right click etc.
	 * @return				String the matched item in the list
	 * @throws SAFSException
	 */
	protected String performDoubleClick(GuiSubitemTestObject guiObj, String param, boolean exact,MouseModifiers mbuttons) throws SAFSException{
		String debugmsg = getClass().getName() + ".performDoubleClick(): ";
		String[] indexItem = searchForListItem(guiObj, param, exact);
		int index = Integer.parseInt(indexItem[0]);
		Log.debug(debugmsg + " matched item index: " + index);
		
		try{
			if(mbuttons!=null){
				guiObj.doubleClick(mbuttons,Script.localAtText(indexItem[1]));
			}else{
				guiObj.doubleClick(Script.localAtText(indexItem[1]));
			}
		}catch(Exception e){
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			throw new SAFSException(e.getMessage());
		}
		
		return indexItem[1];
	}
	
	/**
	 * <em>Note:</em>	Scroll to the item to make it shown on screen so that click or select can be executed.
	 * @param guiObj	A test object representing a list
	 * @param index		The item index in the list
	 */
	protected boolean scrollToIndex(GuiSubitemTestObject guiObj, int index){
		boolean scrollOK = true;
		
		if(isFlexDomain(guiObj)){
			//scroll to the item to make sure it is shown on the screen
			FlexListTestObject flexList = new FlexListTestObject(guiObj.getObjectReference());
			flexList.scroll(Script.atPosition(index),FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
		}else{
			Log.info("Scrolling to the item is only need for Flex list");
		}
		
		return scrollOK;
	}
	
	/**
	 * <em>Note:</em>	Scroll to the item to make it shown on screen so that click or select can be executed.
	 * @param guiObj	A test object representing a list
	 * @param text		The item in the list
	 */
	protected boolean scrollToText(GuiSubitemTestObject guiObj, String text){
		String debugmsg = getClass().getName()+".scrollToText(): ";
		boolean scrollOK = true;
		
		if(isFlexDomain(guiObj)){
			//scroll to the item to make sure it is shown on the screen
			String[] indexItem = null;
			try {
				indexItem = searchForListItem(guiObj,text,true);
				FlexListTestObject flexList = new FlexListTestObject(guiObj.getObjectReference());
				flexList.scroll(Script.atPosition(Integer.parseInt(indexItem[0])),FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
			} catch (SAFSException e) {
				Log.debug(debugmsg+" Exception: "+e.getMessage());
				scrollOK = false;
			}
		}else{
			Log.info("Scrolling to the item is only need for Flex list");
		}

		return scrollOK;
	}
	
	  /** <br><em>Purpose:</em> clickColumn
	   **/
	  protected void clickColumn (GuiSubitemTestObject guiObj, String param) throws SAFSException{
	  	int index = 0;
	  	
	  	Log.info("param: "+param);
	  	if(action.equalsIgnoreCase(CLICKCOLUMNINDEX)){
		    try {
		      index = Integer.parseInt(param);
		      index = index -1;
		    }catch(Exception x){
		      Log.debug(getClass().getName()+".clickColumn(): index format error: "+param);
		    }
	  	}
	  	
	    try{
	      if (action.equalsIgnoreCase(CLICKCOLUMNINDEX)) {
	    	  Log.info("Index: "+index);
	    	  guiObj.click(Script.atHeader(Script.atIndex(index)));
	      } else if(action.equalsIgnoreCase(CLICKCOLUMNLABEL)) {
	    	  Log.info("Header Label: "+param);
	    	  guiObj.click(Script.atHeader(Script.atText(param)));
	      }
          String altText = param+", "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4, altText, param,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
	    } catch (Exception se) {
	    	Log.debug(se.getMessage());
	    	throw new SAFSException(se.getMessage());
	    }
	  }
}
