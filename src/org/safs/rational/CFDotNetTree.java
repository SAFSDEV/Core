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
package org.safs.rational;

import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.text.FAILStrings;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.script.Value;

/**
 * <br><em>Purpose:</em> 	Process DotNetTree component. 
 * @author  Lei Wang
 * @since   AUG 05, 2008
 *   <br>	AUG 05, 2008	(Lei Wang)	Original Release
 **/

public class CFDotNetTree extends CFTree {

	//System.Windows.Forms.TreeView's properties
	public static final String PROPERTY_PATHSEPARATOR	=			"PathSeparator";
	public static final String PROPERTY_SELECTEDNODE	=			"SelectedNode";
	
	//System.Windows.Forms.TreeNode's properties
	public static final String PROPERTY_FULLPATH		=			"FullPath";
	
	/**
	 * @param guiObj		Represent a TreeView object.
	 * @param testPathList	The path to be tested if it is the selected path.
	 * @return				True if the path is selected; False otherwise.
	 * @throws				SAFSException
	 */
	protected boolean isNodeSelected(GuiSubitemTestObject guiObj,com.rational.test.ft.script.List testPathList)  throws SAFSException{
		String debugmsg = getClass().getName() + ".isNodeSelected() ";
		Subitem[] pathToBeTested = testPathList.getSubitems();

		try{
			String pathSeparator = (String) guiObj.getProperty(PROPERTY_PATHSEPARATOR);
			Log.info(debugmsg+" pathSeparator is "+pathSeparator);
			
			TestObject pathobj = (TestObject) guiObj.getProperty(PROPERTY_SELECTEDNODE);
			if (pathobj == null) {
				Log.debug(debugmsg + " No path has been selected");
				return false;
			}
	
			String selectedPath = (String) pathobj.getProperty(PROPERTY_FULLPATH);
			Log.debug("SELECTED PATH: " + selectedPath);
			
			if (selectedPath == null) {
				Log.debug(debugmsg + " Can not get selected full path");
				return false;
			}

			StringTokenizer selectedNodes = new StringTokenizer(selectedPath,pathSeparator);
			int realPathLength = selectedNodes.countTokens();
			int testPathLength = pathToBeTested.length;
			
			// If root is no visible, pathToBeTested will not contain the root, but
			// selectedPath contains the root, so we need skip the first object of selectedPath
			if (!isRootVisible(guiObj)) {
				//Start from the seconde element in the selectedNodes
				Log.debug(debugmsg + " Root is not visible.");
				if((realPathLength-1)!=testPathLength) return false;
				//skip the first hidden root
				selectedNodes.nextToken();
			} else {
				Log.debug(debugmsg + " Root is visible.");
				if(realPathLength!=testPathLength) return false;
			}
	
			for (int i = 0; i < testPathLength; i++) {
				String node1 = ((Value) pathToBeTested[i]).getValue().toString();
				String node2 = selectedNodes.nextToken();
				Log.debug(debugmsg + " comparing... " + node1 + " and "+ node2);
				if (!node1.equals(node2)) {
					return false;
				}
			}
		}catch(PropertyNotFoundException e1){
			Log.debug(debugmsg+e1.getMessage());
			throw new SAFSException(failedText.text(FAILStrings.PROPERTY_NOT_FOUND, "Can not find property.")+e1.getMessage());
		}catch (MethodNotFoundException e2) {
			Log.debug(debugmsg+e2.getMessage());
			throw new SAFSException(failedText.text(FAILStrings.METHOD_NOT_FOUND, "Can not find method.")+e2.getMessage());
		}
		
		return true;
	}
}
