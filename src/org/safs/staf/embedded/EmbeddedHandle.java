package org.safs.staf.embedded;

import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.queue.Fifo;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

/**
 * A handle subclass used by clients of Services.
 * @author Carl Nagle
 * @see EmbeddedServiceHandle
 */
public class EmbeddedHandle implements HandleInterface{
	public static final String DEFAULT_QUEUE_NAME   = "DEFAULT_QUEUE";
	
	protected String handleId;
	protected Fifo<String> queue = new Fifo<String>();
	protected boolean isRegistered = false;
	protected STAFHandle stafHandle;
	
	/**
	 * Create and Register our STAFHandle subclass with STAF *and* our non-STAF handlers.
	 * @param handleId
	 * @throws STAFException
	 */
	public EmbeddedHandle(String handleId) throws STAFException {
		//super(handleId);// can throw a STAFException if STAF is not running or handle in-use.
		setHandleId(handleId);
	}
	
	/** If used, must be called before registering. */
	public void setHandleId(String handleId){
		this.handleId = handleId;
	}
	
	/** 
	 * Subclasses call this to invoke the overridden registerHandle function and set the isRegistered flag.
	 * @throws STAFException
	 */
	public void register() throws STAFException {
		if(! isRegistered) {
			registerHandle();
		}
		isRegistered = true;
	}
	
	/**
	 * Override in subclasses.  Will be called by the register() function.
	 * @throws STAFException
	 */
	protected void registerHandle() throws STAFException {
		try{ 
			EmbeddedHandles.registerHandle(handleId, this); 
			if((!STAFHelper.no_staf_handles)&& (stafHandle == null) ) stafHandle = new STAFHandle(handleId);
		}
		catch(IllegalArgumentException x) {
			throw new STAFException(STAFResult.InvalidHandle, x.getMessage());}
		catch(SAFSException x){
			throw new STAFException(STAFResult.HandleAlreadyExists, x.getMessage());}
	}
	
	/**
	 * Send a submit request to a registered (non-STAF) service Handle.
	 * @return the String result from the service response.
	 * @throws STAFException if the service is not registered.
	 * @see org.safs.staf.embedded.HandleInterface#submit(java.lang.String, java.lang.String)
	 */
	public String submit(String handler, String request) throws STAFException {
		if(! isRegistered ) register();
		try{
			ServiceInterface service = EmbeddedHandles.getService(handler);
			STAFResult sr = service.acceptRequest(new InfoInterface.RequestInfo(STAFHelper.LOCAL_MACHINE, getHandle(), handleId, request));
			if(sr.rc == STAFResult.Ok) return sr.result;
			throw new STAFException(sr.rc, sr.result);
		}catch(IllegalArgumentException x){
			throw new STAFException(STAFResult.ServiceNotAvailable, x.getMessage());
		}
	}

	/**
	 * Send a submit2 request to a registered (non-STAF) service Handle.
	 * @return STAFResult service response.
	 * @throws STAFException if the service is not registered.
	 * @see org.safs.staf.embedded.HandleInterface#submit2(java.lang.String, java.lang.String)
	 */
	public STAFResult submit2(String handler, String request) throws STAFException{
		if(! isRegistered ) register();
		try{
			ServiceInterface service = EmbeddedHandles.getService(handler);			
			return service.acceptRequest(new InfoInterface.RequestInfo(STAFHelper.LOCAL_MACHINE, getHandle(), handleId, request));
		}catch(IllegalArgumentException x){
			throw new STAFException(STAFResult.ServiceNotAvailable, x.getMessage());
		}
	}

	/**
	 * Receive a queue message into our (non-STAF) FIFO queue.
	 * @see org.safs.staf.embedded.HandleInterface#acceptQueueMessage(java.lang.String)
	 */
	@Override
	public STAFResult acceptQueueMessage(String message){
		try {
			if(! isRegistered ) register();
			queue.in(message);
			return new STAFResult(STAFResult.Ok);
		}catch (SAFSException e) {
			if(e.getCode().equals(SAFSException.CODE_CONTAINER_ISFULL)){
				return new STAFResult(STAFResult.QueueFull);
			}
			return new STAFResult(STAFResult.UnknownError, StringUtils.debugmsg(e));
		}catch(Exception e){
			return new STAFResult(STAFResult.UnknownError, StringUtils.debugmsg(e));
		}
	}
	
	/**
	 * Send a queue message to the FIFO queue of the specified (non-STAF) handler (handle/service).
	 * @see org.safs.staf.embedded.HandleInterface#sendQueueMessage(java.lang.String, java.lang.String)
	 */
	@Override
	public STAFResult sendQueueMessage(String handler, String message) throws STAFException{
		if(! isRegistered ) register();
		try{
			HandleInterface handle = EmbeddedHandles.getHandle(handler);
			return handle.acceptQueueMessage(message);
		}catch(IllegalArgumentException x){
			throw new STAFException(STAFResult.ServiceNotAvailable, x.getMessage());
		}
	}

	/**
	 * @return get a queue message from our (non-STAF) FIFO queue, or null if there are no messages.
	 */
	@Override
	public STAFResult getQueueMessage(int timeout){
		try{
			if(! isRegistered ) register();
			String message = queue.out(timeout);
			return new STAFResult(STAFResult.Ok, message);
		}catch (SAFSException e) {
			if(SAFSException.CODE_TIMEOUT_REACHED.equals(e.getCode())){
				return new STAFResult(STAFResult.Timeout);
			}else if(SAFSException.CODE_CONTAINER_ISEMPTY.equals(e.getCode())){
				return new STAFResult(STAFResult.NoQueueElement);
			}
			return new STAFResult(STAFResult.UnknownError, StringUtils.debugmsg(e));
		}catch(Exception e){
			return new STAFResult(STAFResult.UnknownError, StringUtils.debugmsg(e));
		}
	}

	/**
	 * Tries to use a registered (non-STAF) Handler first before resorting to the super (STAF) submit. 
	 * @see com.ibm.staf.STAFHandle#submit(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String submit(String machine, String handler, String request) throws STAFException {
		if(! isRegistered ) register();
		try{
			return submit(handler, request);
		}catch(STAFException x){ 
			//IndependantLog.debug("EmbeddedHandle.submit ignoring "+x.getClass().getName()+": "+ x.getMessage(), x); 
		}
		if(stafHandle != null) return stafHandle.submit(machine, handler, request);
		throw new STAFException(STAFResult.STAFRegistrationError, "STAFHandle not registered.");
	}

	/**
	 * Tries to use a registered (non-STAF) Handler first before resorting to the super (STAF) submit2. 
	 * @see com.ibm.staf.STAFHandle#submit2(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public STAFResult submit2(String machine, String handler, String request) {
		try{ if(! isRegistered ) register();}
		catch(STAFException x){
			return new STAFResult(STAFResult.STAFRegistrationError, "Registration Error");
		}
		try{
			return submit2(handler, request);
		}catch(STAFException x){ 
			//IndependantLog.debug("EmbeddedHandle.submit2 ignoring "+x.getClass().getName()+": "+ x.getMessage(), x); 
		}
		if(stafHandle != null) return stafHandle.submit2(machine, handler, request);
		return new STAFResult(STAFResult.STAFRegistrationError, "STAFHandle is not registered.");
	}

	/**
	 * Unregisters the Handle from both our registered (non-STAF) handlers and STAF, if applicable. 
	 * @see com.ibm.staf.STAFHandle#unRegister()
	 */
	@Override
	public void unRegister(){
		EmbeddedHandles.unRegister(handleId);
		try{ stafHandle.unRegister();}catch(Exception ignore){}
	}

	@Override
	public int getHandle() {
		return (stafHandle instanceof STAFHandle) ? stafHandle.getHandle(): -1; 
	}
	
	public void clearQueue(){
		queue.clear();
	}
}
