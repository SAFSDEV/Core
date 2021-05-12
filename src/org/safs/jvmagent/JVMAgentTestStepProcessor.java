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
package org.safs.jvmagent;

import org.safs.*;
import org.safs.jvmagent.*;
import java.util.Collection;
/**
 * This subclass of TestStepProcessor primarily catches many of the SAFSRuntimeExceptions 
 * produced by the RMI Agent implementation.  Other than that, the default TestStepProcessor 
 * functionality is not altered.
 * 
 * @author Carl Nagle
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
