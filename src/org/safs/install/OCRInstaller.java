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
package org.safs.install;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;

public class OCRInstaller extends InstallerImpl {

	private static final String s = File.separator;

	/** "/ocr" */
	public static final String OCRDIR  = s+"ocr";
	/** "/gocrdata" */
	public static final String OCRDATADIR = OCRDIR +s+ "gocrdata";

	/** "/bin" */
	public static final String BINDIR  = s+"bin";
	/** "/ocr/tessdllWrapper.dll" */
	public static final String OCRDLL = OCRDIR+s+"tessdllWrapper.dll"; // is it at /ocr/tessdllWrapper.dll
	/** "/bin/tessdllWrapper.dll" */
	public static final String BINDLL  = BINDIR+s+"tessdllWrapper.dll"; // or is at /bin/tessdllWrapper.dll
	/** "TESSDATA_PREFIX" */
	public static final String TESSDATA_PREFIX = "TESSDATA_PREFIX";
	/** "GOCRDATA_DIR" */
	public static final String GOCRDATA_DIR = "GOCRDATA_DIR";
	/** "vcredist_x86" */
	public static final String VSREDIST_EXE = "vcredist_x86.exe";

	boolean isInBinDir = true; //normally it is in safs/bin by default

	public OCRInstaller() { super(); }

	public OCRInstaller(String _installdir) { super(_installdir); }

	/**
	 * Set the System Environment Variables used by SAFS.
	 * <p>
	 * TESSDATA_PREFIX<br>
	 * GOCRDATA_DIR<br>
	 * PATH<br>
	 * <p>
	 * On Windows, we also run the vcredist_x86 executable.
	 * @param args -- optional arg[0] installation directory to override existing rootdir setting.
	 * @return
	 */
	@Override
	public boolean install(String... args) {
		String safsdir = args.length > 0 ? args[0]: getInstallationRoot();
		if(safsdir == null || safsdir.length() == 0) return false;
		File file = new CaseInsensitiveFile(safsdir).toFile();
		if(!file.isDirectory()) return false;

		//find the DLL
		String ocrdir = safsdir+OCRDIR;
		String bindir = safsdir+BINDIR;
		String dllloc = safsdir + BINDLL;
		file = new CaseInsensitiveFile(dllloc).toFile();
		if(!file.isFile()){
			isInBinDir = false; //it was found not found at /bin/tessdllWrapper.dll
			dllloc = safsdir + OCRDLL;
			file = new CaseInsensitiveFile(dllloc).toFile();
			if(!file.isFile()) return false; // DLL not found anywhere
		}
		String path = isInBinDir ? bindir : ocrdir;
		appendSystemEnvironment("PATH", path, null);//will only add it if not already there.
		setEnvValue(TESSDATA_PREFIX, ocrdir+s);

		String gocrdata = safsdir + OCRDATADIR;
		file = new CaseInsensitiveFile(gocrdata).toFile();
		try{ if(!file.isDirectory()) file.mkdirs(); }
		catch(Exception x){
			uninstall();
			return false;
		}
		setEnvValue(GOCRDATA_DIR, gocrdata+s);
		String vspath = isInBinDir ? bindir+s+VSREDIST_EXE : ocrdir+s+VSREDIST_EXE;
		file = new CaseInsensitiveFile(vspath).toFile();
		if(!file.isFile()){
			uninstall();
			return false;
		}
		if(vspath.contains(" ")) vspath = "\""+ vspath +"\"";
		try{
			Hashtable result = NativeWrapper.runShortProcessAndWait(vspath, "/q");
			int rc = (Integer)result.get("Result");
			Vector data = (Vector)result.get("Vector");
			//just in-case we want to use it.
		}
		catch(Exception x){
			uninstall();
			return false;
		}
		return true;
	}

	/**
	 * UnSets the System Environment Variables used by SAFS.
	 * <p>
	 * TESSDATA_PREFIX<br>
	 * GOCRDATA_DIR<br>
	 * PATH<br>
	 * <p>
	 * @param args
	 * @return
	 */
	@Override
	public boolean uninstall(String... args) {

		setEnvValue(TESSDATA_PREFIX, null);
		setEnvValue(GOCRDATA_DIR, null);

		String safsdir = getInstallationRoot();
		if(safsdir == null || safsdir.length() == 0) return false;
		removeSystemEnvironmentSubstring("PATH", safsdir+OCRDIR+s, null);
		return true;
	}

	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.OCRInstaller
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.OCRInstaller -installdir "c:\SeleniumPlus\extra\automation"
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.OCRInstaller -u
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.OCRInstaller -u -installdir "c:\SeleniumPlus\extra\automation"
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --
	 * <br>-u to perform an uninstall instead of install.
	 * <br>-installdir to specify where the installation is or should be.
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		String arg;
		for(int i=0;i<args.length;i++) {
			arg = args[i];
			if (arg.equals(ARG_UNINSTALL)) uninstall = true;
			if (arg.equals(ARG_INSTALLDIR)){
				try{ rootdir = args[++i];}catch(Exception x){
					System.err.println("Invalid -installdir argument.");
					System.exit(1);
				}
			}
		}
		OCRInstaller installer = new OCRInstaller();
		if(uninstall){
			if( installer.uninstall() ) System.exit(0);
		}else{
			if( installer.install() ) System.exit(0);
		}
		System.exit(-1);
	}
}
