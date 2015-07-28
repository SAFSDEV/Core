package org.safs.text;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

//import org.safs.Log;

/*******************************************************************************************
 * Copyright 2004 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This INIFileReader class is intended to read INI formatted text files.<br>
 * <p>
 * Physical INI files are expected to be in a particular format.  This is very much 
 * like the format for Windows INI files--which is the App Map format currently in use for the 
 * Rational Robot SAFS Engine (RRAFS).  (Though, there may be slight differences.)
 * <p>
 * The following characters are allowed to indicate commentlines or are otherwise ignored. Lines 
 * are trimmed of leading whitespace before this check is made:
 * <p>
 * &nbsp; &nbsp; &#033; (bang - exclamation point)<br>
 * &nbsp; &nbsp; &#059; (semicolon)<br>
 * &nbsp; &nbsp; &#035; (hash or pound mark)<br>
 * <p>
 * However, if an equality character ('=') exists in the line the line will be treated as a 
 * valid text line.  The assumption is the comment indicators are actually part of the NAME 
 * in a NAME=VALUE pair.
 * <p>
 * Each INI "Section" is delimited with brackets ([ ])containing the name of the section.  
 * In SAFS automation parlance, these are the "Window" definition sections.  A section delimiter 
 * should appear all by itself on a line. There should be no additional text.  The line is 
 * first trimmed before the check for a section is made.
 * <p>
 * A section identifier is not case-sensitive.  
 * <p>
 * If an INI file contains a [ApplicationConstants] section, that will be considered the "default" 
 * section.  Otherwise, any first, unnamed section is the "default". 
 * <p>
 * Items within a section have a NAME=VALUE format.<br>
 * To the left of the Equals sign is the NAME.  The substring is trimmed of leading or 
 * trailing whitespace and by default is not case-sensitive.  Enclose any space character(s) 
 * in double-quote characters.  
 * Everything to the right of the Equals sign is returned unmodified.  It is NOT trimmed 
 * of leading or trailing whitespace and the text is returned unmodified.  However, leading 
 * and trailing quotes will be removed if they exist.
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
 * @version 2.0, 02/23/2005
 * 
 * @author Carl Nagle, 02/23/2005 Modified for use of CaseInsensitiveHashtable
 * @author Carl Nagle, 02/23/2007 Modified for optional use of java.util.Hashtable
 * @author Carl Nagle, 07/16/2010 Mod to support other valid Charset encodings.
 * 
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 ******************************************************************************************/
public class INIFileReader extends FileLineReader {

	protected boolean ignoreItemCase = true;
	
	// sections are always case-insensitive 
	protected CaseInsensitiveHashtable sections = new CaseInsensitiveHashtable(20);
	
	
	/*******************************************************************************************
	 * Specifies to completely read and store the file in memory for performance. (DEFAULT)
	 ******************************************************************************************/
	public static final int IFR_MEMORY_MODE_STORED  = 0;


	/*******************************************************************************************
	 * FUTURE:Specifies to store file pointers and map regions in memory for storage efficiency.
	 ******************************************************************************************/
	public static final int IFR_MEMORY_MODE_MAPPED  = 1;

	
	/*******************************************************************************************
	 * The text string that specifies to reference the "default" AppMap section in requests.
	 ******************************************************************************************/
	public static final String IFR_DEFAULT_MAP_SECTION_COMMAND  = "DefaultMapSection";

	/*******************************************************************************************
	 * the Default App Map section until reassigned: ApplicationConstants
	 ******************************************************************************************/
	public static final String IFR_DEFAULT_MAP_SECTION = "ApplicationConstants";


	/*******************************************************************************************
	 * Determines if the file should be fully loaded and stored in memory.
	 * It would be more efficient to read and store relatively small files into object arrays.  
	 * For this, there is the IFR_MEMORY_MODE_STORED mode.
	 * <p>
	 * In this mode, a helper thread runs in the background populating our internal storage.  
	 * The underlying file stream will actually be closed once the helper thread has loaded all 
	 * values into memory.  Yet, this file instance remains valid servicing data requests until 
	 * it receives the official close() request from the object owner.
	 * <p>
	 * For much larger files, it may be more efficient to simply store pointers to key locations 
	 * in the file and then map sections in and out of memory as needed.  For this there will be 
	 * the IFR_MEMORY_MODE_MAPPED mode.
	 * <p>
	 * The initial release of this file handler only supports the STORED memorymode.  However, 
	 * the MAPPED memory mode will be implemented using the RandomAccessFile and FileChannel 
	 * support in Java.
	 ******************************************************************************************/
	protected int       memorymode = 0;
	protected String    defaultsection = new String();

	protected JITLoader jit        = null;
	protected boolean   jitstop    = false;  // set to true to stop the thread mid-process
	
	
	/*******************************************************************************************
	 * This constructor will create an inoperable (Closed) file object.  No use whatsoever. :)
	 ******************************************************************************************/
	public INIFileReader (){super();}


	/*******************************************************************************************
	 * The constructor used by the SAFSAppMapReader.
	 * 
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(file);
	 * <p>
	 * @param file A valid File object for the file to be opened.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 ******************************************************************************************/
	public INIFileReader (File file, int memorymode){

		super(file);
		
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}

	/*******************************************************************************************
	 * The constructor used by the SAFSAppMapReader.
	 * 
	 * All subclasses MUST invoke this constructor prior to completing their initialization.<br>
	 * Invoke this constructor from the subclass with:
	 * <p>
	 * &nbsp; &nbsp; super(file);
	 * <p>
	 * @param file A valid File object for the file to be opened.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 * <p>
	 * @param encoding a valid Charset encoding like "UTF-16", etc...                  
	 ******************************************************************************************/
	public INIFileReader (File file, int memorymode, String encoding){

		super(file, encoding);
		
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}
	
	/*******************************************************************************************
	 * The constructor used to force Item case sensitivity.
	 * 
	 * @param file A valid File object for the file to be opened.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 * @param ignoreItemCase set to FALSE to make Item NAMES case-sensitive.  By default Item 
	 * names are not case-sensitive.
	 ******************************************************************************************/
	public INIFileReader (File file, int memorymode, boolean ignoreItemCase){

		super(file);
		this.ignoreItemCase = ignoreItemCase;
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}

	
	/*******************************************************************************************
	 * The constructor used to force Item case sensitivity.
	 * 
	 * @param file A valid File object for the file to be opened.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 * @param ignoreItemCase set to FALSE to make Item NAMES case-sensitive.  By default Item 
	 * names are not case-sensitive.
	 * @param encoding a valid Charset encoding like "UTF-16", etc...
	 ******************************************************************************************/
	public INIFileReader (File file, int memorymode, boolean ignoreItemCase, String encoding){

		super(file, encoding);
		this.ignoreItemCase = ignoreItemCase;
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}

	/*******************************************************************************************
	 * The constructor used to force Item case sensitivity while using an InputStream.
	 * For example, resources or files stored in JARs loaded via ClassLoaders.  
	 * Files can be loaded from the file system if the directory is in the System CLASSPATH 
	 * where getSystemResourceAsStream can find it.
	 * 
	 * @param stream A valid InputStream object for the data to be read.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 * @param ignoreItemCase set to FALSE to make Item NAMES case-sensitive.  By default Item 
	 * names are not case-sensitive.
	 * @see ClassLoader#getSystemResourceAsStream(java.lang.String)
	 ******************************************************************************************/
	public INIFileReader (InputStream stream, int memorymode, boolean ignoreItemCase){

		super(stream);
		this.ignoreItemCase = ignoreItemCase;
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}

	
	/*******************************************************************************************
	 * The constructor used to force Item case sensitivity while using an InputStream.
	 * For example, resources or files stored in JARs loaded via ClassLoaders.  
	 * Files can be loaded from the file system if the directory is in the System CLASSPATH 
	 * where getSystemResourceAsStream can find it.
	 * 
	 * @param stream A valid InputStream object for the data to be read.
	 * <p>
	 * @param memorymode the memory model for this handler to use: STORED or MAPPED.  Currently 
	 *                   only the STORED model is used.
	 * @param ignoreItemCase set to FALSE to make Item NAMES case-sensitive.  By default Item 
	 * names are not case-sensitive.
	 * @param encoding a valid Charset encoding like "UTF-16", etc...
	 * @see ClassLoader#getSystemResourceAsStream(java.lang.String)
	 ******************************************************************************************/
	public INIFileReader (InputStream stream, int memorymode, boolean ignoreItemCase, String encoding){

		super(stream, encoding);
		this.ignoreItemCase = ignoreItemCase;
		if(memorymode == IFR_MEMORY_MODE_MAPPED) this.memorymode = memorymode;
		tryJITLoader();
	}

	
	/*******************************************************************************************
	 * Will run the JITLoader to populate the cache if the memorymode is appropriate.
	 * Currently, we only support the STORED memorymode; so the JITLoader will definitely run.
	 ******************************************************************************************/
    protected void tryJITLoader(){
		//if (memorymode == IFR_MEMORY_MODE_STORED){			
		jit = new JITLoader();
		jit.run();
		//}    	
    }

	/**
	 * All the stored Hashtables and the parent sections Hashtable are clear()ed.
	 * The sections Hashtable is left empty on exit.
	 */
	protected void clearHashtables(){
		for(Enumeration e = sections.elements(); e.hasMoreElements();){
			java.util.Hashtable items = (java.util.Hashtable) e.nextElement();
			items.clear();
		}
		sections.clear();				
	}
	
	/**
	 * Shutdown the JIT file processor.  The JIT will be nulled on exit.
	 */
	protected void closeJIT(){
		if ((jit != null)&&(jit.isAlive())){
			jitstop = true;
			for (int i = 0; i < 30; i++){
				try{ 
					wait(1000);
					if (!jitstop) break;
				}
				catch(Exception e) {;}
			}			
			if (jitstop) {
				try{
					jit.interrupt();
				}catch(Exception e){;}
			}
		}
		jit = null;		
	}
	
	/*******************************************************************************************
	 * Closes the file (if still open) and releases all stored resources via clearHashtables().
	 * This is the call to shutdown for termination.  This also forwards the close request 
	 * to the superclass which will shutdown the file reader.
	 * @see #closeJIT()
	 * @see #clearHashtables()
	 ******************************************************************************************/
	public void close(){
		try{
			closeJIT();
		}catch(Exception x){
//			Log.debug("INIFileReader exception closing JIT:"+x);
			System.err.println("INIFileReader exception closing JIT:"+x);
		}
		try{
			clearHashtables();
		}catch(Exception x){
//			Log.debug("INIFileReader exception clearingHashtables:"+ x);
			System.err.println("INIFileReader exception clearingHashtables:"+ x);
		}
		super.close();
	}

		
	/*******************************************************************************************
	 * Lookup an item in a particular section of the map.
	 * The section can be an empty string--the first, unnamed section.  You may also specify to 
	 * use the "default" section by specifying "DefaultMapSection" for the section parameter.
	 * The parameters are not case-sensitive.  If no "default" section has been set, the first 
	 * unnamed section is used.
	 * <p>
	 * @param section the section of the Map to find an item in.  If section is null, an empty 
	 *                String will be used to identify the the first, unnamed section.  This is not 
	 *                case-sensitive.  To use the "default" section, specify "DefaultMapSection" 
	 *                for this parameter.  All "keys" are stored in lower-case.
	 * <p>
	 * @param item the item in the section to find a value on.  This is not case-sensitive.  
	 *             If the item parameter is null or empty then a null value will be returned. 
	 *             All "keys" are stored in lower-case.
	 * <p>
	 * @return the value of the item in the specified section.
	 *         If the value appears to be a quoted string then any leading/trailing blanks 
	 *         outside the quotes and the quotes themselves will be removed.
	 *         null if the item parameter is invalid or the item cannot be found.
	 ******************************************************************************************/
	public String getAppMapItem(String section, String item){

		String mapitem = null;
		
		try{ mapitem = item.trim(); }
		catch(NullPointerException e){ return null; }
			
		String lcsection = null;
		
		try{ lcsection = section.trim(); }
		catch(NullPointerException e) { lcsection = new String(); }

		if(lcsection.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION_COMMAND))
			lcsection = defaultsection;
		
		if ((!(jit == null))&&(jit.isAlive())) {
			for (int i = 0; i < 12; i++){
				try{ wait(1000);
					 if (!jit.isAlive()) break;
				}catch(InterruptedException e){;}				
			}
		}
		
		java.util.Hashtable items = null;
		String theItem = null;
		String tempItem = null;
		try{//catch any NullPointerExceptions if theItem from the map is null.
			items = (java.util.Hashtable) sections.get(lcsection);		
			theItem = (String) items.get(mapitem);
			tempItem = theItem.trim(); //do not trim original value in-case it is NOT quoted
			if (tempItem.length() > 2){
				if ((tempItem.startsWith("\""))||
					(tempItem.startsWith("\'"))){
						theItem=tempItem.substring(1); //remove that leading quote
						tempItem=tempItem.substring(1);//from both copies for substring below
				}
				if ((tempItem.endsWith("\""))||
					(tempItem.endsWith("\'"))){
						theItem=tempItem.substring(0, tempItem.length()-1); 
				}
			}
		}
		catch(NullPointerException e){ theItem = null; }
		return theItem;
	}
	
	public String getItem(String section, String item){

		String mapitem = null;
		
		try{ mapitem = item.trim(); }
		catch(NullPointerException e){ return null; }
			
		String lcsection = null;
		
		try{ lcsection = section.trim(); }
		catch(NullPointerException e) { lcsection = new String(); }

		if(lcsection.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION_COMMAND))
			lcsection = defaultsection;
		
		if ((!(jit == null))&&(jit.isAlive())) {
			for (int i = 0; i < 12; i++){
				try{ wait(1000);
					 if (!jit.isAlive()) break;
				}catch(InterruptedException e){;}				
			}
		}
		
		java.util.Hashtable items = null;
		String theItem = null;
		try{
			items = (java.util.Hashtable) sections.get(lcsection);		
			theItem = (String) items.get(mapitem);
			}catch(NullPointerException e){ theItem = null; }
			
		return theItem;
	}
	
	/*******************************************************************************************
	 * Retrieves the memorymode (STORED or MAPPED)
	 ******************************************************************************************/
	public int getMode(){ return memorymode;}

	
	/*******************************************************************************************
	 * Retrieve the list of sections.
	 * An object with no elements is returned if there are no sections in the map.
	 ******************************************************************************************/
	public Vector getSections(){ 
		Vector list = new Vector();
		if (sections.size() == 0) return list;
		list = new Vector(sections.size());
		for(Enumeration enumerator = sections.keys();enumerator.hasMoreElements(); list.addElement(enumerator.nextElement()));
		return list;
	}

	
	/*******************************************************************************************
	 * Return the "default" section setting
	 ******************************************************************************************/
	public String getDefaultSection(){ return defaultsection; }
	
	
	/*******************************************************************************************
	 * Set the "default" section.  
	 * @param section the section to set as default.  If the value is null or 0-length, 
	 *                then we will set the default to be the first, unnamed section.
	 * <p>
	 * @return 0 on success. STAFResult.DoesNotExist (48) if the specified section does not exist.
	 ******************************************************************************************/
	public   int  setDefaultSection(String section){ 
		if ((section == null)||(section.length() == 0)) {
			defaultsection = new String();
			return 0;
			
		}else if (sections.containsKey(section.trim().toLowerCase())){
			defaultsection = section.trim().toLowerCase();
			return 0;
		
		}else{
			return 48; // STAFResult.DoesNotExist;
		}
	}

	
	/*******************************************************************************************
	 * Returns the list of items in a section.
	 * <p>
	 * @param section the section to query for list items.  If the value is null or 0-length, 
	 *                then we will get the list of items in the first, unnamed section.  If the 
	 *                value is DEFAULTMAPSECTION, then we will get the list of items in the 
	 *                "default" section.
	 * <p>
	 * @return the list of items for the section. size() should be 0 if no items exist. A NULL 
	 *         object is returned if the requested section does not exist in the map.
	 ******************************************************************************************/
	public Vector getItems(String section){ 

		Vector list = new Vector();
		String tsection = null;
		if ((section == null)||(section.length() == 0)){
			tsection = new String();
		}else if(section.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION_COMMAND)){
			tsection = defaultsection;
		}else{
			tsection = new String(section.trim());
		}
		java.util.Hashtable items = (java.util.Hashtable) sections.get(tsection);
		if (items == null) return null;
		if (items.size() == 0) return list;
		
		list = new Vector(items.size());
		
		for(Enumeration enumerator = items.keys(); enumerator.hasMoreElements(); list.addElement(enumerator.nextElement()));
		return list;
	}
	

	/*******************************************************************************************
	 * Clears any internally stored cache of prefetched items.  This will cause the App Map 
	 * to again be opened and read as necessary.  This is especially necessary for STORED memory 
	 * mode after the user has made changes to the App Map file.
	 ******************************************************************************************/
	public void clearCache(){
		close();
		open();
		tryJITLoader();
	}	

	/**
	 * Instantiates a new Hashtable or CaseInsensitiveHashtable depending on the type of 
	 * storage to be used.
	 * @return A Hashtable or CaseInsensitiveHashtable as needed to store items.
	 */
    public java.util.Hashtable getNewHashtable(int size){
    	return ignoreItemCase ? new CaseInsensitiveHashtable(size): new java.util.Hashtable(size);
    }

	// helper class the loads the map in the background
	protected class JITLoader extends Thread {
		
		private String section = new String();
		private char openb  = '[';
		private char closeb = ']';
		private char eq     = '=';
		private char bang   = '!';
		private char pound  = '#';
		private char semi   = ';';
		
		public void run(){
			
			String    text  = null;
			int       index = 0;
			java.util.Hashtable items = getNewHashtable(20);
			
			if (reader == null) return;
						
			while(readLine() != null){
					
				text = linetext.trim();
				if( text.length() == 0) continue;
				
				// if character, and an =, store it
				index = linetext.indexOf(eq, 1);

				char c1 = text.charAt(0);
				
				// skip comments and bang
				if ((( c1 == bang )||( c1 == pound )||( c1 == semi ))&& (index < 1)) continue;
				
				// open bracket
				if (c1 == openb){
					
					try{
						if (text.charAt(text.length()-1) == closeb) {
							text = text.substring(1, text.length()-1);
							// store the previous section and start a new one
							sections.put(section, items);
							items = getNewHashtable(200);
					
							section = text.trim();
					
							// set applicationsconstants as default section if it is found
							// and only if a different section has not already been set
							if ((defaultsection.length()==0)&&
								(section.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION))) 
								defaultsection = section.toString();
						}
					}catch(IndexOutOfBoundsException e){ continue; }
					
				}
				
				if (index == -1) continue;
				
				text = linetext.substring(0, index).trim();
				
				try{items.put(text, linetext.substring(index+1));}
				catch(IndexOutOfBoundsException e){ items.put(text, new String());}										
			}			
			sections.put(section, items);
		}
	}
}

