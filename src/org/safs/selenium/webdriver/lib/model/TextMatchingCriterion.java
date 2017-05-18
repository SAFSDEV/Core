/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * This class contains text informations for searching an item in a list, tree, menu etc.<br>
 * <ol>
 * <li> The text/path to match
 * <li> If the match is partial of full, which means if the text is substring of the item's text in the list, tree etc.
 * <li> The expected matched index or expected matched indices.<br>
 * "expected matched index" is a single int value and it means the Nth matched node/path is wanted<br>
 * "expected matched indices" is an array of int value and it means the Nth matched node of each level in the path<br>
 * If {@link #matchIndexHierarchically()} return true, then it contains an array; otherwise it just contains a single int value.<br>
 * </ol>
 *
 * <br>
 * History:<br>
 *
 *  <br>   Jun 26, 2014    (Lei Wang) Initial release.
 *  <br>   Oct 29, 2014    (Lei Wang) Add constructor with only one int value, to match only the index.
 *                                  Overload method matchText(): to match text at certain level for hierarchical structure.
 */
public class TextMatchingCriterion {
	/**'-1' an invalid index value.*/
	public static final int INVALID_INDEX = -1;
	/**
	 * '-1000', normally a matchIndex should be given to verify if a text is selected.<br>
	 * matchIndex allows to match item N in a list containing duplicate entries.<br>
	 * when this value is provided as matchIndex, then verification will be ok if one<br>
	 * of matched items is verified.<br>
	 *
	 * @see #verifyItemSelection(String, boolean, int, boolean)
	 */
	public static final int INDEX_TRY_ALL_MATCHED_ITEMS = -1000;

	/**The text/path string to match, it can also be a regex expression*/
	private String text = null;
	/**path array, parsed from field text*/
	private String[] pathArray = null;
	/**If the match of text is partial or full*/
	private boolean partialMatch = false;

	private String separator = GuiObjectRecognition.DEFAULT_PATH_SEPARATOR;

	/**
	 * <pre>
	 * it is 0-based index.
	 * if the field text is null, then this field means:
	 *   to get Nth item in a list/tab/tree
	 * if the field text is NOT null, then this field means:
	 *   to match item N in a list/tab/tree containing duplicate entries
	 *   <font color='red'>if it is provided as {@link #INDEX_TRY_ALL_MATCHED_ITEMS}, then get all possible matched items to verify.</font>
	 * </pre>
	 */
	private int expectedMatchedIndex = INVALID_INDEX;
	/**it is 0-based indicies array, to match item N in a tree/menu containing duplicate entries for each level*/
	private int[] expectedMatchedIndices = null;

	public TextMatchingCriterion(int expectedMatchedIndex){
		this.expectedMatchedIndex = expectedMatchedIndex;
	}

	/**
	 * constructor of TextMatchingCriterion.
	 * @param text String, the text to match
	 * @param partialMatch boolean, if the text is substring of fullstring
	 * @param expectedMatchedIndex int, the times to match, 0-based
	 *                                  that is to match item N in a list/tab/tree containing duplicate entries
	 */
	public TextMatchingCriterion(String text, boolean partialMatch, int expectedMatchedIndex){
		setText(text);
		this.partialMatch = partialMatch;
		this.expectedMatchedIndex = expectedMatchedIndex;
	}

	/**
	 * constructor of TextMatchingCriterion.
	 * @param path String, the path to match
	 * @param partialMatch boolean, if the text is substring of fullstring
	 * @param expectedMatchedIndex int[], the times to match of each level, 0-based
	 */
	public TextMatchingCriterion(String path, boolean partialMatch, int[] expectedMatchedIndices){
		setText(path);
		this.partialMatch = partialMatch;
		this.expectedMatchedIndices = expectedMatchedIndices;
	}

	/**
	 * constructor of TextMatchingCriterion.
	 * @param path String, the path to match
	 * @param partialMatch boolean, if the text is substring of fullstring
	 * @param indexPathStr String, a separated string containing indices, 1-based, for example 3->2->4
	 */
	public TextMatchingCriterion(String path, boolean partialMatch, String indexPathStr){
		this(path, partialMatch, indexPathStr, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
	}

	/**
	 * constructor of TextMatchingCriterion.
	 * @param path String, the path to match
	 * @param partialMatch boolean, if the text is substring of fullstring
	 * @param indexPathStr String, a separated string containing indices, 1-based, for example 3->2->4
	 * @param separator String, the separator to separate index in indexPathStr
	 */
	public TextMatchingCriterion(String path, boolean partialMatch, String indexPathStr, String separator){
		String debugmsg = StringUtils.debugmsg(false);

		setText(path);
		this.separator = separator;
		this.partialMatch = partialMatch;

		String[] nodes = StringUtils.getTokenArray(path, separator);

		if(indexPathStr==null || indexPathStr.isEmpty()){
			expectedMatchedIndices = new int[nodes.length];
			for(int i=0;i<nodes.length;i++) expectedMatchedIndices[i]=0;
		}else{
			String[] pathIndexArray = StringUtils.getTokenArray(indexPathStr, separator);
			if(nodes.length!=pathIndexArray.length){
				IndependantLog.warn("path length '"+nodes.length+"' does NOT equal to indexPath length '"+pathIndexArray.length+"'");
			}
			expectedMatchedIndices = new int[pathIndexArray.length];
			for(int i=0;i<pathIndexArray.length;i++){
				try{
					expectedMatchedIndices[i] = Integer.parseInt(pathIndexArray[i])-1;//convert 1-based index to 0-based.
				}catch(NumberFormatException e){
					IndependantLog.warn(debugmsg+"'"+pathIndexArray[i]+"' cannot be converted to int.");
				}
			}
		}

		if(!isHierarchical()) expectedMatchedIndex = expectedMatchedIndices[0];
	}

	public String getSeparator(){
		return separator;
	}

	protected void setText(String text){
		this.text = text;
		if(text!=null && text.contains(separator))
			pathArray = StringUtils.getTokenArray(text, separator, StringUtils.REGEX_ESCAPE_CHARACTER);
	}

	/**
	 * Test if the expected-index should be matched level by level.<br>
	 * Be careful, even if {@link #isHierarchical()} is true, but this method may return false. In that situation,<br>
	 * which means that we want to match the path as a whole part.<br>
	 * @return boolean if the item should be matched level by level.
	 * @see #isHierarchical()
	 */
	public boolean matchIndexHierarchically(){
		return expectedMatchedIndices!=null;
	}

	/**
	 * Test if the criterion is hierarchical or not.<br>
	 * @return boolean, true if the criterion is hierarchical
	 */
	public boolean isHierarchical(){
		boolean isHierarchical = false;
		try{
			isHierarchical = (expectedMatchedIndices!=null && expectedMatchedIndices.length>1);
			if(!isHierarchical) isHierarchical = text.indexOf(separator)>-1;//text contain ->
		}catch(Exception e){}

		return isHierarchical;
	}

	/**return 0-based indices array*/
	public int[] getExpectedMatchedIndices(){
		return expectedMatchedIndices;
	}
	/**return 0-based index*/
	public int getExpectedMatchedIndex(){
		return expectedMatchedIndex;
	}
	public void setExpectedMatchedIndex(int expectedMatchedIndex){
		this.expectedMatchedIndex = expectedMatchedIndex;
	}
	/**The text/path string to match*/
	public String getText(){
		return text;
	}
	/**If the match of text is partial or full*/
	public boolean isPartialMatch(){
		return partialMatch;
	}

	/**
	 * Test if the actualText matches with the {@link #getText()}.<br>
	 * Used for non-hierarchical structure, such as List, ComboBox etc.
	 * @param actualText String, the actual text of the item
	 */
	public boolean matchText(String actualText){
		return StringUtils.matchText(actualText, text, partialMatch, false);
	}

	/**
	 * Test if the actualText matches with the value of array {@link #pathArray} at index.<br>
	 * Used for hierarchical structure, such as Tree, Menu etc.
	 * @param actualText String, the actual text of the item
	 * @param level int, the level in the path, 0-based
	 */
	public boolean matchText(String actualText, int level){
		try{
			return StringUtils.matchText(actualText, pathArray[level], partialMatch, false);
		}catch(Exception e){
			return false;
		}
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();

		sb.append("text/path="+text+" ");
		sb.append("partialMatch="+partialMatch+" ");
		if(matchIndexHierarchically()){
			StringBuffer expectedMatchedIndicesPath = new StringBuffer();
			for(int index: expectedMatchedIndices){
				expectedMatchedIndicesPath.append(index+separator);
			}
			int end = expectedMatchedIndicesPath.length();
			int start = expectedMatchedIndicesPath.length()-separator.length();
			expectedMatchedIndicesPath.replace(start, end, "");//remove the last separator "->"
			sb.append("expectedMatchedIndices="+expectedMatchedIndicesPath.toString()+" ");
		}else{
			sb.append("expectedMatchedIndex="+expectedMatchedIndex+" ");
		}

		return sb.toString();
	}
}
