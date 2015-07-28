/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets.android;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.safs.android.auto.lib.AndroidTools;
import org.safs.android.auto.lib.DUtilities;
import org.safs.android.auto.lib.Process2;
import org.safs.sockets.AbstractProtocolRunner;
import org.safs.sockets.AvailablePortFinder;
import org.safs.sockets.NamedListener;
import org.safs.sockets.SocketProtocol;

/**
 * This class extends {@link SocketProtocol}, before connect to server<br>
 * it will try to forward the 'pc local port' to 'device server port' if<br>
 * the 'port forwarding' is enabled.<br>
 * 
 * @see SocketProtocol
 * @see AbstractProtocolRunner
 */
public class DroidSocketProtocol extends SocketProtocol{

	String[] tcpfowardParams = {"forward", "tcp:2411", "tcp:2410"};
	static AndroidTools tools = null;
	
	public DroidSocketProtocol(){}
	
	public DroidSocketProtocol(NamedListener listener){
		super(listener);
	}
	
	/**
	 * <pre>
	 * {@link #portForwarding} means if we want to forward local port {@link #controllerPort} to
	 * remote server port {@link #remotePort}
	 * If an emulator or a USB-connected-device is used to run android application, you should
	 * set {@link #portForwarding} to true;
	 * Otherwise, if a WIFI-connected-device is used to run android application, you should
	 * set {@link #portForwarding} to false;
	 * 
	 * If we have forwarded, the {@link #controllerPort} will be considered as a server port
	 * to be connected by client controller.
	 * 
	 * if true, controller Runner will use {@link #controllerPort} to create connection.
	 * if false, controller Runner will use {@link #remotePort} to create connection.
	 * </pre>
	 */
	private boolean portForwarding = true;
	
	/**
	 * Override the method in super class.<br>
	 * If we have forwarded the tcp port from {@link #controllerPort} to {@link #remotePort}.
	 * we will use the {@link #controllerPort} to create connection.<br>
	 * Otherwise, we just call {@link super#bindToRemoteServer()}, which use {@link #remotePort} to create connection.<br>
	 * 
	 * <p>
	 * Attention:<br>
	 * If port forwarding is true, adb will make a port forward from {@link #controllerPort} to {@link #remotePort}
	 * BUT, adb will NOT check if there is a ServerSocket running on the {@link #remotePort}
	 * After forwarding, the Socket {@link #controllerRunner} can always be created successfully.
	 * So this socket connection is not reliable!!! See {@link #main(String[])}.
	 * If verification fail, retry connection IS NEEDED.
	 * 
	 * @see #retryConnectionAfterVerificationFail()
	 */
	protected void bindToRemoteServer() throws IOException{
		if(portForwarding){
			if(forwardPort(controllerPort, remotePort)){
				//After forwarding port, should we create the Socket connection always with "localhost"? 
//				controllerRunner = new Socket("localhost", controllerPort);
				//If remoteHostname is assigned to an other value (not default "localhost"), this may fail.
				controllerRunner = new Socket(remoteHostname, controllerPort);
			}else{
				debug("Fail to forward from 'local:"+controllerPort+"' to 'device/emulator:"+remotePort+"'");
				//throw new IOException("Fail to forward from '"+controllerPort+"' to '"+remotePort+"'");
			}
		}else{
			super.bindToRemoteServer();
		}
	}
	
	/**
	 * set the value for field {@link #portForwarding}<br>
	 * If the 'port forwarding' is set to true, remember to call {@link #adjustControllerPort()}<br>
	 * to choose an available port for 'controller'.<br>
	 * 
	 * @param portForwarding
	 * @see #adjustControllerPort()
	 */
	public void setPortForwarding(boolean portForwarding){
		this.portForwarding = portForwarding;
	}
	/**
	 * get the value of {@link #portForwarding}
	 * @return	boolean, portForwarding
	 */
	public boolean getPortForwarding(){
		return portForwarding;
	}
	
	/**
	 * Use adb to forward local machine's port to a device/emulator's port
	 * 
	 * @param localPCPort		int,	port number of local machine
	 * @param remoteDevicePort	int,	port number of device/emulator
	 * @return
	 */
	public boolean forwardPort(int localPCPort, int remoteDevicePort){
		Process2 forwardProcess = null;
		
		try {
			debug("Forwarding port from 'local:"+localPCPort+"' to 'device/emulator:"+remoteDevicePort+"'");
			tcpfowardParams[1] = "tcp:"+localPCPort;
			tcpfowardParams[2] = "tcp:"+remoteDevicePort;
			if(tools==null) tools = DUtilities.getAndroidTools(null);
			forwardProcess = tools.adb(DUtilities.addDeviceSerialParam(tcpfowardParams)).forwardOutput().waitForSuccess();
			return true;
		} catch (InterruptedException e) {
			debug("Forwarding port error: "+e.getMessage());
		} catch (IOException e) {
			debug("Forwarding port error: "+e.getMessage());
		} finally{
			if(forwardProcess!=null) forwardProcess.destroy();
		}
		
		return false;
	}
	
	/**
	 * Choose an available port (not occupied by other ServerSocket or DatagramSocket) for controller port.<br>
	 * After calling this, you should NOT call {@link #setControllerPort(int)} unless you know you are setting<br>
	 * an available port for controller port.<br>
	 * 
	 * This method should be called after calling {@link #setPortForwarding(boolean)}<br>
	 * 
	 * @see #setControllerPort(int)
	 * @see #setPortForwarding(boolean)
	 */
	public void adjustControllerPort(){
		if(portForwarding){
			//verify controllerPort is available, if not, get the next available one.
			controllerPort = AvailablePortFinder.getNextAvailable(controllerPort);
			debug("Port forwarding will be from '"+controllerPort+"'");
			setControllerPort(controllerPort);
		}
	}
	
	/**
	 * This method will prove that we can create a connection SUCCESSFULLY with a 'serverPort' where 
	 * no SocketServer is running, the precondition is that we use adb to forward that 'serverPort' to
	 * a port on device/emulator (even there is NO SocketServer running on that port on device/emulator)
	 * 
	 * This prove that the connection is probably not a real one if we used 'adb port forwarding'.
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		ServerSocket server = null;
		try {
			int port = 2411;
			if(args.length==1){
				try{
					port = Integer.parseInt(args[0]);
				}catch(Exception e){}
			}
			server = new ServerSocket(port);
			System.out.println("ServerSocket is running on port "+server.getLocalPort());
			
			try{
				//The first connection can be established.
				Socket client = new Socket("localhost", port);
				System.out.println("socket connection with server on port "+client.getPort());
			}catch(Exception e){
				System.err.println("warning: "+e.getMessage());
			}
			
			//Create a server port(hope there is no Server running) to make a connection test 
			int serverPort = port+2400;
			try{
				//Normally, the following connection will fail except there is a Server running on that port
				Socket client1 = new Socket("localhost", serverPort);
				System.out.println("socket connection with server on port "+client1.getPort());
			}catch(IOException e){
				System.err.println("warning: "+e.getMessage()+": connection can't be established on port "+serverPort);
				try{
					//If comes here, we know there is no server running on 'serverPort'
					//But if we execute adb forward, the connection can be established on that 'serverPort'
					//even if we forward to a device port where no SocketServer is running.
					//This proves that the connection is not reliable.
					int whatEverPortOnMobileDevice = 2895;
					Runtime.getRuntime().exec("adb forward tcp:"+serverPort+" tcp:"+whatEverPortOnMobileDevice);
					Socket client2 = new Socket("localhost", serverPort);
					System.out.println("socket connection with server on port "+client2.getPort());
				}catch(IOException e1){
					System.err.println("warning: "+e1.getMessage());
				}
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(server!=null)
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
