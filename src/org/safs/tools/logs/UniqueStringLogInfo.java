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
package org.safs.tools.logs;

import org.safs.tools.UniqueIDInterface;
import org.safs.tools.logs.UniqueStringLogLevelInfo;

public class UniqueStringLogInfo 
             extends UniqueStringLogLevelInfo 
             implements UniqueLogInterface {

	protected String textname  = null;
	protected String xmlname   = null;
	protected long   logmodes  = 0;
	protected UniqueIDInterface linkedfac = null;
	
	/**
	 * Constructor for UniqueStringLogInfo
	 */
	public UniqueStringLogInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringLogInfo UniqueIDInterface
	 */
	public UniqueStringLogInfo(String id){
		super(id);
	}
	
	/**
	 * Constructor for UniqueStringLogInfo UniqueLogLevelInterface
	 */
	public UniqueStringLogInfo(String id, String loglevel){
		super(id, loglevel);
	}
	
	/**
	 * PREFERRED Constructor for UniqueStringLogInfo
	 */
	public UniqueStringLogInfo(String id, 
	                           String textname, 
	                           String xmlname, 
	                           String loglevel,
	                           long logmodes,
	                           UniqueIDInterface linkedfac){
		this(id, loglevel);
		setTextLogName(textname);
		setXMLLogName(xmlname);
		setLogModes(logmodes);
		setLinkedFac(linkedfac);
	}

	/**
	 * Set text logname to provided value.
	 */
	public void setTextLogName(String textname) {
		this.textname = textname;
	}

	/**
	 * @see UniqueLogInterface#getTextLogName()
	 */
	public String getTextLogName() {
		return textname;
	}

	/**
	 * Set XML logname to provided value.
	 */
	public void setXMLLogName(String xmlname) {
		this.xmlname = xmlname;
	}

	/**
	 * @see UniqueLogInterface#getXMLLogName()
	 */
	public String getXMLLogName() {
		return xmlname;
	}

	/**
	 * Set logmodes to the value provided.
	 */
	public void setLogModes(long logmodes) {
		this.logmodes = logmodes;
	}

	/**
	 * @see UniqueLogInterface#getLogModes()
	 */
	public long getLogModes() {
		return logmodes;
	}

	/**
	 * Set the linked fac to the one provided.
	 */
	public void setLinkedFac(UniqueIDInterface linkedfac ) {
		this.linkedfac = linkedfac;
	}

	/**
	 * @see UniqueLogInterface#getLinkedFac()
	 */
	public UniqueIDInterface getLinkedFac() {
		return linkedfac;
	}

}
