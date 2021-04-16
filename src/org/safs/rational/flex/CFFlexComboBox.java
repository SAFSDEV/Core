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
package org.safs.rational.flex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.rational.CFComboBox;
import org.safs.rational.FlexUtil;
import org.safs.rational.Script;
import org.safs.text.FAILStrings;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexComboBaseTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexComboBoxTestObject;
import com.rational.test.ft.script.CaptionText;
import com.rational.test.ft.script.FlexScrollDetails;
import com.rational.test.ft.script.FlexScrollDirections;

/**
 * <br><em>Purpose:</em> CFFlexComboBox, process a FLEX ComboBox component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Lei Wang
 * @since   FEB 04, 2009
 * 			JUN 24, 2011 (JunwuMa) Update to fix an infinite loop issue in scrolling the ComboBox to get all the items
 * **/
public class CFFlexComboBox extends CFComboBox{
	
	  /** <br><em>Purpose:</em> process: process the testRecordData
	   ** <br>This is our specific version for FLEX test object. We subclass the generic CFComboBox.
	   ** The actions handled here are:
	   ** <br><ul>
	   ** <li>hidelist
	   ** <li>showlist
	   ** <li>select
	   ** <li>selectTextItem
	   ** <li>selectIndex
	   ** <li>selectPartialMatch
	   ** <li>selectUnverified
	   ** <li>selectUnverifiedTextItem
	   ** <li>setTextValue
	   ** <li>setUnverifiedTextValue
	   ** <li>verifySelected
	   ** <li>verifySelectedItem
	   ** </ul><br>
	   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
	   * based on the result of the processing
	   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
	   * <br><em>Assumptions:</em>  none
	   **/
	protected void localProcess(){
		String debugmsg = getClass().getName()+"localProcess(): ";
		Log.debug(debugmsg +" action: "+action+"; win: "+ windowName +"; comp: "+compName);
		
		try{
			if(action.equalsIgnoreCase(HIDELIST)){
        		if(showCombo(false)){
                    log.logMessage(testRecordData.getFac()," "+action+" ok", PASSED_MESSAGE);
                    testRecordData.setStatusCode(StatusCodes.OK);
        		}else{
        			log.logMessage(testRecordData.getFac()," "+action+" failed", FAILED_MESSAGE);
        			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        		}
			}else if(action.equalsIgnoreCase(SHOWLIST)){
        		if(showCombo(true)){
                    log.logMessage(testRecordData.getFac()," "+action+" ok", PASSED_MESSAGE);
                    testRecordData.setStatusCode(StatusCodes.OK);
        		}else{
        			log.logMessage(testRecordData.getFac()," "+action+" failed", FAILED_MESSAGE);
        			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        		}
			}else if (action.equalsIgnoreCase(SELECT) ||
		            action.equalsIgnoreCase(SELECTTEXTITEM) ||
		            action.equalsIgnoreCase(SELECTINDEX) ||
		            action.equalsIgnoreCase(SELECTPARTIALMATCH) ||
		            action.equalsIgnoreCase(SELECTUNVERIFIED) ||
		            action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)) {
				if (params.size() < 1) {
					paramsFailedMsg(windowName, compName);
				} else {
					String param = (String) params.iterator().next();
					Log.info(debugmsg + " ..... param: " + param);

					// SELECTINDEX
					if (action.equalsIgnoreCase(SELECTINDEX)) {
						int index = 0;
						try {
							Integer pi = new Integer(param);
							index = pi.intValue();
						} catch (NumberFormatException nfe) {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), getClass().getName()+ ": invalid index format: " + param,FAILED_MESSAGE);
						}
						if (index < 1) {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), getClass().getName()+ ": bad index (less than 1): " + param,FAILED_MESSAGE);
						}
						// component item index is 0-based
						if (selectItemAtIndex(obj1, index - 1)) {
							// hideCombo(guiObj);
							log.logMessage(testRecordData.getFac(), " " + action + " ok at index : " + index, PASSED_MESSAGE);
							testRecordData.setStatusCode(StatusCodes.OK);
						} else {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item index may be out of range: "+ index, FAILED_MESSAGE);
						}
					}else if(action.equalsIgnoreCase(SELECT) ||
							 action.equalsIgnoreCase(SELECTTEXTITEM)){
						if (selectItemAtText(obj1,param,true)) {
							log.logMessage(testRecordData.getFac(), " " + action + " ok at text : " + param, PASSED_MESSAGE);
							testRecordData.setStatusCode(StatusCodes.OK);
						} else {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), getClass().getName()+ ": can not select text: "+ param, FAILED_MESSAGE);
						}
					}else if(action.equalsIgnoreCase(SELECTUNVERIFIED) ||
							 action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM) ||
							 action.equalsIgnoreCase(SELECTPARTIALMATCH)){
						if (selectItemAtText(obj1,param,false)) {
							log.logMessage(testRecordData.getFac(), " " + action + " ok at text : " + param, PASSED_MESSAGE);
							testRecordData.setStatusCode(StatusCodes.OK);
						} else {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), getClass().getName()+ ": can not select text: "+ param, FAILED_MESSAGE);
						}						
					}
				}
			}
		
			//If the record is not executed, let its superclass CFComboBox to treat
			if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED){
				Log.debug(getClass().getName()+".localProcess(): can not treate action "+action);		
				super.localProcess();
			}
		}catch(Exception ex){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String alttext = "Unable to perform "+action+" on "+compName+" in "+windowName+".";
			String message = failedText.convert(FAILStrings.FAILURE_3, alttext, windowName,compName,action);
			log.logMessage(testRecordData.getFac(),message+" : "+ex.getMessage(),FAILED_MESSAGE);
		}
	}
	
	/**
	 * @param show		boolean, try to make drop down list showing if true; otherwise try to hide the drop down list.
	 * @return			boolean, if action is executed successfully then return true.
	 */
	protected boolean showCombo(boolean show){
		String debugmsg = getClass().getName()+".showCombo(): ";
		
		if(obj1 instanceof FlexComboBaseTestObject){
			FlexComboBaseTestObject comboBase = new FlexComboBaseTestObject(obj1.getObjectReference());
			if(show){
				comboBase.open();
			}else{
				comboBase.close();
			}
		}else{
			Log.debug(debugmsg+" this flex object is "+obj1.getObjectClassName()+". It can not be processed as  FlexComboBoxTestObject.");
			return false;
		}
		
		return true;
	}
	
	  /** <br><em>Purpose:</em> extract items from a ComboBox object. This method override that of superclass CFComboBox.
	   * 
	   * @param guiObj, which refers to the ComboBox.
	   * @return java.util.List, all items of the ComboBox are in it. 
	   * @exception SAFSException
	   */
	@SuppressWarnings("unchecked")
	protected List getItems(TestObject guiObj)throws SAFSException{
		  String debugmsg = getClass().getName()+".getItems(): ";	  
	      List list = null;
	      Log.info("...CFComBox is extracting list of items: ");

	      String classname = guiObj.getObjectClassName();
	      try {
	    	  Log.debug(debugmsg+" guiObj's class is "+classname);
	    	  //As flex combobox test object is not GuiSubitemTestObject, so we can not use method extractListItems() of 
	    	  //class RDDGUIUtilities.
	  		if(guiObj instanceof FlexComboBaseTestObject){
				FlexComboBaseTestObject comboBase = new FlexComboBaseTestObject(obj1.getObjectReference());
				//Flex ComboBox has a property rowCount which indicate the number of visible items,
				//but getProperty("rowCount") will not return a correct value !!!!
				//We will get the visible items and store them to list, then scroll to next visible items and get and store them ...
				
				comboBase.scroll(Script.atPosition(0), FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
				TestObject[] children = comboBase.getChildren();
		    	int visibleRows = children.length;
		    	int page = 0;
		    	
				if(visibleRows>0){
					list = new ArrayList();
					
					TestObject firstChildOnPreviousPage = null;
			    	while(true){
						if (children.length>0)
							firstChildOnPreviousPage = children[0]; 
			    		for(int i=0;i<children.length;i++){
				    		  list.add(children[i].getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME));
						}
						if(children.length < visibleRows){
							//This means that there are no more items in this ComboBox.
							break;
						}
						page++;
						comboBase.scroll(Script.atPosition(visibleRows*page), FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
						children = comboBase.getChildren();
						
						//the children have no change if the new position to scroll to is beyond the range
						//in this case, children.length is equal to visibleRows, exit the loop
						//not find other way to get the total item number, use this instead.
						if (children.length == visibleRows) 
							if (firstChildOnPreviousPage.equals(children[0]))
								break;

			    	}		
				}
			}else{
				Log.debug(debugmsg+" this flex object is "+obj1.getObjectClassName()+". It can not be processed as  FlexComboBoxTestObject.");
				throw new SAFSException("This Flex ComboBox is not subclass of FlexComboBoxTestObject.");
			}	    	  
	      }catch(Exception ex){
	  		  Log.info("RJ:CFComboBox could NOT extract list of items on Domain, "+guiObj.getDomain().toString());
	    	  throw new SAFSException(ex.toString());
	      }
	      return list;
	}
	
	 /**
	 * <br>
	 * <em>Purpose:</em> setTextValue
	 * <br>
	 * This method override that of it's superclass CFComboBox, implemented special for FLEX ComboBox.
	 **/
	protected void setTextValue(boolean performVerification) throws SAFSException {
		String debugmsg = getClass().getName()+"setTextValue(): ";
		
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			Log.debug(debugmsg+" params: "+params);

			Iterator piter = params.iterator();
			String newValue = (String) piter.next();
			Log.info(debugmsg+" ...value to set: " + newValue);

			try {
		  		if(obj1 instanceof FlexComboBaseTestObject){
					FlexComboBaseTestObject comboBase = new FlexComboBaseTestObject(obj1.getObjectReference());
					//comboBase.inputKeys("{extEND}+{extHOME}{extDELETE}" + newValue + "{TAB}");
					//{extEND}, +, {extHOME} do not work for method inputKeys() of FlexComboBaseTestObject
					//but the method input() will clear the text and input the new value to ComboBox
					comboBase.input(newValue);
		  		}else{
					Log.debug(debugmsg+" this flex object is "+obj1.getObjectClassName()+". It can not be processed as  FlexComboBoxTestObject.");
					throw new SAFSException("This Flex ComboBox is not subclass of FlexComboBoxTestObject.");
		  		}
			} catch (Exception x) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(), getClass().getName() + ": General Error in ComboBox SetTextValue.", FAILED_MESSAGE);
				return;
			}

			// verification
			if (performVerification && !StringUtils.containsSepcialKeys(newValue)) {
				if (verifyTextProperty(obj1,newValue)) {
					log.logMessage(testRecordData.getFac(), windowName + ":" + compName + " " + action
							+ " new verified value is '" + newValue + "'", PASSED_MESSAGE);
				}
			} else {
				testRecordData.setStatusCode(StatusCodes.OK);
				log.logMessage(testRecordData.getFac(), windowName + ":"
						+ compName + " " + action + " performed.", PASSED_MESSAGE);
			}
		}
	}
	
	/**
	 * <br>
	 * <em>Purpose:</em> verifyTextProperty
	 ** <p>
	 * <br>
	 * Can be called after setting the ComboBox's value to ensure that the <br>
	 * change has taken place.
	 **/
	private boolean verifyTextProperty(TestObject guiObj, String param) {
		String value = null;

		value = guiObj.getProperty("text").toString();

		return compareValues(value, param);
	}
	  
	/**
	 * <em>Purpose:</em>
	 **/
	protected void verifySelected() throws SAFSException {
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String param = (String) params.iterator().next();
			Log.info("..... param: " + param);
			// ready to do the verify
			FlexComboBaseTestObject comboBoxObject = new FlexComboBaseTestObject(obj1.getObjectReference());
			if (verifySelectedText(comboBoxObject, param)) {
				log.logMessage(testRecordData.getFac(), passedText.convert(
						TXT_SUCCESS_4, action, windowName, compName, action,
						param), PASSED_MESSAGE);
			}
		}
	}

	/**
	 * <br>
	 * <em>Purpose:</em> verifySelectedText
	 ** <p>
	 * <br>
	 * Can be called after setting the combo box value to ensure that the <br>
	 * change has taken place. Also called by the VERIFYSELECTED <br>
	 * command / verifySelected() method.
	 ** 
	 **/
	private boolean verifySelectedText(FlexComboBaseTestObject comboBoxObject, String param) {
		String debugmsg = getClass().getName() + ".verifySelectedText() ";
		String value = null;

		String selectIndexProperty = "selectedIndex";

		int j = Integer.parseInt((String)comboBoxObject.getProperty(selectIndexProperty));
		if (j >= 0) {
			//selectedIndex is counted from the first item of this ComboBox
			//but method getAutomationChildAt(i) will return the ith visible item of ComboBox
			//so we must scroll to the selected item, then the item returned by getAutomationChildAt(0)
			//will be the selected item.
			comboBoxObject.scroll(Script.atPosition(j), FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
			Object n = comboBoxObject.getAutomationChildAt(0);
			if (n instanceof GuiTestObject) {
				GuiTestObject selectedObject = (GuiTestObject) n;
				String textProperty = FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME;

				try {
					Object textObject = selectedObject.getProperty(textProperty);
					if (textObject instanceof String) {
						value = (String) textObject;
					} else if (textObject instanceof CaptionText) {
						value = ((CaptionText) textObject).getCaption();
					} else {
						Log.debug(debugmsg + " textObject is class of "+textObject.getClass().getName()+". Need other way to get it's text value.");
					}
				} catch (PropertyNotFoundException e) {
					Log.debug(debugmsg + " property " + textProperty + " can not be found for " + selectedObject.getObjectClassName());
				} finally {
					selectedObject.unregister();
				}
			} else if (n instanceof String) {
				value = (String) n;
			} else {
				Log.debug(debugmsg + " subitem " + j + " is " + n.getClass().getName() + ". Did not get it's string value");
			}

			// if the value is null, we will try other way to assign it
			if (value == null) {
				Log.debug(debugmsg + " Looking for property selectedItem ... ");
				Object obj = comboBoxObject.getProperty("selectedItem");
				if (obj instanceof String) {
					value = obj.toString();
				} else {
					Log.debug(debugmsg + " SelectedItem " + j + " is " + obj.getClass().getName()+ ". Did not get it's string value");
				}
			}

			return compareValues(value, param);
		} else {
			return verifyTextProperty(comboBoxObject, param);
		}
	}
	
	/**
	 * select the item whose index is the same as parameter index
	 */
	private boolean selectItemAtIndex(TestObject comboboxObject, int index) {
		String debugmsg = getClass().getName()+".selectItemAtIndex(): ";
		
		try {
			if(comboboxObject instanceof FlexComboBoxTestObject){
				FlexComboBoxTestObject flexCombobox = new FlexComboBoxTestObject(comboboxObject.getObjectReference());
				//Need to scroll to that item firstly, otherwise if the item is not visible, it can not be selected.
				flexCombobox.scroll(Script.atPosition(index), FlexScrollDirections.SCROLL_VERTICAL, FlexScrollDetails.THUMBPOSITION);
				flexCombobox.select(index);
			}else{
				Log.debug(debugmsg+" comboboxObject's class is "+comboboxObject.getObjectClassName()+". Not supported yet, need new implementation.");
				return false;
			}
		} catch (Exception e) {
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * select the item whose text is the same as parameter text
	 */
	private boolean selectItemAtText(TestObject comboboxObject, String text,boolean verify) {
		String debugmsg = getClass().getName()+".selectItemAtText(): ";
		boolean selectedOk = true;
		
		try {
			if(comboboxObject instanceof FlexComboBoxTestObject){
				FlexComboBoxTestObject flexCombobox = new FlexComboBoxTestObject(comboboxObject.getObjectReference());

				//We must find the index of the text to be selected in the ComboBox and
				//scroll to that item, otherwise the method select() of FlexComboBoxTestObject can not work
				//so we do the match work firstly
				List list = getItems(flexCombobox);
				Log.info(debugmsg + " list: " + list);
				// do the work of matching...
				ListIterator iter = list.listIterator();
				int j = StringUtils.findMatchIndex(iter, text);
				if (j >= 0) {
					String match = (String) iter.previous();
					Log.info("match: " + j + ", " + match);

					try {
						// Try to select the matched text
						//Need to scroll to that item firstly, otherwise if the item is not visible, it can not be selected.
						flexCombobox.scroll(Script.atPosition(j),FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
						flexCombobox.select(match);
					} catch (Exception e) {
						// Try to select the matched index
						selectedOk = selectItemAtIndex(flexCombobox, j);
					}
				} else {
					Log.debug(debugmsg + " can not find matched item for "+ text);
					selectedOk = false;
				}
				
				if(verify){
					selectedOk = verifySelectedText(flexCombobox,text);
				}
			}else{
				Log.debug(debugmsg+" comboboxObject's class is "+comboboxObject.getObjectClassName()+". Not supported yet, need new implementation.");
				selectedOk = false;
			}
		} catch (Exception e) {
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			selectedOk = false;
		}
		return selectedOk;
	}
}
