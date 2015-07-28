/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational.flex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.CFTable;
import org.safs.rational.Script;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexDataGridTestObject;
import com.rational.test.ft.script.Cell;
import com.rational.test.ft.vp.ITestDataTable;
import com.rational.test.ft.vp.ITestDataText;

public class CFFlexTable extends CFTable {

	/**
	 * This method override that of superclass CFTable.
	 */
	@SuppressWarnings("unchecked")
	protected void doActionClick(GuiSubitemTestObject guiObj, 
	                            int row, int col,
	                            String rowval, String colval)
	                            throws SAFSException {
		String debugmsg = getClass().getName()+".doActionClick(): ";
		Log.debug(debugmsg+"objectClassName: "+guiObj.getObjectClassName());//FlexDataGrid
		//We need firstly convert FLEX table test object to FlexDataGridTestObject
		FlexDataGridTestObject flexTable = new FlexDataGridTestObject(guiObj.getObjectReference());
		
  		Cell cell = getCell(row, col);
	    if (action.equalsIgnoreCase(RIGHTCLICKCELL)) {
	    	//It seems that this right click can not work for FLEX table
	    	flexTable.click(Script.RIGHT, cell);
  		} else if (action.equalsIgnoreCase(DOUBLECLICKCELL) ||
            action.equalsIgnoreCase(ACTIVATECELL) ||
            action.equalsIgnoreCase(CLICKCELL)) {
  			//FlexDataGridTestObject does not support doubleClick(Subitem), so use click(Subitem) instead
  			flexTable.click(cell);
  		} else{
  			flexTable.click(cell);
  		}
  		// set status to ok
      	String altText = windowName+":"+compName+" "+action+
    		", row: "+row+", col: "+col+", or rowval: "+rowval+
    		", or column name: "+colval;
      	Collection p = new LinkedList();
  		p.add(windowName); p.add(compName); p.add(action);
  		p.add("row: ");         p.add(Integer.toString(row));
  		p.add("col: ");         p.add(Integer.toString(col));
  		p.add("rowval: ");      p.add(rowval);
  		p.add("column name: "); p.add(colval);
  		log.logMessage(testRecordData.getFac(),passedText.convert(TXT_SUCCESS_3, altText, p),PASSED_MESSAGE);
  		testRecordData.setStatusCode(StatusCodes.OK);
	}
	
	/**
	 * Get the content of a FLEX table, the returning value is an ITestDataTable.
	 * Note: It will take relatively much time. 
	 * 
	 * @param tableguiObj, a FLEX table GUI object
	 * @return ITestDataTable
	 */
	private ITestDataTable getITestDataTable(TestObject tableguiObj){
	    String debugMsg = getClass().getName() + ".getITestDataTable(): ";
		ITestDataTable testTable = null;
		try{
			testTable = (ITestDataTable)tableguiObj.getTestData("contents");
		}catch(Exception e){
			Log.debug(debugMsg+e.toString());
		}
		
		if (testTable == null)
			Log.info(debugMsg+".... the table Component(FLEX) is not supported");
		return testTable;
	}
	
	/** <br><em>Purpose:</em> overrides CFTable.getColi. get the column number based on fuzzy match
	 * @param                     fuzzy, boolean
	 * @param                     column, String
	 * @param                     guiObj, GuiTestObject
	 * @return                    int
	 **/
	protected int getColi(boolean fuzzy, String column, GuiTestObject guiObj) throws SAFSException {
	    String debugMsg = getClass().getName() + ".getColi(): ";

		Log.info(debugMsg + " Column name: " + column);
	  	try {
		  	ITestDataTable testTable = getITestDataTable(guiObj); 
		    int colCount = testTable.getColumnCount();
		    Log.info(debugMsg+" Column number: " + colCount);

			if (fuzzy) {
				for (int i = 0; i < colCount; i++) {
					String colName = getColumnHeader(testTable,i);
					Log.info("column " + i + " : " + colName);
					if (colName.toLowerCase().indexOf(column.toLowerCase()) >= 0) { //found it
						log.logMessage(testRecordData.getFac(), "Found col: "+ colName, DEBUG_MESSAGE);
						return i;
					}
				}
			}else{
				for (int i = 0; i < colCount; i++) {
					String colName = getColumnHeader(testTable,i);
					Log.info("column " + i + " : " + colName);
					if (colName.equals(column)) { //found it
						log.logMessage(testRecordData.getFac(), "Found col: "+ colName, DEBUG_MESSAGE);
						return i;
					}
				}	
			}
	  	} catch(Exception e) {
	  		Log.debug(debugMsg + e.toString());
	  	}
	  	// not found, throw a SAFSException
	  	throw new SAFSException(this, debugMsg,
				" failure, column not found in " + (fuzzy ? "fuzzy " : "") + "match: " + column);
	}

	/** <br><em>Purpose:</em> overrides CFTable.getNumRows, gets number of rows in FLEX tables.
	 *                        
	 * @param                     flexTable, TestObject
	 * @return                    int
	 * @exception SAFSException based on caught Exception
	 **/
	protected int getNumRows(TestObject flexTable) throws SAFSException  {
	  	String debugMsg = getClass().getName() + ".getNumRows(): ";   
		try {
		    ITestDataTable testTable = getITestDataTable(flexTable);
	    	return testTable.getRowCount();
	    } catch(Exception e) {
	    	Log.debug(debugMsg + e.getMessage());
	        throw new SAFSException(e.getMessage());
	    }    
	}
	/** <br><em>Purpose:</em> overrides CFTable.getNumColumns, gets number of columns in FLEX table
	 * @param                     flexTable, TestObject
	 * @return                    int
	 * @exception SAFSException based on caught Exception
	 **/
	protected int getNumColumns (TestObject flexTable) throws SAFSException  {
	  	String debugMsg = getClass().getName() + ".getNumColumns(): ";   
		try {
		    ITestDataTable testTable = getITestDataTable(flexTable);
	    	return testTable.getColumnCount();
	    } catch(Exception e) {
	    	Log.debug(debugMsg+ e.getMessage());
	        throw new SAFSException(e.getMessage());
	    }    
	}
	
	/** <br><em>Purpose:</em> overrides the mehod in its superclass CFTable
	 * @param                     jctable, TestObject
	 * @param                     row, int 0-based index.
	 * @param                     col, int 0-based index.
	 * @return                    Object, contents of cell
	 * @exception SAFSException based on caught Exception, like ClassCastException
	 **/
	protected Object getCell(TestObject tableObject, int row, int col) throws SAFSException {
		String debugMsg = getClass().getName() + ".getCell(TestObject,int,int): ";
		try {
			ITestDataTable table = getITestDataTable(tableObject);
			Log.info("getCell; row: " + row + ", col: " + col);
			Object nsub = table.getCell(row, col);
			Log.info("getCell; row: " + row + ", col: " + col + ", nsub: " + nsub);
			return nsub;
		} catch (NullPointerException npe) {
			Log.debug(debugMsg+npe.getMessage());
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			throw new SAFSException(debugMsg + ": item not found for: "+ "row: " + row + ", col: " + col);
		} catch (WrappedException we) {
			Log.debug(debugMsg+we.getMessage());
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			throw new SAFSException(debugMsg + ": item not found for: " + "row: " + row + ", col: " + col);
		} catch (Exception e) {
			Log.debug(debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			throw new SAFSException(debugMsg + ": item not found for: " + "row: " + row + ", col: " + col);
		}
	}
	
	/**
	 * @param table			ITestDataTable, represents the testing table
	 * @param columnNumber	int, the number of the column
	 * @return				String, the header name of column whose number is given as parameter columnNumber
	 */
	protected String getColumnHeader(ITestDataTable table, int columnNumber){
		String debugmsg = getClass().getName()+"getColumnHeader(): ";
		Object columnHeader = null;
		String headerName = null;
		
		columnHeader = table.getColumnHeader(columnNumber);
		if(columnHeader instanceof String){
			headerName = (String) columnHeader;
		}else if(columnHeader instanceof ITestDataText){
			headerName = ((ITestDataText) columnHeader).getText();
		}else{
			Log.debug(debugmsg+" column header's class is "+columnHeader.getClass()+". Need add new implementation.");
		}
		
		return headerName;
	}
	
	  /**
	   * Override that of its superclss CFTable
	   */
	  protected String getColumnName(TestObject guiObj, int col){
	  	try {
	  		ITestDataTable table = getITestDataTable(guiObj);
	  		return getColumnHeader(table,col);
	  	}
	  	catch(Exception x){
	  		return null;
	  	}
	  }	
	
	/**
	 * @param table GuiTestObject to snapshot data from. DataGrid and DataGridView are acceptable.
	 * 
	 * @return List of rows of column value Lists. Null if an invalid table
	 *         reference is provided or some other error occurs.
	 * 
	 * @throws SAFSException
	 * @throws IllegalArgumentException if table is not an acceptable GuiTestObject.
	 */
	@SuppressWarnings("unchecked")
	protected List captureObjectData(TestObject table)throws IllegalArgumentException, SAFSException{
		String debugMsg = getClass().getName() + ".captureObjectData: ";

	  	ITestDataTable iTestTable = getITestDataTable(table);
	    if (iTestTable==null) {
	    	Log.info(debugMsg+" TestObject is not a FLEX table.");
	    	return null;
	    }	
	    int absrows =  iTestTable.getRowCount();
	    int abscols =  iTestTable.getColumnCount();
	    
	    List rows = new ArrayList();
	    List header = new ArrayList();
	    String colname = null;
	    for (int headi = 0;headi<abscols;headi++){
	    	try{
	    		// if no column header does null get added?
	    		colname = getColumnHeader(iTestTable,headi);
	    		if (colname == null) colname = "";
	    		header.add(colname);
	    	}
	    	catch(Exception x){
	    		header.add("");
	    	}
	    }
	    // might be empty collection if no header exists
	    rows.add(header);

	    try {
	    	for (int rowi = 0; rowi < absrows; rowi++) {
	        	List cols = new ArrayList();
	    		for(int coli = 0; coli < abscols; coli++) {
	    			Object cellobj = iTestTable.getCell(rowi, coli);
	    			if (cellobj != null){
	    				if(cellobj instanceof String)	cols.add((String)cellobj);
	    				else if(cellobj instanceof ITestDataText){
	    					cols.add(((ITestDataText)cellobj).getText());
	    				}else{
	    					Log.debug(debugMsg+" cell object is "+cellobj.getClass().getName()+". Need new implementation to treat.");
	    					cols.add(cellobj.toString());					
	    				}
	    			}else
	    				cols.add(""); //add a blank for a null cell. Fix S0533290
	    		}
	    		rows.add(cols);
	    	}
	    } catch(Exception e) {
			throw new SAFSException(debugMsg+e.toString());
		}

	    return rows;
	}
	
	/**
	 * Override that of its superclass CFTable
	 * <br>
	 * Note: Flex table does not extend from GuiSubitemTestObject, but from FlexDataGridTestObject
	 */
	protected void actionLocalSelectCellTextSpecial(GuiTestObject guiObj,
			int row, int col, String rowval, String column, boolean control,
			boolean shift, boolean leftRight) throws SAFSException,
			SubitemNotFoundException, NullPointerException, WrappedException {

		Cell cell = getCell(row, col);
		FlexDataGridTestObject flexTable = new FlexDataGridTestObject(guiObj.getObjectReference());

		if (leftRight) { // left is true
			if (control) {
				flexTable.click(Script.CTRL_LEFT, cell);
			} else if (shift) {
				flexTable.click(Script.SHIFT_LEFT, cell);
			} else {
				flexTable.click(cell);
			}
		} else { // Right is true
			if (control) {
				flexTable.click(Script.CTRL_RIGHT, cell);
			} else if (shift) {
				flexTable.click(Script.SHIFT_RIGHT, cell);
			} else {
				flexTable.click(cell);
			}
		}

		log.logMessage(testRecordData.getFac(), " " + action + " ok [" + row + "," + col + "]", PASSED_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);
	}
	
	/**
	 * Override that of its superclass CFTable
	 * <br>
	 * Note: For Flex Table, property editable is applied to all table, so
	 * 		 parameter row and col will have no effect here.
	 **/
	protected void actionVerifyCellEditable(GuiTestObject guiObj, int row,int col, boolean verifyFlag) throws SAFSException {
		String debugmsg = getClass().getName() + ".actionVerifyCellEditable(): ";

		try {
			Boolean isEditable = new Boolean(false);
			String propertyEditable = "editable";

			try {
				Object obj = guiObj.getProperty(propertyEditable);
				if (obj instanceof String) {
					isEditable = new Boolean((String) obj);
				} else if (obj instanceof Boolean) {
					isEditable = (Boolean) obj;
				} else {
					Log.debug(debugmsg + " object is "+ obj.getClass().getName()+ ". It needs to be converted to Boolean.");
				}

			} catch (PropertyNotFoundException mnfe) {
				Log.debug(debugmsg + " property " + propertyEditable + " can not be found");
			}

			if (isEditable.booleanValue() == verifyFlag) {
				String altText = "read prop: " + isEditable + ", compare value is: " + verifyFlag + ", " + windowName + ":" + compName + " " + action;
				log.logMessage(testRecordData.getFac(), passedText.convert(
						PRE_TXT_SUCCESS_4, altText, windowName, compName,
						action, "read prop: " + isEditable + ", compare value is: " + verifyFlag),
						PASSED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.OK);
			} else {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(), " " + action
						+ ", VP failure, read property is: " + isEditable
						+ ", compare value is: " + verifyFlag, FAILED_MESSAGE);
			}
		} catch (Exception e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), getClass().getName() + ": item not found for: " + "row:"
							+ row + ", col: " + col, FAILED_MESSAGE);
			throw new SAFSException(e.toString());
		}

	}

	/**
	 * overrides CFTable.actionClickColumnLabel for Flex support
	 * <br>
	 * Note: the parameter yoffset can not be supported here
	 */
	protected void actionClickColumnLabel(GuiTestObject guiObj, int col,int yoffset) throws SAFSException {
		String debugMsg = getClass().getName() + ".actionClickColumnLabel: ";
		try {
			Log.info(debugMsg + "***** actionClickColumnLabel: " + col);
			FlexDataGridTestObject flexTable = new FlexDataGridTestObject(guiObj.getObjectReference());

			flexTable.headerClick(Script.atIndex(col));
			// set status to ok
			testRecordData.setStatusCode(StatusCodes.OK);
			String altText = windowName + ":" + compName + " " + action;
			altText += " successful using '" + col + "'.";
			log.logMessage(testRecordData.getFac(), passedText.convert(
					"success3a", altText, windowName, compName, action, String.valueOf(col)), PASSED_MESSAGE);

		} catch (Exception e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), debugMsg
					+ ": item not found for: " + "col: " + col, FAILED_MESSAGE);
			throw new SAFSException(debugMsg + e.toString());
		}

	}
}
