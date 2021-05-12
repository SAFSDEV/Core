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

import java.io.File;

import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.PathInterface;
import org.safs.tools.UniqueStringID;

public class UniqueStringMapInfo
	extends UniqueStringID
	implements UniqueMapInterface {

	protected String mapinfo = null;
	protected String mappath = null;
	
	/**
	 * Constructor for UniqueStringMapInfo
	 */
	public UniqueStringMapInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringMapInfo
	 */
	public UniqueStringMapInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringMapInfo
	 */
	public UniqueStringMapInfo(String id, String mapinfo) {
		this(id);
		setMapInfo(mapinfo);
	}

	/**
	 * Set the mapinfo to the String provided.
	 */
	public void setMapInfo(String mapinfo) {
		this.mapinfo = mapinfo;
	}

	/**
	 * @see UniqueMapInterface#getMapInfo()
	 */
	public Object getMapInfo() {
		return mapinfo;
	}

	String returnSetPath(File afile){
		mappath=afile.getAbsolutePath();
		return mappath;
	}
	
	/**
	 * @return null if invalid source information exists.
	 */
	public Object getMapPath(PathInterface driver) {
		if (mappath==null){

			String lctestext  = ".map";
			String uctestext  = lctestext.toUpperCase();
			
			// TRY PROVIDED FILENAME and DERIVATIVES		
			File theFile = new CaseInsensitiveFile(mapinfo).toFile();
			
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile(mapinfo + uctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile(mapinfo + lctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			// TRY DATAPOOL RELATIVE FILENAME and DERIVATIVES		
			String trypath = driver.getDatapoolDir() + File.separator;
	
			theFile = new CaseInsensitiveFile(trypath + mapinfo).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
			
	
			theFile = new CaseInsensitiveFile(trypath + mapinfo + uctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile(trypath + mapinfo + lctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			// TRY PROJECT RELATIVE FILENAME and DERIVATIVES		
			trypath = driver.getProjectRootDir() + File.separator;
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo + uctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo + lctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
			
			// TRY BENCH RELATIVE FILENAME and DERIVATIVES		
			trypath = driver.getBenchDir() + File.separator;
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo + uctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
	
			theFile = new CaseInsensitiveFile (trypath + mapinfo + lctestext).toFile();
			if (theFile.isFile()) return returnSetPath(theFile);
			
			return null;			
		}
		return mappath;
	}
}

