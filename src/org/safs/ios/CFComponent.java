/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.ios;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.SAFSRuntimeException;
import org.safs.SAFSStringTokenizer;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringMBTokenizer;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.image.JAIImagingListener;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

public class CFComponent extends ComponentFunction {

	/** "GenericObject/" 
	 * Subdir off of Utilities.ROOT_JSCRIPTS_DIR containing GenericObject and GenericMaster keyword implementation scripts. */
	public static final String JSCRIPTS_GENERICOBJECT_SUBDIR="GenericObject/";
	
	/** "click.js" implementation for supported Click commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_CLICK_SCRIPT="click.js";
	/** "press.js" implementation for supported Press commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_PRESS_SCRIPT="press.js";
	/** "guiexists.js" implementation for supported GuiDoes/DoesNotExist commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_GUIEXISTS_SCRIPT="guiexists.js";
	/** "guiimage.js" implementation for supported GetGuiImage and related commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_GUIIMAGE_SCRIPT="guiimage.js";
	/** "guiproperty.js" implementation for supported GetProperty and related commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_GUIPROPERTY_SCRIPT="guiproperty.js";
	/** "flick.js" implementation for supported Flick commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_FLICK_SCRIPT="flick.js";
	/** "keyboard.js" implementation for supported keyboard commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_KEYBOARD_SCRIPT="keyboard.js";
	
	static final String CASE_INSENSITIVE = "CASE-INSENSITIVE";
	static final String CASEINSENSITIVE = "CASEINSENSITIVE";
	
	/** "ASSIGNPROPERTYVARIABLE" */
	public static final String COMMAND_ASSIGNPROPERTYVARIABLE = "ASSIGNPROPERTYVARIABLE";
	/** "CAPTUREPROPERTYTOFILE" */
	public static final String COMMAND_CAPTUREPROPERTYTOFILE = "CAPTUREPROPERTYTOFILE";
	/** "CAPTUREPROPERTIESTOFILE" */
	public static final String COMMAND_CAPTUREPROPERTIESTOFILE = "CAPTUREPROPERTIESTOFILE";
	/** "CLICK" */
	public static final String COMMAND_CLICK = "CLICK";
	/** "DOUBLECLICK" */
	public static final String COMMAND_DOUBLECLICK = "DOUBLECLICK";
	/** "DOUBLETAP" */
	public static final String COMMAND_DOUBLETAP = "DOUBLETAP";
	/** "FLICK" */
	public static final String COMMAND_FLICK = "FLICK";
	/** "FLICKUP" */
	public static final String COMMAND_FLICKUP = "FLICKUP";
	/** "FLICKDOWN" */
	public static final String COMMAND_FLICKDOWN = "FLICKDOWN";
	/** "FLICKLEFT" */
	public static final String COMMAND_FLICKLEFT = "FLICKLEFT";
	/** "FLICKRIGHT" */
	public static final String COMMAND_FLICKRIGHT = "FLICKRIGHT";
	/** "GUIDOESEXIST" */
	public static final String COMMAND_GUIDOESEXIST = "GUIDOESEXIST";
	/** "GUIDOESNOTEXIST" */
	public static final String COMMAND_GUIDOESNOTEXIST = "GUIDOESNOTEXIST";
	/** "GETGUIIMAGE" */
	public static final String COMMAND_GETGUIIMAGE = "GETGUIIMAGE";
	/** "INPUTCHARACTERS" */
	public static final String COMMAND_INPUTCHARACTERS = "INPUTCHARACTERS";
	/** "LOCATESCREENIMAGE" */
	public static final String COMMAND_LOCATESCREENIMAGE = "LOCATESCREENIMAGE";
	/** "PRESS" */
	public static final String COMMAND_PRESS = "PRESS";
	/** "TAP" */
	public static final String COMMAND_TAP = "TAP";
	/** "TWOFINGERTAP" */
	public static final String COMMAND_TWOFINGERTAP = "TWOFINGERTAP";
	/** "VERIFYGUIIMAGETOFILE" */
	public static final String COMMAND_VERIFYGUIIMAGETOFILE = "VERIFYGUIIMAGETOFILE";
	/** "VERIFYPROPERTY" */
	public static final String COMMAND_VERIFYPROPERTY = "VERIFYPROPERTY";
	/** "VERIFYPROPERTYTOFILE" */
	public static final String COMMAND_VERIFYPROPERTYTOFILE = "VERIFYPROPERTYTOFILE";
	/** "VERIFYPROPERTIESTOFILE" */
	public static final String COMMAND_VERIFYPROPERTIESTOFILE = "VERIFYPROPERTIESTOFILE";
	/** "VERIFYPROPERTYCONTAINS" */
	public static final String COMMAND_VERIFYPROPERTYCONTAINS = "VERIFYPROPERTYCONTAINS";
	
	protected String windowGUIID = null;
	protected String compGUIID = null;
	
	protected CFWindow windowFunctions = new CFWindow();
	protected CFEditBox editBoxFunctions = new CFEditBox();
	
	/**
	 * Keywords like CaptureXXXXX, will write file with different encoding before.<br>
	 * To keep them consistent, we let them to use the same default encoding<br>
	 * But this will affect the old test, this option is used to tell if we use consistent<br>
	 * encoding for these keywords<br>
	 * Default, we should let it as true to use consistent encoding.<br>
	 * See defect S0751446.
	 */
	protected boolean keepEncodingConsistent = true;
	public void setKeepEncodingConsistent(){
	    String temp = System.getProperty("encoding.consistency", "true");
		keepEncodingConsistent = Boolean.parseBoolean(temp);
	}
	public boolean getKeepEncodingConsistent(){
	  return keepEncodingConsistent;  
	}
	  
	public CFComponent() {
		super();
		setKeepEncodingConsistent();
	}
	
	/* not sure if we need this, or if setting a chainedProcessor might be more appropriate
	 * it is initialized during process() call.
	protected boolean initProcessorAndProcess (Processor aprocessor, Collection params) {
	    if (aprocessor != null) {
	        Log.info("IOS CFComponent configuring subprocessors...");
	        windowFunctions.setLogUtilities(log);
	        windowFunctions.setTestRecordData(testRecordData);
	        if(!windowFunctions.processorConfigSet) windowFunctions.distributeConfigInformation();
	        windowFunctions.setParams(params);
	    }
	    return super.initProcessorAndProcess(aprocessor, params);
	}
	
	*/
	/**
	 * TestRecordData should already have most fields including the object recognition strings.
	 */
	public void process(){		
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		try{ 
			getHelpers();
		}catch(Exception x){//assuming no exceptions are actually thrown
			Log.debug("CFComponent.process unexpected getHelpers() "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		windowGUIID = null;
		compGUIID = null;
		if(action==null){
			Log.debug("CFComponent could not process NULL action command.");
			return;
		}		
		try {
			if((action.equalsIgnoreCase(COMMAND_CLICK))||
			   (action.equalsIgnoreCase(COMMAND_DOUBLECLICK))||
			   (action.equalsIgnoreCase(COMMAND_TAP))||
			   (action.equalsIgnoreCase(COMMAND_DOUBLETAP))||
			   (action.equalsIgnoreCase(COMMAND_TWOFINGERTAP))){
				doClick();
			}
			else if(action.equalsIgnoreCase(COMMAND_ASSIGNPROPERTYVARIABLE)){
				doPropertyCommands(false,false);
			}
			else if(action.equalsIgnoreCase(COMMAND_CAPTUREPROPERTYTOFILE)){
				doPropertyToFileCommands(false);
			}
			else if(action.equalsIgnoreCase(COMMAND_CAPTUREPROPERTIESTOFILE)){
				doPropertiesToFileCommands(false);
			}
			else if(action.equalsIgnoreCase(COMMAND_VERIFYPROPERTY)){
				doPropertyCommands(true,false);
			}
			else if(action.equalsIgnoreCase(COMMAND_VERIFYPROPERTYCONTAINS)){
				doPropertyCommands(true,true);
			}
			else if(action.equalsIgnoreCase(COMMAND_VERIFYPROPERTYTOFILE)){
				doPropertyToFileCommands(true);
			}
			else if(action.equalsIgnoreCase(COMMAND_VERIFYPROPERTIESTOFILE)){
				doPropertiesToFileCommands(true);
			}
			else if(action.equalsIgnoreCase(COMMAND_PRESS)){
				doPress();
			}
			else if(action.equalsIgnoreCase(COMMAND_FLICK)){
				doFlick();
			}
			else if((action.equalsIgnoreCase(COMMAND_FLICKUP))||
					(action.equalsIgnoreCase(COMMAND_FLICKDOWN))||
					(action.equalsIgnoreCase(COMMAND_FLICKLEFT))||
					(action.equalsIgnoreCase(COMMAND_FLICKRIGHT))){
				    doSimpleFlick();
			}
			else if(action.equalsIgnoreCase(COMMAND_INPUTCHARACTERS)){
				doCharacters();
			
			}else if(action.equalsIgnoreCase(COMMAND_GUIDOESEXIST)){
						doGuiExists(true);
			}else if(action.equalsIgnoreCase(COMMAND_GUIDOESNOTEXIST)){
				doGuiExists(false);
			}else if(action.equalsIgnoreCase(COMMAND_GETGUIIMAGE)){
				doGetGuiImage();
			}else if(action.equalsIgnoreCase(COMMAND_VERIFYGUIIMAGETOFILE)){
				doVerifyGuiImage();
			}
			
			/*
			 * Windows functions
			 */
			if(testRecordData.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
		        Log.info("IOS CFComponent configuring CFWindow...");
		        windowFunctions.setLogUtilities(log);
		        windowFunctions.setTestRecordData(testRecordData);
		        if(!windowFunctions.processorConfigSet) windowFunctions.distributeConfigInformation();
		        windowFunctions.setParams(params);
				windowFunctions.process();
			} 
			/*
			 * EditBox functions 
			 */
			if(testRecordData.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
		        Log.info("IOS CFComponent configuring CFEditBox...");
		        editBoxFunctions.setLogUtilities(log);
		        editBoxFunctions.setTestRecordData(testRecordData);
		        if(!editBoxFunctions.processorConfigSet) editBoxFunctions.distributeConfigInformation();
		        editBoxFunctions.setParams(params);
				editBoxFunctions.process();
			}			
			
		} catch (SAFSObjectRecognitionException e) {
			Log.debug("CFComponent recognition string missing or invalid for: "+ e.getMessage());
			issueParameterValueFailure(e.getMessage());
		} catch (InstrumentsScriptExecutionException e){
			Log.debug("CFComponent "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueUnknownErrorFailure(e.getMessage());
		} catch (SAFSObjectNotFoundException e){
			Log.debug("CFComponent "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
									   e.getMessage() +" was not found.", e.getMessage()));		
		}
	}

	/**
	 * @param jsSubdir -- Subdirectory off of {@value Utilities#ROOT_JSCRIPTS_DIR} to locate desired jscript.
	 * If jsSubdir is not null and equals {@value Utilities#JSCRIPTS_ABSOLUTE_PATH} than the routine will 
	 * assume the specified jscript parameter is NOT relative and contains the full absolute path to the script.
	 * 
	 * @param jscript -- filename (or fullpath) to be used with jsSubdir for hook.js.
	 * @param trdAppend -- any additional javascript variables or code to add to trd.js.
	 * @return IStatus object generated from {@link Utilities#waitScriptComplete(String)}<br>
	 * This routine should be assumed to have already localized whatever comment or detail was sent back from Instruments.<br>
	 */
	static protected IStatus processIOSScript(String jsSubdir, String jscript, String jsAppend, TestRecordHelper testRecordData) throws InstrumentsScriptExecutionException{
		// rootdir could change dynamically, though currently this is not expected
		String rootdir = Utilities.ROOT_JSCRIPTS_DIR;
		boolean absolute = Utilities.JSCRIPTS_ABSOLUTE_PATH.equals(jsSubdir);
		try {
			if(absolute) {
				Utilities.prepareNextInstrumentsTest(Utilities.JSCRIPTS_ABSOLUTE_PATH, jscript, null);
			}else{
				if(jsSubdir == null) jsSubdir = File.separator;
				if(!jsSubdir.endsWith(File.separator)) jsSubdir = jsSubdir.concat(File.separator);
				Utilities.prepareNextInstrumentsTest(rootdir + jsSubdir, jscript, null);
			}
			Utilities.prepareNextTestRecordData(testRecordData);
			
			if((jsAppend != null)&&(jsAppend.length()> 0)){
				try { Utilities.writeDataToFile(jsAppend, rootdir, Utilities.JSCRIPTS_TRD, true); }
				catch (InstrumentsTestRecordDataException e) {
					Log.debug("WARNING: " + testRecordData.getCommand() +" ignoring optional "+ Utilities.JSCRIPTS_TRD +" append error: "+ e.getMessage());
				}
			}			
			Utilities.nextInstrumentsTest();
			Utilities.waitScriptComplete();
			IStatus stat =  Utilities.getIStatus();
			
			//might try to NLS convert any comment or details passed in here
			
			if(stat.comment == null) stat.comment = "";
			if(stat.detail ==null) stat.detail = "";
			
			return stat;
		} catch (InstrumentsTestRecordDataException e) {
			Log.debug("CFComponent.processIOSScript failed to export testRecordData:"+ e.getMessage());
			throw new InstrumentsScriptExecutionException(e.getMessage());
		} catch (InstrumentsStartScriptException e) {
			Log.debug("CFComponent.processIOSScript failed to run "+ jscript+": "+ e.getMessage());
			throw new InstrumentsScriptExecutionException(e.getMessage());
		} catch (InstrumentsLaunchFailureException e) {
			Log.debug("CFComponent.processIOSScript failed to process Automation Results for "+ jscript+": "+ e.getMessage());
			throw new InstrumentsScriptExecutionException(e.getMessage());
		} catch (Exception e){
			Log.debug("CFComponent.processIOSScript failed to run "+ jscript+": "+ e.getClass().getSimpleName()+":"+e.getMessage());
			throw new InstrumentsScriptExecutionException(e.getClass().getSimpleName()+":"+e.getMessage());
		}
	}

	/** 
	 * Support commands CapturePropertiesToFile and VerifyPropertiesToFile
	 * @param b 
	 **/
	private void doPropertiesToFileCommands (boolean verify)throws SAFSObjectRecognitionException, 
	  												InstrumentsScriptExecutionException,
	  												SAFSObjectNotFoundException {
	    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    String filetype = verify ? "BenchFile":"File";

	    if (params.size() < 1) {
		    String msg = filetype;
	        issueParameterCountFailure(msg);
		    return;
	    }
		Iterator iterator = params.iterator();
	    String filename =  (String) iterator.next();
		if((filename==null)||(filename.length()==0)){
	        this.issueParameterValueFailure(filetype);
	        return;
		}

		filename = FileUtilities.normalizeFileSeparators(filename);
	    
	    //build File
	    File fn = new CaseInsensitiveFile(filename).toFile();
	    if (!fn.isAbsolute()) {
	    	String pdir = null;
	    	try{
		   		if( filename.indexOf(File.separator) > -1 ) {
			  		pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
		    	}else{
			   		pdir = verify ? getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY):getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
		    	}
	    	}catch(Exception x){}
	    	if ((pdir == null)||(pdir.equals(""))){
	  		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
	  				"Could not get one or more variable values.")+ 
	  				" "+ STAFHelper.SAFS_VAR_PROJECTDIRECTORY+", "+ 
	  				(verify ? STAFHelper.SAFS_VAR_BENCHDIRECTORY:STAFHelper.SAFS_VAR_BENCHDIRECTORY);
	  		this.issueActionOnXFailure(filename, error);
	  		return;
	    	}
		    fn = new CaseInsensitiveFile(pdir, filename).toFile();
	    }
	    if(verify && (!fn.exists()||!fn.isFile()||!fn.canRead())){
	        this.issueParameterValueFailure("BenchmarkFile="+fn.getAbsolutePath());
	    	return;
	    }
	    try{
	    	Log.info("CF "+ action +" filename resolves to: "+ fn.getAbsolutePath());}
	    catch(NullPointerException np){
	    	Log.info("CF "+ action +" filename resolves to NULL");
	    }
	    
	    String encoding = null;
	    if (iterator.hasNext()) encoding = ((String)iterator.next()).trim();
	    Log.info(".....CFComponent.doPropertiesToFileCommands ready to "+ action +" using "+filename +" with encoding "+ encoding);

		String trdAppend = null;

	    // write trd.js to locate given component and captureRectWithName
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
			
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIPROPERTY_SCRIPT, trdAppend, testRecordData); 

		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == if OK, name or path to captured image from Instruments.
		if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else if(stat.rc != IStatus.STAT_OK){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
    
        String rval = stat.detail;
        Log.info("..... real value is: "+rval);
        // do it...
        Collection contents = new ArrayList();
        if(rval.length() > 0){
        	StringMBTokenizer toker = new StringMBTokenizer(rval, "|P|");
		    Log.info(".....CFComponent.doPropertiesToFileCommands found "+ toker.countTokens() +" properties.");
		    while(toker.hasMoreTokens()){
		    	try{ contents.add(toker.nextElement().toString());}catch(Exception x){}//ignore
		    }
        }else{
		    Log.info(".....CFComponent.doPropertiesToFileCommands found no properties.");
        }
        
        if(! verify){ //capture to file
	        try{
	    	    //If a file encoding is given or we need keep the encoding consistent.
	    	    if(encoding!=null || keepEncodingConsistent){
	    	    	StringUtils.writeEncodingfile(fn.getAbsolutePath(), contents, encoding);
	    	    }else{
	    	    	//keep compatible with old version
	    	    	StringUtils.writefile(fn.getAbsolutePath(), contents);
	    	    }
	        }
    	    catch (java.lang.SecurityException se) {
    	    	//error, security problems accessing output file
    	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
    	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
    	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
    	  	  return;
    	    }
    	    catch (java.lang.IllegalArgumentException se) {
    	    	//error, bad parameters sent to JAI.create call
    		      	//error, security problems accessing output file
    	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
    	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
    	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
    	  	  return;
    	    }
    	    catch (Exception e) {
    	    	//error, unable to capture the screen image
    	  	    this.issueErrorPerformingAction(e.getClass().getSimpleName());
    	      	return;
    	    }
	    	//success!  set status to ok
	        this.issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO, 
	      		  compName +" has been saved to '"+fn.getAbsolutePath()+"'", 
	      		  compName, fn.getAbsolutePath()));
	        testRecordData.setStatusCode(StatusCodes.OK);
        }
        else{// verify property
            Collection benchContents = null;
            
            try{
	    	    //If a file encoding is given or we need to keep the encoding consistent
	    	    if(encoding!=null || keepEncodingConsistent){
	    	    	benchContents = StringUtils.readEncodingfile(fn.getAbsolutePath(), encoding);
	    	    }else{
	    	    	//Keep compatible with old version
	    	    	benchContents = StringUtils.readfile(fn.getAbsolutePath());
	    	    }
            }catch (IOException ioe) {
    	    	//error, security problems accessing output file
      	  	    this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.FILE_READ_ERROR, 
      	  			  "File read error for file '"+fn.getAbsolutePath()+"': "+ioe.getClass().getSimpleName(), 
      	  			  fn.getAbsolutePath()+": "+ ioe.getClass().getSimpleName()));
      	  	    return;
            }
        	if(contents.equals(benchContents)){
       			this.issuePassedSuccess(passedText.convert(GENStrings.CONTENT_MATCHES_KEY, 
    					"The contents of '"+ compName +"' matches the contents of '"+filename+"'.", 
    					compName, filename));
        	}else{
    			issueErrorPerformingAction(passedText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY, 
    					"The contents of '"+ compName +"' does not match the contents of '"+filename+"'.", 
    					compName, filename));
        	}
        }
	}
	  
	
	/** 
	 * Support commands CapturePropertyToFile and VerifyPropertyToFile
	 * Appends an offsets variable into trd.js with:
	 * <pre>
	 * var propname="propertyname";
	 * </pre>
	 * @param b 
	 **/
	private void doPropertyToFileCommands (boolean verify)throws SAFSObjectRecognitionException, 
	  												InstrumentsScriptExecutionException,
	  												SAFSObjectNotFoundException {
	    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    String filetype = verify ? "BenchFile":"File";

	    if (params.size() < 2) {
		    String msg = "PropertyName, ";
		    msg += filetype;
	        issueParameterCountFailure(msg);
		    return;
	    }
		Iterator iterator = params.iterator();
	    String prop = (String) iterator.next();
	    String filename =  (String) iterator.next();
		if((filename==null)||(filename.length()==0)){
	        this.issueParameterValueFailure(filetype);
	        return;
		}

		filename = FileUtilities.normalizeFileSeparators(filename);
	    
	    //build File
	    File fn = new CaseInsensitiveFile(filename).toFile();
	    if (!fn.isAbsolute()) {
	    	String pdir = null;
	    	try{
		   		if( filename.indexOf(File.separator) > -1 ) {
			  		pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
		    	}else{
			   		pdir = verify ? getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY):getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
		    	}
	    	}catch(Exception x){}
	    	if ((pdir == null)||(pdir.equals(""))){
	  		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
	  				"Could not get one or more variable values.")+ 
	  				" "+ STAFHelper.SAFS_VAR_PROJECTDIRECTORY+", "+ 
	  				(verify ? STAFHelper.SAFS_VAR_BENCHDIRECTORY:STAFHelper.SAFS_VAR_BENCHDIRECTORY);
	  		this.issueActionOnXFailure(filename, error);
	  		return;
	    	}
		    fn = new CaseInsensitiveFile(pdir, filename).toFile();
	    }
	    if(verify && (!fn.exists()||!fn.isFile()||!fn.canRead())){
	        this.issueParameterValueFailure("BenchmarkFile="+fn.getAbsolutePath());
	    	return;
	    }
	    try{
	    	Log.debug("CF "+ action +" filename resolves to: "+ fn.getAbsolutePath());}
	    catch(NullPointerException np){
	    	Log.debug("CF "+ action +" filename resolves to NULL");
	    }
	    
	    String encoding = null;
	    if (iterator.hasNext()) encoding = ((String)iterator.next()).trim();
	    Log.info(".....CFComponent.doPropertyToFileCommands ready to "+ action +" for prop : "+prop+" using "+filename +" with encoding "+ encoding);

		String trdAppend = "\nvar propname=\""+ prop +"\";\n";

	    // write trd.js to locate given component and captureRectWithName
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
			
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIPROPERTY_SCRIPT, trdAppend, testRecordData); 

		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == if OK, name or path to captured image from Instruments.
		if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else if(stat.rc != IStatus.STAT_OK){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
    
        String rval = stat.detail;
        Log.info("..... real value is: "+rval);
        // do it...
        Collection contents = new ArrayList();
        contents.add(prop+"="+rval);
        
        if(! verify){ //capture to file
	        try{
	    	    //If a file encoding is given or we need keep the encoding consistent.
	    	    if(encoding!=null || keepEncodingConsistent){
	    	    	StringUtils.writeEncodingfile(fn.getAbsolutePath(), contents, encoding);
	    	    }else{
	    	    	//keep compatible with old version
	    	    	StringUtils.writefile(fn.getAbsolutePath(), contents);
	    	    }
	        }
    	    catch (java.lang.SecurityException se) {
    	    	//error, security problems accessing output file
    	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
    	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
    	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
    	  	  return;
    	    }
    	    catch (java.lang.IllegalArgumentException se) {
    	    	//error, bad parameters sent to JAI.create call
    		      	//error, security problems accessing output file
    	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
    	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
    	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
    	  	  return;
    	    }
    	    catch (Exception e) {
    	    	//error, unable to capture the screen image
    	  	    this.issueErrorPerformingAction(e.getClass().getSimpleName());
    	      	return;
    	    }
	    	//success!  set status to ok
	        this.issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO, 
	      		  prop +" has been saved to '"+fn.getAbsolutePath()+"'", 
	      		  prop, fn.getAbsolutePath()));
	        testRecordData.setStatusCode(StatusCodes.OK);
        }
        else{// verify property
            Collection benchContents = null;
            
            try{
	    	    //If a file encoding is given or we need to keep the encoding consistent
	    	    if(encoding!=null || keepEncodingConsistent){
	    	    	benchContents = StringUtils.readEncodingfile(fn.getAbsolutePath(), encoding);
	    	    }else{
	    	    	//Keep compatible with old version
	    	    	benchContents = StringUtils.readfile(fn.getAbsolutePath());
	    	    }
            }catch (IOException ioe) {
    	    	//error, security problems accessing output file
      	  	    this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.FILE_READ_ERROR, 
      	  			  "File read error for file '"+fn.getAbsolutePath()+"': "+ioe.getClass().getSimpleName(), 
      	  			  fn.getAbsolutePath()+": "+ ioe.getClass().getSimpleName()));
      	  	    return;
            }
        	if(contents.equals(benchContents)){
       			this.issuePassedSuccess(passedText.convert(GENStrings.CONTENT_MATCHES_KEY, 
    					"The contents of '"+ prop +"' matches the contents of '"+filename+"'.", 
    					prop, filename));
        	}else{
    			issueErrorPerformingAction(passedText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY, 
    					"The contents of '"+ prop +"' does not match the contents of '"+filename+"'.", 
    					prop, filename));
        	}
        }
	}
	  
	
	/** 
	 * Support commands AssignPropertyVariable, VerifyProperty, VerifyPropertyContains.
	 * Appends an offsets variable into trd.js with:
	 * <pre>
	 * var propname="propertyname";
	 * </pre>
	 * @param b 
	 **/
	private void doPropertyCommands (boolean verify, boolean partial)throws SAFSObjectRecognitionException, 
	  												InstrumentsScriptExecutionException,
	  												SAFSObjectNotFoundException {
	    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    if (params.size() < 2) {
		    String msg = "PropertyName, ";
		    if (verify){
		        msg += partial? "SearchString":"ExpectedValue";
		    }else{
		    	msg += "VariableName";
		    }
	        issueParameterCountFailure(msg);
		    return;
	    }
		Iterator iterator = params.iterator();
	    String prop = (String) iterator.next();
	    String var =  (String) iterator.next();
	    boolean usecase = true;
	    if (verify && iterator.hasNext()){
	    	try{
	    		String checkcase = ((String)iterator.next()).trim();
	    	    if ((checkcase.equalsIgnoreCase(CASE_INSENSITIVE))||
	    	            (checkcase.equalsIgnoreCase(CASEINSENSITIVE))||
	    	            (checkcase.equalsIgnoreCase("FALSE"))){
	    	    	usecase = false;
	    	        }
	    	}catch(Exception x){
	    		Log.debug(x.getClass().getSimpleName()+": CFComponent.doPropertyCommands ignoring unrecognized CASE-INSENSITIVE setting.");
	    	}	    	
	    }
	    Log.info(".....CFComponent.doPropertyCommands ready to "+ action +" for prop : "+prop+" using "+var);

		String trdAppend = "\nvar propname=\""+ prop +"\";\n";

	    // write trd.js to locate given component and captureRectWithName
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
			
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIPROPERTY_SCRIPT, trdAppend, testRecordData); 

		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == if OK, name or path to captured image from Instruments.
		if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else if(stat.rc != IStatus.STAT_OK){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
    
        String rval = stat.detail;
        Log.info("..... real value is: "+rval);
        // do it...
        
        if(! verify){ //assign variable
	        try{
	        	if (!setVariable(var, rval)) throw new SAFSException("Failed to set variable value.");
	        }catch(SAFSException x){
	        	Log.debug(x.getClass().getSimpleName()+" aborting "+action, x);
				issueErrorPerformingAction(FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
						"Could not set '"+ var +"' to '"+ rval +"'", var, rval));	
				return;
	        }
	        // set status to ok
	        String varset = passedText.convert(GENStrings.VARASSIGNED2, 
	        		        "Value '"+ rval +"' was assigned to variable '"+ var +"'.", 
	        		        rval, var);
			issuePassedSuccess(varset);
        }
        else{// verify property
        	boolean matched = false;
        	if(usecase){ // IS case-sensitive
        		matched = partial ? rval.contains(var):rval.equals(var);
        	}else{
        		String lcrval = rval.toLowerCase();
        		String lcvar = var.toLowerCase();
        		matched = partial ? lcrval.contains(lcvar):lcrval.equals(lcvar);
        	}
        	String comment = null;
        	if(matched){
        		if(partial){
        			comment = passedText.convert("selection_partial_match", 
        					windowName+":"+compName +" property '"+prop+"' contains substring '"+var+"'.", 
        					windowName, compName, prop, var);
         		}else{
        			comment = passedText.convert("something_match", 
        					"'"+ prop +"' matches expected value '"+var+"'.", 
        					prop, var);
        		}
       			this.issuePassedSuccess(comment);
        	}else{
        		if(partial){
        			comment = failedText.convert("selection_not_partial_match", 
        					"Property '"+prop+"' does not contain substring '"+var+"'.", 
        					prop, var);
         		}else{
        			comment = failedText.convert("something_not_match", 
        					"'"+ prop +"' value '"+ rval +"' does not match expected value '"+var+"'.", 
        					prop, rval, var);
        		}
    			issueErrorPerformingAction(comment);
        	}
        }
	}
	  
	/**
	 * Capture a component GUI image to a file.
	 * Currently, IOS formats for the output file are JPG, BMP, TIF, GIF, PNG and PNM. 
	 * Optionally, the user may utilize the SubArea parameter to only capture a portion of the Component.
	 * Appends an offsets variable into trd.js with:
	 * <pre>
	 * var imagename="imagename";
	 * </pre>
	 * @param b 
	   **/
	protected void doGetGuiImage () throws SAFSObjectRecognitionException, 
    									   InstrumentsScriptExecutionException,
                                           SAFSObjectNotFoundException {
	    testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
	    if ( params.size( ) < 1 ) {
	        this.issueParameterCountFailure("OutputFile");
	        return;
	    } 
	    Iterator aIterator = params.iterator();
	    String filename = ( String ) aIterator.next( );
		if((filename==null)||(filename.length()==0)){
	        this.issueParameterValueFailure("OutputFile");
	        return;
		}
		File fn;
	    
	    filename = filename.trim();
	    String tmpname = filename.toLowerCase();
	    if(tmpname.endsWith(".jpg") ||
	       tmpname.endsWith(".bmp") ||
	       tmpname.endsWith(".png") ||
	       tmpname.endsWith(".tif") ||
	       tmpname.endsWith(".gif") ||
	       tmpname.endsWith(".pnm")){
	    	Log.info("Image file format is supported. file name: "+filename);
	    }else{
	    	Log.info("Image file format is not supported yet. file name: "+filename+" ; convert it to default format '.bmp' .");
	    	filename = filename + ".bmp";    	
	    }

	    filename = FileUtilities.normalizeFileSeparators(filename);
	    
	    //build File
	    fn = new CaseInsensitiveFile(filename).toFile();
	    if (!fn.isAbsolute()) {
	    	String pdir = null;
	    	try{
		   		if( filename.indexOf(File.separator) > -1 ) {
			  		pdir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
		    	}else{
			   		pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
		    	}
	    	}catch(Exception x){}
	    	if ((pdir == null)||(pdir.equals(""))){
	  		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
	  				"Could not get one or more variable values.")+ 
	  				" "+ STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY+", "+STAFHelper.SAFS_VAR_TESTDIRECTORY;
	  		this.issueActionOnXFailure(filename, error);
	  		return;
	    	}
		    fn = new CaseInsensitiveFile(pdir, filename).toFile();
	    }
	    try{
	    	Log.debug("CF GetGUiImage filename resolves to: "+ fn.getAbsolutePath());}
	    catch(NullPointerException np){
	    	Log.debug("CF GetGUiImage filename resolves to NULL");
	    }
	    
	    //get optional SubArea parameter
	    String stemp = new String("");
	    String subarea = new String("");
	    try {	stemp   = (String) aIterator.next(); }
	    catch (Exception nse) {}
	    if(stemp.equals("")){
			Log.info ("Component: getGuiImage SubArea not provided...");
	    }
	    else{
			subarea = this.lookupAppMapReference(stemp);
			if (subarea == null) {
				//error, subarea not found in AppMap
				this.issueActionOnXFailure("SubArea", 
						//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
						FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM,
					    "Item '"+ compName+":"+stemp +"' was not found in App Map '"+ mapname +"'", 
					    compName+":"+stemp, mapname));
				return;
			}
	    }
	    //make the short image name for iOS Instruments
	    String imagename = fn.getName();
	    String suffix = "bmp";
	    int pindex = imagename.lastIndexOf('.');
	    if(pindex > 0 && pindex < imagename.length()-3){
	    	suffix = imagename.substring(pindex+1).toLowerCase();
	    	imagename = imagename.substring(0, pindex);
	    }
		String trdAppend = "\nvar imagename=\""+ imagename +"\";\n";

		// write trd.js to locate given component and captureRectWithName
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
			
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIIMAGE_SCRIPT, trdAppend, testRecordData); 

		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == if OK, name or path to captured image from Instruments.
		if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else if(stat.rc != IStatus.STAT_OK){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
		// comp was found and image should be valid		
		String fullpath = Utilities.ROOT_INSTRUMENTS_OUTPUT_DIR + File.separatorChar + imagename +".png";		
		BufferedImage loadimg = null;
		BufferedImage buffimg = null;
		try{ 
			loadimg =  ImageIO.read(new File(fullpath));//PNG contains ALPHA information
			if (loadimg == null) throw new IllegalArgumentException("Unable to read "+ fullpath);
			//remove ALPHA information from the PNG image or it won't output correctly (bmp,jpg).
			//not sure if removing ALPHA will be a problem with output formats that allow ALPHA.
			buffimg = new BufferedImage(loadimg.getWidth(), loadimg.getHeight(), BufferedImage.TYPE_INT_RGB);
			buffimg.createGraphics().drawImage(loadimg, 0, 0, Color.BLACK, null);
		}
		catch(Exception fx){
			this.issueActionOnXFailure("OutputFile", 
					FAILStrings.convert(FAILStrings.FILE_READ_ERROR,
				    "Error reading from file '"+ fullpath +"'.", fullpath));
			return;        	
        }
		
	    Rectangle compRect = new Rectangle(0,0, buffimg.getWidth(), buffimg.getHeight());
    	Log.debug("CF GetGuiImage compRect resolves to: "+ compRect);
	    
	    Rectangle imageRect;
	    // use ImageUtils to crop image if SUBAREA was specified		    
	    if (!subarea.equals("")) {
			//get Rectangle from SubArea parameter
			imageRect = ImageUtils.getSubAreaRectangle(compRect, subarea);
			if (imageRect == null) {
				this.issueParameterValueFailure("SubArea="+subarea);
				return;
			}
			try{ buffimg = buffimg.getSubimage(imageRect.x, imageRect.y, imageRect.width, imageRect.height);}
			catch(RasterFormatException rx){
				this.issueParameterValueFailure("SubArea="+subarea);
				return;				
			}
	    }else{
	    	imageRect = compRect;
	    }
    	Log.debug("CF GetGuiImage final imageRect resolves to: "+ imageRect);
    	Log.debug("CF GetGuiImage captured image resolves to: "+ buffimg);
	    //capture component image to file
	    //since our call to getSubAreaRectangle() has already confirmed that imageRect is
	    //contained in compRect, we can assume that imageRect is also contained in the parent window
	    //(compRect was retrieved from parent window)
	    try {
	    	ImageUtils.saveImageToFile(buffimg, fn, 1.0F);
 	    	//ImageIO.write(buffimg, suffix, fn);
	        
	    	//success!  set status to ok
	        this.issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO, 
	      		  "Image has been saved to '"+fn.getAbsolutePath()+"'", 
	      		  "Image", fn.getAbsolutePath()));
	        testRecordData.setStatusCode(StatusCodes.OK);
	    }
	    catch (java.lang.SecurityException se) {
	    	//error, security problems accessing output file
	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
	  	  return;
	    }
	    catch (java.lang.IllegalArgumentException se) {
	    	//error, bad parameters sent to JAI.create call
		      	//error, security problems accessing output file
	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE, 
	  			  "Can not create file '"+fn.getAbsolutePath()+"': "+
	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
	  	  return;
	    }
	    catch (NoClassDefFoundError ncdfe) {
	    	//error, JAI not installed
	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
	  			  "Support for Java Advanced Imaging (JAI) not found!", 
	  			  "Java Advanced Imaging (JAI)"));
	  	  return;
	    }
	    catch (Exception e) {
	    	//error, unable to capture the screen image
	  	this.issueErrorPerformingAction(e.getClass().getSimpleName());
	      	return;
	    }
	  }

	/**
	 * Verify a component GUI image to a benchmark file.
	 * Currently, IOS formats for the output file are JPG, BMP, TIF, GIF, PNG and PNM. 
	 * Optionally, the user may utilize the SubArea parameter to only capture a portion of the Component.
	 * Appends an offsets variable into trd.js with:
	 * <pre>
	 * var imagename="imagename";
	 * </pre>
	 * @param b 
	   **/
	  protected void doVerifyGuiImage ()throws SAFSObjectRecognitionException, 
	   										   InstrumentsScriptExecutionException,
	   										   SAFSObjectNotFoundException  {
	    testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
	    if ( params.size( ) < 1 ) {
	      this.issueParameterCountFailure("BenchmarkFile");
	      return;
	    } 
	    Iterator aIterator = params.iterator();
	    String filename = ( String ) aIterator.next( );
		  if((filename==null)||(filename.length()==0)){
	      this.issueParameterValueFailure("BenchmarkFile");
	      return;
		  }
		  File fn;
	    
	    //currently we offer support for JPG, BMP, TIF, GIF, PNG and PNM, default to bmp
	    filename = filename.trim();
	    String tmpname = filename.toLowerCase();
	    if(tmpname.endsWith(".jpg") ||
	       tmpname.endsWith(".bmp") ||
	       tmpname.endsWith(".png") ||
	       tmpname.endsWith(".tif") ||
	       tmpname.endsWith(".gif") ||
	       tmpname.endsWith(".pnm")){
	    	Log.info("Image file format is supported. file name: "+filename);
	    }else{
	    	Log.info("Image file format is not supported yet. file name: "+filename+" ; convert it to default format '.bmp' .");
	    	filename = filename + ".bmp";    	
	    }
	    
	    filename = FileUtilities.normalizeFileSeparators(filename);
	    
	    //build File
	    fn = new CaseInsensitiveFile(filename).toFile();
	    if (!fn.isAbsolute()) {
	    	String pdir = null;
	    	try{
		   		pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
	    	}catch(Exception x){}
	    	if ((pdir == null)||(pdir.equals(""))){
	  		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
	  				"Could not get one or more variable values.")+ 
	  				" "+ STAFHelper.SAFS_VAR_BENCHDIRECTORY;
	  		this.issueActionOnXFailure(filename, error);
	  		return;
	    	}
		    fn = new CaseInsensitiveFile(pdir, filename).toFile();
	    }
	    if(!fn.exists()||!fn.isFile()||!fn.canRead()){
	        this.issueParameterValueFailure("BenchmarkFile="+fn.getAbsolutePath());
	    	return;
	    }

	    //get optional SubArea parameter
	    String stemp = new String("");
	    String subarea = new String("");
	    try {	stemp   = (String) aIterator.next(); }
	    catch (Exception nse) {}
	    if(stemp.equals("")){
			Log.info ("Component: verifyGuiImage SubArea not provided...");
	    }
	    else{
			subarea = this.lookupAppMapReference(stemp);
			if (subarea == null) {
				//error, subarea not found in AppMap
				this.issueActionOnXFailure("SubArea", 
						//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
						FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM,
					    "Item '"+ compName+":"+stemp +"' was not found in App Map '"+ mapname +"'", 
					    compName+":"+stemp, mapname));
				return;
			}
	    }
	    
	    //make the short image name for iOS Instruments
	    String imagename = fn.getName();
	    String suffix = "bmp";
	    int pindex = imagename.lastIndexOf('.');
	    if(pindex > 0 && pindex < imagename.length()-3){
	    	suffix = imagename.substring(pindex+1).toLowerCase();
	    	imagename = imagename.substring(0, pindex);
	    }
		String trdAppend = "\nvar imagename=\""+ imagename +"\";\n";

		// write trd.js to locate given component and captureRectWithName
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
			
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIIMAGE_SCRIPT, trdAppend, testRecordData); 

		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == if OK, name or path to captured image from Instruments.
		if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else if(stat.rc != IStatus.STAT_OK){
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
			return;
		}
		// comp was found and image should be valid		
		String fullpath = Utilities.ROOT_INSTRUMENTS_OUTPUT_DIR + File.separatorChar + imagename +".png";		
		BufferedImage loadimg = null;
		BufferedImage buffimg = null;
		try{ 
			loadimg =  ImageIO.read(new File(fullpath));//PNG contains ALPHA information
			if (loadimg == null) throw new IllegalArgumentException("Unable to read "+ fullpath);
			//remove ALPHA information from the PNG image or it won't output correctly (bmp,jpg).
			//not sure if removing ALPHA will be a problem with output formats that allow ALPHA.
			buffimg = new BufferedImage(loadimg.getWidth(), loadimg.getHeight(), BufferedImage.TYPE_INT_RGB);
			buffimg.createGraphics().drawImage(loadimg, 0, 0, Color.BLACK, null);
		}
		catch(Exception fx){
			this.issueActionOnXFailure("OutputFile", 
					FAILStrings.convert(FAILStrings.FILE_READ_ERROR,
				    "Error reading from file '"+ fullpath +"'.", fullpath));
			return;        	
        }
		
	    Rectangle compRect = new Rectangle(0,0, buffimg.getWidth(), buffimg.getHeight());
    	Log.debug("CF VerifyGuiImage compRect resolves to: "+ compRect);
	    
	    Rectangle imageRect;
	    if (!subarea.equals("")) {
			//get Rectangle from SubArea parameter
			imageRect = ImageUtils.getSubAreaRectangle(compRect, subarea);
			if (imageRect == null) {
				this.issueParameterValueFailure("SubArea="+subarea);
				return;
			}
	    }
	    else
			//get Rectangle from component itself
			imageRect = compRect;

	    //capture component image to file
	    //since our call to getSubAreaRectangle() has already confirmed that imageRect is
	    //contained in compRect, we can assume that imageRect is also contained in the parent window
	    //(compRect was retrieved from parent window)
	    try {
	    	//Use ImageIO to write image to a file, so that the same content will be used in the verifyGUIImageToFile()
	      	File tmpFile = null;

	      	if (filename.toLowerCase().endsWith(".jpg")) {
	      		tmpFile = File.createTempFile("image",".jpg");
	      	}else if (filename.toLowerCase().endsWith(".bmp")) {
	      		tmpFile = File.createTempFile("image",".bmp");
	      	}else if (filename.toLowerCase().endsWith(".png")) {
	      		tmpFile = File.createTempFile("image",".png");
	      	}else if (filename.toLowerCase().endsWith(".tif")) {
	      		tmpFile = File.createTempFile("image",".tif");
	      	}else if (filename.toLowerCase().endsWith(".gif")) {
	      		tmpFile = File.createTempFile("image",".gif");
	      	}else if (filename.toLowerCase().endsWith(".pnm")) {
	      		tmpFile = File.createTempFile("image",".pnm");
	      	}
	      	ImageUtils.saveImageToFile(buffimg, tmpFile, 1.0F);
	        
	      	//Read these two files
	        String bench = StringUtils.readBinaryFile(fn.getAbsolutePath()).toString();
	        String test  = StringUtils.readBinaryFile(tmpFile.getAbsolutePath()).toString();

	        tmpFile.delete();
	        
	        Log.info("benchcontents.length: "+bench.length());
	        Log.info("testcontents.length: "+test.length());
	        
	        //Compare two strings
	        if (bench.equals(test)) {
			    //success!  set status to ok
			    testRecordData.setStatusCode(StatusCodes.OK);
			    this.issuePassedSuccess(GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY,
	  					"the content of 'GUI Image' matches the content of "+fn.getAbsolutePath(),
	  					"GUI Image",fn.getAbsolutePath()));
			    return;
			} else {
				//TODO: We should store a snapshot of the failed image in Datapool\Test 
				this.issueErrorPerformingAction(GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
	        			"the content of 'GUI Image' does not match the content of "+fn.getAbsolutePath(),
	        			"GUI Image",fn.getAbsolutePath()));
				return;
			}
	    }
	    catch (java.lang.SecurityException se) {
	    	//error, security problems accessing output file
	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.FILE_ERROR, 
	  			"Error opening or reading or writing file '"+fn.getAbsolutePath()+"': "+
	  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
	    }
	    catch (java.lang.IllegalArgumentException se) {
	    	//error, bad parameters sent to JAI.create call
		      	//error, security problems accessing output file
		  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.FILE_ERROR, 
		  			"Error opening or reading or writing file '"+fn.getAbsolutePath()+"': "+
		  			  se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
	    }
	    catch (NoClassDefFoundError ncdfe) {
	    	//error, JAI not installed
	  	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
	  			  "Support for Java Advanced Imaging (JAI) not found!", 
	  			  "Java Advanced Imaging (JAI)"));
	    }
	    catch (Exception e) {
	    	//error, unable to capture the screen image
	  	    this.issueErrorPerformingAction(e.getClass().getSimpleName());
	    }
	  }


	/**
	 * process the GuiDoesExist, GuiDoesNotExist commands.
	 * @param seekexists true for GuiDoesExist, false for GuiDoesNotExist.
	 */
	protected void doGuiExists(boolean seekexists) throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_GUIEXISTS_SCRIPT, null, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		String msg = null;
		boolean match = (stat.rc == IStatus.STAT_OK);
		
		//GuiDoesExist
	    if (seekexists) {
	       if (match) {
				msg = GENStrings.convert(GENStrings.EXISTS, 
						windowName+":"+compName +" exists.", windowName+":"+compName);
				issuePassedSuccess(msg); 
	        }
		    else {
	        	msg = GENStrings.convert(GENStrings.NOT_EXIST, 
	        			windowName+":"+compName +" does not exist.", 
	        			windowName+":"+compName);
	        	this.issueErrorPerformingAction(msg);
			}
	    //GuiDoesNotExist...
	    }else {
	        if (match) {
				msg = GENStrings.convert(GENStrings.NOT_EXIST, 
						windowName+":"+compName +" does not exist", windowName+":"+compName);
				issuePassedSuccess(msg); 
	        }
	        else {
	        	msg = GENStrings.convert(GENStrings.EXISTS, 
	        			windowName+":"+compName +" exists.", windowName+":"+compName);
	        	this.issueErrorPerformingAction(msg);
	        }
	    }
	}

	/**
	 * process the Click, DoubleClick, Tap, DoubleTap, and TwoFingerTap commands...
	 * Appends an offsets variable into trd.js with:
	 * <pre>
	 * var offsets={x:value, y:value};
	 * </pre>
	 * If no offsets are specified than the default percent relative offsets of x:50, y:50 will be sent.
	 */
	protected void doClick() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		String trdAppend = null;
		String mapkey = null;
		Point coords = null;
		if (params.size()> 0){
			mapkey = (String) params.toArray()[0];
			coords = lookupAppMapCoordReference(mapkey);
			if(coords == null) coords=convertCoords(mapkey);
		}
		if(coords == null) coords = new Point(50,50);
		if(coords.x < 0) coords.x = 0;
		if(coords.x >100) coords.x = 100;
		if(coords.y < 0) coords.y = 0;
		if(coords.y >100) coords.y = 100;
		trdAppend = "\nvar offsets={\n";
		trdAppend += "x:"+coords.x+",\n";
		trdAppend += "y:"+coords.y+"};\n";
		Log.info(action +" using relative offsets: "+ coords.x +", "+ coords.y);
		
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_CLICK_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			if(coords == null){
				issuePassedSuccess(stat.comment); //comment might be empty
			}else{
				issuePassedSuccessUsing(Integer.toString(coords.x) +","+ Integer.toString(coords.y));
			}
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}
	}

	/**
	 * process the Press command.
	 * Appends a duration variable into trd.js with:
	 * <pre>
	 * var seconds=N (in seconds);
	 * </pre>
	 * If no duration is specified than the default value of 0 will be sent.
	 */
	protected void doPress() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		String trdAppend = null;
		String mapkey = null;
		int time = 0;
		if (params.size()> 0){
			mapkey = (String) params.toArray()[0];
			try{time = Integer.parseInt(mapkey);}catch(NumberFormatException x){
				Log.debug(action +" ignoring invalid Duration parameter:"+ mapkey +". Defaulting to 0.");
		    }
		}
		if(time < 0) time = 0;
		trdAppend = "\nvar seconds="+ time +";\n";
		Log.info(action +" using seconds: "+ time);
		
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_PRESS_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			issuePassedSuccessUsing(Integer.toString(time));
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}
	}

	/**
	 * process the InputCharacters command.
	 * Appends a duration variable into trd.js with:
	 * <pre>
	 * var characters="string";
	 * </pre>
	 * If no duration is specified than the default value of 0 will be sent.
	 */
	protected void doCharacters() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		String trdAppend = null;
		String mapkey = null;
		if (params.size()> 0){
			mapkey = (String) params.toArray()[0];
		}else{
			this.issueParameterCountFailure("TextValue");
			return;
		}
		trdAppend = "\nvar characters=\""+ mapkey +"\";\n";
		Log.info(action +" using characters: \""+ mapkey +"\"");
		
		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_KEYBOARD_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			issuePassedSuccessUsing("\""+ mapkey +"\"");
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}
	}

	/**
	 * process the Flick command...
	 * Appends the following variables into trd.js:
	 * <pre>
	 * var offsets={x1:value, y1:value, x2:value, y2:value};
	 * var seconds=N;  (in seconds, default: 1 )
	 * var touches=N; (default: 1)
	 * var counter=N; (default: 1)
	 * </pre>
	 * If no offsets are specified than the default percent relative offsets of x:50, y:50 will be sent.
	 */
	protected void doFlick() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		String trdAppend = null;
		String mapkey = null;
		Polygon coords = null;
		Iterator param = null;
		int touches = 1;
		int duration = 1;
		int counter = 1;
		
		if (params.size()> 0){
			param = params.iterator();
			mapkey = (String) param.next();			
			coords = lookupAppMapLineReference(mapkey);
			if(coords == null) coords=convertLine(mapkey);
		}
		if(coords == null) {
			int xs[] = new int[2];
			int ys[] = new int[2];
			if(action.equalsIgnoreCase(COMMAND_FLICK)||action.equalsIgnoreCase(COMMAND_FLICKLEFT)){
			    xs[0] = 90;xs[1]=10;
			    ys[0] = 50;ys[1]=50;
			}
			else if(action.equalsIgnoreCase(COMMAND_FLICKUP)){
			    xs[0] = 50;xs[1]=50;
			    ys[0] = 90;ys[1]=10;
			}
			else if(action.equalsIgnoreCase(COMMAND_FLICKDOWN)){
			    xs[0] = 50;xs[1]=50;
			    ys[0] = 10;ys[1]=90;
			}
			else if(action.equalsIgnoreCase(COMMAND_FLICKRIGHT)){
			    xs[0] = 10;xs[1]=90;
			    ys[0] = 50;ys[1]=50;
			}
			coords = new Polygon(xs, ys, 2);
		}
		if(coords.xpoints[0] < 0) coords.xpoints[0] = 0;
		if(coords.xpoints[1] < 0) coords.xpoints[1] = 0;
		if(coords.xpoints[0] > 100) coords.xpoints[0] = 100;
		if(coords.xpoints[1] > 100) coords.xpoints[1] = 100;

		if(coords.ypoints[0] < 0) coords.ypoints[0] = 0;
		if(coords.ypoints[1] < 0) coords.ypoints[1] = 0;
		if(coords.ypoints[0] > 100) coords.ypoints[0] = 100;
		if(coords.ypoints[1] > 100) coords.ypoints[1] = 100;

		if(param != null){
			String tmp = null;
			if(param.hasNext()){
				tmp = (String) param.next();
				try{ duration = Integer.parseInt(tmp);}
				catch(NumberFormatException x){
					Log.debug(action +" ignoring invalid Duration parameter:"+ tmp +". Defaulting to "+ duration);
				}
			}
			if(param.hasNext()){
				tmp = (String) param.next();
				try{ touches = Integer.parseInt(tmp);}
				catch(NumberFormatException x){
					Log.debug(action +" ignoring invalid Touches parameter:"+ tmp +". Defaulting to "+ touches);
				}
			}
			if(param.hasNext()){
				tmp = (String) param.next();
				try{ counter = Integer.parseInt(tmp);}
				catch(NumberFormatException x){
					Log.debug(action +" ignoring invalid Repeat parameter:"+ tmp +". Defaulting to "+ counter);
				}
			}
		}
		if (duration < 0) duration = 0;
		if (touches < 0) touches = 1;
		if (counter < 1) counter = 1;
		
		trdAppend = "\nvar offsets={\n";
		trdAppend += "x1:"+coords.xpoints[0]+",\n";
		trdAppend += "y1:"+coords.ypoints[0]+",\n";
		trdAppend += "x2:"+coords.xpoints[1]+",\n";
		trdAppend += "y2:"+coords.ypoints[1]+"};\n";
		Log.info(action +" using relative offsets: "+ coords.xpoints[0] +","+ coords.ypoints[0]+", "+ coords.xpoints[1]+","+ coords.ypoints[1]);
		
		trdAppend += "\nvar seconds="+ duration +";\n";
		Log.info(action +" using seconds: "+ duration);
		
		trdAppend += "\nvar touches="+ touches +";\n";
		Log.info(action +" using touches: "+ touches);

		trdAppend += "\nvar counter="+ counter +";\n";
		Log.info(action +" using counter: "+ counter);

		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_FLICK_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			if(coords == null){
				issuePassedSuccess(stat.comment); //comment might be empty
			}else{
				issuePassedSuccessUsing(Integer.toString(coords.xpoints[0]) +","+ Integer.toString(coords.ypoints[0])+", "+ 
						                Integer.toString(coords.xpoints[1]) +","+ Integer.toString(coords.ypoints[1]));
			}
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}
	}
	
	/**
	 * process the FlickUp, FlickDown, FlickLeft, FlickRight commands...
	 * Appends the following variables into trd.js:
	 * <pre>
	 * var offsets={x1:value, y1:value, x2:value, y2:value};
	 * var seconds=N;  (in seconds, default: 0)
	 * var touches=N; (default: 1)
	 * var counter=N; (default: 1)
	 * </pre>
	 * If no offsets are specified than the default percent relative offsets of x:50, y:50 will be sent.
	 */
	protected void doSimpleFlick() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}
		String trdAppend = null;
		String tmp = null;
		Polygon coords = null;
		Iterator param = null;
		int touches = 1;
		int duration = 0;
		int counter = 1;
		
		if (params.size()> 0){
			param = params.iterator();
			tmp = (String) param.next();			
		}
		int xs[] = new int[2];
		int ys[] = new int[2];
		if(action.equalsIgnoreCase(COMMAND_FLICKLEFT)){
		    xs[0] = 90;xs[1]=10;
		    ys[0] = 50;ys[1]=50;
		}
		else if(action.equalsIgnoreCase(COMMAND_FLICKUP)){
		    xs[0] = 50;xs[1]=50;
		    ys[0] = 90;ys[1]=10;
		}
		else if(action.equalsIgnoreCase(COMMAND_FLICKDOWN)){
		    xs[0] = 50;xs[1]=50;
		    ys[0] = 10;ys[1]=90;
		}
		else if(action.equalsIgnoreCase(COMMAND_FLICKRIGHT)){
		    xs[0] = 10;xs[1]=90;
		    ys[0] = 50;ys[1]=50;
		}
		coords = new Polygon(xs, ys, 2);
		if(tmp != null){
			try{ counter = Integer.parseInt(tmp);}
			catch(NumberFormatException x){
				Log.debug(action +" ignoring invalid Repeat parameter:"+ tmp +". Defaulting to "+ counter);
			}
		}
		if (counter < 1) counter = 1;
		
		trdAppend = "\nvar offsets={\n";
		trdAppend += "x1:"+coords.xpoints[0]+",\n";
		trdAppend += "y1:"+coords.ypoints[0]+",\n";
		trdAppend += "x2:"+coords.xpoints[1]+",\n";
		trdAppend += "y2:"+coords.ypoints[1]+"};\n";
		Log.info(action +" using relative offsets: "+ coords.xpoints[0]+","+ coords.ypoints[0]+", "+ coords.xpoints[1]+","+coords.ypoints[1]);
		
		trdAppend += "\nvar seconds="+ duration +";\n";
		Log.info(action +" using seconds: "+ duration);
		
		trdAppend += "\nvar touches="+ touches +";\n";
		Log.info(action +" using touches: "+ touches);

		trdAppend += "\nvar counter="+ counter +";\n";
		Log.info(action +" using counter: "+ counter);

		IStatus stat = processIOSScript(JSCRIPTS_GENERICOBJECT_SUBDIR, JSCRIPTS_FLICK_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			if(coords == null){
				issuePassedSuccess(stat.comment); //comment might be empty
			}else{
				issuePassedSuccessUsing(Integer.toString(coords.xpoints[0]) +","+ Integer.toString(coords.ypoints[0])+", "+ 
						                Integer.toString(coords.xpoints[1]) +","+ Integer.toString(coords.ypoints[1]));
			}
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}
	}
	
}
