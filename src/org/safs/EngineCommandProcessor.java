/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.safs.jvmagent.STAFLocalServer;
import org.safs.tools.drivers.DriverConstant;

/**
 * EngineCommandProcessor for Record Type: 'E'
 * Instantiated by ProcessRequest.
 * <p>
 * Engines utilizing the GUI processing functions of this class must provide an engine-specific 
 * version of their GuiObjectVector for this generic processor to function properly.  Once set 
 * the processor will gain access to engine-specific implementations of functionality.
 * <p>
 * @since   JUL 10, 2007
 * @author canagl JUL 10, 2007 Original Release
 * <br>    JunwuMa AUG 15, 2008 Add getEngineCommand().    
 * <br>    JunwuMa NOV 27, 2008 Add two engine commands, 'getAccessibleName' and 'getNonAccessibleName', used by STAFPC
 *                              to generate R-Strings according to if users need Accessible Name be found first.
 *                              In 'getName', AccessibleName and Name are coupled together. 'getName' is kept unchange so that
 *                              there is no impact on some users using it.
 * <br>    JunwuMa AUG  6, 2009 Added an engine command, enableDomains.      
 * <br>    DharmeshPatel FEB 11,2011 Added an engine command, 'isTopLevelPopupContainer'.    
 * <br>    Dharmesh4	May 25, 2011 Added an engine command, 'getDomainName' and 'getClassIndex' 
 * <br>    JunwuMa MAR 16, 2012 Adding engine command, 'getObjectRecognitionAtScreenCoords'.          
 * @see #setGuiObjectVector(GuiObjectVector)
 */
public class EngineCommandProcessor extends Processor {

	/** 'getDomainName' */
	public static final String COMMAND_GET_DOMAINNAME = "getDomainName";
	
	/** 'getCaption' */
	public static final String COMMAND_GET_CAPTION = "getCaption";

	/** 'getChildCount' */
	public static final String COMMAND_GET_CHILD_COUNT = "getChildCount";

	/** 'getChildren' */
	public static final String COMMAND_GET_CHILDREN = "getChildren";

	/** 'getClassName' */
	public static final String COMMAND_GET_CLASSNAME = "getClassName";

	/** 'getClassIndex' */
	public static final String COMMAND_GET_CLASSINDEX = "getClassIndex";
	
	/** 'getID' */
	public static final String COMMAND_GET_ID = "getID";

	/** 'getLevel' */
	public static final String COMMAND_GET_LEVEL = "getLevel";

	/** 'getMatchingChildObjects' */
	public static final String COMMAND_GET_MATCHING_CHILD_OBJECTS = "getMatchingChildObjects";

	/** 'getMatchingParentObject' */
	public static final String COMMAND_GET_MATCHING_PARENT_OBJECT = "getMatchingParentObject";

	/** 'getMatchingPathObject' */
	public static final String COMMAND_GET_MATCHING_PATH_OBJECT = "getMatchingPathObject";

	/** 'getName' */
	public static final String COMMAND_GET_NAME = "getName";

	/** 'getAccessibleName' */
	public static final String COMMAND_GET_ACCESSIBLENAME =	"getAccessibleName";
	
	/** 'getNonAccessibleName' */
	public static final String COMMAND_GET_NONACCESSIBLENAME =	"getNonAccessibleName";

	/** 'getProperty' */
	public static final String COMMAND_GET_PROPERTY = "getProperty";

	/** 'getPropertyNames' */
	public static final String COMMAND_GET_PROPERTY_NAMES = "getPropertyNames";

	/** 'getStringData' */
	public static final String COMMAND_GET_STRING_DATA = "getStringData";

	/** 'getSuperClassNames' */
	public static final String COMMAND_GET_SUPER_CLASSNAMES = "getSuperClassNames";

	/** 'getText' */
	public static final String COMMAND_GET_TEXT = "getText";

	/** 'getTopLevelCount' */
	public static final String COMMAND_GET_TOPLEVEL_COUNT = "getTopLevelCount";

	/** 'getTopLevelWindows' */
	public static final String COMMAND_GET_TOPLEVEL_WINDOWS = "getTopLevelWindows";

	/** 'isMatchingPath' */
	public static final String COMMAND_IS_MATCHING_PATH = "isMatchingPath";

	/** 'isShowing' */
	public static final String COMMAND_IS_SHOWING = "isShowing";

	/** 'isValid' */
	public static final String COMMAND_IS_VALID = "isValid";

	/** 'setActiveWindow' */
	public static final String COMMAND_SET_ACTIVE_WINDOW = "setActiveWindow";

	/** 'enableDomains' */
	public static final String COMMAND_ENABLE_DOMAINS 	 = "enableDomains";
	
	/** 'isTopLevelPopupContainer' */
	public static final String COMMAND_IS_TOPLEVEL_POPUP_CONTAINER = "isTopLevelPopupContainer";
	
	/** 'getObjectRecognitionAtScreenCoords' */
	public static final String COMMAND_GET_OBJECTRECOGNITION_ATSCREENCOORDS = "getObjectRecognitionAtScreenCoords"; 
	
	/** Stores engine-specific GuiObjectVector object for use */
	GuiObjectVector gov = null;
	
	/** Convenient storage of testRecordData.getCommand() value */
	protected String command = null;
	
	/**
	 * 
	 */
	public EngineCommandProcessor() {
		super();
	}

	/**
	 * Supports 'E'
	 * @see org.safs.Processor#isSupportedRecordType(java.lang.String)
	 * @see org.safs.Processor#isEngineCommandRecord(java.lang.String)
	 */
	public boolean isSupportedRecordType(String recordType) {
		return isEngineCommandRecord(recordType);
	}
	
	/**
	 * <p>
	 * By default executes setRecordProcessed(false) if no chainedProcessor is present.
	 * @since JUL 10, 2007 CANAGL
	 */
	public void process(){
		Log.info("ECP.process testDomains: "+ testDomains);
		try{ 
			//params to contain fields 2-N
			params = interpretFields();
			if (command.equalsIgnoreCase(COMMAND_ENABLE_DOMAINS)){
				_enableDomains();
			}else if (command.equalsIgnoreCase(COMMAND_GET_DOMAINNAME)){
				_getDomainName();
			}else if (command.equalsIgnoreCase(COMMAND_GET_CAPTION)){
				_getCaption();
			}else if (command.equalsIgnoreCase(COMMAND_GET_CHILD_COUNT)){
				_getChildCount();
			}else if (command.equalsIgnoreCase(COMMAND_GET_CHILDREN)){
				_getChildren();
			}else if (command.equalsIgnoreCase(COMMAND_GET_CLASSNAME)){
				_getClassName();
			}else if (command.equalsIgnoreCase(COMMAND_GET_CLASSINDEX)){
				_getClassIndex();
			}else if (command.equalsIgnoreCase(COMMAND_GET_ID)){
				_getID();
			}else if (command.equalsIgnoreCase(COMMAND_GET_LEVEL)){
				_getLevel();
			}else if (command.equalsIgnoreCase(COMMAND_GET_MATCHING_CHILD_OBJECTS)){
				_getMatchingChildObjects();
			}else if (command.equalsIgnoreCase(COMMAND_GET_MATCHING_PARENT_OBJECT)){
				_getMatchingParentObject();
			}else if (command.equalsIgnoreCase(COMMAND_GET_MATCHING_PATH_OBJECT)){
				_getMatchingPathObject();
			}else if (command.equalsIgnoreCase(COMMAND_GET_NAME)){
				_getName();
			}else if (command.equalsIgnoreCase(COMMAND_GET_ACCESSIBLENAME)){
				_getAccessibleName();
			}else if (command.equalsIgnoreCase(COMMAND_GET_NONACCESSIBLENAME)){
				_getNonAccessibleName();
			}else if (command.equalsIgnoreCase(COMMAND_GET_PROPERTY)){
				_getProperty();
			}else if (command.equalsIgnoreCase(COMMAND_GET_PROPERTY_NAMES)){
				_getPropertyNames();
			}else if (command.equalsIgnoreCase(COMMAND_GET_STRING_DATA)){
				_getStringData();
			}else if (command.equalsIgnoreCase(COMMAND_GET_SUPER_CLASSNAMES)){
				_getSuperClassNames();
			}else if (command.equalsIgnoreCase(COMMAND_GET_TEXT)){
				_getText();
			}else if (command.equalsIgnoreCase(COMMAND_GET_TOPLEVEL_COUNT)){
				_getTopLevelCount();
			}else if (command.equalsIgnoreCase(COMMAND_GET_TOPLEVEL_WINDOWS)){
				_getTopLevelWindows();
			}else if (command.equalsIgnoreCase(COMMAND_IS_MATCHING_PATH)){
				_isMatchingPath();
			}else if (command.equalsIgnoreCase(COMMAND_IS_SHOWING)){
				_isShowing();
			}else if (command.equalsIgnoreCase(COMMAND_IS_VALID)){
				_isValid();
			}else if (command.equalsIgnoreCase(COMMAND_SET_ACTIVE_WINDOW)){
				_setActiveWindow();
			}else if(command.equalsIgnoreCase(COMMAND_IS_TOPLEVEL_POPUP_CONTAINER)){
				_isTopLevelPopupContainer();
			}else if(command.equalsIgnoreCase(COMMAND_GET_OBJECTRECOGNITION_ATSCREENCOORDS)){
				_getObjectRecognitionAtScreenCoords();
			}else{
				setRecordProcessed(false);
			}
		}
		catch(SAFSException e){
			//TODO: Exception thrown by interpretFields has already logged failure
			Log.debug("ECP.process "+ e.getClass().getSimpleName());
			setRecordProcessed(false);
		}
		catch(Exception x){
			Log.debug("ECP.process "+ x.getClass().getSimpleName(), x);
			setRecordProcessed(false);
		}
		
		if (! isRecordProcessed()) super.process();
	}
	
	/**
	 * params[0] = component key
	 */
	private void _isTopLevelPopupContainer() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _parentKey = (String) iterator.next();
		testRecordData.setStatusInfo(Boolean.toString(gov.isTopLevelPopupContainer(_parentKey)));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}
	
	/**
	 * params[0] = domain string, formated like "Java,Html,Win"
	 */
	private void _enableDomains() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _domainString = (String) iterator.next();
		Log.info("ECP."+ command +" processing: testDomains = "+ _domainString);
	    Processor.setTestDomains(_domainString);
	    Domains.enableDomains(_domainString);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);		
	}
	/**
	 * params[0] = component key
	 */
	private void _setActiveWindow() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		Log.info("ECP."+ command +" processing "+ _comp.getClass().getName()+":"+_comp.toString());
		gov.setActiveWindow(_comp);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}

	/**
	 * params[0] = component key
	 */
	private void _isValid() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		Log.info("ECP."+ command +" processing "+ _comp.getClass().getName()+":"+_comp.toString());
	    testRecordData.setStatusInfo(Boolean.toString(gov.isValidGuiObject(_comp)));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
		
	}

	/**
	 * params[0] = component key
	 */
	private void _isShowing() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" processing "+ item.getClass().getName()+":"+item.toString());
	    testRecordData.setStatusInfo(Boolean.toString(rec.isObjectShowing(item)));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}

	/**
	 * params[0] = component key
	 * params[1] = node path "Root->Branch->Leaf"
	 */
	private void _isMatchingPath() {
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		String _path = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" processing "+ item.getClass().getName()+":"+item.toString());
	    testRecordData.setStatusInfo(Boolean.toString(rec.isMatchingPath(item,_path)));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}

	/**
	 * No params
	 */
	private void _getTopLevelWindows() {
		Log.info("ECP."+ command +" processing...");
		String[] wins = (String[])gov.getParentObjects();
		if(wins==null)wins = new String[0];
		testRecordData.setStatusInfo(convertToDelimitedString(wins));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}

	/**
	 * No params
	 */
	private void _getTopLevelCount() {
		Log.info("ECP."+ command +" processing...");
		Object[] wins = gov.getParentObjects();	
		if (wins==null) wins = new Object[0];
	    testRecordData.setStatusInfo((Integer.toString(wins.length)).trim());
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?
	}

	/**
	 * params[0] = component key
	 */
	private void _getText() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _text = rec.getObjectText(item);
	    if ((_text==null)||(_text.trim().length()==0)){
	    	_text=DriverConstant.SAFS_NULL;
	    }
	    testRecordData.setStatusInfo(_text);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?		
	}

	/**
	 * params[0] = component key
	 */
	private void _getSuperClassNames() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String [] names = rec.getObjectSuperClassNames(item);
		testRecordData.setStatusInfo(convertToDelimitedString(names));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?		
	}

	/**
	 * params[0] = component key
	 * params[1] = data type
	 */
	private void _getStringData() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		String _type = (String) iterator.next();

		//TODO: getStringData not implemented yet
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	}

	/**
	 * params[0] = component key
	 */
	private void _getPropertyNames() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" processing "+ item.getClass().getName()+":"+item.toString());
	    String [] names = rec.getObjectPropertyNames(item);
		testRecordData.setStatusInfo(convertToDelimitedString(names));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?		
	}

	/**
	 * params[0] = component key
	 * params[1] = property name
	 * 
	 */
	private void _getProperty() {
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		String _prop = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" '"+ _prop +"' processing "+ item.getClass().getName()+":"+item.toString());
		String result = rec.getObjectProperty(item,_prop); // can be null
		if(result==null){
			testRecordData.setStatusInfo(DriverConstant.SAFS_NULL);			
		}else{
			testRecordData.setStatusInfo(result);
		}
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 * 
	 */
	private void _getName() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _name = rec.getObjectAccessibleName((item));
	    if((_name==null)||(_name.trim().length()==0))
	    	_name = rec.getObjectName(item);
	    if((_name==null)||(_name.trim().length()==0))
	    	_name = DriverConstant.SAFS_NULL;
		testRecordData.setStatusInfo(_name);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getAccessibleName() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _name = rec.getObjectAccessibleName((item)); 
	    if((_name==null)||(_name.trim().length()==0))
	    	_name = DriverConstant.SAFS_NULL;
		testRecordData.setStatusInfo(_name);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}	
	
	/**
	 * params[0] = component key
	 */
	private void _getNonAccessibleName() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _name = rec.getObjectName((item)); 
	    if((_name==null)||(_name.trim().length()==0))
	    	_name = DriverConstant.SAFS_NULL;
		testRecordData.setStatusInfo(_name);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}
	
	/**
	 * params[0] = component key
	 * params[1] = node path ("Root->Branch->Leaf")
	 */
	private void _getMatchingPathObject() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		String _path = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    Object _obj = rec.getMatchingPathObject(item,_path);
	    if(_obj==null){
			Log.info("ECP."+ command +" found no matching path object for:"+ _path);
			testRecordData.setStatusInfo(DriverConstant.SAFS_NULL);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			return;
	    }
		Log.info("ECP."+ command +" found path object "+ _obj.getClass().getName()+":"+_obj.toString());
	    String key = null;
	    if(_obj instanceof String){
	    	key = (String) _obj;
	    }else{
	    	key = gov.makeUniqueCacheKey(_obj);
	    	gov.putCachedItem(key, _obj);
	    }
		testRecordData.setStatusInfo(key);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = recognition info
	 */
	private void _getMatchingParentObject() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    gov.setPathVector(_comp);
	    gov.initGuiObjectRecognition();
	    Object _obj = gov.getMatchingParentObject();	
	    if(_obj==null){
			Log.info("ECP."+ command +" found no matching object for:"+ _comp);
			testRecordData.setStatusInfo(DriverConstant.SAFS_NULL);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			return;
	    }
		Log.info("ECP."+ command +" found parent "+ _obj.getClass().getName()+":"+_obj.toString());
	    String key = null;
	    if(_obj instanceof String){
	    	key = (String) _obj;
	    }else{
	    	key = gov.makeUniqueCacheKey(_obj);
	    	gov.putCachedItem(key, _obj);
	    }
		Log.info("ECP."+ command +" returning parent String key:"+ key);
		testRecordData.setStatusInfo(key);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * Currently we only return a single element array or SAFS_NULL.
	 * params[0] = parent key
	 * params[1] = recognition info
	 */
	private void _getMatchingChildObjects() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		String _info = (String) iterator.next();
	    gov.setPathVector(_info);
	    gov.initGuiObjectRecognition();
	    
	    Object _child = gov.getMatchingChildObject(_comp, null);

	    // should already be a cached String key if not null
		testRecordData.setStatusInfo(DriverConstant.SAFS_NULL);			
	    if(_child!=null){
		    try{
		    	String[] child = {(String)_child};
		    	testRecordData.setStatusInfo(convertToDelimitedString(child));
		    }
		    catch(ClassCastException cce){
		    	Log.info("ECP."+command +" child reference error.");
		    }
		}
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?		
	}

	/**
	 * params[0] = component key
	 */
	private void _getLevel() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		testRecordData.setStatusInfo(String.valueOf(rec.getObjectLevel(item)).trim());
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getID() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _id = rec.getObjectID(item);
	    if ((_id==null)||(_id.trim().length()==0)){
	    	_id=DriverConstant.SAFS_NULL;
	    }
		testRecordData.setStatusInfo(_id);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}
	
	/**
	 * params[0] = component key
	 */
	private void _getClassIndex() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _index = rec.getObjectClassIndex(item);
	    if ((_index==null)||(_index.trim().length()==0)){
	    	_index=DriverConstant.SAFS_NULL;
	    }
		testRecordData.setStatusInfo(_index);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getDomainName() {
		Log.info("ECP."+ command +" processing...");
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
	    String _index = rec.getObjectDomain(item);
	    if ((_index==null)||(_index.trim().length()==0)){
	    	_index=DriverConstant.SAFS_NULL;
	    }
		testRecordData.setStatusInfo(_index);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}
	
	/**
	 * params[0] = component key
	 */
	private void _getClassName() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" processing "+ item.getClass().getName()+":"+item.toString());
	    String _name = rec.getObjectClassName(item);
	    if ((_name==null)||(_name.trim().length()==0)){
	    	_name=DriverConstant.SAFS_NULL;
	    }
		testRecordData.setStatusInfo(_name);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getChildren() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		Log.info("ECP."+ command +" processing "+ _comp.getClass().getName()+":"+_comp.toString());
		Object[] kids = gov.getChildObjects(_comp);
	    String[] keys = new String[kids.length];
    	for(int i=0;i<kids.length;i++){
    		if(kids[i] instanceof String){
    			keys[i]=(String)kids[i];
    		}else{
    			keys[i]=gov.makeUniqueCacheKey(kids[i]);
    			gov.putCachedItem(keys[i],kids[i]);
    		}
    		Log.info("ECP."+ command +" item cached = "+ gov.getCachedItem(keys[i]).getClass().getName());
    	}
		testRecordData.setStatusInfo(convertToDelimitedString(keys));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getChildCount() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
		Log.info("ECP."+ command +" processing "+ _comp.getClass().getName()+":"+_comp.toString());
		Object[] kids = gov.getChildObjects(_comp);
		for(int i=0;i<kids.length;i++){
			gov.removeCachedItem(kids[i]);
		}
		testRecordData.setStatusInfo(String.valueOf(kids.length).trim());
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/**
	 * params[0] = component key
	 */
	private void _getCaption() {
		if(! validateParamSize(1)) return;
	    Iterator iterator = params.iterator();
	    String _comp = (String) iterator.next();
	    GuiObjectRecognition rec = gov.createGuiObjectRecognition(_comp, -1);
	    Object item = gov.getCachedItem(_comp);
		Log.info("ECP."+ command +" processing "+ item.getClass().getName()+":"+item.toString());
		String _caption = rec.getObjectCaption(item);
		if((_caption==null)||(_caption.trim().length()==0)){
			_caption = DriverConstant.SAFS_NULL;
		}
		testRecordData.setStatusInfo(_caption);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		//logging?				
	}

	/************************************************************************************
	 * @return Returns the engine-specific GuiObjectVector.
	 */
	public GuiObjectVector getGuiObjectVector() {
		return gov;
	}
	
	/**
	 * @param gov The engine-specific GuiObjectVector to set.
	 */
	public void setGuiObjectVector(GuiObjectVector gov) {
		this.gov = gov;
	}
	/**
	 * @return Returns the engine command.
	 */
	public String getEngineCommand() {
		return command;
	}

	  /** 
	   ** Interprets the fields of the test record and puts the appropriate
	   ** values into the fields of testRecordData.  Also sets the 'command' convenience 
	   ** field.
	   ** <br><em>Side Effects:</em> {@link #testRecordData} fields are set from the inputRecord.
	   ** <br><em>State Read:</em>   {@link #testRecordData}, the inputRecord field
	   ** <br><em>Assumptions:</em>  The following order:
	   ** <p><code>
	   **      Field #1:   The TEST record type (E).
	   ** </code><p>
	   **      Subsequent fields would be as follows (with a separator between each field):
	   ** <code>
	   ** <br> Field:  #2            #3-N      
	   ** <br> ==============  ==============
	   ** <br> ENGINECOMMAND,  [PARAMETER(S),]
	   ** </code>
	   * @return Collection of the parameter(s)
	   **/
	  protected Collection interpretFields () throws SAFSException {
	    String methodName = "interpretFields";
	    Collection params = new ArrayList();
	    String nextElem = ""; // used to log errors in the catch blocks below
	    int tokenIndex = 1; // start from 1, because we already have the recordType which was 0
	    try {
	      nextElem = "command"; //..get the command, the 2nd token (from 1)
	      String command = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	      testRecordData.setCommand(command);
	      this.command = command;
	      
	      for(tokenIndex = 2; tokenIndex < testRecordData.inputRecordSize(); tokenIndex++) {
	        nextElem = "param"; //..get the param, tokens #3 - N (from 1)
	        String param = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	        params.add(param);
	      }
	    } catch (IndexOutOfBoundsException ioobe) {
	      log.logMessage(testRecordData.getFac(), "EngineCommandProcessor.doRequest field index #"+ tokenIndex +" error processing inputRecord: "+
	                testRecordData.getInputRecord(), FAILED_MESSAGE);
	      throw new SAFSException(this, methodName, ioobe.getMessage()); // this should never happen
	    } catch (SAFSException e) {
		      log.logMessage(testRecordData.getFac(), "EngineCommandProcessor.doRequest error processing inputRecord: "+
	                testRecordData.getInputRecord() + "\n" +
	                e.getMessage(), FAILED_MESSAGE);
	      throw e; // this only happens if we don't have token 1
	    }
	    return params;
	  }

	  /**
	   * find a unique separator using STAFLocalServer.getUniqueSeparator and convert 
	   * the array to a single string of separated values.  The first character in the 
	   * returned string defines the delimiter used to separate the items. If there are 
	   * no items in the provided array (length==0) then we return a 0-length (empty) 
	   * String.
	   * @param items
	   * @return character delimited String of fields
	   * @see STAFLocalServer#getUniqueSeparator(String)
	   */
	  protected String convertToDelimitedString(String[] items){
		String result = "";
		for(int i=0;i<items.length;i++){
			result +=items[i]; 
		}
		String s = STAFLocalServer.getUniqueSeparator(result);
		result = "";
		for(int i=0;i<items.length;i++){
			result += s + items[i]; 
		}
		return result;
	  }
	  
	  /**
	   * get recognition string of a GUI object at screen coordinates.
	   * the recognition string depends on specific engine.
	   * 
	   * params[0] = x coords on screen 
	   * params[1] = y coords on screen
	   * 
	   * @see #recognitionOfObjectAtPoint(int, int)
	   */	  
	protected void _getObjectRecognitionAtScreenCoords() {
		String debugMsg = getClass().getSimpleName()+"._getObjectRecognitionAtScreenCoords(): ";
		Log.info("ECP."+ getEngineCommand() +" processing...");
		if(! validateParamSize(2)) return;
		  
		Iterator iterator = params.iterator();
		String strX = (String) iterator.next(); 	 // x-coordination
		String strY = (String) iterator.next();	     // y-coordination

		int x = 0;
		int y = 0;
		try {
			x = Integer.parseInt(strX);
			y = Integer.parseInt(strY);
		}catch(NumberFormatException nfe){
		    Log.debug(debugMsg + nfe.toString());
		}
		
	    String recogString = recognitionOfObjectAtPoint(x, y);
	    if ((recogString==null)||(recogString.trim().length()==0)){
	    	recogString=DriverConstant.SAFS_NULL;
	    }
		testRecordData.setStatusInfo(recogString);
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
	
	//to be implemented in derived class to bind to specific testing tool
	protected String recognitionOfObjectAtPoint(int x, int y) { return ""; }
}
