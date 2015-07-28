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
