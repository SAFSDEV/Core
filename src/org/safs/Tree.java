/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;


/**
 * <br><em>Purpose:</em> generic tree object
 *     
 * @author  Doug Bauman
 * @since   JUL 11, 2003
 *
 *   <br>   JUL 11, 2003    (DBauman) Original Release
 **/
public class Tree implements java.io.Serializable {
  public static final String PATH_SEPARATOR = "->";
  public static final String EQUAL_SEPARATOR = "=";

  private Object userObject;
  public Object getUserObject () {return userObject;}
  public void setUserObject (Object userObject) {this.userObject = userObject;}

  private Tree firstChild;
  public Tree getFirstChild () {return firstChild;}
  public void setFirstChild (Tree firstChild) {this.firstChild = firstChild;}

  private Tree nextSibling;
  public Tree getNextSibling () {return nextSibling;}
  public void setNextSibling (Tree nextSibling) {this.nextSibling = nextSibling;}

  private Integer level;
  public Integer getLevel () {return level;}
  public void setLevel(Integer level) {this.level = level;}
  
  private Integer siblingCount;
  public Integer getSiblingCount () {return siblingCount;}
  public void setSiblingCount(Integer siblingCount) {this.siblingCount = siblingCount;}
  
  private Integer childCount;
  public Integer getChildCount () {return childCount;}
  public void setChildCount(Integer childCount) {this.childCount = childCount;}

  public Tree() {
  }

  public String toString () {
    return StringUtils.getSpaces(level)+ userObject +
      (firstChild == null ? "" : "\n"+firstChild.toString()) +
      (nextSibling == null ? "" : "\n"+nextSibling.toString());
  }

  public String[] toStringArray () {
    String[] fca = null;
    String[] nsa = null;
    int len = 1;
    if (firstChild != null) {
      fca = firstChild.toStringArray();
      len += fca.length;
    }
    if (nextSibling != null) {
      nsa = nextSibling.toStringArray();
      len += nsa.length;
    }
    String[] arr = new String[len];
    int c=0;
    arr[c++] = ""+userObject;
    if (firstChild != null) {
      for(int j=0; j<fca.length; j++) {
        arr[c++] = fca[j];
      }
    }
    if (nextSibling != null) {
      for(int j=0; j<nsa.length; j++) {
        arr[c++] = nsa[j];
      }
    }
    return arr;
  }

  public String[] toStringArrayWOSiblings () {
    String[] fca = null;
    int len = 1;
    if (firstChild != null) {
      fca = firstChild.toStringArray();
      len += fca.length;
    }
    String[] arr = new String[len];
    int c=0;
    arr[c++] = ""+userObject;
    if (firstChild != null) {
      for(int j=0; j<fca.length; j++) {
        arr[c++] = fca[j];
      }
    }
    return arr;
  }

  /** <br><em>Purpose:</em> find a match based on the parameter by walking the tree,
   ** finding a string which contains the matching substring, and returning the full path
   ** if it matched.  
   * <br><em>Assumptions:</em>  match is not null; first match will be considered the match,
   * subsequent matches would therefore be ignored.
   * @param                     match, String
   * @return                    full path to match, null otherwise
   **/
  public String findMatchPath (String match) {
    String result = null;

    String stripArrow = match;
    int stripArrowI = match.indexOf("->"); // the first occurance of the arrow
    if (stripArrowI >= 0) stripArrow = stripArrow.substring(0, stripArrowI);
    Log.debug(stripArrow);
    try{
	    String obj = userObject.toString();
	    if (obj.toLowerCase().indexOf(stripArrow.toLowerCase()) >= 0) { // success at this level
	      Log.debug("success at this level");
	      if (stripArrowI >= 0) { // means we still have to look at our children
	        if (firstChild == null) return null;
	        Log.debug("continuing with: "+match.substring(stripArrowI+2, match.length()));
	        String chResult = firstChild.findMatchPath(match.substring(stripArrowI+2, match.length()));
	        if (chResult == null) return null;
	        return obj + "->" + chResult;
	      } else {
	        return obj;
	      }
	    } else { // otherwise try the nextSibling
	      Log.debug("otherwise try the nextSibling");
	      if (nextSibling == null) return null;
	      return nextSibling.findMatchPath(match);
	    }
    }catch(NullPointerException npe){
	    Log.debug("Item null at this level. Trying next sibling or child.");
	    if (nextSibling != null) return nextSibling.findMatchPath(match);
    	if (firstChild == null) return null;
	    return firstChild.findMatchPath(match);
    }
  }

  /** <br><em>Purpose:</em> find an exact match based on the parameter by walking the tree,
   ** finding a string which equals each matching substring, and returning the full path
   ** if it matched.  
   * <br><em>Assumptions:</em>  match is not null; first match will be considered the match,
   * subsequent matches would therefore be ignored.
   * @param                     match, String
   * @return                    full path to match, null otherwise
   **/
  public String exactMatchPath (String match) {
    String result = null;

    String stripArrow = match;
    int stripArrowI = match.indexOf("->"); // the first occurance of the arrow
    if (stripArrowI >= 0) stripArrow = stripArrow.substring(0, stripArrowI);
    Log.debug(stripArrow);
    try{
	    String obj = userObject.toString(); // NullPointerException on Menus
    	if (obj.equalsIgnoreCase(stripArrow)) { // success at this level
	      Log.debug("success at this level");
    	  if (stripArrowI >= 0) { // means we still have to look at our children
        	if (firstChild == null) return null;
	        Log.debug("continuing with: "+match.substring(stripArrowI+2, match.length()));
    	    String chResult = firstChild.exactMatchPath(match.substring(stripArrowI+2, match.length()));
        	if (chResult == null) return null;
	        return obj + "->" + chResult;
    	  } else {
        	return obj;
	      }
    	} else { // otherwise try the nextSibling
	      Log.debug("otherwise try the nextSibling");
    	  if (nextSibling == null) return null;
	      return nextSibling.exactMatchPath(match);
    	}
    }catch(NullPointerException npe){
	    Log.debug("Item null at this level. Trying next sibling or child...");
	    if (nextSibling != null) return nextSibling.exactMatchPath(match);
    	if (firstChild == null) return null;
	    return firstChild.exactMatchPath(match);
    }
  }


}
