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
package org.safs.staf.service.map;

import java.io.File;

import org.safs.text.INIFileReader;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSAppMapFile class is intended as the file instance class for a SAFSAppMapService.<br>
 * Although there are no STAF dependencies, the class is tightly integrated with the
 * reader.  It has not been evaluated for any other use.  Though, standalone use
 * is likely possible.
 * <p>
 * Physical SAFS AppMap files are expected to be in a particular format.  This is very much
 * like the format for Windows INI files--the same App Map format currently in use for the
 * Rational Robot SAFS Engine (RRAFS).  (Though, there may be slight differences.)
 * <p>
 * The following characters are allowed to indicate commentlines or are otherwise ignored. Lines
 * are trimmed of leading whitespace before this check is made:
 * <p>
 * &nbsp; &nbsp; &#033; (bang - exclamation point)<br>
 * &nbsp; &nbsp; &#059; (semicolon)<br>
 * &nbsp; &nbsp; &#035; (hash mark)<br>
 * <p>
 * An AppMap "Section" is delimited with square brackets containing the name of the section.
 * In SAFS automation parlance, these are the "Window" definition sections.  A section delimiter
 * should appear all by itself on a line. There should be no additional text.  If the closing
 * bracket is missing, the handler will graciously except the entry.  The line is first trimmed
 * before the check for a section is made.
 * <p>
 * A section identifier is not case-sensitive.
 * <p>
 * If an AppMap contains a [ApplicationConstants] section, that will be considered the "default"
 * section.  Otherwise, any first, unnamed section is the "default".
 * <p>
 * Items within a section have a NAME = VALUE format.<br>
 * To the left of the Equals sign is the NAME.  The substring is trimmed of leading or
 * trailing whitespace and is not case-sensitive.
 * Everything to the right of the Equals sign is returned unmodified.  It is NOT trimmed
 * of leading or trailing whitespace and the text is returned unmodified.
 * <p>
 * An example is below:
 * <p>
 * &nbsp; &nbsp; &#033 this line is ignored<br>
 * &nbsp; &nbsp; &#059 this line is ignored<br>
 * &nbsp; &nbsp; &#035 this line is ignored<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; &#059 the following 2 items are in an initial, unnamed section.<br>
 * &nbsp; &nbsp; &#059 these are accessible by passing a NULL or empty string when<br>
 * &nbsp; &nbsp; &#059 specifying a Section parameter.<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; An Item  = a value<br>
 * &nbsp; &nbsp; Another  = another value<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; <b>[A Section Name]</b><br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; An Item  = a value<br>
 * &nbsp; &nbsp; Another  = another value<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; <b>[A Second Section]</b><br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; An Item  = a value<br>
 * &nbsp; &nbsp; Another  = another value<br>
 * &nbsp; &nbsp; <br>
 * <p>
 * The file uses a java.io.BufferedReader for STORED memory mode, or a java.io.RandomAccessFile
 * for MAPPED memory mode.  (MAPPED mode is not yet implemented.)
 * <p>
 * @author Carl Nagle, SAS Institute
 * @version 1.0, 06/06/2003
 * @see SAFSAppMapService
 *
 * @author Carl Nagle  JUL 07, 2003 Strip single or double quotes from retrieved items.
 * @author Carl Nagle  DEC 11, 2003 Added clearCache capability to force reread of files.
 *
 * Software Automation Framework Support (SAFS) http://safsdev:8880<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 ******************************************************************************************/
public class SAFSAppMapFile extends INIFileReader {

	/*******************************************************************************************
	 * This constructor will create an inoperable (Closed) file object.  No use whatsoever. :)
	 ******************************************************************************************/
	public SAFSAppMapFile (){super();}

	protected String machine  = new String();
	protected String process  = new String();
	protected int    handle   = 0;
	protected String fileid   = new String();

	// backwards compatibility with refactored classes
	public static final String SAM_DEFAULT_MAP_SECTION_COMMAND = IFR_DEFAULT_MAP_SECTION_COMMAND;
	public static final int SAM_MEMORY_MODE_STORED = IFR_MEMORY_MODE_STORED;
	public static final int SAM_MEMORY_MODE_MAPPED = IFR_MEMORY_MODE_MAPPED;

	/*******************************************************************************************
	 * The constructor used by the SAFSAppMapReader.
	 *
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(machine, process, handle, fileid, file);
	 * <p>
	 * @param machine The STAF machine that requested this file be opened.
	 * <p>
	 * @param process The STAF process that requested this file be opened.
	 * <p>
	 * @param handle  The STAF process handle that requested this file be opened.
	 * <p>
	 * @param fileid A unique String ID to identify this file for the requesting process.
	 *               This is not a filename.  Multiple open views of the same file could be opened
	 *               and each should have a unique fileid within the process namespace.  However,
	 *               there is probably little value in multiple views unless MAPPED memory mode
	 *               is used.
	 * <p>
	 * @param file A valid File object for the file to be opened.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently
	 *                   only the STORED model is used.
	 ******************************************************************************************/
	public SAFSAppMapFile (String machine, String process, int handle,
	                       String fileid, File file, int memorymode){

		super(file, memorymode);
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

