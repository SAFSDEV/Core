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