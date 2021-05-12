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
package org.safs.rational;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.safs.DDGUIUtilities;
import org.safs.Domains;
import org.safs.GuiChildIterator;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.Tree;
import org.safs.logging.LogUtilities;
import org.safs.rational.flex.CFFlexMenuBar;
import org.safs.rational.win.CFWinMenuBar;
import org.safs.rational.wpf.CFWPFMenuBar;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.object.interfaces.BrowserTestObject;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelSubitemTestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.map.IMappedTestObject;
import com.rational.test.ft.object.map.MappedTestObject;
import com.rational.test.ft.object.map.SpyMappedTestObject;
import com.rational.test.ft.script.IOptionName;
import com.rational.test.ft.value.MethodInfo;

/**
 * Rational extension to DDGUIUtilities.
 * Handles registering and storing Rational-specific RApplicationMap objects for
 * Rational TestObject lookup and caching.  Also provides Rational-specific functions
 * for basic GUI object operations (setActive, waitFor, etc...)
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *   <br>   JUN 04, 2003    (DBauman) Original Release
 *
 * @author Carl Nagle, JUN 26, 2003
 *         Extended DDGUIUtilities.
 *
 * @author Carl Nagle, JUL 03, 2003
 *         Updated documentation to reflect previous changes.
 *
 * @author Carl Nagle, NOV 05, 2003
 *         waitForObject no longer catches RuntimeException.  TestStepProcessor will 
 *         catch these from various sources.
 *
 * @author Jeremy J. Smith, JUN 03, 2005
 *         Search for additional menuitem classes in localWaitForObject()
 *         (previously JCheckBoxMenuItem/CheckBoxMenuItemUI did not work)
 *         
 * @author Lei Wang, JAN 04, 2009
 * 		   Modify method getTestObject(): Trim the component type before setting it to trdata, otherwise if
 * 										  the type contains extra blank, program can not instantiate it successfully.
 * 										  See defect S0554638.
 * 	       Junwu Ma, APR 03, 2009   In waitForObject(), catch ObjectIsDisposedException that sometimes causes SetContext 
 *                                  on one same IE window can't be executed twice; try to find out the TestObject again with ignoreCache=true. 
 * @author Lei Wang, JUN 12, 2009	Modify method: extractMenuBarItems(),extractMenuItems(). 
 * 									For supporting the menu (type is .Menubar) of win domain.
 * @author Carl Nagle, NOV 05, 2003 Enhanced object search algorithm for stability.
 * @author Lei Wang, SEP 15, 2009	Modify method getHtmlDocTitle(): If the Html.Document's property 'title' is not set, IE will show its url
 * 									in the title area, so try to get the property 'url' as title when property 'title' is not set.
 * @author Junwu Ma, NOV 03, 2009   Update extractMenuBarItems() to support DotNet WPF.
 * @author Dharmesh Patel, APR 25, 2011 Added getMappedTestObject() method.
 * 
 * Copyright (C) (MSA, Inc) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class RDDGUIUtilities extends DDGUIUtilities {

  //private static Map compTypes = new HashMap();
  private static final String NO_OBJ_MSG = "NO OBJECT TO WAIT FOR:";
  private static final String NO_OBJ_MSG_TAIL = " POSSIBLY APPMAP ENTRY IS WRONG OR OBJECT IS NOT VISIBLE";

	RGuiClassData classdata = new RGuiClassData();
	
  /**
   * The default, JavaBean-happy constructor.
   * The instance cannot do much of anything without the STAFHelper, Script, and
   * RTestRecordData references provided by the other constructors.
   **/
  public RDDGUIUtilities(){;}


  /**
   * Constructor providing the STAFHelper and Script needed by the instance for
   * proper operation.  For proper operation, the caller must still setTestRecordData.
   * This constructor would be used when the RTestRecordData object to be used has
   * not yet been instanced.
   *
   * @param helper The STAFHelper for performing STAF requests.
   * @param script The Script (RationalTestScript) needed for RobotJ functions.
   **/
  public RDDGUIUtilities(STAFHelper helper, Script script, LogUtilities log) {
  	setLogUtilities(log); //must be first 
    setSTAFHelper(helper);
    setScript(script);
  }


  /**
   * Constructor providing the STAFHelper, Script, and RTestRecordData needed by the
   * instance for proper operation.
   *
   * @param helper The STAFHelper for performing STAF requests.
   * @param script The Script (RationalTestScript) needed for RobotJ functions.
   * @param data   The RTestRecordData needed for getting and updated TestRecordData.
   **/
  public RDDGUIUtilities(STAFHelper helper, Script script, 
                         RTestRecordData data, LogUtilities log) {
  	setLogUtilities(log); //must be first 
    setSTAFHelper(helper);
    setScript(script);
    setTestRecordData(data);
  }


  private Script script;
  /**
   * A one-time setting of the Script (RationalTestScript) to be used by this instance.
   * This normally happens as part of construction, or immediately following
   * construction.
   **/
  public void setScript(Script script) { this.script = script; }
  public Script getScript(){ return script;}




  /**
   * Register an AppMap for use.  If the map has not been opened by STAF,
   * then we will attempt to open it.
   * <p>
   * @param mapname the name of the map to register/open.  This will be
   *                the same name used by the SAFSMAPS service to identify
   *                the AppMap.
   * <p>
   * @param mapfile the filename of the file in the event we have to open it.
   *                This can be a relative or fullpath filename depending on
   *                how the SAFSMAPS service was configured.  If mapfile is null,
   *                the underlying routines will attempt to use mapname as the
   *                mapfile filename.
   * <p>
   * @return true if the map is registered, already registered, or successfully opened.
   **/
  public boolean registerAppMap (String mapname, String mapfile){

    try{
      if (isAppMapRegistered(mapname)) return true;

      if (openAppMap(mapname, mapfile)){
        RApplicationMap map = new RApplicationMap(mapname, script);
        registerAppMap(mapname, map);
        return true;
      }
      // what if not open...support local app map only?!
      else{
        RApplicationMap map = new RApplicationMap(mapname, script);
        registerAppMap(mapname, map);
        return true;      	
      }
    }
    catch(NullPointerException np){  
    	log.logMessage(trdata.getFac(), np.getMessage(), FAILED_MESSAGE); }
    return false;
  }

	/**
	 * clear a single local App Map cache of all old references.
	 * Here we also override and clear all registered TestObjects.
	 * @param mapname of the App Map to clear
	 * @see Script#localUnregisterAll()
	 */
	public void clearAppMapCache(String mapname){
		super.clearAppMapCache(mapname);
		script.localUnregisterAll();
	}

	/**
	 * clear all known local App Maps' caches.
	 * Here we also override and clear all registered TestObjects.
	 * @see Script#localUnregisterAll()
	 */
	public void clearAllAppMapCaches(){
		super.clearAllAppMapCaches();
		script.localUnregisterAll();
	}
	
  /**
   * Attempt to retrieve either a TopLevelTestObject or BrowserTestObject, based on if it 
   * is a browser or not.
   * <p>
   * @param obj a TestObject
   * <p>
   * @return TestObject, either TopLevelTestObject or BrowserTestObject
   **/
  public TopLevelTestObject getParentTestObject (TestObject obj) {
    if (obj instanceof BrowserTestObject) {
      return (BrowserTestObject)obj;
    } else {
      GuiTestObject guiObj = new GuiTestObject(obj);
      TopLevelTestObject parent;
      if (guiObj instanceof TopLevelTestObject){
        return (TopLevelTestObject) guiObj;
      }else{
        return new TopLevelTestObject(guiObj.getTopParent());
      }
    }
  }
  /**
   * Attempt to retrieve a TestObject using the named references; this version
   * calls the other version, passing 'false' to param 'ignoreCache'
   * <p>
   * First we see if the object is already cached in the TestRecordData.<br>
   * Then we will see if it is already cached in our internal AppMap storage.<br>
   * Then we will attempt to locate the object in the available RobotJ Object Map.
   * <p>
   * If not in any of these places, we will then attempt to locate the object by
   * traversing the TestObject hierarchy in all supported test domains.
   * The routine will require recognition strings retrieved from the external
   * SAFSMAPS AppMap to identify parent and child components during this search.
   * <p>
   * The mapname is expected to already be open and registered prior to this call.
   * If it is not, we will make an attempt to open/register the AppMap using the
   * mapname provided.  However, success on this is dependent on how the SAFSMAPS
   * service was configured at launch and the extent of path information available
   * in mapname.
   * <p>
   * @param mapname the name of the stored AppMap
   * <p>
   * @param windowName the name of the parent object to retrieve.  This
   *                   cannot be null as we always look for a parent object
   *                   prior to walking the tree to look for its child.
   * <p>
   * @param childName the name of the child object to find.  If the parent
   *                  object is actually the item of interest, then provide
   *                  windowName as the childName as well.
   * <p>
   * @return TestObject proxy for the object or null if it was not found or an
   *                    error occurred.
   **/
  public TestObject getTestObject(String mapname, String windowName, String childName) {
    return getTestObject(mapname, windowName, childName, false);
  }
  /**
   * Attempt to retrieve a TestObject using the named references; this version
   * calls the other version, passing 'false' to param 'ignoreCache', and passing
   * null to 'gather'
   * <p>
   * First we see if the object is already cached in the TestRecordData.<br>
   * Then we will see if it is already cached in our internal AppMap storage.<br>
   * Then we will attempt to locate the object in the available RobotJ Object Map.
   * <p>
   * If not in any of these places, we will then attempt to locate the object by
   * traversing the TestObject hierarchy in all supported test domains.
   * The routine will require recognition strings retrieved from the external
   * SAFSMAPS AppMap to identify parent and child components during this search.
   * <p>
   * The mapname is expected to already be open and registered prior to this call.
   * If it is not, we will make an attempt to open/register the AppMap using the
   * mapname provided.  However, success on this is dependent on how the SAFSMAPS
   * service was configured at launch and the extent of path information available
   * in mapname.
   * <p>
   * @param mapname the name of the stored AppMap
   * <p>
   * @param windowName the name of the parent object to retrieve.  This
   *                   cannot be null as we always look for a parent object
   *                   prior to walking the tree to look for its child.
   * <p>
   * @param childName the name of the child object to find.  If the parent
   *                  object is actually the item of interest, then provide
   *                  windowName as the childName as well.
   * @param ignoreCache will ignore the cached TestObject, and refind it if true.
   * <p>
   * @return TestObject proxy for the object or null if it was not found or an
   *                    error occurred.
   **/
  public TestObject getTestObject(String mapname, String windowName, String childName,
                                  boolean ignoreCache) {
    return getTestObject(mapname, windowName, childName, ignoreCache, (java.util.List)null);
  }  
  
  /**
   * Attempt to retrieve a TestObject using the named references.
   * <p>
   * First we see if the object is already cached in the TestRecordData.<br>
   * Then we will see if it is already cached in our internal AppMap storage.<br>
   * Then we will attempt to locate the object in the available RobotJ Object Map.
   * <p>
   * If not in any of these places, we will then attempt to locate the object by
   * traversing the TestObject hierarchy in all supported test domains.
   * The routine will require recognition strings retrieved from the external
   * SAFSMAPS AppMap to identify parent and child components during this search.
   * <p>
   * The mapname is expected to already be open and registered prior to this call.
   * If it is not, we will make an attempt to open/register the AppMap using the
   * mapname provided.  However, success on this is dependent on how the SAFSMAPS
   * service was configured at launch and the extent of path information available
   * in mapname.
   * <p>
   * @param mapname the name of the stored AppMap
   * <p>
   * @param windowName the name of the parent object to retrieve.  This
   *                   cannot be null as we always look for a parent object
   *                   prior to walking the tree to look for its child.
   * <p>
   * @param childName the name of the child object to find.  If the parent
   *                  object is actually the item of interest, then provide
   *                  windowName as the childName as well.
   * <p>
   * @param ignoreCache will ignore the cached TestObject, and refind it if true.
   * <p>
   * @param gather, List containing names matched, if null, then match first name
   * <p>
   * @return TestObject proxy for the object or null if it was not found or an
   *                    error occurred.
   **/
  public TestObject getTestObject(String mapname, String windowName, String childName,
                                  boolean ignoreCache, java.util.List gather) {
    String methodName = "getTestObject";

    if ((mapname == null)||(windowName == null)) return null;
    GuiChildIterator.getAbsoluteIndexStore().clear();
    RApplicationMap map = (RApplicationMap) getAppMap(mapname);

    if (map == null) {
      if (registerAppMap(mapname, mapname)) {
        map = (RApplicationMap) getAppMap(mapname);
        if (map == null) {
          Log.info("RDDG: gto1 could NOT retrieve registered AppMap "+ mapname);
          return null;
        }
      }
      // what if NOT registered?
      else{
          Log.info("RDDG: gto1 could NOT register AppMap "+ mapname);
          return null;
      }
    }

    TestObject tobj = null;
    String objectClassName = null;
    //looking for Window
    if ((childName == null)||(windowName.equalsIgnoreCase(childName))){

      if (!ignoreCache) {
        tobj = ((RTestRecordData)trdata).getWindowTestObject();
      }
      if (tobj == null) {
        tobj = map.findMappedParent(windowName, ignoreCache);
        if (tobj == null) {
          Log.info("RDDG: Could not findMappedParent "+ windowName);
          return null;
        }
        try{
        	objectClassName = tobj.getObjectClassName(); //can return null!!!
        }catch(Exception x){ // old object reference might throw Exception
        	Log.debug("RDDG: returned PARENT TestObject problem: "+ x.getClass().getSimpleName()+". Returning NULL.");
        	return null;
        }        
        ((RTestRecordData)trdata).setWindowTestObject(tobj);
      }else{
        return tobj;
      }

    }else { // looking for Child 

      if (!ignoreCache) {
        tobj = ((RTestRecordData)trdata).getCompTestObject();
      }
      if (tobj == null) {
        tobj = map.findMappedChild(windowName, childName, ignoreCache, gather);
        if (tobj == null) {
          Log.info("RDDG: Could not findMappedChild "+ childName);
          return null;
        }
        //String key = mapname+"|"+windowName +"|"+childName;
        //if (compTypes.get(key) != null) {
        //  if (debug) log.logMessage(trdata.getFac(), "have cached value of compType: "+compTypes.get(key), DEBUG_MESSAGE);
        //  // since it is cached, no need to drop down below to set it, just return.
        //  trdata.setCompType((String)compTypes.get(key));
        //  return tobj;
        //}
        try{ 
        	objectClassName = tobj.getObjectClassName(); //can return null!!!        
        }catch(Exception x){ // old object reference might throw Exception
        	Log.info("RDDG: returned TestObject problem: "+ x.getClass().getSimpleName()+". Returning NULL.");
        	return null;
        }
        trdata.setAltCompType(objectClassName);
        ((RTestRecordData)trdata).setCompTestObject(tobj);
      }else{//object was in TestRecordData, but it may now be invalid!
        try {
        	objectClassName = tobj.getObjectClassName(); //can return null!!!
        }catch(Exception x){ // old object reference might throw Exception
        	Log.info("RDDG: returned TestObject problem: "+ x.getClass().getSimpleName()+". Returning NULL.");
        	return null;
        }
        trdata.setAltCompType(objectClassName);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "...", DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "returning cached object: "+objectClassName, DEBUG_MESSAGE);
        return tobj;
      }
    }

    try{
      try {
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: compTestObject : "+ objectClassName, DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: mappedClassType: "+
                  classdata.getMappedClassType(objectClassName, tobj), DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: childName: "+childName, DEBUG_MESSAGE);
        //Trim the component type before setting it to trdata, see defect S0554638.
        String componentType = RGuiClassData.getGenericObjectType(classdata.getMappedClassType(objectClassName, tobj));
        if(componentType!=null) componentType = componentType.trim();
        trdata.setCompType(componentType);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: compType: "+trdata.getCompType(), DEBUG_MESSAGE);
        //String key = mapname+"|"+windowName +"|"+childName;
        //compTypes.put(key, trdata.getCompType()); // cache this for later.
      }
      // Carl Nagle JUL 08, 2003
      // if we are generating an uncaught exception in the above
      // RGuiClassData code, that must be resolved so we don't throw
      // the exception.
      // The reasoning: if we made it to the RGuiClassData code that
      // means we already found our TestObject via the RGuiClassData
      // code.  An error in the getGenericObjectType MUST be corrected,
      // not ignored.
      // However:
      // If the environment is not properly setup/packaged,
      // we resort to using the role lookup mechanism.
      // the below code should NEVER get executed in a properly configured
      // environment, I would think.
      catch (Exception npe) {
        //npe.printStackTrace();

        log.logMessage(trdata.getFac(), getClass().getName()+"."+methodName+
                 ": An error in RGuiClassData.getMappedClassType MUST be corrected, childName: "+
                 childName + ", tobjClassName:" + objectClassName +
                 ", tobj: " + tobj + "; most likely the definition of the class does not exist in the .dat file(s); "+npe, 
                 WARNING_MESSAGE);

        // this mechanism should not generate NullPointerException
        String arole = script.getScriptDefinition().getRole(childName);
        trdata.setCompType(arole);  // may set a null CompType
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "ScriptInterface Role: "+arole, DEBUG_MESSAGE);
        // this value may be null if the role is unknown or not set.
        if (arole == null) {
          // this mechanism may generate a NullPointerException if no
          // mapped object is found.
          SpyMappedTestObject spy =
            script.getMappedTestObject(childName);
          String role = spy.getRole();
          if (Log.ENABLED) log.logMessage(trdata.getFac(), "SpyMapped Role: "+role, DEBUG_MESSAGE);
          trdata.setCompType(role);
        }
      }
    }
    catch(Exception npe2){
      log.logMessage(trdata.getFac(), "RDDG: No Mapped TestObject named \""+ childName +"\" found.", WARNING_MESSAGE);
      Log.info("", npe2);
    }

    return tobj;
  }
  /**
   * Attempt to retrieve a TestObject using the named references.
   * <p>
   * First we see if the object is already cached in the TestRecordData.<br>
   * Then we will see if it is already cached in our internal AppMap storage.<br>
   * Then we will attempt to locate the object in the available RobotJ Object Map.
   * <p>
   * If not in any of these places, we will then attempt to locate the object by
   * traversing the TestObject hierarchy in all supported test domains.
   * The routine will require recognition strings retrieved from the external
   * SAFSMAPS AppMap to identify parent and child components during this search.
   * <p>
   * The mapname is expected to already be open and registered prior to this call.
   * If it is not, we will make an attempt to open/register the AppMap using the
   * mapname provided.  However, success on this is dependent on how the SAFSMAPS
   * service was configured at launch and the extent of path information available
   * in mapname.
   * <p>
   * @param mapname the name of the stored AppMap
   * <p>
   * @param windowName the name of the parent object to retrieve.  This
   *                   cannot be null as we always look for a parent object
   *                   prior to walking the tree to look for its child.
   * <p>
   * @param childName the name of the child object to find.  If the parent
   *                  object is actually the item of interest, then provide
   *                  windowName as the childName as well.
   * <p>
   * @param ignoreCache will ignore the cached TestObject, and refind it if true.
   * <p>
   * @param nameString, name of the actual component, (childName should end in Name=*)
   * <p>
   * @return TestObject proxy for the object or null if it was not found or an
   *                    error occurred.
   **/
  public TestObject getTestObject(String mapname, String windowName, String childName,
                                  boolean ignoreCache, String nameString) {
    String methodName = "getTestObject";

    if ((mapname == null)||(windowName == null)) return null;
    GuiChildIterator.getAbsoluteIndexStore().clear();
    RApplicationMap map = (RApplicationMap) getAppMap(mapname);

    if (map == null) {
      if (registerAppMap(mapname, mapname)) {
        map = (RApplicationMap) getAppMap(mapname);
        if (map == null) {
          Log.info("RDDG: gto2 could not open registered AppMap "+ mapname);
          return null;
        }
      }
      // what if NOT registered?
      else{
          Log.info("RDDG: gto2 could not register AppMap "+ mapname);
          return null;
      }
    }

    TestObject tobj = null;
    String objectClassName = null;
    if ((childName == null)||(windowName.equalsIgnoreCase(childName))){

      if (!ignoreCache) {
        tobj = ((RTestRecordData)trdata).getWindowTestObject();
      }
      if (tobj == null) {
        tobj = map.findMappedParent(windowName, ignoreCache);
        if (tobj == null) {
          Log.info("RDDG: Could not findMappedParent "+ windowName);
          return null;
        }
        objectClassName = tobj.getObjectClassName(); //can return null!!!
        ((RTestRecordData)trdata).setWindowTestObject(tobj);
      }else{
        return tobj;
      }

    } else {

      if (!ignoreCache) {
        tobj = ((RTestRecordData)trdata).getCompTestObject();
      }
      if (tobj == null) {
        tobj = map.findMappedChild(windowName, childName, nameString);
        if (tobj == null) {
          Log.info("RDDG: Could not findMappedChild "+ childName);
          return null;
        }
        //String key = mapname+"|"+windowName +"|"+childName;
        //if (compTypes.get(key) != null) {
        //  if (Log.ENABLED) log.logMessage(trdata.getFac(), "have cached value of compType: "+compTypes.get(key), DEBUG_MESSAGE);
        //  // since it is cached, no need to drop down below to set it, just return.
        //  trdata.setCompType((String)compTypes.get(key));
        //  return tobj;
        //}
        objectClassName = tobj.getObjectClassName(); //can return null!!!
        trdata.setAltCompType(objectClassName);
        ((RTestRecordData)trdata).setCompTestObject(tobj);
      }else{
        objectClassName = tobj.getObjectClassName(); // can return null!!!
        trdata.setAltCompType(objectClassName);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "...", DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "returning cached object: "+objectClassName, DEBUG_MESSAGE);
        return tobj;
      }
    }

    try{
      try {
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: compTestObject : "+ objectClassName, DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: mappedClassType: "+
                  classdata.getMappedClassType(objectClassName, tobj), DEBUG_MESSAGE);
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: childName: "+childName, DEBUG_MESSAGE);
        trdata.setCompType(RGuiClassData.getGenericObjectType(
                                                              classdata.getMappedClassType(objectClassName, tobj)));
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "RDDG: compType: "+trdata.getCompType(), DEBUG_MESSAGE);
        //String key = mapname+"|"+windowName +"|"+childName;
        //compTypes.put(key, trdata.getCompType()); // cache this for later.
      }
      // Carl Nagle JUL 08, 2003
      // if we are generating an uncaught exception in the above
      // RGuiClassData code, that must be resolved so we don't throw
      // the exception.
      // The reasoning: if we made it to the RGuiClassData code that
      // means we already found our TestObject via the RGuiClassData
      // code.  An error in the getGenericObjectType MUST be corrected,
      // not ignored.
      // However:
      // If the environment is not properly setup/packaged,
      // we resort to using the role lookup mechanism.
      // the below code should NEVER get executed in a properly configured
      // environment, I would think.
      catch (Exception npe) {
        npe.printStackTrace();

        log.logMessage(trdata.getFac(), getClass().getName()+"."+methodName+
                 ": An error in RGuiClassData.getMappedClassType MUST be corrected, childName: "+
                 childName + ", tobjClassName:" + objectClassName +
                 ", tobj: " + tobj + "; most likely the definition of the class does not exist in the .dat file(s); "+npe, 
                 WARNING_MESSAGE);

        // this mechanism should not generate NullPointerException
        String arole = script.getScriptDefinition().getRole(childName);
        trdata.setCompType(arole);  // may set a null CompType
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "ScriptInterface Role: "+arole, DEBUG_MESSAGE);
        // this value may be null if the role is unknown or not set.
        if (arole == null) {
          // this mechanism may generate a NullPointerException if no
          // mapped object is found.
          SpyMappedTestObject spy =
            script.getMappedTestObject(childName);
          String role = spy.getRole();
          if (Log.ENABLED) log.logMessage(trdata.getFac(), "SpyMapped Role: "+role, DEBUG_MESSAGE);
          trdata.setCompType(role);
        }
      }
    }
    catch(Exception npe2){
      log.logMessage(trdata.getFac(), "RDDG: No Mapped TestObject named \""+ childName +"\" found.", WARNING_MESSAGE);
      Log.info("", npe2);
    }

    return tobj;
  }


  /**
   * Wait up to a maximum timeout value for the specified component to "exist".
   * Uses RFT's 'WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES' between search iterations.
   * @param mapName     name of AppMap used to lookup component.
   * @param windowName  name of Window in AppMap.
   * @param compName    name of Window Component in AppMap.
   *                    If the Window IS the desired Component, then both windowName
   *                    and compName will be the same.
   * @param secTimeout  maximum wait in seconds
   * @param ignoreCache will ignore the cached TestObject, and refind it if true.
   *
   * @return  status 0 if successful; thrown exceptions if not.
   *
   * @throws SAFSObjectNotFoundException if the component or its parent cannot be found.
   **/
  private int localWaitForObject (String mapName, String windowName,
                                  String compName, long secTimeout,
                                  boolean ignoreCache)
    throws SAFSObjectNotFoundException {
    long msecTimeout = secTimeout * 1000;
    String methodName = "RDDG.localWaitForObject ";
    String messageSuffix = windowName +":"+ compName +" in AppMap: "+ mapName;
    TestObject tobj = null;
    long ms0 = System.currentTimeMillis();
    long ms1 = ms0 + msecTimeout;
    Log.info("..getTestObject before : ms0: "+ms0+",   timeout: "+ms1);
    int j=0;
    boolean tempIgnore = ignoreCache;
  	Double wait_retry_seconds = (Double) script.getOption(IOptionName.WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES);

    for(; tobj == null; j++) { // try several times
      tobj = getTestObject(mapName, windowName, compName, tempIgnore);
      tempIgnore = ignoreCache;
      long msn = System.currentTimeMillis();
      if (tobj == null) {
          Log.info("..trying again...getTestObject ..... : msn: "+msn+", j: "+j);
      }
      else{      	  
      	  // can be a similar object in a non-visible container
          try{ 
          	Log.info(" ..checking object visibility..."+ tobj);
			boolean visible = false;
		    boolean showing = false;
          	visible = RGuiObjectRecognition.isObjectVisible(tobj);
          	showing = RGuiObjectRecognition.isObjectShowing(tobj);
		    String uiclass = RGuiObjectRecognition.getUIClassID(tobj);
          	
          	Log.info(" ..object is visible: "+ visible);
          	Log.info(" ..object is showing: "+ showing);
          	Log.info(" ..object is uiClass: "+ uiclass);
          	
          	if (((! visible)||(! showing)) && // and not any of these
          	    (!((uiclass.equalsIgnoreCase("MenuBarUI")) ||
          	       (uiclass.toLowerCase().endsWith("menuitemui"))))){
		        Log.info("..NOT SHOWING: trying again...getTestObject ..... : msn: "+msn+", j: "+j);
		        tobj = null;  // see if both are valid when items scrolled from view
          	}
          }
          catch(Exception x){
          	// do nothing. retain old behavior.
          	// a found object without these properties is still a found object.
          }
      }
          
      if (msn > ms1) {
      	Log.info("...search timeout has been reached...");
      	break;
      }
      //if ((j>0)&&((j%2)==0)){
      if (tobj == null){
      	Log.info("..forcing an IgnoreCache iteration...");
      	tempIgnore=true;
      	//script.localUnregisterAll(); //already in clearAppMapCache
      	clearAppMapCache(mapName);
      	try{Thread.sleep(wait_retry_seconds.longValue()*1000);}catch(Exception x){}
      }
    }
    
    if (tobj == null) {
      throw new SAFSObjectNotFoundException(NO_OBJ_MSG+ messageSuffix+ NO_OBJ_MSG_TAIL);
    } else {
      GuiTestObject obj = new GuiTestObject(tobj);
      if (obj.exists())  return 0;      
      return 1;
    }
  }
  /**
   * Wait up to a maximum timeout value for the specified component to "exist".
   * Uses RFT's 'WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES' between search iterations.
   * @param mapName     name of AppMap used to lookup component.
   * @param windowName  name of Window in AppMap.
   * @param compName    name of Window Component in AppMap.
   *                    If the Window IS the desired Component, then both windowName
   *                    and compName will be the same.
   * @param secTimeout  maximum wait in seconds
   *
   * @return  status 0 if successful; thrown exceptions if not.
   *
   * @throws SAFSObjectNotFoundException if the component or its parent cannot be found.
   * @see #localWaitForObject(String, String, String, long, boolean)
   **/
  public int waitForObject (String mapName, String windowName,
                            String compName, long secTimeout)
    throws SAFSObjectNotFoundException {
    String methodName = "waitForObject";
    String messageSuffix = windowName +":"+ compName +" in AppMap: "+ mapName;
    if (Log.ENABLED) log.logMessage(trdata.getFac(), 
                   methodName +": "+ messageSuffix+", secTimeout:"+secTimeout, 
                   DEBUG_MESSAGE);
    try {
      return localWaitForObject(mapName, windowName, compName, secTimeout, false);
    } catch (SAFSObjectNotFoundException npe){ 
    	throw npe;
    } catch (Exception npe){//might be NullPointer or RationalTestException...
      // this happens if the caching of an object no longer applies,
      // for instance, a dialog that is dynamically recreated, we need to recreate this object
      if (Log.ENABLED) log.logMessage(trdata.getFac(), 
                     "...this happens if the caching of an object no longer applies, ignoring cache.. ", 
                     DEBUG_MESSAGE);
      return localWaitForObject(mapName, windowName, compName, secTimeout, true);
    } 
  }


  /**
   * Attempt to locate a child object based on a matching property value.
   * The check can require an exactMatch; or, a case-insensitive partial substring match if
   * the exactMatch parameter is false.
   * <p>
   * Uses StringUtils.isCaseContainsMatch
   * <p>
   * @param testobject -- the object whose children will be evaluated. This is expected to be
   *                      castable to a TestObject instance.
   * <p>
   * @param property -- the property of each child that will be evaluated.  The property is
   *                    expected to be a Value Property that can successfully return a value
   *                    cast as type String.
   * <p>
   * @param bench -- the benchmark value for the property to be used in comparisons.
   * <p>
   * @param exactMatch -- if true, an exact case-sensitive match of the bench value
   *                      provided is required to signal a match.<br>
   *                      if false, a case-insensitive partial substring match with the
   *                      bench value is performed.
   * <p>
   * @return Object, the first child object whose property value matches accordingly; or,<br>
   *         null if a suitable match is not found.
   **/
  public Object findPropertyMatchedChild(Object testobject, String property, String bench, boolean exactMatch)
    throws SAFSObjectNotFoundException {

    String methodName = "findPropertyMatchingChild";
    TestObject parentObj  = null;
    TestObject childObj   = null;
    TestObject matchObj   = null;
    TestObject[] children = null;
    boolean match         = false;
    int i = 0;

    try{
      parentObj = (TestObject) testobject;
      children = parentObj.getMappableChildren();
      for(; ((i < children.length)&&(matchObj==null));){
        childObj = children[i];
        String value = (String) childObj.getProperty(property);
        if (StringUtils.isCaseContainsMatch(bench, value, exactMatch)){
          matchObj = childObj;
        }
        else i++;
      }
    }
    catch(ClassCastException cc){;}
    catch(NullPointerException np){;}

    if (matchObj == null) throw new SAFSObjectNotFoundException(this, methodName,
                                                                "Could not find/match property \""+ property +"\" "+
                                                                "with provided bench value \""+ bench +"\"");
    return matchObj;
  }

  /**
   * Copy\cast the TestObject to either a TopLevelTestObject or TopLevelSubitemTestObject.
   * Called specifically from setActiveWindow.
   * Uses getTopParent if the object is not a TopLevel TestObject of one type or the other.
   * It is possible this routine may throw NullPointerExceptions or others.
   * @param anobj TestObject
   * @return Object cast or null
   * @see #setActiveWindow(String, String, String)
   */
  private Object _copyCastWindow (TestObject anobj){
	  Object parent = null;
      if (anobj instanceof TopLevelTestObject){
          parent = (TopLevelTestObject) anobj;
      }else if(anobj instanceof TopLevelSubitemTestObject){ 
          parent = (TopLevelSubitemTestObject) anobj;
      }else{
          parent =  new TopLevelTestObject(anobj.getTopParent());
      }
      return parent;
  }
  
  /**
   * Call activate() on the TopLevelTestObject or TopLevelSubitemTestObject provided.
   * May throw certain exceptions. Specifically used by setActiveWindow.
   * @param parent TopLevelTestObject or TopLevelSubitemTestObject.
   * Anything else will be ignored with a debug log message.
   * @see #setActiveWindow(String, String, String)
   */
  private void _activateWindow (Object parent){
	  Log.info(".._activateWindow evaluating possible popup menu class...");
      if (parent instanceof TopLevelTestObject){
    	  TopLevelTestObject sobj = (TopLevelTestObject)parent;
    	  if(classdata.isPopupMenuClass(sobj.getObjectClassName())){
    		  Log.info("..bypassing activate for TopLevelTestObject popup menu.");
   			  return;
    	  }
    	  sobj.activate();
      }else if (parent instanceof TopLevelSubitemTestObject){ 
    	  TopLevelSubitemTestObject sobj = (TopLevelSubitemTestObject)parent;
    	  if(classdata.isPopupMenuClass(sobj.getObjectClassName())){
    		  Log.info("..bypassing activate for TopLevelSubitemTestObject popup menu.");
   			  return;
    	  }
    	  sobj.activate();
      }else{
          Log.debug("..uncertain support for activate() in "+parent);
      }
  }
  
  /**
   * "Activate" the specified component's topmost parent.
   *
   * @param mapName     name of AppMap used to lookup component.
   * @param windowName  name of Window in AppMap.
   * @param compName    name of Window Component in AppMap.
   *                    If the Window IS the desired Component, then both windowName
   *                    and compName will be the same.
   *
   * @return  status 0 if successful; thrown exceptions if not.
   *
   * @throws SAFSObjectNotFoundException if the component or its parent cannot be found.
   **/
  public int setActiveWindow (String mapName, String windowName,
                              String compName) throws SAFSObjectNotFoundException {

    String methodName = "setActiveWindow";
    String messageSuffix = windowName +":"+ compName +" in AppMap: "+ mapName;
    if (Log.ENABLED) log.logMessage(trdata.getFac(), methodName +": "+ messageSuffix, DEBUG_MESSAGE);

    try{
      TestObject tobj = getTestObject(mapName, windowName, compName);
      GuiTestObject obj = new GuiTestObject(tobj);
      if (obj == null) {
        throw new SAFSObjectNotFoundException(NO_OBJ_MSG+ messageSuffix+ NO_OBJ_MSG_TAIL);
      } else {
        if (tobj instanceof BrowserTestObject) {
          BrowserTestObject br = (BrowserTestObject) tobj;
          if (br == null) {
            throw new SAFSObjectNotFoundException(this, methodName, "obj IS NULL");
          }
          Log.debug("..browser.activate: "+br);
          try{ br.activate(); }
          catch(Exception awx){ throw new NullPointerException(); }
          Log.debug("..after browser.activate: "+br);
          return 0;
        } else if (tobj instanceof FlexObjectTestObject) {
            FlexObjectTestObject br = (FlexObjectTestObject) tobj;
            if (br == null) {
              throw new SAFSObjectNotFoundException(this, methodName, "Flex obj IS NULL");
            }
            Log.debug("..flexobject.setFocus: "+ br);
            try{
            	br.setFocus();
            }catch(MethodNotFoundException e){
            	br.changeFocus();
            	br.changeFocus(true);
            }
            Log.debug("..after flexobject.setFocus: "+br);
            return 0;
        } else {
          Object parent = _copyCastWindow(obj);
          if (parent == null) {
            throw new SAFSObjectNotFoundException(this, methodName, "PARENT IS NULL");
          }
          Log.debug("..parent.activate: "+parent);
          try{ _activateWindow(parent); }
          catch(MethodNotFoundException mnf){ throw mnf; } //caught below
          catch(Exception awx){
        	  Log.debug("..handling "+ awx.getClass().getSimpleName()+" "+ awx.getMessage());
        	  throw new NullPointerException(); } //caught below
          Log.debug("..after parent.activate: "+parent);
          return 0;
        }
      }
    } catch (com.rational.test.ft.MethodNotFoundException mnfe) {
      // this can happen if the top level test object is some sort of embedded web component
      // for instance, OleEmbeddedFrame, or Applet
      Log.info("...while trying to 'setActiveWindow', MethodNotFoundException was caught and ignored.. this can happen if the top level test object is some sort of embedded web component");
      return 0;
      //throw new SAFSObjectNotFoundException("this can happen if the top level test object is some sort of embedded web component");
    } catch (NullPointerException npe){
     try {
      // this happens if the caching of an object no longer applies,
      // for instance, a dialog that is dynamically recreated, we need to recreate this object
      Log.debug("...RETRY: ignoring cache. This happens if the caching of an object no longer applies.");
      TestObject tobj = getTestObject(mapName, windowName, compName, true); // ignore cached obj
      GuiTestObject obj = new GuiTestObject(tobj);
      if (obj == null) {
        throw new SAFSObjectNotFoundException(NO_OBJ_MSG+ messageSuffix+ NO_OBJ_MSG_TAIL);
      } else {
        if (tobj instanceof BrowserTestObject) {
          BrowserTestObject br = (BrowserTestObject) tobj;
          if (br == null) {
            throw new SAFSObjectNotFoundException(this, methodName, "Browser obj IS NULL");
          }
          Log.debug("..browser.activate: "+ br);
          br.activate();
          Log.debug("..after browser.activate: "+br);
          return 0;
        } else if (tobj instanceof FlexObjectTestObject) {
            FlexObjectTestObject br = (FlexObjectTestObject) tobj;
            if (br == null) {
              throw new SAFSObjectNotFoundException(this, methodName, "Flex obj IS NULL");
            }
            Log.debug("..flexobject.setFocus: "+ br);
            br.setFocus();
            Log.debug("..after flexobject.setFocus: "+br);
            return 0;
        } else{
          Object parent = _copyCastWindow(obj);
          if (parent == null) {
            throw new SAFSObjectNotFoundException(this, methodName, "PARENT IS NULL");
          }
          Log.debug("..parent.activate: "+parent);
          _activateWindow(parent);
          Log.debug("..after parent.activate: "+parent);
          return 0;
        }
      }
     } catch (NullPointerException npe2){
    	 Log.debug("RDDG unexpected NullPointerException", npe2);
         //npe2.printStackTrace();
       // or it happened because the object truly does not exist at all or anymore...
       throw new SAFSObjectNotFoundException(NO_OBJ_MSG+ messageSuffix+ NO_OBJ_MSG_TAIL);
     }
    } catch (TargetGoneException tge){
      // this happens if the caching of an object no longer applies,
      // for instance, a dialog that is dynamically recreated, we need to recreate this object
      Log.debug("...this happens if the caching of an object no longer applies, ignoring cache.. ");
      TestObject tobj = getTestObject(mapName, windowName, compName, true); // ignore cached obj
      GuiTestObject obj = new GuiTestObject(tobj);
      if (obj == null) {
        throw new SAFSObjectNotFoundException(NO_OBJ_MSG+ messageSuffix+ NO_OBJ_MSG_TAIL);
      } else {
        if (tobj instanceof BrowserTestObject) {
          BrowserTestObject br = (BrowserTestObject) tobj;
          if (br == null) {
            throw new SAFSObjectNotFoundException(this, methodName, "obj IS NULL");
          }
          Log.debug("..browser.activate: "+br);
          br.activate();
          Log.debug("..after browser.activate: "+br);
          return 0;
        } else if (tobj instanceof FlexObjectTestObject) {
            FlexObjectTestObject br = (FlexObjectTestObject) tobj;
            if (br == null) {
              throw new SAFSObjectNotFoundException(this, methodName, "Flex obj IS NULL");
            }
            Log.debug("..flexobject.setFocus: "+ br);
            br.setFocus();
            Log.debug("..after flexobject.setFocus: "+br);
            return 0;
        } else {
            Object parent = _copyCastWindow(obj);
            if (parent == null) {
              throw new SAFSObjectNotFoundException(this, methodName, "PARENT IS NULL");
            }
            Log.debug("..parent.activate: "+parent);
            _activateWindow(parent);
            Log.debug("..after parent.activate: "+parent);
            return 0;
        }
      }
    } catch(Exception e){
      // catches Cast Exceptions and others--including our own--so re-throw
      Log.info("", e);
      throw new SAFSObjectNotFoundException(e.getMessage());
    }
  }

  /* ************************************************************************* */
  // LEAVING THESE IN (FOR NOW) IN CASE DOUG OR OTHERS ARE USING THESE....

  /** <br><em>Purpose:</em> list Map elements
   * <br><em>Side Effects:</em>
   * <br><em>Assumptions:</em>  none
   * @param                     script, Object (Script)
   * @return                    none
   **/
  public void listMapElements () {
    Enumeration e = script.getMap().elements();
    for(; e.hasMoreElements(); ) {
      Object next = e.nextElement();
      MappedTestObject obj = (MappedTestObject) next;
      if (Log.ENABLED) log.logMessage(trdata.getFac(), "Name: "+obj.getDescriptiveName()
                +", Role: "+obj.getRole()
                //+", DomainName: "+obj.getDomainName()
                //+", Id: "+obj.getId()
                +", Parent: "+(obj.getParent()==null?"null":""+obj.getParent().getDescriptiveName())
                +", SimpleDescription: "+obj.getSimpleDescription()
                +", TestObjectClassName: "+obj.getTestObjectClassName(), 
                DEBUG_MESSAGE);
    }
  }


  /** <br><em>Purpose:</em> list Map elements
   * <br><em>Side Effects:</em>
   * <br><em>Assumptions:</em>  none
   * @param                     script, Object (Script)
   * @return                    none
   **/
  public void listTopLevelObjects () {
    IMappedTestObject [] objs =
      script.getMap().getTopLevelObjects();
    for(int i=0; i<objs.length; i++) {
      if (Log.ENABLED) log.logMessage(trdata.getFac(), "TopLevelObj: "+
                objs[i].getDescriptiveName(), 
                DEBUG_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> extract a list of items from a TestObject.
   ** The item is either a String (for JList) or GuiTestObject (for JComboBox), for example.
   ** <br>This is the pseudo code for GuiTestObject items:
   ** <code>
   **<br> for(int i=0; i<count.intValue(); i++) {
   **<br> GuiTestObject item = (GuiTestObject)
   **      ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
   **<br>result.add(item.getProperty(itemProp));
   **<br> }
   ** <br><br>This is the pseudo code for String items:
   **<br> for(int i=0; i<count.intValue(); i++) {
   **<br> String item = (String)
   **      ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
   **<br>result.add(item);
   **<br> }
   **</code>
   * <br><em>Assumptions:</em>  obj is of type GuiSubitemTestObject
   * @param                     obj, Object (actually of type GuiSubitemTestObject)
   * @param                     countProp, String, the property which will contain an Integer count of items
   * @param                     itemProp, String, the property which will contain
   * @return                    List
   * @exception                 SAFSException
   **/
  public java.util.List extractListItems (Object obj,
                                          String countProp,
                                          String itemProp) throws SAFSException {
    String methodName = "extractListItems";
    if (!(obj instanceof GuiSubitemTestObject)) {
      throw new SAFSException(this, methodName, "obj not instance of GuiSubitemTestObject: "+
                              obj+", "+obj.getClass().getName());
    }
    java.util.List result = new LinkedList();
    Object c = ((TestObject)obj).getProperty(countProp);
    if (!(c instanceof Integer)) {
      throw new SAFSException(this, methodName,
                              "property("+countProp+"), value not of type Integer");
    }
    Integer count = (Integer) c;
    try {
      for(int i=0; i<count.intValue(); i++) {
        String val = null;
        Object next = ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
        if (next instanceof TestObject) { // for JComboBox
          val = (String) ((TestObject)next).getProperty(itemProp);
          ((TestObject)next).unregister();
        } else if (next instanceof String) { // for JList
          val = (String) next;
        } else {
          throw new SAFSException(this, methodName, "item not of type GuiTestObject or String:"+next.getClass().getName());
        }
        result.add(val);
      }
    } catch (ClassCastException cce) {
      throw new SAFSException(this, methodName, cce.getMessage());
    } catch (com.rational.test.ft.UnsupportedSubitemException use) {
      if (Log.ENABLED) log.logMessage(trdata.getFac(), 
                     "Since got UnsupportedSubitemException, trying another method(for popups)...", 
                     DEBUG_MESSAGE);
      TestObject[] objs = (TestObject[]) ((TestObject)obj).invoke("getComponents");
      for(int i=0; i<objs.length; i++) {
        String val = null;
        if (Log.ENABLED) log.logMessage(trdata.getFac(), "........next: "+objs[i], DEBUG_MESSAGE);
        if (objs[i] instanceof GuiTestObject) { // for popup item
          val = (String) ((GuiTestObject)objs[i]).getProperty(itemProp);
          ((GuiTestObject)objs[i]).unregister();
        } else {
          throw new SAFSException(this, methodName, "item not of type GuiTestObject:"+objs[i]);
        }
        result.add(val);
      }
    }
    return result;
  }

  /** <br><em>Purpose:</em> get a list item from a TestObject.
   ** The item is either a String (for JList) or GuiTestObject (for JComboBox), for example.
   ** <br>This is the pseudo code for GuiTestObject items:
   ** <code>
   **<br> GuiTestObject item = (GuiTestObject)
   **      ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
   **<br>result.add(item.getProperty(itemProp));
   ** <br><br>This is the pseudo code for String items:
   **<br> String item = (String)
   **      ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
   **<br>result.add(item);
   **</code>
   * <br><em>Assumptions:</em>  obj is of type GuiSubitemTestObject
   * @param                     obj, Object (actually of type GuiSubitemTestObject)
   * @param                     i, int, index into object
   * @param                     itemProp, String, the property which will contain
   * @return                    String
   * @exception                 SAFSException
   **/
  public String getListItem (Object obj,
                             int i,
                             String itemProp) throws SAFSException {
    String methodName = getClass().getName()+".getListItem() ";
    if (!(obj instanceof GuiSubitemTestObject)) {
      throw new SAFSException(this, methodName, "obj not instance of GuiSubitemTestObject: "+
                              obj+", "+obj.getClass().getName());
    }
    try {
      String val = null;
      Object next = ((GuiSubitemTestObject)obj).getSubitem(script.localAtIndex(i));
      if (next instanceof TestObject) {
        val = (String) ((TestObject)next).getProperty(itemProp);
        ((TestObject)next).unregister();
      } else if (next instanceof String) {
        val = (String) next;
      } else {
        throw new SAFSException(this, methodName, "need more code to treate item of type: "+next.getClass().getName());
      }
      if (val == null) {
        throw new SAFSException(this, methodName, "item is null");
      }
      return val;
    } catch (ClassCastException cce) {
      throw new SAFSException(this, methodName, cce.getMessage());
    }
  }

  /** required implementation for superclass **/
  public Tree extractMenuBarItems (Object obj) throws SAFSException {
		String msg = getClass().getName()+".extractMenuBarItems() ";
		Tree tree = null;
		TestObject tobj = (TestObject) obj;
		
		if(CFComponent.isDotnetDomain(tobj)){
			TestObject clazz = DotNetUtil.getClazz(tobj);
			if (DotNetUtil.isSubclassOf(clazz, CFWPFMenuBar.CLASS_MENU_NAME) || 
				DotNetUtil.isSubclassOf(clazz, CFWPFMenuBar.CLASS_CONTEXTMENU_NAME))
				tree = CFWPFMenuBar.staticExtractMenuItems(obj, 0);
			else
				tree = CFDotNetMenuBar.staticExtractMenuItems(obj, 0);
		}else if(CFComponent.isJavaDomain(tobj)){
			tree = CFMenuBar.staticExtractMenuItems(obj, 0);	
		}else if(CFComponent.isFlexDomain(tobj)){
			tree = CFFlexMenuBar.staticExtractMenuItems(obj, 0);	
		}else if(CFComponent.isWinDomain(tobj)){
			tree = CFWinMenuBar.staticExtractMenuItems(obj, 0);	
		}else {
			Log.debug(msg+" Object of domain "+tobj.getDomain().getName().toString()+" can not be processed now.");
		}
		
		return tree;
  }


  /** required implementation for superclass **/
  public Tree extractMenuItems (Object obj) throws SAFSException {
	String msg = getClass().getName()+".extractMenuItems() ";
	Tree tree = null;
	TestObject tobj = (TestObject) obj;
	
	if(CFComponent.isDotnetDomain(tobj)){
		tree = CFDotNetMenuBar.staticExtractMenuItems(obj, 0);
	}else if(CFComponent.isJavaDomain(tobj)){
		tree = CFMenuBar.staticExtractMenuItems(obj, 0);	
	}else if(CFComponent.isFlexDomain(tobj)){
		tree = CFFlexMenuBar.staticExtractMenuItems(obj, 0);	
	}else if(CFComponent.isWinDomain(tobj)){
		tree = CFWinMenuBar.staticExtractMenuItems(obj, 0);	
	}else{
		Log.debug(msg+" Object of domain "+tobj.getDomain().getName().toString()+" can not be processed now.");
	}
	
	return tree;
  }


  protected void listAllProperties (TestObject obj, String str) {
  	if (!Log.ENABLED) return;
    log.logMessage(trdata.getFac(), 
                   " ...............listProperties: "+str, 
                   DEBUG_MESSAGE);
    listProperties(obj);
    log.logMessage(trdata.getFac(), 
                   " .......listNonValueProperties: "+str, 
                   DEBUG_MESSAGE);
    listNonValueProperties(obj);
    log.logMessage(trdata.getFac(), " .....", DEBUG_MESSAGE);
  }

  protected void listProperties (TestObject obj) {
  	if (!Log.ENABLED) return;
    Map m = obj.getProperties();
    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
      String key = (String)i.next();
      Object next = m.get(key);
      log.logMessage(trdata.getFac(), "key: "+key+": "+next, DEBUG_MESSAGE);
    }
  }
  protected void listNonValueProperties (TestObject obj) {
  	if (!Log.ENABLED) return;
    Map m = obj.getNonValueProperties();
    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
      String key = (String)i.next();
      Object next = m.get(key);
      log.logMessage(trdata.getFac(), "key: "+key+": "+next, DEBUG_MESSAGE);
    }
  }
  protected void listMethods (TestObject obj) {
  	if (!Log.ENABLED) return;
    MethodInfo[] mi = obj.getMethods();
    for(int i=0; i<mi.length; i++) {
      log.logMessage(trdata.getFac(), 
                     "next method: "+mi[i].getName()+ ", "+mi[i], 
                     DEBUG_MESSAGE);
    }
  }

  /**
   * @param obj descendant TestObject in an Html.HtmlDocument
   * @return Html.HtmlDocument TestObject or null
   */
  public static TestObject getHtmlDocumentObject(TestObject obj){
		TestObject doc = null;
		TestObject child = obj;
		String docclass = null;
		boolean done = false;
		while(!done){
			doc = child.getParent();
			if (doc == null) break;
			if (doc == child ) break;
			try{
				docclass = doc.getObjectClassName();
				if(docclass.equalsIgnoreCase("HTML.HTMLDOCUMENT")){
					done = true;
				}
			}catch(Exception x){;}
			if(!done){
				child = doc;
				doc = null;
			}
		}
		return done ? doc:null;
  }

    /**
     * @param obj TestObject expected to support 'title' or '.title' properties
     * @return value of title or .title property or an empty String.
     */
	public static String getHtmlDocTitle(TestObject obj){
		String theCaption = "";
		try{ theCaption = (String) obj.getProperty("title");}
		catch(Exception p1){
			Log.debug("RDDG.getHtmlDocTitle IGNORE exception from HTMLBrowser: "+ p1.getClass().getSimpleName());
		}
		if( theCaption.length()==0){
		  	try{ theCaption = (String) obj.getProperty(".title");}
		  	catch(Exception p1){
				Log.debug("RDDG.getHtmlDocTitle IGNORE exception from HTMLBrowser: "+ p1.getClass().getSimpleName());
		  	}
		}
		if(theCaption.length()==0){
			try{
				//If Html.Document's property '.title' or 'title' is not set
				//In the IE title area, the URL will be shown, we will try to get it.
				theCaption = (String) obj.getProperty("url");
			}catch(Exception e){
				Log.debug("RDDG.getHtmlDocTitle IGNORE exception from HTMLBrowser: "+ e.getClass().getSimpleName());
			}
		}
		if(theCaption.length()==0){
			try{
				Object captionObj = obj.getProperty(".url");
				if(captionObj instanceof String){
					theCaption = (String) captionObj;
				}else if(captionObj instanceof com.rational.test.ft.script.Href){
					com.rational.test.ft.script.Href href = (com.rational.test.ft.script.Href) captionObj;
					theCaption = href.getHref();
				}
			}catch(Exception e){
				Log.debug("RDDG.getHtmlDocTitle IGNORE exception from HTMLBrowser: "+ e.getClass().getSimpleName());
			}
		}
		
		Log.info("...HTMLDocument caption:"+ theCaption);
		return theCaption;	  
	}

    /**
     * Cast TestObject to GuiTestObject or create new GuiTestObject(ob).
     * @param obj TestObject expected to be cast or created as GuiTestObject.
     * @return GuiTestObject or null.
     */
	public static GuiTestObject getGuiTestObject(TestObject obj){
        GuiTestObject tobj = null;
        try{
    	    tobj = (GuiTestObject) obj;
        }catch(ClassCastException cc){
    	    try{ tobj = new GuiTestObject(obj);}
    	    catch(Exception x){
    		    Log.debug("Ignoring RDDG getGuiTestObject new GuiTestObject "+ x.getClass().getSimpleName());
    	    }    	    
        }catch(Exception x){
		    Log.debug("Ignoring RDDG getGuiTestObject casting "+ x.getClass().getSimpleName());
        }
        return tobj;
	}
	
	/**
	 * Create appropriate TestObject from SpyMappedTestObject
	 * @param SpyMappedTestObject - SpyMappedTestObject
	 * @return TestObject
	 */
	public static TestObject getMappedTestObject(SpyMappedTestObject spy){
		
		String className;
		Class newClass = null;
		Class[] argsSpyClass = new Class[] {SpyMappedTestObject.class};
		TestObject to = null;
		Object[] spyObject;
	
		if (spy != null){
			
			spyObject = new Object[]{spy};
			className = spy.getTestObjectClassName();
			
			Log.debug("RDDG getMappedTestObject SpyMappedTestObject class name:"+ className);
			
			try{ newClass = Class.forName(className); }catch(ClassNotFoundException cnfe){}
			
			if (newClass == null) {
				String genericClassName = "com.rational.test.ft.object.interfaces."+ className;
				try {newClass = Class.forName(genericClassName);}catch(ClassNotFoundException cnfe){}
			}
			
			if (newClass == null) {
				String flexClassName = "com.rational.test.ft.object.interfaces.flex."+ className;
				try {newClass = Class.forName(flexClassName);}catch(ClassNotFoundException cnfe){}
			}
			
			if (newClass == null) {
				String sapClassName = "com.rational.test.ft.object.interfaces.sap."+ className;
				try {newClass = Class.forName(sapClassName);}catch(ClassNotFoundException cnfe){}
			}
			
			if (newClass == null) {
				String siebelClassName = "com.rational.test.ft.object.interfaces.siebel."+ className;
				try {newClass = Class.forName(siebelClassName);}catch(ClassNotFoundException cnfe){}
			}
			
			if (newClass != null){
				
				try {
					
					Constructor constructor	= newClass.getConstructor(argsSpyClass);
					Log.debug("RDDG getMappedTestObject constructor:"+ constructor.getName());
	
					to =  (TestObject) constructor.newInstance(spyObject);		
					
				} catch (Exception e) {
					Log.debug("RDDG getMappedTestObject throw exception for the class: " + className +" erroMessage: "+  e.getMessage());				
				}
				Log.debug("RDDG getMappedTestObject SpyMappedTestObject tostring:"+  to.toString());
			}
		}		
		return to;
	}
}
