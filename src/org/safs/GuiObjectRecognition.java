/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.*;

import org.safs.jvmagent.SAFSObjectNotFoundRuntimeException;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Used internally.<br>
 * Manages and manipulates object recognition string information.
 * Provides utility functions for parsing and identifying components of our 
 * recognition strings.  Example recognition strings:
 * <p><pre>
 * Type=Window;Caption=Notepad			 :Window with specified Caption
 * Type=EditBox;Index=2					 :2nd EditBox
 * Type=CheckBox;Name=check1	 		 :A Named CheckBox        	
 * Class=java.awt.Button;ClassIndex=1    :The first java.awt.Button
 * Type=JavaMenu;Index=1;Path=File->Exit :A File Exit menuitem.
 * </pre><p>
 * Recognition strings are made of of different named "pieces" or components.
 * <p><ol>
 * <li>Recognition "Category" 
 *     <ul>
 *     <li>Type
 *     <li>Class
 *     <li>CurrentWindow  (used alone)
 *     <li>Domain         (precedes Type or Class)
 *     <li>Process        (precedes Type or Class -- normally only for native domains)
 *     </ul>
 * <p>
 * <li>Recognition "Qualifier" (some not yet implemented)
 *     <ul>
 *     <li>Index = i
 *     <li>Class = text
 *     <li>ClassIndex = i
 *     <li>ObjectIndex = i
 *     <li>Caption = text
 *     <li>JavaCaption = text
 *     <li>HTMLTitle = text (Ex:"Type=HTMLTitle;HTMLTitle=text")
 *     <li>Level = i
 *     <li>Path = root->branch->leaf
 *     <li>Property = propertyname:value
 *     <li>PropertyContains = propertyname:value
 *     <li>Name = text
 *     <li>NameContains = text
 *     <li>Text = text
 *     <li>HTMLText = text
 *     <li>ID = text
 *     <li>HTMLID = text
 *     </ul>
 * </ol>
 * <p>
 * In general, a single object's recognition string is comprised of a Category and 
 * one or more Qualifiers.
 * <p>
 * Domain can specify a specific domain (like Domain=Html;) to search and would precede 
 * other Category info like Type or Class.  Domain would not normally be used with the 
 * Process Category as mentioned below.
 * <p>
 * Process can specify a specific Process (like Process=EXCEL.EXE;) to search and would 
 * precede other Category info like Type or Class.  This would normally only be used 
 * when dealing with native objects in the Win or Net Domains and would not normally be 
 * used with the Domain Category mentioned above.  The process name specified is currently 
 * case-sensitive, so an exact match is attempted when compared.
 * <p>
 * CurrentWindow is Process, Domain, and Category independent and means 
 * "the topmost window"--regardless of what Type or Domain it is.   
 * <p>
 * To identify a particular child object in a hierarchy of 
 * objects we provide a recognition string that provides some or all of the object 
 * hierarchy information.  Each child recognition string is separated from it's parent 
 * recognition string with an object separator substring: ";\;"
 * <p>
 * Example: Type=Window;Caption=MyApp;\;Type=EditBox;Index=1
 * <p>
 * This matches the first child EditBox (Index=1) in a Window with Caption "MyApp".
 * <p>
 * Note, however, that an instance of this class only handles the recognition string 
 * for a single object in the hierarchy.  There will be a separate instance handling 
 * each parent and each child of the full path recognition string.
 * 
 * 
 * @author Carl Nagle
 * @since  JUL 03, 2003
 * 
 * @author Carl Nagle JUL 08, 2003 Fixed refactoring error in extractQualifierInfo.
 * @author Carl Nagle NOV 05, 2004 Added Indexing Qualifier matches for multiple qualifiers.
 * @author Carl Nagle JUL 15, 2005 Added JavaClass support
 * @author CASEBEER   DEC 16, 2005 Added Property and PropertyContains qualifiers
 * @author Carl Nagle JAN 31, 2006 Fixed isMatchingName and added support for NameContains qualifier
 * @author Bob Lawler AUG 29. 2006 Added empty getObjectAccessibleName() so that individual engines 
 *         can implement their own method to retrieve the Object's accessible name.
 *                                 Updated isMatchingName() to first attempt match with 
 *         getObjectAccessibleName(), and if still necessary, then match with getObjectName(). This
 *         allows matches for recognition strings that include either in identifying the object.
 *                                 Updated isMatchingObject() to first try getObjectAccessibleName() 
 *         and, if necessary, try getObjectName() when saving the Object's name.  This preserves
 *         backwards compatability when retrieving Object name.
 * @author JunwuMa    JUN 17, 2008 Added "null.glassPane" to Array containerNamesIgnoredForRecognition. 
 *         When generating recognization string in STAFPC for Java apps, "null.glassPane" should be shown as 'Panel'.  	  
 * <br>		LeiWang		OCT 29, 2008	Modify method isMatchingClass(): when the category is class, we
 * 										will use wildcast to match it with object's classname.
 * 										Example: {ClassN*} will be matched to ClassName
 * 										See defect S0543037.
 * <br>     JunwuMa     NOV 3, 2008     Add 'FlexWindow' to array topLevelContainers[] used by isTopLevelContainer(String), 
 *                                      which is called by PCTree.isContainerTypesUsedToSetupIndexMap() for 
 *                                      creating individual indexMap.
 *                      NOV 6, 2008     Modify elements in array containerTypesIgnoredForRecognition[], removing "TabControl". 
 *                                      The R-String for "TabControl" should be generated, it usually is used to be 
 *                                      performed tab-choosing action on. 
 *                                      Removing "TabControl" is for supporting the change in STAFProcessContainer.
 *                                      In the change, containers not cared by users will NOT be put in mapping file and component hierarchy.
 *                      NOV 11, 2008    Added method isContainerClassIgnoredForRecognition(compClass) and array containerClassesIgnoredForRecognition[].
 *                                      See PCTree#ignoreParentRecognitionString(PCTree).
 *                                      This array may be put outside the code instead of hard-coded here.
 * <br>		Carl Nagle	    APR 08, 2009	Added missing support for NameContains
 * <br>		Carl Nagle	    APR 17, 2009	Added HTMLText support for isFinalMatch
 * <br>		Carl Nagle	    JUL 20, 2009	Added Domain= Category support.
 * <br>		Carl Nagle	    JUL 28, 2009	Added public getDomainValue Getter.
 * <br>		Carl Nagle	    JUL 28, 2009	Added Process= Category support.
 * <br>		Carl Nagle	    Aug 07, 2009	Fixed some Domain and Process initialization calls.
 * <br>		Carl Nagle	    Oct 26, 2009	Enhanced isMatchingText to check for undesirable mnemonic '&' chars.
 * <br>		Carl Nagle	    FEB 02, 2010	Qualifier HTMLTitle=text allows for matching isMatchingCaption OR isMatchingText
 * <br>		Carl Nagle	    JUN 15, 2010	Allow NBSP to be replaced with SPACE in HTML domain comparisons.
 * 
 * Copyright (C) (SAS) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public abstract class GuiObjectRecognition {

	/** ";" **/
	static public final String DEFAULT_QUALIFIER_SEPARATOR = ";";
	/** "=" **/
	static public final String DEFAULT_ASSIGN_SEPARATOR    = "=";
    /** ":" **/
    static public final String DEFAULT_PROPERTY_QUALIFIER_SEPARATOR = ":";
    /** "->" **/
    static public final String DEFAULT_PATH_SEPARATOR = "->";
	/** "Process" **/
	static public final String CATEGORY_PROCESS          = "Process";
	/** "Domain" **/
	static public final String CATEGORY_DOMAIN           = "Domain";
    /** "Class" **/
	static public final String CATEGORY_CLASS            = "Class";
	/** "JavaClass" **/
	static public final String CATEGORY_JAVA_CLASS       = "JavaClass";
	/** "Type" **/
	static public final String CATEGORY_TYPE             = "Type";
	/** "CurrentWindow" **/
	static public final String CATEGORY_CURRENTWINDOW    = "CurrentWindow";
	
	static public final int    CATEGORY_UNKNOWN_ID       = 0;
	static public final int    CATEGORY_CLASS_ID         = 1;  //same as QUALIFIER
	static public final int    CATEGORY_TYPE_ID          = 2;
	static public final int    CATEGORY_CURRENTWINDOW_ID = 3;
	static public final int    CATEGORY_DOMAIN_ID        = 4;
	static public final int    CATEGORY_PROCESS_ID       = 5;
		
	/** "Index" **/
	static public final String QUALIFIER_INDEX           = "Index";
	/** "ObjectIndex" **/
	static public final String QUALIFIER_OBJECT_INDEX    = "ObjectIndex";
	/** "ClassContains" **/
	static public final String QUALIFIER_CLASS_CONTAINS  = "ClassContains";
	/** "ClassIndex" **/
	static public final String QUALIFIER_CLASS_INDEX     = "ClassIndex";
	/** "AbsIndex" **/
	static public final String QUALIFIER_ABS_INDEX       = "AbsIndex";
	/** "Class" **/
	static public final String QUALIFIER_CLASS           = "Class";
	/** "JavaClass" **/
	static public final String QUALIFIER_JAVA_CLASS      = "JavaClass";
	/** "Caption" **/
	static public final String QUALIFIER_CAPTION         = "Caption";
	/** "JavaCaption" **/
	static public final String QUALIFIER_JAVACAPTION     = "JavaCaption";
	/** "Property" **/
	static public final String QUALIFIER_PROPERTY          = "Property";
	/** "PropertyContains" **/
    static public final String QUALIFIER_PROPERTY_CONTAINS = "PropertyContains";
	/** "Level" **/
	static public final String QUALIFIER_LEVEL           = "Level";
	/** "Path" **/
	static public final String QUALIFIER_PATH            = "Path";
	/** "Name" **/
	static public final String QUALIFIER_NAME            = "Name";
	/** "NameContains" **/
	static public final String QUALIFIER_NAME_CONTAINS   = "NameContains";
	/** "Text" **/
	static public final String QUALIFIER_TEXT            = "Text";
	/** "ID" **/
	static public final String QUALIFIER_ID              = "ID";

	/** "HTMLID" **/
	static public final String QUALIFIER_HTMLID          = "HTMLID";
	/** "HTMLText" **/
	static public final String QUALIFIER_HTMLTEXT        = "HTMLText";
	/** "HTMLTitle" **/
	static public final String QUALIFIER_HTMLTITLE       = "HTMLTitle";
	
	static public final int    QUALIFIER_UNKNOWN_ID      = 0;	
	static public final int    QUALIFIER_CLASS_ID        = 1; //same as CATEGORY
	static public final int    QUALIFIER_OBJECT_INDEX_ID = 2;
	static public final int    QUALIFIER_ABS_INDEX_ID    = 13;
	static public final int    QUALIFIER_INDEX_ID        = 3;
	static public final int    QUALIFIER_CLASS_INDEX_ID  = 4;
	static public final int    QUALIFIER_CAPTION_ID      = 5;
	static public final int    QUALIFIER_JAVACAPTION_ID  = 6;

	static public final int    QUALIFIER_PROPERTY_ID          = 7;
    static public final int    QUALIFIER_PROPERTY_CONTAINS_ID = 17;

	static public final int    QUALIFIER_LEVEL_ID        = 8;

	static public final int    QUALIFIER_NAME_ID          = 9;
	static public final int    QUALIFIER_NAME_CONTAINS_ID = 18;

	static public final int    QUALIFIER_TEXT_ID         = 10;
	static public final int    QUALIFIER_ID_ID           = 11;
	static public final int    QUALIFIER_PATH_ID         = 12;

	static public final int    QUALIFIER_HTMLTEXT_ID      = 14;
	static public final int    QUALIFIER_HTMLTITLE_ID     = 15;
	static public final int    QUALIFIER_HTMLID_ID        = 16;

	/** "{" Begin wildcard definition **/
	static public final String DEFAULT_OPEN_CAPTION_EXPRESSION  = "{";
	/** "}" End wildcard definition **/
	static public final String DEFAULT_CLOSE_CAPTION_EXPRESSION = "}";	
	static public final String DEFAULT_MATCH_ANY        = StringUtils.REGEX_CHARACTER_ANY + StringUtils.REGEX_CHARACTER_MATCH_MANY;

	/** "xA0" or #160 -- non-breaking space in Html */
	static public final String NBSP_STRING = String.valueOf((char)160);
	/**
	 * Default true means always convert NBSP characters to normal SPACE characters 
	 * during comparisons.  Set to false to require NBSP exact matching.
	 */
	static public boolean _convert_NBSP_Strings = true;
	
	protected String qualifierSeparator = DEFAULT_QUALIFIER_SEPARATOR;
	protected String assignSeparator    = DEFAULT_ASSIGN_SEPARATOR;

	/** the hierarchy level within the GuiObjectVector path represented by this recognition substring.
	 * The first item in the GuiObjectVector path is at govLevel 0. 
	 **/
	protected int govLevel = -1;
	
	/** the full recognition string provided to constructor 
	 *  Ex: "Type=Window;Caption=MyApp" **/
	protected String pathInfo          = null;
	
	/** only the Domain portion of the recognition string, if present. 
	 *  Ex: "Domain=Html" **/		
	protected String domainInfo         = null;
	
	/** only the Domain value portion of the recognition string, if present. 
	 *  Ex: "Html" **/		
	protected String domainValue        = null;
		
	/**
	 * Normally only valid for the topmost Parent GOR object.
	 * @return the domainValue. The "Html" of "Domain=Html", if present.  May be null.
	 */
	public String getDomainValue() { 
		if(domainValue==null && !domainTried) initDomainInfo();
		return domainValue; 
	}

	/** only the Process portion of the recognition string, if present. 
	 *  Ex: "Process=Excel.exe" **/		
	protected String processInfo         = null;
	
	/** only the Process value portion of the recognition string, if present. 
	 *  Ex: "Excel.exe" **/		
	protected String processValue        = null;
		
	/**
	 * Normally only valid for the topmost Parent GOR object.
	 * @return the processValue. The "Excel.exe" of "Process=Excel.exe", if present.  May be null.
	 */
	public String getProcessValue() {
		if(processValue==null && !processTried) initDomainInfo();
		return processValue; 
	}

	/** only the Category portion of the recognition string. 
	 *  Ex: "Type=Window" **/		
	protected String classInfo         = null;
	
	/** only the Category class/type portion of the recognition string. 
	 *  Ex: "Type" **/		
	protected String classCategory     = null;
		
	/** The Category ID of the recognition string. 
	 *  Ex: CATEGORY_TYPE_ID **/		
	protected int    classCategoryID   = CATEGORY_UNKNOWN_ID;
	
	/** index to the Category value portion of the recognition string.
	 * Normally, this is 0.  However, it will be >0 if Domain is present. 
	 *  Ex: "Type=Window" **/		
	protected int categoryIndex        = 0;
		
	/** only the Category value portion of the recognition string. 
	 *  Ex: "Window" **/		
	protected String classValue        = null;
		
	/** only the Qualifier portion of the recognition string. 
	 *  Ex: "Caption=MyApp" **/		
	protected String qualifierInfo       = null;
	
	/** There can be multiple qualifiers.  So they are stored in the gorInfos array. **/		
	protected GORInfo[] gorInfos         = new GORInfo[0];
	
	/** 
	 * Tracks multiple qualifier matches.
	 * When there is more than one qualifier, such as Text=SomeText;Index=2, we need to 
	 * count how many times we match all other qualifiers besides Index.  All other qualifiers 
	 * matching will increment this counter which will be compared against the target 
	 * Index=N value. **/
	protected int qualifierMatches       = 0;


    public static final String[] htmlElements = {
      "Html.!",
      "Html.A",
      "Html.AREA",
      "Html.B",
      "Html.BODY",
      "Html.BR",
      "Html.BUTTON",
      "Html.CENTER",
      "Html.CHECKBOX",
      "Html.DIALOG",
      "Html.DIALOGBUTTON",
      "Html.DIV",
      "Html.FONT",
      "Html.FORM",
      "Html.FRAME",
      "Html.FRAMESET",
      "Html.HEAD",
      "Html.HTML",
      "Html.HtmlDocument",
      "Html.IMG",
      "Html.INPUT.hidden",
      "Html.INPUT.image",
      "Html.INPUT.text=EditBox",
      "Html.INPUT.checkbox",
      "Html.INPUT.password",
      "Html.INPUT.radio",
      "Html.INPUT.submit",
      "Html.INPUT.button",
      "Html.TEXTAREA",
      "Html.LINK",
      "Html.MAP",
      "Html.META",
      "Html.P",
      "Html.PASSWORD",
      "Html.RADIO",
      "Html.SCRIPT",
      "Html.SELECT",
      "Html.SPAN",
      "Html.STYLE",
      "Html.SUBMIT",
      "Html.TABLE",
      "Html.TBODY",
      "Html.TD",
      "Html.TEXT",
      "Html.TEXTAREA",
      "Html.TextNode",
      "Html.TH",
      "Html.TITLE",
      "Html.TR"
    };

    public static boolean isHtmlElement(String element) {
        if (element != null) {
          for(int i=0; i<htmlElements.length; i++) {
            if (element.toLowerCase().indexOf(htmlElements[i].toLowerCase()) >= 0) return true;
          }
        }
        return false;
      }

    public static final String[] linkHtmlElements = {
      "Html.A",
      "Html.LINK"
    };

    public static boolean isLinkHtmlElement(String element) {
        if (element != null) {
          for(int i=0; i<linkHtmlElements.length; i++) {
            if (element.toLowerCase().indexOf(linkHtmlElements[i].toLowerCase()) >= 0) return true;
          }
        }
        return false;
      }

    public static final String[] DhtmlElements = {
      "Html.HtmlBrowser.ToolbarButton",
      "Html.BUTTON",
      "Html.CHECKBOX",
      "Html.DIALOGBUTTON",
      "Html.INPUT.hidden",
      "Html.INPUT.submit",
      "Html.INPUT.button",
      "Html.INPUT.text",
      "Html.PASSWORD",
      "Html.RADIO",
      "Html.SELECT",
      "Html.SUBMIT",
      "Html.TEXT",
      "Html.TEXTAREA"
    };

    public static boolean isDHtmlElement(String element) {
        if (element != null) {
          for(int i=0; i<DhtmlElements.length; i++) {
            if (element.toLowerCase().indexOf(DhtmlElements[i].toLowerCase()) >= 0) return true;
          }
        }
        return false;
      }  	  	
    
    /**
     * List of items considered to be component containers:<br>
     * TabControl, JavaPanel, JavaWindow, Window, JavaSplitPane
     */
    protected static final String[] containerTypes = {
        "TabControl", "JavaPanel", "JavaWindow", "Window", "JavaSplitPane"
    };
    
    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the containerTypes list.
     */
    public static boolean isContainerType(String compType){
    	  try{
	  	  	  for(int i=0;i<containerTypes.length;i++){
	  	  	  	  if (GuiClassData.classtypeContainsClassType(compType,containerTypes[i]))
	  	  	  	  	return true;
	  	  	  }
    	  }catch(NullPointerException npx){;}
  	  return false;
    }
    
    /**
     * List of containers whose non-visible children we may sometimes wish to process.
     * This list affects how indices are calculated for immediate container children and 
     * their children as the indices are reset to entry values for each separate child container 
     * for these objects.  See the 'setupIndexMap' function for more details on this.
     * <p>
     * Some containers, like TabControls, might have multiple children, but only one is visible 
     * at any given time.  For example, only one TabPane is selected and visible in a TabControl 
     * at any given time.  This is a list of such known container types.
     * @see #setupIndexMap
     */
    protected static final String[] containerTypesWithOnlyOneChildVisible = {
        "TabControl"
    };

    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the containerTypesWithOnlyOneChildVisible list.
     */
    public static boolean isContainerTypeWithOnlyOneChildVisible(String compType){
  	  try{
  	  	  for(int i=0;i<containerTypesWithOnlyOneChildVisible.length;i++){
	  	  	  	  if (GuiClassData.classtypeContainsClassType(compType,containerTypesWithOnlyOneChildVisible[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }

    /**
     * List of known container types that should have IndexMaps prepared for their children.
     * These are generally top level containers we know we want to process as testable windows. 
     */
    protected static final String[] topLevelContainers = {
        "Window", "JavaWindow", "CIMainFrame", "OleEmbeddedFrame", "FlexWindow"
    };

    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the topLevelContainers list.
     */
    public static boolean isTopLevelContainer(String compType){
  	  try{
  	  	  for(int i=0;i<topLevelContainers.length;i++){
	  	  	  	  if (GuiClassData.classtypeContainsClassType(compType, topLevelContainers[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }

    /**
     * List of components from which we should not getChildren() as we go through the hierarchy.
     * ComboBox, ListBox, CellRendererPane
     */
    protected static final String[] ignoreTypeChildren = {
    		"ComboBox", "ListBox", "CellRendererPane"
    };
    
    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the ignoreTypeChildren list.
     */
    public static boolean isIgnoredTypeChild(String compType){
  	  try{
  	  	  for(int i=0;i<ignoreTypeChildren.length;i++){
  	  	  	  if (GuiClassData.classtypeContainsClassType(compType, ignoreTypeChildren[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }

    /**
     * List of container types we may wish to exclude from longer recognition strings.
     * For example, we don't really care about the RootPane of a JFrame, or the TabControl 
     * parent of child Panels.  We usually care more about identifying the child Panels instead.
     */
    /* Removed 'TabControl' since its R-String should be generated to be used to performed tab-choosing action on. 
     * Moved elements prefixed with 'Html.' to containerClassesIgnoredForRecognition[].
     * Those elements can't match the passed component type, which is registered in JavaObjectsMap.dat.
     * (JunwuMa NOV 6, 2008) 
     */
    protected static final String[] containerTypesIgnoredForRecognition = {
    	    "RootPane", /*"TabControl",*/ "CellRendererPane", "Html.!",
			"Html.HTML", "Html.BASE", "Html.META", "Html.HEAD", "Html.BODY", 
			"Html.H1", "Html.H2", "Html.H3", "Html.H4", "Html.H5", "Html.H6", 
			"Html.P", "Html.SCRIPT", "Html.NOSCRIPT", "Html.TBODY",  "Html.TH", "Html.TR",
			"WPFTextBlock", "WPFAccessText", "WPFListBoxItem", "WPFListViewItem", "WPFComboBoxItem",
			"WPFTreeViewItem"
    };
    
    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the containerTypesIgnoredForRecognition list.
     */
    public static boolean isContainerTypeIgnoredForRecognition(String compType){
  	  try{
  	  	  for(int i=0;i<containerTypesIgnoredForRecognition.length;i++){
  	  	  	  if (GuiClassData.classtypeContainsClassType(compType, containerTypesIgnoredForRecognition[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }
    
    /**
     * List of container classes we may wish to not only exclude from longer recognition strings but also exclude 
     * from whole component hierarchy. Without showing those container classes, the component hierarchy would be more 
     * clear with less levels. It could be more easy for users to find out the cared component in the the component hierarchy.
     * For example, we don't really care about mx.containers.GridItem in Flex.  We usually care more about identifying 
     * the child Panels instead.
     * 
     */
    protected static final String[] containerClassesIgnoredForRecognition = {
			"Html.HTML", "Html.BASE", "Html.META", "Html.HEAD", "Html.BODY", 
			"Html.H1", "Html.H2", "Html.H3", "Html.H4", "Html.H5", "Html.H6", 
			"Html.P", "Html.SCRIPT", "Html.NOSCRIPT", "Html.TBODY",  "Html.TH", "Html.TR",
			"FlexBox"
    };
    
    /**
     * Called by PCTree#ignoreParentRecognitionString(PCTree)
     * @param compClass
     * @return true if compClass.equalsIgnoreCase an item in the containerClassesIgnoredForRecognition list.
     */
    public static boolean isContainerClassIgnoredForRecognition(String compClass){
  	  try{
  	  	  for(int i=0;i<containerClassesIgnoredForRecognition.length;i++){
  	  	  	  if (compClass.equalsIgnoreCase(containerClassesIgnoredForRecognition[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }

    /**
     * List of container names we may wish to exclude from longer recognition strings.
     * These are generally containers given default names by the OS or JVM for container 
     * types that we otherwise do care about, but not these particular instances.
     * Example, the low-level contentPane (JavaPanel) and layeredPane (JavaPanel) of a JFrame 
     * we don't really need in the recognition string, but other JavaPanels we do care 
     * about.
     */
    protected static final String[] containerNamesIgnoredForRecognition = {
    	    "null", "null.contentPane", "null.layeredPane", DriverConstant.SAFS_NULL, "null.glassPane"    	  
    };

    /**
     * @param compName
     * @return true if compName.equalsIgnoreCase an item in the containerNamesIgnoredForRecognition list.
     */
    public static boolean isContainerNameIgnoredForRecognition(String compName){
  	  try{
  	  	  for(int i=0;i<containerNamesIgnoredForRecognition.length;i++){
  	  	  	  if (compName.equalsIgnoreCase(containerNamesIgnoredForRecognition[i])) return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }

    /**
     * List of component types we may wish to getText on for recognition purposes.
     * For example, the static text of comps like labels and buttons may be appropriate 
     * to use for Text= types of recognition.
     */
    protected static final String[] textOKForRecognition = {
    	    "Label", "PushButton", "CheckBox", "RadioButton", "MenuItem", 
			"HTMLTitle", "HTMLLink", "HTMLTableCell"
    };

    /**
     * @param compType
     * @return true if compType.equalsIgnoreCase an item in the textOKForRecognition list.
     */
    public static boolean isTextOKForRecognition(String compType){
  	  try{
  	  	  for(int i=0;i<textOKForRecognition.length;i++){
  	  	  	  if (GuiClassData.classtypeContainsClassType(compType, textOKForRecognition[i]))
	  	  	  	  	return true;
  	  	  }
  	  }catch(NullPointerException npx){;}
  	  return false;
    }
    
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * Standard Constructor using default qualifier separator.
	 * 
	 * @param objectInfo the recognition string representing one of the parent 
	 *        or child objects in the full hierarchy being searched.
	 **/
	public GuiObjectRecognition(String objectInfo, int govLevel){
		
		try{
			if (govLevel >= 0) this.govLevel=govLevel;
			pathInfo  = new String(objectInfo);
		}			
		catch(NullPointerException np){;}
	}
	
	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * Constructor using a different qualifier separator from our standard.
	 * 
	 * @param objectInfo the recognition string representing one of the parent 
	 *        or child objects in the full hierarchy being searched.
	 * <p>
	 * @param aQualifierSeparator an alternate qualifier separator used in this
	 *        recognition string.  If null, then the default separator will be used.
	 **/
	public GuiObjectRecognition(String objectInfo, String aQualifierSeparator, int govLevel){

		this(objectInfo, govLevel);		
		try{ 
			// force an exception if separator invalid
			int l = aQualifierSeparator.length();
			qualifierSeparator = new String(aQualifierSeparator); }					
		catch(NullPointerException np){;}		
	}
	
	/**
	 * Returns the govLevel assigned in the constructor.
	 * @return int govLevel assigned at construction.
	 */
	public int getGovLevel(){ return govLevel;}
	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * @return String recognition string provided at construction.	<br>
	 *         Ex: "Type=Window;Caption=MyApp"
	 **/
	public String getObjectRecognition()  { return pathInfo; }


	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * @return String qualifier separator in use.	 
	 **/
	public String getQualifierSeparator() { return qualifierSeparator; }

	private boolean domainTried = false;
    private boolean processTried = false;    
	/**
	 * Initialize to isolate any Domain= or Process= information in the 
	 * recognition string.
	 */
	public void initDomainInfo(){
		if(domainTried && processTried) return;
		if ((!domainTried) && (pathInfo.substring(0,6).equalsIgnoreCase(CATEGORY_DOMAIN))){
			domainTried = true;
			int i = pathInfo.indexOf(qualifierSeparator);
			if (i > 0){
				domainInfo = pathInfo.substring(0, i);
				String[] tokens = domainInfo.split("=");
				domainValue = tokens[1].trim();
				try{
					pathInfo = pathInfo.substring(i+1);
				}catch(Exception x){
					Log.debug("GOR.initDomainInfo IGNORING invalid string: "+pathInfo);
				}
				Log.info("GOR.initDomainInfo extracted domainValue: "+ domainValue);
			}else{
				// invalid recognition string really,
				// but Domain *is* present
				Log.debug("GOR.initDomainInfo IGNORING invalid string: "+pathInfo);
			}
			initDomainInfo();
	    }else if ((!processTried)&&(pathInfo.substring(0,7).equalsIgnoreCase(CATEGORY_PROCESS))){
	    	processTried = true;
			int i = pathInfo.indexOf(qualifierSeparator);
			if (i > 0){
				processInfo = pathInfo.substring(0, i);
				String[] tokens = processInfo.split("=");
				processValue = tokens[1].trim();
				try{
					pathInfo = pathInfo.substring(i+1);
				}catch(Exception x){
					Log.debug("GOR.initDomainInfo IGNORING invalid string: "+pathInfo);
				}
				Log.info("GOR.initDomainInfo extracted processValue: "+ processValue);
			}else{
				// invalid recognition string really,
				// but Process *is* present
				Log.debug("GOR.initDomainInfo IGNORING invalid string: "+pathInfo);
			}
			initDomainInfo();
	    }else{
	    	domainTried=true;
	    	processTried=true;
	    }
	}
	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * Sets the domainInfo, domainValue if Domain is present.
	 * @return true if the recognition string contains leading Domain information 
	 *  (Ex: Domain=Html)
	 **/
     protected boolean hasDomainInfo(){
		try{
			if (domainValue instanceof String) return true;
			if (!domainTried) {
				initDomainInfo();
				return domainValue instanceof String;
			}
		}
		catch(Exception np){
			Log.debug("GOR.hasDomainInfo IGNORING "+ np.getClass().getSimpleName()+" "+ np.getMessage());
		}     	
		return false;
     }
	
 	/** 
 	 * Used internally, or by direct subclasses only.<br>
 	 * Sets the processInfo, processValue if Process is present.
 	 * @return true if the recognition string contains leading Process information 
 	 *  (Ex: Process=Excel.exe)
 	 **/
      protected boolean hasProcessInfo(){
 		try{
 			if (processValue instanceof String) return true;
 			if (!processTried) {
 				initDomainInfo();
 				return processValue instanceof String;
 			}
 		}
 		catch(Exception np){
 			Log.debug("GOR.hasProcessInfo IGNORING "+ np.getClass().getSimpleName()+" "+ np.getMessage());
 		}     	
 		return false;
      }
 	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * @return true if the recognition string contains leading Category information 
	 *          (Type= or Class= or JavaClass= etc..)
	 * The routine calls hasDomainInfo to see if Category info is preceded by Domain.
	 * The call to hasDomainInfo also sets the categoryIndex if necessary.          
	 **/
     protected boolean hasCategoryInfo(){
		try{
			hasDomainInfo(); //ensure categoryIndex is set
			// check for known Class Category text first
			if ( (pathInfo.substring(categoryIndex, categoryIndex + 4).equalsIgnoreCase(CATEGORY_TYPE))  ||
		         (pathInfo.substring(categoryIndex, categoryIndex + 5).equalsIgnoreCase(CATEGORY_CLASS)) ||
		         (pathInfo.substring(categoryIndex, categoryIndex + 9).equalsIgnoreCase(CATEGORY_JAVA_CLASS))||
		         (pathInfo.substring(categoryIndex, categoryIndex + 13).equalsIgnoreCase(CATEGORY_CURRENTWINDOW))){
		         return true;
		    }
		}
		catch(Exception np){
			Log.debug("GOR.hasCategoryInfo IGNORING "+ np.getClass().getSimpleName()+" "+ np.getMessage());
		}     	
		return false;
     }
	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * @return String Category portion of the provided recognition string minus and
	 * Domain information, if present.<br>
	 *         Ex: "Type=Window"  or  "Class=aClassName"
	 * This may be the full recognition string of qualifiers if no Type= or Class= 
	 * category info exists in the recognition string. Ex: "HTMLTitle=MyApp"
	 * In that case, the class category will be UNKNOWN (0).
	 **/
	public String getClassRecognition(){

		if (classInfo instanceof Object) return classInfo;

		// minimally the classinfo will contain the full recognition string
		// Ex: Type=HTMLLink  (match any/all HTMLLinks)
		// classInfo NOT of Type= or Class= or CURRENTWINDOW will have an UNKNOWN ID
		classInfo = pathInfo;		
		try{
			// check for known Class Category text first
			if (hasCategoryInfo()){		         
				// find the expected index to the class qualifier(Index=, Caption=m etc.)
				int qualifierIndex = pathInfo.indexOf(qualifierSeparator, categoryIndex);	
				if (qualifierIndex > 0) { 
					classInfo = pathInfo.substring(categoryIndex, qualifierIndex );
				}
		    }
		}
		catch(Exception np){
			Log.debug("GOR.getClassRecognition IGNORING classInfo "+ np.getClass().getSimpleName()+" "+ np.getMessage());
		}
		Log.info("GOR.classInfo extracted : "+ classInfo);					
		return classInfo;
	}


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Internal helper routine to fill internal fields.<br>
	 * Separates the Category into classCategory and classValue fields.<br>
	 * Ex: "Type=Window" gets separated into:
	 * <p>
	 * classCategory = "Type"<br>
	 * classValue = "Window"
	 **/
	protected void extractClassInfo() {

		if (! (getClassRecognition() instanceof Object))  return; 

		// find the assignSeparator "=" in Class= or Type=
		// EXCEPT for "CurrentWindow"
		if (classInfo.equalsIgnoreCase(CATEGORY_CURRENTWINDOW)){
			classCategory = CATEGORY_CURRENTWINDOW;
			classValue = CATEGORY_CURRENTWINDOW;
			classCategoryID = CATEGORY_CURRENTWINDOW_ID;
			return;
		}
		int eq = classInfo.indexOf(assignSeparator);		
		if ((eq < 2)||(eq == classInfo.length() -1)) return;
		
		// separate class category(Type, Class) from name
		classCategory = classInfo.substring(0, eq);
		classValue = classInfo.substring(eq+1);

		// set the ID of the category
		if ((classCategory.equalsIgnoreCase(CATEGORY_CLASS))||
		    (classCategory.equalsIgnoreCase(CATEGORY_JAVA_CLASS)))
		{			
			classCategoryID = CATEGORY_CLASS_ID;
		}
		else if(classCategory.equalsIgnoreCase(CATEGORY_TYPE)) {			
			classCategoryID = CATEGORY_TYPE_ID;
		}
		else { 
			classCategoryID = CATEGORY_UNKNOWN_ID; 
		}		
		return;
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @return the classCategory portion of the recognition string.<br>
	 *         Ex: Of "Type=Window", this returns "Type".
	 **/
	public String getClassCategory(){

		if (classCategory == null) extractClassInfo();		
		return classCategory;		
	}
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @return the int Constant that represents the type of class category we have.<br>
	 *         Ex: CATEGORY_CLASS_ID  or  CATEGORY_TYPE_ID
	 **/
	public int getClassCategoryID(){

		if (classCategory instanceof Object) return classCategoryID;

		extractClassInfo();		
		return classCategoryID;		
	}
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @return the classValue portion of the recognition string.<br>
	 *         Ex: Of "Type=Window", this returns "Window".
	 **/
	public String getClassValue(){

		if (classValue instanceof Object) return classValue;
		
		extractClassInfo();				
		return classValue;		
	}
	
	
	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * @return String Qualifier portion of the provided recognition string.<br>
	 *         Can include multiple separated qualifiers.<br>
	 *         May have no Class Category at all!! Type= or Class= may not be present!<br>
	 *         Ex: "Caption=MyApp"  or  "Name=aName"  or  "Index=1;Path=File->Exit"
	 **/
	public String getQualifierRecognition(){

		if (qualifierInfo == null){
			
			try{
				// check for known Class Category text first
				if (hasCategoryInfo()){			         
					// find the expected index to the class qualifier(Index=, Caption=m etc.)
					// this is the first instance of the qualifier separator in the category info
					// note: categoryIndex is used in case Domain= exists
					int qualifierIndex = pathInfo.indexOf(qualifierSeparator, categoryIndex);
			
					if (qualifierIndex > 0){
						qualifierInfo = pathInfo.substring(qualifierIndex + qualifierSeparator.length());
					}
			    }
			    // the whole path would be nothing but qualifiers to match ANY object
			    // Ex: HTMLText="MyText"  (the item of any type containing "MyText")
			    else{
			    	qualifierInfo = pathInfo;
			    }
				// infinite loop bypassed because we will not call extractQualifierInfo
				// if qualifierInfo is null;
				if (qualifierInfo instanceof String) {extractQualifierInfo();}
			}
			catch(Exception np){;}	
		}

		return qualifierInfo;		
	}	
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Internal helper routine to fill internal qualifiers array.<br>
	 * Separates the Qualifiers into the array of GORInfos.<br>
	 * (Though, usually, there is only 1 qualifier.)<br>
	 * 2009.04.08 (Carl Nagle) Added missing support for NameContains
	 **/
	private void extractQualifierInfo() {

		// infinite loop bypassed because we will not be called from getQualifierRecognition 
		// if the value returned from getQualifierRecognition is null;
		if (qualifierInfo == null)  return; 

		// check for multiple qualifiers
		StringTokenizer toker = new StringTokenizer(qualifierInfo, qualifierSeparator);
		int qualifiers = toker.countTokens();
		
		if (qualifiers == 0) return;

		// reinitialize the array
		gorInfos = new GORInfo[qualifiers];		

		int i = 0;
		while(toker.hasMoreTokens()){
			String qualifier = toker.nextToken();

			// find the assignSeparator "=" in Class= or Type=
			int eq = qualifier.indexOf(assignSeparator);
			
			if ((eq < 2)||(eq == qualifier.length() -1)) continue;
			
			// separate category(Index, Caption) from value
			String category = qualifier.substring(0, eq);
			String value = qualifier.substring(eq+1);
			int    id = QUALIFIER_UNKNOWN_ID;
	
			// set the ID of the category
			if (category.equalsIgnoreCase(QUALIFIER_INDEX)) {
				
				id = QUALIFIER_INDEX_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_OBJECT_INDEX)) {
				
				id = QUALIFIER_OBJECT_INDEX_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_ABS_INDEX)) {
				
				id = QUALIFIER_ABS_INDEX_ID;
			}
			else if((category.equalsIgnoreCase(QUALIFIER_CLASS)) ||
			         (category.equalsIgnoreCase(QUALIFIER_JAVA_CLASS))) {
				
				id = QUALIFIER_CLASS_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_CLASS_INDEX)) {
				
				id = QUALIFIER_CLASS_INDEX_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_CAPTION)) {
				
				id = QUALIFIER_CAPTION_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_JAVACAPTION)) {
				
				id = QUALIFIER_JAVACAPTION_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_PROPERTY)) {
				
				id = QUALIFIER_PROPERTY_ID;
			}
            else if(category.equalsIgnoreCase(QUALIFIER_PROPERTY_CONTAINS)){
                id = QUALIFIER_PROPERTY_CONTAINS_ID;
            }
			else if(category.equalsIgnoreCase(QUALIFIER_LEVEL)) {
				
				id = QUALIFIER_LEVEL_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_PATH)) {
				
				id = QUALIFIER_PATH_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_NAME)) {
				
				id = QUALIFIER_NAME_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_NAME_CONTAINS)) {
				
				id = QUALIFIER_NAME_CONTAINS_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_TEXT)) {
				
				id = QUALIFIER_TEXT_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_ID)){
				
				id = QUALIFIER_ID_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_HTMLID)){
				
				id = QUALIFIER_HTMLID_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_HTMLTITLE)){
				
				id = QUALIFIER_HTMLTITLE_ID;
			}
			else if(category.equalsIgnoreCase(QUALIFIER_HTMLTEXT)){
				
				id = QUALIFIER_HTMLTEXT_ID;
			}
			else {
				id = QUALIFIER_UNKNOWN_ID;
			}
			gorInfos[i++] = new GORInfo(qualifier, category, id, value);
		}		
		return;
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @return the number of qualifiers found in the recognition string.<br>
	 *         Usually there is just 1.  Occassionally, more.
	 **/
	public int getQualifierCount(){
		if (getQualifierRecognition() == null) return 0;		
		int count = 0;
		try{count = gorInfos.length;}catch(Exception e){Log.info("getQualifierCount: ", e);}
		return count;
	}
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @param index the 0-based index of the gorInfo qualifier to interrogate.<br>
	 *              index=0 is the first (and usually "only") qualifier.
	 * <p>
	 * @return the qualifierCategory portion of the indexed qualifier string.<br>
	 *         Ex: Of "Caption=MyApp", this returns "Caption".
	 **/
	public String getQualifierCategory(int index) {
		if (getQualifierRecognition() == null) return null;		
		GORInfo info;		
		String qualifier = null;
		try{ info = gorInfos[index];
			 qualifier = info.itemName;			
		}catch(Exception e){Log.info("getQualifierCategory", e);}
		return qualifier;
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @param index the 0-based index of the gorInfo qualifier to interrogate.<br>
	 *              index=0 is the first (and usually "only") qualifier.
	 * <p>
	 * @return the int Constant that represents the type of qualifier category we have.<br>
	 *         Ex: QUALIFIER_INDEX_ID  or  QUALIFIER_CAPTION_ID
	 **/
	public int getQualifierCategoryID(int index) {
		if (getQualifierRecognition() == null) return QUALIFIER_UNKNOWN_ID;		
		GORInfo info;		
		int id = 0;
		try{ info = gorInfos[index];
			 id = info.itemID;			
		}catch(Exception e){Log.info("getQualifierCategoryID", e);}
		return id;
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * @param index the 0-based index of the gorInfo qualifier to interrogate.<br>
	 *              index=0 is the first (and usually "only") qualifier.
	 * <p>
	 * @return the qualifierValue portion of the indexed qualifier string.<br>
	 *         Ex: Of "Caption=MyApp", this returns "MyApp".
	 **/
	public String getQualifierValue(int index) {
		if (getQualifierRecognition() == null) return null;		
		GORInfo info;		
		String qualifier = null;
		try{ info = gorInfos[index];
			 qualifier = info.itemValue;			
		}catch(Exception e){Log.info("getQualifierValue", e);}
		return qualifier;
	}
	
	
	/** 
	 * Convenience routine to convert our single Caption char ? and * wildcard characters 
	 * to regex equivalent wildcards.
	 * 
	 * @param expression Generally a qualifierValue holding Caption information.
	 * 
	 * @return String expression with correct regex wildcard pattern (if appropriate).
	 **/
	public String makeRegexReadyWildcards(String expression)
	{
		if (expression == null) return expression;
		
		// convert any caption value with wildcards to regex
		// otherwise, use as-is
		
		if (expression.startsWith(DEFAULT_OPEN_CAPTION_EXPRESSION)){
			
			//find closeCaptionExpression
			int exclose = expression.indexOf(DEFAULT_CLOSE_CAPTION_EXPRESSION, 1);

			// no closing brace, take all remaining string chars (this is really invalid)
			if (exclose < 1) {
				expression = StringUtils.convertWildcardsToRegularExpression(expression.substring(1));
			}
			// empty braces may mean any caption (this is really invalid)
			else if (exclose < 2) {
				expression = DEFAULT_MATCH_ANY;
			}
			// seems OK, parse into regex
			else{
				expression = StringUtils.convertWildcardsToRegularExpression(expression.substring(1, exclose));
			}
		}
		return expression;
	}
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Convenience routine to check if the object matches the Class or Type information stored 
	 * in this GuiObjectRecognition instance *without* specifying what the classname or type is 
	 * as required by the primary, tool-dependent isMatchingClass routine.<br>
	 * Simply calls the subclass isMatchingClass routine with null as the classname parameter.
	 * 
	 * @param theObject to evaluate against our stored recognition information.
	 * 
	 * @return true if theObject is considered a match to our stored class information.
	 **/
	public boolean isMatchingClass(Object theObject){ return isMatchingClass(theObject, null); }

	/**
	 * Used internally, or by direct subclasses.<br>
	 * Subclasses using proxies must override with a tool-dependent mechanism to provide 
	 * the requested information.  The information is used to determine if a particular 
	 * object is a match for our stored recognition information.  This implementation 
	 * assumes the Java object of interest is provided.  Implementations using caches 
	 * and keys will need to override this method. 
	 * 
	 * @param theObject--sometimes a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String[] values of the requested object information. 
	 *  A 0-length array for Object.class or null objects.
	 **/
	public String[] getObjectSuperClassNames(Object theObject){
		if (theObject == null) return new String[0];
		String[] rc = new String[0];
		Vector classes = new Vector();
		Class aclass = theObject.getClass();
		do{
			classes.add(aclass.getName());
			aclass = aclass.getSuperclass();
		}while(aclass != null);
		rc = new String[classes.size()];
		for(int vi=classes.size()-1, si=0; si < classes.size(); vi--, si++)
		    rc[si] = (String) classes.get(vi);
		return rc;
	}
		
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Convenience routine to check if the object matches the Caption information stored 
	 * in this GuiObjectRecognition instance *without* specifying what the is 
	 * as required by the primary, tool-dependent isMatchingCaption routine.<br>
	 * Simply calls the subclass isMatchingCaption routine with null as the caption parameter.
	 * 
	 * @param theObject to evaluate against our stored recognition information.
	 * 
	 * @return true if theObject is considered a match to any stored caption information.
	 **/
	public boolean isMatchingCaption(Object theObject){ return isMatchingCaption(theObject, null); }


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Extracts the Value of the qualifier with the matching qualifier type ID.
	 * Example, return the value of the Caption qualifier if given the QUALIFIER_CAPTION_ID 
	 * as the provided parameter.
	 * 
	 * @param qualifierID constant for the type of qualifier from which we want the value.<br>
	 *        Ex: QUALIFIER_CAPTION_ID, QUALIFIER_INDEX_ID, QUALIFIER_NAME_ID, etc..
	 * <p>
	 * @return the qualifierValue portion of the matching gorInfo qualifier.<br>
	 *         null if the requested qualifier type is not present anywhere in the recognition 
	 *         string.<br>
	 *         Ex: Of "Caption=MyApp", this returns "MyApp".
	 **/
	public String getQualifierIDValue(int qualifierID) {
		
		int qualCount = getQualifierCount();
		if (qualCount == 0) return null;
		
		GORInfo info;		
		String qualifier = null;
		int id = QUALIFIER_UNKNOWN_ID;
		
		for (int i = 0; (i < qualCount)&&(qualifier == null); i++){
			try{ id = getQualifierCategoryID(i);
				 if (id == qualifierID) qualifier = getQualifierValue(i);
			}catch(Exception e){Log.info("getQualifierIDValue", e);}
		}
		return qualifier;
	}
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * First checks to see if the provided object matches class information for our stored 
	 * recognition method.  If it does, it proceeds to check against all the stored gorInfo 
	 * qualifiers too.
	 * 
	 * @param theObject to evaluate against our stored recognition information.
	 * 
	 * @param classindex to use when comparing Index= class qualifiers.  This will either be 
	 *        the index of the Class instance encountered (ex: javax.swing.JPanel), or the 
	 *        index of the Class Type encountered (ex: JavaPanel).  The correct index type
	 *        will have been determined and forwarded by the calling routine (usually a
	 *        GuiChildIterator).
     * @param matchName, String [] , if not null, then return in matchName[0] the matching name,
     *        but only when the name is used to match with.
	 * 
	 * @return true if both the Class information and all gorInfo qualifiers are satisfied 
	 *         by theObject.
	 * 
	 * @throws SAFSObjectRecognitionException passed along from called routines like 
	 *         isMatchingQualifiers.
	 * 
	 * @author Bob Lawler (Bob Lawler), 08.29.2006 - Updated to first try getObjectAccessibleName() 
     *         and, if necessary, try getObjectName() when saving the Object's name.  This preserves
     *         backwards compatability when retrieving Object name.
	 **/
	public boolean isMatchingObject(Object theObject, int classindex, int absindex,
                                        String [] matchName)
	                                throws SAFSObjectRecognitionException {
		boolean classMatch = isMatchingClass(theObject);
		if(classMatch) {
			Log.debug("GOR: Matched object class...checking qualifiers...");
			classMatch = isMatchingQualifiers(theObject, classindex, absindex);
            if (matchName != null && matchName.length>0) {
            	// special case when we are searching for names
                        	
               	// first try to retrieve Object's accessible name...
                matchName[0] = getObjectAccessibleName(theObject);
                Log.debug("GOR: getObjectAccessibleName(): <" + matchName[0] + ">.");
                
                if (matchName[0].equals("")) {
                	// no accessible name, so try Object's name property(ies)...
                	matchName[0]= getObjectName(theObject);
                	Log.debug("GOR: getObjectName(): <" + matchName[0] + ">.");
                } 
            }
		}
		return classMatch;
	}


	/** 
	 * Used internally, or by direct subclasses only.<br>
	 * Called from ChildIterator.  Usually just returns theObject that was provided.  
	 * In the case of objects with subitems, like subitems referenced with "Path=" qualifiers, 
	 * this routine will actually locate the subitem via appropriate calls in the subclass 
	 * implementation and return those instead.
	 * 
	 * @param theObject to evaluate with stored recognition information checking for the need 
	 *        to locate a different (sub)item.
	 * 
	 * @return Object -- Usually just returns theObject as provided.  May return a subitem or 
	 *         other associated Object if that is what is specified in the recognition string.
	 **/
	public Object getMatchingObject (Object theObject){ 

		String thePath = getQualifierIDValue(QUALIFIER_PATH_ID);
		if(thePath == null) return theObject;
		
		Object pathMatch = getMatchingPathObject(theObject, thePath);
		if (pathMatch == null) pathMatch = theObject;
		return pathMatch; 
	}


	/**
	 * Used internally, called by isMatchingObject.<br>
	 * Attempts to verify that every gorInfo qualifier from the recogntion string stored herein is 
	 * satisfied by theObject.
	 * 
	 * @param theObject to evaluate against our stored recognition information.
	 * 
	 * @param classindex to use when comparing Index= class qualifiers.  This will either be 
	 *        the index of the Class instance encountered (ex: javax.swing.JPanel), or the 
	 *        index of the Class Type encountered (ex: JavaPanel).  The correct index type
	 *        will have been determined and forwarded by the calling routine (usually a
	 *        GuiChildIterator).
	 * 
	 * @return true if all gorInfo qualifiers are satisfied by theObject or if there 
	 *          are no qualifiers.  No qualifiers means that a matching Type= or Class= 
	 *          is always a match.  This is generally only useful when we are seeking 
	 *          ALL objects of a particular Type or Class.
	 * 
	 * @throws SAFSObjectRecognitionException when encountering unknown or unsupported 
	 *         categories or qualifiers.
	 * 
	 * @author CASEBEER   DEC 16, 2005 Added Property and PropertyContains qualifiers
	 * @author Carl Nagle JAN 31, 2006 Fixed isMatchingName and added support for NameContains qualifier
	 **/
	protected boolean isMatchingQualifiers(Object theObject, int classindex, int absindex) 
	                                    throws SAFSObjectRecognitionException {


		int categoryID     = getClassCategoryID();

		// force extraction of all relevant qualifier information
		int qualifierCount = getQualifierCount();
		if (qualifierCount == 0) return true;

		boolean classMatch = true;	
		// loop through each qualifier (usually only 1)
		for(int i=0; (i < qualifierCount)&&(classMatch);i++){
			
			classMatch = false; // no unexpected successes
			int qualifierID = getQualifierCategoryID(i);
			String qualifierValue = getQualifierValue(i);
			int indexValue  = -1;
	
			switch (categoryID) {
		
				// we match by a Class=classname;Index=N specification
				// caption is not usually an option here,
				case CATEGORY_CLASS_ID:
				case CATEGORY_TYPE_ID:
				
					switch (qualifierID){
						
						// Index= Acts the same as ObjectIndex= or ClassIndex= UNLESS 
						// Index= is NOT the only qualifier.  Then Index=N is the 
						// count of matching all previous qualifiers, not just the 
						// classindex.
						case QUALIFIER_INDEX_ID:

							if (i > 0){
								qualifierMatches++;
								try{ indexValue = Integer.parseInt(qualifierValue);
									 if (qualifierMatches == indexValue) classMatch = true;
								}catch(NumberFormatException nf){;}
								Log.info(" GOR: QualifierMatches ="+ qualifierMatches +";SeekingIndex="+indexValue);
							}
							else{
								try{ indexValue = Integer.parseInt(qualifierValue);
									 if (classindex == indexValue) classMatch = true;
								}catch(NumberFormatException nf){;}
								Log.info(" GOR: ActualIndex ="+ classindex +";SeekingIndex="+indexValue);
							}
							break;
						// Currently matches against classindex.  
						// Maybe ObjectIndex should match against typeindex?
						case QUALIFIER_OBJECT_INDEX_ID:
						case QUALIFIER_CLASS_INDEX_ID:
						
							try{ indexValue = Integer.parseInt(qualifierValue);
								 if (classindex == indexValue) classMatch = true;
							}catch(NumberFormatException nf){;}
							Log.info(" GOR: ActualIndex ="+ classindex +";SeekingIndex="+indexValue);
							break;
	
						case QUALIFIER_ABS_INDEX_ID:
						
							try{ indexValue = Integer.parseInt(qualifierValue);
								 if (absindex == indexValue) classMatch = true;
							}catch(NumberFormatException nf){;}
							Log.info(" GOR(ABS): ActualIndex ="+ absindex +";SeekingIndex="+indexValue);
							break;
	
						case QUALIFIER_CAPTION_ID:
						case QUALIFIER_JAVACAPTION_ID:
	
							classMatch = isMatchingCaption(theObject, qualifierValue);
							break;
							
						case QUALIFIER_HTMLTITLE_ID:
							classMatch = isMatchingCaption(theObject, qualifierValue);
							if(!classMatch) classMatch = isMatchingText(theObject, qualifierValue);
							break;
	
						case QUALIFIER_CLASS_ID:
	
							classMatch = isMatchingClass(theObject, qualifierValue);
							break;
							
						case QUALIFIER_PATH_ID:
	
							classMatch = isMatchingPath(theObject, qualifierValue);
							break;
							
						case QUALIFIER_NAME_ID:
	
							classMatch = isMatchingName(theObject, qualifierValue, true);
							break;
							
						case QUALIFIER_NAME_CONTAINS_ID:
	
							classMatch = isMatchingName(theObject, qualifierValue, false);
							break;
							
						case QUALIFIER_TEXT_ID:
						case QUALIFIER_HTMLTEXT_ID:
	
							classMatch = isMatchingText(theObject, qualifierValue);								
							break;
							
						case QUALIFIER_ID_ID:
						case QUALIFIER_HTMLID_ID:
	
							classMatch = isMatchingID(theObject, qualifierValue);
							break;

                        case QUALIFIER_PROPERTY_ID:
                            classMatch = isMatchingProperty(theObject, qualifierValue,true);
                            break;

                        case QUALIFIER_PROPERTY_CONTAINS_ID:
                            classMatch = isMatchingProperty(theObject,qualifierValue,false);
                            break;

						case QUALIFIER_UNKNOWN_ID:
						default:
							
							classMatch = false;
							throw new SAFSObjectRecognitionException("Unknown or Unimplemented Recognition Qualifier: "+ getQualifierCategory(i));							
					}
					
					break;
					
				// what do we do if NOT a Class ID or Type ID?	
				default:
					
					classMatch = false; //just makin sure
					throw new SAFSObjectRecognitionException("Unknown or Unimplemented Recognition Category: "+ getClassCategory());							
				
			}// end outside SWITCH
		} // endo for loop
		
		return classMatch;
	}
	

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Determine if the provided Object matches our segment of the 
	 * recognition string.
	 * <p>
	 * If our recognition string contains a Class definition <i>Class=classname</i> 
	 * then the routine will try a case-sensitive match of the provided classname.  
	 * If that doesn't match, the routine will try to see if the actual class is a 
	 * subclass of the class specified in the recognition string.
	 * <p>
	 * If our recognition string contains a Type definition <i>Type=typename</i> 
	 * then the routine will call {@link #isMatchingType(Object, String)} 
	 * to attempt the match.
	 * <p>
	 * @param Object proxy of the actual object to be compared against our 
	 *        recognition string.
	 * 
	 * @return true if the class matches, or is a subclass of, the class specified 
	 *         in the recognition string.
	 *         false if we cannot identify an appropriate match or an error occurs.
	 */	
	public boolean isMatchingClass(Object theObject, String theClass){
          Log.debug("GOR: pathInfo:"+pathInfo+", classInfo:"+classInfo+", classCategory:"+classCategory+",classValue:"+classValue+", qualifierInfo:"+qualifierInfo+", theClass: "+theClass+", theObject: "+theObject);
		// retrieve the actual class from Object
		String parentClass = getObjectClassName(theObject);
		Log.debug("GOR: isMatchingClass : parentClass = "+ parentClass);
		
		int category;
		String soughtClass = theClass;
		
		if (soughtClass == null){
			category = getClassCategoryID();
			soughtClass = getClassValue();// can be "CurrentWindow"		
		// else we assume a Class= Qualifier
		}else{			
			category = CATEGORY_CLASS_ID;
		}
		Log.debug("GOR: isMatchingClass: category = "+category);
		Log.info(" GOR: isMatchingClass: soughtClass = "+soughtClass);
		// exit if class or value is invalid
		if (( category == CATEGORY_UNKNOWN_ID)     ||
		    (! (soughtClass instanceof Object))) return false;
		
		// category now holds the type categoryID "Class", "Type", etc...
		// soughtClass now has the classname or class type		
		boolean classMatch = false;
		
		switch (category){

			// evaluate the category to determine how to match the classes
			case CATEGORY_CLASS_ID:						
				// match the two class name by RegularExpression OR by superclass (isAssignableTo?)		
				// if matched then break
				//begin S0543037
				//We will use the method StringUtils.matchRegex to match the soughtClass and the object's classname
				//Example: 	RS is Class={WinDemo*};Name=Form1, so soughtClass is {WinDemo*}. If we have
				//			an object whose class name is WinDemo.Form1, then this will be considered as
				//			class matched.
				//end S0543037
				String expression = makeRegexReadyWildcards(soughtClass);
                Log.debug(", expression:"+expression);

//				if (parentClass.equals(soughtClass)){
				try {
					if (StringUtils.matchRegex(expression, parentClass)){
						classMatch = true;
						Log.debug("GOR: isMatchingClass: parentClass.equals(soughtClass) = "+ classMatch);
						break;
					}
				} catch (SAFSException e) {
					Log.debug("Exception when matching Regex "+expression+" and "+parentClass+" "+e.getMessage());
				}
				//if not, check for assignable or instanceof match
				//if not, then this one is not a match
				/* classMatch = isMatchingType(theObject, soughtClass); */
				classMatch = isMatchingSubClass(theObject, soughtClass, parentClass);
				if(!classMatch){
					//last ditch effort to match stripped package names (Java)
					try{ classMatch = parentClass.endsWith("."+ soughtClass);}
					catch(Exception np){;}
				}
				Log.debug("GOR: isMatchingClass: isMatchingType = "+ classMatch);
				break;
				
			// match the two according to the class type
			case CATEGORY_TYPE_ID:				
				classMatch = isMatchingSubClass(theObject, soughtClass, parentClass);
                Log.info(" GOR: isMatchingClass: classMatch = "+ classMatch);
				break;
			case CATEGORY_CURRENTWINDOW_ID:				
				classMatch = isMatchingSubClass(theObject, soughtClass, parentClass);
                Log.info(" GOR: isMatchingClass: CurrentWindow classMatch = "+ classMatch);
				break;
		}		
		return classMatch;		
	}


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Calls the subclass's getObjectCaption function.  This is generally only valid for 
	 * top level Window types of objects.
	 * 
	 * @param theObject to evaluate 
	 * 
	 * @param theCaption to match.  If null, we will attempt to retrieve the stored recognition 
	 *        information in qualifiers for CAPTION or JAVACAPTION.
	 * 
	 * @return true if the retrieved value from getObjectCaption matches theID exactly, or matches 
	 * an appropriate SAFS wildcard expression like {SomeSubString*}.
	 **/
	public boolean isMatchingCaption (Object theObject, String theCaption){
                Log.debug("isMatchingCaption: pathInfo:"+pathInfo+", classInfo:"+classInfo+", classCategory:"+classCategory+",classValue:"+classValue+", qualifierInfo:"+qualifierInfo);
                Log.debug(", theCaption:"+theCaption);
		
		if(theCaption == null) {
			theCaption = getQualifierIDValue(QUALIFIER_CAPTION_ID);		
		    Log.debug(", theCaption:"+theCaption);
		}
		if(theCaption == null) {
			theCaption = getQualifierIDValue(QUALIFIER_JAVACAPTION_ID);		
            Log.debug(", theCaption:"+theCaption);
		}
		if(theCaption == null) return false;		
		
		String expression = makeRegexReadyWildcards(theCaption);
                Log.debug(", expression:"+expression);

		String value = getObjectCaption(theObject);
        Log.debug(", value:"+value);
        if(value==null||value.length()==0) {
            Log.debug(", retrieved caption is null or empty...");
        	return false;
        }
        try {
          boolean result = false;
          // if no appropriate SAFS wildcarding then try to match exactly
          if(theCaption.equals(expression)){
        	  result = theCaption.equals(value);
          }else{
        	  result = StringUtils.matchRegex(expression, value);
          }
          Log.debug(", result:"+result);
          return result;
        } catch (SAFSException se) {
          Log.error(getClass().getName()+".isMatchingCaption: "+se.getMessage());
          return false;
        }
	}		

	/**
	 * Used to compare 2 text values.  Normally this is required to be an exact match 
	 * using .equals().  However, in the case of some text we might want to ignore 
	 * various differences.
	 * <p>
	 * The routine currently handles:<br>
	 *  -- text benchmarks that might have originally been captured and truncated by Rational Robot<br>
	 *  -- text that might contain mnemonic or hotkey ampersands (&)<br>
	 *  -- text that might contain non-breaking spaces that can be ignored<br>
	 *  -- retrieved object text needing trim()med whitespace<br>
	 * 
	 * @param theText - non-null text to match
	 * @param objText - non-null retrieved text to compare
	 * @return
	 * @see #_convert_NBSP_Strings
	 */
	protected boolean compareText(String theText, String objText){
		
		boolean matched = theText.equals(objText);
		if(matched) return true;
		
		String modObjText = null;
		String modTheText = null;

		// handle truncated text recognition strings (Robot around 160 chars)
		if((objText.length()>150)&&(theText.length()>150)){
			try{
		        Log.info("text comparison attempting to ignore truncated Robot benchmarks...");
				modObjText=objText.substring(0,theText.length());
				matched = theText.equals(modObjText);
			}catch(IndexOutOfBoundsException x){
				// not a robot-like truncation issue
			}
		}
		if(matched) return true;

		// handle cases where mnemonic '&' might be present, but should not be
		// the length of the two strings would not be the same, but that would be OK
		// Example: trying to match &File == File in a menuitem.  The ampersand is not 
		// visible but serves to underline the 'F' hotkey or shortcut.
		if((objText.indexOf('&')> -1)&&(theText.indexOf('&')< 0)){
	        Log.info("text comparison attempting to ignore mnemonics characters...");
			modObjText = objText.replaceAll("&", "");
			//objText = StringUtilities.findAndReplace(objText, "&", "");
			matched = theText.equals(modObjText);
		}
		if(matched) return true;

		if(modObjText == null) modObjText = objText;
		
		// handle cases where NBSP might be present, but can be ignored
		if(_convert_NBSP_Strings){		
	        Log.info("text comparison attempting to ignore non-breaking spaces...");
			modObjText = modObjText.replaceAll(NBSP_STRING, " ");
			modTheText = theText.replaceAll(NBSP_STRING, " ");
			matched = modTheText.equals(modObjText);
		}
		if(matched) return true;		

		if(modTheText == null) modTheText = theText;		
		if(modObjText.length()!= modTheText.length()) modObjText = modObjText.trim();	
		return modTheText.equals(modObjText);
	}
	
        /**
         * Used internally, or by direct subclasses only.<br>
         * calls the subclass's getObjectProperty function with
         * correct information (string parse occurs before call)
         * 
         * @param theObject to evaluate
         *
         * @param qualifierValue used to determine property test/value
         *
         * @param blnExact true will run exact match; false will wildcard: *theValue*
         *
         * @return true if the retrieved value from getObjectProperty matches the
         * Property value.
         **/
        public boolean isMatchingProperty(Object theObject, 
                                          String qualifierValue, 
                                          boolean blnExact){  
            boolean rtnMatch=false;
            if(qualifierValue == null) return false;            
            /**
             * Parsing out the Property to check and the value to match
             **/
            int locationSeperator = 
                qualifierValue.indexOf(DEFAULT_PROPERTY_QUALIFIER_SEPARATOR);
            String theProperty = qualifierValue.substring(0,locationSeperator);
            String theValue = qualifierValue.substring(locationSeperator+1);
            /**
             *Check to see if there was a property and value to check
             *if not fail the matching process
             **/
            if(theProperty == null || theValue == null) return false;
            
            Log.info("GOR: seeking value of Property '"+ theProperty +"'");
            String matchValue = getObjectProperty(theObject,theProperty);
            if(matchValue==null) return false;
            if(!blnExact){   
                theValue="*" + theValue + "*";
                try{
                    String expression = StringUtils.convertWildcardsToRegularExpression(theValue);
                    Log.info(" ... expression:" + expression);
                    Log.info(" ... value: " + matchValue);
                    rtnMatch = StringUtils.matchRegex(expression,matchValue);                    
                }catch(SAFSException se){
                    Log.info(".....................................se: " + se);
                }
            }else{
                Log.info(" ... value: " + matchValue);
                rtnMatch = compareText(theValue, matchValue);
            }
            Log.info(" ... match: " + rtnMatch);
            return rtnMatch;
        }

	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Calls the subclass's getObjectName function.
	 * 
	 * @param theObject to evaluate 
	 * 
	 * @param theName to match
	 * 
     * @param blnExact true will run exact match; false will wildcard: *theName*
     *
	 * @return true if the retrieved value from getObjectAccessibleName OR getObjectName matches theName.
	 * 
	 * @author Bob Lawler (Bob Lawler), 08.29.2006 - Updated to first call getObjectAccessibleName() 
	 *         for potential match with Object's accessible name, then if necessary try to match
	 *         with getObjectName() for potential match with Objects name property.  This allows 
	 *         matches for recognition strings that include either in identifying the object.    
	 * 
	 **/
	public boolean isMatchingName     (Object theObject, String theName, boolean blnExact){ 
		if(theName == null) return false;
		boolean match = false;
		
		// first try Object's accessible name
		String matchName = getObjectAccessibleName(theObject);
		Log.info("isMatchingName: getObjectAccessibleName(): theName: "+theName+", matchName:"+matchName);
		if (! blnExact) {
            theName="*" + theName + "*";
            try {
	            String expression = StringUtils.convertWildcardsToRegularExpression(theName);
	            Log.info(" ... expression:"+expression);
	            Log.info(" ... value:"+matchName);
	            match = StringUtils.matchRegex(expression, matchName);
            } catch (SAFSException se) {
                Log.info(" .........................................se: "+se);
            }
        }
        else{
			match = theName.equals(matchName);
        }
		
		// if no match with accessible name, try with Object properties...
		if (! match) {
	        matchName = getObjectName(theObject);
	        Log.info("isMatchingName: getObjectName(): theName: "+theName+", matchName:"+matchName);
	        if (! blnExact) {
	            theName="*" + theName + "*";
	            try {
		            String expression = StringUtils.convertWildcardsToRegularExpression(theName);
		            Log.info(" ... expression:"+expression);
		            Log.info(" ... value:"+matchName);
		            match = StringUtils.matchRegex(expression, matchName);
	            } catch (SAFSException se) {
	                Log.info(" .........................................se: "+se);
	            }
	        }
	        else{
				match = theName.equals(matchName);
	        }
		}
        
        Log.info(" ... match: "+match);
        return match;
	}


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Calls the subclass's getObjectLevel function.
	 * 
	 * @param theObject to evaluate 
	 * 
	 * @param theLevel to match
	 * 
	 * @return true if the retrieved value from getObjectLevel matches theLevel exactly.
	 **/
	public boolean isMatchingLevel    (Object theObject, int theLevel){
		int level = getObjectLevel(theObject);
		return (theLevel == level);
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Calls the subclass's getObjectText function.
	 * Uses the compareText() function to handle certain ignorable cases 
	 * of missing or mismatched text from UI-specific elements.
	 * 
	 * @param theObject to evaluate 
	 * 
	 * @param theText to match
	 * 
	 * @return true if the retrieved value from getObjectText matches theText "exactly".
	 * @see #compareText(String, String)
	 **/
	public boolean isMatchingText	   (Object theObject, String theText){
		if (theText == null) return false;
		String objText = getObjectText(theObject);
        Log.info("isMatchingText: matchText: "+theText+", objText:"+objText);
        return compareText(theText, objText);
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Calls the subclass's getObjectID function.
	 * 
	 * @param theObject to evaluate 
	 * 
	 * @param theID to match
	 * 
	 * @return true if the retrieved value from getObjectID matches theID exactly.
	 **/
	public boolean isMatchingID	   (Object theObject, String theID){
		if (theID == null) return false;
		return theID.equals(getObjectID(theObject));
	}
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Perform whatever tests we want on the found Object to decide if this is 
	 * our desired final match.
	 * <p>
	 * Currently, we test that the object isShowing if the object is found by Index.  
	 * This is done via a call to the subclass's isObjectShowing function.  
	 * This check is required because different object in hidden panels can have the same 
	 * Index as those that are showing.
	 * <p>
	 * <b>We will have to resolve cases where there are multiple potential matches and 
	 * none of them are showing.  Especially when doing things like interrogating the 
	 * property values of temporarily hidden (not showing) components.</b>
	 * <p>
	 * @param theObject -- the Object to evaluate for a final match.
	 * <p>
	 * @return true if the algorithm determines this Object satisfies our search.
	 **/
	public boolean isFinalMatch(Object theObject) {
		
		// we might get 0 qualifiers. at this stage, 
		// that might mean an exact CLASS match
		int qualCount = getQualifierCount();
		
		// no qualifiers (never happen?) should theoretically ALWAYS be a match		
		boolean hasFinalMatch = true;
		
		for(int i = 0; (i <	qualCount)&&(hasFinalMatch); i++){
			
			// if we have qualifier, each must match as true
			hasFinalMatch = false;
			
			switch(getQualifierCategoryID(i)){
	
				case QUALIFIER_CAPTION_ID:
				case QUALIFIER_JAVACAPTION_ID:
				case QUALIFIER_NAME_ID:
				case QUALIFIER_NAME_CONTAINS_ID:
				case QUALIFIER_TEXT_ID:
				case QUALIFIER_HTMLTEXT_ID:
				case QUALIFIER_PATH_ID:
	
					hasFinalMatch = true;
					break;
	
				default:
	
					hasFinalMatch = isObjectShowing(theObject);
					break;								
			}
		}
		return hasFinalMatch;		
	}


	/**
	 * Used internally, or by direct subclasses only.<br>
	 **/
	protected class GORInfo {
		
		/** the qualifier piece of recognition stored herein. Ex: "Index=1" **/
		public String itemInfo;
		/** the qualifier category substring. Ex: "Index" (for "Index=1") **/
		public String itemName;
		/** the qualifier category ID for the substring. Ex: QUALIFIER_INDEX_ID (for "Index=1") **/
		public int    itemID    = CATEGORY_UNKNOWN_ID;
		/** the qualifier value substring of the stored recognition. Ex: "1" (for "Index=1") **/
		public String itemValue;		

		/**
		 * Default simple constructor.
		 **/
		public GORInfo(){;}
		
		/**
		 * @param info -- the qualifier piece of recognition stored herein. Ex: "Index=1"
		 * 
		 * @param name -- the qualifier category substring. Ex: "Index" (for "Index=1")
		 * 
		 * @param id   -- the qualifier category ID for the substring. Ex: QUALIFIER_INDEX_ID (for "Index=1")
		 * 
		 * @param value -- the qualifier value substring of the stored recognition. Ex: "1" (for "Index=1")
		 **/
		public GORInfo(String info, String name, int id, String value){
			itemInfo  = info;
			itemName  = name;
			itemID    = id;
			itemValue = value;
		}
	}

	/**
	 * @return Overrides the Object.toString() method to return our recognition string.
	 **/
	public String toString(){ return pathInfo;}
	
	/**
	 * Allow individual engines (ie. RJ) to override this method with their own implementation.
	 * @param theObject
	 * @return String
	 * @author Bob Lawler (Bob Lawler), 08.29.2006
	 */
	public String  getObjectAccessibleName(Object theObject) {
		return "";
	}
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectName      (Object theObject);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectClassName (Object theObject);
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectCaption   (Object theObject);
	
	/**
	 * Used internally or by subclasses only.
	 * The object domain is the environment, OS, or programming language origins of 
	 * theObject.  Example: Java, Html, Win, Net, Visual Basic, etc.
	 * @param theObject -- usually a tool-dependent proxy for the object to be evaluated.
	 * @return String value of "Java" , "Net", "Html", Win", etc..
	 */
	public abstract String getObjectDomain(Object theObject);
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Level is generally considered to be the Z-Order information of the object--where in 
	 * the Z-Order the object (usually a window) resides.
	 * <p>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return int value of the requested object information.
	 **/
	public abstract  int    getObjectLevel     (Object theObject);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectText      (Object theObject);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectID        (Object theObject);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String[] of all known property names.  Property names are assumed to 
	 * be case-sensitive.
	 **/
	public abstract String[]  getObjectPropertyNames  (Object theObject);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @param theProperty name of the property value to be evaluated in the object.
	 * Property names are assumed to be case-sensitive.
	 * 
	 * @return String value of the requested object information.
	 **/
	public abstract String  getObjectProperty  (Object theObject, String theProperty);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * Retrieves the resulting object identified with the Path information applied to theObject. 
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return Object child sub-object found relative to theObject
	 **/
	public abstract Object  getMatchingPathObject (Object theObject, String thePath);
	
	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return true if the child sub-object was found relative to theObject.
	 **/
	public abstract boolean isMatchingPath        (Object theObject, String thePath);	


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return true if theObject was found to be "showing" or "visible".
	 **/
	public abstract boolean isObjectShowing    (Object theObject);


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses are generally expected to simply forward this request on to the tool-dependent 
	 * GuiClassData.getMappedClassType (like RGuiClassData) function after casting the object to 
	 * the appropriate class for the tool implementation.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @param theClass information extracted here and forwarded for GuiClassData.isMappedClassType.
	 * 
	 * @return true if theObject is determined to satisfy Class Type information.
	 **/
	public abstract boolean isMatchingType     (Object theObject, String theClass);


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses are generally expected to simply forward this request on to the tool-dependent 
	 * GuiClassData.isMatched (like RGuiClassData) function after casting the object to 
	 * the appropriate class for the tool implementation.
	 * 
	 * Subclasses should also use this method to support the "CurrentWindow" recognition string.
	 * isMatchingSubclass should return true IF the class sought is "CurrentWindow" and the 
	 * object is determined to be the topmost active window with input (keyboard) focus.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @param theClass information extracted here and forwarded for GuiClassData.isMatched.
	 * 
	 * @param parentClass information extracted here and forwarded for GuiClassData.isMatched.
	 * 
	 * @return true if theObject is determined to satisfy the requested information.
	 **/
	public abstract boolean isMatchingSubClass (Object theObject, String theClass, String parentClass);	


	
	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses are generally expected to implement a means to determine the Nth occurrence 
	 * of the specific object class in the application's child object hierarchy.  This index 
	 * is used in future attempts to locate the correct occurrence of the child object via one or 
	 * more search algorithms. The index should be 1-Based.  That is, the first occurrence is "1".
	 * <p>
	 * Example use: Give us the Class Index that would work for a Class category recognition string like:
	 * <p>
	 * Child="Class=com.custom.class.MyTreeClass;Index=N", or<br>
	 * Child="Class=com.custom.class.MyTreeClass;ClassIndex=N"
	 * <p>
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String representation of the index or an empty string ("").  This default 
	 * implementation which should be overwritten in subclasses, if possible, is to return 
	 * an empty string (""). 
	 **/
	public String getObjectClassIndex(Object item){ return "";}
}

