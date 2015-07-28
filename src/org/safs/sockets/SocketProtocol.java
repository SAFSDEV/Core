/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;


/**
 * This class implements a TCP protocol needed for Sockets control of remote processes 
 * through TCP String messages character encoded in UTF-8.  
 * <p>
 * The class implements both the "local" controller side and the "remote" ServerSocket 
 * side in order to keep the protocol implementation for both sides correct.
 * <p>
 * This class is not normally used directly, but thru subclasses of {@link AbstractProtocolRunner} 
 * which provides the necessary separate Threading required for the SocketProtocol.
 * <p>
 * Currently, this protocol expects remote TCP services to be running and accepting connections
 * on port {@link #DEFAULT_REMOTE_PORT} at the first, if port {@link #DEFAULT_REMOTE_PORT} is no available, 
 * remote TCP services will try to run on 2412, 2414 ... with augmentation of pace 2, the maximum possible
 * port is {@link #MAX_SERVER_PORT}.
 * By trying a broad range of ports, it can prevent conflicts with other system resources.  
 * 
 * Currently, this protocol lets local controller to contact and attempt a connection to 
 * those remote TCP services. As those TCP services can choose port dynamically, the local controller
 * doesn't know which remote port to use for the connection. It will try to connect to port {@link #DEFAULT_REMOTE_PORT},
 * if fail it will try to connect to port 2412, 2414 ... with augmentation of pace 2, the maximum possible
 * port is {@link #MAX_SERVER_PORT}.
 * <p>
 * 
 * If there is no need for the local controller's port to be specific--such as using port forwarding 
 * to local emulators appearing as remote machines--then the local controller port really could be 
 * any port at all. 
 * 
 * <p>
 * There is an initial handshake or verification that occurs between the local and remote SocketProtocol  
 * instances to confirm the device port owners are both SocketProtocol implementations.
 * @see AbstractProtocolRunner
 * 
 * (LeiWang)	SEP 29, 2012	Fix a connection problem for mobile-device connected by USB (portForwarding is used)
 */
public class SocketProtocol {

	public String TAG = getClass().getSimpleName();
	private static int protocol = 1; // TCP Protocol version
	
	public static final String DEFAULT_SERVER = "localhost";
	public static final int DEFAULT_REMOTE_PORT = 2410;
	public static final int DEFAULT_CONTROLLER_PORT = 2411;

	public static final int MAX_SERVER_PORT = 2500;
	public static final int NEXT_SERVER_PORT_PACE = 2;
	
	private boolean local_mode = true; // determines local or remote operating mode
	private boolean isRunning = false;
	
	/** "PROTOCOLVERSION"
	 * Prompt and partial response for initial handshake between the local protocol runner and a remote 
	 * protocol runner.  The local runner will send this prompt terminated with the EOM marker and 
	 * expects to receive a response in the format of "PROTOCOLVERSION=N"--an Integer representing the remote 
	 * runner's protocol version.  Currently, only "PROTOCOLVERSION=1" showing version 1 
	 * is supported. The response String is expected to be terminated with the EOM marker. */
	public static final String MSG_PROTOCOL_VERSION_QUERY = "PROTOCOLVERSION";
	
	public static final String ENV_KEY_REMOTE_PORT = "ENV_KEY_REMOTE_PORT";
	
	

	/** Indicates the Socket is being shutdown normally.
	 * @see #shutdownThread
	 * @see #shutdownCause */
	public static final int STATUS_SHUTDOWN_NORMAL = 0;

	/** Indicates the Socket is being shutdown abnormally no 
	 * remote client connection will be possible.  All attempts have been exhausted.
	 * @see #shutdownThread
	 * @see #shutdownCause */
	public static final int STATUS_SHUTDOWN_REMOTE_CLIENT = 1;
	
	/** Indicates the Socket is being shutdown abnormally because no 
	 * remote service connection will be possible.  All attempts have been exhausted.
	 * @see #shutdownThread
	 * @see #shutdownCause */
	public static final int STATUS_SHUTDOWN_REMOTE_SERVICE = 2;
	
	/** Indicates the Socket is being shutdown because from the controller side.
	 * @see #shutdownThread
	 * @see #shutdownCause */
	public static final int STATUS_SHUTDOWN_CONTROLLER = 3;
	
	/**
	 * String representations of the STATUS_SHUTDOWN causes.
	 */
	protected static final String[] STATUS_STRINGS = new String[]{"Normal Shutdown initiated", 
		                                                          "Remote Test initiating Shutdown",
																  "Remote Service initiating Shutdown",
																  "Controller initiating Shutdown"};

	/** "[!_!]"
	 * Case-Insensitive End-Of-Message marker for all character messages exchanged between local and remote runners.<br>
	 * Normally, this is never changed.  The local and remote runner implementations--coded 
	 * together (or this same class)--share in an implied contract of what this marker shall be 
	 * since both ends of the protocol must use it.*/
	protected String EOM = "[!_!]";		

	private boolean keepAlive = true;
	
	/** Hostname to contact for a remote client Runner. */
	protected String remoteHostname = DEFAULT_SERVER;
	
	/** port to contact for a remote client Runner. */
	protected int remotePort = DEFAULT_REMOTE_PORT; 
	
	/** port to use for a local controller Runner. */
	protected int controllerPort = DEFAULT_CONTROLLER_PORT;
	
	/** default 60 seconds timeout to complete a connection with a remote ServerSocket accepting 
	 * connections. */
	private int clientConnectTimeout = 60; //seconds 
	
	/** local controller Socket connection. */
	protected Socket controllerRunner = null;
	
	/** remote client ServerSocket connection. */
	private ServerSocket remoteRunner = null;

	/** inputstream from the other Runner.*/
	private InputStream inputstream = null;
	/** inputstreamreader from the other Runner.*/
    private InputStreamReader rawinputreader = null;
	/** bufferedreader from the other Runner.*/
    private BufferedReader bufferedreader = null;
    
	/** outputstream to the other Runner.*/
	private OutputStream outputstream = null;
	/** outputstreamwriter to the other Runner.*/
    private OutputStreamWriter rawoutputwriter = null;
	/** bufferedwriter to the other Runner.*/
    private BufferedWriter bufferedwriter = null;
    
	/** Named DebugListeners and ConnectionListeners registered with this instance. */
	private Vector combinedlisteners = new Vector();
	
	/**
	 * int value providing an indication of why the thread might have been 
	 * shutdown prematurely.
	 * @see #STATUS_SHUTDOWN_REMOTE_CLIENT
	 * @see #STATUS_SHUTDOWN_NORMAL */
	private int shutdownCause = STATUS_SHUTDOWN_NORMAL;
	
	/**
	 * For Local controller side, it is used to indicate whether it is connected to 
	 * Remote side (ServerSocket)<br>
	 * 
	 * For Remote side, it is used to indicate whether a controller runner has
	 * connected to it (ServerSocket).<br>
	 */
	private boolean connected = false;
	
	/**
	 * Default no-op constructor using all defaults.
	 * The user should change any desired remote settings and add 
	 * any DebugListeners and ConnectionListerners prior to starting the
	 * full use of the instance. 	
	 */
	public SocketProtocol(){;}

	/**
	 * Constructor using all defaults while registering a NamedListener.
	 * The user should change any desired remote hostname/port settings prior to starting 
	 * the full use of the instance.
	 * @param listener NamedListener (DebugListener, ConnectionListener, etc...) to register with 
	 * the new instance.  Ideally, the listener should implement both DebugListener and 
	 * ConnectionListener interfaces.
	 */
	public SocketProtocol(NamedListener listener){
		this();
		addListener(listener);
	}
	
	/**
	 * Constructor using all defaults while registering a NamedListener and 
	 * setting the local or remote mode of the instance.
	 * <p>  
	 * By default, the class is setup to run in local controller mode.  
	 * Users would normally only call this constructor to make local_mode = false and 
	 * run in remote client mode.
	 * <p>
	 * The user should change any desired remote hostname/port settings prior to starting 
	 * the full use of the instance.
	 * @param listener NamedListener to register with the new instance.  Ideally, the 
	 * listener should implement both DebugListener and ConnectionListener interfaces.
	 */
	public SocketProtocol(NamedListener listener, boolean local_mode){
		this(listener);
		setLocalMode(local_mode);
	}

	/**
	 * Set the mode of this instance.  True for local controller mode and false for 
	 * remote client mode.  By default, the class is setup to run in local controller mode.
	 * @param local_mode true to run in local protocol controller mode (ex: port 2411). false 
	 * to run in remote protocal client mode (ex: port 2410).
	 * @throws IllegalThreadStateException if a call attempts to set/change this while the 
	 * instance is already connected or running.
	 */
	public void setLocalMode(boolean local_mode)throws IllegalThreadStateException{
		if(isRunning){
			throw new IllegalThreadStateException("Cannot change local/remote mode while thread is running.");
		}else{this.local_mode = local_mode;}
		
	}
	
	/**
	 * @return true if the Runner is set for local controller mode.  
	 * false for remote client mode.
	 */
	public boolean isLocalMode(){
		return local_mode;
	}
	
	/**
	 * Register a DebugListener/ConnectionListener.  If no DebugListener is registered then our 
	 * debug output, if enabled, will go to System.out.println.
	 * @param listener to register to receive notifications.
	 * @return true if the listener was new and was successfully added
	 * @see #notifyConnection()
	 * @see #notifyLocalShutdown(int)
	 * @see #notifyRemoteShutdown(int)
	 */
	public boolean addListener(NamedListener listener){
		String debugmsg = TAG+".addListener(): ";
		boolean result = false;
		if (listener == null) {
			debug(debugmsg+"Invalid null NamedListener cannot be registered.");
			return false;
		}
		if(! combinedlisteners.contains(listener)) {
			result = combinedlisteners.add(listener);
			if(result) {
				debug(debugmsg+listener.getListenerName() +" was successfully registered.");
			}else{
				debug(debugmsg+listener.getListenerName() +" was NOT successfully registered.");
			}
			return result;
		}
		debug(debugmsg+"Runner is likely already registered with "+ listener.getListenerName());
		return false;
	}
	
	/**
	 * @param listener
	 * @return true if the listener was found and was successfully removed
	 */
	public boolean removeListener(NamedListener listener){
		String debugmsg = TAG+".removeListener(): ";
		boolean result = false;
		if (listener == null) {
			debug(debugmsg+"Invalid null NamedListener cannot be unregistered.");
			return false;
		}
		if(combinedlisteners.contains(listener)) {
			result = combinedlisteners.remove(listener);
			if(result) {
				debug(debugmsg+listener.getListenerName() +" was successfully unregistered.");
			}else{
				debug(debugmsg+listener.getListenerName() +" was NOT successfully unregistered.");
			}
			return result;
		}
		debug(debugmsg+"DebugListener is likely NOT registered and was not unregistered.");
		return false;
	}

	/** set to false to disable debug logging and improve performance. */
	public boolean _debugEnabled = true;
	
	/**
	 * Convenience routine for logging debug messages.  Simply forwards the call to 
	 * notifyDebug(String).
	 * @param text
	 * @see #notifyDebug(String)
	 */
	protected void debug(String text){
		notifyDebug(text);
	}
	
	/**
	 * Notify registered DebugListeners to log a debug message.
	 * Notification will be sent to all registered DebugListeners, or to System.out.println 
	 * if we have none or we did not successfully send to any listeners.
	 * This will only happen if debug logging is enabled--which it is by default.
	 * @param text
	 * @see DebugListener
	 * @see #_debugEnabled
	 */
	private void notifyDebug(String text){
		if(_debugEnabled){
			boolean success = false;
			if(combinedlisteners.size() > 0){
				for(int i=0;i<combinedlisteners.size();i++){
					try{
						((DebugListener)combinedlisteners.get(i)).onReceiveDebug(text);
						success = true;
					}
					catch(Exception x){}
				}
			}
			if(!success) System.out.println(text);
		}
	}
	
	/**
	 * @param cause -- int STATUS_SHUTDOWN_xxx cause to describe.
	 * @return A String description of the cause of the shutdown.  This is deduced from 
	 * the shutdownCause int being used as a String[] lookup in STATUS_STRINGS.
	 * @see #STATUS_STRINGS
	 */
	public static String getShutdownCauseDescription(int cause){
		String append = ".";
		try{ append = ": "+ STATUS_STRINGS[cause];}catch(Exception x){}
		return append;
	}
	
	/**
	 * Notify registered ConnectionListeners that a local to remote communication connection 
	 * has been established..
	 * Notification will be sent to all registered ConnectionListeners, or to System.out.println 
	 * if we have none or we did not successfully send to any listeners.
	 * @param text
	 * @see ConnectionListener#onReceiveConnection()
	 */
	private void notifyConnection(){
		boolean success = false;
		if(combinedlisteners.size() > 0){
			for(int i=0;i<combinedlisteners.size();i++){
				try{
					((ConnectionListener)combinedlisteners.get(i)).onReceiveConnection();
					success = true;
				}
				catch(Exception x){}
			}
		}
		if(!success) System.out.println("Protocol Runners Connected");		
	}
	
	/**
	 * Notify registered ConnectionListeners that a local shutdown has or should occur.
	 * Notification will be sent to all registered ConnectionListeners, or to System.out.println 
	 * if we have none or we did not successfully send to any listeners.
	 * @param int shutdownCause
	 * @see ConnectionListener#onReceiveLocalShutdown(int)
	 */
	private void notifyLocalShutdown(int shutdownCause){
		boolean success = false;
		if(combinedlisteners.size() > 0){
			for(int i=0;i<combinedlisteners.size();i++){
				try{
					((ConnectionListener)combinedlisteners.get(i)).onReceiveLocalShutdown(shutdownCause);
					success = true;
				}
				catch(Exception x){}
			}
		}
		if(!success) System.out.println("Protocol Runner performing local shutdown"+ getShutdownCauseDescription(shutdownCause));		
	}
	
	/**
	 * Notify registered ConnectionListeners that a remote shutdown has or should occur.
	 * Notification will be sent to all registered ConnectionListeners, or to System.out.println 
	 * if we have none or we did not successfully send to any listeners.
	 * @param int shutdownCause
	 * @see ConnectionListener#onReceiveRemoteShutdown(int)
	 */
	private void notifyRemoteShutdown(int shutdownCause){
		boolean success = false;
		if(combinedlisteners.size() > 0){
			for(int i=0;i<combinedlisteners.size();i++){
				try{
					((ConnectionListener)combinedlisteners.get(i)).onReceiveRemoteShutdown(shutdownCause);
					success = true;
				}
				catch(Exception x){}
			}
		}
		if(!success) System.out.println("Protocol Runner received remote shutdown"+ getShutdownCauseDescription(shutdownCause));		
	}
	
	/**
	 * @return the current Case-insensitive End-Of-Message marker String ending all character messages in the  
	 * protocol.<br> 
	 * Normally, this is never changed.  The local and remote runner implementations--coded 
	 * together (or this same class)--share in an implied contract of what this marker shall be 
	 * since both ends of the protocol must use it.*/
	public String getEOM() {
		return EOM;
	}

	/**
	 * @param Case-insensitive End-Of-Message marker String to use for terminating all String messages in the protocol.
	 * Normally, this is never changed.  The local and remote runner implementations--coded 
	 * together (or this same class)--share in an implied contract of what this marker shall be 
	 * since both ends of the protocol must use it.
	 * @throws IllegalArgumentException if the supplied marker argument is null, zero-length, or appears to be 
	 * case-sensitive when comparing calls for toUpperCase() and toLowerCase(). */
	public void setEOM(String marker) throws IllegalArgumentException{
		if(marker == null       || 
		   marker.length() == 0 || 
		   (! marker.toUpperCase().equals(marker.toLowerCase()))) 
			throw new IllegalArgumentException("TCP protocol EOM marker must be a valid case-insensitive String of 1 or more characters.");
		EOM = marker;
	}

	// store a runtime exception type so we register/log it only once.
	private String _acceptException = null;
	
	/**
	 * Used internally.
	 * This is for a remote client ServerSocket instance looking for connection requests from an 
	 * external local controller instance.  
	 * <p>
	 * If a connection request is received this routine will then attempt to validate 
	 * the external local controller via verifyController.  If the validation succeeds 
	 * this routine will notify registered listeners of the connection.
	 * <p>
	 * If the connection request is received but the validation fails this routine will 
	 * call notify registered listeners of a local shutdown and proceed to shutdown all 
	 * sockets and streams.
	 * @param msTimeout milliseconds to look for a connection request.
	 * @return true if we accepted and verified a controller connection
	 * @throws IllegalThreadStateException if this method is called from an instance operating 
	 * in local controller mode.
	 * @see #verifyControllerClient(int)
	 * @see #notifyConnection()
	 * @see #notifyLocalShutdown(int)
	 * @see #closeStreams()
	 */
	private boolean acceptControllerConnection(int msTimeout) throws IllegalThreadStateException{
		String debugmsg = TAG+".acceptControllerConnection(): ";
		if(isLocalMode()) throw new IllegalThreadStateException(
			"Cannot acceptControllerConnections when running in local controller mode."); 
		try{				
			if(controllerRunner == null && remoteRunner != null) { // same as tcp not connected
				remoteRunner.setSoTimeout(msTimeout);
				controllerRunner = remoteRunner.accept();
				controllerRunner.setKeepAlive(keepAlive);
				connectStreams(controllerRunner);
				
				debug(debugmsg+"Verifying controller Socket connection from: "+ 
					   controllerRunner.getInetAddress().getHostAddress()+
					   " port "+ controllerRunner.getLocalPort());
				
				if(verifyControllerClient(10)) {
					notifyConnection();
					setConnected(true);
					return true;
				}
				
				debug(debugmsg+"Remote client did NOT verify itself as a Protocol Runner running in local mode!");
				
				// remote Socket is not our protocol client -- cannot proceed
				shutdownCause = STATUS_SHUTDOWN_REMOTE_CLIENT;
				notifyLocalShutdown(STATUS_SHUTDOWN_REMOTE_CLIENT);
				setConnected(false);
			}
		}catch(Exception st){
			if(!(st instanceof SocketTimeoutException)){
				// only log this once per unique exception type
				if(! st.getClass().getSimpleName().equals(_acceptException)){
					_acceptException = st.getClass().getSimpleName();
					debug(debugmsg+"acceptServerConnection "+ 
						  _acceptException +", "+ st.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * Used Internally.
	 * captures input and output streams from the Socket and instantiates necessary 
	 * Buffered Readers and Writers with UTF-8 character encoding.
	 * @param socket valid connected Socket instance.
	 * @throws IOException if problems instantiating Readers and Writers.
	 */
	private void connectStreams(Socket socket)throws IOException{
		inputstream = socket.getInputStream();
		rawinputreader = new InputStreamReader(inputstream, "UTF-8");
		bufferedreader = new BufferedReader(rawinputreader);
		outputstream = socket.getOutputStream();
		rawoutputwriter = new OutputStreamWriter(outputstream, "UTF-8");
		bufferedwriter = new BufferedWriter(rawoutputwriter);		
	}

	/**
	 * Used Internally.
	 * Close all instantiated Streams, Readers, and Writers and null 
	 * all associated references.
	 */
	private void closeStreams(){
		try{ if(bufferedreader!=null) bufferedreader.close();}catch(Exception x){}
		try{ if(bufferedwriter!=null) bufferedwriter.close();}catch(Exception x){}
		try{ if(rawinputreader!=null) rawinputreader.close();}catch(Exception x){}
		try{ if(rawoutputwriter!=null) rawoutputwriter.close();}catch(Exception x){}
		inputstream = null;
		rawinputreader = null;
		bufferedreader = null;
		outputstream = null;
		rawoutputwriter = null;
		bufferedwriter = null;
	}

	/**
	 * Used Internally. Used by Local Controller side.
	 * Attempt to connect to a remote protocol client.
	 * This is only valid in local controller mode.  For remote client mode the equivalent 
	 * call would be {@link #acceptControllerConnection(int)}.
	 * <p>
	 * If a connection is accepted this routine will then attempt to validate 
	 * the remote client via verifyRemoteClient.  If the validation succeeds 
	 * this routine will notify registered listeners of the connection.
	 * <p>
	 * If the connection is accepted but the validation fails this routine will 
	 * call notify registered listeners of a local shutdown and proceed to shutdown all 
	 * sockets and streams.
	 * @param sTimeout in seconds to keep trying to make the connection
	 * @return boolean -- connection established.
	 * false means "not successfully connected".
	 * @throws IllegalThreadStateException if the Thread is running in remote client 
	 * mode instead of local controller mode.
	 * @see #notifyConnection()
	 * @see #notifyLocalShutdown(int)
	 * @see #closeStreams()
	 */
	private boolean createRemoteClientConnection(int sTimeout) throws IllegalThreadStateException{
		String debugmsg = TAG+".createRemoteClientConnection(): ";
		if(! isLocalMode()) throw new IllegalThreadStateException(
		"Cannot createRemoteClientConnections when running in remote client mode."); 
		boolean keepTrying = true;
		int ticks = 0;
		int msWait = 100;
		int maxticks = (sTimeout > 0) ? (sTimeout * (1000/msWait)) : 0;
		int verifyRemoteTimeoutSecond = 10;
		int verificationFail = 0;
		int verificationFailMaxTry = sTimeout<verifyRemoteTimeoutSecond ? 1:(sTimeout/verifyRemoteTimeoutSecond);
		debug(debugmsg+"Local Runner attempting to make remote Runner connection...");
		try{
			while(keepTrying){
				//Try to bind to the remote ServerSocket at the same remotePort
				bindToRemoteServer();
				
				if(controllerRunner==null){
					keepTrying = ticks++ < maxticks;
					try{ Thread.sleep(msWait);}catch(Exception x){}	
				}else{
					debug(debugmsg+"Remote Runner seems to be connected!");
					controllerRunner.setKeepAlive(keepAlive);
					connectStreams(controllerRunner);
					
					if(verifyRemoteClient(verifyRemoteTimeoutSecond)) {
						debug(debugmsg+"Remote Runner has been connected!");
						notifyConnection();
						setConnected(true);
						return true;
					}else{
						//remote Socket is not SAFS client -- cannot proceed
						debug(debugmsg+"Remote client did NOT verify itself as a remote SocketProtocolRunner!");
						//Do we need continue to connect?
						//Yes. If verification fail due to IOException, read input timeout, remote not ready etc.
						//No. If verification fail due to failure of handshake: "wrong remote", how to detect?
						verificationFail +=1;
						keepTrying = (verificationFail<verificationFailMaxTry);							
						//Close streams and set controllerRunner to null if continue to reconnect
						if(keepTrying) setConnected(false);
					}
				}
			}
		}catch(IOException io){
			debug(debugmsg+"createRemoteClientConnection failure: "+ io.getClass().getSimpleName()+", "+ io.getMessage());
		}
		
		//If we come here, which means that we didn't connect to Remote Client (ServerSocket)
		//at the default remotePort within the timeout.
		//We should set the connected to false so that the runner will continue try to connect
		//to the Remote side at the next possible TCP port remotePort.
		setConnected(false);
		remotePort = getNextPort(remotePort);
		debug(debugmsg+"The remotePort is set to next possible port number '"+remotePort+"'");

		//if the port number is exhausted, send a shutdown notification to runner so that runner will stop.
		if(remotePort>MAX_SERVER_PORT){							
			shutdownCause = STATUS_SHUTDOWN_REMOTE_CLIENT;
			notifyLocalShutdown(STATUS_SHUTDOWN_REMOTE_CLIENT);
		}
		
		return false;
	}

	/**
	 * Create the Socket object according to server name and port.
	 */
	protected void bindToRemoteServer() throws IOException{
		controllerRunner = new Socket(remoteHostname, remotePort);
	}
	
	/**
	 * Used Internally. Used by Remote Client side.
	 * Attempt to createRemoteServerSocket.
	 * This is only valid in remote client mode.  For local controller mode the equivalent 
	 * call would be {@link #createRemoteClientConnection(int)}.
	 * @return boolean -- Remote ServerSocket successfully connected and ready to accept connections.
	 * @throws IllegalThreadStateException if the Thread is running in local controller 
	 * mode instead of remote client mode.
	 */
	private boolean createRemoteServerSocket() throws IllegalThreadStateException{
		String debugmsg = TAG+".createRemoteServerSocket(): ";
		if(isLocalMode()) throw new IllegalThreadStateException(
		"Cannot createRemoteServerSocket when running in local controller mode."); 
		try{
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			NetworkInterface net;
			String adds;
			while(e.hasMoreElements()){
				net = (NetworkInterface)e.nextElement();
				adds = "NetworkInterface: " + net.getDisplayName();
				Enumeration a = net.getInetAddresses();
				while(a.hasMoreElements()){
					adds+= ", "+ ((InetAddress)a.nextElement()).getHostAddress();
				}
				debug(debugmsg+adds);
			}
			
			boolean keeptrying = true;
			while(keeptrying){
				try {
					debug(debugmsg+"Try to create socket server at port '"+remotePort+"'.");
					remoteRunner = new ServerSocket(remotePort);
					keeptrying = false;
				} catch (IOException e1) {
					debug(debugmsg+"Fail to create socket server at port '"+remotePort+"': Exception "+ e1.getMessage());
					remotePort = getNextPort(remotePort);
				}
			}
			
			System.setProperty(ENV_KEY_REMOTE_PORT, String.valueOf(remotePort));
			debug(debugmsg+"Remote Runner available on port: "+ remoteRunner.getLocalPort());
			return true;
		}
		catch(Exception x){
			x.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Try to augment the port number by {@value #NEXT_SERVER_PORT_PACE}<br>
	 * 
	 * @param prevPort		int, the previous port number
	 * @return				int, the next port number
	 * @throws				IllegalStateException, if the next port number exceeds the max port number.
	 */
	public int getNextPort(int prevPort){
		if(prevPort+NEXT_SERVER_PORT_PACE > MAX_SERVER_PORT){
			throw new IllegalStateException("No more port to use. Modify code to augment the constant MAX_SERVER_PORT.");
		}
		return prevPort+NEXT_SERVER_PORT_PACE;
	}
	
	/**
	 * For local controller:<br>
	 * Set the value of field {@link #connected} to indicate if the 'local controller'<br>
	 * is connected to 'remote client'<br>
	 * 
	 * For remote client:<br>
	 * Set the value of field {@link #connected} to indicate if the 'remote client'<br>
	 * accepts a connection from 'local controller'<br>
	 * 
	 * If the connected is false, the opened streams will be closed and the Socket<br>
	 * connection {@link #controllerRunner} will be set to null.<br>
	 * 
	 * @param connected	boolean
	 * @see #createRemoteClientConnection(int)
	 */
	private void setConnected(boolean connected){
		this.connected = connected;
		if(!connected){
			//Close all opened streams
			closeStreams();
			//Set the controllerRunner to null
			try{controllerRunner.close();}catch(Exception x){}
			controllerRunner = null;
		}
	}
	
	/**
	 * @return true if we have a validated 2-way connection.
	 */
	public boolean isConnected(){
		return connected;
	}
	
	/**
	 * Used Internally.
	 * verify the connected remote socket is controlled by a SocketProtocol running in 
	 * local controller mode.
	 * @param sTimeout in seconds to wait for proper client response.
	 * @return true only if this full handshake has completed
	 * @throws IllegalThreadStateException if this is called from an instance running in  local 
	 * controller mode.
	 * @throws InvalidObjectException if we don't have valid input/output streams.
	 * @throws IllegalThreadStateException if the Thread is running in local controller 
	 * mode instead of remote client mode.
	 */
	private boolean verifyControllerClient(int sTimeout) throws IllegalThreadStateException, InvalidObjectException{
		String debugmsg = TAG+".verifyControllerClient(): ";
		if(isLocalMode()) throw new IllegalThreadStateException(
		"Cannot verifyControllerClient when running in local controller mode."); 
		String result = waitForInput(sTimeout * 1000);
		if(result != null){
			if(result.startsWith(MSG_PROTOCOL_VERSION_QUERY)){
				if(sendResponse(MSG_PROTOCOL_VERSION_QUERY+"="+ protocol)){
					debug(debugmsg+"Controller verification has succeeded.");
					return true;
				}else{
					debug(debugmsg+"Failed to send response to the Controller verification prompt!");
				}						
			}else{
				debug(debugmsg+"Invalid verification prompt from the Controller: "+ result);
			}
		}else{
			debug(debugmsg+"Client did NOT receive verification prompt from Controller within timeout period.");
		}			
		return false;
	}
	
	/** By default, this class only accepts connections from instances using the same 
	 * protocol version.  It is possible future subclasses will want to override 
	 * this behavior to accept other--typically older known protocol versions. 
	 * @param protocol
	 * @return true if our supported protocol version matches the requested version.
	 */
	public boolean acceptProtocolVersion(int protocol){
		return this.protocol == protocol;
	}
	
	/**
	 * Used Internally.
	 * verify the connected remote socket is controlled by a client knowing the handshake.
	 * We send MSG_PROTOCOL_VERSION_QUERY, and the client should respond MSG_PROTOCOL_VERSION_QUERY=N.<br>
	 * Currently version N=1 is supported.
	 * @param sTimeout in seconds to wait for proper client response.
	 * @return true if the remote client has successfully validated.
	 * @see #MSG_PROTOCOL_VERSION_QUERY
	 * @throws InvalidObjectException if we don't have valid input/output streams.
	 * @throws IllegalThreadStateException if the Thread is running in remote client 
	 * mode instead of local controller mode.
	 */
	private boolean verifyRemoteClient(int sTimeout)throws IllegalThreadStateException, InvalidObjectException{
		String debugmsg = TAG+".verifyRemoteClient(): ";
		if(!isLocalMode()) throw new IllegalThreadStateException(
		"Cannot verifyRemoteClient when running in remote client mode."); 
		// exchange a handshake
		boolean result = sendResponse(MSG_PROTOCOL_VERSION_QUERY);
		if(result){
			String response = waitForInput(sTimeout * 1000);
			if(response == null) {
				debug(debugmsg+"Remote client did not verify in timeout period.");
				return false;
			}
			if (response.startsWith(MSG_PROTOCOL_VERSION_QUERY)){
				String[] split = response.split("=");
				try{
					int check = Integer.parseInt(split[1]);
					if(acceptProtocolVersion(check)){
						debug(debugmsg+" client protocol "+ protocol +" connected.");
						return true;
					}else{
						debug(debugmsg+"Remote client protocol "+ check +" is NOT supported.");							
					}						
				}catch(Exception x){
					debug(debugmsg+"Remote client invalid protocol response format: "+ response);
				}
			}else{
				debug(debugmsg+"Remote client invalid protocol response: "+ response);
			}
		}else{
			debug(debugmsg+"Local Protocol Runner did NOT successfully send prompt to Remote client for verification.");
		}			
		return false;
	}

	/**
	 * Simply callse closeStreams
	 * @see #closeStreams()
	 */
	public void closeProtocolRunners(){
		closeStreams();
	}
	
	protected final static String debugprefix = "debug:";
	/**
	 * Listen for UTF-8 encoded content from the connected instance and return it to the caller if 
	 * it is deemed valid.
	 * <p>
	 * String content is not considered valid unless/until the End-Of-Message marker is received. 
	 * Without receiving the EOM within the timeout period the routine will consider any received 
	 * content invalid and will subsequently return a null value upon timeout. 
	 * @param msTimeout timeout in milliseconds
	 * @return received input or null if no input stream available or no valid input 
	 * received in timeout period.  The EOM will have already been stripped from the message.
	 * @throws InvalidObjectException if we have no InputStream connected. 
	 * @see #EOM */
	public String waitForInput(long msTimeout)throws InvalidObjectException{
		String debugmsg = TAG+".waitForInput(): ";
		if(bufferedreader == null) throw new InvalidObjectException("No Remote Input Stream Connected.");
		String request = null;
		long maxTicks = System.currentTimeMillis();
		if(msTimeout > 0 ) maxTicks += msTimeout;
		boolean keepTrying = true;		
   		StringBuffer buffer = null;
		try{
			//wait for some bytes to show up
			while(keepTrying && !bufferedreader.ready()){ 
				keepTrying = maxTicks > System.currentTimeMillis();
				if(keepTrying) {
					try{ Thread.sleep(100);}catch(Exception x){}
				}else{
					return null;				    				
				}
			}    			
			//let the buffer start loading			
			buffer = new StringBuffer();
			int ichar;
			long activityTimeout = 30000; // milliseconds 
			long origMax = maxTicks;
			maxTicks = System.currentTimeMillis() + activityTimeout;
			while((request==null) && (System.currentTimeMillis() < maxTicks)){
				while(request == null && bufferedreader.ready()){
    			    ichar = bufferedreader.read();
    			    if(ichar != -1) buffer = buffer.append((char)ichar);
    			    // start the timer over on new bytes
    			    maxTicks = System.currentTimeMillis()+ activityTimeout;
    			    if(buffer.length() >= EOM.length()){
    			    	if(EOM.equalsIgnoreCase(buffer.substring(buffer.length()- EOM.length()))){
    			    		if(buffer.length() > EOM.length()){
    			    			request = buffer.substring(0, buffer.length()-EOM.length());
    			    		}else{
    			    			// we have received an EOM only!
    			    			// clear it and start over.
    			    			buffer = new StringBuffer();
    			    			maxTicks = origMax;
    			    		}
    			    	}
    			    }
				}
				if(request == null){ // reader was not ready
					try{ Thread.sleep(100);}catch(Exception x){}
				}
			}
			if(request == null){
    			debug(debugmsg+"waitForInput Timeout without End Of Message. Input: "+ buffer.toString());
    			return null;
			}
		}catch(IOException io){
			io.printStackTrace();
		}
		// avoid debug logging debug messages twice!
		if(!(request.indexOf(debugprefix)==0)) debug(debugmsg+"Received client input: "+ request);
		return request;
	}
	
	/**
	 * Send UTF-8 encoded message to the connected instance, if any.
	 * This routine automatically adds the End-Of-Message marker to the message before sending it.
	 * Thus, callers should not put any End-of_message marker on the message to be sent.
	 * @param message
	 * @return true if we sent the message to the connected socket stream without error.
	 * This should NOT be considered any kind of confirmation that the remote TCP client received the message.
	 * @throws InvalidObjectException if we have no OutputStream connected. 
	 * @see #EOM
	 */
	public boolean sendResponse(String message)throws InvalidObjectException{
		if(bufferedwriter == null) throw new InvalidObjectException("No remote OutputStream connected.");
		try{
			bufferedwriter.write(message + EOM);
			bufferedwriter.flush();
			return true;
		}catch(IOException io){
			io.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Attempt to create appropriate local or remote socket connections and attempt to
	 * connect with the other side.
	 * @return results of call to isConnected()
	 * @see #isConnected()
	 * @see #createRemoteServerSocket()
	 * @see #acceptControllerConnection(int)
	 * @see #createRemoteClientConnection(int)
	 */
	public boolean connectProtocolRunners(){		
		if(!isConnected()){
			if(isLocalMode()){
				// local controller mode
				createRemoteClientConnection(clientConnectTimeout);
			}
			else{
				// remote Runner mode
				if (remoteRunner == null) {
					createRemoteServerSocket();
				}
				if(remoteRunner != null){
					acceptControllerConnection(100); 
				}

			}
		}
		return isConnected();
	}
	
	/**
	 * By default keepAlive is TRUE unless changed.
	 * @return the keepAlive
	 */
	public boolean getKeepAlive() {
		return keepAlive;
	}

	/**
	 * By default keepAlive is TRUE unless changed.
	 * @param keepAlive the keepAlive to set
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * The default remote hostname is/was typically the "localhost".  This is because the initial use 
	 * and implementation for this protocol was for talking with simulators and emulators 
	 * acting as remote devices on the local machine.  Other uses may actually use a remote 
	 * hostname that is NOT on the local machine.
	 * @return the server hostname on which we expect remote clients to accept connections.
	 */
	public String getRemoteHostname() {
		return remoteHostname;
	}

	/**
	 * The default remote hostname is/was typically the "localhost".  This is because the initial use 
	 * and implementation for this protocol was for talking with simulators and emulators 
	 * acting as remote devices on the local machine.  Other uses may actually use a remote 
	 * hostname that is NOT on the local machine.
	 * @param the server hostname on which we expect remote clients to accept connections.
	 */
	public void setRemoteHostname(String hostname) {
		remoteHostname = hostname;
	}

	/**
	 * The default port currently used for remote client connections--typically port 2410. 
	 * @return the server port on which we expect remote clients to accept connections.
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/** Provide an alternate port on which the remote client is accepting connections.
	 * The default port currently used for remote client connections is 2410.  In the future 
	 * a broader set of predefined port numbers needs to be established to avoid resource 
	 * contention.
	 * @param remote server port on which the remote client is accepting connections. */
	public void setRemotePort(int port) {
		remotePort = port;
	}
	
	/**
	 * The default port currently used for outgoing controller Socket connections is 2411. 
	 * <p>
	 * Typically Sockets can normally communicate on "any available port".  However the initial use 
	 * and implementation for this protocol was for talking with simulators and emulators 
	 * acting as remote devices on the local machine.  Thus, it was necessary to be able to 
	 * do port forwarding to specifically forward a known local controller port to the known 
	 * remote client port.
	 * @return the port on which we expect local controller Socket to communicate.
	 */
	public int getControllerPort() {
		return controllerPort;
	}

	/** Provide an alternate port on which the local controller should open a Socket.
	 * The default port currently used for local controller communications is 2411.
	 * Typically Sockets can normally communicate on "any available port".  The initial use 
	 * and implementation for this protocol was for talking with simulators and emulators 
	 * acting as remote devices on the local machine.  Thus, it was necessary to be able to 
	 * do port forwarding to specifically forward a known local controller port to the known 
	 * remote client port.
	 * @param the port on which the local controller Socket will attempt to communicate. */
	public void setControllerPort(int port) {
		controllerPort = port;
	}
	
	/**
	 * @return the protocol version for the remote connection.  Currently, only version 1 is 
	 * known or supported.
	 * @throws InvalidObjectException if we have not successfully connected to a remote client. */
	public int getConnectedProtocol()throws InvalidObjectException{
		if(!isConnected()) throw new InvalidObjectException("No Remote Client Connection.");
		return protocol;
	}

	/**
	 * Default is set at 60 seconds.  This value is typically used internally for the call to 
	 * {@link #createRemoteClientConnection(int)}
	 * @return the current clientConnectTimeout setting. */
	public int getClientConnectTimeout() {
		return clientConnectTimeout;
	}

	/**
	 * Default is set at 60 seconds.  This value is typically used internally for the call to 
	 * {@link #createRemoteClientConnection(int)}
	 * @param clientConnectTimeout in seconds */
	public void setClientConnectTimeout(int clientConnectTimeout) {
		this.clientConnectTimeout = clientConnectTimeout;
	}

}
