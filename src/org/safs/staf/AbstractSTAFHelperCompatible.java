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
package org.safs.staf;

import org.safs.Log;
import org.safs.STAFHelper;

/**
 * <pre>
 * Contains the common implementation of STAFHelperCompatibleInterface.
 * </pre>
 * 
 * @author Lei Wang
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
