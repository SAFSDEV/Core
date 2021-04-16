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
package org.safs.tools.input;

import org.safs.tools.UniqueStringID;
import org.safs.tools.PathInterface;
import org.safs.tools.drivers.DriverConstant;
import java.io.File;

public class UniqueStringFileInfo
	extends UniqueStringID
	implements UniqueFileInterface {

	protected String filename   = null;
	protected String ucfilename = null;
	protected String lcfilename = null;
	
	protected String filepath  = null;
	protected String separator = null;
	protected String testlevel = null;
	
	/**
	 * Constructor for UniqueStringFileInfo
	 */
	public UniqueStringFileInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringFileInfo
	 */
	public UniqueStringFileInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringFileInfo
	 */
	public UniqueStringFileInfo(String id, String filename, String separator, String testlevel) {
		this(id);
		setFilename(filename);
		setDefaultSeparator(separator);
		setTestLevel(testlevel);
	}

	/**
	 * Set our testlevel to that provided.
	 */
	public void setTestLevel(String testlevel) {
		this.testlevel = testlevel;
	}

	/**
	 * @see SourceInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return testlevel;
	}

	/**
	 * @see SourceInterface#getSourceName()
	 */
	public String getSourceName() {
		return filename;
	}

	/**
	 * Set our filename to that provided.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
		lcfilename = filename.toLowerCase();
		ucfilename = filename.toUpperCase();
	}

	/**
	 * @see UniqueFileInterface#getFilename()
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Set our sourcepath to that provided.
	 */
	public void setSourcePath(String sourcepath) {
		this.filepath = sourcepath;
	}

	
	String returnSetPath(File afile){
		filepath = afile.getPath();
		return filepath;
	}
	
	// find a case-insensitive match to the provided filename.ext
	// rootpath is expected to be a valid/verified path on the current platform.
	// we will search all files in the rootpath directory looking for a case-insensitive
	// match.  This is needed to support Unix case-sensitive filenames.
	private File findFileMP(String rootpath, String afilename, String ext){
		File fdir = new File(rootpath);
		if (!fdir.isDirectory()) return null;
		
		String[] files = fdir.list();
		if ((files == null)||(files.length == 0)) return null;
		
		String lcfile;
		String lcfilename = afilename.toLowerCase();
		if (ext == null) ext = "";
		String lcext = ext.toLowerCase();
		
		File tryfile = null;
		
		for (int i=0; i< files.length; i++){
			tryfile = new File(fdir, files[i]);			
			lcfile = files[i].toLowerCase();
			if (lcfile.equals(lcfilename)){
				//make sure it is NOT a directory
				if (tryfile.isFile()) return tryfile;
			}
			if(lcfile.equals(lcfilename + lcext))
			    return tryfile;
			
		}
		return null;
	}
	
	
	/**
	 * @return null if invalid source information exists.
	 * @see SourceInterface#getSourcePath()
	 */
	public String getSourcePath(PathInterface driver) {
		if (filepath==null){

			String lctestext  = null;
			String uctestext  = null;
			
			try{
				if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL)){
					uctestext = DriverConstant.DEFAULT_CYCLE_TESTNAME_SUFFIX;
					lctestext = uctestext.toLowerCase();
				}
				else if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL)){
					uctestext = DriverConstant.DEFAULT_SUITE_TESTNAME_SUFFIX;
					lctestext = uctestext.toLowerCase();
				}
				else if (testlevel.equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL)){
					uctestext = DriverConstant.DEFAULT_STEP_TESTNAME_SUFFIX;
					lctestext = uctestext.toLowerCase();
				}
				else {
					return null;
				}			
			}
			catch(Exception x){	return null; }
	
			File theFile = findFileMP(driver.getDatapoolDir(), filename, lctestext);
			if ((theFile != null)&&(theFile.isFile())) return returnSetPath(theFile);
			
			theFile = findFileMP(driver.getProjectRootDir(), filename, lctestext);
			if ((theFile != null)&&(theFile.isFile())) return returnSetPath(theFile);
	
			theFile = findFileMP(driver.getBenchDir(), filename, lctestext);
			if ((theFile != null)&&(theFile.isFile())) return returnSetPath(theFile);
	
			// TRY PROVIDED FILENAME and DERIVATIVES		
			theFile = new File(filename);		
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (filename + lctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (filename + uctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (lcfilename);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (lcfilename + lctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (lcfilename + uctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (ucfilename);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (ucfilename + uctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new File (ucfilename + lctestext);
			if (theFile.isFile()) return returnSetPath(theFile);
			
			return null;			
		}
		return filepath;
	}

	/**
	 * Set the separator to that provided.
	 */
	public void setDefaultSeparator(String separator) {
		this.separator = separator;
	}
	/**
	 * @see SourceInterface#getDefaultSeparator()
	 */
	public String getDefaultSeparator() {
		return separator;
	}

}

