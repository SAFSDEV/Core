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
import java.util.Vector;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;
import com.ibm.staf.service.STAFServiceInterfaceLevel1;


/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSTextFileReader class is an external STAF service run by the JSTAF Service Proxy.<br>
 * The intention is to provide buffered read-only file services for reading text files
 * line-by-line, bookmarking a particular line, and even going back to the beginning
 * of the file.
 * <p>
 * The TextFileReader service provides the following commands:<br>
 * <ul>
 * <li>OPEN - open a text file for read operations
 * <li>NEXT - return the next line (record) from the file
 * <li>PEEK - get the current line (again) without incrementing the line pointer
 * <li>MARK - bookmark the file position for any subsequent RESET
 * <li>RESET - reset the line pointer to the previous MARK
 * <li>BEGIN - reset the line pointer to the beginning of the file
 * <li>QUERY - retrieve status information from the file
 * <li>CLOSE - close and release resources on the text file
 * <li>LIST - list open information for the service instance
 * <li>HELP - returns syntax information
 * </ul>
 * <h2>1.0 SAFSTextFileReader Service Registration</h2>
 * <p>
 * Each instance of the service must be registered via the STAF Service service.
 * <p>
 * Example showing comandline registration:
 * <p><pre>
 * STAF LOCAL SERVICE ADD SERVICE &lt;servicename> LIBRARY JSTAF /
 *            EXECUTE org/safs/staf/service/SAFSTextFileReader [PARMS &lt;Parameters>]
 *
 * Other examples:
 *
 * SERVICE ADD SERVICE FileReader  LIBRARY JSTAF EXECUTE org/safs/staf/service/TextFileReader
 *
 * SERVICE ADD SERVICE StepReader  LIBRARY JSTAF EXECUTE org/safs/staf/service/TextFileReader PARMS DIR c:\repo\Datapool EXT ".sdd"
 *
 * SERVICE ADD SERVICE BenchReader LIBRARY JSTAF EXECUTE org/safs/staf/service/TextFileReader PARMS DIR "c:\new repo\Datapool\Bench"
 *
 * </pre>
 * <p>
 * <b>1.1</b> Valid Parameters when registering a SAFSTextFileReader service:
 * <p>
 * <b>1.1.1 DIR &lt;default directory></b><br>
 * If provided, the DIR parameter specifies a default directory to use if the OPEN
 * request provides relative path information or no path information at all.  File
 * searches will not use environment PATH information.  The OPEN request expects a
 * full filename path, or a path relative to this DIR option.
 * <p>
 * EX: PARMS DIR "c:\testrepo\Datapool"
 * <p>
 * If the DIR parameter is not provided, then OPEN requests will not attempt
 * relative path searches.  The filename provided to the OPEN request must then be
 * an exact full filepath match or the OPEN request will fail.
 * <p>
 * <b>1.1.2 EXT &lt;default file extension></b><br>
 * If provided, the EXT parameter specifies a default file extension (suffix) to try if
 * the OPEN request does not find the file as provided.  You must include any period
 * (.) if it is to be part of any appended suffix.
 * <p>
 * EX: PARMS EXT .txt
 * <p>
 * If the EXT parameter is not provided, then OPEN requests will not attempt the additional
 * file search with the appended extension if the provided filename was not found.  The
 * filename provided to the OPEN request must then be an exact filename.ext match or
 * the OPEN request will fail.
 * <p>
 * <h2>2.0 SAFSTextFileReader Commands</h2>
 * <p>
 * Note, there must be a uniquely qualified match for NAME/ID combinations listed below.
 * If the same Process has opened two different files with the same ID from 2 different
 * Handles, then you will have to use HANDLE, instead of NAME.
 * <p>
 * <h3>2.1 OPEN </h3>
 * <p>
 * The OPEN command attempts to open a file for read operations.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * OPEN ID &lt; FileID > FILE &lt; Filename > /<br>
 * [ TRIMLEADING | TRIMTRAILING | TRIMWHITESPACE ] [ SKIPBLANKLINES ] /<br>
 * [ COMMENTLINE &lt;Prefix> ]... [ NOLINENUMBERS ]
 * <p>
 * <b>2.1.1</b> ID is a unique name for this file handling instance.<br>
 * <p>
 * <b>2.1.2</b> FILE is the filename of the file to open.<br>
 * If a default Directory was specified when the service was launched, then the filename
 * can be relative to that directory.  Otherwise, the full filepath must be specified.<br>
 * If a default Extension (Ext) was specified when the service was launched, then the
 * filename can be specified without the extension.<br>
 * <p>
 * <b>2.1.3</b> TRIMLEADING, TRIMTRAILING, TRIMWHITESPACE<br>
 * If provided, the TrimLeading option specifies that each line should be trimmed of
 * leading whitespace only.  TrimTrailing specifies that each line should be trimmed of
 * trailing whitespace only.  TrimWhitespace specifies that each line should be trimmed of
 * both leading and trailing whitespace.<br>
 * "Whitespace" is considered to be all control characters below '\u0020'(SPACE).
 * <p>
 * <b>2.1.4</b> SKIPBLANKLINES instructs the reader to skip empty lines and
 * lines containing only whitespace.  Skipped blank lines are still counted.<br>
 * "Whitespace" is considered to be all control characters below '\u0020'(SPACE).
 * <p>
 * <b>2.1.5</b> COMMENTLINE specifies a substring of 1 or more characters that
 * prefix a line and identify the line as a comment.  If COMMENTLINE options are specified,
 * lines that begin with the specified prefix are skipped (though counted) for read
 * operations.  You can specify up to 10 COMMENTLINE options and each will separately
 * identify comment lines in the file.  The test for a comment line occurs
 * after leading whitespace has been trimmed if TRIMLEADING or TRIMWHITESPACE options
 * have been specified.<br>
 * "Whitespace" is considered to be all control characters below '\u0020'(SPACE).
 * <p>
 * <b>2.1.6</b> NOLINENUMBERS specifies lines read from the file should not be returned
 * with line numbers prefixed.
 * <p>
 * <h3>2.2 NEXT </h3>
 * <p>
 * The NEXT command returns the next available line in the specified file.
 * The text will be trimmed and prefixed with linenumbers according to parameters
 * provided when the file was OPENed.  Blank lines and/or comment lines will also be
 * skipped according to OPEN parameters.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; fileID > NEXT
 * <p>
 * <b>2.2.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.2.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.2.3</b> ID is the unique fileID that was provided when the file was OPENed.
 * <p>
 * <h3>2.3 PEEK </h3>
 * <p>
 * The PEEK command returns (again) the current line without incrementing line pointers.
 * This is the same text that would have been received from the most recent NEXT command.
 * The value returned from PEEK is undefined following a RESET or BEGIN command.
 * Not until the NEXT command will PEEK accurately reflect the last (current) read.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; fileID > PEEK
 * <p>
 * <b>2.3.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.3.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.4 MARK </h3>
 * <p>
 * The MARK command bookmarks the current line number (the start of the next line)
 * for any subsequent RESET.  That is: MARK points to the line BEFORE the first line
 * that will be read after a RESET.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > MARK
 * <p>
 * <b>2.4.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.4.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.4.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.5 RESET </h3>
 * <p>
 * The RESET command resets the reader to the previous MARK position.  The MARK
 * effectively points to the next line to read, and any "current" line text (such as
 * returned in PEEK) may not be accurate until the NEXT command is performed.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > RESET
 * <p>
 * <b>2.5.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.5.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.5.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.6 BEGIN </h3>
 * <p>
 * The BEGIN command resets the line pointer to the beginning of the file.  The pointer
 * is set to read the first line in the file, and any "current" line text (such as
 * returned in PEEK) may not be accurate until that first NEXT read is performed.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > BEGIN
 * <p>
 * <b>2.6.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.6.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.6.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.7 CLOSE </h3>
 * <p>
 * The CLOSE command closes and releases resources for the file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > CLOSE
 * <p>
 * <b>2.7.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.7.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.7.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <h3>2.8 QUERY </h3>
 * <p>
 * The QUERY command returns requested information about an open file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] ID &lt; FileID > QUERY
 * &lt STATUS | LINEPOINTER | MARKPOINTER | FULLPATH | LASTERROR >
 * <p>
 * <b>2.8.1</b> HANDLE specifies the handle of the process that OPENed the file.
 * You can instead provide the NAME of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.8.2</b> NAME specifies the name of the process that OPENed the file.
 * You can instead provide the HANDLE of the process that OPENed the file.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.8.3</b> ID is the unique fileID that was given provided when the file was OPENed.
 * <p>
 * <b>2.8.4</b> STATUS returns one of "OPEN", "CLOSED", or "EOF".  If the "CLOSED"
 * status is returned, that generally means some type of critical failure has occurred.
 * A file that is OPEN or at EOF can still be RESET or sent to the first line with BEGIN.
 * <p>
 * <b>2.8.5</b> LINEPOINTER returns the current value of the line pointer.  This generally
 * contains the number of the "current" line--the last line read.
 * The first line of a file is line 1.
 * <p>
 * <b>2.8.6</b> MARKPOINTER returns the current value of the MARK pointer.  If a bookMARK has
 * been set, the pointer contains the line number of the line BEFORE the line that will be
 * read after a RESET.  That is, the line number of the line that was just read before the
 * MARK command was performed.  This is, effectively, a pointer to the next line. 8^p
 * <p>
 * <b>2.8.7</b> FULLPATH is the full path to the file.  Due to the OPEN parameters DIR and
 * EXT, the full path to the file may be different than any relative path information that
 * was provided to the OPEN command.
 * <p>
 * <b>2.8.8</b> LASTERROR is not yet implemented.
 * <p>
 * <h3>2.9 LIST </h3>
 * <p>
 * The LIST command returns info on each open file for the service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * [ HANDLE &lt; Handle > | NAME &lt; Process > ] LIST
 * <p>
 * <b>2.9.1</b> HANDLE specifies the handle of the process to query.
 * You can instead provide the NAME of the process to query. If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <b>2.9.2</b> NAME specifies the name of the process to query.
 * You can instead provide the HANDLE of the process to query.  If neither is
 * provided then the Handle of the process that originated this request is assumed.
 * <p>
 * <h3>2.10 HELP </h3>
 * <p>
 * The HELP command returns syntax information for the TextFileReader service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HELP
 * <p>
 * Software Automation Framework Support (SAFS) https://safsdev.github.io/<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 *********************************************************************************************/
public class SAFSTextFileReader extends SAFSFileReader implements STAFServiceInterfaceLevel1 {

	// OVERRIDE THIS WITH YOUR SAFSFILE TYPE
	/**********************************************************************
	 * 	Override the superclass file type information.
	 **********************************************************************/
	protected           String SFR_SERVICE_SAFSFILE_LISTINFO   = "SAFSTextFile";

	/**********************************************************************
	 * 	The maximum number of COMMENT options handled in a request.
	 **********************************************************************/
	protected             int  SFR_SERVICE_PARM_COMMENT_MAX    = 10;

	/**********************************************************************
	 * 	Overrides the superclass limit of maximum request arguments.
	 **********************************************************************/
	protected             int  SFR_SERVICE_REQUEST_ARGS_MAX    = 30;  //most OPEN should see


	public static final String SFR_SERVICE_REQUEST_NEXT        = "NEXT";
	public static final String SFR_SERVICE_REQUEST_PEEK        = "PEEK";
	public static final String SFR_SERVICE_REQUEST_MARK        = "MARK";
	public static final String SFR_SERVICE_REQUEST_RESET       = "RESET";
	public static final String SFR_SERVICE_REQUEST_BEGIN       = "BEGIN";

	public static final String SFR_SERVICE_PARM_COMMENT        = "COMMENTLINE";
	public static final String SFR_SERVICE_PARM_TRIMLEADING    = "TRIMLEADING";
	public static final String SFR_SERVICE_PARM_TRIMTRAILING   = "TRIMTRAILING";
	public static final String SFR_SERVICE_PARM_TRIMWHITESPACE = "TRIMWHITESPACE";
	public static final String SFR_SERVICE_PARM_SKIPBLANKLINES = "SKIPBLANKLINES";
	public static final String SFR_SERVICE_PARM_NOLINENUMBERS  = "NOLINENUMBERS";

	// parms for QUERY
	public static final String SFR_SERVICE_PARM_LINEPOINTER    = "LINEPOINTER";
	public static final String SFR_SERVICE_PARM_MARKPOINTER    = "MARKPOINTER";

	// MUST CALL THE SUPERCLASS CONSTRUCTOR
	/**********************************************************************
	 * 	Default constructor MUST call the superclass constructor.
	 **********************************************************************/
	public SAFSTextFileReader (){  super(); }

	// ADD NEW COMMAND EXTENSIONS
	/**********************************************************************
	 * 	Add all our new keywords to the list of superclass keywords.
	 **********************************************************************/
	@Override
	protected void addCommandOptions(STAFCommandParser aparser){

		aparser.addOption( SFR_SERVICE_REQUEST_NEXT , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_PEEK , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_MARK , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_RESET, 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_REQUEST_BEGIN, 1, STAFCommandParser.VALUENOTALLOWED );

		aparser.addOption( SFR_SERVICE_PARM_TRIMLEADING   , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_TRIMTRAILING  , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_TRIMWHITESPACE, 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_SKIPBLANKLINES, 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_NOLINENUMBERS , 1, STAFCommandParser.VALUENOTALLOWED );

		aparser.addOption( SFR_SERVICE_PARM_LINEPOINTER , 1, STAFCommandParser.VALUENOTALLOWED );
		aparser.addOption( SFR_SERVICE_PARM_MARKPOINTER , 1, STAFCommandParser.VALUENOTALLOWED );

		aparser.addOption( SFR_SERVICE_PARM_COMMENT, SFR_SERVICE_PARM_COMMENT_MAX, STAFCommandParser.VALUEREQUIRED);
		super.addCommandOptions(aparser);
    }

	// ADD OUR NEW COMMANDS TO THE LIST OF EXCLUSIVE OPTIONS
	/**********************************************************************
	 * 	Add our new commands to the list of superclass commands.
	 **********************************************************************/
    @Override
	protected String buildCommandList(String requestoptions) {

		// each request MUST have only 1 of these
		requestoptions += s+ SFR_SERVICE_REQUEST_NEXT  +s+ SFR_SERVICE_REQUEST_RESET +s+
		                     SFR_SERVICE_REQUEST_PEEK  +s+ SFR_SERVICE_REQUEST_MARK  +s+
		                     SFR_SERVICE_REQUEST_BEGIN +s;

		return super.buildCommandList(requestoptions);
	}

	// ADD OUR NEW QUERY OPTIONS
	/**********************************************************************
	 * 	Add our new QUERY options to existing superclass QUERY options.
	 **********************************************************************/
	@Override
	protected String buildQueryCommandList(String queryoptions) {

		// QUERY parameters mutually exclusive
		queryoptions += s+ SFR_SERVICE_PARM_LINEPOINTER +s+
		                   SFR_SERVICE_PARM_MARKPOINTER +s;

		return super.buildQueryCommandList(queryoptions);
	}

	// ADD OUR COMMAND DEPENDENCIES
	/**********************************************************************
	 * 	Add our new command dependencies to superclass dependencies.
	 **********************************************************************/
	protected void buildCommandOptionNeeds(STAFCommandParser aparser) {

		// REQUIRED parameters for each type of request
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_NEXT , SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_PEEK , SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_MARK , SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_RESET, SFR_SERVICE_PARM_ID );
		aparser.addOptionNeed( SFR_SERVICE_REQUEST_BEGIN, SFR_SERVICE_PARM_ID );

		// QUERY parameters exclusive to QUERY
		aparser.addOptionNeed( SFR_SERVICE_PARM_LINEPOINTER , SFR_SERVICE_REQUEST_QUERY );
		aparser.addOptionNeed( SFR_SERVICE_PARM_MARKPOINTER , SFR_SERVICE_REQUEST_QUERY );

		super.addCommandOptionNeeds( aparser );
	}

	// PROCESS A SERVICE REQUEST NOT HANDLED BY THE SUPERCLASS
	/**********************************************************************
	 * 	Process any of the new commands we have added for this service.
	 **********************************************************************/
	@Override
	protected STAFResult processRequest( STAFResult result, String machine, String process,
	                                     int handle, STAFCommandParseResult parsedData) {

		// gonna do it here cause it is used everywhere (except HELP)
		String fileid = parsedData.optionValue(SFR_SERVICE_PARM_ID); // may be empty

		// ===============================================================
		if( parsedData.optionTimes(SFR_SERVICE_REQUEST_BEGIN) > 0){

			SAFSTextFile textfile = (SAFSTextFile) getParsedDataTextFile( result, handle, fileid, parsedData );
			if (result.rc != 0)  return result;

			textfile.begin();
			if(textfile.isClosed()){
				result.rc = STAFResult.FileOpenError;
				result.result = textfile.getFullpath();
			}else{
				result.result = SFR_SERVICE_REQUEST_BEGIN +c+ String.valueOf(textfile.getLineNumber()+1);
			}
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_RESET) > 0){

			SAFSTextFile textfile = (SAFSTextFile) getParsedDataTextFile( result, handle, fileid, parsedData );
			if (result.rc != 0)  return result;

			textfile.reset();
			if(textfile.isClosed()){
				result.rc = STAFResult.FileOpenError;
				result.result = textfile.getFullpath();
			}else{
				result.result = SFR_SERVICE_REQUEST_RESET +c+ String.valueOf(textfile.getLineNumber()+1);
			}
			return result;


		// ===============================================================
		}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_MARK) > 0){

			SAFSTextFile textfile = (SAFSTextFile) getParsedDataTextFile( result, handle, fileid, parsedData );
			if (result.rc != 0)  return result;

			textfile.mark();
			if(textfile.isClosed()){
				result.rc = STAFResult.FileOpenError;
				result.result = textfile.getFullpath();
			}else{
				result.result = SFR_SERVICE_REQUEST_MARK +c+ String.valueOf(textfile.getMark()+1);
			}
			return result;


		// ===============================================================
		}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_PEEK) > 0){

			SAFSTextFile textfile = (SAFSTextFile) getParsedDataTextFile( result, handle, fileid, parsedData );
			if (result.rc != 0)  return result;

			result.result = textfile.peek();
			if(textfile.isClosed()){
				result.rc = STAFResult.FileReadError;
				result.result = textfile.getFullpath();
			}

			if (result.result == null) result.result = new String();
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SFR_SERVICE_REQUEST_NEXT) > 0){

			SAFSTextFile textfile = (SAFSTextFile) getParsedDataTextFile( result, handle, fileid, parsedData );
			if (result.rc != 0)  return result;

			result.result = textfile.next();
			if(textfile.isClosed()){
				result.rc = STAFResult.FileReadError;
				result.result = textfile.getFullpath();
			}

			if (result.result == null) result.result = new String();
			return result;

		// ===============================================================
		}else{
			super.processRequest(result, machine, process, handle, parsedData);
		}
		return result;
	}

	// PREPEND OUR HELP INFORMATION TO THE SUPERCLASS HELP INFORMATION
	/**********************************************************************
	 * 	Prepend our HELP text to the superclass HELP.
	 **********************************************************************/
	@Override
	protected String getHELPInfo(String info){

		info += r+
		        "SAFSTextFileReader HELP" +r+
		        r+
		        "OPEN  ID <fileID> FILE <filename> [SKIPBLANKLINES] [NOLINENUMBERS]" +r+
		        "      [TRIMLEADING] [TRIMTRAILING] [TRIMWHITESPACE]" +r+
		        r+
		        "[HANDLE <handle> | NAME <process>] ID <fileID> /"+r+
		        "      NEXT | PEEK | MARK | RESET | BEGIN" +r+
		        r+
		        "[HANDLE <handle> | NAME <process>] ID <fileID> /"+r+
		        "      QUERY <LINEPOINTER | MARKPOINTER>" +r;

		return super.getHELPInfo(info);
	}


	// OVERRIDE THE SAFSFILE TYPE
	/**********************************************************************
	 * 	Overrides the superclass function to return our SAFSTextFile type.
	 **********************************************************************/
	@Override
	protected String getLISTInfo () { return SFR_SERVICE_SAFSFILE_LISTINFO; }


	// BUILD OUR OPEN RESPONSE WITH THE SUPERCLASS
	/**********************************************************************
	 * 	Append our string response to the OPEN file request.
	 **********************************************************************/
	@Override
	protected  String  getOPENInfo   (STAFCommandParseResult parsedData, String info){

		int options = parsedData.optionTimes(SFR_SERVICE_PARM_COMMENT);

		if (options > 0) {

			String cid = null;
			for(int i = 0; i < options; i++){
				cid = parsedData.optionValue(SFR_SERVICE_PARM_COMMENT, i+1);
				info += "CL:"+ cid +c;
			}
		}

		info += (parsedData.optionTimes(SFR_SERVICE_PARM_SKIPBLANKLINES) > 0) ? new String("SB:"):new String();
		info += (parsedData.optionTimes(SFR_SERVICE_PARM_NOLINENUMBERS ) > 0) ? new String("NL:"):new String();
		info += (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMLEADING   ) > 0) ? new String("TL:"):new String();
		info += (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMTRAILING  ) > 0) ? new String("TT:"):new String();
		info += (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMWHITESPACE) > 0) ? new String("TW:"):new String();

		return super.getOPENInfo(parsedData, info);
	}

	/**********************************************************************
	 * 	Handle the new QUERY options we have added for this service.
	 **********************************************************************/
	@Override
	protected  String  getQUERYInfo  (SAFSFile textfile,
	                                  STAFCommandParseResult parsedData,
	                                  String info){

		SAFSTextFile tfile = (SAFSTextFile) textfile;

		if  (parsedData.optionTimes(SFR_SERVICE_PARM_LINEPOINTER)       > 0) {
			info += String.valueOf(tfile.getLineNumber());

		}else if (parsedData.optionTimes(SFR_SERVICE_PARM_MARKPOINTER) > 0) {
			info += String.valueOf(tfile.getMark());
		}

		return super.getQUERYInfo(textfile, parsedData, info);
	}


	/**********************************************************************
	 * 	Overrides the superclass function to instantiate SAFSTextFiles instead.
	 **********************************************************************/
	@Override
	protected SAFSFile openFile (String machine, String process, int handle,
	                             String fileid , File file, STAFCommandParseResult parsedData){

		Vector comments = null;
		int options = parsedData.optionTimes(SFR_SERVICE_PARM_COMMENT);

		if (options > 0) {

			comments = new Vector(options);
			String cid = null;
			for(int i = 0; i < options; i++){
				cid = parsedData.optionValue(SFR_SERVICE_PARM_COMMENT, i+1);
				comments.addElement(cid.toString());
			}
		}
		return new SAFSTextFile ( machine, process, handle,
				                  fileid,
				                  file,
			                      (parsedData.optionTimes(SFR_SERVICE_PARM_SKIPBLANKLINES) > 0),
				                  (parsedData.optionTimes(SFR_SERVICE_PARM_NOLINENUMBERS ) > 0),
				                  (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMLEADING   ) > 0),
				                  (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMTRAILING  ) > 0),
				                  (parsedData.optionTimes(SFR_SERVICE_PARM_TRIMWHITESPACE) > 0),
				                  comments);
	}
}

