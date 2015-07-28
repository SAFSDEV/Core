package org.safs;

import org.safs.logging.*;

public class DCJavaHook extends JavaHook {

    public static final String SAFS_DRIVER_COMMANDS = "SAFS/DriverCommands";
	/**
	 * Constructor for DCJavaHook
	 */
	public DCJavaHook() {
		super();
	}

	/**
	 * Constructor for DCJavaHook
	 */
	public DCJavaHook(String process_name) {
		super(process_name);
	}

	/**
	 * Constructor for DCJavaHook
	 */
	public DCJavaHook(String process_name, LogUtilities logs) {
		super(process_name, logs);
	}

	/**
	 * Constructor for DCJavaHook
	 */
	public DCJavaHook(String process_name, String trd_name) {
		super(process_name, trd_name);
	}


	/**
	 * Standard Constructor for DCJavaHook
	 */
	public DCJavaHook(String process_name, String trd_name, LogUtilities logs) {
		super(process_name, trd_name, logs);
	}

    /**
     * Advanced Constructor for DCJavaHook.
     */  
    public DCJavaHook (String process_name, String trd_name, LogUtilities logs,
                       TestRecordHelper trd_data, 
                       DDGUIUtilities gui_utils, 
                       ProcessRequest aprocessor){
  
        super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
    }


	/**
	 * Use this method to retain the current/default DDGUIUtilities implementation.
	 * @see JavaHook#getTRDData()
	 */
	public TestRecordHelper getTRDData() {
          if (data == null){
            data = new DCTestRecordHelper();
            data.setSTAFHelper(getHelper());
            data.setDDGUtils(getGUIUtilities());
          }
          return data;
	}


	/**
	 * @see JavaHook#getGUIUtilities()
	 */
	public DDGUIUtilities getGUIUtilities() {
		if (utils == null) {
			utils = new DCGUIUtilities();
			utils.setTestRecordData(getTRDData());
		}
		return utils;
	}


	/**
	 * @see JavaHook#getLogUtilities()
	 */
	public LogUtilities getLogUtilities() {
		if(log == null) log = new LogUtilities();
		return log;
	}


	/**
	 * @see JavaHook#getRequestProcessor()
	 */
	public ProcessRequest getRequestProcessor() {
		if(processor == null) 
		    processor = new ProcessRequest(getTRDData(), getLogUtilities());
		return processor;
	}


    public static void main (String[] args) {

		// SAMPLE STANDARD HOOK INITIALIZATION
		// DCJavaHook hook = new DCJavaHook(SAFS_DRIVER_COMMANDS, new LogUtilities());

		// SAMPLE ADVANCED HOOK INITIALIZATION
		
    	TestRecordHelper datahelper = new TestRecordHelper();
    	LogUtilities     logs       = new LogUtilities();
    	
    	ProcessRequest requester = new ProcessRequest(
            datahelper,                   // TestRecordHelper
            logs,                         // LogUtilities
            new DriverCommandProcessor(), // use standard DriverCommandProcessor
            null,                         // disable standard TestStepProcessor
            null,                         // no custom driver command support
            null);                        // no custom test step support
    	      
    	DDGUIUtilities   gui_utils  = new DCGUIUtilities();
    	
        DCJavaHook hook = new DCJavaHook(
            SAFS_DRIVER_COMMANDS,         // STAF process name for hook instance
            STAFHelper.SAFS_HOOK_TRD,     // (default) SAFSVARS TestRecordData
            logs,                         // LogUtilities
            datahelper,                   // TestRecordHelper
            gui_utils,                    // DDGUIUtilities
            requester);                   // ProcessRequest
        
        // this should now be properly handled by the superclass...
        datahelper.setSTAFHelper(hook.getHelper());

		// HOOK INITIALIZATION COMPLETE
		            
        if (args.length > 0 && args[0].equalsIgnoreCase("log")) {
          Log.setHelper(hook.getHelper());
          logs.setCopyLogClass(true);
        }
        hook.start();
  }
}

