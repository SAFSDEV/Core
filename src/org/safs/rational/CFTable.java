/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.Cell;
import com.rational.test.ft.script.Column;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.Row;

/**
 * <br><em>Purpose:</em> CFTable, process a Table component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 18, 2003
 *
 *   <br>   JUL 14, 2003    (DBauman) Original Release
 *   <br>   JAN 07, 2004    (BNat) Added New Keyword GETCELLCOORDINATES
 *	 <br>   JAN 19, 2004    (BNat) Added New Keywords VERIFYROWLABEL,VERIFYCOLUMNLABEL
 *   <br>   FEB 04, 2004	(BNat) Added New Keywords CLICKROWLABEL, DOUBLECLICKROWLABEL, ACTIVATEROWLABEL
 * 													  CLICKCOLUMNLABEL, DOUBLECLICKCOLUMNLABEL, ACTIVATECOLUMNLABEL.
 *   <br>   FEB 06, 2004	(BNat) Added New Keyword VERIFYCELLEDITABLE
 *   <br>   FEB 09, 2004	(BNat) Added New Keyword SELECTCELLTEXTSPECIAL
 *   <br>   FEB 10, 2004	(BNat) Added New Keyword GETTABLEROWCOLUMNCOUNT 
 *   <br>   AUG 30, 2004	(CANAGL) VerifyCellTextContains fixed to be case-insensitive
 *   <br>   DEC 09, 2004    (DBauman) now implement VerifyColumnLabel
 *   <br>   DEC 16, 2004    (DBauman) now implement ClickColumnLabel,DoubleClickColumnLabel, ActivateColumnLabel,
 *   <br>   DEC 16, 2004    (DBauman) now implement VerifyCellEditable, except it doesn't work for JTable (does work for a custom extending table)
 *   <br>   OCT 17, 2005    (CANAGL) Changed Click variants to ClickCell variants.
 *   <br>   OCT 28, 2005    (CANAGL) performClick extended to support Row=N;Col=N syntax for CFComponent
 *   <br>   FEB 08, 2006    (CANAGL) fixed getCellText to work with tables containing String primitives.
 *   <br>   FEB 12, 2006    (CANAGL) fixed actionVerify for HTMLTables. getModel method does NOT apply!
 * 	 <br>   APR 09, 2008    (LeiWang) 	Reorder keyword constant alphabetically;
 * 										Change script to Script when calling static method of Script;
 * 										Use "triple click" to imitate "SELECT action", SELECTCELL SELECTCELLCONTAINSTEXTFIND 
 * 																						SELECTCELLTEXTFIND SELECTCELLTEXT
 * 																						SELECTFUZZYCELLTEXT SELECTFUZZYCELLTEXTFIND
 * 	 <br>   APR 11, 2008    (LeiWang) 	Added keyword ASSIGNCELLCONTAINSTEXTROW and ASSIGNCELLTEXTROW	
 * 	 <br>   AUG 05, 2008    (CANAGL) 	Modified to use normal cell click for "SELECT" actions
 *   <br>	SEP 09, 2008	(LeiWang)	Modified method actionAssign(): if cell text is null, return "" instead of 
 *   									considering the action is fail. See defect S0532491.
 *   									Modified method getCellText(TestObject,TestObject,int,int): if table cell contains
 *   									a java.util.Date object, it will be return as an object of com.rational.test.ft.value.DateWrapper,
 *   									but this object can not give the proper string displayed in the table. So use java swing 
 *   									TableCellRendererComponent to get the text firstly, if this fail then use the RFT DateWrapper
 *   									to get the cell text. See defect S0532493.
 *   									Modify method getRowi(), use getCellText() to get cell's text. see defect S0532491.
 *   <br>	NOV 30, 2010	(JunwuMa)	Update to handle NullPointerException thrown by getMousePosition() in actionClickColumnLabel.
 *   <br>	JAN 18, 2011	(DharmeshPatel) Added New Keywords RIGHTCLICKCOLUMNLABEL.	 								
 * @safsinclude CFTable.include.htm
 *
 **/
public class CFTable extends CFComponent {
	
	public static final String ACTIVATECELL 						= "ActivateCell";
	public static final String ASSIGNCELLCONTAINSTEXTROW			= "AssignCellContainsTextRow";
	public static final String ASSIGNCELLTEXTROW					= "AssignCellTextRow";
	
	public static final String ACTIVATECOLUMNLABEL 					= "ActivateColumnLabel";
	public static final String ACTIVATEROWLABEL 					= "ActivateRowLabel";
	public static final String ASSIGNVARIABLECELLTEXT 				= "AssignVariableCellText";
	public static final String ASSIGNVARIABLEFUZZYCELLTEXT 			= "AssignVariableFuzzyCellText";
	
	public static final String CAPTURERANGETOFILE 					= "CaptureRangeToFile";
	public static final String CAPTUREFUZZYRANGETOFILE 				= "CaptureFuzzyRangeToFile";
	public static final String CLICKCELL 							= "ClickCell";
	public static final String CLICKCELLOFCOLWITHROWVALUES 			= "ClickCellOfColWithRowValues";
	public static final String CLICKCOLUMNLABEL 					= "ClickColumnLabel";
	public static final String RIGHTCLICKCOLUMNLABEL				= "RightClickColumnLabel";
	public static final String CLICKROWLABEL 						= "ClickRowLabel";
  
	public static final String DOUBLECLICKCELLOFCOLWITHROWVALUES 	= "DoubleClickCellOfColWithRowValues";
	public static final String DOUBLECLICKCELL 						= "DoubleClickCell";
	public static final String DOUBLECLICKCOLUMNLABEL 				= "DoubleClickColumnLabel";
	public static final String DOUBLECLICKROWLABEL 					= "DoubleClickRowLabel";
	
	public static final String GETCELLCOORDINATES 					= "GetCellCoordinates";
	public static final String GETTABLEROWCOLUMNCOUNT 				= "GetTableRowColumnCount";
	
	public static final String RIGHTCLICKCELL 						= "RightClickCell";
	
	public static final String SELECTCELL 							= "SelectCell";
	public static final String SELECTCELLCONTAINSTEXTFIND	 		= "SelectCellContainsTextFind";
	public static final String SELECTCELLTEXTFIND 					= "SelectCellTextFind";
	public static final String SELECTCELLTEXT 						= "SelectCellText";
	public static final String SELECTCELLTEXTSPECIAL 				= "SelectCellTextSpecial";
	public static final String SELECTFUZZYCELLTEXT 					= "SelectFuzzyCellText";
	public static final String SELECTFUZZYCELLTEXTFIND 				= "SelectFuzzyCellTextFind";
	public static final String SELECTROWWITHROWVALUES 				= "SelectRowWithRowValues";
	
	public static final String VERIFYCELLEDITABLE 					= "VerifyCellEditable";
	public static final String VERIFYCELLTEXT 						= "VerifyCellText";
	public static final String VERIFYCELLTEXTFIND 					= "VerifyCellTextFind";
	public static final String VERIFYCELLTEXTCONTAINS 				= "VerifyCellTextContains";
	public static final String VERIFYCOLUMNLABEL 					= "VerifyColumnLabel";
	public static final String VERIFYFUZZYCELLTEXT 					= "VerifyFuzzyCellText";
	public static final String VERIFYFUZZYCELLTEXTCONTAINS 			= "VerifyFuzzyCellTextContains";
	public static final String VERIFYFUZZYCELLTEXTFIND 				= "VerifyFuzzyCellTextFind";
	public static final String VERIFYROWLABEL 						= "VerifyRowLabel";

	
	public static final String RFTGENERALVALUEPACKAGENAME			= "com.rational.test.ft.value";
	protected String[] customCellObjects = {"com.rational.test.ft.value.DateWrapper","com.rational.test.ft.value.FontInfo"};
  
	public String getTextProperty() {return "text";}
  /**
   * <br>
   * <em>Purpose:</em> constructor, calls super
   */
  public CFTable () {
    super();
  }

  /**
   * <br>
   * <em>Purpose:</em> process: process the testRecordData *<br>
   * This is our specific version. We subclass the generic CFComponent. * We
   * first call super.process() [which handles actions like 'click'] * The types
   * of objects handled here are '{@link GuiSubitemTestObject}'. *
   * Example:custTableTable().click(atCell(atColumn("PHONE"), * atRow("NAME",
   * "Jill Sanford", * "CUSTID", "19", "STREET", * "80 West End"))); * The
   * actions handled here are: *<br>
   * <ul>*
   * <li>clickCellOfColWithRowValues - first param specifies a point (pixel
   * coords within cell, indexed * from 1, row separated by comma or semicolon,
   * then column, or an AppMap reference name), * if omitted then the coords are
   * not used to do the click, second param is the column name, * remaining
   * params are the name/value pairs for the row specification for the cell *
   * <li>doubleclickCellOfColWithRowValues - first params specifies a point
   * (pixel coords within cell, * indexed from 1, row separated by comma or
   * semicolon, then column,or an AppMap reference name), * if omitted then the
   * coords are not used to do the click, second param is the column name, *
   * remaining params are the name/value pairs for the row specification for the
   * cell *
   * <li>click - first param is the row and column index (from 1) or AppMap
   * reference name *
   * <li>doubleclick - first param is the row and column index (from 1) or
   * AppMap reference name *
   * <li>activatecell - same as doubleclick *
   * <li>rightclick - first param is the row and column index (from 1) or
   * AppMap reference name *
   * <li>selectcell - select based on row and column index (from 1) or AppMap
   * reference name *
   * <li>selectcelltext - select based on row and column index (from 1) or
   * AppMap reference name *
   * <li>selectfuzzycelltext - select based on fuzzy column, or row and column
   * index (from 1) or AppMap reference name *
   * <li>selectRowWithRowValues - all of the params are the name/value * pairs
   * for the row specification for the row to select *
   * <li>verifycelltext - Attempts to verify a particular cell value, based on
   * several formats *
   * <li>verifycelltextcontains - Attempts to verify a particular cell value
   * substring, based on several formats *
   * <li>verifyfuzzycelltext - Attempts to verify a particular 'fuzzy cell'
   * value, based on several formats *
   * <li>verifyfuzzycelltextcontains - Attempts to verify a particular 'fuzzy
   * cell' value substring, based on several formats *
   * <li>assignvariablecelltext - assign the value of the cell text to the
   * variable *
   * <li>assignvariablefuzzycelltext - assign the value of the 'fuzzy cell'
   * text to the variable *
   * <li>capturerangetofile - capture range to a file *
   * <li>capturefuzzyrangetofile - capture 'fuzzy' range to a file
   *  *
   * </ul>
   * <br>
   * <br>
   * <em>Side Effects:</em> {@link #testRecordData}statusCode is set based on
   * the result of the processing <br>
   * <em>State Read:</em> {@link #testRecordData},{@link #params}<br>
   * <em>Assumptions:</em> none
   */
  protected void localProcess() {
    try {
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+".process, searching specific tests...",
          DEBUG_MESSAGE);

      if (action != null) {
        Log.info(".....CFTable.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CLICKCELLOFCOLWITHROWVALUES) ||
            action.equalsIgnoreCase(DOUBLECLICKCELLOFCOLWITHROWVALUES)) {
          commandClickCellOfColWithRowValues();
        } else if (action.equalsIgnoreCase(CLICKCELL) ||
                   action.equalsIgnoreCase(DOUBLECLICKCELL) ||
                   action.equalsIgnoreCase(ACTIVATECELL) ||
                   action.equalsIgnoreCase(RIGHTCLICKCELL)) {
          commandClick(false);
        } else if (action.equalsIgnoreCase(SELECTROWWITHROWVALUES)) {
          commandSelectRowWithRowValues();
        } else if (action.equalsIgnoreCase(SELECTCELL) ||
                   action.equalsIgnoreCase(SELECTCELLTEXT)) {
          selectCellText(false);
        } else if (action.equalsIgnoreCase(SELECTCELLTEXTFIND)) {
          commandClickFind(false);
        } else if (action.equalsIgnoreCase(SELECTFUZZYCELLTEXT)) {
          selectCellText(true);
        } else if (action.equalsIgnoreCase(SELECTFUZZYCELLTEXTFIND)||
        		action.equalsIgnoreCase(SELECTCELLCONTAINSTEXTFIND)) {
          commandClickFind(true);
        } else if (action.equalsIgnoreCase(VERIFYCELLTEXT)) {
          commandVerifyCellText(false);
        } else if (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS)) {
          commandVerifyCellText(true);
        } else if (action.equalsIgnoreCase(VERIFYCELLTEXTFIND)) {
          commandVerifyCellTextFind(false);
        } else if (action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTFIND)) {
          Log.index("fuzzyfind:"+action);
          commandVerifyCellTextFind(true);
        } else if (action.equalsIgnoreCase(VERIFYFUZZYCELLTEXT) ||
                   action.equalsIgnoreCase(VERIFYFUZZYCELLTEXTCONTAINS)) {
          commandVerifyCellText(true);
        } else if (action.equalsIgnoreCase(ASSIGNVARIABLECELLTEXT)) {
          commandAssignVariableCellText(false);
        } else if (action.equalsIgnoreCase(ASSIGNVARIABLEFUZZYCELLTEXT)) {
          commandAssignVariableCellText(true);
        } else if (action.equalsIgnoreCase(CAPTURERANGETOFILE)) {
          commandCaptureRangeToFile(false);
        } else if (action.equalsIgnoreCase(CAPTUREFUZZYRANGETOFILE)) {
          commandCaptureRangeToFile(true);
        } else if (action.equalsIgnoreCase(GETCELLCOORDINATES)) {
          commandGetCellCoordinates();
		} else if (action.equalsIgnoreCase(VERIFYROWLABEL)) {
          commandVerifyRowLabel();
		} else if (action.equalsIgnoreCase(CLICKROWLABEL) ||
                   action.equalsIgnoreCase(DOUBLECLICKROWLABEL) ||
                   action.equalsIgnoreCase(ACTIVATEROWLABEL)) {
		  commandClickRowLabel();
		} else if (action.equalsIgnoreCase(VERIFYCOLUMNLABEL)) {
          commandVerifyColumnLabel();
		} else if (action.equalsIgnoreCase(CLICKCOLUMNLABEL) ||
                   action.equalsIgnoreCase(DOUBLECLICKCOLUMNLABEL) ||
                   action.equalsIgnoreCase(ACTIVATECOLUMNLABEL) ||
                   action.equalsIgnoreCase(RIGHTCLICKCOLUMNLABEL)) {
		  commandClickColumnLabel();
		} else if (action.equalsIgnoreCase(VERIFYCELLEDITABLE)) {
          commandVerifyCellEditable();
		} else if (action.equalsIgnoreCase(SELECTCELLTEXTSPECIAL)) {
          commandSelectCellTextSpecial(false);
        } else if (action.equalsIgnoreCase(GETTABLEROWCOLUMNCOUNT)) {
          commandGetTableRowColumnCount();
        }else if(action.equalsIgnoreCase(ASSIGNCELLCONTAINSTEXTROW)){
        	commandAssignCellTextRow(true);
        }else if(action.equalsIgnoreCase(ASSIGNCELLTEXTROW)){
        	commandAssignCellTextRow(false);
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
      //ex.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          "SAFSException: "+ex.getMessage(),
          FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> commandClickCellOfColWithRowValues: process commands like:
   ** clickCellOfColWithRowValues, doubleclickCellOfColWithRowValues
   ** Example:custTableTable().click(atCell(atColumn("PHONE"),
   **                                       atRow("NAME", "Jill Sanford", 
   **                                             "CUSTID", "19", "STREET", 
   **                                             "80 West End"))); // with no coords
   ** Example:custTableTable().click(atCell(atColumn("PHONE"),
   **                                       atRow("NAME", "Jill Sanford", 
   **                                             "CUSTID", "19", "STREET", 
   **                                             "80 West End")),
   **                                atPoint(4,5)); // with coords
   ** <br> example step commands:
   ** <br> T,Frame,custTableTable,clickCellOfColWithRowValues,,PHONE,CUSTID,5,NAME,Susan Flontly,STREET,750 Central Expy
   ** <br> T,Frame,orderTableTable,clickCellOfColWithRowValues,,QUANTITY,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
   ** <br> T,Frame,orderTableTable,clickCellOfColWithRowValues,44;15,QUANTITY,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
   ** <br> T,Frame,orderTableTable,clickCellOfColWithRowValues,"Coords=44;15",QUANTITY,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
   ** <br> first param is cell pixel xoffset and yoffset
   ** <br> second param is the column name, remaining params are the name/value
   ** pairs for the row specification for the cell
   **/
  protected void commandClickCellOfColWithRowValues () throws SAFSException {
    if (params.size() < 4) {
      paramsFailedMsg(windowName, compName);
    } else {
      String column = null;
      String name1 = null;
      String val1 = null;
      String name2 = null;
      String val2 = null;
      String name3 = null;
      String val3 = null;
      java.awt.Point point = null;
      try {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        Iterator piter = params.iterator();
        // first param will be the point or coordinates
        if (piter.hasNext()) { // row;col
          String coord = (String) piter.next();
          if (!coord.trim().equals("")) { // then leave point null and continue
          	  // try locating the app map reference
              point = lookupAppMapCoordReference(coord);
            if (point == null) { 
            	//try literal text
	            point = convertCoords(coord);
            }
            if (point == null) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              // no message as to WHY (bad Coords) this is failing?
              return;
            }
          }
        }

        column = (String) piter.next();
        log.logMessage(testRecordData.getFac(),"column: "+column,
            DEBUG_MESSAGE);
        // get two at a time, the name/value row pairs
        if (piter.hasNext()) {
          name1 = (String) piter.next();
          if (piter.hasNext()) {
            val1 = (String) piter.next();
            if (piter.hasNext()) {
              name2 = (String) piter.next();
              if (piter.hasNext()) {
                val2 = (String) piter.next();
                if (piter.hasNext()) {
                  name3 = (String) piter.next();
                  if (piter.hasNext()) {
                    val3 = (String) piter.next();
                  }
                }
              }
            }
          }
        }
        Row row = null;
        if (name3 !=null && val3 != null) {
          row = Script.localAtRow(name1, val1, name2, val2, name3, val3);
        } else if (name2 !=null && val2 != null) {
          row = Script.localAtRow(name1, val1, name2, val2);
        } else if (name1 !=null && val1 != null) {
          row = Script.localAtRow(name1, val1);
        }
        Column col = Script.localAtColumn(column);
        Cell cell = null;
        if (row == null) {
          log.logMessage(testRecordData.getFac(),getClass().getName()+": no row specified",
              FAILED_MESSAGE);
          return;
        } else {
          cell = Script.localAtCell(col, row);
        }
        if (action.equalsIgnoreCase(CLICKCELLOFCOLWITHROWVALUES)) {
          if (point == null) {
            guiObj.click(cell);
          } else {
            guiObj.click(cell, point);
          }
        } else if (action.equalsIgnoreCase(DOUBLECLICKCELLOFCOLWITHROWVALUES)) {
          if (point == null) {
            guiObj.doubleClick(cell);
          } else {
            guiObj.doubleClick(cell, point);
          }
        }
        // set status to ok
        String altText = windowName+":"+compName+" "+action;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                  getClass().getName()+": item not found: column: "+column+
                  ", name1: "+name1+ ", val1: "+val1+ ", name2: "+name2+
                  ", val2: "+val2+ ", name3: "+name3+ ", val3: "+val3+
                  (point== null ? "" :
                   ", cell.xoffset: "+point.x+", cell.yoffset: "+point.y)+
                  "; msg: "+ex.getMessage(),
                  FAILED_MESSAGE);
      }
    }
  }

  /** 
   * Process commands like: clickcell, doubleclickcell, activatecell
   * Example:custTableTable().click(atCell(atRow(rowIndex), atColumn(colIndex)));
   * <br> first param is the row and second param is column (both from 1), the col could be the name of the column, if non-numeric
   * @param                     fuzzy, is it a partial match on the column name or cell value
   * <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   * if matching cell values, use substrings)
   **/
  protected void commandClick (boolean fuzzy) throws SAFSException {
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    Integer rrow2 = null;
    Integer ccol2 = null;
    String column = null;
    String rowval = null;
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      // first param will be the row or appmap name of row
      Iterator piter = params.iterator();
      if (piter.hasNext()) { // row
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          rrow = convertNum(next);
          if (rrow == null) { // assume looking for first cell with this content
            rowval = next;
          }
        }
      }
      // second param will be the col number, or col name
      if (piter.hasNext()) { // col
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          ccol = convertNum(next);
          if (ccol == null) { //assume it is a column name
            column = next;
          }
        }
      }
      // optional: row2
      if (piter.hasNext()) { // row
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          rrow2 = convertNum(next);
        }
      }
      // optional: col2
      if (piter.hasNext()) { // col
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          ccol2 = convertNum(next);
        }
      }

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
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowval+", colname: "+column,
          DEBUG_MESSAGE);

      if (rrow2 != null && ccol2 != null) {
        actionClick(guiObj, row, col, rrow2, ccol2, rowval, column);
      } else {
        actionClick(guiObj, row, col, rowval, column);
      }
  }

  /** 
   * Example:custTableTable().click(atCell(atRow(rowIndex), atColumn(colIndex)));
   * first param is the row and second param is column (both from 1), 
   * the col could be the name of the column, if non-numeric
   * the row could be the value of a cell in col #1.
   **/
  protected void selectCellText (boolean fuzzy) throws SAFSException {
      Integer rrow = new Integer(0);
      Integer ccol = new Integer(0);
      String column = null;
      String rowval = null;
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      // first param will be the row or appmap name of row
      Iterator piter = params.iterator();
      if (piter.hasNext()) { // row
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          //convert 1-based to 0-based index
          rrow = convertNum(next); 
          if (rrow == null) { 
        	// assume looking for first col1 cell with this content
            rowval = next;
          }
        }
      }
      // second param will be the col number, or col name
      if (piter.hasNext()) { // col
        String next = (String) piter.next();
        if (!next.trim().equals("")) {
          ccol = convertNum(next);
          if (ccol == null) { //assume it is a column name
            column = next;
          }
        }
      }
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
      Log.debug(action+": Row: "+row+", Col: "+col+", rowval: "+rowval+", colname: "+column);
      actionClick(guiObj, row, col, rowval, column);
  }

  /**
   * Overrides CFComponent performClick to intercept TableCell specs.
   * TableCell specifies "Row=n;Col=n" where normally Coords=x,y would 
   * be issued.
   * @param point Object if instanceof TableCell, handle here; otherwise
   *               handle in CFComponent superclass.
   */
  protected void performClick(Object point){
  	if ((point == null)||(!(point instanceof TableCell))){
  		if (point==null) point = new java.awt.Point(1,1);
  		super.performClick(point);
  		return;
  	}

  	TableCell tcell = (TableCell) point;
    String altText = windowName+":"+compName+" "+action+
    	   " successful using Row="+ tcell.y +" Col="+ tcell.x;

    GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
    try{
  		Cell cell = getCell(tcell.y-1, tcell.x-1);  		
	    if (action.equalsIgnoreCase(CLICK)||action.equalsIgnoreCase(COMPONENTCLICK)) {
    	    guiObj.click(cell);
  		} else if (action.equalsIgnoreCase(RIGHTCLICK)) {
    		guiObj.click(Script.RIGHT, cell);
  		} else if (action.equalsIgnoreCase(DOUBLECLICK)) {
    		guiObj.doubleClick(cell);
  		} else {
    		Row trow = Script.localAtRow(tcell.y-1);
//    		guiObj.setState(Action.select(), trow); // this doesn't get the cell, we have to click...
//    		guiObj.click(cell);
    		guiObj.nClick(3,Script.LEFT,cell,new Point(3,3));
  		}
  		// set status to ok
      	Collection p = new LinkedList();
  		p.add(windowName); p.add(compName); p.add(action);
  		p.add("Row="+Integer.toString(tcell.y)+ " Col="+Integer.toString(tcell.x));
  		log.logMessage(testRecordData.getFac(),
                 passedText.convert("success3a", altText, p),
                 GENERIC_MESSAGE);
  		testRecordData.setStatusCode(StatusCodes.OK);
  		return;
    }catch(Exception x){
    	// certain HTML cases will not work correctly?
    	// allow another engine to try them out?
	    Log.info(action +" failed. Flagging NOT_EXECUTED. "+x.toString());
	  	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	  	return;
    }
  }

  /** <br><em>Purpose:</em> commandClickFind: process commands like: click, doubleclick, activatecell
   ** <br> example step commands:
   ** <br> T,Frame,custTableTable,selectCellTextFind,rowIndexVar,3, FindCol1, FindVal1, FindCol2, FindVal2
   ** <br> T,Frame,orderTableTable,selectCellTextFind,rowIndexVar,PHONE NUM, FindCol1, FindVal1, FindCol2, FindVal2,FindCol3,FindVal3
   ** <br> second param is the column name/num to select, the rest of the params go in pairs,
   ** first a findColumn, then a findValue. all the columns can be specified as either column
   ** name or column number.  All of the rows are searched until an exact match for all of the
   ** findcol/fundval parameters are matched.
   ** <br> the first param, rowindex, is a variable which is assigned the row index found.
   * @param                     fuzzy, is it a partial match on the column name or cell value
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   ** if matching cell values, use substrings)
   **/
  protected void commandClickFind (boolean fuzzy) throws SAFSException {
    if (params.size() < 4) {
      paramsFailedMsg(windowName, compName);
    } else {
      Integer rrow = new Integer(0);
      Integer ccol = new Integer(0);
      String column = null;
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      Iterator piter = params.iterator();
      // first param is the rowIndex variable
      String rowIndexVar = (String) piter.next();
      // initialize the variable to "", then later, if we find the cell, do the real value
      if (!setVariable(rowIndexVar, "")) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       " "+action+" setVariable failure, var: "+rowIndexVar+", val: ''",
                       FAILED_MESSAGE);
        return;
      }
      // second param will be the col number, or col name
      String next = (String) piter.next();
      if (!next.trim().equals("")) {
        ccol = convertNum(next);
        if (ccol == null) { //assume it is a column name
          column = next;
        }
      }
      int col = 0;
      if (column != null) {
        col = getColi(fuzzy, column, guiObj);
      } else {
        col = ccol.intValue();
      }
      Log.index("COL: "+col);
      Collection rowvals = new LinkedList();
      // additional param pairs will be like: {colname/num} / {cell value}
      while (piter.hasNext()) {
        //colname/num
        String colnamenum = (String) piter.next();
        //cell value
        if (piter.hasNext()) { // row
          String rowval = (String) piter.next();
          Collection rowvale = new LinkedList();
          rowvals.add(rowvale);
          rowvale.add(colnamenum);
          rowvale.add(rowval);
        }
      }
      int row = getRowi(fuzzy, rowvals, guiObj);
      if (!setVariable(rowIndexVar, (new Integer(row+1)).toString())) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       " "+action+" setVariable failure, var: "+rowIndexVar+", val:"+(row+1),
                       FAILED_MESSAGE);
        return;
      }
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowvals+", colname: "+column,
                     DEBUG_MESSAGE);

      actionClick(guiObj, row, col, rowvals.toString(), column);
    }
  }

  /** <br><em>Purpose:</em> commandSelectRowWithRowValues: process commands like: selectRowWithRowValues
   ** Example:custTableTable().setState(Action.select(),
   **                                   atRow("NAME", "Jill Sanford", 
   **                                         "CUSTID", "19", "STREET", 
   **                                         "80 West End"));
   ** <br> example step commands:
   ** <br> T,Frame,custTableTable,select,CUSTID,5,NAME,Susan Flontly,STREET,750 Central Expy
   ** <br> T,Frame,orderTableTable,select,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
   ** <br> all of the params are the name/value
   ** pairs for the row specification for the row to select
   **/
  protected void commandSelectRowWithRowValues () throws SAFSException {
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      String name1 = null;
      String val1 = null;
      String name2 = null;
      String val2 = null;
      String name3 = null;
      String val3 = null;
      try {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        Iterator piter = params.iterator();
        // get two at a time, the name/value row pairs
        if (piter.hasNext()) {
          name1 = (String) piter.next();
          if (piter.hasNext()) {
            val1 = (String) piter.next();
            if (piter.hasNext()) {
              name2 = (String) piter.next();
              if (piter.hasNext()) {
                val2 = (String) piter.next();
                if (piter.hasNext()) {
                  name3 = (String) piter.next();
                  if (piter.hasNext()) {
                    val3 = (String) piter.next();
                  }
                }
              }
            }
          }
        }
        Row row = null;
        if (name3 !=null && val3 != null) {
          row = Script.localAtRow(name1, val1, name2, val2, name3, val3);
        } else if (name2 !=null && val2 != null) {
          row = Script.localAtRow(name1, val1, name2, val2);
        } else if (name1 !=null && val1 != null) {
          row = Script.localAtRow(name1, val1);
        }
        if (row == null) {
          log.logMessage(testRecordData.getFac(),
              getClass().getName()+", "+action+": no row specified",
              FAILED_MESSAGE);
          return;
        }
        guiObj.setState(Action.select(), row);
        // set status to ok
        String altText = windowName+":"+compName+" "+action;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_3, altText,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                  getClass().getName()+": item not found: "+
                  ", name1: "+name1+ ", val1: "+val1+ ", name2: "+name2+
                  ", val2: "+val2+ ", name3: "+name3+ ", val3: "+val3+
                  "; msg: "+ex.getMessage(),
                  FAILED_MESSAGE);
      }
    }
  }

  /** <br><em>Purpose:</em> commandVerifyCellText: process commands like: verifyCellText
   ** and verifyCellTextContains
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JTable, VerifyCellText, FirstCellValue
   ** <br> Compares the value of cell 1,1 in JTable to bench text "FirstCellValue". 
   **      Default row and column indices substituted for missing parameters. 
   ** <br>
   ** <br> T, JavaWin, JTable, VerifyCellText, "FirstCellValue", 1,1 
   ** <br> Compares the value of cell 1,1 in JTable to bench text "FirstCellValue". 
   ** <br> 
   ** <br> T, JavaWin, JTable, VerifyCellText, "BenchValue", 4, "Field5"
   ** <br> Compares the value of cell 4, Field5 in JTable to bench text "BenchValue". 
   **     The column is determined by matching the text "Field5" to the field headers in the JTable.
   ** <br> 
   ** <br> T, JavaWin, JTable, VerifyCellTextContains, "ench", 4, "Field5"
   ** <br> Compares the value of cell 4, Field5 in JTable to bench substring "ench". 
   **     The column is determined by matching the text "Field5" to the field headers in the JTable.
   ** <p>
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   ** if matching cell values, use substrings)
   * @param                     fuzzy, boolean
   **/
  protected void commandVerifyCellText (boolean fuzzy) throws SAFSException {
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    String column = null;
    String rowval = null;
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator piter = params.iterator();
        String val =  (String) piter.next();
        log.logMessage(testRecordData.getFac(),"...val: "+val, DEBUG_MESSAGE);
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
        log.logMessage(testRecordData.getFac(),
            getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowval+", colname: "+column+", guiObj:"+guiObj,
            DEBUG_MESSAGE);

        actionVerify(guiObj, row, col, rowval, column, fuzzy, val);
    }
  }

  /** <br><em>Purpose:</em> commandVerifyCellTextFind: process commands like: verifyCellTextFind
   ** <p> example step commands:
   ** <br> T, JavaWin, JTable, VerifyCellText, "BenchValue", rowIndexVar, "Field5", FindCol1, FindVal1, FindCol2, FindVal2,FindCol3,FindVal3
   ** <br> Compares value of a found cell row, column Field5 in JTable to bench text "BenchValue". 
   ** <br> third param is the column name/num to select, the rest of the params go in pairs,
   ** first a findColumn, then a findValue. all the columns can be specified as either column
   ** name or column number.  All of the rows are searched until an exact match for all of the
   ** findcol/fundval parameters are matched.
   ** <br> the second param, rowindex, is a variable which is assigned the row index found.
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   ** if matching cell values, use substrings)
   * @param                     fuzzy, boolean
   **/
  protected void commandVerifyCellTextFind (boolean fuzzy) throws SAFSException {
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    String column = null;
    if (params.size() < 5) {
      paramsFailedMsg(windowName, compName);
    } else {
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      Iterator piter = params.iterator();
      String val =  (String) piter.next();
      // first param is the rowIndex variable
      String rowIndexVar = (String) piter.next();
      // initialize the variable to "", then later, if we find the cell, do the real value
      if (!setVariable(rowIndexVar, "")) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       " "+action+" setVariable failure, var: "+rowIndexVar+", val: ''",
                       FAILED_MESSAGE);
        return;
      }
      // third param will be the col number, or col name
      String next = (String) piter.next();
      if (!next.trim().equals("")) {
        ccol = convertNum(next);
        if (ccol == null) { //assume it is a column name
          column = next;
        }
      }
      int col = 0;
      if (column != null) {
        col = getColi(fuzzy, column, guiObj);
      } else {
        col = ccol.intValue();
      }
      Log.index("COL: "+col);
      Collection rowvals = new LinkedList();
      // additional param pairs will be like: {colname/num} / {cell value}
      while (piter.hasNext()) {
        //colname/num
        String colnamenum = (String) piter.next();
        //cell value
        if (piter.hasNext()) { // row
          String rowval = (String) piter.next();
          Collection rowvale = new LinkedList();
          rowvals.add(rowvale);
          rowvale.add(colnamenum);
          rowvale.add(rowval);
        }
      }
      int row = getRowi(fuzzy, rowvals, guiObj);
      if (!setVariable(rowIndexVar, (new Integer(row+1)).toString())) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       " "+action+" setVariable failure, var: "+rowIndexVar+", val:"+(row+1),
                       FAILED_MESSAGE);
        return;
      }
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowvals+", colname: "+column,
                     DEBUG_MESSAGE);

      actionVerify(guiObj, row, col, rowvals.toString(), column, fuzzy, val);
    }
  }

  /** <br><em>Purpose:</em> commandAssignCellTextRow: 
   * 	<br>Assign the "found row number" to a staf variable.
   * 	<br>Find the cell according to the "found row number" and "given column" and assing the cell value to the varibalb.value
   * <p> example step commands:
   * <br> T, Window, Table, AssignCellTextRow, "aVariable", "Date", FindCol1, FindVal1, FindCol2, FindVal2,FindCol3,FindVal3
   * <br>
   * <br> The first parameter, "aVariable", is a STAF variable which will contain the "found row number".  
   * <br> And "aVariable.value" will be a STAF variable which will contain the cell value.
   * <br>
   * <br> The second param, "Date", is a "column header name" or "column number".
   * <br>
   * <br> The rest of the params, "findcol/fundval ...", go in pairs, first a findColumn, then a findValue. 
   * <br>
   * <br> All the columns can be specified as either column name or column number. --> "given column"
   * <br> All of the rows are searched until an exact match for all of the findcol/fundval parameters are matched. --> "found row number"
   * <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or if matching cell values, use substrings)
   * 
   * @param                 fuzzy, boolean
   * @throws				SAFSException
   **/
  protected void commandAssignCellTextRow(boolean fuzzy) throws SAFSException{
  	String debugmsg = getClass().getName()+".commandAssignCellTextRow(): ";
  	
    if (params.size() < 4) {
        paramsFailedMsg(windowName, compName);
    } else {
        Integer rrow = new Integer(0);
        Integer ccol = new Integer(0);
        String column = null;
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        
        Iterator piter = params.iterator();
        //First param is the variable where the "found row number" will be stored
        String rowIndexVariable = (String) piter.next();
        String cellValueVariable = rowIndexVariable+".value";
        //Initialize the variable to "", then later, if we find the cell, do the real value
        if (!setVariable(rowIndexVariable, "")) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          String detail = " STAF: SetVariable failure, var: "+rowIndexVariable+", val: ''";
          Log.debug(debugmsg+detail);
          componentFailureMessage(detail);
          return;
        }
        Log.info(debugmsg+" rowIndexVariable name: "+rowIndexVariable+" , cellValueVariable name: "+cellValueVariable);
        
        //Second param will be the col number, or col name
        String next = (String) piter.next();
        int col = 0;
        if (!next.trim().equals("")) {
          ccol = convertNum(next);
          if (ccol == null) { //assume it is a column name
            column = next;
            col = getColi(fuzzy, column, guiObj);
          }else{
          	col = ccol.intValue();
          }
        }
        Log.info(debugmsg+" column header: "+column+" column index: "+col);

        //Additional param pairs will be like: {{colname/num},{cell value}} , {{colname/num},{cell value}} ...
        //According these parameters to search the row number
        Collection rowvals = new LinkedList();
        while (piter.hasNext()) {
          //colname/num
          String colnamenum = (String) piter.next();
          //cell value
          if (piter.hasNext()) { // row
            String rowval = (String) piter.next();
            Collection rowvale = new LinkedList();
            rowvals.add(rowvale);
            rowvale.add(colnamenum);
            rowvale.add(rowval);
          }
        }
        Log.info(debugmsg+" Search condition: {column/value}* : "+rowvals);
        int row = getRowi(fuzzy, rowvals, guiObj);
        Log.info(debugmsg+" Found row number: "+row);
        
        
        //Try to set the "found row number" to the variable
        if (!setVariable(rowIndexVariable, (new Integer(row+1)).toString())) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            String detail = " STAF: SetVariable failure, var: "+rowIndexVariable+", val:"+(row+1);
            Log.debug(debugmsg+detail);
            componentFailureMessage(detail);
            return;
        }
        
        //Try to get the cell value and set it to variable.value
        String cellValue = getCellText(guiObj,row,col);
        Log.debug(debugmsg+" Value of cell("+row+","+"): "+cellValue);
        if (!setVariable(cellValueVariable, cellValue)) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            String detail = " STAF: SetVariable failure, var: "+cellValueVariable+", val:"+cellValue;
            Log.debug(debugmsg+detail);
            componentFailureMessage(detail);
            return;
        }
        
        //Success, set the statusCode and log the success messag.
        testRecordData.setStatusCode(StatusCodes.OK);
        componentSuccessMessage(rowIndexVariable+"="+(row+1)+"; "+cellValueVariable+"="+cellValue);
    }  	
  }
  
  
  /** <br><em>Purpose:</em> commandAssignVariableCellText: process commands like: 
   ** assignvariablecelltext
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JTable, AssignVariableCellText, AVariableName
   ** <br> Assign the value of cell 1,1 in JTable to DDVariable ^AVariableName. 
   ** <br> 
   ** <br> T, JavaWin, JTable, AssignVariableCellText, "AVariableName", 2,3 
   ** <br> Assign the value of cell 2,3 in JTable to DDVariable ^AVariableName. 
   ** <br> 
   ** <br> T, JavaWin, JTable, AssignVariableCellText, "AVariableName", 4, "Field5"
   ** <br> Assign the value of cell 4, Field5 in JTable to DDVariable ^AVariableName. 
   ** <br> The column is determined by matching the text "Field5" to the field headers in the JTable. 
   ** <p>
   ** <br> NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   ** if matching cell values, use substrings)
   * @param                     fuzzy, boolean, if true then substrings or case insensitive match
   **/
  protected void commandAssignVariableCellText (boolean fuzzy) throws SAFSException {
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    String column = null;
    String rowval = null;
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator piter = params.iterator();
        String var =  (String) piter.next();
        log.logMessage(testRecordData.getFac(),"...variable: "+var, DEBUG_MESSAGE);
	  	if (!setVariable(var, "")) {
	  		this.issueErrorPerformingActionOnX(var, FAILStrings.convert( 
	  				FAILStrings.COULD_NOT_SET, "Could not set '"+ var +"' to '\"\"'.",
	  				var, "\"\""));
	  		return;
	  	}
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
        log.logMessage(testRecordData.getFac(),
            getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowval+", colname: "+column,
            DEBUG_MESSAGE);

        actionAssign(guiObj, row, col, rowval, column, var);
      }
    }
  }

  /** 
   * process commands like CaptureRangeToFile, CaptureFuzzyRangeToFile
   *
   * NOTE: if fuzzy is true then: (case is ignored; if matching columns, or
   * if matching cell values, use substrings)
   * 
   * @param fuzzy boolean, if true then match substrings and ignore case for any 
   * specified column names or row value identifiers.
   **/
  protected void commandCaptureRangeToFile (boolean fuzzy) throws SAFSException {
      int rowi = 0;
      int coli = 0;
      Integer rrow = new Integer(0);
      Integer ccol = new Integer(0);
      Integer rrows = null;
      Integer ccols = null;
      String column = null;
      String rowval = null;
      String fileEncoding = null;
    
      if (params.size() < 1) {
      	paramsFailedMsg(windowName, compName);
      	return;
      }
      try {
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());

        Iterator piter = params.iterator();
        String filename =  (String) piter.next();
        Log.info(action +".filename: "+filename);

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
              if (rrows.intValue() <= 0) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                    action +": rows cannot be less than 1: "+next,
                    FAILED_MESSAGE);
                return;
              }
            } catch (NumberFormatException ee) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                  action+": invalid rows number format: "+ee.getMessage(),
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
              
              if (ccols.intValue() <= 0) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                    action+": cols cannot be less than 1: "+next,
                    FAILED_MESSAGE);
                return;
              }
            } catch (NumberFormatException ee) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                  action +": invaldi cols number format: "+ee.getMessage(),
                  FAILED_MESSAGE);
              return;
            }
          }
        }
        //file encoding
        if(piter.hasNext()){
      	  fileEncoding = (String) piter.next();
          //If user put a blank string as encoding,
          //we should consider that user does NOT provide a encoding, reset encoding to null.
          fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
        }
        Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);
        //filter mode 
        //if(piter.hasNext()){ piter.next();}
        //filter options
        //if(piter.hasNext()){ piter.next();}
        
        actionCaptureRangeToFile(guiObj, rrow, rrows, rowval, ccol, ccols, column, filename, fuzzy, fileEncoding);
      } catch (SubitemNotFoundException ex) {
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  log.logMessage(testRecordData.getFac(),
			 action +": error capturing table contents: "+ ex.getMessage(),
			 FAILED_MESSAGE);
      }
      Log.info(getClass().getName()+"."+action+": Row: "+rrow+", Col: "+ccol+", rowval: "+rowval+", colname: "+column+", rows: "+rrows+", colss:"+ccols);
  }

  /**
   * Attempts to cast the generic Object to a GuiTestObject.  Also, for certain "custom" tables, 
   * like JCTables, we attempt to extract the embedded Java table from the custom wrapper.
   * 
   * @param table
   * @return GuiTestObject reference to the real Java table.
   * @throws IllegalArgumentException
   * @see #getJCTable(TestObject)
   */
  protected GuiTestObject getRealTable(Object table)throws IllegalArgumentException{
  	GuiTestObject guiObj;
  	TestObject jctable;
	IllegalArgumentException x2 = new IllegalArgumentException("Invalid Table object:");
  	try{
  	  	guiObj = (GuiTestObject) table;
	    return (GuiTestObject)getJCTable(guiObj);
  	}catch(Exception x){
  		x2.initCause(x);
  	}
  	Log.debug("Error interpretting provided Java Table",x2);
  	throw x2;
  }
  
  /**
   * Captures the object data into a List of rows.  Each row is a List of column values.  
   * The first row is a List of Column names, if any.  If no column names exists then this 
   * first row will be a List of empty strings.
   * 
   * @param table GuiTestObject to snapshot data from.
   * 
   * @return List of rows of column value Lists.  Null if an invalid table reference is 
   * provided or some other error occurs.
   * 
   * @throws SAFSException
   * @throws IllegalArgumentException if table is not an acceptable GuiTestObject.
   * @see CFComponent#captureObjectData(TestObject)
   * @see #formatObjectData(java.util.List)
   */
  protected List<List<String>> captureObjectData(TestObject table)throws IllegalArgumentException, SAFSException{

  	TestObject jctable = getRealTable(table); // Logs IllegalArgumentException, SAFSException
    int absrows = getNumRows(jctable);    // actual number of rows    (throws SAFSException)
    int abscols = getNumColumns(jctable); // actual number of columns (throws SAFSException)

    List<List<String>> rows = new ArrayList<List<String>>();
    List<String> header = new ArrayList<String>();
    String colname = null;
    for (int headi = 0;headi<abscols;headi++){
    	try{
    		// if no column header does null get added?
    		colname = getColumnName(jctable, headi);
    		if (colname == null) colname = "";
    		header.add(colname);
    	}
    	catch(Exception x){
    		header.add("");
    	}
    }
    // might be empty collection if no header exists
    rows.add(header);

    TestObject tblModel = (TestObject)jctable.invoke("getModel"); //throws ?NoSuchMethodException?
	
    String[] vals = null;
    List<String> cols = null;
    for (int rowi = 0; rowi < absrows; rowi++) {
        cols = new ArrayList<String>();
    	for(int coli = 0; coli < abscols; coli++) {
    		// vals[0] = cell value
    		// vals[1] = column name
    		vals = getCellText(jctable, tblModel, rowi, coli);
        	cols.add(vals[0]);	      
    	}
    	rows.add(cols);
    }
    return rows;
  }
  
  /**
   * Format the List data into a single String.  This String may include whatever formatting like 
   * Tabs, line separators, etc. are needed for the command being executed.  For example, the commands 
   * CaptureRangeToFile and CaptureObjectDataToFile have historically written different formatted 
   * versions of the same data to files.  This method may route to helper methods for formatting the 
   * data based on the action command currently being executed.
   * <p>
   * The List is expected to be a List of Lists as provided by our local captureObjectData.  
   * Each final item in the List will be output using its toString().trim() method.
   * <p>
   * It is expected the data is ready for writing to a file or other similar use without further 
   * modification.
   * 
   * @param list List returned from captureObjectData
   * @return String formatted for writing to file or screen.
   * @throws IllegalArgumentException if the List or the Object extracted from the List are null.
   * @see #captureObjectData(TestObject)
   * @see CFComponent#formatObjectData(java.util.List)
   */
  public String formatObjectData(java.util.List list)throws IllegalArgumentException{
  	try{
	  	StringBuffer buf = new StringBuffer();    	
	    java.util.List row;
	    for(int rowi=0;rowi<list.size();rowi++){
	    	row = (java.util.List) list.get(rowi);
	    	buf.append(System.getProperty("line.separator"));
	    	for(int coli = 0; coli < row.size(); coli++) {
	       		buf.append("\t"+ row.get(coli).toString().trim());	      
	    	}
	    	buf.append("\n");
	    }
	    return buf.toString();
  	}catch(NullPointerException np){
  		throw new IllegalArgumentException("Invalid ObjectData provided for formatting.");
  	}
  }
  
  /**
   * Captures the object data into a Tab delimited List of values.  Each row is a separate 
   * item in the List.  Some additional formatting of blank lines and Tab prefixes are added 
   * where appropriate to match outputs provided by other engines.
   * 
   * @param table GuiTestObject to snapshot data from.
   * 
   * @param rrow Integer default row# to begin capture. 
   *             0 = first row.  Null will force default to first row. 
   * 
   * @param rrows Integer of number of rows to capture. 
   *             Null means All.
   * 
   * @param rowval String value in first column identifying first row to capture.
   *             Null means start at first row.
   * 
   * @param ccol Integer default column# to begin capture. 
   *             0 = first column.  Null will force default to first column.
   * 
   * @param ccols Integer of number of columns to capture. 
   *             Null means All.
   * 
   * @param column String of column header text identifying column to start capture.
   *             Null means start at specified default first column.
   * 
   * @param fuzzy true if column and rowval text need NOT be an exact match.
   * 
   * @return java.util.List containing all captured rows.
   * @throws SAFSException
   * @throws IllegalArgumentException
   * 
   * @see #getColi(boolean, String, GuiTestObject)
   * @see #getRowi(boolean, Collection, GuiTestObject)
   */
  protected java.util.List getObjectData(GuiTestObject table, 
        Integer rrow, Integer rrows, String rowval, Integer ccol,
        Integer ccols, String column, boolean fuzzy)
		throws IllegalArgumentException, SAFSException{
  	String debugmsg = getClass().getName()+".getObjectData(): ";
  	TestObject jctable = getRealTable(table); //throws IllegalArgumentException
	        
    // make sure start defaults are not null
    if (rrow == null) rrow = new Integer(0);
    if (ccol == null) ccol = new Integer(0);
    
    int col = 0;
    if (column != null) {
        col = getColi(fuzzy, column, (GuiTestObject)jctable) ;
    } else {
        col = ccol.intValue();
    }
    Log.info(getClass().getName()+" deduced col: "+ col);
    
    int row = 0;
    if (rowval != null) {
        Collection rowvals = new LinkedList();
        Collection rowvale = new LinkedList();
        rowvals.add(rowvale);
        rowvale.add("1");
        rowvale.add(rowval);
        row = getRowi(fuzzy, rowvals, (GuiTestObject)jctable);
    } else {
        row = rrow.intValue();
    }
    Log.info(getClass().getName()+" deduced row: "+ row);
    
    int absrows = getNumRows(jctable); // get the actual rows
    int abscols = getNumColumns(jctable); // actual
    
    // absrows to contain last row# to capture
    if (rrows!=null && (row + rrows.intValue() < absrows)) {
        absrows = row + rrows.intValue();
    }
    Log.info(getClass().getName()+" deduced absrows: "+ absrows);
    
    // abscols to contain last column# to capture
    if (ccols!=null && (col + ccols.intValue() < abscols)) {
        abscols = col + ccols.intValue();
    }
    Log.info(getClass().getName()+" deduced abscols: "+ abscols);
    
    //=====================================
    // header is row 0. Data begins row 1
    //=====================================
    java.util.List rows;
    try{ rows = captureObjectData(jctable); }
    catch(Exception x){ //IllegalArgumentException, SAFSException
    	return null;
    }
    java.util.List list = new ArrayList();    

    //rows contains header info at row 0, so contains absrows+1 and row data starts at row 1
	for (int rowi = row+1; rowi < absrows+1; rowi++) {
		if(rowi>=rows.size()){
			Log.debug(debugmsg+" There is no more rows in this table. Table row is "+rows.size());
			break;
		}
    	java.util.List arow = (java.util.List) rows.get(rowi);
    	StringBuffer buf = new StringBuffer();
    	for(int coli = col; coli < abscols; coli++) {
    		if(coli>=arow.size()){
    			Log.debug(debugmsg+" There is no column "+coli+" in row "+rowi);
    			break;
    		}
        	buf.append(arow.get(coli));
        	if(coli < abscols-1) buf.append("\t");
    	}
        list.add(buf.toString());
    }
    return list;
  }
  
  /** <br><em>Purpose:</em> actually do the capture action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     rowval, String
   * @param                     column, String
   * @param                     var, String
   **/
  protected void actionCaptureRangeToFile (GuiTestObject guiObj, 
      Integer rrow, Integer rrows, String rowval, Integer ccol,
      Integer ccols, String column, String filename, boolean fuzzy, String encoding) throws SAFSException {

      java.util.List list = getObjectData(guiObj, rrow, rrows, rowval, ccol, ccols, column, fuzzy);

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
	      //If a file encoding is given or we need to keep the encoding consistent
	      if (encoding != null || keepEncodingConsistent) {
	    	  StringUtils.writeEncodingfile(filename, list,encoding);
	      } else {
	    	  // Keep compatible with old version
	    	  StringUtils.writeUTF8file(filename, list);
	      }
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
  }


  /** <br><em>Purpose:</em> get the column number based on fuzzy match
   * @param                     fuzzy, boolean
   * @param                     column, String
   * @param                     guiObj, GuiSubitemTestObject
   * @return                    Integer
   **/
  protected int getColi (boolean fuzzy, String column, GuiTestObject guiObj)
    throws SAFSException {
    Log.info(getClass().getName()+".getColi: column name: "+column);
    int columnCount = getNumColumns(guiObj);
    Log.info("...getNumColumns reports "+ columnCount +" columns...");
    Object[] colnam = null;
    try {
      colnam= (Object[])guiObj.getProperty("columnName");
      try{Log.info("...columnName property holds "+ colnam.length +" columns.");}
      catch(NullPointerException np){
    	  Log.info("!!! columnName property returns no Object[] !!!");
      }
      int coli=0;
      for(; coli<columnCount; coli++) {
        Log.info("Attempting match with column "+ coli +", value='"+ colnam[coli].toString() +"'");
        if (fuzzy) {
          if (colnam[coli].toString().toLowerCase().indexOf(column.toLowerCase())>=0) { //found it
            log.logMessage(testRecordData.getFac(),
                "Found col: "+colnam[coli],
                DEBUG_MESSAGE);
            return coli;
          }
        } else {
          if (colnam[coli].toString().equals(column)) { //found it
            log.logMessage(testRecordData.getFac(),
                "Found col: "+colnam[coli],
                DEBUG_MESSAGE);
            return coli;
          }
        }
      }
      try{// try Swing tableHeader value stored with some tables
    	  Log.info("No columnName match...Trying tableHeader...");
    	  TestObject header = (TestObject)guiObj.getProperty("tableHeader");
    	  Log.info("...Retrieving TableHeader.columnModel...");
    	  TestObject model = (TestObject)header.getProperty("columnModel");
    	  columnCount = ((Integer)model.getProperty("columnCount")).intValue();
    	  Log.info("...ColumnModel.columnCount="+ columnCount);
    	  TestObject col = null;
    	  String coltext = null;
    	  coli=0;
          for(; coli<columnCount; coli++) {
        	  col = (TestObject) getObjectProperty(model, "column("+coli+")");
        	  coltext = col.getProperty("headerValue").toString();
              if (fuzzy) {
                  if (coltext.toLowerCase().indexOf(column.toLowerCase())>=0) { //found it
                    log.logMessage(testRecordData.getFac(),
                        "Found col: "+coltext,
                        DEBUG_MESSAGE);
                    return coli;
                  }
                } else {
                  if (coltext.equals(column)) { //found it
                    log.logMessage(testRecordData.getFac(),
                        "Found col: "+coltext,
                        DEBUG_MESSAGE);
                    return coli;
                  }
              }
          }
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          throw new SAFSException(this, "getColi",
                                " failure, column not found in "+(fuzzy?"fuzzy ":"")+"match: "+column);
      }
      catch(Exception x){
    	  Log.debug("While processing tableHeader: " +x.getClass().getName()+"\n", x);
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          throw new SAFSException(this, "getColi",
                                " failure, column not found in "+(fuzzy?"fuzzy ":"")+"match: "+column);
      }
    } catch (PropertyNotFoundException pnf) {
      // this code is for JCTable, since it does not have a 'columnName' like JTable does
  	  Log.debug("'columnName' property not valid.  Trying JCTable.getDataSource...");
      try {
        TestObject myo = getJCTable(guiObj);
        TestObject dso = (TestObject)myo.invoke("getDataSource");
        Object[] coords = new Object[1];
        int coli=0;
        for(; coli<columnCount; coli++) {
          coords[0]=new Integer(coli);//index
          Object r = dso.invoke("getTableColumnLabel", "(I)Ljava/lang/Object;", coords);
          log.logMessage(testRecordData.getFac(),"r: "+r, DEBUG_MESSAGE);
          String ncol = (r== null ? "" : r.toString());
          if (fuzzy) {
            if (ncol.toLowerCase().indexOf(column.toLowerCase())>=0) { //found it
              log.logMessage(testRecordData.getFac(),
                  "Found col: "+ncol,
                  DEBUG_MESSAGE);
              return coli;
            }
          } else {
            if (ncol.equals(column)) { //found it
              log.logMessage(testRecordData.getFac(),
                  "Found col: "+ncol,
                  DEBUG_MESSAGE);
              return coli;
            }
          }
        }
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        throw new SAFSException(this, "getColi",
                                " failure, column not found in "+(fuzzy?"fuzzy ":"")+"match: "+column);
      } catch (PropertyNotFoundException p2) {
        throw new SAFSException(pnf.toString()+"; second prop not found: "+p2.toString());
      }
    }
  }

  /** <br><em>Purpose:</em> get the row number based on fuzzy match
   * @param                     fuzzy, boolean
   * @param                     rowvalsParam, Collection of collection, the inner collection is length 2
   * with the first element being the column number/name, and the second being the value;
   * if the element on the outer collection is not a collection, then we only look in the
   * first column for the 'value' of that element.
   * @param                     guiObj, GuiSubitemTestObject
   * @return                    Integer
   * @exception SAFSException, if no row found that matches exactly
   **/
  protected int getRowi (boolean fuzzy, Collection rowvalsParam, GuiTestObject guiObj)
    throws SAFSException {
    int rowCount = getNumRows(guiObj);
    int maxCol=0;
    int col=0;
    // first fix rowvals to get rid of all the blank columns
    for(Iterator rvi=rowvalsParam.iterator(); rvi.hasNext(); col++) {
      String colName = "";
      Object rve = rvi.next();
      if (rve instanceof Collection) {
        Iterator rvei = ((Collection)rve).iterator();
        colName = (String) rvei.next();
      }
      if (colName == null || colName.trim().equals("")) continue;
      maxCol = col;
    }
    col=0;
    Collection rowvals = new LinkedList();
    // second step in fix, add only up to 'maxCol'
    for(Iterator rvi=rowvalsParam.iterator(); rvi.hasNext(); col++) {
      Object rve = rvi.next();
      if (col<=maxCol) rowvals.add(rve);
    }
    // now do the real work.
    for(int rowi=0; rowi<rowCount; rowi++) {
      int fcount = 0;
      for(Iterator rvi=rowvals.iterator(); rvi.hasNext(); ) {
        String colName = "";
        String rowval = "";
        Object rve = rvi.next();
        if (rve instanceof Collection) {
          Iterator rvei = ((Collection)rve).iterator();
          colName = (String) rvei.next();
          Object n = rvei.next();
          rowval = (n==null ? "" : n.toString());
        } else {
          rowval = (rve==null ? "" : rve.toString());
        }
        Log.index("colName : "+colName);
        Log.index("rowval : "+rowval);
        Integer ccol = convertNum(colName);
        if (ccol == null) {
          ccol = new Integer(getColi(fuzzy, colName, guiObj));
        }
        Log.index("col: "+ccol);

        String ssub = getCellText(guiObj, rowi, ccol.intValue());
        if (fuzzy) {
          if (ssub.toLowerCase().indexOf(rowval.toLowerCase())>=0) { //found it
            Log.debug("Found row["+rowi+"]: "+ssub);
            fcount++;
          }
        } else {
          if (ssub.equals(rowval)) { //found it
            Log.debug("FOUND row["+rowi+"]: "+ssub);
            fcount++;
          }
        }
      }
      if (fcount > 0 && fcount == rowvals.size()) return rowi;
    }
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    throw new SAFSException(this, "getRowi",
                            " failure, cell not found in "+(fuzzy?"fuzzy ":"")+"match: "+rowvals);
  }
  /** <br><em>Purpose:</em> get number of rows in table
   * @param                     jctable, TestObject
   * @return                    int
   * @exception SAFSException based on caught Exception
   **/
  protected int getNumRows (TestObject jctable) throws SAFSException  {
    try {
      return ((Integer)jctable.getProperty("rowCount")).intValue();
    } catch (Exception e) {
      jctable = getJCTable(jctable);
      try {
        TestObject dso = (TestObject)jctable.invoke("getDataSource");
        Integer i = (Integer) dso.getProperty("numRows");
        return i.intValue();
      } catch (Exception e2) {
        throw new SAFSException(e.toString()+", and another: "+e2.toString());
      }
    }
  }
  /** <br><em>Purpose:</em> get number of columns in table
   * @param                     jctable, TestObject
   * @return                    int
   * @exception SAFSException based on caught Exception
   **/
  protected int getNumColumns (TestObject jctable) throws SAFSException  {
    try {
      return ((Integer)jctable.getProperty("columnCount")).intValue();
    } catch (Exception e) {
      //listAllProperties(jctable);
      Log.debug("Exception "+ e.getClass().getSimpleName()+" ignored. Checking for JCTable.getDataSource...");
      jctable = getJCTable(jctable);
      try {
        TestObject dso = (TestObject)jctable.invoke("getDataSource");
        Integer i = (Integer) dso.getProperty("numColumns");
        return i.intValue();
      } catch (Exception e2) {
        throw new SAFSException(e.toString()+", and another: "+e2.toString());
      }
    }
  }
  /** <br><em>Purpose:</em> get a Cell based on row and col
   * @param                     row, int
   * @param                     col, int
   * @return                    Cell, the cell
   * @exception SAFSException based on caught Exception
   **/
  protected Cell getCell (int row, int col) throws SAFSException  {
    try {
      Row nrow = Script.localAtRow(row);
      Column c = Script.localAtColumn(col);
      Cell ncell = Script.localAtCell(c, nrow);
      if (ncell == null) {
        throw new SAFSException(this, "getCell", "For row: "+row+", col: "+col+", cannot find cell");
      }
      return ncell;
    } catch (Exception e) {
      throw new SAFSException(e.toString());
    }
  }
  /** <br><em>Purpose:</em> get a cell based on row and col
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @return                    Object, contents of cell
   * @exception SAFSException based on caught Exception, like ClassCastException
   **/
  protected Object getCell (TestObject jctable, int row, int col) throws SAFSException  {
    try {
      Log.info("getCell; row: "+row+", col: "+col);
      Cell ncell = getCell(row, col);
      Object nsub = ((GuiSubitemTestObject)jctable).getSubitem(ncell);
      Log.info("getCell; row: "+row+", col: "+col+", nsub: "+nsub);
      return nsub;
    } catch (NullPointerException e) {
      //e.printStackTrace();
      log.logMessage(testRecordData.getFac(),"npe: .............", DEBUG_MESSAGE);
      listAllProperties(jctable);
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      throw new SAFSException(getClass().getName()+": item not found for: "+ 
                              "row: "+row+", col: "+col);
    } catch (WrappedException we) {
      //e.printStackTrace();
      log.logMessage(testRecordData.getFac(),"wrapped exception: .............",
          DEBUG_MESSAGE);
      listAllProperties(jctable);
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      throw new SAFSException(getClass().getName()+": item not found for: "+ 
                              "row: "+row+", col: "+col);
    } catch (Exception e) {
      e.printStackTrace();
      log.logMessage(testRecordData.getFac(),
          getClass().getName()+": item not found for: "+ 
          "row: "+row+", col: "+col,
          FAILED_MESSAGE);
      throw new SAFSException(e.toString());
    }
  }
  /** <br><em>Purpose:</em> actually do the assign action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     rowval, String
   * @param                     column, String
   * @param                     var, String
   **/
  protected void actionAssign (GuiTestObject guiObj, int row, int col,
                               String rowval, String column,
                               String var) throws SAFSException {
    try {
	  if (!setVariable(var, "")) {
		this.issueErrorPerformingActionOnX(var, FAILStrings.convert( 
				FAILStrings.COULD_NOT_SET, "Could not set '"+ var +"' to '\"\"'.",
				var, "\"\""));
		return;
	  }
      Object sub = getCell(guiObj, row, col);
      String rval = "";
      if (sub instanceof TestObject) {
        rval = (String) ((TestObject)sub).getProperty(getTextProperty());
      } else {
        rval = (String) (sub == null ? "" : sub.toString());
      }
      Log.info("..... rval: "+rval);
      if (!setVariable(var, rval)) {
  		this.issueErrorPerformingActionOnX(var, FAILStrings.convert( 
				FAILStrings.COULD_NOT_SET, "Could not set '"+ var +"' to '"+ rval +"'.",
				var, rval));
		return;
      }
      // set status to ok
      this.issuePassedSuccess(GENStrings.convert(
    		  GENStrings.VARASSIGNED2, "Value '"+ rval +"' was assigned to variable '"+ var +"'.",
    		  rval, var));
    } catch (SubitemNotFoundException ex) {
    	//can_not_get_text:Can not get component's text.
  		this.issueErrorPerformingActionOnX(compName, FAILStrings.text(FAILStrings.CAN_NOT_GET_TEXT));
    }
  }
  
  /** <br><em>Purpose:</em> actually do the verify action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     rowval, String
   * @param                     column, String
   * @param                     fuzzy, boolean
   * @param                     val, String
   * @author (CANAGL) 	Feb 12, 2006 	Put back old functionality for fallback.Some tables, like HTMLTables do not have models!
   * <br>	 (LeiWang) 	Apr 11, 2008	Move some codes to method getCellText(guiObj,row,col).
   **/
  protected void actionVerify (GuiTestObject guiObj, int row, int col,
                               String rowval, String column,
                               boolean fuzzy, String val) throws SAFSException {
  	try {
  	  if (column != null && column.length() != 0) {
  	 	col = getColi(fuzzy, column, guiObj);
  	  }
  	  String rval = getCellText(guiObj,row,col);
  		
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
  
    /**
     * Overrides the version in superclass ComponentFunctions to allow for 
     * alternative of "Row=1;Col=1" or "Col = 1;Row = 1".  Standard "Coords=" 
     * usage will still be handled by the superclass.
     * <p>
     * The calling routine will have to determine whether the returned Point is an
     * x,y coordinate or a row,col cell -- probably by checking 
     * for "Row" or "Col" text in the provided string just as we do internally.
     * 
     * @param   coords, String x;y or x,y or Coords=x;y  or Coords=x,y  or
     *                   Row=y,Col=x  or  Col=x;Row=y
     * @return  Point if successfull, null otherwise
     * @author CANAGL OCT 21, 2005 modified to work as required for 
     *                              keywords as documented.
     */
    public java.awt.Point convertCoords(String coords) {

   		// may throw NullPointerException
    	try{ if(coords.length()==0) return null; }
	   	catch(Exception x){ return null; }
    		
		String uccoords = coords.toUpperCase();
		
		// begin check for alternative "Row=n;Col=n" format    		
		int yindex = uccoords.indexOf("ROW");
		// "=" may not be immediately next, as in "Row = y"
		if (yindex >= 0) yindex = coords.indexOf("=", yindex);

		// revert to super method if not alternate format
		if (yindex < 0) {
			// should we check for and log if null returned?
			return super.convertCoords(coords);
		}		
		int xindex = uccoords.indexOf("COL");
		// "=" may not be immediately next, as in "Col = x"
		if (xindex >= 0) xindex = coords.indexOf("=", xindex);
		if ((xindex < 0)||(xindex == yindex)) {
			// log bad format?
			return null;
		}
		int sepindex = coords.indexOf(',');
		if (sepindex < 5) sepindex = coords.indexOf(';');
		if (sepindex < 5) {
			// log bad format?
			return null;
		}    		
		try{
			String sy = null; // y
			String sx = null; // x		
			// did Row = come before Col =
			if (yindex < xindex){
				sy = coords.substring(yindex+1, sepindex).trim();
				sx = coords.substring(xindex+1).trim();
			}
			// else col came first
			else{
				sx = coords.substring(xindex+1, sepindex).trim();
				sy = coords.substring(yindex+1).trim();
			}
			int x = (int) Float.parseFloat(sx);
			int y = (int) Float.parseFloat(sy);
			return new TableCell(x,y);
		}
		// IndexOutOfBounds?  NumberFormat?
		catch(Exception nf){
			// log bad format?
			return null;
		}
    }
    
	public class TableCell extends java.awt.Point {
		public TableCell (int x, int y){
			super(x,y);
		}
	}
	
	/**
	 * Attempt to perform a click-type action on a cell and issue a success message 
	 * if successful.
	 * The routines in here may throw Exceptions that should be caught 
	 * by the calling routine.  This routine is generally called internally by other
	 * functions like commandClick.
	 * 
	 * @throws SubitemNotFoundException
	 * @throws NullPointerException
	 * @throws WrappedException
	 * @throws SAFSException  (often passed through the caller)
	 */
	protected void doActionClick(GuiSubitemTestObject guiObj, 
	                            int row, int col,
	                            String rowval, String colval)
	                            throws SAFSException {

  		Cell cell = getCell(row, col);
	    if (action.equalsIgnoreCase(RIGHTCLICKCELL)) {
    		guiObj.click(Script.RIGHT, cell);
  		} else if (action.equalsIgnoreCase(DOUBLECLICKCELL) ||
            action.equalsIgnoreCase(ACTIVATECELL)) {
    		guiObj.doubleClick(cell);
  		} else{
    		guiObj.click(cell);
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
  		log.logMessage(testRecordData.getFac(),
                 passedText.convert(TXT_SUCCESS_3, altText, p),
                 PASSED_MESSAGE);
  		testRecordData.setStatusCode(StatusCodes.OK);
	}

	/**
	 * Issue generic click action failure status and log message.
	 * This is for item or subitem not found errors.
	 */	
	private void issueClickActionFailure( int row, int col, 
	                                       String rowval, String colval,
	                                       Exception exception){
	    String exmsg = null;
	    try{ exmsg = exception.getMessage(); }catch(Exception np){;}
	                                  	
      	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      	log.logMessage(testRecordData.getFac(),
            getClass().getName()+": item not found for num: "+ 
            "row: "+ row +", col: "+ col +", or rowval: "+ rowval +
            ", or column: "+ colval + "; msg: "+ exmsg, FAILED_MESSAGE);		
	}
	  
	/** 
	 * Perform a click-variant action on a cell in a table.
	 * 
	 * @param                     guiObj, GuiTestObject
	 * @param                     row, int
	 * @param                     col, int
	 **/
	protected void actionClick (GuiTestObject guiObj, int row, int col,
	                              String rowval, String column) throws SAFSException {
	    try { 
	    	
	    	doActionClick((GuiSubitemTestObject) guiObj, row, col, rowval, column); 
	    	    
	    }catch (SubitemNotFoundException ex) {
	    	issueClickActionFailure(row,col, rowval, column, ex);
	    
	    } catch (NullPointerException npe) {
	    	issueClickActionFailure(row,col, rowval, column, npe);
	    
	    } catch (WrappedException we) {
	          
	        Log.info("wrapped exception, attempting to clear cache and try again: "+we);
	      
	      	try{
	      		obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
	      		guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
		    	doActionClick( (GuiSubitemTestObject)guiObj, row, col, rowval, column); 

		    }catch (SubitemNotFoundException ex) {
		    	issueClickActionFailure(row,col, rowval, column, ex);
		    
		    } catch (NullPointerException npe) {
		    	issueClickActionFailure(row,col, rowval, column, npe);
		    
		    } catch (WrappedException we2) {
		    	issueClickActionFailure(row,col, rowval, column, we2);
	      	}
	    }
	}


    /** 
     * Perform a click action (this version takes 4 params),
     * not implemented for CFTable, just reverts to the other version...
     * @param                     guiObj, GuiTestObject
     * @param                     row, int
     * @param                     col, int
     **/
    protected void actionClick (GuiTestObject guiObj, int row, int col,
                              Integer rrow2, Integer ccol2,
                              String rowval, String column) throws SAFSException {
        actionClick(guiObj, row, col, rowval, column);
    }
  

  	/** 
  	 * drill down through wrapper objects to get the table
   	 * <br><em>Assumptions:</em>  to be used by sub-classes. This version just returns passed value
     * @param                     jctable, TestObject
     * @return                    TestObject
     **/
  	protected TestObject getJCTable (TestObject jctable) throws SAFSException {
    	return jctable;
  	}


  	protected void commandGetCellCoordinates () throws SAFSException {
    	Integer rrow = new Integer(0);
    	Integer ccol = new Integer(0);
    	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
    	Iterator piter = params.iterator();
    	String next = (String) piter.next(); // row
    	rrow = convertNum(next);
    	next = (String) piter.next();
    	ccol = convertNum(next);
    	String xCoordVariable = (String) piter.next();
    	String yCoordVariable = (String) piter.next();
		// do the action, for classes that 'extend' this class, be sure to
		// override this method.
    	actionGetCellCoordinates(guiObj, rrow.intValue(), ccol.intValue(),
                             	xCoordVariable, yCoordVariable);
  	}
  
  
  /** <br><em>Purpose:</em> GetCellCoordinates, *NOTE* currently not
   * implemented, but will be implemented by classes that extend this class
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     String, xCoordVariable
   * @param                     String, yCoordVariable
   * 
   **/
  protected void actionGetCellCoordinates (GuiTestObject guiObj, int row, int col,
                              String xCoordVariable, String yCoordVariable) throws SAFSException {
    java.awt.Point p = new java.awt.Point();
    try {
      Cell cell = getCell(row, col);
	  if (1==1) {// todo: not yet implemented for CFTable, figure out how to get the x/y coords
	    // look in the rational api help for the class 'Cell' to see
        //  if it will give back the x/y coords
        // p.x = cell.getXCoord();
        // p.y = cell.getYCoord();
        //listAllProperties(guiObj, "***** Get Cell Co-Ords **** ");
	    //p = guiObj.getScreenPoint();
	    //getScreenPoint() method is not vaild with the Cell object or class.
	    //Log.info(".................p: "+p);
	    throw new SAFSException("For CFTable, keyword: GetCellCoordinates not yet implemented");
	  }
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
      log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                     "row: "+row+", col: "+col+"; msg: "+ex.getMessage(), FAILED_MESSAGE);
    }
  }

  protected void commandVerifyRowLabel () throws SAFSException {
	Integer rrow = new Integer(0);
	String rowval = null;
	if (params.size() < 2) {
	  paramsFailedMsg(windowName, compName);   
	} else {
	  GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	  Iterator piter = params.iterator();
	  // first param will be the "bench text" or verify text for the label
	  String val =  (String) piter.next();
	  log.logMessage(testRecordData.getFac(),"...val: "+val, DEBUG_MESSAGE);
	  // second param will be the row number or index
	  if (piter.hasNext()) { // row
		String next = (String) piter.next();
		if (!next.trim().equals("")) {
		  rrow = convertNum(next);
		  // if (rrow == null) then, assume looking for first frozen label i.e. row = 0 rrow = 0 set in the first line.
		}
	  }
	  // do the action, for classes that 'extend' this class, be sure to
	  // override this method.
	  actionVerifyRowLabel(guiObj, val, rrow.intValue());
	}
  }

  /** <br><em>Purpose:</em> VerifyRowLabel, *NOTE* currently not
   * implemented, but will be implemented by classes that extend this class
   * @param                     guiObj, GuiTestObject
   * @param                     val, String
   * @param                     row, int
   **/
  protected void actionVerifyRowLabel (GuiTestObject guiObj, String val, int row) throws SAFSException {
    try {
	  if (1==1) {
	  	// todo: not yet implemented for CFTable
	    throw new SAFSException("For CFTable, keyword: VerifyRowLabel not yet implemented");
	  }
	  testRecordData.setStatusCode(StatusCodes.OK);
    } catch (Exception e) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		e.printStackTrace();
		log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item not found for: "+ 
					   "row: "+row, FAILED_MESSAGE);
		throw new SAFSException(e.toString());
    }

  }


  protected void commandVerifyColumnLabel () throws SAFSException {
	Integer ccol = new Integer(0);
	String colval = null;
	if (params.size() < 2) {
	  paramsFailedMsg(windowName, compName);   
	} else {
	  GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	  Iterator piter = params.iterator();
	  // first param will be the "bench text" or verify text for the label
	  String val =  (String) piter.next();
	  log.logMessage(testRecordData.getFac(),"...val: "+val, DEBUG_MESSAGE);
	  // second param will be the col number or index
	  if (piter.hasNext()) { // col
		String next = (String) piter.next();
		if (!next.trim().equals("")) {
		  ccol = convertNum(next);
		  // if (ccol == null) then, assume looking for first frozen label i.e. col = 0; ccol = 0 set in the first line.
		}
	  }
	  // do the action, for classes that 'extend' this class, be sure to
	  // override this method.
	  actionVerifyColumnLabel(guiObj, val, ccol.intValue());
	}
  }

  /** <br><em>Purpose:</em> VerifyColLabel, *NOTE* currently not
   * implemented, but will be implemented by classes that extend this class
   * @param                     guiObj, GuiTestObject
   * @param                     val, String
   * @param                     col, int
   **/
  protected void actionVerifyColumnLabel (GuiTestObject guiObj, String val, int col) throws SAFSException {
    try {
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

/** <br><em>Purpose:</em> commandClickRowLabel: process commands like: click, doubleclick, activate
   ** <br> T,windowName,TableName,clickRowLabel,2
   ** <br> T,windowName,TableName,doubleclickRowLabel,2
   ** <br> T,windowName,TableName,activateRowLabel,2 
   **/

  protected void commandClickRowLabel () throws SAFSException {
	Integer rrow = new Integer(0);
	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	Iterator piter = params.iterator();
	// first param will be the row number or index
	String next = (String) piter.next();
	if (!next.trim().equals("")) {
	  rrow = convertNum(next);		
	}
	
	int xoffset=0;
	if (piter.hasNext()) { // optional x offset i.e. for column replacement...
	  String nextOptional = (String) piter.next();
	  if (!nextOptional .trim().equals("")) {
		Integer xo = convertNum(nextOptional);
		xoffset = xo.intValue();
	  }

	}

	// do the action, for classes that 'extend' this class, be sure to
	// override this method.
	actionClickRowLabel(guiObj, rrow.intValue(), xoffset);
  }

  /** <br><em>Purpose:</em> ClickRowLabel, *NOTE* currently not
   * implemented, but will be implemented by classes that extend this class
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param    Optional         xoffset, int
   **/
  protected void actionClickRowLabel (GuiTestObject guiObj, int row, int xoffset) throws SAFSException {
    try {
	  if (1==1) {
	  	// todo: not yet implemented for CFTable
	    throw new SAFSException("For CFTable, keyword: ClickRowLabel, DoubleClickRowLabel, ActivateRowLabel not yet implemented");
	  }
	  testRecordData.setStatusCode(StatusCodes.OK);
    } catch (Exception e) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		e.printStackTrace();
		log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item not found for: "+ 
					   "row: "+row, FAILED_MESSAGE);
		throw new SAFSException(e.toString());
    }

  }



 /** <br><em>Purpose:</em> commandClickColumnLabel: process commands like: click, doubleclick, activate
   ** <br> T,windowName,TableName,clickColumnLabel,2
   ** <br> T,windowName,TableName,doubleclickColumnLabel,2
   ** <br> T,windowName,TableName,activateColumnLabel,2 
   **/

  protected void commandClickColumnLabel () throws SAFSException {
	Integer ccol = new Integer(0);
	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	Iterator piter = params.iterator();
	// first param will be the col number or index
	String next = (String) piter.next();
	if (!next.trim().equals("")) {
	  ccol = convertNum(next);		
	}

	int yoffset=0;
	if (piter.hasNext()) { // optional y offset i.e. for row replacement...
	  String nextOptional = (String) piter.next();
	  if (!nextOptional .trim().equals("")) {
		Integer yo = convertNum(nextOptional);
		yoffset = yo.intValue();
	  }

	}

	// do the action, for classes that 'extend' this class, be sure to
	// override this method.
	actionClickColumnLabel(guiObj, ccol.intValue(), yoffset);
  }

  /** <br><em>Purpose:</em> ClickColumnLabel,DoubleClickColumnLabel, ActivateColumnLabel
   * @param                     guiObj, GuiTestObject
   * @param                     col, int
   * @param    Optional         yoffset, int
   **/
  protected void actionClickColumnLabel (GuiTestObject guiObj, int col, int yoffset) throws SAFSException {
    try {
      Log.info("***** actionClickColumnLabel: "+col);
      guiObj = (GuiTestObject) getJCTable(guiObj);
      try{((GuiSubitemTestObject)guiObj).setState(Action.hScroll(0));}
      catch(Exception x){
      	  Log.warn("actionClickColumnLabel Ignoring Exception:\n"+x.toString());
      }
      int tot = 3; // some minimal pixel offset to the column
      java.awt.Rectangle tr = guiObj.getClippedScreenRectangle();
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
       
      //The following lines setup the appropriate point on screen where the
      //action will occur.  The coordinates are translated from guiObj coordinates
      //to screen coordinates.  The actual click is then done at the screen level,
      //not at the guiObj level.
      java.awt.Point p = new java.awt.Point(tot, 0);
      guiObj.click(p);
      java.awt.Point mousePt = null;
      try {
    	  mousePt = new java.awt.Point(RationalTestScript.getScreen().getMousePosition());
      } catch(NullPointerException npe) {
    	  //try to use awt to get mouse location directly
    	  Log.debug("NullPointerException thrown by RationalTestScript.getScreen().getMousePosition()");
          java.awt.PointerInfo mouseinfo = MouseInfo.getPointerInfo();
          mousePt = new java.awt.Point(mouseinfo.getLocation());
          Log.debug("using java.awt.MouseInfo to get mouse location: " + mousePt);
      }

      java.awt.Point p2 = new java.awt.Point(mousePt.x,(mousePt.y - 8));
      if (action.equalsIgnoreCase(CLICKCOLUMNLABEL)) {
	      RationalTestScript.getScreen().click(p2);
	      // set status to ok
	      testRecordData.setStatusCode(StatusCodes.OK);
  		  String altText = windowName+":"+compName+" "+action;
		  altText += " successful using '"+ col +"'.";
		  log.logMessage(testRecordData.getFac(), passedText.convert("success3a", altText, windowName, compName, action, String.valueOf(col)));

      }else if (action.equalsIgnoreCase(RIGHTCLICKCOLUMNLABEL)) {
	      RationalTestScript.getScreen().click(Script.RIGHT,p2);
	      // set status to ok
	      testRecordData.setStatusCode(StatusCodes.OK);
  		  String altText = windowName+":"+compName+" "+action;
		  altText += " successful using '"+ col +"'.";
		  log.logMessage(testRecordData.getFac(), passedText.convert("success3a", altText, windowName, compName, action, String.valueOf(col)));
  
      } else if (action.equalsIgnoreCase(DOUBLECLICKCOLUMNLABEL) ||
                 action.equalsIgnoreCase(ACTIVATECOLUMNLABEL)) {
	      RationalTestScript.getScreen().doubleClick(p2);	  
     	  // set status to ok
	      testRecordData.setStatusCode(StatusCodes.OK);
  		  String altText = windowName+":"+compName+" "+action;
		  altText += " successful using '"+ col +"'.";
		  log.logMessage(testRecordData.getFac(), passedText.convert("success3a", altText, windowName, compName, action, String.valueOf(col)));
      }

    } catch (Exception e) {
	    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    e.printStackTrace();
	    log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item not found for: "+ 
		       "col: "+ col, FAILED_MESSAGE);
	    throw new SAFSException(e.toString());
    }

  }
  /** <br><em>Purpose:</em> getLocalRect get a rectangle based on row and col indicating the offset
   * @param                     jctable GuiTestObject
   * @param                     row int
   * @param                     col int
   * @return                    java.awt.Rectangle
   **/
  protected java.awt.Rectangle getLocalRect(GuiTestObject jctable, int row, int col) {
	Log.debug("**** getLocalRect: Given Row: " + row + "  Col: " + col + " ****");
	Object[] coords = new Object[3];
	coords[0]=new Integer(row);//row1
    coords[1]=new Integer(col);//col1
    coords[2]=new Boolean(true);//boolean
    java.awt.Rectangle  r = (java.awt.Rectangle)jctable.invoke("getCellRect", "(IIZ)Ljava/awt/Rectangle;", coords);
	Log.info("**** Rectangle : " + r);
	return r;
  }



 /** <br><em>Purpose:</em> commandVerifyCellEditable: 
   ** <br> T,windowName,TableName,VerifyCellEditable,2,4,true or false
   ** <br> T,windowName,TableName,VerifyCellEditable,2,Field4, true or false
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
  protected void commandVerifyCellEditable () throws SAFSException {
	String next;
	Integer rrow = new Integer(0);
	Integer ccol = new Integer(0);
  	boolean verifyFlag = true;

	if (params.size() < 3) {
	  paramsFailedMsg(windowName, compName);
	} else {
	  GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	  Iterator piter = params.iterator();

	  next = (String) piter.next();
	  if (!next.trim().equals("")) {
		rrow = convertNum(next);		
	  }

	  next = (String) piter.next();
	  if (!next.trim().equals("")) {
		ccol = convertNum(next);		
	  }

	  // 3 rd param will be the "true/false text"
	  String val =  (String) piter.next();
	  log.logMessage(testRecordData.getFac(),"...val: "+val, DEBUG_MESSAGE);
	  if (!val.trim().equals("")) {
	  	if (val.equalsIgnoreCase("true")){
	  		verifyFlag = true;
	  	} else if (val.equalsIgnoreCase("false")) {
	  		verifyFlag = false;
	  	}
	  }
	  
	  // do the action, for classes that 'extend' this class, be sure to
	  // override this method.
	  actionVerifyCellEditable(guiObj, rrow.intValue(), ccol.intValue(), verifyFlag);
	}


  }
  


  /** <br><em>Purpose:</em> VerifyCellEditable
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JCTable, VerifyCellEditable, 2,5,true
   ** <br> Verifies the cell in the Table, at row = 2, col = 5 is ediable. Validates the cell property
   ** <br> to a known value.
   ** <p> 
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param						verifyFlag, boolean
   **/
  protected void actionVerifyCellEditable (GuiTestObject guiObj, int row, int col, boolean verifyFlag) throws SAFSException {
  	Log.debug("***  [Inside] actionVerifyCellEditable on CFTable Class ***]");
    try {
      GuiTestObject jctable = (GuiTestObject) getJCTable(guiObj);
	  //listAllProperties(jctable, "* * * * jctable   * * *");
      //listMethods(jctable);

      Boolean isEditable = new Boolean(false);
      Object[] coords = new Object[2];
      coords[0]=new Integer(row);
      coords[1]=new Integer(col);
      try{
          isEditable = (Boolean)jctable.invoke("editCellAt","(II)Z", coords);
      } catch (MethodNotFoundException mnfe) {
      	Log.info("* * * Caught Method Not Found Exception (mnfe) * * *");
      	isEditable = new Boolean (false);
      } catch (WrappedException we) {		//This catch is manily to cover the grids which setTableDataItem is valid...
      	Log.info("* * * Caught WrappedException (we) [2 nd catch] * * *");
      } catch (com.rational.test.ft.IllegalAccessException iae) {		// All the grids in the InvoiceAdjSumWinJ...
      	Log.info("* * * Caught IllegalAccessException (iae) [3 rd catch] * * *");
      	isEditable = new Boolean (false);
      }

	  if (isEditable.booleanValue() == verifyFlag) {
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
		log.logMessage(testRecordData.getFac(), getClass().getName()+": cell not found for row: "+ 
                row + ", col: "+ col + "; msg: "+ ex.getMessage(), FAILED_MESSAGE);
    } catch (Exception e) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		e.printStackTrace();
		log.logMessage(testRecordData.getFac(), getClass().getName()+ ": item not found for: "+ 
					   "row:" + row +", col: "+ col, FAILED_MESSAGE);
		throw new SAFSException(e.toString());
    }

  }



  /** <br><em>Purpose:</em> commandSelectCellTextSpecial: process command click, with Contorl or shirt key
   **                       and left or right mouse button.
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
  protected void commandSelectCellTextSpecial (boolean fuzzy) throws SAFSException {
    Integer rrow = new Integer(0);
    Integer ccol = new Integer(0);
    Integer rrow2 = null;
    Integer ccol2 = null;
    String column = null;
    String rowval = null;
    boolean control = false;
    boolean shift = false;
    boolean leftRight = false;


    if (params.size() < 4) {
	  paramsFailedMsg(windowName, compName);
	} else {
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      // first param will be the row or appmap name of row
      Iterator piter = params.iterator();

	  if (piter.hasNext()) { // Keyboard key Control or Shift...
		String val = (String) piter.next();
		if (val.equalsIgnoreCase("CONTROL")) {
			control = true;
			shift = false;
		} else if (val.equalsIgnoreCase("SHIFT")) {
			control = false;
			shift = true;
		} else {
			control = false;
			shift = false;
		}
	  }

	  if (piter.hasNext()) { // Mouse Button left or right...
		String val = (String) piter.next();
		if (val.equalsIgnoreCase("RIGHT")) {
			leftRight = false;
		} else if (val.equalsIgnoreCase("LEFT")) {
			leftRight = true;
		}
	  }

      if (piter.hasNext()) { // row
		String next = (String) piter.next();
		if (!next.trim().equals("")) {
		  rrow = convertNum(next);
		  if (rrow == null) { // assume looking for first cell with this content
			rowval = next;
		  }
	    }
	  }

	  // second param will be the col number, or col name
	  if (piter.hasNext()) { // col
	    String next = (String) piter.next();
	    if (!next.trim().equals("")) {
		  ccol = convertNum(next);
		  if (ccol == null) { //assume it is a column name
		    column = next;
		  }
	    }
	  }

	
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

	  log.logMessage(testRecordData.getFac(),
          getClass().getName()+"."+action+": Row: "+row+", Col: "+col+", rowval: "+rowval+", colname: "+column
          + ", control: " + control + ", shift: " + shift + ", leftRight: " + leftRight, DEBUG_MESSAGE);

	
	  actionSelectCellTextSpecial(guiObj, row, col, rowval, column, control, shift, leftRight);
	}


  }
  
  
  protected void actionLocalSelectCellTextSpecial (GuiTestObject guiObj, int row, int col,
                              String rowval, String column, boolean control, 
                              boolean shift, boolean leftRight) throws SAFSException, 
                              SubitemNotFoundException, NullPointerException, WrappedException {

	Cell cell = getCell(row, col);

	if (leftRight) {	//left is true
	  if (control) {
		((GuiSubitemTestObject)guiObj).click(Script.CTRL_LEFT,cell);
	  } else if (shift) {
		((GuiSubitemTestObject)guiObj).click(Script.SHIFT_LEFT,cell);
	  } else{
		((GuiSubitemTestObject)guiObj).click(cell);
	  }
	} else {	//Right is true
	  if (control) {
		((GuiSubitemTestObject)guiObj).click(Script.CTRL_RIGHT,cell);
	  } else if (shift) {
		((GuiSubitemTestObject)guiObj).click(Script.SHIFT_RIGHT,cell);
	  } else{
		((GuiSubitemTestObject)guiObj).click(cell);
	  }
	}

	String altText = windowName +":"+compName+" "+action+ ", row: "+row+", col: "+ col + ", control: "+ control
					 + ", shift: "+ shift + ", Right: "+ leftRight;

	Log.info("***  altText: " + altText);

	log.logMessage(testRecordData.getFac()," "+ action +" ok [" + row + "," + col + "]", PASSED_MESSAGE);

	testRecordData.setStatusCode(StatusCodes.OK);

  }


 /** <br><em>Purpose:</em> actually do the right or left click action with Contorl or Shift key pressed.
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     control, boolean
   * @param                     shift, boolean
   * @param                     leftRight, boolean
   **/
  protected void actionSelectCellTextSpecial (GuiTestObject guiObj, int row, int col,
                              String rowval, String column, boolean control, 
                              boolean shift, boolean leftRight) throws SAFSException {
    try {

	  actionLocalSelectCellTextSpecial (guiObj, row, col, rowval, column, control, shift, leftRight);
     } catch (SubitemNotFoundException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column +
                 ", control: "+ control + ", shift: "+ shift + ", leftRight: "+ leftRight +
                "; msg: "+ex.getMessage(), FAILED_MESSAGE);
    } catch (NullPointerException npe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+ column +
				", control: "+ control + ", shift: "+ shift + ", leftRight: "+ leftRight +
                "; msg: "+npe.getMessage(), FAILED_MESSAGE);
    } catch (WrappedException we) {
    	try{
      	  Log.info("wrapped exception, attempting to clear cache and try again: "+we);
      	  obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
      	  guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      
      	  actionLocalSelectCellTextSpecial (guiObj, row, col, rowval, column, control, shift, leftRight);
    	} catch (Exception ex) {
      		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      		log.logMessage(testRecordData.getFac(), getClass().getName()+": item not found for num: "+ 
                "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+ column +
				", control: "+ control + ", shift: "+ shift + ", leftRight: "+ leftRight +
                "; msg: "+ ex.getMessage(), FAILED_MESSAGE);
    	}
    	
 	
    }
  }
  


 /** <br><em>Purpose:</em> commandGetTableRowColumnCount
   ** <br> T,windowName,TableName,GetTableRowColumnCount,retRowCountVar, retColCountVar
   ** <br>
   ** <p> 
   ** <br> 
   ** <br>
   **/
  protected void commandGetTableRowColumnCount () throws SAFSException {
	String next, rowCountVariable = "", colCountVariable = "";

	if (params.size() < 2) {
	  paramsFailedMsg(windowName, compName);
	} else {
	  GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
	  Iterator piter = params.iterator();

	  // first parameter is rowCount var...
	  next = (String) piter.next();
	  if (!next.trim().equals("")) {	//rowCount
		rowCountVariable = next;
	  }

	  // second parameter is colCount var...
	  next = (String) piter.next();
	  if (!next.trim().equals("")) {
		colCountVariable = next;		
	  }

	  // do the action, for classes that 'extend' this class, be sure to
	  // override this method.
	  actionGetTableRowColumnCount(guiObj, rowCountVariable, colCountVariable);
	}


  }
  


  /** <br><em>Purpose:</em> GetTableRowColumnCount *NOTE* currently not
   * implemented, but will be implemented by classes that extend this class
   * @param                     guiObj, GuiTestObject
   * @param                     String, rowCountVariable
   * @param                     String, colCountVariable
   **/
  protected void actionGetTableRowColumnCount (GuiTestObject guiObj, String rowCountVariable, String colCountVariable) throws SAFSException {
  	Log.debug("***  Inside CFTable.java ***");
    try {
      Integer numColumns = (Integer) guiObj.getProperty("columnCount");
      Log.debug("***  numColumns: " + numColumns);
	  Integer numRows = (Integer) guiObj.getProperty("rowCount");
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
		pnfe.printStackTrace();
		log.logMessage(testRecordData.getFac(), getClass().getName()+ ": property not found: "+ 
					   "row count:" + rowCountVariable +", col count: "+ colCountVariable, FAILED_MESSAGE);
		throw new SAFSException(pnfe.toString());
    }

  }

  /**
   * Retrieve the column name for the provided zero-based column number.
   * @param guiObj Expected to be a Table with a getColumnName(i) method that returns a String.
   * @param col the column to get the name of.
   * @return String column name or null if not available or an error occurs.
   */
  protected String getColumnName(TestObject guiObj, int col){
  	try {
  		Object[] getColName = new Object[1];
  		getColName[0] = new Integer(col);
        return (String) guiObj.invoke("getColumnName", "(I)", getColName);
  	}
  	catch(Exception x){
  		return null;
  	}
  }
  
  /**
   * @param			guiObj			The TestObject representing the Table
   * @param			tblModel		The TestObject representing the Table Model
   * @param			row				The row number in the table view NOT in Model
   * @param			col				The column number in the table view NOT in Model
   * Returns String[]
   * String[0] = cell text value;
   * String[1] = cell column name
   */
  protected String[] getCellText(TestObject guiObj, TestObject tblModel, int row, int col) {
	String debugmsg = getClass().getName()+".getCellText(): ";
  	String[] rval = new String[]{"",""};
  	try {
  		//The parameter row and col describe the position of the value in the table view NOT model!!!!
  		Object[] getColName = new Object[1];
		getColName[0] = new Integer(col);
		Log.info(debugmsg + " processing on table, which type is " + guiObj.getObjectClassName());
		String columnName = (String) guiObj.invoke("getColumnName", "(I)", getColName);
		rval[1] = columnName;
		
//        Object[] getColIndex = new Object[1];            
//        getColIndex[0] = columnName;
//        int colModelIndex =
//        	((Integer)((TestObject)(guiObj.invoke("getColumn",
//        			"(Ljava.lang.Object;)",getColIndex))).invoke("getModelIndex")).intValue();
//        
//        Object[] rowColCoords = new Object[2];
//        rowColCoords[0] = new Integer(row);
//        rowColCoords[1] = new Integer(colModelIndex);
//        
//        Object tblModelValue = (Object)((TestObject)tblModel)
//            .invoke("getValueAt","(II)",rowColCoords);
  		
		//As the row and col are the position in the table view, we can just call
		//"getValueAt" of JTable, it will return the value locating at the (row,col) of table view (NOT model)
  		Object[] rowColCoords = new Object[2];
	    rowColCoords[0] = new Integer(row);
	    rowColCoords[1] = new Integer(col);
  		Object tblModelValue = (Object) (guiObj.invoke("getValueAt","(II)",rowColCoords));
        
        if ( !(tblModelValue instanceof TestObject) &&
        		!(tblModelValue.getClass().getName().startsWith(RFTGENERALVALUEPACKAGENAME)))
        {
        	rval[0] = tblModelValue.toString();
        }
        else {
        	//Try to use java swing API to get the cell text
        	try{
            	Object[] cellRendCompParams = new Object[6];
            	cellRendCompParams[0] = guiObj;
            	cellRendCompParams[1] = tblModelValue;
            	cellRendCompParams[2] = new Boolean(false);
            	cellRendCompParams[3] = new Boolean(false);
            	cellRendCompParams[4] = rowColCoords[0];
            	//the column of parameter in getCellRenderer() is the model column number
            	//we need to convert the view column number to model column number
          		Object[] viewColumn = new Object[1];
          		viewColumn[0] = col;
          		Integer modelColumn = (Integer) (guiObj.invoke("convertColumnIndexToModel", "(I)", viewColumn));
            	cellRendCompParams[5] = modelColumn;

            	TestObject tblCellRend = (TestObject)guiObj
				    .invoke("getCellRenderer","(II)",rowColCoords);
                
            	Object tblCellRendComp = tblCellRend
                    .invoke("getTableCellRendererComponent",
                            "(Ljavax.swing.JTable;Ljava.lang.Object;ZZII)",
                            cellRendCompParams);
            
            	if (tblCellRendComp instanceof TestObject) {

                    rval[0] = (String)((TestObject)tblCellRendComp)
					    .getProperty("text");
                    
                    if (rval[0].equals("")) {
                        rval[0]=getTableCellSubItemText(guiObj, row, col);
                    }
                }
            	else {
            		rval[0] = tblCellRendComp.toString();
            	}
        	}catch(MethodNotFoundException e){
        		Log.debug(debugmsg+" Method getCellRenderer or getTableCellRendererComponent not found.");
        	}catch(Exception oe){
        		Log.debug(debugmsg+" Exception occured: "+oe.getMessage());
        	}
        	
        	Log.info(debugmsg+" cell text is "+rval[0]);
        	//If the cell text can not be got by java swing API, try the custom method
        	if(rval[0].equals("")){
        		if (tblModelValue.getClass().getName().startsWith(RFTGENERALVALUEPACKAGENAME)){
        			if (isCustomCellObject(tblModelValue.getClass().getName())){
        				rval[0] = processCustomCellObject(tblModelValue);
        			}
        		}else if (isCustomCellObject((String)((TestObject)tblModelValue).getProperty("class"))){
        			rval[0] = processCustomCellObject((TestObject)tblModelValue);
        		}
            }
        }
  	} catch (Exception pnfe) {
  		log.logMessage(testRecordData.getFac(),"pnfe: "+pnfe, DEBUG_MESSAGE);
  		try{ 
  			rval[0] = getTableCellSubItemText(guiObj, row, col);
  		}
  		catch(Exception anyall){
  		    log.logMessage(testRecordData.getFac(),"anyall: "+anyall, DEBUG_MESSAGE);
  		}
  	}
    return rval;
  }
  
  // the old way that actually worked for most Swing tables
  protected String getTableCellSubItemText(TestObject guiObj, int row, int col){
  	  String rval;
      Row nrow = Script.localAtRow(row);
      Column ncol = Script.localAtColumn(col);
      Cell ncell = Script.localAtCell(ncol, nrow);
      Object nsub = ((GuiSubitemTestObject)guiObj).getSubitem(ncell);
    
      if (nsub == null) {
          rval = "";
      }
      else {
     	  try{
     	  	  rval = (String)((TestObject)nsub).invoke("toString");
     	  }
     	  catch(Exception anyall){
     	  	  rval = (String) nsub.toString();
     	  }
      }
      return rval;
  }
  
  
  protected boolean isCustomCellObject(String testObjectClass) {
    boolean retValue = false;
    for (int i = 0; i < customCellObjects.length; i++) {    
        if (customCellObjects[i].equals(testObjectClass)) {
            return true;
        }
    }
    return retValue;
  }
  
  protected String processCustomCellObject(TestObject tblModelValue) {
  	return tblModelValue.toString();
  }
  
  protected String processCustomCellObject(Object tblModelValue) {
    return tblModelValue.toString();
  }
  
  /**
   * <b>Note:</b><br> Get the text value of the cell at (row,col) in Java table
   * 			 <br> Its subclass may override this method.
   * @param guiObj
   * @param row
   * @param col
   * @return String, the text of the cell at row,col
   */
  protected String getCellText(TestObject guiObj, int row, int col) throws SAFSException{
  	String debugMsg = getClass().getName()+".getCellText(): ";
	  String rval = "";
	  
  	  try{ 
  		  TestObject tblModel = (TestObject)guiObj.invoke("getModel");        
          rval = getCellText(guiObj, tblModel, row, col)[0];
  	  }
  	  // back to way that worked before
  	  catch(Exception nomodel){
  	  	//Now the subclass CFHtmlTable override this method and do this section.
  	  	//so when deal with "java table", this exception should not happen. But I keep this section.
	      Object sub = getCell(guiObj, row, col);
	      if (sub == null) {
	      	String detail = " Row: "+row+", Col: "+col+ ", subitem not found for that cell.";
	      	Log.debug(debugMsg+detail);
	      	throw new SAFSException(detail);
	      }
	      rval = sub.toString();
	      if (sub instanceof TestObject) {
	        Log.info("sub is actually of type TestObject, trying getPropety(\""+getTextProperty()+
	                 "\"), if that isn't sufficient then further investigation of this is required!");
	        try {
	          String prop = getTextProperty();
	          rval = (String) ((TestObject)sub).getProperty(prop);
	        } catch (Exception pnfe) {
	          Log.debug(debugMsg+pnfe);
	        }
	      }
  	  }
  	  return rval;
  } 
  
}	//End of the class


