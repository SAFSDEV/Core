/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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
public abstract class VerifierToFile extends AbstractRuntimeDataVerifier{

	/**
	 * The name of file to hold the information of a Persistable object to be verified.
	 */
	protected String filename = null;

	protected Reader reader = null;

	public VerifierToFile(RuntimeDataInterface runtime, String filename){
		super(runtime);
		this.filename = filename;
	}

	@Override
	public void verify(Persistable persistable, boolean... conditions) throws SAFSException {
		super.verify(persistable);

		try {
			//open the persistence file
			File file = FileUtilities.deduceBenchFile(filename, runtime);
			reader = FileUtilities.getUTF8BufferedFileReader(file.getAbsolutePath());
			//write the persistable object to the file
			beforeCheck(persistable, conditions);
			check(persistable, conditions);
			afterCheck(persistable, conditions);
		} catch (IOException e) {
			String message = FAILStrings.convert(FAILKEYS.FILE_ERROR, "Error opening or reading or writing file '"+filename+"'", filename);
			throw new SAFSException(message);
		} finally{
			//close the persistence file
			try {
				reader.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the reader on file '"+filename+"'.");
			}
		}
	}

	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{}
	public abstract void check(Persistable persistable, boolean... conditions)  throws SAFSException, IOException;
	public void afterCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{}

	public PersistenceType getType(){
		return PersistenceType.FILE;
	}

	/**
	 * If they have the same filename, then we consider them equivalent
	 */
	@Override
	public boolean equals(Object o){
		if(o==null) return false;
		if(!(o instanceof VerifierToFile)) return false;
		VerifierToFile p = (VerifierToFile) o;

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
