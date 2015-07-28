/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.model.commands.TreeViewFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.Tree;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.selenium.webdriver.lib.model.TreeNode;
import org.safs.text.FileUtilities;
import org.safs.tools.stringutils.StringUtilities;

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 20, 2014    (DHARMESH4) Initial release.
 *  <br>   JUN 10, 2014    (Lei Wang) Implement keywords.
 *  <br>   MAR 03, 2015    (Lei Wang) Will not verify that node is selected for keywords RightClickPartial, RightClickTextNode.
 *                                  The right click should not be expected to select a tree node.
 */
public class CFTree extends CFComponent {
	
	Tree tree = null;
	
	public CFTree() {
		super();		
	}
	
	/** sub class need to override this method and provide its own library Component.*/
	protected Tree newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new Tree(webelement);
	}
	
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();				
				tree = (Tree) libComponent;
				
				IndependantLog.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				//Handle keywords with one-required parameter
				if(action.equalsIgnoreCase(TreeViewFunctions.CAPTURETREEDATATOFILE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.CLICKPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.CLICKTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.CLICKUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSETEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.CTRLCLICKUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.EXPAND_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.EXPANDPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.EXPANDUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.SELECT_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.SELECTPARTIAL_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.SELECTUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.SHIFTCLICKUNVERIFIEDTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.VERIFYNODEUNSELECTED_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.VERIFYSELECTEDNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSPARTIALMATCH_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.SELECTTEXTNODE_KEYWORD)
				   ||action.equalsIgnoreCase(TreeViewFunctions.EXPANDTEXTNODE_KEYWORD)
				   ){
					
					if(params.size() < 1){
						issueParameterCountFailure();
						return;
					}
					
					if(processWithOneRequiredParameter()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
								windowName, compName, action);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
					}
					
				}else if(action.equalsIgnoreCase(TreeViewFunctions.SETTREECONTAINSNODE_KEYWORD)
						 ||action.equalsIgnoreCase(TreeViewFunctions.SETTREECONTAINSPARTIALMATCH_KEYWORD)
						 ){
					if(params.size() < 2){
						issueParameterCountFailure();
						return;
					}
					
					if(processWithTwoRequiredParameters()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
								windowName, compName, action);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
					}
					
				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					IndependantLog.warn(debugmsg+action+" could not be handled here.");
				}
				
			}catch(Exception e){
				IndependantLog.error(debugmsg+"Selenium TreeView Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+StringUtils.debugmsg(e);
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean processWithOneRequiredParameter() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneRequiredParameter");
		String requiredParam = (String) iterator.next();
		
		//Handle optionl parameters
		int matchIndex = 1;
		String branch = null;
		String indentMark = null;
		String encoding = null;

		if(action.equalsIgnoreCase(TreeViewFunctions.CLICKPARTIAL_KEYWORD)
		   ||action.equalsIgnoreCase(TreeViewFunctions.CLICKTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.CLICKUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSETEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.CTRLCLICKUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.EXPAND_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.EXPANDPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.EXPANDUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SELECT_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SELECTPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SELECTUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SHIFTCLICKUNVERIFIEDTEXTNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.VERIFYNODEUNSELECTED_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.VERIFYSELECTEDNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSPARTIALMATCH_KEYWORD)
		   ){

			//'matchIndex' optional parameter
			if(iterator.hasNext()) matchIndex = StringUtilities.getIndex((String)iterator.next());
		}
		else if(action.equalsIgnoreCase(TreeViewFunctions.CAPTURETREEDATATOFILE_KEYWORD)){
			//'encoding' optional parameter
			if(iterator.hasNext()) branch = (String)iterator.next();
			if(iterator.hasNext()) indentMark = (String)iterator.next();
			if(iterator.hasNext()) encoding = (String)iterator.next();
			if(indentMark==null || indentMark.trim().isEmpty()) indentMark = INDENT_MARK;
		}
		
		matchIndex--;//convert 1-based index to 0-based index
		
		if(action.equalsIgnoreCase(TreeViewFunctions.CLICKPARTIAL_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SELECTPARTIAL_KEYWORD)
			){
			tree.selectItem(new TextMatchingCriterion(requiredParam, true, matchIndex), true, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.CLICKTEXTNODE_KEYWORD)
				||action.equalsIgnoreCase(TreeViewFunctions.SELECT_KEYWORD)
				||action.equalsIgnoreCase(TreeViewFunctions.SELECTTEXTNODE_KEYWORD)
			){
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), true, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
			//tree.SelectTextNode(requiredParam);//used for keyword SELECTTEXTNODE_KEYWORD
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.CLICKUNVERIFIEDTEXTNODE_KEYWORD)
				||action.equalsIgnoreCase(TreeViewFunctions.SELECTUNVERIFIEDTEXTNODE_KEYWORD)
			){
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.COLLAPSE_KEYWORD)
				||action.equalsIgnoreCase(TreeViewFunctions.COLLAPSETEXTNODE_KEYWORD)
				){
			tree.collapseItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, true);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEPARTIAL_KEYWORD)){
			tree.collapseItem(new TextMatchingCriterion(requiredParam, true, matchIndex), false, true);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.COLLAPSEUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.collapseItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, false);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.CTRLCLICKUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, Keys.CONTROL, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKPARTIAL_KEYWORD)){
			tree.activateItem(new TextMatchingCriterion(requiredParam, true, matchIndex), true, null, null);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKTEXTNODE_KEYWORD) ){
			tree.activateItem(new TextMatchingCriterion(requiredParam, false, matchIndex), true, null, null);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.DOUBLECLICKUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.activateItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, null, null);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.EXPAND_KEYWORD)
				|| action.equalsIgnoreCase(TreeViewFunctions.EXPANDTEXTNODE_KEYWORD)
				){
			tree.expandItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, true);
			//tree.ExpandTextNode(requiredParam);//used for keyword EXPANDTEXTNODE_KEYWORD

		}else if(action.equalsIgnoreCase(TreeViewFunctions.EXPANDPARTIAL_KEYWORD)){
			tree.expandItem(new TextMatchingCriterion(requiredParam, true, matchIndex), false, true);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.EXPANDUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.expandItem(new TextMatchingCriterion(requiredParam, true, matchIndex), false, false);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKPARTIAL_KEYWORD)){
//			tree.selectItem(new TextMatchingCriterion(requiredParam, true, matchIndex), true, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);
			tree.selectItem(new TextMatchingCriterion(requiredParam, true, matchIndex), false, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKTEXTNODE_KEYWORD)){
//			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), true, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.RIGHTCLICKUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.SHIFTCLICKUNVERIFIEDTEXTNODE_KEYWORD)){
			tree.selectItem(new TextMatchingCriterion(requiredParam, false, matchIndex), false, Keys.SHIFT, null, WDLibrary.MOUSE_BUTTON_LEFT);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.VERIFYNODEUNSELECTED_KEYWORD)){
			tree.verifyItemSelection(new TextMatchingCriterion(requiredParam, false, matchIndex), false);

		}else if(action.equalsIgnoreCase(TreeViewFunctions.VERIFYSELECTEDNODE_KEYWORD)){
			tree.verifyItemSelection(new TextMatchingCriterion(requiredParam, false, matchIndex), true);
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSNODE_KEYWORD)){
			tree.verifyContains(new TextMatchingCriterion(requiredParam, false, matchIndex));
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.VERIFYTREECONTAINSPARTIALMATCH_KEYWORD)){
			tree.verifyContains(new TextMatchingCriterion(requiredParam, true, matchIndex));
			
		}else if(action.equalsIgnoreCase(TreeViewFunctions.CAPTURETREEDATATOFILE_KEYWORD)){
			TreeNode[] contents = null;
			if(branch==null || branch.isEmpty()){
				contents = tree.getContent();
			}else{
				//if branch contains value, find the correspond node.
				TreeNode node = tree.getMatchedElement(new TextMatchingCriterion(branch, false, 0));
				contents = new TreeNode[1];
				contents[0] = node;
			}

			BufferedWriter writer = null;
			try {
				//requiredParam is filename
				String filename = deduceTestFile(requiredParam).getAbsolutePath();
				writer = FileUtilities.getBufferedFileWriter(filename, encoding);
				writeTreeNodesToFile(writer, contents, indentMark, 0);
			} catch (FileNotFoundException e) {
				throw new SeleniumPlusException("Can not find file '"+requiredParam+"'");
			}finally{
				try { if(writer!=null) writer.close(); } catch (IOException ignore) {}
			}
		}
		else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
			return false;
		}

		return true;
	}
	
	/**
	 * process keywords who need at least two parameters; but it may be supplied with optional parameters.
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException if there are any problem during handling keyword.
	 */
	private boolean processWithTwoRequiredParameters() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithTwoRequiredParameters");
		String requiredParam = (String) iterator.next();
		String variable = (String) iterator.next();
		
		if(action.equalsIgnoreCase(TreeViewFunctions.SETTREECONTAINSNODE_KEYWORD)
			||action.equalsIgnoreCase(TreeViewFunctions.SETTREECONTAINSPARTIALMATCH_KEYWORD)
			){
			try{
				boolean partialMatch = action.equalsIgnoreCase(TreeViewFunctions.SETTREECONTAINSPARTIALMATCH_KEYWORD);
				tree.verifyContains(new TextMatchingCriterion(requiredParam, partialMatch, 0));
				setVariable(variable, Boolean.toString(true));
			}catch(SeleniumPlusException se){
				if(!SeleniumPlusException.CODE_VERIFICATION_FAIL.equals(se.getCode())) throw se;
				setVariable(variable, Boolean.toString(false));
			}
		}
		else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
			return false;
		}
		
		return true;
	}

	private void writeTreeNodesToFile(BufferedWriter writer, TreeNode[] nodes, String indentMark, int level) throws SeleniumPlusException{

		for(TreeNode node: nodes){
			try{
				if(indentMark!=null && !indentMark.isEmpty()) for(int i=0;i<level;i++) writer.write(indentMark); 
				writer.write(node.getLabel()+Console.EOL);
			} catch (IOException e) {
				throw new SeleniumPlusException("Can not write to file due to "+e.getMessage());
			}
			writeTreeNodesToFile(writer, node.getChildren(), indentMark, level+1);
		}
	}

}
	
