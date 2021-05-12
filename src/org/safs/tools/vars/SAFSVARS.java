/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.tools.vars;

import org.safs.Log;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.input.EmbeddedInputService;
import org.safs.staf.service.map.EmbeddedMapService;
import org.safs.staf.service.var.AbstractSAFSVariableService;
import org.safs.staf.service.var.EmbeddedVariableService;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.STAFHelper;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.expression.*;
import org.safs.tools.stringutils.StringUtilities;

import java.io.File;
import com.ibm.staf.STAFResult;

/**
 * This concrete implementation interfaces clients to the SAFSVARS service.
 * This class expects a DriverInterface object to provide access to all 
 * configuration information.
 * <p>
 * This class can also be configured to run Embedded in the same process space 
 * as the Driver.<br> 
 * To do this, configure ServiceClass=org.safs.staf.service.var.EmbeddedVariableService.
 * <p>
 * When using the EmbeddedVariableService, the class can continue to use the STAF VAR 
 * service for the storage of variable values.  It can also be configured to store 
 * variable values internally instead.<br>
 * To do this, configure EmbedVars=True.
 * <p>
 * <ul><li><h4>ConfigureInterface Information</h4>
 * <p><pre>
 * [STAF]
 * ;NOSTAF=TRUE  will launch this service as an embedded services if AUTOLAUNCH=TRUE.
 * 
 * [SAFS_VARS]
 * AUTOLAUNCH=TRUE
 * ;ITEM=org.safs.tools.vars.SAFSVARS
 * ;Service=SAFSVARS
 * ;ServiceClass=org.safs.staf.service.var.SAFSVariableService
 * ;ServiceClass=org.safs.staf.service.var.EmbeddedVariableService
 * ;OPTIONS=
 * ;EmbedVars=TRUE
 * </pre><br>
 * <ul>Note those items commented with semicolons are only needed when using alternate values.</ul>
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
 * <dt>ITEM
 * <p><dd>
 * The full class name for an alternate VarsInterface class for SAFSVARS.  
 * This parameter is only needed if an alternate/custom VarsInterface is used.
 * The class must be findable by the JVM and Class.forName functions.  
 * <p>
 * The default ITEM value is  org.safs.tools.vars.SAFSVARS
 * <p>
 * <dt>SERVICECLASS
 * <p><dd>
 * The full class name for an alternate service class for SAFSVARS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * This setting, if provided, will cause any SERVICEJAR setting to be ignored.
 * <p>
 * org.safs.staf.service.var.EmbeddedVariableService is one such class. 
 * This class will embed the SAFSVARS service inside the Driver process and 
 * bypass the initialization of any STAF SAFSVARS service. Combined with the 
 * EmbedVars setting, this will make all variables inaccessible to other tools  
 * and processes--effectively eliminating the use of STAF altogether.
 * <p>
 * The default SERVICECLASS value is  org.safs.staf.service.SAFSVariableService
 * <p>
 * <dt>SERVICEJAR
 * <p><dd>
 * The full path and name for an alternate service JAR for SAFSVARS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * If a value is specified for SERVICECLASS, then this setting is ignored.
 * <p>
 * The default SERVICEJAR value is  [safsroot]/lib/safsvars.jar
 * <p>
 * <dt>SERVICE
 * <p><dd>
 * The service name for an alternate service instead of "SAFSVARS".
 * This parameter is only needed if an alternate/custom service is used.
 * Note: All of the standard SAFS Framework tools currently expect the default 
 * "SAFSVARS" service name.
 * <p>
 * <dt>OPTIONS
 * <p><dd>
 * Any additional PARMS to be sent to the service upon initialization.  The default 
 * SAFSMAPS service name parameter is already provided.  Any additional options 
 * should be specified here.  There typically will be none.
 * <p>
 * <dt>EmbedVars
 * <p><dd>
 * Only valid when using the EmbeddedVariableService class. When set to TRUE this 
 * will cause the service to store all variable values internally and bypass the 
 * use of the STAF VAR service.  This effectively bypasses the use of 
 * STAF altogether for the SAFSVARS service.
 * </dl>
 * @author Carl Nagle  DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author JunwuMa MAY 15, 2009 Added support for SAFSVARS, make it work with STAF2 or STAF3, loading different 
 *                              version of SAFSVariableService according to STAF's version.
 * @author Carl Nagle  JUL 03, 2014 Commencing support for embedded non-STAF services.
 * @author Carl Nagle  JUL 11, 2014 Enabling EmbedVars support for the EmbeddedVariableService.
 * @author Carl Nagle  JUL 16, 2014 Added NOSTAF support for the Embedded Service.
 **/
public class SAFSVARS extends DriverConfiguredSTAFInterfaceClass 
	                  implements VarsInterface {

     /** "org.safs.staf.service.var.SAFSVariableService" */
     protected static final String DEFAULT_SAFSVARS_CLASS = "org.safs.staf.service.var.SAFSVariableService";
     
     /** "org.safs.staf.service.var.SAFSVariableService3" */
     protected static final String DEFAULT_SAFSVARS_3_CLASS = "org.safs.staf.service.var.SAFSVariableService3";

     /** "org.safs.staf.service.var.EmbeddedVariableService" */
     protected static final String DEFAULT_SAFSVARS_EMBEDDED_CLASS = "org.safs.staf.service.var.EmbeddedVariableService";
     
     /**************************************************************
	 * "safsvars.jar"
	 */
     protected static final String DEFAULT_SAFSVARS_JAR = "safsvars.jar";
     
	/**************************************************************
	 * Stores classname or JAR file fullpath for STAF service initialization.
	 */
	protected String classpath = "";
	
	protected SafsExpression se ;
	
	/**
	 * Constructor for SAFSVARS
	 */
	public SAFSVARS() {
		super();
		servicename = STAFHelper.SAFS_VARIABLE_SERVICE;		
		se = new SafsExpression(this) ;
	}
	
	private String getDefaultLoadingClass() {
		int stafVersion = staf.getSTAFVersion();
		if (stafVersion == 3)
			return DEFAULT_SAFSVARS_3_CLASS;
		else if (stafVersion == 2)
			return DEFAULT_SAFSVARS_CLASS;
		else {
			Log.info("NOT supported STAF version: " + stafVersion);
			return null;
		}	
	}

	private void startEmbeddedService(String params, boolean embedVARS){
    	System.out.println("config.EmbeddedVariableService bypassing STAF Service creation for "+ servicename);
    	classpath = EmbeddedVariableService.class.getName();
    	EmbeddedVariableService eserv = new EmbeddedVariableService();
    	eserv.setEmbedVars(embedVARS);
    	eserv.init(new InfoInterface.InitInfo(servicename, params));
	}
	
	
	/**
	 * Expects a DriverInterface for initialization.
	 * The superclass handles generic initialization and then we provide 
	 * SAFSVARS-specific initialization.
	 * <p>
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {

		super.launchInterface(configInfo);
		
		// see if SAFSVARS is already running
		
		// launch it if our config says AUTOLAUNCH=TRUE and it is not running
		// otherwise don't AUTOLAUNCH it.
		if( ! staf.isServiceAvailable(servicename)){

			System.out.println(servicename +" is not running. Evaluating AUTOLAUNCH...");
			
			//check to see if AUTOLAUNCH was passed as a Driver command-line option
			String setting = System.getProperty(DriverConstant.PROPERTY_SAFS_DRIVER_AUTOLAUNCH, "");

			// if not
			if (setting.length()==0){

				//check to see if AUTOLAUNCH of SAFSVARS exists in ConfigureInterface
				setting = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, 
				                "AUTOLAUNCH");
				if (setting==null) setting = "";
			}
			boolean launch = StringUtilities.convertBool(setting);

		    String tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, "SERVICE");
		    Log.debug("config.SERVICE="+tempstr);				                 
		    servicename = (tempstr==null) ? STAFHelper.SAFS_VARIABLE_SERVICE : tempstr;
			
			// launch it if we dare!
			if (launch && !STAFHelper.no_staf_handles){
			    
			    String options = null;			    
	   	        
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, 
			         		      "SERVICECLASS");
				Log.debug("config.ServiceClass="+tempstr);				                 
				if (tempstr == null) {
					tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, 
			         		      "SERVICEJAR");
     			    Log.debug("config.ServiceJAR="+tempstr);
				}
			    classpath = (tempstr==null) ? getDefaultLoadingClass() : tempstr;	
			    if (classpath == null)
					throw new IllegalArgumentException(
							"STAF version is NOT supported by default SAFSVARS service");
			    
			    // do normal stuff if NOT embedded
			    if(! classpath.equalsIgnoreCase(DEFAULT_SAFSVARS_EMBEDDED_CLASS)){
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, 
				                      "OPTIONS");
					Log.debug("config.Options="+tempstr);				                 
				    options   = (tempstr==null) ? "" : tempstr;
				    options   = configureJSTAFServiceEmbeddedJVMOption(options);
	
		   	        //Change it soon
				    tempstr  = null;
					String mapService = (tempstr==null)? "":tempstr;
	
					// launch SAFSVARS
					staf.addServiceSAFSVARS(machine, servicename, classpath, mapService, options);
					waitForServiceStartCompletion(5);
			    }else{
			    	Log.info("config.EmbeddedVariableService bypassing STAF Service creation for "+ servicename);
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_VARS, 
		                      "EMBEDVARS");
				    boolean val = tempstr == null ? false : StringUtilities.convertBool(tempstr);
				    startEmbeddedService("", val);
					waitForServiceStartCompletion(5);
			    }			
			}else if (STAFHelper.no_staf_handles){
				startEmbeddedService("", true);
				waitForServiceStartCompletion(5);
			}
			// not supposed to autolaunch
			else{
				System.out.println(servicename +" AUTOLAUNCH is not enabled.");
				// ?we will hope the user is getting it online before we have to use it?
			}

			// we want to store the following into safsvars, autolaunch or not
			DriverInterface di = (DriverInterface) configInfo ;
	
			String safsbenchdirectory = di.getBenchDir() + File.separatorChar ;
			String safsdatapooldirectory = di.getDatapoolDir() + File.separatorChar ;
			String safsdifdirectory = di.getDifDir() + File.separatorChar ;
			String safslogsdirectory = di.getLogsDir() + File.separatorChar ;
			String safsprojectdirectory = di.getProjectRootDir() + File.separatorChar ;
			String safstestdirectory = di.getTestDir() + File.separatorChar ;
	
			Log.info("  SAFSVARS launchInterface di.getBenchDir(): " + safsbenchdirectory) ;
			Log.info("  SAFSVARS launchInterface di.getDatapoolDir(): " + safsdatapooldirectory) ;
			Log.info("  SAFSVARS launchInterface di.getDifDir(): " + safsdifdirectory) ;
			Log.info("  SAFSVARS launchInterface di.getLogsDir(): " + safslogsdirectory) ;
			Log.info("  SAFSVARS launchInterface di.getProjectRootDir(): " + safsprojectdirectory) ;
			Log.info("  SAFSVARS launchInterface di.getTestDir(): " + safstestdirectory) ;
	
			this.setValue("safsbenchdirectory",safsbenchdirectory) ;
			this.setValue("safsdatapooldirectory",safsdatapooldirectory) ;
			this.setValue("safsdifdirectory",safsdifdirectory) ;
			this.setValue("safslogsdirectory",safslogsdirectory) ;
			this.setValue("safsprojectdirectory",safsprojectdirectory) ;
			this.setValue("safstestdirectory",safstestdirectory) ;
		
		}

	}

	/**
	 * @see VarsInterface#resolveExpressions(String, String)
	 */
	public String resolveExpressions(String record, String sep) {

		if(record!=null&&sep!=null)
		{
			String _ddv_record = StringUtilities.findAndReplace(record, AbstractSAFSVariableService.SVS_CARET, AbstractSAFSVariableService.SVS_FALSE_CARET);
			String NOEXPRESSIONS = (driver.isExpressionsEnabled()) ? "":" NOEXPRESSIONS";
			
		    String     request = "RESOLVE "+staf.lentagValue(_ddv_record)+" SEPARATOR "+staf.lentagValue(sep) + NOEXPRESSIONS;
		    STAFResult result  = staf.submit2ForFormatUnchangedService(machine,servicename,request);		    
		    if (result.rc==STAFResult.Ok) 
		        // remove leading "0:"
		    	return result.result.substring(2);
            //The message below already appears in Debug Log
		    //System.out.println("SAFSVARS ignoring RESOLVE problem: "+ String.valueOf(result.rc) +":"+ result.result);  
		    return record;
        }
        else
        { 
           System.out.println("SAFSVARS ignoring RESOLVE RECORD or SEPARATOR set to NULL.");  
           return record; 
        }
	}

	/**
	 * @see SimpleVarsInterface#setValue(String, String)
	 */
	public String setValue(String var, String value) {
		
		if(var!=null&&value!=null)
		 {
		   String     request = "SET "+staf.lentagValue(var)+" Value "+staf.lentagValue(value);
		   STAFResult result  = staf.submit2ForFormatUnchangedService(machine,servicename,request);
		   return value;
         }
        else
        { 
         System.out.println("The value of the variable / value cannot be Null");  
         return value; 
        }
	    
	}

	/**
	 * @see SimpleVarsInterface#getValue(String)
	 */
	public String getValue(String var) {
		
		if(var!=null)
		 {
		 	
		   String     request = "GET "+staf.lentagValue(var);
		   STAFResult result  = staf.submit2ForFormatUnchangedService(machine,servicename,request);
		   if(result.rc==0)
		    {
		      String value=result.result;
		      return value;
		    }
		   else 
		     return "";
		 }
		else 
		 System.out.println("The value of the variable cannot be Null");  
		 return "";
	}

	/**
	 * @see VarsInterface#deleteVariable(String)
	 */
	public void deleteVariable(String var) {
		if(var!=null)
		 {
		String     request = "DELETE "+staf.lentagValue(var);
		STAFResult result  = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	     }
	    else 
		 System.out.println("The value of the variable cannot be Null"); 
	}

	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
		String     request = "RESET";
		STAFResult result  = staf.submit2ForFormatUnchangedService(machine,servicename,request);
	}


	/** 
	 * Invoke all superclass finalization.
	 * @see DriverConfiguredSTAFInterfaceClass#finalize()
	 */
	protected void finalize() throws Throwable { super.finalize();}
}

