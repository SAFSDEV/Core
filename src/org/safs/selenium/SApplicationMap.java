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
package org.safs.selenium;

import java.util.Hashtable;

import org.safs.ApplicationMap;
import org.safs.text.CaseInsensitiveHashtable;

import com.thoughtworks.selenium.Selenium;

public class SApplicationMap extends ApplicationMap {
	/** collection of selenium objects **/
	private static CaseInsensitiveHashtable seleniumList = new CaseInsensitiveHashtable();
	private static Selenium lastUsedSel;
	public SApplicationMap(String mapname) {
		super(mapname);
	}
	
	public static void addSelenium(String id, Selenium s){
		if(id == null || id.equals("")){
			id = String.valueOf(s.hashCode());
		}
		seleniumList.put(id,s);
		lastUsedSel = s;
	}
	
	public static Selenium getSelenium(String id){
		Selenium temp = null;
		if(id != null)
			temp = (Selenium)seleniumList.get(id);
		if(temp == null)
			return lastUsedSel;
		return temp;
	}
	
	/**
	 * Remove a Selenium session from storage.
	 * Usually done after the session is Closed or Stopped.
	 * @param id name of Selenium session\window.
	 * @return the Selenium object removed or null if not stored.
	 */
	public static Selenium removeSelenium(String id){
		Selenium obj = null;
		try{ obj = (Selenium) seleniumList.remove(id);}
		catch(Exception x){}
		return obj;
	}
	
	public static Hashtable getSeleniumList(){
		return seleniumList;
	}	
}
