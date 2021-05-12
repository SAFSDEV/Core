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
 * A superclass for objects that will need a reference to a STAFHandle for STAF requests.
 * @see STAFHelper
 * **/
public abstract class STAFRequester {

	/** STAF request processor used by all subclasses. **/
	protected  STAFHelper staf = null;

	/** @return the STAFHelper reference **/	
	public STAFHelper getSTAFHelper(){ return staf; }
	
	/**
	 * Sets the STAFHelper instance to be used by the subclass. 
	 * @param helper STAFHelper instance set by the owning object. 
	 *                This is usually done by a subclass implementation.
	 **/
	public void setSTAFHelper(STAFHelper helper) { staf = helper;}
	
}

