/*******************************************************************************
 * Copyright (C) 2004 Novell, Inc
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.tools.logs;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.logging.EmbeddedLogService;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.stringutils.StringUtilities;

import com.ibm.staf.STAFResult;
 
 
/**
 * This concrete implementation interfaces clients to the SAFSLOGS service.
 * This class expects a DriverInterface object to provide access to all 
 * configuration information.
 * <p>
 * <ul><li><h4>ConfigureInterface Information</h4>
 * <p><pre>
 * [STAF]
 * ;NOSTAF=TRUE  will launch this service as an embedded services if AUTOLAUNCH=TRUE.
 * 
 * [SAFS_LOGS]
 * AUTOLAUNCH=TRUE
 * OVERWRITE=TRUE
 * CAPXML=TRUE
 * TRUNCATE=ON|OFF|#chars
 * ;ITEM=org.safs.tools.logs.SAFSLOGS
 * ;Service=SAFSLOGS
 * ;ServiceClass will be different for STAF2 and STAF3
 * ;ServiceClass=org.safs.staf.service.logging.v2.SAFSLoggingService
 * ;ServiceClass=org.safs.staf.service.logging.v3.SAFSLoggingService3
 * ;If we indicate the SERVICEJAR, the jar's manifest file contains the necessary info to
 * ;distinguish the STAF's version, and select the right class to load service
 * ;SERVICEJAR=C:\safs\lib\safslogs.jar
 * ;OPTIONS=
 * </pre><br>
 * Note those items commented with semicolons are only needed when using alternate values.</ul>
 * </pre></ul>
 * <p>
 * <dl>
 * <dt>AUTOLAUNCH
 * <p><dd>
 * TRUE--Enable this class to launch the STAF service if it
 * is not already running.<br>
 * FALSE--Do not try to launch the service if it is not running.
 * <p>
 * The Driver's 'safs.driver.autolaunch' command-line option is also queried 
 * for this setting and overrides any other configuration source setting.  
 * <p>
 * The default AUTOLAUNCH setting is TRUE.
 * <p>
 * <dt>OVERWRITE
 * <p><dd>
 * TRUE for the service to delete/overwrite existing logs of the same name.
 * If this parameter is not TRUE, the service will NOT overwrite existing logs 
 * and no logs will be generated.  The files will have to be moved or renamed 
 * before logging will commence.
 * <p>
 * The default OVERWRITE value is FALSE
 * <p>
 * <dt>CAPXML
 * <p><dd>
 * TRUE for the service to "cap" the XML logs when they are closed.  XML logs 
 * when closed are not yet valid XML because they do not have the standard XML 
 * header, nor a single root node.  This allows multiple logs to be appended 
 * together before they are made into valid XML.
 * <p>
 * The default CAPXML value is FALSE.  Most users will want to provide this 
 * value set to TRUE, unless they intend to cap the XML themselves at some later 
 * time.
 * <p>
 * <dt>TRUNCATE
 * <p><dd>
 * Truncate messages and descriptions to a fixed length.  By default, Truncate is 
 * disabled.  When enabled, the default truncate length for messages and descriptions 
 * is 128 characters each.<br>
 * The user can change the truncate length by sending an integer > 0 as the value
 * of the option.<br>
 * Ex: TRUNCATE 256<br>
 * TRUNCATE ON -- same as TRUNCATE with no option value. Enables truncate mode.<br>
 * TRUNCATE OFF -- disables truncate mode.
 * <p>
 * The default CAPXML value is FALSE.  Most users will want to provide this 
 * value set to TRUE, unless they intend to cap the XML themselves at some later 
 * time.
 * <p>
 * <dt>ITEM
 * <p><dd>
 * The full class name for an alternate LogsInterface class for SAFSLOGS.  
 * This parameter is only needed if an alternate/custom LogsInterface is used.
 * The class must be findable by the JVM and Class.forName functions.  
 * <p>
 * The default ITEM value is  org.safs.tools.logs.SAFSLOGS
 * <p>
 * <dt>SERVICECLASS
 * <p><dd>
 * The full class name for an alternate service class for SAFSLOGS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * This setting, if provided, will cause any SERVICEJAR setting to be ignored.
 * <p>
 * The default SERVICECLASS value is  org.safs.staf.service.logging.v2.SAFSLoggingService
 * <p>
 * <dt>SERVICEJAR
 * <p><dd>
 * The full path and name for an alternate service JAR for SAFSLOGS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * If a value is specified for SERVICECLASS, then this setting is ignored.
 * <p>
 * The default SERVICEJAR value is  [safsroot]/lib/safslogs.jar
 * <p>
 * <dt>SERVICE
 * <p><dd>
 * The service name for an alternate service instead of "SAFSLOGS".
 * This parameter is only needed if an alternate/custom service is used.
 * Note: All of the standard SAFS Framework tools currently expect the default 
 * "SAFSLOGS" service name.
 * <p>
 * <dt>OPTIONS
 * <p><dd>
 * Any additional PARMS to be sent to the service upon initialization.
 * We already handle sending the DIR parameter with the path obtained from 
 * the Driver.  Any other options needed for service initialization should be 
 * specified here.  There typically will be none.
 * </dl>
 * @author CANAGL 	DEC 14, 2005 	Refactored with DriverConfiguredSTAFInterface superclass
 * <BR/> CANAGL 	2009.03.19 	Fixed logMessage handling of empty Descriptions
 * <BR/> LeiWang 	2009.05.12 	Add support for STAF version 3.
 * <BR/> CANAGL     JUL 16, 2014 Added NOSTAF support for the Embedded Service.
 **/
public class SAFSLOGS extends DriverConfiguredSTAFInterfaceClass
                      implements LogsInterface {



     /** "org.safs.staf.service.logging.v2.SAFSLoggingService" */
	protected static final String DEFAULT_SAFSLOGS_CLASS = "org.safs.staf.service.logging.v2.SAFSLoggingService";

	/** "org.safs.staf.service.logging.v3.SAFSLoggingService3" */
	protected static final String DEFAULT_SAFSLOGS_CLASS3 = "org.safs.staf.service.logging.v3.SAFSLoggingService3";
    
	/** "org.safs.staf.service.logging.EmbeddedLogService" */
	protected static final String DEFAULT_EMBEDDED_SAFSLOGS_CLASS = "org.safs.staf.service.logging.EmbeddedLogService";
    
	/** "safslogs.jar" */
	protected static final String DEFAULT_SAFSLOGS_JAR = "safslogs.jar";

	/**************************************************************
	 * Stores classname or JAR file fullpath for STAF service initialization.
	 */
	protected String classpath = "";


	/**************************************************************
	 * Enables/Disables previous log overwrite when initializing a new log.
	 * Defaults to FALSE -- delete/overwrite previous log
	 */
	protected boolean overwrite = false;

	/**************************************************************
	 * Enables "capping" the XML log when it is closed.
	 * Defaults to FALSE -- 
	 */
	protected boolean capXML = false;

	/**************************************************************
	 * Enables "TRUNCATE" of log messages.
	 * Defaults to FALSE -- 
	 */
	protected boolean truncate = false;

	/**
	 * Default length of enabled TRUNCATE is 128 chars.
	 */
	protected int truncateLength = AbstractSAFSLoggingService.SLS_TRUNCATELENGTH_DEFAULT;
	
    // STAFHelper stafHlp = new STAFHelper(); 
	/**
	 * Constructor for SAFSLOGS
	 */
	public SAFSLOGS() {
		super();
		servicename = STAFHelper.SAFS_LOGGING_SERVICE;
	}

	private void startEmbeddedService(String logdir){
    	System.out.println("config.EmbeddedLogService bypassing STAF Service creation for "+ servicename);
    	classpath = EmbeddedLogService.class.getName();
    	EmbeddedLogService eserv = new EmbeddedLogService();
    	eserv.init(new InfoInterface.InitInfo(servicename, "DIR "+ logdir));
	}
		
	/**
	 * Expects a DriverInterface for initialization.
	 * The superclass handles generic initialization and then we provide 
	 * SAFSLOGS-specific initialization.
	 * <p>
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
		
		super.launchInterface(configInfo);

		//check to see if safs.log.overwrite was passed as a Driver command-line option
		String setting = System.getProperty(DriverConstant.PROPERTY_SAFS_LOG_OVERWRITE, "");

		//check to see if OVERWRITE exists in ConfigureInterface
		if (setting.length()==0) 
		    setting = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, "OVERWRITE");

		if (setting==null) setting = "";
		String msg = "SAFSLOGS interface 'OVERWRITE'="+ setting;
		Log.info(msg);
		System.out.println(msg);

		// launch it if we dare!
		if ((setting.equalsIgnoreCase("TRUE"))||
		    (setting.equalsIgnoreCase("YES")) ||
		    (setting.equalsIgnoreCase("1"))){

		      overwrite = true;
		}
		else{ overwrite = false; }
		msg = "SAFSLOGS interface setting OVERWRITE = "+ overwrite;
		Log.info(msg);
		System.out.println(msg);

		setting = null;
		//check to see if safs.log.capxml was passed as a Driver command-line option
		setting = System.getProperty(DriverConstant.PROPERTY_SAFS_LOG_CAPXML, "");
		
		//check to see if CAPXML exists in ConfigureInterface
		if (setting.length()==0) 
		    setting = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, "CAPXML");

		if (setting==null) setting = "";
		msg = "SAFSLOGS interface 'CAPXML'="+ setting;
		Log.info(msg);
		System.out.println(msg);

		// launch it if we dare!
		if ((setting.equalsIgnoreCase("TRUE"))||
		    (setting.equalsIgnoreCase("YES")) ||
		    (setting.equalsIgnoreCase("1"))){

		      capXML = true;
		}
		else{ capXML = false; }
		msg = "SAFSLOGS interface setting CAPXML = "+ capXML;
		Log.info(msg);
		System.out.println(msg);

		setting = null;
		//check to see if safs.log.truncate was passed as a Driver command-line option
		setting = System.getProperty(DriverConstant.PROPERTY_SAFS_LOG_TRUNCATE, "");
		
		//check to see if TRUNCATE exists in ConfigureInterface
		if (setting.length()==0) 
		    setting = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, "TRUNCATE");

		if (setting==null) setting = "";
		msg = "SAFSLOGS interface 'TRUNCATE'="+ setting;
		Log.info(msg);
		System.out.println(msg);

		// launch it if we dare!
		if ((setting.equalsIgnoreCase("TRUE"))||
		    (setting.equalsIgnoreCase("YES" ))||
		    (setting.equalsIgnoreCase("ON" ))){
		      truncate = true;		      
		}else{ 
			try{
				truncateLength = Integer.parseInt(setting);
				if(truncateLength < 1){
					truncateLength = AbstractSAFSLoggingService.SLS_TRUNCATELENGTH_DEFAULT;
				}else{
					truncate = true;
				}
			}catch(Exception x){
				
			}
		}
		msg = truncate ? "SAFSLOGS interface setting TRUNCATE at "+ truncateLength : 
			             "SAFSLOGS interface setting TRUNCATE OFF";
		Log.info(msg);
		System.out.println(msg);
		
		// see if SAFSLOGS is already running
		
		// launch it if our config says AUTOLAUNCH=TRUE and it is not running
		// otherwise don't AUTOLAUNCH it.
		if( ! staf.isServiceAvailable(servicename)){

			msg = servicename +" is not running. Evaluating AUTOLAUNCH...";
			Log.info(msg);
			System.out.println(msg);
			
			//check to see if AUTOLAUNCH was passed as a Driver command-line option
			setting = System.getProperty(DriverConstant.PROPERTY_SAFS_DRIVER_AUTOLAUNCH, "");

			// if not
			if (setting.length()==0){

				//check to see if AUTOLAUNCH of SAFSLOGS exists in ConfigureInterface
				setting = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, 
				                "AUTOLAUNCH");
				if (setting==null) setting = "";
			}
			boolean launch = StringUtilities.convertBool(setting);

		    String logdir  = driver.getLogsDir();
		    String tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, "SERVICE");
		    Log.debug("config.SERVICE="+tempstr);				                 
		    servicename = (tempstr==null) ? STAFHelper.SAFS_LOGGING_SERVICE : tempstr;
			
			// launch it if we dare!
			if (launch && !STAFHelper.no_staf_handles){
			    
			    String options = null;			    
	   	    

			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, 
			         		      "SERVICECLASS");
				Log.debug("SAFSLOG interface config.ServiceClass="+tempstr);				                 
				if (tempstr == null) {
					tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, 
			         		      "SERVICEJAR");
     			    Log.debug("SAFSLOG interface config.ServiceJAR="+tempstr);
				}
				//If the STAF's version is 3, then the service class will be org.safs.staf.service.logging.v3.SAFSLoggingService3
				String defaultServiceClass = (staf.getSTAFVersion()==3) ? DEFAULT_SAFSLOGS_CLASS3 : DEFAULT_SAFSLOGS_CLASS; 
			    classpath = (tempstr==null) ? defaultServiceClass : tempstr;
			    
			    if(! classpath.equalsIgnoreCase(DEFAULT_EMBEDDED_SAFSLOGS_CLASS)){
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_LOGS, 
				                      "OPTIONS");
					Log.debug("SAFSLOG interface config.Options="+tempstr);
				    options   = (tempstr==null) ? "" : tempstr;
				    options   = configureJSTAFServiceEmbeddedJVMOption(options);
	
				    // launch SAFSLOGS
					staf.addService(machine, servicename, classpath, logdir, options); 
					waitForServiceStartCompletion(5);
			    }else{
			    	startEmbeddedService(logdir);
					waitForServiceStartCompletion(5);
			    }
			}else if(STAFHelper.no_staf_handles){
				startEmbeddedService(logdir);
				waitForServiceStartCompletion(5);
			}
			// not supposed to autolaunch
			else{
				Log.info(servicename +" AUTOLAUNCH is not enabled.");
				System.out.println(servicename +" AUTOLAUNCH is not enabled.");
				// ?we will hope the user is getting it online before we have to use it?
			}
		}
		
	}

	/**
	 * @see LogsInterface#initLog(UniqueLogInterface)
	 * Initializes the Log Facility.
	 * It checks the types of logging enabled 
	 *  on the basis of LOG MODE.
	 * For example if only TextLog or XML log is enabled then
	 *  it initializes the LOG Facility with only those two enabled. 
	 * It also checks if any alternate name or path 
	 *  is specified for Text Log and XML log. 
	 *  
	 * Log Facility is still not taken care of.   
	 * 
	 */
	public void initLog(UniqueLogInterface logInfo) {
           
           String logFac      = (String)logInfo.getUniqueID();
           String textLogName = (String)logInfo.getTextLogName();
           String xmlLogName  = (String)logInfo.getXMLLogName();
           
           String request     =  null;
           long modes = logInfo.getLogModes();
           //If alternate TextLogName is not specified it is made an empty string
           if(textLogName==null)
              textLogName="";
           
           //If alternate XMLLogName is not specified it is made an empty string 
           if(xmlLogName==null)
              xmlLogName="";
           

           
           if(logFac!=null)
            {
            	// Checking whether Logging is enabled or not .
                if (modes!=0)
                 {
	                request = "INIT "+logFac+" ";
	               // If LOG MODE is specified as ALL then make the 
	               // request to the service here itself with all the parameters.
	               if (modes==127)
	               {
	                 request = request+"TEXTLOG "+textLogName+" XMLLOG "+xmlLogName+" TOOLLOG "+" CONSOLELOG ";	                 
	               }
	               else
	               {
	               	
	               	// Construct the request string to the service by checking which all 
	               	// logging modes are enabled.
	                if((modes & 1)==1)
	            	  request = request+" TOOLLOG ";
	               
	                if((modes & 8)==8)               
	                  request = request+" CONSOLELOG ";
	               
	                if((modes & 32)==32)
	                  request = request+" TEXTLOG "+textLogName;
	                  
	                if((modes & 64)==64)                   
	                  request = request+" XMLLOG "+xmlLogName;        
	                }
	                
	                if(overwrite) request = request +" OVERWRITE";	                 
	                
	                if(capXML) request = request +" CAPXML";	
	                
	                if(truncate) request = request +" TRUNCATE "+ truncateLength;
	                
  	          	    Log.info("SAFSLOGS interface sending request: "+ request);
	                STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	                
	                // set loglevel if different than INFO
	                if(result.rc==STAFResult.Ok && logInfo.getLogLevel()!= null){
	                	if(!AbstractSAFSLoggingService.SLS_SERVICE_PARM_INFO.equalsIgnoreCase(logInfo.getLogLevel())){
	                		setLogLevel(new UniqueStringLogLevelInfo(logFac, logInfo.getLogLevel()));
	                	}
	                }
                }
               else 
                {
                  throw new IllegalArgumentException(
					"Logging is disabled.");
                }
            }         
           else
            {
        	  Log.debug("SAFSLOGS Log Facility needs to be specified .");
              System.out.println("sgsg"+logFac);
              System.out.println("Log Facility needs to be specified .");
            } 
            
        }

	/**
	 * @see LogsInterface#setLogLevel(UniqueLogLevelInterface)
	 *  Set's the Logging Level .
	 *   I am allowing an empty Log Level because the service does not
	 *	 give an error instead it returns the current log level .
	 * 	 But the intended set operation is not done.
	 *  .
	 */
	public void setLogLevel(UniqueLogLevelInterface logLevel) {
		
		String logFac  = (String)logLevel.getUniqueID();
		String myLogLevel = logLevel.getLogLevel();
		
		 
	    // I am allowing an empty Log Level because the service does not
		// give an error instead it returns the current log level .
		//   But the intended set operation is not done.
		  if(myLogLevel==null)
		    {
		      myLogLevel="";
		    }
		
		 if(logFac!=null)
           {
             	String request = "LOGLEVEL"+" "+logFac+" "+myLogLevel;
		        STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
           }
          else 
            System.out.println("Log Facility needs to be specified."); 		    		
	}

	/**
	 * @see LogsInterface#logMessage(UniqueMessageInterface)
	 * Log's a message in the Log Facility.
	 * If Log Facility or the Message to be
	 * logged is not specified then the request is not made.
	 * If Description is null it is not included in the 
	 * request to the SAFSLOGGING service.
	 */
	public void logMessage(UniqueMessageInterface message) {

        String logFac  = (String)message.getUniqueID();
                
        
        String msg     = staf.lentagValue(message.getLogMessage());
        
        String desc=null;
	    String description    = message.getLogMessageDescription();
		if((description!=null)&&(description.length()>0))
		{
			desc    = staf.lentagValue(message.getLogMessageDescription());
		}
			
		
		long   msgType = message.getLogMessageType();
	
		String request=null;
      
        // If Log Facility or the Message to be logged is not specified then the request is not made.
        if((logFac!=null)&&(msg!=null))
            {
        		if(desc==null)
        	       {
					 // If Description is null it is not included in the request to the SAFSLOGGING service.	        
        	         request = "LOGMESSAGE"+" "+logFac+" "+"Message"+" "+msg+" "+"MSGTYPE"+" "+msgType;           
        	       }
        	     else 
        	       {
        	         request = "LOGMESSAGE"+" "+logFac+" "+"Message"+" "+msg+" "+"Description"+" "+desc+" "+"MSGTYPE"+" "+msgType;
        	       }   
        	
        	     
        	     STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
        	     
           	}
        else 
            { 
               System.out.println("Log Facility / Message option is required for logging a message.");	
               
            }
         
	}


	/**
	 * Log a status report for the particular StatusInterface object provided.
	 * @param log -- String name of an open LogFacility in SAFSLOGS.
	 * @param info -- StatusInterface containing status counts.
	 * @param infoID -- Name or ID to give to the status information in the log.
	 *                  This is usually used to show the subject of the status info.
	 *                  For example, "Regression Test", "TestCase 123456", etc..
	 *                  DEFAULT_STATUSINFO_ID used if null or zero-length.
	 **/
	public void logStatusInfo(UniqueIDInterface log, StatusInterface info, String infoID){

		String locID = DEFAULT_STATUSINFO_ID;
		String logResult = null;
		UniqueStringMessageInfo facname = new UniqueStringMessageInfo((String)log.getUniqueID());
		
		if ((infoID != null)&&(infoID.length()>0))
		    locID = infoID;

		facname.setLogMessage(locID);
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_START);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getTotalRecords()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_RECORDS);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getSkippedRecords()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_SKIPPED);
		logMessage(facname);

		facname.setLogMessage(" ");
		facname.setLogMessageType(AbstractLogFacility.GENERIC_MESSAGE);
		logMessage(facname);

		long tests = info.getTestFailures();
		tests += info.getTestPasses();
		tests += info.getTestWarnings();

		facname.setLogMessage(String.valueOf(tests).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_TESTS);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getTestFailures()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_TEST_FAILURES);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getTestWarnings()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_TEST_WARNINGS);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getTestPasses()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_TEST_PASSES);
		logMessage(facname);


		facname.setLogMessage(" ");
		facname.setLogMessageType(AbstractLogFacility.GENERIC_MESSAGE);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getGeneralFailures()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_GENERAL_FAILURES);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getGeneralWarnings()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_GENERAL_WARNINGS);
		logMessage(facname);

		facname.setLogMessage(String.valueOf(info.getIOFailures()).trim());
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_IO_FAILURES);
		logMessage(facname);


		facname.setLogMessage(locID);
		facname.setLogMessageType(AbstractLogFacility.STATUS_REPORT_END);
		logMessage(facname);
	}


	/**
	 * @see LogsInterface#suspendLog(UniqueIDInterface)
	 * This ensure that the specified log is suspended.
	 */
	public void suspendLog(UniqueIDInterface log) {
	
	      String logFac  = (String)log.getUniqueID();
          
          if(logFac!=null)
           {
             	String	request = "SUSPENDLOG"+" "+logFac;
		        STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
           }
          else 
             System.out.println("Log Facility needs to be specified."); 	           
	}

	/**
	 * @see LogsInterface#suspendAllLogs()
	 * This ensures that all the Logs are supended.
	 */
	public void suspendAllLogs() {
	    
	    String request = "SUSPENDLOG ALL"; 
		STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	}

	/**
	 * @see LogsInterface#resumeLog(UniqueIDInterface)
	 * This ensures that the specified Log is resumed again.
	 */
	public void resumeLog(UniqueIDInterface log) {
         
         String logFac  = (String)log.getUniqueID();
         if(logFac!=null)
           {
             	String request = "RESUMELOG " + logFac;
		        STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
           }
          else 
            System.out.println("Log Facility needs to be specified.");            
	}

	/**
	 * @see LogsInterface#resumeAllLogs()
	 * This ensures that all the previously suspended Logs are started again.
	 */
	public void resumeAllLogs() {

        String request = "RESUMELOG ALL"; 		

		STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	}

	/**
	 * Enables truncation of logged messages to the numchars length provided. 
	 */
	public void truncate(int numchars) {
	    String request = "TRUNCATE " + numchars;
   	    Log.info("SAFSLOGS interface sending request: "+ request);
    	STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	}

	/**
	 * Enables or disables the truncation of logged messages. 
	 */
	public void truncate(boolean enabled) {
	    String request = enabled ? "TRUNCATE ON" : "TRUNCATE OFF";
   	    Log.info("SAFSLOGS interface sending request: "+ request);
    	STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
    	truncate = enabled;
	}

	/**
	 * This closes the specified Log . 
	 * @see LogsInterface#closeLog(UniqueIDInterface)
	 */
	public void closeLog(UniqueIDInterface log) {

        String logFac = (String)log.getUniqueID();
		
		if (logFac!=null) {
		    String request = "CLOSE " + logFac;
            if(capXML) request = request +" CAPXML";	                 
       	    Log.info("SAFSLOGS interface sending request: "+ request);
        	STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
          }
         else
           System.out.println("Log Facility needs to be specified");           
	}

	/**
     *This  makes sure that all the open logs are closed.
	 * @see LogsInterface#closeAllLogs()
	 */
	public void closeAllLogs() {
         
        String request = "CLOSE ALL"; 
        if(capXML) request = request +" CAPXML";	                 
   	    Log.info("SAFSLOGS interface sending request: "+ request);
	    STAFResult result = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	}

	/**
	 * This should probably make sure that all open logs are closed.
	 * This would allow us to recover from interrupted tests when logs are 
	 * left open and unprocessed.
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
		  closeAllLogs();
	}

	/** 
	 * Invoke all superclass finalization.
	 * @see DriverConfiguredSTAFInterfaceClass#finalize()
	 */
	protected void finalize() throws Throwable { super.finalize();}
}

