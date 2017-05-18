/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.Tree;
import org.safs.tools.stringutils.StringUtilities;

/**
 *
 * History:<br>
 *
 *  <br>   May 30, 2014    (sbjlwa) Initial release.
 */
public class TreeNode extends HierarchicalElement {

	protected boolean expanded = false;
	protected boolean selectable = false;

	protected TreeNode(){}
	public TreeNode(Object object){ initialize(object);}

	public void setExpanded(boolean expanded){
		this.expanded = expanded;
	}
	public boolean isExpanded() {
		return expanded;
	}

	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	public void updateFields(){
		super.updateFields();

		if(map!=null){
			expanded = StringUtilities.convertBool(getAttribute(PROPERTY_EXPANDED));
			selectable = StringUtilities.convertBool(getAttribute(PROPERTY_DISABLED));

		}else if(webelement!=null){
			expanded = Tree.isTreeNodeExpanded(webelement);
			selected = Tree.isTreeNodeSelected(webelement);

		}
	}

	/**
	 * If the label contains multiple lines, the first line will be consider as the label.<br>
	 * As for TreeView, the WebElement node's innerText may contain the children-node's label.<br>
	 */
	protected String _getLabel(){
		String text = super._getLabel();
		return StringUtils.getFirstLine(text);
	}

	protected TreeNode newInstance(Object object){
		return new TreeNode(object);
	}
	protected TreeNode[] newArray(int length){
		return new TreeNode[length];
	}

	public TreeNode getParent(){
		String debugmsg = StringUtils.debugmsg(getClass(), "getParent");
		if(parent!=null && (parent instanceof TreeNode)) return (TreeNode) parent;
		else{
			IndependantLog.error(debugmsg+"The parent should be "+getClass().getSimpleName()+".");
			return null;
		}
	}

	public TreeNode[] getChildren() {
		String debugmsg = StringUtils.debugmsg(getClass(), "getChildren");
		if(children==null) return null;
		else if(children instanceof TreeNode[]) return (TreeNode[])children;
		else{
			IndependantLog.error(debugmsg+"The children should be "+getClass().getSimpleName()+"[].");
			return null;
		}
	}

	/**
	 * @param children TreeNode[], an array of TreeNode
	 */
	public void setChildren(HierarchicalElement[] children) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "setChildren");
		if(children instanceof TreeNode[]){
			this.children = children;
		}else{
			String message = "Only "+getClass().getSimpleName()+"[] is accepted as parameter.";
			IndependantLog.error(debugmsg+message);
			throw new SeleniumPlusException(message);
		}
	}

	public boolean equals(Object node){
		if(node==null) return false;
		if(!(node instanceof TreeNode)) return false;

		return super.equals(node);
	}
}
