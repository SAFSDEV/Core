/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * 
 * History:<br>
 * 
 *  <br>   May 30, 2014    (Lei Wang) Initial release.
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
		
		if(node.isDisabled()){
			String msg = "This element is disabled, it cannot be selected.";
			throw new SeleniumPlusException(msg);
		}
		expandItem(node, expandChildren);

		if(verify) verifyItemExpension(node, true);
	}
	
	public void collapseItem(TextMatchingCriterion criterion, boolean collpaseChildren, boolean verify) throws SeleniumPlusException {
		TreeNode node = getMatchedElement(criterion);
		
		if(node.isDisabled()){
			String msg = "This element is disabled, it cannot be selected.";
			throw new SeleniumPlusException(msg);
		}
		collapseItem(node, collpaseChildren);

		if(verify) verifyItemExpension(node, false);
	}
	
	/**
	 * @param node TreeNode
	 * @param expectExpended boolean, true then the node is expected as expended; false then the node is expected as collapsed.
	 * @throws SeleniumPlusException
	 */
	protected void verifyItemExpension(TreeNode node, boolean expectExpended) throws SeleniumPlusException{
		if(node.isDisabled()){
			String msg = "This element is disabled, it cannot be expended or collapsed.";
			throw new SeleniumPlusException(msg);
		}
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
