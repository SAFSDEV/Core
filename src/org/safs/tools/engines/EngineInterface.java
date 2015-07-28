package org.safs.tools.engines;

import org.safs.TestRecordHelper;
import org.safs.tools.ConfigurableToolsInterface;

public interface EngineInterface extends ConfigurableToolsInterface {

	/** 
	 * "SHUTDOWN_HOOK" 
	 * A TRD inputrecord command to terminate and shutdown the hook normally.
	 */
	public static final String COMMAND_SHUTDOWN_HOOK = "SHUTDOWN_HOOK";
	
	/** 
	 * Retrieve the name of the Engine.  For example, SAFS/DriverCommands.
	 * For STAF engines, this would be the engine's process name in STAF.
	 */
	public String getEngineName();

	/** 
	 * Execute the processing of a stored test record.
	 * The test record and associated test record data should already be 
	 * stored and accessible to the engine.  The routine should return 
	 * a standard return code as defined in our DriverConstants.
	 * <p>
	 * For STAF engines, this means the Test Record Data is stored in the 
	 * predefined SAFSVARS storage.
	 * <p>
	 * For other engines, this will be implementation specific.
	 * @return the status code resulting from the processing.<br>
	 * <ul>
	 * <li> 4 -- SCRIPT NOT EXECUTED. "I did not process this record."
	 * <li>-1 -- NO SCRIPT FAILURE.  "I processed the record successfully."
	 * <li>-2 -- SCRIPT WARNING. "I processed the record, issued warning(s)."
	 * <li> 0 -- SCRIPT FAILURE. "I processed the record, issued failure(s)."
	 * <li> 2 -- IO FAILURE. "I process the record, issued IO failure(s)."
	 * <li> 8 -- EXIT TABLE. "I processed the record, exit current table."
	 * <li>16 -- IGNORE STATUS. "I processed the record, ignore code and continue."
	 * </ul>
	 * @see org.safs.tools.drivers.DriverConstant DriverConstants 
	 */
	public long processRecord(TestRecordHelper testRecordData);
}

