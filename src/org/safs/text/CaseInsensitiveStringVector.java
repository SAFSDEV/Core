package org.safs.text;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * Overrides important java.util.Vector methods to allow for case-insensitive String item matches.
 * <b>This means that 2 string items differing only by case will not exist.</b>
 * Normal Vector functionality prevails unless a call to affected methods does not find a 
 * case-insensitive match. In that event, if the item is a String we will attempt to locate an 
 * appropriate String item by ignoring the case of the key.
 * 
 * @author canagl
 * @since Nov 14, 2013
 */
public class CaseInsensitiveStringVector extends Vector {

	public CaseInsensitiveStringVector() {
		super();
	}

	public CaseInsensitiveStringVector(int initialCapacity) {
		super(initialCapacity);
	}

	public CaseInsensitiveStringVector(Collection c) {
		super(c);
	}

	public CaseInsensitiveStringVector(int initialCapacity, int capacityIncrement) {
		super(initialCapacity, capacityIncrement);
	}

	/**
	 * Returns the proper String item or null.
	 */
	public String findCaseInsensitiveStringItem(String key){
		if(key==null) return null;
		String match = null;
		Enumeration keys = elements();
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
	 * Returns the 0-based index of the case-insensitive String item or -1 if the item is not contained.
	 */
	public int findCaseInsensitiveStringIndex(String key){
		if(key==null) return -1;
		Enumeration keys = elements();
		Object item = null;
		int match = -1;
		while(keys.hasMoreElements()){
			item = keys.nextElement();
			if (item instanceof String){
				if(((String)item).equalsIgnoreCase(key)){
					match = indexOf(item);
					break;
				}
			}
		}
		return match;
	}
		
	/* (non-Javadoc)
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	@Override
	public synchronized boolean add(Object e) {
		if(e instanceof String) {
			if(findCaseInsensitiveStringItem((String)e) != null)
				return false;
		}
		return super.add(e);
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		if(o instanceof String) return findCaseInsensitiveStringItem((String)o)!= null;
		return super.contains(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		if(o instanceof String) return findCaseInsensitiveStringIndex((String)o);
		return super.indexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		if(o instanceof String) return super.remove(findCaseInsensitiveStringItem((String)o));
		return super.remove(o);
	}
}
