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
