/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

import java.io.CharArrayWriter;
import java.io.InvalidObjectException;
import java.util.Properties;
import java.util.Vector;


/**
 * This abstract class implements the necessary Threading of a {@link SocketProtocol}.  
 * The class provides the implementation needed by processes running on 
 * Windows, *nix, or Mac to control a remote client complying with this SocketProtocol.
 * <p>
 * Concrete subclasses must implement the abstract processProtocolMessage(String) in order 
 * to parse and process the messages received.
 * <p>
 * Currently, this server expects remote TCP services to be accepting connections on port 2410 
 * as required by the underlying SocketProtocol.
 * <p>
 * Currently, this server uses port 2411 to contact and attempt a connection to those remote 
 * TCP services as required by the underlying SocketProtocol.
 * <p>
 * Both sides eventually need to be able to use a broader range of ports to prevent conflicts with 
 * other system resources.
 * <p>
 * There is an initial handshake or verification that occurs between this remote controller server and the 
 * on-device Service to confirm the device port owners conform to the SocketProtocol.
 *
 * @see SocketProtocol
 * @see SocketProtocol#MSG_PROTOCOL_VERSION_QUERY
 */
public abstract class AbstractProtocolRunner implements Runnable, ConnectionListener, DebugListener{

	private int target_protocol = 1; // SAFS Protocol Runner version
	
	/** set to false to disable debug logging and improve performance. */
	public boolean _debugEnabled = true;
	
	/**
	 * Convenience routine to route internal debug messages to registered DebugListeners.
	 * @param text
	 */
	protected void debug(String text){
		onReceiveDebug(text);
	}
	
	/** All Listeners registered with this instance--whether they be simple NamedListeners, 
	 * DebugListeners, or ConnectionListeners. */
	protected Vector runnerlisteners = new Vector();
	
	/**
	 * Our running thread monitors this value to know whether or not it should continue 
	 * its looping execution or shut itself down. 
	 * @see #shutdownThread() */
	private boolean shutdownThread = false;
	
	/**
	 * The underlying SocketProtocol instance performing the actual TCP communication.
	 */
	public SocketProtocol protocolserver = null;
	
	/** "AbstractRunner" 
	 * The name of this NamedListener, */
	private String runnerName = "AbstractRunner";
	
	/**
	 * Default no-op constructor setting using all defaults.
	 * This creates a default SocketProtocol instance running in local controller mode with 
	 * this class added as a registered Debug and Connection Listener.
	 * <p>
	 * Subclass implementation should change any desired remote hostname/port settings and add 
	 * any other Listeners prior to starting the Runnable thread.
	 * @see SocketProtocol#SocketProtocol(NamedListener)
	 */
	public AbstractProtocolRunner(){
		protocolserver = new SocketProtocol(this);
	}

	/**
	 * Add any class or subclass of NamedListener to the registered Listeners list.
	 * This allows the chaining or pass-thru of notifications from the underlying SocketProtocol 
	 * object to the registered Listeners of this class.
	 * @param listener
	 * @return true if the Listener was successfully registered. false if the Listener was 
	 * already registered.
	 * @see NamedListener
	 * @see DebugListener
	 * @see ConnectionListener
	 */
	public boolean addListener(NamedListener listener){
		if(! runnerlisteners.contains(listener)){
			runnerlisteners.add(listener);
			return true;
		}
		return false;
	}
	
	/**
	 * Remove a Listener from the list of registered Listeners for this instance.
	 * @param listener
	 * @return true if the Listener was successfully removed. false if the Listener was 
	 * not previously registered.
	 * @see NamedListener
	 * @see DebugListener
	 * @see ConnectionListener
	 */
	public boolean removeListener(NamedListener listener){
		if(runnerlisteners.contains(listener)){
			runnerlisteners.remove(listener);
			return true;
		}
		return false;
	}
	
	/**
	 * Required Runnable interface for the Threaded execution of the underlying SocketProtocol.
	 * This thread implements the required sequence of events for both the local controller and 
	 * the remote client--both using a matching underlying SocketProtocol object--to connect through 
	 * TCP Sockets and continuously monitor and exchange UTF-8 messages until a request for 
	 * shutdown has been detected.
	 * <p>
	 * Subclasses of this abstract class would not normally change or override this method regardless 
	 * of being local controllers or remote clients.  This routine handles both through the SocketProtocol 
	 * object.
	 * <p>
	 * The thread constantly loops performing the following tasks:
	 * <p>
	 * <ol>
	 * <li>if not connected to a remote SocketProtocol, attempt to locate and connect to it.
	 * <li>check to see if the remote SocketProtocol has sent any valid content we need to dispatch.
	 * <li>if so, dispatch the content via {@link #processProtocolMessage(String)}
	 * <li>if not shutdown, loop again.
	 * </ol>
	 * <p>
	 * When exiting the loop due to a shutdown, all registered ConnectionListeners will be notified 
	 * via {@link ConnectionListener#onReceiveLocalShutdown(int)} that a "normal" shutdown has occurred.  
	 * The thread will then attempt to finish by closing the SocketProtocol communication channels.
	 * <p>
	 * Note that <i>sending</i> messages from the local SocketProtocol to the remote SocketProtocol is 
	 * NOT handled in this thread.  Once the remote connection is made, registered ConnectionListeners
	 * are notified via {@link ConnectionListener#onReceiveConnection()}.  Messages sent from the local 
	 * using class to the remote SocketProtocol are sent via {@link #sendProtocolMessage(String)} and 
	 * usually from a different Thread.    
	 * Consequently, the two-way communication should be considered asynchronous.  Though it is expected 
	 * the local and remote clients will attempt to maintain whatever "synchronous" communication is  
	 * appropriate for their shared protocol implementation. 
	 *
	 * @see #sendProtocolMessage(String)
	 * @see SocketProtocol#closeProtocolRunners()
	 * @see #shutdownThread() 
	 */
	public void run(){		
		String message = null;
		// loop
		while(!shutdownThread){
			// makeClientConnection			
			if(!protocolserver.isConnected()){
				protocolserver.connectProtocolRunners();
			}
			// listen for remote messages
			if(protocolserver.isConnected()){
				try{ message = protocolserver.waitForInput(25);}
				catch(Exception x){ message = null; }				
				// route message to appropriate listener callbacks
				if(message != null && message.length() > 0){
					processProtocolMessage(message);
				}
			}else{ // not yet connected
				try{Thread.sleep(100);}catch(Exception x){}
			}
		}//while
		for(int n = 0; n < runnerlisteners.size(); n++){
			try{((ConnectionListener)runnerlisteners.get(n)).onReceiveLocalShutdown(SocketProtocol.STATUS_SHUTDOWN_NORMAL);}
			catch(Exception x){}
		}
		protocolserver.closeProtocolRunners();
	}
	

	/**
	 * Command the ProtocolRunner thread to shutdown.  
	 * If the thread is running, this will ultimately null out all communication 
	 * streams and close the active socket connection (if any) to the remote client. 
	 * @see #run()*/
	public void shutdownThread(){
		shutdownThread = true;
	}

	/** {@link NamedListener#getListenerName()} */
	public String getListenerName() {
		return runnerName;
	}

	/** {@link NamedListener#setListenerName(String)} */
	public void setListenerName(String name) {
		runnerName = name;
	}

	/**
	 * Send all registered DebugListeners a Debug message received from a remote source.
	 * If no DebugListeners are registered, then the message may be routed to System.out.
	 * This will only happen if debug output is enabled--which it is by default. 
	 * @param message
	 * @see #_debugEnabled
	 * @see DebugListener#onReceiveDebug(String)
	 */
	public void onReceiveDebug(String message){
		if(_debugEnabled){
			boolean sent = false;
			for(int i = 0; i< runnerlisteners.size();i++){
				try{
					((DebugListener)runnerlisteners.get(i)).onReceiveDebug(message);
					sent = true;
				}
				catch(ClassCastException e){/* not all listeners are appropriate */ }
			}
			if(!sent) System.out.println(message);
		}
	}
	
	/**
	 * Notify all registered ConnectionListeners a 2-way SocketProtocol connection has been established.
	 * If no ConnectionListeners are registered, then the notification may be routed to System.out.
	 * @see ConnectionListener#onReceiveConnection()
	 */
	public void onReceiveConnection(){
		boolean sent = false;
		for(int i = 0; i< runnerlisteners.size();i++){
			try{
				((ConnectionListener)runnerlisteners.get(i)).onReceiveConnection();
				sent = true;
			}
			catch(ClassCastException e){/* not all listeners are appropriate */ }
		}
		if(!sent) System.out.println("Protocol Runners Connected");
	}
	
	/**
	 * Notify all registered ConnectionListeners the 2-way SocketProtocol connection is shutting down 
	 * on our local side.
	 * If no ConnectionListeners are registered, then the notification may be routed to System.out.
	 * @param caus -- whether "normal", or not.
	 * @see ConnectionListener#onReceiveLocalShutdown(int)
	 * @see SocketProtocol#STATUS_SHUTDOWN_NORMAL
	 * @see SocketProtocol#STATUS_SHUTDOWN_REMOTE_CLIENT
	 */
	public void onReceiveLocalShutdown(int cause){
		boolean sent = false;
		for(int i = 0; i< runnerlisteners.size();i++){
			try{
				((ConnectionListener)runnerlisteners.get(i)).onReceiveLocalShutdown(cause);
				sent = true;
			}
			catch(ClassCastException e){/* not all listeners are appropriate */ }
		}
		if(!sent) System.out.println("Processing a local shutdown notification: "+ SocketProtocol.getShutdownCauseDescription(cause));
	}
	
	/**
	 * Notify all registered ConnectionListeners the 2-way SocketProtocol connection is shutting down 
	 * from the remote side.
	 * If no ConnectionListeners are registered, then the notification may be routed to System.out.
	 * @param caus -- whether "normal", or not.
	 * @see ConnectionListener#onReceiveRemoteShutdown(int)
	 * @see SocketProtocol#STATUS_SHUTDOWN_NORMAL
	 * @see SocketProtocol#STATUS_SHUTDOWN_REMOTE_CLIENT
	 */
	public void onReceiveRemoteShutdown(int cause){
		boolean sent = false;
		for(int i = 0; i< runnerlisteners.size();i++){
			try{
				((ConnectionListener)runnerlisteners.get(i)).onReceiveRemoteShutdown(cause);
				sent = true;
			}
			catch(ClassCastException e){/* not all listeners are appropriate */ }
		}
		if(!sent) System.out.println("Processing a remote shutdown notification: "+ SocketProtocol.getShutdownCauseDescription(cause));
	}
	
	/**
	 * Send an arbitrary message through our SocketProtocol.
	 * Subclasses of AbstractProtocolRunner will usually define the message content and syntax 
	 * to be appropriate for whatever the application is trying to accomplish.  That is, different  
	 * applications will send and receive different messages.
	 * @param message
	 * @return true if successfully sent
	 * @throws InvalidObjectException from the underlying SocketProtocol if no connection has 
	 * yet been made.
	 * @see SocketProtocol#sendResponse(String)
	 */
	public boolean sendProtocolMessage(String message)throws InvalidObjectException{
		return protocolserver.sendResponse(message);
	}
	
	/**
	 * Concrete implementations must insert the parsing and processing of the messages 
	 * received from the underlying SocketProtocol.
	 * Subclasses of AbstractProtocolRunner will usually define the message content and syntax 
	 * to be appropriate for whatever the application is trying to accomplish.  That is, different  
	 * applications will send and receive different messages.
	 * @param message
	 */
	public abstract void processProtocolMessage(String message);
	
	/**
	 * Send a shutdown command to the remote client.  The format or syntax of this will be 
	 * implementation specific.
	 * <p>
	 * A remote client Protocol Runner would implement this as a do-nothing method.
	 * 
	 * @return true if the message was successfully sent.
	 */
	public abstract boolean sendShutdown();

	/**
	 * Send the remote client a dispatch Properties message containing a Serialized 
	 * Properties object containing all the data needed for 
	 * the execution of the Dispatch.  The format or syntax of this will be 
	 * implementation specific.
	 * <p>
	 * A remote client Protocol Runner would implement this as a do-nothing method.
	 *
	 * @param Properties trd to send
	 * @return true if the message was sent successfully.
	 */
	public abstract boolean sendDispatchProps(Properties trd);	
	
	/**
	 * Send the remote client a dispatch file message with the filepath to a file that 
	 * should be readable by the remote client.  The format or syntax of this will be 
	 * implementation specific.
	 * <p>
	 * A remote client Protocol Runner would implement this as a do-nothing method.
	 * 
	 * @param filepath path to a remote client readable file.
	 * @return true if the message was successfully sent.
	 */
	public abstract boolean sendDispatchFile(String filepath);
	
}
