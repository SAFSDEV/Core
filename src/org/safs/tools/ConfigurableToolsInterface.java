package org.safs.tools;

public interface ConfigurableToolsInterface extends GenericToolsInterface {

    /** 
     * Launch/initialize the underlying tool or service. 
     * The implementation should be able to recognize if the underlying tool or service 
     * is already running, or not.  If the tool is already running, then it should exit 
     * gracefully without adversely affecting anything. 
     * <p>
     * @param configInfo can be whatever the final implementation needs it to be.  This 
     * might be a String with configuration information, the name of a class, or an instance 
     * of some object used to provide configuration assets.**/
	public void launchInterface (Object configInfo);
}

