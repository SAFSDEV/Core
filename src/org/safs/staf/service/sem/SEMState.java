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
package org.safs.staf.service.sem;

import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.safs.staf.embedded.EmbeddedHandle;

import com.ibm.staf.STAFResult;

/**
 * @author Carl Nagle
 *
 */
public class SEMState {
	
	public static final int MIN_STATE       = 1;
	
	public static final int STATE_WAITING   = MIN_STATE;
	public static final int STATE_POSTED    = STATE_WAITING   +1;
	public static final int STATE_RESET     = STATE_POSTED    +1;
	public static final int STATE_REQUESTED = STATE_RESET     +1;
	public static final int STATE_RELEASED  = STATE_REQUESTED +1;

	public static final int MAX_STATE = STATE_RELEASED;
	
	public static final int STATE_INVALID = MAX_STATE +1;
	
	public static final String[] STATE_DESC = new String[]{
		"Waiters",
		"Posted",
		"Reset",
		"LOCKED",
		"Available",
		"INVALID"
	};
	
	private String itemname;
	private int itemstate;
	protected Semaphore sem;
	private Vector<String> waiters = new Vector();
	
	public String getItemName(){ return itemname;}
	
	/**
	 * @returns the id of the state being controlled/waited for.
	 */
	public int getItemState(){ return itemstate;}
	
	protected void setItemState(int state){
		itemstate = state;
	}

	
	public void addWaiterId(String waitername){
		if(!waiters.contains(waitername)) waiters.add(waitername);
	}
	public void removeWaiterId(String waitername){
		waiters.remove(waitername);
	}
	public Vector<String> getWaiterIds(){ return waiters;}
	
	/**
	 * Return a description of the current item state.
	 */
	public String getStateDesc() throws IllegalArgumentException{
		return STATE_DESC[itemstate];
	}

	/**
	 * Creates a default Semaphore controlled item.
	 * The default implementation allows any number of waiters to be registered and released  
	 * for the given item--typically an Event.
	 * @param itemname
	 * @param itemstate
	 * @throws IllegalArgumentException if itemname or itemstate are invalid.
	 */
	public SEMState(String itemname, int itemstate){
		if(itemname == null || itemname.length()==0) throw new IllegalArgumentException("SEMState.itemname invalid: '"+ itemname +"'");
		if(itemstate < MIN_STATE || itemstate > MAX_STATE) throw new IllegalArgumentException("SEMState.itemstate invalid: '"+ itemstate +"'");
		this.itemname = itemname;
		this.itemstate = itemstate;
		sem = new Semaphore(0,false);
	}
	
	/**
	 * @return true if we have waiters already waiting. false if there are no known waiters.
	 */
	public boolean hasWaiters(){
		synchronized(sem){
			return sem.hasQueuedThreads() || sem.getQueueLength()> 0;
		}
	}
	
	/**
	 * Release all waiters currently registered for the item.
	 */
	public void releaseWaiters(){
		synchronized(sem){
			int count = sem.getQueueLength();
			while(count > 0){
			    sem.release(count);
			    count = sem.getQueueLength();
			}
		}
	}
	
	/**
	 * Blocks the calling thread--which should be from an EmbeddedHandle--until the item is triggered with another thread's call to releaseWaiters.
	 * <p>
	 * Example call from an EmbeddedHandle:
	 * <p>
	 * <ul>event.addWaiter();</ul>
	 * <p>
	 * @return STAFResult.Ok when the item is available/posted/pulsed.
	 * @throws InterruptedException
	 */
	public STAFResult addWaiter() throws InterruptedException{
		sem.acquire();
		return new STAFResult(STAFResult.Ok);
	}	
	
	/**
	 * Blocks the calling thread--which should be from an EmbeddedHandle--
	 * until the item is triggered with another thread's call to releaseWaiters or the timeout has been reached.
	 * <p>
	 * Example call from an EmbeddedHandle:
	 * <p>
	 * <ul>STAFResult rc = event.addWaiter(100);</ul>
	 * <p>
	 * @return STAFResult.Ok, or STAFResult.Timeout
	 * @throws InterruptedException if the waiting thread gets interrupted.
	 */
	public STAFResult addWaiter(long msTimeout) throws InterruptedException{
		if (msTimeout < 1) return addWaiter();
		boolean success = sem.tryAcquire(msTimeout, TimeUnit.MILLISECONDS);
		return success ? new STAFResult(STAFResult.Ok):new STAFResult(STAFResult.Timeout, itemname);
	}
}
