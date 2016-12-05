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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

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
	
	protected BufferedWriter bufferedWriter = null;
	
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
			bufferedWriter = FileUtilities.getUTF8BufferedFileWriter(file.getAbsolutePath());
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
				bufferedWriter.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the buffered writer on file '"+filename+"'.");
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
	
	public Type getType(){
		return Type.FILE;
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
		
		if(filename==null){
			return p.filename==null;
		}else{
			return this.filename.equals(p.filename);
		}
	}

}
