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
package org.safs.rational;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;

import java.util.*;
import org.safs.*;


/**
 * No end user would ever normally instantiate an object of this class.  This is done 
 * by other classes in the framework satisfying TestObject lookup requests.
 * <p>
 * Consequently, the API for this class is subject to change without notice.
 * <p>
 * Iterates a Rational object hierarchy looking for a match to the provided GuiObjectVector.
 * An object vector is essentially the object recognition string with additional 
 * information deduced from it.  Such as how many parent/child relationships are 
 * apparent in the provided recognition string.
 * <p>
 * The external initiating routine is expected to provide the parent TestObject.
 * From there we try to go down each possible child path looking for the final match. 
 * <p>
 * @author Carl Nagle
 * @since JUN 26, 2003
 *   <br>   JUN 04, 2003    (Carl Nagle) Original Release
 *   <br>   SEP 26, 2003    (DBauman) removed the pooling because need to pass 'gather' parameter to each new instance (constructor)
 * 
 * Copyright (C) (SAS) All rights reserved.
 * GNU General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
public class RGuiChildIterator extends GuiChildIterator{
	
	/**
	 * Constructor used internally by the initial RGuiChildIterator for each subsequent 
	 * child level in the hierarchy search.
         * @param gather, List containing names matched, if null, then match first name
	 **/
	protected RGuiChildIterator(java.util.List gather) {
		super(gather);
    }
	
	
	/**
	 * Called only once by some external routine kicking off a TestObject search.
	 * The govLevel and objLevel in the ObjectVector will be assumed to be 0.
	 * The routine will install an initial match for govLevel 0 to be the provided parent.
	 * <p>
	 * @param aparent the topmost parent to search.
	 * <p>
	 * @param agovVector the govVector (recognition string) to satisfy with the search.
	 * <p>
         * @param gather, List containing names matched, if null, then match first name
	 */
	public RGuiChildIterator (Object aparent, GuiObjectVector agovVector,
                                  java.util.List gather) {
        super(aparent, agovVector, gather);
	}
	

}
