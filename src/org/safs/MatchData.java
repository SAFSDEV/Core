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

