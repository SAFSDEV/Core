package org.safs.staf.service.input;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSTextFile class is intended as the file instance class for services.<br>
 * Although there are no STAF dependencies, the class is tightly integrated with the 
 * reader.  It has not been evaluated for any other use.  Though, standalone use 
 * is likely possible.
 * <p>
 * The class uses a java.io.BufferedReader as the underlying IO mechanism.
 * <p>
 * @author Carl Nagle, SAS Institute
 * @version 1.0, 06/02/2003
 * @see SAFSTextFileReader
 * @see SAFSInputService
 * 
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 ******************************************************************************************/
public class SAFSTextFile extends SAFSFile{

	public static final int MARK_INVALID = -1;
	
	private boolean skipblanklines = false;
	private boolean nolinenumbers  = false;
	private boolean trimleading    = false;
	private boolean trimtrailing   = false;
	private boolean trimwhitespace = false;

	private long    pointer     = 0;  // current (last read) linenumber
	private long    mark        = MARK_INVALID;  // marked linenumber
	private boolean isValidMark = false;
	
	private Vector commentids = new Vector(3, 2);

	/*******************************************************************************************
	 * This constructor will create an inoperable (Closed) file object.  No use whatsoever. :)
	 ******************************************************************************************/
	public SAFSTextFile (){;}
	
	/*******************************************************************************************
	 * The constructor used by the SAFSTextFileReader.
	 * 
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(machine, process, handle, fileid, file, skipblanklines, /<br>
	 * &nbsp; &nbsp; nolinenumbers, trimleading, trimtrailing, trimwhitespace, /<br>
	 * &nbsp; &nbsp; commentids);
	 * <p>
	 * This class automatically initializes its SAFSFile superclass with a call to:
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
	 * @param skipblanklines true to skip empty lines or lines with only whitespace.
	 *        By default we it will not skip blank lines.
	 * @param nolinenumbers true to turn off the line numbering prefix. By defaul line numbers 
	 *        prefix the returned text.
	 * @param trimleading trim only leading whitespace from the line.
	 * @param trimtrailing trim only trailing whitespace from the line.
	 * @param trimwhitespace trim leading AND trailing whitespace from the line.
	 * @param commentids essentially a set of strings that identify a line as a comment line.
	 *        If any are provided, then lines that begin with any one of the stored comment 
	 *        identifier strings will be skipped.  The test for leading substring occurs AFTER 
	 *        trimming any whitespace if that is enabled.
	 ******************************************************************************************/
	public SAFSTextFile (String machine, String process, int handle, String fileid, 
	                 File file, boolean skipblanklines, boolean nolinenumbers, 
	                 boolean trimleading, boolean trimtrailing, boolean trimwhitespace,
	                 Vector commentids){

		super(machine, process, handle, fileid, file);
				
		this.skipblanklines = skipblanklines;
		this.nolinenumbers  = nolinenumbers;
		
		if (trimwhitespace){
			this.trimwhitespace = true;
			this.trimleading    = true;
			this.trimtrailing   = true;
		}else{			
			this.trimleading    = trimleading;
			this.trimtrailing   = trimtrailing;
		}
		
		if ((commentids != null) && (!commentids.isEmpty())){
			this.commentids = commentids;
		}		
	}

	/*******************************************************************************************
	 * @return the line number of the last line read.
	 ******************************************************************************************/
	public  long  getLineNumber() { return pointer; }

	/*******************************************************************************************
	 * @return the bookMark line pointer for the last bookMark set
	 ******************************************************************************************/
	public  long  getMark      () { return mark   ; }
		
	/*******************************************************************************************
	 * This adds to the SAFSFile.readLine function.  While it does not modify the returned line 
	 * in any way, it does increment the line number pointer appropriately.  It also ignores 
	 * all line skipping settings.  It reads the next line regardless.
	 * <p>
	 * This routine should always call super.readLine() to take advantage of UTF-8 FORMAT marker
	 * handling done in the superclass. 
	 * <p>
	 * @return the next line in the file using the basic super.readLine function.
	 *         Returns null once at EOF.
	 ******************************************************************************************/
	public String readLine(){
		
		String val = super.readLine();
		if(val != null) pointer++;
		return val;
	}
	
	/*******************************************************************************************
	 * Reads the lines in the file and returns the next line as required by option settings.
	 * If the skipping of blank lines and comment lines are enabled, a single call to this 
	 * function will read any number of lines until the first non-blank, non-comment line is 
	 * found.  The check for leading comment substrings is done AFTER any trimming of leading 
	 * whitespace.
	 * <p>
	 * The returned line will be prefixed with the line number followed by a colon unless 
	 * 'nolinenumbers' was specified (true) when the object was created.
	 * <p>
	 * @return the next line in the file based on the options specified at creation.
	 *         Returns null once at EOF.
	 ******************************************************************************************/
	public String next (){ 
		if((isClosed())||(isEOF())) return null;
		
		boolean filtering = false;
		do{
			filtering = false;
			
			linetext = readLine();
			
			// if ! EOF
			if (! isEOF()){
				
				if (trimwhitespace) {
					linetext = linetext.trim();
				}else{
					if (trimleading && (linetext.length() > 0)){
						int i = 0;
					    for(;i<linetext.length();i++) 
					        if (linetext.charAt(i) > (int)32) break;
					    if( i < linetext.length()) linetext = linetext.substring(i);						        
					}
					if (trimtrailing && (linetext.length() > 0)){
						int i = linetext.length()-1;
					    for(;i>-1;i--) 
					        if (linetext.charAt(i) > (int)32) break;
					    if( i < linetext.length()-1) linetext = linetext.substring(0,i+1);						        
					}
				}
				if((! commentids.isEmpty())&&(linetext.length() > 0)){
					
					for(Enumeration ec = commentids.elements();((ec.hasMoreElements())&&(!filtering));)
					    filtering = linetext.startsWith((String)ec.nextElement());
				}
				
				if(!filtering) filtering = ( (linetext.length() == 0) && skipblanklines);
			}							
		}while( filtering );
		
		if(!(linetext == null)&&(! nolinenumbers)) 
		    linetext = String.valueOf(pointer).trim() +':'+ linetext;
			
		return linetext;
	}

	/*******************************************************************************************
	 * Returns the last line returned from the 'next' function.  Thus, you can re-read 
	 * the line any number of times.  Note, the value returned may be something unexpected 
	 * after a reset() or begin() operation.  The value only becomes resynchronized and 
	 * 'correct' after a call to the 'next()' function.
	 * @return the last line that was returned from the 'next' function.  
	 ******************************************************************************************/
	public String peek(){ return linetext; }
		
	/*******************************************************************************************
	 * bookMarks the current line pointer location for a subsequent reset().
	 ******************************************************************************************/
	public void mark (){ 
		if((isClosed())||(isEOF())) {
			isValidMark = false;
			mark = MARK_INVALID;
			return;
		}
		try{
			reader.mark(DEFAULT_BUFFER_SIZE);
			mark = pointer;
			isValidMark = true;
			
		// we don't care if an error occurred because our RESET will handle it
		}catch(IOException e){ isValidMark = false;}
		return;
	}
		
	
	/*******************************************************************************************
	 * sets a new line pointer location for a subsequent reset().
	 ******************************************************************************************/
	public void setMark(long line) throws IllegalArgumentException{
		if ( line < 0 ) throw new IllegalArgumentException(
		    "SAFSTextFile.setMark line specification less than 0!");
		isValidMark = false;
		mark = line;
	}
	
	/*******************************************************************************************
	 * goto the new line pointer set with setMark -- cannot use reader.reset
	 ******************************************************************************************/
	private void gotoMark(){
		
		long tmark = mark;
		close();
		open();
		
		if(! isClosed()){
			try{
				pointer = tmark;
				mark = tmark;
				if(tmark==0){
					//we are at the begining of the file, so eof is false.
					eof = false;
				}else{
					//If tmark is not 0, we should skip all lines before 'tmark' of the reader
					for(long i = 0; i<tmark;i++) linetext = reader.readLine();
					eof = (linetext == null);
				}
				return;
			}catch(IOException e2){  
				close(); 
			}
		}
		return;
	}
	
	/*******************************************************************************************
	 * resets the line pointer location to the position of the last bookMark.
	 ******************************************************************************************/
	public void reset(){ 

		if((isClosed())||(mark == MARK_INVALID)) return;
		if (mark == pointer) {
			linetext = null;
			return;
		}

		if (! isValidMark){
			gotoMark();
			return;
		}
		
		try{
			reader.reset();
			pointer = mark;
			eof = false;		
		}catch(IOException e){			
			gotoMark();
		}
		return;
	}
		
	/*******************************************************************************************
	 * resets the current line pointer location back to the beginning of the file.
	 ******************************************************************************************/
	public void begin(){ 
		if((isClosed())||(pointer==0)) return;
		
		long tmark = mark;
		close();
		open();
		if(! isClosed()) mark=tmark;
		
		return;
	}

	/*******************************************************************************************
	 * resets our line and mark pointers to default read-to-get-started values.
	 * This routine also calls the function in SAFSFile:
	 * <p>
	 * &nbsp; &nbsp; super.resetpointers();
	 * <p>
	 ******************************************************************************************/
	protected void resetpointers(){
		super.resetpointers();
		pointer = 0;
		mark = MARK_INVALID;
	}
	
}

