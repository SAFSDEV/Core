/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.tools.input.RobotKeyEvent;

public class CFEditBox extends CFComponent {
	//	EditBoxFunctions Actions
	public static final String SETTEXTCHARACTERS			= "SetTextCharacters";
	public static final String SETTEXTVALUE					= "SetTextValue";
	public static final String SETUNVERIFIEDTEXTCHARACTERS	= "SetUnverifiedTextCharacters";
	public static final String SETUNVERIFIEDTEXTVALUE		= "SetUnverifiedTextValue";
	
	public CFEditBox() {
		super();
	}
	
	protected void localProcess(){
		String debugmsg = this.getClass().getName()+".localProcess(): ";

		if (action != null) {
			String keys = "";
			boolean verify = false;
			boolean isCharacter = false;
			SGuiObject _comp = sHelper.getCompTestObject();
			
			Log.debug(debugmsg+" Processing keyword: "+action);
			try {
				keys = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(4));
			}
			// Generic commands like CLICK will not have this 4th(5th)  field
			catch (Exception e) {	}
			Log.debug(debugmsg+" input keys='"+keys+"'");
			
			if(action.equalsIgnoreCase(SETTEXTCHARACTERS) ||
			   action.equalsIgnoreCase(SETUNVERIFIEDTEXTCHARACTERS) ||
			   action.equalsIgnoreCase(SETTEXTVALUE) ||
			   action.equalsIgnoreCase(SETUNVERIFIEDTEXTVALUE)){
				
				verify = action.equalsIgnoreCase(SETTEXTCHARACTERS) ||
                        (action.equalsIgnoreCase(SETTEXTVALUE) && !StringUtils.containsSepcialKeys(keys));
				isCharacter = action.equalsIgnoreCase(SETTEXTCHARACTERS) ||
				              action.equalsIgnoreCase(SETUNVERIFIEDTEXTCHARACTERS);
				
				//Scroll to the component and set focus to it.
				scrollToAndClickComponent(_comp);
				
				clearEditBox(_comp);
				inputEditBox(_comp, keys, isCharacter);
				
				if(verify){
					if(verifyEditBox(_comp, keys)){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);					
					}else{
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					}
				}else{
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				}				
			}else{
				Log.debug(debugmsg+" keyword '"+action+"' will be processed in superclass "+this.getClass().getSuperclass().getName());
			}
			
			if(testRecordData.getStatusCode() == StatusCodes.NO_SCRIPT_FAILURE){
				String msg = genericText.convert("success3a", windowName +":"+ compName + " "+ action +" successful using '"+ keys +"'",
						windowName, compName, action, keys);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				// just in case. (normally any failure should have issued an Exception)
			}
		}else{
			Log.error(debugmsg+" keyword is null.");
		}
	}
	
	/**
	 * <em>Purpose:</em> Remove the content from EditBox.<br>
	 * @param _comp
	 */
	protected void clearEditBox(SGuiObject _comp){
		String debugmsg = getClass().getName()+".clearEditBox(): ";
		
		try{
			selenium.type(_comp.getLocator(), "");
		}catch(Exception e){
			Log.debug(debugmsg+" Exception occur. "+e.getMessage());
			selenium.click(_comp.getLocator());
			RobotKeyEvent.doKeystrokes(keysparser.parseInput("^a{ExtDelete}"), robot, 0);
			//RobotKeyEvent.doKeystrokes(keysparser.parseInput("{ExtHome}+{ExtEnd}{ExtDelete}"), robot, 0);
		}
	}
	
	/**
	 * <em>Purpose:</em> Set the text as the content of EditBox<br>
	 * @param _comp          The EditBox
	 * @param text           The text to be set to EditBox
	 * @param isCharacter    If it is true, text will be considered as normal characters;<br>
	 *                       otherwise, text will be parsed as a string containing special keys.<br>
	 *                       Special keys example: + --> ShiftKey  ^ --> CtrlKey. For other special keys,<br>
	 *                       refer to com.rational.test.ft.object.interfaces.ITopWindow.inputKeys()<br>
	 */
	protected void inputEditBox(SGuiObject _comp,String text, boolean isCharacter){
		String debugmsg = getClass().getName()+".inputEditBox(): ";
		
		if(isCharacter){
			try{
				selenium.type(_comp.getLocator(), text);
			}catch(Exception e){
				Log.debug(debugmsg+" Exception occur. "+e.getMessage());
				//Try the IBT engine to input characters
				RobotKeyEvent.doKeystrokes(keysparser.parseChars(text), robot, 0);
			}
		}else{
			//selenium typeKeys() will not work as RFT, 
			//it can't translate the special chars like + ^ to ShiftKey, CtrlKey, etc.
			//selenium.typeKeys(_comp.getLocator(), text);

			//So we use the IBT engine to do this work.
			selenium.focus(_comp.getLocator());
			RobotKeyEvent.doKeystrokes(keysparser.parseInput(text), robot, 0);
		}
	}
	

	/**
	 * <em>Purpose:</em> Compare the content of EditBox to the original keys,<br>
	 *                   If they are same, return true; Otherwise, return false.
	 * @param _comp The EditBox whose content will be verified.
	 * @param keys  The string to be compared to during verification.
	 * @return		If verification is passed, return true; otherwise, return false;
	 */
	protected boolean verifyEditBox(SGuiObject _comp, String keys){
		String debugmsg = getClass().getName()+".verifyEditBox(): ";
		boolean passVerification = false;
		String contents = selenium.getValue(_comp.getLocator());
		
		Log.debug(debugmsg+" keys='"+keys);
		Log.debug(debugmsg+" retrieved from editbox, contents='"+contents);

		if(contents.equals(keys)){
			passVerification = true;					
		}else{
			passVerification = false;
			String failure = genericText.convert("not_equal",
					"'"+ contents +"' does not equal '"+ keys +"'",
					contents, keys);
			standardFailureMessage(action, failure);
		}
		
		return passVerification;
	}
}
