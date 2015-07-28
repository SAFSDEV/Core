/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.input;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.input.EmbeddedInputService;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.ibm.staf.STAFResult;

/**
 * This concrete implementation interfaces clients to the SAFSINPUT service.
 * This class expects a DriverInterface object to provide access to all 
 * configuration information.
 * <p>
 * <ul><li><h4>ConfigureInterface Information</h4>
 * <p><pre>
 * [STAF]
 * ;NOSTAF=TRUE  will launch this service as an embedded services if AUTOLAUNCH=TRUE.
 * 
 * [SAFS_INPUT]
 * AUTOLAUNCH=TRUE
 * ;ITEM=org.safs.tools.input.SAFSINPUT
 * ;Service=SAFSINPUT
 * ;ServiceClass will be different for STAF2 and STAF3
 * ;ServiceClass=org.safs.staf.service.input.SAFSInputService
 * ;ServiceClass=org.safs.staf.service.input.SAFSInputService3
 * ;ServiceClass=org.safs.staf.service.input.EmbeddedInputService
 * 
 * ;If we indicate the SERVICEJAR, the jar's manifest file contains the necessary info to
 * ;distinguish the STAF's version, and select the right class to load service
 * ;SERVICEJAR=C:\safs\lib\safsinput.jar
 * ;OPTIONS=
 * 
 * </pre><br>
 * Note those items commented with semicolons are only needed when using alternate values.</ul>
 * <p>
 * <dl>
 * <dt>AUTOLAUNCH
 * <p><dd>
 * TRUE--Enable this class to launch the STAF service if it is not already running.<br>
 * FALSE--Do not try to launch the service if it is not running.
 * <p>
 * The Driver's 'safs.driver.autolaunch' command-line option is also queried 
 * for this setting and overrides any other configuration source setting.  
 * <p>
 * The default AUTOLAUNCH setting is TRUE.
 * <p>
 * <dt>ITEM
 * <p><dd>
 * The full class name for an alternate InputInterface class for SAFSINPUT.  
 * This parameter is only needed if an alternate/custom InputInterface is used.
 * The class must be findable by the JVM and Class.forName functions.  
 * <p>
 * The default ITEM value is  org.safs.tools.input.SAFSINPUT
 * <p>
 * <dt>SERVICECLASS
 * <p><dd>
 * The full class name for an alternate service class for SAFSINPUT.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * This setting, if provided, will cause any SERVICEJAR setting to be ignored.
 * <p>
 * The default SERVICECLASS value is  org.safs.staf.service.input.SAFSInputService
 * <p>
 * <dt>SERVICEJAR
 * <p><dd>
 * The full path and name for an alternate service JAR for SAFSINPUT.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * If a value is specified for SERVICECLASS, then this setting is ignored.
 * <p>
 * The default SERVICEJAR value is  [safsroot]/lib/safsinput.jar
 * <p>
 * <dt>SERVICE
 * <p><dd>
 * The service name for an alternate service instead of "SAFSINPUT".
 * This parameter is only needed if an alternate/custom service is used.
 * Note: All of the standard SAFS Framework tools currently expect the default 
 * "SAFSINPUT" service name.
 * <p>
 * <dt>OPTIONS
 * <p><dd>
 * Any additional PARMS to be sent to the service upon initialization.
 * We already handle sending the DIR parameter with the path obtained from 
 * the Driver.  Any other options needed for service initialization should be 
 * specified here.  There typically will be none.
 * </dl>
 * @author Carl Nagle DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author Carl Nagle JUN 17, 2014 Commencing support for embedded (non-STAF) services.
 * @author Carl Nagle JUL 16, 2014 Added NOSTAF support for the Embedded Service.
 **/
public class SAFSINPUT extends DriverConfiguredSTAFInterfaceClass implements InputInterface {

	/** "org.safs.staf.service.input.SAFSInputService" */
	protected static final String DEFAULT_SAFSINPUT_CLASS = "org.safs.staf.service.input.SAFSInputService";
	/** "org.safs.staf.service.input.SAFSInputService3" */
	protected static final String DEFAULT_SAFSINPUT_CLASS3 = "org.safs.staf.service.input.SAFSInputService3";
	/** "org.safs.staf.service.input.EmbeddedInputService" */
	protected static final String DEFAULT_SAFSINPUT_EMBEDDED_CLASS = "org.safs.staf.service.input.EmbeddedInputService";
	
	/**************************************************************
	 * "safsinput.jar" 
	 */
	protected static final String DEFAULT_SAFSINPUT_JAR   = "safsinput.jar";

	/**************************************************************
	 * Stores classname or JAR file fullpath for STAF service initialization.
	 */
	protected String classpath = "";

	/**************************************************************
	 * Constructor for SAFSINPUT.
	 * The object cannot do much of anything at all until the DriverInterface 
	 * and ConfigureInterface have been received via the launchInterface function.
	 */
	public SAFSINPUT() {
		super();
		servicename = STAFHelper.SAFS_INPUT_SERVICE;
	}

	private void startEmbeddedService(String inputdir)throws IllegalArgumentException{
    	System.out.println("config.EmbeddedInputService bypassing STAF Service creation for "+ servicename);
    	classpath = EmbeddedInputService.class.getName();
    	EmbeddedInputService eserv = new EmbeddedInputService();
    	eserv.init(new InfoInterface.InitInfo(servicename, "DIR "+ inputdir));
	}
	
	/**************************************************************
	 * Expects a DriverInterface for initialization.
	 * The superclass handles generic initialization and then we provide 
	 * SAFSINPUT-specific initialization.
	 * <p>
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
		
		super.launchInterface(configInfo);

		// see if SAFSINPUT is already running
		// launch it if our config says AUTOLAUNCH=TRUE and it is not running
		// otherwise don't AUTOLAUNCH it.
		if( ! staf.isServiceAvailable(servicename)){

			System.out.println(servicename +" is not running. Evaluating AUTOLAUNCH...");
			
			//check to see if AUTOLAUNCH was passed as a Driver command-line option
			String setting = System.getProperty(DriverConstant.PROPERTY_SAFS_DRIVER_AUTOLAUNCH, "");
			
			// if not
			if (setting.length()==0){

				//check to see if AUTOLAUNCH of SAFSINPUT exists in ConfigureInterface
				setting = config.getNamedValue(DriverConstant.SECTION_SAFS_INPUT, 
				                "AUTOLAUNCH");
				if (setting==null) setting = "";
			}
			boolean launch = StringUtilities.convertBool(setting);

		    String inputdir  = driver.getDatapoolDir();
		    String tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_INPUT, "SERVICE");
		    Log.debug("config.SERVICE="+tempstr);				                 
		    servicename = (tempstr==null) ? STAFHelper.SAFS_INPUT_SERVICE : tempstr;
  
		    // launch it if we dare!
			if (launch && !STAFHelper.no_staf_handles){
			    
			    String options = null;			    

			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_INPUT, 
			         		      "SERVICECLASS");
				Log.debug("config.ServiceClass="+tempstr);				                 
				if (tempstr == null) {
					tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_INPUT, 
			         		      "SERVICEJAR");
     			    Log.debug("config.ServiceJAR="+tempstr);
				}

				//If the STAF's version is 3, then the service class will be org.safs.staf.service.input.SAFSInputService3
				String defaultServiceClass = (staf.getSTAFVersion()==3) ? DEFAULT_SAFSINPUT_CLASS3 : DEFAULT_SAFSINPUT_CLASS; 
			    classpath = (tempstr==null) ? defaultServiceClass : tempstr;
			    
			    // do normal stuff if NOT embedded
			    if(! classpath.equalsIgnoreCase(DEFAULT_SAFSINPUT_EMBEDDED_CLASS)){
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_INPUT, 
				                      "OPTIONS");
					Log.debug("config.OPTIONS="+tempstr);				                 
				    options   = (tempstr==null) ? "" : tempstr;
			    
				    options   = configureJSTAFServiceEmbeddedJVMOption(options);
	
				    // launch SAFSINPUT
					staf.addService(machine, servicename, classpath, inputdir, options);
				    waitForServiceStartCompletion(5);
			    }else{
			    	startEmbeddedService(inputdir);
				    waitForServiceStartCompletion(5);
			    }
			}else if (launch && STAFHelper.no_staf_handles){
		    	startEmbeddedService(inputdir);		    	
			    waitForServiceStartCompletion(5);
			}
			// not supposed to autolaunch
			else{
				System.out.println(servicename +" AUTOLAUNCH is not enabled.");
				// ?we will hope the user is getting it online before we have to use it?
			}
		}			
	}

	/**
	 * Open a new Input source.<br>
	 * The UniqueSourceInterface is expected to simply have the id  
	 * and the filename or fullpath.
	 * <p>
	 * @see InputInterface#open(UniqueSourceInterface)
	 */
	public boolean open(UniqueSourceInterface source) {

		String id = (String)source.getUniqueID();
		if (id==null) id="";

		//String info = (String) source.getSourceName();
		String info = source.getSourcePath(driver);
		if (info==null) info="";
		
		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "OPEN " + staf.lentagValue(id)   +
		                               " FILE "+ staf.lentagValue(info));

		return (result.rc == STAFResult.Ok);		                               
	}


	/**
	 * Return a valid InputRecordInfo object or an InputRecordInvalid object.
	 * Parses the record looking for valid "linenum:data" format.
	 */
	protected InputRecordInterface parseInputRecord (STAFResult result){

		String record = result.result;
		if (result.rc != STAFResult.Ok){
			Log.info(servicename +": STAF input error: "+ String.valueOf(result.rc) +":"+ record);
			return new InputRecordInvalid();
		}
		
		//this may never happen now..
		if ( record == null) return new InputRecordInvalid();
		
		int sep = record.indexOf(':');
		if (sep < 1) return new InputRecordInvalid();
		
		String num = record.substring( 0, sep );
		String rec = record.substring( sep+1  );
		
		try{
			long val = Long.parseLong(num);
			return new InputRecordInfo(rec, val);
		}
		catch(NumberFormatException nfe){
			return new InputRecordInvalid();		
		}		
	}
	

	/**
	 * Return the next record from the specified input source.
	 * <p>
	 * @see InputInterface#nextRecord(UniqueIDInterface)
	 */
	public InputRecordInterface nextRecord(UniqueIDInterface source) {

		String id = (String)source.getUniqueID();
		if (id==null) id="";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "NEXT " + staf.lentagValue(id));

		return parseInputRecord(result);
	}


	/**
	 * Goto the beginning of the specified input source.
	 * <p>
	 * @see InputInterface#gotoStart(UniqueIDInterface)
	 */
	public boolean gotoStart(UniqueIDInterface source) {
		String id = (String)source.getUniqueID();
		if (id==null) id="";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "BEGIN " + staf.lentagValue(id));

		return (result.rc == STAFResult.Ok);
	}

	/**
	 * Goto a specific linenumber or blockid of the specified input source.
	 * <p>
	 * @see InputInterface#gotoRecord(UniqueRecordInterface)
	 */
	public InputRecordInterface gotoRecord(UniqueRecordInterface recordInfo) {
		
		UniqueRecordNumInterface recnum = null;
		UniqueRecordIDInterface  recid  = null;
		String val = null;

		String id = (String) recordInfo.getUniqueID();
		String sep = recordInfo.getSeparator();
		
		if (recordInfo instanceof UniqueRecordNumInterface){
			recnum = (UniqueRecordNumInterface) recordInfo;
			val = String.valueOf(recnum.getRecordNum()).trim();
		}
		else if(recordInfo instanceof UniqueRecordIDInterface){
			recid = (UniqueRecordIDInterface) recordInfo;
			val = (String) recid.getRecordID();
		}
		else{
			Log.info(servicename +": invalid input RecordInterface:"+ recordInfo);
			return new InputRecordInvalid();
		}
		
		Log.info(servicename +":seeking \""+ val +"\" in \""+ id +"\" using sep \""+ sep +"\"");
		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "GOTO " + staf.lentagValue(id) +
	                                    " LOCATE " + staf.lentagValue(val) +
	                                    " SEPARATOR "+ staf.lentagValue(sep));

		return parseInputRecord(result);
	}

	/**
	 * Close the specified input source.
	 * <p>
	 * @see InputInterface#close(UniqueIDInterface)
	 */
	public void close(UniqueIDInterface source) {

		String id = (String)source.getUniqueID();
		if (id==null) id="";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "CLOSE " + staf.lentagValue(id));

	}

	/**************************************************************
	 * Reset/Clear all sources in the service.
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
		                                "RESET");
	}

}

