package org.safs.tools.drivers;
public interface FlowControlInterface {
	
	/****************************************************** 
	 * Is a ScriptNotExecuted BlockID assigned?
	 **/
	public boolean isScriptNotExecutedBlockValid ();

	/** BlockID for status=ScriptNotExecuted response. 
	 **/
	public String getScriptNotExecutedBlock ();

	/** BlockID for status=ScriptNotExecuted response. 
	 **/
	public void setScriptNotExecutedBlock (String blockID);



	/****************************************************** 
	 * Is an ExitTable BlockID assigned?
	 **/
	public boolean isExitTableBlockValid ();

	/** BlockID for status=ExitTable response. 
	 **/
	public String getExitTableBlock ();

	/** BlockID for status=ExitTable response. 
	 **/
	public void setExitTableBlock (String blockID);



	/****************************************************** 
	 * Is a NoScriptFailure BlockID assigned?
	 **/
	public boolean isNoScriptFailureBlockValid ();

	/** BlockID for status=NoScriptFailure response. 
	 **/
	public String getNoScriptFailureBlock ();

	/** BlockID for status=NoScriptFailure response. 
	 **/
	public void setNoScriptFailureBlock (String blockID);



	/****************************************************** 
	 * Is a ScriptFailure BlockID assigned?
	 **/
	public boolean isScriptFailureBlockValid ();

	/** BlockID for status=ScriptFailure response. 
	 **/
	public String getScriptFailureBlock ();

	/** BlockID for status=ScriptFailure response. 
	 **/
	public void setScriptFailureBlock (String blockID);



	/***************************************************** 
	 * Is a ScriptWarning BlockID assigned?
	 **/
	public boolean isScriptWarningBlockValid ();

	/** BlockID for status=ScriptWarning response. 
	 **/
	public String getScriptWarningBlock ();

	/** BlockID for status=ScriptWarning response. 
	 **/
	public void setScriptWarningBlock (String blockID);



	/***************************************************** 
	 * Is an IOFailrue BlockID assigned?
	 **/
	public boolean isIOFailureBlockValid ();

	/** BlockID for status=IOFailure response. 
	 **/
	public String getIOFailureBlock ();

	/** BlockID for status=IOFailure response. 
	 **/
	public void setIOFailureBlock (String blockID);
	
}

