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
package org.safs.rational.custom;

import org.safs.Log;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;


public class CFTable extends org.safs.rational.CFTable{
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
	  		String objectClassName = guiObj.getObjectClassName();
	  		Log.info(debugmsg + " processing on table, which type is " + guiObj.getObjectClassName());
	  		//For custom table "com.sas.workspace.WsTable", we try to get cell text from cell render firstly, refer to S0725571
			if("com.sas.workspace.WsTable".equals(objectClassName)){
		  		//The parameter row and col describe the position of the value in the table view NOT model!!!!
		  		Object[] getColName = new Object[1];
				getColName[0] = new Integer(col);
				String columnName = (String) guiObj.invoke("getColumnName", "(I)", getColName);
				rval[1] = columnName;
				//As the row and col are the position in the table view, we can just call
				//"getValueAt" of JTable, it will return the value locating at the (row,col) of table view (NOT model)
		  		Object[] rowColCoords = new Object[2];
			    rowColCoords[0] = new Integer(row);
			    rowColCoords[1] = new Integer(col);
		  		Object tblModelValue = (Object) (guiObj.invoke("getValueAt","(II)",rowColCoords));
		        
		        // Try to use java swing API to get the cell render text directly
				try {
					Object[] cellRendCompParams = new Object[6];
					cellRendCompParams[0] = guiObj;
					cellRendCompParams[1] = tblModelValue;
					cellRendCompParams[2] = new Boolean(false);
					cellRendCompParams[3] = new Boolean(false);
					cellRendCompParams[4] = rowColCoords[0];
					// the column of parameter in getCellRenderer() is the model
					// column number
					// we need to convert the view column number to model column
					// number
					Object[] viewColumn = new Object[1];
					viewColumn[0] = col;
					Integer modelColumn = (Integer) (guiObj.invoke("convertColumnIndexToModel", "(I)", viewColumn));
					cellRendCompParams[5] = modelColumn;

					TestObject tblCellRend = (TestObject) guiObj.invoke("getCellRenderer", "(II)", rowColCoords);

					Object tblCellRendComp = tblCellRend.invoke(
							"getTableCellRendererComponent",
							"(Ljavax.swing.JTable;Ljava.lang.Object;ZZII)",
							cellRendCompParams);

					if (tblCellRendComp instanceof TestObject) {
						rval[0] = (String) ((TestObject) tblCellRendComp).getProperty("text");

						if (rval[0].equals("")) {
							rval[0] = getTableCellSubItemText(guiObj, row, col);
						}
					} else {
						rval[0] = tblCellRendComp.toString();
					}
				} catch (MethodNotFoundException e) {
					Log.debug(debugmsg+ " Method getCellRenderer or getTableCellRendererComponent not found.");
				} 

				Log.info(debugmsg + " cell text is " + rval[0]);
			}
	  		
			//If we can not get the value from the cell render, let the super class to process
			if (rval[0].equals("")) {
				Log.info(debugmsg + " try to get from super class. ");
				return super.getCellText(guiObj, tblModel, row, col);
			}
	  	}catch (Exception oe) {
			Log.debug(debugmsg + " Exception occured: " + oe.getMessage());
			Log.info(debugmsg + " try to get from super class. ");
			return super.getCellText(guiObj, tblModel, row, col);
		}
	  	
	    return rval;
	  }
}
