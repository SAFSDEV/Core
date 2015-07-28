/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.remotecontrol;

import com.jayway.android.robotium.remotecontrol.solo.SoloRemoteControl;
import com.jayway.android.robotium.remotecontrol.solo.Solo;

/** 
 * 
 * @author Carl Nagle, SAS Institute, Inc
 */
public class SAFSWorker extends Solo{

	public SAFSWorker() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.jayway.android.robotium.remotecontrol.solo.SoloWorker#getListenerName()
	 */
	@Override
	public String getListenerName() {
		return super.getListenerName();
	}

	/* (non-Javadoc)
	 * @see com.jayway.android.robotium.remotecontrol.solo.SoloWorker#createRemoteControl()
	 */
	@Override
	protected SoloRemoteControl createRemoteControl() {
		return new SAFSRemoteControl();
	}

}
