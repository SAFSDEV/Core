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
package org.safs;

import org.safs.image.ImageUtils;
import org.safs.tools.drivers.ConfigureInterface;

/**
 * <br><em>Purpose:</em>TestRecordHelper: helper class which can populate data in
 * TestRecordData
 *
 * It can be populated from a STAF Variables pool.
 *
 * @author  Doug Bauman
 * @since   MAY 30, 2003
 *
 *  <br>    MAY 30, 2003    (DBauman) Original Release
 *  <br>    NOV 15, 2005    (Bob Lawler)  Added support for new TRD statusinfo field (RJL).
 *  <br>    APR 07, 2010    (Lei Wang) Added method getRecognitionString(), isMixedRsUsed()
 *  								  Added method getWindowGuiId() getCompGuiId(): override method in super class.
 *  <br>    JUN 30, 2011    (CANAGAL) Moved config to this superclass for all Java engines.
 **/
public class TestRecordHelper extends TestRecordData {


  static class MySTAFRequester extends STAFRequester {}

  protected STAFRequester requester = new MySTAFRequester();

  public STAFHelper getSTAFHelper() {return requester.getSTAFHelper();}
  protected STAFHelper getStaf() {return requester.getSTAFHelper();}
  public void setSTAFHelper(STAFHelper helper) { requester.setSTAFHelper(helper);}


  /** Hook's DDGUIUtilities when it is appropriate. **/
  private DDGUIUtilities ddgutils = null;
  public DDGUIUtilities getDDGUtils() { return ddgutils; }
  public void setDDGUtils(DDGUIUtilities ddgutils) { this.ddgutils = ddgutils; }

  /** Stores interface to chained configuration settings.
   * @see org.safs.JavaHook#initConfigPaths()
   */
  private static ConfigureInterface config = null;


  /** <br><em>Purpose:</em> no-arg constructor to make this fully qualified javabean
   **/
  public TestRecordHelper() {  super();  }

  /**
   * accessor method
   */
  public static void setConfig (ConfigureInterface c) {config = c;}

  /**
   * accessor method
   */
  public static ConfigureInterface getConfig () {return config;}


  /** <br><em>Purpose:</em> populate data from the VAR pool using STAF, basically
   ** grabs the following fields in a TestRecordData instance from the VAR of STAF:
   **
   **   <br>- inputRecord
   **   <br>- filename
   **   <br>- lineNumber
   **   <br>- separator
   **   <br>- testLevel
   **   <br>- appMapName
   **   <br>- fac
   **   <br>- statusCode
   **   <br>- statusInfo
   **   <br>
   * <br><em>Side Effects:</em>
   * <br><em>State Read:</em>
   * <br><em>Assumptions:</em>  none
   * @throws SAFSException STAFHelper.getVariable() will throw it
   **/
  public void populateDataFromVar () throws SAFSException {

    setInputRecord(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_INPUTRECORD));
    setFilename(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_FILENAME));

    String next = getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_LINENUMBER);
    int num = 0;
    try { num = (new Integer(next)).intValue();}
    catch (NumberFormatException nfe) {}
    setLineNumber(num);

    setSeparator(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_SEPARATOR));
    setTestLevel(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_TESTLEVEL));
    setAppMapName(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_APPMAPNAME));
    setFac(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_FAC));

    next = getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_STATUSCODE);
    num = 0;
    try { num = (new Integer(next)).intValue();}
    catch (NumberFormatException nfe) {}
    setStatusCode(num);

    setStatusInfo(getStaf().getVariable(getInstanceName() + STAFHelper.SAFS_VAR_STATUSINFO));
  }

  /** <br><em>Purpose:</em> sendback response, the statusCode is sent back
   ** in the VAR:  getInstanceName() + "statuscode"
   * <br><em>Assumptions:</em>  none
   * @throws                 SAFSException, if STAF has a problem
   **/
  public void sendbackResponse () throws SAFSException {
    int status = getStatusCode();
    getStaf().setVariable(getInstanceName() + STAFHelper.SAFS_VAR_STATUSCODE,
                      (Integer.toString(status)).trim());

    String info = getStatusInfo();
    //if statusinfo is null, reset it to ""
    if (info == null)
    	info = "";
    getStaf().setVariable(getInstanceName() + STAFHelper.SAFS_VAR_STATUSINFO, info);
  }

  /**
   * A method to return a default class package prefix used by some processors.
   * This allows subclassing engines to create a subclass of TestRecordHelper and
   * provide a single package designation used to dynamically instance tool-specific
   * processors.
   * <p>
   * For example, RTestRecordData used by Rational RobotJ returns "org.safs.rational."
   * This enables default processors to location the following processors:
   * <p>
   *    org.safs.rational.DCDriverCommand<br>
   *    org.safs.rational.custom.DCDriverCommand<br>
   *    org.safs.rational.CF[componentType] classes<br>
   *    org.safs.rational.CFComponent<br>
   *    org.safs.rational.custom.CF[componentType]
   * <p>
   * This requires that the subclassing implementations stick to this
   * strict class naming convention.
   * <p>
   * The org.safs.Processor class now allows users to specify any package and or
   * classname for use in dynamically locating classes at runtime.  This means that
   * subclasses like DriverCommandProcessor and TestStepProcessor can be given
   * alternative package or class names to search for.
   * <p>
   * This method is no longer required, though it will be queried first to maintain
   * compatibility with processors using this feature.
   **/
  public String getCompInstancePath(){return null;}

	/**
	 * <br><em>Purpose:</em> Retrieve window recognition info from current app map. <br>
	 * 						 Issues ActionFailure and FAILED status if not retrievable.
	 * <br><em>Note:</em> 	 This method will try to get windowGuiID and componentGuiID<br>
	 * 						 from its super class, if not found, it will use the windowName<br>
	 * 						 and the componentName to get the GuiID from the map file, and<br>
	 * 						 store the result to its super class.
	 *                       Before calling this method, make sure the property windowName, compName
	 *                       , mapName and stafHelper are correctly set.
	 *
	 * @param  boolean isWin if true, return window RS; otherwise, component RS.
	 * @return recognition string
	 * @throws SAFSException if there is a problem with STAF or the AppMap itself.
	 */
	private String getRecognitionString(boolean isWin) throws SAFSException
	{
      String debugmsg = getClass().getName()+".getRecognition(): ";
	  String rec = null;
      String mapname = getAppMapName();
      String winname = getWindowName();
      String compname = getCompName();

      if(winname==null || winname.equals("") || compname==null || compname.equals("")){
    	  Log.debug(debugmsg+" windowName="+winname+" ; compName="+compname+
    			             " . They should be set correctly in test record.");
    	  return null;
      }
      //If winname equals compname, we should alternate isWin to true.
      isWin = isWin ? isWin: (winname==compname);
      String item = isWin ? winname : winname+":"+compname;

      STAFHelper staf = getSTAFHelper();

      if(isWin) rec = super.getWindowGuiId();
      else rec = super.getCompGuiId();

  	  if(rec == null){
  	      if(mapname==null || staf==null){
  	    	  Log.debug(debugmsg+" map name: "+mapname+" and STAF HANDLER: "+staf+
  	    			  			 " should NOT be null. Set them firstly.");
  	    	  throw new SAFSException("Test Record not properly initialized.");
  	      }
  	      Log.debug(debugmsg+" get Recognition String for "+ item+" from map: "+ mapname);
  		  if(isWin){
  			  rec =staf.getAppMapItem( mapname, winname, winname);
  			  super.setWindowGuiId(rec);
  		  }else{
  			  rec =staf.getAppMapItem( mapname, winname, compname);
  			  super.setCompGuiId(rec);
  		  }
  	  }else{
  		Log.debug(debugmsg+" Got from super class: Recognition String for "+ item+" is "+rec);
  	  }

      if((rec==null)||(rec.length()==0)){
    	  Log.debug(debugmsg+" Can NOT get Recognition String for "+ item+" from map: "+ mapname);
    	  throw new SAFSException("AppMap Error: Item '"+ item +"' was not found in '"+mapname+"'");
      }
      Log.debug(debugmsg+" Recognition String for "+ item+" is "+rec);

      return rec;
	}

	/**
	 * <br><em>Note:</em>    Before calling this method, make sure the property windowName, compName
	 *                       , mapName and stafHelper are correctly set.
	 * @return	Window's Recognition String
	 * @throws SAFSException
	 */
	@Override
	public String getWindowGuiId () throws SAFSException{
		return getRecognitionString(true);
	}

	/**
	 * <br><em>Note:</em>    Before calling this method, make sure the property windowName, compName
	 *                       , mapName and stafHelper are correctly set.
	 * @return	Component's Recognition String
	 * @throws SAFSException
	 */
	@Override
	public String getCompGuiId () throws SAFSException {
		return getRecognitionString(false);
	}

	/**
	 * <br><em>Purpose:</em>	To test if the RS is specify in mixed mode. <br>
	 * 							That is, parent in OBT format; Component in IBT format.
	 * @return	boolean			true if the RS is in mixed mode.
	 */
	public boolean isMixedRsUsed() throws SAFSException{
		boolean mixedRsUsed = false;

		//Get window and component Recognition String
		String winRs = getRecognitionString(true);
		String compRs = getRecognitionString(false);

		//If the window Recognition String is not in IBT format (ex. in OBT format)
		//and the component RS is in IBT format
		if(!ImageUtils.isImageBasedRecognition(winRs) && ImageUtils.isImageBasedRecognition(compRs))
			mixedRsUsed = true;

		return mixedRsUsed;
	}

	/**
	 * @return boolean, true if this record contains a window to handle.
	 * @throws SAFSException if the window's name is null.
	 */
	public boolean targetIsWindow() throws SAFSException{
		return !targetIsComponent();
	}

	/**
	 * @return boolean, true if this record contains a component to handle.
	 * @throws SAFSException if the window's name is null.
	 */
	public boolean targetIsComponent() throws SAFSException{
		String winname = getWindowName();
		String compname = getCompName();

		if(winname==null){
			throw new SAFSException("The window's name is null!");
		}

		return !winname.equals(compname);
	}

}

