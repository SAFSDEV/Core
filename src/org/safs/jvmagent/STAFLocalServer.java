/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

import java.awt.Rectangle;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.safs.Log;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.STAFProcessContainerHelper;
import org.safs.tools.drivers.STAFProcessContainerResult;
import org.safs.tools.engines.EngineInterface;

/**
 * @author Carl Nagle
 *
 * Provides a concrete implementation of a LocalServer using STAF communication 
 * to running SAFS engines.  This is NOT limited to Java implementations of engines.
 * 
 * JUL 28 2008 JunwuMa Added method hightLightMatchingChildObject. The engine command is already supported in RJ.
 *                     See org.safs.EngineCommandProcessor   
 * NOV  7 2008 JunwuMa Modify the returning type of method highlightMatchingChildObject(), setting it to boolean.
 * NOV 27 2008 JunwuMa Add methods getAccessibleName and getNonAccessibleName.  
 * AUG  6 2009 JunwuMa Add method enableDomains() for sending engine command 'enableDomains'.    
 * OCT 20 2009 JunwuMa Add method getMatchingChildKeysAtPoint().
 * OCT 27 2009 JunwuMa Add method highlightMatchingChildObjectByKey().
 * NOV 02 2010 LeiWang Add method getComponentRectangle(): Help to implement the "Map JPG" function of SPC.  
 * FEB 11 2011 Dharmesh4 Added method isTopLevelPopupContainer.
 * MAY 25,2011 Dharmesh4 Added getClassIndex and getDomainName methods. 
 * MAR 16 2012 JunwuMa Add getObjectRecognitionAtScreenCoords(x, y) to call corresponding engine command. 
 */

public class STAFLocalServer extends DriverConfiguredSTAFInterfaceClass implements LocalServer {

	/** 'E' **/
	public static final String RTYPE = DriverConstant.RECTYPE_E;
	
	STAFProcessContainerHelper trd;
	
	/**
	 * 
	 */
	public STAFLocalServer() {
		super();
		servicename = "SAFSProcessContainerServer"; //not a service
	}

	/**
	 * @see org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
		super.launchInterface(configInfo);
		weStartedService=false; //so no attempt is made to shut down a "service"
		trd = new STAFProcessContainerHelper();
		trd.setSTAFHelper(staf);
		trd.reinit();
	}

	/**
	 * Superclass implementation checks for a service.  We are not a service.
	 * We always return 'true'.
	 * @see org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass#isToolRunning()
	 */
	public boolean isToolRunning() { return true;}
	
	/**
	 * Generic routine to execute an engine command that takes no parameters and 
	 * is only to be executed by the specific EngineInterface stored in the 
	 * STAFProcessContainerResult.
	 * @param object STAFProcessContainerResult object identifying one engine and component.
	 * @param command String engine command to execute.
	 * @return trd.statusInfo returned from engine. May be null.
	 *         DriverConstant.SAFS_NULL will be converted to a null value.
	 * @see DriverConstant#SAFS_NULL
	 */
	protected String processStringMethod(Object object, String command){
		if(object==null) return null;		
		STAFProcessContainerResult oobject = null;
		try{
			oobject = (STAFProcessContainerResult) object;
		}catch(ClassCastException cce){
			Log.info("STAFLocalServer.processStringMethod object was NOT a STAFProcessContainerResult.");
			return null;
		}
		String sobject = oobject.get_statusInfo();
		if ((sobject==null)||(sobject.length()==0)||sobject.equalsIgnoreCase(DriverConstant.SAFS_NULL)) return null;
		
		String sep = getUniqueSeparator(sobject);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+command+sep+sobject);
		long status = oobject.get_engine().processRecord(trd);

		String val = trd.getStatusInfo();
		return ((val==null)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL)))? null : val;
	}
	
	/**
	 * Return true if the object is PopupWindow Container, 
	 * Some Java Frames and\or JFrames can contain a JPopup in a child
	 * panel instead of an "owned" window.
	 * @param object An object for to check PopupWindow. 
	 * @return boolean true if PopupWindow found or false.
	 */
	public boolean isTopLevelPopupContainer(Object object){
		String val = processStringMethod(object, "isTopLevelPopupContainer");
		if ((val==null)||(val.length()==0)) return false;
		return (new Boolean(val)).booleanValue();
	}
	
	public void enableDomains(String domains){
		String method = "enableDomains";
		if ((domains == null)||(domains.length()==0)) return;
		
		String sep = getUniqueSeparator(domains);
		if (sep == null) return;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+domains);
		processCommand(trd, true );
	}
	/**
	 * Return the number of currently active Top Level Windows from 
	 * all known engines.
	 */
	public int getTopLevelCount() {		
		String method = "getTopLevelCount";		
		String sep = getUniqueSeparator(method);
		if (sep == null) return 0;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method);

		long status = processCommand(trd, true );

		Vector results = trd.getResults();
		if (results.size()==0) return 0;
		
		STAFProcessContainerResult oresult = null;
		String val = null;
		int count = 0;
		for (int r=0;r<results.size();r++){
			try{ oresult = (STAFProcessContainerResult) results.elementAt(r);}
			catch(ClassCastException cce){continue;}
			val = oresult.get_statusInfo();
			if ((val==null)||(val.length()==0)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL))) continue;
			try{
				count += Integer.parseInt(val);
			}
			catch(NumberFormatException nfe){}
		}
		return count;
	}

    /**
     * Return an array representing the TopLevel windows from all known engines.
     * Each engine's array of objects will be stored in the 
	 * trd.getStatusInfo() as a delimited array of fields.  The first character in 
	 * the String will identify the separator used between fields.  The fields will 
	 * be converted to a String array by this routine.
     * @return Object[] (STAFProcessContainerResult objects)representing all active top 
     * level windows from all known engines.
     * A zero-length Object[] array will be returned if no Top Level Windows are active.
     */
	public Object[] getTopLevelWindows() {
		
		String method = "getTopLevelWindows";
		
		String sep = getUniqueSeparator(method);
		if (sep == null) return new Object[0];
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method);

		long status = processCommand(trd, true );
		Vector results = trd.getResults();
		if (results.size()==0) return new Object[0];
		
		STAFProcessContainerResult oresult = null;
		Vector windows = new Vector();
		String val = null;
		String[] wins;
		for (int r=0;r<results.size();r++){
			try{oresult = (STAFProcessContainerResult) results.elementAt(r);}
			catch(ClassCastException cce){ continue;}
			val = oresult.get_statusInfo();
			if ((val==null)||(val.length()==0)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL))) continue;
			//first char is separator
			sep = val.substring(0,1);
			val = val.substring(1);
			wins = val.split(sep);
			for (int w=0;w<wins.length;w++){
				windows.add(new STAFProcessContainerResult(oresult.get_engine(), oresult.get_statusCode(), wins[w]));
			}
		}
		return windows.toArray();
	}

    /**
     * Attempts to set anObject as the active (topmost?) Window or Component.
     * @param anObject STAFProcessContainerResult provided by the engine identifying the object.
     */
	public void setActiveWindow(Object anObject) {
		processStringMethod(anObject, "setActiveWindow");
	}

	/**
	 * Return the number of children available in the provided STAFProcessContainerResult parent.
	 */
 	public int getChildCount(Object parent) {
		String val = processStringMethod(parent,"getChildCount");	
		if ((val==null)||(val.length()==0)) return 0;
		try{
			return Integer.parseInt(val);
		}
		catch(NumberFormatException nfe){}
		return 0;
	}

	/**
	 * Return an array representing the children of the provided parent object.
     * The array of objects returned by the engine will be stored in the 
	 * trd.getStatusInfo() as a delimited array of fields.  The first character in 
	 * the String will identify the separator used between fields.  The children will 
	 * be converted to an Object[] of STAFProcessContainerResult objects by this routine.
	 * 
     * @param parent A STAFProcessContainerResult object from getTopLevelWindows or from a previous call to getChildren.
	 * The parent is often one of the elements of the TopLevelWindow array or somewhere 
	 * lower in that same hierarchy.
	 * 
     * @return Object[] (STAFProcessContainerResult objects) representing all known children of the provided parent.
     * A zero-length Object[] array will be returned if the parent has no children.
	 */
	public Object[] getChildren(Object parent) {
		String val = processStringMethod(parent,"getChildren");
		if ((val==null)||(val.length()==0)) return new Object[0];
		
		//first char is separator
		String sep = val.substring(0,1);
		val = val.substring(1);
		Vector children = new Vector();
		String[] childs = val.split(sep);
		STAFProcessContainerResult oparent = null;
		
		try{oparent = (STAFProcessContainerResult) parent;}
		catch(ClassCastException cce){ return new Object[0];}
		
		for(int c=0;c<childs.length;c++){
			children.add(new STAFProcessContainerResult(oparent.get_engine(),oparent.get_statusCode(),childs[c]));
		}
		return children.toArray();
	}

    /**
     * Retrieve the Caption of the object if one exits.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the caption of the object or an empty String.  Null is possible.
     */
 	public String getCaption(Object object) {
		return processStringMethod(object, "getCaption");
	}

    /**
     * Retrieve the accessible name of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object.
     */
	public String getAccessibleName(Object object) {
		return processStringMethod(object, "getAccessibleName");
	}

    /**
     * Retrieve the name (not including accessible name) of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object.
     */
	public String getNonAccessibleName(Object object) {
		return processStringMethod(object, "getNonAccessibleName");
	}

    /**
     * Retrieve the name of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object.
     */
	public String getName(Object object) {
		return processStringMethod(object, "getName");
	}
	
    /**
     * Retrieve the ID of the object if the object has an ID.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the ID of the object.
     */
	public String getID(Object object) {
		return processStringMethod(object, "getID");
	}

    /**
     * Retrieve the displayed text value of the object if the object has a text value.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String the text value of the object.  Since text values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
	public String getText(Object object) {
		return processStringMethod(object, "getText");
	}

	/**
	 * The array of objects will be stored in the 
	 * trd.getStatusInfo() as a delimited array of fields.  The first character in 
	 * the String will identify the separator used between fields.  The fields will 
	 * be converted to a String array by this routine.
     * A zero-length array will be returned if the parent has no children.
     */
	public String[] getPropertyNames(Object object) {
		String val = processStringMethod(object,"getPropertyNames");
		if ((val==null)||(val.length()==0)||val.equalsIgnoreCase(DriverConstant.SAFS_NULL)) return new String[0];		
		//first char is separator
		String sep = val.substring(0,1);
		val = val.substring(1);
		return val.split(sep);
	}
	
	/**
     * Retrieve the ClassIndex of the object if the object has an ClassIndex
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the ClassIndex of the object.
     */
	public String getClassIndex(Object object) {
		return processStringMethod(object, "getClassIndex");
	}
	
	/**
	 * Retrive the domain name of the object
	 * @param object An object from getTopLevelWindows or from the a previous call to getChildren
	 * @return String the Domain name of the object.
	 */
	public String getDomainName(Object object){
		return processStringMethod(object,"getDomainName");
	}

    /**
     * Retrieve the property value of the object if the object has the property.
     * 
     * @param object -- An object from getTopLevelWindows or from a previous call to getChildren.
	 * @param property -- the case-sensitive name of the property to seek.
	 * 
	 * @return String the text value of the object property.  Since property values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
	public String getProperty(Object object, String property) {
		String method = "getProperty";
		if (object == null) return null;
		if ((property == null)||(property.length()==0)) return null;
		STAFProcessContainerResult oparent = null;
		try{ oparent = (STAFProcessContainerResult) object;}
		catch(ClassCastException cce){return null;}
		String sparent = oparent.get_statusInfo();
		
		String sep = getUniqueSeparator(sparent + property);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+sparent+sep+property);
		long status = oparent.get_engine().processRecord(trd);
		if (status != DriverConstant.STATUS_NO_SCRIPT_FAILURE) return null;
		String result = trd.getStatusInfo();
		return (result.equalsIgnoreCase(DriverConstant.SAFS_NULL))? null:result;
	}

    /**
     * Return the Class name of the object.  
     * For example, we may get "javax.swing.JFrame" or the name of the subclass if 
     * it is a subclass of JFrame.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String Class name of the object.
     */
	public String getClassName(Object object) {
		return processStringMethod(object, "getClassName");
	}

    /**
     * Return the Z-Order level of the object (generally for a top level window).  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return int z-order value of the object among all objects if this can be determined.
	 *          0 normally indicates the topmost Window.  
	 *          1 is normally the Window behind that, etc..
     */
	public int getLevel(Object object) {
		String val = processStringMethod(object, "getLevel");
		if ((val==null)||(val.length()==0)) return 0;
		try{
			return Integer.parseInt(val);
		}
		catch(NumberFormatException nfe){}
		return 0;
	}

    /**
     * Return true if the object is showing/visible.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is showing/visible.
     */
	public boolean isShowing(Object object) {
		String val = processStringMethod(object, "isShowing");
		if ((val==null)||(val.length()==0)) return false;
		return (new Boolean(val)).booleanValue();
	}

    /**
     * Return true if the object is still valid/finadable in the JVM.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is still valid/findable.
     */
	public boolean isValid(Object object) {
		String val = processStringMethod(object, "isValid");
		if ((val==null)||(val.length()==0)) return false;
		return (new Boolean(val)).booleanValue();
	}

	/**
	 * The array of objects will be stored in the 
	 * trd.getStatusInfo() as a delimited array of fields.  The first character in 
	 * the String will identify the separator used between fields.  The fields will 
	 * be converted to a String array by this routine.
     * A zero-length array will be returned if the parent has no children.
     */
	public String[] getSuperClassNames(Object object) {
		String val = processStringMethod(object,"getSuperClassNames");
		if ((val==null)||(val.length()==0)) return new String[0];		
		//first char is separator
		String sep = val.substring(0,1);
		val = val.substring(1);
		return val.split(sep);
	}

	/**
	 * Mechanism to retrieve a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * @param theObject--STAFProcessContainerResult object for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return Object STAFProcessContainerResult object found relative to theObject or null.
	 **/
	public Object getMatchingPathObject(Object parent, String path){
		String method = "getMatchingPathObject";
		if (parent == null) return null;
		if ((path == null)||(path.length()==0)) return null;
		STAFProcessContainerResult oparent = null;
		try{ oparent = (STAFProcessContainerResult) parent;}
		catch(ClassCastException cce){return null;}
		String sparent = oparent.get_statusInfo();
		
		String sep = getUniqueSeparator(sparent+path);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+sparent+sep+path);
		long status = oparent.get_engine().processRecord(trd);

		String val = trd.getStatusInfo();
		if ((val==null)||(val.length()==0)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL))) return null;
		//first char is separator
		return new STAFProcessContainerResult(oparent.get_engine(),oparent.get_statusCode(),val);
	}
	
	/**
	 * 
	 * @param parentkey, a String as the key to the cached object in Engine.
	 * @param x
	 * @param y
	 * @return an array that holds every unique key of matching objects
	 */
	public Object[] getMatchingChildKeysAtPoint(Object parentkey, int x, int y){
		String method = "getMatchingChildKeysAtPoint";
		if (parentkey == null) return null;
		if ((x <= 0)||(y <= 0)) return null;

		String sep = getUniqueSeparator((String)parentkey);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+parentkey+sep+x+sep+y);
		long status = processCommand(trd, false); 
		
		if (status != DriverConstant.STATUS_NO_SCRIPT_FAILURE) return new String[0];

		String result = trd.getStatusInfo();
		//first char is separator
		if ( result.length() == 0)
			return new String[0];
		else {
			sep = result.substring(0,1);
			result = result.substring(1);
			String[] childs = result.split(sep);
			return childs;
		}
	}
	
	/**
	 * Currently always returns 'false'. 
	 * @see org.safs.jvmagent.LocalAgent#isMatchingPath(java.lang.Object, java.lang.String)
	 */
	public boolean isMatchingPath(Object theObject, String thePath)
			throws Exception {
		return false;
	}

	/**
	 * Currently returns an empty 2D array.
	 * @see org.safs.jvmagent.LocalAgent#getStringData(java.lang.Object, java.lang.Object)
	 */
	public String[][] getStringData(Object object, Object dataInfo) {
		return new String[0][0];
	}

	/** 
	 * Currently returns TestRecordData unmodified.  
	 * @see org.safs.jvmagent.LocalAgent#process(java.lang.Object, org.safs.TestRecordData)
	 */
	public TestRecordData process(Object object, TestRecordData testRecordData) {
		return testRecordData;
	}
	
	/**
	 * Engine will try to locate/highlight a window object matching the 
	 * provided recognition string (ChildRec) under the top-most window with R-String (winRec).
	 * if winRec equals to ChildRec, the top-most window will be highlighted.
	 * It is supported by RJ.
	 *  
	 * @param winRec, the recognition string of the top-most window
	 * @param ChildRec, the recognition string of the child window
	 */
	public boolean highlightMatchingChildObject(String winRec, String ChildRec){
		String method = "highlightMatchingChildObject";
		if ((winRec == null)||(winRec.length()==0)||ChildRec.length()==0) return false;
		
		String sep = getUniqueSeparator(winRec);
		if (sep == null) return false;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+winRec+sep+ChildRec);

		// highlight the matching object (allEngines=false)
		long status = processCommand(trd, false);
		return new Boolean(trd.getStatusInfo()).booleanValue();
	}
	
	/**
	 * Engine will try to locate/highlight a window object by its key to the cached object
	 * It is supported by RJ.
	 *  
	 * @param winkey,  the key to cached test object for window in RJ engine
	 * @param compkey, the key to cached test object for component in RJ engine
	 * @param rstring, the recognition string of the cached test object
	 */
	public boolean highlightMatchingChildObjectByKey(Object winkey, Object compkey, String rstring){
		String method = "highlightMatchingChildObjectByKey";
		if (winkey==null || compkey==null) return false;
		
		String sep = getUniqueSeparator((String)winkey+(String)compkey);
		if (sep == null) return false;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+winkey+sep+compkey+sep+rstring);

		// highlight the matching object (allEngines=false)
		long status = processCommand(trd, false);
		return new Boolean(trd.getStatusInfo()).booleanValue();
	}	
	
	public boolean clearHighlightedDialog(){
		String method = "clearHighlightedDialog";
		
		String sep = getUniqueSeparator(method);
		if (sep == null) return false;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method);

		processCommand(trd, false);
		return new Boolean(trd.getStatusInfo()).booleanValue();
	}
	/**
	 * 
	 * @param winkey	The cached key of the component's Test Object
	 * @return			A rectangle representing the component's screen location.
	 */
	public Rectangle getComponentRectangle(Object winkey){
		String debugmsg = this.getClass().getName()+".getComponentRectangle(): ";
		String method = "getComponentRectangle";
		String resutlSeparator = ",";
		
		String sep = getUniqueSeparator(method);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+winkey);

		long rc =processCommand(trd, false); 
		if(rc==DriverConstant.STATUS_NO_SCRIPT_FAILURE){
			//trd.getStatusInfo() will contain a string like "x, y, width, height", representing a rectangle 
			//of the component on the screen.
			List<String> list = StringUtils.getTokenList(trd.getStatusInfo(), resutlSeparator);
			int x,y,w,h;
			try{
				x = Integer.parseInt(list.get(0));
				y = Integer.parseInt(list.get(1));
				w = Integer.parseInt(list.get(2));
				h = Integer.parseInt(list.get(3));
			}catch(Exception e){
				Log.debug(debugmsg+e.getMessage());
				return null;
			}
			Rectangle rect = new Rectangle(x,y,w,h);
			Log.info(debugmsg+" get rectangle "+rect);
			return rect;
		}else{
			Log.debug(debugmsg+" processCommand return error code:"+rc);
			return null;
		}
	}
	
	/**
	 * 
	 * @param key		The key of window or component returned by engine side.
	 * @return			The Windows native handle.
	 */
	public long getTopWindowHandle(Object key){
		String debugmsg = this.getClass().getName()+".getTopWindowHandle(): ";
		String method = "getTopWindowHandle";
		
		String sep = getUniqueSeparator(method);
		if (sep == null) return 0;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+key);
		long rc = processCommand(trd,false);
		if(rc==DriverConstant.STATUS_NO_SCRIPT_FAILURE){
			long hWnd = Long.parseLong(trd.getStatusInfo());
			return hWnd;
		}else{
			Log.debug(debugmsg+" processCommand return error code:"+rc);
			return 0;
		}
	}
	/**
	 * Engines will attempt to locate a top-level window object matching the 
	 * provided recognition string.  The String object will be stored in the 
	 * trd.getStatusInfo().
	 * @param winRec String recognition string for Parent window object.
	 * @return STAFProcessContainerResult reference to the first engine and window match.
	 * This object will be null if not found.
	 */
	public Object getMatchingParentObject(String winRec){
		String method = "getMatchingParentObject";
		if ((winRec == null)||(winRec.length()==0)) return null;
		
		String sep = getUniqueSeparator(winRec);
		if (sep == null) return null;
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+winRec);

		// right now just get the first match (allEngines=false)
		long status = processCommand(trd, false );
		if (status!=DriverConstant.STATUS_NO_SCRIPT_FAILURE) return null;
		
		//process potentially multiple results Vector
		//right now just get first match
		String val = trd.getStatusInfo();
		if ((val==null)||(val.length()==0)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL))) return null;
		return trd.getResults().get(0);
	}

	/**
	 * Engine will attempt to locate a child object matching the 
	 * provided recognition string.  The array of objects will be stored in the 
	 * trd.getStatusInfo() as a delimited array of fields.  The first character in 
	 * the String will identify the separator used between fields.  The fields will 
	 * be converted to a String array by this routine.
	 * @param parent STAFProcessContainerResult object identifying parent window previously retrieved.
	 * @param childRec String recognition string for desired child object.
	 * @return Object array of STAFProcessContainerResult references to the matching child object(s) found.
	 * A zero-length Object[] array will be returned if the parent has no children.
	 */
	public Object[] getMatchingChildObjects(Object parent, String childRec){
		String method = "getMatchingChildObjects";
		if (parent == null) return new Object[0];
		if ((childRec == null)||(childRec.length()==0)) return new Object[0];
		STAFProcessContainerResult oparent = null;
		try{oparent = (STAFProcessContainerResult) parent;}
		catch(ClassCastException cce){return new Object[0];}
		String sparent = oparent.get_statusInfo();
		
		String sep = getUniqueSeparator(sparent+childRec);
		if (sep == null) return new Object[0];
		
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+sparent+sep+childRec);
		long status = oparent.get_engine().processRecord(trd);
		if (status!=DriverConstant.STATUS_NO_SCRIPT_FAILURE) {
			Log.info("SLS.getMatchingChildObjects failed status ("+ String.valueOf(status) +")");
			return new Object[0];
		}

		String val = trd.getStatusInfo();
		Log.info("SLS.getMatchingChildObjects status ("+ String.valueOf(status) +") parsing returned value: "+val);
		if ((val==null)||(val.length()==0)||(val.equalsIgnoreCase(DriverConstant.SAFS_NULL))) return new Object[0];
		//first char is separator
		sep = val.substring(0,1);
		val = val.substring(1);
		String[] childs = val.split(sep);
		STAFProcessContainerResult[] children = new STAFProcessContainerResult[childs.length];
		for(int c=0;c<childs.length;c++){
			children[c] = new STAFProcessContainerResult(oparent.get_engine(), status, childs[c]);
			Log.info("SLS.getMatchingChildObjects storing child: "+childs[c]);
		}
		Log.info("SLS returning "+ children.length + " objects in array.");
		return children;
	}
	
	
	/******************************************************************************
	 * This command is normally only called internally by processCommand.
	 * Route the input record to preferred engines only in the order of preference.
	 * Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 * @see #processCommand(TestRecordHelper)
	 ******************************************************************************/
	protected long routeToPreferredEngines(STAFProcessContainerHelper trd, boolean allEngines){
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;	
		ListIterator list = null;
		
		// try preferred engines first in the order they are preferred
		if (driver.hasEnginePreferences()){
			list = driver.getEnginePreferences();
			while((list.hasNext())&&(result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
				try{
					EngineInterface theEngine = driver.getPreferredEngine((String)list.next());
					result = theEngine.processRecord(trd);
					trd.addResults(new STAFProcessContainerResult(theEngine, result, trd.getStatusInfo()));
				}catch(IllegalArgumentException iax){
					// this should not happen!
					System.out.println(iax.getMessage());
					Log.error(iax.getMessage());
				}
				if(allEngines) result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
			}
		}
		return result;
	}
	
	/***************************************************************************
	 * This command is normally only called internally by processCommand.
	 * Route the input record to all engines in the order of initialization.
	 * Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 * <p>
	 * We will NOT route to an engine where isPreferredEngine==true since 
	 * preferred engines would have already been tried.
	 * <p>
	 * @see #routeToPreferredEngines(TestRecordHelper)
	 * @see #processCommand(TestRecordHelper)
	 **************************************************************************/
	protected long routeToEngines( STAFProcessContainerHelper trd, boolean allEngines) {
	
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;			
		ListIterator list = driver.getEngines();
		
		while((list.hasNext())&& (result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
			EngineInterface theEngine = (EngineInterface) list.next();
			try{
				// don't call it if we already called it as a 'preferred engine
				if(driver.isPreferredEngine(theEngine)) continue;
				result = theEngine.processRecord(trd);
				trd.addResults(new STAFProcessContainerResult(theEngine, result, trd.getStatusInfo()));
			}catch(IllegalArgumentException iax){
				// this should not happen!
				System.out.println(iax.getMessage());
				Log.error(iax.getMessage());
			}
			if(allEngines) result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}		
		return result;
	}
	
	/**
	 * Route the input record to available engines.
	 * Clears the STAFProcessContainerHelper results Vector before sending.
	 * Forward the input record to each engine until one of the 
	 * engines signals that it processed the record.  If allEngines is true 
	 * then the record will be forwarded to all preferred engines and then 
	 * all remaining engines.
	 * <p>
	 * Try preferred engines first, then try any remaining engines.
	 * <p>
	 * @see #routeToPreferredEngines(TestRecordHelper,boolean)
	 * @see #routeToEngines(TestRecordHelper,boolean)
	 */
	protected long processCommand(STAFProcessContainerHelper trd, boolean allEngines){

		trd.clearResults();
		long rc = routeToPreferredEngines(trd, allEngines);

		// try any remaining engines if not satisfied
		if ((allEngines)||(rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED))		
			rc =  routeToEngines(trd, allEngines);

		if ((!allEngines)&&(rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){			
			rc = DriverConstant.STATUS_SCRIPT_WARNING;			
		}else if (allEngines){
			rc = DriverConstant.STATUS_NO_SCRIPT_FAILURE;			
		}
		return rc;		
	}
	
	/** Array of posibbl separators: "," "|" ":" ";" "_" "#" "!" */
	static final String[] SEPS = {",","|",":",";","_","#","!"};
	
	/***********************************************************************
	 * Return a usable single character separator string that does NOT exist 
	 * in the provided field.
	 * Tries each character in SEPS array.
	 * @param afield String field to keep intact.
	 * @return unique String separator that does NOT exist in afield.
	 **********************************************************************/
	public static String getUniqueSeparator(String afield){
		try{
			for(int d=0;d<SEPS.length;d++){
				if(afield.indexOf(SEPS[d])< 1) return SEPS[d];
			}
			throw new NullPointerException("Separator options exhausted for:"+ afield);
		}catch(NullPointerException x){
			Log.info("SLS.getUniqueSeparator error for:"+ afield, x);
		}
		return null;		
	}
	
	/**
	 * @see org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass#reset()
	 */
	public void reset(){
		//TODO: how do we reset here?
	}
	
	public String getObjectRecognitionAtScreenCoords(int x, int y) {
		String method = "getObjectRecognitionAtScreenCoords";
			
		String sep = getUniqueSeparator(method);
		if (sep == null) return null;
			
		trd.setSeparator(sep);
		trd.setInputRecord(RTYPE+sep+method+sep+x+sep+y);

		long status = processCommand(trd, true );
		if (status != DriverConstant.STATUS_NO_SCRIPT_FAILURE) return null;
		String result = trd.getStatusInfo();
		return (result.equalsIgnoreCase(DriverConstant.SAFS_NULL))? null:result;
	}
}
