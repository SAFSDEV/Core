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

/**
 * Generally used to store the full Classname for the main class started by the JVM.
 * Is only set by classes that know to use it.  Can only be set once per running JVM.
 * @author Carl Nagle
 */
public class MainClass {
	
	private static String mainclass = null;
	/**
	 * @param main classname to be stored as the internal main classname.  Can only be set once.
	 */
	public static void setMainClass(String classname){
		if (mainclass == null) mainclass = classname;
	}
	/**
	 * @return the internal saved main classname String.  Can be null if never set or deduced. 
	 */
	public static String getMainClass(){
		return mainclass;
	}
	
	/**
	 * Deduce the main classname started by the JVM.
	 * If the internally set mainclass is NOT set, this routine will also attempt to set it.
	 * @return the main classname as deduced by JVM Property values, or null.
	 */
	public static String deduceMainClass(){
		String theCommand = System.getProperty("sun.java.command").trim();
		if(theCommand == null || theCommand.length()== 0)
			theCommand = System.getProperty("JAVA_MAIN_CLASS").trim();
		if(theCommand == null || theCommand.length()==0) return null;
		String[] theSplit = theCommand.split(" ");
		String theClass = theSplit[0];
		setMainClass(theClass);
		return theClass;
	}
}
