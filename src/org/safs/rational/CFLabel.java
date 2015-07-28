/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.*;

import org.safs.*;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;

/**
 * <br><em>Purpose:</em> CFLabel, process a LABEL component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUL 16, 2003
 *
 *   <br>   JUL 16, 2003    (DBauman) Original Release
 *   <br>	JAN 10, 2011	(JunwuMa) Added captureObjectData overriding its super to support keywords 
 *   								  CAPTUREOBJECTDATATOFILE/VERIFYOBJECTDATATOFILE.
 **/
public class CFLabel extends CFComponent {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFLabel () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** We first call super.process() [which handles actions like 'click']
   ** The actions handled here are:
   ** <br><ul>
   ** <li>none
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    //?? none for now, similar to CFButton, so check there to see if anything was added.
  }
  
  /**
   * Overrides its super for supporting CFLabel specifically.
   * For CFLabel, we extract the "text", "Text" or ".text" property from the TestObject.
   * 
   * @param table TestObject to snapshot data from.
   * 
   * @return List containing a single Object item.  Null if an invalid table reference is 
   * provided or some other error occurs.
   * 
   * @throws SAFSException
   * @throws IllegalArgumentException, not happen in this procedure.
   */
  protected java.util.List captureObjectData(TestObject table)throws IllegalArgumentException, SAFSException{
  	String[] props = { "text",    // Java 
  					   "Text",    // DotNet
  					   ".text" }; // Html 
    Object rval = null;
	for(int i=0; i<props.length; i++) {
	    try {
	    	Log.info("CFLabel.captureObjectData: trying to get its property " + props[i]);
	    	rval = table.getProperty(props[i]); //may throw PropertyNotFoundException
	    	if (rval != null) {
	    		Log.info("..... success to get its property: " + props[i]);
	    		break; //take this as expecting value
	    	}	
	    } catch (PropertyNotFoundException nfe){
    		Log.info("...PropertyNotFoundException: no property '"+ props[i] + "'");
	    }
	}
    Log.info("..... captureObjectData value: "+ rval);
    if (rval == null) return null;
    java.util.List list = new ArrayList();
    list.add(rval);
    return list;
  }
}
