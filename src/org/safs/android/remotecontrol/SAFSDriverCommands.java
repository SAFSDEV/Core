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

import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.android.auto.lib.DUtilities;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.android.ddmlib.IDevice;
import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;
import com.jayway.android.robotium.remotecontrol.solo.Solo;

/**
 * Provide an Android RemoteControl API interface to SAFS Driver Commands for non-SAFS users using a SAFSTestRunner.
 * <p>
 * Default usage:
 * <p><pre>
 * Instantiate *and* initialize Solo API object according to the Solo class, then:
 *
 * SAFSDriverCommands driver = new SAFSDriverCommands(solo);
 * (use the API)
 * </pre>
 * Note: SAFSDriverCommands can use component UID references that have been acquired from either
 * the remote control Solo class APIs or the SAFSEngineCommands APIs.
 * <p>
 * @author Carl Nagle, SAS Institute, Inc.
 * @see org.safs.android.remotecontrol.SoloTest
 */
public class SAFSDriverCommands extends SAFSWorker {

	private Solo solo = null;
	private static final String TAG = "SAFSDriverCommands.";

	/**
	 * Default (required) public constructor.
	 * @param solo Solo instance already created and initialized.
	 */
	public SAFSDriverCommands(Solo solo){
		super();
		setRemoteControl(solo.getRemoteControl(), true);
	}

	/* hidden for non-use */
	private SAFSDriverCommands() {
		super();
	}

	/**
	 * Used internally.
	 * Prepare a dispatchProps object targeting a remote "safs_driver" command instead of a remote "instrument"
	 * or "solo" command.
	 * @param command
	 * @return Properties ready to be populated with command-specific parameters.
	 */
	private Properties prepDriverDispatch(String command){
		_props = super.prepSoloDispatch(command);
		_props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);
		return _props;
	}


	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsClearClipboard.html">ClearClipboard</a>
	 * Clear the device clipboard.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void clearClipboard()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_clearclipboard);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!=SAFSMessage.STATUS_REMOTERESULT_OK) throw new RemoteException("clearClipboard did not execute properly on remote device.");
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsSetClipboard.html">SetClipboard</a>
	 * @param text -- to put on the device clipboard.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void setClipboard(String text)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_setclipboard);
		if(text != null) props.setProperty(SAFSMessage.PARAM_1, text);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!=SAFSMessage.STATUS_REMOTERESULT_OK) throw new RemoteException("setClipboard did not execute properly on remote device.");
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsAssignClipboardVariable.html">AssignClipboardVariable</a>
	 * Retrieve text contents of the device clipboard.
	 * @return String of clipboard contents or null if no contents or not String contents.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getClipboard()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_assignclipboardvariable);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("getClipboard did not execute properly on remote device.");
		if(results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK) return results.getStatusInfo();
		return null;
	}

	/** Same as getClipboard.
	 * @see #getClipboard() */
	public String assignClipboardVariable()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return getClipboard();
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsHideSoftKeyboard.html">HideSoftKeyboard</a>
	 * @return true if successful.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	@Override
	public boolean hideSoftKeyboard()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_hidesoftkeyboard);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("hidesoftkeyboard did not execute properly on remote device.");
		return results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK;
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsShowSoftKeyboard.html">ShowSoftKeyboard</a>
	 * @return true if successful.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public boolean showSoftKeyboard()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_showsoftkeyboard);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("showsoftkeyboard did not execute properly on remote device.");
		return results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK;
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsWaitForGUI.html">WaitForGUI</a>
	 * @param winrec -- SAFS window recognition string, or UID reference from Solo or SAFSEngineCommands
	 * @param comprec -- SAFS component recognition string, or UID reference from Solo or SAFSEngineCommands
	 * @param stimeout -- wait timeout in seconds. If less than 0 then uses default 15 second wait timeout.
	 * @return true if the GUI object was found in the timeout period.  false if it was not.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 * @see com.jayway.android.robotium.remotecontrol.solo.Solo
	 * @see SAFSEngineCommands
	 */
	public boolean waitForGUI(String winrec, String comprec, int stimeout)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_waitforgui);
		props.setProperty(SAFSMessage.KEY_WINNAME, "Activity");
		props.setProperty(SAFSMessage.KEY_COMPNAME, "Child");
		props.setProperty(SAFSMessage.KEY_WINREC, winrec);
		props.setProperty(SAFSMessage.KEY_COMPREC, comprec);
		if (stimeout < 0) stimeout = 15;
		props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(stimeout));
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("waitForGUI did not execute properly on device.");
		return results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK;
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsWaitForGUIGone.html">WaitForGUIGone</a>
	 * @param winrec -- SAFS window recognition string, or UID reference from Solo or SAFSEngineCommands
	 * @param comprec -- SAFS component recognition string, or UID reference from Solo or SAFSEngineCommands
	 * @param stimeout -- wait timeout in seconds. If less than 0 then uses default 15 second wait timeout.
	 * @return true if the GUI object was gone in the timeout period.  false if it was still found.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 * @see com.jayway.android.robotium.remotecontrol.solo.Solo
	 * @see SAFSEngineCommands
	 */
	public boolean waitForGUIGone(String winrec, String comprec, int stimeout)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_waitforguigone);
		props.setProperty(SAFSMessage.KEY_WINNAME, "Activity");
		props.setProperty(SAFSMessage.KEY_COMPNAME, "Child");
		props.setProperty(SAFSMessage.KEY_WINREC, winrec);
		props.setProperty(SAFSMessage.KEY_COMPREC, comprec);
		if (stimeout < 0) stimeout = 15;
		props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(stimeout));
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("waitForGUIGone did not execute properly on device.");
		return results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK;
	}

	/**
	 * <a href="/sqabasic2000/AndroidDDDriverCommandsTakeScreenShot.html">TakeScreenShot</a>
	 * @param rotatable -- true if the app/screen might be rotated and the image should be flipped appropriately.
	 * @return BufferedImage of screenshot, or null if not received.
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public BufferedImage getScreenShot(boolean rotatable) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepDriverDispatch(SAFSMessage.driver_takescreenshot);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("getScreenShot did not execute properly on device.");
		if(results.getStatusCode() == SAFSMessage.STATUS_REMOTERESULT_OK){
	    	int rotation = results.getInt(SAFSMessage.KEY_REMOTERESULTINFO);
			IDevice device = DUtilities.getIDevice();
			if(device!=null){
				return DUtilities.getDeviceScreenImage(device, rotation, rotatable);
			}
			throw new RemoteException("getScreenShot DUtilities was not able to get a Device to screenshot.");
		}
		throw new RemoteException("getScreenShot did not return with success.");
	}

	/** Same as getScreenShot.
	 * @see #getScreenShot(boolean) */
	public BufferedImage takeScreenShot(boolean rotatable) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return getScreenShot(rotatable);
	}
}
