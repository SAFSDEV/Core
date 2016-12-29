/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

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
			File file = FileUtilities.deduceTestFile(filename, runtime);
			writer = FileUtilities.getUTF8BufferedFileWriter(file.getAbsolutePath());
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
	 * When writing a string to a file, some special characters MUST be escaped, such as
	 * new line <b>"\n", "\r", "\r\n"</b>, or double quote <b>"</b>, or <b>&lt;?XML>...&lt;/XML></b> etc.<br/>
	 * What characters to escape and how to escape, these depend on the format of the persistence file, and the
	 * parser of the file. Subclass should override this method.<br/>
	 *
	 * @param value String, the value to escape.
	 * @return String, the escaped string
	 */
	protected String escape(String value){
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
			File file = FileUtilities.deduceProjectFile(filename, runtime);
			reader = FileUtilities.getUTF8BufferedFileReader(file.getAbsolutePath());

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
