/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 27, 2016    (SBJLWA) Initial release.
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safs.tools.CaseInsensitiveFile;

/**
 * This class ONLY provides implementation for
 * <ul>
 * <li>{@link #getBins()}
 * <li>{@link #getValidExecutable(File, String)}
 * </ul>
 * User NEEDS to implement the other methods to get it work.
 * @author sbjlwa
 */
public class ProductDetectorDefault implements IProductDetector{
	/** 'HKLM\Software\' registry path for application on 32-bit OS */
	static final String REG_HKLM_ST  = "HKLM\\Software\\";	
	/** 'HKLM\Software\Wow6432Node' registry path for 32-bit application on 64-bit OS */
	static final String REG_HKLM_ST_WOW6432   = "HKLM\\Software\\Wow6432Node\\";
	/** 'bin' the sub-folder (relative to product home) holding executables */
	static final String BIN = "bin";
	/**
	 * Return an empty Map.
	 */
	public Map<String, String> getHomes() {
		return new HashMap<String, String>();
	}

	/**
	 * Return null.
	 */
	public String gerPreferredHome() {
		return null;
	}

	/**
	 * Return an array containing one sub folder 'bin'.
	 */
	public String[] getBins() {
		return new String[]{BIN};
	}

	/**
	 * Return an empty String array.
	 */
	public String[] getPossibleExecutables() {
		return new String[0];
	}

	/**
	 * Implement the search logic suggested in the interface.<br>
	 * 1. find the default executable under product home<br>
	 * 2. find the default executable under the {@link #getBins()} sub folder of product home<br>
	 * 3. find the {@link #getPossibleExecutables()} under the product home<br>
	 */
	public File getValidExecutable(File home, String defaultExecutable) throws FileNotFoundException{
		List<String> nonExistExecutables = new ArrayList<String>();
		File executableFile = new CaseInsensitiveFile(home, defaultExecutable).toFile();

		//Check if we can find the default executable under the "bin" sub folder 
		if(!executableFile.isFile()){
			nonExistExecutables.add(executableFile.getAbsolutePath());
			if(defaultExecutable!=null && !defaultExecutable.trim().isEmpty()){
				for(String bin:getBins()){
					if(!defaultExecutable.startsWith(bin+File.separator)){
						executableFile = new CaseInsensitiveFile(home, bin+File.separator+defaultExecutable).toFile();
						if(executableFile.isFile()){
							break;
						}else{
							nonExistExecutables.add(executableFile.getAbsolutePath());
						}
					}
				}
			}
		}

		//Check if we can find the possible executables
		if(!executableFile.isFile()){				
			for(String executable:getPossibleExecutables()){
				if(!executable.equals(defaultExecutable)){
					executableFile =new CaseInsensitiveFile(home,  executable).toFile();
					if(executableFile.isFile()){
						break;
					}else{
						nonExistExecutables.add(executableFile.getAbsolutePath());
					}
				}				
			}
		}

		if(!executableFile.isFile()){
			StringBuffer paths = new StringBuffer();
			for(String path:nonExistExecutables){
				paths.append("\n"+path);
			}
			throw new FileNotFoundException("Executable Paths '"+paths+"' seems to be invalid.");
		}

		return executableFile;
	}
}
