package org.safs.tools;

public interface SimpleToolsInterface extends GenericToolsInterface {

    /** 
     * Launch/initialize the underlying tool or service. 
     * The implementation should be able to recognize if the underlying tool or service 
     * is already running, or not.  If the tool is already running, then it should exit 
     * gracefully without adversely affecting anything. **/
	public void launchInterface ();
}

