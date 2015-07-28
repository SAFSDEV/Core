package org.safs.staf.embedded;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

public interface HandleInterface {

	public void setHandleId(String handleId);
	public void register() throws STAFException;
	public String submit(String where, String handler, String request) throws STAFException;
	public STAFResult submit2(String where, String handler, String request);
	
	/**
	 * @param target -- destination handle name for the message.
	 * @param message -- the message to send.
	 */
	public STAFResult sendQueueMessage(String target, String message) throws STAFException;
	public STAFResult acceptQueueMessage(String message);
	public STAFResult getQueueMessage(int timeout);
	public int getHandle();
	public void unRegister()throws STAFException;
}
