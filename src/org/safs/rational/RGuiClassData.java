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

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;

import org.safs.*;

import java.util.*;


/**
 * External users would normally not use this class directly.<br>
 * Consequently, the API and associated data is subject to change without notice.
 * <p>
 * Extends org.safs.GuiClassData to use Rational specific mechanisms for 
 * identifying class hierarchy in TestObjects.
 * 
 * @author Carl Nagle, JUL 03, 2003 Updated documentation.
 *  
 * <br>	SEP 03, 2008	(Lei Wang)	Add method getMappedNetClassType()
 * 									Modify method getMappedClassType()
 * <br> FEB 13, 2009    (JunwuMa)   Modify getMappedClassType(), fix S0561499 about how to judge if a Html.SELECT is ComboBox.
 *                                  See the same behavior about Html.SELECT in org.safs.jvmagent.LocalServerGuiClassData.getMappedClassType().
 * <br> JUN 18, 2009    (Carl Nagle)    Fixed IllegalArgumentExceptions from isSameObject() call with Mapped Objects.
 * <br> NOV 24, 2010    (Carl Nagle)    Fixed NullPointerException resulting from getTopParent returning null.
 *                                  Also fixed some generic and/or Flex issues with getMappedClassType
 * 
 *   
 * Copyright (C) (SAS) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class RGuiClassData extends GuiClassData{

	public RGuiClassData(){super();}

	/**
	 * Retrieves the class Type we have stored for the provided class name (if any).
	 * We will cycle through all possible superclasses (except Object) looking for 
	 * a match to a known superclass.  For example, we want to find out that the 
	 * input swingapp.SwingAppFrame--which we don't know about--is actually a 
	 * subclass of javax.swing.JFrame--something we know how to handle.
	 * <p>
	 * Overrides the superclass to handle Rational TestObject proxies.
	 * 
	 * 
	 * @param classname the actual classname sought as a known class type.  This is 
	 *                  really just the name of the proxied class as provided in 
	 *                  theObject.  On rare occassions this can be NULL!!!
	 * 
	 * @param theObject the TestObject proxy to evaluate for class hierarchy.
	 * 
	 * @return the class Type (i.e. JavaPanel) for the provided class name (i.e. javax.swing.JPanel).
	 *         <CODE>Window,[classname]</CODE> if this is a top-level object without 
	 *         a mapping. <CODE>Generic</CODE> if no mapped type is found otherwise.
	 */
	public String getMappedClassType(String classname, Object anObject){
		
		String typeclass = null;
		if (classname == null) {
			try{ typeclass = (String) ((TestObject)anObject).getDomain().getName();}
			catch(Exception x){	
				Log.debug("RGCD.getMappedClassType IGNORING getDomain Exception:"+ x.getClass().getName(), x);
				typeclass = "Generic"; 
			}
			return typeclass;
		}
		try{
			typeclass = classmap().getProperty(classname, null);
			Log.info("RGCD.getMappedClassType initial lookup for "+ classname +"="+ typeclass);
		}
		catch(Exception ex) { 				
			Log.debug("RGCD.getMappedClassType IGNORING inital lookup Exception:"+ ex.getMessage() +" "+ ex.getClass().getName(), ex);
		}
		
		if (typeclass == null) {
			try{ 
                if (classname.toLowerCase().indexOf("html.")>=0) {
                	classmap().setProperty(classname, classname);
                    return classname;
                }
                TestObject theObject = (TestObject) anObject;                
                String domainname = (String) theObject.getDomain().getName();
                
                // how shall we process Win and Net hierarchies?
                if ((domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME))||
                	(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_HTML_DOMAIN_NAME))){
                	typeclass = getMappedJavaClassType(classname, theObject);
                	if(typeclass != null) classmap().setProperty(classname, typeclass);
                }else if(domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME)){
                	typeclass = getMappedNetClassType(classname, theObject);
                	if(typeclass != null) classmap().setProperty(classname, typeclass);
                }else {
                	TestObject topobj = theObject.getTopParent();
                	if(topobj==null){
                		Log.info("RGCD.getMappedClassType object.getTopParent returns null.");
                		Log.info("RGCD.getMappedClassType defaulting top parent type to 'Window'.");
                		typeclass = "Window,"+ classname.trim();
                		if(typeclass != null) classmap().setProperty(classname, typeclass);
                	}else{
	                	try{
	                		if (theObject.isSameObject(topobj)) {                	
		                		Log.info("RGCD: defaulting top parent type to 'Window'.");
		                		typeclass = "Window,"+ classname.trim();;
		                		if(typeclass != null) classmap().setProperty(classname, typeclass);
		                		
	                		}else if (domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_FLEX_DOMAIN_NAME)){
	                			try{
	                				TestObject parentobj = theObject.getParent();
	                				String parentClassName = (String)parentobj.getProperty(FlexUtil.PROPERTY_TYPE_CLASSNAME);
	                				Log.info("RGCD: Evaluating Flex object's parent class: "+ parentClassName);
	                				if(FlexUtil.isRuntimeLoader(parentClassName)||FlexUtil.isSWFLoader(parentClassName)){
	                					typeclass = "FlexWindow,Window,"+ classname.trim();
	                					if(typeclass != null) classmap().setProperty(classname, typeclass);
	                				}else{ // not the top object and parent not a runtime loader
	                					String superclass = null;
	                					try{
	                						superclass = (String)theObject.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONCLASSNAME);
	                						if((superclass==null)||(superclass.length()==0))throw new PropertyNotFoundException(FlexUtil.PROPERTY_TYPE_AUTOMATIONCLASSNAME);
	                						try{ 
	                							typeclass = classmap().getProperty(superclass, null);
	    	                					if(typeclass != null) classmap().setProperty(classname, typeclass);
	                						}
	                						catch(Exception ex) { 				
	                							Log.info("RGCD: IGNORING Flex automationClassName lookup Exception:"+ ex.getMessage());
	                						}
	                					}catch(PropertyNotFoundException p){
	                						Log.info("RGCD: IGNORING Flex class: "+ classname +" PropertyNotFoundException: "+ p.getMessage());
	                					}
	                				}
	                			}catch(Exception p){}	                			
	                		}
	                	// IllegalArgumentException can be thrown if using mapped objects
	                	}catch(IllegalArgumentException iax){
	                		Log.info("RGCD.getMappedClassType ignoring isSameObject failure:"+ iax.getMessage());
	                	}
                	}
                }
			}
			catch(Exception ex) { 				
				Log.info("RGCD.getMappedClassType "+ ex.getClass().getSimpleName(), ex);
			}
		}
		// special processing for various classnames
		else if (classname.equalsIgnoreCase("HTML.SELECT")){
            TestObject theObject = (TestObject) anObject;            			
            String size = "";
            String multiple = "";
			Log.info("RGCD.getMappedClassType checking ComboBox/ListBox flags..."+ classname);
			try{ multiple = theObject.getProperty("multiple").toString();}
			catch(Exception x){;}finally{if(multiple==null) multiple = "";}
			try{ size = theObject.getProperty("size").toString();}
			catch(Exception x){;}finally{if(size==null) size = "";}
			if (multiple.equalsIgnoreCase("true")) {
				typeclass = "ListBox,HTMLListBox";}
			else if ((size.trim().equalsIgnoreCase("1"))||(size.equals(""))||(size.trim().equals("0"))) {
				typeclass = "ComboBox,HTMLComboBox";}
		}		
        Log.info("RGCD:classname: "+classname+", typeclass: "+typeclass);
		return (typeclass==null)? (String)classmap().setProperty(classname, "Generic"):typeclass;
	}		
	
	/**
	 * Retrieves Java class Type we have stored for the provided class name (if any).
	 * We will cycle through all possible superclasses (except Object) looking for 
	 * a match to a known superclass.  For example, we want to find out that the 
	 * input swingapp.SwingAppFrame--which we don't know about--is actually a 
	 * subclass of javax.swing.JFrame--something we know how to handle.
	 * <p>
	 * Overrides the superclass to handle Rational TestObject proxies.
	 * 
	 * 
	 * @param classname the actual classname sought as a known class type.  This is 
	 *                  really just the name of the proxied class as provided in 
	 *                  theObject.  On rare occassions this can be null!!!
	 * 
	 * @param theObject the TestObject proxy to evaluate for class hierarchy.
	 * 
	 * @return the class Type (i.e. JavaPanel) for the provided class name (i.e. javax.swing.JPanel).
	 *          <CODE>null</CODE> if no mapped type is found.
	 */
	protected String getMappedJavaClassType(String classname, Object anObject){

		String typeclass = null;
		if (classname == null) {
			try{ typeclass = (String) ((TestObject)anObject).getDomain().getName();}
			catch(Exception x){	
				Log.debug("RGCD.getMappedJavaClassType IGNORING getDomain Exception:"+ x.getClass().getName(), x);
				typeclass = "Generic"; 
			}
			return typeclass;
		}
		
		try{ 
            Log.info("RGCD: resolving Java classname: "+classname+", typeclass: "+typeclass);
            if (classname.toLowerCase().indexOf("html.")>=0) {
                return classname;
            }
            TestObject theObject = (TestObject) anObject;
			TestObject theClass = (TestObject) theObject.invoke("getClass");
			TestObject superClass  = null;
							
			Object baseObject = new Object();
			String basename   = baseObject.getClass().getName();

			while ((theClass != null ) &&
			       (typeclass == null))  {
				
				// this is NOT valid for non-java objects
				superClass = (TestObject) theClass.invoke("getSuperclass");
				classname  = (String) superClass.invoke("getName");					
				theClass.unregister();
				theClass = null;
				
				// don't handle top level Object class
				if (classname.equals(basename)){
					superClass.unregister();
					superClass = null;
					break;
				}
				
				typeclass = classmap().getProperty(classname, null);

				if (typeclass == null){						
					theClass = superClass;
					//superClass.unregister();
					superClass = null;
				}
				else{
					// succeeded and exiting loops
					superClass.unregister();
					superClass = null;
				}
			}							
		}
		catch(Exception ex) { 			
			Log.info("RGCD.getMappedJavaClassType "+ ex.getMessage() +" "+ ex.getClass().getName(), ex);
		}
		return typeclass;
	}
	
	/**
	 * Has the same functionality as getMappedJavaClassType()
	 * If we can not find the apporiate type for the parameter classname, we will try to use
	 * the ancestor of parameter anObject to get the mapping type.
	 * <p>
	 * @param classname the actual classname sought as a known class type.  This is 
	 *                  really just the name of the proxied class as provided in 
	 *                  theObject.  On rare occassions this can be null!!!
	 * 
	 * @param anObject the TestObject proxy to evaluate for class hierarchy.
	 * 
	 * @return the class Type (i.e. ListView) for the provided class name (i.e. System.Windows.Forms.ListView).
	 *          <CODE>null</CODE> if no mapped type is found.
	 */
	protected String getMappedNetClassType(String classname, Object anObject){

		String type = null;
		if (classname == null) {
			try{ type = (String) ((TestObject)anObject).getDomain().getName();}
			catch(Exception x){	
				Log.debug("RGCD.getMappedNetClassType IGNORING getDomain Exception:"+ x.getClass().getName(), x);
				type = "Generic"; 
			}
			return type;
		}
		
		try{ 
            Log.info("RGCD: resolving Net classname: "+classname+", type: "+type);
            TestObject theObject = (TestObject) anObject;
			TestObject theClass = DotNetUtil.getClazz(theObject);
			TestObject superClass  = null;

			while ((theClass != null ) && (type == null)){
				superClass = DotNetUtil.getSuperClazz(theClass);
				classname  = DotNetUtil.getClazzFullName(superClass);					
				theClass.unregister();
				theClass = null;
				
				// don't handle top level Object class
				if (classname.equals(DotNetUtil.CLASS_OBJECT_NAME)){
					superClass.unregister();
					superClass = null;
					break;
				}
				
				type = classmap().getProperty(classname, null);

				if (type == null){
					Log.info("RGCD: looking type (DotNet) for super class: "+classname);
					theClass = superClass;
					superClass = null;
				}else{
					// succeeded and exiting loops
					Log.info("RGCD: matched type (DotNet) for super class: "+classname);
					superClass.unregister();
					superClass = null;
				}
			}							
		}
		catch(Exception ex) { 			
			Log.info("RGCD.getMappedNetClassType "+ ex.getMessage() +" "+ ex.getClass().getName(), ex);
		}
		return type;
	}
}

