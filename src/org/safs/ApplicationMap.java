/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.Hashtable;

import org.safs.staf.service.map.AbstractSAFSAppMapService;

/**
 * The Application Map (App Map) provides storage for Objects referencing the Application 
 * being tested.  These may be actual application objects, or object proxies.  This 
 * is dependent upon the implementation of the tools actually used for testing.
 * <p>
 * Each App Map represents one of any number of separate storage maps used 
 * for testing the application.  Each App Map is known by its mapname which is 
 * retrievable with getMapName().  The name of the App Map is expected to match a  
 * corresponding App Map opened and handled by the SAFSMAPS service in STAF.
 * <p>
 * A Valid STAFHelper object must have been provided by a subclass implementation 
 * or via a direct call to setSTAFHelper prior to using an instance of this class.
 * 
 * @author Carl Nagle
 * @since JUN 26, 2003
 *
 **/
public class ApplicationMap extends STAFRequester {

	// map contains named references to Window objects and Child components.
	// each String Window key points to a Hashtable of named Child GuiTestObjects
	private Hashtable map  = new Hashtable(20);
	public void clearMap() {map.clear();}
	
	/** The name of this AppMap.  Must be identical to name used by SAFSMAPS service. **/	
	protected String mapname = null;
	

	/**
	 * Subclasses should call this constructor or insure mapname gets set.
	 * <p>
	 * @param mapname the name given to this AppMap.  This should be identical to 
	 *        the name of an AppMap handled by the SAFSMAPS service.
	 **/
	public ApplicationMap(String mapname)
	{ 
		this.mapname = mapname; 
	}

	/** 
	 * @return String the stored name of this AppMap
	 **/
	public String getMapName() { return mapname; }


	/**
	 * Retrieves the stored recognition string for the parent's child.
	 * Uses the SAFSMAPS service via STAF.  
	 * <p>
	 * A Valid STAFHelper object must have been provided by a subclass implementation 
	 * or via a direct call to setSTAFHelper prior to this call.
	 * <p>
	 * @param parentName the name of the parent object or section in the AppMap.
	 * <p>
	 * @param childName the name of the parent's child to retrieve.  
	 * <p>
	 * @return String recognition string or null if not found or an error occurs.
	 **/
	public String getChildGUIID(String parentName, String childName){
		try{ return staf.getAppMapItem(mapname, parentName, childName);}
		catch(NullPointerException np){ return null; }
	}			


	/**
	 * Retrieves the stored recognition string for the parent.
	 * Uses the SAFSMAPS service via STAF.
	 * <p>
	 * @param parentName the name of the parent object as stored.
	 * <p>
	 * @return String recognition string or null if not found or an error occurs.
	 **/
	public String getParentGUIID(String parentName){
		return getChildGUIID(parentName, parentName);
	}
	
	/**
	 * Retrieves the stored recognition string for the parent's child.
	 * Uses the SAFSMAPS service via STAF.  
	 * <p>
	 * A Valid STAFHelper object must have been provided by a subclass implementation 
	 * or via a direct call to setSTAFHelper prior to this call.
	 * <p>
	 * @param parentName the name of the parent object or section in the AppMap.
	 * <p>
	 * @param childName the name of the parent's child to retrieve.  
	 * <p>
	 * @param isDynamic determines whether recognition string should be checked to see if its dynamic
	 * <p>
	 * @return String recognition string or null if not found or an error occurs.
	 **/
	public String getChildGUIID(String parentName, String childName, boolean isDynamic){
		try{ return staf.getAppMapItem(mapname, parentName, childName,isDynamic);}
		catch(NullPointerException np){ return null; }
	}			


	/**
	 * Retrieves the stored recognition string for the parent.
	 * Uses the SAFSMAPS service via STAF.
	 * <p>
	 * @param parentName the name of the parent object as stored.
	 * <p>
	 * @param isDynamic determines whether recognition string should be checked to see if its dynamic
	 * <p>
	 * @return String recognition string or null if not found or an error occurs.
	 **/
	public String getParentGUIID(String parentName, boolean isDynamic){
		return getChildGUIID(parentName, parentName, isDynamic);
	}

	/**
	 * Was the recognition string tagged by SAFSMAPS with ISDYNAMIC?
	 * SAFSMAPS will now tag recognition strings with ISDYNAMIC if it is requested to 
	 * do so by the caller:
	 * <p>
	 * ISDYNAMIC;RECOGNITION=&lt;GUIID recognition>
	 * <p>
	 * The routine will also return true if the recognition string is "CurrentWindow".
	 * @param recognition -- the non-null, potentially tagged recognition string from getGUIID calls.
	 * @return true if ";Recognition" and "ISDYNAMIC" is prefixed in the string of if the recognition 
	 * string is "CurrentWindow"
	 * @see org.safs.staf.service.map.SAFSAppMapService
	 */
	public static boolean isGUIIDDynamic(String recognition){
		boolean isDynamic = false;
		try{
			String [] recParts = recognition.split(AbstractSAFSAppMapService.SAM_SERVICE_TAGGED_PREFIX);
			if(recParts.length > 1){
				if(recParts[0].indexOf(AbstractSAFSAppMapService.SAM_SERVICE_PARM_ISDYNAMIC)> -1)
					isDynamic = true;
			}else if (recParts[0].equalsIgnoreCase(AbstractSAFSAppMapService.SAM_CURRENTWINDOW_ITEM))
					isDynamic = true;
				
		}catch(NullPointerException np){}
		return isDynamic;
	}

	/**
	 * Extract the recognition string portion of a GUIID "tagged" by SAFSMAPS.
	 * SAFSMAPS will now tag recognition strings with flags like "ISDYNAMIC" if it is requested to 
	 * do so by the caller.  When tagged, the recognition portion of the string appears as below:
	 * <p>
	 * &lt;tags>;Recognition=&lt;GUIID recognition>
	 * <p>
	 * If the item is not "tagged" then it will be returned unmodified.
	 * 
	 * @param recognition -- the non-null, potentially tagged recognition string from getGUIID calls.
	 * @return the recognition portion of the string if tagged, or the unmodified input string if not.
	 * @see org.safs.staf.service.map.SAFSAppMapService
	 */
	public static String extractTaggedGUIID(String recognition){
		try{
			int i = recognition.indexOf(AbstractSAFSAppMapService.SAM_SERVICE_TAGGED_PREFIX);
			if (i > 0) return recognition.substring( i + 13);
		}catch(NullPointerException np){}
		return recognition;
	}
	
	/**
	 * Stores the actual child Object for the named parent/child.  This object will 
	 * be implementation specific for different tool-dependent subclasses. For example, 
	 * this will contain a TestObject reference for certain Rational implementations.
	 * <p>
	 * @param parentName the name of the parent as stored in the AppMap.
	 * <p>
	 * @param childName the name of the parent's child.  
	 * <p>
	 * @param child the actual child object.
	 **/
	public void setChildObject(String parentName, String childName, Object child){

		String parentUC = parentName.toUpperCase();
		String childUC = childName.toUpperCase();
		Hashtable section = (Hashtable) map.get(parentUC);
		if (section == null) section = new Hashtable(20);
		section.put(childUC, child);
		map.put(parentUC, section);
	}


	/**
	 * Retrieves the object stored for the given parent's child.  This object will 
	 * be implementation specific for different tool-dependent subclasses. For example, 
	 * this will contain a TestObject reference for certain Rational implementations.
	 * The returned object will need to be cast to the appropriate type by the caller.
	 * <p>
	 * @param parentName the name of the parent as stored in the AppMap.
	 * <p>
	 * @param childName the name of the parent's child.  
	 * <p>
	 * @return Object the object if found, or null.  The returned object will 
	 *                need to be cast to the appropriate type by the caller.
	 **/
	public Object getChildObject (String parentName, String childName){
		try{
			String parentUC = parentName.toUpperCase();
			String childUC = childName.toUpperCase();
			Hashtable section = (Hashtable) map.get(parentUC);
			if (section == null) return null;
			
			Object child = section.get(childUC);
			return child;
		}
		catch(Exception e){;}
		return null;
	}


	/**
	 * Retrieves the actual parent Object for the given parentName.  This object will 
	 * be implementation specific for different tool-dependent subclasses. For example, 
	 * this will contain a TestObject reference for certain Rational implementations.
	 * The returned object will need to be cast to the appropriate type by the caller.
	 * <p>
	 * @param parentName the name of the parent object stored in the AppMap.
	 * <p>
	 * @return Object the parent object if found, or null.  The returned object will 
	 *                need to be cast to the appropriate type by the caller.
	 **/
	public Object getParentObject (String parentName){
		return getChildObject(parentName, parentName);
	}


	/**
	 * Stores the actual parent Object for the given parentName.  This object will 
	 * be implementation specific for different tool-dependent subclasses. For example, 
	 * this will contain a TestObject reference for certain Rational implementations.
	 * <p>
	 * @param parentName the name of the parent object to store in the AppMap.
	 * <p>
	 * @param parent the actual parent object.
	 * <p>
	 * @return String recognition string or null if not found or an error occurs.
	 **/
	public void setParentObject(String parentName, Object parent){
		setChildObject(parentName, parentName, parent);
	}

}

