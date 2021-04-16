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
package org.safs.staf;

import org.safs.STAFHelper;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.queue.Fifo;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

public class STAFHandleInterface implements HandleInterface {

	STAFHandle handle;
	String handleId;
	
	/**
	 * Must setHandleId and register before using the Handle.
	 */
	public STAFHandleInterface(){}

	/**
	 * If no STAFException is thrown then the Handle is registered.
	 */
	public STAFHandleInterface(String handleId)throws STAFException{
		setHandleId(handleId);
		register();
	}
	
	/**
	 * Must register before using the Handle.
	 */
	@Override
	public void setHandleId(String handleId){
		this.handleId = handleId;
	}
	
	@Override
	public void register() throws STAFException {
		handle = new STAFHandle(handleId);
	}

	@Override
	public String submit(String where, String handler, String request) throws STAFException {
		return handle.submit(where, handler, request);
	}

	@Override
	public STAFResult submit2(String where, String handler, String request) {
		return handle.submit2(where, handler, request);
	}

	@Override
	public STAFResult sendQueueMessage(String target, String message) throws STAFException {
		return handle.submit2(STAFHelper.LOCAL_MACHINE, "QUEUE","QUEUE MESSAGE "+ 
	                                             STAFHelper.lentagValue(message)+" "+
				                                 "NAME "+ STAFHelper.lentagValue(target));
	}

	@Override
	public STAFResult acceptQueueMessage(String message) {
		// does nothing
		return null;
	}

	@Override
	public STAFResult getQueueMessage(int timeout) {
	    String service = "QUEUE";
	    String command = " GET";
	    
//	    if (handleName != null) {
//	      command = command + " NAME " + STAFHelper.lentagValue(handleName);
//	    }
	    if(Fifo.TIMEOUT_NO_WAIT==timeout){
	    	//no wait, return immediately
	    }else if(Fifo.TIMEOUT_WAIT_FOREVER==timeout){
	    	command = command + " WAIT";	    	
	    }else{
	    	command = command + " WAIT "+String.valueOf(timeout);	    	
	    }
	    
	    return handle.submit2(STAFHelper.LOCAL_MACHINE, service, command);
	}

	@Override
	public int getHandle() {
		return handle.getHandle();
	}

	@Override
	public void unRegister() throws STAFException {
		handle.unRegister();
	}
}
