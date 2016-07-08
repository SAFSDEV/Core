/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * 
 * History:
 * 
 *  May 30, 2014    (Lei Wang) Initial release.
 *  Jul 08, 2016    (Lei Wang) Add checkIfDisabled(): Log warning message instead of throwing Exception if the node is not enabled.
 */
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * 
 * May 30, 2014    (Lei Wang) Initial release.
 */
public abstract class AbstractTreeSelectable extends AbstractHierarchicalSelectable implements ITreeSelectable{

	/**
	 * @param parent
	 * @throws SeleniumPlusException
	 */
	public AbstractTreeSelectable(Component parent) throws SeleniumPlusException {
		super(parent);
	}
	
	public TreeNode[] getContent() throws SeleniumPlusException{
		try{
			return (TreeNode[]) super.getContent();
		}catch(SeleniumPlusException se){
			throw se;
		}catch(Exception e){
			String msg = "Fail to convert content to TreeNode[].";
			IndependantLog.error(msg, e);
			throw new SeleniumPlusException(msg);
		}
	}
	
	public void expandItem(TextMatchingCriterion criterion, boolean expandChildren, boolean verify) throws SeleniumPlusException {
		TreeNode node = getMatchedElement(criterion);

		checkIfDisabled(node, "This element is disabled, it cannot be selected.");
		expandItem(node, expandChildren);

		if(verify) verifyItemExpension(node, true);
	}
	
	public void collapseItem(TextMatchingCriterion criterion, boolean collpaseChildren, boolean verify) throws SeleniumPlusException {
		TreeNode node = getMatchedElement(criterion);
		
		checkIfDisabled(node, "This element is disabled, it cannot be selected.");
		collapseItem(node, collpaseChildren);

		if(verify) verifyItemExpension(node, false);
	}
	
	/**
	 * @param node TreeNode
	 * @param expectExpended boolean, true then the node is expected as expended; false then the node is expected as collapsed.
	 * @throws SeleniumPlusException
	 */
	protected void verifyItemExpension(TreeNode node, boolean expectExpended) throws SeleniumPlusException{
		
		checkIfDisabled(node, "This element is disabled, it cannot be expended or collapsed.");
		boolean verificationOK = false;
		
		try{
			verificationOK = (verifyItemExpanded(node)==expectExpended);
		}catch(SeleniumPlusException spe){
			if(spe.getCode().equals(SeleniumPlusException.CODE_VERIFICATION_FAIL)){
				verificationOK = !expectExpended;
			}else{
				throw spe;
			}
		}
		
		if(!verificationOK){
			String msg = "verification error: node='"+node+"' is "+(expectExpended?"collapsed":"expended");
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
	}

	/**
	 * Check if the node is disabled. Normally it could not be operated if the node is disabled, so
	 * we should check this before doing any actions on it.
	 * 
	 * @param node	TreeNode, the node to check.
	 * @param msg	String, the error message to log or to 
	 * @throws SeleniumPlusException
	 */
	private void checkIfDisabled(TreeNode node, String msg) throws SeleniumPlusException{
		if(node.isDisabled()){
			//Log a warning message instead of throwing Exception
			//sometimes the API sap.ui.commons.TreeNode.getSelectable() will return false, but the node still could be selected.
			IndependantLog.warn(msg);
			//throw new SeleniumPlusException(msg);
		}
	}
	
	
	/**
	 * @param node TreeNode, the node to expand.
	 * @param expandChildren boolean, if true then expand all the children of this node.
	 * @throws SeleniumPlusException
	 */
	protected abstract void expandItem(TreeNode node, boolean expandChildren) throws SeleniumPlusException;

	/**
	 * @param node TreeNode, the node to collapse.
	 * @param collpaseChildren boolean, if true then collapse all the children of this node.
	 * @throws SeleniumPlusException
	 */
	protected abstract void collapseItem(TreeNode node, boolean collpaseChildren) throws SeleniumPlusException;

	/**
	 * <b>Note:</b>In subclass, we need to update the 'expanded' property of TreeNode.<br>
	 * Verify if a tree node is expanded or collapsed.<br>
	 * @param node TreeNode
	 * @return	boolean, true if the node is expended; false if it is collapsed.
	 * @throws SeleniumPlusException
	 */
	protected boolean verifyItemExpanded(TreeNode node) throws SeleniumPlusException {
		return node.isExpanded();
	}
	
	/**
	 * According to the "hierarchical path" to get a TreeNode object.
	 */
	public TreeNode getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(getClass(), "getMatchedElement");
		try{
			return (TreeNode) super.getMatchedElement(criterion);
		}catch(SeleniumPlusException spe){
			throw spe;
		}catch(Exception e){
			IndependantLog.error(debugmsg+"Fail to get matched element.",e);
			throw new SeleniumPlusException("Fail to get matched element."+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Cast the Element to TreeNode.
	 */
	protected TreeNode convertTo(Element element) throws SeleniumPlusException{
		if(!(element instanceof TreeNode)) throw new SeleniumPlusException("element is not a TreeNode object.");
		return (TreeNode) element;
	}
}
