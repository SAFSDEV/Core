/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;
import java.util.*;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;


import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.*;

import com.rational.test.ft.vp.*;


/**
 * Derived from CFTable, process table components for dotnet.
 * Two kinds of dotnet tables are supported. 
 * System.Windows.Forms.DataGridView and System.Windows.Forms.DataGrid.
 * 
 * @author JunwuMa
 * @since JUL 23, 2008
 * <br>   JUL 21, 2008    (JunwuMa) Original Release. 
 * 
 */
public class CFDotnetTable extends CFTable {
	public static final String CLASS_DATAGRIDVIEW_NAME = "System.Windows.Forms.DataGridView";
	public static final String CLASS_DATAGRID_NAME     = "System.Windows.Forms.DataGrid";
	
	/** <br><em>Purpose:</em> constructor, calls super
	 **/
	public CFDotnetTable() {
		super();
	}	

	private boolean IsRowHeadersVisible(GuiTestObject guiTable){
		String debugMsg = getClass().getName()+".IsRowHeadersVisible: ";
	    try {
	    	//both DataGridView and DataGrid have this property
	    	Boolean isVisible = (Boolean)guiTable.getProperty("RowHeadersVisible");
	    	return isVisible.booleanValue();	
	    } catch(Exception e) {
	    	Log.debug(debugMsg+e.toString());
		    return false;	
	    }
	}
	private boolean IsColumnHeadersVisible(GuiTestObject guiTable){
		String debugMsg = getClass().getName()+".IsColumnHeadersVisible: ";
	    try {
	    	//both DataGridView and DataGrid have this property
	    	Boolean isVisible = (Boolean)guiTable.getProperty("ColumnHeadersVisible");
	    	return isVisible.booleanValue();	
	    } catch(Exception e) {
	    	Log.debug(debugMsg+e.toString());
		    return false;	
	    }
	}
	/**
	 * overrides CFTable.actionClickColumnLabel for .NET support
	 */
	protected void actionClickColumnLabel (GuiTestObject guiObj, int col, int yoffset) throws SAFSException {
		String debugMsg = getClass().getName()+".actionClickColumnLabel: ";
		try {
			Log.info(debugMsg+"***** actionClickColumnLabel: "+col);
			      
		    Integer rowHeadersWidth = new Integer(0);

		    if (!IsColumnHeadersVisible(guiObj)) {
		    	throw new SAFSException(debugMsg+" Error: the table column is not visible!");
		    }
		    
		    if (IsRowHeadersVisible(guiObj)) {
  		        // get RowHeadersWidth
		    	try {
			    	//try it as DataGridView
			    	rowHeadersWidth = (Integer)guiObj.getProperty("RowHeadersWidth");
			    } catch(Exception e) {
			    	// try it as DataGrid
			    	rowHeadersWidth = (Integer)guiObj.getProperty("RowHeaderWidth");		    	
			    }
		    }
		    
		    int tot = 10; // some minimal pixel offset to the column
		    tot += rowHeadersWidth.intValue();
		    java.awt.Rectangle tr = guiObj.getClippedScreenRectangle();
		    //get Y of the left-point of cell(0,0). Header is right above cell(0,0). the point with Y-10 must be in Header.
		    int headerMargin_y = getLocalRect(guiObj, 0, 0).y;
		    
		    for(int c=0; c<col; c++) {
		        java.awt.Rectangle r = getLocalRect(guiObj, 0, c);
		        tot += r.width;
		    }
		    if(tot > tr.width){
		    	try{((GuiSubitemTestObject)guiObj).setState(Action.hScroll(tot - tr.width));}
			    catch(Exception x){
			      	  Log.warn("actionClickColumnLabel Ignoring Exception:\n"+x.toString());
			    }
		    }
		    Log.info("** total width: "+tot);
		       
		    java.awt.Point p = new java.awt.Point(tot, headerMargin_y-10);

		    if (action.equalsIgnoreCase(CLICKCOLUMNLABEL)) {
		    	guiObj.click(p);
			    // set status to ok
			    testRecordData.setStatusCode(StatusCodes.OK);
			    String altText = windowName+":"+compName+" "+action;
				altText += " successful using '"+ col +"'.";
				log.logMessage(testRecordData.getFac(), passedText.convert("success3a", altText, windowName, compName, action, String.valueOf(col)), PASSED_MESSAGE);
		    } else if (action.equalsIgnoreCase(DOUBLECLICKCOLUMNLABEL) ||
		                 action.equalsIgnoreCase(ACTIVATECOLUMNLABEL)) {
		    	guiObj.doubleClick(p);
		     	// set status to ok
			    testRecordData.setStatusCode(StatusCodes.OK);
		  		String altText = windowName+":"+compName+" "+action;
				altText += " successful using '"+ col +"'.";
				log.logMessage(testRecordData.getFac(), passedText.convert("success3a", altText, windowName, compName, action, String.valueOf(col)), PASSED_MESSAGE);
		    }

		} catch (Exception e) {
		    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		    e.printStackTrace();
		    log.logMessage(testRecordData.getFac(), debugMsg + ": item not found for: "+ 
			       "col: "+ col, FAILED_MESSAGE);
		    throw new SAFSException(debugMsg+e.toString());
		}

	}
	/* overrides CFTable.getLocalRect supporting .NET table classes.
	 * (non-Javadoc)
	 * @see org.safs.rational.CFTable#getLocalRect(com.rational.test.ft.object.interfaces.GuiTestObject, int, int)
	 */
	protected java.awt.Rectangle getLocalRect(GuiTestObject jctable, int row, int col) {
	    String debugMsg = getClass().getName() + ".getLocalRect(): ";
		Log.info(debugMsg);
	    Log.debug("**** getLocalRect: Given Row: " + row + "  Col: " + col + " ****");

	    java.awt.Rectangle  r =null;
	    try {
	    	// treat jctable as System.Windows.Forms.DataGridView
			Object[] params_3 = new Object[3];
			params_3[0]=new Integer(col);
			params_3[1]=new Integer(row);
			params_3[2]=new Boolean(true);		    	
	    	r = (java.awt.Rectangle)jctable.invoke("GetCellDisplayRectangle", "(IIZ)LSystem.Object;", params_3);
	    } catch(Exception e) {
	    	// treat jctable as System.Windows.Forms.DataGrid
			try {
				Object[] params_2 = new Object[2];
				params_2[0]=new Integer(row);
				params_2[1]=new Integer(col);
		    	r = (java.awt.Rectangle)jctable.invoke("GetCellBounds", "(II)LSystem.Object;", params_2);
			} catch(Exception ex) {
				Log.debug(debugMsg+ex.toString());
				Log.info(debugMsg+ex.toString());
				return null;
			}
	    }
	    Log.info("**** Rectangle : " + r);
		return r;
	}
	
	/**
	 * Get the content of a .NET table, the returning value is an ITestDataTable.
	 * Note: It will take relatively much time. 
	 * 
	 * @param tableguiObj, a .NET table GUI object
	 * @return ITestDataTable
	 */
	private ITestDataTable getITestDataTable(TestObject tableguiObj){
	    String debugMsg = getClass().getName() + ".getITestDataTable(): ";
		ITestDataTable testTable = null;
		try {
			// treat tableguiObj as DataGridView
			testTable = (ITestDataTable)tableguiObj.getTestData("viewcontents");
			if(testTable == null) {
				// treat tableguiObj as DataGrid
				testTable = (ITestDataTable)tableguiObj.getTestData("contents");				
			}
		} catch(Exception e) {
			Log.debug(debugMsg+e.toString());
		}
		
		if (testTable == null)
			Log.info(debugMsg+".... the table Component(.NET) is not supported");
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
					String colName = (String)testTable.getColumnHeader(i);
					Log.info("column " + i + " : " + colName);
					if (colName.toLowerCase().indexOf(column.toLowerCase()) >= 0) { //found it
						log.logMessage(testRecordData.getFac(), "Found col: "+ colName, DEBUG_MESSAGE);
						return i;
					}
				}
			}else{
				for (int i = 0; i < colCount; i++) {
					String colName = (String)testTable.getColumnHeader(i);
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
	 	
	/** <br><em>Purpose:</em> overrides CFTable.getNumRows, gets number of rows in .Net tables.
	 *                        
	 * @param                     dotNetable, TestObject
	 * @return                    int
	 * @exception SAFSException based on caught Exception
	 **/
	protected int getNumRows(TestObject dotNetable) throws SAFSException  {
	  	String debugMsg = getClass().getName() + ".getNumRows(): ";   
		try {
		    ITestDataTable testTable = getITestDataTable(dotNetable);
	    	return testTable.getRowCount();
	    } catch(Exception e) {
	    	Log.debug(debugMsg + e.getMessage());
	        throw new SAFSException(e.getMessage());
	    }    
	}
	/** <br><em>Purpose:</em> overrides CFTable.getNumColumns, gets number of columns in .Net table
	 * @param                     dotNetable, TestObject
	 * @return                    int
	 * @exception SAFSException based on caught Exception
	 **/
	protected int getNumColumns (TestObject dotNetable) throws SAFSException  {
	  	String debugMsg = getClass().getName() + ".getNumColumns(): ";   
		try {
		    ITestDataTable testTable = getITestDataTable(dotNetable);
	    	return testTable.getColumnCount();
	    } catch(Exception e) {
	    	Log.debug(debugMsg+ e.getMessage());
	        throw new SAFSException(e.getMessage());
	    }    
	}
	/** <br><em>Purpose:</em> overrides CFTable.getCell, gets a Cell based on row and col
	 * @param                     row, int 0-based index.
	 * @param                     col, int 0-based index.
	 * @return                    Cell, the cell
	 * @exception SAFSException based on caught Exception
	 **/	  
	protected Cell getCell(int row, int col) throws SAFSException {
	  	String debugMsg = getClass().getName() + ".getCell(): "; 
		try {
			Row nrow = Script.localAtRow(row);
			// When working with DataGridView, atColumn(idx) returns nothing but the column at 0 no matter what idx is, 
			// seems it can't work with DataGridView(.NET) in RFT7.0.1.2. It is fine with DataGrid
			// it is ok using atColumn(atText("column_caption"))
			// The temporary solution is getting the column caption from column index first, and getting the column from its caption.
			// Potential risk: it could not return correct column when two column have same caption!!
			// It will be solved in the future. 
			Column ncol = null;
			if (DotNetUtil.isSubclassOf(DotNetUtil.getClazz(obj1), CLASS_DATAGRIDVIEW_NAME)) { // fix S0553061
			    String colname = getColumnName(obj1,col);
			    ncol = com.rational.test.ft.script.SubitemFactory.atColumn(SubitemFactory.atText(colname));
				
			} else
				ncol = Script.localAtColumn(col); // treated as System.Windows.Forms.DataGrid
			
			Cell ncell = Script.localAtCell(ncol, nrow);
			if (ncell == null) {
				throw new SAFSException(this, debugMsg, "For row: " + row
						+ ", col: " + col + ", cannot find cell");
			}
			return ncell;
		} catch(Exception e) {
			throw new SAFSException(debugMsg + e.toString());
		}
	}
	
	/* Get a Cell in an ITestDataTable based on row and col.
	 * row and col are 0-based index.
	 */
	private Object getCell(ITestDataTable iTestTable, int row, int col) throws SAFSException  {
		String debugMsg = getClass().getName() + ".getCell(ITestDataTable,int,int): "; 
	    try {
	      Log.info("getCell; row: "+row+", col: "+col);
	      Object nsub = iTestTable.getCell(row, col);
	      Log.info("getCell; row: "+row+", col: "+col+", nsub: "+nsub);
	      return nsub;
	    } catch (Exception e) {
	      e.printStackTrace();
	      log.logMessage(testRecordData.getFac(),
	    		  debugMsg+": item not found for: "+ 
	          "row: "+row+", col: "+col,
	          FAILED_MESSAGE);
	      throw new SAFSException(debugMsg + e.toString());
	    }
	}	

	/** <br><em>Purpose:</em> overrides CFTable.getCell supporting .NET tables to get a cell based on row and col
	 *  Note:it is low-effective to call this method repeatly. 
	 * @param                     jctable, TestObject
	 * @param                     row, int 0-based index.
	 * @param                     col, int 0-based index.
	 * @return                    Object, contents of cell
	 * @exception SAFSException based on caught Exception, like ClassCastException
	 **/
	protected Object getCell(TestObject jctable, int row, int col) throws SAFSException  {
		String debugMsg = getClass().getName() + ".getCell(TestObject,int,int): "; 
	    try {
    	  ITestDataTable dotNetTable = getITestDataTable(jctable);
	      Log.info("getCell; row: "+row+", col: "+col);
	      Object nsub = dotNetTable.getCell(row, col);
	      Log.info("getCell; row: "+row+", col: "+col+", nsub: "+nsub);
	      return nsub;
	    } catch (NullPointerException e) {
	      //e.printStackTrace();
	      log.logMessage(testRecordData.getFac(),"npe: .............", DEBUG_MESSAGE);
	      listAllProperties(jctable);
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      throw new SAFSException(debugMsg+": item not found for: "+ 
	                              "row: "+row+", col: "+col);
	    } catch (WrappedException we) {
	      //e.printStackTrace();
	      log.logMessage(testRecordData.getFac(),"wrapped exception: .............",
	          DEBUG_MESSAGE);
	      listAllProperties(jctable);
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      throw new SAFSException(debugMsg+": item not found for: "+ 
	                              "row: "+row+", col: "+col);
	    } catch (Exception e) {
	      e.printStackTrace();
	      log.logMessage(testRecordData.getFac(),
	    		  debugMsg+": item not found for: "+ 
	          "row: "+row+", col: "+col,
	          FAILED_MESSAGE);
	      throw new SAFSException(debugMsg + e.toString());
	    }
	}	
	/**
	 * <br><em>Purpose:</em> overrides CFTable.getColumnName. Retrieve the column name for the provided zero-based column number.
	 * @param guiObj Expected to be a Table with a getColumnName(i) method that returns a String.
	 * @param col the column to get the name of.
	 * @return String column name or null if not available or an error occurs.
	 */
	protected String getColumnName(TestObject guiObj, int col){
		String debugMsg = getClass().getName() + ".getColumnName: ";		
	  	try {
		    ITestDataTable testTable = getITestDataTable(guiObj);	  		
	        return (String) testTable.getColumnHeader(col);
	  	} catch(Exception x) {
	  		Log.debug(debugMsg + x.toString());
	  		return null;
	  	}
	}	  
	
	/**
	   * overrides CFTable.captureObjectData to capture the object data into a List of rows.  Each row is a List of column values.  
	   * The first row is a List of Column names, if any.  If no column names exists then this 
	   * first row will be a List of empty strings.
	   * 
	   * @param table GuiTestObject to snapshot data from. DataGrid and DataGridView are acceptable.
	   * 
	   * @return List of rows of column value Lists.  Null if an invalid table reference is 
	   * provided or some other error occurs.
	   * 
	   * @throws SAFSException
	   * @throws IllegalArgumentException if table is not an acceptable GuiTestObject.
	   * @see CFTable#captureObjectData(TestObject)
	   * @see #formatObjectData(java.util.List)
	   */
	protected java.util.List captureObjectData(TestObject table)throws IllegalArgumentException, SAFSException{
		String debugMsg = getClass().getName() + ".captureObjectData: ";
	  	Log.info(debugMsg);

	  	ITestDataTable iTestTable = getITestDataTable(table);
	    if (iTestTable==null) {
	    	Log.info(debugMsg+" TestObject is not a .NET table.");
	    	return null;
	    }	
	    int absrows =  iTestTable.getRowCount();
	    int abscols =  iTestTable.getColumnCount();
	    
	    java.util.List rows = new ArrayList();
	    java.util.List header = new ArrayList();
	    String colname = null;
	    for (int headi = 0;headi<abscols;headi++){
	    	try{
	    		// if no column header does null get added?
	    		colname = (String)iTestTable.getColumnHeader(headi);
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
	    	//ITestDataTable dotNetTable = getITestDataTable(jctable);
	    	for (int rowi = 0; rowi < absrows; rowi++) {
	        	java.util.List cols = new ArrayList();
	    		for(int coli = 0; coli < abscols; coli++) {
	    			//it is low-effective to call getCell(jctable, rowi, coli) repeatly
	    			Object cellobj = getCell(iTestTable, rowi, coli);
	    			if (cellobj != null)
	    				cols.add((String)cellobj);
	    			else
	    				cols.add(""); //add a blank for a null cell. Fix S0533290
	    		}
	    		rows.add(cols);
	    	}
	    } catch(Exception e) {
			throw new SAFSException(debugMsg+e.toString());
		}

	    Log.info(debugMsg+" success!");
	    return rows;
	}
	  
	/** <br><em>Purpose:</em> overides CFTable.VerifyCellEditable for supporting VerifyCellEditable on .NET
	 ** <p> For DataGrid and DataGridView, property "ReadOnly" shows if the table is editable.
	 ** <p> if row or col is not in the range it should be, throws SAFSExecption. 
	 ** <br> T, JavaWin, JCTable, VerifyCellEditable, 2,5,true
	 ** <p> 
	 * @param                     guiObj, GuiTestObject
	 * @param                     row, int 0-based index.
	 * @param                     col, int 0-based index.
	 * @param					  verifyFlag, boolean
	 **/
	protected void actionVerifyCellEditable (GuiTestObject guiObj, int row, int col, boolean verifyFlag) throws SAFSException {
		String debugMsg = getClass().getName()+".actionVerifyCellEditable: ";
		Log.debug(debugMsg);
	    try {
	      //GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);

	      boolean isEditable = false;

	      try{
	    	  isEditable = !((Boolean)guiObj.getProperty("ReadOnly")).booleanValue();
	      } catch (ClassCastException mnfe) {
		      	Log.info(debugMsg + "* * * Caught ClassCastException * * *");
		      	isEditable = false;
	      } catch (MethodNotFoundException mnfe) {
	      	Log.info(debugMsg + "* * * Caught Method Not Found Exception (mnfe) * * *");
	      	isEditable = false;
	      } catch (WrappedException we) {		//This catch is manily to cover the grids which setTableDataItem is valid...
	      	Log.info(debugMsg + "* * * Caught WrappedException (we) [2 nd catch] * * *");
	      } catch (com.rational.test.ft.IllegalAccessException iae) {		// All the grids in the InvoiceAdjSumWinJ...
	      	Log.info(debugMsg + "* * * Caught IllegalAccessException (iae) [3 rd catch] * * *");
	      	isEditable = false;
	      }

		  if (isEditable == verifyFlag) {
			String altText = "read prop: "+isEditable+", compare value is: "+verifyFlag+", "+windowName+":"+compName+" "+action;
			log.logMessage(testRecordData.getFac(), passedText.convert(PRE_TXT_SUCCESS_4, altText, windowName, compName, action, "read prop: "+isEditable+", compare value is: "+verifyFlag), PASSED_MESSAGE);
			testRecordData.setStatusCode(StatusCodes.OK);
		  } else {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(),
			" "+action+", VP failure, read property is: "+isEditable+", compare value is: "+verifyFlag, FAILED_MESSAGE);
		  }
		} catch (SubitemNotFoundException ex) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(), debugMsg+": cell not found for row: "+ 
	                row + ", col: "+ col + "; msg: "+ ex.getMessage(), FAILED_MESSAGE);
	    } catch (Exception e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			e.printStackTrace();
			log.logMessage(testRecordData.getFac(), debugMsg+ ": item not found for: "+ 
						   "row:" + row +", col: "+ col, FAILED_MESSAGE);
			throw new SAFSException(e.toString());
	    }
	}
	
} 
