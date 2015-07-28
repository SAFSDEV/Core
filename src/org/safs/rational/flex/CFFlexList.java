/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational.flex;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.CFList;
import org.safs.rational.Script;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexListTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.script.FlexScrollDetails;
import com.rational.test.ft.script.FlexScrollDirections;
import com.rational.test.ft.script.MouseModifiers;
import com.rational.test.ft.vp.ITestDataElement;
import com.rational.test.ft.vp.ITestDataElementList;
import com.rational.test.ft.vp.ITestDataList;

/**
 * <br>
 * <em>Purpose:</em> CFFlexList, process a Flex List component
 * <p>
 * 
 * @author Lei Wang
 * @since FEB 18, 2009
 **/

public class CFFlexList extends CFList {

	/**
	 * <em>Note:</em> Override the method of its superclass CFList, support Flex List
	 * 
	 * @param guiObj This is the reference to the List object (java or .net or other list)
	 * @return A list contains all items of a listbox
	 * @throws SAFSException
	 */
	@SuppressWarnings({ "unchecked"})
	protected List captureObjectData(TestObject guiObj) throws SAFSException {
		String debugmsg = getClass().getName() + ".getListContents(): ";
		List list = new ArrayList();

		String className = guiObj.getObjectClassName();
		Log.info(debugmsg + "className=" + className);

		try {
      	  ITestDataList dataList = (ITestDataList) guiObj.getTestData("list");
      	  int elementCount = dataList.getElementCount();
    	  for(int i=0;i<elementCount;i++){
    		  list.add(getListItem(dataList,i));
    	  }
		} catch (Exception ex) {
			Log.info(debugmsg + ex.toString()+ " could NOT extract list of items for "+ guiObj.getObjectClassName());
			throw new SAFSException(ex.toString()+ " RJ:CFFlexList could NOT extract list of items");
		}

		return list;
	}
	
	/**
	 * <br>
	 * <em>Note:</em> Override the method of its superclass CFList
	 */
	protected String getListItem(GuiTestObject guiObj, int index) throws SAFSException{
		String debugmsg = getClass().getName() + ".getListItem(): ";
		String item = "";

		String className = guiObj.getObjectClassName();
		Log.info(debugmsg + "className=" + className);

		try {
			ITestDataList list = (ITestDataList) guiObj.getTestData("list");
			item = getListItem(list,index);
		} catch (Exception ex) {
			Log.info(debugmsg + ex.toString()+ " could NOT extract item "+ index+" for "+ guiObj.getObjectClassName());
			throw new SAFSException(ex.toString() + " RJ:CFFlexList could NOT extract item "+index);
		}
		
		return item;
	}
	
	/**
	 * @param list		ITestDataList, an object returned by RFT API, which contains the list contents
	 * @param index		int, the item's index
	 * @return			String, the item value corresponding to the index
	 */
	protected String getListItem(ITestDataList list, int index){
		String debugmsg = getClass().getName() + ".getListItem(): ";
		String item = "";

		ITestDataElementList el = list.getElements();
		ITestDataElement e = el.getElement(index);
		Object object = e.getElement();
		if (object instanceof String) {
			item = (String) object;
		} else {
			Log.debug(debugmsg + " item's class is "+ item.getClass().getName() + ". Need new implementation.");
		}

		return item;
	}	
	
	/**
	 * <br>
	 * <em>Note:</em> Override the method of its superclass CFList, as FlexListTestObject API does not
	 * 				  provide method to double click on text of list, we have to search the item and 
	 * 				  get the position of this text, then perform a double click on that point. This 
	 * 				  means that we do a verification, but this method is called for keyword without
	 * 				  verification, so there is a conflict :-(
	 **/
	protected void modifiedDoubleClickTextItem(MouseModifiers mbuttons,GuiSubitemTestObject guiObj, String param) throws SAFSException {
		
		try {
			//TODO need to find a way to perform double click without verification
			performDoubleClick(guiObj,param,true,mbuttons);
		} catch (Exception x) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), getClass().getName()+ ": action error on: " + param, FAILED_MESSAGE);
			return;
		}
		String altText = param + ", " + windowName + ":" + compName + " "+ action;
		log.logMessage(testRecordData.getFac(),
				passedText.convert(PRE_TXT_SUCCESS_4, altText, param, windowName,compName, action),
				PASSED_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);
	}

	/**
	 * <br>
	 * <em>Note:</em> Override the method of its superclass CFList, as Flex list test object
	 * 				  does not support doubleClick(atText()), so we have to find the relative
	 * 				  position of the item we want to click at, then call doubleClick(atPoint())
	 **/
	protected String performDoubleClick(GuiSubitemTestObject guiObj, String param, boolean exact,MouseModifiers mbuttons) throws SAFSException{
		String debugmsg = getClass().getName() + ".performDoubleClick(): ";
		String[] indexItem = searchForListItem(guiObj, param, exact);
		int index = Integer.parseInt(indexItem[0]);
		Log.debug(debugmsg + " matched item index: " + index);

		FlexListTestObject flexList = new FlexListTestObject(guiObj.getObjectReference());

		flexList.scroll(Script.atPosition(index),FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
		FlexObjectTestObject listRenderItem = flexList.getAutomationChildAt(0);
		Rectangle r = listRenderItem.getScreenRectangle();
		Point clickPoint = new Point(r.width / 2, r.height / 2);
		
		try{
			if(mbuttons!=null){
				flexList.doubleClick(mbuttons, clickPoint);
			}else{
				flexList.doubleClick(clickPoint);			
			}
		}catch(Exception e){
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			throw new SAFSException(e.getMessage());
		}
		
		return indexItem[1];
	}
	
	
	/**
	 * <em>Note:</em> Override the method of its superclass CFList, But click(AtIndex()) can
	 * 				  not work for flex list, so use click(atText())
	 */
	protected boolean selectItemAtIndex(GuiSubitemTestObject guiObj, int index) {
		String debugmsg = getClass().getName()+".selectItemAtIndex(): ";
		
		try {
			if(!scrollToIndex(guiObj, index)){
				Log.debug(debugmsg+" can not scroll to index "+index);
				return false;
			}
			String itemText = getListItem(guiObj, index);
			guiObj.click(Script.localAtText(itemText));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
