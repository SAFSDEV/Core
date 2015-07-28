/**
 * 
 */
package org.safs.staf.service.sem;

import java.util.concurrent.Semaphore;

import org.safs.staf.embedded.EmbeddedHandle;

import com.ibm.staf.STAFResult;

/**
 * @author Carl Nagle
 *
 */
public class MutexState extends SEMState {
	
	private String holder;

	/**
	 * @param itemname
	 * @param itemstate
	 */
	public MutexState(String itemname, int itemstate) {
		super(itemname, itemstate);
	}

	/**
	 * Used internally.  Do not call directly.
	 * Use requestMutex and releaseMutex instead.<p>
	 * Release 1 (and only 1) waiter currently registered for the item.<p>
	 * Called by releaseMutex
	 * @see MutexState#releaseMutex()
	 */
	@Override
	public void releaseWaiters(){
		if(sem.hasQueuedThreads()) {
			sem.release();
		}
	}	
	
	/**
	 * @return true if the Mutex is currently not held by any owner.
	 */
	public boolean isAvailable(){
		return !(this.holder instanceof String);
	}
	
	/**
	 * Request exclusive ownership of the Mutex.<br>
	 * Calling thread will be blocked until access is granted.
	 * @param holder
	 * @return STAFResult.Ok, STAFResult.InvalidParam.
	 * @throws InterruptedException if the waiting thread is interrupted.
	 */
	public STAFResult requestMutex(String holder) throws InterruptedException{
		if(holder == null) return new STAFResult(STAFResult.InvalidParm, "MutexState.requestMutex(EmbeddedHandle) parameter cannot be null!");
		// only block if we already have an active owner
		if(this.holder instanceof String) addWaiter();
		// resumes here AFTER Mutex release
		setItemState(STATE_REQUESTED);
		this.holder = holder;
		return new STAFResult(STAFResult.Ok);
	}
	
	/**
	 * Release 1 (and only 1) Mutex waiter currently registered for the item.
	 * <p>
	 * Only the owner of the Mutex can release it.
	 * @return STAFResult.OK, STAFResult.NotSemaphoreOwner.
	 */
	public STAFResult releaseMutex(String holder){
		if(holder == null || this.holder == null || !this.holder.equals(holder))
			return new STAFResult(STAFResult.NotSemaphoreOwner, this.getItemName());
		holder = null;
		setItemState(STATE_RELEASED);
		releaseWaiters();
		return new STAFResult(STAFResult.Ok);
	}
}
