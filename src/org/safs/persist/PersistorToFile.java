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
import java.io.Writer;

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

	protected Writer writer = null;

	public PersistorToFile(RuntimeDataInterface runtime, String filename){
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
			throw new SAFSException(message);
		} finally{
			//close the persistence file
			try {
				writer.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the writer on file '"+filename+"'.");
			}
		}
	}

	public void writeHeader(Persistable persistable)  throws SAFSException, IOException{}
	public abstract void write(Persistable persistable)  throws SAFSException, IOException;
	public void writeTailer(Persistable persistable)  throws SAFSException, IOException{}

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

	public PersistenceType getType(){
		return PersistenceType.FILE;
	}

	public String getPersistenceName(){
		return filename;
	}

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
