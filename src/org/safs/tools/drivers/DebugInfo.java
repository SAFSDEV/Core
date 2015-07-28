package org.safs.tools.drivers;
public class DebugInfo implements DebugInterface {

	protected boolean breakpointsOn  = false;
	protected boolean recordDebugOn  = false;
	protected boolean commandDebugOn = false;
	protected boolean testDebugOn    = false;

	/**
	 * Constructor for DebugInfo
	 */
	public DebugInfo() {
		super();
	}

	/**
	 * @see DebugInterface#isBreakpointEnabled()
	 */
	public boolean isBreakpointEnabled() {
		return breakpointsOn;
	}

	/**
	 * @see DebugInterface#isRecordDebugEnabled()
	 */
	public boolean isRecordDebugEnabled() {
		return recordDebugOn;
	}

	/**
	 * @see DebugInterface#isCommandDebugEnabled()
	 */
	public boolean isCommandDebugEnabled() {
		return commandDebugOn;
	}

	/**
	 * @see DebugInterface#isTestRecordDebugEnabled()
	 */
	public boolean isTestRecordDebugEnabled() {
		return testDebugOn;
	}

	/**
	 * @see DebugInterface#setBreakpointEnabled(boolean)
	 */
	public void setBreakpointEnabled(boolean enabled) { breakpointsOn=enabled;}

	/**
	 * @see DebugInterface#setRecordDebugEnabled(boolean)
	 */
	public void setRecordDebugEnabled(boolean enabled) { recordDebugOn=enabled;}

	/**
	 * @see DebugInterface#setCommandDebugEnabled(boolean)
	 */
	public void setCommandDebugEnabled(boolean enabled) { commandDebugOn=enabled;}

	/**
	 * @see DebugInterface#setTestRecordDebugEnabled(boolean)
	 */
	public void setTestRecordDebugEnabled(boolean enabled) { testDebugOn=enabled;}

}

