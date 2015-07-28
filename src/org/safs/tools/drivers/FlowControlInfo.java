package org.safs.tools.drivers;
public class FlowControlInfo implements FlowControlInterface {

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_ScriptNotExecuted = "";

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_ExitTable         = "";

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_NoScriptFailure   = "";

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_ScriptFailure     = "";

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_ScriptWarning     = "";

	/** BlockID for error recovery and flow control. 
	 **/
	protected String block_IOFailure         = "";
	

	/**
	 * Constructor for FlowControlInfo
	 */
	public FlowControlInfo() {
		super();
	}

	/**
	 * @see FlowControlInterface#isScriptNotExecutedBlockValid()
	 */
	public boolean isScriptNotExecutedBlockValid() {
		return (block_ScriptNotExecuted.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getScriptNotExecutedBlock()
	 */
	public String getScriptNotExecutedBlock() {
		return block_ScriptNotExecuted;
	}

	/**
	 * @see FlowControlInterface#setScriptNotExecutedBlock(String)
	 */
	public void setScriptNotExecutedBlock(String blockID) {
		if(blockID!=null) block_ScriptNotExecuted = blockID;
	}

	/**
	 * @see FlowControlInterface#isExitTableBlockValid()
	 */
	public boolean isExitTableBlockValid() {
		return (block_ExitTable.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getExitTableBlock()
	 */
	public String getExitTableBlock() {
		return block_ExitTable;
	}

	/**
	 * @see FlowControlInterface#setExitTableBlock(String)
	 */
	public void setExitTableBlock(String blockID) {
		if(blockID!=null) block_ExitTable=blockID;
	}

	/**
	 * @see FlowControlInterface#isNoScriptFailureBlockValid()
	 */
	public boolean isNoScriptFailureBlockValid() {
		return (block_NoScriptFailure.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getNoScriptFailureBlock()
	 */
	public String getNoScriptFailureBlock() {
		return block_NoScriptFailure;
	}

	/**
	 * @see FlowControlInterface#setNoScriptFailureBlock(String)
	 */
	public void setNoScriptFailureBlock(String blockID) {
		if(blockID!=null) block_NoScriptFailure = blockID;
	}

	/**
	 * @see FlowControlInterface#isScriptFailureBlockValid()
	 */
	public boolean isScriptFailureBlockValid() {
		return (block_ScriptFailure.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getScriptFailureBlock()
	 */
	public String getScriptFailureBlock() {
		return block_ScriptFailure;
	}

	/**
	 * @see FlowControlInterface#setScriptFailureBlock(String)
	 */
	public void setScriptFailureBlock(String blockID) {
		if(blockID!=null) block_ScriptFailure = blockID;
	}

	/**
	 * @see FlowControlInterface#isScriptWarningBlockValid()
	 */
	public boolean isScriptWarningBlockValid() {
		return (block_ScriptWarning.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getScriptWarningBlock()
	 */
	public String getScriptWarningBlock() {
		return block_ScriptWarning;
	}

	/**
	 * @see FlowControlInterface#setScriptWarningBlock(String)
	 */
	public void setScriptWarningBlock(String blockID) {
		if(blockID!=null) block_ScriptWarning = blockID;
	}

	/**
	 * @see FlowControlInterface#isIOFailureBlockValid()
	 */
	public boolean isIOFailureBlockValid() {
		return (block_IOFailure.length() > 0);
	}

	/**
	 * @see FlowControlInterface#getIOFailureBlock()
	 */
	public String getIOFailureBlock() {
		return block_IOFailure;
	}

	/**
	 * @see FlowControlInterface#setIOFailureBlock(String)
	 */
	public void setIOFailureBlock(String blockID) {
		if(blockID!=null) block_IOFailure = blockID;
	}

}

