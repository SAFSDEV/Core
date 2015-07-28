/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.Hashtable;

import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;

import com.ibm.staf.STAFResult;

/**
 * Here we provide the basis for handling Application Maps through the SAFSMAPS 
 * service in STAF.  This class handles the registering, opening, and storing of 
 * App Map references from different sources.
 * <p>
 * DDGUIUtilities handles interfacing to the text-based App Map files handled by 
 * SAFSMAPS.  When doing this, it also forms a cache of all App Maps referenced AND 
 * the objects or items retrieved from those App Maps.  These are all handled and 
 * stored in ApplicationMap objects and their subclasses.
 * <p>
 * For example, a Rational subclass for ApplicationMap would use the basic interface 
 * to lookup object recognition strings, but then would extend that functionality to 
 * actually store tool-dependent TestObject references in the RApplicationMap objects
 * stored in this DDGUIUtilities.
 * <p>
 * As a subclass of STAFRequester, this class is expecting an implementation specific 
 * subclass to instantiate and provide a valid STAFHelper object.
 * 
 * @author Carl Nagle
 * @author Carl Nagle, SEP 16, 2003 Enabled new SAFSLOGS logging
 * @author CANAGL, JAN 25, 2008 Removed unused Selenium Import
 * @author CANAGL, FEB 01, 2010 Removed Selenium-specific ComponentFunctions.dll usage
 * @author SBJLWA, NOV 26, 2014 Modify waitForObject(): throws SAFSException to indicate
 *                              errors other than "ObjectNotFound", such as map is null,
 *                              cannot find window/component mapped value etc.
 * @since JUN 26, 2003
 **/
public abstract class DDGUIUtilities extends STAFRequester{
	
	
  /**
   * The storage of all ApplicationMaps ever opened or registered thru this 
   * class.  Each ApplicationMap is stored and referenced by its given name.
   **/
  protected static Hashtable appmaps = new Hashtable(5);
	
  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int DEBUG_MESSAGE      = AbstractLogFacility.DEBUG_MESSAGE;

  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int GENERIC_MESSAGE    = AbstractLogFacility.GENERIC_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_MESSAGE     = AbstractLogFacility.FAILED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_OK_MESSAGE  = AbstractLogFacility.FAILED_OK_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int PASSED_MESSAGE     = AbstractLogFacility.PASSED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_MESSAGE    = AbstractLogFacility.WARNING_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_OK_MESSAGE = AbstractLogFacility.WARNING_OK_MESSAGE;


	/**
	 * Retrieve a named ApplicationMap object from storage.  
	 * Only ApplicationMaps that have been successfully registered or opened can
	 * be retrieved.  This is normally only called by DDGUIUtilities and its 
	 * subclasses to satisfy other calls.
	 * <p>
	 * The retrieved ApplicationMap may have to be cast to the appropriate 
	 * ApplicationMap subclass.  For example RApplicationMap for Rational.
	 * <p>
	 * @param mapname -- the name by which the ApplicationMap will be known.
	 * <p>
	 * @return null if not found, which generally means it hasn't been successfully 
	 *         registered or opened.
	 **/
	protected ApplicationMap getAppMap(String mapname){
		return (ApplicationMap) appmaps.get(mapname.toUpperCase());
	}

	/**
	 * Create an ApplicationMap instance.
	 * Subclasses may override to provide engine-specific ApplicationMap subclass 
	 * instances suitable for their needs.
	 */
	protected ApplicationMap createAppMapInstance (String mapname){
		return new ApplicationMap(mapname);		
	}
	
	/**
	 * Store a named AppMap that has been successfully opened or found already 
	 * opened.
	 * <p>
	 * @param mapname -- the name by which the ApplicationMap will be known.
	 * <p>
	 * @param map -- the ApplicationMap to store with the given mapname.
	 **/
	protected void registerAppMap(String mapname, ApplicationMap map){
		appmaps.put(mapname.toUpperCase(), map);
	}

	/**
	 * Store a named AppMap that has been successfully opened or found already 
	 * opened.  This will create an AppMapInstance to store in our local cache.
	 * <p>
	 * @param mapname -- the name by which the ApplicationMap will be known.
	 * <p>
	 * @param mapfile -- the ApplicationMap to store with the given mapname.
	 **/
	public boolean registerAppMap (String mapname, String mapfile) {
		try {
			if (isAppMapRegistered(mapname)) return true;
			
			if (openAppMap(mapname, mapfile)) {
				// local cache for Application Maps
				ApplicationMap map = createAppMapInstance(mapname);
				map.setSTAFHelper(getSTAFHelper());
				registerAppMap(mapname, map);
				return true;
			
			} else { return false; }
			
		} catch(NullPointerException np) {	return false; }
	}

		
	/**
	 * Checks for an ApplicationMap object stored with the given name.
	 * <p>
	 * @param mapname -- the name for the AppMap
	 * <p>
	 * @return true if an ApplicationMap by this name is in storage.
	 **/
	public boolean isAppMapRegistered (String mapname){
		
		if (mapname == null) throw new NullPointerException(
							          "DDGUIUtilities.isAppMapRegistered: " + 
			                          "mapname");				
		// see if already registered	
		return (getAppMap(mapname) instanceof ApplicationMap);
	}


	/**
	 * Attempts to open an ApplicationMap file via the SAFSMAPS service in STAF.
	 * The file will be opened, but a subclass will still have to register/store  
	 * an ApplicationMap cache via a registerAppMap call.
	 * <p>
	 * The routine will NOT call STAF to open the file if an ApplicationMap 
	 * object is already registered for a map with the given name.
	 * <p>
	 * @param mapname -- the name for the AppMap
	 * <p>
	 * @param mapfile -- the AppMap file to open.  May be a full or relative path 
	 *                   depending upon how SAFSMAPS is configured.  If the mapfile 
	 *                   value is null, then we will attempt to use the mapname as 
	 *                   the filename.
	 * <p>
	 * @return true if successfully opened (or already registered as open)
	 **/
	public boolean openAppMap (String mapname, String mapfile){
		
		if (mapname == null) throw new NullPointerException(
							          "DDGUIUtilities.openAppMap: " + 
			                          "mapname is null");			

		if (mapname.length()==0){
			Log.info("DDG.openAppMap: invalid 0-length mapname");
			return false;
		}
		
		if (mapfile == null) mapfile = mapname;			
	
		// see if already OPEN in STAF	
		STAFResult result = null;
		try{ 
			result = staf.submit2ForFormatUnchangedService("local", STAFHelper.SAFS_APPMAP_SERVICE, 
		                          "QUERY "+ staf.lentagValue(mapname) +
		                          "FILENAME");
		
			if(result.rc == STAFResult.DoesNotExist){
                Log.info( "DDGUIUtilities.openAppMap: mapname: \"" + mapname+ 
                          "\" does not exist, trying again...");
				result = staf.submit2ForFormatUnchangedService("local", STAFHelper.SAFS_APPMAP_SERVICE,
		                          "OPEN "+ staf.lentagValue(mapname) +
		                          "FILE "+ staf.lentagValue(mapfile) );
			}
		
			if(result.rc == STAFResult.Ok) return true;

			Log.info( "DDGUIUtilities.openAppMap: mapname: \"" + mapname+
                      "\", file: \"" + mapfile+
                      "\", STAF Error: "+ result.rc +":"+ result.result);
			
		}catch(NullPointerException x){ 
			
			throw new NullPointerException( "DDGUIUtilities.openAppMap: possible \"staf.\" NullPointerException");
		}
		
		return false;			
	}

	/**
	 * clear a single local App Map cache of all old references.
	 * @param mapname of the App Map to clear
	 */
	public void clearAppMapCache(String mapname){
		ApplicationMap map = getAppMap(mapname);
		if (map instanceof ApplicationMap){
			map.clearMap();
		}
	}
	
	/**
	 * clear all known local App Maps' caches.
	 */
	public void clearAllAppMapCaches(){
		appmaps.clear();
	}
	
	
    /**
     * Wait up to a maximum timeout value for the specified component to "exist".
     *
     * @param appMapName  name of AppMap used to lookup component.
     * @param windowName  name of Window in AppMap.
     * @param compName    name of Window Component in AppMap.
     *                    If the Window IS the desired Component, then both windowName
     *                    and compName will be the same.
     * @param secTimeout  maximum wait in seconds
     *
     * @return  status 0 if successful; thrown exceptions if not.
     *           Other return codes may be implementation specific.
     * 
     * @throws SAFSObjectNotFoundException if the component or its parent cannot be found.
     * @throws SAFSException if some other problems occur, <br>
     *                       such as Map is not registered, or windowName/compName cannot be found in map.<br>
     **/
	public abstract int waitForObject (String appMapName, String windowName, String compName, long secTimeout)
										 throws SAFSObjectNotFoundException, SAFSException;

    /**
     * "Activate" the specified component's topmost parent.
     *
     * @param appMapName  name of AppMap used to lookup component.
     * @param windowName  name of Window in AppMap.
     * @param compName    name of Window Component in AppMap.
     *                    If the Window IS the desired Component, then both windowName
     *                    and compName will be the same.
     *
     * @return  status 0 if successful; thrown exceptions if not.
     *           Other return codes may be implementation specific.
     *
     * @throws SAFSObjectNotFoundException if the component or its parent cannot be found.
     **/
	public abstract int setActiveWindow (String appMapName, String windowName, String compName)
										 throws SAFSObjectNotFoundException;


	/**
	 * Attempt to locate a child object based on a matching property value.
	 * The check can require an exactMatch; or, a case-insensitive partial substring match if 
	 * the exactMatch parameter is false.  (Can use StringUtils.isCaseContainsMatch)
	 * <p>
	 * @param obj -- the pseudo-object reference whose children will be evaluated.
	 * <p>
	 * @param property -- the property of each child that will be evaluated.  The property is 
	 *                     expected to be one that can successfully return a value 
	 *                     cast as type String.
	 * <p>
	 * @param bench -- the benchmark value for the property to be used in comparisons.
	 * <p>
	 * @param exactMatch -- if true, an exact case-sensitive match of the bench value 
	 *                      provided is required to signal a match.<br>
	 *                      if false, a case-insensitive partial substring match with the 
	 *                      bench value is performed.
	 * <p>
	 * @return Object, the first child pseudo-object whose property value matches accordingly.
	 * <p>
	 * @exception SAFSObjectNotFoundException if no matching child object is found.
	 * <p>
	 * @see org.safs.StringUtils#isCaseContainsMatch(String,String,boolean)
	 **/
	public abstract Object findPropertyMatchedChild(Object obj, String property, String bench, boolean exactMatch)
	                                                 throws SAFSObjectNotFoundException;
	
    /** 
     * Extract items from a List or Array type of object property.
     * @param      obj, pseudo-object identifying object to extract from.
     * @param      countProp, String, the property which will contain an Integer count of items
     * @param      itemProp, String, the property which will contain the List or Array of items
     * @return     List
     * @exception  SAFSException
     **/
	public abstract java.util.List extractListItems (Object obj, String countProp, String itemProp) 
	                                                   throws SAFSException;

    /** 
     * Get a single list item from a List or Array type of object property.
     * @param      obj, pseudo-object identifying object to extract from.
     * @param      i, int, index into object list or array.
     * @param      itemProp, String, the property which will contain the List or Array of items
     * @return     String
     * @exception  SAFSException
     **/
    public abstract String getListItem (Object obj, int i, String itemProp) 
                                          throws SAFSException;

    /** 
     * Extract a menu hierarchy from a MenuBar pseudo-object reference.
     * Often this can be served by the extracMenuItems function.
     * This is generally for JMenuBars.
     * @param      obj, pseudo-object reference
     * @return     org.safs.Tree
     * @exception  SAFSException
     **/
	public abstract Tree extractMenuBarItems (Object obj) throws SAFSException;

    /** 
     * Extract a menu hierarchy from a object.
     * This is generally for JPopupMenus, JMenus, and JMenuItems (JMenu is subclass of JMenuItem).
     * @param      obj, pseudo-object reference
     * @return     org.safs.Tree
     * @exception  SAFSException
     **/
	public abstract Tree extractMenuItems (Object obj) throws SAFSException;

	protected TestRecordData trdata;
	/**
	 * A one-time setting of the RTestRecordData to be used by this instance.
	 * This normally happens as part of construction, or immediately following 
	 * construction.
	 **/
    public void setTestRecordData(TestRecordData trdata) { this.trdata = trdata; }
    public TestRecordData getTestRecordData(){ return trdata;}

	protected LogUtilities log;
	/**
	 * A one-time setting of the LogUtilities to be used by this instance.
	 * This normally happens as part of construction, or immediately following 
	 * construction.
	 **/
    public void setLogUtilities(LogUtilities log) { this.log = log; }
    public LogUtilities getLogUtilities(){ return log;}
    
}

