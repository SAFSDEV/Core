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
package org.safs.selenium;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.jvmagent.SAFSActionErrorRuntimeException;

public class CFListBox extends CFComponent {
	//ListBoxFunctions Actions
	public static final String ACTIVATEPARTIALMATCH	= "ActivatePartialMatch";
	public static final String ACTIVATETEXTITEM	= "ActivateTextItem";
	public static final String ACTIVATEUNVERIFIEDPARTIALMATCH	= "ActivateUnverifiedPartialMatch";
	public static final String ACTIVATEUNVERIFIEDTEXTITEM	= "ActivateUnverifiedTextItem";
	public static final String SELECTANOTHERTEXTITEM	= "SelectAnotherTextItem";
	public static final String SELECTANOTHERUNVERIFIEDTEXTITEM	= "SelectAnotherUnverifiedTextItem";
	public static final String SELECTINDEX	= "SelectIndex";
	public static final String SELECTPARTIALMATCH	= "SelectPartialMatch";
	public static final String SELECTTEXTITEM	= "SelectTextItem";
	//public static final String SELECTUNVERIFIEDANOTHERTEXTITEM	= "SelectUnverifiedAnotherTextItem"; //DEPRECATED
	public static final String SELECTUNVERIFIEDPARTIALMATCH	= "SelectUnverifiedPartialMatch";
	public static final String SELECTUNVERIFIEDTEXTITEM	= "SelectUnverifiedTextItem";
	//public static final String VERIFYITEM	= "VerifyItem"; //RobotJ only (for now)
	public static final String VERIFYITEMUNSELECTED	= "VerifyItemUnselected";
	public static final String VERIFYLISTCONTAINS	= "VerifyListContains";
	public static final String VERIFYLISTCONTAINSPARTIALMATCH	= "VerifyListContainsPartialMatch";
	public static final String VERIFYLISTDOESNOTCONTAIN	= "VerifyListDoesNotContain";
	public static final String VERIFYLISTDOESNOTCONTAINPARTIALMATCH	= "VerifyListDoesNotContainPartialMatch";
	//public static final String VERIFYMENUITEM	= "VerifyMenuItem";  //RobotJ only (for now)
	//public static final String VERIFYMENUPARTIALMATCH	= "VerifyMenuPartialMatch";  //RobotJ only (for now)
	//public static final String VERIFYPARTIALMATCH	= "VerifyPartialMatch";  //RobotJ only (for now)
	public static final String VERIFYSELECTEDITEM	= "VerifySelectedItem";
	public static final String VERIFYSELECTEDPARTIALMATCH	= "VerifySelectedPartialMatch";

	String option = "";			
	
	public CFListBox() {
		super();
	}
	
	protected void localProcess(){
		option = "";
		if (action != null) {
			try {
				option = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(4));
			} catch (SAFSNullPointerException e) {	
				//TODO: handle no parser available for inputRecord?
			}
			SGuiObject comp = sHelper.getCompTestObject();
			
			if(action.equalsIgnoreCase(SELECTTEXTITEM)){
				doSelectItem(true);
			} else if(action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)){
				doSelectItem(false);
			} else if(action.equalsIgnoreCase(SELECTINDEX)){
				doSelectItemIndex();
			} else if(action.equalsIgnoreCase(SELECTPARTIALMATCH)){
				doSelectPartialMatch(true);
			} else if(action.equalsIgnoreCase(SELECTUNVERIFIEDPARTIALMATCH)){
				doSelectPartialMatch(false);
			} else if(action.equalsIgnoreCase(SELECTANOTHERTEXTITEM)){
				doSelectAnotherItem(true);
			} else if(action.equalsIgnoreCase(SELECTANOTHERUNVERIFIEDTEXTITEM)){
				doSelectAnotherItem(false);
			} else if(action.equalsIgnoreCase(ACTIVATETEXTITEM) || action.equalsIgnoreCase(ACTIVATEUNVERIFIEDTEXTITEM) ){
				doActivateItem(false);
			} else if(action.equalsIgnoreCase(ACTIVATEPARTIALMATCH) || action.equalsIgnoreCase(ACTIVATEUNVERIFIEDPARTIALMATCH) ){
				doActivatePartialMatch(false);
			} else if((action.equalsIgnoreCase(VERIFYLISTCONTAINS))||
					(action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH))||
					(action.equalsIgnoreCase(VERIFYSELECTEDITEM))||
					(action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH))){
				doVerifyItemSelected();
			} else if((action.equalsIgnoreCase(VERIFYITEMUNSELECTED))||
					(action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAIN))||
					(action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH))){
				doVerifyItemUnselected();
			}
		}
	}
		
	private void doActivateItem(boolean verify){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);
		selenium.fireEvent(comp.getLocator()+"/OPTION[text()='"+option+"']","dblclick");
		if (verify){
			//TODO: Verify may not be viable option since double-clicks do different things in apps	
		}else{		
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				genericText.convert("attempt3a",  windowName+":"+compName +" "+ action +
									" attempted using '"+ option +"'.",
									windowName, compName, action, option), 
				PASSED_MESSAGE);
		}
	}

	// VerifyItemSelected, VerifyListContains, VerifyListContainsPartialMatch
	private void doVerifyItemSelected(){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);		
		String [] options = null;
		
		if ((action.equalsIgnoreCase(VERIFYLISTCONTAINS))|| 
			(action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH))){
			options = selenium.getSelectOptions(comp.getLocator());
		} else {
			options = selenium.getSelectedLabels(comp.getLocator());
		} 
		int i;
		String msg ="";
		boolean matched;
		String matchedValue = option;
		for(i = 0; i < options.length; i++){
			if ((action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH))||
				(action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH))){
				matched = (options[i].indexOf(option) > -1);
				matchedValue = options[i];
			}
			// VerifyItemSelected, VerifyListDoesContains
			else{
				matched = (options[i].equals(option));
			}
			if(matched){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
		
				if ((action.equalsIgnoreCase(VERIFYSELECTEDITEM))||
					(action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH))){
					msg = genericText.convert("is_selected",  "'"+ matchedValue +"' is selected in "+ compName,
						matchedValue, compName);			
				}else if ((action.equalsIgnoreCase(VERIFYLISTCONTAINS))||
						  (action.equalsIgnoreCase(VERIFYLISTCONTAINSPARTIALMATCH))){
					msg = genericText.convert("contains",  "'"+ compName +"' contains '"+ matchedValue +"'",
						compName, matchedValue);			
				} 
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				return;
			}
		}
		Log.debug("...CFListBox failed to "+ action +" "+ option);
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		
		if ((action.equalsIgnoreCase(VERIFYSELECTEDITEM))||
			(action.equalsIgnoreCase(VERIFYSELECTEDPARTIALMATCH))){
			msg = genericText.convert("is_not_selected",  "'"+ option +"' is not selected in "+ compName,
				option, compName);			
		}
		// VerifyListContains, VerifyListContainsPartialMatch
		else {
			msg = genericText.convert("not_contain",  "'"+ compName +"' does not contain '"+ option +"'",
				compName, option);			
		} 
		log.logMessage(testRecordData.getFac(), this.getStandardErrorMessage(action), msg, FAILED_MESSAGE);
	}

	// VerifyItemUnselected, VerifyListDoesNotContain, VerifyListDoesNotContainPartialMatch
	private void doVerifyItemUnselected(){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);		
		String [] options = null;
		
		if ((action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAIN))|| 
			(action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH))){
			options = selenium.getSelectOptions(comp.getLocator());
		} else if (action.equalsIgnoreCase(VERIFYITEMUNSELECTED)) {
			options = selenium.getSelectedLabels(comp.getLocator());
		} 
		int i;
		String msg ="";
		boolean matched;
		String matchedValue = option;
		for(i = 0; i < options.length; i++){
			if (action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH)){
				matched = (options[i].indexOf(option) > -1);
				matchedValue = options[i];
			}
			// VerifyItemUnselected, VerifyListDoesNotContain
			else{
				matched = (options[i].equals(option));
			}
			if(matched){
				Log.debug("...CFListBox failed to "+ action +" "+ option);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		
				if (action.equalsIgnoreCase(VERIFYITEMUNSELECTED)){
					msg = genericText.convert("is_selected",  "'"+ matchedValue +"' is selected in "+ compName,
						matchedValue, compName);			
				}else if ((action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAIN))||
						  (action.equalsIgnoreCase(VERIFYLISTDOESNOTCONTAINPARTIALMATCH))){
					msg = genericText.convert("contains",  "'"+ compName +"' contains '"+ matchedValue +"'",
						compName, matchedValue);			
				} 
				log.logMessage(testRecordData.getFac(), 
					getStandardErrorMessage(action), msg, FAILED_MESSAGE);
				return;
			}
		}
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
		
		if (action.equalsIgnoreCase(VERIFYITEMUNSELECTED)){
			msg = genericText.convert("is_not_selected",  "'"+ option +"' is not selected in "+ compName,
				option, compName);			
		}
		// VerifyListDoesNotContain, VerifyListDoesNotContainPartialMatch
		else {
			msg = genericText.convert("not_contain",  "'"+ compName +"' does not contain '"+ option +"'",
				compName, option);			
		} 
		log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
	}

	private void doActivatePartialMatch(boolean verify){
		//TODO: Verify may not be viable since doubleclicks do various things to an app
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);		
		String [] options = selenium.getSelectOptions(comp.getLocator());
		int i;
		for(i = 0; i < options.length; i++){
			if(options[i].indexOf(option) > -1){
				String optionValue = options[i];
				selenium.fireEvent(comp.getLocator()+"/OPTION[text()='"+optionValue+"']","dblclick");
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					genericText.convert("attempt3a",  windowName+":"+compName +" "+ action +
										" attempted using '"+ option +"'.",
										windowName, compName, action, option), 
					PASSED_MESSAGE);
				return;
			}
		}
		Log.debug("...CFListBox failed to "+ action +" "+ option);
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
		log.logMessage(testRecordData.getFac(), 
			getStandardErrorMessage(action),
			failedText.convert("could_not_select",  "Could not select '"+ option +
							"' in "+ compName,
							option, compName), 
			FAILED_MESSAGE);
	}

	private void doSelectPartialMatch(boolean verify){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);		
		String [] options = selenium.getSelectOptions(comp.getLocator());
		int i;
		for(i = 0; i < options.length; i++){
			if(options[i].indexOf(option) > -1){
				break;
			}
		}
		if(i != options.length){
			selenium.select(comp.getLocator(),"index="+i);
		}
		if(verify){			
			String selected = selenium.getSelectedLabel(comp.getLocator());		
			if(selected.indexOf(option)> -1){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					genericText.convert("selection_partial_match",  compName +":"+ action +
										" selection '"+ selected +"' contains substring '"+ option +"'.",
										compName, action, selected, option), 
					PASSED_MESSAGE);
			}else{
				Log.debug("...CFListBox failed to "+ action +" "+ option);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					getStandardErrorMessage(action),
					//selection_not_partial_match :Selection '%1%' does not contain substring '%2%'.
					genericText.convert("selection_not_partial_match",  "Selection '"+ selected +
									"' does not contain substring '"+ option +"'.",
									selected,option), 
					FAILED_MESSAGE);
			}
		}
		// UNVERIFIED
		else{
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				genericText.convert("attempt3a",  windowName+":"+compName +" "+ action +
									" attempted using '"+ option +"'.",
									windowName, compName, action, option), 
				PASSED_MESSAGE);
		}
	}

	private void doSelectItem(boolean verify){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);
		selenium.select(comp.getLocator(),option);	
		if (verify){		
			String selected = selenium.getSelectedLabel(comp.getLocator());
			if(selected.equals(option)){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					genericText.convert("selection_matches",  compName +":"+ action +
	                                    " selection matches expected value '"+ option +"'.",
	                                    compName, action,option), 
					PASSED_MESSAGE);
			}else{
				Log.debug("...CFListBox failed to "+ action +" "+ option);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					getStandardErrorMessage(action),
					failedText.convert("selection_not_match",  "Selection '"+ selected +
									"' does not match expected value '"+ option +"'.",
									selected,option), 
					FAILED_MESSAGE);
			}
		}
		// UNVERIFIED
		else{
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				genericText.convert("attempt3a",  windowName+":"+compName +" "+ action +
									" attempted using '"+ option +"'.",
									windowName, compName, action, option), 
				PASSED_MESSAGE);
		}
	}

	private void doSelectAnotherItem(boolean verify){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "TEXTVALUE", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);
		selenium.addSelection(comp.getLocator(),option);
		if (verify){		
			String [] selected = selenium.getSelectedLabels(comp.getLocator());
			for(int i = 0; i < selected.length; i++){
				if(selected[i].equals(option)){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					genericText.convert("selection_matches",  compName +":"+ action +
										" selection matches expected value '"+ option +"'.",
										compName, action,option), 
					PASSED_MESSAGE);
					return;
				}
			}
			Log.debug("...CFListBox failed to "+ action +" "+ option);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				getStandardErrorMessage(action),
				failedText.convert("could_not_select",  "Could not select '"+ option +
								"' in "+ compName,
								option, compName), 
				FAILED_MESSAGE);
		}
		// UNVERIFIED
		else{
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				genericText.convert("attempt3a",  windowName+":"+compName +" "+ action +
									" attempted using '"+ option +"'.",
									windowName, compName, action, option), 
				PASSED_MESSAGE);
		}
	}

	private void doSelectItemIndex(){
		if (! validateParamSize(1)) return;
		if (! getRequiredField(4, "INDEX", option)) return;
		SGuiObject comp = sHelper.getCompTestObject();
		Log.info("...CFListBox attempting to "+ action +" "+ option);
		try{
			int i = Integer.parseInt(option);
			if (i < 0){
				Log.debug("...CFListBox invalid INDEX using "+ action +" "+ option);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
				log.logMessage(testRecordData.getFac(), 
					getStandardErrorMessage(failedText.convert("bad_param", 
											"Invalid parameter value for INDEX",
											"INDEX")),
					testRecordData.getInputRecord(), 
					FAILED_MESSAGE);
				return;
			}
		}catch(NumberFormatException nf){
			Log.debug("...CFListBox invalid INDEX using "+ action +" "+ option);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				getStandardErrorMessage(failedText.convert("bad_param", 
										"Invalid parameter value for INDEX",
										"INDEX")),
				testRecordData.getInputRecord(), 
				FAILED_MESSAGE);
			return;
		}
		selenium.select(comp.getLocator(),"index="+option);
		String selected = selenium.getSelectedIndex(comp.getLocator()).trim();
		if(selected.equalsIgnoreCase(option)){
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				genericText.convert("selection_matches",  compName +":"+ action +
									" selection matches expected value '"+ option +"'.",
									compName, action,option), 
				PASSED_MESSAGE);
		}else{
			Log.debug("...CFListBox failed to "+ action +" "+ option);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
				getStandardErrorMessage(action),
				failedText.convert("selection_not_match",  "Selection '"+ selected +
								"' does not match expected value '"+ option +"'.",
								selected,option), 
				FAILED_MESSAGE);
		}
	}
}
