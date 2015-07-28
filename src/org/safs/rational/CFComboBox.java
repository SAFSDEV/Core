/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.Rectangle;
import java.io.File;
import java.util.Iterator;
import java.util.ListIterator;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.text.FAILStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.SubitemNotFoundException;
import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.SelectGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;
import com.rational.test.ft.script.Action;
import com.rational.test.ft.script.CaptionText;

/**
 * <br><em>Purpose:</em> CFComboBox, process a ComboBox component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 17, 2003
 *
 *   <br>   JUL 17, 2003     (DBauman) Original Release
 *   <br>   MAR 18, 2004     (BNat) Select keyword updated with Null Pointer Exception.
 *   <br>   JUN 21, 2005     (Jeremy_J_Smith) Post-Set verification now 
 *                            consistent with regular VERIFYSELECTED method. 
 *   <br>   JUN 22, 2005     (Jeremy_J_Smith) Post-Set verification enhancements 
 *                            and new SETUNVERIFIEDTEXTVALUE action.
 *   <br>   JUL 10, 2008     (JunwuMa)Modified methods verifySelectedText() and verifyTextProperty() to 
 *                               ensure they can work with .NET objects for SELECT and SELECTTEXTITEM,VERIFYSELECTED and VERIFYSELECTEDITEM.
 *          JUL 11, 2008     (JunwuMa)Added getItems() supporting .NET for CAPTUREITEMSTOFILE,SELECTPARTIALMATCH.
 *                               Switched on .NET domain support in SetTextValue() for SETTEXTVALUE and SETUNVERIFIEDTEXTVALUE
 *                               All keywords in this class are already .NET supported.
 *   <br>	SEP 12, 2008	(LeiWang)	Modify hideCombo() and showCombo(). Right-click can not hide the combo list.
 *   									A simple click can open and close the combo list. But before using showCombo(), we are must
 *   									suer the combobox is hide; otherwise the combobox will be closed. The same to hideCombo().
 *	<br>	OCT 04, 2008	(LeiWang)	Modify method verifySelectedText(). Test the object type before casting. See defect S0534980.
 *	<br>	NOV 25, 2008	(LeiWang)	Modify method setTextValue(): If value contains special, do not verify. See defect S0546329.
 *	<br>	DEC 03, 2008	(Carl Nagle)	Modify SelectPartialMatch to selectAtIndex if selectAtText fails after 4 attempts. 
 *	<br>	DEC 04, 2008	(LeiWang)	Modify method setTextValue(): Include win domain to be processed. Now Java, Net, Win domain are included.
 *																	  For win doamin, keywords with verification are not supported.
 *																	  See defect S0550143.
 *	<br>	DEC 10, 2008	(LeiWang)	Modify method getItems(): treat combobox in toolbar, the type of combobox is
 *										"System.Windows.Forms.ToolStripComboBox+ToolStripComboBoxControl"
 *	<br>	DEC 24, 2008	(LeiWang)	Modify method verifySelectedText(): add support for window ComboBox. See defect S0554263.
 * **/
public class CFComboBox extends CFComponent {

  public static final String CAPTUREITEMSTOFILE         = "CaptureItemsToFile";
  public static final String SHOWLIST                   = "ShowList";
  public static final String HIDELIST                   = "HideList";
  public static final String SELECT                     = "Select";
  public static final String SELECTTEXTITEM             = "SelectTextItem";
  public static final String SELECTINDEX                = "SelectIndex";
  public static final String SELECTPARTIALMATCH         = "SelectPartialMatch";
  public static final String SELECTUNVERIFIED           = "SelectUnverified";
  public static final String SELECTUNVERIFIEDTEXTITEM   = "SelectUnverifiedTextItem";
  public static final String SETTEXTVALUE               = "SetTextValue";
  public static final String SETUNVERIFIEDTEXTVALUE     = "SetUnverifiedTextValue";
  public static final String VERIFYSELECTED             = "VerifySelected";
  public static final String VERIFYSELECTEDITEM         = "VerifySelectedItem";
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFComboBox () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li>hidelist
   ** <li>showlist
   ** <li>select
   ** <li>selectTextItem
   ** <li>selectIndex
   ** <li>selectPartialMatch
   ** <li>selectUnverified
   ** <li>selectUnverifiedTextItem
   ** <li>setTextValue
   ** <li>setUnverifiedTextValue
   ** <li>verifySelected
   ** <li>verifySelectedItem
   ** </ul><br>
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
        Log.info(".....CFComboBox.process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(VERIFYSELECTED) ||
            action.equalsIgnoreCase(VERIFYSELECTEDITEM)) {
          verifySelected();
        } else if (action.equalsIgnoreCase(CAPTUREITEMSTOFILE)) {
          captureItemsToFile();
        } else if (action.equalsIgnoreCase(SETTEXTVALUE)) {
            setTextValue(true);
        } else if (action.equalsIgnoreCase(SETUNVERIFIEDTEXTVALUE)) {
            setTextValue(false);
        } else if (action.equalsIgnoreCase(SELECT) ||
            action.equalsIgnoreCase(SELECTTEXTITEM) ||
            action.equalsIgnoreCase(SELECTINDEX) ||
            action.equalsIgnoreCase(SELECTPARTIALMATCH) ||
            action.equalsIgnoreCase(SELECTUNVERIFIED) ||
            action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)) {
        	doSelect(action);
        } else if (action.equalsIgnoreCase(HIDELIST)) {
          GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
          hideCombo(guiObj);
          // set status to ok
          log.logMessage(testRecordData.getFac()," "+action+" ok", PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else if (action.equalsIgnoreCase(SHOWLIST)) {
          GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
          showCombo(guiObj);
          // set status to ok
          log.logMessage(testRecordData.getFac()," "+action+" ok", PASSED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        }

        //all for now
      }
    } catch (NullPointerException npe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "Item not in list for: "+action+", "+params,
                     FAILED_MESSAGE);
    } catch (WrappedException we) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "WrappedException: possibly the component is not visible, or possibly this"+
                     " action is really not supported; window: "+ windowName +"; comp: "+compName+
                     "; msg: "+we.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.UnsupportedActionException uae) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "UnsupportedAction: possibly the component is not visible, or possibly this"+
                     " action is really not supported; window: "+ windowName +"; comp: "+compName+
                     "; msg: "+uae.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "ObjectNotFound: the window or component are not visible or do not exist;"+
                     " window: "+ windowName +"; comp: "+compName+
                     "; msg: "+onfe.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (com.rational.test.ft.TargetGoneException tge) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "TargetGone: the window or component are unexpectedly gone;"+
                     " window: "+ windowName +"; comp: "+compName+
                     "; msg: "+tge.getMessage(),
                     FAILED_MESSAGE);
    }
    catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getClass().getName()+", msg: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /**
   * try both click and setState(Action.select() means to select an item by text value
   */
  private boolean selectTextItem(GuiSubitemTestObject guiObj, String itemtext){
	    try{
	      guiObj.click(script.localAtText(itemtext)); } 
	    catch (Exception e) {
	      try{ 
	        showCombo(guiObj);
	      	guiObj.click(script.localAtText(itemtext)); }
	      catch(Exception x){
	      	try{ 
	      		guiObj.setState(Action.select(), script.localAtText(itemtext)); }
	      	catch(Exception y){
	          try{
	      		showCombo(guiObj);
	      		guiObj.setState(Action.select(), script.localAtText(itemtext)); }
	      	  catch(Exception z){
	            return false;
	      	  }
	      	}
	      }
	    }
	    return true;	   
  }
  
  /**
   * try both click and setState(Action.select() means to select an item by index
   */
  private boolean selectItemAtIndex(GuiSubitemTestObject guiObj, int index){
	    try{
	      guiObj.click(script.localAtIndex(index));
	    } 
	    catch (Exception e) {
	      try{ 
	        showCombo(guiObj);
	      	guiObj.click(script.localAtIndex(index));}
	      catch(Exception x){
	      	try{ 
	      		guiObj.setState(Action.select(), script.localAtIndex(index));}
	      	catch(Exception y){
	          try{
	      		showCombo(guiObj);
	      		guiObj.setState(Action.select(), script.localAtIndex(index)); }
	      	  catch(Exception z){
	            return false;
	      	  }
	      	}
	      }
	    }
	    return true;
  }
  
  /** <br><em>Purpose:</em> 
   **/
  protected void verifySelected () throws SAFSException {
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      String param = (String) params.iterator().next();
      Log.info("..... param: "+param);
      // ready to do the verify
      GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
      if (verifySelectedText(guiObj, param)) {
          log.logMessage(testRecordData.getFac(),
              passedText.convert(TXT_SUCCESS_4, action,
              windowName, compName, action, param),
              PASSED_MESSAGE);
      }
    }
  }

  /** <br><em>Purpose:</em> hide the combo
   ** <br>Assumption: by right clicking it, no matter what the state, it will hide the box
   * @param                     guiObj, GuiSubitemTestObject
   **/
  private void hideCombo (GuiSubitemTestObject guiObj) {
	  guiObj.click();
//  	DomainTestObject domain = ((TestObject) guiObj).getDomain();
//  	  	
//  	if ( ((String)domain.getName()).equalsIgnoreCase(RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME)){
//    	// If Java, send a right click to close combobox if open (ESC will close entire dialogs)	
//	    try { 
//	   	  // this deselects the combo box
//	      guiObj.click(Script.RIGHT); 	    	
//	    } catch(Exception z) {
//    	  // silently ignore failure?  We may be alright...
//        }	
//  	} else {
//	    // HTML and other(s?) -- send ESCAPE character to close combobox if open
//  	  	try {
//          TopLevelTestObject parent = ((RDDGUIUtilities)utils).getParentTestObject(guiObj);
//	      parent.inputKeys("{ESC}");
//  	  	} catch(Exception y) {
//  		  // this right-click does not work on HTML Combos
//	      try { 
//	      	// this deselects the combo box (hopefully?)	
//	      	guiObj.click(Script.RIGHT);  
//	      } catch(Exception z) {
//    		// silently ignore failure?  We may be alright
//    	  }
//  	  	}
//  	}	
  }
  
  /** <br><em>Purpose:</em> show the combo by first hiding it, then clicking it.
   ** <br>Assumption: by first hiding, then clicking the combo, it will show it.
   * @param                     guiObj, GuiSubitemTestObject
   **/
  private void showCombo (GuiSubitemTestObject guiObj) {
//    hideCombo(guiObj);
    guiObj.click();
  }
  /* 
   * called by keywords CaptureItemsToFile and SelectPartialMatch
   */
  
//  /** <br><em>Purpose:</em> extract items from a ComboBox object. It supports Java,Html,.NET so far.
//   * 
//   * @param guiObj, which refers to the ComboBox.  
//   * @return java.util.List, all items of the ComboBox are in it. 
//   * @exception SAFSException
//   */
//  protected java.util.List getItems(GuiSubitemTestObject guiObj)throws SAFSException{
//	  String debugmsg = getClass().getName()+".getComboxContent(): ";	  
//      java.util.List list = null;
//      Log.info("...CFComBox is extracting list of items: "+debugmsg);
//
//      String classname = guiObj.getObjectClassName();
//      try {
//    	  if (classname.equalsIgnoreCase("HTML.SELECT"))
//    		  list = utils.extractListItems(guiObj, ".length", ".text");
//    	  else if (classname.equalsIgnoreCase(DotNetUtil.CLASS_COMBOBOX_NAME) || 
//    			   classname.equalsIgnoreCase(DotNetUtil.CLASS_TOOLSTRIPCOMBOBOXANDCONTROL_NAME))
//    		  list = utils.extractListItems(guiObj, ".ItemCount", ""); //for .NET object
//    	  else
//    		  list = utils.extractListItems(guiObj, "itemCount", "text");
//
//      }catch(Exception ex){
//  		  Log.info("RJ:CFComboBox could NOT extract list of items on Domain, "+guiObj.getDomain().toString());
//    	  throw new SAFSException(ex.toString());
//      }
//      return list;
//  }
  
  /**
   * Captures the object data into a List.  This is generally for the CaptureObjectDataToFile and 
   * VerifyObjectDataToFile, CaptureItemsToFile and SelectPartialMatch commands.
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

	String countprop = "itemCount"; //default count property
  	String itemprop = "text"; 	    //default value property
    String classname = table.getObjectClassName();
    Log.info("CFComboBox.captureObjectData attempting to extract list items...");
    if(isDotnetDomain(table)){
    	TestObject clazz = DotNetUtil.getClazz(table);
    	if(DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_COMBOBOX_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_TOOLSTRIPCOMBOBOX_NAME) ||
    	   DotNetUtil.isSubclassOf(clazz, DotNetUtil.CLASS_TOOLSTRIPCOMBOBOXANDCONTROL_NAME)){
        	countprop = ".ItemCount";
        	itemprop = "";
    	}else{
    		Log.debug("This is a custom combox which does not extend from standard .NET combox. Need to be treated specially.");
    	}
    }else if (classname.equalsIgnoreCase("HTML.SELECT")){
    	countprop = ".length";
    	itemprop = ".text";
    }
    java.util.List list = null;
	try{ list = utils.extractListItems(table, countprop, itemprop);}catch(Exception x){
		Log.debug("CFComboBox.captureObjectData catching "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	}	
   	if (list == null) try { list = super.captureObjectData(table);}catch(Exception x){
		Log.debug("CFComboBox.captureObjectData returning null due to "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	}	
   	if (list == null) throw new SAFSException(
   			FAILStrings.convert(FAILStrings.FAIL_EXTRACT_KEY, "Failed to extract ObjectData", "ObjectData")); 
    return list;
  }
  

  /** <br><em>Purpose:</em> captureItemsToFile
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JList, CaptureItemsToFile, AFileName.txt
   ** <br> Capture all items of JList to file AFileName.txt
   ** <br> 
   **/
  protected void captureItemsToFile () throws SAFSException {
    if (params.size() < 1) {
      paramsFailedMsg(windowName, compName);
    } else {
      try {
        log.logMessage(testRecordData.getFac(),"...params: "+params, DEBUG_MESSAGE);
        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        String filename =  null;
        String encoding = null;
        
        Iterator piter = params.iterator();
        if(piter.hasNext()) filename =  (String) piter.next();
        if(piter.hasNext()){
        	encoding =  (String) piter.next();
        	//If user put a blank string as encoding,
        	//we should consider that user does NOT provide a encoding, reset encoding to null.
        	encoding = "".equals(encoding.trim())? null: encoding;
        }
        
        Log.info("...filename: "+filename+" ; encoding:"+encoding);

        java.util.List list = captureObjectData(guiObj);

        Log.info("list: "+list);
        try {
          File file = new CaseInsensitiveFile(filename).toFile();
          if (!file.isAbsolute()) {
            String testdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
            if (testdir != null) {
              file = new CaseInsensitiveFile(testdir, filename).toFile();
              filename = file.getAbsolutePath();
            }
          }
          Log.info("Writing to file: "+filename);
	      //If a file encoding is given or we need to keep the encoding consistent
	      if (encoding != null || keepEncodingConsistent) {
	    	  StringUtils.writeEncodingfile(filename, list, encoding);
	      } else {
	    	  // Keep compatible with old version
	    	  StringUtils.writefile(filename, list);
	      }
        } catch (java.io.IOException e) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(),
                         getClass().getName()+": io exception: "+e.getMessage(),
                         FAILED_MESSAGE);
          return;
        }
        // set status to ok
        log.logMessage(testRecordData.getFac()," "+action+", write file ok: "+filename,
                       PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": item not found "+ 
                       "; msg: "+ex.getMessage(),
                       FAILED_MESSAGE);
      } catch (SAFSException se) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       getClass().getName()+": " + se.getMessage(),
                       FAILED_MESSAGE);
      }
    }
  }
  
  /** <br><em>Purpose:</em> compareValues and report failure if not matching.
   **/
  protected boolean compareValues(String value, String param) {
    boolean retVal;

    if (value == null) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
            windowName +":"+ compName +" value is empty or unknown following "+ action +" '"+ param +"'",
            FAILED_MESSAGE);
        retVal = false;
    } else {
        if (value.equals(param)) {
            testRecordData.setStatusCode(StatusCodes.OK);
            retVal = true;
        } else {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),
            windowName +":"+ compName +" does not match "+ action +" '"+ param +"'",
                FAILED_MESSAGE);
            retVal = false;
        }
    }  		  
    return retVal;
  }
  
  
  /** <br><em>Purpose:</em> verifyTextProperty
   ** <p> 
   ** <br> Can be called after setting the combo box value to ensure that the 
   ** <br> change has taken place.  Also called by the VERIFYSELECTED 
   ** <br> command / verifySelected() method.
   **
   **/
  private boolean verifyTextProperty(GuiSubitemTestObject guiObj, String param) {
    String value = null;

    try { value = guiObj.getProperty(".value").toString();} 
    catch (Exception x) {
        try { value = guiObj.getProperty("text").toString();} 
        catch (Exception y) { 
        	try {value = guiObj.getProperty("Text").toString();} // treated as .NET object 
        	catch (Exception z){ ;}
        }              
    }
    return compareValues(value, param);
  }
  
  
  /** <br><em>Purpose:</em> verifySelectedText
   ** <p> 
   ** <br> Can be called after setting the combo box value to ensure that the 
   ** <br> change has taken place.  Also called by the VERIFYSELECTED 
   ** <br> command / verifySelected() method.
   **
   **/
  private boolean verifySelectedText(GuiSubitemTestObject guiObj, String param) {
	String debugmsg = getClass().getName()+".verifySelectedText() ";
    String value = null;

    //For JAVA domain, selectIndexProperty's name is 'selectedIndex'
    String selectIndexProperty = "selectedIndex";
    if (isDotnetDomain(guiObj)){
    	//For DOTNET domain, selectIndexProperty's name is 'SelectedIndex'
		selectIndexProperty = "SelectedIndex";
  	}else if(isWinDomain(guiObj) ||
  			isHtmlDomain(guiObj)){
    	//For WIN and HTML domain, selectIndexProperty's name is '.selectedIndex'
  		selectIndexProperty = ".selectedIndex";
	}
    
	int j = ((Integer) guiObj.getProperty(selectIndexProperty)).intValue();
    if (j>=0) {
        // attempt to validate our selected item's text value
    	//Firstly, try general way to get the text in the ComboBox
    	Object n = guiObj.getSubitem(script.localAtIndex(j));
        if (n instanceof GuiTestObject) {
        	GuiTestObject selectedObject = (GuiTestObject)n;
        	//For DOTNET and JAVA the textProperty's name is 'text'
        	String textProperty = "text";
        	//For WIN and HTML the textProperty's name is '.text'
        	if(isWinDomain(guiObj) ||
        	   isHtmlDomain(guiObj)	){
        		textProperty = ".text";
        	}
        	try{
        		Object textObject = selectedObject.getProperty(textProperty);
        		if(textObject instanceof String){
        			value = (String) textObject;
        		}else if(textObject instanceof CaptionText){
        			value = ((CaptionText) textObject).getCaption();
        		}else{
        			Log.debug(debugmsg+" textObject is class of "+textObject.getClass().getName()+". Need other way to get it's text value.");
        		}
        	}catch(PropertyNotFoundException e){
        		Log.debug(debugmsg+" property "+textProperty+" can not be found for "+ selectedObject.getObjectClassName());
        	}finally{
        		selectedObject.unregister();
        	}
        } else if (n instanceof String) {
        	  value = (String) n;
        }else{
        	Log.debug(debugmsg+" subitem "+j+" is "+n.getClass().getName()+". Did not get it's string value");
        }
        
        //if the value is null, we will try other way to assign it
        if(value==null){
        	if(isDotnetDomain(guiObj)){
        		Log.debug(debugmsg+" Looking for .NET property SelectedItem ... ");
        		Object obj = guiObj.getProperty("SelectedItem");
        		if(obj instanceof String){
        			value= obj.toString();
        		}else{
        			Log.debug(debugmsg+" SelectedItem "+j+" is "+obj.getClass().getName()+". Did not get it's string value");
        		}
        	}if(isWinDomain(guiObj)){
        		Log.debug(debugmsg+" Looking for WIN property .selectedItem ... ");
        		Object obj = guiObj.getProperty(".selectedItem");
        		if(obj instanceof String){
        			value= obj.toString();
        		}else{
        			Log.debug(debugmsg+" SelectedItem "+j+" is "+obj.getClass().getName()+". Did not get it's string value");
        		}
        	}else{
        		Log.debug(debugmsg+" Need to add implementation for domain "+guiObj.getDomain().getName().toString());
        	}
        }
        
        return compareValues (value, param);
    } else {
    	return verifyTextProperty(guiObj, param);
    }
  }
  
  
  /** <br><em>Purpose:</em> setTextValue
   ** <p> example step commands:
   ** <p>
   ** <br> T, JavaWin, JComboBox, SetTextValue, "04/20/1977"
   ** <br> 
   ** <br> Sets the "text" value of the combobox - value does not have to be in the "itemlist".
   ** <br> This method only works for JAVA components, not "HTML" ones...
   **/
  protected void setTextValue (boolean performVerification) throws SAFSException {
    if (params.size() < 1) {
    	paramsFailedMsg(windowName, compName);
    } else {
      
    	log.logMessage(testRecordData.getFac(),"...params: "+params, DEBUG_MESSAGE);

        GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
        String domainname = guiObj.getDomain().getName().toString();
        Iterator piter = params.iterator();
        String newValue = (String) piter.next();
        Log.info("...value to set: " + newValue);
                
        /* only supported for java components -- HTML dropdown/combos do not ever allow
           setting a value that is not in the list.  With Internet Explorer, "inputKeys" 
           just "scrolls" to the entry that begins with the letter of the key pressed.
           In Mozilla/Firefox, "inputKeys" allows one to enter the whole string, and if
           the string is an item in the combobox, this will work fine.  Either way, for
           the sake of consistency, we only support Java components here.
        */
        if (!(
            domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME) ||
            domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME) ||
            domainname.equalsIgnoreCase(RGuiObjectVector.DEFAULT_WIN_DOMAIN_NAME)
            )){
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(), getClass().getName()
     			   	+ ": ComboBox SetTextValue does not make sense for HTML comboboxes -- "
					+ "Try SelectTextItem instead.",
					FAILED_MESSAGE);       	
        	return;
        }
        
        TopLevelTestObject parent = null;
                
        // use "input keys" to enter the value
        try {         	
        	parent = ((RDDGUIUtilities)utils).getParentTestObject(guiObj);
        	guiObj.click(new java.awt.Point(4,4));
        	this.delay(100);
        	if (! guiObj.hasFocus() ) { // does it EVER have focus?
        		// sometimes we need 2 clicks to get focus
        		guiObj.click(new java.awt.Point(4,4));
	        	this.delay(100);
        	}
        	hideCombo(guiObj); // hide it first because "setFocus" may open it and prevent key input
  	      	parent.inputKeys("{extEND}+{extHOME}{extDELETE}" + newValue + "{TAB}");  	    
	  	} 
	  	catch (MethodNotFoundException mnf){	  		
	  		String detail = mnf.getMessage();
	  		if(detail.indexOf("AppletHostProxy.inputKeys") > 0){
	  			TopLevelTestObject win = getAppletsBrowser(guiObj);
				if (win instanceof TopLevelTestObject) {
					win.inputKeys("{extEND}+{extHOME}{extDELETE}" + newValue +"{TAB}");
				}
	  			else{
		            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		            log.logMessage(testRecordData.getFac(), getClass().getName()
		     			   	+ ": SetTextValue cannot InputKeys to this Applet ComboBox.  Try its parent window.",
							FAILED_MESSAGE);       	
		        	return;
	  			}
	  		}else{
	            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	            log.logMessage(testRecordData.getFac(), getClass().getName()
	     			   	+ ": SetTextValue cannot InputKeys to this ComboBox.  Try its parent window.",
						FAILED_MESSAGE);       	
	        	return;
	  		}	  		
	  	}
	  	catch (Exception x) {
		 	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		 	log.logMessage(testRecordData.getFac(), getClass().getName()
		 			+ ": General Error in ComboBox SetTextValue.",
					FAILED_MESSAGE); 
		 	return;
	  	}	
      
	  	// verification
	  	if (performVerification && !StringUtils.containsSepcialKeys(newValue)){
	  	    if (verifyTextProperty(guiObj, newValue)) {        
	  	        log.logMessage(testRecordData.getFac(), windowName + ":" 
	  	            + compName + " " + action + " new verified value is '" 
                    + newValue + "'",
	  				PASSED_MESSAGE);
            }
        } else {
            testRecordData.setStatusCode(StatusCodes.OK);
            log.logMessage(testRecordData.getFac(), windowName + ":" 
                    + compName + " " + action + " performed.",
                    PASSED_MESSAGE);
	  	}
     }
  }
  
  protected void doSelect(String action) throws SAFSException {
      if (params.size() < 1) {
          paramsFailedMsg(windowName, compName);
      } else {
          String param = null;
          try {
            // ready to do the select
            GuiSubitemTestObject guiObj = null;
            SelectGuiSubitemTestObject selObj = null;
            boolean isSelect = obj1 instanceof SelectGuiSubitemTestObject;
            
            if(isSelect){
            	Log.info("CFCB.doSelect using a SelectGuiSubitemTestObject....");
            	selObj = (SelectGuiSubitemTestObject)obj1;
            	Rectangle rect = selObj.getScreenRectangle();
            	Log.info("CFCB.doSelect found combo at: "+ rect.toString());
            }else{
            	Log.info("CFCB.doSelect defaulting to a standard GuiSubitemTestObject....");
            	guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
            }
            //String classname = guiObj.getObjectClassName();
            //String domainname = guiObj.getDomain().getName().toString();
            
            param = (String) params.iterator().next();
            Log.info("..... param: "+param);

            if(isSelect) 
               	 showCombo(selObj); 
            else showCombo(guiObj);
			
            if(! isSelect) guiObj = (GuiSubitemTestObject)guiObj.find();
            else guiObj = selObj;
            
			  
			  //bypassed when debug TESTLOG is not running
			  //listAllProperties(guiObj,"* * Combo Box EXPOSED LIST guiObj * *");
			  
			  // SELECTINDEX
            if (action.equalsIgnoreCase(SELECTINDEX)) {
              int index = 0;
              try {
                Integer pi = new Integer(param);
                index = pi.intValue();
              } 
              catch (NumberFormatException nfe) {
                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                log.logMessage(testRecordData.getFac(),
                               getClass().getName()+": invalid index format: "+param,
                               FAILED_MESSAGE);
                return;
              }
              if (index < 1 ) {
	                testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	                log.logMessage(testRecordData.getFac(),
	                               getClass().getName()+": bad index (less than 1): "+param,
	                               FAILED_MESSAGE);
	                return;
              }
              // component item index is 0-based
              if (selectItemAtIndex(guiObj, index -1)){
			                	    //hideCombo(guiObj);
				    log.logMessage(testRecordData.getFac(),
				                   " "+action+" ok at index : "+index,
				                   PASSED_MESSAGE);
				    testRecordData.setStatusCode(StatusCodes.OK);
              }
              else{
		            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		            log.logMessage(testRecordData.getFac(),
		                         getClass().getName()+": item index may be out of range: "+index,
		                         FAILED_MESSAGE);
              }                
              return;
            } 

			  // SELECT, SELECTTEXTITEM            
            else if (action.equalsIgnoreCase(SELECT) ||
                       action.equalsIgnoreCase(SELECTTEXTITEM)) {
                       	
                if (selectTextItem(guiObj, param)){                  	  
                	  if (verifySelectedText(guiObj, param)) {                  	  
				          log.logMessage(testRecordData.getFac(),
				                     " "+action+" ok for : "+param,
				                     PASSED_MESSAGE);				          
                	  }
                }
                else{
//                	  hideCombo(guiObj);
		              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		              log.logMessage(testRecordData.getFac(),
		                             getClass().getName()+": text parameter may not be valid: "+param,
		                             FAILED_MESSAGE);
                } 
                return;
            } 
            
            // SELECTPARTIALMATCH
            else if (action.equalsIgnoreCase(SELECTPARTIALMATCH)) {
              //implement the partial match...
              java.util.List list = null;
              try {
                list = captureObjectData(guiObj);

                Log.info("list: "+list);
                // do the work of matching...
                ListIterator iter = list.listIterator();
                int j = StringUtils.findMatchIndex(iter, param);
                if (j>=0) {
                  String match = (String) iter.previous();
                  Log.info("match: "+j+", "+match);

                  if (selectTextItem(guiObj, match)){
					    log.logMessage(testRecordData.getFac(),
					                   " "+action+" ok for : "+match,
					                   PASSED_MESSAGE);
					    testRecordData.setStatusCode(StatusCodes.OK);
	                }
	                else{
	                	//try to locate by index then
              		if(selectItemAtIndex(guiObj, j)){
  					    log.logMessage(testRecordData.getFac(),
					                   " "+action+" ok for : "+match,
					                   PASSED_MESSAGE);
  					    testRecordData.setStatusCode(StatusCodes.OK);
  					    return;
              		}
			            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			            log.logMessage(testRecordData.getFac(),
			                           getClass().getName()+": text parameter may not be valid: "+match,
			                           FAILED_MESSAGE);
	                }
                  return;
                } 
                else {
                  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
                  log.logMessage(testRecordData.getFac(),
                                 getClass().getName()+": no match on: " + param,
                                 FAILED_MESSAGE);
                }
              } 
              //what gets us here?  Extracting list items?!
              // this may need to abort to ScriptNotExecuted so that 
              // other engines can give this a try.
              catch (SAFSException se) {
                testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
              }
            } 
            
            // SELECTUNVERIFIED, SELECTUNVERIFIEDTEXTITEM
            else if (action.equalsIgnoreCase(SELECTUNVERIFIED) ||
                       action.equalsIgnoreCase(SELECTUNVERIFIEDTEXTITEM)) {
                if (selectTextItem(guiObj, param)){
				      //hideCombo(guiObj);
				      log.logMessage(testRecordData.getFac(),
				                     " "+action+" ok for : "+param,
				                     PASSED_MESSAGE);
				      testRecordData.setStatusCode(StatusCodes.OK);
                }
                else{
		              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		              log.logMessage(testRecordData.getFac(),
		                             getClass().getName()+": text parameter may not be valid: "+param,
		                             FAILED_MESSAGE);
                } 
                return;
            } 

        // should never get here base on very first IF statement
        else {
          testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        }
      } catch (SubitemNotFoundException ex) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),getClass().getName()+": item not found: " + param +
                  "; msg: "+ex.getMessage(),
                  FAILED_MESSAGE);
      }
    }
  } // end of doSelect
}
