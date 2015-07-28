/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSParamException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.robot.Robot;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.stringutils.StringUtilities;

import com.thoughtworks.selenium.SeleniumException;

/**
 * <br><em>Purpose:</em> HTMLTable Functions for Selenium
 *      
 * @author  Carl Nagle	
 * @since   Mar 07, 2007
 *
 *   <br>   Mar 07, 2007    (Carl Nagle)    Original Release
 *   <br>   Jul 19, 2011    (LeiWang) 	Update and add some keywords.
 **/
public class CFHTMLTable extends CFComponent {

	public static final String ACTIVATECELL 			= "ActivateCell";
	public static final String ASSIGNVARIABLECELLTEXT	= "AssignVariableCellText";
	public static final String CLICKCELL 				= "ClickCell";
	public static final String DOUBLECLICKCELL 			= "DoubleClickCell";
	public static final String SELECTCELLTEXTSPECIAL 	= "SelectCellTextSpecial"; //NOT YET IMPLEMENTED
	public static final String VERIFYCELLEDITABLE 		= "VerifyCellEditable";
	public static final String VERIFYCELLTEXT 			= "VerifyCellText";
	public static final String VERIFYCELLTEXTCONTAINS 	= "VerifyCellTextContains";
	
	public static final String SELECTCELL				= "SelectCell";
	public static final String CAPTURERANGETOFILE		= "CaptureRangeToFile";
	public static final String CAPTUREFUZZYRANGETOFILE  = "CaptureFuzzyRangeToFile";
	public static final String ASSIGNCELLCONTAINSTEXTROW= "AssignCellContainsTextRow";
	public static final String ASSIGNCELLTEXTROW		= "AssignCellTextRow";
	
	public static final String RIGHTCLICKCELL				= "RightClickCell";
	public static final String SELECTCELLCONTAINSTEXTFIND	= "SelectCellContainsTextFind";
	public static final String SELECTCELLTEXT				= "SelectCellText";
	public static final String SELECTCELLTEXTFIND			= "SelectCellTextFind";
	public static final String SELECTFUZZYCELLTEXT			= "SelectFuzzyCellText";
	public static final String SELECTFUZZYCELLTEXTFIND		= "SelectFuzzyCellTextFind";
	
	public static final String VERIFYCELLTEXTFIND			= "VerifyCellTextFind";
	public static final String VERIFYCELLVALUE				= "VerifyCellValue";
	public static final String VERIFYCOLUMNLABEL			= "VerifyColumnLabel";
	public static final String VERIFYFUZZYCELLTEXT			= "VerifyFuzzyCellText";
	public static final String VERIFYFUZZYCELLTEXTCONTAINS	= "VerifyFuzzyCellTextContains";
	public static final String VERIFYFUZZYCELLTEXTFIND		= "VerifyFuzzyCellTextFind";
	
	protected String msg;
	protected String detail;
	private String[][] tableContents = null;
	
	public CFHTMLTable() {
		super();
	}
	
	protected void localProcess() {
		try {
			if (action == null)
				return;
			tableContents=null;

			if (action.equalsIgnoreCase(CLICKCELL) || 
				action.equalsIgnoreCase(ACTIVATECELL) || 
				action.equalsIgnoreCase(DOUBLECLICKCELL) || 
				action.equalsIgnoreCase(SELECTCELL) ||
				action.equalsIgnoreCase(RIGHTCLICKCELL) ||
				action.equalsIgnoreCase(SELECTCELLTEXT) ||
				action.equalsIgnoreCase(SELECTFUZZYCELLTEXT)) {
				doClicks();
			} else if (action.equalsIgnoreCase(VERIFYCELLTEXT) || 
					   action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS) || 
					   action.equalsIgnoreCase(ASSIGNVARIABLECELLTEXT)) {
				doCellText();
			} else if (action.equalsIgnoreCase(VERIFYCELLEDITABLE)) {
				doCellEditable();
			} else if (action.equalsIgnoreCase(SELECTCELLTEXTSPECIAL)) {

			} else if (action.equalsIgnoreCase(CAPTURERANGETOFILE)) {
				commandCaptureRangeToFile(false);
			}else if(action.equalsIgnoreCase(CAPTUREFUZZYRANGETOFILE)){
				commandCaptureRangeToFile(true);
			}else if(action.equalsIgnoreCase(ASSIGNCELLTEXTROW)){
				commandAssignCellTextRow(false);
			}else if(action.equalsIgnoreCase(ASSIGNCELLCONTAINSTEXTROW)){
				commandAssignCellTextRow(true);
			}else if(action.equalsIgnoreCase(SELECTCELLCONTAINSTEXTFIND) ||
					 action.equalsIgnoreCase(SELECTFUZZYCELLTEXTFIND)){
				commandClickFind(true);
			}else if(action.equalsIgnoreCase(SELECTCELLTEXTFIND)){
				commandClickFind(false);
			}else if(action.equalsIgnoreCase(VERIFYCELLTEXTFIND)){
				commandVerifyCellTextFind(false);
			}else if(action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTFIND)){
				commandVerifyCellTextFind(true);
			}else if(action.equalsIgnoreCase(VERIFYCELLVALUE)){
				commandVerifyCellValue();
			}else if(action.equalsIgnoreCase(VERIFYFUZZYCELLTEXT)){
				commandVerifyFuzzyCellText(false);
			}else if(action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTCONTAINS)){
				commandVerifyFuzzyCellText(true);
			}else if(action.equalsIgnoreCase(VERIFYCOLUMNLABEL)){
				commandVerifyColumnLabel();
			}
		} catch (SAFSException ex) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), "SAFSException: "
					+ ex.getMessage(), FAILED_MESSAGE);
		}
	}
	
	protected Rectangle getTableCellLocation(int row, int column) throws SAFSException{
		Rectangle compRect = null;
		
		if(compObject==null){
			Log.error("Component object is null.");
			throw new SAFSException("Component object is null.");
		}
		
		String cellXpath = recRowCol(compObject.getLocator(), row, column);
		Log.debug("Table cell("+row+","+column+") , its xpath is "+cellXpath);
		
		compRect = getComponentBounds(cellXpath);
		
		return compRect;
	}
	
	protected void doClicks(){
		int irow = 1;
		int icol = 1;
		boolean fuzzy = false;
		String coord = null;
		
		if(action.equalsIgnoreCase(SELECTFUZZYCELLTEXT)){
			fuzzy = true;
		}

		try{
			Iterator iter = params.iterator();
			
			irow = paramRow(iter,fuzzy);
			icol = paramColumn(iter,fuzzy);
			
			coord = " "+ String.valueOf(irow) +", "+ String.valueOf(icol);
			
			Rectangle compRect = getTableCellLocation(irow, icol);
			Point cellPoint = new Point((int)compRect.getCenterX(),(int)compRect.getCenterY());
			
			if(action.equalsIgnoreCase(CLICKCELL) ||
			   action.equalsIgnoreCase(SELECTCELL)||
			   action.equalsIgnoreCase(SELECTCELLTEXT) ||
			   action.equalsIgnoreCase(SELECTFUZZYCELLTEXT)){
				Robot.click(cellPoint);
			}else if(action.equalsIgnoreCase(ACTIVATECELL) ||
					 action.equalsIgnoreCase(DOUBLECLICKCELL)){
				Robot.doubleClick(cellPoint);
			}else if(action.equalsIgnoreCase(RIGHTCLICKCELL)){
				Robot.rightClick(cellPoint);
			}
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			msg = genericText.convert("success3", windowName +":"+compName +" "+ action + coord + " successful.",
                                      windowName, compName, action + coord);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
		}catch(SAFSParamException pe){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = getStandardErrorMessage(action + pe.getMessage());
			log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);	
		}catch(Exception e){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = getStandardErrorMessage(action + coord);
			log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);			
		}		
	}
	
	protected void doCellText(){
		String bench = "";
		String srow = ""; 
		String scol = "";
		int irow = 1;
		int icol = 1;
		try{ bench = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(4)); }
		catch(Exception np){;}
		if((action.equalsIgnoreCase(ASSIGNVARIABLECELLTEXT))||
		   (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS))){
		   	if (bench.length()==0){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		   		srow = action +" '"+ bench +"'";
				msg = failedText.convert("bad_parameter","Invalid parameter value for "+ srow, srow);			
				log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);
				return;			
		   	}
		}
		
		try{ 
			srow = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(5));
			irow = Integer.parseInt(srow);
			if (irow < 1) {
				irow = 1;
				throw new NumberFormatException();
			} 
		}
		catch(NumberFormatException np){
			msg = failedText.convert("ignore_bad_param", action +" ignoring invalid parameter 'ROW:"+ srow +"'.",
									 action, "ROW:"+srow);			
			log.logMessage(testRecordData.getFac(), msg, WARNING_MESSAGE);			
		}
		catch(Exception np){;}
		try{ 
			scol = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(6));
			icol = Integer.parseInt(scol);
			if (icol < 1) {
				icol = 1;
				throw new NumberFormatException();
			} 
		}
		catch(NumberFormatException np){
			msg = failedText.convert("ignore_bad_param", action +" ignoring invalid parameter 'COL:"+ scol +"'.",
									 action, "COL:"+scol);
			log.logMessage(testRecordData.getFac(), msg, WARNING_MESSAGE);			
		}
		catch(Exception np){;}
		String rec = "";
		String text = "";
		String coord = " "+ String.valueOf(irow) +", "+ String.valueOf(icol);
		try{			
			rec = recRowCol(sHelper.getCompTestObject().getLocator(), irow, icol);
			text = selenium.getText(rec);
			boolean success = false;
			if (action.equalsIgnoreCase(VERIFYCELLTEXT)){
				if(text.equals(bench)){
					success = true;
					detail = genericText.convert("equals", "'"+ text +"'"+ " equals '"+ bench +"'",
												   text, bench);
				}else{
					detail = genericText.convert("not_equal", "'"+ text +"'"+ " does not equal '"+ bench +"'",
												 text, bench);
				}
			}else
			if (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS)){
				if(text.indexOf(bench)> -1){
					success = true;
					detail = genericText.convert("contains", "'"+ text +"'"+ " contains '"+ bench +"'",
												   text, bench);
				}else{
					detail = genericText.convert("not_contain", "'"+ text +"'"+ " does not contain '"+ bench +"'",
												 text, bench);
				}
			}else
			if (action.equalsIgnoreCase(ASSIGNVARIABLECELLTEXT)){
				try{
					setVariable(bench, text);
					success = true;
					detail = genericText.convert("something_set","'"+ bench +"' set to '"+ text +"'",
												   bench, text);
				}catch(Exception x){
					detail = genericText.convert("could_not_set","Could not set '"+ bench +"' to '"+ text +"'",
												   bench, text);
				}
			}
			
			if(success){			
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				msg = genericText.convert("success4", windowName +":"+compName +" "+ action + coord +
										  " successful. "+ detail,
										  windowName, compName, action + coord, detail);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			}else{
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(action + coord);
				log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
			}
		}
		catch(Exception e){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = getStandardErrorMessage(action + coord);
			log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);			
		}		
	}

	protected void doCellEditable(){
		String srow = ""; 
		String scol = "";
		String sedit = "";
		int irow = 1;
		int icol = 1;
		boolean btest = false;
		boolean bedit = false;
		
		try{ 
			srow = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(4));
			irow = Integer.parseInt(srow);
			if (irow < 1) {
				irow = 1;
				throw new NumberFormatException();
			} 
		}
		catch(NumberFormatException np){
			msg = failedText.convert("ignore_bad_param", action +" ignoring invalid parameter 'ROW:"+ srow +"'.",
									 action, "ROW:"+srow);			
			log.logMessage(testRecordData.getFac(), msg, WARNING_MESSAGE);			
		}
		catch(Exception np){;}
		try{ 
			scol = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(5));
			icol = Integer.parseInt(scol);
			if (icol < 1) {
				icol = 1;
				throw new NumberFormatException();
			} 
		}
		catch(NumberFormatException np){
			msg = failedText.convert("ignore_bad_param", action +" ignoring invalid parameter 'COL:"+ scol +"'.",
									 action, "COL:"+scol);
			log.logMessage(testRecordData.getFac(), msg, WARNING_MESSAGE);			
		}
		catch(Exception np){;}
		try{ 
			sedit = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(6));
			btest = StringUtilities.convertBool(sedit);
		}
		catch(Exception np){;}
		String coord = " "+ String.valueOf(irow) +", "+ String.valueOf(icol);
		try{
			bedit = selenium.isEditable(recRowCol(sHelper.getCompTestObject().getLocator(), irow, icol));
			if(bedit == btest){			
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				detail = genericText.convert("equals", "'"+ String.valueOf(bedit) +"'"+ " equals '"+ String.valueOf(btest) +"'",
											   String.valueOf(bedit), String.valueOf(btest));
				msg = genericText.convert("success4", windowName +":"+compName +" "+ action + coord +
										  " successful. "+ detail,
										  windowName, compName, action + coord, detail);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			}
			else{
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				detail = genericText.convert("not_equal", "'"+ String.valueOf(bedit) +"'"+ " does not equal '"+ String.valueOf(btest) +"'",
											   String.valueOf(bedit), String.valueOf(btest));
				msg = getStandardErrorMessage(action + coord);
				log.logMessage(testRecordData.getFac(), msg, detail, FAILED_MESSAGE);
			}
		}
		catch(Exception e){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = getStandardErrorMessage(action + coord);
			log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);			
		}		
	}
	
	/**
	 * <em>Note:</em>      This method only works for the simplest table<br>
	 *                     If the table is composite with another table and <br>
	 *                     the other table has header <TH>, this method can't<br>
	 *                     detect it.
	 * @param tableXpath
	 * @return
	 */
	protected boolean hasHeader(String tableXpath){
		String debugmsg = getClass().getName()+".tableHasHeader(): ";
		boolean tableHasHeader = false;

		Log.debug(debugmsg+" table's xpath is "+tableXpath);
		tableXpath = normalizeTableXpath(tableXpath);

		String headerXpath = tableXpath+"/TR[1]/TH[1]";
		try{
			tableHasHeader = selenium.isElementPresent(headerXpath);
		}catch(Exception e){
			Log.debug("Exception occur: "+e.getMessage()+" Header "+headerXpath+" doesn't exist.");
		}
		
		return tableHasHeader;
	}
	
	/**
	 * @param rec	The table xpath
	 * @param row   The row number, begin from 1
	 * @param col   The column number, begin from 1
	 * @return      The xpath for the table cell indicated by row and column
	 */
	protected String recRowCol(String rec, int row, int col){
		String cellRec = null;
		String debugmsg = getClass().getSimpleName()+".recRowCol(): ";
		Log.debug(debugmsg+"Table recognition string is "+rec);
		
		rec = normalizeTableXpath(rec);
		
		if(row<1){
			Log.warn(debugmsg+"Row number is smaller than 1, it is wrong, reset it to 1");
			row=1;
		}
		if(col<1){
			Log.warn(debugmsg+"Column number is smaller than 1, it is wrong, reset it to 1");
			col=1;			
		}
		
		if(hasHeader(rec)){
//			cellRec = rec +"/TR["+ String.valueOf(row) +"]/TH["+ String.valueOf(col) +"]";
			//If table has header, the first row is header, we add 1 to the row number
			cellRec = rec +"/TR["+ String.valueOf(row+1) +"]/TD["+ String.valueOf(col) +"]";
		}else{
			cellRec = rec +"/TR["+ String.valueOf(row) +"]/TD["+ String.valueOf(col) +"]";			
		}
		
		Log.debug(debugmsg+"The table cell("+row+","+col+") xpath is "+cellRec);
		
		return cellRec;
	}
	
	/**
	 * 
	 * @param tableXpath   The xpath representing the table.
	 * @return             A list of header name, if the list is empty,<br>
	 *                     this means that this table has no headers.<br>
	 */
	protected List<String> getHeaders(String tableXpath){
		String debugmsg = getClass().getName()+".getHeaders() ";
		List<String> headers = new ArrayList<String>();
		Log.debug(debugmsg+" table's xpath is "+tableXpath);
		tableXpath = normalizeTableXpath(tableXpath);
		
		String headerXpath = null;
		String headerName = null;
		int i = 1;
		try{
			if(hasHeader(tableXpath)){
				while(true){
					headerXpath = tableXpath+"/TR[1]/TH["+(i++)+"]";
					if(selenium.isElementPresent(headerXpath)){
						headerName = selenium.getText(headerXpath);
						headers.add(headerName);						
					}else{
						Log.debug(debugmsg+headerXpath+" doesn't exist, break loop.");
						break;
					}
				}
			}
		}catch(Exception e){
			Log.debug("Exception occur: "+e.getMessage()+" Header "+headerXpath+" doesn't exist.");
		}
		
		return headers;
	}
	
	/**
	 * 
	 * @param tableXpath  The xpath representing the table
	 * @return            The number of rows contained by the table,<br>
	 *                    if the table contains a row of header, that row<br>
	 *                    will not be counted.
	 */
	protected int getRowCount(String tableXpath){
		int rowcount = 0;
		int beginRow = 0;
		String headerXpath = null;
		
		try{
			tableXpath = normalizeTableXpath(tableXpath);
			
			if(hasHeader(tableXpath)){
				beginRow = 1;				
			}
			
			while(true){
				headerXpath = tableXpath+"/TR["+(++rowcount+beginRow)+"]/TD[1]";
				if(!selenium.isElementPresent(headerXpath)){
					rowcount--;
					break;
				}
			}
		}catch(Exception e){
			Log.debug("Exception occur: "+e.getMessage()+" Header "+headerXpath+" doesn't exist.");
		}
		
		return rowcount;
	}
	
	/**
	 * 
	 * @param tableXpath  The xpath representing the table
	 * @return            The number of columns contained by the table
	 */	
	protected int getColumnCount(String tableXpath){
		int columncount = 0;
		String headerXpathPrefix = null;
		String headerXpath = null;
		
		try{
			tableXpath = normalizeTableXpath(tableXpath);
			
			if(hasHeader(tableXpath)){
				headerXpathPrefix = tableXpath+"/TR[1]/TH[";				
			}else{
				headerXpathPrefix = tableXpath+"/TR[1]/TD[";				
			}
			
			while(true){
				headerXpath = headerXpathPrefix+(++columncount)+"]";
				if(!selenium.isElementPresent(headerXpath)){
					columncount--;
					break;
				}
			}
		}catch(Exception e){
			Log.debug("Exception occur: "+e.getMessage()+" Header "+headerXpath+" doesn't exist.");
		}
		
		return columncount;
	}
	
	/**
	 * 
	 * @param tableXpath  The xpath representing the table.
	 * @return            An array containing the table contents without header.
	 */
	protected String[][] getTableContents(String tableXpath){
		String debugmsg = getClass().getName()+".getTableContents() ";
		String cellXpath = null;
		
		if(tableContents!=null) return tableContents;
		
		int rowcount = getRowCount(tableXpath);
		int columncount = getColumnCount(tableXpath);
		String[][] contents = new String[rowcount][columncount];
		
		for(int i=0;i<rowcount;i++){
			for(int j=0;j<columncount;j++){
				try{
					cellXpath = recRowCol(tableXpath, i+1,j+1);
					contents[i][j] = selenium.getText(cellXpath);
				}catch(SeleniumException e){
					Log.warn(debugmsg+" cell "+cellXpath+" doesn't exist.");
					contents[i][j] = "";
				}
			}
		}
		tableContents = contents;
		
		return contents;
	}
	
	/**
	 * <em>Note:</em>     If the talbe's xpath does not end with '/TBODY', append '/TBODY' to it.<br>
	 * @param tableXpath
	 * @return
	 */
	private String normalizeTableXpath(String tableXpath){
		String debugmsg = getClass().getSimpleName()+".normalizeTableXpath(): ";
		Log.debug(debugmsg+" table's xpath is "+tableXpath);
		if(!tableXpath.toLowerCase().endsWith("/tbody")){
			tableXpath = tableXpath+"/TBODY";
		}
		return tableXpath;
	}
	
	/**
	 * 
	 * @param row             The beginning row number
	 * @param column          The beginning column number or the beginning column header name
	 * @param rows            The number of rows to capture
	 * @param columns         The number of columns to capture
	 * @param fuzzy           If it is true, the header will be matched partially and ignoring case
	 * @return                A list of rows
	 * @throws SAFSException
	 */
	protected List<String> getObjectData(String row, String column, int rows, int columns, boolean fuzzy) throws SAFSException{
		String debugmsg = getClass().getName()+".getObjectData() ";
		List<String> contents = new ArrayList<String>();
		int startRow = 0;
		int startColumn = 0;
		int endRow = 0;
		int endColumn = 0;
		
		if(compObject==null){
			Log.error(debugmsg+" table object should not be null.");
			throw new SAFSException(" Table Object is null");
		}
		
		Log.debug(debugmsg+" row="+row+" column="+column+" rows="+rows+" columns="+columns);
		String tableXpath = normalizeTableXpath(compObject.getLocator());
		
		try{
			startRow = Integer.parseInt(row);
			startRow = startRow-1;
		}catch(NumberFormatException e){
			Log.warn(debugmsg+": "+row+ " is not an Integer.");
		}
		
		try{
			startColumn = Integer.parseInt(column);
			startColumn = startColumn-1;
		}catch(NumberFormatException e){
			Log.debug(debugmsg+": "+column+ " is not an Integer.");
			List<String> headers = getHeaders(tableXpath);
			String header = null;
			for(int i=0;i<headers.size();i++){
				header = headers.get(i);
				if(fuzzy){
					if(header.toLowerCase().indexOf(column.toLowerCase())>-1){
						startColumn = i;
						break;
					}
				}else{
					if(header.equals(column)){
						startColumn = i;
						break;			
					}
				}
			}
		}
		Log.debug(debugmsg+" start_row="+startRow+" start_col="+startColumn);
		
		String[][] tableContents = getTableContents(tableXpath);
		int totalRowCount = tableContents.length;
		int totalColumnCount = totalRowCount>0? tableContents[0].length:0;
		if(rows==-1){
			endRow = totalRowCount;
		}else{
			endRow= (startRow+rows)>totalRowCount? totalRowCount:(startRow+rows);
		}
		if(columns==-1){
			endColumn = totalColumnCount;
		}else{
			endColumn = (startColumn+columns)>totalColumnCount? totalColumnCount:(startColumn+columns);
		}
		Log.debug(debugmsg+" end_row="+endRow+" end_col="+endColumn);
		
		String rowString = "";
		for(int i=startRow;i<endRow;i++){
			rowString = "";
			for(int j=startColumn;j<endColumn;j++){
				rowString += tableContents[i][j]+"\t";
			}
			contents.add(rowString);
		}
		
		return contents;
	}
	/**
	 * 
	 * @param row             The beginning row number
	 * @param column          The beginning column number or the beginning column header name
	 * @param rows            The number of rows to capture
	 * @param columns         The number of columns to capture
	 * @param fuzzy           If it is true, the header will be matched partially and ignoring case
	 * @param filename        The file to which the table content will be written
	 * @param encoding        The encoding to be used to write a file.
	 * @throws SAFSException
	 */
	protected void actionCaptureRangeToFile (String row, String column, int rows, int columns,
			                                 boolean fuzzy, String filename, String encoding) throws SAFSException {

		List<String> list = getObjectData(row,column, rows, columns, fuzzy);

		try {
			File file = new CaseInsensitiveFile(filename).toFile();
		    if (!file.isAbsolute()) {
		    	String testdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
				if (testdir != null) {
					file = new CaseInsensitiveFile(testdir, filename).toFile();
					filename = file.getAbsolutePath();
				}
		    }
		    Log.info("Writing to file: "+filename);
		    StringUtils.writeEncodingfile(filename, list,encoding);

		} catch (java.io.IOException e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		    log.logMessage(testRecordData.getFac(),
					 getClass().getName()+": io exception: "+e.getMessage(),
					 FAILED_MESSAGE);
		    return;
		}
		// set status to ok
		String altText = "write file ok: "+filename+", "+windowName+":"+compName+" "+action;
		log.logMessage(testRecordData.getFac(),
			    passedText.convert(PRE_TXT_SUCCESS_4, altText, filename, windowName, compName, action),
			    PASSED_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);
	}
	
	protected void commandCaptureRangeToFile (boolean fuzzy) throws SAFSException {
		String debugmsg = getClass().getName()+".commandCaptureRangeToFile() ";
		String row = null;
		String column = null;
		int rows = -1;
		int columns = -1;
	    String fileEncoding = null;
	    String filename = null;
	    
	    if (params.size() < 1) {
	    	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    	paramsFailedMsg(windowName, compName);
	    	return;
	    }

			Iterator piter = params.iterator();
			filename = (String) piter.next();

			// second param will be the row number or contents
			if (piter.hasNext()) {
				row = (String) piter.next();
			}
			// third param will be the col number, or col name
			if (piter.hasNext()) { // col
				column = (String) piter.next();
			}
			
			// fourth param will be the rows
			if (piter.hasNext()) { // rows
				String next = (String) piter.next();
				if (!next.trim().equals("")) {
					try {
						rows = Integer.parseInt(next);
						if (rows <= 0) {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), action+ ": rows cannot be less than 1: " + next, FAILED_MESSAGE);
							return;
						}
					} catch (NumberFormatException ee) {
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						log.logMessage(testRecordData.getFac(), action + ": invalid rows number format: " + ee.getMessage(), FAILED_MESSAGE);
						return;
					}
				}
			}
			// fifth param will be the cols
			if (piter.hasNext()) { // cols
				String next = (String) piter.next();
				if (!next.trim().equals("")) {
					try {
						columns = new Integer(next);

						if (columns <= 0) {
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							log.logMessage(testRecordData.getFac(), action + ": cols cannot be less than 1: " + next, FAILED_MESSAGE);
							return;
						}
					} catch (NumberFormatException ee) {
						testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
						log.logMessage(testRecordData.getFac(), action + ": invaldi cols number format: " + ee.getMessage(), FAILED_MESSAGE);
						return;
					}
				}
			}
			// file encoding
			if (piter.hasNext()) {
				fileEncoding = (String) piter.next();
				// If user put a blank string as encoding,
				// we should consider that user does NOT provide a encoding,
				// reset encoding to null.
				fileEncoding = "".equals(fileEncoding.trim()) ? null : fileEncoding;
			}
			
			Log.info("...filename: " + filename + " ; encoding:"+ fileEncoding);
			// filter mode
			// if(piter.hasNext()){ piter.next();}
			// filter options
			// if(piter.hasNext()){ piter.next();}

			Log.info(debugmsg+ ": Row: " + row + ", Col: " + column + ", rows: " + rows + ", columns: " + columns
					         + "  ...filename: " + filename + " ; encoding: "+ fileEncoding);
			actionCaptureRangeToFile(row, column, rows, columns, fuzzy, filename, fileEncoding);

	}
	/**
	 * 
	 * @param fuzzy   If it is true, the header will be matched partially and ignoring case.
	 * @param column  The column header to match.
	 * @return        The matched column's index, begin from 1.<br>
	 *                If there is no matched column, return -1.
	 */
	protected int getColi(boolean fuzzy, String column){
		String debugmsg = getClass().getName()+".getColi() ";
		int columnIndex = -1;
		
		Log.debug(debugmsg+" fuzzy="+fuzzy+" looking for column index for header '"+column+"'");
		try{
			List<String> headers = getHeaders(compObject.getLocator());
			for(int i=0;i<headers.size();i++){
				if(fuzzy){
					if(headers.get(i).toLowerCase().indexOf(column.toLowerCase())>-1){
						columnIndex = i+1;
						break;
					}
				}else{
					if(headers.get(i).equals(column)){
						columnIndex = i+1;
						break;
					}
				}
			}
		}catch(Exception e){
			Log.error(debugmsg+" Exception occur: "+e.getMessage()+"; Can't get column index!");
		}
		
		return columnIndex;
	}
	
	/**
	 * 
	 * @param fuzzy    If it is true, the header will be matched partially and ignoring case.
	 * @param rowvals  A list of list of pair (Integer columnIndex, String value)
	 * @return         The matched row's index, begin from 1.<br>
	 *                 If there is no matched row, return -1.
	 */
	protected int getRowi(boolean fuzzy, List rowvals){
		String debugmsg = getClass().getName()+".() ";
		int rowIndex = -1;
		
		Log.debug(debugmsg+" search condition: fuzzy="+fuzzy+"; rowvals="+rowvals);
		
		try{
			String[][] contents = getTableContents(compObject.getLocator());
			List columnAndValue = null;
			int column = 0;
			String value = null;
			String cellValue = null;
outer:		for(int row=0;row<contents.length;row++){
				for(int i=0;i<rowvals.size();i++){
					columnAndValue = (List) rowvals.get(i);
					column = ((Integer) columnAndValue.get(0)).intValue()-1;
					value = (String) columnAndValue.get(1);
					cellValue = contents[row][column];
					if(fuzzy){
						if(! (cellValue.toLowerCase().indexOf(value.toLowerCase())>-1) ){
							continue outer;//Try next row
						}
					}else{
						if(!cellValue.equals(value)){
							continue outer;//Try next row
						}
					}
					if(i==(rowvals.size()-1)){
						rowIndex = row+1;
						break outer;
					}
				}
			}
		}catch(Exception e){
			Log.error(debugmsg+"Exception occur: "+e.getMessage()+", can't get matched row number.");
		}
		
		return rowIndex;
	}
	
	/**
	 * 
	 * @param row     Row number, begin from 1.
	 * @param column  Column number, begin from 1.
	 * @return        The text of cell(row, column)
	 */
	protected String getCellText(int row, int column){
		String cell = null;
		
		try{
			String[][] contents = getTableContents(compObject.getLocator());
			cell = contents[row-1][column-1];
		}catch(Exception e){
			Log.error("Exception occur: "+e.getMessage()+" ; Can't get cell text!");
		}
		
		return cell;
	}
	
	/**
	 * <em>Note:</em>               Consume one object from parameter 'iter'.<br>
	 *                              This object is a variable name, it will be initialized to "".<br>
	 * @param iter                  A Iterator, contains the parameters of SAFS's keyword
	 * @return                      The first object got from 'iter', the variable name to store 'row index'.<br>
	 * @throws SAFSException
	 * @throws SAFSParamException   
	 */
	protected String paramRowIndexVariable(Iterator iter) throws SAFSException, SAFSParamException{
		String rowIndexVariable = null;
		
		if (iter.hasNext()) {
			rowIndexVariable = (String) iter.next();
			Log.debug("parameter rowIndexVariable="+rowIndexVariable);
			// Initialize the variable to "", then later, if we find the cell,do the real value
			if (!setVariable(rowIndexVariable, "")) {
				String detail = " STAF: SetVariable failure, var: " + rowIndexVariable + ", val: ''";
				throw new SAFSParamException(detail);
			}
		} else {
			throw new SAFSParamException("Can't retrieve the parameter 'rowIndexVariable'");
		}
		
		return rowIndexVariable;
	}
	
	/**
	 * <em>Note:</em>               Consume one object from parameter 'iter'.<br>
	 *                              This object is a 'row number' or a 'row header'.<br>
	 *                              'row header' is the value of the cell from first column<br>
	 *                              
	 * @param iter                  A Iterator, contains the parameters of SAFS's keyword
	 * @param fuzzy                 boolean, if the comparison is case-sensitive
	 * @return                      The row number represent by the first object from 'iter'<br>
	 * @throws SAFSParamException
	 */
	protected int paramRow(Iterator iter, boolean fuzzy) throws SAFSParamException{
		int row = 0;
		
		if(iter.hasNext()){
			String next = (String) iter.next();
			Log.debug("Row number or first column value is "+next);
			if (!next.trim().equals("")) {
				Integer number = convertNum(next);
				if (number == null) { // assume it is a row value of the first column
					Log.debug(" parameter ROW:" + next+" is not a number.");
					List rowvals = new ArrayList();
					List rowvale = new ArrayList();
					rowvale.add(new Integer(1));
					rowvale.add(next);
					rowvals.add(rowvale);
					row = getRowi(fuzzy,rowvals);
					Log.info(" row header(first column value): " + next);
				} else {
					row = number.intValue();
				}
			}
			Log.info(" row index: "+ row);
			
			if (row==-1) {
				String detail = " Can't find row header '"+next+"'";
				throw new SAFSParamException(detail);
			}
		}else{
			throw new SAFSParamException("Can't retrieve the parameter 'row'");
		}
		
		return row;
	}
	
	/**
	 * <em>Note:</em>               Consume one object from parameter 'iter'.<br>
	 *                              This object is a 'column number' or a 'column header'.<br>
	 *                              
	 * @param iter                  A Iterator, contains the parameters of SAFS's keyword
	 * @param fuzzy                 boolean, if the comparison is case-sensitive
	 * @return                      The column number represent by the first object from 'iter'<br>
	 * @throws SAFSParamException
	 */
	protected int paramColumn(Iterator iter, boolean fuzzy) throws SAFSParamException{
		int col = 0;
		
		if(iter.hasNext()){
			String next = (String) iter.next();
			Log.debug("Column number or column header is "+next);
			if (!next.trim().equals("")) {
				Integer number = convertNum(next);
				if (number == null) { // assume it is a column name
					col = getColi(fuzzy, next);
				} else {
					col = number.intValue();
				}
			}
			Log.info(" column header: " + next + " column index: "+ col);
			
			if (col==-1) {
				String detail = " Can't find column header '"+next+"'";
				throw new SAFSParamException(detail);
			}
		}else{
			throw new SAFSParamException("Can't retrieve the parameter 'column'");
		}
		
		return col;
	}
	
	/**
	 * <em>Note:</em>               Consume objects from parameter 'iter'.<br>
	 *                              These objects are pairs of ('column number/column header', 'cell value').<br>
	 *                              These pairs are used as condition to match a row.
	 *                              
	 * @param iter                  A Iterator, contains the parameters of SAFS's keyword
	 * @param fuzzy                 boolean, if the comparison is case-sensitive
	 * @return                      The 'row number' matching the pairs of ('column number/column header', 'cell value')
	 * @throws SAFSParamException
	 */
	protected int paramRowFromColumns(Iterator iter, boolean fuzzy) throws SAFSParamException{
		String debugmsg = getClass().getName()+".paramRowFromColumns() ";
		
		// Additional param pairs will be like: {{colname/num},{cell value}}, {{colname/num},{cell value}} ...
		// According these parameters to search the row number
		List rowvals = new ArrayList();
		while (iter.hasNext()) {
			// colname/num
			String tmp = (String) iter.next();
			Integer columnIndex = null;
			columnIndex = convertNum(tmp);
			if (columnIndex == null) { // assume it is a column name
				columnIndex = getColi(fuzzy, tmp);
			}

			// cell value
			if (iter.hasNext()) { // row
				String rowval = (String) iter.next();
				List rowvale = new ArrayList();
				rowvale.add(columnIndex);
				rowvale.add(rowval);
				rowvals.add(rowvale);
			}else{
				Log.warn(debugmsg+" For column "+tmp+", you miss giving a value!");
			}
		}
		
		Log.info(debugmsg + " Search condition: {column/value}* : "+ rowvals);
		int row = getRowi(fuzzy, rowvals);
		Log.info(debugmsg + " Found row number: " + row);
		if (row==-1) {
			throw new SAFSParamException(" Can't find a row matching condition "+rowvals);
		}
		
		return row;
	}
	
	/**
	 * <em>Note:</em>            This method is used to store a value to a SAFS Variable.<br>
	 *                           If it fails to do that, it will set status code to failure and<br>
	 *                           log failed message and return false.
	 * @param variable           The SAFS Variable to store value.
	 * @param value              The value to be stored.
	 * @return
	 * @throws SAFSException
	 */
	protected boolean setVariableLogWhenFail(String variable, String value) throws SAFSException{
		if(setVariable(variable,value)){
			return true;
		}else{
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String detail = " STAF: SetVariable failure, var: "+ variable + ", val:" + value;
			Log.debug(detail);
			componentFailureMessage(detail);
			return false;
		}
	}
	
	protected void commandAssignCellTextRow(boolean fuzzy) throws SAFSException {
		String debugmsg = getClass().getName() + ".commandAssignCellTextRow(): ";

		if (params.size() < 4) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			Integer ccol = new Integer(0);
			try{
				Iterator piter = params.iterator();
				// First param is the variable where the "found row number" will be stored
				String rowIndexVariable = paramRowIndexVariable(piter);
				if (rowIndexVariable==null) return;
	
				String cellValueVariable = rowIndexVariable + ".value";
				Log.info(debugmsg + " rowIndexVariable name: " + rowIndexVariable+ " , cellValueVariable name: " + cellValueVariable);
				
				// Second param will be the col number, or col name
				int col = paramColumn(piter,fuzzy);
				
				// Search the row number according to pairs of (column,value)
				int row = paramRowFromColumns(piter,fuzzy);
				Log.debug(debugmsg+action + " deduce Row: " + row + ", Col: " + col);
				
				// Try to set the "found row number" to the variable
				if (!setVariableLogWhenFail(rowIndexVariable, String.valueOf(row))) return;

				// Try to get the cell value and set it to variable.value
				String cellValue = getCellText(row, col);
				Log.debug(debugmsg + " Value of cell(" + row + "," +col+ "): "+ cellValue);
				if (!setVariableLogWhenFail(cellValueVariable, cellValue)) return;

				// Success, set the statusCode and log the success messag.
				testRecordData.setStatusCode(StatusCodes.OK);
				componentSuccessMessage(rowIndexVariable + "=" + row + "; "+ cellValueVariable + "=" + cellValue);
			}catch(SAFSParamException pe){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug(debugmsg + pe.getMessage());
				componentFailureMessage(pe.getMessage());
				return;
			}
		}
	}
	
	protected void commandClickFind(boolean fuzzy) throws SAFSException {
		String debugmsg = getClass().getName()+".commandClickFind() ";
		
		if (params.size() < 4) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			try{
				Iterator piter = params.iterator();
				// first param is the rowIndex variable
				String rowIndexVar = paramRowIndexVariable(piter);
				
				// Second param will be the col number, or col name
				int col = paramColumn(piter, fuzzy);
				
				// Search the row number according to pairs of (column,value)
				int row = paramRowFromColumns(piter, fuzzy);
				Log.debug(debugmsg+action + " deduce Row: " + row + ", Col: " + col);
	
				if (!setVariableLogWhenFail(rowIndexVar, String.valueOf(row)))  return;
	
				try{
					Rectangle cellLocation = getTableCellLocation(row, col);
					Robot.click((int)cellLocation.getCenterX(), (int)cellLocation.getCenterY());
				}catch(Exception e){
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					msg = getStandardErrorMessage(action+" "+e.getMessage());
					log.logMessage(testRecordData.getFac(), msg, testRecordData.getInputRecord(), FAILED_MESSAGE);			
				}
				
				testRecordData.setStatusCode(StatusCodes.OK);
				componentSuccessMessage(" row=" + row + "; "+ "column=" + col);
			}catch(SAFSParamException pe){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug(debugmsg + pe.getMessage());
				componentFailureMessage(pe.getMessage());
				return;
			}
		}
	}
	
	protected void commandVerifyCellTextFind(boolean fuzzy) throws SAFSException {
		String debugmsg = getClass().getName()+".commandVerifyCellTextFind() ";
		
		if (params.size() < 5) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			try{
				Iterator piter = params.iterator();
				//First parameter is the bench value
				String val = (String) piter.next();
				
				// Second param is the rowIndex variable
				String rowIndexVar = paramRowIndexVariable(piter);
				
				// Third param will be the col number, or col name
				int col = paramColumn(piter, fuzzy);
				
				// Search the row number according to pairs of (column,value)
				int row = paramRowFromColumns(piter, fuzzy);
				Log.debug(debugmsg+action + " deduce Row: " + row + ", Col: " + col);

				if (!setVariableLogWhenFail(rowIndexVar, String.valueOf(row))) return;
	
				actionVerify(row, col, null, fuzzy, val);
			}catch(SAFSParamException pe){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug(debugmsg + pe.getMessage());
				componentFailureMessage(pe.getMessage());
				return;
			}
		}
	}

	protected void commandVerifyCellValue() throws SAFSException{
		String debugmsg = getClass().getName()+".commandVerifyCellValue() ";
		
		if (params.size() < 1) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			try{
				int row = 1, column =1;
				Iterator piter = params.iterator();
				//First parameter is the bench value
				String val = (String) piter.next();
				
				//Second parameter is the cell location (row, column)
				if(piter.hasNext()){
					String cell = (String) piter.next();
					Point cellPoint = convertCoords(cell);
					Log.debug(debugmsg+" parameter cell="+cell+" ; it is converted to point="+cellPoint);
					if(cellPoint==null){
						Log.debug(debugmsg+" cell is null");
						throw new SAFSParamException("parameter cell's value '"+cell+"' can't be converted to row and column");
					}else{
						row = cellPoint.x;
						column = cellPoint.y;
					}
				}
				
				Log.debug(debugmsg+" compare to cell("+row+","+column+") " );
				actionVerify(row,column,null,false,val);
			}catch(SAFSParamException pe){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug(debugmsg + pe.getMessage());
				componentFailureMessage(pe.getMessage());
				return;
			}
		}
	}
	
	protected void commandVerifyFuzzyCellText(boolean benchFuzzy) throws SAFSException{
		String debugmsg = getClass().getName()+".commandVerifyFuzzyCellText() ";
		
		if (params.size() < 1) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			try{
				int row = 1, column =1;
				Iterator piter = params.iterator();
				//First parameter is the bench value
				String val = (String) piter.next();
				
				//Second parameter is the row
				row = paramRow(piter,true);
				
				//Third parameter is the column
				column = paramColumn(piter,true);
				
				Log.debug(debugmsg+" compare to cell("+row+","+column+") " );
				actionVerify(row,column,null,benchFuzzy,val);
			}catch(SAFSParamException pe){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				Log.debug(debugmsg + pe.getMessage());
				componentFailureMessage(pe.getMessage());
				return;
			}
		}
	}
	
	protected void commandVerifyColumnLabel() throws SAFSException{
		String debugmsg = getClass().getName()+".commandVerifyColumnLabel() ";
		
		if (params.size() < 2) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			paramsFailedMsg(windowName, compName);
		} else {
			Iterator piter = params.iterator();
			//First parameter is the bench value, column header name
			String val = (String) piter.next();
			int columnIndex = getColi(false, val.trim());
			Log.debug(debugmsg+" Column header="+val+" ; its index="+columnIndex);
			
			//Second parameter is the column
			String column = (String) piter.next();
			column = column.trim();
			Log.debug(debugmsg+" parameter column index="+column);
			
			detail = " Column header="+val+" ; its index="+columnIndex+" parameter column index="+column;
			if(String.valueOf(columnIndex).equals(column)){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				componentSuccessMessage(detail);
			}else{
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				componentFailureMessage(detail);
			}
		}
	}
	
	protected void actionVerify( int row, int col, String column, boolean fuzzy, String val) throws SAFSException {
		if (column != null && column.length() != 0) {
			col = getColi(fuzzy, column);
		}
		String rval = getCellText( row, col);
	
		if (action.equalsIgnoreCase(VERIFYCELLTEXT) ||
			action.equalsIgnoreCase(VERIFYFUZZYCELLTEXT) ||
			action.equalsIgnoreCase(VERIFYCELLTEXTFIND) ||
			action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTFIND) ||
			action.equalsIgnoreCase(VERIFYCELLVALUE)) {
			if (fuzzy) {
				if (!val.equalsIgnoreCase(rval.toString())) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							       " " + action + ", VP failure, read property is: " + rval + ", compare value is: " + val,
							       FAILED_MESSAGE);
					return;
				} else {
					String altText = "read prop: " + rval + ", compare value is: " + val + ", "+ windowName + ":" + compName + " " + action;
					log.logMessage(testRecordData.getFac(),
							       passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val, windowName, compName, action),
							       PASSED_MESSAGE);
				}
			} else {
				if (!val.equals(rval.toString())) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							       " " + action+ ", VP failure, read property is: " + rval+ ", compare value is: " + val,
							       FAILED_MESSAGE);
					return;
				} else {
					String altText = "read prop: " + rval+ ", compare value is: " + val + ", " + windowName + ":" + compName + " " + action;
					log.logMessage(testRecordData.getFac(),
							       passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,windowName, compName, action),
							       PASSED_MESSAGE);
				}
			}
		} else if (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS) ||
				   action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTCONTAINS)) {
			if (fuzzy) {
				if (rval.toString().toLowerCase().indexOf(val.toLowerCase()) < 0) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							       " " + action+ ", VPContains failure, read property is: "+ rval + ", compare value is: " + val,
							       FAILED_MESSAGE);
					return;
				} else {
					String altText = "read prop: " + rval+ ", compare value is: " + val + ", " + windowName + ":" + compName + " " + action;
					log.logMessage(testRecordData.getFac(),
							       passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,windowName, compName, action),
							       PASSED_MESSAGE);
				}
			} else {
				if (rval.toString().indexOf(val) < 0) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							       " " + action+ ", VPContains failure, read property is: "+ rval + ", compare value is: " + val,
							       FAILED_MESSAGE);
					return;
				} else {
					String altText = "read prop: " + rval+ ", compare value is: " + val + ", "+ windowName + ":" + compName + " " + action;
					log.logMessage(testRecordData.getFac(),
							       passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,windowName, compName, action),
							       PASSED_MESSAGE);
				}
			}
		} else {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), "unrecognized action: " + action, FAILED_MESSAGE);
			return;
		}
		// set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
	}

	/**
	 * <em>Note:</em>  As the method of super will subtract 1 from the returned value,
	 *                 so that a 0-based index can be returned<br>
	 *                 But we need a 1-based index in this class, so I override the it
	 *                 and add 1 back.
	 * @param number   The number to be converted to integer
	 * @return Integer The converted number.
	 */
	public Integer convertNum(String number){
		Integer result = super.convertNum(number);
		
		if(result!=null){
			return result+1;
		}else{
			return null;
		}
	}
}
