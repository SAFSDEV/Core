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
package org.safs.rational.wpf;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.CFList;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.WPF.WpfSelectGuiSubitemTestObject;

public class CFWPFList extends CFList {

	public static final String LISTDATA_WPF_RFT_VP_PROPERTY			= "allitems";
	/**
	 * <em>Note:</em> Override the method in its super class CFList
	 * 
	 * @param guiObj	This is the reference to the List object (java or .net or other list)
	 * @return A list contains all items of a listbox
	 * @throws SAFSException
	 */
	protected List captureObjectData(TestObject guiObj) throws SAFSException {
		String debugmsg = getClass().getName() + ".captureObjectData(): ";
		List list = new ArrayList();
		String contents = null;

		String className = guiObj.getObjectClassName();
		Log.info(debugmsg + "className=" + className);

		try {
			// As guiObj is a GuiSubitemTestObject (new instance from CFList),
			// so it can not be cast to
			// WpfSelectGuiSubitemTestObject. But it is really a
			// WpfSelectGuiSubitemTestObject, we must
			// use its ObjectReference to new a WpfSelectGuiSubitemTestObject
			WpfSelectGuiSubitemTestObject listObject = new WpfSelectGuiSubitemTestObject(guiObj.getObjectReference());
			contents = listObject.getText();
			StringTokenizer st = new StringTokenizer(contents, "\n");
			while (st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
		} catch (Exception e) {
			Log.info(debugmsg+ " could NOT extract list of items for .NET WPF application");
			list = getListItems(guiObj, LISTDATA_WPF_RFT_VP_PROPERTY);
		}

		return list;
	}

	/**
	 * <em>Note:</em> 			Override the method in its super class CFList
	 * 
	 * @param partialmatch		if true, then partial match on selected
	 * @param desireSelected	if true, test for selected; otherwise, unselected
	 **/
	protected void verifySelected(GuiSubitemTestObject guiObj, String param,
			boolean partialmatch, boolean desireSelected) {

		String debugmsg = getClass().getName() + ".verifySelected(): ";
		int statusCode = StatusCodes.SCRIPT_NOT_EXECUTED;
		int logMsgCode = FAILED_MESSAGE;
		String logMsg = null;

		String className = guiObj.getObjectClassName();
		Log.info(debugmsg + "className=" + className);

		Log.info("..... param: " + param);
		// ready to do the verify
		List selectedItems = new ArrayList();

		try {
			// As guiObj is a GuiSubitemTestObject (new instance from CFList),
			// so it can not be cast to
			// WpfSelectGuiSubitemTestObject. But it is really a
			// WpfSelectGuiSubitemTestObject, we must
			// use its ObjectReference to new a WpfSelectGuiSubitemTestObject
			WpfSelectGuiSubitemTestObject listObject = new WpfSelectGuiSubitemTestObject(guiObj.getObjectReference());
			String contents = listObject.getSelectedText();
			StringTokenizer st = new StringTokenizer(contents, "\n");
			while (st.hasMoreTokens()) {
				selectedItems.add(st.nextToken());
			}
		} catch (Exception x) {
			Log.info(debugmsg + " Exception occured: " + x.getMessage());
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			// try another engine
			return;
		}

		if (selectedItems.size() == 0) {
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
		} else {
			int i = 0;
			for (;i<selectedItems.size(); i++) {
				// something is selected
				String text = (String) selectedItems.get(i);
				boolean match = false;

				if(partialmatch){
					match = text.indexOf(param) > -1;
				}else{
					match = param.equals(text);
				}

				if (match) {
					if (desireSelected) {
						// verification success
						statusCode = StatusCodes.OK;
						logMsgCode = PASSED_MESSAGE;
						logMsg = passedText.convert(TXT_SUCCESS_4, action,
								windowName, compName, action, param);
					} else {
						// verification fail
						statusCode = StatusCodes.GENERAL_SCRIPT_FAILURE;
						logMsgCode = FAILED_MESSAGE;
						logMsg = getClass().getName() + ": selected item '"
								+ text + "' unexpectedly matched: " + param;
					}
					break;
				}
			}// End for loop

			// We can not find a matched item
			if (i == selectedItems.size()) {
				if (desireSelected) {
					// verification fail
					statusCode = StatusCodes.GENERAL_SCRIPT_FAILURE;
					logMsgCode = FAILED_MESSAGE;
					logMsg = getClass().getName()
							+ ": selected items did not match: " + param;
				} else {
					// verification success
					statusCode = StatusCodes.OK;
					logMsgCode = PASSED_MESSAGE;
					logMsg = passedText.convert(TXT_SUCCESS_4, action,
							windowName, compName, action, param);
				}
			}
		}

		// report results
		log.logMessage(testRecordData.getFac(), logMsg, logMsgCode);
		testRecordData.setStatusCode(statusCode);
	}
	
	/**
	 * <em>Note:</em> 		Override the method in its super class CFList
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
			List items = captureObjectData(guiObj);
			item = (String) items.get(index);
		} catch (Exception ex) {
			Log.info(debugmsg + ex.toString()+ " could NOT extract item "+ index+" for "+ className);
			throw new SAFSException(ex.toString() + " RJ:CFList could NOT extract item "+index);
		}
		
		return item;
	}
}
