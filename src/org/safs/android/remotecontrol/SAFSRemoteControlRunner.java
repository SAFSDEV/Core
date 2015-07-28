/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.remotecontrol;

import org.safs.sockets.SocketProtocolListener;

import com.jayway.android.robotium.remotecontrol.solo.SoloRemoteControlRunner;

/** 
 * 
 * @author Carl Nagle, SAS Institute, Inc
 */
public class SAFSRemoteControlRunner extends SoloRemoteControlRunner {
	
	public SAFSRemoteControlRunner(SocketProtocolListener listener) {
		super(listener);
	}
}
