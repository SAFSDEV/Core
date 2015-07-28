/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/**
 * This class is used to fill the matches Vector with match data.
 * At each level in the GuiObjectVector, we will store a match if one is 
 * found.  This happens all the way down to the final GuiTestObject matching 
 * the final GuiObjectVector level.  Only if there is a match all the way 
 * down to the final GuiObjectVector level can we say we have found an 
 * object matching the recognition string.
 */
public class MatchData {
	
	GuiObjectVector pgov = null;
	int pgovLevel = -1;
	int pobjLevel = -1;
	Object pchild = null;


	/**
	 * Creates a new MatchData object for provided GuiObjectVector depth level.
	 * 
	 * @param agov The GuiObjectVector to satisfy.
	 * 
	 * @param agovLevel The GuiObjectVector depth level satisfied by the match.
	 * 
	 * @param aobjLevel The actual GuiTestObject hierarchy level satisfied by the match.
	 * 
	 * @param achild The actual GuiTestObject proxy satisfying the match.
	 */
	public MatchData(GuiObjectVector agov, int agovLevel, int aobjLevel, Object achild){
		pgov      = agov;
		pgovLevel = agovLevel;
		pobjLevel = aobjLevel;
		pchild    = achild;			
	}		

	
	/**
	 * @return The GuiObjectVector satisfied by this match at some level.
	 */
	public GuiObjectVector getGuiObjectVector()  { return pgov; }


	/**
	 * @return The GuiObjectVector depth level satisfied by this match.
	 */
	public int getVectorRecognitionLevel()  { return pgovLevel; }


	/**
	 * @return The actual object depth satisfying this match.
	 */
	public int getObjectRecognitionLevel()  { return pobjLevel; }


	/**
	 * @return The GuiTestObject proxy satisfying the GuiObjectVector depth level match.
	 */
	public Object getGuiTestObject() { return pchild;    }
}

