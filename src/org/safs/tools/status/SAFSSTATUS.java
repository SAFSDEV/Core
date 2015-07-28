package org.safs.tools.status;

import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.CoreInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.vars.VarsInterface;

public class SAFSSTATUS implements ConfigurableToolsInterface, StatusMonitorInterface {

	protected CountersInterface counters = null;
	protected VarsInterface     vars = null;
	
	/**
	 * Constructor for SAFSSTATUS
	 */
	public SAFSSTATUS() {
		super();
	}

	/**
	 * Expects a DriverInterface for initialization.
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
	}

    /** 
     * TRUE if the underlying tool is running.
     * Typically called AFTER initialization attempts have been made.
     */
	public boolean isToolRunning(){
		return false;
	}
	
	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
	}

	/**
	 * @see GenericToolsInterface#shutdown()
	 */
	public void shutdown() {
	}


	/**
	 * @see GenericToolsInterface#getCoreInterface()
	 * @throws IllegalStateException ALWAYS since there is no CoreInterface in this class.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException {
		throw new IllegalStateException("SAFSSTATUS does not interface with the core framework directly.");
	}
	
	/**
	 * @see StatusMonitorInterface#getStatusInfo(String)
	 */
	public StatusInfoInterface getStatusInfo(String testLevel) {
		return null;
	}

	/**
	 * @see StatusMonitorInterface#setStatusInfo(String, StatusInfoInterface)
	 */
	public void setStatusInfo(String testLevel, StatusInfoInterface statusInfo) {
	}

}

