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


/**
 * <br><em>Purpose:</em> a wrapper class for the STAFHandle, includes some common
 * functionality used many places associated with the SAFSVARS and SAFSMAPS;
 * This is the Singleton version.
 *
 * @author  Doug Bauman
 * @since   JUN 27, 2003
 * <br>     JUN 27, 2003    (DBauman) Original Release
 *
 **/
public class SingletonSTAFHelper extends STAFHelper {

  /** Singleton reference **/
  private static STAFHelper helper = new SingletonSTAFHelper();

  /** <br><em>Purpose:</em> static reference to only Singleton instance of this class
   * <br><em>Assumptions:</em>  it's up to the user to 'initialize()' the helper.
   * @return                    STAFHelper instance
   **/
  public static STAFHelper getHelper () {return helper;}

  /**
   * Set the internal STAFHelper with an initialized STAFHelper only if the internal one 
   * is not already initialized.
   * 
   * @param _helper An initialized STAFHelper
   */
  public static void setInitializedHelper(STAFHelper _helper){
	  if (_helper==null) return;
	  if((! helper.isInitialized())&&(_helper.isInitialized())) helper = _helper;  
  }
  
  /** <br><em>Purpose:</em> static reference to only Singleton instance of this class.
   *  If the user provides a non-null processName, the object will automatically be
   *  initialized *if* it is not already initialized.
   * <p>
   * @param processName The name of the process to register with STAF.  If the object
   *  has not been initialized, and a null processName is provided, then we will
   *  initialize the object with the SAFS_GENERIC_PROCESS processName.
   * <p>
   * @return STAFHelper instance, initialized as necessary.
   **/
  public static STAFHelper getInitializedHelper (String processName) throws SAFSSTAFRegistrationException{
  	if (helper.isInitialized()) return helper;
  	if (processName != null) {
  		helper.initialize(processName);
  	}else{
  		helper.initialize(SAFS_GENERIC_PROCESS);
  	}
  	return helper;
  }

  private SingletonSTAFHelper() {
  }

}
