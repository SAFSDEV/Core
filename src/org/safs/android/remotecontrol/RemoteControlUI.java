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
package org.safs.android.remotecontrol;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InvalidObjectException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;
import org.safs.sockets.SocketProtocolListener;


/**
 * Crude Test app for initial manual testing of SAFS Robotium Remote Control extension.
 * This app provides a minimalist crude UI for manually entering and sending messages using SAFSRemoteControl 
 * implementing a SocketProtocol to communicate and control Android automation on a remote device or emulator.
 * <p>
 * Note this has been tested on both a real device and the Emulator.  To use the Emulator you 
 * must have a working Emulator and AVD.  
 * <p>
 * SampleCommand used to launch the emulator with an appropriate AVD:
 * <p>
 * emulator -no-snapstorage -avd SprintEvo
 * <p>
 * Steps to test the remote control concept:
 * <ol>
 * <p>
 * <li>Insure the Android SAFS TCP Messenger APK has been built and installed 
 * on the device or emulator:
 * <p>
 *    ant debug<br>
 *    adb install -r bin\SAFSTCPMessenger-debug.apk
 * <p>
 * <li>Insure the SAFS Test Runner  AndroidManifest.xml has the correct android:targetPackage setting 
 * for the application to be tested--whatever that is:<br><pre>
 *    &lt;instrumentation android:name="org.safs.android.engine.DSAFSTestRunner"
 *                  android:targetPackage="com.android.example.spinner"
 *                  android:label="General-Purpose SAFS Test Runner"/>
 * </pre>
 * <p>
 * <li>Insure the SAFSTestRunner APK has been built and installed on the device or emulator:
 * <p>
 *    ant debug<br>
 *    adb install -r bin\SAFSTestRunner-debug.apk
 * <p>
 * <li>Insure the application to be tested has been installed on the device or emulator.
 * <p>
 * <li>For the emulator, insure port forwarding has been activated:<br>
 * <p>adb forward tcp:2411 tcp:2410<br>
 * <p>
 * <li>In a separate CMD window, monitor "adb logcat"
 * <p>
 * <li>In a separate CMD window, launch the SAFS Test Runner:
 * <p>
 * adb shell am instrument -w org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
 * <p>You should see a SAFS TCP Messenger notification appear in the notification area on the device\emulator and 
 * there should be logcat messages showing the status of the SAFS Test Runner registering with the SAFS 
 * TCP Messenger.
 * <p>
 * <li>Launch the RemoteControlUI:
 * <p>java org.safs.android.remotecontrol.RemoteControlUI
 * <p>
 * <li>Press the "Connect" button to attempt to establish remote control connection with the SAFS 
 * TCP Messenger on the device/emulator. Monitor the logcat to see the status of this.  If everything worked the RemoteControlUI 
 * status line should read "REMOTE READY".
 * <p>
 * <li>Type "launch" and press "Send".  The target app launcher Activity should get launched.
 * <p>
 * <li>Type "close" and press "Send". The target app should shutdown.
 * <p>
 * <li>Press the "Dispatch" button to perform a roundtrip dispatch of a Properties object.<br>
 * The adb logcat window should show the properties received remotely on the Test Runner and the 
 * RemoteControlUI should then show a Dialog of the same properties PLUS statuscode and statusinfo properties 
 * returned back from the Test Runner.
 * <p><li>Press the "Shutdown" button and the remote SAFS Test Runner and SAFS TCP Messenger 
 * should both shutdown and the Notification icon on the device/emulator should disappear.
 * <p><li>RemoteControlUI will not be able to connect again until you relaunch the Instrumentation test again and 
 * the appropriate Notification is present on the emulator or device.
 * </ol>
 * @author Carl Nagle, SAS Institute, Inc.
 * @see SAFSRemoteControl
 */
public class RemoteControlUI extends JFrame implements SocketProtocolListener, ActionListener {
	
	SAFSRemoteControl server = null;
	
    JPanel controls = null;
    JLabel cmdLabel = null;
    JTextField cmdField = null;
    String sendAction = "send";
    String connectAction = "connect";
    String shutdownAction = "shutdown_hook";
    String dispatchPropsAction = "dispatchprops";
    JButton sendButton = null;
    JButton connectButton = null;
    JButton shutdownButton = null;
    JButton dispatchPropsButton = null;
    JTextField status = null;
    Properties props = new Properties();
    
	public RemoteControlUI() throws HeadlessException {
		super();
		createUI();
	}

	public RemoteControlUI(String title) throws HeadlessException {
		super(title);
		createUI();
	}

	Properties fillProperties(){
		props.clear();
		props.setProperty("action", "launchapplication");
		props.setProperty("windowid", "windowObject");
		props.setProperty("componentid", "childObject");
		props.setProperty("param1", "any parameter");
		props.setProperty("recordtype", "t");
		return props;
	}
	
	void createUI(){		
		setTitle(getListenerName());
		setName(getListenerName());
		getAccessibleContext().setAccessibleName(getListenerName());
		
		controls = new JPanel();
		
		cmdLabel = new JLabel("Command:");
		cmdField = new JTextField();
		cmdField.setEditable(false);
		cmdField.setEnabled(false);
		cmdField.setMinimumSize(new Dimension(350,18));
		cmdField.setPreferredSize(cmdField.getMinimumSize());
		sendButton = new JButton("Send");
		sendButton.setActionCommand(sendAction);
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
		connectButton = new JButton("Connect");
		connectButton.setActionCommand(connectAction);
		connectButton.addActionListener(this);
		connectButton.setEnabled(true);
		shutdownButton = new JButton("Shutdown");
		shutdownButton.setActionCommand(shutdownAction);
		shutdownButton.addActionListener(this);
		shutdownButton.setEnabled(false);
		dispatchPropsButton = new JButton("Dispatch");
		dispatchPropsButton.setActionCommand(dispatchPropsAction);
		dispatchPropsButton.addActionListener(this);
		dispatchPropsButton.setEnabled(false);
		
		controls.add(cmdLabel);
		controls.add(cmdField);
		controls.add(sendButton);
		controls.add(dispatchPropsButton);
		controls.add(shutdownButton);
		controls.add(connectButton);
		getContentPane().add(controls, "North");
		
		status = new JTextField();
		status.setEditable(false);
		
		getContentPane().add(status, "South");
		pack();
		int y = getHeight();
		//setSize(320 + 250,y);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		setFocusableWindowState(true);
	}
	
	void onConnectAction(){
		disableInteraction();
		status.setText("Attempting Remote Server Connection...");
		server = new SAFSRemoteControl();
		server.addListener(this);
		server.start();
	}
	
	void onSendAction(){
		disableInteraction();
		status.setText("Attempting sendMessage: "+ cmdField.getText());
		try{
			server.performRemoteMessageCommand(cmdField.getText(), 3, 3);
		}
		catch(IllegalThreadStateException x){ 
			
		}
		catch(RemoteException x){ 
			
		}
		catch(TimeoutException x){ 
			
		}
		catch(ShutdownInvocationException x){
			
		}
	}
	
	void onDispatchPropsAction(){
		try{ 
			server.performRemotePropsCommand(fillProperties(), 3, 3, 5); 
		}
		catch(IllegalThreadStateException x){ 
			
		}
		catch(RemoteException x){ 
			// TODO:
		}
		catch(TimeoutException x){ 
			// TODO:
		}
		catch(ShutdownInvocationException x){ 
			// TODO:
		}
	}
	
	void onShutdownAction() {
		disableInteraction();
		status.setText("Attempting Remote Client Shutdown...");
		try{
			server.performRemoteShutdown(3,3,5);
		}
		catch(IllegalThreadStateException x){ 
			
		}
		catch(RemoteException x){ 
			
		}
		catch(TimeoutException x){ 
			
		}
		catch(ShutdownInvocationException x){ 
			
		}		
		shutdownRemoteControl();
	}
	
	protected void disableInteraction(){
		cmdField.setEnabled(false);
		cmdField.setEditable(false);
		sendButton.setEnabled(false);
		shutdownButton.setEnabled(false);
		dispatchPropsButton.setEnabled(false);
		connectButton.setEnabled(false);
	}
	
	protected void enableInteraction(){
		cmdField.setEnabled(true);
		cmdField.setEditable(true);
		sendButton.setEnabled(true);
		shutdownButton.setEnabled(true);
		dispatchPropsButton.setEnabled(true);
	}
	
	public String getListenerName() {
		return getClass().getSimpleName();
	}

	public void onReceiveConnection() {
		status.setText("REMOTE CONNECTED");
	}

	public void onReceiveDebug(String message) {
		status.setText("REMOTE DEBUG: "+ message);
		System.out.println(message);
	}

	public void onReceiveReady() {
		status.setText("REMOTE READY");
		enableInteraction();
		connectButton.setEnabled(false);
	}

	public void onReceiveRunning() {
		status.setText("REMOTE RUNNING");
		connectButton.setEnabled(false);
		disableInteraction();
	}

	public void onReceiveResult(int rc, String info) {
		status.setText("REMOTE RESULT: "+ rc +" : "+ info);
	}

	public void onReceiveResultProperties(Properties result) {
		String alertmsg = "Received Properties:\n\n";
		Enumeration keys = result.keys();
		String key;
		while(keys.hasMoreElements()){
			key = (String)keys.nextElement();
			alertmsg += key +" = "+ result.getProperty(key)+"\n";
		}
		JOptionPane.showMessageDialog(this, alertmsg);
	}

	public void onReceiveMessage(String message) {
		status.setText("REMOTE MESSAGE: "+ message);
	}

	public void actionPerformed(ActionEvent e) {
		// there is only one action: send
		if(e.getActionCommand().equals(connectAction)) {
			onConnectAction();
		}else if(e.getActionCommand().equals(sendAction)) {
			onSendAction();
		}else if(e.getActionCommand().equals(shutdownAction)) {
			onShutdownAction();
		}else if(e.getActionCommand().equals(dispatchPropsAction)) {
			onDispatchPropsAction();
		}
	}
	
	/**
	 * java org.safs.android.RemoteControlUI
	 * 
	 * @param args -- none
	 */
	public static void main(String[] args) {
		new RemoteControlUI("Test SocketServer");
	}

	protected void shutdownRemoteControl(){
		disableInteraction();
		connectButton.setEnabled(true);
		try{server.shutdown();}catch(Exception x){}
		try{server.removeListener(this);}catch(Exception x){}
		server = null;
	}
	
	public void onReceiveLocalShutdown(int cause) {
		status.setText("LocalShutdown: "+ cause);
		shutdownRemoteControl();
	}

	public void onReceiveRemoteShutdown(int cause) {
		status.setText("RemoteShutdown: "+ cause);
		shutdownRemoteControl();
	}

	public void onReceiveException(String message) {
		status.setText("REMOTE EXCEPTION: "+ message);
	}

}
