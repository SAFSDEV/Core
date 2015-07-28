package org.safs.tools.input;

import org.safs.tools.UniqueIDInterface;
public interface MapsInterface {

	/** Open a Map and give it a unique ID. **/
	public void openMap (UniqueMapInterface map);
	
	/** 
	 * Set the identified Map as the default Map to be used. 
	 * This is set for some calls or external tools that may not always specify 
	 * which Map to use.  They expect the "current" or "default" Map to be referenced.**/
	public void setDefaultMap (UniqueIDInterface map);
		
	/** 
	 * Get the ID of the current default map. 
	 */
	public UniqueIDInterface getDefaultMap ();
		
	/** 
	 * Set the identified Map Section as the default Section to be used when normal 
	 * lookups fail. "ApplicationConstants" is the pre-programmed default.  This allows 
	 * us to change that.**/
	public void setDefaultMapSection (UniqueSectionInterface section);
	
	/** 
	 * Get the ID of the current default map section. 
	 */
	public UniqueSectionInterface getDefaultMapSection ();

		
	/** Get the value of a Mapped item. **/
	public String getMapItem (UniqueItemInterface item);
	
	/** 
	 * Clear the cache of one specific Map.
	 * @see #reset to clear the cache for ALL Maps. **/
	public void clearCache (UniqueIDInterface map);
	
	/** Close the identified Map and release all resources associated with it.**/
	public void closeMap (UniqueIDInterface map);
}