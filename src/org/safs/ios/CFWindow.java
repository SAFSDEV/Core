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
import java.awt.Polygon;
import java.util.Iterator;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.StatusCodes;
import org.safs.text.FAILStrings;

public class CFWindow extends ComponentFunction {

	/** "Window/" 
	 * Subdir off of Utilities.ROOT_JSCRIPTS_DIR containing Window keyword implementation scripts. */
	public static final String JSCRIPTS_WINDOW_SUBDIR="Window/";

	/** "pinch.js" implementation for supported Flick commands in JSCRIPTS_GENERICOBECT_SUBDIR. */
	public static final String JSCRIPTS_PINCH_SCRIPT="pinch.js";
	
	
	/** "PINCHOPEN" */
	public static final String COMMAND_PINCHOPEN = "PINCHOPEN";
	/** "PINCHCLOSE" */
	public static final String COMMAND_PINCHCLOSE = "PINCHCLOSE";

	protected String windowGUIID = null;
	
	public CFWindow() {
		super();
	}

	/**
	 * TestRecordData should already have most fields including the object recognition strings.
	 */
	public void process(){		
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		try{ 
			getHelpers();
		}catch(Exception x){//assuming no exceptions are actually thrown
			Log.debug("CFWindow.process unexpected getHelpers() "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		windowGUIID = null;
		if(action==null){
			Log.debug("CFWindow could not process NULL action command.");
			return;
		}		
		try {
			if((action.equalsIgnoreCase(COMMAND_PINCHOPEN))||
			   (action.equalsIgnoreCase(COMMAND_PINCHCLOSE))){
				doPinch();
			}
		} catch (SAFSObjectRecognitionException e) {
			Log.debug("CFWindow recognition string missing or invalid for: "+ e.getMessage());
			issueParameterValueFailure(e.getMessage());
		} catch (InstrumentsScriptExecutionException e){
			Log.debug("CFWindow "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueUnknownErrorFailure(e.getMessage());
		} catch (SAFSObjectNotFoundException e){
			Log.debug("CFWindow "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
									   e.getMessage() +" was not found.", e.getMessage()));
		}	
	}

	/**
	 * process the PinchOpen, PinchClose commands...
	 * Appends the following variables into trd.js:
	 * <pre>
	 * var origin={x:value, y:value, width:value, height:value};
	 * var target={width:value, height:value};
	 * var seconds=N;  (in seconds)
	 * </pre>
	 * If no seconds is specified than the default of 1 second will be used.
	 */
	protected void doPinch() throws SAFSObjectRecognitionException,
	                                InstrumentsScriptExecutionException,
    					            SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		Iterator param = null;
		String trdAppend = null;
		String mapkey = null;
		Polygon origin = null;
		Point target = null;
		int duration = 1;
		if (params.size() < 2){
			Log.debug(action +" in CFWindow insufficient parameters..");
			issueParameterCountFailure("Origin, Target");
			return;
		}
		param = params.iterator();
		mapkey = (String) param.next();			
		origin = lookupAppMapLineReference(mapkey);
		if(origin == null) origin=convertLine(mapkey);
		if(origin == null) {
			Log.debug(action +" Origin string missing or invalid: "+ origin);
			issueParameterValueFailure("Origin");
			return;
		}
		if (origin.xpoints[0] < 0) origin.xpoints[0]=0;
		if (origin.xpoints[1] < 0) origin.xpoints[1]=0;
		if (origin.ypoints[0] < 0) origin.ypoints[0]=0;
		if (origin.ypoints[1] < 0) origin.ypoints[1]=0;
		
		mapkey = (String) param.next();			
		target = lookupAppMapCoordReference(mapkey);
		if(target == null) target=convertCoords(mapkey);
		if(target == null) {
			Log.debug(action +" Target string missing or invalid: "+ target);
			issueParameterValueFailure("Target");
			return;
		}

		if(param.hasNext()){
			mapkey = (String) param.next();
			try{ duration = Integer.parseInt(mapkey);}
			catch(NumberFormatException x){
				Log.debug(action +" ignoring invalid Duration parameter:"+ mapkey +". Defaulting to "+ duration);
			}
		}
		trdAppend = "\nvar origin={\n";
		trdAppend += "x:"+origin.xpoints[0]+",\n";
		trdAppend += "y:"+origin.ypoints[0]+",\n";
		trdAppend += "width:"+origin.xpoints[1]+",\n";
		trdAppend += "height:"+origin.ypoints[1]+"};\n";
		Log.info(action +" using Origin: "+ origin.xpoints[0]+","+origin.ypoints[0]+", "+origin.xpoints[1]+","+origin.ypoints[1]);
				
		trdAppend += "\nvar target={\n";
		trdAppend += "width:"+target.x+",\n";
		trdAppend += "height:"+target.y+"};\n";
		Log.info(action +" using Target: "+ target.x+","+target.y);
				
		trdAppend += "\nvar seconds="+ duration +";\n";
		Log.info(action +" using seconds: "+ duration);

		IStatus stat = CFComponent.processIOSScript(JSCRIPTS_WINDOW_SUBDIR, JSCRIPTS_PINCH_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			if(origin == null){
				issuePassedSuccess(stat.comment); //comment might be empty
			}else{
				issuePassedSuccessUsing(Integer.toString(target.x) +","+ Integer.toString(target.y));
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
