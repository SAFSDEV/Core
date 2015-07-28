package org.safs.rational.wpf;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.rational.CFComboBox;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.WPF.WpfTextSelectGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.ISelect;
import com.rational.test.ft.object.interfaces.IText;

/**
 * <br><em>Purpose:</em> CFWPFComboBox extends CFComboBox to process a ComboBox component of WPF.
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  JunwuMa
 * @since   Oct 12, 2009
 *   <br>   Oct 12, 2009  (JunwuMa) Original Release
 *
 */   
public class CFWPFComboBox extends CFComboBox {
	protected void doSelect(String action) throws SAFSException {
		String debugMsg = getClass().getName() + "doSelect(): ";
		if (params.size() < 1) {
	        paramsFailedMsg(windowName, compName);
	    } else {
	    	String param = (String) params.iterator().next();
	        Log.info(debugMsg + ".....with param: "+param);
	        
	        if (!(obj1 instanceof ISelect)) {
				Log.debug(debugMsg+ obj1.getObjectClassName() + " is NOT an instance of ISelect. Find other way to do select-actions.");
	            log.logMessage(testRecordData.getFac(),
            			failedText.convert(FAILStrings.UNEXPECTED_OBJECT, 
            					"Unexpected object: NOT an instance of ISelect.", "ISelect"),
                        FAILED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);	
				return;
	        }
	        
	        ISelect combox = (ISelect)obj1;
            if (action.equalsIgnoreCase(SELECTINDEX)){
                try {
                	int index = Integer.parseInt(param);
                	combox.select(index);	
                }catch(NumberFormatException nfe){
                    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                    log.logMessage(testRecordData.getFac(),
                                   getClass().getName()+": invalid index format: "+param,
                       			   failedText.convert(FAILStrings.INVALID_INDEX_FORMAT, 
                    					"Invalid index format: "+param, param),
                                   FAILED_MESSAGE);
                    return;               	
                }
            }
            else if (action.equalsIgnoreCase(SELECTPARTIALMATCH)){
                //implement the partial match...
                java.util.List list = null;
                try {
                	list = captureObjectData(obj1);
	                Log.info("list: "+list);
	                // do the work of matching...
	                int idx = StringUtils.findMatchIndex(list.listIterator(), param);
	                if (idx >= 0) 
	                	combox.select(idx);  
	                else {
	                    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	                    log.logMessage(testRecordData.getFac(),
	                       			   failedText.convert(FAILStrings.NO_MATCH_FOUND, 
	                       					"No match found for: "+param, param),
	                                   FAILED_MESSAGE);
	                    return;
	                }
	            } catch(Exception ex) {
	                throw new SAFSException(ex.getMessage());
	            }  
	        }
	        else if (action.equalsIgnoreCase(SELECT) || action.equalsIgnoreCase(SELECTTEXTITEM)) {
	        	combox.select(param);
	        	// verifying...
	            String selectedTest = combox.getSelectedText();
	            if (!compareValues(selectedTest, param)) 
	            	return; // failure state has been set in compareValues
	      	}	
	        else if (action.equalsIgnoreCase(SELECTUNVERIFIED) || action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)) {
	            	combox.select(param);	  
	        }
            // set status to ok
        	String altText = action + " successful using " + param;
        	log.logMessage(testRecordData.getFac(),
        				   genericText.convert(GENStrings.SUCCESS_2A, altText, action, param),
			               PASSED_MESSAGE);	
            testRecordData.setStatusCode(StatusCodes.OK);	            
	    }
	}
	/**
	 * overrides its parent to support DOTNET_WPF controls
	 * @param table TestObject to snapshot data from.
	 * @exception SAFSException
	 */
	protected java.util.List captureObjectData(TestObject table)throws SAFSException{
		String debugMsg = getClass().getName() + ".captureObjectData() ";
		Log.info(debugMsg + "attempting to extract list items...");
		   
	    java.util.List list = new ArrayList();

		if (obj1 instanceof ISelect) {
			String selectedText = ((ISelect)obj1).getText();
			try {
				list = (ArrayList)StringUtils.readstring(selectedText);
			} catch(IOException ioe) {}	
		}else {
			Log.debug(debugMsg+ obj1.getObjectClassName() + " can't be handled as an ISelect, Please find other way to get text.");
            log.logMessage(testRecordData.getFac(),
        					failedText.convert(FAILStrings.UNEXPECTED_OBJECT, 
        					"Unexpected object: can't be handled as an instance of ISelect.", "ISelect"),
        					FAILED_MESSAGE);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		}
		return list;
	}
	
	  
	/** <br><em>Overrides its parent to perform action 'VerifySelected' on DOTNET_WPF controls</em> 
	 *  @exception SAFSException
	 */
	protected void verifySelected() throws SAFSException {
		String debugMsg = getClass().getName() + ".verifySelected(): ";
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
	    } else {
	    	String param = (String) params.iterator().next();
			Log.info(debugMsg + "...verifying selected text in " + obj1.getObjectClassName() + "with param: " + param);
	    	// ready to do the verify
			if (obj1 instanceof ISelect) {
				String selectedText = ((ISelect)obj1).getSelectedText();
		    	if (compareValues(selectedText, param)) {
		    		log.logMessage(testRecordData.getFac(),
							passedText.convert(TXT_SUCCESS_4, action, windowName, compName, action, param),
							PASSED_MESSAGE);
		            testRecordData.setStatusCode(StatusCodes.OK);
		    	}	
			} else {
				Log.debug(debugMsg+ obj1.getObjectClassName() + " can't be handled as an ISelect, Please find other way to get selected text.");
	            log.logMessage(testRecordData.getFac(),
            					failedText.convert(FAILStrings.UNEXPECTED_OBJECT, 
            					"Unexpected object: can't be handled as an instance of ISelect.", "ISelect"),
            					FAILED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			}
	    }
	}
	
	/**
	 * Overrides its parent to support DOTNET_WPF
	 * @exception SAFSException
	 */
	protected void setTextValue (boolean performVerification) throws SAFSException {
		String debugMsg = getClass().getName() + "setTextValue(): ";
	    if (params.size() < 1) {
	    	paramsFailedMsg(windowName, compName);
	    } else {
	       	IText combox = null;
	       	if (!(obj1 instanceof IText)) {
				Log.debug(debugMsg+ obj1.getObjectClassName() + " is NOT an instance of ISelect. Find other way to do select-actions.");
	            log.logMessage(testRecordData.getFac(),
            					failedText.convert(FAILStrings.UNEXPECTED_OBJECT, 
            					"Unexpected object: NOT an instance of ISelect.", "ISelect"),
            					FAILED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);	
				return;
	        }else
	        
	        combox = (IText)obj1;
	        
	        Iterator piter = params.iterator();
	        String newValue = (String) piter.next();
	        Log.info(debugMsg + "...value to set: " + newValue);
	       	
	        boolean isEditable = false;
	        try {
	        	Object prop = obj1.getProperty("IsEditable");
	        	isEditable = Boolean.parseBoolean(prop.toString());
	        	Log.info("...the value of property 'IsEditable': " + isEditable);
	        } catch (PropertyNotFoundException pnfe) {
	        	Log.debug(debugMsg + "object has no property: 'IsEditable'." + pnfe.getMessage());
	        	Log.info(debugMsg + "...PropertyNotFoundException is thrown. Its value is considered unchangable.");
	        }
	        if (isEditable) {
		        Log.info("...start to set new value: " + newValue);
	        	combox.setText(newValue);
	        	// do not verify the new value
	    		log.logMessage(testRecordData.getFac(),
						passedText.convert(TXT_SUCCESS_4, action, windowName, compName, action, newValue),
						PASSED_MESSAGE);
	            testRecordData.setStatusCode(StatusCodes.OK);	        		
	        } else {
	            log.logMessage(testRecordData.getFac(),
            			failedText.convert(FAILStrings.TEXT_UNEDITABLE, 
            					"Component's current text is uneditable.",""),
    					FAILED_MESSAGE);
	            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	        }
	     }
	  }
	  
	  
	
}