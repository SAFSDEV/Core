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
