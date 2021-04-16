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
package org.safs;

import org.safs.logging.*;
import org.safs.tools.drivers.EmbeddedHookDriver;

/**
 * Default EmbeddedHookDriver subclass for the SAFS DriverCommands Engine.
 * <p>
 * This is primarily a proof-of-concept at this time.
 * Users would use a subclass of this class for runtime execution inside the DriverCommands engine.
 * <p>
 * The class has the same config requirements as JSAFSDriver:
 * <p>
 * -Dsafs.project.config=pathTo\test.ini
 * <p>
 * Example:
 * <p>
 * %SAFSDIR%\jre\bin\java.exe -Dsafs.project.config=fullPath\to\test.ini org.safs.EmbeddedDCHookDriver
 * <p> 
 * @author Carl Nagle
 * @see EmbeddedHookDriver
 * @see org.safs.tools.drivers.JSAFSDriver
 */
public class EmbeddedDCHookDriver extends EmbeddedHookDriver {

	static boolean enableLogs = false;
	
    public static final String SAFS_DRIVER_COMMANDS = "SAFS/EmbeddedDCDriverCommands";
	/**
	 * Constructor for DCJavaHook
	 */
	public EmbeddedDCHookDriver() {
		super(SAFS_DRIVER_COMMANDS);
	}

	/**
	 * Instantiates a default DCTestRecordHelper for this engine.
	 * @see JavaHook#getTRDData()
	 * @see DCTestRecordHelper
	 */
	@Override
	public TestRecordHelper getTRDData() {
          if (data == null){
            data = new DCTestRecordHelper();
            data.setSTAFHelper(getHelper());
            data.setDDGUtils(getGUIUtilities());
          }
          return data;
	}

	/**
	 * The class has the same config requirements as JSAFSDriver:
	 * <p>
	 * -Dsafs.project.config=pathTo\test.ini
	 * <p>
	 * Example:
	 * <p>
	 * %SAFSDIR%\jre\bin\java.exe -Dsafs.project.config=fullPath\to\test.ini org.safs.EmbeddedDCHookDriver
	 * <p> 
	 * @param args
	 * @author Carl Nagle
	 * @see EmbeddedHookDriver
	 * @see org.safs.tools.drivers.JSAFSDriver
	 */
    public static void main (String[] args) {

        EmbeddedDCHookDriver hook = new EmbeddedDCHookDriver();
        hook.run();

        System.out.println("EmbeddedDCHook Sleeping briefly....");
        try{ Thread.sleep(5000);}catch(Exception x){}
        System.out.println("EmbeddedDCHook shutting down....");
        
        hook.shutdown();        
  }
}

