package org.safs.rational.custom;

import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.rational.DotNetUtil;
import org.safs.rational.RGuiObjectRecognition;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.ITestDataTable;

/**
 * Extend core rational.CFList to handle SAS-specific classes.
 * 
 * @author CANAGL
 */
public class CFList extends org.safs.rational.CFList {

	public CFList() {
		super();
	}

	/**
	 * Override default CFList.captureObjectData for certain SAS-specific cases.
	 * Forward to super for all other cases.
	 */
	protected List captureObjectData(TestObject guiObj) throws SAFSException{
	  	java.util.List list = null;	    
	    if((isDotnetDomain(guiObj))&&
	       (DotNetUtil.isSubclassOf(DotNetUtil.getClazz(guiObj), "SAS.SharedUI.TreeListView"))){
			Log.info("SAS Custom CFList extracting list of items for " + guiObj.getObjectClassName());
			try {
				list = new ArrayList();
				// treat guiObj as DataGrid
				ITestDataTable testTable = (ITestDataTable)guiObj.getTestData("contents");				
				int rows = testTable.getRowCount();
				for (int i = 0; i < rows; i++) {
					// take the first column of this object as the ListBox treated as usual.  Fix S0553625.
					// each of the items in the first column will added to list.   
					Object nsub = testTable.getCell(i, 0); 
					list.add((String)nsub);
				} 
			} catch(Exception ex) {
				Log.info("SAS Custom CFList could NOT extract list of items for " + guiObj.getObjectClassName());
				throw new SAFSException("Custom CFList could NOT extract list of items");
			}    					        
		    return list;
		}
		else {
			return super.captureObjectData(guiObj);
		}
	}

	/**
	 * Overrides CFComponent.converObjectValueToString to handle SAS-specific .NET classes.
	 */
	protected String convertObjectValueToString(Object value) throws SAFSException{
		if ((value instanceof TestObject)&&(isDotnetDomain((TestObject)value))){
		    try{
				if (DotNetUtil.isSubclassOf(DotNetUtil.getClazz((TestObject)value), "SAS.Shared.SASFormatInfo")) {
					String strvalue = ((TestObject) value).getProperty("DisplayName").toString();
					return strvalue + "w."; // make a item suffixed with "w." to keep the item consistent with the string shown on GUI.  
				}
			}catch(Exception x){
				Log.debug("SAS custom CFList.convertObjectValue needs to add more code to analyse "+value.toString());
			}
		}
		return super.convertObjectValueToString(value);
	}
}
