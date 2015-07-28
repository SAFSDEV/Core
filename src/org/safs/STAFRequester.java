/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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

