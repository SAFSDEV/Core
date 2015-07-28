/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.remotecontrol;

import com.jayway.android.robotium.remotecontrol.solo.SoloRemoteControl;

/** 
 * 
 * @author Carl Nagle, SAS Institute, Inc
 */
public class SAFSRemoteControl extends SoloRemoteControl {

	public static String SAFS_LISTENER_NAME = "SAFS Remote Control";
	
	public SAFSRemoteControl() {
		super();
	}

	@Override
	public String getListenerName(){ return SAFS_LISTENER_NAME; } 

	@Override
	protected boolean createProtocolRunner(){
		runner = new SAFSRemoteControlRunner(this);
		return runner != null;
	}
}
