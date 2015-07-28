package org.safs;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;
import java.util.StringTokenizer;

/** 
 * This class is intended to maintain critical component recognition information 
 * for each level of the application GUI hierarchy represented in a standard 
 * component recognition string.
 * <p>
 * <br>     Copyright (C) (SAS) All rights reserved.
 * <br>     General Public License: http://www.opensource.org/licenses/gpl-license.php
 * @since
 * <br>     JUL 15, 2005 (Carl Nagle) Added JavaClass support
 * <br>     APR 17, 2009 (Carl Nagle) Fixed problem in makeUniqueCacheKey
 * <br>     APR 23, 2009 (Carl Nagle) Added support for fullpath search mode(:FPSM:).
 * <br>     MAY 01, 2009 (Carl Nagle) Changes to complete FULLPATH_SEARCH_MODE implementation. 
 * <br>     MAY 11, 2009 (Carl Nagle) Support JPopups as child Panels in JFrames. 
 * <br>     JUL 20, 2009 (Carl Nagle) Added Domain= Category support.
 * <br>     SEP 04, 2009 (Junwu) Added mapped class search mode and complete MAPPEDCLASS_SEARCH_MODE(:MCSM:) implementation.
 * <br>     NOV 17, 2009 (LeiWang) Added constant "PopupWindow" to represent generic top level popup window,use it in method isSeekingPopupMenu() and isTopLevelPopupWindowType().
 * <br>     DEC 09, 2009 (Carl Nagle) Fixed case-sensitive comparison error in isSeekingPopupMenu. Added "PopupMenu" support in isTopLevelPopupWindowType().
 * <br>     APR 23, 2010 (Carl Nagle) Mode convertToKeys handling case of individual null items in arrays.
 * <br>     MAR 09,2011 (DharmeshPatel) Added RFT Find Search Mode (:RFSM:) implementation.
 */
public abstract class GuiObjectVector {

	/** ";\;" -- separates parent/child relationships in recognition strings. */
	static public final String DEFAULT_CHILD_SEPARATOR  = ";\\;";	

	/** "\;" -- Reference to the Desktop.
	 * This is only valid when used as the first leading text of a recognition string. 
	 **/
	static public final String DESKTOP_REFERENCE  = "\\;";	

	/** ".\;" -- Reference to the current active window. 
	 * This is only valid when used as the first leading text of a recognition string. 
	 **/
	static public final String ACTIVE_WINDOW_REFERENCE  = ".\\;";	

	/** "JavaPopupWindow" -- The "sought" object type for top level popup menu window. */
	static public final String OBJECTTYPE_POPUPWINDOW  = "JavaPopupWindow";

	/** "POPUPWINDOW" -- The upper-case generic "sought" object type for top level popup menu window. */
	static public final String GENERIC_OBJECTTYPE_POPUPWINDOW  = "POPUPWINDOW";	

	/** "JavaPopupContainer" -- A JFrame can contain an active JavaPopupMenu. */
	static public final String OBJECTTYPE_POPUPCONTAINER  = "JavaPopupContainer";	

	/** "JavaPopupMenu" -- The "sought" object type for java popup menus. */
	static public final String OBJECTTYPE_POPUPMENU    = "JavaPopupMenu";	

	static public final String DOTNET_POPUPMENU    = "DotNetPopupMenu";
	static public final String WIN_POPUPMENU    = "WinPopupMenu";
	
	/** "POPUPMENU" -- The upper-case "simple" object type for popup menus. */
	static public final String SIMPLETYPE_POPUPMENU    = "POPUPMENU";	

	public static final int MODE_ENGINE_PROCESSING   = 0; //The default
	public static final int MODE_EXTERNAL_PROCESSING = 1; 
	public static final int INITIAL_CACHE_SIZE       = 400;
    
	/**================================================================================= 
	 * Some prefixes represent search modes
	 * 1):FPSM:, fullpath search mode
	 * 2):MCSM: mappedclass search mode.
	 * 3):RFSM: RFT find search mode.
	 * 4):PASM: property all search mode.
	 * The prefixes in an R-String have no priority, can work together or work alone by itself.
	 * When they work together, it may be defined like 
	 *          ":FPSM::MCSM:Type=Window;Caption={caption}" or
	 * 			":MCSM::FPSM:Type=Window;Caption={caption}"
	 * ==================================================================================
	 */
	
	/** 
	 * <pre>
	 * ":FPSM:" 
	 * case-insensitive prefix on entry path vector indicating recognition string is a 
	 * fullpath search mode recognition string.
	 * </pre>
	 */
	public static final String FULLPATH_SEARCH_MODE_PREFIX = ":FPSM:";
	
	/**
	 * <pre>
	 * ":MCSM:"
	 * case-insensitive prefix on entry path vector indicating recognition string is a 
	 * mapped class search mode recognition string. 
	 * In mapped-class-search-mode, only mapped classes shall be considered; those who do not hold 
	 * interesting data or controls, shall be ignored. In this way, it saves lots of time to retrieval an application tree.
	 * </pre>
	 **/
	public static final String MAPPEDCLASS_SEARCH_MODE_PREFIX = ":MCSM:";
	
	/**
	 * <pre>
	 * ":RFSM:"
	 * case-insensitive prefix on entry path vector indicating recognition string is a 
	 * RFT find search mode recognition string.
	 * In Rft-find-search-mode, RFT find API used to retrieve window and component objects.
	 * </pre>
	 */
	public static final String RFT_FIND_SEARCH_MODE_PREFIX= ":RFSM:";
	
	/**
	 * <pre>
	 * ":PASM:"
	 * With this prefix, for RS like "prop1=xxx;prop2=xxx;prop3=xxx", "prop1", "prop2" and "prop3" will
	 * all be considered as properties; Without this prefix, they will be considered as SAFS qualifier.
	 * </pre>
	 */
	public static final String PROPERTYALL_SEARCH_MODE_PREFIX = ":PASM:";
	
	/** List of all R-String prefixes supported by SAFS */
	public static final String[] recognitionStringPrefixes = {FULLPATH_SEARCH_MODE_PREFIX,
		                                                      MAPPEDCLASS_SEARCH_MODE_PREFIX,
		                                                      RFT_FIND_SEARCH_MODE_PREFIX,
		                                                      PROPERTYALL_SEARCH_MODE_PREFIX};
	
	/** Stores the defined parent/child separator. */	
	protected String childSeparator  = DEFAULT_CHILD_SEPARATOR;

	/** Holds full hierarchy of GuiObjectRecognition objects. */
	protected Vector path = new Vector(4,1);

	/** Stores the full recognition string provided to the constructor. 
	 *  It is a pure recognition string, from which any prefix defined in recognitionStringPrefixes will be removed. 
	 * */
	protected String pathVector = null;
	
	/** True for FullPath Search Mode. False for default Classic Search Mode. */
	protected boolean isFullPathSearchString = false;
	
	/** True for MappedClass Search Mode. False for default other Search Mode.*/
	protected boolean isMappedClassSearchString = false;
	
	/** True for RFT Find Search Mode. False for default other Search Mode.*/
	protected boolean isRftFindSearchString = false;
	
	/** True for Property All Search Mode. False for default other Search Mode.*/
	protected boolean propertyAllSearchMode = false;
	
	protected Hashtable cache = null;
	
	/** 
	 * Stores the name of the window component involved in our search. 
	 * windowName and childName may be indentical if the window is the target.*/
	protected String windowName = null;

	/** 
	 * Stores the name of the child component involved in our search. 
	 * windowName and childName may be indentical if the window is the target.*/
	protected String childName  = null;

	protected int process_mode = MODE_ENGINE_PROCESSING;

	/**
	 * Alternate minimal constructor.
	 * All subclasses should call this constructor with
	 * <p><ul>
	 * super();
	 * <p>
	 * Instances created with this minimal constructor MUST still have 
	 * windowName, childName, and pathVector set prior to calling initGuiObjectRecognition().
	 * 
	 * @see #setWindowName(String)
	 * @see #setChildName(String)
	 * @see #setPathVector(String) 
	 * @see #initGuiObjectRecognition()
	 */
	public GuiObjectVector(){
		super();
	}

	/**
	 * Primary constructor for the superclass.
	 * All subclasses should call this constructor with
	 * <p><ul>
	 * super(window, child, pathString);
	 * <p>
	 * ....subclass initialization completes....
	 * <p>
	 * initGuiObjectRecognition();</ul>
	 * <p>
	 * if pathString starts with :FPSM: then path is a fullpath search mode recognition string 
	 * and we will set the search mode flag accordingly.
	 */
	public GuiObjectVector(String window, String child, String pathString){
		super();
		windowName = new String(window);
		childName  = new String(child);
		setPathVector(pathString);
	}

	/**
	 * Set to true to specify recognition string is a full path search mode recognition string.
	 * @param fullpathmode true if recognition string is for full path search mode.
	 * false is for default Classic search mode recognition strings.
	 */
	public void setFullPathSearchMode(boolean fullpathmode){
		isFullPathSearchString = fullpathmode;
	}
	
	/**
	 * @return boolean true if the initialized path vector represented a full path search mode 
	 * recognition string.  False is for default Classic search mode recognition strings.
	 */
	public boolean isFullPathSearchMode(){
		return isFullPathSearchString;
	}

	/**
	 * Set to true to specify recognition string is a mapped class search mode recognition string.
	 * @param mappedclassmode true if recognition string is for mapped class search mode.
	 * false is for not mapped-class-search-mode recognition strings.
	 */
	public void setMappedClassSearchMode(boolean mappedclassmode){
		isMappedClassSearchString = mappedclassmode;
	}
	
	/**
	 * @return boolean true if the initialized path vector represented a mapped class search mode 
	 * recognition string.  False is for not mapped-class-search-mode recognition strings.
	 */
	public boolean isMappedClassSearchMode(){
		return isMappedClassSearchString;
	}		
	
	/**
	 * @return boolean true if the initialized path vector represented a RFT find search mode 
	 * recognition string.  False is for not RFT-find-search-mode recognition strings.
	 */
	public boolean isRftFindSearchMode() {
		return isRftFindSearchString;
	}

	/**
	 * Set to true to specify recognition string is a RFT find search mode recognition string.
	 * @param rftFindSearchMode true if recognition string is for RFT find search mode.
	 * false is for not RFT-find-search-mode recognition strings.
	 */
	public void setRftFindSearchMode(boolean rftFindSearchMode) {
		isRftFindSearchString = rftFindSearchMode;
	}
	
	/**
	 * @return boolean true if the initialized path vector represented a 'Property All' search mode 
	 * recognition string.
	 */
	public boolean isPropertyAllSearchMode(){
		return propertyAllSearchMode;
	}
	
	/**
	 * Set to true to specify recognition string is a 'Property All' search mode recognition string.
	 * @param propertyAllSearchMode booleanm, true if recognition string is for 'Property All'  search mode.
	 */
	public void setPropertyAllSearchMode(boolean propertyAllSearchMode) {
		this.propertyAllSearchMode = propertyAllSearchMode;
	}

	/**
	 * Initialize path vector storage with GuiObjectRecognition objects.
	 * This routine breaks down a standard component recogntion string into 
	 * separate parent/child elements represented by GuiObjectRecognition objects.
	 * <p>
	 * Must be called by all GuiObjectVector subclass constructors AFTER 
	 * their own constructor activities are complete.
	 * <p><ul>
	 * super(window, child, pathString);
	 * <p>
	 * ....subclass initialization completes....
	 * <p>
	 * initGuiObjectRecognition();</ul>
	 * 
	 * @author Carl Nagle Oct 02, 2007 Added support for \; and .\;
	 */	
	public void initGuiObjectRecognition(){
		int start = 0;
		int pos   = 0;
		int level = 0;
		path.clear();
		
		if (pathVector.startsWith(DESKTOP_REFERENCE)){
			Log.info("GOV: Path includes leading DESKTOP_REFERENCE");
			start = DESKTOP_REFERENCE.length();
			path.addElement(createGuiObjectRecognition(pathVector.substring(0, start ), level++));			
		}else if(pathVector.startsWith(ACTIVE_WINDOW_REFERENCE)){
			Log.info("GOV: Path includes leading ACTIVE_WINDOW_REFERENCE");
			start = ACTIVE_WINDOW_REFERENCE.length();
			path.addElement(createGuiObjectRecognition(pathVector.substring(0, start ), level++));
		}
		do{
			pos = pathVector.indexOf(childSeparator, start);
			
			if (pos > start){
			
				path.addElement(createGuiObjectRecognition(pathVector.substring(start, pos ), level++));
				start = pos + childSeparator.length();
			}
			else{
				path.addElement(createGuiObjectRecognition(pathVector.substring(start), level++));
				start = pathVector.length();
			}
			
		}while(start < pathVector.length() );
				
		path.trimToSize();
	}

	/** Stores the constructors pathString. */	
	public String getVectorString() { return pathVector; }

	/** Stores the constructors window string. */	
	public String getWindowName()   { return windowName; }

	/** Stores the constructors child string. */	
	public String getChildName ()   { return childName;  }

	/** 
	 * Returns the hierarchy depth of the pathString when available. 
	 * This is the number of GuiObjectRecognition objects stored 
	 * in our path vector. */
	public int getRecognitionDepth(){ return path.size(); }

	/** 
	 * Returns the topmost parent GuiObjectRecognition.
	 * In most cases this is the first GuiObjectRecognition stored.  However,
	 * some cases referencing the Desktop ("\;") will change this to be the 
	 * second GuiObjectRecognition stored.
	 * <p>
	 * We may also have to deal with the case where NO parent information is 
	 * stored.  Sometimes this is the case with the Object Recognition strings 
	 * provided by the user for Process Container.
	 * @author Carl Nagle Oct 02, 2007 Added support for \; and .\;
	 **/
	public GuiObjectRecognition getParentGuiObjectRecognition(){
		GuiObjectRecognition gor = (GuiObjectRecognition) path.firstElement();
		if (gor.pathInfo.equals(DESKTOP_REFERENCE)) gor = (GuiObjectRecognition) path.get(1);
		return gor;
	}
	
	/** 
	 * Returns the indexed GuiObjectRecognition stored.
	 * The parent object is at index=0 (or index=1 if DESKTOP_REFERENCE exists). 
	 * The first child is usually at index=1 (or 2).
	 * <p>
	 * An exception to this may exist when code like Process Container provides 
	 * the parent object and the Object Recognition string does NOT provide 
	 * parent path information but only child path information.
	 * */
	public GuiObjectRecognition getChildGuiObjectRecognition(int index){
		return (GuiObjectRecognition) path.elementAt(index);
	}


	/** 
	 * Returns the last GuiObjectRecognition stored--the one for final match.*/
	public GuiObjectRecognition getFinalChildGuiObjectRecognition(){
		return (GuiObjectRecognition) path.lastElement();
	}


	// called internally only.  Subclasses should not need to call this.	
	/**
	 * @param objecttype - extracted from GuiClassData.
	 */
	private boolean isTopLevelPopupWindowType(String objecttype){
		if (objecttype == null) return false;
		String mappedtype  = null;
		mappedtype  = getGuiClassData().getGenericObjectType(objecttype);
		boolean popup = (mappedtype == null);		
		if(! popup) popup = mappedtype.equalsIgnoreCase(OBJECTTYPE_POPUPMENU);
		if(! popup) popup = mappedtype.equalsIgnoreCase(DOTNET_POPUPMENU);
		if(! popup) popup = mappedtype.equalsIgnoreCase(WIN_POPUPMENU);
		//Carl Nagle 2009.12.09 I believe a PopupMenu should also be considered a TopLevelPopupWindow
		//For example, #32768 PopupMenu IS a Top Level Window
		if(! popup) popup = mappedtype.equalsIgnoreCase(SIMPLETYPE_POPUPMENU);
		
		//Use generic type "PopupWindow" instead of "JavaPopuWindow" to test if the window is a top popup window
		//if(! popup) popup = GuiClassData.classtypeContainsClassType(objecttype, OBJECTTYPE_POPUPWINDOW);
		if(! popup) popup = GuiClassData.classtypeContainsClassType(objecttype, GENERIC_OBJECTTYPE_POPUPWINDOW);
		Log.info ("GOV:isToplevelPopupType for:"+ objecttype +"/"+ mappedtype +"="+ popup);
		return popup;
	}

	// called internally only.  Subclasses should not need to call this.
	/**
	 * @param _parent - Will convert key if MODE_EXTERNAL_PROCESSING.
	 * @return the object type(s) as mapped via GuiClassData or NULL.
	 * @see GuiClassData#getMappedClassType(String, Object)
	 **/
	protected String getGuiObjectType(Object _parent){
		String objectclass = null;
		String objecttype  = null;
		GuiObjectRecognition parentInfo = getParentGuiObjectRecognition();		
		Object parent = _parent;
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			parent = getCachedItem(_parent);
		}
		objectclass = parentInfo.getObjectClassName(parent);			
		return getGuiClassData().getMappedClassType(objectclass, parent);
	}
	
	// called internally only.  Subclasses should not need to call this.
	/**
	 * @param _parent - Will convert key if MODE_EXTERNAL_PROCESSING.
	 * @return true if the _parent maps strictly to a PopupMenu Window class.
	 * This excludes the test for a JavaPopupContainer--like JFrame.
	 * We don't want just any old JFrame considered a match as a Parent object 
	 * with Type=JavaPopupMenu.
	 * @see #getGuiObjectType(Object)
	 */
	protected boolean isTopLevelPopupWindow(Object _parent){
		String objecttype  = getGuiObjectType(_parent);
		return isTopLevelPopupWindowType(objecttype);
	}

	// called internally only.  Subclasses should not need to call this.
	/**
	 * @param _parent - Will convert key if MODE_EXTERNAL_PROCESSING.
	 * @return true if the _parent maps to a PopupMenu Window OR JavaPopupContainer--
	 * like JFrame.  This is generally when searching for child objects matching 
	 * Type=JavaPopupMenu when they are contained inside a parent window not normally 
	 * considered a top-level Popup window--like JFrame.
	 * @see #getGuiObjectType(Object)
	 */
	protected boolean isTopLevelPopupContainer(Object _parent){
		String objecttype  = getGuiObjectType(_parent);
		boolean popup = isTopLevelPopupWindowType(objecttype);
		if(! popup) popup = GuiClassData.classtypeContainsClassType(objecttype, OBJECTTYPE_POPUPCONTAINER);
		return popup;
	}



	// called internally only.  Subclasses should not need to call this.
	/**
	 * Only reference recognition string and no specific object.
	 */
	protected boolean isSeekingPopupMenu(){
		GuiObjectRecognition lastInfo = getFinalChildGuiObjectRecognition();
		Log.debug("GOV.lastInfo:"+ lastInfo.toString());
		if (lastInfo == null) return false;
		String soughttype = lastInfo.getClassValue();
		Log.debug("GOV.soughttype:"+ soughttype);
		if(soughttype == null) return false;
		String simpletype = getGuiClassData().getGenericObjectType(soughttype);
		Log.debug("GOV.simpletype:"+ simpletype);
//		boolean popup = (soughttype.equalsIgnoreCase(OBJECTTYPE_POPUPMENU)||
//               		      soughttype.equalsIgnoreCase(OBJECTTYPE_POPUPWINDOW)||
//               		      soughttype.equalsIgnoreCase(DOTNET_POPUPMENU)||
//               		      soughttype.equalsIgnoreCase(WIN_POPUPMENU));
		
		//If the soughttype ends with "PopupMenu" or "PopupWindow", we will consider it as looking for a popup menu.
		//toUpperCase().endsWith is a CASE-SENSITIVE compare!
		boolean popup = (soughttype.toUpperCase().endsWith(SIMPLETYPE_POPUPMENU)||
				 		 soughttype.toUpperCase().endsWith(GENERIC_OBJECTTYPE_POPUPWINDOW));
		
        if(! popup) {
        	try{popup = simpletype.equalsIgnoreCase(SIMPLETYPE_POPUPMENU);}
        	catch(Exception any){;}
        }
		Log.info ("GOV: checking isSeekingPopupMenu for:"+soughttype +"/"+ simpletype +"="+ popup);
        return popup;
	}
	
		
	// handle: 
	//		Type=Window;Caption=...
	//		Type=JavaWindow;JavaCaption=....
	//		Class=<class>;Index=N
	//		Class=<class>;ClassIndex=N
	//		Class=<class>;ObjectIndex=N
	//		Type=Window;Class=...
	//		Type=JavaWindow;JavaClass=....
	//      Type=FlexWindow;Caption=....   OCT 09, 2008 JunwuMa. New added. Use the content of property 'automationName' as Caption
	//                                     E.g. Caption=FlexWebDemo.swf,  FlexWebDemo.swf is regarded as a unique mark to
	//                                     make different from other flex app. Flex app has no caption.
	//                                         A better way could be introduced?
	//                                 Flex got supported. No code modified.
	//                                 By calling isMatchingClass and isMatchingQualifiers with new update inside, Method isMatchingParent 
	//                                 supports Flex format like: Type=FlexWindow;Caption=...
	//                         
	
	/**
	 * @param _parent - Will convert key if MODE_EXTERNAL_PROCESSING.
	 */
	public boolean isMatchingParent(Object _parent){

		GuiObjectRecognition parentInfo = getParentGuiObjectRecognition();
		Object parent = _parent;
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			parent = getCachedItem(_parent);
		}
		Log.debug("GOV: processing parentClass: "+ parentInfo.getObjectClassName(parent));
		if(! parentInfo.isObjectShowing(parent)) return false;		
		if(! parentInfo.isMatchingClass(parent))
			return (isSeekingPopupMenu())&&(isTopLevelPopupWindow(parent));

		// match the Caption (ignore Index for topmost parents)
		// actually, cannot do this indefinetely...
		// if (parentInfo.isMatchingCaption(parent)) return true;
		try {if (parentInfo.isMatchingQualifiers(parent, 1, 1)) return true;}
		catch(SAFSObjectRecognitionException orx){;}
		return (isSeekingPopupMenu())&&(isTopLevelPopupWindow(parent));
	}

	/**
	 * Searches the test domains one-by-one in the order received from Domains.getEnabledDomains().
	 * Thus, if the order of enabled domains is {Java, Html, Win} then we will first check ALL 
	 * Java domains for the match, then all Html domains, and finally all Win domains.
	 * Find the one parent object that matches our recognition string.
	 * @return Object or null if no match found. Will be a cached key from 
	 * getParentObjects if MODE_EXTERNAL_PROCESSING.
	 */
	public Object getMatchingParentObject(){

		if (path.isEmpty()){
			Log.info("GOV: no parent path information available.");
			return null;
		}

		// locate childPath hierarchy via objects
		Object child  = null;
		Object[] parents = null;
		int parentCount = 0;
		int parentIndex = 0;
		String[] domains = null;
		try{
			GuiObjectRecognition tgor = (GuiObjectRecognition) path.elementAt(0);
			if(tgor.hasDomainInfo()){
				domains = new String[]{ tgor.domainValue };
				Log.debug ("GOV: Domains overridden by Recognition String Domain: "+ domains[0]);			
			}
		}catch(Exception x){
			Log.debug ("GOV: IGNORING Domain Recognition "+ x.getClass().getSimpleName());			
		}
		if (domains == null) domains = Domains.getEnabledDomains();
		try{
			if(domains.length==0)
				Log.debug ("GOV: Domains reports there are no enabled domains!");			
		}catch(NullPointerException x){
			Log.debug ("GOV: Domains null for getEnabledDomains!");			
		}
		for(int d=0;(d<domains.length)&&(child==null);d++){
			parents = getDomainParentObjects(domains[d]);	
			if (parents == null) {
				Log.info ("GOV: Could not get parent window objects from domain: "+domains[d]);
				continue;
			}
			parentCount = 0;
			parentIndex = 0;
			
	        Log.info("PARENTS.length: "+parents.length);
			parentCount = parents.length -1;			
			
			// we will also have to test for parent object of Index > 1
			// right now we accept the first Index or we validate on Caption
			while( (parentIndex <=  parentCount) && (child==null)){
	            Log.info("NEXTPARENT:"+parentIndex+", "+parents[parentIndex]);
				if (! isMatchingParent(parents[parentIndex])) {
					parentIndex++;
				}
				else {				
					child = parents[parentIndex];
				}
			}
		}
		return child;		
	}


	/**
	 * used by getMatchingChildObject below.
	 * @param _aparent - Will convert to object on MODE_EXTERNAL_PROCESSING
	 * @return Object - Will convert to cached key on MODE_EXTERNAL_PROCESSING
	 */
	protected Object iterateChildren(Object _aparent, List gather){
		Object aparent = _aparent;
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			aparent = getCachedItem(_aparent);
		}
        Log.info("GOV.iterateChildren: aparent: "+ aparent);

		GuiChildIterator iterator = createGuiChildIterator(aparent, this, gather);
        Log.info("GOV: iterator: "+iterator);		
        //might be null if no match found
        Object match = iterator.getMatchedGuiObject();
        if (match==null) return null;
        Object key = match;
        if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
        	key = makeUniqueCacheKey(match);
        	putCachedItem(key,match);
        }
       	return key;
	}
	
	// used by getChildMatchData below
	protected Vector iterateChildMatchData(Object _aparent, List gather){
		Object aparent = _aparent;
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			aparent = getCachedItem(_aparent);
		}
		GuiChildIterator iterator = createGuiChildIterator(aparent, this, gather);
        Log.info("GOV: iterator: "+iterator);
		// might be null if no match found
        Vector matches = iterator.getMatches();
        if (matches.size()==0) return matches;
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			MatchData match;
			Object key;
			for(int m=0;m<matches.size();m++){
				match = (MatchData)matches.elementAt(m);
				key = makeUniqueCacheKey(match.getGuiTestObject());
				matches.setElementAt(new MatchData(
										match.getGuiObjectVector(),
										match.getVectorRecognitionLevel(),
										match.getObjectRecognitionLevel(),
										key),m);
			}
		}
		return matches;		
	}
	
    /** 
     * get child test object
     * @param  aparent - Will convert from d key on MODE_EXTERNAL_PROCESSING
     * @param  gather, java.util.List
     * @return Object or null - Object will be a cached key on MODE_EXTERNAL_PROCESSING
     **/
	public Object getMatchingChildObject(Object _aparent, List gather) {

        Log.info("GOV.getMatchingChildObject: aparent: "+ _aparent);
		// currently only process Gui Objects
		Object aparent = _aparent;		
		if (aparent == null) return null;
		if (!isValidGuiObject(aparent)){
			Log.info("Gov:aparent "+ windowName +" is not a valid GUI object.");
			return null;
		}
		GuiObjectRecognition gor = getParentGuiObjectRecognition();		
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			aparent = getCachedItem(_aparent);
	        Log.info("GOV.getMatchingChildObject: cachedItem: "+ aparent);
		}
		if (! gor.isObjectShowing(aparent)) {
			Log.info("GOV:aparent "+ windowName +" may not be visible!");
			return null;
		}
		
		// ensure there are child recognition strings to evaluate
		// (unless we are dealing with a top level popup menu?)
		int maxdepth = getRecognitionDepth();
        Log.info("GOV: maxdepth: "+maxdepth +", path="+ getPathVector());

        Object result = null;
        GuiChildIterator.resetAbsoluteIndexStore(); //Carl Nagle 10-4-2007
        
        if( isSeekingPopupMenu()){
        	//check if this object is potentially a PopupWindow Container
        	//Some Java Frames and\or JFrames can contain a JPopup in a child 
        	//panel instead of an "owned" window.
			if( isTopLevelPopupContainer(aparent)){
				Log.info("GOV:get(Popup)ChildObject "+childName+" from top-level popup "+ windowName);				
				// need to provide support for maxdepth = 1
				result =  iterateChildren(aparent, gather);
			}
			//try other popup avenues and containers
			if (result==null){
				Log.info("GOV:get(Popup)ChildObject "+childName+" from non-popup "+ windowName);				

				// get the real top-level window if possible
				Object realparent = getMatchingParentObject();				
				if (realparent != null){
					if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
						realparent = getCachedItem(realparent);
					}
					if(isTopLevelPopupWindow(realparent)){
						Log.info("GOV:get(Popup)ChildObject "+childName+" for found top-level popup "+ windowName);
					}
					else{
						Log.info("GOV:get(Popup)ChildObject "+childName+" for found top-level non-popup "+ windowName);
					}
					// need to provide support for maxdepth = 1
					result =  iterateChildren(realparent, gather);						
				}
			}
        }
        // most searches use this normal process
        else if (maxdepth > 1) {
			Log.info("GOV:getMatchingChildObject maxdepth>1:"+childName+" for "+ windowName);				
        	result = iterateChildren(aparent, gather);
        }
        // (Carl Nagle) some depths are still only 1
        else{
			Log.info("GOV:getMatchingChildObject maxdepth=1:"+childName+" for "+ windowName);				
			
			// Carl Nagle Oct 03, 2007 DEBUG -- may no longer be necessary
			//path.addElement(createGuiObjectRecognition(pathVector, maxdepth));
        	result = iterateChildren(aparent, gather);
        }

        Log.info("GOV: result: "+result);
        Object _result = result;
        //if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
        //	_result = this.makeUniqueCacheKey(result);
        //	putCachedItem(_result, result);
        //}
		return _result;
	}

    /** <br><em>Purpose:</em> get ALL child MatchData
     * This is primarily intended to be used by Component Functions or other entities 
     * that need to get ALL possible components or objects of a particular recognition 
     * string.  Normally, this is just one object.  But with a recognition string that 
     * merely requests one particular type or class of object we can get all such 
     * matches.  For example, we can provide an HTMLDocument object and request to 
     * retrieve all HTMLLinks within that document.
     * 
     * @param                     aparent, TestObject
     * @param                     gather, java.util.List
     * @return                    Vector null or all GuiChildIterator MatchData
     **/
	public Vector getChildMatchData(Object _aparent, List gather) {
        Log.info("GOV.getChildMatchData: aparent: "+ _aparent);
		// currently only process Gui Objects
		Object aparent = _aparent;
		if (aparent == null) return null;
		if (!isValidGuiObject(aparent)){
			Log.info("GOV.getChildMatchData:aparent "+ windowName +" is not a valid GUI object.");
			return null;
		}
		GuiObjectRecognition gor = getParentGuiObjectRecognition();
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			aparent = getCachedItem(_aparent);
		}
		if (! gor.isObjectShowing(aparent)) {
			Log.info("GOV.getChildMatchData:aparent "+ windowName +" may not be visible!");
			return null;
		}
		
		// ensure there are child recognition strings to evaluate
		// (unless we are dealing with a top level popup menu?)
		int maxdepth = getRecognitionDepth();
        Log.info("GOV.getChildMatchData: maxdepth: "+maxdepth);

        Vector result = new Vector();
        
        if( isSeekingPopupMenu() ){
			if( isTopLevelPopupWindow(aparent)){
				Log.info("GOV.getChildMatchData:get(Popup)ChildObject "+childName+" from top-level popup "+ windowName);				
				// need to provide support for maxdepth = 1
				result =  iterateChildMatchData(aparent, gather);
			}else{
				Log.info("GOV.getChildMatchData:get(Popup)ChildObject "+childName+" from non-popup "+ windowName);				

				// get the real top-level window if possible
				Object realparent = getMatchingParentObject();

				if (realparent != null){
					if(isTopLevelPopupWindow(realparent)){					
						Log.info("GOV.getChildMatchData:get(Popup)ChildObject "+childName+" for found top-level popup "+ windowName);
					}else{
						Log.info("GOV.getChildMatchData:get(Popup)ChildObject "+childName+" for found top-level non-popup "+ windowName);
					}
					// need to provide support for maxdepth = 1
					result =  iterateChildMatchData(realparent, gather);
				}
			}
        }
        // most searches use this normal process
        else if (maxdepth > 1) {
			Log.info("GOV.getChildMatchData maxdepth>1:"+childName+" for "+ windowName);				
        	result = iterateChildMatchData(aparent, gather);
        }
        // (Carl Nagle) some depths are still only 1
        else{
			Log.info("GOV.getChildMatchData maxdepth=1:"+childName+" for "+ windowName);				
			path.addElement(createGuiObjectRecognition(pathVector, maxdepth));
        	result = iterateChildMatchData(aparent, gather);
        }

        Log.info("GOV.getChildMatchData: matches: "+ result.size() );
		return result;
	}


    /** Return the subclass of GuiClassData used by your tool. */				
    protected abstract GuiClassData getGuiClassData();
    
	/** 
	 * Return an array representing all known parent windows.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the returned 
     * array should actually be an array of keys created from convertToKeys 
     * and\or makeUniqueCacheKey.
     * @see #convertToKeys(Object[])
     * @see #makeUniqueCacheKey(Object)
     * @see #putCachedItem(Object, Object)  
     * @see #getCachedItem(Object)
     **/
	public abstract Object[] getParentObjects();		

	/** 
	 * Return an array representing all known parent windows from one single domain.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the returned 
     * array should actually be an array of keys created from convertToKeys 
     * and\or makeUniqueCacheKey.
     * @param domainname should be one of the supported org.safs.Domains constants like 
     * "Java", "Html", "Win", etc..
     * @see #convertToKeys(Object[])
     * @see #makeUniqueCacheKey(Object)
     * @see #putCachedItem(Object, Object)  
     * @see #getCachedItem(Object)
     **/
	public abstract Object[] getDomainParentObjects(String domainname);		

	/** 
	 * Return an array representing all known child objects of the parent.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the implementor should
     * assume the parent is actually stored in the internal cache and the 
     * parent in hand is actually a key to the cached object that must be 
     * retrieved using getCachedItem(object).  
     * Also, if getProcessMode()==MODE_EXTERNAL_PROCESSING the returned 
     * array should actually be an array of keys created from convertToKeys 
     * and\or makeUniqueCacheKey.
	 * @return array of objects or an empty array of new Object[0].
     * @see #convertToKeys(Object[])
     * @see #makeUniqueCacheKey(Object)
     * @see #putCachedItem(Object, Object)  
     * @see #getCachedItem(Object)
     **/
	public abstract Object[] getChildObjects(Object parent);		

    /** 
     * Return true if the provided object represents a valid gui component.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the implementor should
     * assume the object is actually stored in the internal cache and the 
     * object in hand is actually a key to the cached object that must be 
     * retrieved using getCachedItem(object).
     * @see #getCachedItem(Object)
     **/
    public abstract boolean isValidGuiObject(Object object);
    
    /** 
     * Return true if the provided object represents a gui container. 
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the implementor should
     * assume the object is actually stored in the internal cache and the 
     * object in hand is actually a key to the cached object that must be 
     * retrieved using getCachedItem(object).
     * @see #getCachedItem(Object)
     **/
    protected abstract boolean isValidGuiContainer(Object object);
    
	/** Return a GuiObjectRecognition subclass instance.*/
	protected abstract GuiObjectRecognition createGuiObjectRecognition(String subpath, int govLevel);		

	/** 
	 * Return a GuiChildIterator subclass instance.
	 * @param aparent is NOT a cached key, but should be the actual 
	 * engine-specific object that should have already been retrieved from 
	 * internal cache if MODE_EXTERNAL_PROCESSING.
	 */
	protected abstract GuiChildIterator createGuiChildIterator(Object aparent, GuiObjectVector govVector, List gather);

	/** Return a GuiChildIterator subclass instance.*/
	protected abstract GuiChildIterator createGuiChildIterator(List gather);
	
	/**
	 * @return Returns the pathVector.
	 */
	public String getPathVector() {
		return pathVector;
	}
	
	/**
	 * Remove prefixes known by SAFS from a recognition string, return the left part and a list of prefixes removed.
	 * The known prefixes are defined in recognitionStringPrefixes[].
	 * 
	 * @param pathVector String, a recognition string that may start with prefixes.
	 * @param list List<String>, returned list that contains prefixes SAFS can handle.
	 * @return a string that has been removed prefixes from.
	 */
	public static String removeRStringPrefixes(String pathVector, List<String> list) {
		String tmpath = pathVector;
		int idx;
		do {
			for(idx=0; idx<recognitionStringPrefixes.length; idx++){
				if(tmpath.startsWith(recognitionStringPrefixes[idx])){
					list.add(recognitionStringPrefixes[idx]);
					tmpath = tmpath.substring(recognitionStringPrefixes[idx].length());
					break;
				}
			}
		} while(idx < recognitionStringPrefixes.length); // end loop if any prefix no found in pathVector

		return tmpath;
	}
	
	/**
	 * @param prefixList List<String>, a list of mode-prefix
	 * @param mode String, a ceratin mode like {@link #FULLPATH_SEARCH_MODE_PREFIX}, {@link #RFT_FIND_SEARCH_MODE_PREFIX}
	 * @return boolean, true if the list contains the mode.
	 */
	public static boolean isMode(List<String> prefixList, String mode){
		if(prefixList==null || mode==null || prefixList.isEmpty()) return false;
		for(String prefix: prefixList) if (mode.equalsIgnoreCase(prefix)) return true;
		return false;
	}
	public static boolean isPASMMode(List<String> prefixList){
		return GuiObjectVector.isMode(prefixList, GuiObjectVector.PROPERTYALL_SEARCH_MODE_PREFIX);
	}
	
	/**
	 * This must be provided to a Constructor or otherwise be set prior to using
	 * an instance of this class.  The routine will check the provided path vector 
	 * for the FULLPATH_SEARCH_MODE_PREFIX (:FPSM:) and setFullPathSearchMode; 
	 * for the MAPPEDCLASS_SEARCH_MODE_PREFIX (:MCSM:) and setMappedClassSearchMode
	 * for the RFT_FIND_SEARCH_MODE (:RFSM:) and setRftFindSearchMode
	 * accordingly.  If the prefix exists it is removed prior to setting the 
	 * official pathVector.
	 * 
	 * @param pathVector The pathVector to set.
	 */
	public void setPathVector(String pathVector) {
		String ucpath = pathVector.trim();
		
		setFullPathSearchMode(false);     // make sure it is default	
		setMappedClassSearchMode(false);  // make sure it is default 
		setRftFindSearchMode(false);
		setPropertyAllSearchMode(false);
		
		try{
			List<String> prefixlist = new ArrayList<String>();
			ucpath = removeRStringPrefixes(ucpath, prefixlist);
			for (int i=0; i < prefixlist.size(); i++){
				String prefix = (String)prefixlist.get(i);
				if (prefix.equals(FULLPATH_SEARCH_MODE_PREFIX)) {
					setFullPathSearchMode(true);
					Log.info("GOV.setPathVector detected FULLPATH_SEARCH_MODE.");					
				} else if (prefix.equals(MAPPEDCLASS_SEARCH_MODE_PREFIX)){
					setMappedClassSearchMode(true);
					Log.info("GOV.setPathVector detected MAPPEDCLASS_SEARCH_MODE.");					
				} else if (prefix.equals(RFT_FIND_SEARCH_MODE_PREFIX)){
					setRftFindSearchMode(true);
					Log.info("GOV.setPathVector detected RFT_FIND_SEARCH_MODE.");
				}else if (prefix.equals(PROPERTYALL_SEARCH_MODE_PREFIX)){
					setPropertyAllSearchMode(true);
					Log.info("GOV.setPathVector detected PROPERTYALL_SEARCH_MODE_PREFIX.");	
				}
			} 
		}catch(Exception x){}

		this.pathVector = ucpath.toString();
	}

	/**
	 * This must be provided to a Constructor or otherwise be set prior to using
	 * an instance of this class.
	 * 
	 * @param childName The childName to set.
	 */
	public void setChildName(String childName) {
		this.childName = childName;
	}
	/**
	 * This must be provided to a Constructor or otherwise be set prior to using
	 * an instance of this class.
	 * 
	 * @param windowName The windowName to set.
	 */
	public void setWindowName(String windowName) {
		this.windowName = windowName;
	}

	/**
	 * Make the Window referenced by _comp the active window.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the implementor should
     * assume the _comp is actually stored in the internal cache and the 
     * _comp in hand is actually a key to the cached object that must be 
     * retrieved using getCachedItem(object).
	 * @param _comp is a String key used to retrieve cached items or an 
	 * engine-specific proxy implementation of the component itself.  This is 
	 * usually one the objects returned in the Object[] from getParentObjects();
	 * @see #getParentObjects() 
     * @see #getCachedItem(Object)
	 */
	public abstract void setActiveWindow(Object _comp);
	
	/**
	 * As of initial writing, there are two modes in which Gui object usage 
	 * may differ inside a running engine:<br/>
	 * <ul>
	 * <li>MODE_ENGINE_PROCESSING
	 * <li>MODE_EXTERNAL_PROCESSING
	 * </ul>
	 * <p>Engine processing is the typical usage when the engine controls the flow of 
	 * component processing and uses largely engine-specific objects (like RFT  
	 * TestObjects) to pass component references around within its own 
	 * process space.
	 * <p>External processing is when an external process largely controls the flow of 
	 * component processing and must usually resort to unique String keys to reference 
	 * cached components in the engine process.  This is the mode tools like 
	 * ProcessContainer need to use.
	 * <p>
	 * When mode is (re)set to MODE_EXTERNAL_PROCESSING the internal reference cache 
	 * will be (re)initialized and empty.
	 * @param mode 
	 */
	public void setProcessMode(int mode){
		process_mode = mode;
		if (mode==MODE_EXTERNAL_PROCESSING) resetExternalModeCache();
	}
	
	/**
	 * Return the current setting for processing mode. 
	 * Valid values: MODE_ENGINE_PROCESSING, MODE_EXTERNAL_PROCESSING
	 */
	public  int getProcessMode(){ return process_mode; }
	
	/**
	 * Clear and\or reset the internal component cache used in non-typical modes 
	 * of operation like MODE_EXTERNAL_PROCESSING (Process Container). 
	 * @see #makeUniqueCacheKey(Object)
	 * @see #putCachedItem(Object, Object)
	 * @see #getCachedItem(Object)
	 * @see #removeCachedItem(Object)
	 */
	public void resetExternalModeCache(){
		if (cache != null) cache.clear();
		cache = new Hashtable(INITIAL_CACHE_SIZE);
	}
	
	/**
	 * Attempts to retrieve an item from cache using the provided key.
	 * If the key is a String, will attempt to convert it to upper-case before 
	 * using it.
	 * If not found will attempt to use key as-is.
	 * If not found will return key as-is.
	 * @param key Object to use as lookup reference into cache
	 * @return Object stored in cache or key as provided.
	 * @see #makeUniqueCacheKey(Object)
	 * @see #putCachedItem(Object, Object)
	 * @see #removeCachedItem(Object)
	 */
	protected Object getCachedItem(Object key){
		Object item = null;
		if (cache==null) return key;
		if (key instanceof String) item = cache.get(((String)key).toUpperCase());		
		if (item==null) item = cache.get(key);
		if (item==null) item = key;
		return item;
	}
	
	/** 
	 * Remove an item from cache.  
	 * Will attempt to use key as-is.
	 * If item is not found and the key is a String, will attempt to convert it 
	 * to upper-case before using it.
	 * @param key Object to use as lookup reference into cache
	 * @return the Object removed or null if not found.
	 * @see #makeUniqueCacheKey(Object)
	 * @see #getCachedItem(Object)
	 * @see #putCachedItem(Object, Object)
	 */
	protected Object removeCachedItem(Object key){
		Object item = null;
		if ((cache==null)||(key==null)) return null;
		item = cache.remove(key);
		if ((item==null)&&(key instanceof String)) item = cache.remove(((String)key).toUpperCase());		
		return item;
	}

	/**
	 * Attempts to put an item in cache using the provided key.
	 * If the key is a String, will convert it to upper-case before 
	 * using it.
	 * The routine will initialize the cache if it does not exist.
	 * @param key Object to use as lookup reference into cache.
	 * @param item Item to store in the cache.
	 * @throws IllegalArgumentException if either key or item is null. 
	 * @see #makeUniqueCacheKey(Object)
	 * @see #getCachedItem(Object)
	 * @see #removeCachedItem(Object)
	 */
	protected void putCachedItem(Object key, Object item){
		if (cache==null) resetExternalModeCache();
		try{
			if(key instanceof String) {
				cache.put(((String)key).toUpperCase(), item);
			}else{
				cache.put(key, item);
			}
		}
		catch(NullPointerException np){
			throw new IllegalArgumentException("Neither cache key nor item can be null.");
		}
	}
	
	private static String __last_unique_key = "";
	/**
	 * Routine is used to create a unique ID String key that can be used by external 
	 * processes like Process Container to identify an engine-specific item in the 
	 * cache.
	 * @param item to be stored in cache.
	 * @return unique String suitable to be the key for the item.
	 * @see #putCachedItem(Object, Object)
	 * @see #getCachedItem(Object)
	 * @see #removeCachedItem(Object)
	 */
	protected String makeUniqueCacheKey(Object item){
		
		String timestamp = "";
		synchronized(__last_unique_key){
			do{	timestamp = UUID.randomUUID().toString();
			}while(timestamp.equals(__last_unique_key));
			__last_unique_key = timestamp;
		}
		return timestamp;
	}
	
	/**
	 * Convert an array of engine-specific objects to an array of unique keys 
	 * in the cache.  The items will be stored in the cache using the unique keys.
	 * @param items Array of objects to store in the cache.
	 * @return an array of object keys used to retrieve the items from the cache.
	 * @see #makeUniqueCacheKey(Object)
	 * @see #putCachedItem(Object, Object)
	 * @see #getCachedItem(Object)
	 * @see #removeCachedItem(Object)
	 * @author Carl Nagle APR 23,2010 handle case of null items in items array.
	 */
	protected Object[] convertToKeys(Object[] items){
		if (items == null) return new Object[0];
		Vector keys = new Vector();
		Object item;
		String key = null;
		for(int it=0; it<items.length;it++){
			item = items[it];
			if(item != null){
				key = makeUniqueCacheKey(item);
				keys.add(key);
				putCachedItem(key, item);
			}
		}
		if(keys.size()==0) return new Object[0];
		return keys.toArray();
	}
	
}

