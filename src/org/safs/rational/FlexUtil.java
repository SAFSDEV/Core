/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational;

import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;

import com.rational.test.ft.object.interfaces.flex.FlexMenuTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexApplicationTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexMenuBarTestObject;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.SubitemFactory;


/**
 * <br><em>Purpose:</em> Support Flex applications providing common methods for calling.
 *              The format of the R-String for top Flex applicaiton: "Type=FlexApplication;Caption={***.swf}"
 *              ***.swf is flex applicaiton's file name.  
 *              
 * @see FlexUtil#getObjectClassName and FlexUtil#getCaption
 * @author JunwuMa
 * @since  OCT 07 2008  Original Release
 *         OCT 27 2008 (JunwuMa) Added getName(TestObject), getTextOfFlexMenuItem(TestObject). Added method getMatchingPathObject(TestObject,String) for identifying Flex menu object 
 *                     according to the Path information. 
 *         DEC 12 2008 (JunwuMa) Modify method drillDownRealFlexObj to fit in the changes of Flex runtime classLoader that is built with RFT8.
 *                     Flex support in RFT8 is better than in RFT7. Methods getObjectClassName and GetChildren are NO need to be used any more in RFT8.
 *         JAN 14 2009 (JunwuMa) Add method getMatchingMenuFromMenuBar(), dig in a Flex menu bar to find out the corresponding menu inside it.
 *         JAN 20 2009 (JunwuMa) Add method doSelectMenubar for being evoked from CFComponent and CFFlexMenuBar.             
 *         MAY 31 2009 (JunwuMa) Update drillDownRealFlexObj() for finding out the FlexApplication not loaded by runtime loader.
 *         SEP 18 2009 (JunwuMa) Updates to adapt drillDownRealFlexObj() to more SWFLoaders including SAS-defined SWFLoader.                 
 *         NOV 24 2010 (Carl Nagle) Updates to adapt drillDownRealFlexObj() to non-FlexApplication applications.  No SWFLoader.
 *                              Also fixed issues with getObjectClassName, and getCaption.
 **/

public class FlexUtil {

	public static final String FLEX_APP_NAME                        = "FlexApplication";
	public static final String FLEX_RUNTIMELOADING_NAME             = "runtimeloading";
	public static final String FLEX_RUNTIMELOADER_PREFIX            = "runtimeloader";
	public static final String FLEX_RUNTIMELOADER_SUFFIX            = "loader";
	public static final String FLEX_SWFLOADER_CLASSNAME_SUFFIX      = "swfloader";  // in lower case
	public static final String FLEX_SWFLOADER_CLASSNAME             = "mx.controls.SWFLoader";
	public static final String FLEX_MENUBAR_CLASSNAME    			= "mx.controls.MenuBar";
	public static final String FLEX_MENU_CLASSNAME    				= "mx.controls.Menu";
	public static final String FLEX_MENUITEMRENDERER_CLASSNAME    	= "mx.controls.menuClasses.MenuItemRenderer";
	
	//Properties of standard Flex control 
	public static final String PROPERTY_TYPE_AUTOMATIONCLASSNAME	= "automationClassName";
	public static final String PROPERTY_TYPE_AUTOMATIONINDEX		= "automationIndex";
	public static final String PROPERTY_TYPE_AUTOMATIONNAME			= "automationName";  
	public static final String PROPERTY_TYPE_CLASSNAME				= "className";
	public static final String PROPERTY_TYPE_ID						= "id";
	public static final String PROPERTY_TYPE_LABEL					= "label";
	
	public static final String PROPERTY_TYPE_NUMAUTOMATIONCHILDREN	= "numAutomationChildren";
	/**
	 * @param className -- usually extracted from a Flex TestObject.
	 * @return True if the classname provided is believed to be a common automation RuntimeLoader class.
	 */
	public static boolean isRuntimeLoader(String className){
		return className.equalsIgnoreCase(FLEX_RUNTIMELOADING_NAME)         ||
			   className.toLowerCase().startsWith(FLEX_RUNTIMELOADER_PREFIX)||
			   className.toLowerCase().endsWith(FLEX_RUNTIMELOADER_SUFFIX);
	}
	
	/**
	 * @param className -- usually extracted from a Flex TestObject.
	 * @return True if the classname provided is believed to be a common Flex SWFLoader class.
	 */
	public static boolean isSWFLoader(String className){
		return className.equalsIgnoreCase(FLEX_SWFLOADER_CLASSNAME) ||
		       className.toLowerCase().endsWith(FLEX_SWFLOADER_CLASSNAME_SUFFIX);
	}	
	
	/**
	 * Automated testing of Flex applications requires users to load supporting files in two different ways:
  	 * 1) At compile-time for applications that are enabled for RFT
     * 2) At run-time for applications that are not enabled for RFT
	 * 
	 * For the second way, the top flex window returned by getTopObjects() on Flex domain object, might be a Flex runtimeloading 
	 * object not the real application loaded. See the hierarchy of a Flex application loaded by Flex runtime-loading.
	 * 
	 *           Flex runtimeloading object        ----- top flex window returned by RFT  
	 *             /              \
	 *            /                \
	 *     FlexLoader          Flex object         -----  the Real Flex object loaded in RFT8
	 *            /
	 *           /
	 *      Flex object                        ------ no this object in RFT8 (different from in RFT7)
	 *      
	 * Purpose: Drill down a top FlexApplicationTestObject finding out the real Flex application loaded. 
	 *          Only the second loading way is considered. The first loading way will be considered later... 
	 *
	 * @param   flexObj, top FlexApplicationTestObject
	 * @return  an object(FlexApplicationTestObject) that is the real Flex application      
	 */
	public static TestObject drillDownRealFlexObj(TestObject flexObj){
		String debugmsg = "FlexUtil.drillDownRealFlexObj ";
		if(flexObj==null)
			return null;
		TestObject rtlTestObj = null;
		String className = null;
		String autoclassName = null;
		try{
			//autoclassName may be something like "FlexApplication" or "SASFlexApplication", the app class type
			//className may be the name of the app or swf, like "SampleExplorer" or "MyFlexApp"
			autoclassName = (String)flexObj.getProperty(PROPERTY_TYPE_AUTOMATIONCLASSNAME); //flexObj.getObjectClassName();
			className = (String)flexObj.getProperty(PROPERTY_TYPE_CLASSNAME);
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg + e);
			return null;
		}
		if(className==null || autoclassName==null)
			return null;
		Log.info(debugmsg +" evaluating AUTOCLASSNAME:"+ autoclassName +" CLASSNAME:"+ className);
        // check if runtime swf loader Flex object
		if(isRuntimeLoader(className)){
			Log.info(debugmsg +" seeking real FlexApplication...");
			TestObject[] _children = flexObj.getChildren();
			if(_children == null || _children.length==0){
				Log.info(debugmsg +" no children for this top-level FlexApplication");
			}
			// get SWFLoader
			for(int i=0; i<_children.length; i++){
				TestObject childobj = _children[i];
				try{
					String loaderClassname = (String)childobj.getProperty(PROPERTY_TYPE_CLASSNAME);
					if(isSWFLoader(loaderClassname)){
						Log.info(debugmsg +" skipping "+loaderClassname);
						// comment below out for the migration from RFT7 to RFT8
						// String fileLoaded = (String)childobj.getProperty(PROPERTY_TYPE_AUTOMATIONNAME);
						// wow! the real flex Application object is here
						// TestObject[] swfLoaderChildren = childobj.getChildren();
						// for(int j=0; j<swfLoaderChildren.length; j++)
						//	if(fileLoaded.equals((String)swfLoaderChildren[j].getProperty(PROPERTY_TYPE_AUTOMATIONNAME))){
						//		rtlTestObj = swfLoaderChildren[j]; // match  'flexwebDemo.swf'
						//		break;
						//	}
					}
					else {
						Log.info(debugmsg +" ACCEPTING "+loaderClassname);
						rtlTestObj = childobj; // the first non-classLoader class is the flex application loaded
						break;
					}	
				}catch(PropertyNotFoundException e){
					Log.debug(debugmsg + e);						
				}
			}
		}
		// here we check for top-level classnames NOT loaded by runtime loader
		// assume the automation was compiled into the app, or dynamically loaded by the app
		else if(!isRuntimeLoader(className)){
				Log.info(debugmsg +" no runtime loader found...");
				Log.info(debugmsg +" ACCEPTING "+className);
				rtlTestObj = flexObj; // return the FlexApplication not loaded by runtime loader			
		}
		// don't believe this will EVER get executed?
		else{
			Log.info(debugmsg+"does NOT consider this the FlexApplication to be tested...");
		}
		return rtlTestObj;
	}
	
	/**
	 *  Take property 'automationName' of the top Flex application (FlexApplicationTestObject) as Caption, although there is 
	 *  no caption for a flex application. See a top flex window's R-String: "Type=FlexApplcaiton;Caption={FlexWebDemo.swf}"
	 *  The unique file name is taken as caption to identify different Flex application. This caption format is consistent 
	 *  with it used to be in Java, DotNet, Html... 
	 *  @param  flexObj, a FlexObjectTestObject
	 *  @return its caption 
	 */
	public static String getCaption(TestObject flexObj){
		String caption = null;
		TestObject parent = flexObj.getTopParent();
		if(parent == null || flexObj.isSameObject(parent)){
			String className = "";
			className = getObjectClassName(flexObj);
			Log.info("FlexUtil.getCaption processing objectClassName: "+className);
			try{ 
				caption = flexObj.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME).toString();
			}					
			catch(PropertyNotFoundException x1){
				Log.debug("FlexUtil.getCaption: "+x1.toString());
			}
		}else{
			Log.info("FlexUtil.getCaption skipped processing object Class: "+flexObj.getClass().getSimpleName());			
		}
		return caption;
	}
	
	/*
	 * Take automationName as the name of the Flex object, and return it. Called by RGuiObjetRecognition#getName(TestObject).
	 * It should return nothing for mx.controls.MenuBar if want to generate correct menu path information in STAFPC.genRecogString, like "Type=FlexMenuBar;index=1;Path=File->Exit".
	 *  Path=File->Exit
	 * See FlexUtil#getMatchingPathObject(TestObject, String).
	 * */
	public static String getName(TestObject flexObj){
		if(!(flexObj instanceof FlexObjectTestObject))
			return new String("");
		String name = null;
		String className = (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_CLASSNAME).toString();
		String autoname = (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
        if(!autoname.startsWith("index:") && !className.equals(FLEX_MENUBAR_CLASSNAME))
        	name = autoname; 
		return (name!=null)? name : new String("");
	}
	/*
	 * Returns the content of the Flex menuitem. Called by RGuiObjetRecognition#getText(TestObject).
	 * To generate path menu information(like Path=File->Exit), it is needed to return the text for Flex menuitem. 
	 * That is required by the algorithm of STAFPC.genRecogString.	
	 */
	public static String getTextOfFlexMenuItem(TestObject flexObj){
		if(!(flexObj instanceof FlexObjectTestObject))
			return new String("");
		
		String className = (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_CLASSNAME).toString();
		if (className.equals("mx.controls.menuClasses.MenuItemRenderer")) 
	        return (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);   
	    else 
	        return new String("");
	}	
	
	/**
	 * returns the class name for FlexObjectTestObject. For top Flex application whatever its file name is, its 
	 * automationClassName is often 'FlexApplication'. This cannot be taken as the UNIQUE class type for identifying 
	 * a specific flex window because all FlexApplications might have that class name.  Instead, the object className 
	 * should be unique across Flex applications.  className should give us that. 
	 * See a R-String for a top flex window like "Type=FlexApplication;Caption=...."
	 * @param flexObj, a FlexObjectTestObject
	 * @return class name
	 */
	public static String getObjectClassName(TestObject flexObj){
		String className = null;
		
		if(flexObj instanceof FlexObjectTestObject)
			// Why getObjectClassName() on flex object can't return the actual class like 'mx.controls.List'?
			// getObjectClassName() returns the same string as its property 'automationClassName',that is 'FlexList'. 
			// using property className instead!! 
			className = (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_CLASSNAME);
		else
			className = flexObj.getObjectClassName();
        return className;
	}
	
	/**
	 * returns the class Index for FlexObjectTestObject.
	 * @return class name
	 */
	public static String getObjectClassIndex(TestObject flexObj){
		String classIndex = null;
		
		if(flexObj instanceof FlexObjectTestObject){
			classIndex = (String)flexObj.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONINDEX);
		}
	      return classIndex;
	}


    /**
     * Called by RGuiObjectVector#getChildren for solving duplicate object problem. In the hierarchy of Flex software 
     * under test, a container's child objects can be unexpectedly listed by calling getChildren on the container's 
     * parent. These child objects are duplicated. The duplicate can be solved by using property numAutomationChildren 
     * and method getAutomationChildAt(int), which returns the real child objects that belong to the Flex container. 
     * @param obj, FlexObjectTestObject
	 * @return array of TestObjects or an empty array of new TestObject[0].
     */
	public static TestObject[] getChildren(TestObject obj){
		TestObject[] _children = null;
		FlexObjectTestObject flexobj = null;
		try{
			flexobj = (FlexObjectTestObject)obj;
			// for FlexObjectTestObject, using 'numAutomationChildren' to get its real children.
    		String num = (String)flexobj.getProperty(PROPERTY_TYPE_NUMAUTOMATIONCHILDREN); // might throws PropertyNotFoundException
    	    
    	    int count = Integer.parseInt(num); // might throws NumberFormatException
    	    if(count==0)
    	    	_children = new TestObject[0];
    	    else{
    	    	_children = new FlexObjectTestObject[count];
    	    	for(int i=0; i<count;i++)
    	    		_children[i] = flexobj.getAutomationChildAt(i);
    	    }
		}catch(Exception e){	
		    Log.info("FlexUtil.getChildren(TestObject) Exception: "+e);
	    	_children = flexobj.getChildren();
		}
		return _children;
	}
	
	/**
	 * Retrieves the resulting object identified with the Path information applied to flexmenuBar.
	 * Called by {@link RGuiObjectRecognition#getMatchingPathObject(Object, String)}.
	 * In R-String "Type=FlexMenuBar;index=1;Path=File->Exit", mx.controls.Menu matches 'File'; 
	 * mx.controls.menuClasses.MenuItemRenderer matches 'Exit'.
	 * 
	 * @param flexmenuBar, a FlexMenuBarTestObject (not considering FlexTreeTestObject)
	 * @param path
	 * @return a FlexObjectTestObject that matches the path.
	 */
	public static FlexObjectTestObject getMatchingPathObject(Object flexmenuBar, String path){
		String debugmsg = FlexUtil.class.getName()+".getMatchingPathObject(): ";
		if(flexmenuBar == null || path == null){
			Log.debug(debugmsg+" null flexmenu object or null path");
			return null;
		}
		FlexMenuBarTestObject menuBarobj = null;
		try{
			menuBarobj = (FlexMenuBarTestObject)flexmenuBar;
		}catch(ClassCastException  e){
			Log.debug(debugmsg+" Cast exceptioin. The Object passed in should be FlexMenuBarTestObject.");
			return null;
		}
		
		StringTokenizer toker = new StringTokenizer(path, "->"); // File->Open->Exit
		FlexObjectTestObject parent = menuBarobj;
		
		int idxOfToken = 0;
		boolean match = false;
		while(toker.hasMoreTokens()){
			String pathToken = toker.nextToken();
			String num = (String)parent.getProperty(PROPERTY_TYPE_NUMAUTOMATIONCHILDREN);	
			int count = 0;
			try{count = Integer.parseInt(num);
			}catch(NumberFormatException e){}
			
			// starting to match the path token 
			match = false;
			for(int i=0; i<count;i++){
				FlexObjectTestObject obj = parent.getAutomationChildAt(i);
				String className = (String)obj.getProperty(PROPERTY_TYPE_CLASSNAME);
				String autoName = (String)obj.getProperty(PROPERTY_TYPE_AUTOMATIONNAME);
				//Flex menu object is responsible for the first node in path
				String comparedClassName = (idxOfToken==0? FLEX_MENU_CLASSNAME : FLEX_MENUITEMRENDERER_CLASSNAME);
				if(autoName.equals(pathToken) && className.equals(comparedClassName)){
					// Yeah! obj matches this path token
					parent = obj;
					match = true;
					break;
				}
			}
			if(!match) // break if not match on this level 
				break;
			++idxOfToken;
		} // end of while
		return (match? parent:null);
	}	

	/**
	 *  <em>Purpose:</em> Dig in a Flex menu bar to find out the corresponding menu inside it according menu path.
	 *  @param  flexmenuBar
	 *  @param  menuItemPath
	 *  @return a FlexMenuTestObject (the corresponding menu) 
	 */
	public static FlexObjectTestObject getMatchingMenuFromMenuBar(TestObject flexmenuBar, String menuItemPath){
		String debugmsg = FlexUtil.class.getName()+".getMatchingFlexMenu(): ";
		
	    if(flexmenuBar == null || menuItemPath == null){
			Log.debug(debugmsg+" null flexmenu object or null path");
			return null;
		}

	    FlexMenuBarTestObject menuBarobj = null;
		try{
			menuBarobj = (FlexMenuBarTestObject)flexmenuBar;
		}catch(ClassCastException  e){
			Log.debug(debugmsg+" Cast exceptioin. The Object passed in should be FlexMenuBarTestObject.");
			return null;
		}

		// get the first token and take it as the menu in Flex
	    String menuTitle = menuItemPath;
	    int pos = menuItemPath.indexOf("->");
		if (pos >= 0)
			menuTitle = menuItemPath.substring(0, pos);
		
		FlexMenuTestObject flexMenuObj = null;
		String num = (String) menuBarobj.getProperty(FlexUtil.PROPERTY_TYPE_NUMAUTOMATIONCHILDREN);	
		int count = 0;
		try{count = Integer.parseInt(num);
		}catch(NumberFormatException e){}
			
		// starting to match the menu title 
		for(int i=0; i<count; i++){
			FlexObjectTestObject obj = menuBarobj.getAutomationChildAt(i);
			String className = (String)obj.getProperty(FlexUtil.PROPERTY_TYPE_CLASSNAME);
			String autoName = (String)obj.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
			//Flex menu object is responsible for the first node in path
			if(className.equals(FlexUtil.FLEX_MENU_CLASSNAME) && autoName.equals(menuTitle)){
				// obj matches 
				flexMenuObj = (FlexMenuTestObject) obj;
				break;	
			}
		}	
		return flexMenuObj;	
	}
	
	/**
	 * <em>Purpose:</em> Perform a select operation on Flex menu items. 
	 *
	 * @param menuObj, a FlexMenuBarTestObject
	 * @param menuItemPath, menu path like "File->Open"	 
	 * @throws SAFSException, thrown while the operation failed
	 */
	public static void doSelectMenubar(TestObject menuObj, String menuItemPath) throws SAFSException {
		String debugMsg = FlexUtil.class.getName() + ".doSelectMenubar() ";
		StringTokenizer tokenizer = new StringTokenizer(menuItemPath, "->");
	    String firstToken = null;
	    if (tokenizer.hasMoreTokens()) 
	    	firstToken = tokenizer.nextToken();
	      
	    boolean morethanOneToken = tokenizer.countTokens() > 0 ? true : false;

	    FlexObjectTestObject menuActionObj = null;
	    try {
	    	Log.info(debugMsg + "start clicking at the menu item...");
	    	if (morethanOneToken) { // find out corresponding flex menu to the path
	    		menuActionObj =  FlexUtil.getMatchingMenuFromMenuBar(menuObj, menuItemPath);
	    		// two steps to click a menu item: 
	    		// 1. performed by FlexMenuBar to expand the corresponding flex menu that the item is in
	    		((FlexMenuBarTestObject) menuObj).click(SubitemFactory.atText(firstToken));
	    		  
	    		// 2. performed by the expanded menu to click the item 
	    		((FlexMenuTestObject)menuActionObj).click(SubitemFactory.atPath(menuItemPath));
	    	} else {
	    		menuActionObj = (FlexMenuBarTestObject) menuObj;
	    		((FlexMenuBarTestObject) menuActionObj).click(SubitemFactory.atText(menuItemPath));
	    	}
	    } catch (NullPointerException npex) {
	    	Log.debug(debugMsg + npex.getMessage() + "item not found: " + menuItemPath);
	    	throw new SAFSException(npex.toString());
	    } catch (Exception ex) {
	    	Log.debug(debugMsg + ex.getMessage());
	    	throw new SAFSException(ex.toString());
	    }
	}	
}
