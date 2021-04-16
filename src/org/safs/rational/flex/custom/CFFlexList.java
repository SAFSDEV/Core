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
package org.safs.rational.flex.custom;

import org.safs.Log;
import org.safs.SAFSException;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexListTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.script.MouseModifiers;

/**
 * <br>
 * <em>Purpose:</em> CFFlexList, process a SAS Flex List component<br>
 * <em>Note:</em> As in CustomJavaObjectsMap.dat, we have mapped the SASFlexXXX to<br>
 *                same type as the standard-flex-class; So the standard-flex-class<br>
 *                will be processed in this custom-handler. But the standard-flex-class<br>
 *                SHOULD NOT be processed here, it should be processed by the super classes<br>
 *                in package org.safs.rational.flex, so we will test the class-name of the<br>
 *                testObject, if it does not begin will 'com.sas', we will let the super<br>
 *                class to process.<br>
 *                We should DO this in each override method in this class and the other<br>
 *                SAS Flex custom classes, like CFFlexPageTabList, CFFlexTree etc.<br>
 * <p>
 * 
 * @author Lei Wang
 * @since FEB 05, 2011
 **/

public class CFFlexList extends org.safs.rational.flex.CFFlexList{
	/**
	 * <br>
	 * <em>Note:</em> Override the method of its superclass CFFlexList
	 *                call the delegate's action of SAS Flex List to perform 'double click'
	 **/
	protected String performDoubleClick(GuiSubitemTestObject guiObj, String param, 
			                            boolean exact,MouseModifiers mbuttons) throws SAFSException{
		//If the standard FlexList comes here, we should let the super class to handle it, NOT here.
		if(!SASUtil.isSASFlexComponent(obj1)){
			return super.performDoubleClick(guiObj, param, exact, mbuttons);
		}
		
		String debugmsg = getClass().getName() + ".performDoubleClick(): ";
		String[] indexItem = searchForListItem(guiObj, param, exact);
		int index = Integer.parseInt(indexItem[0]);
		Log.debug(debugmsg + " matched item index: " + index);
		FlexListTestObject flexList = new FlexListTestObject(guiObj.getObjectReference());

		TestObject[] children = flexList.getChildren();
		try{
			if(children.length>=index){
				//TODO How we know this is the list item? If there are some separators?
				FlexObjectTestObject listItem = (FlexObjectTestObject) children[index];
				Log.debug(debugmsg+" listItem's class name is "+listItem.getObjectClassName());
				if(mbuttons!=null){
					listItem.doubleClick(mbuttons);
				}else{
					listItem.doubleClick();
				}
			}else{
				//TODO We should call the action "DoubleClick" of delegate for SASFlexList 
				//defined in FlexEnv.xml; But it seems that is does not work,
				//I call action "SelectIndex" instead, this action is not the exact
				//action described in the SAFS Reference.
				Log.debug(debugmsg+" Try to perform action 'DoubleClick' on "+flexList.getObjectClassName());
				flexList.performAction("SelectIndex", String.valueOf(index));
				//I will call the action "DoubleClick" also, if one day it works ...
				flexList.performAction("DoubleClick", String.valueOf(index));
			}
		}catch(Exception e){
			Log.warn(debugmsg+" Exception occur "+e.getMessage()+". Try to perform action 'DoubleClick' on "+flexList.getObjectClassName());
			flexList.performAction("SelectIndex", String.valueOf(index));
			flexList.performAction("DoubleClick", String.valueOf(index));
		}
		return indexItem[1];
	}

}
