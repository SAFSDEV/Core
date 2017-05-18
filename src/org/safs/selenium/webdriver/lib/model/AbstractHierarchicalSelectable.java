/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import java.awt.Point;
import java.util.Arrays;

import org.openqa.selenium.Keys;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * This class represents a hierarchical selectable structure like Tree or Menu etc.<br>
 * It provides a defult implementation of {@link #getMatchedElement(String, boolean, int)}<br>
 *
 * <br>
 * History:<br>
 *
 *  <br>   Jun 23, 2014    (sbjlwa) Initial release.
 */
public abstract class AbstractHierarchicalSelectable extends AbstractSelectable{

	/**
	 * @param parent
	 * @throws SeleniumPlusException
	 */
	public AbstractHierarchicalSelectable(Component parent) throws SeleniumPlusException {
		super(parent);
	}

	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	/**
	 *  According to the "hierarchical path" to get a Element object.<br>
	 *  The sub-class needs to provide an appropriate implementation of method {@link #getContent()}, on which<br>
	 *  this method depends. Subclass can override this method to cast the result HierarchicalElement to ist own type.<br>
	 *  @param path String, the hierarchical path, "root->node1" for example. The path should be separated by {@link GuiObjectRecognition#DEFAULT_PATH_SEPARATOR}<br>
	 *  @see #getContent()
	 */
	public HierarchicalElement getMatchedElement(TextMatchingCriterion criteria) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement");
		HierarchicalElement matchedElement = null;

		try{
			HierarchicalElement[] elements = (HierarchicalElement[]) getContent();
			String[] pathNodes = StringUtils.getTokenArray(criteria.getText(), GuiObjectRecognition.DEFAULT_PATH_SEPARATOR, null);

			if(criteria.matchIndexHierarchically()){
				matchedElement = this.getMatchedNode(elements, pathNodes, criteria.isPartialMatch(), criteria.getExpectedMatchedIndices());

			}else{
				MutableInteger matchedTimes = new MutableInteger(0);
				matchedElement = getMatchedNode(elements, pathNodes, criteria.isPartialMatch(), matchedTimes, criteria.getExpectedMatchedIndex());
				IndependantLog.debug(debugmsg+" found "+matchedTimes.getValue()+" matches.");
			}

		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Cannot get elements from container.", e);
		}

		if(matchedElement==null){
			IndependantLog.error(debugmsg+"Fail to find element "+criteria.toString());
			throw new SeleniumPlusException("Fail to find element '"+criteria.getText()+"'.");
		}
		return matchedElement;
	}

	/**
	 * According to a pathNodes (an array of path) and expectedMatchTimes, find the Nth-matched node in a hierarchical structure.<br>
	 * @param hierarchicalStructure HierarchicalElement[], the hierarchical structure where to find the matched node.
	 * @param pathNodes String[], an array of node path to match
	 * @param partialMatch boolean, if the pathNodes contains partial string or full string to match
	 * @param matchedTimes MutableInteger, how many times that the node-path has been found.
	 * @param expectedMatchTimes int, the expected times that the node-path should be found.
	 * @return HierarchicalElement, the matched node. Sub-class may need to convert it to its own type.
	 * @throws SeleniumPlusException
	 */
	HierarchicalElement getMatchedNode(HierarchicalElement[] hierarchicalStructure, String[] pathNodes, boolean partialMatch, MutableInteger matchedTimes, int expectedMatchTimes) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(getClass(), "getMatchedNode");
		String label = null;
		String[] subPathNodes = null;
		HierarchicalElement matchedNode = null;

		if(hierarchicalStructure==null || hierarchicalStructure.length<=0){
			IndependantLog.error(debugmsg+" hierarchical structure (tree/menubar etc.) is null or has no nodes to match");
			return null;
		}

		if(pathNodes==null || pathNodes.length<=0){
			IndependantLog.error(debugmsg+" pathNodes is null or has no nodes to match");
			return null;
		}
		label = pathNodes[0];

		for(HierarchicalElement node: hierarchicalStructure){
			if(StringUtils.matchText(node.getLabel(), label, partialMatch, false)){
				if(pathNodes.length==1){//when the last node in the path has matched
					if(matchedTimes.incrementBefore()==expectedMatchTimes){//matchedTimes increment, it will bring back its value
						matchedNode = node;
						break;
					}
				}else{
					//there are nodes of deeper level to match
					subPathNodes = Arrays.copyOfRange(pathNodes, 1, pathNodes.length);
					matchedNode = getMatchedNode(node.getChildren(), subPathNodes, partialMatch, matchedTimes, expectedMatchTimes);
					if(matchedNode!=null) break;//else contineu to search, matchedTimes will hold the latest number.
				}
			}//else{//if not matched, try next sibling
		}
		return matchedNode;
	}

	/**
	 * According to a pathNodes (an array of path) and expectedMatchTimes (an array of int), find the matched node in a hierarchical structure.<br>
	 * If at certain level, the expectedMatchedTime is not satisfied, then the search will fail and just return a null.<br>
	 * @param hierarchicalStructure HierarchicalElement[], the hierarchical structure where to find the matched node.
	 * @param pathNodes String[], an array of node path to match
	 * @param partialMatch boolean, if the pathNodes contains partial string or full string to match
	 * @param expectedMatchTimes int[], an array of int, each int value represents the expected times that the node-path should be found at its level.
	 * @return HierarchicalElement, the matched node. Sub-class may need to convert it to its own type.
	 * @throws SeleniumPlusException
	 */
	HierarchicalElement getMatchedNode(HierarchicalElement[] hierarchicalStructure, String[] pathNodes, boolean partialMatch, int[] expectedMatchTimes) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(getClass(), "getMatchedNode");
		String label = null;
		String[] subPathNodes = null;
		int[] subExpectedMatchTimes = null;
		int expectedMatchTime = -1;
		int matchedTimes=0;

		if(hierarchicalStructure==null || hierarchicalStructure.length<=0){
			IndependantLog.error(debugmsg+" hierarchical structure (tree/menubar etc.) is null or has no nodes to match");
			return null;
		}

		if(pathNodes==null || pathNodes.length<=0){
			IndependantLog.error(debugmsg+" pathNodes is null or has no nodes to match");
			return null;
		}
		label = pathNodes[0];
		if(expectedMatchTimes==null || expectedMatchTimes.length<=0 || expectedMatchTimes.length!=pathNodes.length){
			IndependantLog.error(debugmsg+" expectedMatchTimes is null or has no item or its length does not equal to pathNodes length.");
			return null;
		}
		expectedMatchTime = expectedMatchTimes[0];

		for(HierarchicalElement node: hierarchicalStructure){
			if(StringUtils.matchText(node.getLabel(), label, partialMatch, false)){
				if(matchedTimes++==expectedMatchTime){
					if(pathNodes.length==1){
						return node;
					}else{
						//there are nodes of deeper level to match
						subPathNodes = Arrays.copyOfRange(pathNodes, 1, pathNodes.length);
						subExpectedMatchTimes = Arrays.copyOfRange(expectedMatchTimes, 1, expectedMatchTimes.length);
						return getMatchedNode(node.getChildren(), subPathNodes, partialMatch, subExpectedMatchTimes);
					}
				}
				//else contienu to test the next sibling

			}//else{//if not matched, try next sibling
		}

		return null;
	}

	/**
	 * Cast the an Element object to HierarchicalElement.<br>
	 * Subclass needs to override this method to cast it to its own type.<br>
	 * @param element	Element, the Element object to cast
	 * @return HierarchicalElement, the HierarchicalElement object cast from Element object.
	 * @throws SeleniumPlusException if the Element object cannot be cast to HierarchicalElement.
	 */
	protected HierarchicalElement convertTo(Element element) throws SeleniumPlusException{
		if(!(element instanceof HierarchicalElement)) throw new SeleniumPlusException("element is not a HierarchicalElement object.");
		return (HierarchicalElement) element;
	}
}
