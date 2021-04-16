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
 * DEC 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 */
public abstract class PersistorToFile extends AbstractRuntimeDataPersistor{

	/**
	 * The name of file to hold the information of a Persistable object.
	 */
	protected String filename = null;

	/**
	 * The file holds the information of a Persistable object.
	 */
	protected File persistFile = null;

	/**
	 * The Writer object of the test file. It is used to write Persistable object.
	 */
	protected Writer writer = null;

	/**
	 * The Reader object of the project file. Read content from it to construct/unpickle a Persistable object.
	 */
	protected Reader reader = null;

	/**
	 * Holding the fields which are ignored for each class. No need to unpickle.<br/>
	 * The field is expressed as a pair of (classname, a list of fields).<br/>
	 * @see #isIgnoredFiled(String)
	 */
	protected Map<String/*className*/, List<String>/*field-names*/> ignoredFieldsForUnpickle = null;

	protected PersistorToFile(RuntimeDataInterface runtime, String filename){
		super(runtime);
		this.filename = filename;
	}

	@Override
	public void persist(Persistable persistable) throws SAFSException {
		super.persist(persistable);

		try {
			//open the persistence file
			persistFile = FileUtilities.deduceTestFile(filename, runtime);
			writer = FileUtilities.getUTF8BufferedFileWriter(persistFile.getAbsolutePath());
			//write the persistable object to the file
			writeHeader(persistable);
			write(persistable);
			writeTailer(persistable);
		} catch (IOException e) {
			String message = FAILStrings.convert(FAILKEYS.FILE_ERROR, "Error opening or reading or writing file '"+filename+"'", filename);
			IndependantLog.error(message+". Met "+e.toString());
			throw new SAFSException(message);
		} finally{
			//close the persistence file
			try {
				if(writer!=null) writer.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the writer on file '"+filename+"'. Met "+e.toString());
			}
		}
	}

	/** This is called before {@link #write(Persistable)}. */
	protected void writeHeader(Persistable persistable)  throws SAFSException, IOException{}
	/**
	 * Write the Persistable object into a persistent material.<br/>
	 * {@link #writeHeader(Persistable)} is called before<br/>
	 * {@link #writeTailer(Persistable)} is called after<br/>
	 */
	protected abstract void write(Persistable persistable)  throws SAFSException, IOException;
	/** This is called after {@link #write(Persistable)}. */
	protected void writeTailer(Persistable persistable)  throws SAFSException, IOException{}

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
	 * Try to delete a persistence file in the test/bench folder.
	 */
	@Override
	public void unpersist() throws SAFSException {
		File file = FileUtilities.deduceTestFile(filename, runtime);
		if(file==null || !file.exists()){
			file = FileUtilities.deduceBenchFile(filename, runtime);
		}

		if(file!=null && file.exists()){
			file.delete();
		}
	}

	@Override
	public Persistable unpickle(Map<String/*className*/, List<String>/*field-names*/> ignoredFields) throws SAFSException{
		Persistable persistable = null;

		try {
			//open the persistence file
			persistFile = FileUtilities.deduceProjectFile(filename, runtime);
			reader = FileUtilities.getUTF8BufferedFileReader(persistFile.getAbsolutePath());

			//read the Persistable object from the file
			ignoredFieldsForUnpickle = ignoredFields;
			beforeUnpickle();
			persistable = doUnpickle();

		} catch (IOException e) {
			String message = FAILStrings.convert(FAILKEYS.FILE_ERROR, "Error opening or reading or writing file '"+filename+"'", filename);
			IndependantLog.error(message+". Met "+e.toString());
			throw new SAFSException(message);
		} finally{
			//close the persistence file
			try {
				if(reader!=null) reader.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the reader on file '"+filename+"'. Met "+e.toString());
			}
		}

		return persistable;
	}

	protected void beforeUnpickle()  throws SAFSException, IOException{
		//do nothing for now.
	}

	protected Persistable doUnpickle()  throws SAFSException, IOException{
		throw new SAFSException(StringUtils.debugmsg(false)+"Method not supported yet!");
	}

	/**
	 * Check if the filed is being ignored at the moment for un-pickle.<br/>
	 * @param className String, the className to check
	 * @param field String, the field to check
	 * @return boolean if this field is being ignored.
	 * @see #ignoredFieldsForUnpickle
	 */
	protected boolean isIgnoredFiled(String className, String field){
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

	public PersistenceType getType(){
		return PersistenceType.FILE;
	}

	public String getPersistenceName(){
		return filename;
	}

	/**
	 * If they have the same filename, then we consider them equivalent
	 */
	@Override
	public boolean equals(Object o){
		if(o==null) return false;
		if(!(o instanceof PersistorToFile)) return false;
		PersistorToFile p = (PersistorToFile) o;

		if(this.equals(o)){
			return true;
		}

		if(filename==null){
			return p.filename==null;
		}else{
			return this.filename.equals(p.filename);
		}
	}

}
