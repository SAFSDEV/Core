/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.Tree;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.data.DataUtilities;
import org.safs.tools.data.NodeInfo;
import org.safs.tools.stringutils.StringUtilities;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.script.Value;
import com.rational.test.ft.vp.ITestDataText;
import com.rational.test.ft.vp.ITestDataTree;
import com.rational.test.ft.vp.ITestDataTreeNode;
import com.rational.test.ft.vp.ITestDataTreeNodes;
import com.rational.test.ft.vp.impl.ObjectReference;

/**
 * <br><em>Purpose:</em> CFTree, process a TREE component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   JUL 11, 2003    (DBauman) Original Release
 *   <br>   SEP 09, 2004    (CANAGL)  Added/Fixed all partial text matching!
 *   <br>   AUG 18, 2005    (Jeremy_J_Smith) Added "Right Click" action
 *   <br>   Aug 22, 2005    (bolawl)  Added SelectAnother*TextNode actions (RJL).
 *   <br>                             Changed Select* actions to use click() rather than 
 *   <br>                              setState() so that they are always recognized (RJL).
 *   <br>                             Added extractRootName() helper function to retrieve the
 *   <br>                              text of the rootnode (RJL).
 *   <br>                             Fixed code to properly handle root node (RJL).
 *   <br>                             Re-factored commandWithOneParam() for more tree types (RJL). 
 *   <br>   Aug 26, 2005    (bolawl)  Added captureTreeDataToFile() (with partial match) (RJL).
 *   <br>   Sep 02, 2005    (bolawl)  Re-factored for CFTree to utilize 2DArray of tree (rather
 *   <br>                              than a Tree object) (RJL).
 *   <br>	Sep 08, 2005    (bolawl)  Updated commandWithOneParam() and captureTreeDataToFile() 
 *   <br>                              to trim off hidden root node before logging success and
 *   <br>                              improved success messages. (RJL)
 *   <br>                             Added isRootVisible() helper function. (RJL) 
 *   <br>   OCT 13, 2005    (canagl)  Changes to (hopefully) better support SWT
 *   <br>   OCT 18, 2005    (canagl)  Changed generic Click commands to ClickTextNode commands
 *   <br>   JAN 11, 2006    (bolawl)  Updated captureTreeDataToFile() to format output file with 
 *   <br>                                  new optional parameter indentMark (default is tab \t). (RJL)
 *   <br>   JAN 19, 2006    (bolawl)  Added verifyTreeContains() for new Component Functions: (RJL)
 *   <br>                              - VerifyTreeContainsNode/PartialMatch
 *   <br>                              - SetTreeContainsNode/PartialMatch
 *   <br>   MAR 10, 2008    (junwuma) Added two keywords for multi-selecting nodes in JavaTree
 *                                    CtrlClickUnverifiedTextNode and ShiftClickUnverifiedTextNode. 
 *	 <br>	MAY 5,	2008	(leiwang)	Reorganize keyword order,
 *										Add keyword: 
 *													ClickPartial
 *													CollapsePartial
 *													DoubleClickPartial
 *													ExpandPartial
 *													RightClickPartial
 *													SelectPartial
 *													
 *													ActivateUnverifiedTextNode
 *													ClickUnverifiedTextNode
 *													CollapseUnverifiedTextNode
 *													DoubleClickUnverifiedTextNode
 *													ExpandUnverifiedTextNode
 *													RightClickUnverifiedTextNode
 *													SelectAnotherUnverifiedTextNode
 *													SelectUnverifiedTextNode
 *													
 *													VerifyNodeUnselected
 *													VerifySelectedNode
 *										Modify keyword: CtrlClickUnverifiedTextNode, ShiftClickUnverifiedTextNode
 *										Add the optional parameter [MatchIndex = index] support for many keywords
 * 	<br>	AUG 5,	2008	(leiwang)	Modify methods: commandWithOneParam(),isRootVisible(),extractRootName()
 * 														captureTreeDataToFile(),verifyTreeContains(). -- Repalce the 
 * 														usage of calling isSWTWidget() by isJavaDomain() and isSwtDomain().
 * 														I suppose the "hidden root" exists only for java-swing component.
 * 										Add counstant:	METHOD_TOSTRING,METHOD_GETSELECTIONPATH,METHOD_GETPATH,PROPERTY_USEROBJECT
 * 										Modify method:	isNodeSelected(), catch PropertyNotFoundException and MethodNotFoundException.
 *  													If more than one node is selected in the tree, we will test each selected path
 *  													with our test path.
 *  <br>    OCT 28, 2008    (canagl)    Catch and process NullPointerException in to2DArray for empty trees.
 **/
public class CFTree extends CFComponent {
  public static final String ACTIVATEUNVERIFIEDTEXTNODE  	= "ActivateUnverifiedTextNode";
  public static final String CAPTURETREEDATATOFILE       	= "CaptureTreeDataToFile";
  public static final String CLICKPARTIAL				  	= "ClickPartial";//SelectPartialTextNode
  public static final String CLICK                       	= "ClickTextNode";
  public static final String CLICKUNVERIFIEDTEXTNODE       	= "ClickUnverifiedTextNode";
  public static final String COLLAPSE                    	= "Collapse";
  public static final String COLLAPSEPARTIALTEXTNODE     	= "CollapsePartialTextNode";
  public static final String COLLAPSEPARTIAL			  	= "CollapsePartial"; //CollapsePartialTextNode
  public static final String COLLAPSETEXTNODE            	= "CollapseTextNode";
  public static final String COLLAPSEUNVERIFIEDTEXTNODE     = "CollapseUnverifiedTextNode";
  public static final String CTRLCLICKUNVERIFIEDTEXTNODE 	= "CtrlClickUnverifiedTextNode";
  public static final String DOUBLECLICKPARTIAL			 	= "DoubleClickPartial";
  public static final String DOUBLECLICK                 	= "DoubleClickTextNode";
  public static final String DOUBLECLICKUNVERIFIEDTEXTNODE  = "DoubleClickUnverifiedTextNode";
  public static final String EXPAND                      	= "Expand";
  public static final String EXPANDPARTIAL				  	= "ExpandPartial";//ExpandPartialTextNode
  public static final String EXPANDPARTIALTEXTNODE       	= "ExpandPartialTextNode";
  public static final String EXPANDTEXTNODE              	= "ExpandTextNode";
  public static final String EXPANDUNVERIFIEDTEXTNODE       = "ExpandUnverifiedTextNode";
  public static final String MAKESELECTION               	= "MakeSelection";
  public static final String PARTIALTREEDATATOFILE       	= "PartialMatchTreeDataToFile";
  public static final String RIGHTCLICKPARTIAL			  	= "RightClickPartial";
  public static final String RIGHTCLICK                  	= "RightClickTextNode";
  public static final String RIGHTCLICKUNVERIFIEDTEXTNODE   = "RightClickUnverifiedTextNode";
  public static final String SELECT                      	= "Select";
  public static final String SELECTANOTHERPARTIALTEXTNODE 	= "SelectAnotherPartialTextNode";
  public static final String SELECTANOTHERTEXTNODE       	= "SelectAnotherTextNode";
  public static final String SELECTANOTHERUNVERIFIEDTEXTNODE= "SelectAnotherUnverifiedTextNode";
  public static final String SELECTPARTIAL				  	= "SelectPartial";//SelectPartialTextNode
  public static final String SELECTPARTIALTEXTNODE       	= "SelectPartialTextNode";
  public static final String SELECTTEXTNODE              	= "SelectTextNode";
  public static final String SELECTUNVERFIEDTEXTNODE	  	= "SelectUnverifiedTextNode";
  public static final String SETTREECONTAINSNODE		 	= "SetTreeContainsNode";
  public static final String SETTREECONTAINSPARTIAL	 	 	= "SetTreeContainsPartialMatch";
  public static final String SHIFTCLICKUNVERIFIEDTEXTNODE 	= "ShiftClickUnverifiedTextNode";
  public static final String VERIFYNODEUNSELECTED		 	= "VerifyNodeUnselected";
  public static final String VERIFYSELECTEDNODE			  	= "VerifySelectedNode";
  public static final String VERIFYTREECONTAINSNODE		 	= "VerifyTreeContainsNode";
  public static final String VERIFYTREECONTAINSPARTIAL	 	= "VerifyTreeContainsPartialMatch";
  public static final String NODE_DELIMIT					= "->";
  public static final String INDEX_PREFIX					= "INDEX=";
  
  public static final String METHOD_TOSTRING				= "toString";
  //javax.swing.JTree method
  public static final String METHOD_GETSELECTIONPATHS		= "getSelectionPaths";
  //javax.swing.tree.TreePath method
  public static final String METHOD_GETPATH					= "getPath";
  //javax.swing.tree.DefaultMutableTreeNode's property
  public static final String PROPERTY_USEROBJECT			= "userObject";
  
  boolean partialmatch;			//used for partial text matching
  private ArrayList rowlist;	//used in building 2D array of Tree
  private int columns;			//max number of element in all rows of rowlist
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFTree () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** We first call super.process() [which handles actions like 'click']
   ** The types of objects handled here are '{@link GuiSubitemTestObject}'.
   ** Path Example: "Composers->Bach->Brandenburg Concertos Nos. 1 & 3"
   **
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+".process, searching specific tests...",
          DEBUG_MESSAGE);

      if (action != null) {
        Log.info(".....CFTree.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
       
	      partialmatch = false;	//initialize
	      if (action.equalsIgnoreCase(SELECTPARTIALTEXTNODE) ||
	          action.equalsIgnoreCase(SELECTANOTHERPARTIALTEXTNODE) ||
			  action.equalsIgnoreCase(EXPANDPARTIALTEXTNODE) ||
			  action.equalsIgnoreCase(COLLAPSEPARTIALTEXTNODE) ||
			  action.equalsIgnoreCase(RIGHTCLICKPARTIAL) ||
			  action.equalsIgnoreCase(CLICKPARTIAL) ||
			  action.equalsIgnoreCase(COLLAPSEPARTIAL) ||
			  action.equalsIgnoreCase(EXPANDPARTIAL) ||
			  action.equalsIgnoreCase(SELECTPARTIAL) ||
			  action.equalsIgnoreCase(DOUBLECLICKPARTIAL)) {
	      	partialmatch = true;
	        commandWithOneParam();
	      }
	      else if (action.equalsIgnoreCase(CLICK) ||
	      		   action.equalsIgnoreCase(RIGHTCLICK) ||
	      		   action.equalsIgnoreCase(DOUBLECLICK) ||  
				   action.equalsIgnoreCase(SELECT) ||
				   action.equalsIgnoreCase(MAKESELECTION) ||
				   action.equalsIgnoreCase(SELECTTEXTNODE) ||
				   action.equalsIgnoreCase(SELECTANOTHERTEXTNODE) ||				
				   action.equalsIgnoreCase(EXPANDTEXTNODE) ||
				   action.equalsIgnoreCase(COLLAPSETEXTNODE) ||
				   action.equalsIgnoreCase(EXPAND) ||
				   action.equalsIgnoreCase(COLLAPSE) ||
				   action.equalsIgnoreCase(VERIFYSELECTEDNODE) ||
				   action.equalsIgnoreCase(VERIFYNODEUNSELECTED)) {
	      	commandWithOneParam();
	      }
	      else if (action.equalsIgnoreCase(CAPTURETREEDATATOFILE)){
	      	partialmatch = false;
	      	captureTreeDataToFile();
	      }
	      else if (action.equalsIgnoreCase(PARTIALTREEDATATOFILE)) {
	      	partialmatch = true;
	      	captureTreeDataToFile();
	      }
	      else if (action.equalsIgnoreCase(VERIFYTREECONTAINSNODE) ||
	      		   action.equalsIgnoreCase(SETTREECONTAINSNODE))
	      	verifyTreeContains();
	      else if (action.equalsIgnoreCase(VERIFYTREECONTAINSPARTIAL) ||
	      		   action.equalsIgnoreCase(SETTREECONTAINSPARTIAL)) {
	      	partialmatch = true;
	      	verifyTreeContains();
	      }else if(action.equalsIgnoreCase(ACTIVATEUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(SELECTUNVERFIEDTEXTNODE)	||
	      		   action.equalsIgnoreCase(CTRLCLICKUNVERIFIEDTEXTNODE) ||   
	      		   action.equalsIgnoreCase(SHIFTCLICKUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(CLICKUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(DOUBLECLICKUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(RIGHTCLICKUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(COLLAPSEUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(EXPANDUNVERIFIEDTEXTNODE) ||
				   action.equalsIgnoreCase(SELECTANOTHERUNVERIFIEDTEXTNODE)){
	      	commandWithOneParamWithoutVerification();
	      }
        //all for now
      }
    } catch (com.rational.test.ft.SubitemNotFoundException snfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      componentFailureMessage(snfe.getMessage());
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      componentFailureMessage(onfe.getMessage());
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      componentFailureMessage(ex.getMessage());
    }
  }

  /** <br><em>Purpose:</em> commandWithOneParam: process commands like: click, doubleclick,
   ** expand and collapse; ones which take one parameter, (the path in the tree).
   ** example:
   ** tree2Tree().setState(Action.collapse(), atPath("Composers->Bach"));
   * @param                     action, String  (i.e. expand)
   * @param                     script, Script
   * @param                     compName, String
   * @param                     utils, DDGUtilsInterface
   * <br>    Sep 08, 2005    (bolawl)  Improved messaging. RJL
   * <br>    OCT 13, 2005    (canagl)  Changes to (hopefully) better support SWT
   **/
  protected void commandWithOneParam () throws SAFSException {
  	String debugmsg = getClass().getName()+".commandWithOneParam() ";
    // example:
    //tree2Tree().setState(Action.collapse(), atPath("Composers->Bach"));
    if (params.size() < 1)
    	paramsFailedMsg(windowName, compName); 
    else {
    	//get the 2D representation of the tree
    	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
    	String[][] atree = to2DArray(guiObj);	
    	if (atree == null) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		log.logMessage(testRecordData.getFac(),
    	                   testRecordData.getCommand()+
    	                   " failure, could not process " + compName + ".", 
						   FAILED_MESSAGE);
    	    return;
    	}

    	Iterator iter = params.iterator();
    	//Treate the first parameter path, which is required.
    	String path = iter.next().toString();
    	String actionpath = path;
    	String delimitedrootlabel = "";
    	//Treate the second parameter index, which is optional.
    	String index = null;
    	if(iter.hasNext()){
    		index = StringUtilities.removePrefix(iter.next().toString().toUpperCase(),INDEX_PREFIX);
    	}
    	
    	// prepare use with possible hidden Swing or Flex root nodes
    	// this does not apply to SWT, .NET components, etc.
    	if(isJavaDomain(guiObj) || isFlexDomain(guiObj)){
    		// only works when there is a single root node
    		// which apparently is the case for Swing but is NOT for SWT
	    	String rootlabel = atree[0][0];
	    	delimitedrootlabel = rootlabel + NODE_DELIMIT;
	    	//retrieve path root node from user supplied path
	    	String pathrootnode = path;
	    	int stripArrowI = path.indexOf(NODE_DELIMIT); // the first occurance of the delimiter
	    	boolean singlenodepath = stripArrowI < 0;
	    	if (!singlenodepath) 
	    		pathrootnode = path.substring(0, stripArrowI);
		  
	    	//pre-pend root node (hidden or not), if not already included in user supplied path
	    	if (partialmatch) {
	    		if (!(rootlabel.toLowerCase().indexOf(pathrootnode.toLowerCase()) >= 0)) {
	    			actionpath = delimitedrootlabel + path;
	    		}
	    	}	
	    	else {
	    		if (!(path.startsWith(rootlabel))) {
	    			actionpath = delimitedrootlabel + path;
	    		}
	    	}
    	}
    	
    	Log.info("..... path: "+path);
    	Log.info("..... actionpath: "+actionpath);
      
    	//search for actionpath in tree...
    	try {
    		NodeInfo node = DataUtilities.getObjectDataNodeInfo (atree, actionpath, NODE_DELIMIT, partialmatch,convertToInteger(index));

    		if (node == null) {
    			//no partial match found, exit with failure
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			String optionalInfo = (index==null? "" : " for "+index+"th times.");
    			String detail = failedText.convert("no_node1", "No matching node found for '" + path + "'.", path)+optionalInfo;
    			componentFailureMessage(detail);
    			return;
    		}
    		actionpath = node.getPath();
    		Log.info("updated actionpath: "+actionpath);
      		
    		//strip off root node if not visible (Selection will fail if included)
    		//we already know that actionpath includes it!
    		if (!isRootVisible(guiObj)) 
        		actionpath = actionpath.substring(delimitedrootlabel.length());
    	}
    	catch (StringIndexOutOfBoundsException siobe) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		String rootmsg = "Cannot access non-visible root node '" + actionpath + "'.";
    		String detail = failedText.convert(rootmsg, rootmsg, actionpath);
    		componentFailureMessage(detail);
    		return;
    	}
      	
    	//process action!
    	try {
    		String altText = action+" performed on '" + path + "' in " + compName + ".";
    		com.rational.test.ft.script.List slist = getPathList(actionpath,index);
    		
    		if (action.equalsIgnoreCase(CLICK) ||
				action.equalsIgnoreCase(SELECT) ||
				action.equalsIgnoreCase(MAKESELECTION) ||
				action.equalsIgnoreCase(SELECTTEXTNODE) ||
				action.equalsIgnoreCase(SELECTPARTIALTEXTNODE) ||
				action.equalsIgnoreCase(CLICKPARTIAL) ||
				action.equalsIgnoreCase(SELECTPARTIAL)) {
    			guiObj.click(slist);
    		} else if (action.equalsIgnoreCase(RIGHTCLICK) ||
    				action.equalsIgnoreCase(RIGHTCLICKPARTIAL)) {
    			guiObj.click(Script.RIGHT, slist);
    		} else if (action.equalsIgnoreCase(DOUBLECLICK) ||
    				action.equalsIgnoreCase(DOUBLECLICKPARTIAL)) {
    			doDoubleClick(guiObj,slist);
    		} else if (action.equalsIgnoreCase(SELECTANOTHERTEXTNODE) ||
    				   action.equalsIgnoreCase(SELECTANOTHERPARTIALTEXTNODE)) {
    			guiObj.click(RationalTestScript.CTRL_LEFT, slist);
    		} else if (action.equalsIgnoreCase(EXPAND) ||
    				   action.equalsIgnoreCase(EXPANDTEXTNODE) ||
				       action.equalsIgnoreCase(EXPANDPARTIALTEXTNODE) ||
					   action.equalsIgnoreCase(EXPANDPARTIAL)) {
    			doExpand(guiObj,slist,true);
    		} else if (action.equalsIgnoreCase(COLLAPSE) ||
				       action.equalsIgnoreCase(COLLAPSETEXTNODE) ||
				       action.equalsIgnoreCase(COLLAPSEPARTIALTEXTNODE) ||
					   action.equalsIgnoreCase(COLLAPSEPARTIAL)) {
    			doExpand(guiObj,slist,false);
    		} else if(action.equalsIgnoreCase(VERIFYSELECTEDNODE)){
    			String notSelectedMessage = genericText.convert(GENStrings.IS_NOT_SELECTED,actionpath+" is not selected in Tree",actionpath,"Tree");
    			Log.debug("PATH TO BE TESTED: "+actionpath);
    			if(!isNodeSelected(guiObj,slist)){
    				componentExecutedFailureMessage(notSelectedMessage);
    				testRecordData.setStatusCode(StatusCodes.OK);
    				return;
    			}
    		}else if(action.equalsIgnoreCase(VERIFYNODEUNSELECTED)){
    			String selectedMessage = genericText.convert(GENStrings.IS_SELECTED,actionpath+" is selected in Tree",actionpath,"Tree");
    			Log.debug("PATH TO BE TESTED: "+actionpath);
    			if(isNodeSelected(guiObj,slist)){
    				componentExecutedFailureMessage(selectedMessage);
    				testRecordData.setStatusCode(StatusCodes.OK);
    				return;
    			}
    		}
  	    		
    		//set status to ok
    		log.logMessage(testRecordData.getFac(),
    					   passedText.convert("perfnode3", altText, action, actionpath, compName), 
						   PASSED_MESSAGE);
    		testRecordData.setStatusCode(StatusCodes.OK);
    	} 
    	catch (NullPointerException npe) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		String detail = failedText.convert("no_node1", "No matching node found for '" + path + "'.", path);
    		componentFailureMessage(detail);
    	}
    	catch (com.rational.test.ft.SubitemNotFoundException snfe) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		String detail = failedText.convert("no_node1", "No matching node found for '" + path + "'.", path);
    		componentFailureMessage(detail);
    	}
     }
  }
  
  /**
   * @param testObjct		TestObject The tree test object
   * @param path			String	   The tree path to be selected
   * @param expand			boolean	   If true, expand the tree path; otherwise collapse the tree path
   * @throws SAFSException
   */
  protected void doExpand(TestObject testObjct, com.rational.test.ft.script.List path, boolean expand) throws SAFSException{
	  String debugmsg = getClass().getName()+".doExpand(): ";
	  GuiSubitemTestObject treeObject = (GuiSubitemTestObject) testObjct;
	  try{
		  if(expand){
			  treeObject.setState(Action.expand(), path);
		  }else{
			  treeObject.setState(Action.collapse(), path);
		  }
	  }catch(SubitemNotFoundException e){
		  Log.debug(debugmsg+" Exception: "+e.getMessage());
		  throw new SAFSException(e.getMessage());
	  }
  }
 
  protected void doDoubleClick(TestObject testObjct, com.rational.test.ft.script.List path) throws SAFSException{
	  String debugmsg = getClass().getName()+".doDoubleClick(): ";
	  GuiSubitemTestObject treeObject = (GuiSubitemTestObject) testObjct;
	  
	  try{
		  treeObject.doubleClick(path);
	  }catch(SubitemNotFoundException e){
		  Log.debug(debugmsg+" Exception: "+e.getMessage());
		  throw new SAFSException(e.getMessage());
	  }	  
  }
  
  private void tryUnverifiedListAction(GuiSubitemTestObject guiObj, com.rational.test.ft.script.List slist)
                             throws SubitemNotFoundException, SAFSException{	  
	if (action.equalsIgnoreCase(SELECTUNVERFIEDTEXTNODE) ||
			action.equalsIgnoreCase(CLICKUNVERIFIEDTEXTNODE)) {
		guiObj.click(slist);
	} else if (action.equalsIgnoreCase(ACTIVATEUNVERIFIEDTEXTNODE) ||
			action.equalsIgnoreCase(DOUBLECLICKUNVERIFIEDTEXTNODE)) {
		doDoubleClick(guiObj,slist);
	} else if (action.equalsIgnoreCase(CTRLCLICKUNVERIFIEDTEXTNODE) ||
			action.equalsIgnoreCase(SELECTANOTHERUNVERIFIEDTEXTNODE)) { 
		guiObj.click(Script.CTRL_LEFT, slist);   			   
	} else if (action.equalsIgnoreCase(SHIFTCLICKUNVERIFIEDTEXTNODE)) { 
		guiObj.click(Script.SHIFT_LEFT, slist);
	}else if(action.equalsIgnoreCase(RIGHTCLICKUNVERIFIEDTEXTNODE)){
		guiObj.click(Script.RIGHT,slist);
	}else if(action.equalsIgnoreCase(COLLAPSEUNVERIFIEDTEXTNODE)){
		doExpand(guiObj,slist,false);
	}else if(action.equalsIgnoreCase(EXPANDUNVERIFIEDTEXTNODE)){
		doExpand(guiObj,slist,true);
	}	  
  }
  
	/** <br><em>Purpose:</em> commandWithOneParamWithoutVerification: process commands like: click, doubleclick,
	 ** expand and collapse; ones which take one parameter, (the path in the tree).
	 *  <br>This function will not check the existance of the path to be click.
	 *  <br>If you need that verification, please use commandWithOneParam().
	 **/
	
	private void commandWithOneParamWithoutVerification() throws SAFSException {
	    if (params.size() < 1){
	    	paramsFailedMsg(windowName, compName);
	    	return;
	    }
    	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

    	Iterator iter = params.iterator();
    	//Treate the first parameter path, which is required.
    	String path = iter.next().toString();
    	//Treate the second parameter index, which is optional.
    	String index = null;
    	if(iter.hasNext()){
    		index = StringUtilities.removePrefix(iter.next().toString().toUpperCase(),INDEX_PREFIX);
    	}
    	com.rational.test.ft.script.List slist = null;
    	//process action!
    	try {
    		String altText = action+" performed on '" + path + "' in " + compName + ".";
    		slist = getPathList(path,index);
    		tryUnverifiedListAction(guiObj, slist);  	    		
    		//set status to ok
    		componentSuccessMessage(path);
    		testRecordData.setStatusCode(StatusCodes.OK);
    		return;
    	} 
    	catch (NullPointerException npe) {;}
    	catch (com.rational.test.ft.SubitemNotFoundException snfe) {;}
    	
    	//see if removing a possible first hidden root node works for us
		Subitem[] items = slist.getSubitems();
		Subitem[] ancestorItems = new Subitem[items.length-1];
		System.arraycopy(items,1,ancestorItems,0,ancestorItems.length);
		slist = Script.atList(Script.atList(ancestorItems));
    	try {
    		tryUnverifiedListAction(guiObj, slist);  	    		
    		//set status to ok
    		componentSuccessMessage(path);
    		testRecordData.setStatusCode(StatusCodes.OK);
    	} 
    	catch (NullPointerException npe) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		String detail = failedText.convert("no_node1", "No matching node found for '" + path + "'.", path);
    		componentFailureMessage(detail);
    	}
    	catch (com.rational.test.ft.SubitemNotFoundException snfe) {
    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		String detail = failedText.convert("no_node1", "No matching node found for '" + path + "'.", path);
    		componentFailureMessage(detail);
    	}
    }
  
  /** 
   * Extract the text of the root node in an SWT Tree.<br>
   * Note: SWT Trees can have multiple root nodes.  We currently only return the 
   * text of the first one.
   * 
   * @param                     gstobj, GuiSubitemTestObject
   * @return                    String text of first root node. May be empty or null.
   * @author canagl 09.27.2005                  
   **/
  protected String extractSWTRootName (GuiSubitemTestObject gstobj) {
  	  try{
  	  	  // can we use gstobj.getChildren() here?  Or must we use invoke?
	  	  // TestObject[] items = gstobj.getChildren();
  	  	  TestObject[] items = (TestObject[]) gstobj.invoke("getItems");
  	  	  return (String) items[0].invoke("getText");
  	  }catch(Exception x){;}
  	  return null;
  }
  
  /** 
   * Extract the text of the root node.<br>
   * Note: Swing JTrees only have 1 root node.  However, SWT Trees can have multiple 
   * root nodes.
   * 
   * @param                     gstobj, Object (actually of type GuiSubitemTestObject)
   * @return                    String text of (first) root node. May be empty or null.
   * @author bolawl 08.22.2005  -Added (RJL)
   *         bolawl 08.26.2005  -Updated conditional if userObj == "" (empty string)
   *         canagl 09.27.2005  -Accomodate SWT Trees                 
   **/
  protected String extractRootName (GuiSubitemTestObject gstobj) {
  	String methodName = "extractRootName";
  	if (isSwtDomain(gstobj)) return extractSWTRootName(gstobj);
  	try {
  		TestObject model = (TestObject) gstobj.invoke("getModel");
  		listAllProperties(model, "Model of JTree:");
  		TestObject root  = (TestObject) model.invoke("getRoot");
  		listAllProperties(root, "Root Node of JTree:");
  		Object userObj  = getUserObject(root, new Integer(1));
  		
  		// if needed, try the tree node's userObject
  		if ((userObj == null) || (userObj.toString().equals(""))) {
  	    	Object obj = root.getProperty("userObject");
  	    	userObj = getUserObject(obj, new Integer(1));
  	    	if ((userObj == null) || (userObj.toString().equals(""))) {
  	    		//preserves orig code (tree node's userObject.toString() is last-ditch effort)
  	    		if (obj instanceof TestObject)
  	    			userObj = (String)((TestObject) obj).invoke("toString");
  	    		else
  	    			userObj = obj.toString();
  	    	}
  	    }
  		return userObj.toString().trim(); //FT has a problem with trailing spaces, remove them
  	}
  	catch (Exception e){
        Log.debug ("CFTree Exception caught in "+ methodName + ":", e);
        //return JTree default "root"
  		return "root";
  	}
  }
  
  /** <br><em>Purpose:</em> helper function to check if the root node of the tree is visible
   * @param                     gstobj, Object (actually of type GuiSubitemTestObject--the JTree)
   * @return                    boolean; We assume that root is visible if the rootVisible property 
   *                             is not found or equals anything other than "false".
   *                            Always returns true for SWT widgets.
   * @exception                 PropertyNotFoundException			
   *  
   * @author bolawl 09.08.2005  -Added (RJL)
   *                 
   **/
  protected boolean isRootVisible (GuiSubitemTestObject guiObj) {
  	String methodName = "isRootVisible";
  	//Only java swing may have hidden root?? SWT or .NET roots are always visible!?
  	if(!isJavaDomain(guiObj)) return true; 
  	try {
  		String rootvisible = guiObj.getProperty("rootVisible").toString();
		if (rootvisible.equalsIgnoreCase("false")) 
    		return false;
		else
			return true;
  	}
  	catch (PropertyNotFoundException pnfe){
  		Log.info (methodName +": " + pnfe);
		Log.info (methodName +": returning true; assume that root node is visible.");
  		return true;
  	}
  }
  
  /** 
   * Extract a 'Tree' hierarchy from a TestObject.
   * This method is kept for backward compatibility with old or custom user code.
   * We now use 'to2DArray()' instead.
   * 
   * @param                     obj, Object (actually of type GuiSubitemTestObject--the JTree)
   * @return                    org.safs.Tree or null
   * @exception                 SAFSException
   **/
  protected Tree extractTreeItems (Object obj) throws SAFSException {
    String methodName = "extractTreeItems";
    Tree tree         = null;
    //GuiTestObject gto = null;
    GuiSubitemTestObject nitem  = null;
    try {
        Log.debug ("CFTree retrieving TestData...");
    	nitem = (GuiSubitemTestObject) obj;
    	// known data keys for Tree TestData are "tree" and "selected"
    	ITestDataTree      tdata = (ITestDataTree) nitem.getTestData("tree");
    	ITestDataTreeNodes cnodes = tdata.getTreeNodes();
    	ITestDataTreeNode[] nodes = cnodes.getRootNodes();
    	ITestDataTreeNode   node = nodes[0];
    	Object nodeobj = node.getNode();
    	TestObject topnode = (TestObject) nitem.getSubitem(Script.localAtPath(nodeobj.toString()));

    	//listAllProperties(topnode, "First 'Visible' Root Node of JTree");

    	return extractTreeItemsSub(topnode);

    } catch (Exception ee) {
      Log.info ("CFTree Exception caught in "+ methodName +"\r", ee);
    }
    return null;
  }
  
  /**
   * Overrides CFComponent.captureObjectData.  Provides a List of Lists.  Each List contains one tree 
   * node text at an index appropriate to its position in the tree hierarchy.
   * <pre>
   * Root
   *      Trunk
   *            Branch1
   *                    Leaf1
   *            Branch2
   *                    Leaf2
   * etc.
   * </pre>
   * @see CFComponent#captureObjectData(TestObject)
   */
  protected java.util.List captureObjectData(TestObject tree){
	String debugmsg = getClass().getName()+".captureObjectData() ";
  	rowlist = new ArrayList();	//initialize
  	columns = 0;				//initialize
	int level = 0;				//all trees start at level 0
  	 
  	Log.debug ("CFTree retrieving TestData...");
	//known data keys for Tree TestData are "tree" and "selected"
    GuiSubitemTestObject nitem = (GuiSubitemTestObject) tree;
	String testDataTypeKey = getTestDataTypeKey();
	Log.debug(debugmsg+" get TestData with key '"+testDataTypeKey+"'.");
	ITestDataTree      tdata = (ITestDataTree) nitem.getTestData(testDataTypeKey);
	ITestDataTreeNodes cnodes = tdata.getTreeNodes();
	ITestDataTreeNode[] nodes = cnodes.getRootNodes();
	Log.debug("...Number of RootNodes is " + nodes.length);
	
	//include the hidden root node in 2D array as well
	//this only applies to swing JTrees. SWT trees do not apparently have hidden root nodes.
	//(Note: FT does not return hidden root nodes in getRootNodes()
	boolean visibleRoot = isRootVisible(nitem);
    if (!visibleRoot) {
    	//root is hidden, add it to rowlist
        String rootlabel = extractRootName(nitem);
    	ArrayList rowarray = new ArrayList();
    	rowarray.add(rootlabel);
    	rowlist.add(rowarray);
    	level++;
    }
    Log.debug("...rootvisible is " + visibleRoot);
	
	//iterate through all RootNodes
	for (int i = 0; i < nodes.length; i++) {
		process2DSubtree(nodes[i], level);
	}
  	
  	//no items found in Tree for 2D representation
  	if (rowlist.size() == 0)
  		return null;
  	
  	return rowlist;
  }
  
  /**
   * Overrides CFComponent.formatObjectData.  Provides a formatted String.
   * @see CFComponent#formatObjectData(java.util.List)
   */
  protected String formatObjectData(java.util.List list){
	StringBuffer buf = new StringBuffer();
	java.util.List cols;
	for (int r = 0; r < list.size(); r++) {
		cols = (java.util.List) list.get(r);
		buf.append(System.getProperty("line.separator"));
		for (int c = 0; c < cols.size(); c++) {
			buf.append("\t");
			if (cols.get(c) != null) {
				buf.append(cols.get(c).toString());
				break;
			}
		}
		buf.append("\n");
	}
	return buf.toString();
  }
  
  /** 
   * Return a 2D format of the JTree by first recursively updating the 
   * global rowlist which contains each treenode, and then dumping rowlist into the 2D array.  
   * 2D array is sized by [num. of rows in rowlist][maximum num. of elements of each row in 
   * rowlist].
   * @param                     obj, Object, actually of type GuiSubitemTestObject
   * @return                    arr, a 2D array representation of the Tree, or null
   * @author bolawl				- Added 08.26.2005 (RJL)
   **/
  protected String[][] to2DArray (Object obj) {
  	rowlist = new ArrayList();	//initialize
  	columns = 0;				//initialize
	int level = 0;				//all trees start at level 0
  	String debugmsg = getClass().getName()+".to2DArray() ";
	
  	Log.debug (debugmsg+" retrieving TestData...");
	//known data keys for Tree TestData are "tree" and "selected"
    GuiSubitemTestObject nitem = (GuiSubitemTestObject) obj;
	ITestDataTree       tdata = null;
	ITestDataTreeNodes cnodes = null;
	ITestDataTreeNode[] nodes = null;
	try{
		String testDataTypeKey = getTestDataTypeKey();
		Log.debug(debugmsg+" get TestData with key '"+testDataTypeKey+"'.");
		tdata = (ITestDataTree) nitem.getTestData(testDataTypeKey);
		cnodes = tdata.getTreeNodes();
		//For Flex tree, cnodes.getRootNodes() will always return the root node despite of visibility of root node
		//For Java swing tree, cnodes.getRootNodes() will return root node if the root node is not hide; otherwise return it children.
		nodes = cnodes.getRootNodes();
		Log.debug("...Number of RootNodes is " + nodes.length);
	}catch(NullPointerException np){
		Log.debug("...Number of RootNodes is 0!");
		return null;
	}
	
	//include the hidden root node in 2D array as well
	//this only applies to swing JTrees. SWT trees do not apparently have hidden root nodes.
	//(Note: FT does not return hidden root nodes in getRootNodes()
	boolean rootVisible = isRootVisible(nitem);
	//For Flex tree, even if the root is not visible, cnodes.getRootNodes() will return root node,
	//so we should not add the other root label.
	//Only need to add the root label for Java swing tree.
    if (!rootVisible && isJavaDomain(nitem)) {
    	//root is hidden, add it to rowlist
        String rootlabel = extractRootName(nitem);
    	ArrayList rowarray = new ArrayList();
    	rowarray.add(rootlabel);
    	rowlist.add(rowarray);
    	level++;
    }
    Log.debug("...rootvisible is " + rootVisible);
	
	//iterate through all RootNodes
	for (int i = 0; i < nodes.length; i++) {
		process2DSubtree(nodes[i], level);
	}
  	
  	//no items found in Tree for 2D representation
  	if (rowlist.size() == 0)
  		return null;
  	
  	//build arr from global rowlist
  	String arr[][] = new String[rowlist.size()][columns + 1];
  	int rowcount = 0;
  	while (rowcount < rowlist.size()) {
  		ArrayList row = (ArrayList) rowlist.get(rowcount);
  		int colcount = 0;
  		while (colcount < row.size()) {
  			if (row.get(colcount) != null)
  				arr[rowcount][colcount] = (String) row.get(colcount);
  			//Log.debug("...arr["+rowcount+"]["+colcount+"] is " + row[colcount]);
  			colcount++;
  		}
  		rowcount++;
  	}
  	return arr;
  }
  
  /** 
   * Recursive helper function for to2DArray().  This function will
   * first save the node string for subtree into the global rowarray (ArrayList), then handle each 
   * child and sibling.  It also keeps track of the max number of columns necessary for the 2D 
   * array.
   * @param                     treenode, ITestDataTreeNode (the treenode)
   * @exception                 ArrayIndexOutOfBoundsException
   * @author bolawl				- Added 08.26.2005 (RJL)
   **/
  protected void process2DSubtree (ITestDataTreeNode treenode, int lev) {
  	String methodName = "process2DSubtree";
  	//store data from treenode at level lev
  	try {
  		ArrayList rowarray;
  		rowarray = new ArrayList(lev+1);
  		//Log.debug("...treenode.getNode().toString() is " + treenode.getNode().toString());
  		for(int fill = 0;fill<lev;fill++)rowarray.add(null);
  		
  		rowarray.add(lev, getTreeNodeValue(treenode));
  		rowlist.add(rowarray);
  	}
  	catch (IndexOutOfBoundsException aiobe) {
  		Log.debug ("CFTree Exception caught in "+ methodName, aiobe);
  		return;
  	}
  	
  	//update max number of columns if necessary
  	if (lev > columns)
  		columns = lev;
  	
  	//recursively process each child
  	//TODO For Flex tree, only the nodes displayed on the screen will be returned.
  	//This is a big problem. We should not expand all nodes to make them visible.
  	//I decide ingore this problem, hope RFT will fix this bug.
  	int childcount = treenode.getChildCount();
  	ITestDataTreeNode[] children = treenode.getChildren();
  	for (int i = 0; i < childcount; i++) {
  		process2DSubtree(children[i], lev + 1);
  	}
  }
  
  /**
   * <em>Note:</em> Get the tree node's text value.
   * @param treenode
   * @return
   */
  protected String getTreeNodeValue(ITestDataTreeNode treenode){
	  String debugmsg = this.getClass().getName()+".getTreeNodeValue(): ";
	  String nodeValue = "";
	  
	  if(treenode==null){
		  Log.warn(debugmsg+" the treenode is null, nodeValue="+nodeValue);
		  return nodeValue;
	  }else{
		  //Assign a default value to the nodeValue
		  //nodeValue = treenode.toString();
	  }
	  
	  Object nodeObject = treenode.getNode();
	  if(nodeObject==null){
		  Log.warn(debugmsg+" the nodeObject is null, nodeValue="+nodeValue);
		  return nodeValue;
	  }else{
		  //Assign a default value to the nodeValue
		  nodeValue = nodeObject.toString();
	  }
	  
	  Log.debug(debugmsg+"Tree Node Class is "+nodeObject.getClass().getName());
	  if(nodeObject instanceof ObjectReference){
		  ObjectReference reference = (ObjectReference) nodeObject;
		  Object referedObject = reference.getObject();
		  if(referedObject==null){
			  Log.warn(debugmsg+" the referedObject is null, nodeValue="+nodeValue);
			  return nodeValue;
		  }
		  Log.debug(debugmsg+"Included Object's class is "+referedObject.getClass().getName());
		  nodeValue = referedObject.toString();
	  }else if(nodeObject instanceof ITestDataText){
		  nodeValue = ((ITestDataText) nodeObject).getText();	  
	  }else{
		  //TODO For other types, need more codes to handle 
	  }
	  
	  Log.debug(debugmsg+" nodeValue is "+nodeValue);
	  
	  return nodeValue;
  }
  
  /** <br><em>Purpose:</em> capture the 'Tree' hierarchy data (from a TestObject) to a file.
   * @exception                 SAFSException
   * @author bolawl				-Added 08.26.2005 (RJL)
   *  <br>	08.26.2005 (bolawl) -Updated to trim off hidden root node before logging success
   *  <br>                       and improved success message. (RJL)
   *  <br>  10.13.2005 (canagl) -Changes to (hopefully) better support SWT
   *  <br>  01.11.2006 (bolawl) -Updated to format output file with new optional parameter 
   *                             indentMark (default is tab \t). (RJL)
   **/
  protected void captureTreeDataToFile () throws SAFSException {
    String methodName = "captureTreeDataToFile";
    //get params
    Iterator iterator = params.iterator();
    String filename = "";
    String updatedbranch = "";
    String fileEncoding = null;
    int startrow = 0;
	int startcol = 0;
    
    try {
    	if (params.size() < 1)
    		paramsFailedMsg(windowName, compName); 
    	else {
    		//get the 2D representation of the tree
    		GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
    		String[][] a2DTree = to2DArray(guiObj);	
    		if (a2DTree == null) {
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			log.logMessage(testRecordData.getFac(),
    	                       testRecordData.getCommand()+
    	                       " failure, could not process " + compName + ".", 
							   FAILED_MESSAGE);
    	    	return;
    		}

    		//verify file
    		filename = (String) iterator.next();
    		filename = deduceTestFile(filename).getAbsolutePath();
    		
    		//branch?
    		String branch = new String("");
    		boolean do_all = true;
    		
    		try {
    			branch   = (String) iterator.next();	
    		}
    		catch (NoSuchElementException nse) {
    			//exception is okay, branch is optional parameter
    			Log.info (methodName +": Optional branch not specified");
    		}
    		if (branch.equals("")) {
    			log.logMessage(testRecordData.getFac(), 
    					       "Proceeding with no Tree branch/node specified for " +
    					       testRecordData.getCommand() + 
							   " in table " + testRecordData.getFilename() + 
							   " at line " + Long.toString(testRecordData.getLineNumber()) + ".",
							   GENERIC_MESSAGE);
    		}
    			
    		if (branch.length()>0) {
    			//pre-pend (hidden) root in branch, if not specified by user
    			//assume true root is at position a2DTree[0][0] (we built the array!)
    			//retrieve path root node from user supplied path
    			//caveat: SWT Trees may have more than a single root node
    			//so the user MUST specify the root node in the path
    			updatedbranch = branch;
    			do_all = false;    			
    			if(isJavaDomain(guiObj) || isFlexDomain(guiObj)){ // java-swing or flex trees enter here
	    			String pathrootnode = branch;
	    			int stripArrowI = branch.indexOf(NODE_DELIMIT); // the first occurance of delimiter
	    			boolean singlenodepath = stripArrowI < 0;
	    			if (!singlenodepath) 
	    				pathrootnode = branch.substring(0, stripArrowI);
	
    				String rootlabel = a2DTree[0][0];
    				String delimitedrootlabel = rootlabel + NODE_DELIMIT;
    			
	    			//pre-pend root if not already included in path
	    			if (partialmatch) {				
	    				if (!(rootlabel.toLowerCase().indexOf(pathrootnode.toLowerCase()) >= 0)) {
	    					updatedbranch = delimitedrootlabel + branch;
	    				}
	    			}	
	    	      	else {
	    	      		if (!(branch.startsWith(rootlabel))) {
	    	      			updatedbranch = delimitedrootlabel + branch;
	    	      		}
	    	      	}
    			}
    		}
    		
    		//search for branch in 2D array
    		NodeInfo node = DataUtilities.getObjectDataNodeInfo (a2DTree, updatedbranch, NODE_DELIMIT, partialmatch);
    		
    		if (node == null) {
    			//branch was not found in tree
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		    String detail = failedText.convert("no_node1", "No matching node found for '" + branch + "'.", branch);
    		    componentFailureMessage(detail);
    		   	return;
    		}
    		else {
    			//branch found, reset start values
    			startrow = node.getPoint().x;
    			startcol = node.getPoint().y;
    		}
    		
    		//indentMark? (01.11.2006 RJL)
    		String indentMark = new String("");	
    		try {
    			indentMark   = (String) iterator.next();	
    		}
    		catch (NoSuchElementException nse) {
    			//exception is okay, indentMark is optional parameter
    			Log.info (methodName +": Optional indentMark not specified");
    		}
    		if (indentMark.equals("")) indentMark = "\t";//default to tab char
    		
    		//File Encoding
      		if(iterator.hasNext()){
      			fileEncoding = (String) iterator.next();
        		//If user put a blank string as encoding,
        		//we should consider that user does NOT provide a encoding, reset encoding to null.
        		fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
      		}
      		Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);
      		
    		//Write desired portion of 2D array to file starting at (startrow, startcol).
    		//First, write contents of cell (startrow, startcol) since we can assume
    		//there will always be data in that cell because 1) the 2D array representation
    		//of tree will always contain root node, or 2) cell coordinates were returned 
    		//from successful search in getObjectDataNodeInfo(). The format of a2DTree also
    		//allows us to assume that once cell data is found in a row, there is no more 
    		//data in that row. Likewise, once cell data is again found in startcol, we've 
    		//reached the end of the items in that branch. (RJL)
    		Collection contents = new ArrayList();
    		String outputrow = new String("");
    		contents.add(a2DTree[startrow][startcol]);
    		for (int r = startrow + 1; r < a2DTree.length; r++) {
    			for (int c = startcol; c < a2DTree[0].length; c++) {
    				if (a2DTree[r][c] != null) {
    					if (c == startcol) {
    						if (! do_all) {
	    						//we've reached the end of items in this branch, exit loops
	    						c = a2DTree[0].length;
	    						r = a2DTree.length; 
    						}
    						else{
    							outputrow = a2DTree[r][c];
    							break;
    						}
    					}
    					else {
    						//data found, write out contents of cell
    						outputrow = outputrow + a2DTree[r][c];
    						break;
    					}
    				}
    				else
    					//empty cell, write out indentMark instead (01.11.2006 RJL)
    					outputrow = outputrow + indentMark;
    			}
    			if (r < a2DTree.length && !outputrow.trim().equals("")) {
    				//write out this row to contents
    				contents.add(outputrow);
    				outputrow = "";
    			}
    		}
  	      	//If a file encoding is given or we need to keep the encoding consistent
  	      	if (fileEncoding != null || keepEncodingConsistent) {
  	      		StringUtils.writeEncodingfile(filename, contents,fileEncoding);
  	      	} else {
  	      		// Keep compatible with old version
  	      		StringUtils.writefile(filename, contents);
  	      	}
    	    
    	    //set status to ok
    		String newpath = node.getPath();
    		try {
    			//Only java-swing may have hidden root????
    			if(isJavaDomain(guiObj)){
	    			//strip off root node if not visible.		09.08.2005 RJL
	        		//we already know that newpath includes it!
	    			String rootlabel = a2DTree[0][0];
	    			String delimitedrootlabel = rootlabel + NODE_DELIMIT;
	        		if (!isRootVisible(guiObj)) 
	            		newpath = newpath.substring(delimitedrootlabel.length());
    			}
        	}
        	catch (StringIndexOutOfBoundsException siobe) {
        		Log.info (methodName +": Path only contains hidden root node '" + newpath + "'.", siobe);
        		branch = "";
    		}	
    	    testRecordData.setStatusCode(StatusCodes.OK);
    	    if (!branch.equals("")) {
    	    	String altText = action + " performed on '" + newpath + "' in " + compName + ";" +
    	    	" output file '" + filename + "'.";
    	    	log.logMessage(testRecordData.getFac(), passedText.convert("perfnode4", 
    	    			   altText, action, newpath, compName, filename), PASSED_MESSAGE);
    	    }
    	    else {
    	    	String altText = action + " performed on " + compName + ";" +
    			" output file '" + filename + "'.";
    	    	log.logMessage(testRecordData.getFac(), passedText.convert("perfnode4a", 
 	    			   altText, action, compName, filename), PASSED_MESSAGE);
    	    }
    	}
    }
    catch (IOException ioe) {
    	Log.debug ("CFTree Exception caught in "+ methodName, ioe);
    	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
    }
    catch (SAFSException se) {
    	Log.debug ("CFTree Exception caught in "+ methodName, se);
      	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
        			   testRecordData.getCommand()+
                       " SAFSException: " + se.getMessage(),
                       FAILED_MESSAGE);
    }
    catch (IndexOutOfBoundsException aiobe) {
    	Log.debug ("CFTree Exception caught in "+ methodName, aiobe);
      	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      	log.logMessage(testRecordData.getFac(),
      				   testRecordData.getCommand()+
      				   " failure, could not process " + compName + ".",
					   FAILED_MESSAGE);
  	}
    catch (Exception ee) {
        Log.debug ("CFTree Exception caught in "+ methodName, ee);
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        componentFailureMessage(ee.getMessage());
    }   
  }
  
  /** <br><em>Purpose:</em> Verify the existence of a node in a tree.
   * <br>
   * <br>  Since the basic functionality is the same, this routine is 
   * <br>  used to both verify that a node is found in a tree 
   * <br>  (VerifyTreeContainsNode/PartialMatch) and to set a user-defined 
   * <br>  variable with those results (SetTreeContainsNode/PartialMatch).
   * <br>
   * <br>  For SetTreeContainsNode/PartialMatch commands only, the name of 
   * <br>  the variable which gets assigned the result will be set to the
   * <br>  string 'TRUE' if the node is found, or the string 'FALSE' if
   * <br>  the node is not found.
   * <br>
   * @exception                 SAFSException
   * @author bolawl				-Added 01.19.2006 (RJL)
   * <br>	
   **/
  protected void verifyTreeContains () throws SAFSException {
    String methodName = "verifyTreeContains";
    //get params
    Iterator iterator = params.iterator();
    String updatedbranch = "";
    boolean isSetCmd = false;
	String varName = new String("");
    
	//determine command type Verify* or Set*
    if (action.equalsIgnoreCase(SETTREECONTAINSNODE) ||
   		action.equalsIgnoreCase(SETTREECONTAINSPARTIAL)) {
    	isSetCmd = true;
    }
    
    try {
    	if (params.size() < 1)
    		paramsFailedMsg(windowName, compName); 
    	else {
    		
    		//get searchnode
    		String searchnode = new String("");
    		try {
    			searchnode   = (String) iterator.next();	
    		}
    		catch (NoSuchElementException nse) {
    			//no node specified
    			Log.debug (methodName +": Node not specified!");
    		}
    		if (searchnode.equals("")) {
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			log.logMessage(testRecordData.getFac(), 
    					       "No node specified for " + testRecordData.getCommand() + 
							   " in table " + testRecordData.getFilename() + 
							   " at line " + Long.toString(testRecordData.getLineNumber()) + ".",
							   FAILED_MESSAGE, testRecordData.getInputRecord());
    			return;
    		}
    		
    		//if Set* command, get the varName
    		if (isSetCmd) {
    			try {
    				varName   = (String) iterator.next();	
    				}
    			catch (NoSuchElementException nse) {
    				//no varName specified
    				Log.debug (methodName +": Variable name not specified!");
    			}
    			if (varName.equals("")) {
    				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    				log.logMessage(testRecordData.getFac(), 
    					       	   "No variable name specified for " + testRecordData.getCommand() + 
							       " in table " + testRecordData.getFilename() + 
							       " at line " + Long.toString(testRecordData.getLineNumber()) + ".",
							       FAILED_MESSAGE, testRecordData.getInputRecord());
    				return;
    			}
    		}
    		
    		//get the 2D representation of the tree
    		GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
    		String[][] a2DTree = to2DArray(guiObj);	
    		if (a2DTree == null) {
    			Log.debug (methodName +": Could not read tree data into 2D array!");
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			log.logMessage(testRecordData.getFac(),
    	                       "No object data was extracted for " + testRecordData.getCommand()+
    	                       " in table " + testRecordData.getFilename() + 
						       " at line " + Long.toString(testRecordData.getLineNumber()) + ".",
						       FAILED_MESSAGE, testRecordData.getInputRecord());
    	    	return;
    		}
    			
    		//pre-pend (hidden) root in branch, if not specified by user
    		//assume true root is at position a2DTree[0][0] (we built the array!)
    		//retrieve path root node from user supplied path
    		//caveat: SWT Trees may have more than a single root node
    		//so the user MUST specify the root node in the path
    		updatedbranch = searchnode;			
    		if(isJavaDomain(guiObj) || isFlexDomain(guiObj)){ // java-swing or flex trees enter here
	    		String pathrootnode = searchnode;
	    		int stripArrowI = searchnode.indexOf(NODE_DELIMIT); // the first occurance of delimiter
	    		boolean singlenodepath = stripArrowI < 0;
	    		if (!singlenodepath) 
	    			pathrootnode = searchnode.substring(0, stripArrowI);
	
    			String rootlabel = a2DTree[0][0];
    			String delimitedrootlabel = rootlabel + NODE_DELIMIT;
    			
	    		//pre-pend root if not already included in path
	    		if (partialmatch) {				
	    			if (!(rootlabel.toLowerCase().indexOf(pathrootnode.toLowerCase()) >= 0)) {
	    				updatedbranch = delimitedrootlabel + searchnode;
	    			}
	    		}	
	    	    else {
	    	     	if (!(searchnode.startsWith(rootlabel))) {
	    	     		updatedbranch = delimitedrootlabel + searchnode;
	    	     	}
	    	    }
    		}
    		
    		//search for searchnode in 2D array
    		NodeInfo node = DataUtilities.getObjectDataNodeInfo (a2DTree, updatedbranch, NODE_DELIMIT, partialmatch);
    		
    		String nodefound = new String("FALSE");
    		String detail = new String("");
    		int typeMessage = PASSED_MESSAGE;
    		if (node == null) {
    			//searchnode was not found in tree
    			Log.info (methodName +": Node not found in tree.");
    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			typeMessage = FAILED_MESSAGE;
    			if (isSetCmd) {
    				//Set* command, so set varname to "FALSE" - this is not a test failure
    				setVariable(varName, nodefound);
    				String altText = "Value '" + nodefound + "' was assigned to variable '" + varName + "'.";
        	    	detail = passedText.convert("varAssigned2", altText, nodefound, varName); 
    				typeMessage = GENERIC_MESSAGE;
    				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    			}
    			String altText = action + " did not find node '" + searchnode + "' in " + compName + ".";
    	    	log.logMessage(testRecordData.getFac(), failedText.convert("no_node3", 
 	    			   altText, action, searchnode, compName), typeMessage, detail);
    		   	return;
    		}
    		else {
    			//searchnode was found in tree
    			Log.info (methodName +": Node found in tree.");
    			nodefound = "TRUE";
    			if (isSetCmd) {
    				//Set* command, so set varName to "TRUE"
    				setVariable(varName, nodefound);
    			}
    		}
    		
    	    //trim off prepended hidden root before logging success
    		String newpath = node.getPath();
    		try {
    			//Java-swing or flex may have hidden root
    			if(isJavaDomain(guiObj) || isFlexDomain(guiObj)){
	    			//strip off root node if not visible.
	        		//we already know that newpath includes it!
	    			String rootlabel = a2DTree[0][0];
	    			String delimitedrootlabel = rootlabel + NODE_DELIMIT;
	        		if (!isRootVisible(guiObj)) 
	            		newpath = newpath.substring(delimitedrootlabel.length());
    			}
        	}
        	catch (StringIndexOutOfBoundsException siobe) {
        		Log.info (methodName +": Path only contains hidden root node '" + newpath + "'.");
        		Log.info (methodName +":"+ siobe);
    		}	
    	   
        	//log success
    	    if (isSetCmd) {
    	    	String altText = "Value '" + nodefound + "' was assigned to variable '" + varName + "'.";
    	    	detail = passedText.convert("varAssigned2", altText, nodefound, varName); 
    	    	typeMessage = GENERIC_MESSAGE;
    	    }
    	    testRecordData.setStatusCode(StatusCodes.OK);
    	    String altText = action + " found node '" + newpath + "' in " + compName + ".";
    	    log.logMessage(testRecordData.getFac(), passedText.convert("found3", 
    	    			   altText, action, newpath, compName), typeMessage, detail);
    	}
    }
    catch (SAFSException se) {
    	Log.debug ("CFTree Exception caught in "+ methodName);
        Log.debug (methodName +":"+ se);
      	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      	componentFailureMessage(se.getMessage());
    }
    catch (Exception ee) {
        Log.debug ("CFTree Exception caught in "+ methodName);
        Log.debug (methodName +":"+ ee);
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        componentFailureMessage(ee.getMessage());
    }   
  }

  /**
   * <em>Note:</em>			This method can only treate the java-swing component,
   * 						For other appliction, it needed to be overrided.
   * @param guiObj			Represent a javax.swing.JTree object.
   * @param testPathList	The path to be tested if it is the selected path.
   * @return				True if the path is selected; False otherwise.
   * @throws				SAFSException
   */
  protected boolean isNodeSelected(GuiSubitemTestObject guiObj,com.rational.test.ft.script.List testPathList) throws SAFSException{
  	String debugmsg = getClass().getName()+".verifyNodeSelected() ";
	Subitem[] pathToBeTested= testPathList.getSubitems();
	
	try{
		Object selectedPathesArray = guiObj.invoke(METHOD_GETSELECTIONPATHS);
		
		if(selectedPathesArray==null){
			Log.debug(debugmsg+" No tree node has been selected.");
			return false;
		}
		
		int length = Array.getLength(selectedPathesArray);
outer:	for(int j=0;j<length;j++){
			TestObject pathobj = (TestObject) Array.get(selectedPathesArray, j);
			if(pathobj==null){
				Log.debug(debugmsg+" Can not get TreePath object.");
				continue outer;
			}
			Log.debug("SELECTED PATH: "+pathobj.invoke(METHOD_TOSTRING));
			
			Object[] selectedNodes = (Object[]) pathobj.invoke(METHOD_GETPATH);
			
			if(selectedNodes==null){
				Log.debug(debugmsg+" Can not get the array of selected path");
				continue outer;
			}
			
			//If root is no visible, pathToBeTested will not contain the root, but selectedPath contains the root
			//so we need skip the first object of selectedPath
			String[] selectedPath = null;
			if(!isRootVisible(guiObj)){
				Log.debug(debugmsg+" Root is not visible.");
				selectedPath = new String[selectedNodes.length-1];
				for(int i=0;i<selectedPath.length;i++){
					selectedPath[i] = ((TestObject)selectedNodes[i+1]).getProperty(PROPERTY_USEROBJECT).toString();
				}
			}else{
				Log.debug(debugmsg+" Root is visible.");
				selectedPath = new String[selectedNodes.length];
				for(int i=0;i<selectedPath.length;i++){
					selectedPath[i] = ((TestObject)selectedNodes[i]).getProperty(PROPERTY_USEROBJECT).toString();
				}
			}
			
			//Compare each item on the path
			if(selectedPath.length != pathToBeTested.length){
				continue outer;
			}else{
				for(int i=0;i<pathToBeTested.length;i++){
					String node1 = ((Value)pathToBeTested[i]).getValue().toString();
					String node2 = selectedPath[i];
					Log.debug(debugmsg+" comparing... "+node1+" and "+node2);
					if(!node1.equals(node2)){
						continue outer;
					}
				}
				return true;
			}
		}
	}catch(PropertyNotFoundException e1){
		Log.debug(debugmsg+e1.getMessage());
		throw new SAFSException(failedText.text(FAILStrings.PROPERTY_NOT_FOUND, "Can not find property.")+e1.getMessage());
	}catch (MethodNotFoundException e2) {
		Log.debug(debugmsg+e2.getMessage());
		throw new SAFSException(failedText.text(FAILStrings.METHOD_NOT_FOUND, "Can not find method.")+e2.getMessage());
	}
	return false;
  }
  
  /**
   * @param stringValue
   * @return	int
   */
  private int convertToInteger(String stringValue){
  	int intValue = 1;
  	String debugmsg = getClass().getName()+".convertToInteger() ";
  	try{
  		intValue = new Integer(stringValue).intValue();
  	}catch(NumberFormatException e){
  		Log.debug(debugmsg+" Can not convert "+stringValue+" to Integer.");
  	}
  	return intValue;
  }
  /**
   * @param path,	String, full path of a tree node. Example: aTree->aPath->aNode
   * @param index,	String, indicate the Nth duplicate item of node. 
   * 				<br>Example: if index is 2, we return the seconde "aNode" under "aTree->aPath"
   * @return
   */
  private com.rational.test.ft.script.List getPathList(String path,String index){
	com.rational.test.ft.script.List slist = Script.atPath(path);

	//If optional parameter is provided, use this parameter to reconstruct list.
	if(index!=null){
		Subitem[] items = slist.getSubitems();
		Subitem[] ancestorItems = new Subitem[items.length-1];
		System.arraycopy(items,0,ancestorItems,0,ancestorItems.length);
		String lastNodeName = ((Value)items[items.length-1]).getValue().toString();
		slist = Script.atList(Script.atList(ancestorItems),Script.atText(lastNodeName,convertToInteger(index)-1));
	}
	
	return slist;
  }
  
  /**
   * Note:		Subclass may override this method.
   * 			RFT TestObject.getTestDataTypes().keys() will return a collection of key, select the 
   * 			correct one as the return value.
   * @return	String, the correct key of TestData, which is used to get the Tree content
   */
  protected String getTestDataTypeKey(){
	//known data keys for Tree TestData are "tree" and "selected"
	//For Java, .NET Form, Flex, we will return the key "tree"
	return "tree";
  }
}
