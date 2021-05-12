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

import java.util.*;
import java.io.*;

import org.safs.*;
import org.safs.tools.*;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

/**
 * <br><em>Purpose:</em> CFJCTable, process a JCTable component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUL 18, 2003
 *
 *   <br>   JUL 18, 2003    (DBauman) Original Release
 *   <br>   JAN 07, 2004    (BNat) Added New Keyword GETCELLCOORDINATES
 *	 <br>   JAN 19, 2004    (BNat) Added New Keywords VERIFYROWLABEL,VERIFYCOLUMNLABEL
 *   <br>   FEB 04, 2004	(BNat) Added New Keywords CLICKROWLABEL, DOUBLECLICKROWLABEL, ACTIVATEROWLABEL
 * 	 <br>											  CLICKCOLUMNLABEL, DOUBLECLICKCOLUMNLABEL, ACTIVATECOLUMNLABEL
 *   <br>   FEB 06, 2004	(BNat) Added New Keyword VERIFYCELLEDITABLE
 *   <br>   FEB 09, 2004	(BNat) Added New Keyword SELECTCELLTEXTSPECIAL
 *   <br>   FEB 10, 2004	(BNat) Added New Keyword GETTABLEROWCOLUMNCOUNT
 *   <br>	MAR 22, 2004	(BNat) Modified the code for VerifyCellEditable keyword first to look for the table
 *                                 level edit property then cell level.
 *   <br>	MAR 23, 2004	(BNat) Added Illegal Access Exception catch code for VerifyCellEditable keyword.
 *   <br>	APR 19, 2004	(BNat) getLocalCoords method updated to work more roboust with frozen column(s).
 * 
 **/
public class CFJCTable extends CFTable {
    
    protected int[] columnMap;
    protected int[] rowMap;

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFJCTable () {
    super();
  }

  /** <br><em>Purpose:</em> get a cell based on row and col, uses 'getCells' method
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @return                    Object, contents of cell
   * @exception SAFSException based on caught Exception, like index out of bounds, etc.
   **/
  protected Object getCell (TestObject jctable, int row, int col) throws SAFSException  {
    try {
      Collection cells = getCells(jctable);
      Collection entirerow = (Collection)((Vector)cells).elementAt(row);
      log.logMessage(testRecordData.getFac(),"entirerow:"+entirerow, DEBUG_MESSAGE);
      return ((Vector)entirerow).elementAt(col);
    } catch (Exception e) {
      throw new SAFSException(e.toString());
    }
  }
  
  /** <br><em>Purpose:</em> get all cells of the testobject (JCTable)
   * @param                     jctable, TestObject
   * @return                    Collection of Collections, contents of cells
   * @exception SAFSException based on caught Exception
   **/
  protected Collection getCells (TestObject jctable) throws SAFSException  {
    jctable = getJCTable(jctable);
    TestObject tblDataSource = null;
    try{
        tblDataSource = (TestObject)jctable.invoke("getDataSource");
        
        Integer numColumns = (Integer)tblDataSource.getProperty("numColumns");
        Integer numRows = (Integer)tblDataSource.getProperty("numRows");
        
            // the columnMap and rowMap define the display of the columns and rows
            setDefaultColumnMap(jctable);
            setDefaultRowMap(jctable);
        
        
        //following mehtods manipulate the row and column maps before they are
        //used to extract the actual visual cell text
        purgeHiddenCells(jctable);
        adjustForFrozenCells(jctable);
        
    } catch (MethodNotFoundException mnfe) {
    	Log.debug("Method not found exception received");
        mnfe.printStackTrace();
    }
    return extractCellText(jctable, tblDataSource);
  }
  
  /** <br /><em>Purpose:</em> Refine the columnMap and rowMap variables by
   * removing all of the hidden columns and rows from the arrays.
   * @param jctable
   */
  protected void purgeHiddenCells (TestObject jctable) {
    
    //object arrays used by the invoke() method for checking
  	//whether a row or column is hidden
    Object[] currentColumn = new Object[1];
    Object[] currentRow = new Object[1];        

    //temporary arrays to used to store the non-hidden columns and rows
    int[] nonHiddenColumnArray = new int[columnMap.length];
    int[] nonHiddenRowArray = new int[rowMap.length];
    
    int nonHiddenColumnCount = 0;
    int nonHiddenRowCount = 0;

    //loop through the columnMap and the rowMap and check to see if the column
    //or row is hidden.  if not, add the columnMap or rowMap to the temporary
    //arrays and increment the non-hidden column/row counters
    for (int j = 0; j < columnMap.length; j++) {
        currentColumn[0] = new Integer(columnMap[j]);
        if (!((Boolean)jctable.invoke("isColumnHidden","(I)",currentColumn))
                .booleanValue())
        {
            nonHiddenColumnArray[nonHiddenColumnCount] = columnMap[j];
            nonHiddenColumnCount++;
        }
    }
    for (int i = 0; i < rowMap.length; i++) {
        currentRow[0] = new Integer(rowMap[i]);
        if (!((Boolean)jctable.invoke("isRowHidden","(I)",currentRow))
                .booleanValue())
        {
            nonHiddenRowArray[nonHiddenRowCount] = rowMap[i];
            nonHiddenRowCount++;
        }
    }

    //reinitialize the columnMap and rowMap to arrays with enough room for the
    //number of hidden columns and rows 
    columnMap = new int[nonHiddenColumnCount];
    rowMap = new int[nonHiddenRowCount];
    
    //copy the data from the non-hidden arrays back into the column and row maps
    for (int j = 0; j < columnMap.length; j++) {
        columnMap[j] = nonHiddenColumnArray[j];
    }
    for (int i = 0; i < rowMap.length; i++) {
        rowMap[i] = nonHiddenRowArray[i];
    }
  }

  /** <br /><em>Purpose:</em> Refine the columnMap and rowMap variables by
   * reordering the arrays if certain frozen rows or frozen columns are placed
   * in the table in an abnormal way.
   * 
   * Columns: The placement can be either on the left side of the table or on
   * the right side of the table.  If the placement is on the right, adjustments
   * must be made to the columnMap array so that the designated frozen columns are
   * placed at the end of the column map.
   * 
   * Rows: The placement can be either on the top of the table or on the bottom
   * of the table.  If the placement is on the bottom, adjustments must be made
   * to the rowMap array so that the designated frozen rows are placed at the
   * end of the row map.
   * 
   * @param jctable
   */
  protected void adjustForFrozenCells (TestObject jctable) {    
    //get the number of frozen columns and rows.  the reference point for the
    //grouping of frozen columns and rows begins with the top-left corner of a
    //JCTable's datasource.
  	int numFrozenColumns = ((Integer)jctable
            .invoke("getFrozenColumns")).intValue();
    int numFrozenRows = ((Integer)jctable
            .invoke("getFrozenRows")).intValue();
    
    int frozenColumnPlacement;
    int frozenRowPlacement;
    
    if (numFrozenColumns != 0) {
        
        //frozen columns placed on the left returns 2
        //frozen columns placed on the right returns 4
        frozenColumnPlacement = ((Integer)jctable
                .invoke("getFrozenColumnPlacement")).intValue();
        
        //we only care if the columns are placed on the right.  in this case we
        //need to place these columns at the tail of the columnMap array
        if (frozenColumnPlacement == 4) {
            
            //create 2 arrays, a frozen array and a non-frozen array
        	int[] leftSideFrozenArray =
                new int[numFrozenColumns];
            int[] rightSideFrozenArray =
                new int[columnMap.length - numFrozenColumns];

            //these loops just initialize the frozen and non-frozen arrays
            for (int j = 0; j < numFrozenColumns; j++) {
                leftSideFrozenArray[j] = columnMap[j];
            }
            for (int j = numFrozenColumns; j < columnMap.length; j++) {
            	rightSideFrozenArray[j - numFrozenColumns] = columnMap[j];
            }
            
            //these loops perform a swap of the data inside the column map
            for (int j = 0; j < rightSideFrozenArray.length; j++) {
                columnMap[j] = rightSideFrozenArray[j];
            }
            for (int j = rightSideFrozenArray.length; j < columnMap.length; j++) {
                columnMap[j] = leftSideFrozenArray[j - rightSideFrozenArray.length];
            }
        }
    }
    
    if (numFrozenRows != 0) {
        
        //frozen rows placed on the top returns 1
        //frozen rows placed on the bottom returns 3
        frozenRowPlacement = ((Integer)jctable
                .invoke("getFrozenRowPlacement")).intValue();
        
        //we only care if the rows are placed on the bottom.  in this case we
        //need to place these rows at the tail of the rowMap array
        if (frozenRowPlacement == 3) {
            
        	//create 2 arrays, a frozen array and a non-frozen array
            int[] upperRowsFrozenArray = new int[numFrozenRows];
            int[] lowerRowsFrozenArray = new int[rowMap.length - numFrozenRows];

            //these loops just initialize the frozen and non-frozen arrays            
            for (int i = 0; i < numFrozenRows; i++) {
                upperRowsFrozenArray[i] = rowMap[i];
            }
            for (int i = numFrozenRows; i < rowMap.length; i++) {
                lowerRowsFrozenArray[i - numFrozenRows] = rowMap[i];
            }
            
            //these loops perform a swap of the data inside the row map            
            for (int i = 0; i < lowerRowsFrozenArray.length; i++) {
                rowMap[i] = lowerRowsFrozenArray[i];
            }
            for (int i = lowerRowsFrozenArray.length; i < rowMap.length; i++) {
                rowMap[i] = upperRowsFrozenArray[i - lowerRowsFrozenArray.length];
            }
        }
    }
  }
  
  protected Collection extractCellText (TestObject jctable, TestObject tblDataSource) {
    int i, j;
    Collection cells = new Vector();
    Object[] coords = new Object[2];
    Collection row;
        
    for (i = 0; i < rowMap.length; i++) {
        int rowMapNum = rowMap[i];
        row = new Vector();
        coords[0] = new Integer(rowMapNum);                
        String nstr = "";
        for (j = 0; j < columnMap.length; j++) {
            int columnMapNum = columnMap[j];
            coords[1] = new Integer(columnMapNum);
            Object cell = tblDataSource.invoke("getTableDataItem", "(II)", coords);                         
            if (cell == null) {
                nstr = "";
            }
            else {
                if (!(cell instanceof TestObject)
                        && !(cell.getClass().toString()
                                .startsWith("com.rational.test.ft.value"))) {
                    nstr = cell.toString();
                } else {
                	try {
                		Object ccont = ((TestObject) cell).getProperty("displayValue");
                        nstr = (String) (ccont == null ? "" : ccont.toString());
                    } catch (PropertyNotFoundException p) {
                        try {
                            Object ccont = ((TestObject) cell).getProperty("name");
                            nstr = (String) (ccont == null ? "" : ccont.toString());
                        } catch (PropertyNotFoundException p2) {
                            try {
                                Object t = ((TestObject) cell).invoke("toString");
                                Log.debug("cell.toString: " + t);
                                nstr = (t == null ? "" : "" + t);
                            } catch (Exception e3) {
                                nstr = "<Unknown-Cell-Type>";
                            }
                        }
                    }
                }
            }
            row.add(nstr);
        }
        cells.add(row);
    }
    return cells;
  }

  /** <br><em>Purpose:</em> set selection (NOTE: currently doesn't allow edit of cell when
   ** selected, looks like this is just a highlight, rather than a selection).
   ** Also does a click in the cell
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     row2, int
   * @param                     col2, int
   * @return                    boolean if success
   * @exception SAFSException based on caught Exception
   **/
  protected boolean setSelection (GuiTestObject jctable, int row, int col,
                                  int row2, int col2) throws SAFSException  {
    Log.info("* * * Inside setSelection with 5 parameters [coded 2nd] in cls CFJCTable.java * * *");

    return setSelection(jctable, row, col, row2, col2, true, false);
  }
  
    protected java.awt.Point getLocalCoords(GuiTestObject jctable, int row,
            int col)
    {
      int numFrozenColumns = ((Integer)jctable
                .invoke("getFrozenColumns")).intValue();
      int numFrozenRows = ((Integer)jctable
                .invoke("getFrozenRows")).intValue();
      
      java.awt.Point p = new java.awt.Point(0,0); 
	  Object[] coords = new Object[2];
      coords[0] = new Integer(numFrozenRows);
      coords[1] = new Integer(numFrozenColumns);
      
      jctable.invoke("makeVisible", "(II)", coords);

      coords[0] = new Integer(row);
      coords[1] = new Integer(col);
      
      Object[] currentCol = new Object[1];
      currentCol[0] = new Integer(col);
      
      Object[] currentRow= new Object[1];
      currentRow[0] = new Integer(row);
      
      boolean colVisible = ((Boolean)jctable.invoke("isColumnVisible", "(I)",
              currentCol)).booleanValue(); 
      boolean rowVisible = ((Boolean)jctable.invoke("isRowVisible", "(I)",
              currentRow)).booleanValue();
      

      
      if (numFrozenColumns != 0) {
          if (col < numFrozenColumns) {
              colVisible = true;
          }
      }
      
      if (numFrozenRows != 0) {
          if (row < numFrozenRows) {
              rowVisible = true;
          }
      }
      
      int width = ((Integer)jctable.getProperty("width")).intValue();      
      int height = ((Integer)jctable.getProperty("height")).intValue();
      
      int colPixelWidth = ((Integer)jctable.invoke("getColumnPixelWidth", "(I)",
              currentCol)).intValue();
      int rowPixelHeight = ((Integer)jctable.invoke("getRowPixelHeight", "(I)",
              currentRow)).intValue();
      
      java.awt.Point cellPosition = (java.awt.Point) jctable.invoke(
                "getCellPosition", "(II)", coords);
      
      if (colVisible && rowVisible) {
          p=cellPosition;
      }
      // the row is not visible, but the column is.  In this case we have to
      // move down to bring the row into view.
      else if (colVisible && !rowVisible) {
          jctable.invoke("makeVisible", "(II)", coords);
          int totRow = getRowTot(row,jctable);//total width to the col we want
          int rowPixOffScreen = height - (totRow + 20);//the negative number(we need to adjust for)
          int y = totRow + rowPixOffScreen;
          p.y = y;
          p.x = cellPosition.x;
      }
      // the column is not visible, but the row is.  In this case we have to
      // move to the right to bring the column into view.
      else if (!colVisible && rowVisible) {
          jctable.invoke("makeVisible", "(II)", coords);
          int tot = getColTot(col,jctable);//total width to the col we want
          int pixOffScreen = width - (tot + 20);//the negative number(we need to adjust for)
          int x = tot + pixOffScreen;
          p.setLocation(x,cellPosition.y);   
      }
      // neither the row or the column are visible.  In this case we have to
      // move to the right and move down to bring the cell into view.
      else { 
          jctable.invoke("makeVisible", "(II)", coords);
          int totCol = getColTot(col,jctable);//total width to the col we want
          int colPixOffScreen = width - (totCol + 20);//the negative number(we need to adjust for)
          int x = totCol + colPixOffScreen;
          int totRow = getRowTot(row,jctable);//total width to the col we want
          int rowPixOffScreen = height - (totRow + 20);//the negative number(we need to adjust for)
          int y = totRow + rowPixOffScreen;
          p.x = x;
          p.y = y;
      }
	return p;
  }
  
  
  protected boolean setLocalSelection (GuiTestObject jctable, int row, int col,
                                       int row2, int col2, boolean left, boolean right) throws SAFSException,
                                       com.rational.test.ft.UnsupportedActionException, MethodNotFoundException {

	Log.info("***  Inside the setLocalSelection ***");
	if (right) {
	  java.awt.Point p = getLocalCoords(jctable, row, col);
      Log.info("Right click at point.......p: " + p);
      jctable.click(script.RIGHT, p);
	} else if (left) {	// default click is left
	  Log.info("***  Inside the else if left ***");
	  java.awt.Point p = getLocalCoords(jctable, row, col);
      Log.info("Left click at point........p: " + p);
      jctable.click(p);
	} else {
	  Object[] coords = new Object[2];
	  coords[0]=new Integer(row);//row1
	  coords[1]=new Integer(col);//col1
	  jctable.invoke("makeVisible", "(II)Z", coords);
	  jctable.invoke("setCurrentCell", "(II)V", coords);

	  //boolean setSelection(int start_row, int start_column, int end_row, int end_column) 
	  coords = new Object[4];
	  coords[0]=new Integer(row);//row1
	  coords[1]=new Integer(col);//col1
	  coords[2]=new Integer(row2);//row2
	  coords[3]=new Integer(col2);//col2
	  jctable.invoke("setSelection", "(IIII)Z", coords);

	  //jctable.click(p);
	}

      return true;
  }
  
  
  /** <br><em>Purpose:</em> set selection <br>
   **  Does a click in the cell based on the result of invoking
   **  getCellPosition on the jctable.
   **  if this doesn't work, this method catches MethodNotFoundException
   **  and assumes that the parent is the table and tries again.
   **  this works if the mapped object is actually a 'CellArea' which is
   **  contained by the JCTable, and it was not possible to get to the JCTable itself.
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     row2, int
   * @param                     col2, int
   * @param                     left, boolean, is it a left click
   * @param                     right, boolean, is it a right click
   * @return                    boolean if success
   * @exception SAFSException based on caught Exception
   **/
  protected boolean setSelection (GuiTestObject jctable, int row, int col,
                                  int row2, int col2, boolean left, boolean right) throws SAFSException  {
	Log.info("***  Inside setSelection with 7 parameters [Coded 1 st] in cls CFJCTable  ***");
    try {
	  Log.info("***  Inside the try block on setSelection ***");
      jctable = (GuiTestObject) getJCTable(jctable);
      //listAllProperties(jctable, "inside setSelection [jctable]");
      //listMethods(jctable);
      return setLocalSelection(jctable, row, col, row2, col2, left, right);

    } catch (com.rational.test.ft.UnsupportedActionException uae) {
	  Log.info("***  Inside the 1st catch block on setSelection ***");
	 //uae.printStackTrace();
      Log.info("uae, attempting to clear cache and try again: "+uae);
      obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      jctable = (GuiTestObject) getJCTable(guiObj);

      return setLocalSelection(jctable, row, col, row2, col2, left, right);

    } catch (MethodNotFoundException mnfe) {
	  Log.info("***  Inside the 2nd catch block on setSelection ***");
      // try something else maybe, lets assume that the parent is the table...
      jctable = (GuiTestObject) getJCTable(jctable);
      jctable = (GuiTestObject) jctable.getProperty("parent");
      String classl = (String) jctable.getProperty("class");
      Log.info("*****************************-parent class: "+classl);
      //listAllProperties(jctable, "jctable");

	  return setLocalSelection(jctable, row, col, row2, col2, left, right);

    } catch (Exception e) {
      //e.printStackTrace();
      Log.info("* * * Caught an Exception in SetSelection with 7 parameters * * *");
      Log.info("Class Name: " + e.getClass().getName());
      throw new SAFSException(e.toString());
    }
  }
  /** <br><em>Purpose:</em> set text value of cell
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     text, String
   * @exception SAFSException based on caught Exception
   **/
  protected void setTextValue (TestObject jctable, int row, int col, String text) throws SAFSException {
    jctable = getJCTable(jctable);
    try {
      Object[] objs = new Object[3];
      objs[0] = new Integer(row);
      objs[1] = new Integer(col);
      objs[2] = text;
      TestObject dso = (TestObject)jctable.invoke("getDataSource");
      dso.invoke("setCell", "(IILjava/lang/Object;)V", objs);
    } catch (Exception e) {
      throw new SAFSException(e.toString());
    }
  }
  /** <br><em>Purpose:</em> actually do the click action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   **/
  protected void actionClick(GuiTestObject guiObj, int row, int col,
                             String rowval, String column) throws SAFSException {
	Log.info("***  Inside 2nd actionClick with 5 parameters [in cls CFJCTable] ***");
    try {
      if (action.equalsIgnoreCase(CLICK)) {
        //guiObj.click(cell);
        Log.info("***  Inside Click before calling setSelection ***");
        setSelection(guiObj, row, col, row, col, true, false);
        String altText = "setSelection ok: row: "+row+", col: "+col;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_5, altText,
                                          Integer.toString(row), Integer.toString(col),
                                          windowName, compName, action),
                       PASSED_MESSAGE);
      } else if (action.equalsIgnoreCase(RIGHTCLICK)) {
      	Log.info("* * * inside 1 st. else if right click * * *");
        setSelection(guiObj, row, col, row, col, false, true);
        //((GuiSubitemTestObject)guiObj).click(script.RIGHT);
        String altText = "rightClick ok: row: "+row+", col: "+col;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_5, altText,
                                          Integer.toString(row), Integer.toString(col),
                                          windowName, compName, action),
                       PASSED_MESSAGE);
      } else if (action.equalsIgnoreCase(DOUBLECLICK) ||
                 action.equalsIgnoreCase(ACTIVATECELL)) {
        //guiObj.doubleClick(cell);
        Log.info("* * * inside 2 nd. else if right click * * *");
        setSelection(guiObj, row, col, row, col, true, false); //?? where is doubleclick
	java.awt.Point clickPoint = getLocalCoords(guiObj, row, col);
	Log.info("Double click (in CFJCTable) at point.......clickPoint: " + clickPoint);
	guiObj.doubleClick(clickPoint);
        String altText = "setSelection ok: row: "+row+", col: "+col;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_5, altText,
                                          Integer.toString(row), Integer.toString(col),
                                          windowName, compName, action),
                       PASSED_MESSAGE);
      } else {
        //guiObj.setState(Action.select(), row); // this doesn't get the cell, we have to click...
        //guiObj.click(cell);
        Log.info("* * * inside Else Left click * * *");
        log.logMessage(testRecordData.getFac(),
                       "about to do: setSelection(guiObj, row, col); "+row+", "+col,
                       DEBUG_MESSAGE);
        setSelection(guiObj, row, col, row, col);
        String altText = "setSelection ok: row: "+row+", col: "+col;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_5, altText,
                                          Integer.toString(row), Integer.toString(col),
                                          windowName, compName, action),
                       PASSED_MESSAGE);
      }

      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (SubitemNotFoundException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column+
                "; msg: "+ex.getMessage(),
                FAILED_MESSAGE);
    }
  }
  /** <br><em>Purpose:</em> actually do the click action, this version takes 4 coords
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   **/
  protected void actionClick(GuiTestObject guiObj, int row, int col,
                             Integer rrow2, Integer ccol2,
                             String rowval, String column) throws SAFSException {
	Log.info("***  Inside 1 st actionClick with 7 parameters [in cls CFJCTable] ***");
    try {
      if (action.equalsIgnoreCase(CLICK)) {
        //guiObj.click(cell);
        setSelection(guiObj, row, col, rrow2.intValue(), ccol2.intValue(), true, false);
        String altText = "Click ok: row: "+row+", col: "+col+", row2: "+rrow2+", col2: "+ccol2;
        Collection p = new LinkedList();
        p.add(windowName); p.add(compName); p.add(action);
        p.add("row: ");         p.add(Integer.toString(row));
        p.add("col: ");         p.add(Integer.toString(col));
        p.add("row2: ");        p.add(rrow2==null?"":rrow2.toString());
        p.add("col2: ");        p.add(ccol2==null?"":ccol2.toString());
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText, p),
                       PASSED_MESSAGE);
      } else if (action.equalsIgnoreCase(RIGHTCLICK)) {
        setSelection(guiObj, row, col, rrow2.intValue(), ccol2.intValue(), false, true);
        //((GuiSubitemTestObject)guiObj).click(script.RIGHT);
        String altText = "rightClick ok: row: "+row+", col: "+col+", row2: "+rrow2+", col2: "+ccol2;
        Collection p = new LinkedList();
        p.add(windowName); p.add(compName); p.add(action);
        p.add("row: ");         p.add(Integer.toString(row));
        p.add("col: ");         p.add(Integer.toString(col));
        p.add("row2: ");        p.add(rrow2==null?"":rrow2.toString());
        p.add("col2: ");        p.add(ccol2==null?"":ccol2.toString());
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText, p),
                       PASSED_MESSAGE);
      } else if (action.equalsIgnoreCase(DOUBLECLICK) ||
                 action.equalsIgnoreCase(ACTIVATECELL)) {
        
        setSelection(guiObj, row, col, rrow2.intValue(), ccol2.intValue(), true, false); //?? where is doubleclick
        String altText = "setSelection ok: row: "+row+", col: "+col+", row2: "+rrow2+", col2: "+ccol2;
	java.awt.Point clickPoint = getLocalCoords(guiObj, row, col);
	Log.info("Double click (in CFJCTable) at point.......clickPoint: " + clickPoint);
	guiObj.doubleClick(clickPoint);
        Collection p = new LinkedList();
        p.add(windowName); p.add(compName); p.add(action);
        p.add("row: ");         p.add(Integer.toString(row));
        p.add("col: ");         p.add(Integer.toString(col));
        p.add("row2: ");        p.add(rrow2==null?"":rrow2.toString());
        p.add("col2: ");        p.add(ccol2==null?"":ccol2.toString());
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText, p),
                       PASSED_MESSAGE);
      } else {
        //guiObj.setState(Action.select(), row); // this doesn't get the cell, we have to click...
        //guiObj.click(cell);
        log.logMessage(testRecordData.getFac(),
                       "about to do: setSelection(guiObj, row, col); "+row+", "+col,
                       DEBUG_MESSAGE);
        setSelection(guiObj, row, col, rrow2.intValue(), ccol2.intValue());
        String altText = "setSelection ok: row: "+row+", col: "+col+", row2: "+rrow2+", col2: "+ccol2;
        Collection p = new LinkedList();
        p.add(windowName); p.add(compName); p.add(action);
        p.add("row: ");         p.add(Integer.toString(row));
        p.add("col: ");         p.add(Integer.toString(col));
        p.add("row2: ");        p.add(rrow2==null?"":rrow2.toString());
        p.add("col2: ");        p.add(ccol2==null?"":ccol2.toString());
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText, p),
                       PASSED_MESSAGE);
      }

      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (SubitemNotFoundException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column+
                "; msg: "+ex.getMessage(),
                FAILED_MESSAGE);
    }
  }
  protected int getRowOffset() {return 0;} // accounts for first row which contains dates
  /** <br><em>Purpose:</em> commandCaptureRangeToFile: process commands like: 
   ** CaptureRangeToFile, CaptureFuzzyRangeToFile
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JTable, CaptureRangeToFile, AFileName.txt
   ** <br> CaptureRange entire table To File AFileName.txt
   ** <br> 
   ** <br>T, JavaWin, JTable, CaptureRangeToFile, AFileName.txt, 4, "Field5", 2, 5
   ** <br>Saves a 2 rows by 5 columns range of cells starting at cell 4, Field5.
   ** <br>The starting column is determined by matching the text "Field5" to the field headers
   ** in the JTable. The values are saved into AFileName.txt
   ** <br>
   ** <br>T, JavaWin, JTable, CaptureRangeToFile, AFileName.txt, "ADatum", 5, , 2
   ** <br>Saves a range of all remaining rows by 2 columns wide starting at cell "ADatum", 5.
   ** <br>The starting row is determined by matching the text "ADatum" to the first cell
   ** in column 1 in the JTable that contains "ADatum". Since no ROWS parameter was provided,
   ** all subsequest rows from the 2 columns are captured. The values are saved into
   ** AFileName.txt 
   ** <p>
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   ** if matching cell values, use substrings)
   * @param                     fuzzy, boolean, if true then substrings or case insensitive match
   **/
  protected void commandCaptureRangeToFile (boolean fuzzy) throws SAFSException {
    try {
      // this should work in most cases, but if not, it falls throught to our impl.
      super.commandCaptureRangeToFile(fuzzy);
      return;
    } catch (com.rational.test.ft.MethodNotFoundException mnfe) {
      Log.info("mnfe: "+mnfe+", TRYING our overloaded method...");
    }
    int rowi = 0;
    int coli = 0;
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    Integer rrows = null;
    Integer ccols = null;
    String column = null;
    String rowval = null;
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      try {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator piter = params.iterator();
        String filename =  (String) piter.next();
        Log.info("...filename: "+filename);
        // second param will be the row number or contents
        if (piter.hasNext()) { // row
          String next = (String) piter.next();
          if (!next.trim().equals("")) {
            rrow = convertNum(next);
            if (rrow == null) { // assume looking for first cell with this content
              rowval = next;
            }
          }
        }
        // third param will be the col number, or col name
        if (piter.hasNext()) { // col
          String next = (String) piter.next();
          if (!next.trim().equals("")) {
            ccol = convertNum(next);
            if (ccol == null) { //assume it is a column name
              column = next;
            }
          }
        }
        // fourth param will be the rows
        if (piter.hasNext()) { // rows
          String next = (String) piter.next();
          if (!next.trim().equals("")) {
            try {
              rrows = new Integer(next);
              if (rrows.intValue() < 0) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                    getClass().getName()+": rows cannot be negative: "+next,
                    FAILED_MESSAGE);
                return;
              }
            } catch (NumberFormatException ee) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                  getClass().getName()+": rows number format exception: "+ee.getMessage(),
                  FAILED_MESSAGE);
              return;
            }
          }
        }
        // fifth param will be the cols
        if (piter.hasNext()) { // cols
          String next = (String) piter.next();
          if (!next.trim().equals("")) {
            try {
              ccols = new Integer(next);
              if (ccols.intValue() < 0) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                    getClass().getName()+": cols cannot be negative: "+next,
                    FAILED_MESSAGE);
                return;
              }
            } catch (NumberFormatException ee) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                  getClass().getName()+": cols number format exception: "+ee.getMessage(),
                  FAILED_MESSAGE);
              return;
            }
          }
        }

        Log.info(getClass().getName()+"."+action+": Row: "+rrow+", Col: "+ccol+", rowval: "+rowval+", colname: "+column+", rows: "+rrows+", colss:"+ccols);
        int col = 0;
        if (column != null) {
          col = getColi(fuzzy, column, guiObj);
        } else {
          col = ccol.intValue();
        }
        int row = 0;
        if (rowval != null) {
          Collection rowvals = new LinkedList();
          Collection rowvale = new LinkedList();
          rowvals.add(rowvale);
          rowvale.add("1");
          rowvale.add(rowval);
          row = getRowi(fuzzy, rowvals, guiObj);
        } else {
          row = rrow.intValue();
        }
        int absrows = getNumRows(guiObj); // get the actual rows
        int abscols = getNumColumns(guiObj); // actual
        if (rrows!=null && (row + rrows.intValue() < absrows)) {
          absrows = row + rrows.intValue();
        }
        if (ccols!=null && (col + ccols.intValue() < abscols)) {
          abscols = col + ccols.intValue();
        }
        java.util.List list = new LinkedList();

        Collection cells = getCells(guiObj);
	    if (!cells.isEmpty()){ 
	        absrows = cells.size();
	        abscols = ((Collection)((Vector)cells).elementAt(0)).size();
	        
	        for (rowi = row; rowi < absrows; rowi++) {
	          Collection entirerow = (Collection)((Vector)cells).elementAt(rowi);
	          Log.debug("....ENTIREROW:"+entirerow);
	          StringBuffer buf = new StringBuffer();
	          for(coli = col; coli < abscols; coli++) {
	            //Row nrow = script.localAtRow(rowi);
	            //Column ncol = script.localAtColumn(coli);
	            //Cell ncell = script.localAtCell(ncol, nrow);
	            //Object nsub = guiObj.getSubitem(ncell);
	            Object nsub = ((Vector)entirerow).elementAt(coli);
	            Log.debug("cel["+rowi+","+coli+"]: "+nsub);
	            String ssub = (String) (nsub == null ? "" : nsub.toString());
	            buf.append(ssub);
	            if (coli+1 < abscols) buf.append("\t");
	          }
	          list.add(buf.toString());
	        }
	     }
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
          StringUtils.writefile(filename, list);
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
                       passedText.convert(PRE_TXT_SUCCESS_4, altText, filename,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
        return;
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                  getClass().getName()+": item not found for num: "+ 
                  "row: "+rrow+", col: "+ccol+", or rowval: "+rowval+", or column name: "+column+
                  ", rows: "+rrows+", cols: "+ccols+
                  ", rowIndex: "+rowi+", colIndex: "+coli+
                  "; msg: "+ex.getMessage(),
                  FAILED_MESSAGE);
      }
    }
  }

  /** <br><em>Purpose:</em> GetCellCoordinates, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, GetCellCoordinates, 2, 4, xCoordVar, yCoordVar
   ** <br> Returns the X and Y coordinates value of row 2 and col 4 in JCTable.
   ** <p>
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     xCoordVariable, String
   * @param                     yCoordVariable, String
   * 
   **/
  protected void actionGetCellCoordinates (GuiTestObject guiObj, int row, int col,
                              String xCoordVariable, String yCoordVariable) throws SAFSException {
    java.awt.Point p = new java.awt.Point();
    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      //  get the x/y coords
      Object[] coords = new Object[2];
      coords[0]=new Integer(row);//row1
      coords[1]=new Integer(col);//col1
      p = (java.awt.Point) jctable.invoke("getCellPosition", "(II)Ljava/awt/Point;", coords);
      Log.info(".................p: "+p);
	  String xval = Integer.toString(p.x);
	  String yval = Integer.toString(p.y);
      if (!setVariable(xCoordVariable, xval) || !setVariable(yCoordVariable, yval)) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), " "+action+" failure to write variables",
            FAILED_MESSAGE);
        return;
      }
      // set status to ok
      String altText = windowName+":"+compName+" "+action+ ", row: "+row+", col: "+col;
      Collection list = new LinkedList();
      list.add(windowName); list.add(compName); list.add(action);
      list.add("row: ");         list.add(Integer.toString(row));
      list.add("col: ");         list.add(Integer.toString(col));
      log.logMessage(testRecordData.getFac(), passedText.convert(TXT_SUCCESS_3, altText, list),
                     PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (SubitemNotFoundException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+
                "; msg: "+ex.getMessage(),
                FAILED_MESSAGE);
    }
  }


  /** <br><em>Purpose:</em> VerifyRowLabel, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, VerifyRowLabel, "BenchValue", 2
   ** <br> Compares the value (Frozen row label) of row 2, in JCTable to bench text "BenchValue".
   ** <p>
   * @param                     guiObj, GuiTestObject
   * @param                     val, String
   * @param                     row, int
   **/
  protected void actionVerifyRowLabel (GuiTestObject guiObj, String val, int row) throws SAFSException {
    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);

	  TestObject dso = (TestObject)jctable.getProperty("dataSource");
      if (dso == null) {
        throw new SAFSException(action+", "+compName+", dataSource not setup yet for this grid");
      }

      Object[] coords = new Object[1];
      coords[0] = new Integer(row);	//row number or index default is zero
	  
	  Object rl = dso.invoke("getTableRowLabel", "(I)Ljava/lang/Object;", coords);
	  String rowLabel = "";
	  if (rl == null) {
		Log.debug("Given rowID (" + row + ") has no (null) label");
	  } else if (rl instanceof String) {
		rowLabel = (String) rl;
	  } else if (rl instanceof TestObject) {
		TestObject rlo = (TestObject) rl;
		//listAllProperties(rlo,"rlo"); To find out which property to use...
		rowLabel = (String) rlo.getProperty("text");
	  } else if (rl instanceof Integer) {
		rowLabel = rl.toString();
	  } else {
		Log.debug("Unknown class: "+rl.getClass().getName());
	  }

	  Log.debug("***** Maintain Imperssion Table Row label values for [Row= " + row + " ] : "+ rowLabel);

	  if (!val.equalsIgnoreCase(rowLabel.toString())) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
		" "+action+", VP failure, read property is: "+rowLabel+", compare value is: "+val, FAILED_MESSAGE);
	  } else {
		String altText =
		"read prop: "+rowLabel+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
		log.logMessage(testRecordData.getFac(), passedText.convert(PRE_TXT_SUCCESS_5, altText, rowLabel, val,
					   windowName, compName, action), PASSED_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);
	  }
      
	} catch (SubitemNotFoundException ex) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+ row + "; msg: "+ex.getMessage(), FAILED_MESSAGE);
	}


  }


  /** <br><em>Purpose:</em> VerifyColumnLabel, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, VerifyColumnLabel, "BenchValue", 3
   ** <br> Compares the value (Frozen column label) of column 3, in JCTable to bench text "BenchValue".
   ** <p>
   ** <br> NOTE: When you work with the column lable which consist of multiple column to find
   ** <br>       the next column label in the table you need to add one to the number of columns
   ** <br>       under the column label.
   ** <br>       
   ** <p> 
   * @param                     guiObj, GuiTestObject
   * @param                     val, String
   * @param                     col, int
   **/
  protected void actionVerifyColumnLabel (GuiTestObject guiObj, String val, int col) throws SAFSException {
    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
	  String methodName = "getJCTable";
	  String myclass = (String) jctable.getProperty("class");

	  TestObject dso = (TestObject)jctable.getProperty("dataSource");
      if (dso == null) {
        throw new SAFSException(action+", "+compName+", dataSource not setup yet for this grid");
      }

      Object[] coords = new Object[1];
      coords[0] = new Integer(col);	//row number or index default is zero

	  Object cl = dso.invoke("getTableColumnLabel", "(I)Ljava/lang/Object;", coords);
	  String colLabel = "";

	  if (cl == null) {
		Log.debug("Given colID (" + col + ") has no (null) label");
	  } else if (cl instanceof String) {
		colLabel = (String) cl;
	  } else if (cl instanceof TestObject) {
		TestObject clo = (TestObject) cl;
		colLabel = (String) clo.getProperty("text");
	  } else {
		Log.debug("Unknown class: "+ cl.getClass().getName());
	  }

	  Log.debug("*** Maintain Imperssion Table Col label values for [Col= " + col + " ] : "+ colLabel);

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
      
	} catch (SubitemNotFoundException ex) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "col: "+ col + "; msg: "+ ex.getMessage(), FAILED_MESSAGE);
	}

  }



  /** <br><em>Purpose:</em> getRowLabelY
   ** <br> This method let you implement the the custom calculation to find the y position for any 
   ** <br> frozen row labels in the JCTable. You can override this method in any of the custom class
   ** <br> which is inherited from the this (CFJCTable) class...!
   ** <br>  
   ** p> 
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     y, int
   **/
  protected int getRowLabelY(GuiTestObject jctable, int row, int y) { return y; }



 /** <br><em>Purpose:</em> ClickRowLabel, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, ClickRowLabel, 2
   ** <br> Make a single click on the specified forzen row label. In this example the keyword makes
   ** <br> click on the second forzen row label in the JCTable.
   ** <p>
   ** <br> T,windowName,TableName,doubleclickRowLabel,2
   ** <br> T,windowName,TableName,activateRowLabel,2 
   ** <br> Make a double click on the specified forzen row label. In this example the keyword makes
   ** <br> click on the second forzen row label in the JCTable.
   ** <p>
   ** <p>
   ** <br> NOTE: This keyword is implemeted using the co-ordinates.  This keyword internally calculates 
   ** <br>       the co-ordinates of any given frozen row label cell location and executes a single or double
   ** <br>       click on that frozen row label.
   ** <p>
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   **/
  protected void actionClickRowLabel (GuiTestObject guiObj, int row, int xoffset) throws SAFSException {

    try {

	  GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
	  java.awt.Point p = getLocalCoords(jctable, row, 0);
	  Log.info("Inside actionClickRowLabel...p: " + p);

	  p.x = (p.x / 2) + xoffset;
	  Log.info("***  Inside actionClickRowLabel..Adjucting.p: " + p);
	  
	  p.y = getRowLabelY(jctable, row, p.y);
      Log.debug("...Another Adj after calling method getRowLabelY in class MSAGrid...:" + p);

	  if (action.equalsIgnoreCase(CLICKROWLABEL)) {
		jctable.click(p);
		// set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
	  } else if (action.equalsIgnoreCase(DOUBLECLICKROWLABEL) ||
                 action.equalsIgnoreCase(ACTIVATEROWLABEL)) {
		jctable.doubleClick(p);
		// set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
	  }
	} catch (SubitemNotFoundException ex) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+ row + "; msg: "+ex.getMessage(), FAILED_MESSAGE);
	}


  }



  /** <br><em>Purpose:</em> ClickColumnLabel, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, ClickColumnLabel, 2
   ** <br> Make a single click on the specified forzen col label. In this example the keyword makes
   ** <br> click on the second forzen col label in the JCTable.
   ** <p>
   ** <br> T,windowName,TableName,DoubleClickColumnLabel,2
   ** <br> T,windowName,TableName,ActivateColumnLabel,2 
   ** <br> Make a double click on the specified forzen col label. In this example the keyword makes
   ** <br> click on the second forzen col label in the JCTable.
   ** <p>
   ** <p>
   ** <br> NOTE: This keyword is implemeted using the co-ordinates.  This keyword internally calculates 
   ** <br>       the co-ordinates of any given frozen col label cell location and executes a single or
   ** <br>       double click on that frozen col label.
   ** <p>
   * @param                     guiObj, GuiTestObject
   * @param                     col, int
   **/
  protected void actionClickColumnLabel (GuiTestObject guiObj, int col, int yoffset) throws SAFSException {

    try {

	  GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
	  java.awt.Point p = getLocalCoords(jctable, 0, col);
	  Log.info("Inside actionClickColumnlabel...p: " + p);

	  p.y = p.y / 2;
	  Log.info("***  Inside actionClickColumnlabel..Adjucting.p: " + p);

	  if (action.equalsIgnoreCase(CLICKCOLUMNLABEL)) {
		jctable.click(p);
		// set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
	  } else if (action.equalsIgnoreCase(DOUBLECLICKCOLUMNLABEL) ||
                 action.equalsIgnoreCase(ACTIVATECOLUMNLABEL)) {
		jctable.doubleClick(p);
		// set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
	  }

	} catch (SubitemNotFoundException ex) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "col: "+ col + "; msg: "+ex.getMessage(), FAILED_MESSAGE);
	}


  }



  /** <br><em>Purpose:</em> VerifyCellEditable, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, VerifyCellEditable, 2,5,true
   ** <br> Verifies the cell in JCTable, at row = 2, col = 5 is ediable. Validates the cell property
   ** <br> to a known value.
   ** <p> 
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param						verifyFlag, boolean
   **/
  protected void actionVerifyCellEditable (GuiTestObject guiObj, int row, int col, boolean verifyFlag) throws SAFSException {
  	Log.debug("***  [Inside] actionVerifyCellEditable on CFJCTable Class ***]");

    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      Boolean vis;

	  Object[] coords1 = new Object[3];
	  coords1[0] = new String();
	  coords1[1] = new Integer(row);
	  coords1[2] = new Integer(col);
	  
            Boolean isEditable = new Boolean(true);

	  Object[] coords = new Object[2];
	  coords[0] = new Integer(row);
	  coords[1] = new Integer(col);

            vis = (Boolean) jctable.invoke("isEditable", "(II)Z", coords);

	  if ((vis.booleanValue()) == verifyFlag) {
		String altText = "read prop: "+vis+", compare value is: "+verifyFlag+", "+windowName+":"+compName+" "+action;
		log.logMessage(testRecordData.getFac(), passedText.convert(PRE_TXT_SUCCESS_4, altText, windowName, compName, action, "read prop: "+vis+", compare value is: "+verifyFlag), PASSED_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);
	  } else {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
		" "+action+", VP failure, read property is: "+vis+", compare value is: "+verifyFlag, FAILED_MESSAGE);
	  }
	} catch (SubitemNotFoundException ex) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), getClass().getName()+": cell not found for row: "+ 
                row + ", col: "+ col + "; msg: "+ ex.getMessage(), FAILED_MESSAGE);
	}

  }


  protected void setLocalSelectionSpecial (GuiTestObject jctable, int row, int col,
                                       boolean control, boolean shift, boolean leftRight) throws SAFSException,
                                       com.rational.test.ft.UnsupportedActionException, MethodNotFoundException {

	java.awt.Point p = getLocalCoords(jctable, row, col);
	Log.info("***  [Inside setLocalSelectionSpecial] in class CFJCTable.java ... p: " + p);

	if (leftRight) {	//left is true
	  if (control) {
		jctable.click(script.CTRL_LEFT, p);
	  } else if (shift) {
		jctable.click(script.SHIFT_LEFT, p);
	  } else{
        jctable.click(p);
	  }
	} else {	//Right is true
	  if (control) {
		jctable.click(script.CTRL_RIGHT, p);
	  } else if (shift) {
		jctable.click(script.SHIFT_RIGHT, p);
	  } else{
        jctable.click(script.RIGHT, p);
	  }
	}

	String altText = windowName +":"+compName+" "+action+ ", row: "+row+", col: "+ col + ", control: "+ control
					 + ", shift: "+ shift + ", Right: "+ leftRight;

	Log.info("***  altText: " + altText);

	log.logMessage(testRecordData.getFac()," "+ action +" ok [" + row + "," + col + "]", PASSED_MESSAGE);

	testRecordData.setStatusCode(StatusCodes.OK);

  }


  
  /** <br><em>Purpose:</em> setSelectionSpecial <br>
   **  Does a click in the cell based on the result of invoking getCellPosition on the jctable.
   **  If this doesn't work, this method catches MethodNotFoundException and assumes that the
   **  parent is the table and tries again. This works if the mapped object is actually a 
   **  'CellArea' which is contained by the JCTable, and it was not possible to get to the JCTable
   **  itself.
   * 
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     row2, int
   * @param                     col2, int
   * @param                     left, boolean, is it a left click
   * @param                     right, boolean, is it a right click
   * @return                    boolean if success
   * @exception SAFSException based on caught Exception
   **/
  protected void setSelectionSpecial (GuiTestObject guiObj, int row, int col,
                             boolean control, boolean shift, boolean leftRight) throws SAFSException, 
                             SubitemNotFoundException {
    try {
	  Log.info("***  [Inside try block (setSelectionSpecial) in Class CFJCTable]  ***");
	  GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      //listAllProperties(jctable, "jctable");
      setLocalSelectionSpecial(jctable, row, col, control, shift, leftRight);
      //Status set to ok in the setLocalSelectionSpecial method itself...
    } catch (com.rational.test.ft.UnsupportedActionException uae) {
      //uae.printStackTrace();
      Log.info("uae, attempting to clear cache and try again: "+uae);
      obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
      guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      setLocalSelectionSpecial(jctable, row, col, control, shift, leftRight);
    } catch (MethodNotFoundException mnfe) {
	  Log.info("***  [Inside 2nd catch block (setSelectionSpecial) in Class CFJCTable] MNFE  ***");
      // try something else maybe, lets assume that the parent is the table...
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      jctable = (GuiTestObject) jctable.getProperty("parent");
      String classl = (String) jctable.getProperty("class");
      Log.info("*****************************-parent class: "+classl);
      //listAllProperties(jctable, "jctable");
	  setLocalSelectionSpecial(jctable, row, col, control, shift, leftRight);
    } catch (Exception e) {
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      e.printStackTrace();
      throw new SAFSException(e.toString());
    }
  }


  /** <br><em>Purpose:</em> actionSelectCellTextSpecial: process command click, with 
   ** 						Contorl or shirt key and left or right mouse button.
   ** 
   ** Example:custTableTable().click(SHIFT_LEFT, atCell(atRow(rowIndex), atColumn(colIndex)));
   ** Example:custTableTable().click(CTRL_LEFT, atCell(atRow(rowIndex), atColumn(colIndex)));
   ** <br> example step commands: (params are Control, row, then column index)
   ** <br> T,Frame,custTableTable,SelectCellTextSpecial,Control,Left, 2,3
   ** <br> T,Frame,orderTableTable,SelectCellTextSpecial,Control,Left,4,PHONE NUM
   ** <br> T,Frame,custTableTable,SelectCellTextSpecial,Shift,Left, 2,3
   ** <br> T,Frame,orderTableTable,SelectCellTextSpecial,Shift,Left,4,PHONE NUM
   ** <br> 
   ** <p> 
   ** <br> first param is the row and second param is column (both from 1), the col 
   **      could be the name of the column, if non-numeric fuzzy, is it a partial match
   **      on the column name or cell value.
   ** <br>
   ** <p> 
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or if matching
   **            cell values, use substrings)
   **/

 /** <br><em>Purpose:</em> actually do the right or left click action with Contorl or Shift key pressed.
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     control, boolean
   * @param                     shift, boolean
   * @param                     leftRight, boolean
   * 
   **/
  protected void actionSelectCellTextSpecial(GuiTestObject guiObj, int row, int col,
                             String rowval, String column, boolean control, 
                             boolean shift, boolean leftRight) throws SAFSException {
    try {
	  Log.info("***  [Inside try block (actionSelectCellTextSpecial) in Class CFJCTable]  ***");
	  setSelectionSpecial(guiObj, row, col, control, shift, leftRight);
	  // status to ok in the setLocalSelectionSpecial method...
	} catch (SubitemNotFoundException ex) {
	  Log.info("***  [Inside Catch block (actionSelectCellTextSpecial) in Class CFJCTable]  before setting the statuscode FAIL  ***");
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column
                + ", control: " + control + ", shift: " + shift + ", leftRight: " + leftRight +
                "; msg: "+ex.getMessage(),FAILED_MESSAGE);
    }
  }



  /** <br><em>Purpose:</em> GetTableRowColumnCount, this overrides the version in our parent
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, GetTableRowColumnCount, rowCountVar, colCountVar
   ** <br> Returns the total count of rows and columns in JCTable.
   ** <p>
   * @param                     guiObj, GuiTestObject
   * @param                     rowCountVariable, String
   * @param                     colCountVariable, String
   * 
   **/
  protected void actionGetTableRowColumnCount (GuiTestObject guiObj, String rowCountVariable, String colCountVariable) throws SAFSException {
  	Log.debug("***  Inside CFJCTable.java ***");
    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
      Integer numColumns = (Integer) jctable.getProperty("numColumns");
      Log.debug("***  numColumns: " + numColumns);
	  Integer numRows = (Integer) jctable.getProperty("numRows");
      Log.debug("***  numRows: " + numRows);

	  String rowVal = numRows.toString();
	  String colVal = numColumns.toString();
      if (!setVariable(rowCountVariable, rowVal) || !setVariable(colCountVariable, colVal)) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), " "+action+" failure to write variables", FAILED_MESSAGE);
        return;
      }
      // set status to ok
      String altText = windowName+":"+compName+" "+action+ ", row count: "+ rowCountVariable +", col count: "+ colCountVariable;
      Collection list = new LinkedList();
      list.add(windowName); list.add(compName); list.add(action);
      list.add("row count: ");         list.add(rowVal);
      list.add("col count: ");         list.add(colVal);
      log.logMessage(testRecordData.getFac(), passedText.convert(TXT_SUCCESS_3, altText, list), PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (PropertyNotFoundException pnfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName() + "; msg: "+ pnfe.getMessage(), FAILED_MESSAGE);
    }
  }

  protected void actionVerify (GuiTestObject guiObj, int row, int col,
                               String rowval, String column,
                               boolean fuzzy, String val) throws SAFSException {
    try {
        if (column != null && column.length() != 0) {
            col = getColi(fuzzy, column, guiObj);
        }
        String rval = getCell(guiObj, row, col).toString();
        
      if (action.equalsIgnoreCase(VERIFYCELLTEXT) ||
          action.equalsIgnoreCase(VERIFYFUZZYCELLTEXT) ||
          action.equalsIgnoreCase(VERIFYCELLTEXTFIND) ||
          action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTFIND)) {
        if (fuzzy) {
          if (!val.equalsIgnoreCase(rval.toString())) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                " "+action+", VP failure, read property is: "+rval+", compare value is: "+val,
                FAILED_MESSAGE);
            return;
          } else {
            String altText =
              "read prop: "+rval+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,
                                              windowName, compName, action),
                           PASSED_MESSAGE);
          }
        } else {
          if (!val.equals(rval.toString())) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                " "+action+", VP failure, read property is: "+rval+", compare value is: "+val,
                FAILED_MESSAGE);
            return;
          } else {
            String altText =
              "read prop: "+rval+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,
                                              windowName, compName, action),
                           PASSED_MESSAGE);
          }
        }
      } else if (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS) ||
                 action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTCONTAINS)) {
        if (fuzzy) {
          if (rval.toString().toLowerCase().indexOf(val.toLowerCase())<0) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                " "+action+", VPContains failure, read property is: "+rval+", compare value is: "+val,
                FAILED_MESSAGE);
            return;
          } else {
            String altText =
              "read prop: "+rval+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,
                                              windowName, compName, action),
                           PASSED_MESSAGE);
          }
        } else {
          if (rval.toString().indexOf(val)<0) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
                " "+action+", VPContains failure, read property is: "+rval+", compare value is: "+val,
                FAILED_MESSAGE);
            return;
          } else {
            String altText =
              "read prop: "+rval+", compare value is: "+val+", "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_5, altText, rval, val,
                                              windowName, compName, action),
                           PASSED_MESSAGE);
          }
        }
      } else {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),"unrecognized action: "+action,
            FAILED_MESSAGE);
        return;
      }
      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (SubitemNotFoundException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column+
                "; msg: "+ex.getMessage(),
                FAILED_MESSAGE);
    }
  }
  
  /** <br><em>Purpose:</em> get the column number based on fuzzy match
   * @param                     fuzzy, boolean
   * @param                     column, String
   * @param                     guiObj, GuiSubitemTestObject
   * @return                    Integer
   **/
  protected int getColi (boolean fuzzy, String column, GuiTestObject guiObj)
    throws SAFSException {
    
    TestObject tblDataSource = (TestObject)guiObj.invoke("getDataSource");
    
        // the columnMap and rowMap define the display of the columns and rows
        setDefaultColumnMap(guiObj);
        setDefaultRowMap(guiObj);
    
    purgeHiddenCells(guiObj);
    adjustForFrozenCells(guiObj);    
    
    try {
    	Object[] columnName = new Object[1];
        for (int coli = 0; coli < columnMap.length; coli++) {
          columnName[0]=new Integer(columnMap[coli]);//index
          String colName = (String)(Object)tblDataSource
		      .invoke("getTableColumnLabel", "(I)", columnName);
          
          if (colName == null) { 
            colName = "";
          }
            
          if (fuzzy) {
            if (colName.toLowerCase().indexOf(column.toLowerCase())>=0) {
              log.logMessage(testRecordData.getFac(),
                  "Found col: "+colName,
                  DEBUG_MESSAGE);
              return coli;
            }
          } else {
            if (colName.equals(column)) {
              log.logMessage(testRecordData.getFac(), "Found col: "+colName,
                    DEBUG_MESSAGE);
              return coli;
            }
          }
        }
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        throw new SAFSException(this, "getColi",
        		" failure, column not found in "+(fuzzy?"fuzzy ":"")+"match: "+column);
    } catch (PropertyNotFoundException pnf) {
    	throw new SAFSException("prop not found: "+pnf.toString());
    }
  }
  
    /**
     * @param columnMap
     */
    protected void setDefaultColumnMap(TestObject jctable) {
        columnMap = (int[])(((TestObject)(jctable.invoke("getDataView")))
                .invoke("getColumnMap")); 
        try {
            if (columnMap == null) {
                int numColumns = getNumColumns(jctable);
                columnMap = new int[numColumns];
                for (int j = 0; j < numColumns; j++) {
                    columnMap[j] = j;
                }
            }
        }
        catch (Exception e) {
            // nothing for now
        }

    }
    /**
     * @param rowMap
     */
    protected void setDefaultRowMap(TestObject jctable) {
        
        rowMap = (int[]) (((TestObject)(jctable.invoke("getDataView")))
                .invoke("getRowMap"));
        
        if (rowMap == null) {
            try {
                int numRows = getNumRows(jctable);
                rowMap = new int[numRows];
                for (int i = 0; i < numRows; i++) {
                    rowMap[i] = i;
                }
            }
            catch (Exception e) {
                // nothing for now
            }
        }
    }
    protected int getNumRows (TestObject jctable) throws SAFSException  {
       
        try {   
	        jctable = getJCTable(jctable);
	        TestObject dso = (TestObject)jctable.invoke("getDataSource");
	        Integer i = (Integer) dso.getProperty("numRows");
	        return i.intValue();} 
        catch (Exception e2) {
               throw new SAFSException(e2.toString()); }
      }

    protected int getColTot(int col, GuiTestObject jctable) {
        int tot = 0; // some minimal pixel offset to the column
        for(int c=0; c<col; c++) {
            Object[] colob = new Object[1];
            colob[0] = new Integer(c);//row number or index default is zero
            int colPixWidth = ((Integer)jctable.invoke("getColumnPixelWidth", "(I)",
                    colob)).intValue();
            tot += colPixWidth;
        }
        Log.info("** total width: "+tot);
        return tot;
      }
    protected int getRowTot(int row, GuiTestObject jctable) {
        int tot = 0; // some minimal pixel offset to the column
        try{
	        for(int c=0; c<row; c++) {
	            Object[] rowob = new Object[1];
	            rowob[0] = new Integer(c);//row number or index default is zero
	            int rowPixWidth = ((Integer)jctable.invoke("getRowPixelHeight", "(I)",
	                    rowob)).intValue();
	            tot += rowPixWidth;
	        }
        }catch(MethodNotFoundException mnfe){
            Log.info("** total height failure  ");
            tot = 0;
        }
        Log.info("** total width: "+tot);
        return tot;
      }
} // End of Class
