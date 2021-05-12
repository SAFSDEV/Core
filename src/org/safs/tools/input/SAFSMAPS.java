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
package org.safs.tools.input;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.input.EmbeddedInputService;
import org.safs.staf.service.map.EmbeddedMapService;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.UniqueStringID;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConfiguredSTAFInterfaceClass;
import org.safs.tools.stringutils.StringUtilities;

import com.ibm.staf.STAFResult;

/**
 * This concrete implementation interfaces clients to the SAFSMAPS service.
 * This class expects a DriverInterface object to provide access to all 
 * configuration information.
 * <p>
 * <ul><li><h4>ConfigureInterface Information</h4>
 * <p><pre>
 * [STAF]
 * ;NOSTAF=TRUE  will launch this service as an embedded services.
 * 
 * [SAFS_MAPS]
 * AUTOLAUNCH=TRUE
 * ;ITEM=org.safs.tools.input.SAFSMAPS
 * ;Service=SAFSMAPS
 * ;ServiceClass=org.safs.staf.service.SAFSAppMapService
 * ;ServiceClass=org.safs.staf.service.EmbeddedMapService
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
 * <dt>ITEM
 * <p><dd>
 * The full class name for an alternate MapsInterface class for SAFSMAPS.  
 * This parameter is only needed if an alternate/custom MapsInterface is used.
 * The class must be findable by the JVM and Class.forName functions.  
 * <p>
 * The default ITEM value is  org.safs.tools.input.SAFSMAPS
 * <p>
 * <dt>SERVICECLASS
 * <p><dd>
 * The full class name for an alternate service class for SAFSMAPS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * This setting, if provided, will cause any SERVICEJAR setting to be ignored.
 * <p>
 * The default SERVICECLASS value is  org.safs.staf.service.SAFSAppMapService
 * <p>
 * <dt>SERVICEJAR
 * <p><dd>
 * The full path and name for an alternate service JAR for SAFSMAPS.
 * This parameter is only needed if an alternate/custom service is used.
 * This class must be findable by the JVM.
 * If a value is specified for SERVICECLASS, then this setting is ignored.
 * <p>
 * The default SERVICEJAR value is  [safsroot]/lib/safsmaps.jar
 * <p>
 * <dt>SERVICE
 * <p><dd>
 * The service name for an alternate service instead of "SAFSMAPS".
 * This parameter is only needed if an alternate/custom service is used.
 * Note: All of the standard SAFS Framework tools currently expect the default 
 * "SAFSMAPS" service name.
 * <p>
 * <dt>OPTIONS
 * <p><dd>
 * Any additional PARMS to be sent to the service upon initialization.
 * We already handle sending the DIR parameter with the path obtained from 
 * the Driver.  Any other options needed for service initialization should be 
 * specified here.  There typically will be none.
 * </dl>
 * @author Carl Nagle  DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author JunwuMa MAY 15, 2009 Added support for SAFSVARS, make it work with STAF2 or STAF3, loading different 
 *                              version of SAFSAppMapService according to STAF's version.
 * @author Carl Nagle JUL 16, 2014 Added NOSTAF support for the Embedded Service.
 **/
public class SAFSMAPS extends DriverConfiguredSTAFInterfaceClass 
                      implements MapsInterface {

	/** "org.safs.staf.service.map.SAFSAppMapService" */
	protected static final String DEFAULT_SAFSMAPS_CLASS = "org.safs.staf.service.map.SAFSAppMapService";
	
	/** "org.safs.staf.service.map.SAFSAppMapService3" */
    protected static final String DEFAULT_SAFSMAPS_3_CLASS = "org.safs.staf.service.map.SAFSAppMapService3";	

	/** "org.safs.staf.service.map.EmbeddedMapService" */
    protected static final String DEFAULT_SAFSMAPS_EMBEDDED_CLASS = "org.safs.staf.service.map.EmbeddedMapService";	

	/**************************************************************
	 * "safsmaps.jar" 
	 */
	protected static final String DEFAULT_SAFSMAPS_JAR   = "safsmaps.jar";

	/**************************************************************
	 * Stores classname or JAR file fullpath for STAF service initialization.
	 */
	protected String classpath = "";

	/**************************************************************
	 * Constructor for SAFSMAPS.
	 * The object cannot do much of anything at all until the DriverInterface 
	 * and ConfigureInterface have been received via the launchInterface function.
	 */
	public SAFSMAPS() {
		super();
		servicename = STAFHelper.SAFS_APPMAP_SERVICE;
	}

	private String getDefaultLoadingClass() {
		int stafVersion = staf.getSTAFVersion();
		if (stafVersion == 3)
			return DEFAULT_SAFSMAPS_3_CLASS;
		else if (stafVersion == 2)
			return DEFAULT_SAFSMAPS_CLASS;
		else {
			Log.info("NOT supported STAF version: " + stafVersion);
			return null;
		}	
	}

	private void startEmbeddedService(String mapdir){
    	System.out.println("config.EmbeddedMapService bypassing STAF Service creation for "+ servicename);
    	classpath = EmbeddedMapService.class.getName();
    	EmbeddedMapService eserv = new EmbeddedMapService();
    	eserv.init(new InfoInterface.InitInfo(servicename, "DIR "+ mapdir));
	}
	
	
	/**************************************************************
	 * Expects a DriverInterface for initialization.
	 * The superclass handles generic initialization and then we provide 
	 * SAFSMAPS-specific initialization.
	 * <p>
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
		
		super.launchInterface(configInfo);

		// see if SAFSMAPS is already running
		// launch it if our config says AUTOLAUNCH=TRUE and it is not running
		// otherwise don't AUTOLAUNCH it.
		if( ! staf.isServiceAvailable(servicename)){

			System.out.println(servicename +" is not running. Evaluating AUTOLAUNCH...");
			
			//check to see if AUTOLAUNCH was passed as a Driver command-line option
			String setting = System.getProperty(DriverConstant.PROPERTY_SAFS_DRIVER_AUTOLAUNCH, "");

			// if not
			if (setting.length()==0){

				//check to see if AUTOLAUNCH of SAFSMAPS exists in ConfigureInterface
				setting = config.getNamedValue(DriverConstant.SECTION_SAFS_MAPS, 
				                "AUTOLAUNCH");
				if (setting==null) setting = "";
			}
			boolean launch = StringUtilities.convertBool(setting);

		    String mapdir  = driver.getDatapoolDir();
		    String tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_MAPS, "SERVICE");
		    Log.debug("config.SERVICE="+tempstr);				                 
		    servicename = (tempstr==null) ? STAFHelper.SAFS_APPMAP_SERVICE : tempstr;
		    
			// launch it if we dare!
			if (launch && !STAFHelper.no_staf_handles){
			    
			    String options = null;			    

			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_MAPS, 
			         		      "SERVICECLASS");
				Log.debug("config.ServiceClass="+tempstr);				                 
				if (tempstr == null) {
					tempstr = config.getNamedValue(DriverConstant.SECTION_SAFS_MAPS, 
			         		      "SERVICEJAR");
     			    Log.debug("config.ServiceJAR="+tempstr);
				}
			    classpath = (tempstr==null) ? getDefaultLoadingClass() : tempstr;
			    if (classpath == null)
					throw new IllegalArgumentException(
							"STAF version is NOT supported by default SAFSMAPS service");
			    
			    // do normal stuff if NOT embedded
			    if(! classpath.equalsIgnoreCase(DEFAULT_SAFSMAPS_EMBEDDED_CLASS)){
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_MAPS, 
				                      "OPTIONS");
					Log.debug("config.OPTIONS="+tempstr);				                 
				    options   = (tempstr==null) ? "" : tempstr;
				    options   = configureJSTAFServiceEmbeddedJVMOption(options);
			                 
				    // launch SAFSMAPS
					staf.addService(machine, servicename, classpath, mapdir, options);
				    waitForServiceStartCompletion(10);
			    }else{
			    	startEmbeddedService(mapdir);
				    waitForServiceStartCompletion(5);
			    }
			}else if(STAFHelper.no_staf_handles){
				startEmbeddedService(mapdir);
			    waitForServiceStartCompletion(5);
			}
			// not supposed to autolaunch
			else{
				System.out.println(servicename +" AUTOLAUNCH is not enabled.");
				// ?we will hope the user is getting it online before we have to use it?
			}
		}			
	}

	/**************************************************************
	 * Open a new App Map for reference.
	 * Always sets the opened map as the new default map.
	 * The UniqueMapInterface is expected to simply have the mapid, 
	 * and the <b>String</b> map filename or fullpath.
	 * <p>
	 * @see MapsInterface#openMap(UniqueMapInterface)
	 */
	public void openMap(UniqueMapInterface map) {
		
		String id = (String)map.getUniqueID();
		if (id==null) id="";

		String info = (String) map.getMapPath(driver);
		if (info==null) info = (String) map.getMapInfo();
		if (info==null) info="";
		
		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
	                                    "OPEN " + staf.lentagValue(id)   +
		                               " FILE "+ staf.lentagValue(info) +
		                               " DEFAULTMAP");
	}

	/**************************************************************
	 * Set a previously opened map to be the default map.
	 * Most clients will probably never do this.
	 * @see MapsInterface#setDefaultMap(UniqueIDInterface)
	 */
	public void setDefaultMap(UniqueIDInterface map) {

		String id = (String)map.getUniqueID();
		if (id==null) id="";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
                                        servicename, 
		                               "DEFAULTMAP "+ id);
	}

	/**************************************************************
	 * Get the ID of the current default map.
	 * @see MapsInterface#getDefaultMap()
	 */
	public UniqueIDInterface getDefaultMap() {

		String id = "";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
                                        servicename, 
		                               "DEFAULTMAP");

		if(result.rc==0) id = result.result;
		return new UniqueStringID(id);
	}

	/**************************************************************
	 * Set a specific 'section' of a Map to be the default section.
	 * The default section is normally 'ApplicationConstants'.  
	 * The user can change this if desired.
	 * @see MapsInterface#setDefaultMapSection(UniqueSectionInterface)
	 */
	public void setDefaultMapSection(UniqueSectionInterface section) {

		String id = (String)section.getUniqueID();
		if (id==null) id="";

		String sectionID = section.getSectionName();
		if (sectionID==null) sectionID="";

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
		                                "DEFAULTMAPSECTION "+ id +
		                               " SECTION "+ sectionID);
	}

	/**************************************************************
	 * Get the current maps default map section.
	 * The default section is normally 'ApplicationConstants'.  
	 * The user can change this if desired.
	 * @see #setDefaultMapSection(UniqueSectionInterface)
	 */
	public UniqueSectionInterface getDefaultMapSection() {

		UniqueIDInterface idi = getDefaultMap();
		String id = (String) idi.getUniqueID();

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, servicename, 
		                                "DEFAULTMAPSECTION");

		String section = "";
		if(result.rc==0) section = result.result;
		return new UniqueStringItemInfo(id, section, "");		
	}

	/**************************************************************
	 * Retrieve the value of an item from an App Map.
	 * @see MapsInterface#getMapItem(UniqueItemInterface)
	 */
	public String getMapItem(UniqueItemInterface item) {

		String value = "";
		String commandID =  "GETITEM ";
		String sectionID = " SECTION ";
		String itemID    = " ITEM ";
		
		// may possibly be blank or null
		String id = (String)item.getUniqueID();
		if (id==null) id="";
			
		
		// may possibly be blank or null
		String section = item.getSectionName();
		if ((section==null)||(section.length()==0)) {
			section   = "";
			sectionID = "";}
		
		// should never be blank or null :)
		String itemName    = item.getItemName();
		if (itemName==null) itemName ="";
		
		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
		                                 commandID + staf.lentagValue(id) +
		                                 sectionID + staf.lentagValue(section) +
		                                 itemID    + staf.lentagValue(itemName));

		if (result.rc==0) value=result.result;
		return value;
	}


	/**************************************************************
	 * NOT YET SUPPORTED BY SAFSMAPS service.  Currently calls 'reset()'.
	 * @see MapsInterface#clearCache(UniqueIDInterface)
	 */
	public void clearCache(UniqueIDInterface map) {
		
		// NOT YET SUPPORTED BY SAFSMAPS service
		reset();
	}

	/**************************************************************
	 * Close a previously opened App Map.
	 * @see MapsInterface#closeMap(UniqueIDInterface)
	 */
	public void closeMap(UniqueIDInterface map) {

		String id = (String)map.getUniqueID();
		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
		                                "CLOSE "+ staf.lentagValue(id) );
	}

	/**************************************************************
	 * Clear all App Map caches from the service.
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {

		STAFResult result = staf.submit2ForFormatUnchangedService(machine, 
		                                 servicename, 
		                                "CLEARCACHE");
	}
}

