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
package org.safs.rational;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.safs.Log;
import org.safs.MatchData;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.IScreen;
import com.rational.test.ft.object.interfaces.IWindow;
import com.rational.test.ft.object.interfaces.StatelessGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Area;
import com.rational.test.ft.script.Text;
import com.rational.test.ft.script.SubitemFactory;


/**
 * CFHtmlImage
 * Process HTML.AREA components.  Future versions may expand to support HTML.IMAGE and
 * other related classes.
 * <p>
 * @author  Carl Nagle
 * @since   NOV 30, 2004
 *
 *   <br>   NOV 30, 2004    (Carl Nagle) 	Original Release
 *   <br>   OCT 14, 2005    (Carl Nagle) 	Renamed support for Click to ClickArea
 *   <br>   MAR 25, 2008    (Lei Wang) 	Add keyword SaveHTMLImage and VerifyHTMLImage
 *   <br>   DEC 24, 2008    (JunwuMa)   Modify doHtmlAreaClick() giving a different try on AREA with click(atPoint) if the first click on it fails to be preformed.
 *                                      Fix S0548371. 
 **/
public class CFHtmlImage extends CFComponent {
	public static final String CLICK_AREA 				= "ClickArea";
	public static final String SAVEHTMLIMAGE          	= "SaveHTMLImage";
	public static final String VERIFYHTMLIMAGE         	= "VerifyHTMLImage";

	private static String FILENAME_PREFIX				= "^filename=";
	private static String COORDINATE_PREFIX				= "^xy=";
	
	protected String classname = "";

	
    /** <br><em>Purpose:</em> constructor, calls super
     **/
    public CFHtmlImage () {
        super();
    }

    /** 
     * Process the testRecordData.
     * This subclass only processes HTML.AREA class components at this time.
     **/
    protected void localProcess() {
    	if((action == null)||(obj1 == null)) return;
    	   		
    	classname = obj1.getObjectClassName();
    	Log.info("CFHtmlImage processing action: "+ action + " on class: "+ classname);

    	// exit if not a locally processed keyword
    	if( (! action.equalsIgnoreCase(CLICK_AREA)) &&
    		(! action.equalsIgnoreCase(SAVEHTMLIMAGE)) &&
    		(! action.equalsIgnoreCase(VERIFYHTMLIMAGE))) return;

    	// only process here those we care about.  
    	// Otherwise let the superclass handle it.
    	// Html.IMG and Html.MAP will be supported here later as necessary
    	if (classname.equalsIgnoreCase("HTML.AREA")) processHtmlArea();
    	else if(classname.equalsIgnoreCase("Html.IMG")) processHtmlImage();
    }
    
    /**
     *  handle Html.AREA objects
     *  Supports:
     *     ClickArea
     * 
	 *   <br>   OCT 14, 2005    (Carl Nagle) Renamed support for Click to ClickArea
     */    
    protected void processHtmlArea(){
    	// only handle ClickArea right now
    	if (action.equalsIgnoreCase(CLICK_AREA)) doHtmlAreaClick();
    }

	/**
	 * doHtmlAreaClick
	 * <P>
	 * Working on a defined Html.AREA within a Html.MAP
	 * 
     * locate the parent Html.MAP for its name
     * locate the parent Html.DOCUMENT
     * locate the Html.IMG within the DOCUMENT that uses the MAP
     * CLICK on the appropriate AREA within the IMG
     */
    protected void doHtmlAreaClick(){

		TestObject htmlMap = null;
		TestObject mapDocument = null;
		StatelessGuiSubitemTestObject htmlImage = null;
		String mapname = "";
		String areaname = "";

		String objclass = null;
		
    	// locate the parent Html.MAP for its name
		try{
	    	Log.info("CFHtmlImage.doHtmlAreaClick processing...");
	    	areaname = (String) obj1.getProperty("alt");
	    	htmlMap = obj1.getParent();    	
	    	objclass = htmlMap.getObjectClassName(); // should be Html.MAP
	    	Log.info("...Area parent: "+ objclass);
	    	mapname = (String) htmlMap.getProperty(".name");
	    	if ((mapname == null)||(mapname.length()==0)) 
	    	    mapname = (String) htmlMap.getProperty(".id");
	    	if ((mapname == null)||(mapname.length()==0)) {
		    	Log.info("...cannot locate associated map NAME or ID.");
		    	return;
	    	}
		}
		catch(Exception x){ 
	    	Log.info("...encountered exception:"+ x.toString());
	    	return;
		}
    	
    	TestObject ancestor = new TestObject(htmlMap);
    	boolean docFound = false;

    	// locate the parent Html.HTMLDOCUMENT
		Log.info("...seeking parent document...");
		do{
			mapDocument = ancestor.getParent();
			objclass = mapDocument.getObjectClassName(); // looking for Html.HtmlDocument
			Log.info("......found ancestor: "+ objclass);
			if (objclass.equalsIgnoreCase("Html.HtmlBrowser")) break; // too far
			docFound = objclass.equalsIgnoreCase("Html.HtmlDocument");			
			ancestor = mapDocument;
		
		}while(! docFound);
    	
    	if (! docFound) {
	    	Log.info("...did not locate parent document.");
	    	return;
    	}

    	// locate the Html.IMG within the DOCUMENT that uses the MAP
		Log.info("...found parent document...retrieving child IMGs...");

      	// this should be our HTML Document object
	    GuiTestObject guiObj = new GuiTestObject(mapDocument);
	    RGuiObjectVector search = new RGuiObjectVector(windowName, 
	                                                   compName, 
	                                                   "Type=HTMLImage", 
	                                                   script);

	    // the input Vector is NOT the same as the output Vector
		Vector matches = search.getChildMatchData(guiObj, new java.util.Vector());

	    if ((matches == null)||(matches.size()==0)){
	    	Log.info("...did not locate HTML document IMGs.");
            return;
	    }
		
		String usemap = "";
		MatchData tobj = null;
		boolean imageFound = false;
		int img = 0;
		
		// locate the first\only? image that uses the defined mapname
		Log.info("...seeking image using image map: "+ mapname);
		do{						
			try { 
				tobj = (MatchData) matches.elementAt(img++);
				htmlImage = (StatelessGuiSubitemTestObject) tobj.getGuiTestObject();
			    usemap = htmlImage.getProperty("useMap").toString();
				Log.info("......found IMG useMap: "+ usemap);
				if ((usemap == null)||(usemap.length()<mapname.length())) continue;
				imageFound = usemap.endsWith("#"+mapname);
			}
			catch(ClassCastException x){ Log.info("...IMG is NOT a StatelessGuiTestObject!");}
			catch(Exception x){ Log.info("...IMG handling exception: "+x.toString());}
		}while((!imageFound)&&(img<matches.size()));

    	if (! imageFound) {
	    	Log.info("...did not locate image using image map: "+ mapname);
	    	return;
    	}
		
    	// CLICK on the appropriate AREA within the IMG
    	try {
	        htmlImage.click(new Area(new Text(areaname)));	        
    	}
    	catch (Exception x) {
       		// try to click the center point of the AREA
    		try {
    			java.awt.Rectangle rc = ((GuiTestObject)obj1).getScreenRectangle();
    			Point centerOfArea = SubitemFactory.atPoint(rc.x + rc.width/2, rc.y + rc.height/2);
    			htmlImage.click(centerOfArea);
    		} catch (Exception ex) {
    	    	Log.info("...image click failed using image map: "+ mapname +
   	    	         ": area : "+ areaname +" : "+x.toString());
    			String message = failedText.convert(FAILStrings.FAILURE_2, "Unable to perform " + action + " on " + compName, compName, action);
    	        log.logMessage(testRecordData.getFac(),
    	                       message + ex.getMessage(),
    	                       FAILED_MESSAGE);
    	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    	        return;
    		}    		
    	}
    	
        // set status to ok
        String message = GENStrings.convert("success3", 
                                            windowName+":"+compName +" "+action+" successful.",
                                            windowName, compName, action);
        log.logMessage(testRecordData.getFac(), message, PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
    }

    /**
     * <b>Purpose:</b><br>
     * Save HtmlImage to a file; Or compare the HtmlImage to a bench file.<br>
     *
     */
    protected void processHtmlImage(){
    	String debugMsg = getClass().getName() + ".processHtmlImage(): ";
    	Iterator iterator = params.iterator();
      	StringBuffer benchfileName = new StringBuffer();
      	StringBuffer testfileName = new StringBuffer();
      	java.awt.Point point = new java.awt.Point(3,3);
    	
    	try{
        	//Parse parameters:
        	//The first parameter must be a file name, which is required.
        	//The second parameter is the coordination, which is optional. The default value is "3,3".
        	int paramterCount = this.params.size();
        	if(paramterCount == 0){
        		paramsFailedMsg(windowName, compName);
            	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        		return;
        	}else if(paramterCount ==1 ){
        		String filename = iterator.next().toString();
        		//filename = StringUtilities.removePrefix(filename,FILENAME_PREFIX);
        		initialFileName(filename,benchfileName,testfileName);
        	}else if(paramterCount >1){
        		//Get the filename parameter
        		String filename = iterator.next().toString();
        		//filename = StringUtilities.removePrefix(filename,FILENAME_PREFIX);
        		initialFileName(filename,benchfileName,testfileName);
        		//Get the coordiantion parameter
        		String coord = iterator.next().toString();
        		//coord = StringUtilities.removePrefix(coord,COORDINATE_PREFIX);
          		point = checkForCoord(coord);
              	if (point== null) {
            		point = new java.awt.Point(3,3);
        			Log.debug(debugMsg+" Invalid parameter value for Coords. Use the default coordination: "+point.toString());
              	}
        	}

    		//Do save or verification
    		if(action.equalsIgnoreCase(SAVEHTMLIMAGE)){
    			saveImageToFile(testfileName.toString(),point,false);
        		//Log the success message and set statusCode to success
    			String detail = passedText.convert(GENStrings.BE_SAVED_TO,
    					"GUI Image has been saved to "+testfileName.toString(),
    					"GUI Image",testfileName.toString());
        		this.componentSuccessMessage(detail);
            	testRecordData.setStatusCode(StatusCodes.OK);
    		}else if(action.equalsIgnoreCase(VERIFYHTMLIMAGE)){
    			//Save temporaire image file,the temporaire image file is always save to directory "project/test/"
    			saveImageToFile(testfileName.toString(),point,false);
    			
    			//Compare the bench file and the temporaire file
    			String bench = StringUtils.readBinaryFile(benchfileName.toString()).toString();
    			String test  = StringUtils.readBinaryFile(testfileName.toString()).toString();
    			
    			Log.debug(debugMsg+" bench length: "+bench.length());
    			Log.debug(debugMsg+" test length: "+test.length());
    			
    			if((bench.length()==test.length()) && bench.equals(test) ){
    	    		//Log the success message and set statusCode to success
    				String detail = genericText.convert(GENStrings.CONTENT_MATCHES_KEY,
    						"the content of 'GUI Image' matches the content of '"+benchfileName.toString()+"'",
							"GUI Image",benchfileName.toString());
    	    		this.componentSuccessMessage(detail);
    	        	testRecordData.setStatusCode(StatusCodes.OK);
    			}else{
    	    		//Log the fail message and set statusCode to fail
    				String detail = genericText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
    						"the content of 'GUI Image' does not match the content of '"+benchfileName.toString()+"'",
							"GUI Image",benchfileName.toString());
    				Log.debug(debugMsg+detail);
    	    		this.componentExecutedFailureMessage(detail);
    	        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    			}
    			//Delete the temporaire file
    			deleteImageFile(testfileName.toString());
    		}
    	}catch(SAFSException e){
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        	Log.debug("Exception occur in "+debugMsg+e.getMessage());
        	componentFailureMessage(e.getMessage());
    	} catch (IOException e) {
        	testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
        	Log.debug("Exception occur in "+debugMsg+e.getMessage());
        	componentFailureMessage(e.getMessage());
		}
    }
    
    /**
     * <b>Implementation1:</b><br> 
     * Pop up the popup menu at point, and use the "save image..." to save the iamge as a file<br>
     * 
     * <b>Implementation2:</b><br>
     * Capture the screen of the image object as bufferedImage and save it to a file<br>
     * The fault: If the image is not show totally, just part of image can be saved<br>
     * The second parameter is not used.<br>
     * 
     * @param testfileName	 Which file the image will be saved to.
     * @param point			 Where the context menu will be shown, relative to the image. (only used in Implementation2)
     * @param usePopup		 true if Use the popup-menu to save the image (Implementation2); otherwise use Implementation1.
     * @throws SAFSException
     */
    private void saveImageToFile(String testfileName,Point point,boolean usePopup) throws SAFSException{
    	String debugMsg = getClass().getName() + ".saveHtmlImage(): ";
    	
    	//The obj1 should be an html.image
    	if(obj1 instanceof GuiTestObject){
    		GuiTestObject guiTestObject = new GuiTestObject(obj1.getObjectReference());
    		
			try {
				//1. Treate the testfileName
				//Verify if the parent directories exist. If it not exists, we try to create it.
				File parentDir = new File(testfileName.substring(0,testfileName.lastIndexOf(File.separator)));
				if(!parentDir.exists()){
					if(parentDir.mkdirs()){
						Log.info(debugMsg+" Create directory "+parentDir.getAbsolutePath());
					}else{
						String detail = failedText.convert(FAILStrings.CANT_CREATE_DIRECTORY,
								" Can not create directory "+parentDir.getAbsolutePath(),
								parentDir.getAbsolutePath());
						Log.debug(debugMsg+detail);
						throw new SAFSException(detail);
					}
				}
				//Verify if the file exists. If it exists, we try to delete it.
				File file = new File(testfileName);
				if(file.exists()){
					if(file.delete()){
						Log.info(debugMsg+" Delete file "+testfileName);
					}else{
						String detail = failedText.convert(FAILStrings.CANT_DELETE_FILE,
								" Can not delete file "+testfileName,
								testfileName);
						Log.debug(debugMsg+detail);
						throw new SAFSException(detail);
					}
				}

				//2. Save image to the file
				if (usePopup) {//Implementation1: use the context-menu to save
					IScreen screen = Script.getScreen();
					IWindow window = null;

					//The active window should be the "Browser window"
					window = screen.getActiveWindow();
					Log.info(debugMsg + window.getText() + " : " + window.getWindowClassName());
					//Pop-up the context menu
					guiTestObject.click(Script.RIGHT, point);
					//Use the accelerate-key Ctrl+S to activate the "Save image dialog"
					window.inputKeys("^s");

					//The active window should be the "Save image dialog"
					window = screen.getActiveWindow();
					Log.info(debugMsg + window.getText() + " : " + window.getWindowClassName());
					//Change the file name of the "save image dialog"
					window.inputChars(testfileName);
					window.inputKeys("~");
				} else {//Implementation2: use the screen-cut to save
					Rectangle compRect = guiTestObject.getClippedScreenRectangle();
					//Get bufferedImage
					Robot rob;
					rob = new Robot();
					BufferedImage buffimg = rob.createScreenCapture(compRect);

					//Write bufferedImage to file
					if (testfileName.toLowerCase().endsWith(".jpg") || testfileName.toLowerCase().endsWith(".jpeg")) {
						ImageIO.write(buffimg, "jpg", file);
					}else if(testfileName.toLowerCase().endsWith(".bmp")) {
						ImageIO.write(buffimg, "bmp", file);
					}else{
						String detail = " testfileName is "+testfileName+". Change the file suffix to .jpg, .jpeg or .bmp!";
						Log.debug(debugMsg+detail);
						throw new SAFSException(detail);
					}
				}
			} catch (AWTException e) {
				Log.debug(debugMsg + " can not instantiate java.awt.Robot object. Msg: "  + e.getMessage());
				throw new SAFSException( "Can not instantiate java.awt.Robot object.");
			} catch (IOException e) {
				String detail = failedText.convert(FAILStrings.FILE_ERROR,
						"Error opening or reading or writing file " + testfileName, testfileName);
				Log.debug(debugMsg + e.getMessage() + ". " + detail);
				throw new SAFSException(detail);
			}
    	}else{
    		Log.debug(debugMsg+" object is not GuiTestObject, can not process!");
    		throw new SAFSException("object is not GuiTestObject, can not process!");
    	}
    }
    
    /**
     * <b>Purpose:</b><br>
     * Delete the temporaire file<br>
     * 
     * @param testfileName		The file to be deleted
     * @throws SAFSException
     */
    private void deleteImageFile(String testfileName){
    	String debugMsg = getClass().getName() + ".deleteHtmlImage(): ";
  		if(testfileName!=null){
  			//Delete the test file
  			try{
  				//It seems that JAI did not close the outputstream, can not delete the file.
  	  			//reference to http://forum.java.sun.com/thread.jspa?threadID=166271&tstart=45
  				System.gc();
  				File file = new File(testfileName);
  				if(file.delete()){
  					Log.debug(debugMsg+testfileName+" has been deleted!");
  				}else{
  					Log.debug(debugMsg+testfileName+" can not be deleted!");
  				}
  			}catch (SecurityException e){
  				Log.debug(debugMsg+" Delete file failed. Msg:"+e.getMessage());
  			}
  		}
    }
    
    /**
     * <b>Note:</b><br>
     * 	This is used internally by processHtmlImage().<br>
     * 	If action is SAVEHTMLIMAGE, only testFileName will be initialed; otherwise, both will be initialed.<br>
     * 
     * <b>Purpose:</b><br>
     * Initial the testFileName and benchFileName.<br>
     * 
     * @param filename
     * @param benchfileName	Will contains an absolute bench file name
     * @param testfileName	Will contains an absolute test file name
     * @throws SAFSException
     */
    private void initialFileName(String filename, StringBuffer benchfileName,StringBuffer testfileName) throws SAFSException{
    	String debugMsg = getClass().getName() + ".initialFileName(): ";
    	
	    //Get the bench and test absolute fileName
		if(filename.equals("")||filename.endsWith(File.separator)){
			String detail = failedText.convert(FAILStrings.BAD_PARAM,"Invalid parameter value for FileName","FileName");
			Log.debug(debugMsg+detail);
			throw new SAFSException(detail);
		}
		
		//currently we only offer support for bmp or jpg, default to bmp
		if ( !(filename.toLowerCase().endsWith(".jpg") ||
			   filename.toLowerCase().endsWith(".jpeg") ||
			   filename.toLowerCase().endsWith(".bmp")))
			filename = filename + ".bmp";
	    
		File fn = new CaseInsensitiveFile(filename).toFile();
	    String pdir = null;
		if(action.equalsIgnoreCase(SAVEHTMLIMAGE)){
			//Resolve the testfile name
  	        if (!fn.isAbsolute()) {
				if (filename.indexOf(File.separator) < 0) {
					testfileName.append(getAbsoluteFileName(STAFHelper.SAFS_VAR_TESTDIRECTORY,filename));
				} else {
					testfileName.append(getAbsoluteFileName(STAFHelper.SAFS_VAR_PROJECTDIRECTORY,filename));
				}
  	        }else{
  	        	testfileName.append(filename);
  	        }
		}else if(action.equalsIgnoreCase(VERIFYHTMLIMAGE)){
			//Resolve the benchfile name
  	        if (!fn.isAbsolute()) {
				if (filename.indexOf(File.separator) < 0) {
					benchfileName.append(getAbsoluteFileName(STAFHelper.SAFS_VAR_BENCHDIRECTORY,filename));
				} else {
					benchfileName.append(getAbsoluteFileName(STAFHelper.SAFS_VAR_PROJECTDIRECTORY,filename));
				}
	        }else{
	        	benchfileName.append(filename);
	        }
  	        
  	        //Resolve the testfile name, which is always under directory STAFHelper.SAFS_VAR_TESTDIRECTORY
  	        //If the file name contain fileSperator "\", take the last file name.
  	        //For example, if filename is c:\dir\FILE or dir\FILE, we will take the FILE as filename
  	        int index = filename.lastIndexOf(File.separator);
  	        if(index > 0){
  	        	filename = filename.substring(index+1);
  	        }
  	        testfileName.append(getAbsoluteFileName(STAFHelper.SAFS_VAR_TESTDIRECTORY,filename));
		}
    } 
    /**
     * <b>Note:</b><br>
     * This is used internally by initialFileName().<br>
     * 
     * <b>Purpose:</b><br>
     * Use directory and filename to form a absolute file path<br>
     * 
     * @param directory
     * @param filename		
     * @return	File absolute path
     * @throws SAFSException
     */
    private String getAbsoluteFileName(String directory,String filename) throws SAFSException{
    	String debugMsg = getClass().getName() + "getAbsoluteFileNaem(): ";
    	String pdir = getVariable(directory);

    	if(pdir==null){
			String detail = failedText.convert(FAILStrings.STAF_ERROR,
					"SATF ERROR: Can not get variable "+directory,
					"Can not get variable "+directory);	
        	Log.debug(debugMsg+detail);		
        	throw new SAFSException(detail);
    	}

		return new CaseInsensitiveFile(pdir, filename).toFile().getAbsolutePath();
    }
}
