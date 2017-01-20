/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 *   <br>   Sep 23, 2003    (DBauman) Original Release
 *   <br>   Oct 02, 2003    (DBauman) moved to package org.safs because no dependency on rational
 *   <br>   NOV 21, 2003    (Carl Nagle) Added FindSubstringInFile
 *   <br>   Jan 13, 2004    (Carl Nagle) Fixed findSubstrings to accept caret in varname strings.<br>
 *   <br>   Jan 20, 2006    (Carl Nagle) Removed incorrectly implemented ReadFileString support.
 *   <br>   Aug 16, 2006    (PHSABO) Added FilterImage method to black out parts of images.
 *   <br>   Nov 07, 2006    (Bob Lawler) Added DeleteDirectoryContents and CopyMatchingFiles. RJL
 *   <br>   Nov 07, 2006    (Bob Lawler) Updated copyFile() to use new actuallyCopyBytes() helper method. RJL
 *   <br>   Dec 19, 2006    (Carl Nagle) Updated openFile() to handle new OpenUTF8File command
 *   <br>	May 26, 2008	(LeiWang) Add keyword OnFileEOFGoToBlockId
 *   <br>	Sep 23, 2008	(LeiWang) Modified methods setFileProtections(),getFileProtections(),getFiles()
 *   								  Add method isSomeFile()
 *   								  Add inner class RJFileFilter
 *   								  See defect S0536736
 *   <br>   DEC 07, 2009	(JunwuMa) Update RJFileFilter.accept() to use the same way for non-Windows to decide 
 *                                    whether a file on Windows is archive. Fix S0629544.
 *   <br>	FEB 25, 2010	(JunwuMa) Added two keywords GETTEXTFROMIMAGE and SAVETEXTFROMIMAGE 
 *                                    for detecting text in image file using OCR.  
 *   <br>	APR 20, 2010	(LeiWang) Modify method GetSaveTextFromImage(): use static method of OCREngine to get
 *                                    an OCR engine to use.                              
 *   <br>   Mar 20, 2013    (Carl Nagle) Fixed FilterImage to use ImageUtils and accept more formats.
 *   <br>   Nov 19, 2014    (Lei Wang) Refactor: move codes to FileUtilies, fix errors, model FileAttribute.
 *   <br>   NOV 29, 2016    (Lei Wang) Modified readFileLine(): support to read the whole file content.
 **/
package org.safs;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.safs.android.auto.lib.Console;
import org.safs.image.ImageUtils;
import org.safs.natives.NativeWrapper;
import org.safs.text.FAILStrings;
import org.safs.text.FileLineReader;
import org.safs.text.FileProcessor;
import org.safs.text.FileUtilities;
import org.safs.text.FileUtilities.DateType;
import org.safs.text.FileUtilities.FileAttribute;
import org.safs.text.FileUtilities.FileAttributeFilter;
import org.safs.text.GENStrings;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

/**
 * DCDriverFileCommands, process a file driver commands
 * instantiated by DCDriverCommand
 *
 * @author Doug Bauman
 * @since   Sep 23, 2003
 **/
public class DCDriverFileCommands extends DriverCommand {
  public static final String CLOSEFILE                     = "CloseFile";
  public static final String COPYFILE                      = "CopyFile";
  public static final String COPYMATCHINGFILES             = "CopyMatchingFiles";
  public static final String CREATEDIRECTORY               = "CreateDirectory";
  public static final String CREATEFILE                    = "CreateFile";
  public static final String DELETEDIRECTORY               = "DeleteDirectory";
  public static final String DELETEDIRECTORYCONTENTS       = "DeleteDirectoryContents";
  public static final String DELETEFILE                    = "DeleteFile";
  public static final String FILTERTEXTFILE                = "FilterTextFile";
  public static final String FINDSQAFILE                   = "FindSqaFile";
  public static final String FINDSUBSTRINGINFILE           = "FindSubstringInFile";
  public static final String GETSUBSTRINGSINFILE           = "GetSubstringsInFile";
  public static final String GETSTRINGCOUNTINFILE          = "GetStringCountInFile";
  public static final String GETFILEDATETIME               = "GetFileDateTime";
  public static final String GETFILESIZE                   = "GetFileSize";
  public static final String GETFILES                      = "GetFiles";
  public static final String IFEXISTDIR                    = "IfExistDir";
  public static final String IFEXISTFILE                   = "IfExistFile";
  public static final String OPENFILE                      = "OpenFile";
  public static final String OPENUTF8FILE                  = "OpenUTF8File";
  public static final String PRINTTOFILE                   = "PrintToFile";
  public static final String WRITEFILECHARS                = "WriteFileChars";
  public static final String READFILECHARS                 = "ReadFileChars";
  public static final String READFILELINE                  = "ReadFileLine";
  //public static final String READFILESTRING                = "ReadFileString";
  public static final String RENAMEFILE                    = "RenameFile";
  public static final String GETFILEPROTECTIONS            = "GetFileProtections";
  public static final String SETFILEPROTECTIONS            = "SetFileProtections";
  public static final String ISENDOFFILE                   = "IsEndOfFile";
  public static final String GETINIFILEVALUE               = "GetINIFileValue";
  public static final String FILTERIMAGE                   = "FilterImage";
  public static final String ONFILEEOFGOTOBLOCKID          = "OnFileEOFGoToBlockId";
  public static final String GETTEXTFROMIMAGE		       = "GetTextFromImage";
  public static final String SAVETEXTFROMIMAGE    		   = "SaveTextFromImage";

  /** the 3 marker bytes at the beginning of a UTF-8 file--and the first line **/
  public static final byte[] UTF8_MARKER = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
  
  /** "UTF-8" */
  public static final String UTF8_ENCODING = "UTF-8";
  
  /** "CASEINSENSITIVE" */
  public static final String CASEINSENSITIVE            = "CASEINSENSITIVE";
  
  /** <br><em>Purpose:</em>      static map wich cross references file numbers (keys)
   ** to actual files (values in the map)
   ** <br><em>Initialized:</em>  here
   **/
  private static Map fileMap = new HashMap();
  private static Map fileWriterMap = new HashMap();
  
  private static int DEFAULT_BUFFER_SIZE = 1024 * 10;
  
  
  //for deleteDirectoryContents
  private boolean deleteFailure = false;
  //for copyMatchingFiles
  private boolean copyMatchingFilesFound = false;
  private boolean copyFailure = false;
  

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverFileCommands () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is the driver command processor for file commands.
   ** Current commands :<br>
   ** CloseFile, CopyFile, CreateDirectory, CreateFile, DeleteDirectory,
   ** DeleteFile, GetFileDateTime, GetFileSize, GetFiles, OpenFile,
   ** PrintToFile, ReadFileChars, ReadFileLine, ReadFileString, RenameFile
   ** IfExistDir, IfExistFile, WriteFileChars,
   ** GetFileProtections, SetFileProtections, IsEndOfFile, GetINIFileValue,
   ** FilterImage
   ** <br>
   ** <br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
    try {    	
      if (testRecordData.getCommand().equalsIgnoreCase(CLOSEFILE)) {
        closeFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(OPENFILE)    ||
                 testRecordData.getCommand().equalsIgnoreCase(OPENUTF8FILE)||
   				 testRecordData.getCommand().equalsIgnoreCase(CREATEFILE)) {
        openFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(READFILELINE)) {
        readFileLine(false);
      } else if (testRecordData.getCommand().equalsIgnoreCase(READFILECHARS)) {
        readFileLine(true);
      } else if (testRecordData.getCommand().equalsIgnoreCase(PRINTTOFILE)) {
        printToFile(false);
      } else if (testRecordData.getCommand().equalsIgnoreCase(WRITEFILECHARS)) {
        printToFile(true);
      } else if (testRecordData.getCommand().equalsIgnoreCase(COPYFILE)) {
        copyFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(COPYMATCHINGFILES)) {
        copyMatchingFiles();  
      } else if (testRecordData.getCommand().equalsIgnoreCase(RENAMEFILE)) {
        renameFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(CREATEDIRECTORY)) {
        createDirectory();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DELETEDIRECTORY)) {
        deleteDirectory();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DELETEDIRECTORYCONTENTS)) {
        deleteDirectoryContents();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DELETEFILE)) {
        deleteFile();
      } else if ((testRecordData.getCommand().equalsIgnoreCase(FINDSUBSTRINGINFILE))||
                 (testRecordData.getCommand().equalsIgnoreCase(GETSUBSTRINGSINFILE))){
        findSubstrings();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFILESIZE)) {
        getFileAttr(true);
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFILEDATETIME)) {
        getFileAttr(false);
      } else if (testRecordData.getCommand().equalsIgnoreCase(IFEXISTDIR)) {
        ifExist(false);
      } else if (testRecordData.getCommand().equalsIgnoreCase(IFEXISTFILE)) {
        ifExist(true);
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFILES)) {
        getFiles();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFILEPROTECTIONS)) {
        getFileProtections();
      } else if (testRecordData.getCommand().equalsIgnoreCase(SETFILEPROTECTIONS)) {
        setFileProtections();
      } else if (testRecordData.getCommand().equalsIgnoreCase(ISENDOFFILE)) {
        isEndOfFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(FILTERTEXTFILE)) {
        filterTextFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(FINDSQAFILE)) {
        findSqaFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETINIFILEVALUE)) {
      	GetINIFileValue();
      } else if (testRecordData.getCommand().equalsIgnoreCase(FILTERIMAGE)) {
      	FilterImage();
      } else if(testRecordData.getCommand().equalsIgnoreCase(ONFILEEOFGOTOBLOCKID)){
      	onFileEOFGotoBlockID();
      } else if(testRecordData.getCommand().equalsIgnoreCase(GETTEXTFROMIMAGE) ||
    		    testRecordData.getCommand().equalsIgnoreCase(SAVETEXTFROMIMAGE)){
    	GetSaveTextFromImage();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETSTRINGCOUNTINFILE)) {
        	countStringInFile();
      } else {
         setRecordProcessed(false);
      }
    } catch (SAFSException ex) {
      //ex.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /**
   * Deduce a full project-relative path from the relative path provided.
   * We do not actually attempt to see if the directory or file exists.
   * @param relativePath any string that can be considered a non-absolulte file path.
   * @return relativePath with PROJECT directory prefix.
   * @throws SAFSException if SAFS PROJECT DIRECTORY cannot be deduced.
   * issueActionOnXFailure is generated prior to throwing the Exception. 
   */
  private String deduceRelativeProjectPath(String relativePath) throws SAFSException {
    String projdir = null;
    try{ projdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);}
	catch(SAFSException x){
		String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
				STAFHelper.SAFS_VAR_PROJECTDIRECTORY +" was not found", 
				STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
		this.issueActionOnXFailure(relativePath, error);
		throw new SAFSException(STAFHelper.SAFS_VAR_PROJECTDIRECTORY +" was not found");
	}
	return FileUtilities.normalizeFileSeparators(projdir + relativePath);
  }
  
  
  /**
   * Deduce a full project-relative or datapool-relative path from the relative path provided.
   * If the relative path already contains any path separators then create a fullpath relative 
   * to the project.  If no path separators exist then create a fullpath relative to the project's 
   * Datapool\Test directory.  We do not actually attempt to see if the directory or file 
   * exists.
   * @param relativePath any string that can be considered a non-absolulte file path.
   * @return relativePath with PROJECT directory prefix or DATAPOOL Test prefix.
   * @throws SAFSException if PROJECTDIR or DATAPOOLDIR cannot be deduced.
   * issueActionOnXFailure is generated prior to throwing the Exception. 
   */
  private String deduceFullFilePath(String relativePath) throws SAFSException {
    String fullpath = null;
    relativePath = FileUtilities.normalizeFileSeparators(relativePath);
	if( relativePath.indexOf(File.separator) > -1 ) {
 		// if the separator is contained within filename, we have a relative
		// path which means relative to the project path
		try{ return this.deduceRelativeProjectPath(relativePath);}
		catch(SAFSException x){ throw x; }
	} else {
		// no pathSeparator means we have a bare file so use project Datapool/Test dir
		String datapooldir = null;
		try{ datapooldir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);}
		catch(SAFSException x){
			String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
					STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY +" was not found", 
					STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
			this.issueActionOnXFailure(relativePath, error);
			throw new SAFSException(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY +" was not found");
		}
		
		fullpath = datapooldir + "Test" + File.separator + relativePath ;
	}
	return FileUtilities.normalizeFileSeparators(fullpath);
  }
  
  /** <br><em>Purpose:</em> get file protections
   **/
  private void getFileProtections () throws SAFSException {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("FileName, VarName");
        return;
    }
    Log.info(".............................params: "+params);
    
    // get the params
    Iterator<?> iterator = params.iterator();
    String filename = (String) iterator.next();
    filename = FileUtilities.normalizeFileSeparators(filename);
    String varName = (String) iterator.next();

    if(filename.length()==0){
    	issueParameterValueFailure("FileName");
    	return;
    }
    if(varName.indexOf('^')==0){
    	try{
    		varName = varName.substring(1);
    	}catch(Exception x){
        	issueParameterValueFailure("VarName");
        	return;
    	}
    }
    if(varName.length()==0){
    	issueParameterValueFailure("VarName");
    	return;
    }
    
    try{
    	File file = new CaseInsensitiveFile(filename).toFile();
    	String attr = FileUtilities.getFileAttribute(file).getStringValue();
    	if (setVariable(varName, attr)) {
    		testRecordData.setStatusCode(StatusCodes.OK);
    		log.logMessage(testRecordData.getFac(),
    						genericText.convert(TXT_SUCCESS_2,
    								testRecordData.getCommand()+" "+ filename +" successful.",
    								testRecordData.getCommand(), filename),
    						GENStrings.convert(GENStrings.VARASSIGNED2, 
    								"Value '"+ attr +"' was assigned to variable '"+ varName +"'", 
    								attr, varName),
    						GENERIC_MESSAGE);
    	}else{
    		issueErrorPerformingActionOnX(filename, 
    				FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    						"Could not set '"+ varName +"' to '"+ attr +"'", 
    						varName, attr));
    	}
    }catch(Exception e){
		issueErrorPerformingActionOnX(filename, StringUtils.debugmsg(e));
    }    
  }

  /** <br><em>Purpose:</em> get file protections
   **/
  private void setFileProtections () throws SAFSException {
	String debugmsg = StringUtils.debugmsg(false);

    if (params.size() < 2) {
    	this.issueParameterCountFailure("FileName, FileProtection");
        return;
    }
    Log.info(debugmsg+" params: "+params);

    Iterator<?> iterator = params.iterator();
    //get the params
    String filename = FileUtilities.normalizeFileSeparators((String) iterator.next());
    String prot = (String) iterator.next();

    if(filename.length()==0){
    	issueParameterValueFailure("FileName");
    	return;
    }

    try{
    	File file = new CaseInsensitiveFile(filename).toFile();
    	FileAttribute attribute = FileAttribute.instance(StringUtilities.convertToInteger(prot).intValue());
    
    	FileUtilities.setFileAttribute(attribute, file);
    	testRecordData.setStatusCode(StatusCodes.OK);
        log.logMessage(testRecordData.getFac(),
                		genericText.convert(TXT_SUCCESS_2,
                        testRecordData.getCommand()+" "+ filename +" successful.",
                        testRecordData.getCommand(), filename),
                        GENERIC_MESSAGE);
    }catch(SAFSException e){
      if(SAFSException.CODE_ERROR_SET_FILE_ATTR.equals(e.getCode())) 
    	  issueErrorPerformingActionOnX(filename, e.getMessage());
      else
    	  throw e;
    }catch(Exception e){
    	throw new SAFSException(StringUtils.debugmsg(e));
    }
  }

  /** <br><em>Purpose:</em> findSqaFile.  Locate the specified sqa file from either absolute path or relative to datapool and store absolute path in specified variable
   * @author (Jack Imbriani) Dec 22, 2004
   **/
  private void findSqaFile() throws SAFSException {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("FileName, FilePathVariable");
        return;
    }
    Iterator iterator = params.iterator();
    // get the params
    String filename = (String) iterator.next();
    filename = FileUtilities.normalizeFileSeparators(filename);
    if(filename.length()==0){
    	this.issueParameterValueFailure("FileName="+ filename);
    	return;
    }
    String varName = (String) iterator.next();
	if(varName.indexOf('^')==0){
		try{ varName = varName.substring(1);}
		catch(Exception iob){
			this.issueParameterValueFailure("FilePathVariable="+ varName);
			return;
		}
	}
    if(varName.length()==0){
    	this.issueParameterValueFailure("FilePathVariable="+ varName);
    	return;
    }
	File file = new CaseInsensitiveFile(filename).toFile() ;
	String fullFilePath = filename;
	File testFile = null;
	if( file.isAbsolute() ) {
		// absolute path, so can use it directly
 		testFile = new CaseInsensitiveFile(fullFilePath).toFile() ;
 	} else {
 		String datapooldir = null;
		// file is relative to Datapool directory
 		try{
			datapooldir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
 		}catch(SAFSException x){
 			IndependantLog.error(StringUtils.debugmsg(false)+" Failed to get datapool directory, due to "+StringUtils.debugmsg(x));
 		}
 		if(datapooldir==null || datapooldir.length()==0){
 			this.issueActionOnXFailure(fullFilePath, FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
 					STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY +" was not found.",
 					STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY));
 			return;
 		}
 		
 		testFile = new CaseInsensitiveFile(datapooldir, filename).toFile() ;
 		fullFilePath = datapooldir + filename;
	}

	if( ! testFile.exists() ) {
		
	  // logging a warning message because that is what the RobotClassic message is
	  // and trying to make the messages as compatible as possible
	  testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
   	  log.logMessage(testRecordData.getFac(),
   			  FAILStrings.convert(FAILStrings.FAILURE_2, 
   					  "Unable to perform "+ testRecordData.getCommand()+" on "+ fullFilePath, 
   					  fullFilePath, testRecordData.getCommand()),
              FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
            		  fullFilePath +" was not found", fullFilePath),
              WARNING_MESSAGE);
      return;
	}
	//strip leading ^ if present
    if (!setVariable(varName, testFile.getAbsolutePath())) {
    	this.issueErrorPerformingActionOnX(fullFilePath, 
    		 FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    				 "Could not set variable '"+ varName +"' to value '"+ testFile.getAbsolutePath(), 
    				 varName, testFile.getAbsolutePath()));
        return;
    }
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_2,
                           testRecordData.getCommand(),
                           testRecordData.getCommand(), testFile.getAbsolutePath()),
                   GENStrings.convert(GENStrings.VARASSIGNED2, 
                		   "Value '"+ testFile.getAbsolutePath()+"' was assigned to variable '"+ varName +"'", 
                		   testFile.getAbsolutePath(), varName),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }

  /** <br><em>Purpose:</em> filterTextFile.  Filter the contents of a text file
   * @author (Jack Imbriani) Dec 17, 2004
   * <br>	OCT 18, 2013	(Lei Wang)    Handle file encoding.
   **/
  private void filterTextFile() throws SAFSException {
	boolean caseSensitive = true;
    if (params.size() < 1) {
      this.issueParameterCountFailure("File, FilterMode, FilterOptions");
      return;
    }

    Iterator iterator = params.iterator();
    String filename     = (String) iterator.next();  // need to fail on 0 length
    if (filename.length()<1) {
    	issueParameterValueFailure("FILE");
    	return;
    }

	// next parameter is the filter mode and is required
	String mode = "Default";
	if(iterator.hasNext()) mode = (String)iterator.next();
	if (mode.length()==0) mode = "Default";
	
	// mode should be RegExp, Default, or empty (implies Default)
	if( ! (mode.equalsIgnoreCase("RegExp") || mode.equalsIgnoreCase("Default")) ) {
    	issueParameterValueFailure("FilterMode");
    	return;
	}

	// Default mode explicitly given.  does no filtering, so just return
	if( mode.equalsIgnoreCase("Default") ) {
	    log.logMessage(testRecordData.getFac(), 
                genericText.convert(TXT_SUCCESS_2, 
                testRecordData.getCommand()+" "+ filename +" successul.", 
                testRecordData.getCommand(), filename),
                GENERIC_MESSAGE);
	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		return;
	}

	// next parameter is the pattern and is required
	if(! iterator.hasNext()){
		this.issueParameterCountFailure("Pattern");
		return;
	}
	String regexp = (String)iterator.next();
	if( regexp.length() == 0 ) {
    	issueParameterValueFailure("Pattern");
		return ;  // empty pattern, so nothing to do
	}

	// next parameter is the replacement string and is optional
	String replacement = "" ;
	if ( iterator.hasNext() ) replacement = (String)iterator.next();

	// last parameter is the case sensitivity and is optional (default is CaseSensitive)
	String strCaseSensitive = "";
	if ( iterator.hasNext() ) strCaseSensitive = (String)iterator.next();
	if(strCaseSensitive.equalsIgnoreCase(CASEINSENSITIVE)) {
		caseSensitive = false ;
	}

	// at this point we have filename, mode, regexp, replacement, and caseSensitive
	// to specify how to filter the file

	// file may be a full path, relative path, or just a file name
    //   full path is used as is
	//   relative path is relative to the project path
	//   file name implies to use project Datapool/Test

	File file = new CaseInsensitiveFile(filename).toFile() ;
	String fullFilePath = null ;
	if( file.isAbsolute() ) {
 		fullFilePath = filename ;
 	} else {
 		try{ fullFilePath = this.deduceFullFilePath(filename);}
 		catch(SAFSException x){return;}// errors and status already generated
	}

	// file must exist and we must have read/write access in order to filter it
	File fullfile = new CaseInsensitiveFile(fullFilePath).toFile() ;
	if( ! fullfile.exists() )  {
		String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
				       fullFilePath +" was not found", fullFilePath);
		this.issueActionOnXFailure(fullFilePath, error);
		return ;
	}
	if( ! fullfile.canRead() ) {
		String error = FAILStrings.convert(FAILStrings.FILE_READ_DENIED, 
				"Denied read permissions for file '"+ fullFilePath+"'.", fullFilePath);
		this.issueActionOnXFailure(fullFilePath, error);
		return ;
	}
	if( ! fullfile.canWrite() ) {
		String error = FAILStrings.convert(FAILStrings.FILE_WRITE_DENIED, 
				"Denied write permissions for file '"+ fullFilePath+"'.", fullFilePath);
		this.issueActionOnXFailure(fullFilePath, error);
		return ;
	}
	
	String encoding = null;
	String fileContents = null;
	if ( iterator.hasNext() ){
		encoding = (String)iterator.next();
		Log.debug("User provides file encoding as '"+encoding+"'");
		//If user-provided-encoding is not supported by java.nio.charset.Charset, set encoding to null
		//so that FileUtilities.detectFileEncoding() will detect the encoding from the file.
		try{ if(!Charset.isSupported(encoding)) encoding=null;}catch(Exception e){ encoding=null; }
		if(encoding==null) Log.warn("User provided file encoding is not valid.");
	}
	
	if(encoding==null){
		encoding = FileUtilities.detectFileEncoding(fullFilePath);
		Log.debug("We detect the file encoding is '"+encoding+"'");
	}
	
	try {
		fileContents = FileUtilities.readStringFromEncodingFile(fullFilePath, encoding);
	} catch (IOException e) {
		String error = FAILStrings.convert(FAILStrings.FILE_READ_ERROR,
				                           "Error reading from file '"+ fullFilePath+"'.", fullFilePath);
		this.issueErrorPerformingActionOnX(fullFilePath, error);
		return;
	}
	
	Pattern patt =null ;
	if( caseSensitive ) {
		patt = Pattern.compile(regexp) ;
  	} else {
		patt = Pattern.compile(regexp,Pattern.CASE_INSENSITIVE) ;
  	}
	Matcher matcher = patt.matcher(fileContents) ;
	String filtered = matcher.replaceAll(replacement) ;
	try {
		FileUtilities.writeStringToFile(fullFilePath, encoding, filtered);
	} catch (IOException e) {
		String error = FAILStrings.convert(FAILStrings.FILE_WRITE_ERROR, 
				"Error writing to file '"+ fullFilePath+"'.", fullFilePath);
		this.issueErrorPerformingActionOnX(fullFilePath, error);
		return ;
	}
	
    log.logMessage(testRecordData.getFac(), 
            genericText.convert(TXT_SUCCESS_2, 
            testRecordData.getCommand()+" "+ fullFilePath +" successul.", 
            testRecordData.getCommand(), fullFilePath),
            GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
  }
  
  /** <br><em>Purpose:</em> countStringInFile.  Count the occurrences of string in file.
   * <br> GetStringCountInFile, File, String, [VarName], [CaseInSensitive], [Encoding]
   * @author (Carl Nagle) May 25, 2011
   **/
  private void countStringInFile() throws SAFSException {
	boolean caseSensitive = true;
    if (params.size() < 2) {
      this.issueParameterCountFailure("File, String");
      return;
    }

    Iterator iterator = params.iterator();
    String filename     = (String) iterator.next();  // need to fail on 0 length
    if (filename.length()<1) {
    	issueParameterValueFailure("FILE");
    	return;
    }

    String pattern = (String) iterator.next();  // need to fail on 0 length
    if (pattern.length()<1) {
    	issueParameterValueFailure("String");
    	return;
    }

    // next parameter is the optional varname. Defaults to "GetStringCountInFile"
	String varname = "";
	if(iterator.hasNext()) varname = (String)iterator.next();
	if (varname.length()==0) varname = "GetStringCountInFile";
    if (varname.charAt(0)=='^'){ varname = varname.substring(1);}
	
	String docase = "";
	if(iterator.hasNext()) docase = (String)iterator.next();
	if (docase.equalsIgnoreCase(CASEINSENSITIVE)) caseSensitive = false;
	
	String testpattern = caseSensitive ? pattern: pattern.toUpperCase();

	boolean encoded = false;
	String encoding = "";
	if(iterator.hasNext()) encoding = (String)iterator.next();
	if (encoding.length() > 1) encoded = true;

	// at this point we have filename, testpattern, varname, caseSensitive, encoded

	// file may be a full path, relative path, or just a file name
    //   full path is used as is
	//   relative path is relative to the project path
	//   file name implies to use project Datapool/Test
	File file = new CaseInsensitiveFile(filename).toFile() ;
	String fullFilePath = null ;
	if( file.isAbsolute() ) {
 		fullFilePath = filename ;
 	} else {
 		try{ fullFilePath = this.deduceFullFilePath(filename);}
 		catch(SAFSException x){return;}// errors and status already generated
	}

	// file must exist and we must have read/write access in order to filter it
	File fullfile = new CaseInsensitiveFile(fullFilePath).toFile() ;
	if( ! fullfile.exists() )  {
		String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
				       fullFilePath +" was not found", fullFilePath);
		this.issueActionOnXFailure(fullFilePath, error);
		return ;
	}
	if( ! fullfile.canRead() ) {
		String error = FAILStrings.convert(FAILStrings.FILE_READ_DENIED, 
				"Denied read permissions for file '"+ fullFilePath+"'.", fullFilePath);
		this.issueActionOnXFailure(fullFilePath, error);
		return ;
	}

	Log.info("DCDFC.countStringsInFile processing file '"+fullfile.getAbsolutePath()+"', for substring ["+ testpattern +"], case-sensitive="+caseSensitive);
	int counter = 0;
	int lines = 0;
	int index = 0;
	String text = null; 
	try{
		setVariable(varname, "0");//throws SAFSException
		FileLineReader fp = encoded ? new FileLineReader(fullfile, encoding): new FileLineReader(fullfile);
		while(! fp.isEOF()){
			text = fp.readLine();
			if(text != null) lines++;
			if (text != null && text.length()> 0){
				if (!caseSensitive) text = text.toUpperCase();
				index = 0;
				do{
					index = text.indexOf(testpattern, index);
					if (index > -1){
						counter++;
						Log.info("DCDFC.countStringsInFile found match "+ counter +" for testpattern at index "+ index +" on line "+lines);
						index += testpattern.length();
					}
				}while(index > -1 && index < text.length());
			}
		}
		try{ fp.close();}catch(Exception x){}finally{fp=null;file=null;fullfile=null;}
		String strcount = String.valueOf(counter);
		Log.info("DCDFC.countStringsInFile setting variable '"+ varname +"' to "+ strcount +".");
		setVariable(varname, strcount);
		String msg = genericText.convert(TXT_SUCCESS_2, 
	            testRecordData.getCommand()+" "+ fullFilePath +" successul.", 
	            testRecordData.getCommand(), fullFilePath);
		String detail = genericText.convert("varAssigned2",
				"Value '"+ strcount +"' was assigned to variable '"+ varname +"'",
				strcount,varname);
	    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE, detail); 
	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	}
	catch(Exception x){
		Log.error("DCDFC.countStringsInFile "+x.getClass().getSimpleName()+", "+ x.getMessage());
    	this.issueUnknownErrorFailure(x.getClass().getSimpleName()+":"+ x.getMessage());
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	}
  }

  /** <br><em>Purpose:</em> FindSubstringsInFile
   * @author (Carl Nagle) Jan 13, 2004 Fixed to ignore leading caret on rootvar parameter.
   **/
  private void findSubstrings () throws SAFSException {
    if (params.size() <= 3) {
      issueParameterCountFailure();
      return;
    }
    Iterator iterator = params.iterator();
    String filename     = (String) iterator.next();  // need to fail on 0 length
    if (filename.length()<1) {
    	issueParameterValueFailure("FILE");
    	return;
    }
    String regexStart = (String) iterator.next();
    if (regexStart.length()<1) {
    	issueParameterValueFailure("REGEXStart");
    	return;
    }
    String regexStop  = (String) iterator.next();
    if (regexStop.length()<1) {
    	issueParameterValueFailure("REGEXStop");
    	return;
    }
    String rootvar    = (String) iterator.next();  // need to fail on 0 length
    if (rootvar.length()<1) {
    	issueParameterValueFailure("ROOTNAME");
    	return;
    }
    if (rootvar.charAt(0)=='^'){ rootvar = rootvar.substring(1);}
    Log.info(".............................params: "+params);
    FileProcessor fp = new FileProcessor();
    String[] substrings = fp.getEmbeddedSubstrings(filename, regexStart, regexStop);
    int status = fp.returnStatus();
    Log.info(".................substrings: "+substrings);
    Log.info(".....................status: "+status);
    switch(status){
    	// continue
    	case 0:
    	    break;
    	// exit with unknown error
    	case 6:
    	    String regex = GENStrings.text(GENStrings.REGULAR_EXPRESSIONS,
    	                   "Regular Expressions");
    	    String error = FAILStrings.convert(FAILStrings.SUPPORT_NOT_INSTALLED,
    	                   regex+" support may not be installed!",
    	                   regex);
    	    issueUnknownErrorFailure(error);
    	    return;
    	// exit with File Error
    	default:
    	    issueFileErrorFailure(filename);
    	    return;
    }
    // get substrings succeeded
    String value  = null;
    String scount = null;
    String variable = null;
    if(substrings.length > 0){
    	scount = String.valueOf(substrings.length).trim();
    	for(int i = 0; i < substrings.length;i++){
    		value = substrings[i];
    		variable = rootvar + String.valueOf(i+1).trim();
    		setVariable(variable, value);
    	}
    }
    else{
    	scount = "0";
    }
    variable = rootvar +"Count";
    setVariable(variable, scount);
    String comment = variable +" = "+getVariable(variable);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> getFiles
   **/
  private void getFiles () throws SAFSException {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("Directory, FileName, [FileAttributes]");
        return;
    }
    Log.info(".............................params: "+params);
    Iterator iterator = params.iterator();
    // get the params
    String dirname = (String) iterator.next();
    dirname = FileUtilities.normalizeFileSeparators(dirname);
    if(!StringUtils.isValid(dirname)){
    	this.issueParameterValueFailure("directory");
    	return;
    }
    
    String outputFilename = (String) iterator.next();
    outputFilename = FileUtilities.normalizeFileSeparators(outputFilename);
    if(!StringUtils.isValid(outputFilename)){
    	this.issueParameterValueFailure("outputFileName");
    	return;
    }
    
    FileAttribute expectedAttribute = FileAttribute.instance();
    if(iterator.hasNext()){
        expectedAttribute.add(StringUtilities.convertToInteger((String) iterator.next()).intValue());
    }
    
    File adir = new CaseInsensitiveFile(dirname).toFile();
    if (adir != null && adir.isDirectory()) {
      Writer writer = null;
      try {
    	File out =  new CaseInsensitiveFile(outputFilename).toFile();
    	outputFilename = out.getCanonicalPath();
    	  // !!! CONCERNED THAT OUTPUT IS HARDCODED FOR UTF-8 !!!
        writer =  FileUtilities.getUTF8BufferedFileWriter(outputFilename);
        if (writer != null) {
          //If volume label is set, all other attributes code will be ignored.
          if(expectedAttribute.containsVolumeLabel()){
        	  String volumeLabel = null;
        	  File[] roots = File.listRoots();
        	  for(int i=0;i<roots.length;i++){
        		  if(adir.getAbsolutePath().toLowerCase().startsWith(roots[i].getAbsolutePath().toLowerCase())){
        			  volumeLabel = FileSystemView.getFileSystemView().getSystemDisplayName(roots[i]);
        			  writer.write(volumeLabel+"\n");
        			  break;
        		  }
        	  }
          }else{
        	  File[] flist = adir.listFiles(new FileAttributeFilter(expectedAttribute));
        	  for(int i=0; i<flist.length; i++) {
        		  writer.write(flist[i].getAbsolutePath()+"\n");
        	  }
          }
          log.logMessage(testRecordData.getFac(), GENStrings.convert(GENStrings.PERFNODE4A,
                         testRecordData.getCommand()+" performed on "+ dirname +"; output file '"+ outputFilename+"'",
                         testRecordData.getCommand(), dirname, outputFilename ),                         
                         GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else {
        	this.issueErrorPerformingActionOnX(dirname, 
        			FAILStrings.convert(FAILStrings.FILE_WRITE_ERROR, 
        					"Error writing to file '"+ outputFilename +"'", 
        					outputFilename));
        	return;
        }
      } catch (IOException ioe) {
    	  this.issueErrorPerformingActionOnX(dirname, ioe.getClass().getSimpleName()+" "+ ioe.getMessage());
      } finally {
        try { if (writer!= null) writer.close(); } catch (IOException ioe2) { }
      }
    } else {
    	this.issueParameterValueFailure("Directory="+ dirname);
    }
  }

  /** <br><em>Purpose:</em> ifExistFile and ifExistDir
   ** @param fileVsDir  if true, then test for a file, else test for a directory
   **/
  private void ifExist (boolean fileVsDir) throws SAFSException {
    String fd = (fileVsDir?"file":"directory");
    if (params.size() <= 1) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
                     ": wrong params, the first param is supposed to be the "+ fd +
                     " name, then then another file driver command.",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    // get the params
    String oldCommand = testRecordData.getCommand();
    Log.info("  ..............."+oldCommand+
           "\n  ............... params: "+params);
    String fdname = (String) iterator.next();
    String newCommand = (String) iterator.next();

    Collection newParams = new ArrayList();
    for(; iterator.hasNext(); ) {
      newParams.add(iterator.next());
    }

    File afd = new CaseInsensitiveFile(fdname).toFile();
    if (afd != null &&
        ((fileVsDir && afd.isFile()) ||
         (!fileVsDir && afd.isDirectory()))) {
      testRecordData.setCommand(newCommand);
      params.clear();
      params.addAll(newParams);
      log.logMessage(testRecordData.getFac(), oldCommand + ": "+fd+" exists: "+
                     fdname+", EXECUTING : "+newCommand+" for: "+newParams,
                     GENERIC_MESSAGE);
      process();
    } else {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+
                     oldCommand + ": "+fd+" does not exist or is not a normal "+
                     fd+" : "+fdname+", NOT EXECUTING: "+newCommand+" for: "+newParams,
                     GENERIC_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> getFileSize and getFileDateTime
   ** @param sizeVsDateTime  if true, then getFileSize, else getFileDateTime
   **/
  private void getFileAttr (boolean sizeVsDateTime) throws SAFSException {
	String debugmsg = StringUtils.debugmsg(false);
    String paramVarName = sizeVsDateTime ? "FileSizeVariable":"FileDateVariable";
    if (params.size() < 2) {
    	this.issueParameterCountFailure("FileName, "+ paramVarName);
        return;
    }
    Iterator iterator = params.iterator();
    // get the params
    String filename = (String) iterator.next();
    filename = FileUtilities.normalizeFileSeparators(filename);
    if(filename.length()==0){
    	this.issueParameterValueFailure("FileName="+ filename);
    	return;
    }
    String varName = (String) iterator.next();
    try{
    	if(varName.indexOf('^')==0) varName = varName.substring(1);
    }catch(Exception iob){
    	this.issueParameterValueFailure(paramVarName+"="+ varName);
    	return;
    }
    if(varName.length()==0){
    	this.issueParameterValueFailure(paramVarName+"="+ varName);
    	return;
    }
    String dateType = DateType.LASTMODIFIED.name;
    boolean isMilitary = false;
    if(!sizeVsDateTime){//GetFileDateTime
    	if(iterator.hasNext()) isMilitary = StringUtilities.convertBool(iterator.next());
    	if(iterator.hasNext()) dateType = (String)iterator.next();
    }

    Log.info(".............................params: "+params);

    String fullPath = filename;
    File afile = new CaseInsensitiveFile(filename).toFile();
    if (afile != null) {
      if(! afile.isAbsolute()){
    	  String datadir = null;
    	  try{
    		  datadir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
    	  }catch(SAFSException x){
    		  IndependantLog.error(StringUtils.debugmsg(false)+" Failed to get datapool directory, due to "+StringUtils.debugmsg(x));
    	  }
    	  if (datadir==null || datadir.length()==0){
    		  this.issueActionOnXFailure(filename, 
    				  FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    						  STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY +" was not found", 
    						  STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY));
    		  return;
    	  }
    	  afile = new CaseInsensitiveFile(datadir, filename).toFile();
    	  fullPath = datadir + filename;
      }
      if(! afile.exists()){
    	  this.issueActionOnXFailure(fullPath, FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    					  fullPath +" was not found", fullPath));
    	  return;
      }
      String attr = null;
      // GetFileSize
      if (sizeVsDateTime) {
        long len = afile.length();
        attr = Long.toString(len);
      }
      //GetFileDateTime
      else {
    	Date date = null;
        
        if(Console.isWindowsOS()){
        	Date createTime = new Date();
        	Date accessTime = new Date();
        	Date writeTime = new Date();
        	if(!NativeWrapper.getFileTime(afile.getAbsolutePath(), createTime, accessTime, writeTime)){
        		IndependantLog.warn(debugmsg+"Fail to get date thru Native method.");
        	}
        	
        	if(DateType.CREATED.name.equalsIgnoreCase(dateType)){
        		date = createTime;
        	}else if(DateType.LASTACCESSED.name.equalsIgnoreCase(dateType)){
        		date = accessTime;
        	}else{
        		//LastModified date time
//        		date = writeTime;
        		date = new Date(afile.lastModified());
        	}
        	
        }else{
        	IndependantLog.warn(debugmsg+" For OS '"+Console.getOsFamilyName()+"', only LastModified time is supported.");
        	date = new Date(afile.lastModified());
        }

        attr = StringUtilities.getDateTimeString(date, isMilitary);
      }
      if (!setVariable(varName, attr)) {
    	  this.issueErrorPerformingActionOnX(fullPath, FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    			  "Could not set '"+ varName +"' to '"+ attr +"'", varName, attr));
          return;
      }
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2,
                             testRecordData.getCommand() +" "+ fullPath +" successful.",
                             testRecordData.getCommand(), fullPath),
                     GENStrings.convert(GENStrings.VARASSIGNED2, 
                    		 "Value '"+ attr +"' was assigned to variable '"+ varName +"'", 
                    		 attr, varName),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    } else {
    	this.issueParameterValueFailure("FileName="+ filename);
    }
  }

  /** <br><em>Purpose:</em> deleteDirectory:   Delete the directory for the directory name provided
   **/
  private void deleteDirectory () {
    if (params.size() < 1) {
    	this.issueParameterCountFailure("DirectoryName");
    	return;
    }
    Iterator iterator = params.iterator();
    Log.info(".............................params: "+params);
    String dirname = (String) iterator.next();
    dirname = FileUtilities.normalizeFileSeparators(dirname);
    
    File adir = new CaseInsensitiveFile(dirname).toFile();
    if (adir != null) {
      if(! adir.isAbsolute()){
    	this.issueParameterValueFailure("DirectoryName="+ dirname);
    	return;
      }
      if( ! adir.exists()){
    	  this.issueGenericSuccess(
    			  GENStrings.convert(GENStrings.NOT_EXIST, 
    					  dirname +" does not exist.", dirname));
    	  return;
      }
      if (!adir.isDirectory()) {
    	  this.issueActionOnXFailure(dirname, 
    			  FAILStrings.convert(FAILStrings.NOT_A_DIRECTORY, 
    					  dirname +" is not a directory", dirname));
    	  return;
      } else if (adir.delete()) {
    	  this.issueGenericSuccess(dirname);
    	  return;
      } else {
    	  this.issueActionOnXFailure(dirname, 
    			  FAILStrings.convert(FAILStrings.CANT_DELETE_DIRECTORY, 
    					  "Can not delete directory '"+ dirname +"'", dirname));
    	  return;
      }
    } else {
    	this.issueParameterValueFailure("DirectoryName="+dirname);
    }
  }

  /** <br><em>Purpose:</em> deleteSubDirectoryContents:  Recursive helper method for
   *                  deleteDirectoryContents().
   * 
   * @author (Bob Lawler) Nov 07, 2006 RJL
   **/
  private void deleteSubDirectoryContents (File f, boolean delSubDir) {
  	File[] fileList = f.listFiles();
  	// for all Files in f...
  	for (int i = 0; i < fileList.length; i++) {
  		if (fileList[i].isDirectory())
  			// delete subdirectory contents and subdirectory itself
  			deleteSubDirectoryContents(fileList[i], true);
  		else
  			// delete single file
  			if (!fileList[i].delete()) {
  	  	        log.logMessage(testRecordData.getFac(), 
  	  	        		FAILStrings.convert(FAILStrings.CANT_DELETE_FILE, 
  									"Can not delete file "+ fileList[i].getAbsolutePath(), 
  									fileList[i].getAbsolutePath()), 
  	  	        		GENERIC_MESSAGE);
  	  				deleteFailure = true;
  			}
  	}
  	// delete subdirectory?
  	if (delSubDir){
  		int deltry = 0;
  		for(; !f.delete() && deltry < 5; deltry++){
  			try{Thread.sleep(1000);}catch(Exception x){}
  		}
  		if (deltry == 5 && f.exists()&& f.isDirectory()) {
  	        log.logMessage(testRecordData.getFac(), 
  	        		FAILStrings.convert(FAILStrings.CANT_DELETE_DIRECTORY, 
								"Can not delete directory "+ f.getAbsolutePath(), 
								f.getAbsolutePath()), 
  	        		GENERIC_MESSAGE);
  				deleteFailure = true;
  		}
  	}
  }
  
  /** <br><em>Purpose:</em> deleteDirectoryContents:  Delete the contents of directory
   *                  and, optionally, the directory itself.  The directory to delete is
   *                  either absolute or relative to the SAFS Project Directory.
   * 
   * @author (Bob Lawler) Nov 07, 2006 RJL
   **/
  private void deleteDirectoryContents () {
  	  // initialize...
  	  deleteFailure = false;
  	  File adir;
  	  
  	  // validate params
      if (params.size() < 1) {
    	  this.issueParameterCountFailure("DirectoryName");
          return;
      }
      
      // get dirname
      Iterator iterator = params.iterator();
      Log.info(".............................params: " + params);
      String dirname = (String) iterator.next();
      dirname = FileUtilities.normalizeFileSeparators(dirname);
      if((dirname==null)||(dirname.length()< 1)){
    	  this.issueParameterValueFailure("DirectoryName="+ dirname);
    	  return;
      }
      try {
      	adir = new CaseInsensitiveFile(dirname).toFile();
      	// if provided dirname is not absolute, it must be relative to SAFS Project Directory
      	if (!adir.isAbsolute()) {
      		try{
      			String pdir = this.deduceRelativeProjectPath(dirname);
        		adir = new CaseInsensitiveFile(pdir).toFile();
    		}catch(SAFSException x){ return;} //error already generated.
      	}
      }
      catch (Exception e) {
    	  this.issueErrorPerformingActionOnX(dirname, 
    			  e.getClass().getSimpleName()+":"+e.getMessage());
          return;
      }
      
      // get optional parameter delDir (true or anything else for false)
      boolean delDir = false;
      if(iterator.hasNext()) delDir = StringUtilities.convertBool(iterator.next());
      Log.info(".............................delDir: " + delDir);

	  // only process a directory
      if (!adir.isDirectory()) {
    	  this.issueActionOnXFailure(adir.getAbsolutePath(), 
    			  FAILStrings.convert(FAILStrings.NOT_A_DIRECTORY, 
    					  adir.getAbsolutePath() +" is not a directory", 
    					  adir.getAbsolutePath()));
          return;
      }
      else {
      	// initialize status code to OK
  	    testRecordData.setStatusCode(StatusCodes.OK);
      	try {
      		// delete all Files in adir and if requested, adir too!
      		deleteSubDirectoryContents(adir, delDir);
	  	}
	  	catch (Exception e) {
	    	this.issueErrorPerformingActionOnX(dirname, 
	    	     e.getClass().getSimpleName()+":"+e.getMessage());
	        return;
    	}
      }

      // issue driver command completed, including test record details if any failures!   
	  if (deleteFailure) {
		  this.issueFileErrorFailure(adir.getAbsolutePath());
		  return;
	  }
	  this.issueGenericSuccess(adir.getAbsolutePath());
  }

  /** <br><em>Purpose:</em> actuallyCopyBytes:  helper method for copyFile() and 
   *                  copySubDirectoryFiles().
   * 
   *  This method was taken directly from the old way copyFile() used to copy
   *  byte-by-byte.  Now that the function has been pulled out, copyFile() can 
   *  also be updated accordingly.
   * 
   * @author (Bob Lawler) Nov 07, 2006 RJL
   * @author (Carl Nagle) Apr 09, 2007 Convert to byte-by-byte for NLS

   *   **/
  private void actuallyCopyBytes (File sourceFile, File targetFile)
  				throws IOException {
    // do the work
    InputStream reader = null;
    OutputStream writer = null;
	try{ reader = new BufferedInputStream(new FileInputStream(sourceFile), DEFAULT_BUFFER_SIZE);}
	catch(Exception x){
		try{reader.close();}catch(Exception c){;}
		String error = FAILStrings.convert(FAILStrings.FILE_READ_ERROR, 
				"Error reading from file '"+ sourceFile.getAbsolutePath()+"'", 
				sourceFile.getAbsolutePath());
		this.issueErrorPerformingActionOnX(sourceFile.getAbsolutePath(), error);
		throw new IOException(x.getMessage()+ error);
	}
	try{ writer = new BufferedOutputStream(new FileOutputStream(targetFile), DEFAULT_BUFFER_SIZE);}
	catch(Exception x){
		try{reader.close();}catch(Exception c){;}
		try{writer.close();}catch(Exception c){;}
		String error = FAILStrings.convert(FAILStrings.FILE_WRITE_ERROR, 
				"Error writing to file '"+ targetFile.getAbsolutePath()+"'", 
				targetFile.getAbsolutePath());
		this.issueErrorPerformingActionOnX(targetFile.getAbsolutePath(), error);
		throw new IOException(x.getMessage() + error);
	}
	if (writer != null && reader != null) {
		try{
    		byte[] cbuf = new byte[5];
    		for(int i=0; (i=reader.read(cbuf)) >= 0; ) {
    			if (i>0) writer.write(cbuf, 0, i);
    		}
    		Log.info("................copied " + sourceFile.getAbsolutePath() + " to " +
        		     targetFile.getAbsolutePath());
			try{reader.close();}catch(Exception c){;}
			try{writer.close();}catch(Exception c){;}
			targetFile.setLastModified(sourceFile.lastModified());
		}catch(Exception io){
			try{reader.close();}catch(Exception c){;}
			try{writer.close();}catch(Exception c){;}
			String error = FAILStrings.convert(FAILStrings.FILE_ERROR, 
					"Error opening or reading or writing file '"+ io.getClass().getSimpleName()+"'", 
					io.getClass().getSimpleName());
			this.issueErrorPerformingAction(error);
			throw new IOException(error);
		}
	} 
	else {
		try{reader.close();}catch(Exception c){;}
		try{writer.close();}catch(Exception c){;}
		String error = "";
		if (writer==null) error = FAILStrings.convert(FAILStrings.FILE_WRITE_DENIED, 
				"Denied write permissions for file '"+ targetFile.getAbsolutePath()+"'", 
				targetFile.getAbsolutePath());
		if (reader==null) error += FAILStrings.convert(FAILStrings.FILE_READ_DENIED, 
				"Denied read permissions for file '"+ sourceFile.getAbsolutePath()+"'", 
				sourceFile.getAbsolutePath());
		this.issueErrorPerformingAction(error);
		throw new IOException(error);
	}
  }
  
  /** <br><em>Purpose:</em> copySubDirectoryFiles:  Recursive helper method for
   *                  copyMatchingFiles().
   * 
   * @author (Bob Lawler) Nov 07, 2006 RJL
   **/
  private void copySubDirectoryFiles (File source, File target, FilenameFilter filter) {
  	File[] fileList = source.listFiles(filter);
  	// for all Files in source...
  	for (int i = 0; i < fileList.length; i++) {
  		// we must have found at least one match, so update global static var!
  		copyMatchingFilesFound = true;
  		String newTargetString = target.getAbsolutePath() + File.separator + fileList[i].getName();
  		File newTargetFile = new CaseInsensitiveFile(newTargetString).toFile();
  		if (newTargetFile == null) {
			String error = FAILStrings.convert(FAILStrings.BAD_PARAM, 
					"Invalid parameter value for "+newTargetString, newTargetString);
			this.issueErrorPerformingActionOnX(newTargetString, error);
			copyFailure = true;
			}
  		
  		else if (fileList[i].isDirectory()) {
  			// first, copy subdirectory itself...,
  			Log.info("................attempting copy of dir " + newTargetString);
  			if (!newTargetFile.mkdirs()) {
  				String error = FAILStrings.convert(FAILStrings.CANT_CREATE_DIRECTORY, 
  						"Can not create directory "+newTargetString, newTargetString);
  				this.issueErrorPerformingActionOnX(newTargetString, error);
  				copyFailure = true;
  			}
  			// ...then subdirectory contents
  			copySubDirectoryFiles(fileList[i], newTargetFile, null);
  		}
  		else {
  			// copy single file
  			Log.info("................attempting copy of " + newTargetString);
  		    try {
  		    	actuallyCopyBytes(fileList[i], newTargetFile);
  		    }
  		    catch (Exception e) {
  		    	// since we already logged the io failure details, just update status code and
  		    	// remember that we had at least one failure.
  		    	testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
  		    	copyFailure = true;
  		    }
  		}
  	}
  }
  
  /** <br><em>Purpose:</em> copyMatchingFiles:  Copy multiple files, based on matching the
   *                  provided pattern, from the source directory into the target directory.
   *                  Source and target directories are either absolute or relative to the
   *                  SAFS Project Directory.
   * 
   *  Supported FilterModes:
   *         - WILDCARD (default), "?" and "*"
   *         - REGEXP, regular expression patterns
   * 
   * @author (Bob Lawler) Nov 07, 2006 RJL
   **/
  private void copyMatchingFiles () {
  	  // initialize...
  	  copyMatchingFilesFound = false;
      copyFailure = false;
  	  File sourcedir;
  	  File targetdir;
  	  
  	  // validate params
      if (params.size() < 3) {
    	  this.issueParameterCountFailure("FromDirectoryName, ToDirectoryName, Pattern, FilterMode");
          return;
      }
      iterator = params.iterator();
      Log.info(".............................params: " + params);
     
      // get source dirname
      String sourcedirname = (String) iterator.next();
      sourcedirname = FileUtilities.normalizeFileSeparators(sourcedirname);
      Log.info(".............................sourcedirname: " + sourcedirname);
      sourcedir = new CaseInsensitiveFile(sourcedirname).toFile();
      try {
      	sourcedir = new CaseInsensitiveFile(sourcedirname).toFile();
      	// if provided sourcedirname is not absolute, it must be relative to SAFS Project Directory
      	if (!sourcedir.isAbsolute()) {
      		String pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
    		if (pdir == null) pdir="";
    		sourcedir = new CaseInsensitiveFile(pdir, sourcedirname).toFile();
      	}
      }
      catch (Exception e) {
    	  String error = e.getClass().getSimpleName()+":"+ e.getMessage();
    	  this.issueErrorPerformingActionOnX(sourcedirname, error);
          return;
      }
      
      // get target dirname
      String targetdirname = (String) iterator.next();
      targetdirname = FileUtilities.normalizeFileSeparators(targetdirname);
      Log.info(".............................targetdirname: " + targetdirname);
      try {
      	targetdir = new CaseInsensitiveFile(targetdirname).toFile();
      	// if provided targetdirname is not absolute, it must be relative to SAFS Project Directory
      	if (!targetdir.isAbsolute()) {
      		String pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
    		if (pdir == null) pdir="";
    		targetdir = new CaseInsensitiveFile(pdir, targetdirname).toFile();
      	}
      }
      catch (Exception e) {
    	  String error = e.getClass().getSimpleName()+":"+ e.getMessage();
    	  this.issueErrorPerformingActionOnX(targetdirname, error);
          return;
      }
      
      // ensure we have valid directories
      if (!sourcedir.isDirectory()) {
    	  String error = FAILStrings.convert(FAILStrings.BAD_PARAM, 
    			  "Invalid parameter value for "+ sourcedir.getAbsolutePath(), 
    			  sourcedir.getAbsolutePath());
    	  this.issueErrorPerformingActionOnX(sourcedir.getAbsolutePath(), error);
          return;
      }
      else if (!targetdir.isDirectory()) {
    	  String error = FAILStrings.convert(FAILStrings.BAD_PARAM, 
    			  "Invalid parameter value for "+ targetdir.getAbsolutePath(), 
    			  targetdir.getAbsolutePath());
    	  this.issueErrorPerformingActionOnX(targetdir.getAbsolutePath(), error);
          return;
      }

      // get file pattern
      String filepattern = (String) iterator.next();
      Log.info("..................user supplied filepattern: " + filepattern);
      if ((filepattern == "") || (filepattern == null)) {
    	  this.issueParameterValueFailure("Pattern");
          return;
      }
      // this Inner class is used to build the FilenameFilter based on the
      // user supplied pattern (String.matches() uses RegExpr pattern matching)
      class DynamicFilenameFilter implements FilenameFilter {
        private String pattern;
        DynamicFilenameFilter(String filter) {
        	pattern = filter;
        }
        public boolean accept(File dir, String name) {
        	return name.matches(pattern);
        }
      	public void setfilepattern (String newpattern){
      	    pattern = newpattern;
      	    }
      	public String getfilepattern (){
      	    return pattern;
      	    }
      }
      DynamicFilenameFilter fileFilter = new DynamicFilenameFilter(filepattern);
      
      // get optional filterMode (default is "WILDCARD")
      String filterMode = "WILDCARD";
      if(iterator.hasNext()) filterMode = (String) iterator.next();
	  
	  // for supporting format like "C,CopyMatchingFiles,sourcedir,targDir,regex,"
	  // using default filterMode instead if there is nothing in field filterMode. See S0547177. 
	  if (filterMode.length() == 0)  
		  filterMode = "WILDCARD";
	  Log.info(".............................filterMode: " + filterMode);
	  // since our DynamicFilenameFilter is based on RegExpr pattern matching, we will
	  // need to update the previously created fileFilter when using standard wildcards
	  if (filterMode.equalsIgnoreCase("WILDCARD")) {
	  	// currently, we only support the standard ? and * wildcards
	  	fileFilter.setfilepattern(fileFilter.getfilepattern().replaceAll("\\.", "\\\\.")); 
	  	fileFilter.setfilepattern(fileFilter.getfilepattern().replaceAll("\\?", ".{1}"));
	  	fileFilter.setfilepattern(fileFilter.getfilepattern().replaceAll("\\*", ".*"));
	  }
	  else if (filterMode.equalsIgnoreCase("REGEXP")) {
	  	// no update to fileFilter needed
	  }
	  else {
		  this.issueParameterValueFailure("FilterMode");
          return;
	  }
	  Log.info("......................filefilterpattern: " + fileFilter.getfilepattern());
	  
      try {
      	//// copy all Files in sourcedir that match the specified input pattern
      	//log.logMessage(testRecordData.getFac(), testRecordData.getCommand() +
		//	           " from '" + sourcedir.getAbsolutePath() + "' to '" + 
		//		       targetdir.getAbsolutePath() + "' using '" +
		//		       filepattern +"' " + filterMode + " matching started.", 
		//		       GENERIC_MESSAGE);
		copySubDirectoryFiles(sourcedir, targetdir, fileFilter);
	  }
	  catch (Exception e) {
		  String error = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
				  "*** ERROR *** "+e.getClass().getSimpleName()+":"+e.getMessage(), 
				  e.getClass().getSimpleName()+":"+e.getMessage());
		  this.issueErrorPerformingAction(error);
		  return;
	  }

	  // log results!
	  if (!copyMatchingFilesFound) {
	  	// warn that no matches were found!  
	  	testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
	  	String warning = GENStrings.convert(GENStrings.NO_FILE_MATCHES, 
	  			"No files or directories found matching filter '"+ filepattern +"'", 
	  			filepattern);
		log.logMessage(testRecordData.getFac(), 
				testRecordData.getCommand() +":"+ warning, WARNING_MESSAGE);
	  	return;
	  }
	  else {
	  	// issue driver command completed, including test record details if any failures!   
	  	if (copyFailure) {
		  	String error = GENStrings.convert(GENStrings.NO_FILE_MATCHES, 
		  			"No files or directories found matching filter '"+ filepattern +"'", 
		  			filepattern);
		  	this.issueErrorPerformingAction(error);
		  	return;
	  	}
	    testRecordData.setStatusCode(StatusCodes.OK);
	    String msg = GENStrings.convert(GENStrings.PERFNODE4A, 
	    		testRecordData.getCommand()+" performed on "+ sourcedir.getAbsolutePath() +
	    		"; output file '"+ targetdir.getAbsolutePath() +"'.",
	    		testRecordData.getCommand(), sourcedir.getAbsolutePath(), targetdir.getAbsolutePath());
		log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
	  }
  }

  /** <br><em>Purpose:</em> deleteFile:   Delete the file for the file name provided
   **/
  private void deleteFile () {
    if (params.size() < 1) {
    	this.issueParameterCountFailure("FileName");
        return;
    }
    Iterator iterator = params.iterator();
    Log.info(".............................params: "+params);
    String filename = (String) iterator.next();
    filename = FileUtilities.normalizeFileSeparators(filename);
    if((filename == null)||(filename.length()==0)){
    	this.issueParameterValueFailure("FileName");
    	return;
    }
    String snoverify = "";
    if(iterator.hasNext()) snoverify = (String)iterator.next();
    boolean verify = ! snoverify.equalsIgnoreCase("NOVERIFY");
    
    File afile = new CaseInsensitiveFile(filename).toFile();
	String fullFilePath = null ;
	if( afile.isAbsolute() ) {
 		fullFilePath = filename ;
 	} else {
 		try{ 
 			fullFilePath = this.deduceRelativeProjectPath(filename);}
 		catch(SAFSException x){
 			return;
 		}
	}
	afile = new CaseInsensitiveFile(fullFilePath).toFile();
    try {
    	if( fileWriterMap.containsValue(afile.getCanonicalPath())) {
    		// can not have same file opened for writing more than once, so return with ERROR
    		String error = FAILStrings.convert(FAILStrings.FILE_OPEN_ERROR, 
    				"File '"+ afile.getCanonicalPath() +"' is OPEN for read/write operations.",
    				afile.getCanonicalPath());
    		this.issueActionOnXFailure(afile.getCanonicalPath(), error);
    		return;
    	}
    } catch (IOException ioe ) {
    	this.issueActionOnXFailure(fullFilePath, ioe.getClass().getSimpleName());
    	return;
    }
    if (afile != null) {
      boolean deleted = afile.delete();
      if (deleted) {
  	    log.logMessage(testRecordData.getFac(), 
                genericText.convert(TXT_SUCCESS_2, 
                testRecordData.getCommand()+" "+ fullFilePath +" successul.", 
                testRecordData.getCommand(), fullFilePath),
                GENERIC_MESSAGE);
	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
      } else if(verify) {
      	String error = FAILStrings.convert(FAILStrings.CANT_DELETE_FILE, 
    			"Can not delete file "+ fullFilePath, fullFilePath);
    	this.issueErrorPerformingActionOnX(fullFilePath, error);
      }else{
    	    log.logMessage(testRecordData.getFac(), 
                    genericText.convert(TXT_SUCCESS_2, 
                    testRecordData.getCommand()+" "+ fullFilePath +" successul.", 
                    testRecordData.getCommand(), fullFilePath),
                    GENERIC_MESSAGE);
    	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
      }
    } else if(verify) {
    	String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    			fullFilePath +" was not found", fullFilePath);
    	this.issueErrorPerformingActionOnX(fullFilePath, error);
    }else{
	    log.logMessage(testRecordData.getFac(), 
                genericText.convert(TXT_SUCCESS_2, 
                testRecordData.getCommand()+" "+ fullFilePath +" successul.", 
                testRecordData.getCommand(), fullFilePath),
                GENERIC_MESSAGE);
	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    }
  }

  /** <br><em>Purpose:</em> createDirectory:  Create the directory for the provided directory pathname
   **/
  private void createDirectory () {
    if (params.size() < 1) {
    	this.issueParameterCountFailure("DirectoryName");
        return;
    }
    Iterator iterator = params.iterator();
    Log.info(".............................params: "+params);
    String dirname = (String) iterator.next();
    dirname = FileUtilities.normalizeFileSeparators(dirname);
    
    File dir = new CaseInsensitiveFile(dirname).toFile();
    if (dir != null) {
      if(!dir.isAbsolute()){
    	  this.issueParameterValueFailure("DirectoryName="+dirname);
    	  return;
      }
      if(dir.exists() && dir.isDirectory()){
    	  this.issueGenericSuccess(
    			  GENStrings.convert(GENStrings.EXISTS, 
    					  dirname +" exists.", dirname));
    	  return;
      }
      if (dir.mkdirs()) {
    	  String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
    			  testRecordData.getCommand()+" "+ dir.getAbsolutePath()+"  successful.", 
    			  testRecordData.getCommand(), dir.getAbsolutePath());
        log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } else {
    	  String error = FAILStrings.convert(FAILStrings.CANT_CREATE_DIRECTORY, 
    			  "Can't create directory "+ dirname, dirname);
    	  this.issueErrorPerformingActionOnX(dirname, error);
      }
    } else {
    	this.issueParameterValueFailure("DirectoryName="+dirname);
    }
  }

  /** <br><em>Purpose:</em> renameFile:  Rename the file from the old file name to the new filename
   ** The first param (from 'params') is the file path and file name for the file to be renamed.
   ** The second param (from 'params') is the file path and file name to rename it to.
   **/
  private void renameFile () {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("OldFileName, NewFileName");
        return;
    }
    Iterator iterator = params.iterator();
    // get the filename
    Log.info(".............................params: "+params);
    String oldFilename = (String) iterator.next();
    String newFilename = (String) iterator.next();
    String sverify = "";
    if(iterator.hasNext()) sverify = (String) iterator.next();
    boolean verify = sverify.equalsIgnoreCase("NOVERIFY");
    
    File oldFile = new CaseInsensitiveFile(oldFilename).toFile();
    File newFile = new CaseInsensitiveFile(newFilename).toFile();
    if (oldFile != null && newFile != null) {
      if ((oldFile.renameTo(newFile))||!verify) {
    	String msg = GENStrings.convert(GENStrings.PERFNODE4A, 
    			testRecordData.getCommand() +" performed on "+ oldFile.getAbsolutePath() +
    			"; output file '"+ newFile.getAbsolutePath() +"'.", 
    			testRecordData.getCommand(), oldFile.getAbsolutePath(), newFile.getAbsolutePath());
        log.logMessage(testRecordData.getFac(),msg,GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } else {
    	  String error = null;
    	  if(oldFile!=null){
	      	  if(!oldFile.exists()){
	    		  error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
	    				  oldFile.getAbsolutePath() +" was not found", 
	    	              oldFile.getAbsolutePath());
	      	  }else{
	      		  error = FAILStrings.convert(FAILStrings.FILE_ERROR, 
	      				"Error opening or reading or writing file '"+ oldFile.getAbsolutePath() +"'", 
	      				oldFile.getAbsolutePath());
	      	  }
    	  }else{
    		  error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    				  oldFilename +" was not found", 
    	              oldFilename);
    	  }
    	  this.issueErrorPerformingActionOnX(oldFilename, error);
      }
    } else if(verify){
    	String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    			"Could not set '"+ oldFilename +"' to '"+ newFilename +"'", 
    			oldFilename, newFilename);
  	    this.issueErrorPerformingAction(error);
    }else{
    	String msg = GENStrings.convert(GENStrings.PERFNODE4A, 
    			testRecordData.getCommand() +" performed on "+ oldFilename +
    			"; output file '"+ newFilename +"'.", 
    			testRecordData.getCommand(), oldFilename, newFilename);
        log.logMessage(testRecordData.getFac(),msg,GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
    }
  }

  /** <br><em>Purpose:</em> copyFile: Copy the file of the filename provided to the defined
   **                 filename.
   *
   *  The first param (from 'params') is the file path and file name for the file to be copied.
   *  The second param (from 'params') is the file path and file name to copy the file to.
   *
   *  History:
   *  Nov 07, 2006    (Bob Lawler) Updated to use new actuallyCopyBytes() helper method. RJL
   * 
   **/
  private void copyFile () {
    
    //validate params
    if (params.size() < 2) {
    	this.issueParameterCountFailure("FromFileName, ToFileName");
        return;
    }
    Iterator iterator = params.iterator();
    Log.info(".............................params: " + params);
   
    // get source filename
    String sourcefilename = (String) iterator.next();
    sourcefilename = FileUtilities.normalizeFileSeparators(sourcefilename);
    Log.info(".............................sourcefilename: " + sourcefilename);
    File sourcefile = new CaseInsensitiveFile(sourcefilename).toFile();
	String fullSourceFilePath = null ;
	if( sourcefile.isAbsolute() ) {
 		fullSourceFilePath = sourcefilename ;
 	} else {
 		try{ fullSourceFilePath = deduceFullFilePath(sourcefilename);}
 		catch(SAFSException x){return;} //handles issung failure message and status
	}
    sourcefile = new CaseInsensitiveFile(fullSourceFilePath).toFile();
    if (sourcefile == null) {
    	this.issueParameterValueFailure("FromFileName");
    	return;
    }

    // get target filename
    String targetfilename = (String) iterator.next();
    targetfilename = FileUtilities.normalizeFileSeparators(targetfilename);
    Log.info(".............................targetfilename: " + targetfilename);
    File targetfile = new CaseInsensitiveFile(targetfilename).toFile();
	String fullTargetFilePath = null ;
	if( targetfile.isAbsolute() ) {
 		fullTargetFilePath = targetfilename ;
 	} else {
 		try{ fullTargetFilePath = deduceFullFilePath(targetfilename);}
 		catch(SAFSException x){return;} //handles issung failure message and status
	}
    targetfile = new CaseInsensitiveFile(fullTargetFilePath).toFile();    
    if (targetfile == null) {
    	this.issueParameterValueFailure("ToFileName");
    	return;
    }
    if(fullSourceFilePath.equalsIgnoreCase(fullTargetFilePath)){
    	this.issueActionOnXFailure("FromFileName", "FromFileName == ToFileName");
    	return;
    }
    
    // attempt file copy
    try { actuallyCopyBytes(sourcefile, targetfile);}
    catch (Exception e) {
    	// since we've already logged the failure details, just set status code,
    	// summarize, and return
    	testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
    	return;
    }
    
    // issue driver command completed!
    String msg = GENStrings.convert(GENStrings.PERFNODE4A, 
    		testRecordData.getCommand()+" performed on "+ fullSourceFilePath +"; output file '"+fullTargetFilePath+"'.", 
    		testRecordData.getCommand(), fullSourceFilePath, fullTargetFilePath);
    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }

  /** <br><em>Purpose:</em> PRINTTOFILE:  Write output to a sequential file opened for writing
   ** @param chars  if true, then command is WRITEFILECHARS
   **/
  private void printToFile (boolean chars) {
	  if (chars && params.size() < 3) { // WRITEFILECHARS
		  this.issueParameterCountFailure("FileNumber, NumberOfChars, ExpressionListVariable");
		  return;
	  }else if (params.size() < 2){ // PRINTTOFILE
		  this.issueParameterCountFailure("FileNumber, ExpressionListVariable");
		  return;
	  }
	  Iterator iterator = params.iterator();
	  // get the params
	  String filenum = (String) iterator.next();

	  Integer numchars = null;
	  String numcharsStr = null;
	  String expressionList = null;
	  String placement = null;
	  boolean allchars = true;
	  expressionList = (String) iterator.next(); //printtofile
	  if (chars) { //writefilechars   	
		  numcharsStr = expressionList;
		  expressionList = (String) iterator.next();
		  allchars = numcharsStr.equalsIgnoreCase("ALL");
		  if (!allchars) { // parse valid number of chars
			  try {
				  numchars = Integer.valueOf(numcharsStr);
				  allchars = (numchars<0);
			  } catch (NumberFormatException nfe) {
				  this.issueParameterValueFailure("NumberOfChars");
				  return;
			  }
		  }
	  }else{
		  //outputplacement can be
		  //";" immediate,  placement=null 
		  //"" newline, placement="\n"
		  //"," next print zone is considered as Tabulation, placement="\t"
		  if(iterator.hasNext()){
			  placement = (String)iterator.next();
			  if(placement.equals(";")){
				  placement = null;
			  }else if(placement.equals(",")){
				  placement = "\t";
			  }else if(placement.equals("")){
				  placement = "\n";
			  }else{
				  this.issueParameterValueFailure("PrintOutputPlacement="+ placement);
				  return;
			  }
		  }else{
			  placement = "\n";// PRINTTOFILE append newline by default
		  }    	

	  }
	  try {
		  Log.info(".............................params: "+params);

		  if(expressionList.charAt(0)=='^'){
			  Log.info("DCDFC attempting variable lookup for:"+ expressionList);
			  String tstr = null;
			  try{
				  tstr = getVariable(expressionList.substring(1));
				  expressionList = tstr;
				  Log.info("DCDFC substituting variable value:"+ expressionList);
			  }catch(Exception x){
				  Log.info("DCDFC lookup of '"+ expressionList +"' unsuccessful.  Using as-is.");
			  }
		  }
		  // now do the work
		  Object rw = fileMap.get(filenum);
		  if(rw instanceof Writer){
			  Writer writer = (Writer) rw;
			  if(writer != null){
				  if(chars){//WRITEFILECHARS
					  if(allchars){ 
						  writer.write(expressionList);
					  }else{
						  writer.write(expressionList, 0, numchars.intValue());
					  }	            
				  }else{//PRINTTOFILE
					  if(placement!=null) writer.write(placement);
					  writer.write(expressionList);
					  writer.flush();
				  }
				  log.logMessage(testRecordData.getFac(),
						  GENStrings.convert(GENStrings.SUCCESS_2, 
								  testRecordData.getCommand()+" "+ expressionList +" successful.",
								  testRecordData.getCommand(), expressionList),
								  GENERIC_MESSAGE);
				  testRecordData.setStatusCode(StatusCodes.OK);
				  return;
			  } 
			  else{ //writer == null
				  this.issueParameterValueFailure("FileNumber");
				  return;
			  }
			  // NOT instanceof Writer
		  } else if (rw instanceof Reader) {
			  String error = GENStrings.convert(GENStrings.ITEM_OPEN_READ, 
					  "FileNumber "+ filenum +" is open for READ operations.", 
					  "FileNumber "+ filenum);
			  this.issueErrorPerformingActionOnX("FileNumber "+ filenum, error);
			  return;
		  } else {
			  this.issueParameterValueFailure("FileNumber");
			  return;
		  }
	  } catch (IOException ioe) {
		  this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
		  testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
	  }
  }

  /** <br><em>Purpose:</em> "ReadFileLine" or "ReadFileString" or "ReadFileChars"
   ** @param chars  if true, then read the number of chars specified by second param from 'params'
   **/
  private void readFileLine (boolean chars) throws SAFSException {

    if (chars && params.size() < 3) { // READFILECHARS
    	this.issueParameterCountFailure("FileNumber, NumberOfChars, VariableName");
    	return;
    }else if (params.size() < 2){ // READFILELINE
    	this.issueParameterCountFailure("FileNumber, VariableName");
    	return;
    }
    Iterator iterator = params.iterator();
    // get the params
    String filenum = (String) iterator.next();
    String varname = (String) iterator.next();
    String numcharsStr = null;
    int numchars = FileUtilities.PARAM_ALL_CHARACTERS;
    if (chars) {
      numcharsStr = varname;
      varname = (String) iterator.next();
      try {
        numchars = Integer.parseInt(numcharsStr);
      } catch (NumberFormatException nfe) {
    	  this.issueParameterValueFailure("NumberOfChars");
    	  return;
      }
    }
    Log.info(".............................params: "+params);
    // now do the work
    try {
      Object rw = fileMap.get(filenum);
      if (rw instanceof BufferedReader) {
        BufferedReader reader = (BufferedReader) rw;
        if (reader != null) {
          String line = null;
          if (chars) {
        	if(FileUtilities.PARAM_ALL_CHARACTERS==numchars){
        		line = FileUtilities.readStringFromFile(reader);        		
        		Log.debug("The whole file has been read.");
        	}else{
        		int numRead = 0;
        		char [] buf = new char[numchars];
        		numRead = reader.read(buf, 0, numchars);        		
        		Log.debug("'"+numRead+"' chars have been read.");
        		line = new String(buf);
        	}
          }else{
            line = reader.readLine();
                       
            if(line != null){
            	byte[] bytes = new byte[0];
            	try{ bytes = line.getBytes(UTF8_ENCODING);}catch(Exception ignore){} 
            	// byte[] bytes = line.getBytes(); // might need to try this instead
            	if(bytes.length > 2 && 
            		UTF8_MARKER[0]==bytes[0] && UTF8_MARKER[1]==bytes[1] && UTF8_MARKER[2]==bytes[2]){
            		Log.info("DCDFC.readFileLine has detected possible UTF-8 leader bytes and is attempting to remove them.");
            		if(bytes.length == 3){
                		Log.info("DCDFC.readFileLine contained ONLY UTF-8 leader bytes.");
            			line = "";
            		}else{
            			byte[] newbytes = java.util.Arrays.copyOfRange(bytes, 3, bytes.length);
            			try{ line = new String(newbytes, UTF8_ENCODING);}catch(Exception ignore){
                    		Log.info("DCDFC.readFileLine was NOT successful in removing possible UTF-8 leader bytes.");
            			}
            		}
            	}
            }            
          }
          if (!setVariable(varname, line)) {
        	  String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
        			  "Could not set '"+ varname +"' to '"+ line +"'", 
        			  varname, line);
              this.issueErrorPerformingActionOnX(filenum, error);
              return;
          }
          log.logMessage(testRecordData.getFac(),
                  GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ line +" successful.",
                  testRecordData.getCommand(), line),
                  GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
          return;
        } 
        else { // reader == null
        	this.issueParameterValueFailure("FileNum="+filenum);
        	return;
        }
      } else if (rw instanceof Writer) {
    	  String error = GENStrings.convert(GENStrings.ITEM_OPEN_WRITE, 
    			  "FileNumber "+ filenum +" is open for WRITE operations.", 
    			  "FileNumber "+ filenum);
    	  this.issueErrorPerformingActionOnX("FileNumber "+ filenum, error);
    	  return;
      } else {
      	  this.issueParameterValueFailure("FileNum="+filenum);
    	  return;
      }
    } catch (IOException ioe) {
    	this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
    }
  }

  /** <br><em>Purpose:</em> openFile
   **/
  private void openFile () throws SAFSException {
    if (params.size() < 4) {
    	this.issueParameterCountFailure("Filename, Mode, Access, FileNumberVariable");
        return;
    }
    Iterator iterator = params.iterator();
    // get the filename
    String filename = (String) iterator.next();
    Log.info(".............................params: "+params);
    String inOutApp = (String) iterator.next();
    String readWrite = (String) iterator.next();
    String varName = (String) iterator.next();
    String filenum = null;
    if (iterator.hasNext()) {
      filenum = (String) iterator.next();
    }

    // now do the work
    try {
	    // we only allow one writer for each file and do not want to allow reading
    	// of a file that is open for writing.  check to see if file is
	  	// already open for writing and ERROR if it is
	  	if( fileWriterMap.containsValue(new CaseInsensitiveFile(filename).toFile().getCanonicalPath())) {
	  		// can not have same file opened for writing more than once, so return with ERROR
	  		String error = FAILStrings.convert(FAILStrings.FILE_OPEN_ERROR, 
	  				"File '"+ filename +"' is OPEN for read/write operations.", 
	  				filename);
	  		issueActionOnXFailure(filename, error);
	  		return;
	  	}
      if (readWrite.equalsIgnoreCase("read") && inOutApp.equalsIgnoreCase("input")) {
      	Reader reader = null;
      	// need to better handle the attempt to read a non-existent file
      	if (testRecordData.getCommand().equalsIgnoreCase(OPENUTF8FILE)){
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new CaseInsensitiveFile(filename).toFile()), Charset.forName("UTF-8")), DEFAULT_BUFFER_SIZE);
		}else{
			reader = new BufferedReader(new FileReader(new CaseInsensitiveFile(filename).toFile()));
		}
        if (reader != null) {
          filenum = getNextFilenum(filenum, fileMap);
          if (!setVariable(varName, filenum)) {
        	  String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
        			  "Could not set '"+ varName +"' to '"+ filenum +"'", 
        			  varName, filenum);
              this.issueErrorPerformingActionOnX(filename, error);
              return;
          }
          fileMap.put(filenum, reader);
          String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ filename +" successful.",
                  testRecordData.getCommand(), filename);
          msg +=" "+ GENStrings.convert(GENStrings.VARASSIGNED2,
        		  "Value '"+ filenum +"' was assigned to variable '"+ varName +"'.",
        		  filenum, varName);
          log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
          return;
        } else {
      	  String error = FAILStrings.convert(FAILStrings.FILE_READ_DENIED, 
    			  "Denied read permissions for file '"+ filename +"'", 
    			  filename);
          this.issueErrorPerformingActionOnX(filename, error);
          return;
        }
      } else if (readWrite.equalsIgnoreCase("write") &&
                 (inOutApp.equalsIgnoreCase("output") || 
                  inOutApp.equalsIgnoreCase("append") || 
                  inOutApp.equalsIgnoreCase("appendraw"))) {
      	Collection prePart = null;
      	Writer writer = null;
        if (inOutApp.equalsIgnoreCase("append")) {
			if (testRecordData.getCommand().equalsIgnoreCase(OPENUTF8FILE)){
				prePart = StringUtils.readUTF8file(filename);
			}else{			
          		prePart = StringUtils.readfile(filename);
			}
        }else if (inOutApp.equalsIgnoreCase("appendraw")) {
			prePart = new ArrayList();
			if (testRecordData.getCommand().equalsIgnoreCase(OPENUTF8FILE)){
				prePart.add(StringUtils.readRawUTF8File(filename).toString());
			}else{			
          		prePart.add(StringUtils.readRawFile(filename).toString());
			}
        }
        // need to handle creation of NEW file that does not exist
		if (testRecordData.getCommand().equalsIgnoreCase(OPENUTF8FILE)){
	        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(filename).toFile()), Charset.forName("UTF-8")), DEFAULT_BUFFER_SIZE);
		}else{		
			writer = new BufferedWriter(new FileWriter(new CaseInsensitiveFile(filename).toFile()));
		}
        if (writer != null) {
          if (inOutApp.equalsIgnoreCase("appendraw")) {
            for(Iterator i= prePart.iterator(); i.hasNext(); ) {
              String s = i.next().toString();
              writer.write(s);
            }
          }else if (inOutApp.equalsIgnoreCase("append")) {
            for(Iterator i= prePart.iterator(); i.hasNext(); ) {
              String s = i.next().toString() + "\n";
              writer.write(s);
            }
          }
          filenum = getNextFilenum(filenum, fileMap);
          if (!setVariable(varName, filenum)) {
        	  String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
        			  "Could not set '"+ varName +"' to '"+ filenum +"'", 
        			  varName, filenum);
              this.issueErrorPerformingActionOnX(filename, error);
              return;
          }
          fileMap.put(filenum, writer);
          fileWriterMap.put(filenum,new CaseInsensitiveFile(filename).toFile().getCanonicalPath());
          String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ filename +" successful.",
                  testRecordData.getCommand(), filename);
          msg +=" "+ GENStrings.convert(GENStrings.VARASSIGNED2,
        		  "Value '"+ filenum +"' was assigned to variable '"+ varName +"'.",
        		  filenum, varName);
          log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
          return;
        } else {
        	  String error = FAILStrings.convert(FAILStrings.FILE_WRITE_DENIED, 
        			  "Denied write permissions for file '"+ filename +"'", 
        			  filename);
              this.issueErrorPerformingActionOnX(filename, error);
              return;
        }
      } else {
    	  this.issueParameterValueFailure("Filename="+ filename +", Mode="+inOutApp +", Access="+readWrite);
    	  return;
      }
    } catch (IOException ioe) {
    	this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
    }
  }

  /** <br><em>Purpose:</em> closeFile
   **/
  private void closeFile () {
  	if (params.size() < 1) {
  		this.issueParameterCountFailure("FileNumber");
        return;
    }
    Iterator iterator = params.iterator();
    // get the param
    String param = (String) iterator.next();

    Log.info(".............................param: "+param);
    // now do the work
    try {
      Object rw = fileMap.get(param);
      fileMap.remove(param);
      if (rw instanceof Reader) {
        Reader reader = (Reader) rw;
        if (reader != null) {
          reader.close();
          log.logMessage(testRecordData.getFac(),
                  GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ param +" successful.",
                  testRecordData.getCommand(), param),
                  GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else {
        	this.issueParameterValueFailure("FileNumber="+ param);
        }
      } else if (rw instanceof Writer) {
        fileWriterMap.remove(param);
        Writer writer = (Writer) rw;
        if (writer != null) {
          writer.flush();
          writer.close();
          log.logMessage(testRecordData.getFac(),
                  GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ param +" successful.",
                  testRecordData.getCommand(), param),
                  GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
        } else {
        	this.issueParameterValueFailure("FileNumber="+ param);
        }
      } else {
      	this.issueParameterValueFailure("FileNumber="+ param);
      }
    } catch (IOException ioe) {
    	this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
    }
  }

  /** <br><em>Purpose:</em> isEndOfFile
   **/
  private void isEndOfFile () throws SAFSException {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("FileNumber, Variable");
        return;
    }
    Iterator iterator = params.iterator();
    Log.info(".............................params: "+params);
    // get the param
    String fileNo = (String) iterator.next();
    String var = (String) iterator.next();

    Log.info("............................. file number: "+fileNo);
    // now do the work
    try {
      Object rw = fileMap.get(fileNo);
      if (rw instanceof Reader) {
        Reader reader = (Reader) rw;
        if (reader != null) {
          if (reader.markSupported()) reader.mark(10);
          Boolean j = new Boolean(reader.read() == -1);
          String eof = j.toString();
          if (reader.markSupported()) reader.reset();
          if (!setVariable(var, eof)) {
        	  String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
        			  "Could not set '"+var+"' to '"+eof+"'", 
        			  var, eof);
        	  this.issueErrorPerformingActionOnX(fileNo, error);
              return;
          }
          String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
                  testRecordData.getCommand()+" "+ fileNo +" successful.",
                  testRecordData.getCommand(), fileNo);
          msg +=" "+ GENStrings.convert(GENStrings.VARASSIGNED2,
        		  "Value '"+ eof +"' was assigned to variable '"+ var +"'.",
        		  eof, var);
          log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.OK);
          return;
        } else {
        	this.issueParameterValueFailure("FileNumber="+fileNo);
        	return;
        }
      } else if (rw instanceof Writer) {
        String eof = "true";
        if (!setVariable(var, eof)) {
      	  String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    			  "Could not set '"+var+"' to '"+eof+"'", 
    			  var, eof);
    	  this.issueErrorPerformingActionOnX(fileNo, error);
          return;
        }
        String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
                testRecordData.getCommand()+" "+ fileNo +" successful.",
                testRecordData.getCommand(), fileNo);
        msg +=" "+ GENStrings.convert(GENStrings.VARASSIGNED2,
      		  "Value '"+ eof +"' was assigned to variable '"+ var +"'.",
      		  eof, var);
        log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
        return;
      } else {
      	this.issueParameterValueFailure("FileNumber="+fileNo);
      }
    } catch (IOException ioe) {
    	this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
    }
  }
  
  private void GetINIFileValue(){
  	String value =  null;

    if (params.size() < 4) {
    	this.issueParameterCountFailure("Filename, Section, Item, Variable");
    	return;
      }
      Iterator iterator = params.iterator();
      // get the parameters
      String fileName = (String) iterator.next();
      fileName = FileUtilities.normalizeFileSeparators(fileName);
      if(fileName.length()==0){
    	  this.issueParameterValueFailure("FileName");
    	  return;
      }
      String section = (String) iterator.next();
      if(section.length()==0){
    	  this.issueParameterValueFailure("Section");
    	  return;
      }
      String item = (String) iterator.next();
      if(item.length()==0){
    	  this.issueParameterValueFailure("Item");
    	  return;
      }
      String var = (String) iterator.next();
      // support "^varname"
      try{ if (var.indexOf('^')==0) var = var.substring(1);}
      catch(Exception x){
    	  this.issueParameterValueFailure("Variable="+ var);
    	  return;
      }
      if(var.length()==0){
    	  this.issueParameterValueFailure("Variable");
    	  return;
      }
      File file = new CaseInsensitiveFile(fileName).toFile();

      // Is this file a complete path if so use it,if not default to runtime dir
      try{
	      if (!file.isAbsolute()) {
	    	  String dir = null;
	    	  if(fileName.indexOf(File.separator) == -1){ //no project relative path separator
			      dir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
			      dir = dir + File.separator +"RunTime";
	    	  }else{
			      dir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
	    	  }
		      if (dir == null) {
		    	  String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
		    			  STAFHelper.SAFS_VAR_PROJECTDIRECTORY +" was not found", 
		    			  STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
		    	  this.issueErrorPerformingActionOnX(fileName, error);
		    	  return;
		      }
		      file = new CaseInsensitiveFile(dir, fileName).toFile();
	      }
      } catch(SAFSException se){
    	  String error = FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    			  STAFHelper.SAFS_VAR_PROJECTDIRECTORY +" was not found", 
    			  STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
    	  this.issueErrorPerformingActionOnX(fileName, error);
    	  return;
      }
      String sectitem = section+":"+item;
      // *** need to consider using SAFSMAPS for this ***
	  INIFileReader inireader = new INIFileReader(file, INIFileReader.IFR_MEMORY_MODE_STORED);

      value = inireader.getItem(section, item); // returns null if not found 
      //Close file if we finish read item.
      inireader.close();
      if(value==null){
    	  //something_not_found :%1% was not found
    	  this.issueErrorPerformingActionOnX(file.getAbsolutePath(), 
    			  FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
    					  sectitem+" was not found", sectitem));
    	  return;
      }
      //Set the variable to the value
      try{
      	if(!setVariable(var, value)){
	    	String error = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
	    			  "Could not set '"+var+"' to '"+value+"'", 
	    			  var, value);
	    	this.issueErrorPerformingActionOnX(fileName, error);
      	}else{
	        String msg = GENStrings.convert(GENStrings.SUCCESS_2, 
	                testRecordData.getCommand()+" "+ sectitem +" successful.",
	                testRecordData.getCommand(), sectitem);
	        msg +=" "+ GENStrings.convert(GENStrings.VARASSIGNED2,
	      		  "Value '"+ value +"' was assigned to variable '"+ var +"'.",
	      		  value, var);
	        log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
	        testRecordData.setStatusCode(StatusCodes.OK);
      	}
      }catch(SAFSException se){
    	  this.issueErrorPerformingActionOnX(fileName, 
    			  FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
    					  "Could not set '"+ var +"' to '"+ value +"'.", 
    					  var, value));
      }
  }

	/**
	 * Method used to black out certain parts of an image using sets of coordinates,
	 * or percentage of coordinates, as rectangles to be blacked out.
	 * Example:	10,20,30,30 would make a 20 by 10 rectangle at 10,20
	 * 			10%,10%,50%,50% would make a rectangle from the 10% of image width, 10% of
	 *            image height to 50% of image width, 50% of image height.
	 * Rectangle with malformed coordinates will be thrown out, and a Warning outputted.
	 *
	 * Uses helper function getSubAreaRectangle() found in Processor superclass.
	 */
	private void FilterImage(){
		//init
	  	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	
		//Parameters are InputFilename, OutputFilename, FilterMode, and Mode Parameters (depending on mode)
		//Required parameter for COORD mode is a set of coordinates as described above.
		if(params.size() < 4){
			this.issueParameterCountFailure("InputFilename, OutputFilename, FilterMode, Coords");
	        return;
		}
	
		Iterator iterator = params.iterator();
		String InputFilename = (String) iterator.next();
		String OutputFilename = (String) iterator.next();
		String FilterMode = (String) iterator.next();
		String Coords = ((String) iterator.next()).trim();
	    
		if (InputFilename.length()< 1){
      		this.issueParameterValueFailure("InputFilename");
      		return;
      	}
		if (OutputFilename.length()< 1){
      		this.issueParameterValueFailure("OutputFilename");
      		return;
      	}		
	  	if(! FilterMode.equalsIgnoreCase("COORD")){ //Check if mode is COORD
	  		this.issueParameterValueFailure("FilterMode="+ FilterMode);
	  		return;
	  	}
		BufferedImage image = null;
		File infile = new CaseInsensitiveFile(InputFilename).toFile();
		if(! infile.exists()){
			this.issueActionOnXFailure(InputFilename, 
				FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
				InputFilename +" was not found.", InputFilename));
			return;
		}
		try {
			image = ImageUtils.getStoredImage(infile.getAbsolutePath());
		} catch (IOException ioe) {
			this.issueErrorPerformingActionOnX(InputFilename, "ImageIO "+
					FAILStrings.convert(FAILStrings.FILE_READ_DENIED,  
							"Denied read permissions for file "+ InputFilename, InputFilename));
		    return;
		} catch(NullPointerException npe){
			this.issueErrorPerformingActionOnX(InputFilename, "ImageIO "+ 
					FAILStrings.convert(FAILStrings.FILE_READ_ERROR, 
							"Error reading from file "+ InputFilename, 
							InputFilename));
		    return;
		}
		
		try {
			//Coords = getAppMapItem("", "ApplicationConstants", Coords);
			List<String> warnings = new ArrayList<String>();
			image = ImageUtils.filterImage(image, Coords, warnings);
			if(!warnings.isEmpty()) for(String warning:warnings) log.logMessage(testRecordData.getFac(), warning, WARNING_MESSAGE);
		} catch (SAFSException e1) {
			this.issueParameterValueFailure("Coords="+ Coords);
			return;
		}
		
		try{
			ImageUtils.saveImageToFile(image, new CaseInsensitiveFile(OutputFilename).toFile());
			if(! ImageUtils.isImageFormatSupported(OutputFilename)){
	      		this.issueErrorPerformingActionOnX(OutputFilename, 
	      				FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
	      						"Support for "+ OutputFilename +" not found!", 
	      						OutputFilename));
	  			return;
	      	}
		} catch (IOException e) {
			this.issueErrorPerformingActionOnX(OutputFilename, "ImageIO "+
					FAILStrings.convert(FAILStrings.FILE_WRITE_DENIED,  
							"Denied write permissions for file "+ OutputFilename, OutputFilename));
		    return;
		}catch (java.lang.SecurityException se) {
	      	//error, security problems accessing output file
			this.issueErrorPerformingActionOnX(OutputFilename, 
					FAILStrings.convert(FAILStrings.FILE_WRITE_DENIED, 
							"Denied write permissions to file "+OutputFilename, 
							OutputFilename));
	      	return;
		} catch (java.lang.IllegalArgumentException iae) {
	      	//error, bad parameters sent to JAI.create call
			this.issueErrorPerformingActionOnX(OutputFilename, "JAI: "+
					FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
							"Support for "+ OutputFilename +" not found.", OutputFilename));
	      	return;
        }catch (NoClassDefFoundError ncdfe) {
	      	//error, JAI not installed
			this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SUPPORT_NOT_INSTALLED, 
							"Support for Java Advanced Imaging (JAI) may not be properly installed.", 
							"Java Advanced Imaging (JAI)"));
	      	return;
        }
  	    testRecordData.setStatusCode(StatusCodes.OK);  	    
  	    log.logMessage(testRecordData.getFac(), 
  	    		GENStrings.convert(GENStrings.PERFNODE4A, 
  	    				testRecordData.getCommand()+" performed on "+InputFilename+"; output file '"+OutputFilename+"'.", 
  	    				testRecordData.getCommand(), InputFilename,OutputFilename),GENERIC_MESSAGE);
	    return;
	}

  /** <br><em>Purpose:</em> get next filenum (max of all the keys in the map plus one)
   * @param                     filenum, String, if not null, then just return filenum
   * @param                     map, Map
   * @return                    String value of next integer, or if param filenum was not
   * null, then just returns that
   **/
  private String getNextFilenum(String filenum, Map map) {
    if ((filenum != null)&&(filenum.length()>0)) return filenum;
    int maxnum = -1;
    for(Iterator i = map.keySet().iterator(); i.hasNext(); ) {
      try {
        String key = (String) i.next();
        Log.debug("  next key: "+key);
        Integer num = new Integer(key);
        if (maxnum < num.intValue()) maxnum = num.intValue();
      } catch (NumberFormatException nfe) {
        //ignore
      }
    }
    Log.debug("getNextFilenum: "+(1+maxnum));
    return Integer.toString(++maxnum);
  }

  /**
   * <br><em>Purpose:</em> branch to the proper block if the EOF is reached.
   * <br> If the EOF is reached, then set the statusCode to 256 and statusInfo to the BlockID
   * <br> so that the branch action will be performed in InputProcessor. InputProcessor is a 
   * <br> driver who drives the RJ Engine.
 * @throws SAFSException
   */
  private void onFileEOFGotoBlockID() throws SAFSException{
  	  String command = testRecordData.getCommand();
  	  if (params.size() < 2) {
  		 this.issueParameterCountFailure("BlockID, FileNumber");
         return;
      }
  	 
      Iterator iterator = params.iterator();
      // get the param
      String blockID = (String) iterator.next();
      String fileNumber = (String) iterator.next();

      Log.info(".............................blockID: "+blockID);
      Log.info(".............................fileNumber: "+fileNumber);
      
      // now do the work
      try {
        Object rw = fileMap.get(fileNumber);
        if (rw instanceof Reader) {
          Reader reader = (Reader) rw;
          if (reader != null) {
            if (reader.markSupported()) reader.mark(10);
            Boolean eof = new Boolean(reader.read() == -1);
            if (reader.markSupported()) reader.reset();
            Log.info("DCDFC EOF is "+eof);
            if(eof.booleanValue()){
            	Log.info("DCDFC Branch to blockID "+blockID);
                log.logMessage(testRecordData.getFac(),
                        GENStrings.convert(GENStrings.EQUALS,
                                "'"+ fileNumber +"' equals 'EOF'",
								fileNumber,"EOF")  +" "+
						GENStrings.convert(GENStrings.BRANCHING, 
								command +" attempting branch to "+ blockID, 
								command, blockID),					
                        GENERIC_MESSAGE);
            	testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
            	testRecordData.setStatusInfo(blockID);
            	return;
            }else{
            	Log.info("DCDFC File "+fileNumber+" is not at End Of File.  Branch will not be performed.");
                log.logMessage(testRecordData.getFac(),
                		genericText.text(GENStrings.EOF_NOT_REACHED,"EOF is not reached.") +" "+
                        genericText.convert(GENStrings.NOT_BRANCHING,
                                            command+ " did not branch to "+blockID+ ".",
											command,blockID),
                        GENERIC_MESSAGE);
                testRecordData.setStatusCode(StatusCodes.OK);
                return;
            }
          } else {
        	  this.issueParameterValueFailure("FileNumber="+fileNumber);
        	  return;
          }
        } else if (rw instanceof Writer) {
        	Log.info("DCDFC Branch to blockID "+blockID);
            log.logMessage(testRecordData.getFac(),
					GENStrings.convert(GENStrings.BRANCHING, 
							command +" attempting branch to "+ blockID, 
							command, blockID),					
                    GENERIC_MESSAGE);
        	testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
        	testRecordData.setStatusInfo(blockID);
        	return;
        } else {
      	  this.issueParameterValueFailure("FileNumber="+fileNumber);
    	  return;
        }
      } catch (IOException ioe) {
      	this.issueUnknownErrorFailure(ioe.getClass().getSimpleName()+":"+ ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
      }
  }
  
  /**
   * Use OCR to detect the text from image file, executing SaveTextFromImage or GetTextFromImage.
   * C, SaveTextFromImage, imagefile, outputfile [,OCR option] [,LangId] [,scaleRatio]
   * C, GetTextFromImage, imagefile, variable [,OCR] [,LangId] [,scaleRatio]
   * eg:
   * C,SaveTextFromImage,d:\ocr\samples\pie.gif,pie.txt,TOCR,en
   * 
   * @throws SAFSException
   */
  private void GetSaveTextFromImage() throws SAFSException {
	  String command = testRecordData.getCommand();
	  STAFHelper staf = testRecordData.getSTAFHelper();
	  
  	  if (params.size() < 2) {
  		 issueParameterCountFailure("imagefile, variable");
         return;
      }
      Iterator iterator = params.iterator();
      // get the params
      String imgfile    = (String) iterator.next();		      // 1st param
      // outputVar is a variable name if command is GetTextFromImage, a file name if command is SaveTextFromImage
      String outputVar  = (String) iterator.next();   		  // 2nd param
      
      String ocrId      = OCREngine.OCR_DEFAULT_ENGINE_KEY;	   // 3th optional param, setting default 
      String langId     = OCREngine.getOCRLanguageCode(staf);  // 4th optional param, setting the language defined in STAF as default
      float  scaleRatio = -1;								   // 5th optional param, setting default 
	  
      if (iterator.hasNext()) {
    	  ocrId = (String) iterator.next();  // the next if has is ocrId
    	  if(ocrId.equals("")) ocrId = OCREngine.OCR_DEFAULT_ENGINE_KEY;	
      }
      if (iterator.hasNext()) {
    	  langId = (String) iterator.next(); // the next if has is langId
    	  if(langId.equals("")) langId = OCREngine.getOCRLanguageCode(staf);
      }
      if (iterator.hasNext()) // the next if has is scaleRatio
      try {
    	  scaleRatio = (float)Double.parseDouble((String) iterator.next());
      }catch(NumberFormatException nfe){}
      	  
      OCREngine ocrEngine = OCREngine.getOCREngine(ocrId, staf);
      
      scaleRatio = (scaleRatio <=0 )? ocrEngine.getdefaultZoomScale():scaleRatio;
      
      //check the path of image File
      File fimg = new CaseInsensitiveFile(imgfile).toFile();
      if (!fimg.isAbsolute()) {
      	String pdir = null;
      	try{
  	   		pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
      	}catch(Exception x){}
      	if ((pdir == null)||(pdir.equals(""))){
    		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
    				"Could not get one or more variable values.")+ 
    				" "+ STAFHelper.SAFS_VAR_BENCHDIRECTORY;
    		this.issueActionOnXFailure(imgfile, error);
    		return;
      	}
      	fimg = new CaseInsensitiveFile(pdir, imgfile).toFile();
      	imgfile = fimg.getAbsolutePath();
      }
      if(!fimg.exists()||!fimg.isFile()||!fimg.canRead()){
          this.issueParameterValueFailure("BenchmarkFile="+fimg.getAbsolutePath());
      	return;
      }
      
      // detect the text in the image file
      String text = ocrEngine.storedImageToText(imgfile, langId, null, scaleRatio);
	  
	  if (command.equalsIgnoreCase(GETTEXTFROMIMAGE)) {
		  // write the detected text to outputVar, which should be a variable name 
		  if (!setVariable(outputVar, text)) {
		      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		      log.logMessage(testRecordData.getFac(),
		                       " setVariable failure, variable: " + outputVar,
		                       FAILED_MESSAGE);
		      return;
		  }
	  } else if (command.equalsIgnoreCase(SAVETEXTFROMIMAGE)) {
		  // write the detected text to outputVar, which should be a file name  
		  outputVar = FileUtilities.normalizeFileSeparators(outputVar);
		  //build File
		  File  fn = new CaseInsensitiveFile(outputVar).toFile();
		    if (!fn.isAbsolute()) {
		    	String pdir = null;
		    	try{
			   		if( outputVar.indexOf(File.separator) > -1 ) {
				  		pdir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
			    	}else{
				   		pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
			    	}
		    	}catch(Exception x){}
		    	if ((pdir == null)||(pdir.equals(""))){
		  		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
		  				"Could not get one or more variable values.")+ 
		  				" "+ STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY+", "+STAFHelper.SAFS_VAR_TESTDIRECTORY;
		  		this.issueActionOnXFailure(outputVar, error);
		  		return;
		    	}
			    fn = new CaseInsensitiveFile(pdir, outputVar).toFile();
		    }
		  
		  try {
			  FileUtilities.writeStringToUTF8File(fn.getAbsolutePath(), text);
		  } catch(Exception ex) {
		      throw new SAFSException(ex.toString());
		  }
		  outputVar = fn.getAbsolutePath();
	  } else {
		  Log.debug("No code responsible for keyword:" + command);
		  return;
	  }

	  // set status to ok
	  String detail = genericText.convert(TXT_SUCCESS_2a, command + " successful using " + ocrId, command, ocrId) 
      				  + ". " + 
      				  genericText.convert(GENStrings.BE_SAVED_TO, 
				              "'" + text + "' has been saved to '" + outputVar + "'", 
				              text, outputVar); 		  
	  log.logMessage(testRecordData.getFac(),
			  detail,
	                   GENERIC_MESSAGE);
	  testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  public static Map getfileMap() {
  	return fileMap ;
  }

}

