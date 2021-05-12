/**
 * Copyright (C) SAS Institute, All rights reserved.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.safs.Domains;
import org.safs.GuiObjectRecognition;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.natives.NativeWrapper;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.IWindow;
import com.rational.test.ft.object.interfaces.ProcessTestObject;
import com.rational.test.ft.object.interfaces.StatelessGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.script.CaptionText;
import com.rational.test.ft.sys.DynamicEnabler;
import com.rational.test.ft.sys.DynamicEnablerResult;
import com.rational.test.ft.value.MethodInfo;

/**
 * External users would normally not use this class directly.<br>
 * Consequently, the API and associated data is subject to change without notice.
 * <p>
 * Rational specific implementation for storing and comparing GuiTestObjects a\
 * against generic recognition strings.
 * <p>
 * To effectively support "CurrentWindow" recognition strings we now have the need for 
 * JNA: <a href="http://jna.dev.java.net" target="_blank">Java Native Access</a> 
 * 
 * @author Carl Nagle, JUL 03, 2003
 *         Updated documentation.
 * @author Carl Nagle, JUL 15, 2003
 *         Updated documentation.
 * @author Bob Lawler (Bob Lawler), AUG 29, 2006
 *         Added getObjectAccessibleName() to retrieve Object's accessible name.
 *         Removed first attempt to retrieve Object's accessible name from getName(),
 *         which is called by getObjectName().  Then updated super class to first look at
 *         accessible name, and if necessary, the name property when comparing GuiTestObects
 *         by name.  This allows matches for recognition strings that include either in 
 *         identifying the object.
 * @author Carl Nagle, AUG 11, 2008 Added Firefox support for Caption
 * <br>		SEP 03, 2008   (Lei Wang)	Modify method getObjectSuperClassNames()
 * 										Add method getDotNetSuperClassNames() and getJavaSuperClassNames()
 * <br>     SEP 09, 2008   (JunwuMa)	Add Flex support.
 *                                    	Modify methods getCaption and getObjectClassName supporting Flex domain.
 *                                      In Flex domain, the top Flex window has no caption. Its automationName (*.swf) is the flex file executed. 
 *                                      Different flex applcaition owns different name in general. The unique name is taken as caption for 
 *                                      identifying the Flex application. In this way, the R-string for top window is consistent with it used to be 
 *                                      in Java, .NET,HTML like "Type=FlexWindow;Caption={FlexWebDemo.swf}"
 * <br>		Oct 14, 2008	(Lei Wang)	Modified method isMatchingSubClass(), getObjectClassName(). Change variable type from GuiTestObject to TestObject.
 * 										We need to these two methods work for class "System.Windows.Forms.ToolBarButton", see defect S0539954.
 * <br>     Oct 27, 2008    (JunwuMa)   Modified getName(TestObject), getText(TestObject) and getMatchingPathObject(Object, String) 
 *                                      supporting Flex domain.
 * <br>     Dec 15, 2008    (JunwuMa)   Change method getObjectClassName to it used to be before SEP 09, 2008, because of the migration from RFT7 to RFT8. 
 *                                      Flex object classes are no need to be treated differently in RFT8.
 * <br>     Feb 03, 2009    (Carl Nagle)    Added JNA dependency to support "CurrentWindow" effectively. 
 * <br>		Feb 25, 2009	(JunwuMa)	Modify getID(TestObject) in case an unexpected NullPointerException is thrown. Fix S0564918.
 * <br>     MAR 06, 2009    (Carl Nagle)	Fixing NullPointerExceptions in getName()
 * <br>		MAR 30, 2009	(JunwuMa)   Refactored isObjectVisible(TestObject), added isVisiblePropertyTrue(TestObject, String). 
 * <br>		MAY 07, 2009	(Carl Nagle)    Added "Value" as possible property name for getText. 
 * <br>		OCT	19, 2009	(Lei Wang)	Modify method getCaption(): For .NET domain, Try 'Caption' firstly, because some WPF application set
 * 										'Text' as a different value, which cause this window can not be found.
 * <br>		APR 16, 2010	(Carl Nagle)    Use Java Reflection to fix RFT API change in 8.1.1. 
 * <br>		JUN 12, 2010	(Lei Wang)   Modify method isMatchingSubClass(): Fix CurrentWindow can't be found. See defect S0676618
 * <br>		JUN 24, 2010	(Lei Wang)   Modify method fixIsShowing(),isMatchingSubClass(): 
 *                                      Fix CurrentWindow could not work for .Net and WPF window. See defect S0677270
 * <br>		NOV 24, 2010	(Carl Nagle)    Fixed use of getObjectClassName for FLEX domain.
 * <br>     MAR 16, 2012	(JunwuMa)   Update getObjectProperty to expand the properties to search from, support engine command getObjectRecognitionAtScreenCoordinates.
 * 
 * Copyright (C) (SAS) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class RGuiObjectRecognition extends GuiObjectRecognition {

	/** Used to call RobotJ specific functions to match recognition information. **/
	private Script script = null;

	static RGuiClassData classdata = new RGuiClassData();
	
	/** Standard Constructor **/
	public RGuiObjectRecognition(String objectInfo, Script script, int govLevel){ 
		super(objectInfo, govLevel); 
		this.script = script;
	}
		
	/** Constructor with Alternate Qualifier Separator **/
	public RGuiObjectRecognition(String objectInfo, String aQualifierSeparator, int govLevel, Script script){
		super(objectInfo, aQualifierSeparator, govLevel);
		this.script = script;		
	}


	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--GuiTestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object class name or null on error.
	 **/
	public String getObjectClassName(Object theObject){
		String debugmsg = this.getClass().getName()+".getObjectClassName() ";
		
		TestObject gtoChild;
		try{ gtoChild = (TestObject) theObject; }
		catch(ClassCastException e){
			Log.debug(debugmsg+" Can not cast object to TestObject");
			return null; 
		}
		if (gtoChild.getDomain().getName().toString().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
			return FlexUtil.getObjectClassName(gtoChild);
		}
		// retrieve the actual class from TestObject
		return gtoChild.getObjectClassName();		
	}

	/**
	 * @param theObject--sometimes a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String[] values of the requested object information. 
	 *  A 0-length array of Super class name for the object to be tested or null objects.
	 **/
	public String[] getObjectSuperClassNames(Object anObject){
		if (anObject == null) return new String[0];
		String _classname = getObjectClassName(anObject);
		if (_classname == null) return new String[0];		
        if (_classname.toLowerCase().indexOf("html.")>=0) {
            return new String[]{_classname};
        }
        TestObject theObject = (TestObject) anObject;                
        String domainname = (String) theObject.getDomain().getName();
        List classes = null;
        
        //Now we can manipulate the .NET and Java Application
        //We need to add new code to support the other Application
		if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME)){
			classes = getDotNetSuperClassNames(theObject);
		}else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME)){
			classes = getJavaSuperClassNames(theObject);
		}else if((domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_FLEX_DOMAIN_NAME))){
			classes = getFlexSuperClassNames(theObject);
		}else{
			return new String[]{_classname};
		}
		
		String[] rc = new String[classes.size()];
		for(int vi=classes.size()-1, si=0; si < classes.size(); vi--, si++)
		    rc[si] = (String) classes.get(vi);
		return rc;
	}
	
	/**
	 * @param theObject		A proxy object that represents the object to be tested
	 * @return				A list of System.Type.FullName, which are ancestor of the objetc to be tested.
	 */
	protected List getDotNetSuperClassNames(TestObject theObject){
		String debugmsg = this.getClass().getName()+".getDotNetSuperClassNames() ";
		ArrayList superClazzNames = new ArrayList();
		
		superClazzNames.add(getObjectClassName(theObject));
		
		try{
			TestObject theClass = DotNetUtil.getClazz(theObject);
			TestObject superClass  = null;
			String classname = null;
	
			while (theClass != null){
				superClass = DotNetUtil.getSuperClazz(theClass);
				classname  = DotNetUtil.getClazzFullName(superClass);
				superClazzNames.add(classname);
				theClass.unregister();
				theClass = null;
				
				// don't handle top level Object class
				if (classname.equals(DotNetUtil.CLASS_OBJECT_NAME)){
					superClass.unregister();
					superClass = null;
				}else{						
					theClass = superClass;
					superClass = null;
				}
			}
		}catch(SAFSException e){
			Log.debug(debugmsg+e.getMessage());
			//maybe we should throw this exception out??
		}
		
		return superClazzNames;
	}
	
	/**
	 * @param theObject		A proxy object that represents the object to be tested
	 * @return				A list of name of Class, which are ancestor of the objetc to be tested.
	 */
	protected List getJavaSuperClassNames(TestObject theObject){
		String debugmsg = this.getClass().getName()+".getJavaSuperClassNames() ";
		ArrayList superClazzNames = new ArrayList();
				
		superClazzNames.add(getObjectClassName(theObject));
		TestObject theClass = (TestObject) theObject.invoke("getClass");
		TestObject superClass  = null;
						
		Object baseObject = new Object();
		String basename   = baseObject.getClass().getName();
		String classname = null;
		
		while (theClass != null )  {
			
			// this is NOT valid for non-java objects
			superClass = (TestObject) theClass.invoke("getSuperclass");
			try{
				//supClass might be null?
				classname  = (String) superClass.invoke("getName");					
				superClazzNames.add(classname);
				
				theClass.unregister();
				theClass = null;
				
				// don't handle top level Object class
				if (classname.equals(basename)){
					superClass.unregister();
					superClass = null;
				}else{			
					theClass = superClass;
					superClass = null;
				}
			}
			catch(NullPointerException np){
				Log.debug(debugmsg+" Encounter null pointer exception.");
				theClass = null;
			}
		}
		
		return superClazzNames;
	}
	/**
	 * @param theObject		A proxy object that represents the FlexObjectTestObject to be tested
	 * @return				A list of name of Class, which are ancestor of the objetc to be tested.
	 */
	protected List getFlexSuperClassNames(TestObject theObject){
		ArrayList superClassNames = new ArrayList();
		
		String classname = getObjectClassName(theObject);
		//the order in the array is correct?
		//superClassNames.add(classname); the order in the array is correct?
		try{
			String superclass = null;
			superclass = (String)theObject.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONCLASSNAME);
			if((superclass!=null)&&(superclass.length()!=0))
				superClassNames.add(superclass);
		}catch(PropertyNotFoundException p){
			Log.info("RGOR: IGNORING Flex class: "+ classname +" PropertyNotFoundException: "+ p.getMessage());
		}
		// put it here to match, the reversing the array in LocalServerGuiClassData.getMappedClassType
		//  the loop algo in LocalServerGuiClassData/Line79 might not be correct?
		superClassNames.add(classname); 
		return superClassNames;
	}
	/**
	 * Determine if the provided GuiTestObject matches our segment of the 
	 * recognition string.  Primarily just forwards the call to 
	 * RGuiClassData.getMappedClassType.
	 * 
	 * @param theObject--GuiTestObject proxy of the actual object to be compared against our 
	 *        recognition string.
	 * 
	 * @param theClass information provided and forwarded to RGuiClassData.isMappedClassType.
	 * 
	 * @return true if the class matches, or is a subclass of, the class specified 
	 *         in the recognition string.
	 *         false if we cannot identify an appropriate match or an error occurs.
	 */	
	public boolean isMatchingType(Object theObject, String theClass){
		
		GuiTestObject gtoChild;
		try{ gtoChild = (GuiTestObject) theObject; }
		catch(ClassCastException e){ return false; }
		
		return (classdata.getMappedClassType(theClass, gtoChild) instanceof String);
	}

	/**
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to simply forward this request on to the 
	 * RGuiClassData.isMatched function after casting the object to 
	 * the appropriate class.
	 *
	 * We use this method to support the "CurrentWindow" recognition string.
	 * isMatchingSubclass should return true IF the class sought is "CurrentWindow" and the 
	 * object is determined to be the topmost active window with input (keyboard) focus.
	 * 
	 * @param theObject--GuiTestObject proxy for the object to be evaluated.
	 * 
	 * @param theClass information from GuiObjectRecognition and forwarded 
	 *        to RGuiClassData.isMatched.
	 * 
	 * @param parentClass information provided and forwarded to 
	 *        RGuiClassData.isMatched.
	 * 
	 * @return true if theObject is determined to satisfy the requested information.
	 * @author Carl Nagle  Apr 16, 2010 Use Java Reflection to fix RFT API change in 8.1.1.
	 **/
	public boolean isMatchingSubClass(Object theObject, String theClass, String parentClass){
		
		TestObject toChild;
		GuiTestObject gto;
		
		if(theClass.equalsIgnoreCase(CATEGORY_CURRENTWINDOW)){
			Log.info("RGOR.isMatchingSubClass handling CurrentWindow.");
			// TODO: WindowFunctions only accommodates WIN32 for now
			long hwnd = 0;//default "didn't work" value
			long hwndpid = 0;
			try{
				hwnd = ((Long) NativeWrapper.GetForegroundWindow()).longValue();
				//Pre 8.1.1:
				//DynamicEnablerResult rc = DynamicEnabler.hookProcessWithHWND(hwnd, "Win");
				//Changed for 8.1.1:
				//DynamicEnablerResult rc = DynamicEnabler.hookProcessWithHWNDNative(hwnd);
				//Fixing with Java Reflection below:
				Method method = null;
				DynamicEnablerResult rc = null;
				boolean isNative = false;
				try{
					method = DynamicEnabler.class.getMethod("hookProcessWithHWNDNative", new Class[]{long.class});
					//System.out.println("hookProcessWithHWNDNative(long) was successfully found....");
					isNative = true;
				}catch(NoSuchMethodException nsm){
					//System.out.println("Not found: "+ nsm.getMessage());
					try{
						method = DynamicEnabler.class.getMethod("hookProcessWithHWND", new Class[]{long.class,String.class});
						//System.out.println("hookProcessWithHWND(long,String) was successfully found....");			
					}catch(NoSuchMethodException nsm2){
						//System.out.println("Not found: "+ nsm.getMessage());
					}
				}
				if (method==null){
					Log.debug("RGOR.isMatchingSubClass IGNORING failure to find hookProcess due to RFT API mismatch.");
					return false; 
				}
				try{
					if (isNative){
						//DynamicEnablerResult rc = DynamicEnabler.hookProcessWithHWNDNative(hwnd);						
						rc = (DynamicEnablerResult) method.invoke(null, new Object[]{new Long(hwnd)});
					}else{
						//DynamicEnablerResult rc = DynamicEnabler.hookProcessWithHWND(hwnd, "Win");
						rc = (DynamicEnablerResult) method.invoke(null, new Object[]{new Long(hwnd), "Win"});
					}
				}catch(Throwable t){
					Log.debug("RGOR.isMatchingSubClass IGNORING failure to invoke hookProcess due to RFT API mismatch. "+ 
							  t.getClass().getSimpleName()+":"+ t.getMessage());
					return false; 
				}				
				hwndpid = rc.processId;
				Log.info("RGOR.isMatchingSubClass DynamicEnabler enabled process "+ hwndpid);
			}catch(Exception x){
				Log.debug("RGOR.isMatchingSubClass IGNORING CurrentWindow GetForegroundWindow Exception: "+ theObject.getClass().getSimpleName(), x);
			}			
			try{
				gto = (GuiTestObject) theObject;
			}catch(ClassCastException e){
				Log.debug("RGOR.isMatchingSubClass IGNORING CurrentWindow GuiTestObject Exception for "+ theObject.getClass().getSimpleName(), e);
				return false; 
			}
			boolean focused = gto.hasFocus();
			Log.debug("Current window pid is "+hwndpid+". Testobject Domain: "+gto.getDomain().getName()
					+"; Testobject Processid is "+gto.getProcess().getProcessId()+"; Testobject focused is "+focused);
			
			// call to hasFocus is NOT accurate for Java and Web (and others?)
			if (!focused){				
				String domain = gto.getDomain().getName().toString();
				TopLevelTestObject parent ;
				ProcessTestObject process = gto.getProcess();
				int processid = (int)process.getProcessId();
				// Java Windows don't seem to return hasFocus properly
				if (domain.equalsIgnoreCase(Domains.JAVA_DOMAIN)){
					try{ focused = gto.getProperty("focused").toString().equalsIgnoreCase("true"); }
					catch(Exception x){
						Log.debug("RGOR.isMatchingSubClass IGNORING Java Domain CurrentWindow focus Exception for "+ theObject.getClass().getSimpleName(), x);
						focused = false;
					}
				}else if (! domain.equalsIgnoreCase(Domains.WIN_DOMAIN)){
			        Log.info("RGOR.isMatchingSubclass GetForegroundWindow trying HWND: "+ hwnd + " PID: "+ processid);	
					try{ 
				        if( domain.equalsIgnoreCase(Domains.HTML_DOMAIN )){
				        	//which .browserName "MS Internet Explorer" or "Firefox"
				        	String browser = (String) gto.getProperty(".browserName");
				        	String browserclass = browser.equalsIgnoreCase("Firefox")? "MozillaUIWindowClass" : "IEFrame";
					        Log.info("RGOR.isMatchingSubclass looking for browser class '"+ browserclass +"' with pID '"+ processid +"'");
			        		try{ 
		        				IWindow[] wins = script.getTopWindows();
								Log.debug("RGOR.isMatchingSubclass HTML hooked WIN domain matches ="+ String.valueOf(wins.length));
		        				IWindow win = null;
		        				String winclass = null;
		        				int pid = 0;
		        				long whandle = 0;
								for(int i=0;(i<wins.length && !focused);i++){
									win = wins[i];
									whandle = win.getHandle();
									winclass = win.getWindowClassName();
									pid = win.getPid();
									Log.info("RGOR evaluating IWindow class: "+ winclass +" processID: "+ pid +" handle: "+whandle);
									if( ! winclass.equalsIgnoreCase(browserclass)){
										if(whandle==hwnd){
											Log.info("RGOR.isMatchingSubclass did not match browser: "+ browserclass +" with handle: "+ whandle);
											return false;
										}else
											continue;
									}
//									if (!(pid==processid)) continue;// not the right for the same browserclass
									if (!(pid==processid || pid==hwndpid)) continue;
									
									//is the right browser class, is the right processid....
									if(win.hasFocus()||(whandle==hwnd)) {
										Log.info("RGOR.isMatchingSubclass matched CurrentWindow to pid: "+ pid +":"+ browser +" handle: "+ whandle);
										return true;
									}else{
										Log.info("RGOR evaluated "+ winclass +" processID: "+ pid +" does not have focus.");
										return false;
									}
								}
			        		}catch(Exception x){
			        			Log.debug("RGOR HtmlBrowser hookProcess Exception: "+ x.getClass().getSimpleName());
			        		}
							Log.info("RGOR.isMatchingSubclass did not match CurrentWindow to "+ browser);
			        		return false;
				        }//not HTML domain
				        else{
				        	//S0677270, if the domain is DotNet, maybe we should not call DynamicEnabler.isTopParentWinFocused()
				        	//In that method, we call find( ..., script.atProperty(".domain", "Win")), if the domain is Net, this 
				        	//risk to block the whole program
				        	if(!domain.equalsIgnoreCase(Domains.NET_DOMAIN))
				        		focused = org.safs.rational.ft.DynamicEnabler.isTopParentWinFocused(script, processid);
				        }
					}
					catch(Exception x){
						Log.debug("RGOR.isMatchingSubClass IGNORING HTML Domain CurrentWindow focus Exception for "+ theObject.getClass().getSimpleName(), x);
						focused = false;
					}
				}// IS Win domain
				else{
					if((hwnd != 0)&&(hwndpid != 0)&&(hwndpid != processid)){
						Log.debug("RGOR.isMatchingSubClass IGNORING wrong process "+ processid);
						return false;
					}
		        	focused = org.safs.rational.ft.DynamicEnabler.isTopParentWinFocused(script, processid);
				}
			}
			return focused;
		}
		try{ toChild = (TestObject) theObject; }
		catch(ClassCastException e){ 
			Log.debug("RGOR.isMatchingSubClass normal TestObject Exception for "+ theObject.getClass().getSimpleName(), e);
			return false; 
		}
		Log.info("RGOR:isMatchingSubClass: parentClass:"+parentClass);
		return classdata.isMatched(theClass, parentClass, toChild);
	}


	/**
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--GuiTestObject proxy for the object to be evaluated.
	 * 
	 * @return true if theObject was found to be "showing" or "visible".
	 **/
	public boolean isObjectShowing(Object theObject){

		GuiTestObject gtoChild;
		try{ gtoChild = (GuiTestObject) theObject; }
		catch(ClassCastException e){
            Log.debug("RGOR: ClassCastException: isObjectShowing: false, "+theObject, e);
            return false;
        }
		return fixIsShowing(gtoChild);
	}
	

	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--GuiTestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information or an empty string.
	 *         Must not return a null value at this time.
	 **/
	public String getObjectCaption(Object theObject){		
		TestObject parent = null;
		try{ parent = (TestObject) theObject; }
		catch(Exception x){ 
			Log.info("RGOR.getObjectCaption initialization exception:"+x);
			return "";
		}
		return getCaption(parent);		
	}

	
	/**
	 * Used internally or by subclasses only.
	 * The object domain is the environment, OS, or programming language origins of 
	 * theObject.  Example: Java, Html, Win, Net, Visual Basic, etc.
	 * @param theObject -- usually a tool-dependent proxy for the object to be evaluated.
	 * @return String value of "Java" , "Net", "Html", Win", etc..
	 */
	public String getObjectDomain(Object theObject){
		TestObject parent = null;
		try{ 
			parent = (TestObject) theObject;
			return (String) parent.getDomain().getName();
		}
		catch(Exception x){ 
			Log.info("RGOR.getObjectDomain initialization exception:"+x);
			return "";}		
	}
	
	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--GuiTestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information or an empty string.
	 *         Must not return a null value at this time.
	 **/
	public String getObjectName(Object theObject){

		TestObject parent = null;

		try{ parent = (TestObject) theObject; }
		catch(Exception x){ 
			Log.info("RGOR.getObjectName initialization exception:"+x);
			return "";}
		return getName(parent);
	}	
	
	/**
	 * @author Bob Lawler (Bob Lawler), 08.29.2006 - Added so that an Object can be recognized by its
	 *         accessible name, independent of its Name property(ies).  
	**/
	public String getObjectAccessibleName(Object obj){

		String theName = "";

		try{ 
			theName = ((TestObject) obj).getProperty("accessibleContext.accessibleName").toString();
			theName = theName.trim();
	        if (theName.length()>0) {
	            Log.info("RGOR: the object's accessible name is '"+ theName +"'.");
	        	return theName;
	        }
		}catch(Exception x){
			theName = "";
		}

        Log.info("RGOR: the object does not seem have an accessible name.");
		return theName;
	}	
			

	/** 
	 * not yet implemented 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * <p>
	 * Level is considered to be the Z-Order--often of top level window objects--
	 * of components on the desktop or in a container.  The highest Level is 
	 * Level=1, and this usually indicates the item is visible and forefront.  
	 * The next Level is Level=2, and so on.
	 * <p>
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--TestObject proxy for the object to be evaluated.
	 * 
	 * @return int value of the requested object information or -1.
	 **/
	public int getObjectLevel	  (Object theObject){ 
		Log.debug("Recognition by Qualifier \"Level\" is not yet supported.");
		return -1; 
	}


	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--TestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information or an empty string.
	 *         Must not return a null value at this time.
	 **/
	public String getObjectText	  (Object theObject){ 
		String theText = "";
		TestObject parent = null;
		try{ parent = (TestObject) theObject; 
		}catch(Exception x){ 
			Log.info("RGOR.getObjectText initialization exception:"+x);
			return "";
		}
		return getText(parent);
	}


	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--TestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information or an empty string.
	 *         Must not return a null value at this time.
	 **/
	public String getObjectID (Object theObject){ 
		TestObject parent = null;
		try{ parent = (TestObject) theObject;}
		catch(Exception x){ 
			Log.info("RGOR.getObjectID initialization exception:"+x);
			return "";
		}
		return getID(parent);
	}


	/**
	 * Used internally, or by direct subclasses only.<br>
	 * Subclasses implement tool-dependent mechanism to provide the requested information.
	 * 
	 * @param theObject--usually a tool-dependent proxy for the object to be evaluated.
	 * 
	 * @return String[] of all known property names.  Property names are assumed to 
	 * be case-sensitive.
	 **/
	public String[]  getObjectPropertyNames  (Object theObject){
        GuiTestObject parent = null;
        String[] names = new String[0];
        try{ 
            parent = (GuiTestObject) theObject; 
        }
        catch(Exception x){ 
            Log.info("RGOR.getObjectPropertyNames initialization exception:"+x);
            return names;
        }
        try{
            Hashtable props = parent.getProperties();
            Hashtable nvprops = parent.getNonValueProperties();
            names = new String[props.size()+ nvprops.size()];
            int i=0;
            Enumeration keys = props.keys();
            while(keys.hasMoreElements()){
            	names[i++] = keys.nextElement().toString();            	
            }
            keys = nvprops.keys();
            while(keys.hasMoreElements()){
            	names[i++] = keys.nextElement().toString();            	
            }
            
        }catch(Exception x){
            ;
        }
        return names;
	}

	
	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--TestObject proxy for the object to be evaluated.
	 * 
	 * @param theProperty name of the property value to be evaluated in the object.
	 * 
	 * @return String value of the requested object information.  May be an empty string.
	 *         May be null if the property does not exist.
	 **/
	public String getObjectProperty (Object theObject, String theProperty){ 
		boolean exthrown = false;  
        String value = null;
        
        GuiTestObject parent = null;
		try{ 
            parent = (GuiTestObject) theObject; 
            value = parent.getProperty(theProperty).toString().trim();
        }
        catch(Exception x){ 
        	exthrown = true;
            Log.info("RGOR.getObjectProperty initialization exception:"+x);
        } 
		
		//try to get the TestObject's Recognition Properties, which are I think given by RFT. 
    	if (value == null || exthrown) {
    		Hashtable  rsProps = parent.getRecognitionProperties();
    			
    		Enumeration e = rsProps.keys();
    		while(e.hasMoreElements()) {
    			Object key =  e.nextElement();
    			if (((String)key) == theProperty) {
    				value = rsProps.get(key).toString();
    				break;
    			}	
    		}
    	}
    	return value;
	}


	/**
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * Retrieves the resulting object identified with the Path information applied to theObject. 
	 * 
	 * @param theObject--StatelessGuiSubitemTestObject proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return Object child sub-object found relative to theObject
	 **/
	public Object getMatchingPathObject (Object theObject, String thePath){ 

		if(thePath == null) return null;
		
		Object pathMatch = null;
		try{ 
			StatelessGuiSubitemTestObject parent = (StatelessGuiSubitemTestObject) theObject; 
			Object subitem = parent.getSubitem(script.localAtPath(thePath));
			if (subitem instanceof GuiTestObject) pathMatch = subitem;
		}
		catch(Exception ex){
            Log.info("caught exception: "+ex, ex);
			// theObject is not StatelessGuiSubitemTestObject proxy in Flex domain
			// try to drill down it finding its child sub-object according thePath info.
            // only consider Flex Menu, not including Flex Tree.
            Log.info("trying it as FlexMenuBarTestObject");
			pathMatch = FlexUtil.getMatchingPathObject(theObject, thePath);
        }
		
		//If the pathMatch is null, we will try to find it by searching in the subtree
		if(pathMatch ==null && theObject instanceof TestObject){
			pathMatch = DotNetUtil.getMatchingPathTestObject(this, (TestObject)theObject, thePath, DEFAULT_PATH_SEPARATOR);
		}
		
		return pathMatch; 
	}


	/**
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to determine the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--StatelessGuiSubitemTestObject proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return true if the child sub-object was found relative to theObject.
	 **/
	public boolean isMatchingPath	  (Object theObject, String thePath){ 
		boolean pathMatch = false;
		try{ 
			Object subitem = getMatchingPathObject(theObject, thePath);
			pathMatch = (subitem instanceof TestObject);
  			// removing this unregister because currently unregistering any one reference 
  			// to an object seems to unregister ALL stored references to an object, 
  			// including cached references in our AppMaps.
			/* if (pathMatch) ((TestObject)subitem).unregister(); */
		}
		catch(Exception ex){
                  Log.info("caught exception: "+ex, ex);
                }
		return pathMatch; 
	}
  protected void listAllProperties (TestObject obj, String str) {
  	if(!Log.ENABLED) return;
    Log.debug(" ...............listProperties: "+str);
    listProperties(obj);
    Log.debug(" .......listNonValueProperties: "+str);
    listNonValueProperties(obj);
    Log.debug(" .....");
  }

  protected void listProperties (TestObject obj) {
  	if(!Log.ENABLED) return;
    Map m = obj.getProperties();
    Log.info(" .......Number of Value properties:"+ m.size());
    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
      String key = (String)i.next();
      Object next = m.get(key);
      Log.debug("key: "+key+": "+next);
    }
  }
  protected void listNonValueProperties (TestObject obj) {
  	if(!Log.ENABLED) return;
    Map m = obj.getNonValueProperties();
    Log.info(" .......Number of Non-Value properties:"+ m.size());
    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
      String key = (String)i.next();
      Object next = m.get(key);
      Log.debug("key: "+key+": "+next);
    }
  }
  protected void listMethods (TestObject obj) {
  	if(!Log.ENABLED) return;
    MethodInfo[] mi = obj.getMethods();
    Log.info(" .......Number of Methods:"+ mi.length);
    for(int i=0; i<mi.length; i++) {
      Log.debug("next method: "+mi[i].getName()+ ", "+mi[i]);
    }
  }
  
  public static boolean isObjectVisible(TestObject obj){
  	boolean visible = false;
  	GuiTestObject gobj = null;
  	TestObject uobj = null;
  	
  	if(obj instanceof GuiTestObject) gobj = (GuiTestObject) obj;
  	else try{
  		//Log.debug("Attempting to create GuiTestObject...");
  		gobj = new GuiTestObject(obj);
  	}catch(Exception x){
  		Log.debug("Ignoring create GuiTestObject Exception: "+ x.getClass().getSimpleName());
  	}
  	uobj = (gobj == null) ? obj:gobj;  	
  	try{
  		visible = isVisiblePropertyTrue(uobj, "visible");
  	}catch(Exception y){}
  	
  	try{
  		if(!visible) visible = isVisiblePropertyTrue(uobj, ".visible");
  	}catch(Exception y2){}
  	
  	try{
  		if(!visible) visible = isVisiblePropertyTrue(uobj, "Visible");
  	}catch(Exception y3){}
  	
  	if(!visible) visible = isObjectShowing(uobj);
  	
  	return visible;
  }
  /*
   * Called by isObjectVisible(TestObject) for getting curtain visible property of a TestObject.
   * An PropertyNotFoundException will be thrown if the property not found
   * 
   * @param guiObj		A test object 
   * @param visibleProp	the property of the test object for its visibility
   * @return			boolean, true if the property represents being visible
   */
  public static boolean isVisiblePropertyTrue(TestObject testobj, String visibleProp) throws PropertyNotFoundException{
	  boolean isVisible = false;
	  Object vobj = testobj.getProperty(visibleProp); 
	  if (vobj == null)
		  return false;
	  if (vobj instanceof Integer)  
		  //for the visible property of the objects like awt.Scrollbar, the Integer returned represents true if the value is more than zero.
		  isVisible = (((Integer) vobj).intValue()>0)? true : false;
	  else
		  isVisible = vobj.toString().equalsIgnoreCase("true");
	  return isVisible;
  }
  
  private static boolean fixIsShowing(GuiTestObject obj){
		//RFT Html.Dialog boxes are sometimes ALWAYS isShowing=false (.visible=false) even when they 
		//are clearly visible and showing.  Thus, we cannot rely on the GuiTestObject call at this time.
	    boolean isShowing = false;
	    try{
	    	isShowing = obj.isShowing();

	    	if(!isShowing){
	    		//obj.getProperty(".class") may be a null, so include this section in try block.
	    		if(Domains.HTML_DOMAIN.equalsIgnoreCase(obj.getDomain().getName().toString())){
	    			//If getProperty(".class") return null, the following will
	    			//throw NullpointerException, we should catch it here. If it is not
	    			//caught here, it will be caught in GOV, but this will cause the rest
	    			//test object NOT processed
	    			//See defect S0677270.
	    			String _class = obj.getProperty(".class").toString();
	    			Log.info("RGOR.isShowing=false evaluating Html.Dialog workaround for "+ _class);
	    			if(_class.equalsIgnoreCase("Html.Dialog")){
	    				//not sure if we should assume the Html.Dialog is showing if it exists at all.
	    				//However, the dialog would have to satisfy other criteria like proper caption or 
	    				//other properties, too.
	    				isShowing=true;
	    			}
	    		}
	    	}
	    }catch(Exception x){
            Log.debug("RGOR: isObjectShowing Exception: "+obj, x);
            return false;
	    }
        Log.info("isObjectShowing: "+ isShowing +","+ obj);
		return isShowing;
  }
  
  public static boolean isObjectShowing(TestObject obj){
	GuiTestObject gtoChild = null;
	TestObject uobj = null;
	String showing = null;
	try{ 
		gtoChild = (GuiTestObject) obj;
		uobj = gtoChild;
	}
	catch(ClassCastException e){ 
		try{ 
			Log.debug("Handling isObjectShowing ClassCastException: Attempting to create new GuiTestObject...");
			gtoChild = new GuiTestObject(obj);
			return gtoChild.isShowing();
		}catch(Exception x){
			Log.debug("Ignoring isObjectShowing new GuiTestObject() Exception: "+ x.getClass().getSimpleName());
		}
		try{
			showing = obj.getProperty("showing").toString();
			return (showing==null)? false:showing.equalsIgnoreCase("true"); 
		}
		catch(Exception e2){ 
			Log.debug("Ignoring isObjectShowing Exception: "+ e.getClass().getSimpleName());
			return false; 
		}
	}
	//RFT Html.Dialog boxes are sometimes ALWAYS isShowing=false (.visible=false) even when they 
	//are clearly visible and showing.  Thus, we cannot rely on the GuiTestObject call at this time.
	return fixIsShowing(gtoChild);
  }

  public static String getUIClassID(TestObject obj){
		GuiTestObject gtoChild = null;
		try{ 
			gtoChild = (GuiTestObject) obj;
			return gtoChild.getProperty("uIClassID").toString();
		}
		catch(ClassCastException e){ 
			//Log.debug("getUIClassID attempting to create new GuiTestObject...");
			try{ 
				gtoChild = new GuiTestObject(obj);			
				return gtoChild.getProperty("uIClassID").toString();
			}catch(Exception x){
				Log.debug("getUIClassID could not deduce 'uiClassID': "+ x.getClass().getSimpleName());
				return "unknown";
			}
		}
		catch(Exception e){ 
			Log.debug("getUIClassID could not deduce uiClassID: "+ e.getClass().getSimpleName());
			return "unknown";
		}
  }

  public static boolean isTopLevelWindow(TestObject obj){
  	return obj.isSameObject(obj.getTopParent());
  }
  
  public static String getCaption(TestObject obj){
	String theCaption = "";

	Object guiCaption = null;
	String domainname = (String) obj.getDomain().getName();
	String classname = obj.getObjectClassName();
	
	//First, we try to get caption object for a specific domain
	try{
		// return property 'automationName' for top Flex application window ONLY, take it as Caption.
		// See a top flex window's R-String: "Type=FlexApplcaiton;Caption={FlexWebDemo.swf}"
		// this step must be done at first, for obj.getProperty(".captionText") in next step returns blank string for top Flex application window.
		// It is interesting that property '.captionText' can't be listed for top Flex application window, but can be called without Exception.
		if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_FLEX_DOMAIN_NAME)){
	    	guiCaption = FlexUtil.getCaption(obj);
	    }else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME)){
	    	if(guiCaption==null || guiCaption.equals("")){
		    	try{
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We try to get property 'Title'");
		    		guiCaption = obj.getProperty("Title");
		    	}catch(Exception x1){
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get property 'Title'");
		    	}
	    	}
	    	if(guiCaption==null || guiCaption.equals("")){
		    	try{
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We try to get property 'Text'");
		    		guiCaption = obj.getProperty("Text");
		    	}catch(Exception x1){
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get property 'Text'");
		    	}
	    	}
	    	if(guiCaption==null || guiCaption.equals("")){
		    	try{
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We try to get property 'Name'");
		    		guiCaption = obj.getProperty("Name");
		    	}catch(Exception x1){
		    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get property 'Name'");
		    	}
	    	}
	    	if(guiCaption==null || guiCaption.equals("")){
		    	Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get caption, You should try other property.");
	    	}
	    }else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_WIN_DOMAIN_NAME)){
	    	try{
	    		guiCaption = obj.getProperty(".text");
	    	}catch(Exception x1){
	    		try{
	    			guiCaption = obj.getProperty(".name");
	    		}catch(Exception x2){
	    			Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get property '.text' or '.name' as Caption. "+x2.getMessage());
	    		}
	    	}
	    }else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_HTML_DOMAIN_NAME)){
	    	// check if HtmlBrowser
	    	Log.debug("RGOR.getCaption(): Try to get caption object for domain "+domainname);
	    	if(classname.equalsIgnoreCase("HTML.DIALOG")){
	    		Log.info("...seeking HTML Dialog caption...");
	    		try{
	    			guiCaption = obj.getProperty(".caption");
	        		Log.debug("Html.Dialog guiCaption:"+ guiCaption);
	    		}catch(Exception x){
	        		Log.debug("Html.Dialog getCaption Exception IGNORED,",x);
	    		}
	    	}else if(classname.equalsIgnoreCase("HTML.HTMLBROWSER")){
	    		Log.info("...seeking HTMLBrowser caption...");
	    		TestObject[] children = obj.getMappableChildren();
	    		TestObject child = null;
	    		String childclass = "";
	    		for (int i=0; i<children.length; i++){
	    			try{
	    			    child = children[i];
	    			    childclass = child.getObjectClassName();
	    			    if (childclass.equalsIgnoreCase("HTML.HTMLDOCUMENT")){
	    			    	guiCaption = RDDGUIUtilities.getHtmlDocTitle(child);
	    			    	break;
	    			    }
	    			}catch(Exception ax){
	    				Log.debug("RGOR.getCaption IGNORE exception from HTMLBrowser: "+ ax.getClass().getSimpleName());
	    			}
	    		}
	    	}else if(classname.equalsIgnoreCase("HTML.BODY")){
	        	//Type=HtmlDocument; = Html.BODY
	    		Log.info("...seeking HTML.BODY  caption/HTMLTitle...");
	    		TestObject doc = RDDGUIUtilities.getHtmlDocumentObject(obj);
	    		if( doc != null ) {
	    			guiCaption = RDDGUIUtilities.getHtmlDocTitle(doc);
	    		}else{
	        		Log.info("...no parent HTML Document found...");
	    		}
	        // do not return a caption for HTMLDocument (or most other subnodes)
	        // for compatibility HTMLDocument is normally found as 
	        // Index=1 within its parent frame (or browser)
	    	}else{
	    		Log.info("...no HTML caption found...");
	    	}
	    }else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME)){
	    	try{
	    		guiCaption = obj.getProperty("title");
	    	}catch(Exception x1){
	    		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get property 'title' as Caption. "+x1.getMessage());
	    	}
	    }
	}catch(PropertyNotFoundException e){
		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get caption object. "+e.getMessage());
	}
	
	//Try to get a CaptionText with a general property '.captionText' if we
	//did not get caption object for a specific domain
	try{
		if(guiCaption==null){
			guiCaption = obj.getProperty(".captionText");
		}
	}catch(PropertyNotFoundException p){
		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get caption object with property '.captionText'. "+p.getMessage());
		try{
			guiCaption = (String)obj.getProperty("title");
		}catch(PropertyNotFoundException p3) {
			Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get caption object with property 'title'. "+p.getMessage());
		}
    }catch(Exception ex){
        Log.debug("RGOR.getCaption(): caught exception: "+ex, ex);
    }
	
    //Treat the caption object
	if(guiCaption==null){
		Log.debug("RGOR.getCaption(): for domain "+domainname+", We can not get an caption object.");
		theCaption = "";
	}else if(guiCaption instanceof CaptionText){
		theCaption = ((CaptionText)guiCaption).getCaption();
	}else{
		if(!(guiCaption instanceof String)){
			Log.debug("RGOR.getCaption(): for domain "+domainname+", caption object is a "+guiCaption.getClass().getName());
		}
		theCaption = guiCaption.toString();
	}
	
	return theCaption;
  }
  
	public static String getText(TestObject obj){ 
		String theText = "";
		String holdValue = null;
		
		String domainname = (String) obj.getDomain().getName();
	    String classname = obj.getObjectClassName();
		
		boolean exthrown = false;
		try {
            theText = (String)obj.getProperty("text");
            if (theText.length() > 0) {
                return theText;
            }
		} catch(Exception x) {
            exthrown = true;
            theText = "";
		}
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;

		exthrown=false;
		try {
            theText = (String)obj.getProperty(".text");
            if (theText.length() > 0) {
                return theText;                 
            }
		}
		catch (Exception x) {
            exthrown=true;
            theText = "";
		}

		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;

		exthrown = false;
        try {
            theText = (String) obj.getProperty("Text");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;

        exthrown = false;
        try {
            theText = (String) obj.getProperty("Value");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }

        exthrown = false;
        try {
            theText = (String) obj.getProperty("value");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;
		
        exthrown = false;
        try {
            theText = (String) obj.getProperty(".value");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;
		
        exthrown = false;
        try {
            theText = (String) obj.getProperty(".title");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;
		
        exthrown = false;
        try {
            theText = (String) obj.getProperty("title");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		// what if the text is blank?
		// we may have to do special checks based on type of component
		//if (!exthrown) return theText;

        exthrown = false;
        try {
            theText = (String) obj.getProperty("alt");
            if (theText.length() > 0) {
                return theText;
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
		
        exthrown = false;
        try {
            theText = (String) obj.getProperty(".alt");
            if (theText.length() > 0) {
                return theText;     
            }
        } catch (Exception x) {
            exthrown = true;
            theText = "";
        }
        
        // Support Flex domain. Some Flex controls (like Text,Label) not including Flex Menuitem have property 'text', 
        // which has been tried before in this method. To generate path menu information(like Path=File->Exit), it is 
        // needed to find out and return the text for Flex menuitem. 
        // That is required by the algorithm of STAFPC.genRecogString.
        if(domainname.equalsIgnoreCase(Domains.FLEX_DOMAIN)){ 
	    	return FlexUtil.getTextOfFlexMenuItem(obj); //no need to try getCaption(obj) followed.
        }else if(domainname.equalsIgnoreCase(Domains.NET_DOMAIN)){
        	Log.debug("Try to get the Text from its properties directly.");
        	theText = DotNetUtil.getText(obj);
        	if(theText.length()>0) return theText;
        }
        
		//last ditch effort.
		return getCaption(obj);
	}  

	/**
	 * 
	 * 08.29.2006 - Bob Lawler - With the addition of getObjectAccessibleName(), it's no longer necessary
	 *                        to check for accessibleName here.  Super class will be updated to first attempt
	 *                        match on getObjectAccessibleName() and if no match, call getObjectName()
	 *                        (which utlimately calls this function. 
	 *
	 * MARCH 06, 2009 (Carl Nagle) Fixing NullPointerExceptions
	 * 
	 **/
	public static String getName(TestObject obj){

		String theName = "";

		//removed 08/29/2006 - Bob Lawler
		//try{ theName = obj.getProperty("accessibleContext.accessibleName").toString();}catch(Exception x){;}
        //if (theName.trim().length()>0) return theName.trim();
		
		try{ 
			theName = obj.getProperty("name").toString();
	        if (theName.trim().length()>0) return theName;
		}catch(Exception x){;} // RFT does throw NullPointerException if theName=null

		try{ 
			theName = obj.getProperty(".name").toString();
	        if (theName.trim().length()>0) return theName;
		}catch(Exception x){;} // RFT does throw NullPointerException if theName=null

		try{ 
			theName = obj.getProperty("Name").toString();
	        if (theName.trim().length()>0) return theName;
		}catch(Exception x){;} // RFT does throw NullPointerException if theName=null

		// for flex domain
		try{ 
	        if (obj.getDomain().getName().toString().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
	        	theName = FlexUtil.getName(obj);
	        	if (theName.trim().length()>0) return theName;
	        }
		}catch(Exception x){;} // RFT does throw NullPointerException if theName=null
        
		// for WIN/RCP/SWT domain ".name" getProperty() invalid but exists in getProperties()
        if (obj.getDomain().getName().toString().equalsIgnoreCase(Domains.WIN_DOMAIN)){
            Log.info("RGOR: checking getProperties() in the WIN/SWT domain...");
        	try{
        		theName = obj.getProperties().toString();        	
        		String indexed = " .name=";  //most unique
        		int index = theName.indexOf(indexed);
        		if (index < 0) {
        			indexed = ".name="; //less unique, but matches if first property
        			index = theName.indexOf(indexed);
        		}
        		if (index > -1){
        			int startindex = index + indexed.length();
        			int endindex = theName.indexOf(",", startindex);
        			if (endindex < 0) endindex = theName.indexOf("}", startindex);
        			if (endindex < 0){
        				theName = theName.substring(startindex);
        			}else{
        				theName = theName.substring(startindex, endindex);
        			}
        	        if (theName.trim().length()>0) return theName;
        		}
        	}
        	catch(Exception x){
                Log.debug("RGOR: IGNORED "+  x.getClass().getSimpleName() +" while checking getProperties() in the WIN/SWT domain.");
        	}
        }
        
        Log.info("RGOR: the "+ obj.getDomain().getName() +" domain object did not have a name.");
		return (theName == null) ? "" : theName;
	}	

	public static String getID(TestObject obj){ 
		String theID = "";
		String	domainname = (String) obj.getDomain().getName();
		String	classname = obj.getObjectClassName();
		
		boolean exthrown = false;
		try{ theID = (String)obj.getProperty(".id");}
		catch(Exception x){exthrown=true;}
		// what if the text is blank?
		// we may have to do special checks based on type of component
		if (!exthrown && theID!=null && theID.length()>0) return theID;

		exthrown=false;
		try{ theID = (String)obj.getProperty("id");}
		catch(Exception x){exthrown=true;}
		if (!exthrown && theID!=null && theID.length()>0) return theID;

		exthrown = false;
		try{ theID = (String)obj.getProperty("Id");}
		catch(Exception x){exthrown=true;}
		if (!exthrown && theID!=null && theID.length()>0) return theID;

		exthrown = false;
		try{ theID = (String)obj.getProperty("Name");}
		catch(Exception x){exthrown=true;}
		// before checking the length of theID, make sure theID is not null. Fix S0564918.
		if (!exthrown && theID!=null && theID.length()>0) return theID;

		return "";
	}
	

	/** 
	 * Used internally by GuiObjectRecognition superclass.<br>
	 * Rational RobotJ mechanism to provide the requested information.
	 * The information is used to determine if a particular object is a match for our stored 
	 * recognition information.
	 * 
	 * @param theObject--TestObject proxy for the object to be evaluated.
	 * 
	 * @return String value of the requested object information or an empty string.
	 *         Must not return a null value at this time.
	 **/
	public String getObjectClassIndex (Object theObject){ 
		TestObject parent = null;
		try{ parent = (TestObject) theObject;}
		catch(Exception x){ 
			Log.info("RGOR.getObjectID initialization exception:"+x);
			return "";
		}
		return getClassIndex(parent);
	}	

	public static String getClassIndex(TestObject obj){ 
		String theClassIndex = "";
		String	domainname = (String) obj.getDomain().getName();
		String	classname = obj.getObjectClassName();
		
		boolean exthrown = false;
		try{ theClassIndex = (String)obj.getProperty(".classIndex");}
		catch(Exception x){exthrown=true;}
		// what if the text is blank?
		// we may have to do special checks based on type of component
		if (!exthrown && theClassIndex!=null && theClassIndex.length()>0) return theClassIndex;
		
				
		// for flex domain
		try{ 
	        if (obj.getDomain().getName().toString().equalsIgnoreCase(Domains.FLEX_DOMAIN)){
	        	theClassIndex = FlexUtil.getObjectClassIndex(obj);
	        	if (theClassIndex.trim().length()>0) return theClassIndex;
	        }
		}catch(Exception x){;} // RFT does throw NullPointerException if theClassIndex=null
		
		return "";
	}
	
}

