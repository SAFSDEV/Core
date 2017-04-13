/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.text;

import java.io.*;
import org.safs.text.CaseInsensitiveHashtable;
import java.util.Enumeration;


/**
 * Extends INIFileReader to allow modifying INI File values and then writing these values
 * back to a file.  We can overwrite the existing INI file or create a different one.
 * We cannot (yet) create and build a new INI file from scratch.
 *
 * @author Carl Nagle
 * @since Feb 23, 2005
 */
public class INIFileReadWrite extends INIFileReader {

	/**
	 * @see INIFileReader#INIFileReader()
	 */
	public INIFileReadWrite() {
		super();
	}

	/**
	 *
	 * @see INIFileReader#INIFileReader(File, int)
	 */
	public INIFileReadWrite(File file, int memorymode) {
		super(file, memorymode);
	}

	/**
	 * Set/Change a new or existing INI value.
	 * The routine will add a new section or item as necessary.
	 * Note: these changes will not persist past a call to clearCache unless a
	 * writeINIFile call is performed first.
	 *
	 * @param section section to receive value. If null or 0-length the item will be
	 * stored in the first unnamed section.  The section names are not case-sensitive.
	 *
	 * @param item to receive the specified value.  null or 0-length item names will be
	 * ignored and the routine will return without error.  The item names are not case-sensitive.
	 *
	 * @param value of the item.  null values will be ignored and the routine will return
	 * without error. 0-length values (empty strings) are allowed.  Note: values are not
	 * automatically wrapped in single or double quotes.  So if the value as written to file
	 * should contain quotes they need to already be part of the value provided.
	 *
	 * @see #writeINIFile(File)
	 * @see INIFileReader#clearCache()
	 */
	public void setAppMapItem(String section, String item, String value){
		if((item == null)||(item.length()==0)||(value == null)) return;
		if(section==null) section = new String();

		String lcsection = section.trim();
		String lcitem = item.trim();

		if(lcsection.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION_COMMAND))
			lcsection = defaultsection;

		// let the JIT reader finish if running	(up to ~30 seconds)
		if ((!(jit == null))&&(jit.isAlive())) {
			for (int i = 0; i < 30; i++){
				try{ wait(1000);
					 if (!jit.isAlive()) break;
				}catch(InterruptedException e){;}
			}
		}
		CaseInsensitiveHashtable items = null;
		try{
			items = (CaseInsensitiveHashtable) sections.get(lcsection);
			items.put(lcitem, value);
		}
		// no such items section
		catch(NullPointerException e){
			CaseInsensitiveHashtable asection = new CaseInsensitiveHashtable(20);
			asection.put(lcitem, value);
			sections.put(lcsection, asection);
		}
	}

	/**
	 * Write the (modified) INI File contents to file.
	 * Closes our File if it is open and then opens to write the new file.  We can write a
	 * new INI file or overwrite the existing INI file if an alternate File object is
	 * provided.
	 *
	 * @param alternate optional File object to write the information to a File other than
	 * the one originally read in.  A null value will result in writing back (overwriting) the
	 * same file we read in.
	 */
	public void writeINIFile(File alternate){
		saveINIFileWithANSIorUTF8(alternate, false);
	}
	/**
	 * Write the (modified) INI File contents to file formatted with UTF-8.
	 * @see #writeINIFile(File)
	 */
	public void writeUTF8INIFile(File alternate){
		saveINIFileWithANSIorUTF8(alternate, true);
	}
	/**
	 * Called by writeINIFile(File) and writeUTF8INIFile(File)
	 * @param alternate  alternate optional File object.
	 * @param isUTF8format   true, write file with UTF8 format; false, write file with ANSI format.
	 * @see #writeINIFile(File) #writeUTF8INIFile(File)
	 */
	protected void saveINIFileWithANSIorUTF8(File alternate, boolean isUTF8format){
		File outfile = (alternate == null)? file:alternate;
		if(! outfile.isFile()) return;
		close();
		resetpointers();
		BufferedWriter writer;
		try{
			if (isUTF8format)
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile),"UTF8"));
			else
				writer = new BufferedWriter(new FileWriter(outfile)); //ANSI format

			// write unnamed section first
			String section = new String();
			CaseInsensitiveHashtable store = (CaseInsensitiveHashtable) sections.get(section);
			if (store != null) writeSection(writer, section, store);
			// write ApplicationConstants next
			section = sections.findCaseInsensitiveStringKey(IFR_DEFAULT_MAP_SECTION);
			store = (section == null) ? null: (CaseInsensitiveHashtable) sections.get(section);
			if (store != null) writeSection(writer, section, store);
			// write remaining sections
			Enumeration<?> keys = sections.keys();
			while(keys.hasMoreElements()){
				section = (String) keys.nextElement();
				if (section.length()==0) continue;
				if (section.equalsIgnoreCase(IFR_DEFAULT_MAP_SECTION)) continue;
				store = (CaseInsensitiveHashtable) sections.get(section);
				if (store != null) writeSection(writer, section, store);
			}
			writer.flush();
			writer.close();
			writer = null;
			setFile(outfile);
			tryJITLoader();
		}catch(IOException e){
			close();
		}
	}
	/**
	 * Output an INI file [section] to the writer.  If 'section' is null or 0-length then
	 * we are writing to an unnamed (unbracketed) section.  Each item in the items Hashtable
	 * is written as a name=value pair on a separate line.  A single blank line is written
	 * at the end of the section to separate sections.
	 */
	protected void writeSection(BufferedWriter writer, String section, CaseInsensitiveHashtable items){
		if (section == null) section = new String();
		try{
			if (section.length() > 0){
					writer.write("["+section+"]");
					writer.newLine();
			}
			String item = null;
			String value = null;
			Enumeration<?> keys = items.keys();
			while(keys.hasMoreElements()){
				item = (String) keys.nextElement();
				value = item+"="+items.get(item).toString();
				writer.write(value);
				writer.newLine();
			}
			writer.newLine();
		}catch(IOException x){
			x.printStackTrace();
			return;
		}
	}

	/*******************************************************************************************
	 * Closes the JIT and file reader (if still open).  This is not necessarily an object
	 * termination event because we may just be writing our INI file data back to a file and
	 * then resuming normal operation.
	 * @see #closeJIT()
	 * @see #closeReader()
	 ******************************************************************************************/
	public void close(){
		closeJIT();
		closeReader();
	}

	public void finalize() throws Throwable {
		close();
		clearHashtables();
	}
}
