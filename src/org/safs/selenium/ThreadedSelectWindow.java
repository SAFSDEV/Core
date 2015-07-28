package org.safs.selenium;

import org.safs.Log;

import com.thoughtworks.selenium.Selenium;
/**
 * Used to workaround a defect in Selenium's selectWindow method.  The method 
 * sometimes locks-up indefinitely if the sought item is still being loaded or 
 * otherwise is not yet ready for testing. 
 * 
 * @author Carl Nagle
 */
public class ThreadedSelectWindow extends Thread {

    private Selenium selenium = null;
    private String target_id = null;
    private long max_time = 30;
    private long timer = 0;
    private boolean found = false;
    private boolean searching = false;
    private Thread thread = null;

    /**
     * Disable instantiation of default constructor.
     */
	private ThreadedSelectWindow() {
		super();
	}
	
	/**
	 * Intended usage:
	 *  
	 *  ThreadedSelectWindow selectWindow = 
	 *  	new ThreadedSelectWindow(Thread.currentThread(), selenium, id, timeout);
	 *  try{
	 *  	selectWindow.start();
	 *  	Thread.sleep((timeout * 1000)+ 1000); //backup timeout for deadlocks
	 *  } catch (Exception e){}
	 *  if(selectWindow.isSearching()){
	 *  	Log.debug("SGU: Selenium.selectWindow thread may have deadlocked after "+ 
	 *  			  selectWindow.searchSeconds() +" seconds.");
	 *  }
	 *  return selectWindow.isSelected();
	 *  
	 * @param caller -- Thread to be interrupted upon successful run()--the calling Thread.
	 * @param server -- Selenium object on which to invoke threaded selectWindow
	 * @param id -- id of browser window object to select -- can be null
	 * @param timeout_seconds -- timeout in seconds to attempt the search
	 */
	public ThreadedSelectWindow(Thread caller, Selenium server, String id, long timeout_seconds) {
		super();
		selenium = server;
		target_id = id;
		max_time = timeout_seconds;	
		thread = caller;
	}
	
	/**
	 * Implements the Runnable Interface.
	 * Attempt a selenium.selectWindow(target_id) call.
	 * This should be executed via a call to a new Thread using a ThreadedSelectWindow instance
	 * and then invoking start() on that new Thread instance.
	 */
	public void run() {
		timer = 0;
		found = false;
		searching = true;
		do {
			try{
				selenium.selectWindow(target_id);
				found = true;
				searching = false;
				thread.interrupt();
			} catch (Exception e){
				try {
					Thread.sleep(1000);
					timer++;
				} catch (InterruptedException e1) {}			
			}
		} while(!found && timer < max_time);
		searching = false;
	}

	/**
	 * @return true if the target item was found within the timeout period.
	 */
	public boolean isSelected() {
		return found;
	}

	/**
	 * @return true if the search is still in-progress.
	 */
	public boolean isSearching() {
		return searching;
	}
	/**
	 * @return how long the search took, or has taken so far, in seconds.
	 */
	public long searchSeconds(){
		return timer;
	}
}
