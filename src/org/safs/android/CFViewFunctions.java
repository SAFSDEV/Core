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
package org.safs.android;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.image.ImageUtils;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64FormatException;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid GenericMaster and GenericObject functions.
 */
public class CFViewFunctions extends CFComponentFunctions {

	/**
	 * Default constructor. 
	 */
	public CFViewFunctions() {
		super();
	}
	
	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_view_click.equalsIgnoreCase(action)||
		   SAFSMessage.cf_view_tap.equalsIgnoreCase(action)  ||
		   SAFSMessage.cf_view_press.equalsIgnoreCase(action))		{
			_clickCommandResults(results);
		}else 
		if(SAFSMessage.cf_view_getguiimage.equalsIgnoreCase(action))		{
			_getGuiImageResults(results);
		}else 
		if(SAFSMessage.cf_view_guidoesexist.equalsIgnoreCase(action)||
		   SAFSMessage.cf_view_guidoesnotexist.equalsIgnoreCase(action)){
			_guiExistsResults(results);
		}else 
		if(SAFSMessage.cf_view_inputcharacters.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_view_inputkeys.equalsIgnoreCase(action) || 
		   SAFSMessage.cf_view_typechars.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_view_typekeys.equalsIgnoreCase(action)){
			_inputCharactersResults(results);
		}else 
		if(SAFSMessage.cf_view_capturepropertiestofile.equalsIgnoreCase(action)||
		   SAFSMessage.cf_view_verifypropertiestofile.equalsIgnoreCase(action)){
		   	_captureVerifyPropertiesToFileResults(results);
		}
	}	

	/**
	 * An ArrayList that contains a list of property names to be excluded when performing 
	 * VerifyPropertiesToFile.  This is provided for those properties that will always be 
	 * dynamic and fail on Verify commands.
	 * <p>
	 * By default, the current list of property names is:
	 * <p><ul>
	 * drawingTime
	 * </ul>
	 * <p>
	 * This ArrayList can be overwritten by external classes as needed.
	 */
	public static ArrayList _ignoreProperties = new ArrayList(
		Arrays.asList(new String[]{
			"drawingTime"
		}));

	/**
	 * Handles Results for CaptureObjectPropertiesToFile and VerifyPropertiesToFile
	 * 
	 * @param results
	 */
	protected void _captureVerifyPropertiesToFileResults(RemoteResults results) {
		String debugmsg = getClass().getName()+"._captureVerifyPropertiesToFileResults() ";
		boolean verify = SAFSMessage.cf_view_verifypropertiestofile.equalsIgnoreCase(action);
	    if ( params.size( ) < 1 ) {
	      this.issueParameterCountFailure("File");
	      setRecordProcessed(false);// not sure if necessary
	      return;
	    } 
	    Iterator aIterator = params.iterator();
	    String filename = ( String ) aIterator.next( );
		if((filename==null)||(filename.length()==0)){
	        this.issueParameterValueFailure("File");
	        setRecordProcessed(false);// not sure if necessary
	        return;
		}
		File fn;
	    filename = FileUtilities.normalizeFileSeparators(filename);	    
	    //build File
	    fn = new CaseInsensitiveFile(filename).toFile();
	    if (!fn.isAbsolute()) {
	    	String pdir = null;
	    	try{
		   		if( filename.indexOf(File.separator) > -1 ) {
			  		pdir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
		    	}else{
			   		pdir = verify ? getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY):getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
		    	}
	    	}catch(Exception x){}
	    	if ((pdir == null)||(pdir.equals(""))){
 	  		    String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
	  				"Could not get one or more variable values.")+ 
	  				" "+ STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY+", ";

 	  		    if(verify) error += STAFHelper.SAFS_VAR_BENCHDIRECTORY;
 	  		    else error += STAFHelper.SAFS_VAR_TESTDIRECTORY;
 	  		    
	  		    this.issueActionOnXFailure(filename, error);
		        setRecordProcessed(false);// not sure if necessary
	  		    return;
	    	}
		    fn = new CaseInsensitiveFile(pdir, filename).toFile();
	    }
	    try{
	    	Log.debug("CF "+ action +" filename resolves to: "+ fn.getAbsolutePath());}
	    catch(NullPointerException np){
	    	Log.debug("CF "+ action +" filename resolves to NULL");
	    }
	    //get optional Encoding parameter
	    String stemp = new String("");
	    String encoding = new String("");
	    try {	stemp   = (String) aIterator.next(); }
	    catch (Exception nse) {}
	    if(stemp.equals("")){
			Log.info ("Component: "+ action +" encoding not provided...");
	    }
		
		setRecordProcessed(true);
		String fnpath = (fn != null) ? fn.getAbsolutePath():null;
		if(encoding != null && encoding.length() < 1) encoding = null;
		
		try{
			if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
		    	Properties store = new Properties();
		    	CharArrayReader reader = new CharArrayReader(results.getStatusInfo().toCharArray());
		    	store.load(reader);
		    	Enumeration names = store.propertyNames();
		    	String name = null;
		    	String value = null;		    	
				Collection contents = new ArrayList();
		    	while(names.hasMoreElements()){
		    		name = (String) names.nextElement();
		    		value = store.getProperty(name);

		    		//some exclusions allowed
					if(SAFSMessage.cf_view_verifypropertiestofile.equalsIgnoreCase(action) &&
					   _ignoreProperties.contains(name)) continue;
		    		
					contents.add(name +"="+value);
		    	}
				if(SAFSMessage.cf_view_capturepropertiestofile.equalsIgnoreCase(action)){
	        		if(encoding != null){
	        			StringUtils.writeEncodingfile(fnpath, contents, encoding);
	        		}else{
	        			StringUtils.writefile(fnpath, contents);
	        		}
					issuePassedSuccessUsing(fnpath);
					return;
				}
				// VerifyPropertiesToFile
				Collection benchContents = null;
	    	    //If a file encoding is given or we need to keep the encoding consistent
	    	    if(encoding!=null){
	    	    	benchContents = StringUtils.readEncodingfile(fn.getAbsolutePath(), encoding);
	    	    }else{
	    	    	//Keep compatible with old version
	    	    	benchContents = StringUtils.readfile(fn.getAbsolutePath());
	    	    }
	        	if(contents.equals(benchContents)){
	       			issuePassedSuccess(passedText.convert(GENStrings.CONTENT_MATCHES_KEY, 
	    					"The contents of '"+ compName +"' matches the contents of '"+fnpath+"'.", 
	    					compName, fnpath));
	        	}else{
	        		//write "bad" contents to Test Directory
			   		String tdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);			   		
	        		File test = new CaseInsensitiveFile( tdir, fn.getName()).toFile();
	        		if(encoding != null){
	        			StringUtils.writeEncodingfile(test.getAbsolutePath(), contents, encoding);
	        		}else{
	        			StringUtils.writefile(test.getAbsolutePath(), contents);
	        		}
	    			issueErrorPerformingAction(passedText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY, 
	    					"The contents of '"+ compName +"@"+ test.getAbsolutePath() +"' does not match the contents of '"+fnpath+"'.", 
	    					compName +"@"+ test.getAbsolutePath(), fnpath));
	        	}
			}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
				// driver will log warning...
				setRecordProcessed(false);
				return;
			}else{ //object not found? what?
				logResourceMessageFailure();
				return;
			}					
		}catch(FileNotFoundException x){
			Log.debug(x.getMessage(), x);
			issueErrorPerformingActionOnX(fnpath, x.getMessage());
		}catch(IOException x){
			Log.debug(x.getMessage(), x);
			issueErrorPerformingActionOnX(fnpath, x.getMessage());
		}catch(NullPointerException x){
			Log.debug(x.getMessage(), x);
			issueErrorPerformingActionOnX(fnpath, x.getMessage());
		}catch(SAFSException x){
			Log.debug(x.getMessage(), x);
			issueErrorPerformingActionOnX(fnpath, x.getMessage());
		}
	}

	// *****************************************************************************

	/**
	 * Process the remote results following the execution of the GuiDoesExist command(s).
	 * @param results
	 */
	protected void _guiExistsResults(RemoteResults results){
		setRecordProcessed(true);
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
				return;
			issuePassedSuccess("");// no additional comment
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}
	
	/**
	 * Process the remote results following the execution of the click command(s).
	 * @param results
	 */
	protected void _clickCommandResults(RemoteResults results){
		setRecordProcessed(true);
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
				return;
			if(props.containsKey(SAFSMessage.PARAM_1)){
				issuePassedSuccessUsing(props.getProperty(SAFSMessage.PARAM_1));
			}else{
				issuePassedSuccess("");// no additional comment
			}
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			this.logResourceMessageFailure();
		}
	}
		
	/**
	 * Process the remote results following the execution of the getGuiImage command(s).
	 * @param results
	 */
	protected void _getGuiImageResults(RemoteResults results){
		String debugmsg = getClass().getName()+"._getGuiImageResults() ";		
	    if ( params.size( ) < 1 ) {
	      this.issueParameterCountFailure("OutputFile");
	      setRecordProcessed(false);// not sure if necessary
	      return;
	    } 
	    Iterator aIterator = params.iterator();
	    String filename = ( String ) aIterator.next( );
		if((filename==null)||(filename.length()==0)){
	        this.issueParameterValueFailure("OutputFile");
	        setRecordProcessed(false);// not sure if necessary
	        return;
		}
		File fn;
	    if (filename.equals("")) {
	      this.issueParameterValueFailure("OutputFile");
	      setRecordProcessed(false);// not sure if necessary
	      return;
	    }
	    
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
		        setRecordProcessed(false);// not sure if necessary
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
			    setRecordProcessed(false);// not sure if necessary
				return;
			}
	    }
		
		setRecordProcessed(true);
		String fnpath = (fn != null) ? fn.getAbsolutePath():null;
		
		// extract the Base64 encoded image
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			try{
				String data = results.getStatusInfo();
				ByteArrayInputStream instream = new ByteArrayInputStream(data.getBytes());
				File pngFile = File.createTempFile("remoteAndroidView", ".PNG");
				Log.debug(debugmsg+" Saving intermidate .png file as "+pngFile.getAbsolutePath());
				FileOutputStream outstream = new FileOutputStream(pngFile);
				Base64Decoder decoder = new Base64Decoder(instream, outstream);
				decoder.process();
				instream.close();
				outstream.close();
				
				//Read the png file as a BufferedImage
		    	BufferedImage viewImage = ImageIO.read(pngFile);
		    	BufferedImage viewSubImage = viewImage;
		    	pngFile.delete();
		    	
		        if (!subarea.equals("")) {
		    		//get Rectangle from SubArea parameter
		        	Rectangle originalArea = new Rectangle(viewImage.getMinX(),viewImage.getMinY(),
		        			                               viewImage.getWidth(),viewImage.getHeight());
		        	Rectangle modifiedArea = ImageUtils.getSubAreaRectangle(originalArea, subarea);
		        	Log.debug("Origianl Image Rect="+originalArea);
		        	Log.debug("Modified Image Rect="+modifiedArea);
		    		if (modifiedArea == null) {
		    			issueParameterValueFailure("SubArea="+subarea);
		    			return;
		    		}else{
				    	viewSubImage = viewImage.getSubimage(modifiedArea.x, modifiedArea.y, 
		                                                     modifiedArea.width, modifiedArea.height);	
		    		}
		        }
		        
		    	ImageUtils.saveImageToFile(viewSubImage, fn, 1.0F);

				if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
					if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
						return;
					issuePassedSuccessUsing(fnpath);
					return;
				}
				else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
					// driver will log warning...
					setRecordProcessed(false);
					return;
				}
				else{ //object not found? what?
					logResourceMessageFailure();
					return;
				}					
			}catch(Base64FormatException x){
				Log.debug(x.getMessage(), x);
				issueErrorPerformingActionOnX(fnpath, x.getMessage());
			}catch(FileNotFoundException x){
				Log.debug(x.getMessage(), x);
				issueErrorPerformingActionOnX(fnpath, x.getMessage());
			}catch(IOException x){
				Log.debug(x.getMessage(), x);
				issueErrorPerformingActionOnX(fnpath, x.getMessage());
			}catch(NullPointerException x){
				Log.debug(x.getMessage(), x);
				issueErrorPerformingActionOnX(fnpath, x.getMessage());
			} catch (SAFSException x) {
				Log.debug(x.getMessage(), x);
				issueErrorPerformingActionOnX(fnpath, x.getMessage());
			}
		}else{// base64 image was NOT returned!			
			logResourceMessageFailure();
		}						
	}

	/**
	 * Process the remote results following the execution of inputCharacters.
	 * @param results
	 */
	protected void _inputCharactersResults(RemoteResults results){
		setRecordProcessed(true);
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
				return;
			if(props.containsKey(SAFSMessage.PARAM_1)){
				issuePassedSuccessUsing(props.getProperty(SAFSMessage.PARAM_1));
			}else{
				issuePassedSuccess("");// no additional comment
			}
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			this.logResourceMessageFailure();
		}
	}		
}
