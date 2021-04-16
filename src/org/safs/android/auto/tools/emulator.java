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
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 **/ 
package org.safs.android.auto.tools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.safs.android.auto.lib.AndroidTools;
import org.safs.android.auto.lib.Process2;

/**
 * Also see alternative org.safs.android.auto.lib.StartEmulator
 */
public class emulator {

	private static void help(){
		System.out.println("Valid arguments:");
		System.out.println("   -help  (this message)");
		System.out.println("   -sdk <full path to sdk root dir> (required if ANDROID_SDK not set)");
		System.out.println("   any other args (-avd <avd>) passed along to the emulator invocation.");
	}
	
	/**
	 * Used for launching an emulator via a separate Java process.
	 * This is sometimes necessary when the emulator won't otherwise launch on a platform. 
	 * @param args<br>
	 * -sdk path_to_sdk_root_dir (may be required)<br>
	 * any additional args to pass along to the emulator--especially the -avd.
	 */
	public static void main(String [] args) throws InterruptedException, IOException {
		String toolroot = null;
		String avd = null;
		
		Properties props = System.getProperties();
		Enumeration e = props.keys();
		String key;
		System.out.println("SYSTEM PROPERTIES:");
		while(e.hasMoreElements()){
			key = (String) e.nextElement();
			System.out.println("   "+ key +"="+ props.getProperty(key));
		}
		System.out.println("ENVIRONMENT VARS:");
		Map map = System.getenv();
		Set set = map.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()){
			key = (String) it.next();
			System.out.println("   "+ key +"="+ map.get(key));
		}
		System.out.println("COMMAND ARGS:");
		for(int i=0;i<args.length;i++)	System.out.println("   "+ args[i] );
		
		// new to Java 6
		//System.out.println("SYSTEM CONSOLE:"+ System.console());
		
		ArrayList emuargs = new ArrayList();
		if(args != null && args.length > 0){
			String arg1;
			try{
				for(int i=0;i<args.length;i++){
					arg1 = args[i];
					if(arg1.equalsIgnoreCase("-sdk")){
						toolroot = args[++i];
					}else if(arg1.equalsIgnoreCase("-help")){
						help();
						return;
					}else {
						emuargs.add(arg1);
					}
				}
			}catch(Exception any){
				help();
				return;
			}
		}else{
			help();
			return;
		}
		AndroidTools sdk = AndroidTools.get();
		if(toolroot != null && toolroot.length()> 0) sdk.setAndroidHome(toolroot);
		Process2 proc = sdk.emulator(emuargs);
		proc.forwardOutput();
		proc.waitForSuccess();
	}
}
