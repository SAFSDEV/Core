package org.safs.logging;

import java.util.*;

/**
 * This is a helper class for log facilities to store their 
 * <code>LogItem</code>s. This class maps log type indentifier 
 * (<code>LOGMODE</code> constant defined by <code>AbstractLogFacility</code>) 
 * to a <code>LogItem</code> of that type. This implies that only one 
 * <code>LogItem</code> of a particular type can be stored in a 
 * <code>LogItemDictionary</code>, which is always the case for log facilities.
 * 
 * @see LogItem
 * @see AbstractLogFacility
 */
public class LogItemDictionary 
{
	private Hashtable items;
	
	/**
	 * Creates an empty <code>LogItemDictionary</code>.
	 */
	public LogItemDictionary()
	{
		items = new Hashtable();
	}

	/**
	 * Tests if this dictionary contains <code>LogItem</code> of a type.
	 * <p>
	 * @param mode	the type of the <code>LogItem</code>. One of the
	 * 				<code>LOGMODE</code> constants defined by
	 *              <code>AbstractLogFacility</code>.
	 * @return		<code>true</code> if the <code>LogItem</code> exists.
	 * 				<code>false</code> if not.
	 */
	public boolean contains(long mode)
	{
		return items.containsKey(new Long(mode));
	}

	/**
	 * Returns the <code>LogItem</code> of a particular type.
	 * <p>
	 * @param mode	the type of the <code>LogItem</code>. One of the
	 * 				<code>LOGMODE</code> constants defined by
	 *              <code>AbstractLogFacility</code>.
	 * @return		the <code>LogItem</code> of the specified type; 
	 * 				<code>null</code> if this type is not mapped to any item.
	 */
	public LogItem get(long mode)
	{
		return (LogItem) items.get(new Long(mode));
	}

	/**
	 * Adds a <code>LogItem</code>. Its type is used as the key.
	 * <p>
	 * @param item	the <code>LogItem</code> to add.
	 * @return		the previous <code>LogItem</code> of the same type, or null
	 * 				if there was none.
	 */
	public LogItem put(LogItem item)
	{
		return (LogItem) items.put(new Long(item.mode), item);
	}

	/**
	 * Returns an enumeration of all log items.
	 * <p>
	 * @return	an enumeration of all log items.
	 */
	public Enumeration items()
	{
		return items.elements();
	}
}