/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.*;
import java.util.List;

import org.safs.*;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

/**
 * <br><em>Purpose:</em> CFPageTabList, process a PageTabList component (tabbedPane)
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 19, 2003
 *
 *   <br>   JUN 19, 2003    (DBauman) Original Release
 *   <br>   OCT 17, 2005    (CANAGL) Click no longer supported here. Use ClickTab or SelectTab.
 *                                   Generic Click is supported through CFComponent.
 *   <br>   MAY 8,  2008    (JunwuMa) Add UnverifiedClickTab
 *   <br>	APR 1,  2009	(JunwuMa) Add captureObjectData() for DotNet TabControl.
 *   <br>	JAN 26, 2011	(JunwuMa) Rename UnverifiedClickTabContains as ClickTabContains for the conflict between 'Unverified'
 *   								  and 'Contains'. Fix an UnsupportedSubitemException thrown by this keyword.  
 **/
public class CFPageTabList extends CFComponent {

  public static final String CLICKTAB                         = "ClickTab";
  public static final String SELECTTAB                        = "SelectTab";
  public static final String SELECTTABINDEX                   = "SelectTabIndex";
  public static final String MAKESELECTION                    = "MakeSelection";
  public static final String UNVERIFIEDCLICKTAB               = "UnverifiedClickTab";
  public static final String CLICKTABCONTAINS       		  = "ClickTabContains";

  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFPageTabList () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The types of objects handled here are '{@link GuiSubitemTestObject}'.
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
        Log.info(".....CFPageTabList.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CLICKTAB) ||
            action.equalsIgnoreCase(SELECTTAB) ||
            action.equalsIgnoreCase(SELECTTABINDEX) ||
            action.equalsIgnoreCase(MAKESELECTION) ||
            action.equalsIgnoreCase(UNVERIFIEDCLICKTAB) ||
            action.equalsIgnoreCase(CLICKTABCONTAINS)) {
          commandWithOneParam();
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
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          "SAFSException: "+ex.getMessage(),
          FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> commandWithOneParam: process commands like: click, doubleclick,
   ** expand and collapse; ones which take one parameter, (the path or which tab).
   ** example:
   ** tabbedPanePageTabList().click(atText("Details"));
   * @param                     action, String  (i.e. expand)
   * @param                     script, Script
   * @param                     compName, String
   * @param                     utils, DDGUtilsInterface
   **/
  protected void commandWithOneParam () throws SAFSException {
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      String t = (String) params.iterator().next();
      Log.info("... "+ action +" param: "+t);
      //we had to use our own version of 'Script' because 'atPath' was protected
      com.rational.test.ft.script.Text text = Script.localAtText(t);
      
      if (action.equalsIgnoreCase(CLICKTAB)||
          action.equalsIgnoreCase(UNVERIFIEDCLICKTAB)) {
          // CLICKTAB action is not verified. it may be implemented in future.  --Junwu
          guiObj.click(text);
      } else if (action.equalsIgnoreCase(SELECTTAB) ||
                 action.equalsIgnoreCase(MAKESELECTION)) {
    	  guiObj.setState(Action.select(), text);
      } else if (action.equalsIgnoreCase(SELECTTABINDEX)){
    	  int theIndex = 0;
    	  String paramName = "TabIndex";
    	  try{
    		  theIndex = Integer.parseInt(t);
    		  // treat all tabs as 1-based even if they are 0-based
    		  if (theIndex < 1) {    			  	
	                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	                String detail = failedText.convert("bad_param", "Invalid parameter value for "+ paramName, paramName);
	                componentFailureMessage(detail+"="+t);
	                return;
    		  }
    	  }catch(NumberFormatException nfe){
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              String detail = failedText.convert("bad_param", "Invalid parameter value for "+ paramName, paramName);
              componentFailureMessage(detail+"="+t);
              return;
    	  }
    	  // convert 1-based index to 0-based index via -1
    	  try{ guiObj.click(Script.localAtIndex(theIndex -1));}
    	  // what if index is not valid?
    	  catch(Exception x){
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              String detail = failedText.convert("failure2", "Unable to perform "+ action +" on "+ compName, action, compName);
              componentFailureMessage(detail+": "+x.getMessage());
              return;
    	  }
      }else if(action.equalsIgnoreCase(CLICKTABCONTAINS)){
			String name = getMatchedItem(guiObj,t);
			//Script.localAtName throws UnsupportedSubitemException if the argument is Chinese characters
			Log.info("... clicking at tab text:" + name);
			guiObj.click(Script.localAtText(name)); 
	  }
      // set status to ok
      String altText = windowName+":"+compName+" "+action +" successful using "+ t;
      log.logMessage(testRecordData.getFac(),
                     passedText.convert("success3a", altText, 
                                        windowName, compName, action, t),
                     PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    }
  }
  
  /**
   * Overrides its super for DotNet TabControl. Forward to super for all other cases.  
   */
  protected java.util.List captureObjectData(TestObject guiObj)throws IllegalArgumentException, SAFSException{
	  String debugMsg =  getClass().getName() + ".captureObjectData(): ";  
	  Log.info(debugMsg + "attempting to get title for its every table page...");
	  
	  String classname = guiObj.getObjectClassName();
	  Log.info("...className:" + classname);
	  
	  if (isDotnetDomain(guiObj)) {
		  // suppose the class is System.Windows.Forms.TabControl
		  java.util.List result = new LinkedList();
	      Integer count = (Integer) guiObj.getProperty("TabCount");;
	      for (int i=0; i<count.intValue(); i++) {
	    	  try {
	    		  Object[] params_1 = new Object[1];
	    		  params_1[0]=new Integer(i);
	    		  TestObject tabPageObj = (TestObject)guiObj.invoke("GetTabPage", "(I)LSystem.Object;", params_1);
			      String val = (String)tabPageObj.getProperty("Text");
		          result.add(val);
	  		  } catch (Exception ex) {
	  			  String msg = debugMsg + ex.toString();
	  			  Log.debug(msg);
				  throw new SAFSException(msg);
			  }
	      }	
	      return result;   
	  }
	  else
		  return super.captureObjectData(guiObj);
  }

  private String getMatchedItem(TestObject guiObj, String itemPartialName)throws IllegalArgumentException, SAFSException{
	  String itemName = null;
	  List items = captureObjectData(guiObj);
	  String item = null;
	  for(int i=0;i<items.size();i++){
		  item = items.get(i).toString();
		  if(item.indexOf(itemPartialName)>-1){
			  itemName = item;
			  break;
		  }
	  }
	  
	  return itemName;
  }
}


