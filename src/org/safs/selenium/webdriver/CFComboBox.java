
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
/**
 *
 * History:<br>
 *
 *  DEC 18, 2013    (Lei Wang) Initial release.
 *  JAN 16, 2014    (Lei Wang) Add keywords support.
 *  OCT 30, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 *  NOV 02, 2018    (Lei Wang) Modified method waitReady(): call super-class's waitReady(), if web-element is already ready then we will not wait here.
 */
package org.safs.selenium.webdriver;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.ComboBoxFunctions;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.ComboBoxException;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;
import org.safs.tools.CaseInsensitiveFile;

public class CFComboBox extends CFComponent {

	/** "ComboBox" */
	public static final String LIBRARY_NAME = CFComboBox.class.getSimpleName().substring("CF".length());

	protected ComboBox combobox = null;

	public CFComboBox() {
		super();
	}

	@Override
	protected ComboBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new ComboBox(webelement);
	}

	@Override
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String param1 = "";
			String param2 = "";
			String msg = "";
			String detail = "";
			String label = "";
			Iterator<?> iterator = null;
			boolean originalRefreshStatus = false;

			try{
				super.localProcess();
				combobox = (ComboBox) libComponent;

				Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				if(action.equalsIgnoreCase(ComboBoxFunctions.SELECT_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.SELECTINDEX_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.SELECTPARTIALMATCH_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.SELECTUNVERIFIEDPARTIALMATCH_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.SELECTUNVERIFIED_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.VERIFYSELECTED_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.CAPTUREITEMSTOFILE_KEYWORD)||
				   action.equalsIgnoreCase(ComboBoxFunctions.SETTEXTVALUE_KEYWORD)){

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

				originalRefreshStatus = combobox.getForceRefresh();
				if(!param2.isEmpty()){
					combobox.setForceRefresh(Boolean.parseBoolean(param2));
				}

				if(action.equalsIgnoreCase(ComboBoxFunctions.SELECT_KEYWORD)){
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

				}else if(action.equalsIgnoreCase(ComboBoxFunctions.SELECTINDEX_KEYWORD)){
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

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.SELECTPARTIALMATCH_KEYWORD)){
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

				}else if(action.equalsIgnoreCase(ComboBoxFunctions.SELECTUNVERIFIED_KEYWORD)){
					combobox.select(param1, false, false, true);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3A, windowName+":"+compName+" "+action+" successful using "+param1,
							                  windowName, compName, action, param1);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.SELECTUNVERIFIEDPARTIALMATCH_KEYWORD)){
					combobox.select(param1, false, true, true);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3A, windowName+":"+compName+" "+action+" successful using "+param1,
							                  windowName, compName, action, param1);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.VERIFYSELECTED_KEYWORD)){
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

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.HIDELIST_KEYWORD)){
					combobox.hidePopup();
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3, windowName+":"+compName+" "+action+" successful.",
							                  windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.SHOWLIST_KEYWORD)){
					combobox.showPopup();
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_3, windowName+":"+compName+" "+action+" successful.",
							                  windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.CAPTUREITEMSTOFILE_KEYWORD)){
					captureItemsToFile(param1, param2);

				} else if(action.equalsIgnoreCase(ComboBoxFunctions.SETTEXTVALUE_KEYWORD)){
					doSetText(LIBRARY_NAME, false, true);
				}
			}
			catch(SAFSException e){
				Log.error(debugmsg+"Selenium ComboBox Error processing parameters. ", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+e.getMessage();
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
			finally {
				combobox.setForceRefresh(originalRefreshStatus);
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

	@Override
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

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);
		if(!ready){
			//According to the actions, we will wait in different ways
			if(ComboBoxFunctions.CAPTUREITEMSTOFILE_KEYWORD.equals(action)){
				//What should we wait for?
				//For HTML combo-box, we try to get the 'option' tags;
				//readyElement = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.tagName("option"));

				//For SAP, DOJO combo-box, we call their JS APIs to get contents.
				readyElement = waiter.until(ExpectedConditions.visibilityOf(element));
				ready = true;
			}else{
				//For other actions, we wait the component to be click-able
				readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
				ready = true;
			}
		}

		return readyElement;
	}
}
