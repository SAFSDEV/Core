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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 27, 2016    (Lei Wang) Initial release.
 * JUN 29, 2018    (Lei Wang) Added some default implementation to fit the changes in interface IProductDetector.
 *                           Added getRegistryValue(): get registry value according to system's bits and product's bits.
 * JUL 02, 2018    (Lei Wang) Modified getRegistryValue(): on 64 bit system, get the 64 bit registry value for 64 bit product even the JRE being used is 32 bit.
 * JUL 03, 2018    (Lei Wang) Moved constants to Constants.RegistryConstants
 *                           Moved getting registry code to NativeWrapper.getRegistry32Prodcut/getRegistry64Prodcut.
 *                           Added deleteRegistry().
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.install.InstallerImpl.BIT_OPTION;
import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;

/**
 * This class ONLY provides implementation for
 * <ul>
 * <li>{@link #getBins()}
 * <li>{@link #getValidExecutable(File, String)}
 * </ul>
 * User NEEDS to implement the other methods to get it work.
 * @author Lei Wang
 */
public class ProductDetectorDefault implements IProductDetector{

	/** 'bin' the sub-folder (relative to product home) holding executables */
	public static final String BIN = "bin";

	/** The product's bit, 32 or 64. The default is 64 bit. */
	protected BIT_OPTION productBit = BIT_OPTION.BIT_64;

	public ProductDetectorDefault(){}
	public ProductDetectorDefault(BIT_OPTION productBit){
		this.productBit = productBit;
	}

	@Override
	public BIT_OPTION getProductBit() {
		return productBit;
	}
	@Override
	public void setProductBit(BIT_OPTION productBit) {
		this.productBit = productBit;
	}

	/**
	 * To get the value from registry for path starting with {@link #HKLM_ST}<br>
	 * According to product bit {@link #productBit}, get the registry's value.<br>
	 *
	 * @param keyPrefix String, the prefix of the registry path, such as 'HKLM\Software\'.
	 * @param keySuffix String, the suffix of the registry path, such as 'GPL Ghostscript\9.23'
	 * @param value String, the registry value, it can be "(Default)".
	 * @return String the value got from registry
	 */
	public String getRegistryValue(String keyPrefix, String keySuffix, String value){
		String result = null;

		if(BIT_OPTION.BIT_32.equals(productBit)){
			result = NativeWrapper.getRegistry32Prodcut(keyPrefix, keySuffix, value);

		}else if(BIT_OPTION.BIT_64.equals(productBit)){
			result = NativeWrapper.getRegistry64Prodcut(keyPrefix, keySuffix, value);

		}else{
			IndependantLog.debug("productBit "+productBit+ " is neither 32 bit nor 64 bit, cannot evaluate ...");
		}

		return result;
	}

	/**
	 * To delete the value from registry for path starting with {@link #HKLM_ST}<br>
	 * According to product bit {@link #productBit}, delete the registry's value.<br>
	 *
	 * @param keyPrefix String, the prefix of the registry path, such as 'HKLM\Software\'.
	 * @param keySuffix String, the suffix of the registry path, such as 'GPL Ghostscript\9.23'
	 * @param value String, the registry value, it can be "(Default)".
	 * @return boolean true if the registry value has been successfully deleted.
	 */
	public boolean deleteRegistry(String keyPrefix, String keySuffix, String value){
		boolean result = false;

		if(BIT_OPTION.BIT_32.equals(productBit)){
			result = NativeWrapper.deleteRegistry32Prodcut(keyPrefix, keySuffix, value);

		}else if(BIT_OPTION.BIT_64.equals(productBit)){
			result = NativeWrapper.deleteRegistry64Prodcut(keyPrefix, keySuffix, value);

		}else{
			IndependantLog.debug("productBit "+productBit+ " is neither 32 bit nor 64 bit, cannot evaluate ...");
		}

		return result;
	}

	/**
	 * Return an empty Map.
	 */
	@Override
	public Map<String, String> getHomes() {
		return new HashMap<String, String>();
	}

	/**
	 * Return null.
	 */
	@Override
	public String getPreferredHome() {
		return null;
	}

	/**
	 * Return null.
	 */
	@Override
	public String getDefaultHome(){
		return null;
	}

	@Override
	public String findUnInstaller(String home)throws FileNotFoundException{
		String uninstallerFolder = home;
		if(uninstallerFolder==null){
			uninstallerFolder = this.getPreferredHome();
		}
		if(uninstallerFolder==null){
			throw new FileNotFoundException("The folder containing uninstaller is not provided!");
		}
		String uninstaller = getUnInstaller();
		if(uninstaller==null){
			throw new FileNotFoundException("The uninstaller file name is not provided!");
		}

		CaseInsensitiveFile uninstallerFile = new CaseInsensitiveFile(uninstallerFolder, uninstaller);
		if(! uninstallerFile.exists()) throw new FileNotFoundException(uninstaller);
		return uninstallerFile.getPath();
	}

	@Override
	public String appendSientOption(String command){
		return command;
	}
	@Override
	public String appendInstallDirOption(String command, String installDirectory){
		return command;
	}
	@Override
	public String appendOptions(String command, String... options){
		String appenedCommand = command;

		if(options!=null){
			for(String option:options){
				appenedCommand += " "+option;
			}
		}
		return appenedCommand;
	}
	/**
	 * Return null.
	 */
	@Override
	public String getInstaller(){
		return null;
	}

	/**
	 * Return null.
	 */
	@Override
	public String getUnInstaller(){
		return null;
	}

	/**
	 * Return an array containing one sub folder 'bin'.
	 */
	@Override
	public String[] getBins() {
		return new String[]{BIN};
	}

	/**
	 * Return an empty String array.
	 */
	@Override
	public String[] getPossibleExecutables() {
		return new String[0];
	}

	/**
	 * Implement the search logic suggested in the interface.<br>
	 * 1. find the default executable under product home<br>
	 * 2. find the default executable under the {@link #getBins()} sub folder of product home<br>
	 * 3. find the {@link #getPossibleExecutables()} under the product home<br>
	 */
	@Override
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
