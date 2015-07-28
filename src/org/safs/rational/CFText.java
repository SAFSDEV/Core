/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.AWTException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.robot.Robot;
import org.safs.text.FAILStrings;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;

/**
 * <br><em>Purpose:</em> CFText, process a Text component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   Sep 08, 2003    (DBauman) Original Release
 *   <br>   Dec 10, 2003    (BNat) catch PropertyNotFoundException and look for property .value instead 
 * 	 <br>	Mar	24, 2008	(Lei Wang) Modify program structure, Add SetTextCharacters, SetUnverifiedTextCharacters, SetUnverifiedTextValue
 * 	 <br>	Jun 26, 2008 	(Lei Wang) Modify getText() to ensure keywords "SetTextCharacters" and "SetTextValue" work
 * 	 <br>	Jul 14, 2008 	(Lei Wang) Modify getText(): Use CFComponent.isXXXDomain(testobj) to distinguish the test domain
 * 														 For java domain: will not instance the specific class (this way can not work for custom
 * 														 class like com.sas.workspace.WATextField), just get the property "Text". If java class
 * 														 has method getText(), we can obtain "text" property.
 *	<br>	NOV 25, 2008	(LeiWang)	Modify method setText(): Call StringUtils.containsSepcialKeys() to test sepcial key. See defect S0546329.
 *	<br>	JAN	06, 2009	(LeiWang)	Add method setPropertyText(): set value to component's text.
 *										Modify method setText(): As Flex's top parent is FlexApplicationTestObject, it can not handle inputkeys
 *																 and inputchars, so use org.safs.robot.Robot to handle them.
 *  <br>	JAN 09, 2009	(LeiWang)	Modify method localProcess() and performAction(): If the keyword will not be processed here, should not
 *  																					  let performAction() to process, as it check the number
 *  																					  of parameter, this will prevent CFComponent from handling
 *  																					  some keywords without parameter, like Click. 
 **/
public class CFText extends CFComponent {

  public static final String SETTEXTCHARACTERS                   = "SetTextCharacters";
  public static final String SETTEXTVALUE                        = "SetTextValue";
  public static final String SETUNVERIFIEDTEXTCHARACTERS         = "SetUnverifiedTextCharacters";
  public static final String SETUNVERIFIEDTEXTVALUE              = "SetUnverifiedTextValue";   
        
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFText () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>SetTextCharacters
   ** <li>setTextValue
   ** <li>SetUnverifiedTextCharacters
   ** <li>SetUnverifiedTextValue
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      Log.info(getClass().getName()+".process, searching specific tests...");
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      
      if (action != null) {
      	Log.info("....."+getClass().getName()+".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if(action.equalsIgnoreCase(SETTEXTCHARACTERS) ||
           action.equalsIgnoreCase(SETTEXTVALUE) ||
           action.equalsIgnoreCase(SETUNVERIFIEDTEXTCHARACTERS) ||
           action.equalsIgnoreCase(SETUNVERIFIEDTEXTVALUE)){
        	performAction();
        }else{
        	//Action can not be found here
        	//We must set this status to StatusCodes.SCRIPT_NOT_EXECUTED so that CFComponent will treate this command
        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	return;
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
   * Purpose: 	Perform SetTextCharacters,SetTextValue,SetUnverifiedTextCharacters,SetUnverifiedTextValue
   * @throws 	SAFSException
   */
  private void performAction() throws SAFSException{
    if (params.size() < 1) {
        paramsFailedMsg(windowName, compName);
    } else {
        String errorMsg = null;
    	String val = (String) params.iterator().next();
        Log.info("..... val: "+val);
        
        if(action.equalsIgnoreCase(SETTEXTCHARACTERS)){
        	errorMsg = setText(val,false,true);
        }else if(action.equalsIgnoreCase(SETTEXTVALUE)){
        	errorMsg = setText(val,true,true);
        }else if(action.equalsIgnoreCase(SETUNVERIFIEDTEXTCHARACTERS)){
        	errorMsg = setText(val,false,false);
        }else if(action.equalsIgnoreCase(SETUNVERIFIEDTEXTVALUE)){
        	errorMsg = setText(val,true,false);
        }
        
        if(errorMsg==null){
            testRecordData.setStatusCode(StatusCodes.OK);
            componentSuccessMessage("");
        }else{
        	componentExecutedFailureMessage(errorMsg);
        }
    }
  }
  /**
   * Note:	 To be used by method setText()
   * @return The text of the component
   */
  protected String getText(){
  	String debugMsg = getClass().getName() + ".getText(): ";
  	String textValue=null;
    String className = obj1.getObjectClassName();
    Log.debug("*************  object's className: "+className);
    
    //Treate the html text object: "Html.INPUT.text", "Html.TEXTAREA"
    if(isHtmlDomain(obj1)){
    	textValue = (String) obj1.getProperty("value");
    //Treate the .net text object: "System.Windows.Forms.RichTextBox", "System.Windows.Forms.TextBox", "System.Windows.Forms.MaskedTextBox"
    }else if(isDotnetDomain(obj1)){
    	textValue = (String) obj1.getProperty("Text");
    //Treate the java text object: "java.awt.TextComponent", "javax.swing.text.JTextComponent", JTextField, JTextArea, TextField, TextArea
    }else if(isJavaDomain(obj1)){
    	textValue = (String) obj1.getProperty("text");
    //Treate the FlexTextArea: mx.controls.TextInput and mx.controls.TextArea
    }else if(isFlexDomain(obj1)){
    	textValue = (String) obj1.getProperty("text");
    }else{
    	Log.info(debugMsg+className+" is not supported in current implementation.");
    }
	return textValue;
  }
  
  /**
   * Note:				To be used internally by method performAction()
   * @param text 		The value to be set to the component
   * @param isValue 	true if the type of text is value, false if character
   * @param toVerify	ture if we need to verify the text has been set to the component correctly
   * @return 			null if the text has been correctly set
   */
  private String setText(String text,boolean isValue,boolean toVerify){
  	String debugMsg = getClass().getName() + ".setText(): ";
  	String returnMsg = null;
    GuiTestObject guiObj = new GuiTestObject(obj1);
  
    String currentText = getText();
	//Verify if we can get the text of this component
    if(toVerify){
    	//If we can not get the text from the component
    	//verification fail, returnMsg will be assigned to a fail description
    	//no need to do more, so return.
    	if(currentText==null){
    		returnMsg = failedText.text(FAILStrings.CAN_NOT_GET_TEXT,"Can not read component's text.");
      		Log.debug(debugMsg+returnMsg);
    		return returnMsg;
    	}
    }
    Log.info("............. Orginal val is "+currentText);

    //Set text to the component
    try {
        guiObj.click(new java.awt.Point(4,4));
        TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(guiObj);
        //Clear the text of the component
        parent.inputKeys("^a{ExtDelete}");
        //For some .net textfield, ^a can not select all text, so use another shortcut key to do that again
        //parent.inputKeys("^{ExtHome}^+{ExtEnd}{ExtDelete}");
        parent.inputKeys("{ExtHome}+{ExtEnd}{ExtDelete}");
        
        if(isValue){
        	//Treate the special character in the text and set the translated string to the component
        	parent.inputKeys(text);
        }else{
        	//Take the text as a normal string and set it to the component
        	parent.inputChars(text);
        }
    } catch (MethodNotFoundException mnfe) {
    	//For Flex application, the top parent is FlexApplicationTestObject who has no method inputKeys and inputChars
    	//So we use org.safs.robot.Robot to handle it.
    	Log.debug("Exception occured in " + debugMsg+mnfe.getMessage());
    	try{
    		Log.debug(debugMsg+" Try org.safs.robot.Robot to handle input keys and chars.");
    		guiObj.click();
    		Robot.inputKeys("^a{ExtDelete}");
    		if(isValue){
    			Robot.inputKeys(text);
    		}else{
    			Robot.inputChars(text);
    		}
    		//If we use Robot to input keys or chars, we must wait for a while before verification of input text.
    		//If we don't wait, the verification may fail because that the last character may not have been input by Robot.
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.debug(debugMsg+" did not wait for Robot inputting, the verification may fail.");
			}
    	}catch(AWTException awte){
    		//If Robot can not handle inputting keys and chars, then just set the text property
    		Log.debug(debugMsg+" org.safs.robot.Robot can not handle inputkeys and inputchars. Exception: "+awte.getMessage());
    		if(!setPropertyText(guiObj,text)){
	    		returnMsg = failedText.convert(FAILStrings.COULD_NOT_SET,"Could not set "+text+" to component's text",text,"component's text" );
	      		Log.debug(debugMsg+returnMsg);
	      		return returnMsg;
    		}
    	}
    }

    //Verify if the the current component's text is equal to the text that we want to set
    //If they are not equal, verification fail, returnMsg will be assigned to a fail description
    if(toVerify){
    	currentText = this.getText();
    	//If the type of text is value,text may contain special characters
    	if(isValue){
    		//If the text does not contain special character, do the verification
    		if(!StringUtils.containsSepcialKeys(text)){
    			if(!text.equalsIgnoreCase(currentText)){
    	    		returnMsg = failedText.convert(FAILStrings.TEXT_DIFFERENT,"Component's current text is different to "+text,text);
    	      		Log.debug(debugMsg+returnMsg);
    			}
    		}
    	}else{
    		//The type of text is character, do the verification
    		if(!text.equalsIgnoreCase(currentText)){
        		returnMsg = failedText.convert(FAILStrings.TEXT_DIFFERENT,"Component's current text is different to "+text,text +".  current:" +currentText);
          		Log.debug(debugMsg+returnMsg);
    		}
    	}
    }
    return returnMsg;
  }
  
  protected boolean setPropertyText(TestObject testObject,String text){
	  	String debugMsg = getClass().getName() + ".setPropertyText(): ";
	  	String textPropertyName = null;
	  	
	    String className = testObject.getObjectClassName();
	    Log.debug(debugMsg+"set value "+text+" to object whose className is "+className);
	    
	    //Treate the html text object: "Html.INPUT.text", "Html.TEXTAREA"
	    if(isHtmlDomain(testObject)){
	    	textPropertyName = "value";
	    //Treate the .net text object: "System.Windows.Forms.RichTextBox", "System.Windows.Forms.TextBox", "System.Windows.Forms.MaskedTextBox"
	    }else if(isDotnetDomain(testObject)){
	    	textPropertyName = "Text";
	    //Treate the java text object: "java.awt.TextComponent", "javax.swing.text.JTextComponent", JTextField, JTextArea, TextField, TextArea
	    }else if(isJavaDomain(testObject)){
	    	textPropertyName = "text";
	    //Treate the FlexTextArea: mx.controls.TextInput and mx.controls.TextArea
	    }else if(isFlexDomain(testObject)){
	    	textPropertyName = "text";
	    }else if(isWinDomain(testObject)){
	    	textPropertyName = ".value";
	    }
	    
	    if(textPropertyName!=null){
	    	try{
	    		testObject.setProperty(textPropertyName,text);
	    	}catch(PropertyNotFoundException e){
	    		Log.info(debugMsg+" property" +textPropertyName+" can not be found for class "+className+". Could not set valut to it.");
	    		return false;
	    	}
	    }else{
	    	Log.info(debugMsg+className+" is not supported in current implementation.");
	    	return false;
	    }
	    
	    return true;
  }
  
}
