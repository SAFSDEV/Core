package org.safs.rational.flex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.safs.rational.CFPageTabList;
import org.safs.rational.FlexUtil;
import org.safs.rational.Script;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexTabNavigatorTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexButtonBarTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexNavigationBarTestObject;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.script.Index;
import org.safs.*;


/**
 * <br><em>Purpose:</em> CFFlexPageTabList, process a FLEX PageTabList component (FlexToggleButtonBar)
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  JunwuMa
 * @since   FEB 17, 2008
 *  <br>	MAY 31, 2011	(JunwuMa) Add captureObjectData(), update to support FlexNavigationBarTestObject. 
 *   								     
 **/
public class CFFlexPageTabList extends CFPageTabList {
	
	/** Overrides the superclass to execute ClickTab,MakeSelection,SelectTab,SelectTab and SelectTabIndex 
	 *  on a FLEX PageTabList component (FlexToggleButtonBar).
	 */
	protected void commandWithOneParam () throws SAFSException {
		String debugMsg = getClass().getName()+".commandWithOneParam(): ";
        Log.info(debugMsg + ".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
		
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String param = (String) params.iterator().next();
		    Log.info(debugMsg + "...param: " + param);
		      
		    if (action.equalsIgnoreCase(CLICKTAB)||
		        action.equalsIgnoreCase(UNVERIFIEDCLICKTAB)||
		        action.equalsIgnoreCase(SELECTTAB) ||
		        action.equalsIgnoreCase(MAKESELECTION)) {
		    	clickTabPartialMatch(obj1, param, true);
		    	return;
		    } else if (action.equalsIgnoreCase(CLICKTABCONTAINS)) {
		    	clickTabPartialMatch(obj1, param, false);
		    	return;
		    } else if (action.equalsIgnoreCase(SELECTTABINDEX)) {
		    	int theIndex = 0;
		    	String paramName = "TabIndex";
		    	try {
		    		theIndex = Integer.parseInt(param);
		    	} catch(NumberFormatException nfe){
		    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		            String detail = failedText.convert("bad_param", "Invalid parameter value for "+ paramName, paramName);
		            componentFailureMessage(detail + "=" + param);
		            return;
		    	}

		    	// treat all tabs as 1-based even if they are 0-based
		    	if (theIndex < 1) {    			  	
		    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			        String detail = failedText.convert("bad_param", "Invalid parameter value for "+ paramName, paramName);
			        componentFailureMessage(detail + "=" + param);
			        return;
		    	}
			    if (obj1 instanceof FlexNavigationBarTestObject) {
			    	FlexObjectTestObject tabButton = ((FlexNavigationBarTestObject)obj1).getAutomationChildAt(theIndex-1);
			    	if (tabButton != null)
			    		tabButton.click();
			    	else
			    		Log.debug("Index is out of the range");
			    } else if (obj1 instanceof FlexTabNavigatorTestObject) {
			    	((FlexTabNavigatorTestObject)obj1).click(Script.localAtIndex(theIndex-1));
			    } else {
			    	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			        String detail = failedText.convert("failure2", "Unable to perform "+ action +" on "+ compName, action, compName);
			        componentFailureMessage(detail);
			        return;
			    }
		    }
		    // set status to ok
		    String altText = windowName+":"+compName+" "+action +" successful using "+ param;
		    log.logMessage(testRecordData.getFac(),
		                   passedText.convert("success3a", altText, 
		                                      windowName, compName, action, param),
		                    PASSED_MESSAGE);
		    testRecordData.setStatusCode(StatusCodes.OK);
	    }
	}
	/**
	 * Click a tab by its text shown on the GUI. 
	 * @param guiObj		tabNavigator object in Flex 
	 * @param tabText		the text to be clicked in the tabPage
	 * @param exact        	true: exact match, false: partial match
	 */
	protected void clickTabPartialMatch(TestObject guiObj, String tabText, boolean exact) {
		String itemName = null;
    	if (guiObj instanceof FlexNavigationBarTestObject) {
    		try {
    			itemName = getAutomationNameOfMatchedTab((FlexNavigationBarTestObject)guiObj, tabText, exact);
    		}catch (SAFSException se) {}
    		if (itemName != null) {
    			((FlexNavigationBarTestObject)guiObj).change(itemName);
    			return;
    		} else 
    			Log.debug("No tab found with parameter '" + tabText + "'");	
    	} else if (guiObj instanceof FlexTabNavigatorTestObject) {
    		((FlexTabNavigatorTestObject)guiObj).click(Script.atText(itemName));
    		return;
		} else {
			Log.debug("No support for " + guiObj.getObjectClassName());
		}
    	
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		String detail = failedText.convert("failure2", "Unable to perform "	+ action + " on " + compName, action, compName);
		componentFailureMessage(detail);
		return;
	}
	
	/**
	 * Overrides its super for Flex TabControl.
	 */
	protected java.util.List captureObjectData(TestObject guiObj)throws SAFSException {
		Log.info("CFFlexPageTabList.captureObjectData(): attempting to get title for its every table page...");
		java.util.List result = null;
		if(isFlexDomain(guiObj)) {
			result = new ArrayList();
			if (guiObj instanceof FlexNavigationBarTestObject) {
				TestObject children[] = guiObj.getChildren();
				// its children should be FlexButton, which "label" are shown as tab page's title
				for (int i = 0; i < children.length; i++) {
					String val = (String) children[i].getProperty("label");
					result.add(val);
				}
			} else if (guiObj instanceof FlexTabNavigatorTestObject) {
				String msg = "CFFlexPageTabList could NOT extract tab pages' title for " + guiObj.getObjectClassName();
				Log.debug(msg);
				throw new SAFSException(msg);
			}
			return result;
		} else {
			return super.captureObjectData(guiObj);
		}	
	}
	/**
	 * get the item which property 'label' matches tabText, and return its automationName with which RFT can perform right actions.
	 * @param guiObj		tabNavigator object in Flex 
	 * @param tabText		the text to be clicked in the tabPage
	 * @param exact        	true: exact match, false: partial match          
	 * @return the automationName of match item
	 * @throws SAFSException
	 */
	protected String getAutomationNameOfMatchedTab(FlexObjectTestObject guiObj, String tabText, boolean exact)throws SAFSException {
		List items = captureObjectData(guiObj);
		String item = null;
		int i;
		for (i = 0; i < items.size(); i++) {
			item = items.get(i).toString();
			if (exact) {
				if (item.equals(tabText)) 
					break;
			} else {
				if (item.indexOf(tabText) > -1)
					break;
			}
		}
		if (i != items.size()) //return automationName
			return (String)guiObj.getAutomationChildAt(i).getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
		Log.debug("CFFlexPageTabList.getAutomationNameOfMatchedTab(): No matched tab found");
		return null;
	}
}