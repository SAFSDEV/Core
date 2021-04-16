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

import java.awt.Point;
import java.util.Iterator;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.text.FAILStrings;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> CFToolBar, process a CFToolBar component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Lei Wang
 * @since   OCT 30, 2008
 *
 *   <br>   OCT 30, 2008    (Lei Wang) Original Release
 *   <br>	DEC 01, 2008	(Lei Wang) Add method getObjectTooltip();
 *   								  Modify method clickButtonTooltip(): If RFT API toolbar.click(Script.atToolTipText(tooltip)) 
 *   								  can not work, then we will find the object ourself and perform a click on it. See defect S0539954
 *   <br>	DEC 08, 2008	(Lei Wang) Add keywords ClickUnverifiedButtonText, ClickButtonText.
 *   								  Add methods getObjectText(), getObjectTooltip(), performClick(), clickButtonText() 
 *   								  isOnSelectedState(), getMatchedTestObjectFromChildren(), getFirstMatchingPathTestObject()
 *   								  to implement new added keywords.
 *   								  Modify method clickButtonTooltip(): call performClick() to click item.
 *   <br>	DEC	12, 2008	(Lei Wang) Modify method performClick(): add a new parameter point to it.
 *   								  Modify method clickButtonText(): If the third parameter point exists, then try to click at this point.
 *   <br>	JAN 07, 2009	(Lei Wang) Modify method clickButtonIndex(): If the parameter point exists, then try to click at this point. See defect S0551177.
 **/
public class CFToolBar extends CFComponent {
	public static final String CLICKBUTTONINDEX	 				= "ClickButtonIndex";
	public static final String CLICKBUTTONTOOLTIP 				= "ClickButtonTooltip";
	public static final String CLICKBUTTONTEXT					= "ClickButtonText";
	public static final String CLICUNVERIFIEDKBUTTONTEXT 		= "ClickUnverifiedButtonText";
 
	
	public static final String TYPETEXT					 		= "Text";
	public static final String TYPETOOLTIP				 		= "Tooltip";
	public static final String PATH_SEPARATOR 					= "->";
	
	/** <br><em>Purpose:</em> constructor, calls super
	 **/
	public CFToolBar() {
		super();
	}

	/** <br><em>Purpose:</em> process: process the testRecordData
	 ** <br>This is our specific version. We subclass the generic CFComponent.
	 ** The actions handled here are:
	 ** <br><ul>
	 ** <li>ClickButtonIndex
	 ** <li>ClickButtonTooltip
	 ** </ul><br>
	 * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
	 * based on the result of the processing
	 * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
	 * <br><em>Assumptions:</em>  none
	 **/
	protected void localProcess() {
		try {
			String debugmsg = getClass().getName()+".localProcess() ";
			Log.info(debugmsg+ " searching specific tests...");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);

			if (action != null) {
				Log.info(debugmsg + " ACTION: "
						+ action + "; win: " + windowName + "; comp: "
						+ compName);
				if(action.equalsIgnoreCase(CLICKBUTTONINDEX) ||
				   action.equalsIgnoreCase(CLICKBUTTONTOOLTIP) ||
				   action.equalsIgnoreCase(CLICKBUTTONTEXT) ||
				   action.equalsIgnoreCase(CLICUNVERIFIEDKBUTTONTEXT)){
					performActionWithOneParameter();	
				}else{
					//Action can not be found here
					//We must set this status to StatusCodes.SCRIPT_NOT_EXECUTED 
					//so that CFComponent will treate this command
					Log.debug(debugmsg+" action "+action+" can not be performed here.");
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
				}
			}
		} catch (com.rational.test.ft.ObjectNotFoundException e) {
			componentFailureMessage(e.getMessage());
		} catch (SAFSException e) {
			componentFailureMessage(e.getMessage());
		} catch (com.rational.test.ft.SubitemNotFoundException e) {
			componentFailureMessage(e.getMessage());
		}
	}

	/**
	 * Purpose: 	Perform ClickAtIndex ClickAtTooltip
	 * @throws 	SAFSException
	 */
	protected void performActionWithOneParameter() throws SAFSException {
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String errorMsg = null;
			String val = (String) params.iterator().next();
			Log.info("..... val: " + val);

			if (action.equalsIgnoreCase(CLICKBUTTONINDEX)) {
				errorMsg = clickButtonIndex(val);
			}else if(action.equalsIgnoreCase(CLICKBUTTONTOOLTIP)) {
				errorMsg = clickButtonTooltip(val);
			}else if(action.equalsIgnoreCase(CLICUNVERIFIEDKBUTTONTEXT)){
				errorMsg = clickButtonText(val,false);
			}else if(action.equalsIgnoreCase(CLICKBUTTONTEXT)){
				errorMsg = clickButtonText(val,true);
			}
			
			if (errorMsg == null) {
				testRecordData.setStatusCode(StatusCodes.OK);
				componentSuccessMessage("");
			} else {
				componentExecutedFailureMessage(errorMsg);
			}
		}
	}
	
	protected String clickButtonIndex(String indexString) throws SAFSException{
		TestObject toolbarObject = obj1;
		String debugmsg = getClass().getName()+".clickButtonIndex() ";
		String errormsg = null;
		int index = 0;
		try{
			index = Integer.parseInt(indexString);
		}catch(NumberFormatException e){
			Log.debug(debugmsg+" Can not convert "+indexString+" to Integer. "+e.getMessage());
			errormsg = FAILStrings.convert(FAILStrings.BAD_PARAM, 
										   "Invalid parameter value for "+indexString,
										   indexString);
			return errormsg;
		}
		
    	Iterator iter = params.iterator();
    	//Skip the first parameter
    	iter.next();
    	//Get the next parameter (coordination) if exists
    	Point point = null;
    	if(iter.hasNext()){
    		point = checkForCoord((String)iter.next());
    	}
    	Log.debug(debugmsg+" coordination is "+point);
    	
		//It seems that atIndex() will not count toolbar_separator, it's 0-based.
		//Button1	|	Button2		|	Button3
		//  0				1				2
		//If the index is bigger than the number of children of toolbar,
		//the last button will be selected.
		//atIndex(4) will click on the Button3
		int childrenNumber = toolbarObject.getChildren().length;
		Log.debug(debugmsg+" index="+index);
		if(index<1 || index>childrenNumber){
			Log.debug(debugmsg+" index="+index+" is not in ("+1+","+childrenNumber+")");
			errormsg = FAILStrings.convert(FAILStrings.BAD_PARAM, 
										   "Invalid parameter value for "+indexString,
										   indexString);
			return errormsg;
		}
		
		if(toolbarObject instanceof GuiSubitemTestObject){
			Log.debug(debugmsg+" clicking at index "+index);
			GuiSubitemTestObject toolbar = new GuiSubitemTestObject(toolbarObject.getObjectReference());
			if(point!=null){
				toolbar.click(Script.atIndex(index-1),point);
			}else{
				toolbar.click(Script.atIndex(index-1));
			}
		}else{
			errormsg = " This toolbar is "+toolbarObject.getObjectClassName()
						 + ", not instance of GuiSubitemTestObject. Needs new implementation to perform it.";
			Log.debug(debugmsg+errormsg);
		}
		
		return errormsg;
	}
	
	protected String clickButtonTooltip(String tooltip) throws SAFSException{
		TestObject toolbarObject = obj1;
		String debugmsg = getClass().getName()+".clickButtonTooltip() ";
		String errormsg = null;
		boolean caseInsensitive = false;//maybe this will be get from the optional parameter in furture

		try{
			if (toolbarObject instanceof GuiSubitemTestObject) {
				Log.debug(debugmsg + " clicking at button whose tooltip is "+ tooltip);
				GuiSubitemTestObject toolbar = new GuiSubitemTestObject(toolbarObject.getObjectReference());
				toolbar.click(Script.atToolTipText(tooltip));
			} else {
				errormsg = " This toolbar is "
						+ toolbarObject.getObjectClassName()
						+ ", not instance of GuiSubitemTestObject. Needs new implementation to perform it.";
				Log.debug(debugmsg + errormsg);
			}
		}catch(Exception e){
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			Log.debug(debugmsg+" Looking for a child whose tooltip is "+tooltip+" from "+toolbarObject.getObjectClassName());
			
			GuiTestObject tooltipMatchedChild = null;
			tooltipMatchedChild = (GuiTestObject) getMatchedTestObjectFromChildren(toolbarObject, caseInsensitive, tooltip, TYPETOOLTIP);
			
			if(tooltipMatchedChild!=null){
				Log.debug(debugmsg+" got object "+tooltipMatchedChild.getObjectClassName());
				if(performClick(tooltipMatchedChild,null)){
					Log.debug(debugmsg+" Perform click ok.");
				}else{
					errormsg = " Can not perform click";
				}
			}else{
				errormsg = " Can not get a matched object whose tooltip is "+tooltip;
			}
		}
		
		return errormsg;
	}
	
	/**
	 * If textOrPath is a single, click on the item whose text match to the parameter in this toolbar.
	 * Else if textOrPath is a string with "->", click on the subitem whose path math the parameter in this toolbar.
	 * @param textOrPath		The text can be a single string or a string containning "->"
	 * @param toVerify			If true, verify the item's state is checked.
	 * @return
	 * @throws SAFSException
	 */
    protected String clickButtonText(String textOrPath, boolean toVerify) throws SAFSException{
    	TestObject toolbarObject = obj1;
    	String debugmsg = getClass().getName()+".clickButtonText() ";
    	String errormsg = null;
    	

    	Iterator iter = params.iterator();
    	//Skip the first parameter
    	iter.next();
    	//Get the second parameter (caseInsensitive) if exists
    	boolean caseInsensitive = false;
    	if(iter.hasNext()){
    		String insensitive = (String) iter.next();
    		if(insensitive.equalsIgnoreCase("1") ||
    		   insensitive.equalsIgnoreCase("CaseInsensitive") ||
    		   insensitive.equalsIgnoreCase("Case-Insensitive")){
    			caseInsensitive = true;
    		}
    	}
    	//Get the third parameter (coordination) if exists: this paramter only work for atText() not for atPath()
    	Point point = null;
    	if(iter.hasNext()){
    		point = checkForCoord((String)iter.next());
    	}
    	Log.debug(debugmsg+" coordination is "+point);
    	
    	//Try RFT API click(Script.atText(text)) or click(Script.atPath(text)) firstly, the match should be case sensitive. 
    	boolean clickOk = false;
    	boolean isPath = (textOrPath.indexOf(PATH_SEPARATOR)>-1);
    	try{
    		if(!caseInsensitive){
				if (toolbarObject instanceof GuiSubitemTestObject) {
					Log.debug(debugmsg + " clicking at button whose text is "+ textOrPath);
					GuiSubitemTestObject toolbar = new GuiSubitemTestObject(toolbarObject.getObjectReference());
					if(isPath){
						toolbar.click(Script.atPath(textOrPath));
					}else{
						if(point!=null){
							toolbar.click(Script.atText(textOrPath),point);
						}else{
							toolbar.click(Script.atText(textOrPath));
						}
					}
					clickOk = true;
				} else {
					String msg = " This toolbar is "
							+ toolbarObject.getObjectClassName()
							+ ", not instance of GuiSubitemTestObject. Needs new implementation to perform it.";
					Log.debug(debugmsg + msg);
				}
    		}
		}catch(Exception e){
			Log.debug(debugmsg+" Exception: "+e.getMessage());
			Log.debug(debugmsg+" Looking for a child whose text is "+textOrPath+" from "+toolbarObject.getObjectClassName());
		}
    	
    	//If RFT API click(Script.atText(text)) can not work,
		//Then try to find the testObject whose text is the same as parameter text, and click on it.
		GuiTestObject textMatchedChild = null;
		if(isPath){
			textMatchedChild = (GuiTestObject) getFirstMatchingPathTestObject(toolbarObject, caseInsensitive, textOrPath, PATH_SEPARATOR);
		}else{
			textMatchedChild = (GuiTestObject) getMatchedTestObjectFromChildren(toolbarObject, caseInsensitive, textOrPath, TYPETEXT);
		}
		if(!clickOk){			
			if(textMatchedChild!=null){
				Log.debug(debugmsg+" got object "+textMatchedChild.getObjectClassName());
				clickOk = performClick(textMatchedChild,point);
			}else{
				Log.debug(debugmsg+ " Can not get a matched object whose text is "+textOrPath);
			}
		}
    	
		//If the click action is performed successfully, try to verify the state.
		if(clickOk){
			Log.debug(debugmsg+" Perform click ok.");
	    	if(toVerify){
	    		//Verify the button state is selected:
	    		if(!isOnSelectedState(textMatchedChild)){
	    			errormsg = " Verification failed: The state is not 'selected'.";
	    		}
	    	}
		}else{
			errormsg = " Can not perform click.";
		}

    	return errormsg;
    }
    
    /**
     * Verify if the item's state is selected.
     * The item's type can be one of javax.swing.AbstractButton, System.Windows.Forms.ToolStripItem, java.awt.Checkbox
     * @param guiTestObject
     * @return
     */
    protected boolean isOnSelectedState(GuiTestObject guiTestObject){
    	String debugmsg = getClass().getName()+".isOnSelectedState() ";
    	boolean selected = false;
    	
    	try{
    		//Try javax.swing.AbstractButton
    		selected = (Boolean) guiTestObject.invoke("isSelected");
    	}catch(Exception e){
    		Log.debug(debugmsg+" Exception: "+e.getMessage());
    		try{
    			//System.Windows.Forms.ToolStripItem
    			selected = (Boolean) guiTestObject.getProperty("Selected");
    		}catch(Exception e1){
    			Log.debug(debugmsg+" Exception: "+e1.getMessage());
        		try{
        			//java.awt.Checkbox
        			selected = (Boolean) guiTestObject.invoke("getState");
        		}catch(Exception e2){
        			Log.debug(debugmsg+" Exception: "+e2.getMessage());
        		}
    		}
    	}
    	
    	return selected;
    }
    
    /**
     * Try to perform a click on the given GuiTestObject, if RFT API click() can not work then
     * try to invoke some native call to perform click.
     * @param testObject	A GuiTestObejct
     * @param point			The point inside the testObject at where the mouse should click.
     * @return
     * @throws SAFSException
     */
    protected boolean performClick(GuiTestObject testObject,Point point) throws SAFSException{
    	String debugmsg = getClass().getName()+".performClick() ";
    	boolean success = true;
    	
		Log.debug(debugmsg+" got object "+testObject.getObjectClassName());
		try{
			if(point!=null){
				testObject.click(point);
			}else{
				testObject.click();	
			}
		}catch(Exception e1){
			Log.debug(debugmsg+" RFT click does not work. "+e1.getMessage());
			Log.debug(debugmsg+" Try native click API");
			success = false;
			if(isDotnetDomain(testObject)){
				//We will treate only subclass of "System.Windows.Forms.ToolStripItem"
				if(DotNetUtil.isSubclassOf(DotNetUtil.getClazz(testObject),DotNetUtil.CLASS_TOOLSTRIPITEM_NAME)){
					try{
						//invoke the native method OnClick() to perform click,
						//but it seems that it can only trriger the action linked to click event of this item,
						//it will not trriger the mouse click on the item, that is to say you will not see the
						//mouse move to the object and click on it.
						testObject.invoke(DotNetUtil.METHOD_TOOLSTRIPITEM_ONCLICK,"(LSystem.EventArgs;)V",new Object[1]);
						success = true;
					}catch(Exception e){
						Log.debug(debugmsg+" Exception occured when invoking PerformClick() on "+testObject.getObjectClassName());
					}
				}else{
					Log.debug(debugmsg+" object is "+testObject.getObjectClassName()+". But we treate only subclass of 'System.Windows.Forms.ToolStripItem'");
				}
			}else{
				Log.debug(debugmsg+" object domain is "+testObject.getDomain().getName().toString()+". Need call its native click API");
			}
		}

		return success;
    }
    
    /**
     * Try to get the tooltip of the testObject. Now we treat two kind of object:
     * 1. Java swing object which extends from javax.swing.JComponent
     * 2. DotNet object which extnds from System.Windows.Forms.ToolStripItem or System.Windows.Forms.ToolBarButton
     * @param testObject
     * @return
     */
    protected String getObjectTooltip(TestObject testObject){
    	String tooltip = null;
    	String debugmsg = getClass().getName()+".getObjectTooltip() ";
    	
    	try{
    		//method JComponent.getToolTipText() works for java SWING object
    		tooltip = (String) testObject.invoke("getToolTipText");
    	}catch(Exception e){
    		Log.debug(debugmsg+" can not get tooltip by method getToolTipText() for object "+testObject.getObjectClassName()+". "+e.getMessage());
    	}
    	
    	if(tooltip==null){
    		//try to get the property ToolTipText,
    		//this works for .NET object System.Windows.Forms.ToolStripItem
    		try{
    			tooltip = (String) testObject.getProperty("ToolTipText");
    		}catch(Exception e){
    			Log.debug(debugmsg+" can not get tooltip by property ToolTipText for object "+testObject.getObjectClassName()+". "+e.getMessage());
    		}
    	}
    	
    	if(tooltip==null){
    		Log.debug(debugmsg+" need new implementation to get tooltip of "+testObject.getObjectClassName());
    	}
    	
    	return tooltip;
    }
    
    /**
     * Try to get the text of the testObject. Now we treat two kind of object:
     * 1. Java swing object which extends from javax.swing.AbstractButton
     * 2. DotNet object which extnds from System.Windows.Forms.ToolStripItem or System.Windows.Forms.ToolBarButton
     * @param testObject
     * @return
     */
    protected String getObjectText(TestObject testObject){
    	String text = null;
    	String debugmsg = getClass().getName()+".getObjectText() ";
    	
    	try{
    		//method JButton.getText() works for java SWING JButton object
    		text = (String) testObject.invoke("getText");
    	}catch(Exception e){
    		Log.debug(debugmsg+" can not get text by method getText() for object "+testObject.getObjectClassName()+". "+e.getMessage());
    	}
    	
    	if(text==null){
    		//try to get the property Text,
    		//this works for .NET object System.Windows.Forms.ToolStripItem
    		try{
    			text = (String) testObject.getProperty("Text");
    		}catch(Exception e){
    			Log.debug(debugmsg+" can not get text by property Text for object "+testObject.getObjectClassName()+". "+e.getMessage());
    		}
    	}
    	
    	if(text==null){
    		Log.debug(debugmsg+" need new implementation to get text of "+testObject.getObjectClassName());
    	}
    	
    	return text;
    }
    
    /**
     * @param toolbarObject			The toolbar contains every kinds of items.
     * @param caseInsensitive		True if match with case insensitive.
     * @param stringToMatch			Text or Path or Tooltip to be matched
     * @param matchType				It can be "Text" or "Tooltip", which are defined as constant TYPETEXT and TYPETOOLTIP
     * @return
     */
    protected TestObject getMatchedTestObjectFromChildren(TestObject toolbarObject,boolean caseInsensitive,String stringToMatch,String matchType){
		TestObject matchedChild = null;
		String debugmsg = getClass().getName()+".getMatchedTestObjectFromChildren() ";
		
		TestObject[] children = toolbarObject.getChildren();
		for(int i=0;i<children.length;i++){
			//the value can be text or tooltip
			String value = null;
			if(TYPETEXT.equals(matchType)){
				value = getObjectText(children[i]);
			}else if(TYPETOOLTIP.equals(matchType)){
				value = getObjectTooltip(children[i]);
			}else{
				Log.debug(debugmsg+" matchType "+matchType+" is not supported yet.");
				return null;
			}
			
			if(value!=null){
				if(caseInsensitive){
					if(value.equalsIgnoreCase(stringToMatch)){
						matchedChild = children[i];
						break;
					}
				}else{
					if(value.equals(stringToMatch)){
						matchedChild = children[i];
						break;
					}
				}
			}
		}
		
		return matchedChild;
    }
    
    /**
     * Get the first matched object from children of parameter testObject.
     * @param testObject		A tree object from which we search a mathced object.
     * @param caseInsensitive	If true, use String.equalsIgnoreCase() to match text.
     * @param path				A string like "a->b->c"
     * @param pathSeparator		A stirng like "->", which is used to separate node text.
     * @return
     */
	protected TestObject getFirstMatchingPathTestObject( TestObject testObject,boolean caseInsensitive, String path, String pathSeparator){
		String debugmsg = DotNetUtil.class.getName()+".getMatchingPathTestObject() ";
		TestObject matchedTestObject = null;
		
		if(testObject==null || path==null){
			Log.debug(debugmsg+"testObject or path is null. Can not get object matching path.");
			return null;
		}
		
		int separatorIndex = path.indexOf(pathSeparator);
		boolean isLastElementInPath = (separatorIndex==-1);
		String currentElement = "";
		String nextPath = "";
		if(!isLastElementInPath){
			currentElement = path.substring(0,separatorIndex);
			if(pathSeparator.length()+separatorIndex<path.length()){
				nextPath = path.substring(pathSeparator.length()+separatorIndex);
			}
		}else{
			currentElement = path;
		}
		
		TestObject[] children = testObject.getChildren();
		String childName = "";
		for(int i=0;i<children.length;i++){
			childName = getObjectText(children[i]);
			boolean matched = caseInsensitive? currentElement.equalsIgnoreCase(childName):currentElement.equals(childName) ;
			
			if(matched){
				if(isLastElementInPath){
					matchedTestObject = children[i];
					break;
				}else{
					matchedTestObject = getFirstMatchingPathTestObject( children[i],caseInsensitive, nextPath, pathSeparator);
					if(matchedTestObject!=null) break;
				}
			}
		}
		
		return matchedTestObject;
	}
}
