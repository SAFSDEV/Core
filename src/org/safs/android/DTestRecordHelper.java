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
package org.safs.android;

import org.safs.SocketTestRecordHelper;
import org.safs.TestRecordHelper;
import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.android.remotecontrol.SAFSWorker;
import org.safs.text.ResourceMessageInfo;

/**
 * Extends TestRecordHelper, which holds key data used by the SAFS/Droid Engine.<br>
 * Based on the SAFS Test Record Data doc.
 *
 * @author  Carl Nagle
 * @since   DEC 15, 2011
 * @author Carl Nagle MAR 28, 2012 Added storage for our SAFSWorker instance.
 * 		   Lei Wang SEP 10, 2012 Modify methods setMessage() and setDetailMessage(): clone the ResourceMessageInfo
 * 							   and set it to message/detailMessage.
 **/
public class DTestRecordHelper extends SocketTestRecordHelper {

	protected SAFSRemoteControl controller = null;

	/* should these be reset during each reinit() ? */
	protected ResourceMessageInfo message = null;
	
	/* should these be reset during each reinit() ? */
	protected ResourceMessageInfo detailMessage = null;
	
	protected SAFSWorker safsworker = null;
	
	/** Retrieve a valid ResourceMessageInfo or null if not set. */
    public ResourceMessageInfo getMessage() {
		return message;
	}

	/** Set to a valid message or null. */
	public void setMessage(ResourceMessageInfo message) {
		if(message==null){
			this.message = null;
		}else{
			this.message = message.clone();
		}
	}

	/** Retrieve a valid ResourceMessageInfo or null if not set. */
	public ResourceMessageInfo getDetailMessage() {
		return detailMessage;
	}

	/** Set to a valid message or null. */
	public void setDetailMessage(ResourceMessageInfo detailMessage) {
		if(detailMessage==null){
			this.detailMessage = null;
		}else{
			this.detailMessage = detailMessage.clone();
		}
	}

	/** 
     * No-arg constructor to make this fully qualified javabean
     **/
    public DTestRecordHelper() {
        super();
    }
    
    /**
	 * @return the controller in use, or null if not set.
	 */
	public SAFSRemoteControl getRemoteController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setRemoteController(SAFSRemoteControl controller) {
		this.controller = controller;
	}

    /**
	 * @return SAFSWorker in use, or null if not set.
	 */
	public SAFSWorker getSAFSWorker() {
		return safsworker;
	}

	/**
	 * @param worker SAFSWorker to store
	 */
	public void setSAFSWorker(SAFSWorker worker) {
		this.safsworker = worker;
	}

	/**
     * "org.safs.android." per the needs of the abstract interface.
     * @see TestRecordHelper#getCompInstancePath()
     */
    public String getCompInstancePath(){ return "org.safs.android."; }

}

