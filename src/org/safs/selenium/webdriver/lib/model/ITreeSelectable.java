/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * 
 * History:<br>
 * 
 *  <br>   Jun 03, 2014    (sbjlwa) Initial release.
 */
public interface ITreeSelectable extends IHierarchicalSelectable{

	/**
	 * Expand the item according to the path.
	 * <br>
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @param expandChildren boolean, if true then expand all the children of this node.
	 * @param verify boolean, if true then verify the treepath has been expended.
	 * @throws SeleniumPlusException
	 */	
	public void expandItem(TextMatchingCriterion criterion, boolean expandChildren, boolean verify) throws SeleniumPlusException;

	/**
	 * Collapse the item according to the path.
	 * <br>
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @param collpaseChildren boolean, if true then collapse all the children of this node.
	 * @param verify boolean, if true then verify the treepath has been collapse.
	 * @throws SeleniumPlusException
	 */
	public void collapseItem(TextMatchingCriterion criterion, boolean collpaseChildren, boolean verify) throws SeleniumPlusException;
	
	
	/**
	 * Get all tree nodes.
	 * @return TreeNode[] an array of the TreeNode
	 * @throws SeleniumPlusException
	 */
	public TreeNode[] getContent() throws SeleniumPlusException;
	
	public TreeNode getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException;
}
