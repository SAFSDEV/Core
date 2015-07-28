/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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

