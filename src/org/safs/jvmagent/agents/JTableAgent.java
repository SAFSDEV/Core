/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import java.util.Enumeration;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.table.TableColumn;

import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.Log;


/**
 * @author Carl Nagle
 *
 * Feb 23, 2006 (Szucs) adding the getSubItemAtIndex( ) method
 */
public class JTableAgent extends JChildlessAgent {

	/** 
	 * "JTable"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JTable";
	
	/**
	 * Constructor for Agent.
	 */
	public JTableAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.JComponentAgent.class.getName());
	}

	/**
	 * ";"
	 * Separator used to delimit individual items in indexed fields like columnNames, etc..
	 * Default value is the semicolon ";".
	 */
	public static String INDEX_SEP = ";";
	
	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getColumnNames(JTable table){
		String names = "";
		for (int i=0; i< table.getColumnCount();i++) {
			names += table.getColumnName(i);
			if(i< table.getColumnCount()-1) names += INDEX_SEP;
		}
		return names;
	}

	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getColumnWidths(JTable table){
		String widths = "";
		Enumeration enumerator = table.getColumnModel().getColumns();
		while(enumerator.hasMoreElements()) {
			widths += String.valueOf(((TableColumn)enumerator.nextElement()).getWidth());
			if(enumerator.hasMoreElements()) widths += INDEX_SEP;
		}
		return widths;
	}

	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getRowHeights(JTable table){
		String heights = "";
		for (int i=0; i< table.getRowCount();i++) {
			heights += table.getRowHeight(i);
			if(i< table.getRowCount()-1) heights += INDEX_SEP;
		}
		return heights;
	}

	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getSelectedColumns(JTable table){
		String columns = "";
		int[] selected = table.getSelectedColumns();
		for (int i=0; i< selected.length;i++) {
			columns += String.valueOf(selected[i]);
			if(i< selected.length -1) columns += INDEX_SEP;
		}
		return columns;
	}

	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getSelectedRows(JTable table){
		String rows = "";
		int[] selected = table.getSelectedRows();
		for (int i=0; i< selected.length;i++) {
			rows += String.valueOf(selected[i]);
			if(i< selected.length -1) rows += INDEX_SEP;
		}
		return rows;
	}

	/**
	 * Returns an INDEX_SEP delimited list of values.
	 */
	protected String getHeaderValues(JTable table){
		String values = "";
		Enumeration enumerator = table.getColumnModel().getColumns();
		while(enumerator.hasMoreElements()) {
			values += ((TableColumn)enumerator.nextElement()).getHeaderValue().toString();
			if(enumerator.hasMoreElements()) values += INDEX_SEP;
		}
		return values;
	}

	public Object getSubItemAtIndex( Object object, int index ) throws Exception {
            JTable table = ( JTable )object;
            int rowCount = table.getRowCount( );
            int colCount = table.getColumnCount( );
            
            if ( ( index  < 0 ) || ( index >= rowCount * colCount ) ) {
                throw new Exception( "JTable item index is out of range" );
            } else {
                Object obj = table.getValueAt( ( index + 1 ) / colCount - 1, ( index + 1 ) % colCount - 1 );
                if ( obj == null ) return "";
                else return obj;
            }
        }                                
}
