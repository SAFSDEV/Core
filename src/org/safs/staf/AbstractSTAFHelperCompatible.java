/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.staf;

import org.safs.Log;
import org.safs.STAFHelper;

/**
 * <pre>
 * Contains the common implementation of STAFHelperCompatibleInterface.
 * </pre>
 * 
 * @author SBJLWA
 * 
 */
public abstract class AbstractSTAFHelperCompatible implements STAFHelperCompatibleInterface{
	protected STAFHelper staf = null;

	public void setSTAFHelper(STAFHelper staf) {
		this.staf = staf;
	}
	
	/**
	 * According to the STAF's version, get the instance of STAFHelperCompatibleInterface.<br>
	 * 
	 * @param stafVersion, int the STAF's major version
	 * @return	STAFHelperCompatibleInterface, an instance of STAFHelperCompatibleInterface or null.
	 */
	public static STAFHelperCompatibleInterface getCompatibleSTAF(int stafVersion){
		STAFHelperCompatibleInterface compatibleStaf = null;
		try {
			if(stafVersion==2){
				compatibleStaf = (STAFHelperCompatibleInterface) Class.forName(STAFHelperCompatibleInterface.STAF2_CLASS_NAME).newInstance();
			}else if(stafVersion==3){
				compatibleStaf = (STAFHelperCompatibleInterface) Class.forName(STAFHelperCompatibleInterface.STAF3_CLASS_NAME).newInstance();
			}else{
				Log.debug("STAF Major Version is "+stafVersion+" Need new implementation for STAFHelperCompatibleInterface.");
			}
		} catch (InstantiationException e) {
			Log.debug("Can't get instance of STAFHelperCompatibleInterface, Exception="+e.getMessage());
		} catch (IllegalAccessException e) {
			Log.debug("Can't get instance of STAFHelperCompatibleInterface, Exception="+e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.debug("Can't get instance of STAFHelperCompatibleInterface, Exception="+e.getMessage());
		}
		
		if(compatibleStaf!=null){
			Log.debug("AbstractSTAFHelperCompatible.getCompatibleSTAF(): get instance for class '"+compatibleStaf.getClass().getName()+"'");
		}
		return compatibleStaf;
	}
}
