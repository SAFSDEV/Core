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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 24, 2017    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;

/**
 * This class is used to convert a Persistable object to a string and convert a string to a Persistable object.<br>
 * The string format will depends on the sub-class's implementation.<br>
 *
 * @author Lei Wang
 */
public abstract class PersistorToString extends AbstractPersistor{

	/**
	 * The string format of a Persistable object. The concrete format will depend on the sub-class's implementation.<br>
	 */
	protected StringBuilder stringFormat = null;

	/**
	 * The Reader containing string format of a Persistable object.<br>
	 * @see #stringFormat
	 */
	protected Reader stringFormatReader = null;

	/**
	 * Holding the fields which are ignored for each class. No need to unpickle.<br/>
	 * The field is expressed as a pair of (classname, a list of fields).<br/>
	 * @see #isIgnoredFiled(String)
	 */
	protected Map<String/*className*/, List<String>/*field-names*/> ignoredFieldsForUnpickle = null;

	public PersistorToString(){
		stringFormat = new StringBuilder();
	}

	public PersistorToString(String stringFormat){
		this.stringFormat = new StringBuilder(stringFormat);
	}

	public PersistorToString(Reader stringFormatReader){
		this.stringFormatReader = stringFormatReader;
	}

	/**
	 * @return the stringFormat of a Persistable object.
	 */
	public String getStringFormat() {
		String debugmsg = PersistorToString.class.getSimpleName()+".getStringFormat(): ";

		if(stringFormat==null){
			stringFormat = new StringBuilder();
		}
		if(stringFormat.length()==0){
			if(stringFormatReader!=null){
				try {
					char[] buffer = new char[1024];
					int read = stringFormatReader.read(buffer);;
					while(read>0){
						stringFormat.append(buffer,0,read);
						read = stringFormatReader.read(buffer);
					}
					//Reset the pointer to the beginning of this stream so that the other can still use this stream.
					stringFormatReader.reset();
				} catch (IOException e) {
					IndependantLog.warn(debugmsg+"Met "+e.toString());
				}
			}
		}

		return stringFormat.toString();
	}

	/**
	 * @param stringFormat the stringFormat of a Persistable object.
	 */
	public void setStringFormat(String stringFormat) {
		this.stringFormat = new StringBuilder(stringFormat);
		this.stringFormatReader = new StringReader(stringFormat.toString());
	}

	/**
	 * @return Reader, the stringFormatReader. It can be null.
	 */
	public Reader getStringFormatReader() {
		return stringFormatReader;
	}

	/**
	 * @param stringFormatReader the stringFormatReader to set
	 */
	public void setStringFormatReader(Reader stringFormatReader) {
		this.stringFormatReader = stringFormatReader;
	}

	@Override
	public void persist(Persistable persistable) throws SAFSException {
		//Before we write the Persistable object to local StringBuilder, we should clear the StringBuilder
		unpersist();

		//Then we can safely write the Persistable object.
		writeHeader(persistable);
		write(persistable);
		writeTailer(persistable);
	}

	/** This is called before {@link #write(Persistable)}. */
	protected void writeHeader(Persistable persistable)  throws SAFSException{}
	/**
	 * Write the Persistable object into a persistent material.<br/>
	 * {@link #writeHeader(Persistable)} is called before<br/>
	 * {@link #writeTailer(Persistable)} is called after<br/>
	 */
	protected final void write(Persistable persistable)  throws SAFSException{
		stringFormat.append((parse(persistable, true)));
	}
	/** This is called after {@link #write(Persistable)}. */
	protected void writeTailer(Persistable persistable)  throws SAFSException{}

	/**
	 * If the string value needs to be quoted before writing into a kind of persistence.<br/>
	 * JSON file requires that for <b>string value</b>.<br/>
	 * In JSON file, For example: <br/>
	 * "key" : "stringValue"		is good<br/>
	 * "key" : stringValue			is bad<br/>
	 *
	 * @return boolean true if the value needs to be quoted before writing into persistence.
	 */
	protected boolean stringNeedQuoted(){
		return false;
	}

	/**
	 * When writing a string to a file, some special characters MUST be escaped, such as
	 * new line <b>"\n", "\r", "\r\n"</b>, or double quote <b>"</b>, or <b>&lt;?XML>...&lt;/XML></b> etc.<br/>
	 * What characters to escape and how to escape, these depend on the format of the persistence file, and the
	 * parser of the file. Subclass should override this method.<br/>
	 * This method will also quote the value if {@link #stringNeedQuoted()} returns true.<br/>
	 *
	 * @param value String, the value to escape.
	 * @return String, the escaped string
	 * @see #stringNeedQuoted()
	 */
	protected String escape(String value){
		if(stringNeedQuoted()){
			return StringUtils.quote(value);
		}
		return value;
	}

	/**
	 * Try to delete a persistence StringBuffer.
	 */
	@Override
	public void unpersist() throws SAFSException {
		stringFormat.setLength(0);
	}

	@Override
	public Persistable unpickle(Map<String/*className*/, List<String>/*field-names*/> ignoredFields) throws SAFSException{
		//The Persistable is stored in the field 'stringFormat'
		Persistable persistable = null;

		beforeUnpickle();
		persistable = doUnpickle();

		return persistable;
	}

	/**
	 * Prepare the parser with the string format of a Persistable object.
	 * @throws SAFSException
	 */
	protected void beforeUnpickle()  throws SAFSException{
		//do nothing for now.
	}

	/**
	 * Convert the string format of a Persistable object to Persistable object.
	 * @return Persistable, the Persistable object.
	 * @throws SAFSException
	 */
	protected Persistable doUnpickle()  throws SAFSException{
		throw new SAFSException(StringUtils.debugmsg(false)+"Method not supported yet!");
	}

	/**
	 * Convert a Persistable Object to a string. The concrete format depends on the sub-class.
	 *
	 * @param persistable
	 * @param needLeadingNameForContainer
	 * @return
	 * @throws SAFSException
	 */
	protected abstract String parse(Persistable persistable, boolean needLeadingNameForContainer)  throws SAFSException;

	/**
	 * Check if the field is being ignored at the moment for un-pickle.<br/>
	 * @param className String, the className to check
	 * @param field String, the field to check
	 * @return boolean if this field is being ignored.
	 * @see #ignoredFieldsForUnpickle
	 */
	protected boolean isIgnoredField(String className, String field){
		if(ignoredFieldsForUnpickle==null || className==null || field==null){
			return false;
		}

		List<String> ignoredFields = ignoredFieldsForUnpickle.get(className);
		if(ignoredFields==null){
			return false;
		}
		for(String ignoredField:ignoredFields){
			if(ignoredField.equals(field)) return true;
		}
		return false;
	}

	@Override
	public PersistenceType getType(){
		return PersistenceType.STRING;
	}

	@Override
	public String getPersistenceName(){
		return PersistorToString.class.getSimpleName();
	}

}
