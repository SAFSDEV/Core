/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Date;

import org.safs.Log;
import org.safs.tools.drivers.MouseHookObserver;

/** 
 * MouseCheckTimer
 * an Runnable for a separate thread to check mouse moving to imitate WM_MOUSEHOVER. When this thread is running, if mouse hovering over somewhere 
 * longer enough, its Observer, if has, will be notified to call onHandleMouseCheck(Point point) defined inMouseHookObserver. 
 * 
 * @see org.safs.tools.drivers.MouseHookObserver
 * @see org.safs.tools.drivers.STAFProcessContainer.HierarchyDlg
 * <p>
 * @author  Junwu Ma
 * @since   OCT 22, 2009
 */
public class MouseCheckTimer implements Runnable{
	final static int  millsecCycle = 500;
	final static int  millsecStay  = 1500;
	private Point lastPoint;
	private Date lastTime = new Date();
	private boolean isHovered = false;
	private boolean isRunning = false;
	
	private MouseHookObserver mouseCheckObserver = null;
	
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean status) {
		Log.info("MouseCheckTimer.setRunning is processing, current running status: " + String.valueOf(status));
		isRunning = status;
	}
	// register an Observer
	public void setObserver(MouseHookObserver anObserver){
		Log.info("MouseCheckTimer.setObserver is processing, setting an observer: " + anObserver.getClass().getName());
		mouseCheckObserver = anObserver; 
	}
	public void run(){
		while(true) {
			try {
				Thread.sleep(millsecCycle);
			}catch(Exception e){}
			
			if (!isRunning) continue;
			
			Point point = MouseInfo.getPointerInfo().getLocation();
			Point curPoint = point; 
			if ( !curPoint.equals(lastPoint) ) {
				lastTime = new Date();
				isHovered = false;
			} else {
				if (!isHovered) {
					Date nowTime = new Date();
					long costmillsec = nowTime.getTime() - lastTime.getTime();
					if (costmillsec >= millsecStay) {
						//lastTime = nowTime;
						isHovered = true;
						//to do
						if (mouseCheckObserver != null) {
							Log.info("MouseCheckTimer fires up onHandleMouseCheck at:" + curPoint.toString());
							mouseCheckObserver.onHandleMouseCheck(curPoint); // execute
						} 
					}
				}
			}
			lastPoint = curPoint;
		}
	}
}