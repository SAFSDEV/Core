package org.safs.tools;

public interface GenericToolsInterface {
	
	/** 
	 * Reset or clear any cached information in the underlying tool or service.
     * The implementation should be able to recognize if the underlying tool or service 
     * is not actually running.  If the tool is not running, then this should exit 
     * gracefully without adversely affecting anything. **/
	public void reset ();
	
    /** 
     * Shutdown the underlying tool or service. 
     * The implementation should be able to recognize if the underlying tool or service 
     * is not actually running.  If the tool is not running, then this should exit 
     * gracefully without adversely affecting anything. **/
	public void shutdown ();
	
    /** 
     * TRUE if the underlying tool is running.
     * Typically called AFTER initialization attempts have been made.
     */
	public boolean isToolRunning();
	
	/**
	 * Retrieve the instance of the CoreInterface used to communicate with the framework.
	 * @return CoreInterface instance.
	 * @throws IllegalStateException if the CoreInterface is null, has not been initialized yet, or is otherwise invalid.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException;
}

