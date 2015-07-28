/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.TestRecordData;

/**
 * 
 * @author Carl Nagle
 * @since Mar 31, 2005
 */
public interface AlternateAncestorUser {

	/**
	 * AlternateAncestorUser interface.
	 */
	public LocalAgent getAlternateAncestor();

	/**
	 * AlternateAncestorUser interface.
	 */
	public void setAlternateAncestor(LocalAgent ancestor);
		
	/**
	 * AlternateAncestorUser interface.
	 */
	public String getAlternateAncestorClassname();

	/**
	 * AlternateAncestorUser interface.
	 */
	public void setAlternateAncestorClassname(String ancestorClassname);
		
	/**
	 * AlternateAncestorUser interface.
	 */
	public TestRecordData processAncestor(Object object, TestRecordData testRecordData);
}
