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

public class CFComboBox extends CFComponent {
	//ComboBoxFunctions Actions
	//public static final String CAPTUREITEMSTOFILE	= "CaptureItemsToFile";//RobotJ only (for now)
	//public static final String HIDELIST	= "HideList";//RobotJ only (for now)
	public static final String SELECT	= "Select";
	public static final String SELECTINDEX	= "SelectIndex";
	public static final String SELECTPARTIALMATCH	= "SelectPartialMatch";
	public static final String SELECTUNVERIFIED	= "SelectUnverified";
	public static final String SETTEXTVALUE	= "SetTextValue"; //UNIMPLEMENTED
	public static final String SETUNVERIFIEDTEXTVALUE	= "SetUnverifiedTextValue";//UNIMPLEMENTED
	//public static final String SHOWLIST	= "ShowList";//RobotJ only (for now)
	//public static final String VERIFYSELECTED	= "VerifySelected";//RobotJ only (for now)
	
	public CFComboBox() {
		super();
	}
	
	protected void localProcess(){
		if (action != null) {
			String option = "";
			String msg = "";
			String detail = "";
			String label = "";
			int selindex = 0;
			
			try{
				if (params.size() < 1) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					paramsFailedMsg(windowName, compName);
					return;
				}
				option = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(4));
				
				//===================================================
				if(action.equalsIgnoreCase(SELECT)){
					selenium.select(sHelper.getCompTestObject().getLocator(),option);
					label = selenium.getSelectedLabel(sHelper.getCompTestObject().getLocator());
					if(label.equalsIgnoreCase(option)){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("selection_matches", windowName +":"+ compName +" selection matches expected value '"+ option +"'.",
								 windowName, compName, option);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
						return;
					}
					else{
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
						detail = failedText.convert("selection_not_match", "Selection '"+ label +"' does not match expected value '"+ option +"'.",
						         label, option);
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
						return;
					}
				}
				//===================================================
				// test tables indices are 1-based but Selenium is 0-based 
				else if(action.equalsIgnoreCase(SELECTINDEX)){
					int offset = 0;
					try{ 
						offset = Integer.parseInt(option);
						if (offset < 1){
							detail = genericText.convert("not_greater",
							"IndexValue is not greater than 0.",
							"IndexValue", "0");
							throw new NumberFormatException("Less Than 0");      
						}
						offset--;					
					}catch(NumberFormatException nf){
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = failedText.convert("invalid_missing", 
							  "Invalid or Missing IndexValue in table "+ 
							  testRecordData.getFilename() +" at line "+
							  testRecordData.getLineNumber() +".",
							  "IndexValue",
							  testRecordData.getFilename(),
							  String.valueOf(testRecordData.getLineNumber()));
						if (detail.length() > 0)
							log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
						else
							log.logMessage(testRecordData.getFac(), msg, FAILED_MESSAGE);					
						return;
					}
					selenium.select(sHelper.getCompTestObject().getLocator(),"index="+offset);
					label = selenium.getSelectedIndex(sHelper.getCompTestObject().getLocator());
					if( label.equalsIgnoreCase(String.valueOf(offset).trim())){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("selected_index_matches", windowName +":"+ compName +" selected index matches expected value '"+ option +"'.",
								 windowName, compName, option);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
						return;
					}
					else{
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						label = String.valueOf(Integer.parseInt(label)+1);
						detail = failedText.convert("selected_index_not_match", "Selected index '"+ label +"' does not match expected value '"+ option +"'.",
								 label, option);
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
						return;
					}
				//===================================================
				} else if(action.equalsIgnoreCase(SELECTPARTIALMATCH)){
					String [] options = selenium.getSelectOptions(sHelper.getCompTestObject().getLocator());
					int i;
					for(i = 0; i < options.length; i++){
						if(options[i].indexOf(option) > -1){
							break;
						}
					}
					if(i != options.length){
						selenium.select(sHelper.getCompTestObject().getLocator(),"index="+i);
						label = selenium.getSelectedLabel(sHelper.getCompTestObject().getLocator());
						if(label.indexOf(option) > -1){
							testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
							msg = genericText.convert("selection_partial_match", windowName +":"+ compName +" selection '"+ label +"' contains substring '"+ option +"'.",
									 windowName, compName, label, option);
							log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
							return;
						}
						else{
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
							detail = failedText.convert("selection_not_partial_match", "Selection '"+ label +"' does not contain substring '"+ option +"'.",
									 label, option);
							log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
							return;
						}
					}
				//===================================================
				} else if(action.equalsIgnoreCase(SELECTUNVERIFIED)){
					try{
						selenium.select(sHelper.getCompTestObject().getLocator(),option);
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					}
					catch(Exception x){
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						Log.debug("Selenium ComboBox Select '"+ option +"' error:", x);
					}
				} 
			}catch (SAFSNullPointerException e) {	
			
			}
			catch(SAFSException e){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug("Selenium ComboBox Error processing parameters. ", e);
			}
			
			//TODO: SetTextValue and SetTextValueUnverified
			if(testRecordData.getStatusCode() == StatusCodes.NO_SCRIPT_FAILURE){
				msg = genericText.convert("success3a", windowName +":"+ compName + " "+ action +" successful using "+ option +".",
						windowName, compName, action, option);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

			}
			//FAILURE: No warnings or other failure types currently expected
			else if(testRecordData.getStatusCode() != StatusCodes.SCRIPT_NOT_EXECUTED){ 
				msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);
			}
		}
	}

}
