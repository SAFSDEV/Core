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
package org.safs.ios;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import org.safs.DriverCommand;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.StatusCodes;
import org.safs.text.CaseInsensitiveHashtable;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.drivers.DriverConstant;

/**
 * SAFS/IOS supported Driver Commands.
 * 
 * @author Carl Nagle
 */
public class IDriverCommand extends DriverCommand {

	  public static final String CALLSCRIPT                    = "CallScript";
	  public static final String CLOSEAPPLICATION              = "CloseApplication";
	  public static final String LAUNCHAPPLICATION             = "LaunchApplication";
	  public static final String ONGUIEXISTSGOTOBLOCKID        = "OnGuiExistsGotoBlockID";
	  public static final String ONGUINOTEXISTGOTOBLOCKID      = "OnGuiNotExistGotoBlockID";
	
	  String command = null;
	  Hashtable appids = new CaseInsensitiveHashtable();
	  String JSCRIPTS_CUSTOM_SUBDIR = "custom";
	  String JSCRIPTS_DRIVERCOMMANDS_SUBDIR = "DriverCommands";
	  String JSCRIPTS_ONGUIEXISTS_SCRIPT = "onguiexists.js";
	  
	public IDriverCommand() {
		super();
	}
	
	public void process(){
		testRecordData.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
		try {
			params = interpretFields();
			command = getTestRecordData().getCommand();
			Log.debug("IDC attempting to process "+ command);
			if(command.equalsIgnoreCase(LAUNCHAPPLICATION)) {
				doLaunchApplication();
			}else if(command.equalsIgnoreCase(CLOSEAPPLICATION)){
				doCloseApplication();				
			}else if(command.equalsIgnoreCase(ONGUIEXISTSGOTOBLOCKID)){
				doGUIBranching(true);				
			}else if(command.equalsIgnoreCase(ONGUINOTEXISTGOTOBLOCKID)){
				doGUIBranching(false);				
			}else if(command.equalsIgnoreCase(CALLSCRIPT)){
				doCallScript();				
			}
		} catch (SAFSObjectRecognitionException e) {
			Log.debug("IDC recognition string missing or invalid for: "+ e.getMessage());
			issueParameterValueFailure(e.getMessage());
		} catch (InstrumentsScriptExecutionException e){
			Log.debug("IDC "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueUnknownErrorFailure(e.getMessage());
		} catch (SAFSObjectNotFoundException e){
			Log.debug("IDC "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
									   e.getMessage() +" was not found.", e.getMessage()));
		} catch (Throwable e) {
			Log.debug("IDC.process error: "+ e.getClass().getSimpleName()+": "+ e.getMessage());
		}
		if(! (testRecordData.getStatusCode()==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
			setRecordProcessed(true);
		}
	}

	/**
	 * Support for OnGuiExistsGotoBlockID and OnGuiNotExistGotoBlockID
	 * <p>
	 * Appends an variables into trd.js with:
	 * <pre>
	 * var timeout=seconds;
	 * var blockId=blockname;
	 * </pre>
	 * @param seekexists true for OnGuiExists, false for OnGuiNotExist.
	 * @throws SAFSObjectRecognitionException if window or component not in app map.
	 * @throws InstrumentsScriptExecutionException if any error occurs in Instruments.
	 */
	protected void doGUIBranching(boolean seekexists) throws SAFSObjectRecognitionException,
	                                       InstrumentsScriptExecutionException{
		if(params.size() < 3) {
			Log.debug("IDC."+ command +" does NOT contain one or more required parameters: BlockID, WindowID, or ComponentID.");
			issueParameterCountFailure("BlockID, WindowID, ComponentID");
			return;
		}
		Iterator iparams = params.iterator();
		String winid = null;
		String compid = null;
		String blockid = null;
		int timeout = 15;
		
		blockid = (String) iparams.next();
		Log.info("IDC."+ command +" extracted blockID: "+ blockid);
		if(blockid==null||blockid.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for BlockID.");
			this.issueParameterValueFailure("BlockID");
			return;
		}
		winid = (String) iparams.next();
		Log.info("IDC."+ command +" extracted windowID: "+ winid);
		if(winid==null||winid.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for WindowID.");			
			this.issueParameterValueFailure("WindowID");
			return;
		}
		compid = (String) iparams.next();
		Log.info("IDC."+ command +" extracted compID: "+ compid);
		if(compid==null||compid.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for ComponentID.");			
			this.issueParameterValueFailure("ComponentID");
			return;
		}
		if(iparams.hasNext()){
			String stime = (String) iparams.next();
			try{
				timeout = Integer.parseInt(stime);
				if(timeout < 0) timeout = 0;
			}catch(NumberFormatException x){
				Log.debug("IDC."+ command +" ingnoring invalid timeout value. Using Default.");			
			}
		}
		// see if the recognition is stored in the map
    	String lookup = getAppMapItem( null, winid, winid);
		if(lookup==null||lookup.length()==0){
			throw new SAFSObjectRecognitionException("windowName:"+ winid);
		}
		testRecordData.setWindowGuiId(lookup);
		testRecordData.setWindowName(winid);
		
    	lookup = getAppMapItem( null, winid, compid);
		if(lookup==null||lookup.length()==0){
			throw new SAFSObjectRecognitionException("compName:"+ compid);
		}
		testRecordData.setCompGuiId(lookup);
		testRecordData.setCompName(compid);
		
		String trdAppend = "\nvar timeout="+ timeout +";\n";
		trdAppend +="\nvar blockId=\""+ blockid +"\";\n";
		Log.info(command +" using timeout: "+ timeout);
		Log.info(command +" using blockid: "+ blockid);
		
		IStatus stat = CFComponent.processIOSScript(JSCRIPTS_DRIVERCOMMANDS_SUBDIR, JSCRIPTS_ONGUIEXISTS_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		
   		if(stat.rc == IStatus.STAT_FAILURE){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}

		String msg = null;
		boolean match = (stat.rc == IStatus.STAT_BRANCH);
		
		//onguiexists
	    if (seekexists) {
	       if (match) {
	           //we were searching for gui, since it was found, attempt branch
	        	msg = GENStrings.convert(GENStrings.BRANCHING, 
	        			command +" attempting branch to "+ blockid +".", 
	        			command, blockid);
	        	msg += "  "+ GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
	        				compid +" found within timeout "+ String.valueOf(timeout), 
	        				compid, String.valueOf(timeout));
	        	//set statuscode and statusinfo fields so driver will know to branch
	        	testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
	        	testRecordData.setStatusInfo(blockid);
	        }
	        else {
	        	//we were searching for gui, since it wasn't found, don't branch
	        	msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
	        				command +" not branching to "+ blockid +".", 
	        				command, blockid);
	        	msg += "  "+ FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
	        				compid +" not found within timeout "+ String.valueOf(timeout), 
	        				compid, String.valueOf(timeout));
	        	testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	        }
	    }
	    //onguinotexist...
	    else {
	        if (match) {
	        	//we were searching for no gui, since it wasn't found, branch
	        	msg = GENStrings.convert(GENStrings.BRANCHING, 
	        				command +" attempting branch to "+ blockid +".", 
	        				command, blockid);
	        	msg += "  "+ GENStrings.convert(GENStrings.NOT_EXIST, 
	        				compid +" does not exist", 
	        				compid);
	        	//set statuscode and statusinfo fields so driver will know to branch
	        	testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
	        	testRecordData.setStatusInfo(blockid);	
	        }
	        else {
	        	//we were searching for no gui, since it was found, don't branch
	        	msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
	        				command +" not branching to "+ blockid +".", 
	        				command, blockid);
	        	msg += "  "+ GENStrings.convert(GENStrings.EXISTS, 
	        				compid +" exists", 
	        				compid);
	        	testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	        }
	    }
	    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);  
	}

	/**
	 * LaunchApplication implementation.<br>
	 * ApplicationID: Required. Stored for shutdown with CloseApplication. Can be ApplicationConstant.<br>
	 * ExecutablePath: Required. Path to *?.tracetemplate needed by Instruments. 
	 *     Can be relative to WorkDir, if provided. Can be ApplicationConstant.<br>
	 * WorkDir: Optional. Path to root Instruments Project directory. Can be ApplicationConstant.<br>
	 */
	protected void doLaunchApplication() {
		String appid, ucappid, apptemplate, ucapptemplate, workdir, device, msgtext;
		Iterator iparams = params.iterator();
		if(params.size() < 2) {
			Log.debug("IDC."+ command +" does NOT contain required parameters ApplicationID and TraceTemplate.");
			issueParameterCountFailure("ApplicationID, TraceTemplate");
			return;
		}
		appid = (String) iparams.next();
		Log.info("IDC."+ command +" extracted ApplicationID: "+ appid);
		if(appid==null||appid.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for ApplicationID.");
			this.issueParameterValueFailure("ApplicationID");
			return;
		}
		// see if the name is actually stored as an ApplicationConstant in the AppMap
    	String lookup = getAppMapItem( null, null, appid);
    	if((lookup!=null)&&(lookup.length()>0)){
    		appid = lookup;
    		Log.info("IDC."+ command +" retrieved ApplicationID CONSTANT value: "+ appid);
    	}
    	ucappid = appid.toUpperCase();

		apptemplate = (String) iparams.next();
		Log.info("IDC."+ command +" extracted TraceTemplate: "+ apptemplate);
		if(apptemplate==null||apptemplate.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for TraceTemplate.");
			this.issueParameterValueFailure("TraceTemplate");
			return;
		}		
		// see if the executable is actually stored as an ApplicationConstant in the AppMap
    	lookup = getAppMapItem( null, null, apptemplate);
    	if((lookup!=null)&&(lookup.length()>0)){
    		apptemplate = lookup;
    		Log.info("IDC."+ command +" retrieved TraceTemplate CONSTANT value: "+ apptemplate);
    	}
    	ucapptemplate = apptemplate.toUpperCase();
    	
    	// must be a valid file .tracetemplate
		if(! ucapptemplate.endsWith(".TRACETEMPLATE")){
			Log.debug("IDC."+ command +" invalid file extension for TraceTemplate.");
			msgtext = failedText.convert(FAILStrings.FILE_EXT_ERROR,"'"+apptemplate+"' does not contain expected file ext '.tracetemplate'", apptemplate, ".tracetemplate");
			this.issueParameterValueFailure(msgtext);
			return;
		}   
		workdir = null;
		File workfile = null;
		if(iparams.hasNext()){
			workdir = (String) iparams.next();
			Log.info("IDC."+ command +" extracted WorkDir: "+ workdir);
			lookup = getAppMapItem(null,null,workdir);
			if((lookup!=null)&&(lookup.length()>0)){
				workdir = lookup;
	    		Log.info("IDC."+ command +" retrieved WorkDir CONSTANT value: "+ workdir);
			}
			if(workdir.length()==0) {
				workdir = null;
			}else{
				workfile = new CaseInsensitiveFile(workdir).toFile();
				if((!workfile.exists())||(!workfile.isDirectory())){
					Log.debug("IDC."+ command +" IGNORING invalid Workdir:"+workdir);
					workdir = null;
					workfile = null;					
				}else{
					if (! workdir.endsWith(File.separator)) workdir += File.separator;
					Utilities.ROOT_INSTRUMENTS_PROJECT_DIR = workdir;
		    		Log.info("IDC."+ command +" using derived WorkDir value: "+ workdir);					
				}
			}
		}
		device = null;
		String arguments = null;
		String app = null;
		if(iparams.hasNext()){
			arguments = (String) iparams.next();
			Log.info("IDC."+ command +" extracted CMDLineParams: "+ arguments);
			if(arguments!=null) {
				if (arguments.length()==0) {
					arguments = null;
				}else{ 
					// parse "-d:device -app:application" arguments
					// it is an error if only one of these is specified.  
					// Both are required if one is present.
					String lcargs = arguments.toLowerCase();
					int idev = lcargs.indexOf("-d");
					int iapp = lcargs.indexOf("-app");
					if(idev < 0 && iapp < 0){
						Log.info("IDC."+ command +" ignoring invalid Instruments CMDLineParam settings: "+ arguments);
					}else if(idev < 0 || iapp < 0){
						Log.debug("IDC."+ command +" invalid Instruments CMDLineParam settings: "+ arguments);
						if(idev < 0) Log.debug("IDC."+ command +" requires -d:Device for IOS Instruments.");
						if(iapp < 0) Log.debug("IDC."+ command +" requires -app:AppName for IOS Instruments.");
						issueParameterValueFailure("CMDLineParams: -d:Device -app:AppName");
						return;
					}else{
						String seps = ":;, ";
						String thesep = null;
						boolean proper = true;
						// after -d must be a valid separator ":;, " (colon, semi-colon, comma, or space)
						try{
							thesep = arguments.substring(idev +2,idev +3);
							if(seps.indexOf(thesep)< 0) {
								proper = false;
							}
							thesep = arguments.substring(iapp +4,iapp +5);
							if(seps.indexOf(thesep)< 0) proper = false;
							if(!proper){
								Log.debug("IDC."+ command +" invalid Instruments CMDLineParam settings: "+ arguments);
								Log.debug("IDC."+ command +" requires BOTH -d and -app settings with valid separators (:;, )");
								issueParameterValueFailure("CMDLineParam="+arguments);
								return;
							}
							// if -d is first
							if(idev < iapp){
								device = arguments.substring(idev+3, iapp).trim();
								app = arguments.substring(iapp+5).trim();
							}else{ // -app is first
								app = arguments.substring(iapp+5, idev).trim();
								device = arguments.substring(idev+3).trim();
							}
							if(device.length()==0 || app.length()==0){
								Log.debug("IDC."+ command +" invalid Instruments CMDLineParam settings: "+ arguments);
								Log.debug("IDC."+ command +" requires BOTH -d:Device and -app:AppName settings for IOS Instruments.");
								issueParameterValueFailure("CMDLineParam: -d:Device -app:AppName");
								return;
							}
						}catch(Exception x){
							Log.debug("IDC."+ command +" "+ x.getClass().getSimpleName() +":"+x.getMessage());
							Log.debug("IDC."+ command +" invalid Instruments CMDLineParam settings: "+ arguments);
							Log.debug("IDC."+ command +" requires valid -d:Device and -app:AppName settings for IOS Instruments.");
							issueParameterValueFailure(x.getClass().getSimpleName()+":CMDLineParam="+arguments);
							return;
						}
					}
				}
			}
		}
		File appfile = null;
		appfile = new CaseInsensitiveFile(apptemplate).toFile();
		if((!appfile.exists())||(!appfile.isFile())){
			//try relative path from workdir
			Log.info("IDC."+ command +" "+ apptemplate +" not found as absolute path.  Trying relative to WorkDir...");
			if(workfile != null){
				//Log.debug("IDC."+ command +" trying appfile path relative to WorkDir...");
				appfile = new CaseInsensitiveFile(workfile, apptemplate).toFile();
				apptemplate = workfile.getAbsolutePath() + apptemplate;
			}
			if((!appfile.exists())||(!appfile.isFile())){
				Log.debug("IDC."+ command +" invalid parameter value for WorkDir/TraceTemplate.");
				msgtext = failedText.convert(FAILStrings.NOT_A_FILE,"'"+apptemplate+"' is not a file", apptemplate);
				this.issueParameterValueFailure(msgtext);
				return;				
			}
			
		}
		if (device != null) { //place them back in desired osascript order
		    Log.info("IDC."+ command +" using derived TraceTemplate path: "+ apptemplate +" on device: "+ device +" for app: "+ app);
			apptemplate = apptemplate +" -d "+ device +" -app "+ app;
		}else{
		    Log.info("IDC."+ command +" using derived TraceTemplate path: "+ apptemplate);
		}
		try{
			Utilities.prepareNewAutomationTest();
			// set the do-nothing startup script
			Utilities.prepareNextInstrumentsTest(Utilities.DEFAULT_JSSTARTUP_IMPORT);			
			Utilities.prepareNextTestRecordData(testRecordData);
			int tried = 0;
			boolean success = false;
			while(!success && tried++ < 2){
			    try{
				    ProcessCapture proc = Utilities.launchInstrumentsTest(apptemplate);
				    if(proc.getDataLineCount()> 0){
				    	String[] data = (String[]) proc.getData().toArray(new String[0]);
				    	for(int i=0;i<data.length;i++) Log.info(data[i]);
				    }
				    
				    // for IOS 5 we need to detect if we have to set the script
				    // we run AppleScript which will do this ONLY for IOS 5
				    proc = Utilities.runAppleScript(null, Utilities.PATH_SELECT_RECENT_SCRIPT_ASCRIPT, 
				    		                       Utilities.DEFAULT_SAFSRUNTIME_SCRIPT, true, 10);				    
				    if(proc.getDataLineCount()> 0){
				    	String[] data = (String[]) proc.getData().toArray(new String[0]);
				    	for(int i=0;i<data.length;i++) Log.info(data[i]);
				    }
				    
				    proc = Utilities.startInstrumentsTest();
				    if(proc.getDataLineCount()> 0){
				    	String[] data = (String[]) proc.getData().toArray(new String[0]);
				    	for(int i=0;i<data.length;i++) Log.info(data[i]);
				    }
				    Utilities.verifyInstrumentsRecording(null);
				    success = true;
			    }catch(InstrumentsStartScriptException x){
				    Utilities.killAllInstruments();
				    Utilities.killAllSimulators();
				    try{Thread.sleep(1000);}catch(Exception x2){}
			    }
		    }
			if(!success) throw new InstrumentsStartScriptException("IDC."+ command +" did not detect proper Instruments execution after "+ tried +" attempts.");

			Utilities.waitScriptComplete();			

			appids.put(ucappid, apptemplate);// could/should store process id in order to KILL it, if needed.
		    
			msgtext = genericText.convert(GENStrings.SUCCESS_3B, command +" "+ appid +" successful using "+ apptemplate, command, appid, apptemplate);
			Log.info(msgtext);
	        testRecordData.setStatusCode(StatusCodes.OK);
	        log.logMessage(testRecordData.getFac(), msgtext, GENERIC_MESSAGE);
		}
		catch(Exception x){
			String errmsg = "IDC."+ command +" "+ x.getClass().getSimpleName()+":"+ x.getMessage();
			Log.debug(errmsg);
			issueErrorPerformingAction(x.getClass().getSimpleName()+":"+ x.getMessage());
			Utilities.restoreInstrumentsScript();
		}
	}

	/**
	 * CloseApplication implementation.
	 * Currently does not use the ApplicationID.  It simply stops Instruments and any IOS Simulator 
	 * since these can only test one application at a time.
	 */
	protected void doCloseApplication() {
		String appid;
		Iterator iparams = params.iterator();
		if(params.size() < 1) {
			Log.debug("IDC."+ command +" does NOT contain required parameter ApplicationID.");
			issueParameterCountFailure("ApplicationID");
			return;
		}
		appid = (String) iparams.next();
		if (appids.contains(appid.toUpperCase())) appids.remove(appid.toUpperCase());
		Log.info("IDC."+ command +" ApplicationID: "+ appid);
	    //ignoring all possible parameters at this time and just stopping instruments and the simulator
		try {
			Utilities.stopInstrumentsTest();
			Utilities.restoreInstrumentsScript();
		} catch (Exception e) {
			String errmsg = "IDC."+ command +" "+ e.getClass().getSimpleName()+":"+ e.getMessage();
			Log.debug(errmsg, e);
			issueErrorPerformingAction(e.getClass().getSimpleName()+":"+ e.getMessage());
			return;
		}
		issueGenericSuccess(appid);
	}
	
	/**
	 * CallScript implementation.
	 * The scriptname will be sought as an ApplicationConstant in the current app map.
	 * If not there, we will use the scriptname "as-is".
	 * The final name/path of the script can be relative, or absolute.
	 * If a relative path, we will first look relative to the Project jscript subdirectory. 
	 * If still not found we will look relative to the jscript/custom subdirectory.
	 * The final scriptname can be with or without the ".js" extension.  We will append it if missing.
	 * <p>
	 * The routine also will accept and parse parameters that will be appended to the trd.js as available vars. 
	 * The routine will accept these in two formats:
	 * <p><ol>
	 * <li>varname=varvalue
	 * <li>varvalue
	 * </ol>
	 * <p>
	 * The 1st format will create a var entries in the JavaScript like:
	 * <p>
	 * var varname="varvalue"<br>
	 * var othername="othervalue"<br>
	 * <p>
	 * The 2nd format will create a var VARn entries in the JavaScript like:
	 * <p>
	 * var VAR1="varvalue"<br>
	 * var VAR2="othervalue"<br>
	 * etc...
	 */
	protected void doCallScript() {
		if(params.size() < 1) {
			Log.debug("IDC."+ command +" does NOT contain required parameter: ScriptName.");
			issueParameterCountFailure("ScriptName");
			return;
		}
		Iterator iparams = params.iterator();
		String scriptname = null;
		String aparam = null;
		int timeout = 15;
		
		scriptname = (String) iparams.next();
		Log.info("IDC."+ command +" extracted ScriptName: "+ scriptname);
		if(scriptname==null||scriptname.length()==0){
			Log.debug("IDC."+ command +" invalid parameter value for ScriptName.");
			this.issueParameterValueFailure("ScriptName");
			return;
		}
		String lookup = getAppMapItem(null,null,scriptname);
		if((lookup!=null)&&(lookup.length()>0)){
			scriptname = lookup;
    		Log.info("IDC."+ command +" retrieved ScriptName AppMap value: "+ scriptname);
		}
		if(! scriptname.toLowerCase().endsWith(".js")) scriptname += ".js";

		File workfile = new CaseInsensitiveFile(scriptname).toFile();
		boolean absolute = (workfile.exists())&&(workfile.isFile())&&(workfile.isAbsolute());					
		if(! absolute){
    		Log.info("IDC."+ command +" ScriptName  is NOT absolute.  Trying JSCRIPTS DIR for "+ scriptname);
			workfile = new CaseInsensitiveFile(Utilities.ROOT_JSCRIPTS_DIR + scriptname).toFile();
			absolute = (workfile.exists())&&(workfile.isFile())&&(workfile.isAbsolute());
		}
		if(! absolute){
    		Log.info("IDC."+ command +" ScriptName  is NOT absolute.  Trying JSCRIPTS/CUSTOM DIR for "+ scriptname);
			workfile = new CaseInsensitiveFile(Utilities.ROOT_JSCRIPTS_DIR + JSCRIPTS_CUSTOM_SUBDIR + File.separator + scriptname).toFile();
			absolute = (workfile.exists())&&(workfile.isFile())&&(workfile.isAbsolute());
		}
		if(! absolute){
			Log.debug("IDC."+ command +" unresolvable File location for Script: "+ scriptname);
			this.issueParameterValueFailure("ScriptName");
			return;
		}
		
		int varcount = 0;
		String varname = null;
		String varvalue = null;
		char cx = 'x';
		char c = cx;
		char cq = '=';
		char cv = '^';
		int i = 0;
		String trdAppend = "";
		while(iparams.hasNext()){
			i++;
			varname = "";
			aparam = (String) iparams.next();
			if (aparam == null) aparam = "";
			Log.info("IDC."+ command +" extracted possible Script parameter: "+ aparam);
			
			//check format "varname=varvalue"
			int index = aparam.indexOf(cq);
			try{ c = aparam.charAt(0);}catch(Exception x){ c = cx;}
			// "=" separator found
			if(index > 0){
				try{ varvalue = aparam.substring(index+1);}catch(Exception x){ varvalue = "";} 
				varname = aparam.substring(0, index);
				// try to remove leading ^ if present
				if(varname.charAt(0)==cv){ 
					try{ varname = varname.substring(1);}
					catch(Exception x){
						Log.debug("IDC."+ command +" ignoring invalid parameter name: "+ varname);
						varname = "";
						// just use aparam unmodified as value
						varvalue = aparam;
					}
				}
			// no "=" separator	
			}else{
				varvalue = aparam;
			}
			if(varname.length()==0){
			    varname = "VAR"+ String.valueOf(i);	
			}
			Log.info(command +" appending JavaScript var "+ varname + cq + "\""+ varvalue +"\"");
			trdAppend += "\nvar "+ varname + cq +"\""+ varvalue +"\";";			
		}
		IStatus stat = null;
		try{
			stat = CFComponent.processIOSScript(Utilities.JSCRIPTS_ABSOLUTE_PATH, workfile.getAbsolutePath(), trdAppend, testRecordData); 
		}catch(Exception x){
			String errmsg = "IDC."+ command +" "+ x.getClass().getSimpleName()+":"+ x.getMessage();
			Log.debug(errmsg);
			issueErrorPerformingAction(x.getClass().getSimpleName()+":"+ x.getMessage());
			Utilities.restoreInstrumentsScript();
			return;
		}
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.		
   		if(stat.rc == IStatus.STAT_FAILURE){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
   		// if NOT_EXECUTED that is more an indication the script doesn't set our return codes
   		if(stat.rc == IStatus.STAT_NOT_EXECUTED){
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);   			
   		}else{
			testRecordData.setStatusCode(stat.rc);   			
   		}
   		boolean hasdetail = stat.comment != null && stat.comment.length() > 0;
   		if(hasdetail) testRecordData.setStatusInfo(stat.comment);

		String msg = GENStrings.convert(GENStrings.SUCCESS_2A, 
	        			command +" successful using "+ workfile.getAbsolutePath(), 
	        			command, workfile.getAbsolutePath());
	    if(hasdetail){
	    	log.logMessage(testRecordData.getFac(), msg, stat.comment, GENERIC_MESSAGE);
	    }else{
	    	log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);	    	
	    }
	}
}
