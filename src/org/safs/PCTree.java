/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs;

import java.util.*;
import java.io.*;
import org.safs.tools.CaseInsensitiveFile;

/**
 * PCTree, used by various testing tool's ProcessContainer, is used to produce consistent recognition 
 * strings across all tools.
 * <p>
 * Historically, the algorithm used here and in the ProcessContainer classes was intended to mirror 
 * the algorithm used by earlier versions of SQA Robot a.k.a Rational Robot.  In this way, recognition 
 * strings that would work for Robot would also work in other testing tools that might be used 
 * simultaneously.  This allowed one App Map to work for all testing tools involved.
 * <p>
 * However, more recent versions of Robot have introduced large and critical departures from the 
 * traditional algorithm in different environments.  For example, the .Net support in Robot has 
 * introduced a completely different algorithm for object recognition that is inconsistent with 
 * previous versions of Robot and other environments supported by Robot.
 * <p>
 * Consequently, the algorithm here is no longer guaranteed to mirror these later versions of Robot 
 * recognition algorithms.  Instead, we will stick with this single, fairly well-defined algorithm 
 * that should provide consistency and interoperability for all current and future SAFS engines EXCEPT 
 * for later versions of IBM's Rational Robot in certain environments (like later versions of .NET).
 * <p>
 * The tradional algorithm:
 * <p>
 * For a type, a child will increment the index, depth first,
 * a sibling will also increment the index, breadth second.
 * <p>example:</p><pre>
 * 
 *                  1
 *                  |
 *         _________2___________
 *         \                    \     
 *      ___3________         ___4_________
 *     /   /\    \  \       /   /\    \   \  
 *    4   5  7    9  10    5   6  8    10  11
 *        /   \                /   \        
 *       6     8              7     9       
 *       /      \             /      \
 *   text1,2   text3,4    text5,6   text7,8 
 *   
 *</pre><p>
 * Items with onlyOneChildVisible like TabControls are an exception, 
 * because their immediate child containers share the same space and are not all visible</p>
 * <p>example:</p><pre>
 *
 *                 1
 *                 |
 *        _________2____________________________________
 *        \                                             \
 *      TabControl                                   ___4________
 *         \     \____tabN______                    /   /\   \   \
 *          \                   \                  /   /  \   \   \
 *       visible            not-visible           5   6    8  10  11
 *       ___3________         ___3________           /      \
 *      /   /\    \  \       /   /\    \  \         7        9
 *     4   5  7    9  10    4   5  7    9  10      /          \
 *        /    \               /    \           text5,6     text7,8
 *       6      8             6      8       
 *      /        \           /        \
 *   text1,2   text3,4    text1     text2 
 *
 * <p>Same app with TabControl with different Tab visible:</p><pre>
 * 
 *                 1
 *                 |
 *        _________2____________________________________
 *        \                                             \
 *      TabControl                                   ___4________
 *         \     \____tabN______                    /   /\   \   \
 *          \                   \                  /   /  \   \   \
 *       not-visible          visible             5   6    8  10  11
 *       ___3________         ___3________           /      \
 *      /   /\    \  \       /   /\    \  \         7        9
 *     4   5  7    9  10    4   5  7    9  10      /          \
 *        /    \               /    \           text3,4     text5,6
 *       6      8             6      8       
 *      /        \           /        \
 *   text1,2   text3,4    text1     text2 
 *   
 * clicking on yet a another tab may find:
 *
 * 1-2--TabControl
 *         \
 *         3
 *         /
 *       table1
 * </pre>
 * Note how all containers immediately under each tab start with the same index.
 * In other words, the sibling breadth layer does not increment the index.  This is because 
 * at any given time only one pane will be visible and it will always be found at Index=3 
 * (given the example above).
 * <p>
 * There are known shortcomings to this algorithm.  Most notable is the changing indices of 
 * components shown in the rightmost Panel #4 when different Tabs are selected in the 
 * TabControl.  Alternative algorithms may be used in later releases allowing the user to choose 
 * between using the traditional algorithm, and any new one.
 * 
 * @author  DBauman
 * @since   OCT 13, 2003
 *
 * <br>OCT 13, 2003 (DBauman) Original Release
 * <br>OCT 14, 2003 (DBauman) adding all of the 'part' info, WE does the generation
 * of the recognition string, so now we have lots of properties like 'name',
 * 'type', 'myclass', 'nameValue', 'caption', 'classIndex', 'path', and 'parent'
 * <br>OCT 31, 2003 (DBauman) adding comments
 * <br>SEP 16, 2005 (Carl Nagle) Major overall to make recognition strings more consistent across 
 * technologies like Java, .NET, and WIN32.
 * <br>JUL 08, 2008 (JunwuMa) added method getComponentRecogString(boolean), which returns 
 *                  the recognition string of the PCTree on this level. 
 *                  Called by STAFProcessContainer.RsPCTreeMap.CreateJTreeNodes()
 * <br>NOV 06, 2008 (JunwuMa) Added methods isIgnoredNode() and toIniStringWithoutIgnoredNodes() supporting ignoring some 
 *                  containers both in mapping file and in component hierarchy. Called by STAFProcessContainer.  
 * <br>NOV 11, 2008 (JunwuMa) Introduced a new member, objectclass, for holding the real class name of the represented component. 
 *                   Modified ignoreParentRecognitionString(PCTree), added code ignoring the container that is one of the #containerClassesIgnoredForRecognition.
 * <br>NOV 27, 2008 (JunwuMa) Add member ID for holding the ID of represented component. 
 *                   Modify getRecogString(boolean) adding ID qualifier for generating R-Strings.
 * <br>JAN 16, 2009 (JunwuMa) Add member indexOnly to decide whether using 'Index/ClassIndex'  or not (Auto Qualifier) when
 *                   generating R-Strings by calling getRecogString(bool). See org.safs.tools.drivers.RStringStrategy.                 
 * <br>MAY 01, 2009 (Carl Nagle) Changes to complete FULLPATH_SEARCH_MODE implementation. 
 * <br>MAY 25, 2011 (Dharmesh4) Updated RFSM Search Flex recognition string support. 
 * <br>APR 15, 2013 (Lei Wang) Update to permit user to update the tree node's name. 
 * <br>JUN 14, 2013 (Lei Wang) Update to add extra qualifier "index=" for objects having the same ID/Name.  
 **/
public class PCTree extends Tree {

  
  public PCTree() {
    super();
  }

    private boolean fullPathSearchMode = false;
    private boolean mappedClassSearchMode = false;
    private boolean rfsmSearchMode = false;
  
  
    /**
     * @return true if rfsmSearchMode is true
     */
	public boolean isRfsmSearchMode() {
		return rfsmSearchMode;
	}
	
	/**
	 * @param set true if rfsmSearchMode is to be used.
	 */
	public void setRfsmSearchMode(boolean rfsmSearchMode) {
		this.rfsmSearchMode = rfsmSearchMode;
	}

	/**
	 * @return true if fullPathSearchMode is true
	 */
	public boolean isFullPathSearchMode() {
		return fullPathSearchMode;
	}
	
	/**
	 * @param set true if fullPathSearchMode is to be used.
	 */
	public void setFullPathSearchMode(boolean fullPathSearchMode) {
		this.fullPathSearchMode = fullPathSearchMode;
	}
	
	/**
	 * 
	 * @return true if mappedClassSearchMode is true
	 */
	public boolean isMappedClassSearchMode(){
		return mappedClassSearchMode;
	}
	
	/**
	 * set if MappedClassSearchMode is to be used.
	 * @param mappedClassSearchMode
	 */
	public void setMappedClassSearchMode(boolean mappedClassSearchMode){
		this.mappedClassSearchMode = mappedClassSearchMode;
	}
	
	
	
	private String defaultRecognition = null;
	
  /**
   * Usually only for the topmost parent, the recognition string provided by the user (or Process Container)
   * Can be null;
   */
  public void setDefaultRecognition(String recog){ 
  	  try{
  	  	defaultRecognition = recog.trim();
  	  }catch(Exception x){
  	  	defaultRecognition = null;
  	  }
  }
  /**
   * Return any defaultRecognition set.  May be null.
   * @see #setDefaultRecognition(String)
   */
  public String getDefaultRecognition(){ return defaultRecognition;}
  
  private boolean appendCompInfo = false;
  /**
   * Set TRUE to append CompType info at the end of output recognition strings.
   * This is useful when using the App Map output to import component 
   * information into other tools.  However, this information is not compatible with normal 
   * App Map usage (during testing) because the recognition string is no longer properly 
   * formed.
   */
  public void setAppendCompInfo(boolean appendCompInfo){
  	  this.appendCompInfo = appendCompInfo;
  }
  /**
   * Return TRUE if we are to append CompType info to output recognition strings.
   * @see #setAppendCompInfo(boolean)
   */
  public boolean doAppendCompInfo(){ return appendCompInfo;}
  
  // Holding the ID for represented component.
  // Can be null if no ID for the component or users don't want to use ID to generate R-Strings. 
  private String id;
  /**
   * @return the ID for represented component. 
   */
  public String getId () {return id;}
  
  public void setId (String id) { this.id = id; }
  
  // true: use 'Index='/'ClassIndex=' to generate R-Strings (NLS friendly) false: getRecogString(bool) decides to use 'ID=', 'Name=' or others (Auto Qualifier).
  private boolean indexOnly;
  public boolean getIndexOnly() { return indexOnly; }
  public void setIndexOnly(boolean indexOnly) { this.indexOnly = indexOnly;}
  
  private String name;
  /**
   * @return the user-friendly name deduced for the represented component by something like ProcessContainer.
   * This may be null if no name has ever been set.
   */
  public String getName () {return name;}
  
  /**
   * Update the tree node's name.
   * @param oldName
   * @param newName
   * @return
   */
  public boolean updateName(String oldName, String newName){
	  try{
		  if(oldName.equals(getName())){
//			  Log.debug(oldName+"="+getName()+ "; set to "+newName);
			  setName(newName);
			  return true;
		  }else{
			  if(getFirstChild()!=null && ((PCTree)getFirstChild()).updateName(oldName, newName)){
//				  Log.debug("Matched First child.");
				  return true;
			  }else if(getNextSibling()!=null && ((PCTree)getNextSibling()).updateName(oldName, newName)){
//				  Log.debug("Matched Next sibling");
				  return true;
			  }else{
				  return false;
			  }
		  }	  
	  }catch(Exception e){
		  Log.debug("Met Exception"+e.getMessage(), e);
		  return false;
	  }
  }
  
  /**
   * Sets the user-friendly name deduced for the represented component by something like ProcessContainer.
   */
  public void setName (String name) {this.name = name;}

  private String[] index_types = new String[0];
  /**
   * @return the index_types
   */
  public String[] getIndex_types() {
		return index_types;
  }
  /**
   * @return the nth index_types item or null if invalid index
   */
  public String getIndex_typesIndex(int index) {
		try{ return index_types[index];}
		catch(Exception x){}
		return null;
  }
	
  /**
   * @param index_types the index_types to set
   */
  public void setIndex_types(String[] index_types) {
		this.index_types = index_types;
  }
	
  private String type;
  /**
   * @return the deduced component type for the represented component.
   * If set, this generally comes from info derived from subclasses of GuiObjectData.
   * @see org.safs.GuiClassData
   */
  public String getType () {return type;}
  /**
   * Set the deduced component type for the represented component.
   * If set, this generally comes from info derived from subclasses of GuiClassData.
   * @see org.safs.GuiClassData
   */
  public void setType (String type) {this.type = type;}

  /* Introduced for holding the real class name of the represented component. It is different from this.myclass, 
   * which can be 'null' if the component Type is not 'Generic'. objectclass won't be changed. 
   * For example, javax.swing.JFrame, Html.BODY
   */ 
  private String objectclass; 
  /**
   * @return the class for the represented component.
   */
  public String getObjectClass () {return objectclass;}
  /**
   * Sets the class for the represented component.
   */
  public void setObjectClass (String objectclass) {this.objectclass = objectclass;}

  private String myclass;
  /**
   * @return the class for the represented component.
   */
  public String getMyclass () {return myclass;}
  /**
   * Sets the class for the represented component.
   */
  public void setMyclass (String myclass) {this.myclass = myclass;}

  private String nameValue;
  /**
   * @return the text associated with any Name=, Caption=, or Text= recognition string.
   */
  public String getNameValue () {return nameValue;}
  /**
   * Sets the text associated with any Name=, Caption=, or Text= recognition string.
   */
  public void setNameValue (String nameValue) {this.nameValue = nameValue;}

  private boolean caption;
  /**
   * @return true if the NameValue stored represents a components Caption= value.
   */
  public boolean isCaption () {return caption;}
  /**
   * Set true if the NameValue stored represents a components Caption= value.
   */
  public void setCaption (boolean caption) {this.caption = caption;}

  private boolean textvalue;
  /**
   * @return true if the NameValue stored represents a components Text= value.
   */
  public boolean isTextValue () {return textvalue;}
  /**
   * Set true if the NameValue stored represents a components Text= value.
   */
  public void setTextValue (boolean textval) {textvalue = textval;}
  
  private boolean withNameIncludeOnlyCaption;
  /**
   * @return true if recognition strings should be stripped of intermediate parent recognition 
   * info that may be deemed unnecessary when we have a (uniquely) named target component.
   */
  public boolean isWithNameIncludeOnlyCaption () {return withNameIncludeOnlyCaption;}
  /**
   * Set true if recognition strings should be stripped of intermediate parent recognition 
   * info that may be deemed unnecessary when we have a (uniquely) named target component.
   */
  public void setWithNameIncludeOnlyCaption (boolean withNameIncludeOnlyCaption) {this.withNameIncludeOnlyCaption = withNameIncludeOnlyCaption;}

  private boolean withCommentsAndBlankLines;
  /**
   * @return true if output is to include comments and\or blank lines?
   */
  public boolean isWithCommentsAndBlankLines () {return withCommentsAndBlankLines;}
  /**
   * Set true if output is to include comments and\or blank lines.
   */
  public void setWithCommentsAndBlankLines (boolean withCommentsAndBlankLines) {this.withCommentsAndBlankLines = withCommentsAndBlankLines;}

  private boolean shortenGeneralRecognition;
  /**
   * @return true if recognition strings should be stripped of intermediate parent recognition 
   * info that may be deemed unnecessary.  For example, we know that the contentPane and 
   * layoutPane exist in Java JFrames so including them in the recognition string is usually unnecessary.
   */
  public boolean isShortenGeneralRecognition () {return shortenGeneralRecognition;}
  /**
   * Set true if recognition strings should be stripped of intermediate parent recognition 
   * info that may be deemed unnecessary.
   */
  public void setShortenGeneralRecognition (boolean shorten) {shortenGeneralRecognition = shorten;}

  private boolean onlyOneChildVisible;
  /**
   * @return true if this component, like TabControls, has multiple children, but only one is visible 
   * at any given time.
   */
  public boolean isOnlyOneChildVisible () {return onlyOneChildVisible;}
  /**
   * Set true if this component, like TabControls, has multiple children, but only one is visible 
   * at any given time.
   */
  public void setOnlyOneChildVisible (boolean onlyOneChildVisible) {this.onlyOneChildVisible = onlyOneChildVisible;}

  private boolean compVisible;
  /**
   * @return true if this component was visible at the time of processing.
   */
  public boolean isComponentVisible () {return compVisible;}
  /**
   * Set true if this component was visible at the time of processing.
   */
  public void setComponentVisible (boolean setVisible) {this.compVisible = setVisible;}

  private boolean classIndex;
  /**
   * @return true if the index stored for this component is considered to be a ClassIndex 
   * index instead of the normal object type index.
   */
  public boolean isClassIndex () {return classIndex;}
  /**
   * Set true if the index stored for this component is considered to be a ClassIndex 
   * index instead of the normal object type index.
   */
  public void setClassIndex (boolean classIndex) {this.classIndex = classIndex;}

  /** Absolute class index provide by engine  */ 
  private String classAbsIndex;
   
  /**
   * Get absolute class index return from engine (use for RFSM mode) 
   * @return
   */
  public String getClassAbsIndex() {
	return classAbsIndex;
  }

  /**
   * Set absoulate class index  
   * @param classAbsIndex
   */
  public void setClassAbsIndex(String classAbsIndex) {
	this.classAbsIndex = classAbsIndex;
  }

  private int siblingIndex;	
  /**
   * @return the index of this sibling among all siblings.
   */
  public int getSiblingIndex () {return siblingIndex;}
  /**
   * Set the index of this sibling among all siblings.
   */
  public void setSiblingIndex (int siblingIndex) {this.siblingIndex = siblingIndex;}

  private String path;
  /**
   * @return the Path= information that may exist for some components (like menuitems).
   * This value may be null if the component is not one found via Path= information.
   */
  public String getPath () {return path;}
  /**
   * Set the Path= information that may exist for some components (like menuitems).
   * Generally, components found by Path= are not considered children of their parent 
   * object, but an additional recognition Path property added to the parent object.
   * For example, the "File->Exit" menuitem is a Path property tacked onto the parent 
   * menu as in:
   * <ul>
   * <li>ExitItem="Type=Window;Name=Main;\;Type=Menu;Index=1;Path=File->Exit"
   */
  public void setPath (String path) {this.path = path;}

  private PCTree parent;
  /**
   * @return the PCTree object (if any) that represents the parent component of the 
   * component represented by this PCTree object.  This may be null if no parent 
   * object exists or has been set.
   */
  public PCTree getParent () {return parent;}
  /**
   * Set the parent PCTree object that represents the parent component of the component 
   * represented by this PCTree object.
   */
  public void setParent (PCTree parent) {this.parent = parent;}

  private Map compMap;
  /**
   * @return the Map object that holds the PCTrees for 
   * all objects encountered up to this point in the object hierarchy.
   */
  public Map getCompMap () {return compMap;}
  /**
   * Set the Map object that holds the PCTrees for 
   * all objects encountered up to this point in the object hierarchy.
   */
  public void setCompMap (Map compMap) {this.compMap = compMap;}

  /**
   * stores our object type indices for all components encountered to this point in the hierarchy.
   */
  private transient Map indexMap;

  /** 
   * Stores id/name indices for all components encountered to this point in the hierarchy.<br>
   * The index is absolute index (calculated from the traversal of the whole tree)<br>
   * It contains pair (id/name, index). The index begins from 1.<br>
   */
  private static Map<String, Integer> idNameIndexMap = new HashMap<String, Integer>();
  /** Combine with name to create a key stored in {@link #idNameIndexMap}*/
  private final String NAME_INDEX_PREFIX = "NAME_INDEX_PREFIX";
  /** Combine with id to create a key stored in {@link #idNameIndexMap}*/
  private final String ID_INDEX_PREFIX = "ID_INDEX_PREFIX";
  /** The absolute index for nodes having the same name.*/
  private int nameIndex = -1;
  /** The absolute index for nodes having the same id.*/
  private int idIndex = -1;
  
  /**
   * stores our object type index for this component.
   */
  private transient int indexIndex = -1;

  public static final int totalChildCountWithCommentsAndBlankLines = 5;

  private String domainName;
  
  
  
  public void setDomainName(String domainName){this.domainName = domainName;}
  
  public String getDomainName(){return domainName;}
  
  // pieces used to build recognition strings
  public static final String recogSeparator = ";\\;";
  public static final String typePart = "Type=";
  public static final String classPart = "Class=";
  public static final String partSeparator = ";";
  public static final String namePart = "Name=";
  public static final String captionPart = "Caption=";
  public static final String indexPart = "Index=";
  public static final String pathPart = "Path=";
  public static final String textPart = "Text=";
  public static final String classIndexPart = "ClassIndex=";
  public static final String idPart = "Id=";
  public static final String processName = ".domain=";
  
  protected String getAppendedCompInfo(){
  	  if(!doAppendCompInfo()) return "";
  	  String _prefix = "|%|";
  	  String _type = (getType()==null)? getMyclass():getType();
  	  return _prefix + _type;
  }
  
  /**
   * Build the piece of the recognition string that represents this component only.
   * Parent recognition information will be retrieved and prepended separately.
   * @param origCallerIsParent true if this component is the topmost parent window.
   * @see #getParentRecogString()
   */
  protected String getRecogString (boolean origCallerIsParent) {
	String PREFIX = (isFullPathSearchMode()&& parent==null) ? GuiObjectVector.FULLPATH_SEARCH_MODE_PREFIX : "";
	PREFIX += (isMappedClassSearchMode()&& parent==null) ? GuiObjectVector.MAPPEDCLASS_SEARCH_MODE_PREFIX : "";
	
	
	if (!isRfsmSearchMode()){
	
	  	if(origCallerIsParent && getDefaultRecognition()!=null){
	  		Log.info("PCTree.getRecogString resorting to Default Recognition: "+ getDefaultRecognition());
	  		return PREFIX + getDefaultRecognition();
	  	}  	  	
	    
	  	String info = getPath();
	    
	    // see if we have Path= information (like in Menus)
	    // Path info is (normally) appended to the parent recognition string    
	    if (info != null) {
	        info = pathPart + info;
	        Log.info("PCTree.getRecogString Path: "+ info);
	        return PREFIX + info;
	    }
	    // use 'Index=' or 'ClassIndex=' to generate R-Strings (NLS friendly); standard topmost parent windows (Window,JavaWindow,..) wouldn't use Index.
	    else if (getIndexOnly() && !origCallerIsParent) {
	        info = partSeparator + (isClassIndex() ? classIndexPart : indexPart) + (getIndex()+1);
	   	    Log.info("PCTree.getRecogString indexOnly=true Index: "+ info);    	
	    }
	    // start the process of Auto-qualifier
	    // try by Id=  
	    else if (getId() != null) {    	
	    	info = partSeparator + idPart + getId();
	    	if(idIndex>1) info += partSeparator + indexPart + idIndex;
	    	Log.info("PCTree.getRecogString Id: "+ info);
	    }
	    // otherwise try by Caption=, Text=, or Name=
	    else if (getNameValue() != null) {
	        if (isCaption()) { 
		      	info = partSeparator + captionPart + getNameValue(); 
		      	Log.info("PCTree.getRecogString Caption: "+ info);
	        }
	        else if (isTextValue()) {
		      	info = partSeparator + textPart + getNameValue(); 
		      	Log.info("PCTree.getRecogString Text: "+ info);
	        }
	        else { 
	        	info = partSeparator + namePart + getNameValue(); 
	        	if(nameIndex>1) info += partSeparator + indexPart + nameIndex;
	        	Log.info("PCTree.getRecogString Name: "+ info);
	        }
	    }    
	    // need an ID one, too
	    
	    // getNameValue == null: use ClassIndex=
	    else {
	    	info = partSeparator + (isClassIndex() ? classIndexPart : indexPart) + (getIndex()+1);
	        Log.info("PCTree.getRecogString Index: "+ info);
	    }
	    // end the process of Auto-qualifier
	    
	    if (info == null) info = "";    
	    
	    // put it all together    
	    if (getMyclass() == null) {
	    	info = typePart + (origCallerIsParent ? "Window" : getType() )+ info;
	   	    Log.info("PCTree.getRecogString Recog: "+ info);
	        return PREFIX + info;
	    }
	    // Class= info required
	    else {	    	
	    	info = classPart + getMyclass() + info;    		
	    	Log.info("PCTree.getRecogString Class: "+ info);
	        return PREFIX + info;
	    }
	}else {
		return getRFSMRecogString(origCallerIsParent);
	}
  }
  
  /**
   * Build the piece of RFSM the recognition string that represents this component only.
   * Parent recognition information will be retrieved and prepended separately.
   * @param origCallerIsParent true if this component is the topmost parent window.
   * @see #getParentRecogString()
   * @param origCallerIsParent
   * @return
   */

  protected String getRFSMRecogString (boolean origCallerIsParent) {
		String PREFIX = "";
		
		if (parent == null ) {
			PREFIX = processName +getDomainName() + partSeparator;
		}
		
		String classPart = null;
		String classIndexPart= null;
		String classNamePart = null;
		String namePart= null;
		String idPart = null;
		String labelPart = null;
				
		if (getDomainName().equalsIgnoreCase(Domains.JAVA_DOMAIN)){
			// TODO: domain isn't supported yet
			classPart = ".class=";
			classIndexPart = ".classIndex=";
			namePart = "name=";
			labelPart = "label=";
		} else if (getDomainName().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
			classIndexPart = "automationIndex=";
			namePart = "automationName=";
			classNamePart = "className=";
			idPart = "id=";
		} else if (getDomainName().equalsIgnoreCase(Domains.HTML_DOMAIN)){
			// TODO: domain isn't supported yet			
		} else if (getDomainName().equalsIgnoreCase(Domains.NET_DOMAIN)){
			// TODO: domain isn't supported yet
		} else if (getDomainName().equalsIgnoreCase(Domains.WIN_DOMAIN)){
			// TODO: domain isn't supported yet
  		}
		
	  	if(origCallerIsParent && getDefaultRecognition()!=null){
	  		Log.info("PCTree.getRFSMRecogString resorting to Default Recognition: "+ getDefaultRecognition());
	  		return PREFIX + getDefaultRecognition();
	  	}  	  	
		  	
	  	String info = getPath();
	    
	    // see if we have Path= information (like in Menus)
	    // Path info is (normally) appended to the parent recognition string    
	    if (info != null) {
	        info = pathPart + info;
	        Log.info("PCTree.getRFSMRecogString Path: "+ info);
	        return PREFIX + info;
	    }
	   
	    // start the process of Auto-qualifier
	    // try by Id=  
	    else if (getId() != null) {    	
	       	info = partSeparator + idPart + getId(); 
	       	Log.info("PCTree.getRFSMRecogString Id: "+ info);    	
	    }
	    // otherwise try by Caption=, Text=, or Name=
	    else if (getNameValue() != null) {
	        if (isCaption()) { 
		      	info = partSeparator + captionPart + getNameValue(); 
		      	Log.info("PCTree.getRFSMRecogString Caption: "+ info);
	        }
	        else if (isTextValue()) {
		      	info = partSeparator + textPart + getNameValue(); 
		      	Log.info("PCTree.getRFSMRecogString Text: "+ info);
	        }
	        else { 
	        	info = partSeparator + namePart + getNameValue(); 
	        	Log.info("PCTree.getRFSMRecogString Name: "+ info);
	        }
	    }    
	    // need an ID one, too
	    
	    // getNameValue == null: use ClassIndex=
	    else {
	    	info = partSeparator + classIndexPart + getClassAbsIndex();
	   	    Log.info("PCTree.getRFSMRecogString Index: "+ info);
	    }
	    // end the process of Auto-qualifier
	    
	    if (info == null) info = "";    
	    
	    // put it all together    
	    if (getMyclass() == null) {
	    	info = typePart + (origCallerIsParent ? "Window" : getType() )+ info;
	   	    Log.info("PCTree.getRFSMRecogString Recog: "+ info);
	        return PREFIX + info;
	    }
	    // Class= info required
	    else {
	    	
	    	info = classNamePart + getMyclass() + info;    		
	    	Log.info("PCTree.getRFSMRecogString Class: "+ info);
	        return PREFIX + info;
	    }
  }
  
  /** 
   * Get index, first try using the 'indexMap' setup with setupCompIndex, then try getIndexOld
   **/
  protected int getIndex () {
    if (indexMap != null && indexIndex >= 0) {
      for(Iterator i = indexMap.values().iterator(); i.hasNext(); ) {
        PCTree next = (PCTree) i.next();
        if (next == this) {
          return indexIndex;
        }
      }
      System.out.println("**** can't find us in indexMap");
    } else {
      System.out.println("**** indexMap not setup or indexIndex < 0: "+indexIndex);
    }
    return getIndexOld();
  }

  /** 
   * if type equals our type, return getIndex()+1, basically incrementing any previous index 
   * for a component of the same type.
   * else if parent is not null, return parent.getIndex(type), maybe success at upper levels.
   * else return 0
   *
   * @param type, String type (getType)
   * @return int index
   **/
  protected int getIndex (String type) {
    if (type != null && getType() != null && type.equals(getType())) return getIndex();
    else if (parent != null) return parent.getIndex(type);
    return 0;
  }
  
  /** 
   * Generate index based on this algorithm:
   * First get a starting index by asking our parent for an index for the type.
   * Then we get our 'Base' index by:
   * getting our matching index based on all of our siblings, and our position.
   * We use 'getCompMap()' which is a Map keyed by 'type' of component.
   * The values in the Map are ArrayLists of PCTree elements.  If we match ourselves
   * in that list then that index is the index we want.
   * We then add in the parent index previously obtained from 'getIndex(getType())'
   *
   * @return int index
   **/
  protected int getIndexOld () {
    int pi = 0;
    if (parent != null) pi = parent.getIndex(getType());
    int base = getIndexBase();
    return base + pi;
  }
  
  /** 
   * Generate base index based on this algorithm:
   * getting our matching index based on all of our siblings, and our position.
   * We use 'getCompMap()' which is a Map keyed by 'type' of component.
   * The values in the Map are ArrayLists of PCTree elements.  If we match ourselves
   * in that list then that index is the index we want.
   *
   * @return int index
   **/
  protected int getIndexBase () {
    Map map = getCompMap();
    ArrayList list = (ArrayList)map.get(getType());
    if (list != null) {
      //System.out.println("for type: "+getType()+" Using List: "+list.size());
      Iterator j= list.iterator();
      for(int i=0; i<list.size(); i++) {
        PCTree next = (PCTree) j.next();
        if (next == this) {
          //System.out.println("MATCHED: "+i);
          return i;
        }
      }
    }
    System.out.println("DID NOT MATCH; using sibling index: "+getSiblingIndex());
    return getSiblingIndex();
  }

  /** 
   * Setup the IndexMap values based on our described algorithm.
   * <p>
   * @param rdepth, int depth into the hierarchy as we recursively drill down
   * @param indexMap, Map, used to keep the types with index appended (keys),the values are the PCTree instances themselves.
   * @param onlyOneChildVisible, boolean, used to indicate containers like TabControls
   * @param altMap, Map, alternate map cloned for siblings under containers like TabControls
   * with onlyOneChildVisible
   **/
  protected void setupIndexMap (int rdepth, HashMap indexMap, boolean onlyOneChildVisible, HashMap altMap) {
    this.onlyOneChildVisible = onlyOneChildVisible;

    //if(onlyOneChildVisible) altMap = (HashMap)altMap.clone();
  	HashMap localalt = onlyOneChildVisible ? (HashMap)altMap.clone(): altMap;
    
  	this.indexMap = indexMap;
    String type = getType();//null if no mapped type exists.
    if(type==null) 
    	type = removeNonNameChars(getMyclass());
    
    if (type==null) {
      System.err.println("type is null");
      type="";
    }
    boolean isContainerWithOnlyOneChildVisible = GuiObjectRecognition.isContainerTypeWithOnlyOneChildVisible(type);
    for(int i=0; ; i++) {
      String altType = type + ":" + Integer.toString(i+1);
      if (indexMap.get(altType) != null) { // already used, generate another.
        continue;
      }
      Log.info("PCTree.type index for "+ type +"="+ altType);
      indexMap.put(altType, this);
      indexIndex = i;
      String totalChildCount = Integer.toString(getTotalChildCountFromFirstChild());
      System.out.println("."+ (onlyOneChildVisible?"TAB.":"....") +
                         StringUtils.getSpaces(new Integer(3-totalChildCount.length())) +
                         totalChildCount + ".."
                         +StringUtils.getChars(new Integer(rdepth), " .")+"type:"+altType);
      break;
    }
    //increment other alttypes too, if existing
    if(getIndex_types().length > 1){
    	String alttype = null;
    	String ntype = null;
    	boolean done = false;
    	for(int i=0;i<getIndex_types().length;i++){
    		alttype = getIndex_typesIndex(i);
    		if (alttype==null) continue;//should not happen
    		done = false;
    		if (alttype.equalsIgnoreCase(type)) continue; // already done
    		for(int n=0; !done ;n++){
    			ntype = alttype +":"+ Integer.toString(n+1);
    			if(indexMap.get(ntype)!= null){
    				continue;
    			}
    			Log.info("PCTree.alttype indices for "+ alttype +"="+ ntype);
    			indexMap.put(ntype, this);
    			done = true;
    		}
    	}    	
    }
    
    setupIndexForIDName();
    
    PCTree achild = (PCTree)getFirstChild();

    if(isFullPathSearchMode()){
        if (achild != null) {
            achild.setupIndexMap(rdepth+1, new HashMap(), isContainerWithOnlyOneChildVisible, new HashMap());
        }
        achild = (PCTree)getNextSibling();
        if (achild != null) {
            achild.setupIndexMap(rdepth, indexMap, onlyOneChildVisible, indexMap);
        }
    }else{ // Classic Search Mode
        if (achild != null) {
        	achild.setupIndexMap(rdepth+1, indexMap, isContainerWithOnlyOneChildVisible, indexMap);
        }
        achild = (PCTree)getNextSibling();
        if (achild != null) {
            achild.setupIndexMap(rdepth, localalt, onlyOneChildVisible, localalt);
        }
    }
  }

  /**
   * Set the absolute index for tree nodes who have the same ID or Name.<br>
   * It will be called during the traversal of the whole tree.<br>
   * 
   * @see #setupIndexMap(int, HashMap, boolean, HashMap)
   */
  protected void setupIndexForIDName(){
	  String key = null;
	  Integer index = null;
	  
	  try{
		  String id = getId();
		  if(id!=null){
			  key = generateMapKey(ID_INDEX_PREFIX, id);
			  index = idNameIndexMap.get(key);
			  if(index==null){
				  this.idIndex = 1;
			  }else{
				  this.idIndex = index.intValue()+1;
			  }
			  idNameIndexMap.put(key, new Integer(idIndex));
		  }
	  }catch(Exception e){
		  Log.debug("Set index for id '"+id+"', Met Exception "+e.getClass().getSimpleName());
	  }
	  
	  try{
		  String name = getNameValue();
		  if(name!=null){
			  key = generateMapKey(NAME_INDEX_PREFIX, name);
			  index = idNameIndexMap.get(key);
			  if(index==null){
				  this.nameIndex = 1;
			  }else{
				  this.nameIndex = index.intValue()+1;
			  }
			  idNameIndexMap.put(key, new Integer(nameIndex));
		  }
	  }catch(Exception e){
		  Log.debug("Set index for name '"+name+"', Met Exception "+e.getClass().getSimpleName());
	  }
  }
  
  /**
   * Generate a key to be used for map {@link #idNameIndexMap}<br>
   * @param prefix	String, the key prefix, like {@link #NAME_INDEX_PREFIX} or {@link #ID_INDEX_PREFIX}
   * @param value	String, the object's name or object's id
   * @return	String, prefix+":"+value
   * @see #setupIndexForIDName()
   */
  protected String generateMapKey(String prefix, String value){
	  return prefix+":"+value;
  }
  
  /** 
   * Setup the IndexMap values based on our described algorithm.  
   * <p>
   * Note: This version calls the other 'protected' version, which uses recursion, and
   * passes 'false' for the parameter 'onlyOneChildVisible'
   * Note: All of the top level siblings are split out into separate calls
   * if they are of type 'Window' or 'JavaWindow' using separate HashMaps
   **/
  public void setupIndexMap () {
  	String firstchildtype;
    HashMap map = new HashMap();
    int i=0;
    for(PCTree tree = this; tree != null; tree = (PCTree)tree.getNextSibling()) {
      tree.setupIndexMap(0, map, false, map);
      try{ firstchildtype = ((PCTree)tree.getFirstChild()).getType();}
      catch(Exception x){firstchildtype=null;}
      System.out.println("....Did for: "+(i++)+", "+tree.getType()+", "+ firstchildtype);
      // if any of the top level siblings are Window or JavaWindow, reinit the hashmap
      if (tree.getType() != null) {        
        if (tree.isContainerTypesUsedToSetupIndexMap()) map = new HashMap();        
      }
    }
  }

  /**
   * (Future) Currently ALWAYS returns 0
   **/
  protected int getPathIndex() {
    return 0; //?? how to generate path index
  }

  /** 
   * Output the PCTree
   * @return our string
   **/
  public String toString () {
	return StringUtils.getSpaces(getLevel())+ getName() + "=\"" +
      getRecogString(isJavaWindow()&&(parent == null)) +  "\"" + getAppendedCompInfo() +
      (getFirstChild() == null ? "" : "\n"+getFirstChild().toString()) +
      (getNextSibling() == null ? "" : "\n"+getNextSibling().toString());
  }

  /**
   * @param aparent
   * @return true if the provided PCTree, usually a parent or ancestor, is one of the containerTypesIgnoredForRecognition  
   * or has one of the containerNamesIgnoredForRecognition or is one of the containerClassesIgnoredForRecognition. 
   * 
   * @see #containerTypesIgnoredForRecognition
   * @see #containerNamesIgnoredForRecognition
   * @see GuiObjectRecognition#containerClassesIgnoredForRecognition
   */
  protected boolean ignoreParentRecognitionString(PCTree aparent){
	String recogname = aparent.getType();
	boolean ignore = false;
	//ignore (Java) Type=RootPane
	if ((recogname != null)&&(recogname.length()>0)){
		ignore = GuiObjectRecognition.isContainerTypeIgnoredForRecognition(recogname);
	}
	if (!ignore){
		recogname = aparent.getNameValue();
		ignore = GuiObjectRecognition.isContainerNameIgnoredForRecognition(recogname);
	}
	if (!ignore){
		recogname = aparent.getObjectClass();
		ignore = GuiObjectRecognition.isContainerClassIgnoredForRecognition(recogname);
	}
	return ignore;
  }
  
  /** 
   * Recursively get all of the parent->parent->... recog strings.
   * If withNameIncludeOnlyCaption is true and this component is identified by 
   * Name then only the topmost parent info is returned.
   * <p>
   * Other items which modify the completeness or format of the recognition information include:
   * <ul>
   * <li>If the component is found via Path= (getPath() != null)
   * <li>If shortenGeneralRecognition has been set to true
   * <li>When ignoreParentRecognitionString returns true on certain ancestors
   * </ul>
   * @return  String representation of full parent hierarchy.  This does NOT include any portion 
   * of the recognition string for the local component itself.  That would be appended to the 
   * parent recognition information provided here.
   * 
   * @see #getRecogString(boolean)
   **/
  protected String getParentRecogString () {
    if (parent == null) return "";
    if (withNameIncludeOnlyCaption) {
      if ((getNameValue() != null)&&(!isCaption())&&(!isTextValue())) {
      	Log.info("PCTree.getParentRecognitionString withNameOnly detected Name qualifier VALID.");
        return getTopParent().getRecogString(false) + recogSeparator;
      }else{
      	Log.info("PCTree.getParentRecognitionString withNameOnly detected Name qualifier INVALID.");
      }
    }
    boolean isPath = (getPath()!=null);
    PCTree aparent = parent;
    String recog = "";
    String recogname = null;
    boolean ancestor = true;
    boolean ignore = false;
    
    
    while (aparent != null){
    	ignore = false;
    	if (isPath ){
    		// ignore immediate ancestors with Path information, this is duplicated
    		if (aparent.getPath() != null) {
    			ignore=true; // do nothing
    		// if this is our first true non-Path ancestor use it with parent;Path separation
    		}else if (ancestor){
    			recog = aparent.getRecogString(false) + partSeparator;
    			ancestor = !ancestor;
    		// otherwise use the traditional parent;\;child separation
    		}else if (!shortenGeneralRecognition){
    			if (isRfsmSearchMode()){
    				if (aparent.getParent()!= null){
    					recog = aparent.getRecogString(false) + recogSeparator + recog;
    				}
    			}else {
    				recog = aparent.getRecogString(false) + recogSeparator + recog;
    			}
    			
    		}else if(!ignoreParentRecognitionString(aparent)){
        		if (isRfsmSearchMode()){
    				if (aparent.getParent()!= null){
    					recog = aparent.getRecogString(false) + recogSeparator + recog;
    				}
        		}else {
        			recog = aparent.getRecogString(false) + recogSeparator + recog;
        		}
            }
    	}else if (!shortenGeneralRecognition){
    		//for RFSM, remove main window string 	
    		if(isRfsmSearchMode()){
	    		if ( aparent.getParent()!= null){
	    			recog = aparent.getRecogString(false) + recogSeparator + recog;
	    		}
    		} else {
    			recog = aparent.getRecogString(false) + recogSeparator + recog;
    		} 
    		// withNameIncludeOnlyCaption = shorten lengthy recog if possible
    	}else if(!ignoreParentRecognitionString(aparent)){
    		//for RFSM, remove main window string 	
    		if (isRfsmSearchMode()){
				if (aparent.getParent()!= null){
					recog = aparent.getRecogString(false) + recogSeparator + recog;
				}
    		} else {
    			recog = aparent.getRecogString(false) + recogSeparator + recog;
    		}
    	}
    	aparent = aparent.getParent();    	
    }
    return  recog;    
  }
  
  /** 
   * Get the topmost parent PCTree of this object. Otherwise, return this PCTree if it
   * is the topmost parent.
   **/
  private PCTree getTopParent () {
    if (parent == null) return this;
    return parent.getTopParent();
  }
  
  /** 
   * Output header String (like '[windowname]'), if isContainerTypesUsedToSetupIndexMap.
   * Otherwise we just return an empty string.
   * This is used during output of AppMap formatted information.
   **/
  protected String getHeader() {
    if ((getType() != null)&&(isContainerTypesUsedToSetupIndexMap())) {
        return "\n\n["+getName()+"]\n";
    }
    return "";
  }

  /**
   * Output a blank line and 'Child of' information if the parent control has onlyOneChildVisible 
   * (like TabControls).  This visually separates the children of different tabs.  
   * The routine also provides similar 'Child of' information at regular intervals for objects with 
   * a great many children.  Only provides such non-empty output if withCommentsAndBlankLines is true.
   * Otherwise, simply returns an empty string. 
   **/
  protected String getBlankLine() {
    if (isWithCommentsAndBlankLines()) {
      if (isOnlyOneChildVisible()) {
        String ptext = (getParent()==null?"Top Parent":"Child of "+getParent().getName());
        return "\n; "+ptext+" \n";
      } else if (getTotalChildCountFromFirstChild()>totalChildCountWithCommentsAndBlankLines) {
        String ptext = (getParent()==null?"Top Parent":"Child of "+getParent().getName());
        return "\n; "+ptext+" \n";
      }
    }
    return "";
  }

  /** 
   * Calculate the total child count of all children of this component.
   * @return int  0 if this component has no firstChild.
   **/
  public int getTotalChildCountFromFirstChild () {
    if (getFirstChild() != null) {
      return ((PCTree)getFirstChild()).getTotalChildCount();
    }
    return 0;
  }

  /** 
   * uses recursion to get all children from firstChild *AND* the  nextSibling. 
   * Does not count peer siblings and parents higher in the hierarchy, if any.
   * Includes this component itself, so the return is always >= 1.  
   * It's always best to get the true count by calling getTotalChildCountFromFirstChild().
   * @return int >=1
   * @see #getTotalChildCountFromFirstChild()
   **/
  private int getTotalChildCount () {
    return 1 +
      (getFirstChild() == null ? 0 : ((PCTree)getFirstChild()).getTotalChildCount()) +
      (getNextSibling() == null ? 0 : ((PCTree)getNextSibling()).getTotalChildCount());
  }

  /** 
   * @return String representing an INI formatted name=value representation of the component 
   * with recognition string.  An INI section header and any separating blank lines will also 
   * be included where appropriate.  The output will include the output from all children and 
   * subsequent siblings. 
   **/
  public String toIniString () {
    return
      getHeader() +
      getBlankLine() +
      getName() + "=\"" +
      // my parent part 
      getParentRecogString() +
      // my part
      getRecogString(isParentWindow()&&(parent == null)) + "\"" + getAppendedCompInfo() +
      (getFirstChild() == null ? "" : "\n"+((PCTree)getFirstChild()).toIniString()) +
      (getNextSibling() == null ? "" : "\n"+((PCTree)getNextSibling()).toIniString());
  }
  /**
   * @return String like returned by toIniString(). 
   * If isIgnoredNode() is true, the ignored containers will NOT be included in the INI formatted string. 
   */
  public String toIniStringWithoutIgnoredNodes(){
	  
	if(!isIgnoredNode()){
	    return
	      "\n" +
	      getHeader() +
	      getBlankLine() +
	      getName() + (rfsmSearchMode ? "=\""+GuiObjectVector.RFT_FIND_SEARCH_MODE_PREFIX : "=\"") +
	      // my parent part 
	       getParentRecogString() + 
	       // my part
	      getRecogString(isParentWindow()&&(parent == null)) + "\"" + getAppendedCompInfo() +
	      (getFirstChild() == null ? "" : ((PCTree)getFirstChild()).toIniStringWithoutIgnoredNodes()) +
	      (getNextSibling() == null ? "" : ((PCTree)getNextSibling()).toIniStringWithoutIgnoredNodes());
	}
	else return
	      (getFirstChild() == null ? "" : ((PCTree)getFirstChild()).toIniStringWithoutIgnoredNodes()) +
	      (getNextSibling() == null ? "" : ((PCTree)getNextSibling()).toIniStringWithoutIgnoredNodes());
  } 
  /**
   * See if this node can be ignored according to 'shortenGeneralRecognition'. Called by STAFPC
   */
  public boolean isIgnoredNode(){
	  if (shortenGeneralRecognition)
		return ignoreParentRecognitionString(this);
	  else
		return false;
  }
    
  /**
   * generate recognition string for the PCTree on this level. 
   * @param includeParentPart
   * @return recognition string.
   */ 
  public String getComponentRecogString(boolean includeParentPart){
      return (includeParentPart?
           getParentRecogString()+getRecogString(isParentWindow()&&(parent == null)):
           getRecogString(isParentWindow()&&(parent == null))
          );
  }

  /** 
   * For testing only.
   * Loads inputfile, write outputfile
   * opens outfilename for write, process the pctree stored in the xmlinput file
   * @param                     infilename, String
   * @param                     outfilename, String
   **/
  public static void main(String[] args) {
    System.out.println("args[0]:"+args[0]);
    System.out.println("args[1]:"+args[1]);
    String infilename = args[0];
    String outfilename = args[1];
    Writer writer = null;
    try {
      writer = new FileWriter(new CaseInsensitiveFile(outfilename).toFile());
    } catch (IOException io) {
      System.err.println(io.getMessage());
      return;
    }
    writer = new BufferedWriter(writer);
    try {
      PCTree tree = (PCTree) org.safs.xml.XMLEncoderDecoder.xmlDecode(infilename);
      if (tree != null) {
        tree.setupIndexMap();
        //System.out.println(" tree: \n"+(tree==null?"<null>":tree.toString()));
        writer.write(tree.toIniString());
        writer.write("\n");
      }
    }catch(IOException io) {
      System.err.println("io: "+io);
    }
    try{
      writer.close();
    } catch (IOException io) {
      io.printStackTrace();
      System.err.println(io.getMessage());
    }
  }

  /** 
   * Remove most typical non-alphanumeric characters from the provided text.  
   * " ;:'/\"\\-=!@#%^&*()+{}[]|<>?.,"
   * @param                     text, String
   * @return                    String without the non-alphanumeric chars
   **/
  public static String removeNonNameChars(String text) {
    if(text == null) //Fixed S0532729
    	return null;
	String result = text;
    String chars = " ;:'/\"\\-=!@#%^&*()+{}[]|<>?.,";
    for(int i=0; i<chars.length(); i++) {
      String nchar = chars.substring(i, i+1);
   	  result = removeNonNameChars(result, nchar);
    }
    return result;
  }

  /** 
   * Remove all occurrences of nextChar from the text string.
   * 
   * @param                     text, String
   * @param                     nextChar, String of one character
   * @return                    String without any 'nextChar'
   **/
  public static String removeNonNameChars(String text, String nextChar) {
    for(;;) {
      int index = text.indexOf(nextChar);
      if (index>=0) {
        text=text.substring(0, index) + text.substring(index+1, text.length());
      }
      else break;
    }
    return text;
  }

  /** 
   * @return true if getType() is case-insensitive member of 'containerTypesUsedToSetupIndexMap'
   * @see #containerTypesUsedToSetupIndexMap
   **/
  private boolean isContainerTypesUsedToSetupIndexMap () {
    return GuiObjectRecognition.isTopLevelContainer(getType());
  }

  /** 
   * @return true if getType() is 'JavaWindow'
   **/
  private boolean isJavaWindow () {
    String type = getType();
    if (type != null && type.equalsIgnoreCase("JavaWindow")) {
      return true;
    }
    return false;
  }
  
  /** 
   * @return true if getType() is 'Window' or 'JavaWindow' (case-insensitive)
   **/
  private boolean isParentWindow () {
    String type = getType();
    if (type != null){
    	return ((type.equalsIgnoreCase("Window")) ||
		    (type.equalsIgnoreCase("JavaWindow")) ||
		    (type.equalsIgnoreCase("DotNetWindow")) ||
		    (type.equalsIgnoreCase("FlexWindow")));
   	}
    return false;
  }
}
