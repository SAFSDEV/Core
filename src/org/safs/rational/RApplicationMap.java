/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/


package org.safs.rational;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

import org.safs.*;
import org.safs.rational.ft.DynamicEnabler;
import org.safs.tools.stringutils.StringUtilities;

import java.util.*;
import java.util.List;

/**
 * Extends org.safs.ApplicationMap to use Rational specific mechanisms for 
 * locating objects in the tested Application.
 * <p>
 * Creates a SingletonSTAFHelper to satisfy the STAFHelper requirements of the 
 * ApplicationMap superclass if it has not already been created.
 * 
 * @author Carl Nagle
 * @since JUN 26, 2003
 * 
 * @author Carl Nagle, JUL 03, 2003
 *         Updated documentation.
 * 
 * @author Carl Nagle, MAR 01, 2005 Catch RobotJ NoSuchMethodError for non-RFT1.1 clients
 *
 * @author Jeremy J. Smith, MAR 24, 2005 
 *         Manage the old RobotJ NoSuchMethodError at compile-time as well as
 *         at run-time.
 * @author Carl Nagle, JUN 02, 2005 Get rid of getFlagsText altogether since it provides no
 *                                   production runtime value but adds a performance penalty.
 * @author Carl Nagle, NOV 30, 2005
 *         findMappedParent updated to always search the WIN domain last.
 * 
 * @author CANAGL JAN 30, 2006 Domain disabling supported.
 *   <br>   JUN 17, 2008    (LeiWang) Modify method findMappedChild(String,String,boolean,List)
 * 									  If the childPath begin with the parentPath, the child can not be obtained correctly.
 * 	 								  So remove the parentPath from the childPath, and try to obtain the child again.
 *   <br>   OCT 09, 2008   	(JunwuMa) Modify method findMappedParent(String windowName, boolean ignoreCache).
 *                                    Open FLEX domain.
 *   <br>	NOV 07, 2008	(LeiWang) Modify method findMappedParent(): add DynamicEnabler.enableNetWindows()	                           
 *   <br>	JUN 25, 2009	(CANAGL)  Cast RFT Object Map Objects as GuiTestObject in findMapped methods.	                           
 *   <br>	JUL 28, 2009	(CANAGL)  Added checks to bypass WIN domain enablement when not required.	                           
 *   <br>	AUG 07, 2009	(CANAGL)  Added support for enabling only specific process(es).
 *   <br>   MAR 08, 2011 (Dharmesh4) Added findParent(),findChid() and getRootObject() for RFSM mode.
 *   <br>   APR 25, 2011 (Dharmesh4) Modify scrpit.getMappedTestObject()call to return appropriate TestObject. 
 *   <br>	JUL 01, 2011 (Dharmesh4) Fixed findChild Null Pointer Exception.  
 *   <br>   FEB 15, 2013 (CANAGL) Fixed findChild() use of getDomain().getDescriptiveName() to getDomain().getName().toString()
 * 
 * Copyright (C) (SAS) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class RApplicationMap extends org.safs.ApplicationMap {

	/** 
	 * RobotJ object providing access to RobotJ API 
	 * All AppMaps share this object.
	 ***/
	private static Script script = null;
	
	/**
	 * RFT script root test object.
	 */
	private static RootTestObject root = null;
	
	private static String indexValue = null;
	
	/**
	 * Creates a RApplicationMap handler with the necessary name and RobotJ object.
	 * This may also create our inherited STAF handle with the SingletonSTAFHelper.
	 * <p>
	 * @param mapname the name of the map.  This should be identical to 
	 *        the name of an AppMap handled by the SAFSMAPS service.
	 * <p>
	 * @param script the RobotJ script object needed for RobotJ API calls.
	 **/
	public RApplicationMap(String mapname, Script script){ 

		super(mapname);
		if(this.script == null) this.script = script;

		try{ 
			this.setSTAFHelper(SingletonSTAFHelper.getInitializedHelper(STAFHelper.SAFS_GENERIC_PROCESS)); 
		}
		catch(SAFSSTAFRegistrationException x){			
			System.err.println(x.getMessage());
		}
	}	
	
	/**
	 * Retrieves the actual Object for the given windowName.  
	 * Looks in the internal ApplicationMap cache first.
	 * Then attempts to find this in the Script's Object Map.
	 * This command will NOT attempt to traverse the object hierarchy to locate an object.
	 * <p>
	 * @param windowName the name of the parent object stored in the AppMap.
	 * <p>
     * @param ignoreCache will ignore the cached TestObject, and refind it if true.
	 * @return TestObject the parent object if found, or null.  
	 **/
	public TestObject getParentTestObject(String windowName, boolean ignoreCache){
		
		TestObject to = null;
        if (!ignoreCache) {
          //check to see if immediate cache has already been set for this specific testrecord
          RTestRecordData rdata = null;
          try{ rdata= script.getRobotJHook().getRTestRecordData();}
          catch(Exception x){
    		  Log.debug("RAM.getPTO ignoring getTestRecordData "+ x.getClass().getSimpleName());
          }
          if(rdata != null){
        	  try{
        		  to = rdata.getWindowTestObject();
        		  //this next step may not be necessary...it is a double-check
        		  if((to != null)&&(windowName.equalsIgnoreCase(rdata.getWindowName()))){
        		  	Log.info("RAM.getPTO USING PARENT JUST FOUND IN PREVIOUS SEARCH!");
        		  	return to;
        		  }
      		  	  Log.info("RAM.getPTO CANNOT USE PARENT FOUND IN PREVIOUS SEARCH:"+ to);
        		  to = null;
        	  }catch(Exception x){
        		  Log.debug("RAM.getPTO ignoring getWindowTestObject "+ x.getClass().getSimpleName());
        	  }
          }else{
    		  Log.debug("RAM.getPTO ERROR getTestRecordData should NOT be null!");
          }
          to = (TestObject) super.getParentObject(windowName);
        } else {
          // we have to get rid of all of the child cached objects
          // because they cannot be trusted.  Unfortunately, the catching
          // of exceptions for the child objects is spread out throughout
          // all of the component functions, and it would be a lot of work
          // to figure out for each and every one where and how an Exception
          // would occur for a dynamicly created object such that the built
          // in mechanism to clear the cache for just that one object.
          // so instead of that when it is detected that the parent
          // cache needs reset, we simply reset all of the map's cached
          // objects.  Thus...
          Log.info("RAM: Resetting cached app map objects");
          clearMap();
        }
		if (to != null) {
			Log.info("RAM: Validating CACHED parent: "+ windowName +": "+to.toString());
			try{
				to.waitForExistence(2, 1);
				//to = (GuiTestObject) to.find();//waits full RFT timeout if not found
				boolean exists = to.exists();
				Log.info("RAM: CACHED parent : "+ windowName +
				         ": exists: "+ exists);// always seems to exist!
				if(!exists){
					try{to.unregister();}catch(Exception ne){;}
					clearMap();
					to = null;
				}
		    }
			catch(Exception any){
				Log.info("RAM: INVALID cached parent: "+ windowName +
						 ": Resetting cached app map objects...");
				try{to.unregister();}catch(Exception ne){;}
				clearMap();
				to = null;
			}
			if (to == null){
				Log.info("RAM: INVALID cached parent: "+ windowName);
			}else{
				Log.info("RAM: CACHED parent TestObject appears valid: "+ windowName +": "+ to.toString());
				return to;
			}
		}
		
		try{ 
			to = RDDGUIUtilities.getMappedTestObject(script.getMappedTestObject(windowName));
		}catch(Throwable e){
			Log.info("RAM: Parent NOT retrievable from Object Map: "+ windowName);
		}
		return to;
	}


	/**
	 * Retrieves the object stored for the given window's child.  
	 * Looks in the internal ApplicationMap cache first.
	 * Then attempts to find this in the Script's Object Map.
	 * This command will NOT attempt to traverse the object hierarchy to locate an object.
	 * <p>
	 * @param windowName the name of the parent as stored in the AppMap.
	 * <p>
	 * @param childName the name of the parent's child.  
	 * <p>
         * @param ignoreCache will ignore the cached TestObject, and refind it if true.
	 * @return TestObject the object if found, or null.
	 **/
	public TestObject getChildTestObject(String windowName, String childName, boolean ignoreCache){
		
		TestObject to = null;
                if (!ignoreCache) {
                  to = (TestObject) super.getChildObject(windowName, childName);
                }
		if (to != null) {
			Log.debug("RAM: FOUND cached CHILD TestObject: "+ windowName +":"+ childName);
			return to;
		}

		try{ 
			to = RDDGUIUtilities.getMappedTestObject(script.getMappedTestObject(childName));
		}
        catch(com.rational.test.ft.ObjectNotInMapException onime){ //ignore
            Log.debug("RAM: Child NOT in Object Map: "+ childName);
        }
		catch(Throwable e){
            Log.debug("RAM: Child NOT in Object Map: "+ childName +": "+ e.getClass().getSimpleName() ); //e is passed in
		}
		return to;
		
	}

	/**
	 * Override superclass static method.  Returns true if Mapped Test Object used.
	 * @param recognition
	 * @return true if the recognition is found to be a mappedTestObject.
	 */
	public static boolean isGUIIDDynamic(String recognition){
		
		try{ 
			if(script.getMap().containsId(recognition)) {
				Log.info("RMAP marking mappedTestObject "+ recognition +" as DYNAMIC.");
				return true;
			}
		}catch(Throwable x){}
		return ApplicationMap.isGUIIDDynamic(recognition);
	}


	/**
	 * Attempts to provide the RobotJ TestObject matching the named references.
	 * This is a workhorse routine that finds our objects for testing.  The 
	 * routine will interrogate any Object Map in use by RobotJ. It will check for 
	 * previously stored objects in our local storage.  And, if all else fails, 
	 * it will attempt to locate the object using the recognition strings stored 
	 * in the associated SAFS AppMap file handled by the SAFSMAPS service in STAF.
	 * <p>
	 * WIN domains are handled last so that the other domains have a chance to locate 
	 * their objects before the more generic WIN domain processes them.
	 * <p>
	 * @param windowName the name of the parent window to retrieve.
	 * <p>
     * @param ignoreCache will ignore the cached TestObject, and refind it if true.
	 * <p>
	 * @return the corresponding TestObject, or null if not found or an error occurs.
	 * 
     * @author CANAGL JAN 30, 2006 Domain disabling added.
	 **/
	@SuppressWarnings("unused")
	public TestObject findMappedParent( String windowName, boolean ignoreCache){

		TestObject parent = null;		
		RGuiObjectVector guivector = null;		
		boolean isDynamic = false;
		
		parent = getParentTestObject(windowName, ignoreCache);
		
		try{
			if (parent != null){
				// found cached object -- make sure it is a valid "registered" object
				parent.waitForExistence(2,1);
				//parent = (GuiTestObject) parent.find();//waits full RFT timeout if not found
			}			
		}catch(Throwable uoe){
			Log.debug("Parent: \""+ windowName +"\" cached object "+ uoe.getClass().getSimpleName()+". Trying hierarchy AGAIN!");
			parent = null;
		}
		
		if (parent == null) {

			// we now need to use ISDYNAMIC on App Map items
			String windowPath = staf.getAppMapItem(mapname, windowName, windowName, true);
			if (windowPath == null) return null;
		
			//prevent caching objects found with dynamic recognition strings
			isDynamic = isGUIIDDynamic(windowPath);
			
			//get recognition portion of ANY tagged windowPath (not just ISDYNAMIC) 
			windowPath= extractTaggedGUIID(windowPath);
			windowPath = StringUtils.getTrimmedUnquotedStr(windowPath);
	
			try{ 
				parent = RDDGUIUtilities.getMappedTestObject(script.getMappedTestObject(windowPath));
											
				if(parent != null) {					
					
					// problem: 
					// any of the calls below can cause a random lockup in Html, Win, Net domains:
					//
					//     parent.getProcess
					//     parent.exists()
					//     parent.getScriptCommandFlags()
					//     parent.find()
					//
					// essentially, any attempt to communicate with the remote proxy at 
					// very specific and indeterminate times.
					//
					// We have witnessed:
					// "SAFS/RobotJ: Unexpected Error: java.lang.Error: Interrupted attempt to aquire write lock, 
					//               Interrupted attempt to aquire write lock"
					// solution:
					// RFT Window->Preferences:
					//     Functional Test->Playback
					//           "Pause between" and "Retry time" increased (Ex: 2.0 seconds)
					//     Functional Test->Playback->Other Delays
					//           Delay after window activates (3 seconds, maybe more)
					//           Delay before Test Object action:  0.1
					
					try{
						parent.waitForExistence(2,1);						
					}catch(Throwable fx){
						try{parent.unregister();}catch(Throwable tx){};
						parent = null;
		                Log.info("Parent: returning NULL due to mappedTestObject "+ fx.getClass().getSimpleName());
		                return null;
					}
				}
			}
			catch(Throwable e){
			    Log.debug("Parent: Can't find first two ways, trying Domains (windowPath: "+windowPath+")..."+ e.getClass().getSimpleName());
			}
			
			if (parent == null) {
					
					guivector = new RGuiObjectVector(windowName, windowName, windowPath, script);
				
					if (Processor.isRFSMOnly() || guivector.isRftFindSearchMode()){
						/*
						 * RFMSOnly=true into tidtest.ini Or
						 * recognition string has :RFSM: mode prefix 
						 */
						ArrayList alist = new ArrayList();
						Log.debug("RAM: find calls for parent...");
						parent = findParent(guivector.removeRStringPrefixes(windowPath,alist));
						alist = null;
					} else {
									
						DomainTestObject[] domains = script.getDomains();
						int sdomains = domains.length;							
						String parentdomain = null;
						String parentprocess = null;
						DynamicEnabler.clearEnabledProcs();
						DynamicEnabler.clearEnabledDomains();
						try{ parentprocess = guivector.getParentGuiObjectRecognition().getProcessValue();}catch(Exception x){}
						if(parentprocess!=null)DynamicEnabler.addEnabledProc(parentprocess);
						try{ parentdomain = guivector.getParentGuiObjectRecognition().getDomainValue();}catch(Exception x){}
						if(parentdomain!=null)DynamicEnabler.addEnabledDomain(parentdomain);
						
						parent = guivector.getTopTestObject();
		
						// still might have to try the DynamicEnabler if still not found
						if(parent==null){
							if((parentdomain == null)||
							   ((! parentdomain.equalsIgnoreCase(Domains.HTML_DOMAIN))&&
							    (! parentdomain.equalsIgnoreCase(Domains.JAVA_DOMAIN))&&
							    (! parentdomain.equalsIgnoreCase(Domains.FLEX_DOMAIN)))){
								
						      	Log.info("..forcing DynamicEnabler.enableTopWindows...");
						      	//Attempt to dynamic enable Win32 and .NET windows in Functional Tester					
						      	try{ 
						      		if (Domains.isSwtEnabled()) DynamicEnabler.enableSWTWindows();
						      		if (Domains.isWinEnabled()) DynamicEnabler.enableWinWindows();
						      		if (Domains.isNetEnabled()) DynamicEnabler.enableNetWindows();
						      	}
						      	catch(Throwable thx){/* RobotJ\XDE Tester do not support this */}
								
						      	// now try *ONLY* WIN and NET domains for any new finds
						      	domains = script.getDomains();
						      	int edomains = domains.length;
								Log.debug("RAM: New Number of Domains reported: "+ domains.length);
								if(edomains > sdomains){
									guivector = new RGuiObjectVector(windowName, windowName, windowPath, script);
									parent = guivector.getTopTestObject();
								}else{
									Log.debug("RAM: Number of Domains did not change...");
								}
							}
						}				
					}
			}
			Log.debug("findMappedParent:"+windowName+", windowPath: "+windowPath+", parent: "+parent);
			if ((parent instanceof TestObject)&&(!isDynamic)) setParentObject(windowName, parent);				
		}
		return parent;
	}		


	/**
	 * Attempts to provide the RobotJ TestObject matching the named references.
	 * This is the workhorse routine that provides our objects for testing.  The 
	 * routine will interrogate any Object Map in use by RobotJ. It will check for 
	 * previously stored objects in our local storage.  And, if all else fails, 
	 * it will attempt to locate the object using the recognition strings stored 
	 * in the associated SAFS AppMap file handled by the SAFSMAPS service in STAF.
	 * <p>
	 * @param windowName the name of the parent window to search.
	 * <p>
	 * @param childName the name of the parent's child to find.  This value should 
	 *                  be identical to windowName if it is the window itself that 
	 *                  we are seeking.
	 * <p>
         * @param ignoreCache will ignore the cached TestObject, and refind it if true.
         * @param gather, List containing names matched, if null, then match first name
	 * @return the corresponding TestObject, or null if not found or an error occurs.
	 **/
	public TestObject findMappedChild( String windowName, String childName,
                                           boolean ignoreCache, java.util.List gather) {
		TestObject child = null;
		TestObject parent = null;		
		RGuiObjectVector guivector = null;		
		boolean isDynamic = false;
		
		parent = findMappedParent(windowName, ignoreCache);

		if (parent == null) return null;
		
		child = getChildTestObject(windowName, childName, ignoreCache);

		try{
			if (child instanceof TestObject) {
				// found cached object -- make sure it is a valid "registered" object
				child.waitForExistence(2,1);
				//child = (GuiTestObject) child.find();//waits full RFT timeout if not found
				return child;
			}
		}catch(Throwable uoe){			
			Log.debug("Child: \""+ windowName +":"+ childName +"\" cached object "+ uoe.getClass().getSimpleName()+". Walking hierarchy AGAIN!");
			child = null;
		}
		// we now need to use ISDYNAMIC on App Map items		
		String childPath = staf.getAppMapItem(mapname, windowName, childName, true);
		if (childPath == null) return null;

		//prevent caching objects found with dynamic recognition strings
		isDynamic = isGUIIDDynamic(childPath);
		
		//get recognition portion of ANY tagged childPath (not just ISDYNAMIC) 
		childPath= extractTaggedGUIID(childPath);		
		childPath = StringUtils.getTrimmedUnquotedStr(childPath);
		
		try{ 
			child = RDDGUIUtilities.getMappedTestObject(script.getMappedTestObject(childPath));
			if(child != null){
				try{					
					child.waitForExistence(2,1);				
				}catch(Throwable fx){
					Log.info("Child: mappedTestObject returning NULL due to "+fx.getClass().getSimpleName());
					return null;
				}
			}
		}
        catch(com.rational.test.ft.ObjectNotInMapException onime) {//ignore
            Log.debug("Child: Can't find first two ways, trying Domains (childPath: "+childPath+")...");
            child = null;
        }
		catch(Throwable e){
  		    Log.debug("Child: Can't find first two ways, Trying Domains (childPath: "+childPath+")..."+ e.getClass().getSimpleName());
            child = null;
		}

		if(child == null) {
			
			guivector = new RGuiObjectVector(windowName, childName, childPath, script);
			 
			if (Processor.isRFSMOnly() || guivector.isRftFindSearchMode()){
				/*
				 * RFMSOnly=true into tidtest.ini Or
				 * recognition string has :RFSM: mode prefix 
				 */
				ArrayList alist = new ArrayList();
				Log.debug("RAM1: find calls for child...");
				child = findChild(parent,guivector.removeRStringPrefixes(childPath,alist));	 
				alist = null;
			} else {
				 Log.debug("RAM1: childPath: "+childPath);
				 child = guivector.getChildTestObject(parent, gather);
			}
		}
		//If the childPath contains the parent path, maybe we can not get the child correctly
		//So we need to remove the parent path, and try again
		//Example: parentPath is "Type=Window;Caption=Application", childPath is "Type=Window;Caption=Application;\;Type=Panel;Index=1"
		//We should remove "Type=Window;Caption=Application;\;" from childPath
		if(child == null){
			if (Processor.isRFSMOnly() || guivector.isRftFindSearchMode()){
				/*
				 * RFMSOnly=true into tidtest.ini Or
				 * recognition string has :RFSM: mode prefix 
				 * Chances are very small to call this code unless child path has window path prefix 
				 */
				ArrayList alist = new ArrayList();
				
				Log.debug("RAM1: find calls for child..., prefix windowname:" +windowName );
				String parentPath = staf.getAppMapItem(mapname, windowName, windowName, true);
				if(parentPath!=null){	
					parentPath = StringUtils.getTrimmedUnquotedStr(extractTaggedGUIID(parentPath));
					if(childPath.startsWith(parentPath)){
						
						childPath = childPath.substring(parentPath.length()+GuiObjectVector.DEFAULT_CHILD_SEPARATOR.length());
						child = findChild(parent,guivector.removeRStringPrefixes(childPath,alist));
						alist = null;
					}
				}
				
			} else {
				String parentPath = staf.getAppMapItem(mapname, windowName, windowName, true);
				if(parentPath!=null){	
					parentPath = StringUtils.getTrimmedUnquotedStr(extractTaggedGUIID(parentPath));
					if(childPath.startsWith(parentPath)){
						childPath = childPath.substring(parentPath.length()+GuiObjectVector.DEFAULT_CHILD_SEPARATOR.length());
	                    Log.debug("RAM1: childPath: "+childPath);
	        			guivector = new RGuiObjectVector(windowName, childName, childPath, script);
	        			child = guivector.getChildTestObject(parent, gather);
					}
				}				
			}
		}				
		
		Log.debug("RAM2: child: "+child);
        
        if (Processor.isRFSMOnly() || guivector.isRftFindSearchMode()){
        		if (child != null && Processor.isRFSMCache()){
        			setChildObject(windowName, childName, child);
        		}
        }else if ((child instanceof TestObject)&&(!isDynamic)){
        	setChildObject(windowName, childName, child);
        }
        
		return child;
	}		

	/**
	 * Attempts to provide the RobotJ TestObject matching the named references.
	 * This is the workhorse routine that provides our objects for testing.  The 
	 * routine will interrogate any Object Map in use by RobotJ. It will check for 
	 * previously stored objects in our local storage.  And, if all else fails, 
	 * it will attempt to locate the object using the recognition strings stored 
	 * in the associated SAFS AppMap file handled by the SAFSMAPS service in STAF.
	 * <p>
	 * @param windowName the name of the parent window to search.
	 * <p>
	 * @param wildcardChildName the name of the wildcard parent's child to find.
	 *                  It should have an appmap entry which ends with Name=*
	 *                  The 'nameString' value will be replaced for the *
     * <p>
     * @param nameString, name of the actual component, (childName should end in Name=*)
	 * <p>
	 * @return the corresponding TestObject, or null if not found or an error occurs.
	 **/
	public TestObject findMappedChild( String windowName, String wildcardChildName,
                                           String nameString ) {
		TestObject child = null;
		TestObject parent = null;		
		RGuiObjectVector guivector = null;		
		boolean isDynamic = false;
		
		parent = findMappedParent(windowName, true); 

		if (parent == null) return null;
		
		// we now need to use ISDYNAMIC on App Map items
		String childPath = staf.getAppMapItem(mapname, windowName, wildcardChildName, true);
		if (childPath == null) return null;
		
		//prevent caching objects found with dynamic recognition strings
		isDynamic = isGUIIDDynamic(childPath);
		
		//get recognition portion of ANY tagged childPath (not just ISDYNAMIC) 
		childPath= extractTaggedGUIID(childPath);		
		childPath = StringUtils.getTrimmedUnquotedStr(childPath);
		
        Log.debug("RAM3a : childPath: "+childPath);
        if (childPath.length() == 0 ||
            !childPath.substring(childPath.length()-1, childPath.length()).equals("*")) {
          Log.info("childPath must end with a *  : "+childPath);
          return null;
        }
        childPath = childPath.substring(0, childPath.length()-1) + nameString;
                		
		if(child == null) {
            Log.debug("RAM3: childPath: "+childPath);
			guivector = new RGuiObjectVector(windowName, nameString, childPath, script);
			child = guivector.getChildTestObject(parent, null);
		}
        Log.debug("RAM3 again: childPath: "+childPath);
        Log.debug("RAM4: child: "+child);
		if ((child instanceof TestObject)&&(!isDynamic)) setChildObject(windowName, nameString, child);
		
		return child;
	}		
	
	/**
	 * Find parent from the root object. Use RFT find API to find main window. The search at "atChild()" 
	 * since it likely direct child of the root object.  The routine supports additional parent;\\;child 
	 * information in the recognition string.  If present, the routine will assume an FPSM (Full Path Search Mode) 
	 * for the hierarchy and use atChild()--direct child--for the find() call.
	 * @param windowProp window path
	 * @return TestObject or null
	 */
	@SuppressWarnings("unused")
	public TestObject findParent(String windowName){
		
		String methodName = this.getClass().getName() + ".findParent() ";		
		TestObject[] parent = null;		
		indexValue = null;		
		List<Property> parentListProp = null;		
		Property[] parentPro = null;
		List windowsList = null;
						
		windowsList = StringUtils.getTokenList(windowName,GuiObjectVector.DEFAULT_CHILD_SEPARATOR);
		
		/*
		 * If parent;\;child recognition string
		 */
		if ( windowsList.size() > 1){
			
			Anchor[] subitem = new Anchor[windowsList.size()];
			
			Iterator it = windowsList.iterator() ;
			
			for ( int i = 0 ; it.hasNext(); i++){
			
				parentListProp = convertStringToList((String)it.next());				
			
				/*
				 * Convert list to RFT property object
				 */
				parentPro = (Property[]) parentListProp.toArray(new Property[parentListProp.size()]);
				
				subitem[i] = SubitemFactory.atChild(parentPro);				
			}
			
			/*
			 * Main find call for main window.
			 */
			Log.debug(methodName +"call find API with atList(atChild) level");
			
			try{ parent = getRootObject().find(SubitemFactory.atList(subitem),false);}
			catch(Exception x){
				Log.debug(methodName +" returning null due to "+ x.getClass().getSimpleName() +": "+ x.getMessage());
				return null;
			}
			
		} else {
			/*
			 *  if only window recognition string
			 */			
			parentListProp = convertStringToList(windowName);	
			
			/*
			 * Convert list to RFT property object
			 */
			parentPro = (Property[]) parentListProp.toArray(new Property[parentListProp.size()]);	
				
			/*
			 * Main find call for main window.
			 */
			Log.debug(methodName +"call find API with atChild level");
			
			try{ parent = getRootObject().find(SubitemFactory.atChild(parentPro),false);}
			catch(Exception x){ /* ignore */ }
			
			/*
			 * Try one more time at descendant level, may have luck
			 */
			if (parent == null || parent.length == 0){
				Log.debug(methodName +"call find API with atDescendant level");
				try{ parent = getRootObject().find(SubitemFactory.atDescendant(parentPro),false);}
				catch(Exception x){
					Log.debug(methodName +" returning null due to "+ x.getClass().getSimpleName() +": "+ x.getMessage());
					return null;
				}
			}		
		
		}
		
		if(parent == null){
			Log.debug(methodName + "main window NOT found. find() returned null.");
			return null;
		}
		if(parent.length == 0){
			Log.debug(methodName + "main window NOT found. find() returned 0 matches.");
			return null;
		}
		
		/*
		 * see if index supply from user to select appropriate object
		 */
		Log.info(methodName + parent.length +" parent matches found.");
		int objectIndex = 0;
		if (indexValue != null){
			try{
				objectIndex = StringUtilities.convertToInteger(indexValue);
			} catch(Exception nfe){}
		
			if (objectIndex <= 0 || objectIndex > parent.length){ // index 1 base. Return null object for 0 or -1. 
				Log.debug(methodName +"main window NOT found because of Index=" +indexValue);					
				return null;
			}			
			return parent[objectIndex - 1]; //index 1 base count
		} else {
			return parent[0]; // always first object if more object found (may be wrong regx?) 
		}
	}
	
	/**
	 * Find children from parent object.
	 * @param parent - parent object
	 * @param childPath - child path (child window recognition string)
	 * @return TestObject or null
	 */
	@SuppressWarnings("unused")
	public TestObject findChild(TestObject parent, String childPath){
		
		String methodName = this.getClass().getName() + ".findChild() ";		
		indexValue = null;
		TestObject[] children = null;		
		List<Property> childListProp = null;		
		Property[] childPro = null;
		List childrenList = null;
		
			
		childrenList = StringUtils.getTokenList(childPath,GuiObjectVector.DEFAULT_CHILD_SEPARATOR);
		
		/*
		 * If child;\;child recognition string
		 */
		if ( childrenList.size() > 1){
			
			Log.info(methodName +"parent/child hierarchy provided: "+ childrenList.size());
			
			Anchor[] subitem = new Anchor[childrenList.size()];
			
			Iterator it = childrenList.iterator();
			
			for ( int i = 0 ; it.hasNext(); i++){
			
				childListProp = convertStringToList((String)it.next());				
			
				/*
				 * Convert list to RFT property object
				 */
				childPro = (Property[]) childListProp.toArray(new Property[childListProp.size()]);
				
				//subitem[i] = SubitemFactory.atChild(childPro);
				if (parent.getDomain().getName().toString().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
					subitem[i] = SubitemFactory.atChild(childPro);
				}else{ 							
					subitem[i] = SubitemFactory.atDescendant(childPro);
				}
			}
			
			/*
			 * Main find call for main children.
			 */
			//Log.debug(methodName +"call find API with atList(atChild...) level");
			Log.debug(methodName +"call find API with atList(atDescendant...) level");
			
			children = parent.find(SubitemFactory.atList(subitem),false);
			
		} else {
			/*
			 *  if only child window recognition string
			 */
			Log.info(methodName +"no parent/child hierarchy provided.");
			
						
			childListProp = convertStringToList(childPath);
					
			/*
			 * Convert list to RFT property object
			 */
			
			childPro = (Property[]) childListProp.toArray(new Property[childListProp.size()]);		
				
			/*
			 * Main RFT find call for children.
			 * Find call on flex domain is slow. atDescendant(pro) works slow so try atChild(pro)
			 *   
			 */
			if (parent.getDomain().getName().toString().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
				Log.info(methodName +"call find API with atChild level");
				children = parent.find(SubitemFactory.atChild(childPro),false);
			} 
						
			if (children == null || children.length == 0){
				Log.info(methodName +"call find API with atDescendant level");
				children = parent.find(SubitemFactory.atDescendant(childPro),false);
			}
		}
		
		/*
		 * see if index supply from user to select appropriate object
		 */
		if (children != null && children.length != 0 ){
			Log.info(methodName +children.length +" matched children found.");
			if (indexValue != null){
				int objectIndex = 0;
				 try{
					 objectIndex = StringUtilities.convertToInteger(indexValue);		
				 } catch (Exception nfe){}	 
				if (objectIndex <= 0 || objectIndex > children.length ){ // index 1 base NOT 0, -1 or 
					Log.debug(methodName + "Index alwyas start from 1, children length: "+children.length +" Index=" +indexValue);
					return null;
				}
				return children[objectIndex - 1]; // index 1 base count
			} else {
				return children[0]; // should be one child, if more children found then just return first.				
			}	
		} else {
			if(children == null){
				Log.debug(methodName + "component NOT found, children array is null.");
			}else{
				Log.debug(methodName + "component NOT found, children array length:"+children.length);
			}
			return null;
		}
	}
		
	/**
	 * Main test root object initialization
	 * @return - RootTestObject from RFT script
	 */
	private TestObject getRootObject(){
		
		String methodName = this.getClass().getName() + ".getRootObject() ";
		
		if (root == null){
			root =  RootTestObject.getRootTestObject();
		} else {
			return root;
		}
		Log.debug(methodName + "RootTestObject is: "+root);
		return root;		
	}
	
	/**
	 * RFT specific string conversion to list.
	 * @param appMapString string
	 * @return List<Property>
	 */
	private List<Property> convertStringToList(String appMapString){
		/*
		 * Since the Property RFT specific, implemented here. 
		 */
		String methodName =  this.getClass().getName() + ".convertStringToPropertyArray() ";
					
		// split string by ";" first
		StringTokenizer st = new StringTokenizer(appMapString,";");
				
		List<Property> list = new ArrayList<Property>();
			
		// split remain string by '='
		for (int i =0; st.hasMoreElements(); i++){			
			String token = st.nextToken();
			String[] tokens = token.split("=");
			Log.debug(methodName +"property: " +tokens[0]+" " +tokens[1]);
			list.add(new Property(tokens[0],(Object)tokens[1]));
		}
		
		for(int i =0; i < list.size(); i++ ){
		
			Property prop = (Property)list.get(i);		
			
			/*
			 * Check if 'index=1...x' passes by user to retrieve object
			 * Remove index=1..x from list. It is not object property
			 * Example: class=com.sas.console;class=Html.IMG;id=/dv\d+/;index=2
			 */
			if (prop.getPropertyName().equalsIgnoreCase("Index")){
				this.indexValue = prop.getPropertyValue().toString();
				Log.debug(methodName + "Index detected: " +prop.getPropertyName()+":"+this.indexValue);
				list.remove(i);
				continue;
			}
			
			/*
			 * Regular expression block '/ regx /'
			 * Example: class=com.sas.console;id=/dv\d+/
			 */
			if(prop.getPropertyValue().toString().startsWith("/") && 
					prop.getPropertyValue().toString().endsWith("/") ){
				
				list.set(i, new Property(prop.getPropertyName().toString(),
						regExp(prop.getPropertyValue().toString())));					
			}
		}
		
		return list;		
	}	
	
	/**
	 * Convert recognition string to RFT regular expression. 
	 * The regx should be specify into the '/ regx /' block.
	 * <br>
	 * Example: Input string = /gv\d+/. where gv\d+ is regular expression. / and / is regx block.
	 * @param recogString String
	 * @return RegularExpression object
	 */
	private RegularExpression regExp(String recogString) {
		/*
		 * Since the RFT specific RegularExpression class, implemented here. 
		 */
		String methodName =  this.getClass().getName() + ".regExp() ";

		if (recogString.startsWith("/") && recogString.endsWith("/")) {

			String regx = recogString.replaceAll("/$", "").replaceAll("^/", ""); // trim "/"
																	
			Log.debug(methodName + "regex string is: " + regx);
			
			return new RegularExpression(regx, false);
		} else {
			Log.debug(methodName
					+ "regx doesn't support beginning and end pattern, e.g. '/ regx /', value="
					+ recogString);
		}
		return null; // never reach this point
	}  	

}

