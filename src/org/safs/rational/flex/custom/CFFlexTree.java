/* $Id: CFFlexTree.java,v 1.1.2.4 2011/06/29 09:18:12 Junwu Ma Exp $ */
package org.safs.rational.flex.custom;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSStringTokenizer;
import org.safs.StatusCodes;
import org.safs.rational.FlexUtil;
import org.safs.rational.Script;
import org.safs.text.GENStrings;
import org.safs.tools.data.DataUtilities;
import org.safs.tools.data.NodeInfo;
import org.safs.tools.stringutils.StringUtilities;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexComboBaseTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexTreeTestObject;
import com.rational.test.ft.script.FlexScrollDetails;
import com.rational.test.ft.script.FlexScrollDirections;
import com.rational.test.ft.script.RationalTestScript;

/**
 * <br>
 * <em>Purpose:</em> CFFlexTree, process a SAS Flex Tree component in SAFS custom processor. <br>
 * <p>
 * 
 * @author Junwu Ma
 * @since JUN 20, 2011
 *  <br>  JUN 21, 2011  (dharmesh4) Fixed scrollToNodeFromPos getChildren call location and logs support.
 *  <br>  JUN 23, 2011  (JunwuMa)  Refactored to add partial match for partial match keywords.
 *  <br>  JUN 27, 2011  (JunwuMa)  Added DoubleClickTextNode/DoubleClickPartial and RightClickTextNode/RightClickPartial.
 **/
public class CFFlexTree extends org.safs.rational.flex.CFFlexTree {

	public static final String DELEGATE_OPR_EXPAND   	= "Open";
	public static final String DELEGATE_OPR_COLLAPSE 	= "Close";
	public static final String DELEGATE_OPR_SELECT 		= "Select";
	public static final String DELEGATE_OPR_DESELECT 	= "Deselect";
	public static final String DELEGATE_OPR_DOUBLECLICK = "DoubleClick"; // this method cannot be supported by the delegate
	
	protected boolean isNodePartialMatch(String nodeAutoName, String comparedAutoMame) {
		StringTokenizer nodes = new StringTokenizer(nodeAutoName, TREE_ITEMNAME_SEPARATOR);
		StringTokenizer comparedNodes = new StringTokenizer(comparedAutoMame, TREE_ITEMNAME_SEPARATOR);
		boolean match = true;
		while(nodes.hasMoreTokens() || comparedNodes.hasMoreTokens()){
			String fullStr="", partialStr="";
			if (nodes.hasMoreTokens())
				fullStr = nodes.nextToken();
			if (comparedNodes.hasMoreTokens())
				partialStr = comparedNodes.nextToken();
			if (fullStr.equals("") || partialStr.equals("") ||fullStr.indexOf(partialStr)<0){
				match = false;
				break;
			}
		}
		return match;
	}
	/*
	 * scroll down from the startPos to search a target node in a Flex tree and make it visible.
	 * A target node's automationName: "node1>node1.1>node1.1.1" for exactly match; "e1>e1.1>e1.1.1" for partial match
	 * @param tree, a FlexTreeTestObject
	 * @param autoNameOfNode, (input) the automationName of the target node for search; (output)when exactMatch is false, automationName will be setting to the full matching string and passed out 
	 * @param startPos, (>=0) the starting position in vertical direction for searching the target node
	 * @param exactMatch, true: exactly match for autoNameOfNode false: partial match 
	 * @return the position, at which the target node is found and visible; -1: not found
	 */
	private int scrollToNodeFromPos(FlexTreeTestObject tree, StringBuffer autoNameOfNode, int startPos, boolean exactMatch) {
		
		String methodName = this.getClass().getName()+".scrollToNodeFromPos(): ";

		int foundPos = -1;
    	int page = 0;
    	
    	// start from the first node by scroll to startPos
    	Log.info(methodName + "scroll move position " +startPos);
    	tree.scroll(Script.atPosition(startPos), FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);

		TestObject[] children = tree.getChildren();
    	int visibleRows = children.length;
    	
    	if(visibleRows>0){
			TestObject firstChildOnPreviousPage = null;
	    	while(foundPos<0){
				if (children.length>0)
					firstChildOnPreviousPage = children[0]; // remember the first child for comparing 
				
	    		//children.length may change and is smaller than visibleRows on the last page
				for(int i=0;i<children.length;i++){
		    		  String source = (String)children[i].getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
		    		  
		    		  if (source.length() == 0) continue;
		    		  
		    		  Log.info(autoNameOfNode.toString() + " : " + source);
		    		  boolean match = exactMatch ?  source.equals(autoNameOfNode.toString()) : isNodePartialMatch(source, autoNameOfNode.toString());
		    		  if (match) {
		    			  foundPos = startPos + visibleRows*page;
		    			  Log.info("found the matching node" + " : " + source);
		    			  if (!exactMatch) { // output the real automationName instead of the partial string 
		    				  autoNameOfNode.setLength(0);   
		    				  autoNameOfNode.append(source);
		    			  }
		    			  break;
		    		  }
				}
				if(foundPos >= 0 || children.length < visibleRows){
					//This means matching node found or no more items in the Tree panel for searching.
					break;
				}
				page++;
				Log.info(methodName +"scroll move position "+startPos+visibleRows*page);
				tree.scroll(Script.atPosition(startPos+visibleRows*page), FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
				children = tree.getChildren();
				
				//the children have no change if the new position is beyond the range.
				//this may happens under one of the two conditions 
				//1) no vertical scroll bar shown; the panel is big enough to hold all tree nodes 
				//2) vertical scroll bar shown; the number of tree nodes is divided by visibleRows exactly without remainder
				//in these cases, children.length is equal to visibleRows; startPos+visibleRows*page beyond the range, 
				//so break if no change with children in case of an infinitive loop
				//not find other way to get the total item number, use this instead.
				if (children.length == visibleRows) 
					if (firstChildOnPreviousPage.equals(children[0]))
						break;
	    	}		
		}
		return foundPos;
	}

	/**
	 * Do an action on a node with nodepath in a SASFlexTree. 
	 * nodepath represents a Flex tree node, formatted like "node1->node1.1->node1.1.1".
	 * In the path, nodes are delimited with '->'. A node's automationName is prefixed with its parent node's 
	 * automationName, delimited with '>'. E.g., the automationName of node1.1.1 is "node1>node1.1>node1.1.1".
	 * 
	 * @param tree, a FlexTreeTestObject
	 * @param action, defined operations for SASFlexTree in FlexEnv.XML (delegate)
	 *                "Open"		--- for expanding a node
	 *                "Close"		--- for collapsing a node
	 *                "Select" 		--- for selecting a node
	 *                "Deselect"  	--- for deselecting a node
	 * @param nodepath, formatted like "node1->node1.1->node1.1.1", nodes are delimited with '->'
	 *                  the automationName of node1.1.1 is "node1>node1.1>node1.1.1"
	 * @param exactMatch, true: exactly match for nodepath false: partial match 
	 * @return true if the node found and the action executed; otherwise false
	 */
	protected boolean doActionByPath(FlexTreeTestObject tree, String action, String nodepath, boolean exactMatch) {

		String methodName = this.getClass().getName()+".doActionByPath(): ";

		String[] nodeArray = nodepath.split(NODE_DELIMIT);
		int nodepathLen = nodeArray.length;
		
		StringBuffer targetAutoName = null;
		
		int pos = 0;
		if (nodepathLen < 1) {
			return false;
		}else if(nodepathLen == 1)
			targetAutoName = new StringBuffer(nodeArray[0]);
		else { // visit the nodes (>=2) in the path 
			targetAutoName = new StringBuffer("");
			// visit and expand all the nodes in the path except for the last one
			for (int i = 0; i<nodepathLen-1; i++) {
				if (targetAutoName.length() == 0)
					targetAutoName.append(nodeArray[i]);
				else
					targetAutoName.append(TREE_ITEMNAME_SEPARATOR + nodeArray[i]);
				
				pos = scrollToNodeFromPos(tree, targetAutoName, pos, exactMatch);
				
				if (pos < 0) {
					return false; //fail to find the node
				} else
					// 'expand' may take a while. The next action based on it may fail if 'expand' cannot be finished 
					// before starting the next action!  
					tree.performAction(DELEGATE_OPR_EXPAND,	new Object[] { targetAutoName.toString() }); // expand it, as it is a parent node
			}
			// the last node
			targetAutoName.append(TREE_ITEMNAME_SEPARATOR+nodeArray[nodepathLen-1]);
			Log.info(methodName + "last node " + targetAutoName.toString());
		}
		// search the last node and ensure it is visible, starting from the pos, which is used for its previous visible node(its parent) 	
		if (scrollToNodeFromPos(tree, targetAutoName, pos, exactMatch) >= 0) {
			// do action on the node
			tree.performAction(action,	new Object[] { targetAutoName.toString() });
			return true;
		} else {
			Log.debug(methodName + " the returned tree node is NULL");
			return false;
		}
	}

	protected boolean doRightClickByPath(FlexTreeTestObject tree, String nodepath, boolean exactMatch) {
		String methodName = this.getClass().getName()+".doRightClickByPath(): ";
		
		// select the node
		if (!doActionByPath(tree, DELEGATE_OPR_SELECT, nodepath, true)) {
			return false;
		}
		Object selectedIndexObject =  tree.getProperty(PROPERTY_SELECTEDINDEX);
		Log.info(selectedIndexObject.toString());
		int selectedIndex = 0;

		if (selectedIndexObject instanceof String) {
			selectedIndex = Integer.parseInt((String) selectedIndexObject);
		} else {
			Log.debug(methodName + " selectedIndexObject is " + selectedIndexObject.getClass().getName()+", it needs to be converted to int.");
			return false;
		}
		
		//Get the selected child, mx.controls.treeClasses.TreeItemRenderer
		GuiTestObject pathobj = (GuiTestObject) tree.getAutomationChildAt(selectedIndex);
		if (pathobj != null) {
			pathobj.click(Script.RIGHT);
			return true;
		} else {
			Log.debug(methodName + " the returned tree node is NULL");
			return false;
		}
	}	

	protected boolean doDoubleClickByPath(FlexTreeTestObject tree, String nodepath, boolean exactMatch) {
		// select the node
		if(!doActionByPath(tree, DELEGATE_OPR_SELECT, nodepath, true)) {
			return false;
		}
		
		Object selectedIndexObject =  tree.getProperty(PROPERTY_SELECTEDINDEX);
		Log.info(selectedIndexObject.toString());
		int selectedIndex = 0;

		if(selectedIndexObject instanceof String) {
			selectedIndex = Integer.parseInt((String) selectedIndexObject);
		} else {
			Log.debug(" selectedIndexObject is " + selectedIndexObject.getClass().getName()+", it needs to be converted to int.");
			return false;
		}
		
		//Get the selected child, mx.controls.treeClasses.TreeItemRenderer
		GuiTestObject pathobj = (GuiTestObject) tree.getAutomationChildAt(selectedIndex);
		if (pathobj != null) {
			pathobj.doubleClick();
			return true;
		} else {
			Log.debug(" selectedIndexObject is " + selectedIndexObject.getClass().getName()+", it needs to be converted to int.");
			return true;
		}
	}

	protected void commandWithOneParam () throws SAFSException {

		if (!SASUtil.isSASFlexComponent(obj1)) {
			super.commandWithOneParam();
			return;
		}

		Log.debug("Executing org.safs.rational.flex.custom.commandWithOneParam()");
		if (params.size() < 1)
			paramsFailedMsg(windowName, compName);
		else {
			Iterator iter = params.iterator();
			// Treat the first parameter as path, which is required.
			String path = iter.next().toString();
			String actionpath = path;

			// Treat the second parameter index, which is optional. (not supported)
			String index = null;
			if (iter.hasNext()) {
				index = StringUtilities.removePrefix(iter.next().toString()
						.toUpperCase(), INDEX_PREFIX);
			}

			Log.info("..... path: " + path);
			Log.info("..... actionpath: " + actionpath);

			// process action!
			try {
				String altText = action+" performed on '" + path;


				FlexTreeTestObject SASflexTree = new FlexTreeTestObject(
						obj1.getObjectReference());
				boolean pass;
				if (action.equalsIgnoreCase(CLICK) ||
					action.equalsIgnoreCase(SELECT)|| 
					action.equalsIgnoreCase(MAKESELECTION) ||
					action.equalsIgnoreCase(SELECTTEXTNODE)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_SELECT, actionpath, true);
				} else if (action.equalsIgnoreCase(SELECTPARTIALTEXTNODE) ||
						   action.equalsIgnoreCase(CLICKPARTIAL) ||
						   action.equalsIgnoreCase(SELECTPARTIAL)){
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_SELECT, actionpath, false);
				} else if (action.equalsIgnoreCase(EXPAND) || 
						  action.equalsIgnoreCase(EXPANDTEXTNODE)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_EXPAND, actionpath, true);
				} else if (action.equalsIgnoreCase(EXPANDPARTIALTEXTNODE) || 
						   action.equalsIgnoreCase(EXPANDPARTIAL)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_EXPAND, actionpath, false);
				}else if (action.equalsIgnoreCase(COLLAPSE) ||
						  action.equalsIgnoreCase(COLLAPSETEXTNODE)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_COLLAPSE, actionpath, true);
				} else if (action.equalsIgnoreCase(COLLAPSEPARTIALTEXTNODE) || 
						   action.equalsIgnoreCase(COLLAPSEPARTIAL)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_COLLAPSE, actionpath, false);
				} else if (action.equalsIgnoreCase(VERIFYSELECTEDNODE)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_SELECT, actionpath, true);
				} else if (action.equalsIgnoreCase(VERIFYNODEUNSELECTED)) {
					pass = doActionByPath(SASflexTree, DELEGATE_OPR_DESELECT, actionpath, true);
				} else if (action.equalsIgnoreCase(DOUBLECLICK)) {
					pass = doDoubleClickByPath(SASflexTree,  actionpath, true);
				} else if (action.equalsIgnoreCase(DOUBLECLICKPARTIAL)) {
					pass = doDoubleClickByPath(SASflexTree,  actionpath, false);
				} else if (action.equalsIgnoreCase(RIGHTCLICK)) {
					pass = doRightClickByPath(SASflexTree,  actionpath, true);
				} else if (action.equalsIgnoreCase(RIGHTCLICKPARTIAL)){
					pass = doRightClickByPath(SASflexTree,  actionpath, false);
				} else {
					Log.info("...Not supported for command " + action);
					Log.info("...Marking it SCRIPT_NOT_EXECUTED");
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					return;
				} 
				/*else if (action.equalsIgnoreCase(SELECTANOTHERTEXTNODE) ||
				  action.equalsIgnoreCase(SELECTANOTHERPARTIALTEXTNODE)) {
				  guiObj.click(RationalTestScript.CTRL_LEFT, slist); }*/

				if (pass) {
					// set status to ok
					log.logMessage(testRecordData.getFac(), passedText.convert(
							"perfnode3", altText, action, actionpath, compName),
							PASSED_MESSAGE);
					testRecordData.setStatusCode(StatusCodes.OK);
				} else {
				    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);	
					String detail = failedText.convert("no_node1",
							"No matching node found for '" + path + "'.", path);
					componentFailureMessage(detail);
				}
			} catch (NullPointerException npe) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				String detail = failedText.convert("no_node1",
						"No matching node found for '" + path + "'.", path);
				componentFailureMessage(detail);
			} 
		}
	}
	
	public static void main(String[] args){
		String s1=">Outbox>>>Test>";
		String s2="Out>est";
		CFFlexTree tree = new CFFlexTree();
		if(tree.isNodePartialMatch(s1, s2))
			System.out.println("match");
	}
}
