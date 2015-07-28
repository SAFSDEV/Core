/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational.wpf;

import org.safs.rational.CFPageTabList;
import org.safs.rational.Script;
import org.safs.text.FAILStrings;
import org.safs.StatusCodes;
import org.safs.SAFSException;
import org.safs.Log;

import com.rational.test.ft.object.interfaces.WPF.WpfGuiSubitemTestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.Header;
import com.rational.test.ft.script.SubitemFactory;


/**
 * <br><em>Purpose:</em> CFWPFPageTabList, process a WPF tab-page  component 
 * @author  JunwuMa
 * @since   OCT 14, 2009
 * 
 **/
public class CFWPFPageTabList extends CFPageTabList {

  
	/** <br><em>Purpose:</em> process commands like: CLICKTAB, UNVERIFIEDCLICKTAB, SELECTTAB and MAKESELECTION
	 */
	protected void commandWithOneParam() throws SAFSException {
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String param1 = (String) params.iterator().next();
			Log.info("... "+ action + " param: " + param1);
			
			WpfGuiSubitemTestObject guiObj = new WpfGuiSubitemTestObject(obj1.getObjectReference());
			
			if (action.equalsIgnoreCase(CLICKTAB)||
				action.equalsIgnoreCase(UNVERIFIEDCLICKTAB)||
				action.equalsIgnoreCase(SELECTTAB) ||
				action.equalsIgnoreCase(MAKESELECTION)) {
				try {
					// not verified
					Header header = SubitemFactory.atHeader(Script.localAtText(param1));
					guiObj.click(header);
				} catch (Exception ex) { 
					Log.debug(getClass().getName()+".commandWithOneParam() " + ex.getMessage());
					throw new SAFSException(ex.toString());
				}
			} else if (action.equalsIgnoreCase(SELECTTABINDEX)){
				// parse index parameter
				int index;
		    	try {
		    		index = Integer.parseInt(param1);
		    		// treat all tabs as 1-based even if they are 0-based
		    		if (index < 1) {    			  	
		    			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			            String detail = failedText.convert("bad_param", "Invalid parameter value for "+ param1, param1);
			            componentFailureMessage(detail+"="+param1);
			            return;
		    		 }
		    	} catch (NumberFormatException nfe) {
		              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		              String detail = failedText.convert("bad_param", "Invalid parameter value for "+ param1, param1);
		              componentFailureMessage(detail+"="+param1);
		              return;
		    	}
		    	
		    	Header header = SubitemFactory.atHeader(Script.localAtIndex(index-1));
		    	guiObj.click(header);	
			}
			// set status to ok
			testRecordData.setStatusCode(StatusCodes.OK);
			String altText = windowName+":"+compName+" "+action +" successful using "+ param1;
			log.logMessage(testRecordData.getFac(),
							passedText.convert("success3a", altText, windowName, compName, action, param1),
							PASSED_MESSAGE);
		}
	} 
}