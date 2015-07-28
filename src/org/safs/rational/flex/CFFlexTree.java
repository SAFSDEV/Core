/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational.flex;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.rational.CFTree;
import org.safs.text.FAILStrings;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexTreeTestObject;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.script.Value;

public class CFFlexTree extends CFTree {
	public static final String PROPERTY_SELECTEDINDEX = "selectedIndex";
	public static final String PROPERTY_SELECTEDINDICES = "selectedIndices";
	public static final String PROPERTY_AUTOMATIONNAME = "automationName";
	// Not sure if this separator will change in future in the automationName  of treenode
	public static final String TREE_ITEMNAME_SEPARATOR = ">";

	/**
	 * This override the method of its superclass CFTree
	 */
	protected boolean isRootVisible(GuiSubitemTestObject guiObj) {
		String debugmsg = getClass().getName() + ".isRootVisible(): ";

		try {
			String rootvisible = guiObj.getProperty("showRoot").toString();
			if (rootvisible.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (PropertyNotFoundException pnfe) {
			Log.info(debugmsg + ": " + pnfe.getMessage());
			Log.info(debugmsg + ": returning true; assume that root node is visible.");
			return true;
		}
	}

	/**
	 * @param guiObj 		Represent a TreeView object.
	 * @param testPathList 	The path to be tested if it is the selected path.
	 * @return 				True if the path is selected; False otherwise.
	 * @throws SAFSException
	 */
	protected boolean isNodeSelected(GuiSubitemTestObject guiObj,
			com.rational.test.ft.script.List testPathList) throws SAFSException {
		String debugmsg = getClass().getName() + ".isNodeSelected() ";
		Subitem[] pathToBeTested = testPathList.getSubitems();

		try {
			FlexObjectTestObject flexTree = new FlexObjectTestObject(guiObj.getObjectReference());
			String selectedIndex = (String) flexTree.getProperty(PROPERTY_SELECTEDINDEX);
			Log.info(debugmsg + " selectedIndex is " + selectedIndex);

			// className: mx.controls.treeClasses.TreeItemRenderer,
			// automationName: root>SecondNode>FirstChild
			TestObject pathobj = (TestObject) flexTree.getAutomationChildAt(Integer.parseInt(selectedIndex));
			if (pathobj == null) {
				Log.debug(debugmsg + " No path has been selected");
				return false;
			}

			String selectedPath = (String) pathobj.getProperty(PROPERTY_AUTOMATIONNAME);
			Log.debug("SELECTED PATH: " + selectedPath);

			if (selectedPath == null) {
				Log.debug(debugmsg + " Can not get selected full path");
				return false;
			}

			StringTokenizer selectedNodes = new StringTokenizer(selectedPath,TREE_ITEMNAME_SEPARATOR);
			int realPathLength = selectedNodes.countTokens();
			int testPathLength = pathToBeTested.length;

			// If root is no visible, pathToBeTested will not contain the root,
			// For Flex tree, the selectedPath is got from automationName who
			// will not contain the root,
			// so no need to remove the first part from the selectedPath
			if (!isRootVisible(guiObj)) {
				Log.debug(debugmsg + " Root is not visible.");
			} else {
				Log.debug(debugmsg + " Root is visible.");
			}

			if (realPathLength != testPathLength)
				return false;

			for (int i = 0; i < testPathLength; i++) {
				String node1 = ((Value) pathToBeTested[i]).getValue().toString();
				String node2 = selectedNodes.nextToken();
				Log.debug(debugmsg + " comparing... " + node1 + " and "+ node2);
				if (!node1.equals(node2)) {
					return false;
				}
			}
		} catch (PropertyNotFoundException e1) {
			Log.debug(debugmsg + e1.getMessage());
			throw new SAFSException(failedText.text(
					FAILStrings.PROPERTY_NOT_FOUND, "Can not find property.")
					+ e1.getMessage());
		} catch (MethodNotFoundException e2) {
			Log.debug(debugmsg + e2.getMessage());
			throw new SAFSException(failedText.text(
					FAILStrings.METHOD_NOT_FOUND, "Can not find method.")
					+ e2.getMessage());
		}

		return true;
	}

	/**
	 * @param testObjct 	TestObject The tree test object
	 * @param path 			String The tree path to be expanded or collapsed
	 * @param expand 		boolean If true, expand the tree path; otherwise collapse the tree path
	 * @throws SAFSException
	 */
	protected void doExpand(TestObject testObjct, com.rational.test.ft.script.List path, boolean expand)
			throws SAFSException {
		String debugmsg = getClass().getName() + ".doExpand(): ";
		FlexTreeTestObject flexTree = new FlexTreeTestObject(testObjct.getObjectReference());

		try {
			if (expand) {
				flexTree.expand(path);
			} else {
				flexTree.collapse(path);
			}
		} catch (SubitemNotFoundException e) {
			Log.debug(debugmsg + " Exception: " + e.getMessage());
			throw new SAFSException(e.getMessage());
		}
	}
	
	/**
	 * Note:	It seems that double click has no effect on a flex tree node.
	 * @param testObjct 	TestObject The tree test object
	 * @param path 			String The tree path to be double clicked
	 * @throws SAFSException
	 */	
	protected void doDoubleClick(TestObject testObjct, com.rational.test.ft.script.List path) throws SAFSException {
		String debugmsg = getClass().getName() + ".doDoubleClick(): ";

		try {
			FlexTreeTestObject flexTree = new FlexTreeTestObject(testObjct.getObjectReference());
			
			//As there is no method doubleClick(SubItem) for FlexTreeTestObject()
			//I have to do a single click to let the item selected, then use doubleClick(Point) to do the work.
			//The Point must be relative to the tree object
			flexTree.click(path);
			Object selectedIndexObject =  flexTree.getProperty(PROPERTY_SELECTEDINDEX);
			Log.info(debugmsg +selectedIndexObject.toString());
			int selectedIndex = 0;

			if(selectedIndexObject instanceof String){
				selectedIndex = Integer.parseInt((String) selectedIndexObject);
			}else{
				Log.debug(debugmsg+" selectedIndexObject is " + selectedIndexObject.getClass().getName()+", it needs to be converted to int.");
				throw new SAFSException("Need to convert "+selectedIndexObject.getClass().getName()+" to int.");
			}
			
			//Get the selected child, mx.controls.treeClasses.TreeItemRenderer
			GuiTestObject pathobj = (GuiTestObject) flexTree.getAutomationChildAt(selectedIndex);
			if (pathobj == null) {
				Log.debug(debugmsg + " No path has been selected");
				throw new SAFSException("No path has been selected");
			}
			//Calculate the selected item's relative position to the tree
			//'x' and 'y' are properties of mx.core.UIComponent, they represent 
			//the component's horizontal and vertical position, in pixels, within its parent container.
			Object xObj = pathobj.getProperty("x");
			Object yObj = pathobj.getProperty("y");
			int x = Integer.parseInt((String)xObj)+5;
			int y = Integer.parseInt((String)yObj)+5;
			Point clickPoint = new Point(x,y);
			Log.debug(debugmsg+" double click at point: "+clickPoint);
			flexTree.doubleClick(clickPoint);
		}catch (PropertyNotFoundException e1){
			Log.debug(debugmsg + e1.getMessage());
			throw new SAFSException(e1.getMessage());
		}catch (SubitemNotFoundException e) {
			Log.debug(debugmsg + " Exception: " + e.getMessage());
			throw new SAFSException(e.getMessage());
		} 
	}
}
