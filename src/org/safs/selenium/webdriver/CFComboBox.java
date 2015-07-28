/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.ComboBoxException;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;
import org.safs.tools.CaseInsensitiveFile;

/**
 * 
 * History:<br>
 * 
 *  <br>   DEC 18, 2013    (Lei Wang) Initial release.
 *  <br>   JAN 16, 2014    (Lei Wang) Add keywords support.
 */
public class CFComboBox extends CFComponent {

	public static final String CAPTUREITEMSTOFILE	= "CaptureItemsToFile";
	public static final String HIDELIST	= "HideList";
	public static final String SHOWLIST	= "ShowList";
	
	public static final String SELECT	= "Select";
	public static final String SELECTINDEX	= "SelectIndex";
	public static final String SELECTPARTIALMATCH	= "SelectPartialMatch";
	public static final String SELECTUNVERIFIED	= "SelectUnverified";
	public static final String VERIFYSELECTED	= "VerifySelected";

	//The following 2 keywords are not supported for now. Html combo-box doesn't accept setting value.
	public static final String SETTEXTVALUE	= "SetTextValue";
	public static final String SETUNVERIFIEDTEXTVALUE	= "SetUnverifiedTextValue";
	
	ComboBox combobox;
	
	public CFComboBox() {
		super();
	}
	
	protected ComboBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new ComboBox(webelement);
	}
	
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");
		
		if (action != null) {
			String param1 = "";
			String param2 = "";
			String msg = "";
			String detail = "";
			String label = "";
			Iterator<?> iterator = null;
			
			try{
				super.localProcess();
				combobox = (ComboBox) libComponent;
				
				Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();
				
				if(action.equalsIgnoreCase(SELECT)||
				   action.equalsIgnoreCase(SELECTINDEX)||
				   action.equalsIgnoreCase(SELECTPARTIALMATCH)||
				   action.equalsIgnoreCase(SELECTUNVERIFIED)||
				   action.equalsIgnoreCase(VERIFYSELECTED)||
				   action.equalsIgnoreCase(CAPTUREITEMSTOFILE)){
					
					if (params.size() < 1) {
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						paramsFailedMsg(windowName, compName);
						return;
					}
					param1 = (String) iterator.next();
				}
				
				if(iterator.hasNext()){
					if(param1.isEmpty()) param1 = (String)iterator.next();
					else param2 = (String) iterator.next();
				}
				
				if(action.equalsIgnoreCase(SELECT)){
					try{
						combobox.select(param1, true, false, true);

						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.SELECTION_MATCHES, windowName +":"+ compName +" selection matches expected value '"+ param1 +"'.",
								                  windowName, compName, param1);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					}catch(SeleniumPlusException spe){
						Log.debug(debugmsg+spe.getCode());
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
						if(ComboBoxException.CODE_FAIL_VERIFICATION.equals(spe.getCode())){
							label = spe.getInfo();
							detail = failedText.convert(FAILKEYS.SELECTION_NOT_MATCH, "Selection '"+ label +"' does not match expected value '"+ param1 +"'.",
									                    label, param1);							
						}
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
					}
					
				}else if(action.equalsIgnoreCase(SELECTINDEX)){
					int offset = 0;
					try{ 
						offset = Integer.parseInt(param1);
						if (offset < 1){
							detail = genericText.convert(GENKEYS.NOT_GREATER,
							"IndexValue is not greater than 0.",
							"IndexValue", "0");
							throw new NumberFormatException("Less Than 0");      
						}
						// test tables indices are 1-based but Selenium is 0-based 
						offset--;					
					}catch(NumberFormatException nf){
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = failedText.convert(FAILKEYS.INVALID_MISSING, 
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
					
					try{
						combobox.selectIndex(offset, true, true);

						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.SELECTED_INDEX_MATCHES, windowName +":"+ compName +" selected index matches expected value '"+ param1 +"'.",
								                  windowName, compName, param1);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
						
					}catch(SeleniumPlusException spe){
						Log.debug(debugmsg+spe.getCode());
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
						if(ComboBoxException.CODE_FAIL_VERIFICATION.equals(spe.getCode())){
							label = String.valueOf(Integer.parseInt(spe.getInfo())+1);
							detail = failedText.convert(FAILKEYS.SELECTED_INDEX_NOT_MATCH, "Selected index '"+ label +"' does not match expected value '"+ param1 +"'.",
									                    label, param1);
						}
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
					}
					
				} else if(action.equalsIgnoreCase(SELECTPARTIALMATCH)){
					try{
						List<String> list = combobox.select(param1, true, true, true);

						label = list.get(0);
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.SELECTION_PARTIAL_MATCH, windowName +":"+ compName +" selection '"+ label +"' contains substring '"+ param1 +"'.",
								                  windowName, compName, label, param1);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
						
					}catch(SeleniumPlusException spe){
						Log.debug(debugmsg+spe.getCode());
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = this.getStandardErrorMessage(windowName +":"+ compName +" "+ action);
						if(ComboBoxException.CODE_FAIL_VERIFICATION.equals(spe.getCode())){
							label = spe.getInfo();
							detail = failedText.convert(FAILKEYS.SELECTION_NOT_PARTIAL_MATCH, "Selection '"+ label +"' does not contain substring '"+ param1 +"'.",
									                    label, param1);
						}						
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
					}

				} else if(action.equalsIgnoreCase(SELECTUNVERIFIED)){
					combobox.select(param1, false, false, true);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3A, windowName+":"+compName+" "+action+" successful using "+param1,
							                  windowName, compName, action, param1);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					
				} else if(action.equalsIgnoreCase(VERIFYSELECTED)){
					try{
						List<String> list = combobox.verifySelected(param1);

						label = list.get(0);
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.IS_SELECTED, "'"+label+"' is selected in "+windowName +":"+ compName,
								                  label, windowName +":"+ compName);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
						
					}catch(SeleniumPlusException spe){
						Log.debug(debugmsg+spe.getCode());
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
						if(ComboBoxException.CODE_FAIL_VERIFICATION.equals(spe.getCode())){
							label = spe.getInfo();
							detail = genericText.convert(GENKEYS.IS_NOT_SELECTED, "'"+label+"' is not selected in "+windowName +":"+ compName,
									                     label, windowName +":"+ compName);
						}
						log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
					}

				} else if(action.equalsIgnoreCase(HIDELIST)){
					combobox.hidePopup();
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3, windowName+":"+compName+" "+action+" successful.", 
							                  windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
					
				} else if(action.equalsIgnoreCase(SHOWLIST)){
					combobox.showPopup();
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3, windowName+":"+compName+" "+action+" successful.", 
							                  windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
					
				} else if(action.equalsIgnoreCase(CAPTUREITEMSTOFILE)){
					captureItemsToFile(param1, param2);
					
				}
			}
			catch(SAFSException e){
				Log.error(debugmsg+"Selenium ComboBox Error processing parameters. ", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+e.getMessage();
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}
	
	/**
	 * <br><em>Purpose:</em> captureItemsToFile
	 * <p> example step commands:
	 * <p>
	 * <br> T, JavaWin, JList, CaptureItemsToFile, AFileName.txt
	 * <br> Capture all items of Combo Box to file AFileName.txt
	 * <br> 
	 **/
	protected void captureItemsToFile (String filename, String encoding) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "captureItemsToFile");

		try {
			Log.info(debugmsg+"...filename: "+filename+" ; encoding:"+encoding);

			List<String> list = combobox.getDataList();
			Log.info(debugmsg+"list: "+list);

			File file = new CaseInsensitiveFile(filename).toFile();
			if (!file.isAbsolute()) {
				String testdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
				if (testdir != null) {
					file = new CaseInsensitiveFile(testdir, filename).toFile();
					filename = file.getAbsolutePath();
				}
			}
			Log.info(debugmsg+"Writing to file: "+filename);
			StringUtils.writeEncodingfile(filename, list, encoding);

			testRecordData.setStatusCode(StatusCodes.OK);
			String msg = genericText.convert(GENKEYS.SUCCESS_4, action+" performed on "+compName+" in "+windowName+"; output file '"+filename+"'.",
					                         action, compName, windowName, filename);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

		}catch (Exception se) {
			throw new SeleniumPlusException(se.getMessage());
		}
	}

	protected Collection<String> captureObjectData() throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);
		Collection<String> data = null;
		
		try{
			data = combobox.getDataList();
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Met "+StringUtils.debugmsg(e));
		}
		if(data==null || data.isEmpty()){
			data = super.captureObjectData();
		}
		
		return data;
	}
}
