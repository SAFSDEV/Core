/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
/**
 * Developer Histories:
 *
 * As of OCT 31, 2005 there is a new Java JAI dependency added for compiling the
 * code or using the GetGUIImage command.  The download for JAI can be found at:
 * <pre>
 *     http://java.sun.com/products/java-media/jai/
 * </pre>
 * The build requires the following JAR files be in the build path:
 * <pre>
 *     jai_core.jar
 *     jai_codec.jar
 * </pre>
 * To actually use the new command and other future image related commands the
 * developer and\or user will need to install the Java JAI support if it does
 * not become part of our standard install.
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   AUG 19, 2003    (DBauman) Original Release
 *   <br>   SEP 02, 2005    (Bob Lawler) reverting to previous code w/ some
 *                            cosmetic cleanup.  Also commented out call to
 *                            getGuiImage() as it is clearly not working (RJl).
 *   <br>   OCT 17, 2005    (Carl Nagle) Added DoubleClick Support
 *   <br>   OCT 28, 2005    (Carl Nagle) Refactored for click variant extensions
 *   <br>   OCT 31, 2005    (Carl Nagle) Adding a generic GetGUIIMage submitted by
 *                                   Bob Lawler.  Adds a new Java JAI dependency.
 *   <br>   AUG 08, 2006    (PHSABO) Moved getSubAreaRectangle and getClippedSubAreaRectangle
 *   								 helper functions to Processor superclass.
 *   <br>   MAR 11, 2008    (JunwuMa)Added HoverMouse,CtrlClick,CtrlRightClick and ShiftClick support
 *   <br>   MAR 11, 2008    (Lei Wang)Added SetPropertyValue, VerifyGUIImageToFile support
 *   <br>   MAR 26, 2008    (Lei Wang)Modified GetGuiImage, VerifyGUIImageToFile
 * 	 <br>   MAR 26, 2008    (Lei Wang)Added HScrollTo, VScrollTo
 *   <br>   APR 21, 2008    (JunwuMa)Added SelectMenuItem, SelectMenuItemContains
 *   <br>                            VerifyMenuItem, VerifyMenuItemContains 
 *   <br>   APR 22, 2008    (JunwuMa)Added SetPosition 
 *   <br>   MAY 8,  2008    (JunwuMa)Added LeftDrag and RightDrag
 *   <br>   JUL 11, 2008	(Lei Wang)Add static method isJavaDomain(), isDotnetDomain(), isHtmlDomain(), isWinDomain(), isSwtDomain()
 *   <br>   JUL 31, 2008	(Lei Wang)Modify method verifyMenuItem(),selectMenuItem()
 *   								 Add method findMenuBars()
 *   <br>	AUG 11, 2008	(Lei Wang)Modify method performScorll() to support .NET application
 *   <br>   AUG 11, 2008    (JunwuMa)Add .NET support for setPropertyValue,CaptureObjectDataToFile and VerifyObjectDataToFile.
 *   <br>	NOV 11, 2008	(Lei Wang)Added method getAbsolutFileName(),capturePropertyToFile()
 *   								 Modified method verifyArrayPropertyToFile(), see defect S0543643.
 *   <br>	JAN 12, 2009	(Lei Wang)Modify method performScorll() to support FLEX application
 *   <br>	JAN 20, 2009    (JunwuMa)Modify methods findMenuBars, selectMenuItem, matchedPathOfMenuItem and verifyMenuItem to support keywords on FLEX menu bar.
 *   <br>	FEB 20, 2009	(JunwuMa)Modify inputkeys() to support FLEX for keywords Inputkeys and InputCharacters.
 *   <br>   MAY 01, 2009    (Carl Nagle) Adding Float value support for numeric parameters.
 *   <br>   JUN 12, 2009    (Lei Wang)Modify method:findMenuBars(),matchedPathOfMenuItem(),selectMenuItem(),verifyMenuItem().
 *    								 For supporting the menu (type is .Menubar) of win domain.
 *   <br>   JUN 30, 2009    (Girish Kolapkar) Added LeftDrag variations using MouseModifiers
 *   <br>   NOV 03, 2009    (JunwuMa)Added method findMenuBarsForWPF() as a workaround for finding the menu bars of a DotNet WPF application.
 *   <br>   NOV 12, 2009    (Carl Nagle) Attempt to catch Exceptions resulting from intended Window closures.
 *   <br>   FEB 05, 2010    (Carl Nagle) Adding SendEvent support for Flex. FlexObjectTestObject.performAction() is 
 *                                   only supported in RFT 8.1 or later.
 *   <br>	FEB 25, 2010    (JunwuMa)Added keywords GetTextFromGUI and SaveTextFromGUI for detecting text on GUI 
 *                                   using OCR.
 *   <br>   APR 14, 2010    (JunwuMa)Adding keyword LocateScreenImage for RJ.
 *   <br>   APR 20, 2010    (Lei Wang)Modify method action_GetSaveTextFromGUI(): use static method of OCREngine to get
 *                                   an OCR engine to use.
 *   <br>   JUN 30, 2011	(Dharmesh4) Added new Keyword MouseClick for RJ. 
 *   <br>   SEP 07, 2015    (Lei Wang) Correct a typo, change method preformDrag to performDrag. Modify comments: developer history will not show in java doc.
 **/
package org.safs.rational;

import java.awt.AWTError;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.safs.ComponentFunction;
import org.safs.DDGUIUtilities;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.Tree;
import org.safs.rational.win.CFWinMenuBar;
import org.safs.rational.wpf.CFWPFMenuBar;
import org.safs.robot.Robot;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.RationalTestException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.UnsupportedActionException;
import com.rational.test.ft.object.interfaces.DomainTestObject;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.ScrollTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexScrollBaseTestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.FlexScrollDetails;
import com.rational.test.ft.script.FlexScrollDirections;
import com.rational.test.ft.script.LowLevelEvent;
import com.rational.test.ft.script.RationalTestScriptConstants;
import com.rational.test.ft.script.Subitem;
import com.rational.test.ft.script.SubitemFactory;
import com.rational.test.ft.value.MethodInfo;

/**
 * CFComponent, process a generic component to handle 
 * <a href="/sqabasic2000/GenericMasterFunctionsIndex.htm">Generic Master Keywords</a>
 * and <a href="/sqabasic2000/GenericObjectFunctionsIndex.htm">Generic Object Keywords</a><br>
 **/
public class CFComponent extends ComponentFunction {

  protected Script script;
  protected TestObject winObject;
  protected TestObject obj1;//compObject

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
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFComponent () {
    super();
    setKeepEncodingConsistent();
  }

  protected void getHelpers() throws SAFSException{
    getHelpersWorker();
    script = ((RTestRecordData)testRecordData).getScript();
    if (action.equalsIgnoreCase("InputKeys")||
    	action.equalsIgnoreCase("InputCharacters")){
    	String comprec = "";
    	try{
    		comprec = testRecordData.getCompGuiId();
    		if (comprec.equalsIgnoreCase("CurrentWindow")) {
        		Log.info("CFComponent InputKeys or InputCharacters using 'CurrentWindow'");
        		obj1 = null;
    			return;
    		}
    	}catch(Exception x){
    		//ignoring since it means it is not set
    		Log.info("CFComponent InputKeys or InputCharacters likely NOT using 'CurrentWindow'");
    	}
    }
    // DON'T do this unless the action will go back to the parent   
    if (!action.equalsIgnoreCase(GUIDOESEXIST) &&
        !action.equalsIgnoreCase(GUIDOESNOTEXIST) &&
        !action.equalsIgnoreCase(VERIFYTEXTFILETOFILE) &&
        !action.equalsIgnoreCase(VERIFYFILETOFILE) &&
        !action.equalsIgnoreCase(VERIFYBINARYFILETOFILE) &&
        !action.equalsIgnoreCase(VERIFYVALUES) &&
        !action.equalsIgnoreCase(VERIFYVALUESIGNORECASE) &&
        !action.equalsIgnoreCase(VERIFYVALUEEQUALS) &&
        !action.equalsIgnoreCase(VERIFYVALUECONTAINS) &&
        !action.equalsIgnoreCase(VERIFYCLIPBOARDTOFILE)) {
    	
    	winObject = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, windowName);
    	obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName);
    }
  }

  /** <br><em>Purpose:</em>  do the processing.  Children of us can us this method to
   ** call their own 'localProcess' method
   * <br><em>Assumptions:</em>
   * <br> sequence:
   * <br> testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
   * <br> localProcess();
   * <br> if (testRecordData.getStatusCode() === StatusCodes.SCRIPT_NOT_EXECUTED) {
   * <br>      componentProcess(); // CFComponent will do the work
   * <br> }
   **/
  public void process() {
    try{ getHelpers();}
    catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getClass().getName()+", msg: "+ex.getMessage(),
                     FAILED_MESSAGE);
      return;
    }
    // assume this for now..
    testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
    // do the work
    localProcess();
    // only do if not done locally
    if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
      componentProcess(); //handle Generic keywords
    } else {
      /*
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+".process: "+testRecordData,
                     DEBUG_MESSAGE);
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+".process: params:"+params,
                     DEBUG_MESSAGE);
      */
    }
  }

  /** no implementation, just here so that our children know the 'protected' interface **/
  protected void localProcess(){}

	/**
	 * Parse a string to determine 'where' something should happen on a given component.
	 * This is generally x,y coordinates, but subclasses can override this to provide
	 * other options.  This is most often called by the CLICK commands.
	 * <p>
	 * Supported formats of whereinfo:
	 * <p><ul>
	 * <li>Coords=x,y
	 * <li>x,y     (same as Coords=x,y)
	 * <li>Coords=x;y
	 * <li>x;y     (same as Coords=x;y)
	 * </ul>
	 * @param whereinfo String usually retrieved from the test table.
	 * @return java.awt.Point or null.  Subclasses may return other object
	 * types as they deem appropriate.
	 */
	protected Object parseWhereInfo(String whereinfo){

	    if (whereinfo == null) return null;
		whereinfo.trim();
		if (whereinfo.length()==0) return null;

	    // get the optional app map lookup reference
	    String lookupval = lookupAppMapReference(whereinfo);

	    //lookupval will be null if no AppMapReference found.
	    //then use whereinfo as literal string equivalent.
	    if ((lookupval==null)||(lookupval.length()==0))
	    	 lookupval = whereinfo;

	    String uclookupval = lookupval.toUpperCase();

	    // Coords=x,y  (or just x,y)
	    // coords=x;y  (or just x;y)
	    int coordsindex = uclookupval.indexOf("COORDS=");

	  	String coords = uclookupval;
	  	if (coordsindex >= 0){
	  	  	if(uclookupval.length() > 8)
	  	  	  	coords = uclookupval.substring(coordsindex+7);
	  	}
	  	//we should now have x,y  or  x;y (if proper input provided)
  	  	Log.info("CF:evaluating coordinates: "+coords);

  	  	String sepchar = (coords.indexOf(',')>0) ? "," : ";" ;
  		StringTokenizer tokens = new StringTokenizer(coords, sepchar);
		try{
  			// check x,y
  			if(tokens.countTokens()==2){
  	  			int x = (int) Float.parseFloat(tokens.nextToken().trim());
  	  			int y = (int) Float.parseFloat(tokens.nextToken().trim());
  	  			return new java.awt.Point(x,y);
  			}
		}catch(Exception ex){ }

		log.logMessage(testRecordData.getFac(),
           	failedText.convert("ignore_bad_param", action +" ignoring invalid parameter '"+ whereinfo +"'",
            action, whereinfo));
		return null;
	}

	/**
	 * called from sendEvent for Flex Domain sendEvent handling.
	 */
	protected void sendFlexEvent(){
		if (params.size()<1) {
	    	issueParameterValueFailure("EVENT");
	        return;
	    }	    
	    int argscount = params.size()-1;
	    Iterator iter = params.iterator();
		String event = (String) iter.next();
		
		FlexObjectTestObject fobj = null;
		try{
			fobj = (FlexObjectTestObject) obj1;
		}catch(Exception cc){
			Log.debug("CFComponent.sendFlexEvent IGNORING "+  cc.getClass().getSimpleName()+" "+ cc.getMessage());
			try{
				fobj = new FlexObjectTestObject(obj1);
			}catch(Exception x){
				Log.debug("CFComponent.sendFlexEvent ABORTING due to "+  x.getClass().getSimpleName()+" "+ x.getMessage());
				return; //SCRIPT_NOT_EXECUTED
			}
		}		
		String strargs = "";
		if(argscount==0){
			Log.info("CFComponent.sendFlexEvent '"+ event +"' with no arguments.");
			try{ fobj.performAction(event);}
			catch(Exception x){
				Log.debug("CFComponent.sendFlexEvent ERROR in performAction "+ x.getClass().getSimpleName()+" "+ x.getMessage());
				issueActionFailure(event+": "+ x.getMessage());
				return;
			}
		}else{
			String[] args = new String[argscount];
			for(int i=0;i<argscount;i++){
				try{args[i] = (String) iter.next();}
				catch(Exception x){ args[i] = new String();}
				if(i > 0) strargs += ", ";
				strargs += args[i];
			}
			Log.info("CFComponent.sendFlexEvent '"+ event +"' with arguments: '"+ strargs +"'");
			try{ fobj.performAction(event, args);}
			catch(Exception x){
				Log.debug("CFComponent.sendFlexEvent ERROR in performAction "+ x.getClass().getSimpleName()+" "+ x.getMessage());
				issueActionFailure(event+": "+ x.getMessage());
				return;
			}
		}
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		String using = event;
		if(strargs.length()> 0) using +=": "+ strargs;			
		issuePassedSuccessUsing(using);		
	}
	
	/**
	 * send an event to the object--assuming it supports receiving events.
	 * Initially, this is being implemented to support the Flex "performAction" 
	 * functionality, but this can readily be expanded to other component types and 
	 * technologies.
	 * <p>
	 * In general, the command as initially implemented for Flex expects there to be an 
	 * event name followed by 0 or more parameters.  These parameters will be different 
	 * and defined by the event or technology.
	 * @exception SAFSException
	 * @author Carl Nagle 
	 */
	protected void sendEvent() throws SAFSException {

		//currently we only SendEvent to FlexObjects
	    if (! isFlexDomain(obj1)) return; //SCRIPT_NOT_EXECUTED	
	    sendFlexEvent();
	}

  /**
   * perform SelectMenuItem and SelectMenuItemContains
   * @param fuzzy, false: select the menu item that exactly matches the gaven path 
   * @exception SAFSException
   * @author Carl Nagle trying to intercept TargetGoneException when selection intends to close window
   */
  protected void selectMenuItem(boolean fuzzy) throws SAFSException {
    String debugInf = getClass().getName()+".selectMenuItem() ";
        
    if (params.size()<1) {
        paramsFailedMsg(windowName, compName);  
        return;
    }
    // get the path of menu item
    String path = (String) params.iterator().next();
  	GuiTestObject guiObj = new GuiTestObject(obj1);          

  	TestObject[] children = findMenuBars(guiObj);

    if(children!=null) {
        // visit every menubar until finding the matched menu item; 
        for (int count = 0; count<children.length; count++) {
           	try {
           		String matchedPath = null;
           		if (isJavaDomain(children[count]) || isDotnetDomain(children[count]) || isWinDomain(children[count])){
           			GuiSubitemTestObject menuBarGuiObj = new GuiSubitemTestObject(children[count].getObjectReference());

           			//see if current menubar contains the path of menu item. status is null 
           			matchedPath = matchedPathOfMenuItem(menuBarGuiObj, path, fuzzy, null);
           			Log.info("...matched path: "+matchedPath);		    
           			if (matchedPath == null) continue;  //no match on path  
            	
           			com.rational.test.ft.script.List list = Script.localAtPath(matchedPath);
           			if (list == null) break;

            		try{
            			menuBarGuiObj.click(list);
            		}catch(TargetGoneException e){
            			Log.info("...IGNORING TargetGoneException usually resulting from Window closure after select...");
            		}catch(Exception e){
            			Log.debug("can not select path "+path +" due to "+e.getClass().getSimpleName());
            			continue;
            		}
           		} else if(isFlexDomain(children[count])) {
               		matchedPath = matchedPathOfMenuItem((GuiTestObject)children[count], path, fuzzy, null);
               			
              		Log.info("...matched path: "+matchedPath);	
                    if (matchedPath == null) continue;  //no match on path    
                       	
               		FlexUtil.doSelectMenubar(children[count], matchedPath);
               	} else {
               		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
               		log.logMessage(testRecordData.getFac(),getClass().getName() + ":" + action + 
               				": does not make sense for other domains than Java, DotNet and Flex",
               		        FAILED_MESSAGE);
               		return;
               	}
           		testRecordData.setStatusCode(StatusCodes.OK);
           		log.logMessage(testRecordData.getFac(),
              		               genericText.convert(TXT_SUCCESS_3a, altText, windowName, compName, action, matchedPath),
              		               PASSED_MESSAGE);
          		return;
            } catch (Exception e) {
                String msg = debugInf+e.getMessage();
                Log.debug(msg);  
                throw new SAFSException(msg);
            } 
        } // end of while
    }
    
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	log.logMessage(testRecordData.getFac(),getClass().getName()+":"+action+": no matched menu item on: "+path,
	        FAILED_MESSAGE);
  }
  
  /**
   * perform actions for keywords verifyMenuItem and verifyMenuItemContains
   * @param fuzzy, to do exactly match if false, otherwise do fuzzy match 
   * @throws SAFSException
   */
  protected void verifyMenuItem(boolean fuzzy) throws SAFSException {
    String debugInf = getClass().getName()+".verifyMenuItem() ";
      
    if (params.size()<2) {
        paramsFailedMsg(windowName, compName);  
        return;
    }
    // 1st param is the path of menu item
    String path = (String) iterator.next();
    // 2nd param is the status string of menu item
    String strStatus = (String) iterator.next();
    GuiTestObject guiObj = new GuiTestObject(obj1);          
		
    TestObject[] children = findMenuBars(guiObj); 

	if(children!=null) {
  	    // visit every menubar captured until finding the matched menu item; 
        for (int count = 0; count<children.length; count++) {
            GuiTestObject menuBarGuiObj = null; 
          	try {
           		if (isJavaDomain(children[count]) || isDotnetDomain(children[count]) || isWinDomain(children[count])){
           			menuBarGuiObj = new GuiSubitemTestObject(children[count].getObjectReference());
           		} else if(isFlexDomain(children[count])) { 
           			menuBarGuiObj = (GuiTestObject)children[count];
           		}
          		
          	    //see if current menubar contanis the path of menu item. 
          	    String matchedPath = matchedPathOfMenuItem(menuBarGuiObj, path, fuzzy, strStatus);
          	    Log.info("...item status: " +strStatus);
          	    Log.info("...matched path: "+matchedPath);   
          	    if (matchedPath == null) // no match on path with strStatus
          	        continue;
          	    else {
          	        testRecordData.setStatusCode(StatusCodes.OK);
          	        log.logMessage(testRecordData.getFac(),
          		                 genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action+" on "+path),
          		                 PASSED_MESSAGE);
          	        return;
          	    }
        	} catch (SAFSException se) {
        	    String msg = debugInf+se.getMessage();
        	    Log.debug(msg);  
        	    throw new SAFSException(msg);
        	}   
        }
    }
  	
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	log.logMessage(testRecordData.getFac(),getClass().getName()+":"+action+": no match on: "+ path+" Status: " + strStatus ,
	          FAILED_MESSAGE);   
  }
 
  /**
   * perform LeftDrag or RightDrag on component moving from (x1,y1) to (x2,y2).
   * Format: "T,SwingApp,component,LeftDrag,"Coords=x1;y1;x2;y2" 
   * @exception SAFSException
   */
  protected void performDrag() throws SAFSException{
      String debugInf = StringUtils.debugmsg(false);
      if (params.size()<1) {
            paramsFailedMsg(windowName, compName);  
            return;
      }
      // format of preset: Coords=0,0,640,480       
      String preset = (String)params.iterator().next();
      Log.info("...params for "+action+" :"+preset);  
      String rPreset = lookupAppMapReference(preset);
      if (rPreset != null)
          preset = rPreset; 
      
      try { 
    	  //Carl Nagle replaced case-sensitive test of "Coords="
    	  String[] strip = preset.split("=");
    	  if (strip.length > 1) preset = strip[1];
          preset = preset.replace(";",",");
          
          StringTokenizer tokens = new StringTokenizer(preset, ",");
          int x1      = (int) Float.parseFloat(tokens.nextToken().trim());
          int y1      = (int) Float.parseFloat(tokens.nextToken().trim());
          int x2      = (int) Float.parseFloat(tokens.nextToken().trim());
          int y2      = (int) Float.parseFloat(tokens.nextToken().trim());
          
          // do the work 
          GuiTestObject guiObj = new GuiTestObject(obj1);
          
          // have to test if x2,y2 is outside the bounds of the current TestObject
          // if it is, then we may have to modify coords to be screen coords
          Point point1 = new java.awt.Point(x1, y1);
          Point point2 = new java.awt.Point(x2, y2);
          Rectangle objRect = guiObj.getScreenRectangle();
          boolean useScreenCoords = (x2 < 0 || x2 > objRect.width || y2 < 0 || y2 > objRect.height);
          if(useScreenCoords) {
        	  point2.x = objRect.x + x2;
        	  point2.y = objRect.y + y2;
          }

          String msg = " :"+action+" from "+point1.toString()+" to "+point2.toString();
          Log.info("..."+msg);          
          if (action.equalsIgnoreCase(LEFTDRAG)){
              if (useScreenCoords ) 
            	  guiObj.dragToScreenPoint(Script.LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(SHIFTLEFTDRAG)){ 
        	  if (useScreenCoords ) 
        		  guiObj.dragToScreenPoint(Script.SHIFT_LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.SHIFT_LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(CTRLSHIFTLEFTDRAG)){ 
        	  if (useScreenCoords ) 
        		  guiObj.dragToScreenPoint(Script.CTRL_SHIFT_LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.CTRL_SHIFT_LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(CTRLLEFTDRAG)){ 
        	  if (useScreenCoords ) 
        		  guiObj.dragToScreenPoint(Script.CTRL_LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.CTRL_LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(ALTLEFTDRAG)){ 
        	  if (useScreenCoords ) 
        		  guiObj.dragToScreenPoint(Script.ALT_LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.ALT_LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(CTRLALTLEFTDRAG)){ 
        	  if (useScreenCoords ) 
        		  guiObj.dragToScreenPoint(Script.CTRL_ALT_LEFT,point1,point2); 
              else
            	  guiObj.drag(Script.CTRL_ALT_LEFT,point1,point2);
          }else if (action.equalsIgnoreCase(RIGHTDRAG)){          
              if (useScreenCoords ) 
            	  guiObj.dragToScreenPoint(Script.RIGHT,point1,point2); 
              else
            	  guiObj.drag(Script.RIGHT,point1,point2);
          }else{
        	  throw new SAFSException(action+" Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
          }
          
          testRecordData.setStatusCode(StatusCodes.OK);
          log.logMessage(testRecordData.getFac(),
      	               genericText.convert(TXT_SUCCESS_4, altText+msg, windowName, compName, action, msg),
      	               PASSED_MESSAGE);	    
  	    
      } catch (Exception e) {
          Log.debug(debugInf+e.getMessage());
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          componentFailureMessage(e.getMessage());
      }       
  }
  /** Note: guiObj is a GuiSubitemTestObject in Java or DotNet domains; a FlexMenuBarTestObject in Flex domain.
   */
  protected String matchedPathOfMenuItem(GuiTestObject guiObj, String path,boolean fuzzy, String status) throws SAFSException {
    MenuTree atree = null;
    atree = (MenuTree) utils.extractMenuBarItems(guiObj);
    Log.debug(testRecordData.getFac()+":"+testRecordData.getCommand()+" atree: "+atree);
    if(atree==null){
    	return null;
    }
    //do the work of matching..., verify the path
    return atree.matchPath(path, fuzzy, status);
  }            
  
  
  /** A workaround for finding menubar in WPF application
   * @param guiObj, a test object that represents WPF application
   * @param foundObjs, an empty ArrayList as input, any matching menu bar will be put into this ArrayList as return.  
   * */
  private void findMenuBarsForWPF(TestObject guiObj, ArrayList foundObjs) {
	  TestObject[] children = guiObj.getMappableChildren(); 
   	  for (int i = 0; i<children.length; i++) {
   		  try {
   			  String classname = (String)children[i].getProperty(".class");
   			  if (classname.equals(CFWPFMenuBar.CLASS_MENU_NAME) ||	
   				  classname.equals(CFWPFMenuBar.CLASS_CONTEXTMENU_NAME))
   				  foundObjs.add(children[i]);
   		  }catch(Exception e) {}
   		  findMenuBarsForWPF(children[i], foundObjs);
   	  }	  
  }
  /**
   * <em>Note:</em>		Only java-swing and .NET are supported.
   * @param guiObj		A test object represents an application window from which
   * 					we try to get menu bars.
   * @return			An array of MenuBars found from parameter guiObj
   */
  protected TestObject[] findMenuBars(TestObject guiObj){
	String debugmsg = getClass().getName()+".findMenuBars() ";
	TestObject[] children = null;
	
	//Find MenuBar components. Now, only java swing and .NET applications are supported.
    if(isDotnetDomain(guiObj)){
        	//TODO if the .net menubar inherits from the two standard menubar class, our code here will not work
        	//need to find a better way.
    		children = guiObj.find(Script.atDescendant(".class",CFDotNetMenuBar.CLASS_MENUSTRIP_NAME));
        	if(children.length == 0){
        		children = guiObj.find(Script.atDescendant(".class",CFDotNetMenuBar.CLASS_MAINMENU_NAME));
        	}
        	// consider WPF object
    		//API, find(atDescendant) doesn't work for WPF TestObject in RFT 8.0.0.2.
    		//using a workaround instead.  
        	if(children.length == 0) {
	        	ArrayList wpfmenulist = new ArrayList();
	        	try {
	        		findMenuBarsForWPF(guiObj, wpfmenulist);
	        	}catch(Exception e){}

	        	TestObject [] objArray = new TestObject[wpfmenulist.size()];
        		children = (TestObject [])wpfmenulist.subList(0, wpfmenulist.size()).toArray(objArray);
        	}
    }else if(isWinDomain(guiObj)){
    	//For win domain application
    	children = guiObj.find(Script.atDescendant(".class",CFWinMenuBar.CLASS_MENUBAR_NAME));
    	if(children==null || children.length==0)
        	children = guiObj.find(Script.atDescendant(".class",CFWinMenuBar.CLASS_POPUPMENU_NAME));
    }else if(isJavaDomain(guiObj)){
    	//For swing application, the subclass of JMenuBar will have the same value for property "uIClassID"
    	children = guiObj.find(Script.atDescendant(CFMenuBar.UITYPE_PROPERTY,CFMenuBar.UITYPE_MENUBAR));
    }else if(this.isFlexDomain(guiObj)){
    	children = guiObj.find(Script.atDescendant("className",FlexUtil.FLEX_MENUBAR_CLASSNAME));
    }else{
    	Log.debug(debugmsg+testRecordData.getCommand()+" is not supported for domains other than Java, .NET, Flex, Win");
    }
    
    return children;
  }
  
  /**
   * Checks for optional coords parameter and then routes to performClick.
   * Some subclasses may add additional functionality for the optional
   * argument string.  For example, CFTable supports "Row=n;Col=n".
   * Subclasses may override componentClick or other methods to detect
   * such extensions and route accordingly.
   **/
  protected void componentClick () {
    java.awt.Point point = checkForCoord(iterator);
    // ready to do the click
    performClick(point);
  }

  /**
   * Perform a standard Click, RightClick, or DoubleClick on the component.
   * Subclasses may override this param
   * @param point java.awt.Point x,y coords relative to the component or null.
   */
  protected void performClick(Object point){
    GuiTestObject guiObj = new GuiTestObject(obj1);
    try{
	    if ((point != null)&&(point instanceof java.awt.Point)) {
	    	java.awt.Point awtpoint = (java.awt.Point) point;
	        Log.info("clicking point: "+point);
	        try{
		        if (action.equalsIgnoreCase(CLICK) ||
	                action.equalsIgnoreCase(COMPONENTCLICK)) {
				    guiObj.click(awtpoint);
		        } else if (action.equals(MOUSECLICK)){
		        	mouseClick(guiObj,awtpoint);
	        	} else if (action.equalsIgnoreCase(DOUBLECLICK)) {
	          		guiObj.doubleClick(awtpoint);
		        } else if (action.equalsIgnoreCase(RIGHTCLICK)) {
	          		guiObj.click(Script.RIGHT, awtpoint);
	            } else if (action.equalsIgnoreCase(CTRLCLICK)) {  //where we handle CTRL_LEFT CTRL_RIGHT --junwu
	            	guiObj.click(Script.CTRL_LEFT, awtpoint);
	            } else if (action.equalsIgnoreCase(CTRLRIGHTCLICK)) {  //start
	            	guiObj.click(Script.CTRL_RIGHT, awtpoint);
	            } else if (action.equalsIgnoreCase(SHIFTCLICK)) {  //start
	            	guiObj.click(Script.SHIFT_LEFT, awtpoint);
	            }
	        }catch(TargetGoneException te){
	        	Log.info("...performClick IGNORING "+ te.getClass().getSimpleName()+
   			             " likely resulting from intentional window shutdown...");
	        }

	        
	        String use = "X="+  String.valueOf(awtpoint.x) +
	                     " Y="+ String.valueOf(awtpoint.y);
	        altText = windowName+":"+compName+" "+action+" successful using "+ use;
		    // set status to ok
		    log.logMessage(testRecordData.getFac(),
		                   genericText.convert("success3a", altText, windowName, compName, action, use),
		                   GENERIC_MESSAGE);
		    testRecordData.setStatusCode(StatusCodes.OK);
	    } else {
	    	Rectangle rect = null;
	    	Point center = null;
	    	try{
	    		// it is uncertain that this CENTER calculation is accurate or even 
	    		// coming from the correct originating Property.  To date the code 
	    		// using this has not been hit during runtime.  It was inserted in response to 
	    		// UnsupportedActionExceptions thrown by RFT saying it could not locate the 
	    		// screen coordinates of the object to be clicked.  However, the Exception 
	    		// was not hit since the code was inserted.  This may be related to issues with 
	    		// RFT on Dual-Monitor systems.
	    		//rect = (Rectangle)guiObj.getProperty(".bounds");
	    		//center = new Point(new Double(rect.getCenterX()).intValue(), new Double(rect.getCenterY()).intValue());
	    		rect = guiObj.getScreenRectangle();
	    		center = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
	    	} 
	    	catch(Exception x2){ Log.info("CFComponent could not extract .bounds for backup.");}			    	
	        
	    	// the CATCH blocks for each action below are draft versions.
	    	// I don't think they have ever been hit at runtime.
	    	Log.info("clicking center.");
	        if (action.equalsIgnoreCase(CLICK) ||
                action.equalsIgnoreCase(COMPONENTCLICK)) {
			    try{
			    	try{ guiObj.click(); }			    
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.click(center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }

        	} else if (action.equalsIgnoreCase(DOUBLECLICK)) {
          		try{
          			try{ guiObj.doubleClick();}          		
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.doubleClick(center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }
	        } else if (action.equalsIgnoreCase(RIGHTCLICK)) {
          		try{
          			try{guiObj.click(Script.RIGHT);}          		
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.click(Script.RIGHT, center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }
	    	} else if (action.equalsIgnoreCase(CTRLCLICK)) {  //where we handle CTRL_LEFT CTRL_RIGHT --junwu
	    		try{
	    			try{guiObj.click(Script.CTRL_LEFT);}	    		
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.click(Script.CTRL_LEFT, center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }
            } else if (action.equalsIgnoreCase(CTRLRIGHTCLICK)) {  //start
            	try{
            		try{guiObj.click(Script.CTRL_RIGHT);}            	
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.click(Script.CTRL_RIGHT, center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }
            } else if (action.equalsIgnoreCase(SHIFTCLICK)) {  //start
            	try{
            		try{guiObj.click(Script.SHIFT_LEFT);}
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;
				    	guiObj.click(Script.SHIFT_LEFT, center);
				    }
			    }catch(TargetGoneException te){
			    	Log.info("..."+ action +" IGNORING "+ te.getClass().getSimpleName()+
	    			         " likely resulting from intentional window shutdown...");
			    }
            } else if (action.equalsIgnoreCase(MOUSECLICK))	{ // start 
            		      		
            		try{ mouseClick(guiObj,center);}          		
				    catch(UnsupportedActionException x){ // cannot deduce coords sometimes UnsupportedActionException
				    	Log.debug(action +" "+x.getClass().getSimpleName()+" handling...");
				    	if (center==null) throw x;	    	
				    }
            }
		    // set status to ok
		    log.logMessage(testRecordData.getFac(),
		                   genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action),
		                   GENERIC_MESSAGE);
		    testRecordData.setStatusCode(StatusCodes.OK);
        }
    }catch(Exception x){
	    Log.info(action +" failed. "+x.toString());
	    // Carl Nagle this probably needs to be changed to logging a full-fledged failure! 
	  	//testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
   		issueErrorPerformingActionOnX(compName, x.getClass().getSimpleName()+": "+ x.getMessage());
   		return;
    }
  }


  /****************************************************************************
   * <br><em>Purpose:</em> closeWindow
   ***************************************************************************/
  protected void closeWindow () {
    TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
    
    try{ parent.close();}
    catch(RationalTestException te){
    	Log.info("...closeWindow IGNORING "+ te.getClass().getName()+
    			 " likely resulting from intentional window shutdown...");
    }
    catch(RuntimeException te){
    	Log.info("...closeWindow IGNORING Runtime "+ te.getClass().getName()+
    			 " likely resulting from intentional window shutdown...");
    }

    // THESE XDE "unregister" MECHANISMS MAY BE DEFECTIVE TO SOME DEGREE
    // I'VE BEEN GETTING 4 MINUTE RESPONSE TIMES FROM A SINGLE FUNCTION CALL.
    // AND NO CPU USAGE.  JUST 4 MINUTES OF NOTHING HAPPENING!
    //script.localUnregisterAll();
    //try{obj1.unregister();}catch(Exception any){;}
    //try{if (obj1 != parent) parent.unregister();}catch(Exception any){;}

    // set status to ok
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /**
   * If the application is .NET, and its windows has been maximized.<br>
   * RFT method restore() can't NOT restore the window.<br>
   * But we can call minimize() and activate() to make the window NOT<br>
   * maximized, the the restore() method can work correctly.<br>
   * 
   * @param parent
   */
  private void prepareForRestore(TopLevelTestObject parent) {
		if (isDotnetDomain(parent)) {
			try{
				parent.minimize();
				parent.activate();
			}catch(Exception e){
				Log.warn(getClass().getName()+".prepareForRestore(): "+e.getMessage());
			}
		}
  }

  /** <br><em>Purpose:</em> restore
   **/
  protected void _restore() throws SAFSException{
	  String debugmsg = StringUtils.debugmsg(getClass(), "_restore");
	  try{
		  TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
		  prepareForRestore(parent);
		  parent.restore();
	  }catch(Exception e){
		  String msg = "Fail to resotre window due to Exception "+e.getMessage();
		  Log.error(debugmsg+msg);
		  throw new SAFSException(msg);
	  }    
  }
  
  protected void _setPosition(Point position) throws SAFSException {
	  String debugmsg = StringUtils.debugmsg(getClass(), "_setPosition");
	  try{
		  TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
		  parent.move(position);
	  }catch(Exception e){
		  String msg = "Fail to set window to position "+position+", due to Exception "+e.getMessage();
		  Log.error(debugmsg+msg);
		  throw new SAFSException(msg);
	  }
  }

  protected void _setSize(Dimension size) throws SAFSException {
	  String debugmsg = StringUtils.debugmsg(getClass(), "_setSize");
	  try{
		  TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
		  parent.resize(size.width, size.height);
	  }catch(Exception e){
		  String msg = "Fail to set window to size "+size+" ,due to Exception "+e.getMessage();
		  Log.error(debugmsg+msg);
		  throw new SAFSException(msg);
	  }
  }
  
  /** <br><em>Purpose:</em> minimize
   **/
  protected void _minimize() throws SAFSException{
	  String debugmsg = StringUtils.debugmsg(getClass(), "_minimize");
	  try{
		  TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
		  //TopLevelTestObject parent = (TopLevelTestObject) guiObj.getTopParent();
		  parent.minimize();
	  }catch(Exception e){
		  String msg = "Fail to minimize window due to Exception "+e.getMessage();
		  Log.error(debugmsg+msg);
		  throw new SAFSException(msg);
	  }
  }
  /** <br><em>Purpose:</em> maximize
   **/
  protected void _maximize() throws SAFSException{
	  String debugmsg = StringUtils.debugmsg(getClass(), "_maximize");
	  try{
		  TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
		  parent.maximize();
	  }catch(Exception e){
		  String msg = "Fail to maximize window due to Exception "+e.getMessage();
		  Log.error(debugmsg+msg);
		  throw new SAFSException(msg);
	  }
  }
  
//  	protected int waitForObject(String mapname, String windowName, String compName, int secii) throws SAFSException{
//		DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils(); 
//		return utils.waitForObject(testRecordData.getAppMapName(),windowName, windowName, secii);
//	}
	
  /**
   * Attempt to click an object by mouse event. MouseClick uses low level 
   * RFT's API (emitLowLevelEvent).
   * @param gto - GuiTestObject
   * @param clickPoint - Point as location, default center
   */
  protected void mouseClick(GuiTestObject gto, Point clickPoint){
	String methodName = getClass().getName() + ": mouseClick()";
	  
	LowLevelEvent mouseEvents[] = new LowLevelEvent[6];
	mouseEvents[0] = SubitemFactory.mouseMove(clickPoint);
	mouseEvents[1] = SubitemFactory.delay(100);
	mouseEvents[2] = SubitemFactory.leftMouseButtonDown();
	mouseEvents[3] = SubitemFactory.delay(100);
	mouseEvents[4] = SubitemFactory.leftMouseButtonUp();
	mouseEvents[5] = SubitemFactory.delay(100);
	Log.info(methodName + "calling emitLowLevelEvent");
	RootTestObject.getRootTestObject().emitLowLevelEvent(mouseEvents);
		  
  }
  
  /**
   * Attempt to extract a property value from the Hashtable provided from getProperties().
   * This is sometimes useful if getProperty does NOT return a specific property's value 
   * but getProperties does!
   * @param o - TestObject to get property from
   * @param prop - property to seek
   * @return null or Object value
   */
  protected Object getObjectPropertyFromProperties(TestObject o, String prop){
  	try{
		Hashtable props = o.getProperties();
		return props.get(prop);
	}
	catch(Exception x){
        Log.debug("CFComponent: IGNORED "+  x.getClass().getSimpleName() +" executing getProperties()");
	}
	return null;
  }
  
  
  /**
   * get the RFT value of a property or an indexed property as in rowLabel(0)
   * @param o - TestObject to get property from
   * @param prop - property to seek, can be an indexed property like rowLabel(0)
   * @return null or Object value or PropertyNotFoundException
   */
  protected Object getObjectProperty(TestObject o, String prop) 
                   throws PropertyNotFoundException{
	  if (o==null)return null;
	  if ((prop==null)||(prop.length()==0)) throw new PropertyNotFoundException("null");
      Object rval = null;
      try{
    	  rval = o.getProperty(prop); //can throw PropertyNotFoundException
    	  if (rval == null){
			  try{
	    		  rval = getObjectPropertyFromProperties(o, prop);
	    		  if (rval == null){
		    		  Log.debug(".....CFComponent property '"+ prop +"' is invalid or null.");
	    		  }
			  }catch(Exception x){
	    		  Log.debug(".....CFComponent property '"+ prop +"' is invalid or null. "+ 
	    				  x.getClass().getSimpleName());
			  }
    	  }
      }catch(PropertyNotFoundException nfe){
    	  //check for array property name. Ex: rowLabel(0)
    	  int sp = prop.indexOf("(");
    	  int ep = prop.indexOf(")");
    	  if ((ep > sp+1)&&(sp > 0)){
        	  
    		  //should we force use of 1-based index, or not?
        	  //No. negative indices or arguments for properties may be valid
    		  
    		  int n = Integer.MIN_VALUE;
    		  String indexer = "";   
    		  String tindex = "";
    		  try{
            	  //strip off (n) for property name (RFT Indexer)
            	  //convert n to Integer
    			  tindex = prop.substring(sp+1, ep);
    			  tindex = tindex.replace("\"", ""); //remove any expression artifacts
    			  tindex = tindex.replace("'", ""); //remove any expression artifacts
    			  n = (int)Float.parseFloat(tindex);
    			  indexer = prop.substring(0, sp).trim();    			  
    			  String methodname = "get"+ indexer.toUpperCase().substring(0,1) + indexer.substring(1);
    			  MethodInfo[] methods = o.getMethods();
    			  MethodInfo method = null;
    			  String mname = null;
    			  String msig = null;
    			  for(int m = 0;m < methods.length;m++){
    				  method = methods[m];
    				  mname = method.getName();
    				  if(mname.equals(methodname)){
        				  msig = method.getSignature();
        				  Log.info(".....Found potential property method: "+ mname +" using signature '"+ msig +"'");
        				  //an indexed property returning a single value
        				  if((msig.startsWith("(I)"))&&(!(msig.endsWith("]")))){
        					  try{ 
        						  rval = o.invoke(mname, msig, new Object[]{new Integer(n)});
        						  break;
        					  }catch(Exception minvoke){
        						  Log.debug(".....Property method invocation unsuccessful: "+ minvoke.getClass().getName()+":"+minvoke.getMessage());
        						  throw nfe;
        					  }
        				  }
    				  }
    			  }
    			  //debug output to show all obj methods for development
    			  if(rval==null){
	        		  Log.debug(".....Property is not a supported array type. "+
    		  			"Listing methods and aborting with PropertyNotFound.");
    				  this.listProperties(o);
    				  this.listMethods(o);
	        		  throw nfe;
    			  }
    		  }
    		  //old versions of RFT might not support Indexers
    		  catch(NumberFormatException x){
        		  Log.debug(".....Property is not formatted properly for an array type. "+
        		  			"Aborting with PropertyNotFound:"+ x.getClass().getName());
    			  throw nfe;
    		  }
    		  //old versions of RFT might not support Indexers
    		  catch(Throwable x){
        		  Log.debug(".....Property is not a supported array type. "+
      		  			"Aborting with PropertyNotFound:"+ x.getClass().getName());
    			  throw nfe;
    		  }
    	  }else{
    		  try{
	    		  rval = getObjectPropertyFromProperties(o, prop);
	    		  if (rval == null){
		    		  Log.debug(".....CFComponent property '"+ prop +"' is not valid. "+
					    		"Aborting with PropertyNotFound.");
		    		  throw nfe;
	    		  }
    		  }catch(Exception x){
	    		  Log.debug(".....CFComponent property '"+ prop +"' is not valid. "+ 
	    				  x.getClass().getSimpleName() +
		    		      " Aborting with PropertyNotFound.");
    			  throw nfe;
    		  }
    	  }
      }
	  return rval;
  }
  
  /** <br><em>Purpose:</em> verifyPropertyContains
   **/
  protected void verifyPropertyContains () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String prop = (String) iterator.next();
      String val =  (String) iterator.next();
      String strcase = "";
      boolean ignorecase = false;
      try { strcase = (String) iterator.next();}catch(Exception x) {;}
      ignorecase = !StringUtils.isCaseSensitive(strcase);

      Log.info(".....CFComponent.process; ready to do the VP for prop : "+prop+" val: "+val);

      String rval = null;
      try{ rval = getObjectProperty(obj1, prop).toString();}
      catch(Exception x){;}//Often PropertyNotFoundException

      Log.info("..... real value is: "+rval);

      // it is possible the property name is not valid for RobotJ
	  // it may be a property valid in a different engine though
      // Carl Nagle -- However, we should probably issue FAILURE and not NOT_EXECUTED
      if (rval == null) {
	      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
          return;
      }

	  String testval = (ignorecase)?val.toUpperCase():val;
	  String testrval = (ignorecase)?rval.toUpperCase():rval;

      if (testrval.indexOf(testval)<0) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, read property is: "+rval+", compare(contain) value is: "+val,
                       FAILED_MESSAGE);
      } else {
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_5,
                                          altText+", Property \""+ prop +"\" contains substring \""+ val +"\" as expected.",
                                          windowName, compName, action,
                                          "Property "+prop,
                                          "contains substring '"+val+"' as expected."),
                       PASSED_MESSAGE);
      }
    }
  }

  /** <br><em>Purpose:</em> verifyValueContains
   **/
  protected void verifyValueContains () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String str = (String) iterator.next();
      String val =  (String) iterator.next();
      //str = substituteVariable(str); // Carl Nagle: this has already occurred
      if (str==null) return;
      //val = substituteVariable(val); // Carl Nagle: this has already occurred
      if (val==null) return;
      Log.info(".....CFComponent.process; ready to do the VVContains for string : "+str+" substring: "+val);
      if (str.indexOf(val)<0) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, read property is: "+str+", compare(contain) value is: "+val,
                       FAILED_MESSAGE);
      } else {
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_5,
                                          altText+", Variable \""+ str +"\" contains value substring \""+ val +"\" as expected.",
                                          windowName, compName, action,
                                          "Variable "+str,
                                          "contains value substring '"+val+"' as expected."),
                       PASSED_MESSAGE);
      }
    }
  }
  /** <br><em>Purpose:</em> verifyValues, verifyValuesIgnoreCase, verifyValueEquals
   **/
  protected void verifyValues () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String str = (String) iterator.next();
      String val =  (String) iterator.next();
      //str = substituteVariable(str); //Carl Nagle: substitution has already occurred
      if (str==null) return;
      //val = substituteVariable(val); //Carl Nagle: substitution has already occurred
      if (val==null) return;
      Log.info(".....CFComponent.process; ready to do the VVEquals for string : "+str+" substring: "+val);
      if (action.equalsIgnoreCase(VERIFYVALUESIGNORECASE)) {
        val = val.toLowerCase();
        str = str.toLowerCase();
      }
      if (!val.equals(str)) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, read property is: "+str+", value is: "+val,
                       FAILED_MESSAGE);
      } else {
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_5,
                                          altText+", Variable \""+ str +"\" has value \""+ val +"\" as expected.",
                                          windowName, compName, action,
                                          "Variable "+str,
                                          "had value '"+val+"' as expected."),
                       PASSED_MESSAGE);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getProperties() throws SAFSException {
    if (obj1 != null) {
      return obj1.getProperties();
    } else {
      Log.debug("component object is null, can't get properties.");
      return null;
    }
  }

  protected Object getPropertyObject(String propertyName) throws SAFSException{
	try{
		return getObjectProperty(obj1, propertyName);
	}catch(PropertyNotFoundException pne){
		throw new SAFSException(SAFSException.CODE_PropertyNotFoundException);
	}
  }

  /** <br><em>Purpose:</em> clearAppMapCache
   **/
  protected void clearAppMapCache () {
    obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
    // set status to ok
    testRecordData.setStatusCode(StatusCodes.OK);
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action),
                   GENERIC_MESSAGE);
  }

  // issues success AND failure messages and sets status code
  private void robotInputKeys(String keys){
	  String target = windowName+":"+compName;
	  try {
		  if(action.equalsIgnoreCase(INPUTCHARACTERS))
			  Robot.inputChars(keys);
		  else
			  Robot.inputKeys(keys);
	   	}catch(AWTException x){
        	issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
        			"Support for 'AWT Robot' not found.", "AWT Robot"));
        	return;
	   	}catch(Exception x){
	   		issueErrorPerformingActionOnX(keys, x.getClass().getSimpleName()+": "+ x.getMessage());
	   		return;
	   	}
      testRecordData.setStatusCode(StatusCodes.OK);
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(SENT_MSG_3,
                                         action+" "+keys+" sent to "+target, //altText
                                         action, keys, target),
                     PASSED_MESSAGE);
  }
  
  /** <br><em>Purpose:</em> inputkeys and inputcharacters
   * @author Carl Nagle Trying to intercept and ignore TargetGoneException when input may intend to close window.
   **/
  protected void inputKeystrokes () throws SAFSException {
	String debugMsg = getClass().getName() + ".inputkeys(): ";
	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else if (obj1 == null){
    	robotInputKeys((String) iterator.next());
	    return;
    } else {
      TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(obj1);
      String keys = (String) iterator.next();
      Log.info(".....CFComponent.process; ready to do inputKeys '"+ keys +"' for : "+ parent);
      //guiObj.click(); // has to be selected first!! NO, LET THE TEST DO THIS.
      //parent.clickDisabled(); //need to consider this clickDisabled() function.
      
      // S0531918 fix.  2008.9.20 
      // comment out this, since non-standard EditBox can't be recognized by SAFS, and treated as CFText.
      // It is ridiculous to send InputKeys to a GUI control other than EditBox, such as a button, Checkbox and etc.
      //S0537974 2008.10.10
      // Those custom EditBox classes which extends from standard .net class will be match to CFText also
      // so only those custom classes which does not extend from stanadard .net class will not be treated here.
      // (Carl Nagle) Comment: It is not really ridiculous to send keystrokes to other controls.  
      // For example, buttons use SPACE or ENTER to activate.  What matters is "who" has the keyboard 
      // focus is the one that gets first chance to do something with the keystrokes--like ESC or F11, etc.
      try {
    	  //An alternative: set focus for the CFText without focus. Better to put the code in CFText.(Junwu)
          if (this instanceof CFText && !((GuiTestObject)obj1).hasFocus())
           	  ((GuiTestObject)obj1).click(); // call click to setfocus on the target component. It is no harm to click on EditBoxes. 

          if (action.equalsIgnoreCase(INPUTCHARACTERS)){
        	  parent.inputChars(keys);
          }else{
        	  parent.inputKeys(keys);
          }    	  
      } catch(MethodNotFoundException mnfe) {
    	  Log.debug("Exception occured in " + debugMsg + mnfe.getMessage());   	  
		  if (obj1 instanceof FlexObjectTestObject) { 
			  robotInputKeys(keys);
			  return;
    	  } else {
			  Log.debug(debugMsg+" it is not a Flex component. No need to have last try?");
			  robotInputKeys(keys);
			  return;
    	  }
      }catch(TargetGoneException tge){
		  Log.info(debugMsg+" IGNORING TargetGoneException usually resulting from window closure after input...");
      }
      
      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(SENT_MSG_3,
                                         action+" "+keys+" sent to "+windowName, //altText
                                         action, keys, windowName),
                     PASSED_MESSAGE);
    }
  }
  /** <br><em>Purpose:</em> verifyObjectDataToFile
   **/
  protected void verifyObjectDataToFile () throws SAFSException {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      if (params.size() < 1) {
          paramsFailedMsg(windowName, compName);
          return;
      }
      String filename = (String) iterator.next();
      String absfilename = null;
      String actfilename = null;
      //fileEncoding is used to read bench file, and store the actual data to a file.
      String fileEncoding = null;
      if(iterator.hasNext()){
      	fileEncoding = (String) iterator.next();
        //If user put a blank string as encoding,
        //we should consider that user does NOT provide a encoding, reset encoding to null.
        fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
      }
      Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);
      
      File fn = new CaseInsensitiveFile(filename).toFile();
      if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
        if (pdir == null) pdir="";
        fn = new CaseInsensitiveFile(pdir, filename).toFile();
      }
      try{
      	  if(!fn.isAbsolute())
      	  	throw new FileNotFoundException(filename +" cannot be resolved as a readable benchmark.");
      	  
          absfilename = fn.getAbsolutePath();
      	  java.util.List list = captureObjectData(obj1);
      	  String data = formatObjectData(list);
      	  
      	  String bench = null;
      	  //If a file encoding is given or we need to keep the encoding consistent
      	  if(fileEncoding!=null || keepEncodingConsistent){
      		  bench = FileUtilities.readStringFromEncodingFile(absfilename,fileEncoding);
      	  }else{
      		  //Keep compatible with old version
      		  bench = FileUtilities.readStringFromUTF8File(absfilename);
      	  }
      	  
          if (!data.equals(bench)) {
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              String tdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
              if (tdir==null) tdir = "";
              actfilename = filename+ TEST_DATA_SUFFIX;
              File tfile = new CaseInsensitiveFile(tdir, actfilename).toFile();
              String details = "TEST data snapshot ";
              if(tfile.isAbsolute()){
            	  actfilename = tfile.getAbsolutePath();
            	  if(fileEncoding!=null || keepEncodingConsistent){
              		  FileUtilities.writeStringToFile(actfilename, data, fileEncoding);
              	  }else{
              		  //Keep compatible with old version
              		  FileUtilities.writeStringToUTF8File(actfilename, data);
              	  }
            	  details +="stored at: "+actfilename;
              }else{
              	details +="could not be saved to: "+ tdir +" as '"+ actfilename +"'";
              }
              log.logMessage(testRecordData.getFac(),
                    testRecordData.getCommand()+
                    " failure, object data did not match benchmark: "+ absfilename,
                    FAILED_MESSAGE,
					details);
          }else{
	          // set status to ok
	          testRecordData.setStatusCode(StatusCodes.OK);
	          String pre = "benchmark '"+ absfilename +"'";
	          log.logMessage(testRecordData.getFac(),
	                         passedText.convert(PRE_TXT_SUCCESS_4,
	                                            pre+", "+altText,
	                                            pre,
	                                            windowName, compName, action),
	                         PASSED_MESSAGE);
          }
      }catch (IllegalArgumentException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, error capturing object data: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }catch (FileNotFoundException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, invalid benchmark specified: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }catch (IOException ioe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         testRecordData.getCommand()+
                         " failure, error reading benchmark: "+absfilename+", msg: "+ioe.getMessage(),
                         FAILED_MESSAGE);
      }
  }
  
  /**
   * Given an object of any type attempt to convert or extract its intended or 
   * visible text value.  This function is intended to be overridden as necessary 
   * by subclasses and\or custom classes to extend the number of supported objects 
   * that can have their visible text extracted.
   * 
   * This implementation handles:<br>
   * <br>
   * String = value.toString()
   * TestObject = RGuiObjectRecognition.getText(value)
   * 
   * Handling something other than these must be done in subclasses.
   * 
   * @param value String or TestObject to get the text from 
   * @return String or null -- may be object.toString() which might not be desired.
   * @see org.safs.rational.RGuiObjectRecognition#getText(TestObject)
   */
  protected String convertObjectValueToString(Object value) throws SAFSException{
		if(value instanceof String){
			return value.toString();
		}else if(value instanceof TestObject){
			return RGuiObjectRecognition.getText((TestObject) value);
		}else{
			Log.debug("CFComponent.convertObjectValue needs to add more code to analyse "+value.toString());
		}
		return value.toString();
  }
  
  /**
   * Captures the object data into a List.  This is generally for the CaptureObjectDataToFile and 
   * VerifyObjectDataToFile commands. Subclasses may provide this as a single List, 
   * or a List of Lists for 2D arrays.  This implementation assumes the List is a 
   * single Object item.  We extract the "text" or "itemText" property from the TestObject.
   * 
   * @param table TestObject to snapshot data from.
   * 
   * @return List containing a single Object item.  Null if an invalid table reference is 
   * provided or some other error occurs.
   * 
   * @throws SAFSException
   * @throws IllegalArgumentException if table is not an acceptable TestObject.
   */
  protected java.util.List captureObjectData(TestObject table)throws IllegalArgumentException, SAFSException{
  	String prop = PROPERTY_text;  //this isn't the property for all objects...
    Log.info("CFComponent.captureObjectData attempting to extract list items...");
    String myClass = getClass().getSimpleName();
    if (myClass.equals("CFList") || myClass.equals("CFComboBox")) {
      prop = PROPERTY_DOT_itemText;
    }
    
    if (isDotnetDomain(table)){
    	TestObject clazz = DotNetUtil.getClazz(table);
    	if(DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_COMBOBOX_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_TOOLSTRIPCOMBOBOX_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_TOOLSTRIPCOMBOBOXANDCONTROL_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_LISTBOX_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_LISTVIEW_NAME)){
    		try{
    			return utils.extractListItems(table, ".ItemCount", "");
    		}catch(SAFSException e){
    			Log.debug("SAFSException: "+e.getMessage());
        		Log.debug("Can not get items by method extractListItems() of DDGUIUtilities.");	
    		}
    	}else if(DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_TEXTBOXBASE_NAME)){
    		Log.debug("This is a TextBox Object. Try to get its property "+DotNetUtil.PROPERTY_TYPE_TEXT);
    		prop = DotNetUtil.PROPERTY_TYPE_TEXT;
    	}else{
    		//Suppose this is a combobox component
    		Log.debug("Suppose it is a custom combobox which does not extend from standard .NET combox.");
        	Log.debug("Try to get propertiy "+PROPERTY_Items);
        	prop = PROPERTY_Items;
    	}
    }
        
    Object rval = getObjectProperty(table,prop);      
    Log.info("..... captureObjectData value: "+ rval);
    if (rval==null) return null;
    java.util.List list = new ArrayList();
    list.add(rval);
    return list;
  }
  
  /**
   * Format the List data into a single String.  This String may include whatever formatting like 
   * Tabs, line separators, etc. are needed for the command being executed.  This is generally 
   * for the CaptureObjectDataToFile and VerifyObjectDataToFile commands.  Subclasses may need 
   * to override this method as appropriate for these commands.
   * <p>
   * In this implementation the List is expected to be a single Object provided by captureObjectData.
   * This Object has typically been a Rational NameSet of Strings, a Java Collection of Strings, 
   * or a simple Object on which we use toString(). 
   * <p>  
   * Each final item(s) in the List will be output using the toString().trim() method.
   * <p>
   * It is expected the data is ready for writing to a file or other similar use without further 
   * modification.
   * 
   * @param list List returned from captureObjectData
   * @return String formatted for writing to file or screen.
   * @throws IllegalArgumentException if the List or the Object extracted from the List are null.
   * @see #captureObjectData(TestObject)
   */
  protected String formatObjectData(java.util.List list)throws IllegalArgumentException{
  	try{
  		Object rval = list.get(0);//can throw IndexOutOfBoundsException  	
	    if (rval instanceof com.rational.test.ft.value.NameSet ||
	        rval instanceof Collection) {
		    Enumeration enu = null;
		    Iterator ii = null;
		    int size = 0;
		    if (rval instanceof Collection) {
		        ii = ((Collection)rval).iterator();
		        size = ((Collection)rval).size();
		    } else {
		    	enu = ((com.rational.test.ft.value.NameSet)rval).elements();
		    	size = ((com.rational.test.ft.value.NameSet)rval).size();
		    }
		    StringBuffer contents = new StringBuffer();
	    	try {
	    		for(;;) {
			        String rnext = "";
			        if (ii != null) rnext = ii.next().toString().trim();
			        else rnext = enu.nextElement().toString().trim();
			        contents.append(rnext+ "\n");
	    		}
	    	}
	    	catch (Exception ee) {} // ignore, iterator/enumerator at end of list
	    	return contents.toString();
	    } else if (rval instanceof String){  // branch String for supporting .NET
		    String rString = "";
	    	Iterator iter = list.iterator(); 
	    	while (iter.hasNext())
	    		rString = rString + iter.next() + "\n";
	    	return rString;
	    }// NOT a NameSet or Collection or String
	    else { 
		    return rval.toString().trim();
	    }
  	}catch(IndexOutOfBoundsException iob){//if List has no items
  		return "";  	
  	}catch(NullPointerException np){
  		throw new IllegalArgumentException("Invalid ObjectData provided for formatting.");
  	}
  }

  protected boolean performHoverMouse(Point point, int miliseconds) throws SAFSException{  	
	  String debugmsg = StringUtils.debugmsg(false);

	  try{
		  GuiTestObject guiObj = new GuiTestObject(obj1);
		  if(point!=null){
			  guiObj.hover(miliseconds/1000, point);
		  }else{
			  guiObj.hover(miliseconds/1000);
		  }
		  return true;
	  }catch(Exception e){
		  IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
	  }
	  return false;
  }
	
  /**
   * A generic implementation for CaptureObjectDataToFile.
   * Provides the primary implementation for validating the filename and writing the 
   * data to the specified file.  Calls the functions captureObjectData then formatObjectData 
   * allowing subclasses like CFTable to override the data capturing and formatting functionality.
   * The String retrieved from formatObjectData is written to the specified file unmodified.
   * @see #captureObjectData(TestObject)
   * @see #formatObjectData(java.util.List)
   **/
  protected void captureObjectDataToFile () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    String fileEncoding = null;
    String filename = null;

    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
      return;
    }
    //filename
    filename = (String) iterator.next();
    //file encoding
    if(iterator.hasNext()){
      	fileEncoding = (String) iterator.next();
        //If user put a blank string as encoding,
        //we should consider that user does NOT provide a encoding, reset encoding to null.
        fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
    }
    Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);
    //filter mode
    //if(iterator.hasNext()){ iterator.next();}
    //filter options
    //if(iterator.hasNext()){ iterator.next();}
    
    File fn = new CaseInsensitiveFile(filename).toFile();
    if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
        if (pdir == null) pdir="";
       	fn = new CaseInsensitiveFile(pdir, filename).toFile();
        filename = fn.getAbsolutePath();
	}  
    try{
        java.util.List list = captureObjectData(obj1);
	    String buf = "";
	    if((list != null)&&(list.size()>0)){
	    	buf = formatObjectData(list); //can throw IllegalArgumentException
	    }else if (list == null){
	    	Log.debug("CFComponent.captureObjectData WARNING: call returned null.");
	    }else { // list.size==0
	    	Log.debug("CFComponent.captureObjectData WARNING: call returned empty array of data.");
	    }
	    //If a file encoding is given or we need to keep the encoding consistent
	    if(fileEncoding!=null || keepEncodingConsistent){
	    	FileUtilities.writeStringToFile(filename, fileEncoding, buf);
	    }else{
	    	//Keep compatible with old version
	    	FileUtilities.writeStringToUTF8File(filename, buf);
	    }
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        String pre = " '"+ filename +"'";
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_4,
                       pre+", "+ altText,
                       pre,
                       windowName, compName, action),
                       PASSED_MESSAGE);
    } 
    catch(IllegalArgumentException ia) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    	log.logMessage(testRecordData.getFac(),
                   testRecordData.getCommand()+
                   " failure, could not capture object data.",
                   FAILED_MESSAGE);
    }
    catch (FileNotFoundException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, invalid file specification: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }
    catch (IOException ioe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     testRecordData.getCommand()+
                     " failure, error writing to file: "+filename+", msg: "+ioe.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> capturePropertiesToFile
   **/
  protected void capturePropertiesToFile () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      String filename = (String) iterator.next();
      File fn = new CaseInsensitiveFile(filename).toFile();
      if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
        if (pdir == null) pdir="";
        fn = new CaseInsensitiveFile(pdir, filename).toFile();
      }
      filename = fn.getAbsolutePath();
      String delim = testRecordData.getSeparator();
      Collection contents = new ArrayList();
      listAllProperties(obj1, "capturePropertiesToFile");
      Map m = obj1.getProperties();
      for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
        String prop = (String)i.next();
        Object next = m.get(prop);
        contents.add(prop+delim+next);
      }
      try {
        StringUtils.writefile(filename, contents);
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        String pre = " '"+ filename +"'";
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_4,
                                          pre+", "+altText,
                                          pre,
                                          windowName, compName, action),
                       PASSED_MESSAGE);
      } catch (IOException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }
  /** <br><em>Purpose:</em> verifyFileToFile
   ** @param text, boolean, if true, then text files, else binary files
   **/
  protected void verifyFileToFile (boolean text) throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String benchfilename = (String) iterator.next();
      File fn = new CaseInsensitiveFile(benchfilename).toFile();
      if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
        if (pdir == null) pdir="";
        fn = new CaseInsensitiveFile(pdir, benchfilename).toFile();
      }
      benchfilename = fn.getAbsolutePath();
      String testfilename = (String) iterator.next();
      fn = new CaseInsensitiveFile(testfilename).toFile();
      if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
        if (pdir == null) pdir="";
        fn = new CaseInsensitiveFile(pdir, testfilename).toFile();
      }
      testfilename = fn.getAbsolutePath();
      Log.info(".....CFComponent.process; ready to do the VTFTF for bench file : "+
               benchfilename + ", test file: "+testfilename);
      // now compare the two files
      Collection benchcontents = new ArrayList();
      Collection testcontents = new ArrayList();
      try {
        if (text) {
          benchcontents = StringUtils.readfile(benchfilename);
          testcontents = StringUtils.readfile(testfilename);
        } else {
          String bench = (StringUtils.readBinaryFile(benchfilename)).toString();
          String test  = (StringUtils.readBinaryFile(testfilename)).toString();
          benchcontents.add(bench);
          testcontents.add(test);
          Log.info("benchcontents.length: "+bench.length());
          Log.info("testcontents.length: "+test.length());
        }
        String pre = "file '"+testfilename+"', benchmark '"+benchfilename +"'";
        if (!benchcontents.equals(testcontents)) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         warningText.convert(TXT_FAILURE_4, action,
                                             windowName, compName, action,
                                             " contents do not match"),
                         FAILED_MESSAGE);
        } else {
          // set status to ok
          testRecordData.setStatusCode(StatusCodes.OK);
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4,
                                            pre+", "+altText,
                                            pre,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
        }
      } catch (IOException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, file can't be read: "+benchfilename+", or: "+
                       testfilename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }

  protected Rectangle getWindowRectangleOnScreen(){
      GuiTestObject guiObj = new GuiTestObject(winObject);
      Rectangle winRect = guiObj.getClippedScreenRectangle();

      if(winRect==null) IndependantLog.warn(windowName+":"+windowName +" was not found on screen");

      return winRect;
  }

  /**
   * Return the component's screen bounds. 
   */
  protected Rectangle getComponentRectangleOnScreen(){
	  return getComponentRectangle();
  }
  /**
   * Return the component's screen bounds. 
   */
  protected Rectangle getComponentRectangle (){
      GuiTestObject guiObj = new GuiTestObject(obj1);
      Rectangle compRect = guiObj.getClippedScreenRectangle();
      
      if(compRect==null) IndependantLog.warn(windowName+":"+compName +" was not found on screen");
      
      return compRect;
  }
  
  protected boolean exist() throws SAFSException{
	  try{
		  //wait for the window/component
		  Object obj = localClearAppMapCache(windowName, compName);
		  Log.info("localClearAppMapCache.., obj:"+obj);
		  if(obj==null){
			  Log.info("The ............................object is now gone...");
			  return false;
		  }
		  return true;
	  }catch(TargetGoneException tge) {
		  Log.info("The object is now gone...");
		  return false;
	  }
  }
	
  /** <br><em>Purpose:</em> verifyArrayPropertyToFile/verifyPropertyToFile
   ** @param array, boolean, if true, then array property, else scalar
   ** <br>Note: for now, the value of the parameter is meaningless because
   ** the value of the property inferrs if it is an array or not.
   **/
  protected void verifyPropertyToFile (boolean array) throws SAFSException {
	String debugmsg = getClass().getName()+".verifyArrayPropertyToFile() ";
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String prop = (String) iterator.next();
      String filename =  (String) iterator.next();
      String fileEncoding = null;
      if(iterator.hasNext()){
      	fileEncoding = (String) iterator.next();
        //If user put a blank string as encoding,
        //we should consider that user does NOT provide a encoding, reset encoding to null.
        fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
      }
      Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);     
      //TODO FilterMode
      //TODO FilterOptions 
      
//      filename = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_BENCHDIRECTORY);
      filename = deduceBenchFile(filename).getAbsolutePath();
      
      Log.info(".....CFComponent.process; ready to do the VP for prop : "+prop+" filename: "+filename);
      try {
        listAllProperties(obj1, "verifyPropertyToFile");
        if (array) {
          //prop = ITEM_TEXT_PROP;
        }
        TestObject testObject = new TestObject(obj1.getObjectReference());
        Collection propertyContents = getPropertyValue(testObject, prop);
        Collection benchContents = null;
        
	    //If a file encoding is given or we need to keep the encoding consistent
	    if(fileEncoding!=null || keepEncodingConsistent){
	    	benchContents = StringUtils.readEncodingfile(filename, fileEncoding);
	    }else{
	    	//Keep compatible with old version
	    	benchContents = StringUtils.readfile(filename);
	    }
        
        Log.debug(debugmsg+" property contents: "+propertyContents);
        Log.debug(debugmsg+" bench contents: "+benchContents);
        
        if(propertyContents.equals(benchContents)){
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_5,
                                          altText+", Property \""+ prop +"\" had value \""+ propertyContents +"\".",
                                          windowName, compName, action,
                                          "Property "+prop,
                                          " had value '"+propertyContents+"'."),
                       PASSED_MESSAGE);
        }else{
        	String errormsg = FAILStrings.text(FAILStrings.FAIL_MATCH_KEY, "Fail to match.")+"\n"+
        						"property value: "+propertyContents+"\n"+
        						"bench value: "+benchContents;
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        	componentExecutedFailureMessage(errormsg);
        }
      } catch (IOException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }

  /** <br><em>Purpose:</em> capturePropertyToFile
   ** <br>Note: for now, the value of the parameter is meaningless because
   ** the value of the property inferrs if it is an array or not.
   **/
  protected void capturePropertyToFile () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    if (params.size() < 2) {
      paramsFailedMsg(windowName, compName);
    } else {
      String prop = (String) iterator.next();
      String filename =  (String) iterator.next();
      String encoding = null;
      if(iterator.hasNext()){
      	encoding = (String) iterator.next();
        //If user put a blank string as encoding,
        //we should consider that user does NOT provide a encoding, reset encoding to null.
        encoding = "".equals(encoding.trim())? null: encoding;
      } 
      Log.info("...filename: "+filename+" ; encoding:"+encoding);
      
//      filename = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_TESTDIRECTORY);
      filename = deduceTestFile(filename).getAbsolutePath();
      
      Log.info(".....CFComponent.process; ready to do the CP for prop : "+prop+" filename: "+filename+" encoding:"+encoding);
      try {
        TestObject testObject = new TestObject(obj1.getObjectReference());
        Collection contents = getPropertyValue(testObject, prop);
        
	    //If a file encoding is given or we need keep the encoding consistent.
	    if(encoding!=null || keepEncodingConsistent){
	    	StringUtils.writeEncodingfile(filename, contents, encoding);
	    }else{
	    	//keep compatible with old version
	    	StringUtils.writefile(filename, contents);
	    }
	    
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(TXT_SUCCESS_5,
                                          altText+", Property \""+ prop +"\" had value \""+ contents +"\".",
                                          windowName, compName, action,
                                          "Property "+prop,
                                          " had value '"+contents+"'."),
                       PASSED_MESSAGE);
      } catch (IOException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }
  
  private Collection getPropertyValue(TestObject testObject, String property) throws SAFSException{
	  String debugmsg = getClass().getName() + ".getPropertyValue() ";
		Collection contents = new ArrayList();

		try {
			Object rval = getObjectProperty(testObject, property);
			Log.info(debugmsg+" property: "+property +", its real value is: " + rval);
			if (rval == null) {
				String errormsg = FAILStrings.convert(FAILStrings.PROPERTY_VALUE_IS_NULL, 
									"Property: "+property+", its value is null.", property);
				Log.debug(debugmsg+" the value of "+property+" is null.");
				throw new SAFSException(errormsg);
			} else {
				// treat it as an array
				if (rval instanceof com.rational.test.ft.value.NameSet
						|| rval instanceof Collection) {
					Enumeration enu = null;
					Iterator ii = null;
					int size = 0;
					if (rval instanceof Collection) {
						ii = ((Collection) rval).iterator();
						size = ((Collection) rval).size();
					} else {
						enu = ((com.rational.test.ft.value.NameSet) rval).elements();
						size = ((com.rational.test.ft.value.NameSet) rval).size();
					}
					for (int j = 0; j < size; j++){
						String rnext = "";
						if (ii != null)
							rnext = ((String) ii.next()).trim();
						else
							rnext = ((String) enu.nextElement()).trim();
						contents.add(rnext);
					}
				}else{
					contents.add(rval.toString());
				}
			}
		} catch (Exception e) {
			Log.debug(debugmsg + " Exception occured: " + e.getMessage());
			throw new SAFSException(e.getMessage());
		}

		return contents;
  }
  
  /**<br><em>Purpose:</em> verifyClipboardToFile
  **/
  protected void verifyClipboardToFile () throws SAFSException {
    try {
      Toolkit tk = Toolkit.getDefaultToolkit();
      Clipboard cl = tk.getSystemClipboard();
      String val = "";
      if (params.size() <= 0) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
                       ": wrong params, should be one.",
                       FAILED_MESSAGE);
        return;
      }
      Iterator iterator = params.iterator();
      String path = (String) iterator.next();
      File name = new CaseInsensitiveFile(path).toFile();
      if (!name.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
        if (pdir == null) return;
        name = new CaseInsensitiveFile(pdir, path).toFile();
      }
      path = name.getAbsolutePath();
      Log.info("path: "+path);
      Object requestor= null; // currently not used
      Transferable t = cl.getContents(requestor);

      // Only DataFlavor.stringFlavor is taken consideration. Fixing S0535525.
      Object obj = t.getTransferData(DataFlavor.stringFlavor);
      val = (obj==null?"":obj.toString());

      Collection realValue = StringUtils.readstring(val);
      Collection compValue = StringUtils.readfile(path);
      Log.info("realValue: "+realValue);
      Log.info("compValue: "+compValue);
      if (!realValue.equals(compValue)) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       warningText.convert(TXT_FAILURE_2, testRecordData.getCommand(),
                                           testRecordData.getCommand(),
                                           " clipboard does not match contents of "+path),
                       FAILED_MESSAGE);
        return;
      }
      // set status to ok
      log.logMessage(testRecordData.getFac(),
    		  		 GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY, "the content of Clipborad matches the content of "+path,
                                                 "Clipboard",path),
                     PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (IllegalStateException ise) {
      ise.printStackTrace();
      throw new SAFSException("ise: "+ise.getMessage());
    } catch (AWTError ae) {
      ae.printStackTrace();
      throw new SAFSException("ae: "+ae.getMessage());
    } catch (FileNotFoundException fnfe) {
      throw new SAFSException(": "+fnfe.getMessage());
    } catch (Exception he) {
      he.printStackTrace();
      throw new SAFSException("he: "+he.getMessage());
    }
  }

  
  /**
   * <br><em>Purpose:</em> setPropertyValue
   * Note: Log the original value the the property; Set the new value to this property.
   * 	   Note that this directly modifies the object in the software under test; 
   *       therefore, it should be done with extreme care. Avoid using setProperty whenever possible
   * 	   because it allows you to modify the software under test in ways that a typical user cannot.
   */
  protected void setPropertyValue(){
  	if(params.size()<2){
        log.logMessage(testRecordData.getFac(), 
        		getClass().getName()+", "+testRecordData.getCommand() +": wrong params, should be two.",
				FAILED_MESSAGE);
        this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  	}else{
  		try{
  			String property = (String) iterator.next();
			Object value = iterator.next();
			Object originalValue = getObjectProperty(obj1, property);

			// cast value to the class as same as originalValue. Not list all types here. 
			if (originalValue instanceof Boolean) // required to cast Object to Boolean in .NET
				value = new Boolean(value.toString()); 
			else if (originalValue instanceof String)
				value = (String)value;
			else if (originalValue instanceof Integer)
				value = new Integer(value.toString());
			
			Log.debug(property+ " Original value is " + originalValue);
			obj1.setProperty(property, value);
			Log.debug(" Set "+ property + " to " + value);
			
			this.componentSuccessMessage(" Set "+ property + " to " + value);
			this.testRecordData.setStatusCode(StatusCodes.OK);
  		}catch(RationalTestException rte){
			this.componentFailureMessage(" Exception: "+rte.getMessage());
			this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  		}
  	}
  }
  

  /**
   * <br><em>Purpose:</em> Drag a compenent horizontally or vertically
   * Note: 	If TestObject 'obj1' represent a GuiSubitemTestObject, then use the RFT API setState method; This method will consider the position as a relative value.
   * 	   	Else the object will be draged by mouse to simulate the scroll action; And the position will be considered as a absolute value.
   */
  protected void performScorll()  throws SAFSException{
  	String debugMsg = getClass().getName()+ ".performScorll() ";
  	int position = 0;
  	if(params.size()<1){
  		paramsFailedMsg(windowName,compName);
  		this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  	}else{
  		try{
  			position = (int)Float.parseFloat(params.iterator().next().toString().trim());
  		}catch(NumberFormatException nfe){
  			Log.error(debugMsg+nfe.getMessage());
  			this.testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  			this.componentFailureMessage(nfe.getMessage());
  			return;
  		}
  		
 		if(obj1 instanceof GuiSubitemTestObject){
  			GuiSubitemTestObject guiTestObject = new GuiSubitemTestObject(obj1);
  			try{
  				//setState method will perform a drag horizontally or vertically
  				//For swing application, both scrollbar and 'container of scrollbar' can be handled by setState()
  				//For .net application, only scrollbar can be handled by setState; 'container of scrollbar'
  				//will be handled in catch clause.
  				Log.debug(debugMsg+" perform RFT API setState action.");
  				if(action.equalsIgnoreCase(VSCROLLTO)){
					guiTestObject.setState(Action.vScroll(position));
				}else if(action.equalsIgnoreCase(HSCROLLTO)){
					guiTestObject.setState(Action.hScroll(position));
				}
  			}catch(Exception e){
  				//For .net appliction
  				//If obj1 represents a component which contains a scrollbar
  	  			//setState(Action) may not be performed on 'obj1', 
  				//MethodNotFoundException,UnsupportedActionException may be thrown.
  	  			//We need to drag the 'scroll button' to perform the scroll action.
  				Log.debug(debugMsg+" Exception occurs: "+e.getMessage()+". try mouse drag to simulate scroll.");
	  			Subitem baseSubitem = null;
	  			Subitem	destinationSubitem = null;
	  			Point basePoint = new Point(0,0);
	  			Point destinationScreenPoint = null;
	  			//Calculate the absolute screen coordination from the 'destinationSubitem',
	  			//then perform a drag horizontally or vertically on the 'scroll button' to that coordination
	  			try{
		  			if(action.equalsIgnoreCase(VSCROLLTO)){
	  		  			baseSubitem = RationalTestScriptConstants.SCROLL_VERTICAL_ELEVATOR;
	  		  			destinationSubitem = RationalTestScriptConstants.SCROLL_UPBUTTON;
	  		  			destinationScreenPoint = guiTestObject.getScreenPoint(destinationSubitem);
	  		  			Rectangle r = guiTestObject.getScreenRectangle(destinationSubitem);
	  		  			destinationScreenPoint.translate(0, r.height/2+position);
	  		  			guiTestObject.dragToScreenPoint(baseSubitem, basePoint, destinationScreenPoint);
	  		  		}else if(action.equalsIgnoreCase(HSCROLLTO)){
	  		  			baseSubitem = RationalTestScriptConstants.SCROLL_HORIZONTAL_ELEVATOR;
	  		  			destinationSubitem = RationalTestScriptConstants.SCROLL_LEFTBUTTON;
	  		  			destinationScreenPoint = guiTestObject.getScreenPoint(destinationSubitem);
	  		  			Rectangle r = guiTestObject.getScreenRectangle(destinationSubitem);
	  		  			destinationScreenPoint.translate(r.width/2+position,0);  
	  		  			guiTestObject.dragToScreenPoint(baseSubitem, basePoint, destinationScreenPoint);
	  		  		}
	  			}catch(SubitemNotFoundException e1){
	  				//If the 'scroll bar' is not appear on the 'test object', those subitems like
	  				//SCROLL_VERTICAL_ELEVATOR,SCROLL_UPBUTTON,etc will not be found.
	  				Log.debug(debugMsg+" Scroll Bar is not visible, can not perform scroll action. "+e1.toString());
	  				String msg = failedText.text(FAILStrings.SCROLLBAR_NOT_VISIBLE,"Scroll Bar is not visible, can not perform scroll action.");
	  				componentFailureMessage(msg);
	  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  				return;
	  			}catch(Exception e2){
	  				String msg = e2.getClass().getName()+": "+e2.getMessage();
	  				Log.debug(debugMsg+msg);
	  				componentFailureMessage(msg);
	  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  				return;
	  			}
  		  	}
  		}else if(obj1 instanceof FlexScrollBaseTestObject){
  			//Treate flex component: mx.controls.TextArea,mx.controls.Tree,mx.controls.List
  			try{
  				FlexScrollBaseTestObject scrollObject = new FlexScrollBaseTestObject(obj1.getObjectReference());
  				if(action.equalsIgnoreCase(VSCROLLTO)){
  					scrollObject.scroll(Script.atPosition(position),FlexScrollDirections.SCROLL_VERTICAL,FlexScrollDetails.THUMBPOSITION);
  				}else if(action.equalsIgnoreCase(HSCROLLTO)){
  					scrollObject.scroll(Script.atPosition(position),FlexScrollDirections.SCROLL_HORIZONTAL,FlexScrollDetails.THUMBPOSITION);
  				}
  			}catch(Exception e){
  				String msg = e.getClass().getName()+": "+e.getMessage();
  				Log.debug(debugMsg+msg);
  				componentFailureMessage(msg);
  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  				return;
  			}
  		}else if(obj1 instanceof ScrollTestObject){
  			try{
  				ScrollTestObject scrollObject = new ScrollTestObject(obj1.getObjectReference());
  				if(action.equalsIgnoreCase(VSCROLLTO)){
  					scrollObject.vScrollTo(position);
  				}else if(action.equalsIgnoreCase(HSCROLLTO)){
  					scrollObject.hScrollTo(position);
  				}
  			}catch(Exception e){
  				String msg = e.getClass().getName()+": "+e.getMessage();
  				Log.debug(debugMsg+msg);
  				componentFailureMessage(msg);
  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  				return;
  			} 			
  		}else{
  			//If obj1 represent a normal gui component
  			Log.debug(debugMsg+" perform mouse drag to simulate scroll.");
  			GuiSubitemTestObject guiTestObject = new GuiSubitemTestObject(obj1);
  			//Get object's coordination relation to the screen
  			Point point = guiTestObject.getScreenPoint();
  			//perform a drag horizontally or vertically
  			if(action.equalsIgnoreCase(VSCROLLTO)){
  				guiTestObject.dragToScreenPoint(new Point(point.x,position));
  			}else if(action.equalsIgnoreCase(HSCROLLTO)){
  				guiTestObject.dragToScreenPoint(new Point(position,point.y));
  			}
  		}
  		
		this.componentSuccessMessage("");
  		this.testRecordData.setStatusCode(StatusCodes.OK);
  	}
  }    
 
  
  /** <br><em>Purpose:</em> list all TestObject properties, using listProperties and
   ** listNonValueProperties
   * @param                     obj, TestObject
   * @return                    none
   **/
  protected void listAllProperties (TestObject obj) {
  	if(!Log.ENABLED) return;
    log.logMessage(testRecordData.getFac(),
                   " ...............listProperties: ",
                   DEBUG_MESSAGE);
    listProperties(obj);
    log.logMessage(testRecordData.getFac(),
                   " .......listNonValueProperties: ",
                   DEBUG_MESSAGE);
    listNonValueProperties(obj);
    log.logMessage(testRecordData.getFac()," .....", DEBUG_MESSAGE);
  }

  /** <br><em>Purpose:</em> list all TestObject properties, using listProperties and
   ** listNonValueProperties
   * @param                     obj, TestObject
   * @param                     str, String, used in the header of the list
   * @return                    none
   **/
  protected void listAllProperties (TestObject obj, String str) {
  	if(!Log.ENABLED) return;
    log.logMessage(testRecordData.getFac(),
                   " ...............listProperties: "+str,
                   DEBUG_MESSAGE);
    listProperties(obj);
    log.logMessage(testRecordData.getFac(),
                   " .......listNonValueProperties: "+str,
                   DEBUG_MESSAGE);
    listNonValueProperties(obj);
    log.logMessage(testRecordData.getFac()," .....", DEBUG_MESSAGE);
  }

  /** <br><em>Purpose:</em> list 'regular' TestObject properties
   * @param                     obj, TestObject
   * @return                    none
   **/
  protected void listProperties (TestObject obj) {
  	if(!Log.ENABLED) return;
    Map m = obj.getProperties();
	try{
		Log.debug(" .......Number of Value properties:"+ m.size());
	    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
	      String key = (String)i.next();
	      Object next = m.get(key);
	      Log.debug("    key: "+key+": "+next);
	      log.logMessage(testRecordData.getFac(),"key: "+key+": "+next, DEBUG_MESSAGE);
		}
	}
	catch(NullPointerException npe){
	    Log.debug(" .......NullPointer: Number of Value properties: 0");
    }
  }
  /** <br><em>Purpose:</em> list 'non-value' TestObject properties
   * @param                     obj, TestObject
   * @return                    none
   **/
  protected void listNonValueProperties (TestObject obj) {
  	if(!Log.ENABLED) return;
    Map m = obj.getNonValueProperties();
    try{
	    Log.info(" .......Number of Non-Value properties:"+ m.size());
	    for(Iterator i=m.keySet().iterator(); i.hasNext(); ) {
	      String key = (String)i.next();
	      Object next = m.get(key);
	      log.logMessage(testRecordData.getFac(),"key: "+key+": "+next, DEBUG_MESSAGE);
	    }
    }
	catch(NullPointerException npe){
	    Log.info(" .......NullPointer: Number of Non-Value properties may be 0");
	}
  }
  /** <br><em>Purpose:</em> list TestObject methods
   * @param                     obj, TestObject
   * @return                    none
   **/
  protected void listMethods (TestObject obj) {
  	if(!Log.ENABLED) return;
    MethodInfo[] mi = obj.getMethods();
    try{
	    for(int i=0; i<mi.length; i++) {
	      Log.debug("  obj method: "+mi[i].getName()+ ", "+mi[i]);
	      log.logMessage(testRecordData.getFac(),
	                     "next method: "+mi[i].getName()+ ", "+mi[i],
	                     DEBUG_MESSAGE);
	    }
    }
    catch(NullPointerException npe){
	    Log.info(" .......NullPointer: Number of Methods may be 0");
    }
  }

  /**
   * Extract a 'Tree' hierarchy from a TestObject.
   * The method currently only supports swing JTrees.
   * <br><em>Assumptions:</em>
   * <br>this is the mechanism used (gleaned through lots of trial and error):
   * @param                     nitem, TestObject, the next item
   * @return                    org.safs.Tree
   * @exception                 SAFSException
   **/
  protected Tree extractTreeItemsSub (Object nitem) throws SAFSException {
    String methodName = "extractTreeItemsSub";
    Tree tree = null;
    try{
      TestObject last = (TestObject)((TestObject)nitem).getProperty("lastPathComponent");
      log.logMessage(testRecordData.getFac(), "l: "+last, DEBUG_MESSAGE);
      tree = tobj(last);
      ((TestObject)nitem).unregister();
    } catch (Exception ee) {
      ee.printStackTrace();
      throw new SAFSException(this, methodName, ee.getMessage());
    }
    return tree;
  }

  /** <br><em>Purpose:</em> extract a 'Tree' hierarchy from a TestObject using recursion;
   ** The item is for JTree node.
   * <br><em>Assumptions:</em>
   * <br>this is the mechanism used (gleaned through lots of trial and error):
   * @param                     obj, Object (actually of type GuiSubitemTestObject)
   * @return                    org.safs.Tree
   * @exception                 Exception
   **/
  protected Tree tobj(TestObject nitem) throws Exception {
    if (nitem == null) return null;
    Tree tree = new Tree();
    tree.setLevel((Integer)nitem.getProperty("level"));
    log.logMessage(testRecordData.getFac(),"level: "+tree.getLevel(),
                   DEBUG_MESSAGE);
    tree.setSiblingCount((Integer)nitem.getProperty("siblingCount"));
    log.logMessage(testRecordData.getFac(),"siblingCount: "+tree.getSiblingCount(),
                   DEBUG_MESSAGE);
    tree.setChildCount((Integer)nitem.getProperty("childCount"));
    log.logMessage(testRecordData.getFac(),"childCount: "+tree.getChildCount(),
                   DEBUG_MESSAGE);
    // this gets the actual object String, or if it wasn't a String, a TestObject
    Object obj = nitem.getProperty("userObject");
    Object userObj = getUserObject(obj, tree.getLevel());
    tree.setUserObject(userObj);

    // recurse for our children
    if (tree.getChildCount().intValue() > 0) {
      Object fl = nitem.getProperty("firstChild");
      if (fl != null) {
        tree.setFirstChild(tobj((TestObject)fl));
      }
    }

    // get next sibling
    Object ns = nitem.getProperty("nextSibling");
    if (ns != null) {
      tree.setNextSibling(tobj((TestObject)ns));
    }
    return tree;
  }
  /** <br><em>Purpose:</em> getUserObject, should be overridden by children.
   ** Check if obj instanceof TestObject, if not, return obj;
   ** This version hard codes the equivalent of 'obj.toString()' if it is a test object.
   * @param                     obj, Object obj
   * @param                     level, Integer
   * @return                    either obj, or fi a testobject, the output of 'obj.toString()'
   **/
  protected Object getUserObject (Object obj, Integer level) {
    if (obj instanceof TestObject) {

      //listAllProperties((TestObject)obj);

      TestObject anObj = (TestObject) obj;
      String stuff = (String) anObj.invoke("toString");
      Log.info(StringUtils.getSpaces(level)+ stuff);

      ////?? FOR NOW, HARD CODE A TEST VALUE??!!??!?!?!?!*##(@*)#!*@(@&@_(_@#()_@
      //String stuff = "unknown"+obj;
      //Log.info(StringUtils.getSpaces(level)+ stuff);

      return stuff;

    } else {
      Log.info(StringUtils.getSpaces(level)+ obj);
      return obj;
    }
  }

  /**
   * Determine if the TestObject is an Eclipse SWT component.
   * @param theobj, TestObject reference to GUI component
   * @return true if the component classname begins with 'org.eclipse.swt.'
   */
  protected boolean isSWTWidget(TestObject theobj){
  	try{ return (theobj.getObjectClassName().indexOf("org.eclipse.swt.")==0);}
  	catch(Throwable t){ return false; }
  }
    
  /** clear the cache of the test objects maintained by the appmap class,
   ** plus return the new TestObject for the windowName anc compName
   ** @param windowName, String
   ** @param compName, String
   ** @return, after clearing the cache, returns the new TestObject if found
   **/
  private Object localClearAppMapCache(String windowName, String compName) {
    if (windowName==null || compName==null) return null;
    String mapname = testRecordData.getAppMapName();
    DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
    Object obj = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
    return obj;
  }

	/**
	 * getParentBrowser
	 * Given a component assumed to be inside an Applet, we will attempt to
	 * locate the TopLevelTestObject in the "Html" domain that has the
	 * containing Java Plug-in object overlapping the same screen location.
	 *
	 * Example of use is inputKeys to an applet component.
	 * Applets and the HTMLAppletProxy do not have inputKeys and there is no
	 * easy way to get hold of the parent Browser object for an applet.  We
	 * must simply get the Point on screen of our Component and then attempt to
	 * find a TopLevelTestObject (BrowserTestObject?) whose child at that same
	 * Point is a Java Plug-In ActiveX control.
	 *
	 * It is possible that there will be multiple browsers running Applets on
	 * screen at the same time.  We will match the first one whose applet screen
	 * coordinates overlap our component screen coordinates.  This will almost
	 * always give us the right browser window, but it may be possible for 2
	 * browsers with applets to reside one on top of the other and it may be
	 * possible that we will find the wrong one first.  There isn't much we can
	 * do about that given the API and object properties currently available.
	 *
	 * If we do see this error in the future we may have to do additional checks
	 * like checking process ids, applet rectangle coordinates, and the like, but
	 * even these can still result in false matches because 2 browsers could be
	 * showing the same applet, the same size, and in 2 browsers that have the
	 * same process id.
	 *
	 * @param GuiTestObject component assumed to be inside a browser applet.
	 * @return TopLevelTestObject of the matching Browser or null.
	 */
    protected TopLevelTestObject getAppletsBrowser(GuiTestObject guiObj){
		DomainTestObject[] domains = script.getDomains();
		DomainTestObject domain;
		TestObject[] wins;
		TopLevelTestObject win;
		String sdomain;
		java.awt.Point loc = guiObj.getScreenPoint();
		Object pointcheck;
		GuiTestObject guipoint;

		for (int d=0; d < domains.length; d++){
			domain = domains[d];
			sdomain = (String) domain.getName();
			if (sdomain.equalsIgnoreCase("HTML")){
				wins = domain.getTopObjects();
				for( int w = 0; w < wins.length; w++){
					if (wins[w] instanceof TopLevelTestObject){
						win = (TopLevelTestObject) wins[w];
						pointcheck = win.getChildAtPoint(loc);
						if( pointcheck instanceof GuiTestObject) {
		  					guipoint = (GuiTestObject) pointcheck;
		  					try{
		  						sdomain = (String) guipoint.getProperty("classid");
		  						if ((sdomain.indexOf("8AD9C840-044E-11D1-B3E9-00805F499D93") > 0)||
		  						    (sdomain.indexOf("CAFEEFAC-0014-0000-0000-ABCDEFFEDCBA") > 0)){
				  					return win;
		  						}
		  					}
		  					catch(Exception pnfx){;}
						}
					}
				}
			}
		}
		// no HTML domain match found?
		return null;
    }
    
    protected static boolean isJavaDomain(TestObject tobj){
      String domainName = tobj.getDomain().getName().toString();
  	  return RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
    protected static boolean isDotnetDomain(TestObject tobj){
      String domainName = tobj.getDomain().getName().toString();
      return RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
    protected static boolean isHtmlDomain(TestObject tobj){
      String domainName = tobj.getDomain().getName().toString();
  	  return RGuiObjectVector.DEFAULT_HTML_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
    protected static boolean isWinDomain(TestObject tobj){
        String domainName = tobj.getDomain().getName().toString();
    	return RGuiObjectVector.DEFAULT_WIN_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
    protected static boolean isSwtDomain(TestObject tobj){
        String domainName = tobj.getDomain().getName().toString();
    	return RGuiObjectVector.DEFAULT_SWT_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
    protected static boolean isFlexDomain(TestObject tobj){
        String domainName = tobj.getDomain().getName().toString();
    	return RGuiObjectVector.DEFAULT_FLEX_DOMAIN_NAME.equalsIgnoreCase(domainName);
    }
}
