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
package org.safs.model.examples.advanced;
import org.safs.model.components.EditBox;
import org.safs.model.components.PushButton;
import org.safs.model.components.Window;

/**
 * Sample Driver Component Map allowing the naming of application constants and components.
 * Note this type of map is required to duplicate the names used in the actual runtime 
 * SAFS App Map.  Because of this, we have the Driver AppMapGenerator tool that can automatically 
 * generate class files similar to this sample from existing SAFS App Maps.
 * <p>
 * Maps like this allow a Driver test to perform actions using syntax shown below:
 * <p>
 * <UL>result = jsafs.runComponentFunction(JSAFSMap.LoginWindow.Submit.click());</UL>
 * <P>
 * @author Carl Nagle -- FEB 02, 2011
 * @see org.safs.model.tools.AppMapGenerator
 */
public final class JSAFSMap {
	
	/** No use for a default constructor at this time. */
	private JSAFSMap(){}

	/**
	 * Easy-to-use access to Application Constants.
	 * The 'values' of the constants are the 'names' of the Application Constants stored in 
	 * the real runtime SAFS App Map(s). 
	 */
	public static class Constant {		

		/** No use for a default constructor at this time. */
		private Constant(){}

		public static final String EXEPath 			= "EXEPath";
		public static final String AConstant 		= "AConstant";
		public static final String AnotherConstant 	= "AnotherConstant";
	}	
	/**
	 * Easy-to-use access to LoginWindow Component names and types.
	 * Each of the Component types mapped to a named component is expected to match the 
	 * type of component as it is mapped in the real runtime SAFS App Map(s).  In this way,
	 * the references stored here can be used directly in Driver calls requiring a reference to 
	 * ComponentFunction components.
	 * <p>
	 * Mappings like this allow a Driver test to perform actions using syntax shown below:
	 * <p>
	 * <UL>result = jsafs.runComponentFunction(JSAFSMap.LoginWindow.Submit.click());</UL>
	 * <P>
	 * @see org.safs.tools.drivers.JSAFSDriver#runComponentFunction(org.safs.model.ComponentFunction)
	 */
	public static class LoginWindow {

		/** No use for a default constructor at this time. */
		private LoginWindow(){}

		/** Retrieve the windows string name without resorting to the Window object.*/
		public static String getName(){ return "LoginWindow";}
		public static final Window LoginWindow 	= new Window("LoginWindow");
		public static final EditBox UserId		= new EditBox(LoginWindow, "UserId");
		public static final EditBox Password	= new EditBox(LoginWindow, "Password");
		public static final PushButton Submit	= new PushButton(LoginWindow, "Submit");
		public static final PushButton Cancel	= new PushButton(LoginWindow, "Cancel");
	};
	/**
	 * Easy-to-use access to MainWindow Component names and types.
	 * Each of the Component types mapped to a named component is expected to match the 
	 * type of component as it is mapped in the real runtime SAFS App Map(s).  In this way,
	 * the references stored here can be used directly in Driver calls requiring a reference to 
	 * ComponentFunction components.
	 * <p>
	 * Mappings like this allow a Driver test to perform actions using syntax shown below:
	 * <p>
	 * <UL>result = jsafs.runComponentFunction(JSAFSMap.MainWindow.MainWindow.closeWindow());</UL>
	 * <P>
	 * @see org.safs.tools.drivers.JSAFSDriver#runComponentFunction(org.safs.model.ComponentFunction)
	 */
	public static class MainWindow {		

		/** No use for a default constructor at this time. */
		private MainWindow(){}

		/** Retrieve the windows string name without resorting to the Window object.*/
		public static String getName(){ return "MainWindow";}
		public static final Window MainWindow 	= new Window("MainWindow");
		// ...
	};
}
