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
package org.safs.rational;

import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.StringUtils;
import org.safs.Tree;


/**
 * <br><em>Purpose:</em> To handle the manipulation of JMenuBar and JPopupMenu
 *     
 * @author  Lei Wang
 * @since   APR 25, 2008
 * 
 * <br>		JUN	12, 2009	(Lei Wang)	Add method toString() to print the tree.
 * <br>		NOV 02, 2009	(Lei Wang)	Modify method getTreePathAndStatus(String) to getTreePaths(String, boolean)
 * 										This method will return a list of menu tree path with or without menuitem's status.
 **/
public class MenuTree extends Tree{
  public MenuTree() {
  	super();
  }
	
  public MenuTreeNode getMenuTreeNode (){
  	return (MenuTreeNode) super.getUserObject();
  }

  public void setUserObject (Object userObject) {
  	if(userObject instanceof MenuTreeNode){
  		super.setUserObject(userObject);
  	}else{
  		Log.debug(getClass().getName()+": userObject must be type of "+MenuTreeNode.class);
  	}
  }

  public void setFirstChild (Tree firstChild) {
  	if(firstChild instanceof MenuTree){
  		super.setFirstChild(firstChild);
  	}else{
  		Log.debug(getClass().getName()+": child must be type of "+MenuTree.class);
  	}
  }

  public void setNextSibling (Tree nextSibling) {
  	if(nextSibling instanceof MenuTree){
  		super.setNextSibling(nextSibling);
  	}else{
  		Log.debug(getClass().getName()+": nextSibling must be type of "+MenuTree.class);
  	}
  }
  
  /**
   * @return	List, a list of the path and status of each node of this tree
   * <br>Example: {"File=Enabled","File->Open=Enabled","File-Open=Enabled","Edit=Enabled","Edit->Cut=Disabled","Edit->Copy=Enabled"}
   */
  public List getTreePaths(String ancestorPath, boolean getPahtwithSatus){
  	List treePaths = new ArrayList();
  	
  	MenuTreeNode node = getMenuTreeNode();
  	MenuTree nextSibling = (MenuTree) getNextSibling();
  	MenuTree firstChild = (MenuTree) getFirstChild();
  	
  	String fullPath = "".equals(ancestorPath.trim())? node.getNodeLabel():ancestorPath+PATH_SEPARATOR+node.getNodeLabel();
  	if(fullPath!=null && !fullPath.equals("")){
  	  	StringBuffer paths = new StringBuffer();
  	  	paths.append(fullPath);
  	  	if(getPahtwithSatus){
  	  		paths.append(EQUAL_SEPARATOR+node.getStatusString());
  	  	}
  	  treePaths.add(paths.toString());
  	}

  	if(firstChild!=null)
  		treePaths.addAll(firstChild.getTreePaths(fullPath,getPahtwithSatus));
  	if(nextSibling!=null)
  		treePaths.addAll(nextSibling.getTreePaths(ancestorPath,getPahtwithSatus));
  		
  	return treePaths;
  }
  
  /** <br><em>Purpose:</em> find an matched path based on the parameter by walking the tree.
   * 	finding a string which equals each matching substring if fuzzy is false;
   * 	finding a string which contains the matching substring if fuzzy is true.
   * 	finally returning the full path if it matched.
   * 
   * <br><em>Assumptions:</em>  pathTobeMatched is not null; 
   * 	first part of pathTobeMatched will be considered the match, subsequent matches would therefore be ignored.
   * 	for example, if pathTobeMatched is "firstlevel->secondlevel", only "firstlevel" will be considered at this level,
   * 	"secondlevel" will be considered at it's child level.
   * 
   * @param                     pathTobeMatched, String, the path to be matched
   * @param						fuzzy, boolean, Whether the match is exact
   * @param					  	status, String, the status of the node, example "Enabled Checked"
   * @return                    full path to match, null otherwise
   **/
  public String matchPath (String pathTobeMatched,boolean fuzzy,String status) {
    String result = null;

  	MenuTreeNode node = getMenuTreeNode();
  	MenuTree nextSibling = (MenuTree) getNextSibling();
  	MenuTree firstChild = (MenuTree) getFirstChild();
    
    //Try to get the "first level string" of pathTobeMatched
    String stripArrow = pathTobeMatched;
    int stripArrowI = pathTobeMatched.indexOf(PATH_SEPARATOR); // the first occurance of the arrow
    if (stripArrowI >= 0) stripArrow = stripArrow.substring(0, stripArrowI);
    Log.debug(stripArrow);
    
    //Compare the "first level string" with this node label
	boolean matched = false;
	String nodeLabel = node.getNodeLabel();
	if (fuzzy) {
		matched = (nodeLabel.toLowerCase().indexOf(stripArrow.toLowerCase()) >= 0);
	} else {
		matched = nodeLabel.equalsIgnoreCase(stripArrow);
	}
  	
	//If match result at this level is ok. 
	//		If the pathTobeMatched has next level to match, try to compare with the child of this node.
	//		Else try to check the status or return the nodelabel directly according to status
	//Else try next node at this level
	if (matched && !node.isSeparator()) {  // ignore reserved items like Separator, continues its nextSibling in ELSE
		Log.debug("success at this level");
		if (stripArrowI >= 0) { //we still have children to match
			if (firstChild == null) return null;
			Log.debug("continuing with: " + pathTobeMatched.substring(stripArrowI + 2, pathTobeMatched.length()));
			String chResult = firstChild.matchPath(pathTobeMatched.substring(stripArrowI + 2, pathTobeMatched.length()),fuzzy,status);
			if (chResult == null) return null;
			return nodeLabel + PATH_SEPARATOR + chResult;
		} else {
			if(status!=null){
				if(node.matchStatus(status)) return nodeLabel;
				else return null;
			}else{
				return nodeLabel;	
			}				
		}
	} else { // otherwise try the nextSibling
		Log.debug("otherwise try the nextSibling");
		if (nextSibling == null) return null;
		return nextSibling.matchPath(pathTobeMatched,fuzzy,status);
	}
  }
  
  public String toString() {
	return StringUtils.getSpaces(getLevel()*2)
				+ getMenuTreeNode().getNodeLabel()
				+ (getFirstChild() == null ? "" : "\n" + getFirstChild().toString())
				+ (getNextSibling() == null ? "" : "\n" + getNextSibling().toString());
  }
}
