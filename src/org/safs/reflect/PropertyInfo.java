/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.reflect;

import org.safs.jvmagent.SAFSInvalidActionArgumentRuntimeException;

/**
 * This class is used internally by the Reflection class.
 * @author canagl
 * @since Jun 3, 2005
 * @see Reflection
 */
public class PropertyInfo {

	Object theObject;
	String theProperty;
	String trimmedProperty;
	String lctrimmedProperty;
	String uctrimmedProperty;
	boolean isindexed = false;
	boolean islong = false;
	int bindex;
	int eindex;
	String sindex;
	int iindex;
	long lindex;
	
	/**
	 * ProeprtyInfo Constructor
	 * @param object - object being evaluated for a property value.
	 * @param property - the name of the property being evaluated.
	 */
	PropertyInfo( Object object, String property ){
		setObject( object );
		setProperty( property );
	}		

	/**
	 * Returns the isindexed.
	 * @return boolean
	 */
	public boolean isIndexed() {
		return isindexed;
	}

	/**
	 * Returns the islong.
	 * @return boolean
	 */
	public boolean isLong() {
		return islong;
	}

	/**
	 * Returns the theObject.
	 * @return Object
	 */
	public Object getObject() {
		return theObject;
	}

	/**
	 * Returns the theProperty.
	 * @return String
	 */
	public String getProperty() {
		return theProperty;
	}

	/**
	 * Returns the trimmedProperty.
	 * @return String
	 */
	public String getTrimmedProperty() {
		return trimmedProperty;
	}

	/**
	 * Sets the isindexed.
	 * @param isindexed The isindexed to set
	 */
	public void setIndexed(boolean isindexed) {
		this.isindexed = isindexed;
	}

	/**
	 * Sets the islong.
	 * @param islong The islong to set
	 */
	public void setLong(boolean islong) {
		this.islong = islong;
	}

	/**
	 * Sets the theObject.
	 * @param object The object to reflect on
	 */
	public void setObject(Object object) {
		this.theObject = object;
	}

	/**
	 * Sets the theProperty.
	 * @param property The property name to set
	 */
	public void setProperty(String property) {
		this.theProperty = property;
	}

	/**
	 * Sets the trimmedProperty.
	 * @param trimmedProperty The trimmedProperty to set
	 */
	public void setTrimmedProperty(String trimmedProperty) {
		this.trimmedProperty = trimmedProperty;
		setLCTrimmedProperty( trimmedProperty.substring(0,1).toLowerCase() + trimmedProperty.substring(1));
		setUCTrimmedProperty( trimmedProperty.substring(0,1).toUpperCase() + trimmedProperty.substring(1));
	}

	/**
	 * Returns the bindex.
	 * @return int
	 */
	public int getBindex() {
		return bindex;
	}

	/**
	 * Returns the eindex.
	 * @return int
	 */
	public int getEindex() {
		return eindex;
	}

	/**
	 * Sets the bindex.
	 * @param bindex The bindex to set
	 */
	public void setBindex(int bindex) {
		this.bindex = bindex;
	}

	/**
	 * Sets the eindex.
	 * @param eindex The eindex to set
	 */
	public void setEindex(int eindex) {
		this.eindex = eindex;
	}
	
	
	/**
	 * Returns the iindex.
	 * @return int
	 */
	public int getIindex() {
		return iindex;
	}

	/**
	 * Returns the lindex.
	 * @return long
	 */
	public long getLindex() {
		return lindex;
	}

	/**
	 * Returns the sindex.
	 * @return String
	 */
	public String getSindex() {
		return sindex;
	}

	/**
	 * Sets the iindex.
	 * @param iindex The iindex to set
	 */
	public void setIindex(int iindex) {
		this.iindex = iindex;
	}

	/**
	 * Sets the lindex.
	 * @param lindex The lindex to set
	 */
	public void setLindex(long lindex) {
		this.lindex = lindex;
	}

	/**
	 * Sets the sindex, Iindex, Lindex, and Indexed(true) as appropriate.<br/>
	 * Index will become 0 if a NumberFormatException would otherwise result.<br/>
	 * SAFSInvalidActionArgumentRuntimeException is thrown if a valid string 
	 * index is provided whose value is less than 0.
	 * @param sindex The sindex to set
	 * @throws SAFSInvalidActionArgumentRuntimeException( getProperty() )
	 */
	public void setSindex(String sindex) {
		try{
			this.sindex = sindex.trim();
			setIindex(Integer.parseInt(sindex));
			if (getIindex() >= 0) {
				setLindex(getIindex());
				setIndexed(true);
			}else
				throw new SAFSInvalidActionArgumentRuntimeException(getProperty());				
		}					
		catch(NumberFormatException nfx){
			try{ 
				setLindex( Long.parseLong(sindex));				
				if (getLindex() >= 0) {
					setLong(true);
					setIndexed(true);
				}else
					throw new SAFSInvalidActionArgumentRuntimeException(getProperty());				
			}
			catch(NumberFormatException nfx2){
				this.sindex = "0";
				setIindex( 0 );
				setLindex( 0 );
				setIndexed(true);
			}
		}
	}


	/**
	 * Returns the lctrimmedProperty.
	 * @return String
	 */
	public String getLCTrimmedProperty() {
		return lctrimmedProperty;
	}

	/**
	 * Returns the uctrimmedProperty.
	 * @return String
	 */
	public String getUCTrimmedProperty() {
		return uctrimmedProperty;
	}

	/**
	 * Sets the lctrimmedProperty.
	 * @param lctrimmedProperty The lctrimmedProperty to set
	 */
	public void setLCTrimmedProperty(String lctrimmedProperty) {
		this.lctrimmedProperty = lctrimmedProperty;
	}

	/**
	 * Sets the uctrimmedProperty.
	 * @param uctrimmedProperty The uctrimmedProperty to set
	 */
	public void setUCTrimmedProperty(String uctrimmedProperty) {
		this.uctrimmedProperty = uctrimmedProperty;
	}

}
