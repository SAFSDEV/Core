package org.safs.tools.drivers;
public interface DebugInterface {

	/**
	 * During debugging, do we STOP at BP (Breakpoint) records?
	 */
	public boolean isBreakpointEnabled();

	/**
	 * During debugging, do we STOP on EVERY record?
	 */
	public boolean isRecordDebugEnabled();

	/**
	 * During debugging, do we STOP on C,CW,CF Driver Command records?
	 */
	public boolean isCommandDebugEnabled();

	/**
	 * During debugging, do we STOP on T,TW,TF Test records?
	 */
	public boolean isTestRecordDebugEnabled();

	/**
	 * Enable/Disable BP (Breakpoint) record debugging.
	 */
	public void setBreakpointEnabled(boolean enabled);

	/**
	 * Enable/Disable debugging at EVERY record.
	 */
	public void setRecordDebugEnabled(boolean enabled);

	/**
	 * Enable/Disable C,CW,CF (Driver Command) record debugging.
	 */
	public void setCommandDebugEnabled(boolean enabled);

	/**
	 * Enable/Disable T,TW,TF (Test) record debugging.
	 */
	public void setTestRecordDebugEnabled(boolean enabled);

}

