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

