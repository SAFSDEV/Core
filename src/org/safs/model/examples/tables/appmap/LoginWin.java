/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.examples.tables.appmap;

import org.safs.model.components.*;


/**
 * @author Carl Nagle
 */
public class LoginWin {

	public static Window LoginWin = new Window("LoginWin");
	public static ComboEditBox UserName = new ComboEditBox(LoginWin, "UserName");
	public static EditBox Password = new EditBox(LoginWin, "Password");
	public static PushButton OK = new PushButton(LoginWin, "OK");
	public static PushButton Cancel = new PushButton(LoginWin, "Cancel");
}
