/** Copyright (C) (SAS Institute, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.io.*;
import java.util.*;

import org.safs.*;
import org.safs.natives.NativeWrapper;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.drivers.DriverConstant;

/**
 * <br><em>Purpose:</em> DCDriverFlowCommands, process driver flow commands
 * <br><em>Lifetime:</em> instantiated by DCDriverCommand
 * im
 * <p>
 * @author Carl Nagle
 * @since   Feb 04, 2010
 **/
public class DCDriverFlowCommands extends DriverCommand {
	
	public static final String ON_REGISTRY_KEY_EXIST_COMMAND     = "OnRegistryKeyExistGotoBlockID";
	public static final String ON_REGISTRY_KEY_NOT_EXIST_COMMAND = "OnRegistryKeyNotExistGotoBlockID";

	String command = null;
	
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverFlowCommands () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is the driver command processor for flow commands.
   ** <br> 
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
    try {
      command = testRecordData.getCommand();
      if (command.equalsIgnoreCase(ON_REGISTRY_KEY_EXIST_COMMAND) ||
    	  command.equalsIgnoreCase(ON_REGISTRY_KEY_NOT_EXIST_COMMAND)) {
    	  cmdOnRegistryKeyExistGoto();
      } else {
        setRecordProcessed(false);
      }
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

	private void cmdOnRegistryKeyExistGoto() throws SAFSException{
		boolean cmdexists = command.equalsIgnoreCase(ON_REGISTRY_KEY_EXIST_COMMAND);
		final String TIMEOUT_DEFAULT = "15";
		String blockid = "";
		String keyname = "";
		String valuename = "";
		String strtimeout = "";
		String message = null;
		String detail = null;
		int tseconds = 15;
		boolean isWarnOK = testRecordData.getRecordType().equalsIgnoreCase(DriverConstant.RECTYPE_CW);
		long status_warn = isWarnOK ? DriverConstant.STATUS_NO_SCRIPT_FAILURE:DriverConstant.STATUS_SCRIPT_WARNING;		
		try{ blockid = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(Exception npx){}
		if ( blockid.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for BLOCKID", "BLOCKID");
			issueParameterCountFailure(message);
			return;
		}

		try{ keyname = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
		catch(Exception npx){}
		if ( keyname.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for KEY", "KEY");
			issueParameterCountFailure(message);
			return;
		}
		String msgval = keyname;
		
		try{ valuename = testRecordData.getTrimmedUnquotedInputRecordToken(4);}
		catch(Exception npx){}
		if ( valuename.length()==0 ) valuename = null;
	    if (valuename == null){
			Log.info(command +" seeking a Key.  No KeyValue specified...");
		}else{
			msgval +=":"+ valuename;
		}

	    try{ strtimeout = testRecordData.getTrimmedUnquotedInputRecordToken(5);}
		catch(Exception npx){}
		if ( strtimeout.length()==0 ) strtimeout = TIMEOUT_DEFAULT;
	    try{ 
			tseconds = Integer.parseInt(strtimeout);
			if(tseconds < 0) {
				tseconds = 0;
				strtimeout = "0";
			}
			Log.info(command +" using TIMEOUT value "+ strtimeout);
		}
		catch(NumberFormatException nf){
			strtimeout = TIMEOUT_DEFAULT;
			Log.info(command +" IGNORING invalid TIMEOUT value. Using Default "+ TIMEOUT_DEFAULT);
		}
		boolean exists = false;
		boolean matched = false;
		for(int i=0;!matched && i<=tseconds;i++){ 
			exists = NativeWrapper.DoesRegistryKeyExist(keyname, valuename);
			matched = (cmdexists == exists);
		  	if(!matched && i<tseconds) try{Thread.sleep(1000);}catch(Exception x){;}	  		
		}
		if(matched){
	  		message = genericText.convert(GENStrings.BRANCHING, 
					command +" attempting branch to "+ blockid, 
					command, blockid);
			if(cmdexists){
		  		detail = genericText.convert(GENStrings.FOUND_TIMEOUT, 
						msgval +" was found within timeout "+ strtimeout,
						msgval, strtimeout);
			}else{
		  		detail = genericText.convert(GENStrings.GONE_TIMEOUT, 
						msgval +" was gone within timeout "+ strtimeout,
						msgval, strtimeout);
			}
			issueGenericSuccess(message, detail);
			testRecordData.setStatusCode(DriverConstant.STATUS_BRANCH_TO_BLOCKID);
			testRecordData.setStatusInfo(blockid);
	  		return;
		// not matched
		}else{
	  		message = genericText.convert(GENStrings.NOT_BRANCHING, 
					command +" did not branch to "+ blockid, 
					command, blockid);
			if(cmdexists){
		  		detail = failedText.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
						msgval +" was not found within timeout "+ strtimeout,
						msgval, strtimeout);
			}else{
		  		detail = failedText.convert(FAILStrings.NOT_GONE_TIMEOUT, 
						msgval +" was not gone within timeout "+ strtimeout,
						msgval, strtimeout);
			}
	  		if(isWarnOK){
				issueGenericSuccess(message, detail);
	  		}else{
	  			issueActionWarning(message +" "+ detail);
	  		}
	        return;
        }		  
	}
  
}

