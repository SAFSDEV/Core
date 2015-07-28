/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.examples.tables.step;

import org.safs.model.StepTestTable;
import org.safs.model.examples.tables.Variables;
import org.safs.model.examples.tables.appmap.LoginWin;

/**
 * @author Carl Nagle
 */
public class Logon extends StepTestTable {
	
	public Logon (String userid, String password){
		super("Logon");
		
		// the Suite test record
		addParameter(Variables.userid.getAssignment(userid));
		addParameter(Variables.password.getAssignment(password));
		
		//the Step table
	    add( LoginWin.UserName.setTextValue( Variables.userid.getReference() ));
		add( LoginWin.Password.setTextValue( Variables.password.getReference() ));
		add( LoginWin.OK.click() );
	}	
}
