/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs.rational;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.text.GENStrings;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.ITestDataTable;
import com.rational.test.ft.vp.ITestDataText;

  // Html.TABLE is of class 'StatelessGuiSubitemTestObject'

/**
 * Process html table components (like Html.TABLE).
 * See the file ObjectTypesMap.dat for cross reference as to which map to us.
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   Dec 16, 2003
 *
 *   <br>   Dec 16, 2003    (DBauman) Original Release
 *   <br>   Nov 23, 2004    (Carl Nagle) Correcting Click implementation for HTMLTables
 *   <br>   Nov 03, 2005    (Carl Nagle) Removing Generic Click as supported by CFComponent.
 *                                   Removing ClickCell as supported by CFTable.
 *                                   Added convertCoords to accept Coords=x1,y1,x2,y2 variants 
 *                                   of generic Click commands.
 *   <br>   Apr 08, 2008    (Lei Wang and JunwuMa) 
 * 								     Added localprocess() for better using hopefully(in the future).
 *                                   Added 'VerifyCellValue' in localprocess().
 *                                   Added getColumnNames,getColumns,getCellText.
 *                                   Overrided getColumnName,getNumRows, getNumColumns and getColi for supporting HTML table.
 *                                   Modified some code in CFHtmlTable.commandCaptureRangeToFile making it useful 
 *                                   for 'CaptureFuzzyRangeToFile' and 'CaptureRangeToFile' in HTML table.
 *                          Below are new keywords supported by HTML table.
 *										CaptureFuzzyRangeToFile
 *                                      CaptureRangeToFile
 *                                      SelectCell
 *                                      SelectCellContainsTextFind
 *										SelectCellText
 *										SelectCellTextFind
 *										SelectFuzzyCellText
 *										SelectFuzzyCellTextFind
 *										VerifyCellTextFind
 *										VerifyColumnLabel
 *										VerifyFuzzyCellText
 *										VerifyFuzzyCellTextContains
 *										VerifyFuzzyCellTextFind
 *										VerifyCellValue
 *                          
 *                          JSAFSBefore this modification, supported keywords are:
 *										ActivateCell  
 *										AssignVariableCellText  
 *										ClickCell  
 *										DoubleClickCell  
 *										RightClickCell  
 *										SelectCellTextSpecial  
 *										VerifyCellEditable  
 *										VerifyCellText  
 *										VerifyCellTextContains
 *	<br>	Oct 27, 2008 	(Lei Wang)	Add method captureObjectData() and getITestDataTable(), these methods
 *										are almost the same as those in CFDotNetTable, maybe we should consider
 *										to move them to CFTable in future. See defect S0542755.
 *  <br>	MAR 10, 2009	(Lei Wang)	Remove method command commandCaptureRangeToFile(), keyword CaptureRangeToFile
 *  									will call the same method in superclass CFTable.
 *  									Modify method getITestDataTable(): try type 'visiblegrid' and 'visiblecontents'
 *  									to get ITestDataTable.
 *  <br>	AUG 28, 2013	(Lei Wang)	Modify method getITestDataTable(): try 'visiblegrid', 'visiblecontents', 'contents' and 'grid' one by one.
 **/
public class CFHtmlTable extends CFTable  {
    
  public static final String VERIFYCELLVALUE = "VerifyCellValue";  
    
  public String getTextProperty() {return ".text";}

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFHtmlTable () {
    super();
  }

    /**
     * Overrides the version in superclass CFTable to allow for 
     * alternative of "Coords=x1,y1,x2,y2".  Supports both comma or semi-colon 
     * separators.  Calculates the point that is the center of the specified 
     * region.
     * <p>
     * The calling routine will have to determine whether the returned Point is an
     * x,y coordinate or a row,col cell -- probably by checking 
     * for "Row" or "Col" text in the provided string just as we do internally.
     * 
     * @param   coords, String x;y or x,y or Coords=x;y  or Coords=x,y  or
     *                   Row=y,Col=x  or  Col=x;Row=y  or
     *                   Coords=x1,y1,x2,y2  or  Coords=x1;y1;x2;y2
     * 
     * @return  Point (center of region) if successfull, null otherwise
     * 
     * @author Carl Nagle OCT 21, 2005 modified to work as required for 
     *                              keywords as documented.
     */
    public java.awt.Point convertCoords(String coords) {

   		// may throw NullPointerException
    	try{ if(coords.length()==0) return null; }
	   	catch(Exception x){ return null; }
    		
		String uccoords = coords.toUpperCase();
		
		// begin check for alternative "Row=n;Col=n" format    		
		int y = uccoords.indexOf("ROW");
		if (y >= 0) return super.convertCoords(coords);

		int x = uccoords.indexOf("COL");
		if (x >= 0) return super.convertCoords(coords);
		
		StringTokenizer toker = new StringTokenizer(coords, ",");
		int sepindex = toker.countTokens();
		if (sepindex <= 1){
			toker = new StringTokenizer(coords, ";");
			sepindex = toker.countTokens();	
		}
		// if not our special case then handle normally.
		if (sepindex < 4) return super.convertCoords(coords);

  	  	// get x1,y1,x2,y2
  	  	try{
  	  	  	  String token = toker.nextToken().trim();
  	  	  	  //check for possible Coords =
  	  	  	  sepindex = token.indexOf('=');
  	  	  	  if(sepindex > 0) token = token.substring(sepindex +1);
  	  	  	  x = Integer.parseInt(token.trim());
  	  	  	  y = Integer.parseInt(toker.nextToken().trim());
  	  	  	  int x2 = Integer.parseInt(toker.nextToken().trim());
  	  	  	  int y2 = Integer.parseInt(toker.nextToken().trim());
  	  	  	  // calculate the center of x,y x2,y2
  	  	  	  if (x > x2) {sepindex = x; x = x2; x2 = sepindex;}
  	  	  	  if (y > y2) {sepindex = y; y = y2; y2 = sepindex;}
 	  	  	  x = x + ((x2-x)/2);
 	  	  	  y = y + ((y2-y)/2);
 	  	  	  
 	  	  	  return new java.awt.Point(x,y);
 	  	  	  
		// IndexOutOfBounds?  NumberFormat?
  	  	}catch(Exception nf){
			// log bad format?
			return null;
		}
    }
    
 
  protected TestObject[] processChildren (TestObject obj1, String matchType) {
    TestObject[] children = obj1.getChildren();
    Log.info("ch.len: "+children.length);
    for(int i=0; i<children.length; i++) {
      Log.info("  obj.getObjectClassName: "+children[i].getObjectClassName());
      if (children[i].getObjectClassName().equals(matchType)) return children;
      TestObject[] children_i = processChildren(children[i], matchType);
      if (children_i != null) return children_i;
    }
    return null;
  }
  
  /**
   * <b>Note:</b><br>
   * For furture use if we want to separate the "manipulation of HtmlTable" from "CFTable"
   * Now the super class CFTable handle also the html table.
   */
  protected void localProcess() {      
      // do the work for htmlTable
      // take care of keywords for Html table only 
      
      htmlTableProcess();
      
      // only do if htmlTableProcess not done 
      if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
        super.localProcess(); // its parent class (CFTable) will do the work
      } else {
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+".process: "+testRecordData,
                       DEBUG_MESSAGE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+".process: params:"+params,
                       DEBUG_MESSAGE);
      }  
  }
  /**
   * <b>Note:</b><br>
   * For furture use. So far there is only one action inside.
   * Do local process for htmlTable
   *
   */
  private void htmlTableProcess() {
      //put code here
      
      try {
          // then we have to process for specific items not covered by our super
          log.logMessage(testRecordData.getFac(),
              getClass().getName()+".process, searching specific tests...",
              DEBUG_MESSAGE);

          if (action != null) {
            Log.info(".....CFHtmlTable.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
            if (action.equalsIgnoreCase(VERIFYCELLVALUE)) {
              commandVerifyCellValue();
            } 
            //all for now
          }
        } catch (com.rational.test.ft.SubitemNotFoundException snfe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
              "SubitemNotFoundException: "+snfe.getMessage(),
              FAILED_MESSAGE);
        } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
              "ObjectNotFoundException: "+onfe.getMessage(),
              FAILED_MESSAGE);
        } catch (SAFSException ex) {
          ex.printStackTrace();
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
              "SAFSException: "+ex.getMessage(),
              FAILED_MESSAGE);
        }
  }
	/**
	 * overrides CFTable.captureObjectData to capture the object data into a
	 * List of rows. Each row is a List of column values. The first row is a
	 * List of Column names, if any. If no column names exists then this first
	 * row will be a List of empty strings.
	 * 
	 * @param table GuiTestObject to snapshot data from. DataGrid and DataGridView are acceptable.
	 * 
	 * @return List of rows of column value Lists. Null if an invalid table
	 *         reference is provided or some other error occurs.
	 * 
	 * @throws SAFSException
	 * @throws IllegalArgumentException if table is not an acceptable GuiTestObject.
	 * @see CFTable#captureObjectData(TestObject)
	 * @see #formatObjectData(java.util.List)
	 */
	protected List<List<String>> captureObjectData(TestObject table)
			throws IllegalArgumentException, SAFSException {
		String debugMsg = getClass().getName() + ".captureObjectData: ";
		Log.info(debugMsg);

		ITestDataTable iTestTable = getITestDataTable(table);
		
		if (iTestTable == null) {
			Log.info(debugMsg + " TestObject is not a HTML table.");
			return null;
		}
		int absrows = iTestTable.getRowCount();
		int abscols = iTestTable.getColumnCount();

		List<List<String>> rows = new ArrayList<List<String>>();
		
		//Add column headers
		List<String> header = new ArrayList<String>();
		for (int headi = 0; headi < abscols; headi++) {
			header.add(getTextFromCellObject(iTestTable.getColumnHeader(headi)));
		}
		// might be empty collection if no header exists
		rows.add(header);

		//Add table content, each row
		try {
			String text = null;
			List<String> cols = null;
			for (int rowi = 0; rowi < absrows; rowi++) {
				cols = new ArrayList<String>();
				for (int coli = 0; coli < abscols; coli++) {
					text = getTextFromCellObject(iTestTable.getCell(rowi, coli));
					Log.debug(debugMsg+"processing cell("+rowi+","+coli+"): its text is "+text);
					cols.add(text);
				}
				rows.add(cols);
			}
		} catch (Exception e) {
			throw new SAFSException(debugMsg + e.toString());
		}

		Log.info(debugMsg + " success!");
		return rows;
	}
	
	/**
	 * @param cell,		Object, The cell object (cell of table/column-header/row-header)
	 * @return String,	The text value of the cell, or a blank string if some errors occur.
	 */
	private String getTextFromCellObject(Object cell){
		String debugMsg = getClass().getName() + ".getTextFromCellObject: ";
		String text = null;
		
		try {
			if (cell != null){
				if(cell instanceof ITestDataText){
					text = ((ITestDataText)cell).getText();
				}else if(cell instanceof String){
					text = cell.toString();
				}else{
					Log.debug(debugMsg+"cell type is"+cell.getClass().getName()+", need to add new code to handle it.");
				}
			}else{
				Log.error(debugMsg+"cell is null");
			}
		} catch (Exception x) {
			Log.debug(debugMsg+"Met Exception.", x);
		}
		if(text==null) text = "";
		
		return text;
	}

	/**
	 * Get the content of a HTML table, the returning value is an ITestDataTable.
	 * Note: It will take relatively much time. 
	 * 
	 * @param tableguiObj, a HTML table GUI object
	 * @return ITestDataTable
	 */
	private ITestDataTable getITestDataTable(TestObject tableguiObj){
	    String debugMsg = getClass().getName() + ".getITestDataTable(): ";
		ITestDataTable testTable = null;

		try{
			testTable = (ITestDataTable)tableguiObj.getTestData("visiblegrid");
		}catch(Exception ignore){}
		
		try{
			if(testTable==null || testTable.getRowCount()==0) {
				testTable = (ITestDataTable)tableguiObj.getTestData("visiblecontents");
			}
		}catch(Exception ignore){}
		
		try{
			if(testTable==null || testTable.getRowCount()==0) {
				testTable = (ITestDataTable)tableguiObj.getTestData("contents");
			}
		}catch(Exception ignore){}
		
		try{
			if(testTable==null || testTable.getRowCount()==0) {
				testTable = (ITestDataTable)tableguiObj.getTestData("grid");
			}
		}catch(Exception ignore){}
		
		if (testTable == null)
			Log.info(debugMsg+".... the table Component(HTML) is not supported");
		return testTable;
	}
	
  /**
	 * preform VerifyCellValue.
	 * 
	 * @throws SAFSException
	 */
  protected void commandVerifyCellValue() throws SAFSException{
  	String debugMsg = getClass().getName() + ".commandVerifyCellValue(): "; 
  	Log.info(debugMsg+"starting...");
    if (params.size() < 1) {
        paramsFailedMsg(windowName, compName);
    }
    else {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator<?> piter = params.iterator();
        // first param will be expected value
        String val =  (String) piter.next();
        Log.info("...val: "+val);
          
        // second param will be the optional cell location to be tested. Format: row,col row;col and etc.
        int row = 1; //use 1 as default if row param is empty
        int col = 1; //use 1 as default if col param is empty
        if (piter.hasNext()) { 
            String cell = (String) piter.next();
            Log.info("...cell location: "+cell);
            java.awt.Point cellPoint = convertCoords(cell);
            if (cellPoint == null) {
                String msg = "bad cell format:"+cell;
                Log.info(msg);
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                componentFailureMessage(msg);
                return;
            } else {
                row = cellPoint.x;
                col = cellPoint.y;
            }
        }
        // change to 0-based number
        if (--row < 0 || --col < 0) {
            String msg = debugMsg+"illegal row or col number, they should start from 1: ";
            Log.info(msg);
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            componentFailureMessage(msg);
            return ;
        }
            
        Log.info(getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", guiObj:"+guiObj);
        String cellVal = getCellText(guiObj, row, col);
        if (cellVal == null)
        {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            componentFailureMessage(" Row:"+row+" Col:"+col+", subitem not found for that cell.");
            return;
        }
        
        Log.info("...comparison:"+" read property is: "+cellVal+", compare value is: "+val);
        if (!val.equals(cellVal)) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            this.componentExecutedFailureMessage(genericText.convert(GENStrings.NOT_EQUAL,
                    								" cell value not equal.", 
                    								val,
                    								cellVal));   
        } else {
            testRecordData.setStatusCode(StatusCodes.OK);
            this.componentSuccessMessage(genericText.convert(GENStrings.EQUALS,
					" cell value equal.", 
					val,
					cellVal));            
        }
    }
    
  }
  /** <br><em>Purpose:</em> overrides CFTable.actionVerifyColumnLabel, only for HTML Table. assume that column labels is in the first line of the html table.
   * @param                     guiObj, GuiTestObject
   * @param                     val, String
   * @param                     col, int
   **/  
  protected void actionVerifyColumnLabel (GuiTestObject guiObj, String val, int col) throws SAFSException {
    try {
        Object[] param = new Object[1];
        param[0]=new Integer(col);
        String colLabel = getColumnName(guiObj, col);
  	    if (!val.equalsIgnoreCase(colLabel.toString())) {
  	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  	        log.logMessage(testRecordData.getFac(),
  	                " "+action+", VP failure, read property is: "+colLabel+", compare value is: "+val, FAILED_MESSAGE);
   	    } else {
   	        String altText =
  		    "read prop: "+colLabel+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
  			log.logMessage(testRecordData.getFac(), passedText.convert(PRE_TXT_SUCCESS_5, altText, colLabel, val,
  					   windowName, compName, action), PASSED_MESSAGE);
  			testRecordData.setStatusCode(StatusCodes.OK);
   	    }
    } catch (Exception e) {
  		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  		e.printStackTrace();
  		log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item not found for: "+ 
  					   "col: "+ col, FAILED_MESSAGE);
  		throw new SAFSException(e.toString());
    }

  }  
  /** <br><em>Purpose:</em> overrides CFTable.getColi. get the column number based on fuzzy match
   * @param                     fuzzy, boolean
   * @param                     column, String
   * @param                     guiObj, GuiSubitemTestObject
   * @return                    Integer
   **/
  protected int getColi(boolean fuzzy, String column, GuiTestObject guiObj) throws SAFSException {
  	String debugMsg = getClass().getName() + ".getNumRows(): ";
  	
  	Log.info(debugMsg+" Column name: " + column);
	String[] columnNames = getColumnNames(guiObj);
	Log.info(debugMsg+" Column number: " + columnNames.length);

	if (fuzzy) {
		for (int i = 0; i < columnNames.length; i++) {
			Log.info("column " + i + " : " + columnNames[i]);
			if (columnNames[i].toLowerCase().indexOf(column.toLowerCase()) >= 0) { //found it
				log.logMessage(testRecordData.getFac(), "Found col: "+ columnNames[i], DEBUG_MESSAGE);
				return i;
			}
		}
	}else{
		for (int i = 0; i < columnNames.length; i++) {
			Log.info("column " + i + " : " + columnNames[i]);
			if (columnNames[i].equals(column)) { //found it
				log.logMessage(testRecordData.getFac(), "Found col: "+ columnNames[i], DEBUG_MESSAGE);
					return i;
			}
		}
	}
	
	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	throw new SAFSException(this, "getColi",
			" failure, column not found in " + (fuzzy ? "fuzzy " : "") + "match: " + column);
  }
  
  /**
   * <br><em>Purpose:</em> get number of rows in HTML table; override CFTable.getNumRows(TestObject).
   * @param jctable
   * @return  int, the number of rows
   * @throws SAFSException 
   */
  protected int getNumRows (TestObject jctable) throws SAFSException  {
  	String debugMsg = getClass().getName() + ".getNumRows(): ";
    try {
    	int numberRows = 2000;
    	ITestDataTable testDataTable = getITestDataTable(jctable);
    	if(testDataTable!=null){
    		numberRows = testDataTable.getColumnCount();
    		return numberRows;
    	}
    	
    	Log.debug(debugMsg+" Can not get the RFT ITestDataTable, Use HTML TAG to calculate.");
      	TestObject[] bodies = jctable.find(Script.atChild(".class","Html.TBODY"),false);
      	if(bodies!=null && bodies.length>0){
      		TestObject[] rows = bodies[0].find(Script.atChild(".class","Html.TR"),false);
      		if(rows!=null){
      			numberRows = rows.length;
      		}
      	}
      	return numberRows;
    }
    catch (Exception e) {
    	Log.debug(debugMsg+ e.getMessage());
        throw new SAFSException(e.getMessage());
    }
  }
  
  /**
   * <br><em>Purpose:</em> get number of columns in HTML table; override CFTable.GetNumColumns(TestObject jctable), 
   * @param jctable
   * @return  int,  the number of columns
   * @throws SAFSException
   */
  protected int getNumColumns(TestObject jctable) throws SAFSException  {
  	String debugMsg = getClass().getName() + ".getNumColumns(): ";
    try {
    	int numberColumns = 2000;
    	ITestDataTable testDataTable = getITestDataTable(jctable);
    	if(testDataTable!=null){
    		numberColumns = testDataTable.getColumnCount();
    		return numberColumns;
    	}
    	
    	Log.debug(debugMsg+" Can not get the RFT ITestDataTable, Use HTML TAG to calculate.");
      	TestObject[] bodies = jctable.find(Script.atChild(".class","Html.TBODY"),false);
      	if(bodies!=null && bodies.length>0){
      		TestObject[] rows = bodies[0].find(Script.atChild(".class","Html.TR"),false);
      		if(rows!=null && rows.length>0){
      			TestObject[] columns = rows[0].getChildren();
          		if(columns!=null && columns.length>0){
          			numberColumns = columns.length;
          		}
      		}
      	}
      	return numberColumns;
    }
    catch (Exception e) {
    	Log.debug(debugMsg+ e.getMessage());
        throw new SAFSException(e.getMessage());        
    }
  }
  
  /**
   * <b>Note:</b><br>
   * 	The first line of html.table will be found, that is the first "&lttr>...&lt/tr>" contained in "&lttable>&lt/table>".
   * 	The "&lttd>...&lt/td>s" contained in that "&lttr>...&lt/tr>" will be returned as an array.
   * @param 	jctable		Which is the Html.TABLE object
   * @return				An array of "&lttd>...&lt/td>s"	
   * @throws SAFSException
   */
  protected TestObject[] getColumns(TestObject jctable) throws SAFSException {
  	String debugMsg = getClass().getName() + ".getColumns(): ";
  	TestObject[] columns = null;
  	
  	try {
		TestObject[] bodies = jctable.find(Script.atChild(".class","Html.TBODY"), false);
		if (bodies != null && bodies.length > 0) {
			TestObject[] rows = bodies[0].find(Script.atChild(".class","Html.TR"), false);
			if (rows != null && rows.length > 0) {
				columns = rows[0].getChildren();
			}
		}
	} catch (Exception e) {
		Log.debug(debugMsg+ e.getMessage());
		throw new SAFSException(e.getMessage());
	}
	
	if(columns==null){
		throw new SAFSException(this, "getColumns", "Can not get array of column for this table");
	}
		
	return columns;
  }
  
  /**
   * Overrides CFTable.getColumnName for using by HTML Table only. Retrieve the column name for the provided zero-based column number.
   * @param guiObj Expected to be a Html Table with a getColumnName(i) method that returns a String.
   * @param col the column to get the name of.
   * @return String column name or null if not available or an error occurs.
   */
  protected String getColumnName(TestObject jctable, int col){
  	String debugMsg = getClass().getName() + ".getColumnName(): ";
  	TestObject[] columns = null;
  	String columnHeaderName = null;
  	
  	try {
		TestObject[] bodies = jctable.find(Script.atChild(".class","Html.TBODY"), false);
		if (bodies != null && bodies.length > 0) {
			TestObject[] rows = bodies[0].find(Script.atChild(".class","Html.TR"), false);
			if (rows != null && rows.length > 0) {
				columns = rows[0].getChildren();
				//if(!columns[0].getObjectClassName().equalsIgnoreCase("Html.TH")){
				//	return null;
				//}
				// assume the first line in html.table is Column-Name-line no matter what attribute it is. The attribute could be Html.TH or Html.TD.
				columnHeaderName = columns[col].getProperty(getTextProperty()).toString();
			}
		}
	} catch (Exception e) {
		Log.debug(debugMsg+ e.getMessage());
		return null;
	}
	
	return columnHeaderName;
  }
  
  /**
   * @param jctable			Which is the Html.TABLE object
   * @return				An array of column names
   * @throws SAFSException
   */
  private String[] getColumnNames(TestObject jctable) throws SAFSException{
  	TestObject[] columns = getColumns(jctable);
  	String names[] = new String[columns.length];
  	
  	for(int i=0;i<columns.length;i++){
  		names[i] = (String) columns[i].getProperty(getTextProperty());
  	}
  	
  	return names;
  }
  
  /**
   * <b>Note:</b><br> get the text value of the cell at (row,col) in HTML table
   * @param guiObj
   * @param row
   * @param col
   * @return String, the text of the cell at row,col
   */
  protected String getCellText(TestObject guiObj, int row, int col){
    try {
        String rtlCellText = null;
        Object sub = getCell(guiObj, row, col);
        if (sub == null) {
            Log.info(" Row:"+row+" Col:"+col+", subitem not found for that cell.");
            return null;
        }
        if (sub instanceof TestObject) {
            Log.info("sub is actually of type TestObject, trying getPropety:'"+getTextProperty()+"'");
            rtlCellText = (String) ((TestObject)sub).getProperty(this.getTextProperty());
        }
        return rtlCellText;
      
    } catch (Exception se) {
          Log.debug(getClass().getName()+"."+"getCellText() "+se.getMessage());
          return null;
    }
  }  
    
}
