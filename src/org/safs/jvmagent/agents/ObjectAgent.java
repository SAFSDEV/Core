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
package org.safs.jvmagent.agents;

import java.lang.reflect.*;
import java.util.Vector;
import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.TestRecordData;
import org.safs.jvmagent.AlternateAncestorUser;
import org.safs.jvmagent.InvalidAgentException;
import org.safs.jvmagent.LocalAgent;
import org.safs.jvmagent.LocalAgentFactory;
import org.safs.jvmagent.LocalAgentFactoryUser;
import org.safs.jvmagent.LocalSubItemsAgent;
import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.jvmagent.SAFSActionUnsupportedRuntimeException;
import org.safs.jvmagent.SAFSInvalidActionArgumentRuntimeException;
import org.safs.jvmagent.SAFSObjectNotFoundRuntimeException;
import org.safs.jvmagent.SAFSSubItemsAgentUnsupportedRuntimeException;
import org.safs.reflect.Reflection;


/**
 * 
 * @author  Carl Nagle
 * @since   FEB 16, 2005
 *
 * Feb 17, 2006 (Szucs) extending the class with the LocalSubItemsAgent interface
 **/
public class ObjectAgent implements LocalAgent, 
                                       LocalAgentFactoryUser, 
                                       AlternateAncestorUser, LocalSubItemsAgent {

	/** 
	 * "Object"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "Object";

	/**
	 * Constructor for ObjectAgent.
	 */
	public ObjectAgent() {
		super();
		// ensure no alternate ancestor
		this.setAlternateAncestorClassname(null);
	}

	/**
	 * @param parent -- the actual object or component to be checked -- not a pseudo reference.
	 * @return 0
	 * @throws SAFSActionUnsupportedRuntimeException("ChildCount Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object parent) {
		throw new SAFSActionUnsupportedRuntimeException("ChildCount Unsupported");
	}

	/**
	 * @param parent -- the actual object or component to be checked -- not a pseudo reference.
	 * @return new String[0]
	 * @throws SAFSActionUnsupportedRuntimeException("Children Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		throw new SAFSActionUnsupportedRuntimeException("Children Unsupported");
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return null
	 * @throws SAFSActionUnsupportedRuntimeException("Caption Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getCaption(Object)
	 */
	public String getCaption(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("Caption Unsupported");
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return null
	 * @throws SAFSActionUnsupportedRuntimeException("Name Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getName(Object)
	 */
	public String getName(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("Name Unsupported");
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return null
	 * @throws SAFSActionUnsupportedRuntimeException("ID Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getID(Object)
	 */
	public String getID(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("ID Unsupported");
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return null
	 * @throws SAFSActionUnsupportedRuntimeException("Text Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getText(Object)
	 */
	public String getText(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("Text Unsupported");
	}

	
	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return String[] of derived property names
	 * @see org.safs.jvmagent.LocalAgent#getPropertyNames(Object)
	 */
	public String[] getPropertyNames(Object object) {
		return Reflection.reflectPropertyNames(object);
	}


	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @param property -- case-sensitive name of the property to retrieve
	 * @return property value which may be null
	 * @throws SAFSInvalidActionArgumentRuntimeException(property)
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object")
	 * @see org.safs.jvmagent.LocalAgent#getProperty(Object, String)
	 */
	public String getProperty(Object object, String property) {
		try{ return Reflection.reflectProperty( object, property ); }
		catch(NoSuchPropertyException nsp){
			return null;
		}
	}


	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return object.getClass().getName()
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object")
	 * @see org.safs.jvmagent.LocalAgent#getClassName(Object)
	 */
	public String getClassName(Object object) {
		if (object == null)
			throw new SAFSObjectNotFoundRuntimeException("Invalid Object");
			
		return object.getClass().getName();
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @ return -1 (unknown level)
	 * @throws SAFSActionUnsupportedRuntimeException("Level Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getLevel(Object)
	 */
	public int getLevel(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("Level Unsupported");
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return false
	 * @throws new SAFSActionUnsupportedRuntimeException("Showing Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#isShowing(Object)
	 */
	public boolean isShowing(Object object) {
		throw new SAFSActionUnsupportedRuntimeException("Showing Unsupported");
	}

    /**
     * Return true if the object is still valid/finadable in the JVM.  
     * This is normally already handled by the Agent using the LocalAgentFactory 
     * and finding the actual Component objects in the JVM.  Thus, this function is 
     * not normally called in this class or its subclasses.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is not null.
     */
    public boolean isValid(Object object){
		return (object instanceof Object);
    }

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @return Array of class names.  String[0] = "java.lang.Object" and on up.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid Object")
	 * @see org.safs.jvmagent.LocalAgent#getSuperClassNames(Object)
	 */
	public String[] getSuperClassNames(Object object) {
		if (object == null)
			throw new SAFSObjectNotFoundRuntimeException("Invalid Object");

		String[] rc = new String[0];
		Vector classes = new Vector();
		Class aclass = object.getClass();
		do{
			classes.add(aclass.getName());
			aclass = aclass.getSuperclass();
		}while(aclass != null);
		rc = new String[classes.size()];
		for(int vi=classes.size()-1, si=0; si < classes.size(); vi--, si++)
		    rc[si] = (String) classes.get(vi);
		return rc;
	}

        
	public Object getSubItemAtIndex(Object object, int index) throws Exception {
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("SubItemAtIndex Unsupported");	            
        } 
        
        
	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @param thePath 
	 * @return null
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPathObject Unsupported")
	 * @see org.safs.jvmagent.LocalAgent#getMatchingPathObject(Object, String)
	 */
	public Object getMatchingPathObject(Object theObject, String thePath) throws Exception {
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPathObject Unsupported");	
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @param thePath
	 * @return false
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPath Unsupported")
	 * @see org.safs.jvmagent.LocalAgent#isMatchingPath(Object, String)
	 */
	public boolean isMatchingPath(Object theObject, String thePath) throws Exception {
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPath Unsupported");	
	}

	/**
	 * @param object -- the actual object or component to be checked -- not a pseudo reference.
	 * @param dataInfo
	 * @return new String[0][0]
	 * @throws SAFSActionUnsupportedRuntimeException("StringData Unsupported")
	 * @see org.safs.jvmagent.LocalAgent#getStringData(Object, Object)
	 */
	public String[][] getStringData(Object object, Object dataInfo) {
		throw new SAFSActionUnsupportedRuntimeException("StringData Unsupported");
	}

	/** 
	 * LocalAgentFactoryUser implementation.
	 * storage for LocalAgentFactory that instanced us. 
	 */
	protected LocalAgentFactory factory = null;

	/** 
	 * LocalAgentFactoryUser Interface.
	 * Set the LocalAgentFactory used by the class instance.
	 * This is normally set by the LocalAgentFactory itself when the User is instanced.
	 */
	public void setLocalAgentFactory (LocalAgentFactory factory){
		this.factory = factory;
	}

	/** 
	 * LocalAgentFactoryUser Interface.
	 * Retrieve any LocalAgentFactory set for this object.
	 * May be null if not set.
	 */
	public LocalAgentFactory getLocalAgentFactory () { return factory; }
	
	/**
	 * AlternateAncestorUser implementation.
	 * Subclasses can specify the fullclassname of an alternate LocalAgent instance 
	 * that should be used to satisfy "super.process()" calls instead of the class's 
	 * true superclass ancestor.  For example, org.safs.abbot.JFrameAgent might 
	 * specify "org.safs.abbot.FrameAgent" as the chosen LocalAgent to handle unknown 
	 * actions instead of its true superclass "org.safs.jvmagent.JFrameAgent".
	 * If no alternate processAncestor is specified then the true superclass is used 
	 * when forwarding process calls.
	 */
	protected String alternateAncestorClassname = null;
	protected LocalAgent alternateAncestor = null;
		
	/**
	 * AlternateAncestorUser interface.
	 */
	public LocalAgent getAlternateAncestor() 
	{ return alternateAncestor; }

	/**
	 * AlternateAncestorUser interface.
	 */
	public void setAlternateAncestor(LocalAgent ancestor) 
	{ alternateAncestor = ancestor; }

	/**
	 * AlternateAncestorUser interface.
	 */
	public String getAlternateAncestorClassname() 
	{ return alternateAncestorClassname; }

	/**
	 * AlternateAncestorUser interface.
	 */
	public void setAlternateAncestorClassname(String ancestorClassname) 
	{ alternateAncestorClassname = ancestorClassname; }


	/** 
	 * Used internally by processAncestor and subclasses to attempt to retrieve the 
	 * alternateAncestor locally or via any stored LocalAgentFactory.
	 * @return LocalAgent alternateAncestor stored or retrieved from the LocalAgentFactory 
	 * or a null if not set or available.
	 */
	protected LocalAgent getAncestorAgent(){
		String ancestorname = getAlternateAncestorClassname();
		Log.info("Getting alternate ancestor: "+ ancestorname);
		LocalAgent ancestor = getAlternateAncestor();
		if(ancestor == null){
			if((ancestorname instanceof String)&&
			   (this instanceof LocalAgentFactoryUser)){
			   	LocalAgentFactory factory = ((LocalAgentFactoryUser)this).getLocalAgentFactory();
			   	try{ ancestor = factory.getLocalAgent(ancestorname);}
			   	catch(InvalidAgentException x){;}
			}
		}
		return ancestor;
	}

	/**
	 * Process the action provided in the testRecordData. 
	 * Subclasses will override this method as necessary.
	 * <p>
	 * Subclasses in the org.safs.jvmagent package should always attempt to processAncestor 
	 * if they themselves do not handle the command in the testRecordData.  This is shown
	 * below:
	 * <code>
	 *	if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	 *		processAncestor(object, testRecordData);
	 * </code>
	 * Subclasses in other packages like org.safs.abbot should typically always call 
	 * super.process() if they themselves do not handle the command in the testRecordData.
	 * This is shown below:
	 * <code>
	 *	if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	 *		super.process(object, testRecordData);
	 * </code>
	 * In this way, the proper order of processing in-package classes and superclasses 
	 * is maintained.
	 * <p>
	 * An Agent may throw various types of Agent-specific RuntimeExceptions depending upon failure modes.
	 * @param testRecordData provides all the information needed by the Agent to perform the action and
	 * to get/set the process statuscode.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object")
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid testRecordData")
	 */
    public TestRecordData process(Object object, TestRecordData testRecordData){ 
		if (object == null)
			throw new SAFSObjectNotFoundRuntimeException("Invalid object");
		if (testRecordData == null)
			throw new SAFSInvalidActionArgumentRuntimeException("Invalid testRecordData");

    	// process command
    	Log.info("Processing in "+ getClass().getName());
    	
		// command still unprocessed?
		// all org.safs.jvmagent package subclasses must processAncestor().
		// all other package subclasses like org.safs.abbot should call super.process()
		if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
			return processAncestor(object, testRecordData);

		return testRecordData;
    }
	
	/**
	 * Attempts to retrieve the alternateAncestor if defined and execute process on that.
	 * If no alternate ancestor is found then we simply exit without modifying anything.
	 */
	public TestRecordData processAncestor(Object object, TestRecordData testRecordData){
		LocalAgent ancestor = getAncestorAgent();
		if (ancestor instanceof LocalAgent){
			return ancestor.process(object, testRecordData);
		}
		return testRecordData;
	}		
}
