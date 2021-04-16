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
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 *
 * History:<br>
 *
 *  <br>   Jun 03, 2014    (Lei Wang) Initial release.
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
