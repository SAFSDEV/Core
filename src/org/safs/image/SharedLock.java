package org.safs.image;

import java.awt.Point;

/**
 * SharedLock is used to synchronized the main thread and search thread.
 * And it contains some shared data between main thread and search thread.
 * 
 * For main thread, please see method findImageWithXYBTThread() in ImageUtils
 * For search thread, please see SmallPieceComparator
 * 
 * @author LeiWang
 *
 * @see org.safs.image.ImageUtils
 * @see org.safs.image.SmallPieceComparator
 */

public class SharedLock {
	//The maximum bit we can tolerance the errors; If no BitTolerance is set on, this is 0
	private int maxErrorBits = 0;
	//This will be incremented, if one thread meet a unmatched bit
	private int errorBits =0;
	/**
	 * If one thread finished, then it will decrement this value,
	 * when this field is 0, which means all threads finish.
	 */
	private int runningThreads = 0;
	/**
	 * This field waiting is shared between main thread and other search thread
	 * Only when main thread set this field to true, the search thread can send
	 * the notification signal.
	 */
	private boolean waiting = false;
	private boolean matched = false;
	private Point matchedPoint = null;
	
	public SharedLock(){}
	public SharedLock(int maxErrorBits, int threads,boolean matched){
		this.maxErrorBits = maxErrorBits;
		errorBits = 0;
		runningThreads = threads;
		this.matched = matched;
	}
	
	public int getMaxErrorBits() {
		return maxErrorBits;
	}
	public void setMaxErrorBits(int maxErrorBits) {
		this.maxErrorBits = maxErrorBits;
	}

	//Increment the errorBits field and compare with the field maxErrorBits
	//True, if the errorBits is bigger than maxErrorBits
	public synchronized boolean incrementErrorBitsAndPassMax(){
		errorBits++;
		matched = !(errorBits>maxErrorBits);
		return !matched;
	}
	
	//If there exist running thread, then return true;
	public synchronized boolean hasRunningThreads() {
		return runningThreads!=0;
	}
	public synchronized boolean decrementRunningThreadsAndTestHasRunning(){
		runningThreads--;
		return runningThreads!=0;
	}
	public synchronized void decrementRunningThreads(){
		runningThreads--;
	}
	public synchronized boolean isMatched() {
		return matched;
	}
	public synchronized void setMatched(boolean matched) {
		this.matched = matched;
	}
	public synchronized Point getMatchedPoint() {
		return matchedPoint;
	}
	public synchronized void setMatchedPoint(Point matchedPoint) {
		if(!matched){
			matched = true;
			this.matchedPoint = matchedPoint;
		}
	}
	public synchronized void reset(int maxErrorBits, int threads,boolean matched){
		this.maxErrorBits = maxErrorBits;
		this.runningThreads = threads;
		this.matched = matched;
		this.errorBits = 0;
	}
	public synchronized boolean isWaiting() {
		return waiting;
	}
	public synchronized void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}
}
