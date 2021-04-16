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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.safs.Domains;
import org.safs.EngineCommandProcessor;
import org.safs.GuiClassData;
import org.safs.GuiObjectRecognition;
import org.safs.GuiObjectVector;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;

import com.rational.test.ft.object.interfaces.CrossDomainContainer;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.ITopWindow;
import com.rational.test.ft.object.interfaces.IWindow;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexApplicationTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.sys.graphical.Mouse;


/**
 * REngineCommandProcessor works with RRobotJHook. Instantiated in RRobotJHook.initializeUtilities(). 
 * RGuiObjectVector (engine-specific) also is instantiated in RRobotJHook.initializeUtilities().
 * Engine commands that are not handled in this class, shall be handled by its parent class: EngineCommandProcessor.
 *
 * @since  AUG 14, 2008
 * @author JunwuMa AUG 14, 2008 Original Release
 *                         One feature: Engine command 'highlightMatchingChildObject' is able to locate 
 *                         a target GUI object, and highlight it by drawing its border in red if found.
 *                         This new engine command is called in STAFProcessContainer.
 * <br>    JunwuMa OCT 17, 3008 Add flex support.
 * <br>    JunwuMa NOV 07, 2008 Modified _highlightMatchingChildObject() supporting the top most GUI window can be highlighted.
 * <br>    JunwuMa OCT 22, 2009 Adding two commands getMatchingChildKeysAtPoint, highlightMatchingChildObjectByKey.
 * <br>    Lei Wang NOV 02, 2010 Adding method _getComponentRectangle().
 * <br>    JunwuMa MAR 15, 2011 Fixed the issue of highlight not working with RFT8.2 against Flex applications loaded by AutomationModuleRFT.swf.
 * <br>    JunwuMa MAR 16, 2012 Adding support for 'getObjectRecognitionAtScreenCoords' in RFT engine.  
 *                                            
 *                            
 */
public class REngineCommandProcessor extends EngineCommandProcessor {
	/** highlightMatchingChildObject */
	public static final String COMMAND_HIGHLIGHT_MATCHING_CHILD_OBJECT 	= "highlightMatchingChildObject";
	/** highlightMatchingChildObjectByKey */
	public static final String COMMAND_HIGHLIGHT_MATCHING_CHILD_OBJECT_BYKEY = "highlightMatchingChildObjectByKey";
	/** clearHighlightedDialog */
	public static final String COMMAND_CLEAR_HIGHLIGHTED_DIALOG 		= "clearHighlightedDialog";
	/** getMatchingChildKeysAtPoint */
	public static final String COMMAND_GET_MATCHING_CHILDKEYS_ATPOINT 	= "getMatchingChildKeysAtPoint";
	/** getComponentRectangle */
	public static final String COMMAND_GET_COMPONET_RECTANGLE 			= "getComponentRectangle";
	/** getTopWindowHandle */
	public static final String COMMAND_GET_TOP_WINDOW_HANDLE 			= "getTopWindowHandle";
	
	
	public REngineCommandProcessor() {
		super();
	}

	public void process(){
		Log.info("RECP.process testDomains: "+ testDomains);
		try{ 
			//params to contain fields 2-N
			params = interpretFields();
			String command = getEngineCommand();
			if (command.equalsIgnoreCase(COMMAND_HIGHLIGHT_MATCHING_CHILD_OBJECT)){
				_highlightMatchingChildObject();
				setRecordProcessed(true);
			}else if(command.equalsIgnoreCase(COMMAND_CLEAR_HIGHLIGHTED_DIALOG)){
				clearHighlightedDialog();
				setRecordProcessed(true);
			}else if(command.equalsIgnoreCase(COMMAND_GET_MATCHING_CHILDKEYS_ATPOINT)){
				_getMatchingChildKeysAtPoint();
				setRecordProcessed(true);
			}else if(command.equalsIgnoreCase(COMMAND_HIGHLIGHT_MATCHING_CHILD_OBJECT_BYKEY)){
				_highlightMatchingChildObjectByKey();
				setRecordProcessed(true);
			}else if(command.equalsIgnoreCase(COMMAND_GET_COMPONET_RECTANGLE)){
				_getComponentRectangle();
				setRecordProcessed(true);
			}else if(command.equalsIgnoreCase(COMMAND_GET_TOP_WINDOW_HANDLE)){
				_getTopWindowHandle();
				setRecordProcessed(true);
			}else{
				setRecordProcessed(false);
			}
		}
		catch(SAFSException e){
			//TODO: Exception thrown by interpretFields has already logged failure
			setRecordProcessed(false);
		}
		if (! isRecordProcessed()) super.process();
	}
	
	private boolean isFormatTopmostWindow(String rstring){
	    if (rstring.indexOf(";\\;") >= 0) 
	    	return false;
	    else if ( rstring.startsWith("Type=Window") || 
	    		  rstring.startsWith("Type=JavaWindow") || 
	    		  rstring.startsWith("Type=DotNetWindow") ||
	    		  rstring.startsWith("Type=FlexWindow"))
	    	return true;
		else
			return false;
	}

 	private void _highlightMatchingChildObject(){
		String debugMsg = getClass().getName()+"._highlightMatchingChildObject(): ";
		Log.info("RECP."+ getEngineCommand() +" processing...");
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String _parentRS = (String) iterator.next();  // R-String for top-most Window
	    String _compRS = (String) iterator.next();    // R-String for the GUI component

	    RGuiObjectVector rgov = (RGuiObjectVector)getGuiObjectVector();
	    TestObject _win = null;
	    TestObject _child = null;
	    try {
	    	if (_parentRS.equalsIgnoreCase(_compRS) || isFormatTopmostWindow(_compRS)) { // see if searching a top most window
	    		// get the target object for the top most window's R-string		
	    	    rgov.setPathVector(_compRS);
	    	    rgov.initGuiObjectRecognition();
	    	    _child = rgov.getTopTestObject(); 
	    	    _win = _child;
	    	} else {
	    		//get parent object		
	    	    rgov.setPathVector(_parentRS);
	    	    rgov.initGuiObjectRecognition();
	    	    _win = rgov.getTopTestObject();    		
	    		// get child object	as the target object	
	    		rgov.setPathVector(_compRS);
	    		rgov.initGuiObjectRecognition();
	    		_child = rgov.getChildTestObject(_win, null); 
	    	}
			// highlight this GuiTestObject
	    	if (_child!=null) {
	    		boolean ishighLighted = highLightTestObject((GuiTestObject)_win,(GuiTestObject)_child, _compRS);
	    		Log.info(debugMsg + (ishighLighted?"target object highlighted.":" target object not highlighted."));
	    	    testRecordData.setStatusInfo(Boolean.toString(ishighLighted)); // true: found.  for being used by the sender of this engine command.
	    	} else {
	    		Log.info(debugMsg+" target object not found.");
		    	testRecordData.setStatusInfo(Boolean.toString(false));
	    	}
	    }catch(Exception e){
	    	Log.debug(debugMsg + "Exception." + e.toString());
	    	Log.info(debugMsg + " target object not found or not highlighted. Exception:" + e);
	    	testRecordData.setStatusInfo(Boolean.toString(false));
	    }

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
 
 	private void _highlightMatchingChildObjectByKey(){
		String debugMsg = getClass().getName()+"._highlightMatchingChildObject(): ";
		Log.info("RECP."+ getEngineCommand() +" processing...");
		if(! validateParamSize(2)) return;
	    Iterator iterator = params.iterator();
	    String winKey = (String) iterator.next();
	    String compKey = (String) iterator.next();     // key to the cached object in cache
	    String rstring = (String) iterator.next();     // its r-string
		
	    RGuiObjectVector rgov = (RGuiObjectVector)getGuiObjectVector();
	    
	    // get the test object in cache
	    GuiTestObject cachedWinObj = null;
	    GuiTestObject cachedCompObj = null;
	    try{
	    	cachedWinObj = (GuiTestObject)rgov.getCachedItem(winKey);
	    	cachedCompObj = (GuiTestObject)rgov.getCachedItem(compKey);
	    }catch(Exception e){
	    	Log.debug(debugMsg + "Exception thrown:" +e.getMessage());
	    }
	    
		// highlight this GuiTestObject
	    if (cachedCompObj!=null) {
	    	boolean ishighLighted = false;
	    	try {
	    		ishighLighted = highLightTestObject(cachedWinObj, cachedCompObj, rstring);
	    	}catch(Exception npe) {
	    		Log.debug(debugMsg + "Exception thrown:" + npe.toString());
	    	}
	    	Log.info(debugMsg + (ishighLighted?"target object highlighted.":" target object not highlighted."));
	        testRecordData.setStatusInfo(Boolean.toString(ishighLighted)); // true: found.  for being used by the sender of this engine command.
	    } else {
	    	Log.info(debugMsg + " target object not found.");
		   	testRecordData.setStatusInfo(Boolean.toString(false));
	    }

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
 	private void clearHighlightedDialog(){
		String debugMsg = getClass().getName()+".clearHighlightedDialog(): ";
		Log.info("RECP."+ getEngineCommand() +" processing...");
		
	    try {
	    	if(transparentDialog!=null){
	    		transparentDialog.dispose();
	    		transparentDialog = null;
	    		testRecordData.setStatusInfo(Boolean.toString(true));
	    	}else{
	    		Log.info(debugMsg+" There is no highlighted dialog.");
	    	}
	    }catch(Exception e){
	    	Log.debug(debugMsg + "Exception." + e.toString());
	    	Log.info(debugMsg + " can not dispose the highlighted dialog. Exception:" + e);
	    	testRecordData.setStatusInfo(Boolean.toString(false));
	    }

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
 	
 	/**
 	 * Attempt to locate the child GuiTestObject at a specific point.
 	 * It is assumed anObject is a parent container of the desired child at some level.
 	 * anObject is normally a GuiTestObject, but can also be a CrossDomainContainer.
 	 * @param anObject -- normally a GuiTestObject or CrossDomainContainer
 	 * @return the child at the point or null.
 	 */
 	public static GuiTestObject _getChildAtPoint(TestObject anObject, Point pt){
    	Object childobj = anObject;
    	Object curobj = null;
    	TestObject[] children = null;
    	try{
	    	do {
	    		////childobj = (GuiTestObject)((GuiTestObject)curobj).getChildAtPoint(new Point(x,y));

	    		// getChildAtPoint API returns an Object.  Normally, this is a GuiTestObject,
	    		// but we now have at least one case (ActiveX in Html) where the Object is a 
	    		// CrossDomainContainer and NOT a GuiTestObject.
    			Log.info("RECP._getChildAtPoint new childobj is a "+ childobj.getClass().getSimpleName());
	    		curobj = childobj;
	    		if(curobj instanceof GuiTestObject){
	    			childobj = ((GuiTestObject)curobj).getChildAtPoint(pt);
	    		}else if (curobj instanceof CrossDomainContainer){
	    			children = ((CrossDomainContainer)curobj).getChildren();
	    			Log.info("CrossDomainContainer encountered with "+ children.length +" children.");
	    			return null;
	    		}else{
	    			return null;
	    		}
	    	} while(childobj != null);
	    	
	    	if (curobj instanceof GuiTestObject) return (GuiTestObject)curobj;
	    	Log.debug("Child class '"+ curobj.getClass().getSimpleName()+"' may not support getChildAtPoint.");
	    	return null;
	    	
    	}catch(Exception x){
	    	Log.debug(x.getClass().getSimpleName()+":"+x.getMessage()+" prevents getChildAtPoint for "+curobj, x);
    	}
    	return null;
 	}
 	
 	/**
 	 * Find the component TestObject, and get its rectangle on screen and save the rectangle 
 	 * in form of "x, y, width, height" in the StatusInfo of the TestRecord. 
 	 * 
 	 * params[0] = component key
 	 * 
 	 */
 	private void _getComponentRectangle(){
 		String debugmsg = getClass().getName()+"._getComponentRectangle(): ";
 		Log.info(debugmsg+getEngineCommand()+" processing...");
 		if(! validateParamSize(1)){
 			Log.debug(debugmsg+" parameter size is not 1. It should be componet's TestObject cache key.");
 			return;
 		}
 		Iterator iter = params.iterator();
 		Object componentKey = iter.next();
 		
 		Log.info(debugmsg+" Searching cached TestObject for componentKey: "+componentKey);
 		RGuiObjectVector rgov = (RGuiObjectVector) this.getGuiObjectVector();
 		
 		GuiTestObject component = (GuiTestObject) rgov.getCachedItem(componentKey);
 		
	    if (component == null){
	    	Log.info(debugmsg + " cached TestObject is not found by key:" + componentKey);
			testRecordData.setStatusInfo("");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    }else{
	    	Log.info(debugmsg + " cached TestObject is found by key:" + componentKey);
	    	Rectangle rect = component.getClippedScreenRectangle();
	    	if(rect==null){
	    		Log.debug("The rectangle is null for component "+component.getObjectClassName());
	    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    		return;
	    	}
	    	String rectString = rect.x+","+rect.y+","+rect.width+","+rect.height;
	    	Log.info(debugmsg + " TestObject's location is: " + rect+" ; pass to server side: "+rectString);
			testRecordData.setStatusInfo(rectString);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	    }
 	}
 	
 	/**
 	 * Return the Windows native handle of the window containing the component
 	 * 
 	 * params[0] = component key
 	 */
 	private void _getTopWindowHandle(){
 		String debugmsg = getClass().getName()+"._getTopWindowHandle(): ";
 		Log.info(debugmsg+getEngineCommand()+" processing...");
 		if(! validateParamSize(1)){
 			Log.debug(debugmsg+" parameter size is not 1. componentKey");
 			return;
 		}
 		Iterator iter = params.iterator();
 		String key = (String) iter.next();
 		
 		Log.info(debugmsg+" key: "+key);
 		RGuiObjectVector rgov = (RGuiObjectVector) this.getGuiObjectVector();
 		TestObject component = (TestObject) rgov.getCachedItem(key);
 		
	    if (component == null){
	    	Log.info(debugmsg + " cached TestObject is not found by key:" + key);
			testRecordData.setStatusInfo("0");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    }else{
	    	TestObject topParent = component.getTopParent();
	    	if(topParent instanceof ITopWindow){
	    		ITopWindow window = (ITopWindow) topParent;
	    		window.activate();
	    		IWindow nativeWindow = Script.getScreen().getActiveWindow();
	    		if(nativeWindow!=null){
		    		long hWnd = nativeWindow.getHandle();
		    		Log.info(debugmsg+" window's handle is "+hWnd);
		    		testRecordData.setStatusInfo(String.valueOf(hWnd));
		    		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		    		return;
	    		}
	    	}
	    	
		    Log.info(debugmsg + " can not get IWindow Object.");
			testRecordData.setStatusInfo("0");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    }
 	}
 	
	/**
	 * Return all matching keys in cache. Three parts in the whole process.
	 * 1. get the cached object that 'parent key' is attached to in RJ-cache, Hashtable <key, cached object>
	 * 2. recursively call GuiTestObject.getChildAtPoint(x,y) until find final child object on GUI at (x,y). Lei Wang gave this cool idea!
	 * 3. reverse look up Hashtable <key, cached object> by final child object found in step #2, get all matching keys.
	 * 
	 * A cached object may own more than one different keys. 
	 * 
	 * params[0] = parent key
	 * params[1] = x coordination on screen 
	 * params[2] = y coordination on screen
	 * @see GuiObjectVector.cache
	 */
	private void _getMatchingChildKeysAtPoint(){
		String debugMsg = getClass().getSimpleName()+"._getMatchingChildObjectKeysAtPoint(): ";
		Log.info("RECP."+ getEngineCommand() +" processing...");
		if(! validateParamSize(3)) return;
	    Iterator iterator = params.iterator();
	    String parentKey = (String) iterator.next();     // key to the parent Window in cache
	    String strX = (String) iterator.next(); 		 // x-coordination
	    String strY = (String) iterator.next();	         // y-coordination

	    int x = 0;
	    int y = 0;
	    try {
	    	x = Integer.parseInt(strX);
	    	y = Integer.parseInt(strY);
	    }catch(NumberFormatException nfe){
	    	Log.debug(debugMsg + nfe.toString());
	    }
		Log.info("...parentKey:"+ parentKey +" at point [" + x + " , " + y + "]");
		
	    RGuiObjectVector rgov = (RGuiObjectVector)getGuiObjectVector();
	    
	    // get parent test object in cache
	    GuiTestObject parentObj = (GuiTestObject)rgov.getCachedItem(parentKey);

	    if (parentObj == null){
	    	Log.info(debugMsg + " cached parentObj not found by key:" + parentKey);
			testRecordData.setStatusInfo("");
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			return;
	    }
	    // parentObj is NOT null	
	    ArrayList keys = new ArrayList();
    	GuiTestObject curobj = _getChildAtPoint(parentObj, new Point(x,y));
    	if ((curobj instanceof GuiTestObject) && (curobj!=parentObj)){
			try {
				keys = rgov.getCachedKeysByValue(curobj);
			}catch(Exception e){
				Log.debug(debugMsg + " getCachedKeyByValue throws" + e);
			}
			Log.info("found GUI class:" + curobj.getObjectClassName());
		}else{
	    	Log.debug(debugMsg +" cached object can't be found by parentKey");
		}

	    String[] stringArray = new String[keys.size()];
	    String[] keyArray = (String [])keys.subList(0, keys.size()).toArray(stringArray);
	    
	    Log.info("number of found keys: " + keys.size());
		testRecordData.setStatusInfo(convertToDelimitedString(keyArray));
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
	
	// transparentDialog will be resized and located on the component that needs to be highlighted. With a TransComponent inside, it looks like transparent.
	private HighLightDialog transparentDialog = null;
 	
	private boolean highLightTestObject(GuiTestObject winObj, GuiTestObject obj, String rstring) {
 		String debugMsg = getClass().getName()+".highLightTestObject(TestObject): ";
		// do the work to highlight the matched object
		if(obj != null) {
	    	Log.info(debugMsg+"object found for " + rstring);
	    	Log.info(debugMsg + "...start highlighting the matched component");
	    	TestObject topObj = null;
	    	Log.info(debugMsg + "...bring its top window to front");
	    	if(winObj!=null){
				if(RGuiObjectVector.isFlexDomain(winObj)){
					try {
						((FlexApplicationTestObject)winObj).click(); // for bringing it to front, it works when casting to FlexApplicationTestObject 
					} catch (ClassCastException cex){
						((FlexObjectTestObject)winObj).click();  // in non runtime loading, the top window is not a FlexApplicationTestObject. 
					}
				}else{
					try {
						((ITopWindow)winObj).activate();
					} catch (ClassCastException cex){
						winObj.click();
					}
				}
	    	}else{
				if(RGuiObjectVector.isFlexDomain(obj)){
					topObj = FlexUtil.drillDownRealFlexObj(obj.getTopParent());
					try {
						((FlexApplicationTestObject)topObj).click(); // for bringing it to front
					} catch (ClassCastException cex){
						((FlexObjectTestObject)topObj).click(); 
					}
				}else{
					topObj = obj.getTopParent();
					try {
						((ITopWindow)topObj).activate();
					} catch (ClassCastException cex){
						GuiTestObject gobj = new GuiTestObject(topObj);
						gobj.click();
					}
				}
	    	}
			
			try {
				Thread.sleep(500);  // waiting for a while until the top most window has been bought to front already
			} catch (InterruptedException ie) {}			
			
			Log.info(debugMsg+" getting the size of the component to be highlighted");
			// get the size of the matched component
			java.awt.Rectangle rc = obj.getScreenRectangle();
			Log.info(debugMsg+" the size: x=" + rc.x + " y=" + rc.y + "width=" + rc.width + " height="+ rc.height);
			if(rc.height==0 || rc.width==0){
				Log.info(debugMsg+" The component's size is zero, can not draw retangle around it.");
				log.logMessage(testRecordData.getFac(),debugMsg + "NO object found for " + rstring,FAILED_MESSAGE);
				return false;
			}
			
			if (this.transparentDialog == null) {
			   	this.transparentDialog = new HighLightDialog();
			}
			this.transparentDialog.setRString(rstring);
			// update the area that the matched component covers
			this.transparentDialog.updateBackground(rc);
			// change dialog's size, make it as same as the size of the matched component
			this.transparentDialog.setBounds(rc);
			this.transparentDialog.repaint();
			this.transparentDialog.setVisible(true);
			transparentDialog.toFront();
			log.logMessage(testRecordData.getFac(),debugMsg + "found and highlighted the object: "+ obj.getClass().getName()+":"+rstring, GENERIC_MESSAGE);
			return true;
		}else {
	    	Log.info(debugMsg+"NO object found for " + rstring);
			log.logMessage(testRecordData.getFac(),debugMsg + "NO object found for " + rstring,FAILED_MESSAGE);
			return false;
		}
 	}
	
	/* highLightDialog is a JDialog with no caption, with a 'Transparent' component inside.
	 * It is designed to be a message box right over the component matching the R-String, highlighting this component.
	 * At the same time the R-String will be shown as a Tip text when moving mouse over this dailog.
	 * */
	private class HighLightDialog extends JDialog{
		private TransComponent bg = null;
		HighLightDialog(){
			super();
			bg = new TransComponent();
			bg.setLayout(new BorderLayout());
			this.getContentPane().add("Center",bg);
			this.setUndecorated(true);
			this.pack();
		}
		public void updateBackground(Rectangle rc){
			bg.updateBackground(rc);
		}
		public void setRString(String rs){
			bg.setToolTipText(rs);
		}
		/**
		 * TransComponent derived from JComponent, is able to capture the screen image behind the component 
		 * and copy the image with border highlighted to this component. So the component looks like transparent, and border highlighted.
		 */
		private class TransComponent extends JComponent { 
			private Image background = null;
			public TransComponent() {
				super();
			}
			public void updateBackground(Rectangle rect) {
				try {
					Robot robot = new Robot( );
					background = robot.createScreenCapture(rect); 
				} catch (Exception ex) {
					Log.debug(getClass().getName()+".updateBackground(): " + ex.toString());
				}
			}
			
			/*
			 * Draw the screen area capured on the component and draw its border in red, 
			 * The component looks like transparent. 
			 * This method will be called automatically if the component's size changes 
			 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
			 */
			public void paintComponent(Graphics g) {
				try {
					g.drawImage(background,0,0,null);	    
					// 	make the image highlighted by drawing its border in red
					g.setColor(Color.red);
					int imageWidth  = this.getWidth()-1;
					int imageHeight = this.getHeight()-1; 
					int [] xpoints = {0, imageWidth, imageWidth,0, 0};
					int [] ypoints = {0, 0, imageHeight,imageHeight, 0};
					g.drawPolyline(xpoints, ypoints, 5);
					g.drawLine(0, 0, imageWidth, 0);
				} catch(Exception ex) {
					Log.debug(getClass().getName()+".paintComponent(): " + ex.toString());
				}
			}
		}
	} // end of Class HighLightDialog
	
	//implemented for RFT engine
	protected String recognitionOfObjectAtPoint(int x, int y){
		
		RGuiObjectVector rgov = (RGuiObjectVector)getGuiObjectVector();
		Script script = rgov.getScript();
		RootTestObject root = script.getRootTestObject();
		TestObject obj = root.objectAtPoint(new Point(x, y));
		TestObject topobj = null;
		
		Log.info("RECP.recognitionOfObjectAtPoint() processing...");
        if (obj == null) {
        	Log.info("...no GUI return by RootTestObject.objectAtPoint (" + x + "," + y + ")");
        	return null;
        }
		
        String domain = (String)obj.getDomain().getName();
        
        //enable the domain
        Domains.enableDomain(domain);
        
        GuiObjectRecognition rgor = new RGuiObjectRecognition("", script, 1);

        String rstring="";
        String className;
        String mappedClassType;
        String typeInfo;
        String idInfo;
        GuiClassData classdata = new RGuiClassData();
        
		while(obj!= null) {
			
			//1
			className = rgor.getObjectClassName(obj);
			
			//2 
			mappedClassType = classdata.getMappedClassType(className, obj);
			
			if (mappedClassType == null) {
				typeInfo = "Class="+className+";";
			} else {
				StringTokenizer typeTokens = new StringTokenizer(mappedClassType, ", ");
				typeInfo = typeTokens.nextToken(); // get the first token
				typeInfo = "Type=" + typeInfo.trim();
			}
			
			//get the qualifier part 
			idInfo = genIdInformation(obj, rgor);
			
			if (rstring == "")
				rstring = typeInfo + GuiObjectRecognition.DEFAULT_QUALIFIER_SEPARATOR + idInfo;
			else
				rstring = typeInfo + GuiObjectRecognition.DEFAULT_QUALIFIER_SEPARATOR  + idInfo + GuiObjectVector.DEFAULT_CHILD_SEPARATOR + rstring;
			
			
			topobj = obj;
			obj = obj.getMappableParent();
		}
		
		rstring = ":FPSM::MCSM:domain=" + domain + ";" + rstring;
		
		Log.info("...get a recognition string: " + rstring);
		
		//verify if the rstring works
		if (verifyRecogniton(topobj, rstring)) {
			Log.info("...rstring verified!");
			return rstring;
		}	
		else {
			Log.info("...rstring NOT verified!");
			return null;
		}	
	}
	
	private boolean verifyRecogniton(TestObject topobj, String rstring) {

		Object childobj = null;
		RGuiObjectVector rgov = (RGuiObjectVector)getGuiObjectVector();
		rgov.setPathVector(rstring);
		rgov.initGuiObjectRecognition();
		
		if (topobj == null) {
			topobj = (TestObject)rgov.getMatchingParentObject();
		}else{
			childobj = rgov.getMatchingChildObject(topobj, null);
			if (childobj != null)
				Log.debug("verified and found with r-string:" + rstring);
		}
		return (childobj != null);
	}
	
	protected String genIdInformation(TestObject obj, GuiObjectRecognition gor) {
		String rtlString = null;

		//try qualifier "Name="
		String name = null;
		name = gor.getObjectName(obj);
		if (name!=null && name.length()>0) {
			rtlString = "Name=" + name;
		} else {		
			//try qualifier "ID="
			String ID = null;
			ID = gor.getObjectID(obj);
			if (ID!=null && ID.length()>0) {
				rtlString = "ID=" + ID;
			} else {
				// try qualifier "Text="
				String textvalue = null;
				textvalue = gor.getObjectText(obj);
				if (textvalue!=null && textvalue.length()>0) {
					rtlString = "Text=" + textvalue;
				} else {
					// using qualifier "Property=", find out the recognition property with the heaviest weight 
					StringBuffer key = new StringBuffer();
					StringBuffer value = new StringBuffer();
					if (getRecogPropertyWithTopWeight(obj, key, value)) {
						rtlString = "Property=" + key.toString() + ":" + value.toString();
					} else {
						Log.debug("...no recognition string auto produced for this token!");
					}
				}
			} 
		}	
		return rtlString;
	}
	
	/*
	 * obj: TestObject from which to get the heaviest recognition property
	 * rtlkey: [out] the key of the heaviest recognition property
	 * rtlval: [out] the value of the heaviest recognition property 
	 * 
	 * Return: true: the property exist  false: no property found
	 */
	protected boolean getRecogPropertyWithTopWeight(TestObject obj, StringBuffer rtlkey, StringBuffer rtlvalue) {
		boolean isExit = false;
		int topweight = 0;
		
		Hashtable  rsProps = obj.getRecognitionProperties();
		Enumeration e = rsProps.keys();
		while(e.hasMoreElements()) {
			Object key =  e.nextElement();
			Object value = rsProps.get(key);
			int weight = obj.getRecognitionPropertyWeight((String)key);
				
			if (weight > topweight && value.toString().trim().length() > 0) { // value may be blank
				topweight = weight;
				
				rtlkey.setLength(0);
				rtlkey.append(key.toString());
				rtlvalue.setLength(0);
				rtlvalue.append(value.toString());

				isExit = true;
			}
		}
		return isExit;
	}

} // end of Class REngineCommandProcessor



