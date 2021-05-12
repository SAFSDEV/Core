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
package org.safs.rational;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.Tree;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;


/**
 * <br><em>Purpose:</em> CFMenuBar, process a MenuBar (Swing) component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   JUL 16, 2003    (DBauman) 	Original Release
 *   <br>   JAN 23, 2006    (Carl Nagle) 	Catch some unexpected exceptions for non-Swing components
 * 	 <br>	APR 15, 2008	(Lei Wang)	Fix bug of keyword SelectMenuItem: 
 * 										bug example: T,SwingWindow,PTools,SelectMenuItem,,  if PTools is a MenuItem, this record will not work
 * 										Add support to check menu status
 * 										Add keyword VerifyPopupMenu,SelectUnverifiedPopupMenuItem
 * 	 <br>	JUL 25, 2008	(LwiWang)	Add protected method isMenuBar(), isMenuItem(), isPopupMenu(), 
 * 															 getPropertyText(),getNewTreeNode()
 * 															 staticExtractMenuItems()
 * 										Modify method convertToMap(), extractMenuItems(), 
 * 													  selectMenuBar(), getSubMenuItemFullPath(),
 * 													  getAbsoluteFileName(), getSubMenuItemCount()
 * 													  selectMenuItemWithVerification(), verifyMenuBar()
 * 													  verifyPopupMenu()
 *   <br>	SEP 10, 2008	(Lei Wang)	Modify method verifyPopupMenu(), use benchFileName as default test and diff file name
 *   									when test and diff file name are not provided. See defect S0533774.
 *   <br>	NOV 25, 2008	(Lei Wang)	Add method selectUnverifiedMenuItem().
 *   									So some dynamically generated menu can be selected without verification. See defect S0546870
 **/
public class CFMenuBar extends CFComponent {
  public static final String SELECTMENUITEM              		= "SelectMenuItem";
  public static final String SELECTMENUITEMCONTAINS      		= "SelectMenuItemContains";
  public static final String SELECTUNVERIFIEDMENUITEM        	= "SelectUnverifiedMenuItem";
  public static final String SELECTPOPUPMENUITEM         		= "SelectPopupMenuItem";
  public static final String SELECTUNVERIFIEDPOPUPMENUITEM		= "SelectUnverifiedPopupMenuItem";
  
  public static final String VERIFYMENUITEM              		= "VerifyMenuItem";
  public static final String VERIFYMENUITEMCONTAINS      		= "VerifyMenuItemContains";
  
  public static final String VERIFYPOPUPMENU 		     		= "VerifyPopupMenu";
  public static final String VERIFYPOPUPMENUCONTAINS     		= "VerifyPopupMenuContains";
  public static final String VERIFYPOPUPMENUITEM         		= "VerifyPopupMenuItem";
  public static final String VERIFYPOPUPMENUPARTIALMATCH 		= "VerifyPopupMenuPartialMatch";

//  public static final String CLASS_SWING_MENUBAR_NAME			= "javax.swing.JMenuBar";
  public static final String TEXT_PROPERTY        				= "text";
  public static final String ITEMCOUNT_PROPERTY   				= "itemCount";
  public static final String MENUCOUNT_PROPERTY   				= "menuCount";
  public static final String UITYPE_PROPERTY      				= "uIClassID";
  public static final String UITYPE_MENUBAR       				= "MenuBarUI";
  public static final String UITYPE_POPUPMENU     				= "PopupMenuUI";
//  public static final String UITYPE_MENU          			= "MenuUI";
//  public static final String UITYPE_MENUITEM      			= "MenuItemUI";
//  public static final String PATH_SEPARATOR       			= "->";
  public static final String PATH_PREFIX          				= "Path=";
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFMenuBar () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   * <br>This is our specific version. We subclass the generic CFComponent.
   * The types of objects handled here are '{@link GuiSubitemTestObject}' and 
   * '{@link ToggleGUITestObject}'.
   * Path Example: "Admin->Customers..."
   * The actions handled here are:
   * <br><ul>
   * <li>selectmenuitem
   * <li>selectmenuitemcontains
   * <li>verifymenuitem
   * <li>verifymenuitemcontains
   * </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      // then we have to process for specific items not covered by our super
      log.logMessage(testRecordData.getFac(),
                     getClass().getName()+".process, searching specific tests...",
                     DEBUG_MESSAGE);
      
      if (action != null) {
        Log.info(".....CFMenuBar.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if ((action.equalsIgnoreCase(SELECTMENUITEM))        ||
            (action.equalsIgnoreCase(SELECTPOPUPMENUITEM))) {
        	selectMenuItem();
        }else if((action.equalsIgnoreCase(SELECTMENUITEMCONTAINS))){
        	selectMenuItemContains();
        }else if((action.equalsIgnoreCase(SELECTUNVERIFIEDMENUITEM))){
        	selectUnverifiedMenuItem();
        }else if(action.equalsIgnoreCase(SELECTUNVERIFIEDPOPUPMENUITEM)){
        	selectUnverifiedPopupMenuItem();
        }else if((action.equalsIgnoreCase(VERIFYMENUITEM))      || 
					(action.equalsIgnoreCase(VERIFYPOPUPMENUITEM)) 	) {
        	verifyMenuItem();
        }else if ((action.equalsIgnoreCase(VERIFYMENUITEMCONTAINS))   ||  
				(action.equalsIgnoreCase(VERIFYPOPUPMENUCONTAINS))	|| 
				(action.equalsIgnoreCase(VERIFYPOPUPMENUPARTIALMATCH))) {
        	verifyMenuItemContains();
        }else if(action.equalsIgnoreCase(VERIFYPOPUPMENU)){
        	verifyPopupMenu();
        }
        //all for now
      }
    } catch (com.rational.test.ft.SubitemNotFoundException snfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"SubitemNotFoundException: "+snfe.getMessage(),
          FAILED_MESSAGE);
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"ObjectNotFoundException: "+onfe.getMessage(),
          FAILED_MESSAGE);
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"SAFSException: "+ex.getMessage(),
          FAILED_MESSAGE);
    } catch (Exception unk) {
      unk.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
          unk.getClass().getName() + ": "+ unk.getMessage(),
          FAILED_MESSAGE);
    }
  }

  /**
   * <b>Purpose:</b>	Verify the existance of the path exactly, if it exists then select the path
   * @throws SAFSException
   */
  protected void selectMenuItem() throws SAFSException{
	// we CAN process menu selections with no additional information when using 
    // SAFS recognition strings that drill down to the item level.
    // So we don't need the path parameter if the third parameter is a menuItem.
  	// Example: T, WINDOW, FileMenu, SelectMenuItem

  	selectMenuBar(obj1,getPath(),false,true);
  }

  /**
   * <b>Purpose:</b>	Verify the existance of the path non-exactly, if it exists then select the path
   * @throws SAFSException
   */
  protected void selectMenuItemContains() throws SAFSException{
	// we always need the path parameter, so need to check
  	if (params.size() < 1) {
 	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
  				"Invalid parameter value for TextValue", "TextValue"));
      return;
    }

  	selectMenuBar(obj1,getPath(),true,true);
  } 

  /**
   * <b>Purpose:</b>	Select the path (exactly matched) without verification of it's existance
   * @throws SAFSException
   */
  protected void selectUnverifiedMenuItem() throws SAFSException{
	// we always need the path parameter, so need to check
  	if (params.size() < 1) {
 	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
  				"Invalid parameter value for TextValue", "TextValue"));
      return;
    }

  	selectMenuBar(obj1,getPath(),false,false);
  }
  
  /**
   * <b>Purpose:</b>	Select the path provided without the verification of existance of this path
   * @throws SAFSException
   */
  protected void selectUnverifiedPopupMenuItem() throws SAFSException{
	// we always need the path parameter, so need to check
  	if (params.size() < 1) {
   	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
				"Invalid parameter value for MenuItemText", "MenuItemText"));
      return;
    }

  	selectMenuBar(obj1,getPath(),false,false);  	
  }

  /**
   * <b>Purpose:</b>	Verify of existance of the provided path exactly
   * <br>				Verify whether the status of the menuitem matches the provided status (optional parameter)	
   * @throws SAFSException
   */
  protected void verifyMenuItem() throws SAFSException{
	// we always need the path parameter, so need to check
  	if (params.size() < 1) {
   	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
  				"Invalid parameter value for MenuItemText", "MenuItemText"));
      return;
    }

  	verifyMenuBar(obj1,getPath(),getStatus(),false);
  }
  
  /**
   * <b>Purpose:</b>	Verify of existance of the provided path non-exactly
   * <br>				Verify whether the status of the menuitem matches the provided status (optional parameter)	
   * @throws SAFSException
   */
  protected void verifyMenuItemContains() throws SAFSException{
	// we always need the path parameter, so need to check
  	if (params.size() < 1) {
   	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
  				"Invalid parameter value for MenuItemText", "MenuItemText"));
      return;
    }
  	
  	verifyMenuBar(obj1,getPath(),getStatus(),true);
  }
  
  /**
   * <b>Purpose:</b>	Verify the complete status of the current popup menu with a benchmark file.
   * @throws SAFSException
   */
  protected void verifyPopupMenu() throws SAFSException{
  	String debugmsg = getClass().getName()+".verifyPopupMenu() ";
  	String benchFileName=null, headerString=null, testFileName=null, diffFileName=null;
  	
  	//Analyse the four parameters, three of them are optional
  	//benchFileName (required),headerString (optional),testFileName (optional),diffFileName (optional)
  	if (params.size() < 1) {
     	  this.issueParameterCountFailure(FAILStrings.convert(FAILStrings.BAD_PARAM, 
  				"Invalid parameter value for BenchmarkFile", "BenchmarkFile"));
        Log.debug(debugmsg+" Missing parameter of 'benchFileName'!!!");
        return;
    }
  	Iterator iter = params.iterator();
  	String defaultFileName = iter.next().toString();
  	Log.info(debugmsg+" default file name (for bench,test,diff): "+defaultFileName);
  	benchFileName = getAbsoluteFileName(defaultFileName,STAFHelper.SAFS_VAR_BENCHDIRECTORY);
  	if(iter.hasNext()) headerString = iter.next().toString();
  	if(iter.hasNext()){
  		testFileName = iter.next().toString();
  		if(testFileName!=null && !testFileName.trim().equals("")){
  			testFileName = getAbsoluteFileName(testFileName,STAFHelper.SAFS_VAR_TESTDIRECTORY);
  		}else{
  			testFileName = getAbsoluteFileName(defaultFileName,STAFHelper.SAFS_VAR_TESTDIRECTORY);
  		}
  	}else{
  		testFileName = getAbsoluteFileName(defaultFileName,STAFHelper.SAFS_VAR_TESTDIRECTORY);
  	}
  	if(iter.hasNext()){
  		diffFileName = iter.next().toString();
  		if(diffFileName!=null && !diffFileName.trim().equals("")){
  			diffFileName = getAbsoluteFileName(diffFileName,STAFHelper.SAFS_VAR_DIFDIRECTORY);
  		}else{
  			diffFileName = getAbsoluteFileName(defaultFileName,STAFHelper.SAFS_VAR_DIFDIRECTORY);
  		}
  	}else{
  		diffFileName = getAbsoluteFileName(defaultFileName,STAFHelper.SAFS_VAR_DIFDIRECTORY);
  	}
  	
  	//1.Get the whole sub-tree of the JMenu provided (the third parameter)
  	GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
  	MenuTree tree = (MenuTree) extractMenuItems(guiObj,0);
  	//Get a list which contains all nodes' "path=status"
  	List treePathAndStatus = tree.getTreePaths("",true);
	
  	//2.Stroe the header-file into the test-file
  	//	Store the each node's path and it's status of the sub-tree into the "test-file"
	if(headerString!=null && !headerString.trim().equals("")){
  		Log.debug(debugmsg+testFileName+" will contains headerString: "+headerString);
  		treePathAndStatus.add(0,headerString);
  	}
	if(testFileName!=null && !testFileName.trim().equals("")){
  		Log.debug(debugmsg+"Menu status will be saved to "+testFileName);
  		try {
			StringUtils.writefile(testFileName,treePathAndStatus);
		} catch (IOException e) {
			String detail = failedText.convert(FAILStrings.FILE_ERROR,"Can not write to "+testFileName,testFileName);
			Log.debug(debugmsg+detail);
			throw new SAFSException(detail);
		}
  	}

  	//3.Compare with the bench-file; 
  	//		If mached, set the testRecord's status to OK.
  	//		Otherwise, set the testRecord's status to FAILURE and store the difference to diff-file.
  	List benchContents = new ArrayList();
  	try {
		benchContents.addAll(StringUtils.readfile(benchFileName));
	} catch (IOException e) {
		String detail = failedText.convert(FAILStrings.FILE_ERROR,"Can not read "+benchFileName,benchFileName);
		Log.debug(debugmsg+detail);
		throw new SAFSException(detail);
	}
	Log.debug(debugmsg+"BenchFile Menu's status:"+benchContents);
	Log.debug(debugmsg+"Current Menu's status:"+treePathAndStatus);
	
	//The list differences will contains the difference between current status and bench status
	List differences = new ArrayList();
	if(headerString!=null && !headerString.trim().equals("")){
		Log.debug(debugmsg+"Compare the header line.");		
		String benchHeader = benchContents.isEmpty()? "":benchContents.remove(0).toString();
		String currentHeader = treePathAndStatus.remove(0).toString();		
		Log.debug("benchHeader: "+benchHeader);
		Log.debug("currentHeader: "+currentHeader);
		if(!currentHeader.equalsIgnoreCase(benchHeader)){
			differences.add("Header different: \n"+							
							"benchHeader: "+benchHeader+"\n"+
							"currentHeader: "+currentHeader+"\n\n");
		}
	}
	//Convert List to Map which contains path as key and status as value
	Map benchStatusMap = convertToMap(benchContents);
	Map currentStatusMap = convertToMap(treePathAndStatus);
	//I.	If the two maps have the same path and the same status
	//II.	Else If the two maps have the same path but status are different, then write this difference to the list differences.
	//		Finally remove the path from both maps
	Object[] currentPathArray = currentStatusMap.keySet().toArray();
	for(int i=0;i<currentPathArray.length;i++){
		String key = currentPathArray[i].toString();
		String benchStatus = null;
		if(benchStatusMap.containsKey(key)){
			benchStatus = benchStatusMap.get(key).toString();
		}else{
			continue;
		}
		String currentStatus = currentStatusMap.get(key).toString();
		Log.debug("menu path: "+key);
		Log.debug("benchStatusValue: "+benchStatus);
		Log.debug("currentStatusValue: "+currentStatus);
		if(!benchStatus.equalsIgnoreCase(currentStatus)){
			differences.add("Menu path: "+key+" has difference: \n"+
							"benchStatus: "+benchStatus+"\n"+
							"currentStatus: "+currentStatus+"\n");
		}
		//Finally remove this path from both benchStatusMap and currentStatusMap
		benchStatusMap.remove(key);
		currentStatusMap.remove(key);
	}
	//III.	Else if MenuItem exist only in bench file, write this difference to list differences.
	if(benchStatusMap.size()!=0){
		differences.add("MenuItem exist only in "+benchFileName);
		Object[] keys = benchStatusMap.keySet().toArray();
		for(int i=0;i<keys.length;i++){
			String key = keys[i].toString();
			differences.add("Menu path: "+key+" ##  Status: "+benchStatusMap.get(key));
		}
	}
	//IV.	Else if MenuItem exist only in current menu, write this difference to list differences.
	if(currentStatusMap.size()!=0){
		differences.add("MenuItem exist only in current menu");
		Object[] keys = currentStatusMap.keySet().toArray();
		for(int i=0;i<keys.length;i++){
			String key = keys[i].toString();
			differences.add("Menu path: "+key+" ##  Status: "+currentStatusMap.get(key));
		}
	}
	
	//If the size of differences is bigger than 0, there are some differences between current status and bench status
	if(differences.size()>0){
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		String detail = failedText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
										   "the content of 'Current menu status' does not match the content of '"+benchFileName+"'",
										   "Current menu's status",benchFileName);
		if(diffFileName!=null && !diffFileName.trim().equals("")){
			try {
				StringUtils.writefile(diffFileName,differences);
				detail += " "+genericText.convert(GENStrings.SEE_DIFFERENCE_FILE,
						  						  "\n\tPlease see difference in file '"+diffFileName+"'.",
												  diffFileName);
			} catch (IOException e1) {
				String message = failedText.convert(FAILStrings.FILE_ERROR,"Can not write to "+diffFileName,diffFileName);
				Log.debug(debugmsg+message);
			}
		}
		componentExecutedFailureMessage(detail);
		return;
	}
	
  	Log.debug(debugmsg+"All status of menu match that of benchfile.");
  	String detail = genericText.convert(GENStrings.CONTENT_MATCHES_KEY,
  										"the content of 'Current menu status' matches the content of '"+benchFileName+"'",
										"Current menu's status",benchFileName);
  	componentSuccessMessage(detail);
  	testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /** <br><em>Purpose:</em> Select a menuItem <br>
   * <br> We use an equivalent of this to accomplish the select:
   * jmbMenuBar().click(atPath("Admin->Customers..."));
   * @param menuObj 			-- GuiSubItemTestObject representing a JMenuBar or JPopupMenu or JMenuItem or JMenu or
   * 												 MainMenu, MenuItem, MenuStrip, and ToolStripMenuItem
   * @param menuItemPath 	-- String, path information to the final JMenu or JMenuItem or MenuItem or ToolStripMenuItem to be selected.
   * @param fuzzy 			-- If false, match the given path exactly with the Menu.
   * 				 		   This parameter will not be used if verify is false.
   * @param verify 			-- If true, verify existance of path before selecting.
   * @exception SAFSException 
   **/
  protected void selectMenuBar(TestObject menuObj,String menuItemPath,boolean fuzzy,boolean verify) throws SAFSException {
      
      String debugMsg = getClass().getName()+".selectMenuBar(): ";

      //Create a new StringBuffer path to store the full path when calling getSubMenuItemFullPath()
      StringBuffer path = new StringBuffer(menuItemPath);
    	
      GuiSubitemTestObject guiObj = null;
      //If the object is a sub-menu,
      //try to get the test proxy object of "MenuBar" or "PopupMenu" and get the full path.
      if(!isMenuBar(menuObj)){
    	Log.debug(debugMsg+"JSAFSBefore get full path: "+menuObj.getObjectClassName()+" $$ path="+path);
      	guiObj = getSubMenuItemFullPath(menuObj, path);
      	if(guiObj==null){
      		Log.debug(debugMsg+"Can not get the test object of 'MenuBar' or 'PopupMenu' for "+menuObj);
      		throw new SAFSException("Can not get the test object of 'MenuBar' or 'PopupMenu'.");
      	}
      	Log.debug(debugMsg+"After get full path: "+guiObj.getObjectClassName()+" $$ path="+path);
      }else{
      	guiObj = new GuiSubitemTestObject(menuObj.getObjectReference());
      }
      //Check the existance of path
      if(verify){
      	selectMenuItemWithVerification(guiObj, path.toString(),fuzzy);
      }else{
      	//If we don't verify the existance of path, no need the fuzzy parameter
      	selectMenuItemWithoutVerification(guiObj,path.toString());
      }
  }
  
  /** <br><em>Purpose:</em> Verify the existance of a path and verify it's status
   * @param anObj -- GuiSubItemTestObject representing a JMenuBar or JPopupMenu.
   * @param menuItemPath -- String, the path to be selected
   * @param status -- String, the menuItem status to be verified
   * @param fuzzy -- If false, match the given path exactly with the Menu.
   * @exception SAFSException 
   **/
  protected void verifyMenuBar(Object anObj,String menuItemPath,String status,boolean fuzzy) throws SAFSException {
      
      String debugmsg = getClass().getName()+".verifyMenuBar() ";
      TestObject testObj = (TestObject) anObj;
      
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(testObj.getObjectReference());
      log.logMessage(testRecordData.getFac(),"..... guiObj: "+guiObj, DEBUG_MESSAGE);
      log.logMessage(testRecordData.getFac(),"..... path: "+menuItemPath, DEBUG_MESSAGE);
      listNonValueProperties(guiObj);
      listProperties(guiObj);
      
      MenuTree atree = null;
      try {
        atree = (MenuTree) extractMenuItems(guiObj,0);
        log.logMessage(testRecordData.getFac(),"atree: "+atree, DEBUG_MESSAGE);
        // do the work of matching...        
        String match = null;
        match = atree.matchPath(menuItemPath,fuzzy,status);
        
        if (match != null) {
          log.logMessage(testRecordData.getFac(),"match: "+match, 
              DEBUG_MESSAGE);
          // set status to ok
          testRecordData.setStatusCode(StatusCodes.OK);
          String altText = "MenuItem \""+ match +"\", "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4, altText, match,
                                            windowName, compName, action),
                         PASSED_MESSAGE);
        } else {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
        		  		 debugmsg+": no match on: " + menuItemPath,
                         FAILED_MESSAGE);
        }
      } catch (SAFSException se) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
        			   debugmsg+": " + se.getMessage(),
                       FAILED_MESSAGE);
      }
  }
  
  /**
   * @param guiObj,	TestObject may be JMenuBar or JPopupMenu
   * @param path,	String is the full path of some MenuItem on which a click action will take place
   */
  protected void selectMenuItemWithoutVerification (GuiSubitemTestObject guiObj, String path) {
    com.rational.test.ft.script.List list = Script.localAtPath(path);
    //guiObj.setState(Action.select(), list); // RobotJ doesn't like this...
    try {
      guiObj.click(list);
      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
      String altText = "MenuItem \""+ path +"\" clicked, "+windowName+":"+compName+" "+action;
      log.logMessage(testRecordData.getFac(),
                     passedText.convert(PRE_TXT_SUCCESS_4, altText, path,
                                        windowName, compName, action),
                     PASSED_MESSAGE);
    } catch (TargetGoneException tge) {
    	Log.info("CFMenuBar IGNORING TargetGoneException probably resulting from intended window closure...");
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        String altText = "MenuItem \""+ path +"\" clicked, "+windowName+":"+compName+" "+action;
        log.logMessage(testRecordData.getFac(),
                       passedText.convert(PRE_TXT_SUCCESS_4, altText, path,windowName, compName, action),
                       PASSED_MESSAGE);
    } catch (NullPointerException npe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),"item not found: "+path,
          FAILED_MESSAGE);
    }  	
  }

  /**
   * @param guiObj,	TestObject may be JMenuBar or JPopupMenu
   * @param path,	String is the full path of some MenuItem on which a click action will take place
   * @param fuzzy,	boolean, If true, we will do a non-exact verification of existance of path
   */
  protected void selectMenuItemWithVerification(GuiSubitemTestObject guiObj, String path,boolean fuzzy) {
    MenuTree atree = null;
    try {
      atree = (MenuTree) extractMenuItems(guiObj,0);
      log.logMessage(testRecordData.getFac(),"atree: "+atree, DEBUG_MESSAGE);
      //Do the work of matching..., verify the path
      //If fuzzy is true, do the partial match; otherwise do the exact match
      String match = atree.matchPath(path,fuzzy,null);
      if (match != null) {
        log.logMessage(testRecordData.getFac(),"match: "+match, DEBUG_MESSAGE);
        com.rational.test.ft.script.List slist = Script.localAtPath(match);
        try {
          guiObj.click( slist);
          // set status to ok
          testRecordData.setStatusCode(StatusCodes.OK);
          String altText = "MenuItem \""+ match +"\" clicked, "+windowName+":"+compName+" "+action;
          log.logMessage(testRecordData.getFac(),
                         passedText.convert(PRE_TXT_SUCCESS_4, altText, match,windowName, compName, action),
                         PASSED_MESSAGE);
        } catch (TargetGoneException tge) {
        	Log.info("CFMenuBar IGNORING TargetGoneException probably resulting from intended window closure...");
            // set status to ok
            testRecordData.setStatusCode(StatusCodes.OK);
            String altText = "MenuItem \""+ match +"\" clicked, "+windowName+":"+compName+" "+action;
            log.logMessage(testRecordData.getFac(),
                           passedText.convert(PRE_TXT_SUCCESS_4, altText, match,windowName, compName, action),
                           PASSED_MESSAGE);
        } catch (NullPointerException npe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),"item not found: "+path,FAILED_MESSAGE);
        }
      } else {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),getClass().getName()+": no match on: " + path,FAILED_MESSAGE);
      }
    } catch (SAFSException se) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),getClass().getName()+": " + se.getMessage(),FAILED_MESSAGE);
    }
  }
 
  // insure a never-null path value
  /**
   * Retrieves the "path" parameter for commands like SelectMenuItem and SelectMenuItemContains.  
   * The routine handles the "NoSuchElementException" if no "path" parameter is provided.
   * It also makes sure we get a valid path, or an empty string.  We should never get a null.
   * The routine stores the value in the private "path" field for repeated referencing.
   * 
   * @return String path parameter or an empty string if none is available.
   **/
  protected String getPath(){
  	String path = null;
  	{
  		try{
  			path = (String) params.iterator().next();
  			String rpath = lookupAppMapReference(path);
  			if (rpath != null) {
  				if(rpath.substring(0,PATH_PREFIX.length()).equalsIgnoreCase(PATH_PREFIX)) {
  					rpath = rpath.substring(PATH_PREFIX.length());
  				}
  				path = rpath;
  			}
  		}
  		catch(NoSuchElementException nse){;}
  		if (path == null) path = "";
  	}
  	return path;
  }
  /**
   * @return	String the menuitem status to be verified which is an optional parameter.
   */
  protected String getStatus(){
  	String status = null;
    Iterator iter = params.iterator();
    //skip the path parameter
    iter.next();
    if(iter.hasNext()){
    	status = iter.next().toString();	
    }
  	return status;
  }
  
  /**
   * Return the number of immediate child Menus or MenuItems from the provided JMenuBar or JMenu.
   * The routine will look for both the itemCount property and menuCount property--whichever it 
   * finds to be present.
   * 
   * @param aMenuObj -- Typically a JMenuBar or JMenu proxy.  Will be cast to TestObject.
   * @return Integer -- the number of child Menus or MenuItems or 0.
   **/
  protected Integer getSubMenuItemCount(TestObject aMenuObj){
  	Integer val = new Integer(0);
  	TestObject anObj = null;
  	try{anObj = (TestObject) aMenuObj;
  		try{val = (Integer) anObj.getProperty(ITEMCOUNT_PROPERTY);}
  		catch(PropertyNotFoundException pnf) { 
			val = (Integer) anObj.getProperty(MENUCOUNT_PROPERTY);}
  	} catch(Exception ex) {;}
  	return val;
  }
  
  
  /**
   * theObj is expected to be a JMenu TestObject proxy (Usually a ToggleGUITestObject).
   * Path information (might not have length > 0).  
   * <p>
   * We are then going to go UP the tree to make the full path from menubar down to the subitem.
   * <p>
   * @param theObj -- The JMenu proxy to be evaluated.
   * @param path -- The initial path provided prior to us filling in the full path up the hierarchy.
   * 				This must be the child path of "theObj"
   * 
   * @return		GuiSubitemTestObject, the test proxy object of "JMenuBar" or "JPopupMenu". 
   * 				The parameter path will contain the full path to the ancestor whose type is "JMenuBar" or "JPopupMenu"
   **/  
  protected GuiSubitemTestObject getSubMenuItemFullPath(TestObject theObj, StringBuffer path)
  										 throws SAFSException {
	String debugmsg = getClass().getName()+".getSubMenuItemFullPath() ";
  	String text = null;
  	TestObject proxyObj = new TestObject(theObj.getObjectReference());
	GuiSubitemTestObject guiSbuitemObj = null;
	
	//Try to get the ancestor of this test object,
	//The ancestor must be the test proxy object of "MenuBar" or "PopupMenu"
	boolean isMenuBarOrPopupMenu = false;

 	while((! isMenuBarOrPopupMenu)&&(proxyObj != null)){
  		if(!isMenuBarOrPopupMenu){
  			text = getPropertyText(proxyObj);
  			if(path.length()>0) path.insert(0,text + Tree.PATH_SEPARATOR);
  			else path.insert(0,text);
  		}
  		proxyObj = proxyObj.getMappableParent();
  		isMenuBarOrPopupMenu = isMenuBar(proxyObj);
  	}
  	if(isMenuBarOrPopupMenu){
		guiSbuitemObj = new GuiSubitemTestObject(proxyObj.getObjectReference());
	}else{
		Log.debug(debugmsg+"Can not get ancestor of type 'Menu Bar' or 'Popup Menu' for "+theObj.getObjectClassName());
	}
  	
  	return guiSbuitemObj;
  }

  /**
   * <em>Note:</em>			This method is used by the RDDGUIUtilities.java
   */
  public static Tree staticExtractMenuItems (Object obj, int level) throws SAFSException {
	  return new CFMenuBar().extractMenuItems(obj, level);
  }
  
  /** <br><em>Purpose:</em> 	Extract a menu hierarchy from a TestObject;
   * 							The item is for JMenuBars, JPopupMenus, JMenus, and JMenuItems (JMenu is subclass of JMenuItem).
   * 							This routine is reentrant until there are no more submenus to process.
   * <br><em>Assumptions:</em>  obj is a MenuBar, PopupMenu, Menu or MenuItem TestObject proxy.
   * <br><em>Note:</em>  		For this method works for application other than java-swing, we need to override
   * 							getSubMenuItemCount(),getPropertyText(),getPropertyTextName(),getNewTreeNode()
   * 							in the subclass like CFDotNetMenuBar.
   *  
   * The following tree show the structure of JMenuBar
   * 	JMenuBar
   *       |____________________________________
   *            |               |               |
   *          JMenu          JMenu            JMenu
   *         ___|___            |               |
   *        |       |
   *    JMenuItem  JMenu
   *              __|__
   *             |     |
   *       JMenuItem JMenuItem
   * 
   * @param                     obj, Object (TestObject)
   * @param                     level, what level in the tree are we processing
   * @return                    org.safs.Tree, the real instance is org.safs.rational.MenuTree
   * @exception                 SAFSException
   **/
  protected Tree extractMenuItems (Object obj, int level) throws SAFSException {
    
    String debugmsg = getClass().getName()+".extractMenuItems() ";
    Tree tree = null;
    TestObject[] subitems = null;
    
    try{
      TestObject tobj = (TestObject) obj;
	  Integer itemCount = null;
	  
      if(isPopupMenu(tobj)) {      	  
      	  subitems = tobj.getMappableChildren();
      	  itemCount = new Integer(subitems.length);
      }else{      	
          itemCount = getSubMenuItemCount(tobj);
          
          // having some problems in V2003
          if (itemCount.intValue() > 0 )  subitems = tobj.getMappableChildren();
          try{itemCount = new Integer(subitems.length);}
          catch(Exception x){itemCount = new Integer(0);}
      }
	  
      Tree lastjTree = null;
      
      for(int j=0; j<itemCount.intValue(); j++) {
        TestObject gto2 = null;
        try {
          gto2 = subitems[j];
        } catch (ArrayIndexOutOfBoundsException aie) {
          Log.debug("ArrayIndexOutOfBoundsException for level: "+level+", menuitem: "+j+", probably your menu has a separator or some other unknown object, continuing...");
          continue;
        }
        
        String text2 = getPropertyText(gto2);
        
        // do NOT increment level for what appears to be a SubMenu placeholder
		int inc = (text2==null)? 0:1;
		
        Integer itemCount2 = getSubMenuItemCount(gto2);
        if (itemCount2.intValue()==0){
        	TestObject[] subkids = gto2.getMappableChildren();
        	if (subkids != null) itemCount2=new Integer(subkids.length);
        }
        
        Log.debug("level "+ level +": item "+j+": "+ getPropertyTextName() +" \""+text2+"\" "+" children: "+itemCount2);
        
        //Use test object to form a tree node
        Tree jtree = new MenuTree();
        MenuTreeNode treeNode = getNewTreeNode(gto2,itemCount.intValue(),itemCount2.intValue());
        jtree.setUserObject(treeNode);

        if (j==0) tree = jtree;
        else { lastjTree.setNextSibling(jtree); }

        jtree.setLevel(new Integer(level));
        jtree.setSiblingCount(itemCount);
        jtree.setChildCount(itemCount2);
        if (itemCount2.intValue() > 0){
          // inc only when a valid new level exists
          Tree subtree = extractMenuItems(gto2, level+inc);
          jtree.setFirstChild(subtree);
        }
        lastjTree = jtree;
      }  
    } catch (Exception ee) {
      ee.printStackTrace();
      throw new SAFSException(debugmsg +": "+ ee.getMessage());
    }
    return tree;
  }
  
  /** 
   * @param filename	String
   * @param parentDir	String, the parent directory of the filename
   * @return			String, absolute file name got from filename and it's parent
   * @throws SAFSException
   */
  protected String getAbsoluteFileName(String filename,String parentDir) throws SAFSException{
	String debugMsg = getClass().getName() + ".getAbsoluteFileName(): ";
	
    //Get the bench and test absolute fileName
	if(filename==null||filename.equals("")||filename.endsWith(File.separator)){
		String detail = failedText.convert(FAILStrings.BAD_PARAM,"Invalid parameter value for FileName","FileName");
		Log.debug(debugMsg+detail);
		throw new SAFSException(detail);
	}
    
	File fn = new CaseInsensitiveFile(filename).toFile();
	if (!fn.isAbsolute()) {
    	String pdir = getVariable(parentDir);

    	if(pdir==null){
			String detail = failedText.convert(FAILStrings.STAF_ERROR,
					"SATF ERROR: Can not get variable "+parentDir,
					"Can not get variable "+parentDir);	
        	Log.debug(debugMsg+detail);		
        	throw new SAFSException(detail);
    	}

		return new CaseInsensitiveFile(pdir, filename).toFile().getAbsolutePath();
	}
	
	return filename;
  }
  
  /**
   * @param 	pathToStatus, String contains "path=status". Example: "File->Open=Enabled"
   * @return	Map, contains path as key and status as value
   */
  protected Map convertToMap(List pathToStatusList){
  	String debugmsg = getClass().getName()+".convertToMap() ";
  	Map map = new HashMap();
  	String pathStatus = null;
  	StringTokenizer tokens = null;
  	
  	for(int i=0;i<pathToStatusList.size();i++){
  		pathStatus = pathToStatusList.get(i).toString();
  		if(pathStatus.contains(Tree.EQUAL_SEPARATOR)){
  			tokens = new StringTokenizer(pathStatus,Tree.EQUAL_SEPARATOR);
  			map.put(tokens.nextToken(),tokens.nextToken());
  		}else{
  			Log.debug(debugmsg+" Missing status: "+pathStatus);
  		}
  	}
  	
  	return map;
  }
  
  /**
   * <em>Note:</em> 		Needed to be override in subclass. "uIClassID" is a property specific for java swing object,
   * 						we use this property to test what UI Component it is. This can only work for swing.
   * @param menuObject		A TestObject represents a MenuBar or PopupMenu or Menu or MenuItem
   * @return				True if the TestObject is MenuBar or PopupMenu. False otherwise.
   * @throws				SAFSException
   */
	protected boolean isMenuBar(TestObject menuObject) throws SAFSException{
		String debugmsg = getClass().getName()+".isMenuBar() ";
		boolean isMenuBar = false;

		String ui = null;
		try {
		    //Other non-Swing components may not have similar properties
			ui = (String) menuObject.getProperty(UITYPE_PROPERTY);
		} catch (PropertyNotFoundException x) {
			Log.debug(debugmsg+x.getMessage()+". Property "+UITYPE_PROPERTY+" not found for"+menuObject.getObjectClassName());
			throw new SAFSException("Property "+UITYPE_PROPERTY+" not found for"+menuObject.getObjectClassName());
		}

		isMenuBar = UITYPE_MENUBAR.equalsIgnoreCase(ui) ||
					UITYPE_POPUPMENU.equalsIgnoreCase(ui);

		return isMenuBar;
	}

	/**
	 * <em>Note:</em> 			Needed to be override in subclass. We just call isMenuBar() to make the test.
	 * @param menuObject		A TestObject represents a MenuBar or PopupMenu or Menu or MenuItem
	 * @return					True if the TestObject is Menu or MenuItem. False otherwise.
	 * @throws					SAFSException
	 */
	protected boolean isMenuItem(TestObject menuObject) throws SAFSException{
		return !isMenuBar(menuObject);
	}
	/**
	 * @param menuObject
	 * @return					True if the menuObject is a popupMenu; False otherwise.
	 * @throws					SAFSException
	 */
	protected boolean isPopupMenu(TestObject menuObject) throws SAFSException{
		String debugmsg = getClass().getName()+".isPopupMenu() ";
		boolean isMenuBar = false;

		String ui = null;
		try {
			ui = (String) menuObject.getProperty(UITYPE_PROPERTY);
		} catch (PropertyNotFoundException x) {
			Log.debug(debugmsg+x.getMessage()+". Property "+UITYPE_PROPERTY+" not found for"+menuObject.getObjectClassName());
			throw new SAFSException("Property "+UITYPE_PROPERTY+" not found for"+menuObject.getObjectClassName());
		}

		isMenuBar = UITYPE_POPUPMENU.equalsIgnoreCase(ui);

		return isMenuBar;
	}
	
	/**
	 * <em>Note:</em>		Try to get The value of "text" property of a menuitem.
	 * 						Needed to be overrided for other application than Swing.
	 * 						For example: For java, "text" is the text property. But for .NET, "Text" is.
	 * @param menuObj		A TestObject represents menu or menuItem.
	 * @return				The value of "text" property of a menuitem.
	 */
	protected String getPropertyText(TestObject menuObject){
		String debugmsg = getClass().getName()+".getTextProperty() ";
		String text = "";
		
		try{
			text = (String) menuObject.getProperty(TEXT_PROPERTY);
		}catch (PropertyNotFoundException x) {
			Log.debug(debugmsg+x.getMessage()+". Property "+TEXT_PROPERTY+" not found for"+menuObject.getObjectClassName());
		}
		
		return text;
	}
	
	protected String getPropertyTextName(){
		return TEXT_PROPERTY;
	}
	
	/**
	 * <em>Note:</em>			Needed to be overrided for other application than Swing.
	 * @param userObject
	 * @param siblingCounter
	 * @param childrenCounter
	 * @return					An apporiate MenuTreeNode. 
	 * 							For Example, a JavaMenuTreeNode for java application;
	 * 							a DotNetMenuTreeNode for .NET application.
	 */
	protected MenuTreeNode getNewTreeNode(Object userObject,int siblingCounter,int childrenCounter){
		return new JavaMenuTreeNode(userObject,siblingCounter,childrenCounter);
	}
	
	/**
	 * Turn the menu to a tree, and return each level of the tree node.
	 * 
	 * @param guiObj GuiTestObject to snapshot data from.
	 * @return List of a menu tree
	 * 
	 * Example:
	 * A menu is:
	 * File		Edit
	 * Open		Cut
	 * Close	Paste
	 * 
	 * Output list will be:
	 * File
	 * File->Open
	 * File->Close
	 * Edit
	 * Edit->Cut
	 * Edit->Paste
	 * 
	 * @throws SAFSException
	 * @throws IllegalArgumentException if table is not an acceptable GuiTestObject.
	 * 
	 * @see CFComponent#captureObjectData(TestObject)
	 * @see CFComponent#formatObjectData(java.util.List)
	 */
	protected java.util.List captureObjectData(TestObject guiObj)
			throws IllegalArgumentException, SAFSException {
		MenuTree tree = (MenuTree) extractMenuItems(guiObj, 0);
		// Get a list which contains all nodes's path
		List treePaths = tree.getTreePaths("", false);

		return treePaths;
	}
}


