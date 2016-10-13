package org.safs.staf.embedded;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.SAFSException;

/**
 * Handles the storage, registration, and un-registration of Handles and Services.
 * @author canagl
 */
public class EmbeddedHandles {

	/** map a handle name to the actual owner */
	private static Hashtable<String,EmbeddedHandle> handles = new Hashtable<String, EmbeddedHandle>();
	
	/** map a service name to a handle name */
	private static Hashtable<String,String> services = new Hashtable<String, String>();

	private EmbeddedHandles() { }
	
	/** normalize the provided id to be case-insensitive */
	private static String normId(String id){ return id.toUpperCase(); }

	/**
	 * @param handleId -- unique name for registering the Handle
	 * @param ihandle -- unique Handle object to be registered.
	 * @throws IllegalArgumentException - if null parameters are provided
	 * @throws SAFSException -- if the handle is already registered.
	 */
	public static void registerHandle(String handleId, EmbeddedHandle ihandle)throws IllegalArgumentException, SAFSException{
		if(handleId == null) throw new IllegalArgumentException("Handle ID cannot be null.");
		if(ihandle == null) throw new IllegalArgumentException("Handle Object cannot be null.");
		if(handles.containsKey(normId(handleId))) throw new SAFSException("Handle with this Id is already registered: "+ handleId);
		if(handles.contains(ihandle))throw new SAFSException("Handle is already registered under a different Id.");
		handles.put(normId(handleId), ihandle);
	}
	
	/**
	 * @param handleId -- unique name for registering the Handle
	 * @param ihandle -- unique Handle object to be registered.
	 * @throws IllegalArgumentException - if null parameters are provided
	 * @throws SAFSException -- if the handle is already registered.
	 */
	public static void registerService(String handleId, String serviceId, EmbeddedServiceHandle ihandle)throws IllegalArgumentException, SAFSException{
		if(serviceId == null) throw new IllegalArgumentException("Service ID cannot be null.");
		if(services.containsKey(normId(handleId))) throw new SAFSException("Service with this Handle is already registered: "+ handleId);
		if(services.contains(normId(serviceId))) throw new SAFSException("Service with this Id is already registered: "+ serviceId);
		try{ 
			registerHandle(handleId, ihandle);
		}
		// this is expected when registering a service because the initial handle is already registered.
		catch(SAFSException ignore){
			ignore.printStackTrace();
		}
		services.put(normId(handleId), normId(serviceId));
	}
	
	/**
	 * Unregister (remove) the registered Handle.
	 * Does nothing if the handleId is invalid or was not registered.
	 * @param handleId
	 */
	public static void unRegister(String handleId){
		if(handleId == null) return;
		handles.remove(normId(handleId));
		services.remove(normId(handleId));
	}
	
	/**
	 * @param handleId
	 * @return HandleInterface to the embedded handler.
	 * @throws IllegalArgumentException -- if the id provided is NOT for a registered Handle.
	 */
	public static EmbeddedHandle getHandle(String handleId)throws IllegalArgumentException{
		if(! handles.containsKey(normId(handleId)))
			throw new IllegalArgumentException("Provided Handle Id is NOT a valid Handle: "+ handleId);
		return handles.get(normId(handleId));
	}

	/**
	 * @param serviceId
	 * @return ServiceInterface to the embedded service handler.
	 * @throws IllegalArgumentException -- if the id provided is null or is NOT for a registered Service.
	 */
	public static EmbeddedServiceHandle getService(String serviceId)throws IllegalArgumentException{
		if(serviceId == null)throw new IllegalArgumentException("Provided service Id cannot be null.");
		if(! services.contains(normId(serviceId)))
			throw new IllegalArgumentException("Provided service Id is NOT a registered Service: "+ serviceId);
		Set<String> set = services.keySet();
		String service = null;
		try{
			for(String handle: set){           // already normalized
				service = services.get(handle);// already normalized
				if(service.equalsIgnoreCase(serviceId)){
					return (EmbeddedServiceHandle) getHandle(handle); 
				}
			}
		}catch(ClassCastException cc){}
		throw new IllegalArgumentException("Provided service Id is NOT a registered Service: "+ serviceId);
	}
	
	/**
	 * @param handleId -- of the client/service sought.
	 * @return true if the handleId is registered.  false if not.
	 */
	public static boolean isToolRunning(String handleId){
		try{ return handles.containsKey(normId(handleId));}catch(NullPointerException x){}
		return false;
	}

	/**
	 * @param serviceId -- of the service sought.
	 * @return true if the service is registered.  false if not.
	 */
	public static boolean isServiceRunning(String serviceId){
		try{ return services.contains(normId(serviceId));}catch(NullPointerException x){}
		return false;
	}
	
	public static boolean cleanQueues(){
		try{
			Iterator<EmbeddedHandle> theHandles = handles.values().iterator();
			while(theHandles.hasNext()){
				theHandles.next().clearQueue();
			}
		}catch(Exception e){
			IndependantLog.error("Fail to clear up queues of handle", e);
			return false;
		}
		
		return true;
	}
}
