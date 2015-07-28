/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.rational.CFTree;
import org.safs.text.FAILStrings;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.vp.ITestDataTreeNode;

public class CFWPFTree extends CFTree {
	//Note: WPF System.Windows.Control.TreeView will be recognized as a WpfGuiSubitemTestObject,
	//which does not extends GuiSubitemTestObject, but if we create a new instance of GuiSubitemTestObject
	//with the reference of WpfGuiSubitemTestObject, it seems that it works. WpfGuiSubitemTestObject and
	//GuiSubitemTestObject have the same API method.
	private static final String ICON_TEXT 		= "<image>";
	private static final String SIGN_SMALLER 	= "<";
	private static final String SIGN_BIGGER 	= ">";
	
	protected String getTestDataTypeKey() {
		//known data keys for Tree TestData "treehierarchy", "allitems", "allitemstext", "itemcount"
		//For .NET WPF, we will return the key "treehierarchy"
		return "treehierarchy";
	}
	
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
			//There are 3 possible ways to get selected path
			//TODO	If RFT provide a TestDataType like "selectedPath", we can use testObject.getTestData("selectedPath").
			//		But there is no this type until RFT 8.0.0.2
			//TODO	We can try to get ListView's property "SelectedValuePath", but this will give us a blank string
			//System.out.println(guiObj.getProperty("SelectedValuePath"));
			
//			String pathSeparator = (String) guiObj.getProperty(PROPERTY_PATHSEPARATOR);
//			Log.info(debugmsg+" pathSeparator is "+pathSeparator);
			
//			String selectedPath = (String) guiObj.getProperty("SelectedValuePath");
//			Log.debug("SELECTED PATH: " + selectedPath);
//			
//			if (selectedPath == null) {
//				Log.debug(debugmsg + " Can not get selected full path");
//				return false;
//			}
//
//			StringTokenizer selectedNodes = new StringTokenizer(selectedPath,pathSeparator);
//			int realPathLength = selectedNodes.countTokens();
//			int testPathLength = pathToBeTested.length;
//			
//			// If root is no visible, pathToBeTested will not contain the root, but
//			// selectedPath contains the root, so we need skip the first object of selectedPath
//			if (!isRootVisible(guiObj)) {
//				//Start from the seconde element in the selectedNodes
//				Log.debug(debugmsg + " Root is not visible.");
//				if((realPathLength-1)!=testPathLength) return false;
//				//skip the first hidden root
//				selectedNodes.nextToken();
//			} else {
//				Log.debug(debugmsg + " Root is visible.");
//				if(realPathLength!=testPathLength) return false;
//			}
//	
//			for (int i = 0; i < testPathLength; i++) {
//				String node1 = ((Value) pathToBeTested[i]).getValue().toString();
//				String node2 = selectedNodes.nextToken();
//				Log.debug(debugmsg + " comparing... " + node1 + " and "+ node2);
//				if (!node1.equals(node2)) {
//					return false;
//				}
//			}
			
			//TODO  We can try to get ListView's property "SelectedItem", but did not find a way to manipulate it.
			TestObject obj;
//			System.out.println(guiObj.getProperty("SelectedValuePath"));
			//System.out.println(guiObj.getProperty("SelectedItem"));
			//System.out.println(guiObj.getProperty("SelectedValue"));
			
			
//			obj = (TestObject) guiObj.getProperty("SelectedItem");
//			MyUtil.listProperties(obj);
			//System.out.println(obj.getProperty("IsSelected"));
//			obj = (TestObject) obj.getProperty("Items");
//			int count = Integer.parseInt((obj.getProperty("Count").toString()));
//			
//			for(int i=0;i<count;i++){
//				Object[] index = new Object[1];
//			      index[0] = new Integer(i);	//row number or index default is zero
//				  
//			      TestObject value = (TestObject) obj.invoke("GetItemAt", "(I)LSystem.Object;", index);
//				  MyUtil.listProperties(value);
//				  //System.out.println();
//			}

		}catch(PropertyNotFoundException e1){
			Log.debug(debugmsg+e1.getMessage());
			throw new SAFSException(failedText.text(FAILStrings.PROPERTY_NOT_FOUND, "Can not find property.")+e1.getMessage());
		}catch (MethodNotFoundException e2) {
			Log.debug(debugmsg+e2.getMessage());
			throw new SAFSException(failedText.text(FAILStrings.METHOD_NOT_FOUND, "Can not find method.")+e2.getMessage());
		}
		
		return true;
	}
	
	protected String getTreeNodeValue(ITestDataTreeNode treenode){
		String nodeValue = super.getTreeNodeValue(treenode);
		
		Log.debug("JSAFSBefore strip: nodeValue="+nodeValue);
		
		if(nodeValue.startsWith(ICON_TEXT)){
			//Remove the extra string of the Icon: "<image>"
			nodeValue = nodeValue.substring(ICON_TEXT.length());
		}
		
		if(nodeValue.startsWith(SIGN_SMALLER) && nodeValue.endsWith(SIGN_BIGGER)){
			//Remove the "<" and ">" which enclose the "real-node-string"
			int endIndex = nodeValue.length()-SIGN_BIGGER.length();
			nodeValue = nodeValue.substring(SIGN_SMALLER.length(),endIndex);
		}
		Log.debug("After strip: nodeValue="+nodeValue);
		
		return nodeValue;
	}
}
