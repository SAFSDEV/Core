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
package org.safs.rational.custom;

import java.util.*;

import org.safs.*;
import org.safs.rational.*;
import org.safs.rational.CFTable;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

/**
 * <br><em>Purpose:</em> CFJCTreeTable, process a JCTable component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   AUG 01, 2003
 *
 *   <br>   AUG 01, 2003    (DBauman) Original Release
 **/
public class CFJCTreeTable extends CFTable {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFJCTreeTable () {
    super();
  }

  /** <br><em>Purpose:</em> actually do the verify action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   * @param                     rowval, String
   * @param                     column, String
   * @param                     fuzzy, boolean
   * @param                     val, String
   **/
  //protected void actionVerify (GuiTestObject guiObj, int row, int col,
  //                             String rowval, String column,
  //                             boolean fuzzy, String val) throws SAFSException {
  //  try {
  //    Object sub = getCell(guiObj, row, col);
  //    if (sub == null) {
  //      log.logMessage(testRecordData.getFac(),
  //                     getClass().getName()+"."+action+": Row: "+row+", Col: "+col+
  //                     ", subitem not found for that cell.", WARNING_MESSAGE);
  //      return;
  //    }
  //    String rval = sub.toString();
  //    if (sub instanceof TestObject) {
  //      // we should get stuff like this:
  //      //[DEBUG: ==========]
  //      //[DEBUG: key: parentPath: javax.swing.tree.TreePath]
  //      //[DEBUG: key: lastPathComponent: java.lang.Object]
  //      //[DEBUG: ==========]
  //      //[DEBUG: key: pathCount: 2]
  //      //[DEBUG: key: class: javax.swing.tree.TreePath]
  //      Log.info("sub is actually of type TestObject, further investigation of this is required!");
  //      Log.debug("==========sub non value properties:");
  //      listNonValueProperties((TestObject)sub);
  //      Log.debug("==========sub properties:");
  //      listProperties((TestObject)sub);
  //
  //      Tree atree = extractTreeItemsSub(sub);
  //      Log.info("atree: "+atree);
  //      // do the work of matching...
  //      String match = atree.findMatchPath(val);
  //      if (match != null) {
  //        Log.info("match: "+match);
  //      }
  //
  //    }
  //    if (action.equalsIgnoreCase(VERIFYCELLTEXT)) {
  //      if (fuzzy) {
  //        if (!val.equalsIgnoreCase(rval.toString())) {
  //          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //          log.logMessage(testRecordData.getFac(), "VP failure, read property is: "+rval+", compare value is: "+val, WARNING_MESSAGE);
  //          return;
  //        } else {
  //          Log.pass("VP ok, read property is: "+rval+", compare value is: "+val);
  //        }
  //      } else {
  //        if (!val.equals(rval.toString())) {
  //          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //          log.logMessage(testRecordData.getFac(), "VP failure, read property is: "+rval+", compare value is: "+val, WARNING_MESSAGE);
  //          return;
  //        } else {
  //          Log.pass("VP ok, read property is: "+rval+", compare value is: "+val);
  //        }
  //      }
  //    } else if (action.equalsIgnoreCase(VERIFYCELLTEXTCONTAINS)) {
  //      if (fuzzy) {
  //        if (rval.toString().toLowerCase().indexOf(val.toLowerCase())<0) {
  //          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //          log.logMessage(testRecordData.getFac(), "VPContains failure, read property is: "+rval+", compare value is: "+val, WARNING_MESSAGE);
  //          return;
  //        } else {
  //          Log.pass("VPContains ok, read property is: "+rval+", compare value is: "+val);
  //        }
  //      } else {
  //        if (rval.toString().indexOf(val)<0) {
  //          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //          log.logMessage(testRecordData.getFac(), "VPContains failure, read property is: "+rval+", compare value is: "+val, WARNING_MESSAGE);
  //          return;
  //        } else {
  //          Log.pass("VPContains ok, read property is: "+rval+", compare value is: "+val);
  //        }
  //      }
  //    }
  //    // set status to ok
  //    testRecordData.setStatusCode(StatusCodes.OK);
  //  } catch (SubitemNotFoundException ex) {
  //    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //    log.logMessage(testRecordData.getFac(),
  //        getClass().getName()+": item not found for num: "+ 
  //              "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column+
  //              "; msg: "+ex.getMessage(),
  //              FAILED_MESSAGE);
  //  }
  //}
  /** <br><em>Purpose:</em> actually do the click action
   * @param                     guiObj, GuiTestObject
   * @param                     row, int
   * @param                     col, int
   **/
  //protected void actionClick (GuiTestObject guiObj, int row, int col,
  //                            String rowval, String column) throws SAFSException {
  //  try {
  //    Cell cell = getCell(row, col);
  //    if (action.equalsIgnoreCase(CLICK)) {
  //      ((GuiSubitemTestObject)guiObj).click(cell);
  //    } else if (action.equalsIgnoreCase(DOUBLECLICK) ||
  //               action.equalsIgnoreCase(ACTIVATECELL)) {
  //      ((GuiSubitemTestObject)guiObj).doubleClick(cell);
  //    } else {
  //      Row trow = script.localAtRow(row);
  //      ((GuiSubitemTestObject)guiObj).setState(Action.select(), trow); // this doesn't get the cell, we have to click...
  //      ((GuiSubitemTestObject)guiObj).click(cell);
  //    }
  //    // set status to ok
  //    testRecordData.setStatusCode(StatusCodes.OK);
  //  } catch (SubitemNotFoundException ex) {
  //    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  //    log.logMessage(testRecordData.getFac(),
  //        getClass().getName()+": item not found for num: "+ 
  //              "row: "+row+", col: "+col+", or rowval: "+rowval+", or column name: "+column+
  //              "; msg: "+ex.getMessage(),
  //              FAILED_MESSAGE);
  //  }
  //}

  /** <br><em>Purpose:</em> get a cell based on row and col, uses 'getCells' method
   * @param                     jctable, TestObject
   * @param                     row, int
   * @param                     col, int
   * @return                    Object, contents of cell
   * @exception SAFSException based on caught Exception, like index out of bounds, etc.
   **/
  protected Object getCell (TestObject jctable, int row, int col) throws SAFSException  {
    //jctable = getJCTable(jctable);
    try {
      Boolean autoSort = new Boolean(false);
      try {
        autoSort = (Boolean)jctable.getProperty("autoSort");
      } catch (Exception pe) {}
      Log.info("autoSort: "+autoSort);
      TestObject dso1 = (TestObject)jctable.getProperty("model");
      Integer numColumns = (Integer) dso1.getProperty("columnCount");
      Log.info("**************numColumns: "+numColumns);
      Integer numRows = (Integer) dso1.getProperty("rowCount");
      Log.info("**************numRows: "+numRows);

      Object[] coords = new Object[2];
      coords[0]=new Integer(row);//row1
      coords[1]=new Integer(col);//col1
      Object ncell = dso1.invoke("getValueAt", "(II)Ljava/lang/Object;", coords);
      log.logMessage(testRecordData.getFac(), "ncell: "+ncell, DEBUG_MESSAGE);
      String nstr = "";
      if (ncell instanceof TestObject) {
        TestObject cell = (TestObject) ncell;
        Tree tree = null;
        try {
          tree = extractTreeItemsSub(cell);
          log.logMessage(testRecordData.getFac(),"   cell is a tree: "+tree, DEBUG_MESSAGE);
          log.logMessage(testRecordData.getFac(),"   ..........", DEBUG_MESSAGE);
          String[] atree = tree.toStringArrayWOSiblings();
          String[] stree = atree;
          if (autoSort.booleanValue()) {
            stree = StringUtils.getSortArray(atree);
          }
          for(int i=0; i<atree.length; i++) {
            Log.info("i: "+i+", "+atree[i]);
          }
          Log.info("   ..........");
          for(int i=0; i<stree.length; i++) {
            Log.info("i: "+i+", "+stree[i]);
          }
          nstr = stree[0];// this first one is the parent, the rest are the children.
          //the children are shown if the user double clicks the parent.
        } catch (Exception ee) {
          // probably because the cell isn't a tree, try something else...
          log.logMessage(testRecordData.getFac(),"vvvvvvvvvvvvvvvvvvvvvvvvvvvv", 
                         DEBUG_MESSAGE);
          listNonValueProperties(cell);
          log.logMessage(testRecordData.getFac(),"vvvvvvvvvvvvvvvvvvvv",
                         DEBUG_MESSAGE);
          listProperties(cell);
          log.logMessage(testRecordData.getFac(),"vvvvvvvvvvvv", 
                         DEBUG_MESSAGE);
          Object time = cell.getProperty("time");
          nstr = ""+time;
          Log.info("TIME: "+time);
        }
      } else {
        nstr = (String) (ncell==null ? "" : ncell.toString());
      }
      log.logMessage(testRecordData.getFac(),"::::::::::::::::cell: "+nstr,
                     DEBUG_MESSAGE);
      return nstr;
      //Log.debug("yyyyyyyyyyyyyyyyyyyyyyyyyyyy");
      //listNonValueProperties(jctable);
      //Log.debug("yyyyyyyyyyyyyyyyyyyyy");
      //listProperties(jctable);
      //Log.debug("yyyyyyyyyyyyyy");
      //listMethods(jctable);
      //TestObject dso2 = (TestObject)dso1.getProperty("treeTableModel");//contains 'columnName' prop
      //TestObject root = (TestObject)dso2.getProperty("root");
      //Tree tree = null;
      //try{
      //  tree = tobj(root);
      //} catch (Exception ee) {
      //  ee.printStackTrace();
      //  throw new SAFSException(this, "getCell", ee.getMessage());
      //}
      //Log.info(""+tree);

      //Log.info("o: "+root);
      //Log.debug("xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
      //listNonValueProperties(root);
      //Log.debug("xxxxxxxxxxxxxxxxxxx");
      //listProperties(root);
      //Log.debug("xxxxxxxxxxxxxxx");
      //listMethods(dso);
    } catch (Exception e) {
      throw new SAFSException(e.toString());
    }
  }

  /** <br><em>Purpose:</em> getUserObject, check if obj instanceof TestObject,
   ** if not, return obj, if so, grab the 'programName' property value and return that
   * @param                     obj, Object obj
   * @param                     level, Integer
   * @return                    either obj, or if a TestObject, then getProperty("programName");
   **/
  protected Object getUserObject (Object obj, Integer level) {
    if (obj instanceof TestObject) {
      listNonValueProperties((TestObject)obj);
      listProperties((TestObject)obj);
      //use 'programName'
      String stuff = (String) ((TestObject)obj).getProperty("programName");
      Log.info(StringUtils.getSpaces(level)+ stuff);
      return stuff;
    } else {
      log.logMessage(testRecordData.getFac(),
                     StringUtils.getSpaces(level)+ obj,
                     DEBUG_MESSAGE);
      return obj;
    }
  }
}
