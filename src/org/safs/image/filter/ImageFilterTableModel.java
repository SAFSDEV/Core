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
package org.safs.image.filter;

import javax.swing.table.AbstractTableModel;

/**
 * Asset associated with {@link ImageFilterGUI} 
 */
class ImageFilterTableModel extends AbstractTableModel {
		private String[] columnNames = {"X1","Y1","X2","Y2","WIDTH","HEIGHT","On/Off"};
		private Object[][] data = {{"0","0","0","0","0","0", Boolean.FALSE}};
		
		public void reinitialize(){
			data = new Object[1][7];
			data[0][0] = "0";
			data[0][1] = "0";
			data[0][2] = "0";
			data[0][3] = "0";
			data[0][4] = "0";
			data[0][5] = "0";
			data[0][6] = Boolean.FALSE;
		}
		
		public void removeRow(int i){
			if(data.length <= 1){
				reinitialize();
			}else {
				Object[][] temp = new Object[data.length-1][7];
				for(int j = 0; j < temp.length; j++){
					if(j < i){
						temp[j] = data[j];
					} else {
						temp[j] = data[j+1];
					}
				}
				data = temp;
			}
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			return true;
			
		}
			
		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			Object[][] temp;
			
			if(row > data.length-1){
				temp = data;
				data = new Object[row+1][temp[0].length];
				for(int i = 0; i< temp.length; i++){
					for(int j = 0; j<temp[0].length; j++){
						data[i][j] = temp[i][j];
					}
				}
				data[row][col] = value;
				fireTableDataChanged();
			} else {
				data[row][col] = value;
				fireTableCellUpdated(row, col);
			}
		}
		
		public void setValueAtSimple(Object value, int row, int col) {
			data[row][col] = value;
		}
		
	}
