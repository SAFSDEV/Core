/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
