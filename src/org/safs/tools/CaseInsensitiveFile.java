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
package org.safs.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.safs.IndependantLog;

/**
 * This Class is meant to extend File behavior allowing for
 * case insensitive files.  This "has a" internal protected File object
 * and all File methods are implemented by simplying returning the internal
 * File.method() for all File methods.  The toFile() method returns the 
 * internal File object.
 * <p>
 * This class has no SAFS dependencies and can be packaged independent of other SAFS classes.
 * 
 * @author Jack Imbriani
 */

public class CaseInsensitiveFile {

	// internal File used to surface File behavior
	protected File ciFile = null ;
	
	/**
	 * @see java.io.File#File(java.lang.String)
	 */
	public CaseInsensitiveFile(String pathname) {
		ciFile = getCaseInsensitiveFile(pathname);
	}

	/**
	 * @see java.io.File#File(java.lang.String, java.lang.String)
	 */
	public CaseInsensitiveFile(String parent, String child) {
		ciFile = getCaseInsensitiveFile(parent + File.separator + child);
	}

	/**
	 * @see java.io.File#File(java.io.File, java.lang.String)
	 */
	public CaseInsensitiveFile(File parent, String child) {
		ciFile = getCaseInsensitiveFile(parent.getPath() + File.separator + child);
	}

	/**
	 * @see java.io.File#File(java.io.File, java.lang.String)
	 */
	public CaseInsensitiveFile(CaseInsensitiveFile parent, String child) {
		ciFile = getCaseInsensitiveFile(parent.getPath() + File.separator + child);
	}

	/**
	 * @see java.io.File#File(java.net.URI)
	 * Note that case insensitivity does not apply to URI
	 */
	public CaseInsensitiveFile(URI uri) {
		// case insensetivity does not apply here.
		ciFile = new File(uri) ;
	}
	
	/**
	 * Method toFile.
	 * @return File  Returns the internal File object so can be used as a File
	 */
	public File toFile() {
		return ciFile ;
	}

	/*
	 ******************************************************************************
	 * 
	 * handle directory path search beginning with root and returning first matching
	 * 
	 ******************************************************************************
	 */

	protected File getCaseInsensitiveFile( String strFile ) {

		// early return possibility if strFile exists exactly as is
		File theFile = new File(strFile) ;
		if( theFile.exists() ) return theFile ;

		//remove any enclosing quotes
		try{
			if(strFile.charAt(0)==(char)34) strFile = strFile.substring(1);
			if(strFile.charAt(strFile.length()-1)==(char)34) strFile = strFile.substring(0, strFile.length()-1);
		}catch(Exception x){
			IndependantLog.debug("CaseInsensitiveFile.getCaseInsensitiveFile for '"+ strFile +"' IGNORING: "+ x.getClass().getSimpleName());
		}
		// we need to work with whatever file.separator the System is using noting that
		// windows uses \ as a separator requiring the String.split regexp to use \\
		String file_separator = File.separator ;
		if( file_separator.equals("\\") ) file_separator += "\\" ;
		
		// work with array of strings that make up the strFile path
		String subpaths[] = strFile.split(file_separator) ;

		// the case insensitive file object
		File caseInsensitiveFile = null ;

		// traverse the subpaths building foundPath as we go using the strFile
		// substrings exactly if they make an existing path, or case permuted variants
		// of the substrings if found

		// this will store the path to the file we find including variants of case
		// note that only the first case permuted variant will be used if multiple exist
		String foundPath = "" ;
		
		// absolute and relative files behave differently and considerations must be made
		// for how they are specified (begin with File.separator, filesystemroot, or .)
		int start_idx = 0 ;
		if( theFile.isAbsolute() ) {
			// may begin with File.separator or drive specification (c:\ for example)
			if( theFile.getPath().startsWith(file_separator) ) {
				// starting with file_separator means [i.e. /file (unix) or \file (windows)]
				start_idx = 1 ;
			} else {
				// subpaths[0] should be file root specification (c: on windows for example)
				foundPath = subpaths[0] ;
				start_idx = 1 ;
			}
		} else {
			// relative path may begin with . or a name.
			// a name requires prepending ., finding name, then removing .+file_separator
			// if begins with ., treat like absolute path (no else needed)
			if( ! subpaths[0].startsWith(".") ) {
				File relFile = findFileMP(".",subpaths[0]) ;
				// return early using original strFile if no match found
				if( relFile == null ) return new File(strFile) ;
				foundPath = relFile.getPath().replaceFirst("."+file_separator,"") ;
				start_idx = 1 ;
			}
		}
		
		
		// traverse the subpaths building foundPath as we go using the strFile
		// substrings exactly if they make an existing path, or case permuted variants
		// of the substrings if found
		for( int idx = start_idx ; idx < subpaths.length ; idx++ ) {
			String huntPath = null ;
						
			if( ! foundPath.equals("") ) {
				// foundPath has been set, either above or in prior iteration, so go with it
				huntPath = foundPath + File.separator + subpaths[idx] ;
			} else {
				// empty foundPath means have to set huntPath
				// and possibly foundPath prior to use
				if( theFile.isAbsolute() ) {
					// absolute file, start with separator and hunt for subpaths[idx]
					foundPath = File.separator ;
					huntPath = subpaths[idx] ;
				} else {
					// relative file, use subpaths[idx] as starting point
					// foundPath is set above
					huntPath = subpaths[idx] ;
				}
			}

			File tfile = new File(huntPath) ;
			if( tfile.exists() ) {
				// path exists as is, so use it exactly
				foundPath = tfile.getPath() ;
			} else {
				// path does not exist as is, so search for case permuted variants
				// we know that foundPath exists so use helper method for
				// case insensitive compares taking the first match found
				caseInsensitiveFile = findFileMP(foundPath,subpaths[idx]) ;
				if( caseInsensitiveFile != null ) {
					// variant found, use it in the foundPath
					foundPath = caseInsensitiveFile.getPath() ;
				} else {
					// no case permuted variant exists.
					// return File based on current foundPath and
					// remaining portion of original input strFile path
					String therest = "" ;
					for ( int i = idx ; i < subpaths.length ; i++ ) {
						therest += File.separator + subpaths[i] ;
					}
					return new File(foundPath+therest) ;
				}
			}
		}

		// return a File which will be some permutation of strFile
		// not sure why but need to create new File object instead
		// of returning caseInsensitiveFile.  guess is object goes out of scope.
		return new File(foundPath) ;
		
	}

	// this method was copied from Carl's work in org.safs.tools.input.UniqueStringFileInfo
	// find a case-insensitive match to the provided filename.ext
	// rootpath is expected to be a valid/verified path on the current platform.
	// we will search all files in the rootpath directory looking for a case-insensitive
	// match.  This is needed to support Unix case-sensitive filenames.
	protected File findFileMP(String rootpath, String afilename){

		File fdir = new File(rootpath);
		if (!fdir.isDirectory()) return null;
		
		String[] files = fdir.list();
		if ((files == null)||(files.length == 0)) return null;
		
		String lcfile;
		String lcfilename = afilename.toLowerCase();
		
		for (int i=0; i< files.length; i++){
			lcfile = files[i].toLowerCase();
			if (lcfile.equals(lcfilename)) return new File(fdir, files[i]);
		}
		return null;
	}

	/*
	 ******************************************************************************
	 * 
	 *  All of the following methods simply invoke and return 
	 *  ciFile.method(args).  These are all methods of the File object
	 *  and this design was chosen because we cannot extend File and manipulate
	 *  args before calling the super constructors.  These methods let the
	 *  CaseInsensitiveFile behave like a File without being a File.
	 * 
	 ******************************************************************************
	 */
	
	/**
	 * @see java.io.File#canRead()
	 */
	public boolean canRead() {

		return ciFile.canRead();
	}
	/**
	 * @see java.io.File#canWrite()
	 */
	public boolean canWrite() {

		return ciFile.canWrite();
	}
	/**
	 * @see java.io.File#compareTo(java.io.File)
	 */
	public int compareTo(File arg0) {

		return ciFile.compareTo(arg0);
	}
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {

		return ciFile.compareTo((File)arg0);
	}
	/**
	 * @see java.io.File#createNewFile()
	 */
	public boolean createNewFile() throws IOException {

		return ciFile.createNewFile();
	}
	/**
	 * @see java.io.File#delete()
	 */
	public boolean delete() {

		return ciFile.delete();
	}
	/**
	 * @see java.io.File#deleteOnExit()
	 */
	public void deleteOnExit() {

		ciFile.deleteOnExit();
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {

		return ciFile.equals(arg0);
	}
	/**
	 * @see java.io.File#exists()
	 */
	public boolean exists() {

		return ciFile.exists();
	}
	/**
	 * @see java.io.File#getAbsoluteFile()
	 */
	public File getAbsoluteFile() {

		return ciFile.getAbsoluteFile();
	}
	/**
	 * @see java.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath() {

		return ciFile.getAbsolutePath();
	}
	/**
	 * @see java.io.File#getCanonicalFile()
	 */
	public File getCanonicalFile() throws IOException {

		return ciFile.getCanonicalFile();
	}
	/**
	 * @see java.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws IOException {

		return ciFile.getCanonicalPath();
	}
	/**
	 * @see java.io.File#getName()
	 */
	public String getName() {

		return ciFile.getName();
	}
	/**
	 * @see java.io.File#getParent()
	 */
	public String getParent() {

		return ciFile.getParent();
	}
	/**
	 * @see java.io.File#getParentFile()
	 */
	public File getParentFile() {

		return ciFile.getParentFile();
	}
	/**
	 * @see java.io.File#getPath()
	 */
	public String getPath() {

		return ciFile.getPath();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {

		return ciFile.hashCode();
	}
	/**
	 * @see java.io.File#isAbsolute()
	 */
	public boolean isAbsolute() {

		return ciFile.isAbsolute();
	}
	/**
	 * @see java.io.File#isDirectory()
	 */
	public boolean isDirectory() {

		return ciFile.isDirectory();
	}
	/**
	 * @see java.io.File#isFile()
	 */
	public boolean isFile() {

		return ciFile.isFile();
	}
	/**
	 * @see java.io.File#isHidden()
	 */
	public boolean isHidden() {

		return ciFile.isHidden();
	}
	/**
	 * @see java.io.File#lastModified()
	 */
	public long lastModified() {

		return ciFile.lastModified();
	}
	/**
	 * @see java.io.File#length()
	 */
	public long length() {

		return ciFile.length();
	}
	/**
	 * @see java.io.File#list()
	 */
	public String[] list() {

		return ciFile.list();
	}
	/**
	 * @see java.io.File#list(java.io.FilenameFilter)
	 */
	public String[] list(FilenameFilter arg0) {

		return ciFile.list(arg0);
	}
	/**
	 * @see java.io.File#listFiles()
	 */
	public File[] listFiles() {

		return ciFile.listFiles();
	}
	/**
	 * @see java.io.File#listFiles(java.io.FileFilter)
	 */
	public File[] listFiles(FileFilter arg0) {

		return ciFile.listFiles(arg0);
	}
	/**
	 * @see java.io.File#listFiles(java.io.FilenameFilter)
	 */
	public File[] listFiles(FilenameFilter arg0) {

		return ciFile.listFiles(arg0);
	}
	/**
	 * @see java.io.File#mkdir()
	 */
	public boolean mkdir() {

		return ciFile.mkdir();
	}
	/**
	 * @see java.io.File#mkdirs()
	 */
	public boolean mkdirs() {

		return ciFile.mkdirs();
	}
	/**
	 * @see java.io.File#renameTo(java.io.File)
	 */
	public boolean renameTo(File arg0) {

		return ciFile.renameTo(arg0);
	}
	/**
	 * @see java.io.File#setLastModified(long)
	 */
	public boolean setLastModified(long arg0) {
		return ciFile.setLastModified(arg0);
	}
	/**
	 * @see java.io.File#setReadOnly()
	 */
	public boolean setReadOnly() {
		return ciFile.setReadOnly();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ciFile.toString();
	}
	/**
	 * @see java.io.File#toURI()
	 */
	public URI toURI() {
		return ciFile.toURI();
	}
	/**
	 * @see java.io.File#toURL()
	 */
	public URL toURL() throws MalformedURLException {
		return ciFile.toURL();
	}
	
}
