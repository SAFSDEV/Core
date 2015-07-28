package org.safs.tools.engines;

import org.safs.DDGUIUtilities;
import org.safs.DCGUIUtilities;
import org.safs.DriverCommandProcessor;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.logging.LogUtilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.logs.LogsInterface;

/**
 * Provides support for comprehensive, in-process Driver Commands.
 * <p>
 * Here we are going to use the existing org.safs.DriverCommandsProcessor.<br>
 * We are going to attempt to use them in-process, rather than starting up the 
 * STAF SAFS/DriverCommands version of the same.  This mimics how RobotJ is able 
 * to use these same Driver Command classes.
 * <p>
 * Note, the current implementation of DriverCommandsProcessor requires access to 
 * a STAFHelper.  So this class can only be used when such a STAFHelper is accessible 
 * from tools associated with the Driver, or via the STAFProcessHelpers class.
 * @author CANAGL DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 */
public class SAFSDRIVERCOMMANDS extends GenericEngine {

	/** 'SAFS/IPDriverCommands' **/
	static final String ENGINE_NAME = "SAFS/IPDriverCommands";

	/** Needed by STAF-centric DriverCommandProcessor. **/
	protected LogUtilities            logUtils = new LogUtilities();
	
	/** Needed by command Processors. **/
	protected DDGUIUtilities          guiUtils = new DCGUIUtilities();

	///** Needed by TIDDriver-centric EngineInterface. **/
	//protected LogsInterface               logs = null;

	/** Standard Java-based Driver Command processor. **/
	protected DriverCommandProcessor processor = new DriverCommandProcessor();	
	
	/**
	 * Constructor for SAFSDRIVERCOMMANDS
	 */
	public SAFSDRIVERCOMMANDS() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSDriverCommands.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public SAFSDRIVERCOMMANDS(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Expects a DriverInterface object for initialization.
	 * 
	 * @see GenericEngine#launchInterface(Object)
	 * @see DriverInterface
	 */
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);
		logUtils.setSTAFHelper(staf);
		processor.setLogUtilities(logUtils);	
		System.out.println("("+ launchCount +") "+ ENGINE_NAME +
		                     " running internal to "+ processName);		
	}
		
	/**
	 * Process the record present in the provided testRecordData.
	 */
	public long processRecord (TestRecordHelper testRecordData){
		this.testRecordData = testRecordData;
		try{			
			testRecordData.setDDGUtils(guiUtils);
			guiUtils.setTestRecordData(testRecordData);
			if(testRecordData.getSTAFHelper()==null) testRecordData.setSTAFHelper(staf);
		    processor.setTestRecordData(testRecordData);
		    processor.process();
		    return testRecordData.getStatusCode();
		}
		catch(ClassCastException ccx){
			System.err.println(
			"TestRecordHelper not valid!\n"+
			"SAFSIPDriverCommands requires a valid TestRecordHelper for processRecord initialization!\n"+
			ccx.getMessage());
		}
		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}
	
}

