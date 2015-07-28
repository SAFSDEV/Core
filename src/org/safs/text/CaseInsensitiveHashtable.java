/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.text;

import java.util.*;

/**
 * Overrides java.util.Hashtable methods to allow for case-insensitive String key matches.
 * This also means that 2 string keys differing only by case will not exist.
 * Normal Hashtable functionality prevails unless a call to affected methods does not find a match.
 * In that event, if the key is a String we will attempt to locate an appropriate String 
 * key by ignoring the case of the key.
 * 
 * @author Carl Nagle
 * @since Feb 23, 2005
 */
public class CaseInsensitiveHashtable extends Hashtable {

	/**
	 * Constructor for CaseInsensitiveHashtable.
	 */
	public CaseInsensitiveHashtable() {
		super();
	}

	/**
	 * Constructor for CaseInsensitiveHashtable.
	 * @param arg0
	 */
	public CaseInsensitiveHashtable(int arg0) {
		super(arg0);
	}

	/**
	 * Constructor for CaseInsensitiveHashtable.
	 * @param arg0
	 * @param arg1
	 */
	public CaseInsensitiveHashtable(int arg0, float arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor for CaseInsensitiveHashtable.
	 * @param arg0
	 */
	public CaseInsensitiveHashtable(Map arg0) {
		super(arg0);
	}

	/**
	 * Returns the proper String key or null.
	 */
	public String findCaseInsensitiveStringKey(String key){
		if(key==null) return null;
		String match = null;
		Enumeration keys = keys();
		Object item = null;
		while(keys.hasMoreElements()){
			item = keys.nextElement();
			if (item instanceof String){
				if(((String)item).equalsIgnoreCase(key)){
					match = (String)item;
					break;
				}
			}
		}
		return match;
	}
	
	/**
	 * Override for String keys to allow case-insensitivity.
	 * @see Hashtable#containsKey(Object)
	 */
	public boolean containsKey(Object key){
		boolean match = super.containsKey(key);
		String nckey = null;
		if((! match)&&(key instanceof String))
			return (findCaseInsensitiveStringKey((String)key) instanceof String);
		return match;
	}	

	/**
	 * Override for String keys to allow case-insensitivity.
	 * @see Hashtable#get(Object)
	 */
	public Object get(Object key){
		Object item = super.get(key);
		if (item instanceof Object) return item;		// was found.
		if (! (key instanceof String)) return item;	// not String key.		
		String nckey = findCaseInsensitiveStringKey((String)key);
		if (nckey instanceof String) return super.get(nckey);
		return item;
	}		

	/**
	 * Override for String keys to allow case-insensitivity.
	 * @see Hashtable#put(Object, Object)
	 */
	public Object put(Object key, Object value){
		Object nckey = null;
		if (key instanceof String) nckey = findCaseInsensitiveStringKey((String) key);
		if (nckey == null) nckey = key;
		return super.put(nckey, value);
	}

	/**
	 * Override for String keys to allow case-insensitivity.
	 * @see Hashtable#remove(Object)
	 */
	public Object remove(Object key){
		Object nckey = null;
		if (key instanceof String) nckey = findCaseInsensitiveStringKey((String) key);
		if (nckey == null) nckey = key;
		return super.remove(nckey);
	}	

}
