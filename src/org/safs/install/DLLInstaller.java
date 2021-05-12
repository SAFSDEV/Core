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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

public class DLLInstaller extends InstallerImpl {

	public static final String stafwrapfile   = s+"stafwrap.dll";
	public static final String ddvariable = s+"bin"+s+"DDVariableStore.dll";
	public static final String cfdllfile      = s+"ComponentFunctions.dll";

	public DLLInstaller() { super(); }

	/**
	 * Register DDVariableStore.DLL and copy STAFWrap.DLL and ComponentFunctions.DLL
	 * to the System32/SysWOW64 directory.
	 */
	@Override
	public boolean install(String... args) {

		String safsdir = getInstallationRoot();
		if(safsdir == null || safsdir.length() == 0) return false;
		if(! new CaseInsensitiveFile(safsdir).isDirectory()) return false;

		// find the DLLs
		String stafwrappath = safsdir + s + "bin" + stafwrapfile;
		String ddvariablepath = safsdir + ddvariable;
		String cfdllpath = safsdir + s + "bin" + cfdllfile;

		String system32 = getSystem32Dir();
		if(system32 == null) return false;

		String procstr = system32+REGSVR32;
		if(! new CaseInsensitiveFile(procstr).isFile()) return false;
		if(procstr.contains(" ")) procstr = "\""+ procstr +"\"";
		String dllstring = ddvariablepath.contains(" ") ? "\""+ ddvariablepath +"\"": ddvariablepath;
		try{
			Hashtable result = NativeWrapper.runShortProcessAndWait(procstr, new String[]{"/s", dllstring});
			Integer rc = (Integer)result.get("Result");
			if(rc != 0) return false;
		}catch(Exception x){ return false;}

		enable64BitCOM("DDVariableStore.DDVariables");
		enable64BitCOM("DDVariableStore.GlobalMappings");
		enable64BitCOM("DDVariableStore.GlobalVariables");
		enable64BitCOM("DDVariableStore.SAFSMonitor");
		enable64BitCOM("DDVariableStore.STAFResult");
		enable64BitCOM("DDVariableStore.STAFUtilities");
		enable64BitCOM("DDVariableStore.StringUtilities");
		enable64BitCOM("DDVariableStore.TestRecordData");

		File src = new CaseInsensitiveFile(stafwrappath).toFile();
		File dst = new CaseInsensitiveFile(system32, src.getName()).toFile();
		try{ FileUtilities.copyFile(new FileInputStream(src), new FileOutputStream(dst));}
		catch(Exception x){ return false;}

		src = new CaseInsensitiveFile(cfdllpath).toFile();
		dst = new CaseInsensitiveFile(system32, src.getName()).toFile();
		try{ FileUtilities.copyFile(new FileInputStream(src), new FileOutputStream(dst));}
		catch(Exception x){ return false;}

		return true;
	}

	private String getWindowsDir(){
		return getRegistryValue("HKLM\\Software\\Microsoft\\Windows NT\\CurrentVersion", "SystemRoot");
	}

	private String getSystem32Dir(){
		String windows = getWindowsDir();
		if(windows == null) return null;
		String system32 = windows + SYSWOW64;
		if(! new CaseInsensitiveFile(system32).isDirectory()) {
			system32 = windows + SYSTEM32;
			if(! new CaseInsensitiveFile(system32).isDirectory()) return null;
		}
		return system32;
	}

	/** "HKEY_CLASSES_ROOT\\" */
	static String CLASSES_ROOT                  = "HKEY_CLASSES_ROOT\\";
	/** "\\Clsid\\" */
	static String ClsidKey						= "\\Clsid\\";
	/** "HKEY_CLASSES_ROOT\\Wow6432Node\\CLSID\\" */
	static String Wow6432NodeCLSIDPrefix 		= "HKEY_CLASSES_ROOT\\Wow6432Node\\CLSID\\";
	/** "HKEY_CLASSES_ROOT\\Wow6432Node\\AppID\\" */
	static String Wow6432NodeAppIDPrefix 		= "HKEY_CLASSES_ROOT\\Wow6432Node\\AppID\\";
	/** "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\AppID\\" */
	static String LocalSoftwareClassAppIDPrefix = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\AppID\\";

/** <pre>Locate your COM object GUID under HKey_Classes_Root/Wow6432Node/CLSID.
 Once located, add a new REG_SZ (string) value. The name should be AppID
 and the data should be the same COM object GUID you have just searched for.
 Add a new key under HKey_Classes_Root/Wow6432Node/AppID. The new key should
 be called the same as the COM object GUID.
 Under the new key you just added, add a new REG_SZ (string) value, and call
 it DllSurrogate. Leave the value empty.
 Create a new key under HKey_Local_Machine/Software/Classes/AppID, if it doesn't
 already exist. Again, the new key should be called the same as the COM object's
 GUID. No values are necessary to be added under this key.
</pre> */
	private boolean enable64BitCOM(String comclass){

//    Locate your COM object GUID under HKey_Classes_Root/Wow6432Node/CLSID.
//    Once located, add a new REG_SZ (string) value. The name should be AppID and the data should be the same COM object GUID you have just searched for.
//    Add a new key under HKey_Classes_Root/Wow6432Node/AppID. The new key should be called the same as the COM object GUID.
//    Under the new key you just added, add a new REG_SZ (string) value, and call it DllSurrogate. Leave the value empty.
//    Create a new key under HKey_Local_Machine/Software/Classes/AppID, if it doesn't already exist. Again, the new key should be called the same as the COM object's GUID. No values are necessary to be added under this key.

		//'GUID is stored as default value for KEY like "HKEY_CLASSES_ROOT\DDVariableStore.DDVariable\Clsid\"
		//GUID = WshShell.RegRead(DDVariableComNames(i) & "\" & ClsidKey & "\") 'Read registry key, must ends with "\"
		String GUID = getRegistryValue(CLASSES_ROOT+ comclass + ClsidKey, null);
		if(GUID == null || GUID.length()==0 ) return false;
		int s = GUID.indexOf("{");
		if(s<0) return false;
		GUID = GUID.substring(s);
		s=GUID.indexOf("]");
		if(s < 0) return false;
		GUID = GUID.substring(0, s);

		//Create under HKEY_CLASSES_ROOT\Wow6432Node\CLSID\GUID a new REG_SZ string, named as AppID, value is GUID
		//WshShell.RegWrite Wow6432NodeCLSIDPrefix&GUID&"\AppID", GUID, "REG_SZ"
		boolean success = setRegistryValue(Wow6432NodeCLSIDPrefix + GUID, "AppID", GUID);
		//Add under HKEY_CLASSES_ROOT\Wow6432Node\AppID\ a new key, name is GUID
		//WshShell.RegWrite Wow6432NodeAppIDPrefix&GUID, ""
		success = setRegistryValue(Wow6432NodeAppIDPrefix + GUID, "", null);

		//Create under key HKEY_CLASSES_ROOT\Wow6432Node\AppID\GUID a new REG_SZ string, named as DllSurrogate, value is empty
		//WshShell.RegWrite Wow6432NodeAppIDPrefix&GUID&"\DllSurrogate", "", "REG_SZ"
		success = setRegistryValue(Wow6432NodeAppIDPrefix + GUID, "DllSurrogate", null);
		//'Add under HKEY_LOCAL_MACHINE\SOFTWARE\Classes\AppID\ a new key, name is GUID
		//WshShell.RegWrite LocalSoftwareClassAppIDPrefix&GUID, ""
		success = setRegistryValue(LocalSoftwareClassAppIDPrefix + GUID, "", null);
		return true;
	}

	/**
	 * Unregister DDVariableStore.DLL and delete STAFWrap.DLL and ComponentFunctions.DLL
	 * from the System32/SysWOW64 directory.
	 */
	@Override
	public boolean uninstall(String... args) {

		String safsdir = getInstallationRoot();
		if(safsdir == null || safsdir.length() == 0) return false;
		if(! new CaseInsensitiveFile(safsdir).isDirectory()) return false;

		// find the DLLs
		String ddvariablepath = safsdir+ddvariable;

		String system32 = getSystem32Dir();
		if(system32 == null) return false;

		String stafwrappath = system32+stafwrapfile;
		String cfdllpath = system32 + cfdllfile;

		File src = new CaseInsensitiveFile(stafwrappath).toFile();
		try{ if(src.isFile()) src.delete();}catch(Exception x){}

		src = new CaseInsensitiveFile(cfdllpath).toFile();
		try{ if(src.isFile()) src.delete();}catch(Exception x){}

		String procstr = system32+REGSVR32;
		if(! new CaseInsensitiveFile(procstr).isFile()) return false;
		if(procstr.contains(" ")) procstr = "\""+ procstr +"\"";
		String dllstring = ddvariablepath.contains(" ") ? "\""+ ddvariablepath +"\"": ddvariablepath;
		try{ NativeWrapper.runShortProcessAndWait(procstr, new String[]{"/u", "/s", dllstring}); }
		catch(Exception x){}

		return true;
	}

	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.DLLInstaller
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.DLLInstaller -u
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --  -u to perform an uninstall instead of install.
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		DLLInstaller installer = new DLLInstaller();
		for(String arg:args) if (arg.equals("-u")) uninstall = true;
		if(uninstall){
			if( installer.uninstall() ) System.exit(0);
		}else{
			if( installer.install() ) System.exit(0);
		}
		System.exit(-1);
	}
}
