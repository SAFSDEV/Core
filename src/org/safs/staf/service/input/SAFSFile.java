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
package org.safs.staf.service.input;

import java.io.File;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSFile class is intended as the file instance class for a SAFSFileReader.<br>
 * Although there are no STAF dependencies, the class is tightly integrated with the
 * reader.  It has not been evaluated for any other use.  Though, standalone use
 * is likely possible.
 * <p>
 * This class represents the most basic reader functionality.  It will simply read a line
 * of text for each call to readLine().  It uses a java.io.BufferedReader as the underlying
 * IO mechanism.
 * <p>
 * @author Carl Nagle, SAS Institute
 * @version 1.0, 06/02/2003
 * @see SAFSFileReader
 *
 * Software Automation Framework Support (SAFS) https://safsdev.github.io/<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 ******************************************************************************************/
public class SAFSFile extends org.safs.text.FileLineReader {

	protected String machine  = new String();
	protected String process  = new String();
	protected int    handle   = 0;
	protected String fileid   = new String();

	/*******************************************************************************************
	 * This constructor will create an inoperable (Closed) file object.  No use whatsoever. :)
	 ******************************************************************************************/
	public SAFSFile (){ super();}

	/*******************************************************************************************
	 * The constructor used by the SAFSFileReader.
	 *
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(machine, process, handle, fileid, file);
	 * <p>
	 * @param machine The STAF machine that requested this file be opened.
	 * @param process The STAF process that requested this file be opened.
	 * @param handle  The STAF process handle that requested this file be opened.
	 * @param fileid A unique String ID to identify this file for the requesting process.
	 *               This is not a filename.  Multiple open views of the same file can be opened
	 *               and each should have a unique fileid within the process namespace.
	 * @param file A File object that references the file to be opened.
	 ******************************************************************************************/
	public SAFSFile (String machine, String process, int handle, String fileid, File file){
		super(file);
		if (machine != null) this.machine = machine.toString();
		if (process != null) this.process = process.toString();
		if (fileid  != null) this.fileid  = fileid.toString();
		if (handle   >  0  ) this.handle  = handle;

	}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the value of the machine constructor parameter
	 ******************************************************************************************/
	public  String  getMachine  (){ return machine.toString() ;}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the value of the process constructor parameter
	 ******************************************************************************************/
	public  String  getProcess  (){ return process.toString() ;}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the value of the fileid constructor parameter
	 ******************************************************************************************/
	public  String  getFileID   (){ return fileid.toString()  ;}

	/*******************************************************************************************
	 * Subclasses should not need to override this function.
	 * <p>
	 * @return the value of the handle constructor parameter
	 ******************************************************************************************/
	public   int    getHandle   (){ return handle  ;}
}

