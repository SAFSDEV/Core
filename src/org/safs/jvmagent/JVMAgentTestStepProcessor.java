/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.*;
import org.safs.jvmagent.*;
import java.util.Collection;
/**
 * This subclass of TestStepProcessor primarily catches many of the SAFSRuntimeExceptions 
 * produced by the RMI Agent implementation.  Other than that, the default TestStepProcessor 
 * functionality is not altered.
 * 
 * @author canagl
 * @since Apr 15, 2005
 */
public class JVMAgentTestStepProcessor extends TestStepProcessor {

	/**
	 * Constructor for JVMAgentTestStepProcessor.
	 */
	public JVMAgentTestStepProcessor() {
		super();
	}

    /** 
     * Overrides TestStepProcessor.initProcessorAndProcess to allow us to intercept 
     * the Exceptions we throw and respond log accordingly.
     * This provides some default error processing for ALL CF libraries.
     * <p>
     * @param                     aprocessor, Processor
     * @param                     params, Collection
     * @return true if it processed the record, false otherwise 
     **/
    protected boolean initProcessorAndProcess (Processor aprocessor, Collection params) {
		
		// we actually use the default routine but catch our own Exceptions
		try{ return super.initProcessorAndProcess(aprocessor, params); }		
        
        catch(SAFSMissingActionArgumentRuntimeException ia){
        	componentFailureMessage(failedText.text("paramsize")+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        catch(SAFSActionUnsupportedRuntimeException au){
        	//componentFailureMessage(failedText.convert("support_not_found", "Support not found for "+ testRecordData.getCommand(), testRecordData.getCommand())+":"+testRecordData.getInputRecord());
        	//testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	aprocessor.setRecordProcessed(false);
        	return false;
        }
        catch(SAFSInvalidActionArgumentRuntimeException ia){
        	componentFailureMessage(failedText.convert("bad_param", "Bad Param for "+ testRecordData.getCommand(), testRecordData.getCommand())+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        catch(SAFSActionErrorRuntimeException ae){
        	componentFailureMessage(ae.getMessage()+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        catch(SAFSInvalidActionRuntimeException ia){
        	componentFailureMessage(failedText.text("action_not_valid")+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        catch(SAFSInvalidComponentRuntimeException ic){
        	componentFailureMessage(failedText.text("object_not_valid")+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        catch(SAFSObjectNotFoundRuntimeException nf){
        	componentFailureMessage(failedText.text("object_not_found")+":"+testRecordData.getInputRecord());
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }
        return true;
  	}
  	
}
