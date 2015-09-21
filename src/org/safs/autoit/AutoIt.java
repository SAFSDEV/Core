package org.safs.autoit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.image.ImageUtils;
import org.safs.tools.drivers.DriverConstant;

import autoitx4java.AutoItX;

import com.jacob.com.ComFailException;
import com.jacob.com.LibraryLoader;

public class AutoIt {
    
	private static AutoItX it = null;
	private static final String JACOB_DLL_32 = "jacob-1.18-x86.dll";
	private static final String JACOB_DLL_64 = "jacob-1.18-x64.dll";

	public static AutoItX AutoItObject() {
		
		String methodName = "AutoItX.AutoItObject() ";
		
		if (it == null) {
			
			String jacobDllVersionToUse;
			String libdir = null;
			
			if (jvmBitVersion().contains("32")){
				jacobDllVersionToUse = JACOB_DLL_32;
			} else {
				jacobDllVersionToUse = JACOB_DLL_64;
			}
			
			if (System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR) != null) {
				libdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR)+ File.separator+"lib"+File.separator;
			} else {
			    libdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SELENIUMPLUS_DIR)+ File.separator +"libs"+File.separator;
			}
		
			File file = new File(libdir, jacobDllVersionToUse);
			System.setProperty(LibraryLoader.JACOB_DLL_PATH, file.getAbsolutePath());
			
			IndependantLog.debug(methodName + "AutoItX object is created");
			
			try {
				it = new AutoItX();
			} catch (ComFailException cfe) {
				// register dll and re initiate object
				IndependantLog.debug(methodName + "Register DLL to the system.");
				
				String cmd =  System.getenv("SYSTEMDRIVE") + "\\Windows\\SysWOW64\\regsvr32 /s "+libdir+"AutoItX3.dll";
				executeCommand(cmd);
				cmd = System.getenv("SYSTEMDRIVE") + "\\Windows\\System32\\regsvr32 /s "+libdir+"AutoItX3_x64.dll";
				executeCommand(cmd);
				
				it = new AutoItX();
			}			
	
			if(it == null) IndependantLog.debug(methodName + "AutoItX object is null");
			
		} else { // use existing object
			IndependantLog.debug(methodName + "Recycle AutoIt object");
		}
		
		return it;
	}	
	
	private static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                           new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	private static String jvmBitVersion(){
		return System.getProperty("sun.arch.data.model");
	}	
}
